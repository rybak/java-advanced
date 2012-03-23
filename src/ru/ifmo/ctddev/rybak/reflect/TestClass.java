package ru.ifmo.ctddev.rybak.reflect;

import java.util.Arrays;

public class TestClass {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Class<?> clazz = Class.forName("ru.ifmo.ctddev.rybak.reflect.test." +
					"AbstractTestClassSuper");
			System.out.println(Arrays.toString(clazz.getMethods()));
			System.out.println(Arrays.toString(clazz.getDeclaredMethods()));
		} catch (ClassNotFoundException e) {
			System.err.println(e.getMessage());
		}
	}

}
