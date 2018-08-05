package com.github.rthoth.xysplit;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;


public class Reference {

	public final XY xy;

	public final double position;

	public static Reference x(double position) {
		return new Reference(XY.X, position);
	}

	public static Reference y(double position) {
		return new Reference(XY.Y, position);
	}

	public Reference(XY xy, double position) {
		this.xy = xy;
		this.position = position;
	}

	public Side classify(CoordinateSequence sequence, int index) {
		return xy.classify(sequence, index, position, 1e-9);
	}

	public Side classify(CoordinateSequence sequence, int index, double offset) {
		return xy.classify(sequence, index, position, offset);
	}

	public Coordinate intersection(double xa, double ya, double xb, double yb) {
		return xy.intersection(xa, ya, xb, yb, position);
	}

	public String toString() {
		return xy + "(" + position + ")";
	}
}
