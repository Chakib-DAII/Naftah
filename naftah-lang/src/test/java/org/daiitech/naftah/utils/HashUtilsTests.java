// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

package org.daiitech.naftah.utils;

import org.daiitech.naftah.errors.NaftahBugError;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HashUtilsTests {

	@Test
	void testHashStringMd5() {
		String input = "hello";
		String hash = HashUtils.hashString(input, "MD5");
		assertEquals("5d41402abc4b2a76b9719d911017c592", hash);
	}

	@Test
	void testHashStringSha256() {
		String input = "hello";
		String hash = HashUtils.hashString(input, "SHA-256");
		assertEquals(
						"2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824",
						hash
		);
	}

	@Test
	void testHashStringUnsupportedAlgorithm() {
		NaftahBugError ex = assertThrows(NaftahBugError.class, () -> HashUtils.hashString("hello", "UNSUPPORTED_ALGO")
		);
		assertTrue(ex.getMessage().contains("غير مدعومة أو غير متوفرة"));
	}
}
