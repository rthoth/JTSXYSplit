package com.github.rthoth.xysplit;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * A Line splitter.
 */
public class LineStringSplitter extends AbstractSplitter<LineString> {

	public LineStringSplitter(Reference reference, double offset) {
		super(new SplitSequencer.Line(reference, offset));
	}

	@Override
	public SplitResult apply(LineString lineString) {
		return split(lineString, sequencer);
	}

	@Override
	public SplitResult apply(LineString lineString, int padding) {
		return split(lineString, sequence -> sequencer.apply(sequence, padding));
	}

	private SplitResult split(LineString lineString, Function<CoordinateSequence, SplitSequencer.Result> sequencer) {
		SplitSequencer.Result result = sequencer.apply(lineString.getCoordinateSequence());

		return new SplitResult(
						new Splitter(Side.LT, lineString, result.lt).result,
						new Splitter(Side.GT, lineString, result.gt).result
		);
	}

	private class Splitter {

		private final LineString original;
		private final Side side;
		private final Deque<SplitEvent> events;
		private final Geometry result;

		public Splitter(Side side, LineString original, List<SplitEvent> events) {
			throw new UnsupportedOperationException();
		}
	}
}
