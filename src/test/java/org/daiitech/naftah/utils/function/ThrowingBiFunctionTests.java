package org.daiitech.naftah.utils.function;

import java.util.function.BiFunction;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThrowingBiFunctionTests {
	static final BiFunction<String, Exception, RuntimeException> WRAPPER = (msg, ex) -> new IllegalArgumentException(
																														"Wrapped: " + msg,
																														ex);

	static final ThrowingBiFunction<Integer, Integer, Integer> division = (a, b) -> a / b;

	static final ThrowingBiFunction<Integer, Integer, Integer> throwing = (a, b) -> {
		throw new Exception("fail");
	};

	@Nested
	class OfMethodTests {

		@Test
		void ofReturnsSameFunctionTest() {
			ThrowingBiFunction<Integer, Integer, Integer> fn = (a, b) -> a + b;
			ThrowingBiFunction<Integer, Integer, Integer> wrapped = ThrowingBiFunction.of(fn);
			assertSame(fn, wrapped);
		}

		@Test
		void ofWithWrapperThrowsWrappedExceptionTest() {
			ThrowingBiFunction<Integer, Integer, Integer> fn = (a, b) -> {
				throw new Exception("test");
			};

			ThrowingBiFunction<Integer, Integer, Integer> wrapped = ThrowingBiFunction.of(fn, WRAPPER);

			RuntimeException ex = assertThrows(RuntimeException.class, () -> wrapped.apply(1, 2));
			assertTrue(ex instanceof IllegalArgumentException);
			assertEquals("Wrapped: test", ex.getMessage());
			assertEquals("test", ex.getCause().getMessage());
		}
	}

	@Nested
	class ApplyWithExceptionTests {

		@ParameterizedTest
		@CsvSource({
					"6, 3, 2",
					"10, 2, 5",
					"-9, -3, 3"
		})
		void applyWithExceptionTest(int a, int b, int expected) throws Exception {
			assertEquals(expected, division.applyWithException(a, b));
		}

		@Test
		void applyWithExceptionThrowsCheckedExceptionTest() {
			assertThrows(Exception.class, () -> throwing.applyWithException(1, 1));
		}
	}

	@Nested
	class ApplyTests {

		@ParameterizedTest
		@CsvSource({
					"6, 3, 2",
					"12, 4, 3"
		})
		void applyDefaultWrapperReturnsResultTest(int a, int b, int expected) {
			assertEquals(expected, division.apply(a, b));
		}

		@Test
		void applyDefaultWrapperThrowsRuntimeWrappedExceptionTest() {
			RuntimeException ex = assertThrows(RuntimeException.class, () -> throwing.apply(1, 1));
			assertEquals("fail", ex.getMessage());
		}

		@Test
		void applyCustomWrapperThrowsWrappedTest() {
			RuntimeException ex = assertThrows( IllegalArgumentException.class,
												() -> throwing.apply(1, 2, WRAPPER));
			assertEquals("Wrapped: fail", ex.getMessage());
			assertTrue(ex.getCause() instanceof Exception);
		}

		@Test
		void applyCustomWrapperRethrowsUncheckedTest() {
			ThrowingBiFunction<Integer, Integer, Integer> rethrowing = (a, b) -> {
				throw new IllegalStateException("unchecked");
			};

			RuntimeException ex = assertThrows( IllegalStateException.class,
												() -> rethrowing.apply(1, 2, WRAPPER));

			assertEquals("unchecked", ex.getMessage());
		}
	}

	@Nested
	class ThrowingMethodTests {

		@Test
		void throwingUsesCustomWrapperTest() {
			ThrowingBiFunction<Integer, Integer, Integer> throwingWrapped = throwing.throwing(WRAPPER);

			RuntimeException ex = assertThrows(RuntimeException.class, () -> throwingWrapped.apply(5, 2));
			assertEquals("Wrapped: fail", ex.getMessage());
		}

		@Test
		void throwingDelegatesToOriginalFunctionTest() throws Exception {
			ThrowingBiFunction<Integer, Integer, Integer> wrapped = division.throwing(WRAPPER);

			assertEquals(2, wrapped.apply(4, 2));
			assertEquals(2, wrapped.applyWithException(4, 2));
		}
	}
}
