package org.daiitech.naftah.builtin.lang;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Holds the results of a classpath or module scanning operation.
 * <p>
 * This class maintains collections of discovered classes, their qualifiers,
 * loaders, and associated functions for use within the runtime or interpreter.
 * </p>
 * <p>
 * The data includes:
 * <ul>
 * <li>Mapping of class names to their {@link ClassLoader}</li>
 * <li>Sets of class qualifiers, including Arabic-qualified names</li>
 * <li>Maps of qualified class names to {@link Class} objects</li>
 * <li>Filtered maps for accessible and instantiable classes</li>
 * <li>Collections of JVM and builtin functions grouped by qualified call names</li>
 * </ul>
 * </p>
 *
 * @author Chakib Daii
 */
public class ClassScanningResult implements Serializable {
	/**
	 * Maps class names to their respective ClassLoader instances.
	 */
	private Map<String, ClassLoader> classNames;

	/**
	 * Set of fully qualified class names discovered.
	 */
	private Set<String> classQualifiers;

	/**
	 * Set of fully qualified class names in Arabic notation.
	 */
	private Set<String> arabicClassQualifiers;

	/**
	 * Maps fully qualified class names to their corresponding Class objects.
	 */
	private Map<String, Class<?>> classes;

	/**
	 * Subset of classes that are accessible for reflection or usage.
	 */
	private Map<String, Class<?>> accessibleClasses;

	/**
	 * Subset of classes that can be instantiated (e.g., not abstract).
	 */
	private Map<String, Class<?>> instantiableClasses;

	/**
	 * Maps qualified method or function names to lists of JVM functions (methods).
	 */
	private Map<String, List<JvmFunction>> jvmFunctions;

	/**
	 * Maps qualified method or function names to lists of builtin functions.
	 */
	private Map<String, List<BuiltinFunction>> builtinFunctions;

	// Getters and setters

	public Map<String, ClassLoader> getClassNames() {
		return classNames;
	}

	public void setClassNames(Map<String, ClassLoader> classNames) {
		this.classNames = classNames;
	}

	public Set<String> getClassQualifiers() {
		return classQualifiers;
	}

	public void setClassQualifiers(Set<String> classQualifiers) {
		this.classQualifiers = classQualifiers;
	}

	public Set<String> getArabicClassQualifiers() {
		return arabicClassQualifiers;
	}

	public void setArabicClassQualifiers(Set<String> arabicClassQualifiers) {
		this.arabicClassQualifiers = arabicClassQualifiers;
	}

	public Map<String, Class<?>> getClasses() {
		return classes;
	}

	public void setClasses(Map<String, Class<?>> classes) {
		this.classes = classes;
	}

	public Map<String, Class<?>> getAccessibleClasses() {
		return accessibleClasses;
	}

	public void setAccessibleClasses(Map<String, Class<?>> accessibleClasses) {
		this.accessibleClasses = accessibleClasses;
	}

	public Map<String, Class<?>> getInstantiableClasses() {
		return instantiableClasses;
	}

	public void setInstantiableClasses(Map<String, Class<?>> instantiableClasses) {
		this.instantiableClasses = instantiableClasses;
	}

	public Map<String, List<JvmFunction>> getJvmFunctions() {
		return jvmFunctions;
	}

	public void setJvmFunctions(Map<String, List<JvmFunction>> jvmFunctions) {
		this.jvmFunctions = jvmFunctions;
	}

	public Map<String, List<BuiltinFunction>> getBuiltinFunctions() {
		return builtinFunctions;
	}

	public void setBuiltinFunctions(Map<String, List<BuiltinFunction>> builtinFunctions) {
		this.builtinFunctions = builtinFunctions;
	}
}
