package ru.ifmo.ctddev.rybak.reflect;

import java.util.AbstractList;

public abstract class AbstractTestClassSuper implements TestInterface{
	AbstractList<Object> l;
	public abstract int ATCSrealizedInATC();
	protected abstract int ATCStoRealizeInImpl();
	abstract long packagelevel();
}
