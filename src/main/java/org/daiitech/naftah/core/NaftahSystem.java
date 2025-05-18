package org.daiitech.naftah.core;

import org.daiitech.naftah.core.errors.NaftahBugError;
import org.daiitech.naftah.utils.ReleaseInfo;

/**
 * @author Chakib Daii
 **/
public final class NaftahSystem {

    private NaftahSystem() {
    }


    /**
     * Returns the groovy version
     */
    public static String getVersion() {
        return ReleaseInfo.getVersion();
    }

    /**
     * Returns the major and minor part of the naftah version excluding the point/patch part of the version.
     * E.g. 1.0.0, 1.0.0-SNAPSHOT, 1.0.0-rc-1 all have 1.0 as the short version.
     *
     * @since 3.0.1
     */
    public static String getShortVersion() {
        String full = getVersion();
        int firstDot = full.indexOf('.');
        int secondDot = full.indexOf('.', firstDot + 1);
        if (secondDot < 0) {
            throw new NaftahBugError("Unexpected version found: " + full);
        }
        return full.substring(0, secondDot);
    }
}
