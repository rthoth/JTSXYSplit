package com.github.rthoth.xysplit;

import org.locationtech.jts.geom.*;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;


public class MultiPolygonSplitter extends AbstractSplitter<MultiPolygon> {

	private final PolygonSplitter underlying;

	public MultiPolygonSplitter(Reference reference, double offset) {
		super(null);
		underlying = new PolygonSplitter(reference, offset);
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
			SplitResult splitResult = underlying.apply((Polygon) multiPolygon.getGeometryN(index));
			add(splitResult.lt, lt);
			add(splitResult.gt, gt);
		}

		GeometryFactory factory = multiPolygon.getFactory();

		MultiPolygon ltResponse = factory.createMultiPolygon(lt.toArray(new Polygon[0]));
		MultiPolygon gtResponse = factory.createMultiPolygon(gt.toArray(new Polygon[0]));

		return new SplitResult(ltResponse, gtResponse);
	}

	@Override
	public SplitResult apply(MultiPolygon multiPolygon, final int padding) {
		LinkedList<Polygon> lt = new LinkedList<>(), gt = new LinkedList<>();

		IntStream.range(0, multiPolygon.getNumGeometries())
						.mapToObj(i -> (Polygon) multiPolygon.getGeometryN(i))
						.forEach(polygon -> {
							SplitResult result = underlying.apply(polygon, padding);
							add(result.lt, lt);
							add(result.gt, gt);
						});

		GeometryFactory factory = multiPolygon.getFactory();
		MultiPolygon ltResponse = factory.createMultiPolygon(lt.toArray(new Polygon[0]));
		MultiPolygon gtResponse = factory.createMultiPolygon(gt.toArray(new Polygon[0]));

		return new SplitResult(ltResponse, gtResponse);
	}
}
