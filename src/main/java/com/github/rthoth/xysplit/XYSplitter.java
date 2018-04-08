package com.github.rthoth.xysplit;

import java.util.function.Function;
import org.locationtech.jts.geom.*;

/**
 * This is a main class. You can use this to split yours geometries.
 */
public class XYSplitter implements Function<Geometry, R> {

	public final Reference reference;

	public XYSplitter(Reference reference) {
		this.reference = reference;
	}

	public R apply(Geometry geometry) {

		if (geometry instanceof Polygon) {
			return new PolygonSplitter(reference).apply((Polygon) geometry);
		} else if (geometry instanceof MultiPolygon) {
			return new MultiPolygonSplitter(reference).apply((MultiPolygon) geometry);
		} else if (geometry instanceof LineString) {
			return new LineStringSplitter(reference).apply((LineString) geometry);
		} else if (geometry instanceof MultiLineString) {
			return new MultiLineStringSplitter(reference).apply((MultiLineString) geometry);
		} else {
			throw new TopologyException("Invalid geometry type " + geometry.getClass().getName() + "!");
		}
	}
}

