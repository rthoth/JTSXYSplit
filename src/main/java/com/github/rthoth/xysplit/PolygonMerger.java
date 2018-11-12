package com.github.rthoth.xysplit;

import com.github.rthoth.xysplit.MergeEvent.Node;
import org.locationtech.jts.geom.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.rthoth.xysplit.IO.I_O;
import static com.github.rthoth.xysplit.IO.O_I;
import static com.github.rthoth.xysplit.InOnOut.*;
import static com.github.rthoth.xysplit.Side.GT;
import static com.github.rthoth.xysplit.Side.LT;

public class PolygonMerger extends AbstractMerger<Polygon> {

	private final MergeNodeSequencer.Poly sequencer;
	private final PolySideMerger sideMerger;

	/**
	 * @param o  Origin
	 * @param oI Origin Index
	 * @param r  Reference
	 * @param rI Reference Index
	 * @param t  Target
	 * @param tI Target Index
	 * @return
	 */
	public static double crossProduct(CoordinateSequence o, int oI, CoordinateSequence r, int rI, CoordinateSequence t, int tI) {
		double xa = r.getX(rI) - o.getX(oI), ya = r.getY(rI) - o.getY(oI);
		double xb = t.getX(tI) - o.getX(oI), yb = t.getY(tI) - o.getY(oI);
		double la = Math.sqrt(xa * xa + ya * ya);
		double lb = Math.sqrt(xb * xb + yb * yb);

		xa /= la;
		ya /= la;
		xb /= lb;
		yb /= lb;

		return xa * yb - ya * xb;
	}

	public PolygonMerger(Reference reference, double offset) {
		super(reference, offset);
		sequencer = new MergeNodeSequencer.Poly(reference, offset);
		sideMerger = new PolySideMerger(sequencer);
	}

	@Override
	public Geometry apply(GeometryFactory factory, Iterable<Polygon> lts, Iterable<Polygon> gts) {
		return new Merger(factory, lts, gts).geometry;
	}

	public static class Unity {

		protected final CoordinateSequence shell;
		protected final LinkedList<CoordinateSequence> holes = new LinkedList<>();
		protected final NavigableSet<Node> nodes;

		public Unity(CoordinateSequence shell, List<Node> nodes, List<CoordinateSequence> holes) {
			this.shell = shell;
			TreeSet<Node> _nodes = new TreeSet<>(Node.INDEX_COMPARATOR);
			_nodes.addAll(nodes);
			this.nodes = Collections.unmodifiableNavigableSet(_nodes);
			this.holes.addAll(holes);
		}

		public Unity(CoordinateSequence shell, MergeNodeSequencer.Result result, Polygon original) {
			this(shell, result.nodes, extractHoles(result.circles, original));
		}

		public static List<CoordinateSequence> extractHoles(List<CoordinateSequence> circles, Polygon polygon) {
			Stream<CoordinateSequence> polygonHoles = IntStream.range(0, polygon.getNumInteriorRing())
							.mapToObj(i -> polygon.getInteriorRingN(i).getCoordinateSequence());

			return Stream.concat(polygonHoles, circles.stream()).collect(Collectors.toList());
		}

		public Polygon build(GeometryFactory factory) {
			LinearRing shell = factory.createLinearRing(this.shell);
			LinearRing[] holes = this.holes
							.stream()
							.map(factory::createLinearRing)
							.toArray(LinearRing[]::new);

			return factory.createPolygon(shell, holes);
		}

		public boolean isEmpty() {
			return shell.size() == 0;
		}
	}

	private class Merger {

		public final Geometry geometry;

		private GeometryFactory factory;

		private final NavigableMap<Double, MergeEvent<Unity>> boundary = new TreeMap<>();
		private final Map<Node, MergeEvent<Unity>> nodeToEvent = new HashMap<>();
		private final Map<Node, Unity> nodeToUnity = new HashMap<>();
		private final Set<Unity> visitedUnities = new HashSet<>();
		private final NavigableSet<Double> positions = new TreeSet<>();
		private final RangeMap<PolyBuilder> intersections = new RangeMap.TreeMap<>();

		private MergeEvent<Unity> originEvent = null;
		private MergeEvent<Unity> startEvent = null;
		private MergeEvent<Unity> stopEvent = null;
		private Side side = GT;
		private Node startNode;
		private Node stopNode;
		private boolean forward;
		private IO io;

		Merger(GeometryFactory factory, Iterable<Polygon> lts, Iterable<Polygon> gts) {
			List<Unity> gtPolies = sideMerger.apply(GT, gts);
			List<Unity> ltPolies = sideMerger.apply(LT, lts);

			this.factory = factory;

			TreeMap<Double, Node> ltBorder = new TreeMap<>();
			TreeMap<Double, Node> gtBorder = new TreeMap<>();

			LinkedList<Unity> untouched = new LinkedList<>();

			BiConsumer<Unity, NavigableMap<Double, Node>> addToBorder = (unity, border) -> {
				for (Node node : unity.nodes) {
					nodeToUnity.put(node, unity);
					border.put(node.position, node);
				}

				if (unity.nodes.isEmpty() && unity.shell.size() != 0)
					untouched.addLast(unity);
			};

			ltPolies.forEach(unity -> addToBorder.accept(unity, ltBorder));
			gtPolies.forEach(unity -> addToBorder.accept(unity, gtBorder));

			MergeScanLine scanLine = new MergeScanLine(offset, ltBorder, gtBorder);

			AtomicReference<Unity> ltPoly = new AtomicReference<>();
			AtomicReference<Unity> gtPoly = new AtomicReference<>();

			BiConsumer<Node, AtomicReference<Unity>> touchPoly = (node, poly) -> {
				if (node != null) {
					Unity _unity = nodeToUnity.get(node);
					if (_unity != poly.get())
						poly.set(_unity);
					else
						poly.set(null);
				}
			};

			for (T2<Node, Node> t2 : scanLine) {
				touchPoly.accept(t2._1, ltPoly);
				touchPoly.accept(t2._2, gtPoly);

				if (t2._1 != null && t2._2 != null) {
					Node lt = t2._1;
					Node gt = t2._2;

					if (!(lt.inOnOut == ON && gt.inOnOut == ON)) {
						double position = (lt.position + gt.position) / 2;
						MergeEvent<Unity> event = new MergeEvent<>(position, lt, gt);
						boundary.put(position, event);
						nodeToEvent.put(lt, event);
						nodeToEvent.put(gt, event);
					}

				} else if (t2._1 != null) {
					unconnectedEvent(t2._1, new MergeEvent<>(t2._1.position, t2._1, null, gtPoly.get()));
				} else {
					unconnectedEvent(t2._2, new MergeEvent<>(t2._2.position, null, t2._2, ltPoly.get()));
				}
			}

			List<Polygon> polygons = merge()
							.stream()
							.map(PolyBuilder::build)
							.collect(Collectors.toList());

			polygons.addAll(untouched.stream().map(x -> x.build(factory)).collect(Collectors.toList()));

			if (polygons.size() == 1) {
				geometry = polygons.get(0);
			} else if (polygons.size() > 1) {
				geometry = factory.createMultiPolygon(polygons.toArray(new Polygon[0]));
			} else {
				geometry = factory.createPolygon();
			}
		}

		private List<PolyBuilder> merge() {
			LinkedList<PolyBuilder> created = new LinkedList<>();

			while (!boundary.isEmpty()) {
				searchOrigin();
				PolyBuilder builder = createNewPoly();
				if (builder != null)
					created.addLast(builder);
			}

			return created;
		}

		private PolyBuilder createNewPoly() {
			startEvent = originEvent;
			setSide();

			PolyBuilder container = intersections.get(originEvent.position);

			Deque<CoordinateSequenceBuilder.Builder> segments = searchSegments();
			CoordinateSequenceBuilder builder = new CoordinateSequenceBuilder();

			CoordinateSequenceBuilder.Builder last = segments.peekLast();

			for (CoordinateSequenceBuilder.Builder current : segments) {
				int startIndex;
				if (shouldIncludeMergePoint(last, current)) {
					startIndex = current.start;
				} else {
					if (current.forward) {
						startIndex = (current.start + 1) % (current.sequence.size() - 1);
					} else {
						startIndex = (current.start - 1) % (current.sequence.size() - 1);
						if (startIndex < 0)
							startIndex += current.sequence.size() + 1;
					}
				}

				builder.addRing(current.sequence, startIndex, current.stop, current.forward);
				last = current;
			}

			PolyBuilder ret = null;

			if (container == null) {
				ret = new PolyBuilder(builder.closeAndBuild());

				Iterator<Double> it = positions.iterator();
				while (it.hasNext()) {
					Double p1 = it.next();
					if (it.hasNext()) {
						Double p2 = it.next();
						intersections.add(p1, p2, ret);
					}
				}
			} else {
				container.holes.addLast(builder.closeAndBuild());
			}

			visitedUnities.clear();
			positions.clear();

			return ret;
		}

		private Deque<CoordinateSequenceBuilder.Builder> searchSegments() {
			LinkedList<CoordinateSequenceBuilder.Builder> ret = new LinkedList<>();

			do {
				searchStop();
				addBorderIntersection();
				side = side.invert();
				visitedUnities.add(nodeToUnity.get(startNode));
				//positions.add(startNode.inOnOut);
				//positions.add(stopNode.inOnOut);

				CoordinateSequenceBuilder.Builder segment;

				if (stopEvent.get(side) != null) {
					// it should jump to other side!

					int stopIndex, limit = stopNode.sequence.size() - 1;
					if (forward) {
						stopIndex = (stopNode.index - 1) % limit;
						if (stopIndex < 0)
							stopIndex += limit;
					} else {
						stopIndex = (stopNode.index + 1) % limit;
					}

					segment = new CoordinateSequenceBuilder.Builder(startNode.sequence, startNode.index, stopIndex, forward, true);
				} else {
					segment = new CoordinateSequenceBuilder.Builder(startNode.sequence, startNode.index, stopNode.index, forward, true);
				}

				ret.addLast(segment);
				searchStart();

			} while (startEvent != originEvent);

			return ret;
		}

		private <T> T getIfNotNull(Map.Entry<?, T> entry) {
			if (entry != null)
				return entry.getValue();
			else
				return null;
		}

		private MergeEvent<Unity> getIfTouches(MergeEvent<Unity> event, Unity unity) {
			if (event != null) {
				if (event.touched == unity)
					return event;

				if (event.touched == null && event.get(side.invert()) == null) {
					Node node = event.get(side);
					return (node != null && nodeToUnity.get(node) == unity) ? event : null;
				}
			}

			return null;
		}

		private <T> T getHigher(NavigableSet<T> values, T value) {
			T ret = values.higher(value);
			return ret != null ? ret : values.first();
		}

		private <T> T getLower(NavigableSet<T> values, T value) {
			T ret = values.lower(value);
			return ret != null ? ret : values.last();
		}

		private void searchOrigin() {
			MergeEvent<Unity> first = boundary.firstEntry().getValue();
			MergeEvent<Unity> last = boundary.lastEntry().getValue();

			if (first.canBeOrigin()) {
				originEvent = first;
				io = O_I;
				return;
			}

			if (last.canBeOrigin()) {
				originEvent = first;
				io = I_O;
				return;
			}

			originEvent = first;
			io = O_I;

			if (canBeOrigin(first.lt) || canBeOrigin(first.gt)) {
				return;
			}

			originEvent = last;
			io = I_O;

			if (canBeOrigin(last.lt) || canBeOrigin(last.gt)) {
				return;
			}

			throw new UnsupportedOperationException();
		}

		private void addBorderIntersection() {
//			double lower, upper;
//			switch (Double.compare(startEvent.inOnOut, stopEvent.inOnOut)) {
//				case -1:
//					lower = startEvent.inOnOut;
//					upper = stopEvent.inOnOut;
//					break;
//
//				case 1:
//					lower = stopEvent.inOnOut;
//					upper = startEvent.inOnOut;
//					break;
//
//				default:
//					throw new UnsupportedOperationException();
//			}
//
//			boolean lowerIsOut = positions.headSet(lower, false).size() % 2 == 0;
//			boolean upperIsOut = positions.tailSet(upper, false).size() % 2 == 0;
//
//			NavigableSet<Double> removable;
//
//			if (lowerIsOut) {
//				positions.add(lower);
//				removable = positions.tailSet(lower, false);
//			} else {
//				removable = positions.tailSet(lower, true);
//			}
//
//			if (upperIsOut) {
//				positions.add(upper);
//				removable = removable.headSet(upper, false);
//			} else {
//				removable = removable.headSet(upper, true);
//			}
//
//			removable.clear();

			positions.add(startEvent.position);
			positions.add(stopEvent.position);
		}

		private boolean canBeOrigin(Node node) {
			if (node != null)
				return node.inOnOut == IN;
			else
				return false;
		}

		private void searchStart() {
			boundary.remove(stopEvent.position);

			if (stopEvent.get(side) != null) {
				startEvent = stopEvent;
			} else {
				MergeEvent<Unity> nextEvent = null;

				if (stopEvent.touched != null) {
					switch (io) {
						case I_O:
							nextEvent = getIfNotNull(boundary.higherEntry(stopEvent.position));
							nextEvent = getIfTouches(nextEvent, stopEvent.touched);
							if (nextEvent == null)
								nextEvent = getIfNotNull(boundary.lowerEntry(stopEvent.position));
							break;

						case O_I:
							nextEvent = getIfNotNull(boundary.lowerEntry(stopEvent.position));
							nextEvent = getIfTouches(nextEvent, stopEvent.touched);
							if (nextEvent == null)
								nextEvent = getIfNotNull(boundary.higherEntry(stopEvent.position));
							break;
					}
				} else {
					switch (io) {
						case I_O:
							nextEvent = getIfNotNull(boundary.lowerEntry(stopEvent.position));
							break;

						case O_I:
							nextEvent = getIfNotNull(boundary.higherEntry(stopEvent.position));
							break;
					}
				}

				if (nextEvent != null) {

					if (nextEvent.get(side) == null) {
						io = io.invert();
						side = side.invert();
					}

					boundary.remove(nextEvent.position);
					startEvent = nextEvent;
				} else
					throw new UnsupportedOperationException();
			}
		}

		private void searchStop() {
			startNode = startEvent.get(side);
			stopNode = null;
			io = io.invert();

			if (startNode != null) {
				Unity unity = nodeToUnity.get(startNode);

				if (startNode.inOnOut == IN) {
					stopNode = getHigher(unity.nodes, startNode);
					forward = true;
				} else if (startNode.inOnOut == OUT) {
					stopNode = getLower(unity.nodes, startNode);
					forward = false;
				} else {
					throw new UnsupportedOperationException();
				}

				stopEvent = nodeToEvent.get(stopNode);
			} else {
				throw new UnsupportedOperationException();
			}
		}

		private void setSide() {
			Node lt = startEvent.lt, gt = startEvent.gt;

			if (lt != null && gt != null) {
				if (lt.inOnOut != gt.inOnOut) {
					side = lt.inOnOut == IN ? LT : GT;
				} else {
					side = side.invert();
				}
			} else if (lt != null) {
				side = LT;
			} else if (gt != null) {
				side = GT;
			} else {
				throw new UnsupportedOperationException();
			}
		}

		private boolean shouldIncludeMergePoint(CoordinateSequenceBuilder.Builder last, CoordinateSequenceBuilder.Builder current) {
			int targetIndex;
			CoordinateSequence curSeq = current.sequence, lastSeq = last.sequence;
			if (current.forward) {
				targetIndex = (current.start + 1) % (curSeq.size() - 1);
			} else {
				targetIndex = (current.start - 1) % (curSeq.size() - 1);
				if (targetIndex < 0)
					targetIndex += curSeq.size() - 1;
			}

			double product = crossProduct(lastSeq, last.stop, curSeq, targetIndex, curSeq, current.start);
			return Math.abs(product) > offset;
		}

		private void unconnectedEvent(Node node, MergeEvent<Unity> event) {
			boundary.put(event.position, event);
			nodeToEvent.put(node, event);
		}

		private class PolyBuilder {

			final CoordinateSequence shell;
			final List<Unity> polies;
			final LinkedList<CoordinateSequence> holes = new LinkedList<>();

			PolyBuilder(CoordinateSequence shell) {
				this.shell = shell;
				this.polies = new ArrayList<>(visitedUnities);
			}

			Polygon build() {
				LinkedList<CoordinateSequence> holes = new LinkedList<>();
				holes.addAll(this.holes);
				polies.stream().flatMap(unity -> unity.holes.stream()).forEach(holes::addLast);

				return factory.createPolygon(
								factory.createLinearRing(shell),
								holes.stream().map(factory::createLinearRing).toArray(i -> new LinearRing[i])
				);
			}
		}
	}
}
