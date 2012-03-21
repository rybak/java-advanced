package ru.ifmo.ctddev.rybak.calc;

class ParenthesesWasExpectedCalcException extends InvalidExpressionCalcException {

	public ParenthesesWasExpectedCalcException() {
		super();
	}

	public ParenthesesWasExpectedCalcException(String message) {
		super(message);
	}

	public ParenthesesWasExpectedCalcException(Exception cause) {
		super(cause);
	}

	public ParenthesesWasExpectedCalcException(String message, Exception cause) {
		super(message, cause);
	}

	public ParenthesesWasExpectedCalcException(String expression, int position) {
		super(expression, createStringPointer(position)
				+ "\n')' was expected at position " + position + ".");
	}

}
