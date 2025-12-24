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
	 * Creates a new {@code ArabicDuration} instance.
	 *
	 * @param durationDefinition the definition of the duration in Arabic text
	 * @param temporalAmount     the actual duration value
	 * @return a new {@code ArabicDuration} instance
	 */
	public static ArabicDuration of(
									DurationDefinition durationDefinition,
									Duration temporalAmount) {
		return new ArabicDuration(durationDefinition, temporalAmount);
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
		 * Creates a new {@code DurationDefinition} instance, filling missing text labels with default Arabic terms.
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
		 * @return a new {@code DurationDefinition} instance
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
