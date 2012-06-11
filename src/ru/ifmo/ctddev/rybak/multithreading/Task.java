package ru.ifmo.ctddev.rybak.multithreading;

public interface Task<X, Y> {
	X run(Y value);
}