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
import java.util.stream.Collectors;
import org.daiitech.naftah.utils.OS;
import org.daiitech.naftah.utils.function.ThrowingBiFunction;
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
  public static final String DEFAULT_ARABIC_LANGUAGE_COUNTRY = "TN";
  public static final Locale ARABIC = new Locale(ARABIC_LANGUAGE);
  private static final String IDENTIFIER_SPLIT_REGEX =
      // JSONTo → JSON, To
      "(?<=[A-Z])(?=[A-Z][a-z])"
          + // userAccount → user, Account
          "|(?<=[^A-Z])(?=[A-Z])"
          + // IPv6 → IPv, 6
          "|(?<=[A-Za-z])(?=\\d)"
          + // 6Parser → 6, Parser
          "|(?<=\\d)(?=[A-Za-z])";
  public static final ResourceBundle CUSTOM_RULES_BUNDLE =
      ResourceBundle.getBundle("transliteration", ARABIC);
  public static String CUSTOM_RULES =
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
            ii > عي;
            """;

  public static final Pattern TEXT_MULTILINE_PATTERN = Pattern.compile("(.+?)(\\r\\n|\\n|\\r|$)");
  private static final Map<String, Matcher> TEXT_MATCHER_CACHE = new HashMap<>();

  static {
    Set<String> keys = CUSTOM_RULES_BUNDLE.keySet();

    // Convert to List
    List<String> keylist = new ArrayList<>(keys);

    // Sort by length descending
    keylist.sort((s1, s2) -> Integer.compare(s2.length(), s1.length()));

    StringBuilder stringBuilder = new StringBuilder();
    for (String key : keylist) {
      String value = CUSTOM_RULES_BUNDLE.getString(key);
      // If s contains underscore or spaces or any special char, quote it
      if (value.matches(".*[ _\\t\\r\\n].*")) { // underscore or space or whitespace
        value = "'" + value + "'";
      }

      stringBuilder.append("""
              %s > %s;
              """.formatted(key, value));
    }
    CUSTOM_RULES = stringBuilder + CUSTOM_RULES;
  }

  public static boolean isMultiline(String input) {
    return getTextMatcher(input).find();
  }

  private static Matcher getTextMatcher(String input) {
    if (TEXT_MATCHER_CACHE.containsKey(input)) return TEXT_MATCHER_CACHE.get(input).reset();

    Matcher matcher = TEXT_MULTILINE_PATTERN.matcher(input);
    TEXT_MATCHER_CACHE.put(input, matcher);
    return matcher.reset();
  }

  public static synchronized String applyBiFunction(
      String input, boolean print, ThrowingBiFunction<String, Boolean, String> function) {
    if (isMultiline(input)) {
      Matcher matcher = getTextMatcher(input).reset();

      StringBuilder output = print ? null : new StringBuilder();

      while (matcher.find()) {
        String line = matcher.group(1);
        String newline = matcher.group(2); // might be "" on last line

        if (!line.trim().isEmpty()) {
          String result = function.apply(line, print);
          if (!print) output.append(result);
        }

        if (!print) output.append(newline); // keep original newlines
      }
      return print ? null : output.toString();

    } else {
      String result = function.apply(input, print);
      return print ? null : result;
    }
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

  public static synchronized String padText(String input, boolean print) {
    return applyBiFunction(input, print, ArabicUtils::doPadText);
  }

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
    } else return result;
  }

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
        } else lines.add(result);

        currentLine = new StringBuilder(word);
      } else {
        if (!currentLine.isEmpty()) currentLine.append(" ");
        currentLine.append(word);
      }
    }
    // Add the last line if it has content
    if (!currentLine.isEmpty()) {
      String result = addPadding(currentLine, terminalWidth);

      if (print) {
        System.out.println(result);
      } else lines.add(result);
    }
    return print ? null : String.join("\n", lines);
  }

  public static synchronized String addPadding(StringBuilder inputSb, int terminalWidth) {
    try {
      int padding = terminalWidth - inputSb.length();
      // add padding to align text
      return addPadding(inputSb.toString(), padding);
    } catch (Exception ignored) {
      return inputSb.toString();
    }
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

  public static String[] transliterateScript(
      String transliteratorID, boolean removeDiacritics, String customRules, String... text) {
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

    for (int i = 0; i < text.length; i++) {
      text[i] = transliterateScript(transliterator, removeDiacritics, text[i]);
    }

    return text;
  }

  public static String transliterateScript(
      Transliterator transliterator, boolean removeDiacritics, String word) {
    // Apply transliteration
    word =
        splitIdentifier(word).stream()
            .map(transliterator::transliterate)
            .collect(Collectors.joining());

    // Remove the diacritics from the Arabic text
    if (removeDiacritics) word = removeDiacritics(word);

    return word;
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

  public static String[] transliterateScript(String transliteratorID, String... text) {
    return transliterateScript(transliteratorID, false, null, text);
  }

  public static String[] transliterateScript(
      String transliteratorID, String customRules, String... text) {
    return transliterateScript(transliteratorID, false, customRules, text);
  }

  public static String[] transliterateToArabicScript(boolean removeDiacritics, String... text) {
    return transliterateScript(LATIN_ARABIC_TRANSLITERATION_ID, removeDiacritics, null, text);
  }

  public static String[] transliterateToArabicScriptDefaultCustom(
      boolean removeDiacritics, String... text) {
    return transliterateScript(
        LATIN_ARABIC_TRANSLITERATION_ID, removeDiacritics, CUSTOM_RULES, text);
  }

  public static String[] transliterateToArabicScript(
      boolean removeDiacritics, String customRules, String... text) {
    return transliterateScript(
        LATIN_ARABIC_TRANSLITERATION_ID, removeDiacritics, customRules, text);
  }

  public static String[] transliterateToArabicScript(String... text) {
    return transliterateToArabicScript(true, text);
  }

  public static String[] transliterateToArabicScriptDefaultCustom(String... text) {
    return transliterateToArabicScript(true, CUSTOM_RULES, text);
  }

  public static String[] transliterateToArabicScript(String customRules, String... text) {
    return transliterateToArabicScript(true, customRules, text);
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

  public static boolean isArabicCharCp(int cp) {
    return (cp >= 0x0600 && cp <= 0x06FF)
        || (cp >= 0x0750 && cp <= 0x077F) // Arabic Supplement
        || (cp >= 0x08A0 && cp <= 0x08FF); // Arabic Extended
  }

  public static boolean isArabicChar(int cp) {
    return Character.UnicodeScript.of(cp) == Character.UnicodeScript.ARABIC;
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
}
