package com.github.rthoth.xysplit;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;

public enum XY {

	X(XYDefinitions.X_INTERPOLATOR, XYDefinitions.X_OFFSET_DECIDER),
	Y(XYDefinitions.Y_INTERPOLATOR, XYDefinitions.Y_OFFSET_DECIDER);

	private final XYDefinitions.Interpolator interpolator;
	private final XYDefinitions.Decider decider;

	XY(XYDefinitions.Interpolator interpolator, XYDefinitions.Decider decider) {
		this.interpolator = interpolator;
		this.decider = decider;
	}

	public Coordinate intersection(double xa, double ya, double xb, double yb, double position) {
		return interpolator.apply(xa, ya, xb, yb, position);
	}

	public Side classify(CoordinateSequence sequence, int index, double position, double offset) {
		return decider.apply(sequence, index, position, offset);
	}

	public XY invert() {
		return this == X ? Y : X;
	}
}
