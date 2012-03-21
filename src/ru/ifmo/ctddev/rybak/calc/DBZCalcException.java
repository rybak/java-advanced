package ru.ifmo.ctddev.rybak.calc;

public class DBZCalcException extends MathCalcException {

	public DBZCalcException() {
		super("division by zero");
	}

	public DBZCalcException(String message) {
		super(message);
	}

	public DBZCalcException(Exception cause) {
		super(cause);
	}

	public DBZCalcException(String message, Exception cause) {
		super(message, cause);
	}

}
