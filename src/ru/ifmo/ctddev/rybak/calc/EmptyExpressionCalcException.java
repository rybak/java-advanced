package ru.ifmo.ctddev.rybak.calc;

public class EmptyExpressionCalcException extends InvalidExpressionCalcException{

	public EmptyExpressionCalcException() {
		super("Expression is empty.");
	}

	public EmptyExpressionCalcException(String message) {
		super(message);
	}

	public EmptyExpressionCalcException(Exception cause) {
		super(cause);
	}

	public EmptyExpressionCalcException(String message, Exception cause) {
		super(message, cause);
	}

}
