package org.daiitech.naftah.builtin.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.daiitech.naftah.builtin.lang.DynamicNumber;
import org.daiitech.naftah.errors.NaftahBugError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.daiitech.naftah.TestUtils.assertEquals;
import static org.daiitech.naftah.TestUtils.doAssertBugEquals;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidNumberConversionOverflowError;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidNumberValueError;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugNullInputError;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugUnsupportedBitwiseDecimalError;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahNegativeNumberError;

public class NumberUtilsTests {
	static Stream<Arguments> convertNumberToTargetClassProvider() {
		return Stream
				.of(
					Arguments.of(true, 42, Byte.class, (byte) 42, null),
					Arguments
							.of(false,
								null,
								Byte.class,
								null,
								newNaftahBugNullInputError(false, null, Byte.class)),
					Arguments
							.of(false,
								42,
								null,
								null,
								newNaftahBugNullInputError( false,
															42,
															null)),
					Arguments.of(true, 128, Short.class, (short) 128, null),
					Arguments.of(true, 999999, Integer.class, 999999, null),
					Arguments.of(true, 123456789L, Long.class, 123456789L, null),
					Arguments.of(true, new BigInteger("123456789"), Long.class, 123456789L, null),
					Arguments.of(true, new BigDecimal("987.654"), Float.class, 987.654f, null),
					Arguments.of(true, 42, Double.class, 42.0, null),
					Arguments
							.of(true,
								new BigDecimal("123.456"),
								BigDecimal.class,
								new BigDecimal("123.456"),
								null),
					Arguments
							.of(true,
								new BigInteger("123456789"),
								BigInteger.class,
								new BigInteger("123456789"),
								null),
					Arguments.of(true, new BigDecimal("456.789"), BigInteger.class, new BigInteger("456"), null),
					Arguments.of(true, 99.9, Double.class, 99.9, null),
					Arguments
							.of(false,
								new BigInteger("128"),
								Byte.class,
								null,
								newNaftahBugInvalidNumberConversionOverflowError(   true,
																					new BigInteger("128"),
																					Byte.class)),
					Arguments
							.of(false,
								new BigInteger("40000"),
								Short.class,
								null,
								newNaftahBugInvalidNumberConversionOverflowError(   true,
																					new BigInteger("40000"),
																					Short.class)),
					Arguments
							.of(false,
								new BigInteger("3000000000"),
								Integer.class,
								null,
								newNaftahBugInvalidNumberConversionOverflowError(   true,
																					new BigInteger(
																									"3000000000"),
																					Integer.class)),
					Arguments
							.of(false,
								new BigInteger("9223372036854775808"),
								Long.class,
								null,
								newNaftahBugInvalidNumberConversionOverflowError(   true,
																					new BigInteger(
																									"9223372036854775808"),
																					Long.class)),
					Arguments.of(true, 123, Integer.class, 123, null),
					Arguments.of(true, 12.34, Double.class, 12.34, null),
					Arguments.of(true, new BigInteger("42"), BigInteger.class, new BigInteger("42"), null)
				);
	}

	static void runTest(boolean valid,
						Object left,
						Supplier<?> resultSupplier,
						Object expectedResult,
						NaftahBugError expectedNaftahBugError) {
		DynamicNumber leftDynamicNumber;

		if (valid && (leftDynamicNumber = DynamicNumber.of(left)).isDecimal()) {
			runTest(false,
					resultSupplier,
					expectedResult,
					newNaftahBugUnsupportedBitwiseDecimalError(true, leftDynamicNumber));
		}
		else {
			runTest(valid,
					resultSupplier,
					expectedResult,
					expectedNaftahBugError);
		}
	}

	static void runTest(boolean valid,
						Object left,
						Object right,
						Supplier<?> resultSupplier,
						Object expectedResult,
						NaftahBugError expectedNaftahBugError) {
		DynamicNumber leftDynamicNumber, rightDynamicNumber;

		if (valid && ((leftDynamicNumber = DynamicNumber.of(left)).isDecimal() | (rightDynamicNumber = DynamicNumber
				.of(right)).isDecimal())) {
			runTest(false,
					resultSupplier,
					expectedResult,
					newNaftahBugUnsupportedBitwiseDecimalError(false, leftDynamicNumber, rightDynamicNumber));
		}
		else {
			runTest(valid,
					resultSupplier,
					expectedResult,
					expectedNaftahBugError);
		}
	}

	static void runTest(boolean valid,
						Supplier<?> resultSupplier,
						Object expectedResult,
						NaftahBugError expectedNaftahBugError) {
		if (valid) {
			var result = resultSupplier.get();
			assertEquals(result, expectedResult);
		}
		else {
			NaftahBugError naftahBugError = Assertions
					.assertThrows(  NaftahBugError.class,
									resultSupplier::get);
			doAssertBugEquals(naftahBugError, expectedNaftahBugError);
		}
	}

	static Stream<Arguments> multiplyProvider() {
		Object[] results = new Object[]{6,
										-20,
										10.0,
										10.889999999999999,
										42,
										7.0,
										5.0,
										0,
										0.0,
										new BigDecimal("2.42"),
										new BigDecimal("3000000000000000"),
										null,
										null,
										null,
										null,
										null};
		return buildProvider(false, results);
	}

	static Stream<Arguments> addProvider() {
		Object[] results = new Object[]{
										5,
										1,
										6.5,
										6.6,
										13,
										5.5,
										10.5,
										100,
										5.2,
										new BigDecimal("3.3"),
										new BigDecimal("1000000000000003"),
										null,
										null,
										null,
										null,
										null
		};

		return buildProvider(false, results);
	}

	static Stream<Arguments> divideProvider() {
		Object[] results = new Object[]{
										0,
										0,
										0.625,
										1.0,
										0,
										1.75,
										20.0,
										0,
										0.0,
										new BigDecimal("0.5"),
										333333333333333L,
										null,
										null,
										null,
										null,
										null
		};

		return buildProvider(false, results);
	}

	static Stream<Arguments> moduloProvider() {
		Object[] results = new Object[]{
										2,
										-4,
										2.5,
										0.0,
										6,
										1.5,
										0.0,
										0,
										0.0,
										new BigDecimal("1.1"),
										new BigDecimal("1"),
										null,
										null,
										null,
										null,
										null
		};

		return buildProvider(false, results);
	}

	static Stream<Arguments> maxProvider() {
		Object[] results = new Object[]{
										3,
										5,
										4.0,
										3.3,
										7,
										3.5,
										10,
										100,
										5.2,
										new BigDecimal("2.2"),
										new BigDecimal("1000000000000000"),
										null,
										null,
										null,
										null,
										null
		};

		return buildProvider(false, results);
	}

	static Stream<Arguments> minProvider() {
		Object[] results = new Object[]{
										2,
										-4,
										2.5,
										3.3,
										6,
										2,
										0.5,
										0,
										0.0,
										new BigDecimal("1.1"),
										new BigDecimal("3"),
										null,
										null,
										null,
										null,
										null
		};


		return buildProvider(false, results);
	}

	static Stream<Arguments> powProvider() {
		Object[] results = new Object[]{
										8,
										-1024,
										39.0625,
										35.937,
										279936,
										12.25,
										1,
										0,
										0.0,
										1.2100000000000002,
										new BigDecimal("1.0E45"),
										null,
										null,
										null,
										null,
										null
		};

		return buildProvider(false, results);
	}

	static Stream<Arguments> subtractProvider() {
		Object[] results = new Object[]{
										-1,
										-9,
										-1.5,
										0.0,
										-1,
										1.5,
										9.5,
										-100,
										-5.2,
										new BigDecimal("-1.1"),
										new BigDecimal("999999999999997"),
										null,
										null,
										null,
										null,
										null
		};

		return buildProvider(false, results);
	}

	static Stream<Arguments> roundProvider() {
		Object[] results = new Object[]{
										2,
										-4,
										3,
										3,
										6,
										4,
										10,
										0,
										0,
										new BigDecimal("1"),
										new BigDecimal("1000000000000000"),
										null,
										2,
										null,
										null,
										null
		};

		return buildProvider(true, results);
	}

	static Stream<Arguments> floorProvider() {
		Object[] results = new Object[]{
										2,
										-4,
										2.0,
										3,
										6,
										3.0,
										10,
										0,
										0,
										new BigDecimal("1"),
										new BigDecimal("1000000000000000"),
										null,
										2,
										null,
										null,
										null
		};

		return buildProvider(true, results);
	}

	static Stream<Arguments> ceilProvider() {
		Object[] results = new Object[]{
										2,
										-4,
										3,
										4,
										6,
										4,
										10,
										0,
										0,
										new BigDecimal("2"),
										new BigDecimal("1000000000000000"),
										null,
										2,
										null,
										null,
										null
		};

		return buildProvider(true, results);
	}

	static Stream<Arguments> negateProvider() {
		Object[] results = new Object[]{
										-2,
										4,
										-2.5,
										-3.3,
										-6,
										-3.5,
										-10,
										0,
										0.0,
										new BigDecimal("-1.1"),
										new BigDecimal("-1000000000000000"),
										null,
										-2,
										null,
										null,
										null
		};

		return buildProvider(true, results);
	}

	static Stream<Arguments> sqrtProvider() {
		Object[] results = new Object[]{
										1.4142135623730951,
										"NaN",
										1.5811388300841898,
										1.816590212458495,
										2.449489742783178,
										1.8708286933869707,
										3.1622776601683795,
										0.0,
										0.0,
										new BigDecimal("1.048808848170151546991453513679938"),
										new BigDecimal("3.162277660168379E7"),
										null,
										1.4142135623730951,
										null,
										null,
										null
		};

		return buildProvider(true, results);
	}

	static Stream<Arguments> absProvider() {
		Object[] results = new Object[]{
										2,
										4,
										2.5,
										3.3,
										6,
										3.5,
										10,
										0,
										0,
										new BigDecimal("1.1"),
										new BigDecimal("1000000000000000"),
										null,
										2,
										null,
										null,
										null
		};

		return buildProvider(true, results);
	}

	static Stream<Arguments> signumProvider() {
		Object[] results = new Object[]{
										1,
										-1,
										1,
										1,
										1,
										1,
										1,
										0,
										0,
										1,
										1,
										null,
										1,
										null,
										null,
										null
		};

		return buildProvider(true, results);
	}

	static Stream<Arguments> isZeroProvider() {
		Object[] results = new Object[]{
										false,
										false,
										false,
										false,
										false,
										false,
										false,
										true,
										true,
										false,
										false,
										null,
										false,
										null,
										null,
										null
		};


		return buildProvider(true, results);
	}

	static Stream<Arguments> equalsProvider() {
		Object[] results = new Object[]{
										false,
										false,
										false,
										true,
										false,
										false,
										false,
										false,
										false,
										false,
										false,
										null,
										false,
										null,
										null,
										null
		};

		return buildProvider(false, results);
	}

	static Stream<Arguments> compareProvider() {
		Object[] results = new Object[]{
										-1,
										-1,
										-1,
										0,
										-1,
										1,
										1,
										-1,
										-1,
										-1,
										1,
										null,
										null,
										null,
										null,
										null
		};

		return buildProvider(false, results);
	}

	static Stream<Arguments> andProvider() {
		Object[] results = new Object[]{
										2,
										4,
										0,
										0,
										6,
										2,
										10,
										0,
										0,
										0,
										0,
										null,
										null,
										null,
										null,
										null
		};

		return buildProvider(false, results);
	}

	static Stream<Arguments> orProvider() {
		Object[] results = new Object[]{
										3,
										-3,
										2,
										3,
										7,
										3,
										10,
										100,
										5,
										3,
										1000000000000003L,
										null,
										null,
										null,
										null,
										null
		};

		return buildProvider(false, results);
	}

	static Stream<Arguments> xorProvider() {
		Object[] results = new Object[]{
										1,
										-7,
										6,
										0,
										1,
										1,
										10,
										100,
										5,
										1,
										1000000000000003L,
										null,
										null,
										null,
										null,
										null
		};

		return buildProvider(false, results);
	}

	static Stream<Arguments> notProvider() {
		Object[] results = new Object[]{
										-3,
										3,
										-3,
										-4,
										-7,
										-4,
										-11,
										-1,
										-6,
										-2,
										-1000000000000001L,
										null,
										-3,
										null,
										null,
										null
		};

		return buildProvider(true, results);
	}

	static Stream<Arguments> shiftLeftProvider() {
		Object[] results = new Object[]{
										16,
										-128,
										1,
										1,
										768,
										56,
										10,
										0,
										0,
										new BigDecimal("88"),
										new BigDecimal("8000000000000000"),
										null,
										null,
										null,
										null,
										null
		};

		return buildProvider(false, results);
	}

	static Stream<Arguments> shiftRightProvider() {
		Object[] results = new Object[]{
										0,
										-1,
										null,
										null,
										0,
										0,
										10,
										0,
										0,
										BigDecimal.ZERO,
										new BigDecimal("125000000000000"),
										null,
										null,
										null,
										null,
										null
		};


		return buildProvider(false, results);
	}

	static Stream<Arguments> unsignedShiftRightProvider() {
		Object[] results = new Object[]{
										0,
										134217727,
										null,
										null,
										0,
										0,
										10,
										0,
										0,
										BigDecimal.ZERO,
										new BigDecimal("125000000000000"),
										null,
										null,
										null,
										null,
										null
		};

		return buildProvider(false, results);
	}

	static Stream<Arguments> preIncrementProvider() {
		Object[] results = new Object[]{
										3,
										-3,
										3.5,
										4.3,
										7,
										4.5,
										11,
										1,
										1,
										new BigDecimal("2.1"),
										new BigDecimal("1000000000000001"),
										null,
										3,
										null,
										null,
										null
		};

		return buildProvider(true, results);
	}

	static Stream<Arguments> postIncrementProvider() {
		Object[] results = new Object[]{
										2,
										-4,
										2.5,
										3.3,
										6,
										3.5,
										10,
										0,
										0,
										new BigDecimal("1.1"),
										new BigDecimal("1000000000000000"),
										null,
										2,
										null,
										null,
										null
		};

		return buildProvider(true, results);
	}

	static Stream<Arguments> preDecrementProvider() {
		Object[] results = new Object[]{
										1,
										-5,
										1.5,
										2.3,
										5,
										2.5,
										9,
										-1,
										-1,
										new BigDecimal("0.1"),
										new BigDecimal("999999999999999"),
										null,
										1,
										null,
										null,
										null
		};

		return buildProvider(true, results);
	}

	static Stream<Arguments> postDecrementProvider() {
		Object[] results = new Object[]{
										2,
										-4,
										2.5,
										3.3,
										6,
										3.5,
										10,
										0,
										0,
										new BigDecimal("1.1"),
										new BigDecimal("1000000000000000"),
										null,
										2,
										null,
										null,
										null
		};

		return buildProvider(true, results);
	}

	static Stream<Arguments> buildProvider(boolean singleInput, Object[] results) {
		return singleInput ?
				Stream
						.of(
							Arguments.of(true, 2, results[0], null),
							Arguments.of(true, -4, results[1], null),
							Arguments.of(true, 2.5, results[2], null),
							Arguments.of(true, 3.3, results[3], null),
							Arguments.of(true, "6", results[4], null),
							Arguments.of(true, "3.5", results[5], null),
							Arguments.of(true, 10, results[6], null),
							Arguments.of(true, 0, results[7], null),
							Arguments.of(true, "0.0", results[8], null),
							Arguments.of(true, new BigDecimal("1.1"), results[9], null),
							Arguments.of(true, "1000000000000000", results[10], null),
							Arguments.of(false, null, results[11], newNaftahBugNullInputError(true, (Object) null)),
							Arguments.of(true, 2, results[12], null),
							Arguments
									.of(false,
										"abc",
										results[13],
										newNaftahBugInvalidNumberValueError("abc",
																			new NumberFormatException(
																										"For input string: \"abc\""))),
							Arguments.of(false, true, results[14], newNaftahBugInvalidNumberValueError(true)),
							Arguments.of(false, false, results[15], newNaftahBugInvalidNumberValueError(false))
						) :
				Stream
						.of(
							Arguments.of(true, 2, 3, results[0], null),
							Arguments.of(true, -4, 5, results[1], null),
							Arguments.of(true, 2.5, 4.0, results[2], null),
							Arguments.of(true, 3.3, 3.3, results[3], null),
							Arguments.of(true, "6", 7, results[4], null),
							Arguments.of(true, "3.5", 2, results[5], null),
							Arguments.of(true, 10, "0.5", results[6], null),
							Arguments.of(true, 0, 100, results[7], null),
							Arguments.of(true, "0.0", "5.2", results[8], null),
							Arguments.of(true, new BigDecimal("1.1"), new BigDecimal("2.2"), results[9], null),
							Arguments.of(true, "1000000000000000", "3", results[10], null),
							Arguments
									.of(false,
										null,
										1,
										results[1],
										newNaftahBugNullInputError(true, (Object) null)),
							Arguments
									.of(false,
										2,
										null,
										results[12],
										newNaftahBugNullInputError(true, (Object) null)),
							Arguments
									.of(false,
										"abc",
										2,
										results[13],
										newNaftahBugInvalidNumberValueError("abc",
																			new NumberFormatException(
																										"For input string: \"abc\""))),
							Arguments.of(false, true, 2, results[14], newNaftahBugInvalidNumberValueError(true)),
							Arguments.of(false, false, 2, results[15], newNaftahBugInvalidNumberValueError(false))
						);
	}

	static Stream<Arguments> parseDynamicNumberProvider() {
		return Stream
				.of(
					Arguments.of(true, "1", 2, 1, null),
					Arguments.of(true, "2", 3, 2, null),
					Arguments.of(true, "3", 4, 3, null),
					Arguments.of(true, "4", 5, 4, null),
					Arguments.of(true, "5", 6, 5, null),
					Arguments.of(true, "6", 7, 6, null),
					Arguments.of(true, "7", 8, 7, null),
					Arguments.of(true, "8", 9, 8, null),
					Arguments.of(true, "9", 10, 9, null),
					Arguments.of(true, "A", 11, 10, null),
					Arguments.of(true, "B", 12, 11, null),
					Arguments.of(true, "C", 13, 12, null),
					Arguments.of(true, "D", 14, 13, null),
					Arguments.of(true, "E", 15, 14, null),
					Arguments.of(true, "F", 16, 15, null),
					Arguments.of(true, "G", 17, 16, null),
					Arguments.of(true, "H", 18, 17, null),
					Arguments.of(true, "I", 19, 18, null),
					Arguments.of(true, "J", 20, 19, null),
					Arguments.of(true, "K", 21, 20, null),
					Arguments.of(true, "L", 22, 21, null),
					Arguments.of(true, "M", 23, 22, null),
					Arguments.of(true, "N", 24, 23, null),
					Arguments.of(true, "O", 25, 24, null),
					Arguments.of(true, "P", 26, 25, null),
					Arguments.of(true, "Q", 27, 26, null),
					Arguments.of(true, "R", 28, 27, null),
					Arguments.of(true, "S", 29, 28, null),
					Arguments.of(true, "T", 30, 29, null),
					Arguments.of(true, "U", 31, 30, null),
					Arguments.of(true, "V", 32, 31, null),
					Arguments.of(false, "2.2", 11, null, newNaftahBugInvalidNumberValueError("2.2", 11)),
					Arguments.of(false, null, 10, null, newNaftahBugNullInputError(true, (Object) null))
				);
	}

	@ParameterizedTest
	@MethodSource("convertNumberToTargetClassProvider")
	void convertNumberToTargetClass(boolean valid,
									Number input,
									Class<? extends Number> targetClass,
									Number expectedResult,
									NaftahBugError expectedNaftahBugError) {
		runTest(valid,
				() -> NumberUtils.convertNumberToTargetClass(input, targetClass),
				expectedResult,
				expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("multiplyProvider")
	void multiply(  boolean valid,
					Object left,
					Object right,
					Number expectedResult,
					NaftahBugError expectedNaftahBugError) {
		runTest(valid,
				() -> NumberUtils.multiply(left, right),
				expectedResult,
				expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("addProvider")
	void add(   boolean valid,
				Object left,
				Object right,
				Number expectedResult,
				NaftahBugError expectedNaftahBugError) {
		runTest(valid,
				() -> NumberUtils.add(left, right),
				expectedResult,
				expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("subtractProvider")
	void subtract(  boolean valid,
					Object left,
					Object right,
					Number expectedResult,
					NaftahBugError expectedNaftahBugError) {
		runTest(valid,
				() -> NumberUtils.subtract(left, right),
				expectedResult,
				expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("divideProvider")
	void divide(boolean valid,
				Object left,
				Object right,
				Number expectedResult,
				NaftahBugError expectedNaftahBugError) {
		runTest(valid,
				() -> NumberUtils.divide(left, right),
				expectedResult,
				expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("moduloProvider")
	void modulo(boolean valid,
				Object left,
				Object right,
				Number expectedResult,
				NaftahBugError expectedNaftahBugError) {
		runTest(valid,
				() -> NumberUtils.modulo(left, right),
				expectedResult,
				expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("maxProvider")
	void max(   boolean valid,
				Object left,
				Object right,
				Number expectedResult,
				NaftahBugError expectedNaftahBugError) {
		runTest(valid,
				() -> NumberUtils.max(left, right),
				expectedResult,
				expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("minProvider")
	void min(   boolean valid,
				Object left,
				Object right,
				Number expectedResult,
				NaftahBugError expectedNaftahBugError) {
		runTest(valid,
				() -> NumberUtils.min(left, right),
				expectedResult,
				expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("powProvider")
	void pow(   boolean valid,
				Object left,
				Object right,
				Number expectedResult,
				NaftahBugError expectedNaftahBugError) {
		if (right != null) {
			runTest(valid,
					() -> NumberUtils.pow(left, NumberUtils.parseDynamicNumber(right).intValue()),
					expectedResult,
					expectedNaftahBugError);
		}
	}

	@ParameterizedTest
	@MethodSource("roundProvider")
	void round( boolean valid,
				Object left,
				Number expectedResult,
				NaftahBugError expectedNaftahBugError) {
		runTest(valid,
				() -> NumberUtils.round(left),
				expectedResult,
				expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("floorProvider")
	void floor( boolean valid,
				Object left,
				Number expectedResult,
				NaftahBugError expectedNaftahBugError) {
		runTest(valid,
				() -> NumberUtils.floor(left),
				expectedResult,
				expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("ceilProvider")
	void ceil(  boolean valid,
				Object left,
				Number expectedResult,
				NaftahBugError expectedNaftahBugError) {
		runTest(valid,
				() -> NumberUtils.ceil(left),
				expectedResult,
				expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("negateProvider")
	void negate(boolean valid,
				Object left,
				Number expectedResult,
				NaftahBugError expectedNaftahBugError) {
		runTest(valid,
				() -> NumberUtils.negate(left),
				expectedResult,
				expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("sqrtProvider")
	void sqrt(  boolean valid,
				Object left,
				Object expectedResult,
				NaftahBugError expectedNaftahBugError) {
		if ("NaN".equals(expectedResult)) {
			runTest(false,
					() -> NumberUtils.sqrt(left),
					expectedResult,
					newNaftahNegativeNumberError());
		}
		else {
			runTest(valid,
					() -> NumberUtils.sqrt(left),
					expectedResult,
					expectedNaftahBugError);
		}
	}

	@ParameterizedTest
	@MethodSource("absProvider")
	void abs(   boolean valid,
				Object left,
				Number expectedResult,
				NaftahBugError expectedNaftahBugError) {
		runTest(valid,
				() -> NumberUtils.abs(left),
				expectedResult,
				expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("isZeroProvider")
	void isZero(boolean valid,
				Object left,
				Boolean expectedResult,
				NaftahBugError expectedNaftahBugError) {
		runTest(valid,
				() -> NumberUtils.isZero(left),
				expectedResult,
				expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("equalsProvider")
	void equals(boolean valid,
				Object left,
				Object right,
				Boolean expectedResult,
				NaftahBugError expectedNaftahBugError) {
		runTest(valid,
				() -> NumberUtils.equals(left, right),
				expectedResult,
				expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("compareProvider")
	void compare(   boolean valid,
					Object left,
					Object right,
					Integer expectedResult,
					NaftahBugError expectedNaftahBugError) {
		runTest(valid,
				() -> NumberUtils.compare(left, right),
				expectedResult,
				expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("andProvider")
	void and(   boolean valid,
				Object left,
				Object right,
				Object expectedResult,
				NaftahBugError expectedNaftahBugError) {
		runTest(valid, left, right, () -> NumberUtils.and(left, right), expectedResult, expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("orProvider")
	void or(boolean valid,
			Object left,
			Object right,
			Object expectedResult,
			NaftahBugError expectedNaftahBugError) {
		runTest(valid, left, right, () -> NumberUtils.or(left, right), expectedResult, expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("xorProvider")
	void xor(   boolean valid,
				Object left,
				Object right,
				Object expectedResult,
				NaftahBugError expectedNaftahBugError) {
		runTest(valid, left, right, () -> NumberUtils.xor(left, right), expectedResult, expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("notProvider")
	void not(   boolean valid,
				Object left,
				Object expectedResult,
				NaftahBugError expectedNaftahBugError) {
		runTest(valid, left, () -> NumberUtils.not(left), expectedResult, expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("shiftLeftProvider")
	void shiftLeft( boolean valid,
					Object left,
					Object right,
					Object expectedResult,
					NaftahBugError expectedNaftahBugError) {
		runTest(valid,
				left,
				() -> NumberUtils.shiftLeft(left, DynamicNumber.of(right).intValue()),
				expectedResult,
				expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("shiftRightProvider")
	void shiftRight(boolean valid,
					Object left,
					Object right,
					Object expectedResult,
					NaftahBugError expectedNaftahBugError) {
		runTest(valid,
				left,
				() -> NumberUtils.shiftRight(left, DynamicNumber.of(right).intValue()),
				expectedResult,
				expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("unsignedShiftRightProvider")
	void unsignedShiftRight(boolean valid,
							Object left,
							Object right,
							Object expectedResult,
							NaftahBugError expectedNaftahBugError) {
		runTest(valid,
				left,
				() -> NumberUtils.unsignedShiftRight(left, DynamicNumber.of(right).intValue()),
				expectedResult,
				expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("preIncrementProvider")
	void preIncrement(  boolean valid,
						Object left,
						Object expectedResult,
						NaftahBugError expectedNaftahBugError) {
		runTest(valid,
				() -> NumberUtils.preIncrement(left),
				expectedResult,
				expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("postIncrementProvider")
	void postIncrement( boolean valid,
						Object left,
						Object expectedResult,
						NaftahBugError expectedNaftahBugError) {
		runTest(valid,
				() -> NumberUtils.postIncrement(left),
				expectedResult,
				expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("preDecrementProvider")
	void preDecrement(  boolean valid,
						Object left,
						Object expectedResult,
						NaftahBugError expectedNaftahBugError) {
		runTest(valid,
				() -> NumberUtils.preDecrement(left),
				expectedResult,
				expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("postDecrementProvider")
	void postDecrement( boolean valid,
						Object left,
						Object expectedResult,
						NaftahBugError expectedNaftahBugError) {
		runTest(valid,
				() -> NumberUtils.postDecrement(left),
				expectedResult,
				expectedNaftahBugError);
	}

	@ParameterizedTest
	@MethodSource("parseDynamicNumberProvider")
	void parseDynamicNumber(boolean valid,
							String text,
							int radix,
							Object expectedResult,
							NaftahBugError expectedNaftahBugError) {
		runTest(valid,
				() -> NumberUtils.parseDynamicNumber(text, radix, null),
				expectedResult,
				expectedNaftahBugError);
	}


}
