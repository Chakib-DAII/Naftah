package org.daiitech.naftah.builtin.time;


import java.time.Duration;
import java.time.Period;
import java.time.chrono.Chronology;
import java.time.temporal.Temporal;

import org.daiitech.naftah.utils.time.ChronologyUtils;
import org.daiitech.naftah.utils.time.TemporalUtils;

/**
 * Represents a complete Arabic date-time, consisting of:
 * <ul>
 * <li>An {@link ArabicDate} component</li>
 * <li>An {@link ArabicTime} component</li>
 * <li>A resolved {@link Temporal} representation</li>
 * </ul>
 *
 * <p>This record is typically produced after parsing Arabic date and time
 * expressions and resolving them against a specific chronology or calendar system.</p>
 *
 * <p>The {@link Temporal} allows integration with Java's time API.</p>
 *
 * @param arabicDate the date component
 * @param arabicTime the time component
 * @param temporal   the resolved temporal
 * @author Chakib Daii
 */
public record ArabicDateTime(
		ArabicDate arabicDate,
		ArabicTime arabicTime,
		Temporal temporal
) implements ArabicTemporalPoint {
	/**
	 * Obtains the current date-time using the default chronology
	 * and the system default time zone.
	 *
	 * @return the current {@code ArabicDateTime}
	 */
	public static ArabicDateTime now() {
		return now(ChronologyUtils.DEFAULT_CHRONOLOGY);
	}

	/**
	 * Obtains the current date-time using the specified chronology
	 * and the system default time zone.
	 *
	 * @param chronology the chronology to use (not {@code null})
	 * @return the current {@code ArabicDateTime}
	 * @throws NullPointerException if {@code chronology} is {@code null}
	 */
	public static ArabicDateTime now(Chronology chronology) {
		var calendar = ArabicDate.Calendar.of(chronology);
		return now(calendar);
	}

	/**
	 * Obtains the current date-time using the specified calendar
	 * and the system default time zone.
	 *
	 * @param calendar the calendar to use (not {@code null})
	 * @return the current {@code ArabicDateTime}
	 * @throws NullPointerException if {@code calendar} is {@code null}
	 */
	public static ArabicDateTime now(ArabicDate.Calendar calendar) {
		return now(calendar, null);
	}

	/**
	 * Obtains the current date-time using the default chronology
	 * and the specified zone or offset.
	 *
	 * @param zoneOrOffset the zone or offset to use, or {@code null}
	 *                     to use the system default
	 * @return the current {@code ArabicDateTime}
	 */
	public static ArabicDateTime now(ArabicTime.ZoneOrOffset zoneOrOffset) {
		var calendar = ArabicDate.Calendar.of(ChronologyUtils.DEFAULT_CHRONOLOGY);
		return now(calendar, zoneOrOffset);
	}

	/**
	 * Obtains the current date-time using the specified chronology
	 * and zone or offset.
	 *
	 * @param chronology   the chronology to use (not {@code null})
	 * @param zoneOrOffset the zone or offset to use, or {@code null}
	 *                     to use the system default
	 * @return the current {@code ArabicDateTime}
	 * @throws NullPointerException if {@code chronology} is {@code null}
	 */
	public static ArabicDateTime now(Chronology chronology, ArabicTime.ZoneOrOffset zoneOrOffset) {
		var calendar = ArabicDate.Calendar.of(chronology);
		return now(calendar, zoneOrOffset);
	}

	/**
	 * Obtains the current date-time using the specified calendar
	 * and zone or offset.
	 *
	 * <p>
	 * The returned {@code ArabicDateTime} represents the current
	 * date and time as resolved by the given calendar and
	 * zone or offset.
	 * </p>
	 *
	 * @param calendar     the calendar to use (not {@code null})
	 * @param zoneOrOffset the zone or offset to use, or {@code null}
	 *                     to use the system default
	 * @return the current {@code ArabicDateTime}
	 * @throws NullPointerException if {@code calendar} is {@code null}
	 */
	public static ArabicDateTime now(ArabicDate.Calendar calendar, ArabicTime.ZoneOrOffset zoneOrOffset) {
		ArabicDate date = ArabicDate.now(calendar, zoneOrOffset);
		ArabicTime time = ArabicTime.now(zoneOrOffset);
		return ArabicDateTime
				.of(
					date,
					time,
					TemporalUtils
							.createDateTime(date.date(),
											date.calendar(),
											time.time(),
											time.zoneOrOffset())
				);
	}

	/**
	 * Creates a new {@code ArabicDateTime} instance.
	 *
	 * @param arabicDate the date component
	 * @param arabicTime the time component
	 * @param temporal   the resolved temporal
	 * @return a new {@code ArabicDateTime} instance
	 */
	public static ArabicDateTime of(
									ArabicDate arabicDate,
									ArabicTime arabicTime,
									Temporal temporal) {
		return new ArabicDateTime(arabicDate, arabicTime, temporal);
	}

	/**
	 * Returns a new {@code ArabicDateTime} obtained by adding the given
	 * Arabic temporal amount to this date-time.
	 *
	 * @param arabicTemporalAmount the temporal amount to add
	 * @return a new {@code ArabicDateTime} instance
	 */
	@Override
	public ArabicDateTime plus(ArabicTemporalAmount arabicTemporalAmount) {
		return compute(arabicTemporalAmount, true);
	}

	/**
	 * Returns a new {@code ArabicDateTime} obtained by subtracting the given
	 * Arabic temporal amount from this date-time.
	 *
	 * @param arabicTemporalAmount the temporal amount to subtract
	 * @return a new {@code ArabicDateTime} instance
	 */
	@Override
	public ArabicDateTime minus(ArabicTemporalAmount arabicTemporalAmount) {
		return compute(arabicTemporalAmount, false);

	}

	/**
	 * Computes a new {@code ArabicDateTime} by applying the given Arabic temporal
	 * amount using either addition or subtraction.
	 *
	 * <p>
	 * Supported temporal amounts:
	 * <ul>
	 * <li>{@link ArabicDuration}</li>
	 * <li>{@link ArabicPeriod}</li>
	 * <li>{@link ArabicPeriodWithDuration}</li>
	 * </ul>
	 * </p>
	 *
	 * <p>
	 * Duration handling rules:
	 * <ul>
	 * <li>Durations shorter than 24 hours affect only the time component</li>
	 * <li>Durations of 24 hours or more are split into a date-based
	 * {@link Period} and a remaining time-based {@link Duration}</li>
	 * </ul>
	 * </p>
	 *
	 * <p>
	 * Date and time components are updated independently while the resolved
	 * {@link Temporal} is updated in full.
	 * </p>
	 *
	 * @param arabicTemporalAmount the temporal amount to apply
	 * @param plus                 {@code true} to add, {@code false} to subtract
	 * @return a new {@code ArabicDateTime} instance
	 */
	private ArabicDateTime compute(ArabicTemporalAmount arabicTemporalAmount, boolean plus) {
		if (arabicTemporalAmount instanceof ArabicDuration arabicDuration) {
			var duration = arabicDuration.temporalAmount();
			long hours = duration.toHours();
			if (hours < 24) {
				return of(  arabicDate,
							arabicTime
									.compute(   arabicTemporalAmount,
												plus ? arabicTime.temporal()::plus : arabicTime.temporal()::minus),
							plus ? this.temporal.plus(duration) : this.temporal.minus(duration)
				);
			}
			else {
				long totalSeconds = duration.getSeconds();
				long nanos = duration.getNano();

				hours = totalSeconds / 3600;
				long extraDays = hours / 24;
				long remainingHours = hours % 24;
				long minutes = (totalSeconds % 3600) / 60;
				long seconds = totalSeconds % 60;
				long millis = nanos / 1_000_000;
				long remainingNanos = nanos % 1_000_000;

				Period period = Period.ofDays((int) extraDays);

				// Adjust duration to less than 24 hours
				Duration newDuration = Duration
						.ofHours(remainingHours)
						.plusMinutes(minutes)
						.plusMillis(millis)
						.plusSeconds(seconds)
						.plusNanos(remainingNanos);


				return of(  ArabicDate
									.of(arabicDate.calendar(),
										plus ?
												arabicDate.temporal().plus(period) :
												arabicDate.temporal().minus(period)),
							ArabicTime
									.of(arabicTime.zoneOrOffset(),
										plus ?
												arabicTime.temporal().plus(newDuration) :
												arabicTime.temporal().minus(newDuration)),
							plus ? this.temporal.plus(duration) : this.temporal.minus(duration)
				);
			}
		}
		else if (arabicTemporalAmount instanceof ArabicPeriodWithDuration arabicPeriodWithDuration) {
			Period period = arabicPeriodWithDuration.arabicPeriod().temporalAmount();
			Duration duration = arabicPeriodWithDuration.arabicDuration().temporalAmount();

			return of(  ArabicDate
								.of(arabicDate.calendar(),
									plus ? arabicDate.temporal().plus(period) : arabicDate.temporal().minus(period)),
						ArabicTime
								.of(arabicTime.zoneOrOffset(),
									plus ?
											arabicTime.temporal().plus(duration) :
											arabicTime.temporal().minus(duration)),
						plus ? this.temporal.plus(period).plus(duration) : this.temporal.minus(period).minus(duration)
			);
		}
		else {
			var period = arabicTemporalAmount.temporalAmount();

			return of(  ArabicDate
								.of(arabicDate.calendar(),
									plus ? arabicDate.temporal().plus(period) : arabicDate.temporal().minus(period)),
						arabicTime,
						plus ? this.temporal.plus(period) : this.temporal.minus(period)
			);
		}
	}

	/**
	 * Returns a string representation of this {@code ArabicDateTime} in the format:
	 * "date time".
	 *
	 * <p>Both {@link ArabicDate} and {@link ArabicTime} components are included.</p>
	 *
	 * @return a formatted string representing the Arabic date-time
	 */
	@Override
	public String toString() {

		String sb = arabicDate + " " + arabicTime;

		return sb.trim();
	}
}
