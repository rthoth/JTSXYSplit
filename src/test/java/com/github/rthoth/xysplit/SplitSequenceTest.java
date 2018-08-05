package com.github.rthoth.xysplit;

import static com.github.rthoth.xysplit.Location.IN;
import static com.github.rthoth.xysplit.Location.OUT;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;

public class SplitSequenceTest extends GeometryTest {

	public static final CoordinateSequence sample01 = coordinateSequence(-6, -7, 4, -9, 0, -7, 0, -5, -2, -3, 3, -2, -2, 0, 3, 2, -2, 3, 0, 5, 0, 7, 4, 9, -6, 7, -6, -7);

	public static final CoordinateSequence sample02 = coordinateSequence(50, -42.3, 48.1, -42., 44.4, -48., 40.7, -42., 33.3, -42., 29.6, -48., 33.3, -55., 25.9, -55., 22.2, -61., 25.9, -67., 33.3, -67., 29.6, -74., 33.3, -80., 25.9, -80., 22.2, -87., 18.5, -80., 11.1, -80., 7.4, -87., 11.1, -93., 3.7, -93., 0, -10, 0, 15., 50, 15., 50, -42.3);

	@Test()
	public void sample01XLTSplit() {

		SplitSequence sequence = new SplitSequence.Poly(new Reference(XY.X, 0D), sample01);

		assertThat(sequence.get(Side.LT))
						.containsExactly(
										new SplitEvent(1, 0, OUT, new Coordinate(0, -8.2), null),
										new SplitEvent(4, 0, IN, new Coordinate(0, -5), null),
										new SplitEvent(5, 0, OUT, new Coordinate(0, -2.6), null),
										new SplitEvent(6, 0, IN, new Coordinate(0, -.8), null),
										new SplitEvent(7, 0, OUT, new Coordinate(0, .8), null),
										new SplitEvent(8, 0, IN, new Coordinate(0, 2.6), null),
										new SplitEvent(9, 0, OUT, null, null),
										new SplitEvent(12, 0, IN, new Coordinate(0, 8.2), null)
						);
	}

	@Test()
	public void sample01XGTSplit() {

		SplitSequence sequence = new SplitSequence.Poly(new Reference(XY.X, 0D), sample01);

		assertThat(sequence.get(Side.GT))
						.containsExactly(
										new SplitEvent(1, 0, IN, new Coordinate(0, -8.2), null),
										new SplitEvent(2, 0, OUT, null, null),
										new SplitEvent(5, 0, IN, new Coordinate(0, -2.6), null),
										new SplitEvent(6, 0, OUT, new Coordinate(0, -.8), null),
										new SplitEvent(7, 0, IN, new Coordinate(0, .8), null),
										new SplitEvent(8, 0, OUT, new Coordinate(0, 2.6), null),
										new SplitEvent(11, 0, IN, new Coordinate(0, 7), null),
										new SplitEvent(12, 0, OUT, new Coordinate(0, 8.2), null)
						);
	}

	@Test()
	public void sample01YLTSplit() {
		SplitSequence sequence = new SplitSequence.Poly(new Reference(XY.Y, 0D), sample01);

		assertThat(sequence.get(Side.LT))
						.containsExactly(
										new SplitEvent(6, 0, OUT, null, null),
										new SplitEvent(13, 0, IN, new Coordinate(-6, 0), null)
						);
	}

	@Test()
	public void sample01YGTSplit() {
		SplitSequence sequence = new SplitSequence.Poly(new Reference(XY.Y, 0D), sample01);

		assertThat(sequence.get(Side.GT))
						.containsExactly(
										new SplitEvent(7, 0, IN, new Coordinate(-2, 0), null),
										new SplitEvent(13, 0, OUT, new Coordinate(-6, 0), null)
						);
	}

	@Test()
	public void error_01() {
		SplitSequence sequence = new SplitSequence.Poly(new Reference(XY.Y, -42.264973081037425), sample02);
	}
}
