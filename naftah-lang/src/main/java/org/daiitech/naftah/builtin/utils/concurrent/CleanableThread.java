// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

package org.daiitech.naftah.builtin.utils.concurrent;

import java.util.Objects;

import org.daiitech.naftah.errors.NaftahBugError;

/**
 * A thread wrapper that ensures a cleanup action is executed
 * after the thread finishes, regardless of whether it terminates normally
 * or due to an exception.
 * <p>
 * This class extends {@link Thread} and adds an optional {@code cleaner}
 * {@link Runnable} that will be executed in the {@code finally} block
 * after the main {@link Runnable} completes.
 * </p>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * Runnable task = () -> System.out.println("Task running");
 * Runnable cleanup = () -> System.out.println("Cleanup after task");
 * Thread t = new CleanableThread(task, cleanup);
 * t.start();
 * }</pre>
 *
 * <p>This is useful for automatically cleaning thread-local variables,
 * releasing resources, or performing other post-execution tasks.</p>
 *
 * @author Chakib Daii
 */
public class CleanableThread extends Thread {
	private final Runnable cleaner;

	/**
	 * Constructs a CleanableThread with no cleaner.
	 *
	 * @param target the main task to run in this thread
	 */
	public CleanableThread(Runnable target) {
		this(target, null);
	}

	/**
	 * Constructs a CleanableThread with a cleaner.
	 *
	 * @param target  the main task to run in this thread
	 * @param cleaner a cleanup task to run after {@code target} completes; may be {@code null}
	 */
	public CleanableThread(Runnable target, Runnable cleaner) {
		super(target);
		this.cleaner = cleaner;
	}

	/**
	 * Runs the thread's task and ensures the cleaner is executed afterward.
	 */
	@Override
	public void run() {
		try {
			super.run();
		}
		catch (Throwable th) {
			throw th instanceof NaftahBugError naftahBugError ? naftahBugError : new NaftahBugError(th);
		}
		finally {
			if (Objects.nonNull(cleaner)) {
				cleaner.run();
			}
		}
	}
}
