package com.github.rthoth.xysplit;

import org.locationtech.jts.algorithm.RayCrossingCounter;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.operation.valid.IsValidOp;
import org.locationtech.jts.operation.valid.TopologyValidationError;

import java.util.*;
import java.util.function.Function;

import static com.github.rthoth.xysplit.Side.*;
import static org.locationtech.jts.geom.Location.EXTERIOR;


public class PolygonSplitter implements Function<Polygon, SplitResult> {

	public final Reference reference;
	
	public static final Comparator<SplitEvent> X_COMPARATOR = (a, b) -> {
		final double ay = a.y(), by = b.y();
		if (ay < by)
			return -1;
		else if (ay > by)
			return 1;
		else
			return 0;
	};
	
	public static final Comparator<SplitEvent> Y_COMPARATOR = (a, b) -> {
		double ax = a.x(), bx = b.x();
		if (ax < bx)
			return -1;
		else if (ax > bx)
			return 1;
		else
			return 0;
	};
	
	private final Comparator<SplitEvent> comparator;

	public PolygonSplitter(Reference reference) {
		this.reference = reference;
		comparator = reference.xy == XY.X ? X_COMPARATOR : Y_COMPARATOR;
	}

	public SplitResult apply(Polygon polygon) {

		SplitSequence shell = new SplitSequence.Poly(reference,
				polygon.getExteriorRing().getCoordinateSequence());

		LinkedList<Poly> ltHoles = new LinkedList<>(), gtHoles = new LinkedList<>();

		for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
			SplitSequence sequence = new SplitSequence.Poly(reference,
					polygon.getInteriorRingN(i).getCoordinateSequence());
			ltHoles.add(new Poly(sequence.sequence, sequence.get(LT)));
			gtHoles.add(new Poly(sequence.sequence, sequence.get(GT)));
		}

		Poly polyLT = new Poly(shell.sequence, shell.get(LT));
		Poly polyGT = new Poly(shell.sequence, shell.get(GT));

		Geometry lt = new Builder(LT).apply(polyLT, ltHoles, polygon);
		Geometry gt = new Builder(GT).apply(polyGT, gtHoles, polygon);

//		check(lt);
//		check(gt);

		return new SplitResult(lt, gt, reference);
	}

	private void check(Geometry geometry) {
		IsValidOp op = new IsValidOp(geometry);
		op.setSelfTouchingRingFormingHoleValid(false);
		TopologyValidationError validationError = op.getValidationError();
		if (validationError != null) {
			throw new TopologyException(validationError.getMessage() + ": " + geometry.toText(), validationError.getCoordinate());
		}
	}

	/**
	 */
	protected static class Poly {
		public final CoordinateSequence coordinates;
		public final List<SplitEvent> events;

		public Poly(CoordinateSequence coordinates, List<SplitEvent> events) {
			this.coordinates = coordinates;
			this.events = events;
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
				throw new XYException.Split(String.format("Impossible to split on %s at %s!", side, reference), cause);
			}
		}

		private Geometry withEvents(Poly shell, List<Poly> holes, GeometryFactory factory) {
			TreeSet<SplitEvent> boundary = new TreeSet<>(comparator);
			LinkedList<SplitEvent> events = new LinkedList<>(shell.events);
			DIMap<SplitEvent> pair = new DIMap.Hash<>();
			HashSet<SplitEvent> seen = new HashSet<>();

			if (events.peek().location == Location.OUT) {
				events.addLast(events.poll());
			}

			for (int i = 0, l = events.size(); i < l; i += 2) {
				final SplitEvent in = events.get(i);
				final SplitEvent out = events.get(i + 1);
				boundary.add(in);
				boundary.add(out);
				pair.add(in, out);
			}
			
			TreeSet<SplitEvent> inputs;
			if (boundary.first().location == Location.IN)
				inputs = new TreeSet<>(comparator);
			else
				inputs = new TreeSet<>(comparator.reversed());
			
			for (SplitEvent event : boundary) {
				if (event.location == Location.IN)
					inputs.add(event);
			}

			List<Poly> wholeInside = new LinkedList<>();

			for (Poly hole : holes) {
				if (!hole.events.isEmpty()) {
					final LinkedList<SplitEvent> temporary = new LinkedList<>(hole.events);
					if (temporary.peek().location == Location.OUT) {
						temporary.addLast(temporary.poll());
					}
					for (int i = 0; i < temporary.size(); i += 2) {
						final SplitEvent in = temporary.get(i);
						final SplitEvent out = temporary.get(i + 1);
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
			while (!inputs.isEmpty()) {
				final CoordinateSequenceBuilder builder = new CoordinateSequenceBuilder();
				final SplitEvent origin = inputs.pollFirst();
				SplitEvent start = origin;

				do {
					final SplitEvent stop = pair.get(start);
					seen.add(start);
					
					if (start.location == Location.IN) {
						if (start.coordinate != null)
							builder.add(start.coordinate);

						int stopIndex = stop.index != 0 ? stop.index - 1 : stop.coordinates.size() - 2;
						builder.addRing(start.coordinates, start.index, stopIndex, true);

						builder.add(stop.getCoordinate());
					} else {
						builder.add(start.getCoordinate());

						int startIndex = start.index != 0 ? start.index - 1 : stop.coordinates.size() - 2;
						builder.addRing(start.coordinates, startIndex, stop.index, false);

						if (stop.coordinate != null)
							builder.add(stop.coordinate);
					}
					
					switch (comparator.compare(origin, stop)) {
					case -1:
						start = boundary.lower(stop);
						break;
					case 1:
						start = boundary.higher(stop);
						break;
					default:
						throw new XYException.Split("Invalid!");
					}
					
					if (start == null)
						throw new XYException.Split("There is not a input near " + stop + "!");
					
					if (start == origin) {
						builder.add(origin.getCoordinate());
						result.add(factory.createLinearRing(builder.build()));
					} else if (!seen.contains(start)) {
						if (start.location == Location.IN)
							inputs.remove(start);
					} else {
						throw new XYException.Split(start + " has already been visited!");
					}
					
				} while (start != origin);
			}

			LinkedList<Polygon> prototypes = new LinkedList<>();
			for (LinearRing ring : result) {
				if (!wholeInside.isEmpty()) {
					LinkedList<LinearRing> _holes = new LinkedList<>();

					for (Poly hole : wholeInside) {
						if (RayCrossingCounter.locatePointInRing(hole.coordinates.getCoordinate(0), hole.coordinates) != EXTERIOR) {
							_holes.add(factory.createLinearRing(hole.coordinates));
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
			if (shell.coordinates.size() != 0) {
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
