package org.daiitech.naftah.builtin.time;

import java.time.temporal.Temporal;

import org.daiitech.naftah.utils.time.TemporalUtils;

/**
 * Marker interface representing an Arabic temporal point.
 * <p>
 * A temporal point represents a specific position on the time-line (such as a date,
 * time, or date-time) with an Arabic textual representation.
 * </p>
 *
 * <p>
 * This sealed interface provides a common abstraction for all Arabic-aware temporal
 * points and allows access to their underlying {@link Temporal} representation.
 * </p>
 *
 * <p>
 * The following implementations are permitted:
 * <ul>
 * <li>{@link ArabicTime} – a time-only temporal point</li>
 * <li>{@link ArabicDate} – a date-only temporal point</li>
 * <li>{@link ArabicDateTime} – a combined date and time temporal point</li>
 * </ul>
 * </p>
 *
 * @author Chakib Daii
 */
public sealed interface ArabicTemporalPoint extends ArabicTemporal permits ArabicTime, ArabicDate, ArabicDateTime {

	/**
	 * Returns the underlying {@link Temporal} representation of this temporal point.
	 *
	 * @return the temporal representation
	 */
	Temporal temporal();

	/**
	 * Returns a temporal point obtained by adding the given Arabic temporal amount
	 * to this temporal point.
	 *
	 * @param arabicTemporalAmount the amount to add
	 * @return a new {@code ArabicTemporalPoint} with the amount added
	 */
	ArabicTemporalPoint plus(ArabicTemporalAmount arabicTemporalAmount);

	/**
	 * Returns a temporal point obtained by subtracting the given Arabic temporal
	 * amount from this temporal point.
	 *
	 * @param arabicTemporalAmount the amount to subtract
	 * @return a new {@code ArabicTemporalPoint} with the amount subtracted
	 */
	ArabicTemporalPoint minus(ArabicTemporalAmount arabicTemporalAmount);

	/**
	 * Determines whether this temporal point represents the same instant
	 * as the given temporal point.
	 *
	 * @param otherArabicTemporalPoint the temporal point to compare with
	 * @return {@code true} if both temporal points represent the same instant;
	 *         {@code false} otherwise
	 */
	default boolean isEquals(ArabicTemporalPoint otherArabicTemporalPoint) {
		var thisInstant = TemporalUtils.toInstant(temporal());
		var otherInstant = TemporalUtils.toInstant(otherArabicTemporalPoint.temporal());
		return thisInstant.equals(otherInstant);
	}

	/**
	 * Determines whether this temporal point does not represent the same instant
	 * as the given temporal point.
	 *
	 * @param otherArabicTemporalPoint the temporal point to compare with
	 * @return {@code true} if the temporal points are not equal; {@code false} otherwise
	 */
	default boolean notEquals(ArabicTemporalPoint otherArabicTemporalPoint) {
		return !isEquals(otherArabicTemporalPoint);
	}

	/**
	 * Determines whether this temporal point occurs after the given temporal point.
	 *
	 * @param otherArabicTemporalPoint the temporal point to compare with
	 * @return {@code true} if this temporal point is after the given one;
	 *         {@code false} otherwise
	 */
	default boolean greaterThan(ArabicTemporalPoint otherArabicTemporalPoint) {
		var thisInstant = TemporalUtils.toInstant(temporal());
		var otherInstant = TemporalUtils.toInstant(otherArabicTemporalPoint.temporal());
		return thisInstant.isAfter(otherInstant);
	}

	/**
	 * Determines whether this temporal point occurs after or at the same instant
	 * as the given temporal point.
	 *
	 * @param otherArabicTemporalPoint the temporal point to compare with
	 * @return {@code true} if this temporal point is after or equal to the given one;
	 *         {@code false} otherwise
	 */
	default boolean greaterThanEquals(ArabicTemporalPoint otherArabicTemporalPoint) {
		var thisInstant = TemporalUtils.toInstant(temporal());
		var otherInstant = TemporalUtils.toInstant(otherArabicTemporalPoint.temporal());
		return !thisInstant.isBefore(otherInstant);
	}

	/**
	 * Determines whether this temporal point occurs before the given temporal point.
	 *
	 * @param otherArabicTemporalPoint the temporal point to compare with
	 * @return {@code true} if this temporal point is before the given one;
	 *         {@code false} otherwise
	 */
	default boolean lessThan(ArabicTemporalPoint otherArabicTemporalPoint) {
		var thisInstant = TemporalUtils.toInstant(temporal());
		var otherInstant = TemporalUtils.toInstant(otherArabicTemporalPoint.temporal());
		return thisInstant.isBefore(otherInstant);
	}

	/**
	 * Determines whether this temporal point occurs before or at the same instant
	 * as the given temporal point.
	 *
	 * @param otherArabicTemporalPoint the temporal point to compare with
	 * @return {@code true} if this temporal point is before or equal to the given one;
	 *         {@code false} otherwise
	 */
	default boolean lessThanEquals(ArabicTemporalPoint otherArabicTemporalPoint) {
		var thisInstant = TemporalUtils.toInstant(temporal());
		var otherInstant = TemporalUtils.toInstant(otherArabicTemporalPoint.temporal());
		return !thisInstant.isAfter(otherInstant);
	}

}
