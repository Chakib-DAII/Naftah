package org.daiitech.naftah.utils.time;

import org.daiitech.naftah.errors.NaftahBugError;

import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;

/**
 * A utility class containing constant values used across the Naftah date-time parsing library.
 *
 * <p>Includes:
 * <ul>
 * <li>Lexer/parser keywords</li>
 * <li>Calendar names (Gregorian and Hijri) and their aliases</li>
 * <li>AM/PM markers</li>
 * <li>Gregorian and Hijri month names with variants</li>
 * </ul>
 *
 * <p>All constants are publicly accessible and static.</p>
 *
 * @author Chakib Daii
 */
public final class Constants {
	/**
	 * Prefix used to indicate a time zone.
	 */
	public static final String ZONE_PREFIX_AR = "بتوقيت";
	/**
	 * Prefix used to indicate a calendar.
	 */
	public static final String CALENDAR_PREFIX_AR = "بالتقويم";
	/**
	 * Default Gregorian calendar name.
	 */
	public static final String DEFAULT_CALENDAR_NAME = "ميلادي";
	/**
	 * Alternative spelling for the Gregorian calendar.
	 */
	public static final String DEFAULT_CALENDAR_NAME_1 = "الميلادي";
	/**
	 * Alternative spelling for the Gregorian calendar.
	 */
	public static final String DEFAULT_CALENDAR_NAME_2 = "التقويم الميلادي";
	/**
	 * Alternative spelling for the Gregorian calendar (Latin-based alias).
	 */
	public static final String DEFAULT_CALENDAR_NAME_3 = "جرجوري";
	/**
	 * Hijri (Islamic) calendar name in Arabic.
	 */
	public static final String HIJRI_CALENDAR_NAME = "هجري";
	/**
	 * Alternative spelling for the Hijri calendar.
	 */
	public static final String HIJRI_CALENDAR_NAME_1 = "الهجري";
	/**
	 * Alternative spelling for the Hijri calendar.
	 */
	public static final String HIJRI_CALENDAR_NAME_2 = "التقويم الهجري";
	/**
	 * Short marker for AM (morning).
	 */
	public static final String AM_SHORT = "ص";
	/**
	 * Full marker for AM (morning).
	 */
	public static final String AM_FULL_1 = "صباحاً";
	/**
	 * Alternative full marker for AM (morning).
	 */
	public static final String AM_FULL_2 = "صباحا";
	/**
	 * Short marker for PM (evening).
	 */
	public static final String PM_SHORT = "م";
	/**
	 * Full marker for PM (evening).
	 */
	public static final String PM_FULL_1 = "مساءً";
	/**
	 * Alternative full marker for PM (evening).
	 */
	public static final String PM_FULL_2 = "مساءا";
	/**
	 * January in Arabic.
	 */
	public static final String JANUARY_AR = "يناير";
	/**
	 * January in Latin variant.
	 */
	public static final String JANUARY_LATIN = "جانفي";
	/**
	 * February in Arabic.
	 */
	public static final String FEBRUARY_AR = "فبراير";
	/**
	 * February in Latin variant.
	 */
	public static final String FEBRUARY_LATIN = "فيفري";
	/**
	 * March in Arabic.
	 */
	public static final String MARCH = "مارس";
	/**
	 * April in Arabic.
	 */
	public static final String APRIL_AR = "أبريل";
	/**
	 * April in Latin variant.
	 */
	public static final String APRIL_LATIN = "أفريل";
	/**
	 * May in Arabic.
	 */
	public static final String MAY_AR = "مايو";
	/**
	 * May in Latin variant.
	 */
	public static final String MAY_LATIN = "ماي";
	/**
	 * June in Arabic.
	 */
	public static final String JUNE_AR = "يونيو";
	/**
	 * June in Latin variant.
	 */
	public static final String JUNE_LATIN = "جوان";
	/**
	 * July in Arabic.
	 */
	public static final String JULY_AR = "يوليو";
	/**
	 * July in Latin variant.
	 */
	public static final String JULY_LATIN = "جويلية";

	/**
	 * August in Arabic.
	 */
	public static final String AUGUST_AR = "أغسطس";
	/**
	 * August in Latin variant.
	 */
	public static final String AUGUST_LATIN = "أوت";
	/**
	 * September in Arabic.
	 */
	public static final String SEPTEMBER = "سبتمبر";
	/**
	 * October in Arabic.
	 */
	public static final String OCTOBER = "أكتوبر";
	/**
	 * Alternative spelling for October.
	 */
	public static final String OCTOBER_ALT = "اكتوبر";
	/**
	 * November in Arabic.
	 */
	public static final String NOVEMBER = "نوفمبر";
	/**
	 * December in Latin variant.
	 */
	public static final String DECEMBER_LATIN = "ديسمبر";
	/**
	 * December in Arabic.
	 */
	public static final String DECEMBER_AR = "دجنبر";
	/**
	 * Muharram, the first month in Hijri calendar.
	 */
	public static final String MUHARRAM = "محرم";
	/**
	 * Safar, the second month in Hijri calendar.
	 */
	public static final String SAFAR = "صفر";
	/**
	 * Rabi' al-awwal, the third month in Hijri calendar.
	 */
	public static final String RABI_AL_AWWAL = "ربيع الأول";
	/**
	 * Rabi' al-thani, the fourth month in Hijri calendar.
	 */
	public static final String RABI_AL_THANI = "ربيع الآخر";
	/**
	 * Alternative spelling for Rabi' al-thani.
	 */
	public static final String RABI_AL_THANI_ALT = "ربيع الثاني";
	/**
	 * Jumada al-awwal, the fifth month in Hijri calendar.
	 */
	public static final String JUMADA_AL_AWWAL = "جمادى الأولى";
	/**
	 * Jumada al-thani, the sixth month in Hijri calendar.
	 */
	public static final String JUMADA_AL_THANI = "جمادى الآخرة";
	/**
	 * Alternative spelling for Jumada al-thani.
	 */
	public static final String JUMADA_AL_THANI_ALT = "جمادى الثانية";
	/**
	 * Rajab, the seventh month in Hijri calendar.
	 */
	public static final String RAJAB = "رجب";
	/**
	 * Shaaban, the eighth month in Hijri calendar.
	 */
	public static final String SHAABAN = "شعبان";
	/**
	 * Ramadan, the ninth month in Hijri calendar.
	 */
	public static final String RAMADAN = "رمضان";
	/**
	 * Shawwal, the tenth month in Hijri calendar.
	 */
	public static final String SHAWAL = "شوال";
	/**
	 * Dhu al-Qidah, the eleventh month in Hijri calendar.
	 */
	public static final String DHU_AL_QIDAH = "ذو القعدة";
	/**
	 * Dhu al-Hijjah, the twelfth month in Hijri calendar.
	 */
	public static final String DHU_AL_HIJJAH = "ذو الحجة";
	/**
	 * Prefix used to indicate a duration.
	 */
	public static final String DURATION_PREFIX = "مدة";
	/**
	 * Prefix used to indicate a period.
	 */
	public static final String PERIOD_PREFIX = "فترة";
	/**
	 * Represents a day in Arabic.
	 */
	public static final String DAY = "يوم";
	/**
	 * Represents a month in Arabic.
	 */
	public static final String MONTH = "شهر";
	/**
	 * Represents a year in Arabic.
	 */
	public static final String YEAR = "سنة";
	/**
	 * Represents an hour in Arabic.
	 */
	public static final String HOUR = "ساعة";
	/**
	 * Represents a minute in Arabic.
	 */
	public static final String MINUTE = "دقيقة";
	/**
	 * Represents a second in Arabic.
	 */
	public static final String SECOND = "ثانية";
	/**
	 * Represents a nanosecond in Arabic.
	 */
	public static final String NANOSECOND = "نانوثانية";

	/**
	 * Private constructor to prevent instantiation.
	 * Always throws a {@link NaftahBugError} when called.
	 */
	private Constants() {
		throw newNaftahBugInvalidUsageError();
	}
}
