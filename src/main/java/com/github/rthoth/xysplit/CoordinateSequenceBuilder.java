package com.github.rthoth.xysplit;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

import java.util.LinkedList;
import java.util.stream.Collectors;

public class CoordinateSequenceBuilder {

	public CoordinateSequence closeAndBuild() {
		Builder first = builders.peekFirst();
		return add(first.sequence.getCoordinate(first.start)).build();
	}

	public static class Builder {

		protected final boolean closed;

		protected final CoordinateSequence sequence;
		protected final boolean forward;
		protected final int start;
		protected final int stop;

		public Builder(CoordinateSequence sequence, int start, int stop, boolean forward, boolean closed) {
			this.sequence = sequence;
			this.start = start;
			this.stop = stop;
			this.forward = forward;
			this.closed = closed;
		}

		public CoordinateSequence build() {
			int limit = closed ? sequence.size() - 1 : sequence.size();
			return forward ? new CoordinateSequenceWindow.ForwardWindow(sequence, start, stop, limit) : new CoordinateSequenceWindow.BackwardWindow(sequence, start, stop, limit);
		}


		public int size() {
			if (forward) {
				if (start <= stop) {
					return stop - start + 1;
				} else {
					return (closed ? sequence.size() - 1 : sequence.size()) - start + stop + 1;
				}
			} else {
				if (start >= stop) {
					return start - stop + 1;
				} else {
					return (closed ? sequence.size() - 1 : sequence.size()) - stop + start + 1;
				}
			}
		}
	}

	private final LinkedList<Builder> builders = new LinkedList<>();
	private int size = 0;

	private CoordinateSequenceBuilder add(Builder builder) {
		size += builder.size();
		builders.addLast(builder);
		return this;
	}

	/**
	 * It adds a opened original line string.
	 * @param coordinates
	 * @return
	 */
	public CoordinateSequenceBuilder add(Coordinate coordinates) {
		return add(false, coordinates);
	}

	public CoordinateSequenceBuilder add(boolean closed, Coordinate... coordinates) {
		return add(new Builder(new CoordinateArraySequence(coordinates), 0, coordinates.length - 1, true, closed));
	}

	public boolean isEmpty() {
		return builders.isEmpty();
	}

	public CoordinateSequence build() {
		return new SegmentedCoordinateSequence(builders.stream().map(Builder::build).collect(Collectors.toList()));
	}

	public CoordinateSequenceBuilder addRing(CoordinateSequence sequence, int start, int stop, boolean forward) {
		return add(new Builder(sequence, start, stop, forward, true));
	}

	public int size() {
		return size;
	}
}







































































































