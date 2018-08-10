package com.github.rthoth.xysplit;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequences;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.impl.PackedCoordinateSequenceFactory;

import java.util.List;
import java.util.TreeMap;

public class SegmentedCoordinateSequence implements CoordinateSequence {

	private final TreeMap<Integer, CoordinateSequence> sequences = new TreeMap<>();
	private final int size;

	public SegmentedCoordinateSequence(List<CoordinateSequence> sequences) {
		int floor = 0;

		for (CoordinateSequence sequence : sequences) {
			this.sequences.put(floor, new FlooredCoordinateSequence(sequence, floor));
			floor += sequence.size();
		}

		size = floor;
	}

	private CoordinateSequence getSequence(int index) {
		return sequences.floorEntry(index).getValue();
	}

	@Override
	public int getDimension() {
		return 2;
	}

	@Override
	public Coordinate getCoordinate(int index) {
		return getSequence(index).getCoordinate(index);
	}

	@Override
	public Coordinate getCoordinateCopy(int index) {
		return getSequence(index).getCoordinateCopy(index);
	}

	@Override
	public void getCoordinate(int index, Coordinate coordinate) {
		getSequence(index).getCoordinate(index, coordinate);
	}

	@Override
	public double getX(int index) {
		return getSequence(index).getX(index);
	}

	@Override
	public double getY(int index) {
		return getSequence(index).getY(index);
	}

	@Override
	public double getOrdinate(int index, int ordinateIndex) {
		return getSequence(index).getOrdinate(index, ordinateIndex);
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public void setOrdinate(int index, int ordinateIndex, double value) {
		getSequence(index).setOrdinate(index, ordinateIndex, value);
	}

	@Override
	public Coordinate[] toCoordinateArray() {
		// TODO: Refactor!
		Coordinate[] array = new Coordinate[size];
		for (int i = 0; i < size; i++)
			array[i] = getSequence(i).getCoordinate(i);

		return array;
	}

	@Override
	public Envelope expandEnvelope(Envelope env) {
		for (CoordinateSequence sequences : sequences.values()) {
			env = sequences.expandEnvelope(env);
		}

		return env;
	}

	@SuppressWarnings("deprecation")
	@Override
	public Object clone() {
		return copy();
	}

	@Override
	public CoordinateSequence copy() {
		double[] coords = new double[size() * 2];

		for (int i = 0, l = size(), p = 0; i < l; i++) {
			coords[p++] = getX(i);
			coords[p++] = getY(i);
		}

		return PackedCoordinateSequenceFactory.DOUBLE_FACTORY.create(coords, 2);
	}

	@Override
	public String toString() {
		return CoordinateSequences.toString(this);
	}
}
