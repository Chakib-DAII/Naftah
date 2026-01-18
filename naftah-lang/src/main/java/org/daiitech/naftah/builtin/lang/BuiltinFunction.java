// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

package org.daiitech.naftah.builtin.lang;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Representation of a builtin function.
 * <p>
 * This class wraps a {@link Method} along with additional metadata
 * about the function's provider and function information.
 * It supports serialization by storing the method's class and method names
 * and restoring the {@code Method} reference upon deserialization.
 * </p>
 *
 * @author Chakib Daii
 */
public final class BuiltinFunction implements Serializable, JvmExecutable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * The name of the method.
	 */
	private final String methodName;
	/**
	 * The method parameter types.
	 */
	private final Class<?>[] methodParameterTypes;

	/**
	 * The fully qualified name of the class declaring the method.
	 */
	private final String className;

	/**
	 * Provider information for this function.
	 */
	private final NaftahFunctionProvider providerInfo;

	/**
	 * Function information for this function.
	 */
	private final NaftahFunction functionInfo;

	/**
	 * The reflected method instance. Marked transient because
	 * it is not serializable and restored after deserialization.
	 */
	private transient Method method;

	/**
	 * Constructs a {@code BuiltinFunction} with the given method and metadata.
	 *
	 * @param method       the reflected method representing this builtin function
	 * @param providerInfo the provider information for the function
	 * @param functionInfo the function-specific information
	 */
	public BuiltinFunction(Method method, NaftahFunctionProvider providerInfo, NaftahFunction functionInfo) {
		this.method = method;
		this.methodName = method.getName();
		this.methodParameterTypes = method.getParameterTypes();
		this.className = method.getDeclaringClass().getName();
		this.providerInfo = providerInfo;
		this.functionInfo = functionInfo;
	}

	/**
	 * Static factory method to create a new {@code BuiltinFunction} instance.
	 *
	 * @param method       the reflected method representing this builtin function
	 * @param providerInfo the provider information for the function
	 * @param functionInfo the function-specific information
	 * @return a new {@code BuiltinFunction} instance
	 */
	public static BuiltinFunction of(Method method, NaftahFunctionProvider providerInfo, NaftahFunction functionInfo) {
		return new BuiltinFunction(method, providerInfo, functionInfo);
	}

	/**
	 * Returns the reflected {@link Method} instance representing this builtin function.
	 *
	 * @return the method instance
	 */
	public Method getMethod() {
		return method;
	}

	/**
	 * Returns the provider information associated with this function.
	 *
	 * @return the function provider information
	 */
	public NaftahFunctionProvider getProviderInfo() {
		return providerInfo;
	}

	/**
	 * Returns the function-specific information associated with this function.
	 *
	 * @return the function information
	 */
	public NaftahFunction getFunctionInfo() {
		return functionInfo;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Executable getExecutable() {
		return method;
	}

	/**
	 * Custom serialization logic.
	 * Writes the non-transient fields using default serialization.
	 *
	 * @param oos the output stream to write to
	 * @throws IOException if an I/O error occurs
	 */
	@Serial
	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
	}

	/**
	 * Custom deserialization logic.
	 * Reads the non-transient fields using default deserialization,
	 * then restores the transient {@code method} field by locating
	 * the method by name in the deserialized class.
	 *
	 * @param ois the input stream to read from
	 * @throws IOException            if an I/O error occurs
	 * @throws ClassNotFoundException if the class for the method cannot be found
	 */
	@Serial
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		try {
			ois.defaultReadObject();
			Class<?> clazz = Class.forName(className);
			for (Method m : clazz.getDeclaredMethods()) {
				if (m.getName().equals(methodName) && Arrays.equals(m.getParameterTypes(), methodParameterTypes)) {
					this.method = m;
					break;
				}
			}
		}
		catch (Throwable ignored) {
		}
	}

	/**
	 * Returns a string representation of this builtin function.
	 * The format is &lt;دالة functionName&gt;.
	 *
	 * @return a string describing this builtin function
	 */
	@Override
	public String toString() {
		return "<%s %s>".formatted("دالة", functionInfo.name());
	}

	public String toDetailedString() {
		return """
				تفاصيل المزود:
				\t\t%s

				تفاصيل الدالة:
				\t\t%s"""
				.formatted(
							providerInfo,
							functionInfo);
	}
}
