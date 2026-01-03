package org.daiitech.naftah.utils.time;

import java.time.Month;
import java.time.chrono.Chronology;
import java.time.chrono.HijrahChronology;

import org.daiitech.naftah.errors.NaftahBugError;

import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;
import static org.daiitech.naftah.utils.time.Constants.APRIL_AR;
import static org.daiitech.naftah.utils.time.Constants.APRIL_LATIN;
import static org.daiitech.naftah.utils.time.Constants.AUGUST_AR;
import static org.daiitech.naftah.utils.time.Constants.AUGUST_LATIN;
import static org.daiitech.naftah.utils.time.Constants.DECEMBER_AR;
import static org.daiitech.naftah.utils.time.Constants.DECEMBER_LATIN;
import static org.daiitech.naftah.utils.time.Constants.DHU_AL_HIJJAH;
import static org.daiitech.naftah.utils.time.Constants.DHU_AL_QIDAH;
import static org.daiitech.naftah.utils.time.Constants.FEBRUARY_AR;
import static org.daiitech.naftah.utils.time.Constants.FEBRUARY_LATIN;
import static org.daiitech.naftah.utils.time.Constants.JANUARY_AR;
import static org.daiitech.naftah.utils.time.Constants.JANUARY_LATIN;
import static org.daiitech.naftah.utils.time.Constants.JULY_AR;
import static org.daiitech.naftah.utils.time.Constants.JULY_LATIN;
import static org.daiitech.naftah.utils.time.Constants.JUMADA_AL_AWWAL;
import static org.daiitech.naftah.utils.time.Constants.JUMADA_AL_THANI;
import static org.daiitech.naftah.utils.time.Constants.JUMADA_AL_THANI_ALT;
import static org.daiitech.naftah.utils.time.Constants.JUNE_AR;
import static org.daiitech.naftah.utils.time.Constants.JUNE_LATIN;
import static org.daiitech.naftah.utils.time.Constants.MARCH;
import static org.daiitech.naftah.utils.time.Constants.MAY_AR;
import static org.daiitech.naftah.utils.time.Constants.MAY_LATIN;
import static org.daiitech.naftah.utils.time.Constants.MUHARRAM;
import static org.daiitech.naftah.utils.time.Constants.NOVEMBER;
import static org.daiitech.naftah.utils.time.Constants.OCTOBER;
import static org.daiitech.naftah.utils.time.Constants.OCTOBER_ALT;
import static org.daiitech.naftah.utils.time.Constants.RABI_AL_AWWAL;
import static org.daiitech.naftah.utils.time.Constants.RABI_AL_THANI;
import static org.daiitech.naftah.utils.time.Constants.RABI_AL_THANI_ALT;
import static org.daiitech.naftah.utils.time.Constants.RAJAB;
import static org.daiitech.naftah.utils.time.Constants.RAMADAN;
import static org.daiitech.naftah.utils.time.Constants.SAFAR;
import static org.daiitech.naftah.utils.time.Constants.SEPTEMBER;
import static org.daiitech.naftah.utils.time.Constants.SHAABAN;
import static org.daiitech.naftah.utils.time.Constants.SHAWAL;


/**
 * Utility class for converting Arabic month names to numeric month values.
 *
 * <p>This class supports both Gregorian and Hijri calendars.</p>
 *
 * <p>Methods include:
 * <ul>
 * <li>{@link #getGregorianMonth(String)} – maps Arabic or Latinized Gregorian month names to
 * * {@link java.time.Month}.</li>
 * <li>{@link #getHijriMonth(String)} – maps Arabic Hijri month names to integer month values (1–12).</li>
 * <li>{@link #arabicMonthToInt(String, java.time.chrono.Chronology)} – converts a month name to its numeric
 * * value based on the given chronology.</li>
 * </ul>
 * </p>
 *
 * <p>Throws {@link IllegalArgumentException} for unknown month names.</p>
 *
 * @author Chakib Daii
 */
public final class MonthUtils {
	/**
	 * Private constructor to prevent instantiation.
	 * Always throws a {@link NaftahBugError} when called.
	 */
	private MonthUtils() {
		throw newNaftahBugInvalidUsageError();
	}

	/**
	 * Converts a Gregorian month name (Arabic or Latinized) to a {@link java.time.Month} enum.
	 *
	 * @param monthName the month name in Arabic or Latinized form
	 * @return the corresponding {@link java.time.Month} enum
	 * @throws IllegalArgumentException if the month name is not recognized
	 */
	public static Month getGregorianMonth(String monthName) {
		return switch (monthName) {
			case JANUARY_AR, JANUARY_LATIN -> Month.JANUARY;
			case FEBRUARY_AR, FEBRUARY_LATIN -> Month.FEBRUARY;
			case MARCH -> Month.MARCH;
			case APRIL_AR, APRIL_LATIN -> Month.APRIL;
			case MAY_AR, MAY_LATIN -> Month.MAY;
			case JUNE_AR, JUNE_LATIN -> Month.JUNE;
			case JULY_AR, JULY_LATIN -> Month.JULY;
			case AUGUST_AR, AUGUST_LATIN -> Month.AUGUST;
			case SEPTEMBER -> Month.SEPTEMBER;
			case OCTOBER, OCTOBER_ALT -> Month.OCTOBER;
			case NOVEMBER -> Month.NOVEMBER;
			case DECEMBER_AR, DECEMBER_LATIN -> Month.DECEMBER;
			default -> throw new IllegalArgumentException();
		};
	}

	/**
	 * Returns the Latinized Arabic name of a Gregorian month given its numeric value.
	 *
	 * <p>The returned name corresponds to the commonly used Arabic/Latin variant
	 * (e.g., "جانفي", "فيفري", "أفريل").</p>
	 *
	 * @param month the Gregorian month number (1–12)
	 * @return the Latinized Arabic month name
	 * @throws IllegalArgumentException if the month number is not in the range 1–12
	 */
	public static String getGregorianMonthName(int month) {
		return switch (month) {
			case 1 -> JANUARY_LATIN;
			case 2 -> FEBRUARY_LATIN;
			case 3 -> MARCH;
			case 4 -> APRIL_LATIN;
			case 5 -> MAY_LATIN;
			case 6 -> JUNE_LATIN;
			case 7 -> JULY_LATIN;
			case 8 -> AUGUST_LATIN;
			case 9 -> SEPTEMBER;
			case 10 -> OCTOBER;
			case 11 -> NOVEMBER;
			case 12 -> DECEMBER_LATIN;
			default -> throw new IllegalArgumentException();
		};
	}

	/**
	 * Converts an Arabic Hijri month name to its corresponding numeric value (1–12).
	 *
	 * @param monthName the Arabic Hijri month name
	 * @return the month number (1 for Muharram, 12 for Dhu al-Hijjah)
	 * @throws IllegalArgumentException if the month name is not recognized
	 */
	public static int getHijriMonth(String monthName) {
		return switch (monthName) {
			case MUHARRAM -> 1;
			case SAFAR -> 2;
			case RABI_AL_AWWAL -> 3;
			case RABI_AL_THANI, RABI_AL_THANI_ALT -> 4;
			case JUMADA_AL_AWWAL -> 5;
			case JUMADA_AL_THANI, JUMADA_AL_THANI_ALT -> 6;
			case RAJAB -> 7;
			case SHAABAN -> 8;
			case RAMADAN -> 9;
			case SHAWAL -> 10;
			case DHU_AL_QIDAH -> 11;
			case DHU_AL_HIJJAH -> 12;
			default -> throw new IllegalArgumentException();
		};
	}

	/**
	 * Returns the Arabic Hijri month name corresponding to the given numeric value.
	 *
	 * <p>The mapping follows the standard Hijri calendar ordering
	 * (1 = Muharram, 12 = Dhu al-Hijjah).</p>
	 *
	 * @param month the Hijri month number (1–12)
	 * @return the Arabic Hijri month name
	 * @throws IllegalArgumentException if the month number is not in the range 1–12
	 */
	public static String getHijriMonthName(int month) {
		return switch (month) {
			case 1 -> MUHARRAM;
			case 2 -> SAFAR;
			case 3 -> RABI_AL_AWWAL;
			case 4 -> RABI_AL_THANI;
			case 5 -> JUMADA_AL_AWWAL;
			case 6 -> JUMADA_AL_THANI;
			case 7 -> RAJAB;
			case 8 -> SHAABAN;
			case 9 -> RAMADAN;
			case 10 -> SHAWAL;
			case 11 -> DHU_AL_QIDAH;
			case 12 -> DHU_AL_HIJJAH;
			default -> throw new IllegalArgumentException();
		};
	}

	/**
	 * Converts an Arabic month name to its numeric value according to the given chronology.
	 *
	 * <p>If the chronology is {@link java.time.chrono.HijrahChronology}, uses Hijri months;
	 * otherwise uses Gregorian months.</p>
	 *
	 * @param monthArabic the Arabic month name
	 * @param chronology  the chronology to determine the calendar type
	 * @return the numeric month value
	 * @throws IllegalArgumentException if the month name is not recognized
	 */
	public static int arabicMonthToInt(String monthArabic, Chronology chronology) {
		try {
			if (chronology.equals(HijrahChronology.INSTANCE)) {
				return getHijriMonth(monthArabic);
			}
			else {
				Month month = getGregorianMonth(monthArabic);
				return month.getValue();
			}
		}
		catch (Exception ex) {
			throw new IllegalArgumentException("اسم الشهر غير معروف: " + monthArabic, ex);
		}
	}

	/**
	 * Converts a numeric month value to its Arabic month name according to the given chronology.
	 *
	 * <p>If the chronology is {@link java.time.chrono.HijrahChronology}, the returned
	 * name is a Hijri month name. Otherwise, a Gregorian month name is returned.</p>
	 *
	 * @param month      the month number (1–12)
	 * @param chronology the chronology used to determine the calendar system
	 * @return the Arabic month name corresponding to the given month number
	 * @throws IllegalArgumentException if the month number is invalid or unsupported
	 */
	public static String monthNumberToArabicName(int month, Chronology chronology) {
		try {
			if (chronology.equals(HijrahChronology.INSTANCE)) {
				return getHijriMonthName(month);
			}
			else {
				return getGregorianMonthName(month);
			}
		}
		catch (Exception ex) {
			throw new IllegalArgumentException("اسم الشهر غير معروف: " + month, ex);
		}
	}

	/**
	 * Returns the number of days in a given month for the specified chronology and year.
	 *
	 * @param month      the month number (1–12)
	 * @param year       the year
	 * @param chronology the chronology (Gregorian or Hijri)
	 * @return the number of days in the month
	 * @throws IllegalArgumentException if month is not in 1–12
	 */
	public static int getMonthLength(int month, int year, Chronology chronology) {
		if (chronology.equals(HijrahChronology.INSTANCE)) {
			// Hijri months: odd=30, even=29, last month 30 in leap year
			return switch (month) {
				case 1, 3, 5, 7, 9, 11 -> 30;
				case 12 -> (chronology.isLeapYear(year) ? 30 : 29);
				case 2, 4, 6, 8, 10 -> 29;
				default -> throw new IllegalArgumentException("رقم الشهر الهجري غير صالح: " + month);
			};
		}
		else {
			// Gregorian months
			return switch (month) {
				case 2 -> (chronology.isLeapYear(year) ? 29 : 28);
				case 4, 6, 9, 11 -> 30;
				case 1, 3, 5, 7, 8, 10, 12 -> 31;
				default -> throw new IllegalArgumentException("رقم الشهر الميلادي غير صالح: " + month);
			};
		}
	}

}
