package com.github.rthoth.xysplit;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.TopologyException;

import java.util.function.ToDoubleFunction;

class XYDefinitions {

	public interface Decider {
		Side apply(CoordinateSequence sequence, int index, double position, double offset);
	}

	public interface Interpolator {
		Coordinate apply(double xa, double ya, double xb, double yb, double position);
	}

	public interface OrdinateExtractor {
		double apply(CoordinateSequence sequence, int index);
	}


	public static final Interpolator X_INTERPOLATOR = (xa, ya, xb, yb, position) -> {
		double d = xb - xa;

		if (d != 0D)
			return new Coordinate(position, (position - xa) / d * (yb - ya) + ya);
		else
			throw new TopologyException(String.format("There is more an only point at x = %f!", position));
	};

	public static final Decider X_OFFSET_DECIDER = (sequence, index, position, offset) -> {
		double x = sequence.getX(index);

		if (x != position) {
			return Math.abs(x - position) > offset ? (x < position ? Side.LT : Side.GT) : Side.EQ;
		} else {
			return Side.EQ;
		}
	};

	public static final OrdinateExtractor X_ORDINATE_EXTRACTOR = (sequence, index) -> sequence.getX(index);

	public static final ToDoubleFunction<Coordinate> X_ORDINATE_COORDINATE_EXTRACTOR = coordinate -> coordinate.x;

	public static final Interpolator Y_INTERPOLATOR = (xa, ya, xb, yb, position) -> {
		double d = yb - ya;

		if (d != 0D)
			return new Coordinate((position - ya) / d * (xb - xa) + xa, position);
		else
			throw new TopologyException(String.format("There is more an only point at y = %f!", position));
	};

	public static final Decider Y_OFFSET_DECIDER = (sequence, index, position, offset) -> {
		double y = sequence.getY(index);

		if (y != position) {
			return Math.abs(y - position) > offset ? (y < position ? Side.LT : Side.GT) : Side.EQ;
		} else {
			return Side.EQ;
		}
	};

	public static final OrdinateExtractor Y_ORDINATE_EXTRACTOR = (sequence, index) -> sequence.getY(index);

	public static final ToDoubleFunction<Coordinate> Y_ORDINATE_COORDINATE_EXTRACTOR = coordinate -> coordinate.y;
}
