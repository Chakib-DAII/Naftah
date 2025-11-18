package org.daiitech.naftah.utils.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;

import org.antlr.v4.runtime.misc.Pair;
import org.daiitech.naftah.errors.NaftahBugError;

import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;

public final class ObjectAccessUtils {

	public static Function<String, String> BUILD_SETTER = (filedName) -> "set" + capitalize(filedName);
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

	private static String capitalize(String str) {
		if (str == null || str.isEmpty()) {
			return str;
		}
		return Character.toUpperCase(str.charAt(0)) + str.substring(1);
	}
}
