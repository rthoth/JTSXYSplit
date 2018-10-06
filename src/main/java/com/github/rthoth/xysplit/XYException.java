package com.github.rthoth.xysplit;

@SuppressWarnings("ALL")
public abstract class XYException extends RuntimeException {

	public XYException(String message) {
		super(message);
	}

	public XYException(String message, Throwable cause) {
		super(message, cause);
	}

	public static class Merge extends XYException {

		public Merge(String message) {
			super(message);
		}

		@SuppressWarnings("unused")
		public Merge(String message, Throwable cause) {
			super(message, cause);
		}
	}

	public static class Split extends XYException {

		public Split(String message, Throwable cause) {
			super(message, cause);
		}

		public Split(String message) {
			super(message);
		}
	}
}
