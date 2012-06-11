package ru.ifmo.ctddev.rybak.multithreading;

import java.util.Random;

public class Client implements Runnable {
	private TaskRunner taskRunner;
	private Random random;

	public Client(TaskRunner taskRunner) {
		this.taskRunner = taskRunner;
		this.random = new Random();
	}

	final private static int max = 10;
	
	public void run() {
		try {
			while (!Thread.interrupted()) {
				Task<Integer, IntegerPair> task = new TaskImpl();
				int a = random.nextInt(max);
				int b = random.nextInt(max);
				System.out.println(a + " + " + b + " = "
						+ taskRunner.run(task, new IntegerPair(a, b)));
			}
		} catch (InterruptedException e) {
		}

	}
}
