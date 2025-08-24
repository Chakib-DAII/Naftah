package org.daiitech.naftah.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.LogManager;

import org.daiitech.naftah.errors.NaftahBugError;

import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;

/**
 * Utility class for initializing Java Util Logging (JUL) configuration.
 * <p>
 * Provides methods to initialize logging configuration from external property files
 * or resources within the classpath. Ensures that the configuration is loaded only once.
 * </p>
 * <p>
 * Supports creating log file directories and files if they do not already exist.
 * </p>
 *
 * @author Chakib Daii
 */
public final class JulLoggerConfig {
	/**
	 * The system property key used by the Java Logging API to locate the logging configuration file.
	 * <p>
	 * Example usage: {@code System.setProperty(JAVA_LOGGING_FILE_PROPERTY, LOGGING_FILE);}
	 */
	public static final String JAVA_LOGGING_FILE_PROPERTY = "java.util.logging.config.file";

	/**
	 * The default name of the Java logging configuration file.
	 * <p>
	 * This file should typically be located on the classpath or in the working directory.
	 */
	public static final String LOGGING_FILE = "logging.properties";

	private static boolean initialized = false;

	/**
	 * Private constructor to prevent instantiation.
	 * Always throws a {@link NaftahBugError} when called.
	 */
	private JulLoggerConfig() {
		throw newNaftahBugInvalidUsageError();
	}

	/**
	 * Initializes the logging configuration from an external properties file path.
	 * This method is synchronized to prevent multiple initializations.
	 *
	 * @param propertiesPath the file system path to the logging properties file
	 * @throws IOException if the properties file cannot be loaded or read
	 */
	public static synchronized void initialize(String propertiesPath) throws IOException {
		try (InputStream configFile = Files.newInputStream(Path.of(propertiesPath))) {
			initialize(configFile);
		}
	}

	/**
	 * Initializes the logging configuration from a properties file located in the classpath resources.
	 * Ensures that the log file specified by the "java.util.logging.FileHandler.pattern" property exists,
	 * creating parent directories and the file itself if necessary.
	 * This method is synchronized to prevent multiple initializations.
	 *
	 * @param propertiesPath the resource path to the logging properties file within the classpath
	 * @throws IOException           if the properties file cannot be found, loaded, or read
	 * @throws FileNotFoundException if the resource cannot be found
	 */
	public static synchronized void initializeFromResources(String propertiesPath) throws IOException {
		try (InputStream configStream = JulLoggerConfig.class.getClassLoader().getResourceAsStream(propertiesPath)) {
			if (configStream == null) {
				throw new FileNotFoundException("تعذر العثور على الملف: " + propertiesPath);
			}

			// Step 2: Read properties (not yet loading into LogManager)
			Properties tempProps = new Properties();
			tempProps.load(configStream);

			// Step 3: Extract file pattern and resolve file path
			String pattern = tempProps.getProperty("java.util.logging.FileHandler.pattern");
			if (pattern != null) {
				Path logPath = Paths.get(pattern).toAbsolutePath();
				Files.createDirectories(logPath.getParent()); // Ensure parent dir exists
				if (!Files.exists(logPath)) {
					Files.createFile(logPath); // Create empty log file
				}
			}

			// Step 4: Reload the stream (LogManager needs a fresh one)
			try (InputStream configStream1 = Thread
					.currentThread()
					.getContextClassLoader()
					.getResourceAsStream(propertiesPath)) {
				if (configStream1 == null) {
					throw new IOException("تعذر إعادة قراءة الملف: " + propertiesPath);
				}
				initialize(configStream1);
			}
		}
	}

	/**
	 * Initializes the Java Util Logging framework from the given InputStream configuration.
	 * This method only performs initialization once; subsequent calls are ignored.
	 *
	 * @param configFile InputStream of the logging properties configuration
	 * @throws IOException if there is an error reading the configuration
	 */
	public static synchronized void initialize(InputStream configFile) throws IOException {
		if (!initialized) {
			LogManager.getLogManager().readConfiguration(configFile);
			initialized = true;
		}
	}
}
