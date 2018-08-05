package com.github.rthoth.xysplit;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.function.Function;

import org.locationtech.jts.geom.*;

/**
 * A Line splitter.
 */
public class LineStringSplitter extends Splitter<LineString> {

	public final Reference reference;

	public LineStringSplitter(Reference reference) {
		this.reference = reference;
	}

	public SplitResult apply(LineString lineString) {
		SplitSequence sequence =
						new SplitSequence.Line(reference, lineString.getCoordinateSequence());

		Builder lt = new Builder(Side.LT, sequence, lineString);
		Builder gt = new Builder(Side.GT, sequence, lineString);

		return new SplitResult(lt.result, gt.result, reference);
	}

	private class Builder {

		public final Geometry result;

		public Builder(Side side, SplitSequence sequence, LineString lineString) {
			throw new UnsupportedOperationException();
		}
	}
}
