// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

package org.daiitech.naftah.utils.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.daiitech.naftah.builtin.utils.tuple.ImmutablePair;
import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.utils.script.ScriptUtils;

import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;

/**
 * Utility class for reflective access to object fields and properties.
 *
 * <p>This class provides methods to get and set field values on Java objects,
 * using either direct field access or getter/setter methods if available.
 * It is designed to work with JavaBeans-style properties as well as raw fields.
 *
 * <p>All methods are static, and the class cannot be instantiated. Attempting
 * to instantiate it will throw a {@link NaftahBugError}.
 *
 * <p>Typical usage examples:
 * <pre>{@code
 * Object value = ObjectAccessUtils.get(person, "name", null);
 * boolean updated = ObjectAccessUtils.set(person, "age", null, 30);
 * }</pre>
 * *
 *
 * @author Chakib Daii
 */
public final class ObjectAccessUtils {

	/**
	 * Function to build the setter method name for a given field.
	 * <p>Example: for field "name", returns "setName".</p>
	 */
	public static Function<String, String> BUILD_SETTER = (filedName) -> "set" + capitalize(filedName);

	/**
	 * Function to build possible getter method names for a given field.
	 * <p>For a field "active", returns an array with:</p>
	 * <ul>
	 * <li>"getActive"</li>
	 * <li>"isActive"</li>
	 * <li>"active" (direct method)</li>
	 * </ul>
	 */
	public static Function<String, String[]> BUILD_GETTERS = (filedName) -> {
		String capital = capitalize(filedName);
		return new String[]{
							"get" + capital,
							"is" + capital,
							filedName
		};
	};

	/**
	 * Private constructor to prevent instantiation.
	 * Always throws a {@link NaftahBugError} when called.
	 */
	private ObjectAccessUtils() {
		throw newNaftahBugInvalidUsageError();
	}

	/**
	 * Retrieves the value of a field or property from a target object.
	 *
	 * <p>If a {@code getter} method is provided, it will be invoked first,
	 * optionally via the associated {@link MethodHandle}. If the getter is not
	 * provided or fails (and {@code failFast} is {@code false}), the method
	 * attempts to access the field directly via reflection.</p>
	 *
	 * <p>Exceptions can be swallowed or propagated depending on {@code safe}
	 * and {@code failFast} flags.</p>
	 *
	 * @param target             the object from which to retrieve the value; may be {@code null}
	 * @param name               the field name; may be {@code null}
	 * @param getter             optional getter {@link Method} to invoke; may be {@code null}
	 * @param getterMethodHandle optional getter {@link MethodHandle} to invoke; may be {@code null}
	 * @param safe               if {@code true}, exceptions are suppressed and {@code null} is returned
	 * @param failFast           if {@code true}, exceptions during getter invocation are thrown immediately
	 * @return the value of the field or property, or {@code null} if not found or suppressed
	 * @throws Throwable if invocation fails and {@code failFast} is {@code true} or {@code safe} is {@code false}
	 */
	public static Object get(   Object target,
								String name,
								Method getter,
								MethodHandle getterMethodHandle,
								boolean safe,
								boolean failFast)
			throws Throwable {
		if (target == null || name == null) {
			return null;
		}

		if (getter != null) {
			try {
				return InvocationUtils
						.invokeJvmExecutable(   target,
												getter,
												getterMethodHandle,
												new Object[]{},
												List.of(),
												getter.getReturnType());
			}
			catch (Throwable th) {
				if (failFast) {
					throw th;
				}
			}
		}

		Class<?> cls = target.getClass();

		// Field
		Field field = findField(cls, name, safe);
		if (field != null) {
			try {
				field.setAccessible(true);
				return field.get(target);
			}
			catch (Throwable th) {
				if (!safe) {
					throw th;
				}
			}
		}

		if (!safe) {
			throw newNaftahNoSuchFieldError(name);
		}

		return null;
	}

	/**
	 * Sets the value of a field or property on a target object.
	 *
	 * <p>If a {@code setter} method is provided, it will be invoked first,
	 * optionally via the associated {@link MethodHandle}. If the setter is not
	 * provided or fails (and {@code failFast} is {@code false}), the method
	 * attempts to set the field directly via reflection.</p>
	 *
	 * <p>Exceptions can be suppressed or propagated depending on {@code safe}
	 * and {@code failFast} flags.</p>
	 *
	 * @param target             the object on which to set the value; may be {@code null}
	 * @param name               the field name; may be {@code null}
	 * @param setter             optional setter {@link Method} to invoke; may be {@code null}
	 * @param setterMethodHandle optional setter {@link MethodHandle} to invoke; may be {@code null}
	 * @param value              the value to assign to the field
	 * @param safe               if {@code true}, exceptions are suppressed and {@code false} is returned on failure
	 * @param failFast           if {@code true}, exceptions during setter invocation are thrown immediately
	 * @return {@code true} if the value was successfully set, {@code false} otherwise
	 * @throws Throwable if invocation fails and {@code failFast} is {@code true} or {@code safe} is {@code false}
	 */
	public static boolean set(  Object target,
								String name,
								Method setter,
								MethodHandle setterMethodHandle,
								Object value,
								boolean safe,
								boolean failFast)
			throws Throwable {
		if (target == null || name == null) {
			return false;
		}

		if (setter != null) {
			try {
				InvocationUtils
						.invokeJvmExecutable(   target,
												setter,
												setterMethodHandle,
												List.of(ImmutablePair.of(null, value)),
												setter.getReturnType(),
												false);
				return true;
			}
			catch (Throwable th) {
				if (failFast) {
					throw th;
				}
			}
		}

		Class<?> cls = target.getClass();

		// Field
		Field field = findField(cls, name, safe);
		if (field != null) {
			try {
				field.setAccessible(true);
				field.set(target, value);
				return true;
			}
			catch (Throwable th) {
				if (!safe) {
					throw th;
				}
			}
		}

		if (!safe) {
			throw newNaftahNoSuchFieldError(name);
		}

		return false;
	}

	/**
	 * Finds a declared field with the given name in the given class or its superclasses.
	 *
	 * @param cls  the class to search
	 * @param name the field name
	 * @return the {@link Field} if found, otherwise {@code null}
	 */
	public static Field findField(Class<?> cls, String name, boolean safe) {
		Class<?> current = cls;
		while (current != null && current != Object.class) {
			try {
				return Arrays
						.stream(current.getDeclaredFields())
						.filter(f -> !Modifier.isStatic(f.getModifiers()) && (ScriptUtils
								.transliterateToArabicScriptDefault(f.getName())[0]
								.equals(name) || f.getName().equals(name)))
						.findFirst()
						.orElseThrow(() -> newNaftahNoSuchFieldError(name));
			}
			catch (NaftahBugError ignored) {
				current = current.getSuperclass();
			}
		}

		if (!safe) {
			throw newNaftahNoSuchFieldError(name);
		}

		return null;
	}

	/**
	 * Capitalizes the first character of the given string.
	 *
	 * @param str the input string
	 * @return the string with the first character capitalized, or the original string if empty/null
	 */
	private static String capitalize(String str) {
		if (str == null || str.isEmpty()) {
			return str;
		}
		return ScriptUtils.isArabicText(str) ? str : Character.toUpperCase(str.charAt(0)) + str.substring(1);
	}

	/**
	 * Creates a {@link NaftahBugError} when a field with the given name
	 * cannot be found.
	 *
	 * <p>The field name is expected to be in Arabic. This wraps the
	 * standard {@link NoSuchFieldException} inside a {@link NaftahBugError}.</p>
	 *
	 * @param name the Arabic name of the field that was not found
	 * @return a {@link NaftahBugError} indicating that the field does not exist
	 * @throws NaftahBugError always thrown to indicate the missing field
	 */
	private static NaftahBugError newNaftahNoSuchFieldError(String name) {
		return new NaftahBugError("لم يتم العثور على الحقل باسم عربي: " + name, new NoSuchFieldException(name));
	}
}
