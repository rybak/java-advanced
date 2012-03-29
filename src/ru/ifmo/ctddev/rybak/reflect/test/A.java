package ru.ifmo.ctddev.rybak.reflect.test;

import java.awt.*;
import java.awt.List;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;

interface I1 {
	Object a();
}
interface I2 {
	String a();
}

public abstract class A extends AbstractMap implements Comparable, DataInput,
		DataOutput, I1, I2 {
	public A(int a, Boolean b) throws VirtualMachineError, IOException {

	}

	protected abstract NullPointerException test(java.awt.List[][][] list1,
			java.util.List list2, OutOfMemoryError... errors) throws Throwable;

}