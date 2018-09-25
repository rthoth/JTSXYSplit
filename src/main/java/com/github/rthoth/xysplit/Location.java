package com.github.rthoth.xysplit;

enum Location {
	IN, OUT, ON;

	public Location invert() {
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
