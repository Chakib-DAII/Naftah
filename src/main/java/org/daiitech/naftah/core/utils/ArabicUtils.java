package org.daiitech.naftah.core.utils;

import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import com.ibm.icu.text.Transliterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Chakib Daii
 */
public class ArabicUtils {
  // Escape code for setting RTL text direction in the terminal (for compatible terminals)
  public static final String RTL_DIRECTION = "\u001B[?2004h"; // Set RTL
  public static final String LTR_DIRECTION = "\u001B[?2004l"; // Set LTR
  // Arabic diacritic marks in Unicode
  public static final String ARABIC_DIACRITICS_REGEX = "[\u0610-\u061A\u064B-\u0652\u0670]";
  public static final String ANSI_ESCAPE = "\033[H\033[2J";
  public static final String LATIN_ARABIC_TRANSLITERATION_ID = "Any-Latin; Latin-Arabic";
  public static final String ARABIC_LANGUAGE = "ar";
  public static final String DEFAULT_ARABIC_LANGUAGE_COUNTRY = "AE";
  public static final String CUSTOM_RULES =
      """
            com > كوم;
            org > أورغ;
            co > كو;
            o > و;
            aa > ع;
            a > ا;
            b > ب;
            tech > تاك;
            t > ت;
            ii > عي;""";

  public static final Pattern SHAPE_MULTILINE_PATTERN = Pattern.compile("(.+?)(\\r\\n|\\n|\\r|$)");
  private static final Map<String, Matcher> SHAPE_MATCHER_CACHE = new HashMap<>();

  public static boolean isMultiline(String input) {
    return getShapeMatcher(input).find();
  }

  private static Matcher getShapeMatcher(String input) {
    if (SHAPE_MATCHER_CACHE.containsKey(input)) return SHAPE_MATCHER_CACHE.get(input);

    Matcher matcher = SHAPE_MULTILINE_PATTERN.matcher(input);
    SHAPE_MATCHER_CACHE.put(input, matcher);
    return matcher;
  }

  public static synchronized String shape(String input) throws ArabicShapingException {
    if (isMultiline(input)) {
      Matcher matcher = getShapeMatcher(input).reset();

      StringBuilder shapedOutput = new StringBuilder();

      while (matcher.find()) {
        String line = matcher.group(1);
        String newline = matcher.group(2); // might be "" on last line

        if (!line.trim().isEmpty()) {
          shapedOutput.append(doShape(line));
        }

        shapedOutput.append(newline); // keep original newlines
      }

      return shapedOutput.toString();
    } else return doShape(input);
  }

  public static synchronized String doShape(String input) throws ArabicShapingException {
    ArabicShaping shaper =
        new ArabicShaping(ArabicShaping.LETTERS_SHAPE | ArabicShaping.TEXT_DIRECTION_VISUAL_RTL);
    String shaped = shaper.shape(input);
    Bidi bidi = new Bidi(shaped, Bidi.DIRECTION_RIGHT_TO_LEFT);
    return bidi.writeReordered(Bidi.DO_MIRRORING);
  }

  /**
   * Removes diacritic marks from Arabic text.
   *
   * @param text Arabic text with diacritics
   * @return Arabic text without diacritics
   */
  public static String removeDiacritics(String text) {
    // Remove all characters in the range of Arabic diacritic marks
    return text.replaceAll(ARABIC_DIACRITICS_REGEX, "");
  }

  public static String transliterateScript(
      String transliteratorID, String text, boolean removeDiacritics, String customRules) {
    Transliterator transliterator = null;

    if (Objects.nonNull(customRules) && !customRules.isEmpty() && !customRules.isBlank()) {
      String customTransliteratorID = "Custom";
      // Create a transliterator to convert based on ID with the custom rules
      Transliterator customTransliterator =
          Transliterator.createFromRules(
              customTransliteratorID, customRules, Transliterator.FORWARD);
      Transliterator.registerInstance(customTransliterator);
      // Create a transliterator to convert based on ID
      transliterator = Transliterator.getInstance(customTransliteratorID + "; " + transliteratorID);
    }
    if (Objects.isNull(transliterator)) {
      // Create a transliterator to convert based on ID
      transliterator = Transliterator.getInstance(transliteratorID);
    }

    // Apply transliteration
    text = transliterator.transliterate(text);

    // Remove the diacritics from the Arabic text
    if (removeDiacritics) text = removeDiacritics(text);

    return text;
  }

  public static String transliterateScriptLetterByLetter(
      String transliteratorID, String textInput) {
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

  public static String transliterateScript(String transliteratorID, String text) {
    return transliterateScript(transliteratorID, text, false, null);
  }

  public static String transliterateScript(
      String transliteratorID, String text, String customRules) {
    return transliterateScript(transliteratorID, text, false, customRules);
  }

  public static String transliterateToArabicScript(String text, boolean removeDiacritics) {
    return transliterateScript(LATIN_ARABIC_TRANSLITERATION_ID, text, removeDiacritics, null);
  }

  public static String transliterateToArabicScriptDefaultCustom(
      String text, boolean removeDiacritics) {
    return transliterateScript(
        LATIN_ARABIC_TRANSLITERATION_ID, text, removeDiacritics, CUSTOM_RULES);
  }

  public static String transliterateToArabicScript(
      String text, boolean removeDiacritics, String customRules) {
    return transliterateScript(
        LATIN_ARABIC_TRANSLITERATION_ID, text, removeDiacritics, customRules);
  }

  public static String transliterateToArabicScript(String text) {
    return transliterateToArabicScript(text, true);
  }

  public static String transliterateToArabicScriptDefaultCustom(String text) {
    return transliterateToArabicScript(text, true, CUSTOM_RULES);
  }

  public static String transliterateToArabicScript(String text, String customRules) {
    return transliterateToArabicScript(text, true, customRules);
  }

  public static String transliterateToArabicScriptLetterByLetter(String text) {
    return transliterateScriptLetterByLetter(LATIN_ARABIC_TRANSLITERATION_ID, text);
  }
}
