package org.daiitech.naftah.errors;

public class ExceptionUtils {
    public static Throwable getRootCause(Throwable original) {
        if (original == null) {
            return null;
        } else {
            Throwable rootCause = null;

            for(Throwable cause = original.getCause(); cause != null && cause != rootCause; cause = cause.getCause()) {
                rootCause = cause;
            }

            return rootCause;
        }
    }

    public static Throwable getMostSpecificCause(Throwable original) {
        Throwable rootCause = getRootCause(original);
        return rootCause != null ? rootCause : original;
    }
}
