package com.github.rthoth.xysplit;

import org.locationtech.jts.algorithm.RayCrossingCounter;
import org.locationtech.jts.geom.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.github.rthoth.xysplit.Location.IN;
import static com.github.rthoth.xysplit.Location.ON;
import static com.github.rthoth.xysplit.Location.OUT;
import static com.github.rthoth.xysplit.Side.EQ;
import static com.github.rthoth.xysplit.Side.LT;
import static com.github.rthoth.xysplit.SplitEvent.POSITION_COMPARATOR;
import static org.locationtech.jts.geom.Location.BOUNDARY;
import static org.locationtech.jts.geom.Location.INTERIOR;


public class PolygonSplitter extends Splitter<Polygon> {

	public final Reference reference;

	public PolygonSplitter(Reference reference) {
		this.reference = reference;
	}

	public SplitResult apply(Polygon polygon) {

		SeqBuilder shellBuilder = new SeqBuilder(polygon.getExteriorRing().getCoordinateSequence());

		Geometry lt, gt;
		if (!shellBuilder.isEmpty()) {
			List<Seq> ltEvents = new LinkedList<>();
			List<Seq> gtEvents = new LinkedList<>();

			for (int i = 0, l = polygon.getNumInteriorRing(); i < l; i++) {
				final SeqBuilder seqBuilder = new SeqBuilder(polygon.getInteriorRingN(i).getCoordinateSequence());
				ltEvents.add(seqBuilder.lt);
				gtEvents.add(seqBuilder.gt);
			}

			lt = new HalfPolygon(Side.LT, polygon, shellBuilder.lt, ltEvents).geometry;
			gt = new HalfPolygon(Side.GT, polygon, shellBuilder.gt, gtEvents).geometry;

		} else if (!polygon.isEmpty()) {
			CoordinateSequence shell = polygon.getExteriorRing().getCoordinateSequence();
			Side side = reference.classify(shell, 0);

			for (int i = 0, l = shell.size(); side == EQ && i < l; i++) {
				side = reference.classify(shell, i);
			}

			if (side == LT) {
				lt = polygon;
				gt = polygon.getFactory().createPolygon();
			} else {
				lt = polygon.getFactory().createPolygon();
				gt = polygon;
			}
		} else {
			gt = lt = polygon;
		}

		return new SplitResult(lt, gt, reference);
	}

	private static class Seq {

		public final CoordinateSequence sequence;
		public final List<SplitEvent> events;

		public Seq(CoordinateSequence sequence, List<SplitEvent> events) {
			this.sequence = sequence;
			this.events = events;
		}

		public boolean isEmpty() {
			return events.isEmpty();
		}

	}

	private class HalfPolygon {

		public final Geometry geometry;

		private final TreeSet<SplitEvent> allEvents = new TreeSet<>(POSITION_COMPARATOR);
		private final TreeSet<SplitEvent> boundary = new TreeSet<>(POSITION_COMPARATOR);
		private final HashMap<SplitEvent, TreeSet<SplitEvent>> eventToSequence = new HashMap<>();
		private final Side side;

		public HalfPolygon(Side side, Polygon original, Seq shell, List<Seq> holes) {
			this.side = side;

			boolean isInside = addToBoundary(shell.events);

			if (isInside) {
				LinkedList<Seq> untouchedHoles = new LinkedList<>();

				for (Seq hole : holes) {
					if (!addToBoundary(hole.events))
						untouchedHoles.addLast(hole);
				}

				LinkedList<PolyBuilder> created = new LinkedList<>();

				CoordinateSequence sequence;
				while (!boundary.isEmpty()) {
					T2<SplitEvent, Boolean> t2 = nextOrigin();
					sequence = createShell(t2._1, t2._1.location == IN, t2._2);
					if (sequence.size() > 3)
						created.addLast(new PolyBuilder(sequence));
					else
						throw new UnsupportedOperationException();
				}

				// Searching holes positions

				nextHole:
				for (Seq hole : untouchedHoles) {
					for (PolyBuilder builder : created) {
						if (builder.addIfContains(hole)) {
							continue nextHole;
						}
					}
				}

				if (created.size() > 1) {
					List<Polygon> polygons = created
									.stream()
									.map(x -> x.build(original.getFactory()))
									.collect(Collectors.toList());

					geometry = original.getFactory().createMultiPolygon(polygons.toArray(new Polygon[0]));
				} else {
					geometry = created.getFirst().build(original.getFactory());
				}
			} else {
				Side lSide = reference.classify(shell.sequence, 0);
				for (int i = 0, l = shell.sequence.size(); i < l && lSide == EQ; i++) {
					lSide = reference.classify(shell.sequence, i);
				}

				if (lSide == side) {
					geometry = original;
				} else {
					geometry = original.getFactory().createPolygon();
				}
			}

		}

		private void addSegment(SplitEvent start, SplitEvent stop, CoordinateSequenceBuilder builder) {
			if (start.location == IN) {

				if (start.coordinate != null)
					builder.add(start.coordinate);

				int stopIndex = stop.index != 0 ? stop.index - 1 : stop.sequence.size() - 2;
				builder.addRing(start.sequence, start.index, stopIndex, true);

				builder.add(stop.getCoordinate());

			} else {

				builder.add(start.getCoordinate());

				int startIndex = start.index != 0 ? start.index - 1 : start.sequence.size() - 2;
				builder.addRing(start.sequence, startIndex, stop.index, false);

				if (stop.coordinate != null)
					builder.add(stop.coordinate);

			}
		}

		/**
		 * @param events true when there is some IN/OUT event!
		 * @return
		 */
		private boolean addToBoundary(List<SplitEvent> events) {
			TreeSet<SplitEvent> set = new TreeSet<>(SplitEvent.INDEX_COMPARATOR);
			int start = boundary.size();

			for (SplitEvent evt : events) {
				set.add(evt);
				eventToSequence.put(evt, set);

				allEvents.add(evt);
				if (evt.location != ON)
					boundary.add(evt);
			}

			return boundary.size() > start;
		}

		private CoordinateSequence createShell(final SplitEvent origin, boolean forward, boolean ascScanLine) {
			SplitEvent start = origin, stop = nextEvent(origin, forward);
			CoordinateSequenceBuilder builder = new CoordinateSequenceBuilder();
			boolean tryClose, shouldContinue;

			do {
				shouldContinue = false;
				if (stop.location != ON) {
					addSegment(start, stop, builder);
					boundary.remove(stop);
					switch (POSITION_COMPARATOR.compare(origin, stop)) {
						case -1:
							start = boundary.lower(stop);
							break;

						case 1:
							start = boundary.higher(stop);
							break;

						default:
							throw new UnsupportedOperationException();
					}

					if (start != origin && start.location != ON) {
						forward = start.location == IN;
						stop = nextEvent(start, forward);
						boundary.remove(start);
					}
				} else {
					if (ascScanLine)
						tryClose = POSITION_COMPARATOR.compare(stop, origin) == 1;
					else
						tryClose = POSITION_COMPARATOR.compare(stop, origin) == -1;

					if (tryClose) {
						SplitEvent maybeOrigin = ascScanLine ? boundary.lower(stop) : boundary.higher(stop);

						if (maybeOrigin == origin || !inSameSequence(maybeOrigin, origin)) {
							addSegment(start, stop, builder);

							// Add a new origin, this point breaks the new polygon.
							SplitEvent newOrigin = stop.withLocation(start.location);
							boundary.add(newOrigin);

							if (maybeOrigin != origin)
								boundary.remove(maybeOrigin);

							allEvents.remove(stop);
							allEvents.add(newOrigin);
							eventToSequence.put(newOrigin, eventToSequence.get(start));
							start = maybeOrigin;
							forward = start.location == IN;
							stop = nextEvent(start, forward);
							continue;
						}
					}

					stop = nextEvent(stop, forward);
					shouldContinue = true;
				}
			} while (start != origin || shouldContinue);

			boundary.remove(start);
			return builder.add(origin.getCoordinate()).build();
		}

		private boolean inSameSequence(SplitEvent a, SplitEvent b) {
			return eventToSequence.get(a) == eventToSequence.get(b);
		}

		private SplitEvent nextEvent(SplitEvent start, boolean forward) {
			TreeSet<SplitEvent> sequence = eventToSequence.get(start);
			SplitEvent next;

			if (forward) {
				next = sequence.higher(start);
				if (next == null)
					next = sequence.first();
			} else {
				next = sequence.lower(start);
				if (next == null)
					next = sequence.last();
			}

			return next;
		}

		private T2<SplitEvent, Boolean> nextOrigin() {
			if (boundary.first().location == IN) {
				return new T2(boundary.first(), true);
			} else {
				return new T2(boundary.last(), false);
			}
		}

		private class PolyBuilder {

			public final CoordinateSequence shell;
			public final LinkedList<CoordinateSequence> holes = new LinkedList<>();

			public PolyBuilder(CoordinateSequence shell) {
				this.shell = shell;
			}

			public boolean addIfContains(Seq hole) {
				CoordinateSequence sequence = hole.sequence;
				Location location = reference.classify(sequence, 0).location(side);

				if (location == OUT)
					return false;

				int pointLocation = RayCrossingCounter.locatePointInRing(sequence.getCoordinate(0), shell);
				for (int i = 1, l = sequence.size(); i < l && pointLocation == BOUNDARY; i++) {
					location = reference.classify(sequence, i).location(side);
					if (location == OUT)
						return false;
					pointLocation = RayCrossingCounter.locatePointInRing(sequence.getCoordinate(i), shell);
				}

				if (pointLocation == INTERIOR) {
					holes.addLast(sequence);
					return true;
				} else
					return false;
			}

			public Polygon build(GeometryFactory factory) {
				LinearRing shell = factory.createLinearRing(this.shell);
				LinearRing[] holes = new LinearRing[this.holes.size()];
				for (int i = 0; i < holes.length; i++) {
					holes[i] = factory.createLinearRing(this.holes.get(i));
				}

				return factory.createPolygon(shell, holes);
			}
		}
	}

	private class SeqBuilder {

		public final Seq lt;

		public final Seq gt;

		public SeqBuilder(CoordinateSequence sequence) {
			SplitSequence splitSequence = new SplitSequence.Poly(reference, sequence);
			lt = new Seq(sequence, splitSequence.ltEvents);
			gt = new Seq(sequence, splitSequence.gtEvents);
		}

		public boolean isEmpty() {
			return lt.isEmpty() || gt.isEmpty();
		}

	}

}
