package org.daiitech.naftah.builtin.utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.daiitech.naftah.builtin.lang.DeclaredVariable;
import org.daiitech.naftah.builtin.lang.None;
import org.daiitech.naftah.builtin.utils.op.BinaryOperation;
import org.daiitech.naftah.builtin.utils.op.UnaryOperation;
import org.daiitech.naftah.errors.NaftahBugError;

import static org.daiitech.naftah.builtin.utils.ObjectUtils.getNaftahValueToString;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahKeyNotFoundError;
import static org.daiitech.naftah.parser.NaftahParserHelper.NULL;

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
	 * Removes and returns the element at the specified index from the given collection.
	 *
	 * <p>This method provides an index-based removal operation for any {@link java.util.Collection}
	 * type that supports element removal via an {@link java.util.Iterator}. It iterates over
	 * the collection until the target index is reached, removes that element, and returns it.
	 * </p>
	 *
	 * <p>If the collection's size is less than or equal to {@code targetIndex}, an
	 * {@code IndexOutOfBoundsException}-like error is thrown using
	 * {@code newNaftahIndexOutOfBoundsBugError(int, int)}.</p>
	 *
	 * <p>Behavior details:</p>
	 * <ul>
	 * <li>Removes the element at the specified zero-based index.</li>
	 * <li>Returns the removed element.</li>
	 * <li>Returns {@code None.get()} if the element cannot be found (which should not occur
	 * if the bounds check passes).</li>
	 * </ul>
	 *
	 * <p>This method does not support random access and runs in O(n) time.</p>
	 *
	 * @param collection  the collection from which to remove the element; must not be {@code null}
	 * @param targetIndex the zero-based index of the element to remove
	 * @return the removed element, or {@code None.get()} if no element was removed
	 * @throws NaftahBugError if {@code targetIndex} is out of bounds for the given collection
	 * @see java.util.Iterator#remove()
	 * @see java.util.Collection#size()
	 */
	public static Object removeElementAt(Collection<?> collection, int targetIndex) {
		if (collection.size() <= targetIndex) {
			throw newNaftahIndexOutOfBoundsBugError(targetIndex, collection.size());
		}

		Iterator<?> iterator = collection.iterator();
		int currentIndex = 0;
		Object removed = None.get();

		while (iterator.hasNext()) {
			Object item = iterator.next();
			if (currentIndex == targetIndex) {
				iterator.remove();
				removed = item;
				break;
			}
			currentIndex++;
		}
		return removed;
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
	public static <T> Collection<T> createCompatibleCollection(Collection<T> original) {
		if (original instanceof LinkedHashSet<T>) {
			return new LinkedHashSet<>();
		}
		if (original instanceof List<T>) {
			return new ArrayList<>();
		}
		if (original instanceof Set<T>) {
			return new HashSet<>();
		}
		if (original instanceof Queue<T>) {
			return new LinkedList<>();
		}
		return new ArrayList<>();
	}

	/**
	 * Creates a new {@link Collection} instance of the specified type.
	 *
	 * <p>If {@code collectionType} is an interface (e.g., List, Set, Queue), a default
	 * implementation will be created (ArrayList, HashSet, LinkedList).
	 * If {@code collectionType} is a concrete class, an attempt is made to create a new
	 * instance using the default constructor.
	 * </p>
	 *
	 * @param collectionType the collection type to instantiate.
	 * @return a new collection instance of the requested type.
	 */
	public static Collection<Object> createCollection(Class<?> collectionType) {
		try {
			//noinspection unchecked
			return (Collection<Object>) collectionType.getDeclaredConstructor().newInstance();
		}
		catch (Exception ignored) {
			// Default implementations for common interfaces
			if (List.class.isAssignableFrom(collectionType)) {
				return new ArrayList<>();
			}
			if (Set.class.isAssignableFrom(collectionType)) {
				return new HashSet<>();
			}
			if (Queue.class.isAssignableFrom(collectionType)) {
				return new LinkedList<>();
			}
			return new ArrayList<>();
		}
	}

	/**
	 * Creates a new {@link Map} instance of the specified type.
	 *
	 * <p>If the provided {@code mapType} represents a concrete class, this method attempts
	 * to instantiate it using its no-argument constructor. If instantiation fails or if the type
	 * is an interface, a default implementation is chosen based on the type hierarchy:</p>
	 *
	 * <ul>
	 * <li>{@link java.util.SortedMap} → {@link java.util.TreeMap}</li>
	 * <li>{@link java.util.LinkedHashMap} → {@link java.util.LinkedHashMap}</li>
	 * <li>Any other type → {@link java.util.HashMap}</li>
	 * </ul>
	 *
	 * @param mapType the {@link Class} representing the map type to instantiate
	 * @param <K>     the type of keys maintained by the map
	 * @param <V>     the type of mapped values
	 * @return a new {@link Map} instance of the requested type, or a default implementation
	 *         if instantiation fails
	 * @throws NullPointerException if {@code mapType} is {@code null}
	 */
	public static <K, V> Map<K, V> createMap(Class<?> mapType) {
		try {
			//noinspection unchecked
			return (Map<K, V>) mapType.getDeclaredConstructor().newInstance();
		}
		catch (Exception ignored) {
			if (SortedMap.class.isAssignableFrom(mapType)) {
				return new TreeMap<>();
			}
			if (LinkedHashMap.class.isAssignableFrom(mapType)) {
				return new LinkedHashMap<>();
			}
			return new HashMap<>();
		}
	}

	/**
	 * Converts any Java array (including primitive arrays) into an Object[].
	 * This is reflection-safe and never throws ClassCastException.
	 *
	 * @param array the source array (e.g. int[], String[], Object[], etc.)
	 * @return an Object[] containing all elements, or an empty array if null
	 * @throws IllegalArgumentException if the input is not an array
	 */
	public static Object[] toObjectArray(Object array) {
		if (array == null) {
			return new Object[0];
		}

		Class<?> type = array.getClass();
		if (!type.isArray()) {
			throw new IllegalArgumentException("Provided object is not an array: " + type);
		}

		// Case 1: Already an Object[] → return same reference (no copy)
		if (array instanceof Object[] objectArray) {
			return objectArray;
		}

		// Case 2: Primitive array → must box elements
		int length = Array.getLength(array);
		Object[] boxed = new Object[length];
		for (int i = 0; i < length; i++) {
			boxed[i] = Array.get(array, i);
		}

		return boxed;
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

	/**
	 * Converts an arbitrary object into its Arabic string representation.
	 * <p>
	 * This method detects the object's runtime type and renders it accordingly:
	 * <ul>
	 * <li>{@code List} → قائمة</li>
	 * <li>{@code Set} → مجموعة</li>
	 * <li>{@code Tuple} → تركيبة</li>
	 * <li>{@code Map} → كائن / مصفوفة ترابطية</li>
	 * <li>Array → قائمة</li>
	 * <li>Other → uses {@code getNaftahValueToString(o)}</li>
	 * </ul>
	 *
	 * @param o the object to convert
	 * @return a string in Arabic describing the object's structure and contents
	 */
	public static String toString(Object o) {
		return toString(o, false);
	}

	/**
	 * Converts an arbitrary object into its Arabic string representation.
	 * <p>
	 * This method detects the object's runtime type and renders it accordingly:
	 * <ul>
	 * <li>{@code List} → قائمة</li>
	 * <li>{@code Set} → مجموعة</li>
	 * <li>{@code Tuple} → تركيبة</li>
	 * <li>{@code Map} → كائن / مصفوفة ترابطية</li>
	 * <li>Array → قائمة</li>
	 * <li>Other → uses {@code getNaftahValueToString(o)}</li>
	 * </ul>
	 *
	 * @param o            the object to convert
	 * @param naftahObject marks that the object to convert is naftah object
	 * @return a string in Arabic describing the object's structure and contents
	 */
	public static String toString(Object o, boolean naftahObject) {
		String result;
		if (o.getClass().isArray()) {
			result = "قائمة: ";
			if (o instanceof Object[] objects) {
				result += toString(objects, '[', ']');
			}
			else {
				result += arrayToString(o, '[', ']');
			}
		}
		else if (o instanceof Collection<?> collection) {
			if (collection instanceof Tuple tuple) {
				result = "تركيبة: " + toString(tuple.toArray(Object[]::new), '(', ')');
			}
			else if (collection instanceof List<?> list) {
				result = "قائمة: " + toString(list.toArray(Object[]::new), '[', ']');
			}
			else if (collection instanceof Set<?> set) {
				result = "مجموعة: " + toString(set.toArray(Object[]::new), '{', '}');
			}
			else {
				result = collection.toString();
			}
		}
		else if (o instanceof Map<?, ?> map) {
			if (naftahObject || map.values().stream().allMatch(value -> value instanceof DeclaredVariable)) {
				result = "كائن: ";
			}
			else {
				result = "مصفوفة ترابطية: ";
			}
			result += toString(map, '{', '}');
		}
		else {
			result = getNaftahValueToString(o);
		}
		return result;
	}

	/**
	 * Converts an array to a string representation, handling both primitive and object arrays.
	 * <p>
	 * This method works for any array type, including nested arrays of primitives or objects.
	 * Each element is converted to a string using {@link ObjectUtils#getNaftahValueToString(Object)}.
	 * The resulting string is enclosed between the specified {@code prefix} and {@code suffix}
	 * characters, and elements are separated by commas.
	 * </p>
	 *
	 * <p>Examples:</p>
	 * <pre>
	 * int[] ints = {1, 2, 3};
	 * arrayToString(ints, '[', ']'); // returns "[1, 2, 3]"
	 *
	 * String[] strs = {"a", "b"};
	 * arrayToString(strs, '(', ')'); // returns "(a, b)"
	 * </pre>
	 *
	 * @param obj    the array object to convert; may be a primitive array, object array, or {@code null}
	 * @param prefix the character to put at the beginning of the string representation
	 * @param suffix the character to put at the end of the string representation
	 * @return a string representation of the array, or {@code NULL} if the array is {@code null}
	 * @see java.lang.reflect.Array
	 * @see ObjectUtils#getNaftahValueToString(Object)
	 */
	public static String arrayToString(Object obj, char prefix, char suffix) {
		if (obj == null) {
			return NULL;
		}
		Class<?> clazz = obj.getClass();

		if (!clazz.isArray()) {
			return getNaftahValueToString(obj);
		}

		int iMax = Array.getLength(obj) - 1;
		if (iMax == -1) {
			return "" + prefix + suffix;
		}

		StringBuilder b = new StringBuilder();
		b.append(prefix);
		for (int i = 0;; i++) {
			Object element = getNaftahValueToString(Array.get(obj, i));
			b.append(element);
			if (i == iMax) {
				return b.append(suffix).toString();
			}
			b.append(" , ");
		}
	}

	/**
	 * Converts an array into a string representation using the specified prefix and suffix characters.
	 * <p>
	 * Each element is converted via {@link ObjectUtils#getNaftahValueToString(Object)}.
	 *
	 * @param <T>    the type of elements in the array
	 * @param a      the array to convert (can be {@code null})
	 * @param prefix the opening character (e.g., '[', '(', '{')
	 * @param suffix the closing character (e.g., ']', ')', '}')
	 * @return a formatted Arabic string representing the array contents
	 */
	public static <T> String toString(T[] a, char prefix, char suffix) {
		if (a == null) {
			return NULL;
		}
		int iMax = a.length - 1;
		if (iMax == -1) {
			return "" + prefix + suffix;
		}

		StringBuilder b = new StringBuilder();
		b.append(prefix);
		for (int i = 0;; i++) {
			b.append(getNaftahValueToString(a[i]));
			if (i == iMax) {
				return b.append(suffix).toString();
			}
			b.append(" , ");
		}
	}

	/**
	 * Converts a {@link Map} into a string representation using the specified prefix and suffix characters.
	 * <p>
	 * Keys and values are converted to strings, with recursive detection for self-references
	 * (to prevent infinite loops).
	 *
	 * @param <K>    the key type
	 * @param <V>    the value type
	 * @param map    the map to convert (can be {@code null})
	 * @param prefix the opening character (e.g., '{')
	 * @param suffix the closing character (e.g., '}')
	 * @return a formatted Arabic string representing the map entries
	 */
	public static <K, V> String toString(Map<K, V> map, char prefix, char suffix) {
		if (map == null) {
			return NULL;
		}
		Iterator<Map.Entry<K, V>> i = map.entrySet().iterator();
		if (!i.hasNext()) {
			return "" + prefix + suffix;
		}

		StringBuilder sb = new StringBuilder();
		sb.append(prefix);
		for (;;) {
			Map.Entry<K, V> e = i.next();
			K key = e.getKey();
			V value = e.getValue();
			sb.append(key == map ? "(هذه المصفوفة ترابطية)" : getNaftahValueToString(key));
			sb.append('=');
			sb.append(value == map ? "(هذه المصفوفة ترابطية)" : getNaftahValueToString(value));
			if (!i.hasNext()) {
				return sb.append(suffix).toString();
			}
			sb.append(" , ");
		}
	}
}
