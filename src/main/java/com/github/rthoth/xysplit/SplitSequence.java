package com.github.rthoth.xysplit;

import static com.github.rthoth.xysplit.Location.*;
import static com.github.rthoth.xysplit.Side.*;

import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.locationtech.jts.awt.PointShapeFactory;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.TopologyException;

public abstract class SplitSequence {

	protected final CoordinateSequence sequence;
	protected final Reference reference;

	protected final List<SplitEvent> gtEvents;
	protected final List<SplitEvent> ltEvents;

	private final XYDefinitions.OrdinateExtractor positionExtractor;

	public SplitSequence(Reference reference, CoordinateSequence sequence) {
		this.sequence = sequence;
		this.reference = reference;
		this.positionExtractor = reference.xy == XY.X ? XYDefinitions.Y_ORDINATE_EXTRACTOR : XYDefinitions.X_ORDINATE_EXTRACTOR;

		if (sequence.size() > 1) {
			Side last = reference.classify(sequence, 0);
			final LinkedList<SplitEvent> ltEvents = new LinkedList<>();
			final LinkedList<SplitEvent> gtEvents = new LinkedList<>();

			for (int i = 1, l = sequence.size(); i < l; i++) {
				final Side current = reference.classify(sequence, i);

				if (current != last) {
					register(i, ltEvents, current.location(LT), last.location(LT));
					register(i, gtEvents, current.location(GT), last.location(GT));
					last = current;
				}
			}

			cleanUp(ltEvents);
			cleanUp(gtEvents);

			this.ltEvents = Collections.unmodifiableList(ltEvents);
			this.gtEvents = Collections.unmodifiableList(gtEvents);
		} else if (sequence.size() == 1) {

			Side side = reference.classify(sequence, 0);
			double position = positionExtractor.get(sequence, 0);

			ltEvents = Collections.singletonList(new SplitEvent(0, position, side.location(LT), null, sequence));
			gtEvents = Collections.singletonList(new SplitEvent(0, position, side.location(GT), null, sequence));
		} else {
			ltEvents = gtEvents = Collections.emptyList();
		}
	}

	protected abstract void cleanUp(Deque<SplitEvent> events);

	public List<SplitEvent> get(Side side) {
		switch (side) {
			case LT:
				return ltEvents;

			case GT:
				return gtEvents;

			default:
				throw new IllegalArgumentException("Invalid ON side!");
		}
	}

	protected double getPosition(int index) {
		return positionExtractor.get(sequence, index);
	}

	protected double getPosition(Coordinate coordinate) {
		return reference.xy == XY.X ? coordinate.y : coordinate.x;
	}

	protected Coordinate intersection(int a, int b) {
		double xa = sequence.getX(a), ya = sequence.getY(a);
		double xb = sequence.getX(b), yb = sequence.getY(b);

		return reference.intersection(xa, ya, xb, yb);
	}

	protected abstract void register(int index, Deque<SplitEvent> events, Location current, Location last);

	public boolean isEmpty() {
		return ltEvents.isEmpty() || gtEvents.isEmpty();
	}

	public static class Line extends SplitSequence {

		public Line(Reference reference, CoordinateSequence coordinates) {
			super(reference, coordinates);
		}

		@Override
		protected void cleanUp(Deque<SplitEvent> events) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected void register(int index, Deque<SplitEvent> events, Location current, Location last) {
			throw new UnsupportedOperationException();
		}
	}

	public static class Poly extends SplitSequence {

		public Poly(Reference reference, CoordinateSequence sequence) {
			super(reference, sequence);
		}

		protected void register(int index, Deque<SplitEvent> events, Location current, Location last) {
			SplitEvent event = null;

			if (last != ON && current != ON) {
				Coordinate coordinate = intersection(index, index - 1);
				event = new SplitEvent(index, getPosition(coordinate), current, coordinate, sequence);
			} else if (current == ON) {
				event = new SplitEvent(index, getPosition(index), last.invert(), null, sequence);
			} else {
				SplitEvent lastEvent = events.peekLast();

				if (lastEvent != null) {
					if (lastEvent.location == current) {
						if (current == IN) {
							events.pollLast();
							event = new SplitEvent(index, getPosition(index - 1), current, sequence.getCoordinate(index - 1), sequence);
						}
					} else if (lastEvent.index == index - 1) {
						events.pollLast();
						if (current == IN) {
							event = new SplitEvent(index - 1, getPosition(index - 1), ON, null, sequence);
						}
					} else if (current == OUT) {
						events.pollLast();
					} else {
						event = new SplitEvent(index - 1, getPosition(index - 1), current, null, sequence);
					}
				} else {
					event = new SplitEvent(index - 1, getPosition(index - 1), current, null, sequence);
				}
			}

			if (event != null)
				events.addLast(event);
		}

		@Override
		protected void cleanUp(Deque<SplitEvent> events) {
			if (!events.isEmpty()) {
				SplitEvent first = events.peekFirst();
				SplitEvent last = events.peekLast();

				if (first.index == 0) {
					if (last.index == sequence.size() - 1) {
						// FIRST == LAST
						if (first.location == last.location) {
							if (first.location == IN)
								events.pollLast();
							else
								events.pollFirst();
						} else {
							events.pollFirst();
							events.pollLast();

							if (first.location == IN)
								events.addFirst(new SplitEvent(0, first.position, ON, first.coordinate, sequence));
						}
					}
				} else if (first.location == last.location) {
					if (first.location == OUT)
						events.pollFirst();
					else
						events.pollLast();
				}

//				if (first.index == 0) {
//					if (last.index == sequence.size() - 1) {
//						events.pollLast();
//
//						if (last.location != first.location)
//							events.pollFirst();
//					} else if (last.location == first.location) {
//						events.pollFirst();
//					}
//				} else if (first.location == last.location) {
//					events.pollFirst();
//				}
			}
		}
	}
}
