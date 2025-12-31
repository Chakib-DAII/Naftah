package org.daiitech.naftah.builtin.time;

/**
 * Provides arithmetic operations for {@code ArabicTemporalPoint} instances,
 * including addition and subtraction of hours, minutes, seconds, and nanoseconds.
 *
 * <p>This interface can be implemented by {@link ArabicTime} or
 * used by {@link ArabicDateTime} to delegate time arithmetic.</p>
 *
 * @author Chakib Daii
 */
public interface TimeSupport {
	/**
	 * Gets the hour-of-day field.
	 *
	 * @return the hour-of-day, from 0 to 23
	 */
	int getHour();

	/**
	 * Gets the minute-of-hour field.
	 *
	 * @return the minute-of-hour, from 0 to 59
	 */
	int getMinute();

	/**
	 * Gets the second-of-minute field.
	 *
	 * @return the second-of-minute, from 0 to 59
	 */
	int getSecond();

	/**
	 * Gets the millisecond-of-second from the time part of this temporal.
	 *
	 * <p>This value is derived from the nano-of-second field by dividing
	 * by {@code 1_000_000}.</p>
	 *
	 * @return the millisecond-of-second, from 0 to 999
	 */
	default int getMilli() {
		return getNano() / 1_000_000;
	}


	/**
	 * Gets the nano-of-second field.
	 *
	 * @return the nano-of-second, from 0 to 999,999,999
	 */
	int getNano();

	/**
	 * Returns a new time instance with the specified number of hours added.
	 *
	 * @param hoursToAdd the number of hours to add, may be negative
	 * @return a new {@code ArabicTemporalPoint} instance with hours added
	 */
	ArabicTemporalPoint plusHours(long hoursToAdd);

	/**
	 * Returns a new time instance with the specified number of minutes added.
	 *
	 * @param minutesToAdd the number of minutes to add, may be negative
	 * @return a new {@code ArabicTemporalPoint} instance with minutes added
	 */
	ArabicTemporalPoint plusMinutes(long minutesToAdd);

	/**
	 * Returns a new time instance with the specified number of seconds added.
	 *
	 * @param secondsToAdd the number of seconds to add, may be negative
	 * @return a new {@code ArabicTemporalPoint} instance with seconds added
	 */
	ArabicTemporalPoint plusSeconds(long secondsToAdd);

	/**
	 * Returns a new time instance with the specified number of nanoseconds added.
	 *
	 * @param nanosToAdd the number of nanoseconds to add, may be negative
	 * @return a new {@code ArabicTemporalPoint} instance with nanoseconds added
	 */
	ArabicTemporalPoint plusNanos(long nanosToAdd);

	/**
	 * Returns a new time instance with the specified number of hours subtracted.
	 *
	 * @param hoursToSubtract the number of hours to subtract, may be negative
	 * @return a new {@code ArabicTemporalPoint} instance with hours subtracted
	 */
	ArabicTemporalPoint minusHours(long hoursToSubtract);

	/**
	 * Returns a new time instance with the specified number of minutes subtracted.
	 *
	 * @param minutesToSubtract the number of minutes to subtract, may be negative
	 * @return a new {@code ArabicTemporalPoint} instance with minutes subtracted
	 */
	ArabicTemporalPoint minusMinutes(long minutesToSubtract);

	/**
	 * Returns a new time instance with the specified number of seconds subtracted.
	 *
	 * @param secondsToSubtract the number of seconds to subtract, may be negative
	 * @return a new {@code ArabicTemporalPoint} instance with seconds subtracted
	 */
	ArabicTemporalPoint minusSeconds(long secondsToSubtract);

	/**
	 * Returns a new time instance with the specified number of nanoseconds subtracted.
	 *
	 * @param nanosToSubtract the number of nanoseconds to subtract, may be negative
	 * @return a new {@code ArabicTemporalPoint} instance with nanoseconds subtracted
	 */
	ArabicTemporalPoint minusNanos(long nanosToSubtract);
}
