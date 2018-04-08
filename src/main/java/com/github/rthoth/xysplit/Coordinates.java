package com.github.rthoth.xysplit;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Coordinate;

public class Coordinates {

	private final CoordinateSequence underlying;

	public Coordinates(CoordinateSequence underlying) {
		this.underlying = underlying;
	}

	public CoordinateSequence getCoordinates() {
		return underlying;
	}

	public Coordinate get(int index) {
		return underlying.getCoordinate(index);
	}
		
	public int size() {
		return underlying.size();
	}
	
	public boolean nonEmpty() {
		return underlying.size() != 0;
	}

	public double x(int index) {
		return underlying.getX(index);
	}

	public double y(int index) {
		return underlying.getY(index);
	}
}
