package org.daiitech.naftah.utils.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.daiitech.naftah.builtin.NaftahFn;
import org.daiitech.naftah.builtin.NaftahFnProvider;
import org.daiitech.naftah.builtin.lang.NaftahFunction;
import org.daiitech.naftah.builtin.lang.NaftahFunctionProvider;
import org.daiitech.naftah.errors.NaftahBugError;

/**
 * Utility class for working with Java annotations on methods, classes, and
 * parameters.
 *
 * @author Chakib Daii
 */
public final class AnnotationsUtils {

	/**
	 * Private constructor to prevent instantiation.
	 * Throws {@link NaftahBugError} if called.
	 */
	private AnnotationsUtils() {
		throw new NaftahBugError("استخدام غير مسموح به.");
	}

	/**
	 * Checks if the specified annotations are present on the given method.
	 *
	 * @param method      The method to check.
	 * @param annotations One or more annotation classes to verify.
	 * @return {@code true} if all specified annotations are present on the method,
	 *         otherwise {@code
	 * false}.
	 */
	@SafeVarargs
	public static boolean isAnnotationsPresent(Method method, Class<? extends Annotation>... annotations) {
		List<Boolean> annotationsPresence = new ArrayList<>();
		for (var annotation : annotations) {
			annotationsPresence.add(method.isAnnotationPresent(annotation));
		}
		return !annotationsPresence.contains(false);
	}

	/**
	 * Returns a list of all annotations declared on the given method.
	 *
	 * @param method The method to inspect.
	 * @return A list of annotations present on the method.
	 */
	public static List<Annotation> getMethodAnnotations(Method method) {
		return Arrays.stream(method.getDeclaredAnnotations()).toList();
	}

	/**
	 * Returns a specific annotation instance from the given method.
	 *
	 * @param method          The method to inspect.
	 * @param annotationClass The class of the annotation to retrieve.
	 * @param <T>             The type of the annotation.
	 * @return The annotation instance if present, or {@code null} if not.
	 */
	public static <T extends Annotation> T getMethodAnnotation(Method method, Class<T> annotationClass) {
		return method.getAnnotation(annotationClass);
	}

	/**
	 * Extracts the custom {@link NaftahFunction} representation from the
	 * {@link NaftahFn} annotation on the method.
	 *
	 * @param method The method annotated with {@link NaftahFn}.
	 * @return A {@link NaftahFunction} instance constructed from the annotation.
	 */
	public static NaftahFunction getNaftahFunctionAnnotation(Method method) {
		NaftahFn naftahFn = getMethodAnnotation(method, NaftahFn.class);
		return NaftahFunction.of(naftahFn.name(), naftahFn.description(), naftahFn.usage(), naftahFn.returnType(), naftahFn.parameterTypes(), naftahFn.exceptionTypes());
	}

	/**
	 * Checks if the specified annotations are present on the given class.
	 *
	 * @param aClass      The class to check.
	 * @param annotations One or more annotation classes to verify.
	 * @return {@code true} if all specified annotations are present on the class,
	 *         otherwise {@code
	 * false}.
	 */
	@SafeVarargs
	public static boolean isAnnotationsPresent(Class<?> aClass, Class<? extends Annotation>... annotations) {
		List<Boolean> annotationsPresence = new ArrayList<>();
		for (var annotation : annotations) {
			annotationsPresence.add(aClass.isAnnotationPresent(annotation));
		}
		return !annotationsPresence.contains(false);
	}

	/**
	 * Returns a list of all annotations present on the given class.
	 *
	 * @param aClass The class to inspect.
	 * @return A list of annotations present on the class.
	 */
	public static List<Annotation> getClassAnnotations(Class<?> aClass) {
		return List.of(aClass.getAnnotations());
	}

	/**
	 * Returns a specific annotation instance from the given class.
	 *
	 * @param aClass          The class to inspect.
	 * @param annotationClass The class of the annotation to retrieve.
	 * @param <T>             The type of the annotation.
	 * @return The annotation instance if present, or {@code null} if not.
	 */
	public static <T extends Annotation> T getClassAnnotation(Class<?> aClass, Class<T> annotationClass) {
		return aClass.getAnnotation(annotationClass);
	}

	/**
	 * Extracts the custom {@link NaftahFunctionProvider} representation from the
	 * {@link NaftahFnProvider} annotation on the class.
	 *
	 * @param aClass The class annotated with {@link NaftahFnProvider}.
	 * @return A {@link NaftahFunctionProvider} instance constructed from the
	 *         annotation.
	 */
	public static NaftahFunctionProvider getNaftahFunctionProviderAnnotation(Class<?> aClass) {
		NaftahFnProvider naftahFnProvider = getClassAnnotation(aClass, NaftahFnProvider.class);
		return NaftahFunctionProvider.of(naftahFnProvider.name(), naftahFnProvider.description(), naftahFnProvider.functionNames());
	}

	/**
	 * Returns a list of all annotations present on the given method parameter.
	 *
	 * @param parameter The method parameter to inspect.
	 * @return A list of annotations present on the parameter.
	 */
	public static List<Annotation> getParametersAnnotations(Parameter parameter) {
		return Arrays.stream(parameter.getAnnotations()).toList();
	}

	/**
	 * Returns a specific annotation instance from the given method parameter.
	 *
	 * @param parameter       The parameter to inspect.
	 * @param annotationClass The class of the annotation to retrieve.
	 * @param <T>             The type of the annotation.
	 * @return The annotation instance if present, or {@code null} if not.
	 */
	public static <T extends Annotation> T getParametersAnnotation(Parameter parameter, Class<T> annotationClass) {
		return parameter.getAnnotation(annotationClass);
	}
}
