package org.daiitech.naftah.builtin.utils;

import java.util.function.Supplier;
import java.util.stream.Stream;

import org.daiitech.naftah.errors.NaftahBugError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import static org.daiitech.naftah.Naftah.VECTOR_API_PROPERTY;
import static org.daiitech.naftah.TestUtils.assertEquals;
import static org.daiitech.naftah.TestUtils.doAssertBugEquals;

public class StringUtilsTests {

	static Stream<Arguments> divideProvider() {
		return Stream
				.of(
					Arguments.of(true, "مرحبا", 1, new String[]{"مرحبا"}, null),
					Arguments.of(true, "مرحبا", 5, new String[]{"م", "ر", "ح", "ب", "ا"}, null),
					Arguments.of(true, "abcdef", 3, new String[]{"ab", "cd", "ef"}, null),
					Arguments.of(true, "abcdefg", 3, new String[]{"abc", "de", "fg"}, null),
					Arguments.of(true, "مرحبا بيك", 2, new String[]{"مرحبا", " بيك"}, null),
					Arguments.of(true, "أهلاً وسهلاً", 1, new String[]{"أهلاً وسهلاً"}, null),
					Arguments.of(true, "صباح الخير", 2, new String[]{"صباح ", "الخير"}, null),
					Arguments.of(true, "123456789", 4, new String[]{"123", "45", "67", "89"}, null),
					Arguments
							.of(false,
								null,
								3,
								null,
								StringUtils.newNaftahInvalidEmptyInputStringCannotBeEmptyBugError()),
					Arguments
							.of(false,
								"",
								2,
								null,
								StringUtils.newNaftahInvalidEmptyInputStringCannotBeEmptyBugError()),
					Arguments
							.of(false,
								"مرحبا",
								0,
								null,
								StringUtils.newNaftahPartsCountMustBeGreaterThanZeroBugError()),
					Arguments
							.of(false,
								"مرحبا",
								-1,
								null,
								StringUtils.newNaftahPartsCountMustBeGreaterThanZeroBugError()),
					Arguments
							.of(false,
								"سلام",
								10,
								null,
								StringUtils.newNaftahPartsCountExceedsStringLengthBugError())
				);
	}

	static Stream<Arguments> multiplyProvider() {
		return Stream
				.of(
					Arguments.of(true, "أهلاً", 3, "أهلاًأهلاًأهلاً", null, null),
					Arguments.of(true, "a", 5, "aaaaa", null, null),
					Arguments.of(true, "🙂", 2, "🙂🙂", null, null),
					Arguments.of(true, "", 1000, "", null, null),
					Arguments.of(true, "", 0, "", null, null),
					Arguments.of(true, "مرحبا", 0, "", null, null),
					Arguments.of(true, "مرحبا", 1, "مرحبا", null, null),
					Arguments
							.of(false,
								null,
								3,
								null,
								StringUtils.newNaftahInvalidEmptyInputStringCannotBeEmptyBugError()),
					Arguments
							.of(false,
								"abc",
								-2,
								null,
								new NaftahBugError(new IllegalArgumentException("count is negative: -2"))),
					Arguments
							.of(false,
								"Hello",
								Integer.MAX_VALUE,
								null,
								new NaftahBugError(new OutOfMemoryError(
																		"Required length exceeds implementation limit")))
				);
	}

	static Stream<Arguments> charWiseAddProvider() {
		return Stream
				.of(
					Arguments
							.of(true,
								"ABC",
								"abc",
								"" + (char) ('A' + 'a') + (char) ('B' + 'b') + (char) ('C' + 'c'),
								null),
					Arguments
							.of(true,
								"مرحبا",
								"123",
								"" + (char) ('م' + '1') + (char) ('ر' + '2') + (char) ('ح' + '3'),
								null),
					Arguments.of(true, "🙂🚀", "😀🌟", "끺뱂끹붟", null),
					Arguments.of(true, "", "test", "", null),
					Arguments.of(true, "test", "", "", null),
					Arguments.of(true, "", "", "", null),
					Arguments
							.of(true,
								"أبج",
								"XYZ",
								"" + (char) ('أ' + 'X') + (char) ('ب' + 'Y') + (char) ('ج' + 'Z'),
								null),
					Arguments
							.of(false,
								null,
								"abc",
								null,
								StringUtils.newNaftahInvalidEmptyInputStringCannotBeEmptyBugError()),
					Arguments
							.of(false,
								"abc",
								null,
								null,
								StringUtils.newNaftahInvalidEmptyInputStringCannotBeEmptyBugError()),
					Arguments
							.of(false,
								null,
								null,
								null,
								StringUtils.newNaftahInvalidEmptyInputStringCannotBeEmptyBugError())
				);
	}

	static Stream<Arguments> charWiseSubtractProvider() {
		return Stream
				.of(
					Arguments
							.of(true,
								"ABC",
								"abc",
								"" + (char) Math.subtractExact('A', 'a') + (char) Math
										.subtractExact( 'B',
														'b') + (char) Math
																.subtractExact(
																				'C',
																				'c'),
								null),
					Arguments
							.of(true,
								"مرحبا",
								"123",
								"" + (char) Math.subtractExact('م', '1') + (char) Math
										.subtractExact( 'ر',
														'2') + (char) Math
																.subtractExact(
																				'ح',
																				'3'),
								null),
					Arguments.of(true, "", "test", "", null),
					Arguments.of(true, "test", "", "", null),
					Arguments.of(true, "", "", "", null),
					Arguments
							.of(true,
								"أبج",
								"XYZ",
								"" + (char) Math.subtractExact('أ', 'X') + (char) Math
										.subtractExact( 'ب',
														'Y') + (char) Math
																.subtractExact(
																				'ج',
																				'Z'),
								null),
					Arguments
							.of(false,
								null,
								"abc",
								null,
								StringUtils.newNaftahInvalidEmptyInputStringCannotBeEmptyBugError()),
					Arguments
							.of(false,
								"abc",
								null,
								null,
								StringUtils.newNaftahInvalidEmptyInputStringCannotBeEmptyBugError()),
					Arguments
							.of(false,
								null,
								null,
								null,
								StringUtils.newNaftahInvalidEmptyInputStringCannotBeEmptyBugError())
				);
	}

	static Stream<Arguments> charWiseMultiplyProvider() {
		return Stream
				.of(
					Arguments
							.of(true,
								"ABC",
								"abc",
								"" + (char) Math.multiplyExact('A', 'a') + (char) Math
										.multiplyExact('B', 'b') + (char) Math.multiplyExact('C', 'c'),
								null),

					Arguments
							.of(true,
								"مرحبا",
								"123",
								"" + (char) Math.multiplyExact('م', '1') + (char) Math
										.multiplyExact('ر', '2') + (char) Math.multiplyExact('ح', '3'),
								null),

					Arguments.of(true, "", "test", "", null),
					Arguments.of(true, "test", "", "", null),
					Arguments.of(true, "", "", "", null),

					Arguments
							.of(true,
								"أبج",
								"XYZ",
								"" + (char) Math.multiplyExact('أ', 'X') + (char) Math
										.multiplyExact('ب', 'Y') + (char) Math.multiplyExact('ج', 'Z'),
								null),

					Arguments
							.of(false,
								null,
								"abc",
								null,
								StringUtils.newNaftahInvalidEmptyInputStringCannotBeEmptyBugError()),

					Arguments
							.of(false,
								"abc",
								null,
								null,
								StringUtils.newNaftahInvalidEmptyInputStringCannotBeEmptyBugError()),

					Arguments
							.of(false,
								null,
								null,
								null,
								StringUtils.newNaftahInvalidEmptyInputStringCannotBeEmptyBugError())
				);
	}

	static Stream<Arguments> charWiseDivideProvider() {
		return Stream
				.of(
					Arguments
							.of(true,
								"ABC",
								"abc",
								"" + (char) Math.floorDiv('A', 'a') + (char) Math.floorDiv('B', 'b') + (char) Math
										.floorDiv('C', 'c'),
								null),
					Arguments
							.of(true,
								"مرح",
								"123",
								"" + (char) Math.floorDiv('م', '1') + (char) Math.floorDiv('ر', '2') + (char) Math
										.floorDiv('ح', '3'),
								null),
					Arguments.of(true, "", "test", "", null),
					Arguments.of(true, "test", "", "", null),
					Arguments.of(true, "", "", "", null),
					Arguments
							.of(true,
								"أبج",
								"XYZ",
								"" + (char) Math.floorDiv('أ', 'X') + (char) Math.floorDiv('ب', 'Y') + (char) Math
										.floorDiv('ج', 'Z'),
								null),
					Arguments
							.of(false,
								null,
								"abc",
								null,
								StringUtils.newNaftahInvalidEmptyInputStringCannotBeEmptyBugError()),
					Arguments
							.of(false,
								"abc",
								null,
								null,
								StringUtils.newNaftahInvalidEmptyInputStringCannotBeEmptyBugError()),
					Arguments
							.of(false,
								null,
								null,
								null,
								StringUtils.newNaftahInvalidEmptyInputStringCannotBeEmptyBugError()),
					Arguments
							.of(false,
								"ABC",
								"\u0000bc",
								null,
								new NaftahBugError(new ArithmeticException("/ by zero")))
				);
	}

	static Stream<Arguments> charWiseModuloProvider() {
		return Stream
				.of(
					// Normal ASCII
					Arguments
							.of(true,
								"ABC",
								"abc",
								"" + (char) Math.floorMod('A', 'a') + (char) Math.floorMod('B', 'b') + (char) Math
										.floorMod('C', 'c'),
								null),

					// Unicode with Arabic characters
					Arguments
							.of(true,
								"مرح",
								"123",
								"" + (char) Math.floorMod('م', '1') + (char) Math.floorMod('ر', '2') + (char) Math
										.floorMod('ح', '3'),
								null),

					// Empty cases
					Arguments.of(true, "", "test", "", null),
					Arguments.of(true, "test", "", "", null),
					Arguments.of(true, "", "", "", null),

					// Mixed Unicode + Latin
					Arguments
							.of(true,
								"أبج",
								"XYZ",
								"" + (char) Math.floorMod('أ', 'X') + (char) Math.floorMod('ب', 'Y') + (char) Math
										.floorMod('ج', 'Z'),
								null),

					// Invalid: null inputs
					Arguments
							.of(false,
								null,
								"abc",
								null,
								StringUtils.newNaftahInvalidEmptyInputStringCannotBeEmptyBugError()),
					Arguments
							.of(false,
								"abc",
								null,
								null,
								StringUtils.newNaftahInvalidEmptyInputStringCannotBeEmptyBugError()),
					Arguments
							.of(false,
								null,
								null,
								null,
								StringUtils.newNaftahInvalidEmptyInputStringCannotBeEmptyBugError()),

					// Modulo by zero (invalid operation)
					Arguments
							.of(false,
								"ABC",
								"\u0000bc",
								null,
								new NaftahBugError(new ArithmeticException("/ by zero")))
				);
	}

	static Stream<Arguments> xorProvider() {
		return Stream
				.of(
					// Normal ASCII
					Arguments
							.of(true,
								"ABC",
								"abc",
								"" + (char) ('A' ^ 'a') + (char) ('B' ^ 'b') + (char) ('C' ^ 'c'),
								null),

					// Unicode (Arabic)
					Arguments
							.of(true,
								"مرح",
								"123",
								"" + (char) ('م' ^ '1') + (char) ('ر' ^ '2') + (char) ('ح' ^ '3'),
								null),

					// Empty input cases
					Arguments.of(true, "", "test", "", null),
					Arguments.of(true, "test", "", "", null),
					Arguments.of(true, "", "", "", null),

					// Mixed Arabic + Latin
					Arguments
							.of(true,
								"أبج",
								"XYZ",
								"" + (char) ('أ' ^ 'X') + (char) ('ب' ^ 'Y') + (char) ('ج' ^ 'Z'),
								null),

					// Null inputs — should trigger NaftahBugError
					Arguments
							.of(false,
								null,
								"abc",
								null,
								StringUtils.newNaftahInvalidEmptyInputStringCannotBeEmptyBugError()),
					Arguments
							.of(false,
								"abc",
								null,
								null,
								StringUtils.newNaftahInvalidEmptyInputStringCannotBeEmptyBugError()),
					Arguments
							.of(false,
								null,
								null,
								null,
								StringUtils.newNaftahInvalidEmptyInputStringCannotBeEmptyBugError())
				);
	}

	static Stream<Arguments> orProvider() {
		return Stream
				.of(
					Arguments
							.of(true,
								"ABC",
								"abc",
								"" + (char) ('A' | 'a') + (char) ('B' | 'b') + (char) ('C' | 'c'),
								null),
					Arguments
							.of(true,
								"مرح",
								"123",
								"" + (char) ('م' | '1') + (char) ('ر' | '2') + (char) ('ح' | '3'),
								null),
					Arguments.of(true, "", "test", "", null),
					Arguments.of(true, "test", "", "", null),
					Arguments.of(true, "", "", "", null),
					Arguments
							.of(true,
								"أبج",
								"XYZ",
								"" + (char) ('أ' | 'X') + (char) ('ب' | 'Y') + (char) ('ج' | 'Z'),
								null),
					Arguments
							.of(false,
								null,
								"abc",
								null,
								StringUtils.newNaftahInvalidEmptyInputStringCannotBeEmptyBugError()),
					Arguments
							.of(false,
								"abc",
								null,
								null,
								StringUtils.newNaftahInvalidEmptyInputStringCannotBeEmptyBugError()),
					Arguments
							.of(false,
								null,
								null,
								null,
								StringUtils.newNaftahInvalidEmptyInputStringCannotBeEmptyBugError())
				);
	}

	static Stream<Arguments> andProvider() {
		return Stream
				.of(
					// Normal ASCII
					Arguments
							.of(true,
								"ABC",
								"abc",
								"" + (char) ('A' & 'a') + (char) ('B' & 'b') + (char) ('C' & 'c'),
								null),

					// Unicode (Arabic)
					Arguments
							.of(true,
								"مرح",
								"123",
								"" + (char) ('م' & '1') + (char) ('ر' & '2') + (char) ('ح' & '3'),
								null),

					// Empty strings
					Arguments.of(true, "", "test", "", null),
					Arguments.of(true, "test", "", "", null),
					Arguments.of(true, "", "", "", null),

					// Mixed Arabic + Latin
					Arguments
							.of(true,
								"أبج",
								"XYZ",
								"" + (char) ('أ' & 'X') + (char) ('ب' & 'Y') + (char) ('ج' & 'Z'),
								null),

					// Null input cases (invalid)
					Arguments
							.of(false,
								null,
								"abc",
								null,
								StringUtils.newNaftahInvalidEmptyInputStringCannotBeEmptyBugError()),
					Arguments
							.of(false,
								"abc",
								null,
								null,
								StringUtils.newNaftahInvalidEmptyInputStringCannotBeEmptyBugError()),
					Arguments
							.of(false,
								null,
								null,
								null,
								StringUtils.newNaftahInvalidEmptyInputStringCannotBeEmptyBugError())
				);
	}

	static Stream<Arguments> preIncrementProvider() {
		return Stream
				.of(
					Arguments.of(true, "abc", "bcd", null),
					Arguments.of(true, "ABC", "BCD", null),
					Arguments.of(true, "123", "234", null),
					Arguments.of(true, "!@#", "\"A$", null),
					Arguments.of(true, "azAZ09", "b{B[1:", null),
					Arguments.of(true, "", "", null),
					Arguments.of(true, "\u0000", "\u0001", null),
					Arguments.of(true, "\uffff", "\u0000", null),
					Arguments
							.of(false,
								null,
								null,
								StringUtils.newNaftahInvalidEmptyInputStringCannotBeEmptyBugError())
				);
	}

	static Stream<Arguments> postIncrementDecrementProvider() {
		return Stream
				.of(
					Arguments.of(true, "abc", "abc", null),
					Arguments.of(true, "ABC", "ABC", null),
					Arguments.of(true, "123", "123", null),
					Arguments.of(true, "!@#", "!@#", null),
					Arguments.of(true, "azAZ09", "azAZ09", null),
					Arguments.of(true, "", "", null),
					Arguments.of(true, "\u0000", "\u0000", null),
					Arguments.of(true, "\uffff", "\uffff", null),
					Arguments
							.of(false,
								null,
								null,
								StringUtils.newNaftahInvalidEmptyInputStringCannotBeEmptyBugError())
				);
	}

	static Stream<Arguments> preDecrementProvider() {
		return Stream
				.of(
					Arguments.of(true, "bcd", "abc", null),
					Arguments.of(true, "BCD", "ABC", null),
					Arguments.of(true, "234", "123", null),
					Arguments.of(true, "\"A$", "!@#", null),
					Arguments.of(true, "b{B[1:", "azAZ09", null),
					Arguments.of(true, "", "", null),
					Arguments.of(true, "\u0001", "\u0000", null),
					Arguments.of(true, "\u0000", "\uffff", null),
					Arguments
							.of(false,
								null,
								null,
								StringUtils.newNaftahInvalidEmptyInputStringCannotBeEmptyBugError())
				);
	}


	static void runTest(boolean vectorCapable,
						boolean valid,
						Supplier<?> resultSupplier,
						Object expectedResult,
						NaftahBugError expectedNaftahBugError) {
		if (valid) {
			var result = resultSupplier.get();
			assertEquals(result, expectedResult);
			if (vectorCapable) {
				System.setProperty(VECTOR_API_PROPERTY, Boolean.toString(true));
				result = resultSupplier.get();
				assertEquals(result, expectedResult);

				System.setProperty(VECTOR_API_PROPERTY, Boolean.toString(false));
			}
		}
		else {
			NaftahBugError naftahBugError = Assertions
					.assertThrows(  NaftahBugError.class,
									resultSupplier::get);
			doAssertBugEquals(naftahBugError, expectedNaftahBugError);
		}
	}

	static Stream<Arguments> stringToIntProvider() {
		return Stream
				.of(
					Arguments.of(true, "abc", 'a' + 'b' + 'c', null),
					Arguments.of(true, "ABC", 'A' + 'B' + 'C', null),
					Arguments.of(true, "123", '1' + '2' + '3', null),
					Arguments.of(true, "!@#", '!' + '@' + '#', null),
					Arguments.of(true, "🙂", 0x1F642, null),
					Arguments.of(true, "a🙂b", 'a' + 0x1F642 + 'b', null),

					Arguments.of(true, "", 0, null),
					Arguments.of(true, "\u0000", 0, null),
					Arguments.of(true, "\uffff", 0xFFFF, null),

					Arguments
							.of(false,
								null,
								0,
								StringUtils.newNaftahInvalidEmptyInputStringCannotBeEmptyBugError())
				);
	}

	@ParameterizedTest
	@CsvSource(value = {
						"'مرحبا', 'مرحبا', true",
						"'سلام', 'سلام', true",
						"'مرحبا', 'سلام', false",
						"null, 'سلام', false",
						"'سلام', null, false",
						"null, null, true"
	}, nullValues = {"null"})
	void equals(String left, String right, boolean expectedResult) {
		boolean result = StringUtils.equals(left, right);
		assertEquals(result, expectedResult);
	}

	@ParameterizedTest
	@CsvSource(value = {
						"'مرحبا', 'مرحبا', 0",
						"'سلام', 'سلام', 0",
						"'مرحبا', 'سلام', 18",
						"null, 'سلام', -1",
						"'سلام', null, 1",
						"null, null, 0"
	}, nullValues = {"null"})
	void compare(String left, String right, int expectedResult) {
		int result = StringUtils.compare(left, right);
		assertEquals(result, expectedResult);
	}

	@ParameterizedTest
	@CsvSource(value = {
						"'مرحبا', 'بالعالم', 'مرحبابالعالم'",
						"'سلام', ' عليكم', 'سلام عليكم'",
						"'', 'أهلاً', 'أهلاً'",
						"'صباح', '', 'صباح'",
						"null, 'مساء', 'مساء'",
						"'نهار', null, 'نهار'",
						"'مرحبا', null, 'مرحبا'",
						"null, 'مرحبا', 'مرحبا'",
						"null, null, null"
	}, nullValues = {"null"})
	void add(String left, String right, String expectedResult) {
		String result = StringUtils.add(left, right);
		assertEquals(result, expectedResult);
	}

	@ParameterizedTest
	@CsvSource(value = {
						"'مرحبا بالعالم', 'بالعالم', 'مرحبا '",
						"'سلام عليكم', 'عليكم', 'سلام '",
						"'أهلاً وسهلاً', 'اً', 'أهل وسهل'",
						"'نهار جميل', 'جميل', 'نهار '",
						"'صباح الخير', 'مساء', 'صباح الخير'",
						"'مرحبا', null, 'مرحبا'",
						"null, 'عالم', null",
						"null, null, null",
						"'أهلاً بالعالم', '', 'أهلاً بالعالم'",
						"'', 'شيء', ''"
	}, nullValues = {"null"})
	void subtract(String left, String right, String expectedResult) {
		String result = StringUtils.subtract(left, right);
		assertEquals(result, expectedResult);
	}

	@ParameterizedTest
	@CsvSource(value = {
						"null, null, null",
						"null, '-', null",
						"'مرحبا', null, 'مرحبا'",
						"'', '-', ''",
						"'مرحبا', ' ', 'مرحبا'",
						"'مرحبا-بالعالم', '-', 'مرحبا|بالعالم'",
						"'أهلا-وسهلا-بكم', '-', 'أهلا|وسهلا|بكم'",
						"'أهلاً،وسهلاً،بكم', '،', 'أهلاً|وسهلاً|بكم'",
						"'-سلام', '-', '|سلام'",
						"'سلام-', '-', 'سلام|'",
						"'مرحبا--بالعالم', '-', 'مرحبا||بالعالم'",
						"'واحد.اثنان.ثلاثة', '\\.', 'واحد|اثنان|ثلاثة'",
						"'صباح الخير للجميع', ' ', 'صباح|الخير|للجميع'",
						"'صباح-الخير،مساء-النور', '-', 'صباح|الخير،مساء|النور'"
	}, nullValues = {"null"})
	void divideByString(String left, String right, String expectedJoinedResult) {
		String[] expectedResult = expectedJoinedResult == null ?
				new String[]{} :
				(expectedJoinedResult.isEmpty() ?
						new String[]{""} :
						expectedJoinedResult
								.split(
										"\\|"));
		String[] result = StringUtils.divide(left, right);
		assertEquals(result, expectedResult);
	}

	@ParameterizedTest
	@MethodSource("divideProvider")
	void divideByParts( boolean valid,
						String string,
						int parts,
						String[] expectedResult,
						NaftahBugError expectedNaftahBugError) {
		runTest(false, valid, () -> StringUtils.divide(string, parts), expectedResult, expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("multiplyProvider")
	void multiply(  boolean valid,
					String string,
					int multiplier,
					String expectedResult,
					NaftahBugError expectedNaftahBugError) {
		runTest(false, valid, () -> StringUtils.multiply(string, multiplier), expectedResult, expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("charWiseAddProvider")
	void charWiseAdd(   boolean valid,
						String left,
						String right,
						String expectedResult,
						NaftahBugError expectedNaftahBugError) {
		runTest(true, valid, () -> StringUtils.charWiseAdd(left, right), expectedResult, expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("charWiseSubtractProvider")
	void charWiseSubtract(  boolean valid,
							String left,
							String right,
							String expectedResult,
							NaftahBugError expectedNaftahBugError) {
		runTest(true, valid, () -> StringUtils.charWiseSubtract(left, right), expectedResult, expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("charWiseMultiplyProvider")
	void charWiseMultiply(  boolean valid,
							String left,
							String right,
							String expectedResult,
							NaftahBugError expectedNaftahBugError) {
		runTest(true, valid, () -> StringUtils.charWiseMultiply(left, right), expectedResult, expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("charWiseDivideProvider")
	void charWiseDivide(boolean valid,
						String left,
						String right,
						String expectedResult,
						NaftahBugError expectedNaftahBugError) {
		runTest(true, valid, () -> StringUtils.charWiseDivide(left, right), expectedResult, expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("charWiseModuloProvider")
	void charWiseModulo(boolean valid,
						String left,
						String right,
						String expectedResult,
						NaftahBugError expectedNaftahBugError) {
		runTest(true, valid, () -> StringUtils.charWiseModulo(left, right), expectedResult, expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("xorProvider")
	void xor(   boolean valid,
				String left,
				String right,
				String expectedResult,
				NaftahBugError expectedNaftahBugError) {
		runTest(true, valid, () -> StringUtils.xor(left, right), expectedResult, expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("andProvider")
	void and(   boolean valid,
				String left,
				String right,
				String expectedResult,
				NaftahBugError expectedNaftahBugError) {
		runTest(true, valid, () -> StringUtils.and(left, right), expectedResult, expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("orProvider")
	void or(boolean valid,
			String left,
			String right,
			String expectedResult,
			NaftahBugError expectedNaftahBugError) {
		runTest(true, valid, () -> StringUtils.or(left, right), expectedResult, expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("preIncrementProvider")
	void preIncrement(  boolean valid,
						String a,
						String expectedResult,
						NaftahBugError expectedNaftahBugError) {
		runTest(true, valid, () -> StringUtils.preIncrement(a), expectedResult, expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("postIncrementDecrementProvider")
	void postIncrement( boolean valid,
						String a,
						String expectedResult,
						NaftahBugError expectedNaftahBugError) {
		runTest(true, valid, () -> StringUtils.postIncrement(a), expectedResult, expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("preDecrementProvider")
	void preDecrement(  boolean valid,
						String a,
						String expectedResult,
						NaftahBugError expectedNaftahBugError) {
		runTest(true, valid, () -> StringUtils.preDecrement(a), expectedResult, expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("postIncrementDecrementProvider")
	void postDecrement( boolean valid,
						String a,
						String expectedResult,
						NaftahBugError expectedNaftahBugError) {
		runTest(true, valid, () -> StringUtils.postDecrement(a), expectedResult, expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("stringToIntProvider")
	void stringToInt(   boolean valid,
						String a,
						int expectedResult,
						NaftahBugError expectedNaftahBugError) {
		runTest(true, valid, () -> StringUtils.stringToInt(a), expectedResult, expectedNaftahBugError);
	}

}
