package org.daiitech.naftah.builtin.functions;

import org.daiitech.naftah.builtin.NaftahFn;
import org.daiitech.naftah.builtin.NaftahFnProvider;
import org.daiitech.naftah.builtin.lang.NaftahObject;
import org.daiitech.naftah.builtin.time.NaftahDuration;
import org.daiitech.naftah.builtin.utils.concurrent.Actor;
import org.daiitech.naftah.builtin.utils.concurrent.Channel;
import org.daiitech.naftah.builtin.utils.concurrent.Task;
import org.daiitech.naftah.errors.NaftahBugError;

import static org.daiitech.naftah.builtin.utils.FunctionUtils.execute;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.isSimpleOrBuiltinOrCollectionOrMapOfSimpleType;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;

/**
 * Concurrency-related built-in functions for manipulating threads,
 * accessing thread information, controlling execution, and managing thread states.
 * <p>
 * This class offers a set of static methods to work with:
 * <ul>
 * <li>Threads (current, named, interrupted, priority, etc.)</li>
 * <li>Tasks (await, cancel, stop, join, etc.)</li>
 * <li>Channels (send, receive, name)</li>
 * <li>Actors (send message, stop, join, status, etc.)</li>
 * </ul>
 * <p>
 * This class is final and cannot be instantiated. Attempting to create an instance will throw
 * a {@link NaftahBugError}.
 *
 * @author Chakib Daii
 */
@NaftahFnProvider(
					name = "دوال الخيوط",
					useQualifiedName = true,
					useQualifiedAliases = true,
					description = """
									يحتوي هذا الموفر على دوال للتعامل مع الخيوط (Threads)
									والمهام غير المتزامنة (Tasks)، بما يشمل:
									• الحصول على الخيط الحالي
									• تغيير اسم الخيط
									• تغيير الأولوية
									• التحقق من المقاطعة
									• تشغيل مهام غير متزامنة
									• الانتظار لنتائج المهام وإلغائها
									""",
					functionNames = {
										"الخيط_الحالي",
										"اسم_الخيط",
										"اسم_الخيط_الحالي",
										"تغيير_اسم_الخيط_الحالي",
										"تغيير_اسم_الخيط",
										"نم",
										"تنفيس",
										"هل_مقاطع",
										"هل_الخيط_الحالي_مقاطع",
										"قاطع_الخيط",
										"قاطع_الخيط_الحالي",
										"أولوية_الخيط",
										"أولوية_الخيط_الحالي",
										"تغيير_أولوية_الخيط_الحالي",
										"تغيير_أولوية_الخيط",
										"معرف_الخيط",
										"معرف_الخيط_الحالي",
										"انتظر",
										"مكتملة",
										"ملغية",
										"الغاء_شغل",
										"معرف_شغل",
										"اوقف_المهمة",
										"انتظر_خيط_المهمة",
										"المهمة_حية",
										"خيط_المهمة",
										"إنشاء_قناة",
										"ارسل",
										"استقبل",
										"اسم_القناة",
										"ارسل_للممثل",
										"اوقف_الممثل",
										"انتظر_الممثل",
										"ممثل_حي",
										"ممثل_يعمل",
										"اسم_الممثل",
										"خيط_الممثل"
					}
)
public final class ConcurrencyBuiltinFunctions {

	/**
	 * Private constructor to prevent instantiation.
	 * Throws {@link NaftahBugError} if called.
	 */
	private ConcurrencyBuiltinFunctions() {
		throw newNaftahBugInvalidUsageError();
	}

	/**
	 * Returns the currently executing thread.
	 *
	 * @return the current {@link Thread}
	 */
	@NaftahFn(
				name = "الخيط_الحالي",
				description = "يعيد الخيط الحالي الذي ينفذ هذا الجزء من الشيفرة.",
				usage = "دوال:الخيوط::الخيط_الحالي()",
				returnType = Thread.class
	)
	public static NaftahObject currentThread() {
		return NaftahObject.of(Thread.currentThread());
	}

	/**
	 * Returns the name of the specified thread.
	 *
	 * @param threadObject the thread whose name to return
	 * @return the name of the thread
	 */
	@NaftahFn(
				name = "اسم_الخيط",
				description = "يعيد اسم الخيط المُعطى.",
				usage = "دوال:الخيوط::اسم_الخيط(دوال:الخيوط::الخيط_الحالي())",
				parameterTypes = Thread.class,
				returnType = String.class
	)
	public static String getThreadName(NaftahObject threadObject) {
		return execute(threadObject, Thread.class, Thread::getName);
	}

	/**
	 * Returns the name of the currently executing thread.
	 *
	 * @return the current thread's name
	 */
	@NaftahFn(
				name = "اسم_الخيط_الحالي",
				description = "يعيد اسم الخيط الحالي.",
				usage = "دوال:الخيوط::اسم_الخيط_الحالي()",
				returnType = String.class
	)
	public static String getCurrentThreadName() {
		return Thread.currentThread().getName();
	}

	/**
	 * Sets the name of the currently executing thread.
	 *
	 * @param newName the new name for the current thread
	 */
	@NaftahFn(
				name = "تغيير_اسم_الخيط_الحالي",
				description = "يغيّر اسم الخيط الحالي.",
				usage = "دوال:الخيوط::تغيير_اسم_الخيط_الحالي(\"جديد\")",
				parameterTypes = String.class,
				returnType = void.class
	)
	public static void setCurrentThreadName(String newName) {
		Thread.currentThread().setName(newName);
	}

	/**
	 * Sets the name of the specified thread.
	 *
	 * @param threadObject the thread to rename
	 * @param newName      the new name for the thread
	 */
	@NaftahFn(
				name = "تغيير_اسم_الخيط",
				description = "يغيّر اسم الخيط المُعطى إلى الاسم الجديد.",
				usage = """
						دوال:الخيوط::تغيير_اسم_الخيط(دوال:الخيوط::الخيط_الحالي(), "المنفذ-١")
						""",
				parameterTypes = {Thread.class, String.class},
				returnType = void.class
	)
	public static void setThreadName(NaftahObject threadObject, String newName) {
		execute(threadObject, Thread.class, (Thread thread) -> thread.setName(newName));
	}

	/**
	 * Causes the currently executing thread to sleep for the specified number of milliseconds.
	 *
	 * @param millis the length of time to sleep in milliseconds
	 * @throws IllegalStateException if the sleep is interrupted
	 */
	@NaftahFn(
				name = "نم",
				description = "يوقِف تنفيذ الخيط الحالي لعدد معين من الملّي ثواني.",
				usage = "دوال:الخيوط::نم(1000)",
				parameterTypes = Number.class,
				returnType = void.class
	)
	public static void sleep(Number millis) {
		try {
			Thread.sleep(millis.longValue());
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("تمت مقاطعة الخيط أثناء النوم", e);
		}
	}

	/**
	 * Causes the currently executing thread to sleep for the specified number of
	 * milliseconds plus an additional nanoseconds adjustment.
	 *
	 * @param millis the length of time to sleep in milliseconds
	 * @param nanos  additional nanoseconds to sleep (0–999999)
	 * @throws IllegalStateException if the sleep is interrupted
	 */
	@NaftahFn(
				name = "نم",
				description = "يوقِف تنفيذ الخيط الحالي لعدد من الملّي ثواني مع ضبط بالنانو ثانية.",
				usage = "دوال:الخيوط::نم(1000 , 500000)",
				parameterTypes = {Number.class, Number.class},
				returnType = void.class
	)
	public static void sleep(Number millis, Number nanos) {
		try {
			Thread.sleep(millis.longValue(), nanos.intValue());
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("تمت مقاطعة الخيط أثناء النوم", e);
		}
	}

	/**
	 * Causes the currently executing thread to sleep for the specified duration.
	 *
	 * @param duration the duration to sleep
	 * @throws IllegalStateException if the sleep is interrupted
	 */
	@NaftahFn(
				name = "نم",
				description = "يوقِف تنفيذ الخيط الحالي لمدة زمنية محددة.",
				usage = "دوال:الخيوط::نم(مدة_)",
				parameterTypes = NaftahDuration.class,
				returnType = void.class
	)
	public static void sleep(NaftahDuration duration) {
		try {
			long millis = duration.temporalAmount().toMillis();
			int nanos = duration.getNano() % 1_000_000;
			Thread.sleep(millis, nanos);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("تمت مقاطعة الخيط أثناء النوم", e);
		}
	}


	/**
	 * Causes the currently executing thread to yield execution to other threads.
	 */
	@NaftahFn(
				name = "تنفيس",
				description = "يطلب من المجدول (Scheduler) منح وقت تنفيذ لخيوط أخرى.",
				usage = "دوال:الخيوط::تنفيس()",
				returnType = void.class
	)
	public static void yieldThread() {
		Thread.yield();
	}

	/**
	 * Checks whether the specified thread has been interrupted.
	 *
	 * @param threadObject the thread to check
	 * @return {@code true} if the thread is interrupted, {@code false} otherwise
	 */
	@NaftahFn(
				name = "هل_مقاطع",
				description = "يتحقق مما إذا كان الخيط قد تم مقاطعته.",
				usage = "دوال:الخيوط::هل_مقاطع(دوال:الخيوط::الخيط_الحالي())",
				parameterTypes = Thread.class,
				returnType = boolean.class
	)
	public static boolean isInterrupted(NaftahObject threadObject) {
		return execute(threadObject, Thread.class, Thread::isInterrupted);
	}

	/**
	 * Checks whether the currently executing thread has been interrupted.
	 *
	 * @return {@code true} if the current thread is interrupted, {@code false} otherwise
	 */
	@NaftahFn(
				name = "هل_الخيط_الحالي_مقاطع",
				description = "يتحقق مما إذا كان الخيط الحالي مقاطعاً.",
				usage = "دوال:الخيوط::هل_الخيط_الحالي_مقاطع()",
				returnType = boolean.class
	)
	public static boolean isCurrentThreadInterrupted() {
		return Thread.currentThread().isInterrupted();
	}

	/**
	 * Interrupts the specified thread.
	 *
	 * @param threadObject the thread to interrupt
	 */
	@NaftahFn(
				name = "قاطع_الخيط",
				description = "يقاطع الخيط المُعطى.",
				usage = "دوال:الخيوط::قاطع_الخيط(خيط)",
				parameterTypes = Thread.class,
				returnType = void.class
	)
	public static void interruptThread(NaftahObject threadObject) {
		execute(threadObject, Thread.class, Thread::interrupt);
	}

	/**
	 * Interrupts the currently executing thread.
	 */
	@NaftahFn(
				name = "قاطع_الخيط_الحالي",
				description = "يقاطع الخيط الحالي.",
				usage = "دوال:الخيوط::قاطع_الخيط_الحالي()",
				returnType = void.class
	)
	public static void interruptCurrentThread() {
		Thread.currentThread().interrupt();
	}

	/**
	 * Returns the priority of the specified thread.
	 *
	 * @param threadObject the thread to query
	 * @return the priority of the thread
	 */
	@NaftahFn(
				name = "أولوية_الخيط",
				description = "يعيد أولوية الخيط.",
				usage = "دوال:الخيوط::أولوية_الخيط(دوال:الخيوط::الخيط_الحالي())",
				parameterTypes = {Thread.class},
				returnType = int.class
	)
	public static int getThreadPriority(NaftahObject threadObject) {
		return execute(threadObject, Thread.class, Thread::getPriority);
	}

	/**
	 * Returns the priority of the currently executing thread.
	 *
	 * @return the current thread's priority
	 */
	@NaftahFn(
				name = "أولوية_الخيط_الحالي",
				description = "يعيد أولوية الخيط الحالي.",
				usage = "دوال:الخيوط::أولوية_الخيط_الحالي()",
				returnType = int.class
	)
	public static int getCurrentThreadPriority() {
		return Thread.currentThread().getPriority();
	}

	/**
	 * Sets the priority of the currently executing thread.
	 *
	 * @param priority the new priority (1–10)
	 */
	@NaftahFn(
				name = "تغيير_أولوية_الخيط_الحالي",
				description = "يغيّر أولوية الخيط الحالي.",
				usage = "دوال:الخيوط::تغيير_أولوية_الخيط_الحالي(7)",
				parameterTypes = Number.class,
				returnType = void.class
	)
	public static void setCurrentThreadPriority(Number priority) {
		Thread.currentThread().setPriority(priority.intValue());
	}

	/**
	 * Sets the priority of the specified thread.
	 *
	 * @param threadObject the thread to modify
	 * @param priority     the new priority (1–10)
	 */
	@NaftahFn(
				name = "تغيير_أولوية_الخيط",
				description = "يحدد أولوية جديدة للخيط (من 1 إلى 10).",
				usage = "دوال:الخيوط::تغيير_أولوية_الخيط(خيط, 7)",
				parameterTypes = {Thread.class, Number.class},
				returnType = void.class
	)
	public static void setThreadPriority(NaftahObject threadObject, Number priority) {
		execute(threadObject, Thread.class, (Thread thread) -> thread.setPriority(priority.intValue()));
	}

	/**
	 * Returns the unique ID of the specified thread.
	 *
	 * @param threadObject the thread to query
	 * @return the thread's unique ID
	 */
	@NaftahFn(
				name = "معرف_الخيط",
				description = "يعيد المعرّف الفريد للخيط.",
				usage = "دوال:الخيوط::معرف_الخيط(دوال:الخيوط::الخيط_الحالي())",
				parameterTypes = {Thread.class},
				returnType = long.class
	)
	public static long getThreadId(NaftahObject threadObject) {
		return execute(threadObject, Thread.class, Thread::getId);
	}

	/**
	 * Returns the unique ID of the currently executing thread.
	 *
	 * @return the current thread's ID
	 */
	@NaftahFn(
				name = "معرف_الخيط_الحالي",
				description = "يعيد المعرّف الفريد للخيط الحالي.",
				usage = "دوال:الخيوط::معرف_الخيط_الحالي()",
				returnType = long.class
	)
	public static long getCurrentThreadId() {
		return Thread.currentThread().getId();
	}

	/**
	 * Waits for the specified task to complete and returns its result.
	 *
	 * @param task the task to wait for
	 * @return the result of the task
	 * @throws NaftahBugError if the task throws an exception
	 */
	@NaftahFn(
				name = "انتظر",
				description = """
								ينتظر اكتمال المهمة ويعيد نتيجتها.
								""",
				usage = """
						ثابت شغل تعيين تشغيل دوال:الحزم::حصول_على_عنصر([1 , 2 , 3], 1)
						دوال:الخيوط::انتظر(شغل)
						""",
				parameterTypes = {Task.class},
				returnType = Object.class
	)
	public static Object await(Task<?> task) {
		try {
			var result = task.await();
			if (isSimpleOrBuiltinOrCollectionOrMapOfSimpleType(result)) {
				return result;
			}
			else {
				return NaftahObject.of(result);
			}
		}
		catch (Throwable th) {
			throw new NaftahBugError(th);
		}
	}

	/**
	 * Checks whether the specified task is completed.
	 *
	 * @param task the task to query
	 * @return {@code true} if the task is done, {@code false} otherwise
	 */
	@NaftahFn(
				name = "مكتملة",
				description = "يتحقق مما إذا كانت المهمة قد اكتملت.",
				usage = "دوال:الخيوط::مكتملة(شغل)",
				parameterTypes = {Task.class},
				returnType = boolean.class
	)
	public static boolean isDone(Task<?> task) {
		return task.isDone();
	}

	/**
	 * Checks whether the specified task has been cancelled.
	 *
	 * @param task the task to query
	 * @return {@code true} if the task was cancelled, {@code false} otherwise
	 */
	@NaftahFn(
				name = "ملغية",
				description = "يتحقق مما إذا كانت المهمة ملغية.",
				usage = "دوال:الخيوط::ملغية(شغل)",
				parameterTypes = {Task.class},
				returnType = boolean.class
	)
	public static boolean isCancelled(Task<?> task) {
		return task.isCancelled();
	}

	/**
	 * Cancels the specified task.
	 *
	 * @param task      the task to cancel
	 * @param interrupt {@code true} to interrupt if running, {@code false} otherwise
	 * @return {@code true} if the task was cancelled, {@code false} otherwise
	 */
	@NaftahFn(
				name = "الغاء_شغل",
				description = "يلغي المهمة (مع خيار مقاطعتها إذا كانت تعمل).",
				usage = "دوال:الخيوط::الغاء_شغل(شغل, صحيح)",
				parameterTypes = {Task.class, boolean.class},
				returnType = boolean.class
	)
	public static boolean cancel(Task<?> task, boolean interrupt) {
		return task.cancel(interrupt);
	}

	/**
	 * Returns the unique ID of the specified task.
	 *
	 * @param task the task to query
	 * @return the task's unique ID
	 */
	@NaftahFn(
				name = "معرف_شغل",
				aliases = {"معرف_المهمة"},
				description = "يعيد المعرّف الفريد للمهمة.",
				usage = """
						ثابت شغل تعيين تشغيل دوال:الحزم::حصول_على_عنصر([1 , 2 , 3], 1)
						دوال:الخيوط::معرف_شغل(شغل)
						""",
				parameterTypes = {Task.class},
				returnType = long.class
	)
	public static long getTaskId(Task<?> task) {
		return task.getTaskId();
	}

	/**
	 * Stops the specified task by interrupting its executing thread.
	 *
	 * @param task the task to stop
	 */
	@NaftahFn(
				name = "اوقف_المهمة",
				description = "يقاطع خيط المهمة.",
				usage = "دوال:الخيوط::اوقف_المهمة(شغل)",
				parameterTypes = {Task.class},
				returnType = void.class
	)
	public static void stopTask(Task<?> task) {
		task.stop();
	}

	/**
	 * Waits for the specified task's thread to finish execution.
	 *
	 * @param task the task to join
	 * @throws NaftahBugError if the current thread is interrupted while waiting
	 */
	@NaftahFn(
				name = "انتظر_خيط_المهمة",
				description = "ينتظر انتهاء خيط المهمة.",
				usage = "دوال:الخيوط::انتظر_خيط_المهمة(t)",
				parameterTypes = {Task.class},
				returnType = void.class
	)
	public static void joinTask(Task<?> task) {
		try {
			task.join();
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new NaftahBugError("تمت مقاطعة الانتظار لانتهاء المهمة", e);
		}
	}

	/**
	 * Checks whether the specified task's thread is still alive.
	 *
	 * @param task the task to query
	 * @return {@code true} if the task thread is alive, {@code false} otherwise
	 */
	@NaftahFn(
				name = "المهمة_حية",
				description = "يتحقق مما إذا كان خيط المهمة ما يزال يعمل.",
				usage = "دوال:الخيوط::المهمة_حية(شغل)",
				parameterTypes = {Task.class},
				returnType = boolean.class
	)
	public static boolean isTaskAlive(Task<?> task) {
		return task.isAlive();
	}

	/**
	 * Returns the thread executing the specified task.
	 *
	 * @param task the task to query
	 * @return the task's executing thread
	 */
	@NaftahFn(
				name = "خيط_المهمة",
				description = "يعيد الخيط الذي تنفذ عليه المهمة.",
				usage = "دوال:الخيوط::خيط_المهمة(شغل)",
				parameterTypes = {Task.class},
				returnType = Thread.class
	)
	public static NaftahObject getTaskThread(Task<?> task) {
		return NaftahObject.of(task.getThread());
	}

	/**
	 * Creates a new channel with the specified name.
	 *
	 * @param name the name of the channel
	 * @return a new {@link Channel} instance
	 */
	@NaftahFn(
				name = "إنشاء_قناة",
				description = "ينشئ قناة جديدة للتواصل بين الخيوط.",
				usage = "ثابت قناة_ = دوال:الخيوط::إنشاء_قناة(\"أوامر\")",
				parameterTypes = {String.class},
				returnType = Channel.class
	)
	public static Channel<Object> createChannel(String name) {
		return Channel.of(name);
	}

	/**
	 * Sends a value to the specified channel. This operation blocks if the channel is full.
	 *
	 * @param channel the channel to send to
	 * @param value   the value to send
	 * @throws NaftahBugError if the thread is interrupted while sending
	 */
	@NaftahFn(
				name = "ارسل",
				description = "يرسل قيمة إلى القناة. العملية تحجب الخيط إذا كانت القناة ممتلئة.",
				usage = "دوال:الخيوط::ارسل(قناة_, 10)",
				parameterTypes = {Channel.class, Object.class},
				returnType = void.class
	)
	public static void send(Channel<Object> channel, Object value) {
		try {
			channel.send(value);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new NaftahBugError("تمت مقاطعة الخيط أثناء الإرسال إلى القناة");
		}
	}

	/**
	 * Receives a value from the specified channel. This operation blocks if the channel is empty.
	 *
	 * @param channel the channel to receive from
	 * @return the received value
	 * @throws NaftahBugError if the thread is interrupted while receiving
	 */
	@NaftahFn(
				name = "استقبل",
				description = "يستقبل قيمة من القناة. العملية تحجب الخيط إذا كانت القناة فارغة.",
				usage = "دوال:الخيوط::استقبل(قناة_)",
				parameterTypes = {Channel.class},
				returnType = Object.class
	)
	public static Object receive(Channel<Object> channel) {
		try {
			var result = channel.receive();
			if (isSimpleOrBuiltinOrCollectionOrMapOfSimpleType(result)) {
				return result;
			}
			else {
				return NaftahObject.of(result);
			}
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new NaftahBugError("تمت مقاطعة الخيط أثناء الانتظار لاستقبال من القناة");
		}
	}

	/**
	 * Returns the name of the specified channel.
	 *
	 * @param channel the channel to query
	 * @return the channel's name
	 */
	@NaftahFn(
				name = "اسم_القناة",
				description = "يعيد اسم القناة.",
				usage = "دوال:الخيوط::اسم_القناة(قناة_)",
				parameterTypes = {Channel.class},
				returnType = String.class
	)
	public static String channelName(Channel<?> channel) {
		return channel.getName();
	}

	/**
	 * Sends a message to the specified actor asynchronously.
	 *
	 * @param actor the actor to send to
	 * @param msg   the message to send
	 * @return {@code true} if the message was accepted, {@code false} otherwise
	 */
	@NaftahFn(
				name = "ارسل_للممثل",
				description = "يرسل رسالة إلى الممثل بشكلٍ غير حاجز وضعها في صندوق بريده.",
				usage = """
						دوال:الخيوط::ارسل_للممثل(الممثل, "مرحبا")
						""",
				parameterTypes = {Actor.class, Object.class},
				returnType = boolean.class
	)
	public static boolean sendToActor(Actor<Object> actor, Object msg) {
		return actor.send(msg);
	}

	/**
	 * Stops the specified actor gracefully.
	 *
	 * @param actor the actor to stop
	 */
	@NaftahFn(
				name = "اوقف_الممثل",
				description = "يوقف الممثل بنعومة عبر تغيير حالته ومقاطعة خيطه.",
				usage = "دوال:الخيوط::اوقف_الممثل(الممثل)",
				parameterTypes = {Actor.class},
				returnType = void.class
	)
	public static void stopActor(Actor<?> actor) {
		actor.stop();
	}

	/**
	 * Waits for the actor's thread to finish execution.
	 *
	 * @param actor the actor to join
	 * @throws NaftahBugError if the current thread is interrupted while waiting
	 */
	@NaftahFn(
				name = "انتظر_الممثل",
				description = "ينتظر انتهاء خيط الممثل من التنفيذ.",
				usage = "دوال:الخيوط::انتظر_الممثل(الممثل)",
				parameterTypes = Actor.class,
				returnType = void.class
	)
	public static void joinActor(Actor<?> actor) {
		try {
			actor.join();
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new NaftahBugError("تمت مقاطعة الانتظار لانتهاء الممثل", e);
		}
	}

	/**
	 * Checks whether the actor's thread is alive.
	 *
	 * @param actor the actor to query
	 * @return {@code true} if the actor thread is alive, {@code false} otherwise
	 */
	@NaftahFn(
				name = "ممثل_حي",
				description = "يتحقق مما إذا كان خيط الممثل ما يزال يعمل.",
				usage = "دوال:الخيوط::ممثل_حي(الممثل)",
				parameterTypes = Actor.class,
				returnType = boolean.class
	)
	public static boolean isActorAlive(Actor<?> actor) {
		return actor.isAlive();
	}

	/**
	 * Checks whether the actor is currently running.
	 *
	 * @param actor the actor to query
	 * @return {@code true} if the actor is running, {@code false} otherwise
	 */
	@NaftahFn(
				name = "ممثل_يعمل",
				description = "يتحقق مما إذا كان الممثل ما يزال في حالة تشغيل.",
				usage = "دوال:الخيوط::ممثل_يعمل(الممثل)",
				parameterTypes = Actor.class,
				returnType = boolean.class
	)
	public static boolean isActorRunning(Actor<?> actor) {
		return actor.isRunning();
	}

	/**
	 * Returns the name of the actor.
	 *
	 * @param actor the actor to query
	 * @return the actor's name
	 */
	@NaftahFn(
				name = "اسم_الممثل",
				description = "يعيد اسم الممثل.",
				usage = "دوال:الخيوط::اسم_الممثل(الممثل)",
				parameterTypes = Actor.class,
				returnType = String.class
	)
	public static String getActorName(Actor<?> actor) {
		return actor.getName();
	}

	/**
	 * Returns the thread executing the actor.
	 *
	 * @param actor the actor to query
	 * @return the actor's thread
	 */
	@NaftahFn(
				name = "خيط_الممثل",
				description = "يعيد الخيط الذي يعمل عليه الممثل.",
				usage = "دوال:الخيوط::خيط_الممثل(الممثل)",
				parameterTypes = Actor.class,
				returnType = Thread.class
	)
	public static NaftahObject getActorThread(Actor<?> actor) {
		return NaftahObject.of(actor.getThread());
	}
}
