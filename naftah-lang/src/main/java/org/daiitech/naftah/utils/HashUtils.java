// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

package org.daiitech.naftah.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.daiitech.naftah.errors.NaftahBugError;

import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;

/**
 * Utility class for hashing strings using various cryptographic algorithms.
 * <p>
 * This class provides static methods to generate hash digests of input strings
 * using standard algorithms like MD5, SHA-1, SHA-256, etc.
 * <p>
 * Instantiation of this class is prevented by a private constructor that throws
 * an exception if called.
 *
 * <p><strong>Usage example:</strong>
 * <pre>{@code
 * String hash = HashUtils.hashString("example", "SHA-256");
 * }</pre>
 *
 * <p><strong>Note:</strong> If the provided algorithm is not supported by
 * the current runtime environment, a {@link NaftahBugError} is thrown.
 *
 * <p>
 * --------------------------------------------------
 * <p>
 * فئة أدوات لتجزئة النصوص باستخدام خوارزميات التشفير المختلفة.
 * <p>
 * توفر هذه الفئة طرقًا ثابتة لتوليد ملخصات التجزئة لسلاسل الإدخال
 * باستخدام خوارزميات قياسية مثل MD5، SHA-1، SHA-256، وغيرها.
 * <p>
 * يتم منع إنشاء مثيل لهذه الفئة عبر مُنشئ خاص يرمي استثناءً إذا تم استدعاؤه.
 *
 * <p><strong>مثال على الاستخدام:</strong>
 * <pre>{@code
 * String hash = HashUtils.hashString("example", "SHA-256");
 * }</pre>
 *
 * <p><strong>ملاحظة:</strong> إذا لم تكن الخوارزمية المقدمة مدعومة
 * في بيئة التشغيل الحالية، سيتم رمي {@link NaftahBugError}.
 *
 * @author Chakib Daii
 */
public final class HashUtils {
	/**
	 * Private constructor to prevent instantiation.
	 * Throws {@link NaftahBugError} if called.
	 */
	private HashUtils() {
		throw newNaftahBugInvalidUsageError();
	}

	/**
	 * Generates a hash string for the given input using the specified algorithm.
	 *
	 * @param input     the input string to hash
	 * @param algorithm the name of the hashing algorithm (e.g., "MD5", "SHA-256")
	 * @return the resulting hash as a hexadecimal string
	 * @throws NaftahBugError if the specified algorithm is not supported or unavailable
	 */
	public static String hashString(String input, String algorithm) {
		try {
			MessageDigest digest = MessageDigest.getInstance(algorithm);
			byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hashBytes);

		}
		catch (NoSuchAlgorithmException e) {
			throw new NaftahBugError("""
										الخوارزمية المقدمة %s غير مدعومة أو غير متوفرة في البيئة الحالية.
										""".formatted(algorithm), e);
		}
	}

}
