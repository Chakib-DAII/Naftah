package org.daiitech.naftah.utils;

import org.daiitech.naftah.errors.NaftahBugError;

import static java.lang.Thread.sleep;

import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.padText;
import static org.daiitech.naftah.utils.repl.REPLHelper.clearScreen;

/**
 * Utility class for displaying a console-based loading spinner animation.
 * <p>
 * The spinner runs on a separate thread and can be started/stopped using static methods.
 * It uses ANSI escape codes to clear the terminal screen for visual effect.
 * </p>
 *
 * <p>
 * Example usage:
 * </p>
 * <pre>{@code
 * ConsoleLoader.startLoader("Loading...");
 * // do work
 * ConsoleLoader.stopLoader();
 * }</pre>
 *
 * @author Chakib Daii
 */
public final class ConsoleLoader {
	/**
	 * Characters used to animate the spinner.
	 */
	private static final char[] SPINNER = {'|', '/', '-', '\\'};
	/**
	 * Background thread responsible for rendering the spinner animation.
	 */
	private static Thread LOADER_THREAD;

	/**
	 * Private constructor to prevent instantiation.
	 * Always throws a {@link NaftahBugError} when called.
	 */
	private ConsoleLoader() {
		throw newNaftahBugInvalidUsageError();
	}

	/**
	 * Starts the loader spinner in a separate thread.
	 *
	 * @param text the message to display alongside the spinner
	 */
	public static void startLoader(String text) {
		LOADER_THREAD = new Thread(() -> {
			int i = 0;
			int j = 0;
			while (!Thread.currentThread().isInterrupted()) {
				clearScreen();
				System.out
						.print(padText(
										String
												.format("%c %s %c",
														SPINNER[i++ % SPINNER.length],
														text,
														SPINNER[j++ % SPINNER.length]),
										false));
				try {
					//noinspection BusyWait
					sleep(150); // control speed
				}
				catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		});

		LOADER_THREAD.start();
	}

	/**
	 * Stops the loader spinner if running and clears the screen.
	 */
	public static void stopLoader() {
		if (LOADER_THREAD.isAlive()) {
			LOADER_THREAD.interrupt();
		}
		clearScreen();
	}
}
