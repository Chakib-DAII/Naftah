package org.daiitech.naftah.utils.reflect;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.daiitech.naftah.utils.reflect.RuntimeClassScanner.CLASS_PATH_PROPERTY;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RuntimeClassScannerTests {

	static String originalClassPath;

	@BeforeAll
	static void setupAll() {
		originalClassPath = System.getProperty(CLASS_PATH_PROPERTY);

		String tempPaths = Arrays
				.stream((originalClassPath).split(File.pathSeparator))
				.filter(path -> path.contains("Naftah") && path.contains("classes") && path.contains("main"))
				.collect(Collectors.joining(File.pathSeparator));

		System.setProperty(CLASS_PATH_PROPERTY, tempPaths);
	}

	@AfterAll
	static void tearDownAll() {
		System.setProperty(CLASS_PATH_PROPERTY, originalClassPath);
	}

	@Test
	void scanCLassesTest() {
		Map<String, ClassLoader> classNames = RuntimeClassScanner.scanCLasses();
		assertNotNull(classNames);
		Set<Class<?>> accessibleClasses = RuntimeClassScanner.loadClassSet(classNames, true);
		Set<Class<?>> allClasses = RuntimeClassScanner.loadClassSet(classNames, false);
		assertNotNull(accessibleClasses);
		assertNotNull(allClasses);
		assertNotEquals(accessibleClasses.size(), allClasses.size());

	}

}
