package com.github.rthoth.xysplit;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.PackedCoordinateSequence;
import org.locationtech.jts.geom.impl.PackedCoordinateSequenceFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


abstract class GeometryTest {

	protected static final GeometryFactory FACTORY = new GeometryFactory(PackedCoordinateSequenceFactory.DOUBLE_FACTORY);

	protected static CoordinateSequence coordinateSequence(double... ords) {
		return new PackedCoordinateSequence.Double(ords, 2);
	}

	protected List<CoordinateSequence> extractShell(Geometry geometry) {
		if (geometry instanceof Polygon) {
			return Collections.singletonList(((Polygon) geometry).getExteriorRing().getCoordinateSequence());
		} else if (geometry instanceof MultiPolygon) {
			MultiPolygon multi = (MultiPolygon) geometry;
			return IntStream
							.range(0, multi.getNumGeometries())
							.mapToObj(i -> ((Polygon) multi.getGeometryN(i)).getExteriorRing().getCoordinateSequence())
							.collect(Collectors.toList());
		} else {
			return null;
		}
	}

	protected List<Coordinate> list(CoordinateSequence sequence) {
		ArrayList<Coordinate> result = new ArrayList<>(sequence.size());
		for (int i = 0; i < result.size(); i++) {
			result.add(sequence.getCoordinate(i));
		}
		return result;
	}
	
	protected Geometry wkt(String wkt) {
		try {
			return new WKTReader(FACTORY).read(wkt);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
}
