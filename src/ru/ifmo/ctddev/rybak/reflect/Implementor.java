package ru.ifmo.ctddev.rybak.reflect;

import java.util.List;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

public class Implementor {
	final private static String IO_ERROR_MESSAGE = "Implementor:\nAn I/O error occurred: ";
	final private String outputFileName;
	private FileWriter out;

	private String className;
	private Class<?> clazz;
	private boolean isClass;
	private String superClassName;

	public Implementor(String superClassName) throws ImplementorException,
			ClassNotFoundException {
		this.clazz = Class.forName(superClassName);
		setupInterfaceOrClass();
		this.superClassName = superClassName;
		this.className = clazz.getSimpleName() + "Impl";
		this.outputFileName = className + ".java";
	}

	private void setupInterfaceOrClass() throws ImplementorException {
		if (isClass(clazz)) {
			isClass = true;
		} else {
			if (clazz.isInterface()) {
				isClass = false;
			} else {
				throw new ImplementorException("Not a class or interface\n");
			}
		}
	}

	private void writeClass() throws IOException {
		writePackage();
		writeHeader();
		for (Method m : getMethods()) {
			writeMethod(m);
		}
		writeFooter();
	}

	private String methodToString(Method m) {
		ArgumentsStrings args = new ArgumentsStrings(m);
		return m.getReturnType().getSimpleName() + " " + m.getName() + " "
				+ Arrays.toString(args.types);
	}

	private List<Method> getMethods() {
		Set<Class<?>> superClasses = createHierarchy(clazz);
		Map<String, Method> ms = new HashMap<String, Method>();
		for (Class<?> c : superClasses) {
			for (Method m : c.getDeclaredMethods()) {
				int mod = m.getModifiers();
				if (Modifier.isAbstract(mod) && !Modifier.isPrivate(mod)) {
					System.err.println("ADD : " + m);
					ms.put(methodToString(m), m);
				}
			}
		}
		for (Class<?> c : superClasses) {
			for (Method m : c.getDeclaredMethods()) {
				int mod = m.getModifiers();
				Modifier.isFinal(mod);
				if (!Modifier.isPrivate(mod) && !Modifier.isNative(mod)
						&& !Modifier.isFinal(mod)) {
					if (!Modifier.isAbstract(mod)) {
						System.err.println("REMOVE : " + m);
						ms.remove(methodToString(m));
					}
				}
			}
		}
		return new ArrayList<Method>(ms.values());
	}

	private Set<Class<?>> createHierarchy(Class<?> clazz) {
		Set<Class<?>> classes = new HashSet<Class<?>>();
		Class<?> superClass = clazz;
		do {
			classes.add(superClass);
			superClass = superClass.getSuperclass();
			for (Class<?> interfaze : clazz.getInterfaces()) {
				classes.addAll(createHierarchy(interfaze));
			}
		} while (superClass != null);
		return classes;
	}

	private void writePackage() throws IOException {
		out.write("package " + clazz.getPackage().getName() + ";\n\n");
	}

	private void writeHeader() throws IOException {
		out.write("public class " + className + " " + createExtentionString()
				+ " {\n");
	}

	private class ArgumentsStrings {
		final private String[] types;
		final private String[] names;

		public ArgumentsStrings(Method method) {
			Class<?>[] parametrs = method.getParameterTypes();
			boolean isVarArgs = method.isVarArgs();
			this.types = new String[parametrs.length];
			this.names = new String[parametrs.length];
			if (parametrs.length > 0) {
				int n = types.length - (isVarArgs ? 1 : 0);
				for (int i = 0; i < n; ++i) {
					types[i] = parametrs[i].getSimpleName();
					while (parametrs[i].isArray()) {
						parametrs[i] = parametrs[i].getComponentType();
					}
					names[i] = createVarName(parametrs[i].getSimpleName()) + i;
				}
				if (isVarArgs) {
					types[n] = "Object...";
					names[n] = "arguments";
				}
			}
		}

		private String createVarName(String name) {
			return Character.toLowerCase(name.charAt(0)) + name.substring(1);
		}

		public String[] getTypes() {
			return types;
		}

		public String[] getNames() {
			return names;
		}
	}

	private void writeModifiers(int mod) throws IOException {
		if (Modifier.isPublic(mod)) {
			out.write("public ");
		} else {
			if (Modifier.isProtected(mod)) {
				out.write("protected ");
			}
		}
		if (Modifier.isStatic(mod)) {
			out.write("static ");
		}
	}

	private void writeArguments(ArgumentsStrings arguments) throws IOException {
		String[] types = arguments.getTypes();
		String[] names = arguments.getNames();
		out.write("(");
		for (int i = 0; i < types.length; ++i) {
			out.write(types[i] + " " + names[i]);
			if (i != types.length - 1) {
				out.write(", ");
			}
		}
		out.write(")");
	}

	private void writeMethod(Method m) throws IOException {
		out.write("\n\t@Override");
		out.write("\n\t");
		writeModifiers(m.getModifiers());
		Class<?> returnType = m.getReturnType();
		ArgumentsStrings arguments = new ArgumentsStrings(m);
		out.write(returnType.getSimpleName() + " " + m.getName());
		writeArguments(arguments);
		out.write(" {\n");
		writeReturn(returnType);
		out.write("\n\t}\n");
	}

	private void writeReturn(Class<?> returnType) throws IOException {
		if (!returnType.equals(void.class)) {
			out.write("\t\treturn ");
			if (returnType.isPrimitive()) {
				if (returnType.equals(int.class)
						|| returnType.equals(short.class)
						|| returnType.equals(char.class)
						|| returnType.equals(byte.class)
						|| returnType.equals(long.class)) {
					out.write("0");
				} else if (returnType.equals(float.class)
						|| returnType.equals(double.class)) {
					out.write("0.0");
				} else if (returnType.equals(boolean.class)) {
					out.write("false");
				}
			} else {
				out.write("null");
			}
			out.write(";");
		}
	}

	private void writeFooter() throws IOException {
		out.write("\n}");
	}

	private boolean isClass(Class<?> classEntry) {
		return !classEntry.isAnnotation() && !classEntry.isArray()
				&& !classEntry.isPrimitive() && !classEntry.isInterface()
				&& !classEntry.isEnum();
	}

	private String createExtentionString() {
		if (isClass) {
			return "extends " + this.superClassName;
		} else {
			return "implements " + this.superClassName;
		}
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("There must be 1 argument!\n");
		} else {
			try {
				new Implementor(args[0]).run();
			} catch (ImplementorException e) {
				System.err.println(e.getMessage());
			} catch (ClassNotFoundException e) {
				System.err.println("Class not found: " + e.getMessage());
			}
		}
	}

	private void run() {
		try {
			out = new FileWriter(outputFileName);
			try {
				writeClass();
			} finally {
				out.close();
			}
		} catch (IOException e) {
			System.err.println(IO_ERROR_MESSAGE + e.getMessage());
		}

	}
}
