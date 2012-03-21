package ru.ifmo.ctddev.rybak.calc;

public class CalcException extends Exception {

	public CalcException() {
		super();
	}

	public CalcException(String message) {
		super(message);
	}

	public CalcException(Exception cause) {
		super(cause);
	}

	public CalcException(String message, Exception cause) {
		super(message, cause);
	}

}

