package org.daiitech.naftah.errors;

import org.antlr.v4.runtime.Parser;

import static org.daiitech.naftah.parser.NaftahParserHelper.getFormattedTokenSymbols;

/**
 * Utility class for exception-related helper methods.
 * <p>
 * This class provides static methods to retrieve the root cause or the most specific cause
 * of an exception, as well as a factory method to create a custom {@link NaftahBugError}
 * related to invalid loop labels.
 * </p>
 *
 * <p><b>Note:</b> This class cannot be instantiated. Attempting to instantiate
 * it will always throw a {@link NaftahBugError}.</p>
 *
 * @author Chakib Daii
 */
public final class ExceptionUtils {

	/**
	 * Private constructor to prevent instantiation.
	 * Always throws a {@link NaftahBugError} when called.
	 */
	private ExceptionUtils() {
		throw new NaftahBugError("استخدام غير مسموح به.");
	}

	/**
	 * Returns the root cause of the given {@link Throwable}.
	 * <p>
	 * Traverses the causal chain to find the deepest cause of the throwable.
	 * If the original throwable is {@code null}, returns {@code null}.
	 * </p>
	 *
	 * @param original the throwable to inspect
	 * @return the root cause throwable, or {@code null} if none found or if {@code original} is {@code null}
	 */
	public static Throwable getRootCause(Throwable original) {
		if (original == null) {
			return null;
		}
		else {
			Throwable rootCause = null;

			for (Throwable cause = original.getCause(); cause != null && cause != rootCause; cause = cause.getCause()) {
				rootCause = cause;
			}

			return rootCause;
		}
	}

	/**
	 * Returns the most specific cause of the given {@link Throwable}.
	 * <p>
	 * This method returns the deepest cause (root cause) if available,
	 * otherwise returns the original throwable.
	 * </p>
	 *
	 * @param original the throwable to inspect
	 * @return the most specific cause (root cause if present) or the original throwable
	 */
	public static Throwable getMostSpecificCause(Throwable original) {
		Throwable rootCause = getRootCause(original);
		return rootCause != null ? rootCause : original;
	}

	/**
	 * Creates a new {@link NaftahBugError} indicating an invalid reuse of a loop label.
	 * <p>
	 * Constructs an error message in Arabic describing that the same loop label cannot be
	 * used in the specified statement.
	 * </p>
	 *
	 * @param label  the loop label name that was reused
	 * @param parser the parser instance used to obtain vocabulary information
	 * @return a new {@link NaftahBugError} with a localized error message
	 */
	public static NaftahBugError newNaftahBugInvalidLoopLabelError(String label, Parser parser) {
		return new NaftahBugError(String.format("لا يمكن استخدام تسمية الحلقة نفسها '%s' في جملة '%s'.", label, getFormattedTokenSymbols(parser.getVocabulary(), org.daiitech.naftah.parser.NaftahLexer.BREAK, false)));
	}
}
