package org.daiitech.naftah.builtin.time;

import java.time.temporal.TemporalAmount;

import org.daiitech.naftah.builtin.utils.op.BinaryOperation;

/**
 * Represents a temporal amount with a Naftah textual representation.
 * <p>
 * This sealed interface is the common abstraction for all Naftah-aware temporal
 * amounts, such as durations, periods, or a combination of both.
 * </p>
 *
 * <p>
 * Implementations are responsible for providing both:
 * <ul>
 * <li>An Naftah textual representation via {@link Object#toString()}</li>
 * <li>The underlying {@link TemporalAmount} representation</li>
 * </ul>
 * </p>
 *
 * <p>
 * The following implementations are permitted:
 * <ul>
 * <li>{@link NaftahDuration} – time-based amounts (hours, minutes, seconds)</li>
 * <li>{@link NaftahPeriod} – date-based amounts (years, months, days)</li>
 * <li>{@link NaftahPeriodWithDuration} – a combination of period and duration</li>
 * </ul>
 * </p>
 *
 * @author Chakib Daii
 */
public sealed interface NaftahTemporalAmount extends NaftahTemporal permits NaftahDuration,
		NaftahPeriod,
		NaftahPeriodWithDuration {
	/**
	 * Returns the underlying temporal amount represented by this Naftah temporal object.
	 *
	 * @return the temporal amount
	 */
	TemporalAmount temporalAmount();

	/**
	 * Returns a temporal amount obtained by adding the given Naftah temporal
	 * amount to this one.
	 * <p>
	 * The default implementation does not support this operation and always
	 * throws an exception.
	 * </p>
	 *
	 * @param other the temporal amount to add
	 * @return the resulting {@code NaftahTemporalAmount}
	 * @throws org.daiitech.naftah.errors.NaftahBugError if the operation is not supported
	 */
	default NaftahTemporalAmount plus(NaftahTemporalAmount other) {
		throw BinaryOperation.newNaftahBugError(BinaryOperation.ADD, this, other);
	}

	/**
	 * Returns a temporal amount obtained by subtracting the given Naftah temporal
	 * amount from this one.
	 * <p>
	 * The default implementation does not support this operation and always
	 * throws an exception.
	 * </p>
	 *
	 * @param other the temporal amount to subtract
	 * @return the resulting {@code NaftahTemporalAmount}
	 * @throws org.daiitech.naftah.errors.NaftahBugError if the operation is not supported
	 */
	default NaftahTemporalAmount minus(NaftahTemporalAmount other) {
		throw BinaryOperation.newNaftahBugError(BinaryOperation.SUBTRACT, this, other);
	}

	/**
	 * Determines whether this temporal amount is equal to the given temporal amount.
	 *
	 * @param other the temporal amount to compare with
	 * @return {@code true} if the temporal amounts are equal; {@code false} otherwise
	 */
	boolean isEquals(NaftahTemporalAmount other);

	/**
	 * Determines whether this temporal amount is not equal to the given temporal amount.
	 *
	 * @param other the temporal amount to compare with
	 * @return {@code true} if the temporal amounts are not equal; {@code false} otherwise
	 */
	boolean notEquals(NaftahTemporalAmount other);

	/**
	 * Determines whether this temporal amount is greater than the given temporal amount.
	 * <p>
	 * The default implementation does not support this operation and always
	 * throws an exception.
	 * </p>
	 *
	 * @param other the temporal amount to compare with
	 * @return {@code true} if this temporal amount is greater than the given one
	 * @throws org.daiitech.naftah.errors.NaftahBugError if the operation is not supported
	 */
	default boolean greaterThan(NaftahTemporalAmount other) {
		throw BinaryOperation.newNaftahBugError(BinaryOperation.GREATER_THAN, this, other);
	}

	/**
	 * Determines whether this temporal amount is greater than or equal to the
	 * given temporal amount.
	 * <p>
	 * The default implementation does not support this operation and always
	 * throws an exception.
	 * </p>
	 *
	 * @param other the temporal amount to compare with
	 * @return {@code true} if this temporal amount is greater than or equal to the given one
	 * @throws org.daiitech.naftah.errors.NaftahBugError if the operation is not supported
	 */
	default boolean greaterThanEquals(NaftahTemporalAmount other) {
		throw BinaryOperation.newNaftahBugError(BinaryOperation.GREATER_THAN_EQUALS, this, other);
	}

	/**
	 * Determines whether this temporal amount is less than the given temporal amount.
	 * <p>
	 * The default implementation does not support this operation and always
	 * throws an exception.
	 * </p>
	 *
	 * @param other the temporal amount to compare with
	 * @return {@code true} if this temporal amount is less than the given one
	 * @throws org.daiitech.naftah.errors.NaftahBugError if the operation is not supported
	 */
	default boolean lessThan(NaftahTemporalAmount other) {
		throw BinaryOperation.newNaftahBugError(BinaryOperation.LESS_THAN, this, other);
	}

	/**
	 * Determines whether this temporal amount is less than or equal to the
	 * given temporal amount.
	 * <p>
	 * The default implementation does not support this operation and always
	 * throws an exception.
	 * </p>
	 *
	 * @param other the temporal amount to compare with
	 * @return {@code true} if this temporal amount is less than or equal to the given one
	 * @throws org.daiitech.naftah.errors.NaftahBugError if the operation is not supported
	 */
	default boolean lessThanEquals(NaftahTemporalAmount other) {
		throw BinaryOperation.newNaftahBugError(BinaryOperation.LESS_THAN_EQUALS, this, other);
	}
}
