package org.daiitech.naftah.builtin.utils;

import java.util.Collection;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;

import org.daiitech.naftah.errors.NaftahBugError;

import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugNullInputError;

/**
 * Utility class providing functional-style operations on various input types,
 * including {@link Collection}, {@link Map}, arrays, and single objects.
 * <p>
 * Methods include {@code allMatch}, {@code noneMatch}, {@code anyMatch}, and {@code reduce},
 * allowing evaluation of predicates and reduction operations on diverse data structures.
 * </p>
 * <p>
 * This class is not meant to be instantiated. Attempting to do so will throw
 * a {@link NaftahBugError}.
 * </p>
 *
 * @author Chakib Daii
 */
public class FunctionUtils {
	/**
	 * Private constructor to prevent instantiation.
	 * Always throws a {@link NaftahBugError} when called.
	 */
	private FunctionUtils() {
		throw newNaftahBugInvalidUsageError();
	}

	/**
	 * Returns {@code true} if all elements in the input match the given predicate.
	 * <p>
	 * Supports input of type {@link Collection}, {@link Map}, arrays, or a single object.
	 * </p>
	 *
	 * @param <T>       the type of elements to be tested by the predicate
	 * @param input     the input object to evaluate; must not be {@code null}
	 * @param predicate the predicate to test each element
	 * @return {@code true} if all elements match the predicate; {@code false} otherwise
	 * @throws NaftahBugError if {@code input} is {@code null}
	 */
	public static <T> boolean allMatch(Object input, Predicate<T> predicate) {
		if (input == null) {
			throw newNaftahBugNullInputError(true, input);
		}

		// Collection
		if (input instanceof Collection<?> collection) {
			return CollectionUtils.allMatch(collection, predicate);
		}

		// Map
		if (input instanceof Map<?, ?> map) {
			return CollectionUtils.allMatch(map, predicate);
		}

		// Array
		if (input.getClass().isArray()) {
			return CollectionUtils.allMatch((Object[]) input, predicate);
		}

		return predicate.test((T) input);
	}

	/**
	 * Returns {@code true} if no elements in the input match the given predicate.
	 * <p>
	 * This method is logically equivalent to negating {@link #allMatch(Object, Predicate)}
	 * with the inverted predicate.
	 * </p>
	 *
	 * @param <T>       the type of elements to be tested by the predicate
	 * @param input     the input object to evaluate; may be {@code null}
	 * @param predicate the predicate to test each element
	 * @return {@code true} if no elements match the predicate; {@code false} otherwise
	 */
	public static <T> boolean noneMatch(Object input, Predicate<T> predicate) {
		return allMatch(input, element -> !predicate.test((T) element));
	}

	/**
	 * Returns {@code true} if at least one element in the input matches the given predicate.
	 * <p>
	 * This method is logically equivalent to negating {@link #noneMatch(Object, Predicate)}.
	 * </p>
	 *
	 * @param <T>       the type of elements to be tested by the predicate
	 * @param input     the input object to evaluate; may be {@code null}
	 * @param predicate the predicate to test each element
	 * @return {@code true} if any element matches the predicate; {@code false} otherwise
	 */
	public static <T> boolean anyMatch(Object input, Predicate<T> predicate) {
		return !noneMatch(input, predicate);
	}

	/**
	 * Reduces the input to a single value using the specified combiner.
	 * <p>
	 * Supports input of type {@link Collection}, {@link Map}, arrays, or a single object.
	 * For collections and arrays, all elements are combined using the {@link BinaryOperator}.
	 * For maps, only the values are combined.
	 * </p>
	 *
	 * @param input    the input object to reduce; may be {@code null}
	 * @param combiner the binary operator to combine elements
	 * @return the reduced value, or {@code null} if the input is {@code null}
	 */
	public static Object reduce(Object input, BinaryOperator<Object> combiner) {
		if (input == null) {
			return null;
		}

		// Collection
		if (input instanceof Collection<?> collection) {
			return CollectionUtils.reduce((Collection<Object>) collection, combiner);
		}

		// Array
		if (input.getClass().isArray()) {
			return CollectionUtils.reduce((Object[]) input, combiner);
		}

		// Map
		if (input instanceof Map<?, ?> map) {
			return CollectionUtils.reduce((Map<?, Object>) map, combiner);
		}

		return input;
	}
}
