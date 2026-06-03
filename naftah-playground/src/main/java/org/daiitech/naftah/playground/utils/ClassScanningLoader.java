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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.daiitech.naftah.builtin.lang.BuiltinFunction;
import org.daiitech.naftah.builtin.lang.BuiltinFunctionInfo;
import org.daiitech.naftah.builtin.lang.JvmClassInitializer;
import org.daiitech.naftah.builtin.lang.JvmFunction;
import org.daiitech.naftah.builtin.lang.NaftahFunction;
import org.daiitech.naftah.builtin.lang.NaftahFunctionProvider;
import org.daiitech.naftah.playground.NaftahPlayground;
import org.daiitech.naftah.utils.reflect.ClassScanningIndex;
import org.daiitech.naftah.utils.reflect.ClassScanningResult;
import org.daiitech.naftah.utils.reflect.ClassUtils;

import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;

/**
 * Utility responsible for reconstructing {@link ClassScanningResult} and {@link ClassScanningIndex}
 * instances from a serialized JSON snapshot.
 *
 * <p>This loader is primarily used by the Naftah Playground environment, where runtime classpath
 * scanning results are precomputed and shipped as JSON instead of being generated at runtime.</p>
 *
 * <p>The loader supports:</p>
 * <ul>
 * <li>Plain JSON and GZIP-compressed JSON inputs</li>
 * <li>Sync and async reconstruction modes</li>
 * <li>Reflection-based restoration of classes, methods, and constructors</li>
 * </ul>
 *
 * <p>Reconstructed metadata includes:</p>
 * <ul>
 * <li>Class loader mappings</li>
 * <li>Class registries (accessible / instantiable classes)</li>
 * <li>JVM methods and constructors</li>
 * <li>Built-in function descriptors and metadata</li>
 * </ul>
 *
 * <p><b>Failure strategy:</b> Any unresolved class, method, or constructor is skipped or
 * replaced with safe defaults to ensure partial recovery instead of full failure.</p>
 *
 * <p><b>Threading note:</b> Async methods create a temporary thread pool per invocation.</p>
 *
 * @author Chakib Daii
 */
public final class ClassScanningLoader {

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
	 * Throws {@link org.daiitech.naftah.errors.NaftahBugError} if called.
	 */
	private ClassScanningLoader() {
		throw newNaftahBugInvalidUsageError();
	}

	/**
	 * Loads and reconstructs a {@link ClassScanningResult} from a JSON snapshot file.
	 *
	 * <p>The input file may be either:</p>
	 * <ul>
	 * <li>Plain JSON</li>
	 * <li>GZIP-compressed JSON (auto-detected via header)</li>
	 * </ul>
	 *
	 * <p>When {@code async = true}, reconstruction of major sections (classes, functions,
	 * metadata) is performed in parallel using a temporary executor service.</p>
	 *
	 * @param path  the snapshot file path
	 * @param async whether to reconstruct the result using parallel execution
	 * @return the reconstructed {@link ClassScanningResult}
	 * @throws Exception if the file cannot be read or parsed
	 */
	public static ClassScanningResult loadClassScanningResultFromJson(Path path, boolean async) throws Exception {
		return loadFromJson(path,
							(jsonObject) -> async ?
									loadClassScanningResultFromJsonAsync(jsonObject) :
									loadClassScanningResultFromJson(jsonObject));
	}

	/**
	 * Reconstructs a {@link ClassScanningResult} from its JSON representation.
	 *
	 * <p>The JSON must match the structure produced by the Naftah class scanning export system.</p>
	 *
	 * <p>This method performs synchronous reconstruction of:</p>
	 * <ul>
	 * <li>Class loader mappings</li>
	 * <li>Class registries</li>
	 * <li>JVM functions and constructors</li>
	 * <li>Built-in functions</li>
	 * </ul>
	 *
	 * @param obj serialized class scanning snapshot
	 * @return reconstructed {@link ClassScanningResult}
	 */
	public static ClassScanningResult loadClassScanningResultFromJson(JsonObject obj) {
		NaftahPlayground.LOGGER.trace("fromJson - obj : " + obj);

		ClassScanningResult result = new ClassScanningResult();

		// ClassLoaders
		result.setClassNames(toClassLoaders(obj));

		// Simple metadata
		result
				.setClassQualifiers(obj.containsKey("classQualifiers") ?
						toSet(obj.getJsonArray("classQualifiers")) :
						null);
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

	/**
	 * Reconstructs a {@link ClassScanningResult} from JSON using parallel execution.
	 *
	 * <p>Each major section of the snapshot is loaded concurrently using a fixed thread pool sized
	 * to the available CPU cores.</p>
	 *
	 * <p>The executor is created per invocation and shut down after completion.</p>
	 *
	 * <p>This method is functionally equivalent to the synchronous loader but optimized for large
	 * snapshots and browser-playground workloads.</p>
	 *
	 * @param obj serialized class scanning snapshot
	 * @return reconstructed {@link ClassScanningResult}
	 */
	public static ClassScanningResult loadClassScanningResultFromJsonAsync(JsonObject obj) {
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
									() -> obj.containsKey("classQualifiers") ?
											toSet(obj.getJsonArray("classQualifiers")) :
											null,
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
	 * Loads a lightweight {@link ClassScanningIndex} from a JSON snapshot file.
	 *
	 * <p>The index version contains only minimal metadata required for fast lookup:</p>
	 * <ul>
	 * <li>Class names</li>
	 * <li>Class qualifiers</li>
	 * <li>Localized (Arabic) qualifiers</li>
	 * <li>Built-in function metadata</li>
	 * </ul>
	 *
	 * <p>This is a reduced form of {@link ClassScanningResult} intended for fast startup
	 * and lightweight runtime usage.</p>
	 *
	 * @param path  snapshot file path
	 * @param async whether to use parallel reconstruction
	 * @return reconstructed {@link ClassScanningIndex}
	 * @throws Exception if loading or parsing fails
	 */
	public static ClassScanningIndex loadClassScanningIndexFromJson(Path path, boolean async) throws Exception {
		return loadFromJson(path,
							(jsonObject) -> async ?
									loadClassScanningIndexFromJsonAsync(jsonObject) :
									loadClassScanningIndexFromJson(jsonObject));
	}

	/**
	 * Reconstructs a {@link ClassScanningIndex} from JSON.
	 *
	 * <p>This method extracts only lightweight metadata and does not resolve full class
	 * or method structures.</p>
	 *
	 * @param obj serialized index snapshot
	 * @return reconstructed index
	 */
	public static ClassScanningIndex loadClassScanningIndexFromJson(JsonObject obj) {
		NaftahPlayground.LOGGER.trace("fromJson - obj : " + obj);

		// Simple metadata
		var classNames = obj.containsKey("classNames") ? toSet(obj.getJsonArray("classNames")) : null;
		var classQualifiers = obj.containsKey("classQualifiers") ? toSet(obj.getJsonArray("classQualifiers")) : null;
		var arabicClassQualifiers = toMap(obj.getJsonObject("arabicClassQualifiers"));

		// Builtin Functions
		var builtinFunctions = toBuiltinFunctionArray(obj.getJsonArray("builtinFunctions"));

		return new ClassScanningIndex(classNames, classQualifiers, arabicClassQualifiers, builtinFunctions);
	}

	/**
	 * Asynchronously reconstructs a {@link ClassScanningIndex}.
	 *
	 * <p>Uses a temporary thread pool to parallelize parsing of metadata sections.</p>
	 *
	 * @param obj serialized index snapshot
	 * @return reconstructed index
	 */
	public static ClassScanningIndex loadClassScanningIndexFromJsonAsync(JsonObject obj) {
		NaftahPlayground.LOGGER.trace("fromJsonAsync - obj loaded");

		ExecutorService executor = Executors
				.newFixedThreadPool(
									Math.max(2, Runtime.getRuntime().availableProcessors()));

		try {
			// Simple metadata
			CompletableFuture<Set<String>> classNamesFuture = CompletableFuture
					.supplyAsync(
									() -> obj.containsKey("classNames") ? toSet(obj.getJsonArray("classNames")) : null,
									executor);

			CompletableFuture<Set<String>> qualifiersFuture = CompletableFuture
					.supplyAsync(
									() -> obj.containsKey("classQualifiers") ?
											toSet(obj.getJsonArray("classQualifiers")) :
											null,
									executor);

			CompletableFuture<Map<String, String>> arabicFuture = CompletableFuture
					.supplyAsync(
									() -> toMap(obj.getJsonObject("arabicClassQualifiers")),
									executor);


			// Builtin Functions
			CompletableFuture<BuiltinFunctionInfo[]> builtinFuture = CompletableFuture
					.supplyAsync(
									() -> toBuiltinFunctionArray(obj.getJsonArray("builtinFunctions")),
									executor);

			CompletableFuture
					.allOf(
							classNamesFuture,
							qualifiersFuture,
							arabicFuture,
							builtinFuture
					)
					.join();

			return new ClassScanningIndex(  classNamesFuture.join(),
											qualifiersFuture.join(),
											arabicFuture.join(),
											builtinFuture.join());

		}
		finally {
			executor.shutdown();
		}
	}

	/**
	 * Generic JSON snapshot loader that handles file IO, compression detection, and parsing.
	 *
	 * <p>This method:</p>
	 * <ol>
	 * <li>Detects GZIP compression via magic bytes</li>
	 * <li>Parses JSON using {@link javax.json.JsonReader}</li>
	 * <li>Delegates conversion to the provided loader function</li>
	 * </ol>
	 *
	 * <p>Performance diagnostics are logged at TRACE level.</p>
	 *
	 * @param path   input snapshot file
	 * @param loader function that converts parsed JSON into a target object
	 * @param <R>    return type
	 * @return reconstructed object
	 * @throws Exception on IO or parsing failure
	 */
	public static <R> R loadFromJson(Path path, Function<JsonObject, R> loader) throws Exception {
		long t0 = System.nanoTime();

		try (   InputStream in = Files.newInputStream(path);
				PushbackInputStream pb = new PushbackInputStream(in, 2)) {

			byte[] sig = new byte[2];
			int n = pb.read(sig);

			if (n > 0) {
				pb.unread(sig, 0, n);
			}

			boolean gzip = n == 2 && (sig[0] & 0xFF) == 0x1F && (sig[1] & 0xFF) == 0x8B;

			InputStream finalStream = gzip ? new GZIPInputStream(pb) : pb;

			long t1 = System.nanoTime();

			NaftahPlayground.LOGGER.debug("loadFromJson - parsing JSON stream");

			try (   Reader reader = new InputStreamReader(finalStream);
					JsonReader jsonReader = Json.createReader(reader)) {

				JsonObject jsonObject = jsonReader.readObject();

				NaftahPlayground.LOGGER.debug("loadFromJson - json object ready");

				long t2 = System.nanoTime();

				var result = loader.apply(jsonObject);

				long t3 = System.nanoTime();

				NaftahPlayground.LOGGER.debug("loadFromJson - class scanning parsed");


				NaftahPlayground.LOGGER.trace("JSON loading (in ms): " + (t1 - t0) / 1_000_000);
				NaftahPlayground.LOGGER.trace("JSON-P parse (in ms): " + (t2 - t1) / 1_000_000);
				NaftahPlayground.LOGGER.trace("Conversion (in ms):" + (t3 - t1) / 1_000_000);
				NaftahPlayground.LOGGER.trace("TOTAL (in ms):" + (t3 - t0) / 1_000_000);
				return result;
			}
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
		if (obj == null || !obj.containsKey("classNames")) {
			return null;
		}

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
	 * {@link ClassScanningLoader#safeClass(String)}.</p>
	 *
	 * @param obj the JSON representation
	 * @return a map of aliases to resolved classes, or {@code null}
	 */
	private static Map<String, Class<?>> toClassMap(JsonObject obj) {
		if (obj == null) {
			return null;
		}

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
		if (arr == null) {
			return null;
		}

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
		if (obj == null) {
			return null;
		}

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
		if (obj == null) {
			return null;
		}

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

					if (method == null) {
						continue;
					}

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
		if (obj == null) {
			return null;
		}

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
					if (ctor == null) {
						continue;
					}

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
		if (obj == null) {
			return null;
		}

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
					if (method == null) {
						continue;
					}

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
	 * Converts a JSON array of serialized built-in function metadata into an array of
	 * {@link BuiltinFunctionInfo} objects.
	 *
	 * <p>Each JSON element is expected to contain the following structure:</p>
	 * <ul>
	 * <li>{@code methodName} - the JVM method name</li>
	 * <li>{@code className} - declaring class name</li>
	 * <li>{@code methodParameterTypes} - array of parameter type names</li>
	 * <li>{@code canonicalKey} - canonical function identifier</li>
	 * <li>{@code qualifiedAliases} - array of fully qualified alias names</li>
	 * </ul>
	 *
	 * <p>The returned array preserves the original JSON order. Each index corresponds
	 * directly to the input array index.</p>
	 *
	 * <p><b>Failure behavior:</b></p>
	 * <ul>
	 * <li>If the input array is {@code null}, an empty array is returned.</li>
	 * <li>If an individual element fails to parse, it is skipped and the corresponding
	 * array slot remains {@code null}.</li>
	 * <li>Parsing errors do not interrupt the overall conversion process.</li>
	 * </ul>
	 *
	 * <p>This method is used as part of the lightweight {@link ClassScanningIndex}
	 * reconstruction pipeline where only metadata (not executable reflection objects)
	 * is required.</p>
	 *
	 * @param arr JSON array of built-in function metadata
	 * @return an array of {@link BuiltinFunctionInfo}, possibly containing {@code null} entries
	 */
	private static BuiltinFunctionInfo[] toBuiltinFunctionArray(JsonArray arr) {
		if (arr == null) {
			return new BuiltinFunctionInfo[0];
		}

		NaftahPlayground.LOGGER.trace("toBuiltinFunctionArray :" + arr);

		BuiltinFunctionInfo[] out = new BuiltinFunctionInfo[arr.size()];
		for (int i = 0; i < arr.size(); i++) {

			JsonObject o = arr.getJsonObject(i);

			try {
				String methodName = o.getString("methodName");
				String className = o.getString("className");
				String[] methodParameterTypes = toStringArray(o.getJsonArray("methodParameterTypes"));
				String canonicalKey = o.getString("canonicalKey");
				String[] qualifiedAliases = toStringArray(o.getJsonArray("qualifiedAliases"));


				out[i] = new BuiltinFunctionInfo(
													methodName,
													className,
													methodParameterTypes,
													canonicalKey,
													qualifiedAliases
				);

			}
			catch (Throwable th) {
				NaftahPlayground.LOGGER.trace("toBuiltinFunctions error : " + th.getMessage());
			}
		}
		return out;
	}

	/**
	 * Reconstructs a {@link NaftahFunctionProvider} descriptor from JSON.
	 *
	 * @param o the serialized provider descriptor
	 * @return the reconstructed provider descriptor, or {@code null}
	 */
	private static NaftahFunctionProvider deserializeProvider(JsonObject o) {
		if (o == null) {
			return null;
		}

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
		if (o == null) {
			return null;
		}

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
		if (arr == null) {
			return new String[0];
		}

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
		if (arr == null) {
			return new Class<?>[0];
		}

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
	 * Resolves a class by name using {@link ClassUtils}, with caching and safe fallback.
	 *
	 * <p>If resolution fails, {@link Object} is returned instead of throwing an exception,
	 * ensuring partial reconstruction of the snapshot.</p>
	 *
	 * <p>Results are cached in {@link #CLASS_CACHE} for the lifetime of the JVM.</p>
	 *
	 * @param name fully qualified class name
	 * @return resolved class or {@link Object} if resolution fails
	 */
	private static Class<?> safeClass(String name) {
		return CLASS_CACHE.computeIfAbsent(name, n -> {
			try {
				NaftahPlayground.LOGGER.trace("safeClass for class :" + name);
				return ClassUtils.resolveType(name);
			}
			catch (Throwable th) {
				NaftahPlayground.LOGGER.trace("safeClass error : " + th.getMessage());
				return Object.class;
			}
		});
	}
}
