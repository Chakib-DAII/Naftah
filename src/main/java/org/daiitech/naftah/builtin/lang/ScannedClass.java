package org.daiitech.naftah.builtin.lang;

import java.util.List;
import java.util.Optional;

import org.daiitech.naftah.utils.reflect.ClassUtils;

import static org.daiitech.naftah.utils.reflect.ClassUtils.getBuiltinMethods;
import static org.daiitech.naftah.utils.reflect.ClassUtils.getClassMethods;
import static org.daiitech.naftah.utils.reflect.ClassUtils.getQualifiedName;

/**
 * Represents metadata about a scanned Java class.
 * <p>
 * This record holds information about the class, including its accessibility,
 * instantiability, and lists of JVM and builtin functions associated with it.
 * </p>
 *
 * @param clazz            the @{@link Class} object representing the class
 * @param qualifiedName    the fully qualified name of the class
 * @param isAccessible     whether the class is accessible
 * @param isInstantiable   whether the class can be instantiated
 * @param jvmFunctions     list of JVM functions available on the class
 * @param builtinFunctions list of builtin functions available on the class
 * @author Chakib Daii
 * @deprecated This class is deprecated and may be removed in future versions.
 */
@Deprecated
public record ScannedClass(
		Class<?> clazz,
		String qualifiedName,
		boolean isAccessible,
		boolean isInstantiable,
		List<JvmFunction> jvmFunctions,
		List<BuiltinFunction> builtinFunctions
) {

	/**
	 * Creates a {@code ScannedClass} instance for the given class and qualified name.
	 *
	 * @param clazz         the class to scan
	 * @param qualifiedName the fully qualified name of the class
	 * @return a new {@code ScannedClass} instance
	 */
	public static ScannedClass of(Class<?> clazz, String qualifiedName) {
		return new ScannedClass(clazz,
								qualifiedName,
								ClassUtils.isAccessibleClass(clazz),
								ClassUtils.isInstantiableClass(clazz),
								getClassMethods(qualifiedName, clazz),
								getBuiltinMethods(clazz));
	}

	/**
	 * Creates a {@code ScannedClass} instance for the given class.
	 *
	 * @param clazz the class to scan
	 * @return a new {@code ScannedClass} instance
	 */
	public static ScannedClass of(Class<?> clazz) {
		return of(clazz, getQualifiedName(clazz.getName()));
	}

	/**
	 * Safely creates a {@code ScannedClass} instance, returning an empty optional if scanning fails.
	 *
	 * @param clazz the class to scan
	 * @return an {@code Optional} containing the {@code ScannedClass} if successful, or empty otherwise
	 */
	public static Optional<ScannedClass> safeOf(Class<?> clazz) {
		ScannedClass scannedClass = null;
		try {
			scannedClass = of(clazz, getQualifiedName(clazz.getName()));
		}
		catch (Throwable ignored) {
			// Silently skip classes that can't be loaded
		}
		return Optional.ofNullable(scannedClass);
	}
}
