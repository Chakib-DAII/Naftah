// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

package org.daiitech.naftah.utils.script;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import static org.daiitech.naftah.utils.script.ScriptUtils.containsArabicLetters;
import static org.daiitech.naftah.utils.script.ScriptUtils.shape;


/**
 * A custom log formatter that supports reshaping text for proper visual display
 * in logging output. If text is detected and reshaping is enabled, the formatter
 * will reshape the text before logging it. Otherwise, the original message is logged.
 *
 * <p>Note: Actual implementations of {@code shape()}, {@code containsArabicLetters(String)},
 * and {@code shouldReshape()} are assumed to exist elsewhere in the codebase.
 *
 * @author Chakib Daii
 */
public class NaftahLogFormatter extends Formatter {

	/**
	 * Template for the formatted log message.
	 * It includes the log level and the message itself.
	 */
	private static final String LOG_MSG = """
											[ %s ]: %s
											""";

	/**
	 * Formats a given {@link LogRecord} by checking if the message contains text.
	 * If so, and reshaping is enabled, the message will be reshaped for visual rendering.
	 * If reshaping fails or is not required, the original message is logged.
	 *
	 * @param record the log record to be formatted
	 * @return the formatted log message as a string
	 */
	@Override
	public String format(LogRecord record) {
		String msg = record.getMessage();

		if (containsArabicLetters(msg)) {
			try {
				String visual = shape(msg);
				return LOG_MSG.formatted(record.getLevel(), visual);
			}
			catch (Exception e) {
				return LOG_MSG.formatted(record.getLevel(), msg); // fallback
			}
		}
		else {
			return LOG_MSG.formatted(record.getLevel(), msg); // fallback
		}
	}
}
