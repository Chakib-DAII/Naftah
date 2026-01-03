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
	 * Returns an {@link ArabicPeriodWithDuration} representing a zero period and zero duration.
	 *
	 * <p>The returned object has all components of both the period (years, months, days)
	 * and the duration (hours, minutes, seconds, milliseconds, nanoseconds) set to zero.</p>
	 *
	 * @return an {@link ArabicPeriodWithDuration} with zero period and zero duration
	 */
	public static ArabicPeriodWithDuration ofZero() {
		return of(ArabicPeriod.ofZero(), ArabicDuration.ofZero());
	}

	/**
	 * Obtains a {@code ArabicPeriodWithDuration} representing a number of years.
	 * <p>
	 * The resulting period will have the specified years.
	 * The months and days units will be zero.
	 *
	 * @param years the number of years, positive or negative
	 * @return the period of years, not null
	 */
	public static ArabicPeriodWithDuration ofYears(int years) {
		return of(ArabicPeriod.ofYears(years), ArabicDuration.ofZero());
	}

	/**
	 * Obtains a {@code ArabicPeriodWithDuration} representing a number of months.
	 * <p>
	 * The resulting period will have the specified months.
	 * The years and days units will be zero.
	 *
	 * @param months the number of months, positive or negative
	 * @return the period of months, not null
	 */
	public static ArabicPeriodWithDuration ofMonths(int months) {
		return of(ArabicPeriod.ofMonths(months), ArabicDuration.ofZero());
	}

	/**
	 * Obtains a {@code ArabicPeriodWithDuration} representing a number of weeks.
	 * <p>
	 * The resulting period will be day-based, with the amount of days
	 * equal to the number of weeks multiplied by 7.
	 * The years and months units will be zero.
	 *
	 * @param weeks the number of weeks, positive or negative
	 * @return the period, with the input weeks converted to days, not null
	 */
	public static ArabicPeriodWithDuration ofWeeks(int weeks) {
		return of(ArabicPeriod.ofWeeks(weeks), ArabicDuration.ofZero());
	}

	/**
	 * Obtains a {@code ArabicPeriodWithDuration} representing a number of days.
	 * <p>
	 * The resulting period will have the specified days.
	 * The years and months units will be zero.
	 *
	 * @param days the number of days, positive or negative
	 * @return the period of days, not null
	 */
	public static ArabicPeriodWithDuration ofDays(int days) {
		return of(ArabicPeriod.ofDays(days), ArabicDuration.ofZero());
	}

	/**
	 * Obtains a {@code ArabicPeriodWithDuration} representing a number of standard hours.
	 * <p>
	 * The seconds are calculated based on the standard definition of an hour,
	 * where each hour is 3600 seconds.
	 * The nanosecond in second field is set to zero.
	 *
	 * @param hours the number of hours, positive or negative
	 * @return a {@code ArabicPeriodWithDuration}, not null
	 * @throws ArithmeticException if the input hours exceeds the capacity of {@code Duration}
	 */
	public static ArabicPeriodWithDuration ofHours(long hours) {
		return of(ArabicPeriod.ofZero(), ArabicDuration.ofHours(hours));
	}

	/**
	 * Obtains a {@code ArabicPeriodWithDuration} representing a number of standard minutes.
	 * <p>
	 * The seconds are calculated based on the standard definition of a minute,
	 * where each minute is 60 seconds.
	 * The nanosecond in second field is set to zero.
	 *
	 * @param minutes the number of minutes, positive or negative
	 * @return a {@code ArabicPeriodWithDuration}, not null
	 * @throws ArithmeticException if the input minutes exceeds the capacity of {@code Duration}
	 */
	public static ArabicPeriodWithDuration ofMinutes(long minutes) {
		return of(ArabicPeriod.ofZero(), ArabicDuration.ofMinutes(minutes));
	}

	/**
	 * Obtains a {@code ArabicPeriodWithDuration} representing a number of seconds.
	 * <p>
	 * The nanosecond in second field is set to zero.
	 *
	 * @param seconds the number of seconds, positive or negative
	 * @return a {@code ArabicPeriodWithDuration}, not null
	 */
	public static ArabicPeriodWithDuration ofSeconds(long seconds) {
		return of(ArabicPeriod.ofZero(), ArabicDuration.ofSeconds(seconds));
	}

	/**
	 * Obtains a {@code ArabicPeriodWithDuration} representing a number of seconds and an
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
	 * @return a {@code ArabicPeriodWithDuration}, not null
	 * @throws ArithmeticException if the adjustment causes the seconds to exceed the capacity of {@code Duration}
	 */
	public static ArabicPeriodWithDuration ofSeconds(long seconds, long nanoAdjustment) {
		return of(ArabicPeriod.ofZero(), ArabicDuration.ofSeconds(seconds, nanoAdjustment));
	}

	/**
	 * Obtains a {@code ArabicPeriodWithDuration} representing a number of milliseconds.
	 * <p>
	 * The seconds and nanoseconds are extracted from the specified milliseconds.
	 *
	 * @param millis the number of milliseconds, positive or negative
	 * @return a {@code ArabicPeriodWithDuration}, not null
	 */
	public static ArabicPeriodWithDuration ofMillis(long millis) {
		return of(ArabicPeriod.ofZero(), ArabicDuration.ofMillis(millis));
	}

	/**
	 * Obtains a {@code ArabicPeriodWithDuration} representing a number of nanoseconds.
	 * <p>
	 * The seconds and nanoseconds are extracted from the specified nanoseconds.
	 *
	 * @param nanos the number of nanoseconds, positive or negative
	 * @return a {@code ArabicPeriodWithDuration}, not null
	 */
	public static ArabicPeriodWithDuration ofNanos(long nanos) {
		return of(ArabicPeriod.ofZero(), ArabicDuration.ofNanos(nanos));
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
	 * Returns the number of years in this period.
	 *
	 * @return the years component of the period
	 */
	public int getYears() {
		return arabicPeriod.getYears();
	}

	/**
	 * Returns the number of months in this period.
	 *
	 * @return the months component of the period
	 */
	public int getMonths() {
		return arabicPeriod.getMonths();
	}

	/**
	 * Returns the number of days in this period.
	 *
	 * @return the days component of the period
	 */
	public int getDays() {
		return arabicPeriod.getDays();
	}

	/**
	 * Returns the total number of hours in this duration.
	 *
	 * @return the total hours
	 */
	public long getHours() {
		return arabicDuration.getHours();
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
		return arabicDuration.getMinutes();
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
		return arabicDuration.getSeconds();
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
		return arabicDuration.getMillis();
	}

	/**
	 * Returns the nanosecond-of-second component of this duration.
	 *
	 * @return the nanoseconds within the current second (0-999,999,999)
	 */
	public int getNano() {
		return arabicDuration.getNano();
	}

	/**
	 * Adds the given Arabic temporal amount to this instance.
	 *
	 * <p>
	 * Supported combinations:
	 * <ul>
	 * <li>{@link ArabicDuration}</li>
	 * <li>{@link ArabicPeriodWithDuration}</li>
	 * <li>Any {@link ArabicTemporalAmount} convertible to a {@link Period}</li>
	 * </ul>
	 * </p>
	 *
	 * @param other the temporal amount to add
	 * @return a new normalized {@code ArabicPeriodWithDuration}
	 */
	@Override
	public ArabicPeriodWithDuration plus(ArabicTemporalAmount other) {
		if (other instanceof ArabicDuration otherDur) {
			Duration result = this.arabicDuration.temporalAmount().plus(otherDur.temporalAmount());
			return ArabicPeriodWithDuration.of(arabicPeriod, ArabicDuration.of(result));
		}
		else if (other instanceof ArabicPeriodWithDuration otherPeriodWithDuration) {
			Period period = this.arabicPeriod
					.temporalAmount()
					.plus(otherPeriodWithDuration.arabicPeriod.temporalAmount());
			Duration duration = this.arabicDuration
					.temporalAmount()
					.plus(otherPeriodWithDuration.arabicDuration.temporalAmount());

			return ArabicPeriodWithDuration.of(ArabicPeriod.of(period), ArabicDuration.of(duration));
		}
		else {
			Period result = this.arabicPeriod.temporalAmount().plus(other.temporalAmount());
			return ArabicPeriodWithDuration.of(ArabicPeriod.of(result), arabicDuration);
		}
	}

	/**
	 * Returns a copy of this period with the specified years added.
	 * <p>
	 * This adds the amount to the years unit in a copy of this period.
	 * The months and days units are unaffected.
	 * For example, "1 year, 6 months and 3 days" plus 2 years returns "3 years, 6 months and 3 days".
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 *
	 * @param yearsToAdd the years to add, positive or negative
	 * @return a {@code ArabicPeriodWithDuration} based on this period with the specified years added, not null
	 * @throws ArithmeticException if numeric overflow occurs
	 */
	public ArabicPeriodWithDuration plusYears(long yearsToAdd) {
		if (yearsToAdd == 0) {
			return this;
		}
		return of(arabicPeriod.plusYears(yearsToAdd), arabicDuration);
	}

	/**
	 * Returns a copy of this period with the specified months added.
	 * <p>
	 * This adds the amount to the months unit in a copy of this period.
	 * The years and days units are unaffected.
	 * For example, "1 year, 6 months and 3 days" plus 2 months returns "1 year, 8 months and 3 days".
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 *
	 * @param monthsToAdd the months to add, positive or negative
	 * @return a {@code ArabicPeriodWithDuration} based on this period with the specified months added, not null
	 * @throws ArithmeticException if numeric overflow occurs
	 */
	public ArabicPeriodWithDuration plusMonths(long monthsToAdd) {
		if (monthsToAdd == 0) {
			return this;
		}
		return of(arabicPeriod.plusMonths(monthsToAdd), arabicDuration);
	}

	/**
	 * Returns a copy of this period with the specified days added.
	 * <p>
	 * This adds the amount to the days unit in a copy of this period.
	 * The years and months units are unaffected.
	 * For example, "1 year, 6 months and 3 days" plus 2 days returns "1 year, 6 months and 5 days".
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 *
	 * @param daysToAdd the days to add, positive or negative
	 * @return a {@code ArabicPeriodWithDuration} based on this period with the specified days added, not null
	 * @throws ArithmeticException if numeric overflow occurs
	 */
	public ArabicPeriodWithDuration plusDays(long daysToAdd) {
		if (daysToAdd == 0) {
			return this;
		}
		return of(arabicPeriod.plusDays(daysToAdd), arabicDuration);
	}


	/**
	 * Returns a copy of this duration with the specified duration in hours added.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 *
	 * @param hoursToAdd the hours to add, positive or negative
	 * @return a {@code ArabicPeriodWithDuration} based on this duration with the specified hours added, not null
	 * @throws ArithmeticException if numeric overflow occurs
	 */
	public ArabicPeriodWithDuration plusHours(long hoursToAdd) {
		if (hoursToAdd == 0) {
			return this;
		}
		return of(arabicPeriod, arabicDuration.plusHours(hoursToAdd));
	}

	/**
	 * Returns a copy of this duration with the specified duration in minutes added.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 *
	 * @param minutesToAdd the minutes to add, positive or negative
	 * @return a {@code ArabicPeriodWithDuration} based on this duration with the specified minutes added, not null
	 * @throws ArithmeticException if numeric overflow occurs
	 */
	public ArabicPeriodWithDuration plusMinutes(long minutesToAdd) {
		if (minutesToAdd == 0) {
			return this;
		}
		return of(arabicPeriod, arabicDuration.plusMinutes(minutesToAdd));
	}

	/**
	 * Returns a copy of this duration with the specified duration in seconds added.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 *
	 * @param secondsToAdd the seconds to add, positive or negative
	 * @return a {@code ArabicPeriodWithDuration} based on this duration with the specified seconds added, not null
	 * @throws ArithmeticException if numeric overflow occurs
	 */
	public ArabicPeriodWithDuration plusSeconds(long secondsToAdd) {
		if (secondsToAdd == 0) {
			return this;
		}
		return of(arabicPeriod, arabicDuration.plusSeconds(secondsToAdd));
	}

	/**
	 * Returns a copy of this duration with the specified duration in milliseconds added.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 *
	 * @param millisToAdd the milliseconds to add, positive or negative
	 * @return a {@code ArabicPeriodWithDuration} based on this duration with the specified milliseconds added, not
	 *         * null
	 * @throws ArithmeticException if numeric overflow occurs
	 */
	public ArabicPeriodWithDuration plusMillis(long millisToAdd) {
		if (millisToAdd == 0) {
			return this;
		}
		return of(arabicPeriod, arabicDuration.plusMillis(millisToAdd));
	}

	/**
	 * Returns a copy of this duration with the specified duration in nanoseconds added.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 *
	 * @param nanosToAdd the nanoseconds to add, positive or negative
	 * @return a {@code ArabicPeriodWithDuration} based on this duration with the specified nanoseconds added, not null
	 * @throws ArithmeticException if numeric overflow occurs
	 */
	public ArabicPeriodWithDuration plusNanos(long nanosToAdd) {
		if (nanosToAdd == 0) {
			return this;
		}
		return of(arabicPeriod, arabicDuration.plusNanos(nanosToAdd));
	}

	/**
	 * Subtracts the given Arabic temporal amount from this instance.
	 *
	 * <p>
	 * Supported combinations mirror those of {@link #plus(ArabicTemporalAmount)}.
	 * </p>
	 *
	 * @param other the temporal amount to subtract
	 * @return a new normalized {@code ArabicPeriodWithDuration}
	 */
	@Override
	public ArabicPeriodWithDuration minus(ArabicTemporalAmount other) {
		if (other instanceof ArabicDuration otherDur) {
			Duration result = this.arabicDuration.temporalAmount().minus(otherDur.temporalAmount());
			return ArabicPeriodWithDuration.of(arabicPeriod, ArabicDuration.of(result));
		}
		else if (other instanceof ArabicPeriodWithDuration otherPeriodWithDuration) {
			Period period = this.arabicPeriod
					.temporalAmount()
					.minus(otherPeriodWithDuration.arabicPeriod.temporalAmount());
			Duration duration = this.arabicDuration
					.temporalAmount()
					.minus(otherPeriodWithDuration.arabicDuration.temporalAmount());

			return ArabicPeriodWithDuration.of(ArabicPeriod.of(period), ArabicDuration.of(duration));
		}
		else {
			Period result = this.arabicPeriod.temporalAmount().minus(other.temporalAmount());
			return ArabicPeriodWithDuration.of(ArabicPeriod.of(result), arabicDuration);
		}
	}

	/**
	 * Returns a copy of this period with the specified years subtracted.
	 * <p>
	 * This subtracts the amount from the years unit in a copy of this period.
	 * The months and days units are unaffected.
	 * For example, "1 year, 6 months and 3 days" minus 2 years returns "-1 years, 6 months and 3 days".
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 *
	 * @param yearsToSubtract the years to subtract, positive or negative
	 * @return a {@code ArabicPeriodWithDuration} based on this period with the specified years subtracted, not null
	 * @throws ArithmeticException if numeric overflow occurs
	 */
	public ArabicPeriodWithDuration minusYears(long yearsToSubtract) {
		if (yearsToSubtract == 0) {
			return this;
		}
		return of(arabicPeriod.minusYears(yearsToSubtract), arabicDuration);
	}

	/**
	 * Returns a copy of this period with the specified months subtracted.
	 * <p>
	 * This subtracts the amount from the months unit in a copy of this period.
	 * The years and days units are unaffected.
	 * For example, "1 year, 6 months and 3 days" minus 2 months returns "1 year, 4 months and 3 days".
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 *
	 * @param monthsToSubtract the years to subtract, positive or negative
	 * @return a {@code ArabicPeriodWithDuration} based on this period with the specified months subtracted, not null
	 * @throws ArithmeticException if numeric overflow occurs
	 */
	public ArabicPeriodWithDuration minusMonths(long monthsToSubtract) {
		if (monthsToSubtract == 0) {
			return this;
		}
		return of(arabicPeriod.minusMonths(monthsToSubtract), arabicDuration);
	}

	/**
	 * Returns a copy of this period with the specified days subtracted.
	 * <p>
	 * This subtracts the amount from the days unit in a copy of this period.
	 * The years and months units are unaffected.
	 * For example, "1 year, 6 months and 3 days" minus 2 days returns "1 year, 6 months and 1 day".
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 *
	 * @param daysToSubtract the months to subtract, positive or negative
	 * @return a {@code ArabicPeriodWithDuration} based on this period with the specified days subtracted, not null
	 * @throws ArithmeticException if numeric overflow occurs
	 */
	public ArabicPeriodWithDuration minusDays(long daysToSubtract) {
		if (daysToSubtract == 0) {
			return this;
		}
		return of(arabicPeriod.minusDays(daysToSubtract), arabicDuration);
	}

	/**
	 * Returns a copy of this duration with the specified duration in hours subtracted.
	 * <p>
	 * The number of hours is multiplied by 3600 to obtain the number of seconds to subtract.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 *
	 * @param hoursToSubtract the hours to subtract, positive or negative
	 * @return a {@code ArabicPeriodWithDuration} based on this duration with the specified hours subtracted, not null
	 * @throws ArithmeticException if numeric overflow occurs
	 */
	public ArabicPeriodWithDuration minusHours(long hoursToSubtract) {
		if (hoursToSubtract == 0) {
			return this;
		}
		return of(arabicPeriod, arabicDuration.minusHours(hoursToSubtract));
	}

	/**
	 * Returns a copy of this duration with the specified duration in minutes subtracted.
	 * <p>
	 * The number of hours is multiplied by 60 to obtain the number of seconds to subtract.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 *
	 * @param minutesToSubtract the minutes to subtract, positive or negative
	 * @return a {@code ArabicPeriodWithDuration} based on this duration with the specified minutes subtracted, not
	 *         * null
	 * @throws ArithmeticException if numeric overflow occurs
	 */
	public ArabicPeriodWithDuration minusMinutes(long minutesToSubtract) {
		if (minutesToSubtract == 0) {
			return this;
		}
		return of(arabicPeriod, arabicDuration.minusMinutes(minutesToSubtract));
	}

	/**
	 * Returns a copy of this duration with the specified duration in seconds subtracted.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 *
	 * @param secondsToSubtract the seconds to subtract, positive or negative
	 * @return a {@code ArabicPeriodWithDuration} based on this duration with the specified seconds subtracted, not
	 *         * null
	 * @throws ArithmeticException if numeric overflow occurs
	 */
	public ArabicPeriodWithDuration minusSeconds(long secondsToSubtract) {
		if (secondsToSubtract == 0) {
			return this;
		}
		return of(arabicPeriod, arabicDuration.minusSeconds(secondsToSubtract));
	}

	/**
	 * Returns a copy of this duration with the specified duration in milliseconds subtracted.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 *
	 * @param millisToSubtract the milliseconds to subtract, positive or negative
	 * @return a {@code ArabicPeriodWithDuration} based on this duration with the specified milliseconds subtracted,
	 *         * not null
	 * @throws ArithmeticException if numeric overflow occurs
	 */
	public ArabicPeriodWithDuration minusMillis(long millisToSubtract) {
		if (millisToSubtract == 0) {
			return this;
		}
		return of(arabicPeriod, arabicDuration.minusMillis(millisToSubtract));
	}

	/**
	 * Returns a copy of this duration with the specified duration in nanoseconds subtracted.
	 * <p>
	 * This instance is immutable and unaffected by this method call.
	 *
	 * @param nanosToSubtract the nanoseconds to subtract, positive or negative
	 * @return a {@code ArabicPeriodWithDuration} based on this duration with the specified nanoseconds subtracted,
	 *         * not null
	 * @throws ArithmeticException if numeric overflow occurs
	 */
	public ArabicPeriodWithDuration minusNanos(long nanosToSubtract) {
		if (nanosToSubtract == 0) {
			return this;
		}
		return of(arabicPeriod, arabicDuration.minusNanos(nanosToSubtract));
	}

	/**
	 * Determines whether this temporal amount is equal to the given one.
	 *
	 * <p>
	 * Equality requires both the period and duration components
	 * to be equal.
	 * </p>
	 *
	 * @param other the temporal amount to compare with
	 * @return {@code true} if both components are equal
	 */
	@Override
	public boolean isEquals(ArabicTemporalAmount other) {
		if (other instanceof ArabicPeriodWithDuration otherPeriodWithDuration) {
			return this.arabicPeriod
					.isEquals(otherPeriodWithDuration.arabicPeriod) && this.arabicDuration
							.isEquals(otherPeriodWithDuration.arabicDuration);
		}
		return false;
	}

	/**
	 * Determines whether this temporal amount is not equal to the given one.
	 *
	 * @param other the temporal amount to compare with
	 * @return {@code true} if the amounts are not equal
	 */
	@Override
	public boolean notEquals(ArabicTemporalAmount other) {
		return !isEquals(other);
	}

	/**
	 * Determines whether this temporal amount is greater than the given one.
	 *
	 * <p>
	 * Comparison is performed component-wise:
	 * both the period and duration must be greater.
	 * </p>
	 *
	 * @param other the temporal amount to compare with
	 * @return {@code true} if both components are greater
	 */
	@Override
	public boolean greaterThan(ArabicTemporalAmount other) {
		if (other instanceof ArabicPeriodWithDuration otherPeriodWithDuration) {
			return this.arabicPeriod
					.greaterThan(otherPeriodWithDuration.arabicPeriod) && this.arabicDuration
							.greaterThan(otherPeriodWithDuration.arabicDuration);
		}
		return ArabicTemporalAmount.super.greaterThan(other);
	}

	/**
	 * Determines whether this temporal amount is greater than or equal
	 * to the given one.
	 *
	 * @param other the temporal amount to compare with
	 * @return {@code true} if both components are greater than or equal
	 */
	@Override
	public boolean greaterThanEquals(ArabicTemporalAmount other) {
		if (other instanceof ArabicPeriodWithDuration otherPeriodWithDuration) {
			return this.arabicPeriod
					.greaterThanEquals(otherPeriodWithDuration.arabicPeriod) && this.arabicDuration
							.greaterThanEquals(otherPeriodWithDuration.arabicDuration);
		}
		return ArabicTemporalAmount.super.greaterThanEquals(other);
	}

	/**
	 * Determines whether this temporal amount is less than the given one.
	 *
	 * @param other the temporal amount to compare with
	 * @return {@code true} if both components are less
	 */
	@Override
	public boolean lessThan(ArabicTemporalAmount other) {
		if (other instanceof ArabicPeriodWithDuration otherPeriodWithDuration) {
			return this.arabicPeriod
					.lessThan(otherPeriodWithDuration.arabicPeriod) && this.arabicDuration
							.lessThan(otherPeriodWithDuration.arabicDuration);
		}
		return ArabicTemporalAmount.super.lessThan(other);
	}

	/**
	 * Determines whether this temporal amount is less than or equal
	 * to the given one.
	 *
	 * @param other the temporal amount to compare with
	 * @return {@code true} if both components are less than or equal
	 */
	@Override
	public boolean lessThanEquals(ArabicTemporalAmount other) {
		if (other instanceof ArabicPeriodWithDuration otherPeriodWithDuration) {
			return this.arabicPeriod
					.lessThanEquals(otherPeriodWithDuration.arabicPeriod) && this.arabicDuration
							.lessThanEquals(otherPeriodWithDuration.arabicDuration);
		}
		return ArabicTemporalAmount.super.lessThanEquals(other);
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
