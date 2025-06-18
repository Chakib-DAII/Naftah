package org.daiitech.naftah.core.builtin.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * @author Chakib Daii
 */
public final class StringUtils {
    public static final BiFunction<Character, Character, Integer> ADD = Integer::sum;
    public static final BiFunction<Character, Character, Integer> SUBTRACT = Math::subtractExact;
    public static final BiFunction<Character, Character, Integer> MUL = Math::multiplyExact;
    public static final BiFunction<Character, Character, Integer> DIV = Math::floorDiv;
    public static final BiFunction<Character, Character, Integer> MOD = Math::floorMod;

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
        if (parts <= 0) throw new IllegalArgumentException("Parts must be > 0");

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
    public static String applyOperation(String a, String b, BiFunction<Character, Character, Integer> operartion) {
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
    public static String charWiseDivision(String a, String b) {
        return applyOperation(a, b, DIV);
    }
    // Bitwise MOD (character-wise)
    public static String charWiseModulo(String a, String b) {
        return applyOperation(a, b, MOD);
    }

    // Bitwise XOR (character-wise)
    public static String xor(String a, String b) {
        int minLen = Math.min(a.length(), b.length());
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < minLen; i++) {
            char c1 = a.charAt(i);
            char c2 = b.charAt(i);
            result.append((char)(c1 ^ c2));
        }

        return result.toString();
    }

    // Bitwise AND (character-wise AND)
    public static String and(String a, String b) {
        int minLen = Math.min(a.length(), b.length());
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < minLen; i++) {
            result.append((char)(a.charAt(i) & b.charAt(i)));
        }
        return result.toString();
    }

    // Bitwise OR (character-wise OR)
    public static String or(String a, String b) {
        int minLen = Math.min(a.length(), b.length());
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < minLen; i++) {
            result.append((char)(a.charAt(i) | b.charAt(i)));
        }
        return result.toString();
    }

    // Bitwise NOT (character-wise NOT)
    public static String not(String a) {
        StringBuilder result = new StringBuilder();
        for (char c : a.toCharArray()) {
            result.append((char) (~c));
        }
        return result.toString();
    }

    public static String preIncrement(String a) {
        StringBuilder result = new StringBuilder();
        for (char c : a.toCharArray()) {
            result.append((char) (NumberUtils.preIncrement((int)c).intValue()));
        }
        return result.toString();
    }

    public static String postIncrement(String a) {
        StringBuilder result = new StringBuilder();
        for (char c : a.toCharArray()) {
            result.append((char) (NumberUtils.postIncrement((int)c).intValue()));
        }
        return result.toString();
    }

    public static String preDecrement(String a) {
        StringBuilder result = new StringBuilder();
        for (char c : a.toCharArray()) {
            result.append((char) (NumberUtils.preDecrement((int)c).intValue()));
        }
        return result.toString();
    }

    public static String postDecrement(String a) {
        StringBuilder result = new StringBuilder();
        for (char c : a.toCharArray()) {
            result.append((char) (NumberUtils.postDecrement((int)c).intValue()));
        }
        return result.toString();
    }

    public static int stringToInt(String s) {
        return s.codePoints().sum();
    }
}
