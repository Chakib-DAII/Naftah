// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

package org.daiitech.naftah.builtin.lang;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Executable;

/**
 * Represents any executable entity in the JVM runtime model, such as a method,
 * a constructor, or a class initializer (clinit block).
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
	 * represented by this object.
	 *
	 * <p>The returned executable may be a {@link java.lang.reflect.Method}
	 * or {@link java.lang.reflect.Constructor}, depending on the concrete
	 * implementation.</p>
	 *
	 * @return the associated {@link java.lang.reflect.Executable}
	 */
	Executable getExecutable();

	/**
	 * Returns the {@link MethodHandle} corresponding to
	 * the underlying executable.
	 *
	 * <p>The handle is typically resolved via
	 * {@link java.lang.invoke.MethodHandles.Lookup} and may be cached
	 * for repeated invocations.</p>
	 *
	 * <p>Callers should prefer this handle for invocation performance.
	 * If access restrictions prevent handle creation, an
	 * {@link IllegalAccessException} is thrown and the caller may
	 * fall back to reflective invocation.</p>
	 *
	 * @return the resolved {@link MethodHandle}
	 * @throws IllegalAccessException if the handle cannot be created
	 *                                due to JVM access control restrictions
	 */
	MethodHandle getMethodHandle() throws IllegalAccessException;
}
