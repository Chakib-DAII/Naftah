// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

package org.daiitech.naftah.playground.utils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

import javax.json.*;

import org.daiitech.naftah.builtin.lang.*;
import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.playground.NaftahPlayground;
import org.daiitech.naftah.utils.reflect.ClassScanningResult;
import org.daiitech.naftah.utils.reflect.RuntimeClassScanner;

import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;

/**
 * Utility responsible for reconstructing a {@link ClassScanningResult}
 * from a JSON snapshot.
 *
 * <p>This loader is primarily used by the browser-based Naftah Playground,
 * where runtime metadata is generated ahead of time and distributed as a
 * serialized JSON document instead of performing a full classpath scan
 * at startup.</p>
 *
 * <p>The loader supports both plain JSON and GZIP-compressed JSON files.
 * During deserialization it restores runtime metadata such as:</p>
 *
 * <ul>
 * <li>Class loader mappings</li>
 * <li>Class qualifiers</li>
 * <li>Accessible and instantiable classes</li>
 * <li>JVM functions and constructors</li>
 * <li>Built-in function descriptors</li>
 * </ul>
 *
 * <p>Class references are resolved lazily using
 * {@link RuntimeClassScanner#loadClass(String)} to avoid triggering
 * class initialization during reconstruction.</p>
 *
 * @author Chakib Daii
 */
public class ClassScanningResultLoader {

	/**
	 * Thread-safe cache of resolved {@link Class} objects indexed by fully qualified class name.
	 *
	 * <p>This cache is used to avoid repeated and expensive class-loading operations for the
	 * same class name during runtime resolution/deserialization.</p>
	 *
	 * <p>Entries are stored indefinitely for the lifetime of the JVM.</p>
	 */
	private static final Map<String, Class<?>> CLASS_CACHE = new ConcurrentHashMap<>(4096);

	/**
	 * Private constructor to prevent instantiation.
	 * Throws {@link NaftahBugError} if called.
	 */
	private ClassScanningResultLoader() {
		throw newNaftahBugInvalidUsageError();
	}

	/**
	 * Reads and deserializes a {@link ClassScanningResult} from the specified file.
	 *
	 * <p>The input may be either a plain JSON document or a GZIP-compressed
	 * JSON document. Compression is detected automatically by inspecting
	 * the file signature.</p>
	 *
	 * @param path the path to the serialized snapshot
	 * @return the reconstructed {@link ClassScanningResult}
	 * @throws Exception if the file cannot be read or the JSON cannot be
	 *                   deserialized successfully
	 */
	public static ClassScanningResult fromJson(Path path, boolean async) throws Exception {
		try (   InputStream in = Files.newInputStream(path);
				PushbackInputStream pb = new PushbackInputStream(in, 2)) {

			byte[] sig = new byte[2];
			int n = pb.read(sig);

			if (n > 0) {
				pb.unread(sig, 0, n);
			}

			boolean gzip = n == 2 && (sig[0] & 0xFF) == 0x1F && (sig[1] & 0xFF) == 0x8B;

			InputStream finalStream = gzip ? new GZIPInputStream(pb) : pb;

			NaftahPlayground.LOGGER.debug("fromJson - parsing JSON stream");

			try (   Reader reader = new InputStreamReader(finalStream);
					JsonReader jsonReader = Json.createReader(reader)) {

				var result = async ? fromJsonAsync(jsonReader.readObject()) : fromJson(jsonReader.readObject());

				NaftahPlayground.LOGGER.debug("fromJson - class scanning result parsed");

				return result;
			}
		}
	}

	/**
	 * Reconstructs a {@link ClassScanningResult} from its JSON representation.
	 *
	 * <p>The supplied JSON object is expected to conform to the structure
	 * produced by the runtime class scanning export process.</p>
	 *
	 * @param obj the serialized class scanning result
	 * @return the reconstructed {@link ClassScanningResult}
	 */
	public static ClassScanningResult fromJson(JsonObject obj) {
		NaftahPlayground.LOGGER.trace("fromJson - obj : " + obj);

		ClassScanningResult result = new ClassScanningResult();

		// ClassLoaders
		result.setClassNames(toClassLoaders(obj));

		// Simple metadata
		result.setClassQualifiers(toSet(obj.getJsonArray("classQualifiers")));
		result.setArabicClassQualifiers(toMap(obj.getJsonObject("arabicClassQualifiers")));

		// Classes
		result.setClasses(toClassMap(obj.getJsonObject("classes")));
		result.setAccessibleClasses(toClassMap(obj.getJsonObject("accessibleClasses")));
		result.setInstantiableClasses(toClassMap(obj.getJsonObject("instantiableClasses")));

		// JVM Functions (Methods)
		result.setJvmFunctions(toJvmFunctions(obj.getJsonObject("jvmFunctions")));

		// JVM Class Initializers (Constructors)
		result.setJvmClassInitializers(toJvmClassInitializers(obj.getJsonObject("jvmClassInitializers")));

		// Builtin Functions
		result.setBuiltinFunctions(toBuiltinFunctions(obj.getJsonObject("builtinFunctions")));

		return result;
	}

	public static ClassScanningResult fromJsonAsync(JsonObject obj) {
		NaftahPlayground.LOGGER.trace("fromJsonAsync - obj loaded");

		ExecutorService executor = Executors
				.newFixedThreadPool(
									Math.max(2, Runtime.getRuntime().availableProcessors()));

		try {
			// ClassLoaders
			CompletableFuture<Map<String, ClassLoader>> classLoadersFuture = CompletableFuture
					.supplyAsync(
									() -> toClassLoaders(obj),
									executor);

			// Simple metadata
			CompletableFuture<Set<String>> qualifiersFuture = CompletableFuture
					.supplyAsync(
									() -> toSet(obj.getJsonArray("classQualifiers")),
									executor);

			CompletableFuture<Map<String, String>> arabicFuture = CompletableFuture
					.supplyAsync(
									() -> toMap(obj.getJsonObject("arabicClassQualifiers")),
									executor);

			// Classes
			CompletableFuture<Map<String, Class<?>>> classesFuture = CompletableFuture
					.supplyAsync(
									() -> toClassMap(obj.getJsonObject("classes")),
									executor);

			CompletableFuture<Map<String, Class<?>>> accessibleFuture = CompletableFuture
					.supplyAsync(
									() -> toClassMap(obj.getJsonObject("accessibleClasses")),
									executor);

			CompletableFuture<Map<String, Class<?>>> instantiableFuture = CompletableFuture
					.supplyAsync(
									() -> toClassMap(obj.getJsonObject("instantiableClasses")),
									executor);

			// JVM Functions (Methods)
			CompletableFuture<Map<String, List<JvmFunction>>> jvmFunctionsFuture = CompletableFuture
					.supplyAsync(
									() -> toJvmFunctions(obj.getJsonObject("jvmFunctions")),
									executor);

			// JVM Class Initializers (Constructors)
			CompletableFuture<Map<String, List<JvmClassInitializer>>> initializersFuture = CompletableFuture
					.supplyAsync(
									() -> toJvmClassInitializers(
																	obj.getJsonObject("jvmClassInitializers")),
									executor);

			// Builtin Functions
			CompletableFuture<Map<String, List<BuiltinFunction>>> builtinFuture = CompletableFuture
					.supplyAsync(
									() -> toBuiltinFunctions(
																obj.getJsonObject("builtinFunctions")),
									executor);

			CompletableFuture
					.allOf(
							classLoadersFuture,
							qualifiersFuture,
							arabicFuture,
							classesFuture,
							accessibleFuture,
							instantiableFuture,
							jvmFunctionsFuture,
							initializersFuture,
							builtinFuture
					)
					.join();

			ClassScanningResult result = new ClassScanningResult();

			result.setClassNames(classLoadersFuture.join());

			result.setClassQualifiers(qualifiersFuture.join());
			result.setArabicClassQualifiers(arabicFuture.join());

			result.setClasses(classesFuture.join());
			result.setAccessibleClasses(accessibleFuture.join());
			result.setInstantiableClasses(instantiableFuture.join());

			result.setJvmFunctions(jvmFunctionsFuture.join());
			result.setJvmClassInitializers(initializersFuture.join());
			result.setBuiltinFunctions(builtinFuture.join());

			return result;
		}
		finally {
			executor.shutdown();
		}
	}

	/**
	 * Reconstructs the exported class loader mapping.
	 *
	 * <p>Each entry associates a class name with the URLs that were used
	 * to load it when the snapshot was generated.</p>
	 *
	 * @param obj the root JSON object
	 * @return a map of class names to reconstructed class loaders,
	 *         or {@code null} if no class loader information is present
	 */
	private static Map<String, ClassLoader> toClassLoaders(JsonObject obj) {
		if (obj == null || !obj.containsKey("classNames")) return null;

		JsonObject classNamesObj = obj.getJsonObject("classNames");
		NaftahPlayground.LOGGER.trace("toClassLoaders - classNames : " + classNamesObj);

		int size = classNamesObj.size();
		Map<String, ClassLoader> result = size > 16 ? new HashMap<>(size) : new HashMap<>();


		for (String key : classNamesObj.keySet()) {
			NaftahPlayground.LOGGER.trace("toClassLoaders - key : " + key);

			if (classNamesObj.isNull(key)) {
				result.put(key, null);
				continue;
			}

			JsonArray urlsArray = classNamesObj.getJsonArray(key);
			NaftahPlayground.LOGGER.trace("toClassLoaders - urlsArray : " + urlsArray);

			size = urlsArray.size();
			URL[] urls = new URL[size];
			int count = 0;

			for (int i = 0; i < size; i++) {
				try {
					urls[count++] = new URL(urlsArray.getString(i));
				}
				catch (Throwable th) {
					NaftahPlayground.LOGGER.trace("toClassLoaders error : " + th.getMessage());
				}
			}

			NaftahPlayground.LOGGER.trace("toClassLoaders - urls : " + Arrays.toString(urls));
			result.put(key, new URLClassLoader(count == size ? urls : Arrays.copyOf(urls, count)));
		}

		return result;
	}

	/**
	 * Converts a JSON object containing class names into a map of resolved
	 * {@link Class} instances.
	 *
	 * <p>Classes are loaded without initialization using
	 * {@link ClassScanningResultLoader#safeClass(String)}.</p>
	 *
	 * @param obj the JSON representation
	 * @return a map of aliases to resolved classes, or {@code null}
	 */
	private static Map<String, Class<?>> toClassMap(JsonObject obj) {
		if (obj == null) return null;
		NaftahPlayground.LOGGER.trace("toClassMap : " + obj);

		int size = obj.size();
		Map<String, Class<?>> map = size > 16 ? new HashMap<>(size) : new HashMap<>();

		for (String k : obj.keySet()) {
			try {
				var value = safeClass(obj.getString(k));
				NaftahPlayground.LOGGER.trace("toClassMap - key : " + k + " - value: " + value);
				map.put(k, value);
			}
			catch (Throwable th) {
				NaftahPlayground.LOGGER.trace("toClassMap error : " + th.getMessage());
			}
		}
		return map;
	}

	/**
	 * Converts a JSON array of strings into a {@link Set}.
	 *
	 * @param arr the JSON array
	 * @return a set containing all string values, or {@code null} if the array is {@code null}
	 */
	private static Set<String> toSet(JsonArray arr) {
		if (arr == null) return null;
		NaftahPlayground.LOGGER.trace("toSet : " + arr);

		int size = arr.size();
		Set<String> set = size > 16 ? new HashSet<>(size) : new HashSet<>();

		for (int i = 0; i < arr.size(); i++) {
			var value = arr.getString(i);
			NaftahPlayground.LOGGER.trace("toSet - value: " + value);
			set.add(value);
		}
		return set;
	}

	/**
	 * Converts a JSON object containing string values into a {@link Map}.
	 *
	 * @param obj the JSON object
	 * @return a map containing all key-value pairs, or {@code null} if the object is {@code null}
	 */
	private static Map<String, String> toMap(JsonObject obj) {
		if (obj == null) return null;
		NaftahPlayground.LOGGER.trace("toMap : " + obj);

		int size = obj.size();
		Map<String, String> map = size > 16 ? new HashMap<>(size) : new HashMap<>();

		for (String k : obj.keySet()) {
			var value = obj.getString(k);
			NaftahPlayground.LOGGER.trace("toMap - key : " + k + " - value: " + value);
			map.put(k, value);
		}
		return map;
	}

	/**
	 * Reconstructs JVM function metadata from its serialized form.
	 *
	 * <p>Each function is restored by resolving its declaring class,
	 * parameter types, and backing {@link Method} instance.</p>
	 *
	 * @param obj the serialized JVM functions
	 * @return the reconstructed JVM function registry
	 */
	private static Map<String, List<JvmFunction>> toJvmFunctions(JsonObject obj) {
		if (obj == null) return null;
		NaftahPlayground.LOGGER.trace("toJvmFunctions : " + obj);

		int size = obj.size();
		Map<String, List<JvmFunction>> result = size > 16 ? new HashMap<>(size) : new HashMap<>();

		for (String key : obj.keySet()) {
			NaftahPlayground.LOGGER.trace("toJvmFunctions - key : " + key);
			JsonArray arr = obj.getJsonArray(key);

			size = arr.size();
			List<JvmFunction> list = size > 10 ? new ArrayList<>(size) : new ArrayList<>();

			for (int i = 0; i < arr.size(); i++) {
				JsonObject o = arr.getJsonObject(i);

				try {
					Class<?> clazz = safeClass(o.getString("className"));
					Class<?>[] params = toClassArray(o.getJsonArray("methodParameterTypes"));

					Method method = findMethod(clazz, o.getString("methodName"), params);
					if (method == null) continue;

					list
							.add(new JvmFunction(
													o.getString("qualifiedCall"),
													clazz,
													method,
													o.getBoolean("isStatic", false),
													o.getBoolean("isInvocable", true)
							));

				}
				catch (Throwable th) {
					NaftahPlayground.LOGGER.trace("toJvmFunctions error : " + th.getMessage());
				}
			}

			result.put(key, list);
		}

		return result;
	}

	/**
	 * Reconstructs JVM constructor metadata from its serialized form.
	 *
	 * <p>Each initializer is restored by resolving its declaring class,
	 * parameter types, and backing {@link Constructor} instance.</p>
	 *
	 * @param obj the serialized JVM class initializers
	 * @return the reconstructed JVM class initializer registry
	 */
	private static Map<String, List<JvmClassInitializer>> toJvmClassInitializers(JsonObject obj) {
		if (obj == null) return null;
		NaftahPlayground.LOGGER.trace("toJvmClassInitializers : " + obj);

		int size = obj.size();
		Map<String, List<JvmClassInitializer>> result = size > 16 ? new HashMap<>(size) : new HashMap<>();

		for (String key : obj.keySet()) {
			NaftahPlayground.LOGGER.trace("toJvmClassInitializers - key : " + key);
			JsonArray arr = obj.getJsonArray(key);

			size = arr.size();
			List<JvmClassInitializer> list = size > 10 ? new ArrayList<>(size) : new ArrayList<>();

			for (int i = 0; i < arr.size(); i++) {
				JsonObject o = arr.getJsonObject(i);

				try {
					Class<?> clazz = safeClass(o.getString("className"));
					Class<?>[] params = toClassArray(o.getJsonArray("constructorParameterTypes"));

					Constructor<?> ctor = findConstructor(clazz, params);
					if (ctor == null) continue;

					list
							.add(new JvmClassInitializer(
															o.getString("qualifiedName"),
															clazz,
															ctor,
															o.getBoolean("isInvocable", true)
							));

				}
				catch (Throwable th) {
					NaftahPlayground.LOGGER.trace("toJvmClassInitializers error : " + th.getMessage());
				}
			}

			result.put(key, list);
		}

		return result;
	}

	/**
	 * Reconstructs built-in function metadata from its serialized form.
	 *
	 * <p>Each entry is converted into a {@link BuiltinFunction} by restoring
	 * its backing method, provider information, and function descriptor.</p>
	 *
	 * @param obj the serialized built-in functions
	 * @return the reconstructed built-in function registry
	 */
	private static Map<String, List<BuiltinFunction>> toBuiltinFunctions(JsonObject obj) {
		if (obj == null) return null;
		NaftahPlayground.LOGGER.trace("toBuiltinFunctions : " + obj);

		int size = obj.size();
		Map<String, List<BuiltinFunction>> result = size > 16 ? new HashMap<>(size) : new HashMap<>();

		for (String key : obj.keySet()) {
			NaftahPlayground.LOGGER.trace("toBuiltinFunctions - key : " + key);
			JsonArray arr = obj.getJsonArray(key);

			size = arr.size();
			List<BuiltinFunction> list = size > 10 ? new ArrayList<>(size) : new ArrayList<>();

			for (int i = 0; i < arr.size(); i++) {
				JsonObject o = arr.getJsonObject(i);

				try {
					Class<?> clazz = safeClass(o.getString("className"));
					Class<?>[] params = toClassArray(o.getJsonArray("methodParameterTypes"));

					Method method = findMethod(clazz, o.getString("methodName"), params);
					if (method == null) continue;

					list
							.add(new BuiltinFunction(
														method,
														deserializeProvider(o.getJsonObject("providerInfo")),
														deserializeFunction(o.getJsonObject("functionInfo"))
							));

				}
				catch (Throwable th) {
					NaftahPlayground.LOGGER.trace("toBuiltinFunctions error : " + th.getMessage());
				}
			}

			result.put(key, list);
		}

		return result;
	}

	/**
	 * Reconstructs a {@link NaftahFunctionProvider} descriptor from JSON.
	 *
	 * @param o the serialized provider descriptor
	 * @return the reconstructed provider descriptor, or {@code null}
	 */
	private static NaftahFunctionProvider deserializeProvider(JsonObject o) {
		if (o == null) return null;
		NaftahPlayground.LOGGER.trace("deserializeProvider : " + o);

		var naftahFunctionProvider = NaftahFunctionProvider
				.of(
					o.getString("name"),
					o.getBoolean("useQualifiedName", false),
					o.getBoolean("useQualifiedAliases", false),
					o.getString("description", ""),
					toStringArray(o.getJsonArray("functionNames"))
				);

		NaftahPlayground.LOGGER.trace("deserializeProvider - naftahFunctionProvider: " + naftahFunctionProvider);

		return naftahFunctionProvider;
	}

	/**
	 * Reconstructs a {@link NaftahFunction} descriptor from JSON.
	 *
	 * @param o the serialized function descriptor
	 * @return the reconstructed function descriptor, or {@code null}
	 */
	private static NaftahFunction deserializeFunction(JsonObject o) {
		if (o == null) return null;
		NaftahPlayground.LOGGER.trace("deserializeFunction : " + o);

		var naftahFunction = NaftahFunction
				.of(
					o.getString("name"),
					o.getBoolean("useQualifiedName", false),
					o.getBoolean("useQualifiedAliases", false),
					toStringArray(o.getJsonArray("aliases")),
					o.getString("description", ""),
					o.getString("usage", ""),
					safeClass(o.getString("returnType")),
					toClassArray(o.getJsonArray("parameterTypes")),
					toClassArray(o.getJsonArray("exceptionTypes"))
				);

		NaftahPlayground.LOGGER.trace("deserializeFunction - naftahFunction: " + naftahFunction);

		return naftahFunction;
	}

	/**
	 * Converts a {@link JsonArray} containing string values into a {@code String[]}.
	 *
	 * <p>If the provided array is {@code null}, an empty array is returned.</p>
	 *
	 * @param arr the JSON array to convert, or {@code null}
	 * @return a new array containing all string values from the JSON array,
	 *         or an empty array if {@code arr} is {@code null}
	 */
	private static String[] toStringArray(JsonArray arr) {
		if (arr == null) return new String[0];

		String[] out = new String[arr.size()];
		for (int i = 0; i < arr.size(); i++) {
			out[i] = arr.getString(i);
		}
		return out;
	}

	/**
	 * Converts a JSON array of class names into an array of resolved classes.
	 *
	 * <p>Unresolvable classes are replaced with {@link Object} via
	 * {@link #safeClass(String)}.</p>
	 *
	 * @param arr the JSON array of fully qualified class names
	 * @return the resolved class array, never {@code null}
	 */
	private static Class<?>[] toClassArray(JsonArray arr) {
		if (arr == null) return new Class<?>[0];
		NaftahPlayground.LOGGER.trace("toClassArray :" + arr);

		Class<?>[] out = new Class<?>[arr.size()];
		for (int i = 0; i < arr.size(); i++) {
			out[i] = safeClass(arr.getString(i));
		}
		return out;
	}

	/**
	 * Attempts to locate a declared method.
	 *
	 * @param clazz  the declaring class
	 * @param name   the method name
	 * @param params the parameter types
	 * @return the matching method, or {@code null} if it cannot be found
	 */
	private static Method findMethod(Class<?> clazz, String name, Class<?>[] params) {
		try {
			NaftahPlayground.LOGGER
					.trace("findMethod for class :" + clazz + ", name: " + name + ", params: " + Arrays
							.toString(
										params));
			return clazz.getDeclaredMethod(name, params);
		}
		catch (Throwable th) {
			NaftahPlayground.LOGGER.trace("findMethod error : " + th.getMessage());
			return null;
		}
	}

	/**
	 * Attempts to locate a declared constructor.
	 *
	 * @param clazz  the declaring class
	 * @param params the constructor parameter types
	 * @return the matching constructor, or {@code null} if it cannot be found
	 */
	private static Constructor<?> findConstructor(Class<?> clazz, Class<?>[] params) {
		try {
			NaftahPlayground.LOGGER
					.trace("findConstructor for class :" + clazz + ", params: " + Arrays
							.toString(
										params));
			return clazz.getDeclaredConstructor(params);
		}
		catch (Throwable th) {
			NaftahPlayground.LOGGER.trace("findConstructor error : " + th.getMessage());
			return null;
		}
	}

	/**
	 * Resolves a class by its fully qualified name using a shared cache.
	 *
	 * <p>If the class cannot be loaded for any reason, {@link Object} is returned
	 * as a safe fallback to allow processing to continue.</p>
	 *
	 * <p>Results are cached in a thread-safe map to avoid repeated class loading
	 * attempts for the same name.</p>
	 *
	 * @param name the fully qualified binary class name
	 * @return the resolved {@link Class}, or {@link Object} if resolution fails
	 */
	private static Class<?> safeClass(String name) {
		return CLASS_CACHE.computeIfAbsent(name, n -> {
			try {
				NaftahPlayground.LOGGER.trace("safeClass for class :" + name);
				return RuntimeClassScanner.loadClass(name);
			}
			catch (Throwable th) {
				NaftahPlayground.LOGGER.trace("safeClass error : " + th.getMessage());
				return Object.class;
			}
		});
	}
}
