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

import static org.daiitech.naftah.utils.reflect.AnnotationsUtils.getNaftahFunctionAnnotation;
import static org.daiitech.naftah.utils.reflect.AnnotationsUtils.getNaftahFunctionProviderAnnotation;
import static org.daiitech.naftah.utils.reflect.AnnotationsUtils.isAnnotationsPresent;

/**
 * @author Chakib Daii
 */
public final class ClassUtils {
	public static final String CLASS_SEPARATORS_REGEX = "[.$]";
	public static final String QUALIFIED_NAME_SEPARATOR = ":";
	public static final String QUALIFIED_CALL_SEPARATOR = "::";

	/**
	 * Private constructor to prevent instantiation.
	 * Always throws a {@link NaftahBugError} when called.
	 */
	private ClassUtils() {
		throw new NaftahBugError("استخدام غير مسموح به.");
	}

	public static String getQualifiedName(String className) {
		return String.join(QUALIFIED_NAME_SEPARATOR, ArabicUtils.transliterateToArabicScriptDefaultCustom(className.split(CLASS_SEPARATORS_REGEX)));
	}

	public static String getQualifiedCall(String qualifiedName, Method method) {
		return "%s::%s".formatted(qualifiedName, ArabicUtils.transliterateToArabicScriptDefaultCustom(method.getName()));
	}

	/**
	 * returns a set of class parts of the provided paths or classpath and jdk
	 * classnames if not
	 *
	 * @return set of qualified class names parts
	 */
	public static Map<String, String[]> getClassQualifiers(Set<String> classNames, boolean flattened) {
		var baseStream = classNames.stream().map(s -> s.split(CLASS_SEPARATORS_REGEX));

		if (flattened) {
			return baseStream.flatMap(strings -> Arrays.stream(strings).map(element -> Map.entry(element, strings))).collect(
					Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (existing, replacement) -> existing
					));
		}

		return baseStream.map(strings -> Map.entry(String.join(QUALIFIED_NAME_SEPARATOR, strings), strings)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	/**
	 * returns a set of class parts of the provided paths or classpath and jdk
	 * classnames if not
	 *
	 * @return set of qualified class names parts transliterated to arabic
	 */
	public static Set<String> getArabicClassQualifiers(Collection<String[]> classQualifiers) {
		return classQualifiers.stream().map(strings -> String.join(QUALIFIED_NAME_SEPARATOR, ArabicUtils.transliterateToArabicScriptDefaultCustom(strings))).collect(Collectors.toSet());
	}

	public static Map<String, String> getArabicClassQualifiersMapping(Collection<String[]> classQualifiers) {
		return classQualifiers.stream().map(strings -> Map.entry(
				String.join(QUALIFIED_NAME_SEPARATOR, ArabicUtils.transliterateToArabicScriptDefaultCustom(strings)), String.join(QUALIFIED_NAME_SEPARATOR, strings))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (existing, replacement) -> existing));
	}

	/**
	 * returns methods mapped to the input classes filtered by method filter Usage:
	 *
	 * <pre>
	 * Set<Class<?>> classes = ClassUtils.getClasses();
	 * Predicate<Method> instancePublicMethodPredicate = method -> Modifier.isPublic(method.getModifiers());
	 * Predicate<Method> classPublicMethodPredicate = instancePublicMethodPredicate
	 * .and(method -> Modifier.isStatic(method.getModifiers()));
	 * Map<Class<?>, Method[]> instancePublicMethods = ClassUtils.getClassMethods(classes,
	 * instancePublicMethodPredicate);
	 * Map<Class<?>, Method[]> classPublicMethods = ClassUtils.getClassMethods(classes, classPublicMethodPredicate);
	 * </pre>
	 *
	 * @param classes         set of classes having methods as members
	 * @param methodPredicate methods filter
	 * @return hashtable (Map) of classes and methods
	 */
	public static Map<String, List<JvmFunction>> getClassMethods(Map<String, Class<?>> classes, Predicate<Method> methodPredicate) {
		return classes.entrySet().stream().filter(Objects::nonNull).flatMap(classEntry -> {
			try {
				return Arrays.stream(classEntry.getValue().getMethods()).filter(methodPredicate).map(method -> Map.entry(method, classEntry));
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
		}).collect(
				Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
	}

	public static List<JvmFunction> getClassMethods(String qualifiedName, Class<?> clazz) {
		return Arrays.stream(clazz.getMethods()).map(method -> {
			String qualifiedCall = getQualifiedCall(qualifiedName, method);
			return JvmFunction.of(qualifiedCall, clazz, method);
		}).toList();
	}

	/**
	 * returns methods mapped to the input classes Usage:
	 *
	 * <pre>
	 * Set<Class<?>> classes = ClassUtils.getClasses();
	 * Map<Class<?>, Method[]> methods = ClassUtils.getClassMethods(classes);
	 * </pre>
	 *
	 * @param classes set of classes having methods as members
	 * @return hashtable (Map) of classes and methods
	 */
	public static Map<String, List<JvmFunction>> getClassMethods(Map<String, Class<?>> classes) {
		return getClassMethods(classes, (method) -> true);
	}

	public static boolean isAccessibleClass(Class<?> clazz) {
		// Try to find a public static factory method returning the same type
		for (Method method : clazz.getDeclaredMethods()) {
			if (Modifier.isStatic(method.getModifiers()) && Modifier.isPublic(method.getModifiers())) {
				return true;
			}
		}
		return false;
	}

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

	public static boolean isStatic(Method method) {
		// If it's a static method, it's invocable directly
		return Modifier.isStatic(method.getModifiers());
	}

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

	public static Map<String, Class<?>> filterClasses(Map<String, Class<?>> classes, Predicate<Class<?>> classPredicate) {
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

	public static Map<String, List<BuiltinFunction>> getBuiltinMethods(Map<String, Class<?>> classes, Predicate<Method> methodPredicate) {
		return classes.entrySet().stream().filter(Objects::nonNull).filter(classEntry -> isAnnotationsPresent(classEntry.getValue(), NaftahFnProvider.class)).flatMap(classEntry -> {
			try {
				return Arrays.stream(classEntry.getValue().getMethods()).filter(method -> isAnnotationsPresent(method, NaftahFn.class) && methodPredicate.test((method))).map(method -> Map.entry(method, classEntry));
			}
			catch (Throwable e) {
				// skip
				return null;
			}
		}).filter(Objects::nonNull).map(methodEntry -> {
			Class<?> clazz = methodEntry.getValue().getValue();
			Method method = methodEntry.getKey();
			var naftahFunctionProvider = getNaftahFunctionProviderAnnotation(clazz);
			var naftahFunction = getNaftahFunctionAnnotation(method);
			return Map.entry(naftahFunction.name(), BuiltinFunction.of(method, naftahFunctionProvider, naftahFunction));
		}).collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
	}

	public static Map<String, List<BuiltinFunction>> getBuiltinMethods(Map<String, Class<?>> classes) {
		return getBuiltinMethods(classes, (method) -> true);
	}

	public static List<BuiltinFunction> getBuiltinMethods(Class<?> clazz) {
		return isAnnotationsPresent(clazz, NaftahFnProvider.class) ? Arrays.stream(clazz.getMethods()).filter(method -> isAnnotationsPresent(method, NaftahFn.class)).map(method -> {
			var naftahFunctionProvider = getNaftahFunctionProviderAnnotation(clazz);
			var naftahFunction = getNaftahFunctionAnnotation(method);
			return BuiltinFunction.of(method, naftahFunctionProvider, naftahFunction);
		}).toList() : List.of();
	}
}
