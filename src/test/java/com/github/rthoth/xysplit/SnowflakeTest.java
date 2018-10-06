package com.github.rthoth.xysplit;

import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.operation.overlay.OverlayOp;
import org.locationtech.jts.shape.fractal.KochSnowflakeBuilder;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SnowflakeTest {

	public static Geometry regular(Envelope envelope, int points) {
		KochSnowflakeBuilder builder = new KochSnowflakeBuilder(GeometryTest.FACTORY);
		builder.setExtent(envelope);
		builder.setNumPoints(points * 3);

		return builder.getGeometry();
	}

	public final Geometry SMALL_1 = regular(new Envelope(-100, 100, -100, 100), 1500);
	public final Geometry SMALL_2 = regular(new Envelope(-50, 150, -50, 150), 1000);

	public final Geometry SNOWFLAKE_1 = regular(new Envelope(-100, 100, -100, 100), (int) 2e6);
	public final Geometry HALF_SNOWFLAKE_1 = regular(new Envelope(-50, 150, -50, 150), (int) 2e6);

	public final int DEEP = Math.max(Runtime.getRuntime().availableProcessors(), 4);

	private final Parallel.Strategy strategy = Parallel.level(DEEP);

	@Ignore
	public void _00_azt() throws ExecutionException, InterruptedException {
		Future<Geometry> result = Parallel.intersection(SMALL_1, SMALL_2, strategy);
		assertThat(result.get().getArea()).isCloseTo(0, offset(0.1));
	}

	@Ignore
	public void _01_simple_intersection() {
		Geometry result = new OverlayOp(SNOWFLAKE_1, HALF_SNOWFLAKE_1).getResultGeometry(OverlayOp.INTERSECTION);
		assertThat(result.getArea()).isCloseTo(14180.139445546563, offset(1e-7));
	}

	@Test
	public void _02_split_intersection() throws ExecutionException, InterruptedException {
		Future<Geometry> future = Parallel.intersection(SNOWFLAKE_1, HALF_SNOWFLAKE_1, strategy);
		Geometry geometry = future.get();
		assertThat(geometry.getArea()).isCloseTo(14180.139445546563, offset(1e-7));
	}

	@Ignore
	public void _03_simple_union() {
		Geometry result = new OverlayOp(SNOWFLAKE_1, HALF_SNOWFLAKE_1).getResultGeometry(OverlayOp.UNION);
		assertThat(result.getArea()).isCloseTo(41239.23586882192, offset(1e-7));
	}

	@Test
	public void _04_split_union() throws ExecutionException, InterruptedException {
		Future<Geometry> future = Parallel.union(SNOWFLAKE_1, HALF_SNOWFLAKE_1, strategy);
		assertThat(future.get().getArea()).isCloseTo(41239.23586882192, offset(1e-7));
	}

	@Ignore
	public void _05_simple_difference() {
		Geometry result = new OverlayOp(SNOWFLAKE_1, HALF_SNOWFLAKE_1).getResultGeometry(OverlayOp.DIFFERENCE);
		assertThat(result.getArea()).isCloseTo(13529.548211646685, offset(1e-7));
	}

	@Test
	public void _05_split_difference() throws Exception {
		Future<Geometry> future = Parallel.difference(SNOWFLAKE_1, HALF_SNOWFLAKE_1, strategy);
		assertThat(future.get().getArea()).isCloseTo(13529.548211646685, offset(1e-7));
	}
}


