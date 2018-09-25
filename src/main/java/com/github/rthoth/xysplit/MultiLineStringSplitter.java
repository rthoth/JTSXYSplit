package com.github.rthoth.xysplit;

import org.locationtech.jts.geom.*;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

class MultiLineStringSplitter implements Function<MultiLineString, SplitResult> {

	private final LineStringSplitter underlying;

	public MultiLineStringSplitter(Reference reference, double offset) {
	   underlying = new LineStringSplitter(reference, offset);
	}

	private void add(Geometry geometry, List<LineString> geometries) {
		if (!geometry.isEmpty()) {
			if (geometry instanceof LineString) {
				geometries.add((LineString) geometry);
			} else if (geometry instanceof MultiLineString) {
				for (int i = 0; i < geometry.getNumGeometries(); i++) {
					geometries.add((LineString) geometry.getGeometryN(i));
				}
			} else {
				throw new TopologyException(String.format("Invalid Geometry! It should be a LineString, but it was %s!", geometry.getClass().getSimpleName()));
			}
		}
	}

	public SplitResult apply(MultiLineString lineString) {

		LinkedList<LineString> lt = new LinkedList<>(), gt = new LinkedList<>();

		for (int i = 0; i < lineString.getNumGeometries(); i++) {
			SplitResult splitResult = underlying.apply((LineString) lineString.getGeometryN(i));
			add(splitResult.lt, lt);
			add(splitResult.gt, gt);
		}

		MultiLineString ltResponse = lineString.getFactory().createMultiLineString(lt.toArray(new LineString[0]));
		MultiLineString gtResponse = lineString.getFactory().createMultiLineString(gt.toArray(new LineString[0]));

		return new SplitResult(ltResponse, gtResponse);
	}

	public SplitResult applyPadding(MultiLineString multiLineString, int padding) {
		throw new UnsupportedOperationException();
	}
}
