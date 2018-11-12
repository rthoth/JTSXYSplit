package com.github.rthoth.xysplit;


enum Side {

	LT, GT, EQ;

	public InOnOut inOnOut(Side reference) {

	   if (this != EQ)
			return (this == reference) ? InOnOut.IN : InOnOut.OUT;
		else
			return InOnOut.ON;
	}

	public Side invert() {
		return this == LT ? GT : (this == GT ? LT : EQ);
	}
}
