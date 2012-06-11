package ru.ifmo.ctddev.rybak.multithreading;

public class TaskImpl implements Task<Integer, IntegerPair> {
	public Integer run(IntegerPair value) {
		return value.a + value.b;
	}
}

