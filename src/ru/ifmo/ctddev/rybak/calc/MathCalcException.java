package ru.ifmo.ctddev.rybak.calc;

public class MathCalcException extends CalcException {
	public MathCalcException() {
		super();
	}

	public MathCalcException(String message) {
		super(message);
	}

	public MathCalcException(Exception cause) {
		super(cause);
	}

	public MathCalcException(String message, Exception cause) {
		super(message, cause);
	}
}
