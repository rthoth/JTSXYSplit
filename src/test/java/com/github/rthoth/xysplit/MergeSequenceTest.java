package com.github.rthoth.xysplit;

import org.junit.Test;
import org.locationtech.jts.geom.CoordinateSequence;

import java.util.List;

import static com.github.rthoth.xysplit.Location.IN;
import static com.github.rthoth.xysplit.Location.OUT;
import static com.github.rthoth.xysplit.Assertions.*;
import static com.github.rthoth.xysplit.PolygonTest.*;

public class MergeSequenceTest extends GeometryTest {

	@Test
	public void polygon1_x1() {
		SplitResult r = new PolygonSplitter(REF_X_1).apply(POLYGON_1);

		List<CoordinateSequence> lt = extractShell(r.lt);
		List<CoordinateSequence> gt = extractShell(r.gt);

		MergeSequence.Poly eventSequence = new MergeSequence.Poly(REF_X_1, MergeSequence.DEFAULT_OFFSET, lt, gt);

		assertThat(eventSequence.getLTNodes())
						.containsExactly(
										new MergeEvent.Node(0, IN, 5, lt.get(0)),
										new MergeEvent.Node(9, OUT, 8.4, lt.get(0)),
										new MergeEvent.Node(6, IN, 9, lt.get(0)),
										new MergeEvent.Node(5, OUT, 9.5, lt.get(0))
						);

		assertThat(eventSequence.getGTNodes())
						.containsExactly(
										new MergeEvent.Node(2, OUT, 5, gt.get(1)),
										new MergeEvent.Node(0, IN, 6, gt.get(1)),
										new MergeEvent.Node(2, OUT, 7, gt.get(0)),
										new MergeEvent.Node(3, IN, 8.4, gt.get(0)),
										new MergeEvent.Node(5, OUT, 9, gt.get(0)),
										new MergeEvent.Node(0, IN, 9.5, gt.get(0))
						);
	}

	@Test
	public void polygon1_y1() {
		SplitResult result = new XYSplitter(REF_Y_1).apply(POLYGON_1);

		List<CoordinateSequence> lt = extractShell(result.lt);
		List<CoordinateSequence> gt = extractShell(result.gt);

		MergeSequence mergeSequence = new MergeSequence.Poly(REF_Y_1, 1e-8, lt, gt);

		assertThat(mergeSequence.getLTNodes())
						.containsExactly(
										new MergeEvent.Node(3, OUT, -3, lt.get(0)),
										new MergeEvent.Node(0, IN, 2, lt.get(0))
						);

		assertThat(mergeSequence.getGTNodes())
						.containsExactly(
										new MergeEvent.Node(0, IN, -3, gt.get(0)),
										new MergeEvent.Node(6, OUT, 2, gt.get(0))
						);
	}

	@Test
	public void polygon2_y2() {
		SplitResult result = new XYSplitter(REF_Y_2).apply(POLYGON_2);

		List<CoordinateSequence> lt = extractShell(result.lt);
		List<CoordinateSequence> gt = extractShell(result.gt);

		MergeSequence eventSequence = new MergeSequence.Poly(REF_Y_2, MergeSequence.DEFAULT_OFFSET, lt, gt);

		assertThat(eventSequence.getLTNodes())
						.containsExactly(
										new MergeEvent.Node(3, OUT, -9.625, lt.get(0)),
										new MergeEvent.Node(4, IN, -2, lt.get(0)),
										new MergeEvent.Node(6, OUT, -0.2857142857142858, lt.get(0)),
										new MergeEvent.Node(0, IN, 4.375, lt.get(0))
						);

		assertThat(eventSequence.getGTNodes())
						.containsExactly(
										new MergeEvent.Node(0, IN, -9.625, gt.get(0)),
										new MergeEvent.Node(11, OUT, -2, gt.get(0)),
										new MergeEvent.Node(9, IN, -0.2857142857142858, gt.get(0)),
										new MergeEvent.Node(8, OUT, 4.375, gt.get(0))
						);
	}

	@Test
	public void polygon2_x2() {
		SplitResult result = new XYSplitter(REF_X_2).apply(POLYGON_2);
		List<CoordinateSequence> lt = extractShell(result.lt);
		List<CoordinateSequence> gt = extractShell(result.gt);

		MergeSequence.Poly eventSequence = new MergeSequence.Poly(REF_X_2, lt, gt);

		assertThat(eventSequence.getLTNodes())
						.containsExactly(
										new MergeEvent.Node(0, IN, -8.8, lt.get(0)),
										new MergeEvent.Node(6, OUT, -0.8571428571428568, lt.get(0))
						);

		assertThat(eventSequence.getGTNodes())
						.containsExactly(
										new MergeEvent.Node(5, OUT, -8.8, gt.get(0)),
										new MergeEvent.Node(6, IN, -5, gt.get(0)),
										new MergeEvent.Node(8, OUT, -3, gt.get(0)),
										new MergeEvent.Node(0, IN, -0.8571428571428568, gt.get(0))
						);
	}
}
