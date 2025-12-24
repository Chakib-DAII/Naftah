package org.daiitech.naftah.builtin.time;


import java.time.temporal.Temporal;

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
