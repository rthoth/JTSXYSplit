package com.github.rthoth.xysplit;

import org.locationtech.jts.geom.*;

import java.util.function.Function;


/**
 * This is a main class. You can use this to split your geometries.
 */
public class XYSplitter implements Function<Geometry, SplitResult> {

	static final double DEFAULT_OFFSET = 1e-8;

	public final Reference reference;
	public final double offset;

	public XYSplitter(Reference reference) {
		this(reference, DEFAULT_OFFSET);
	}

	public XYSplitter(Reference reference, double offset) {
		this.reference = reference;
		this.offset = Math.abs(offset);
	}

	public XYMerger merger() {
		return new XYMerger(reference, offset);
	}

	public SplitResult apply(Geometry geometry) {

		if (geometry instanceof Polygon) {
			return new PolygonSplitter(reference, offset).apply((Polygon) geometry);
		} else if (geometry instanceof MultiPolygon) {
			return new MultiPolygonSplitter(reference, offset).apply((MultiPolygon) geometry);
		} else if (geometry instanceof LineString) {
			return new LineStringSplitter(reference, offset).apply((LineString) geometry);
		} else if (geometry instanceof MultiLineString) {
			return new MultiLineStringSplitter(reference, offset).apply((MultiLineString) geometry);
		} else {
			throw new TopologyException("Invalid geometry type " + geometry.getClass().getName() + "!");
		}
	}

	@SuppressWarnings("unused")
	public SplitResult apply(Geometry geometry, int padding) {
		if (padding > 2) {
			if (geometry instanceof Polygon)
				return new PolygonSplitter(reference, offset).apply((Polygon) geometry, padding);
			else if (geometry instanceof MultiPolygon)
				return new MultiPolygonSplitter(reference, offset).applyPadding((MultiPolygon) geometry, padding);
			else if (geometry instanceof LineString)
				return new LineStringSplitter(reference, offset).apply((LineString) geometry, padding);
			else if (geometry instanceof MultiLineString)
				return new MultiLineStringSplitter(reference, offset).applyPadding((MultiLineString) geometry, padding);
			else
				throw new TopologyException("Invalid geometry type " + geometry.getClass().getName() + "!");
		} else
			return apply(geometry);
	}
}

