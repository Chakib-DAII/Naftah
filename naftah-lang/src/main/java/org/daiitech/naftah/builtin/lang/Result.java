// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

package org.daiitech.naftah.builtin.lang;

import java.util.function.Function;

import org.daiitech.naftah.errors.NaftahBugError;

/**
 * A sealed interface representing the result of a computation that may either succeed (Ok)
 * or fail (Error). Inspired by the Result type in functional programming languages.
 *
 * @param <T> the type of the success value
 * @param <E> the type of the error value
 * @author Chakib Daii
 */
public sealed interface Result<T, E> permits Result.Error, Result.Ok {

	/**
	 * Returns {@code true} if the result is an {@code Ok}, otherwise {@code false}.
	 */
	boolean isOk();

	/**
	 * Returns {@code true} if the result is an {@code Error}, otherwise {@code false}.
	 */
	boolean isError();

	/**
	 * Returns the contained success value if present, or throws a {@link NaftahBugError}
	 * if this is an {@code Error}.
	 *
	 * @return the success value
	 * @throws NaftahBugError if called on an Error
	 */
	T unwrap() throws NaftahBugError;

	/**
	 * Returns the contained error value if present, or throws a {@link NaftahBugError}
	 * if this is an {@code Ok}.
	 *
	 * @return the error value
	 * @throws NaftahBugError if called on an Ok
	 */
	E unwrapError() throws NaftahBugError;

	/**
	 * Returns the success value if this is an {@code Ok}, otherwise returns the given default.
	 *
	 * @param defaultValue the value to return if this is an {@code Error}
	 * @return the success value or the default
	 */
	T unwrapOr(T defaultValue);

	/**
	 * Maps the success value to another value using the given function, if present.
	 * If this is an {@code Error}, the error is propagated.
	 *
	 * @param <U>    the type of the result of the mapping function
	 * @param mapper a function to apply to the success value
	 * @return a new {@code Result} containing the mapped value or the original error
	 */
	<U> Result<U, E> map(Function<? super T, ? extends U> mapper);

	/**
	 * Applies a function that returns another {@code Result}, effectively flattening
	 * the structure. If this is an {@code Error}, it is propagated unchanged.
	 *
	 * @param <U>    the type of the value in the returned result
	 * @param mapper a function returning a {@code Result}
	 * @return the result of applying the function, or the original error
	 */
	<U> Result<U, E> flatMap(Function<? super T, Result<U, E>> mapper);

	/**
	 * Represents a successful result containing a value of type {@code T}.
	 *
	 * @param <T> the type of the success value
	 * @param <E> the type of the error value (unused in Ok, but part of Result)
	 */
	final class Ok<T, E> implements Result<T, E> {
		private final T value;

		private Ok(T value) {
			this.value = value;
		}


		/**
		 * Creates a new {@code Ok} result with the given value.
		 *
		 * @param value the success value
		 * @return a new {@code Ok} instance
		 */
		public static <T, E> Ok<T, E> of(T value) {
			return new Ok<>(value);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean isOk() {
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean isError() {
			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public T unwrap() {
			return value;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public E unwrapError() {
			throw new NaftahBugError("محاولة استخدام فشل على قيمة من النوع نجاح.");
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public T unwrapOr(T defaultValue) {
			return value;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public <U> Result<U, E> map(Function<? super T, ? extends U> mapper) {
			return new Ok<>(mapper.apply(value));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public <U> Result<U, E> flatMap(Function<? super T, Result<U, E>> mapper) {
			return mapper.apply(value);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			return "<(%s)%s>".formatted(value, "نجاح");
		}
	}

	/**
	 * Represents a failed result containing an error value of type {@code E}.
	 *
	 * @param <T> the type of the success value (unused in Error, but part of Result)
	 * @param <E> the type of the error value
	 */
	final class Error<T, E> implements Result<T, E> {
		private final E error;

		private Error(E error) {
			this.error = error;
		}

		/**
		 * Creates a new {@code Error} result with the given error value.
		 *
		 * @param error the error value
		 * @return a new {@code Error} instance
		 */
		public static <T, E> Error<T, E> of(E error) {
			return new Error<>(error);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean isOk() {
			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean isError() {
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public T unwrap() {
			throw new NaftahBugError("محاولة استخدام نجاح على قيمة من النوع فشل.");
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public E unwrapError() {
			return error;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public T unwrapOr(T defaultValue) {
			return defaultValue;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public <U> Result<U, E> map(Function<? super T, ? extends U> mapper) {
			return new Error<>(error);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public <U> Result<U, E> flatMap(Function<? super T, Result<U, E>> mapper) {
			return new Error<>(error);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			return "<(%s)%s>".formatted(error, "فشل");
		}

	}

}
