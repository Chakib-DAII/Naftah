package org.daiitech.naftah.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import org.daiitech.naftah.Naftah;
import org.daiitech.naftah.errors.NaftahBugError;

import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;
import static org.daiitech.naftah.parser.NaftahParserHelper.resolvePlaceholders;

/**
 * Utility class for handling resources such as reading files,
 * locating the JAR directory, opening streams from URLs,
 * and loading properties files.
 * <p>
 * This class cannot be instantiated.
 *
 * @author Chakib Daii
 */
public final class ResourceUtils {

	/**
	 * Private constructor to prevent instantiation.
	 * Throws {@link NaftahBugError} if called.
	 */
	private ResourceUtils() {
		throw newNaftahBugInvalidUsageError();
	}

	/**
	 * Reads all lines from a text file and returns them as a list of strings.
	 * Uses UTF-8 encoding by default.
	 *
	 * @param filePath the path of the file to read
	 * @return a list of lines read from the file
	 * @throws IOException if an I/O error occurs reading from the file
	 */
	public static List<String> readFileLines(String filePath) throws IOException {
		Path path = Paths.get(filePath);
		return Files.readAllLines(path);
	}

	/**
	 * Returns the directory path of the currently running JAR file.
	 * If running from a JAR file, returns the directory containing the JAR.
	 * If running from an IDE or file system, returns the directory path directly.
	 *
	 * @return the directory path of the running JAR or execution directory
	 * @throws NaftahBugError if unable to determine the JAR directory due to URI syntax issues
	 */
	public static Path getJarDirectory() {
		try {
			File jarFile = new File(Naftah.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			return jarFile.isFile() ? jarFile.getParentFile().toPath() : jarFile.toPath();
		}
		catch (URISyntaxException e) {
			throw new NaftahBugError("تعذّر تحديد مسار ملف JAR.", e);
		}
	}

	/**
	 * Opens an {@link InputStream} from the specified URL with an option to enable or disable caching.
	 * Useful for preventing file descriptor leaks when reading from file system URLs.
	 *
	 * @param url       the URL to open a connection to
	 * @param useCaches whether to use caches or not
	 * @return an input stream for reading from the URL connection
	 * @throws IOException if an I/O exception occurs while opening the stream
	 */
	public static InputStream openStream(URL url, boolean useCaches) throws IOException {
		URLConnection urlConnection = url.openConnection();
		urlConnection.setUseCaches(useCaches);
		return urlConnection.getInputStream();
	}

	/**
	 * Loads properties from a properties file at the given path,
	 * and resolves any placeholders within the property values.
	 *
	 * @param filePath the path of the properties file to load
	 * @return a {@link Properties} object containing the loaded properties
	 * @throws NaftahBugError if an error occurs while reading or loading the properties
	 */
	public static Properties getProperties(String filePath) {
		Properties props = new Properties();
		try (FileInputStream input = new FileInputStream(filePath)) {
			props.load(input);
			resolvePlaceholders(props);
		}
		catch (IOException e) {
			throw new NaftahBugError(e);
		}
		return props;
	}

	/**
	 * Loads properties from a resource file located in the classpath.
	 *
	 * <p>The resource file must be accessible via the class loader. If the file
	 * cannot be found or read, a {@link NaftahBugError} is thrown. The error
	 * message is in Arabic for clarity when the file is missing.</p>
	 *
	 * @param filePath The path to the resource file in the classpath.
	 * @return A {@link Properties} object containing all properties from the file.
	 * @throws NaftahBugError If the file cannot be found or an I/O error occurs.
	 */
	public static Properties getPropertiesFromResources(String filePath) {
		Properties props = new Properties();
		try (InputStream configStream = JulLoggerConfig.class.getClassLoader().getResourceAsStream(filePath)) {
			if (configStream == null) {
				throw new FileNotFoundException("تعذر العثور على الملف: " + filePath);
			}
			props.load(configStream);
		}
		catch (IOException e) {
			throw new NaftahBugError(e);
		}
		return props;
	}
}
