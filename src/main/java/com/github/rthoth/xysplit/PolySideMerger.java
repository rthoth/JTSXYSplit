package com.github.rthoth.xysplit;

import com.github.rthoth.xysplit.MergeEvent.Node;
import com.github.rthoth.xysplit.MergeNodeSequencer.Result;
import com.github.rthoth.xysplit.PolygonMerger.Unity;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Polygon;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.github.rthoth.xysplit.Location.IN;

class PolySideMerger implements BiFunction<Side, Iterable<Polygon>, List<Unity>> {

	private static int computeNewIndex(int reference, int index, boolean forward, int size) {
		if (forward)
			return (index >= reference) ? index - reference : size - reference + index - 1;
		else
			return (index <= reference) ? reference - index : reference + size - index - 1;
	}

	protected final MergeNodeSequencer.Poly sequencer;

	public PolySideMerger(MergeNodeSequencer.Poly sequencer) {
		this.sequencer = sequencer;
	}

	@Override
	public List<Unity> apply(Side side, Iterable<Polygon> polygons) {
		return new Merger(side, polygons).result;
	}

	private class Merger {

		public final List<Unity> result;

		private Set<Unity> readyUnities = new HashSet<>();
		private final NavigableMap<Double, Join> joins = new TreeMap<>();
		private final Map<Node, Unity> nodeToUnity = new HashMap<>();
		private final Map<Node, Join> nodeToJoin = new HashMap<>();

		private boolean forward;
		private boolean inside;
		private boolean toward;
		private Join startJoin;
		private Join stopJoin;
		private Node startNode;
		private Node stopNode;
		private Node originNode;

		public Merger(Side side, Iterable<Polygon> polygons) {

			final LinkedList<Unity> ret = new LinkedList<>();
			final TreeMap<Double, Node> boundary = new TreeMap<>();

			for (Polygon polygon : polygons) {
				CoordinateSequence shell = polygon.getExteriorRing().getCoordinateSequence();
				Result sequenceResult = sequencer.apply(side, shell);
				List<Node> nodes = sequenceResult.nodes;

				Unity unity = new Unity(shell, sequenceResult, polygon);

				if (!nodes.isEmpty()) {

					int joinSize = joins.size();

					for (Node node : nodes) {
						Node other = boundary.put(node.position, node);
						nodeToUnity.put(node, unity);

						if (other == null) {
							nodeToUnity.put(node, unity);
							boundary.put(node.position, node);
						} else {
							Join join = new Join(node.position, node, other);
							Join otherJoin = joins.put(join.position, join);

							if (otherJoin != null)
								throw new UnsupportedOperationException();

							nodeToJoin.put(node, join);
							nodeToJoin.put(other, join);
							boundary.remove(other.position);
							readyUnities.remove(nodeToUnity.get(other));
						}
					}

					if (joinSize == joins.size())
						readyUnities.add(unity);

				} else if (!unity.isEmpty()) {
					ret.addLast(unity);
				}
			}

			Iterator<Node> iterator = boundary.values().iterator();

			while (iterator.hasNext()) {
				Node first = iterator.next();

				if (iterator.hasNext()) {
					Node second = iterator.next();
					if (Math.abs(second.position - first.position) <= sequencer.offset) {
						Join join = new Join(first, second);
						Join otherJoin = joins.put(join.position, join);
						if (otherJoin != null) {
							throw new UnsupportedOperationException();
						}
					}
				}
			}

			ret.addAll(readyUnities);

			if (!joins.isEmpty())
				mergeJoins(ret);

			result = Collections.unmodifiableList(ret);
		}

		private void mergeJoins(Deque<Unity> ret) {
			while (!joins.isEmpty()) {
				mergeJoin(joins.pollFirstEntry().getValue(), ret);
			}
		}

		private void mergeJoin(final Join root, Deque<Unity> ret) {
			startJoin = root;
			Node _1 = root._1, _2 = root._2;

			originNode = startNode = _1.location == IN ? _1 : _2;

			LinkedList<Node> newNodes = new LinkedList<>();
			CoordinateSequenceBuilder builder = new CoordinateSequenceBuilder();

			inside = true;
			Set<Unity> usedPolies = new HashSet<>();

			do {
				usedPolies.add(nodeToUnity.get(startNode));
				searchStop(newNodes, builder.size());

				int stopIndex;

				if (toward)
					stopIndex = stopNode.index != 0 ? stopNode.index - 1 : stopNode.sequence.size() - 2;
				else
					stopIndex = (stopNode.index + 1) % (stopNode.sequence.size() - 1);

				if (stopIndex != startNode.index)
					builder.addRing(startNode.sequence, startNode.index, stopIndex, toward);

				searchStart();

			} while (startNode != originNode);

			CoordinateSequence sequence = builder.closeAndBuild();
			List<Node> mappedNodes = newNodes.stream().map(node -> node.withSequence(sequence)).collect(Collectors.toList());


			List<CoordinateSequence> holes = usedPolies
							.stream()
							.flatMap(unity -> unity.holes.stream())
							.collect(Collectors.toList());

			ret.addLast(new Unity(sequence, mappedNodes, holes));
		}

		private void searchStart() {
			if (stopJoin != null) {
				startNode = stopJoin.jumpFrom(stopNode);
				startJoin = stopJoin;
			} else {
				inside = !inside;
				startNode = startJoin.jumpFrom(startNode);
			}
		}

		private void searchStop(Deque<Node> newNodes, int size) {
			forward = startNode.location == IN;
			toward = forward == inside;
			Unity unity = nodeToUnity.get(startNode);
			Iterable<Node> iterable = new IterableLoop.NaviableSet<>(unity.nodes, startNode, false, toward);
			int sequenceSize = startNode.sequence.size();

			stopNode = null;
			stopJoin = null;

			for (Node node : iterable) {
				Join join = nodeToJoin.get(node);
				if (join == null) {

					final Node newNode = toward ?
									node.withIndex(size + computeNewIndex(startNode.index, node.index, toward, sequenceSize))
									: node.withIndex(size + computeNewIndex(startNode.index, node.index, toward, sequenceSize), node.location.invert());

					newNodes.addLast(newNode);

				} else {
					joins.remove(join.position);
					stopNode = node;
					stopJoin = join;
					break;
				}
			}

			if (stopNode == null)
				stopNode = startNode;
		}

		private class Join {

			protected final double position;
			protected final Node _1;
			protected final Node _2;

			public Join(Node _1, Node _2) {
				this((_1.position + _2.position) / 2, _1, _2);
			}

			public Join(double position, Node _1, Node _2) {
				this.position = position;
				this._1 = _1;
				this._2 = _2;
			}

			public Node jumpFrom(Node node) {
				if (_1 == node)
					return _2;
				else if (_2 == node)
					return _1;
				else
					throw new IllegalArgumentException(node.toString());
			}
		}
	}
}
