// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

package org.daiitech.naftah.builtin.lang;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.daiitech.naftah.builtin.utils.CollectionUtils;
import org.daiitech.naftah.builtin.utils.ObjectUtils;
import org.daiitech.naftah.utils.script.ScriptUtils;

import static org.daiitech.naftah.Naftah.JAVA_OBJECT_REFLECT_ACTIVE_PROPERTY;
import static org.daiitech.naftah.Naftah.JAVA_OBJECT_REFLECT_MAX_DEPTH_PROPERTY;
import static org.daiitech.naftah.Naftah.JAVA_OBJECT_REFLECT_SKIP_NULLS_PROPERTY;
import static org.daiitech.naftah.builtin.utils.CollectionUtils.createCollection;
import static org.daiitech.naftah.builtin.utils.CollectionUtils.createMap;
import static org.daiitech.naftah.builtin.utils.CollectionUtils.isCollectionMapOrArray;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.isBuiltinType;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.isSimpleType;

/**
 * Represents a dynamic "Naftah" object — a flexible wrapper that can encapsulate
 * either a native Java object or a declarative structure of named fields.
 *
 * <p>This record provides utilities to:</p>
 * <ul>
 * <li>Wrap Java objects and expose their structure as nested maps</li>
 * <li>Recursively serialize Java objects, records, arrays, and collections into a {@link Map}</li>
 * <li>Transliterate field or key names into Arabic script for user-facing representation</li>
 * <li>Distinguish between Java-backed and declaratively defined Naftah objects</li>
 * </ul>
 *
 * <p>When {@code fromJava} is {@code true}, the instance wraps a native Java object.
 * Otherwise, it wraps a map of {@link DeclaredVariable}s describing a Naftah object structure.</p>
 *
 * @param fromJava     whether this object originates from a Java instance
 * @param javaObject   the wrapped Java object if {@code fromJava} is {@code true}; otherwise {@code null}
 * @param type         the Java class type of the wrapped object or declared structure
 * @param objectFields the field definitions if this object is a declarative Naftah object; otherwise {@code null}
 * @author Chakib Daii
 */
public record NaftahObject(
		boolean fromJava,
		Object javaObject,
		Class<?> type,
		Map<String, DeclaredVariable> objectFields
) {

	/**
	 * Maximum depth allowed for Java object reflection mapping.
	 */
	public static final int MAX_DEPTH;
	/**
	 * Whether null fields should be skipped during reflection mapping.
	 */
	public static final boolean SKIP_NULLS;
	/**
	 * Format pattern used for transliterating field or key names into Arabic script.
	 * The resulting format will be {@code "<arabic> (<latin>)"}.
	 */
	public static final String KEY_OR_FIELD_TRANSLITERATION_FORMAT = "%s (%s)";
	/**
	 * Format pattern for field processing errors in Arabic: "[خطأ أثناء معالجة الحقل: %s]".
	 */
	public static final String FIELD_ERROR_FORMAT = "[خطأ أثناء معالجة الحقل: %s]";

	static {
		if (Boolean.getBoolean(JAVA_OBJECT_REFLECT_ACTIVE_PROPERTY)) {
			MAX_DEPTH = Math.max(Integer.getInteger(JAVA_OBJECT_REFLECT_MAX_DEPTH_PROPERTY, 0), 0);
			SKIP_NULLS = Boolean.getBoolean(JAVA_OBJECT_REFLECT_SKIP_NULLS_PROPERTY);
		}
		else {
			MAX_DEPTH = 0;
			SKIP_NULLS = false;
		}
	}

	/**
	 * Canonical constructor with validation logic.
	 *
	 * <p>Ensures that required fields are non-null based on {@code fromJava}:</p>
	 * <ul>
	 * <li>If {@code fromJava} is {@code true}, {@code javaObject} must not be {@code null}.</li>
	 * <li>If {@code fromJava} is {@code false}, {@code objectFields} must not be {@code null}.</li>
	 * </ul>
	 *
	 * @throws NullPointerException if {@code type} is {@code null}, or if required fields are missing
	 */
	public NaftahObject {
		Objects.requireNonNull(type);
		if (fromJava) {
			Objects.requireNonNull(javaObject);
			if (javaObject instanceof NaftahObject naftahObject) {
				javaObject = Objects.requireNonNull(naftahObject.javaObject);
			}
		}
		else {
			Objects.requireNonNull(objectFields);
		}
	}

	/**
	 * Formats a given key or field name into a bilingual representation using Arabic transliteration.
	 *
	 * @param keyOrFieldName the Latin field or key name
	 * @return a formatted string combining Arabic transliteration and Latin script
	 */
	public static String formatKeyOrFieldName(String keyOrFieldName) {
		return KEY_OR_FIELD_TRANSLITERATION_FORMAT
				.formatted(
							ScriptUtils
									.transliterateToArabicScriptDefault(keyOrFieldName)[0],
							keyOrFieldName
				);
	}

	/**
	 * Creates a {@code NaftahObject} instance backed by declarative field definitions.
	 *
	 * @param objectFields a map of field names to declared variables
	 * @return a new {@code NaftahObject} representing a declarative object
	 * @throws NullPointerException if {@code objectFields} is {@code null}
	 */
	public static NaftahObject of(Map<String, DeclaredVariable> objectFields) {
		return new NaftahObject(false, null, objectFields.getClass(), objectFields);
	}

	/**
	 * Creates a {@code NaftahObject} instance backed by a Java object.
	 *
	 * @param javaObject the Java object to wrap
	 * @return a new {@code NaftahObject} representing a Java object
	 * @throws NullPointerException if {@code javaObject} is {@code null}
	 */
	public static NaftahObject of(Object javaObject) {
		return new NaftahObject(true, javaObject, javaObject.getClass(), null);
	}

	/**
	 * Converts an arbitrary Java object into a structured {@link Map} representation.
	 * Equivalent to calling {@link #toMap(Object, boolean)} with {@code skipNulls = false}.
	 *
	 * @param obj the object to convert
	 * @return a map representation of the object, or {@code null} if the input is {@code null}
	 */
	public static Map<String, Object> toMap(Object obj) {
		return toMap(obj, new IdentityHashMap<>(), SKIP_NULLS, 0);
	}

	/**
	 * Converts an arbitrary Java object into a structured {@link Map} representation,
	 * optionally skipping {@code null} values.
	 *
	 * @param obj       the object to convert
	 * @param skipNulls whether to exclude {@code null} values from the resulting map
	 * @return a map representation of the object, or {@code null} if the input is {@code null}
	 */
	public static Map<String, Object> toMap(Object obj, boolean skipNulls) {
		return toMap(obj, new IdentityHashMap<>(), skipNulls, 0);
	}

	/**
	 * Recursively resolves a Java object into a nested map structure, tracking visited instances
	 * to prevent infinite recursion from circular references.
	 *
	 * <p>This method supports:</p>
	 * <ul>
	 * <li>Records (via {@link java.lang.reflect.RecordComponent})</li>
	 * <li>Plain old Java objects (POJOs)</li>
	 * <li>Collections, maps, and arrays</li>
	 * </ul>
	 *
	 * <p>If a circular reference is detected, a special placeholder entry is added:
	 * {@code "[circular-reference]" → <simple-class-name>}.</p>
	 */
	private static Map<String, Object> toMap(   Object obj,
												IdentityHashMap<Object, Boolean> visited,
												boolean skipNulls,
												int depth) {
		if (obj == null) {
			return null;
		}

		if (visited.containsKey(obj)) {
			return Map.of("[(circular-reference)مرجع دائري]", obj.getClass().getSimpleName());
		}
		visited.put(obj, true);

		// If depth exceeded, shallow print
		if (depth > MAX_DEPTH) {
			return Map.of("(value) القيمة", obj.toString());
		}

		Class<?> clazz = obj.getClass();

		Map<String, Object> result = new LinkedHashMap<>();

		// Handle records
		if (clazz.isRecord()) {
			for (RecordComponent rc : clazz.getRecordComponents()) {
				try {
					Object value = rc.getAccessor().invoke(obj);
					if (!skipNulls || value != null) {
						result.put(formatKeyOrFieldName(rc.getName()), convertValue(value, visited, skipNulls, depth));
					}
				}
				catch (Exception e) {
					result.put(formatKeyOrFieldName(rc.getName()), FIELD_ERROR_FORMAT.formatted(e.getMessage()));
				}
			}
			return result;
		}

		// Handle POJOs
		while (clazz != null && clazz != Object.class) {
			for (Field field : clazz.getDeclaredFields()) {
				if (Modifier.isStatic(field.getModifiers())) {
					continue;
				}
				try {
					if (!field.canAccess(obj)) {
						try {
							field.setAccessible(true);
						}
						catch (InaccessibleObjectException e) {
							result
									.put(   formatKeyOrFieldName(field.getName()),
											FIELD_ERROR_FORMAT.formatted(e.getMessage()));
							continue;
						}
					}
					Object value = field.get(obj);
					if (!skipNulls || value != null) {
						result
								.put(   formatKeyOrFieldName(field.getName()),
										convertValue(value, visited, skipNulls, depth));
					}
				}
				catch (Exception e) {
					result.put(formatKeyOrFieldName(field.getName()), FIELD_ERROR_FORMAT.formatted(e.getMessage()));
				}
			}
			clazz = clazz.getSuperclass();
		}

		return result;
	}

	/**
	 * Converts an arbitrary value (including nested collections, maps, or arrays)
	 * into a serializable representation suitable for mapping.
	 *
	 * @param value     the value to convert
	 * @param visited   objects already visited during recursion to avoid cycles
	 * @param skipNulls whether to skip {@code null} entries
	 * @return a converted value or map/list structure
	 */
	private static Object convertValue(
										Object value,
										IdentityHashMap<Object, Boolean> visited,
										boolean skipNulls,
										int depth) {
		if (value == null || value instanceof String || value instanceof Number || value instanceof Boolean || value instanceof Character) {
			return value;
		}

		if (depth > MAX_DEPTH) {
			return value.toString();
		}

		Class<?> clazz = value.getClass();

		// Maps
		if (value instanceof Map<?, ?> map) {
			Map<Object, Object> out = new LinkedHashMap<>();
			for (var entry : map.entrySet()) {
				Object k = entry.getKey();
				Object v = entry.getValue();
				if (!skipNulls || v != null) {
					out
							.put(
									convertValue(k, visited, skipNulls, depth + 1),
									convertValue(v, visited, skipNulls, depth + 1)
							);
				}
			}
			return out;
		}

		// Collections
		if (value instanceof Collection<?> collection) {
			List<Object> out = new ArrayList<>();
			for (var item : collection) {
				if (!skipNulls || item != null) {
					out.add(convertValue(item, visited, skipNulls, depth + 1));
				}
			}
			return out;
		}

		// Arrays
		if (clazz.isArray()) {
			int length = Array.getLength(value);
			List<Object> out = new ArrayList<>(length);
			for (int i = 0; i < length; i++) {
				Object element = Array.get(value, i);
				if (!skipNulls || element != null) {
					out.add(convertValue(element, visited, skipNulls, depth + 1));
				}
			}
			return out;
		}

		// Recursively map object
		return toMap(value, visited, skipNulls, depth + 1);
	}

	/**
	 * Returns a converted form of the given object suitable for Naftah representation.
	 *
	 * <p>Performs recursive conversion for collections, maps, arrays, and custom objects.
	 * Leaves simple or built-in types unchanged.</p>
	 *
	 * @param javaObject the object to process
	 * @return a converted object representation
	 */
	public static Object get(Object javaObject, boolean original) {
		if (javaObject instanceof Collection<?> collection) {
			return collection
					.stream()
					.map(object -> NaftahObject.get(object, original))
					.collect(Collectors.toCollection(() -> createCollection(collection.getClass())));
		}
		else if (javaObject != null && javaObject.getClass().isArray()) {
			Class<?> componentType = javaObject.getClass().getComponentType();

			// Only handle Object[] and String[]
			if (!componentType.isPrimitive() && (Object.class
					.isAssignableFrom(componentType) || componentType == String.class)) {
				int length = Array.getLength(javaObject);
				Object resultArray = Array.newInstance(componentType, length);

				for (int i = 0; i < length; i++) {
					Object element = Array.get(javaObject, i);
					Array.set(resultArray, i, NaftahObject.get(element, original));
				}

				return resultArray;
			}
			else {
				return javaObject;
			}
		}
		else if (javaObject instanceof Map<?, ?> map) {
			return map
					.entrySet()
					.stream()
					.collect(Collectors
							.toMap(
									e -> NaftahObject.get(e.getKey(), original),
									e -> NaftahObject.get(e.getValue(), original),
									(a, b) -> b,
									() -> createMap(map.getClass())
							));
		}
		else if (None.isNone(javaObject) || isSimpleType(javaObject) || isBuiltinType(javaObject)) {
			return javaObject;
		}
		else {
			return original ? javaObject : toMap(javaObject);
		}
	}

	/**
	 * Returns the evaluated value represented by this {@code NaftahObject}.
	 *
	 * <ul>
	 * <li>If wrapping a Java object, recursively resolves it into a map, collection, or scalar value.</li>
	 * <li>If wrapping declared fields, returns the underlying field map directly.</li>
	 * </ul>
	 *
	 * @return the evaluated representation of this object
	 */
	public Object get(boolean original) {
		if (fromJava) {
			return get(javaObject, original);
		}
		else {
			return objectFields;
		}
	}

	/**
	 * Returns the evaluated representation of this object.
	 *
	 * <p>This is equivalent to calling {@link #get(boolean)} with
	 * {@code original = false}.</p>
	 *
	 * @return the evaluated representation of this object
	 */
	public Object get() {
		return get(false);
	}


	/**
	 * Compares this {@code NaftahObject} to another object for equality.
	 *
	 * <p>Two {@code NaftahObject} instances are considered equal if and only if:
	 * <ul>
	 * <li>They are of the same runtime class, and</li>
	 * <li>Their {@code fromJava} flags are identical, and</li>
	 * <li>Their {@code javaObject}, {@code type}, and {@code objectFields} properties are equal
	 * according to {@link java.util.Objects#equals(Object, Object)}.</li>
	 * </ul>
	 * </p>
	 *
	 * <p>This ensures that equality reflects both the source (Java vs. declarative)
	 * and the internal data or type structure of the wrapped object.</p>
	 *
	 * @param o the object to compare with this instance
	 * @return {@code true} if the specified object is equal to this one; otherwise {@code false}
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		NaftahObject that = (NaftahObject) o;
		return fromJava == that.fromJava && Objects
				.equals(javaObject,
						that.javaObject) && Objects
								.equals(type,
										that.type) && Objects
												.equals(
														objectFields,
														that.objectFields);
	}

	/**
	 * Computes the hash code for this {@code NaftahObject}.
	 *
	 * <p>The hash code is derived from the {@code fromJava} flag,
	 * the {@code javaObject} reference, the {@code type} of the object,
	 * and the {@code objectFields} map. This ensures that two
	 * {@code NaftahObject} instances considered equal will always
	 * produce the same hash code, satisfying the general contract
	 * of {@link Object#hashCode()}.</p>
	 *
	 * @return a hash code value for this object
	 */
	@Override
	public int hashCode() {
		return Objects.hash(fromJava, javaObject, type, objectFields);
	}

	/**
	 * Returns a string representation of this Naftah object.
	 *
	 * <p>The format depends on whether it wraps a Java object or declared fields:</p>
	 * <ul>
	 * <li>For collections, maps, and arrays → formatted collection output</li>
	 * <li>For simple or built-in values → localized string representation</li>
	 * <li>For complex Java objects → Arabic-labeled key-value pairs</li>
	 * <li>For declarative Naftah objects → field map string representation</li>
	 * </ul>
	 *
	 * @return a human-readable Arabic/Latin mixed representation of the object
	 */
	@Override
	@SuppressWarnings("NullableProblems")
	public String toString() {
		if (fromJava) {
			if (isCollectionMapOrArray(javaObject)) {
				return CollectionUtils.toString(get());
			}
			if (None.isNone(javaObject) || isSimpleType(javaObject) || isBuiltinType(javaObject)) {
				return ObjectUtils.getNaftahValueToString(javaObject);
			}
			else {
				return "كائن: " + CollectionUtils.toString((Map<?, ?>) get(), '{', '}');
			}
		}
		else {
			return CollectionUtils.toString(objectFields, true);
		}
	}
}
