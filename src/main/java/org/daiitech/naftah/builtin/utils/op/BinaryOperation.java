package org.daiitech.naftah.builtin.utils.op;

import java.util.Objects;

import org.daiitech.naftah.builtin.lang.NaN;
import org.daiitech.naftah.builtin.lang.None;
import org.daiitech.naftah.builtin.utils.NumberUtils;
import org.daiitech.naftah.builtin.utils.StringUtils;
import org.daiitech.naftah.errors.NaftahBugError;

import static org.daiitech.naftah.builtin.utils.ObjectUtils.booleanToInt;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.getNaftahType;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.intToBoolean;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.isTruthy;
import static org.daiitech.naftah.builtin.utils.StringUtils.charWiseModulo;
import static org.daiitech.naftah.builtin.utils.StringUtils.stringToInt;
import static org.daiitech.naftah.parser.DefaultNaftahParserVisitor.PARSER_VOCABULARY;
import static org.daiitech.naftah.utils.reflect.ClassUtils.getQualifiedName;

/**
 * Represents binary operations in the Naftah language.
 * <p>
 * This enum implements {@link Operation} and defines various
 * overloaded {@code apply} methods to perform operations on
 * different combinations of operand types including numbers,
 * characters, booleans, and strings.
 * </p>
 * <p>
 * Subclasses/enums implementing this must provide implementations
 * for applying the operation to {@link Number} operands, and
 * for mixing {@code Number} with {@code Object} and {@code String} operands.
 * </p>
 *
 * <p>Utility conversions are performed internally, such as converting booleans
 * to integers and characters to integers to unify the operation logic.</p>
 *
 * @author Chakib Daii
 */
public enum BinaryOperation implements Operation {
	// Logical
	/**
	 * Logical AND operation.
	 * <p>
	 * Applies short-circuit evaluation:
	 * <ul>
	 * <li>If the left operand is "truthy", returns the right operand.</li>
	 * <li>If the left operand is "falsy", returns the left operand.</li>
	 * </ul>
	 * <p>
	 * Supports numbers, booleans, characters, and strings.
	 * Treats values using custom "truthy" rules via {@code isTruthy()}.
	 */
	AND("AND") {
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Number apply(Number left, Number right) {
			return isTruthy(left) ? right : left;
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Number left, Object right) {
			return applyLogical(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Object left, Number right) {
			return applyLogical(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String apply(String left, String right) {
			return isTruthy(left) ? right : left;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object handleFalsy(Object left, Object right) {
			return isTruthy(left) ? right : left;
		}
	},

	/**
	 * Logical OR operation.
	 * <p>
	 * Applies short-circuit evaluation:
	 * <ul>
	 * <li>If the left operand is "truthy", returns the left operand.</li>
	 * <li>If the left operand is "falsy", returns the right operand.</li>
	 * </ul>
	 * <p>
	 * Supports numbers, booleans, characters, and strings.
	 * Treats values using custom "truthy" rules via {@code isTruthy()}.
	 */
	OR("OR") {
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Number apply(Number left, Number right) {
			return isTruthy(left) ? left : right;
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Number left, Object right) {
			return applyLogical(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Object left, Number right) {
			return applyLogical(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String apply(String left, String right) {
			return isTruthy(left) ? left : right;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object handleFalsy(Object left, Object right) {
			return isTruthy(left) ? left : right;
		}
	},

	// Arithmetic
	/**
	 * Represents the addition operation (+).
	 * Supports adding numbers, concatenating strings, and converting
	 * booleans and characters appropriately during addition.
	 */
	ADD("PLUS") {
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Number apply(Number left, Number right) {
			return NumberUtils.add(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Number left, Object right) {
			return applyArithmetic(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Object left, Number right) {
			return applyArithmetic(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String apply(String left, String right) {
			return StringUtils.add(left, right);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object handleFalsy(Object left, Object right) {
			if (left instanceof String || right instanceof String) {
				return String.valueOf(left) + right;
			}
			return handleFalsyArithmetic(left, right);
		}
	},

	/**
	 * Represents the subtraction operation (-).
	 * Supports subtracting numbers and converting booleans and characters
	 * appropriately during subtraction.
	 */
	SUBTRACT("MINUS") {
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Number apply(Number left, Number right) {
			return NumberUtils.subtract(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Number left, Object right) {
			return applyArithmetic(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Object left, Number right) {
			return applyArithmetic(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String apply(String left, String right) {
			return StringUtils.subtract(left, right);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object handleFalsy(Object left, Object right) {
			return handleFalsyArithmetic(left, right);
		}
	},

	/**
	 * Represents the multiplication operation (*).
	 * Supports multiplying numbers and converting booleans and characters
	 * appropriately during multiplication.
	 */
	MULTIPLY("MUL") {
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Number apply(Number left, Number right) {
			return NumberUtils.multiply(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Number left, Object right) {
			return applyArithmetic(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Object left, Number right) {
			if (left instanceof Boolean aBoolean) {
				return intToBoolean(((Number) apply((boolean) aBoolean, right)).intValue());
			}
			else if (left instanceof Character character) {
				return new String(new char[right.intValue()]).replace('\0', character);
			}
			else if (left instanceof String string) {
				return StringUtils.multiply(string, right.intValue());
			}
			throw BinaryOperation.newNaftahBugError(this, left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String apply(String left, String right) {
			return StringUtils.charWiseMultiply(left, right);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object handleFalsy(Object left, Object right) {
			return handleFalsyArithmetic(left, right);
		}
	},

	/**
	 * Represents the division operation (/).
	 * Supports dividing numbers and converting booleans and characters
	 * appropriately during division.
	 */
	DIVIDE("DIV") {
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Number apply(Number left, Number right) {
			return NumberUtils.divide(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Number left, Object right) {
			return applyArithmetic(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Object left, Number right) {
			if (left instanceof Boolean aBoolean) {
				return intToBoolean(((Number) apply((boolean) aBoolean, right)).intValue());
			}
			else if (left instanceof Character character) {
				return (char) ((Number) apply((char) character, right)).intValue();
			}
			else if (left instanceof String string) {
				return StringUtils.divide(string, right.intValue());
			}
			throw BinaryOperation.newNaftahBugError(this, left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String[] apply(String left, String right) {
			return StringUtils.divide(left, right);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object handleFalsy(Object left, Object right) {
			return handleFalsyArithmetic(left, right);
		}
	},

	/**
	 * Represents the modulo operation (%).
	 * Calculates the remainder of division between two numeric operands.
	 * Supports conversion of booleans and characters to numbers for the operation.
	 */
	MODULO("MOD") {
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Number apply(Number left, Number right) {
			return NumberUtils.modulo(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Number left, Object right) {
			return applyArithmetic(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Object left, Number right) {
			return applyArithmetic(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(String left, String right) {
			return charWiseModulo(left, right);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object handleFalsy(Object left, Object right) {
			return handleFalsyArithmetic(left, right);
		}
	},

	// Comparison
	/**
	 * Represents the greater-than comparison operation (>).
	 * Compares two numeric values or compatible types and returns a boolean indicating
	 * whether the left operand is greater than the right operand.
	 * Supports conversions from boolean and character types to numbers.
	 */
	GREATER_THAN("GT") {
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Boolean apply(Number left, Number right) {
			return NumberUtils.compare(left, right) > 0;
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Number left, Object right) {
			return applyLogical(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Object left, Number right) {
			return applyLogical(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Boolean apply(String left, String right) {
			return StringUtils.compare(left, right) > 0;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object handleFalsy(Object left, Object right) {
			return isTruthy(left);
		}
	},

	/**
	 * Represents the greater-than-or-equal-to comparison operation (>=).
	 * Compares two numeric values or compatible types and returns a boolean indicating
	 * whether the left operand is greater than or equal to the right operand.
	 * Supports conversions from boolean and character types to numbers.
	 */
	GREATER_THAN_EQUALS("GE") {
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Boolean apply(Number left, Number right) {
			return NumberUtils.compare(left, right) >= 0;
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Number left, Object right) {
			return applyLogical(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Object left, Number right) {
			return applyLogical(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Boolean apply(String left, String right) {
			return StringUtils.compare(left, right) >= 0;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object handleFalsy(Object left, Object right) {
			return isTruthy(left);
		}
	},

	/**
	 * Represents the less-than comparison operation (&lt;).
	 * Compares two numeric values or compatible types and returns a boolean indicating
	 * whether the left operand is less than the right operand.
	 * Supports conversions from boolean and character types to numbers.
	 */
	LESS_THAN("LT") {
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Boolean apply(Number left, Number right) {
			return NumberUtils.compare(left, right) < 0;
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Number left, Object right) {
			return applyLogical(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Object left, Number right) {
			return applyLogical(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Boolean apply(String left, String right) {
			return StringUtils.compare(left, right) < 0;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object handleFalsy(Object left, Object right) {
			return isTruthy(right);
		}
	},

	/**
	 * Represents the less-than-or-equal-to comparison operation (&lt;=).
	 * Compares two numeric values or compatible types and returns a boolean indicating
	 * whether the left operand is less than or equal to the right operand.
	 * Supports conversions from boolean and character types to numbers.
	 */
	LESS_THAN_EQUALS("LE") {
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Boolean apply(Number left, Number right) {
			return NumberUtils.compare(left, right) <= 0;
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Number left, Object right) {
			return applyLogical(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Object left, Number right) {
			return applyLogical(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Boolean apply(String left, String right) {
			return StringUtils.compare(left, right) <= 0;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object handleFalsy(Object left, Object right) {
			return isTruthy(right);
		}
	},

	/**
	 * Represents the equality comparison operation (==).
	 * Compares two operands for equality and returns a boolean result.
	 * Supports numeric, boolean, character, and string comparisons with necessary conversions.
	 */
	EQUALS("EQ") {
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Boolean apply(Number left, Number right) {
			return NumberUtils.equals(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Number left, Object right) {
			return applyLogical(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Object left, Number right) {
			return applyLogical(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Boolean apply(String left, String right) {
			return StringUtils.equals(left, right);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object handleFalsy(Object left, Object right) {
			if (NaN.isNaN(left) || NaN.isNaN(right)) {
				return false;
			}
			if (None.isNone(left) && None.isNone(right)) {
				return true;
			}
			if (None.isNone(left) || None.isNone(right)) {
				return false;
			}
			return apply(left, right);
		}
	},

	/**
	 * Represents the inequality comparison operation (!=).
	 * Compares two operands for inequality and returns a boolean result.
	 * Supports numeric, boolean, character, and string comparisons with necessary conversions.
	 */
	NOT_EQUALS("NEQ") {
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Boolean apply(Number left, Number right) {
			return NumberUtils.compare(left, right) != 0;
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Number left, Object right) {
			return applyLogical(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Object left, Number right) {
			return applyLogical(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Boolean apply(String left, String right) {
			return !StringUtils.equals(left, right);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object handleFalsy(Object left, Object right) {
			return !(boolean) EQUALS.apply(left, right);
		}
	},

	// Bitwise
	/**
	 * Represents the bitwise AND operation (&amp;).
	 * Performs a bitwise AND between two operands.
	 * Supports numeric and character operands with appropriate conversions.
	 */
	BITWISE_AND("BITWISE_AND") {
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Number apply(Number left, Number right) {
			return NumberUtils.and(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Number left, Object right) {
			return applyArithmetic(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Object left, Number right) {
			return applyArithmetic(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String apply(String left, String right) {
			return StringUtils.and(left, right);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object handleFalsy(Object left, Object right) {
			return handleFalsyBitOrElementWiseArithmetic(left, right);
		}

	},

	/**
	 * Represents the bitwise OR operation (|).
	 * Performs a bitwise OR between two operands.
	 * Supports numeric and character operands with appropriate conversions.
	 */
	BITWISE_OR("BITWISE_OR") {
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Number apply(Number left, Number right) {
			return NumberUtils.or(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Number left, Object right) {
			return applyArithmetic(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Object left, Number right) {
			return applyArithmetic(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String apply(String left, String right) {
			return StringUtils.or(left, right);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object handleFalsy(Object left, Object right) {
			return handleFalsyBitOrElementWiseArithmetic(left, right);
		}
	},

	/**
	 * Represents the bitwise XOR (exclusive OR) operation (^).
	 * Performs a bitwise exclusive OR between two operands.
	 * Supports numeric and character operands with appropriate conversions.
	 */
	BITWISE_XOR("BITWISE_XOR") {
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Number apply(Number left, Number right) {
			return NumberUtils.xor(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Number left, Object right) {
			return applyArithmetic(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Object left, Number right) {
			return applyArithmetic(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String apply(String left, String right) {
			return StringUtils.xor(left, right);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object handleFalsy(Object left, Object right) {
			return handleFalsyBitOrElementWiseArithmetic(left, right);
		}
	},

	/**
	 * Represents element-wise addition.
	 * Applies addition operation to each corresponding element in collections or arrays.
	 * Supports element-wise combination of compatible data structures.
	 */
	ELEMENTWISE_ADD("ELEMENTWISE_PLUS") {
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Number apply(Number left, Number right) {
			return NumberUtils.xor(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Number left, Object right) {
			return applyArithmetic(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Object left, Number right) {
			return applyArithmetic(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String apply(String left, String right) {
			return StringUtils.charWiseAdd(left, right);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object handleFalsy(Object left, Object right) {
			return handleFalsyBitOrElementWiseArithmetic(left, right);
		}
	},

	/**
	 * Represents element-wise subtraction.
	 * Performs subtraction on corresponding elements in collections or arrays.
	 * Supports element-wise operations on compatible data structures.
	 */
	ELEMENTWISE_SUBTRACT("ELEMENTWISE_MINUS") {
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Number apply(Number left, Number right) {
			return NumberUtils.xor(left, NumberUtils.not(right));
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Number left, Object right) {
			return applyArithmetic(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Object left, Number right) {
			return applyArithmetic(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String apply(String left, String right) {
			return StringUtils.charWiseSubtract(left, right);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object handleFalsy(Object left, Object right) {
			return handleFalsyBitOrElementWiseArithmetic(left, right);
		}
	},

	/**
	 * Represents element-wise multiplication.
	 * Performs multiplication on corresponding elements in collections or arrays.
	 * Supports element-wise operations on compatible data structures.
	 */
	ELEMENTWISE_MULTIPLY("ELEMENTWISE_MUL") {
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Number apply(Number left, Number right) {
			return NumberUtils.and(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Number left, Object right) {
			return applyArithmetic(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Object left, Number right) {
			return applyArithmetic(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String apply(String left, String right) {
			return StringUtils.charWiseMultiply(left, right);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object handleFalsy(Object left, Object right) {
			return handleFalsyBitOrElementWiseArithmetic(left, right);
		}
	},

	/**
	 * Represents element-wise division.
	 * Performs division on corresponding elements in collections or arrays.
	 * Supports element-wise operations on compatible data structures.
	 */
	ELEMENTWISE_DIVIDE("ELEMENTWISE_DIV") {
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Number apply(Number left, Number right) {
			return NumberUtils.shiftRight(left, right.intValue());
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Number left, Object right) {
			return applyArithmetic(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Object left, Number right) {
			return applyArithmetic(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String apply(String left, String right) {
			return StringUtils.charWiseDivide(left, right);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object handleFalsy(Object left, Object right) {
			return handleFalsyBitOrElementWiseArithmetic(left, right);
		}
	},

	/**
	 * Represents element-wise modulo operation.
	 * Performs modulo on corresponding elements in collections or arrays.
	 * Supports element-wise operations on compatible data structures.
	 */
	ELEMENTWISE_MODULO("ELEMENTWISE_MOD") {
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Number apply(Number left, Number right) {
			return NumberUtils.and(left, NumberUtils.subtract(right, 1));
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Number left, Object right) {
			return applyArithmetic(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Object left, Number right) {
			return applyArithmetic(left, right);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String apply(String left, String right) {
			return StringUtils.charWiseModulo(left, right);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object handleFalsy(Object left, Object right) {
			return handleFalsyBitOrElementWiseArithmetic(left, right);
		}
	};

	/**
	 * The symbolic representation of the binary operator,
	 */
	private final String op;

	/**
	 * Constructs a {@code BinaryOperation} enum constant with its symbolic operator.
	 *
	 * @param op the operator symbol (e.g., "+", "-", "*")
	 */
	BinaryOperation(String op) {
		this.op = op;
	}

	/**
	 * Creates a new {@link NaftahBugError} indicating that the given binary operation
	 * is not supported for the provided operand types.
	 *
	 * @param binaryOperation the binary operation that was attempted
	 * @param left            the left operand involved in the operation
	 * @param right           the right operand involved in the operation
	 * @return a new {@code NaftahBugError} describing the unsupported operation and operand types
	 */
	public static NaftahBugError newNaftahBugError(Operation binaryOperation, Object left, Object right) {
		return new NaftahBugError("العملية '%s' غير مدعومة للنوعين: '%s' و'%s'."
				.formatted( binaryOperation,
							Objects.isNull(PARSER_VOCABULARY) ?
									getQualifiedName(left.getClass().getName()) :
									getNaftahType(PARSER_VOCABULARY, left.getClass()),
							Objects.isNull(PARSER_VOCABULARY) ?
									getQualifiedName(right.getClass().getName()) :
									getNaftahType(PARSER_VOCABULARY, right.getClass())));
	}

	/**
	 * Returns the {@code BinaryOperation} enum constant corresponding to the given operator symbol.
	 *
	 * @param op the operator symbol
	 * @return the matching {@code BinaryOperation}
	 * @throws NaftahBugError if no matching enum constant is found
	 */
	public static BinaryOperation of(String op) {
		for (BinaryOperation operator : BinaryOperation.values()) {
			if (operator.op.equals(op)) {
				return operator;
			}
		}

		throw Operation
				.newNaftahBugNoEnumValueError(
												BinaryOperation.class,
												op);
	}

	/**
	 * Applies the binary operation to two dynamic operands.
	 * <p>
	 * Supports combinations of the following types:
	 * <ul>
	 * <li>Number, Boolean, Character, String</li>
	 * <li>Cross-type operations (e.g., Number + String, Boolean + Number, etc.)</li>
	 * </ul>
	 * If the operand types are not supported, throws a {@link NaftahBugError}.
	 *
	 * @param left  the left operand
	 * @param right the right operand
	 * @return the result of applying the operation
	 * @throws NaftahBugError if the operand types are unsupported
	 */
	public Object apply(Object left, Object right) {
		if ((NaN.isNaN(left) || NaN.isNaN(right)) || (None.isNone(left) || None.isNone(right))) {
			return handleFalsy(left, right);
		}
		// Number vs Number
		if (left instanceof Number number && right instanceof Number number1) {
			return apply(number, number1);
		}

		// Number vs Boolean
		if (left instanceof Number number && right instanceof Boolean aBoolean) {
			return apply(number, aBoolean);
		}

		// Number vs Character
		if (left instanceof Number number && right instanceof Character character) {
			return apply(number, character);
		}

		// Number vs String
		if (left instanceof Number number && right instanceof String string) {
			return apply(number, string);
		}

		// Boolean vs Number
		if (left instanceof Boolean aBoolean && right instanceof Number number) {
			return apply(aBoolean, number);
		}

		// Character vs Number
		if (left instanceof Character character && right instanceof Number number) {
			return apply(character, number);
		}

		// String vs Number
		if (left instanceof String string && right instanceof Number number) {
			return apply(string, number);
		}

		// Boolean vs Boolean
		if (left instanceof Boolean aBoolean && right instanceof Boolean aBoolean1) {
			return apply(aBoolean.booleanValue(), aBoolean1.booleanValue());
		}

		// Character vs Character
		if (left instanceof Character character && right instanceof Character character1) {
			return apply(character.charValue(), character1.charValue());
		}

		// String vs String
		if (left instanceof String s && right instanceof String s1) {
			return apply(s, s1);
		}

		// String vs Character
		if (left instanceof String s && right instanceof Character character) {
			return apply(s, String.valueOf(character));
		}

		// Character vs String
		if (left instanceof Character character && right instanceof String s) {
			return apply(String.valueOf(character), s);
		}

		throw BinaryOperation.newNaftahBugError(this, left, right);
	}

	/**
	 * Handles the case where one or both operands are "falsy" (e.g., {@code null}, {@code None}, or {@code NaN}).
	 * <p>
	 * This method must be implemented by each binary operation to define custom handling logic for falsy values.
	 *
	 * @param left  the left operand
	 * @param right the right operand
	 * @return the result of the operation after handling falsy values
	 */
	protected abstract Object handleFalsy(Object left, Object right);

	/**
	 * Default handler for falsy values in bitwise or element-wise arithmetic operations.
	 * <p>
	 * If either operand is {@code NaN} or {@code None}, it is treated as zero.
	 *
	 * @param left  the left operand
	 * @param right the right operand
	 * @return the result of applying the operation with normalized values
	 */
	protected Object handleFalsyBitOrElementWiseArithmetic(Object left, Object right) {
		if (None.isNone(left) || NaN.isNaN(left)) {
			left = 0;
		}
		if (None.isNone(right) || NaN.isNaN(right)) {
			right = 0;
		}
		return apply(left, right);
	}

	/**
	 * Default handler for falsy values in basic arithmetic operations.
	 * <p>
	 * - If either operand is {@code NaN}, the result is {@code NaN}.
	 * - If either operand is {@code None}, it is treated as zero.
	 *
	 * @param left  the left operand
	 * @param right the right operand
	 * @return the result of the arithmetic operation or {@code NaN}
	 */
	protected Object handleFalsyArithmetic(Object left, Object right) {
		if (NaN.isNaN(left) || NaN.isNaN(right)) {
			return NaN.get();
		}
		if (None.isNone(left)) {
			left = 0;
		}
		if (None.isNone(right)) {
			right = 0;
		}
		return apply(left, right);
	}

	/**
	 * Applies the binary operation to two {@link Number} operands.
	 *
	 * @param left  the left operand
	 * @param right the right operand
	 * @return the result of the operation
	 */
	// TODO : minimize the overhead of creating dynamic number from number everytime we perform operation by creating
	//  and using dynamic number
	protected abstract Object apply(Number left, Number right);

	/**
	 * Applies the operation to a {@link Number} left operand and a {@code char} right operand.
	 *
	 * @param left  the left operand
	 * @param right the right operand as char
	 * @return the result of the operation
	 */
	protected Object apply(Number left, char right) {
		return apply(left, (int) right);
	}

	/**
	 * Applies the operation to a {@code char} left operand and a {@link Number} right operand.
	 *
	 * @param left  the left operand as char
	 * @param right the right operand
	 * @return the result of the operation
	 */
	protected Object apply(char left, Number right) {
		return apply((int) left, right);
	}

	/**
	 * Applies the operation to a {@link Number} left operand and a {@code boolean} right operand.
	 *
	 * @param left  the left operand
	 * @param right the right operand as boolean
	 * @return the result of the operation
	 */
	protected Object apply(Number left, boolean right) {
		return apply(left, booleanToInt(right));
	}

	/**
	 * Applies the operation to a {@code boolean} left operand and a {@link Number} right operand.
	 *
	 * @param left  the left operand as boolean
	 * @param right the right operand
	 * @return the result of the operation
	 */
	protected Object apply(boolean left, Number right) {
		return apply(booleanToInt(left), right);
	}

	/**
	 * Applies the operation to a {@link Number} left operand and a generic {@link Object} right operand.
	 *
	 * @param left  the left operand
	 * @param right the right operand
	 * @return the result of the operation
	 */
	protected abstract Object apply(Number left, Object right);

	/**
	 * Applies the operation to a generic {@link Object} left operand and a {@link Number} right operand.
	 *
	 * @param left  the left operand
	 * @param right the right operand
	 * @return the result of the operation
	 */
	protected abstract Object apply(Object left, Number right);

	/**
	 * Applies the operation to two {@code char} operands.
	 * <p>
	 * If the result is a {@link Number}, it will be cast back to {@code char}.
	 * If the result is a {@link Boolean}, it will be returned as is.
	 * Otherwise, returns the raw result.
	 *
	 * @param left  the left char operand
	 * @param right the right char operand
	 * @return the result of the operation
	 */
	protected Object apply(char left, char right) {
		var result = apply((int) left, (int) right);
		if (result instanceof Number number) {
			return (char) number.intValue();
		}
		else if (result instanceof Boolean aBoolean) {
			return aBoolean;
		}
		return result;
	}

	/**
	 * Applies the operation to two {@code boolean} operands.
	 * <p>
	 * If the result is a {@link Number}, it will be converted back to {@code boolean}.
	 * If the result is a {@link Boolean}, it will be returned as is.
	 * Otherwise, returns the raw result.
	 *
	 * @param left  the left boolean operand
	 * @param right the right boolean operand
	 * @return the result of the operation
	 */
	protected Object apply(boolean left, boolean right) {
		var result = apply(booleanToInt(left), booleanToInt(right));
		if (result instanceof Number number) {
			return intToBoolean(number.intValue());
		}
		else if (result instanceof Boolean aBoolean) {
			return aBoolean;
		}
		return result;
	}

	/**
	 * Applies the operation to two {@link String} operands.
	 *
	 * @param left  the left string operand
	 * @param right the right string operand
	 * @return the result of the operation
	 */
	protected abstract Object apply(String left, String right);

	/**
	 * Applies a logical operation where the left operand is a {@link Number}.
	 * Supports logical interaction with Boolean, Character, and String.
	 *
	 * @param left  the left operand (Number)
	 * @param right the right operand (Boolean, Character, or String)
	 * @return the result of the logical operation
	 * @throws NaftahBugError if the operand types are unsupported
	 */
	protected Object applyLogical(Number left, Object right) {
		if (right instanceof Boolean aBoolean) {
			return apply(left, (boolean) aBoolean);
		}
		else if (right instanceof Character character) {
			return apply(left, (char) character);
		}
		else if (right instanceof String string) {
			return apply(left, stringToInt(string));
		}
		throw BinaryOperation.newNaftahBugError(this, left, right);
	}

	/**
	 * Applies a logical operation where the right operand is a {@link Number}.
	 * Supports logical interaction with Boolean, Character, and String.
	 *
	 * @param left  the left operand (Boolean, Character, or String)
	 * @param right the right operand (Number)
	 * @return the result of the logical operation
	 * @throws NaftahBugError if the operand types are unsupported
	 */
	protected Object applyLogical(Object left, Number right) {
		if (left instanceof Boolean aBoolean) {
			return apply((boolean) aBoolean, right);
		}
		else if (left instanceof Character character) {
			return apply((char) character, right);
		}
		else if (left instanceof String string) {
			return apply(stringToInt(string), right);
		}
		throw BinaryOperation.newNaftahBugError(this, left, right);
	}

	/**
	 * Applies an arithmetic operation where the left operand is a {@link Number}.
	 * Casts the result back to appropriate logical type if necessary.
	 *
	 * @param left  the left operand (Number)
	 * @param right the right operand (Boolean, Character, or String)
	 * @return the result of the arithmetic operation
	 * @throws NaftahBugError if the operand types are unsupported
	 */
	protected Object applyArithmetic(Number left, Object right) {
		if (right instanceof Boolean aBoolean) {
			return intToBoolean(((Number) apply(left, (boolean) aBoolean)).intValue());
		}
		else if (right instanceof Character character) {
			return (char) ((Number) apply(left, (char) character)).intValue();
		}
		else if (right instanceof String string) {
			return apply(left, stringToInt(string));
		}
		throw BinaryOperation.newNaftahBugError(this, left, right);
	}

	/**
	 * Applies an arithmetic operation where the right operand is a {@link Number}.
	 * Casts the result back to appropriate logical type if necessary.
	 *
	 * @param left  the left operand (Boolean, Character, or String)
	 * @param right the right operand (Number)
	 * @return the result of the arithmetic operation
	 * @throws NaftahBugError if the operand types are unsupported
	 */
	protected Object applyArithmetic(Object left, Number right) {
		if (left instanceof Boolean aBoolean) {
			return intToBoolean(((Number) apply((boolean) aBoolean, right)).intValue());
		}
		else if (left instanceof Character character) {
			return (char) ((Number) apply((char) character, right)).intValue();
		}
		else if (left instanceof String string) {
			return apply(string, right.toString());
		}
		throw BinaryOperation.newNaftahBugError(this, left, right);
	}

	/**
	 * Returns the string representation of the binary operator symbol.
	 *
	 * @return the operator symbol as a string
	 */
	@Override
	public String toString() {
		return op;
	}
}
