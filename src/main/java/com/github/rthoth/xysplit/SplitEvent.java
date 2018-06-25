package com.github.rthoth.xysplit;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;

public class SplitEvent {

	public final Coordinate coordinate;
	public final CoordinateSequence coordinates;
	public final int index;
	public final Location location;
	
	public SplitEvent(int index, Location location, Coordinate coordinate) {
		this(index, location, coordinate, null);
	}

	public SplitEvent(int index, Location location, Coordinate coordinate, CoordinateSequence coordinates) {
		this.index = index;
		this.location = location;
		this.coordinate = coordinate;
		this.coordinates = coordinates;
	}

	public boolean equals(Object obj) {
		if (obj instanceof SplitEvent) {
			SplitEvent other = (SplitEvent) obj;

			return index == other.index
				&& location == other.location
				&& (coordinate == other.coordinate || (coordinate != null && coordinate.equals(other.coordinate)));
		} else {
			return false;
		}
	}

	public int hashCode() {
		return index;
	}

	public Coordinate getCoordinate() {
		return coordinate != null ? coordinate : coordinates.getCoordinate(index);
	}

	public String toString() {
		return String.format("Event(%d, %s, %s)", index, location, coordinate);
	}

	public double x() {
		return coordinate != null ? coordinate.x : coordinates.getX(index);
	}

	public double y() {
		return coordinate != null ? coordinate.y : coordinates.getY(index);
	}
}
