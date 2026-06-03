// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

package org.daiitech.naftah.playground.utils.logging;

import org.daiitech.naftah.playground.NaftahPlayground;

/**
 * Simple logger implementation used by the Naftah Playground.
 *
 * <p>This logger provides basic level-based logging and forwards log
 * messages to the browser-hosted logging console through the
 * {@link NaftahPlayground} bridge.</p>
 *
 * <p>Messages are emitted only when the configured logger level permits
 * the requested message level. Log output follows the format:</p>
 *
 * <pre>
 * [LEVEL] LoggerName - Message
 * </pre>
 *
 * <p>This class is intended for lightweight runtime diagnostics within
 * browser-hosted environments and is not designed as a replacement for
 * full-featured logging frameworks.</p>
 *
 * @author Chakib Daii
 */
public class Logger {

	/**
	 * Name of the logger, included in every log message.
	 */
	private final String name;

	/**
	 * Current logging level.
	 */
	private LogLevel level;

	/**
	 * Creates a logger with the specified name and logging level.
	 *
	 * @param name  the logger name
	 * @param level the minimum logging level to allow
	 */
	public Logger(String name, LogLevel level) {
		this.name = name;
		this.level = level;
	}

	/**
	 * Updates the logger's active logging level.
	 *
	 * @param level the new logging level
	 */
	public void setLevel(LogLevel level) {
		this.level = level;
	}

	/**
	 * Logs an error message.
	 *
	 * @param msg the message to log
	 */
	public void error(String msg) {
		log(LogLevel.ERROR, msg);
	}

	/**
	 * Logs a warning message.
	 *
	 * @param msg the message to log
	 */
	public void warn(String msg) {
		log(LogLevel.WARN, msg);
	}

	/**
	 * Logs an informational message.
	 *
	 * @param msg the message to log
	 */
	public void info(String msg) {
		log(LogLevel.INFO, msg);
	}

	/**
	 * Logs a debug message.
	 *
	 * @param msg the message to log
	 */
	public void debug(String msg) {
		log(LogLevel.DEBUG, msg);
	}

	/**
	 * Logs a trace message.
	 *
	 * @param msg the message to log
	 */
	public void trace(String msg) {
		log(LogLevel.TRACE, msg);
	}

	/**
	 * Logs a message at the specified level.
	 *
	 * <p>If the current logger configuration allows the supplied level,
	 * the message is forwarded to the browser logging bridge. Any errors
	 * occurring during log delivery are silently ignored to prevent
	 * logging failures from affecting application execution.</p>
	 *
	 * @param msgLevel the level of the message being logged
	 * @param msg      the message text
	 */
	private void log(LogLevel msgLevel, String msg) {
		if (this.level.allows(msgLevel)) {
			try {
				NaftahPlayground
						.sendToLog(
									"[" + msgLevel + "] " + name + " - " + msg
						);
			}
			catch (Throwable ignored) {
			}
		}
	}
}
