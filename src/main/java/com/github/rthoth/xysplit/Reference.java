package com.github.rthoth.xysplit;

import org.locationtech.jts.geom.Coordinate;


class Reference {

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

	public Side classify(Coordinate coordinate) {
		return xy.classify(coordinate, position);
	}

	public Side classify(Coordinates coordinates, int index) {
		return xy.classify(coordinates, index, position);
	}

	public Coordinate intersection(double xa, double ya, double xb, double yb) {
		return xy.intersection(xa, ya, xb, yb, position);
	}
	
	public String toString() {
		return xy + "(" + position + ")";
	}
}
