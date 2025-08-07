package org.daiitech.naftah.utils;

import static org.daiitech.naftah.parser.NaftahParserHelper.resolvePlaceholders;

import java.io.*;
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

/**
 * @author Chakib Daii
 */
public final class ResourceUtils {
  public static List<String> readFileLines(String filePath) throws IOException {
    Path path = Paths.get(filePath);
    // Reads all lines as UTF-8
    return Files.readAllLines(path);
  }

  public static Path getJarDirectory() {
    try {
      // Get the path of the currently executing JAR
      File jarFile =
          new File(Naftah.class.getProtectionDomain().getCodeSource().getLocation().toURI());

      // If it's a file (JAR), return its parent directory
      return jarFile.isFile() ? jarFile.getParentFile().toPath() : jarFile.toPath();
    } catch (URISyntaxException e) {
      throw new NaftahBugError("تعذر تحديد مجلد الـ JAR.", e);
    }
  }

  /**
   * Opens an {@link InputStream} reading from the given URL with/without caching the stream. This
   * prevents file descriptor leaks when reading from file system URLs.
   *
   * @param url the URL to connect to
   * @return an input stream reading from the URL connection
   */
  public static InputStream openStream(URL url, boolean useCaches) throws IOException {
    URLConnection urlConnection = url.openConnection();
    urlConnection.setUseCaches(useCaches);
    return urlConnection.getInputStream();
  }

  public static Properties getProperties(String filePath) {
    Properties props = new Properties();
    try (FileInputStream input = new FileInputStream(filePath)) {
      props.load(input);
      resolvePlaceholders(props);
    } catch (IOException e) {
      throw new NaftahBugError(e);
    }
    return props;
  }
}
