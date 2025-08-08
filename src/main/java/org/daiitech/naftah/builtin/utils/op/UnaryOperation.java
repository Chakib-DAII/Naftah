package org.daiitech.naftah.builtin.utils.op;

import org.daiitech.naftah.builtin.utils.NumberUtils;
import org.daiitech.naftah.builtin.utils.StringUtils;
import org.daiitech.naftah.errors.NaftahBugError;

import static org.daiitech.naftah.builtin.utils.ObjectUtils.booleanToInt;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.intToBoolean;

/**
 * @author Chakib Daii
 */
public enum UnaryOperation implements Operation {
	// Bitwise

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

	public static NaftahBugError newNaftahBugError(Operation unaryOperation, Object o) {
		return new NaftahBugError("العملية '%s' غير مدعومة للنوع: '%s'".formatted(unaryOperation, o.getClass()));
	}

	// TODO : minimize the overhead of creating dynamic number from number everytime we perform operation by creating and using dynamic number
	public abstract Number apply(Number number);

	public Character apply(char c) {
		return (char) apply((int) c).intValue();
	}

	public Boolean apply(boolean b) {
		return intToBoolean(apply(booleanToInt(b)).intValue());
	}

	public abstract String apply(String string);
}
