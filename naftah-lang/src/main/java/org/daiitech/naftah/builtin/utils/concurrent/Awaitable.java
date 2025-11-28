package org.daiitech.naftah.builtin.utils.concurrent;

import org.daiitech.naftah.errors.NaftahBugError;

/**
 * Represents an asynchronous or deferred computation whose result can be awaited.
 *
 * <p>An {@code Awaitable} may represent a task, a scope, or any computation that
 * produces a result in the future. Implementations provide a way to block until
 * the computation completes and obtain its result.</p>
 *
 * @param <T> the type of result produced by this computation
 * @author Chakib Daii
 */
public interface Awaitable<T> {

	/**
	 * Blocks until the computation completes and returns the result.
	 *
	 * <p>If the computation has already completed, this method returns immediately
	 * with the result. If it has not started, calling this method may throw an
	 * {@link IllegalStateException}, depending on the implementation.</p>
	 *
	 * @return the result of the computation
	 * @throws NaftahBugError        if the computation throws an exception during execution
	 * @throws IllegalStateException if the computation has not been spawned or is otherwise unavailable
	 */
	T await() throws NaftahBugError;
}
