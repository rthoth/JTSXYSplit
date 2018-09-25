package com.github.rthoth.xysplit;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Envelope;

class FlooredCoordinateSequence implements CoordinateSequence {

	private final CoordinateSequence underlying;
	private final int floor;

	public FlooredCoordinateSequence(CoordinateSequence underlying, int floor) {
		this.underlying = underlying;
		this.floor = floor;
	}

	@Override
	public int getDimension() {
		return underlying.getDimension();
	}

	@Override
	public Coordinate getCoordinate(int index) {
		return underlying.getCoordinate(index - floor);
	}

	@Override
	public Coordinate getCoordinateCopy(int index) {
		return underlying.getCoordinateCopy(index - floor);
	}

	@Override
	public void getCoordinate(int index, Coordinate coordinate) {
		underlying.getCoordinate(index - floor, coordinate);
	}

	@Override
	public double getX(int index) {
		return underlying.getX(index - floor);
	}

	@Override
	public double getY(int index) {
		return underlying.getY(index - floor);
	}

	@Override
	public double getOrdinate(int index, int ordinateIndex) {
		return underlying.getOrdinate(index - floor, ordinateIndex);
	}

	@Override
	public int size() {
		return underlying.size();
	}

	@Override
	public void setOrdinate(int index, int ordinateIndex, double value) {
		underlying.setOrdinate(index - floor, ordinateIndex, value);
	}

	@Override
	public Coordinate[] toCoordinateArray() {
		return underlying.toCoordinateArray();
	}

	@Override
	public Envelope expandEnvelope(Envelope env) {
		return underlying.expandEnvelope(env);
	}

	@Override
	public Object clone() {
		return copy();
	}

	@Override
	public CoordinateSequence copy() {
		return new FlooredCoordinateSequence(underlying.copy(), floor);
	}
}
