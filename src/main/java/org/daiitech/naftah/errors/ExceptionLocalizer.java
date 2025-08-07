package org.daiitech.naftah.errors;

import java.util.ResourceBundle;

import static org.daiitech.naftah.errors.ExceptionUtils.getMostSpecificCause;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.ARABIC;

public final class ExceptionLocalizer {
	private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("exceptions", ARABIC);

	public static String localizeException(Throwable e) {
		e = getMostSpecificCause(e);
		String key = e.getClass().getSimpleName();
		return String.format(BUNDLE.containsKey(key) ? BUNDLE.getString(key) : BUNDLE.getString("default"), e.getLocalizedMessage());
	}
}
