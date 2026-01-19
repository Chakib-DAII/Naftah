// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

package org.daiitech.naftah.utils.reflect;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.daiitech.naftah.builtin.lang.BuiltinFunction;
import org.daiitech.naftah.builtin.lang.JvmClassInitializer;
import org.daiitech.naftah.builtin.lang.JvmFunction;

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

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * Maps class names to their respective ClassLoader instances.
	 */
	private transient Map<String, ClassLoader> classNames;

	/**
	 * Set of fully qualified class names discovered.
	 */
	private Set<String> classQualifiers;

	/**
	 * Set of fully qualified class names in Arabic notation mapped to original java qualified class name.
	 */
	private Map<String, String> arabicClassQualifiers;

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
	 * Maps qualified Java class names to lists of {@link JvmClassInitializer} constructors.
	 */
	private Map<String, List<JvmClassInitializer>> jvmClassInitializers;
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

	public Map<String, String> getArabicClassQualifiers() {
		return arabicClassQualifiers;
	}

	public void setArabicClassQualifiers(Map<String, String> arabicClassQualifiers) {
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

	public Map<String, List<JvmClassInitializer>> getJvmClassInitializers() {
		return jvmClassInitializers;
	}

	public void setJvmClassInitializers(Map<String, List<JvmClassInitializer>> jvmClassInitializers) {
		this.jvmClassInitializers = jvmClassInitializers;
	}

	public Map<String, List<BuiltinFunction>> getBuiltinFunctions() {
		return builtinFunctions;
	}

	public void setBuiltinFunctions(Map<String, List<BuiltinFunction>> builtinFunctions) {
		this.builtinFunctions = builtinFunctions;
	}

	/**
	 * Custom serialization logic for {@link ClassScanningResult}.
	 * <p>
	 * Since {@link URLClassLoader} and other {@link ClassLoader} instances are
	 * not serializable, this method converts each {@link URLClassLoader} in
	 * {@link #classNames} to a list of its URL strings and writes that map
	 * to the stream.
	 *
	 * @param out the ObjectOutputStream to write the object to
	 * @throws IOException if an I/O error occurs during serialization
	 */
	@Serial
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();

		Map<String, List<String>> urlsMap = new HashMap<>();
		if (classNames != null) {
			for (var entry : classNames.entrySet()) {
				ClassLoader cl = entry.getValue();
				if (cl != null) {
					List<String> urls = new ArrayList<>();
					if (cl instanceof URLClassLoader ucl) {
						for (URL url : ucl.getURLs()) {
							urls.add(url.toExternalForm());
						}
					}
					urlsMap.put(entry.getKey(), urls);
				}
				else {
					urlsMap.put(entry.getKey(), null);
				}
			}
		}
		out.writeObject(urlsMap);
	}

	/**
	 * Custom deserialization logic for {@link ClassScanningResult}.
	 * <p>
	 * Reads the map of class names to URL strings from the stream and reconstructs
	 * {@link URLClassLoader} instances for the {@link #classNames} map.
	 *
	 * @param in the ObjectInputStream to read the object from
	 * @throws IOException            if an I/O error occurs during deserialization
	 * @throws ClassNotFoundException if a class required during deserialization cannot be found
	 */
	@Serial
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();

		//noinspection unchecked
		Map<String, List<String>> urlsMap = (Map<String, List<String>>) in.readObject();
		classNames = new HashMap<>();
		for (var entry : urlsMap.entrySet()) {
			List<String> urls = entry.getValue();
			if (urls != null) {
				URL[] urlArray = urls.stream().map(u -> {
					try {
						return new URL(u);
					}
					catch (Exception e) {
						throw new RuntimeException(e);
					}
				}).toArray(URL[]::new);
				classNames.put(entry.getKey(), new URLClassLoader(urlArray));
			}
			else {
				classNames.put(entry.getKey(), null);
			}
		}
	}
}
