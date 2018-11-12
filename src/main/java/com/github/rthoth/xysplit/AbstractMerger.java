package com.github.rthoth.xysplit;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

public abstract class AbstractMerger<G extends Geometry> {

	protected final double offset;
	protected final Reference reference;

	public AbstractMerger(Reference reference, double offset) {
		this.reference = reference;
		this.offset = Math.abs(offset);
	}

	/**
	 *
	 * @param lts
	 * @param gts
	 * @return
	 */
	public abstract Geometry apply(GeometryFactory factory, Iterable<G> lts, Iterable<G> gts);
}
