package org.daiitech.naftah.builtin.utils.op;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UnaryOperationTests {

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
}
