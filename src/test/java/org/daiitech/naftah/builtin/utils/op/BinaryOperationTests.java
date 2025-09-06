package org.daiitech.naftah.builtin.utils.op;

import java.util.stream.Stream;

import org.daiitech.naftah.errors.NaftahBugError;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BinaryOperationTests {

	@Nested
	class NumbersOperationTests {
		static Stream<Arguments> numbersOperationProvider() {
			return Stream
					.of(BinaryOperation.values())
					.flatMap(op -> Stream
							.of(
								Arguments.of(op, 5, 3),
								Arguments.of(op, 0, 1),
								Arguments.of(op, -1, 2)
							));
		}

		@ParameterizedTest
		@MethodSource("numbersOperationProvider")
		void numberOperandsTest(BinaryOperation op, Number left, Number right) {
			assertNotNull(op.apply(left, right));
		}
	}

	@Nested
	class BooleanAndNumberOperationTests {
		static Stream<Arguments> booleanAndNumberOperationProvider() {
			return Stream
					.of(BinaryOperation.values())
					.flatMap(op -> Stream
							.of(
								Arguments.of(op, true, 1),
								Arguments.of(op, false, 0)
							));
		}

		static Stream<Arguments> numberAndBooleanOperationProvider() {
			return booleanAndNumberOperationProvider()
					.map(args -> Arguments.of(args.get()[0], args.get()[2], args.get()[1]));
		}

		@ParameterizedTest
		@MethodSource("booleanAndNumberOperationProvider")
		void booleanAndNumberOperandsTest(BinaryOperation op, boolean left, Number right) {
			if ((BinaryOperation.DIVIDE.equals(op) || BinaryOperation.MODULO.equals(op)) && right.equals(0)) {
				assertThrows(ArithmeticException.class, () -> op.apply(left, right));
			}
			else {
				assertNotNull(op.apply(left, right));
			}
		}

		@ParameterizedTest
		@MethodSource("numberAndBooleanOperationProvider")
		void numberAndBooleanOperandsTest(BinaryOperation op, Number left, boolean right) {
			if ((BinaryOperation.DIVIDE.equals(op) || BinaryOperation.MODULO.equals(op)) && !right) {
				assertThrows(ArithmeticException.class, () -> op.apply(left, right));
			}
			else {
				assertNotNull(op.apply(left, right));
			}
		}
	}

	@Nested
	class CharAndNumberOperationTests {
		static Stream<Arguments> charAndNumberOperationProvider() {
			return Stream
					.of(BinaryOperation.values())
					.flatMap(op -> Stream
							.of(
								Arguments.of(op, 'A', 2),
								Arguments.of(op, 'b', 1)
							));
		}

		static Stream<Arguments> numberAndCharOperationProvider() {
			return charAndNumberOperationProvider()
					.map(args -> Arguments.of(args.get()[0], args.get()[2], args.get()[1]));
		}

		@ParameterizedTest
		@MethodSource("charAndNumberOperationProvider")
		void charAndNumberOperandsTest(BinaryOperation op, char left, Number right) {
			Object result = op.apply(left, right);
			assertTrue(result instanceof Character || result instanceof Number || result instanceof Boolean);
		}

		@ParameterizedTest
		@MethodSource("numberAndCharOperationProvider")
		void numberAndCharOperandsTest(BinaryOperation op, Number left, char right) {
			if (BinaryOperation.ELEMENTWISE_DIVIDE.equals(op)) {
				assertThrows(NaftahBugError.class, () -> op.apply(left, right));
			}
			else {
				Object result = op.apply(left, right);
				assertTrue(result instanceof Character || result instanceof Number || result instanceof Boolean);
			}
		}
	}

	@Nested
	class CharsOperationTests {
		static Stream<Arguments> charsOperationProvider() {
			return Stream
					.of(BinaryOperation.values())
					.flatMap(op -> Stream
							.of(
								Arguments.of(op, 'A', 'B'),
								Arguments.of(op, 'x', 'y')
							));
		}

		@ParameterizedTest
		@MethodSource("charsOperationProvider")
		void charOperandsTest(BinaryOperation op, char left, char right) {
			if (BinaryOperation.ELEMENTWISE_DIVIDE.equals(op)) {
				assertThrows(NaftahBugError.class, () -> op.apply(left, right));
			}
			else {
				Object result = op.apply(left, right);
				assertTrue(result instanceof Character || result instanceof Boolean || result instanceof Number);
			}
		}
	}

	@Nested
	class BooleansOperationTests {
		static Stream<Arguments> booleansOperationProvider() {
			return Stream
					.of(BinaryOperation.values())
					.flatMap(op -> Stream
							.of(
								Arguments.of(op, true, false),
								Arguments.of(op, false, false)
							));
		}

		@ParameterizedTest
		@MethodSource("booleansOperationProvider")
		void booleanOperandsTest(BinaryOperation op, boolean left, boolean right) {
			if ((BinaryOperation.DIVIDE.equals(op) || BinaryOperation.MODULO.equals(op)) && !right) {
				assertThrows(ArithmeticException.class, () -> op.apply(left, right));
			}
			else {
				Object result = op.apply(left, right);
				assertTrue(result instanceof Boolean || result instanceof Number);
			}
		}
	}

	@Nested
	class StringsOperationTests {
		static Stream<Arguments> stringsOperationProvider() {
			return Stream
					.of(BinaryOperation.values())
					.flatMap(op -> Stream
							.of(
								Arguments.of(op, "abc", "xyz"),
								Arguments.of(op, "123", "456")
							));
		}

		@ParameterizedTest
		@MethodSource("stringsOperationProvider")
		void stringOperandsTest(BinaryOperation op, String left, String right) {
			Object result = op.apply(left, right);
			assertNotNull(result);
			assertTrue(result instanceof String || result instanceof Boolean || result.getClass().isArray());
		}
	}

	@Nested
	class NumberAndObjectOperationTests {
		static Object invalid = new Object();

		static Stream<Arguments> objectAndNumberOperationProvider() {
			return Stream
					.of(BinaryOperation.values())
					.flatMap(op -> Stream
							.of(
								Arguments.of(op, "abc", 3),
								Arguments.of(op, true, 1),
								Arguments.of(op, 'Z', 2),
								Arguments.of(op, invalid, 1)
							));
		}

		static Stream<Arguments> numberAndObjectOperationProvider() {
			return objectAndNumberOperationProvider()
					.map(args -> Arguments.of(args.get()[0], args.get()[2], args.get()[1]));
		}

		@ParameterizedTest
		@MethodSource("objectAndNumberOperationProvider")
		void objectAndNumberOperandsTest(BinaryOperation op, Object left, Number right) {
			if (invalid.equals(left)) {
				assertThrows(NaftahBugError.class, () -> op.apply(left, right));
			}
			else {
				assertNotNull(op.apply(left, right));
			}
		}

		@ParameterizedTest
		@MethodSource("numberAndObjectOperationProvider")
		void numberAndObjectOperandsTest(BinaryOperation op, Number left, Object right) {
			if (invalid.equals(right)) {
				assertThrows(NaftahBugError.class, () -> op.apply(left, right));
			}
			else if (BinaryOperation.ELEMENTWISE_DIVIDE.equals(op) && (right instanceof String string && string
					.codePoints()
					.sum() > 31 || right instanceof Character character && character > 31)) {
						assertThrows(NaftahBugError.class, () -> op.apply(left, right));
					}
			else {
				assertNotNull(op.apply(left, right));
			}
		}
	}
}
