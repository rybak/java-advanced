package ru.ifmo.ctddev.rybak.reflect;

import java.util.Arrays;

public class TestClass {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Class<?> clazz = Class.forName("SubSubInt");
			assert clazz.isInterface();
			System.out.println(Arrays.toString(clazz.getMethods()));
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
		}
	}

}
