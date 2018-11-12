package com.github.rthoth.xysplit;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;

import java.util.Comparator;

import static com.github.rthoth.xysplit.InOnOut.ON;

class MergeEvent<T> {

	protected final double position;
	protected final Node lt;
	protected final Node gt;

	protected final T touched;

	public MergeEvent(double position, Node lt, Node gt, T touched) {
		this.position = position;
		this.lt = lt;
		this.gt = gt;
		this.touched = touched;
	}

	public MergeEvent(double position, Node lt, Node gt) {
		this(position, lt, gt, null);
	}

	public Node get(Side side) {
		switch (side) {
			case LT:
				return lt;

			case GT:
				return gt;

			default:
				throw new IllegalArgumentException(side.toString());
		}
	}

	public boolean canBeOrigin() {
		if (lt != null && gt != null) {
			return lt.inOnOut != ON && gt.inOnOut != ON && lt.inOnOut != gt.inOnOut;
		} else if (lt != null && lt.inOnOut != ON) {
			return true;
		} else {
			return gt != null && gt.inOnOut != ON;
		}
	}

	public static class Node {

		public static final Comparator<Node> INDEX_COMPARATOR = (a, b) -> a.index - b.index;
		public static final Comparator<Node> POSITION_COMPARATOR = (a, b) -> Double.compare(a.position, b.position);

		public final int index;
		public final double position;
		public final CoordinateSequence sequence;
		public final InOnOut inOnOut;

		public Node(int index, double position, CoordinateSequence sequence, InOnOut inOnOut) {
			this.index = index;
			this.position = position;
			this.sequence = sequence;
			this.inOnOut = inOnOut;
		}

		public Node copy(int index, CoordinateSequence sequence) {
			return new Node(index, position, sequence, inOnOut);
		}

		@Override
		public String toString() {
			return String.format("(i=%d, p=%f, l=%s)", index, inOnOut, inOnOut);
		}

		public double getX() {
			return sequence.getX(index);
		}

		public double getY() {
			return sequence.getY(index);
		}

		public Coordinate getCoordinate() {
			return sequence.getCoordinate(index);
		}

		public Node withIndex(int newIndex) {
			return new Node(newIndex, position, sequence, inOnOut);
		}

		public Node withSequence(CoordinateSequence sequence) {
			return new Node(index, position, sequence, inOnOut);
		}

		public Node withIndex(int newIndex, InOnOut newInOnOut) {
			return new Node(newIndex, position, sequence, newInOnOut);
		}
	}
}
