package org.daiitech.naftah.utils.time;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.chrono.Chronology;
import java.time.temporal.Temporal;
import java.util.Objects;
import java.util.function.Supplier;

import org.daiitech.naftah.builtin.time.ArabicDate;
import org.daiitech.naftah.builtin.time.ArabicTime;
import org.daiitech.naftah.errors.NaftahBugError;

import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;
import static org.daiitech.naftah.utils.time.ChronologyUtils.HIJRAH_CHRONOLOGY;
import static org.daiitech.naftah.utils.time.Constants.PM_SHORT;

/**
 * Utility class for creating and manipulating Java {@link Temporal} instances
 * from Arabic date and time components.
 *
 * <p>Provides methods to create:
 * <ul>
 * <li>{@link LocalTime}, {@link OffsetTime}, and {@link ZonedDateTime} from {@link ArabicTime.Time}</li>
 * <li>{@link ChronoLocalDate} from Arabic date components and {@link Chronology}</li>
 * <li>{@link LocalDateTime}, {@link OffsetDateTime}, and {@link ZonedDateTime} from Arabic date-time components</li>
 * </ul>
 *
 * <p>Handles conversion between 12-hour and 24-hour formats, applies AM/PM logic,
 * and supports both ISO and Hijrah (Islamic) chronologies.</p>
 *
 * @author Chakib Daii
 */
public final class TemporalUtils {
	/**
	 * Private constructor to prevent instantiation.
	 * Always throws a {@link NaftahBugError} when called.
	 */
	private TemporalUtils() {
		throw newNaftahBugInvalidUsageError();
	}

	/**
	 * Creates a {@link Temporal} representing the given Arabic time,
	 * optionally including a time zone or offset.
	 *
	 * @param time         the Arabic time component
	 * @param zoneOrOffset the optional time zone or offset
	 * @return a Temporal representing the time, either {@link LocalTime},
	 * {@link OffsetTime}, or {@link ZonedDateTime} depending on the zone/offset
	 */
	public static Temporal createTime(ArabicTime.Time time, ArabicTime.ZoneOrOffset zoneOrOffset) {
		return createTime(
				time.getHour24(),
				time.minute(),
				time.second(),
				time.nano(),
				Objects.nonNull(zoneOrOffset) ? zoneOrOffset.zoneId() : null
		);
	}

	/**
	 * Creates a {@link Temporal} representing a time with explicit components.
	 *
	 * @param hour24 the hour in 24-hour format
	 * @param minute the minute of the hour, may be null
	 * @param second the second of the minute, may be null
	 * @param nano   the nanosecond part, may be null
	 * @param zoneId the optional {@link ZoneId}, may be null
	 * @return a {@link Temporal} representing the specified time
	 */
	public static Temporal createTime(
			int hour24,
			Integer minute,
			Integer second,
			Integer nano,
			ZoneId zoneId) {

		// Handle minutes, seconds and nanos
		int minuteValue = Objects.nonNull(minute) ? minute : 0;
		int secondValue = Objects.nonNull(second) ? second : 0;
		int nanoValue = Objects.nonNull(nano) ? nano : 0;

		// Create LocalTime
		LocalTime time = LocalTime
				.of(hour24,
					minuteValue,
					secondValue,
					nanoValue);

		return createTime(time, zoneId);
	}

	/**
	 * Returns the current {@link Temporal} representing the time in the specified zone or offset.
	 *
	 * @param zoneOrOffset the optional {@link ArabicTime.ZoneOrOffset}, may be null
	 * @return a {@link Temporal} representing the current time in the given zone or offset
	 */
	public static Temporal currentTime(ArabicTime.ZoneOrOffset zoneOrOffset) {
		return currentTime(
				Objects.nonNull(zoneOrOffset) ? zoneOrOffset.zoneId() : null
		);
	}

	/**
	 * Returns the current {@link Temporal} representing the time in the specified {@link ZoneId}.
	 *
	 * @param zoneId the optional {@link ZoneId}, may be null
	 * @return a {@link Temporal} representing the current time in the given zone
	 */
	public static Temporal currentTime(ZoneId zoneId) {
		return createTime(LocalTime.now(), zoneId);
	}

	/**
	 * Creates a {@link Temporal} from a {@link LocalTime} and an optional {@link ZoneId}.
	 *
	 * <p>If a {@link ZoneId} is provided:
	 * <ul>
	 * <li>If it is a {@link ZoneOffset}, returns an {@link OffsetTime}.</li>
	 * <li>Otherwise, returns a {@link ZonedDateTime} anchored to today in that zone.</li>
	 * </ul>
	 * </p>
	 *
	 * <p>If no {@link ZoneId} is provided, returns the {@link LocalTime} as-is.</p>
	 *
	 * @param time   the {@link LocalTime} to wrap
	 * @param zoneId the optional {@link ZoneId}, may be null
	 * @return a {@link Temporal} representing the given time with the zone applied if present
	 */
	public static Temporal createTime(LocalTime time, ZoneId zoneId) {
		// If time + zoneId
		if (Objects.nonNull(zoneId)) {
			if (zoneId instanceof ZoneOffset offset) {
				// If time + offset -> OffsetTime
				return OffsetTime.of(time, offset);
			}
			// If time + zoneId → ZonedDateTime (anchored to today in that zone)
			return createOffsetTime(time, zoneId);
		}

		// If no zone/offset
		return time;
	}

	/**
	 * Creates a {@link ChronoLocalDate} from the given day, month, year, and chronology.
	 *
	 * @param day        the day of the month
	 * @param monthValue the month value (1–12)
	 * @param year       the year
	 * @param chronology the chronology to use (e.g., ISO or Hijrah)
	 * @return a {@link ChronoLocalDate} representing the specified date
	 */
	public static ChronoLocalDate createDate(
			int day,
			int monthValue,
			int year,
			Chronology chronology) {
		// Create ChronoLocalDate from chronology and date parts
		return chronology.date(year, monthValue, day);
	}

	/**
	 * Returns the current {@link Temporal} for the specified {@link ArabicDate.Calendar}
	 * and optional {@link ArabicTime.ZoneOrOffset}.
	 *
	 * @param calendar     the Arabic calendar
	 * @param zoneOrOffset the optional zone or offset, may be null
	 * @return a {@link Temporal} representing the current date in the given calendar and zone
	 */
	public static Temporal currentDate(ArabicDate.Calendar calendar, ArabicTime.ZoneOrOffset zoneOrOffset) {
		return currentDate(
				calendar.chronology(),
				Objects.nonNull(zoneOrOffset) ? zoneOrOffset.zoneId() : null
		);
	}

	/**
	 * Returns the current {@link ChronoLocalDate} for the specified {@link Chronology}
	 * and optional {@link ZoneId}.
	 *
	 * @param chronology the chronology to use (e.g., ISO or Hijrah)
	 * @param zoneId     the optional {@link ZoneId}, may be null
	 * @return a {@link ChronoLocalDate} representing the current date in the given chronology and zone
	 */
	public static ChronoLocalDate currentDate(Chronology chronology, ZoneId zoneId) {
		// If date + zoneId
		if (Objects.nonNull(zoneId)) {
			return chronology.dateNow(zoneId);
		}

		return chronology.dateNow();
	}

	/**
	 * Creates a {@link Temporal} representing the given Arabic date and time,
	 * optionally including a time zone or offset.
	 *
	 * @param date         the Arabic date component
	 * @param calendar     the Arabic calendar component
	 * @param time         the Arabic time component
	 * @param zoneOrOffset the optional time zone or offset
	 * @return a Temporal representing the date and time
	 */
	public static Temporal createDateTime(ArabicDate.Date date,
										  ArabicDate.Calendar calendar,
										  ArabicTime.Time time,
										  ArabicTime.ZoneOrOffset zoneOrOffset) {
		return createDateTime(
				date.day(),
				date.monthValue(),
				date.year(),
				calendar.chronology(),
				time.getHour24(),
				time.minute(),
				time.second(),
				time.nano(),
				Objects.nonNull(zoneOrOffset) ? zoneOrOffset.zoneId() : null
		);
	}

	/**
	 * Creates a {@link Temporal} representing a date and time with optional time zone or offset.
	 *
	 * <p>This method constructs the appropriate temporal object based on the inputs:
	 * <ul>
	 * <li>If a {@link ZoneOffset} is provided, returns an {@link java.time.OffsetDateTime}.</li>
	 * <li>If a {@link ZoneId} is provided, returns a {@link java.time.ZonedDateTime}.</li>
	 * <li>If no zone/offset is provided, returns a {@link java.time.LocalDateTime}.</li>
	 * </ul>
	 *
	 * <p>Supports both ISO and non-ISO chronologies (e.g., Hijrah) and applies the correct conversions.</p>
	 *
	 * @param day        the day of the month
	 * @param monthValue the month (1–12)
	 * @param year       the year
	 * @param chronology the chronology to use (ISO, Hijrah, etc.)
	 * @param hour24     the hour in 24-hour format
	 * @param minute     the minute of the hour, may be null
	 * @param second     the second of the minute, may be null
	 * @param nano       the nanosecond fraction, may be null
	 * @param zoneId     the optional {@link ZoneId} or {@link ZoneOffset}, may be null
	 * @return a {@link Temporal} representing the date and time with optional zone/offset
	 */
	public static Temporal createDateTime(
			int day,
			int monthValue,
			int year,
			Chronology chronology,
			int hour24,
			Integer minute,
			Integer second,
			Integer nano,
			ZoneId zoneId) {

		ChronoLocalDate date = createDate(day, monthValue, year, chronology);

		// Handle minutes, seconds and nanos
		int minuteValue = Objects.nonNull(minute) ? minute : 0;
		int secondValue = Objects.nonNull(second) ? second : 0;
		int nanoValue = Objects.nonNull(nano) ? nano : 0;

		// Create LocalTime
		LocalTime time = LocalTime
				.of(hour24,
					minuteValue,
					secondValue,
					nanoValue);


		// If date + time + zoneId
		if (Objects.nonNull(zoneId)) {
			// If date + time + offset -> OffsetDateTime
			if (zoneId instanceof ZoneOffset offset) {
				return createOffsetDateTime(chronology,
											() -> date.atTime(time),
											() -> LocalDateTime
													.of(year,
														monthValue,
														day,
														hour24,
														minuteValue,
														secondValue,
														nanoValue),
											offset);
			}
			// If date + time + zoneId -> ZonedDateTime
			return createZonedDateTime(chronology,
									   () -> date.atTime(time),
									   () -> LocalDateTime
											   .of(year,
												   monthValue,
												   day,
												   hour24,
												   minuteValue,
												   secondValue,
												   nanoValue),
									   zoneId);
		}

		// If date + time no zone/offset
		return createLocalDateTime(chronology,
								   () -> date.atTime(time),
								   () -> LocalDateTime
										   .of(year,
											   monthValue,
											   day,
											   hour24,
											   minuteValue,
											   secondValue,
											   nanoValue));
	}

	/**
	 * Returns {@code true} if the given AM/PM marker represents PM.
	 *
	 * @param ampm the AM/PM marker string
	 * @return true if PM, false if AM
	 */
	public static boolean isPM(String ampm) {
		return ampm.startsWith(PM_SHORT);
	}

	/**
	 * Creates an {@link OffsetTime} from a {@link LocalTime} and {@link ZoneId}.
	 *
	 * <p>The actual offset is determined for the given zone at a fixed epoch date.</p>
	 *
	 * @param time   the local time
	 * @param zoneId the zone to apply
	 * @return an OffsetTime representing the time at the given zone offset
	 */
	public static OffsetTime createOffsetTime(LocalTime time, ZoneId zoneId) {
		// get instant using a fixed date (epoch day)
		LocalDate epochDay = LocalDate.ofEpochDay(0);
		Instant instant = time.atDate(epochDay).atZone(ZoneOffset.UTC).toInstant();

		// Get the actual offset for this zone at the instant
		ZoneOffset offset = zoneId.getRules().getOffset(instant);

		return OffsetTime.of(time, offset);
	}

	/**
	 * Creates a {@link LocalDateTime} from a chronology and suppliers for ISO and non-ISO dates.
	 *
	 * <p>For Hijrah chronology, converts to ISO {@link LocalDateTime}; otherwise uses ISO directly.</p>
	 *
	 * @param chronology                  the chronology of the date
	 * @param chronoLocalDateTimeSupplier supplier for ChronoLocalDateTime
	 * @param localDateTimeSupplier       supplier for ISO LocalDateTime
	 * @return the resulting LocalDateTime
	 */
	public static LocalDateTime createLocalDateTime(
			Chronology chronology,
			Supplier<ChronoLocalDateTime<?>> chronoLocalDateTimeSupplier,
			Supplier<LocalDateTime> localDateTimeSupplier) {
		if (HIJRAH_CHRONOLOGY.equals(chronology)) {
			// Non-ISO chronology (e.g. Hijri) to ISO LocalDateTime
			return LocalDateTime.from(chronoLocalDateTimeSupplier.get());
		}
		else {
			// For ISO and other non-ISO chronology fallback to ISO
			return localDateTimeSupplier.get();
		}
	}

	/**
	 * Creates an {@link OffsetDateTime} from a chronology, suppliers, and a {@link ZoneOffset}.
	 *
	 * <p>For Hijrah chronology, converts to ISO and applies the offset; otherwise uses ISO directly.</p>
	 *
	 * @param chronology                  the chronology of the date
	 * @param chronoLocalDateTimeSupplier supplier for ChronoLocalDateTime
	 * @param localDateTimeSupplier       supplier for ISO LocalDateTime
	 * @param offset                      the zone offset to apply
	 * @return the resulting OffsetDateTime
	 */
	public static OffsetDateTime createOffsetDateTime(
			Chronology chronology,
			Supplier<ChronoLocalDateTime<?>> chronoLocalDateTimeSupplier,
			Supplier<LocalDateTime> localDateTimeSupplier,
			ZoneOffset offset) {
		if (HIJRAH_CHRONOLOGY.equals(chronology)) {
			// Non-ISO chronology (e.g. Hijri)
			return OffsetDateTime
					.from(chronoLocalDateTimeSupplier
								  .get()
								  .atZone(ZoneOffset.UTC))
					.withOffsetSameInstant(offset);
		}
		else {
			// For ISO and other non-ISO chronology fallback to ISO
			return OffsetDateTime.of(localDateTimeSupplier.get(), offset);
		}
	}

	/**
	 * Creates a {@link ZonedDateTime} from a chronology, suppliers, and a {@link ZoneId}.
	 *
	 * <p>For Hijrah chronology, converts to ISO and applies the zone; otherwise uses ISO directly.</p>
	 *
	 * @param chronology                  the chronology of the date
	 * @param chronoLocalDateTimeSupplier supplier for ChronoLocalDateTime
	 * @param localDateTimeSupplier       supplier for ISO LocalDateTime
	 * @param zoneId                      the time zone to apply
	 * @return the resulting ZonedDateTime
	 */
	public static ZonedDateTime createZonedDateTime(
			Chronology chronology,
			Supplier<ChronoLocalDateTime<?>> chronoLocalDateTimeSupplier,
			Supplier<LocalDateTime> localDateTimeSupplier,
			ZoneId zoneId) {
		if (HIJRAH_CHRONOLOGY.equals(chronology)) {
			// Non-ISO chronology (e.g. Hijri)
			return ZonedDateTime.from(chronoLocalDateTimeSupplier.get().atZone(zoneId));
		}
		else {
			// For ISO and other non-ISO chronology fallback to ISO
			return ZonedDateTime.of(localDateTimeSupplier.get(), zoneId);
		}
	}

	/**
	 * Converts an hour and AM/PM indicator to 24-hour format.
	 *
	 * <p>If the hour is in 12-hour format, AM/PM is applied to produce a 24-hour value.</p>
	 *
	 * @param hour the hour (1–12 or 0–23)
	 * @param isPM true if PM, false if AM, null if 24-hour format
	 * @return the hour in 24-hour format
	 */
	public static int getHour24(Integer hour, Boolean isPM) {
		int hour24 = Objects.nonNull(hour) ? hour : 0;

		if (Objects.nonNull(isPM)) {
			if (isPM) {
				// PM case
				if (hour24 < 12) {
					// 1 PM → 13, 11 PM → 23
					hour24 += 12;
				}
			}
			else {
				// AM case
				if (hour24 == 12) {
					// 12 AM → 0
					hour24 = 0;
				}
			}
		}

		return hour24;
	}

	/**
	 * Parses the fractional part after seconds as milliseconds.
	 *
	 * <p>The fraction must contain at most 3 digits:
	 * <ul>
	 * <li>{@code .5   -> 500 ms}</li>
	 * <li>{@code .25  -> 250 ms}</li>
	 * <li>{@code .125 -> 125 ms}</li>
	 * </ul>
	 *
	 * @param fraction the fractional part without the dot
	 * @return milliseconds value (0–999)
	 * @throws IllegalArgumentException if the fraction exceeds 3 digits
	 */
	public static int parseMillisFraction(String fraction) {
		if (fraction == null || fraction.isEmpty()) {
			return 0;
		}

		if (fraction.length() > 3) {
			throw new IllegalArgumentException(
					"الجزء العشري بعد الثانية يجب ألا يتجاوز 3 أرقام (ملي ثانية)"
			);
		}

		// Right-pad to 3 digits: "5" -> "500", "25" -> "250"
		String millis = String.format("%-3s", fraction).replace(' ', '0');

		return Integer.parseInt(millis);
	}


	/**
	 * Parses the fractional part after seconds into nanoseconds.
	 *
	 * <p>Supported formats:
	 * <ul>
	 * <li>1–3 digits → milliseconds</li>
	 * <li>4–9 digits → nanoseconds</li>
	 * </ul>
	 *
	 * @param fraction the fractional part without the dot
	 * @return nanoseconds value (0–999,999,999)
	 * @throws IllegalArgumentException if the fraction exceeds 9 digits
	 */
	public static int parseFractionToNanos(String fraction) {
		if (fraction == null || fraction.isEmpty()) {
			return 0;
		}

		int length = fraction.length();

		if (length > 9) {
			throw new IllegalArgumentException(
					"الجزء العشري بعد الثانية يجب ألا يتجاوز 9 أرقام (نانو ثانية)"
			);
		}

		// Right-pad to 9 digits
		String nanos = String.format("%-9s", fraction).replace(' ', '0');

		return Integer.parseInt(nanos);
	}


}
