package ru.ifmo.ctddev.rybak.calc;

public class OverflowCalcException extends MathCalcException {
	public OverflowCalcException() {
		super("overflow");
	}

	public OverflowCalcException(String message) {
		super(message);
	}

	public OverflowCalcException(Exception cause) {
		super(cause);
	}

	public OverflowCalcException(String message, Exception cause) {
		super(message, cause);
	}

	public OverflowCalcException(String message, int position) {
		super("Overflow at position " + position + " (" + message + ").");
	}

}