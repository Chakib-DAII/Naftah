package org.daiitech.naftah.builtin.utils.op;

import org.daiitech.naftah.builtin.utils.NumberUtils;
import org.daiitech.naftah.builtin.utils.StringUtils;
import org.daiitech.naftah.errors.NaftahBugError;

import static org.daiitech.naftah.builtin.utils.ObjectUtils.booleanToInt;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.intToBoolean;

/**
 * Represents all unary operations supported by the Naftah language.
 * Unary operations take a single operand and return a result after applying
 * a transformation (e.g., negation, logical not, etc.).
 *
 * @author Chakib Daii
 */
public enum UnaryOperation implements Operation {

	// Bitwise
	/**
	 * BITWISE_NOT: Performs a bitwise NOT (~) operation on numeric values.
	 * <p>
	 * For integers, it inverts each bit. For characters and booleans, it first converts them
	 * to their integer representations, applies the operation, and converts back to the original type.
	 * Strings are unsupported and will typically throw a runtime exception.
	 */
	BITWISE_NOT {
		@Override
		public Number apply(Number number) {
			return NumberUtils.not(number);
		}

		@Override
		public String apply(String string) {
			return StringUtils.not(string);
		}
	},

	// Unary increments/decrements
	/**
	 * PRE_INCREMENT: Simulates the prefix increment operation (++x).
	 * <p>
	 * Increases the numeric value by 1 and returns the result.
	 * For characters and booleans, it first converts them to integers, applies the increment,
	 * and converts back to the original type.
	 * Strings are unsupported and will typically result in a runtime error.
	 */
	PRE_INCREMENT {
		@Override
		public Number apply(Number number) {
			return NumberUtils.preIncrement(number);
		}

		@Override
		public String apply(String string) {
			return StringUtils.preIncrement(string);
		}
	},

	/**
	 * POST_INCREMENT: Simulates the postfix increment operation (x++).
	 * <p>
	 * Returns the original numeric value before incrementing it by 1.
	 * For characters and booleans, it converts them to integers, stores the original,
	 * applies the increment, and returns the original value as the result.
	 * Strings are unsupported and will typically result in a runtime error.
	 */
	POST_INCREMENT {
		@Override
		public Number apply(Number number) {
			return NumberUtils.postIncrement(number);
		}

		@Override
		public String apply(String string) {
			return StringUtils.postIncrement(string);
		}
	},

	/**
	 * PRE_DECREMENT: Simulates the prefix decrement operation (--x).
	 * <p>
	 * Decreases the numeric value by 1 and returns the result.
	 * For characters and booleans, it converts them to integers,
	 * applies the decrement, and then converts back to the original type.
	 * Strings are unsupported and will typically result in a runtime error.
	 */
	PRE_DECREMENT {
		@Override
		public Number apply(Number number) {
			return NumberUtils.preDecrement(number);
		}

		@Override
		public String apply(String string) {
			return StringUtils.preDecrement(string);
		}
	},

	/**
	 * POST_DECREMENT: Simulates the postfix decrement operation (x--).
	 * <p>
	 * Returns the original numeric value before decreasing it by 1.
	 * For characters and booleans, the operation converts them to integers,
	 * performs the decrement, and then casts back to the original type.
	 * Strings are not supported and will result in an error if used.
	 */
	POST_DECREMENT {
		@Override
		public Number apply(Number number) {
			return NumberUtils.postDecrement(number);
		}

		@Override
		public String apply(String string) {
			return StringUtils.postDecrement(string);
		}
	};

	/**
	 * Constructs a {@link NaftahBugError} to indicate that the specified unary operation
	 * is not supported for the given operand type.
	 *
	 * @param unaryOperation the unary operation attempted
	 * @param o              the operand for which the operation is not supported
	 * @return a {@code NaftahBugError} with a descriptive error message
	 */
	public static NaftahBugError newNaftahBugError(Operation unaryOperation, Object o) {
		return new NaftahBugError("العملية '%s' غير مدعومة للنوع: '%s'".formatted(unaryOperation, o.getClass()));
	}

	// TODO : minimize the overhead of creating dynamic number from number everytime we perform operation by creating and using dynamic number

	/**
	 * Applies the unary operation to a {@link Number} operand.
	 *
	 * @param number the numeric operand
	 * @return the result of the unary operation as a {@link Number}
	 */
	public abstract Number apply(Number number);

	/**
	 * Applies the unary operation to a {@code char} operand.
	 * Internally converts the character to its integer code point,
	 * applies the operation, and casts the result back to {@code char}.
	 *
	 * @param c the character operand
	 * @return the result of the operation as a character
	 */
	public Character apply(char c) {
		return (char) apply((int) c).intValue();
	}

	/**
	 * Applies the unary operation to a {@code boolean} operand.
	 * Converts the boolean to an integer (true → 1, false → 0),
	 * applies the operation, and converts the result back to boolean.
	 *
	 * @param b the boolean operand
	 * @return the result of the operation as a boolean
	 */
	public Boolean apply(boolean b) {
		return intToBoolean(apply(booleanToInt(b)).intValue());
	}

	/**
	 * Applies the unary operation to a {@link String} operand.
	 * The implementation is operation-specific.
	 *
	 * @param string the string operand
	 * @return the result of the operation as a string
	 */
	public abstract String apply(String string);
}
