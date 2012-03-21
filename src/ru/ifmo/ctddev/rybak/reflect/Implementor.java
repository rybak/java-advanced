package ru.ifmo.ctddev.rybak.reflect;

import java.util.List;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class Implementor {
	final private static String IO_ERROR_MESSAGE = "Implementor:\nAn I/O error occurred: ";
	final private String outputFileName;
	private FileWriter out;

	final private String className;
	final private String superClassName;
	final private Class<?> clazz;
	final private boolean isClass;

	public Implementor(String superClassName) throws ImplementorException,
			ClassNotFoundException {
		this.clazz = Class.forName(superClassName);
		if (isClass(clazz)) {
			isClass = true;
		} else {
			if (clazz.isInterface()) {
				isClass = false;
			} else {
				throw new ImplementorException("Not a class or interface\n");
			}
		}
		this.superClassName = superClassName;
		this.className = clazz.getSimpleName() + "Impl";
		this.outputFileName = className + ".java";
	}

	private void writeClass() throws IOException {
		writePackage();
		writeHeader();
		for (Method m : getMethods()) {
			writeMethod(m);
		}
		writeFooter();
	}

	private Set<Method> getMethods() {
		Set<Method> methods = new HashSet<Method>();
		if (isClass) {
			Deque<Class<?>> Hierarchy = getClassHierarchy(clazz);
			for (Class<?> superClass : Hierarchy) {
				Class<?>[] interfaces = superClass.getInterfaces();
				for (Class<?> interfaze : interfaces) {
					System.err.println("getMethods : " + interfaze);
					List<Method> interfaceMethods = extractMethodsFromInterface(interfaze);
					updateMethods(methods, interfaceMethods);
				}
			}
			for (Class<?> superClass : Hierarchy) {
				System.err.println("getMethods : " + superClass);
				List<Method> superMethods = extractMethodsFromClass(superClass);
				updateMethods(methods, superMethods);
			}
		} else {
			methods.addAll(extractMethodsFromInterface(clazz));
		}
		return methods;
	}

	private void updateMethods(Collection<Method> methods,
			Collection<Method> superMethods) {
		Set<Method> overridedMethods = new HashSet<Method>();
		for (Method sm : superMethods) {
			for (Method m : methods) {
				if (isOverrided(m, sm)) {
					overridedMethods.add(m);
				}
			}
		}
		methods.removeAll(overridedMethods);
		methods.addAll(superMethods);
	}

	private Deque<Class<?>> getClassHierarchy(Class<?> clazz) {
		Deque<Class<?>> classes = new LinkedList<Class<?>>();
		Class<?> superClass = clazz;
		do {
			classes.addFirst(superClass);
			superClass = superClass.getSuperclass();
		} while (superClass != null);
		return classes;
	}

	private List<Method> extractMethodsFromClass(Class<?> cls) {
		if (!isClass(cls)) {
			throw new NotAClassImplementorError("extractMethodsFromClass", cls);
		}
		return extractMethods(cls);
	}

	private List<Method> extractMethodsFromInterface(Class<?> interfaze) {
		if (!interfaze.isInterface()) {
			throw new NotAInterfaceImplementorError(
					"extractMethodsFromInterface:\n\t", interfaze);
		}
		List<Class<?>> interfaces = new ArrayList<Class<?>>(
				Arrays.asList(interfaze.getInterfaces()));
		interfaces.add(interfaze);
		List<Method> methods = new ArrayList<Method>();

		for (Class<?> i : interfaces) {
			methods.addAll(extractMethods(i));
		}
		return methods;
	}

	private List<Method> extractMethods(Class<?> clazz) {
		List<Method> methods = new ArrayList<Method>();
		for (Method m : clazz.getDeclaredMethods()) {
			int mod = m.getModifiers();
			if (Modifier.isAbstract(mod)) {
				methods.add(m);
			}
		}
		return methods;
	}

	private boolean isOverrided(Method method, Method other) {
		String methodName = method.getName();
		String otherName = other.getName();
		if (methodName.equals(otherName)) {
			Class<?>[] parametrs = method.getParameterTypes();
			Class<?>[] otherParametrs = other.getParameterTypes();
			if (parametrs.length == otherParametrs.length) {
				for (int i = 0; i < parametrs.length; i++) {
					if (parametrs[i] != otherParametrs[i]) {
						return false;
					}
				}
				Class<?> returnType = method.getReturnType();
				Class<?> otherReturnType = other.getReturnType();
				if (returnType.isAssignableFrom(otherReturnType)) {
					return false;
				}
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unused")
	private List<Class<?>> getInterfaces(Class<?> clazz) {
		// TODO interfaces
		return null;
	}

	private void writePackage() throws IOException {
		Package clazzPackage = clazz.getPackage();
		if (clazzPackage != null) {
			out.write("package " + clazzPackage.getName() + ";\n\n");
		}
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
