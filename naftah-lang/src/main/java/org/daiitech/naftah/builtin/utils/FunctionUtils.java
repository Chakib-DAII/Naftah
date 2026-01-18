// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

package org.daiitech.naftah.builtin.utils;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.daiitech.naftah.builtin.lang.NaftahObject;
import org.daiitech.naftah.builtin.utils.tuple.NTuple;
import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.utils.function.ThrowingFunction;

import static org.daiitech.naftah.errors.ExceptionUtils.newIllegalArgumentException;
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
public final class FunctionUtils {
	/**
	 * Private constructor to prevent instantiation.
	 * Always throws a {@link NaftahBugError} when called.
	 */
	private FunctionUtils() {
		throw newNaftahBugInvalidUsageError();
	}

	/**
	 * Returns {@code true} if all elements contained in the given input
	 * satisfy the supplied predicate.
	 * <p>
	 * The input may represent a composite or a scalar value:
	 * <ul>
	 * <li>{@link NTuple} — evaluated element by element</li>
	 * <li>{@link Collection} — evaluated over its elements</li>
	 * <li>{@link Map} — evaluated over its entries</li>
	 * <li>Array — evaluated over its elements</li>
	 * <li>Any other object — evaluated as a single value</li>
	 * </ul>
	 * </p>
	 * <p>
	 * This method is intended for use in recursive or dynamically-typed
	 * processing where the concrete structure of the input is not known
	 * at compile time.
	 * </p>
	 *
	 * @param <T>       the type of values tested by the predicate
	 * @param input     the input object to evaluate; must not be {@code null}
	 * @param predicate the predicate applied to each evaluated value
	 * @return {@code true} if every evaluated value satisfies the predicate;
	 *         {@code false} otherwise
	 * @throws NaftahBugError if {@code input} is {@code null}
	 */
	public static <T> boolean allMatch(Object input, Predicate<T> predicate) {
		if (input == null) {
			throw newNaftahBugNullInputError(true, (Object) null);
		}

		// NTuple
		if (input instanceof NTuple nTuple) {
			return CollectionUtils.allMatch(nTuple.toArray(), predicate);
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
			return CollectionUtils.allMatch(CollectionUtils.toObjectArray(input), predicate);
		}

		//noinspection unchecked
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
		//noinspection unchecked
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
	 * Reduces the given input to a single value using the specified combiner.
	 * <p>
	 * The input may represent either a composite structure or a scalar value:
	 * <ul>
	 * <li>{@link NTuple} — reduced by combining its elements</li>
	 * <li>{@link Collection} — reduced by combining its elements</li>
	 * <li>Array — reduced by combining its elements</li>
	 * <li>{@link Map} — reduced by combining its values</li>
	 * <li>Any other object — returned as-is</li>
	 * </ul>
	 * </p>
	 * <p>
	 * If the input is {@code null}, this method returns {@code null} without
	 * invoking the combiner.
	 * </p>
	 *
	 * @param input    the input object to reduce; may be {@code null}
	 * @param combiner the binary operator used to combine values
	 * @return the reduced value, or {@code null} if {@code input} is {@code null}
	 * @apiNote This method performs unchecked casts internally and relies on the caller
	 *          to ensure that all combined values are compatible with the provided
	 *          {@link BinaryOperator}.
	 */
	public static Object reduce(Object input, BinaryOperator<Object> combiner) {
		if (input == null) {
			return null;
		}

		// NTuple
		if (input instanceof NTuple nTuple) {
			return CollectionUtils.reduce(nTuple.toArray(), combiner);
		}

		// Collection
		if (input instanceof Collection<?> collection) {
			//noinspection unchecked
			return CollectionUtils.reduce((Collection<Object>) collection, combiner);
		}

		// Array
		if (input.getClass().isArray()) {
			return CollectionUtils.reduce(CollectionUtils.toObjectArray(input), combiner);
		}

		// Map
		if (input instanceof Map<?, ?> map) {
			//noinspection unchecked
			return CollectionUtils.reduce((Map<?, Object>) map, combiner);
		}

		return input;
	}

	/**
	 * Executes a function on the underlying Java value of a {@link NaftahObject}
	 * after validating its runtime type.
	 *
	 * <p>The provided {@link NaftahObject} is unwrapped using
	 * {@link NaftahObject#get(boolean)} with {@code true}. The unwrapped value must
	 * be non-{@code null} and assignable to {@code rawClass}. If these conditions
	 * are met, the value is cast to {@code T} and passed to the supplied
	 * {@code executionFunction}.</p>
	 *
	 * <p>If the {@code NaftahObject} is {@code null}, unwraps to {@code null}, or
	 * does not match the expected runtime type, a
	 * {@link NaftahBugError} is thrown via
	 * {@link org.daiitech.naftah.errors.ExceptionUtils#newIllegalArgumentException(Object)}.</p>
	 *
	 * @param naftahObject      the {@link NaftahObject} to unwrap and validate
	 * @param rawClass          the expected raw runtime class of the unwrapped value
	 * @param executionFunction a function to execute on the validated value
	 * @param <T>               the expected runtime type of the unwrapped value
	 * @param <R>               the return type of the function
	 * @return the result of {@code executionFunction.apply(T)}
	 * @throws NaftahBugError if the object is {@code null}, cannot be unwrapped, or is not
	 *                        assignable to {@code rawClass}
	 */
	public static <T, R> R execute( NaftahObject naftahObject,
									Class<?> rawClass,
									ThrowingFunction<T, R> executionFunction) {
		T object = null;
		//noinspection unchecked
		if (Objects.nonNull(naftahObject) && Objects.nonNull(object = (T) naftahObject.get(true)) && rawClass
				.isAssignableFrom(object.getClass())) {
			return executionFunction.apply(object);
		}
		else {
			throw newIllegalArgumentException(object);
		}
	}

	/**
	 * Executes a consumer on the underlying Java value of a {@link NaftahObject}
	 * after validating its runtime type.
	 *
	 * <p>The provided {@link NaftahObject} is unwrapped using
	 * {@link NaftahObject#get(boolean)} with {@code true}. The unwrapped value must
	 * be non-{@code null} and assignable to {@code rawClass}. If these conditions
	 * are met, the value is cast to {@code T} and passed to the supplied
	 * {@code executionConsumer}.</p>
	 *
	 * <p>If the {@code NaftahObject} is {@code null}, unwraps to {@code null}, or
	 * does not match the expected runtime type, a
	 * {@link NaftahBugError} is thrown via
	 * {@link org.daiitech.naftah.errors.ExceptionUtils#newIllegalArgumentException(Object)}.</p>
	 *
	 * @param naftahObject      the {@link NaftahObject} to unwrap and validate
	 * @param rawClass          the expected raw runtime class of the unwrapped value
	 * @param executionConsumer a consumer to execute on the validated value
	 * @param <T>               the expected runtime type of the unwrapped value
	 * @throws NaftahBugError if the object is {@code null}, cannot be unwrapped, or is not
	 *                        assignable to {@code rawClass}
	 */
	public static <T> void execute(NaftahObject naftahObject, Class<?> rawClass, Consumer<T> executionConsumer) {
		T object = null;
		//noinspection unchecked
		if (Objects.nonNull(naftahObject) && Objects.nonNull(object = (T) naftahObject.get(true)) && rawClass
				.isAssignableFrom(object.getClass())) {
			executionConsumer.accept(object);
		}
		else {
			throw newIllegalArgumentException(object);
		}
	}
}
