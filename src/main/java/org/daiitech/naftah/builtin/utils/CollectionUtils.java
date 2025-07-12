package org.daiitech.naftah.builtin.utils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.daiitech.naftah.builtin.utils.op.BinaryOperation;
import org.daiitech.naftah.builtin.utils.op.UnaryOperation;
import org.daiitech.naftah.errors.NaftahBugError;

/**
 * @author Chakib Daii
 */
public class CollectionUtils {

  public static Object[] applyOperation(Object[] left, Object[] right, BinaryOperation operation) {
    if (left.length != right.length) throw newNaftahSizeBugError(left, right);
    return IntStream.range(0, left.length)
        .mapToObj(i -> ObjectUtils.applyOperation(left[i], right[i], operation))
        .toArray(Object[]::new);
  }

  public static Collection<?> applyOperation(
      Collection<?> left, Collection<?> right, BinaryOperation operation) {
    if (left.size() != right.size()) throw newNaftahSizeBugError(left.toArray(), right.toArray());
    var arr1 = left.toArray(Object[]::new);
    var arr2 = right.toArray(Object[]::new);
    return List.of(applyOperation(arr1, arr2, operation));
  }

  public static Object[] applyOperation(Object[] arr, Number scalar, BinaryOperation operation) {
    return Arrays.stream(arr)
        .map(o -> ObjectUtils.applyOperation(o, scalar, operation))
        .toArray(Object[]::new);
  }

  public static Collection<?> applyOperation(
      Collection<?> collection, Number scalar, BinaryOperation operation) {
    return List.of(applyOperation(collection.toArray(Object[]::new), scalar, operation));
  }

  public static Map<?, ?> applyOperation(
      Map<?, ?> left, Map<?, ?> right, BinaryOperation operation) {
    Map<Object, Object> result = new HashMap<>();

    for (var key : left.keySet()) {
      if (right.containsKey(key)) {
        var val1 = left.get(key);
        var val2 = right.get(key);
        result.put(key, ObjectUtils.applyOperation(val1, val2, operation)); // Reuse from earlier
      } else {
        throw new NaftahBugError("المفتاح '%s' غير موجود في المصفوفة الترابطية الثانية.");
      }
    }

    return result;
  }

  public static Map<?, ?> applyOperation(Map<?, ?> map, Number scalar, BinaryOperation operation) {
    Map<Object, Object> result = new HashMap<>();

    for (var entry : map.entrySet()) {
      result.put(
          entry.getKey(),
          ObjectUtils.applyOperation(entry.getValue(), scalar, operation)); // Reuse from earlier
    }

    return result;
  }

  public static Object[] applyOperation(Object[] arr, UnaryOperation operation) {
    return Arrays.stream(arr)
        .map(o -> ObjectUtils.applyOperation(o, operation))
        .toArray(Object[]::new);
  }

  public static Collection<?> applyOperation(Collection<?> collection, UnaryOperation operation) {
    var arr = collection.toArray(Object[]::new);
    return List.of(applyOperation(arr, operation));
  }

  public static Map<?, ?> applyOperation(Map<?, ?> map, UnaryOperation operation) {
    return map.entrySet().stream()
        .map(
            entry ->
                Map.entry(entry.getKey(), ObjectUtils.applyOperation(entry.getValue(), operation)))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  public static NaftahBugError newNaftahSizeBugError(Object[] left, Object[] right) {
    return new NaftahBugError(
        """
              يجب أن تكون أحجام المصفوفات متساوية.
              '%s'
              '%s'
              """
            .formatted(Arrays.toString(left), Arrays.toString(right)));
  }
}
