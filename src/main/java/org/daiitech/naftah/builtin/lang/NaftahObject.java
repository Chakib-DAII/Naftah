package org.daiitech.naftah.builtin.lang;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
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
import org.daiitech.naftah.utils.arabic.ArabicUtils;

import static org.daiitech.naftah.builtin.utils.CollectionUtils.createCollection;
import static org.daiitech.naftah.builtin.utils.CollectionUtils.createMap;
import static org.daiitech.naftah.builtin.utils.CollectionUtils.isCollectionMapOrArray;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.isBuiltinType;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.isSimpleType;

// TODO: use for object access and so on
// TODO: use later for dates. for now dte time are treated as java object which will change with dates support

/**
 * Represents a dynamic "Naftah" object — a flexible wrapper that can encapsulate
 * either a native Java object or a declarative structure of named fields.
 *
 * <p>This record provides utilities to:</p>
 * <ul>
 *   <li>Wrap Java objects and expose their structure as nested maps</li>
 *   <li>Recursively serialize Java objects, records, arrays, and collections into a {@link Map}</li>
 *   <li>Transliterate field or key names into Arabic script for user-facing representation</li>
 *   <li>Distinguish between Java-backed and declaratively defined Naftah objects</li>
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
	 * Format pattern used for transliterating field or key names into Arabic script.
	 * The resulting format will be {@code "<arabic> (<latin>)"}.
	 */
	public static final String KEY_OR_FIELD_TRANSLITERATION_FORMAT = "%s (%s)";
	public static final String FIELD_ERROR_FORMAT = "[خطأ أثناء معالجة الحقل: %s]";

	/**
	 * Canonical constructor with validation logic.
	 *
	 * <p>Ensures that required fields are non-null based on {@code fromJava}:</p>
	 * <ul>
	 *   <li>If {@code fromJava} is {@code true}, {@code javaObject} must not be {@code null}.</li>
	 *   <li>If {@code fromJava} is {@code false}, {@code objectFields} must not be {@code null}.</li>
	 * </ul>
	 *
	 * @throws NullPointerException if {@code type} is {@code null}, or if required fields are missing
	 */
	public NaftahObject {
		Objects.requireNonNull(type);
		if (fromJava) {
			Objects.requireNonNull(javaObject);
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
						ArabicUtils
								.transliterateToArabicScriptDefault(false, keyOrFieldName)[0],
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
		return toMap(obj, new IdentityHashMap<>(), false);
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
		return toMap(obj, new IdentityHashMap<>(), skipNulls);
	}

	/**
	 * Recursively resolves a Java object into a nested map structure, tracking visited instances
	 * to prevent infinite recursion from circular references.
	 *
	 * <p>This method supports:</p>
	 * <ul>
	 *   <li>Records (via {@link java.lang.reflect.RecordComponent})</li>
	 *   <li>Plain old Java objects (POJOs)</li>
	 *   <li>Collections, maps, and arrays</li>
	 * </ul>
	 *
	 * <p>If a circular reference is detected, a special placeholder entry is added:
	 * {@code "[circular-reference]" → <simple-class-name>}.</p>
	 */
	private static Map<String, Object> toMap(Object obj,
											 IdentityHashMap<Object, Boolean> visited,
											 boolean skipNulls) {
		if (obj == null) {
			return null;
		}

		if (visited.containsKey(obj)) {
			return Map.of("[(circular-reference)مرجع دائري]", obj.getClass().getSimpleName());
		}
		visited.put(obj, true);

		Map<String, Object> result = new LinkedHashMap<>();

		Class<?> clazz = obj.getClass();

		// Handle records
		if (clazz.isRecord()) {
			for (RecordComponent rc : clazz.getRecordComponents()) {
				try {
					Object value = rc.getAccessor().invoke(obj);
					if (!skipNulls || value != null) {
						result.put(formatKeyOrFieldName(rc.getName()), convertValue(value, visited, skipNulls));
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
				field.setAccessible(true);
				try {
					Object value = field.get(obj);
					if (!skipNulls || value != null) {
						result.put(formatKeyOrFieldName(field.getName()), convertValue(value, visited, skipNulls));
					}
				}
				catch (IllegalAccessException e) {
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
	private static Object convertValue(Object value, IdentityHashMap<Object, Boolean> visited, boolean skipNulls) {
		if (value == null || value instanceof String || value instanceof Number || value instanceof Boolean || value instanceof Character) {
			return value;
		}

		if (value instanceof Map<?, ?> map) {
			Map<Object, Object> nested = new LinkedHashMap<>();
			for (var entry : map.entrySet()) {
				Object key = entry.getKey();
				Object val = entry.getValue();
				if (!skipNulls || val != null) {
					nested.put(convertValue(key, visited, skipNulls), convertValue(val, visited, skipNulls));
				}
			}
			return nested;
		}

		if (value instanceof Collection<?> collection) {
			List<Object> list = new ArrayList<>();
			for (var item : collection) {
				if (!skipNulls || item != null) {
					list.add(convertValue(item, visited, skipNulls));
				}
			}
			return list;
		}

		if (value.getClass().isArray()) {
			int length = Array.getLength(value);
			List<Object> list = new ArrayList<>();
			for (int i = 0; i < length; i++) {
				Object element = Array.get(value, i);
				if (!skipNulls || element != null) {
					list.add(convertValue(element, visited, skipNulls));
				}
			}
			return list;
		}

		// Recursively map object
		return toMap(value, visited, skipNulls);
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
	public static Object get(Object javaObject) {
		if (javaObject instanceof Collection<?> collection) {
			return collection
					.stream()
					.map(NaftahObject::get)
					.collect(Collectors.toCollection(() -> createCollection(collection.getClass())));
		}
		else if (javaObject instanceof Map<?, ?> map) {
			return map
					.entrySet()
					.stream()
					.collect(Collectors
									 .toMap(
											 e -> NaftahObject.get(e.getKey()),
											 e -> NaftahObject.get(e.getValue()),
											 (a, b) -> b,
											 () -> createMap(map.getClass())
									 ));
		}
		else if (None.isNone(javaObject) || isSimpleType(javaObject) || isBuiltinType(javaObject)) {
			return javaObject;
		}
		else {
			return toMap(javaObject);
		}
	}

	/**
	 * Returns the evaluated value represented by this {@code NaftahObject}.
	 *
	 * <ul>
	 *   <li>If wrapping a Java object, recursively resolves it into a map, collection, or scalar value.</li>
	 *   <li>If wrapping declared fields, returns the underlying field map directly.</li>
	 * </ul>
	 *
	 * @return the evaluated representation of this object
	 */
	public Object get() {
		if (fromJava) {
			return get(javaObject);
		}
		else {
			return objectFields;
		}
	}

	/**
	 * Returns a string representation of this Naftah object.
	 *
	 * <p>The format depends on whether it wraps a Java object or declared fields:</p>
	 * <ul>
	 *   <li>For collections, maps, and arrays → formatted collection output</li>
	 *   <li>For simple or built-in values → localized string representation</li>
	 *   <li>For complex Java objects → Arabic-labeled key-value pairs</li>
	 *   <li>For declarative Naftah objects → field map string representation</li>
	 * </ul>
	 *
	 * @return a human-readable Arabic/Latin mixed representation of the object
	 */
	@Override
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
			return CollectionUtils.toString(objectFields);
		}
	}
}
