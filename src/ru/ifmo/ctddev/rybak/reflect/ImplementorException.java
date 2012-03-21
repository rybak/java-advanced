package ru.ifmo.ctddev.rybak.reflect;

public class ImplementorException extends Exception {
	public ImplementorException() {

	}

	public ImplementorException(String message) {
		super(message);
	}

	public ImplementorException(Exception cause) {
		super(cause);
	}

	public ImplementorException(String message, Exception cause) {
		super(message, cause);
	}
}
