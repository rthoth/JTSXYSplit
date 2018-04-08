package com.github.rthoth.xysplit;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.TopologyException;


enum XY {

	X(
	  (coordinate, position) -> {
		  if (coordinate.x < position)
			  return Side.LT;
		  else if (coordinate.x > position)
			  return Side.GT;
		  else
			  return Side.EQ;
	  },

	  (coordinates, index, position) -> {
		  double x = coordinates.x(index);

		  if (x < position)
			  return Side.LT;
		  else if (x > position)
			  return Side.GT;
		  else
			  return Side.EQ;
	  },

	  (xa, ya, xb, yb, position) -> {
		  double d = xb - xa;
		  
		  if (d != 0D)
			  return new Coordinate(position, (position - xa) / d * (yb - ya) + ya);
		  else
			  throw new TopologyException(String.format("There is more an only point at x = %f!", position));
	  }),

	Y(
	  (coordinate, position) -> {
		  if (coordinate.y < position)
			  return Side.LT;
		  else if (coordinate.y > position)
			  return Side.GT;
		  else
			  return Side.EQ;
	  },

	  (coordinates, index, position) -> {
		  double y = coordinates.y(index);

		  if (y < position)
			  return Side.LT;
		  else if (y > position)
			  return Side.GT;
		  else
			  return Side.EQ;
	  },
	  
	  (xa, ya, xb, yb, position) -> {
		  double d = yb - ya;
		  
		  if (d != 0D)
			  return new Coordinate((position - ya) / d * (xb - xa) + xa, position);
		  else
			  throw new TopologyException(String.format("There is more an only point at y = %f!", position));
	  });

	interface CoordinateClassifier {
		
		public Side classify(Coordinate coordinate, double position);
	}

	interface SequenceClassifier {

		public Side classify(Coordinates coordinates, int index, double position);
	}

	interface Interpolator {
		
		public Coordinate apply(double xa, double ya, double xb, double yb, double position);
	}

	public final CoordinateClassifier coordinateClassifier;

	public final SequenceClassifier coordinatesClassifier;

	public final Interpolator interpolator;

	XY(CoordinateClassifier coordinateClassifier, SequenceClassifier coordinatesClassifier, Interpolator interpolator) {
		this.coordinateClassifier = coordinateClassifier;
		this.coordinatesClassifier = coordinatesClassifier;
		this.interpolator = interpolator;
	}

	public Side classify(Coordinate coordinate, double position) {
		return coordinateClassifier.classify(coordinate, position);
	}

	public Side classify(Coordinates coordinates, int index, double position) {
		return coordinatesClassifier.classify(coordinates, index, position);
	}

	public Coordinate intersection(double xa, double ya, double xb, double yb, double position) {
		return interpolator.apply(xa, ya, xb, yb, position);
	}
}
