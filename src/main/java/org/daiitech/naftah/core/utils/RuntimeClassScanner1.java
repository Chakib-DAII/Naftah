package org.daiitech.naftah.core.utils;

import org.daiitech.naftah.core.builtin.lang.ScannedClass;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * @author Chakib Daii
 *     <p>TODO: create the implementation and make it run in parallel fashion and blocks the
 *     interpreation only in case of need of any of these classes and the loading did't finish yet
 *     <p>
 */
public final class RuntimeClassScanner1 {
    public static final String CLASS_PATH_PROPERTY = "java.class.path";
    public static final String CLASS_PATH = System.getProperty(CLASS_PATH_PROPERTY);
    public static final String JAVA_HOME_PROPERTY = "java.home";
    public static final String JAVA_HOME = System.getProperty(JAVA_HOME_PROPERTY);
    public static final String JAR_EXTENSION = ".jar";
    public static final String JMOD_EXTENSION = ".jmod";
    public static final String CLASS_EXTENSION = ".class";
    public static final String CLASS_EXTENSION_REGEX = "\\.class$";
    public static final Set<String> IGNORE = Set.of("module-info", "package-info");
    public static final Set<String> IGNORE_CLASS = IGNORE.stream().map(s -> s + CLASS_EXTENSION).collect(Collectors.toSet());
    // Get the classpath and java home files
    public static final String[] PATHS = (CLASS_PATH + File.pathSeparator + JAVA_HOME).split(File.pathSeparator);
    public static final ClassLoader[] CLASS_LOADERS = {
            ClassLoader.getSystemClassLoader(),
            ClassLoader.getPlatformClassLoader(),
            Object.class.getClassLoader()
    };

    /**
     * scans classes from default paths
     * @return map of class files and possible {@link URLClassLoader}
     */
    public static Map<String, ScannedClass> scanCLasses() {
        return scanCLasses(PATHS);
    }
    public static Map<String, ScannedClass> scanCLasses(String[] paths) {
        Map<String, ScannedClass> classes = new HashMap<>();
        for (String path : paths) {
            File file = new File(path);
            if (file.exists()) {
                if (file.isDirectory()) {
                    classes.putAll(findClassesInDirectory(file, file));
                } else if (file.getName().endsWith(JAR_EXTENSION) || file.getName().endsWith(JMOD_EXTENSION)) {
                    classes.putAll(findClassesInJar(file));
                }
            }
        }
        return classes;
    }

    public static Class<?> loadClass(String className, URLClassLoader classLoader) {
        var loaders = Arrays.copyOf(CLASS_LOADERS, CLASS_LOADERS.length + (Objects.nonNull(classLoader) ? 0 : 1));
        if (Objects.nonNull(classLoader)) loaders[CLASS_LOADERS.length + 1] = classLoader;

        for (ClassLoader cl : loaders) try {
            if (Objects.isNull(cl)) continue;
            return Class.forName(className, false, cl);
        } catch (Throwable t) {
            // Silently skip classes that can't be loaded
        }
        return null;
    }

    /**
     * scans classes inside directories
     * @param root root directory where to start the scan
     * @param dir current dir/file
     * @return map of class files and possible {@link URLClassLoader}
     */
    public static Map<String, ScannedClass> findClassesInDirectory(File root, File dir) {
        Map<String, ScannedClass> classes = new HashMap<>();
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                classes.putAll(findClassesInDirectory(root, file));
            } else if (IGNORE_CLASS.stream().noneMatch(s -> file.getName().endsWith(s)) && file.getName().endsWith(CLASS_EXTENSION)) {
                String className = file.getAbsolutePath()
                        .substring(root.getAbsolutePath().length() + 1)
                        .replace("/", ".")
                        .replace(File.separatorChar, '.')
                        .replaceAll(CLASS_EXTENSION_REGEX, "");
                Class<?> clazz = loadClass(className, null);
                if (Objects.nonNull(clazz)) {
                    var scannedClassOptional = ScannedClass.safeOf(clazz);
                    scannedClassOptional.ifPresent(scannedClass -> classes.put(scannedClass.qualifiedName(), scannedClass));;
                }
            } else if (file.getName().endsWith(JAR_EXTENSION) || file.getName().endsWith(JMOD_EXTENSION)) {
                classes.putAll(findClassesInJar(file));
            }
        }
        return classes;
    }

    /**
     * scans classes inside jar
     * @param jarFile jar file
     * @return map of class files and possible {@link URLClassLoader}
     */
    public static Map<String, ScannedClass> findClassesInJar(File jarFile) {
        Map<String, ScannedClass> classes = new HashMap<>();
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (IGNORE_CLASS.stream().noneMatch(s -> entry.getName().endsWith(s))
                        && entry.getName().endsWith(CLASS_EXTENSION)) {
                    String className = entry.getName()
                            .replace("classes/", "") // handling jmod class prefix
                            .replace("/", ".")
                            .replace(File.separatorChar, '.')
                            .replaceAll(CLASS_EXTENSION_REGEX, "");
                    Class<?> clazz = loadClass(className, null);
                    if (Objects.nonNull(clazz)) {
                        var scannedClassOptional = ScannedClass.safeOf(clazz);
                        scannedClassOptional.ifPresent(scannedClass -> classes.put(scannedClass.qualifiedName(), scannedClass));;
                    }
                }  else if (entry.getName().endsWith(JAR_EXTENSION) || entry.getName().endsWith(JMOD_EXTENSION)) {
                    File tempInnerJar = jarEntryToTempFile(jar, entry);
                    // Load inner JAR with URLClassLoader
                    try(URLClassLoader loader = new URLClassLoader(
                            new URL[]{ tempInnerJar.toURI().toURL() },
                            RuntimeClassScanner1.class.getClassLoader()
                    )) {
                        classes.putAll(findClassesInJar(tempInnerJar).keySet().stream()
                                .map(className -> Optional.ofNullable(loadClass(className, loader))
                                        .map(clazz -> {
                                            var scannedClassOptional = ScannedClass.safeOf(clazz);
                                            return scannedClassOptional.map(scannedClass-> Map.entry(scannedClass.qualifiedName(), scannedClass))
                                                    .orElse(null);
                                        })
                                        .orElse(null))
                                        .filter(Objects::nonNull)
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                    }
                }
            }
        } catch (Exception e) {
            // Silently skip classes that can't be loaded
        }
        return classes;
    }

    /**
     * reading jar nested entries
     * @param outerJar the system jar
     * @param innerJarEntry the jar inside the system jar
     * @return jar file
     * @throws IOException IO exception
     */
    public static File jarEntryToTempFile(JarFile outerJar, JarEntry innerJarEntry) throws IOException {
        // Create a temp file
        File tempInnerJar = File.createTempFile("entry-", "-" + innerJarEntry.getName()
                .replace("/", "-")
                .replace(File.separatorChar, '-'));
        tempInnerJar.deleteOnExit();

        try (InputStream is = outerJar.getInputStream(innerJarEntry);
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
