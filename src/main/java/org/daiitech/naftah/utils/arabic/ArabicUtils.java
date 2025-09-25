package org.daiitech.naftah.utils.arabic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.utils.OS;
import org.daiitech.naftah.utils.function.ThrowingBiFunction;
import org.daiitech.naftah.utils.function.ThrowingFunction;

import com.ibm.icu.impl.Pair;
import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import com.ibm.icu.text.DecimalFormat;
import com.ibm.icu.text.DecimalFormatSymbols;
import com.ibm.icu.text.NumberFormat;
import com.ibm.icu.text.Transliterator;

import static org.daiitech.naftah.Naftah.ARABIC_INDIC_PROPERTY;
import static org.daiitech.naftah.Naftah.INTERPOLATION_CACHE_PROPERTY;
import static org.daiitech.naftah.NaftahSystem.TERMINAL_WIDTH_PROPERTY;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;

/**
 * Utility class providing various methods for handling Arabic text processing,
 * including text shaping, bidi reordering, transliteration, diacritics removal,
 * padding for terminal display, and detection of Arabic characters.
 * <p>
 * This class is designed as a final utility class and cannot be instantiated.
 * </p>
 * <p>
 * It uses ICU4J {@link com.ibm.icu.text.ArabicShaping} and
 * {@link com.ibm.icu.text.Transliterator} for shaping and transliteration.
 * </p>
 *
 * @author Chakib Daii
 */
public final class ArabicUtils {

	/**
	 * Escape code to set Right-To-Left (RTL) text direction in compatible terminals.
	 */
	public static final String RTL_DIRECTION = "\u001B[?2004h"; // Set RTL

	/**
	 * Escape code to set Left-To-Right (LTR) text direction in compatible terminals.
	 */
	public static final String LTR_DIRECTION = "\u001B[?2004l"; // Set LTR

	/**
	 * Regular expression matching Arabic diacritic marks in Unicode.
	 */
	public static final String ARABIC_DIACRITICS_REGEX = "[\u0610-\u061A\u064B-\u0652\u0670]";
	/**
	 * ANSI escape sequence to clear the screen.
	 */
	public static final String ANSI_ESCAPE = "\033[H\033[2J";
	/**
	 * ICU Transliterator ID for Latin-to-Arabic and Arabic-to-Latin transliteration.
	 */
	public static final String LATIN_ARABIC_TRANSLITERATION_ID = "Any-Latin; Latin-Arabic";
	/**
	 * Language code for Arabic.
	 */
	public static final String ARABIC_LANGUAGE = "ar";
	/**
	 * Default country code used in Arabic locale.
	 */
	public static final String DEFAULT_ARABIC_LANGUAGE_COUNTRY = "TN";

	/**
	 * Locale instance representing Arabic language.
	 */
	public static final Locale ARABIC = new Locale(ARABIC_LANGUAGE);
	/**
	 * ResourceBundle loaded with custom transliteration rules for Arabic.
	 */
	public static final ResourceBundle CUSTOM_RULES_BUNDLE = ResourceBundle.getBundle("transliteration", ARABIC);
	/**
	 * Pattern to detect lines in multiline text, capturing line content and newline characters.
	 */
	public static final Pattern TEXT_MULTILINE_PATTERN = Pattern.compile("(.+?)(\\r\\n|\\n|\\r|$)");
	/**
	 * Regular expression used to split identifiers into components based on transitions between uppercase letters,
	 * digits, and lowercase letters.
	 * <p>
	 * For example:
	 * <ul>
	 * <li>"JSONTo" → "JSON", "To"</li>
	 * <li>"userAccount" → "user", "Account"</li>
	 * <li>"IPv6" → "IPv", "6"</li>
	 * <li>"6Parser" → "6", "Parser"</li>
	 * </ul>
	 * </p>
	 */
	private static final String IDENTIFIER_SPLIT_REGEX =
			// JSONTo → JSON, To
			"(?<=[A-Z])(?=[A-Z][a-z])" + // userAccount → user, Account
					"|(?<=[^A-Z])(?=[A-Z])" + // IPv6 → IPv, 6
					"|(?<=[A-Za-z])(?=\\d)" + // 6Parser → 6, Parser
					"|(?<=\\d)(?=[A-Za-z])";
	/**
	 * Arabic alphabet letters used for transliteration to Latin letters.
	 * <p>
	 * The characters are mapped positionally (index by index) to uppercase Latin letters.
	 * This list includes 26 Arabic letters starting from 'ا' to 'ه', and is intended to be used
	 * for character-by-character mapping to Latin base encoding (e.g., base 11 to base 36 systems).
	 *
	 * <p>Examples of mapping:
	 * <ul>
	 * <li>'ا' → 'A'</li>
	 * <li>'ب' → 'B'</li>
	 * <li>'ت' → 'C'</li>
	 * ...
	 * <li>'ه' → 'Z'</li>
	 * </ul>
	 */
	private static final char[] ARABIC_LETTERS = {
													'ا',
													'ب',
													'ت',
													'ث',
													'ج',
													'ح',
													'خ',
													'د',
													'ذ',
													'ر',
													'ز',
													'س',
													'ش',
													'ص',
													'ض',
													'ط',
													'ظ',
													'ع',
													'غ',
													'ف',
													'ق',
													'ك',
													'ل',
													'م',
													'ن',
													'ه'
	};
	/**
	 * Latin uppercase letters used as transliteration equivalents for Arabic letters.
	 * <p>
	 * Each letter corresponds to an Arabic letter by position in the {@code ARABIC_LETTERS} array.
	 * This mapping supports systems like base-36 encodings or custom symbolic notations
	 * using Arabic letters.
	 *
	 * <p>Examples of mapping:
	 * <ul>
	 * <li>'A' → 'ا'</li>
	 * <li>'B' → 'ب'</li>
	 * <li>'C' → 'ت'</li>
	 * ...
	 * <li>'Z' → 'ه'</li>
	 * </ul>
	 */
	private static final char[] LATIN_LETTERS = {
													'A',
													'B',
													'C',
													'D',
													'E',
													'F',
													'G',
													'H',
													'I',
													'J',
													'K',
													'L',
													'M',
													'N',
													'O',
													'P',
													'Q',
													'R',
													'S',
													'T',
													'U',
													'V',
													'W',
													'X',
													'Y',
													'Z'
	};
	/**
	 * A set of reserved words used by the ICU (International Components for Unicode)
	 * transliteration and normalization APIs. These words have special meaning in
	 * ICU transliteration rules and Unicode transformations.
	 *
	 * <p>Examples of usage contexts include:
	 * <ul>
	 * <li>Transliteration rule syntax (e.g., "::NFD;" or "::Latin-ASCII;")</li>
	 * <li>Normalization forms (e.g., "NFC", "NFD", "NFKC", "NFKD")</li>
	 * <li>Unicode script and block identifiers (e.g., "Latin", "Greek", "Han")</li>
	 * <li>Keywords in rule definitions (e.g., "use", "import", "function")</li>
	 * </ul>
	 *
	 * <p>This set can be used to:
	 * <ul>
	 * <li>Validate user-defined transliteration rules</li>
	 * <li>Highlight or flag reserved words in editors or tools</li>
	 * <li>Prevent conflicts in custom ICU rule definitions</li>
	 * </ul>
	 *
	 * @see com.ibm.icu.text.Transliterator
	 * @see com.ibm.icu.text.Normalizer2
	 * @see <a href="https://unicode-org.github.io/icu/userguide/transforms/general/">ICU Transliteration Guide</a>
	 */
	private static final Set<String> ICU_RESERVED_WORDS = Set
			.of(
				"::",
				"use",
				"import",
				"function",
				"Null",
				"NFD",
				"NFC",
				"NFKD",
				"NFKC",
				"Any",
				"Latin",
				"Greek",
				"Cyrillic",
				"Han",
				"Kana",
				"Hiragana",
				"Katakana",
				"Common",
				"Inherited"
			);
	/**
	 * A reusable {@link NumberFormat} instance configured for the Arabic locale.
	 * <p>
	 * This formatter uses Arabic locale conventions for decimal and grouping separators,
	 * and may render numbers using Arabic-Indic digits (e.g., ٠١٢٣٤٥٦٧٨٩), depending on JVM
	 * settings and font support.
	 * <p>
	 * Note: {@link NumberFormat} instances are <strong>not thread-safe</strong>. If this
	 * formatter is used across multiple threads, synchronize access or create a new instance
	 * via {@code NumberFormat.getNumberInstance(ARABIC)}.
	 *
	 * @see Locale#forLanguageTag(String)
	 * @see NumberFormat#getNumberInstance(Locale)
	 */
	public static volatile NumberFormat ARABIC_NUMBER_FORMAT;
	/**
	 * Custom transliteration rules defined as a multi-line string.
	 * Each rule maps Latin script sequences to their corresponding Arabic script sequences.
	 * For example, "com > كوم" transliterates "com" to Arabic "كوم".
	 */
	public static String CUSTOM_RULES = """
										com > كوم;
										org > أورغ;
										co > كو;
										o > و;
										aa > ع;
										a > ا;
										b > ب;
										tech > تاك;
										t > ت;
										ii > عي;
										""";
	/**
	 * Cache of precompiled {@link Matcher} instances for text processing, keyed by the input text string.
	 * Used to improve performance by avoiding
	 * repeated compilation of patterns.
	 */
	private static Map<String, Matcher> TEXT_MATCHER_CACHE;

	static {
		if (Boolean.getBoolean(INTERPOLATION_CACHE_PROPERTY)) {
			TEXT_MATCHER_CACHE = new HashMap<>();
		}
		Map<String, String> existentCustomRules = parseRules(CUSTOM_RULES);

		Set<String> keys = CUSTOM_RULES_BUNDLE.keySet();
		keys.addAll(existentCustomRules.keySet());

		// Convert to List
		List<String> keylist = new ArrayList<>(keys);

		// Sort by length descending
		keylist.sort((s1, s2) -> Integer.compare(s2.length(), s1.length()));

		StringBuilder stringBuilder = new StringBuilder();
		for (String key : keylist) {
			String value = CUSTOM_RULES_BUNDLE.containsKey(key) ?
					CUSTOM_RULES_BUNDLE.getString(key) :
					existentCustomRules.get(key);
			// If s contains underscore or spaces or any special char, quote it
			String regex = ".*[ _\\t\\r\\n].*";
			if (key.matches(regex) || ICU_RESERVED_WORDS.contains(key.trim()) || key
					.trim()
					.startsWith("::")) { // underscore or space or whitespace
				key = "'" + key + "'";
			}
			if (value.matches(regex)) { // underscore or space or whitespace
				value = "'" + value + "'";
			}

			stringBuilder.append("""
									%s > %s;
									""".formatted(key.toLowerCase(Locale.US), value));
		}
		CUSTOM_RULES = stringBuilder.toString();

		DecimalFormatSymbols symbols = new DecimalFormatSymbols(ARABIC);

		if (Boolean.getBoolean(ARABIC_INDIC_PROPERTY)) {
//			TODO : not working; check it
			symbols
					.setDigitStrings(new String[]{
													"٠",
													"١",
													"٢",
													"٣",
													"٤",
													"٥",
													"٦",
													"٧",
													"٨",
													"٩"
					}); // Arabic-Indic zero
		}
		else {
			symbols.setZeroDigit('0'); // Latin digits
		}

		symbols.setDecimalSeparator('٫'); // Arabic decimal separator
		symbols.setGroupingSeparator('_'); // Arabic grouping separator  (in use if the pattern is "#,##0.###")
		ARABIC_NUMBER_FORMAT = new DecimalFormat("###0.###", symbols);
	}

	/**
	 * Private constructor to prevent instantiation.
	 * Always throws a {@link NaftahBugError} when called.
	 */
	private ArabicUtils() {
		throw newNaftahBugInvalidUsageError();
	}

	/**
	 * Parses a set of transformation rules from a string into a map.
	 *
	 * <p>The input string should contain one rule per line in the format:
	 * <pre>{@code
	 * source > target;
	 * }</pre>
	 *
	 * <p>Each line:
	 * <ul>
	 * <li>Is stripped of leading/trailing whitespace</li>
	 * <li>Ignores empty lines</li>
	 * <li>Removes trailing semicolons</li>
	 * <li>Splits on the first occurrence of the {@code '>'} character</li>
	 * </ul>
	 *
	 * <p>Example input:
	 * <pre>{@code
	 * a > b;
	 * c > d;
	 * }</pre>
	 *
	 * <p>Will result in a map:
	 * <pre>{@code
	 * {
	 *   "a" -> "b",
	 *   "c" -> "d"
	 * }
	 * }</pre>
	 *
	 * @param rules A string containing one or more transformation rules separated by newlines
	 * @return A map of source-to-target transformations
	 */
	public static Map<String, String> parseRules(String rules) {
		Map<String, String> map = new HashMap<>();
		String[] lines = rules.strip().split("\n");

		for (String line : lines) {
			line = line.strip();
			if (line.isEmpty()) {
				continue;
			}
			line = line.replace(";", "");
			String[] parts = line.split(">", 2);
			if (parts.length == 2) {
				String key = parts[0].strip();
				String value = parts[1].strip();
				map.put(key, value);
			}
		}

		return map;
	}

	/**
	 * Checks if the given input string contains multiple lines.
	 *
	 * @param input the input string to check
	 * @return true if the input contains one or more newline characters; false otherwise
	 */
	public static boolean isMultiline(String input) {
		return getTextMatcher(input).find();
	}

	/**
	 * Retrieves a cached {@link Matcher} for the given input string using the
	 * {@link #TEXT_MULTILINE_PATTERN} pattern. If a matcher for the input
	 * already exists in the cache, it is reset and returned; otherwise, a new
	 * matcher is created, cached, reset, and returned.
	 * <p>
	 * This caching mechanism improves performance by reusing matcher instances
	 * for repeated input strings.
	 *
	 * @param input the input string to create or retrieve a matcher for
	 * @return a reset {@link Matcher} instance ready for matching against the input
	 */
	private static Matcher getTextMatcher(String input) {
		if (Objects.nonNull(TEXT_MATCHER_CACHE) && TEXT_MATCHER_CACHE.containsKey(input)) {
			return TEXT_MATCHER_CACHE.get(input).reset();
		}

		Matcher matcher = TEXT_MULTILINE_PATTERN.matcher(input);

		if (Objects.nonNull(TEXT_MATCHER_CACHE)) {
			TEXT_MATCHER_CACHE.put(input, matcher);
		}

		return matcher.reset();
	}

	/**
	 * Applies a bi-function to each line in the input text.
	 * <p>
	 * If the input is multiline, applies the function to each line individually,
	 * preserving line separators. Otherwise, applies the function once to the whole input.
	 * </p>
	 *
	 * @param input    the input text (possibly multiline)
	 * @param print    if true, the result is printed to the console; if false, the result is returned
	 * @param function a bi-function taking a line and the print flag, returning the processed line
	 * @return the processed text if {@code print} is false; otherwise, null
	 */
	public static synchronized String applyBiFunction(  String input,
														boolean print,
														ThrowingBiFunction<String, Boolean, String> function) {
		if (isMultiline(input)) {
			Matcher matcher = getTextMatcher(input).reset();

			StringBuilder output = print ? null : new StringBuilder();

			while (matcher.find()) {
				String line = matcher.group(1);
				String newline = matcher.group(2); // might be "" on last line

				if (!line.trim().isEmpty()) {
					String result = function.apply(line, print);
					if (!print) {
						output.append(result);
					}
				}

				if (!print) {
					output.append(newline); // keep original newlines
				}
			}
			return print ? null : output.toString();

		}
		else {
			String result = function.apply(input, print);
			return print ? null : result;
		}
	}

	/**
	 * Applies a function to each line in the input text.
	 * <p>
	 * If the input is multiline, applies the function to each line individually,
	 * preserving line separators. Otherwise, applies the function once to the whole input.
	 * </p>
	 *
	 * @param input    the input text (possibly multiline)
	 * @param function a function taking a line and returning the processed line
	 * @return the processed text with all lines processed by the function
	 */
	public static synchronized String applyFunction(String input, ThrowingFunction<String, String> function) {
		if (isMultiline(input)) {
			Matcher matcher = getTextMatcher(input).reset();

			StringBuilder output = new StringBuilder();

			while (matcher.find()) {
				String line = matcher.group(1);
				String newline = matcher.group(2); // might be "" on last line

				if (!line.trim().isEmpty()) {
					output.append(function.apply(line));
				}

				output.append(newline); // keep original newlines
			}

			return output.toString();
		}
		else {
			return function.apply(input);
		}
	}


	/**
	 * Applies Arabic shaping and bidirectional reordering to the input text.
	 *
	 * @param input the input Arabic text
	 * @return the shaped and reordered text suitable for visual rendering in terminals
	 */
	public static synchronized String shape(String input) {
		return applyFunction(input, ArabicUtils::doShape);
	}

	/**
	 * Performs Arabic shaping and bidirectional reordering on a single input line.
	 *
	 * @param input the input Arabic text
	 * @return the shaped and reordered text
	 * @throws ArabicShapingException if an error occurs during shaping
	 */
	public static synchronized String doShape(String input) throws ArabicShapingException {
		ArabicShaping shaper = new ArabicShaping(ArabicShaping.LETTERS_SHAPE | ArabicShaping.TEXT_DIRECTION_VISUAL_RTL);
		String shaped = shaper.shape(input);
		Bidi bidi = new Bidi(shaped, Bidi.DIRECTION_RIGHT_TO_LEFT);
		return bidi.writeReordered(Bidi.DO_MIRRORING);
	}

	/**
	 * Pads the input text to align it within the terminal width.
	 * <p>
	 * If {@code print} is true, prints the padded text; otherwise, returns it.
	 * </p>
	 *
	 * @param input the input text to pad
	 * @param print if true, print the padded text; else return it
	 * @return the padded text if {@code print} is false; otherwise null
	 */
	public static synchronized String padText(String input, boolean print) {
		return applyBiFunction(input, print, ArabicUtils::doPadText);
	}

	/**
	 * Pads the input text to align within the terminal width, adjusting for overflow.
	 *
	 * @param input the input text to pad
	 * @param print if true, prints the padded lines; else returns them as a single string
	 * @return the padded text if {@code print} is false; otherwise null
	 */
	public static synchronized String doPadText(String input, boolean print) {
		int terminalWidth = Integer.getInteger(TERMINAL_WIDTH_PROPERTY);
		int padding = terminalWidth - input.length();
		if (padding < 0) {
			// correct text in case of terminal overflow
			String result = doPadText(input, terminalWidth, print);
			return print ? null : result;
		}
		// add padding to align text
		String result = addPadding(input, padding);
		if (print) {
			System.out.println(result);
			return null;
		}
		else {
			return result;
		}
	}

	/**
	 * Pads the input text, splitting it into multiple lines if needed to fit the terminal width.
	 *
	 * @param input         the input text to pad
	 * @param terminalWidth the width of the terminal
	 * @param print         if true, prints padded lines; else returns them joined as a string
	 * @return the padded text if {@code print} is false; otherwise null
	 */
	public static synchronized String doPadText(String input, int terminalWidth, boolean print) {
		String[] words = input.split("\\s+");
		StringBuilder currentLine = new StringBuilder();
		List<String> lines = print ? null : new ArrayList<>();

		for (String word : words) {
			if (currentLine.length() + word.length() + (!currentLine.isEmpty() ? 1 : 0) > terminalWidth) {
				// Line is full, store it and start a new one
				String result = addPadding(currentLine, terminalWidth);

				if (print) {
					System.out.println(result);
				}
				else {
					lines.add(result);
				}

				currentLine = new StringBuilder(word);
			}
			else {
				if (!currentLine.isEmpty()) {
					currentLine.append(" ");
				}
				currentLine.append(word);
			}
		}
		// Add the last line if it has content
		if (!currentLine.isEmpty()) {
			String result = addPadding(currentLine, terminalWidth);

			if (print) {
				System.out.println(result);
			}
			else {
				lines.add(result);
			}
		}
		return print ? null : String.join("\n", lines);
	}

	/**
	 * Adds padding spaces to the given {@link StringBuilder} input to align the text to the specified terminal width.
	 * The padding is calculated as the difference between the terminal width and the current length of the input.
	 * <p>
	 * If any exception occurs during padding calculation, the original input string
	 * is returned without modification.
	 *
	 * @param inputSb       the {@link StringBuilder} containing the text to pad
	 * @param terminalWidth the total width of the terminal to align the text to
	 * @return a {@link String} with added padding spaces to align the text,
	 *         or the original text if padding cannot be applied
	 */
	public static synchronized String addPadding(StringBuilder inputSb, int terminalWidth) {
		try {
			int padding = terminalWidth - inputSb.length();
			// add padding to align text
			return addPadding(inputSb.toString(), padding);
		}
		catch (Exception ignored) {
			return inputSb.toString();
		}
	}

	/**
	 * Adds padding spaces to the left or right of the input to reach the specified padding length.
	 * <p>
	 * Padding is appended on the right if the input contains Arabic characters; otherwise on the left.
	 * </p>
	 *
	 * @param input   the input text
	 * @param padding the number of spaces to add
	 * @return the padded string
	 */
	public static synchronized String addPadding(String input, int padding) {
		// TODO: this is not needed in windows after rechecking.
		// return " ".repeat(padding) + input;
		// TODO: it works like this in windows (maybe Posix systems still need extra
		// fixes, like
		// above)
		return containsArabic(input) ? input + " ".repeat(padding) : " ".repeat(padding) + input;
	}

	/**
	 * Removes Arabic diacritic marks from the given Arabic text.
	 *
	 * @param text the Arabic text possibly containing diacritics
	 * @return the Arabic text with diacritics removed
	 */
	public static String removeDiacritics(String text) {
		// Remove all characters in the range of Arabic diacritic marks
		return text.replaceAll(ARABIC_DIACRITICS_REGEX, "");
	}

	/**
	 * Transliterates the given text(s) from Latin script to Arabic or vice versa,
	 * using the specified ICU Transliterator ID and optional custom rules.
	 *
	 * @param transliteratorID the ICU Transliterator ID to use
	 * @param removeDiacritics whether to remove diacritics after transliteration
	 * @param customRules      optional custom transliteration rules; may be null
	 * @param text             one or more strings to transliterate
	 * @return an array of transliterated strings in the same order
	 */
	public static String[] transliterateScript( String transliteratorID,
												boolean removeDiacritics,
												String customRules,
												String... text) {
		Transliterator transliterator = null;

		if (Objects.nonNull(customRules) && !customRules.isEmpty() && !customRules.isBlank()) {
			String customTransliteratorID = "Custom";
			// Create a transliterator to convert based on ID with the custom rules
			Transliterator customTransliterator = Transliterator
					.createFromRules(customTransliteratorID, customRules, Transliterator.FORWARD);
			Transliterator.registerInstance(customTransliterator);
			// Create a transliterator to convert based on ID
			transliterator = Transliterator.getInstance(customTransliteratorID + "; " + transliteratorID);
		}
		if (Objects.isNull(transliterator)) {
			// Create a transliterator to convert based on ID
			transliterator = Transliterator.getInstance(transliteratorID);
		}

		for (int i = 0; i < text.length; i++) {
			text[i] = transliterateScript(transliterator, removeDiacritics, text[i]);
		}

		return text;
	}

	/**
	 * Transliterates a single word using the given Transliterator.
	 *
	 * @param transliterator   the ICU Transliterator instance to use
	 * @param removeDiacritics whether to remove diacritics after transliteration
	 * @param word             the input word to transliterate
	 * @return the transliterated word
	 */
	public static String transliterateScript(Transliterator transliterator, boolean removeDiacritics, String word) {
		// Apply transliteration
		word = splitIdentifier(word)
				.stream()
				.map(s -> s.toLowerCase(Locale.US))
				.map(transliterator::transliterate)
				.collect(Collectors.joining());

		// Remove the diacritics from the Arabic text
		if (removeDiacritics) {
			word = removeDiacritics(word);
		}

		return word;
	}

	/**
	 * Transliterates the input text letter by letter using the specified transliterator ID.
	 *
	 * @param transliteratorID the ICU Transliterator ID to use
	 * @param textInput        the input text to transliterate
	 * @return the transliterated text
	 */
	public static String transliterateScriptLetterByLetter(String transliteratorID, String textInput) {
		textInput = textInput.toLowerCase(Locale.US);
		Transliterator transliterator = Transliterator.getInstance(transliteratorID);
		// Iterate over each character and apply transliteration
		StringBuilder textOutput = new StringBuilder();

		for (int i = 0; i < textInput.length(); i++) {
			char latinChar = textInput.charAt(i);

			// We transliterate one character at a time
			String transliteratedChar = transliterator.transliterate(String.valueOf(latinChar));

			// Append the transliterated character to the output
			textOutput.append(transliteratedChar);
		}

		return textOutput.toString();
	}

	/**
	 * Transliterates one or more strings using the specified transliterator ID.
	 * Diacritics are not removed.
	 *
	 * @param transliteratorID the ICU Transliterator ID
	 * @param text             the input strings
	 * @return transliterated strings
	 */
	public static String[] transliterateScript(String transliteratorID, String... text) {
		return transliterateScript(transliteratorID, false, null, text);
	}

	/**
	 * Transliterates one or more strings using the specified transliterator ID and custom rules.
	 * Diacritics are not removed.
	 *
	 * @param transliteratorID the ICU Transliterator ID
	 * @param customRules      custom transliteration rules
	 * @param text             the input strings
	 * @return transliterated strings
	 */
	public static String[] transliterateScript(String transliteratorID, String customRules, String... text) {
		return transliterateScript(transliteratorID, false, customRules, text);
	}

	/**
	 * Transliterates one or more strings to Arabic script.
	 * Diacritics are removed by default.
	 *
	 * @param removeDiacritics whether to remove diacritics after transliteration
	 * @param text             the input strings
	 * @return transliterated Arabic script strings
	 */
	public static String[] transliterateToArabicScript(boolean removeDiacritics, String... text) {
		return transliterateScript(LATIN_ARABIC_TRANSLITERATION_ID, removeDiacritics, null, text);
	}

	/**
	 * Transliterates one or more strings to Arabic script using default custom rules.
	 * Diacritics are removed by default.
	 *
	 * @param removeDiacritics whether to remove diacritics after transliteration
	 * @param text             the input strings
	 * @return transliterated Arabic script strings
	 */
	public static String[] transliterateToArabicScriptDefaultCustom(boolean removeDiacritics, String... text) {
		return transliterateScript(LATIN_ARABIC_TRANSLITERATION_ID, removeDiacritics, CUSTOM_RULES, text);
	}

	/**
	 * Transliterates one or more strings to Arabic script using provided custom rules.
	 * Diacritics are removed by default.
	 *
	 * @param removeDiacritics whether to remove diacritics after transliteration
	 * @param customRules      custom transliteration rules
	 * @param text             the input strings
	 * @return transliterated Arabic script strings
	 */
	public static String[] transliterateToArabicScript(boolean removeDiacritics, String customRules, String... text) {
		return transliterateScript(LATIN_ARABIC_TRANSLITERATION_ID, removeDiacritics, customRules, text);
	}

	/**
	 * Transliterates one or more strings to Arabic script.
	 * Diacritics are removed by default.
	 *
	 * @param text the input strings
	 * @return transliterated Arabic script strings
	 */
	public static String[] transliterateToArabicScript(String... text) {
		return transliterateToArabicScript(true, text);
	}

	/**
	 * Transliterates one or more strings to Arabic script using default custom rules.
	 * Diacritics are removed by default.
	 *
	 * @param text the input strings
	 * @return transliterated Arabic script strings
	 */
	public static String[] transliterateToArabicScriptDefaultCustom(String... text) {
		return transliterateToArabicScript(true, CUSTOM_RULES, text);
	}

	/**
	 * Transliterates one or more strings to Arabic script using the provided custom rules.
	 * Diacritics are removed by default.
	 *
	 * @param customRules custom transliteration rules
	 * @param text        the input strings
	 * @return transliterated Arabic script strings
	 */
	public static String[] transliterateToArabicScript(String customRules, String... text) {
		return transliterateToArabicScript(true, customRules, text);
	}

	/**
	 * Transliterates the given text to Arabic script letter by letter.
	 *
	 * @param text the input text
	 * @return the transliterated Arabic script text
	 */
	public static String transliterateToArabicScriptLetterByLetter(String text) {
		return transliterateScriptLetterByLetter(LATIN_ARABIC_TRANSLITERATION_ID, text);
	}

	/**
	 * Indicates whether Arabic text shaping should be applied for the current OS.
	 *
	 * @return true if the OS is Windows (where reshaping is needed), false otherwise
	 */
	public static boolean shouldReshape() {
		return OS.isFamilyWindows();
	}

	/**
	 * Checks if the given text contains any Arabic characters.
	 *
	 * @param text the text to check
	 * @return true if the text contains one or more Arabic characters, false otherwise
	 */
	public static boolean containsArabic(String text) {
		return text.codePoints().anyMatch(ArabicUtils::isArabicChar);
	}

	/**
	 * Checks if the given Unicode code point is an Arabic character.
	 *
	 * @param cp the Unicode code point
	 * @return true if the code point is in Arabic Unicode blocks, false otherwise
	 */
	public static boolean isArabicCharCp(int cp) {
		return (cp >= 0x0600 && cp <= 0x06FF) || (cp >= 0x0750 && cp <= 0x077F) // Arabic Supplement
				|| (cp >= 0x08A0 && cp <= 0x08FF); // Arabic Extended
	}

	/**
	 * Checks if the given Unicode code point belongs to the Arabic Unicode script.
	 *
	 * @param cp the Unicode code point
	 * @return true if the code point belongs to the Arabic Unicode script, false otherwise
	 */
	public static boolean isArabicChar(int cp) {
		return Character.UnicodeScript.of(cp) == Character.UnicodeScript.ARABIC;
	}

	/**
	 * Returns a list of pairs representing the Unicode code points (in hex) and characters from the given character
	 * array.
	 *
	 * @param charArray the array of characters to analyze
	 * @return list of pairs with Unicode code point hex strings and character strings
	 */
	public static List<Pair<String, String>> getRawHexBytes(char[] charArray) {
		var hexAndCharPairArrayList = new ArrayList<Pair<String, String>>();
		System.out.println("Character stream:");
		for (char c : charArray) {
			var hexAndCharPair = Pair.of("U+%04X".formatted((int) c), "%c".formatted(c));
			hexAndCharPairArrayList.add(hexAndCharPair);
			System.out.printf("U+%04X '%c'%n", (int) c, c);
		}
		return hexAndCharPairArrayList;
	}

	/**
	 * Converts the given {@link String} into a list of pairs, where each pair contains the Unicode hexadecimal
	 * representation of a character and the character itself.
	 *
	 * @param text the input string to process
	 * @return a list of pairs of the form ("U+XXXX", "char"), representing each character's Unicode code point and
	 *         character
	 */
	public static List<Pair<String, String>> getRawHexBytes(String text) {
		return getRawHexBytes(text.toCharArray());
	}

	/**
	 * Splits an identifier string into constituent parts based on various naming conventions.
	 * It handles underscores, dashes, whitespace, camelCase, PascalCase, acronyms, and digits.
	 * <p>
	 * Example:
	 * - "userAccount" → ["user", "Account"]
	 * - "IPv6Address" → ["IPv", "6", "Address"]
	 * - "snake_case-name" → ["snake", "case", "name"]
	 *
	 * @param input the identifier string to split
	 * @return a list of strings representing the split components of the identifier
	 */
	public static List<String> splitIdentifier(String input) {
		// Normalize underscores and dashes to spaces
		String normalized = input.replaceAll("[-_]", " ");

		// Split into segments by whitespace first (e.g., snake_case and kebab-case)
		String[] initialParts = normalized.split("\\s+");

		List<String> result = new ArrayList<>();
		for (String part : initialParts) {
			// Split camelCase, PascalCase, acronyms, numbers
			result.addAll(Arrays.asList(part.split(IDENTIFIER_SPLIT_REGEX)));
		}

		return result;
	}

	/**
	 * Converts an input string from Arabic characters and digits to their Latin equivalents.
	 * <p>
	 * This method supports:
	 * <ul>
	 * <li>Arabic letters mapped one-to-one to Latin uppercase letters (A-Z).</li>
	 * <li>Arabic-Indic digits (٠-٩) mapped to Latin digits (0-9).</li>
	 * <li>Latin letters (A-Z, a-z) and digits (0-9) passed through unchanged.</li>
	 * </ul>
	 * <p>
	 * Any unsupported character will cause a {@code NaftahBugError} to be thrown.
	 *
	 * @param arabicText the input string containing Arabic characters and/or digits
	 * @return the Latin-equivalent string after transliteration
	 * @throws NaftahBugError if the input contains unsupported characters
	 */
	public static String convertArabicToLatinLetterByLetter(String arabicText) {
		StringBuilder latinText = new StringBuilder();

		for (int i = 0; i < arabicText.length(); i++) {
			char maybeArabicChar = arabicText.charAt(i);

			int index = -1;
			for (int j = 0; j < ARABIC_LETTERS.length; j++) {
				if (maybeArabicChar == ARABIC_LETTERS[j]) {
					index = j;
					break;
				}
			}

			if (index != -1) {
				latinText.append(LATIN_LETTERS[index]);
			}
			else if (isArabicDigit(maybeArabicChar)) {
				// Arabic digit (٠ to ٩) maps to Latin digits (0 to 9)
				char latinDigit = (char) ('0' + (maybeArabicChar - '٠'));
				latinText.append(latinDigit);
			}
			else if (isLatinDigit(maybeArabicChar) || isLatinLetter(maybeArabicChar)) {
				latinText.append(maybeArabicChar);
			}
			else {
				throw new NaftahBugError(String
						.format("""
								الحرف '%c' في النص '%s' غير مدعوم. يرجى التأكد من أن جميع الأحرف المدخلة مدعومة.""",
								maybeArabicChar,
								arabicText));
			}
		}

		return latinText.toString();
	}

	/**
	 * Checks whether a character is a Latin letter (A-Z or a-z).
	 *
	 * @param ch the character to check
	 * @return {@code true} if the character is a Latin letter; {@code false} otherwise
	 */
	public static boolean isLatinLetter(char ch) {
		return (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z');
	}

	/**
	 * Checks whether a character is a Latin digit (0-9).
	 *
	 * @param ch the character to check
	 * @return {@code true} if the character is a Latin digit; {@code false} otherwise
	 */
	public static boolean isLatinDigit(char ch) {
		return (ch >= '0' && ch <= '9');
	}

	/**
	 * Checks whether a character is an Arabic-Indic digit (٠ to ٩).
	 *
	 * @param ch the character to check
	 * @return {@code true} if the character is an Arabic digit; {@code false} otherwise
	 */
	public static boolean isArabicDigit(char ch) {
		return (ch >= '٠' && ch <= '٩');
	}

}
