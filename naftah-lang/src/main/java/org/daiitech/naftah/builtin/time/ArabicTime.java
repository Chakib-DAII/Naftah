package org.daiitech.naftah.builtin.time;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.Objects;
import java.util.function.Function;

import org.daiitech.naftah.builtin.utils.ObjectUtils;
import org.daiitech.naftah.utils.time.TemporalUtils;
import org.daiitech.naftah.utils.time.ZoneUtils;

import static org.daiitech.naftah.builtin.utils.ObjectUtils.numberToString;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.padZero;
import static org.daiitech.naftah.utils.time.Constants.ZONE_PREFIX_AR;
import static org.daiitech.naftah.utils.time.ZoneUtils.parseZoneOffset;

/**
 * Represents an Arabic time expression, optionally including a time zone or offset,
 * and a resolved {@link Temporal} representation.
 *
 * <p>Implemented as a record with the components:
 * <ul>
 * <li>{@link Time} – the hour, minute, optional second and nanosecond, and AM/PM indicator</li>
 * <li>{@link ZoneOrOffset} – optional time zone or numeric offset</li>
 * <li>{@link Temporal} – the resolved temporal representation</li>
 * </ul>
 *
 * <p>This record implements {@link ArabicTemporalPoint} and is typically produced
 * after parsing Arabic time expressions.</p>
 *
 * @param time         the time component
 * @param zoneOrOffset the optional time zone or offset component
 * @param temporal     the resolved {@link Temporal} representation
 * @author Chakib Daii
 */
public record ArabicTime(
		Time time,
		ZoneOrOffset zoneOrOffset,
		Temporal temporal
) implements ArabicTemporalPoint, TimeSupport {

	/**
	 * Obtains the current time using the system default time zone.
	 *
	 * <p>
	 * This method is equivalent to invoking {@code now(null)}.
	 * </p>
	 *
	 * @return the current {@code ArabicTime}
	 */
	public static ArabicTime now() {
		return now(null);
	}

	/**
	 * Obtains the current time using the specified zone or offset.
	 *
	 * <p>
	 * If {@code zoneOrOffset} is {@code null}, the system default
	 * time zone is used.
	 * </p>
	 *
	 * @param zoneOrOffset the zone or offset to use, or {@code null}
	 *                     to use the system default
	 * @return the current {@code ArabicTime}
	 */
	public static ArabicTime now(ArabicTime.ZoneOrOffset zoneOrOffset) {
		return ArabicTime
				.of(zoneOrOffset,
					TemporalUtils
							.currentTime(
											zoneOrOffset
							)
				);
	}

	/**
	 * Creates an {@code ArabicTime} instance from hour and minute.
	 *
	 * <p>Seconds and nanoseconds default to {@code 0}.</p>
	 *
	 * @param hour   the hour-of-day (0–23)
	 * @param minute the minute-of-hour (0–59)
	 * @return a new {@code ArabicTime} instance
	 */
	public static ArabicTime of(int hour, int minute) {
		return of(hour, minute, 0, 0);
	}


	/**
	 * Creates an {@code ArabicTime} instance from hour and minute,
	 * optionally associated with a time zone or offset.
	 * <p>
	 * The second and nanosecond fields are set to {@code 0}.
	 *
	 * @param hour         the hour-of-day (0–23)
	 * @param minute       the minute-of-hour (0–59)
	 * @param zoneOrOffset the optional time zone or offset, may be null
	 * @return a new {@code ArabicTime} instance, not null
	 */
	public static ArabicTime of(int hour,
								int minute,
								ArabicTime.ZoneOrOffset zoneOrOffset) {
		return of(hour, minute, 0, 0, zoneOrOffset);
	}


	/**
	 * Creates an {@code ArabicTime} instance from hour, minute, and second.
	 *
	 * <p>Nano-of-second defaults to {@code 0}.</p>
	 *
	 * @param hour   the hour-of-day (0–23)
	 * @param minute the minute-of-hour (0–59)
	 * @param second the second-of-minute (0–59)
	 * @return a new {@code ArabicTime} instance
	 */
	public static ArabicTime of(int hour, int minute, int second) {
		return of(hour, minute, second, 0);
	}

	/**
	 * Creates an {@code ArabicTime} instance from hour, minute, and second,
	 * optionally associated with a time zone or offset.
	 * <p>
	 * The nanosecond field is set to {@code 0}.
	 *
	 * @param hour         the hour-of-day (0–23)
	 * @param minute       the minute-of-hour (0–59)
	 * @param second       the second-of-minute (0–59)
	 * @param zoneOrOffset the optional time zone or offset, may be null
	 * @return a new {@code ArabicTime} instance, not null
	 */
	public static ArabicTime of(int hour,
								int minute,
								int second,
								ArabicTime.ZoneOrOffset zoneOrOffset) {
		return of(hour, minute, second, 0, zoneOrOffset);
	}

	/**
	 * Creates an {@code ArabicTime} instance from hour, minute, second, and nanosecond.
	 *
	 * @param hour         the hour-of-day (0–23)
	 * @param minute       the minute-of-hour (0–59)
	 * @param second       the second-of-minute (0–59)
	 * @param nanoOfSecond the nanosecond-of-second (0–999,999,999)
	 * @return a new {@code ArabicTime} instance
	 */
	public static ArabicTime of(int hour,
								int minute,
								int second,
								int nanoOfSecond) {
		return of(hour, minute, second, nanoOfSecond, null);
	}

	/**
	 * Creates an {@code ArabicTime} instance from hour, minute, second,
	 * and nanosecond, optionally associated with a time zone or offset.
	 *
	 * @param hour         the hour-of-day (0–23)
	 * @param minute       the minute-of-hour (0–59)
	 * @param second       the second-of-minute (0–59)
	 * @param nanoOfSecond the nanosecond-of-second (0–999,999,999)
	 * @param zoneOrOffset the optional time zone or offset, may be null
	 * @return a new {@code ArabicTime} instance, not null
	 */
	public static ArabicTime of(int hour,
								int minute,
								int second,
								int nanoOfSecond,
								ArabicTime.ZoneOrOffset zoneOrOffset) {
		var time = Time.of(hour, minute, second, nanoOfSecond, null);
		var temporal = TemporalUtils.createTime(hour, minute, second, nanoOfSecond, zoneOrOffset);
		return of(time, zoneOrOffset, temporal);
	}

	/**
	 * Creates an {@code ArabicTime} instance from the total number of seconds
	 * since midnight.
	 *
	 * @param secondOfDay the second-of-day (0–86,399)
	 * @return a new {@code ArabicTime} instance
	 */
	public static ArabicTime ofSecondOfDay(long secondOfDay) {
		return ofSecondOfDay(secondOfDay, null);
	}

	/**
	 * Creates an {@code ArabicTime} instance from the total number of seconds
	 * since midnight, optionally associated with a time zone or offset.
	 *
	 * @param secondOfDay  the second-of-day (0–86,399)
	 * @param zoneOrOffset the optional time zone or offset, may be null
	 * @return a new {@code ArabicTime} instance, not null
	 */
	public static ArabicTime ofSecondOfDay(long secondOfDay, ArabicTime.ZoneOrOffset zoneOrOffset) {
		var timeAndTemporal = TemporalUtils.createTimeOfSecondOfDay(secondOfDay, zoneOrOffset);
		LocalTime lt = (LocalTime) timeAndTemporal.get(0);
		Temporal temporal = (Temporal) timeAndTemporal.get(1);
		var time = Time.of(lt.getHour(), lt.getMinute(), lt.getSecond(), lt.getNano(), null);
		return of(time, zoneOrOffset, temporal);
	}

	/**
	 * Creates an {@code ArabicTime} instance from the total number of nanoseconds
	 * since midnight.
	 *
	 * @param nanoOfDay the nano-of-day (0–86,399,999,999,999)
	 * @return a new {@code ArabicTime} instance
	 */
	public static ArabicTime ofNanoOfDay(long nanoOfDay) {
		return ofNanoOfDay(nanoOfDay, null);
	}

	/**
	 * Creates an {@code ArabicTime} instance from the total number of nanoseconds
	 * since midnight, optionally associated with a time zone or offset.
	 *
	 * @param nanoOfDay    the nano-of-day (0–86,399,999,999,999)
	 * @param zoneOrOffset the optional time zone or offset, may be null
	 * @return a new {@code ArabicTime} instance, not null
	 */
	public static ArabicTime ofNanoOfDay(long nanoOfDay, ArabicTime.ZoneOrOffset zoneOrOffset) {
		var timeAndTemporal = TemporalUtils.createTimeOfNanoOfDay(nanoOfDay, zoneOrOffset);
		LocalTime lt = (LocalTime) timeAndTemporal.get(0);
		Temporal temporal = (Temporal) timeAndTemporal.get(1);
		var time = Time.of(lt.getHour(), lt.getMinute(), lt.getSecond(), lt.getNano(), null);
		return of(time, zoneOrOffset, temporal);
	}

	/**
	 * Creates a new {@code ArabicTime} instance using explicit components.
	 *
	 * <p>This factory method is typically used when the parsed time,
	 * optional zone/offset, and the resolved {@link Temporal} representation
	 * are already available.</p>
	 *
	 * @param time         the logical time component (hour, minute, second, nano, AM/PM)
	 * @param zoneOrOffset the optional time zone or offset information, may be {@code null}
	 * @param temporal     the resolved {@link Temporal} representation backing this time
	 * @return a new {@code ArabicTime} instance
	 */
	public static ArabicTime of(
								Time time,
								ZoneOrOffset zoneOrOffset,
								Temporal temporal) {
		return new ArabicTime(time, zoneOrOffset, temporal);
	}

	/**
	 * Creates a new {@code ArabicTime} instance using explicit components
	 * and resolves the backing {@link Temporal} automatically.
	 *
	 * @param time         the logical time component, not null
	 * @param zoneOrOffset the optional time zone or offset, may be null
	 * @return a new {@code ArabicTime} instance, not null
	 */
	public static ArabicTime of(
								Time time,
								ZoneOrOffset zoneOrOffset) {
		return new ArabicTime(  time,
								zoneOrOffset,
								TemporalUtils
										.createTime(
													time,
													zoneOrOffset
										));
	}


	/**
	 * Creates a new {@code ArabicTime} instance by extracting time fields
	 * directly from a {@link Temporal} object.
	 *
	 * <p>The following fields are resolved if supported by the temporal:
	 * <ul>
	 * <li>{@link ChronoField#HOUR_OF_DAY}</li>
	 * <li>{@link ChronoField#MINUTE_OF_HOUR}</li>
	 * <li>{@link ChronoField#SECOND_OF_MINUTE}</li>
	 * <li>{@link ChronoField#NANO_OF_SECOND}</li>
	 * </ul>
	 * Unsupported fields default to {@code 0}.</p>
	 *
	 * <p>The resulting {@link ArabicTime.Time} instance is created using
	 * a 24-hour clock representation, with no AM/PM marker.</p>
	 *
	 * @param zoneOrOffset the optional time zone or offset information, may be {@code null}
	 * @param temporal     the temporal object from which time fields are extracted
	 * @return a new {@code ArabicTime} instance
	 * @throws IllegalArgumentException if the temporal cannot represent a valid time
	 */
	public static ArabicTime of(
								ZoneOrOffset zoneOrOffset,
								Temporal temporal) {
		int hour = temporal.isSupported(ChronoField.HOUR_OF_DAY) ? temporal.get(ChronoField.HOUR_OF_DAY) : 0;
		int minute = temporal.isSupported(ChronoField.MINUTE_OF_HOUR) ? temporal.get(ChronoField.MINUTE_OF_HOUR) : 0;
		int second = temporal.isSupported(ChronoField.SECOND_OF_MINUTE) ?
				temporal.get(ChronoField.SECOND_OF_MINUTE) :
				0;
		int nano = temporal.isSupported(ChronoField.NANO_OF_SECOND) ? temporal.get(ChronoField.NANO_OF_SECOND) : 0;

		var time = ArabicTime.Time.of(hour, minute, second, nano, null);

		return new ArabicTime(time, zoneOrOffset, temporal);
	}

	/**
	 * Gets the hour-of-day field.
	 *
	 * @return the hour-of-day, from 0 to 23
	 */
	@Override
	public int getHour() {
		return time.getHour24();
	}

	/**
	 * Gets the minute-of-hour field.
	 *
	 * @return the minute-of-hour, from 0 to 59
	 */
	@Override
	public int getMinute() {
		return time.minute;
	}

	/**
	 * Gets the second-of-minute field.
	 *
	 * @return the second-of-minute, from 0 to 59
	 */
	@Override
	public int getSecond() {
		return time.second;
	}

	/**
	 * Gets the nano-of-second field.
	 *
	 * @return the nano-of-second, from 0 to 999,999,999
	 */
	@Override
	public int getNano() {
		return time.nano;
	}

	/**
	 * Returns a new {@code ArabicTime} obtained by adding the given Arabic temporal
	 * amount to this time.
	 *
	 * @param arabicTemporalAmount the temporal amount to add
	 * @return a new {@code ArabicTime} instance
	 */
	@Override
	public ArabicTime plus(ArabicTemporalAmount arabicTemporalAmount) {
		return compute(arabicTemporalAmount, this.temporal::plus);
	}

	/**
	 * Returns a new {@code ArabicTime} with the specified number of hours added.
	 *
	 * <p>If {@code hoursToAdd} is zero, this instance is returned unchanged.</p>
	 *
	 * @param hoursToAdd the number of hours to add, may be negative
	 * @return a new {@code ArabicTime} instance with the hours added
	 */
	@Override
	public ArabicTime plusHours(long hoursToAdd) {
		if (hoursToAdd == 0) {
			return this;
		}
		return of(zoneOrOffset, temporal.plus(hoursToAdd, ChronoUnit.HOURS));
	}

	/**
	 * Returns a new {@code ArabicTime} with the specified number of minutes added.
	 *
	 * <p>If {@code minutesToAdd} is zero, this instance is returned unchanged.</p>
	 *
	 * @param minutesToAdd the number of minutes to add, may be negative
	 * @return a new {@code ArabicTime} instance with the minutes added
	 */
	@Override
	public ArabicTime plusMinutes(long minutesToAdd) {
		if (minutesToAdd == 0) {
			return this;
		}
		return of(zoneOrOffset, temporal.plus(minutesToAdd, ChronoUnit.MINUTES));
	}

	/**
	 * Returns a new {@code ArabicTime} with the specified number of seconds added.
	 *
	 * <p>If {@code secondsToAdd} is zero, this instance is returned unchanged.</p>
	 *
	 * @param secondsToAdd the number of seconds to add, may be negative
	 * @return a new {@code ArabicTime} instance with the seconds added
	 */
	@Override
	public ArabicTime plusSeconds(long secondsToAdd) {
		if (secondsToAdd == 0) {
			return this;
		}
		return of(zoneOrOffset, temporal.plus(secondsToAdd, ChronoUnit.SECONDS));
	}

	/**
	 * Returns a new {@code ArabicTime} with the specified number of nanoseconds added.
	 *
	 * <p>If {@code nanosToAdd} is zero, this instance is returned unchanged.</p>
	 *
	 * @param nanosToAdd the number of nanoseconds to add, may be negative
	 * @return a new {@code ArabicTime} instance with the nanoseconds added
	 */
	@Override
	public ArabicTime plusNanos(long nanosToAdd) {
		if (nanosToAdd == 0) {
			return this;
		}
		return of(zoneOrOffset, temporal.plus(nanosToAdd, ChronoUnit.NANOS));
	}

	/**
	 * Returns a new {@code ArabicTime} obtained by subtracting the given Arabic temporal
	 * amount from this time.
	 *
	 * @param arabicTemporalAmount the temporal amount to subtract
	 * @return a new {@code ArabicTime} instance
	 */
	@Override
	public ArabicTime minus(ArabicTemporalAmount arabicTemporalAmount) {
		return compute(arabicTemporalAmount, this.temporal::minus);
	}

	/**
	 * Returns a new {@code ArabicTime} with the specified number of hours subtracted.
	 *
	 * <p>If {@code hoursToSubtract} is zero, this instance is returned unchanged.</p>
	 *
	 * @param hoursToSubtract the number of hours to subtract, may be negative
	 * @return a new {@code ArabicTime} instance with the hours subtracted
	 */
	@Override
	public ArabicTime minusHours(long hoursToSubtract) {
		if (hoursToSubtract == 0) {
			return this;
		}
		return of(zoneOrOffset, temporal.minus(hoursToSubtract, ChronoUnit.HOURS));
	}

	/**
	 * Returns a new {@code ArabicTime} with the specified number of minutes subtracted.
	 *
	 * <p>If {@code minutesToSubtract} is zero, this instance is returned unchanged.</p>
	 *
	 * @param minutesToSubtract the number of minutes to subtract, may be negative
	 * @return a new {@code ArabicTime} instance with the minutes subtracted
	 */
	@Override
	public ArabicTime minusMinutes(long minutesToSubtract) {
		if (minutesToSubtract == 0) {
			return this;
		}
		return of(zoneOrOffset, temporal.minus(minutesToSubtract, ChronoUnit.MINUTES));
	}

	/**
	 * Returns a new {@code ArabicTime} with the specified number of seconds subtracted.
	 *
	 * <p>If {@code secondsToSubtract} is zero, this instance is returned unchanged.</p>
	 *
	 * @param secondsToSubtract the number of seconds to subtract, may be negative
	 * @return a new {@code ArabicTime} instance with the seconds subtracted
	 */
	@Override
	public ArabicTime minusSeconds(long secondsToSubtract) {
		if (secondsToSubtract == 0) {
			return this;
		}
		return of(zoneOrOffset, temporal.minus(secondsToSubtract, ChronoUnit.SECONDS));
	}

	/**
	 * Returns a new {@code ArabicTime} with the specified number of nanoseconds subtracted.
	 *
	 * <p>If {@code nanosToSubtract} is zero, this instance is returned unchanged.</p>
	 *
	 * @param nanosToSubtract the number of nanoseconds to subtract, may be negative
	 * @return a new {@code ArabicTime} instance with the nanoseconds subtracted
	 */
	@Override
	public ArabicTime minusNanos(long nanosToSubtract) {
		if (nanosToSubtract == 0) {
			return this;
		}
		return of(zoneOrOffset, temporal.minus(nanosToSubtract, ChronoUnit.NANOS));
	}

	/**
	 * Computes a new {@code ArabicTime} by applying the given temporal computation.
	 *
	 * <p>
	 * Only duration-based arithmetic is supported for time-only values.
	 * Period-based operations are not permitted.
	 * </p>
	 *
	 * @param arabicTemporalAmount the temporal amount to apply
	 * @param computeFunction      the temporal computation function
	 * @return a new {@code ArabicTime} instance
	 * @throws IllegalArgumentException if the operation is not supported
	 */
	ArabicTime compute( ArabicTemporalAmount arabicTemporalAmount,
						Function<TemporalAmount, Temporal> computeFunction) {
		if (arabicTemporalAmount instanceof ArabicDuration duration) {
			long hours = duration.temporalAmount().toHours();

			if (hours > 24) {
				throw new IllegalArgumentException(
													"لا يمكن إضافة مدة تزيد عن 24 ساعة إلى قيمة زمنية فقط"
				);
			}

			return of(zoneOrOffset, computeFunction.apply(duration.temporalAmount()));
		}
		else if (arabicTemporalAmount instanceof ArabicPeriodWithDuration arabicPeriodWithDuration) {
			return of(zoneOrOffset, computeFunction.apply(arabicPeriodWithDuration.arabicDuration().temporalAmount()));
		}
		throw new IllegalArgumentException(
											"لا يمكن إضافة فترة (Period) إلى قيمة زمنية فقط"
		);
	}

	/**
	 * Returns a string representation of this {@code ArabicTime} in the format:
	 * "time zoneOrOffset".
	 *
	 * <p>If a {@link ZoneOrOffset} is present, it is appended after the time.
	 * Otherwise, only the time component is returned.</p>
	 *
	 * @return a formatted string representing the time and optional zone or offset
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(time);

		if (Objects.nonNull(zoneOrOffset)) {
			sb.append(" ").append(zoneOrOffset);
		}

		return sb.toString().trim();
	}

	/**
	 * Represents the time component of an Arabic time expression.
	 *
	 * <p>Stores hour, minute, optional second and nanosecond values, and
	 * optionally an AM/PM indicator.</p>
	 *
	 * <p>Validates the values to ensure they are within correct ranges depending
	 * on 12-hour or 24-hour format.</p>
	 *
	 * @param hour   the hour of the day
	 * @param minute the minute of the hour
	 * @param second optional second of the minute
	 * @param nano   optional nanosecond part
	 * @param isPM   true for PM, false for AM, null if 24-hour format
	 */
	public record Time(int hour, int minute, Integer second, Integer nano, Boolean isPM) {

		public Time {
			if (Objects.nonNull(isPM)) {
				if (hour < 1 || hour > 12) {
					throw new IllegalArgumentException(
														"الساعة يجب أن تكون من 1 إلى 12 عند استخدام ص/م"
					);
				}
			}
			else {
				if (hour < 0 || hour > 23) {
					throw new IllegalArgumentException("الساعة غير صحيحة");
				}
			}
			if (minute < 0 || minute > 59) {
				throw new IllegalArgumentException("الدقيقة غير صحيحة");
			}
			if (second != null && (second < 0 || second > 59)) {
				throw new IllegalArgumentException("الثانية غير صحيحة");
			}
			if (nano != null && (nano < 0 || nano > 999_999_999)) {
				throw new IllegalArgumentException("الجزء من الثانية غير صحيح");
			}
		}

		/**
		 * Creates a {@code Time} instance with the given hour, minute, optional second,
		 * nanosecond, and AM/PM indicator.
		 *
		 * @param hour   the hour of the day
		 * @param minute the minute of the hour
		 * @param second optional second of the minute
		 * @param nano   optional nanosecond part
		 * @param isPM   true for PM, false for AM, null if 24-hour format
		 * @return a new {@code Time} instance
		 * @throws IllegalArgumentException if any value is out of valid range
		 */
		public static Time of(int hour, int minute, Integer second, Integer nano, Boolean isPM) {
			return new Time(hour, minute, second, nano, isPM);
		}

		/**
		 * Returns the hour converted to 24-hour format based on the AM/PM indicator.
		 *
		 * @return the hour in 24-hour format
		 */
		public int getHour24() {
			return TemporalUtils.getHour24(hour, isPM);
		}

		/**
		 * Returns a string representation of this {@code Time} in the format:
		 * "HH:mm[:ss[.nnnnnnnnn]] [AM/PM]".
		 *
		 * <p>Optional seconds, nanoseconds, and AM/PM indicator are included if present.</p>
		 *
		 * @return a formatted string representing the time
		 */
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			int displayHour = getHour24();

			sb.append(numberToString(displayHour)).append(":").append(padZero(numberToString(minute), 2));
			if (Objects.nonNull(second)) {
				sb.append(":").append(padZero(numberToString(second), 2));
			}
			if (Objects.nonNull(nano)) {
				sb.append(".").append(padZero(numberToString(nano), 9));
			}
			if (Objects.nonNull(isPM)) {
				sb.append(isPM ? " م" : " ص");
			}
			return sb.toString();
		}
	}

	/**
	 * Represents an Arabic time zone or numeric offset.
	 *
	 * <p>Stores the original Arabic zone or offset string, a boolean indicating
	 * whether it is a numeric offset, and the resolved {@link ZoneId}.</p>
	 *
	 * @param arabicZoneOrOffset the Arabic zone name or offset string
	 * @param offset             true if the value is a numeric offset, false if a named zone
	 * @param zoneId             the resolved {@link ZoneId} representation
	 */
	public record ZoneOrOffset(String arabicZoneOrOffset, boolean offset, ZoneId zoneId) {

		private static ZoneId parseZone(String arabicZoneOrOffset, boolean offset) {
			if (offset) {
				return parseZoneOffset(arabicZoneOrOffset);
			}
			else {
				String javaZoneId = ZoneUtils.arabicZoneNameToJava(arabicZoneOrOffset);
				return ZoneId.of(javaZoneId);
			}
		}

		/**
		 * Creates a {@code ZoneOrOffset} instance representing a named time zone.
		 *
		 * @param arabicZone the Arabic name of the time zone
		 * @return a new {@code ZoneOrOffset} instance
		 */
		public static ZoneOrOffset ofZone(String arabicZone) {
			return new ZoneOrOffset(arabicZone, false, parseZone(arabicZone, false));
		}

		/**
		 * Creates a {@code ZoneOrOffset} instance representing a numeric offset.
		 *
		 * @param offset the offset string (e.g., "+02:00")
		 * @return a new {@code ZoneOrOffset} instance
		 */
		public static ZoneOrOffset ofOffset(String offset) {
			return new ZoneOrOffset(offset, true, parseZone(offset, true));
		}

		/**
		 * Returns a string representation of this {@code ZoneOrOffset}.
		 *
		 * <ul>
		 * <li>If it represents an offset, returns the offset string directly.</li>
		 * <li>If it represents a named time zone, returns "بتوقيت &lt;zoneName&gt;"</li>
		 * <li>If the zone name is empty, returns an empty string.</li>
		 * </ul>
		 *
		 * @return a formatted string representing the zone or offset
		 */
		@Override
		public String toString() {
			if (offset) {
				return arabicZoneOrOffset;
			}
			else if (!ObjectUtils.isEmpty(arabicZoneOrOffset)) {
				return ZONE_PREFIX_AR + " " + arabicZoneOrOffset;
			}
			else {
				return "";
			}
		}
	}
}
