package org.daiitech.naftah.builtin.time;

import java.time.Period;
import java.util.Objects;

import org.daiitech.naftah.builtin.utils.ObjectUtils;

import static org.daiitech.naftah.utils.time.Constants.DAY;
import static org.daiitech.naftah.utils.time.Constants.MONTH;
import static org.daiitech.naftah.utils.time.Constants.PERIOD_PREFIX;
import static org.daiitech.naftah.utils.time.Constants.YEAR;
import static org.daiitech.naftah.utils.time.TemporalUtils.compare;

/**
 * Represents a period expressed in Arabic text, including years, months, and days.
 * <p>
 * This class wraps a {@link PeriodDefinition} for Arabic formatting and a {@link Period}
 * for the actual temporal amount.
 * </p>
 *
 * @param periodDefinition the definition of the period in Arabic text
 * @param temporalAmount   the actual period value
 * @author Chakib Daii
 */
public record ArabicPeriod(
		PeriodDefinition periodDefinition,
		Period temporalAmount
) implements ArabicTemporalAmount {

	/**
	 * Creates a new {@link ArabicPeriod} instance using the provided
	 * Arabic period definition and the underlying {@link Period}.
	 *
	 * @param periodDefinition the Arabic textual definition describing the period
	 *                         (years, months, and days)
	 * @param temporalAmount   the underlying {@link Period} representing the actual
	 *                         date-based amount
	 * @return a new {@link ArabicPeriod} instance
	 */
	public static ArabicPeriod of(
									PeriodDefinition periodDefinition,
									Period temporalAmount) {
		return new ArabicPeriod(periodDefinition, temporalAmount);
	}

	/**
	 * Creates a new {@link ArabicPeriod} instance from a {@link Period}.
	 * <p>
	 * The period is decomposed into its date-based components
	 * (years, months, and days) to build the corresponding
	 * Arabic textual representation.
	 * </p>
	 *
	 * @param period the {@link Period} to convert into an {@link ArabicPeriod}
	 * @return a new {@link ArabicPeriod} instance representing the given period
	 */
	public static ArabicPeriod of(Period period) {
		return of(PeriodDefinition
				.of(
					period.getYears(),
					period.getMonths(),
					period.getDays()
				), period);
	}

	/**
	 * Returns an {@link ArabicPeriod} representing a zero period.
	 *
	 * <p>The returned period has all components set to zero: years, months, and days.</p>
	 *
	 * @return an {@link ArabicPeriod} representing a period of zero
	 */
	public static ArabicPeriod ofZero() {
		return of(PeriodDefinition
				.of(
					0,
					0,
					0
				), Period.ZERO);
	}

	/**
	 * Obtains a {@code ArabicPeriod} representing a number of years.
	 * <p>
	 * The resulting period will have the specified years.
	 * The months and days units will be zero.
	 *
	 * @param years the number of years, positive or negative
	 * @return the period of years, not null
	 */
	public static ArabicPeriod ofYears(int years) {
		return of(PeriodDefinition
				.of(
					years,
					0,
					0
				), Period.ofYears(years));
	}

	/**
	 * Obtains a {@code ArabicPeriod} representing a number of months.
	 * <p>
	 * The resulting period will have the specified months.
	 * The years and days units will be zero.
	 *
	 * @param months the number of months, positive or negative
	 * @return the period of months, not null
	 */
	public static ArabicPeriod ofMonths(int months) {
		return of(PeriodDefinition
				.of(
					0,
					months,
					0
				), Period.ofMonths(months));
	}

	/**
	 * Obtains a {@code ArabicPeriod} representing a number of weeks.
	 * <p>
	 * The resulting period will be day-based, with the amount of days
	 * equal to the number of weeks multiplied by 7.
	 * The years and months units will be zero.
	 *
	 * @param weeks the number of weeks, positive or negative
	 * @return the period, with the input weeks converted to days, not null
	 */
	public static ArabicPeriod ofWeeks(int weeks) {
		return of(PeriodDefinition
				.of(
					0,
					0,
					Math.multiplyExact(weeks, 7)
				), Period.ofWeeks(weeks));
	}

	/**
	 * Obtains a {@code ArabicPeriod} representing a number of days.
	 * <p>
	 * The resulting period will have the specified days.
	 * The years and months units will be zero.
	 *
	 * @param days the number of days, positive or negative
	 * @return the period of days, not null
	 */
	public static ArabicPeriod ofDays(int days) {
		return of(PeriodDefinition
				.of(
					0,
					0,
					days
				), Period.ofDays(days));
	}


	/**
	 * Returns the number of years in this period.
	 *
	 * @return the years component of the period
	 */
	public int getYears() {
		return temporalAmount.getYears();
	}

	/**
	 * Returns the number of months in this period.
	 *
	 * @return the months component of the period
	 */
	public int getMonths() {
		return temporalAmount.getMonths();
	}

	/**
	 * Returns the number of days in this period.
	 *
	 * @return the days component of the period
	 */
	public int getDays() {
		return temporalAmount.getDays();
	}

	/**
	 * Returns a new {@code ArabicPeriod} obtained by adding the given
	 * Arabic temporal amount to this period.
	 *
	 * <p>
	 * Addition is supported only when the other amount is also an
	 * {@code ArabicPeriod}.
	 * </p>
	 *
	 * @param other the temporal amount to add
	 * @return a new {@code ArabicPeriod} instance
	 */
	@Override
	public ArabicPeriod plus(ArabicTemporalAmount other) {
		if (other instanceof ArabicPeriod otherPeriod) {
			Period result = temporalAmount.plus(otherPeriod.temporalAmount);
			return ArabicPeriod.of(result);
		}
		return (ArabicPeriod) ArabicTemporalAmount.super.plus(other);
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
	 * @return a {@code ArabicPeriod} based on this period with the specified years added, not null
	 * @throws ArithmeticException if numeric overflow occurs
	 */
	public ArabicPeriod plusYears(long yearsToAdd) {
		if (yearsToAdd == 0) {
			return this;
		}
		return of(temporalAmount.plusYears(yearsToAdd));
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
	 * @return a {@code ArabicPeriod} based on this period with the specified months added, not null
	 * @throws ArithmeticException if numeric overflow occurs
	 */
	public ArabicPeriod plusMonths(long monthsToAdd) {
		if (monthsToAdd == 0) {
			return this;
		}
		return of(temporalAmount.plusMonths(monthsToAdd));
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
	 * @return a {@code ArabicPeriod} based on this period with the specified days added, not null
	 * @throws ArithmeticException if numeric overflow occurs
	 */
	public ArabicPeriod plusDays(long daysToAdd) {
		if (daysToAdd == 0) {
			return this;
		}
		return of(temporalAmount.plusDays(daysToAdd));
	}

	/**
	 * Returns a new {@code ArabicPeriod} obtained by subtracting the given
	 * Arabic temporal amount from this period.
	 *
	 * <p>
	 * Subtraction is supported only when the other amount is also an
	 * {@code ArabicPeriod}.
	 * </p>
	 *
	 * @param other the temporal amount to subtract
	 * @return a new {@code ArabicPeriod} instance
	 */
	@Override
	public ArabicPeriod minus(ArabicTemporalAmount other) {
		if (other instanceof ArabicPeriod otherPeriod) {
			Period result = temporalAmount.minus(otherPeriod.temporalAmount);
			return ArabicPeriod.of(result);
		}
		return (ArabicPeriod) ArabicTemporalAmount.super.minus(other);
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
	 * @return a {@code ArabicPeriod} based on this period with the specified years subtracted, not null
	 * @throws ArithmeticException if numeric overflow occurs
	 */
	public ArabicPeriod minusYears(long yearsToSubtract) {
		if (yearsToSubtract == 0) {
			return this;
		}
		return of(temporalAmount.minusYears(yearsToSubtract));
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
	 * @return a {@code ArabicPeriod} based on this period with the specified months subtracted, not null
	 * @throws ArithmeticException if numeric overflow occurs
	 */
	public ArabicPeriod minusMonths(long monthsToSubtract) {
		if (monthsToSubtract == 0) {
			return this;
		}
		return of(temporalAmount.minusMonths(monthsToSubtract));
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
	 * @return a {@code ArabicPeriod} based on this period with the specified days subtracted, not null
	 * @throws ArithmeticException if numeric overflow occurs
	 */
	public ArabicPeriod minusDays(long daysToSubtract) {
		if (daysToSubtract == 0) {
			return this;
		}
		return of(temporalAmount.minusDays(daysToSubtract));
	}

	/**
	 * Determines whether this period is equal to the given temporal amount.
	 *
	 * @param other the temporal amount to compare with
	 * @return {@code true} if the periods are equal; {@code false} otherwise
	 */
	@Override
	public boolean isEquals(ArabicTemporalAmount other) {
		if (other instanceof ArabicPeriod otherPeriod) {
			return temporalAmount.equals(otherPeriod.temporalAmount);
		}
		return false;
	}

	/**
	 * Determines whether this period is not equal to the given temporal amount.
	 *
	 * @param other the temporal amount to compare with
	 * @return {@code true} if the periods are not equal; {@code false} otherwise
	 */
	@Override
	public boolean notEquals(ArabicTemporalAmount other) {
		return !isEquals(other);
	}

	/**
	 * Determines whether this period is greater than the given temporal amount.
	 *
	 * <p>
	 * Comparison is performed lexicographically:
	 * years → months → days.
	 * </p>
	 *
	 * @param other the temporal amount to compare with
	 * @return {@code true} if this period is greater
	 */
	@Override
	public boolean greaterThan(ArabicTemporalAmount other) {
		if (other instanceof ArabicPeriod otherPeriod) {
			return compare(temporalAmount, otherPeriod.temporalAmount) > 0;
		}
		return ArabicTemporalAmount.super.greaterThan(other);
	}

	/**
	 * Determines whether this period is greater than or equal to the
	 * given temporal amount.
	 *
	 * @param other the temporal amount to compare with
	 * @return {@code true} if this period is greater than or equal
	 */
	@Override
	public boolean greaterThanEquals(ArabicTemporalAmount other) {
		if (other instanceof ArabicPeriod otherPeriod) {
			return compare(temporalAmount, otherPeriod.temporalAmount) >= 0;
		}
		return ArabicTemporalAmount.super.greaterThanEquals(other);
	}

	/**
	 * Determines whether this period is less than the given temporal amount.
	 *
	 * @param other the temporal amount to compare with
	 * @return {@code true} if this period is less
	 */
	@Override
	public boolean lessThan(ArabicTemporalAmount other) {
		if (other instanceof ArabicPeriod otherPeriod) {
			return compare(temporalAmount, otherPeriod.temporalAmount) < 0;
		}
		return ArabicTemporalAmount.super.lessThan(other);
	}

	/**
	 * Determines whether this period is less than or equal to the
	 * given temporal amount.
	 *
	 * @param other the temporal amount to compare with
	 * @return {@code true} if this period is less than or equal
	 */
	@Override
	public boolean lessThanEquals(ArabicTemporalAmount other) {
		if (other instanceof ArabicPeriod otherPeriod) {
			return compare(temporalAmount, otherPeriod.temporalAmount) <= 0;
		}
		return ArabicTemporalAmount.super.lessThanEquals(other);
	}

	/**
	 * Returns the Arabic textual representation of the period.
	 *
	 * @return a string describing the period in Arabic
	 */
	@Override
	public String toString() {
		return periodDefinition.toString();
	}

	/**
	 * Definition of a period in Arabic text, including years, months, and days.
	 *
	 * @param years     the number of years
	 * @param yearText  the Arabic word for years (default is
	 *                  * {@link org.daiitech.naftah.utils.time.Constants#YEAR})
	 * @param months    the number of months
	 * @param monthText the Arabic word for months (default is
	 *                  * {@link org.daiitech.naftah.utils.time.Constants#MONTH})
	 * @param days      the number of days
	 * @param dayText   the Arabic word for days (default is {@link org.daiitech.naftah.utils.time.Constants#DAY})
	 */
	public record PeriodDefinition(
			int years,
			String yearText,
			int months,
			String monthText,
			int days,
			String dayText
	) {
		/**
		 * Creates a new {@link PeriodDefinition} instance.
		 * <p>
		 * Any missing Arabic text labels are automatically filled using the
		 * default Arabic date unit terms defined in
		 * {@link org.daiitech.naftah.utils.time.Constants}.
		 * </p>
		 *
		 * @param years     the number of years
		 * @param yearText  the Arabic text used for the year unit; if {@code null},
		 *                  {@link org.daiitech.naftah.utils.time.Constants#YEAR} is used
		 * @param months    the number of months
		 * @param monthText the Arabic text used for the month unit; if {@code null},
		 *                  {@link org.daiitech.naftah.utils.time.Constants#MONTH} is used
		 * @param days      the number of days
		 * @param dayText   the Arabic text used for the day unit; if {@code null},
		 *                  {@link org.daiitech.naftah.utils.time.Constants#DAY} is used
		 * @return a new {@link PeriodDefinition} instance
		 */
		public static PeriodDefinition of(
											int years,
											String yearText,
											int months,
											String monthText,
											int days,
											String dayText) {
			return new PeriodDefinition(years,
										Objects.requireNonNullElse(yearText, YEAR),
										months,
										Objects.requireNonNullElse(monthText, MONTH),
										days,
										Objects.requireNonNullElse(dayText, DAY));
		}

		/**
		 * Creates a new {@link PeriodDefinition} instance using the default
		 * Arabic date unit labels.
		 *
		 * @param years  the number of years
		 * @param months the number of months
		 * @param days   the number of days
		 * @return a new {@link PeriodDefinition} instance
		 */
		public static PeriodDefinition of(
											int years,
											int months,
											int days) {
			return new PeriodDefinition(years,
										YEAR,
										months,
										MONTH,
										days,
										DAY);
		}

		/**
		 * Returns a formatted Arabic string representing the period.
		 * <p>
		 * Only non-zero units are printed, with the Arabic conjunction " و " between them.
		 * If all units are zero, the output defaults to "0 يوم".
		 * </p>
		 *
		 * @return a string describing the period in Arabic
		 */
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(PERIOD_PREFIX);

			if (years != 0) {
				sb
						.append(" ")
						.append(ObjectUtils.numberToString(years))
						.append(" ")
						.append(yearText);
			}
			if (months != 0) {
				if (sb.length() > PERIOD_PREFIX.length()) {
					sb.append(" و ");
				}
				else {
					sb.append(" ");
				}
				sb
						.append(ObjectUtils.numberToString(months))
						.append(" ")
						.append(monthText);
			}
			if (days != 0) {
				if (sb.length() > PERIOD_PREFIX.length()) {
					sb.append(" و ");
				}
				else {
					sb.append(" ");
				}
				sb
						.append(ObjectUtils.numberToString(days))
						.append(" ")
						.append(dayText);
			}

			if (sb.length() == PERIOD_PREFIX.length()) {
				sb
						.append(" ")
						.append(ObjectUtils.numberToString(0))
						.append(" ")
						.append(DAY);
			}

			return sb.toString();
		}
	}
}
