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
public record ArabicDuration(
		DurationDefinition durationDefinition,
		Duration temporalAmount
) implements ArabicTemporalAmount {

	/**
	 * Creates a new {@link ArabicDuration} instance using the provided
	 * Arabic duration definition and the underlying {@link Duration}.
	 *
	 * @param durationDefinition the Arabic textual definition describing the duration
	 *                           (hours, minutes, seconds, etc.)
	 * @param temporalAmount     the underlying {@link Duration} representing the actual
	 *                           time-based amount
	 * @return a new {@link ArabicDuration} instance
	 */
	public static ArabicDuration of(
									DurationDefinition durationDefinition,
									Duration temporalAmount) {
		return new ArabicDuration(durationDefinition, temporalAmount);
	}

	/**
	 * Creates a new {@link ArabicDuration} instance from a {@link Duration}.
	 * <p>
	 * The duration is decomposed into its time-based components
	 * (hours, minutes, seconds, milliseconds, and nanoseconds) to
	 * build the corresponding Arabic textual representation.
	 * </p>
	 *
	 * @param duration the {@link Duration} to convert into an {@link ArabicDuration}
	 * @return a new {@link ArabicDuration} instance representing the given duration
	 */
	public static ArabicDuration of(Duration duration) {
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
