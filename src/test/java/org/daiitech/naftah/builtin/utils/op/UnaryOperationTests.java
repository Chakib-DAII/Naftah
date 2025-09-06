package org.daiitech.naftah.builtin.utils.op;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UnaryOperationTests {

	@Test
	void bitwiseNotIntegerTest() {
		assertEquals(~5, UnaryOperation.BITWISE_NOT.apply(5).intValue());
	}

	@Test
	void bitwiseNotCharTest() {
		char input = 'A';
		char expected = (char) ~((int) input);
		assertEquals(expected, UnaryOperation.BITWISE_NOT.apply(input));
	}

	@Test
	void bitwiseNotBooleanTest() {
		assertFalse(UnaryOperation.BITWISE_NOT.apply(true));
		assertTrue(UnaryOperation.BITWISE_NOT.apply(false));
	}

	@Test
	void preIncrementIntegerTest() {
		assertEquals(6, UnaryOperation.PRE_INCREMENT.apply(5).intValue());
	}

	@Test
	void postIncrementIntegerTest() {
		assertEquals(5, UnaryOperation.POST_INCREMENT.apply(5).intValue());
	}

	@Test
	void preDecrementIntegerTest() {
		assertEquals(4, UnaryOperation.PRE_DECREMENT.apply(5).intValue());
	}

	@Test
	void postDecrementIntegerTest() {
		assertEquals(5, UnaryOperation.POST_DECREMENT.apply(5).intValue());
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
		assertFalse(UnaryOperation.PRE_INCREMENT.apply(true));
	}

	@Test
	void postDecrementBooleanTest() {
		assertTrue(UnaryOperation.POST_DECREMENT.apply(true));
	}

	@Test
	void bitwiseNotStringTest() {
		String input = "test";
		String result = UnaryOperation.BITWISE_NOT.apply(input);
		assertNotNull(result);
	}

	@Test
	void preIncrementStringTest() {
		String input = "value";
		String result = UnaryOperation.PRE_INCREMENT.apply(input);
		assertNotNull(result);
	}
}
