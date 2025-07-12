package org.daiitech.naftah.builtin.utils;

import org.daiitech.naftah.errors.NaftahBugError;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author Chakib Daii
 */
public final class StringUtils {
  // Bit wise
  public static final BiFunction<Character, Character, Integer> XOR =
      (character, character2) -> character ^ character2;
  public static final BiFunction<Character, Character, Integer> AND =
      (character, character2) -> character & character2;
  public static final BiFunction<Character, Character, Integer> OR =
      (character, character2) -> character | character2;
  public static final Function<Character, Number> NOT = (character) -> ~character;

  // Char wise
  public static final BiFunction<Character, Character, Integer> ADD = Integer::sum;
  public static final BiFunction<Character, Character, Integer> SUBTRACT = Math::subtractExact;
  public static final BiFunction<Character, Character, Integer> MUL = Math::multiplyExact;
  public static final BiFunction<Character, Character, Integer> DIV = Math::floorDiv;
  public static final BiFunction<Character, Character, Integer> MOD = Math::floorMod;
  public static final Function<Character, Number> PRE_INCREMENT = NumberUtils::preIncrement;
  public static final Function<Character, Number> POST_INCREMENT = NumberUtils::postIncrement;
  public static final Function<Character, Number> PRE_DECREMENT = NumberUtils::preDecrement;
  public static final Function<Character, Number> POST_DECREMENT = NumberUtils::postDecrement;

  // Equality
  public static boolean equals(String a, String b) {
    return a.equals(b);
  }

  // Lexicographic comparison

  public static int compare(String a, String b) {
    return a.compareTo(b);
  }

  // Concatenation
  public static String add(String a, String b) {
    return a + b;
  }

  // Subtract (remove all occurrences of b from a)
  public static String subtract(String a, String b) {
    return a.replace(b, "");
  }

  // Split (divide)
  public static String[] divide(String a, String delimiter) {
    return a.split(delimiter);
  }

  public static String[] divide(String s, int parts) {
    if (parts <= 0) throw new NaftahBugError("يجب أن يكون عدد الأجزاء أكبر من 0.");

    int length = s.length();
    int partSize = length / parts;
    int remainder = length % parts;

    List<String> result = new ArrayList<>();
    int start = 0;

    for (int i = 0; i < parts; i++) {
      int currentPartSize = partSize + (i < remainder ? 1 : 0);
      int end = start + currentPartSize;
      result.add(s.substring(start, end));
      start = end;
    }
    return result.toArray(String[]::new);
  }

  // repeat (multiply)
  public static String multiply(String a, int multiplier) {
    return a.repeat(multiplier);
  }

  // char wise operation
  public static String applyOperation(
      String a, String b, BiFunction<Character, Character, Integer> operartion) {
    int minLen = Math.min(a.length(), b.length());
    StringBuilder result = new StringBuilder(minLen);

    for (int i = 0; i < minLen; i++) {
      char c1 = a.charAt(i);
      char c2 = b.charAt(i);

      int intResult = operartion.apply(c1, c2);
      // Cast result to char (lower 16 bits)
      char charResult = (char) intResult;

      result.append(charResult);
    }

    return result.toString();
  }

  // Bitwise ADD (character-wise)
  public static String charWiseAdd(String a, String b) {
    return applyOperation(a, b, ADD);
  }

  // Bitwise SUBTRACT (character-wise)
  public static String charWiseSubtract(String a, String b) {
    return applyOperation(a, b, SUBTRACT);
  }

  // Bitwise MUL (character-wise)
  public static String charWiseMultiply(String a, String b) {
    return applyOperation(a, b, MUL);
  }

  // Bitwise DIV (character-wise)
  public static String charWiseDivide(String a, String b) {
    return applyOperation(a, b, DIV);
  }

  // Bitwise MOD (character-wise)
  public static String charWiseModulo(String a, String b) {
    return applyOperation(a, b, MOD);
  }

  // Bitwise XOR (character-wise)
  public static String xor(String a, String b) {
    return applyOperation(a, b, XOR);
  }

  // Bitwise AND (character-wise AND)
  public static String and(String a, String b) {
    return applyOperation(a, b, AND);
  }

  // Bitwise OR (character-wise OR)
  public static String or(String a, String b) {
    return applyOperation(a, b, OR);
  }

  public static String applyOperation(String a, Function<Character, Number> operartion) {
    StringBuilder result = new StringBuilder();
    for (char c : a.toCharArray()) {
      result.append((char) operartion.apply(c).intValue());
    }
    return result.toString();
  }

  // Bitwise NOT (character-wise NOT)
  public static String not(String a) {
    return applyOperation(a, NOT);
  }

  public static String preIncrement(String a) {
    return applyOperation(a, PRE_INCREMENT);
  }

  public static String postIncrement(String a) {
    return applyOperation(a, POST_INCREMENT);
  }

  public static String preDecrement(String a) {
    return applyOperation(a, PRE_DECREMENT);
  }

  public static String postDecrement(String a) {
    return applyOperation(a, POST_DECREMENT);
  }

  public static int stringToInt(String s) {
    return s.codePoints().sum();
  }
}
