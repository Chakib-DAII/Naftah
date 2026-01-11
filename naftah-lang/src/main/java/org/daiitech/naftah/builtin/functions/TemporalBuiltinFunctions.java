package org.daiitech.naftah.builtin.functions;


import org.daiitech.naftah.builtin.NaftahFn;
import org.daiitech.naftah.builtin.NaftahFnProvider;
import org.daiitech.naftah.builtin.time.DateSupport;
import org.daiitech.naftah.builtin.time.NaftahDate;
import org.daiitech.naftah.builtin.time.NaftahDateTime;
import org.daiitech.naftah.builtin.time.NaftahDuration;
import org.daiitech.naftah.builtin.time.NaftahPeriod;
import org.daiitech.naftah.builtin.time.NaftahPeriodWithDuration;
import org.daiitech.naftah.builtin.time.NaftahTemporalAmount;
import org.daiitech.naftah.builtin.time.NaftahTemporalPoint;
import org.daiitech.naftah.builtin.time.NaftahTime;
import org.daiitech.naftah.builtin.time.TimeSupport;
import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.parser.time.NaftahDateParserHelper;
import org.daiitech.naftah.utils.time.ChronologyUtils;

import static org.daiitech.naftah.errors.ExceptionUtils.newIllegalArgumentException;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;

/**
 * Provider for built-in temporal functions.
 * Contains functions to create, manipulate, and query durations, periods,
 * and period-with-duration objects.
 * <p>
 * All functions are static; this class cannot be instantiated.
 * </p>
 *
 * @author Chakib Daii
 */
@NaftahFnProvider(
					name = "دوال الزمن",
					description = """
									يحتوي هذا الموفر على دوال مدمجة للتعامل مع الفترات، المدد، والنقاط الزمنية ضمن لغة نفطه بدقة وكفاءة.
									""",
					functionNames = {
										"أنشئ_مدة_من_أيام",
										"أنشئ_مدة_من_ساعات",
										"أنشئ_مدة_من_دقائق",
										"أنشئ_مدة_من_ثواني",
										"أنشئ_مدة_من_ثواني_مع_نانو",
										"أنشئ_مدة_من_مللي",
										"أنشئ_مدة_من_نانو",
										"أنشئ_مدة_صفرية",
										"أنشئ_فترة_صفرية",
										"أنشئ_فترة_من_سنوات",
										"أنشئ_فترة_من_شهور",
										"أنشئ_فترة_من_أسابيع",
										"أنشئ_فترة_من_أيام",
										"أنشئ_فترة_ومدة_صفرية",
										"أنشئ_فترة_ومدة_من_سنوات",
										"أنشئ_فترة_ومدة_من_شهور",
										"أنشئ_فترة_ومدة_من_أسابيع",
										"أنشئ_فترة_ومدة_من_أيام",
										"أنشئ_فترة_ومدة_من_ساعات",
										"أنشئ_فترة_ومدة_من_دقائق",
										"أنشئ_فترة_ومدة_من_ثواني",
										"أنشئ_فترة_ومدة_من_ثواني_مع_نانو",
										"أنشئ_فترة_ومدة_من_مللي",
										"أنشئ_فترة_ومدة_من_نانو",
										"أضف_سنوات_لفترة",
										"أضف_أشهر_لفترة",
										"أضف_أيام_لفترة",
										"اطرح_سنوات_من_الفترة",
										"اطرح_أشهر_من_الفترة",
										"اطرح_أيام_من_الفترة",
										"أضف_أيام_للمدة",
										"أضف_ساعات_للمدة",
										"أضف_دقائق_للمدة",
										"أضف_ثواني_للمدة",
										"أضف_مللي_للمدة",
										"أضف_نانو_للمدة",
										"اطرح_أيام_من_المدة",
										"اطرح_ساعات_من_المدة",
										"اطرح_دقائق_من_المدة",
										"اطرح_ثواني_من_المدة",
										"اطرح_مللي_من_المدة",
										"اطرح_نانو_من_المدة",
										"أضف_سنوات_لفترة_مع_مدة",
										"أضف_أشهر_لفترة_مع_مدة",
										"أضف_أيام_لفترة_مع_مدة",
										"أضف_ساعات_لفترة_مع_مدة",
										"أضف_دقائق_لفترة_مع_مدة",
										"أضف_ثواني_لفترة_مع_مدة",
										"أضف_مللي_لفترة_مع_مدة",
										"أضف_نانو_لفترة_مع_مدة",
										"اطرح_سنوات_من_الفترة_مع_مدة",
										"اطرح_أشهر_من_الفترة_مع_مدة",
										"اطرح_أيام_من_الفترة_مع_مدة",
										"اطرح_ساعات_من_الفترة_مع_مدة",
										"اطرح_دقائق_من_الفترة_مع_مدة",
										"اطرح_ثواني_من_الفترة_مع_مدة",
										"اطرح_مللي_من_الفترة_مع_مدة",
										"اطرح_نانو_من_الفترة_مع_مدة",
										"احصل_على_السنة",
										"احصل_على_الشهر",
										"احصل_على_اليوم",
										"احصل_على_الساعة",
										"احصل_على_الدقيقة",
										"احصل_على_الثانية",
										"احصل_على_الملي_ثانية",
										"احصل_على_النانو_ثانية",
										"احصل_على_سنوات_الفترة",
										"احصل_على_أشهر_الفترة",
										"احصل_على_أيام_الفترة",
										"احصل_على_ساعات_المدة",
										"احصل_على_دقائق_المدة",
										"احصل_على_ثواني_المدة",
										"احصل_على_مللي_المدة",
										"احصل_على_نانو_المدة",
										"احصل_على_سنوات_الفترة_مع_مدة",
										"احصل_على_أشهر_الفترة_مع_مدة",
										"احصل_على_أيام_الفترة_مع_مدة",
										"احصل_على_ساعات_الفترة_مع_مدة",
										"احصل_على_دقائق_الفترة_مع_مدة",
										"احصل_على_ثواني_الفترة_مع_مدة",
										"احصل_على_مللي_الفترة_مع_مدة",
										"احصل_على_نانو_الفترة_مع_مدة",
										"أنشئ_مدة_بين_نقطتين",
										"الوقت_الحالي",
										"الوقت_الحالي_بتوقيت",
										"الوقت_الحالي_بإزاحة",
										"التاريخ_الحالي",
										"التاريخ_الحالي_بتقويم",
										"التاريخ_الحالي_بتوقيت",
										"التاريخ_الحالي_بإزاحة",
										"التاريخ_الحالي_بتقويم_وتوقيت",
										"التاريخ_الحالي_بتقويم_وإزاحة",
										"التاريخ_والوقت_الحالي",
										"التاريخ_والوقت_الحالي_بتقويم",
										"التاريخ_والوقت_الحالي_بتوقيت",
										"التاريخ_والوقت_الحالي_بإزاحة",
										"التاريخ_والوقت_الحالي_بتقويم_وتوقيت",
										"التاريخ_والوقت_الحالي_بتقويم_وإزاحة",
										"انشاء_الوقت",
										"انشاء_الوقت_بتوقيت",
										"انشاء_الوقت_بإزاحة",
										"انشاء_الوقت_مع_ثانية",
										"انشاء_الوقت_بتوقيت_مع_ثانية",
										"انشاء_الوقت_بإزاحة_مع_ثانية",
										"انشاء_الوقت_مع_نانوثانية",
										"انشاء_الوقت_مع_نانوثانية_بتوقيت",
										"انشاء_الوقت_مع_الإزاحة_ونانوثانية",
										"انشاء_الوقت_من_ثواني_اليوم",
										"انشاء_الوقت_من_ثواني_اليوم_بتوقيت",
										"انشاء_الوقت_من_ثواني_اليوم_بإزاحة",
										"انشاء_الوقت_من_نانوثواني_اليوم",
										"انشاء_الوقت_من_نانوثواني_اليوم_بتوقيت",
										"انشاء_الوقت_من_نانوثواني_اليوم_بإزاحة",
										"انشاء_التاريخ_من_اليوم_و_شهر_و_سنة",
										"انشاء_التاريخ_من_اليوم_و_شهر_و_سنة_بتقويم",
										"انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة",
										"انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة_بتوقيت",
										"انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة_بإزاحة",
										"انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة_وثانية",
										"انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة_وثانية_بتوقيت",
										"انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة_وثانية_بإزاحة",
										"انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة_وثانية_ونانوثانية",
										"انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة_وثانية_بتوقيت",
										"انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة_وثانية_بإزاحة",
										"انشاء_التاريخ_والوقت_بتقويم_وساعة_ودقيقة",
										"انشاء_التاريخ_والوقت_بتقويم_وساعة_ودقيقة_بتوقيت",
										"انشاء_التاريخ_والوقت_بتقويم_وساعة_ودقيقة_بإزاحة",
										"انشاء_التاريخ_والوقت_بتقويم_وساعة_ودقيقة_وثانية",
										"انشاء_التاريخ_والوقت_بتقويم_وثانية_وبتوقيت",
										"انشاء_التاريخ_والوقت_بتقويم_وثانية_بإزاحة",
										"انشاء_التاريخ_والوقت_بتقويم_وثانية_ونانوثانية",
										"انشاء_التاريخ_والوقت_بتقويم_وثانية_ونانوثانية_بتوقيت",
										"انشاء_التاريخ_والوقت_بتقويم_وثانية_ونانوثانية_بإزاحة",
										"تحويل_الى_يوم_منذ_الحقبة",
										"احصل_على_السنة",
										"احصل_على_رقم_الشهر",
										"احصل_على_الشهر",
										"احصل_على_اليوم",
										"احصل_على_يوم_السنة",
										"احصل_على_يوم_الأسبوع",
										"هل_السنة_كبيسة",
										"عدد_ايام_الشهر",
										"عدد_ايام_السنة",
										"اضافة_سنوات",
										"اضافة_اشهر",
										"اضافة_اسابيع",
										"اضافة_ايام",
										"طرح_سنوات",
										"طرح_شهور",
										"طرح_اسابيع",
										"طرح_ايام",
										"احصل_على_الساعة",
										"احصل_على_الدقيقة",
										"احصل_على_الثانية",
										"احصل_على_الملي_ثانية",
										"احصل_على_النانو_ثانية",
										"اضافة_ساعات",
										"اضافة_دقائق",
										"اضافة_ثواني",
										"اضافة_نانوثواني",
										"طرح_ساعات",
										"طرح_دقائق",
										"طرح_ثواني",
										"طرح_نانوثواني"
					}
)
public final class TemporalBuiltinFunctions {
	/**
	 * Private constructor to prevent instantiation.
	 * Throws {@link NaftahBugError} if called.
	 */
	private TemporalBuiltinFunctions() {
		throw newNaftahBugInvalidUsageError();
	}

	/**
	 * Obtains the current time using the system default time zone.
	 *
	 * @return the current {@link NaftahTime}
	 */
	@NaftahFn(
				name = "الوقت_الحالي",
				description = "الحصول على الوقت الحالي",
				usage = "الوقت_الحالي()",
				returnType = NaftahTime.class
	)
	public static NaftahTime currentTime() {
		return NaftahTime.now();
	}

	/**
	 * Obtains the current time using the specified time zone.
	 *
	 * @param zone the zone ID (e.g. Asia/Dubai)
	 * @return the current {@link NaftahTime}
	 */
	@NaftahFn(
				name = "الوقت_الحالي_بتوقيت",
				aliases = {"الوقت_الحالي"},
				description = "الحصول على الوقت الحالي بتوقيت محدد",
				usage = "الوقت_الحالي_بتوقيت(منطقة)",
				parameterTypes = {String.class},
				returnType = NaftahTime.class
	)
	public static NaftahTime currentZonedTime(String zone) {
		return NaftahTime.now(NaftahTime.ZoneOrOffset.ofZone(zone));
	}

	/**
	 * Obtains the current time using a fixed offset.
	 *
	 * @param offset the offset (e.g. +04:00)
	 * @return the current {@link NaftahTime}
	 */
	@NaftahFn(
				name = "الوقت_الحالي_بإزاحة",
				aliases = {"الوقت_الحالي"},
				description = "الحصول على الوقت الحالي بإزاحة زمنية",
				usage = "الوقت_الحالي_بإزاحة(إزاحة)",
				parameterTypes = {String.class},
				returnType = NaftahTime.class
	)
	public static NaftahTime currentOffsetTime(String offset) {
		return NaftahTime.now(NaftahTime.ZoneOrOffset.ofOffset(offset));
	}

	/**
	 * Creates an {@link NaftahTime} instance from hour and minute.
	 *
	 * @param hour   the hour value
	 * @param minute the minute value
	 * @return a new {@link NaftahTime} instance
	 */
	@NaftahFn(
				name = "انشاء_الوقت",
				description = "انشاء الوقت من ساعة ودقيقة",
				usage = "انشاء_الوقت(ساعة, دقيقة)",
				parameterTypes = {Number.class, Number.class},
				returnType = NaftahTime.class
	)
	public static NaftahTime createTime(Number hour, Number minute) {
		return NaftahTime.of(hour.intValue(), minute.intValue());
	}

	/**
	 * Creates a zoned {@link NaftahTime} instance from hour, minute, and time zone.
	 *
	 * @param hour   the hour value
	 * @param minute the minute value
	 * @param zone   the time zone ID (e.g. "Asia/Riyadh")
	 * @return a new {@link NaftahTime} instance with the specified zone
	 */
	@NaftahFn(
				name = "انشاء_الوقت_بتوقيت",
				aliases = {"انشاء_الوقت"},
				description = "انشاء الوقت مع المنطقة الزمنية",
				usage = "انشاء_الوقت_بتوقيت(ساعة, دقيقة, المنطقة)",
				parameterTypes = {Number.class, Number.class, String.class},
				returnType = NaftahTime.class
	)
	public static NaftahTime createZonedTime(Number hour, Number minute, String zone) {
		return NaftahTime.of(hour.intValue(), minute.intValue(), NaftahTime.ZoneOrOffset.ofZone(zone));
	}

	/**
	 * Creates an offset {@link NaftahTime} instance from hour, minute, and offset.
	 *
	 * @param hour   the hour value
	 * @param minute the minute value
	 * @param offset the offset string (e.g. "+04:00")
	 * @return a new {@link NaftahTime} instance with the specified offset
	 */
	@NaftahFn(
				name = "انشاء_الوقت_بإزاحة",
				aliases = {"انشاء_الوقت"},
				description = "انشاء الوقت مع الإزاحة الزمنية",
				usage = "انشاء_الوقت_بإزاحة(ساعة, دقيقة, الإزاحة)",
				parameterTypes = {Number.class, Number.class, String.class},
				returnType = NaftahTime.class
	)
	public static NaftahTime createOffsetTime(Number hour, Number minute, String offset) {
		return NaftahTime.of(hour.intValue(), minute.intValue(), NaftahTime.ZoneOrOffset.ofOffset(offset));
	}

	/**
	 * Creates an {@link NaftahTime} instance from hour, minute, and second.
	 *
	 * @param hour   the hour value
	 * @param minute the minute value
	 * @param second the second value
	 * @return a new {@link NaftahTime} instance
	 */
	@NaftahFn(
				name = "انشاء_الوقت_مع_ثانية",
				aliases = {"انشاء_الوقت"},
				description = "انشاء الوقت من ساعة ودقيقة وثانية",
				usage = "انشاء_الوقت_مع_ثانية(ساعة, دقيقة, ثانية)",
				parameterTypes = {Number.class, Number.class, Number.class},
				returnType = NaftahTime.class
	)
	public static NaftahTime createTime(Number hour, Number minute, Number second) {
		return NaftahTime.of(hour.intValue(), minute.intValue(), second.intValue());
	}

	/**
	 * Creates a zoned {@link NaftahTime} instance from hour, minute, second, and zone.
	 *
	 * @param hour   the hour value
	 * @param minute the minute value
	 * @param second the second value
	 * @param zone   the time zone ID
	 * @return a new {@link NaftahTime} instance with the specified zone
	 */
	@NaftahFn(
				name = "انشاء_الوقت_بتوقيت_مع_ثانية",
				aliases = {"انشاء_الوقت"},
				description = "انشاء الوقت مع المنطقة الزمنية والثانية",
				usage = "انشاء_الوقت_بتوقيت_مع_ثانية(ساعة, دقيقة, ثانية, المنطقة)",
				parameterTypes = {Number.class, Number.class, Number.class, String.class},
				returnType = NaftahTime.class
	)
	public static NaftahTime createZonedTime(Number hour, Number minute, Number second, String zone) {
		return NaftahTime
				.of(hour.intValue(),
					minute.intValue(),
					second.intValue(),
					NaftahTime.ZoneOrOffset.ofZone(zone));
	}

	/**
	 * Creates an offset {@link NaftahTime} instance from hour, minute, second, and offset.
	 *
	 * @param hour   the hour value
	 * @param minute the minute value
	 * @param second the second value
	 * @param offset the offset string
	 * @return a new {@link NaftahTime} instance with the specified offset
	 */
	@NaftahFn(
				name = "انشاء_الوقت_بإزاحة_مع_ثانية",
				aliases = {"انشاء_الوقت"},
				description = "انشاء الوقت مع الإزاحة الزمنية والثانية",
				usage = "انشاء_الوقت_بإزاحة_مع_ثانية(ساعة, دقيقة, ثانية, الإزاحة)",
				parameterTypes = {Number.class, Number.class, Number.class, String.class},
				returnType = NaftahTime.class
	)
	public static NaftahTime createOffsetTime(Number hour, Number minute, Number second, String offset) {
		return NaftahTime
				.of(hour.intValue(),
					minute.intValue(),
					second.intValue(),
					NaftahTime.ZoneOrOffset.ofOffset(offset));
	}

	/**
	 * Creates an {@link NaftahTime} instance from hour, minute, second, and nanosecond.
	 *
	 * @param hour         the hour value
	 * @param minute       the minute value
	 * @param second       the second value
	 * @param nanoOfSecond the nanosecond value
	 * @return a new {@link NaftahTime} instance
	 */
	@NaftahFn(
				name = "انشاء_الوقت_مع_نانوثانية",
				aliases = {"انشاء_الوقت"},
				description = "انشاء الوقت من ساعة ودقيقة وثانية ونانوثانية",
				usage = "انشاء_الوقت_مع_نانوثانية(ساعة, دقيقة, ثانية, نانوثانية)",
				parameterTypes = {Number.class, Number.class, Number.class, Number.class},
				returnType = NaftahTime.class
	)
	public static NaftahTime createTime(Number hour, Number minute, Number second, Number nanoOfSecond) {
		return NaftahTime.of(hour.intValue(), minute.intValue(), second.intValue(), nanoOfSecond.intValue());
	}

	/**
	 * Creates an {@link NaftahTime} instance from hour, minute, second, and nanosecond with a specific time zone.
	 *
	 * @param hour         the hour value
	 * @param minute       the minute value
	 * @param second       the second value
	 * @param nanoOfSecond the nanosecond value
	 * @param zone         the time zone ID (e.g., "Asia/Riyadh")
	 * @return a new {@link NaftahTime} instance with the specified zone
	 */
	@NaftahFn(
				name = "انشاء_الوقت_مع_نانوثانية_بتوقيت",
				aliases = {"انشاء_الوقت"},
				description = "انشاء الوقت من ساعة ودقيقة وثانية ونانوثانية مع منطقة زمنية محددة",
				usage = "انشاء_الوقت_مع_نانوثانية_بتوقيت(ساعة, دقيقة, ثانية, نانوثانية, منطقة)",
				parameterTypes = {Number.class, Number.class, Number.class, Number.class, String.class},
				returnType = NaftahTime.class
	)
	public static NaftahTime createZonedTime(   Number hour,
												Number minute,
												Number second,
												Number nanoOfSecond,
												String zone) {
		return NaftahTime
				.of(hour.intValue(),
					minute.intValue(),
					second.intValue(),
					nanoOfSecond.intValue(),
					NaftahTime.ZoneOrOffset.ofZone(zone));
	}

	/**
	 * Creates an {@link NaftahTime} instance from hour, minute, second, and nanosecond with a specific offset.
	 *
	 * @param hour         the hour value
	 * @param minute       the minute value
	 * @param second       the second value
	 * @param nanoOfSecond the nanosecond value
	 * @param offset       the offset string (e.g., "+03:00")
	 * @return a new {@link NaftahTime} instance with the specified offset
	 */
	@NaftahFn(
				name = "انشاء_الوقت_مع_الإزاحة_ونانوثانية",
				aliases = {"انشاء_الوقت"},
				description = "انشاء الوقت من ساعة ودقيقة وثانية ونانوثانية مع إزاحة زمنية محددة",
				usage = "انشاء_الوقت_مع_الإزاحة_ونانوثانية(ساعة, دقيقة, ثانية, نانوثانية, إزاحة)",
				parameterTypes = {Number.class, Number.class, Number.class, Number.class, String.class},
				returnType = NaftahTime.class
	)
	public static NaftahTime createOffsetTime(  Number hour,
												Number minute,
												Number second,
												Number nanoOfSecond,
												String offset) {
		return NaftahTime
				.of(hour.intValue(),
					minute.intValue(),
					second.intValue(),
					nanoOfSecond.intValue(),
					NaftahTime.ZoneOrOffset.ofOffset(offset));
	}

	/**
	 * Creates an {@link NaftahTime} instance from the total number of seconds since midnight.
	 *
	 * @param secondOfDay the total number of seconds since midnight (0–86,399)
	 * @return a new {@link NaftahTime} instance
	 */
	@NaftahFn(
				name = "انشاء_الوقت_من_ثواني_اليوم",
				aliases = {"انشاء_الوقت"},
				description = "انشاء الوقت من العدد الإجمالي للثواني منذ منتصف الليل",
				usage = "انشاء_الوقت_من_ثواني_اليوم(ثانية_اليوم)",
				parameterTypes = {Number.class},
				returnType = NaftahTime.class
	)
	public static NaftahTime createTimeOfSecondOfDay(Number secondOfDay) {
		return NaftahTime.ofSecondOfDay(secondOfDay.longValue());
	}

	/**
	 * Creates an {@link NaftahTime} instance from the total number of seconds since midnight with a specific time
	 * * zone.
	 *
	 * @param secondOfDay the second-of-day (0–86,399)
	 * @param zone        the time zone ID (e.g., "Asia/Riyadh")
	 * @return a new {@link NaftahTime} instance with the specified zone
	 */
	@NaftahFn(
				name = "انشاء_الوقت_من_ثواني_اليوم_بتوقيت",
				aliases = {"انشاء_الوقت"},
				description = "انشاء الوقت من عدد الثواني منذ منتصف الليل مع منطقة زمنية محددة",
				usage = "انشاء_الوقت_من_ثواني_اليوم_بتوقيت(ثواني_اليوم, منطقة)",
				parameterTypes = {Number.class, String.class},
				returnType = NaftahTime.class
	)
	public static NaftahTime createZonedTimeOfSecondOfDay(Number secondOfDay, String zone) {
		return NaftahTime.ofSecondOfDay(secondOfDay.longValue(), NaftahTime.ZoneOrOffset.ofZone(zone));
	}

	/**
	 * Creates an {@link NaftahTime} instance from the total number of seconds since midnight with a specific offset.
	 *
	 * @param secondOfDay the second-of-day (0–86,399)
	 * @param offset      the offset (e.g., "+04:00")
	 * @return a new {@link NaftahTime} instance with the specified offset
	 */
	@NaftahFn(
				name = "انشاء_الوقت_من_ثواني_اليوم_بإزاحة",
				aliases = {"انشاء_الوقت"},
				description = "انشاء الوقت من عدد الثواني منذ منتصف الليل مع إزاحة زمنية محددة",
				usage = "انشاء_الوقت_من_ثواني_اليوم_بإزاحة(ثواني_اليوم, إزاحة)",
				parameterTypes = {Number.class, String.class},
				returnType = NaftahTime.class
	)
	public static NaftahTime createOffsetTimeOfSecondOfDay(Number secondOfDay, String offset) {
		return NaftahTime.ofSecondOfDay(secondOfDay.longValue(), NaftahTime.ZoneOrOffset.ofOffset(offset));
	}

	/**
	 * Creates an {@link NaftahTime} instance from the total number of nanoseconds since midnight.
	 *
	 * @param nanoOfDay the nano-of-day (0–86,399,999,999,999)
	 * @return a new {@link NaftahTime} instance
	 */
	@NaftahFn(
				name = "انشاء_الوقت_من_نانوثواني_اليوم",
				aliases = {"انشاء_الوقت"},
				description = "انشاء الوقت من عدد النانوثانية منذ منتصف الليل",
				usage = "انشاء_الوقت_من_نانوثواني_اليوم(نانوثواني_اليوم)",
				parameterTypes = {Number.class},
				returnType = NaftahTime.class
	)
	public static NaftahTime createTimeOfNanoOfDay(Number nanoOfDay) {
		return NaftahTime.ofNanoOfDay(nanoOfDay.longValue());
	}

	/**
	 * Creates an {@link NaftahTime} instance from the total number of nanoseconds since midnight
	 * with a specific time zone.
	 *
	 * @param nanoOfDay the nano-of-day (0–86,399,999,999,999)
	 * @param zone      the time zone ID (e.g., "Asia/Riyadh")
	 * @return a new {@link NaftahTime} instance with the specified zone
	 */
	@NaftahFn(
				name = "انشاء_الوقت_من_نانوثواني_اليوم_بتوقيت",
				aliases = {"انشاء_الوقت"},
				description = "انشاء الوقت من عدد النانوثانية منذ منتصف الليل مع منطقة زمنية محددة",
				usage = "انشاء_الوقت_من_نانوثواني_اليوم_بتوقيت(نانوثواني_اليوم, منطقة)",
				parameterTypes = {Number.class, String.class},
				returnType = NaftahTime.class
	)
	public static NaftahTime createZonedTimeOfNanoOfDay(Number nanoOfDay, String zone) {
		return NaftahTime.ofNanoOfDay(nanoOfDay.longValue(), NaftahTime.ZoneOrOffset.ofZone(zone));
	}

	/**
	 * Creates an {@link NaftahTime} instance from the total number of nanoseconds since midnight
	 * with a fixed offset.
	 *
	 * @param nanoOfDay the nano-of-day (0–86,399,999,999,999)
	 * @param offset    the offset (e.g., "+04:00")
	 * @return a new {@link NaftahTime} instance with the specified offset
	 */
	@NaftahFn(
				name = "انشاء_الوقت_من_نانوثواني_اليوم_بإزاحة",
				aliases = {"انشاء_الوقت"},
				description = "انشاء الوقت من عدد النانوثانية منذ منتصف الليل مع إزاحة زمنية محددة",
				usage = "انشاء_الوقت_من_نانوثواني_اليوم_بإزاحة(نانوثواني_اليوم, إزاحة)",
				parameterTypes = {Number.class, String.class},
				returnType = NaftahTime.class
	)
	public static NaftahTime createOffsetTimeOfNanoOfDay(Number nanoOfDay, String offset) {
		return NaftahTime.ofNanoOfDay(nanoOfDay.longValue(), NaftahTime.ZoneOrOffset.ofOffset(offset));
	}

	/**
	 * Obtains the current date using the default chronology.
	 *
	 * @return the current {@link NaftahDate}
	 */
	@NaftahFn(
				name = "التاريخ_الحالي",
				description = "الحصول على التاريخ الحالي",
				usage = "التاريخ_الحالي()",
				returnType = NaftahDate.class
	)
	public static NaftahDate currentDate() {
		return NaftahDate.now();
	}

	/**
	 * Obtains the current date using the specified chronology.
	 *
	 * @param chronology the chronology name
	 * @return the current {@link NaftahDate}
	 */
	@NaftahFn(
				name = "التاريخ_الحالي_بتقويم",
				aliases = {"التاريخ_الحالي"},
				description = "الحصول على التاريخ الحالي بتقويم محدد",
				usage = "التاريخ_الحالي_بتقويم(تقويم)",
				parameterTypes = {String.class},
				returnType = NaftahDate.class
	)
	public static NaftahDate currentChronologyDate(String chronology) {
		return NaftahDate.now(ChronologyUtils.getChronologyByName(chronology));
	}

	/**
	 * Obtains the current date using the specified time zone.
	 *
	 * @param zone the zone ID (e.g. Asia/Dubai)
	 * @return the current {@link NaftahDate}
	 */
	@NaftahFn(
				name = "التاريخ_الحالي_بتوقيت",
				aliases = {"التاريخ_الحالي"},
				description = "الحصول على التاريخ الحالي بتوقيت محدد",
				usage = "التاريخ_الحالي_بتوقيت(منطقة)",
				parameterTypes = {String.class},
				returnType = NaftahDate.class
	)
	public static NaftahDate currentZonedDate(String zone) {
		return NaftahDate.now(NaftahTime.ZoneOrOffset.ofZone(zone));
	}

	/**
	 * Obtains the current date using a fixed offset.
	 *
	 * @param offset the offset (e.g. +04:00)
	 * @return the current {@link NaftahDate}
	 */
	@NaftahFn(
				name = "التاريخ_الحالي_بإزاحة",
				aliases = {"التاريخ_الحالي"},
				description = "الحصول على التاريخ الحالي بإزاحة زمنية",
				usage = "التاريخ_الحالي_بإزاحة(إزاحة)",
				parameterTypes = {String.class},
				returnType = NaftahDate.class
	)
	public static NaftahDate currentOffsetDate(String offset) {
		return NaftahDate.now(NaftahTime.ZoneOrOffset.ofOffset(offset));
	}

	/**
	 * Obtains the current date using the specified chronology and time zone.
	 *
	 * @param chronology the chronology name
	 * @param zone       the zone ID
	 * @return the current {@link NaftahDate}
	 */
	@NaftahFn(
				name = "التاريخ_الحالي_بتقويم_وتوقيت",
				aliases = {"التاريخ_الحالي"},
				description = "الحصول على التاريخ الحالي بتقويم وتوقيت محددين",
				usage = "التاريخ_الحالي_بتقويم_وتوقيت(تقويم, منطقة)",
				parameterTypes = {String.class, String.class},
				returnType = NaftahDate.class
	)
	public static NaftahDate currentZonedChronologyDate(String chronology, String zone) {
		return NaftahDate.now(ChronologyUtils.getChronologyByName(chronology), NaftahTime.ZoneOrOffset.ofZone(zone));
	}

	/**
	 * Obtains the current date using the specified chronology and offset.
	 *
	 * @param chronology the chronology name
	 * @param offset     the offset (e.g. +04:00)
	 * @return the current {@link NaftahDate}
	 */
	@NaftahFn(
				name = "التاريخ_الحالي_بتقويم_وإزاحة",
				aliases = {"التاريخ_الحالي"},
				description = "الحصول على التاريخ الحالي بتقويم وإزاحة زمنية",
				usage = "التاريخ_الحالي_بتقويم_وإزاحة(تقويم, إزاحة)",
				parameterTypes = {String.class, String.class},
				returnType = NaftahDate.class
	)
	public static NaftahDate currentOffsetChronologyDate(String chronology, String offset) {
		return NaftahDate
				.now(   ChronologyUtils.getChronologyByName(chronology),
						NaftahTime.ZoneOrOffset.ofOffset(offset));
	}

	/**
	 * Creates an {@link NaftahDate} instance from day, month, and year.
	 * <p>
	 * The {@code month} parameter may be either:
	 * <ul>
	 * <li>a {@link String} representing an Arabic month name</li>
	 * <li>a {@link Number} representing a numeric month value (1–12)</li>
	 * </ul>
	 *
	 * @param day   the day of month (1–31)
	 * @param month the month value, either an Arabic month name ({@link String}) or a numeric month ({@link Number}),
	 *              * not null
	 * @param year  the year value
	 * @return a new {@link NaftahDate} instance
	 * @throws IllegalArgumentException if {@code month} is neither a {@link String} nor a {@link Number}
	 */
	@NaftahFn(
				name = "انشاء_التاريخ_من_اليوم_و_شهر_و_سنة",
				aliases = {"انشاء_التاريخ"},
				description = "انشاء التاريخ من اليوم واسم الشهر والسنة",
				usage = "انشاء_التاريخ_من_اليوم_و_شهر_و_سنة(يوم, شهر, سنة)",
				parameterTypes = {Number.class, Object.class, Number.class},
				returnType = NaftahDate.class
	)
	public static NaftahDate createDate(Number day, Object month, Number year) {
		if (month instanceof String arabicMonth) {
			return NaftahDate.of(day.intValue(), arabicMonth, year.intValue());
		}
		else if (month instanceof Number monthNumber) {
			return NaftahDate.of(day.intValue(), monthNumber.intValue(), year.intValue());
		}
		else {
			throw newIllegalArgumentException(month);
		}
	}

	/**
	 * Creates an {@link NaftahDate} instance from a specific chronology, day, month, and year.
	 * <p>
	 * The {@code month} parameter may be either:
	 * <ul>
	 * <li>a {@link String} representing an Arabic month name</li>
	 * <li>a {@link Number} representing a numeric month value (1–12)</li>
	 * </ul>
	 *
	 * @param chronology the chronology name (e.g., "Hijri", "Gregorian")
	 * @param day        the day of month (1–31)
	 * @param month      the month value, either an Arabic month name ({@link String}) or a numeric month
	 *                   ({@link Number}), not null
	 * @param year       the year value
	 * @return a new {@link NaftahDate} instance with the specified chronology
	 * @throws IllegalArgumentException if {@code month} is neither a {@link String} nor a {@link Number}
	 */
	@NaftahFn(
				name = "انشاء_التاريخ_من_اليوم_و_شهر_و_سنة_بتقويم",
				aliases = {"انشاء_التاريخ"},
				description = "انشاء التاريخ من اليوم واسم الشهر والسنة مع تحديد التقويم",
				usage = "انشاء_التاريخ_من_اليوم_و_شهر_و_سنة_بتقويم(تقويم, يوم, شهر, سنة)",
				parameterTypes = {String.class, Number.class, Object.class, Number.class},
				returnType = NaftahDate.class
	)
	public static NaftahDate createDate(String chronology, Number day, Object month, Number year) {
		if (month instanceof String arabicMonth) {
			return NaftahDate
					.of(ChronologyUtils.getChronologyByName(chronology),
						day.intValue(),
						arabicMonth,
						year.intValue());
		}
		else if (month instanceof Number monthNumber) {
			return NaftahDate
					.of(ChronologyUtils.getChronologyByName(chronology),
						day.intValue(),
						monthNumber.intValue(),
						year.intValue());
		}
		else {
			throw newIllegalArgumentException(month);
		}
	}

	/**
	 * Obtains the current date-time using the system default settings.
	 *
	 * @return the current {@link NaftahDateTime}
	 */
	@NaftahFn(
				name = "التاريخ_والوقت_الحالي",
				description = "الحصول على التاريخ والوقت الحاليين",
				usage = "التاريخ_والوقت_الحالي()",
				returnType = NaftahDateTime.class
	)
	public static NaftahDateTime currentDateTime() {
		return NaftahDateTime.now();
	}


	/**
	 * Obtains the current date-time using a specific chronology.
	 *
	 * @param chronology the chronology name
	 * @return the current {@link NaftahDateTime}
	 */
	@NaftahFn(
				name = "التاريخ_والوقت_الحالي_بتقويم",
				aliases = {"التاريخ_والوقت_الحالي"},
				description = "الحصول على التاريخ والوقت الحاليين بتقويم محدد",
				usage = "التاريخ_والوقت_الحالي_بتقويم(تقويم)",
				parameterTypes = {String.class},
				returnType = NaftahDateTime.class
	)
	public static NaftahDateTime currentChronologyDateTime(String chronology) {
		return NaftahDateTime.now(ChronologyUtils.getChronologyByName(chronology));
	}

	/**
	 * Obtains the current date-time using the specified time zone.
	 *
	 * @param zone the zone ID
	 * @return the current {@link NaftahDateTime}
	 */
	@NaftahFn(
				name = "التاريخ_والوقت_الحالي_بتوقيت",
				aliases = {"التاريخ_والوقت_الحالي"},
				description = "الحصول على التاريخ والوقت الحاليين بتوقيت محدد",
				usage = "التاريخ_والوقت_الحالي_بتوقيت(منطقة)",
				parameterTypes = {String.class},
				returnType = NaftahDateTime.class
	)
	public static NaftahDateTime currentZonedDateTime(String zone) {
		return NaftahDateTime.now(NaftahTime.ZoneOrOffset.ofZone(zone));
	}

	/**
	 * Obtains the current date-time using a fixed offset.
	 *
	 * @param offset the offset (e.g. +04:00)
	 * @return the current {@link NaftahDateTime}
	 */
	@NaftahFn(
				name = "التاريخ_والوقت_الحالي_بإزاحة",
				aliases = {"التاريخ_والوقت_الحالي"},
				description = "الحصول على التاريخ والوقت الحاليين بإزاحة زمنية",
				usage = "التاريخ_والوقت_الحالي_بإزاحة(إزاحة)",
				parameterTypes = {String.class},
				returnType = NaftahDateTime.class
	)
	public static NaftahDateTime currentOffsetDateTime(String offset) {
		return NaftahDateTime.now(NaftahTime.ZoneOrOffset.ofOffset(offset));
	}

	/**
	 * Obtains the current date-time using the specified chronology and time zone.
	 *
	 * @param chronology the chronology name
	 * @param zone       the zone ID
	 * @return the current {@link NaftahDateTime}
	 */
	@NaftahFn(
				name = "التاريخ_والوقت_الحالي_بتقويم_وتوقيت",
				aliases = {"التاريخ_والوقت_الحالي"},
				description = "الحصول على التاريخ والوقت الحاليين بتقويم وتوقيت محددين",
				usage = "التاريخ_والوقت_الحالي_بتقويم_وتوقيت(تقويم, منطقة)",
				parameterTypes = {String.class, String.class},
				returnType = NaftahDateTime.class
	)
	public static NaftahDateTime currentZonedChronologyDateTime(String chronology, String zone) {
		return NaftahDateTime
				.now(   ChronologyUtils.getChronologyByName(chronology),
						NaftahTime.ZoneOrOffset.ofZone(zone));
	}

	/**
	 * Obtains the current date-time using the specified chronology and offset.
	 *
	 * @param chronology the chronology name
	 * @param offset     the offset (e.g. +04:00)
	 * @return the current {@link NaftahDateTime}
	 */
	@NaftahFn(
				name = "التاريخ_والوقت_الحالي_بتقويم_وإزاحة",
				aliases = {"التاريخ_والوقت_الحالي"},
				description = "الحصول على التاريخ والوقت الحاليين بتقويم وإزاحة زمنية",
				usage = "التاريخ_والوقت_الحالي_بتقويم_وإزاحة(تقويم, إزاحة)",
				parameterTypes = {String.class, String.class},
				returnType = NaftahDateTime.class
	)
	public static NaftahDateTime currentOffsetChronologyDateTime(String chronology, String offset) {
		return NaftahDateTime
				.now(   ChronologyUtils.getChronologyByName(chronology),
						NaftahTime.ZoneOrOffset.ofOffset(offset));
	}

	/**
	 * Creates an {@link NaftahDateTime} instance from year, month, day, hour, and minute.
	 * <p>
	 * The {@code month} parameter may be either:
	 * <ul>
	 * <li>a {@link String} representing an Arabic month name</li>
	 * <li>a {@link Number} representing a numeric month value (1–12)</li>
	 * </ul>
	 * <p>
	 * Seconds and nanoseconds default to {@code 0}.
	 *
	 * @param year       the year value
	 * @param month      the month value, either an Arabic month name ({@link String})
	 *                   or a numeric month ({@link Number})
	 * @param dayOfMonth the day-of-month value (1–31)
	 * @param hour       the hour-of-day (0–23)
	 * @param minute     the minute-of-hour (0–59)
	 * @return a new {@link NaftahDateTime} instance
	 * @throws IllegalArgumentException if {@code month} is neither a {@link String} nor a {@link Number}
	 */
	@NaftahFn(
				name = "انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة",
				aliases = {"انشاء_التاريخ_والوقت"},
				description = "انشاء التاريخ والوقت من السنة والشهر (اسم عربي أو رقم) واليوم والساعة والدقيقة",
				usage = "انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة(سنة, شهر, يوم, ساعة, دقيقة)",
				parameterTypes = {Number.class, Object.class, Number.class, Number.class, Number.class},
				returnType = NaftahDateTime.class
	)
	public static NaftahDateTime createDateTime(Number year,
												Object month,
												Number dayOfMonth,
												Number hour,
												Number minute) {
		if (month instanceof String arabicMonth) {
			return NaftahDateTime
					.of(year.intValue(),
						arabicMonth,
						dayOfMonth.intValue(),
						hour.intValue(),
						minute.intValue());
		}
		else if (month instanceof Number monthNumber) {

			return NaftahDateTime
					.of(year.intValue(),
						monthNumber.intValue(),
						dayOfMonth.intValue(),
						hour.intValue(),
						minute.intValue());
		}
		else {
			throw newIllegalArgumentException(month);
		}
	}

	/**
	 * Creates an {@link NaftahDateTime} instance from year, month, day, hour, and minute
	 * using a specific time zone.
	 * <p>
	 * The {@code month} parameter may be either:
	 * <ul>
	 * <li>a {@link String} representing an Arabic month name</li>
	 * <li>a {@link Number} representing a numeric month value (1–12)</li>
	 * </ul>
	 * <p>
	 * Seconds and nanoseconds default to {@code 0}.
	 * <p>
	 * ram year the year value
	 *
	 * @param month      the month value, either an Arabic month name ({@link String})
	 *                   or a numeric month ({@link Number})
	 * @param dayOfMonth the day-of-month value (1–31)
	 * @param hour       the hour-of-day (0–23)
	 * @param minute     the minute-of-hour (0–59)
	 * @param zone       the time zone ID (e.g. {@code "Asia/Riyadh"})
	 * @return a new {@link NaftahDateTime} instance with the specified time zone
	 * @throws IllegalArgumentException if {@code month} is neither a {@link String} nor a {@link Number}
	 */
	@NaftahFn(
				name = "انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة_بتوقيت",
				aliases = {"انشاء_التاريخ_والوقت"},
				description = "انشاء التاريخ والوقت من السنة والشهر (اسم عربي أو رقم) واليوم والساعة والدقيقة مع منطقة " + "زمنية",
				usage = "انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة_بتوقيت(سنة, شهر, يوم, ساعة, دقيقة, منطقة)",
				parameterTypes = {Number.class, Object.class, Number.class, Number.class, Number.class, String.class},
				returnType = NaftahDateTime.class
	)
	public static NaftahDateTime createZonedDateTime(   Number year,
														Object month,
														Number dayOfMonth,
														Number hour,
														Number minute,
														String zone) {
		if (month instanceof String arabicMonth) {
			return NaftahDateTime
					.of(year.intValue(),
						arabicMonth,
						dayOfMonth.intValue(),
						hour.intValue(),
						minute.intValue(),
						NaftahTime.ZoneOrOffset.ofZone(zone));
		}
		else if (month instanceof Number monthNumber) {
			return NaftahDateTime
					.of(year.intValue(),
						monthNumber.intValue(),
						dayOfMonth.intValue(),
						hour.intValue(),
						minute.intValue(),
						NaftahTime.ZoneOrOffset.ofZone(zone));
		}
		else {
			throw newIllegalArgumentException(month);
		}

	}

	/**
	 * Creates an {@link NaftahDateTime} instance from year, month, day, hour, and minute
	 * using a fixed time offset.
	 * <p>
	 * The {@code month} parameter may be either:
	 * <ul>
	 * <li>a {@link String} representing an Arabic month name</li>
	 * <li>a {@link Number} representing a numeric month value (1–12)</li>
	 * </ul>
	 * <p>
	 * Seconds and nanoseconds default to {@code 0}.
	 *
	 * @param year       the year value
	 * @param month      the month value, either an Arabic month name ({@link String})
	 *                   or a numeric month ({@link Number})
	 * @param dayOfMonth the day-of-month value (1–31)
	 * @param hour       the hour-of-day (0–23)
	 * @param minute     the minute-of-hour (0–59)
	 * @param offset     the time offset (e.g. {@code "+03:00"})
	 * @return a new {@link NaftahDateTime} instance with the specified offset
	 * @throws IllegalArgumentException if {@code month} is neither a {@link String} nor a {@link Number}
	 */
	@NaftahFn(
				name = "انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة_بإزاحة",
				aliases = {"انشاء_التاريخ_والوقت"},
				description = "انشاء التاريخ والوقت من السنة والشهر (اسم عربي أو رقم) واليوم والساعة والدقيقة مع إزاحة " + "زمنية",
				usage = "انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة_بإزاحة(سنة, شهر, يوم, ساعة, دقيقة, إزاحة)",
				parameterTypes = {Number.class, Object.class, Number.class, Number.class, Number.class, String.class},
				returnType = NaftahDateTime.class
	)
	public static NaftahDateTime createOffsetDateTime(  Number year,
														Object month,
														Number dayOfMonth,
														Number hour,
														Number minute,
														String offset) {
		if (month instanceof String arabicMonth) {
			return NaftahDateTime
					.of(year.intValue(),
						arabicMonth,
						dayOfMonth.intValue(),
						hour.intValue(),
						minute.intValue(),
						NaftahTime.ZoneOrOffset.ofOffset(offset));
		}
		else if (month instanceof Number monthNumber) {

			return NaftahDateTime
					.of(year.intValue(),
						monthNumber.intValue(),
						dayOfMonth.intValue(),
						hour.intValue(),
						minute.intValue(),
						NaftahTime.ZoneOrOffset.ofOffset(offset));
		}
		else {
			throw newIllegalArgumentException(month);
		}
	}

	/**
	 * Creates an {@link NaftahDateTime} instance from year, month, day, hour, minute, and second.
	 * <p>
	 * The {@code month} parameter may be either:
	 * <ul>
	 * <li>a {@link String} representing an Arabic month name</li>
	 * <li>a {@link Number} representing a numeric month value (1–12)</li>
	 * </ul>
	 * <p>
	 * Nanoseconds default to {@code 0}.
	 *
	 * @param year       the year value
	 * @param month      the month value, either an Arabic month name ({@link String})
	 *                   or a numeric month ({@link Number})
	 * @param dayOfMonth the day-of-month value (1–31)
	 * @param hour       the hour-of-day (0–23)
	 * @param minute     the minute-of-hour (0–59)
	 * @param second     the second-of-minute (0–59)
	 * @return a new {@link NaftahDateTime} instance
	 * @throws IllegalArgumentException if {@code month} is neither a {@link String} nor a {@link Number}
	 */
	@NaftahFn(
				name = "انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة_وثانية",
				aliases = {"انشاء_التاريخ_والوقت"},
				description = "انشاء التاريخ والوقت من السنة والشهر (اسم عربي أو رقم) واليوم والساعة والدقيقة والثانية",
				usage = "انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة_وثانية(سنة, شهر, يوم, ساعة, دقيقة, ثانية)",
				parameterTypes = {Number.class, Object.class, Number.class, Number.class, Number.class, Number.class},
				returnType = NaftahDateTime.class
	)
	public static NaftahDateTime createDateTime(Number year,
												Object month,
												Number dayOfMonth,
												Number hour,
												Number minute,
												Number second) {
		if (month instanceof String arabicMonth) {
			return NaftahDateTime
					.of(year.intValue(),
						arabicMonth,
						dayOfMonth.intValue(),
						hour.intValue(),
						minute.intValue(),
						second.intValue());
		}
		else if (month instanceof Number monthNumber) {
			return NaftahDateTime
					.of(year.intValue(),
						monthNumber.intValue(),
						dayOfMonth.intValue(),
						hour.intValue(),
						minute.intValue(),
						second.intValue());
		}
		else {
			throw newIllegalArgumentException(month);
		}

	}

	/**
	 * Creates an {@link NaftahDateTime} instance from year, month, day, hour, minute, and second
	 * using a specific time zone.
	 * <p>
	 * The {@code month} parameter may be either:
	 * <ul>
	 * <li>a {@link String} representing an Arabic month name</li>
	 * <li>a {@link Number} representing a numeric month value (1–12)</li>
	 * </ul>
	 * <p>
	 * Nanoseconds default to {@code 0}.
	 *
	 * @param year       the year value
	 * @param month      the month value, either an Arabic month name ({@link String})
	 *                   or a numeric month ({@link Number})
	 * @param dayOfMonth the day-of-month value (1–31)
	 * @param hour       the hour-of-day (0–23)
	 * @param minute     the minute-of-hour (0–59)
	 * @param second     the second-of-minute (0–59)
	 * @param zone       the time zone ID (e.g. {@code "Asia/Riyadh"})
	 * @return a new {@link NaftahDateTime} instance with the specified time zone
	 * @throws IllegalArgumentException if {@code month} is neither a {@link String} nor a {@link Number}
	 */
	@NaftahFn(
				name = "انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة_وثانية_بتوقيت",
				aliases = {"انشاء_التاريخ_والوقت"},
				description = "انشاء التاريخ والوقت من السنة والشهر (اسم عربي أو رقم) واليوم والساعة والدقيقة والثانية " + "مع" + " " + "منطقة زمنية",
				usage = "انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة_وثانية_بتوقيت(سنة, شهر, يوم, ساعة, دقيقة, " + "ثانية, منطقة)",
				parameterTypes = {
									Number.class,
									Object.class,
									Number.class,
									Number.class,
									Number.class,
									Number.class,
									String.class
				},
				returnType = NaftahDateTime.class
	)
	public static NaftahDateTime createZonedDateTime(   Number year,
														Object month,
														Number dayOfMonth,
														Number hour,
														Number minute,
														Number second,
														String zone) {
		if (month instanceof String arabicMonth) {
			return NaftahDateTime
					.of(year.intValue(),
						arabicMonth,
						dayOfMonth.intValue(),
						hour.intValue(),
						minute.intValue(),
						second.intValue(),
						NaftahTime.ZoneOrOffset.ofZone(zone));
		}
		else if (month instanceof Number monthNumber) {
			return NaftahDateTime
					.of(year.intValue(),
						monthNumber.intValue(),
						dayOfMonth.intValue(),
						hour.intValue(),
						minute.intValue(),
						second.intValue(),
						NaftahTime.ZoneOrOffset.ofZone(zone));
		}
		else {
			throw newIllegalArgumentException(month);
		}

	}

	/**
	 * Creates an {@link NaftahDateTime} instance from year, month, day, hour, minute, and second
	 * using a fixed time offset.
	 * <p>
	 * The {@code month} parameter may be either:
	 * <ul>
	 * <li>a {@link String} representing an Arabic month name</li>
	 * <li>a {@link Number} representing a numeric month value (1–12)</li>
	 * </ul>
	 * <p>
	 * Nanoseconds default to {@code 0}.
	 *
	 * @param year       the year value
	 * @param month      the month value, either an Arabic month name ({@link String})
	 *                   or a numeric month ({@link Number})
	 * @param dayOfMonth the day-of-month value (1–31)
	 * @param hour       the hour-of-day (0–23)
	 * @param minute     the minute-of-hour (0–59)
	 * @param second     the second-of-minute (0–59)
	 * @param offset     the time offset (e.g. {@code "+04:00"})
	 * @return a new {@link NaftahDateTime} instance with the specified offset
	 * @throws IllegalArgumentException if {@code month} is neither a {@link String} nor a {@link Number}
	 */
	@NaftahFn(
				name = "انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة_وثانية_بإزاحة",
				aliases = {"انشاء_التاريخ_والوقت"},
				description = "انشاء التاريخ والوقت من السنة والشهر (اسم عربي أو رقم) واليوم والساعة والدقيقة والثانية " + "مع" + " " + "إزاحة زمنية",
				usage = "انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة_وثانية_بإزاحة(سنة, شهر, يوم, ساعة, دقيقة, " + "ثانية, إزاحة)",
				parameterTypes = {
									Number.class,
									Object.class,
									Number.class,
									Number.class,
									Number.class,
									Number.class,
									String.class
				},
				returnType = NaftahDateTime.class
	)
	public static NaftahDateTime createOffsetDateTime(  Number year,
														Object month,
														Number dayOfMonth,
														Number hour,
														Number minute,
														Number second,
														String offset) {
		if (month instanceof String arabicMonth) {
			return NaftahDateTime
					.of(year.intValue(),
						arabicMonth,
						dayOfMonth.intValue(),
						hour.intValue(),
						minute.intValue(),
						second.intValue(),
						NaftahTime.ZoneOrOffset.ofOffset(offset));
		}
		else if (month instanceof Number monthNumber) {
			return NaftahDateTime
					.of(year.intValue(),
						monthNumber.intValue(),
						dayOfMonth.intValue(),
						hour.intValue(),
						minute.intValue(),
						second.intValue(),
						NaftahTime.ZoneOrOffset.ofOffset(offset));
		}
		else {
			throw newIllegalArgumentException(month);
		}

	}

	/**
	 * Creates an {@link NaftahDateTime} instance from year, month, day, hour, minute,
	 * second, and nanosecond.
	 * <p>
	 * The {@code month} parameter may be either:
	 * <ul>
	 * <li>a {@link String} representing an Arabic month name</li>
	 * <li>a {@link Number} representing a numeric month value (1–12)</li>
	 * </ul>
	 *
	 * @param year         the year value
	 * @param month        the month value, either an Arabic month name ({@link String})
	 *                     or a numeric month ({@link Number})
	 * @param dayOfMonth   the day-of-month value (1–31)
	 * @param hour         the hour-of-day (0–23)
	 * @param minute       the minute-of-hour (0–59)
	 * @param second       the second-of-minute (0–59)
	 * @param nanoOfSecond the nanosecond-of-second (0–999,999,999)
	 * @return a new {@link NaftahDateTime} instance
	 * @throws IllegalArgumentException if {@code month} is neither a {@link String} nor a {@link Number}
	 */
	@NaftahFn(
				name = "انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة_وثانية_ونانوثانية",
				aliases = {"انشاء_التاريخ_والوقت"},
				description = "انشاء التاريخ والوقت من السنة والشهر (اسم عربي أو رقم) واليوم والساعة والدقيقة والثانية " + "والنانوثانية",
				usage = "انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة_وثانية_ونانوثانية(سنة, شهر, يوم, ساعة, " + "دقيقة," + " " + "ثانية, نانوثانية)",
				parameterTypes = {
									Number.class,
									Object.class,
									Number.class,
									Number.class,
									Number.class,
									Number.class,
									Number.class
				},
				returnType = NaftahDateTime.class
	)
	public static NaftahDateTime createDateTime(Number year,
												Object month,
												Number dayOfMonth,
												Number hour,
												Number minute,
												Number second,
												Number nanoOfSecond) {
		if (month instanceof String arabicMonth) {
			return NaftahDateTime
					.of(year.intValue(),
						arabicMonth,
						dayOfMonth.intValue(),
						hour.intValue(),
						minute.intValue(),
						second.intValue(),
						nanoOfSecond.intValue());
		}
		else if (month instanceof Number monthNumber) {
			return NaftahDateTime
					.of(year.intValue(),
						monthNumber.intValue(),
						dayOfMonth.intValue(),
						hour.intValue(),
						minute.intValue(),
						second.intValue(),
						nanoOfSecond.intValue());
		}
		else {
			throw newIllegalArgumentException(month);
		}

	}

	/**
	 * Creates an {@link NaftahDateTime} instance from year, month, day, hour, minute,
	 * second, and nanosecond using a specific time zone.
	 * <p>
	 * The {@code month} parameter may be either:
	 * <ul>
	 * <li>a {@link String} representing an Arabic month name</li>
	 * <li>a {@link Number} representing a numeric month value (1–12)</li>
	 * </ul>
	 *
	 * @param year         the year value
	 * @param month        the month value, either an Arabic month name ({@link String})
	 *                     or a numeric month ({@link Number})
	 * @param dayOfMonth   the day-of-month value (1–31)
	 * @param hour         the hour-of-day (0–23)
	 * @param minute       the minute-of-hour (0–59)
	 * @param second       the second-of-minute (0–59)
	 * @param nanoOfSecond the nanosecond-of-second (0–999,999,999)
	 * @param zone         the time zone ID (e.g. {@code "Asia/Riyadh"})
	 * @return a new {@link NaftahDateTime} instance with the specified time zone
	 * @throws IllegalArgumentException if {@code month} is neither a {@link String} nor a {@link Number}
	 */
	@NaftahFn(
				name = "انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة_وثانية_ونانوثانية_بتوقيت",
				aliases = {"انشاء_التاريخ_والوقت"},
				description = "انشاء التاريخ والوقت من السنة والشهر (اسم عربي أو رقم) واليوم والساعة والدقيقة والثانية " + "والنانوثانية مع منطقة زمنية",
				usage = "انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة_وثانية_ونانوثانية_بتوقيت(سنة, شهر, يوم, " + "ساعة," + " " + "دقيقة, ثانية, نانوثانية, منطقة)",
				parameterTypes = {
									Number.class,
									Object.class,
									Number.class,
									Number.class,
									Number.class,
									Number.class,
									Number.class,
									String.class
				},
				returnType = NaftahDateTime.class
	)
	public static NaftahDateTime createZonedDateTime(   Number year,
														Object month,
														Number dayOfMonth,
														Number hour,
														Number minute,
														Number second,
														Number nanoOfSecond,
														String zone) {
		if (month instanceof String arabicMonth) {
			return NaftahDateTime
					.of(year.intValue(),
						arabicMonth,
						dayOfMonth.intValue(),
						hour.intValue(),
						minute.intValue(),
						second.intValue(),
						nanoOfSecond.intValue(),
						NaftahTime.ZoneOrOffset.ofZone(zone));
		}
		else if (month instanceof Number monthNumber) {
			return NaftahDateTime
					.of(year.intValue(),
						monthNumber.intValue(),
						dayOfMonth.intValue(),
						hour.intValue(),
						minute.intValue(),
						second.intValue(),
						nanoOfSecond.intValue(),
						NaftahTime.ZoneOrOffset.ofZone(zone));
		}
		else {
			throw newIllegalArgumentException(month);
		}

	}

	/**
	 * Creates an {@link NaftahDateTime} instance from year, month, day, hour, minute,
	 * second, and nanosecond using a fixed offset.
	 * <p>
	 * The {@code month} parameter may be either:
	 * <ul>
	 * <li>a {@link String} representing an Arabic month name</li>
	 * <li>a {@link Number} representing a numeric month value (1–12)</li>
	 * </ul>
	 *
	 * @param year         the year value
	 * @param month        the month value, either an Arabic month name ({@link String})
	 *                     or a numeric month ({@link Number})
	 * @param dayOfMonth   the day-of-month value (1–31)
	 * @param hour         the hour-of-day (0–23)
	 * @param minute       the minute-of-hour (0–59)
	 * @param second       the second-of-minute (0–59)
	 * @param nanoOfSecond the nanosecond-of-second (0–999,999,999)
	 * @param offset       the fixed offset (e.g., "+04:00")
	 * @return a new {@link NaftahDateTime} instance with the specified offset
	 * @throws IllegalArgumentException if {@code month} is neither a {@link String} nor a {@link Number}
	 */
	@NaftahFn(
				name = "انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة_وثانية_ونانوثانية_بإزاحة",
				aliases = {"انشاء_التاريخ_والوقت"},
				description = "انشاء التاريخ والوقت من السنة والشهر (اسم عربي أو رقم) واليوم والساعة والدقيقة والثانية " + "والنانوثانية مع إزاحة زمنية محددة",
				usage = "انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة_وثانية_ونانوثانية_بإزاحة(سنة, شهر, يوم, " + "ساعة," + " " + "دقيقة, ثانية, نانوثانية, إزاحة)",
				parameterTypes = {
									Number.class,
									Object.class,
									Number.class,
									Number.class,
									Number.class,
									Number.class,
									Number.class,
									String.class
				},
				returnType = NaftahDateTime.class
	)
	public static NaftahDateTime createOffsetDateTime(  Number year,
														Object month,
														Number dayOfMonth,
														Number hour,
														Number minute,
														Number second,
														Number nanoOfSecond,
														String offset) {
		if (month instanceof String arabicMonth) {
			return NaftahDateTime
					.of(year.intValue(),
						arabicMonth,
						dayOfMonth.intValue(),
						hour.intValue(),
						minute.intValue(),
						second.intValue(),
						nanoOfSecond.intValue(),
						NaftahTime.ZoneOrOffset.ofOffset(offset));
		}
		else if (month instanceof Number monthNumber) {
			return NaftahDateTime
					.of(year.intValue(),
						monthNumber.intValue(),
						dayOfMonth.intValue(),
						hour.intValue(),
						minute.intValue(),
						second.intValue(),
						nanoOfSecond.intValue(),
						NaftahTime.ZoneOrOffset.ofOffset(offset));
		}
		else {
			throw newIllegalArgumentException(month);
		}

	}

	/**
	 * Creates an {@link NaftahDateTime} instance from year, month, day, hour, and minute
	 * using a specific chronology.
	 * <p>
	 * The {@code month} parameter may be either:
	 * <ul>
	 * <li>a {@link String} representing an Arabic month name</li>
	 * <li>a {@link Number} representing a numeric month value (1–12)</li>
	 * </ul>
	 *
	 * @param chronology the chronology name (e.g., "Hijri", "Gregorian")
	 * @param year       the year value
	 * @param month      the month value, either an Arabic month name ({@link String}) or a numeric month
	 *                   * ({@link Number})
	 * @param dayOfMonth the day-of-month value (1–31)
	 * @param hour       the hour-of-day (0–23)
	 * @param minute     the minute-of-hour (0–59)
	 * @return a new {@link NaftahDateTime} instance with the specified chronology
	 * @throws IllegalArgumentException if {@code month} is neither a {@link String} nor a {@link Number}
	 */
	@NaftahFn(
				name = "انشاء_التاريخ_والوقت_بتقويم_وساعة_ودقيقة",
				aliases = {"انشاء_التاريخ_والوقت"},
				description = "انشاء التاريخ والوقت باستخدام تقويم محدد من السنة والشهر (اسم عربي أو رقم) واليوم والساعة" + " " + "والدقيقة",
				usage = "انشاء_التاريخ_والوقت_بتقويم_وساعة_ودقيقة(تقويم, سنة, شهر, يوم, ساعة, دقيقة)",
				parameterTypes = {
									String.class,
									Number.class,
									Object.class,
									Number.class,
									Number.class,
									Number.class
				},
				returnType = NaftahDateTime.class
	)
	public static NaftahDateTime createChronologyDateTime(  String chronology,
															Number year,
															Object month,
															Number dayOfMonth,
															Number hour,
															Number minute) {
		if (month instanceof String arabicMonth) {
			return NaftahDateTime
					.of(ChronologyUtils.getChronologyByName(chronology),
						year.intValue(),
						arabicMonth,
						dayOfMonth.intValue(),
						hour.intValue(),
						minute.intValue());
		}
		else if (month instanceof Number monthNumber) {
			return NaftahDateTime
					.of(ChronologyUtils.getChronologyByName(chronology),
						year.intValue(),
						monthNumber.intValue(),
						dayOfMonth.intValue(),
						hour.intValue(),
						minute.intValue());
		}
		else {
			throw newIllegalArgumentException(month);
		}

	}

	/**
	 * Creates an {@link NaftahDateTime} instance from year, month, day, hour, and minute
	 * using a specific chronology and a time zone.
	 * <p>
	 * The {@code month} parameter may be either:
	 * <ul>
	 * <li>a {@link String} representing an Arabic month name</li>
	 * <li>a {@link Number} representing a numeric month value (1–12)</li>
	 * </ul>
	 *
	 * @param chronology the chronology name (e.g., "Hijri", "Gregorian")
	 * @param year       the year value
	 * @param month      the month value, either an Arabic month name ({@link String}) or a numeric month
	 *                   * ({@link Number})
	 * @param dayOfMonth the day-of-month value (1–31)
	 * @param hour       the hour-of-day (0–23)
	 * @param minute     the minute-of-hour (0–59)
	 * @param zone       the time zone ID (e.g., "Asia/Riyadh")
	 * @return a new {@link NaftahDateTime} instance with the specified chronology and zone
	 * @throws IllegalArgumentException if {@code month} is neither a {@link String} nor a {@link Number}
	 */
	@NaftahFn(
				name = "انشاء_التاريخ_والوقت_بتقويم_وساعة_ودقيقة_بتوقيت",
				aliases = {"انشاء_التاريخ_والوقت"},
				description = "انشاء التاريخ والوقت باستخدام تقويم محدد من السنة والشهر (اسم عربي أو رقم) واليوم والساعة" + " " + "والدقيقة مع منطقة زمنية",
				usage = "انشاء_التاريخ_والوقت_بتقويم_وساعة_ودقيقة_بتوقيت(تقويم, سنة, شهر, يوم, ساعة, دقيقة, منطقة)",
				parameterTypes = {
									String.class,
									Number.class,
									Object.class,
									Number.class,
									Number.class,
									Number.class,
									String.class
				},
				returnType = NaftahDateTime.class
	)
	public static NaftahDateTime createZonedChronologyDateTime( String chronology,
																Number year,
																Object month,
																Number dayOfMonth,
																Number hour,
																Number minute,
																String zone) {
		if (month instanceof String arabicMonth) {
			return NaftahDateTime
					.of(ChronologyUtils.getChronologyByName(chronology),
						year.intValue(),
						arabicMonth,
						dayOfMonth.intValue(),
						hour.intValue(),
						minute.intValue(),
						NaftahTime.ZoneOrOffset.ofZone(zone));
		}
		else if (month instanceof Number monthNumber) {
			return NaftahDateTime
					.of(ChronologyUtils.getChronologyByName(chronology),
						year.intValue(),
						monthNumber.intValue(),
						dayOfMonth.intValue(),
						hour.intValue(),
						minute.intValue(),
						NaftahTime.ZoneOrOffset.ofZone(zone));
		}
		else {
			throw newIllegalArgumentException(month);
		}

	}

	/**
	 * Creates an {@link NaftahDateTime} instance from year, month, day, hour, and minute
	 * using a specific chronology and a fixed offset.
	 * <p>
	 * The {@code month} parameter may be either:
	 * <ul>
	 * <li>a {@link String} representing an Arabic month name</li>
	 * <li>a {@link Number} representing a numeric month value (1–12)</li>
	 * </ul>
	 *
	 * @param chronology the chronology name (e.g., "Hijri", "Gregorian")
	 * @param year       the year value
	 * @param month      the month value, either an Arabic month name ({@link String}) or a numeric month
	 *                   * ({@link Number})
	 * @param dayOfMonth the day-of-month value (1–31)
	 * @param hour       the hour-of-day (0–23)
	 * @param minute     the minute-of-hour (0–59)
	 * @param offset     the fixed offset (e.g., "+04:00")
	 * @return a new {@link NaftahDateTime} instance with the specified chronology and offset
	 * @throws IllegalArgumentException if {@code month} is neither a {@link String} nor a {@link Number}
	 */
	@NaftahFn(
				name = "انشاء_التاريخ_والوقت_بتقويم_وساعة_ودقيقة_بإزاحة",
				aliases = {"انشاء_التاريخ_والوقت"},
				description = "انشاء التاريخ والوقت باستخدام تقويم محدد من السنة والشهر (اسم عربي أو رقم) واليوم والساعة" + " " + "والدقيقة مع إزاحة زمنية",
				usage = "انشاء_التاريخ_والوقت_بتقويم_وساعة_ودقيقة_بإزاحة(تقويم, سنة, شهر, يوم, ساعة, دقيقة, إزاحة)",
				parameterTypes = {
									String.class,
									Number.class,
									Object.class,
									Number.class,
									Number.class,
									Number.class,
									String.class
				},
				returnType = NaftahDateTime.class
	)
	public static NaftahDateTime createOffsetChronologyDateTime(String chronology,
																Number year,
																Object month,
																Number dayOfMonth,
																Number hour,
																Number minute,
																String offset) {
		if (month instanceof String arabicMonth) {
			return NaftahDateTime
					.of(ChronologyUtils.getChronologyByName(chronology),
						year.intValue(),
						arabicMonth,
						dayOfMonth.intValue(),
						hour.intValue(),
						minute.intValue(),
						NaftahTime.ZoneOrOffset.ofOffset(offset));
		}
		else if (month instanceof Number monthNumber) {
			return NaftahDateTime
					.of(ChronologyUtils.getChronologyByName(chronology),
						year.intValue(),
						monthNumber.intValue(),
						dayOfMonth.intValue(),
						hour.intValue(),
						minute.intValue(),
						NaftahTime.ZoneOrOffset.ofOffset(offset));
		}
		else {
			throw newIllegalArgumentException(month);
		}

	}

	/**
	 * Creates an {@link NaftahDateTime} instance from year, month, day, hour, minute, and second
	 * using a specific chronology.
	 * <p>
	 * The {@code month} parameter may be either:
	 * <ul>
	 * <li>a {@link String} representing an Arabic month name</li>
	 * <li>a {@link Number} representing a numeric month value (1–12)</li>
	 * </ul>
	 *
	 * @param chronology the chronology name (e.g., "Hijri", "Gregorian")
	 * @param year       the year value
	 * @param month      the month value, either an Arabic month name ({@link String}) or a numeric month
	 *                   * ({@link Number})
	 * @param dayOfMonth the day-of-month value (1–31)
	 * @param hour       the hour-of-day (0–23)
	 * @param minute     the minute-of-hour (0–59)
	 * @param second     the second-of-minute (0–59)
	 * @return a new {@link NaftahDateTime} instance with the specified chronology
	 * @throws IllegalArgumentException if {@code month} is neither a {@link String} nor a {@link Number}
	 */
	@NaftahFn(
				name = "انشاء_التاريخ_والوقت_بتقويم_وساعة_ودقيقة_وثانية",
				aliases = {"انشاء_التاريخ_والوقت"},
				description = "انشاء التاريخ والوقت باستخدام تقويم محدد من السنة والشهر (اسم عربي أو رقم) واليوم والساعة" + " " + "والدقيقة والثانية",
				usage = "انشاء_التاريخ_والوقت_بتقويم_وساعة_ودقيقة_وثانية(تقويم, سنة, شهر, يوم, ساعة, دقيقة, ثانية)",
				parameterTypes = {
									String.class,
									Number.class,
									Object.class,
									Number.class,
									Number.class,
									Number.class,
									Number.class
				},
				returnType = NaftahDateTime.class
	)
	public static NaftahDateTime createChronologyDateTime(  String chronology,
															Number year,
															Object month,
															Number dayOfMonth,
															Number hour,
															Number minute,
															Number second) {
		if (month instanceof String arabicMonth) {
			return NaftahDateTime
					.of(ChronologyUtils.getChronologyByName(chronology),
						year.intValue(),
						arabicMonth,
						dayOfMonth.intValue(),
						hour.intValue(),
						minute.intValue(),
						second.intValue());
		}
		else if (month instanceof Number monthNumber) {
			return NaftahDateTime
					.of(ChronologyUtils.getChronologyByName(chronology),
						year.intValue(),
						monthNumber.intValue(),
						dayOfMonth.intValue(),
						hour.intValue(),
						minute.intValue(),
						second.intValue());
		}
		else {
			throw newIllegalArgumentException(month);
		}

	}

	/**
	 * Creates an {@link NaftahDateTime} instance from year, month, day, hour, minute, and second
	 * using a specific chronology and time zone.
	 * <p>
	 * The {@code month} parameter may be either:
	 * <ul>
	 * <li>a {@link String} representing an Arabic month name</li>
	 * <li>a {@link Number} representing a numeric month value (1–12)</li>
	 * </ul>
	 *
	 * @param chronology the chronology name (e.g., "Hijri", "Gregorian")
	 * @param year       the year value
	 * @param month      the month value, either an Arabic month name ({@link String}) or a numeric month
	 *                   * ({@link Number})
	 * @param dayOfMonth the day-of-month value (1–31)
	 * @param hour       the hour-of-day (0–23)
	 * @param minute     the minute-of-hour (0–59)
	 * @param second     the second-of-minute (0–59)
	 * @param zone       the time zone ID (e.g., "Asia/Riyadh")
	 * @return a new {@link NaftahDateTime} instance with the specified chronology and time zone
	 * @throws IllegalArgumentException if {@code month} is neither a {@link String} nor a {@link Number}
	 */
	@NaftahFn(
				name = "انشاء_التاريخ_والوقت_بتقويم_وثانية_وبتوقيت",
				aliases = {"انشاء_التاريخ_والوقت"},
				description = "انشاء التاريخ والوقت باستخدام تقويم محدد من السنة والشهر (اسم عربي أو رقم) واليوم والساعة" + " " + "والدقيقة والثانية مع منطقة زمنية محددة",
				usage = "انشاء_التاريخ_والوقت_بتقويم_وثانية_وبتوقيت(تقويم, سنة, شهر, يوم, ساعة, دقيقة, ثانية, منطقة)",
				parameterTypes = {
									String.class,
									Number.class,
									Object.class,
									Number.class,
									Number.class,
									Number.class,
									Number.class,
									String.class
				},
				returnType = NaftahDateTime.class
	)
	public static NaftahDateTime createZonedChronologyDateTime( String chronology,
																Number year,
																Object month,
																Number dayOfMonth,
																Number hour,
																Number minute,
																Number second,
																String zone) {
		if (month instanceof String arabicMonth) {
			return NaftahDateTime
					.of(ChronologyUtils.getChronologyByName(chronology),
						year.intValue(),
						arabicMonth,
						dayOfMonth.intValue(),
						hour.intValue(),
						minute.intValue(),
						second.intValue(),
						NaftahTime.ZoneOrOffset.ofZone(zone));
		}
		else if (month instanceof Number monthNumber) {
			return NaftahDateTime
					.of(ChronologyUtils.getChronologyByName(chronology),
						year.intValue(),
						monthNumber.intValue(),
						dayOfMonth.intValue(),
						hour.intValue(),
						minute.intValue(),
						second.intValue(),
						NaftahTime.ZoneOrOffset.ofZone(zone));
		}
		else {
			throw newIllegalArgumentException(month);
		}

	}

	/**
	 * Creates an {@link NaftahDateTime} instance from year, month, day, hour, minute, and second
	 * using a specific chronology and fixed offset.
	 * <p>
	 * The {@code month} parameter may be either:
	 * <ul>
	 * <li>a {@link String} representing an Arabic month name</li>
	 * <li>a {@link Number} representing a numeric month value (1–12)</li>
	 * </ul>
	 *
	 * @param chronology the chronology name (e.g., "Hijri", "Gregorian")
	 * @param year       the year value
	 * @param month      the month value, either an Arabic month name ({@link String}) or a numeric month
	 *                   * ({@link Number})
	 * @param dayOfMonth the day-of-month value (1–31)
	 * @param hour       the hour-of-day (0–23)
	 * @param minute     the minute-of-hour (0–59)
	 * @param second     the second-of-minute (0–59)
	 * @param offset     the fixed offset (e.g., "+04:00")
	 * @return a new {@link NaftahDateTime} instance with the specified chronology and offset
	 * @throws IllegalArgumentException if {@code month} is neither a {@link String} nor a {@link Number}
	 */
	@NaftahFn(
				name = "انشاء_التاريخ_والوقت_بتقويم_وثانية_بإزاحة",
				aliases = {"انشاء_التاريخ_والوقت"},
				description = "انشاء التاريخ والوقت باستخدام تقويم محدد من السنة والشهر (اسم عربي أو رقم) واليوم والساعة" + " " + "والدقيقة والثانية مع إزاحة زمنية محددة",
				usage = "انشاء_التاريخ_والوقت_بتقويم_وثانية_بإزاحة(تقويم, سنة, شهر, يوم, ساعة, دقيقة, ثانية, إزاحة)",
				parameterTypes = {
									String.class,
									Number.class,
									Object.class,
									Number.class,
									Number.class,
									Number.class,
									Number.class,
									String.class
				},
				returnType = NaftahDateTime.class
	)
	public static NaftahDateTime createOffsetChronologyDateTime(String chronology,
																Number year,
																Object month,
																Number dayOfMonth,
																Number hour,
																Number minute,
																Number second,
																String offset) {
		if (month instanceof String arabicMonth) {
			return NaftahDateTime
					.of(ChronologyUtils.getChronologyByName(chronology),
						year.intValue(),
						arabicMonth,
						dayOfMonth.intValue(),
						hour.intValue(),
						minute.intValue(),
						second.intValue(),
						NaftahTime.ZoneOrOffset.ofOffset(offset));
		}
		else if (month instanceof Number monthNumber) {
			return NaftahDateTime
					.of(ChronologyUtils.getChronologyByName(chronology),
						year.intValue(),
						monthNumber.intValue(),
						dayOfMonth.intValue(),
						hour.intValue(),
						minute.intValue(),
						second.intValue(),
						NaftahTime.ZoneOrOffset.ofOffset(offset));
		}
		else {
			throw newIllegalArgumentException(month);
		}

	}

	/**
	 * Creates an {@link NaftahDateTime} instance from year, month, day, hour, minute, second, and nanosecond
	 * using a specific chronology.
	 * <p>
	 * The {@code month} parameter may be either:
	 * <ul>
	 * <li>a {@link String} representing an Arabic month name</li>
	 * <li>a {@link Number} representing a numeric month value (1–12)</li>
	 * </ul>
	 *
	 * @param chronology   the chronology name (e.g., "Hijri", "Gregorian")
	 * @param year         the year value
	 * @param month        the month value, either an Arabic month name ({@link String}) or a numeric month
	 *                     * ({@link Number})
	 * @param dayOfMonth   the day-of-month value (1–31)
	 * @param hour         the hour-of-day (0–23)
	 * @param minute       the minute-of-hour (0–59)
	 * @param second       the second-of-minute (0–59)
	 * @param nanoOfSecond the nanosecond-of-second (0–999,999,999)
	 * @return a new {@link NaftahDateTime} instance with the specified chronology and nanoseconds
	 * @throws IllegalArgumentException if {@code month} is neither a {@link String} nor a {@link Number}
	 */
	@NaftahFn(
				name = "انشاء_التاريخ_والوقت_بتقويم_وثانية_ونانوثانية",
				aliases = {"انشاء_التاريخ_والوقت"},
				description = "انشاء التاريخ والوقت باستخدام تقويم محدد من السنة والشهر (اسم عربي أو رقم) واليوم والساعة" + " " + "والدقيقة والثانية والنانوثانية",
				usage = "انشاء_التاريخ_والوقت_بتقويم_وثانية_ونانوثانية(تقويم, سنة, شهر, يوم, ساعة, دقيقة, ثانية, " + "نانوثانية)",
				parameterTypes = {
									String.class,
									Number.class,
									Object.class,
									Number.class,
									Number.class,
									Number.class,
									Number.class,
									Number.class
				},
				returnType = NaftahDateTime.class
	)
	public static NaftahDateTime createChronologyDateTime(  String chronology,
															Number year,
															Object month,
															Number dayOfMonth,
															Number hour,
															Number minute,
															Number second,
															Number nanoOfSecond) {
		if (month instanceof String arabicMonth) {
			return NaftahDateTime
					.of(ChronologyUtils.getChronologyByName(chronology),
						year.intValue(),
						arabicMonth,
						dayOfMonth.intValue(),
						hour.intValue(),
						minute.intValue(),
						second.intValue(),
						nanoOfSecond.intValue());
		}
		else if (month instanceof Number monthNumber) {
			return NaftahDateTime
					.of(ChronologyUtils.getChronologyByName(chronology),
						year.intValue(),
						monthNumber.intValue(),
						dayOfMonth.intValue(),
						hour.intValue(),
						minute.intValue(),
						second.intValue(),
						nanoOfSecond.intValue());
		}
		else {
			throw newIllegalArgumentException(month);
		}

	}

	/**
	 * Creates an {@link NaftahDateTime} instance from year, month, day, hour, minute, second, and nanosecond
	 * using a specific chronology and time zone.
	 * <p>
	 * The {@code month} parameter may be either:
	 * <ul>
	 * <li>a {@link String} representing an Arabic month name</li>
	 * <li>a {@link Number} representing a numeric month value (1–12)</li>
	 * </ul>
	 *
	 * @param chronology   the chronology name (e.g., "Hijri", "Gregorian")
	 * @param year         the year value
	 * @param month        the month value, either an Arabic month name ({@link String}) or a numeric month
	 *                     * ({@link Number})
	 * @param dayOfMonth   the day-of-month value (1–31)
	 * @param hour         the hour-of-day (0–23)
	 * @param minute       the minute-of-hour (0–59)
	 * @param second       the second-of-minute (0–59)
	 * @param nanoOfSecond the nanosecond-of-second (0–999,999,999)
	 * @param zone         the time zone ID (e.g., "Asia/Riyadh")
	 * @return a new {@link NaftahDateTime} instance with the specified chronology and zone
	 * @throws IllegalArgumentException if {@code month} is neither a {@link String} nor a {@link Number}
	 */
	@NaftahFn(
				name = "انشاء_التاريخ_والوقت_بتقويم_وثانية_ونانوثانية_بتوقيت",
				aliases = {"انشاء_التاريخ_والوقت"},
				description = "انشاء التاريخ والوقت باستخدام تقويم محدد من السنة والشهر (اسم عربي أو رقم) واليوم والساعة" + " " + "والدقيقة والثانية والنانوثانية مع منطقة زمنية محددة",
				usage = "انشاء_التاريخ_والوقت_بتقويم_وثانية_ونانوثانية_بتوقيت(تقويم, سنة, شهر, يوم, ساعة, دقيقة, ثانية, " + "نانوثانية, منطقة)",
				parameterTypes = {
									String.class,
									Number.class,
									Object.class,
									Number.class,
									Number.class,
									Number.class,
									Number.class,
									Number.class,
									String.class
				},
				returnType = NaftahDateTime.class
	)
	public static NaftahDateTime createZonedChronologyDateTime( String chronology,
																Number year,
																Object month,
																Number dayOfMonth,
																Number hour,
																Number minute,
																Number second,
																Number nanoOfSecond,
																String zone) {
		if (month instanceof String arabicMonth) {
			return NaftahDateTime
					.of(ChronologyUtils.getChronologyByName(chronology),
						year.intValue(),
						arabicMonth,
						dayOfMonth.intValue(),
						hour.intValue(),
						minute.intValue(),
						second.intValue(),
						nanoOfSecond.intValue(),
						NaftahTime.ZoneOrOffset.ofZone(zone));
		}
		else if (month instanceof Number monthNumber) {
			return NaftahDateTime
					.of(ChronologyUtils.getChronologyByName(chronology),
						year.intValue(),
						monthNumber.intValue(),
						dayOfMonth.intValue(),
						hour.intValue(),
						minute.intValue(),
						second.intValue(),
						nanoOfSecond.intValue(),
						NaftahTime.ZoneOrOffset.ofZone(zone));
		}
		else {
			throw newIllegalArgumentException(month);
		}

	}

	/**
	 * Creates an {@link NaftahDateTime} instance from year, month, day, hour, minute, second, and nanosecond
	 * using a specific chronology and fixed offset.
	 * <p>
	 * The {@code month} parameter may be either:
	 * <ul>
	 * <li>a {@link String} representing an Arabic month name</li>
	 * <li>a {@link Number} representing a numeric month value (1–12)</li>
	 * </ul>
	 *
	 * @param chronology   the chronology name (e.g., "Hijri", "Gregorian")
	 * @param year         the year value
	 * @param month        the month value, either an Arabic month name ({@link String}) or a numeric month
	 *                     ({@link Number})
	 * @param dayOfMonth   the day-of-month value (1–31)
	 * @param hour         the hour-of-day (0–23)
	 * @param minute       the minute-of-hour (0–59)
	 * @param second       the second-of-minute (0–59)
	 * @param nanoOfSecond the nanosecond-of-second (0–999,999,999)
	 * @param offset       the offset (e.g., "+04:00")
	 * @return a new {@link NaftahDateTime} instance with the specified chronology and offset
	 * @throws IllegalArgumentException if {@code month} is neither a {@link String} nor a {@link Number}
	 */
	@NaftahFn(
				name = "انشاء_التاريخ_والوقت_بتقويم_وثانية_ونانوثانية_بإزاحة",
				aliases = {"انشاء_التاريخ_والوقت"},
				description = "انشاء التاريخ والوقت باستخدام تقويم محدد من السنة والشهر (اسم عربي أو رقم) واليوم والساعة" + " " + "والدقيقة والثانية والنانوثانية مع إزاحة زمنية محددة",
				usage = "انشاء_التاريخ_والوقت_بتقويم_وثانية_ونانوثانية_بإزاحة(تقويم, سنة, شهر, يوم, ساعة, دقيقة, ثانية, " + "نانوثانية, إزاحة)",
				parameterTypes = {
									String.class,
									Number.class,
									Object.class,
									Number.class,
									Number.class,
									Number.class,
									Number.class,
									Number.class,
									String.class
				},
				returnType = NaftahDateTime.class
	)
	public static NaftahDateTime createOffsetChronologyDateTime(String chronology,
																Number year,
																Object month,
																Number dayOfMonth,
																Number hour,
																Number minute,
																Number second,
																Number nanoOfSecond,
																String offset) {
		if (month instanceof String arabicMonth) {
			return NaftahDateTime
					.of(ChronologyUtils.getChronologyByName(chronology),
						year.intValue(),
						arabicMonth,
						dayOfMonth.intValue(),
						hour.intValue(),
						minute.intValue(),
						second.intValue(),
						nanoOfSecond.intValue(),
						NaftahTime.ZoneOrOffset.ofOffset(offset));
		}
		else if (month instanceof Number monthNumber) {
			return NaftahDateTime
					.of(ChronologyUtils.getChronologyByName(chronology),
						year.intValue(),
						monthNumber.intValue(),
						dayOfMonth.intValue(),
						hour.intValue(),
						minute.intValue(),
						second.intValue(),
						nanoOfSecond.intValue(),
						NaftahTime.ZoneOrOffset.ofOffset(offset));
		}
		else {
			throw newIllegalArgumentException(month);
		}

	}

	/**
	 * Converts the given {@link DateSupport} instance to the epoch day.
	 * <p>
	 * The epoch day is the count of days since 1970-01-01 (ISO).
	 *
	 * @param dateSupport the date or date-time object to convert
	 * @return the number of days since the epoch (1970-01-01)
	 */
	@NaftahFn(
				name = "تحويل_الى_يوم_منذ_الحقبة",
				aliases = {"منذ_الحقبة", "يوم_منذ_الحقبة"},
				description = "يحصل على عدد الأيام منذ 1 يناير 1970 من كائن التاريخ المدعوم",
				usage = "تحويل_الى_يوم_منذ_الحقبة(كائن_التاريخ)",
				parameterTypes = {DateSupport.class},
				returnType = long.class
	)
	public static long toEpochDay(DateSupport dateSupport) {
		return dateSupport.toEpochDay();
	}

	/**
	 * Retrieves the year from a given {@link DateSupport} instance.
	 *
	 * @param dateSupport the date or date-time object
	 * @return the year value
	 */
	@NaftahFn(
				name = "احصل_على_السنة",
				aliases = {"السنة", "سنة"},
				description = "الحصول على السنة من نقطة زمنية",
				usage = "احصل_على_السنة(نقطة_زمنية_)",
				parameterTypes = {DateSupport.class},
				returnType = int.class
	)
	public static int getYear(DateSupport dateSupport) {
		return dateSupport.getYear();
	}

	/**
	 * Gets the month from a temporal point.
	 *
	 * @param dateSupport the temporal point
	 * @return the month (1-12)
	 */
	@NaftahFn(
				name = "احصل_على_رقم_الشهر",
				aliases = {"رقم_الشهر", "الشهر", "شهر"},
				description = "الحصول على الشهر من نقطة زمنية",
				usage = "احصل_على_رقم_الشهر(نقطة_زمنية_)",
				parameterTypes = {DateSupport.class},
				returnType = int.class
	)
	public static int getMonthValue(DateSupport dateSupport) {
		return dateSupport.getMonthValue();
	}

	/**
	 * Retrieves the month from a given {@link DateSupport} instance.
	 *
	 * @param dateSupport the date or date-time object
	 * @return the month as a {@link String}, which could be an Arabic month name
	 */
	@NaftahFn(
				name = "احصل_على_الشهر",
				aliases = {"الشهر", "شهر"},
				description = "الحصول على اسم الشهر من كائن التاريخ أو التاريخ والوقت",
				usage = "احصل_على_الشهر(كائن_التاريخ)",
				parameterTypes = {DateSupport.class},
				returnType = String.class
	)
	public static String getMonth(DateSupport dateSupport) {
		return dateSupport.getMonth();
	}

	/**
	 * Gets the day of the month from a temporal point.
	 *
	 * @param dateSupport the temporal point
	 * @return the day of the month
	 */
	@NaftahFn(
				name = "احصل_على_اليوم",
				aliases = {"اليوم", "يوم", "يوم_الشهر"},
				description = "الحصول على اليوم من نقطة زمنية",
				usage = "احصل_على_اليوم(نقطة_زمنية_)",
				parameterTypes = {DateSupport.class},
				returnType = int.class
	)
	public static int getDayOfMonth(DateSupport dateSupport) {
		return dateSupport.getDayOfMonth();
	}

	/**
	 * Retrieves the day of the year from a given {@link DateSupport} instance.
	 *
	 * @param dateSupport the date or date-time object
	 * @return the day of the year (1–365/366)
	 */
	@NaftahFn(
				name = "احصل_على_يوم_السنة",
				aliases = {"اليوم", "يوم", "يوم_السنة"},
				description = "الحصول على رقم اليوم من السنة من كائن التاريخ أو التاريخ والوقت",
				usage = "احصل_على_يوم_السنة(كائن_التاريخ)",
				parameterTypes = {DateSupport.class},
				returnType = int.class
	)
	public static int getDayOfYear(DateSupport dateSupport) {
		return dateSupport.getDayOfYear();
	}

	/**
	 * Retrieves the day of the week from a given {@link DateSupport} instance.
	 *
	 * @param dateSupport the date or date-time object
	 * @return the day of the week as a {@link String} (e.g., "Monday", "Tuesday")
	 */
	@NaftahFn(
				name = "احصل_على_يوم_الأسبوع",
				aliases = {"اليوم", "يوم", "يوم_الأسبوع"},
				description = "الحصول على اسم يوم الأسبوع من كائن التاريخ أو التاريخ والوقت",
				usage = "احصل_على_يوم_الأسبوع(كائن_التاريخ)",
				parameterTypes = {DateSupport.class},
				returnType = String.class
	)
	public static String getDayOfWeek(DateSupport dateSupport) {
		return dateSupport.getDayOfWeek();
	}

	/**
	 * Checks whether the year of the given {@link DateSupport} instance is a leap year.
	 *
	 * @param dateSupport the date or date-time object
	 * @return {@code true} if the year is a leap year, {@code false} otherwise
	 */
	@NaftahFn(
				name = "هل_السنة_كبيسة",
				aliases = {"كبيسة", "سنة_كبيسة"},
				description = "التحقق مما إذا كانت السنة في كائن التاريخ أو التاريخ والوقت سنة كبيسة",
				usage = "هل_السنة_كبيسة(كائن_التاريخ)",
				parameterTypes = {DateSupport.class},
				returnType = boolean.class
	)
	public static boolean isLeapYear(DateSupport dateSupport) {
		return dateSupport.isLeapYear();
	}

	/**
	 * Returns the number of days in the month of the given {@link DateSupport} instance.
	 *
	 * @param dateSupport the date or date-time object
	 * @return the length of the month in days
	 */
	@NaftahFn(
				name = "عدد_ايام_الشهر",
				aliases = {"ايام_الشهر"},
				description = "الحصول على عدد الأيام في شهر الكائن الزمني المحدد",
				usage = "عدد_ايام_الشهر(كائن_التاريخ)",
				parameterTypes = {DateSupport.class},
				returnType = int.class
	)
	public static int lengthOfMonth(DateSupport dateSupport) {
		return dateSupport.lengthOfMonth();
	}

	/**
	 * Returns the number of days in the year of the given {@link DateSupport} instance.
	 *
	 * @param dateSupport the date or date-time object
	 * @return the length of the year in days
	 */
	@NaftahFn(
				name = "عدد_ايام_السنة",
				aliases = {"ايام_السنة"},
				description = "الحصول على عدد الأيام في سنة الكائن الزمني المحدد",
				usage = "عدد_ايام_السنة(كائن_التاريخ)",
				parameterTypes = {DateSupport.class},
				returnType = int.class
	)
	public static int lengthOfYear(DateSupport dateSupport) {
		return dateSupport.lengthOfYear();
	}

	/**
	 * Adds a specified number of years to the given {@link DateSupport} instance.
	 *
	 * @param dateSupport the date or date-time object
	 * @param yearsToAdd  the number of years to add (can be negative)
	 * @return a new {@link NaftahTemporalPoint} representing the resulting date-time
	 */
	@NaftahFn(
				name = "اضافة_سنوات",
				aliases = {"اضافة_س", "اضافة"},
				description = "اضافة عدد محدد من السنوات إلى كائن التاريخ المحدد",
				usage = "اضافة_سنوات(كائن_التاريخ, عدد_السنوات)",
				parameterTypes = {DateSupport.class, Number.class},
				returnType = NaftahTemporalPoint.class
	)
	public static NaftahTemporalPoint plusYears(DateSupport dateSupport, Number yearsToAdd) {
		return dateSupport.plusYears(yearsToAdd.longValue());
	}

	/**
	 * Adds a specified number of months to the given {@link DateSupport} instance.
	 *
	 * @param dateSupport the date or date-time object
	 * @param monthsToAdd the number of months to add (can be negative)
	 * @return a new {@link NaftahTemporalPoint} representing the resulting date-time
	 */
	@NaftahFn(
				name = "اضافة_اشهر",
				aliases = {"اضافة_ش", "اضافة"},
				description = "اضافة عدد محدد من الأشهر إلى كائن التاريخ المحدد",
				usage = "اضافة_اشهر(كائن_التاريخ, عدد_الأشهر)",
				parameterTypes = {DateSupport.class, Number.class},
				returnType = NaftahTemporalPoint.class
	)
	public static NaftahTemporalPoint plusMonths(DateSupport dateSupport, Number monthsToAdd) {
		return dateSupport.plusMonths(monthsToAdd.longValue());
	}

	/**
	 * Adds a specified number of weeks to the given {@link DateSupport} instance.
	 *
	 * @param dateSupport the date or date-time object
	 * @param weeksToAdd  the number of weeks to add (can be negative)
	 * @return a new {@link NaftahTemporalPoint} representing the resulting date-time
	 */
	@NaftahFn(
				name = "اضافة_اسابيع",
				aliases = {"اضافة_ا", "اضافة"},
				description = "اضافة عدد محدد من الأسابيع إلى كائن التاريخ المحدد",
				usage = "اضافة_اسابيع(كائن_التاريخ, عدد_الأسابيع)",
				parameterTypes = {DateSupport.class, Number.class},
				returnType = NaftahTemporalPoint.class
	)
	public static NaftahTemporalPoint plusWeeks(DateSupport dateSupport, Number weeksToAdd) {
		return dateSupport.plusWeeks(weeksToAdd.longValue());
	}

	/**
	 * Adds a specified number of days to the given {@link DateSupport} instance.
	 *
	 * @param dateSupport the date or date-time object
	 * @param daysToAdd   the number of days to add (can be negative)
	 * @return a new {@link NaftahTemporalPoint} representing the resulting date-time
	 */
	@NaftahFn(
				name = "اضافة_ايام",
				aliases = {"اضافة_ي", "اضافة"},
				description = "اضافة عدد محدد من الأيام إلى كائن التاريخ المحدد",
				usage = "اضافة_ايام(كائن_التاريخ, عدد_الأيام)",
				parameterTypes = {DateSupport.class, Number.class},
				returnType = NaftahTemporalPoint.class
	)
	public static NaftahTemporalPoint plusDays(DateSupport dateSupport, Number daysToAdd) {
		return dateSupport.plusDays(daysToAdd.longValue());
	}

	/**
	 * Subtracts a specified number of years from the given {@link DateSupport} instance.
	 *
	 * @param dateSupport     the date or date-time object
	 * @param yearsToSubtract the number of years to subtract (can be negative)
	 * @return a new {@link NaftahTemporalPoint} representing the resulting date-time
	 */
	@NaftahFn(
				name = "طرح_سنوات",
				aliases = {"طرح_س", "طرح"},
				description = "طرح عدد محدد من السنوات من كائن التاريخ المحدد",
				usage = "طرح_سنوات(كائن_التاريخ, عدد_السنوات)",
				parameterTypes = {DateSupport.class, Number.class},
				returnType = NaftahTemporalPoint.class
	)
	public static NaftahTemporalPoint minusYears(DateSupport dateSupport, Number yearsToSubtract) {
		return dateSupport.minusYears(yearsToSubtract.longValue());
	}

	/**
	 * Subtracts a specified number of months from the given {@link DateSupport} instance.
	 *
	 * @param dateSupport      the date or date-time object
	 * @param monthsToSubtract the number of months to subtract (can be negative)
	 * @return a new {@link NaftahTemporalPoint} representing the resulting date-time
	 */
	@NaftahFn(
				name = "طرح_شهور",
				aliases = {"طرح_أشهر", "طرح_ش", "طرح"},
				description = "طرح عدد محدد من الأشهر من كائن التاريخ المحدد",
				usage = "طرح_شهور(كائن_التاريخ, عدد_الشهور)",
				parameterTypes = {DateSupport.class, Number.class},
				returnType = NaftahTemporalPoint.class
	)
	public static NaftahTemporalPoint minusMonths(DateSupport dateSupport, Number monthsToSubtract) {
		return dateSupport.minusMonths(monthsToSubtract.longValue());
	}

	/**
	 * Subtracts a specified number of weeks from the given {@link DateSupport} instance.
	 *
	 * @param dateSupport     the date or date-time object
	 * @param weeksToSubtract the number of weeks to subtract (can be negative)
	 * @return a new {@link NaftahTemporalPoint} representing the resulting date-time
	 */
	@NaftahFn(
				name = "طرح_اسابيع",
				aliases = {"طرح_ا", "طرح"},
				description = "طرح عدد محدد من الأسابيع من كائن التاريخ المحدد",
				usage = "طرح_اسابيع(كائن_التاريخ, عدد_الأسابيع)",
				parameterTypes = {DateSupport.class, Number.class},
				returnType = NaftahTemporalPoint.class
	)
	public static NaftahTemporalPoint minusWeeks(DateSupport dateSupport, Number weeksToSubtract) {
		return dateSupport.minusWeeks(weeksToSubtract.longValue());
	}

	/**
	 * Subtracts a specified number of days from the given {@link DateSupport} instance.
	 *
	 * @param dateSupport    the date or date-time object
	 * @param daysToSubtract the number of days to subtract (can be negative)
	 * @return a new {@link NaftahTemporalPoint} representing the resulting date-time
	 */
	@NaftahFn(
				name = "طرح_ايام",
				aliases = {"طرح_ي", "طرح"},
				description = "طرح عدد محدد من الأيام من كائن التاريخ المحدد",
				usage = "طرح_ايام(كائن_التاريخ, عدد_الأيام)",
				parameterTypes = {DateSupport.class, Number.class},
				returnType = NaftahTemporalPoint.class
	)
	public static NaftahTemporalPoint minusDays(DateSupport dateSupport, Number daysToSubtract) {
		return dateSupport.minusDays(daysToSubtract.longValue());
	}

	/**
	 * Gets the hour of the day from a temporal point.
	 *
	 * @param timeSupport the temporal point
	 * @return the hour (0-23)
	 */
	@NaftahFn(
				name = "احصل_على_الساعة",
				aliases = {"ساعة"},
				description = "الحصول على الساعة من نقطة زمنية",
				usage = "احصل_على_الساعة(نقطة_زمنية_)",
				parameterTypes = {TimeSupport.class},
				returnType = int.class
	)
	public static int getHour(TimeSupport timeSupport) {
		return timeSupport.getHour();
	}

	/**
	 * Gets the minute of the hour from a temporal point.
	 *
	 * @param timeSupport the temporal point
	 * @return the minute
	 */
	@NaftahFn(
				name = "احصل_على_الدقيقة",
				aliases = {"دقيقة"},
				description = "الحصول على الدقيقة من نقطة زمنية",
				usage = "احصل_على_الدقيقة(نقطة_زمنية_)",
				parameterTypes = {TimeSupport.class},
				returnType = int.class
	)
	public static int getMinute(TimeSupport timeSupport) {
		return timeSupport.getMinute();
	}

	/**
	 * Gets the second of the minute from a temporal point.
	 *
	 * @param timeSupport the temporal point
	 * @return the second
	 */
	@NaftahFn(
				name = "احصل_على_الثانية",
				aliases = {"ثانية"},
				description = "الحصول على الثانية من نقطة زمنية",
				usage = "احصل_على_الثانية(نقطة_زمنية_)",
				parameterTypes = {TimeSupport.class},
				returnType = int.class
	)
	public static int getSecond(TimeSupport timeSupport) {
		return timeSupport.getSecond();
	}

	/**
	 * Gets the millisecond of the second from a temporal point.
	 *
	 * @param timeSupport the temporal point
	 * @return the millisecond
	 */
	@NaftahFn(
				name = "احصل_على_الملي_ثانية",
				aliases = {"احصل_على_الملي", "احصل_على_المللي_ثانية", "ملي", "احصل_على_المللي", "مللي"},
				description = "الحصول على المللي ثانية من نقطة زمنية",
				usage = "احصل_على_الملي_ثانية(نقطة_زمنية_)",
				parameterTypes = {TimeSupport.class},
				returnType = int.class
	)
	public static int getMilli(TimeSupport timeSupport) {
		return timeSupport.getMilli();
	}

	/**
	 * Gets the nanosecond of the second from a temporal point.
	 *
	 * @param timeSupport the temporal point
	 * @return the nanosecond
	 */
	@NaftahFn(
				name = "احصل_على_النانو_ثانية",
				aliases = {"احصل_على_نانو", "نانو"},
				description = "الحصول على النانو ثانية من نقطة زمنية",
				usage = "احصل_على_النانو_ثانية(نقطة_زمنية_)",
				parameterTypes = {TimeSupport.class},
				returnType = int.class
	)
	public static int getNano(TimeSupport timeSupport) {
		return timeSupport.getNano();
	}

	/**
	 * Adds a specified number of hours to the given {@link TimeSupport} instance.
	 *
	 * @param timeSupport the time object
	 * @param hoursToAdd  the number of hours to add (can be negative)
	 * @return a new {@link NaftahTemporalPoint} representing the resulting time
	 */
	@NaftahFn(
				name = "اضافة_ساعات",
				aliases = {"اضافة_س", "اضافة"},
				description = "اضافة عدد محدد من الساعات إلى كائن الوقت المحدد",
				usage = "اضافة_ساعات(كائن_الوقت, عدد_الساعات)",
				parameterTypes = {TimeSupport.class, Number.class},
				returnType = NaftahTemporalPoint.class
	)
	public static NaftahTemporalPoint plusHours(TimeSupport timeSupport, Number hoursToAdd) {
		return timeSupport.plusHours(hoursToAdd.longValue());
	}

	/**
	 * Adds a specified number of minutes to the given {@link TimeSupport} instance.
	 *
	 * @param timeSupport  the time object
	 * @param minutesToAdd the number of minutes to add (can be negative)
	 * @return a new {@link NaftahTemporalPoint} representing the resulting time
	 */
	@NaftahFn(
				name = "اضافة_دقائق",
				aliases = {"اضافة_د", "اضافة"},
				description = "اضافة عدد محدد من الدقائق إلى كائن الوقت المحدد",
				usage = "اضافة_دقائق(كائن_الوقت, عدد_الدقائق)",
				parameterTypes = {TimeSupport.class, Number.class},
				returnType = NaftahTemporalPoint.class
	)
	public static NaftahTemporalPoint plusMinutes(TimeSupport timeSupport, Number minutesToAdd) {
		return timeSupport.plusMinutes(minutesToAdd.longValue());
	}

	/**
	 * Adds a specified number of seconds to the given {@link TimeSupport} instance.
	 *
	 * @param timeSupport  the time object
	 * @param secondsToAdd the number of seconds to add (can be negative)
	 * @return a new {@link NaftahTemporalPoint} representing the resulting time
	 */
	@NaftahFn(
				name = "اضافة_ثواني",
				aliases = {"اضافة_ث", "اضافة"},
				description = "اضافة عدد محدد من الثواني إلى كائن الوقت المحدد",
				usage = "اضافة_ثواني(كائن_الوقت, عدد_الثواني)",
				parameterTypes = {TimeSupport.class, Number.class},
				returnType = NaftahTemporalPoint.class
	)
	public static NaftahTemporalPoint plusSeconds(TimeSupport timeSupport, Number secondsToAdd) {
		return timeSupport.plusSeconds(secondsToAdd.longValue());
	}

	/**
	 * Adds a specified number of nanoseconds to the given {@link TimeSupport} instance.
	 *
	 * @param timeSupport the time object
	 * @param nanosToAdd  the number of nanoseconds to add (can be negative)
	 * @return a new {@link NaftahTemporalPoint} representing the resulting time
	 */
	@NaftahFn(
				name = "اضافة_نانوثواني",
				aliases = {"أضف_نانو", "اضافة_ن", "اضافة"},
				description = "اضافة عدد محدد من النانوثواني إلى كائن الوقت المحدد",
				usage = "اضافة_نانوثواني(كائن_الوقت, عدد_النانوثواني)",
				parameterTypes = {TimeSupport.class, Number.class},
				returnType = NaftahTemporalPoint.class
	)
	public static NaftahTemporalPoint plusNanos(TimeSupport timeSupport, Number nanosToAdd) {
		return timeSupport.plusNanos(nanosToAdd.longValue());
	}

	/**
	 * Subtracts a specified number of hours from the given {@link TimeSupport} instance.
	 *
	 * @param timeSupport     the time object
	 * @param hoursToSubtract the number of hours to subtract (can be negative)
	 * @return a new {@link NaftahTemporalPoint} representing the resulting time
	 */
	@NaftahFn(
				name = "طرح_ساعات",
				aliases = {"طرح_س", "طرح"},
				description = "طرح عدد محدد من الساعات من كائن الوقت المحدد",
				usage = "طرح_ساعات(كائن_الوقت, عدد_الساعات)",
				parameterTypes = {TimeSupport.class, Number.class},
				returnType = NaftahTemporalPoint.class
	)
	public static NaftahTemporalPoint minusHours(TimeSupport timeSupport, Number hoursToSubtract) {
		return timeSupport.minusHours(hoursToSubtract.longValue());
	}

	/**
	 * Subtracts a specified number of minutes from the given {@link TimeSupport} instance.
	 *
	 * @param timeSupport       the time object
	 * @param minutesToSubtract the number of minutes to subtract (can be negative)
	 * @return a new {@link NaftahTemporalPoint} representing the resulting time
	 */
	@NaftahFn(
				name = "طرح_دقائق",
				aliases = {"طرح_د", "طرح"},
				description = "طرح عدد محدد من الدقائق من كائن الوقت المحدد",
				usage = "طرح_دقائق(كائن_الوقت, عدد_الدقائق)",
				parameterTypes = {TimeSupport.class, Number.class},
				returnType = NaftahTemporalPoint.class
	)
	public static NaftahTemporalPoint minusMinutes(TimeSupport timeSupport, Number minutesToSubtract) {
		return timeSupport.minusMinutes(minutesToSubtract.longValue());
	}

	/**
	 * Subtracts a specified number of seconds from the given {@link TimeSupport} instance.
	 *
	 * @param timeSupport       the time object
	 * @param secondsToSubtract the number of seconds to subtract (can be negative)
	 * @return a new {@link NaftahTemporalPoint} representing the resulting time
	 */
	@NaftahFn(
				name = "طرح_ثواني",
				aliases = {"طرح_ث", "طرح"},
				description = "طرح عدد محدد من الثواني من كائن الوقت المحدد",
				usage = "طرح_ثواني(كائن_الوقت, عدد_الثواني)",
				parameterTypes = {TimeSupport.class, Number.class},
				returnType = NaftahTemporalPoint.class
	)
	public static NaftahTemporalPoint minusSeconds(TimeSupport timeSupport, Number secondsToSubtract) {
		return timeSupport.minusSeconds(secondsToSubtract.longValue());
	}

	/**
	 * Subtracts a specified number of nanoseconds from the given {@link TimeSupport} instance.
	 *
	 * @param timeSupport     the time object
	 * @param nanosToSubtract the number of nanoseconds to subtract (can be negative)
	 * @return a new {@link NaftahTemporalPoint} representing the resulting time
	 */
	@NaftahFn(
				name = "طرح_نانوثواني",
				aliases = {"طرح_نانو", "طرح_ن", "طرح"},
				description = "طرح عدد محدد من النانوثواني من كائن الوقت المحدد",
				usage = "طرح_نانوثواني(كائن_الوقت, عدد_النانوثواني)",
				parameterTypes = {TimeSupport.class, Number.class},
				returnType = NaftahTemporalPoint.class
	)
	public static NaftahTemporalPoint minusNanos(TimeSupport timeSupport, Number nanosToSubtract) {
		return timeSupport.minusNanos(nanosToSubtract.longValue());
	}

	/**
	 * Creates a zero duration.
	 *
	 * @return a {@link NaftahDuration} representing zero duration
	 */
	@NaftahFn(
				name = "أنشئ_مدة_صفرية",
				aliases = {"أنشئ_مدة"},
				description = "إنشاء مدة صفرية",
				usage = "أنشئ_مدة_صفرية()",
				returnType = NaftahDuration.class
	)
	public static NaftahDuration createZeroDuration() {
		return NaftahDuration.ofZero();
	}

	/**
	 * Creates a duration from the specified number of days.
	 *
	 * @param days the number of days
	 * @return a {@link NaftahDuration} representing the specified days
	 */
	@NaftahFn(
				name = "أنشئ_مدة_من_أيام",
				aliases = {"أنشئ_مدة"},
				description = "إنشاء مدة من عدد أيام",
				usage = "أنشئ_مدة_من_أيام(أيام)",
				parameterTypes = {Number.class},
				returnType = NaftahDuration.class
	)
	public static NaftahDuration createDurationFromDays(Number days) {
		return NaftahDuration.ofDays(days.longValue());
	}

	/**
	 * Creates a duration from the specified number of hours.
	 *
	 * @param hours the number of hours
	 * @return a {@link NaftahDuration} representing the specified hours
	 */
	@NaftahFn(
				name = "أنشئ_مدة_من_ساعات",
				aliases = {"أنشئ_مدة"},
				description = "إنشاء مدة من عدد ساعات",
				usage = "أنشئ_مدة_من_ساعات(ساعات)",
				parameterTypes = {Number.class},
				returnType = NaftahDuration.class
	)
	public static NaftahDuration createDurationFromHours(Number hours) {
		return NaftahDuration.ofHours(hours.longValue());
	}

	/**
	 * Creates a duration from the specified number of minutes.
	 *
	 * @param minutes the number of minutes
	 * @return a {@link NaftahDuration} representing the specified minutes
	 */
	@NaftahFn(
				name = "أنشئ_مدة_من_دقائق",
				aliases = {"أنشئ_مدة"},
				description = "إنشاء مدة من عدد دقائق",
				usage = "أنشئ_مدة_من_دقائق(دقائق)",
				parameterTypes = {Number.class},
				returnType = NaftahDuration.class
	)
	public static NaftahDuration createDurationFromMinutes(Number minutes) {
		return NaftahDuration.ofMinutes(minutes.longValue());
	}

	/**
	 * Creates a duration from the specified number of seconds.
	 *
	 * @param seconds the number of seconds
	 * @return a {@link NaftahDuration} representing the specified seconds
	 */
	@NaftahFn(
				name = "أنشئ_مدة_من_ثواني",
				aliases = {"أنشئ_مدة"},
				description = "إنشاء مدة من عدد ثواني",
				usage = "أنشئ_مدة_من_ثواني(ثواني)",
				parameterTypes = {Number.class},
				returnType = NaftahDuration.class
	)
	public static NaftahDuration createDurationFromSeconds(Number seconds) {
		return NaftahDuration.ofSeconds(seconds.longValue());
	}

	/**
	 * Creates a duration from seconds with a nanosecond adjustment.
	 *
	 * @param seconds        the number of seconds
	 * @param nanoAdjustment the nanosecond adjustment
	 * @return a {@link NaftahDuration} representing the specified seconds and nano adjustment
	 */
	@NaftahFn(
				name = "أنشئ_مدة_من_ثواني_مع_نانو",
				aliases = {"أنشئ_مدة"},
				description = "إنشاء مدة من ثواني مع تعديل النانوثانية",
				usage = "أنشئ_مدة_من_ثواني_مع_نانو(ثواني, نانو)",
				parameterTypes = {Number.class, Number.class},
				returnType = NaftahDuration.class
	)
	public static NaftahDuration createDurationFromSecondsAndNano(Number seconds, Number nanoAdjustment) {
		return NaftahDuration.ofSeconds(seconds.longValue(), nanoAdjustment.longValue());
	}

	/**
	 * Creates a duration from the specified number of milliseconds.
	 *
	 * @param millis the number of milliseconds
	 * @return a {@link NaftahDuration} representing the specified milliseconds
	 */
	@NaftahFn(
				name = "أنشئ_مدة_من_مللي",
				aliases = {"أنشئ_مدة"},
				description = "إنشاء مدة من عدد مللي ثانية",
				usage = "أنشئ_مدة_من_مللي(مللي)",
				parameterTypes = {Number.class},
				returnType = NaftahDuration.class
	)
	public static NaftahDuration createDurationFromMillis(Number millis) {
		return NaftahDuration.ofMillis(millis.longValue());
	}

	/**
	 * Creates a duration from the specified number of nanoseconds.
	 *
	 * @param nanos the number of nanoseconds
	 * @return a {@link NaftahDuration} representing the specified nanoseconds
	 */
	@NaftahFn(
				name = "أنشئ_مدة_من_نانو",
				aliases = {"أنشئ_مدة"},
				description = "إنشاء مدة من عدد نانو ثانية",
				usage = "أنشئ_مدة_من_نانو(نانو)",
				parameterTypes = {Number.class},
				returnType = NaftahDuration.class
	)
	public static NaftahDuration createDurationFromNanos(Number nanos) {
		return NaftahDuration.ofNanos(nanos.longValue());
	}

	/**
	 * Creates a zero period.
	 *
	 * @return a {@link NaftahPeriod} representing zero period
	 */
	@NaftahFn(
				name = "أنشئ_فترة_صفرية",
				aliases = {"أنشئ_فترة"},
				description = "إنشاء فترة صفرية",
				usage = "أنشئ_فترة_صفرية()",
				returnType = NaftahPeriod.class
	)
	public static NaftahPeriod createZeroPeriod() {
		return NaftahPeriod.ofZero();
	}

	/**
	 * Creates a period from the specified number of years.
	 *
	 * @param years the number of years
	 * @return a {@link NaftahPeriod} representing the specified years
	 */
	@NaftahFn(
				name = "أنشئ_فترة_من_سنوات",
				aliases = {"أنشئ_فترة"},
				description = "إنشاء فترة من عدد سنوات",
				usage = "أنشئ_فترة_من_سنوات(سنوات)",
				parameterTypes = {Number.class},
				returnType = NaftahPeriod.class
	)
	public static NaftahPeriod createPeriodFromYears(Number years) {
		return NaftahPeriod.ofYears(years.intValue());
	}

	/**
	 * Creates a period from the specified number of months.
	 *
	 * @param months the number of months
	 * @return a {@link NaftahPeriod} representing the specified months
	 */
	@NaftahFn(
				name = "أنشئ_فترة_من_شهور",
				aliases = {"أنشئ_فترة"},
				description = "إنشاء فترة من عدد أشهر",
				usage = "أنشئ_فترة_من_شهور(شهور)",
				parameterTypes = {Number.class},
				returnType = NaftahPeriod.class
	)
	public static NaftahPeriod createPeriodFromMonths(Number months) {
		return NaftahPeriod.ofMonths(months.intValue());
	}

	/**
	 * Creates a period from the specified number of weeks.
	 *
	 * @param weeks the number of weeks
	 * @return a {@link NaftahPeriod} representing the specified weeks
	 */
	@NaftahFn(
				name = "أنشئ_فترة_من_أسابيع",
				aliases = {"أنشئ_فترة"},
				description = "إنشاء فترة من عدد أسابيع",
				usage = "أنشئ_فترة_من_أسابيع(أسابيع)",
				parameterTypes = {Number.class},
				returnType = NaftahPeriod.class
	)
	public static NaftahPeriod createPeriodFromWeeks(Number weeks) {
		return NaftahPeriod.ofWeeks(weeks.intValue());
	}

	/**
	 * Creates a period from the specified number of days.
	 *
	 * @param days the number of days
	 * @return a {@link NaftahPeriod} representing the specified days
	 */
	@NaftahFn(
				name = "أنشئ_فترة_من_أيام",
				aliases = {"أنشئ_فترة"},
				description = "إنشاء فترة من عدد أيام",
				usage = "أنشئ_فترة_من_أيام(أيام)",
				parameterTypes = {Number.class},
				returnType = NaftahPeriod.class
	)
	public static NaftahPeriod createPeriodFromDays(Number days) {
		return NaftahPeriod.ofDays(days.intValue());
	}

	/**
	 * Creates a period with duration of zero (both period and duration are zero).
	 *
	 * @return a {@link NaftahPeriodWithDuration} representing zero period and zero duration
	 */
	@NaftahFn(
				name = "أنشئ_فترة_و_مدة_صفرية",
				aliases = {"أنشئ_فترة"},
				description = "إنشاء فترة و مدة صفرية",
				usage = "أنشئ_فترة_و_مدة_صفرية()",
				returnType = NaftahPeriodWithDuration.class
	)
	public static NaftahPeriodWithDuration createZeroPeriodWithDuration() {
		return NaftahPeriodWithDuration.ofZero();
	}

	/**
	 * Creates a period with duration from the specified number of years.
	 *
	 * @param years the number of years
	 * @return a {@link NaftahPeriodWithDuration} with specified years and zero duration
	 */
	@NaftahFn(
				name = "أنشئ_فترة_و_مدة_من_سنوات",
				aliases = {"أنشئ_فترة"},
				description = "إنشاء فترة و مدة من عدد سنوات",
				usage = "أنشئ_فترة_و_مدة_من_سنوات(سنوات)",
				parameterTypes = {Number.class},
				returnType = NaftahPeriodWithDuration.class
	)
	public static NaftahPeriodWithDuration createPeriodWithDurationFromYears(Number years) {
		return NaftahPeriodWithDuration.ofYears(years.intValue());
	}

	/**
	 * Creates a period with duration from the specified number of months.
	 *
	 * @param months the number of months
	 * @return a {@link NaftahPeriodWithDuration} with specified months and zero duration
	 */
	@NaftahFn(
				name = "أنشئ_فترة_و_مدة_من_شهور",
				aliases = {"أنشئ_فترة_و_مدة"},
				description = "إنشاء فترة و مدة من عدد أشهر",
				usage = "أنشئ_فترة_و_مدة_من_شهور(شهور)",
				parameterTypes = {Number.class},
				returnType = NaftahPeriodWithDuration.class
	)
	public static NaftahPeriodWithDuration createPeriodWithDurationFromMonths(Number months) {
		return NaftahPeriodWithDuration.ofMonths(months.intValue());
	}

	/**
	 * Creates a period with duration from the specified number of weeks.
	 *
	 * @param weeks the number of weeks
	 * @return a {@link NaftahPeriodWithDuration} with specified weeks and zero duration
	 */
	@NaftahFn(
				name = "أنشئ_فترة_و_مدة_من_أسابيع",
				aliases = {"أنشئ_فترة_و_مدة"},
				description = "إنشاء فترة و مدة من عدد أسابيع",
				usage = "أنشئ_فترة_و_مدة_من_أسابيع(أسابيع)",
				parameterTypes = {Number.class},
				returnType = NaftahPeriodWithDuration.class
	)
	public static NaftahPeriodWithDuration createPeriodWithDurationFromWeeks(Number weeks) {
		return NaftahPeriodWithDuration.ofWeeks(weeks.intValue());
	}

	/**
	 * Creates a period with duration from the specified number of days.
	 *
	 * @param days the number of days
	 * @return a {@link NaftahPeriodWithDuration} with specified days and zero duration
	 */
	@NaftahFn(
				name = "أنشئ_فترة_و_مدة_من_أيام",
				aliases = {"أنشئ_فترة_و_مدة"},
				description = "إنشاء فترة و مدة من عدد أيام",
				usage = "أنشئ_فترة_و_مدة_من_أيام(أيام)",
				parameterTypes = {Number.class},
				returnType = NaftahPeriodWithDuration.class
	)
	public static NaftahPeriodWithDuration createPeriodWithDurationFromDays(Number days) {
		return NaftahPeriodWithDuration.ofDays(days.intValue());
	}

	/**
	 * Creates a period with duration from the specified number of hours.
	 *
	 * @param hours the number of hours
	 * @return a {@link NaftahPeriodWithDuration} with zero period and specified hours as duration
	 */
	@NaftahFn(
				name = "أنشئ_فترة_و_مدة_من_ساعات",
				aliases = {"أنشئ_فترة_و_مدة"},
				description = "إنشاء فترة و مدة من عدد ساعات",
				usage = "أنشئ_فترة_و_مدة_من_ساعات(ساعات)",
				parameterTypes = {Number.class},
				returnType = NaftahPeriodWithDuration.class
	)
	public static NaftahPeriodWithDuration createPeriodWithDurationFromHours(Number hours) {
		return NaftahPeriodWithDuration.ofHours(hours.longValue());
	}

	/**
	 * Creates a period with duration from the specified number of minutes.
	 *
	 * @param minutes the number of minutes
	 * @return a {@link NaftahPeriodWithDuration} with zero period and specified minutes as duration
	 */
	@NaftahFn(
				name = "أنشئ_فترة_و_مدة_من_دقائق",
				aliases = {"أنشئ_فترة_و_مدة"},
				description = "إنشاء فترة و مدة من عدد دقائق",
				usage = "أنشئ_فترة_و_مدة_من_دقائق(دقائق)",
				parameterTypes = {Number.class},
				returnType = NaftahPeriodWithDuration.class
	)
	public static NaftahPeriodWithDuration createPeriodWithDurationFromMinutes(Number minutes) {
		return NaftahPeriodWithDuration.ofMinutes(minutes.longValue());
	}

	/**
	 * Creates a period with duration from the specified number of seconds.
	 *
	 * @param seconds the number of seconds
	 * @return a {@link NaftahPeriodWithDuration} with zero period and specified seconds as duration
	 */
	@NaftahFn(
				name = "أنشئ_فترة_و_مدة_من_ثواني",
				aliases = {"أنشئ_فترة_و_مدة"},
				description = "إنشاء فترة و مدة من عدد ثواني",
				usage = "أنشئ_فترة_و_مدة_من_ثواني(ثواني)",
				parameterTypes = {Number.class},
				returnType = NaftahPeriodWithDuration.class
	)
	public static NaftahPeriodWithDuration createPeriodWithDurationFromSeconds(Number seconds) {
		return NaftahPeriodWithDuration.ofSeconds(seconds.longValue());
	}

	/**
	 * Creates a period with duration from the specified seconds and nanosecond adjustment.
	 *
	 * @param seconds        the number of seconds
	 * @param nanoAdjustment the nanosecond adjustment
	 * @return a {@link NaftahPeriodWithDuration} with zero period and adjusted duration
	 */
	@NaftahFn(
				name = "أنشئ_فترة_و_مدة_من_ثواني_مع_نانو",
				aliases = {"أنشئ_فترة_و_مدة"},
				description = "إنشاء فترة و مدة من ثواني مع تعديل النانوثانية",
				usage = "أنشئ_فترة_و_مدة_من_ثواني_مع_نانو(ثواني, نانو)",
				parameterTypes = {Number.class, Number.class},
				returnType = NaftahPeriodWithDuration.class
	)
	public static NaftahPeriodWithDuration createPeriodWithDurationFromSecondsAndNano(  Number seconds,
																						Number nanoAdjustment) {
		return NaftahPeriodWithDuration.ofSeconds(seconds.longValue(), nanoAdjustment.longValue());
	}

	/**
	 * Creates a period with duration from the specified number of milliseconds.
	 *
	 * @param millis the number of milliseconds
	 * @return a {@link NaftahPeriodWithDuration} with zero period and specified milliseconds as duration
	 */
	@NaftahFn(
				name = "أنشئ_فترة_و_مدة_من_مللي",
				aliases = {"أنشئ_فترة_و_مدة"},
				description = "إنشاء فترة و مدة من عدد مللي ثانية",
				usage = "أنشئ_فترة_و_مدة_من_مللي(مللي)",
				parameterTypes = {Number.class},
				returnType = NaftahPeriodWithDuration.class
	)
	public static NaftahPeriodWithDuration createPeriodWithDurationFromMillis(Number millis) {
		return NaftahPeriodWithDuration.ofMillis(millis.longValue());
	}

	/**
	 * Creates a period with duration from the specified number of nanoseconds.
	 *
	 * @param nanos the number of nanoseconds
	 * @return a {@link NaftahPeriodWithDuration} with zero period and specified nanoseconds as duration
	 */
	@NaftahFn(
				name = "أنشئ_فترة_و_مدة_من_نانو",
				aliases = {"أنشئ_فترة_و_مدة"},
				description = "إنشاء فترة و مدة من عدد نانو ثانية",
				usage = "أنشئ_فترة_و_مدة_من_نانو(نانو)",
				parameterTypes = {Number.class},
				returnType = NaftahPeriodWithDuration.class
	)
	public static NaftahPeriodWithDuration createPeriodWithDurationFromNanos(Number nanos) {
		return NaftahPeriodWithDuration.ofNanos(nanos.longValue());
	}

	/**
	 * Adds a number of years to the given period.
	 *
	 * @param p     the original period
	 * @param years the number of years to add
	 * @return a {@link NaftahPeriod} with years added
	 */
	@NaftahFn(
				name = "أضف_سنوات_لفترة",
				aliases = {"أضف_سنوات", "اضافة_س", "اضافة"},
				description = "إضافة عدد من السنوات إلى فترة",
				usage = "أضف_سنوات_لفترة(فترة_, سنوات)",
				parameterTypes = {NaftahPeriod.class, Number.class},
				returnType = NaftahPeriod.class
	)
	public static NaftahPeriod plusYears(NaftahPeriod p, Number years) {
		return p.plusYears(years.longValue());
	}

	/**
	 * Adds a number of months to the given period.
	 *
	 * @param p      the original period
	 * @param months the number of months to add
	 * @return a {@link NaftahPeriod} with months added
	 */
	@NaftahFn(
				name = "أضف_أشهر_لفترة",
				aliases = {"أضف_أشهر", "اضافة_ش", "اضافة"},
				description = "إضافة عدد من الأشهر إلى فترة",
				usage = "أضف_أشهر_لفترة(فترة_, أشهر)",
				parameterTypes = {NaftahPeriod.class, Number.class},
				returnType = NaftahPeriod.class
	)
	public static NaftahPeriod plusMonths(NaftahPeriod p, Number months) {
		return p.plusMonths(months.longValue());
	}

	/**
	 * Adds a number of days to the given period.
	 *
	 * @param p    the original period
	 * @param days the number of days to add
	 * @return a {@link NaftahPeriod} with days added
	 */
	@NaftahFn(
				name = "أضف_أيام_لفترة",
				aliases = {"أضف_أيام", "اضافة_ي", "اضافة"},
				description = "إضافة عدد من الأيام إلى فترة",
				usage = "أضف_أيام_لفترة(فترة_, أيام)",
				parameterTypes = {NaftahPeriod.class, Number.class},
				returnType = NaftahPeriod.class
	)
	public static NaftahPeriod plusDays(NaftahPeriod p, Number days) {
		return p.plusDays(days.longValue());
	}

	/**
	 * Subtracts a number of years from the given period.
	 *
	 * @param p     the original period
	 * @param years the number of years to subtract
	 * @return a {@link NaftahPeriod} with years subtracted
	 */
	@NaftahFn(
				name = "اطرح_سنوات_من_الفترة",
				aliases = {"طرح_سنوات", "طرح_س", "طرح"},
				description = "طرح عدد من السنوات من فترة",
				usage = "اطرح_سنوات_من_الفترة(فترة_, سنوات)",
				parameterTypes = {NaftahPeriod.class, Number.class},
				returnType = NaftahPeriod.class
	)
	public static NaftahPeriod minusYears(NaftahPeriod p, Number years) {
		return p.minusYears(years.longValue());
	}

	/**
	 * Subtracts a number of months from the given period.
	 *
	 * @param p      the original period
	 * @param months the number of months to subtract
	 * @return a {@link NaftahPeriod} with months subtracted
	 */
	@NaftahFn(
				name = "اطرح_أشهر_من_الفترة",
				aliases = {"طرح_أشهر", "طرح_شهور", "طرح_ش", "طرح"},
				description = "طرح عدد من الأشهر من فترة",
				usage = "اطرح_أشهر_من_الفترة(فترة_, أشهر)",
				parameterTypes = {NaftahPeriod.class, Number.class},
				returnType = NaftahPeriod.class
	)
	public static NaftahPeriod minusMonths(NaftahPeriod p, Number months) {
		return p.minusMonths(months.longValue());
	}

	/**
	 * Subtracts a number of days from the given period.
	 *
	 * @param p    the original period
	 * @param days the number of days to subtract
	 * @return a {@link NaftahPeriod} with days subtracted
	 */
	@NaftahFn(
				name = "اطرح_أيام_من_الفترة",
				aliases = {"اطرح_أيام", "طرح_ي", "طرح"},
				description = "طرح عدد من الأيام من فترة",
				usage = "اطرح_أيام_من_الفترة(فترة_, أيام)",
				parameterTypes = {NaftahPeriod.class, Number.class},
				returnType = NaftahPeriod.class
	)
	public static NaftahPeriod minusDays(NaftahPeriod p, Number days) {
		return p.minusDays(days.longValue());
	}

	/**
	 * Adds a number of days to a given duration.
	 *
	 * @param d    the original duration
	 * @param days the number of days to add
	 * @return a {@link NaftahDuration} with days added
	 */
	@NaftahFn(
				name = "أضف_أيام_للمدة",
				aliases = {"أضف_أيام", "اضافة_ي", "اضافة"},
				description = "إضافة أيام إلى مدة",
				usage = "أضف_أيام_للمدة(مدة_, أيام)",
				parameterTypes = {NaftahDuration.class, Number.class},
				returnType = NaftahDuration.class
	)
	public static NaftahDuration plusDays(NaftahDuration d, Number days) {
		return d.plusDays(days.longValue());
	}

	/**
	 * Adds a number of hours to a given duration.
	 *
	 * @param d     the original duration
	 * @param hours the number of hours to add
	 * @return a {@link NaftahDuration} with hours added
	 */
	@NaftahFn(
				name = "أضف_ساعات_للمدة",
				aliases = {"أضف_ساعات", "اضافة_س", "اضافة"},
				description = "إضافة ساعات إلى مدة",
				usage = "أضف_ساعات_للمدة(مدة_, ساعات)",
				parameterTypes = {NaftahDuration.class, Number.class},
				returnType = NaftahDuration.class
	)
	public static NaftahDuration plusHours(NaftahDuration d, Number hours) {
		return d.plusHours(hours.longValue());
	}

	/**
	 * Adds a number of minutes to a given duration.
	 *
	 * @param d       the original duration
	 * @param minutes the number of minutes to add
	 * @return a {@link NaftahDuration} with minutes added
	 */
	@NaftahFn(
				name = "أضف_دقائق_للمدة",
				aliases = {"أضف_دقائق", "اضافة_د", "اضافة"},
				description = "إضافة دقائق إلى مدة",
				usage = "أضف_دقائق_للمدة(مدة_, دقائق)",
				parameterTypes = {NaftahDuration.class, Number.class},
				returnType = NaftahDuration.class
	)
	public static NaftahDuration plusMinutes(NaftahDuration d, Number minutes) {
		return d.plusMinutes(minutes.longValue());
	}

	/**
	 * Adds seconds to a duration.
	 *
	 * @param d       the duration
	 * @param seconds the number of seconds to add
	 * @return a new duration with the added seconds
	 */
	@NaftahFn(
				name = "أضف_ثواني_للمدة",
				aliases = {"أضف_ثواني", "اضافة_ث", "اضافة"},
				description = "إضافة ثواني إلى مدة",
				usage = "أضف_ثواني_للمدة(مدة_, ثواني)",
				parameterTypes = {NaftahDuration.class, Number.class},
				returnType = NaftahDuration.class
	)
	public static NaftahDuration plusSeconds(NaftahDuration d, Number seconds) {
		return d.plusSeconds(seconds.longValue());
	}

	/**
	 * Adds milliseconds to a duration.
	 *
	 * @param d      the duration
	 * @param millis the number of milliseconds to add
	 * @return a new duration with the added milliseconds
	 */
	@NaftahFn(
				name = "أضف_مللي_للمدة",
				aliases = {"أضف_مللي", "اضافة_م", "اضافة"},
				description = "إضافة مللي ثانية إلى مدة",
				usage = "أضف_مللي_للمدة(مدة_, مللي)",
				parameterTypes = {NaftahDuration.class, Number.class},
				returnType = NaftahDuration.class
	)
	public static NaftahDuration plusMillis(NaftahDuration d, Number millis) {
		return d.plusMillis(millis.longValue());
	}

	/**
	 * Adds nanoseconds to a duration.
	 *
	 * @param d     the duration
	 * @param nanos the number of nanoseconds to add
	 * @return a new duration with the added nanoseconds
	 */
	@NaftahFn(
				name = "أضف_نانو_للمدة",
				aliases = {"أضف_نانو", "اضافة_ن", "اضافة"},
				description = "إضافة نانو ثانية إلى مدة",
				usage = "أضف_نانو_للمدة(مدة_, نانو)",
				parameterTypes = {NaftahDuration.class, Number.class},
				returnType = NaftahDuration.class
	)
	public static NaftahDuration plusNanos(NaftahDuration d, Number nanos) {
		return d.plusNanos(nanos.longValue());
	}

	/**
	 * Subtracts days from a duration.
	 *
	 * @param d    the duration
	 * @param days the number of days to subtract
	 * @return a new duration with the days subtracted
	 */
	@NaftahFn(
				name = "اطرح_أيام_من_المدة",
				aliases = {"اطرح_أيام", "طرح_ي", "طرح"},
				description = "طرح أيام من مدة",
				usage = "اطرح_أيام_من_المدة(مدة_, أيام)",
				parameterTypes = {NaftahDuration.class, Number.class},
				returnType = NaftahDuration.class
	)
	public static NaftahDuration minusDays(NaftahDuration d, Number days) {
		return d.minusDays(days.longValue());
	}

	/**
	 * Subtracts hours from a duration.
	 *
	 * @param d     the duration
	 * @param hours the number of hours to subtract
	 * @return a new duration with the hours subtracted
	 */
	@NaftahFn(
				name = "اطرح_ساعات_من_المدة",
				aliases = {"طرح_ساعات", "طرح_س", "طرح"},
				description = "طرح ساعات من مدة",
				usage = "اطرح_ساعات_من_المدة(مدة_, ساعات)",
				parameterTypes = {NaftahDuration.class, Number.class},
				returnType = NaftahDuration.class
	)
	public static NaftahDuration minusHours(NaftahDuration d, Number hours) {
		return d.minusHours(hours.longValue());
	}

	/**
	 * Subtracts minutes from a duration.
	 *
	 * @param d       the duration
	 * @param minutes the number of minutes to subtract
	 * @return a new duration with the minutes subtracted
	 */
	@NaftahFn(
				name = "اطرح_دقائق_من_المدة",
				aliases = {"طرح_دقائق", "طرح_د", "طرح"},
				description = "طرح دقائق من مدة",
				usage = "اطرح_دقائق_من_المدة(مدة_, دقائق)",
				parameterTypes = {NaftahDuration.class, Number.class},
				returnType = NaftahDuration.class
	)
	public static NaftahDuration minusMinutes(NaftahDuration d, Number minutes) {
		return d.minusMinutes(minutes.longValue());
	}

	/**
	 * Subtracts seconds from a duration.
	 *
	 * @param d       the duration
	 * @param seconds the number of seconds to subtract
	 * @return a new duration with the seconds subtracted
	 */
	@NaftahFn(
				name = "اطرح_ثواني_من_المدة",
				aliases = {"طرح_ثواني", "طرح_ث", "طرح"},
				description = "طرح ثواني من مدة",
				usage = "اطرح_ثواني_من_المدة(مدة_, ثواني)",
				parameterTypes = {NaftahDuration.class, Number.class},
				returnType = NaftahDuration.class
	)
	public static NaftahDuration minusSeconds(NaftahDuration d, Number seconds) {
		return d.minusSeconds(seconds.longValue());
	}

	/**
	 * Subtracts milliseconds from a duration.
	 *
	 * @param d      the duration
	 * @param millis the number of milliseconds to subtract
	 * @return a new duration with the milliseconds subtracted
	 */
	@NaftahFn(
				name = "اطرح_مللي_من_المدة",
				aliases = {"طرح_مللي", "طرح_ملي", "طرح_م", "طرح"},
				description = "طرح مللي ثانية من مدة",
				usage = "اطرح_مللي_من_المدة(مدة_, مللي)",
				parameterTypes = {NaftahDuration.class, Number.class},
				returnType = NaftahDuration.class
	)
	public static NaftahDuration minusMillis(NaftahDuration d, Number millis) {
		return d.minusMillis(millis.longValue());
	}

	/**
	 * Subtracts nanoseconds from a duration.
	 *
	 * @param d     the duration
	 * @param nanos the number of nanoseconds to subtract
	 * @return a new duration with the nanoseconds subtracted
	 */
	@NaftahFn(
				name = "اطرح_نانو_من_المدة",
				aliases = {"طرح_نانوثواني", "طرح_نانو", "طرح_ن", "طرح"},
				description = "طرح نانو ثانية من مدة",
				usage = "اطرح_نانو_من_المدة(مدة_, نانو)",
				parameterTypes = {NaftahDuration.class, Number.class},
				returnType = NaftahDuration.class
	)
	public static NaftahDuration minusNanos(NaftahDuration d, Number nanos) {
		return d.minusNanos(nanos.longValue());
	}

	/**
	 * Adds years to a period with duration.
	 *
	 * @param pd    the period with duration
	 * @param years the number of years to add
	 * @return a new period with duration with the added years
	 */
	@NaftahFn(
				name = "أضف_سنوات_لفترة_مع_مدة",
				aliases = {"أضف_سنوات", "اضافة_س", "اضافة"},
				description = "إضافة عدد من السنوات إلى فترة تحتوي على مدة",
				usage = "أضف_سنوات_لفترة_مع_مدة(فترة_, سنوات)",
				parameterTypes = {NaftahPeriodWithDuration.class, Number.class},
				returnType = NaftahPeriodWithDuration.class
	)
	public static NaftahPeriodWithDuration plusYears(NaftahPeriodWithDuration pd, Number years) {
		return pd.plusYears(years.longValue());
	}

	/**
	 * Adds months to a period with duration.
	 *
	 * @param pd     the period with duration
	 * @param months the number of months to add
	 * @return a new period with duration with the added months
	 */
	@NaftahFn(
				name = "أضف_أشهر_لفترة_مع_مدة",
				aliases = {"أضف_أشهر", "اضافة_ش", "اضافة"},
				description = "إضافة عدد من الأشهر إلى فترة تحتوي على مدة",
				usage = "أضف_أشهر_لفترة_مع_مدة(فترة_, أشهر)",
				parameterTypes = {NaftahPeriodWithDuration.class, Number.class},
				returnType = NaftahPeriodWithDuration.class
	)
	public static NaftahPeriodWithDuration plusMonths(NaftahPeriodWithDuration pd, Number months) {
		return pd.plusMonths(months.longValue());
	}

	/**
	 * Adds days to a period with duration.
	 *
	 * @param pd   the period with duration
	 * @param days the number of days to add
	 * @return a new period with duration with the added days
	 */
	@NaftahFn(
				name = "أضف_أيام_لفترة_مع_مدة",
				aliases = {"أضف_أيام", "اضافة_ي", "اضافة"},
				description = "إضافة عدد من الأيام إلى فترة تحتوي على مدة",
				usage = "أضف_أيام_لفترة_مع_مدة(فترة_, أيام)",
				parameterTypes = {NaftahPeriodWithDuration.class, Number.class},
				returnType = NaftahPeriodWithDuration.class
	)
	public static NaftahPeriodWithDuration plusDays(NaftahPeriodWithDuration pd, Number days) {
		return pd.plusDays(days.longValue());
	}

	/**
	 * Adds hours to a period with duration.
	 *
	 * @param pd    the period with duration
	 * @param hours the number of hours to add
	 * @return a new period with duration with the added hours
	 */
	@NaftahFn(
				name = "أضف_ساعات_لفترة_مع_مدة",
				aliases = {"أضف_ساعات", "اضافة_س", "اضافة"},
				description = "إضافة عدد من الساعات إلى فترة تحتوي على مدة",
				usage = "أضف_ساعات_لفترة_مع_مدة(فترة_, ساعات)",
				parameterTypes = {NaftahPeriodWithDuration.class, Number.class},
				returnType = NaftahPeriodWithDuration.class
	)
	public static NaftahPeriodWithDuration plusHours(NaftahPeriodWithDuration pd, Number hours) {
		return pd.plusHours(hours.longValue());
	}

	/**
	 * Adds minutes to a period with duration.
	 *
	 * @param pd      the period with duration
	 * @param minutes the number of minutes to add
	 * @return a new period with duration with the added minutes
	 */
	@NaftahFn(
				name = "أضف_دقائق_لفترة_مع_مدة",
				aliases = {"أضف_دقائق", "اضافة_د", "اضافة"},
				description = "إضافة عدد من الدقائق إلى فترة تحتوي على مدة",
				usage = "أضف_دقائق_لفترة_مع_مدة(فترة_, دقائق)",
				parameterTypes = {NaftahPeriodWithDuration.class, Number.class},
				returnType = NaftahPeriodWithDuration.class
	)
	public static NaftahPeriodWithDuration plusMinutes(NaftahPeriodWithDuration pd, Number minutes) {
		return pd.plusMinutes(minutes.longValue());
	}

	/**
	 * Adds seconds to a period with duration.
	 *
	 * @param pd      the period with duration
	 * @param seconds the number of seconds to add
	 * @return a new period with duration with the added seconds
	 */
	@NaftahFn(
				name = "أضف_ثواني_لفترة_مع_مدة",
				aliases = {"أضف_ثواني", "اضافة_ث", "اضافة"},
				description = "إضافة عدد من الثواني إلى فترة تحتوي على مدة",
				usage = "أضف_ثواني_لفترة_مع_مدة(فترة_, ثواني)",
				parameterTypes = {NaftahPeriodWithDuration.class, Number.class},
				returnType = NaftahPeriodWithDuration.class
	)
	public static NaftahPeriodWithDuration plusSeconds(NaftahPeriodWithDuration pd, Number seconds) {
		return pd.plusSeconds(seconds.longValue());
	}

	/**
	 * Adds milliseconds to a period with duration.
	 *
	 * @param pd     the period with duration
	 * @param millis the number of milliseconds to add
	 * @return a new period with duration with the added milliseconds
	 */
	@NaftahFn(
				name = "أضف_مللي_لفترة_مع_مدة",
				aliases = {"أضف_مللي", "اضافة_م", "اضافة"},
				description = "إضافة عدد من المللي ثانية إلى فترة تحتوي على مدة",
				usage = "أضف_مللي_لفترة_مع_مدة(فترة_, مللي)",
				parameterTypes = {NaftahPeriodWithDuration.class, Number.class},
				returnType = NaftahPeriodWithDuration.class
	)
	public static NaftahPeriodWithDuration plusMillis(NaftahPeriodWithDuration pd, Number millis) {
		return pd.plusMillis(millis.longValue());
	}

	/**
	 * Adds nanoseconds to a period with duration.
	 *
	 * @param pd    the period with duration
	 * @param nanos the number of nanoseconds to add
	 * @return a new period with duration with the added nanoseconds
	 */
	@NaftahFn(
				name = "أضف_نانو_لفترة_مع_مدة",
				aliases = {"أضف_نانو", "اضافة_ن", "اضافة"},
				description = "إضافة عدد من النانو ثانية إلى فترة تحتوي على مدة",
				usage = "أضف_نانو_لفترة_مع_مدة(فترة_, نانو)",
				parameterTypes = {NaftahPeriodWithDuration.class, Number.class},
				returnType = NaftahPeriodWithDuration.class
	)
	public static NaftahPeriodWithDuration plusNanos(NaftahPeriodWithDuration pd, Number nanos) {
		return pd.plusNanos(nanos.longValue());
	}

	/**
	 * Subtracts years from a period with duration.
	 *
	 * @param pd    the period with duration
	 * @param years the number of years to subtract
	 * @return a new period with duration with the years subtracted
	 */
	@NaftahFn(
				name = "اطرح_سنوات_من_الفترة_مع_مدة",
				aliases = {"طرح_سنوات", "طرح_س", "طرح"},
				description = "طرح عدد من السنوات من فترة تحتوي على مدة",
				usage = "اطرح_سنوات_من_الفترة_مع_مدة(فترة_, سنوات)",
				parameterTypes = {NaftahPeriodWithDuration.class, Number.class},
				returnType = NaftahPeriodWithDuration.class
	)
	public static NaftahPeriodWithDuration minusYears(NaftahPeriodWithDuration pd, Number years) {
		return pd.minusYears(years.longValue());
	}

	/**
	 * Subtracts months from a period with duration.
	 *
	 * @param pd     the period with duration
	 * @param months the number of months to subtract
	 * @return a new period with duration with the months subtracted
	 */
	@NaftahFn(
				name = "اطرح_أشهر_من_الفترة_مع_مدة",
				aliases = {"طرح_أشهر", "طرح_شهور", "طرح_ش", "طرح"},
				description = "طرح عدد من الأشهر من فترة تحتوي على مدة",
				usage = "اطرح_أشهر_من_الفترة_مع_مدة(فترة_, أشهر)",
				parameterTypes = {NaftahPeriodWithDuration.class, Number.class},
				returnType = NaftahPeriodWithDuration.class
	)
	public static NaftahPeriodWithDuration minusMonths(NaftahPeriodWithDuration pd, Number months) {
		return pd.minusMonths(months.longValue());
	}

	/**
	 * Subtracts days from a period with duration.
	 *
	 * @param pd   the period with duration
	 * @param days the number of days to subtract
	 * @return a new period with duration with the days subtracted
	 */
	@NaftahFn(
				name = "اطرح_أيام_من_الفترة_مع_مدة",
				aliases = {"اطرح_أيام", "طرح_ي", "طرح"},
				description = "طرح عدد من الأيام من فترة تحتوي على مدة",
				usage = "اطرح_أيام_من_الفترة_مع_مدة(فترة_, أيام)",
				parameterTypes = {NaftahPeriodWithDuration.class, Number.class},
				returnType = NaftahPeriodWithDuration.class
	)
	public static NaftahPeriodWithDuration minusDays(NaftahPeriodWithDuration pd, Number days) {
		return pd.minusDays(days.longValue());
	}

	/**
	 * Subtracts hours from a period with duration.
	 *
	 * @param pd    the period with duration
	 * @param hours the number of hours to subtract
	 * @return a new period with duration with the hours subtracted
	 */
	@NaftahFn(
				name = "اطرح_ساعات_من_الفترة_مع_مدة",
				aliases = {"طرح_ساعات", "طرح_س", "طرح"},
				description = "طرح عدد من الساعات من فترة تحتوي على مدة",
				usage = "اطرح_ساعات_من_الفترة_مع_مدة(فترة_, ساعات)",
				parameterTypes = {NaftahPeriodWithDuration.class, Number.class},
				returnType = NaftahPeriodWithDuration.class
	)
	public static NaftahPeriodWithDuration minusHours(NaftahPeriodWithDuration pd, Number hours) {
		return pd.minusHours(hours.longValue());
	}

	/**
	 * Subtracts minutes from a period with duration.
	 *
	 * @param pd      the period with duration
	 * @param minutes the number of minutes to subtract
	 * @return a new period with duration with the minutes subtracted
	 */
	@NaftahFn(
				name = "اطرح_دقائق_من_الفترة_مع_مدة",
				aliases = {"طرح_دقائق", "طرح_د", "طرح"},
				description = "طرح عدد من الدقائق من فترة تحتوي على مدة",
				usage = "اطرح_دقائق_من_الفترة_مع_مدة(فترة_, دقائق)",
				parameterTypes = {NaftahPeriodWithDuration.class, Number.class},
				returnType = NaftahPeriodWithDuration.class
	)
	public static NaftahPeriodWithDuration minusMinutes(NaftahPeriodWithDuration pd, Number minutes) {
		return pd.minusMinutes(minutes.longValue());
	}

	/**
	 * Subtracts seconds from a period with duration.
	 *
	 * @param pd      the period with duration
	 * @param seconds the number of seconds to subtract
	 * @return a new period with duration with the seconds subtracted
	 */
	@NaftahFn(
				name = "اطرح_ثواني_من_الفترة_مع_مدة",
				aliases = {"طرح_ثواني", "طرح_ث", "طرح"},
				description = "طرح عدد من الثواني من فترة تحتوي على مدة",
				usage = "اطرح_ثواني_من_الفترة_مع_مدة(فترة_, ثواني)",
				parameterTypes = {NaftahPeriodWithDuration.class, Number.class},
				returnType = NaftahPeriodWithDuration.class
	)
	public static NaftahPeriodWithDuration minusSeconds(NaftahPeriodWithDuration pd, Number seconds) {
		return pd.minusSeconds(seconds.longValue());
	}

	/**
	 * Subtracts milliseconds from a period with duration.
	 *
	 * @param pd     the period with duration
	 * @param millis the number of milliseconds to subtract
	 * @return a new period with duration with the milliseconds subtracted
	 */
	@NaftahFn(
				name = "اطرح_مللي_من_الفترة_مع_مدة",
				aliases = {"طرح_مللي", "طرح_ملي", "طرح_م", "طرح"},
				description = "طرح عدد من المللي ثانية من فترة تحتوي على مدة",
				usage = "اطرح_مللي_من_الفترة_مع_مدة(فترة_, مللي)",
				parameterTypes = {NaftahPeriodWithDuration.class, Number.class},
				returnType = NaftahPeriodWithDuration.class
	)
	public static NaftahPeriodWithDuration minusMillis(NaftahPeriodWithDuration pd, Number millis) {
		return pd.minusMillis(millis.longValue());
	}

	/**
	 * Subtracts nanoseconds from a period with duration.
	 *
	 * @param pd    the period with duration
	 * @param nanos the number of nanoseconds to subtract
	 * @return a new period with duration with the nanoseconds subtracted
	 */
	@NaftahFn(
				name = "اطرح_نانو_من_الفترة_مع_مدة",
				aliases = {"طرح_نانوثواني", "طرح_نانو", "طرح_ن", "طرح"},
				description = "طرح عدد من النانو ثانية من فترة تحتوي على مدة",
				usage = "اطرح_نانو_من_الفترة_مع_مدة(فترة_, نانو)",
				parameterTypes = {NaftahPeriodWithDuration.class, Number.class},
				returnType = NaftahPeriodWithDuration.class
	)
	public static NaftahPeriodWithDuration minusNanos(NaftahPeriodWithDuration pd, Number nanos) {
		return pd.minusNanos(nanos.longValue());
	}

	/**
	 * Gets the number of years in a period.
	 *
	 * @param p the period
	 * @return the number of years
	 */
	@NaftahFn(
				name = "احصل_على_سنوات_الفترة",
				aliases = {"احصل_على_السنوات", "السنوات", "سنوات"},
				description = "الحصول على عدد السنوات من فترة",
				usage = "احصل_على_سنوات_الفترة(فترة_)",
				parameterTypes = {NaftahPeriod.class},
				returnType = int.class
	)
	public static int getYears(NaftahPeriod p) {
		return p.getYears();
	}

	/**
	 * Gets the number of months in a period.
	 *
	 * @param p the period
	 * @return the number of months
	 */
	@NaftahFn(
				name = "احصل_على_أشهر_الفترة",
				aliases = { "احصل_على_الأشهر",
							"احصل_على_أشهر",
							"الأشهر",
							"أشهر",
							"احصل_على_الشهور",
							"احصل_على_شهور",
							"الشهور",
							"شهور"},
				description = "الحصول على عدد الأشهر من فترة",
				usage = "احصل_على_أشهر_الفترة(فترة_)",
				parameterTypes = {NaftahPeriod.class},
				returnType = int.class
	)
	public static int getMonths(NaftahPeriod p) {
		return p.getMonths();
	}

	/**
	 * Gets the number of days in a period.
	 *
	 * @param p the period
	 * @return the number of days
	 */
	@NaftahFn(
				name = "احصل_على_أيام_الفترة",
				aliases = {"احصل_على_الأيام", "احصل_على_أيام", "الأيام", "أيام"},
				description = "الحصول على عدد الأيام من فترة",
				usage = "احصل_على_أيام_الفترة(فترة_)",
				parameterTypes = {NaftahPeriod.class},
				returnType = int.class
	)
	public static int getDays(NaftahPeriod p) {
		return p.getDays();
	}

	/**
	 * Gets the number of hours in a duration.
	 *
	 * @param d the duration
	 * @return the number of hours
	 */
	@NaftahFn(
				name = "احصل_على_ساعات_المدة",
				aliases = {"احصل_على_الساعات", "احصل_على_ساعات", "الساعات", "ساعات"},
				description = "الحصول على عدد الساعات من مدة",
				usage = "احصل_على_ساعات_المدة(مدة_)",
				parameterTypes = {NaftahDuration.class},
				returnType = long.class
	)
	public static long getHours(NaftahDuration d) {
		return d.getHours();
	}

	/**
	 * Gets the number of minutes in a duration.
	 *
	 * @param d the duration
	 * @return the number of minutes
	 */
	@NaftahFn(
				name = "احصل_على_دقائق_المدة",
				aliases = {"احصل_على_الدقائق", "احصل_على_دقائق", "الدقائق", "دقائق"},
				description = "الحصول على عدد الدقائق من مدة",
				usage = "احصل_على_دقائق_المدة(مدة_)",
				parameterTypes = {NaftahDuration.class},
				returnType = long.class
	)
	public static long getMinutes(NaftahDuration d) {
		return d.getMinutes();
	}

	/**
	 * Gets the number of seconds in a duration.
	 *
	 * @param d the duration
	 * @return the number of seconds
	 */
	@NaftahFn(
				name = "احصل_على_ثواني_المدة",
				aliases = {"احصل_على_ثواني", "ثواني"},
				description = "الحصول على عدد الثواني من مدة",
				usage = "احصل_على_ثواني_المدة(مدة_)",
				parameterTypes = {NaftahDuration.class},
				returnType = long.class
	)
	public static long getSeconds(NaftahDuration d) {
		return d.getSeconds();
	}

	/**
	 * Gets the number of milliseconds in a duration.
	 *
	 * @param d the duration
	 * @return the number of milliseconds
	 */
	@NaftahFn(
				name = "احصل_على_مللي_المدة",
				aliases = {"احصل_على_الملي", "ملي", "احصل_على_المللي", "مللي"},
				description = "الحصول على عدد المللي ثانية من مدة",
				usage = "احصل_على_مللي_المدة(مدة_)",
				parameterTypes = {NaftahDuration.class},
				returnType = long.class
	)
	public static long getMillis(NaftahDuration d) {
		return d.getMillis();
	}

	/**
	 * Gets the number of nanoseconds in a duration.
	 *
	 * @param d the duration
	 * @return the number of nanoseconds
	 */
	@NaftahFn(
				name = "احصل_على_نانو_المدة",
				aliases = {"احصل_على_نانو", "نانو"},
				description = "الحصول على عدد النانو ثانية من مدة",
				usage = "احصل_على_نانو_المدة(مدة_)",
				parameterTypes = {NaftahDuration.class},
				returnType = int.class
	)
	public static int getNano(NaftahDuration d) {
		return d.getNano();
	}

	/**
	 * Gets the number of years from a period with duration.
	 *
	 * @param p the period with duration
	 * @return the number of years
	 */
	@NaftahFn(
				name = "احصل_على_سنوات_الفترة_مع_مدة",
				aliases = {"احصل_على_السنوات", "السنوات", "سنوات"},
				description = "الحصول على عدد السنوات من فترة تحتوي على مدة",
				usage = "احصل_على_سنوات_الفترة_مع_مدة(فترة_)",
				parameterTypes = {NaftahPeriodWithDuration.class},
				returnType = int.class
	)
	public static int getYears(NaftahPeriodWithDuration p) {
		return p.getYears();
	}

	/**
	 * Gets the number of months from a period with duration.
	 *
	 * @param p the period with duration
	 * @return the number of months
	 */
	@NaftahFn(
				name = "احصل_على_أشهر_الفترة_مع_مدة",
				aliases = { "احصل_على_الأشهر",
							"احصل_على_أشهر",
							"الأشهر",
							"أشهر",
							"احصل_على_الشهور",
							"احصل_على_شهور",
							"الشهور",
							"شهور"},
				description = "الحصول على عدد الأشهر من فترة تحتوي على مدة",
				usage = "احصل_على_أشهر_الفترة_مع_مدة(فترة_)",
				parameterTypes = {NaftahPeriodWithDuration.class},
				returnType = int.class
	)
	public static int getMonths(NaftahPeriodWithDuration p) {
		return p.getMonths();
	}

	/**
	 * Gets the number of days from a period with duration.
	 *
	 * @param p the period with duration
	 * @return the number of days
	 */
	@NaftahFn(
				name = "احصل_على_أيام_الفترة_مع_مدة",
				aliases = {"احصل_على_الأيام", "احصل_على_أيام", "الأيام", "أيام"},
				description = "الحصول على عدد الأيام من فترة تحتوي على مدة",
				usage = "احصل_على_أيام_الفترة_مع_مدة(فترة_)",
				parameterTypes = {NaftahPeriodWithDuration.class},
				returnType = int.class
	)
	public static int getDays(NaftahPeriodWithDuration p) {
		return p.getDays();
	}

	/**
	 * Gets the number of hours from a period with duration.
	 *
	 * @param d the period with duration
	 * @return the number of hours
	 */
	@NaftahFn(
				name = "احصل_على_ساعات_الفترة_مع_مدة",
				aliases = {"احصل_على_الساعات", "احصل_على_ساعات", "الساعات", "ساعات"},
				description = "الحصول على عدد الساعات من فترة تحتوي على مدة",
				usage = "احصل_على_ساعات_الفترة_مع_مدة(فترة_)",
				parameterTypes = {NaftahPeriodWithDuration.class},
				returnType = long.class
	)
	public static long getHours(NaftahPeriodWithDuration d) {
		return d.getHours();
	}

	/**
	 * Gets the number of minutes from a period with duration.
	 *
	 * @param d the period with duration
	 * @return the number of minutes
	 */
	@NaftahFn(
				name = "احصل_على_دقائق_الفترة_مع_مدة",
				aliases = {"احصل_على_الدقائق", "احصل_على_دقائق", "الدقائق", "دقائق"},
				description = "الحصول على عدد الدقائق من فترة تحتوي على مدة",
				usage = "احصل_على_دقائق_الفترة_مع_مدة(فترة_)",
				parameterTypes = {NaftahPeriodWithDuration.class},
				returnType = long.class
	)
	public static long getMinutes(NaftahPeriodWithDuration d) {
		return d.getMinutes();
	}

	/**
	 * Gets the number of seconds from a period with duration.
	 *
	 * @param d the period with duration
	 * @return the number of seconds
	 */
	@NaftahFn(
				name = "احصل_على_ثواني_الفترة_مع_مدة",
				aliases = {"احصل_على_ثواني", "ثواني"},
				description = "الحصول على عدد الثواني من فترة تحتوي على مدة",
				usage = "احصل_على_ثواني_الفترة_مع_مدة(فترة_)",
				parameterTypes = {NaftahPeriodWithDuration.class},
				returnType = long.class
	)
	public static long getSeconds(NaftahPeriodWithDuration d) {
		return d.getSeconds();
	}

	/**
	 * Gets the number of milliseconds from a period with duration.
	 *
	 * @param d the period with duration
	 * @return the number of milliseconds
	 */
	@NaftahFn(
				name = "احصل_على_مللي_الفترة_مع_مدة",
				aliases = {"احصل_على_الملي", "ملي", "احصل_على_المللي", "مللي"},
				description = "الحصول على عدد المللي ثانية من فترة تحتوي على مدة",
				usage = "احصل_على_مللي_الفترة_مع_مدة(فترة_)",
				parameterTypes = {NaftahPeriodWithDuration.class},
				returnType = long.class
	)
	public static long getMillis(NaftahPeriodWithDuration d) {
		return d.getMillis();
	}

	/**
	 * Gets the number of nanoseconds from a period with duration.
	 *
	 * @param d the period with duration
	 * @return the number of nanoseconds
	 */
	@NaftahFn(
				name = "احصل_على_نانو_الفترة_مع_مدة",
				aliases = {"احصل_على_نانو", "نانو"},
				description = "الحصول على عدد النانو ثانية من فترة تحتوي على مدة",
				usage = "احصل_على_نانو_الفترة_مع_مدة(فترة_)",
				parameterTypes = {NaftahPeriodWithDuration.class},
				returnType = int.class
	)
	public static int getNano(NaftahPeriodWithDuration d) {
		return d.getNano();
	}

	/**
	 * Creates a temporal amount representing the difference between two temporal points.
	 *
	 * @param left  the first temporal point
	 * @param right the second temporal point
	 * @return the temporal amount between the two points
	 */
	@NaftahFn(
				name = "أنشئ_مدة_بين_نقطتين",
				aliases = {"إنشاء_مدة", "أنشئ_مدة"},
				description = "إنشاء مدة زمنية بين نقطتين زمنيتين",
				usage = "أنشئ_مدة_بين_نقطتين(نقطة_زمنية_أولى, نقطة_زمنية_ثانية)",
				parameterTypes = {NaftahTemporalPoint.class, NaftahTemporalPoint.class},
				returnType = NaftahTemporalAmount.class
	)
	public static NaftahTemporalAmount createTemporalAmountBetween( NaftahTemporalPoint left,
																	NaftahTemporalPoint right) {
		return NaftahDateParserHelper.getArabicTemporalAmountBetween(left, right);
	}
}
