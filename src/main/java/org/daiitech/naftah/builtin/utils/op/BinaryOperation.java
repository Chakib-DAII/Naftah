package org.daiitech.naftah.builtin.utils.op;

import org.daiitech.naftah.builtin.utils.NumberUtils;
import org.daiitech.naftah.builtin.utils.StringUtils;
import org.daiitech.naftah.errors.NaftahBugError;

import static org.daiitech.naftah.builtin.utils.ObjectUtils.booleanToInt;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.intToBoolean;
import static org.daiitech.naftah.builtin.utils.StringUtils.charWiseModulo;
import static org.daiitech.naftah.builtin.utils.StringUtils.stringToInt;

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
	// Arithmetic
	/**
	 * Represents the addition operation (+).
	 * Supports adding numbers, concatenating strings, and converting
	 * booleans and characters appropriately during addition.
	 */
	ADD {
		@Override
		public Number apply(Number left, Number right) {
			return NumberUtils.add(left, right);
		}

		@Override
		public Object apply(Number left, Object right) {
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

		@Override
		public Object apply(Object left, Number right) {
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

		@Override
		public String apply(String left, String right) {
			return StringUtils.add(left, right);
		}
	},

	/**
	 * Represents the subtraction operation (-).
	 * Supports subtracting numbers and converting booleans and characters
	 * appropriately during subtraction.
	 */
	SUBTRACT {
		@Override
		public Number apply(Number left, Number right) {
			return NumberUtils.subtract(left, right);
		}

		@Override
		public Object apply(Number left, Object right) {
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

		@Override
		public Object apply(Object left, Number right) {
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

		@Override
		public String apply(String left, String right) {
			return StringUtils.subtract(left, right);
		}
	},

	/**
	 * Represents the multiplication operation (*).
	 * Supports multiplying numbers and converting booleans and characters
	 * appropriately during multiplication.
	 */
	MULTIPLY {
		@Override
		public Number apply(Number left, Number right) {
			return NumberUtils.multiply(left, right);
		}

		@Override
		public Object apply(Number left, Object right) {
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

		@Override
		public Object apply(Object left, Number right) {
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

		@Override
		public String apply(String left, String right) {
			return StringUtils.charWiseMultiply(left, right);
		}
	},

	/**
	 * Represents the division operation (/).
	 * Supports dividing numbers and converting booleans and characters
	 * appropriately during division.
	 */
	DIVIDE {
		@Override
		public Number apply(Number left, Number right) {
			return NumberUtils.divide(left, right);
		}

		@Override
		public Object apply(Number left, Object right) {
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

		@Override
		public Object apply(Object left, Number right) {
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

		@Override
		public String[] apply(String left, String right) {
			return StringUtils.divide(left, right);
		}
	},

	/**
	 * Represents the modulo operation (%).
	 * Calculates the remainder of division between two numeric operands.
	 * Supports conversion of booleans and characters to numbers for the operation.
	 */
	MODULO {
		@Override
		public Number apply(Number left, Number right) {
			return NumberUtils.modulo(left, right);
		}

		@Override
		public Object apply(Number left, Object right) {
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

		@Override
		public Object apply(Object left, Number right) {
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

		@Override
		public Object apply(String left, String right) {
			return charWiseModulo(left, right);
		}
	},

	// Comparison
	/**
	 * Represents the greater-than comparison operation (>).
	 * Compares two numeric values or compatible types and returns a boolean indicating
	 * whether the left operand is greater than the right operand.
	 * Supports conversions from boolean and character types to numbers.
	 */
	GREATER_THAN {
		@Override
		public Boolean apply(Number left, Number right) {
			return NumberUtils.compare(left, right) > 0;
		}

		@Override
		public Boolean apply(Number left, Object right) {
			if (right instanceof Boolean aBoolean) {
				return (Boolean) apply(left, (boolean) aBoolean);
			}
			else if (right instanceof Character character) {
				return (Boolean) apply(left, (char) character);
			}
			else if (right instanceof String string) {
				return apply(left, stringToInt(string));
			}
			throw BinaryOperation.newNaftahBugError(this, left, right);
		}

		@Override
		public Boolean apply(Object left, Number right) {
			if (left instanceof Boolean aBoolean) {
				return (Boolean) apply((boolean) aBoolean, right);
			}
			else if (left instanceof Character character) {
				return (Boolean) apply((char) character, right);
			}
			else if (left instanceof String string) {
				return apply(stringToInt(string), right);
			}
			throw BinaryOperation.newNaftahBugError(this, left, right);
		}

		@Override
		public Boolean apply(String left, String right) {
			return StringUtils.compare(left, right) > 0;
		}
	},

	/**
	 * Represents the greater-than-or-equal-to comparison operation (>=).
	 * Compares two numeric values or compatible types and returns a boolean indicating
	 * whether the left operand is greater than or equal to the right operand.
	 * Supports conversions from boolean and character types to numbers.
	 */
	GREATER_THAN_EQUALS {
		@Override
		public Boolean apply(Number left, Number right) {
			return NumberUtils.compare(left, right) >= 0;
		}

		@Override
		public Boolean apply(Number left, Object right) {
			if (right instanceof Boolean aBoolean) {
				return (Boolean) apply(left, (boolean) aBoolean);
			}
			else if (right instanceof Character character) {
				return (Boolean) apply(left, (char) character);
			}
			else if (right instanceof String string) {
				return apply(left, stringToInt(string));
			}
			throw BinaryOperation.newNaftahBugError(this, left, right);
		}

		@Override
		public Boolean apply(Object left, Number right) {
			if (left instanceof Boolean aBoolean) {
				return (Boolean) apply((boolean) aBoolean, right);
			}
			else if (left instanceof Character character) {
				return (Boolean) apply((char) character, right);
			}
			else if (left instanceof String string) {
				return apply(stringToInt(string), right);
			}
			throw BinaryOperation.newNaftahBugError(this, left, right);
		}

		@Override
		public Boolean apply(String left, String right) {
			return StringUtils.compare(left, right) >= 0;
		}
	},

	/**
	 * Represents the less-than comparison operation (<).
	 * Compares two numeric values or compatible types and returns a boolean indicating
	 * whether the left operand is less than the right operand.
	 * Supports conversions from boolean and character types to numbers.
	 */
	LESS_THAN {
		@Override
		public Boolean apply(Number left, Number right) {
			return NumberUtils.compare(left, right) < 0;
		}

		@Override
		public Boolean apply(Number left, Object right) {
			if (right instanceof Boolean aBoolean) {
				return (Boolean) apply(left, (boolean) aBoolean);
			}
			else if (right instanceof Character character) {
				return (Boolean) apply(left, (char) character);
			}
			else if (right instanceof String string) {
				return apply(left, stringToInt(string));
			}
			throw BinaryOperation.newNaftahBugError(this, left, right);
		}

		@Override
		public Boolean apply(Object left, Number right) {
			if (left instanceof Boolean aBoolean) {
				return (Boolean) apply((boolean) aBoolean, right);
			}
			else if (left instanceof Character character) {
				return (Boolean) apply((char) character, right);
			}
			else if (left instanceof String string) {
				return apply(stringToInt(string), right);
			}
			throw BinaryOperation.newNaftahBugError(this, left, right);
		}

		@Override
		public Boolean apply(String left, String right) {
			return StringUtils.compare(left, right) < 0;
		}
	},

	/**
	 * Represents the less-than-or-equal-to comparison operation (<=).
	 * Compares two numeric values or compatible types and returns a boolean indicating
	 * whether the left operand is less than or equal to the right operand.
	 * Supports conversions from boolean and character types to numbers.
	 */
	LESS_THAN_EQUALS {
		@Override
		public Boolean apply(Number left, Number right) {
			return NumberUtils.compare(left, right) <= 0;
		}

		@Override
		public Boolean apply(Number left, Object right) {
			if (right instanceof Boolean aBoolean) {
				return (Boolean) apply(left, (boolean) aBoolean);
			}
			else if (right instanceof Character character) {
				return (Boolean) apply(left, (char) character);
			}
			else if (right instanceof String string) {
				return apply(left, stringToInt(string));
			}
			throw BinaryOperation.newNaftahBugError(this, left, right);
		}

		@Override
		public Boolean apply(Object left, Number right) {
			if (left instanceof Boolean aBoolean) {
				return (Boolean) apply((boolean) aBoolean, right);
			}
			else if (left instanceof Character character) {
				return (Boolean) apply((char) character, right);
			}
			else if (left instanceof String string) {
				return apply(stringToInt(string), right);
			}
			throw BinaryOperation.newNaftahBugError(this, left, right);
		}

		@Override
		public Boolean apply(String left, String right) {
			return StringUtils.compare(left, right) <= 0;
		}
	},

	/**
	 * Represents the equality comparison operation (==).
	 * Compares two operands for equality and returns a boolean result.
	 * Supports numeric, boolean, character, and string comparisons with necessary conversions.
	 */
	EQUALS {
		@Override
		public Boolean apply(Number left, Number right) {
			return NumberUtils.equals(left, right);
		}

		@Override
		public Boolean apply(Number left, Object right) {
			if (right instanceof Boolean aBoolean) {
				return (Boolean) apply(left, (boolean) aBoolean);
			}
			else if (right instanceof Character character) {
				return (Boolean) apply(left, (char) character);
			}
			else if (right instanceof String string) {
				return apply(left, stringToInt(string));
			}
			throw BinaryOperation.newNaftahBugError(this, left, right);
		}

		@Override
		public Boolean apply(Object left, Number right) {
			if (left instanceof Boolean aBoolean) {
				return (Boolean) apply((boolean) aBoolean, right);
			}
			else if (left instanceof Character character) {
				return (Boolean) apply((char) character, right);
			}
			else if (left instanceof String string) {
				return apply(stringToInt(string), right);
			}
			throw BinaryOperation.newNaftahBugError(this, left, right);
		}

		@Override
		public Boolean apply(String left, String right) {
			return StringUtils.equals(left, right);
		}
	},

	/**
	 * Represents the inequality comparison operation (!=).
	 * Compares two operands for inequality and returns a boolean result.
	 * Supports numeric, boolean, character, and string comparisons with necessary conversions.
	 */
	NOT_EQUALS {
		@Override
		public Boolean apply(Number left, Number right) {
			return NumberUtils.compare(left, right) != 0;
		}

		@Override
		public Boolean apply(Number left, Object right) {
			if (right instanceof Boolean aBoolean) {
				return (Boolean) apply(left, (boolean) aBoolean);
			}
			else if (right instanceof Character character) {
				return (Boolean) apply(left, (char) character);
			}
			else if (right instanceof String string) {
				return apply(left, stringToInt(string));
			}
			throw BinaryOperation.newNaftahBugError(this, left, right);
		}

		@Override
		public Boolean apply(Object left, Number right) {
			if (left instanceof Boolean aBoolean) {
				return (Boolean) apply((boolean) aBoolean, right);
			}
			else if (left instanceof Character character) {
				return (Boolean) apply((char) character, right);
			}
			else if (left instanceof String string) {
				return apply(stringToInt(string), right);
			}
			throw BinaryOperation.newNaftahBugError(this, left, right);
		}

		@Override
		public Boolean apply(String left, String right) {
			return !StringUtils.equals(left, right);
		}
	},

	// Bitwise
	/**
	 * Represents the bitwise AND operation (&).
	 * Performs a bitwise AND between two operands.
	 * Supports numeric and character operands with appropriate conversions.
	 */
	BITWISE_AND {
		@Override
		public Number apply(Number left, Number right) {
			return NumberUtils.and(left, right);
		}

		@Override
		public Object apply(Number left, Object right) {
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

		@Override
		public Object apply(Object left, Number right) {
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

		@Override
		public String apply(String left, String right) {
			return StringUtils.and(left, right);
		}
	},

	/**
	 * Represents the bitwise OR operation (|).
	 * Performs a bitwise OR between two operands.
	 * Supports numeric and character operands with appropriate conversions.
	 */
	BITWISE_OR {
		@Override
		public Number apply(Number left, Number right) {
			return NumberUtils.or(left, right);
		}

		@Override
		public Object apply(Number left, Object right) {
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

		@Override
		public Object apply(Object left, Number right) {
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

		@Override
		public String apply(String left, String right) {
			return StringUtils.or(left, right);
		}
	},

	/**
	 * Represents the bitwise XOR (exclusive OR) operation (^).
	 * Performs a bitwise exclusive OR between two operands.
	 * Supports numeric and character operands with appropriate conversions.
	 */
	BITWISE_XOR {
		@Override
		public Number apply(Number left, Number right) {
			return NumberUtils.xor(left, right);
		}

		@Override
		public Object apply(Number left, Object right) {
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

		@Override
		public Object apply(Object left, Number right) {
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

		@Override
		public String apply(String left, String right) {
			return StringUtils.xor(left, right);
		}
	},

	/**
	 * Represents element-wise addition.
	 * Applies addition operation to each corresponding element in collections or arrays.
	 * Supports element-wise combination of compatible data structures.
	 */
	ELEMENTWISE_ADD {
		@Override
		public Number apply(Number left, Number right) {
			return NumberUtils.xor(left, right);
		}

		@Override
		public Object apply(Number left, Object right) {
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

		@Override
		public Object apply(Object left, Number right) {
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

		@Override
		public String apply(String left, String right) {
			return StringUtils.charWiseAdd(left, right);
		}
	},

	/**
	 * Represents element-wise subtraction.
	 * Performs subtraction on corresponding elements in collections or arrays.
	 * Supports element-wise operations on compatible data structures.
	 */
	ELEMENTWISE_SUBTRACT {
		@Override
		public Number apply(Number left, Number right) {
			return NumberUtils.xor(left, NumberUtils.not(right));
		}

		@Override
		public Object apply(Number left, Object right) {
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

		@Override
		public Object apply(Object left, Number right) {
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

		@Override
		public String apply(String left, String right) {
			return StringUtils.charWiseSubtract(left, right);
		}
	},

	/**
	 * Represents element-wise multiplication.
	 * Performs multiplication on corresponding elements in collections or arrays.
	 * Supports element-wise operations on compatible data structures.
	 */
	ELEMENTWISE_MULTIPLY {
		@Override
		public Number apply(Number left, Number right) {
			return NumberUtils.and(left, right);
		}

		@Override
		public Object apply(Number left, Object right) {
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

		@Override
		public Object apply(Object left, Number right) {
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

		@Override
		public String apply(String left, String right) {
			return StringUtils.charWiseMultiply(left, right);
		}
	},

	/**
	 * Represents element-wise division.
	 * Performs division on corresponding elements in collections or arrays.
	 * Supports element-wise operations on compatible data structures.
	 */
	ELEMENTWISE_DIVIDE {
		@Override
		public Number apply(Number left, Number right) {
			return NumberUtils.shiftRight(left, right.intValue());
		}

		@Override
		public Object apply(Number left, Object right) {
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

		@Override
		public Object apply(Object left, Number right) {
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

		@Override
		public String apply(String left, String right) {
			return StringUtils.charWiseDivide(left, right);
		}
	},

	/**
	 * Represents element-wise modulo operation.
	 * Performs modulo on corresponding elements in collections or arrays.
	 * Supports element-wise operations on compatible data structures.
	 */
	ELEMENTWISE_MODULO {
		@Override
		public Number apply(Number left, Number right) {
			return NumberUtils.and(left, NumberUtils.subtract(right, 1));
		}

		@Override
		public Object apply(Number left, Object right) {
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

		@Override
		public Object apply(Object left, Number right) {
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

		@Override
		public String apply(String left, String right) {
			return StringUtils.charWiseModulo(left, right);
		}
	};

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
		return new NaftahBugError("العملية '%s' غير مدعومة للنوعين: '%s' و'%s'.".formatted(binaryOperation, left.getClass(), right.getClass()));
	}

	/**
	 * Applies the binary operation to two {@link Number} operands.
	 *
	 * @param left  the left operand
	 * @param right the right operand
	 * @return the result of the operation
	 */
	// TODO : minimize the overhead of creating dynamic number from number everytime we perform operation by creating and using dynamic number
	public abstract Object apply(Number left, Number right);

	/**
	 * Applies the operation to a {@link Number} left operand and a {@code char} right operand.
	 *
	 * @param left  the left operand
	 * @param right the right operand as char
	 * @return the result of the operation
	 */
	public Object apply(Number left, char right) {
		return apply(left, (int) right);
	}

	/**
	 * Applies the operation to a {@code char} left operand and a {@link Number} right operand.
	 *
	 * @param left  the left operand as char
	 * @param right the right operand
	 * @return the result of the operation
	 */
	public Object apply(char left, Number right) {
		return apply((int) left, right);
	}

	/**
	 * Applies the operation to a {@link Number} left operand and a {@code boolean} right operand.
	 *
	 * @param left  the left operand
	 * @param right the right operand as boolean
	 * @return the result of the operation
	 */
	public Object apply(Number left, boolean right) {
		return apply(left, booleanToInt(right));
	}

	/**
	 * Applies the operation to a {@code boolean} left operand and a {@link Number} right operand.
	 *
	 * @param left  the left operand as boolean
	 * @param right the right operand
	 * @return the result of the operation
	 */
	public Object apply(boolean left, Number right) {
		return apply(booleanToInt(left), right);
	}

	/**
	 * Applies the operation to a {@link Number} left operand and a generic {@link Object} right operand.
	 *
	 * @param left  the left operand
	 * @param right the right operand
	 * @return the result of the operation
	 */
	public abstract Object apply(Number left, Object right);

	/**
	 * Applies the operation to a generic {@link Object} left operand and a {@link Number} right operand.
	 *
	 * @param left  the left operand
	 * @param right the right operand
	 * @return the result of the operation
	 */
	public abstract Object apply(Object left, Number right);

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
	public Object apply(char left, char right) {
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
	public Object apply(boolean left, boolean right) {
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
	public abstract Object apply(String left, String right);
}
