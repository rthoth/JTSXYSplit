package com.github.rthoth.xysplit;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.operation.overlay.OverlayOp;

import java.util.concurrent.*;
import java.util.function.BiFunction;

public class Parallel {

	public static class SplitStep {
		public final SplitResult _1;
		public final SplitResult _2;

		public SplitStep(SplitResult _1, SplitResult _2) {
			this._1 = _1;
			this._2 = _2;
		}
	}

	public static final ExecutorService EXECUTOR = ForkJoinPool.commonPool();

	private static final BiFunction<Geometry, Geometry, Geometry> INTERSECTION = (a, b) -> new OverlayOp(a, b).getResultGeometry(OverlayOp.INTERSECTION);

	private static final BiFunction<Geometry, Geometry, Geometry> UNION = (a, b) -> new OverlayOp(a, b).getResultGeometry(OverlayOp.UNION);

	public static Future<Geometry> intersection(Geometry g1, Geometry g2, int maxCoordinates) {
		return intersection(g1, g2, maxCoordinates, XY.X);
	}

	public static Future<Geometry> intersection(Geometry g1, Geometry g2, int maxCoordinates, XY xy) {
		return parallel(INTERSECTION, g1, g2, maxCoordinates, xy);
	}

	public static CompletableFuture<Geometry> parallel(final BiFunction<Geometry, Geometry, Geometry> function, final Geometry g1, final Geometry g2, final int maxCoordinates, final XY xy) {
		final int total = g1.getNumPoints() + g2.getNumPoints();
		if (total <= maxCoordinates) {
			return CompletableFuture.supplyAsync(() -> function.apply(g1, g2), EXECUTOR);
		} else {

			Envelope env = new Envelope(g1.getEnvelopeInternal());
			env.expandToInclude(g1.getEnvelopeInternal());

			final double half = (xy == XY.X ? env.getMinX() + env.getMaxX() : env.getMinY() + env.getMaxY()) / 2;
			final Reference reference = new Reference(xy, half);
			final XYSplitter splitter = new XYSplitter(reference);

			CompletableFuture<SplitResult> fSplit1 = CompletableFuture.supplyAsync(() -> splitter.apply(g1), EXECUTOR);
			CompletableFuture<SplitResult> fSplit2 = CompletableFuture.supplyAsync(() -> splitter.apply(g2), EXECUTOR);

			CompletableFuture<SplitStep> splitStep = fSplit1
							.thenCombineAsync(fSplit2, (s1, s2) -> new SplitStep(s1, s2), EXECUTOR);

			XY inverted = xy.invert();

			CompletableFuture<Geometry> lt = splitStep.thenComposeAsync(step -> parallel(function, step._1.lt, step._2.lt, maxCoordinates, inverted), EXECUTOR);
			CompletableFuture<Geometry> gt = splitStep.thenComposeAsync(step -> parallel(function, step._1.gt, step._2.gt, maxCoordinates, inverted), EXECUTOR);

			return lt.thenCombineAsync(gt, (gLT, gGT) -> new XYMerger(reference).apply(gLT, gGT), EXECUTOR);
		}
	}

	public static Future<Geometry> union(Geometry g1, Geometry g2, int maxCoordinates) {
		return union(g1, g2, maxCoordinates, XY.X);
	}

	public static Future<Geometry> union(Geometry g1, Geometry g2, int maxCoordinates, XY xy) {
		return parallel(UNION, g1, g2, maxCoordinates, xy);
	}
}
