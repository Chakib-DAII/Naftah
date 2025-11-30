package org.daiitech.naftah.builtin.utils.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A simple thread-safe channel for communicating between threads.
 * <p>
 * The {@code Channel} class wraps a {@link BlockingQueue} to allow
 * sending and receiving messages between threads safely.
 * </p>
 * <p>
 * Key characteristics:
 * <ul>
 * <li>Supports blocking send and receive operations.</li>
 * <li>Each channel has a name for easier identification.</li>
 * <li>Multiple threads can safely send to and receive from the channel concurrently.</li>
 * </ul>
 * </p>
 *
 * @param <T> the type of values sent through the channel
 * @author Chakib Daii
 */
public class Channel<T> {
	private final BlockingQueue<T> queue = new LinkedBlockingQueue<>();
	private final String name;

	/**
	 * Creates a channel with the specified name.
	 *
	 * @param name the name of the channel
	 */
	private Channel(String name) {
		this.name = name;
	}

	/**
	 * Factory method to create a new channel with the given name.
	 *
	 * @param name the name of the channel
	 * @param <T>  the type of values the channel carries
	 * @return a new {@link Channel} instance
	 */
	public static <T> Channel<T> of(String name) {
		return new Channel<>(name);
	}

	/**
	 * Sends a value into the channel, blocking if the queue is full.
	 *
	 * @param value the value to send
	 * @throws InterruptedException if the thread is interrupted while waiting
	 */
	public void send(T value) throws InterruptedException {
		queue.put(value);
	}

	/**
	 * Receives a value from the channel, blocking if the queue is empty.
	 *
	 * @return the next value from the channel
	 * @throws InterruptedException if the thread is interrupted while waiting
	 */
	public T receive() throws InterruptedException {
		return queue.take();
	}

	/**
	 * Returns the name of this channel.
	 *
	 * <p>
	 * The name is assigned when the channel is created using {@link #of(String)}.
	 * It is used primarily for identification and debugging, especially when
	 * multiple channels are involved in concurrent operations.
	 * </p>
	 *
	 * @return the name of the channel
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns a string representation of the channel.
	 *
	 * @return a string in the format "&lt;قناة name&gt;"
	 */
	@Override
	public String toString() {
		return "<%s %s>".formatted("قناة", name);
	}
}
