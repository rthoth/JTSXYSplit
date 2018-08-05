package com.github.rthoth.xysplit;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.operation.overlay.OverlayOp;

import java.util.concurrent.*;
import java.util.function.BiFunction;

public class Parallel {

	public static final ExecutorService EXECUTOR = ForkJoinPool.commonPool();

	public static final XY DEFAULT_START = XY.X;

	public static Strategy level(int value) {

		return new Level(value);
	}

		private static final BiFunction<Geometry, Geometry, Geometry> INTERSECTION = (a, b) -> OverlayOp.overlayOp(a, b, OverlayOp.INTERSECTION);

	private static final BiFunction<Geometry, Geometry, Geometry> UNION = (a, b) -> OverlayOp.overlayOp(a, b, OverlayOp.UNION);

	private static final BiFunction<Geometry, Geometry, Geometry> DIFFERENCE = (a, b) -> OverlayOp.overlayOp(a, b, OverlayOp.DIFFERENCE);

	public static Future<Geometry> difference(Geometry g1, Geometry g2, Strategy strategy) {
		return difference(g1, g2, XY.X, strategy);
	}

	public static Future<Geometry> difference(Geometry g1, Geometry g2, XY xy, Strategy strategy) {
		return parallel(DIFFERENCE, g1, g2, xy, strategy);
	}

	public static Future<Geometry> intersection(Geometry g1, Geometry g2, Strategy strategy) {
		return intersection(g1, g2, DEFAULT_START, strategy);
	}

	public static Future<Geometry> intersection(Geometry g1, Geometry g2, XY xy, Strategy strategy) {
		return parallel(INTERSECTION, g1, g2, xy, strategy);
	}

	public static Future<Geometry> union(Geometry g1, Geometry g2, Strategy strategy) {
		return union(g1, g2, DEFAULT_START, strategy);
	}

	public static Future<Geometry> union(Geometry g1, Geometry g2, XY xy, Strategy strategy) {
		return parallel(UNION, g1, g2, xy, strategy);
	}

	public static CompletableFuture<Geometry> parallel(final BiFunction<Geometry, Geometry, Geometry> function, final Geometry g1, final Geometry g2, final XY xy, final Strategy strategy) {
		if (strategy.shouldSplit(g1, g2)) {
			return CompletableFuture.supplyAsync(() -> function.apply(g1, g2), EXECUTOR);
		} else {

			Envelope env = new Envelope(g1.getEnvelopeInternal());
			env.expandToInclude(g1.getEnvelopeInternal());

			final double half = (xy == XY.X ? env.getMinX() + env.getMaxX() : env.getMinY() + env.getMaxY()) / 2;
			final Reference reference = new Reference(xy, half);
			final XYSplitter splitter = new XYSplitter(reference);

			CompletableFuture<SplitResult> fSplit1 = CompletableFuture.supplyAsync(() -> splitter.apply(g1), EXECUTOR);
			CompletableFuture<SplitResult> fSplit2 = CompletableFuture.supplyAsync(() -> splitter.apply(g2), EXECUTOR);

			final XY inverted = xy.invert();
			final Strategy nextStrategy = strategy.next();

			CompletableFuture<Geometry> lt = fSplit1
							.thenCombineAsync(fSplit2, (s1, s2) -> new GeometryPair(s1.lt, s2.lt), EXECUTOR)
							.thenComposeAsync(pair -> parallel(function, pair._1, pair._2, inverted, nextStrategy));

			CompletableFuture<Geometry> gt = fSplit1
							.thenCombineAsync(fSplit2, (s1, s2) -> new GeometryPair(s1.gt, s2.gt), EXECUTOR)
							.thenComposeAsync(pair -> parallel(function, pair._1, pair._2, inverted, nextStrategy));

			return lt.thenCombineAsync(gt, (gLT, gGT) -> new XYMerger(reference).apply(gLT, gGT), EXECUTOR);
		}
	}
	public static class GeometryPair {


		public final Geometry _1;

		public final Geometry _2;

		public GeometryPair(Geometry _1, Geometry _2) {
			this._1 = _1;
			this._2 = _2;
		}

	}
	interface Strategy {


		boolean shouldSplit(Geometry g1, Geometry g2);

		Strategy next();

	}
	public static class Level implements Strategy {

		private final int value;

		public Level(int value) {
			this.value = value;
		}

		@Override
		public boolean shouldSplit(Geometry g1, Geometry g2) {
			return value == 0;
		}

		@Override
		public Strategy next() {
			return new Level(value - 1);
		}
	}
}
