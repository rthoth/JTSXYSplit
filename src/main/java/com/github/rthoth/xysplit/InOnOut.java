package com.github.rthoth.xysplit;

enum InOnOut {
	IN, OUT, ON;

	public InOnOut invert() {
		switch (this) {
		case IN:
			return OUT;
		case OUT:
			return IN;
		default:
			return ON;
		}
	}
}
