// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

package org.daiitech.naftah.playground.utils.logging;

/**
 * Defines the logging levels supported by the playground logging system.
 *
 * <p>Levels are ordered by severity, from {@link #ERROR} (highest severity)
 * to {@link #TRACE} (most verbose). Each level is associated with a numeric
 * rank used to determine whether a log message should be emitted.</p>
 *
 * <p>For example, a logger configured with {@link #INFO} will allow
 * {@link #ERROR}, {@link #WARN}, and {@link #INFO} messages, but will
 * suppress {@link #DEBUG} and {@link #TRACE} messages.</p>
 *
 * @author Chakib Daii
 */
public enum LogLevel {

	/**
	 * Logs only error conditions that prevent normal operation.
	 */
	ERROR(0),

	/**
	 * Logs warning conditions that may indicate a problem but do not
	 * prevent execution.
	 */
	WARN(1),

	/**
	 * Logs general informational messages about application behavior.
	 */
	INFO(2),

	/**
	 * Logs detailed diagnostic information useful during development
	 * and troubleshooting.
	 */
	DEBUG(3),

	/**
	 * Logs the most detailed execution information available.
	 */
	TRACE(4);

	/**
	 * Internal rank used to compare logging levels.
	 */
	final int rank;

	/**
	 * Creates a logging level with the specified rank.
	 *
	 * @param rank the numeric rank associated with the level
	 */
	LogLevel(int rank) {
		this.rank = rank;
	}

	/**
	 * Determines whether this logging level permits messages of the
	 * specified level.
	 *
	 * <p>A level allows itself and all levels with higher severity.</p>
	 *
	 * @param other the message level to evaluate
	 * @return {@code true} if messages at {@code other} level should be
	 *         logged; {@code false} otherwise
	 */
	public boolean allows(LogLevel other) {
		return other.rank <= this.rank;
	}

	/**
	 * Converts a string representation into a {@code LogLevel}.
	 *
	 * <p>The comparison is case-insensitive. If the supplied value is
	 * {@code null}, empty, or does not correspond to a valid logging
	 * level, {@link #INFO} is returned.</p>
	 *
	 * @param s the string representation of the log level
	 * @return the corresponding {@code LogLevel}, or {@link #INFO} if
	 *         the input is invalid
	 */
	public static LogLevel fromString(String s) {
		if (s == null) return INFO;
		try {
			return LogLevel.valueOf(s.toUpperCase());
		}
		catch (Exception e) {
			return INFO;
		}
	}
}
