package org.daiitech.naftah.builtin.time;

import java.time.Period;
import java.util.Objects;

import org.daiitech.naftah.builtin.utils.ObjectUtils;

import static org.daiitech.naftah.utils.time.Constants.DAY;
import static org.daiitech.naftah.utils.time.Constants.MONTH;
import static org.daiitech.naftah.utils.time.Constants.PERIOD_PREFIX;
import static org.daiitech.naftah.utils.time.Constants.YEAR;

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
