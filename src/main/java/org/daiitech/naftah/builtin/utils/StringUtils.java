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
 * Utility class for performing various operations on {@link String} objects,
 * including arithmetic, bitwise, and vectorized character-wise operations.
 *
 * <p>Supports both scalar and vector APIs for enhanced performance on large strings.
 * The vector operations are backed by the {@link ShortVector} API.
 *
 * <p>This class also defines several functional constants used to map
 * operations across strings.
 *
 * <p>This class is not instantiable.
 *
 * @author Chakib Daii
 */
public final class StringUtils {
	// Bitwise scalar operations on characters
	/**
	 * Bitwise XOR operation for two characters.
	 */
	public static final BiFunction<Character, Character, Integer> XOR = (   character,
																			character2) -> character ^ character2;

	/**
	 * Bitwise AND operation for two characters.
	 */
	public static final BiFunction<Character, Character, Integer> AND = (   character,
																			character2) -> character & character2;

	/**
	 * Bitwise OR operation for two characters.
	 */
	public static final BiFunction<Character, Character, Integer> OR = (character,
																		character2) -> character | character2;

	/**
	 * Bitwise NOT operation for a single character.
	 */
	public static final Function<Character, Number> NOT = (character) -> ~character;

	// Character-wise arithmetic operations
	/**
	 * Character addition (based on ASCII code points).
	 */
	public static final BiFunction<Character, Character, Integer> ADD = Integer::sum;

	/**
	 * Character subtraction.
	 */
	public static final BiFunction<Character, Character, Integer> SUBTRACT = Math::subtractExact;

	/**
	 * Character multiplication.
	 */
	public static final BiFunction<Character, Character, Integer> MUL = Math::multiplyExact;

	/**
	 * Character division.
	 */
	public static final BiFunction<Character, Character, Integer> DIV = Math::floorDiv;

	/**
	 * Character modulo.
	 */
	public static final BiFunction<Character, Character, Integer> MOD = Math::floorMod;

	/**
	 * Pre-increment a character (based on ASCII code).
	 */
	public static final Function<Character, Number> PRE_INCREMENT = character -> NumberUtils
			.preIncrement((int) character);

	/**
	 * Post-increment a character (based on ASCII code).
	 */
	public static final Function<Character, Number> POST_INCREMENT = character -> NumberUtils
			.postIncrement((int) character);

	/**
	 * Pre-decrement a character (based on ASCII code).
	 */
	public static final Function<Character, Number> PRE_DECREMENT = character -> NumberUtils
			.preDecrement((int) character);

	/**
	 * Post-decrement a character (based on ASCII code).
	 */
	public static final Function<Character, Number> POST_DECREMENT = character -> NumberUtils
			.postDecrement((int) character);

	/**
	 * Indicates whether vectorized operations should be used (controlled via JVM property).
	 */
	public static final Boolean USE_VECTOR_API = Boolean.getBoolean(VECTOR_API_PROPERTY);

	// Vector configuration
	/**
	 * Minimum string length for vectorized execution.
	 */
	public static final int VECTOR_THRESHOLD = 128; // TODO: tune the threshold

	// Bitwise vector operations
	/**
	 * Bitwise XOR for vectorized characters.
	 */
	public static final BiFunction<ShortVector, ShortVector, ShortVector> XOR_VEC = (v1, v2) -> v1
			.lanewise(VectorOperators.XOR, v2);

	/**
	 * Bitwise AND for vectorized characters.
	 */
	public static final BiFunction<ShortVector, ShortVector, ShortVector> AND_VEC = ShortVector::and;

	/**
	 * Bitwise OR for vectorized characters.
	 */
	public static final BiFunction<ShortVector, ShortVector, ShortVector> OR_VEC = ShortVector::or;

	/**
	 * Bitwise NOT for a vectorized character.
	 */
	public static final Function<ShortVector, ShortVector> NOT_VEC = ShortVector::not;

	// Arithmetic vector operations
	/**
	 * Vectorized addition of characters.
	 */
	public static final BiFunction<ShortVector, ShortVector, ShortVector> ADD_VEC = ShortVector::add;

	/**
	 * Vectorized subtraction of characters.
	 */
	public static final BiFunction<ShortVector, ShortVector, ShortVector> SUBTRACT_VEC = ShortVector::sub;

	/**
	 * Vectorized multiplication of characters.
	 */
	public static final BiFunction<ShortVector, ShortVector, ShortVector> MUL_VEC = ShortVector::mul;

	/**
	 * Vectorized division of characters.
	 */
	public static final BiFunction<ShortVector, ShortVector, ShortVector> DIV_VEC = ShortVector::div;

	/**
	 * Vectorized modulo operation (simulates {@code floorMod}).
	 */
	public static final BiFunction<ShortVector, ShortVector, ShortVector> MOD_VEC = (v1, v2) -> v1
			.sub(v1.div(v2).mul(v2));

	/**
	 * Vectorized pre-increment.
	 */
	public static final Function<ShortVector, ShortVector> PRE_INCREMENT_VEC = (v) -> v.add((short) 1);

	/**
	 * Vectorized pre-decrement.
	 */
	public static final Function<ShortVector, ShortVector> PRE_DECREMENT_VEC = (v) -> v.sub((short) 1);

	/**
	 * Mapping from scalar binary operations to vectorized equivalents.
	 */
	public static final Map<BiFunction<Character, Character, Integer>, BiFunction<ShortVector, ShortVector, ShortVector>> BINARY_OP_MAP = Map
			.of(
				XOR,
				XOR_VEC,
				AND,
				AND_VEC,
				OR,
				OR_VEC,
				ADD,
				ADD_VEC,
				SUBTRACT,
				SUBTRACT_VEC,
				MUL,
				MUL_VEC,
				DIV,
				DIV_VEC,
				MOD,
				MOD_VEC
			);

	/**
	 * Mapping from scalar unary operations to vectorized equivalents.
	 */
	public static final Map<Function<Character, Number>, Function<ShortVector, ShortVector>> UNARY_OP_MAP = Map
			.of(
				NOT,
				NOT_VEC,
				PRE_INCREMENT,
				PRE_INCREMENT_VEC,
				PRE_DECREMENT,
				PRE_DECREMENT_VEC
			);

	/**
	 * Preferred vector species for character-based operations.
	 */
	private static final VectorSpecies<Short> SPECIES = ShortVector.SPECIES_PREFERRED;

	/**
	 * Private constructor to prevent instantiation.
	 * Always throws a {@link NaftahBugError} when called.
	 */
	private StringUtils() {
		throw new NaftahBugError("استخدام غير مسموح به.");
	}


	/**
	 * Checks if two strings are equal.
	 *
	 * @param a the first string
	 * @param b the second string
	 * @return {@code true} if both strings are equal
	 */
	public static boolean equals(String a, String b) {
		if (a == null) {
			return b == null;
		}
		return a.equals(b);
	}


	/**
	 * Compares two strings lexicographically.
	 *
	 * @param a the first string
	 * @param b the second string
	 * @return a negative integer, zero, or a positive integer as {@code a} is less than, equal to, or greater than
	 *         {@code b}
	 */
	public static int compare(String a, String b) {
		return a == null ? (b == null ? 0 : -1) : (b == null ? 1 : a.compareTo(b));
	}


	/**
	 * Concatenates two strings.
	 *
	 * @param a the first string
	 * @param b the second string
	 * @return the concatenated string
	 */
	public static String add(String a, String b) {
		return (a == null && b == null) ? null : (a == null ? "" : a) + (b == null ? "" : b);
	}


	/**
	 * Removes all occurrences of {@code b} from {@code a}.
	 *
	 * @param a the original string
	 * @param b the substring to remove
	 * @return the resulting string
	 */
	public static String subtract(String a, String b) {
		return (a == null && b == null) ? null : (a == null ? null : a.replace((b == null ? "" : b), ""));
	}

	/**
	 * Splits a string using the given delimiter.
	 *
	 * @param a         the string to split
	 * @param delimiter the delimiter
	 * @return an array of substrings
	 */
	public static String[] divide(String a, String delimiter) {
		return (a == null) ? new String[]{} : ((delimiter == null) ? new String[]{a} : a.split(delimiter));
	}

	/**
	 * Splits a string into a specified number of parts.
	 *
	 * @param s     the string to split
	 * @param parts the number of parts
	 * @return an array containing the parts
	 * @throws NaftahBugError if the string is empty, or if {@code parts} is invalid
	 */
	public static String[] divide(String s, int parts) {
		if (s == null || s.isEmpty()) {
			throw newNaftahInvalidEmptyInputStringCannotBeEmptyBugError();
		}
		if (parts <= 0) {
			throw newNaftahPartsCountMustBeGreaterThanZeroBugError();
		}
		if (parts > s.length()) {
			throw newNaftahPartsCountExceedsStringLengthBugError();
		}

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

	/**
	 * Repeats the given string a specified number of times.
	 *
	 * @param s          the input string
	 * @param multiplier number of repetitions
	 * @return the repeated string
	 */
	public static String multiply(String s, int multiplier) {
		if (s == null) {
			throw newNaftahInvalidEmptyInputStringCannotBeEmptyBugError();
		}
		try {
			return s.repeat(multiplier);
		}
		catch (IllegalArgumentException | OutOfMemoryError throwable) {
			throw new NaftahBugError(throwable);
		}
	}

	/**
	 * Applies a binary character-wise operation to two strings.
	 * <p>
	 * - If either {@code a} or {@code b} is {@code null}, throws a
	 * {@link NaftahBugError} indicating invalid input.
	 * - If one string is empty, returns the other string as-is.
	 * - Otherwise, applies the provided character-wise operation. If vectorization
	 * is enabled and applicable, uses a vectorized implementation for performance.
	 *
	 * @param a         the first string (must not be {@code null})
	 * @param b         the second string (must not be {@code null})
	 * @param operation the character-wise binary operation (e.g. addition, subtraction)
	 * @return the resulting string after applying the operation
	 * @throws NaftahBugError if either {@code a} or {@code b} is {@code null}
	 */
	public static String applyOperation(String a,
										String b,
										BiFunction<Character, Character, Integer> operation) {
		if (a == null || b == null) {
			throw newNaftahInvalidEmptyInputStringCannotBeEmptyBugError();
		}

		try {
			BiFunction<ShortVector, ShortVector, ShortVector> vectorOperation;
			if (!USE_VECTOR_API || a.length() < VECTOR_THRESHOLD || Objects
					.isNull(vectorOperation = BINARY_OP_MAP.get(operation))) {
				return applyOperationScalar(a, b, operation);
			}
			else {
				return applyOperationVectorized(a, b, operation, vectorOperation);
			}
		}
		catch (ArithmeticException arithmeticException) {
			throw new NaftahBugError(arithmeticException);
		}
	}

	/**
	 * Applies a scalar binary character-wise operation to two strings.
	 *
	 * @param a         the first string
	 * @param b         the second string
	 * @param operation the binary operation
	 * @return the result string after applying the operation character-wise
	 */
	public static String applyOperationScalar(  String a,
												String b,
												BiFunction<Character, Character, Integer> operation) {
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

	/**
	 * Applies a vectorized binary operation to two strings using {@link ShortVector}.
	 *
	 * @param a               the first string
	 * @param b               the second string
	 * @param operation       the scalar operation (for fallback)
	 * @param vectorOperation the vectorized operation
	 * @return the resulting string after applying the vectorized operation
	 */
	public static String applyOperationVectorized(  String a,
													String b,
													BiFunction<Character, Character, Integer> operation,
													BiFunction<ShortVector, ShortVector, ShortVector> vectorOperation) {
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

	/**
	 * Applies a scalar binary operation on two characters.
	 *
	 * @param aChar     the first character
	 * @param bChar     the second character
	 * @param operation the binary operation
	 * @return the resulting character after applying the operation
	 */
	public static char doApplyOperation(char aChar, char bChar, BiFunction<Character, Character, Integer> operation) {
		int intResult = operation.apply(aChar, bChar);
		// Cast result to char (lower 16 bits)
		return (char) intResult;
	}

	/**
	 * Performs character-wise addition between two strings.
	 *
	 * @param a the first string
	 * @param b the second string
	 * @return the resulting string
	 */
	public static String charWiseAdd(String a, String b) {
		return applyOperation(a, b, ADD);
	}

	/**
	 * Performs character-wise subtraction between two strings.
	 *
	 * @param a the first string
	 * @param b the second string
	 * @return the resulting string
	 */
	public static String charWiseSubtract(String a, String b) {
		return applyOperation(a, b, SUBTRACT);
	}

	/**
	 * Performs character-wise multiplication between two strings.
	 *
	 * @param a the first string
	 * @param b the second string
	 * @return the resulting string
	 */
	public static String charWiseMultiply(String a, String b) {
		return applyOperation(a, b, MUL);
	}

	/**
	 * Performs character-wise division between two strings.
	 *
	 * @param a the first string
	 * @param b the second string
	 * @return the resulting string
	 */
	public static String charWiseDivide(String a, String b) {
		return applyOperation(a, b, DIV);
	}

	/**
	 * Performs character-wise modulo between two strings.
	 *
	 * @param a the first string
	 * @param b the second string
	 * @return the resulting string
	 */
	public static String charWiseModulo(String a, String b) {
		return applyOperation(a, b, MOD);
	}

	/**
	 * Performs bitwise XOR between characters in two strings.
	 *
	 * @param a the first string
	 * @param b the second string
	 * @return the resulting string
	 */
	public static String xor(String a, String b) {
		return applyOperation(a, b, XOR);
	}

	/**
	 * Performs bitwise AND between characters in two strings.
	 *
	 * @param a the first string
	 * @param b the second string
	 * @return the resulting string
	 */
	public static String and(String a, String b) {
		return applyOperation(a, b, AND);
	}

	/**
	 * Performs bitwise OR between characters in two strings.
	 *
	 * @param a the first string
	 * @param b the second string
	 * @return the resulting string
	 */
	public static String or(String a, String b) {
		return applyOperation(a, b, OR);
	}

	/**
	 * Applies a unary character-wise operation to a string.
	 *
	 * @param a         the input string
	 * @param operation the unary character operation
	 * @return the resulting string
	 */
	public static String applyOperation(String a, Function<Character, Number> operation) {
		if (a == null) {
			throw newNaftahInvalidEmptyInputStringCannotBeEmptyBugError();
		}
		if (a.isEmpty()) {
			return a;
		}
		Function<ShortVector, ShortVector> vectorOperation;
		if (!USE_VECTOR_API || a.length() < VECTOR_THRESHOLD || Objects
				.isNull(vectorOperation = UNARY_OP_MAP.get(operation))) {
			return applyOperationScalar(a, operation);
		}
		else {
			return applyOperationVectorized(a, operation, vectorOperation);
		}
	}

	/**
	 * Applies a scalar unary character-wise operation to a string.
	 *
	 * @param a         the input string
	 * @param operation the unary character operation
	 * @return the resulting string
	 */
	public static String applyOperationScalar(String a, Function<Character, Number> operation) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < a.length(); i++) {
			result.append(doApplyOperation(a.charAt(i), operation));
		}
		return result.toString();
	}

	/**
	 * Applies a vectorized unary operation using {@link ShortVector}.
	 *
	 * @param input           the input string
	 * @param scalarOperation the scalar fallback operation
	 * @param vectorOperation the vector operation to apply
	 * @return the resulting string
	 */
	public static String applyOperationVectorized(  String input,
													Function<Character, Number> scalarOperation,
													Function<ShortVector, ShortVector> vectorOperation) {
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

	/**
	 * Applies a unary operation on a single character.
	 *
	 * @param aChar     the input character
	 * @param operation the unary character operation
	 * @return the resulting character
	 */
	public static char doApplyOperation(char aChar, Function<Character, Number> operation) {
		int intResult = operation.apply(aChar).intValue();
		// Cast result to char (lower 16 bits)
		return (char) intResult;
	}

	/**
	 * Performs bitwise NOT operation on each character of the string.
	 *
	 * @param a the input string
	 * @return the resulting string
	 */
	public static String not(String a) {
		return applyOperation(a, NOT);
	}

	/**
	 * Pre-increments each character in the string.
	 *
	 * @param a the input string
	 * @return the resulting string
	 */
	public static String preIncrement(String a) {
		return applyOperation(a, PRE_INCREMENT);
	}

	/**
	 * Post-increments each character in the string.
	 *
	 * @param a the input string
	 * @return the resulting string
	 */
	public static String postIncrement(String a) {
		return applyOperation(a, POST_INCREMENT);
	}

	/**
	 * Pre-decrements each character in the string.
	 *
	 * @param a the input string
	 * @return the resulting string
	 */
	public static String preDecrement(String a) {
		return applyOperation(a, PRE_DECREMENT);
	}

	/**
	 * Post-decrements each character in the string.
	 *
	 * @param a the input string
	 * @return the resulting string
	 */
	public static String postDecrement(String a) {
		return applyOperation(a, POST_DECREMENT);
	}

	/**
	 * Converts a string into an integer by summing its Unicode code points.
	 *
	 * @param s the input string
	 * @return the sum of the code points
	 */
	public static int stringToInt(String s) {
		if (s == null) {
			throw newNaftahInvalidEmptyInputStringCannotBeEmptyBugError();
		}
		if (s.isEmpty()) {
			return 0;
		}
		return s.codePoints().sum();
	}


	/**
	 * Creates a {@link NaftahBugError} indicating that the input string was null or empty.
	 *
	 * @return a new {@code NaftahBugError} with a message explaining the invalid input.
	 */
	public static NaftahBugError newNaftahInvalidEmptyInputStringCannotBeEmptyBugError() {
		return new NaftahBugError("النص لا يمكن أن يكون فارغًا.");
	}

	/**
	 * Creates a {@link NaftahBugError} indicating that the number of parts is zero or negative.
	 *
	 * @return a new {@code NaftahBugError} with a message explaining the invalid part count.
	 */
	public static NaftahBugError newNaftahPartsCountMustBeGreaterThanZeroBugError() {
		return new NaftahBugError("يجب أن يكون عدد الأجزاء أكبر من 0.");
	}

	/**
	 * Creates a {@link NaftahBugError} indicating that the number of parts exceeds the string length.
	 *
	 * @return a new {@code NaftahBugError} with a message explaining the constraint violation.
	 */
	public static NaftahBugError newNaftahPartsCountExceedsStringLengthBugError() {
		return new NaftahBugError("عدد الأجزاء لا يمكن أن يتجاوز طول السلسلة.");
	}
}
