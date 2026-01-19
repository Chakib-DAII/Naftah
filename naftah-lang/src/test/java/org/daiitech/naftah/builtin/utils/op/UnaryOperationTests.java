// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

package org.daiitech.naftah.builtin.utils.op;

import org.daiitech.naftah.builtin.lang.NaN;
import org.daiitech.naftah.builtin.lang.None;
import org.daiitech.naftah.builtin.utils.NumberUtils;
import org.daiitech.naftah.errors.NaftahBugError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnaryOperationTests {

	@Test
	void bitwiseNotIntegerTest() {
		assertEquals(~5, ((Number) UnaryOperation.BITWISE_NOT.apply(5)).intValue());
	}

	@Test
	void bitwiseNotCharTest() {
		char input = 'A';
		char expected = (char) ~((int) input);
		assertEquals(expected, UnaryOperation.BITWISE_NOT.apply(input));
	}

	@Test
	void bitwiseNotBooleanTest() {
		assertFalse((Boolean) UnaryOperation.BITWISE_NOT.apply(true));
		assertTrue((Boolean) UnaryOperation.BITWISE_NOT.apply(false));
	}

	@Test
	void minusIntegerTest() {
		assertEquals(-5, ((Number) UnaryOperation.MINUS.apply(5)).intValue());
	}

	@Test
	void plusIntegerTest() {
		assertEquals(5, ((Number) UnaryOperation.PLUS.apply(5)).intValue());
	}

	@Test
	void preIncrementIntegerTest() {
		assertEquals(6, ((Number) UnaryOperation.PRE_INCREMENT.apply(5)).intValue());
	}

	@Test
	void postIncrementIntegerTest() {
		assertEquals(5, ((Number) UnaryOperation.POST_INCREMENT.apply(5)).intValue());
	}

	@Test
	void preDecrementIntegerTest() {
		assertEquals(4, ((Number) UnaryOperation.PRE_DECREMENT.apply(5)).intValue());
	}

	@Test
	void postDecrementIntegerTest() {
		assertEquals(5, ((Number) UnaryOperation.POST_DECREMENT.apply(5)).intValue());
	}

	@Test
	void preIncrementCharTest() {
		char input = 'A';
		char expected = (char) ('A' + 1);
		assertEquals(expected, UnaryOperation.PRE_INCREMENT.apply(input));
	}

	@Test
	void postDecrementCharTest() {
		char input = 'Z';
		char expected = 'Z';
		assertEquals(expected, UnaryOperation.POST_DECREMENT.apply(input));
	}

	@Test
	void preIncrementBooleanTest() {
		assertFalse((Boolean) UnaryOperation.PRE_INCREMENT.apply(true));
	}

	@Test
	void postDecrementBooleanTest() {
		assertTrue((Boolean) UnaryOperation.POST_DECREMENT.apply(true));
	}

	@Test
	void bitwiseNotStringTest() {
		String input = "test";
		String result = (String) UnaryOperation.BITWISE_NOT.apply(input);
		assertNotNull(result);
	}

	@Test
	void preIncrementStringTest() {
		String input = "value";
		String result = (String) UnaryOperation.PRE_INCREMENT.apply(input);
		assertNotNull(result);
	}

	@Test
	void minusStringTest() {
		assertTrue(NaN.isNaN(UnaryOperation.MINUS.apply("value")));
	}

	@Test
	void plusStringTest() {
		assertTrue(NaN.isNaN(UnaryOperation.PLUS.apply("value")));
	}

	@Test
	void unaryNaNTest() {
		Assertions.assertThrows(NaftahBugError.class, () -> UnaryOperation.PRE_INCREMENT.apply(NaN.get()));
		Assertions.assertThrows(NaftahBugError.class, () -> UnaryOperation.PRE_DECREMENT.apply(NaN.get()));
		Assertions.assertThrows(NaftahBugError.class, () -> UnaryOperation.POST_DECREMENT.apply(NaN.get()));
		Assertions.assertThrows(NaftahBugError.class, () -> UnaryOperation.POST_INCREMENT.apply(NaN.get()));
		Object result = UnaryOperation.PLUS.apply(NaN.get());
		assertTrue(NaN.isNaN(result));
		result = UnaryOperation.MINUS.apply(NaN.get());
		assertTrue(NaN.isNaN(result));
		result = UnaryOperation.NOT.apply(NaN.get());
		assertTrue((Boolean) result);
		result = UnaryOperation.BITWISE_NOT.apply(NaN.get());
		assertTrue(NumberUtils.equals(-1, result));
	}

	@Test
	void unaryNoneTest() {
		Assertions.assertThrows(NaftahBugError.class, () -> UnaryOperation.PRE_INCREMENT.apply(None.get()));
		Assertions.assertThrows(NaftahBugError.class, () -> UnaryOperation.PRE_DECREMENT.apply(None.get()));
		Assertions.assertThrows(NaftahBugError.class, () -> UnaryOperation.POST_DECREMENT.apply(None.get()));
		Assertions.assertThrows(NaftahBugError.class, () -> UnaryOperation.POST_INCREMENT.apply(None.get()));
		Object result = UnaryOperation.PLUS.apply(None.get());
		assertEquals(0, result);
		result = UnaryOperation.MINUS.apply(None.get());
		assertEquals(-0, result);
		result = UnaryOperation.NOT.apply(None.get());
		assertTrue((Boolean) result);
		result = UnaryOperation.BITWISE_NOT.apply(None.get());
		assertTrue(NumberUtils.equals(-1, result));
	}
}
