package org.daiitech.naftah;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.utils.arabic.ArabicOutputTransformer;

/**
 * @author Chakib Daii
 */
public final class NaftahSystem {

  private NaftahSystem() {}

  /** Returns the groovy version */
  public static String getVersion() {
    return ReleaseInfo.getVersion();
  }

  /**
   * Returns the major and minor part of the naftah version excluding the point/patch part of the
   * version. E.g. 1.0.0, 1.0.0-SNAPSHOT, 1.0.0-rc-1 all have 1.0 as the short version.
   *
   * @since 3.0.1
   */
  public static String getShortVersion() {
    String full = getVersion();
    int firstDot = full.indexOf('.');
    int secondDot = full.indexOf('.', firstDot + 1);
    if (secondDot < 0) {
      throw new NaftahBugError("Unexpected version found: " + full);
    }
    return full.substring(0, secondDot);
  }

  public static void setupOutput() {
    OutputStream out = new ArabicOutputTransformer(System.out);
    PrintStream ps = new PrintStream(out, true, StandardCharsets.UTF_8);
    System.setOut(ps);
  }
}
