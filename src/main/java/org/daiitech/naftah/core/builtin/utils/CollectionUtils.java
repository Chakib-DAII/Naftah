package org.daiitech.naftah.core.builtin.utils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Chakib Daii
 */
public class CollectionUtils {

    public static Collection<Number> multiply(Number[] arr1, Number[] arr2) {
        if (arr1.length != arr2.length)
            throw new IllegalArgumentException("arrays size must be equal.");
        return IntStream.range(0, arr1.length)
                .mapToObj(i -> NumberUtils.multiply(arr1[i], arr2[i]))
                .collect(Collectors.toList());
    }

    public static Collection<Number> multiply(Collection<Number> collection1, Collection<Number> collection2) {
            if (collection1.size() != collection2.size())
                throw new IllegalArgumentException("Collections size must be equal.");
            var arr1 = collection1.toArray(Number[]::new);
            var arr2 = collection2.toArray(Number[]::new);
            return multiply(arr1, arr2);
    }
    public static Collection<Number> multiply(Number[] arr, Number scalar) {
        return Arrays.stream(arr).map(number -> NumberUtils.multiply(number, scalar))
                .collect(Collectors.toList());
    }

    public static Collection<Number> multiply(Collection<Number> collection, Number scalar) {
        return multiply(collection.toArray(Number[]::new), scalar);
    }

    public static Map<Object, Number> multiply(Map<Object, Number> map1, Map<Object, Number> map2) {
        Map<Object, Number> result = new HashMap<>();

        for (Object key : map1.keySet()) {
            if (map2.containsKey(key)) {
                Number val1 = map1.get(key);
                Number val2 = map2.get(key);
                result.put(key, NumberUtils.multiply(val1, val2));  // Reuse from earlier
            } else {
                throw new IllegalArgumentException("Key " + key + " not found in second map");
            }
        }

        return result;
    }

    public static Map<Object, Number> multiply(Map<Object, Number> map, Number scalar) {
        Map<Object, Number> result = new HashMap<>();

        for (Map.Entry<Object, Number> entry : map.entrySet()) {
            result.put(entry.getKey(),  NumberUtils.multiply(entry.getValue(), scalar));  // Reuse from earlier
        }

        return result;
    }

}
