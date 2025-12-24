package org.daiitech.naftah.builtin.time;

import java.time.temporal.TemporalAmount;

/**
 * Represents a temporal amount with an Arabic textual representation.
 * <p>
 * This sealed interface is the common abstraction for all Arabic-aware temporal
 * amounts, such as durations, periods, or a combination of both.
 * </p>
 *
 * <p>
 * Implementations are responsible for providing both:
 * <ul>
 * <li>An Arabic textual representation via {@link Object#toString()}</li>
 * <li>The underlying {@link TemporalAmount} representation</li>
 * </ul>
 * </p>
 *
 * <p>
 * The following implementations are permitted:
 * <ul>
 * <li>{@link ArabicDuration} – time-based amounts (hours, minutes, seconds)</li>
 * <li>{@link ArabicPeriod} – date-based amounts (years, months, days)</li>
 * <li>{@link ArabicPeriodWithDuration} – a combination of period and duration</li>
 * </ul>
 * </p>
 *
 * @author Chakib Daii
 */
public sealed interface ArabicTemporalAmount extends ArabicTemporal permits ArabicDuration,
		ArabicPeriod,
		ArabicPeriodWithDuration {
	/**
	 * Returns the underlying temporal amount represented by this Arabic temporal object.
	 *
	 * @return the temporal amount
	 */
	TemporalAmount temporalAmount();
}
