package ru.ifmo.ctddev.rybak.reflect;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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

	private boolean isClass(Class<?> clazz) {
		return !clazz.isAnnotation() && !clazz.isArray()
				&& !clazz.isPrimitive() && !clazz.isInterface()
				&& !clazz.isEnum();
	}

	private void writeClass() throws IOException {
		writePackage();
		writeHeader();
		/* TODO add constructors processing */
		for (Method m : getMethods()) {
			writeMethod(m);
		}
		writeFooter();
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

	private String createExtentionString() {
		if (isClass) {
			return "extends " + this.superClassName;
		} else {
			return "implements " + this.superClassName;
		}
	}

	private Set<Method> getMethods() {
		if (isClass) {
			return extractAbstractMethodsFromClass(clazz);
		} else {
			return extractMethodsFromInterface(clazz);
		}
	}

	private Set<Method> extractAbstractMethodsFromClass(Class<?> cls) {
		/*
		 * TODO Ask question: What is better? This check or
		 * "assert isClass(cls) == true" ?
		 */
		if (!isClass(cls)) {
			throw new NotAClassImplementorError("extractMethodsFromClass", cls);
		}
		Set<Method> methods = new HashSet<Method>();
		List<Class<?>> classHierarchy = createClassHierarchy(cls);
		Set<Class<?>> interfaces = new HashSet<Class<?>>();
		for (Class<?> superClass : classHierarchy) {
			Class<?>[] superInterfaces = superClass.getInterfaces();
			for (Class<?> interfaze : superInterfaces) {
				interfaces.add(interfaze);
			}
		}
		for (Class<?> interfaze : interfaces) {
			Set<Method> interfaceMethods = extractMethodsFromInterface(interfaze);
			updateMethods(methods, interfaceMethods);
		}
		for (Class<?> superClass : classHierarchy) {
			listMethodsContainer superMethods = extractDeclaredMethods(superClass);
			updateMethods(methods, superMethods.abstractMethods);
			removeImplementedMethods(methods, superMethods.implementedMethods);
		}
		return methods;
	}

	private void removeImplementedMethods(Set<Method> methods,
			Collection<Method> implementedMethods) {
		for (Method candidate : implementedMethods) {
			iterateMethods: for (Method method : methods) {
				OverridingType overridingType = getOverridingType(method,
						candidate);
				switch (overridingType) {
				case OVERRIDES:
					methods.remove(method);
					break iterateMethods;
				case DOESNT_OVERRIDE:
					return;
				}
			}
		}
	}

	private List<Class<?>> createClassHierarchy(Class<?> cls) {
		if (!isClass(cls)) {
			throw new NotAClassImplementorError("createClassHierarchy", cls);
		}
		LinkedList<Class<?>> classes = new LinkedList<Class<?>>();
		Class<?> superClass = cls;
		do {
			classes.addFirst(superClass);
			superClass = superClass.getSuperclass();
		} while (superClass != null);
		return classes;
	}

	private Set<Method> extractMethodsFromInterface(Class<?> interfaze) {
		if (!interfaze.isInterface()) {
			throw new NotAInterfaceImplementorError(
					"extractMethodsFromInterface:\n\t", interfaze);
		}
		Set<Method> methods = new HashSet<Method>();
		List<Method> candidates = Arrays.asList(interfaze.getMethods());
		updateMethods(methods, candidates);
		return methods;
	}

	private enum OverridingType {
		OVERRIDES, DOESNT_OVERRIDE, DIFFERENT_SIGNATURE
	}

	private void updateMethods(Set<Method> methods,
			Collection<Method> candidates) {
		for (Method candidate : candidates) {
			updateMethods(methods, candidate);
		}
	}

	private void updateMethods(Set<Method> methods, Method candidate) {
		iterateMethods: for (Method method : methods) {
			OverridingType overridingType = getOverridingType(method, candidate);
			switch (overridingType) {
			case OVERRIDES:
				methods.remove(method);
				break iterateMethods;
			case DOESNT_OVERRIDE:
				return;
			}
		}
		methods.add(candidate);
	}

	private OverridingType getOverridingType(Method method, Method other) {
		String methodName = method.getName();
		String otherName = other.getName();
		if (methodName.equals(otherName)) {
			Class<?>[] parametrs = method.getParameterTypes();
			Class<?>[] otherParametrs = other.getParameterTypes();
			if (parametrs.length == otherParametrs.length) {
				for (int i = 0; i < parametrs.length; i++) {
					if (parametrs[i] != otherParametrs[i]) {
						return OverridingType.DIFFERENT_SIGNATURE;
					}
				}
				Class<?> returnType = method.getReturnType();
				Class<?> otherReturnType = other.getReturnType();
				if (returnType.isAssignableFrom(otherReturnType)) {
					return OverridingType.OVERRIDES;
				}
				return OverridingType.DOESNT_OVERRIDE;
			}
		}
		return OverridingType.DIFFERENT_SIGNATURE;
	}

	private class listMethodsContainer {
		public List<Method> abstractMethods, implementedMethods;
		public listMethodsContainer(List<Method> abstractMethods,
				List<Method> implementedMethods) {
			this.abstractMethods = abstractMethods;
			this.implementedMethods = implementedMethods;
		}
	}

	private listMethodsContainer extractDeclaredMethods(Class<?> clazz) {
		List<Method> abstractMethods = new ArrayList<Method>();
		List<Method> implementedMethods = new ArrayList<Method>();
		for (Method m : clazz.getDeclaredMethods()) {
			int mod = m.getModifiers();
			if (Modifier.isAbstract(mod)) {
				abstractMethods.add(m);
			} else {
				implementedMethods.add(m);
			}
		}
		return new listMethodsContainer(abstractMethods, implementedMethods);
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

	private void writeMethod(Method method) throws IOException {
		out.write("\n\t@Override");
		out.write("\n\t");
		writeModifiers(method.getModifiers());
		Class<?> returnType = method.getReturnType();
		out.write(returnType.getSimpleName() + " " + method.getName());
		ArgumentsStrings arguments = new ArgumentsStrings(method);
		writeArguments(arguments);
		out.write(" {\n");
		writeReturn(returnType);
		out.write("\n\t}\n");
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
