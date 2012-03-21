package ru.ifmo.ctddev.rybak.calc;

public class WrongNumberFormatCalcException extends
		InvalidExpressionCalcException {

	public WrongNumberFormatCalcException() {
		super();
	}

	public WrongNumberFormatCalcException(Exception cause) {
		super(cause);
	}

	public WrongNumberFormatCalcException(String message, Exception cause) {
		super(message, cause);
	}

	public WrongNumberFormatCalcException(String message) {
		super(message);
	}
	
	public WrongNumberFormatCalcException(String expression, int pos) {
		super(expression, createStringPointer(expression.length())
				+ "\nUnexpected end of string.");
	}
}
