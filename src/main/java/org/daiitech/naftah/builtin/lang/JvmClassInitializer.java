package org.daiitech.naftah.builtin.lang;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.daiitech.naftah.utils.arabic.ArabicUtils;
import org.daiitech.naftah.utils.reflect.ClassUtils;

/**
 * Represents a Java class {@link Constructor} that can be invoked dynamically.
 *
 * <p>This class serves as a reflective wrapper around a {@code Constructor<?>},
 * storing metadata about its declaring class, parameter types, and whether
 * it is invocable. It also provides serialization support, automatically
 * restoring the transient {@code Constructor} instance upon deserialization
 * by matching parameter types.</p>
 *
 * <p>Arabic terminology:
 * <ul>
 * <li><strong>المنشئ</strong> — Constructor</li>
 * <li><strong>المُعدّلات</strong> — Modifiers</li>
 * <li><strong>المعاملات</strong> — Parameters</li>
 * </ul>
 * </p>
 *
 * @author Chakib Daii
 */
public final class JvmClassInitializer implements Serializable, JvmExecutable {

	@Serial
	private static final long serialVersionUID = 1L;


	/**
	 * The class declaring this constructor.
	 */
	private final Class<?> clazz;

	/**
	 * The fully qualified signature identifying this constructor.
	 */

	private final String qualifiedName;

	/**
	 * The parameter types accepted by this constructor.
	 */

	private final Class<?>[] constructorParameterTypes;
	/**
	 * Whether this constructor is invocable (accessible and valid).
	 */

	private final boolean isInvocable;

	/**
	 * The reflected constructor instance (transient for serialization).
	 */
	private transient Constructor<?> constructor;

	/**
	 * Creates a new {@code JvmClassInitializer} wrapper.
	 *
	 * @param qualifiedName the fully qualified constructor signature
	 * @param clazz         the class declaring the constructor
	 * @param constructor   the reflected constructor instance
	 * @param isInvocable   whether the constructor can be invoked
	 */
	public JvmClassInitializer(String qualifiedName, Class<?> clazz, Constructor<?> constructor, boolean isInvocable) {
		this.qualifiedName = qualifiedName;
		this.clazz = clazz;
		this.constructor = constructor;
		this.constructorParameterTypes = constructor.getParameterTypes();
		this.isInvocable = isInvocable;
	}


	/**
	 * Factory method for creating a {@code JvmClassInitializer} instance.
	 *
	 * @param qualifiedName the fully qualified constructor signature
	 * @param clazz         the declaring class
	 * @param constructor   the reflected constructor
	 * @return a new {@code JvmClassInitializer} instance
	 */
	public static JvmClassInitializer of(String qualifiedName, Class<?> clazz, Constructor<?> constructor) {
		return new JvmClassInitializer( qualifiedName,
										clazz,
										constructor,
										ClassUtils.isInvocable(constructor));
	}


	/**
	 * Gets the class declaring the method.
	 *
	 * @return the class declaring this constructor
	 */
	public Class<?> getClazz() {
		return clazz;
	}

	/**
	 * Gets the class qualified name.
	 *
	 * @return the fully qualified name of this constructor
	 */

	public String getQualifiedName() {
		return qualifiedName;
	}


	/**
	 * Gets the class constructor.
	 *
	 * @return the reflected {@link Constructor} instance
	 */
	public Constructor<?> getConstructor() {
		return constructor;
	}

	/**
	 * Checks if the method is invocable.
	 *
	 * @return {@code true} if this constructor can be invoked, otherwise {@code false}
	 */
	public boolean isInvocable() {
		return isInvocable;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Executable getExecutable() {
		return constructor;
	}

	/**
	 * Custom serialization logic for writing non-transient fields.
	 *
	 * @param oos the object output stream
	 * @throws IOException if an I/O error occurs
	 */
	@Serial
	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
	}

	/**
	 * Custom deserialization logic for restoring the transient {@link Constructor}.
	 * <p>After reading object data, this method searches the declaring class for
	 * a constructor with matching parameter types and restores it.</p>
	 *
	 * @param ois the object input stream
	 * @throws IOException            if an I/O error occurs
	 * @throws ClassNotFoundException if a class cannot be found during deserialization
	 */
	@Serial
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		try {
			ois.defaultReadObject();
			for (Constructor<?> c : clazz.getConstructors()) {
				if (Arrays.equals(c.getParameterTypes(), constructorParameterTypes)) {
					this.constructor = c;
					break;
				}
			}
		}
		catch (Throwable ignored) {
		}
	}


	/**
	 * Returns a concise Arabic-labeled string representation of this constructor.
	 * <p>Format: {@code <منشئ qualifiedName>}</p>
	 *
	 * @return a short string identifying this constructor
	 */
	@Override
	public String toString() {
		return "<%s %s>".formatted("منشئ", this.qualifiedName);
	}


	/**
	 * Returns a detailed, Arabic-formatted string representation of this constructor,
	 * including its class, parameters, modifiers, and annotations.
	 *
	 * <p>Example output:
	 * <pre>
	 * تفاصيل المنشئ:
	 * - الاسم المؤهل: com.example.MyClass - كوم:إِكْزامْبِل:ماي_كْلاس
	 * - المعاملات:
	 * - java.lang.String - جافا:لانغ:سترينج
	 * - المُعدّلات: public - بَبْلِكْ
	 * - التعليقات التوضيحية:
	 * - @java.lang.Deprecated - جافا:لانغ:دِبْرِكَيْتِدْ
	 * </pre>
	 *
	 * @return an Arabic, multi-line description of the constructor’s metadata
	 */
	public String toDetailedString() {
		StringBuilder detailedString = new StringBuilder();

		detailedString
				.append("""
							تفاصيل المنشئ:
							\t- الاسم المؤهل: %s - %s
						"""
						.formatted( clazz.getName(),
									ClassUtils.getQualifiedName(clazz.getName())));

		if (Objects.nonNull(constructor)) {
			Class<?>[] paramTypes = constructor.getParameterTypes();
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
							.append(ClassUtils.getQualifiedName(param.getName()))
							.append("\n");
				}
			}

			var modifiers = Modifier.toString(constructor.getModifiers());
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

			var annotations = constructor.getDeclaredAnnotations();
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
							.append(ClassUtils.getQualifiedName(annotation.annotationType().getName()))
							.append("\n");
				}
			}
		}

		return detailedString.toString();
	}
}
