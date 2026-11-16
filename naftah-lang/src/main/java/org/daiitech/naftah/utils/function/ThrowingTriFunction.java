// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

package org.daiitech.naftah.utils.function;

import java.util.function.BiFunction;

/**
 * A {@link TriFunction} that allows invocation of code that throws a checked
 * exception.
 *
 * @param <T> the type of the first argument to the function
 * @param <U> the type of the second argument to the function
 * @param <V> the type of the third argument to the function
 * @param <R> the type of the result of the function
 * @author Chakib Daii
 */
@FunctionalInterface
public interface ThrowingTriFunction<T, U, V, R> extends TriFunction<T, U, V, R> {

	/**
	 * Lambda friendly convenience method that can be used to create a
	 * {@link ThrowingTriFunction} where the {@link #apply(Object, Object, Object)}
	 * method wraps any checked exception thrown by the supplied lambda expression
	 * or method reference.
	 *
	 * <p>
	 * This method can be especially useful when working with method references. It
	 * allows you to easily convert a method that throws a checked exception into
	 * an instance compatible with a regular {@link TriFunction}.
	 *
	 * <p>
	 * For example:
	 *
	 * <pre class="code">
	 * someMethod(ThrowingTriFunction.of(Example::methodThatCanThrowCheckedException));
	 * </pre>
	 *
	 * @param <T>      the type of the first argument to the function
	 * @param <U>      the type of the second argument to the function
	 * @param <V>      the type of the third argument to the function
	 * @param <R>      the type of the result of the function
	 * @param function the source function
	 * @return a new {@link ThrowingTriFunction} instance
	 */
	static <T, U, V, R> ThrowingTriFunction<T, U, V, R> of(
															ThrowingTriFunction<T, U, V, R> function) {
		return function;
	}

	/**
	 * Lambda friendly convenience method that can be used to create a
	 * {@link ThrowingTriFunction} where the {@link #apply(Object, Object, Object)}
	 * method wraps any thrown checked exceptions using the given
	 * {@code exceptionWrapper}.
	 *
	 * <p>
	 * This method can be especially useful when working with method references. It
	 * allows you to easily convert a method that throws a checked exception into
	 * an instance compatible with a regular {@link TriFunction}.
	 *
	 * <p>
	 * For example:
	 *
	 * <pre class="code">
	 * someMethod(ThrowingTriFunction.of(
	 * Example::methodThatCanThrowCheckedException,
	 * IllegalStateException::new));
	 * </pre>
	 *
	 * @param <T>              the type of the first argument to the function
	 * @param <U>              the type of the second argument to the function
	 * @param <V>              the type of the third argument to the function
	 * @param <R>              the type of the result of the function
	 * @param function         the source function
	 * @param exceptionWrapper the exception wrapper to use
	 * @return a new {@link ThrowingTriFunction} instance
	 */
	static <T, U, V, R> ThrowingTriFunction<T, U, V, R> of(
															ThrowingTriFunction<T, U, V, R> function,
															BiFunction<String, Exception, RuntimeException> exceptionWrapper) {

		return function.throwing(exceptionWrapper);
	}

	/**
	 * Applies this function to the given arguments, possibly throwing a checked
	 * exception.
	 *
	 * @param t the first function argument
	 * @param u the second function argument
	 * @param v the third function argument
	 * @return the function result
	 * @throws Exception on error
	 */
	R applyWithException(T t, U u, V v) throws Exception;

	/**
	 * Default {@link TriFunction#apply(Object, Object, Object)} that wraps any
	 * thrown checked exceptions (by default in a {@link RuntimeException}).
	 *
	 * @param t the first function argument
	 * @param u the second function argument
	 * @param v the third function argument
	 * @return the function result
	 * @see TriFunction#apply(Object, Object, Object)
	 */
	@Override
	default R apply(T t, U u, V v) {
		return apply(t, u, v, RuntimeException::new);
	}

	/**
	 * Applies this function to the given arguments, wrapping any thrown checked
	 * exceptions using the given {@code exceptionWrapper}.
	 *
	 * @param t                the first function argument
	 * @param u                the second function argument
	 * @param v                the third function argument
	 * @param exceptionWrapper {@link BiFunction} that wraps the given message and
	 *                         checked exception into a runtime exception
	 * @return a result
	 */
	default R apply(T t,
					U u,
					V v,
					BiFunction<String, Exception, RuntimeException> exceptionWrapper) {
		try {
			return applyWithException(t, u, v);
		}
		catch (RuntimeException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw exceptionWrapper.apply(ex.getMessage(), ex);
		}
	}

	/**
	 * Return a new {@link ThrowingTriFunction} where the
	 * {@link #apply(Object, Object, Object)} method wraps any thrown checked
	 * exceptions using the given {@code exceptionWrapper}.
	 *
	 * @param exceptionWrapper {@link BiFunction} that wraps the given message and
	 *                         checked exception into a runtime exception
	 * @return the replacement {@link ThrowingTriFunction} instance
	 */
	default ThrowingTriFunction<T, U, V, R> throwing(
														BiFunction<String, Exception, RuntimeException> exceptionWrapper) {
		return new ThrowingTriFunction<>() {
			@Override
			public R applyWithException(T t, U u, V v) throws Exception {
				return ThrowingTriFunction.this.applyWithException(t, u, v);
			}

			@Override
			public R apply(T t, U u, V v) {
				return apply(t, u, v, exceptionWrapper);
			}
		};
	}
}
