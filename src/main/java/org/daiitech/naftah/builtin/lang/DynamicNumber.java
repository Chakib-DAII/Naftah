package org.daiitech.naftah.builtin.lang;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

import org.daiitech.naftah.builtin.utils.NumberUtils;

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
public class DynamicNumber {
    /**
     * The underlying numeric value.
     */
    private Number value;

    /**
     * Constructs a {@code DynamicNumber} from a {@link Number}.
     *
     * @param value the numeric value, must not be null
     * @throws NullPointerException if the value is null
     */
    public DynamicNumber(Number value) {
        Objects.requireNonNull(value, "القيمة غير صالحة (null)");
        this.value = value;
    }

    /**
     * Constructs a {@code DynamicNumber} from an {@link Object} by parsing it
     * to a numeric value.
     *
     * @param value the value to parse as a number, must not be null
     * @throws NullPointerException if the value is null
     * @see NumberUtils#parseDynamicNumber(Object)
     */
    public DynamicNumber(Object value) {
        Objects.requireNonNull(value, "القيمة غير صالحة (null)");
        this.value = NumberUtils.parseDynamicNumber(value);
    }

    /**
     * Creates a new {@code DynamicNumber} from a {@link Number}.
     *
     * @param value the number
     * @return a new {@code DynamicNumber} wrapping the given value
     */
    public static DynamicNumber of(Number value) {
        return new DynamicNumber(value);
    }

    /**
     * Creates a new {@code DynamicNumber} by parsing an {@link Object}.
     *
     * @param value the value to parse
     * @return a new {@code DynamicNumber} representing the parsed value
     */
    public static DynamicNumber of(Object value) {
        return new DynamicNumber(value);
    }

    /**
     * Checks if the value is a {@link Byte}.
     *
     * @return true if the underlying value is a Byte
     */
    public boolean isByte() {
        return value instanceof Byte;
    }

    /**
     * Checks if the value is a {@link Short}.
     *
     * @return true if the underlying value is a Short
     */
    public boolean isShort() {
        return value instanceof Short;
    }

    /**
     * Checks if the value is an {@link Integer}.
     *
     * @return true if the underlying value is an Integer
     */
    public boolean isInt() {
        return value instanceof Integer;
    }

    /**
     * Checks if the value is a {@link Long}.
     *
     * @return true if the underlying value is a Long
     */
    public boolean isLong() {
        return value instanceof Long;
    }

    /**
     * Checks if the value is a {@link BigInteger}.
     *
     * @return true if the underlying value is a BigInteger
     */
    public boolean isBigInteger() {
        return value instanceof BigInteger;
    }

    /**
     * Checks if the value is an integral integer type
     * (Byte, Short, Integer, Long, or BigInteger).
     *
     * @return true if the value is an integral number
     */
    public boolean isInteger() {
        return isByte() || isShort() || isInt() || isLong() || isBigInteger();
    }

    /**
     * Checks if the value is a {@link Float}.
     *
     * @return true if the underlying value is a Float
     */
    public boolean isFloat() {
        return value instanceof Float;
    }

    /**
     * Checks if the value is a {@link Double}.
     *
     * @return true if the underlying value is a Double
     */
    public boolean isDouble() {
        return value instanceof Double;
    }

    /**
     * Checks if the value is a {@link BigDecimal}.
     *
     * @return true if the underlying value is a BigDecimal
     */
    public boolean isBigDecimal() {
        return value instanceof BigDecimal;
    }

    /**
     * Checks if the value is a floating point type
     * (Float, Double, or BigDecimal).
     *
     * @return true if the value is a decimal number
     */
    public boolean isDecimal() {
        return isFloat() || isDouble() || isBigDecimal();
    }

    /**
     * Returns the value as a byte.
     *
     * @return the byte value
     */
    public byte asByte() {
        return value.byteValue();
    }

    /**
     * Returns the value as a short.
     *
     * @return the short value
     */
    public short asShort() {
        return value.shortValue();
    }

    /**
     * Returns the value as an int.
     *
     * @return the int value
     */
    public int asInt() {
        return value.intValue();
    }

    /**
     * Returns the value as a long.
     *
     * @return the long value
     */
    public long asLong() {
        return value.longValue();
    }

    /**
     * Returns the value as a float.
     *
     * @return the float value
     */
    public float asFloat() {
        return value.floatValue();
    }

    /**
     * Returns the value as a double.
     *
     * @return the double value
     */
    public double asDouble() {
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

    /**
     * Normalizes the number to the smallest suitable numeric type.
     * <p>
     * For example, it will convert BigDecimal without fractional part to BigInteger,
     * BigInteger values that fit into a long down to long, and long values down to
     * int, short, or byte if possible.
     *
     * @return a new {@code DynamicNumber} with the normalized value
     */
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

        return NumberUtils.equals(this, that);
    }

    /**
     * Returns the hash code of the underlying numeric value.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return value.hashCode();
    }
    
    /**
     * Returns the string representation of the numeric value.
     *
     * @return the string form of the wrapped number
     */
    @Override
    public String toString() {
        if (value instanceof BigDecimal bd) {
            return bd.toPlainString();
        }
        else if (value instanceof BigInteger bi) {
            return bi.toString();
        }
        else if (value instanceof Float || value instanceof Double) {
            return new BigDecimal(value.toString()).toPlainString();
        }
        else {
            return value.toString();
        }
    }
}
