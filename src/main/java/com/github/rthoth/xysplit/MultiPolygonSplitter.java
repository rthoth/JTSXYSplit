package com.github.rthoth.xysplit;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.TopologyException;


class MultiPolygonSplitter implements Function<MultiPolygon, SplitResult> {

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
					if (!geometry.getGeometryN(i).isEmpty())
						polygons.add((Polygon) geometry.getGeometryN(i));
				}
			} else {
				throw new TopologyException(String.format("Invalid Geometry! It should be a Polygon, but it was %s!", geometry.getClass().getSimpleName()));
			}
		}
	}

	public SplitResult apply(MultiPolygon multiPolygon) {

		LinkedList<Polygon> lt = new LinkedList<>(), gt = new LinkedList<>();

		for (int index = 0; index < multiPolygon.getNumGeometries(); index++) {
			SplitResult splitResult = polygonSplitter.apply((Polygon) multiPolygon.getGeometryN(index));
			add(splitResult.lt, lt);
			add(splitResult.gt, gt);
		}

		GeometryFactory factory = multiPolygon.getFactory();

		MultiPolygon ltResponse = factory.createMultiPolygon(lt.toArray(new Polygon[0]));
		MultiPolygon gtResponse = factory.createMultiPolygon(gt.toArray(new Polygon[0]));

		return new SplitResult(ltResponse, gtResponse, polygonSplitter.reference);
	}
}
