package org.daiitech.naftah.builtin.time;

import java.time.Duration;
import java.time.Period;
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
		return adjustDurationIntoPeriod(arabicPeriod, arabicDuration);
	}

	/**
	 * Adjusts durations of 24 hours or more by converting full days into the period.
	 *
	 * @param period   the original ArabicPeriod
	 * @param duration the original ArabicDuration
	 * @return a new {@code ArabicPeriodWithDuration} with normalized duration and adjusted period
	 */
	private static ArabicPeriodWithDuration adjustDurationIntoPeriod(
																		ArabicPeriod period,
																		ArabicDuration duration) {

		long totalHours = duration.temporalAmount().toHours();

		if (totalHours >= 24) {
			long extraDays = totalHours / 24;
			long remainingHours = totalHours % 24;

			// Adjust period
			Period newPeriod = period.temporalAmount().plusDays(extraDays);

			// Adjust duration to less than 24 hours
			Duration newDuration = duration.temporalAmount().minusHours(extraDays * 24);

			ArabicPeriod.PeriodDefinition oldPeriodDefinition = period.periodDefinition();
			ArabicPeriod adjustedPeriod = ArabicPeriod
					.of(
						ArabicPeriod.PeriodDefinition
								.of(
									newPeriod.getYears(),
									oldPeriodDefinition.yearText(),
									newPeriod.getMonths(),
									oldPeriodDefinition.monthText(),
									newPeriod.getDays(),
									oldPeriodDefinition.dayText()
								),
						newPeriod
					);

			ArabicDuration.DurationDefinition oldDurationDefinition = duration.durationDefinition();
			ArabicDuration adjustedDuration = ArabicDuration
					.of(
						ArabicDuration.DurationDefinition
								.of(
									(int) remainingHours,
									oldDurationDefinition.hourText(),
									oldDurationDefinition.minutes(),
									oldDurationDefinition.minuteText(),
									oldDurationDefinition.seconds(),
									oldDurationDefinition.millis(),
									oldDurationDefinition.secondText(),
									oldDurationDefinition.nanos(),
									oldDurationDefinition.nanoText()
								),
						newDuration
					);
			return new ArabicPeriodWithDuration(adjustedPeriod, adjustedDuration);
		}

		return new ArabicPeriodWithDuration(period, duration);
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
