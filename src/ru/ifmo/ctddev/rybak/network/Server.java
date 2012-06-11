package ru.ifmo.ctddev.rybak.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Server {
	final public static String messageString = "Hello, World! ";

	private class Sender implements Runnable {
		DatagramPacket packet;
		DatagramSocket socket;

		public void run() {
			boolean success;
			Message message = null;
			try {
				message = new Message(packet.getData());
				success = true;
			} catch (BadPacketException e) {
				success = false;
			}
			message = new Message(messageString
					+ (success ? message.getMessage() : " Bad packet"), success);
			DatagramPacket answer = new DatagramPacket(message.getBytes(),
					message.getBytes().length, packet.getAddress(),
					packet.getPort());
			try {
				socket.send(answer);
			} catch (IOException e) {
				System.out.println("IOExeption");
			}
		}

		public Sender(DatagramPacket packet, DatagramSocket socket) {
			this.packet = packet;
			this.socket = socket;
		}
	}

	final private static int nThreads = 10;
	final private static int port = 1993;

	void run() {
		Executor executor = Executors.newFixedThreadPool(nThreads);
		try {
			DatagramSocket socket = new DatagramSocket(port);
			while (true) {
				DatagramPacket packet = new DatagramPacket(
						new byte[Message.fullSize], Message.fullSize);
				try {
					socket.receive(packet);
					executor.execute(new Sender(packet, socket));
				} catch (IOException e) {
					System.out.println("IOExecption");
				}
			}
		} catch (SocketException e) {
			System.out.println("SocketException");
		}

	}

	public static void main(String[] args) {
		new Server().run();
	}
}
