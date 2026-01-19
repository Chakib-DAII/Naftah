// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

package org.daiitech.naftah.utils.function;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TriFunctionTests {
	private static Stream<Arguments> arithmeticOperandsProvider() {
		return Stream
				.of(
					Arguments.of(2, 3, 4, 10),
					Arguments.of(0, 100, 5, 5),
					Arguments.of(1, 1, 1, 2),
					Arguments.of(-2, 3, 1, -5)
				);
	}

	@Test
	void integerSumTest() {
		TriFunction<Integer, Integer, Integer, Integer> sum = (a, b, c) -> a + b + c;

		assertEquals(6, sum.apply(1, 2, 3));
		assertEquals(0, sum.apply(-1, 1, 0));
		assertEquals(100, sum.apply(30, 30, 40));
	}

	@Test
	void stringConcatTest() {
		TriFunction<String, String, String, String> concat = (a, b, c) -> a + b + c;

		assertEquals("abc", concat.apply("a", "b", "c"));
		assertEquals("hello world!", concat.apply("hello", " ", "world!"));
		assertEquals("123", concat.apply("1", "2", "3"));
	}

	@Test
	void mixedTypesTest() {
		TriFunction<String, Integer, Boolean, String> formatter = (s, i, b) -> s + "-" + i + "-" + b;

		assertEquals("val-42-true", formatter.apply("val", 42, true));
		assertEquals("num-0-false", formatter.apply("num", 0, false));
	}

	@ParameterizedTest
	@MethodSource("arithmeticOperandsProvider")
	void arithmeticOperationTest(int a, int b, int c, int expected) {
		TriFunction<Integer, Integer, Integer, Integer> multiplyAndAdd = (x, y, z) -> x * y + z;
		assertEquals(expected, multiplyAndAdd.apply(a, b, c));
	}

	@Test
	void NullHandlingTest() {
		TriFunction<String, String, String, String> nullSafeJoin = (a, b, c) -> String
				.join(  "-",
						String.valueOf(a),
						String.valueOf(b),
						String.valueOf(c));

		assertEquals("null-null-null", nullSafeJoin.apply(null, null, null));
		assertEquals("x-null-z", nullSafeJoin.apply("x", null, "z"));
	}
}
