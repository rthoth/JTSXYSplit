package com.github.rthoth.xysplit;

public abstract class GeometryMerger {

	public Side nextSide(MergeEvent event, Side current) {
		if (event.lt.location != event.gt.location) {
			if (event.lt.location == Location.IN) {
				return Side.LT;
			} else {
				return Side.GT;
			}
		} else {
			return current.invert();
		}
	}
}
