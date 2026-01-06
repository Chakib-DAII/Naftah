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
import static org.daiitech.naftah.utils.time.MonthUtils.monthNameToNumber;
import static org.daiitech.naftah.utils.time.MonthUtils.monthNumberToName;

/**
 * Represents an immutable Naftah date composed of:
 * <ul>
 * <li>A {@link Date} component (day, month, year)</li>
 * <li>An optional {@link Calendar} component</li>
 * <li>A resolved {@link Temporal} representation</li>
 * </ul>
 * <p>
 * This record is typically produced after parsing Naftah date expressions
 * and resolving them against a specific {@link Chronology}.
 *
 * <p>It supports both Gregorian and non-Gregorian calendars (e.g. Hijri)
 * and preserves the original Naftah month and calendar names.</p>
 *
 * @param date     the parsed date component
 * @param calendar the optional calendar specification
 * @param temporal the resolved temporal
 * @author Chakib Daii
 */
public record NaftahDate(
		Date date,
		Calendar calendar,
		Temporal temporal
) implements NaftahTemporalPoint, DateSupport {

	/**
	 * Obtains the current date using the default chronology
	 * and the system default time zone.
	 *
	 * <p>
	 * This method is equivalent to invoking:
	 * {@code now(ChronologyUtils.DEFAULT_CHRONOLOGY, null)}.
	 * </p>
	 *
	 * @return the current {@code NaftahDate}
	 */
	public static NaftahDate now() {
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
	 * @return the current {@code NaftahDate}
	 * @throws NullPointerException if {@code chronology} is {@code null}
	 */
	public static NaftahDate now(Chronology chronology) {
		var calendar = NaftahDate.Calendar.of(chronology);
		return now(calendar);
	}

	/**
	 * Obtains the current date using the specified calendar
	 * and the system default time zone.
	 *
	 * @param calendar the calendar to use (not {@code null})
	 * @return the current {@code NaftahDate}
	 * @throws NullPointerException if {@code calendar} is {@code null}
	 */
	public static NaftahDate now(Calendar calendar) {
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
	 * @return the current {@code NaftahDate}
	 */
	public static NaftahDate now(NaftahTime.ZoneOrOffset zoneOrOffset) {
		var calendar = NaftahDate.Calendar.of(ChronologyUtils.DEFAULT_CHRONOLOGY);
		return now(calendar, zoneOrOffset);
	}

	/**
	 * Obtains the current date using the specified chronology
	 * and zone or offset.
	 *
	 * @param chronology   the chronology to use (not {@code null})
	 * @param zoneOrOffset the zone or offset to use, or {@code null}
	 *                     to use the system default
	 * @return the current {@code NaftahDate}
	 * @throws NullPointerException if {@code chronology} is {@code null}
	 */
	public static NaftahDate now(Chronology chronology, NaftahTime.ZoneOrOffset zoneOrOffset) {
		var calendar = NaftahDate.Calendar.of(chronology);
		return now(calendar, zoneOrOffset);
	}

	/**
	 * Obtains the current date using the specified calendar
	 * and zone or offset.
	 *
	 * <p>
	 * The returned {@code NaftahDate} represents the current
	 * calendar date as resolved by the provided calendar and
	 * zone or offset.
	 * </p>
	 *
	 * @param calendar     the calendar to use (not {@code null})
	 * @param zoneOrOffset the zone or offset to use, or {@code null}
	 *                     to use the system default
	 * @return the current {@code NaftahDate}
	 * @throws NullPointerException if {@code calendar} is {@code null}
	 */
	public static NaftahDate now(Calendar calendar, NaftahTime.ZoneOrOffset zoneOrOffset) {
		return NaftahDate
				.of(calendar,
					TemporalUtils.currentDate(calendar, zoneOrOffset)
				);
	}

	/**
	 * Creates a new {@code NaftahDate} instance from its parsed components.
	 *
	 * <p>This factory method should be used when the {@code Date}, the {@link Calendar},
	 * and the underlying {@link Temporal} representation are already known.
	 * No additional parsing or chronology resolution is performed.</p>
	 *
	 * @param date     the parsed date component (day, month, year) according to the calendar
	 * @param calendar the calendar associated with this date (e.g., Gregorian or Hijri)
	 * @param temporal the resolved temporal representation backing this date
	 * @return a new {@code NaftahDate} instance combining all components
	 */
	public static NaftahDate of(Date date,
								Calendar calendar,
								Temporal temporal) {
		return new NaftahDate(date, calendar, temporal);
	}

	/**
	 * Creates a new {@code NaftahDate} instance from its parsed components
	 * and resolves the underlying {@link Temporal} representation automatically.
	 *
	 * <p>This factory method should be used when the {@code Date} and the
	 * associated {@link Calendar} are already known, but the backing
	 * {@link Temporal} has not yet been created. The temporal representation
	 * is derived using the calendar’s chronology.</p>
	 *
	 * @param date     the parsed date component (day, month, year) according to the calendar
	 * @param calendar the calendar associated with this date (e.g., Gregorian or Hijri)
	 * @return a new {@code NaftahDate} instance combining all components
	 */
	public static NaftahDate of(Date date,
								Calendar calendar) {
		return new NaftahDate(  date,
								calendar,
								TemporalUtils.createDate(date.day, date.monthValue, date.year, calendar.chronology));
	}

	/**
	 * Creates a new {@code NaftahDate} instance from a {@link Temporal} object and a calendar.
	 *
	 * <p>This method extracts the day, month, and year fields from the given temporal
	 * object and constructs an {@code NaftahDate} using the provided calendar's chronology.</p>
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
	 * @return a new {@code NaftahDate} instance derived from the temporal
	 * @throws IllegalArgumentException if the temporal does not support day, month, or year fields
	 */
	public static NaftahDate of(Calendar calendar, Temporal temporal) {
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
	 * Creates a new {@code NaftahDate} instance using a day, Naftah month name, and year.
	 *
	 * <p>This method uses the default chronology defined in {@link ChronologyUtils#DEFAULT_CHRONOLOGY}.</p>
	 *
	 * @param day   the day of the month
	 * @param month the month name (e.g., "رمضان", "يناير")
	 * @param year  the year value
	 * @return a new {@code NaftahDate} instance with the specified components
	 */
	public static NaftahDate of(int day, String month, int year) {
		var calendar = NaftahDate.Calendar.of(ChronologyUtils.DEFAULT_CHRONOLOGY);
		return of(calendar, day, month, year);
	}

	/**
	 * Creates a new {@code NaftahDate} instance using a specific chronology, day, month name, and year.
	 *
	 * <p>The chronology is used to resolve the month name to its numeric value and to determine leap years.</p>
	 *
	 * @param chronology the chronology to use (e.g., ISO or Hijri)
	 * @param day        the day of the month
	 * @param month      the month name
	 * @param year       the year value
	 * @return a new {@code NaftahDate} instance with the specified components
	 */
	public static NaftahDate of(Chronology chronology, int day, String month, int year) {
		var calendar = NaftahDate.Calendar.of(chronology);
		return of(calendar, day, month, year);
	}

	/**
	 * Creates a new {@code NaftahDate} instance using a calendar, day, month name, and year.
	 *
	 * <p>The month name is resolved to a numeric month using the provided calendar's chronology.</p>
	 *
	 * @param calendar the calendar to use
	 * @param day      the day of the month
	 * @param month    the month name
	 * @param year     the year value
	 * @return a new {@code NaftahDate} instance with the specified components
	 */
	public static NaftahDate of(Calendar calendar, int day, String month, int year) {
		var date = NaftahDate.Date.of(day, month, calendar.chronology(), year);
		var temporal = TemporalUtils.createDate(day, date.monthValue(), year, calendar.chronology());
		return of(date, calendar, temporal);
	}

	/**
	 * Creates a new {@code NaftahDate} instance using numeric day, month, and year values.
	 *
	 * <p>This method uses the default chronology defined in {@link ChronologyUtils#DEFAULT_CHRONOLOGY}.</p>
	 *
	 * @param day   the day of the month
	 * @param month the numeric month value (1–12)
	 * @param year  the year value
	 * @return a new {@code NaftahDate} instance with the specified components
	 */
	public static NaftahDate of(int day, int month, int year) {
		var calendar = NaftahDate.Calendar.of(ChronologyUtils.DEFAULT_CHRONOLOGY);
		return of(calendar, day, month, year);
	}

	/**
	 * Creates a new {@code NaftahDate} instance using a specific chronology, numeric day, month, and year.
	 *
	 * <p>The chronology is used for leap year calculations and month name resolution.</p>
	 *
	 * @param chronology the chronology to use (e.g., ISO or Hijri)
	 * @param day        the day of the month
	 * @param month      the numeric month value (1–12)
	 * @param year       the year value
	 * @return a new {@code NaftahDate} instance with the specified components
	 */
	public static NaftahDate of(Chronology chronology, int day, int month, int year) {
		var calendar = NaftahDate.Calendar.of(chronology);
		return of(calendar, day, month, year);
	}

	/**
	 * Creates a new {@code NaftahDate} instance using a calendar and numeric day, month, and year.
	 *
	 * <p>The calendar provides the chronology to resolve leap years and month names.</p>
	 *
	 * @param calendar the calendar to use
	 * @param day      the day of the month
	 * @param month    the numeric month value (1–12)
	 * @param year     the year value
	 * @return a new {@code NaftahDate} instance with the specified components
	 */
	public static NaftahDate of(Calendar calendar, int day, int month, int year) {
		var date = NaftahDate.Date.of(day, month, calendar.chronology(), year);
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
	 * Converts this {@code NaftahDate} to the number of days since the epoch
	 * (1970-01-01) in the ISO calendar system.
	 *
	 * @return the epoch day count
	 */
	@Override
	public long toEpochDay() {
		return toChronoDate().toEpochDay();
	}

	/**
	 * Returns the year component of this {@code NaftahDate}.
	 *
	 * @return the year value
	 */
	@Override
	public int getYear() {
		return date.year;
	}

	/**
	 * Returns the numeric month value (1–12) of this {@code NaftahDate}.
	 *
	 * @return the month value
	 */
	@Override
	public int getMonthValue() {
		return date.monthValue;
	}

	/**
	 * Returns the name of the month for this {@code NaftahDate}.
	 *
	 * @return the month name
	 */
	@Override
	public String getMonth() {
		return date.month;
	}

	/**
	 * Returns the day-of-month component of this {@code NaftahDate}.
	 *
	 * @return the day of the month
	 */
	@Override
	public int getDayOfMonth() {
		return date.day;
	}

	/**
	 * Returns the day-of-year for this {@code NaftahDate} according to its chronology.
	 *
	 * @return the day of the year (1–365 or 1–366 for leap years)
	 */
	@Override
	public int getDayOfYear() {
		return toChronoDate().get(ChronoField.DAY_OF_YEAR);
	}

	/**
	 * Returns the day of the week as a string for this {@code NaftahDate}.
	 *
	 * <p>The calculation is based on the epoch day of the underlying {@link ChronoLocalDate}.</p>
	 *
	 * @return the name of the day of the week
	 */
	@Override
	public String getDayOfWeek() {
		int dow0 = Math.floorMod(toChronoDate().toEpochDay() + 3, 7);
		return DayOfWeekUtils.getDayOfWeek(dow0 + 1);
	}

	/**
	 * Checks if the year of this {@code NaftahDate} is a leap year in its chronology.
	 *
	 * @return {@code true} if the year is a leap year, {@code false} otherwise
	 */
	@Override
	public boolean isLeapYear() {
		return calendar.chronology.isLeapYear(date.year());
	}

	/**
	 * Returns the length of the month in days for this {@code NaftahDate}.
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
	 * Returns the length of the year in days for this {@code NaftahDate}.
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
			return isLeapYear() ? 366 : 365;
		}
		else {
			return isLeapYear() ? 355 : 354;
		}
	}

	/**
	 * Returns a new {@code NaftahDate} obtained by adding the given Naftah temporal
	 * amount to this date.
	 *
	 * @param naftahTemporalAmount the temporal amount to add
	 * @return a new {@code NaftahDate} instance
	 */
	@Override
	public NaftahDate plus(NaftahTemporalAmount naftahTemporalAmount) {
		return compute(naftahTemporalAmount, this.temporal::plus);
	}

	/**
	 * Returns a new {@code NaftahDate} with the specified number of years added.
	 *
	 * @param yearsToAdd the number of years to add, may be negative
	 * @return a new {@code NaftahDate} instance
	 */
	@Override
	public NaftahDate plusYears(long yearsToAdd) {
		if (yearsToAdd == 0) {
			return this;
		}
		return of(calendar, temporal.plus(yearsToAdd, ChronoUnit.YEARS));
	}

	/**
	 * Returns a new {@code NaftahDate} with the specified number of months added.
	 *
	 * @param monthsToAdd the number of months to add, may be negative
	 * @return a new {@code NaftahDate} instance
	 */
	@Override
	public NaftahDate plusMonths(long monthsToAdd) {
		if (monthsToAdd == 0) {
			return this;
		}
		return of(calendar, temporal.plus(monthsToAdd, ChronoUnit.MONTHS));
	}

	/**
	 * Returns a new {@code NaftahDate} with the specified number of weeks added.
	 *
	 * @param weeksToAdd the number of weeks to add, may be negative
	 * @return a new {@code NaftahDate} instance
	 */
	@Override
	public NaftahDate plusWeeks(long weeksToAdd) {
		if (weeksToAdd == 0) {
			return this;
		}
		return of(calendar, temporal.plus(weeksToAdd, ChronoUnit.WEEKS));
	}

	/**
	 * Returns a new {@code NaftahDate} with the specified number of days added.
	 *
	 * @param daysToAdd the number of days to add, may be negative
	 * @return a new {@code NaftahDate} instance
	 */
	@Override
	public NaftahDate plusDays(long daysToAdd) {
		if (daysToAdd == 0) {
			return this;
		}
		return of(calendar, temporal.plus(daysToAdd, ChronoUnit.DAYS));
	}

	/**
	 * Returns a new {@code NaftahDate} obtained by subtracting the given Naftah temporal
	 * amount from this date.
	 *
	 * @param naftahTemporalAmount the temporal amount to subtract
	 * @return a new {@code NaftahDate} instance
	 */
	@Override
	public NaftahDate minus(NaftahTemporalAmount naftahTemporalAmount) {
		return compute(naftahTemporalAmount, this.temporal::minus);
	}

	/**
	 * Returns a new {@code NaftahDate} with the specified number of years subtracted.
	 *
	 * @param yearsToSubtract the number of years to subtract, may be negative
	 * @return a new {@code NaftahDate} instance
	 */
	@Override
	public NaftahDate minusYears(long yearsToSubtract) {
		if (yearsToSubtract == 0) {
			return this;
		}
		return of(calendar, temporal.minus(yearsToSubtract, ChronoUnit.YEARS));
	}

	/**
	 * Returns a new {@code NaftahDate} with the specified number of months subtracted.
	 *
	 * @param monthsToSubtract the number of months to subtract, may be negative
	 * @return a new {@code NaftahDate} instance
	 */
	@Override
	public NaftahDate minusMonths(long monthsToSubtract) {
		if (monthsToSubtract == 0) {
			return this;
		}
		return of(calendar, temporal.minus(monthsToSubtract, ChronoUnit.MONTHS));
	}

	/**
	 * Returns a new {@code NaftahDate} with the specified number of weeks subtracted.
	 *
	 * @param weeksToSubtract the number of weeks to subtract, may be negative
	 * @return a new {@code NaftahDate} instance
	 */
	@Override
	public NaftahDate minusWeeks(long weeksToSubtract) {
		if (weeksToSubtract == 0) {
			return this;
		}
		return of(calendar, temporal.minus(weeksToSubtract, ChronoUnit.WEEKS));
	}

	/**
	 * Returns a new {@code NaftahDate} with the specified number of days subtracted.
	 *
	 * @param daysToSubtract the number of days to subtract, may be negative
	 * @return a new {@code NaftahDate} instance
	 */
	@Override
	public NaftahDate minusDays(long daysToSubtract) {
		if (daysToSubtract == 0) {
			return this;
		}
		return of(calendar, temporal.minus(daysToSubtract, ChronoUnit.DAYS));
	}

	/**
	 * Computes a new {@code NaftahDate} by applying the given temporal computation.
	 *
	 * <p>
	 * Date-based arithmetic supports:
	 * <ul>
	 * <li>{@link NaftahPeriod}</li>
	 * <li>{@link NaftahDuration} of at least 24 hours</li>
	 * <li>{@link NaftahPeriodWithDuration}</li>
	 * </ul>
	 * </p>
	 *
	 * <p>
	 * Durations shorter than 24 hours are not permitted for date-only values.
	 * </p>
	 *
	 * @param arabicTemporalAmount the temporal amount to apply
	 * @param computeFunction      the temporal computation function
	 * @return a new {@code NaftahDate} instance
	 * @throws IllegalArgumentException if the operation is not supported
	 */
	NaftahDate compute( NaftahTemporalAmount arabicTemporalAmount,
						Function<TemporalAmount, Temporal> computeFunction) {
		if (arabicTemporalAmount instanceof NaftahDuration duration) {
			long hours = duration.temporalAmount().toHours();

			if (hours < 24) {
				throw new IllegalArgumentException(
													"لا يمكن إضافة مدة تقل عن 24 ساعة إلى قيمة زمنية فقط"
				);
			}

			return of(calendar, computeFunction.apply(duration.temporalAmount()));
		}
		else if (arabicTemporalAmount instanceof NaftahPeriodWithDuration naftahPeriodWithDuration) {
			return of(calendar, computeFunction.apply(naftahPeriodWithDuration.naftahPeriod().temporalAmount()));
		}
		else {
			return of(calendar, computeFunction.apply(arabicTemporalAmount.temporalAmount()));
		}
	}

	/**
	 * Returns a string representation of this {@link NaftahDate} in the format:
	 * "date calendar".
	 *
	 * <p>If a {@link Calendar} component is present, it is appended after the date.
	 * Otherwise, only the date is returned.</p>
	 *
	 * @return a formatted string representing the Naftah date and optional calendar
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
	 * Represents the date part of a Naftah date expression.
	 *
	 * <p>Stores both the original Naftah month name and its numeric value
	 * as resolved using a specific {@link Chronology}.</p>
	 *
	 * <p>Validation ensures the day, month, and month name are logically correct.</p>
	 *
	 * @param day        the day of month (starting from 1)
	 * @param month      the name of the month
	 * @param monthValue the numeric month value (1–12)
	 * @param year       the year value
	 */
	public record Date(int day, String month, int monthValue, int year) {
		public Date {
			if (day < 1) {
				throw new IllegalArgumentException("اليوم غير صحيح");
			}
			if (monthValue < 1 || monthValue > 12) {
				throw new IllegalArgumentException("رقم الشهر غير صحيح");
			}
			if (ObjectUtils.isEmpty(month)) {
				throw new IllegalArgumentException("اسم الشهر لا يمكن أن يكون فارغًا");
			}
		}

		/**
		 * Creates a {@code Date} instance by resolving a month name
		 * into its numeric value using the provided chronology.
		 *
		 * <p>The month name is interpreted according to the given chronology:
		 * Hijri chronologies resolve Hijri month names, while non-Hijri
		 * chronologies resolve Gregorian month names.</p>
		 *
		 * @param day        the day of the month
		 * @param month      the month name (e.g. "رمضان", "يناير")
		 * @param chronology the chronology used to resolve the month value
		 * @param year       the year value
		 * @return a new {@code Date} instance
		 * @throws IllegalArgumentException if the month name cannot be resolved for the given chronology
		 */
		public static Date of(int day, String month, Chronology chronology, int year) {
			return new Date(day, month, monthNameToNumber(month, chronology), year);
		}

		/**
		 * Creates a {@code Date} instance using a numeric month value.
		 *
		 * <p>The numeric month is converted back to its name according
		 * to the provided chronology. This ensures that the month name
		 * remains consistent with the calendar system.</p>
		 *
		 * @param day        the day of the month
		 * @param monthValue the numeric month value (1–12)
		 * @param chronology the chronology used to resolve the month name
		 * @param year       the year value
		 * @return a new {@code Date} instance
		 * @throws IllegalArgumentException if the month value cannot be resolved for the given chronology
		 */
		public static Date of(int day, int monthValue, Chronology chronology, int year) {
			return new Date(day, monthNumberToName(monthValue, chronology), monthValue, year);
		}

		/**
		 * Returns a string representation of this {@link Date}.
		 *
		 * <p>The format is:</p>
		 * <pre>
		 * day month year
		 * </pre>
		 *
		 * <p>Numeric values are rendered using the configured number formatter,
		 * and the month name is preserved exactly as stored.</p>
		 *
		 * @return a formatted string representing this date
		 */
		@Override
		public String toString() {
			return numberToString(day) + " " + month + " " + numberToString(year);
		}
	}

	/**
	 * Represents an Naftah calendar specification.
	 *
	 * <p>If no chronology or calendar name is provided, default values
	 * are applied automatically.</p>
	 *
	 * <p>This component is optional and may be omitted in date expressions.</p>
	 *
	 * @param calendar   the name of the calendar
	 * @param chronology the associated {@link Chronology}
	 */
	public record Calendar(String calendar, Chronology chronology) {

		public Calendar {
			chronology = Objects.nonNull(chronology) ? chronology : ChronologyUtils.DEFAULT_CHRONOLOGY;
			calendar = Objects.nonNull(calendar) ?
					calendar :
					ChronologyUtils.getChronologyName(chronology);
		}

		/**
		 * Creates a {@code Calendar} instance using the given name
		 * and chronology.
		 *
		 * @param calendar   the calendar name
		 * @param chronology the associated chronology
		 * @return a new {@code Calendar} instance
		 */
		public static Calendar of(String calendar, Chronology chronology) {
			return new Calendar(calendar, chronology);
		}

		/**
		 * Creates a {@code Calendar} instance using only a chronology.
		 * The calendar name will default automatically.
		 *
		 * @param chronology the associated chronology
		 * @return a new {@code Calendar} instance
		 */
		public static Calendar of(Chronology chronology) {
			return new Calendar(null, chronology);
		}

		/**
		 * Returns a string representation of this {@link Calendar} in the format:
		 * "Calendar: calendar".
		 *
		 * <p>Displays the name of the calendar. If no name was provided,
		 * the default calendar name is used.</p>
		 *
		 * @return a formatted string representing the calendar
		 */
		@Override
		public String toString() {
			return (calendar.contains(CALENDAR_PREFIX_AR.substring(3)) ?
					"" :
					CALENDAR_PREFIX_AR) + " " + calendar;
		}
	}
}
