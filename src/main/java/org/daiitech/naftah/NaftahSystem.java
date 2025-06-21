package org.daiitech.naftah;

import static org.daiitech.naftah.utils.arabic.ArabicOutputTransformer.getPrintStream;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.daiitech.naftah.errors.NaftahBugError;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

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

  public static void setupOutputStream() {
    PrintStream ps = getPrintStream(System.out);
    System.setOut(ps);
  }

  public static void setupErrorStream() {
    PrintStream ps = getPrintStream(System.err);
    System.setErr(ps);
  }

  public static int[] getTerminalWidthAndHeight() {
    try (Terminal terminal = getTerminal()) {
      return new int[] {terminal.getHeight(), terminal.getHeight()};
    } catch (Exception ignored) {
      return new int[] {80, 24}; // fallback width
    }
  }

  public static Terminal getTerminal() throws IOException {
    return TerminalBuilder.builder()
        .encoding(StandardCharsets.UTF_8)
        .streams(System.in, System.out)
        .jna(true)
        .jansi(true)
        .color(true)
        .nativeSignals(true)
        .system(true)
        .build();
  }
}
