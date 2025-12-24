package org.daiitech.naftah.builtin.time;

import java.time.chrono.Chronology;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.util.Objects;

import org.daiitech.naftah.builtin.utils.ObjectUtils;
import org.daiitech.naftah.utils.time.ChronologyUtils;

import static org.daiitech.naftah.builtin.utils.ObjectUtils.numberToString;
import static org.daiitech.naftah.utils.time.Constants.CALENDAR_PREFIX_AR;
import static org.daiitech.naftah.utils.time.Constants.DEFAULT_CALENDAR_NAME_1;
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
) implements ArabicTemporalPoint {

	/**
	 * Creates a new {@code ArabicDate} instance from its individual components.
	 *
	 * <p>This factory method is used when the parsed date, calendar,
	 * and the resolved {@link Temporal} are already known.</p>
	 *
	 * @param date     the parsed date component (day, month, year) according to the calendar
	 * @param calendar the calendar associated with this date (e.g. Gregorian or Hijri)
	 * @param temporal the resolved temporal representation backing this date
	 * @return a new {@code ArabicDate} instance combining all components
	 */
	public static ArabicDate of(Date date,
								Calendar calendar,
								Temporal temporal) {
		return new ArabicDate(date, calendar, temporal);
	}

	/**
	 * Creates a new {@code ArabicDate} from a {@link Temporal} and a calendar.
	 *
	 * <p>This method extracts the {@code day}, {@code month}, and {@code year}
	 * fields from the given temporal object and constructs an {@code ArabicDate}
	 * using the provided calendar's chronology.</p>
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

		int day = temporal.get(ChronoField.DAY_OF_MONTH);
		int month = temporal.get(ChronoField.MONTH_OF_YEAR);
		int year = temporal.get(ChronoField.YEAR);

		var date = ArabicDate.Date.of(day, month, calendar.chronology(), year);

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
			return CALENDAR_PREFIX_AR + " " + arabicCalendar;
		}
	}
}
