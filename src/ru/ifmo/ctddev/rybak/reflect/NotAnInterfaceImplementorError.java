package ru.ifmo.ctddev.rybak.reflect;

public class NotAnInterfaceImplementorError extends ImplementorError {
	public NotAnInterfaceImplementorError() {

	}

	public NotAnInterfaceImplementorError(String message) {
		super(message);
	}

	public NotAnInterfaceImplementorError(Exception cause) {
		super(cause);
	}

	public NotAnInterfaceImplementorError(String message, Exception cause) {
		super(message, cause);
	}

	public NotAnInterfaceImplementorError(String method, Class<?> clazz) {
		super(method + ":\n\t" + clazz.getName() + " is not an interface.");
	}

}
