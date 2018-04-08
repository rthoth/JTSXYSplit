package com.github.rthoth.xysplit;

import org.junit.Test;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import static com.github.rthoth.xysplit.Assertions.*;


public class PolygonTest extends GeometryTest {

	private final CoordinateSequence SHELL_1 = coordinateSequence(0,10, 3,9, 2,7, 2,6, 4,5, -4,5, -2,7, -3,8, 0,10);
	private final CoordinateSequence SHELL_2 = coordinateSequence(2,-3, 10,6, 0,-10, -10,-4, -7,4, -7,-3, -2,2, -5,-3, 9,7, 2,-3);
	private final CoordinateSequence HOLE_1 = coordinateSequence(-2,6, 3,9, -1,9, -2,6);
	private final CoordinateSequence HOLE_2 = coordinateSequence(-2,-3, 4,2, -2,-5, -2,-3);
	private final CoordinateSequence HOLE_3 = coordinateSequence(-5,-4, -3,-6, -6,-5, -5,-4);

	private final LinearRing SHELL_1_RING = FACTORY.createLinearRing(SHELL_1);
	private final LinearRing SHELL_2_RING = FACTORY.createLinearRing(SHELL_2);
	private final LinearRing HOLE_1_RING = FACTORY.createLinearRing(HOLE_1);
	private final LinearRing HOLE_2_RING = FACTORY.createLinearRing(HOLE_2);
	private final LinearRing HOLE_3_RING = FACTORY.createLinearRing(HOLE_3);

	private final Polygon POLYGON_1 = FACTORY.createPolygon(SHELL_1_RING, new LinearRing[]{HOLE_1_RING});
	private final Polygon POLYGON_2 = FACTORY.createPolygon(SHELL_2_RING, new LinearRing[]{HOLE_2_RING, HOLE_3_RING});
	private final MultiPolygon MULTI = FACTORY.createMultiPolygon(new Polygon[]{POLYGON_1, POLYGON_2});

	private final Reference REF_X_1 = Reference.x(2);
	private final Reference REF_X_2 = Reference.x(-2);
	private final Reference REF_Y_1 = Reference.y(6);
	private final Reference REF_Y_2 = Reference.y(-3);

	@Test
	public void polygon1_x1() {
		R r = new XYSplitter(REF_X_1).apply(POLYGON_1);
	   checkThat(r.lt).isEqualsTopo(wkt("POLYGON ((2 9.333333333333334, 0 10, -3 8, -2 7, -4 5, 2 5, 2 8.4, -2 6, -1 9, 2 9, 2 9.333333333333334))"));
	   checkThat(r.gt).isEqualsTopo(wkt("MULTIPOLYGON (((2 5, 4 5, 2 6, 2 5)), ((2 7, 3 9, 2 9.333333333333334, 2 9, 3 9, 2 8.4, 2 7)))"));
	}
	
	@Test
	public void polygon1_x2() {
		R r = new XYSplitter(REF_X_2).apply(POLYGON_1);
	   checkThat(r.lt).isEqualsTopo(wkt("MULTIPOLYGON (((-2 8.666666666666666, -3 8, -2 7, -2 8.666666666666666)), ((-2 7, -4 5, -2 5, -2 7)))"));
	   checkThat(r.gt).isEqualsTopo(wkt("POLYGON ((-2 5, 4 5, 2 6, 2 7, 3 9, 0 10, -2 8.666666666666666, -2 5), (-2 6, 3 9, -1 9, -2 6))"));
	}
	
	@Test
	public void polygon1_y1() {
		R r = new XYSplitter(REF_Y_1).apply(POLYGON_1);
	   checkThat(r.lt).isEqualsTopo(wkt("POLYGON ((-3 6, -4 5, 4 5, 2 6, -3 6))"));
	   checkThat(r.gt).isEqualsTopo(wkt("POLYGON ((2 6, 2 7, 3 9, 0 10, -3 8, -2 7, -3 6, 2 6), (-2 6, 3 9, -1 9, -2 6))"));
	}
	
	@Test
	public void polygon1_y2() {
		R r = new XYSplitter(REF_Y_2).apply(POLYGON_1);
	   assertThat(r.lt.isEmpty()).isTrue();
	   checkThat(r.gt).isEqualTo(POLYGON_1);
	}
	
	@Test
	public void polygon2_x1() {
		R r = new XYSplitter(REF_X_1).apply(POLYGON_2);
	   checkThat(r.lt).isEqualsTopo(wkt("POLYGON ((2 -6.8, 0 -10, -10 -4, -7 4, -7 -3, -2 2, -5 -3, 2 2, 2 0.3333333333333335, -2 -3, -2 -5, 2 -0.3333333333333339, 2 -6.8), (-5 -4, -3 -6, -6 -5, -5 -4))"));
	   checkThat(r.gt).isEqualsTopo(wkt("POLYGON ((2 2, 9 7, 2 -3, 10 6, 2 -6.8, 2 -0.3333333333333339, 4 2, 2 0.3333333333333335, 2 2))"));
	}
	
	@Test
	public void polygon2_x2() {
		R r = new XYSplitter(REF_X_2).apply(POLYGON_2);
	   checkThat(r.lt).isEqualsTopo(wkt("POLYGON ((-2 -8.8, -10 -4, -7 4, -7 -3, -2 2, -5 -3, -2 -0.8571428571428568, -2 -3, -2 -5, -2 -8.8), (-5 -4, -3 -6, -6 -5, -5 -4))"));
	   checkThat(r.gt).isEqualsTopo(wkt("POLYGON ((-2 -0.8571428571428568, 9 7, 2 -3, 10 6, 0 -10, -2 -8.8, -2 -5, 4 2, -2 -3, -2 -0.8571428571428568))"));
	}

	@Test
	public void polygon2_y1() {
		R r = new XYSplitter(REF_Y_1).apply(POLYGON_2);
		checkThat(r.lt).isEqualsTopo(wkt("POLYGON ((8.3 6, 2 -3, 10 6, 0 -10, -10 -4, -7 4, -7 -3, -2 2, -5 -3, 7.6 6, 8.3 6), (-2 -3, 4 2, -2 -5, -2 -3), (-5 -4, -3 -6, -6 -5, -5 -4))"));
		checkThat(r.gt).isEqualsTopo(wkt("POLYGON ((7.6 6, 9 7, 8.3 6, 7.6 6))"));
	}
	
	@Test
	public void polygon2_y2() {
		R r = new XYSplitter(REF_Y_2).apply(POLYGON_2);
		checkThat(r.lt).isEqualsTopo(wkt("POLYGON ((4.375 -3, 0 -10, -10 -4, -9.625 -3, -2 -3, -2 -3, -2 -5, -0.2857142857142858 -3, 4.375 -3), (-5 -4, -3 -6, -6 -5, -5 -4))"));
		checkThat(r.gt).isEqualsTopo(wkt("POLYGON ((-9.625 -3, -7 4, -7 -3, -2 2, -5 -3, 9 7, 2 -3, 10 6, 4.375 -3, -0.2857142857142858 -3, 4 2, -2 -3, -9.625 -3))"));
	}
}
