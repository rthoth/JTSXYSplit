package com.github.rthoth.xysplit;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;

public enum XY {

	X(XYDefinitions.X_DECIDER, XYDefinitions.X_INTERPOLATOR, XYDefinitions.X_OFFSET_DECIDER),
	Y(XYDefinitions.Y_DECIDER, XYDefinitions.Y_INTERPOLATOR, XYDefinitions.Y_OFFSET_DECIDER);

	private final XYDefinitions.Decider decider;
	private final XYDefinitions.Interpolator interpolator;
	private final XYDefinitions.OffsetDecider offsetDecider;

	XY(XYDefinitions.Decider decider, XYDefinitions.Interpolator interpolator, XYDefinitions.OffsetDecider offsetDecider) {
		this.decider = decider;
		this.interpolator = interpolator;
		this.offsetDecider = offsetDecider;
	}

	public Side classify(CoordinateSequence coordinates, int index, double position) {
		return decider.apply(coordinates, index, position);
	}

	public Coordinate intersection(double xa, double ya, double xb, double yb, double position) {
		return interpolator.apply(xa, ya, xb, yb, position);
	}

	public Side classify(CoordinateSequence sequence, int index, double position, double offset) {
		return offsetDecider.apply(sequence, index, position, offset);
	}

	public XY invert() {
		return this == X ? Y : X;
	}
}
