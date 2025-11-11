package org.daiitech.naftah.utils.reflect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import org.daiitech.naftah.builtin.lang.ScannedClass;
import org.daiitech.naftah.errors.NaftahBugError;

import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;

/**
 * Utility class for scanning runtime classes from the classpath and Java home directories.
 * <p>
 * This class provides methods to scan class files inside directories and JAR/JMOD archives,
 * and to load them reflectively using different class loaders.
 * </p>
 *
 * @author Chakib Daii
 * @deprecated This class is deprecated and marked for removal.
 */
@Deprecated(forRemoval = true)
public final class RuntimeClassScanner1 {
	/**
	 * System property key for the Java class path.
	 */
	public static final String CLASS_PATH_PROPERTY = "java.class.path";

	/**
	 * The Java class path obtained from the system property {@code java.class.path}.
	 * This is a list of paths where classes and resources are searched for.
	 */
	public static final String CLASS_PATH = System.getProperty(CLASS_PATH_PROPERTY);

	/**
	 * System property key for the Java installation directory (JAVA_HOME).
	 */
	public static final String JAVA_HOME_PROPERTY = "java.home";

	/**
	 * The Java home directory obtained from the system property {@code java.home}.
	 * Typically points to the directory where the JRE or JDK is installed.
	 */
	public static final String JAVA_HOME = System.getProperty(JAVA_HOME_PROPERTY);

	/**
	 * Array of filesystem paths (directories or archives) used as scan roots.
	 * Initialized statically by combining the classpath and Java home paths,
	 * filtering out ignored dependencies if available.
	 */
	public static final String[] PATHS = (CLASS_PATH + File.pathSeparator + JAVA_HOME).split(File.pathSeparator);

	/**
	 * The file extension for Java archive files (JAR).
	 */
	public static final String JAR_EXTENSION = ".jar";

	/**
	 * The file extension for Java module files (JMOD).
	 */
	public static final String JMOD_EXTENSION = ".jmod";

	/**
	 * The file extension for compiled Java class files.
	 */
	public static final String CLASS_EXTENSION = ".class";

	/**
	 * Regular expression pattern to match the class file extension {@code .class}.
	 * Used to identify class files in file names.
	 */
	public static final String CLASS_EXTENSION_REGEX = "\\.class$";

	/**
	 * A set of class file base names to ignore during scanning.
	 * Typically these include special files like module-info and package-info.
	 */
	public static final Set<String> IGNORE = Set.of("module-info", "package-info");

	/**
	 * A set of full class file names to ignore during scanning.
	 * This is the {@link #IGNORE} set with the {@code .class} extension appended.
	 */
	public static final Set<String> IGNORE_CLASS = IGNORE
			.stream()
			.map(s -> s + CLASS_EXTENSION)
			.collect(Collectors.toSet());

	/**
	 * Array of common base package names to be used when scanning classes.
	 * Includes standard Java and popular top-level package prefixes.
	 */
	public static final ClassLoader[] CLASS_LOADERS = { ClassLoader.getSystemClassLoader(),
														ClassLoader.getPlatformClassLoader(),
														Object.class.getClassLoader()};

	/**
	 * Private constructor to prevent instantiation.
	 * Always throws a {@link NaftahBugError} when called.
	 */
	private RuntimeClassScanner1() {
		throw newNaftahBugInvalidUsageError();
	}

	/**
	 * Scans for classes in the default classpath and java home paths.
	 *
	 * @return a map of fully qualified class names to their corresponding class loaders (may be null if default)
	 */
	public static Map<String, ScannedClass> scanCLasses() {
		return scanCLasses(PATHS);
	}

	/**
	 * Scans for classes in the given paths.
	 *
	 * @param paths an array of file system paths (directories or JAR files) to scan for classes
	 * @return a map of fully qualified class names to their corresponding class loaders (may be null if default)
	 */
	public static Map<String, ScannedClass> scanCLasses(String[] paths) {
		Map<String, ScannedClass> classes = new HashMap<>();
		for (String path : paths) {
			File file = new File(path);
			if (file.exists()) {
				if (file.isDirectory()) {
					classes.putAll(findClassesInDirectory(file, file));
				}
				else if (file.getName().endsWith(JAR_EXTENSION) || file.getName().endsWith(JMOD_EXTENSION)) {
					classes.putAll(findClassesInJar(file));
				}
			}
		}
		return classes;
	}

	/**
	 * Attempts to load a class with the given name using a set of class loaders.
	 *
	 * <p>The method tries the predefined class loaders, optionally including the provided
	 * {@link URLClassLoader}, to load the class without initializing it.</p>
	 *
	 * @param className   the fully qualified name of the class to load
	 * @param classLoader an optional {@link URLClassLoader} to use for loading the class; may be null
	 * @return the {@link Class} object if found and loaded successfully; {@code null} if the class
	 *         could not be loaded by any of the class loaders
	 */
	public static Class<?> loadClass(String className, URLClassLoader classLoader) {
		var loaders = Arrays.copyOf(CLASS_LOADERS, CLASS_LOADERS.length + (Objects.nonNull(classLoader) ? 0 : 1));
		if (Objects.nonNull(classLoader)) {
			//noinspection DataFlowIssue
			loaders[CLASS_LOADERS.length] = classLoader;
		}

		for (ClassLoader cl : loaders) {
			try {
				if (Objects.isNull(cl)) {
					continue;
				}
				return Class.forName(className, false, cl);
			}
			catch (Throwable t) {
				// Silently skip classes that can't be loaded
			}
		}
		return null;
	}


	/**
	 * Recursively scans for classes inside a directory.
	 *
	 * @param root root directory where scanning started
	 * @param dir  current directory or file to scan
	 * @return a map of fully qualified class names to their corresponding class loaders (null here)
	 */
	public static Map<String, ScannedClass> findClassesInDirectory(File root, File dir) {
		Map<String, ScannedClass> classes = new HashMap<>();
		for (File file : Objects.requireNonNull(dir.listFiles())) {
			if (file.isDirectory()) {
				classes.putAll(findClassesInDirectory(root, file));
			}
			else if (IGNORE_CLASS.stream().noneMatch(s -> file.getName().endsWith(s)) && file
					.getName()
					.endsWith(CLASS_EXTENSION)) {
						String className = file
								.getAbsolutePath()
								.substring(root.getAbsolutePath().length() + 1)
								.replace("/", ".")
								.replace(File.separatorChar, '.')
								.replaceAll(CLASS_EXTENSION_REGEX, "");
						Class<?> clazz = loadClass(className, null);
						if (Objects.nonNull(clazz)) {
							var scannedClassOptional = ScannedClass.safeOf(clazz);
							scannedClassOptional
									.ifPresent(scannedClass -> classes.put(scannedClass.qualifiedName(), scannedClass));
						}
					}
			else if (file.getName().endsWith(JAR_EXTENSION) || file.getName().endsWith(JMOD_EXTENSION)) {
				classes.putAll(findClassesInJar(file));
			}
		}
		return classes;
	}


	/**
	 * Scans for classes inside a JAR or JMOD file.
	 *
	 * @param jarFile the JAR or JMOD file to scan
	 * @return a map of fully qualified class names to their associated class loaders (null or URLClassLoader for
	 *         nested
	 *         jars)
	 */
	public static Map<String, ScannedClass> findClassesInJar(File jarFile) {
		Map<String, ScannedClass> classes = new HashMap<>();
		try (JarFile jar = new JarFile(jarFile)) {
			Enumeration<JarEntry> entries = jar.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				if (IGNORE_CLASS.stream().noneMatch(s -> entry.getName().endsWith(s)) && entry
						.getName()
						.endsWith(CLASS_EXTENSION)) {
					String className = entry
							.getName()
							.replace("classes/", "") // handling jmod class prefix
							.replace("/", ".")
							.replace(File.separatorChar, '.')
							.replaceAll(CLASS_EXTENSION_REGEX, "");
					Class<?> clazz = loadClass(className, null);
					if (Objects.nonNull(clazz)) {
						var scannedClassOptional = ScannedClass.safeOf(clazz);
						scannedClassOptional
								.ifPresent(scannedClass -> classes.put(scannedClass.qualifiedName(), scannedClass));
					}
				}
				else if (entry.getName().endsWith(JAR_EXTENSION) || entry.getName().endsWith(JMOD_EXTENSION)) {
					File tempInnerJar = jarEntryToTempFile(jar, entry);
					// Load inner JAR with URLClassLoader
					try (URLClassLoader loader = new URLClassLoader(new URL[]{tempInnerJar.toURI().toURL()},
																	RuntimeClassScanner1.class.getClassLoader())) {
						classes
								.putAll(findClassesInJar(tempInnerJar)
										.keySet()
										.stream()
										.map(className -> Optional
												.ofNullable(loadClass(className, loader))
												.map(clazz -> {
													var scannedClassOptional = ScannedClass.safeOf(clazz);
													return scannedClassOptional
															.map(scannedClass -> Map
																	.entry( scannedClass.qualifiedName(),
																			scannedClass))
															.orElse(null);
												})
												.orElse(null))
										.filter(Objects::nonNull)
										.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
					}
				}
			}
		}
		catch (Exception e) {
			// Silently skip classes that can't be loaded
		}
		return classes;
	}

	/**
	 * Extracts a nested JAR entry from a JAR file and writes it to a temporary file.
	 * The temporary file is deleted on JVM exit.
	 *
	 * @param outerJar      the outer JAR file containing the nested JAR entry
	 * @param innerJarEntry the nested JAR entry inside the outer JAR
	 * @return a temporary File representing the extracted nested JAR
	 * @throws IOException if an I/O error occurs during extraction
	 */
	public static File jarEntryToTempFile(JarFile outerJar, JarEntry innerJarEntry) throws IOException {
		// Create a temp file
		File tempInnerJar = File
				.createTempFile("entry-",
								"-" + innerJarEntry.getName().replace("/", "-").replace(File.separatorChar, '-'));
		tempInnerJar.deleteOnExit();

		try (   InputStream is = outerJar.getInputStream(innerJarEntry);
				FileOutputStream os = new FileOutputStream(tempInnerJar)) {
			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = is.read(buffer)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
		}

		return tempInnerJar;
	}
}
