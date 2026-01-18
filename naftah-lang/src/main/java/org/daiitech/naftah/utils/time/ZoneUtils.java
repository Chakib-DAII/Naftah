// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

package org.daiitech.naftah.utils.time;

import java.time.DateTimeException;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.daiitech.naftah.errors.NaftahBugError;

import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;
import static org.daiitech.naftah.utils.script.ScriptUtils.ARABIC_LOCALE;

/**
 * Utility class for handling time zones with support for names.
 * <p>
 * Provides methods to convert between time zone names and Java Zone IDs,
 * as well as parsing string offsets into {@link java.time.ZoneOffset}.
 *
 * @author Chakib Daii
 */
public final class ZoneUtils {
	/**
	 * Resource bundle containing names of time zones mapped to Java Zone IDs.
	 */
	private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("zones", ARABIC_LOCALE);
	/**
	 * Map of time zone names to Java Zone IDs.
	 * <p>
	 * This map is initialized from the {@link #BUNDLE} and is unmodifiable.
	 */
	private static final Map<String, String> ZONE_MAP;
	/**
	 * Reverse map from Java Zone IDs to canonical time zone names.
	 * <p>
	 * Only the first name encountered for a given Java Zone ID is retained.
	 * This map is unmodifiable.
	 */
	private static final Map<String, String> JAVA_ZONE_TO_MAP;

	static {
		Map<String, String> map = new HashMap<>();
		for (String key : BUNDLE.keySet()) {
			map.put(key, BUNDLE.getString(key).replace("_", " "));
		}
		ZONE_MAP = Collections.unmodifiableMap(map);

		Map<String, String> reverseMap = new HashMap<>();
		for (Map.Entry<String, String> entry : ZONE_MAP.entrySet()) {
			// If multiple names map to same java zone,
			// only the first one encountered will be kept here.
			// solution: alias based map
			reverseMap.putIfAbsent(entry.getValue(), entry.getKey());
		}
		JAVA_ZONE_TO_MAP = Collections.unmodifiableMap(reverseMap);
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
	 * Converts a time zone name to its corresponding Java Zone ID.
	 *
	 * @param zoneName the name of the time zone
	 * @return the Java Zone ID (e.g., "Asia/Riyadh")
	 * @throws IllegalArgumentException if the name is unknown
	 */
	public static String zoneNameToJavaZoneId(String zoneName) {
		String zoneId = ZONE_MAP.get(zoneName);
		if (zoneId == null) {
			String supported = String.join(", ", ZONE_MAP.keySet());
			throw new IllegalArgumentException(
												"اسم المنطقة الزمنية غير معروف: " + zoneName + ". القيم المدعومة هي: " + supported
			);
		}
		return zoneId;
	}

	/**
	 * Converts a Java Zone ID to its canonical name.
	 *
	 * @param javaZoneId the Java Zone ID
	 * @return the corresponding name of the time zone
	 * @throws IllegalArgumentException if the Java Zone ID is unknown
	 */
	public static String javaZoneIdToName(String javaZoneId) {
		String name = JAVA_ZONE_TO_MAP.get(javaZoneId);
		if (name == null) {
			String supported = String.join(", ", JAVA_ZONE_TO_MAP.keySet());
			throw new IllegalArgumentException(
												"معرّف المنطقة الزمنية غير معروف في جافا: " + javaZoneId + ". القيم المدعومة هي: " + supported
			);
		}
		return name;
	}
}
