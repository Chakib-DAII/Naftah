package org.daiitech.naftah.builtin.lang;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.daiitech.naftah.utils.arabic.ArabicUtils;
import org.daiitech.naftah.utils.reflect.ClassUtils;

import static org.daiitech.naftah.utils.reflect.ClassUtils.getQualifiedName;

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
	 * The method parameter types.
	 */
	private final Class<?>[] methodParameterTypes;

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
		this.methodParameterTypes = method.getParameterTypes();
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
	 * by searching methods with matching name and parameter types in the class.
	 *
	 * @param ois the object input stream
	 * @throws IOException            if an I/O error occurs
	 * @throws ClassNotFoundException if the class of a serialized object cannot be found
	 */
	@Serial
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		try {
			ois.defaultReadObject();
			for (Method m : clazz.getMethods()) {
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
	 * Returns a concise string representation of this {@code JvmFunction},
	 * formatted in Arabic as {@code <دالة qualifiedCall>}.
	 * <p>
	 * This is intended for short, human-readable descriptions, using the Arabic label "دالة"
	 * (which means "function" or "method").
	 *
	 * @return a string in the format {@code <دالة qualifiedCall>}
	 */
	@Override
	public String toString() {
		return "<%s %s>".formatted("دالة", this.qualifiedCall);
	}

	/**
	 * Returns a detailed, Arabic-formatted string representation of this {@code JvmFunction},
	 * including metadata about the method, such as return type, parameters, modifiers,
	 * and annotations.
	 * <p>
	 * The output is structured in a human-readable Arabic format and includes
	 * phonetic transliterations (into Arabic script) for class names, return types,
	 * modifiers, and annotations using {@link ClassUtils#getQualifiedName(String)} or
	 * {@link ArabicUtils#transliterateToArabicScriptDefault(String...)}.
	 * <p>
	 * The following information is included:
	 * <ul>
	 * <li><strong>Qualified method name</strong> — including both the fully qualified and transliterated form</li>
	 * <li><strong>Return type</strong> — full Java type name with its Arabic transliteration</li>
	 * <li><strong>Parameters</strong> — each parameter type with its fully qualified name and transliteration</li>
	 * <li><strong>Modifiers</strong> — Java keywords (e.g., {@code public static}) with their Arabic
	 * transliteration</li>
	 * <li><strong>Annotations</strong> — fully qualified annotation names with transliterations</li>
	 * </ul>
	 *
	 * <p><strong>Example output:</strong>
	 * <pre>
	 * تفاصيل الدالة:
	 * - الاسم المؤهل: com.example.MyClass::greet - كوم:إِكْزامْبِل:ماي_كْلاس::غْرِيتْ
	 * - نوع الإرجاع: java.lang.String - جافا:لانغ:سترينج
	 * - المعاملات:
	 * - java.lang.String - جافا:لانغ:سترينج
	 * - المُعدّلات: public static - بَبْلِكْ سْتَاتِكْ
	 * - التعليقات التوضيحية:
	 * - @java.lang.Deprecated - جافا:لانغ:دِبْرِكَيْتِدْ
	 * </pre>
	 *
	 * @return a formatted, multi-line Arabic string describing the method's structure and metadata
	 */
	public String toDetailedString() {
		StringBuilder detailedString = new StringBuilder();

		detailedString
				.append("""
							تفاصيل الدالة:
							\t- الاسم المؤهل: %s::%s - %s
						"""
						.formatted( clazz.getName(),
									methodName,
									ClassUtils
											.getQualifiedCall(  ClassUtils.getQualifiedName(clazz.getName()),
																methodName)));

		if (Objects.nonNull(method)) {
			detailedString
					.append("\t- نوع الإرجاع: ")
					.append(method.getReturnType().getName())
					.append(" - ")
					.append(getQualifiedName(method.getReturnType().getName()))
					.append("\n");

			Class<?>[] paramTypes = method.getParameterTypes();
			detailedString.append("\t- المعاملات:\n");
			if (paramTypes.length == 0) {
				detailedString.append("\t\t- لا يوجد\n");
			}
			else {
				for (Class<?> param : paramTypes) {
					detailedString
							.append("\t\t- ")
							.append(param.getName())
							.append(" - ")
							.append(getQualifiedName(param.getName()))
							.append("\n");
				}
			}

			var modifiers = Modifier.toString(method.getModifiers());
			var modifiersArray = modifiers.split("\\s");
			var modifiersArabicArray = ArabicUtils
					.transliterateToArabicScriptDefault(
														modifiersArray.clone());
			detailedString
					.append("\t- المُعدّلات: ")
					.append(IntStream
							.range(0, modifiersArray.length)
							.mapToObj(index -> "%s (%s)"
									.formatted( modifiersArabicArray[index],
												modifiersArray[index]))
							.collect(Collectors.joining(" ")))
					.append("\n");

			var annotations = method.getDeclaredAnnotations();
			detailedString.append("\t- التعليقات التوضيحية:\n");
			if (annotations.length == 0) {
				detailedString.append("\t\t- لا يوجد\n");
			}
			else {
				for (var annotation : annotations) {
					detailedString
							.append("\t\t- @")
							.append(annotation.annotationType().getName())
							.append(" - ")
							.append(getQualifiedName(annotation.annotationType().getName()))
							.append("\n");
				}
			}
		}

		return detailedString.toString();
	}
}
