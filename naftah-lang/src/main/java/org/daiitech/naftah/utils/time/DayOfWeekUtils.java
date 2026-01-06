package org.daiitech.naftah.utils.time;

import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;

/**
 * Utility class for converting numeric day-of-week values to names.
 *
 * <p>This class supports both Gregorian and Hijri calendars (names are the same).</p>
 *
 * <p>Days are numbered 1–7 (Monday = 1, Sunday = 7).</p>
 *
 * @author Chakib Daii
 */
public final class DayOfWeekUtils {

	/**
	 * names of the week (Monday = 1, Sunday = 7).
	 */
	private static final String[] WEEK_DAYS = {
												"الاثنين",
												"الثلاثاء",
												"الأربعاء",
												"الخميس",
												"الجمعة",
												"السبت",
												"الأحد"
	};

	/**
	 * Private constructor to prevent instantiation.
	 */
	private DayOfWeekUtils() {
		throw newNaftahBugInvalidUsageError();
	}

	/**
	 * Converts a numeric day-of-week to its name.
	 *
	 * @param dayOfWeek the day number (1 = Monday, ..., 7 = Sunday)
	 * @return name of the day
	 * @throws IllegalArgumentException if the day number is not in 1–7
	 */
	public static String getDayOfWeek(int dayOfWeek) {
		if (dayOfWeek < 1 || dayOfWeek > 7) {
			throw new IllegalArgumentException("رقم اليوم يجب أن يكون بين 1 و 7، القيمة المدخلة: " + dayOfWeek);
		}
		return WEEK_DAYS[dayOfWeek - 1];
	}
}
