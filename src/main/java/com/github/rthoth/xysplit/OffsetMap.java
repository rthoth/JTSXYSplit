package com.github.rthoth.xysplit;

import java.util.*;

public abstract class OffsetMap<T> implements Iterable<T> {

	protected final NavigableMap<Double, T> underlying;
	protected final double offset;

	public OffsetMap(NavigableMap<Double, T> underlying, double offset) {
		this.underlying = underlying;
		this.offset = offset;
	}

	private T getIfNear(double key, Map.Entry<Double, T> entry) {
		if (entry != null) {
			if (entry.getKey() != key) {
				return Math.abs(entry.getKey() - key) > offset ? null : entry.getValue();
			} else {
				return entry.getValue();
			}
		} else
			return null;
	}

	public T put(double key, T value) {
		return underlying.put(key, value);
	}

	public T get(double key) {
		T floor = getIfNear(key, underlying.floorEntry(key));
		T ceiling = getIfNear(key, underlying.ceilingEntry(key));

		if (floor == ceiling) {
			return floor;
		} else if (!(floor != null && ceiling != null)) {
			return floor != null ? floor : ceiling;
		} else {
			throw new IllegalArgumentException(String.valueOf(key) + "!");
		}
	}

	public Iterator<T> iterator() {
		return underlying.values().iterator();
	}

	public abstract OffsetMap<T> unmodifiable();

	public static class TreeMap<T> extends OffsetMap<T> {

		public TreeMap(double offset) {
			super(new java.util.TreeMap<>(), offset);
		}

		private TreeMap(NavigableMap<Double, T> original, double offset) {
			super(original, offset);
		}

		@Override
		public OffsetMap<T> unmodifiable() {
			return new TreeMap<T>(Collections.unmodifiableNavigableMap(underlying), offset);
		}
	}
}
