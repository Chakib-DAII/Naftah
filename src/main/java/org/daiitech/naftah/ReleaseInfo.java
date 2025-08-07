package org.daiitech.naftah;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

import org.daiitech.naftah.utils.ResourceUtils;

/**
 * @author Chakib Daii
 *     <p>Exposes the Naftah release information
 */
public class ReleaseInfo {

  private static final Properties RELEASE_INFO = new Properties();
  private static final String RELEASE_INFO_FILE = "META-INF/naftah-release-info.properties";
  private static final String KEY_IMPLEMENTATION_VERSION = "ImplementationVersion";
  private static final String KEY_BUNDLE_VERSION = "BundleVersion";
  private static final String KEY_BUILD_DATE = "BuildDate";
  private static final String KEY_BUILD_TIME = "BuildTime";

  static {
    URL url;
    ClassLoader cl = ReleaseInfo.class.getClassLoader();
    if (cl == null) cl = ClassLoader.getSystemClassLoader();
    if (cl instanceof URLClassLoader) {
      // this avoids going through the parent classloaders/bootstrap
      url = ((URLClassLoader) cl).findResource(RELEASE_INFO_FILE);
    } else {
      // fallback option as ClassLoader#findResource() is protected
      url = cl.getResource(RELEASE_INFO_FILE);
    }
    if (url != null) {
      try (InputStream is = ResourceUtils.openStream(url, false)) {
        if (is != null) {
          RELEASE_INFO.load(is);
        }
      } catch (IOException ioex) {
        // ignore. In case of some exception, release info is not available
      }
    }
  }

  public static String getVersion() {
    return get(KEY_IMPLEMENTATION_VERSION);
  }

  public static String getBundleVersion() {
    return get(KEY_BUNDLE_VERSION);
  }

  public static String getBuildDate() {
    return get(KEY_BUILD_DATE);
  }

  public static String getBuildTime() {
    return get(KEY_BUILD_TIME);
  }

  public static Properties getAllProperties() {
    return RELEASE_INFO;
  }

  private static String get(String propName) {
    String propValue = RELEASE_INFO.getProperty(propName);
    return (propValue == null ? "" : propValue);
  }
}
