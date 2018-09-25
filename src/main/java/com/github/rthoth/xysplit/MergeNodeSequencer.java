package com.github.rthoth.xysplit;

import org.locationtech.jts.geom.CoordinateSequence;

import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;

import static com.github.rthoth.xysplit.Location.IN;
import static com.github.rthoth.xysplit.Location.ON;
import static com.github.rthoth.xysplit.Location.OUT;

abstract class MergeNodeSequencer implements BiFunction<Side, CoordinateSequence, MergeNodeSequencer.Result> {

	public final double offset;
	public final Reference reference;

	protected final XYDefinitions.OrdinateExtractor extractor;

	public MergeNodeSequencer(Reference reference, double offset) {
		this.reference = reference;
		this.extractor = reference.xy == XY.X ? XYDefinitions.Y_ORDINATE_EXTRACTOR : XYDefinitions.X_ORDINATE_EXTRACTOR;
		this.offset = Math.abs(offset);
	}

	@Override
	public Result apply(Side side, CoordinateSequence sequence) {
		if (sequence.size() > 3) {
			LinkedList<MergeEvent.Node> nodes = new LinkedList<>();
			LinkedList<CoordinateSequence> circles = new LinkedList<>();
			Location last = reference.classify(sequence, 0, offset).location(side), current;

			for (int i = 1, l = sequence.size(); i < l; i++) {
				current = reference.classify(sequence, i, offset).location(side);

				if ((current == ON || last == ON) && !(current == ON && last == ON)) {
					register(i, current, last, nodes, sequence, circles);
					last = current;
				}
			}

			cleanUp(nodes);
			return new Result(Collections.unmodifiableList(nodes), Collections.unmodifiableList(circles));
		} else {
			return new Result(Collections.emptyList(), Collections.emptyList());
		}
	}

	protected abstract void cleanUp(Deque<MergeEvent.Node> events);

	protected abstract void register(int index, Location current, Location last, Deque<MergeEvent.Node> nodes, CoordinateSequence sequence, Deque<CoordinateSequence> circles);

	public static class Poly extends MergeNodeSequencer {

		public Poly(Reference reference, double offset) {
			super(reference, offset);
		}

		@Override
		protected void cleanUp(Deque<MergeEvent.Node> nodes) {
			if (!nodes.isEmpty()) {
				MergeEvent.Node first = nodes.peekFirst();
				MergeEvent.Node last = nodes.peekLast();

				if (first.index == 0 && last.index == last.sequence.size() - 1) {
					if (first.location == last.location) {
						if (first.location == OUT) {
							nodes.pollFirst();
						} else {
							nodes.pollLast();
						}
					} else {
						nodes.pollLast();
						nodes.pollFirst();
					}
				} else if (first.location == last.location) {
					if (first.location == OUT) {
						nodes.pollFirst();
					} else {
						nodes.pollLast();
					}
				}
			}
		}

		@Override
		protected void register(int index, Location current, Location last, Deque<MergeEvent.Node> nodes, CoordinateSequence sequence, Deque<CoordinateSequence> circles) {
			MergeEvent.Node newNode = null;

			if (current == ON) {
				newNode = new MergeEvent.Node(index, extractor.apply(sequence, index), sequence, last.invert());
			} else {
				MergeEvent.Node lastNode = nodes.peekLast();

				if (lastNode != null) {
					if (lastNode.location != current) {
						if (lastNode.index != index - 1) {
							newNode = new MergeEvent.Node(index - 1, extractor.apply(sequence, index - 1), sequence, current);
						} else {
							nodes.pollLast();
						}
					} else if (current == IN) {
						nodes.pollLast();
						newNode = new MergeEvent.Node(index - 1, extractor.apply(sequence, index - 1), sequence, current);
					}
				} else {
					newNode = new MergeEvent.Node(index - 1, extractor.apply(sequence, index - 1), sequence, current);
				}
			}

			if (newNode != null) {
				if (nodes.isEmpty()) {
					nodes.addLast(newNode);
				} else {
					MergeEvent.Node lastNode = nodes.peekLast();
					if (Math.abs(lastNode.position - newNode.position) > offset) {
						nodes.addLast(newNode);
					} else {
						if (lastNode.location != newNode.location) {
							// TODO: Does it should to handle invalid geometries????
							nodes.pollLast();

							if (newNode.index > lastNode.index + 1) {

								CoordinateSequenceBuilder builder = new CoordinateSequenceBuilder();
								builder.addRing(sequence, lastNode.index, newNode.index, true);

								if (newNode.position != lastNode.position) {
									circles.addLast(builder.closeAndBuild());
								} else {
									circles.addLast(builder.build());
								}
							}
						} else {
							throw new UnsupportedOperationException();
						}
					}
				}
			}
		}
	}

	public static class Result {

		public final List<MergeEvent.Node> nodes;
		public final List<CoordinateSequence> circles;

		public Result(List<MergeEvent.Node> nodes, List<CoordinateSequence> circles) {
			this.nodes = nodes;
			this.circles = circles;
		}
	}
}