package ru.ifmo.ctddev.rybak.reflect.test;

import java.awt.*;
import java.awt.List;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;

@SuppressWarnings("unused")
interface I1 {
	Object b();
}

@interface I2 {
	String b();
}
abstract class X1 {
	abstract void a();
}
class X2 extends X1{
	void a() {
		
	}
}
abstract class X3 extends X2 {
	abstract void a();
}

@SuppressWarnings("rawtypes")
public abstract class A extends X3 implements Comparable, DataInput,
		DataOutput, I1, I2 {
	public A(int a, Boolean b) throws VirtualMachineError, IOException {

	}

	protected abstract NullPointerException test(java.awt.List[][][] list1,
			java.util.List list2, OutOfMemoryError... errors) throws Throwable;

}