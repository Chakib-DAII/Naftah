package org.daiitech.naftah.builtin.lang;

import static org.daiitech.naftah.parser.NaftahParserHelper.NULL;

/**
 * this class represents a singleton value indicating the absence of a value.
 * <p>
 * This is similar to {@code null} in Java, but it's implemented
 * as a safe, immutable object to avoid {@link NullPointerException}s.
 * <p>
 * Used to represent "no value" in expression evaluations or logic operations.
 *
 * @author Chakib Daii
 */
public final class None {

	/**
	 * The singleton instance of {@code None}.
	 */
	private static final None INSTANCE = new None();

	/**
	 * Private constructor to prevent external instantiation.
	 */
	private None() {
	}

	/**
	 * Returns the singleton instance representing {@code None}.
	 *
	 * @return the {@code None} instance
	 */
	public static Object get() {
		return INSTANCE;
	}

	/**
	 * Checks whether the given object is the {@code None} instance.
	 *
	 * @param object the object to check
	 * @return {@code true} if the object is {@code None}; {@code false} otherwise
	 */
	public static boolean isNone(Object object) {
		return object == INSTANCE;
	}

	/**
	 * Returns the string representation of this {@code None} object.
	 *
	 * @return a string indicating absence of value (can be customized)
	 */
	@Override
	public String toString() {
		return NULL;
	}
}
