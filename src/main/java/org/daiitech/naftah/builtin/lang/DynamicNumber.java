package org.daiitech.naftah.builtin.lang;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

import org.daiitech.naftah.builtin.utils.NumberUtils;

/**
 * @author Chakib Daii
 */
public class DynamicNumber {
	private Number value;

	public DynamicNumber(Number value) {
		Objects.requireNonNull(value, "القيمة غير صالحة (null)");
		this.value = value;
	}

	public DynamicNumber(Object value) {
		Objects.requireNonNull(value, "القيمة غير صالحة (null)");
		this.value = NumberUtils.parseDynamicNumber(value);
	}

	public static DynamicNumber of(Number value) {
		return new DynamicNumber(value);
	}

	public static DynamicNumber of(Object value) {
		return new DynamicNumber(value);
	}

	public boolean isByte() {
		return value instanceof Byte;
	}

	public boolean isShort() {
		return value instanceof Short;
	}

	public boolean isInt() {
		return value instanceof Integer;
	}

	public boolean isLong() {
		return value instanceof Long;
	}

	public boolean isBigInteger() {
		return value instanceof BigInteger;
	}

	public boolean isInteger() {
		return isByte() || isShort() || isInt() || isLong() || isBigInteger();
	}

	public boolean isFloat() {
		return value instanceof Float;
	}

	public boolean isDouble() {
		return value instanceof Double;
	}

	public boolean isBigDecimal() {
		return value instanceof BigDecimal;
	}

	public boolean isDecimal() {
		return isFloat() || isDouble() || isBigDecimal();
	}

	public byte asByte() {
		return value.byteValue();
	}

	public short asShort() {
		return value.shortValue();
	}

	public int asInt() {
		return value.intValue();
	}

	public long asLong() {
		return value.longValue();
	}

	public float asFloat() {
		return value.floatValue();
	}

	public double asDouble() {
		return value.doubleValue();
	}

	public BigInteger asBigInteger() {
		if (isBigInteger()) {
			return (BigInteger) value;
		}
		return new BigInteger(value.toString());
	}

	public BigDecimal asBigDecimal() {
		if (isBigDecimal()) {
			return (BigDecimal) value;
		}
		if (isBigInteger()) {
			return new BigDecimal((BigInteger) value);
		}
		return new BigDecimal(value.toString());
	}

	public Number get() {
		return value;
	}

	public DynamicNumber set(Number value) {
		this.value = value;
		return this;
	}

	public DynamicNumber promote() {
		if (isByte()) {
			return new DynamicNumber(asShort()); // Byte -> Short
		}
		else if (isShort()) {
			return new DynamicNumber(asInt()); // Short -> Int
		}
		else if (isInt()) {
			return new DynamicNumber(asLong()); // Int -> Long
		}
		else if (isLong()) {
			return new DynamicNumber(asBigInteger()); // Long -> BigInteger
		}
		else if (isFloat()) {
			return new DynamicNumber(asDouble()); // Float -> Double
		}
		else if (isDouble()) {
			return new DynamicNumber(asBigDecimal()); // Double -> BigDecimal
		}
		else {
			// BigInteger, BigDecimal or unknown: no promotion possible
			return this;
		}
	}

	public DynamicNumber normalize() {
		Number val = this.value;

		if (val instanceof BigDecimal bd) {
			try {
				// Try to convert BigDecimal to BigInteger if no decimal part
				if (bd.stripTrailingZeros().scale() <= 0) {
					BigInteger bi = bd.toBigIntegerExact();
					return new DynamicNumber(bi).normalize();
				}
			}
			catch (ArithmeticException e) {
				// Can't convert exactly to BigInteger, keep as BigDecimal
				return this;
			}
			return this; // keep BigDecimal as is if decimal part exists
		}

		if (val instanceof BigInteger bi) {
			// Try to downcast BigInteger to Long if fits
			if (bi.bitLength() <= 63) {
				long l = bi.longValue();
				return new DynamicNumber(l).normalize();
			}
			return this; // keep BigInteger if too big
		}

		long longVal = val.longValue();

		// Check fits in int?
		if (longVal >= Integer.MIN_VALUE && longVal <= Integer.MAX_VALUE) {
			int intVal = (int) longVal;

			// Check fits in short?
			if (intVal >= Short.MIN_VALUE && intVal <= Short.MAX_VALUE) {
				short shortVal = (short) intVal;

				// Check fits in byte?
				if (shortVal >= Byte.MIN_VALUE && shortVal <= Byte.MAX_VALUE) {
					return new DynamicNumber((byte) shortVal);
				}
				return new DynamicNumber(shortVal);
			}
			return new DynamicNumber(intVal);
		}
		return new DynamicNumber(longVal); // keep as long
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		DynamicNumber that = (DynamicNumber) o;

		return NumberUtils.equals(this, that);
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
