package ru.ifmo.ctddev.rybak.reflect;

public abstract class AbstractTestClass extends AbstractTestClassSuper
		implements TestInterface {

	protected int size;

	public int ATCSrealizedInATC() {
		return size;
	}

	public abstract Object testArray(int[][][] arr, boolean b);

	public abstract String testArray(String[][][] arr, long ss);

	public void add(int a) {
		ATCStoRealizeInImpl();
		ensureSize("");
	}

	public String a(int l) {
		return "hello world";
	}

	public abstract char ATCtoRealizeInImpl();

	public final char test() {
		return 0;
	}

	protected abstract void ensureSize(String s);

	public abstract int a(long b);

}
