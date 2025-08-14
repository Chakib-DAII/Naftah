package org.daiitech.naftah.builtin.lang;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Method;

import org.daiitech.naftah.utils.reflect.ClassUtils;

/**
 * Represents a Java method that can be invoked dynamically.
 * <p>
 * This class wraps a {@link Method} along with its containing class,
 * and stores information about whether the method is static and invocable.
 * It also supports serialization, recreating the transient {@code Method}
 * upon deserialization by matching the method name.
 * </p>
 *
 * @author Chakib Daii
 */
public class JvmFunction implements Serializable {
	/**
	 * Fully qualified call signature of the method.
	 */
	private final String qualifiedCall;

	/**
	 * The class that declares the method.
	 */
	private final Class<?> clazz;

	/**
	 * The name of the method.
	 */
	private final String methodName;

	/**
	 * Whether the method is static.
	 */
	private final boolean isStatic;

	/**
	 * Whether the method is invocable.
	 */
	private final boolean isInvocable;

	/**
	 * The reflected method instance. Marked transient for serialization.
	 */
	private transient Method method;

	/**
	 * Constructs a new {@code JvmFunction}.
	 *
	 * @param qualifiedCall the fully qualified method call signature
	 * @param clazz         the class declaring the method
	 * @param method        the reflected method
	 * @param isStatic      true if the method is static
	 * @param isInvocable   true if the method is invocable
	 */
	public JvmFunction(String qualifiedCall, Class<?> clazz, Method method, boolean isStatic, boolean isInvocable) {
		this.qualifiedCall = qualifiedCall;
		this.clazz = clazz;
		this.method = method;
		this.methodName = method.getName();
		this.isStatic = isStatic;
		this.isInvocable = isInvocable;
	}

	/**
	 * Factory method to create a {@code JvmFunction} from a class and method.
	 *
	 * @param qualifiedCall the fully qualified method call signature
	 * @param clazz         the class declaring the method
	 * @param method        the reflected method
	 * @return a new {@code JvmFunction} instance
	 */
	public static JvmFunction of(String qualifiedCall, Class<?> clazz, Method method) {
		return new JvmFunction( qualifiedCall,
								clazz,
								method,
								ClassUtils.isStatic(method),
								ClassUtils.isInvocable(method));
	}

	/**
	 * Gets the fully qualified call signature.
	 *
	 * @return the qualified call string
	 */
	public String getQualifiedCall() {
		return qualifiedCall;
	}

	/**
	 * Gets the class declaring the method.
	 *
	 * @return the declaring class
	 */
	public Class<?> getClazz() {
		return clazz;
	}

	/**
	 * Gets the reflected method.
	 *
	 * @return the method instance
	 */
	public Method getMethod() {
		return method;
	}

	/**
	 * Gets the method's name.
	 *
	 * @return the method name
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * Checks if the method is static.
	 *
	 * @return true if static, false otherwise
	 */
	public boolean isStatic() {
		return isStatic;
	}

	/**
	 * Checks if the method is invocable.
	 *
	 * @return true if invocable, false otherwise
	 */
	public boolean isInvocable() {
		return isInvocable;
	}

	/**
	 * Custom serialization logic to write the object's non-transient fields.
	 *
	 * @param oos the object output stream
	 * @throws IOException if an I/O error occurs
	 */
	@Serial
	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
	}

	/**
	 * Custom deserialization logic to restore the transient {@link Method}
	 * by searching methods with matching name in the class.
	 *
	 * @param ois the object input stream
	 * @throws IOException            if an I/O error occurs
	 * @throws ClassNotFoundException if the class of a serialized object cannot be found
	 */
	@Serial
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		try {
			ois.defaultReadObject();
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

	/**
	 * Returns a string representation of this {@code JvmFunction}.
	 *
	 * @return a string in the format &lt;دالة qualifiedCall&gt;
	 */
	@Override
	public String toString() {
		return "<%s %s>".formatted("دالة", this.qualifiedCall);
	}
}
