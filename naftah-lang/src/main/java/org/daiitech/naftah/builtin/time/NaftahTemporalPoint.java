package org.daiitech.naftah.builtin.time;

import java.time.temporal.Temporal;

import org.daiitech.naftah.utils.time.TemporalUtils;

/**
 * Marker interface representing a Naftah temporal point.
 * <p>
 * A temporal point represents a specific position on the time-line (such as a date,
 * time, or date-time) with an Naftah textual representation.
 * </p>
 *
 * <p>
 * This sealed interface provides a common abstraction for all Naftah-aware temporal
 * points and allows access to their underlying {@link Temporal} representation.
 * </p>
 *
 * <p>
 * The following implementations are permitted:
 * <ul>
 * <li>{@link NaftahTime} – a time-only temporal point</li>
 * <li>{@link NaftahDate} – a date-only temporal point</li>
 * <li>{@link NaftahDateTime} – a combined date and time temporal point</li>
 * </ul>
 * </p>
 *
 * @author Chakib Daii
 */
public sealed interface NaftahTemporalPoint extends NaftahTemporal permits NaftahTime, NaftahDate, NaftahDateTime {

	/**
	 * Returns the underlying {@link Temporal} representation of this temporal point.
	 *
	 * @return the temporal representation
	 */
	Temporal temporal();

	/**
	 * Returns a temporal point obtained by adding the given Naftah temporal amount
	 * to this temporal point.
	 *
	 * @param naftahTemporalAmount the amount to add
	 * @return a new {@code NaftahTemporalPoint} with the amount added
	 */
	NaftahTemporalPoint plus(NaftahTemporalAmount naftahTemporalAmount);

	/**
	 * Returns a temporal point obtained by subtracting the given Naftah temporal
	 * amount from this temporal point.
	 *
	 * @param naftahTemporalAmount the amount to subtract
	 * @return a new {@code NaftahTemporalPoint} with the amount subtracted
	 */
	NaftahTemporalPoint minus(NaftahTemporalAmount naftahTemporalAmount);

	/**
	 * Determines whether this temporal point represents the same instant
	 * as the given temporal point.
	 *
	 * @param otherNaftahTemporalPoint the temporal point to compare with
	 * @return {@code true} if both temporal points represent the same instant;
	 *         {@code false} otherwise
	 */
	default boolean isEquals(NaftahTemporalPoint otherNaftahTemporalPoint) {
		var thisInstant = TemporalUtils.toInstant(temporal());
		var otherInstant = TemporalUtils.toInstant(otherNaftahTemporalPoint.temporal());
		return thisInstant.equals(otherInstant);
	}

	/**
	 * Determines whether this temporal point does not represent the same instant
	 * as the given temporal point.
	 *
	 * @param otherNaftahTemporalPoint the temporal point to compare with
	 * @return {@code true} if the temporal points are not equal; {@code false} otherwise
	 */
	default boolean notEquals(NaftahTemporalPoint otherNaftahTemporalPoint) {
		return !isEquals(otherNaftahTemporalPoint);
	}

	/**
	 * Determines whether this temporal point occurs after the given temporal point.
	 *
	 * @param otherNaftahTemporalPoint the temporal point to compare with
	 * @return {@code true} if this temporal point is after the given one;
	 *         {@code false} otherwise
	 */
	default boolean greaterThan(NaftahTemporalPoint otherNaftahTemporalPoint) {
		var thisInstant = TemporalUtils.toInstant(temporal());
		var otherInstant = TemporalUtils.toInstant(otherNaftahTemporalPoint.temporal());
		return thisInstant.isAfter(otherInstant);
	}

	/**
	 * Determines whether this temporal point occurs after or at the same instant
	 * as the given temporal point.
	 *
	 * @param otherNaftahTemporalPoint the temporal point to compare with
	 * @return {@code true} if this temporal point is after or equal to the given one;
	 *         {@code false} otherwise
	 */
	default boolean greaterThanEquals(NaftahTemporalPoint otherNaftahTemporalPoint) {
		var thisInstant = TemporalUtils.toInstant(temporal());
		var otherInstant = TemporalUtils.toInstant(otherNaftahTemporalPoint.temporal());
		return !thisInstant.isBefore(otherInstant);
	}

	/**
	 * Determines whether this temporal point occurs before the given temporal point.
	 *
	 * @param otherNaftahTemporalPoint the temporal point to compare with
	 * @return {@code true} if this temporal point is before the given one;
	 *         {@code false} otherwise
	 */
	default boolean lessThan(NaftahTemporalPoint otherNaftahTemporalPoint) {
		var thisInstant = TemporalUtils.toInstant(temporal());
		var otherInstant = TemporalUtils.toInstant(otherNaftahTemporalPoint.temporal());
		return thisInstant.isBefore(otherInstant);
	}

	/**
	 * Determines whether this temporal point occurs before or at the same instant
	 * as the given temporal point.
	 *
	 * @param otherNaftahTemporalPoint the temporal point to compare with
	 * @return {@code true} if this temporal point is before or equal to the given one;
	 *         {@code false} otherwise
	 */
	default boolean lessThanEquals(NaftahTemporalPoint otherNaftahTemporalPoint) {
		var thisInstant = TemporalUtils.toInstant(temporal());
		var otherInstant = TemporalUtils.toInstant(otherNaftahTemporalPoint.temporal());
		return !thisInstant.isAfter(otherInstant);
	}

}
