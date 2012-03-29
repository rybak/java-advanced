package ru.ifmo.ctddev.rybak.reflect;

public class CannotBeImplementedImplementorException extends
		ImplementorException {
	final private static String MESSAGE_HEADER = "Can not be implemented:\n";

	public CannotBeImplementedImplementorException() {

	}

	public CannotBeImplementedImplementorException(String message) {
		super(MESSAGE_HEADER + message);
	}

	public CannotBeImplementedImplementorException(Exception cause) {
		super(cause);
	}

	public CannotBeImplementedImplementorException(String message,
			Exception cause) {
		super(MESSAGE_HEADER + message, cause);
	}
}
