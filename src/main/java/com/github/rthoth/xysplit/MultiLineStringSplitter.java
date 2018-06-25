package com.github.rthoth.xysplit;

import java.util.function.Function;
import org.locationtech.jts.geom.*;
import java.util.LinkedList;
import java.util.List;

public class MultiLineStringSplitter implements Function<MultiLineString, SplitResult> {

	private final LineStringSplitter underlying;

	public MultiLineStringSplitter(Reference reference) {
	   underlying = new LineStringSplitter(reference);
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
				throw new TopologyException("Invalid lineString!");
			}
		}
	}

	public SplitResult apply(MultiLineString lineString) {

		LinkedList<LineString> lts = new LinkedList<>(), gts = new LinkedList<>();
		GeometryFactory factory = lineString.getFactory();

		for (int i = 0; i < lineString.getNumGeometries(); i++) {
			SplitResult splitResult = underlying.apply((LineString) lineString.getGeometryN(i));
			add(splitResult.lt, lts);
		}

		Geometry lt = (lts.size() > 1) ?
			factory.createMultiLineString(lts.toArray(new LineString[lts.size()])) :
			lts.get(0);

		Geometry gt = (gts.size() > 1) ?
			factory.createMultiLineString(gts.toArray(new LineString[gts.size()])) :
			gts.get(0);
		
		return new SplitResult(lt, gt, underlying.reference);
	}
}
