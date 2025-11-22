package org.daiitech.naftah.utils.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;

import org.antlr.v4.runtime.misc.Pair;
import org.daiitech.naftah.errors.NaftahBugError;

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
	 * <p>If a {@code getter} method is provided, it will be invoked. Otherwise,
	 * the method will attempt to access the field directly using reflection.
	 *
	 * @param target the object from which to retrieve the value; may be null
	 * @param name   the field name; may be null
	 * @param getter optional getter {@link Method} to invoke; may be null
	 * @return the value of the field or property, or {@code null} if not found
	 */
	public static Object get(Object target, String name, Method getter) {
		if (target == null || name == null) {
			return null;
		}

		if (getter != null) {
			try {
				return InvocationUtils
						.invokeJvmExecutable(   target,
												getter,
												new Object[]{},
												List.of(),
												getter.getReturnType());
			}
			catch (Exception ignored) {
			}
		}

		Class<?> cls = target.getClass();

		// Field
		Field field = findField(cls, name);
		if (field != null) {
			try {
				field.setAccessible(true);
				return field.get(target);
			}
			catch (Exception ignored) {
			}
		}

		return null;
	}

	/**
	 * Sets the value of a field or property on a target object.
	 *
	 * <p>If a {@code setter} method is provided, it will be invoked. Otherwise,
	 * the method will attempt to access the field directly using reflection.
	 *
	 * @param target the object on which to set the value; may be null
	 * @param name   the field name; may be null
	 * @param setter optional setter {@link Method} to invoke; may be null
	 * @param value  the value to assign to the field
	 * @return {@code true} if the value was successfully set, {@code false} otherwise
	 */
	public static boolean set(Object target, String name, Method setter, Object value) {
		if (target == null || name == null) {
			return false;
		}

		if (setter != null) {
			try {
				InvocationUtils
						.invokeJvmExecutable(   target,
												setter,
												List.of(new Pair<>(null, value)),
												setter.getReturnType(),
												false);
				return true;
			}
			catch (Exception ignored) {
			}
		}

		Class<?> cls = target.getClass();

		// Field
		Field field = findField(cls, name);
		if (field != null) {
			try {
				field.setAccessible(true);
				field.set(target, value);
				return true;
			}
			catch (Exception ignored) {
			}
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
	private static Field findField(Class<?> cls, String name) {
		Class<?> current = cls;
		while (current != null && current != Object.class) {
			try {
				return current.getDeclaredField(name);
			}
			catch (NoSuchFieldException ignored) {
				current = current.getSuperclass();
			}
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
		return Character.toUpperCase(str.charAt(0)) + str.substring(1);
	}
}
