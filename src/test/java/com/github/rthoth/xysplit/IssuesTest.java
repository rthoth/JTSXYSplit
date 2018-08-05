package com.github.rthoth.xysplit;

import org.junit.Test;
import org.locationtech.jts.geom.Geometry;

public class IssuesTest extends GeometryTest {

	@Test
	public void issue_01() {
		Geometry polygon = wkt(file("test-data/issues/01/polygon.wkt"));
		SplitResult result = new XYSplitter(Reference.y(-42.264973081037425)).apply(polygon);
	}

	@Test
	public void issue_02() {
		Geometry polygon = wkt(file("test-data/issues/02/polygon.wkt"));
		new XYSplitter(Reference.x(-5.555555555555559)).apply(polygon);
	}

	@Test
	public void issue_03() {
		Geometry polygon = wkt("POLYGON ((41.92386831275719 78.10543026389844, 41.9753086419753 78.19452752766227, 42.02674897119341 78.10543026389844, 41.92386831275719 78.10543026389844))");
		new XYSplitter(Reference.x(41.9753086419753)).apply(polygon);
	}
}
