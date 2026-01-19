// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import org.daiitech.naftah.errors.NaftahBugError;

import static org.daiitech.naftah.Naftah.SCAN_JDK_PROPERTY;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;
import static org.daiitech.naftah.utils.ResourceUtils.getJarDirectory;
import static org.daiitech.naftah.utils.ResourceUtils.readFileLines;
import static org.daiitech.naftah.utils.reflect.ClassUtils.getQualifiedName;
import static org.daiitech.naftah.utils.reflect.ClassUtils.isInstantiableClass;

/**
 * Utility class to scan and load classes at runtime from the classpath, directories, and JAR files.
 * Provides methods to find class names and load classes using different class loaders.
 * <p>
 * This class is not meant to be instantiated.
 * </p>
 *
 * @author Chakib Daii
 */
public final class RuntimeClassScanner {
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
	 * Flag indicating whether to scan JDK classes for Naftah types,
	 * obtained from the system property {@code naftah.scanJDK}.
	 */
	public static final boolean SCAN_JDK = Boolean.getBoolean(SCAN_JDK_PROPERTY);

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
	public static final String[] BASE_PACKAGES = {"", "sun", "java", "javax", "com", "org", "edu", "net"};

	/**
	 * Array of filesystem paths (directories or archives) used as scan roots.
	 * Initialized statically by combining the classpath and Java home paths,
	 * filtering out ignored dependencies if available.
	 */
	public static final String[] PATHS;

	/**
	 * Array of ClassLoaders used when attempting to load classes.
	 * Includes the system class loader, the platform class loader, and the bootstrap class loader.
	 */
	public static final ClassLoader[] CLASS_LOADERS = { ClassLoader.getSystemClassLoader(),
														ClassLoader.getPlatformClassLoader(),
														Object.class.getClassLoader()};

	static {
		// Get the classpath and java home files
		String[] tempPaths = (CLASS_PATH + (SCAN_JDK ? File.pathSeparator + JAVA_HOME : "")).split(File.pathSeparator);
		PATHS = getPaths(tempPaths);
	}

	/**
	 * Private constructor to prevent instantiation.
	 * Always throws a {@link NaftahBugError} when called.
	 */
	private RuntimeClassScanner() {
		throw newNaftahBugInvalidUsageError();
	}

	/**
	 * Filters out paths that match any of the ignored JARs.
	 *
	 * @param tempPaths   the array of paths to filter
	 * @param ignoredJars a list of JAR names to ignore
	 * @return a filtered array of paths
	 */
	public static String[] getPaths(String[] tempPaths, List<String> ignoredJars) {
		return Arrays
				.stream(tempPaths)
				.filter(path -> ignoredJars.stream().noneMatch(path::contains))
				.toArray(String[]::new);
	}

	/**
	 * Returns classpath paths while excluding ignored JARs defined in the
	 * "original-dependencies" file.
	 *
	 * @param tempPaths the array of paths to filter
	 * @return a filtered array of paths
	 */
	public static String[] getPaths(String[] tempPaths) {
		try {
			var ignoredJars = readFileLines(getJarDirectory() + "/original-dependencies");
			tempPaths = getPaths(tempPaths, ignoredJars);
		}
		catch (IOException ignored) {
		}
		return tempPaths;
	}

	/**
	 * Scans for classes in the default classpath and java home paths.
	 *
	 * @return a map of fully qualified class names to their corresponding class loaders (may be null if default)
	 */
	public static Map<String, ClassLoader> scanClasses() {
		return scanClasses(PATHS, null);
	}

	/**
	 * Scans for classes in the given paths.
	 *
	 * @param paths an array of file system paths (directories or JAR files) to scan for classes
	 * @return a map of fully qualified class names to their corresponding class loaders (may be null if default)
	 */
	public static Map<String, ClassLoader> scanClasses(String[] paths, String packagePath) {
		Map<String, ClassLoader> classNames = new HashMap<>();
		for (String path : paths) {
			classNames.putAll(scanClasses(path, packagePath));
		}
		return classNames;
	}

	/**
	 * Scans a single path (directory or JAR/JMOD file) for classes.
	 *
	 * @param path the file system path to scan
	 * @return a map of fully qualified class names to their corresponding {@link ClassLoader}s;
	 *         may be {@code null} if default
	 */
	public static Map<String, ClassLoader> scanClasses(String path, String packagePath) {
		Map<String, ClassLoader> classNames = new HashMap<>();
		File file = new File(path);
		if (file.exists()) {
			if (file.isDirectory()) {
				classNames.putAll(findClassesInDirectory(file, file, packagePath));
			}
			else if (file.getName().endsWith(JAR_EXTENSION) || file.getName().endsWith(JMOD_EXTENSION)) {
				classNames.putAll(findClassesInJar(file, packagePath));
			}
		}
		return classNames;
	}

	/**
	 * Scans all classes within the given package.
	 *
	 * <p>Uses the class loaders in {@link #CLASS_LOADERS} to locate the package resource,
	 * then scans the resolved path for classes.</p>
	 *
	 * @param packageName the fully qualified package name to scan
	 * @return a map of fully qualified class names to their corresponding {@link ClassLoader}s
	 * @throws NullPointerException if the package cannot be found in any class loader
	 */
	public static Map<String, ClassLoader> scanPackageCLasses(String packageName) {
		String packagePath = packageName.replace('.', '/');
		URL resource = null;
		for (ClassLoader classLoader : CLASS_LOADERS) {
			if (Objects.isNull(resource)) {
				resource = classLoader.getResource(packagePath);
			}
		}

		if (Objects.nonNull(resource)) {
			String protocol = resource.getProtocol();

			if ("file".equals(protocol)) {
				return scanClasses(resource.getFile(), packagePath);
			}
			else if ("jar".equals(protocol)) {
				String url = resource.toString();
				int separatorIndex = url.indexOf("!/");
				String jarPath = url.substring(url.indexOf("file:") + 5, separatorIndex);
				String insidePath = url.substring(separatorIndex + 2);
				return scanClasses(jarPath, insidePath);
			}
		}
		throw new IllegalArgumentException();
	}

	/**
	 * Loads classes from the given map of class names and their associated class loaders, returning a set of Class
	 * objects.
	 *
	 * @param classNames     map of class names to their class loaders
	 * @param accessibleOnly if true, only loads accessible (public) classes; if false, loads all classes
	 * @return a set of loaded Class objects
	 */
	public static Set<Class<?>> loadClassSet(Map<String, ClassLoader> classNames, boolean accessibleOnly) {
		return new HashSet<>(loadClasses(classNames, accessibleOnly).values());
	}

	/**
	 * Loads classes from the given map of class names and their associated class loaders, optionally filtering by
	 * whether they are instantiable.
	 *
	 * @param classNames       map of class names to their class loaders
	 * @param instantiableOnly if true, only includes classes that can be instantiated; if false, includes all
	 * @return a map of qualified class names to their loaded Class objects
	 */
	public static Map<String, Class<?>> loadClasses(Map<String, ClassLoader> classNames, boolean instantiableOnly) {
		return loadClasses(classNames, CLASS_LOADERS, instantiableOnly);
	}

	/**
	 * Loads classes from the given map of class names and their associated class loaders using the specified
	 * array of class loaders, optionally filtering by instantiability.
	 *
	 * @param classNames       a map of class names to their respective class loaders
	 * @param classLoaders     an array of {@link ClassLoader} instances to attempt loading classes from
	 * @param instantiableOnly if true, only includes classes that can be instantiated; if false, includes all classes
	 * @return a map of fully qualified class names to their loaded {@link Class} objects
	 */
	public static Map<String, Class<?>> loadClasses(Map<String, ClassLoader> classNames,
													ClassLoader[] classLoaders,
													boolean instantiableOnly) {
		// Try to load each class and store Class objects
		Map<String, Class<?>> loadedClasses = new HashMap<>();
		for (var nameEntry : classNames.entrySet()) {
			var classLoaderOptional = Optional.ofNullable(nameEntry.getValue());
			var loaders = Arrays.copyOf(classLoaders, classLoaders.length + (classLoaderOptional.isEmpty() ? 0 : 1));
			classLoaderOptional.ifPresent(classLoader -> loaders[classLoaders.length] = classLoader);
			for (ClassLoader cl : loaders) {
				try {
					if (Objects.isNull(cl)) {
						continue;
					}
					String className = nameEntry.getKey();
					Class<?> currentClass = Class.forName(className, false, cl);
					if (!instantiableOnly || isInstantiableClass(currentClass)) {
						String qualifiedName = getQualifiedName(className);
						loadedClasses.put(qualifiedName, currentClass);
					}
					break;
				}
				catch (Throwable t) {
					// Silently skip classes that can't be loaded
				}
			}
		}
		return loadedClasses;
	}

	/**
	 * Recursively scans for classes inside a directory.
	 *
	 * @param root root directory where scanning started
	 * @param dir  current directory or file to scan
	 * @return a map of fully qualified class names to their corresponding class loaders (null here)
	 */
	public static Map<String, ClassLoader> findClassesInDirectory(File root, File dir, String packagePath) {
		Map<String, ClassLoader> classNames = new HashMap<>();
		for (File file : Objects.requireNonNull(dir.listFiles())) {
			if (file.isDirectory()) {
				classNames.putAll(findClassesInDirectory(root, file, packagePath));
			}
			else if ((Objects.isNull(packagePath) || file
					.getPath()
					.replace('\\', '/')
					.contains(packagePath)) && IGNORE_CLASS
							.stream()
							.noneMatch(s -> file.getName().endsWith(s)) && file
									.getName()
									.endsWith(CLASS_EXTENSION)) {
										String className = file
												.getAbsolutePath()
												.substring(root.getAbsolutePath().length() + 1);
										if (Objects.nonNull(packagePath)) {
											className = packagePath + File.separatorChar + className;
										}

										className = className
												.replace("/", ".")
												.replace(File.separatorChar, '.')
												.replaceAll(CLASS_EXTENSION_REGEX, "");

										classNames.put(className, null);
									}
			else if (file.getName().endsWith(JAR_EXTENSION) || file.getName().endsWith(JMOD_EXTENSION)) {
				classNames.putAll(findClassesInJar(file, packagePath));
			}
		}
		return classNames;
	}


	/**
	 * Scans for classes inside a JAR or JMOD file.
	 *
	 * @param jarFile the JAR or JMOD file to scan
	 * @return a map of fully qualified class names to their associated class loaders (null or URLClassLoader for
	 *         nested
	 *         jars)
	 */
	public static Map<String, ClassLoader> findClassesInJar(File jarFile, String packagePath) {
		Map<String, ClassLoader> classNames = new HashMap<>();
		try (JarFile jar = new JarFile(jarFile)) {
			Enumeration<JarEntry> entries = jar.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				if ((Objects.isNull(packagePath) || entry
						.getName()
						.replace('\\', '/')
						.contains(packagePath)) && IGNORE_CLASS
								.stream()
								.noneMatch(s -> entry.getName().endsWith(s)) && entry
										.getName()
										.endsWith(CLASS_EXTENSION)) {
					String className = entry
							.getName()
							.replace("classes/", "") // handling jmod class prefix
							.replace("/", ".")
							.replace(File.separatorChar, '.')
							.replaceAll(CLASS_EXTENSION_REGEX, "");
					classNames.put(className, null);
				}
				else if (entry.getName().endsWith(JAR_EXTENSION) || entry.getName().endsWith(JMOD_EXTENSION)) {
					File tempInnerJar = jarEntryToTempFile(jar, entry);
					// Load inner JAR with URLClassLoader
					try (URLClassLoader loader = new URLClassLoader(new URL[]{tempInnerJar.toURI().toURL()},
																	RuntimeClassScanner.class.getClassLoader())) {
						classNames
								.putAll(findClassesInJar(tempInnerJar, packagePath)
										.keySet()
										.stream()
										.map(className -> Map.entry(className, loader))
										.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
					}
				}
			}
		}
		catch (Exception e) {
			// Silently skip classes that can't be loaded
		}
		return classNames;
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
