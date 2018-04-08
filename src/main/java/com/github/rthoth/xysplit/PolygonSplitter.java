package com.github.rthoth.xysplit;

import static com.github.rthoth.xysplit.Side.EQ;
import static com.github.rthoth.xysplit.Side.GT;
import static com.github.rthoth.xysplit.Side.LT;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Function;
import org.locationtech.jts.algorithm.PointLocation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;



class PolygonSplitter implements Function<Polygon, R> {

	public final Reference reference;
	
	public static final Comparator<Event> X_COMPARATOR = (a, b) -> {
		final double ay = a.y(), by = b.y();
		if (ay < by)
			return -1;
		else if (ay > by)
			return 1;
		else
			return 0;
	};
	
	public static final Comparator<Event> Y_COMPARATOR = (a, b) -> {
		double ax = a.x(), bx = b.x();
		if (ax < bx)
			return -1;
		else if (ax > bx)
			return 1;
		else
			return 0;
	};
	
	private final Comparator<Event> comparator;

	public PolygonSplitter(Reference reference) {
		this.reference = reference;
		comparator = reference.xy == XY.X ? X_COMPARATOR : Y_COMPARATOR;
	}

	public R apply(Polygon polygon) {

		Builder ltBuilder = new Builder(LT);
		Builder gtBuilder = new Builder(GT);

		EventSequence shell = new EventSequence.Polygon(reference,
				new Coordinates(polygon.getExteriorRing().getCoordinateSequence()));
		LinkedList<Poly> ltHoles = new LinkedList<>(), gtHoles = new LinkedList<>();

		for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
			EventSequence sequence = new EventSequence.Polygon(reference,
					new Coordinates(polygon.getInteriorRingN(i).getCoordinateSequence()));
			ltHoles.add(new Poly(sequence.coordinates, sequence.get(LT)));
			gtHoles.add(new Poly(sequence.coordinates, sequence.get(GT)));
		}

		Poly polyLT = new Poly(shell.coordinates, shell.get(LT));
		Poly polyGT = new Poly(shell.coordinates, shell.get(GT));

		Geometry lt = ltBuilder.apply(polyLT, ltHoles, polygon);
		Geometry gt = gtBuilder.apply(polyGT, gtHoles, polygon);

		return R.from(lt, gt, reference);
	}

	/**
	 */
	protected static class Poly {
		public final Coordinates coordinates;
		public final List<Event> events;

		public Poly(Coordinates coordinates, List<Event> events) {
			this.coordinates = coordinates;
			this.events = events;
		}
	}
	
	/**
	 */
	protected static class Pair {
		
		private final HashMap<Event, Event> inToOut = new HashMap<>();
		private final HashMap<Event, Event> outToIn = new HashMap<>();
		
		public Pair add(Event in, Event out) {
			inToOut.put(in, out);
			outToIn.put(out, in);
			return this;
		}
		
		public Event get(Event key) {
			final Event value = inToOut.get(key);
			return (value != null) ? value : outToIn.get(key);
		}
	}

	/**
	 */
	protected class Builder {

		private final Side side;

		public Builder(Side side) {
			this.side = side;
		}

		public Geometry apply(Poly shell, List<Poly> holes, Polygon original) {
			try {
				return !shell.events.isEmpty() ? withEvents(shell, holes, original.getFactory())
						: withoutEvents(shell, holes, original);
			} catch (Exception cause) {
				throw new XYSplitterException(String.format("Impossible to split on %s at %s!", side, reference), cause);
			}
		}

		private Geometry withEvents(Poly shell, List<Poly> holes, GeometryFactory factory) {
			TreeSet<Event> boundary = new TreeSet<>(comparator);
			LinkedList<Event> events = new LinkedList<>(shell.events);
			Pair pair = new Pair();
			HashSet<Event> seen = new HashSet<>();

			if (events.peek().location == Location.OUT) {
				events.addLast(events.poll());
			}

			for (int i = 0; i < events.size(); i++) {
				final Event in = events.get(i);
				final Event out = events.remove(i + 1);
				boundary.add(in);
				boundary.add(out);
				pair.add(in, out);
			}

			List<Poly> wholeInside = new LinkedList<>();

			for (Poly hole : holes) {
				if (!hole.events.isEmpty()) {
					final LinkedList<Event> temporary = new LinkedList<>(hole.events);
					if (temporary.peek().location == Location.OUT) {
						temporary.addLast(temporary.poll());
					}
					for (int i = 0; i < temporary.size(); i += 2) {
						final Event in = temporary.get(i);
						final Event out = temporary.get(i + 1);
						boundary.add(in);
						boundary.add(out);
						pair.add(in, out);
					}
				} else {
					loop: for (int i = 0; i < hole.coordinates.size(); i++) {
						final Side classification = reference.classify(hole.coordinates, i);
						if (classification == side) {
							wholeInside.add(hole);
							break loop;
						} else if (classification != EQ) {
							break loop;
						}
					}
				}
			}

			LinkedList<LinearRing> result = new LinkedList<>();
			while (!events.isEmpty()) {
				final ArrayList<Coordinate> buffer = new ArrayList<>();
				final Event origin = events.poll();
				Event start = origin;

				do {
					final Event stop = pair.get(start);
					seen.add(start);
					
					if (start.location == Location.IN) {
						
						if (start.coordinate != null)
							buffer.add(start.coordinate);

						if (start.index <= stop.index) {
							for (int i = start.index; i < stop.index; i++) {
								buffer.add(start.coordinates.get(i));
							}
						} else {
							for (int i = start.index, l = start.coordinates.size() - 1; i < l; i++) {
								buffer.add(start.coordinates.get(i));
							}
							for (int i = 0; i < stop.index; i++) {
								buffer.add(stop.coordinates.get(i));
							}
						}
						
						buffer.add(stop.getCoordinate());
					} else {
						
						buffer.add(start.getCoordinate());
						
						if (start.index >= stop.index) {
							for (int i = start.index - 1; i >= stop.index; i--) {
								buffer.add(start.coordinates.get(i));
							}
						} else {
							for (int i = start.index - 1; i > 0; i--) {
								buffer.add(start.coordinates.get(i));
							}
							for (int i = start.coordinates.size() - 1; i >= stop.index; i--) {
								buffer.add(start.coordinates.get(i));
							}
						}
						
						if (stop.coordinate != null)
							buffer.add(stop.coordinate);
					}
					
					switch (comparator.compare(origin, stop)) {
					case -1:
						start = boundary.lower(stop);
						break;
					case 1:
						start = boundary.higher(stop);
						break;
					default:
						throw new XYSplitterException("Invalid!");
					}
					
					if (start == null)
						throw new XYSplitterException("There is not a input near " + stop + "!");
					
					if (start == origin) {
						buffer.add(origin.getCoordinate());
						result.add(factory.createLinearRing(buffer.toArray(new Coordinate[buffer.size()])));
					} else if (seen.contains(start)) {
						throw new XYSplitterException(start + " has already been visited!");
					}
					
				} while (start != origin);
			}

			LinkedList<Polygon> prototypes = new LinkedList<>();
			for (LinearRing ring : result) {
				if (!wholeInside.isEmpty()) {
					LinkedList<LinearRing> _holes = new LinkedList<>();
					Coordinate[] coordinates = ring.getCoordinates();
					for (Poly hole : wholeInside) {
						if (PointLocation.isInRing(hole.coordinates.get(0), coordinates)) {
							_holes.add(factory.createLinearRing(hole.coordinates.getCoordinates()));
						}
					}
					prototypes.add(factory.createPolygon(ring, _holes.toArray(new LinearRing[_holes.size()])));
				} else {
					prototypes.add(factory.createPolygon(ring));
				}
			}

			return (prototypes.size() != 1)
					? factory.createMultiPolygon(prototypes.toArray(new Polygon[prototypes.size()]))
					: prototypes.get(0);
		}

		private Geometry withoutEvents(Poly shell, List<Poly> holes, Polygon original) {
			if (shell.coordinates.nonEmpty()) {
				Location location = reference.classify(shell.coordinates, 0).location(side);
				for (int i = 1; location != Location.ON && i < shell.coordinates.size(); i++) {
					location = reference.classify(shell.coordinates, i).location(side);
				}

				if (location == Location.IN) {
					return original;
				}
			}

			return original.getFactory().createPolygon();
		}
	}
}
