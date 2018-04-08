package com.github.rthoth.xysplit;

import static com.github.rthoth.xysplit.Location.IN;
import static com.github.rthoth.xysplit.Location.OUT;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;

public class EventSequenceTest extends GeometryTest {

	public static final Coordinates sample01 = new Coordinates(coordinateSequence(-6,-7, 4,-9, 0,-7, 0,-5, -2,-3, 3,-2, -2,0, 3,2, -2,3, 0,5, 0,7, 4,9, -6,7, -6,-7));

	@Test()
	public void sample01XLTSplit() {

		EventSequence sequence = new EventSequence.Polygon(new Reference(XY.X, 0D), sample01);

		assertThat(sequence.get(Side.LT))
		.containsExactly(
				new Event(1, OUT, new Coordinate(0, -8.2)),
				new Event(4, IN, new Coordinate(0, -5)),
				new Event(5, OUT, new Coordinate(0, -2.6)),
				new Event(6, IN, new Coordinate(0, -.8)),
				new Event(7, OUT, new Coordinate(0, .8)),
				new Event(8, IN, new Coordinate(0, 2.6)),
				new Event(9, OUT, null),
				new Event(12, IN, new Coordinate(0, 8.2))
				);
	}

	@Test()
	public void sample01XGTSplit() {

		EventSequence sequence = new EventSequence.Polygon(new Reference(XY.X, 0D), sample01);

		assertThat(sequence.get(Side.GT))
		.containsExactly(
				new Event(1, IN, new Coordinate(0, -8.2)),
				new Event(2, OUT, null),
				new Event(5, IN, new Coordinate(0, -2.6)),
				new Event(6, OUT, new Coordinate(0, -.8)),
				new Event(7, IN, new Coordinate(0, .8)),
				new Event(8, OUT, new Coordinate(0, 2.6)),
				new Event(11, IN, new Coordinate(0, 7)),
				new Event(12, OUT, new Coordinate(0, 8.2))
				);
	}

	@Test()
	public void sample01YLTSplit() {
		EventSequence sequence = new EventSequence.Polygon(new Reference(XY.Y, 0D), sample01);

		assertThat(sequence.get(Side.LT))
			.containsExactly(
					new Event(6, OUT, null),
					new Event(13, IN, new Coordinate(-6, 0))
					);
	}

	@Test()
	public void sample01YGTSplit() {
		EventSequence sequence = new EventSequence.Polygon(new Reference(XY.Y, 0D), sample01);

		assertThat(sequence.get(Side.GT))
			.containsExactly(
					new Event(7, IN, new Coordinate(-2, 0)),
					new Event(13, OUT, new Coordinate(-6, 0))
					);
	}
}