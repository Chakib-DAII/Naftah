package org.daiitech.naftah.utils.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.daiitech.naftah.builtin.NaftahFn;
import org.daiitech.naftah.builtin.NaftahFnProvider;
import org.daiitech.naftah.builtin.lang.BuiltinFunction;
import org.daiitech.naftah.builtin.lang.JvmClassInitializer;
import org.daiitech.naftah.builtin.lang.JvmFunction;
import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.utils.arabic.ArabicUtils;

import static org.daiitech.naftah.Naftah.UNDERSCORE;
import static org.daiitech.naftah.builtin.utils.AliasHashMap.toAliasGroupedByName;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;
import static org.daiitech.naftah.utils.reflect.AnnotationsUtils.getNaftahFunctionAnnotation;
import static org.daiitech.naftah.utils.reflect.AnnotationsUtils.getNaftahFunctionProviderAnnotation;
import static org.daiitech.naftah.utils.reflect.AnnotationsUtils.isAnnotationsPresent;

/**
 * Utility class for working with Java classes, methods, and their qualified names.
 * Provides methods to get qualified names, filter classes and methods, and
 * obtain built-in methods annotated for Naftah.
 *
 * @author Chakib Daii
 */
public final class ClassUtils {

	/**
	 * Regex pattern to split class names by dot '.' or dollar '$' sign.
	 */
	public static final String CLASS_SEPARATORS_REGEX = "[.$]";
	/**
	 * Separator used to join qualified names.
	 */
	public static final String QUALIFIED_NAME_SEPARATOR = ":";
	/**
	 * Separator used to join qualified calls (class::method).
	 */
	public static final String QUALIFIED_CALL_SEPARATOR = "::";
	/**
	 * Regex to match strings with at least one ':' and exactly one '::', with '::' before the last segment.
	 */
	public static final Pattern QUALIFIED_CALL_REGEX = Pattern.compile("^[^:]*::(?:[^:]*:)+[^:]*$");


	/**
	 * Commonly recognized <b>factory-style static method names</b> in Java.
	 *
	 * <p>These names are used by {@code hasFactoryMethod(Class<?>)} to detect
	 * whether a class provides a static creation method that returns instances
	 * of itself or its subclasses. This allows Naftah to treat such methods as
	 * "constructors" in its interop layer.</p>
	 *
	 * <p>The list includes canonical JDK factories (e.g., {@code of}, {@code from}),
	 * builder-style factories, parser/deserializer names, and provider patterns
	 * from the JDK, Guava, Jackson, and Spring ecosystems.</p>
	 */
	private static final Set<String> FACTORY_NAMES = Set
			.of(
				"of",
				"ofNullable",
				"from",
				"valueOf",
				"newInstance",
				"instance",
				"getInstance",
				"empty",
				"singleton",
				"copyOf",
				"unmodifiable",
				"immutable",
				"noneOf",
				"anyOf",
				"allOf",
				"builder",
				"build",
				"newBuilder",
				"parse",
				"parseFrom",
				"parseValue",
				"decode",
				"encode",
				"forName",
				"fromString",
				"fromJson",
				"fromXml",
				"open",
				"load",
				"read",
				"connect",
				"acquire",
				"create",
				"createFrom",
				"createInstance",
				"getDefault",
				"defaultInstance",
				"defaultFactory",
				"provide",
				"supply",
				"with",
				"withDefaults",
				"using",
				"usingDefaults",
				"usingFactory",
				"usingProvider"
			);
	/**
	 * Common prefixes used to identify <b>factory-like methods</b> when scanning class members.
	 *
	 * <p>Unlike {@link #FACTORY_NAMES}, these are matched as prefixes,
	 * so methods such as {@code newBuilder()}, {@code createStream()},
	 * {@code fromJsonString()}, and {@code getInstanceSafe()} are also recognized.</p>
	 *
	 * <p>This prefix-based detection is useful for frameworks that
	 * generate descriptive factory names dynamically or through code generation.</p>
	 */
	private static final String[] FACTORY_PREFIXES = {
														"new",
														"create",
														"from",
														"get",
														"of",
														"build",
														"parse",
														"with",
														"using"
	};

	/**
	 * Private constructor to prevent instantiation.
	 * Always throws a {@link NaftahBugError} when called.
	 */
	private ClassUtils() {
		throw newNaftahBugInvalidUsageError();
	}

	/**
	 * Returns a qualified name by transliterating each part of the class name to Arabic.
	 * For example, "java.lang.String" becomes Arabic transliteration joined by colon.
	 *
	 * @param className the fully qualified Java class name
	 * @return the qualified name transliterated to Arabic script
	 */
	public static String getQualifiedName(String className) {
		return String
				.join(  QUALIFIED_NAME_SEPARATOR,
						ArabicUtils.transliterateToArabicScriptDefault(className.split(CLASS_SEPARATORS_REGEX)));
	}

	/**
	 * Returns a qualified method call string by combining the qualified class name
	 * and the method name, transliterated into Arabic script.
	 * <p>
	 * The format of the returned string is: {@code qualifiedName::methodName},
	 * where the method name is transliterated.
	 *
	 * @param qualifiedName the fully qualified name of the class (e.g., {@code com.example.MyClass})
	 * @param method        the {@link Method} instance representing the method
	 * @return a string in the form {@code qualifiedName::methodName} with the method name in Arabic script
	 */
	public static String getQualifiedCall(String qualifiedName, Method method) {
		return "%s::%s"
				.formatted(qualifiedName, ArabicUtils.transliterateToArabicScriptDefault(method.getName())[0]);
	}

	/**
	 * Returns a qualified method call string by combining the qualified class name
	 * and the method name, transliterated into Arabic script.
	 * <p>
	 * The format of the returned string is: {@code qualifiedName::methodName},
	 * where the method name is transliterated.
	 *
	 * @param qualifiedName the fully qualified name of the class (e.g., {@code com.example.MyClass})
	 * @param methodName    the name of the method as a string
	 * @return a string in the form {@code qualifiedName::methodName} with the method name in Arabic script
	 */
	public static String getQualifiedCall(String qualifiedName, String methodName) {
		return "%s::%s"
				.formatted(qualifiedName, ArabicUtils.transliterateToArabicScriptDefault(methodName)[0]);
	}


	/**
	 * Extracts class parts from the input class names and returns a map where
	 * keys are either individual parts (flattened = true) or full qualified names
	 * (flattened = false), and values are the array of parts.
	 *
	 * @param classNames set of fully qualified class names
	 * @param flattened  if true, returns flattened map of each part; if false,
	 *                   returns map keyed by full qualified name
	 * @return map of class parts or qualified names to their parts
	 */
	public static Map<String, String[]> getClassQualifiers(Set<String> classNames, boolean flattened) {
		var baseStream = classNames.stream().map(s -> s.split(CLASS_SEPARATORS_REGEX));

		if (flattened) {
			return baseStream
					.flatMap(strings -> Arrays.stream(strings).map(element -> Map.entry(element, strings)))
					.collect(
								Collectors
										.toMap( Map.Entry::getKey,
												Map.Entry::getValue,
												(existing, replacement) -> existing
										));
		}

		return baseStream
				.map(strings -> Map.entry(String.join(QUALIFIED_NAME_SEPARATOR, strings), strings))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	/**
	 * Transliterates a collection of class qualifiers parts arrays into Arabic
	 * qualified names.
	 *
	 * @param classQualifiers collection of class parts arrays
	 * @return set of Arabic transliterated qualified names joined by colon
	 */
	public static Set<String> getArabicClassQualifiers(Collection<String[]> classQualifiers) {
		return classQualifiers
				.stream()
				.map(strings -> String
						.join(QUALIFIED_NAME_SEPARATOR, ArabicUtils.transliterateToArabicScriptDefault(strings)))
				.collect(Collectors.toSet());
	}

	/**
	 * Returns a map from Arabic transliterated qualified class names to the
	 * original qualified names.
	 *
	 * @param classQualifiers collection of class parts arrays
	 * @return map of Arabic qualified names to original qualified names
	 */
	public static Map<String, String> getArabicClassQualifiersMapping(Collection<String[]> classQualifiers) {
		return classQualifiers
				.stream()
				.map(strings -> Map
						.entry(
								String
										.join(  QUALIFIED_NAME_SEPARATOR,
												ArabicUtils.transliterateToArabicScriptDefault(strings.clone())),
								String.join(QUALIFIED_NAME_SEPARATOR, strings.clone())))
				.collect(Collectors
						.toMap( Map.Entry::getKey,
								Map.Entry::getValue,
								(existing, replacement) -> existing));
	}


	/**
	 * Retrieves methods from given classes filtered by a predicate, grouped by their qualified call strings.
	 * <p>
	 * Usage example:
	 * <pre>
	 * {@link Set}&lt;{@link Class}&gt; classes = {@link ClassUtils}.getClasses();
	 * {@link Predicate}&lt;{@link Method}&gt; instancePublicMethodPredicate = method ->
	 * {@link Modifier}.isPublic(method.getModifiers());
	 * {@link Map}&lt;{@link Class}, {@link Method}[]&gt; instancePublicMethods =
	 * {@link ClassUtils}.getClassMethods(classes,
	 * instancePublicMethodPredicate);
	 * </pre>
	 *
	 * @param classes         map of qualified class names to Class objects
	 * @param methodPredicate predicate to filter methods
	 * @return map from qualified call string to list of JvmFunction wrappers
	 */
	public static Map<String, List<JvmFunction>> getClassMethods(   Map<String, Class<?>> classes,
																	Predicate<Method> methodPredicate) {
		return classes.entrySet().stream().filter(Objects::nonNull).flatMap(classEntry -> {
			try {
				return Arrays
						.stream(classEntry.getValue().getMethods())
						.filter(methodPredicate)
						.map(method -> Map.entry(method, classEntry));
			}
			catch (Throwable e) {
				// skip
				return null;
			}
		}).filter(Objects::nonNull).map(methodEntry -> {
			Class<?> clazz = methodEntry.getValue().getValue();
			Method method = methodEntry.getKey();
			String qualifiedCall = getQualifiedCall(methodEntry.getValue().getKey(), method);
			return Map.entry(qualifiedCall, JvmFunction.of(qualifiedCall, clazz, method));
		})
				.collect(
							Collectors
									.groupingBy(Map.Entry::getKey,
												Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
	}

	/**
	 * Retrieves and groups constructors from multiple classes, filtered by a specified predicate,
	 * mapping each qualified class name to a list of {@link JvmClassInitializer} wrappers.
	 * <p>
	 * For each class in the provided map, this method inspects its public constructors (via
	 * {@link Class#getConstructors()}), applies the given {@code constructorPredicate}, and wraps
	 * all matching constructors as {@link JvmClassInitializer} instances. The results are then grouped
	 * by their qualified class names.
	 * <p>
	 * Any classes or constructors that cannot be accessed or processed (e.g., due to security restrictions)
	 * are silently skipped.
	 *
	 * @param classes              a map where each key is the fully qualified class name
	 *                             (e.g., {@code "com.example.MyClass"}) and each value is the corresponding
	 *                             {@link Class} object
	 * @param constructorPredicate a {@link Predicate} used to filter which constructors should be included
	 * @return a map where each key is a qualified class name and each value is a list of
	 *         {@link JvmClassInitializer} instances representing all constructors of that class
	 *         that satisfy the given predicate
	 * @throws NullPointerException if {@code classes} or {@code constructorPredicate} is {@code null}
	 * @see Class#getConstructors()
	 * @see JvmClassInitializer
	 */
	public static Map<String, List<JvmClassInitializer>> getClassConstructors(  Map<String, Class<?>> classes,
																				Predicate<Constructor<?>> constructorPredicate) {
		return classes.entrySet().stream().filter(Objects::nonNull).flatMap(classEntry -> {
			try {
				return Arrays
						.stream(classEntry.getValue().getConstructors())
						.filter(constructorPredicate)
						.map(constructor -> Map.entry(constructor, classEntry));
			}
			catch (Throwable e) {
				// skip
				return null;
			}
		}).filter(Objects::nonNull).map(methodEntry -> {
			Class<?> clazz = methodEntry.getValue().getValue();
			Constructor<?> constructor = methodEntry.getKey();
			String qualifiedName = methodEntry.getValue().getKey();
			return Map.entry(qualifiedName, JvmClassInitializer.of(qualifiedName, clazz, constructor));
		})
				.collect(
							Collectors
									.groupingBy(Map.Entry::getKey,
												Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
	}

	/**
	 * Retrieves all methods from a single class wrapped as JvmFunction instances.
	 *
	 * @param qualifiedName qualified name of the class
	 * @param clazz         the Class object
	 * @return list of JvmFunction wrapping all class methods
	 */
	public static List<JvmFunction> getClassMethods(String qualifiedName, Class<?> clazz) {
		return Arrays.stream(clazz.getMethods()).map(method -> {
			String qualifiedCall = getQualifiedCall(qualifiedName, method);
			return JvmFunction.of(qualifiedCall, clazz, method);
		}).toList();
	}

	/**
	 * Retrieves all public constructors of the specified class and wraps them
	 * as {@link JvmClassInitializer} instances.
	 * <p>
	 * Each {@code JvmClassInitializer} represents a single constructor
	 * and includes its qualified name and reflective metadata.
	 *
	 * @param qualifiedName the fully qualified name of the class
	 *                      (e.g., "com.example.MyClass")
	 * @param clazz         the {@link Class} object representing the class whose constructors
	 *                      should be retrieved
	 * @return a list of {@link JvmClassInitializer} instances representing all public
	 *         constructors of the specified class
	 */
	public static List<JvmClassInitializer> getClassConstructors(String qualifiedName, Class<?> clazz) {
		return Arrays
				.stream(clazz.getConstructors())
				.map(constructor -> JvmClassInitializer.of(qualifiedName, clazz, constructor))
				.toList();
	}


	/**
	 * Returns all methods from given classes without filtering.
	 *
	 * @param classes map of qualified class names to Class objects
	 * @return map from qualified call string to list of JvmFunction wrappers
	 */
	public static Map<String, List<JvmFunction>> getClassMethods(Map<String, Class<?>> classes) {
		return getClassMethods(classes, (method) -> true);
	}

	/**
	 * Retrieves all public constructors from the given classes without applying any filtering,
	 * mapping each qualified class name to a list of {@link JvmClassInitializer} wrappers.
	 * <p>
	 * This method is a convenience overload of
	 * {@link #getClassConstructors(Map, java.util.function.Predicate)} that includes
	 * all available constructors by default.
	 *
	 * @param classes a map where each key is the fully qualified class name
	 *                (e.g., {@code "com.example.MyClass"}) and each value is the corresponding
	 *                {@link Class} object
	 * @return a map from qualified class names to lists of {@link JvmClassInitializer} instances
	 *         representing all public constructors of the given classes
	 * @throws NullPointerException if {@code classes} is {@code null}
	 * @see #getClassConstructors(Map, java.util.function.Predicate)
	 * @see JvmClassInitializer
	 */
	public static Map<String, List<JvmClassInitializer>> getClassConstructors(Map<String, Class<?>> classes) {
		return getClassConstructors(classes, (constructor) -> true);
	}

	/**
	 * Checks if the class has at least one public static factory method.
	 *
	 * @param clazz the class to check
	 * @return true if accessible (has public static method), false otherwise
	 */
	public static boolean isAccessibleClass(Class<?> clazz) {
		// Try to find a public static factory method returning the same type
		for (Method method : clazz.getDeclaredMethods()) {
			if (Modifier.isStatic(method.getModifiers()) && Modifier.isPublic(method.getModifiers())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if the class is instantiable: public, not abstract/interface,
	 * and has a public no-arg constructor.
	 *
	 * @param clazz the class to check
	 * @return true if instantiable, false otherwise
	 */
	public static boolean isInstantiableClass(Class<?> clazz) {
		// Must be public
		if (!Modifier.isPublic(clazz.getModifiers())) {
			return false;
		}

		// Cannot be abstract or an interface
		if (Modifier.isAbstract(clazz.getModifiers()) || clazz.isInterface()) {
			return false;
		}

		// Try to find a public no-arg constructor
		try {
			Constructor<?> constructor = clazz.getConstructor();
			return Modifier.isPublic(constructor.getModifiers());
		}
		catch (NoSuchMethodException ignored) {
			// check if the class has any public constructor
			var constructors = clazz.getConstructors();
			if (constructors.length == 0) {
				return hasFactoryMethod(clazz);
			}
			else {
				return Arrays
						.stream(constructors)
						.anyMatch(constructor -> Modifier.isPublic(constructor.getModifiers()));
			}
		}
	}

	/**
	 * Determines whether the specified class defines at least one
	 * <b>public static factory method</b> capable of producing
	 * an instance of that class (or a subclass of it).
	 *
	 * <p>This method is used by the Naftah Java interop layer to identify
	 * Java types that are constructible through factory methods rather
	 * than public constructors. Examples include:
	 * {@code Optional.of()}, {@code LocalDate.parse()}, and
	 * {@code List.copyOf()}.</p>
	 *
	 * <p>The detection relies on a curated set of known factory method names
	 * and prefixes (see {@code FACTORY_NAMES} and {@code FACTORY_PREFIXES}).
	 * The following conditions must all be satisfied for a method to be
	 * recognized as a valid factory:</p>
	 *
	 * <ul>
	 * <li>The method must be {@code public static}.</li>
	 * <li>It must not be a compiler-generated bridge or synthetic method.</li>
	 * <li>Its name must either match a known factory name
	 * (e.g. {@code of}, {@code from}, {@code valueOf})
	 * or begin with a recognized prefix (e.g. {@code create}, {@code new}, {@code build}).</li>
	 * <li>It must return a non-{@code void} type assignable to the declaring class,
	 * or whose simple name contains the declaring class’s simple name
	 * (to support builder/wrapper patterns).</li>
	 * </ul>
	 *
	 * <p>Example classes detected as factory-capable:</p>
	 * <pre>{@code
	 * Optional.of("value");
	 * LocalDate.parse("2025-11-07");
	 * List.copyOf(existingList);
	 * EnumSet.noneOf(MyEnum.class);
	 * HttpRequest.newBuilder().build();
	 * }</pre>
	 *
	 * <p>Classes that are primitives, arrays, or {@code void} are excluded immediately.</p>
	 *
	 * @param clazz the class to inspect
	 * @return {@code true} if the class declares at least one public static
	 *         factory method that produces instances of itself or a compatible type;
	 *         {@code false} otherwise
	 */
	public static boolean hasFactoryMethod(Class<?> clazz) {
		// Skip primitive, void, and array types
		if (clazz.isPrimitive() || clazz.isArray() || clazz == void.class) {
			return false;
		}

		for (Method method : clazz.getDeclaredMethods()) {
			int mods = method.getModifiers();

			// Must be public static
			if (!Modifier.isStatic(mods) || !Modifier.isPublic(mods)) {
				continue;
			}

			// Ignore compiler-generated methods
			if (method.isSynthetic() || method.isBridge()) {
				continue;
			}

			String name = method.getName();

			// Quick reject by name
			if (!isFactoryName(name)) {
				continue;
			}

			// Must return something usable
			Class<?> returnType = method.getReturnType();
			if (returnType == void.class) {
				continue;
			}

			// Covariant return allowed: subclass or same type
			if (clazz.isAssignableFrom(returnType)) {
				return true;
			}

			// Optional: also consider wrapper or builder factories
			if (returnType.getSimpleName().contains(clazz.getSimpleName())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Determines whether a given method name matches a known
	 * factory method pattern.
	 *
	 * <p>This method checks whether the name appears in the
	 * {@link #FACTORY_NAMES} set or begins with one of the
	 * {@link #FACTORY_PREFIXES}. Matching is case-sensitive
	 * and uses simple string comparison for efficiency.</p>
	 *
	 * @param name the method name to test
	 * @return {@code true} if the name matches a known factory name or prefix,
	 *         {@code false} otherwise
	 */
	private static boolean isFactoryName(String name) {
		if (FACTORY_NAMES.contains(name)) {
			return true;
		}

		for (String prefix : FACTORY_PREFIXES) {
			if (name.startsWith(prefix)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks if a method is static.
	 *
	 * @param method the method to check
	 * @return true if static, false otherwise
	 */
	public static boolean isStatic(Method method) {
		// If it's a static method, it's invocable directly
		return Modifier.isStatic(method.getModifiers());
	}

	/**
	 * Determines whether a given {@link Executable} (method or constructor) can be invoked dynamically.
	 *
	 * <p>A member is considered <em>invocable</em> if it meets all of the following conditions:</p>
	 * <ul>
	 * <li>It is {@code public}.</li>
	 * <li>It is not synthetic or compiler-generated (e.g., bridge methods).</li>
	 * <li>If it is a {@link Method}, it is either {@code static} or declared in an instantiable class.</li>
	 * <li>If it is a {@link Constructor}, it belongs to an instantiable class.</li>
	 * </ul>
	 *
	 * @param methodOrConstructor the {@link Executable} (method or constructor) to check
	 * @return {@code true} if the executable can be invoked via reflection, otherwise {@code false}
	 * @see Method#isBridge()
	 * @see Executable#isSynthetic()
	 * @see Modifier#isPublic(int)
	 */
	public static boolean isInvocable(Executable methodOrConstructor) {
		// Must be public
		if (!Modifier.isPublic(methodOrConstructor.getModifiers())) {
			return false;
		}

		// Ignore synthetic or bridge methods (compiler-generated)
		if (methodOrConstructor.isSynthetic() || (methodOrConstructor instanceof Method method && method.isBridge())) {
			return false;
		}

		Class<?> declaringClass = methodOrConstructor.getDeclaringClass();

		// If it's a static method, it's invocable directly
		if (methodOrConstructor instanceof Method method && isStatic(method)) {
			return true;
		}

		// For instance methods: check the declaring class is instantiable
		return isInstantiableClass(declaringClass);
	}

	/**
	 * Filters a map of classes by applying a predicate on the Class objects.
	 *
	 * @param classes        map of qualified class names to Class objects
	 * @param classPredicate predicate to filter classes
	 * @return filtered map of classes
	 */
	public static Map<String, Class<?>> filterClasses(  Map<String, Class<?>> classes,
														Predicate<Class<?>> classPredicate) {
		return classes.entrySet().stream().filter(classEntry -> {
			try {
				return classPredicate.test(classEntry.getValue());
			}
			catch (Throwable e) {
				// skip
				return false;
			}
		}).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	/**
	 * Retrieves built-in methods annotated with @NaftahFn from classes annotated
	 * with @NaftahFnProvider, filtered by a method predicate.
	 *
	 * @param classes         map of qualified class names to Class objects
	 * @param methodPredicate predicate to filter methods
	 * @return map of function names to lists of BuiltinFunction instances
	 */
	public static Map<String, List<BuiltinFunction>> getBuiltinMethods( Map<String, Class<?>> classes,
																		Predicate<Method> methodPredicate) {
		return classes
				.entrySet()
				.stream()
				.filter(Objects::nonNull)
				.filter(classEntry -> isAnnotationsPresent(classEntry.getValue(), NaftahFnProvider.class))
				.flatMap(classEntry -> {
					try {
						return Arrays
								.stream(classEntry.getValue().getMethods())
								.filter(method -> isAnnotationsPresent(method, NaftahFn.class) && methodPredicate
										.test((method)))
								.map(method -> Map.entry(method, classEntry));
					}
					catch (Throwable e) {
						// skip
						return null;
					}
				})
				.filter(Objects::nonNull)
				.map(methodEntry -> {
					Class<?> clazz = methodEntry.getValue().getValue();
					Method method = methodEntry.getKey();
					var naftahFunctionProvider = getNaftahFunctionProviderAnnotation(clazz);
					var naftahFunction = getNaftahFunctionAnnotation(   method,
																		naftahFunctionProvider.useQualifiedName(),
																		naftahFunctionProvider.useQualifiedAliases());
					return BuiltinFunction.of(method, naftahFunctionProvider, naftahFunction);
				})
				.collect(toAliasGroupedByName());
	}

	/**
	 * Constructs the canonical name for a built-in function, optionally using a qualified name
	 * that includes the provider name.
	 *
	 * @param useQualifiedName     whether to qualify the function name with the provider name
	 * @param providerName         the name of the function provider (e.g. extension or library name)
	 * @param functionName         the actual function name
	 * @param removeNameDiacritics whether to remove Arabic diacritics from both provider and function names
	 * @return the cleaned and optionally qualified function name, with spaces replaced by underscores
	 */
	public static String getBuiltinFunctionName(boolean useQualifiedName,
												String providerName,
												String functionName,
												boolean removeNameDiacritics) {
		if (useQualifiedName) {
			var qualifiedName = cleanBuiltinFunctionName(   providerName,
															QUALIFIED_NAME_SEPARATOR,
															true) + QUALIFIED_CALL_SEPARATOR + cleanBuiltinFunctionName(
																														functionName,
																														UNDERSCORE,
																														removeNameDiacritics);
			if (QUALIFIED_CALL_REGEX.matcher(qualifiedName).matches()) {
				System.out.println(qualifiedName);
				throw new NaftahBugError(
											"""
											تنسيق الاسم المؤهل غير صالح. يجب أن يحتوي على ':' واحد على الأقل و'::' واحد بالضبط، مثل: 'أ:ب::ج' أو 'أ:ب:ب:أ::ج'..
											"""
				);
			}
			return qualifiedName;
		}
		return cleanBuiltinFunctionName(functionName, UNDERSCORE, removeNameDiacritics);

	}

	/**
	 * Normalizes a function or provider name by optionally removing Arabic diacritics
	 * and replacing whitespace with underscores.
	 *
	 * @param name             the original name to clean
	 * @param removeDiacritics whether to remove Arabic diacritics from the name
	 * @return a sanitized string with optional diacritics removed and spaces replaced with underscores
	 */
	public static String cleanBuiltinFunctionName(  String name,
													String replacement,
													boolean removeDiacritics) {
		name = removeDiacritics ? ArabicUtils.removeDiacritics(name) : name;
		return name.replaceAll("\\s+", replacement);
	}

	/**
	 * Retrieves all built-in methods from classes annotated with @NaftahFnProvider.
	 *
	 * @param classes map of qualified class names to Class objects
	 * @return map of function names to lists of BuiltinFunction instances
	 */
	public static Map<String, List<BuiltinFunction>> getBuiltinMethods(Map<String, Class<?>> classes) {
		return getBuiltinMethods(classes, (method) -> true);
	}

	/**
	 * Retrieves built-in methods annotated with @NaftahFn from a single class
	 * annotated with @NaftahFnProvider.
	 *
	 * @param clazz the class to inspect
	 * @return list of BuiltinFunction instances or empty list if not annotated
	 */
	public static List<BuiltinFunction> getBuiltinMethods(Class<?> clazz) {
		return isAnnotationsPresent(clazz, NaftahFnProvider.class) ?
				Arrays
						.stream(clazz.getMethods())
						.filter(method -> isAnnotationsPresent(method, NaftahFn.class))
						.map(method -> {
							var naftahFunctionProvider = getNaftahFunctionProviderAnnotation(clazz);
							var naftahFunction = getNaftahFunctionAnnotation(   method,
																				naftahFunctionProvider
																						.useQualifiedName(),
																				naftahFunctionProvider
																						.useQualifiedAliases());
							return BuiltinFunction.of(method, naftahFunctionProvider, naftahFunction);
						})
						.toList() :
				List.of();
	}


	/**
	 * Retrieves built-in functions from multiple classes annotated with {@link NaftahFnProvider}.
	 * <p>
	 * This method aggregates results from all provided classes by invoking
	 * {@link #getBuiltinMethods(Class)} on each one.
	 *
	 * @param classes the set of classes to inspect
	 * @return a combined list of {@link BuiltinFunction} instances from all classes,
	 *         or an empty list if none are found
	 */
	public static List<BuiltinFunction> getBuiltinMethods(Set<Class<?>> classes) {
		return classes
				.stream()
				.flatMap(aClass -> getBuiltinMethods(aClass).stream())
				.toList();
	}

	/**
	 * Returns a detailed, Arabic-formatted string representation of the specified Java class.
	 * <p>
	 * The returned string includes the following information about the class:
	 * <ul>
	 * <li>Fully qualified class name (e.g., {@code java.util.ArrayList})</li>
	 * <li>Simple class name (e.g., {@code ArrayList})</li>
	 * <li>Package name (or "(default)" if the class is in the default package)</li>
	 * <li>Class modifiers (whether it's {@code public}, {@code abstract}, or an {@code interface})</li>
	 * <li>The superclass of the class (if any)</li>
	 * <li>All interfaces implemented by the class</li>
	 * <li>Declaring class (if the class is an inner or nested class)</li>
	 * <li>Whether the class is an {@code enum}, {@code annotation}, {@code record}, or {@code primitive} type</li>
	 * </ul>
	 * <p>
	 * Additionally, the method includes Arabic transliteration (phonetic representation in Arabic script)
	 * for class names and package names, using {@link ClassUtils#getQualifiedName(String)}
	 * or {@link ArabicUtils#transliterateToArabicScriptDefault(String...)}.
	 * For example, the simple name {@code ArrayList} might appear as:
	 * <pre>{@code
	 * ArrayList - أرَي لِسْتْ
	 * }</pre>
	 * <p>
	 * The output is formatted as a multi-line string with Arabic labels for each item.
	 * Example output (for {@code java.util.ArrayList}):
	 *
	 * <pre>
	 * تفاصيل الصنف:
	 * - الاسم الكامل: java.util.ArrayList - جاڤا:يوتِل:أرَي_لِسْتْ
	 * - الاسم المختصر: ArrayList - أرَي_لِسْتْ
	 * - الحزمة: java.util - جاڤا:يوتِل
	 * - عام؟: نعم
	 * - مجرد؟: لا
	 * - واجهة؟: لا
	 * - الصنف الأب: java.util.AbstractList - جاڤا:يوتِل:أَبْسْتْرَكْتْ_لِسْتْ
	 * - الواجهات:
	 * - java.util.List - جاڤا:يوتِل:لِسْتْ
	 * - java.util.RandomAccess - جاڤا:يوتِل:رَانْدُم_أَكْسِسْ
	 * - java.lang.Cloneable - جاڤا:لانْغ:كْلُونِبْلْ
	 * - java.io.Serializable - جاڤا:أَي_أُو:سِرِيَالِيزَابْلْ
	 * - تعداد؟: لا
	 * - توصيف؟: لا
	 * - سجل؟: لا
	 * - نوع بدائي؟: لا
	 * </pre>
	 *
	 * @param clazz the {@link Class} object to inspect
	 * @return a multi-line, Arabic-formatted string describing the class, including Arabic transliterations
	 */
	public static String classToDetailedString(Class<?> clazz) {
		StringBuilder detailedString = new StringBuilder();

		detailedString
				.append("""
							تفاصيل الصنف:
							\t- الاسم الكامل: %s
							\t- الاسم المختصر: %s
							\t- الحزمة: %s
							\t- عام (public)؟: %s
							\t- مجرد (abstract)؟: %s
							\t- واجهة (interface)؟: %s
						"""
						.formatted(
									clazz.getName() + " - " + getQualifiedName(clazz.getName()),
									clazz.getSimpleName() + " - " + ArabicUtils
											.transliterateToArabicScriptDefault(clazz.getSimpleName())[0],
									clazz.getPackage() != null ?
											clazz.getPackage().getName() + " - " + getQualifiedName(clazz
													.getPackage()
													.getName()) :
											"(افتراضي)",
									Modifier.isPublic(clazz.getModifiers()) ? "نعم" : "لا",
									Modifier.isAbstract(clazz.getModifiers()) ? "نعم" : "لا",
									Modifier.isInterface(clazz.getModifiers()) ? "نعم" : "لا"
						));

		// Superclass
		if (clazz.getSuperclass() != null) {
			detailedString
					.append("\t- الصنف الأب (super classes): ")
					.append(clazz.getSuperclass().getName())
					.append(" - ")
					.append(getQualifiedName(clazz.getSuperclass().getName()))
					.append("\n");
		}

		// Interfaces
		Class<?>[] interfaces = clazz.getInterfaces();
		if (interfaces.length > 0) {
			detailedString.append("\t- الواجهات (interfaces):\n");
			for (Class<?> iface : interfaces) {
				detailedString
						.append("\t\t- ")
						.append(iface.getName())
						.append(" - ")
						.append(getQualifiedName(iface.getName()))
						.append("\n");
			}
		}

		// Declaring class (for nested/inner classes)
		if (clazz.getDeclaringClass() != null) {
			detailedString
					.append("\t- الصنف المُعلن (declaring class): ")
					.append(clazz.getDeclaringClass().getName())
					.append(" - ")
					.append(getQualifiedName(clazz.getDeclaringClass().getName()))
					.append("\n");
		}

		// Special types
		detailedString
				.append("""
							\t- تعداد (enum)؟: %s
							\t- توصيف (annotation)؟: %s
							\t- سجل (record)؟: %s
							\t- نوع بدائي (primitive)؟: %s
						"""
						.formatted(
									clazz.isEnum() ? "نعم" : "لا",
									clazz.isAnnotation() ? "نعم" : "لا",
									clazz.isRecord() ? "نعم" : "لا",
									clazz.isPrimitive() ? "نعم" : "لا"
						));

		return detailedString.toString();
	}
}
