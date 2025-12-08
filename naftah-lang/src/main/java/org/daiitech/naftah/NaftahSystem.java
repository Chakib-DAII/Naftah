package org.daiitech.naftah;

import java.io.PrintStream;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.utils.OS;
import org.jline.terminal.Terminal;

import static org.daiitech.naftah.utils.arabic.ArabicOutputTransformer.getPrintStream;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.ARABIC_LANGUAGE;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.DEFAULT_ARABIC_LANGUAGE_COUNTRY;
import static org.daiitech.naftah.utils.repl.REPLHelper.getTerminal;

/**
 * Utility class for managing system-wide settings, especially for terminal configuration
 * and versioning in the Naftah runtime environment.
 * <p>
 * This class is non-instantiable and only provides static utility methods and constants.
 * <p>
 * Common usages include:
 * <ul>
 * <li>Setting up terminal output/error streams</li>
 * <li>Retrieving Naftah version information</li>
 * <li>Setting the system locale to Arabic</li>
 * <li>Auto-refreshing terminal dimensions</li>
 * </ul>
 *
 * @author Chakib Daii
 */
public final class NaftahSystem {

	/**
	 * System property key used to store terminal width.
	 */
	public static final String TERMINAL_WIDTH_PROPERTY = "naftah.terminal.width";

	/**
	 * System property key used to store terminal height.
	 */
	public static final String TERMINAL_HEIGHT_PROPERTY = "naftah.terminal.hight";

	/**
	 * Private constructor to prevent instantiation.
	 */
	private NaftahSystem() {
	}

	/**
	 * Returns the current full version of Naftah.
	 *
	 * @return the full version string, e.g., {@code "1.2.3"} or {@code "1.2.3-SNAPSHOT"}
	 */
	public static String getVersion() {
		return ReleaseInfo.getVersion();
	}

	/**
	 * Returns the short form of the version string, containing only the
	 * major and minor parts.
	 * <p>
	 * For example:
	 * <ul>
	 * <li>{@code "1.2.3"} → {@code "1.2"}</li>
	 * <li>{@code "2.0.1-SNAPSHOT"} → {@code "2.0"}</li>
	 * </ul>
	 *
	 * @return the short version string
	 * @throws NaftahBugError if the version format is invalid or unexpected
	 *
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

	/**
	 * Sets the default {@link Locale} to Arabic using predefined constants
	 * for language and region.
	 */
	public static void setupLocale() {
		Locale arabic = new Locale(ARABIC_LANGUAGE, DEFAULT_ARABIC_LANGUAGE_COUNTRY);
		Locale.setDefault(arabic);
	}

	/**
	 * Redirects {@code System.out} to a custom {@link PrintStream} instance
	 * to support UTF-8 or enhanced terminal features.
	 */
	public static void setupOutputStream() {
		PrintStream ps = getPrintStream(System.out);
		System.setOut(ps);
	}

	/**
	 * Redirects {@code System.err} to a custom {@link PrintStream} instance.
	 */
	public static void setupErrorStream() {
		PrintStream ps = getPrintStream(System.err);
		System.setErr(ps);
	}

	/**
	 * Sets up automatic refresh of terminal width and height properties.
	 * <p>
	 * On Windows, uses a scheduled task to periodically refresh.
	 * On other systems, uses the {@link Terminal.Signal#WINCH} signal handler.
	 *
	 * @param terminal the terminal instance to monitor
	 */
	public static void setupRefreshTerminalWidthAndHeight(Terminal terminal) {
		if (OS.isFamilyWindows()) {
			ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
			executor
					.scheduleAtFixedRate(   () -> setupTerminalWidthAndHeight(() -> getTerminalWidthAndHeight(terminal)),
											0,
											500,
											TimeUnit.MILLISECONDS);
		}
		else {
			AtomicBoolean winchTriggered = new AtomicBoolean(false);
			int[] lastSize = {-1, -1};

			terminal
					.handle(Terminal.Signal.WINCH,
							signal -> {
								winchTriggered.set(true);  // mark signal was received
								setupTerminalWidthAndHeight(() -> getTerminalWidthAndHeight(terminal));
							});

			// Poll terminal size every 1s to check for manual window resize
			ScheduledExecutorService resizeChecker = Executors.newSingleThreadScheduledExecutor();

			resizeChecker.scheduleAtFixedRate(() -> {
				int[] currentSize = getTerminalWidthAndHeight(terminal);
				boolean sizeChanged = currentSize[0] != lastSize[0] || currentSize[1] != lastSize[1];

				if (sizeChanged && !winchTriggered.get()) {
					// WINCH didn't fire, but terminal resized — use fallback executor
					ScheduledExecutorService fallbackExecutor = Executors.newSingleThreadScheduledExecutor();
					fallbackExecutor
							.scheduleAtFixedRate(
													() -> setupTerminalWidthAndHeight(() -> getTerminalWidthAndHeight(terminal)),
													0,
													500,
													TimeUnit.MILLISECONDS
							);
					resizeChecker.shutdown();  // Stop monitoring
				}
				else if (winchTriggered.get()) {
					resizeChecker.shutdown();  // Stop monitoring
				}

				lastSize[0] = currentSize[0];
				lastSize[1] = currentSize[1];

			}, 1, 1, TimeUnit.SECONDS);
		}
	}

	/**
	 * Updates the system properties {@link #TERMINAL_WIDTH_PROPERTY} and
	 * {@link #TERMINAL_HEIGHT_PROPERTY} using the values provided by the supplier.
	 *
	 * @param terminalWidthAndHeightSupplier a supplier providing an array of 2 integers: width and height
	 */
	public static void setupTerminalWidthAndHeight(Supplier<int[]> terminalWidthAndHeightSupplier) {
		int[] terminalWidthAndHeight = terminalWidthAndHeightSupplier.get();
		System.setProperty(TERMINAL_WIDTH_PROPERTY, Integer.toString(terminalWidthAndHeight[0]));
		System.setProperty(TERMINAL_HEIGHT_PROPERTY, Integer.toString(terminalWidthAndHeight[1]));
	}

	/**
	 * Returns the terminal dimensions from the given {@link Terminal} instance.
	 *
	 * @param terminal the terminal object to query
	 * @return an array with two elements: [width, height]
	 */
	public static int[] getTerminalWidthAndHeight(Terminal terminal) {
		return new int[]{terminal.getWidth(), terminal.getHeight()};
	}

	/**
	 * Returns the terminal dimensions using a temporary {@link Terminal} instance.
	 * <p>
	 * If an exception occurs during terminal initialization, returns default fallback dimensions (80x24).
	 *
	 * @return an array with two elements: [width, height]
	 */
	public static int[] getTerminalWidthAndHeight() {
		try (Terminal terminal = getTerminal()) {
			return getTerminalWidthAndHeight(terminal);
		}
		catch (Exception ignored) {
			return new int[]{80, 24}; // fallback width
		}
	}
}
