package ru.ifmo.ctddev.rybak.reflect;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
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
	final private static String DIRECTORY_SEPARATOR = System
			.getProperty("file.separator");
	final private static String IO_ERROR_MESSAGE = "An I/O error occurred: ";
	final private ClassImplExtractor classImplExtractor;
	private FileWriter out;

	public Implementor(String superClassName) throws ClassNotFoundException,
			ImplementorException {
		this.classImplExtractor = new ClassImplExtractor(superClassName);
	}

	private enum OverridingType {
		OVERRIDES, DOESNT_OVERRIDE, DIFFERENT_SIGNATURE
	}

	private class ClassImplExtractor {
		final public Collection<Method> methods;
		final public Collection<Constructor<?>> constructors;
		final public String extentionString;
		final public String outputFileName;
		final public String outputDirName;
		final public String className;
		final public String superClassName;
		final public String packageString;

		final private Class<?> clazz;
		final private boolean isClass;

		public ClassImplExtractor(String superClassName)
				throws ClassNotFoundException,
				CannotBeImplementedImplementorException {
			this.superClassName = superClassName;
			this.clazz = Class.forName(superClassName);
			if (Modifier.isFinal(clazz.getModifiers())) {
				throw new CannotBeImplementedImplementorException("Final class");
			}
			this.className = clazz.getSimpleName() + "Impl";
			this.outputDirName = "src"
					+ DIRECTORY_SEPARATOR
					+ clazz.getPackage().getName()
							.replace(".", DIRECTORY_SEPARATOR)
					+ DIRECTORY_SEPARATOR;
			this.outputFileName = className + ".java";
			if (isClass(clazz)) {
				isClass = true;
			} else {
				if (clazz.isInterface()) {
					isClass = false;
				} else {
					throw new CannotBeImplementedImplementorException(
							"Not a class or interface");
				}
			}
			this.packageString = createPackageString();
			this.extentionString = createExtentionString();
			this.constructors = extractConstructors();
			this.methods = new MethodsExtractor().extractMethods();

		}

		private String createPackageString() {
			Package clazzPackage = clazz.getPackage();
			if (clazzPackage != null) {
				return "package " + clazzPackage.getName() + ";";
			}
			return "";
		}

		private String createExtentionString() {
			if (isClass) {
				return "extends " + this.superClassName;
			} else {
				return "implements " + this.superClassName;
			}
		}

		private boolean isClass(Class<?> clazz) {
			return !clazz.isAnnotation() && !clazz.isArray()
					&& !clazz.isPrimitive() && !clazz.isInterface()
					&& !clazz.isEnum();
		}

		private Collection<Constructor<?>> extractConstructors()
				throws CannotBeImplementedImplementorException {
			List<Constructor<?>> constructors = new ArrayList<Constructor<?>>();
			boolean hasNotPrivateConstructors = false;
			boolean hasPrivateConstructors = false;
			Constructor<?>[] candidates = clazz.getDeclaredConstructors();
			for (Constructor<?> constructor : candidates) {
				if (!Modifier.isPrivate(constructor.getModifiers())) {
					hasNotPrivateConstructors = true;
					constructors.add(constructor);
				} else {
					hasPrivateConstructors = true;
				}
			}
			if (hasPrivateConstructors && !hasNotPrivateConstructors) {
				throw new CannotBeImplementedImplementorException("'" + clazz
						+ "' has only private constructors.");
			}
			return constructors;
		}

		private class MethodsExtractor {

			public Collection<Method> extractMethods()
					throws CannotBeImplementedImplementorException {
				if (isClass) {
					return extractAbstractMethodsFromClass(clazz);
				} else {
					return extractMethodsFromInterface(clazz);
				}
			}

			private Set<Method> extractAbstractMethodsFromClass(Class<?> cls)
					throws CannotBeImplementedImplementorException {
				assert isClass(cls);
				List<Class<?>> classHierarchy = createClassHierarchy(cls);
				Set<Class<?>> interfaces = extractInterfacesFromClassHierarchy(classHierarchy);
				Set<Method> methods = new HashSet<Method>();
				for (Class<?> interfaze : interfaces) {
					Set<Method> interfaceMethods = extractMethodsFromInterface(interfaze);
					updateMethods(methods, interfaceMethods);
				}
				for (Class<?> superClass : classHierarchy) {
					ListMethodsContainer superMethods = extractDeclaredMethods(superClass);
					updateMethods(methods, superMethods.abstractMethods);
					removeImplementedMethods(methods,
							superMethods.implementedMethods);
				}
				return methods;
			}

			private List<Class<?>> createClassHierarchy(Class<?> cls) {
				assert isClass(cls);
				LinkedList<Class<?>> classes = new LinkedList<Class<?>>();
				Class<?> superClass = cls;
				do {
					classes.addFirst(superClass);
					superClass = superClass.getSuperclass();
				} while (superClass != null);
				return classes;
			}

			private Set<Class<?>> extractInterfacesFromClassHierarchy(
					List<Class<?>> classHierarchy) {
				Set<Class<?>> interfaces = new HashSet<Class<?>>();
				for (Class<?> superClass : classHierarchy) {
					Class<?>[] superInterfaces = superClass.getInterfaces();
					for (Class<?> interfaze : superInterfaces) {
						interfaces.add(interfaze);
					}
				}
				return interfaces;
			}

			private Set<Method> extractMethodsFromInterface(Class<?> interfaze)
					throws CannotBeImplementedImplementorException {
				assert interfaze.isInterface();
				Set<Method> methods = new HashSet<Method>();
				List<Method> candidates = Arrays.asList(interfaze.getMethods());
				updateMethods(methods, candidates);
				return methods;
			}

			private void updateMethods(Set<Method> methods,
					Collection<Method> candidates)
					throws CannotBeImplementedImplementorException {
				for (Method candidate : candidates) {
					updateMethods(methods, candidate);
				}
			}

			private void updateMethods(Set<Method> methods, Method candidate)
					throws CannotBeImplementedImplementorException {
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
				methods.add(candidate);
			}

			private OverridingType getOverridingType(Method method, Method other)
					throws CannotBeImplementedImplementorException {
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
						} else {
							if (!otherReturnType.isAssignableFrom(returnType)) {
								throw new CannotBeImplementedImplementorException(
										"The return types are incompatible for the methods:\n\t"
												+ method + "\n\t" + other
												+ "\n");
							}
						}
						return OverridingType.DOESNT_OVERRIDE;
					}
				}
				return OverridingType.DIFFERENT_SIGNATURE;
			}

			private class ListMethodsContainer {
				public List<Method> abstractMethods, implementedMethods;

				public ListMethodsContainer(List<Method> abstractMethods,
						List<Method> implementedMethods) {
					this.abstractMethods = abstractMethods;
					this.implementedMethods = implementedMethods;
				}
			}

			private ListMethodsContainer extractDeclaredMethods(Class<?> clazz) {
				List<Method> abstractMethods = new ArrayList<Method>();
				List<Method> implementedMethods = new ArrayList<Method>();
				for (Method method : clazz.getDeclaredMethods()) {
					int mod = method.getModifiers();
					if (Modifier.isAbstract(mod)) {
						abstractMethods.add(method);
					} else {
						implementedMethods.add(method);
					}
				}
				return new ListMethodsContainer(abstractMethods,
						implementedMethods);
			}

			private void removeImplementedMethods(Set<Method> methods,
					Collection<Method> implementedMethods)
					throws CannotBeImplementedImplementorException {
				for (Method candidate : implementedMethods) {
					iterateMethods: for (Method method : methods) {
						OverridingType overridingType = getOverridingType(
								method, candidate);
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
		}

	}

	private class ClassImplWriter {
		final private FileWriter out;
		final private ClassImplExtractor classExtractor;

		public ClassImplWriter(FileWriter out, ClassImplExtractor classExtractor) {
			this.out = out;
			this.classExtractor = classExtractor;
		}

		public void writeClass() throws IOException {
			out.write(classExtractor.packageString + "\n");
			writeHeader();
			writeConstructors();
			for (Method method : classExtractor.methods) {
				writeMethod(method);
			}
			writeFooter();
		}

		private void writeHeader() throws IOException {
			out.write("public class " + classExtractor.className + " "
					+ classExtractor.extentionString + " {\n");
		}

		private void writeConstructors() throws IOException {
			for (Constructor<?> constructor : classExtractor.constructors) {
				writeConstructor(constructor);
			}
		}

		private void writeConstructor(Constructor<?> constructor)
				throws IOException {
			out.write("\n\t");
			writeModifiers(constructor.getModifiers());
			out.write(classExtractor.className);
			ArgumentsStrings arguments = new ArgumentsStrings(
					constructor.getParameterTypes(), constructor.isVarArgs());
			writeHeaderArguments(arguments);
			writeExceptions(constructor);
			out.write(" {\n");
			writeSuper(arguments);
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

		private class ArgumentsStrings {
			final private String[] types;
			final private String[] names;

			public ArgumentsStrings(Class<?>[] parametrs, boolean isVarArgs) {
				this.types = new String[parametrs.length];
				this.names = new String[parametrs.length];
				if (parametrs.length > 0) {
					int n = types.length - (isVarArgs ? 1 : 0);
					for (int i = 0; i < n; ++i) {
						types[i] = createTypeName(parametrs[i]);
						names[i] = createVarName(parametrs[i]) + i;
					}
					if (isVarArgs) {
						types[n] = createTypeName(parametrs[n]
								.getComponentType()) + "...";
						names[n] = "arguments" + n;
					}
				}
			}

			private String createTypeName(Class<?> type) {
				int dim = 0;
				while (type.isArray()) {
					type = type.getComponentType();
					dim++;
				}
				StringBuffer sb = new StringBuffer(type.getName());
				for (int j = 0; j < dim; ++j) {
					sb.append("[]");
				}
				return sb.toString();
			}

			private String createVarName(Class<?> type) {
				while (type.isArray()) {
					type = type.getComponentType();
				}
				String name = type.getSimpleName();
				return Character.toLowerCase(name.charAt(0))
						+ name.substring(1);
			}

			public String[] getTypes() {
				return types;
			}

			public String[] getNames() {
				return names;
			}
		}

		private void writeHeaderArguments(ArgumentsStrings arguments)
				throws IOException {
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

		private void writeExceptions(Constructor<?> constructor)
				throws IOException {
			Class<?>[] constructorThrows = constructor.getExceptionTypes();
			if (constructorThrows.length > 0) {
				out.write(" throws ");
			}
			for (int i = 0; i < constructorThrows.length; ++i) {
				out.write(constructorThrows[i].getName());
				if (i != constructorThrows.length - 1) {
					out.write(", ");
				}
			}
		}

		private void writeSuper(ArgumentsStrings arguments) throws IOException {
			out.write("\t\t");
			out.write("super");
			writeArguments(arguments);
			out.write(";");
		}

		private void writeMethod(Method method) throws IOException {
			out.write("\n\t");
			writeModifiers(method.getModifiers());
			Class<?> returnType = method.getReturnType();
			out.write(returnType.getName() + " " + method.getName());
			ArgumentsStrings arguments = new ArgumentsStrings(
					method.getParameterTypes(), method.isVarArgs());
			writeHeaderArguments(arguments);
			out.write(" {\n");
			writeReturn(returnType);
			out.write("\n\t}\n");
		}

		private void writeArguments(ArgumentsStrings arguments)
				throws IOException {
			String[] names = arguments.getNames();
			out.write("(");
			for (int i = 0; i < names.length; ++i) {
				out.write(names[i]);
				if (i != names.length - 1) {
					out.write(", ");
				}
			}
			out.write(")");
		}

		private void writeReturn(Class<?> returnType) throws IOException {
			if (!returnType.equals(void.class)) {
				out.write("\t\treturn ");
				if (returnType.isPrimitive()) {
					if (returnType.equals(boolean.class)) {
						out.write("false");
					} else {
						out.write("0");
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
			File file = new File(classImplExtractor.outputDirName
					+ classImplExtractor.outputFileName);
			out = new FileWriter(file);
			try {
				ClassImplWriter classWriter = new ClassImplWriter(out,
						this.classImplExtractor);
				classWriter.writeClass();
			} finally {
				out.close();
			}
		} catch (IOException e) {
			System.err.println(IO_ERROR_MESSAGE + e.getMessage());
		}
	}
}
