package com.github.rthoth.xysplit;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Envelope;

public abstract class CoordinateSequenceWindow implements CoordinateSequence {

	protected final CoordinateSequence underlying;
	protected final int start;
	protected final int stop;
	protected final int limit;
	protected final int size;

	public CoordinateSequenceWindow(CoordinateSequence underlying, int start, int stop, int limit) {
		this.underlying = underlying;
		this.start = start;
		this.stop = stop;
		this.limit = limit;
		size = computeSize();
	}

	protected abstract int computeSize();

	public static class Asc extends CoordinateSequenceWindow {

		public Asc(CoordinateSequence underlying, int start, int stop, int limit) {
			super(underlying, start, stop, limit);

		}

		@Override
		protected int computeSize() {
			return start <= stop ? stop - start + 1 : limit - start + stop + 1;
		}

		@Override
		protected int map(int index) {
			return (start + index) % limit;
		}

	}

	public static class Desc extends CoordinateSequenceWindow {

		public Desc(CoordinateSequence underlying, int start, int stop, int limit) {
			super(underlying, start, stop, limit);
		}

		@Override
		protected int computeSize() {
			return start >= stop ? start - stop + 1 : limit - stop + start + 1;
		}

		@Override
		protected int map(int index) {
			index = (start - index) % limit;
			return index >= 0 ? index : limit + index;
		}

	}

	protected abstract int map(int index);

	@Override
	public int getDimension() {
		return underlying.getDimension();
	}

	@Override
	public Coordinate getCoordinate(int index) {
		return underlying.getCoordinate(map(index));
	}

	@Override
	public Coordinate getCoordinateCopy(int index) {
		return underlying.getCoordinateCopy(map(index));
	}

	@Override
	public void getCoordinate(int index, Coordinate coordinate) {
		underlying.getCoordinate(map(index), coordinate);
	}

	@Override
	public double getX(int index) {
		return underlying.getX(map(index));
	}

	@Override
	public double getY(int index) {
		return underlying.getY(map(index));
	}

	@Override
	public double getOrdinate(int index, int ordinateIndex) {
		return underlying.getOrdinate(map(index), ordinateIndex);
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public void setOrdinate(int index, int ordinateIndex, double value) {
		throw new UnsupportedOperationException();
	}

	@Override
	@SuppressWarnings("deprecation")
	public Coordinate[] toCoordinateArray() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Envelope expandEnvelope(Envelope env) {
		for (int i = 0; i < size; i++) {
			int index = map(i);
			env.expandToInclude(underlying.getX(index), underlying.getY(index));
		}

		return env;
	}

	@Override
	@SuppressWarnings("deprecation")
	public Object clone() {
		return copy();
	}

	@Override
	public CoordinateSequence copy() {
		throw new UnsupportedOperationException();
	}
}
