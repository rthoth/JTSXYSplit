package com.github.rthoth.xysplit;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.PackedCoordinateSequence;
import org.locationtech.jts.geom.impl.PackedCoordinateSequenceFactory;
import org.locationtech.jts.io.OutputStreamOutStream;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.WKTReader;

import java.io.*;
import java.nio.file.Files;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.*;


abstract class GeometryTest {

	protected static final GeometryFactory FACTORY = new GeometryFactory(PackedCoordinateSequenceFactory.DOUBLE_FACTORY);

	protected static CoordinateSequence coordinateSequence(double... ords) {
		return new PackedCoordinateSequence.Double(ords, 2);
	}

	protected static List<Geometry> unique(List<Geometry> origins) {
		LinkedList<Geometry> ret = new LinkedList<>();

		go:
		for (Geometry geometry : origins) {
			geometry = geometry.buffer(0D);
			for (Geometry other : ret) {
				if (geometry.equals(other))
					continue go;
			}

			ret.addLast(geometry);
		}

		return ret;
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

	protected File file(String path) {
		return new File(path.replace('/', File.separatorChar));
	}

	protected List<Coordinate> list(CoordinateSequence sequence) {
		ArrayList<Coordinate> result = new ArrayList<>(sequence.size());
		for (int i = 0; i < sequence.size(); i++) {
			result.add(sequence.getCoordinate(i));
		}
		return result;
	}

	protected <G extends Geometry> List<G> wktz(File file) {
		try (InflaterInputStream input = new GZIPInputStream(new FileInputStream(file))) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			LinkedList<G> geometries = new LinkedList<>();

			String line;
			while ((line = reader.readLine()) != null) {
				geometries.addLast(wkt(line));
			}

			return geometries;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected <G extends Geometry> G wkt(File file) {
		try (Reader input = new InputStreamReader(new FileInputStream(file))) {
			char[] chars = new char[1024];
			CharArrayWriter writer = new CharArrayWriter();
			int read;
			do {
				read = input.read(chars);
				if (read > 0) {
					writer.write(chars, 0, read);
				}
			} while (read != -1);

			return wkt(writer.toString());
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	protected <G extends Geometry> G wkt(String wkt) {
		try {
			return (G) new WKTReader(FACTORY).read(wkt);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
}
