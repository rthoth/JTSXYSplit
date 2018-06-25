package com.github.rthoth.xysplit;

import java.util.HashMap;
import java.util.Map;

public abstract class DIMap<T> {

	public abstract DIMap<T> copy();

	public static class Hash<T> extends DIMap<T> {

		public Hash() {
			super(new HashMap<>());
		}

		private Hash(Map<T, T> map) {
			super(map);
		}

		@Override
		public DIMap<T> copy() {
			return new Hash<>(new HashMap<>(underlying));
		}
	}

	protected final Map<T, T> underlying;

	public DIMap(Map<T, T> underlying) {
		this.underlying = underlying;
	}

	public void add(T _1, T _2) {
		underlying.put(_1, _2);
		underlying.put(_2, _1);
	}

	public T get(T key) {
		return underlying.get(key);
	}
}
