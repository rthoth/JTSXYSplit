package com.github.rthoth.xysplit;

enum IO {
	I_O, O_I;

	public IO invert() {
		return this == I_O ? O_I : I_O;
	}

}
