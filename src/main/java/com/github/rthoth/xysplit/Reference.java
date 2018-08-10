package com.github.rthoth.xysplit;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;

import static com.github.rthoth.xysplit.XY.X;


public class Reference {

	public final XY xy;

	public final double position;

	public static Reference x(double position) {
		return new Reference(X, position);
	}

	public static Reference y(double position) {
		return new Reference(XY.Y, position);
	}

	public Reference(XY xy, double position) {
		this.xy = xy;
		this.position = position;
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

	public Reference offset(double offset) {
		return xy == X ? x(position + offset) : y(position + offset);
	}

	public Coordinate intersection(CoordinateSequence sequence, int i1, int i2) {
		return intersection(
						sequence.getX(i1), sequence.getY(i1),
						sequence.getX(i2), sequence.getY(i2)
		);
	}
}
