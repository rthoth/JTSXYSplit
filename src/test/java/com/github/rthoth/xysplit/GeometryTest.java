package com.github.rthoth.xysplit;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.impl.PackedCoordinateSequence;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import java.util.List;
import org.locationtech.jts.geom.Coordinate;
import java.util.ArrayList;


abstract class GeometryTest {

	protected final GeometryFactory FACTORY = new GeometryFactory();

	protected static CoordinateSequence coordinateSequence(double... ords) {
		return new PackedCoordinateSequence.Double(ords, 2);
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
