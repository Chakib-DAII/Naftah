package org.daiitech.naftah.builtin.time;

import java.time.ZoneId;
import java.time.temporal.TemporalAccessor;
import java.util.Objects;

import org.daiitech.naftah.builtin.utils.ObjectUtils;
import org.daiitech.naftah.utils.time.DateTimeUtils;
import org.daiitech.naftah.utils.time.ZoneUtils;

import static org.daiitech.naftah.utils.time.ZoneUtils.parseZoneOffset;

/**
 * Represents an Arabic time expression, optionally including a time zone or offset,
 * and a resolved {@link TemporalAccessor} representation.
 *
 * <p>Implemented as a record with the components:
 * <ul>
 * <li>{@link Time} – the hour, minute, optional second and nanosecond, and AM/PM indicator</li>
 * <li>{@link ZoneOrOffset} – optional time zone or numeric offset</li>
 * <li>{@link TemporalAccessor} – the resolved temporal representation</li>
 * </ul>
 *
 * <p>This record implements {@link ArabicTemporal} and is typically produced
 * after parsing Arabic time expressions.</p>
 *
 * @param time         the time component
 * @param zoneOrOffset the optional time zone or offset component
 * @param temporal     the resolved {@link TemporalAccessor} representation
 * @author Chakib Daii
 * @return a new {@code ArabicTime} instance
 */
public record ArabicTime(
		Time time,
		ZoneOrOffset zoneOrOffset,
		TemporalAccessor temporal
) implements ArabicTemporal {

	/**
	 * Creates a new {@code ArabicTime} instance.
	 *
	 * @param time         the time component
	 * @param zoneOrOffset the optional time zone or offset component
	 * @param temporal     the resolved {@link TemporalAccessor} representation
	 * @return a new {@code ArabicTime} instance
	 */
	public static ArabicTime of(
								Time time,
								ZoneOrOffset zoneOrOffset,
								TemporalAccessor temporal) {
		return new ArabicTime(time, zoneOrOffset, temporal);
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
			return new ArabicTime.Time(hour, minute, second, nano, isPM);
		}

		/**
		 * Returns the hour converted to 24-hour format based on the AM/PM indicator.
		 *
		 * @return the hour in 24-hour format
		 */
		public int getHour24() {
			return DateTimeUtils.getHour24(hour, isPM);
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

			sb.append(displayHour).append(":").append(String.format("%02d", minute));
			if (Objects.nonNull(second)) {
				sb.append(":").append(String.format("%02d", second));
			}
			if (Objects.nonNull(nano)) {
				sb.append(".").append(String.format("%09d", nano));
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
				return "بتوقيت " + arabicZoneOrOffset;
			}
			else {
				return "";
			}
		}
	}
}
