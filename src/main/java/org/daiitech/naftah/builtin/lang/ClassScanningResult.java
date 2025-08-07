package org.daiitech.naftah.builtin.lang;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Chakib Daii
 */
public class ClassScanningResult implements Serializable {
	private Map<String, ClassLoader> classNames;
	private Set<String> classQualifiers;
	private Set<String> arabicClassQualifiers;
	// qualifiedName -> CLass<?>
	private Map<String, Class<?>> classes;
	private Map<String, Class<?>> accessibleClasses;
	private Map<String, Class<?>> instantiableClasses;
	// qualifiedCall -> Method
	private Map<String, List<JvmFunction>> jvmFunctions;
	private Map<String, List<BuiltinFunction>> builtinFunctions;

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
