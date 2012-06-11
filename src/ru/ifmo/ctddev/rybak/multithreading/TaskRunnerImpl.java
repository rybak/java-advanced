package ru.ifmo.ctddev.rybak.multithreading;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TaskRunnerImpl implements TaskRunner {

	private class Counter<X, Y> {
		private volatile boolean ready;
		private Task<X, Y> task;
		private Y argument;
		private X result;

		public Counter(Task<X, Y> task, Y argument) {
			this.task = task;
			this.argument = argument;
			ready = false;
		}

		public void run() {
			result = task.run(argument);
			ready = true;
			synchronized (this) {
				notify();
			}
		}

		public synchronized X getResult() throws InterruptedException {
			while (!ready) {
				wait();
			}
			return result;
		}
	}

	private BlockingQueue<Counter<?, ?>> queue;

	private class Maker implements Runnable {
		public void run() {
			while (!Thread.interrupted()) {
				Counter<?, ?> counter;
				try {
					counter = queue.take();
					counter.run();
				} catch (InterruptedException e) {
				}
			}
		}
	}

	Thread thr[];

	public TaskRunnerImpl(int nThreads) {
		queue = new LinkedBlockingQueue<Counter<?, ?>>();
		thr = new Thread[nThreads];
		for (int i = 0; i < nThreads; ++i) {
			thr[i] = new Thread(new Maker());
			thr[i].start();
		}
	}

	public <X, Y> X run(Task<X, Y> make, Y value) throws InterruptedException {
		Counter<X, Y> todo = new Counter<X, Y>(make, value);
		queue.add(todo);
		return todo.getResult();
	}

	public void shutdown() {
		for (int i = 0; i < thr.length; i++) {
			thr[i].interrupt();
		}
	}

}
