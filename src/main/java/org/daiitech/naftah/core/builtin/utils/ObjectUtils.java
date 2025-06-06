package org.daiitech.naftah.core.builtin.utils;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

/**
 * @author Chakib Daii
 */
public final class ObjectUtils {

    public static boolean isTruthy(Object obj) {
        if (obj == null) return false;

        // Boolean
        if (obj instanceof Boolean bool) return bool;

        // Number (includes Integer, Double, etc.)
        if (obj instanceof Number num) {
            double value = num.doubleValue();
            return value != 0.0 && !Double.isNaN(value);
        }

        // String
        if (obj instanceof String str) return !str.isEmpty();

        // Array
        if (obj.getClass().isArray()) return Array.getLength(obj) > 0;

        // Collection (e.g., List, Set)
        if (obj instanceof Collection<?> collection) return !collection.isEmpty();

        // Map (e.g., HashMap)
        if (obj instanceof Map<?, ?> map) return !map.isEmpty();

        // Other objects (non-null) are truthy
        return true;
    }
}
