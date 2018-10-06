package com.github.rthoth.xysplit;

import org.locationtech.jts.geom.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

public class XYMerger implements BiFunction<Geometry, Geometry, Geometry> {
	
	static boolean isPolygon(Geometry geometry) {
		return geometry instanceof MultiPolygon || geometry instanceof Polygon;
	}

	static boolean isLineString(Geometry geometry) {
		return geometry instanceof MultiLineString || geometry instanceof LineString;
	}

	static boolean isPoint(Geometry geometry) {
		return geometry instanceof Point || geometry instanceof MultiPoint;
	}
	
	static List<Polygon> extractPolygons(Geometry geometry) {
		if (geometry instanceof Polygon)
			return Collections.singletonList((Polygon) geometry);
		else {
			List<Polygon> polygons = new ArrayList<>(geometry.getNumGeometries());
			for (int i = 0, l = geometry.getNumGeometries(); i < l; i++)
				polygons.add((Polygon) geometry.getGeometryN(i));
			return polygons;
		}
	}

	private final double offset;
	private final Reference reference;

	public XYMerger(Reference reference) {
		this(reference, XYSplitter.DEFAULT_OFFSET);
	}
	
	public XYMerger(Reference reference, double offset) {
		this.reference = reference;
		this.offset = offset;
	}

	public Geometry apply(Geometry lt, Geometry gt) {

		GeometryFactory factory = lt.getFactory() != null ? lt.getFactory() : gt.getFactory();

		if (isPolygon(lt) && isPolygon(gt)) {
			List<Polygon> lts = extractPolygons(lt), gts = extractPolygons(gt);
			return new PolygonMerger(reference, offset).apply(factory, lts, gts);
		}

		if (isLineString(lt) && isLineString(gt)) {
			throw new UnsupportedOperationException();
		}

		if (isPoint(lt) && isPoint(gt)) {
			throw new UnsupportedOperationException();
		}

		return new GeometryCollectionMerger(reference, offset).apply(factory, lt, gt);
	}

	@SuppressWarnings("unused")
	public XYSplitter splitter() {
		return new XYSplitter(reference, offset);
	}
}
