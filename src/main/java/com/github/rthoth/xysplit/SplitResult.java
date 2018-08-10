package com.github.rthoth.xysplit;

import org.locationtech.jts.geom.Geometry;

/**
 * Response object.
 */
public class SplitResult {
	public final Geometry lt;
	public final Geometry gt;

	public SplitResult(Geometry lt, Geometry gt) {
		this.lt = lt;
		this.gt = gt;
	}
}
