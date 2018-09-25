package com.github.rthoth.xysplit;

import java.util.Iterator;
import java.util.NavigableSet;

abstract class LoopIterable<T> implements Iterable<T> {

	protected Iterator<T> first;
	protected Iterator<T> last;

	@Override
	public Iterator<T> iterator() {
		return new LoopIterator<T>(first, last);
	}

	public static class LoopIterator<T> implements Iterator<T> {

		private Iterator<T> first;
		private Iterator<T> last;

		public LoopIterator(Iterator<T> first, Iterator<T> last) {
			this.first = first;
			this.last = last;
		}

		@Override
		public boolean hasNext() {
			if (first != null) {
				if (first.hasNext())
					return true;
				else
					first = null;
			}

			if (last != null) {
				if (last.hasNext())
					return true;
				else
					last = null;
			}

			return false;
		}

		@Override
		public T next() {
			if (first != null) {
				if (first.hasNext())
					return first.next();
				else
					first = null;
			}

			if (last != null) {
				if (last.hasNext())
					return last.next();
				else
					last = null;
			}

			throw new IllegalStateException();
		}
	}

	public static class NaviableSet<T> extends LoopIterable<T> {

		public NaviableSet(final NavigableSet<T> set, final T root, final boolean inclusive, final boolean asceding) {
			if (asceding) {
				first = set.tailSet(root, false).iterator();
				last = set.headSet(root, inclusive).iterator();
			} else {
				first = set.headSet(root, false).descendingSet().iterator();
				last = set.tailSet(root, inclusive).descendingSet().iterator();
			}
		}
	}
}
