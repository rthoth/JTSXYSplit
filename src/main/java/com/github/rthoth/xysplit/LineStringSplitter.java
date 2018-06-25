package com.github.rthoth.xysplit;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.function.Function;
import org.locationtech.jts.geom.*;

/** A Line splitter. */
public class LineStringSplitter implements Function<LineString, SplitResult> {

   public final Reference reference;

	public LineStringSplitter(Reference reference) {
		this.reference = reference;
	}

	public SplitResult apply(LineString lineString) {
		SplitSequence sequence =
			new SplitSequence.Line(reference, lineString.getCoordinateSequence());

		Builder ltBuilder = new Builder(Side.LT, sequence, lineString),
			gtBuilder = new Builder(Side.GT, sequence, lineString);

		Geometry lt = ltBuilder.result, gt = gtBuilder.result;

		return new SplitResult(lt, gt, reference);
	}

	private class Builder {

		public final Geometry result;

		public Builder(Side side, SplitSequence sequence, LineString lineString) {
			LinkedList<SplitEvent> events = new LinkedList<>(sequence.get(side));
			GeometryFactory factory = lineString.getFactory();

			if (events.size() > 1) {
				CoordinateSequence coordinates = sequence.sequence;
				LinkedList<LineString> temporary = new LinkedList<>();
				if (events.peek().location != Location.IN) {
					events.addLast(events.poll());
				}

				while (!events.isEmpty()) {
					SplitEvent start = events.poll(), end = events.poll();
					ArrayList<Coordinate> buffer = new ArrayList<>(Math.abs(end.index - start.index) + 2);

					if (start.coordinate != null) {
						buffer.add(start.coordinate);
					}

					for (int i = start.index + 1; i < end.index; i++) {
						buffer.add(coordinates.getCoordinate(i));
					}

					if (end.coordinate != null) {
						buffer.add(end.coordinate);
					}

					temporary.add(factory.createLineString(buffer.toArray(new Coordinate[buffer.size()])));
				}

				result = (temporary.size() > 1)
					? factory.createMultiLineString(temporary.toArray(new LineString[temporary.size()]))
					: temporary.get(0);
			} else {
				result = factory.createLineString();
			}
		}
	}
}
