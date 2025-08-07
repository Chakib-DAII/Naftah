package org.daiitech.naftah.builtin.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.daiitech.naftah.errors.NaftahBugError;

import jdk.incubator.vector.ShortVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;

import static org.daiitech.naftah.Naftah.VECTOR_API_PROPERTY;

/**
 * @author Chakib Daii
 */
public final class StringUtils {
	// Bit wise
	public static final BiFunction<Character, Character, Integer> XOR = (character, character2) -> character ^ character2;
	public static final BiFunction<Character, Character, Integer> AND = (character, character2) -> character & character2;
	public static final BiFunction<Character, Character, Integer> OR = (character, character2) -> character | character2;
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

	// Vector

	public static final Boolean USE_VECTOR_API = Boolean.getBoolean(VECTOR_API_PROPERTY);
	public static final int VECTOR_THRESHOLD = 128; // TODO: tune the threshold
	private static final VectorSpecies<Short> SPECIES = ShortVector.SPECIES_PREFERRED;
	// Bit wise
	public static final BiFunction<ShortVector, ShortVector, ShortVector> XOR_VEC = (v1, v2) -> v1.lanewise(VectorOperators.XOR, v2);
	public static final BiFunction<ShortVector, ShortVector, ShortVector> AND_VEC = ShortVector::and;
	public static final BiFunction<ShortVector, ShortVector, ShortVector> OR_VEC = ShortVector::or;
	public static final Function<ShortVector, ShortVector> NOT_VEC = ShortVector::not;

	// Char wise
	public static final BiFunction<ShortVector, ShortVector, ShortVector> ADD_VEC = ShortVector::add;
	public static final BiFunction<ShortVector, ShortVector, ShortVector> SUBTRACT_VEC = ShortVector::sub;
	public static final BiFunction<ShortVector, ShortVector, ShortVector> MUL_VEC = ShortVector::mul;
	public static final BiFunction<ShortVector, ShortVector, ShortVector> DIV_VEC = ShortVector::div;
	public static final BiFunction<ShortVector, ShortVector, ShortVector> MOD_VEC = (v1, v2) -> v1.sub(v1.div(v2).mul(v2)); // simulates floorMod: a - (a / b) * b
	public static final Function<ShortVector, ShortVector> PRE_INCREMENT_VEC = (v) -> v.add((short) 1);
	public static final Function<ShortVector, ShortVector> PRE_DECREMENT_VEC = (v) -> v.sub((short) 1);

	// Mappings from scalar to vector
	public static final Map<BiFunction<Character, Character, Integer>, BiFunction<ShortVector, ShortVector, ShortVector>> BINARY_OP_MAP = Map.of(XOR, XOR_VEC, AND, AND_VEC, OR, OR_VEC, ADD, ADD_VEC, SUBTRACT, SUBTRACT_VEC, MUL, MUL_VEC, DIV, DIV_VEC, MOD, MOD_VEC);
	public static final Map<Function<Character, Number>, Function<ShortVector, ShortVector>> UNARY_OP_MAP = Map.of(NOT, NOT_VEC, PRE_INCREMENT, PRE_INCREMENT_VEC, PRE_DECREMENT, PRE_DECREMENT_VEC);

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
		if (s == null || s.isEmpty())
			throw new NaftahBugError("النص لا يمكن أن يكون فارغًا.");
		if (parts <= 0)
			throw new NaftahBugError("يجب أن يكون عدد الأجزاء أكبر من 0.");
		if (parts > s.length())
			throw new NaftahBugError("عدد الأجزاء لا يمكن أن يتجاوز طول السلسلة.");

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
	public static String applyOperation(String a, String b, BiFunction<Character, Character, Integer> operation) {
		BiFunction<ShortVector, ShortVector, ShortVector> vectorOperation;
		if (!USE_VECTOR_API || a.length() < VECTOR_THRESHOLD || Objects.isNull(vectorOperation = BINARY_OP_MAP.get(operation))) {
			return applyOperationScalar(a, b, operation);
		}
		else {
			return applyOperationVectorized(a, b, operation, vectorOperation);
		}
	}

	public static String applyOperationScalar(String a, String b, BiFunction<Character, Character, Integer> operation) {
		int minLen = Math.min(a.length(), b.length());
		StringBuilder result = new StringBuilder(minLen);

		// Convert strings to char[] once
		char[] aChars = a.toCharArray();
		char[] bChars = b.toCharArray();

		for (int i = 0; i < minLen; i++) {
			result.append(doApplyOperation(aChars[i], bChars[i], operation));
		}

		return result.toString();
	}

	public static String applyOperationVectorized(String a, String b, BiFunction<Character, Character, Integer> operation, BiFunction<ShortVector, ShortVector, ShortVector> vectorOperation) {
		int minLen = Math.min(a.length(), b.length());
		StringBuilder result = new StringBuilder(minLen);
		result.ensureCapacity(minLen); // reduce internal resizing

		// Convert strings to char[] once
		char[] aChars = a.toCharArray();
		char[] bChars = b.toCharArray();

		// Reuse primitive short[] buffers
		short[] aShorts = new short[minLen];
		short[] bShorts = new short[minLen];
		short[] resultShorts = new short[minLen];

		// Convert chars to shorts once
		for (int i = 0; i < minLen; i++) {
			aShorts[i] = (short) aChars[i];
			bShorts[i] = (short) bChars[i];
		}

		int i = 0;
		int upperBound = SPECIES.loopBound(minLen);

		for (; i < upperBound; i += SPECIES.length()) {
			ShortVector v1 = ShortVector.fromArray(SPECIES, aShorts, i);
			ShortVector v2 = ShortVector.fromArray(SPECIES, bShorts, i);

			ShortVector res = vectorOperation.apply(v1, v2);
			res.intoArray(resultShorts, i);
		}

		// Fallback scalar path for the tail
		for (; i < minLen; i++) {
			resultShorts[i] = (short) doApplyOperation(aChars[i], bChars[i], operation);
		}

		// Append final result as chars
		for (int j = 0; j < minLen; j++) {
			result.append((char) resultShorts[j]);
		}

		return result.toString();
	}

	public static char doApplyOperation(char aChar, char bChar, BiFunction<Character, Character, Integer> operation) {
		int intResult = operation.apply(aChar, bChar);
		// Cast result to char (lower 16 bits)
		return (char) intResult;
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

	public static String applyOperation(String a, Function<Character, Number> operation) {
		Function<ShortVector, ShortVector> vectorOperation;
		if (!USE_VECTOR_API || a.length() < VECTOR_THRESHOLD || Objects.isNull(vectorOperation = UNARY_OP_MAP.get(operation))) {
			return applyOperationScalar(a, operation);
		}
		else {
			return applyOperationVectorized(a, operation, vectorOperation);
		}
	}

	public static String applyOperationScalar(String a, Function<Character, Number> operation) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < a.length(); i++) {
			result.append(doApplyOperation(a.charAt(i), operation));
		}
		return result.toString();
	}

	public static String applyOperationVectorized(String input, Function<Character, Number> scalarOperation, Function<ShortVector, ShortVector> vectorOperation) {
		int length = input.length();

		// Preallocate all necessary buffers
		char[] inputChars = input.toCharArray();
		short[] inputShorts = new short[length];
		short[] resultShorts = new short[length];
		char[] resultChars = new char[length];

		// Convert char[] to short[] once
		for (int i = 0; i < length; i++) {
			inputShorts[i] = (short) inputChars[i];
		}

		int i = 0;
		int upperBound = SPECIES.loopBound(length);
		int step = SPECIES.length() * 2;

		// Vectorized loop with unrolling (×2)
		for (; i <= upperBound - step; i += step) {
			ShortVector v1 = ShortVector.fromArray(SPECIES, inputShorts, i);
			ShortVector r1 = vectorOperation.apply(v1);
			r1.intoArray(resultShorts, i);

			ShortVector v2 = ShortVector.fromArray(SPECIES, inputShorts, i + SPECIES.length());
			ShortVector r2 = vectorOperation.apply(v2);
			r2.intoArray(resultShorts, i + SPECIES.length());
		}

		// Remaining vector-aligned chunk
		for (; i < upperBound; i += SPECIES.length()) {
			ShortVector v = ShortVector.fromArray(SPECIES, inputShorts, i);
			ShortVector r = vectorOperation.apply(v);
			r.intoArray(resultShorts, i);
		}

		// Scalar tail for remaining characters
		for (; i < length; i++) {
			resultShorts[i] = (short) doApplyOperation(inputChars[i], scalarOperation);
		}

		// Convert result shorts to chars
		for (int j = 0; j < length; j++) {
			resultChars[j] = (char) resultShorts[j];
		}

		return new String(resultChars);
	}

	public static char doApplyOperation(char aChar, Function<Character, Number> operation) {
		int intResult = operation.apply(aChar).intValue();
		// Cast result to char (lower 16 bits)
		return (char) intResult;
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
