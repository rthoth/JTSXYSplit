package com.github.rthoth.xysplit;

import static com.github.rthoth.xysplit.Assertions.*;

import org.junit.Test;
import org.locationtech.jts.geom.Geometry;


public class PolygonMergeTest extends PolygonTest {

	@Test
	public void polygon1_x1() {
		SplitResult splitResult = new XYSplitter(REF_X_1).apply(POLYGON_1);
		Geometry merged = new XYMerger(REF_X_1).apply(splitResult.lt, splitResult.gt);
		checkThat(merged).isEqualsTopo(wkt("POLYGON ((2 5, -4 5, -2 7, -3 8, 0 10, 2 9.5, 4 9, 2 7, 2 6, 4 5, 2 5), (2 8.4, 3 9, 2 9, -1 9, -2 6, 2 8.4))"));
	}

	@Test
	public void polygon1_x2() {
		SplitResult splitResult = new XYSplitter(REF_X_2).apply(POLYGON_1);
		Geometry merged = new XYMerger(REF_X_2).apply(splitResult.lt, splitResult.gt);
		checkThat(merged).isEqualsTopo(wkt("POLYGON ((-2 5, -4 5, -2 7, -3 8, -2 8.666666666666666, 0 10, 4 9, 2 7, 2 6, 4 5, -2 5), (-2 6, 3 9, -1 9, -2 6))"));
	}

	@Test
	public void polygon1_y1() {
		SplitResult splitResult = new XYSplitter(REF_Y_1).apply(POLYGON_1);
		Geometry merged = new XYMerger(REF_Y_1).apply(splitResult.lt, splitResult.gt);
		checkThat(merged).isEqualsTopo(POLYGON_1);
	}

	@Test
	public void polygon1_y2() {
		SplitResult splitResult = new XYSplitter(REF_Y_2).apply(POLYGON_1);
		Geometry merged = new XYMerger(REF_Y_2).apply(splitResult.lt, splitResult.gt);
		checkThat(merged).isEqualsTopo(POLYGON_1);
	}

	@Test
	public void polygon2_x1() {
		SplitResult splitResult = new XYSplitter(REF_X_1).apply(POLYGON_2);
		Geometry merged = new XYMerger(REF_X_1).apply(splitResult.lt, splitResult.gt);
		checkThat(merged).isEqualsTopo(wkt("POLYGON ((2 -6.8, 0 -10, -10 -4, -7 4, -7 -3, -2 2, -5 -3, 2 2, 9 7, 2 -3, 10 6, 2 -6.8), (-5 -4, -3 -6, -6 -5, -5 -4), (2 -0.3333333333333339, 4 2, 2 0.3333333333333335, -2 -3, -2 -5, 2 -0.3333333333333339))"));
	}

	@Test
	public void polygon2_x2() {
		SplitResult splitResult = new XYSplitter(REF_X_2).apply(POLYGON_2);
		Geometry merged = new XYMerger(REF_X_2).apply(splitResult.lt, splitResult.gt);
		checkThat(merged).isEqualsTopo(wkt("POLYGON ((-2 -8.8, -10 -4, -7 4, -7 -3, -2 2, -5 -3, -2 -0.8571428571428568, 9 7, 2 -3, 10 6, 0 -10, -2 -8.8), (-5 -4, -3 -6, -6 -5, -5 -4), (-2 -5, 4 2, -2 -3, -2 -5))"));
	}

	@Test
	public void polygon2_y1() {
		SplitResult splitResult = new XYSplitter(REF_Y_1).apply(POLYGON_2);
		Geometry merged = new XYMerger(REF_Y_1).apply(splitResult.lt, splitResult.gt);
		checkThat(merged).isEqualsTopo(wkt("POLYGON ((7.6 6, 9 7, 8.3 6, 2 -3, 10 6, 0 -10, -10 -4, -7 4, -7 -3, -2 2, -5 -3, 7.6 6), (-2 -3, 4 2, -2 -5, -2 -3), (-5 -4, -3 -6, -6 -5, -5 -4))"));
	}

	@Test
	public void polygon2_y2() {
		SplitResult splitResult = new XYSplitter(REF_Y_2).apply(POLYGON_2);
		Geometry merged = new XYMerger(REF_Y_2).apply(splitResult.lt, splitResult.gt);
		checkThat(merged).isEqualsTopo(wkt("POLYGON ((-9.625 -3, -7 4, -7 -3, -2 2, -5 -3, 9 7, 2 -3, 10 6, 4.375 -3, 0 -10, -10 -4, -9.625 -3), (-5 -4, -3 -6, -6 -5, -5 -4), (-2 -3, -2 -5, -0.2857142857142858 -3, 4 2, -2 -3))"));
	}
}
