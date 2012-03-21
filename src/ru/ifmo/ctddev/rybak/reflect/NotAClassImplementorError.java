package ru.ifmo.ctddev.rybak.reflect;

public class NotAClassImplementorError extends ImplementorError {
	public NotAClassImplementorError() {

	}

	public NotAClassImplementorError(String message) {
		super(message);
	}

	public NotAClassImplementorError(Exception cause) {
		super(cause);
	}

	public NotAClassImplementorError(String message, Exception cause) {
		super(message, cause);
	}

	public NotAClassImplementorError(String method, Class<?> clazz) {
		super(method + ":\n\t" + clazz.getName() + " is not a class.");
	}

}
