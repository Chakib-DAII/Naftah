// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

package org.daiitech.naftah.playground;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.antlr.v4.runtime.CharStream;
import org.daiitech.naftah.Naftah;
import org.daiitech.naftah.builtin.lang.None;
import org.daiitech.naftah.errors.ExceptionUtils;
import org.daiitech.naftah.parser.DefaultContext;
import org.daiitech.naftah.parser.NaftahErrorListener;
import org.daiitech.naftah.playground.utils.ClassScanningLoader;
import org.daiitech.naftah.playground.utils.logging.LogManager;
import org.daiitech.naftah.playground.utils.logging.Logger;
import org.daiitech.naftah.utils.reflect.ClassScanningIndex;
import org.daiitech.naftah.utils.reflect.ClassScanningResult;

import static org.daiitech.naftah.Naftah.CACHE_PATH_PROPERTY;
import static org.daiitech.naftah.Naftah.INDEX_CACHE_PATH_PROPERTY;
import static org.daiitech.naftah.Naftah.INSIDE_RUN_PROPERTY;
import static org.daiitech.naftah.Naftah.MINIMAL_CACHE_PATH_PROPERTY;
import static org.daiitech.naftah.Naftah.MINIMAL_INDEX_CACHE_PATH_PROPERTY;
import static org.daiitech.naftah.Naftah.SCAN_CLASSPATH_PROPERTY;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.getNaftahValueToString;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.isSimpleOrBuiltinOrCollectionOrMapOfSimpleType;
import static org.daiitech.naftah.parser.DefaultContext.bootstrapPlayground;
import static org.daiitech.naftah.parser.DefaultContext.cleanClassThreadLocals;
import static org.daiitech.naftah.parser.NaftahParserHelper.doRun;
import static org.daiitech.naftah.parser.NaftahParserHelper.getCharStream;
import static org.daiitech.naftah.parser.NaftahParserHelper.isDeclaredVariableWithFlag;
import static org.daiitech.naftah.parser.NaftahParserHelper.prepareRun;

/**
 * Browser-hosted runtime for the Naftah Playground.
 *
 * <p>This class provides the bridge between the Java-based Naftah runtime
 * and the browser environment. It is intended to run under browser-hosted
 * Java runtimes such as CheerpJ and exposes native methods that allow
 * communication with JavaScript and the playground user interface.</p>
 *
 * <p>The runtime is responsible for:</p>
 * <ul>
 * <li>Initializing the playground environment.</li>
 * <li>Bootstrapping language metadata and class-scanning results.</li>
 * <li>Exposing the runtime instance to JavaScript.</li>
 * <li>Executing user-provided Naftah source code.</li>
 * <li>Reporting execution output and errors back to the browser.</li>
 * </ul>
 *
 * <p>Execution state is cleaned after each run to avoid stale parser state,
 * thread-local leaks, and unintended cross-execution interactions.</p>
 *
 * @author Chakib Daii
 */
public class NaftahPlayground {

	/**
	 * Logger used by the playground runtime.
	 */
	public static final Logger LOGGER;

	/**
	 * System property used to configure the playground logging level.
	 *
	 * <p>If defined, its value is passed to the logging subsystem during
	 * class initialization to determine the minimum log level emitted by
	 * the playground runtime.</p>
	 */
	private static final String NAFTAH_PLAYGROUND_LOG_LEVEL_PROPERTY = "naftah.playground.log.level";

	/**
	 * System property used to enable or disable the classpath index in the playground.
	 *
	 * <p>When set to {@code true}, the runtime will use the precomputed classpath
	 * index for faster lookup instead of performing full classpath scanning.</p>
	 *
	 * <p>If not defined, the default behavior is used.</p>
	 */
	private static final String NAFTAH_PLAYGROUND_USE_CLASSPATH_INDEX_PROPERTY = "naftah.playground.index.active";

	/**
	 * Absolute file system path to the full runtime index cache used by the playground.
	 *
	 * <p>This value is resolved during static initialization and points to the
	 * precomputed index file used to accelerate class and function lookups.</p>
	 */
	public static final Path INDEX_CACHE_PATH;

	/**
	 * Absolute file system path to the minimal runtime index cache used by the playground.
	 *
	 * <p>The minimal index is a lightweight version of the full runtime index,
	 * optimized for reduced memory footprint and faster startup times.</p>
	 */
	public static final Path MINIMAL_INDEX_CACHE_PATH;

	/**
	 * System property used to control asynchronous cache loading behavior.
	 *
	 * <p>If defined and enabled, cache entries may be loaded asynchronously,
	 * allowing cache population to occur in the background without blocking
	 * the calling thread. If not specified, the default cache loading
	 * behavior configured by the runtime is used.</p>
	 */
	public static final String CACHE_LOAD_ASYNC_PROPERTY = "naftah.cache.async";

	static {
		LogManager.setLevel(System.getProperty(NAFTAH_PLAYGROUND_LOG_LEVEL_PROPERTY));
		LOGGER = LogManager.getLogger("NaftahPlayground");

		String indexCachePathProp = System.getProperty(INDEX_CACHE_PATH_PROPERTY);
		INDEX_CACHE_PATH = indexCachePathProp == null ? null : Paths.get(indexCachePathProp);

		String minimalIndexCachePathProp = System.getProperty(MINIMAL_INDEX_CACHE_PATH_PROPERTY);
		MINIMAL_INDEX_CACHE_PATH = minimalIndexCachePathProp == null ? null : Paths.get(minimalIndexCachePathProp);
	}

	/**
	 * Indicates whether the system is currently in the bootstrap phase.
	 *
	 * <p>When {@code true}, certain runtime behaviors may be restricted or handled
	 * differently to support safe initialization.</p>
	 *
	 * <p>This field is {@code volatile} to ensure visibility across threads.</p>
	 */
	private static volatile boolean BOOTSTRAPPING = false;

	/**
	 * Sends text to the playground output area in the browser.
	 *
	 * <p>This method is implemented by the JavaScript host environment and is
	 * used to display execution results, diagnostic information, and error
	 * messages to the user.</p>
	 *
	 * @param s text to display
	 */
	public static native void sendToHTML(String s);

	/**
	 * Sends a message to the browser-side logging system.
	 *
	 * <p>This method is implemented by the JavaScript host and is intended for
	 * low-level diagnostics and debugging output.</p>
	 *
	 * @param s message to log
	 */
	public static native void sendToLog(String s);

	/**
	 * Registers the playground runtime instance with the browser environment.
	 *
	 * <p>The JavaScript host uses this method to obtain a reference to the
	 * running playground instance so that browser code can invoke methods such
	 * as {@link #run(String)}.</p>
	 *
	 * @param app the playground runtime instance
	 */
	public static native void nativeSetApplication(NaftahPlayground app);

	/**
	 * Updates the bootstrap state visible to the browser UI.
	 *
	 * <p>This method is implemented by the JavaScript host and allows the
	 * runtime to communicate its initialization progress.</p>
	 *
	 * @param state the current runtime state
	 */
	public static native void setBootstrapState(String state);

	/**
	 * Initializes and starts the playground runtime.
	 *
	 * <p>This method performs the following steps:</p>
	 * <ol>
	 * <li>Creates and exposes the runtime instance to JavaScript.</li>
	 * <li>Configures runtime and scanning properties.</li>
	 * <li>Loads cached class-scanning metadata.</li>
	 * <li>Bootstraps built-in functions and accessible classes.</li>
	 * <li>Publishes bootstrap status updates to the browser UI.</li>
	 * </ol>
	 *
	 * <p>If initialization fails, the runtime enters the
	 * {@link RuntimeState#FAILED} state and logs diagnostic information.</p>
	 *
	 * @param args command-line arguments; ignored
	 */
	public static void main(String[] args) {
		NaftahPlayground app = new NaftahPlayground();

		// keep runtime alive
		new Thread(() -> {
			// expose instance to JS
			nativeSetApplication(app);
			LOGGER.info("Naftah Playground started");
		}).start();

		bootstrap();
	}

	/**
	 * Bootstraps the Naftah playground runtime.
	 *
	 * <p>This method initializes system properties required for execution,
	 * loads class scanning metadata from either the minimal or full cache,
	 * and prepares the runtime environment for executing Naftah code.</p>
	 *
	 * <p>Bootstrap phases:</p>
	 * <ol>
	 * <li>Configure runtime system properties (JDK scanning, caching, word chunking).</li>
	 * <li>Determine whether full classpath scanning should be used.</li>
	 * <li>Load class scanning metadata from cache (minimal or full).</li>
	 * <li>Initialize the playground runtime via {@code bootstrapPlayground}.</li>
	 * <li>Log runtime metadata for debugging purposes.</li>
	 * <li>Signal final runtime state (READY or FAILED).</li>
	 * </ol>
	 *
	 * <p>The method is designed to run once during application startup. It is
	 * not thread-safe and should not be invoked concurrently.</p>
	 *
	 * <p>If any error occurs during initialization, the runtime is placed in
	 * {@link RuntimeState#FAILED} state and diagnostic information is logged.
	 * Regardless of success or failure, a final "bootstrap done" message is
	 * emitted.</p>
	 *
	 * <p><b>Performance note:</b> loading and parsing the class scanning cache
	 * can be expensive in browser environments (e.g., CheerpJ), especially when
	 * using the full cache. Consider using the minimal cache for faster startup.</p>
	 */
	private static synchronized void bootstrap() {
		BOOTSTRAPPING = true;
		new Thread(() -> {
			System.setProperty(INSIDE_RUN_PROPERTY, Boolean.toString(true));
			System.setProperty(Naftah.SCAN_JDK_PROPERTY, Boolean.toString(false));
			System.setProperty(Naftah.CACHE_SCANNING_RESULTS_PROPERTY, Boolean.toString(false));
			if (Objects.isNull(System.getProperty(Naftah.WORD_CHUNK_PROPERTY))) {
				System.setProperty(Naftah.WORD_CHUNK_PROPERTY, Boolean.toString(true));
			}

			boolean shouldScanClasspath = Boolean.getBoolean(SCAN_CLASSPATH_PROPERTY);

			LOGGER.debug("Should scan classpath: " + shouldScanClasspath);

			LOGGER.debug("Minimal index cache path : " + System.getProperty(MINIMAL_INDEX_CACHE_PATH_PROPERTY));
			LOGGER
					.debug("Minimal index cache path file exists: " + (MINIMAL_INDEX_CACHE_PATH != null && Files
							.exists(MINIMAL_INDEX_CACHE_PATH)));

			LOGGER.debug("Index cache path : " + System.getProperty(INDEX_CACHE_PATH_PROPERTY));
			LOGGER
					.debug("Index cache path file exists: " + (INDEX_CACHE_PATH != null && Files
							.exists(INDEX_CACHE_PATH)));

			LOGGER.debug("Minimal cache path : " + System.getProperty(MINIMAL_CACHE_PATH_PROPERTY));
			LOGGER.debug("Minimal cache path file exists: " + Files.exists(DefaultContext.MINIMAL_CACHE_PATH));

			LOGGER.debug("Cache path : " + System.getProperty(CACHE_PATH_PROPERTY));
			LOGGER.debug("Cache path file exists: " + Files.exists(DefaultContext.CACHE_PATH));

			try {
				setBootstrapState(RuntimeState.BOOTSTRAPPING.name());

				boolean useIndex = Boolean.getBoolean(NAFTAH_PLAYGROUND_USE_CLASSPATH_INDEX_PROPERTY);
				boolean asyncCacheLoading = Boolean.getBoolean(CACHE_LOAD_ASYNC_PROPERTY);
				LOGGER.debug("loading Cache asynchronously: " + asyncCacheLoading + " using index: " + useIndex);

				if (useIndex) {
					ClassScanningIndex classScanningIndex = ClassScanningLoader
							.loadClassScanningIndexFromJson(shouldScanClasspath ?
									INDEX_CACHE_PATH :
									MINIMAL_INDEX_CACHE_PATH, asyncCacheLoading);

					bootstrapPlayground(classScanningIndex);
				}
				else {
					ClassScanningResult classScanningResult = ClassScanningLoader
							.loadClassScanningResultFromJson(shouldScanClasspath ?
									DefaultContext.CACHE_PATH :
									DefaultContext.MINIMAL_CACHE_PATH, asyncCacheLoading);

					bootstrapPlayground(classScanningResult);
				}

				LOGGER.debug("Classes : " + Objects.requireNonNullElse(DefaultContext.getClasses(), Map.of()));
				LOGGER
						.debug("AccessibleClasses : " + Objects
								.requireNonNullElse(DefaultContext.getAccessibleClasses(), Map.of()));
				LOGGER
						.debug("InstantiableClasses : " + Objects
								.requireNonNullElse(DefaultContext.getInstantiableClasses(), Map.of()));
				LOGGER
						.debug("BuiltinFunctions : " + Objects
								.requireNonNullElse(DefaultContext.getBuiltinFunctions(), Map.of()));
				LOGGER
						.debug("BuiltinFunctionsIndex : " + Objects
								.requireNonNullElse(DefaultContext.getBuiltinFunctionsIndex(), Map.of()));
				LOGGER
						.debug("JvmFunctions : " + Objects
								.requireNonNullElse(DefaultContext.getJvmFunctions(), Map.of()));
				LOGGER
						.debug("JvmClassInitializers : " + Objects
								.requireNonNullElse(DefaultContext.getJvmClassInitializers(), Map.of()));

				setBootstrapState(RuntimeState.READY.name());
			}
			catch (Throwable th) {
				LOGGER
						.error("error : " + th.getClass() + " - " + ExceptionUtils
								.getMostSpecificCause(th)
								.getMessage() + "\nstack trace : " + Arrays.toString(th.getStackTrace()));
				setBootstrapState(RuntimeState.FAILED.name());
			}
			finally {
				BOOTSTRAPPING = false;
				LOGGER.info("bootstrap done.");
			}
		}).start();
	}

	/**
	 * Executes a snippet of Naftah source code.
	 *
	 * <p>The provided code is parsed, evaluated, and its result is returned.
	 * If the result is printable, it is also sent to the browser output area.
	 * Variable declarations and {@code None} values do not produce output.</p>
	 *
	 * <p>Any exception thrown during parsing or execution is converted into a
	 * stack trace, displayed in the browser, and returned to the caller.</p>
	 *
	 * <p>Regardless of success or failure, parser state, execution context,
	 * and thread-local resources are cleared before returning.</p>
	 *
	 * @param code the Naftah source code to execute
	 * @return the execution result as a string; an empty string if no output
	 *         is produced; or the exception stack trace if execution fails
	 */
	public String run(String code) {
		if (BOOTSTRAPPING) {
			throw new IllegalStateException(
											"لا يمكن تنفيذ هذا الأمر أثناء تهيئة النظام"
			);
		}

		if (code == null || code.isBlank()) {
			return "";
		}

		try {

			LOGGER.debug("Running code: " + code);

			CharStream input = getCharStream(code);

			var parser = prepareRun(input, NaftahErrorListener.INSTANCE);

			var result = doRun(parser, List.of());

			if (result == null || None.isNone(result)) {
				return "";
			}

			if (isDeclaredVariableWithFlag(result)) {
				return "";
			}

			String output;

			if (isSimpleOrBuiltinOrCollectionOrMapOfSimpleType(result)) {
				output = getNaftahValueToString(result);
			}
			else {
				output = String.valueOf(result);
			}

			sendToHTML(output);

			return output;

		}
		catch (Throwable th) {
			LOGGER
					.debug("Error occurred while running code: " + th.getClass() + " - " + ExceptionUtils
							.getMostSpecificCause(th)
							.getMessage() + "\nstack trace : " + Arrays.toString(th.getStackTrace()));

			StringWriter sw = new StringWriter();
			th.printStackTrace(new PrintWriter(sw));

			String err = sw.toString();

			sendToHTML(err);

			return err;
		}
		finally {
			DefaultContext.clear();
			cleanClassThreadLocals();
		}
	}

	/**
	 * Represents the initialization state of the playground runtime.
	 */
	public enum RuntimeState {

		/**
		 * The runtime is currently loading metadata and bootstrapping
		 * language resources.
		 */
		BOOTSTRAPPING,

		/**
		 * The runtime has been successfully initialized and is ready
		 * to execute code.
		 */
		READY,

		/**
		 * Initialization failed and the runtime cannot accept execution
		 * requests.
		 */
		FAILED
	}
}
