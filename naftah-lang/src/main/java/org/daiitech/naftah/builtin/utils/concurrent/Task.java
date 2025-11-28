package org.daiitech.naftah.builtin.utils.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.daiitech.naftah.builtin.utils.ObjectUtils;
import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.parser.DefaultContext;

/**
 * Represents an asynchronous task that executes within a given {@link DefaultContext}.
 * <p>
 * Each {@code Task} wraps a {@link FutureTask} internally and runs in a {@link CleanableThread},
 * which can optionally execute a cleanup action after the task completes.
 * </p>
 *
 * <p>Key features:</p>
 * <ul>
 * <li>Execution within a specific {@link DefaultContext}, preserving thread-local context.</li>
 * <li>Optional cleaner {@link Runnable} executed when the task completes.</li>
 * <li>Lifecycle management: spawning, awaiting, checking completion, cancelling, and timeout-based retrieval.</li>
 * </ul>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * DefaultContext ctx = DefaultContext.getCurrentContext();
 * Task<String> task = Task.of(ctx, () -> "Hello", () -> System.out.println("Cleaning up"));
 * task.spawn();
 * String result = task.await();
 * }</pre>
 *
 * @param <T> the type of result produced by the task
 * @author Chakib Daii
 */
public class Task<T> implements Awaitable<T> {
	private final long taskId;
	private final DefaultContext context;
	private final Callable<T> callable;
	private final Supplier<CleanableThread> cleanableThreadSupplier;
	private FutureTask<T> future;

	/**
	 * Constructs a new Task for the given context, supplier, and cleaner.
	 *
	 * @param context  the execution context for this task
	 * @param supplier the supplier that produces the task result
	 * @param cleaner  optional cleanup code executed after task completion
	 */
	private Task(DefaultContext context, Supplier<T> supplier, Runnable cleaner) {
		this.taskId = hashCode();
		this.context = context;
		this.callable = () -> {
			DefaultContext.setCurrentContext(context);
			return supplier.get();
		};
		cleanableThreadSupplier = () -> new CleanableThread(future, cleaner);
	}

	/**
	 * Creates a new Task instance.
	 *
	 * @param context  the execution context
	 * @param supplier the task logic
	 * @param cleaner  optional cleaner for post-completion
	 * @param <T>      result type
	 * @return a new Task
	 */
	public static <T> Task<T> of(DefaultContext context, Supplier<T> supplier, Runnable cleaner) {
		return new Task<>(context, supplier, cleaner);
	}

	/**
	 * Spawns the task in a new thread.
	 *
	 * @throws NaftahBugError if the task has already been spawned
	 */
	public void spawn() {
		if (future != null) {
			throw new NaftahBugError("تم تشغيل المهمة مسبقًا ولا يمكن تشغيلها مرة أخرى.");
		}
		this.future = new FutureTask<>(callable);
		Thread thread = cleanableThreadSupplier.get();
		thread.start();
		context.registerTask(this);
	}

	/**
	 * Blocks until the task completes and returns the result.
	 *
	 * @return the result of the task
	 * @throws Exception             if the task throws an exception
	 * @throws IllegalStateException if the task has not been spawned
	 */
	@Override
	public T await() throws NaftahBugError {
		try {
			checkSpawned();
			return future.get();
		}
		catch (Throwable th) {
			throw th instanceof NaftahBugError naftahBugError ?
					naftahBugError :
					new NaftahBugError(th);
		}
		finally {
			context.completeTask();
		}
	}

	/**
	 * Checks if the task has completed.
	 *
	 * @return true if the task is done
	 * @throws IllegalStateException if the task has not been spawned
	 */
	public boolean isDone() {
		checkSpawned();
		return future.isDone();
	}

	/**
	 * Checks if the task was cancelled.
	 *
	 * @return true if the task was cancelled
	 * @throws IllegalStateException if the task has not been spawned
	 */
	public boolean isCancelled() {
		checkSpawned();
		return future.isCancelled();
	}

	/**
	 * Attempts to cancel the task.
	 *
	 * @param mayInterruptIfRunning true if the thread executing the task should be interrupted
	 * @return true if the task was successfully cancelled
	 * @throws IllegalStateException if the task has not been spawned
	 */
	public boolean cancel(boolean mayInterruptIfRunning) {
		checkSpawned();
		return future.cancel(mayInterruptIfRunning);
	}

	/**
	 * Retrieves the task result, blocking up to the specified timeout.
	 *
	 * @param timeout the maximum time to wait
	 * @param unit    the time unit of the timeout
	 * @return the task result
	 * @throws Exception             if the task throws an exception
	 * @throws IllegalStateException if the task has not been spawned
	 */
	public T get(long timeout, TimeUnit unit) throws Exception {
		checkSpawned();
		return future.get(timeout, unit);
	}

	/**
	 * Checks whether this task has been spawned.
	 * <p>
	 * A task is considered "spawned" once {@link #spawn()} has been called,
	 * which creates the internal {@link FutureTask} and starts its execution
	 * in a thread.
	 * </p>
	 *
	 * <p>If this method detects that the task has not yet been spawned,
	 * it throws an {@link IllegalStateException} to prevent operations
	 * that require the task to exist (such as {@link #await()},
	 * {@link #isDone()}, {@link #get(long, TimeUnit)}, or {@link #cancel(boolean)}).</p>
	 *
	 * @throws NaftahBugError if the task has not been spawned yet
	 */
	private void checkSpawned() {
		if (future == null) {
			throw new NaftahBugError("لا يمكن تنفيذ هذا الإجراء لأن المهمة لم تُشغَّل بعد.");
		}
	}

	/**
	 * Returns the context in which this task runs.
	 *
	 * @return the execution context
	 */
	public DefaultContext getContext() {
		return context;
	}

	/**
	 * Returns a string representation of this Task.
	 * <p>
	 * The format is "&lt;شغل {taskId}&gt;", where "شغل" means "Task"
	 * in Arabic and {taskId} is a unique numeric identifier for this task,
	 * converted to a string using {@link ObjectUtils#numberToString(Number)}.
	 * </p>
	 *
	 * <p>This representation is intended for debugging and logging purposes
	 * to easily distinguish tasks by their ID.</p>
	 *
	 * @return a string representation of the task
	 */
	@Override
	public String toString() {
		return "<%s %s>".formatted("شغل", ObjectUtils.numberToString(taskId));
	}
}
