package com.github.rthoth.xysplit;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;

import java.util.Collection;
import java.util.Comparator;

public class MergeEvent implements Comparable<MergeEvent> {

	public final double position;

	public final Node lt;

	public final Node gt;

	public MergeEvent(double position, Node lt, Node gt) {
		this.position = position;
		this.lt = lt;
		this.gt = gt;
	}

	public Side nextSide(Side side) {
		if (lt.location != gt.location) {
			return lt.location == Location.IN ? Side.LT : Side.GT;
		} else {
			return side;
		}
	}

	@Override
	public int compareTo(MergeEvent other) {
		return Double.compare(position, other.position);
	}

	public Node getNode(Side side) {
		return side == Side.LT ? lt : gt;
	}

	@Override
	public String toString() {
		return lt + " <<>> " + gt;
	}

	public static class Node implements Comparable<Node> {

		@SuppressWarnings("ComparatorCombinators")
		public final CoordinateSequence sequence;
		public final int index;
		public final Location location;
		public final double position;

		public Node(int index, Location location, double position, CoordinateSequence sequence) {
			this.position = position;
			this.sequence = sequence;
			this.index = index;
			this.location = location;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Node) {
				Node other = (Node) obj;
				return index == other.index && position == other.position && location == other.location && sequence == other.sequence;
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return index + ((int) position);
		}

		@Override
		public int compareTo(Node other) {
			return Double.compare(position, other.position);
		}

		@Override
		public String toString() {
			return String.format("(%d, %s, %s, %s)", index, location, position, getCoordinate());
		}

		public Coordinate getCoordinate() {
			return sequence.getCoordinate(index);
		}
	}
}
