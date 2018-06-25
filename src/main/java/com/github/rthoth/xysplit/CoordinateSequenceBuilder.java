package com.github.rthoth.xysplit;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

import java.util.LinkedList;
import java.util.stream.Collectors;

public class CoordinateSequenceBuilder {

	public class Builder {

		private final boolean closed;
		private final CoordinateSequence sequence;
		private final boolean asc;
		private final int start;
		private final int stop;

		public Builder(CoordinateSequence sequence, int start, int stop, boolean asc, boolean closed) {
			this.sequence = sequence;
			this.start = start;
			this.stop = stop;
			this.asc = asc;
			this.closed = closed;
		}

		public CoordinateSequence build() {
			int limit = closed ? sequence.size() - 1 : sequence.size();
			return asc ? new CoordinateSequenceWindow.Asc(sequence, start, stop, limit) : new CoordinateSequenceWindow.Desc(sequence, start, stop, limit);
		}
	}

	private final LinkedList<Builder> builders = new LinkedList<>();

	/**
	 * It adds a opened sequence line string.
	 * @param coordinates
	 * @return
	 */
	public CoordinateSequenceBuilder add(Coordinate coordinates) {
		return add(false, coordinates);
	}

	public CoordinateSequenceBuilder add(boolean closed, Coordinate... coordinates) {
		builders.add(new Builder(new CoordinateArraySequence(coordinates), 0, coordinates.length - 1, true, closed));
		return this;
	}

	public boolean isEmpty() {
		return false;
	}

	public CoordinateSequence build() {
		return new SegmentedCoordinateSequence(builders.stream().map(Builder::build).collect(Collectors.toList()));
	}

	public CoordinateSequenceBuilder addRing(CoordinateSequence sequence, int start, int stop, boolean asc) {
		builders.add(new Builder(sequence, start, stop, asc, true));
		return this;
	}
}







































































































