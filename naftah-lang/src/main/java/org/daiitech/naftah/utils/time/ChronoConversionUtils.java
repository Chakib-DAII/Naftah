package org.daiitech.naftah.utils.time;

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

import org.daiitech.naftah.errors.NaftahBugError;

import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;

/**
 * Utility class for converting between ISO date/time objects and
 * {@link ChronoLocalDate} / {@link ChronoLocalDateTime} in arbitrary chronologies.
 * <p>
 * Includes conversions to/from {@link LocalDate}, {@link LocalDateTime},
 * {@link OffsetDateTime}, {@link OffsetTime}, and {@link ZonedDateTime}.
 * </p>
 *
 * @author Chakib Daii
 */
public final class ChronoConversionUtils {
	/**
	 * Private constructor to prevent instantiation.
	 * Always throws a {@link NaftahBugError} when called.
	 */
	private ChronoConversionUtils() {
		throw newNaftahBugInvalidUsageError();
	}

	/**
	 * Converts a {@link ChronoLocalDate} to ISO {@link LocalDate}.
	 * <p>
	 * If the input date is in a non-ISO chronology (e.g., Hijrah), the resulting
	 * {@link LocalDate} represents the same point in time but in the ISO calendar system.
	 * </p>
	 *
	 * @param chronoDate the date in any chronology
	 * @return the corresponding ISO {@link LocalDate}
	 */
	public static LocalDate toIsoDate(ChronoLocalDate chronoDate) {
		return LocalDate.from(chronoDate);
	}

	/**
	 * Converts an ISO {@link LocalDate} to a {@link ChronoLocalDate} in the given chronology.
	 *
	 * @param isoDate    the ISO {@link LocalDate}
	 * @param chronology the chronology to use
	 * @return a {@link ChronoLocalDate} in the specified chronology
	 */
	public static ChronoLocalDate toChronoDate(LocalDate isoDate, Chronology chronology) {
		return chronology.date(isoDate);
	}

	/**
	 * Converts a {@link ChronoLocalDateTime} to ISO {@link LocalDateTime}.
	 *
	 * @param chronoDateTime the date-time in any chronology
	 * @return the corresponding ISO {@link LocalDateTime}
	 */
	public static LocalDateTime toIsoDateTime(ChronoLocalDateTime<?> chronoDateTime) {
		return LocalDateTime.from(chronoDateTime);
	}

	/**
	 * Converts an ISO {@link LocalDateTime} to a {@link ChronoLocalDateTime} in the specified chronology.
	 *
	 * @param isoDateTime the ISO {@link LocalDateTime}
	 * @param chronology  the chronology to use
	 * @return a {@link ChronoLocalDateTime} in the specified chronology
	 */
	public static ChronoLocalDateTime<?> toChronoDateTime(
															LocalDateTime isoDateTime,
															Chronology chronology) {
		return chronology.localDateTime(isoDateTime);
	}

	/**
	 * Converts a {@link ChronoLocalDateTime} to {@link OffsetDateTime} using the specified {@link ZoneOffset}.
	 *
	 * @param chronoDateTime the date-time in any chronology
	 * @param offset         the {@link ZoneOffset} to apply
	 * @return an {@link OffsetDateTime} representing the same instant in the given offset
	 */
	public static OffsetDateTime toIsoOffsetDateTime(
														ChronoLocalDateTime<?> chronoDateTime,
														ZoneOffset offset) {

		return OffsetDateTime
				.from(
						chronoDateTime.atZone(ZoneOffset.UTC)
				)
				.withOffsetSameInstant(offset);
	}

	/**
	 * Converts an {@link OffsetDateTime} to a {@link ChronoLocalDateTime} in the specified chronology.
	 *
	 * @param isoDateTime the {@link OffsetDateTime}
	 * @param chronology  the chronology to use
	 * @return a {@link ChronoLocalDateTime} in the specified chronology
	 */
	public static ChronoLocalDateTime<?> toChronoDateTime(
															OffsetDateTime isoDateTime,
															Chronology chronology) {
		return chronology.localDateTime(isoDateTime.toLocalDateTime());
	}

	/**
	 * Converts a {@link ChronoLocalDateTime} to {@link ZonedDateTime} in the specified {@link ZoneId}.
	 *
	 * @param chronoDateTime the date-time in any chronology
	 * @param zoneId         the time zone to apply
	 * @return a {@link ZonedDateTime} representing the same instant in the given zone
	 */
	public static ZonedDateTime toIsoZonedDateTime(
													ChronoLocalDateTime<?> chronoDateTime,
													ZoneId zoneId) {

		return ZonedDateTime
				.from(
						chronoDateTime.atZone(zoneId)
				);
	}


	/**
	 * Converts a {@link ZonedDateTime} to a {@link ChronoLocalDateTime} in the specified chronology.
	 *
	 * @param isoDateTime the {@link ZonedDateTime}
	 * @param chronology  the chronology to use
	 * @return a {@link ChronoLocalDateTime} in the specified chronology
	 */
	public static ChronoLocalDateTime<?> toChronoDateTime(
															ZonedDateTime isoDateTime,
															Chronology chronology) {
		return chronology.localDateTime(isoDateTime.toLocalDateTime());
	}

	/**
	 * Converts any {@link Temporal} to ISO form.
	 * <p>
	 * - ChronoLocalDate → LocalDate<br>
	 * - ChronoLocalDateTime → LocalDateTime<br>
	 * - Other types are returned unchanged.
	 * </p>
	 *
	 * @param temporal the temporal object to convert
	 * @return an ISO {@link Temporal} equivalent, or the original temporal if conversion is not needed
	 */
	public static Temporal toIsoTemporal(Temporal temporal) {

		if (temporal instanceof ChronoLocalDate chronoDate) {
			return toIsoDate(chronoDate);
		}

		if (temporal instanceof ChronoLocalDateTime<?> chronoDateTime) {
			return toIsoDateTime(chronoDateTime);
		}

		return temporal;
	}

	/**
	 * Converts any {@link Temporal} to ISO form and applies a {@link ZoneId} or {@link ZoneOffset}.
	 *
	 * @param temporal       the temporal object to convert
	 * @param zoneIdOrOffset the zone or offset to apply
	 * @return an ISO {@link Temporal} with the zone or offset applied
	 */
	public static Temporal toIsoTemporal(Temporal temporal, ZoneId zoneIdOrOffset) {
		Temporal iso = toIsoTemporal(temporal);

		if (iso instanceof LocalDateTime ldt) {
			if (zoneIdOrOffset instanceof ZoneOffset offset) {
				return OffsetDateTime.of(ldt, offset);
			}
			else if (zoneIdOrOffset != null) {
				return ZonedDateTime.of(ldt, zoneIdOrOffset);
			}
		}

		if (iso instanceof LocalTime lt && zoneIdOrOffset instanceof ZoneOffset offset) {
			return OffsetTime.of(lt, offset);
		}

		return iso;
	}

	/**
	 * Converts any ISO {@link Temporal} back to a {@link ChronoLocalDate} or {@link ChronoLocalDateTime} in the
	 * * specified chronology.
	 *
	 * @param temporal   the ISO temporal
	 * @param chronology the target chronology
	 * @return the temporal in the specified chronology
	 */
	public static Temporal toChronoTemporal(Temporal temporal, Chronology chronology) {
		if (temporal instanceof LocalDate ld) {
			return chronology.date(ld);
		}

		if (temporal instanceof LocalDateTime ldt) {
			return chronology.localDateTime(ldt);
		}

		if (temporal instanceof OffsetDateTime odt) {
			LocalDateTime ldt = odt.toLocalDateTime();
			return chronology.localDateTime(ldt);
		}

		if (temporal instanceof ZonedDateTime zdt) {
			LocalDateTime ldt = zdt.toLocalDateTime();
			return chronology.localDateTime(ldt);
		}

		return temporal;
	}


	/**
	 * Converts any ISO {@link Temporal} back to a {@link ChronoLocalDate} or {@link ChronoLocalDateTime} in the
	 * * specified chronology,
	 * applying a {@link ZoneId} or {@link ZoneOffset} if present.
	 *
	 * @param temporal       the ISO temporal
	 * @param chronology     the target chronology
	 * @param zoneIdOrOffset the zone or offset to apply
	 * @return the temporal in the specified chronology with zone/offset applied
	 */
	public static Temporal toChronoTemporal(Temporal temporal, Chronology chronology, ZoneId zoneIdOrOffset) {
		Temporal chrono = toChronoTemporal(temporal, chronology);

		if (chrono instanceof ChronoLocalDateTime<?> cldt) {
			if (zoneIdOrOffset instanceof ZoneOffset offset) {
				return cldt.atZone(offset);
			}
			else if (zoneIdOrOffset != null) {
				return cldt.atZone(zoneIdOrOffset);
			}
		}

		if (chrono instanceof LocalTime lt && zoneIdOrOffset instanceof ZoneOffset offset) {
			return OffsetTime.of(lt, offset);
		}

		return chrono;
	}
}
