package ru.ifmo.ctddev.rybak.reflect;

public class ImplementorException extends Exception {
	final private static String MESSAGE_HEADER = "Implementor error:\n";

	public ImplementorException() {

	}

	public ImplementorException(String message) {
		super(MESSAGE_HEADER + message);
	}

	public ImplementorException(Exception cause) {
		super(cause);
	}

	public ImplementorException(String message, Exception cause) {
		super(MESSAGE_HEADER + message, cause);
	}
}
