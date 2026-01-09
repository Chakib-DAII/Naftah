package org.daiitech.naftah.builtin.time;

import java.time.Duration;
import java.util.Objects;

import org.daiitech.naftah.builtin.utils.ObjectUtils;

import static org.daiitech.naftah.utils.time.Constants.DURATION_PREFIX;
import static org.daiitech.naftah.utils.time.Constants.HOUR;
import static org.daiitech.naftah.utils.time.Constants.MINUTE;
import static org.daiitech.naftah.utils.time.Constants.NANOSECOND;
import static org.daiitech.naftah.utils.time.Constants.SECOND;

/**
 * Represents a duration expressed in Arabic text, including hours, minutes, seconds, milliseconds, and nanoseconds.
 * <p>
 * This class wraps a {@link DurationDefinition} for formatting purposes and a {@link java.time.Duration}
 * for the actual temporal amount.
 * </p>
 *
 * @param durationDefinition the definition of the duration in Arabic text
 * @param temporalAmount     the actual duration value
 * @author Chakib Daii
 */
public record NaftahDuration(
		DurationDefinition durationDefinition,
		Duration temporalAmount
) implements NaftahTemporalAmount {

	/**
	 * Creates a new {@link NaftahDuration} instance using the provided
	 * Arabic duration definition and the underlying {@link Duration}.
	 *
	 * @param durationDefinition the Arabic textual definition describing the duration
	 *                           (hours, minutes, seconds, etc.)
	 * @param temporalAmount     the underlying {@link Duration} representing the actual
	 *                           time-based amount
	 * @return a new {@link NaftahDuration} instance
	 */
	public static NaftahDuration of(
									DurationDefinition durationDefinition,
									Duration temporalAmount) {
		return new NaftahDuration(durationDefinition, temporalAmount);
	}

	/**
	 * Creates a new {@link NaftahDuration} instance from a {@link Duration}.
	 * <p>
	 * The duration is decomposed into its time-based components
	 * (hours, minutes, seconds, milliseconds, and nanoseconds) to
	 * build the corresponding Arabic textual representation.
	 * </p>
	 *
	 * @param duration the {@link Duration} to convert into an {@link NaftahDuration}
	 * @return a new {@link NaftahDuration} instance representing the given duration
	 */
	public static NaftahDuration of(Duration duration) {
		long totalSeconds = duration.getSeconds();
		int nanos = duration.getNano();

		int hours = (int) (totalSeconds / 3600);
		int minutes = (int) ((totalSeconds % 3600) / 60);
		int seconds = (int) (totalSeconds % 60);
		int millis = nanos / 1_000_000;
		int remainingNanos = nanos % 1_000_000;

		return of(DurationDefinition
				.of(
					hours,
					minutes,
					seconds,
					millis,
					remainingNanos
				), duration);
	}

	/**
	 * Returns an {@link NaftahDuration} representing a zero duration.
	 *
	 * <p>The returned duration has all components set to zero: hours, minutes, seconds,
	 * milliseconds, and nanoseconds.</p>
	 *
	 * @return an {@link NaftahDuration} representing a duration of zero
	 */
	public static NaftahDuration ofZero() {
		return of(DurationDefinition
				.of(
					0,
					0,
					0,
					0,
					0
				), Duration.ZERO);
	}

	/**
	 * Obtains a {@code ArabicDuration} representing a number of standard 24 hour days.
	 * <p>
	 * The seconds are calculated based on the standard definition of a day,
	 * where each day is 86400 seconds which implies a 24 hour day.
	 * The nanosecond in second field is set to zero.
	 *
	 * @param days the number of days, positive or negative
	 * @return a {@code ArabicDuration}, not null
	 * @throws ArithmeticException if the input days exceeds the capacity of {@code Duration}
	 */
	public static NaftahDuration ofDays(long days) {
		return of(Duration.ofDays(days));
	}

	/**
	 * Obtains a {@code ArabicDuration} representing a number of standard hours.
	 * <p>
	 * The seconds are calculated based on the standard definition of an hour,
	 * where each hour is 3600 seconds.
	 * The nanosecond in second field is set to zero.
	 *
	 * @param hours the number of hours, positive or negative
	 * @return a {@code ArabicDuration}, not null
	 * @throws ArithmeticException if the input hours exceeds the capacity of {@code Duration}
	 */
	public static NaftahDuration ofHours(long hours) {
		return of(Duration.ofHours(hours));
	}

	/**
	 * Obtains a {@code ArabicDuration} representing a number of standard minutes.
	 * <p>
	 * The seconds are calculated based on the standard definition of a minute,
	 * where each minute is 60 seconds.
	 * The nanosecond in second field is set to zero.
	 *
	 * @param minutes the number of minutes, positive or negative
	 * @return a {@code ArabicDuration}, not null
	 * @throws ArithmeticException if the input minutes exceeds the capacity of {@code Duration}
	 */
	public static NaftahDuration ofMinutes(long minutes) {
		return of(Duration.ofMinutes(minutes));
	}

	/**
	 * Obtains a {@code ArabicDuration} representing a number of seconds.
	 * <p>
	 * The nanosecond in second field is set to zero.
	 *
	 * @param seconds the number of seconds, positive or negative
	 * @return a {@code ArabicDuration}, not null
	 */
	public static NaftahDuration ofSeconds(long seconds) {
		return of(Duration.ofSeconds(seconds));
	}

	/**
	 * Obtains a {@code ArabicDuration} representing a number of seconds and an
	 * adjustment in nanoseconds.
	 * <p>
	 * This method allows an arbitrary number of nanoseconds to be passed in.
	 * The factory will alter the values of the second and nanosecond in order
	 * to ensure that the stored nanosecond is in the range 0 to 999,999,999.
	 * For example, the following will result in exactly the same duration:
	 * <pre>
	 * Duration.ofSeconds(3, 1);
	 * Duration.ofSeconds(4, -999_999_999);
	 * Duration.ofSeconds(2, 1000_000_001);
	 * </pre>
	 *
	 * @param seconds        the number of seconds, positive or negative
	 * @param nanoAdjustment the nanosecond adjustment to the number of seconds, positive or negative
	 * @return a {@code ArabicDuration}, not null
	 * @throws ArithmeticException if the adjustment causes the seconds to exceed the capacity of {@code Duration}
	 */
	public static NaftahDuration ofSeconds(long seconds, long nanoAdjustment) {
		return of(Duration.ofSeconds(seconds, nanoAdjustment));
	}

	/**
	 * Obtains a {@code ArabicDuration} representing a number of milliseconds.
	 * <p>
	 * The seconds and nanoseconds are extracted from the specified milliseconds.
	 *
	 * @param millis the number of milliseconds, positive or negative
	 * @return a {@code ArabicDuration}, not null
	 */
	public static NaftahDuration ofMillis(long millis) {
		return of(Duration.ofMillis(millis));
	}

	/**
	 * Obtains a {@code ArabicDuration} representing a number of nanoseconds.
	 * <p>
	 * The seconds and nanoseconds are extracted from the specified nanoseconds.
	 *
	 * @param nanos the number of nanoseconds, positive or negative
	 * @return a {@code ArabicDuration}, not null
	 */
	public static NaftahDuration ofNanos(long nanos) {
		return of(Duration.ofNanos(nanos));
	}

	/**
	 * Returns the total number of hours in this duration.
	 *
	 * @return the total hours
	 */
	public long getHours() {
		return temporalAmount.toHours();
	}

	/**
	 * Returns the minute-of-hour component of this duration.
	 * <p>
	 * This is calculated as the total minutes modulo 60, representing the
	 * remaining minutes after full hours are accounted for.
	 * </p>
	 *
	 * @return the minutes within the current hour (0-59)
	 */
	public long getMinutes() {
		return temporalAmount.toMinutes() % 60;
	}

	/**
	 * Returns the second-of-minute component of this duration.
	 * <p>
	 * This is calculated as the total seconds modulo 60, representing the
	 * remaining seconds after full minutes are accounted for.
	 * </p>
	 *
	 * @return the seconds within the current minute (0-59)
	 */
	public long getSeconds() {
		return temporalAmount.getSeconds() % 60;
	}

	/**
	 * Returns the millisecond-of-second component of this duration.
	 * <p>
	 * This is calculated as the total milliseconds modulo 1000, representing
	 * the remaining milliseconds after full seconds are accounted for.
	 * </p>
	 *
	 * @return the milliseconds within the current second (0-999)
	 */
	public long getMillis() {
		return temporalAmount.toMillisPart();
	}

	/**
	 * Returns the nanosecond-of-second component of this duration.
	 *
	 * @return the nanoseconds within the current second (0-999,999,999)
	 */
	public int getNano() {
		return temporalAmount.getNano();
	}

	/**
	 * Returns a new {@code ArabicDuration} obtained by adding the given
	 * Arabic temporal amount to this duration.
	 *
	 * <p>
	 * Addition is supported only when the other amount is also an
	 * {@code ArabicDuration}.
	 * </p>
	 *
	 * @param other the temporal amount to add
	 * @return a new {@code ArabicDuration} instance
	 */
	@Override
	public NaftahDuration plus(NaftahTemporalAmount other) {
		if (other instanceof NaftahDuration otherDur) {
			Duration result = temporalAmount.plus(otherDur.temporalAmount());
			return NaftahDuration.of(result);
		}
		return (NaftahDuration) NaftahTemporalAmount.super.plus(other);
	}

	/**
	 * Returns a copy of this duration with the specified duration in standard 24 hour days added.
	 * <p>
	 * The number of days is multiplied by 86400 to obtain the number of seconds to add.
	 * This is based on the standard definition of a day as 24 hours.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 *
	 * @param daysToAdd the days to add, positive or negative
	 * @return a {@code ArabicDuration} based on this duration with the specified days added, not null
	 * @throws ArithmeticException if numeric overflow occurs
	 */
	public NaftahDuration plusDays(long daysToAdd) {
		if (daysToAdd == 0) {
			return this;
		}
		return NaftahDuration.of(temporalAmount.plusDays(daysToAdd));
	}

	/**
	 * Returns a copy of this duration with the specified duration in hours added.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 *
	 * @param hoursToAdd the hours to add, positive or negative
	 * @return a {@code ArabicDuration} based on this duration with the specified hours added, not null
	 * @throws ArithmeticException if numeric overflow occurs
	 */
	public NaftahDuration plusHours(long hoursToAdd) {
		if (hoursToAdd == 0) {
			return this;
		}
		return NaftahDuration.of(temporalAmount.plusHours(hoursToAdd));
	}

	/**
	 * Returns a copy of this duration with the specified duration in minutes added.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 *
	 * @param minutesToAdd the minutes to add, positive or negative
	 * @return a {@code ArabicDuration} based on this duration with the specified minutes added, not null
	 * @throws ArithmeticException if numeric overflow occurs
	 */
	public NaftahDuration plusMinutes(long minutesToAdd) {
		if (minutesToAdd == 0) {
			return this;
		}
		return NaftahDuration.of(temporalAmount.plusMinutes(minutesToAdd));
	}

	/**
	 * Returns a copy of this duration with the specified duration in seconds added.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 *
	 * @param secondsToAdd the seconds to add, positive or negative
	 * @return a {@code ArabicDuration} based on this duration with the specified seconds added, not null
	 * @throws ArithmeticException if numeric overflow occurs
	 */
	public NaftahDuration plusSeconds(long secondsToAdd) {
		if (secondsToAdd == 0) {
			return this;
		}
		return NaftahDuration.of(temporalAmount.plusSeconds(secondsToAdd));
	}

	/**
	 * Returns a copy of this duration with the specified duration in milliseconds added.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 *
	 * @param millisToAdd the milliseconds to add, positive or negative
	 * @return a {@code ArabicDuration} based on this duration with the specified milliseconds added, not null
	 * @throws ArithmeticException if numeric overflow occurs
	 */
	public NaftahDuration plusMillis(long millisToAdd) {
		if (millisToAdd == 0) {
			return this;
		}
		return NaftahDuration.of(temporalAmount.plusMillis(millisToAdd));
	}

	/**
	 * Returns a copy of this duration with the specified duration in nanoseconds added.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 *
	 * @param nanosToAdd the nanoseconds to add, positive or negative
	 * @return a {@code ArabicDuration} based on this duration with the specified nanoseconds added, not null
	 * @throws ArithmeticException if numeric overflow occurs
	 */
	public NaftahDuration plusNanos(long nanosToAdd) {
		if (nanosToAdd == 0) {
			return this;
		}
		return NaftahDuration.of(temporalAmount.plusNanos(nanosToAdd));
	}

	/**
	 * Returns a new {@code ArabicDuration} obtained by subtracting the given
	 * Arabic temporal amount from this duration.
	 *
	 * <p>
	 * Subtraction is supported only when the other amount is also an
	 * {@code ArabicDuration}.
	 * </p>
	 *
	 * @param other the temporal amount to subtract
	 * @return a new {@code ArabicDuration} instance
	 */
	@Override
	public NaftahDuration minus(NaftahTemporalAmount other) {
		if (other instanceof NaftahDuration otherDur) {
			Duration result = temporalAmount.minus(otherDur.temporalAmount());
			return NaftahDuration.of(result);
		}
		return (NaftahDuration) NaftahTemporalAmount.super.minus(other);
	}

	/**
	 * Returns a copy of this duration with the specified duration in standard 24 hour days subtracted.
	 * <p>
	 * The number of days is multiplied by 86400 to obtain the number of seconds to subtract.
	 * This is based on the standard definition of a day as 24 hours.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 *
	 * @param daysToSubtract the days to subtract, positive or negative
	 * @return a {@code ArabicDuration} based on this duration with the specified days subtracted, not null
	 * @throws ArithmeticException if numeric overflow occurs
	 */
	public NaftahDuration minusDays(long daysToSubtract) {
		if (daysToSubtract == 0) {
			return this;
		}
		return NaftahDuration.of(temporalAmount.minusDays(daysToSubtract));
	}

	/**
	 * Returns a copy of this duration with the specified duration in hours subtracted.
	 * <p>
	 * The number of hours is multiplied by 3600 to obtain the number of seconds to subtract.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 *
	 * @param hoursToSubtract the hours to subtract, positive or negative
	 * @return a {@code ArabicDuration} based on this duration with the specified hours subtracted, not null
	 * @throws ArithmeticException if numeric overflow occurs
	 */
	public NaftahDuration minusHours(long hoursToSubtract) {
		if (hoursToSubtract == 0) {
			return this;
		}
		return NaftahDuration.of(temporalAmount.minusHours(hoursToSubtract));
	}

	/**
	 * Returns a copy of this duration with the specified duration in minutes subtracted.
	 * <p>
	 * The number of hours is multiplied by 60 to obtain the number of seconds to subtract.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 *
	 * @param minutesToSubtract the minutes to subtract, positive or negative
	 * @return a {@code ArabicDuration} based on this duration with the specified minutes subtracted, not null
	 * @throws ArithmeticException if numeric overflow occurs
	 */
	public NaftahDuration minusMinutes(long minutesToSubtract) {
		if (minutesToSubtract == 0) {
			return this;
		}
		return NaftahDuration.of(temporalAmount.minusMinutes(minutesToSubtract));
	}

	/**
	 * Returns a copy of this duration with the specified duration in seconds subtracted.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 *
	 * @param secondsToSubtract the seconds to subtract, positive or negative
	 * @return a {@code ArabicDuration} based on this duration with the specified seconds subtracted, not null
	 * @throws ArithmeticException if numeric overflow occurs
	 */
	public NaftahDuration minusSeconds(long secondsToSubtract) {
		if (secondsToSubtract == 0) {
			return this;
		}
		return NaftahDuration.of(temporalAmount.minusSeconds(secondsToSubtract));
	}

	/**
	 * Returns a copy of this duration with the specified duration in milliseconds subtracted.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 *
	 * @param millisToSubtract the milliseconds to subtract, positive or negative
	 * @return a {@code ArabicDuration} based on this duration with the specified milliseconds subtracted, not null
	 * @throws ArithmeticException if numeric overflow occurs
	 */
	public NaftahDuration minusMillis(long millisToSubtract) {
		if (millisToSubtract == 0) {
			return this;
		}
		return NaftahDuration.of(temporalAmount.minusMillis(millisToSubtract));
	}

	/**
	 * Returns a copy of this duration with the specified duration in nanoseconds subtracted.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 *
	 * @param nanosToSubtract the nanoseconds to subtract, positive or negative
	 * @return a {@code ArabicDuration} based on this duration with the specified nanoseconds subtracted, not null
	 * @throws ArithmeticException if numeric overflow occurs
	 */
	public NaftahDuration minusNanos(long nanosToSubtract) {
		if (nanosToSubtract == 0) {
			return this;
		}
		return NaftahDuration.of(temporalAmount.minusNanos(nanosToSubtract));
	}

	/**
	 * Determines whether this duration is equal to the given temporal amount.
	 *
	 * @param other the temporal amount to compare with
	 * @return {@code true} if the amounts are equal; {@code false} otherwise
	 */
	@Override
	public boolean isEquals(NaftahTemporalAmount other) {
		if (other instanceof NaftahDuration otherDur) {
			return temporalAmount.equals(otherDur.temporalAmount());
		}
		return false;
	}

	/**
	 * Determines whether this duration is not equal to the given temporal amount.
	 *
	 * @param other the temporal amount to compare with
	 * @return {@code true} if the amounts are not equal; {@code false} otherwise
	 */
	@Override
	public boolean notEquals(NaftahTemporalAmount other) {
		return !isEquals(other);
	}

	/**
	 * Determines whether this duration is greater than the given temporal amount.
	 *
	 * @param other the temporal amount to compare with
	 * @return {@code true} if this duration is greater
	 */
	@Override
	public boolean greaterThan(NaftahTemporalAmount other) {
		if (other instanceof NaftahDuration otherDur) {
			return temporalAmount.compareTo(otherDur.temporalAmount()) > 0;
		}
		return NaftahTemporalAmount.super.greaterThan(other);
	}

	/**
	 * Determines whether this duration is greater than or equal to the
	 * given temporal amount.
	 *
	 * @param other the temporal amount to compare with
	 * @return {@code true} if this duration is greater than or equal
	 */
	@Override
	public boolean greaterThanEquals(NaftahTemporalAmount other) {
		if (other instanceof NaftahDuration otherDur) {
			return temporalAmount.compareTo(otherDur.temporalAmount()) >= 0;
		}
		return NaftahTemporalAmount.super.greaterThanEquals(other);
	}

	/**
	 * Determines whether this duration is less than the given temporal amount.
	 *
	 * @param other the temporal amount to compare with
	 * @return {@code true} if this duration is less
	 */
	@Override
	public boolean lessThan(NaftahTemporalAmount other) {
		if (other instanceof NaftahDuration otherDur) {
			return temporalAmount.compareTo(otherDur.temporalAmount()) < 0;
		}
		return NaftahTemporalAmount.super.lessThan(other);
	}

	/**
	 * Determines whether this duration is less than or equal to the
	 * given temporal amount.
	 *
	 * @param other the temporal amount to compare with
	 * @return {@code true} if this duration is less than or equal
	 */
	@Override
	public boolean lessThanEquals(NaftahTemporalAmount other) {
		if (other instanceof NaftahDuration otherDur) {
			return temporalAmount.compareTo(otherDur.temporalAmount()) <= 0;
		}
		return NaftahTemporalAmount.super.lessThanEquals(other);
	}

	/**
	 * Returns the Arabic textual representation of the duration.
	 *
	 * @return a string describing the duration in Arabic
	 */
	@Override
	public String toString() {
		return durationDefinition.toString();
	}

	/**
	 * Definition of a duration in Arabic text, including hours, minutes, seconds, milliseconds, and nanoseconds.
	 *
	 * @param hours      the number of hours
	 * @param hourText   the Arabic word for hours (default is
	 *                   * {@link org.daiitech.naftah.utils.time.Constants#HOUR})
	 * @param minutes    the number of minutes
	 * @param minuteText the Arabic word for minutes (default is
	 *                   * {@link org.daiitech.naftah.utils.time.Constants#MINUTE})
	 * @param seconds    the number of seconds
	 * @param millis     the number of milliseconds (used as decimal part for seconds)
	 * @param secondText the Arabic word for seconds (default is
	 *                   * {@link org.daiitech.naftah.utils.time.Constants#SECOND})
	 * @param nanos      the number of nanoseconds
	 * @param nanoText   the Arabic word for nanoseconds (default is
	 *                   * {@link org.daiitech.naftah.utils.time.Constants#NANOSECOND})
	 */
	public record DurationDefinition(
			int hours,
			String hourText,
			int minutes,
			String minuteText,
			int seconds,
			int millis,
			String secondText,
			int nanos,
			String nanoText
	) {
		/**
		 * Creates a new {@link DurationDefinition} instance.
		 * <p>
		 * Any missing Arabic text labels are automatically filled using the
		 * default Arabic time unit terms defined in
		 * {@link org.daiitech.naftah.utils.time.Constants}.
		 * </p>
		 *
		 * @param hours      the number of hours
		 * @param hourText   the Arabic text used for the hour unit; if {@code null},
		 *                   {@link org.daiitech.naftah.utils.time.Constants#HOUR} is used
		 * @param minutes    the number of minutes
		 * @param minuteText the Arabic text used for the minute unit; if {@code null},
		 *                   {@link org.daiitech.naftah.utils.time.Constants#MINUTE} is used
		 * @param seconds    the number of seconds
		 * @param millis     the number of milliseconds (used as the decimal fraction of seconds)
		 * @param secondText the Arabic text used for the second unit; if {@code null},
		 *                   {@link org.daiitech.naftah.utils.time.Constants#SECOND} is used
		 * @param nanos      the number of nanoseconds
		 * @param nanoText   the Arabic text used for the nanosecond unit; if {@code null},
		 *                   {@link org.daiitech.naftah.utils.time.Constants#NANOSECOND} is used
		 * @return a new {@link DurationDefinition} instance
		 */
		public static DurationDefinition of(
											int hours,
											String hourText,
											int minutes,
											String minuteText,
											int seconds,
											int millis,
											String secondText,
											int nanos,
											String nanoText) {
			return new DurationDefinition(  hours,
											Objects.requireNonNullElse(hourText, HOUR),
											minutes,
											Objects.requireNonNullElse(minuteText, MINUTE),
											seconds,
											millis,
											Objects.requireNonNullElse(secondText, SECOND),
											nanos,
											Objects.requireNonNullElse(nanoText, NANOSECOND));
		}

		/**
		 * Creates a new {@link DurationDefinition} instance using the default
		 * Arabic time unit labels.
		 *
		 * @param hours   the number of hours
		 * @param minutes the number of minutes
		 * @param seconds the number of seconds
		 * @param millis  the number of milliseconds (used as the decimal fraction of seconds)
		 * @param nanos   the number of nanoseconds
		 * @return a new {@link DurationDefinition} instance
		 */
		public static DurationDefinition of(
											int hours,
											int minutes,
											int seconds,
											int millis,
											int nanos) {
			return new DurationDefinition(  hours,
											HOUR,
											minutes,
											MINUTE,
											seconds,
											millis,
											SECOND,
											nanos,
											NANOSECOND);
		}

		/**
		 * Returns a formatted Arabic string representing the duration.
		 * <p>
		 * Non-zero units are printed with the Arabic conjunction " و " between them.
		 * Milliseconds are displayed as a decimal fraction of seconds if present.
		 * If all units are zero, it defaults to "0 ثانية".
		 * </p>
		 *
		 * @return a string describing the duration in Arabic
		 */
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(DURATION_PREFIX);

			if (hours != 0) {
				sb
						.append(" ")
						.append(ObjectUtils.numberToString(hours))
						.append(" ")
						.append(hourText);
			}
			if (minutes != 0) {
				if (sb.length() > DURATION_PREFIX.length()) {
					sb.append(" و ");
				}
				else {
					sb.append(" ");
				}
				sb
						.append(ObjectUtils.numberToString(minutes))
						.append(" ")
						.append(minuteText);
			}
			if (seconds != 0) {
				if (sb.length() > DURATION_PREFIX.length()) {
					sb.append(" و ");
				}
				else {
					sb.append(" ");
				}

				sb.append(ObjectUtils.numberToString(seconds));

				if (millis != 0) {
					sb.append(".").append(ObjectUtils.numberToString(millis));
				}

				sb.append(" ");

				sb.append(secondText);
			}
			if (nanos != 0) {
				if (sb.length() > DURATION_PREFIX.length()) {
					sb.append(" و ");
				}
				else {
					sb.append(" ");
				}
				sb
						.append(ObjectUtils.numberToString(nanos))
						.append(" ")
						.append(nanoText);
			}

			if (sb.length() == DURATION_PREFIX.length()) {
				sb
						.append(" ")
						.append(ObjectUtils.numberToString(0))
						.append(" ")
						.append(SECOND);
			}

			return sb.toString();
		}
	}
}
