package com.github.rthoth.xysplit;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.geom.impl.PackedCoordinateSequence;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class PolygonMerger extends GeometryMerger {

	protected final Reference reference;
	protected final double offset;

	public PolygonMerger(Reference reference, double offset) {
		this.reference = reference;
		this.offset = Math.abs(offset);
	}

	public Geometry apply(List<Polygon> ltPolygons, List<Polygon> gtPolygons) {
		List<Poly> ltShells = ltPolygons
						.stream()
						.map(Poly::new)
						.collect(Collectors.toList());

		List<Poly> gtShells = gtPolygons
						.stream()
						.map(Poly::new)
						.collect(Collectors.toList());

		GeometryFactory factory = !ltPolygons.isEmpty() ? ltPolygons.get(0).getFactory() : gtPolygons.get(0).getFactory();
		return new Merger(factory, ltShells, gtShells).result;
	}

	private static class Poly {

		public final CoordinateSequence shell;
		public final List<CoordinateSequence> holes;

		public Poly(Polygon polygon) {
			shell = polygon.getExteriorRing().getCoordinateSequence();
			holes = IntStream
							.range(0, polygon.getNumInteriorRing())
							.mapToObj(i -> polygon.getInteriorRingN(i).getCoordinateSequence())
							.collect(Collectors.toList());
		}

		public Poly(CoordinateSequence shell) {
			this.shell = shell;
			holes = new LinkedList<>();
		}

		public Polygon toPolygon(GeometryFactory factory) {
			List<LinearRing> holes = this.holes.stream().map(factory::createLinearRing).collect(Collectors.toList());
			return factory.createPolygon(factory.createLinearRing(shell), holes.toArray(new LinearRing[0]));
		}
	}

	private class Merger {

		private final RangeMap<Poly> intersections = new RangeMap.TreeMap<>();
		private final HashMap<CoordinateSequence, Poly> sequenceToPoly = new HashMap<>();
		private final MergeSequence mergeSequence;
		private final TreeSet<MergeEvent> pendent = new TreeSet<>();

		public final Geometry result;
		private final NavigableSet<MergeEvent.Node> nodeBoundary;

		public Merger(GeometryFactory factory, List<Poly> lts, List<Poly> gts) {

			Function<Poly, CoordinateSequence> mapper = poly -> {
				sequenceToPoly.put(poly.shell, poly);
				return poly.shell;
			};

			List<CoordinateSequence> ltSequences = lts
							.stream()
							.map(mapper)
							.collect(Collectors.toList());

			List<CoordinateSequence> gtSequences = gts
							.stream()
							.map(mapper)
							.collect(Collectors.toList());

			mergeSequence = new MergeSequence.Poly(reference, offset, ltSequences, gtSequences);
			nodeBoundary = new TreeSet<>(mergeSequence.getNodeBoundary());

			for (MergeEvent event : mergeSequence.getEvents()) {
				pendent.add(event);
			}

			LinkedList<Poly> createdPolies = new LinkedList<>();

			while (!pendent.isEmpty()) {
				Poly poly = makePoly(pendent.pollFirst());
				if (poly != null) {
					createdPolies.add(poly);
				}
			}

			for (MergeEvent.Node node : nodeBoundary) {
				if (node.location == Location.IN)
					makeHole(node);
			}

			Consumer<CoordinateSequence> consumer = sequence -> {
				createdPolies.add(sequenceToPoly.get(sequence));
			};

			mergeSequence.getLTUnconnected().forEach(consumer);
			mergeSequence.getGTUnconnected().forEach(consumer);

			List<Polygon> polygons = createdPolies.stream()
							.map(x -> x.toPolygon(factory))
							.collect(Collectors.toList());

			if (polygons.size() == 1) {
				result = polygons.get(0);
			} else if (polygons.size() > 1) {
				result = factory.createMultiPolygon(polygons.toArray(new Polygon[0]));
			} else {
				result = factory.createPolygon();
			}
		}

		private Poly makePoly(final MergeEvent origin) {
			// It checks if the new shell is inside another shell, in this case is a hole!
			final Poly container = intersections.get(origin.position);
			final CoordinateSequenceBuilder builder = new CoordinateSequenceBuilder();
			final SortedSet<MergeEvent> visitedEvents = new TreeSet<>();
			final HashSet<Poly> visitedPolies = new HashSet<>();

			Side side = origin.nextSide(Side.LT);
			MergeEvent nextEvent = origin;

			final MergeEvent.Node originNode = nextEvent.getNode(side);
			MergeEvent.Node startNode = originNode;

			do {
				if (nextEvent != null)
					visitedEvents.add(nextEvent);

				visitedPolies.add(sequenceToPoly.get(startNode.sequence));

				MergeEvent.Node stopNode = mergeSequence.getConnected(startNode);

				nodeBoundary.remove(startNode);
				nodeBoundary.remove(stopNode);

				nextEvent = mergeSequence.getEvent(stopNode);
				final int limit = startNode.sequence.size() - 1;

				if (startNode.location == Location.IN) {
					int stopIndex = (stopNode.index - 1) % limit;
					if (nextEvent != null) {
						if (stopIndex < 0)
							stopIndex = limit + stopIndex;
					} else {
						stopIndex = stopNode.index;
					}

					builder.addRing(startNode.sequence, startNode.index, stopIndex, true);
				} else {
					int stopIndex = (stopNode.index + 1) % limit;
					if (nextEvent == null) {
						stopIndex = stopNode.index;
					}

					builder.addRing(startNode.sequence, startNode.index, stopIndex, false);
				}

				if (nextEvent != null) {
					pendent.remove(nextEvent);
					side = side.invert();
					startNode = nextEvent.getNode(side);
				} else {
					if (!nodeBoundary.isEmpty()) {
						if (stopNode.position <= originNode.position) {
							startNode = checkEvent(nodeBoundary.higher(stopNode));
							if (startNode == null)
								startNode = checkEvent(nodeBoundary.lower(stopNode));

						} else {
							startNode = checkEvent(nodeBoundary.lower(stopNode));
							if (startNode == null)
								startNode = checkEvent(nodeBoundary.higher(stopNode));
						}

						side = nextSideFrom(startNode);
					} else {
//						throw new XYException.Merge(String.format("No way from %s!", stopNode));

						nextEvent = origin;
					}
				}
			} while (nextEvent != origin);

			builder.add(originNode.getCoordinate());

			if (container == null) {
				Poly poly = new Poly(builder.build());

				for (Poly other : visitedPolies) {
					poly.holes.addAll(other.holes);
				}

				Iterator<MergeEvent> it = visitedEvents.iterator();
				while (it.hasNext()) {
					MergeEvent start = it.next();
					if (it.hasNext()) {
						MergeEvent stop = it.next();
						intersections.add(start.position, stop.position, poly);
					}
				}

				return poly;
			} else {
				container.holes.add(builder.build());
				return null;
			}
		}

		private void makeHole(final MergeEvent.Node in) {
			CoordinateSequence sequence = in.sequence;
			Poly container = intersections.get(in.position);
			MergeEvent.Node out = mergeSequence.getConnected(in);

			if (container != null) {
				CoordinateSequenceBuilder builder = new CoordinateSequenceBuilder();
				builder.addRing(sequence, in.index, out.index, true);
				builder.add(in.getCoordinate());
				container.holes.add(builder.build());
			}
		}

		private Side nextSideFrom(MergeEvent.Node node) {
			int limit = node.sequence.size();
			int index;
			if (node.location == Location.IN)
				index = (node.index + 1) % limit;
			else {
				index = (node.index - 1) % limit;
				if (index < 0)
					index = limit + index;
			}

			return reference.classify(node.sequence, index, offset);
		}

		private MergeEvent.Node checkEvent(MergeEvent.Node node) {
			return node != null ? (mergeSequence.getEvent(node) == null ? node : null) : null;
		}
	}
}