package org.daiitech.naftah.builtin.functions;

import java.util.Map;

import org.daiitech.naftah.NaftahSystem;
import org.daiitech.naftah.builtin.NaftahFn;
import org.daiitech.naftah.builtin.NaftahFnProvider;
import org.daiitech.naftah.builtin.lang.NaftahObject;
import org.daiitech.naftah.errors.NaftahBugError;

import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;
import static org.daiitech.naftah.utils.OS.OS_NAME_PROPERTY;

/**
 * <p><b>System Built-in Functions</b></p>
 *
 * <p>
 * This class provides a set of built-in functions derived from the Java {@link System} class.
 * These functions expose system-level features such as environment access, system properties,
 * current time retrieval, and runtime information to the Naftah scripting environment.
 * </p>
 *
 * <p>
 * All functions are registered under the Arabic names provided in their {@link NaftahFn} annotations.
 * </p>
 *
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * الوقت_الحالي_بالملي()
 * اقرأ_متغير_بيئة("JAVA_HOME")
 * اقرأ_خاصية("os.name")
 * اسم_النظام()
 * }</pre>
 *
 * @author Chakib Daii
 */
@NaftahFnProvider(
					name = "دوال النظام",
					description = """
									يحتوي هذا الموفر على دوال مدمجة مأخوذة من Java System class، مثل الوقت الحالي وقراءة متغيرات البيئة.
									""",
					functionNames = {
										"الوقت_الحالي_بالملي",
										"وقت_التنفيذ",
										"اقرأ_متغير_بيئة",
										"اقرأ_خاصية",
										"اكتب_خاصية",
										"نسخة_جافا",
										"اسم_النظام",
										"إسم_المستخدم",
										"مجلد_المستخدم",
										"إصدار_نفطه",
										"إصدار_قصير_نفطه"
					}
)
public final class SystemBuiltinFunctions {

	/**
	 * Private constructor to prevent instantiation.
	 * Throws {@link NaftahBugError} if called.
	 */
	private SystemBuiltinFunctions() {
		throw newNaftahBugInvalidUsageError();
	}


	/**
	 * Returns the current time in milliseconds since the Unix epoch (January 1, 1970 UTC).
	 * <p>
	 * This function is equivalent to {@link System#currentTimeMillis()} and is often used
	 * for measuring time intervals or timestamps.
	 * </p>
	 *
	 * @return the current system time in milliseconds
	 */
	@NaftahFn(
				name = "الوقت_الحالي_بالملي",
				aliases = {"الآن", "توقيت_النظام"},
				description = """
								تُعيد الوقت الحالي (بالميلي ثانية) منذ بداية العصر Unix Epoch.
								يمكن استخدامها لقياس الزمن أو توقيت الأحداث.
								""",
				usage = "الوقت_الحالي_بالملي()",
				returnType = long.class
	)
	public static long currentTimeMillis() {
		return System.currentTimeMillis();
	}

	/**
	 * Returns the current value of the JVM's high-resolution time source, in nanoseconds.
	 * <p>
	 * This value is only meaningful when comparing two calls to measure elapsed time,
	 * not as an absolute timestamp.
	 * </p>
	 *
	 * @return the current high-resolution time in nanoseconds
	 */
	@NaftahFn(
				name = "وقت_التنفيذ",
				aliases = {"نانو_زمن", "نانو_تايم"},
				description = """
								يُستخدم للحصول على الوقت بدقة عالية (بالنانو ثانية)، مفيد لقياس أداء الكود.
								""",
				usage = "وقت_التنفيذ()",
				returnType = long.class
	)
	public static long nanoTime() {
		return System.nanoTime();
	}

	/**
	 * Retrieves the value of an environment variable.
	 * <p>
	 * This corresponds to {@link System#getenv(String)} and returns {@code null} if the variable
	 * is not defined.
	 * </p>
	 *
	 * @param variableName the name of the environment variable
	 * @return the value of the environment variable, or {@code null} if not found
	 */
	@NaftahFn(
				name = "اقرأ_متغير_بيئة",
				aliases = {"بيئة", "متغير_بيئة"},
				description = """
								تُستخدم لقراءة قيمة متغير من متغيرات البيئة (Environment Variables).
								""",
				usage = "اقرأ_متغير_بيئة(\"JAVA_HOME\")",
				parameterTypes = {String.class},
				returnType = String.class
	)
	public static String getEnv(String variableName) {
		return System.getenv(variableName);
	}

	/**
	 * Retrieves all environment variables as a map.
	 * <p>
	 * This corresponds to {@link System#getenv()} and returns an unmodifiable map
	 * containing all environment variables available to the JVM.
	 * </p>
	 *
	 * @return a map of environment variable names to their values
	 */
	@NaftahFn(
				name = "اقرأ_جميع_متغيرات_البيئة",
				aliases = {"جميع_البيئة", "بيئة", "كل_متغيرات_البيئة"},
				description = """
								تُستخدم للحصول على جميع متغيرات البيئة (Environment Variables)
								على شكل خريطة من أسماء المتغيرات إلى قيمها.
								""",
				usage = "اقرأ_جميع_متغيرات_البيئة()",
				returnType = Map.class
	)
	public static NaftahObject getEnv() {
		return NaftahObject.of(System.getenv());
	}

	/**
	 * Retrieves a system property by its key.
	 * <p>
	 * Examples include:
	 * <ul>
	 * <li>{@code os.name} – operating system name</li>
	 * <li>{@code user.home} – current user’s home directory</li>
	 * <li>{@code java.version} – current Java version</li>
	 * </ul>
	 * </p>
	 *
	 * @param key the property key
	 * @return the property value, or {@code null} if undefined
	 */
	@NaftahFn(
				name = "اقرأ_خاصية",
				aliases = {"خاصية_نظام"},
				description = """
								تُعيد قيمة خاصية نظام بناءً على المفتاح المعطى، مثل 'os.name' أو 'user.home'.
								""",
				usage = "اقرأ_خاصية(\"os.name\")",
				parameterTypes = {String.class},
				returnType = String.class
	)
	public static String getProperty(String key) {
		return System.getProperty(key);
	}

	/**
	 * Retrieves all system properties as a map.
	 * <p>
	 * This corresponds to {@link System#getProperties()} and returns a {@link java.util.Properties} object
	 * containing all system properties available to the JVM.
	 * </p>
	 *
	 * @return a map of system property keys to their values
	 */
	@NaftahFn(
				name = "اقرأ_جميع_الخصائص",
				aliases = {"جميع_الخصائص", "خصائص_النظام", "كل_خصائص_النظام"},
				description = """
								تُستخدم للحصول على جميع خصائص النظام (System Properties)
								على شكل خريطة من المفاتيح إلى القيم.
								""",
				usage = "اقرأ_جميع_الخصائص()",
				returnType = Map.class
	)
	public static NaftahObject getProperties() {
		return NaftahObject.of(System.getProperties());
	}

	/**
	 * Sets a system property to the given key-value pair.
	 *
	 * @param key   the property key
	 * @param value the value to set
	 * @return the previous value of the property, or {@code null} if none
	 */
	@NaftahFn(
				name = "اكتب_خاصية",
				aliases = {"تعيين_خاصية"},
				description = "تُعيّن قيمة لخاصية نظام محددة.",
				usage = "اكتب_خاصية(\"my.prop\", \"value\")",
				parameterTypes = {String.class, String.class},
				returnType = String.class
	)
	public static String setProperty(String key, String value) {
		return System.setProperty(key, value);
	}

	/**
	 * Returns the version of the currently running Java Runtime Environment.
	 *
	 * @return the Java version string (e.g., "17.0.2")
	 */
	@NaftahFn(
				name = "نسخة_جافا",
				aliases = {"إصدار_جافا"},
				description = "يُعيد نسخة Java الحالية قيد التشغيل.",
				usage = "نسخة_جافا()",
				returnType = String.class
	)
	public static String javaVersion() {
		return System.getProperty("java.version");
	}

	/**
	 * Returns the full current version of the Naftah, such as "1.2.3".
	 *
	 * @return the full Naftah version string
	 */
	@NaftahFn(
				name = "إصدار_نفطه",
				description = "تعيد الإصدار الكامل الحالي لنفطه، مثل \"1.2.3\".",
				usage = "إصدار_نفطه()",
				returnType = String.class
	)
	public static String getVersion() {
		return NaftahSystem.getVersion();
	}

	/**
	 * Returns the short Naftah version string, containing only the major and minor parts.
	 * <p>
	 * Example: "1.2" for version "1.2.3".
	 * Throws an exception if the format is invalid.
	 * </p>
	 *
	 * @return the short Naftah version
	 */
	@NaftahFn(
				name = "إصدار_قصير_نفطه",
				description = """
								تعيد شكل الإصدار المختصر لنفطه، يحتوي فقط على الأجزاء الرئيسية والثانوية، مثل "1.2".
								ترمي خطأً إذا كان تنسيق الإصدار غير متوقع.
								""",
				usage = "إصدار_قصير_نفطه()",
				returnType = String.class
	)
	public static String getShortVersion() {
		return NaftahSystem.getShortVersion();
	}


	/**
	 * Returns the current operating system name.
	 *
	 * @return the OS name (e.g., "Windows 11", "Linux", "macOS")
	 */
	@NaftahFn(
				name = "اسم_النظام",
				aliases = {"النظام", "اسم_نظام_التشغيل"},
				description = "يُعيد اسم نظام التشغيل.",
				usage = "اسم_النظام()",
				returnType = String.class
	)
	public static String osName() {
		return System.getProperty(OS_NAME_PROPERTY);
	}

	/**
	 * Returns the name of the current user running the JVM.
	 *
	 * @return the system username
	 */
	@NaftahFn(
				name = "إسم_المستخدم",
				aliases = {"المستخدم", "اسم_المستخدم"},
				description = "يُعيد اسم المستخدم الحالي للنظام.",
				usage = "إسم_المستخدم()",
				returnType = String.class
	)
	public static String userName() {
		return System.getProperty("user.name");
	}

	/**
	 * Returns the current user’s home directory path.
	 *
	 * @return the user home directory path
	 */
	@NaftahFn(
				name = "مجلد_المستخدم",
				aliases = {"دليل_المستخدم"},
				description = "يُعيد مجلد المستخدم الحالي (home directory).",
				usage = "مجلد_المستخدم()",
				returnType = String.class
	)
	public static String userHome() {
		return System.getProperty("user.home");
	}
}
