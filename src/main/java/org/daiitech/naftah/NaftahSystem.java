package org.daiitech.naftah;

import static org.daiitech.naftah.utils.arabic.ArabicOutputTransformer.getPrintStream;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.ARABIC_LANGUAGE;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.DEFAULT_ARABIC_LANGUAGE_COUNTRY;
import static org.daiitech.naftah.utils.repl.REPLHelper.getTerminal;

import java.io.PrintStream;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.utils.OS;
import org.jline.terminal.Terminal;

/**
 * @author Chakib Daii
 */
public final class NaftahSystem {
  public static final String TERMINAL_WIDTH_PROPERTY = "naftah.terminal.width";
  public static final String TERMINAL_HEIGHT_PROPERTY = "naftah.terminal.hight";

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
      throw new NaftahBugError("تم العثور على إصدار غير متوقع: " + full);
    }
    return full.substring(0, secondDot);
  }

  public static void setupLocale() {
    Locale arabic = new Locale(ARABIC_LANGUAGE, DEFAULT_ARABIC_LANGUAGE_COUNTRY);
    Locale.setDefault(arabic);
  }

  public static void setupOutputStream() {
    PrintStream ps = getPrintStream(System.out);
    System.setOut(ps);
  }

  public static void setupErrorStream() {
    PrintStream ps = getPrintStream(System.err);
    System.setErr(ps);
  }

  public static void setupRefreshTerminalWidthAndHeight(Terminal terminal) {
    if (OS.isFamilyWindows()) {
      ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
      executor.scheduleAtFixedRate(
          () -> setupTerminalWidthAndHeight(() -> getTerminalWidthAndHeight(terminal)),
          0,
          500,
          TimeUnit.MILLISECONDS);
    } else {
      // TODO: check support for Linux systems (didn't work for a ubuntu Virtual machine)
      terminal.handle(
          Terminal.Signal.WINCH,
          signal -> setupTerminalWidthAndHeight(() -> getTerminalWidthAndHeight(terminal)));
    }
  }

  public static void setupTerminalWidthAndHeight(Supplier<int[]> terminalWidthAndHeightSupplier) {
    int[] terminalWidthAndHeight = terminalWidthAndHeightSupplier.get();
    System.setProperty(TERMINAL_WIDTH_PROPERTY, Integer.toString(terminalWidthAndHeight[0]));
    System.setProperty(TERMINAL_HEIGHT_PROPERTY, Integer.toString(terminalWidthAndHeight[1]));
  }

  public static int[] getTerminalWidthAndHeight(Terminal terminal) {
    return new int[] {terminal.getWidth(), terminal.getHeight()};
  }

  public static int[] getTerminalWidthAndHeight() {
    try (Terminal terminal = getTerminal()) {
      return getTerminalWidthAndHeight(terminal);
    } catch (Exception ignored) {
      return new int[] {80, 24}; // fallback width
    }
  }
}
