package org.daiitech.naftah.builtin.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.daiitech.naftah.builtin.utils.op.BinaryOperation;
import org.daiitech.naftah.builtin.utils.op.UnaryOperation;
import org.daiitech.naftah.errors.NaftahBugError;

import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahKeyNotFoundError;

/**
 * Utility class for applying binary and unary operations on collections, arrays, and maps.
 * <p>
 * This class cannot be instantiated.
 * </p>
 *
 * @author Chakib Daii
 */
public final class CollectionUtils {

	/**
	 * Private constructor to prevent instantiation.
	 * Always throws a {@link NaftahBugError} when called.
	 */
	private CollectionUtils() {
		throw newNaftahBugInvalidUsageError();
	}

	/**
	 * Applies a binary operation element-wise to two arrays of objects.
	 *
	 * @param left      the first array
	 * @param right     the second array
	 * @param operation the binary operation to apply
	 * @return a new array containing the results of the operation
	 * @throws NaftahBugError if arrays have different lengths
	 */
	public static Object[] applyOperation(Object[] left, Object[] right, BinaryOperation operation) {
		if (left.length != right.length) {
			throw newNaftahSizeBugError(left, right);
		}
		return IntStream
				.range(0, left.length)
				.mapToObj(i -> ObjectUtils.applyOperation(left[i], right[i], operation))
				.toArray(Object[]::new);
	}

	/**
	 * Applies a binary operation element-wise to two collections.
	 *
	 * @param left      the first collection
	 * @param right     the second collection
	 * @param operation the binary operation to apply
	 * @return a new collection containing the results of the operation
	 * @throws NaftahBugError if collections have different sizes
	 */
	public static Collection<?> applyOperation(Collection<?> left, Collection<?> right, BinaryOperation operation) {
		if (left.size() != right.size()) {
			throw newNaftahSizeBugError(left.toArray(), right.toArray());
		}
		var arr1 = left.toArray(Object[]::new);
		var arr2 = right.toArray(Object[]::new);
		return List.of(applyOperation(arr1, arr2, operation));
	}

	/**
	 * Applies a binary operation between each element of an array and a scalar number.
	 *
	 * @param arr           the array of objects
	 * @param scalar        the scalar number
	 * @param isLeftOperand is the array left operand
	 * @param operation     the binary operation to apply
	 * @return a new array containing the results of the operation
	 */
	public static Object[] applyOperation(  Object[] arr,
											Number scalar,
											boolean isLeftOperand,
											BinaryOperation operation) {
		return Arrays
				.stream(arr)
				.map(o -> isLeftOperand ?
						ObjectUtils.applyOperation(o, scalar, operation) :
						ObjectUtils.applyOperation(scalar, o, operation))
				.toArray(Object[]::new);
	}

	/**
	 * Applies a binary operation between each element of a collection and a scalar number.
	 *
	 * @param collection    the collection of objects
	 * @param scalar        the scalar number
	 * @param isLeftOperand is the collection left operand
	 * @param operation     the binary operation to apply
	 * @return a new collection containing the results of the operation
	 */
	public static Collection<?> applyOperation( Collection<?> collection,
												Number scalar,
												boolean isLeftOperand,
												BinaryOperation operation) {
		return List.of(applyOperation(collection.toArray(Object[]::new), scalar, isLeftOperand, operation));
	}

	/**
	 * Applies a binary operation element-wise to two maps.
	 * The keys of the first map must exist in the second map.
	 *
	 * @param left      the first map
	 * @param right     the second map
	 * @param operation the binary operation to apply
	 * @return a new map containing the results of the operation
	 * @throws NaftahBugError if a key from the first map is missing in the second map
	 */
	public static Map<?, ?> applyOperation(Map<?, ?> left, Map<?, ?> right, BinaryOperation operation) {
		if (left.size() != right.size()) {
			throw newNaftahSizeBugError(left, right);
		}

		Map<Object, Object> result = new HashMap<>();

		for (var key : left.keySet()) {
			if (right.containsKey(key)) {
				var val1 = left.get(key);
				var val2 = right.get(key);
				result.put(key, ObjectUtils.applyOperation(val1, val2, operation)); // Reuse from earlier
			}
			else {
				throw newNaftahKeyNotFoundError(key);
			}
		}

		return result;
	}

	/**
	 * Applies a binary operation between each value in a map and a scalar number.
	 *
	 * @param map           the map of key-value pairs
	 * @param scalar        the scalar number
	 * @param isLeftOperand is the map left operand
	 * @param operation     the binary operation to apply
	 * @return a new map containing the results of the operation
	 */
	public static Map<?, ?> applyOperation( Map<?, ?> map,
											Number scalar,
											boolean isLeftOperand,
											BinaryOperation operation) {
		Map<Object, Object> result = new HashMap<>();

		for (var entry : map.entrySet()) {
			result
					.put(   entry.getKey(),
							isLeftOperand ?
									ObjectUtils.applyOperation(entry.getValue(), scalar, operation) :
									ObjectUtils
											.applyOperation(scalar,
															entry.getValue(),
															operation)); // Reuse from earlier
		}

		return result;
	}

	/**
	 * Applies a unary operation to each element in an array.
	 *
	 * @param arr       the array of objects
	 * @param operation the unary operation to apply
	 * @return a new array containing the results of the operation
	 */
	public static Object[] applyOperation(Object[] arr, UnaryOperation operation) {
		return Arrays.stream(arr).map(o -> ObjectUtils.applyOperation(o, operation)).toArray(Object[]::new);
	}

	/**
	 * Applies a unary operation to each element in a collection.
	 *
	 * @param collection the collection of objects
	 * @param operation  the unary operation to apply
	 * @return a new collection containing the results of the operation
	 */
	public static Collection<?> applyOperation(Collection<?> collection, UnaryOperation operation) {
		var arr = collection.toArray(Object[]::new);
		return List.of(applyOperation(arr, operation));
	}

	/**
	 * Applies a unary operation to each value in a map.
	 *
	 * @param map       the map of key-value pairs
	 * @param operation the unary operation to apply
	 * @return a new map containing the results of the operation
	 */
	public static Map<?, ?> applyOperation(Map<?, ?> map, UnaryOperation operation) {
		return map
				.entrySet()
				.stream()
				.map(entry -> Map.entry(entry.getKey(), ObjectUtils.applyOperation(entry.getValue(), operation)))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	/**
	 * Constructs a new {@link NaftahBugError} indicating that the sizes of the two arrays do not match.
	 *
	 * @param left  the first array
	 * @param right the second array
	 * @return a new NaftahBugError with a descriptive message in Arabic
	 */
	public static NaftahBugError newNaftahSizeBugError(Object[] left, Object[] right) {
		return new NaftahBugError("""
									يجب أن تكون أحجام المصفوفات متساوية.
									'%s'
									'%s'
									""".formatted(Arrays.toString(left), Arrays.toString(right)));
	}

	/**
	 * Constructs a new {@link NaftahBugError} indicating that the sizes of the two associative arrays (maps) do not
	 * match.
	 *
	 * @param left  the first associative array (map)
	 * @param right the second associative array (map)
	 * @return a new NaftahBugError with a descriptive message in Arabic showing both maps
	 */
	public static NaftahBugError newNaftahSizeBugError(Map<?, ?> left, Map<?, ?> right) {
		return new NaftahBugError("""
									يجب أن تكون أحجام المصفوفات الترابطية متساوية.
									'%s'
									'%s'
									""".formatted(left, right));
	}
}
