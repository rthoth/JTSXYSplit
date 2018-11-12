package com.github.rthoth.xysplit;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;

import java.util.*;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

import static com.github.rthoth.xysplit.InOnOut.IN;
import static com.github.rthoth.xysplit.InOnOut.ON;
import static com.github.rthoth.xysplit.InOnOut.OUT;
import static com.github.rthoth.xysplit.Side.GT;
import static com.github.rthoth.xysplit.Side.LT;
import static com.github.rthoth.xysplit.XY.X;

abstract class SplitSequencer implements Function<CoordinateSequence, SplitSequencer.Result> {

	private static final Result EMPTY = new Result(Collections.emptyList(), Collections.emptyList());

	public static class Result {

		public final List<SplitEvent> lt;
		public final List<SplitEvent> gt;

		Result(List<SplitEvent> lt, List<SplitEvent> gt) {
			this.lt = lt;
			this.gt = gt;
		}
	}

	private final XYDefinitions.OrdinateExtractor ordinatePosition;
	private final ToDoubleFunction<Coordinate> coordinatePosition;
	protected final double offset;
	protected final Reference reference;


	SplitSequencer(Reference reference, double offset) {
		this.reference = reference;
		this.offset = Math.abs(offset);
		coordinatePosition = reference.xy == X ? XYDefinitions.Y_ORDINATE_COORDINATE_EXTRACTOR : XYDefinitions.X_ORDINATE_COORDINATE_EXTRACTOR;
		ordinatePosition = reference.xy == X ? XYDefinitions.Y_ORDINATE_EXTRACTOR : XYDefinitions.X_ORDINATE_EXTRACTOR;
	}

	public abstract Result apply(CoordinateSequence sequence, int padding);

	public static class Line extends SplitSequencer {

		Line(Reference reference, double offset) {
			super(reference, offset);
		}

		@Override
		public Result apply(CoordinateSequence sequence) {
			return new LineSequencer(sequence).get();
		}

		@Override
		public Result apply(CoordinateSequence sequence, int padding) {
			return new LineSequencer(sequence).get(padding);
		}
	}

	public static class Poly extends SplitSequencer {

		Poly(Reference reference, double offset) {
			super(reference, offset);
		}

		@Override
		public Result apply(CoordinateSequence sequence) {
			return new PolySequencer(sequence).get();
		}

		@Override
		public Result apply(CoordinateSequence sequence, int padding) {
			return new PolySequencer(sequence).get(padding);
		}
	}

	private abstract class Sequencer {

		final CoordinateSequence sequence;

		Sequencer(CoordinateSequence sequence) {
			this.sequence = sequence;
		}

		Result get() {
			preStart();

			LinkedList<SplitEvent> lt = new LinkedList<>();
			LinkedList<SplitEvent> gt = new LinkedList<>();

			if (sequence.size() > 1) {
				Side last = reference.classify(sequence, 0, offset), current;

				for (int i = 1, l = sequence.size(); i < l; i++) {
					current = reference.classify(sequence, i, offset);

					if (current != last) {
						register(reference, i, current.inOnOut(LT), last.inOnOut(LT), lt);
						register(reference, i, current.inOnOut(GT), last.inOnOut(GT), gt);
						last = current;
					}
				}

				return new Result(cleanUp(lt), cleanUp(gt));
			} else {
				return EMPTY;
			}
		}

		Result get(int padding) {
			preStart();

			LinkedList<SplitEvent> lt = new LinkedList<>();
			LinkedList<SplitEvent> gt = new LinkedList<>();

			padding = Math.abs(padding);

			final Reference ltReference = reference.offset(padding * offset);
			final Reference gtReference = reference.offset(0 - padding * offset);

			if (sequence.size() > 1) {

				Side ltLast = ltReference.classify(sequence, 0, offset), ltCurrent;
				Side gtLast = gtReference.classify(sequence, 0, offset), gtCurrent;

				for (int i = 1, l = sequence.size(); i < l; i++) {
					ltCurrent = ltReference.classify(sequence, i, offset);
					gtCurrent = gtReference.classify(sequence, i, offset);

					if (ltCurrent != ltLast) {
						register(ltReference, i, ltCurrent.inOnOut(LT), ltLast.inOnOut(LT), lt);
						ltLast = ltCurrent;
					}

					if (gtCurrent != gtLast) {
						register(gtReference, i, ltCurrent.inOnOut(GT), ltLast.inOnOut(GT), gt);
						gtLast = gtCurrent;
					}
				}

				return new Result(cleanUp(lt), cleanUp(gt));
			} else {
				return EMPTY;
			}
		}

		protected abstract List<SplitEvent> cleanUp(LinkedList<SplitEvent> events);

		protected abstract void preStart();

		protected abstract void register(Reference reference, int index, InOnOut current, InOnOut last, Deque<SplitEvent> events);
	}

	private class LineSequencer extends Sequencer {

		LineSequencer(CoordinateSequence sequence) {
			super(sequence);
		}

		@Override
		protected List<SplitEvent> cleanUp(LinkedList<SplitEvent> events) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected void preStart() {
			assert sequence.size() > 1 || sequence.size() == 0 : "CoordinateSequence must have more than 1 point or to be empty!";
		}

		@Override
		protected void register(Reference reference, int index, InOnOut current, InOnOut last, Deque<SplitEvent> events) {
			throw new UnsupportedOperationException();
		}
	}

	private class PolySequencer extends Sequencer {

		PolySequencer(CoordinateSequence sequence) {
			super(sequence);
		}

		@Override
		protected List<SplitEvent> cleanUp(LinkedList<SplitEvent> events) {
			if (!events.isEmpty()) {
				SplitEvent first = events.peekFirst();
				SplitEvent last = events.peekLast();

				if (first != last) {
					if (first.index != 0 || last.index != sequence.size() - 1) {
						if (first.inOnOut == last.inOnOut) {
							if (first.inOnOut == OUT) {
								events.pollFirst();
							} else {
								events.pollLast();
							}
						}
					} else {
						if (first.inOnOut == last.inOnOut) {
							if (first.inOnOut == OUT)
								events.pollFirst();
							else
								events.pollLast();
						} else {
							events.pollLast();
							events.pollFirst();
							if (first.inOnOut == IN) {
								events.addFirst(new SplitEvent(0, ordinatePosition.apply(sequence, 0), ON, null, sequence));
							}
						}
					}
				}
			}

			return Collections.unmodifiableList(events);
		}

		@Override
		protected void preStart() {
			assert sequence.size() == 0 || sequence.size() > 3 : "CoordinateSequence must have more than 3 points or to be empty!";
		}

		@Override
		protected void register(Reference reference, int index, InOnOut current, InOnOut last, Deque<SplitEvent> events) {
			SplitEvent newEvent = null;

			if (current != ON && last != ON) {
				Coordinate coordinate = reference.intersection(sequence, index, index - 1);
				newEvent = new SplitEvent(index, coordinatePosition.applyAsDouble(coordinate), current, coordinate, sequence);
			} else if (current == ON) {
				newEvent = new SplitEvent(index, ordinatePosition.apply(sequence, index), last.invert(), null, sequence);
			} else {
				SplitEvent lastEvent = events.peekLast();
				int lastIndex = index - 1;

				if (lastEvent != null) {
					if (lastEvent.inOnOut != current) {
						if (current == IN) {
							if (lastEvent.index != lastIndex) {
								newEvent = new SplitEvent(lastIndex, ordinatePosition.apply(sequence, lastIndex), current, null, sequence);
							} else {
								events.pollLast();
								newEvent = new SplitEvent(lastIndex, ordinatePosition.apply(sequence, lastIndex), ON, null, sequence);
							}
						} else {
							events.pollLast();
						}
					} else if (current == IN) {
						events.pollLast();
						newEvent = new SplitEvent(lastIndex, ordinatePosition.apply(sequence, lastIndex), current, null, sequence);
					}
				} else {
					newEvent = new SplitEvent(lastIndex, ordinatePosition.apply(sequence, lastIndex), current, null, sequence);
				}
			}

			if (newEvent != null) {
				events.addLast(newEvent);
			}
		}
	}
}
