package com.github.rthoth.xysplit;

import org.locationtech.jts.geom.Geometry;

/**
 * Response object.
 */
public class SplitResult {

	public final Geometry lt;

	public final Geometry gt;

	public final Reference reference;

	public SplitResult(Geometry lt, Geometry gt, Reference reference) {
		this.lt = lt;
		this.gt = gt;
		this.reference = reference;
	}
}
