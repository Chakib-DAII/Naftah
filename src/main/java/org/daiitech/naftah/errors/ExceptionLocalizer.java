package org.daiitech.naftah.errors;

import java.util.ResourceBundle;

import static org.daiitech.naftah.errors.ExceptionUtils.getMostSpecificCause;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.ARABIC;

/**
 * Utility class for localizing exception messages using a resource bundle.
 * <p>
 * This class provides a method to convert a {@link Throwable} into a localized
 * string message based on its exception type. It uses a resource bundle named
 * "exceptions" with the Arabic locale to retrieve localized messages.
 * </p>
 *
 * <p><b>Note:</b> This class cannot be instantiated. Attempting to instantiate
 * it will always throw a {@link NaftahBugError}.</p>
 *
 * @author Chakib Daii
 */
public final class ExceptionLocalizer {

	private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("exceptions", ARABIC);

	/**
	 * Private constructor to prevent instantiation.
	 * <p>
	 * Always throws a {@link NaftahBugError} with an Arabic message
	 * "استخدام غير مسموح به." ("Usage not allowed.") when called.
	 * </p>
	 */
	private ExceptionLocalizer() {
		throw newNaftahBugInvalidUsageError();
	}

	/**
	 * Localizes the message of the given {@link Throwable}.
	 * <p>
	 * Extracts the most specific cause of the exception, retrieves its simple
	 * class name as the key, and looks up the corresponding localized message
	 * from the resource bundle. If the key does not exist, a default message
	 * is used instead. The localized message is then formatted with the
	 * exception's localized message.
	 * </p>
	 *
	 * @param e the throwable to localize
	 * @return the localized message string
	 */
	public static String localizeException(Throwable e) {
		e = getMostSpecificCause(e);
		String key = e.getClass().getSimpleName();
		boolean specificErrorMessageFound = BUNDLE.containsKey(key);
		return String
				.format(specificErrorMessageFound ? BUNDLE.getString(key) : BUNDLE.getString("default"),
						specificErrorMessageFound ?
								e.getLocalizedMessage() :
								e.getClass().getSimpleName() + ": " + e.getLocalizedMessage());
	}
}
