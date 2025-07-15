package org.daiitech.naftah.utils.arabic;

import static org.daiitech.naftah.NaftahSystem.TERMINAL_WIDTH_PROPERTY;

import com.ibm.icu.impl.Pair;
import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import com.ibm.icu.text.Transliterator;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.daiitech.naftah.utils.OS;
import org.daiitech.naftah.utils.function.ThrowingFunction;

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

  public static final Pattern TEXT_MULTILINE_PATTERN = Pattern.compile("(.+?)(\\r\\n|\\n|\\r|$)");
  private static final Map<String, Matcher> TEXT_MATCHER_CACHE = new HashMap<>();

  public static boolean isMultiline(String input) {
    return getTextMatcher(input).find();
  }

  private static Matcher getTextMatcher(String input) {
    if (TEXT_MATCHER_CACHE.containsKey(input)) return TEXT_MATCHER_CACHE.get(input).reset();

    Matcher matcher = TEXT_MULTILINE_PATTERN.matcher(input);
    TEXT_MATCHER_CACHE.put(input, matcher);
    return matcher.reset();
  }

  public static synchronized String applyFunction(
      String input, ThrowingFunction<String, String> function) {
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
    } else return function.apply(input);
  }

  public static synchronized String shape(String input) {
    return applyFunction(input, ArabicUtils::doShape);
  }

  public static synchronized String doShape(String input) throws ArabicShapingException {
    ArabicShaping shaper =
        new ArabicShaping(ArabicShaping.LETTERS_SHAPE | ArabicShaping.TEXT_DIRECTION_VISUAL_RTL);
    String shaped = shaper.shape(input);
    Bidi bidi = new Bidi(shaped, Bidi.DIRECTION_RIGHT_TO_LEFT);
    return bidi.writeReordered(Bidi.DO_MIRRORING);
  }

  public static synchronized String padText(String input) {
    return applyFunction(input, ArabicUtils::doPadText);
  }

  public static synchronized String doPadText(String input) {
    int terminalWidth = Integer.getInteger(TERMINAL_WIDTH_PROPERTY);
    int padding = terminalWidth - input.length();
    if (padding < 0) {
      // correct text in case of terminal overflow
      return doPadText(input, terminalWidth);
    }
    // add padding to align text
    return addPadding(input, padding);
  }


  public static synchronized String doPadText(String input, int terminalWidth) {
    String[] words = input.split("\\s+");
    StringBuilder currentLine = new StringBuilder();
    List<String> lines = new ArrayList<>();

    for (String word : words) {
      if (currentLine.length() + word.length() + (!currentLine.isEmpty() ? 1 : 0) > terminalWidth) {
        // Line is full, store it and start a new one
        lines.add(addPadding(currentLine, terminalWidth));
        currentLine = new StringBuilder(word);
      } else {
        if (!currentLine.isEmpty()) currentLine.append(" ");
        currentLine.append(word);
      }
    }
    // Add the last line if it has content
    if (!currentLine.isEmpty()) {
      lines.add(addPadding(currentLine, terminalWidth));
    }
    return String.join("\n", lines);
  }

  public static synchronized String addPadding(StringBuilder inputSb, int terminalWidth) {
    int padding = terminalWidth - inputSb.length();
    // add padding to align text
    return addPadding(inputSb.toString(), padding);
  }
  public static synchronized String addPadding(String input, int padding) {
    // TODO: this is not needed in windows after rechecking.
    // return " ".repeat(padding) + input;
    // TODO: it works like this in windows (maybe Posix systems still need extra fixes, like
    // above)
    return containsArabic(input) ? input + " ".repeat(padding) : " ".repeat(padding) + input;
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

  public static boolean shouldReshape() {
    return OS.isFamilyWindows();
  }

  public static boolean containsArabic(String text) {
    return text.codePoints().anyMatch(ArabicUtils::isArabicChar);
  }

  public static boolean isArabicChar(int cp) {
    return (cp >= 0x0600 && cp <= 0x06FF)
        || (cp >= 0x0750 && cp <= 0x077F) // Arabic Supplement
        || (cp >= 0x08A0 && cp <= 0x08FF); // Arabic Extended
  }

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

  public static List<Pair<String, String>> getRawHexBytes(String text) {
    return getRawHexBytes(text.toCharArray());
  }
}
