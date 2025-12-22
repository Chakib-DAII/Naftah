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
}
