package ru.ifmo.ctddev.rybak.network;

public class BadPacketException extends Exception {

	public BadPacketException() {

	}

	public BadPacketException(String message) {
		super(message);
	}

	public BadPacketException(Exception cause) {
		super(cause);
	}

	public BadPacketException(String message, Exception cause) {
		super(message, cause);
	}


}
