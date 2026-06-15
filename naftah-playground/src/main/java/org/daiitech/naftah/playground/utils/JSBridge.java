package org.daiitech.naftah.playground.utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.daiitech.naftah.playground.NaftahPlayground;

import netscape.javascript.JSObject;

import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;

/**
 * Utility bridge between Java and JavaScript (JSObject).
 * <p>
 * Provides helper methods to convert JavaScript arrays/objects into
 * Java {@link Map}, {@link List}, and arrays.
 * </p>
 *
 * @author Chakib Daii
 */
public final class JSBridge {

	/**
	 * Private constructor to prevent instantiation.
	 * Throws {@link org.daiitech.naftah.errors.NaftahBugError} if called.
	 */
	private JSBridge() {
		throw newNaftahBugInvalidUsageError();
	}

	/**
	 * Converts a JavaScript array of key-value entries into a Java {@link Map}.
	 *
	 * @param jsEntries        JavaScript object representing an array of entries
	 * @param entryValueMapper function used to convert JS value into type R
	 * @param <R>              value type of resulting map
	 * @return a {@link LinkedHashMap} preserving JS iteration order
	 */
	public static <R> Map<String, R> toMap(JSObject jsEntries, Function<Object, R> entryValueMapper) {
		NaftahPlayground.LOGGER.trace("toMap - jsEntries : " + jsEntries);

		Map<String, R> map = new LinkedHashMap<>();

		if (jsEntries == null) {
			return map;
		}

		int size = getLength(jsEntries);
		NaftahPlayground.LOGGER.trace("toMap - size : " + size);

		for (int i = 0; i < size; i++) {
			JSObject entry = (JSObject) getEntryAtIndex(jsEntries, i);
			NaftahPlayground.LOGGER.trace("toMap - entry : " + entry);

			if (entry == null) {
				continue;
			}

			String key = String.valueOf(entry.getMember("key"));
			R value = entryValueMapper.apply(entry.getMember("value"));

			map.put(key, value);
		}

		return map;
	}

	/**
	 * Converts a JavaScript array into a Java {@link List}.
	 *
	 * @param jsArray          JavaScript array object
	 * @param arrayValueMapper function to convert JS values into type R
	 * @param <R>              element type
	 * @return a {@link ArrayList} containing converted values
	 */
	public static <R> List<R> toList(JSObject jsArray, Function<Object, R> arrayValueMapper) {
		NaftahPlayground.LOGGER.trace("toList - jsArray : " + jsArray);

		List<R> list = new ArrayList<>();

		if (jsArray == null) {
			return list;
		}

		int size = getLength(jsArray);
		NaftahPlayground.LOGGER.trace("toList - size : " + size);

		for (int i = 0; i < size; i++) {
			list.add(arrayValueMapper.apply(getAtIndex(jsArray, i)));
		}

		return list;
	}

	/**
	 * Converts a JavaScript array into a Java array.
	 *
	 * @param jsArray          JavaScript array object
	 * @param type             component type of resulting array
	 * @param arrayValueMapper function to convert JS values into type R
	 * @param <R>              element type
	 * @return a Java array of type R
	 */
	public static <R> R[] toArray(
									JSObject jsArray,
									Class<R> type,
									Function<Object, R> arrayValueMapper
	) {
		NaftahPlayground.LOGGER.trace("toArray - jsArray : " + jsArray);

		if (jsArray == null) {
			//noinspection unchecked
			return (R[]) Array.newInstance(type, 0);
		}

		int size = getLength(jsArray);
		NaftahPlayground.LOGGER.trace("toArray - size : " + size);

		//noinspection unchecked
		R[] result = (R[]) Array.newInstance(type, size);

		for (int i = 0; i < size; i++) {
			result[i] = arrayValueMapper.apply(getAtIndex(jsArray, i));
		}

		return result;
	}

	/**
	 * Retrieves the length of a JavaScript array-like object.
	 *
	 * @param obj JavaScript object
	 * @return length as integer, or 0 if unavailable
	 */
	public static int getLength(JSObject obj) {
		NaftahPlayground.LOGGER.trace("getLength - obj : " + obj);

		if (obj == null) {
			return 0;
		}

		Object length = callJSMethod("jsLength", obj);

		NaftahPlayground.LOGGER.trace("getLength - length : " + length + ", length class: " + length.getClass());

		try {
			return ((Number) length).intValue();
		}
		catch (Exception ignored) {
			try {
				return Integer.parseInt(String.valueOf(length));
			}
			catch (Exception ignored2) {
				return 0;
			}
		}
	}

	/**
	 * Retrieves an element at a given index from a JavaScript array.
	 *
	 * @param obj   JavaScript array
	 * @param index index to access
	 * @return value at index, or null if unavailable
	 */
	public static Object getAtIndex(JSObject obj, int index) {
		return getAtIndex(obj, "jsGetAtIndex", index);
	}

	/**
	 * Retrieves a JS object entry at a given index.
	 *
	 * @param obj   JavaScript array of entries
	 * @param index index to access
	 * @return entry object, or null if unavailable
	 */
	public static Object getEntryAtIndex(JSObject obj, int index) {
		return getAtIndex(obj, "jsGetEntryAtIndex", index);
	}

	/**
	 * Internal helper to access a JavaScript array-like structure using a JS method.
	 *
	 * @param obj        JavaScript object
	 * @param methodName JavaScript method name to invoke
	 * @param index      index parameter
	 * @return result of JS call, or null if unavailable
	 */
	public static Object getAtIndex(JSObject obj, String methodName, int index) {
		NaftahPlayground.LOGGER.trace("getAtIndex - obj : " + obj + ", index : " + index);

		if (obj == null) {
			return null;
		}

		Object value = callJSMethod(methodName, obj, index);

		NaftahPlayground.LOGGER
				.trace("getAtIndex - value : " + value + ", class : " + (value != null ? value.getClass() : "null"));

		return value;
	}

	/**
	 * Calls a JavaScript method on the given object.
	 *
	 * @param methodName JS method name
	 * @param objs       arguments (first argument must be JSObject target)
	 * @return result of JS call
	 */
	private static Object callJSMethod(String methodName, Object... objs) {
		JSObject window = getWindow((JSObject) objs[0]);
		return window.call(methodName, objs);
	}

	/**
	 * Retrieves the JavaScript global window object.
	 *
	 * @param anyJsObject any JSObject instance
	 * @return globalThis JS object
	 */
	private static JSObject getWindow(JSObject anyJsObject) {
		// In most JSObject bridges, globalThis is accessible via eval
		return (JSObject) anyJsObject.eval("globalThis");
	}
}
