package com.github.rthoth.xysplit;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;

import java.util.function.Function;

abstract class AbstractSplitter<G extends Geometry> implements Function<G, SplitResult> {

	static double crossProduct(SplitEvent event) {
		int limit = event.sequence.size() - 1;
		int nextIndex = (event.index + 1) % limit;
		int prevIndex = (event.index - 1) % limit;
		if (prevIndex < 0)
			prevIndex += limit;

		CoordinateSequence sequence = event.sequence;
		double xn = sequence.getX(nextIndex), yn = sequence.getY(nextIndex);
		double xp = sequence.getX(prevIndex), yp = sequence.getY(prevIndex);
		double x0 = sequence.getX(event.index), y0 = sequence.getY(event.index);

		return (xn - x0) * (yp - y0) - (yn - y0) * (xp - x0);
	}

	protected final SplitSequencer sequencer;

	public AbstractSplitter(SplitSequencer sequencer) {
		this.sequencer = sequencer;
	}

	public abstract SplitResult apply(G geometry, int padding);
}
