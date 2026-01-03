package org.daiitech.naftah.utils.time;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.chrono.Chronology;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Objects;
import java.util.function.Supplier;

import org.daiitech.naftah.builtin.time.ArabicDate;
import org.daiitech.naftah.builtin.time.ArabicTime;
import org.daiitech.naftah.builtin.utils.tuple.NTuple;
import org.daiitech.naftah.errors.NaftahBugError;

import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;
import static org.daiitech.naftah.utils.time.ChronoConversionUtils.toIsoDate;
import static org.daiitech.naftah.utils.time.ChronoConversionUtils.toIsoDateTime;
import static org.daiitech.naftah.utils.time.ChronoConversionUtils.toIsoOffsetDateTime;
import static org.daiitech.naftah.utils.time.ChronoConversionUtils.toIsoZonedDateTime;
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
	 *         {@link OffsetTime}, or {@link ZonedDateTime} depending on the zone/offset
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
	 * Creates a {@link LocalTime} and a corresponding {@link Temporal}
	 * from the total number of nanoseconds since midnight.
	 * <p>
	 * The returned tuple contains:
	 * <ul>
	 * <li>The computed {@link LocalTime}</li>
	 * <li>A {@link Temporal} that may be {@link LocalTime},
	 * {@link OffsetTime}, or {@link ZonedDateTime} depending on the zone</li>
	 * </ul>
	 *
	 * @param nanoOfDay    the nano-of-day (0–86,399,999,999,999)
	 * @param zoneOrOffset the optional time zone or offset, may be null
	 * @return an {@link NTuple} containing the {@link LocalTime} and corresponding {@link Temporal}
	 */
	public static NTuple createTimeOfNanoOfDay(long nanoOfDay, ArabicTime.ZoneOrOffset zoneOrOffset) {
		// Create LocalTime
		LocalTime time = LocalTime.ofNanoOfDay(nanoOfDay);
		Temporal temporal = createTime(time, Objects.nonNull(zoneOrOffset) ? zoneOrOffset.zoneId() : null);
		return NTuple.of(time, temporal);
	}


	/**
	 * Creates a {@link LocalTime} and a corresponding {@link Temporal}
	 * from the total number of seconds since midnight.
	 * <p>
	 * The returned tuple contains:
	 * <ul>
	 * <li>The computed {@link LocalTime}</li>
	 * <li>A {@link Temporal} that may be {@link LocalTime},
	 * {@link OffsetTime}, or {@link ZonedDateTime} depending on the zone</li>
	 * </ul>
	 *
	 * @param secondOfDay  the second-of-day (0–86,399)
	 * @param zoneOrOffset the optional time zone or offset, may be null
	 * @return an {@link NTuple} containing the {@link LocalTime} and corresponding {@link Temporal}
	 */
	public static NTuple createTimeOfSecondOfDay(long secondOfDay, ArabicTime.ZoneOrOffset zoneOrOffset) {
		// Create LocalTime
		LocalTime time = LocalTime.ofSecondOfDay(secondOfDay);
		Temporal temporal = createTime(time, Objects.nonNull(zoneOrOffset) ? zoneOrOffset.zoneId() : null);
		return NTuple.of(time, temporal);
	}

	/**
	 * Creates a {@link Temporal} representing a time using explicit components
	 * and an optional time zone or offset.
	 * <p>
	 * Any null minute, second, or nanosecond value will default to {@code 0}.
	 *
	 * @param hour24       the hour in 24-hour format (0–23)
	 * @param minute       the minute of the hour, may be null
	 * @param second       the second of the minute, may be null
	 * @param nano         the nanosecond part, may be null
	 * @param zoneOrOffset the optional time zone or offset, may be null
	 * @return a {@link Temporal} representing the specified time, never null
	 */
	public static Temporal createTime(
										int hour24,
										Integer minute,
										Integer second,
										Integer nano,
										ArabicTime.ZoneOrOffset zoneOrOffset) {
		return createTime(
							hour24,
							minute,
							second,
							nano,
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
	 * Creates a {@link ChronoLocalDate} using the specified day, month, year, and chronology.
	 * <p>
	 * This method preserves the original chronology, so it can represent dates in
	 * non-ISO calendars such as {@link java.time.chrono.HijrahChronology}.
	 * </p>
	 *
	 * @param day        the day of the month (1–31, depending on month and chronology)
	 * @param monthValue the month number (1–12)
	 * @param year       the year in the specified chronology
	 * @param chronology the {@link Chronology} to use (e.g., {@link java.time.chrono.IsoChronology} or
	 *                   * HijrahChronology)
	 * @return a {@link ChronoLocalDate} representing the specified date in the given chronology
	 */
	private static ChronoLocalDate createChronoLocalDate(
															int day,
															int monthValue,
															int year,
															Chronology chronology) {
		// Create ChronoLocalDate from chronology and date parts
		return chronology.date(year, monthValue, day);
	}

	/**
	 * Creates a {@link LocalDate} (ISO calendar) from the specified day, month, year, and chronology.
	 * <p>
	 * If a non-ISO chronology is provided (e.g., HijrahChronology), the date is converted
	 * to the ISO calendar system. This is useful for storing or processing dates
	 * in a standardized ISO format while preserving the original calendar's meaning.
	 * </p>
	 *
	 * @param day        the day of the month (1–31)
	 * @param monthValue the month number (1–12)
	 * @param year       the year in the specified chronology
	 * @param chronology the {@link Chronology} to use (e.g., ISO or Hijrah)
	 * @return a {@link LocalDate} in the ISO calendar representing the same date
	 *         as the specified chronology
	 */
	public static LocalDate createDate(
										int day,
										int monthValue,
										int year,
										Chronology chronology) {
		return toIsoDate(createChronoLocalDate(day, monthValue, year, chronology));
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
	public static Temporal createDateTime(  ArabicDate.Date date,
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

		ChronoLocalDate date = createChronoLocalDate(day, monthValue, year, chronology);

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
			return createZonedDateTime( chronology,
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
		return createLocalDateTime( chronology,
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
		if (isHijrah(chronology)) {
			// Non-ISO chronology (e.g. Hijri) to ISO LocalDateTime
			return toIsoDateTime(chronoLocalDateTimeSupplier.get());
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
		if (isHijrah(chronology)) {
			// Non-ISO chronology (e.g. Hijri)
			return toIsoOffsetDateTime(chronoLocalDateTimeSupplier.get(), offset);
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
		if (isHijrah(chronology)) {
			// Non-ISO chronology (e.g. Hijri)
			return toIsoZonedDateTime(chronoLocalDateTimeSupplier.get(), zoneId);
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
	 * Computes the temporal difference between two {@link Temporal} instances.
	 * <p>
	 * The result is returned as an {@link NTuple} containing:
	 * </p>
	 * <ul>
	 * <li>a {@link Duration} if the difference is less than 24 hours</li>
	 * <li>a {@link Period} if the difference is date-based only</li>
	 * <li>a {@link Period} and a {@link Duration} if the difference spans
	 * both date and time components</li>
	 * </ul>
	 *
	 * @param start the start temporal point
	 * @param end   the end temporal point
	 * @return an {@link NTuple} containing the computed temporal amount(s)
	 * @throws IllegalArgumentException if the temporal type is not supported
	 */
	public static NTuple between(Temporal start, Temporal end) {
		// Handle date-only case directly
		if (start instanceof LocalDate d1 && end instanceof LocalDate d2) {
			return NTuple.of(Period.between(d1, d2));
		}

		// Normalize to LocalDateTime
		LocalDateTime startDT = toLocalDateTime(start);
		LocalDateTime endDT = toLocalDateTime(end);

		// Compute total difference in seconds
		long secondsDiff = ChronoUnit.SECONDS.between(startDT, endDT);

		// Short duration (<24h) → Duration
		if (Math.abs(secondsDiff) < 24 * 3600) {
			return NTuple.of(Duration.ofSeconds(secondsDiff));
		}

		// Compute full days
		Period period = Period.between(startDT.toLocalDate(), endDT.toLocalDate());

		// Compute remaining time part
		Duration timeDuration = Duration.between(startDT.toLocalTime(), endDT.toLocalTime());

		// Adjust if negative
		if (timeDuration.isNegative()) {
			timeDuration = timeDuration.plusDays(1);
			period = period.minusDays(1);
		}

		// Only period → return period
		if (timeDuration.isZero()) {
			return NTuple.of(period);
		}

		// Mixed period + duration
		return NTuple.of(period, timeDuration);
	}

	/**
	 * Converts a {@link Temporal} instance to a {@link LocalDateTime}.
	 * <p>
	 * Supported types:
	 * {@link LocalDateTime}, {@link LocalDate}, {@link LocalTime},
	 * {@link ZonedDateTime}, {@link OffsetDateTime}, and {@link Instant}.
	 * </p>
	 * <p>
	 * Conversion rules:
	 * <ul>
	 * <li>{@link LocalDate} → start of day</li>
	 * <li>{@link LocalTime} → combined with the current system date</li>
	 * <li>{@link ZonedDateTime}/{@link OffsetDateTime} → zone/offset discarded</li>
	 * <li>{@link Instant} → converted using the system default time zone</li>
	 * </ul>
	 * </p>
	 *
	 * @param t the temporal value to convert
	 * @return a {@link LocalDateTime} representation of the temporal value
	 * @throws IllegalArgumentException if the temporal type is not supported
	 */
	private static LocalDateTime toLocalDateTime(Temporal t) {
		if (t instanceof LocalDateTime ldt) {
			return ldt;
		}
		if (t instanceof LocalDate ld) {
			return ld.atStartOfDay();
		}
		if (t instanceof LocalTime lt) {
			return LocalDate.now().atTime(lt);
		}
		if (t instanceof ZonedDateTime zdt) {
			return zdt.toLocalDateTime();
		}
		if (t instanceof OffsetDateTime odt) {
			return odt.toLocalDateTime();
		}
		if (t instanceof Instant instant) {
			return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
		}

		throw new IllegalArgumentException("Unsupported temporal type: " + t.getClass());
	}

	/**
	 * Converts a {@link Temporal} instance to an {@link Instant}.
	 * <p>
	 * Supported types:
	 * {@link Instant}, {@link ZonedDateTime}, {@link OffsetDateTime},
	 * {@link LocalDateTime}, {@link LocalDate}, and {@link LocalTime}.
	 * </p>
	 * <p>
	 * Conversion rules:
	 * <ul>
	 * <li>{@link ZonedDateTime}/{@link OffsetDateTime} → converted directly</li>
	 * <li>{@link LocalDateTime} → interpreted using the system default time zone</li>
	 * <li>{@link LocalDate} → start of day in the system default time zone</li>
	 * <li>{@link LocalTime} → combined with the current system date and zone</li>
	 * </ul>
	 * </p>
	 *
	 * @param t the temporal value to convert
	 * @return an {@link Instant} representing the temporal value
	 * @throws IllegalArgumentException if the temporal type is not supported
	 */
	public static Instant toInstant(Temporal t) {
		if (t instanceof Instant instant) {
			return instant;
		}
		if (t instanceof ZonedDateTime zdt) {
			return zdt.toInstant();
		}
		if (t instanceof OffsetDateTime odt) {
			return odt.toInstant();
		}
		if (t instanceof LocalDateTime ldt) {
			return ldt.atZone(ZoneId.systemDefault()).toInstant();
		}
		if (t instanceof LocalDate ld) {
			return ld.atStartOfDay(ZoneId.systemDefault()).toInstant();
		}
		if (t instanceof LocalTime lt) {
			return LocalDate.now(ZoneId.systemDefault()).atTime(lt).atZone(ZoneId.systemDefault()).toInstant();
		}

		throw new IllegalArgumentException("Unsupported temporal: " + t.getClass());
	}

	/**
	 * Compares two {@link Period} instances by years, then months, then days.
	 * <p>
	 * Comparison is performed lexicographically:
	 * years → months → days.
	 * </p>
	 *
	 * @param p1 the first period to compare
	 * @param p2 the second period to compare
	 * @return a negative integer, zero, or a positive integer as {@code p1}
	 *         is less than, equal to, or greater than {@code p2}
	 */
	public static int compare(Period p1, Period p2) {
		int compare = Integer.compare(p1.getYears(), p2.getYears());
		if (compare == 0) {
			compare = Integer.compare(p1.getMonths(), p2.getMonths());
			if (compare == 0) {
				compare = Integer.compare(p1.getDays(), p2.getDays());
			}
		}
		return compare;
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

	/**
	 * Determines whether the given {@link Chronology} represents the Hijrah (Islamic) calendar.
	 *
	 * <p>This method compares the provided chronology against the system-supported
	 * Hijrah chronology used by the library.</p>
	 *
	 * @param chronology the chronology to test, may be {@code null}
	 * @return {@code true} if the chronology is the Hijrah calendar, {@code false} otherwise
	 */
	private static boolean isHijrah(Chronology chronology) {
		return HIJRAH_CHRONOLOGY.equals(chronology);
	}
}
