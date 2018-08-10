package com.github.rthoth.xysplit;

import java.util.Iterator;
import java.util.NavigableMap;

public class MergeScanLine implements Iterable<T2<MergeEvent.Node, MergeEvent.Node>> {

	private final Iterator<T2<MergeEvent.Node, MergeEvent.Node>> EMPTY_ITERATOR = new Iterator<T2<MergeEvent.Node, MergeEvent.Node>>() {
		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public T2<MergeEvent.Node, MergeEvent.Node> next() {
			throw new UnsupportedOperationException();
		}
	};

	private final double offset;

	private final NavigableMap<Double, MergeEvent.Node> ltBorder;
	private final NavigableMap<Double, MergeEvent.Node> gtBorder;

	public MergeScanLine(double offset, NavigableMap<Double, MergeEvent.Node> ltBorder, NavigableMap<Double, MergeEvent.Node> gtBorder) {
		this.ltBorder = ltBorder;
		this.gtBorder = gtBorder;
		this.offset = Math.abs(offset);
	}

	@Override
	public Iterator<T2<MergeEvent.Node, MergeEvent.Node>> iterator() {

		if (!ltBorder.isEmpty() || !gtBorder.isEmpty()) {
			return new MergeIterator();
		} else {
			return EMPTY_ITERATOR;
		}
	}

	private class MergeIterator implements Iterator<T2<MergeEvent.Node, MergeEvent.Node>> {

		private T2<MergeEvent.Node, MergeEvent.Node> _next;

		private Iterator<MergeEvent.Node> ltIterator = ltBorder.values().iterator();
		private Iterator<MergeEvent.Node> gtIterator = gtBorder.values().iterator();

		private MergeEvent.Node lt;
		private MergeEvent.Node gt;

		MergeIterator() {
			lt = ltIterator.hasNext() ? ltIterator.next() : null;
			gt = gtIterator.hasNext() ? gtIterator.next() : null;
			_next = go();
		}

		@Override
		public boolean hasNext() {
			return _next != null;
		}

		private T2<MergeEvent.Node, MergeEvent.Node> go() {
			T2<MergeEvent.Node, MergeEvent.Node> ret;

			if (lt != null && gt != null) {
				if (Math.abs(lt.position - gt.position) <= offset) {
					ret = T2.of(lt, gt);
					lt = ltIterator.hasNext() ? ltIterator.next() : null;
					gt = gtIterator.hasNext() ? gtIterator.next() : null;
				} else {
					if (lt.position < gt.position) {
						ret = T2.of(lt, null);
						lt = ltIterator.hasNext() ? ltIterator.next() : null;
					} else {
						ret = T2.of(null, gt);
						gt = gtIterator.hasNext() ? gtIterator.next() : null;
					}
				}
			} else if (lt != null) {
				ret = T2.of(lt, null);
				lt = ltIterator.hasNext() ? ltIterator.next() : null;
			} else if (gt != null) {
				ret = T2.of(null, gt);
				gt = gtIterator.hasNext() ? gtIterator.next() : null;
			} else {
				ret = null;
			}

			return ret;
		}

		@Override
		public T2<MergeEvent.Node, MergeEvent.Node> next() {
			T2<MergeEvent.Node, MergeEvent.Node> ret = _next;
			_next = go();
			return ret;
		}
	}
}
