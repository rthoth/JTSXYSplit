package com.github.rthoth.xysplit;

import org.locationtech.jts.geom.*;

import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;

public class GeometryCollectionMerger extends AbstractMerger<Geometry> {

	public GeometryCollectionMerger(Reference reference, double offset) {
		super(reference, offset);
	}

	public Geometry apply(GeometryFactory factory, Geometry lt, Geometry gt) {
		return apply(factory, Collections.singletonList(lt), Collections.singletonList(gt));
	}

	@Override
	public Geometry apply(GeometryFactory factory, Iterable<Geometry> lts, Iterable<Geometry> gts) {

		LinkedList<Polygon> ltPolygons = new LinkedList<>();
		LinkedList<Polygon> gtPolygons = new LinkedList<>();

		filter(lts, ltPolygons);
		filter(gts, gtPolygons);

		return new PolygonMerger(reference, offset).apply(factory, ltPolygons, gtPolygons);
	}

	private void filter(Iterable<Geometry> geometries, Deque<Polygon> polygons) {
		for (Geometry geometry : geometries)
			filter(geometry, polygons);
	}

	private void filter(Geometry geometry, Deque<Polygon> polygons) {
		if (geometry instanceof Polygon)
			polygons.addLast((Polygon) geometry);

		else if (geometry instanceof MultiPolygon) {
			for (int i = 0; i < geometry.getNumGeometries(); i++)
				polygons.addLast((Polygon) geometry.getGeometryN(i));

		} else if (geometry.getClass() == GeometryCollection.class) {
			for (int i = 0; i < geometry.getNumGeometries(); i++)
				filter(geometry.getGeometryN(i), polygons);
		}
	}
}
