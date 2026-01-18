// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

package org.daiitech.naftah;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.utils.ResourceUtils;

import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;

/**
 * Exposes the Naftah release information such as version, build date, and build time.
 * <p>
 * This class loads the properties from {@code META-INF/naftah-release-info.properties}
 * at class initialization time.
 * <p>
 * It is a utility class and cannot be instantiated.
 *
 * @author Chakib Daii
 */
public final class ReleaseInfo {

	/**
	 * System property key representing the Naftah version.
	 */
	public static final String NAFTAH_VERSION_PROPERTY = "naftah.version";

	/**
	 * System property key representing the Naftah build/release date.
	 */
	public static final String NAFTAH_VERSION_DATE_PROPERTY = "naftah.version.date";

	/**
	 * System property key representing the Naftah framework build/release time.
	 */
	public static final String NAFTAH_VERSION_TIME_PROPERTY = "naftah.version.time";
	/**
	 * The loaded release information properties.
	 */
	private static final Properties RELEASE_INFO = new Properties();
	/**
	 * Path to the release info properties file inside the JAR.
	 */
	private static final String RELEASE_INFO_FILE = "META-INF/naftah-release-info.properties";
	/**
	 * Property key for the implementation version.
	 */
	private static final String KEY_IMPLEMENTATION_VERSION = "ImplementationVersion";
	/**
	 * Property key for the bundle version.
	 */
	private static final String KEY_BUNDLE_VERSION = "BundleVersion";
	/**
	 * Property key for the build date.
	 */
	private static final String KEY_BUILD_DATE = "BuildDate";
	/**
	 * Property key for the build time.
	 */
	private static final String KEY_BUILD_TIME = "BuildTime";

	static {
		URL url;
		ClassLoader cl = ReleaseInfo.class.getClassLoader();
		if (cl == null) {
			cl = ClassLoader.getSystemClassLoader();
		}
		if (cl instanceof URLClassLoader) {
			// this avoids going through the parent classloaders/bootstrap
			url = ((URLClassLoader) cl).findResource(RELEASE_INFO_FILE);
		}
		else {
			// fallback option as ClassLoader#findResource() is protected
			url = cl.getResource(RELEASE_INFO_FILE);
		}
		if (url != null) {
			try (InputStream is = ResourceUtils.openStream(url, false)) {
				if (is != null) {
					RELEASE_INFO.load(is);
				}
			}
			catch (IOException ioex) {
				// ignore. In case of some exception, release info is not available
			}
		}
	}

	/**
	 * Private constructor to prevent instantiation.
	 * Always throws a {@link NaftahBugError} when called.
	 */
	private ReleaseInfo() {
		throw newNaftahBugInvalidUsageError();
	}

	/**
	 * Returns the implementation version of the release.
	 *
	 * @return the implementation version string, or an empty string if not found
	 */
	public static String getVersion() {
		return get(KEY_IMPLEMENTATION_VERSION);
	}

	/**
	 * Returns the bundle version of the release.
	 *
	 * @return the bundle version string, or an empty string if not found
	 */
	public static String getBundleVersion() {
		return get(KEY_BUNDLE_VERSION);
	}

	/**
	 * Returns the build date of the release.
	 *
	 * @return the build date string, or an empty string if not found
	 */
	public static String getBuildDate() {
		return get(KEY_BUILD_DATE);
	}

	/**
	 * Returns the build time of the release.
	 *
	 * @return the build time string, or an empty string if not found
	 */
	public static String getBuildTime() {
		return get(KEY_BUILD_TIME);
	}

	/**
	 * Returns all loaded release information properties.
	 *
	 * @return a {@link Properties} object containing all release metadata
	 */
	public static Properties getAllProperties() {
		return RELEASE_INFO;
	}

	/**
	 * Returns the value of a specific release property.
	 *
	 * @param propName the name of the property
	 * @return the value of the property, or an empty string if not found
	 */
	private static String get(String propName) {
		String propValue = RELEASE_INFO.getProperty(propName);
		return (propValue == null ? "" : propValue);
	}
}
