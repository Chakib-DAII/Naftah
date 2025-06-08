package org.daiitech.naftah.core.builtin.utils;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

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
      // TODO: enhance to support all kind of numbers (using DynamicNumber)
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

  public static Object not(Object value) {
    try {
      // arithmetic negation
      return NumberUtils.negate(value);
    } catch (Exception e) {
      // logical negation
      return !isTruthy(value);
    }
  }

  /**
   * Determine whether the given object is empty.
   *
   * <p>This method supports the following object types.
   *
   * <ul>
   *   <li>{@code Optional}: considered empty if not {@link Optional#isPresent()}
   *   <li>{@code Array}: considered empty if its length is zero
   *   <li>{@link CharSequence}: considered empty if its length is zero
   *   <li>{@link Collection}: delegates to {@link Collection#isEmpty()}
   *   <li>{@link Map}: delegates to {@link Map#isEmpty()}
   * </ul>
   *
   * <p>If the given object is non-null and not one of the aforementioned supported types, this
   * method returns {@code false}.
   *
   * @param obj the object to check
   * @return {@code true} if the object is {@code null} or <em>empty</em>
   */
  public static boolean isEmpty(Object obj) {
    if (obj == null) {
      return true;
    }

    if (obj instanceof Optional<?> optional) {
      return optional.isEmpty();
    }
    if (obj instanceof CharSequence charSequence) {
      return charSequence.isEmpty();
    }
    if (obj.getClass().isArray()) {
      return Array.getLength(obj) == 0;
    }
    if (obj instanceof Collection<?> collection) {
      return collection.isEmpty();
    }
    if (obj instanceof Map<?, ?> map) {
      return map.isEmpty();
    }

    // else
    return false;
  }
}
