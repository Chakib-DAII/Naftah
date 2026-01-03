package org.daiitech.naftah.utils.time;

import java.time.DateTimeException;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.daiitech.naftah.errors.NaftahBugError;

import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.ARABIC;

/**
 * Utility class for handling time zones with support for Arabic names.
 * <p>
 * Provides methods to convert between Arabic time zone names and Java Zone IDs,
 * as well as parsing string offsets into {@link java.time.ZoneOffset}.
 *
 * @author Chakib Daii
 */
public final class ZoneUtils {
	/**
	 * Resource bundle containing Arabic names of time zones mapped to Java Zone IDs.
	 */
	private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("zones", ARABIC);
	/**
	 * Map of Arabic time zone names to Java Zone IDs.
	 * <p>
	 * This map is initialized from the {@link #BUNDLE} and is unmodifiable.
	 */
	private static final Map<String, String> ARABIC_ZONE_MAP;
	/**
	 * Reverse map from Java Zone IDs to canonical Arabic time zone names.
	 * <p>
	 * Only the first Arabic name encountered for a given Java Zone ID is retained.
	 * This map is unmodifiable.
	 */
	private static final Map<String, String> JAVA_ZONE_TO_ARABIC_MAP;

	static {
		Map<String, String> map = new HashMap<>();
		for (String key : BUNDLE.keySet()) {
			map.put(key, BUNDLE.getString(key).replace("_", " "));
		}
		ARABIC_ZONE_MAP = Collections.unmodifiableMap(map);

		Map<String, String> reverseMap = new HashMap<>();
		for (Map.Entry<String, String> entry : ARABIC_ZONE_MAP.entrySet()) {
			// If multiple Arabic names map to same java zone,
			// only the first one encountered will be kept here.
			// solution: alias based map
			reverseMap.putIfAbsent(entry.getValue(), entry.getKey());
		}
		JAVA_ZONE_TO_ARABIC_MAP = Collections.unmodifiableMap(reverseMap);
	}

	/**
	 * Private constructor to prevent instantiation.
	 * Always throws a {@link NaftahBugError} when called.
	 */
	private ZoneUtils() {
		throw newNaftahBugInvalidUsageError();
	}

	/**
	 * Parses a string offset (e.g., "+03:00") into a {@link java.time.ZoneOffset}.
	 *
	 * @param offsetStr the string representation of the offset
	 * @return the corresponding {@link java.time.ZoneOffset}
	 * @throws IllegalArgumentException if the offset string is invalid
	 */
	public static ZoneOffset parseZoneOffset(String offsetStr) {
		try {
			return ZoneOffset.of(offsetStr);
		}
		catch (DateTimeException ex) {
			throw new IllegalArgumentException(
												"الإزاحة غير صحيحة: " + offsetStr,
												ex
			);
		}
	}

	/**
	 * Converts an Arabic time zone name to its corresponding Java Zone ID.
	 *
	 * @param arabicZoneName the Arabic name of the time zone
	 * @return the Java Zone ID (e.g., "Asia/Riyadh")
	 * @throws IllegalArgumentException if the Arabic name is unknown
	 */
	public static String arabicZoneNameToJava(String arabicZoneName) {
		String zoneId = ARABIC_ZONE_MAP.get(arabicZoneName);
		if (zoneId == null) {
			String supported = String.join(", ", ARABIC_ZONE_MAP.keySet());
			throw new IllegalArgumentException(
												"اسم المنطقة الزمنية غير معروف: " + arabicZoneName + ". القيم المدعومة هي: " + supported
			);
		}
		return zoneId;
	}

	/**
	 * Converts a Java Zone ID to its canonical Arabic name.
	 *
	 * @param javaZoneId the Java Zone ID
	 * @return the corresponding Arabic name of the time zone
	 * @throws IllegalArgumentException if the Java Zone ID is unknown
	 */
	public static String javaZoneIdToArabic(String javaZoneId) {
		String arabicName = JAVA_ZONE_TO_ARABIC_MAP.get(javaZoneId);
		if (arabicName == null) {
			String supported = String.join(", ", JAVA_ZONE_TO_ARABIC_MAP.keySet());
			throw new IllegalArgumentException(
												"معرّف المنطقة الزمنية غير معروف في جافا: " + javaZoneId + ". القيم المدعومة هي: " + supported
			);
		}
		return arabicName;
	}
}
