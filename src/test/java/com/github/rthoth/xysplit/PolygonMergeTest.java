package com.github.rthoth.xysplit;

import static com.github.rthoth.xysplit.Assertions.*;

import org.junit.Test;
import org.locationtech.jts.geom.Geometry;


public class PolygonMergeTest extends PolygonTest {

	@Test
	public void polygon1_x1() {
		SplitResult result = new XYSplitter(REF_X_1).apply(POLYGON_1);
		Geometry merged = new XYMerger(REF_X_1).apply(result.lt, result.gt);
		checkThat(merged).isEqualsTopo(wkt("POLYGON ((-4 5, -2 7, -3 8, 0 10, 4 9, 2 7, 2 6, 4 5, -4 5), (3 9, -1 9, -2 6, 3 9))"));
	}

	@Test
	public void polygon1_x2() {
		SplitResult result = new XYSplitter(REF_X_2).apply(POLYGON_1);
		Geometry merged = new XYMerger(REF_X_2).apply(result.lt, result.gt);
		checkThat(merged).isEqualsTopo(wkt("POLYGON ((-4 5, -2 7, -3 8, 0 10, 4 9, 2 7, 2 6, 4 5, -4 5), (-2 6, 3 9, -1 9, -2 6))"));
	}

	@Test
	public void polygon1_y1() {
		SplitResult result = new XYSplitter(REF_Y_1).apply(POLYGON_1);
		Geometry merged = new XYMerger(REF_Y_1).apply(result.lt, result.gt);
		checkThat(merged).isEqualsTopo(POLYGON_1);
	}

	@Test
	public void polygon1_y2() {
		SplitResult result = new XYSplitter(REF_Y_2).apply(POLYGON_1);
		Geometry merged = new XYMerger(REF_Y_2).apply(result.lt, result.gt);
		checkThat(merged).isEqualsTopo(POLYGON_1);
	}

	@Test
	public void polygon2_x1() {
		SplitResult result = new XYSplitter(REF_X_1).apply(POLYGON_2);
		Geometry merged = new XYMerger(REF_X_1).apply(result.lt, result.gt);
		checkThat(merged).isEqualsTopo(wkt("POLYGON ((0 -10, -10 -4, -7 4, -7 -3, -2 2, -5 -3, 9 7, 2 -3, 10 6, 0 -10), (4 2, -2 -3, -2 -5, 4 2), (-5 -4, -3 -6, -6 -5, -5 -4))"));
	}

	@Test
	public void polygon2_x2() {
		SplitResult result = new XYSplitter(REF_X_2).apply(POLYGON_2);
		Geometry merged = new XYMerger(REF_X_2).apply(result.lt, result.gt);
		checkThat(merged).isEqualsTopo(wkt("POLYGON ((-10 -4, -7 4, -7 -3, -2 2, -5 -3, 9 7, 2 -3, 10 6, 0 -10, -10 -4), (-2 -5, 4 2, -2 -3, -2 -5), (-5 -4, -3 -6, -6 -5, -5 -4))"));
	}

	@Test
	public void polygon2_y1() {
		SplitResult result = new XYSplitter(REF_Y_1).apply(POLYGON_2);
		Geometry merged = new XYMerger(REF_Y_1).apply(result.lt, result.gt);
		checkThat(merged).isEqualsTopo(wkt("POLYGON ((9 7, 2 -3, 10 6, 0 -10, -10 -4, -7 4, -7 -3, -2 2, -5 -3, 9 7), (-2 -3, 4 2, -2 -5, -2 -3), (-5 -4, -3 -6, -6 -5, -5 -4))"));
	}

	@Test
	public void polygon2_y2() {
		SplitResult result = new XYSplitter(REF_Y_2).apply(POLYGON_2);
		Geometry merged = new XYMerger(REF_Y_2).apply(result.lt, result.gt);
		checkThat(merged).isEqualsTopo(wkt("POLYGON ((-7 4, -7 -3, -2 2, -5 -3, 9 7, 2 -3, 10 6, 0 -10, -10 -4, -7 4), (-2 -3, -2 -5, 4 2, -2 -3), (-5 -4, -3 -6, -6 -5, -5 -4))"));
	}

	@Test
	public void polygon3_x() {
		SplitResult result = new XYSplitter(Reference.x(0D)).apply(POLYGON_3);
		Geometry merged = new XYMerger(Reference.x(0D)).apply(result.lt, result.gt);
		checkThat(merged).isEqualsTopo(POLYGON_3);
	}

	@Test
	public void polygon3_y() {
		SplitResult result = new XYSplitter(Reference.y(0D)).apply(POLYGON_3);
		Geometry merged = new XYMerger(Reference.y(0D)).apply(result.lt, result.gt);
		checkThat(merged).isEqualsTopo(POLYGON_3);
	}
}
