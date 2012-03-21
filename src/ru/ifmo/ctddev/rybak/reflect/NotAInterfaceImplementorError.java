package ru.ifmo.ctddev.rybak.reflect;

public class NotAInterfaceImplementorError extends ImplementorError {
	public NotAInterfaceImplementorError() {

	}

	public NotAInterfaceImplementorError(String message) {
		super(message);
	}

	public NotAInterfaceImplementorError(Exception cause) {
		super(cause);
	}

	public NotAInterfaceImplementorError(String message, Exception cause) {
		super(message, cause);
	}

	public NotAInterfaceImplementorError(String method, Class<?> clazz) {
		super(method + ":\n\t" + clazz.getName() + " is not an interface.");
	}

}
