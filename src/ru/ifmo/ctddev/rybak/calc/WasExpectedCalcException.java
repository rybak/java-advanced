package ru.ifmo.ctddev.rybak.calc;

public class WasExpectedCalcException extends InvalidExpressionCalcException {

	public WasExpectedCalcException() {
		super();
	}

	public WasExpectedCalcException(String message) {
		super(message);
	}

	public WasExpectedCalcException(Exception cause) {
		super(cause);
	}

	public WasExpectedCalcException(String message, Exception cause) {
		super(message, cause);
	}

	public WasExpectedCalcException(String expression, String expectation,
			int position) {
		super(expression, createStringPointer(position) + '\n' + expectation
				+ " was expected at position " + position);
	}

}
