package org.daiitech.naftah.utils.function;

import java.util.function.BiFunction;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ThrowingFunctionTests {

	static final BiFunction<String, Exception, RuntimeException> WRAPPER = (msg, ex) -> new IllegalStateException(
																													"Wrapped: " + msg,
																													ex);

	static final ThrowingFunction<Integer, Integer> square = x -> x * x;

	static final ThrowingFunction<Integer, Integer> throwing = x -> {
		throw new Exception("boom");
	};

	@Nested
	class OfMethodTests {

		@Test
		void ofReturnsSameFunctionTest() {
			ThrowingFunction<Integer, Integer> fn = x -> x + 1;
			ThrowingFunction<Integer, Integer> result = ThrowingFunction.of(fn);
			assertSame(fn, result);
		}

		@Test
		void ofWithWrapperThrowsWrappedExceptionTest() {
			ThrowingFunction<Integer, Integer> fn = x -> {
				throw new Exception("fail");
			};
			ThrowingFunction<Integer, Integer> wrapped = ThrowingFunction.of(fn, WRAPPER);

			RuntimeException ex = assertThrows(RuntimeException.class, () -> wrapped.apply(5));
			assertTrue(ex instanceof IllegalStateException);
			assertEquals("Wrapped: fail", ex.getMessage());
			assertEquals("fail", ex.getCause().getMessage());
		}
	}

	@Nested
	class ApplyWithExceptionTests {

		@ParameterizedTest
		@ValueSource(ints = {1, 2, 5, 10})
		void applyWithExceptionSquareTest(int input) throws Exception {
			assertEquals(input * input, square.applyWithException(input));
		}

		@Test
		void applyWithExceptionThrowsCheckedExceptionTest() {
			assertThrows(Exception.class, () -> throwing.applyWithException(42));
		}
	}

	@Nested
	class ApplyTests {

		@ParameterizedTest
		@CsvSource({
					"2, 4",
					"3, 9",
					"5, 25"
		})
		void applyDefaultWrapperSquareTest(int input, int expected) {
			assertEquals(expected, square.apply(input));
		}

		@Test
		void applyDefaultWrapperThrowsWrappedRuntimeExceptionTest() {
			RuntimeException ex = assertThrows(RuntimeException.class, () -> throwing.apply(1));
			assertEquals("boom", ex.getMessage());
		}

		@Test
		void applyCustomWrapperThrowsWrappedTest() {
			RuntimeException ex = assertThrows( IllegalStateException.class,
												() -> throwing.apply(1, WRAPPER));
			assertEquals("Wrapped: boom", ex.getMessage());
		}

		@Test
		void applyCustomWrapperRethrowsUncheckedTest() {
			ThrowingFunction<Integer, Integer> rethrowing = x -> {
				throw new IllegalArgumentException("unchecked");
			};

			RuntimeException ex = assertThrows( IllegalArgumentException.class,
												() -> rethrowing.apply(10, WRAPPER));
			assertEquals("unchecked", ex.getMessage());
		}
	}

	@Nested
	class ThrowingMethodTests {

		@Test
		void throwingWrapsExceptionWithCustomWrapperTest() {
			ThrowingFunction<Integer, Integer> wrapped = throwing.throwing(WRAPPER);

			RuntimeException ex = assertThrows(RuntimeException.class, () -> wrapped.apply(99));
			assertEquals("Wrapped: boom", ex.getMessage());
			assertTrue(ex.getCause() instanceof Exception);
		}

		@Test
		void throwingDelegatesToOriginalFunctionTest() throws Exception {
			ThrowingFunction<Integer, Integer> wrapped = square.throwing(WRAPPER);

			assertEquals(100, wrapped.apply(10));
			assertEquals(100, wrapped.applyWithException(10));
		}
	}
}
