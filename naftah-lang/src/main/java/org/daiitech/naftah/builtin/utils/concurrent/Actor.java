package org.daiitech.naftah.builtin.utils.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.parser.DefaultContext;

/**
 * An abstract actor implementation for message-driven concurrency.
 * <p>
 * Each {@code Actor} maintains its own mailbox and processes messages sequentially
 * on a dedicated thread. Messages are sent asynchronously and handled via the
 * {@link #handle(Object)} method.
 * </p>
 * <p>
 * Key features:
 * <ul>
 * <li>Each actor has a private {@link BlockingQueue} for incoming messages.</li>
 * <li>The actor runs on a dedicated thread that processes messages sequentially.</li>
 * <li>Provides lifecycle management: {@link #stop()}, {@link #join()}, {@link #isAlive()}.</li>
 * <li>Supports custom cleanup logic via a {@code cleaner} Runnable.</li>
 * </ul>
 * </p>
 *
 * @param <T> the type of messages this actor can receive
 * @author Chakib Daii
 */
public abstract class Actor<T> implements Runnable {
	private final BlockingQueue<T> mailbox = new LinkedBlockingQueue<>();
	private final Thread thread;
	private final String name;
	private final DefaultContext context;
	private boolean running = true;

	/**
	 * Constructs an actor with a given name and a cleanup task.
	 *
	 * @param name    the name of the actor
	 * @param context the execution context
	 * @param cleaner a cleanup runnable to execute when the actor terminates
	 */
	private Actor(String name, DefaultContext context, Runnable cleaner) {
		this.name = name;
		this.context = context;
		thread = new CleanableThread(this, cleaner);
		thread.start();
	}

	/**
	 * Creates a simple actor from a {@link Consumer} for handling messages.
	 *
	 * @param name     the name of the actor
	 * @param context  the execution context
	 * @param consumer a consumer that processes messages
	 * @param cleaner  a cleanup task executed when the actor stops
	 * @param <T>      the message type
	 * @return a new {@link Actor} instance
	 */
	public static <T> Actor<T> of(  String name,
									DefaultContext context,
									Consumer<T> consumer,
									Runnable cleaner) {
		return new Actor<>(name, context, cleaner) {
			@Override
			public void handle(T message) throws Exception {
				consumer.accept(message);
			}
		};
	}

	/**
	 * Sends a message to this actor asynchronously.
	 *
	 * @param msg the message to send
	 * @return true if the message was successfully enqueued, false otherwise
	 */
	public boolean send(T msg) {
		return mailbox.offer(msg);
	}

	/**
	 * Receives the next message from the mailbox, blocking if none are available.
	 *
	 * @return the next message
	 * @throws InterruptedException if the thread is interrupted while waiting
	 */
	protected T receive() throws InterruptedException {
		return mailbox.take();
	}

	/**
	 * Handles a single message from the mailbox.
	 * <p>
	 * Must be implemented by subclasses. Any exceptions should be handled to
	 * prevent the actor thread from terminating unexpectedly.
	 * </p>
	 *
	 * @param message the message to handle
	 * @throws Exception if an error occurs during message handling
	 */
	public abstract void handle(T message) throws Exception;

	@Override
	public void run() {
		try {
			DefaultContext.setCurrentContext(context);
			while (running) {
				T msg = receive();
				handle(msg);
			}
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		catch (Throwable th) {
			throw th instanceof NaftahBugError naftahBugError ? naftahBugError : new NaftahBugError(th);
		}
	}

	/**
	 * Stops this actor gracefully by setting the running flag to false
	 * and interrupting its thread.
	 */
	public void stop() {
		running = false;
		thread.interrupt();
	}

	/**
	 * Waits for the actor thread to terminate.
	 *
	 * @throws InterruptedException if interrupted while waiting
	 */
	public void join() throws InterruptedException {
		thread.join();
	}

	/**
	 * Checks whether the actor's thread is alive.
	 *
	 * @return true if the thread is alive, false otherwise
	 */
	public boolean isAlive() {
		return thread.isAlive();
	}

	/**
	 * Returns the underlying thread of this actor.
	 *
	 * @return the actor's thread
	 */
	public Thread getThread() {
		return thread;
	}

	/**
	 * Returns the name of the actor.
	 *
	 * @return the actor's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Checks whether the actor is currently running.
	 *
	 * @return true if running, false otherwise
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Returns a string representation of this Actor.
	 * <p>
	 * The format is "&lt;ممثل {name}&gt;", where "ممثل" means "Actor"
	 * in Arabic and {name} is the name of the actor.
	 * </p>
	 *
	 * <p>This is primarily intended for debugging or logging to
	 * identify the actor instance by its name.</p>
	 *
	 * @return a string representation of the actor
	 */
	@Override
	public String toString() {
		return "<%s %s>".formatted("ممثل", name);
	}
}
