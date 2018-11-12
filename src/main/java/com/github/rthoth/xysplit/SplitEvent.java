package com.github.rthoth.xysplit;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;

import java.util.Comparator;

class SplitEvent implements Comparable<SplitEvent> {

	public static final Comparator<SplitEvent> POSITION_COMPARATOR = (a, b) -> Double.compare(a.position, b.position);

	public static final Comparator<SplitEvent> INDEX_COMPARATOR = (a, b) -> a.index - b.index;

	public final Coordinate coordinate;
	public final int index;
	public final InOnOut inOnOut;
	public final double position;
	public final CoordinateSequence sequence;

	public SplitEvent(int index, double position, InOnOut inOnOut, Coordinate coordinate, CoordinateSequence sequence) {
		this.index = index;
		this.inOnOut = inOnOut;
		this.coordinate = coordinate;
		this.sequence = sequence;
		this.position = position;
	}

	@Override
	public int compareTo(SplitEvent other) {
		return Double.compare(position, other.position);
	}

	public boolean equals(Object obj) {
		if (obj instanceof SplitEvent) {
			SplitEvent other = (SplitEvent) obj;

			return index == other.index
							&& inOnOut == other.inOnOut
							&& (coordinate == other.coordinate || (coordinate != null && coordinate.equals(other.coordinate)));
		} else {
			return false;
		}
	}

	public int hashCode() {
		return index;
	}

	public Coordinate getCoordinate() {
		return coordinate != null ? coordinate : sequence.getCoordinate(index);
	}

	public String toString() {
		return String.format("Event(i=%d, p=%f, l=%s, c=%s)", index, inOnOut, inOnOut, coordinate);
	}

	public SplitEvent withLocation(InOnOut inOnOut) {
		return new SplitEvent(index, this.position, inOnOut, coordinate, sequence);
	}
}
