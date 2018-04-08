package com.github.rthoth.xysplit;


public enum Side {

	LT, GT, EQ;

	public Location location(Side reference) {

	   if (this != EQ)
			return (this == reference) ? Location.IN : Location.OUT;
		else
			return Location.ON;
	}
}
