package com.github.rthoth.xysplit;

import static com.github.rthoth.xysplit.Assertions.*;

import org.junit.Test;
import org.locationtech.jts.geom.Geometry;


public class PolygonSplitTest extends PolygonTest {

	@Test
	public void polygon1_x1() {
		SplitResult splitResult = new XYSplitter(REF_X_1).apply(POLYGON_1);
		checkThat(splitResult.lt)
			.isEqualsTopo(wkt("POLYGON ((2 5, -4 5, -2 7, -3 8, 0 10, 2 9.5, 2 9, -1 9, -2 6, 2 8.4, 2 5))"));
		
		checkThat(splitResult.gt)
			.isEqualsTopo(wkt("MULTIPOLYGON (((2 9.5, 4 9, 2 7, 2 8.4, 3 9, 2 9, 2 9.5)), ((2 6, 4 5, 2 5, 2 6)))"));
	}

	@Test
	public void polygon1_x2() {
		SplitResult splitResult = new XYSplitter(REF_X_2).apply(POLYGON_1);
		checkThat(splitResult.lt)
			.isEqualsTopo(wkt("MULTIPOLYGON (((-2 8.666666666666666, -3 8, -2 7, -2 8.666666666666666)), ((-2 7, -4 5, -2 5, -2 7)))"));
		checkThat(splitResult.gt)
			.isEqualsTopo(wkt("POLYGON ((-2 8.666666666666666, 0 10, 4 9, 2 7, 2 6, 4 5, -2 5, -2 8.666666666666666), (-2 6, 3 9, -1 9, -2 6))"));
	}

	@Test
	public void polygon1_y1() {
		SplitResult splitResult = new XYSplitter(REF_Y_1).apply(POLYGON_1);
		checkThat(splitResult.lt).isEqualsTopo(wkt("POLYGON ((-3 6, -4 5, 4 5, 2 6, -3 6))"));
		checkThat(splitResult.gt)
			.isEqualsTopo(wkt("POLYGON ((2 6, 2 7, 4 9, 0 10, -3 8, -2 7, -3 6, 2 6), (-2 6, 3 9, -1 9, -2 6))"));
	}

	@Test
	public void polygon1_y2() {
		SplitResult splitResult = new XYSplitter(REF_Y_2).apply(POLYGON_1);
		assertThat(splitResult.lt.isEmpty()).isTrue();
		checkThat(splitResult.gt).isEqualTo(POLYGON_1);
	}

	@Test
	public void polygon2_x1() {
		SplitResult splitResult = new XYSplitter(REF_X_1).apply(POLYGON_2);
		checkThat(splitResult.lt)
			.isEqualsTopo(wkt(
												"POLYGON ((2 -6.8, 0 -10, -10 -4, -7 4, -7 -3, -2 2, -5 -3, 2 2, 2 0.3333333333333335, -2 -3, -2 -5, 2 -0.3333333333333339, 2 -6.8), (-5 -4, -3 -6, -6 -5, -5 -4))"));
		checkThat(splitResult.gt)
			.isEqualsTopo(wkt("POLYGON ((2 2, 9 7, 2 -3, 10 6, 2 -6.8, 2 -0.3333333333333339, 4 2, 2 0.3333333333333335, 2 2))"));
	}

	@Test
	public void polygon2_x2() {
		SplitResult splitResult = new XYSplitter(REF_X_2).apply(POLYGON_2);
		checkThat(splitResult.lt)
			.isEqualsTopo(wkt("POLYGON ((-2 -8.8, -10 -4, -7 4, -7 -3, -2 2, -5 -3, -2 -0.8571428571428568, -2 -3, -2 -5, -2 -8.8), (-5 -4, -3 -6, -6 -5, -5 -4))"));
		checkThat(splitResult.gt)
			.isEqualsTopo(wkt("POLYGON ((-2 -0.8571428571428568, 9 7, 2 -3, 10 6, 0 -10, -2 -8.8, -2 -5, 4 2, -2 -3, -2 -0.8571428571428568))"));
	}

	@Test
	public void polygon2_y1() {
		SplitResult splitResult = new XYSplitter(REF_Y_1).apply(POLYGON_2);
		checkThat(splitResult.lt)
			.isEqualsTopo(wkt("POLYGON ((8.3 6, 2 -3, 10 6, 0 -10, -10 -4, -7 4, -7 -3, -2 2, -5 -3, 7.6 6, 8.3 6), (-2 -3, 4 2, -2 -5, -2 -3), (-5 -4, -3 -6, -6 -5, -5 -4))"));
		checkThat(splitResult.gt).isEqualsTopo(wkt("POLYGON ((7.6 6, 9 7, 8.3 6, 7.6 6))"));
	}

	@Test
	public void polygon2_y2() {
		SplitResult splitResult = new XYSplitter(REF_Y_2).apply(POLYGON_2);
		checkThat(splitResult.lt)
			.isEqualsTopo(wkt("POLYGON ((4.375 -3, 0 -10, -10 -4, -9.625 -3, -2 -3, -2 -3, -2 -5, -0.2857142857142858 -3, 4.375 -3), (-5 -4, -3 -6, -6 -5, -5 -4))"));
		checkThat(splitResult.gt)
			.isEqualsTopo(wkt("POLYGON ((-9.625 -3, -7 4, -7 -3, -2 2, -5 -3, 9 7, 2 -3, 10 6, 4.375 -3, -0.2857142857142858 -3, 4 2, -2 -3, -9.625 -3))"));
	}

	@Test
	public void multi_x1() {
		SplitResult splitResult = new XYSplitter(REF_X_1).apply(MULTI);
		checkThat(splitResult.lt)
			.isEqualsTopo(wkt("MULTIPOLYGON (((2 5, -4 5, -2 7, -3 8, 0 10, 2 9.5, 2 9, -1 9, -2 6, 2 8.4, 2 5)), ((2 -6.8, 0 -10, -10 -4, -7 4, -7 -3, -2 2, -5 -3, 2 2, 2 0.3333333333333335, -2 -3, -2 -5, 2 -0.3333333333333339, 2 -6.8), (-5 -4, -3 -6, -6 -5, -5 -4)))"));
		checkThat(splitResult.gt)
			.isEqualsTopo(wkt("MULTIPOLYGON (((2 9.5, 4 9, 2 7, 2 8.4, 3 9, 2 9, 2 9.5)), ((2 6, 4 5, 2 5, 2 6)), ((2 2, 9 7, 2 -3, 10 6, 2 -6.8, 2 -0.3333333333333339, 4 2, 2 0.3333333333333335, 2 2)))"));
	}

	@Test
	public void multi_x2() {
		SplitResult splitResult = new XYSplitter(REF_X_2).apply(MULTI);
		checkThat(splitResult.lt)
			.isEqualsTopo(wkt("MULTIPOLYGON (((-2 5, -4 5, -2 7, -3 8, -2 8.666666666666666, -2 5)), ((-2 -8.8, -10 -4, -7 4, -7 -3, -2 2, -5 -3, -2 -0.8571428571428568, -2 -3, -2 -5, -2 -8.8), (-5 -4, -3 -6, -6 -5, -5 -4)))"));
		checkThat(splitResult.gt)
			.isEqualsTopo(wkt("MULTIPOLYGON (((-2 8.666666666666666, 0 10, 4 9, 2 7, 2 6, 4 5, -2 5, -2 8.666666666666666), (-2 6, 3 9, -1 9, -2 6)), ((-2 -0.8571428571428568, 9 7, 2 -3, 10 6, 0 -10, -2 -8.8, -2 -5, 4 2, -2 -3, -2 -0.8571428571428568)))"));
	}

	@Test
	public void multi_y1() {
		SplitResult splitResult = new XYSplitter(REF_Y_1).apply(MULTI);
		checkThat(splitResult.lt)
			.isEqualsTopo(wkt("MULTIPOLYGON (((2 6, 4 5, -4 5, -3 6, 2 6)), ((8.3 6, 2 -3, 10 6, 0 -10, -10 -4, -7 4, -7 -3, -2 2, -5 -3, 7.6 6, 8.3 6), (-2 -3, 4 2, -2 -5, -2 -3), (-5 -4, -3 -6, -6 -5, -5 -4)))"));
		checkThat(splitResult.gt)
			.isEqualsTopo(wkt("MULTIPOLYGON (((-3 6, -2 7, -3 8, 0 10, 4 9, 2 7, 2 6, -3 6), (-2 6, 3 9, -1 9, -2 6)), ((7.6 6, 9 7, 8.3 6, 7.6 6)))"));
	}

	@Test
	public void multi_y2() {
		SplitResult splitResult = new XYSplitter(REF_Y_2).apply(MULTI);
		checkThat(splitResult.lt)
			.isEqualsTopo(wkt("MULTIPOLYGON (((4.375 -3, 0 -10, -10 -4, -9.625 -3, -2 -3, -2 -5, -0.2857142857142858 -3, 4.375 -3), (-5 -4, -3 -6, -6 -5, -5 -4)))"));
		checkThat(splitResult.gt)
			.isEqualsTopo(wkt("MULTIPOLYGON (((0 10, 4 9, 2 7, 2 6, 4 5, -4 5, -2 7, -3 8, 0 10), (-2 6, 3 9, -1 9, -2 6)), ((-9.625 -3, -7 4, -7 -3, -2 2, -5 -3, 9 7, 2 -3, 10 6, 4.375 -3, -0.2857142857142858 -3, 4 2, -2 -3, -9.625 -3)))"));
	}

	@Test
	public void interior() {
		Geometry partial = new XYSplitter(REF_X_1).apply(MULTI).lt;
		partial = new XYSplitter(REF_Y_2).apply(partial).gt;
		partial = new XYSplitter(REF_X_2).apply(partial).gt;
		partial = new XYSplitter(REF_Y_1).apply(partial).lt;
		checkThat(partial)
			.isEqualsTopo(wkt("MULTIPOLYGON (((2 6, 2 5, -2 5, -2 6, 2 6)), ((-2 -0.8571428571428568, 2 2, 2 0.3333333333333335, -2 -3, -2 -0.8571428571428568)), ((-0.2857142857142856 -3, 2 -0.3333333333333339, 2 -3, -0.2857142857142856 -3)))"));
	}

	@Test
	public void polygon0_x0() {
		SplitResult result = new XYSplitter(REF_X_0).apply(POLYGON_0);
		checkThat(result.lt).isEqualsTopo(wkt("POLYGON ((0 4.6, -4 3, -4 -4, 0 -3.2, 0 -2.6666666666666665, -2 -2, -2 1, 0 1.6666666666666667, 0 4.6))"));
		checkThat(result.gt).isEqualsTopo(wkt("MULTIPOLYGON (((0 -3.2, 1 -3, 0 -2.6666666666666665, 0 -3.2)), ((0 1.6666666666666667, 1 2, 3 -1, 4 1, 1 5, 0 4.6, 0 1.6666666666666667)))"));
	}

	@Test
	public void polygon0_y0() {
		SplitResult result = new XYSplitter(REF_Y_0).apply(POLYGON_0);
		checkThat(result.lt).isEqualsTopo(wkt("MULTIPOLYGON (((-4 0, -4 -4, 1 -3, -2 -2, -2 0, -4 0)), ((2.3333333333333335 0, 3 -1, 3.5 0, 2.3333333333333335 0)))"));
		checkThat(result.gt).isEqualsTopo(wkt("POLYGON ((3.5 0, 4 1, 1 5, -4 3, -4 0, -2 0, -2 1, 1 2, 2.3333333333333335 0, 3.5 0))"));
	}
}
