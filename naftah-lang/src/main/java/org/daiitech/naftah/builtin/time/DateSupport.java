package org.daiitech.naftah.builtin.time;

/**
 * Provides arithmetic operations for {@code ArabicTemporalPoint} instances,
 * including addition and subtraction of years, months, weeks, and days.
 *
 * <p>This interface can be implemented by {@link ArabicDate} or
 * used by {@link ArabicDateTime} to delegate date arithmetic.</p>
 *
 * @author Chakib Daii
 */
public interface DateSupport {

	/**
	 * Converts this date to the number of days since the epoch (1970-01-01) in the ISO calendar.
	 *
	 * @return the epoch day count
	 */
	long toEpochDay();

	/**
	 * Returns the year component of this date.
	 *
	 * @return the year value
	 */
	int getYear();

	/**
	 * Returns the numeric month value (1–12) of this date.
	 *
	 * @return the month value
	 */
	int getMonthValue();

	/**
	 * Returns the Arabic name of the month for this date.
	 *
	 * @return the Arabic month name
	 */
	String getMonth();

	/**
	 * Returns the day-of-month component of this date.
	 *
	 * @return the day of the month
	 */
	int getDayOfMonth();

	/**
	 * Returns the day-of-year for this date according to its chronology.
	 *
	 * @return the day of the year (1–365 or 1–366 for leap years)
	 */
	int getDayOfYear();

	/**
	 * Returns the day of the week as an Arabic string for this date.
	 *
	 * <p>The calculation is based on the epoch day of the underlying {@link java.time.chrono.ChronoLocalDate}.</p>
	 *
	 * @return the Arabic name of the day of the week
	 */
	String getDayOfWeek();

	/**
	 * Checks if the year of this date is a leap year in its chronology.
	 *
	 * @return {@code true} if the year is a leap year, {@code false} otherwise
	 */
	boolean isLeapYear();

	/**
	 * Returns the length of the month in days for this date.
	 *
	 * <p>The length is determined according to the chronology of the calendar.</p>
	 *
	 * @return the number of days in the month
	 */
	int lengthOfMonth();

	/**
	 * Returns the length of the year in days for this date.
	 *
	 * <p>The number of days depends on the chronology of the date:
	 * <ul>
	 * <li>For Gregorian years (ISO chronology), leap years return 366 days,
	 * and non-leap years return 365 days.</li>
	 * <li>For Hijri years, leap years return 355 days, and non-leap years return 354 days.</li>
	 * </ul>
	 * </p>
	 *
	 * @return the number of days in the year according to its chronology
	 */
	int lengthOfYear();

	/**
	 * Returns a new date instance with the specified number of years added.
	 *
	 * @param yearsToAdd the number of years to add, may be negative
	 * @return a new {@code ArabicTemporalPoint} instance with years added
	 */
	ArabicTemporalPoint plusYears(long yearsToAdd);

	/**
	 * Returns a new date instance with the specified number of months added.
	 *
	 * @param monthsToAdd the number of months to add, may be negative
	 * @return a new {@code ArabicTemporalPoint} instance with months added
	 */
	ArabicTemporalPoint plusMonths(long monthsToAdd);

	/**
	 * Returns a new date instance with the specified number of weeks added.
	 *
	 * @param weeksToAdd the number of weeks to add, may be negative
	 * @return a new {@code ArabicTemporalPoint} instance with weeks added
	 */
	ArabicTemporalPoint plusWeeks(long weeksToAdd);

	/**
	 * Returns a new date instance with the specified number of days added.
	 *
	 * @param daysToAdd the number of days to add, may be negative
	 * @return a new {@code ArabicTemporalPoint} instance with days added
	 */
	ArabicTemporalPoint plusDays(long daysToAdd);

	/**
	 * Returns a new date instance with the specified number of years subtracted.
	 *
	 * @param yearsToSubtract the number of years to subtract, may be negative
	 * @return a new {@code ArabicTemporalPoint} instance with years subtracted
	 */
	ArabicTemporalPoint minusYears(long yearsToSubtract);

	/**
	 * Returns a new date instance with the specified number of months subtracted.
	 *
	 * @param monthsToSubtract the number of months to subtract, may be negative
	 * @return a new {@code ArabicTemporalPoint} instance with months subtracted
	 */
	ArabicTemporalPoint minusMonths(long monthsToSubtract);

	/**
	 * Returns a new date instance with the specified number of weeks subtracted.
	 *
	 * @param weeksToSubtract the number of weeks to subtract, may be negative
	 * @return a new {@code ArabicTemporalPoint} instance with weeks subtracted
	 */
	ArabicTemporalPoint minusWeeks(long weeksToSubtract);

	/**
	 * Returns a new date instance with the specified number of days subtracted.
	 *
	 * @param daysToSubtract the number of days to subtract, may be negative
	 * @return a new {@code ArabicTemporalPoint} instance with days subtracted
	 */
	ArabicTemporalPoint minusDays(long daysToSubtract);
}
