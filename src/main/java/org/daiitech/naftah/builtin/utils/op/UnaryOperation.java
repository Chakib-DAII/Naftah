package org.daiitech.naftah.builtin.utils.op;

import java.util.Objects;

import org.daiitech.naftah.builtin.lang.DynamicNumber;
import org.daiitech.naftah.builtin.lang.NaN;
import org.daiitech.naftah.builtin.lang.None;
import org.daiitech.naftah.builtin.utils.NumberUtils;
import org.daiitech.naftah.builtin.utils.StringUtils;
import org.daiitech.naftah.errors.NaftahBugError;

import static org.daiitech.naftah.builtin.utils.ObjectUtils.booleanToInt;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.getNaftahType;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.intToBoolean;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.not;
import static org.daiitech.naftah.parser.DefaultNaftahParserVisitor.PARSER_VOCABULARY;
import static org.daiitech.naftah.utils.reflect.ClassUtils.getQualifiedName;

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
	BITWISE_NOT("BITWISE_NOT") {
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Number apply(Number number) {
			return NumberUtils.not(number);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String apply(String string) {
			return StringUtils.not(string);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object handleFalsy(Object object) {
			return apply(0);
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
	PRE_INCREMENT("PRE_INCREMENT") {
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Number apply(Number number) {
			return NumberUtils.preIncrement(number);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String apply(String string) {
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
	POST_INCREMENT("POST_INCREMENT") {
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Number apply(Number number) {
			return NumberUtils.postIncrement(number);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String apply(String string) {
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
	PRE_DECREMENT("PRE_DECREMENT") {
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Number apply(Number number) {
			return NumberUtils.preDecrement(number);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String apply(String string) {
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
	POST_DECREMENT("POST_DECREMENT") {
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Number apply(Number number) {
			return NumberUtils.postDecrement(number);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String apply(String string) {
			return StringUtils.postDecrement(string);
		}
	},

	/**
	 * Unary plus operation.
	 * <p>
	 * Returns the number as-is.
	 * For strings, returns {@link NaN} to indicate invalid usage.
	 */
	PLUS("PLUS") {
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Number apply(Number number) {
			return number;
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(String string) {
			return NaN.get();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object handleFalsy(Object object) {
			if (NaN.isNaN(object)) {
				return object;
			}
			return 0;
		}
	},

	/**
	 * Unary minus operation.
	 * <p>
	 * Negates numeric values.
	 * For strings, returns {@link NaN} to indicate invalid usage.
	 */
	MINUS("MINUS") {
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Number apply(Number number) {
			return NumberUtils.negate(number);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(String string) {
			return NaN.get();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object handleFalsy(Object object) {
			if (NaN.isNaN(object)) {
				return object;
			}
			return -0;
		}
	},

	/**
	 * Logical NOT operation.
	 * <p>
	 * Applies a "falsy" check to numeric or string values and returns the opposite boolean.
	 * Uses the custom {@code not(...)} logic based on "truthy"/"falsy" evaluation.
	 */
	NOT("NOT") {
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(Number number) {
			return not(number);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(boolean b) {
			return !b;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object apply(String string) {
			return not(string);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object handleFalsy(Object object) {
			return true;
		}
	};

	/**
	 * Represents the postfix position of an operator, such as <code>x++</code> or <code>x--</code>.
	 * Used to indicate that the operator comes after the operand.
	 */
	public static final String POST = "POST_";

	/**
	 * Represents the prefix position of an operator, such as <code>++x</code> or <code>--x</code>.
	 * Used to indicate that the operator comes before the operand.
	 */
	public static final String PRE = "PRE_";
	/**
	 * The string representation of the increment operation.
	 * Typically used to represent {@code ++} in the language grammar or runtime logic.
	 */
	public static final String INCREMENT = "INCREMENT";
	/**
	 * The string representation of the decrement operation.
	 * Typically used to represent {@code --} in the language grammar or runtime logic.
	 */
	public static final String DECREMENT = "DECREMENT";

	/**
	 * The symbolic name of the unary operator (e.g., "PLUS", "MINUS", "NOT").
	 */
	private final String op;

	/**
	 * Constructs a {@code UnaryOperation} enum constant with its symbolic name.
	 *
	 * @param op the symbolic name of the operation
	 */
	UnaryOperation(String op) {
		this.op = op;
	}

	/**
	 * Constructs a {@link NaftahBugError} to indicate that the specified unary operation
	 * is not supported for the given operand type.
	 *
	 * @param unaryOperation the unary operation attempted
	 * @param o              the operand for which the operation is not supported
	 * @return a {@code NaftahBugError} with a descriptive error message
	 */
	public static NaftahBugError newNaftahBugError(Operation unaryOperation, Object o) {
		return new NaftahBugError("العملية '%s' غير مدعومة للنوع: '%s'"
				.formatted( unaryOperation,
							Objects.isNull(PARSER_VOCABULARY) ?
									getQualifiedName(o.getClass().getName()) :
									getNaftahType(  PARSER_VOCABULARY,
													o.getClass())));
	}

	/**
	 * Resolves a {@link UnaryOperation} by its operator string.
	 * <p>
	 * Accepts exact match, or variants with {@code PRE} or {@code POST} prefixes (e.g., {@code "++"}, {@code "PRE++"}
	 * , {@code "POST++"}).
	 *
	 * @param op the operator string (e.g., "NOT", "++", "MINUS")
	 * @return the matching {@link UnaryOperation} enum constant
	 * @throws NaftahBugError if no matching operation is found
	 */
	public static UnaryOperation of(String op) {
		for (UnaryOperation operator : UnaryOperation.values()) {
			if (operator.op.equals(op)) {
				return operator;
			}
		}

		throw Operation
				.newNaftahBugNoEnumValueError(
												UnaryOperation.class,
												op);
	}

	/**
	 * Applies this unary operation to a dynamically typed operand.
	 * <p>
	 * Supports {@link Number}, {@link Boolean}, {@link Character}, and {@link String}.
	 * If the value is {@link NaN}, it is returned as-is.
	 *
	 * @param object the operand to apply the operation to
	 * @return the result of applying the operation
	 * @throws NaftahBugError if the operand type is unsupported
	 */
	public Object apply(Object object) {
		if (NaN.isNaN(object) || None.isNone(object)) {
			return handleFalsy(object);
		}
		else if (object instanceof Number number) {
			if (DynamicNumber.isNaN(number)) {
				return handleFalsy(NaN.get());
			}
			else {
				return apply(number);
			}
		}
		else if (object instanceof Boolean aBoolean) {
			return apply(aBoolean.booleanValue());
		}
		else if (object instanceof Character character) {
			return apply(character.charValue());
		}
		else if (object instanceof String string) {
			return apply(string);
		}
		throw UnaryOperation.newNaftahBugError(this, object);
	}

	/**
	 * Handles the application of this unary operation to a "falsy" value,
	 * such as {@code null}, {@code NaN}, or {@code None}.
	 * <p>
	 * By default, this method throws a {@link NaftahBugError} to indicate
	 * that the operation is not supported for the given falsy input.
	 * Subclasses or enum constants may override this to provide custom behavior.
	 *
	 * @param object the input object considered "falsy"
	 * @return the result of applying the operation, if overridden
	 * @throws NaftahBugError if the operation is not valid for the input
	 */
	protected Object handleFalsy(Object object) {
		throw UnaryOperation.newNaftahBugError(this, object);
	}

	/**
	 * Applies the unary operation to a {@link Number} operand.
	 *
	 * @param number the numeric operand
	 * @return the result of the unary operation as a {@link Number} or {@link NaN}
	 */
	protected abstract Object apply(Number number);

	/**
	 * Applies the unary operation to a {@code char} operand.
	 * Internally converts the character to its integer code point,
	 * applies the operation, and casts the result back to {@code char}.
	 *
	 * @param c the character operand
	 * @return the result of the operation as a character or {@link NaN}
	 */
	protected Object apply(char c) {
		Object result = apply((int) c);
		if (result instanceof Number number) {
			return (char) number.intValue();
		}
		return result;
	}

	/**
	 * Applies the unary operation to a {@code boolean} operand.
	 * Converts the boolean to an integer (true → 1, false → 0),
	 * applies the operation, and converts the result back to boolean.
	 *
	 * @param b the boolean operand
	 * @return the result of the operation as a boolean or {@link NaN}
	 */
	protected Object apply(boolean b) {
		Object result = apply(booleanToInt(b));
		if (result instanceof Number number) {
			return intToBoolean(number.intValue());
		}
		return result;
	}

	/**
	 * Applies the unary operation to a {@link String} operand.
	 * The implementation is operation-specific.
	 *
	 * @param string the string operand
	 * @return the result of the operation as a string or {@link NaN}
	 */
	protected abstract Object apply(String string);

	/**
	 * Returns the string representation of this unary operation,
	 * which is its symbolic operator.
	 *
	 * @return the operator string (e.g., "PLUS", "MINUS", "NOT")
	 */
	@Override
	public String toString() {
		return op;
	}
}
