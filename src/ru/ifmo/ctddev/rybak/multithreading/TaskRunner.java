package ru.ifmo.ctddev.rybak.multithreading;

public interface TaskRunner {
	<X, Y> X run(Task<X, Y> make, Y value) throws InterruptedException;

	void shutdown();
}