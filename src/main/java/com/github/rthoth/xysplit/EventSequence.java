package com.github.rthoth.xysplit;

import static com.github.rthoth.xysplit.Location.*;
import static com.github.rthoth.xysplit.Side.GT;
import static com.github.rthoth.xysplit.Side.LT;

import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;

/**
 * 
 */
public abstract class EventSequence {

	public final Coordinates coordinates;
	public final Reference reference;

	public final List<Event> gtEvents;
	public final List<Event> ltEvents;

	public EventSequence(Reference reference, Coordinates coordinates) {
		this.coordinates = coordinates;
		this.reference = reference;

		if (coordinates.size() > 1) {
			Side last = reference.classify(coordinates, 0);
			final LinkedList<Event> ltEvents = new LinkedList<>();
			final LinkedList<Event> gtEvents = new LinkedList<>();

			for (int i = 1, l = coordinates.size(); i < l; i++) {
				final Side current = reference.classify(coordinates, i);

				if (current != last) {
					register(i, ltEvents, current.location(LT), last.location(LT));
					register(i, gtEvents, current.location(GT), last.location(GT));
				}

				last = current;
			}
			
			finished(ltEvents);
			finished(gtEvents);
			
			this.ltEvents = Collections.unmodifiableList(ltEvents);
			this.gtEvents = Collections.unmodifiableList(gtEvents);
		} else if (coordinates.size() == 1) {
			Side side = reference.classify(coordinates, 0);

			ltEvents = Collections.singletonList(new Event(0, side.location(LT), null, coordinates));
			gtEvents = Collections.singletonList(new Event(0, side.location(GT), null, coordinates));
		} else {
			ltEvents = gtEvents = Collections.emptyList();
		}
	}
	
	protected abstract void finished(Deque<Event> events);

	public List<Event> get(Side side) {
		switch (side) {
		case LT:
			return ltEvents;

		case GT:
			return gtEvents;

		default:
			throw new IllegalArgumentException("Invalid ON side!");
		}
	}

	protected Coordinate intersection(int a, int b) {
		double xa = coordinates.x(a), ya = coordinates.y(a);
		double xb = coordinates.x(b), yb = coordinates.y(b);

		return reference.intersection(xa, ya, xb, yb);
	}
	
	protected abstract void register(int index, Deque<Event> events, Location current, Location last);
	
	public static class LineString extends EventSequence {
		
		public LineString(Reference reference, Coordinates coordinates) {
			super(reference, coordinates);
		}
		
		protected void finished(Deque<Event> events) {
			
		}
		
		@Override
		protected void register(int index, Deque<Event> events, Location current, Location last) {
			// TODO Auto-generated method stub
		}
	}
	
	public static class Polygon extends EventSequence {
		
		public Polygon(Reference reference, Coordinates coordinates) {
			super(reference, coordinates);
		}
		
		protected void finished(Deque<Event> events) {
			if (!events.isEmpty()) {
				final Event first = events.peekFirst(), last = events.peekLast();
				
				if (first.getCoordinate().equals(last.getCoordinate())) {
					events.pollLast();
					if (events.size() % 2 == 1) {
						events.pollFirst();
					}
				}
			}
		}
		
		protected void register(int index, Deque<Event> events, Location current, Location last) {
			Event event = null;
			
			if (last != ON && current != ON) {
				event = new Event(index, current, intersection(index, index - 1), coordinates);
			} else if (current == ON) {
				event = new Event(index, last.invert(), null, coordinates);
			} else {
				Event lastEvent = events.peekLast();
				if (lastEvent != null) {
					if (lastEvent.location != current) {
						if (current == OUT || lastEvent.index == index - 1) {
							events.pollLast();
						} else {
							event = new Event(index - 1, current, null, coordinates);
						}
					} else {
						if (current == IN) {
							events.pollLast();
							event = new Event(index, current, coordinates.get(index - 1), coordinates);
						}
					}
				} else {
					event = new Event(index - 1, current, null, coordinates);
				}
			}
			
			if (event != null) {
				events.addLast(event);
			}
		}
	}	
}
