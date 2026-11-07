// SPDX-License-Identifier: Apache-2.0
package org.daiitech.naftah.syntax;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import com.ibm.icu.text.Transliterator;

/** Java name transliteration used by Naftah JVM interop. */
public final class CommonScriptUtils {
	private static final Locale ARABIC = Locale.forLanguageTag("ar");
	private static final Pattern IDENTIFIER_BOUNDARY = Pattern
			.compile(
						"(?<=[A-Z])(?=[A-Z][a-z])|(?<=[^A-Z])(?=[A-Z])|(?<=[A-Za-z])(?=\\d)|(?<=\\d)(?=[A-Za-z])");
	private static final Pattern DIACRITICS = Pattern.compile("[\\u0610-\\u061A\\u064B-\\u065F\\u0670\\u06D6-\\u06ED]");
	private static final Map<String, String> RULES = loadRules();
	private static final Transliterator CUSTOM = Transliterator
			.createFromRules(
								"NaftahLanguageSyntax",
								rules(),
								Transliterator.FORWARD);
	private static final Transliterator LATIN_ARABIC = Transliterator.getInstance("Latin-Arabic");

	private CommonScriptUtils() {
	}

	public static String normalize(String text) {
		return com.ibm.icu.text.Normalizer2.getNFKCInstance().normalize(text);
	}

	public static String[] transliterateToArabicScriptDefault(String... text) {
		return transliterateToArabicScriptDefault(true, text);
	}

	public static String[] transliterateToArabicScriptDefault(boolean stripDiacritics, String... text) {
		return java.util.Arrays
				.stream(text)
				.map(value -> transliterateIdentifier(value, stripDiacritics))
				.toArray(String[]::new);
	}

	public static String transliterateIdentifier(String name) {
		return transliterateIdentifier(name, true);
	}

	private static String transliterateIdentifier(String name, boolean stripDiacritics) {
		String normalized = name.toLowerCase(ARABIC);
		String transliterated = RULES.containsKey(normalized) ?
				transliterate(normalized) :
				splitIdentifier(name)
						.stream()
						.map(part -> transliterate(part.toLowerCase(ARABIC)))
						.reduce((left, right) -> left + "_" + right)
						.orElse("");
		return (stripDiacritics ? CommonScriptUtils.removeDiacritics(transliterated) : transliterated)
				.replaceAll("\\s+", "_");
	}

	public static List<String> splitIdentifier(String value) {
		return List
				.of(value.replaceAll("[-_]", " ").split("\\s+"))
				.stream()
				.flatMap(part -> IDENTIFIER_BOUNDARY.splitAsStream(part))
				.filter(part -> !part.isBlank())
				.toList();
	}

	private static String transliterate(String value) {
		return LATIN_ARABIC.transliterate(CUSTOM.transliterate(value));
	}

	public static String removeDiacritics(String text) {
		return DIACRITICS.matcher(text).replaceAll("");
	}

	private static Map<String, String> loadRules() {
		Map<String, String> rules = new LinkedHashMap<>();
		rules.put("java", "جافا");
		rules.put("javax", "جافاح");
		rules.put("lang", "لغة");
		rules.put("string", "سلسلة");
		rules.put("object", "كائن");
		rules.put("class", "صنف");
		rules.put("util", "أداة");
		rules.put("time", "وقت");
		rules.put("trim", "تقليم");
		rules.put("get", "گت");
		rules.put("bytes", "بايتس");
		rules.put("daiitech", "داعيتاك");
		rules.put("naftah", "نفطه");
		rules.put("com", "كوم");
		rules.put("org", "أورغ");
		rules.put("co", "كو");
		rules.put("o", "و");
		rules.put("aa", "ع");
		rules.put("a", "ا");
		rules.put("b", "ب");
		rules.put("tech", "تاك");
		rules.put("t", "ت");
		rules.put("ii", "عي");
		ResourceBundle bundle = ResourceBundle.getBundle("transliteration", ARABIC);
		bundle.keySet().forEach(key -> rules.put(key.toLowerCase(ARABIC), bundle.getString(key)));
		return rules;
	}

	private static String rules() {
		return RULES
				.entrySet()
				.stream()
				.sorted((left, right) -> Integer.compare(right.getKey().length(), left.getKey().length()))
				.map(entry -> quote(entry.getKey()) + " > " + quote(entry.getValue()) + ";")
				.reduce((left, right) -> left + "\n" + right)
				.orElse("");
	}

	private static String quote(String value) {
		return value.matches(".*[_\\-\\s].*") || ICU_RESERVED.contains(value) || value.startsWith("::") ?
				"'" + value + "'" :
				value;
	}

	private static final List<String> ICU_RESERVED = List
			.of(
				"::",
				"use",
				"import",
				"function",
				"null",
				"nfd",
				"nfc",
				"nfkd",
				"nfkc",
				"any",
				"latin",
				"greek",
				"cyrillic",
				"han",
				"kana",
				"hiragana",
				"katakana",
				"common",
				"inherited");
}
