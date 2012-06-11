package ru.ifmo.ctddev.rybak.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Message {
	final public static int BYTES_IN_INT = 4;
	private byte[] data;
	private String str;
	private boolean success;
	private int length;

	final public static int bufferSize = 4096;
	final public static int fullSize = bufferSize + BYTES_IN_INT + 1;

	public Message(String messageString, boolean b) {
		success = b;
		str = messageString;
		length = str.length();
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		DataOutputStream dataStream = new DataOutputStream(byteStream);
		try {
			dataStream.writeInt(length);
			dataStream.writeBoolean(success);
			dataStream.writeUTF(str);
		} catch (IOException e) {
			System.err.println("This can not happen. I swear.");
		}
	}

	public Message(byte[] src) throws BadPacketException {
		data = src;
		ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
		DataInputStream dataStream = new DataInputStream(byteStream);
		try {
			length = dataStream.readInt();
			if (length > bufferSize) {
				throw new BadPacketException();
			}
			success = dataStream.readBoolean();
			if (!success) {
				throw new BadPacketException();
			}
			str = dataStream.readUTF();
		} catch (IOException e) {
			System.err.println("This can not happen either. I swear.");
		}
	}

	public String getMessage() {
		return str;
	}

	public byte[] getBytes() {
		return data;
	}
}
