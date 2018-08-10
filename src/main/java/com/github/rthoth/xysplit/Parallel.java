package com.github.rthoth.xysplit;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.overlay.OverlayOp;
import org.locationtech.jts.operation.union.CascadedPolygonUnion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Parallel {

//	public static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
	public static final ExecutorService EXECUTOR = ForkJoinPool.commonPool();

	public static final XY DEFAULT_START = XY.X;

	public static Strategy level(int value) {
		return new Level(value);
	}

	public static Strategy threshold(int value) {
		return new Threshold(value);
	}

	private static final BiFunction<Geometry, Geometry, Geometry> INTERSECTION = (a, b) -> OverlayOp.overlayOp(a, b, OverlayOp.INTERSECTION);
	private static final BiFunction<Geometry, Geometry, Geometry> UNION = (a, b) -> OverlayOp.overlayOp(a, b, OverlayOp.UNION);
	private static final BiFunction<Geometry, Geometry, Geometry> DIFFERENCE = (a, b) -> OverlayOp.overlayOp(a, b, OverlayOp.DIFFERENCE);
	private static final Function<Collection<? extends Geometry>, Geometry> COLLECTION_UNION = geometries -> {
		Geometry ret = new CascadedPolygonUnion(geometries).union();
//		System.out.println(ret);
		return ret;
	};

	public static Future<Geometry> difference(Geometry g1, Geometry g2, Strategy strategy) {
		return parallel(DIFFERENCE, g1, g2, DEFAULT_START, strategy);
	}

	public static Future<Geometry> difference(Geometry g1, Geometry g2, XY xy, Strategy strategy) {
		return parallel(DIFFERENCE, g1, g2, xy, strategy);
	}

	public static Future<Geometry> intersection(Geometry g1, Geometry g2, Strategy strategy) {
		return parallel(INTERSECTION, g1, g2, DEFAULT_START, strategy);
	}

	public static Future<Geometry> intersection(Geometry g1, Geometry g2, XY xy, Strategy strategy) {
		return parallel(INTERSECTION, g1, g2, xy, strategy);
	}

	public static Future<Geometry> union(Geometry g1, Geometry g2, Strategy strategy) {
		return parallel(UNION, g1, g2, DEFAULT_START, strategy);
	}

	public static Future<Geometry> union(Geometry g1, Geometry g2, XY xy, Strategy strategy) {
		return parallel(UNION, g1, g2, xy, strategy);
	}

	public static <G extends Geometry> Future<Geometry> union(final Collection<G> geometries, final Strategy strategy) {
		return parallel(COLLECTION_UNION, geometries, DEFAULT_START, strategy);
	}

	public static CompletableFuture<Geometry> parallel(final BiFunction<Geometry, Geometry, Geometry> function, final Geometry g1, final Geometry g2, final XY xy, final Strategy strategy) {
		if (strategy.apply(g1, g2)) {
			return CompletableFuture.supplyAsync(() -> function.apply(g1, g2), EXECUTOR);
		} else {

			final Envelope env = new Envelope(g1.getEnvelopeInternal());
			env.expandToInclude(g2.getEnvelopeInternal());

			final double half = (xy == XY.X ? env.getMinX() + env.getMaxX() : env.getMinY() + env.getMaxY()) / 2;
			final XYSplitter splitter = new XYSplitter(new Reference(xy, half));

			CompletableFuture<SplitResult> fSplit1 = CompletableFuture.supplyAsync(() -> splitter.apply(g1), EXECUTOR);
			CompletableFuture<SplitResult> fSplit2 = CompletableFuture.supplyAsync(() -> splitter.apply(g2), EXECUTOR);

			final XY inverted = xy.invert();
			final Strategy nextStrategy = strategy.next();

			CompletableFuture<Geometry> lt = fSplit1
							.thenCombineAsync(fSplit2, (s1, s2) -> T2.of(s1.lt, s2.lt), EXECUTOR)
							.thenComposeAsync(t2 -> parallel(function, t2._1, t2._2, inverted, nextStrategy));

			CompletableFuture<Geometry> gt = fSplit1
							.thenCombineAsync(fSplit2, (s1, s2) -> T2.of(s1.gt, s2.gt), EXECUTOR)
							.thenComposeAsync(t2 -> parallel(function, t2._1, t2._2, inverted, nextStrategy));

//			lt.thenCombineAsync(gt, (ltG, gtG) -> {
//				System.out.println(ltG);
//				System.out.println(gtG);
//
//				return null;
//			}, EXECUTOR);

			return lt.thenCombineAsync(gt, (gLT, gGT) -> splitter.merger().apply(gLT, gGT), EXECUTOR);
		}
	}

	private static <G extends Geometry> CompletableFuture<Geometry> parallel(final Function<Collection<? extends Geometry>, Geometry> function, Collection<G> geometries, XY xy, Strategy strategy) {
		if (strategy.apply(geometries)) {
			return CompletableFuture.supplyAsync(() -> {
				return function.apply(geometries.stream().filter(g -> !g.isEmpty()).collect(Collectors.toList()));
			});
		} else {
			Iterator<G> iterator = geometries.iterator();

			if (iterator.hasNext()) {
				Envelope env = new Envelope(iterator.next().getEnvelopeInternal());

				while (iterator.hasNext()) {
					env.expandToInclude(iterator.next().getEnvelopeInternal());
				}

				final double half = ((xy == XY.X) ? env.getMaxX() + env.getMinX() : env.getMaxY() + env.getMinY()) / 2;
				final Strategy next = strategy.next();
				final Reference reference = new Reference(xy, half);
				final XYSplitter splitter = new XYSplitter(reference);

				CompletableFuture<T2<ArrayList<Geometry>, ArrayList<Geometry>>> splitStage = CompletableFuture.supplyAsync(() -> {
					ArrayList<Geometry> lt = new ArrayList<>(geometries.size());
					ArrayList<Geometry> gt = new ArrayList<>(geometries.size());

					for (G geometry : geometries) {
						SplitResult result = splitter.apply(geometry);
						lt.add(result.lt);
						gt.add(result.gt);
					}

					return T2.of(lt, gt);
				});

				final XY inverted = xy.invert();

				CompletableFuture<Geometry> ltStage = splitStage.thenComposeAsync(t2 -> parallel(function, t2._1, inverted, next), EXECUTOR);
				CompletableFuture<Geometry> gtStage = splitStage.thenComposeAsync(t2 -> parallel(function, t2._2, inverted, next), EXECUTOR);

				return ltStage.thenCombineAsync(gtStage, (lt, gt) -> new XYMerger(reference).apply(lt, gt), EXECUTOR);

			} else {
				return CompletableFuture.completedFuture(null);
			}
		}
	}

	interface Strategy {

		boolean shouldCompute(Geometry g1, Geometry g2);

		default boolean apply(Geometry g1, Geometry g2) {
			if (!g1.isEmpty() || !g2.isEmpty()) {
				return shouldCompute(g1, g2);
			} else {
				return true;
			}
		}

		Strategy next();

		<G extends Geometry> boolean apply(Collection<G> geometries);
	}

	public static class Level implements Strategy {

		private final int value;

		Level(int value) {
			this.value = value;
		}

		@Override
		public boolean shouldCompute(Geometry g1, Geometry g2) {
//			System.out.println(g1.getNumPoints() + g2.getNumPoints());
			return value == 0;
		}

		@Override
		public Strategy next() {
//			System.out.println(value);
			return new Level(value - 1);
		}

		@Override
		public <G extends Geometry> boolean apply(Collection<G> geometries) {
			return value == 0;
		}
	}

	public static class Threshold implements Strategy {

		private final int value;

		Threshold(int value) {
			this.value = Math.abs(value);
		}

		@Override
		public boolean shouldCompute(Geometry g1, Geometry g2) {
			return (g1.getNumPoints() + g2.getNumPoints()) <= value;
		}

		@Override
		public Strategy next() {
			return this;
		}

		@Override
		public <G extends Geometry> boolean apply(Collection<G> geometries) {
			return geometries.stream().mapToInt(Geometry::getNumPoints).sum() <= value;
		}
	}
}
