package org.daiitech.naftah.builtin.utils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.daiitech.naftah.Naftah;

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
      throw new RuntimeException("Could not determine JAR directory", e);
    }
  }
}
