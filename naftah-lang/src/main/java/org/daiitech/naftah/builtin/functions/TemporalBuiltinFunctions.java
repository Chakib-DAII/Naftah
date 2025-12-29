package org.daiitech.naftah.builtin.functions;

import java.time.temporal.ChronoField;

import org.daiitech.naftah.builtin.NaftahFn;
import org.daiitech.naftah.builtin.NaftahFnProvider;
import org.daiitech.naftah.builtin.time.ArabicDate;
import org.daiitech.naftah.builtin.time.ArabicDateTime;
import org.daiitech.naftah.builtin.time.ArabicDuration;
import org.daiitech.naftah.builtin.time.ArabicPeriod;
import org.daiitech.naftah.builtin.time.ArabicPeriodWithDuration;
import org.daiitech.naftah.builtin.time.ArabicTemporalAmount;
import org.daiitech.naftah.builtin.time.ArabicTemporalPoint;
import org.daiitech.naftah.builtin.time.ArabicTime;
import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.parser.time.ArabicDateParserHelper;
import org.daiitech.naftah.utils.time.ChronologyUtils;

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
										"التاريخ_والوقت_الحالي_بتقويم_وإزاحة"
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
	 * @return the current {@link ArabicTime}
	 */
	@NaftahFn(
				name = "الوقت_الحالي",
				description = "الحصول على الوقت الحالي",
				usage = "الوقت_الحالي()",
				returnType = ArabicTime.class
	)
	public static ArabicTime currentTime() {
		return ArabicTime.now();
	}

	/**
	 * Obtains the current time using the specified time zone.
	 *
	 * @param zone the zone ID (e.g. Asia/Dubai)
	 * @return the current {@link ArabicTime}
	 */
	@NaftahFn(
				name = "الوقت_الحالي_بتوقيت",
				description = "الحصول على الوقت الحالي بتوقيت محدد",
				usage = "الوقت_الحالي_بتوقيت(منطقة)",
				parameterTypes = {String.class},
				returnType = ArabicTime.class
	)
	public static ArabicTime currentZonedTime(String zone) {
		return ArabicTime.now(ArabicTime.ZoneOrOffset.ofZone(zone));
	}

	/**
	 * Obtains the current time using a fixed offset.
	 *
	 * @param offset the offset (e.g. +04:00)
	 * @return the current {@link ArabicTime}
	 */
	@NaftahFn(
				name = "الوقت_الحالي_بإزاحة",
				description = "الحصول على الوقت الحالي بإزاحة زمنية",
				usage = "الوقت_الحالي_بإزاحة(إزاحة)",
				parameterTypes = {String.class},
				returnType = ArabicTime.class
	)
	public static ArabicTime currentOffsetTime(String offset) {
		return ArabicTime.now(ArabicTime.ZoneOrOffset.ofOffset(offset));
	}

	/**
	 * Obtains the current date using the default chronology.
	 *
	 * @return the current {@link ArabicDate}
	 */
	@NaftahFn(
				name = "التاريخ_الحالي",
				description = "الحصول على التاريخ الحالي",
				usage = "التاريخ_الحالي()",
				returnType = ArabicDate.class
	)
	public static ArabicDate currentDate() {
		return ArabicDate.now();
	}

	/**
	 * Obtains the current date using the specified chronology.
	 *
	 * @param chronology the chronology name
	 * @return the current {@link ArabicDate}
	 */
	@NaftahFn(
				name = "التاريخ_الحالي_بتقويم",
				description = "الحصول على التاريخ الحالي بتقويم محدد",
				usage = "التاريخ_الحالي_بتقويم(تقويم)",
				parameterTypes = {String.class},
				returnType = ArabicDate.class
	)
	public static ArabicDate currentChronologyDate(String chronology) {
		return ArabicDate.now(ChronologyUtils.getChronologyByName(chronology));
	}

	/**
	 * Obtains the current date using the specified time zone.
	 *
	 * @param zone the zone ID (e.g. Asia/Dubai)
	 * @return the current {@link ArabicDate}
	 */
	@NaftahFn(
				name = "التاريخ_الحالي_بتوقيت",
				description = "الحصول على التاريخ الحالي بتوقيت محدد",
				usage = "التاريخ_الحالي_بتوقيت(منطقة)",
				parameterTypes = {String.class},
				returnType = ArabicDate.class
	)
	public static ArabicDate currentZonedDate(String zone) {
		return ArabicDate.now(ArabicTime.ZoneOrOffset.ofZone(zone));
	}

	/**
	 * Obtains the current date using a fixed offset.
	 *
	 * @param offset the offset (e.g. +04:00)
	 * @return the current {@link ArabicDate}
	 */
	@NaftahFn(
				name = "التاريخ_الحالي_بإزاحة",
				description = "الحصول على التاريخ الحالي بإزاحة زمنية",
				usage = "التاريخ_الحالي_بإزاحة(إزاحة)",
				parameterTypes = {String.class},
				returnType = ArabicDate.class
	)
	public static ArabicDate currentOffsetDate(String offset) {
		return ArabicDate.now(ArabicTime.ZoneOrOffset.ofOffset(offset));
	}

	/**
	 * Obtains the current date using the specified chronology and time zone.
	 *
	 * @param chronology the chronology name
	 * @param zone       the zone ID
	 * @return the current {@link ArabicDate}
	 */
	@NaftahFn(
				name = "التاريخ_الحالي_بتقويم_وتوقيت",
				description = "الحصول على التاريخ الحالي بتقويم وتوقيت محددين",
				usage = "التاريخ_الحالي_بتقويم_وتوقيت(تقويم, منطقة)",
				parameterTypes = {String.class, String.class},
				returnType = ArabicDate.class
	)
	public static ArabicDate currentZonedChronologyDate(String chronology, String zone) {
		return ArabicDate.now(ChronologyUtils.getChronologyByName(chronology), ArabicTime.ZoneOrOffset.ofZone(zone));
	}

	/**
	 * Obtains the current date using the specified chronology and offset.
	 *
	 * @param chronology the chronology name
	 * @param offset     the offset (e.g. +04:00)
	 * @return the current {@link ArabicDate}
	 */
	@NaftahFn(
				name = "التاريخ_الحالي_بتقويم_وإزاحة",
				description = "الحصول على التاريخ الحالي بتقويم وإزاحة زمنية",
				usage = "التاريخ_الحالي_بتقويم_وإزاحة(تقويم, إزاحة)",
				parameterTypes = {String.class, String.class},
				returnType = ArabicDate.class
	)
	public static ArabicDate currentOffsetChronologyDate(String chronology, String offset) {
		return ArabicDate
				.now(   ChronologyUtils.getChronologyByName(chronology),
						ArabicTime.ZoneOrOffset.ofOffset(offset));
	}

	/**
	 * Obtains the current date-time using the system default settings.
	 *
	 * @return the current {@link ArabicDateTime}
	 */
	@NaftahFn(
				name = "التاريخ_والوقت_الحالي",
				description = "الحصول على التاريخ والوقت الحاليين",
				usage = "التاريخ_والوقت_الحالي()",
				returnType = ArabicDateTime.class
	)
	public static ArabicDateTime currentDateTime() {
		return ArabicDateTime.now();
	}


	/**
	 * Obtains the current date-time using a specific chronology.
	 *
	 * @param chronology the chronology name
	 * @return the current {@link ArabicDateTime}
	 */
	@NaftahFn(
				name = "التاريخ_والوقت_الحالي_بتقويم",
				description = "الحصول على التاريخ والوقت الحاليين بتقويم محدد",
				usage = "التاريخ_والوقت_الحالي_بتقويم(تقويم)",
				parameterTypes = {String.class},
				returnType = ArabicDateTime.class
	)
	public static ArabicDateTime currentChronologyDateTime(String chronology) {
		return ArabicDateTime.now(ChronologyUtils.getChronologyByName(chronology));
	}

	/**
	 * Obtains the current date-time using the specified time zone.
	 *
	 * @param zone the zone ID
	 * @return the current {@link ArabicDateTime}
	 */
	@NaftahFn(
				name = "التاريخ_والوقت_الحالي_بتوقيت",
				description = "الحصول على التاريخ والوقت الحاليين بتوقيت محدد",
				usage = "التاريخ_والوقت_الحالي_بتوقيت(منطقة)",
				parameterTypes = {String.class},
				returnType = ArabicDateTime.class
	)
	public static ArabicDateTime currentZonedDateTime(String zone) {
		return ArabicDateTime.now(ArabicTime.ZoneOrOffset.ofZone(zone));
	}

	/**
	 * Obtains the current date-time using a fixed offset.
	 *
	 * @param offset the offset (e.g. +04:00)
	 * @return the current {@link ArabicDateTime}
	 */
	@NaftahFn(
				name = "التاريخ_والوقت_الحالي_بإزاحة",
				description = "الحصول على التاريخ والوقت الحاليين بإزاحة زمنية",
				usage = "التاريخ_والوقت_الحالي_بإزاحة(إزاحة)",
				parameterTypes = {String.class},
				returnType = ArabicDateTime.class
	)
	public static ArabicDateTime currentOffsetDateTime(String offset) {
		return ArabicDateTime.now(ArabicTime.ZoneOrOffset.ofOffset(offset));
	}

	/**
	 * Obtains the current date-time using the specified chronology and time zone.
	 *
	 * @param chronology the chronology name
	 * @param zone       the zone ID
	 * @return the current {@link ArabicDateTime}
	 */
	@NaftahFn(
				name = "التاريخ_والوقت_الحالي_بتقويم_وتوقيت",
				description = "الحصول على التاريخ والوقت الحاليين بتقويم وتوقيت محددين",
				usage = "التاريخ_والوقت_الحالي_بتقويم_وتوقيت(تقويم, منطقة)",
				parameterTypes = {String.class, String.class},
				returnType = ArabicDateTime.class
	)
	public static ArabicDateTime currentZonedChronologyDateTime(String chronology, String zone) {
		return ArabicDateTime
				.now(   ChronologyUtils.getChronologyByName(chronology),
						ArabicTime.ZoneOrOffset.ofZone(zone));
	}

	/**
	 * Obtains the current date-time using the specified chronology and offset.
	 *
	 * @param chronology the chronology name
	 * @param offset     the offset (e.g. +04:00)
	 * @return the current {@link ArabicDateTime}
	 */
	@NaftahFn(
				name = "التاريخ_والوقت_الحالي_بتقويم_وإزاحة",
				description = "الحصول على التاريخ والوقت الحاليين بتقويم وإزاحة زمنية",
				usage = "التاريخ_والوقت_الحالي_بتقويم_وإزاحة(تقويم, إزاحة)",
				parameterTypes = {String.class, String.class},
				returnType = ArabicDateTime.class
	)
	public static ArabicDateTime currentOffsetChronologyDateTime(String chronology, String offset) {
		return ArabicDateTime
				.now(   ChronologyUtils.getChronologyByName(chronology),
						ArabicTime.ZoneOrOffset.ofOffset(offset));
	}

	/**
	 * Creates a zero duration.
	 *
	 * @return a {@link ArabicDuration} representing zero duration
	 */
	@NaftahFn(
				name = "أنشئ_مدة_صفرية",
				description = "إنشاء مدة صفرية",
				usage = "أنشئ_مدة_صفرية()",
				returnType = ArabicDuration.class
	)
	public static ArabicDuration createZeroDuration() {
		return ArabicDuration.ofZero();
	}

	/**
	 * Creates a duration from the specified number of days.
	 *
	 * @param days the number of days
	 * @return a {@link ArabicDuration} representing the specified days
	 */
	@NaftahFn(
				name = "أنشئ_مدة_من_أيام",
				description = "إنشاء مدة من عدد أيام",
				usage = "أنشئ_مدة_من_أيام(أيام)",
				parameterTypes = {Number.class},
				returnType = ArabicDuration.class
	)
	public static ArabicDuration createDurationFromDays(Number days) {
		return ArabicDuration.ofDays(days.longValue());
	}

	/**
	 * Creates a duration from the specified number of hours.
	 *
	 * @param hours the number of hours
	 * @return a {@link ArabicDuration} representing the specified hours
	 */
	@NaftahFn(
				name = "أنشئ_مدة_من_ساعات",
				description = "إنشاء مدة من عدد ساعات",
				usage = "أنشئ_مدة_من_ساعات(ساعات)",
				parameterTypes = {Number.class},
				returnType = ArabicDuration.class
	)
	public static ArabicDuration createDurationFromHours(Number hours) {
		return ArabicDuration.ofHours(hours.longValue());
	}

	/**
	 * Creates a duration from the specified number of minutes.
	 *
	 * @param minutes the number of minutes
	 * @return a {@link ArabicDuration} representing the specified minutes
	 */
	@NaftahFn(
				name = "أنشئ_مدة_من_دقائق",
				description = "إنشاء مدة من عدد دقائق",
				usage = "أنشئ_مدة_من_دقائق(دقائق)",
				parameterTypes = {Number.class},
				returnType = ArabicDuration.class
	)
	public static ArabicDuration createDurationFromMinutes(Number minutes) {
		return ArabicDuration.ofMinutes(minutes.longValue());
	}

	/**
	 * Creates a duration from the specified number of seconds.
	 *
	 * @param seconds the number of seconds
	 * @return a {@link ArabicDuration} representing the specified seconds
	 */
	@NaftahFn(
				name = "أنشئ_مدة_من_ثواني",
				description = "إنشاء مدة من عدد ثواني",
				usage = "أنشئ_مدة_من_ثواني(ثواني)",
				parameterTypes = {Number.class},
				returnType = ArabicDuration.class
	)
	public static ArabicDuration createDurationFromSeconds(Number seconds) {
		return ArabicDuration.ofSeconds(seconds.longValue());
	}

	/**
	 * Creates a duration from seconds with a nanosecond adjustment.
	 *
	 * @param seconds        the number of seconds
	 * @param nanoAdjustment the nanosecond adjustment
	 * @return a {@link ArabicDuration} representing the specified seconds and nano adjustment
	 */
	@NaftahFn(
				name = "أنشئ_مدة_من_ثواني_مع_نانو",
				description = "إنشاء مدة من ثواني مع تعديل النانوثانية",
				usage = "أنشئ_مدة_من_ثواني_مع_نانو(ثواني, نانو)",
				parameterTypes = {Number.class, Number.class},
				returnType = ArabicDuration.class
	)
	public static ArabicDuration createDurationFromSecondsAndNano(Number seconds, Number nanoAdjustment) {
		return ArabicDuration.ofSeconds(seconds.longValue(), nanoAdjustment.longValue());
	}

	/**
	 * Creates a duration from the specified number of milliseconds.
	 *
	 * @param millis the number of milliseconds
	 * @return a {@link ArabicDuration} representing the specified milliseconds
	 */
	@NaftahFn(
				name = "أنشئ_مدة_من_مللي",
				description = "إنشاء مدة من عدد مللي ثانية",
				usage = "أنشئ_مدة_من_مللي(مللي)",
				parameterTypes = {Number.class},
				returnType = ArabicDuration.class
	)
	public static ArabicDuration createDurationFromMillis(Number millis) {
		return ArabicDuration.ofMillis(millis.longValue());
	}

	/**
	 * Creates a duration from the specified number of nanoseconds.
	 *
	 * @param nanos the number of nanoseconds
	 * @return a {@link ArabicDuration} representing the specified nanoseconds
	 */
	@NaftahFn(
				name = "أنشئ_مدة_من_نانو",
				description = "إنشاء مدة من عدد نانو ثانية",
				usage = "أنشئ_مدة_من_نانو(نانو)",
				parameterTypes = {Number.class},
				returnType = ArabicDuration.class
	)
	public static ArabicDuration createDurationFromNanos(Number nanos) {
		return ArabicDuration.ofNanos(nanos.longValue());
	}

	/**
	 * Creates a zero period.
	 *
	 * @return a {@link ArabicPeriod} representing zero period
	 */
	@NaftahFn(
				name = "أنشئ_فترة_صفرية",
				description = "إنشاء فترة صفرية",
				usage = "أنشئ_فترة_صفرية()",
				returnType = ArabicPeriod.class
	)
	public static ArabicPeriod createZeroPeriod() {
		return ArabicPeriod.ofZero();
	}

	/**
	 * Creates a period from the specified number of years.
	 *
	 * @param years the number of years
	 * @return a {@link ArabicPeriod} representing the specified years
	 */
	@NaftahFn(
				name = "أنشئ_فترة_من_سنوات",
				description = "إنشاء فترة من عدد سنوات",
				usage = "أنشئ_فترة_من_سنوات(سنوات)",
				parameterTypes = {Number.class},
				returnType = ArabicPeriod.class
	)
	public static ArabicPeriod createPeriodFromYears(Number years) {
		return ArabicPeriod.ofYears(years.intValue());
	}

	/**
	 * Creates a period from the specified number of months.
	 *
	 * @param months the number of months
	 * @return a {@link ArabicPeriod} representing the specified months
	 */
	@NaftahFn(
				name = "أنشئ_فترة_من_شهور",
				description = "إنشاء فترة من عدد أشهر",
				usage = "أنشئ_فترة_من_شهور(شهور)",
				parameterTypes = {Number.class},
				returnType = ArabicPeriod.class
	)
	public static ArabicPeriod createPeriodFromMonths(Number months) {
		return ArabicPeriod.ofMonths(months.intValue());
	}

	/**
	 * Creates a period from the specified number of weeks.
	 *
	 * @param weeks the number of weeks
	 * @return a {@link ArabicPeriod} representing the specified weeks
	 */
	@NaftahFn(
				name = "أنشئ_فترة_من_أسابيع",
				description = "إنشاء فترة من عدد أسابيع",
				usage = "أنشئ_فترة_من_أسابيع(أسابيع)",
				parameterTypes = {Number.class},
				returnType = ArabicPeriod.class
	)
	public static ArabicPeriod createPeriodFromWeeks(Number weeks) {
		return ArabicPeriod.ofWeeks(weeks.intValue());
	}

	/**
	 * Creates a period from the specified number of days.
	 *
	 * @param days the number of days
	 * @return a {@link ArabicPeriod} representing the specified days
	 */
	@NaftahFn(
				name = "أنشئ_فترة_من_أيام",
				description = "إنشاء فترة من عدد أيام",
				usage = "أنشئ_فترة_من_أيام(أيام)",
				parameterTypes = {Number.class},
				returnType = ArabicPeriod.class
	)
	public static ArabicPeriod createPeriodFromDays(Number days) {
		return ArabicPeriod.ofDays(days.intValue());
	}

	/**
	 * Creates a period with duration of zero (both period and duration are zero).
	 *
	 * @return a {@link ArabicPeriodWithDuration} representing zero period and zero duration
	 */
	@NaftahFn(
				name = "أنشئ_فترة_و_مدة_صفرية",
				description = "إنشاء فترة و مدة صفرية",
				usage = "أنشئ_فترة_و_مدة_صفرية()",
				returnType = ArabicPeriodWithDuration.class
	)
	public static ArabicPeriodWithDuration createZeroPeriodWithDuration() {
		return ArabicPeriodWithDuration.ofZero();
	}

	/**
	 * Creates a period with duration from the specified number of years.
	 *
	 * @param years the number of years
	 * @return a {@link ArabicPeriodWithDuration} with specified years and zero duration
	 */
	@NaftahFn(
				name = "أنشئ_فترة_و_مدة_من_سنوات",
				description = "إنشاء فترة و مدة من عدد سنوات",
				usage = "أنشئ_فترة_و_مدة_من_سنوات(سنوات)",
				parameterTypes = {Number.class},
				returnType = ArabicPeriodWithDuration.class
	)
	public static ArabicPeriodWithDuration createPeriodWithDurationFromYears(Number years) {
		return ArabicPeriodWithDuration.ofYears(years.intValue());
	}

	/**
	 * Creates a period with duration from the specified number of months.
	 *
	 * @param months the number of months
	 * @return a {@link ArabicPeriodWithDuration} with specified months and zero duration
	 */
	@NaftahFn(
				name = "أنشئ_فترة_و_مدة_من_شهور",
				description = "إنشاء فترة و مدة من عدد أشهر",
				usage = "أنشئ_فترة_و_مدة_من_شهور(شهور)",
				parameterTypes = {Number.class},
				returnType = ArabicPeriodWithDuration.class
	)
	public static ArabicPeriodWithDuration createPeriodWithDurationFromMonths(Number months) {
		return ArabicPeriodWithDuration.ofMonths(months.intValue());
	}

	/**
	 * Creates a period with duration from the specified number of weeks.
	 *
	 * @param weeks the number of weeks
	 * @return a {@link ArabicPeriodWithDuration} with specified weeks and zero duration
	 */
	@NaftahFn(
				name = "أنشئ_فترة_و_مدة_من_أسابيع",
				description = "إنشاء فترة و مدة من عدد أسابيع",
				usage = "أنشئ_فترة_و_مدة_من_أسابيع(أسابيع)",
				parameterTypes = {Number.class},
				returnType = ArabicPeriodWithDuration.class
	)
	public static ArabicPeriodWithDuration createPeriodWithDurationFromWeeks(Number weeks) {
		return ArabicPeriodWithDuration.ofWeeks(weeks.intValue());
	}

	/**
	 * Creates a period with duration from the specified number of days.
	 *
	 * @param days the number of days
	 * @return a {@link ArabicPeriodWithDuration} with specified days and zero duration
	 */
	@NaftahFn(
				name = "أنشئ_فترة_و_مدة_من_أيام",
				description = "إنشاء فترة و مدة من عدد أيام",
				usage = "أنشئ_فترة_و_مدة_من_أيام(أيام)",
				parameterTypes = {Number.class},
				returnType = ArabicPeriodWithDuration.class
	)
	public static ArabicPeriodWithDuration createPeriodWithDurationFromDays(Number days) {
		return ArabicPeriodWithDuration.ofDays(days.intValue());
	}

	/**
	 * Creates a period with duration from the specified number of hours.
	 *
	 * @param hours the number of hours
	 * @return a {@link ArabicPeriodWithDuration} with zero period and specified hours as duration
	 */
	@NaftahFn(
				name = "أنشئ_فترة_و_مدة_من_ساعات",
				description = "إنشاء فترة و مدة من عدد ساعات",
				usage = "أنشئ_فترة_و_مدة_من_ساعات(ساعات)",
				parameterTypes = {Number.class},
				returnType = ArabicPeriodWithDuration.class
	)
	public static ArabicPeriodWithDuration createPeriodWithDurationFromHours(Number hours) {
		return ArabicPeriodWithDuration.ofHours(hours.longValue());
	}

	/**
	 * Creates a period with duration from the specified number of minutes.
	 *
	 * @param minutes the number of minutes
	 * @return a {@link ArabicPeriodWithDuration} with zero period and specified minutes as duration
	 */
	@NaftahFn(
				name = "أنشئ_فترة_و_مدة_من_دقائق",
				description = "إنشاء فترة و مدة من عدد دقائق",
				usage = "أنشئ_فترة_و_مدة_من_دقائق(دقائق)",
				parameterTypes = {Number.class},
				returnType = ArabicPeriodWithDuration.class
	)
	public static ArabicPeriodWithDuration createPeriodWithDurationFromMinutes(Number minutes) {
		return ArabicPeriodWithDuration.ofMinutes(minutes.longValue());
	}

	/**
	 * Creates a period with duration from the specified number of seconds.
	 *
	 * @param seconds the number of seconds
	 * @return a {@link ArabicPeriodWithDuration} with zero period and specified seconds as duration
	 */
	@NaftahFn(
				name = "أنشئ_فترة_و_مدة_من_ثواني",
				description = "إنشاء فترة و مدة من عدد ثواني",
				usage = "أنشئ_فترة_و_مدة_من_ثواني(ثواني)",
				parameterTypes = {Number.class},
				returnType = ArabicPeriodWithDuration.class
	)
	public static ArabicPeriodWithDuration createPeriodWithDurationFromSeconds(Number seconds) {
		return ArabicPeriodWithDuration.ofSeconds(seconds.longValue());
	}

	/**
	 * Creates a period with duration from the specified seconds and nanosecond adjustment.
	 *
	 * @param seconds        the number of seconds
	 * @param nanoAdjustment the nanosecond adjustment
	 * @return a {@link ArabicPeriodWithDuration} with zero period and adjusted duration
	 */
	@NaftahFn(
				name = "أنشئ_فترة_و_مدة_من_ثواني_مع_نانو",
				description = "إنشاء فترة و مدة من ثواني مع تعديل النانوثانية",
				usage = "أنشئ_فترة_و_مدة_من_ثواني_مع_نانو(ثواني, نانو)",
				parameterTypes = {Number.class, Number.class},
				returnType = ArabicPeriodWithDuration.class
	)
	public static ArabicPeriodWithDuration createPeriodWithDurationFromSecondsAndNano(  Number seconds,
																						Number nanoAdjustment) {
		return ArabicPeriodWithDuration.ofSeconds(seconds.longValue(), nanoAdjustment.longValue());
	}

	/**
	 * Creates a period with duration from the specified number of milliseconds.
	 *
	 * @param millis the number of milliseconds
	 * @return a {@link ArabicPeriodWithDuration} with zero period and specified milliseconds as duration
	 */
	@NaftahFn(
				name = "أنشئ_فترة_و_مدة_من_مللي",
				description = "إنشاء فترة و مدة من عدد مللي ثانية",
				usage = "أنشئ_فترة_و_مدة_من_مللي(مللي)",
				parameterTypes = {Number.class},
				returnType = ArabicPeriodWithDuration.class
	)
	public static ArabicPeriodWithDuration createPeriodWithDurationFromMillis(Number millis) {
		return ArabicPeriodWithDuration.ofMillis(millis.longValue());
	}

	/**
	 * Creates a period with duration from the specified number of nanoseconds.
	 *
	 * @param nanos the number of nanoseconds
	 * @return a {@link ArabicPeriodWithDuration} with zero period and specified nanoseconds as duration
	 */
	@NaftahFn(
				name = "أنشئ_فترة_و_مدة_من_نانو",
				description = "إنشاء فترة و مدة من عدد نانو ثانية",
				usage = "أنشئ_فترة_و_مدة_من_نانو(نانو)",
				parameterTypes = {Number.class},
				returnType = ArabicPeriodWithDuration.class
	)
	public static ArabicPeriodWithDuration createPeriodWithDurationFromNanos(Number nanos) {
		return ArabicPeriodWithDuration.ofNanos(nanos.longValue());
	}

	/**
	 * Adds a number of years to the given period.
	 *
	 * @param p     the original period
	 * @param years the number of years to add
	 * @return a {@link ArabicPeriod} with years added
	 */
	@NaftahFn(
				name = "أضف_سنوات_لفترة",
				description = "إضافة عدد من السنوات إلى فترة",
				usage = "أضف_سنوات_لفترة(فترة_, سنوات)",
				parameterTypes = {ArabicPeriod.class, Number.class},
				returnType = ArabicPeriod.class
	)
	public static ArabicPeriod plusYears(ArabicPeriod p, Number years) {
		return p.plusYears(years.longValue());
	}

	/**
	 * Adds a number of months to the given period.
	 *
	 * @param p      the original period
	 * @param months the number of months to add
	 * @return a {@link ArabicPeriod} with months added
	 */
	@NaftahFn(
				name = "أضف_أشهر_لفترة",
				description = "إضافة عدد من الأشهر إلى فترة",
				usage = "أضف_أشهر_لفترة(فترة_, أشهر)",
				parameterTypes = {ArabicPeriod.class, Number.class},
				returnType = ArabicPeriod.class
	)
	public static ArabicPeriod plusMonths(ArabicPeriod p, Number months) {
		return p.plusMonths(months.longValue());
	}

	/**
	 * Adds a number of days to the given period.
	 *
	 * @param p    the original period
	 * @param days the number of days to add
	 * @return a {@link ArabicPeriod} with days added
	 */
	@NaftahFn(
				name = "أضف_أيام_لفترة",
				description = "إضافة عدد من الأيام إلى فترة",
				usage = "أضف_أيام_لفترة(فترة_, أيام)",
				parameterTypes = {ArabicPeriod.class, Number.class},
				returnType = ArabicPeriod.class
	)
	public static ArabicPeriod plusDays(ArabicPeriod p, Number days) {
		return p.plusDays(days.longValue());
	}

	/**
	 * Subtracts a number of years from the given period.
	 *
	 * @param p     the original period
	 * @param years the number of years to subtract
	 * @return a {@link ArabicPeriod} with years subtracted
	 */
	@NaftahFn(
				name = "اطرح_سنوات_من_الفترة",
				description = "طرح عدد من السنوات من فترة",
				usage = "اطرح_سنوات_من_الفترة(فترة_, سنوات)",
				parameterTypes = {ArabicPeriod.class, Number.class},
				returnType = ArabicPeriod.class
	)
	public static ArabicPeriod minusYears(ArabicPeriod p, Number years) {
		return p.minusYears(years.longValue());
	}

	/**
	 * Subtracts a number of months from the given period.
	 *
	 * @param p      the original period
	 * @param months the number of months to subtract
	 * @return a {@link ArabicPeriod} with months subtracted
	 */
	@NaftahFn(
				name = "اطرح_أشهر_من_الفترة",
				description = "طرح عدد من الأشهر من فترة",
				usage = "اطرح_أشهر_من_الفترة(فترة_, أشهر)",
				parameterTypes = {ArabicPeriod.class, Number.class},
				returnType = ArabicPeriod.class
	)
	public static ArabicPeriod minusMonths(ArabicPeriod p, Number months) {
		return p.minusMonths(months.longValue());
	}

	/**
	 * Subtracts a number of days from the given period.
	 *
	 * @param p    the original period
	 * @param days the number of days to subtract
	 * @return a {@link ArabicPeriod} with days subtracted
	 */
	@NaftahFn(
				name = "اطرح_أيام_من_الفترة",
				description = "طرح عدد من الأيام من فترة",
				usage = "اطرح_أيام_من_الفترة(فترة_, أيام)",
				parameterTypes = {ArabicPeriod.class, Number.class},
				returnType = ArabicPeriod.class
	)
	public static ArabicPeriod minusDays(ArabicPeriod p, Number days) {
		return p.minusDays(days.longValue());
	}

	/**
	 * Adds a number of days to a given duration.
	 *
	 * @param d    the original duration
	 * @param days the number of days to add
	 * @return a {@link ArabicDuration} with days added
	 */
	@NaftahFn(
				name = "أضف_أيام_للمدة",
				description = "إضافة أيام إلى مدة",
				usage = "أضف_أيام_للمدة(مدة_, أيام)",
				parameterTypes = {ArabicDuration.class, Number.class},
				returnType = ArabicDuration.class
	)
	public static ArabicDuration plusDays(ArabicDuration d, Number days) {
		return d.plusDays(days.longValue());
	}

	/**
	 * Adds a number of hours to a given duration.
	 *
	 * @param d     the original duration
	 * @param hours the number of hours to add
	 * @return a {@link ArabicDuration} with hours added
	 */
	@NaftahFn(
				name = "أضف_ساعات_للمدة",
				description = "إضافة ساعات إلى مدة",
				usage = "أضف_ساعات_للمدة(مدة_, ساعات)",
				parameterTypes = {ArabicDuration.class, Number.class},
				returnType = ArabicDuration.class
	)
	public static ArabicDuration plusHours(ArabicDuration d, Number hours) {
		return d.plusHours(hours.longValue());
	}

	/**
	 * Adds a number of minutes to a given duration.
	 *
	 * @param d       the original duration
	 * @param minutes the number of minutes to add
	 * @return a {@link ArabicDuration} with minutes added
	 */
	@NaftahFn(
				name = "أضف_دقائق_للمدة",
				description = "إضافة دقائق إلى مدة",
				usage = "أضف_دقائق_للمدة(مدة_, دقائق)",
				parameterTypes = {ArabicDuration.class, Number.class},
				returnType = ArabicDuration.class
	)
	public static ArabicDuration plusMinutes(ArabicDuration d, Number minutes) {
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
				description = "إضافة ثواني إلى مدة",
				usage = "أضف_ثواني_للمدة(مدة_, ثواني)",
				parameterTypes = {ArabicDuration.class, Number.class},
				returnType = ArabicDuration.class
	)
	public static ArabicDuration plusSeconds(ArabicDuration d, Number seconds) {
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
				description = "إضافة مللي ثانية إلى مدة",
				usage = "أضف_مللي_للمدة(مدة_, مللي)",
				parameterTypes = {ArabicDuration.class, Number.class},
				returnType = ArabicDuration.class
	)
	public static ArabicDuration plusMillis(ArabicDuration d, Number millis) {
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
				description = "إضافة نانو ثانية إلى مدة",
				usage = "أضف_نانو_للمدة(مدة_, نانو)",
				parameterTypes = {ArabicDuration.class, Number.class},
				returnType = ArabicDuration.class
	)
	public static ArabicDuration plusNanos(ArabicDuration d, Number nanos) {
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
				description = "طرح أيام من مدة",
				usage = "اطرح_أيام_من_المدة(مدة_, أيام)",
				parameterTypes = {ArabicDuration.class, Number.class},
				returnType = ArabicDuration.class
	)
	public static ArabicDuration minusDays(ArabicDuration d, Number days) {
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
				description = "طرح ساعات من مدة",
				usage = "اطرح_ساعات_من_المدة(مدة_, ساعات)",
				parameterTypes = {ArabicDuration.class, Number.class},
				returnType = ArabicDuration.class
	)
	public static ArabicDuration minusHours(ArabicDuration d, Number hours) {
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
				description = "طرح دقائق من مدة",
				usage = "اطرح_دقائق_من_المدة(مدة_, دقائق)",
				parameterTypes = {ArabicDuration.class, Number.class},
				returnType = ArabicDuration.class
	)
	public static ArabicDuration minusMinutes(ArabicDuration d, Number minutes) {
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
				description = "طرح ثواني من مدة",
				usage = "اطرح_ثواني_من_المدة(مدة_, ثواني)",
				parameterTypes = {ArabicDuration.class, Number.class},
				returnType = ArabicDuration.class
	)
	public static ArabicDuration minusSeconds(ArabicDuration d, Number seconds) {
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
				description = "طرح مللي ثانية من مدة",
				usage = "اطرح_مللي_من_المدة(مدة_, مللي)",
				parameterTypes = {ArabicDuration.class, Number.class},
				returnType = ArabicDuration.class
	)
	public static ArabicDuration minusMillis(ArabicDuration d, Number millis) {
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
				description = "طرح نانو ثانية من مدة",
				usage = "اطرح_نانو_من_المدة(مدة_, نانو)",
				parameterTypes = {ArabicDuration.class, Number.class},
				returnType = ArabicDuration.class
	)
	public static ArabicDuration minusNanos(ArabicDuration d, Number nanos) {
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
				description = "إضافة عدد من السنوات إلى فترة تحتوي على مدة",
				usage = "أضف_سنوات_لفترة_مع_مدة(فترة_, سنوات)",
				parameterTypes = {ArabicPeriodWithDuration.class, Number.class},
				returnType = ArabicPeriodWithDuration.class
	)
	public static ArabicPeriodWithDuration plusYears(ArabicPeriodWithDuration pd, Number years) {
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
				description = "إضافة عدد من الأشهر إلى فترة تحتوي على مدة",
				usage = "أضف_أشهر_لفترة_مع_مدة(فترة_, أشهر)",
				parameterTypes = {ArabicPeriodWithDuration.class, Number.class},
				returnType = ArabicPeriodWithDuration.class
	)
	public static ArabicPeriodWithDuration plusMonths(ArabicPeriodWithDuration pd, Number months) {
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
				description = "إضافة عدد من الأيام إلى فترة تحتوي على مدة",
				usage = "أضف_أيام_لفترة_مع_مدة(فترة_, أيام)",
				parameterTypes = {ArabicPeriodWithDuration.class, Number.class},
				returnType = ArabicPeriodWithDuration.class
	)
	public static ArabicPeriodWithDuration plusDays(ArabicPeriodWithDuration pd, Number days) {
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
				description = "إضافة عدد من الساعات إلى فترة تحتوي على مدة",
				usage = "أضف_ساعات_لفترة_مع_مدة(فترة_, ساعات)",
				parameterTypes = {ArabicPeriodWithDuration.class, Number.class},
				returnType = ArabicPeriodWithDuration.class
	)
	public static ArabicPeriodWithDuration plusHours(ArabicPeriodWithDuration pd, Number hours) {
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
				description = "إضافة عدد من الدقائق إلى فترة تحتوي على مدة",
				usage = "أضف_دقائق_لفترة_مع_مدة(فترة_, دقائق)",
				parameterTypes = {ArabicPeriodWithDuration.class, Number.class},
				returnType = ArabicPeriodWithDuration.class
	)
	public static ArabicPeriodWithDuration plusMinutes(ArabicPeriodWithDuration pd, Number minutes) {
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
				description = "إضافة عدد من الثواني إلى فترة تحتوي على مدة",
				usage = "أضف_ثواني_لفترة_مع_مدة(فترة_, ثواني)",
				parameterTypes = {ArabicPeriodWithDuration.class, Number.class},
				returnType = ArabicPeriodWithDuration.class
	)
	public static ArabicPeriodWithDuration plusSeconds(ArabicPeriodWithDuration pd, Number seconds) {
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
				description = "إضافة عدد من المللي ثانية إلى فترة تحتوي على مدة",
				usage = "أضف_مللي_لفترة_مع_مدة(فترة_, مللي)",
				parameterTypes = {ArabicPeriodWithDuration.class, Number.class},
				returnType = ArabicPeriodWithDuration.class
	)
	public static ArabicPeriodWithDuration plusMillis(ArabicPeriodWithDuration pd, Number millis) {
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
				description = "إضافة عدد من النانو ثانية إلى فترة تحتوي على مدة",
				usage = "أضف_نانو_لفترة_مع_مدة(فترة_, نانو)",
				parameterTypes = {ArabicPeriodWithDuration.class, Number.class},
				returnType = ArabicPeriodWithDuration.class
	)
	public static ArabicPeriodWithDuration plusNanos(ArabicPeriodWithDuration pd, Number nanos) {
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
				description = "طرح عدد من السنوات من فترة تحتوي على مدة",
				usage = "اطرح_سنوات_من_الفترة_مع_مدة(فترة_, سنوات)",
				parameterTypes = {ArabicPeriodWithDuration.class, Number.class},
				returnType = ArabicPeriodWithDuration.class
	)
	public static ArabicPeriodWithDuration minusYears(ArabicPeriodWithDuration pd, Number years) {
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
				description = "طرح عدد من الأشهر من فترة تحتوي على مدة",
				usage = "اطرح_أشهر_من_الفترة_مع_مدة(فترة_, أشهر)",
				parameterTypes = {ArabicPeriodWithDuration.class, Number.class},
				returnType = ArabicPeriodWithDuration.class
	)
	public static ArabicPeriodWithDuration minusMonths(ArabicPeriodWithDuration pd, Number months) {
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
				description = "طرح عدد من الأيام من فترة تحتوي على مدة",
				usage = "اطرح_أيام_من_الفترة_مع_مدة(فترة_, أيام)",
				parameterTypes = {ArabicPeriodWithDuration.class, Number.class},
				returnType = ArabicPeriodWithDuration.class
	)
	public static ArabicPeriodWithDuration minusDays(ArabicPeriodWithDuration pd, Number days) {
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
				description = "طرح عدد من الساعات من فترة تحتوي على مدة",
				usage = "اطرح_ساعات_من_الفترة_مع_مدة(فترة_, ساعات)",
				parameterTypes = {ArabicPeriodWithDuration.class, Number.class},
				returnType = ArabicPeriodWithDuration.class
	)
	public static ArabicPeriodWithDuration minusHours(ArabicPeriodWithDuration pd, Number hours) {
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
				description = "طرح عدد من الدقائق من فترة تحتوي على مدة",
				usage = "اطرح_دقائق_من_الفترة_مع_مدة(فترة_, دقائق)",
				parameterTypes = {ArabicPeriodWithDuration.class, Number.class},
				returnType = ArabicPeriodWithDuration.class
	)
	public static ArabicPeriodWithDuration minusMinutes(ArabicPeriodWithDuration pd, Number minutes) {
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
				description = "طرح عدد من الثواني من فترة تحتوي على مدة",
				usage = "اطرح_ثواني_من_الفترة_مع_مدة(فترة_, ثواني)",
				parameterTypes = {ArabicPeriodWithDuration.class, Number.class},
				returnType = ArabicPeriodWithDuration.class
	)
	public static ArabicPeriodWithDuration minusSeconds(ArabicPeriodWithDuration pd, Number seconds) {
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
				description = "طرح عدد من المللي ثانية من فترة تحتوي على مدة",
				usage = "اطرح_مللي_من_الفترة_مع_مدة(فترة_, مللي)",
				parameterTypes = {ArabicPeriodWithDuration.class, Number.class},
				returnType = ArabicPeriodWithDuration.class
	)
	public static ArabicPeriodWithDuration minusMillis(ArabicPeriodWithDuration pd, Number millis) {
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
				description = "طرح عدد من النانو ثانية من فترة تحتوي على مدة",
				usage = "اطرح_نانو_من_الفترة_مع_مدة(فترة_, نانو)",
				parameterTypes = {ArabicPeriodWithDuration.class, Number.class},
				returnType = ArabicPeriodWithDuration.class
	)
	public static ArabicPeriodWithDuration minusNanos(ArabicPeriodWithDuration pd, Number nanos) {
		return pd.minusNanos(nanos.longValue());
	}

	/**
	 * Gets the year from a temporal point.
	 *
	 * @param t the temporal point
	 * @return the year
	 */
	@NaftahFn(
				name = "احصل_على_السنة",
				description = "الحصول على السنة من نقطة زمنية",
				usage = "احصل_على_السنة(نقطة_زمنية_)",
				parameterTypes = {ArabicTemporalPoint.class},
				returnType = int.class
	)
	public static int getYear(ArabicTemporalPoint t) {
		return t.temporal().get(ChronoField.YEAR);
	}

	/**
	 * Gets the month from a temporal point.
	 *
	 * @param t the temporal point
	 * @return the month (1-12)
	 */
	@NaftahFn(
				name = "احصل_على_الشهر",
				description = "الحصول على الشهر من نقطة زمنية",
				usage = "احصل_على_الشهر(نقطة_زمنية_)",
				parameterTypes = {ArabicTemporalPoint.class},
				returnType = int.class
	)
	public static int getMonth(ArabicTemporalPoint t) {
		return t.temporal().get(ChronoField.MONTH_OF_YEAR);
	}

	/**
	 * Gets the day of the month from a temporal point.
	 *
	 * @param t the temporal point
	 * @return the day of the month
	 */
	@NaftahFn(
				name = "احصل_على_اليوم",
				description = "الحصول على اليوم من نقطة زمنية",
				usage = "احصل_على_اليوم(نقطة_زمنية_)",
				parameterTypes = {ArabicTemporalPoint.class},
				returnType = int.class
	)
	public static int getDay(ArabicTemporalPoint t) {
		return t.temporal().get(ChronoField.DAY_OF_MONTH);
	}

	/**
	 * Gets the hour of the day from a temporal point.
	 *
	 * @param t the temporal point
	 * @return the hour (0-23)
	 */
	@NaftahFn(
				name = "احصل_على_الساعة",
				description = "الحصول على الساعة من نقطة زمنية",
				usage = "احصل_على_الساعة(نقطة_زمنية_)",
				parameterTypes = {ArabicTemporalPoint.class},
				returnType = int.class
	)
	public static int getHour(ArabicTemporalPoint t) {
		return t.temporal().get(ChronoField.HOUR_OF_DAY);
	}

	/**
	 * Gets the minute of the hour from a temporal point.
	 *
	 * @param t the temporal point
	 * @return the minute
	 */
	@NaftahFn(
				name = "احصل_على_الدقيقة",
				description = "الحصول على الدقيقة من نقطة زمنية",
				usage = "احصل_على_الدقيقة(نقطة_زمنية_)",
				parameterTypes = {ArabicTemporalPoint.class},
				returnType = int.class
	)
	public static int getMinute(ArabicTemporalPoint t) {
		return t.temporal().get(ChronoField.MINUTE_OF_HOUR);
	}

	/**
	 * Gets the second of the minute from a temporal point.
	 *
	 * @param t the temporal point
	 * @return the second
	 */
	@NaftahFn(
				name = "احصل_على_الثانية",
				description = "الحصول على الثانية من نقطة زمنية",
				usage = "احصل_على_الثانية(نقطة_زمنية_)",
				parameterTypes = {ArabicTemporalPoint.class},
				returnType = int.class
	)
	public static int getSecond(ArabicTemporalPoint t) {
		return t.temporal().get(ChronoField.SECOND_OF_MINUTE);
	}

	/**
	 * Gets the millisecond of the second from a temporal point.
	 *
	 * @param t the temporal point
	 * @return the millisecond
	 */
	@NaftahFn(
				name = "احصل_على_الملي_ثانية",
				description = "الحصول على المللي ثانية من نقطة زمنية",
				usage = "احصل_على_الملي_ثانية(نقطة_زمنية_)",
				parameterTypes = {ArabicTemporalPoint.class},
				returnType = int.class
	)
	public static int getMilli(ArabicTemporalPoint t) {
		return t.temporal().get(ChronoField.MILLI_OF_SECOND);
	}

	/**
	 * Gets the nanosecond of the second from a temporal point.
	 *
	 * @param t the temporal point
	 * @return the nanosecond
	 */
	@NaftahFn(
				name = "احصل_على_النانو_ثانية",
				description = "الحصول على النانو ثانية من نقطة زمنية",
				usage = "احصل_على_النانو_ثانية(نقطة_زمنية_)",
				parameterTypes = {ArabicTemporalPoint.class},
				returnType = int.class
	)
	public static int getNano(ArabicTemporalPoint t) {
		return t.temporal().get(ChronoField.NANO_OF_SECOND);
	}

	/**
	 * Gets the number of years in a period.
	 *
	 * @param p the period
	 * @return the number of years
	 */
	@NaftahFn(
				name = "احصل_على_سنوات_الفترة",
				description = "الحصول على عدد السنوات من فترة",
				usage = "احصل_على_سنوات_الفترة(فترة_)",
				parameterTypes = {ArabicPeriod.class},
				returnType = int.class
	)
	public static int getYears(ArabicPeriod p) {
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
				description = "الحصول على عدد الأشهر من فترة",
				usage = "احصل_على_أشهر_الفترة(فترة_)",
				parameterTypes = {ArabicPeriod.class},
				returnType = int.class
	)
	public static int getMonths(ArabicPeriod p) {
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
				description = "الحصول على عدد الأيام من فترة",
				usage = "احصل_على_أيام_الفترة(فترة_)",
				parameterTypes = {ArabicPeriod.class},
				returnType = int.class
	)
	public static int getDays(ArabicPeriod p) {
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
				description = "الحصول على عدد الساعات من مدة",
				usage = "احصل_على_ساعات_المدة(مدة_)",
				parameterTypes = {ArabicDuration.class},
				returnType = long.class
	)
	public static long getHours(ArabicDuration d) {
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
				description = "الحصول على عدد الدقائق من مدة",
				usage = "احصل_على_دقائق_المدة(مدة_)",
				parameterTypes = {ArabicDuration.class},
				returnType = long.class
	)
	public static long getMinutes(ArabicDuration d) {
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
				description = "الحصول على عدد الثواني من مدة",
				usage = "احصل_على_ثواني_المدة(مدة_)",
				parameterTypes = {ArabicDuration.class},
				returnType = long.class
	)
	public static long getSeconds(ArabicDuration d) {
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
				description = "الحصول على عدد المللي ثانية من مدة",
				usage = "احصل_على_مللي_المدة(مدة_)",
				parameterTypes = {ArabicDuration.class},
				returnType = long.class
	)
	public static long getMillis(ArabicDuration d) {
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
				description = "الحصول على عدد النانو ثانية من مدة",
				usage = "احصل_على_نانو_المدة(مدة_)",
				parameterTypes = {ArabicDuration.class},
				returnType = int.class
	)
	public static int getNano(ArabicDuration d) {
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
				description = "الحصول على عدد السنوات من فترة تحتوي على مدة",
				usage = "احصل_على_سنوات_الفترة_مع_مدة(فترة_)",
				parameterTypes = {ArabicPeriodWithDuration.class},
				returnType = int.class
	)
	public static int getYears(ArabicPeriodWithDuration p) {
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
				description = "الحصول على عدد الأشهر من فترة تحتوي على مدة",
				usage = "احصل_على_أشهر_الفترة_مع_مدة(فترة_)",
				parameterTypes = {ArabicPeriodWithDuration.class},
				returnType = int.class
	)
	public static int getMonths(ArabicPeriodWithDuration p) {
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
				description = "الحصول على عدد الأيام من فترة تحتوي على مدة",
				usage = "احصل_على_أيام_الفترة_مع_مدة(فترة_)",
				parameterTypes = {ArabicPeriodWithDuration.class},
				returnType = int.class
	)
	public static int getDays(ArabicPeriodWithDuration p) {
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
				description = "الحصول على عدد الساعات من فترة تحتوي على مدة",
				usage = "احصل_على_ساعات_الفترة_مع_مدة(فترة_)",
				parameterTypes = {ArabicPeriodWithDuration.class},
				returnType = long.class
	)
	public static long getHours(ArabicPeriodWithDuration d) {
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
				description = "الحصول على عدد الدقائق من فترة تحتوي على مدة",
				usage = "احصل_على_دقائق_الفترة_مع_مدة(فترة_)",
				parameterTypes = {ArabicPeriodWithDuration.class},
				returnType = long.class
	)
	public static long getMinutes(ArabicPeriodWithDuration d) {
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
				description = "الحصول على عدد الثواني من فترة تحتوي على مدة",
				usage = "احصل_على_ثواني_الفترة_مع_مدة(فترة_)",
				parameterTypes = {ArabicPeriodWithDuration.class},
				returnType = long.class
	)
	public static long getSeconds(ArabicPeriodWithDuration d) {
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
				description = "الحصول على عدد المللي ثانية من فترة تحتوي على مدة",
				usage = "احصل_على_مللي_الفترة_مع_مدة(فترة_)",
				parameterTypes = {ArabicPeriodWithDuration.class},
				returnType = long.class
	)
	public static long getMillis(ArabicPeriodWithDuration d) {
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
				description = "الحصول على عدد النانو ثانية من فترة تحتوي على مدة",
				usage = "احصل_على_نانو_الفترة_مع_مدة(فترة_)",
				parameterTypes = {ArabicPeriodWithDuration.class},
				returnType = int.class
	)
	public static int getNano(ArabicPeriodWithDuration d) {
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
				description = "إنشاء مدة زمنية بين نقطتين زمنيتين",
				usage = "أنشئ_مدة_بين_نقطتين(نقطة_زمنية_أولى, نقطة_زمنية_ثانية)",
				parameterTypes = {ArabicTemporalPoint.class, ArabicTemporalPoint.class},
				returnType = ArabicTemporalAmount.class
	)
	public static ArabicTemporalAmount createTemporalAmountBetween( ArabicTemporalPoint left,
																	ArabicTemporalPoint right) {
		return ArabicDateParserHelper.getArabicTemporalAmountBetween(left, right);
	}
}
