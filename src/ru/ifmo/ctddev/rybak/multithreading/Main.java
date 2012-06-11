package ru.ifmo.ctddev.rybak.multithreading;

public class Main {

	private static final int clientsCount = 10;
	private static final int threadsCount = 5;

	public static void main(String[] args) {
		TaskRunner taskRunner = new TaskRunnerImpl(threadsCount);
		Client[] clients = new Client[clientsCount];
		for (int i = 0; i < clientsCount; ++i) {
			clients[i] = new Client(taskRunner);
		}
		Thread[] threads = new Thread[clientsCount];
		for (int i = 0; i < clientsCount; ++i) {
			threads[i] = new Thread(clients[i]);
			threads[i].start();
		}
		try {
			Thread.sleep(1000, 239);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int i = 0; i < clientsCount; ++i) {
			threads[i].interrupt();
		}
		taskRunner.shutdown();
	}
}
