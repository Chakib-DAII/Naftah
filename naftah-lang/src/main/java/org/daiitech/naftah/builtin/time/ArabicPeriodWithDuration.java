package org.daiitech.naftah.builtin.time;

import java.time.temporal.TemporalAmount;

/**
 * Represents a combined period and duration expressed in Arabic text.
 * <p>
 * This class wraps an {@link ArabicPeriod} and an {@link ArabicDuration}, allowing
 * you to work with both periods (years, months, days) and durations (hours, minutes, seconds)
 * together in Arabic formatting.
 * </p>
 *
 * @param arabicPeriod   the period part in Arabic
 * @param arabicDuration the duration part in Arabic
 * @author Chakib Daii
 */
public record ArabicPeriodWithDuration(
		ArabicPeriod arabicPeriod,
		ArabicDuration arabicDuration
) implements ArabicTemporalAmount {

	/**
	 * Creates a new {@code ArabicPeriodWithDuration} instance.
	 *
	 * @param arabicPeriod   the period part in Arabic
	 * @param arabicDuration the duration part in Arabic
	 * @return a new {@code ArabicPeriodWithDuration} instance
	 */
	public static ArabicPeriodWithDuration of(
												ArabicPeriod arabicPeriod,
												ArabicDuration arabicDuration) {
		return new ArabicPeriodWithDuration(arabicPeriod, arabicDuration);
	}

	/**
	 * Returns the combined temporal amount of the period and duration.
	 * <p>
	 * The period and duration are added together as a single {@link TemporalAmount}.
	 * </p>
	 *
	 * @return the total temporal amount
	 */
	@Override
	public TemporalAmount temporalAmount() {
		return arabicPeriod
				.temporalAmount()
				.plus(arabicDuration.temporalAmount());
	}

	/**
	 * Returns the Arabic textual representation of the combined period and duration.
	 * <p>
	 * The string consists of the Arabic period followed by the Arabic duration, joined
	 * with the Arabic conjunction " و ".
	 * </p>
	 *
	 * @return a string describing the combined period and duration in Arabic
	 */
	@Override
	public String toString() {
		return arabicPeriod.toString() + " و " + arabicDuration.toString();
	}

}
