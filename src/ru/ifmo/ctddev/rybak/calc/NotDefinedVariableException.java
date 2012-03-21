package ru.ifmo.ctddev.rybak.calc;

public class NotDefinedVariableException extends RuntimeException {
	public NotDefinedVariableException(char c) {
		super("The value of the variable " + c + " is not defined.");
	}

	public NotDefinedVariableException() {
	}

	public NotDefinedVariableException(String message) {
		super(message);
	}

	public NotDefinedVariableException(String message, Exception cause) {
		super(message, cause);
	}

	public NotDefinedVariableException(Exception cause) {
		super(cause);
	}
}
