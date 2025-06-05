package org.daiitech.naftah.core.builtin.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Objects;
import org.daiitech.naftah.core.builtin.lang.DynamicNumber;

/**
 * Utility class for dynamically parsing and performing arithmetic operations on various numeric
 * types. Supports Byte, Short, Integer, Long, Float, Double, BigInteger, and BigDecimal.
 *
 * @author Chakib Daii
 */
public final class NumberUtils {

  private static final BigInteger LONG_MIN = BigInteger.valueOf(Long.MIN_VALUE);

  private static final BigInteger LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);

  public NumberUtils() {
    throw new IllegalStateException("Illegal usage.");
  }

  public static Number parseDynamicNumber(Object object) {
    if (object instanceof Number number) {
      return number;
    } else if (object instanceof String string) {
      return parseDynamicNumber(string);
    }
    throw new RuntimeException("Invalid number: " + object);
  }

  /**
   * Parses a string into the most appropriate {@link Number} type.
   *
   * <ul>
   *   <li>Parses decimal numbers (with dot or exponent) as Float, Double, or BigDecimal (in that
   *       order).
   *   <li>Parses whole numbers as Byte, Short, Integer, Long, or BigInteger (in that order).
   * </ul>
   *
   * @param text the input numeric string
   * @return the parsed Number
   * @throws RuntimeException if parsing fails or if value is NaN or infinite
   */
  public static Number parseDynamicNumber(String text) {
    Objects.requireNonNull(text, "text must not be null");
    try {
      if (text.contains(".") || text.toLowerCase().contains("e")) {
        try {
          // Try parsing as a float first
          float f = Float.parseFloat(text);
          if (Float.isInfinite(f)) {
            throw new NumberFormatException(
                "Floating-point overflow: value is infinite for input " + text);
          }
          if (Float.isNaN(f)) {
            throw new RuntimeException(new NumberFormatException("Value is NaN: " + text));
          }
          return f;
        } catch (NumberFormatException e1) {
          try {
            // Try parsing as a double first
            double d = Double.parseDouble(text);
            if (Double.isInfinite(d)) {
              throw new NumberFormatException(
                  "Floating-point overflow: value is infinite/ for input " + text);
            }
            if (Double.isNaN(d)) {
              throw new RuntimeException(new NumberFormatException("Value is NaN: " + text));
            }
            return d;
          } catch (NumberFormatException e2) {
            // Fall back to BigDecimal for high-precision decimals
            return new BigDecimal(text);
          }
        }
      } else {
        try {
          return Byte.parseByte(text);
        } catch (NumberFormatException e1) {
          try {
            return Short.parseShort(text);
          } catch (NumberFormatException e2) {
            try {
              // Try parsing as a 32-bit integer
              return Integer.parseInt(text);
            } catch (NumberFormatException e3) {
              try {
                // Try parsing as a 64-bit integer
                return Long.parseLong(text);
              } catch (NumberFormatException e4) {
                // Fall back to arbitrary-precision integer
                return new BigInteger(text);
              }
            }
          }
        }
      }
    } catch (NumberFormatException ex) {
      throw new RuntimeException("Invalid number format: " + text, ex);
    }
  }

  /**
   * Convert the given number into an instance of the given target class.
   *
   * @param number the number to convert
   * @param targetClass the target class to convert to
   * @return the converted number
   * @throws IllegalArgumentException if the target class is not supported (i.e. not a standard
   *     Number subclass as included in the JDK)
   * @see java.lang.Byte
   * @see java.lang.Short
   * @see java.lang.Integer
   * @see java.lang.Long
   * @see java.math.BigInteger
   * @see java.lang.Float
   * @see java.lang.Double
   * @see java.math.BigDecimal
   */
  @SuppressWarnings("unchecked")
  public static <T extends Number> T convertNumberToTargetClass(Number number, Class<T> targetClass)
      throws IllegalArgumentException {
    Objects.requireNonNull(number, "Number must not be null");
    Objects.requireNonNull(targetClass, "Target class must not be null");

    if (targetClass.isInstance(number)) {
      return (T) number;
    } else if (Byte.class == targetClass) {
      long value = checkedLongValue(number, targetClass);
      if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
        raiseOverflowException(number, targetClass);
      }
      return (T) Byte.valueOf(number.byteValue());
    } else if (Short.class == targetClass) {
      long value = checkedLongValue(number, targetClass);
      if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
        raiseOverflowException(number, targetClass);
      }
      return (T) Short.valueOf(number.shortValue());
    } else if (Integer.class == targetClass) {
      long value = checkedLongValue(number, targetClass);
      if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
        raiseOverflowException(number, targetClass);
      }
      return (T) Integer.valueOf(number.intValue());
    } else if (Long.class == targetClass) {
      long value = checkedLongValue(number, targetClass);
      return (T) Long.valueOf(value);
    } else if (BigInteger.class == targetClass) {
      if (number instanceof BigDecimal bigDecimal) {
        // do not lose precision - use BigDecimal's own conversion
        return (T) bigDecimal.toBigInteger();
      }
      // original value is not a Big* number - use standard long conversion
      return (T) BigInteger.valueOf(number.longValue());
    } else if (Float.class == targetClass) {
      return (T) Float.valueOf(number.floatValue());
    } else if (Double.class == targetClass) {
      return (T) Double.valueOf(number.doubleValue());
    } else if (BigDecimal.class == targetClass) {
      // always use BigDecimal(String) here to avoid unpredictability of BigDecimal(double)
      // (see BigDecimal javadoc for details)
      return (T) new BigDecimal(number.toString());
    } else {
      throw new IllegalArgumentException(
          "Could not convert number ["
              + number
              + "] of type ["
              + number.getClass().getName()
              + "] to unsupported target class ["
              + targetClass.getName()
              + "]");
    }
  }

  /**
   * Check for a {@code BigInteger}/{@code BigDecimal} long overflow before returning the given
   * number as a long value.
   *
   * @param number the number to convert
   * @param targetClass the target class to convert to
   * @return the long value, if convertible without overflow
   * @throws IllegalArgumentException if there is an overflow
   * @see #raiseOverflowException
   */
  private static long checkedLongValue(Number number, Class<? extends Number> targetClass) {
    BigInteger bigInt = null;
    if (number instanceof BigInteger bigInteger) {
      bigInt = bigInteger;
    } else if (number instanceof BigDecimal bigDecimal) {
      bigInt = bigDecimal.toBigInteger();
    }
    // Effectively analogous to JDK 8's BigInteger.longValueExact()
    if (bigInt != null && (bigInt.compareTo(LONG_MIN) < 0 || bigInt.compareTo(LONG_MAX) > 0)) {
      raiseOverflowException(number, targetClass);
    }
    return number.longValue();
  }

  /**
   * Raise an <em>overflow</em> exception for the given number and target class.
   *
   * @param number the number we tried to convert
   * @param targetClass the target class we tried to convert to
   * @throws IllegalArgumentException if there is an overflow
   */
  private static void raiseOverflowException(Number number, Class<?> targetClass) {
    throw new IllegalArgumentException(
        "Could not convert number ["
            + number
            + "] of type ["
            + number.getClass().getName()
            + "] to target class ["
            + targetClass.getName()
            + "]: overflow");
  }

  // ==============================
  // Arithmetic Operations
  // ==============================

  /**
   * Adds two {@link Number} values.
   *
   * @param x left operand
   * @param y right operand
   * @return the result of addition
   * @param <T> concrete type that extends @{@link Number}
   */
  public static <T extends Number> Number add(T x, T y) {
    DynamicNumber dx = DynamicNumber.of(x);
    DynamicNumber dy = DynamicNumber.of(y);
    return add(dx, dy);
  }

  /**
   * Adds two numeric values represented as strings.
   *
   * @param x left operand
   * @param y right operand
   * @return the result of addition
   */
  public static Number add(Object x, Object y) {
    DynamicNumber dx = DynamicNumber.of(x);
    DynamicNumber dy = DynamicNumber.of(y);
    return add(dx, dy);
  }

  /**
   * Adds two {@link DynamicNumber} instances with type promotion.
   *
   * @param dx left operand
   * @param dy right operand
   * @return the result of addition
   */
  public static Number add(DynamicNumber dx, DynamicNumber dy) {
    Number result;
    if (dx.isDecimal() || dy.isDecimal()) {
      if (dx.isBigDecimal() || dy.isBigDecimal()) {
        result = dx.asBigDecimal().add(dy.asBigDecimal());
      } else if (dx.isDouble() || dy.isDouble()) {
        result = dx.asDouble() + dy.asDouble();
      } else {
        result = dx.asFloat() + dy.asFloat();
      }
    } else if (dx.isInteger() || dy.isInteger()) {
      if (dx.isBigInteger() || dy.isBigInteger()) {
        result = dx.asBigInteger().add(dy.asBigInteger());
      } else if (dx.isLong() || dy.isLong()) {
        result = dx.asLong() + dy.asLong();
      } else if (dx.isInt() || dy.isInt()) {
        result = dx.asInt() + dy.asInt();
      } else if (dx.isShort() || dy.isShort()) {
        result = dx.asShort() + dy.asShort();
      } else {
        result = dx.asByte() + dy.asByte();
      }
    } else {
      // Unknown or unsupported number types
      throw new UnsupportedOperationException(
          "Unsupported number types: " + dx.get().getClass() + ", " + dy.get().getClass());
    }
    return result;
  }

  /**
   * Subtracts two {@link Number} values.
   *
   * @param x left operand
   * @param y right operand
   * @return the result of subtraction
   * @param <T> concrete type that extends @{@link Number}
   */
  public static <T extends Number> Number subtract(T x, T y) {
    DynamicNumber dx = DynamicNumber.of(x);
    DynamicNumber dy = DynamicNumber.of(y);
    return subtract(dx, dy);
  }

  /**
   * Subtracts two numeric values represented as strings.
   *
   * @param x left operand
   * @param y right operand
   * @return the result of subtraction
   */
  public static Number subtract(Object x, Object y) {
    DynamicNumber dx = DynamicNumber.of(x);
    DynamicNumber dy = DynamicNumber.of(y);
    return subtract(dx, dy);
  }

  /**
   * Subtracts two {@link DynamicNumber} instances with type promotion.
   *
   * @param dx left operand
   * @param dy right operand
   * @return the result of subtraction
   */
  public static Number subtract(DynamicNumber dx, DynamicNumber dy) {
    Number result;
    if (dx.isDecimal() || dy.isDecimal()) {
      if (dx.isBigDecimal() || dy.isBigDecimal()) {
        result = dx.asBigDecimal().subtract(dy.asBigDecimal());
      } else if (dx.isDouble() || dy.isDouble()) {
        result = dx.asDouble() - dy.asDouble();
      } else {
        result = dx.asFloat() - dy.asFloat();
      }
    } else if (dx.isInteger() || dy.isInteger()) {
      if (dx.isBigInteger() || dy.isBigInteger()) {
        result = dx.asBigInteger().subtract(dy.asBigInteger());
      } else if (dx.isLong() || dy.isLong()) {
        result = dx.asLong() - dy.asLong();
      } else if (dx.isInt() || dy.isInt()) {
        result = dx.asInt() - dy.asInt();
      } else if (dx.isShort() || dy.isShort()) {
        result = dx.asShort() - dy.asShort();
      } else {
        result = dx.asByte() - dy.asByte();
      }
    } else {
      // Unknown or unsupported number types
      throw new UnsupportedOperationException(
          "Unsupported number types: " + dx.get().getClass() + ", " + dy.get().getClass());
    }
    return result;
  }

  /**
   * Multiplies two {@link Number} values.
   *
   * @param x left operand
   * @param y right operand
   * @return the result of multiplication
   * @param <T> concrete type that extends @{@link Number}
   */
  public static <T extends Number> Number multiply(T x, T y) {
    DynamicNumber dx = DynamicNumber.of(x);
    DynamicNumber dy = DynamicNumber.of(y);
    return multiply(dx, dy);
  }

  /**
   * Multiplies two numeric values represented as strings.
   *
   * @param x left operand
   * @param y right operand
   * @return the result of multiplication
   */
  public static Number multiply(Object x, Object y) {
    DynamicNumber dx = DynamicNumber.of(x);
    DynamicNumber dy = DynamicNumber.of(y);
    return multiply(dx, dy);
  }

  /**
   * Multiplies two {@link DynamicNumber} instances with type promotion.
   *
   * @param dx left operand
   * @param dy right operand
   * @return the result of multiplication
   */
  public static Number multiply(DynamicNumber dx, DynamicNumber dy) {
    Number result;
    if (dx.isDecimal() || dy.isDecimal()) {
      if (dx.isBigDecimal() || dy.isBigDecimal()) {
        result = dx.asBigDecimal().multiply(dy.asBigDecimal());
      } else if (dx.isDouble() || dy.isDouble()) {
        result = dx.asDouble() * dy.asDouble();
      } else {
        result = dx.asFloat() * dy.asFloat();
      }
    } else if (dx.isInteger() || dy.isInteger()) {
      if (dx.isBigInteger() || dy.isBigInteger()) {
        result = dx.asBigInteger().multiply(dy.asBigInteger());
      } else if (dx.isLong() || dy.isLong()) {
        result = dx.asLong() * dy.asLong();
      } else if (dx.isInt() || dy.isInt()) {
        result = dx.asInt() * dy.asInt();
      } else if (dx.isShort() || dy.isShort()) {
        result = dx.asShort() * dy.asShort();
      } else {
        result = dx.asByte() * dy.asByte();
      }
    } else {
      // Unknown or unsupported number types
      throw new UnsupportedOperationException(
          "Unsupported number types: " + dx.get().getClass() + ", " + dy.get().getClass());
    }
    return result;
  }

  /**
   * Divides two {@link Number} values.
   *
   * @param x the dividend
   * @param y the divisor
   * @return the result of division
   * @param <T> concrete type that extends @{@link Number}
   */
  public static <T extends Number> Number divide(T x, T y) {
    DynamicNumber dx = DynamicNumber.of(x);
    DynamicNumber dy = DynamicNumber.of(y);
    return divide(dx, dy);
  }

  /**
   * Divides two numeric values represented as strings.
   *
   * @param x the dividend
   * @param y the divisor
   * @return the result of division
   */
  public static Number divide(Object x, Object y) {
    DynamicNumber dx = DynamicNumber.of(x);
    DynamicNumber dy = DynamicNumber.of(y);
    return divide(dx, dy);
  }

  /**
   * Divides two {@link DynamicNumber} instances with type promotion.
   *
   * @param dx the dividend
   * @param dy the divisor
   * @return the result of division
   */
  public static Number divide(DynamicNumber dx, DynamicNumber dy) {
    Number result;
    if (dx.isDecimal() || dy.isDecimal()) {
      if (dx.isBigDecimal() || dy.isBigDecimal()) {
        result = dx.asBigDecimal().divide(dy.asBigDecimal(), MathContext.UNLIMITED);
      } else if (dx.isDouble() || dy.isDouble()) {
        result = dx.asDouble() / dy.asDouble();
      } else {
        result = dx.asFloat() / dy.asFloat();
      }
    } else if (dx.isInteger() || dy.isInteger()) {
      if (dx.isBigInteger() || dy.isBigInteger()) {
        result = dx.asBigInteger().divide(dy.asBigInteger());
      } else if (dx.isLong() || dy.isLong()) {
        result = dx.asLong() / dy.asLong();
      } else if (dx.isInt() || dy.isInt()) {
        result = dx.asInt() / dy.asInt();
      } else if (dx.isShort() || dy.isShort()) {
        result = dx.asShort() / dy.asShort();
      } else {
        result = dx.asByte() / dy.asByte();
      }
    } else {
      // Unknown or unsupported number types
      throw new UnsupportedOperationException(
          "Unsupported number types: " + dx.get().getClass() + ", " + dy.get().getClass());
    }
    return result;
  }

  /**
   * Computes the modulo (remainder) of two {@link Number} values.
   *
   * @param x the dividend
   * @param y the divisor
   * @return the result of division
   * @param <T> concrete type that extends @{@link Number}
   */
  public static <T extends Number> Number modulo(T x, T y) {
    DynamicNumber dx = DynamicNumber.of(x);
    DynamicNumber dy = DynamicNumber.of(y);
    return modulo(dx, dy);
  }

  /**
   * Computes the modulo (remainder) of two numeric values represented as strings.
   *
   * @param x the dividend
   * @param y the divisor
   * @return the result of division
   */
  public static Number modulo(Object x, Object y) {
    DynamicNumber dx = DynamicNumber.of(x);
    DynamicNumber dy = DynamicNumber.of(y);
    return modulo(dx, dy);
  }

  /**
   * Computes the modulo (remainder) of two {@link DynamicNumber} instances with type promotion.
   *
   * @param dx the dividend
   * @param dy the divisor
   * @return the result of division
   */
  public static Number modulo(DynamicNumber dx, DynamicNumber dy) {
    Number result;
    if (dx.isDecimal() || dy.isDecimal()) {
      if (dx.isBigDecimal() || dy.isBigDecimal()) {
        result = dx.asBigDecimal().remainder(dy.asBigDecimal());
      } else if (dx.isDouble() || dy.isDouble()) {
        result = dx.asDouble() % dy.asDouble();
      } else {
        result = dx.asFloat() % dy.asFloat();
      }
    } else if (dx.isInteger() || dy.isInteger()) {
      if (dx.isBigInteger() || dy.isBigInteger()) {
        result = dx.asBigInteger().remainder(dy.asBigInteger());
      } else if (dx.isLong() || dy.isLong()) {
        result = dx.asLong() % dy.asLong();
      } else if (dx.isInt() || dy.isInt()) {
        result = dx.asInt() % dy.asInt();
      } else if (dx.isShort() || dy.isShort()) {
        result = dx.asShort() % dy.asShort();
      } else {
        result = dx.asByte() % dy.asByte();
      }
    } else {
      // Unknown or unsupported number types
      throw new UnsupportedOperationException(
          "Unsupported number types: " + dx.get().getClass() + ", " + dy.get().getClass());
    }
    return result;
  }

  /**
   * Returns the greater of two {@link Number} values.
   *
   * @param x the first number
   * @param y the second number
   * @return the greatest of {@code x} and {@code y}, as a {@code Number}
   * @param <T> concrete type that extends @{@link Number}
   */
  public static <T extends Number> Number max(T x, T y) {
    DynamicNumber dx = DynamicNumber.of(x);
    DynamicNumber dy = DynamicNumber.of(y);
    return max(dx, dy);
  }

  /**
   * Returns the greater of two numeric values represented as strings.
   *
   * @param x the first number
   * @param y the second number
   * @return the greatest of {@code x} and {@code y}, as a {@code Number}
   */
  public static Number max(Object x, Object y) {
    DynamicNumber dx = DynamicNumber.of(x);
    DynamicNumber dy = DynamicNumber.of(y);
    return max(dx, dy);
  }

  /**
   * Returns the greater of two {@link DynamicNumber} instances with type promotion.
   *
   * @param dx the first number
   * @param dy the second number
   * @return the greatest of {@code dx} and {@code dy}, as a {@code Number}
   */
  public static Number max(DynamicNumber dx, DynamicNumber dy) {
    Number result;
    if (dx.isDecimal() || dy.isDecimal()) {
      if (dx.isBigDecimal() || dy.isBigDecimal()) {
        result = dx.asBigDecimal().max(dy.asBigDecimal());
      } else if (dx.isDouble() || dy.isDouble()) {
        result = Math.max(dx.asDouble(), dy.asDouble());
      } else {
        result = Math.max(dx.asFloat(), dy.asFloat());
      }
    } else if (dx.isInteger() || dy.isInteger()) {
      if (dx.isBigInteger() || dy.isBigInteger()) {
        result = dx.asBigInteger().max(dy.asBigInteger());
      } else if (dx.isLong() || dy.isLong()) {
        result = Math.max(dx.asLong(), dy.asLong());
      } else {
        result = Math.max(dx.asInt(), dy.asInt());
      }
    } else {
      // Unknown or unsupported number types
      throw new UnsupportedOperationException(
          "Unsupported number types: " + dx.get().getClass() + ", " + dy.get().getClass());
    }
    return result;
  }

  /**
   * Returns the lesser of two {@link Number} values.
   *
   * @param x the first number
   * @param y the second number
   * @return the least of {@code x} and {@code y}, as a {@code Number}
   * @param <T> concrete type that extends @{@link Number}
   */
  public static <T extends Number> Number min(T x, T y) {
    DynamicNumber dx = DynamicNumber.of(x);
    DynamicNumber dy = DynamicNumber.of(y);
    return min(dx, dy);
  }

  /**
   * Returns the lesser of two numeric values represented as strings.
   *
   * @param x the first number
   * @param y the second number
   * @return the least of {@code x} and {@code y}, as a {@code Number}
   */
  public static Number min(Object x, Object y) {
    DynamicNumber dx = DynamicNumber.of(x);
    DynamicNumber dy = DynamicNumber.of(y);
    return min(dx, dy);
  }

  /**
   * Returns the lesser of two {@link DynamicNumber} instances with type promotion.
   *
   * @param dx the first number
   * @param dy the second number
   * @return the least of {@code dx} and {@code dy}, as a {@code Number}
   */
  public static Number min(DynamicNumber dx, DynamicNumber dy) {
    Number result;
    if (dx.isDecimal() || dy.isDecimal()) {
      if (dx.isBigDecimal() || dy.isBigDecimal()) {
        result = dx.asBigDecimal().min(dy.asBigDecimal());
      } else if (dx.isDouble() || dy.isDouble()) {
        result = Math.min(dx.asDouble(), dy.asDouble());
      } else {
        result = Math.min(dx.asFloat(), dy.asFloat());
      }
    } else if (dx.isInteger() || dy.isInteger()) {
      if (dx.isBigInteger() || dy.isBigInteger()) {
        result = dx.asBigInteger().min(dy.asBigInteger());
      } else if (dx.isLong() || dy.isLong()) {
        result = Math.min(dx.asLong(), dy.asLong());
      } else {
        result = Math.min(dx.asInt(), dy.asInt());
      }
    } else {
      // Unknown or unsupported number types
      throw new UnsupportedOperationException(
          "Unsupported number types: " + dx.get().getClass() + ", " + dy.get().getClass());
    }
    return result;
  }

  /**
   * Raises a number {@link Number} to the power of another.
   *
   * @param base the base number
   * @param exponent the exponent
   * @return the result of raising {@code base} to the power of {@code exponent}, as a {@code
   *     Number}
   * @param <T> concrete type that extends @{@link Number}
   */
  public static <T extends Number> Number pow(T base, int exponent) {
    DynamicNumber dx = DynamicNumber.of(base);
    return pow(dx, exponent);
  }

  /**
   * Raises a number represented as string to the power of another.
   *
   * @param base the base number
   * @param exponent the exponent
   * @return the result of raising {@code base} to the power of {@code exponent}, as a {@code
   *     Number}
   */
  public static Number pow(Object base, int exponent) {
    DynamicNumber dx = DynamicNumber.of(base);
    return pow(dx, exponent);
  }

  /**
   * Raises a number {@link DynamicNumber} to the power of another.
   *
   * @param base the base number
   * @param exponent the exponent
   * @return the result of raising {@code base} to the power of {@code exponent}, as a {@code
   *     Number}
   */
  public static Number pow(DynamicNumber base, int exponent) {
    if (base.isBigDecimal() || base.isBigInteger()) {
      return base.asBigDecimal().pow(exponent);
    } else if (base.isBigInteger()) {
      return base.asBigInteger().pow(exponent);
    }
    return Math.pow(base.asDouble(), exponent);
  }

  /**
   * Rounds the given number {@link Number} to the nearest integer.
   *
   * @param x the number to round
   * @return the rounded number
   * @param <T> concrete type that extends @{@link Number}
   */
  public static <T extends Number> Number round(T x) {
    DynamicNumber dx = DynamicNumber.of(x);
    return round(dx);
  }

  /**
   * Rounds the given number represented as string to the nearest integer.
   *
   * @param x the number to round
   * @return the rounded number
   */
  public static Number round(Object x) {
    DynamicNumber dx = DynamicNumber.of(x);
    return round(dx);
  }

  /**
   * Rounds the given number {@link DynamicNumber} to the nearest integer.
   *
   * @param dx the number to round
   * @return the rounded number
   */
  public static Number round(DynamicNumber dx) {
    if (dx.isDecimal() || dx.isInteger()) return dx.asBigDecimal().round(MathContext.UNLIMITED);
    if (dx.isDouble() || dx.isLong()) return Math.round(dx.asDouble());
    return Math.round(dx.asFloat());
  }

  /**
   * Returns the largest integer less than or equal the given number {@link Number} to the nearest
   * integer.
   *
   * @param x the number to round
   * @return the rounded number
   * @param <T> concrete type that extends @{@link Number}
   */
  public static <T extends Number> Number floor(T x) {
    DynamicNumber dx = DynamicNumber.of(x);
    return floor(dx);
  }

  /**
   * Returns the largest integer less than or equal the given number represented as string to the
   * nearest integer.
   *
   * @param x the number to round
   * @return the rounded number
   */
  public static Number floor(Object x) {
    DynamicNumber dx = DynamicNumber.of(x);
    return floor(dx);
  }

  /**
   * Returns the largest integer less than or equal the given number {@link DynamicNumber} to the
   * nearest integer.
   *
   * @param dx the number to round
   * @return the rounded number
   */
  public static Number floor(DynamicNumber dx) {
    if (dx.isBigDecimal()) {
      return dx.asBigDecimal().setScale(0, RoundingMode.FLOOR);
    } else if (dx.isBigInteger()) {
      return dx.asBigInteger(); // floor is identity for BigInteger
    } else if (dx.isDouble() || dx.isFloat()) {
      return Math.floor(dx.asDouble());
    } else if (dx.isLong()) {
      return dx.asLong();
    } else if (dx.isInt()) {
      return dx.asInt();
    } else if (dx.isShort()) {
      return dx.asShort();
    } else if (dx.isByte()) {
      return dx.asByte();
    } else {
      // Unknown or unsupported number types
      throw new UnsupportedOperationException("Unsupported number type: " + dx.get().getClass());
    }
  }

  /**
   * Returns the smallest integer greater than or equal the given number {@link Number} to the
   * nearest integer.
   *
   * @param x the number to apply ceiling to
   * @return the ceiling value
   * @param <T> concrete type that extends @{@link Number}
   */
  public static <T extends Number> Number ceil(T x) {
    DynamicNumber dx = DynamicNumber.of(x);
    return ceil(dx);
  }

  /**
   * Returns the smallest integer greater than or equal the given number represented as string to
   * the nearest integer.
   *
   * @param x the number to apply ceiling to
   * @return the ceiling value
   */
  public static Number ceil(Object x) {
    DynamicNumber dx = DynamicNumber.of(x);
    return ceil(dx);
  }

  /**
   * Returns the smallest integer greater than or equal the given number {@link DynamicNumber} to
   * the nearest integer.
   *
   * @param dx the number to apply ceiling to
   * @return the ceiling value
   */
  public static Number ceil(DynamicNumber dx) {
    if (dx.isBigDecimal()) {
      return dx.asBigDecimal().setScale(0, RoundingMode.CEILING);
    } else if (dx.isBigInteger()) {
      return dx.asBigInteger(); // floor is identity for BigInteger
    } else if (dx.isDouble() || dx.isFloat()) {
      return Math.ceil(dx.asDouble());
    } else if (dx.isLong()) {
      return dx.asLong();
    } else if (dx.isInt()) {
      return dx.asInt();
    } else if (dx.isShort()) {
      return dx.asShort();
    } else if (dx.isByte()) {
      return dx.asByte();
    } else {
      // Unknown or unsupported number types
      throw new UnsupportedOperationException("Unsupported number type: " + dx.get().getClass());
    }
  }

  /**
   * Returns the negation of the given number {@link Number}.
   *
   * @param x the number to negate
   * @return the negated number
   * @param <T> concrete type that extends @{@link Number}
   */
  public static <T extends Number> Number negate(T x) {
    DynamicNumber dx = DynamicNumber.of(x);
    return negate(dx);
  }

  /**
   * Returns the negation of the given number represented as string.
   *
   * @param x the number to negate
   * @return the negated number
   */
  public static Number negate(Object x) {
    DynamicNumber dx = DynamicNumber.of(x);
    return negate(dx);
  }

  /**
   * Returns the negation of the given number {@link DynamicNumber}.
   *
   * @param dx the number to negate
   * @return the negated number
   */
  public static Number negate(DynamicNumber dx) {
    Number result;
    if (dx.isBigDecimal()) result = dx.asBigDecimal().negate();
    else if (dx.isDouble()) result = -dx.asDouble();
    else if (dx.isFloat()) result = -dx.asFloat();
    else if (dx.isBigInteger()) result = dx.asBigInteger().negate();
    else if (dx.isLong()) result = -dx.asLong();
    else if (dx.isInt()) result = -dx.asInt();
    else if (dx.isShort()) result = -dx.asShort();
    else if (dx.isByte()) result = -dx.asByte();
    else {
      // Unknown or unsupported number types
      throw new UnsupportedOperationException("Unsupported number type: " + dx.get().getClass());
    }
    return result;
  }

  /**
   * Returns the square root of the given number {@link Number}.
   *
   * @param x the number to compute the square root of
   * @return the square root of the number
   * @param <T> concrete type that extends @{@link Number}
   */
  public static <T extends Number> Number sqrt(T x) {
    DynamicNumber dx = DynamicNumber.of(x);
    return sqrt(dx);
  }

  /**
   * Returns the square root of the given number represented as string.
   *
   * @param x the number to compute the square root of
   * @return the square root of the number
   */
  public static Number sqrt(Object x) {
    DynamicNumber dx = DynamicNumber.of(x);
    return sqrt(dx);
  }

  /**
   * Returns the square root of the given number {@link DynamicNumber}.
   *
   * @param dx the number to compute the square root of
   * @return the square root of the number
   */
  public static Number sqrt(DynamicNumber dx) {
    if (dx.isBigDecimal()) return dx.asBigDecimal().sqrt(MathContext.UNLIMITED);
    if (dx.isBigInteger()) return dx.asBigInteger().sqrt();
    return Math.sqrt(dx.asDouble());
  }

  /**
   * Returns the absolute value of the given number {@link Number}.
   *
   * @param x the number to compute the absolute value of
   * @return the absolute value of the number
   * @param <T> concrete type that extends @{@link Number}
   */
  public static <T extends Number> Number abs(T x) {
    DynamicNumber dx = DynamicNumber.of(x);
    return abs(dx);
  }

  /**
   * Returns the absolute value of the given number represented as string.
   *
   * @param x the number to compute the absolute value of
   * @return the absolute value of the number
   */
  public static Number abs(Object x) {
    DynamicNumber dx = DynamicNumber.of(x);
    return abs(dx);
  }

  /**
   * Returns the absolute value of the given number {@link DynamicNumber}.
   *
   * @param dx the number to compute the absolute value of
   * @return the absolute value of the number
   */
  public static Number abs(DynamicNumber dx) {
    if (dx.isDecimal()) return dx.asBigDecimal().abs();
    if (dx.isBigInteger()) return dx.asBigInteger().abs();
    return Math.abs(dx.asDouble());
  }

  /**
   * Returns the signum of the given number {@link Number}.
   *
   * @param x the number to compute the signum of
   * @return -1 if the number is negative, 0 if zero, and 1 if positive
   * @param <T> concrete type that extends @{@link Number}
   */
  public static <T extends Number> int signum(T x) {
    DynamicNumber dx = DynamicNumber.of(x);
    return signum(dx);
  }

  /**
   * Returns the signum of the given number represented as string.
   *
   * @param x the number to compute the signum of
   * @return -1 if the number is negative, 0 if zero, and 1 if positive
   */
  public static int signum(Object x) {
    DynamicNumber dx = DynamicNumber.of(x);
    return signum(dx);
  }

  /**
   * Returns the signum of the given number {@link DynamicNumber}.
   *
   * @param dx the number to compute the signum of
   * @return -1 if the number is negative, 0 if zero, and 1 if positive
   */
  public static int signum(DynamicNumber dx) {
    if (dx.isDecimal()) return dx.asBigDecimal().signum();
    if (dx.isInteger()) return dx.asBigInteger().signum();
    return Double.compare(dx.asDouble(), 0);
  }

  /**
   * Checks if the given number {@link Number} is zero.
   *
   * @param x the number to check
   * @return {@code true} if the number is zero; {@code false} otherwise
   * @param <T> concrete type that extends @{@link Number}
   */
  public static <T extends Number> boolean isZero(T x) {
    DynamicNumber dx = DynamicNumber.of(x);
    return isZero(dx);
  }

  /**
   * Checks if the given number is zero represented as string.
   *
   * @param x the number to check
   * @return {@code true} if the number is zero; {@code false} otherwise
   */
  public static boolean isZero(Object x) {
    DynamicNumber dx = DynamicNumber.of(x);
    return isZero(dx);
  }

  /**
   * Checks if the given number {@link DynamicNumber} is zero.
   *
   * @param dx the number to check
   * @return {@code true} if the number is zero; {@code false} otherwise
   */
  public static boolean isZero(DynamicNumber dx) {
    return signum(dx) == 0;
  }

  /**
   * Checks if two numbers {@link Number} are equal.
   *
   * @param x the first number
   * @param y the second number
   * @return {@code true} if {@code x} and {@code y} are equal in value; {@code false} otherwise
   * @param <T> concrete type that extends @{@link Number}
   */
  public static <T extends Number> boolean equals(T x, T y) {
    DynamicNumber dx = DynamicNumber.of(x);
    DynamicNumber dy = DynamicNumber.of(y);
    return equals(dx, dy);
  }

  /**
   * Checks if two numbers, represented as strings, are equal.
   *
   * @param x the first number
   * @param y the second number
   * @return {@code true} if {@code x} and {@code y} are equal in value; {@code false} otherwise
   */
  public static boolean equals(Object x, Object y) {
    DynamicNumber dx = DynamicNumber.of(x);
    DynamicNumber dy = DynamicNumber.of(y);
    return equals(dx, dy);
  }

  /**
   * Checks if two numbers {@link DynamicNumber} are equal.
   *
   * @param dx the first number
   * @param dy the second number
   * @return {@code true} if {@code dx} and {@code dy} are equal in value; {@code false} otherwise
   */
  public static boolean equals(DynamicNumber dx, DynamicNumber dy) {
    return compare(dx, dy) == 0;
  }

  /**
   * Compares two numbers {@link Number}.
   *
   * @param x the first number
   * @param y the second number
   * @return a negative integer if {@code x < y}; zero if {@code x == y}; a positive integer if
   *     {@code x > y}
   * @param <T> concrete type that extends @{@link Number}
   */
  public static <T extends Number> int compare(T x, T y) {
    DynamicNumber dx = DynamicNumber.of(x);
    DynamicNumber dy = DynamicNumber.of(y);
    return compare(dx, dy);
  }

  /**
   * Compares two numbers, represented as strings.
   *
   * @param x the first number
   * @param y the second number
   * @return a negative integer if {@code x < y}; zero if {@code x == y}; a positive integer if
   *     {@code x > y}
   */
  public static int compare(Object x, Object y) {
    DynamicNumber dx = DynamicNumber.of(x);
    DynamicNumber dy = DynamicNumber.of(y);
    return compare(dx, dy);
  }

  /**
   * Compares two numbers {@link DynamicNumber}.
   *
   * @param dx the first number
   * @param dy the second number
   * @return a negative integer if {@code dx < dy}; zero if {@code dx == dy}; a positive integer if
   *     {@code dx > dy}
   */
  public static int compare(DynamicNumber dx, DynamicNumber dy) {
    if (dx.isDecimal() || dy.isDecimal()) {
      return dx.asBigDecimal().compareTo(dy.asBigDecimal());
    } else if (dx.isInteger() || dy.isInteger()) {
      return dx.asBigInteger().compareTo(dy.asBigInteger());
    } else {
      // Unknown or unsupported number types
      throw new UnsupportedOperationException(
          "Unsupported number types: " + dx.get().getClass() + ", " + dy.get().getClass());
    }
  }

  public static <T extends Number> Number and(T x, T y) {
    DynamicNumber dx = DynamicNumber.of(x);
    DynamicNumber dy = DynamicNumber.of(y);
    return and(dx, dy);
  }

  public static Number and(Object x, Object y) {
    DynamicNumber dx = DynamicNumber.of(x);
    DynamicNumber dy = DynamicNumber.of(y);
    return and(dx, dy);
  }

  /**
   * Performs a bitwise AND operation on the given numbers {@link DynamicNumber}.
   *
   * @param dx the first number
   * @param dy the first number
   * @return the number representing {@code x & y}
   */
  public static Number and(DynamicNumber dx, DynamicNumber dy) {
    if (dx.isDecimal() || dy.isDecimal()) {
      throw new UnsupportedOperationException(
          "Bitwise operations are not supported on floating-point numbers.");
    } else if (dx.isBigInteger()) {
      return dx.asBigInteger().and(dy.asBigInteger());
    } else if (dx.isLong()) {
      return dx.asLong() & dy.asLong();
    } else if (dx.isInt()) {
      return dx.asInt() & dy.asInt();
    } else if (dx.isShort()) {
      return dx.asShort() & dy.asShort();
    } else if (dx.isByte()) {
      return dx.asByte() & dy.asByte();
    } else {
      // Unknown or unsupported number types
      throw new UnsupportedOperationException("Unsupported number type: " + dx.get().getClass());
    }
  }

  public static <T extends Number> Number or(T x, T y) {
    DynamicNumber dx = DynamicNumber.of(x);
    DynamicNumber dy = DynamicNumber.of(y);
    return or(dx, dy);
  }

  public static Number or(Object x, Object y) {
    DynamicNumber dx = DynamicNumber.of(x);
    DynamicNumber dy = DynamicNumber.of(y);
    return or(dx, dy);
  }

  /**
   * Performs a bitwise OR operation on the given numbers {@link DynamicNumber}.
   *
   * @param dx the first number
   * @param dy the first number
   * @return the number representing {@code x | y}
   */
  public static Number or(DynamicNumber dx, DynamicNumber dy) {
    if (dx.isDecimal() || dy.isDecimal()) {
      throw new UnsupportedOperationException(
          "Bitwise operations are not supported on floating-point numbers.");
    } else if (dx.isBigInteger()) {
      return dx.asBigInteger().or(dy.asBigInteger());
    } else if (dx.isLong()) {
      return dx.asLong() | dy.asLong();
    } else if (dx.isInt()) {
      return dx.asInt() | dy.asInt();
    } else if (dx.isShort()) {
      return dx.asShort() | dy.asShort();
    } else if (dx.isByte()) {
      return dx.asByte() | dy.asByte();
    } else {
      // Unknown or unsupported number types
      throw new UnsupportedOperationException("Unsupported number type: " + dx.get().getClass());
    }
  }

  public static <T extends Number> Number xor(T x, T y) {
    DynamicNumber dx = DynamicNumber.of(x);
    DynamicNumber dy = DynamicNumber.of(y);
    return xor(dx, dy);
  }

  public static Number xor(Object x, Object y) {
    DynamicNumber dx = DynamicNumber.of(x);
    DynamicNumber dy = DynamicNumber.of(y);
    return xor(dx, dy);
  }

  /**
   * Performs a bitwise XOR operation on the given numbers {@link DynamicNumber}.
   *
   * @param dx the first number
   * @param dy the first number
   * @return the number representing {@code x ^ y}
   */
  public static Number xor(DynamicNumber dx, DynamicNumber dy) {
    if (dx.isDecimal() || dy.isDecimal()) {
      throw new UnsupportedOperationException(
          "Bitwise operations are not supported on floating-point numbers.");
    } else if (dx.isBigInteger()) {
      return dx.asBigInteger().xor(dy.asBigInteger());
    } else if (dx.isLong()) {
      return dx.asLong() ^ dy.asLong();
    } else if (dx.isInt()) {
      return dx.asInt() ^ dy.asInt();
    } else if (dx.isShort()) {
      return dx.asShort() ^ dy.asShort();
    } else if (dx.isByte()) {
      return dx.asByte() ^ dy.asByte();
    } else {
      // Unknown or unsupported number types
      throw new UnsupportedOperationException("Unsupported number type: " + dx.get().getClass());
    }
  }

  public static <T extends Number> Number not(T x) {
    DynamicNumber dx = DynamicNumber.of(x);
    return not(dx);
  }

  public static Number not(Object x) {
    DynamicNumber dx = DynamicNumber.of(x);
    return not(dx);
  }

  /**
   * Performs a bitwise NOT operation on the given number {@link DynamicNumber}.
   *
   * @param dx the number to apply bitwise NOT operation to
   * @return the number representing {@code ~x}
   */
  public static Number not(DynamicNumber dx) {
    if (dx.isDecimal()) {
      throw new UnsupportedOperationException(
          "Bitwise operations are not supported on floating-point numbers.");
    } else if (dx.isBigInteger()) {
      return dx.asBigInteger().not();
    } else if (dx.isLong()) {
      return ~dx.asLong();
    } else if (dx.isInt()) {
      return ~dx.asInt();
    } else if (dx.isShort()) {
      return ~dx.asShort();
    } else if (dx.isByte()) {
      return ~dx.asByte();
    } else {
      // Unknown or unsupported number types
      throw new UnsupportedOperationException("Unsupported number type: " + dx.get().getClass());
    }
  }

  public static <T extends Number> Number shiftLeft(T x, int positions) {
    DynamicNumber dx = DynamicNumber.of(x);
    return shiftLeft(dx, positions);
  }

  public static Number shiftLeft(Object x, int positions) {
    DynamicNumber dx = DynamicNumber.of(x);
    return shiftLeft(dx, positions);
  }

  /**
   * Shifts the given number {@link DynamicNumber} to the left (signed shift).
   *
   * <p>This is equivalent to multiplying the value by {@code 2^n}.
   *
   * @param dx the number to shift
   * @param positions the number of bits to shift to the left; must be non-negative
   * @return the result of {@code value << n}
   */
  public static Number shiftLeft(DynamicNumber dx, int positions) {
    if (dx.isDecimal()) {
      throw new UnsupportedOperationException(
          "Bitwise operations are not supported on floating-point numbers.");
    } else if (dx.isBigInteger()) {
      return dx.asBigInteger().shiftLeft(positions);
    } else if (dx.isLong()) {
      return dx.asLong() << positions;
    } else if (dx.isInt()) {
      return dx.asInt() << positions;
    } else if (dx.isShort()) {
      return dx.asShort() << positions;
    } else if (dx.isByte()) {
      return dx.asByte() << positions;
    } else {
      // Unknown or unsupported number types
      throw new UnsupportedOperationException("Unsupported number type: " + dx.get().getClass());
    }
  }

  public static <T extends Number> Number shiftRight(T x, int positions) {
    DynamicNumber dx = DynamicNumber.of(x);
    return shiftRight(dx, positions);
  }

  public static Number shiftRight(Object x, int positions) {
    DynamicNumber dx = DynamicNumber.of(x);
    return shiftRight(dx, positions);
  }

  /**
   * Shifts the given number {@link DynamicNumber} to the right (signed shift).
   *
   * <p>This is equivalent to dividing the value by {@code 2^n}, with sign extension.
   *
   * @param dx the number to shift
   * @param positions the number of bits to shift to the right; must be non-negative
   * @return the result of {@code value >> n}
   */
  public static Number shiftRight(DynamicNumber dx, int positions) {
    if (dx.isDecimal()) {
      throw new UnsupportedOperationException(
          "Bitwise operations are not supported on floating-point numbers.");
    } else if (dx.isBigInteger()) {
      return dx.asBigInteger().shiftRight(positions);
    } else if (dx.isLong()) {
      return dx.asLong() >> positions;
    } else if (dx.isInt()) {
      return dx.asInt() >> positions;
    } else if (dx.isShort()) {
      return dx.asShort() >> positions;
    } else if (dx.isByte()) {
      return dx.asByte() >> positions;
    } else {
      // Unknown or unsupported number types
      throw new UnsupportedOperationException("Unsupported number type: " + dx.get().getClass());
    }
  }

  public static <T extends Number> Number unsignedShiftRight(T x, int positions) {
    DynamicNumber dx = DynamicNumber.of(x);
    return unsignedShiftRight(dx, positions);
  }

  public static Number unsignedShiftRight(Object x, int positions) {
    DynamicNumber dx = DynamicNumber.of(x);
    return unsignedShiftRight(dx, positions);
  }

  /**
   * Shifts the given number {@link DynamicNumber} to the right (unsigned shift).
   *
   * <p>This is equivalent to dividing the value by {@code 2^n}, with sign extension.
   *
   * @param dx the number to shift
   * @param positions the number of bits to shift to the right; must be non-negative
   * @return the result of an unsigned right shift (zero-fill) of {@code value} by {@code n} bits
   */
  public static Number unsignedShiftRight(DynamicNumber dx, int positions) {
    if (dx.isDecimal()) {
      throw new UnsupportedOperationException(
          "Bitwise operations are not supported on floating-point numbers.");
    } else if (dx.isBigInteger()) {
      return unsignedShiftRight(dx.asBigInteger(), positions);
    } else if (dx.isLong()) {
      return dx.asLong() >>> positions;
    } else if (dx.isInt()) {
      return dx.asInt() >>> positions;
    } else if (dx.isShort()) {
      return dx.asShort() >>> positions;
    } else if (dx.isByte()) {
      return dx.asByte() >>> positions;
    } else {
      // Unknown or unsupported number types
      throw new UnsupportedOperationException("Unsupported number type: " + dx.get().getClass());
    }
  }

  public static <T extends Number> Number PostIncrement(T x) {
    DynamicNumber dx = DynamicNumber.of(x);
    return PostIncrement(dx);
  }

  public static Number PostIncrement(Object x) {
    DynamicNumber dx = DynamicNumber.of(x);
    return PostIncrement(dx);
  }

  public static Number PostIncrement(DynamicNumber dx) {
    if (dx.isBigDecimal()) {
      return dx.set(dx.asBigDecimal().add(BigDecimal.ONE)).asBigDecimal();
    } else if (dx.isBigInteger()) {
      return dx.set(dx.asBigInteger().add(BigInteger.ONE)).asBigInteger();
    } else if (dx.isDouble()) {
      return dx.set(dx.asDouble() + 1).asDouble();
    } else if (dx.isFloat()) {
      return dx.set(dx.asFloat() + 1).asFloat();
    } else if (dx.isLong()) {
      return dx.set(dx.asLong() + 1).asLong();
    } else if (dx.isInt()) {
      return dx.set(dx.asInt() + 1).asInt();
    } else if (dx.isShort()) {
      return dx.set(dx.asShort() + 1).asShort();
    } else if (dx.isByte()) {
      return dx.set(dx.asByte() + 1).asByte();
    } else {
      // Unknown or unsupported number types
      throw new UnsupportedOperationException("Unsupported number type: " + dx.get().getClass());
    }
  }

  public static <T extends Number> Number PreIncrement(T x) {
    DynamicNumber dx = DynamicNumber.of(x);
    return PreIncrement(dx);
  }

  public static Number PreIncrement(Object x) {
    DynamicNumber dx = DynamicNumber.of(x);
    return PreIncrement(dx);
  }

  public static Number PreIncrement(DynamicNumber dx) {
    if (dx.isBigDecimal()) {
      BigDecimal current = dx.asBigDecimal();
      dx.set(dx.asBigDecimal().add(BigDecimal.ONE));
      return current;
    } else if (dx.isBigInteger()) {
      BigInteger current = dx.asBigInteger();
      dx.set(current.add(BigInteger.ONE));
      return current;
    } else if (dx.isDouble()) {
      double current = dx.asDouble();
      dx.set(current + 1);
      return current;
    } else if (dx.isFloat()) {
      double current = dx.asFloat();
      dx.set(current + 1);
      return current;
    } else if (dx.isLong()) {
      double current = dx.asLong();
      dx.set(current + 1);
      return current;
    } else if (dx.isInt()) {
      double current = dx.asInt();
      dx.set(current + 1);
      return current;
    } else if (dx.isShort()) {
      double current = dx.asShort();
      dx.set(current + 1);
      return current;
    } else if (dx.isByte()) {
      double current = dx.asByte();
      dx.set(current + 1);
      return current;
    } else {
      // Unknown or unsupported number types
      throw new UnsupportedOperationException("Unsupported number type: " + dx.get().getClass());
    }
  }

  public static <T extends Number> Number PostDecrement(T x) {
    DynamicNumber dx = DynamicNumber.of(x);
    return PostDecrement(dx);
  }

  public static Number PostDecrement(Object x) {
    DynamicNumber dx = DynamicNumber.of(x);
    return PostDecrement(dx);
  }

  public static Number PostDecrement(DynamicNumber dx) {
    if (dx.isBigDecimal()) {
      return dx.set(dx.asBigDecimal().subtract(BigDecimal.ONE)).asBigDecimal();
    } else if (dx.isBigInteger()) {
      return dx.set(dx.asBigInteger().subtract(BigInteger.ONE)).asBigInteger();
    } else if (dx.isDouble()) {
      return dx.set(dx.asDouble() - 1).asDouble();
    } else if (dx.isFloat()) {
      return dx.set(dx.asFloat() - 1).asFloat();
    } else if (dx.isLong()) {
      return dx.set(dx.asLong() - 1).asLong();
    } else if (dx.isInt()) {
      return dx.set(dx.asInt() - 1).asInt();
    } else if (dx.isShort()) {
      return dx.set(dx.asShort() - 1).asShort();
    } else if (dx.isByte()) {
      return dx.set(dx.asByte() - 1).asByte();
    } else {
      // Unknown or unsupported number types
      throw new UnsupportedOperationException("Unsupported number type: " + dx.get().getClass());
    }
  }

  public static <T extends Number> Number PreDecrement(T x) {
    DynamicNumber dx = DynamicNumber.of(x);
    return PreDecrement(dx);
  }

  public static Number PreDecrement(Object x) {
    DynamicNumber dx = DynamicNumber.of(x);
    return PreDecrement(dx);
  }

  public static Number PreDecrement(DynamicNumber dx) {
    if (dx.isBigDecimal()) {
      BigDecimal current = dx.asBigDecimal();
      dx.set(dx.asBigDecimal().subtract(BigDecimal.ONE));
      return current;
    } else if (dx.isBigInteger()) {
      BigInteger current = dx.asBigInteger();
      dx.set(current.subtract(BigInteger.ONE));
      return current;
    } else if (dx.isDouble()) {
      double current = dx.asDouble();
      dx.set(current - 1);
      return current;
    } else if (dx.isFloat()) {
      double current = dx.asFloat();
      dx.set(current - 1);
      return current;
    } else if (dx.isLong()) {
      double current = dx.asLong();
      dx.set(current - 1);
      return current;
    } else if (dx.isInt()) {
      double current = dx.asInt();
      dx.set(current - 1);
      return current;
    } else if (dx.isShort()) {
      double current = dx.asShort();
      dx.set(current - 1);
      return current;
    } else if (dx.isByte()) {
      double current = dx.asByte();
      dx.set(current - 1);
      return current;
    } else {
      // Unknown or unsupported number types
      throw new UnsupportedOperationException("Unsupported number type: " + dx.get().getClass());
    }
  }

  /**
   * Performs an unsigned (logical) right shift on a {@link BigInteger}.
   *
   * <p>Unlike signed right shift, this operation fills high-order bits with zeros regardless of the
   * sign of the {@code BigInteger}. Since {@code BigInteger} does not have a built-in unsigned
   * right shift, this method emulates it for non-negative values. If the value is negative, the
   * behavior may not match unsigned semantics in lower-level languages like Java's primitive types.
   *
   * @param value the {@code BigInteger} to shift; must not be {@code null}
   * @param n the number of bits to shift right; must be non-negative
   * @return the result of shifting {@code value} to the right by {@code n} bits, using zero-fill
   * @throws NullPointerException if {@code value} is {@code null}
   * @throws IllegalArgumentException if {@code n} is negative
   */
  public static BigInteger unsignedShiftRight(BigInteger value, int n) {
    if (value.signum() >= 0) {
      // If positive, normal shiftRight works as unsigned
      return value.shiftRight(n);
    } else {
      // For negative values:
      // 1. Add 2^(bitLength) to get the unsigned equivalent positive value
      int bitLength = value.bitLength();
      BigInteger twoPowerBitLength = BigInteger.ONE.shiftLeft(bitLength);
      BigInteger unsignedValue = value.add(twoPowerBitLength);

      // 2. Shift right logically
      return unsignedValue.shiftRight(n);
    }
  }
}
