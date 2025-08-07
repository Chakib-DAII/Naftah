package org.daiitech.naftah.builtin.lang;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * representation of a builtin function.
 *
 * @author Chakib Daii
 */
public class BuiltinFunction implements Serializable {
	private final String methodName;
	private final String className;
	private final NaftahFunctionProvider providerInfo;
	private final NaftahFunction functionInfo;
	private transient Method method;

	public BuiltinFunction(Method method, NaftahFunctionProvider providerInfo, NaftahFunction functionInfo) {
		this.method = method;
		this.methodName = method.getName();
		this.className = method.getDeclaringClass().getName();
		this.providerInfo = providerInfo;
		this.functionInfo = functionInfo;
	}

	public static BuiltinFunction of(Method method, NaftahFunctionProvider providerInfo, NaftahFunction functionInfo) {
		return new BuiltinFunction(method, providerInfo, functionInfo);
	}

	public Method getMethod() {
		return method;
	}

	public NaftahFunctionProvider getProviderInfo() {
		return providerInfo;
	}

	public NaftahFunction getFunctionInfo() {
		return functionInfo;
	}

	@Serial
	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
	}

	@Serial
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		try {
			ois.defaultReadObject();
			Class<?> clazz = Class.forName(className);
			for (Method m : clazz.getDeclaredMethods()) {
				if (m.getName().equals(methodName)) {
					this.method = m;
					break;
				}
			}
		}
		catch (Throwable ignored) {
		}
	}

	@Override
	public String toString() {
		return "<%s %s>".formatted("دالة", this.getFunctionInfo().name());
	}
}
