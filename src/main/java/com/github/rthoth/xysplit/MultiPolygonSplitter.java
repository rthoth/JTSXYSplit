package com.github.rthoth.xysplit;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.TopologyException;


class MultiPolygonSplitter implements Function<MultiPolygon, R> {

   private final PolygonSplitter polygonSplitter;

	public MultiPolygonSplitter(Reference reference) {
	   polygonSplitter = new PolygonSplitter(reference);
	}

	private void add(Geometry geometry, List<Polygon> polygons) {
		if (!geometry.isEmpty()) {
			if (geometry instanceof Polygon)
				polygons.add((Polygon) geometry);
			else if (geometry instanceof MultiPolygon) {
				for (int i = 0; i < geometry.getNumGeometries(); i++) {
					polygons.add((Polygon) geometry.getGeometryN(i));
				}
			} else if (geometry != null) {
				throw new TopologyException("Invalid polygon!");
			}
		}
	}

	public R apply(MultiPolygon multiPolygon) {

		LinkedList<Polygon> lt = new LinkedList<>(), gt = new LinkedList<>();

	   for (int index = 0; index < multiPolygon.getNumGeometries(); index++) {
			R r = polygonSplitter.apply((Polygon) multiPolygon.getGeometryN(index));
			add(r.lt, lt);
			add(r.gt, gt);
		}

		GeometryFactory factory = multiPolygon.getFactory();

		MultiPolygon ltResponse = factory.createMultiPolygon(lt.toArray(new Polygon[lt.size()]));
		MultiPolygon gtResponse = factory.createMultiPolygon(gt.toArray(new Polygon[gt.size()]));
		
		return R.from(ltResponse, gtResponse, polygonSplitter.reference);
	}
}
