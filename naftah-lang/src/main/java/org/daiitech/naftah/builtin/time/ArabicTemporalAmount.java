package org.daiitech.naftah.builtin.time;

import java.time.temporal.TemporalAmount;

import org.daiitech.naftah.builtin.utils.op.BinaryOperation;

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

	/**
	 * Returns a temporal amount obtained by adding the given Arabic temporal
	 * amount to this one.
	 * <p>
	 * The default implementation does not support this operation and always
	 * throws an exception.
	 * </p>
	 *
	 * @param other the temporal amount to add
	 * @return the resulting {@code ArabicTemporalAmount}
	 * @throws org.daiitech.naftah.errors.NaftahBugError if the operation is not supported
	 */
	default ArabicTemporalAmount plus(ArabicTemporalAmount other) {
		throw BinaryOperation.newNaftahBugError(BinaryOperation.ADD, this, other);
	}

	/**
	 * Returns a temporal amount obtained by subtracting the given Arabic temporal
	 * amount from this one.
	 * <p>
	 * The default implementation does not support this operation and always
	 * throws an exception.
	 * </p>
	 *
	 * @param other the temporal amount to subtract
	 * @return the resulting {@code ArabicTemporalAmount}
	 * @throws org.daiitech.naftah.errors.NaftahBugError if the operation is not supported
	 */
	default ArabicTemporalAmount minus(ArabicTemporalAmount other) {
		throw BinaryOperation.newNaftahBugError(BinaryOperation.SUBTRACT, this, other);
	}

	/**
	 * Determines whether this temporal amount is equal to the given temporal amount.
	 *
	 * @param other the temporal amount to compare with
	 * @return {@code true} if the temporal amounts are equal; {@code false} otherwise
	 */
	boolean isEquals(ArabicTemporalAmount other);

	/**
	 * Determines whether this temporal amount is not equal to the given temporal amount.
	 *
	 * @param other the temporal amount to compare with
	 * @return {@code true} if the temporal amounts are not equal; {@code false} otherwise
	 */
	boolean notEquals(ArabicTemporalAmount other);

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
	default boolean greaterThan(ArabicTemporalAmount other) {
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
	default boolean greaterThanEquals(ArabicTemporalAmount other) {
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
	default boolean lessThan(ArabicTemporalAmount other) {
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
	default boolean lessThanEquals(ArabicTemporalAmount other) {
		throw BinaryOperation.newNaftahBugError(BinaryOperation.LESS_THAN_EQUALS, this, other);
	}
}
