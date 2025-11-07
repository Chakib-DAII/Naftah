package org.daiitech.naftah.builtin.functions;

import org.daiitech.naftah.builtin.NaftahFn;
import org.daiitech.naftah.builtin.NaftahFnProvider;
import org.daiitech.naftah.errors.NaftahBugError;

import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;

/**
 * <p><b>Runtime Built-in Functions</b></p>
 *
 * <p>
 * This class provides a collection of built-in functions derived from the
 * {@link java.lang.Runtime} API. These functions expose system-level capabilities such as
 * memory management, garbage collection, and process control to the Naftah scripting environment.
 * </p>
 *
 * <p>
 * Each function can be invoked using its Arabic name as defined in the {@link NaftahFn} annotations.
 * </p>
 *
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * اخرج(0)
 * ذاكرة_إجمالية()
 * عدد_المعالجات()
 * جمع_القمامة()
 * }</pre>
 *
 * @author Chakib Daii
 */
@NaftahFnProvider(
					name = "دوال التشغيل",
					description = """
									يحتوي هذا الموفر على دوال مدمجة مأخوذة من Java Runtime class، مثل إدارة الذاكرة وإنهاء البرنامج وجمع القمامة.
									""",
					functionNames = {
										"اخرج",
										"ذاكرة_إجمالية",
										"ذاكرة_قصوى",
										"ذاكرة_متاحة",
										"ذاكرة_مستخدمة",
										"جمع_القمامة",
										"عدد_المعالجات"
					}
)
public final class RuntimeBuiltinFunctions {

	/**
	 * Private constructor to prevent instantiation.
	 * Throws {@link NaftahBugError} if called.
	 */
	private RuntimeBuiltinFunctions() {
		throw newNaftahBugInvalidUsageError();
	}

	/**
	 * Terminates the currently running program with the specified exit code.
	 *
	 * <p>
	 * This method delegates directly to {@link System#exit(int)}.
	 * A code of {@code 0} usually indicates normal termination.
	 * </p>
	 *
	 * <p><b>Example:</b></p>
	 * <pre>{@code اخرج(0)}</pre>
	 *
	 * @param code the exit code (usually 0 or a positive number to indicate an error type)
	 */
	@NaftahFn(
				name = "اخرج",
				aliases = {"إنهاء", "إنهي"},
				description = "يُنهي تنفيذ البرنامج برمز الخروج المحدد.",
				usage = "اخرج(0)",
				parameterTypes = {Number.class}
	)
	public static void exit(Number code) {
		System.exit(code.intValue());
	}

	/**
	 * Returns the total amount of memory currently allocated by the JVM.
	 *
	 * @return the total memory (in bytes) currently allocated.
	 */
	@NaftahFn(
				name = "ذاكرة_إجمالية",
				aliases = {"الذاكرة_الإجمالية", "الذاكرة_الكاملة"},
				description = "تعيد إجمالي حجم الذاكرة التي JVM حجزتها حالياً.",
				usage = "الذاكرة_الإجمالية()",
				returnType = long.class
	)
	public static long totalMemory() {
		return Runtime.getRuntime().totalMemory();
	}

	/**
	 * Returns the maximum amount of memory that the JVM will attempt to use.
	 *
	 * @return the maximum memory (in bytes) available to the JVM.
	 */
	@NaftahFn(
				name = "ذاكرة_قصوى",
				aliases = {"الذاكرة_القصوى"},
				description = "تُعيد الحجم الأقصى للذاكرة المتاحة للآلة الافتراضية (JVM).",
				usage = "ذاكرة_قصوى()",
				returnType = long.class
	)
	public static long maxMemory() {
		return Runtime.getRuntime().maxMemory();
	}

	/**
	 * Returns the amount of free (unused) memory currently available to the JVM.
	 *
	 * @return the amount of free memory (in bytes).
	 */
	@NaftahFn(
				name = "ذاكرة_متاحة",
				aliases = {"الذاكرة_المتاحة"},
				description = "تُعيد مقدار الذاكرة الحرة (غير المستخدمة) حالياً.",
				usage = "ذاكرة_متاحة()",
				returnType = long.class
	)
	public static long freeMemory() {
		return Runtime.getRuntime().freeMemory();
	}

	/**
	 * Calculates the amount of memory currently in use by the JVM.
	 *
	 * <p>Computed as: {@code totalMemory() - freeMemory()}.</p>
	 *
	 * @return the amount of used memory (in bytes).
	 */
	@NaftahFn(
				name = "ذاكرة_مستخدمة",
				aliases = {"الذاكرة_المستخدمة", "الذاكرة_المستهلكة"},
				description = "تحسب مقدار الذاكرة المستخدمة من قبل JVM حالياً.",
				usage = "ذاكرة_مستخدمة()",
				returnType = long.class
	)
	public static long usedMemory() {
		Runtime r = Runtime.getRuntime();
		return r.totalMemory() - r.freeMemory();
	}

	/**
	 * Requests that the Java Virtual Machine perform garbage collection.
	 *
	 * <p>
	 * This method only suggests that the JVM initiate garbage collection and does not guarantee
	 * that it will happen immediately or at all.
	 * </p>
	 */
	@NaftahFn(
				name = "جمع_القمامة",
				aliases = {"تحرير_الذاكرة"},
				description = "يطلب من JVM تنفيذ جمع القمامة لتحرير الذاكرة.",
				usage = "جمع_القمامة()"
	)
	public static void runGarbageCollector() {
		System.gc();
	}

	/**
	 * Returns the number of processor cores available to the JVM.
	 *
	 * <p>
	 * This typically reflects the number of logical processors available on the host machine.
	 * </p>
	 *
	 * @return the number of available processors.
	 */
	@NaftahFn(
				name = "عدد_المعالجات",
				aliases = {"عدد_الأنوية", "المعالجات_المتاحة"},
				description = "تعيد عدد أنوية المعالج المتاحة للبرنامج.",
				usage = "عدد_المعالجات()",
				returnType = int.class
	)
	public static int availableProcessors() {
		return Runtime.getRuntime().availableProcessors();
	}
}
