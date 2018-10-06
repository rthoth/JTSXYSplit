package com.github.rthoth.xysplit;

import org.locationtech.jts.algorithm.RayCrossingCounter;
import org.locationtech.jts.geom.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.rthoth.xysplit.Location.*;
import static com.github.rthoth.xysplit.IO.I_O;
import static com.github.rthoth.xysplit.IO.O_I;
import static com.github.rthoth.xysplit.Side.GT;
import static com.github.rthoth.xysplit.Side.LT;
import static com.github.rthoth.xysplit.XY.X;
import static org.locationtech.jts.geom.Location.BOUNDARY;
import static org.locationtech.jts.geom.Location.INTERIOR;


class PolygonSplitter extends AbstractSplitter<Polygon> {

	PolygonSplitter(Reference reference, double offset) {
		super(new SplitSequencer.Poly(reference, offset));
	}

	private SplitResult split(Polygon polygon, Function<CoordinateSequence, SplitSequencer.Result> sequencer) {
		CoordinateSequence shell = polygon.getExteriorRing().getCoordinateSequence();
		SplitSequencer.Result shellSplitResult = sequencer.apply(shell);

		ArrayList<Unity> ltHoles = new ArrayList<>(polygon.getNumInteriorRing());
		ArrayList<Unity> gtHoles = new ArrayList<>(polygon.getNumInteriorRing());

		for (int i = 0, l = polygon.getNumInteriorRing(); i < l; i++) {
			CoordinateSequence sequence = polygon.getInteriorRingN(i).getCoordinateSequence();
			SplitSequencer.Result holeSplitResult = sequencer.apply(sequence);

			ltHoles.add(new Unity(holeSplitResult.lt, sequence));
			gtHoles.add(new Unity(holeSplitResult.gt, sequence));
		}

		Geometry lt = new Splitter(LT, new Unity(shellSplitResult.lt, shell), ltHoles, polygon.getFactory()).result;
		Geometry gt = new Splitter(GT, new Unity(shellSplitResult.gt, shell), gtHoles, polygon.getFactory()).result;

		return new SplitResult(lt, gt);
	}

	public SplitResult apply(Polygon polygon) {
		return split(polygon, sequencer);
	}

	@Override
	public SplitResult apply(Polygon polygon, final int padding) {
		return split(polygon, sequence -> sequencer.apply(sequence, padding));
	}

	private class Unity {

		final List<SplitEvent> events;
		protected final CoordinateSequence sequence;

		Unity(List<SplitEvent> events, CoordinateSequence sequence) {
			this.events = events;
			this.sequence = sequence;
		}
	}

	private class Splitter {

		private final Geometry result;
		private final NavigableSet<SplitEvent> boundary = new TreeSet<>(SplitEvent.POSITION_COMPARATOR);

		private final Map<SplitEvent, NavigableSet<SplitEvent>> eventToEvents = new HashMap<>();
		private final GeometryFactory factory;

		private boolean forward;

		private SplitEvent origin;
		private SplitEvent start;
		private SplitEvent stop;

		private IO io;
		private Side side;

		private final LinkedList<Unity> untouched = new LinkedList<>();

		Splitter(Side side, Unity shell, List<Unity> holes, GeometryFactory factory) {
			this.side = side;
			this.factory = factory;

			if (shell.events.size() > 1) {
				TreeSet<SplitEvent> events = new TreeSet<>(SplitEvent.INDEX_COMPARATOR);

				for (SplitEvent event : shell.events) {
					if (boundary.add(event))
						events.add(event);
					else {
						removeColision(event, events);
					}
				}

				for (SplitEvent event : events)
					eventToEvents.put(event, events);

				for (Unity hole : holes) {
					if (hole.events.size() > 0) {
						events = new TreeSet<>(SplitEvent.INDEX_COMPARATOR);
						for (SplitEvent event : hole.events) {
							if (boundary.add(event))
								events.add(event);
							else {
								removeColision(event, events);
							}
						}

						for (SplitEvent event : events)
							eventToEvents.put(event, events);

					} else {
						untouched.addLast(hole);
					}
				}

				List<Polygon> ret = createPolygons(untouched);

				if (ret.size() == 1) {
					result = ret.get(0);
				} else if (ret.size() > 1) {
					result = factory.createMultiPolygon(ret.toArray(new Polygon[0]));
				} else {
					result = factory.createPolygon();
				}
			} else {
				if (isInside(shell)) {
					LinearRing[] _holes = holes
									.stream()
									.map(x -> factory.createLinearRing(x.sequence))
									.toArray(LinearRing[]::new);

					result = factory.createPolygon(factory.createLinearRing(shell.sequence), _holes);
				} else {
					result = factory.createPolygon();
				}
			}
		}

		@SuppressWarnings("ConstantConditions")
		private void removeColision(SplitEvent event, TreeSet<SplitEvent> events) {
			SplitEvent other = boundary.ceiling(event);
			if (other.position == event.position) {
				boundary.remove(other);
				events.remove(other);
			} else {
				throw new UnsupportedOperationException();
			}
		}

		private <T> T getHigher(NavigableSet<T> set, T value) {
			T ret = set.higher(value);
			return ret != null ? ret : set.first();
		}

		private <T> T getLower(NavigableSet<T> set, T value) {
			T ret = set.lower(value);
			return ret != null ? ret : set.last();
		}

		private boolean isInside(Unity shell) {
			for (int i = 0, l = shell.sequence.size(); i < l; i++) {
				switch (sequencer.reference.classify(shell.sequence, i, sequencer.offset).location(side)) {
					case IN:
						return true;

					case OUT:
						return false;
				}
			}

			return false;
		}

		private Poly createPoly() {
			searchOrigin();
			start = origin;
			CoordinateSequenceBuilder builder = new CoordinateSequenceBuilder();

			do {
				searchStop();

				if (forward) {
					if (start.coordinate != null)
						builder.add(start.coordinate);

					if (stop.coordinate != null) {
						int stopIndex = stop.index != 0 ? stop.index - 1 : stop.sequence.size() - 2;
						builder.addRing(start.sequence, start.index, stopIndex, forward);
						builder.add(stop.coordinate);
					} else {
						builder.addRing(start.sequence, start.index, stop.index, forward);
					}
				} else {
					if (start.coordinate != null) {

						builder.add(start.coordinate);

						int startIndex = start.index != 0 ? start.index - 1 : start.sequence.size() - 2;
						builder.addRing(start.sequence, startIndex, stop.index, forward);

						if (stop.coordinate != null)
							builder.add(stop.coordinate);

					} else {
						builder.addRing(start.sequence, start.index, stop.index, forward);
						if (stop.coordinate != null)
							builder.add(stop.coordinate);
					}
				}

				searchStart();

			} while (start != origin);

			return new Poly(builder.closeAndBuild());
		}

		private List<Polygon> createPolygons(Deque<Unity> untouched) {
			LinkedList<Poly> ret = new LinkedList<>();

			while (!boundary.isEmpty()) {
				ret.add(createPoly());
			}

			nextHole:
			for (Unity hole : untouched) {

				curPoly:
				for (Poly poly : ret) {
					for (int i = 0, l = hole.sequence.size(); i < l; i++) {
						int loc = RayCrossingCounter.locatePointInRing(hole.sequence.getCoordinate(i), poly.shell);
						if (loc != BOUNDARY) {
							if (loc == INTERIOR) {
								poly.holes.add(hole.sequence);
								continue nextHole;
							} else {
								break curPoly;
							}
						}
					}
				}
			}

			return ret.stream().map(Poly::build).collect(Collectors.toList());
		}

		private void searchOrigin() {
			SplitEvent first = boundary.first();
			SplitEvent last = boundary.last();

			if (first.location != ON || last.location != ON) {
				if (first.location != ON && last.location != ON) {
					if (first.location == IN) {
						origin = first;
						io = O_I;
					} else {
						origin = last;
						io = I_O;
					}
				} else if (last.location == ON) {
					origin = first;
					io = O_I;
				} else {
					origin = last;
					io = I_O;
				}
			} else {
				if (first.sequence.size() >= last.sequence.size()) {
					origin = first;
					io = O_I;
				} else {
					origin = last;
					io = I_O;
				}

				breakOrigin();
			}
		}

		private void breakOrigin() {
			double product = crossProduct(origin);

			if (sequencer.reference.xy == X) {
				if (side == LT)
					product *= io == O_I ? -1 : 1;
				else
					product *= io == O_I ? 1 : -1;
			} else {
				if (side == LT)
					product *= io == O_I ? 1 : -1;
				else
					product *= io == O_I ? -1 : 1;
			}

			NavigableSet<SplitEvent> events = eventToEvents.get(origin);
			events.remove(origin);
			boundary.remove(origin);
			eventToEvents.remove(origin);

			if (product > 0) {
				origin = origin.withLocation(IN);
			} else if (product < 0) {
				origin = origin.withLocation(OUT);
			} else {
				throw new UnsupportedOperationException();
			}

			boundary.add(origin);
			events.add(origin);
			eventToEvents.put(origin, events);
		}

		private void searchStart() {
			start = null;
			boundary.remove(stop);

			if (stop != origin) {

				if (stop.location != ON) {
					switch (io) {
						case I_O:
							start = getLower(boundary, stop);
							break;

						case O_I:
							start = getHigher(boundary, stop);
							break;
					}

					io = io.invert();
				} else {

					NavigableSet<SplitEvent> events = eventToEvents.get(stop);
					SplitEvent next = forward ? getHigher(events, stop) : getLower(events, stop);

					switch (Double.compare(next.position, stop.position)) {
						case -1:
							if (io == I_O) {
								start = stop;
							} else {
								SplitEvent newStop = split(stop, events);
								start = getHigher(boundary, newStop);
								io = io.invert();
							}
							break;

						case 1:
							if (io == O_I) {
								start = stop;
							} else {
								SplitEvent newStop = split(stop, events);
								start = getLower(boundary, newStop);
								io = io.invert();
							}
							break;
					}

				}

				boundary.remove(start);
			} else {
				start = origin;
			}
		}

		private SplitEvent split(SplitEvent stop, NavigableSet<SplitEvent> events) {
			SplitEvent newStop = stop.withLocation(forward ? IN : OUT);
			boundary.add(newStop);
			events.remove(stop);
			events.add(newStop);
			eventToEvents.put(newStop, events);

			return newStop;
		}

		private void searchStop() {
			stop = null;
			NavigableSet<SplitEvent> events = eventToEvents.get(start);

			if (start.location != ON) {
				if (start.location == IN) {
					forward = true;
					stop = getHigher(events, start);
				} else {
					forward = false;
					stop = getLower(events, start);
				}

				io = io.invert();
			} else {
				double product = crossProduct(start);
				if (sequencer.reference.xy == X) {
					if (side == LT)
						product *= io == I_O ? -1 : 1;
					else
						product *= io == I_O ? 1 : -1;
				} else {
					if (side == LT)
						product *= io == I_O ? 1 : -1;
					else
						product *= io == I_O ? -1 : 1;
				}

				if (product > 0) {
					forward = true;
					stop = getHigher(events, start);
				} else if (product < 0) {
					forward = false;
					stop = getLower(events, start);
				} else {
					throw new UnsupportedOperationException();
				}

				if (stop == start) {
					breakRingOnStart();
				}
			}
		}

		private void breakRingOnStart() {
			NavigableSet<SplitEvent> events = eventToEvents.get(start);
			SplitEvent newEvent;

			// remember, start == stop
			boundary.remove(start);
			events.remove(start);

			if (forward) {
				// creating new stop
				newEvent = new SplitEvent(start.index, start.position, OUT, start.getCoordinate(), start.sequence);
				stop = newEvent;
			} else {
				// creating new start
				newEvent = new SplitEvent(start.index, start.position, IN, start.getCoordinate(), start.sequence);
				start = newEvent;
			}

			boundary.add(newEvent);
			events.add(newEvent);
		}

		private class Poly {

			final CoordinateSequence shell;

			final LinkedList<CoordinateSequence> holes = new LinkedList<>();

			Poly(CoordinateSequence shell) {
				this.shell = shell;
			}

			Polygon build() {
				LinearRing shell = factory.createLinearRing(this.shell);
				LinearRing[] holes = this.holes.stream().map(factory::createLinearRing).toArray(LinearRing[]::new);
				return factory.createPolygon(shell, holes);
			}
		}

	}
}
