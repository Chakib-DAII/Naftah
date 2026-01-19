// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

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

import org.daiitech.naftah.builtin.lang.BuiltinFunction;
import org.daiitech.naftah.builtin.lang.DynamicNumber;
import org.daiitech.naftah.builtin.lang.JvmExecutable;
import org.daiitech.naftah.builtin.lang.JvmFunction;
import org.daiitech.naftah.builtin.lang.NaN;
import org.daiitech.naftah.builtin.lang.NaftahObject;
import org.daiitech.naftah.builtin.lang.None;
import org.daiitech.naftah.builtin.utils.ObjectUtils;
import org.daiitech.naftah.builtin.utils.tuple.ImmutablePair;
import org.daiitech.naftah.builtin.utils.tuple.NTuple;
import org.daiitech.naftah.builtin.utils.tuple.Pair;
import org.daiitech.naftah.builtin.utils.tuple.Triple;
import org.daiitech.naftah.builtin.utils.tuple.Tuple;
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
public final class InvocationUtils {

	/**
	 * Private constructor to prevent instantiation.
	 * Always throws a {@link NaftahBugError} when called.
	 */
	private InvocationUtils() {
		throw newNaftahBugInvalidUsageError();
	}

	/**
	 * Dynamically invokes a JVM {@link Method} or {@link Constructor} using reflection.
	 *
	 * <p>This unified utility abstracts the complexity of calling either a {@link Method} or
	 * {@link Constructor} at runtime by automatically handling:</p>
	 * <ul>
	 * <li>Argument conversion to match JVM parameter types, including primitives, arrays,
	 * collections, and generic types</li>
	 * <li>Primitive boxing and unboxing</li>
	 * <li>Naftah-specific type handling and conversions (when enabled)</li>
	 * <li>Varargs executables, including automatic construction of the trailing vararg array
	 * when possible</li>
	 * </ul>
	 *
	 * <p>This method is designed for runtime environments that need to dynamically invoke JVM
	 * executables without compile-time type information.</p>
	 *
	 * <h3>Supported executable types</h3>
	 * <ul>
	 * <li><b>Instance methods:</b> invoked via {@link Method#invoke(Object, Object...)}</li>
	 * <li><b>Static methods:</b> invoked with a {@code null} instance</li>
	 * <li><b>Constructors:</b> invoked via {@link Constructor#newInstance(Object...)}</li>
	 * </ul>
	 *
	 * <h3>Argument handling</h3>
	 * <ul>
	 * <li>The number of provided arguments must match the executable’s parameter count</li>
	 * <li>If the executable is {@code varargs}, a missing final array argument may be
	 * synthesized automatically</li>
	 * <li>Arguments are converted <i>before</i> invocation and may be converted back
	 * into {@code naftahArgs} after invocation</li>
	 * </ul>
	 *
	 * <h3>Return handling</h3>
	 * <p>The raw result of the underlying reflective call is returned. Any Naftah-specific
	 * wrapping or post-processing is handled by the delegated invocation logic.</p>
	 *
	 * @param instance            the target object for instance method calls, or {@code null} for static methods
	 *                            or constructors
	 * @param methodOrConstructor the {@link Executable} to invoke (either a {@link Method} or a {@link Constructor})
	 * @param naftahArgs          a list of {@link Pair}&lt;String, Object&gt; representing argument names and values;
	 *                            this list may be mutated during invocation (e.g. for varargs handling or
	 *                            argument back-conversion)
	 * @param returnType          the expected return type; use {@link Void#TYPE} or {@link Void} for {@code void}
	 *                            executables
	 * @param useNaftahTypes      whether Naftah-specific type semantics and conversions should be applied
	 * @param <T>                 the type of executable being invoked
	 * @return the result of the invocation
	 * @throws InvocationTargetException if the underlying executable throws an exception
	 * @throws InstantiationException    if a constructor fails to create a new instance
	 * @throws IllegalAccessException    if the executable cannot be accessed due to Java access control
	 * @throws IllegalArgumentException  if argument count or types do not match the executable signature
	 * @see Method#invoke(Object, Object...)
	 * @see Constructor#newInstance(Object...)
	 * @see Executable
	 */
	public static <T extends Executable> Object invokeJvmExecutable(
																	Object instance,
																	T methodOrConstructor,
																	List<Pair<String, Object>> naftahArgs,
																	Class<?> returnType,
																	boolean useNaftahTypes)
			throws InvocationTargetException,
			InstantiationException,
			IllegalAccessException {
		Class<?>[] paramTypes = methodOrConstructor.getParameterTypes();

		if (naftahArgs.size() != paramTypes.length) {
			// in case of varargs executable; try to workaround missing array arg if it is the case
			if (methodOrConstructor.isVarArgs()) {
				Object varargArray = getVarargArrayIfPossible(naftahArgs, paramTypes);

				if (Objects.isNull(varargArray)) {
					throw new IllegalArgumentException("Argument count mismatch");
				}

				naftahArgs.add(ImmutablePair.of(null, varargArray));
			}
			else {
				throw new IllegalArgumentException("Argument count mismatch");
			}
		}

		Object[] executableArgs = new Object[naftahArgs.size()];
		Type[] genericTypes = methodOrConstructor.getGenericParameterTypes();

		for (int i = 0; i < naftahArgs.size(); i++) {
			executableArgs[i] = convertArgument(naftahArgs.get(i).getRight(),
												paramTypes[i],
												genericTypes[i],
												useNaftahTypes);
		}

		var result = invokeJvmExecutable(instance, methodOrConstructor, executableArgs, naftahArgs, returnType);

		convertArgumentsBack(executableArgs, naftahArgs);

		return result;
	}

	/**
	 * Creates a varargs array from a list of arguments if the method parameters indicate a varargs parameter.
	 * <p>
	 * This method checks if the last parameter in the {@code paramTypes} array is an array (indicating a varargs
	 * parameter)
	 * and if there are enough arguments in {@code naftahArgs} to populate the normal parameters. If so, it creates
	 * a new array for the varargs portion.
	 * </p>
	 *
	 * <p>
	 * Example usage:
	 * <pre>
	 * {@code
	 * List<Pair<String, Object>> args = List.of(Pair.of("arg1", value1), Pair.of("arg2", value2));
	 * Class<?>[] paramTypes = { String.class, Integer.class, String[].class }; // last is varargs
	 * Object varargsArray = getVarargArrayIfPossible(args, paramTypes);
	 * // varargsArray will be a String[0] if no extra arguments, or filled with remaining args otherwise
	 * }
	 * </pre>
	 * </p>
	 *
	 * @param naftahArgs the list of arguments as pairs of name and value
	 * @param paramTypes the array of parameter types for the target method, where the last element may be a varargs
	 *                   array
	 * @return a newly instantiated array of the varargs component type containing the extra arguments,
	 *         or {@code null} if there are not enough arguments to reach the varargs parameter
	 * @throws NullPointerException if {@code naftahArgs} or {@code paramTypes} is {@code null}
	 * @see java.lang.reflect.Array#newInstance(Class, int)
	 */
	private static Object getVarargArrayIfPossible(List<Pair<String, Object>> naftahArgs, Class<?>[] paramTypes) {
		// Varargs: the last parameter is an array type
		int normalParamCount = paramTypes.length - 1;

		// Too few parameters? skip
		if (naftahArgs.size() < normalParamCount) {
			return null;
		}

		// instantiate an empty array for varargs
		Class<?> varargComponentType = paramTypes[normalParamCount].getComponentType();
		int varargCount = naftahArgs.size() - normalParamCount;
		return Array.newInstance(varargComponentType, varargCount);
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
	 * <p>This method is a thin wrapper around
	 * {@link #invokeJvmExecutable(Object, Executable, List, Class, boolean)} that automatically
	 * supplies a {@code null} instance, as constructors do not require one.</p>
	 *
	 * <p>Constructor arguments are converted automatically to match the constructor’s parameter
	 * types, including primitives, arrays, collections, and generic types. Naftah-specific
	 * type semantics and conversions are applied when enabled.</p>
	 *
	 * <p>The provided argument list may be mutated during invocation (for example, to support
	 * varargs handling or argument back-conversion).</p>
	 *
	 * @param methodOrConstructor the {@link Constructor} to invoke
	 * @param args                the constructor arguments as a list of {@link Pair}&lt;String, Object&gt;
	 * @param returnType          the expected type of the constructed object
	 * @param useNaftahTypes      whether Naftah-specific type semantics and conversions should be applied
	 * @param <T>                 the type of executable (constructor) being invoked
	 * @return the newly created instance
	 * @throws InvocationTargetException if the constructor throws an exception
	 * @throws InstantiationException    if the constructor fails to instantiate a new object
	 * @throws IllegalAccessException    if reflective access is not allowed
	 * @throws IllegalArgumentException  if the argument count or types do not match the constructor signature
	 * @see #invokeJvmExecutable(Object, Executable, List, Class, boolean)
	 * @see Constructor#newInstance(Object...)
	 */
	public static <T extends Executable> Object invokeJvmConstructor(   T methodOrConstructor,
																		List<Pair<String, Object>> args,
																		Class<?> returnType,
																		boolean useNaftahTypes)
			throws InvocationTargetException,
			InstantiationException,
			IllegalAccessException {
		return invokeJvmExecutable(null, methodOrConstructor, args, returnType, useNaftahTypes);
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
	 * Converts a single argument to the target type expected by a reflective method
	 * or constructor parameter.
	 *
	 * <p>This method attempts to adapt the supplied {@code value} to the specified
	 * {@code targetType}. When available, {@code genericType} information is used
	 * to guide recursive, element-wise conversion of arrays, collections, tuples,
	 * and maps.</p>
	 *
	 * <h3>Supported conversions</h3>
	 * <ul>
	 * <li>Primitive types and their boxed equivalents</li>
	 * <li>{@link DynamicNumber} to standard JVM numeric types</li>
	 * <li>{@link NaN} to {@link Double#NaN}</li>
	 * <li>Arrays (recursive element conversion)</li>
	 * <li>{@link Collection Collections} (recursive element conversion)</li>
	 * <li>{@link Map Maps} (recursive key and value conversion)</li>
	 * <li>{@link NTuple}, {@link Pair}, and {@link Triple} (recursive element conversion)</li>
	 * <li>{@link NaftahObject} unwrapping when Naftah types are disabled</li>
	 * </ul>
	 *
	 * <h3>Naftah-specific semantics</h3>
	 * <ul>
	 * <li>If {@code value} is {@code null} or represents {@link None}, the result depends on
	 * {@code useNaftahTypes}:
	 * <ul>
	 * <li>when {@code true}, {@link None#get()} is returned</li>
	 * <li>when {@code false}, {@code null} is returned</li>
	 * </ul>
	 * </li>
	 * <li>When {@code useNaftahTypes} is {@code false}, {@link NaftahObject} values are
	 * automatically unwrapped unless the target type is {@link NaftahObject}</li>
	 * </ul>
	 *
	 * <p>If {@code value} is already assignable to {@code targetType}, it is returned unchanged.</p>
	 *
	 * @param value          the original argument value to convert; may be {@code null} or {@link None}
	 * @param targetType     the target class expected by the executable parameter
	 * @param genericType    generic type information used to guide recursive conversion of collections,
	 *                       arrays, and maps; may be {@code null}
	 * @param useNaftahTypes whether Naftah-specific type semantics and wrappers should be preserved
	 * @return a value compatible with {@code targetType}, or {@code null} if
	 *         {@code value} is {@code null} and Naftah types are disabled
	 * @throws ClassCastException if the value cannot be converted or cast to {@code targetType}
	 * @apiNote This method performs unchecked casts and reflective conversions.
	 *          Callers are responsible for ensuring that {@code targetType} and
	 *          {@code genericType} accurately describe the expected runtime types.
	 * @see #convertArgumentsBack(Object[], List)
	 * @see #convertArgumentBack(Object, Object)
	 */
	public static Object convertArgument(Object value, Class<?> targetType, Type genericType, boolean useNaftahTypes) {
		if (value == null || None.isNone(value)) {
			return useNaftahTypes ? None.get() : null;
		}

		if (!useNaftahTypes && !targetType.equals(NaftahObject.class) && value instanceof NaftahObject naftahObject) {
			value = naftahObject.get(true);
		}

		// Already assignable
		if (targetType.isInstance(value)) {
			return value;
		}

		if (!useNaftahTypes && value instanceof DynamicNumber dynamicNumber) {
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
				return (value instanceof Character character) ?
						character :
						Character.valueOf(value.toString().charAt(0));
			}
			if (targetType == byte.class) {
				return ((Number) value).byteValue();
			}
			if (targetType == short.class) {
				return ((Number) value).shortValue();
			}
		}

		// Handle Triple
		if (Pair.class.isAssignableFrom(targetType) && value instanceof Pair<?, ?> pair) {
			var left = pair.getLeft();
			var leftType = left.getClass();
			var right = pair.getRight();
			var rightType = right.getClass();
			return Pair
					.of(
						convertArgument(left, leftType, leftType, useNaftahTypes),
						convertArgument(right, rightType, rightType, useNaftahTypes)
					);
		}

		// Handle Triple
		if (Triple.class.isAssignableFrom(targetType) && value instanceof Triple<?, ?, ?> triple) {
			var left = triple.getLeft();
			var leftType = left.getClass();
			var middle = triple.getMiddle();
			var middleType = middle.getClass();
			var right = triple.getRight();
			var rightType = right.getClass();
			return Triple
					.of(
						convertArgument(left, leftType, leftType, useNaftahTypes),
						convertArgument(middle, middleType, middleType, useNaftahTypes),
						convertArgument(right, rightType, rightType, useNaftahTypes)
					);
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
												useNaftahTypes));
			}
			return newArray;
		}

		// Handle collections and Tuples
		if ((Collection.class.isAssignableFrom(targetType) || targetType
				.isArray()) && (value instanceof Collection<?> || value instanceof NTuple)) {
			Iterable<?> src = value instanceof NTuple nTuple ? List.of(nTuple.toArray()) : (Collection<?>) value;
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
				result.add(convertArgument(item, itemType, itemType, useNaftahTypes));
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
				Object newKey = convertArgument(entry.getKey(), keyType, keyType, useNaftahTypes);
				Object newValue = convertArgument(entry.getValue(), valueType, valueType, useNaftahTypes);
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
			Object original = currentPair.getRight();
			Object converted = executableArgs[i];


			// Skip if identical reference
			if (original == converted) {
				continue;
			}

			// Skip if logically equal (value equal)
			if (ObjectUtils.equals(original, converted, true)) {
				continue;
			}

			var merged = convertArgumentBack(original, converted);
			naftahArgs.set(i, ImmutablePair.of(currentPair.getLeft(), merged));
		}
	}

	/**
	 * Merges a converted argument value back into its original representation.
	 *
	 * <p>
	 * This method reconciles a value produced by a reflective invocation
	 * ({@code converted}) with the original argument value ({@code original}).
	 * Where possible, mutable objects are updated <em>in place</em>; otherwise,
	 * the original value or a replacement object is returned.
	 * </p>
	 *
	 * <h3>Supported merge behaviors</h3>
	 * <ul>
	 * <li>{@code null} or {@link None} — returns {@link None#get()}</li>
	 * <li>{@link NaN} — preserved unchanged</li>
	 * <li>{@link NaftahObject} — rewrapped with the converted value</li>
	 * <li>{@link DynamicNumber} — updated in place</li>
	 * <li>Primitive wrappers and {@link Number} types — merged via {@link DynamicNumber}</li>
	 * <li>Arrays — elements copied into the original array</li>
	 * <li>{@link Collection Collections} — elements updated in place</li>
	 * <li>{@link Map Maps} — contents replaced</li>
	 * <li>{@link NTuple}, {@link Pair}, {@link Triple}, {@link Tuple} — elements merged reflectively</li>
	 * </ul>
	 *
	 * <p>
	 * If {@code converted} is already assignable to the runtime type of
	 * {@code original}, it is returned directly.
	 * </p>
	 *
	 * <p>
	 * This method is primarily intended to propagate argument mutations made
	 * during reflective method or constructor execution back to the original
	 * argument objects.
	 * </p>
	 *
	 * @param original  the original argument value supplied to the executable
	 * @param converted the value produced by the reflective invocation
	 * @return the merged value, either the updated original instance or a suitable
	 *         replacement object
	 * @throws NaftahBugError if reflective field updates fail
	 * @apiNote This method relies on reflective field access and unchecked casts. Callers
	 *          must ensure that {@code original} and {@code converted} are structurally
	 *          compatible.
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

		// Handle Triple
		if (Pair.class.isAssignableFrom(targetType) && converted instanceof Pair<?, ?> pair) {
			try {
				ObjectAccessUtils
						.set(   original,
								"left",
								null,
								pair.getLeft(),
								false,
								true);

				ObjectAccessUtils
						.set(   original,
								"right",
								null,
								pair.getRight(),
								false,
								true);
			}
			catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
				throw new NaftahBugError(e);
			}
		}

		// Handle Triple
		if (Triple.class.isAssignableFrom(targetType) && converted instanceof Triple<?, ?, ?> triple) {
			try {
				ObjectAccessUtils
						.set(   original,
								"left",
								null,
								triple.getLeft(),
								false,
								true);

				ObjectAccessUtils
						.set(   original,
								"middle",
								null,
								triple.getMiddle(),
								false,
								true);


				ObjectAccessUtils
						.set(   original,
								"right",
								null,
								triple.getRight(),
								false,
								true);
			}
			catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
				throw new NaftahBugError(e);
			}
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

		// Handle collections and tuple
		Class<?> convertedType = converted.getClass();
		if ((Collection.class.isAssignableFrom(convertedType) || convertedType
				.isArray()) && (original instanceof Collection || original instanceof NTuple)) {
			if (original instanceof Pair<?, ?> pair) {
				try {
					ObjectAccessUtils
							.set(   pair,
									"left",
									null,
									getConvertedElementAt(converted, convertedType, 0),
									false,
									true);

					ObjectAccessUtils
							.set(   pair,
									"right",
									null,
									getConvertedElementAt(converted, convertedType, 1),
									false,
									true);
				}
				catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
					throw new NaftahBugError(e);
				}
			}
			else if (original instanceof Triple<?, ?, ?> triple) {
				try {
					ObjectAccessUtils
							.set(   triple,
									"left",
									null,
									getConvertedElementAt(converted, convertedType, 0),
									false,
									true);

					ObjectAccessUtils
							.set(   triple,
									"middle",
									null,
									getConvertedElementAt(converted, convertedType, 1),
									false,
									true);

					ObjectAccessUtils
							.set(   triple,
									"right",
									null,
									getConvertedElementAt(converted, convertedType, 2),
									false,
									true);
				}
				catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
					throw new NaftahBugError(e);
				}
			}
			else if (original instanceof Tuple tuple) {
				var tupleArray = tuple.toArray();
				for (int i = 0; i < tuple.size(); i++) {
					var convertedElement = getConvertedElementAt(converted, convertedType, i);
					Array.set(tupleArray, i, convertedElement);
				}
				try {
					ObjectAccessUtils
							.set(   tuple,
									"values",
									null,
									List.of(tupleArray),
									false,
									true);
				}
				catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
					throw new NaftahBugError(e);
				}
			}
			else {
				//noinspection unchecked,DataFlowIssue
				for (int i = 0; i < ((Collection<Object>) original).size(); i++) {
					var convertedElement = getConvertedElementAt(converted, convertedType, i);
					//noinspection unchecked,DataFlowIssue
					setElementAt((Collection<Object>) original, i, convertedElement);
				}
			}

			return original;
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
	 * Retrieves the element at the specified index from a converted composite value.
	 * <p>
	 * Supports both array and {@link Collection} representations. If
	 * {@code convertedType} represents an array, the element is obtained via
	 * {@link java.lang.reflect.Array#get(Object, int)}; otherwise the value is
	 * retrieved from the collection using index-based access.
	 * </p>
	 *
	 * @param converted     the converted composite value (array or {@link Collection})
	 * @param convertedType the runtime type of {@code converted}, used to distinguish arrays
	 *                      from collections
	 * @param i             the zero-based index of the element to retrieve
	 * @return the element at the specified index
	 * @throws IndexOutOfBoundsException if the index is out of range
	 * @throws ClassCastException        if {@code converted} is not compatible with {@code convertedType}
	 */
	private static Object getConvertedElementAt(Object converted, Class<?> convertedType, int i) {
		return convertedType.isArray() ? Array.get(converted, i) : getElementAt((Collection<?>) converted, i);
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
																					List<Pair<String, Object>> args)
			throws NoSuchMethodException {
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
																					boolean removeInstanceArg)
			throws NoSuchMethodException {
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
				removeInstanceArg = false;
			}

			if (paramTypes.length != args.size()) {
				// in case of varargs executable; try to workaround missing array arg if it is the case
				if (executable.isVarArgs()) {
					Object varargArray = getVarargArrayIfPossible(args, paramTypes);

					if (Objects.isNull(varargArray)) {
						continue;
					}

					args.add(ImmutablePair.of(null, varargArray));
				}
				else {
					continue;
				}
			}

			Pair<Integer, Object[]> scoreAndArgs = matchScore(  executable,
																paramTypes,
																args,
																jvmExecutable instanceof BuiltinFunction);
			if (scoreAndArgs.getLeft() >= 0 && scoreAndArgs.getLeft() < bestScore) {
				best = ImmutablePair.of(jvmExecutable, scoreAndArgs.getRight());
				bestScore = scoreAndArgs.getLeft();
			}
		}
		if (Objects.isNull(best)) {
			throw new NoSuchMethodException("No executable Found that matches the provided arguments.");
		}
		return best;
	}

	/**
	 * Computes a compatibility score describing how well a set of provided arguments
	 * matches the parameter types of a given {@link Executable}.
	 *
	 * <p>Each argument is tentatively converted to the corresponding parameter type
	 * using {@link #convertArgument(Object, Class, Type, boolean)}. If any argument
	 * fails conversion, the executable is considered incompatible and a score of
	 * {@code -1} is returned.</p>
	 *
	 * <h3>Scoring semantics</h3>
	 * <p>Lower scores indicate a better match. The total score is the sum of
	 * per-parameter penalties based on how closely the converted argument matches
	 * the target parameter type:</p>
	 *
	 * <ul>
	 * <li>{@code null} argument: +10</li>
	 * <li>Exact runtime type match: +0</li>
	 * <li>Assignable match (after conversion): +1</li>
	 * <li>Boxed numeric to boxed numeric: +2</li>
	 * <li>Boxed numeric to matching primitive: +3</li>
	 * <li>Numeric or character primitive mismatch: +4</li>
	 * <li>Other non-null compatible conversions: +5</li>
	 * <li>Converted value is {@code null}: +6</li>
	 * </ul>
	 *
	 * <p>If a converted argument is {@code null} but the corresponding parameter is
	 * primitive, the match fails immediately.</p>
	 *
	 * <h3>Naftah-specific behavior</h3>
	 * <p>The {@code useNaftahTypes} flag controls whether {@link None} and
	 * {@link NaftahObject} values are preserved or unwrapped during argument
	 * conversion.</p>
	 *
	 * @param executable     the method or constructor being evaluated
	 * @param params         the raw parameter types of the executable
	 * @param args           the provided arguments as a list of {@link Pair}&lt;String, Object&gt;
	 * @param useNaftahTypes whether Naftah-specific type semantics should be applied during conversion
	 * @param <T>            the type of {@link Executable}
	 * @return a {@link Pair} containing:
	 *         <ul>
	 *         <li>the computed compatibility score (lower is better, {@code -1} indicates incompatibility)</li>
	 *         <li>an array of converted argument values suitable for invocation</li>
	 *         </ul>
	 */
	private static <T extends Executable> Pair<Integer, Object[]> matchScore(   T executable,
																				Class<?>[] params,
																				List<Pair<String, Object>> args,
																				boolean useNaftahTypes) {
		int score = 0;
		Object[] executableArgs = new Object[args.size()];

		for (int i = 0; i < params.length; i++) {
			Object arg = args.get(i).getRight();
			Class<?> param = params[i];

			try {
				Object converted = convertArgument(arg, param, getGenericType(executable, i), useNaftahTypes);
				if (converted == null && param.isPrimitive()) {
					return ImmutablePair.of(-1, null);
				}

				// scoring: lower is better
				if (arg == null) {
					score += 10;
				}
				else if (param.isInstance(converted)) {
					score += 0;
				}
				else if (Objects.nonNull(converted)) {
					Class<?> convertedClass = converted.getClass();
					if (param.isAssignableFrom(convertedClass)) {
						score += 1;
					}
					else if (Number.class.isAssignableFrom(convertedClass)) {
						if (Number.class.isAssignableFrom(param)) {
							score += 2;
						}
						else if (param.isPrimitive()) {
							if (param == int.class && convertedClass == Integer.class || param == long.class && convertedClass == Long.class || param == float.class && convertedClass == Float.class || param == double.class && convertedClass == Double.class || param == short.class && convertedClass == Short.class || param == byte.class && convertedClass == Byte.class) {
								score += 3;
							}
							else {
								score += 4;
							}
						}
					}
					else if (convertedClass == Character.class) {
						if (param == Character.class) {
							score += 2;
						}
						else if (param.isPrimitive()) {
							if (param == char.class) {
								score += 3;
							}
							else {
								score += 4;
							}
						}
					}
					else {
						score += 5;
					}
				}
				else {
					score += 6;
				}
				executableArgs[i] = converted;
			}
			catch (Throwable ignored) {
				return ImmutablePair.of(-1, null); // conversion failed
			}
		}
		return ImmutablePair.of(score, executableArgs);
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
