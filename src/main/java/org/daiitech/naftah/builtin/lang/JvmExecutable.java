package org.daiitech.naftah.builtin.lang;

import java.lang.reflect.Executable;

/**
 * Represents any executable entity in the JVM runtime model, such as a method,
 * a constructor, or a class initializer (<clinit> block).
 *
 * <p>This interface provides a common abstraction for Java reflection
 * {@link java.lang.reflect.Executable Executable} objects, allowing unified
 * access to both built-in and user-defined callable elements.
 * </p>
 *
 * <p>The {@code JvmExecutable} hierarchy is {@code sealed}, restricting which
 * types may implement it to ensure well-defined behavior within the runtime model.
 * The permitted implementors are:
 * <ul>
 * <li>{@link BuiltinFunction}</li>
 * <li>{@link JvmFunction}</li>
 * <li>{@link JvmClassInitializer}</li>
 * </ul>
 * </p>
 *
 * @author Chakib Daii
 * @see java.lang.reflect.Method
 * @see java.lang.reflect.Constructor
 */
public sealed interface JvmExecutable permits BuiltinFunction, JvmFunction, JvmClassInitializer {
	/**
	 * Returns the underlying reflective {@link java.lang.reflect.Executable}
	 * instance represented by this object.
	 *
	 * <p>This may correspond to a Java method, constructor, or other callable
	 * element recognized by the JVM.</p>
	 *
	 * @return the associated {@link java.lang.reflect.Executable} instance
	 */
	Executable getExecutable();
}
