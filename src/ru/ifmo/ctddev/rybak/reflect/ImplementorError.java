package ru.ifmo.ctddev.rybak.reflect;

public class ImplementorError extends RuntimeException {
	public ImplementorError() {

	}

	public ImplementorError(String message) {
		super(message);
	}

	public ImplementorError(Exception cause) {
		super(cause);
	}

	public ImplementorError(String message, Exception cause) {
		super(message, cause);
	}
}
