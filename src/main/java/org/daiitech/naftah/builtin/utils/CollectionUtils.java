package org.daiitech.naftah.builtin.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.daiitech.naftah.builtin.lang.None;
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
	 * Retrieves the element at the specified index from a {@link Collection}.
	 * <p>
	 * Since {@code Collection} does not support direct index-based access, this method
	 * iterates through the elements in the order defined by the collection's iterator.
	 * </p>
	 * <p>
	 * If the index is out of bounds (i.e., {@code targetIndex >= collection.size()}),
	 * a {@code NaftahBugError} is thrown with a detailed Arabic error message.
	 * </p>
	 *
	 * @param collection  the collection to retrieve the element from
	 * @param targetIndex the zero-based index of the desired element
	 * @return the element at the specified index
	 * @throws NaftahBugError if the index is greater than or equal to the collection size
	 */
	public static Object getElementAt(Collection<?> collection, int targetIndex) {
		if (collection.size() <= targetIndex) {
			throw newNaftahIndexOutOfBoundsBugError(targetIndex, collection.size());
		}

		Iterator<?> iterator = collection.iterator();

		int currentIndex = 0;
		Object result = None.get();

		while (iterator.hasNext()) {
			Object item = iterator.next();
			if (currentIndex == targetIndex) {
				result = item;
				break;
			}
			currentIndex++;
		}

		return result;
	}

	/**
	 * Replaces the element at the specified index in a {@link Collection} with a new value.
	 * <p>
	 * This method iterates through the collection using an {@link Iterator},
	 * and rebuilds the collection with the replacement applied. It preserves
	 * insertion order for collections like {@link LinkedHashSet} and {@link List}.
	 * </p>
	 *
	 * @param collection  the collection to modify
	 * @param targetIndex the zero-based index to replace
	 * @param newValue    the new value to insert at the given index
	 * @param <T>         the element type of the collection
	 * @throws IndexOutOfBoundsException     if the target index is out of bounds
	 * @throws UnsupportedOperationException if the collection cannot be cleared or modified
	 */
	public static <T> void setElementAt(Collection<T> collection, int targetIndex, T newValue) {
		if (collection.size() <= targetIndex) {
			throw newNaftahIndexOutOfBoundsBugError(targetIndex, collection.size());
		}

		Iterator<T> iterator = collection.iterator();
		Collection<T> updated = createCompatibleCollection(collection);

		int currentIndex = 0;
		while (iterator.hasNext()) {
			T element = iterator.next();
			if (currentIndex == targetIndex) {
				updated.add(newValue);
			}
			else {
				updated.add(element);
			}
			currentIndex++;
		}

		collection.clear();
		collection.addAll(updated);
	}

	/**
	 * Creates a new, empty collection that is compatible with the given original collection,
	 * in order to preserve its iteration order and general behavior.
	 * <p>
	 * This is used internally to rebuild collections (e.g., when modifying an element by index),
	 * while maintaining the same ordering semantics:
	 * </p>
	 * <ul>
	 * <li>If the original is a {@link LinkedHashSet}, returns a new {@code LinkedHashSet}.</li>
	 * <li>If the original is a {@link List}, returns a new {@code ArrayList}.</li>
	 * <li>If the original is a {@link Set}, returns a new {@code HashSet}.</li>
	 * <li>Otherwise, defaults to a new {@code ArrayList} as a general-purpose fallback.</li>
	 * </ul>
	 *
	 * @param original the original collection to match
	 * @param <T>      the element type of the collection
	 * @return a new empty collection with behavior compatible to the original
	 */
	private static <T> Collection<T> createCompatibleCollection(Collection<T> original) {
		if (original instanceof LinkedHashSet<T>) {
			return new LinkedHashSet<>();
		}
		if (original instanceof List) {
			return new ArrayList<>();
		}
		if (original instanceof Set<T>) {
			return new HashSet<>();
		}
		return new ArrayList<>();
	}

	/**
	 * Recursively verifies that all elements in the given collection match the provided predicate.
	 * <p>
	 * Nested structures (collections, maps, arrays) are traversed and evaluated recursively.
	 * </p>
	 *
	 * @param collection the collection to evaluate
	 * @param predicate  the predicate to test each element against
	 * @param <T>        the expected element type
	 * @return {@code true} if all elements (and nested elements) match the predicate; {@code false} otherwise
	 */
	public static <T> boolean allMatch(Collection<?> collection, Predicate<T> predicate) {
		for (Object element : collection) {
			if (!FunctionUtils.allMatch(element, predicate)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Recursively verifies that all elements in the given array match the provided predicate.
	 * <p>
	 * Nested structures (collections, maps, arrays) are traversed and evaluated recursively.
	 * </p>
	 *
	 * @param array     the array to evaluate
	 * @param predicate the predicate to test each element against
	 * @param <T>       the expected element type
	 * @return {@code true} if all elements (and nested elements) match the predicate; {@code false} otherwise
	 */
	public static <T> boolean allMatch(Object[] array, Predicate<T> predicate) {
		for (Object element : array) {
			if (!FunctionUtils.allMatch(element, predicate)) {
				return false;
			}
		}
		return true;
	}


	/**
	 * Recursively verifies that all values in the given map match the provided predicate.
	 * <p>
	 * Nested structures (collections, maps, arrays) are traversed and evaluated recursively.
	 * </p>
	 *
	 * @param map       the map whose values to evaluate
	 * @param predicate the predicate to test each value against
	 * @param <T>       the expected element type
	 * @return {@code true} if all values (and nested values) match the predicate; {@code false} otherwise
	 */
	public static <T> boolean allMatch(Map<?, ?> map, Predicate<T> predicate) {
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			if (!FunctionUtils.allMatch(entry.getValue(), predicate)) {
				return false;
			}
		}
		return true;
	}


	/**
	 * Reduces the elements of the given collection into a single value
	 * using the specified combining operator.
	 * <p>
	 * The reduction is performed in iteration order, starting with the first element as the initial value.
	 * </p>
	 *
	 * @param collection the collection to reduce
	 * @param combiner   the operator used to combine two elements into one
	 * @return the result of the reduction, or {@code null} if the collection is empty
	 */

	public static Object reduce(Collection<Object> collection, BinaryOperator<Object> combiner) {
		Object result = null;
		for (Object element : collection) {
			result = (result == null) ? element : combiner.apply(result, element);
		}
		return result;
	}

	/**
	 * Reduces the elements of the given array into a single value
	 * using the specified combining operator.
	 * <p>
	 * The reduction is performed in iteration order, starting with the first element as the initial value.
	 * </p>
	 *
	 * @param array    the array to reduce
	 * @param combiner the operator used to combine two elements into one
	 * @return the result of the reduction, or {@code null} if the array is empty
	 */
	public static Object reduce(Object[] array, BinaryOperator<Object> combiner) {
		Object result = null;
		for (Object element : array) {
			result = (result == null) ? element : combiner.apply(result, element);
		}
		return result;
	}


	/**
	 * Reduces the values of the given map into a single value
	 * using the specified combining operator.
	 * <p>
	 * The reduction is performed over the map's values in iteration order.
	 * </p>
	 *
	 * @param map      the map whose values to reduce
	 * @param combiner the operator used to combine two elements into one
	 * @return the result of the reduction, or {@code null} if the map is empty
	 */

	public static Object reduce(Map<?, Object> map, BinaryOperator<Object> combiner) {
		Object result = null;
		for (Map.Entry<?, Object> entry : map.entrySet()) {
			result = (result == null) ? entry.getValue() : combiner.apply(result, entry.getValue());
		}
		return result;
	}

	/**
	 * Determines whether the specified object represents a collection-like structure:
	 * a {@link Collection}, {@link Map}, or an array.
	 * <p>
	 * This method is used for dynamic type inspection in recursive utilities
	 * that operate over general data structures.
	 * </p>
	 *
	 * @param obj the object to inspect
	 * @return {@code true} if the object is a Collection, Map, or Array; {@code false} otherwise
	 */
	public static boolean isCollectionMapOrArray(Object obj) {
		return obj != null && (obj instanceof Collection<?> || obj instanceof Map<?, ?> || obj.getClass().isArray());
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

	/**
	 * Creates a {@link NaftahBugError} indicating that an index is out of bounds for a collection.
	 * <p>
	 * This version does not include a cause (exception).
	 * </p>
	 *
	 * @param targetIndex the index that was attempted to be accessed
	 * @param size        the size of the collection at the time of access
	 * @return a {@code NaftahBugError} with a detailed Arabic error message
	 */
	public static NaftahBugError newNaftahIndexOutOfBoundsBugError(int targetIndex, int size) {
		return newNaftahIndexOutOfBoundsBugError(targetIndex, size, null, -1, -1);
	}

	/**
	 * Creates a {@link NaftahBugError} indicating that an index is out of bounds for a collection,
	 * and optionally includes a cause (wrapped exception).
	 *
	 * @param targetIndex the index that was attempted to be accessed
	 * @param size        the size of the collection at the time of access
	 * @param e           an optional cause of the error (can be {@code null})
	 * @param line        The line number on which the 1st character of this token was matched
	 * @param column      The index of the first character of this token relative to the beginning of the line at which
	 *                    it occurs
	 * @return a {@code NaftahBugError} with a detailed Arabic error message and optional cause
	 */
	public static NaftahBugError newNaftahIndexOutOfBoundsBugError( int targetIndex,
																	int size,
																	Exception e,
																	int line,
																	int column) {
		return new NaftahBugError(String.format("""
												المؤشر المطلوب (%d) خارج حدود المجموعة. عدد العناصر الحالية هو %d.
												""", targetIndex, size), e, line, column);
	}
}
