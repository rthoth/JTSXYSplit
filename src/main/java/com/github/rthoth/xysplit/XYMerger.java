package com.github.rthoth.xysplit;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class XYMerger {

	private final Reference reference;
	
	public static boolean isPolygon(Geometry geometry) {
		return geometry instanceof MultiPolygon || geometry instanceof Polygon;
	}
	
	public static List<Polygon> extractPolygons(Geometry geometry) {
		if (geometry instanceof Polygon)
			return Collections.singletonList((Polygon) geometry);
		else {
			List<Polygon> polygons = new ArrayList<>(geometry.getNumGeometries());
			for (int i = 0, l = geometry.getNumGeometries(); i < l; i++)
				polygons.add((Polygon) geometry.getGeometryN(i));
			return polygons;
		}
	}
	
	public XYMerger(Reference reference) {
		this.reference = reference;
	}

	public Geometry apply(Geometry lt, Geometry gt) {
		return apply(lt, gt, MergeSequence.DEFAULT_OFFSET);
	}
	
	public Geometry apply(Geometry lt, Geometry gt, double offset) {
		if (isPolygon(lt) && isPolygon(gt)) {
			List<Polygon> lts = extractPolygons(lt), gts = extractPolygons(gt);
			return new PolygonMerger(reference, offset).apply(lts, gts);
		}
		return null;		
	}
}
