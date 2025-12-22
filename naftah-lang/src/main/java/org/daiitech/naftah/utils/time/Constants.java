package org.daiitech.naftah.utils.time;

import org.daiitech.naftah.errors.NaftahBugError;

import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;

/**
 * A utility class containing constant values used across the Arabic date-time parsing library.
 *
 * <p>Includes:
 * <ul>
 * <li>Arabic lexer/parser keywords</li>
 * <li>Arabic calendar names (Gregorian and Hijri) and their aliases</li>
 * <li>AM/PM markers in Arabic</li>
 * <li>Gregorian and Hijri month names in Arabic and Latin variants</li>
 * </ul>
 *
 * <p>All constants are publicly accessible and static.</p>
 *
 * @author Chakib Daii
 */
public final class Constants {
	/**
	 * The Arabic prefix used to indicate a time zone.
	 */
	public static final String ZONE_PREFIX_AR = "بتوقيت";
	/**
	 * The Arabic prefix used to indicate a calendar.
	 */
	public static final String CALENDAR_PREFIX_AR = "بالتقويم";
	/**
	 * Default Gregorian calendar names in Arabic, including aliases.
	 */
	public static final String DEFAULT_CALENDAR_NAME = "ميلادي";
	public static final String DEFAULT_CALENDAR_NAME_1 = "الميلادي";
	public static final String DEFAULT_CALENDAR_NAME_2 = "التقويم الميلادي";
	public static final String DEFAULT_CALENDAR_NAME_3 = "جرجوري";
	/**
	 * Hijri (Islamic) calendar names in Arabic, including aliases.
	 */
	public static final String HIJRI_CALENDAR_NAME = "هجري";
	public static final String HIJRI_CALENDAR_NAME_1 = "الهجري";
	public static final String HIJRI_CALENDAR_NAME_2 = "التقويم الهجري";
	/**
	 * Arabic markers for AM (morning) and their variations.
	 */
	public static final String AM_SHORT = "ص";
	public static final String AM_FULL_1 = "صباحاً";
	public static final String AM_FULL_2 = "صباحا";
	/**
	 * Arabic markers for PM (evening) and their variations.
	 */
	public static final String PM_SHORT = "م";
	public static final String PM_FULL_1 = "مساءً";
	public static final String PM_FULL_2 = "مساءا";
	/**
	 * Gregorian month names in Arabic and Latin variants.
	 */
	public static final String JANUARY_AR = "يناير";
	public static final String JANUARY_LATIN = "جانفي";
	public static final String FEBRUARY_AR = "فبراير";
	public static final String FEBRUARY_LATIN = "فيفري";
	public static final String MARCH = "مارس";
	public static final String APRIL_AR = "أبريل";
	public static final String APRIL_LATIN = "أفريل";
	public static final String MAY_AR = "مايو";
	public static final String MAY_LATIN = "ماي";
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
	 * Alternative Arabic spelling for October.
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
	 * Private constructor to prevent instantiation.
	 * Always throws a {@link NaftahBugError} when called.
	 */
	private Constants() {
		throw newNaftahBugInvalidUsageError();
	}
}
