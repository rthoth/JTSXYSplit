package com.github.rthoth.xysplit;

import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;

import java.util.List;

import static com.github.rthoth.xysplit.Assertions.assertThat;

public class SegmentedCoordinateSequenceTest extends GeometryTest {

	@Test
	public void error_01() {
		CoordinateSequence sequence = new CoordinateSequenceBuilder()
						.addRing(PolygonSplitTest.SHELL_1, 5, 8, true)
						.addRing(PolygonSplitTest.SHELL_1, 1, 4, true)
						.add(new Coordinate(-4, -5))
						.build();

		assertThat(sequence.toString())
						.isEqualTo("(-4,5 -2,7 -3,8 0,10 4,9 2,7 2,6 4,5 -4,-5)");
	}

	@Test
	public void error_02() {
		SplitResult result = new XYSplitter(PolygonSplitTest.REF_Y_2)
						.apply(PolygonSplitTest.POLYGON_2);

		List<CoordinateSequence> lt = extractShell(result.lt);

		assertThat(lt.get(0).toString())
						.isEqualTo("(4.4,-3 0,-10 -10,-4 -9.6,-3 -2,-3 -2,-5 -0.3,-3 4.4,-3)");
	}
}
