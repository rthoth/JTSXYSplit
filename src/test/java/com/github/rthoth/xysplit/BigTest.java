package com.github.rthoth.xysplit;

import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.operation.union.CascadedPolygonUnion;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.github.rthoth.xysplit.Assertions.assertThat;
import static com.github.rthoth.xysplit.Assertions.offset;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BigTest extends GeometryTest implements Download {

	@Test
	public void union_01() throws ExecutionException, InterruptedException {
		List<Geometry> geometries = wktz(download("https://dl.bintray.com/rthoth/generic/jtsxyplit_tests/theme_36.gz", "theme_36.gz"));
		Geometry result = Parallel.union(unique(geometries), Parallel.level(8)).get();
		assertThat(result.getArea()).isCloseTo(8.252612058830684E-4, offset(1e-8));
	}

	@Ignore
	public void union_01_simple() {
		List<Geometry> geometries = wktz(download("https://dl.bintray.com/rthoth/generic/jtsxyplit_tests/theme_36.gz", "theme_36.gz"));
		Geometry result = new CascadedPolygonUnion(unique(geometries)).union();
		assertThat(result.getArea()).isCloseTo(8.252612058830684E-4, offset(1e-8));
	}

	@Test
	public void union_02() throws ExecutionException, InterruptedException {
		List<Geometry> geometries = wktz(download("https://dl.bintray.com/rthoth/generic/jtsxyplit_tests/theme_44.gz", "theme_44.gz"));
		Geometry result = Parallel.union(geometries, Parallel.level(8)).get();
		assertThat(result.getArea()).isCloseTo(0.0013266762939079448, offset(1e-8));
	}

	@Ignore
	public void union_02_simple() {
		List<Geometry> geometries = wktz(download("https://dl.bintray.com/rthoth/generic/jtsxyplit_tests/theme_44.gz", "theme_44.gz"));
		Geometry result = new CascadedPolygonUnion(unique(geometries)).union();
		assertThat(result.getArea()).isCloseTo(0.0013266762939079448, offset(1e-8));
	}

	@Test
	public void union_03() throws ExecutionException, InterruptedException {
		List<Geometry> geometries = wktz(download("https://dl.bintray.com/rthoth/generic/jtsxyplit_tests/geometries.gz", "selected_geometries.gz"));
		Geometry result = Parallel.union(unique(geometries), Parallel.level(8)).get();
		assertThat(result.getArea()).isCloseTo(5.056440000782123E-5, offset(1e-8));
	}

	@Ignore
	public void union_03_simple() {
		List<Geometry> geometries = wktz(download("https://dl.bintray.com/rthoth/generic/jtsxyplit_tests/geometries.gz", "selected_geometries.gz"));
		Geometry result = new CascadedPolygonUnion(unique(geometries)).union();
		assertThat(result.getArea()).isCloseTo(5.056440000782123E-5, offset(1e-8));
	}
}
