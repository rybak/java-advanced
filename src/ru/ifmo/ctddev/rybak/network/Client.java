package ru.ifmo.ctddev.rybak.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Client {

	private static class ClientThread implements Runnable {
		private InetAddress address;
		private int port;
		private int num;
		private String prefix;

		public ClientThread(InetAddress address, int port, int num,
				String prefix) {
			this.address = address;
			this.port = port;
			this.num = num;
			this.prefix = prefix;
		}

		final private static int COUNT_PACKETS = 239;

		private void send(int i) throws IOException {
			String messageString = prefix + num + i;
			Message message = new Message(messageString, true);
			DatagramPacket packet = new DatagramPacket(message.getBytes(),
					message.getBytes().length, address, port);
			socket.send(packet);
		}

		private void recieve() throws IOException {
			DatagramPacket packet = new DatagramPacket(
					new byte[Message.bufferSize], Message.bufferSize);
			socket.receive(packet);
			try {
				Message message = new Message(packet.getData());
				System.out.println(message.getMessage());
			} catch (BadPacketException e) {
				System.err.println("Bad packet. I am sad. :-(");
			}

		}

		private DatagramSocket socket;

		public void run() {
			try {
				socket = new DatagramSocket();
				for (int i = 0; i < COUNT_PACKETS; ++i) {
					try {
						send(i);
						recieve();
					} catch (IOException e) {
						System.out.println("IOException: " + e.getMessage());
					}
				}
			} catch (SocketException e) {
				System.out.println("SocketException: " + e.getMessage());
			}
		}
	}

	final private static int nThreads = 10;

	final private static int countArgs = 3;

	public static void main(String[] args) throws UnknownHostException {
		if (args.length < countArgs) {
			System.out.println("Not enought parametrs");
			return;
		}
		Thread[] threads = new Thread[nThreads];
		InetAddress address = InetAddress.getByName(args[0]);
		for (int i = 0; i < nThreads; ++i) {
			threads[i] = new Thread(new ClientThread(address,
					Integer.parseInt(args[1]), i, args[2]));
			threads[i].start();
		}
	}
}
