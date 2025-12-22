package org.daiitech.naftah.builtin.time;

import java.time.chrono.Chronology;
import java.time.temporal.TemporalAccessor;
import java.util.Objects;

import org.daiitech.naftah.builtin.utils.ObjectUtils;
import org.daiitech.naftah.utils.time.ChronologyUtils;

import static org.daiitech.naftah.utils.time.Constants.DEFAULT_CALENDAR_NAME_1;
import static org.daiitech.naftah.utils.time.MonthUtils.arabicMonthToInt;

/**
 * Represents an immutable Arabic date composed of:
 * <ul>
 * <li>A {@link Date} component (day, month, year)</li>
 * <li>An optional {@link Calendar} component</li>
 * <li>A resolved {@link TemporalAccessor} representation</li>
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
 * @param temporal the resolved temporal accessor
 * @author Chakib Daii
 */
public record ArabicDate(
		Date date,
		Calendar calendar,
		TemporalAccessor temporal
) implements ArabicTemporal {

	/**
	 * Creates a new {@code ArabicDate} instance.
	 *
	 * @param date     the date component
	 * @param calendar the calendar component
	 * @param temporal the resolved temporal accessor
	 * @return a new {@code ArabicDate} instance
	 */
	public static ArabicDate of(Date date,
								Calendar calendar,
								TemporalAccessor temporal) {
		return new ArabicDate(date, calendar, temporal);
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
		 * Creates a {@code Date} instance by resolving the Arabic month name
		 * into its numeric value using the provided chronology.
		 *
		 * @param day         the day of month
		 * @param arabicMonth the Arabic month name
		 * @param chronology  the chronology used to resolve the month
		 * @param year        the year value
		 * @return a new {@code Date} instance
		 * @throws IllegalArgumentException if the month cannot be resolved
		 */
		public static Date of(int day, String arabicMonth, Chronology chronology, int year) {
			return new Date(day, arabicMonth, arabicMonthToInt(arabicMonth, chronology), year);
		}

		/**
		 * Returns a string representation of this {@link Date} in the format:
		 * "day arabicMonth year".
		 *
		 * <p>The Arabic month name is preserved as originally provided.</p>
		 *
		 * @return a formatted string representing the day, month, and year
		 */
		@Override
		public String toString() {
			return day + " " + arabicMonth + " " + year;
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
			arabicCalendar = Objects.nonNull(arabicCalendar) ? arabicCalendar : DEFAULT_CALENDAR_NAME_1;
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
			return "التقويم: " + arabicCalendar;
		}
	}
}
