package org.daiitech.naftah.errors;

import java.util.Locale;
import java.util.ResourceBundle;

import static org.daiitech.naftah.errors.ExceptionUtils.getMostSpecificCause;

public final class ExceptionLocalizer {
    private static final Locale ARABIC = new Locale("ar");
    private static final ResourceBundle bundle = ResourceBundle.getBundle("exceptions", ARABIC);

    public static String localizeException(Throwable e) {
        e = getMostSpecificCause(e);
        String key = e.getClass().getSimpleName();
        return String.format(bundle.containsKey(key)
                ? bundle.getString(key)
                : bundle.getString("default"),
                e.getLocalizedMessage()) ;
    }
}
