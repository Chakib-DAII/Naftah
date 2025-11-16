package org.daiitech.naftah.utils.arabic;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import static org.daiitech.naftah.utils.arabic.ArabicUtils.containsArabic;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.shape;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.shouldReshape;


/**
 * A custom log formatter that supports reshaping Arabic text for proper visual display
 * in logging output. If Arabic text is detected and reshaping is enabled, the formatter
 * will reshape the text before logging it. Otherwise, the original message is logged.
 *
 * <p>Note: Actual implementations of {@code shape()}, {@code containsArabic(String)},
 * and {@code shouldReshape()} are assumed to exist elsewhere in the codebase.
 *
 * @author Chakib Daii
 */
public class ArabicLogFormatter extends Formatter {

	/**
	 * Template for the formatted log message.
	 * It includes the log level and the message itself.
	 */
	private static final String LOG_MSG = """
											[ %s ]: %s
											""";

	/**
	 * Formats a given {@link LogRecord} by checking if the message contains Arabic text.
	 * If so, and reshaping is enabled, the message will be reshaped for visual rendering.
	 * If reshaping fails or is not required, the original message is logged.
	 *
	 * @param record the log record to be formatted
	 * @return the formatted log message as a string
	 */
	@Override
	public String format(LogRecord record) {
		String msg = record.getMessage();

		if (shouldReshape() && containsArabic(msg)) {
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
