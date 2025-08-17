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
						Arguments.of(false,
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
		return Stream.of(
				Arguments.of(true, "ABC", "abc",
							 "" + (char) ('A' + 'a') + (char) ('B' + 'b') + (char) ('C' + 'c'), null),
				Arguments.of(true, "مرحبا", "123",
							 "" + (char) ('م' + '1') + (char) ('ر' + '2') + (char) ('ح' + '3'), null),
				Arguments.of(true, "🙂🚀", "😀🌟", "끺뱂끹붟", null),
				Arguments.of(true, "", "test", "", null),
				Arguments.of(true, "test", "", "", null),
				Arguments.of(true, "", "", "", null),
				Arguments.of(true,
							 "أبج",
							 "XYZ",
							 "" + (char) ('أ' + 'X') + (char) ('ب' + 'Y') + (char) ('ج' + 'Z'),
							 null),
				Arguments.of(false, null, "abc", null,
							 StringUtils.newNaftahInvalidEmptyInputStringCannotBeEmptyBugError()),
				Arguments.of(false,
							 "abc",
							 null,
							 null,
							 StringUtils.newNaftahInvalidEmptyInputStringCannotBeEmptyBugError()),
				Arguments.of(false,
							 null,
							 null,
							 null,
							 StringUtils.newNaftahInvalidEmptyInputStringCannotBeEmptyBugError())
		);
	}

	static Stream<Arguments> charWiseSubtractProvider() {
		return Stream.of(
				Arguments.of(true,
							 "ABC",
							 "abc",
							 "" + (char) Math.subtractExact('A', 'a') + (char) Math.subtractExact('B',
																								  'b') + (char) Math.subtractExact(
									 'C',
									 'c'),
							 null),
				Arguments.of(true, "مرحبا", "123",
							 "" + (char) Math.subtractExact('م', '1') + (char) Math.subtractExact('ر',
																								  '2') + (char) Math.subtractExact(
									 'ح',
									 '3'), null),
				Arguments.of(true, "", "test", "", null),
				Arguments.of(true, "test", "", "", null),
				Arguments.of(true, "", "", "", null),
				Arguments.of(true,
							 "أبج",
							 "XYZ",
							 "" + (char) Math.subtractExact('أ', 'X') + (char) Math.subtractExact('ب',
																								  'Y') + (char) Math.subtractExact(
									 'ج',
									 'Z'),
							 null),
				Arguments.of(false, null, "abc", null,
							 StringUtils.newNaftahInvalidEmptyInputStringCannotBeEmptyBugError()),
				Arguments.of(false,
							 "abc",
							 null,
							 null,
							 StringUtils.newNaftahInvalidEmptyInputStringCannotBeEmptyBugError()),
				Arguments.of(false,
							 null,
							 null,
							 null,
							 StringUtils.newNaftahInvalidEmptyInputStringCannotBeEmptyBugError())
		);
	}

	private static void runTest(boolean vectorCapable, boolean valid,
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
					.assertThrows(NaftahBugError.class,
								  resultSupplier::get);
			doAssertBugEquals(naftahBugError, expectedNaftahBugError);
		}
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
	void divideByParts(boolean valid,
					   String string,
					   int parts,
					   String[] expectedResult,
					   NaftahBugError expectedNaftahBugError) {
		runTest(false, valid, () -> StringUtils.divide(string, parts), expectedResult, expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("multiplyProvider")
	void multiply(boolean valid,
				  String string,
				  int multiplier,
				  String expectedResult,
				  NaftahBugError expectedNaftahBugError) {
		runTest(false, valid, () -> StringUtils.multiply(string, multiplier), expectedResult, expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("charWiseAddProvider")
	void charWiseAdd(boolean valid,
					 String left,
					 String right,
					 String expectedResult,
					 NaftahBugError expectedNaftahBugError) {
		runTest(true, valid, () -> StringUtils.charWiseAdd(left, right), expectedResult, expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("charWiseSubtractProvider")
	void charWiseSubtract(boolean valid,
						  String left,
						  String right,
						  String expectedResult,
						  NaftahBugError expectedNaftahBugError) {
		runTest(true, valid, () -> StringUtils.charWiseSubtract(left, right), expectedResult, expectedNaftahBugError);
	}
}
