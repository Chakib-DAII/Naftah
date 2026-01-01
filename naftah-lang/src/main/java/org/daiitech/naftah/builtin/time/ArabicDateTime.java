package org.daiitech.naftah.builtin.time;


import java.time.Duration;
import java.time.Period;
import java.time.chrono.Chronology;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;

import org.daiitech.naftah.utils.time.ChronologyUtils;
import org.daiitech.naftah.utils.time.TemporalUtils;

/**
 * Represents a complete Arabic date-time, consisting of:
 * <ul>
 * <li>An {@link ArabicDate} component</li>
 * <li>An {@link ArabicTime} component</li>
 * <li>A resolved {@link Temporal} representation</li>
 * </ul>
 *
 * <p>This record is typically produced after parsing Arabic date and time
 * expressions and resolving them against a specific chronology or calendar system.</p>
 *
 * <p>The {@link Temporal} allows integration with Java's time API.</p>
 *
 * @param arabicDate the date component
 * @param arabicTime the time component
 * @param temporal   the resolved temporal
 * @author Chakib Daii
 */
public record ArabicDateTime(
		ArabicDate arabicDate,
		ArabicTime arabicTime,
		Temporal temporal
) implements ArabicTemporalPoint, DateSupport, TimeSupport {
	/**
	 * Obtains the current date-time using the default chronology
	 * and the system default time zone.
	 *
	 * @return the current {@code ArabicDateTime}
	 */
	public static ArabicDateTime now() {
		return now(ChronologyUtils.DEFAULT_CHRONOLOGY);
	}

	/**
	 * Obtains the current date-time using the specified chronology
	 * and the system default time zone.
	 *
	 * @param chronology the chronology to use (not {@code null})
	 * @return the current {@code ArabicDateTime}
	 * @throws NullPointerException if {@code chronology} is {@code null}
	 */
	public static ArabicDateTime now(Chronology chronology) {
		var calendar = ArabicDate.Calendar.of(chronology);
		return now(calendar);
	}

	/**
	 * Obtains the current date-time using the specified calendar
	 * and the system default time zone.
	 *
	 * @param calendar the calendar to use (not {@code null})
	 * @return the current {@code ArabicDateTime}
	 * @throws NullPointerException if {@code calendar} is {@code null}
	 */
	public static ArabicDateTime now(ArabicDate.Calendar calendar) {
		return now(calendar, null);
	}

	/**
	 * Obtains the current date-time using the default chronology
	 * and the specified zone or offset.
	 *
	 * @param zoneOrOffset the zone or offset to use, or {@code null}
	 *                     to use the system default
	 * @return the current {@code ArabicDateTime}
	 */
	public static ArabicDateTime now(ArabicTime.ZoneOrOffset zoneOrOffset) {
		var calendar = ArabicDate.Calendar.of(ChronologyUtils.DEFAULT_CHRONOLOGY);
		return now(calendar, zoneOrOffset);
	}

	/**
	 * Obtains the current date-time using the specified chronology
	 * and zone or offset.
	 *
	 * @param chronology   the chronology to use (not {@code null})
	 * @param zoneOrOffset the zone or offset to use, or {@code null}
	 *                     to use the system default
	 * @return the current {@code ArabicDateTime}
	 * @throws NullPointerException if {@code chronology} is {@code null}
	 */
	public static ArabicDateTime now(Chronology chronology, ArabicTime.ZoneOrOffset zoneOrOffset) {
		var calendar = ArabicDate.Calendar.of(chronology);
		return now(calendar, zoneOrOffset);
	}

	/**
	 * Obtains the current date-time using the specified calendar
	 * and zone or offset.
	 *
	 * <p>
	 * The returned {@code ArabicDateTime} represents the current
	 * date and time as resolved by the given calendar and
	 * zone or offset.
	 * </p>
	 *
	 * @param calendar     the calendar to use (not {@code null})
	 * @param zoneOrOffset the zone or offset to use, or {@code null}
	 *                     to use the system default
	 * @return the current {@code ArabicDateTime}
	 * @throws NullPointerException if {@code calendar} is {@code null}
	 */
	public static ArabicDateTime now(ArabicDate.Calendar calendar, ArabicTime.ZoneOrOffset zoneOrOffset) {
		ArabicDate date = ArabicDate.now(calendar, zoneOrOffset);
		ArabicTime time = ArabicTime.now(zoneOrOffset);
		return ArabicDateTime
				.of(date, time);
	}

	/**
	 * Obtains an instance of {@code ArabicDateTime} from year, month, day, hour, and minute.
	 * <p>
	 * The second and nanosecond fields will default to {@code 0}.
	 * The day must be valid for the specified month and year.
	 *
	 * @param year       the year value
	 * @param month      the month-of-year value (1–12)
	 * @param dayOfMonth the day-of-month value (1–31)
	 * @param hour       the hour-of-day (0–23)
	 * @param minute     the minute-of-hour (0–59)
	 * @return a new {@code ArabicDateTime} instance, not null
	 */
	public static ArabicDateTime of(int year, int month, int dayOfMonth, int hour, int minute) {
		return of(year, month, dayOfMonth, hour, minute, 0, 0);
	}

	/**
	 * Obtains an instance of {@code ArabicDateTime} from year, month, day,
	 * hour, and minute, using an optional time zone or offset.
	 *
	 * <p>The second and nanosecond fields will default to {@code 0}.
	 * The day must be valid for the specified month and year.</p>
	 *
	 * <p>If a {@code zoneOrOffset} is provided, the resulting date-time will be
	 * backed by a {@link java.time.OffsetDateTime} or {@link java.time.ZonedDateTime}.
	 * If {@code null}, the result is backed by a {@link java.time.LocalDateTime}.</p>
	 *
	 * @param year         the year value
	 * @param month        the month-of-year value (1–12)
	 * @param dayOfMonth   the day-of-month value (1–31)
	 * @param hour         the hour-of-day (0–23)
	 * @param minute       the minute-of-hour (0–59)
	 * @param zoneOrOffset the optional time zone or offset, may be {@code null}
	 * @return a new {@code ArabicDateTime} instance, not null
	 */
	public static ArabicDateTime of(int year,
									int month,
									int dayOfMonth,
									int hour,
									int minute,
									ArabicTime.ZoneOrOffset zoneOrOffset) {
		return of(year, month, dayOfMonth, hour, minute, 0, 0, zoneOrOffset);
	}

	/**
	 * Obtains an instance of {@code ArabicDateTime} from year, month, day, hour, minute, and second.
	 * <p>
	 * The nanosecond field will default to {@code 0}.
	 * The day must be valid for the specified month and year.
	 *
	 * @param year       the year value
	 * @param month      the month-of-year value (1–12)
	 * @param dayOfMonth the day-of-month value (1–31)
	 * @param hour       the hour-of-day (0–23)
	 * @param minute     the minute-of-hour (0–59)
	 * @param second     the second-of-minute (0–59)
	 * @return a new {@code ArabicDateTime} instance, not null
	 */
	public static ArabicDateTime of(int year, int month, int dayOfMonth, int hour, int minute, int second) {
		return of(year, month, dayOfMonth, hour, minute, second, 0);
	}

	/**
	 * Obtains an instance of {@code ArabicDateTime} from year, month, day,
	 * hour, minute, and second, using an optional time zone or offset.
	 *
	 * <p>The nanosecond field will default to {@code 0}.
	 * The day must be valid for the specified month and year.</p>
	 *
	 * <p>If a {@code zoneOrOffset} is provided, the resulting date-time will be
	 * backed by a {@link java.time.OffsetDateTime} or {@link java.time.ZonedDateTime}.
	 * If {@code null}, the result is backed by a {@link java.time.LocalDateTime}.</p>
	 *
	 * @param year         the year value
	 * @param month        the month-of-year value (1–12)
	 * @param dayOfMonth   the day-of-month value (1–31)
	 * @param hour         the hour-of-day (0–23)
	 * @param minute       the minute-of-hour (0–59)
	 * @param second       the second-of-minute (0–59)
	 * @param zoneOrOffset the optional time zone or offset, may be {@code null}
	 * @return a new {@code ArabicDateTime} instance, not null
	 */
	public static ArabicDateTime of(int year,
									int month,
									int dayOfMonth,
									int hour,
									int minute,
									int second,
									ArabicTime.ZoneOrOffset zoneOrOffset) {
		return of(year, month, dayOfMonth, hour, minute, second, 0, zoneOrOffset);
	}


	/**
	 * Obtains an instance of {@code ArabicDateTime} from year, month, day, hour, minute, second, and nanosecond.
	 * <p>
	 * The day must be valid for the specified month and year.
	 *
	 * @param year         the year value
	 * @param month        the month-of-year value (1–12)
	 * @param dayOfMonth   the day-of-month value (1–31)
	 * @param hour         the hour-of-day (0–23)
	 * @param minute       the minute-of-hour (0–59)
	 * @param second       the second-of-minute (0–59)
	 * @param nanoOfSecond the nano-of-second (0–999,999,999)
	 * @return a new {@code ArabicDateTime} instance, not null
	 */
	public static ArabicDateTime of(int year,
									int month,
									int dayOfMonth,
									int hour,
									int minute,
									int second,
									int nanoOfSecond) {
		return of(year, month, dayOfMonth, hour, minute, second, nanoOfSecond, null);
	}

	/**
	 * Obtains an instance of {@code ArabicDateTime} from year, month, day,
	 * hour, minute, second, and nanosecond, using an optional time zone or offset.
	 *
	 * <p>The day must be valid for the specified month and year.</p>
	 *
	 * <p>If a {@code zoneOrOffset} is provided, the resulting date-time will be
	 * backed by a {@link java.time.OffsetDateTime} or {@link java.time.ZonedDateTime}.
	 * If {@code null}, the result is backed by a {@link java.time.LocalDateTime}.</p>
	 *
	 * @param year         the year value
	 * @param month        the month-of-year value (1–12)
	 * @param dayOfMonth   the day-of-month value (1–31)
	 * @param hour         the hour-of-day (0–23)
	 * @param minute       the minute-of-hour (0–59)
	 * @param second       the second-of-minute (0–59)
	 * @param nanoOfSecond the nano-of-second (0–999,999,999)
	 * @param zoneOrOffset the optional time zone or offset, may be {@code null}
	 * @return a new {@code ArabicDateTime} instance, not null
	 */
	public static ArabicDateTime of(int year,
									int month,
									int dayOfMonth,
									int hour,
									int minute,
									int second,
									int nanoOfSecond,
									ArabicTime.ZoneOrOffset zoneOrOffset) {
		ArabicDate date = ArabicDate.of(dayOfMonth, month, year);
		ArabicTime time = ArabicTime.of(hour, minute, second, nanoOfSecond, zoneOrOffset);
		return of(date, time);
	}

	/**
	 * Obtains an instance of {@code ArabicDateTime} from a specific {@link Chronology},
	 * year, month, day, hour, and minute.
	 * <p>
	 * The second and nanosecond fields will default to {@code 0}.
	 *
	 * @param chronology the chronology to use, not null
	 * @param year       the year value
	 * @param month      the month-of-year value (1–12)
	 * @param dayOfMonth the day-of-month value (1–31)
	 * @param hour       the hour-of-day (0–23)
	 * @param minute     the minute-of-hour (0–59)
	 * @return a new {@code ArabicDateTime} instance, not null
	 */
	public static ArabicDateTime of(Chronology chronology, int year, int month, int dayOfMonth, int hour, int minute) {
		return of(chronology, year, month, dayOfMonth, hour, minute, 0, 0);
	}

	/**
	 * Obtains an instance of {@code ArabicDateTime} from a specific {@link Chronology},
	 * year, month, day, hour, and minute, using an optional time zone or offset.
	 *
	 * <p>The second and nanosecond fields will default to {@code 0}.
	 * The day must be valid for the specified month and year according to the
	 * provided chronology.</p>
	 *
	 * <p>If a {@code zoneOrOffset} is provided, the resulting date-time will be
	 * backed by a {@link java.time.OffsetDateTime} or {@link java.time.ZonedDateTime}.
	 * If {@code null}, the result is backed by a {@link java.time.LocalDateTime}.</p>
	 *
	 * @param chronology   the chronology to use, not null
	 * @param year         the year value
	 * @param month        the month-of-year value (1–12)
	 * @param dayOfMonth   the day-of-month value (1–31)
	 * @param hour         the hour-of-day (0–23)
	 * @param minute       the minute-of-hour (0–59)
	 * @param zoneOrOffset the optional time zone or offset, may be {@code null}
	 * @return a new {@code ArabicDateTime} instance, not null
	 */
	public static ArabicDateTime of(Chronology chronology,
									int year,
									int month,
									int dayOfMonth,
									int hour,
									int minute,
									ArabicTime.ZoneOrOffset zoneOrOffset) {
		return of(chronology, year, month, dayOfMonth, hour, minute, 0, 0, zoneOrOffset);
	}

	/**
	 * Obtains an instance of {@code ArabicDateTime} from a specific {@link Chronology},
	 * year, month, day, hour, minute, and second.
	 * <p>
	 * The nanosecond field will default to {@code 0}.
	 *
	 * @param chronology the chronology to use, not null
	 * @param year       the year value
	 * @param month      the month-of-year value (1–12)
	 * @param dayOfMonth the day-of-month value (1–31)
	 * @param hour       the hour-of-day (0–23)
	 * @param minute     the minute-of-hour (0–59)
	 * @param second     the second-of-minute (0–59)
	 * @return a new {@code ArabicDateTime} instance, not null
	 */
	public static ArabicDateTime of(Chronology chronology,
									int year,
									int month,
									int dayOfMonth,
									int hour,
									int minute,
									int second) {
		return of(chronology, year, month, dayOfMonth, hour, minute, second, 0);
	}

	/**
	 * Obtains an instance of {@code ArabicDateTime} from a specific {@link Chronology},
	 * year, month, day, hour, minute, and second, using an optional time zone or offset.
	 *
	 * <p>The nanosecond field will default to {@code 0}.
	 * The day must be valid for the specified month and year according to the
	 * provided chronology.</p>
	 *
	 * <p>If a {@code zoneOrOffset} is provided, the resulting date-time will be
	 * backed by a {@link java.time.OffsetDateTime} or {@link java.time.ZonedDateTime}.
	 * If {@code null}, the result is backed by a {@link java.time.LocalDateTime}.</p>
	 *
	 * @param chronology   the chronology to use, not null
	 * @param year         the year value
	 * @param month        the month-of-year value (1–12)
	 * @param dayOfMonth   the day-of-month value (1–31)
	 * @param hour         the hour-of-day (0–23)
	 * @param minute       the minute-of-hour (0–59)
	 * @param second       the second-of-minute (0–59)
	 * @param zoneOrOffset the optional time zone or offset, may be {@code null}
	 * @return a new {@code ArabicDateTime} instance, not null
	 */
	public static ArabicDateTime of(Chronology chronology,
									int year,
									int month,
									int dayOfMonth,
									int hour,
									int minute,
									int second,
									ArabicTime.ZoneOrOffset zoneOrOffset) {
		return of(chronology, year, month, dayOfMonth, hour, minute, second, 0, zoneOrOffset);
	}

	/**
	 * Obtains an instance of {@code ArabicDateTime} from a specific {@link Chronology},
	 * year, month, day, hour, minute, second, and nanosecond.
	 *
	 * @param chronology   the chronology to use, not null
	 * @param year         the year value
	 * @param month        the month-of-year value (1–12)
	 * @param dayOfMonth   the day-of-month value (1–31)
	 * @param hour         the hour-of-day (0–23)
	 * @param minute       the minute-of-hour (0–59)
	 * @param second       the second-of-minute (0–59)
	 * @param nanoOfSecond the nano-of-second (0–999,999,999)
	 * @return a new {@code ArabicDateTime} instance, not null
	 */
	public static ArabicDateTime of(Chronology chronology,
									int year,
									int month,
									int dayOfMonth,
									int hour,
									int minute,
									int second,
									int nanoOfSecond) {
		return of(chronology, year, month, dayOfMonth, hour, minute, second, nanoOfSecond, null);
	}

	/**
	 * Obtains an instance of {@code ArabicDateTime} from a specific {@link Chronology},
	 * year, month, day, hour, minute, second, and nanosecond, using an optional
	 * time zone or offset.
	 *
	 * <p>The day must be valid for the specified month and year according to the
	 * provided chronology.</p>
	 *
	 * <p>If a {@code zoneOrOffset} is provided, the resulting date-time will be
	 * backed by a {@link java.time.OffsetDateTime} or {@link java.time.ZonedDateTime}.
	 * If {@code null}, the result is backed by a {@link java.time.LocalDateTime}.</p>
	 *
	 * @param chronology   the chronology to use, not null
	 * @param year         the year value
	 * @param month        the month-of-year value (1–12)
	 * @param dayOfMonth   the day-of-month value (1–31)
	 * @param hour         the hour-of-day (0–23)
	 * @param minute       the minute-of-hour (0–59)
	 * @param second       the second-of-minute (0–59)
	 * @param nanoOfSecond the nano-of-second (0–999,999,999)
	 * @param zoneOrOffset the optional time zone or offset, may be {@code null}
	 * @return a new {@code ArabicDateTime} instance, not null
	 */
	public static ArabicDateTime of(Chronology chronology,
									int year,
									int month,
									int dayOfMonth,
									int hour,
									int minute,
									int second,
									int nanoOfSecond,
									ArabicTime.ZoneOrOffset zoneOrOffset) {
		ArabicDate date = ArabicDate.of(chronology, dayOfMonth, month, year);
		ArabicTime time = ArabicTime.of(hour, minute, second, nanoOfSecond, zoneOrOffset);
		return of(date, time);
	}

	/**
	 * Obtains an instance of {@code ArabicDateTime} from a day, Arabic month name, year, hour, and minute.
	 * <p>
	 * Seconds and nanoseconds default to {@code 0}.
	 *
	 * @param year        the year value
	 * @param arabicMonth the Arabic month name, not null
	 * @param day         the day-of-month value (1–31)
	 * @param hour        the hour-of-day (0–23)
	 * @param minute      the minute-of-hour (0–59)
	 * @return a new {@code ArabicDateTime} instance, not null
	 */
	public static ArabicDateTime of(int year,
									String arabicMonth,
									int day,
									int hour,
									int minute) {
		return of(year, arabicMonth, day, hour, minute, 0, 0);
	}

	/**
	 * Obtains an instance of {@code ArabicDateTime} from a day, Arabic month name, year,
	 * hour, and minute, using an optional time zone or offset.
	 *
	 * <p>The second and nanosecond fields default to {@code 0}.
	 * The day must be valid for the specified month and year.</p>
	 *
	 * <p>If {@code zoneOrOffset} is provided, the resulting date-time will be backed by
	 * an {@link java.time.OffsetDateTime} or {@link java.time.ZonedDateTime}.
	 * If {@code null}, the result is backed by a {@link java.time.LocalDateTime}.</p>
	 *
	 * @param year         the year value
	 * @param arabicMonth  the Arabic month name, not null
	 * @param day          the day-of-month (1–31)
	 * @param hour         the hour-of-day (0–23)
	 * @param minute       the minute-of-hour (0–59)
	 * @param zoneOrOffset the optional time zone or offset, may be {@code null}
	 * @return a new {@code ArabicDateTime} instance, not null
	 */
	public static ArabicDateTime of(int year,
									String arabicMonth,
									int day,
									int hour,
									int minute,
									ArabicTime.ZoneOrOffset zoneOrOffset) {
		return of(year, arabicMonth, day, hour, minute, 0, 0, zoneOrOffset);
	}


	/**
	 * Obtains an instance of {@code ArabicDateTime} from a day, Arabic month name, year,
	 * hour, minute, and second.
	 * <p>
	 * Nanoseconds default to {@code 0}. The day must be valid for the specified month and year.
	 *
	 * @param year        the year value
	 * @param arabicMonth the Arabic month name, not null
	 * @param day         the day-of-month (1–31)
	 * @param hour        the hour-of-day (0–23)
	 * @param minute      the minute-of-hour (0–59)
	 * @param second      the second-of-minute (0–59)
	 * @return a new {@code ArabicDateTime} instance, not null
	 */
	public static ArabicDateTime of(
									int year,
									String arabicMonth,
									int day,
									int hour,
									int minute,
									int second) {
		return of(year, arabicMonth, day, hour, minute, second, 0);
	}

	/**
	 * Obtains an instance of {@code ArabicDateTime} from a day, Arabic month name, year,
	 * hour, minute, and second, using an optional time zone or offset.
	 *
	 * <p>The nanosecond field defaults to {@code 0}.
	 * The day must be valid for the specified month and year.</p>
	 *
	 * <p>If {@code zoneOrOffset} is provided, the resulting date-time will be backed by
	 * an {@link java.time.OffsetDateTime} or {@link java.time.ZonedDateTime}.
	 * If {@code null}, the result is backed by a {@link java.time.LocalDateTime}.</p>
	 *
	 * @param year         the year value
	 * @param arabicMonth  the Arabic month name, not null
	 * @param day          the day-of-month (1–31)
	 * @param hour         the hour-of-day (0–23)
	 * @param minute       the minute-of-hour (0–59)
	 * @param second       the second-of-minute (0–59)
	 * @param zoneOrOffset the optional time zone or offset, may be {@code null}
	 * @return a new {@code ArabicDateTime} instance, not null
	 */
	public static ArabicDateTime of(
									int year,
									String arabicMonth,
									int day,
									int hour,
									int minute,
									int second,
									ArabicTime.ZoneOrOffset zoneOrOffset) {
		return of(year, arabicMonth, day, hour, minute, second, 0, zoneOrOffset);
	}

	/**
	 * Obtains an instance of {@code ArabicDateTime} from a day, Arabic month name, year,
	 * hour, minute, second, and nanosecond.
	 *
	 * @param year         the year value
	 * @param arabicMonth  the Arabic month name, not null
	 * @param day          the day-of-month (1–31)
	 * @param hour         the hour-of-day (0–23)
	 * @param minute       the minute-of-hour (0–59)
	 * @param second       the second-of-minute (0–59)
	 * @param nanoOfSecond the nano-of-second (0–999,999,999)
	 * @return a new {@code ArabicDateTime} instance, not null
	 */
	public static ArabicDateTime of(
									int year,
									String arabicMonth,
									int day,
									int hour,
									int minute,
									int second,
									int nanoOfSecond) {
		return of(year, arabicMonth, day, hour, minute, second, nanoOfSecond, null);
	}

	/**
	 * Obtains an instance of {@code ArabicDateTime} from a day, Arabic month name, year,
	 * hour, minute, second, and nanosecond, using an optional time zone or offset.
	 *
	 * <p>The day must be valid for the specified month and year.</p>
	 *
	 * <p>If {@code zoneOrOffset} is provided, the resulting date-time will be backed by
	 * an {@link java.time.OffsetDateTime} or {@link java.time.ZonedDateTime}.
	 * If {@code null}, the result is backed by a {@link java.time.LocalDateTime}.</p>
	 *
	 * @param year         the year value
	 * @param arabicMonth  the Arabic month name, not null
	 * @param day          the day-of-month (1–31)
	 * @param hour         the hour-of-day (0–23)
	 * @param minute       the minute-of-hour (0–59)
	 * @param second       the second-of-minute (0–59)
	 * @param nanoOfSecond the nanosecond-of-second (0–999,999,999)
	 * @param zoneOrOffset the optional time zone or offset, may be {@code null}
	 * @return a new {@code ArabicDateTime} instance, not null
	 */
	public static ArabicDateTime of(
									int year,
									String arabicMonth,
									int day,
									int hour,
									int minute,
									int second,
									int nanoOfSecond,
									ArabicTime.ZoneOrOffset zoneOrOffset) {
		ArabicDate date = ArabicDate.of(day, arabicMonth, year);
		ArabicTime time = ArabicTime.of(hour, minute, second, nanoOfSecond, zoneOrOffset);
		return of(date, time);
	}

	/**
	 * Obtains an instance of {@code ArabicDateTime} from a specific {@link Chronology},
	 * day, Arabic month name, year, hour, and minute.
	 * <p>
	 * Seconds and nanoseconds default to {@code 0}.
	 *
	 * @param chronology  the chronology to use, not null
	 * @param year        the year value
	 * @param arabicMonth the Arabic month name, not null
	 * @param day         the day-of-month (1–31)
	 * @param hour        the hour-of-day (0–23)
	 * @param minute      the minute-of-hour (0–59)
	 * @return a new {@code ArabicDateTime} instance, not null
	 */
	public static ArabicDateTime of(Chronology chronology,
									int year,
									String arabicMonth,
									int day,
									int hour,
									int minute) {
		return of(chronology, year, arabicMonth, day, hour, minute, 0, 0);
	}

	/**
	 * Obtains an instance of {@code ArabicDateTime} from a specific {@link Chronology},
	 * day, Arabic month name, year, hour, and minute, using an optional time zone or offset.
	 *
	 * <p>Seconds and nanoseconds default to {@code 0}.</p>
	 *
	 * <p>If {@code zoneOrOffset} is provided, the resulting date-time will be backed by
	 * an {@link java.time.OffsetDateTime} or {@link java.time.ZonedDateTime}.
	 * If {@code null}, the result is backed by a {@link java.time.LocalDateTime}.</p>
	 *
	 * @param chronology   the chronology to use, not null
	 * @param year         the year value
	 * @param arabicMonth  the Arabic month name, not null
	 * @param day          the day-of-month (1–31)
	 * @param hour         the hour-of-day (0–23)
	 * @param minute       the minute-of-hour (0–59)
	 * @param zoneOrOffset the optional time zone or offset, may be {@code null}
	 * @return a new {@code ArabicDateTime} instance, not null
	 */
	public static ArabicDateTime of(Chronology chronology,
									int year,
									String arabicMonth,
									int day,
									int hour,
									int minute,
									ArabicTime.ZoneOrOffset zoneOrOffset) {
		return of(chronology, year, arabicMonth, day, hour, minute, 0, 0, zoneOrOffset);
	}


	/**
	 * Obtains an instance of {@code ArabicDateTime} from a specific {@link Chronology},
	 * day, Arabic month name, year, hour, minute, and second.
	 * <p>
	 * Nanoseconds default to {@code 0}.
	 *
	 * @param chronology  the chronology to use, not null
	 * @param year        the year value
	 * @param arabicMonth the Arabic month name, not null
	 * @param day         the day-of-month (1–31)
	 * @param hour        the hour-of-day (0–23)
	 * @param minute      the minute-of-hour (0–59)
	 * @param second      the second-of-minute (0–59)
	 * @return a new {@code ArabicDateTime} instance, not null
	 */
	public static ArabicDateTime of(Chronology chronology,
									int year,
									String arabicMonth,
									int day,
									int hour,
									int minute,
									int second) {
		return of(chronology, year, arabicMonth, day, hour, minute, second, 0);
	}

	/**
	 * Obtains an instance of {@code ArabicDateTime} from a specific {@link Chronology},
	 * day, Arabic month name, year, hour, minute, and second, using an optional time zone or offset.
	 *
	 * <p>Nanoseconds default to {@code 0}.</p>
	 *
	 * <p>If {@code zoneOrOffset} is provided, the resulting date-time will be backed by
	 * an {@link java.time.OffsetDateTime} or {@link java.time.ZonedDateTime}.
	 * If {@code null}, the result is backed by a {@link java.time.LocalDateTime}.</p>
	 *
	 * @param chronology   the chronology to use, not null
	 * @param year         the year value
	 * @param arabicMonth  the Arabic month name, not null
	 * @param day          the day-of-month (1–31)
	 * @param hour         the hour-of-day (0–23)
	 * @param minute       the minute-of-hour (0–59)
	 * @param second       the second-of-minute (0–59)
	 * @param zoneOrOffset the optional time zone or offset, may be {@code null}
	 * @return a new {@code ArabicDateTime} instance, not null
	 */
	public static ArabicDateTime of(Chronology chronology,
									int year,
									String arabicMonth,
									int day,
									int hour,
									int minute,
									int second,
									ArabicTime.ZoneOrOffset zoneOrOffset) {
		return of(chronology, year, arabicMonth, day, hour, minute, second, 0, zoneOrOffset);
	}

	/**
	 * Obtains an instance of {@code ArabicDateTime} from a specific {@link Chronology},
	 * day, Arabic month name, year, hour, minute, second, and nanosecond.
	 *
	 * @param chronology   the chronology to use, not null
	 * @param year         the year value
	 * @param arabicMonth  the Arabic month name, not null
	 * @param day          the day-of-month (1–31)
	 * @param hour         the hour-of-day (0–23)
	 * @param minute       the minute-of-hour (0–59)
	 * @param second       the second-of-minute (0–59)
	 * @param nanoOfSecond the nano-of-second (0–999,999,999)
	 * @return a new {@code ArabicDateTime} instance, not null
	 */
	public static ArabicDateTime of(Chronology chronology,
									int year,
									String arabicMonth,
									int day,
									int hour,
									int minute,
									int second,
									int nanoOfSecond) {
		return of(chronology, year, arabicMonth, day, hour, minute, second, nanoOfSecond, null);
	}

	/**
	 * Obtains an instance of {@code ArabicDateTime} from a specific {@link Chronology},
	 * day, Arabic month name, year, hour, minute, second, and nanosecond, using an optional time zone or offset.
	 *
	 * <p>This method allows full specification of the time down to nanoseconds.</p>
	 *
	 * <p>If {@code zoneOrOffset} is provided, the resulting date-time will be backed by
	 * an {@link java.time.OffsetDateTime} or {@link java.time.ZonedDateTime}.
	 * If {@code null}, the result is backed by a {@link java.time.LocalDateTime}.</p>
	 *
	 * @param chronology   the chronology to use, not null
	 * @param year         the year value
	 * @param arabicMonth  the Arabic month name, not null
	 * @param day          the day-of-month (1–31)
	 * @param hour         the hour-of-day (0–23)
	 * @param minute       the minute-of-hour (0–59)
	 * @param second       the second-of-minute (0–59)
	 * @param nanoOfSecond the nanosecond-of-second (0–999,999,999)
	 * @param zoneOrOffset the optional time zone or offset, may be {@code null}
	 * @return a new {@code ArabicDateTime} instance, not null
	 */
	public static ArabicDateTime of(Chronology chronology,
									int year,
									String arabicMonth,
									int day,
									int hour,
									int minute,
									int second,
									int nanoOfSecond,
									ArabicTime.ZoneOrOffset zoneOrOffset) {
		ArabicDate date = ArabicDate.of(chronology, day, arabicMonth, year);
		ArabicTime time = ArabicTime.of(hour, minute, second, nanoOfSecond, zoneOrOffset);
		return of(date, time);
	}

	/**
	 * Creates a new {@code ArabicDateTime} instance.
	 *
	 * @param arabicDate the date component
	 * @param arabicTime the time component
	 * @return a new {@code ArabicDateTime} instance
	 */
	public static ArabicDateTime of(
									ArabicDate arabicDate,
									ArabicTime arabicTime) {
		return new ArabicDateTime(  arabicDate,
									arabicTime,
									TemporalUtils
											.createDateTime(arabicDate.date(),
															arabicDate.calendar(),
															arabicTime.time(),
															arabicTime.zoneOrOffset())
		);
	}

	/**
	 * Creates a new {@code ArabicDateTime} instance.
	 *
	 * @param arabicDate the date component
	 * @param arabicTime the time component
	 * @param temporal   the resolved temporal
	 * @return a new {@code ArabicDateTime} instance
	 */
	public static ArabicDateTime of(
									ArabicDate arabicDate,
									ArabicTime arabicTime,
									Temporal temporal) {
		return new ArabicDateTime(arabicDate, arabicTime, temporal);
	}

	/**
	 * Returns the number of days since the epoch (1970-01-01 ISO) for the date part
	 * of this {@code ArabicDateTime}.
	 *
	 * @return the epoch day count
	 */
	@Override
	public long toEpochDay() {
		return arabicDate.toEpochDay();
	}

	/**
	 * Returns the year of the date part of this {@code ArabicDateTime}.
	 *
	 * @return the year value
	 */
	@Override
	public int getYear() {
		return arabicDate.getYear();
	}

	/**
	 * Returns the numeric month value (1–12) of the date part of this {@code ArabicDateTime}.
	 *
	 * @return the month value
	 */
	@Override
	public int getMonthValue() {
		return arabicDate.getMonthValue();
	}

	/**
	 * Returns the Arabic month name of the date part of this {@code ArabicDateTime}.
	 *
	 * @return the Arabic month name
	 */
	@Override
	public String getMonth() {
		return arabicDate.getMonth();
	}

	/**
	 * Returns the day-of-month of the date part of this {@code ArabicDateTime}.
	 *
	 * @return the day of the month
	 */
	@Override
	public int getDayOfMonth() {
		return arabicDate.getDayOfMonth();
	}

	/**
	 * Returns the day-of-year of the date part of this {@code ArabicDateTime},
	 * according to its chronology.
	 *
	 * @return the day of the year (1–365 or 1–366 for leap years)
	 */
	@Override
	public int getDayOfYear() {
		return arabicDate.getDayOfYear();
	}

	/**
	 * Returns the day of the week of the date part of this {@code ArabicDateTime}
	 * as an Arabic string.
	 *
	 * @return the Arabic day of the week
	 */
	@Override
	public String getDayOfWeek() {
		return arabicDate.getDayOfWeek();
	}

	/**
	 * Checks whether the year of the date part of this {@code ArabicDateTime} is
	 * a leap year in its chronology.
	 *
	 * @return {@code true} if it is a leap year, {@code false} otherwise
	 */
	@Override
	public boolean isLeapYear() {
		return arabicDate.isLeapYear();
	}

	/**
	 * Returns the length of the month in days for the date part of this {@code ArabicDateTime}.
	 *
	 * @return the number of days in the month
	 */
	@Override
	public int lengthOfMonth() {
		return arabicDate.lengthOfMonth();
	}

	/**
	 * Gets the hour-of-day from the time part of this {@code ArabicDateTime}.
	 *
	 * <p>The value is based on a 24-hour clock.</p>
	 *
	 * @return the hour-of-day, from 0 to 23
	 */
	@Override
	public int getHour() {
		return arabicTime.getHour();
	}

	/**
	 * Gets the minute-of-hour from the time part of this {@code ArabicDateTime}.
	 *
	 * @return the minute-of-hour, from 0 to 59
	 */
	@Override
	public int getMinute() {
		return arabicTime.getMinute();
	}

	/**
	 * Gets the second-of-minute from the time part of this {@code ArabicDateTime}.
	 *
	 * @return the second-of-minute, from 0 to 59
	 */
	@Override
	public int getSecond() {
		return arabicTime.getSecond();
	}

	/**
	 * Gets the nano-of-second from the time part of this {@code ArabicDateTime}.
	 *
	 * @return the nano-of-second, from 0 to 999,999,999
	 */
	@Override
	public int getNano() {
		return arabicTime.getNano();
	}

	/**
	 * Returns the length of the year in days for the date part of this {@code ArabicDateTime}.
	 *
	 * <p>The number of days depends on the chronology of the date:</p>
	 * <ul>
	 * <li>For Gregorian/ISO years, leap years return 366 days, non-leap years return 365 days.</li>
	 * <li>For Hijri years, leap years return 355 days, non-leap years return 354 days.</li>
	 * </ul>
	 *
	 * @return the number of days in the year
	 */
	@Override
	public int lengthOfYear() {
		return arabicDate.lengthOfYear();
	}

	/**
	 * Returns a new {@code ArabicDateTime} obtained by adding the given
	 * Arabic temporal amount to this date-time.
	 *
	 * @param arabicTemporalAmount the temporal amount to add
	 * @return a new {@code ArabicDateTime} instance
	 */
	@Override
	public ArabicDateTime plus(ArabicTemporalAmount arabicTemporalAmount) {
		return compute(arabicTemporalAmount, true);
	}

	/**
	 * Returns a new {@code ArabicDateTime} with the specified number of years added.
	 *
	 * @param yearsToAdd the number of years to add, may be negative
	 * @return a new {@code ArabicDateTime} with the years added
	 */
	@Override
	public ArabicDateTime plusYears(long yearsToAdd) {
		if (yearsToAdd == 0) {
			return this;
		}
		return of(
					arabicDate.plusYears(yearsToAdd),
					arabicTime,
					temporal.plus(yearsToAdd, ChronoUnit.YEARS)
		);
	}

	/**
	 * Returns a new {@code ArabicDateTime} with the specified number of months added.
	 *
	 * @param monthsToAdd the number of months to add, may be negative
	 * @return a new {@code ArabicDateTime} with the months added
	 */
	@Override
	public ArabicDateTime plusMonths(long monthsToAdd) {
		if (monthsToAdd == 0) {
			return this;
		}
		return of(
					arabicDate.plusMonths(monthsToAdd),
					arabicTime,
					temporal.plus(monthsToAdd, ChronoUnit.MONTHS)
		);
	}

	/**
	 * Returns a new {@code ArabicDateTime} with the specified number of weeks added.
	 *
	 * @param weeksToAdd the number of weeks to add, may be negative
	 * @return a new {@code ArabicDateTime} with the weeks added
	 */
	@Override
	public ArabicDateTime plusWeeks(long weeksToAdd) {
		if (weeksToAdd == 0) {
			return this;
		}
		return of(
					arabicDate.plusWeeks(weeksToAdd),
					arabicTime,
					temporal.plus(weeksToAdd, ChronoUnit.WEEKS)
		);
	}

	/**
	 * Returns a new {@code ArabicDateTime} with the specified number of days added.
	 *
	 * @param daysToAdd the number of days to add, may be negative
	 * @return a new {@code ArabicDateTime} with the days added
	 */
	@Override
	public ArabicDateTime plusDays(long daysToAdd) {
		if (daysToAdd == 0) {
			return this;
		}
		return of(
					arabicDate.plusDays(daysToAdd),
					arabicTime,
					temporal.plus(daysToAdd, ChronoUnit.DAYS)
		);
	}

	/**
	 * Returns a new {@code ArabicDateTime} with the specified number of hours added.
	 *
	 * @param hoursToAdd the number of hours to add, may be negative
	 * @return a new {@code ArabicDateTime} with the hours added
	 */
	@Override
	public ArabicDateTime plusHours(long hoursToAdd) {
		if (hoursToAdd == 0) {
			return this;
		}
		return of(arabicDate, arabicTime.plusHours(hoursToAdd), temporal.plus(hoursToAdd, ChronoUnit.HOURS));
	}

	/**
	 * Returns a new {@code ArabicDateTime} with the specified number of minutes added.
	 *
	 * @param minutesToAdd the number of minutes to add, may be negative
	 * @return a new {@code ArabicDateTime} with the minutes added
	 */
	@Override
	public ArabicDateTime plusMinutes(long minutesToAdd) {
		if (minutesToAdd == 0) {
			return this;
		}
		return of(arabicDate, arabicTime.plusMinutes(minutesToAdd), temporal.plus(minutesToAdd, ChronoUnit.MINUTES));
	}

	/**
	 * Returns a new {@code ArabicDateTime} with the specified number of seconds added.
	 *
	 * @param secondsToAdd the number of seconds to add, may be negative
	 * @return a new {@code ArabicDateTime} with the seconds added
	 */
	@Override
	public ArabicDateTime plusSeconds(long secondsToAdd) {
		if (secondsToAdd == 0) {
			return this;
		}
		return of(arabicDate, arabicTime.plusSeconds(secondsToAdd), temporal.plus(secondsToAdd, ChronoUnit.SECONDS));
	}

	/**
	 * Returns a new {@code ArabicDateTime} with the specified number of nanoseconds added.
	 *
	 * @param nanosToAdd the number of nanoseconds to add, may be negative
	 * @return a new {@code ArabicDateTime} with the nanoseconds added
	 */
	@Override
	public ArabicDateTime plusNanos(long nanosToAdd) {
		if (nanosToAdd == 0) {
			return this;
		}
		return of(arabicDate, arabicTime.plusNanos(nanosToAdd), temporal.plus(nanosToAdd, ChronoUnit.NANOS));
	}

	/**
	 * Returns a new {@code ArabicDateTime} obtained by subtracting the given
	 * Arabic temporal amount from this date-time.
	 *
	 * @param arabicTemporalAmount the temporal amount to subtract
	 * @return a new {@code ArabicDateTime} instance
	 */
	@Override
	public ArabicDateTime minus(ArabicTemporalAmount arabicTemporalAmount) {
		return compute(arabicTemporalAmount, false);

	}

	/**
	 * Returns a new {@code ArabicDateTime} with the specified number of years subtracted.
	 *
	 * @param yearsToSubtract the number of years to subtract, may be negative
	 * @return a new {@code ArabicDateTime} with the years subtracted
	 */
	@Override
	public ArabicDateTime minusYears(long yearsToSubtract) {
		if (yearsToSubtract == 0) {
			return this;
		}
		return of(
					arabicDate.minusYears(yearsToSubtract),
					arabicTime,
					temporal.minus(yearsToSubtract, ChronoUnit.YEARS)
		);
	}

	/**
	 * Returns a new {@code ArabicDateTime} with the specified number of months subtracted.
	 *
	 * @param monthsToSubtract the number of months to subtract, may be negative
	 * @return a new {@code ArabicDateTime} with the months subtracted
	 */
	@Override
	public ArabicDateTime minusMonths(long monthsToSubtract) {
		if (monthsToSubtract == 0) {
			return this;
		}
		return of(
					arabicDate.minusMonths(monthsToSubtract),
					arabicTime,
					temporal.minus(monthsToSubtract, ChronoUnit.MONTHS)
		);
	}

	/**
	 * Returns a new {@code ArabicDateTime} with the specified number of weeks subtracted.
	 *
	 * @param weeksToSubtract the number of weeks to subtract, may be negative
	 * @return a new {@code ArabicDateTime} with the weeks subtracted
	 */
	@Override
	public ArabicDateTime minusWeeks(long weeksToSubtract) {
		if (weeksToSubtract == 0) {
			return this;
		}
		return of(
					arabicDate.minusWeeks(weeksToSubtract),
					arabicTime,
					temporal.minus(weeksToSubtract, ChronoUnit.WEEKS)
		);
	}

	/**
	 * Returns a new {@code ArabicDateTime} with the specified number of days subtracted.
	 *
	 * @param daysToSubtract the number of days to subtract, may be negative
	 * @return a new {@code ArabicDateTime} with the days subtracted
	 */
	@Override
	public ArabicDateTime minusDays(long daysToSubtract) {
		if (daysToSubtract == 0) {
			return this;
		}
		return of(
					arabicDate.minusDays(daysToSubtract),
					arabicTime,
					temporal.minus(daysToSubtract, ChronoUnit.DAYS)
		);
	}


	/**
	 * Returns a new {@code ArabicDateTime} with the specified number of hours subtracted.
	 *
	 * @param hoursToSubtract the number of hours to subtract, may be negative
	 * @return a new {@code ArabicDateTime} with the hours subtracted
	 */
	@Override
	public ArabicDateTime minusHours(long hoursToSubtract) {
		if (hoursToSubtract == 0) {
			return this;
		}
		return of(  arabicDate,
					arabicTime.minusHours(hoursToSubtract),
					temporal.minus(hoursToSubtract, ChronoUnit.HOURS));
	}

	/**
	 * Returns a new {@code ArabicDateTime} with the specified number of minutes subtracted.
	 *
	 * @param minutesToSubtract the number of minutes to subtract, may be negative
	 * @return a new {@code ArabicDateTime} with the minutes subtracted
	 */
	@Override
	public ArabicDateTime minusMinutes(long minutesToSubtract) {
		if (minutesToSubtract == 0) {
			return this;
		}
		return of(  arabicDate,
					arabicTime.minusMinutes(minutesToSubtract),
					temporal.minus(minutesToSubtract, ChronoUnit.MINUTES));
	}

	/**
	 * Returns a new {@code ArabicDateTime} with the specified number of seconds subtracted.
	 *
	 * @param secondsToSubtract the number of seconds to subtract, may be negative
	 * @return a new {@code ArabicDateTime} with the seconds subtracted
	 */
	@Override
	public ArabicDateTime minusSeconds(long secondsToSubtract) {
		if (secondsToSubtract == 0) {
			return this;
		}
		return of(  arabicDate,
					arabicTime.minusSeconds(secondsToSubtract),
					temporal.minus(secondsToSubtract, ChronoUnit.SECONDS));
	}

	/**
	 * Returns a new {@code ArabicDateTime} with the specified number of nanoseconds subtracted.
	 *
	 * @param nanosToSubtract the number of nanoseconds to subtract, may be negative
	 * @return a new {@code ArabicDateTime} with the nanoseconds subtracted
	 */
	@Override
	public ArabicDateTime minusNanos(long nanosToSubtract) {
		if (nanosToSubtract == 0) {
			return this;
		}
		return of(  arabicDate,
					arabicTime.minusNanos(nanosToSubtract),
					temporal.minus(nanosToSubtract, ChronoUnit.NANOS));
	}

	/**
	 * Computes a new {@code ArabicDateTime} by applying the given Arabic temporal
	 * amount using either addition or subtraction.
	 *
	 * <p>
	 * Supported temporal amounts:
	 * <ul>
	 * <li>{@link ArabicDuration}</li>
	 * <li>{@link ArabicPeriod}</li>
	 * <li>{@link ArabicPeriodWithDuration}</li>
	 * </ul>
	 * </p>
	 *
	 * <p>
	 * Duration handling rules:
	 * <ul>
	 * <li>Durations shorter than 24 hours affect only the time component</li>
	 * <li>Durations of 24 hours or more are split into a date-based
	 * {@link Period} and a remaining time-based {@link Duration}</li>
	 * </ul>
	 * </p>
	 *
	 * <p>
	 * Date and time components are updated independently while the resolved
	 * {@link Temporal} is updated in full.
	 * </p>
	 *
	 * @param arabicTemporalAmount the temporal amount to apply
	 * @param plus                 {@code true} to add, {@code false} to subtract
	 * @return a new {@code ArabicDateTime} instance
	 */
	private ArabicDateTime compute(ArabicTemporalAmount arabicTemporalAmount, boolean plus) {
		if (arabicTemporalAmount instanceof ArabicDuration arabicDuration) {
			var duration = arabicDuration.temporalAmount();
			long hours = duration.toHours();
			if (hours < 24) {
				return of(  arabicDate,
							arabicTime
									.compute(   arabicTemporalAmount,
												plus ? arabicTime.temporal()::plus : arabicTime.temporal()::minus),
							plus ? this.temporal.plus(duration) : this.temporal.minus(duration)
				);
			}
			else {
				long totalSeconds = duration.getSeconds();
				long nanos = duration.getNano();

				hours = totalSeconds / 3600;
				long extraDays = hours / 24;
				long remainingHours = hours % 24;
				long minutes = (totalSeconds % 3600) / 60;
				long seconds = totalSeconds % 60;
				long millis = nanos / 1_000_000;
				long remainingNanos = nanos % 1_000_000;

				Period period = Period.ofDays((int) extraDays);

				// Adjust duration to less than 24 hours
				Duration newDuration = Duration
						.ofHours(remainingHours)
						.plusMinutes(minutes)
						.plusMillis(millis)
						.plusSeconds(seconds)
						.plusNanos(remainingNanos);


				return of(  ArabicDate
									.of(arabicDate.calendar(),
										plus ?
												arabicDate.temporal().plus(period) :
												arabicDate.temporal().minus(period)),
							ArabicTime
									.of(arabicTime.zoneOrOffset(),
										plus ?
												arabicTime.temporal().plus(newDuration) :
												arabicTime.temporal().minus(newDuration)),
							plus ? this.temporal.plus(duration) : this.temporal.minus(duration)
				);
			}
		}
		else if (arabicTemporalAmount instanceof ArabicPeriodWithDuration arabicPeriodWithDuration) {
			Period period = arabicPeriodWithDuration.arabicPeriod().temporalAmount();
			Duration duration = arabicPeriodWithDuration.arabicDuration().temporalAmount();

			return of(  ArabicDate
								.of(arabicDate.calendar(),
									plus ? arabicDate.temporal().plus(period) : arabicDate.temporal().minus(period)),
						ArabicTime
								.of(arabicTime.zoneOrOffset(),
									plus ?
											arabicTime.temporal().plus(duration) :
											arabicTime.temporal().minus(duration)),
						plus ? this.temporal.plus(period).plus(duration) : this.temporal.minus(period).minus(duration)
			);
		}
		else {
			var period = arabicTemporalAmount.temporalAmount();

			return of(  ArabicDate
								.of(arabicDate.calendar(),
									plus ? arabicDate.temporal().plus(period) : arabicDate.temporal().minus(period)),
						arabicTime,
						plus ? this.temporal.plus(period) : this.temporal.minus(period)
			);
		}
	}

	/**
	 * Returns a string representation of this {@code ArabicDateTime} in the format:
	 * "date time".
	 *
	 * <p>Both {@link ArabicDate} and {@link ArabicTime} components are included.</p>
	 *
	 * @return a formatted string representing the Arabic date-time
	 */
	@Override
	public String toString() {

		String sb = arabicDate + " " + arabicTime;

		return sb.trim();
	}
}
