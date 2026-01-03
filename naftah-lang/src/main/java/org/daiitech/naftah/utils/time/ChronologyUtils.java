package org.daiitech.naftah.utils.time;

import java.time.chrono.Chronology;
import java.time.chrono.HijrahChronology;
import java.time.chrono.IsoChronology;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.daiitech.naftah.errors.NaftahBugError;

import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;
import static org.daiitech.naftah.utils.time.Constants.DEFAULT_CALENDAR_NAME;
import static org.daiitech.naftah.utils.time.Constants.DEFAULT_CALENDAR_NAME_1;
import static org.daiitech.naftah.utils.time.Constants.DEFAULT_CALENDAR_NAME_2;
import static org.daiitech.naftah.utils.time.Constants.DEFAULT_CALENDAR_NAME_3;
import static org.daiitech.naftah.utils.time.Constants.HIJRI_CALENDAR_NAME;
import static org.daiitech.naftah.utils.time.Constants.HIJRI_CALENDAR_NAME_1;
import static org.daiitech.naftah.utils.time.Constants.HIJRI_CALENDAR_NAME_2;

/**
 * Utility class for working with Java {@link Chronology} instances
 * and their corresponding Arabic calendar names.
 *
 * <p>Provides methods to:
 * <ul>
 * <li>Map Arabic calendar names to Java {@link Chronology} instances</li>
 * <li>Retrieve the canonical Arabic calendar name for a given {@link Chronology}</li>
 * </ul>
 *
 * <p>Supports both ISO (Gregorian) and Hijrah (Islamic) chronologies by default.</p>
 *
 * @author Chakib Daii
 */
public final class ChronologyUtils {
	/**
	 * The default chronology used when no specific calendar is provided.
	 * Typically represents the ISO (Gregorian) calendar.
	 */
	public static final Chronology DEFAULT_CHRONOLOGY = IsoChronology.INSTANCE;
	/**
	 * The Hijrah chronology representing the Islamic (Hijri) calendar.
	 */
	public static final Chronology HIJRAH_CHRONOLOGY = HijrahChronology.INSTANCE;
	/**
	 * A map from Arabic calendar names to their corresponding {@link Chronology} instances.
	 *
	 * <p>Supports multiple aliases for ISO and Hijrah calendars.</p>
	 */
	private static final Map<String, Chronology> CALENDAR_MAP = Map
			.ofEntries(
						Map.entry(DEFAULT_CALENDAR_NAME, DEFAULT_CHRONOLOGY),
						Map.entry(DEFAULT_CALENDAR_NAME_1, DEFAULT_CHRONOLOGY),
						Map.entry(DEFAULT_CALENDAR_NAME_2, DEFAULT_CHRONOLOGY),
						Map.entry(DEFAULT_CALENDAR_NAME_3, DEFAULT_CHRONOLOGY),
						Map.entry(HIJRI_CALENDAR_NAME, HIJRAH_CHRONOLOGY),
						Map.entry(HIJRI_CALENDAR_NAME_1, HIJRAH_CHRONOLOGY),
						Map.entry(HIJRI_CALENDAR_NAME_2, HIJRAH_CHRONOLOGY)
			);
	/**
	 * A reverse map from {@link Chronology} instances to a canonical Arabic calendar name.
	 *
	 * <p>If multiple Arabic names map to the same chronology, only the first encountered
	 * name is stored as the canonical name.</p>
	 */
	private static final Map<Chronology, String> CHRONOLOGY_TO_ARABIC;

	static {
		Map<Chronology, String> reverseMap = new HashMap<>();
		for (Map.Entry<String, Chronology> entry : CALENDAR_MAP.entrySet()) {
			// If multiple Arabic names map to same java zone,
			// only the first one encountered will be kept here.
			// solution: alias based map
			reverseMap.putIfAbsent(entry.getValue(), entry.getKey());
		}
		CHRONOLOGY_TO_ARABIC = Collections.unmodifiableMap(reverseMap);
	}

	/**
	 * Private constructor to prevent instantiation.
	 * Always throws a {@link NaftahBugError} when called.
	 */
	private ChronologyUtils() {
		throw newNaftahBugInvalidUsageError();
	}

	/**
	 * Returns the {@link Chronology} instance corresponding to the given Arabic calendar name.
	 *
	 * @param chronologyName the Arabic name of the calendar
	 * @return the corresponding {@link Chronology} instance
	 * @throws IllegalArgumentException if the calendar name is unknown
	 */
	public static Chronology getChronologyByName(String chronologyName) {
		Chronology chronology = CALENDAR_MAP.get(chronologyName);
		if (chronology == null) {
			String supported = String.join(", ", CALENDAR_MAP.keySet());
			throw new IllegalArgumentException(
												"اسم التقويم غير معروف: " + chronologyName + ". القيم المدعومة هي: " + supported
			);
		}
		return chronology;
	}

	/**
	 * Returns the canonical Arabic calendar name corresponding to the given {@link Chronology} instance.
	 *
	 * @param chronology the chronology instance
	 * @return the canonical Arabic calendar name
	 * @throws IllegalArgumentException if the chronology is not supported
	 */
	public static String getChronologyName(Chronology chronology) {
		String name = CHRONOLOGY_TO_ARABIC.get(chronology);
		if (name == null) {
			String supported = String.join(", ", CHRONOLOGY_TO_ARABIC.values());
			throw new IllegalArgumentException(
												"التقويم غير مدعوم: " + chronology + ". القيم المدعومة هي: " + supported
			);
		}
		return name;
	}
}
