package com.github.rthoth.xysplit;

import org.locationtech.jts.geom.CoordinateSequence;

import java.util.*;

public abstract class MergeSequence {

	public static final double DEFAULT_OFFSET = 1e-9D;

	protected final List<CoordinateSequence> lts;
	protected final List<CoordinateSequence> gts;

	protected final double offset;
	protected final Reference reference;

	protected final OffsetMap<MergeEvent> events;
	protected final OffsetMap<MergeEvent.Node> gtNodes;
	protected final OffsetMap<MergeEvent.Node> ltNodes;
	protected final List<CoordinateSequence> ltUnconnected;
	protected final List<CoordinateSequence> gtUnconnected;
	protected final DIMap<MergeEvent.Node> connectedNodes = new DIMap.Hash<>();
	protected final HashMap<MergeEvent.Node, MergeEvent> nodeToEvent = new HashMap<>();
	protected final TreeSet<MergeEvent.Node> boundary = new TreeSet<>();

	protected final XYDefinitions.OrdinateExtractor extractor;

	public MergeSequence(Reference reference, double offset, List<CoordinateSequence> lts, List<CoordinateSequence> gts) {
		this.reference = reference;
		this.offset = Math.abs(offset);
		this.lts = lts;
		this.gts = gts;
		this.events = new OffsetMap.TreeMap<>(this.offset);
		this.extractor = reference.xy == XY.X ? XYDefinitions.Y_ORDINATE_EXTRACTOR : XYDefinitions.X_ORDINATE_EXTRACTOR;

		ltNodes = new OffsetMap.TreeMap<>(offset);
		gtNodes = new OffsetMap.TreeMap<>(offset);

		LinkedList<CoordinateSequence> ltUnconnected = new LinkedList<>();
		LinkedList<CoordinateSequence> gtUnconnected = new LinkedList<>();

		this.ltUnconnected = Collections.unmodifiableList(ltUnconnected);
		this.gtUnconnected = Collections.unmodifiableList(gtUnconnected);

		register(Side.LT, lts, ltNodes, ltUnconnected);
		register(Side.GT, gts, gtNodes, gtUnconnected);
		double position;

		for (MergeEvent.Node _lt : ltNodes) {
			MergeEvent.Node _gt = gtNodes.get(_lt.position);
			if (_gt != null) {
				position = (_lt.position + _gt.position) / 2;
				MergeEvent event = new MergeEvent(position, _lt, _gt);
				events.put(position, event);

				nodeToEvent.put(_lt, event);
				nodeToEvent.put(_gt, event);
			}
		}
	}

	private void register(Side side, List<CoordinateSequence> sequences, OffsetMap<MergeEvent.Node> offsetMap, List<CoordinateSequence> unConnected) {
		for (CoordinateSequence sequence : sequences) {
			Deque<MergeEvent.Node> nodes = register(sequence, side);
			boundary.addAll(nodes);

			if (!nodes.isEmpty()) {
				if (nodes.peekFirst().location == Location.OUT)
					nodes.addLast(nodes.pollFirst());

				Iterator<MergeEvent.Node> it = nodes.iterator();
				while (it.hasNext()) {
					MergeEvent.Node inNode = it.next();
					offsetMap.put(inNode.position, inNode);

					if (it.hasNext()) {
						MergeEvent.Node outNode = it.next();
						offsetMap.put(outNode.position, outNode);
						connectedNodes.add(inNode, outNode);
					}
				}
			} else {
				unConnected.add(sequence);
			}
		}
	}

	private Side classify(CoordinateSequence sequence, int index) {
		return reference.classify(sequence, index, offset);
	}

	protected abstract void cleanUp(Deque<MergeEvent.Node> nodes);

	protected abstract void register(int index, Location current, Location last, CoordinateSequence sequence, Deque<MergeEvent.Node> nodes);

	public MergeEvent.Node getConnected(MergeEvent.Node node) {
		return connectedNodes.get(node);
	}

	public OffsetMap<MergeEvent> getEvents() {
		return events.unmodifiable();
	}

	public NavigableSet<MergeEvent.Node> getNodeBoundary() {
		return Collections.unmodifiableNavigableSet(boundary);
	}

	private Deque<MergeEvent.Node> register(CoordinateSequence sequence, Side side) {
		LinkedList<MergeEvent.Node> nodes = new LinkedList<>();
		if (sequence.size() > 1) {

			Location last = classify(sequence, 0).location(side), current;

			for (int i = 1, l = sequence.size(); i < l; i++) {
				current = classify(sequence, i).location(side);
				if ((current == Location.ON || last == Location.ON) && !(current == Location.ON && last == Location.ON)) {
					register(i, current, last, sequence, nodes);
				}

				last = current;
			}

			cleanUp(nodes);

		}

		return nodes;
	}

	public OffsetMap<MergeEvent.Node> getLTNodes() {
		return ltNodes.unmodifiable();
	}

	public OffsetMap<MergeEvent.Node> getGTNodes() {
		return gtNodes.unmodifiable();
	}

	public MergeEvent getEvent(MergeEvent.Node node) {
		return nodeToEvent.get(node);
	}

	public List<CoordinateSequence> getLTUnconnected() {
		return ltUnconnected;
	}

	public List<CoordinateSequence> getGTUnconnected() {
		return gtUnconnected;
	}


	public static class Poly extends MergeSequence {

		public Poly(Reference reference, double offset, List<CoordinateSequence> lts, List<CoordinateSequence> gts) {
			super(reference, offset, lts, gts);
		}

		public Poly(Reference reference, List<CoordinateSequence> lts, List<CoordinateSequence> gts) {
			super(reference, MergeSequence.DEFAULT_OFFSET, lts, gts);
		}

		@Override
		protected void cleanUp(Deque<MergeEvent.Node> nodes) {
			if (nodes.size() > 1) {
				if (nodes.peekFirst().location == Location.OUT) {
					nodes.addLast(nodes.pollFirst());
				}
			}
		}

		@Override
		protected void register(int index, Location current, Location last, CoordinateSequence sequence, Deque<MergeEvent.Node> nodes) {
			if (current == Location.ON) {
				nodes.addLast(new MergeEvent.Node(index, last.invert(), extractor.get(sequence, index), sequence));
			} else {
				MergeEvent.Node newNode = null;
				MergeEvent.Node lastNode = nodes.peekLast();

				if (lastNode != null && lastNode.index + 1 == index) {
					nodes.removeLast();
				} else {
					newNode = new MergeEvent.Node(index - 1, current, extractor.get(sequence, index - 1), sequence);
				}

				if (newNode != null) {
					nodes.addLast(newNode);
				}
			}
		}
	}
}
