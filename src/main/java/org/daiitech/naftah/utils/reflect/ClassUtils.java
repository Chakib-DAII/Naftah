package org.daiitech.naftah.utils.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.daiitech.naftah.builtin.NaftahFn;
import org.daiitech.naftah.builtin.NaftahFnProvider;
import org.daiitech.naftah.builtin.lang.BuiltinFunction;
import org.daiitech.naftah.builtin.lang.JvmFunction;
import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.utils.arabic.ArabicUtils;

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
						ArabicUtils.transliterateToArabicScriptDefault(true, className.split(CLASS_SEPARATORS_REGEX)));
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
				.formatted(qualifiedName, ArabicUtils.transliterateToArabicScriptDefault(true, method.getName())[0]);
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
				.formatted(qualifiedName, ArabicUtils.transliterateToArabicScriptDefault(true, methodName)[0]);
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
						.join(QUALIFIED_NAME_SEPARATOR, ArabicUtils.transliterateToArabicScriptDefault(true, strings)))
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
												ArabicUtils.transliterateToArabicScriptDefault(true, strings.clone())),
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
	 * Returns all methods from given classes without filtering.
	 *
	 * @param classes map of qualified class names to Class objects
	 * @return map from qualified call string to list of JvmFunction wrappers
	 */
	public static Map<String, List<JvmFunction>> getClassMethods(Map<String, Class<?>> classes) {
		return getClassMethods(classes, (method) -> true);
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
	 * Checks if a method is invocable: public, not synthetic or bridge,
	 * and either static or declared in an instantiable class.
	 *
	 * @param method the method to check
	 * @return true if invocable, false otherwise
	 */
	public static boolean isInvocable(Method method) {
		// Must be public
		if (!Modifier.isPublic(method.getModifiers())) {
			return false;
		}

		// Ignore synthetic or bridge methods (compiler-generated)
		if (method.isSynthetic() || method.isBridge()) {
			return false;
		}

		Class<?> declaringClass = method.getDeclaringClass();

		// If it's a static method, it's invocable directly
		if (isStatic(method)) {
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
					var naftahFunction = getNaftahFunctionAnnotation(method);
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
		return useQualifiedName ?
				cleanBuiltinFunctionName(providerName, true) + QUALIFIED_CALL_SEPARATOR + cleanBuiltinFunctionName(
																													functionName,
																													removeNameDiacritics) :
				cleanBuiltinFunctionName(functionName, removeNameDiacritics);
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
													boolean removeDiacritics) {
		name = removeDiacritics ? ArabicUtils.removeDiacritics(name) : name;
		return name.replaceAll("\\s+", "_");
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
							var naftahFunction = getNaftahFunctionAnnotation(method);
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
	 * or {@link ArabicUtils#transliterateToArabicScriptDefault(boolean, String...)}.
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
											.transliterateToArabicScriptDefault(true, clazz.getSimpleName())[0],
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
