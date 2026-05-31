// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

package org.daiitech.naftah.playground.utils.logging;

import org.daiitech.naftah.errors.NaftahBugError;

import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;

/**
 * Factory and configuration holder for playground loggers.
 *
 * <p>This class maintains the global logging level used when creating new
 * {@link Logger} instances. All loggers obtained through this manager are
 * initialized with the current global level.</p>
 *
 * <p>Changing the global level affects only loggers created after the
 * change. Existing logger instances retain the level that was assigned
 * when they were created unless explicitly updated.</p>
 *
 * @author Chakib Daii
 */
public class LogManager {

	/**
	 * Private constructor to prevent instantiation.
	 * Throws {@link NaftahBugError} if called.
	 */
	private LogManager() {
		throw newNaftahBugInvalidUsageError();
	}

	/**
	 * Global logging level used for newly created loggers.
	 *
	 * <p>Defaults to {@link LogLevel#INFO}.</p>
	 */
	private static LogLevel GLOBAL_LEVEL = LogLevel.INFO;

	/**
	 * Updates the global logging level.
	 *
	 * <p>The supplied value is parsed using
	 * {@link LogLevel#fromString(String)}. Invalid or {@code null} values
	 * result in the default level ({@link LogLevel#INFO}) being used.</p>
	 *
	 * @param level the logging level name
	 */
	public static void setLevel(String level) {
		GLOBAL_LEVEL = LogLevel.fromString(level);
	}

	/**
	 * Creates a new logger with the current global logging level.
	 *
	 * @param name the logger name
	 * @return a new logger instance
	 */
	public static Logger getLogger(String name) {
		return new Logger(name, GLOBAL_LEVEL);
	}
}
