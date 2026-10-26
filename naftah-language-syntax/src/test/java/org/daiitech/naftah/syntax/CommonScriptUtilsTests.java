// SPDX-License-Identifier: Apache-2.0
package org.daiitech.naftah.syntax;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommonScriptUtilsTests {
	@Test
	void mapsJavaNamesUsingThePublishedNaftahVocabulary() {
		assertEquals(   "جافا:لغة:سلسلة",
						String.join(":", CommonScriptUtils.transliterateToArabicScriptDefault("java", "lang", "String")));
		assertEquals("تقليم", CommonScriptUtils.transliterateToArabicScriptDefault("trim")[0]);
		assertEquals("گت_بايتس", CommonScriptUtils.transliterateToArabicScriptDefault("getBytes")[0]);
	}
}
