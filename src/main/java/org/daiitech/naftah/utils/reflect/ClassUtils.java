package org.daiitech.naftah.utils.reflect;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.misc.Pair;
import org.daiitech.naftah.builtin.NaftahFn;
import org.daiitech.naftah.builtin.NaftahFnProvider;
import org.daiitech.naftah.builtin.lang.BuiltinFunction;
import org.daiitech.naftah.builtin.lang.DynamicNumber;
import org.daiitech.naftah.builtin.lang.JvmClassInitializer;
import org.daiitech.naftah.builtin.lang.JvmExecutable;
import org.daiitech.naftah.builtin.lang.JvmFunction;
import org.daiitech.naftah.builtin.lang.NaN;
import org.daiitech.naftah.builtin.lang.NaftahObject;
import org.daiitech.naftah.builtin.lang.None;
import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.utils.arabic.ArabicUtils;

import static org.daiitech.naftah.Naftah.UNDERSCORE;
import static org.daiitech.naftah.builtin.utils.AliasHashMap.toAliasGroupedByName;
import static org.daiitech.naftah.builtin.utils.CollectionUtils.createCollection;
import static org.daiitech.naftah.builtin.utils.CollectionUtils.createMap;
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
			if (Modifier.isPublic(constructor.getModifiers())) {
				return true;
			}
		}
		catch (NoSuchMethodException ignored) {
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

	/**
	 * Dynamically invokes a Java {@link Method} or {@link Constructor} using reflection.
	 *
	 * <p>This unified reflection utility abstracts the complexity of invoking either
	 * a {@link Method} or {@link Constructor} by automatically handling parameter
	 * conversion, null-safety, and primitive boxing/unboxing. It is designed for
	 * runtime execution environments that need to dynamically call JVM executables
	 * without compile-time type information.</p>
	 *
	 * <h3>Supported executable types</h3>
	 * <ul>
	 * <li><b>Instance and static methods</b> — invoked via {@link Method#invoke(Object, Object...)}.</li>
	 * <li><b>Constructors</b> — invoked via {@link Constructor#newInstance(Object...)}.</li>
	 * </ul>
	 *
	 * <h3>Argument conversion</h3>
	 * <p>Each argument is automatically converted to match the target parameter’s
	 * declared type, including support for:
	 * <ul>
	 * <li>Primitive and wrapper types (e.g. {@code int ↔ Integer})</li>
	 * <li>Arrays and collections</li>
	 * <li>Generic types and parameterized arguments</li>
	 * </ul>
	 * </p>
	 *
	 * <h3>Null and {@link None} handling</h3>
	 * <p>If {@code useNone} is {@code true}, the method returns {@link None#get()}
	 * instead of {@code null} or {@code void} results, ensuring a non-null return
	 * value in all cases.</p>
	 *
	 * <h3>Parameter validation</h3>
	 * <p>If the number of provided arguments does not match the executable’s parameter count,
	 * an {@link IllegalArgumentException} is thrown.</p>
	 *
	 * @param instance            the target object instance for method invocation,
	 *                            or {@code null} when invoking a static method or a constructor.
	 * @param methodOrConstructor the reflective {@link Executable} to invoke (either a {@link Method} or a
	 *                            {@link Constructor}).
	 * @param args                a list of {@link Pair}&lt;{@link String}, {@link Object}&gt; representing
	 *                            argument names and their corresponding values.
	 * @param returnType          the expected return type of the invocation. Use {@link Void#TYPE} or {@link Void}
	 *                            for methods that return {@code void}.
	 * @param useNone             if {@code true}, replaces {@code null} or {@code void} results with
	 *                            {@link None#get()}.
	 * @return the invocation result, or {@link None#get()} if the result is {@code null}
	 *         or the executable has a {@code void} return type.
	 * @throws InvocationTargetException if the underlying executable throws an exception.
	 * @throws InstantiationException    if the target constructor fails to create a new instance.
	 * @throws IllegalAccessException    if this {@link Executable} object is enforcing Java language access control
	 *                                   and the underlying method or constructor is inaccessible.
	 * @throws IllegalArgumentException  if the provided arguments do not match the executable’s parameter types or
	 *                                   count.
	 * @see java.lang.reflect.Method#invoke(Object, Object...)
	 * @see java.lang.reflect.Constructor#newInstance(Object...)
	 * @see java.lang.reflect.Executable
	 */
	public static <T extends Executable> Object invokeJvmExecutable(
																	Object instance,
																	T methodOrConstructor,
																	List<Pair<String, Object>> args,
																	Class<?> returnType,
																	boolean useNone)
			throws InvocationTargetException,
			InstantiationException,
			IllegalAccessException {
		Class<?>[] paramTypes = methodOrConstructor.getParameterTypes();

		if (args.size() != paramTypes.length) {
			throw new IllegalArgumentException("Argument count mismatch");
		}

		Object[] executableArgs = new Object[args.size()];
		Type[] genericTypes = methodOrConstructor.getGenericParameterTypes();

		for (int i = 0; i < args.size(); i++) {
			executableArgs[i] = convertArgument(args.get(i).b, paramTypes[i], genericTypes[i], useNone);
		}

		return invokeJvmExecutable(instance, methodOrConstructor, executableArgs, returnType);
	}

	/**
	 * Invokes a given Java {@link Method} or {@link Constructor} reflectively with the specified arguments.
	 *
	 * <p>This low-level helper provides direct reflective invocation of any JVM {@link Executable},
	 * including both instance and static methods as well as constructors. It performs runtime access
	 * override, handles {@link NaftahObject} unwrapping for method calls, and provides unified return
	 * value handling using {@link None#get()} for {@code void} or {@code null} results.</p>
	 *
	 * <h3>Behavior overview</h3>
	 * <ul>
	 * <li>Automatically calls {@link java.lang.reflect.AccessibleObject#setAccessible(boolean)} to bypass Java
	 * access checks.</li>
	 * <li>Invokes instance or static methods via {@link Method#invoke(Object, Object...)}.</li>
	 * <li>Creates new instances via {@link Constructor#newInstance(Object...)}.</li>
	 * <li>Unwraps {@link NaftahObject} instances automatically if the declaring class
	 * is not a subclass of {@link NaftahObject}.</li>
	 * <li>Returns {@link None#get()} for {@code void} or {@code null} return values.</li>
	 * </ul>
	 *
	 * <h3>Return semantics</h3>
	 * <p>
	 * If the executable returns a value and it is non-null, that value is returned directly.
	 * Otherwise, {@link None#get()} is returned to represent an absence of value.
	 * </p>
	 *
	 * @param instance            the target object instance for method invocation,
	 *                            or {@code null} when invoking a static method or a constructor.
	 * @param methodOrConstructor the reflective {@link Executable} to invoke (either a {@link Method} or a
	 *                            {@link Constructor}).
	 * @param executableArgs      an array of argument values to pass to the executable, in declared order.
	 * @param returnType          the expected return type of the invocation. Use {@link Void#TYPE} or {@link Void}
	 *                            for {@code void} methods or constructors.
	 * @return the invocation result, or {@link None#get()} if the result is {@code null}
	 *         or the executable has a {@code void} return type.
	 * @throws InvocationTargetException if the underlying method or constructor throws an exception.
	 * @throws InstantiationException    if the target constructor fails to instantiate a new object.
	 * @throws IllegalAccessException    if this {@link Executable} object is enforcing Java language access control
	 *                                   and the underlying method or constructor is inaccessible.
	 * @see java.lang.reflect.Method#invoke(Object, Object...)
	 * @see java.lang.reflect.Constructor#newInstance(Object...)
	 * @see java.lang.reflect.AccessibleObject#setAccessible(boolean)
	 * @see NaftahObject
	 * @see None#get()
	 */
	public static <T extends Executable> Object invokeJvmExecutable(
																	Object instance,
																	T methodOrConstructor,
																	Object[] executableArgs,
																	Class<?> returnType)
			throws InvocationTargetException,
			InstantiationException,
			IllegalAccessException {
		methodOrConstructor.setAccessible(true);
		Object possibleResult = null;
		if (methodOrConstructor instanceof Method method) {
			if (Objects.nonNull(instance) && !NaftahObject.class
					.isAssignableFrom(method.getDeclaringClass()) && instance instanceof NaftahObject naftahObject) {
				instance = naftahObject.get(true);
			}
			possibleResult = method.invoke(instance, executableArgs);
		}
		else if (methodOrConstructor instanceof Constructor<?> constructor) {
			possibleResult = constructor.newInstance(executableArgs);
		}
		return returnType != Void.class && possibleResult != null ? possibleResult : None.get();
	}

	/**
	 * Invokes a Java {@link Constructor} reflectively using the given argument list.
	 *
	 * <p>This is a convenience wrapper around
	 * {@link #invokeJvmExecutable(Object, Executable, List, Class, boolean)}
	 * that simplifies reflective constructor invocation by automatically passing
	 * {@code null} as the instance (since constructors do not require one).</p>
	 *
	 * <p>All constructor arguments are converted as needed to match the parameter types,
	 * including handling of primitive values, arrays, and generic types. The method also
	 * supports the {@link None} sentinel for representing {@code null} or {@code void}
	 * results, depending on the {@code useNone} flag.</p>
	 *
	 * <h3>Behavior overview</h3>
	 * <ul>
	 * <li>Uses {@link java.lang.reflect.Constructor#newInstance(Object...)} for instantiation.</li>
	 * <li>Automatically adjusts and converts argument types where necessary.</li>
	 * <li>Returns {@link None#get()} if the constructor result is {@code null} or if {@code useNone} is enabled
	 * .</li>
	 * <li>Throws standard reflection exceptions for access or instantiation failures.</li>
	 * </ul>
	 *
	 * @param methodOrConstructor the {@link Constructor} to invoke.
	 * @param args                the constructor arguments as a list of {@code Pair<String, Object>}, where each pair
	 *                            represents an argument name and its value.
	 * @param returnType          the expected return type of the constructed object.
	 * @param useNone             if {@code true}, replaces {@code null} or {@code void} results with
	 *                            {@link None#get()}.
	 * @return the newly created instance, or {@link None#get()} if the constructor returns {@code null}
	 *         or {@code void}, or if {@code useNone} is enabled.
	 * @throws InvocationTargetException if the underlying constructor throws an exception.
	 * @throws InstantiationException    if the constructor cannot instantiate a new object.
	 * @throws IllegalAccessException    if reflective access is not permitted.
	 * @throws IllegalArgumentException  if the argument count or types do not match the constructor parameters.
	 * @see #invokeJvmExecutable(Object, Executable, List, Class, boolean)
	 * @see java.lang.reflect.Constructor#newInstance(Object...)
	 * @see None#get()
	 */
	public static <T extends Executable> Object invokeJvmConstructor(   T methodOrConstructor,
																		List<Pair<String, Object>> args,
																		Class<?> returnType,
																		boolean useNone)
			throws InvocationTargetException,
			InstantiationException,
			IllegalAccessException {
		return invokeJvmExecutable(null, methodOrConstructor, args, returnType, useNone);
	}

	/**
	 * Invokes a Java {@link Constructor} reflectively using the specified argument array.
	 *
	 * <p>This is a lower-level convenience wrapper around
	 * {@link #invokeJvmExecutable(Object, Executable, Object[], Class)}
	 * that specifically targets constructors. It automatically supplies
	 * {@code null} for the {@code instance} parameter since constructors
	 * do not require a target object.</p>
	 *
	 * <h3>Behavior overview</h3>
	 * <ul>
	 * <li>Invokes the provided {@link Constructor} using
	 * {@link java.lang.reflect.Constructor#newInstance(Object...)}.</li>
	 * <li>Delegates reflection handling and {@link None#get()} substitution
	 * to {@link #invokeJvmExecutable(Object, Executable, Object[], Class)}.</li>
	 * <li>Returns {@link None#get()} for {@code void} or {@code null} results.</li>
	 * </ul>
	 *
	 * <p>This method assumes that all arguments in {@code executableArgs}
	 * are already type-compatible with the constructor’s parameters.
	 * For automatic type conversion or named arguments, use the
	 * {@link #invokeJvmConstructor(Executable, List, Class, boolean)} variant.</p>
	 *
	 * @param methodOrConstructor the {@link Constructor} to invoke reflectively.
	 * @param executableArgs      an array of argument values to pass to the constructor.
	 * @param returnType          the expected return type of the constructed object.
	 * @return the newly created instance, or {@link None#get()} if the constructor
	 *         result is {@code null} or represents {@code void}.
	 * @throws InvocationTargetException if the underlying constructor throws an exception.
	 * @throws InstantiationException    if the constructor cannot instantiate a new object.
	 * @throws IllegalAccessException    if reflective access is not permitted.
	 * @see #invokeJvmExecutable(Object, Executable, Object[], Class)
	 * @see java.lang.reflect.Constructor#newInstance(Object...)
	 * @see None#get()
	 */
	public static <T extends Executable> Object invokeJvmConstructor(   T methodOrConstructor,
																		Object[] executableArgs,
																		Class<?> returnType)
			throws InvocationTargetException,
			InstantiationException,
			IllegalAccessException {
		return invokeJvmExecutable(null, methodOrConstructor, executableArgs, returnType);
	}

	/**
	 * Converts a single argument to the target type expected by a method parameter.
	 *
	 * <p>This method supports:
	 * <ul>
	 * <li>Primitive types (int, long, double, float, boolean, char, byte, short)</li>
	 * <li>{@link DynamicNumber}</li>
	 * <li>Arrays (recursively)</li>
	 * <li>Collections (recursively, attempting to respect generic type if provided)</li>
	 * </ul>
	 * </p>
	 *
	 * @param value       the original argument value.
	 * @param targetType  the expected type to convert to.
	 * @param genericType the generic type information, if available.
	 * @return the converted value matching the target type.
	 */
	public static Object convertArgument(Object value, Class<?> targetType, Type genericType, boolean useNone) {
		if (value == null || None.isNone(value)) {
			return useNone ? None.get() : null;
		}

		if (!targetType.equals(NaftahObject.class) && value instanceof NaftahObject naftahObject) {
			value = naftahObject.get(true);
		}

		// Already assignable
		if (targetType.isInstance(value)) {
			return value;
		}

		if (value instanceof DynamicNumber dynamicNumber) {
			value = dynamicNumber.get();
		}

		if (NaN.isNaN(value)) {
			value = Double.NaN;
		}

		// Handle primitives
		if (targetType.isPrimitive()) {
			if (targetType == int.class) {
				return ((Number) value).intValue();
			}
			if (targetType == long.class) {
				return ((Number) value).longValue();
			}
			if (targetType == double.class) {
				return ((Number) value).doubleValue();
			}
			if (targetType == float.class) {
				return ((Number) value).floatValue();
			}
			if (targetType == boolean.class) {
				return value;
			}
			if (targetType == char.class) {
				return (value instanceof Character) ? value : value.toString().charAt(0);
			}
			if (targetType == byte.class) {
				return ((Number) value).byteValue();
			}
			if (targetType == short.class) {
				return ((Number) value).shortValue();
			}
		}

		// Handle arrays
		if (targetType.isArray() && value.getClass().isArray()) {
			int length = Array.getLength(value);
			Object newArray = Array.newInstance(targetType.getComponentType(), length);
			for (int i = 0; i < length; i++) {
				Array
						.set(   newArray,
								i,
								convertArgument(Array.get(value, i),
												targetType.getComponentType(),
												targetType.getComponentType(),
												useNone));
			}
			return newArray;
		}

		// Handle collections
		if ((Collection.class.isAssignableFrom(targetType) || targetType
				.isArray()) && value instanceof Collection<?> src) {
			Collection<Object> result = createCollection(targetType);
			Class<?> itemType = Object.class;
			if (genericType instanceof ParameterizedType pt) {
				Type[] typeArgs = pt.getActualTypeArguments();
				if (typeArgs.length == 1 && typeArgs[0] instanceof Class<?> c) {
					itemType = c;
				}
			}
			for (Object item : src) {
				result.add(convertArgument(item, itemType, itemType, useNone));
			}
			return targetType.isArray() ? result.toArray() : result;
		}


		// Handle maps
		if (Map.class.isAssignableFrom(targetType) && value instanceof Map<?, ?> srcMap) {
			Map<Object, Object> result = createMap(targetType);

			Class<?> keyType = Object.class;
			Class<?> valueType = Object.class;

			if (genericType instanceof ParameterizedType pt) {
				Type[] typeArgs = pt.getActualTypeArguments();
				if (typeArgs.length == 2) {
					if (typeArgs[0] instanceof Class<?> k) {
						keyType = k;
					}
					if (typeArgs[1] instanceof Class<?> v) {
						valueType = v;
					}
				}
			}

			for (Map.Entry<?, ?> entry : srcMap.entrySet()) {
				Object newKey = convertArgument(entry.getKey(), keyType, keyType, useNone);
				Object newValue = convertArgument(entry.getValue(), valueType, valueType, useNone);
				result.put(newKey, newValue);
			}
			return result;
		}

		// Fallback to Class.cast
		return targetType.cast(value);
	}

	/**
	 * Attempts to find the most suitable {@link JvmExecutable} (method or constructor)
	 * from a collection of candidates, based on the provided argument list.
	 *
	 * <p>This overload delegates to
	 * {@link #findBestExecutable(Collection, List, boolean)} with
	 * {@code removeInstanceArg = false}.</p>
	 *
	 * @param candidates the collection of available {@link JvmExecutable} instances to evaluate.
	 * @param args       the argument list to match, represented as {@code Pair<String, Object>}
	 *                   where each pair contains the argument name and its value.
	 * @param <T>        the type of {@link JvmExecutable} (e.g. {@code JvmFunction}, {@code BuiltinFunction}).
	 * @return a {@link Pair} containing the best-matching {@link JvmExecutable} and its
	 *         prepared argument array; or {@code null} if no suitable match is found.
	 * @see #findBestExecutable(Collection, List, boolean)
	 */
	public static <T extends JvmExecutable> Pair<T, Object[]> findBestExecutable(   Collection<T> candidates,
																					List<Pair<String, Object>> args) {
		return findBestExecutable(candidates, args, false);
	}

	/**
	 * Determines the best-matching {@link JvmExecutable} (method or constructor)
	 * from a given collection of candidates based on argument compatibility.
	 *
	 * <p>The matching process computes a “score” for each executable using
	 * {@link #matchScore(Executable, Class[], List, boolean)}, where lower scores
	 * indicate a closer match. The executable with the lowest non-negative score
	 * is returned along with its converted argument array.</p>
	 *
	 * <p>If {@code removeInstanceArg} is {@code true}, and the executable represents
	 * a non-static {@link JvmFunction}, the first argument in {@code args} is removed,
	 * as it corresponds to the instance already passed during invocation.</p>
	 *
	 * @param candidates        the collection of {@link JvmExecutable} candidates to search through.
	 * @param args              the argument list as {@code Pair<String, Object>} entries.
	 * @param removeInstanceArg whether to remove the first argument when matching
	 *                          non-static functions (useful when the instance is already supplied).
	 * @param <T>               the type of {@link JvmExecutable}.
	 * @return a {@link Pair} containing:
	 *         <ul>
	 *         <li>the best-matching {@link JvmExecutable}, and</li>
	 *         <li>the prepared and type-converted argument array for invocation.</li>
	 *         </ul>
	 *         Returns {@code null} if no compatible executable is found.
	 * @see #matchScore(Executable, Class[], List, boolean)
	 * @see JvmExecutable
	 * @see JvmFunction
	 * @see BuiltinFunction
	 */
	public static <T extends JvmExecutable> Pair<T, Object[]> findBestExecutable(   Collection<T> candidates,
																					List<Pair<String, Object>> args,
																					boolean removeInstanceArg) {
		if (candidates == null || candidates.isEmpty()) {
			return null;
		}

		Pair<T, Object[]> best = null;
		int bestScore = Integer.MAX_VALUE;

		for (T jvmExecutable : candidates) {
			Executable executable = jvmExecutable.getExecutable();
			Class<?>[] paramTypes = executable.getParameterTypes();

			// if the current executable is not static we should remove the first arg because the instance is passed
			if (removeInstanceArg && jvmExecutable instanceof JvmFunction jvmFunction && !jvmFunction
					.isStatic()) {
				args.remove(0);
			}

			if (paramTypes.length != args.size()) {
				continue;
			}

			Pair<Integer, Object[]> scoreAndArgs = matchScore(  executable,
																paramTypes,
																args,
																jvmExecutable instanceof BuiltinFunction);
			if (scoreAndArgs.a >= 0 && scoreAndArgs.a < bestScore) {
				best = new Pair<>(jvmExecutable, scoreAndArgs.b);
				bestScore = scoreAndArgs.a;
			}
		}
		return best;
	}

	/**
	 * Calculates how closely a set of provided arguments matches the parameters of a given {@link Executable}.
	 *
	 * <p>This method attempts to convert each argument into the target parameter type using
	 * {@code convertArgument()}, computing a total compatibility score as follows:</p>
	 * <ul>
	 * <li>Exact type match: score += 0</li>
	 * <li>Numeric type coercion (e.g. {@code Integer → Double}): score += 1</li>
	 * <li>Generic or compatible conversion: score += 3</li>
	 * <li>{@code null} argument: score += 10</li>
	 * </ul>
	 *
	 * <p>If any argument fails conversion, the method returns a score of {@code -1} to indicate incompatibility.</p>
	 *
	 * @param executable the target method or constructor being evaluated.
	 * @param params     the parameter types of the executable.
	 * @param args       the provided arguments as a list of {@code Pair<String, Object>}.
	 * @param useNone    whether {@link None#get()} should be treated as a {@code null}-equivalent placeholder.
	 * @param <T>        the type of the {@link Executable}.
	 * @return a {@link Pair} containing:
	 *         <ul>
	 *         <li>The computed match score (lower is better, {@code -1} indicates failure).</li>
	 *         <li>The array of converted argument values ready for invocation.</li>
	 *         </ul>
	 */
	private static <T extends Executable> Pair<Integer, Object[]> matchScore(   T executable,
																				Class<?>[] params,
																				List<Pair<String, Object>> args,
																				boolean useNone) {
		int score = 0;
		Object[] executableArgs = new Object[args.size()];

		for (int i = 0; i < params.length; i++) {
			Object arg = args.get(i).b;
			Class<?> param = params[i];

			try {
				Object converted = convertArgument(arg, param, getGenericType(executable, i), useNone);
				if (converted == null && param.isPrimitive()) {
					return new Pair<>(-1, null);
				}

				// scoring: lower is better
				if (arg == null) {
					score += 10;
				}
				else if (param.isInstance(converted)) {
					score += 0;
				}
				else //noinspection DataFlowIssue
					if (Number.class.isAssignableFrom(param) && Number.class.isAssignableFrom(converted.getClass())) {
						score += 1;
					}
					else {
						score += 3;
					}
				executableArgs[i] = converted;
			}
			catch (Throwable ignored) {
				return new Pair<>(-1, null); // conversion failed
			}
		}
		return new Pair<>(score, executableArgs);
	}

	/**
	 * Retrieves the generic parameter type for the specified parameter index of a given {@link Executable}.
	 *
	 * <p>If the executable declares generic parameter types, the corresponding one is returned;
	 * otherwise, the raw parameter type is used as a fallback.</p>
	 *
	 * @param executable the {@link Executable} whose parameter type should be retrieved.
	 * @param index      the parameter index.
	 * @param <T>        the type of the {@link Executable}.
	 * @return the {@link Type} representing the parameter’s declared or generic type.
	 */
	private static <T extends Executable> Type getGenericType(T executable, int index) {
		Type[] generic = executable.getGenericParameterTypes();
		return (index < generic.length) ? generic[index] : executable.getParameterTypes()[index];
	}
}
