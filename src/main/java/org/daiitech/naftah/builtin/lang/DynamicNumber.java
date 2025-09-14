package org.daiitech.naftah.builtin.lang;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.daiitech.naftah.builtin.utils.NumberUtils;
import org.daiitech.naftah.errors.NaftahBugError;

import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugNullInputError;

/**
 * A wrapper class for numeric values that supports dynamic typing and
 * provides utility methods for type checking, conversion, promotion,
 * and normalization of numbers.
 * <p>
 * This class can hold any subclass of {@link Number} and provides
 * convenience methods to interpret and convert the value to various
 * numeric types like byte, short, int, long, BigInteger, float,
 * double, and BigDecimal.
 * <p>
 * It supports promotion of types to larger ranges and normalization
 * to the smallest suitable numeric type.
 *
 * @author Chakib Daii
 */
public class DynamicNumber extends Number implements Cloneable {
	/**
	 * The underlying numeric value.
	 */
	private Number value;

	/**
	 * Constructs a {@code DynamicNumber} from a {@link Number}.
	 *
	 * @param value the numeric value, must not be null
	 * @throws org.daiitech.naftah.errors.NaftahBugError if the value is null
	 */
	public DynamicNumber(Number value) {
		set(value);
	}

	/**
	 * Constructs a {@code DynamicNumber} from an {@link Object} by parsing it
	 * to a numeric value.
	 *
	 * @param value the value to parse as a number, must not be null
	 * @throws org.daiitech.naftah.errors.NaftahBugError if the value is null
	 * @see NumberUtils#parseDynamicNumber(Object)
	 */
	public DynamicNumber(Object value) {
		if (value == null || None.isNone(value)) {
			throw newNaftahBugNullInputError(true, value);
		}
		else if (NaN.isNaN(value)) {
			this.value = Double.NaN;
		}
		else {
			this.value = NumberUtils.parseDynamicNumber(value);
		}
	}

	/**
	 * Creates a new {@code DynamicNumber} from a {@link Number}.
	 *
	 * @param value the number
	 * @return a new {@code DynamicNumber} wrapping the given value
	 */
	public static DynamicNumber of(Number value) {
		return value instanceof DynamicNumber dynamicNumber ? dynamicNumber.clone() : new DynamicNumber(value);
	}

	/**
	 * Creates a new {@code DynamicNumber} by parsing an {@link Object}.
	 *
	 * @param value the value to parse
	 * @return a new {@code DynamicNumber} representing the parsed value
	 */
	public static DynamicNumber of(Object value) {
		return value instanceof DynamicNumber dynamicNumber ? dynamicNumber.clone() : new DynamicNumber(value);
	}

	/**
	 * Checks if the given number is NaN (Not-a-Number).
	 * <p>
	 * Only applies to {@link Float} and {@link Double} values.
	 * If the number is an instance of {@code DynamicNumber}, it will be unwrapped first.
	 *
	 * @param number the number to check
	 * @return {@code true} if the number is {@code NaN}, {@code false} otherwise
	 */
	public static boolean isNaN(Number number) {
		number = number instanceof DynamicNumber dynamicNumber ? dynamicNumber.value : number;
		if (number instanceof Double d) {
			return Double.isNaN(d);
		}
		if (number instanceof Float f) {
			return Float.isNaN(f);
		}
		return false;
	}

	/**
	 * Checks if the given number is positive or negative infinity.
	 * <p>
	 * Only applies to {@link Float} and {@link Double} values.
	 * If the number is an instance of {@code DynamicNumber}, it will be unwrapped first.
	 *
	 * @param number the number to check
	 * @return {@code true} if the number is {@code Infinity} or {@code -Infinity}, {@code false} otherwise
	 */
	public static boolean isInfinite(Number number) {
		number = number instanceof DynamicNumber dynamicNumber ? dynamicNumber.value : number;
		if (number instanceof Double d) {
			return Double.isInfinite(d);
		}
		if (number instanceof Float f) {
			return Float.isInfinite(f);
		}
		return false;
	}

	/**
	 * Checks if the value is a {@link Byte}.
	 *
	 * @return true if the underlying value is a Byte
	 */
	public boolean isByte() {
		return !isNaN() && value instanceof Byte;
	}

	/**
	 * Checks if the value is a {@link Short}.
	 *
	 * @return true if the underlying value is a Short
	 */
	public boolean isShort() {
		return !isNaN() && value instanceof Short;
	}

	/**
	 * Checks if the value is an {@link Integer}.
	 *
	 * @return true if the underlying value is an Integer
	 */
	public boolean isInt() {
		return !isNaN() && value instanceof Integer;
	}

	/**
	 * Checks if the value is a {@link Long}.
	 *
	 * @return true if the underlying value is a Long
	 */
	public boolean isLong() {
		return !isNaN() && value instanceof Long;
	}

	/**
	 * Checks if the value is a {@link BigInteger}.
	 *
	 * @return true if the underlying value is a BigInteger
	 */
	public boolean isBigInteger() {
		return !isNaN() && value instanceof BigInteger;
	}

	/**
	 * Checks if the value is an integral integer type
	 * (Byte, Short, Integer, Long, or BigInteger).
	 *
	 * @return true if the value is an integral number
	 */
	public boolean isInteger() {
		return !isNaN() && (isByte() || isShort() || isInt() || isLong() || isBigInteger());
	}

	/**
	 * Checks if the value is a {@link Float}.
	 *
	 * @return true if the underlying value is a Float
	 */
	public boolean isFloat() {
		return !isNaN() && value instanceof Float;
	}

	/**
	 * Checks if the value is a {@link Double}.
	 *
	 * @return true if the underlying value is a Double
	 */
	public boolean isDouble() {
		return !isNaN() && value instanceof Double;
	}

	/**
	 * Checks if the value is a {@link BigDecimal}.
	 *
	 * @return true if the underlying value is a BigDecimal
	 */
	public boolean isBigDecimal() {
		return !isNaN() && value instanceof BigDecimal;
	}

	/**
	 * Checks if the value is a floating point type
	 * (Float, Double, or BigDecimal).
	 *
	 * @return true if the value is a decimal number
	 */
	public boolean isDecimal() {
		return !isNaN() && (isFloat() || isDouble() || isBigDecimal());
	}

	/**
	 * Returns the value as an int.
	 *
	 * @return the int value
	 */
	@Override
	public int intValue() {
		return value.intValue();
	}

	/**
	 * Returns the value as a long.
	 *
	 * @return the long value
	 */
	@Override
	public long longValue() {
		return value.longValue();
	}

	/**
	 * Returns the value as a float.
	 *
	 * @return the float value
	 */
	@Override
	public float floatValue() {
		return value.floatValue();
	}

	/**
	 * Returns the value as a double.
	 *
	 * @return the double value
	 */
	@Override
	public double doubleValue() {
		return value.doubleValue();
	}

	/**
	 * Returns the value as a {@link BigInteger}.
	 * <p>
	 * If the underlying value is not already a BigInteger, it converts by parsing
	 * the string representation.
	 *
	 * @return the BigInteger representation
	 */
	public BigInteger asBigInteger() {
		if (isBigInteger()) {
			return (BigInteger) value;
		}
		return new BigInteger(value.toString());
	}

	/**
	 * Returns the value as a {@link BigDecimal}.
	 * <p>
	 * Converts from BigInteger or parses from string if needed.
	 *
	 * @return the BigDecimal representation
	 */
	public BigDecimal asBigDecimal() {
		if (isBigDecimal()) {
			return (BigDecimal) value;
		}
		if (isBigInteger()) {
			return new BigDecimal((BigInteger) value);
		}
		return new BigDecimal(value.toString());
	}

	/**
	 * Returns the underlying {@link Number} value.
	 *
	 * @return the wrapped numeric value
	 */
	public Number get() {
		return value;
	}

	/**
	 * Sets the underlying value.
	 *
	 * @param value the new numeric value
	 * @return this {@code DynamicNumber} instance for chaining
	 */
	public DynamicNumber set(Number value) {
		if (value == null) {
			throw newNaftahBugNullInputError(true, value);
		}
		this.value = value;
		return this;
	}

	/**
	 * Promotes the number to the next wider numeric type.
	 * <ul>
	 * <li>Byte -> Short</li>
	 * <li>Short -> Int</li>
	 * <li>Int -> Long</li>
	 * <li>Long -> BigInteger</li>
	 * <li>Float -> Double</li>
	 * <li>Double -> BigDecimal</li>
	 * </ul>
	 * If no promotion is possible, returns this instance.
	 *
	 * @return a promoted {@code DynamicNumber}, or this if no promotion
	 */
	public DynamicNumber promote() {
		if (isByte()) {
			set(shortValue()); // Byte -> Short
		}
		else if (isShort()) {
			set(intValue()); // Short -> Int
		}
		else if (isInt()) {
			set(longValue()); // Int -> Long
		}
		else if (isLong()) {
			set(asBigInteger()); // Long -> BigInteger
		}
		else if (isFloat()) {
			set(doubleValue()); // Float -> Double
		}
		else if (isDouble()) {
			set(asBigDecimal()); // Double -> BigDecimal
		}
		return this;
	}

	/**
	 * Normalizes the internal number value to the smallest suitable numeric type.
	 * <p>
	 * This method simplifies the internal representation of the number when possible:
	 * <ul>
	 * <li>Converts a {@code BigDecimal} with no fractional part to a {@code BigInteger}</li>
	 * <li>Downcasts a {@code BigInteger} to {@code long} if it fits</li>
	 * <li>Downcasts integral {@code long} values to {@code int}, {@code short}, or {@code byte} if within range</li>
	 * <li>If floating point support is enabled via {@link #normalize(boolean)}, it also tries to:
	 * <ul>
	 * <li>Convert {@code Double} values to {@code Float} if within precision range</li>
	 * <li>Convert whole {@code Float} or {@code Double} values to integral types</li>
	 * </ul>
	 * </li>
	 * </ul>
	 *
	 * @return {@code this}, after normalizing the value in place
	 * @see #normalize(boolean)
	 */
	public DynamicNumber normalize() {
		return normalize(false);
	}

	/**
	 * Normalizes the internal number to the most compact numeric type possible,
	 * optionally including support for floating point number simplification.
	 * <p>
	 * The normalization process works as follows:
	 * <ul>
	 * <li><b>{@code BigDecimal}:</b> If it has no fractional part, it's converted to {@code BigInteger}</li>
	 * <li><b>{@code BigInteger}:</b> Downcast to {@code long} if it fits (bit length ≤ 63)</li>
	 * <li><b>{@code long}:</b> Downcast to {@code int}, {@code short}, or {@code byte} if within range</li>
	 * <li><b>{@code Double}/{@code Float}:</b> If {@code processFloatingNumbers} is {@code true}, and:
	 * <ul>
	 * <li>It's a finite, whole number → convert to {@code long} and then normalize further</li>
	 * <li>{@code Double} fits in {@code Float} range → convert to {@code Float}</li>
	 * </ul>
	 * </li>
	 * </ul>
	 * <p>
	 * Special floating-point values like {@code NaN} or {@code Infinity} are preserved and skipped.
	 *
	 * @param processFloatingNumbers whether to process and simplify {@code Float} / {@code Double} types
	 * @return {@code this}, after normalizing the value in place
	 */
	public DynamicNumber normalize(boolean processFloatingNumbers) {
		Number val = this.value;
		if (!isNaN()) {
			if (val instanceof BigDecimal bd) {
				try {
					// Try to convert BigDecimal to BigInteger if no decimal part
					if (bd.stripTrailingZeros().scale() <= 0) {
						BigInteger bi = bd.toBigIntegerExact();
						set(bi).normalize();
					}
				}
				catch (ArithmeticException e) {
					// Can't convert exactly to BigInteger, keep as BigDecimal
				}
			}
			else if (processFloatingNumbers && val instanceof Double d && !(isNaN(d) || isInfinite(d))) {
				if (d >= Float.MIN_VALUE && d <= Float.MAX_VALUE) {
					set(d.floatValue()).normalize();
				}
				else if (d == Math.rint(d)) { // has no fractional part
					set(d.longValue()).normalize(); // downcast via long
				}
			}
			else if (processFloatingNumbers && val instanceof Float f && !(Float.isNaN(f) || Float
					.isInfinite(f)) && f == Math
							.rint(
									f)) {
										set(f.longValue()).normalize();
									}
			else if (val instanceof BigInteger bi && bi.bitLength() <= 63) {
				// downcast BigInteger to Long if fits
				long l = bi.longValue();
				set(l).normalize();
			}
			else if (isInteger() && !isBigInteger()) {
				long longVal = val.longValue();

				// Check fits in int?
				if (longVal >= Integer.MIN_VALUE && longVal <= Integer.MAX_VALUE) {
					int intVal = (int) longVal;

					// Check fits in short?
					if (intVal >= Short.MIN_VALUE && intVal <= Short.MAX_VALUE) {
						short shortVal = (short) intVal;

						// Check fits in byte?
						if (shortVal >= Byte.MIN_VALUE && shortVal <= Byte.MAX_VALUE) {
							set((byte) shortVal);
						}
						else {
							set(shortVal);
						}
					}
					else {
						set(intVal);
					}
				}
				else {
					set(longVal); // keep as long
				}
			}
		}
		return this;
	}

	/**
	 * Compares this {@code DynamicNumber} to another object for equality.
	 * <p>
	 * Equality is based on numeric value comparison using {@link NumberUtils#equals}.
	 *
	 * @param o the object to compare to
	 * @return true if numerically equal; false otherwise
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		DynamicNumber that = (DynamicNumber) o;

		return !isNaN() && NumberUtils.equals(this, that);
	}

	/**
	 * Returns the hash code of the underlying numeric value.
	 *
	 * @return the hash code
	 */
	@Override
	public int hashCode() {
		return isNaN() ? NaN.get().hashCode() : value.hashCode();
	}

	/**
	 * Returns the string representation of the numeric value.
	 *
	 * @return the string form of the wrapped number
	 */
	@Override
	public String toString() {
		String result;
		if (isNaN()) {
			result = NaN.get().toString();
		}
		else if (value == null || None.isNone(value)) {
			result = None.get().toString();
		}
		else if (value instanceof BigDecimal bd) {
			result = bd.toPlainString();
		}
		else if (value instanceof BigInteger bi) {
			result = bi.toString();
		}
		else if (value instanceof Float || value instanceof Double) {
			result = new BigDecimal(value.toString()).toPlainString();
		}
		else {
			result = value.toString();
		}
		return result;
	}

	/**
	 * Checks if the internal value is a floating-point NaN (Not-a-Number).
	 *
	 * @return {@code true} if the value is NaN, otherwise {@code false}
	 * @see #isNaN(Number)
	 */
	public boolean isNaN() {
		return isNaN(value);
	}

	/**
	 * Checks if the internal value is positive or negative infinity.
	 *
	 * @return {@code true} if the value is {@code Infinity} or {@code -Infinity}, otherwise {@code false}
	 * @see #isInfinite(Number)
	 */
	public boolean isInfinite() {
		return isInfinite(value);
	}

	/**
	 * Creates and returns a copy of this {@code DynamicNumber}.
	 * <p>
	 * This performs a shallow clone. If your subclass adds mutable fields,
	 * ensure they are also copied to prevent shared state.
	 *
	 * @return a cloned copy of this instance
	 * @throws NaftahBugError if cloning fails unexpectedly
	 */
	@Override
	public DynamicNumber clone() {
		try {
			return (DynamicNumber) super.clone();
		}
		catch (CloneNotSupportedException e) {
			throw new NaftahBugError("فشل الاستنساخ بشكل غير متوقع.", e);
		}
	}
}
