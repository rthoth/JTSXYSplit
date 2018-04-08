package com.github.rthoth.xysplit;

import org.locationtech.jts.geom.Coordinate;

public class Event {

	public final Coordinate coordinate;

	public final Coordinates coordinates;

	public final int index;

	public final Location location;
	
	public Event(int index, Location location, Coordinate coordinate) {
		this(index, location, coordinate, null);
	}

	public Event(int index, Location location, Coordinate coordinate, Coordinates coordinates) {
		this.coordinate = coordinate;
		this.index = index;
		this.location = location;
		this.coordinates = coordinates;
	}

	public boolean equals(Object other) {
		if (other instanceof Event) {
			Event e = (Event) other;

			return index == e.index
				&& location == e.location
				&& (coordinate == e.coordinate || (coordinate != null && coordinate.equals(e.coordinate)));
		} else {
			return false;
		}
	}

	public int hashCode() {
		return index;
	}

	public Coordinate getCoordinate() {
		return coordinate != null ? coordinate : coordinates.get(index);
	}

	public String toString() {
		return String.format("Event(%d, %s, %s)", index, location, coordinate);
	}

	public double x() {
		return coordinate != null ? coordinate.x : coordinates.x(index);
	}

	public double y() {
		return coordinate != null ? coordinate.y : coordinates.y(index);
	}
}
