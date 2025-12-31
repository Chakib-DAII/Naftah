package org.daiitech.naftah.builtin.time;

import java.time.chrono.ChronoLocalDate;
import java.time.chrono.Chronology;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.Objects;
import java.util.function.Function;

import org.daiitech.naftah.builtin.utils.ObjectUtils;
import org.daiitech.naftah.utils.time.ChronoConversionUtils;
import org.daiitech.naftah.utils.time.ChronologyUtils;
import org.daiitech.naftah.utils.time.DayOfWeekUtils;
import org.daiitech.naftah.utils.time.MonthUtils;
import org.daiitech.naftah.utils.time.TemporalUtils;

import static org.daiitech.naftah.builtin.utils.ObjectUtils.numberToString;
import static org.daiitech.naftah.utils.time.Constants.CALENDAR_PREFIX_AR;
import static org.daiitech.naftah.utils.time.MonthUtils.arabicMonthToInt;
import static org.daiitech.naftah.utils.time.MonthUtils.monthNumberToArabicName;

/**
 * Represents an immutable Arabic date composed of:
 * <ul>
 * <li>A {@link Date} component (day, month, year)</li>
 * <li>An optional {@link Calendar} component</li>
 * <li>A resolved {@link Temporal} representation</li>
 * </ul>
 * <p>
 * This record is typically produced after parsing Arabic date expressions
 * and resolving them against a specific {@link Chronology}.
 *
 * <p>It supports both Gregorian and non-Gregorian calendars (e.g. Hijri)
 * and preserves the original Arabic month and calendar names.</p>
 *
 * @param date     the parsed date component
 * @param calendar the optional calendar specification
 * @param temporal the resolved temporal
 * @author Chakib Daii
 */
public record ArabicDate(
		Date date,
		Calendar calendar,
		Temporal temporal
) implements ArabicTemporalPoint, DateSupport {

	/**
	 * Obtains the current date using the default chronology
	 * and the system default time zone.
	 *
	 * <p>
	 * This method is equivalent to invoking:
	 * {@code now(ChronologyUtils.DEFAULT_CHRONOLOGY, null)}.
	 * </p>
	 *
	 * @return the current {@code ArabicDate}
	 */
	public static ArabicDate now() {
		return now(ChronologyUtils.DEFAULT_CHRONOLOGY);
	}

	/**
	 * Obtains the current date using the specified chronology
	 * and the system default time zone.
	 *
	 * <p>
	 * The supplied chronology determines how the current
	 * day, month, and year are interpreted.
	 * </p>
	 *
	 * @param chronology the chronology to use (not {@code null})
	 * @return the current {@code ArabicDate}
	 * @throws NullPointerException if {@code chronology} is {@code null}
	 */
	public static ArabicDate now(Chronology chronology) {
		var calendar = ArabicDate.Calendar.of(chronology);
		return now(calendar);
	}

	/**
	 * Obtains the current date using the specified calendar
	 * and the system default time zone.
	 *
	 * @param calendar the calendar to use (not {@code null})
	 * @return the current {@code ArabicDate}
	 * @throws NullPointerException if {@code calendar} is {@code null}
	 */
	public static ArabicDate now(Calendar calendar) {
		return now(calendar, null);
	}

	/**
	 * Obtains the current date using the default chronology
	 * and the specified zone or offset.
	 *
	 * <p>
	 * This method is useful when resolving the current date
	 * relative to a specific time zone or fixed offset.
	 * </p>
	 *
	 * @param zoneOrOffset the zone or offset to use, or {@code null}
	 *                     to use the system default
	 * @return the current {@code ArabicDate}
	 */
	public static ArabicDate now(ArabicTime.ZoneOrOffset zoneOrOffset) {
		var calendar = ArabicDate.Calendar.of(ChronologyUtils.DEFAULT_CHRONOLOGY);
		return now(calendar, zoneOrOffset);
	}

	/**
	 * Obtains the current date using the specified chronology
	 * and zone or offset.
	 *
	 * @param chronology   the chronology to use (not {@code null})
	 * @param zoneOrOffset the zone or offset to use, or {@code null}
	 *                     to use the system default
	 * @return the current {@code ArabicDate}
	 * @throws NullPointerException if {@code chronology} is {@code null}
	 */
	public static ArabicDate now(Chronology chronology, ArabicTime.ZoneOrOffset zoneOrOffset) {
		var calendar = ArabicDate.Calendar.of(chronology);
		return now(calendar, zoneOrOffset);
	}

	/**
	 * Obtains the current date using the specified calendar
	 * and zone or offset.
	 *
	 * <p>
	 * The returned {@code ArabicDate} represents the current
	 * calendar date as resolved by the provided calendar and
	 * zone or offset.
	 * </p>
	 *
	 * @param calendar     the calendar to use (not {@code null})
	 * @param zoneOrOffset the zone or offset to use, or {@code null}
	 *                     to use the system default
	 * @return the current {@code ArabicDate}
	 * @throws NullPointerException if {@code calendar} is {@code null}
	 */
	public static ArabicDate now(Calendar calendar, ArabicTime.ZoneOrOffset zoneOrOffset) {
		return ArabicDate
				.of(calendar,
					TemporalUtils.currentDate(calendar, zoneOrOffset)
				);
	}

	/**
	 * Creates a new {@code ArabicDate} instance from its parsed components.
	 *
	 * <p>This factory method should be used when the {@code Date}, the {@link Calendar},
	 * and the underlying {@link Temporal} representation are already known.
	 * No additional parsing or chronology resolution is performed.</p>
	 *
	 * @param date     the parsed date component (day, month, year) according to the calendar
	 * @param calendar the calendar associated with this date (e.g., Gregorian or Hijri)
	 * @param temporal the resolved temporal representation backing this date
	 * @return a new {@code ArabicDate} instance combining all components
	 */
	public static ArabicDate of(Date date,
								Calendar calendar,
								Temporal temporal) {
		return new ArabicDate(date, calendar, temporal);
	}

	/**
	 * Creates a new {@code ArabicDate} instance from its parsed components
	 * and resolves the underlying {@link Temporal} representation automatically.
	 *
	 * <p>This factory method should be used when the {@code Date} and the
	 * associated {@link Calendar} are already known, but the backing
	 * {@link Temporal} has not yet been created. The temporal representation
	 * is derived using the calendar’s chronology.</p>
	 *
	 * @param date     the parsed date component (day, month, year) according to the calendar
	 * @param calendar the calendar associated with this date (e.g., Gregorian or Hijri)
	 * @return a new {@code ArabicDate} instance combining all components
	 */
	public static ArabicDate of(Date date,
								Calendar calendar) {
		return new ArabicDate(  date,
								calendar,
								TemporalUtils.createDate(date.day, date.monthValue, date.year, calendar.chronology));
	}

	/**
	 * Creates a new {@code ArabicDate} instance from a {@link Temporal} object and a calendar.
	 *
	 * <p>This method extracts the day, month, and year fields from the given temporal
	 * object and constructs an {@code ArabicDate} using the provided calendar's chronology.</p>
	 *
	 * <p>The temporal must support the following fields:
	 * <ul>
	 * <li>{@link ChronoField#DAY_OF_MONTH}</li>
	 * <li>{@link ChronoField#MONTH_OF_YEAR}</li>
	 * <li>{@link ChronoField#YEAR}</li>
	 * </ul>
	 * </p>
	 *
	 * @param calendar the calendar that determines the chronology of the date
	 * @param temporal the temporal object containing date information
	 * @return a new {@code ArabicDate} instance derived from the temporal
	 * @throws IllegalArgumentException if the temporal does not support day, month, or year fields
	 */
	public static ArabicDate of(Calendar calendar, Temporal temporal) {
		if (!temporal.isSupported(ChronoField.DAY_OF_MONTH) || !temporal
				.isSupported(ChronoField.MONTH_OF_YEAR) || !temporal.isSupported(ChronoField.YEAR)) {
			throw new IllegalArgumentException(
												"النقطة الزمنية المقدم لا يدعم اليوم أو الشهر أو السنة: " + temporal
			);
		}

		ChronoLocalDate chronoLocalDate = ChronoConversionUtils.toChronoDate(temporal, calendar.chronology);

		int day = chronoLocalDate.get(ChronoField.DAY_OF_MONTH);
		int month = chronoLocalDate.get(ChronoField.MONTH_OF_YEAR);
		int year = chronoLocalDate.get(ChronoField.YEAR);

		return of(calendar, day, month, year);
	}

	/**
	 * Creates a new {@code ArabicDate} instance using a day, Arabic month name, and year.
	 *
	 * <p>This method uses the default chronology defined in {@link ChronologyUtils#DEFAULT_CHRONOLOGY}.</p>
	 *
	 * @param day         the day of the month
	 * @param arabicMonth the Arabic month name (e.g., "رمضان", "يناير")
	 * @param year        the year value
	 * @return a new {@code ArabicDate} instance with the specified components
	 */
	public static ArabicDate of(int day, String arabicMonth, int year) {
		var calendar = ArabicDate.Calendar.of(ChronologyUtils.DEFAULT_CHRONOLOGY);
		return of(calendar, day, arabicMonth, year);
	}

	/**
	 * Creates a new {@code ArabicDate} instance using a specific chronology, day, Arabic month name, and year.
	 *
	 * <p>The chronology is used to resolve the month name to its numeric value and to determine leap years.</p>
	 *
	 * @param chronology  the chronology to use (e.g., ISO or Hijri)
	 * @param day         the day of the month
	 * @param arabicMonth the Arabic month name
	 * @param year        the year value
	 * @return a new {@code ArabicDate} instance with the specified components
	 */
	public static ArabicDate of(Chronology chronology, int day, String arabicMonth, int year) {
		var calendar = ArabicDate.Calendar.of(chronology);
		return of(calendar, day, arabicMonth, year);
	}

	/**
	 * Creates a new {@code ArabicDate} instance using a calendar, day, Arabic month name, and year.
	 *
	 * <p>The Arabic month name is resolved to a numeric month using the provided calendar's chronology.</p>
	 *
	 * @param calendar    the calendar to use
	 * @param day         the day of the month
	 * @param arabicMonth the Arabic month name
	 * @param year        the year value
	 * @return a new {@code ArabicDate} instance with the specified components
	 */
	public static ArabicDate of(Calendar calendar, int day, String arabicMonth, int year) {
		var date = ArabicDate.Date.of(day, arabicMonth, calendar.chronology(), year);
		var temporal = TemporalUtils.createDate(day, date.monthValue(), year, calendar.chronology());
		return of(date, calendar, temporal);
	}

	/**
	 * Creates a new {@code ArabicDate} instance using numeric day, month, and year values.
	 *
	 * <p>This method uses the default chronology defined in {@link ChronologyUtils#DEFAULT_CHRONOLOGY}.</p>
	 *
	 * @param day   the day of the month
	 * @param month the numeric month value (1–12)
	 * @param year  the year value
	 * @return a new {@code ArabicDate} instance with the specified components
	 */
	public static ArabicDate of(int day, int month, int year) {
		var calendar = ArabicDate.Calendar.of(ChronologyUtils.DEFAULT_CHRONOLOGY);
		return of(calendar, day, month, year);
	}

	/**
	 * Creates a new {@code ArabicDate} instance using a specific chronology, numeric day, month, and year.
	 *
	 * <p>The chronology is used for leap year calculations and month name resolution.</p>
	 *
	 * @param chronology the chronology to use (e.g., ISO or Hijri)
	 * @param day        the day of the month
	 * @param month      the numeric month value (1–12)
	 * @param year       the year value
	 * @return a new {@code ArabicDate} instance with the specified components
	 */
	public static ArabicDate of(Chronology chronology, int day, int month, int year) {
		var calendar = ArabicDate.Calendar.of(chronology);
		return of(calendar, day, month, year);
	}

	/**
	 * Creates a new {@code ArabicDate} instance using a calendar and numeric day, month, and year.
	 *
	 * <p>The calendar provides the chronology to resolve leap years and month names.</p>
	 *
	 * @param calendar the calendar to use
	 * @param day      the day of the month
	 * @param month    the numeric month value (1–12)
	 * @param year     the year value
	 * @return a new {@code ArabicDate} instance with the specified components
	 */
	public static ArabicDate of(Calendar calendar, int day, int month, int year) {
		var date = ArabicDate.Date.of(day, month, calendar.chronology(), year);
		var temporal = TemporalUtils.createDate(day, date.monthValue(), year, calendar.chronology());
		return of(date, calendar, temporal);
	}

	/**
	 * Returns this date interpreted in its calendar chronology.
	 *
	 * <p>The returned {@link ChronoLocalDate} is derived from the underlying
	 * ISO temporal and the associated {@link Chronology}.</p>
	 *
	 * <p>This method must be used whenever calendar-specific fields
	 * (year, month, day) are required.</p>
	 *
	 * @return a {@link ChronoLocalDate} in the calendar's chronology
	 */
	public ChronoLocalDate toChronoDate() {
		return ChronoConversionUtils.toChronoDate(temporal, calendar.chronology);
	}

	/**
	 * Converts this {@code ArabicDate} to the number of days since the epoch
	 * (1970-01-01) in the ISO calendar system.
	 *
	 * @return the epoch day count
	 */
	@Override
	public long toEpochDay() {
		return toChronoDate().toEpochDay();
	}

	/**
	 * Returns the year component of this {@code ArabicDate}.
	 *
	 * @return the year value
	 */
	@Override
	public int getYear() {
		return date.year;
	}

	/**
	 * Returns the numeric month value (1–12) of this {@code ArabicDate}.
	 *
	 * @return the month value
	 */
	@Override
	public int getMonthValue() {
		return date.monthValue;
	}

	/**
	 * Returns the Arabic name of the month for this {@code ArabicDate}.
	 *
	 * @return the Arabic month name
	 */
	@Override
	public String getMonth() {
		return date.arabicMonth;
	}

	/**
	 * Returns the day-of-month component of this {@code ArabicDate}.
	 *
	 * @return the day of the month
	 */
	@Override
	public int getDayOfMonth() {
		return date.day;
	}

	/**
	 * Returns the day-of-year for this {@code ArabicDate} according to its chronology.
	 *
	 * @return the day of the year (1–365 or 1–366 for leap years)
	 */
	@Override
	public int getDayOfYear() {
		return toChronoDate().get(ChronoField.DAY_OF_YEAR);
	}

	/**
	 * Returns the day of the week as an Arabic string for this {@code ArabicDate}.
	 *
	 * <p>The calculation is based on the epoch day of the underlying {@link ChronoLocalDate}.</p>
	 *
	 * @return the Arabic name of the day of the week
	 */
	@Override
	public String getDayOfWeek() {
		int dow0 = Math.floorMod(toChronoDate().toEpochDay() + 3, 7);
		return DayOfWeekUtils.getArabicDayOfWeek(dow0 + 1);
	}

	/**
	 * Checks if the year of this {@code ArabicDate} is a leap year in its chronology.
	 *
	 * @return {@code true} if the year is a leap year, {@code false} otherwise
	 */
	@Override
	public boolean isLeapYear() {
		return calendar.chronology.isLeapYear(date.year());
	}

	/**
	 * Returns the length of the month in days for this {@code ArabicDate}.
	 *
	 * <p>The length is determined according to the chronology of the calendar.</p>
	 *
	 * @return the number of days in the month
	 */
	@Override
	public int lengthOfMonth() {
		return MonthUtils.getMonthLength(date.monthValue, date.year, calendar.chronology);
	}

	/**
	 * Returns the length of the year in days for this {@code ArabicDate}.
	 *
	 * <p>
	 * The number of days depends on the chronology of the date:
	 * <ul>
	 * <li>For Gregorian years (ISO chronology), leap years return 366 days,
	 * and non-leap years return 365 days.</li>
	 * <li>For Hijri years, leap years return 355 days, and non-leap years return 354 days.</li>
	 * </ul>
	 * </p>
	 *
	 * @return the number of days in the year according to its chronology
	 */
	@Override
	public int lengthOfYear() {
		if (calendar.chronology.equals(ChronologyUtils.DEFAULT_CHRONOLOGY)) {
			return isLeapYear() ? 355 : 354;
		}
		else {
			return isLeapYear() ? 366 : 365;
		}
	}

	/**
	 * Returns a new {@code ArabicDate} obtained by adding the given Arabic temporal
	 * amount to this date.
	 *
	 * @param arabicTemporalAmount the temporal amount to add
	 * @return a new {@code ArabicDate} instance
	 */
	@Override
	public ArabicDate plus(ArabicTemporalAmount arabicTemporalAmount) {
		return compute(arabicTemporalAmount, this.temporal::plus);
	}

	/**
	 * Returns a new {@code ArabicDate} with the specified number of years added.
	 *
	 * @param yearsToAdd the number of years to add, may be negative
	 * @return a new {@code ArabicDate} instance
	 */
	@Override
	public ArabicDate plusYears(long yearsToAdd) {
		if (yearsToAdd == 0) {
			return this;
		}
		return of(calendar, temporal.plus(yearsToAdd, ChronoUnit.YEARS));
	}

	/**
	 * Returns a new {@code ArabicDate} with the specified number of months added.
	 *
	 * @param monthsToAdd the number of months to add, may be negative
	 * @return a new {@code ArabicDate} instance
	 */
	@Override
	public ArabicDate plusMonths(long monthsToAdd) {
		if (monthsToAdd == 0) {
			return this;
		}
		return of(calendar, temporal.plus(monthsToAdd, ChronoUnit.MONTHS));
	}

	/**
	 * Returns a new {@code ArabicDate} with the specified number of weeks added.
	 *
	 * @param weeksToAdd the number of weeks to add, may be negative
	 * @return a new {@code ArabicDate} instance
	 */
	@Override
	public ArabicDate plusWeeks(long weeksToAdd) {
		if (weeksToAdd == 0) {
			return this;
		}
		return of(calendar, temporal.plus(weeksToAdd, ChronoUnit.WEEKS));
	}

	/**
	 * Returns a new {@code ArabicDate} with the specified number of days added.
	 *
	 * @param daysToAdd the number of days to add, may be negative
	 * @return a new {@code ArabicDate} instance
	 */
	@Override
	public ArabicDate plusDays(long daysToAdd) {
		if (daysToAdd == 0) {
			return this;
		}
		return of(calendar, temporal.plus(daysToAdd, ChronoUnit.DAYS));
	}

	/**
	 * Returns a new {@code ArabicDate} obtained by subtracting the given Arabic temporal
	 * amount from this date.
	 *
	 * @param arabicTemporalAmount the temporal amount to subtract
	 * @return a new {@code ArabicDate} instance
	 */
	@Override
	public ArabicDate minus(ArabicTemporalAmount arabicTemporalAmount) {
		return compute(arabicTemporalAmount, this.temporal::minus);
	}

	/**
	 * Returns a new {@code ArabicDate} with the specified number of years subtracted.
	 *
	 * @param yearsToSubtract the number of years to subtract, may be negative
	 * @return a new {@code ArabicDate} instance
	 */
	@Override
	public ArabicDate minusYears(long yearsToSubtract) {
		if (yearsToSubtract == 0) {
			return this;
		}
		return of(calendar, temporal.minus(yearsToSubtract, ChronoUnit.YEARS));
	}

	/**
	 * Returns a new {@code ArabicDate} with the specified number of months subtracted.
	 *
	 * @param monthsToSubtract the number of months to subtract, may be negative
	 * @return a new {@code ArabicDate} instance
	 */
	@Override
	public ArabicDate minusMonths(long monthsToSubtract) {
		if (monthsToSubtract == 0) {
			return this;
		}
		return of(calendar, temporal.minus(monthsToSubtract, ChronoUnit.MONTHS));
	}

	/**
	 * Returns a new {@code ArabicDate} with the specified number of weeks subtracted.
	 *
	 * @param weeksToSubtract the number of weeks to subtract, may be negative
	 * @return a new {@code ArabicDate} instance
	 */
	@Override
	public ArabicDate minusWeeks(long weeksToSubtract) {
		if (weeksToSubtract == 0) {
			return this;
		}
		return of(calendar, temporal.minus(weeksToSubtract, ChronoUnit.WEEKS));
	}

	/**
	 * Returns a new {@code ArabicDate} with the specified number of days subtracted.
	 *
	 * @param daysToSubtract the number of days to subtract, may be negative
	 * @return a new {@code ArabicDate} instance
	 */
	@Override
	public ArabicDate minusDays(long daysToSubtract) {
		if (daysToSubtract == 0) {
			return this;
		}
		return of(calendar, temporal.minus(daysToSubtract, ChronoUnit.DAYS));
	}

	/**
	 * Computes a new {@code ArabicDate} by applying the given temporal computation.
	 *
	 * <p>
	 * Date-based arithmetic supports:
	 * <ul>
	 * <li>{@link ArabicPeriod}</li>
	 * <li>{@link ArabicDuration} of at least 24 hours</li>
	 * <li>{@link ArabicPeriodWithDuration}</li>
	 * </ul>
	 * </p>
	 *
	 * <p>
	 * Durations shorter than 24 hours are not permitted for date-only values.
	 * </p>
	 *
	 * @param arabicTemporalAmount the temporal amount to apply
	 * @param computeFunction      the temporal computation function
	 * @return a new {@code ArabicDate} instance
	 * @throws IllegalArgumentException if the operation is not supported
	 */
	ArabicDate compute( ArabicTemporalAmount arabicTemporalAmount,
						Function<TemporalAmount, Temporal> computeFunction) {
		if (arabicTemporalAmount instanceof ArabicDuration duration) {
			long hours = duration.temporalAmount().toHours();

			if (hours < 24) {
				throw new IllegalArgumentException(
													"لا يمكن إضافة مدة تقل عن 24 ساعة إلى قيمة زمنية فقط"
				);
			}

			return of(calendar, computeFunction.apply(duration.temporalAmount()));
		}
		else if (arabicTemporalAmount instanceof ArabicPeriodWithDuration arabicPeriodWithDuration) {
			return of(calendar, computeFunction.apply(arabicPeriodWithDuration.arabicPeriod().temporalAmount()));
		}
		else {
			return of(calendar, computeFunction.apply(arabicTemporalAmount.temporalAmount()));
		}
	}

	/**
	 * Returns a string representation of this {@link ArabicDate} in the format:
	 * "date calendar".
	 *
	 * <p>If a {@link Calendar} component is present, it is appended after the date.
	 * Otherwise, only the date is returned.</p>
	 *
	 * @return a formatted string representing the Arabic date and optional calendar
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(date);

		if (Objects.nonNull(calendar)) {
			sb.append(" ").append(calendar);
		}

		return sb.toString().trim();
	}

	/**
	 * Represents the date part of an Arabic date expression.
	 *
	 * <p>Stores both the original Arabic month name and its numeric value
	 * as resolved using a specific {@link Chronology}.</p>
	 *
	 * <p>Validation ensures the day, month, and month name are logically correct.</p>
	 *
	 * @param day         the day of month (starting from 1)
	 * @param arabicMonth the Arabic name of the month
	 * @param monthValue  the numeric month value (1–12)
	 * @param year        the year value
	 */
	public record Date(int day, String arabicMonth, int monthValue, int year) {
		public Date {
			if (day < 1) {
				throw new IllegalArgumentException("اليوم غير صحيح");
			}
			if (monthValue < 1 || monthValue > 12) {
				throw new IllegalArgumentException("رقم الشهر غير صحيح");
			}
			if (ObjectUtils.isEmpty(arabicMonth)) {
				throw new IllegalArgumentException("اسم الشهر لا يمكن أن يكون فارغًا");
			}
		}

		/**
		 * Creates a {@code Date} instance by resolving an Arabic month name
		 * into its numeric value using the provided chronology.
		 *
		 * <p>The month name is interpreted according to the given chronology:
		 * Hijri chronologies resolve Hijri month names, while non-Hijri
		 * chronologies resolve Gregorian month names.</p>
		 *
		 * @param day         the day of the month
		 * @param arabicMonth the Arabic month name (e.g. "رمضان", "يناير")
		 * @param chronology  the chronology used to resolve the month value
		 * @param year        the year value
		 * @return a new {@code Date} instance
		 * @throws IllegalArgumentException if the month name cannot be resolved for the given chronology
		 */
		public static Date of(int day, String arabicMonth, Chronology chronology, int year) {
			return new Date(day, arabicMonth, arabicMonthToInt(arabicMonth, chronology), year);
		}

		/**
		 * Creates a {@code Date} instance using a numeric month value.
		 *
		 * <p>The numeric month is converted back to its Arabic name according
		 * to the provided chronology. This ensures that the Arabic month name
		 * remains consistent with the calendar system.</p>
		 *
		 * @param day        the day of the month
		 * @param monthValue the numeric month value (1–12)
		 * @param chronology the chronology used to resolve the Arabic month name
		 * @param year       the year value
		 * @return a new {@code Date} instance
		 * @throws IllegalArgumentException if the month value cannot be resolved for the given chronology
		 */
		public static Date of(int day, int monthValue, Chronology chronology, int year) {
			return new Date(day, monthNumberToArabicName(monthValue, chronology), monthValue, year);
		}

		/**
		 * Returns a string representation of this {@link Date}.
		 *
		 * <p>The format is:</p>
		 * <pre>
		 * day arabicMonth year
		 * </pre>
		 *
		 * <p>Numeric values are rendered using the configured number formatter,
		 * and the Arabic month name is preserved exactly as stored.</p>
		 *
		 * @return a formatted string representing this date
		 */
		@Override
		public String toString() {
			return numberToString(day) + " " + arabicMonth + " " + numberToString(year);
		}
	}

	/**
	 * Represents an Arabic calendar specification.
	 *
	 * <p>If no chronology or calendar name is provided, default values
	 * are applied automatically.</p>
	 *
	 * <p>This component is optional and may be omitted in date expressions.</p>
	 *
	 * @param arabicCalendar the Arabic name of the calendar
	 * @param chronology     the associated {@link Chronology}
	 */
	public record Calendar(String arabicCalendar, Chronology chronology) {

		public Calendar {
			chronology = Objects.nonNull(chronology) ? chronology : ChronologyUtils.DEFAULT_CHRONOLOGY;
			arabicCalendar = Objects.nonNull(arabicCalendar) ?
					arabicCalendar :
					ChronologyUtils.getChronologyName(chronology);
		}

		/**
		 * Creates a {@code Calendar} instance using the given Arabic name
		 * and chronology.
		 *
		 * @param arabicCalendar the Arabic calendar name
		 * @param chronology     the associated chronology
		 * @return a new {@code Calendar} instance
		 */
		public static Calendar of(String arabicCalendar, Chronology chronology) {
			return new Calendar(arabicCalendar, chronology);
		}

		/**
		 * Creates a {@code Calendar} instance using only a chronology.
		 * The Arabic calendar name will default automatically.
		 *
		 * @param chronology the associated chronology
		 * @return a new {@code Calendar} instance
		 */
		public static Calendar of(Chronology chronology) {
			return new Calendar(null, chronology);
		}

		/**
		 * Returns a string representation of this {@link Calendar} in the format:
		 * "Calendar: arabicCalendar".
		 *
		 * <p>Displays the Arabic name of the calendar. If no name was provided,
		 * the default calendar name is used.</p>
		 *
		 * @return a formatted string representing the calendar
		 */
		@Override
		public String toString() {
			return (arabicCalendar.contains(CALENDAR_PREFIX_AR.substring(3)) ?
					"" :
					CALENDAR_PREFIX_AR) + " " + arabicCalendar;
		}
	}
}
