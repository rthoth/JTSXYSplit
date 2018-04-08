package com.github.rthoth.xysplit;

import org.locationtech.jts.geom.Geometry;

/**
 * Response object.
 */
class R {

	public final Geometry lt;

	public final Geometry gt;

	public final Reference reference;

	public R(Geometry lt, Geometry gt, Reference reference) {
		this.lt = lt;
		this.gt = gt;
		this.reference = reference;
	}

	public static R from(Geometry lt, Geometry gt, Reference reference) {
		return new R(lt, gt, reference);
	}
}
