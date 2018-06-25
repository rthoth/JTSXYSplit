package com.github.rthoth.xysplit;

import static com.github.rthoth.xysplit.Location.IN;
import static com.github.rthoth.xysplit.Location.OUT;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;

public class SplitSequenceTest extends GeometryTest {

	public static final CoordinateSequence sample01 = coordinateSequence(-6,-7, 4,-9, 0,-7, 0,-5, -2,-3, 3,-2, -2,0, 3,2, -2,3, 0,5, 0,7, 4,9, -6,7, -6,-7);

	public static final CoordinateSequence sample02 = coordinateSequence(50,-42.3,  48.1,-42.,  44.4,-48.,  40.7,-42.,  33.3,-42.,  29.6,-48.,  33.3,-55.,  25.9,-55.,  22.2,-61.,  25.9,-67.,  33.3,-67.,  29.6,-74.,  33.3,-80.,  25.9,-80.,  22.2,-87.,  18.5,-80.,  11.1,-80.,  7.4,-87.,  11.1,-93.,  3.7,-93.,  0,-10,  0,15.,  50,15.,  50,-42.3);

	@Test()
	public void sample01XLTSplit() {

		SplitSequence sequence = new SplitSequence.Poly(new Reference(XY.X, 0D), sample01);

		assertThat(sequence.get(Side.LT))
		.containsExactly(
				new SplitEvent(1, OUT, new Coordinate(0, -8.2)),
				new SplitEvent(4, IN, new Coordinate(0, -5)),
				new SplitEvent(5, OUT, new Coordinate(0, -2.6)),
				new SplitEvent(6, IN, new Coordinate(0, -.8)),
				new SplitEvent(7, OUT, new Coordinate(0, .8)),
				new SplitEvent(8, IN, new Coordinate(0, 2.6)),
				new SplitEvent(9, OUT, null),
				new SplitEvent(12, IN, new Coordinate(0, 8.2))
				);
	}

	@Test()
	public void sample01XGTSplit() {

		SplitSequence sequence = new SplitSequence.Poly(new Reference(XY.X, 0D), sample01);

		assertThat(sequence.get(Side.GT))
		.containsExactly(
				new SplitEvent(1, IN, new Coordinate(0, -8.2)),
				new SplitEvent(2, OUT, null),
				new SplitEvent(5, IN, new Coordinate(0, -2.6)),
				new SplitEvent(6, OUT, new Coordinate(0, -.8)),
				new SplitEvent(7, IN, new Coordinate(0, .8)),
				new SplitEvent(8, OUT, new Coordinate(0, 2.6)),
				new SplitEvent(11, IN, new Coordinate(0, 7)),
				new SplitEvent(12, OUT, new Coordinate(0, 8.2))
				);
	}

	@Test()
	public void sample01YLTSplit() {
		SplitSequence sequence = new SplitSequence.Poly(new Reference(XY.Y, 0D), sample01);

		assertThat(sequence.get(Side.LT))
			.containsExactly(
					new SplitEvent(6, OUT, null),
					new SplitEvent(13, IN, new Coordinate(-6, 0))
					);
	}

	@Test()
	public void sample01YGTSplit() {
		SplitSequence sequence = new SplitSequence.Poly(new Reference(XY.Y, 0D), sample01);

		assertThat(sequence.get(Side.GT))
			.containsExactly(
					new SplitEvent(7, IN, new Coordinate(-2, 0)),
					new SplitEvent(13, OUT, new Coordinate(-6, 0))
					);
	}

	@Test()
	public void error_01() {
		SplitSequence sequence = new SplitSequence.Poly(new Reference(XY.Y, -42.264973081037425), sample02);
	}
}
