package org.daiitech.naftah.utils.reflect;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.antlr.v4.runtime.misc.Pair;
import org.daiitech.naftah.builtin.lang.BuiltinFunction;
import org.daiitech.naftah.builtin.lang.DynamicNumber;
import org.daiitech.naftah.builtin.lang.JvmExecutable;
import org.daiitech.naftah.builtin.lang.JvmFunction;
import org.daiitech.naftah.builtin.lang.NaN;
import org.daiitech.naftah.builtin.lang.NaftahObject;
import org.daiitech.naftah.builtin.lang.None;
import org.daiitech.naftah.builtin.utils.ObjectUtils;
import org.daiitech.naftah.builtin.utils.op.BinaryOperation;
import org.daiitech.naftah.errors.NaftahBugError;

import static org.daiitech.naftah.builtin.utils.CollectionUtils.createCollection;
import static org.daiitech.naftah.builtin.utils.CollectionUtils.createMap;
import static org.daiitech.naftah.builtin.utils.CollectionUtils.getElementAt;
import static org.daiitech.naftah.builtin.utils.CollectionUtils.setElementAt;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;

/**
 * Utility class for reflective invocation of Java methods and constructors,
 * with support for type conversion and integration with the dynamically typed
 * programming language {@code Naftah}.
 *
 * <p>This class provides methods to:</p>
 * <ul>
 * <li>Invoke Java {@link java.lang.reflect.Method} and {@link java.lang.reflect.Constructor} instances
 * reflectively.</li>
 * <li>Handle named and positional arguments, including conversion between Java types and Naftah types such as
 * {@link NaftahObject} and {@link DynamicNumber}.</li>
 * <li>Support automatic handling of {@code null}, {@link None#get()}, and {@link NaN} values used in Naftah.</li>
 * <li>Perform deep conversions for arrays, collections, and maps to match expected Java method parameter types
 * .</li>
 * <li>Merge or convert arguments back to their original Naftah representations after method execution.</li>
 * </ul>
 *
 * <p>Typical usage scenarios:</p>
 * <ul>
 * <li>Invoking a Java constructor or method from Naftah code, where argument types may not exactly match Java
 * parameter types.</li>
 * <li>Interfacing between Naftah and Java, handling automatic type adaptation for Naftah objects.</li>
 * <li>Performing post-invocation conversion or merging of results back into Naftah objects, arrays, or
 * collections.</li>
 * </ul>
 *
 * <p>This class is designed for internal utility use in reflective invocation workflows,
 * especially when integrating Java APIs with the Naftah programming language.</p>
 *
 * @author Chakib Daii
 * @see #invokeJvmConstructor(Executable, Object[], List, Class)
 * @see #invokeJvmExecutable(Object, Executable, Object[], List, Class)
 * @see #convertArgument(Object, Class, Type, boolean)
 * @see #convertArgumentsBack(Object[], List)
 */
public class InvocationUtils {

	/**
	 * Private constructor to prevent instantiation.
	 * Always throws a {@link NaftahBugError} when called.
	 */
	private InvocationUtils() {
		throw newNaftahBugInvalidUsageError();
	}

	/**
	 * Dynamically invokes a Java {@link Method} or {@link Constructor} using reflection.
	 *
	 * <p>This unified utility abstracts the complexity of calling either a {@link Method} or
	 * {@link Constructor} by automatically handling:</p>
	 * <ul>
	 * <li>Parameter conversion, including primitives, arrays, collections, and generic types</li>
	 * <li>Null safety and {@link None} handling</li>
	 * <li>Primitive boxing/unboxing</li>
	 * </ul>
	 *
	 * <p>It is designed for runtime environments that need to dynamically invoke JVM executables
	 * without compile-time type information.</p>
	 *
	 * <h3>Supported executable types</h3>
	 * <ul>
	 * <li><b>Instance and static methods:</b> invoked via {@link Method#invoke(Object, Object...)}</li>
	 * <li><b>Constructors:</b> invoked via {@link Constructor#newInstance(Object...)}</li>
	 * </ul>
	 *
	 * <h3>Return handling</h3>
	 * <p>If {@code useNone} is {@code true}, {@code null} or {@code void} results are replaced with
	 * {@link None#get()} to ensure a non-null return value.</p>
	 *
	 * @param instance            the target object for instance method calls, or {@code null} for static methods or
	 *                            constructors
	 * @param methodOrConstructor the {@link Executable} to invoke (either {@link Method} or {@link Constructor})
	 * @param naftahArgs          a list of {@link Pair}&lt;String, Object&gt; representing argument names and values
	 * @param returnType          the expected return type; use {@link Void#TYPE} or {@link Void} for {@code void}
	 *                            methods
	 * @param useNone             if {@code true}, replaces {@code null} or {@code void} results with
	 *                            {@link None#get()}
	 * @param <T>                 the type of executable
	 * @return the invocation result, or {@link None#get()} if {@code null} or {@code void} and {@code useNone} is true
	 * @throws InvocationTargetException if the underlying executable throws an exception
	 * @throws InstantiationException    if a constructor fails to create a new instance
	 * @throws IllegalAccessException    if the executable cannot be accessed due to Java access control
	 * @throws IllegalArgumentException  if the argument count or types do not match the executable
	 * @see java.lang.reflect.Method#invoke(Object, Object...)
	 * @see java.lang.reflect.Constructor#newInstance(Object...)
	 * @see java.lang.reflect.Executable
	 * @see None#get()
	 */
	public static <T extends Executable> Object invokeJvmExecutable(
																	Object instance,
																	T methodOrConstructor,
																	List<Pair<String, Object>> naftahArgs,
																	Class<?> returnType,
																	boolean useNone)
			throws InvocationTargetException,
			InstantiationException,
			IllegalAccessException {
		Class<?>[] paramTypes = methodOrConstructor.getParameterTypes();

		if (naftahArgs.size() != paramTypes.length) {
			throw new IllegalArgumentException("Argument count mismatch");
		}

		Object[] executableArgs = new Object[naftahArgs.size()];
		Type[] genericTypes = methodOrConstructor.getGenericParameterTypes();

		for (int i = 0; i < naftahArgs.size(); i++) {
			executableArgs[i] = convertArgument(naftahArgs.get(i).b, paramTypes[i], genericTypes[i], useNone);
		}

		var result = invokeJvmExecutable(instance, methodOrConstructor, executableArgs, naftahArgs, returnType);

		convertArgumentsBack(executableArgs, naftahArgs);

		return result;
	}


	/**
	 * Invokes a given {@link Method} or {@link Constructor} reflectively with specified argument values.
	 *
	 * <p>This low-level helper performs:</p>
	 * <ul>
	 * <li>Access override via {@link java.lang.reflect.AccessibleObject#setAccessible(boolean)}</li>
	 * <li>Unwrapping of {@link NaftahObject} instances when necessary</li>
	 * <li>Automatic conversion of {@code void} or {@code null} results to {@link None#get()}</li>
	 * </ul>
	 *
	 * @param instance            the target object for instance methods, or {@code null} for static methods or
	 *                            constructors
	 * @param methodOrConstructor the {@link Executable} to invoke
	 * @param executableArgs      the argument values in declared parameter order
	 * @param naftahArgs          the original argument name/value pairs (used for back-conversion)
	 * @param returnType          the expected return type
	 * @param <T>                 the type of executable
	 * @return the invocation result, or {@link None#get()} if the result is {@code null} or {@code void}
	 * @throws InvocationTargetException if the underlying executable throws an exception
	 * @throws InstantiationException    if a constructor fails to instantiate a new object
	 * @throws IllegalAccessException    if the executable cannot be accessed
	 * @see java.lang.reflect.Method#invoke(Object, Object...)
	 * @see java.lang.reflect.Constructor#newInstance(Object...)
	 * @see NaftahObject
	 * @see None#get()
	 */
	public static <T extends Executable> Object invokeJvmExecutable(
																	Object instance,
																	T methodOrConstructor,
																	Object[] executableArgs,
																	List<Pair<String, Object>> naftahArgs,
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
		var result = returnType != Void.class && possibleResult != null ? possibleResult : None.get();

		convertArgumentsBack(executableArgs, naftahArgs);

		return result;
	}


	/**
	 * Convenience method to invoke a {@link Constructor} reflectively using a list of arguments.
	 *
	 * <p>This method is a wrapper around {@link #invokeJvmExecutable(Object, Executable, List, Class, boolean)}
	 * that automatically passes {@code null} as the instance, since constructors do not require one.</p>
	 *
	 * <p>Arguments are converted automatically to match the constructor's parameter types,
	 * including primitives, arrays, and generic types. {@code useNone} can replace {@code null} results with
	 * {@link None#get()}.</p>
	 *
	 * @param methodOrConstructor the {@link Constructor} to invoke
	 * @param args                the constructor arguments as a list of {@link Pair}&lt;String, Object&gt;
	 * @param returnType          the expected type of the constructed object
	 * @param useNone             if {@code true}, replaces {@code null} results with {@link None#get()}
	 * @param <T>                 the type of constructor
	 * @return the newly created instance, or {@link None#get()} if the result is {@code null} or {@code void}
	 * @throws InvocationTargetException if the constructor throws an exception
	 * @throws InstantiationException    if the constructor fails to instantiate a new object
	 * @throws IllegalAccessException    if reflective access is not allowed
	 * @throws IllegalArgumentException  if the argument count or types do not match the constructor
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
	 * <p>This is a convenience wrapper around {@link #invokeJvmExecutable(Object, Executable, Object[], List, Class)}
	 * that specifically targets constructors. Since constructors do not require a target object, this method
	 * automatically supplies {@code null} for the {@code instance} parameter.</p>
	 *
	 * <h3>Behavior overview</h3>
	 * <ul>
	 * <li>Invokes the provided {@link Constructor} using
	 * {@link java.lang.reflect.Constructor#newInstance(Object...)}.</li>
	 * <li>Delegates argument handling, type checking, and {@link None#get()} substitution to
	 * {@link #invokeJvmExecutable(Object, Executable, Object[], List, Class)}.</li>
	 * <li>Returns {@link None#get()} if the constructor result is {@code null}.</li>
	 * </ul>
	 *
	 * <p>Note: This method assumes that all arguments in {@code executableArgs} are already
	 * type-compatible with the constructor’s parameter types. For automatic type conversion,
	 * named arguments, or more complex argument handling, use the
	 * {@link #invokeJvmConstructor(Executable, List, Class, boolean)} variant.</p>
	 *
	 * @param methodOrConstructor the {@link Constructor} to invoke reflectively.
	 * @param executableArgs      an array of argument values to pass to the constructor.
	 * @param naftahArgs          a list of named argument pairs, for optional post-processing.
	 * @param returnType          the expected type of the constructed object.
	 * @param <T>                 the type of {@link Executable} (usually {@link Constructor}).
	 * @return the newly constructed instance, or {@link None#get()} if the result is {@code null}.
	 * @throws InvocationTargetException if the underlying constructor throws an exception.
	 * @throws InstantiationException    if the constructor cannot instantiate a new object.
	 * @throws IllegalAccessException    if reflective access is not permitted.
	 * @see #invokeJvmExecutable(Object, Executable, Object[], List, Class)
	 * @see java.lang.reflect.Constructor#newInstance(Object...)
	 * @see None#get()
	 */
	public static <T extends Executable> Object invokeJvmConstructor(   T methodOrConstructor,
																		Object[] executableArgs,
																		List<Pair<String, Object>> naftahArgs,
																		Class<?> returnType)
			throws InvocationTargetException,
			InstantiationException,
			IllegalAccessException {
		return invokeJvmExecutable(null, methodOrConstructor, executableArgs, naftahArgs, returnType);
	}

	/**
	 * Converts a single argument to the target type expected by a method parameter.
	 *
	 * <p>This method attempts to convert the provided {@code value} to the specified
	 * {@code targetType}, taking into account the provided {@code genericType} for
	 * collections and maps. It supports the following conversions:</p>
	 *
	 * <ul>
	 * <li>Primitive types (int, long, double, float, boolean, char, byte, short)</li>
	 * <li>{@link DynamicNumber} to numeric types</li>
	 * <li>Arrays (recursively converts elements)</li>
	 * <li>Collections (recursively converts elements, respects generic type if available)</li>
	 * <li>Maps (recursively converts keys and values, respects generic type if available)</li>
	 * <li>{@link NaftahObject} extraction, if the target type is not {@code NaftahObject}</li>
	 * </ul>
	 *
	 * <p>If the value is {@code null} or represents {@link None}, the returned value
	 * depends on the {@code useNone} flag: it returns {@link None#get()} if {@code useNone}
	 * is {@code true}, otherwise {@code null}.</p>
	 *
	 * @param value       the original argument value to convert
	 * @param targetType  the target type to which the value should be converted
	 * @param genericType the generic type information (for collections or maps), may be {@code null}
	 * @param useNone     if {@code true}, {@code null} or {@link None} values are converted to {@link None#get()}
	 * @return the converted argument value, compatible with the target type
	 * @throws ClassCastException if the value cannot be cast to the target type and no other conversion is possible
	 * @see #convertArgumentsBack(Object[], List)
	 * @see #convertArgumentBack(Object, Object)
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

			Class<?> itemType = Object.class;
			Collection<Object> result;
			if (targetType.isArray()) {
				itemType = targetType.getComponentType();
				result = createCollection(itemType);
			}
			else {
				if (genericType instanceof ParameterizedType pt) {
					Type[] typeArgs = pt.getActualTypeArguments();
					if (typeArgs.length == 1 && typeArgs[0] instanceof Class<?> c) {
						itemType = c;
					}
				}
				result = createCollection(targetType);
			}

			for (Object item : src) {
				result.add(convertArgument(item, itemType, itemType, useNone));
			}

			if (targetType.isArray()) {
				Object array = Array.newInstance(itemType, result.size());
				int i = 0;
				for (Object element : result) {
					Array.set(array, i++, element);
				}
				return array;
			}
			else {
				return result;
			}
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
	 * Updates the original argument list with values from the executed arguments array.
	 *
	 * <p>This method iterates through the provided {@code executableArgs} array and
	 * compares each element with the corresponding element in {@code naftahArgs}. If
	 * the original argument and the converted value differ (either by reference or
	 * logical equality), the method attempts to merge the changes back into the
	 * original object using {@link #convertArgumentBack(Object, Object)}.</p>
	 *
	 * <p>This is useful when invoking a method or constructor reflectively and wanting
	 * to propagate modifications of mutable arguments (like arrays, collections, or
	 * maps) back to the original argument references.</p>
	 *
	 * @param executableArgs the array of argument values used during the reflective call.
	 * @param naftahArgs     the list of original named arguments as {@code Pair<String, Object>},
	 *                       where the value part may be updated with the converted value.
	 * @see #convertArgumentBack(Object, Object)
	 */
	public static void convertArgumentsBack(
											Object[] executableArgs,
											List<Pair<String, Object>> naftahArgs) {
		if (executableArgs == null || naftahArgs == null || naftahArgs.isEmpty()) {
			return;
		}

		int limit = Math.min(naftahArgs.size(), executableArgs.length);

		for (int i = 0; i < limit; i++) {
			Pair<String, Object> currentPair = naftahArgs.get(i);
			Object original = currentPair.b;
			Object converted = executableArgs[i];


			// Skip if identical reference
			if (original == converted) {
				continue;
			}

			// Skip if logically equal (value equal)
			if (Boolean.TRUE.equals(ObjectUtils.applyOperation(original, converted, BinaryOperation.EQUALS))) {
				continue;
			}

			var merged = convertArgumentBack(original, converted);
			naftahArgs.set(i, new Pair<>(currentPair.a, merged));
		}
	}

	/**
	 * Merges a converted argument value back into its original form.
	 *
	 * <p>This method attempts to reconcile a new value ({@code converted}) with the
	 * original argument ({@code original}). It handles various types including:
	 * primitives, {@link DynamicNumber}, arrays, collections, maps, and custom objects
	 * like {@link NaftahObject}. For mutable types, the original object is updated
	 * in-place. For immutable types or when in-place modification is not possible, a
	 * new object is returned.</p>
	 *
	 * <p>Use this method to propagate changes made during reflective method or
	 * constructor execution back to the original arguments.</p>
	 *
	 * @param original  the original argument value passed to the executable.
	 * @param converted the value resulting from the reflective call.
	 * @return the merged argument value, either the updated original or a new instance,
	 *         suitable for replacing the original reference.
	 * @see #convertArgumentsBack(Object[], List)
	 */
	public static Object convertArgumentBack(Object original, Object converted) {
		if (original == null || None.isNone(original)) {
			return None.get();
		}

		if (NaN.isNaN(original)) {
			return original;
		}

		if (original instanceof NaftahObject) {
			original = NaftahObject.of(converted);
		}

		Class<?> targetType = original.getClass();

		if (targetType.isInstance(converted)) {
			return converted;
		}


		if (original instanceof DynamicNumber dynamicNumber) {
			dynamicNumber.set((Number) converted);
		}

		if (targetType == Boolean.class) {
			return converted;
		}
		if (targetType == Character.class) {
			return converted;
		}

		// Handle primitives
		if (targetType.isPrimitive() || Number.class.isAssignableFrom(targetType)) {
			return DynamicNumber.of(converted);
		}

		// Handle arrays
		if (targetType.isArray() && converted.getClass().isArray()) {
			int length = Array.getLength(original);
			for (int i = 0; i < length; i++) {
				Object element = Array.get(converted, i);
				Array.set(original, i, element);
			}
			return original;
		}

		// Handle collections
		Class<?> convertedType = converted.getClass();
		if ((Collection.class.isAssignableFrom(convertedType) || convertedType
				.isArray()) && original instanceof @SuppressWarnings("rawtypes")
		Collection src) {

			for (int i = 0; i < src.size(); i++) {
				var convertedElement = convertedType.isArray() ?
						Array.get(converted, i) :
						getElementAt((Collection<?>) converted, i);
				//noinspection unchecked
				setElementAt(src, i, convertedElement);
			}

			return src;
		}


		// Handle maps
		//noinspection rawtypes
		if (converted instanceof Map convertedMap && original instanceof Map srcMap) {
			srcMap.clear();
			//noinspection unchecked
			srcMap.putAll(convertedMap);
			return srcMap;
		}

		return original;
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
