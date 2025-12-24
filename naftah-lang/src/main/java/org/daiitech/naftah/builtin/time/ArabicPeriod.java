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
	 * Creates a new {@code ArabicPeriod} instance.
	 *
	 * @param periodDefinition the definition of the period in Arabic text
	 * @param temporalAmount   the actual period value
	 * @return a new {@code ArabicPeriod} instance
	 */
	public static ArabicPeriod of(
									PeriodDefinition periodDefinition,
									Period temporalAmount) {
		return new ArabicPeriod(periodDefinition, temporalAmount);
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
		 * Creates a new {@code PeriodDefinition} instance, filling missing text labels with default Arabic terms.
		 *
		 * @param years     the number of years
		 * @param yearText  the Arabic word for years (default is
		 *                  * {@link org.daiitech.naftah.utils.time.Constants#YEAR})
		 * @param months    the number of months
		 * @param monthText the Arabic word for months (default is
		 *                  * {@link org.daiitech.naftah.utils.time.Constants#MONTH})
		 * @param days      the number of days
		 * @param dayText   the Arabic word for days (default is {@link org.daiitech.naftah.utils.time.Constants#DAY})
		 * @return a new {@code PeriodDefinition} instance
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
