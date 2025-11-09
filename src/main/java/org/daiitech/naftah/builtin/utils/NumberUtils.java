package org.daiitech.naftah.builtin.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Objects;

import org.antlr.v4.runtime.misc.Pair;
import org.daiitech.naftah.builtin.lang.DynamicNumber;
import org.daiitech.naftah.errors.NaftahBugError;

import static java.math.MathContext.DECIMAL128;

import static org.daiitech.naftah.errors.ExceptionUtils.INFINITE_DECIMAL_ERROR;
import static org.daiitech.naftah.errors.ExceptionUtils.NAN_DECIMAL_ERROR;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidNumberConversionOverflowError;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidNumberValueError;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugNullInputError;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugUnsupportedBitwiseDecimalError;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugUnsupportedNumbersError;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahNegativeNumberError;

/**
 * Utility class for dynamically parsing and performing arithmetic operations on
 * various numeric types. Supports Byte, Short, Integer, Long, Float, Double,
 * BigInteger, and BigDecimal.
 *
 * @author Chakib Daii
 */
public final class NumberUtils {
	private static final BigInteger LONG_MIN = BigInteger.valueOf(Long.MIN_VALUE);

	private static final BigInteger LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);

	/**
	 * Private constructor to prevent instantiation.
	 * Always throws a {@link NaftahBugError} when called.
	 */
	private NumberUtils() {
		throw newNaftahBugInvalidUsageError();
	}

	/**
	 * Parses a dynamic numeric value from the given {@code Object}.
	 * <p>
	 * If the object is already an instance of {@link Number}, it is returned as-is.
	 * If the object is a {@link String}, it is parsed using {@link #parseDynamicNumber(String)}.
	 * Otherwise, an exception is thrown indicating that the value is not a valid numeric type.
	 * </p>
	 *
	 * @param object the object to parse as a numeric value; expected to be either a {@link Number} or a
	 *               {@link String}.
	 * @return the parsed {@link Number}.
	 * @throws NaftahBugError if the object is not a valid numeric value.
	 * @see #parseDynamicNumber(String)
	 */
	public static Number parseDynamicNumber(Object object) {
		if (object instanceof Number number) {
			return number;
		}
		else if (object instanceof String string) {
			return parseDynamicNumber(string);
		}
		throw newNaftahBugInvalidNumberValueError(object);
	}

	/**
	 * Parses a string into the most appropriate {@link Number} type.
	 *
	 * <ul>
	 * <li>Parses decimal numbers (with dot or exponent) as Float, Double, or
	 * BigDecimal (in that order).
	 * <li>Parses whole numbers as Byte, Short, Integer, Long, or BigInteger (in
	 * that order).
	 * </ul>
	 *
	 * @param text the input numeric string
	 * @return the parsed Number
	 * @throws RuntimeException if parsing fails or if value is NaN or infinite
	 */
	public static Number parseDynamicNumber(String text) {
		return parseDynamicNumber(text, 10, null);
	}

	/**
	 * Attempts to parse a given text into a {@link Number} using dynamic number parsing logic.
	 * <p>
	 * This method first checks if the input text is "truthy" (i.e., not null, empty, or false-like).
	 * If the text is not truthy, it returns a dynamic number with value {@code 0}.
	 * <p>
	 * If the text is truthy, it tries to parse it using {@link #parseDynamicNumber(String, int, String)}.
	 * If parsing fails due to any exception or {@link NaftahBugError}, it returns a dynamic number with value {@code
	 * Double.NaN}.
	 *
	 * @param text the string to be parsed
	 * @return a {@link Number} parsed from the input text, or {@code 0} if the text is not truthy,
	 *         or {@code Double.NaN} if parsing fails
	 */
	public static Number tryParseDynamicNumber(String text) {
		if (!ObjectUtils.isTruthy(text)) {
			return DynamicNumber.of(0);
		}
		try {
			return parseDynamicNumber(text, 10, null);
		}
		catch (Exception | NaftahBugError ignored) {
			return DynamicNumber.of(Double.NaN);
		}
	}

	/**
	 * Parses a string into the most appropriate {@link Number} type using the specified radix.
	 * <p>
	 * This method supports custom bases for whole numbers (e.g., base 2 to base 36),
	 * while floating-point numbers are always treated as base-10 (radix is ignored).
	 *
	 * <ul>
	 * <li>If the string represents a floating-point number (contains '.' or scientific notation),
	 * it will be parsed as {@link Float}, {@link Double}, or {@link java.math.BigDecimal}.
	 * <li>Otherwise, it is parsed using the given radix into {@link Byte}, {@link Short}, {@link Integer},
	 * {@link Long}, or {@link java.math.BigInteger} depending on value range.
	 * </ul>
	 *
	 * @param text         the numeric string to parse
	 * @param radix        the base to use for whole number parsing (from 2 to 36)
	 * @param originalText the original string (in case of base numbers after using
	 * @return the parsed {@code Number} instance (type chosen dynamically)
	 * @throws RuntimeException if parsing fails, the radix is invalid, or result is NaN/infinite
	 * @{@link org.daiitech.naftah.utils.arabic.ArabicUtils#convertArabicToLatinLetterByLetter(String)})
	 */
	public static Number parseDynamicNumber(String text, DynamicNumber radix, String originalText) {
		return parseDynamicNumber(text, radix.intValue(), originalText);
	}

	/**
	 * Parses a string into the most appropriate {@link Number} type using the specified radix.
	 * <p>
	 * This method supports custom bases for whole numbers (e.g., base 2 to base 36),
	 * while floating-point numbers are always treated as base-10 (radix is ignored).
	 *
	 * <ul>
	 * <li>If the string represents a floating-point number (contains '.' or scientific notation),
	 * it will be parsed as {@link Float}, {@link Double}, or {@link java.math.BigDecimal}.
	 * <li>Otherwise, it is parsed using the given radix into {@link Byte}, {@link Short}, {@link Integer},
	 * {@link Long}, or {@link java.math.BigInteger} depending on value range.
	 * </ul>
	 *
	 * @param text         the numeric string to parse
	 * @param radix        the base to use for whole number parsing (from 2 to 36)
	 * @param originalText the original string (in case of base numbers after using
	 * @return the parsed {@code Number} instance (type chosen dynamically)
	 * @throws RuntimeException if parsing fails, the radix is invalid, or result is NaN/infinite
	 * @{@link org.daiitech.naftah.utils.arabic.ArabicUtils#convertArabicToLatinLetterByLetter(String)})
	 */
	public static Number parseDynamicNumber(String text, int radix, String originalText) {
		if (text == null) {
			throw newNaftahBugNullInputError(true, text);
		}
		DynamicNumber dx = DynamicNumber.of(Double.NaN);
		// Replace all decimal-like characters with a dot
		text = text.replaceAll("[,٫،٬]", ".");
		try {
			if (text.contains(".") || (radix == 10 && text.toLowerCase(Locale.ROOT).contains("e"))) {
				if (radix != 10) {
					throw newNaftahBugInvalidNumberValueError(text, radix);
				}
				try {
					// Try parsing as a float first
					float f = Float.parseFloat(text);
					if (Float.isInfinite(f)) {
						throw new NumberFormatException(
														INFINITE_DECIMAL_ERROR
																.formatted(text));
					}
					if (Float.isNaN(f)) {
						throw new NumberFormatException(NAN_DECIMAL_ERROR
								.formatted(text));
					}

					dx.set(checkPrecision(text, f));
				}
				catch (NumberFormatException e1) {
					try {
						// Try parsing as a double first
						double d = Double.parseDouble(text);
						if (Double.isInfinite(d)) {
							throw new NumberFormatException(
															INFINITE_DECIMAL_ERROR
																	.formatted(text));
						}
						if (Double.isNaN(d)) {
							throw new NumberFormatException(NAN_DECIMAL_ERROR
									.formatted(text));
						}

						dx.set(checkPrecision(text, d));
					}
					catch (NumberFormatException e2) {
						// Fall back to BigDecimal for high-precision decimals
						dx.set(new BigDecimal(text));
					}
				}
			}
			else {
				try {
					dx.set(Byte.parseByte(text, radix));
				}
				catch (NumberFormatException e1) {
					try {
						dx.set(Short.parseShort(text, radix));
					}
					catch (NumberFormatException e2) {
						try {
							// Try parsing as a 32-bit integer
							dx.set(Integer.parseInt(text, radix));
						}
						catch (NumberFormatException e3) {
							try {
								// Try parsing as a 64-bit integer
								dx.set(Long.parseLong(text, radix));
							}
							catch (NumberFormatException e4) {
								// Fall back to arbitrary-precision integer
								dx.set(new BigInteger(text, radix));
							}
						}
					}
				}
			}
		}
		catch (NumberFormatException ex) {
			throw radix == 10 ?
					newNaftahBugInvalidNumberValueError(text, ex) :
					newNaftahBugInvalidNumberValueError(Objects.nonNull(originalText) ?
							new Pair<>(text, originalText) :
							text, radix, ex);
		}
		return dx;
	}

	/**
	 * Convert the given number into an instance of the given target class.
	 *
	 * @param number      the number to convert
	 * @param targetClass the target class to convert to
	 * @return the converted number
	 * @throws IllegalArgumentException if the target class is not supported (i.e. not a standard Number
	 *                                  subclass as included in the JDK)
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
		if (number == null || targetClass == null) {
			throw newNaftahBugNullInputError(false, number, targetClass);
		}

		if (targetClass.isInstance(number)) {
			return (T) number;
		}
		else if (Byte.class == targetClass) {
			long value = checkedLongValue(number, targetClass);
			if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
				throw newNaftahBugInvalidNumberConversionOverflowError(true, number, targetClass);
			}
			return (T) Byte.valueOf(number.byteValue());
		}
		else if (Short.class == targetClass) {
			long value = checkedLongValue(number, targetClass);
			if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
				throw newNaftahBugInvalidNumberConversionOverflowError(true, number, targetClass);
			}
			return (T) Short.valueOf(number.shortValue());
		}
		else if (Integer.class == targetClass) {
			long value = checkedLongValue(number, targetClass);
			if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
				throw newNaftahBugInvalidNumberConversionOverflowError(true, number, targetClass);
			}
			return (T) Integer.valueOf(number.intValue());
		}
		else if (Long.class == targetClass) {
			long value = checkedLongValue(number, targetClass);
			return (T) Long.valueOf(value);
		}
		else if (BigInteger.class == targetClass) {
			if (number instanceof BigDecimal bigDecimal) {
				// do not lose precision - use BigDecimal's own conversion
				return (T) bigDecimal.toBigInteger();
			}
			// original value is not a Big* number - use standard long conversion
			return (T) BigInteger.valueOf(number.longValue());
		}
		else if (Float.class == targetClass) {
			return (T) Float.valueOf(number.floatValue());
		}
		else if (Double.class == targetClass) {
			return (T) Double.valueOf(number.doubleValue());
		}
		else if (BigDecimal.class == targetClass) {
			// always use BigDecimal(String) here to avoid unpredictability of
			// BigDecimal(double)
			// (see BigDecimal javadoc for details)
			return (T) new BigDecimal(number.toString());
		}
		else {
			throw newNaftahBugInvalidNumberConversionOverflowError(false, number, targetClass);
		}
	}

	/**
	 * Check for a {@code BigInteger}/{@code BigDecimal} long overflow before
	 * returning the given number as a long value.
	 *
	 * @param number      the number to convert
	 * @param targetClass the target class to convert to
	 * @return the long value, if convertible without overflow
	 * @throws IllegalArgumentException if there is an overflow
	 * @see org.daiitech.naftah.errors.ExceptionUtils#newNaftahBugInvalidNumberConversionOverflowError
	 */
	private static long checkedLongValue(Number number, Class<? extends Number> targetClass) {
		BigInteger bigInt = null;
		if (number instanceof BigInteger bigInteger) {
			bigInt = bigInteger;
		}
		else if (number instanceof BigDecimal bigDecimal) {
			bigInt = bigDecimal.toBigInteger();
		}
		// Effectively analogous to JDK 8's BigInteger.longValueExact()
		if (bigInt != null && (bigInt.compareTo(LONG_MIN) < 0 || bigInt.compareTo(LONG_MAX) > 0)) {
			throw newNaftahBugInvalidNumberConversionOverflowError(true, number, targetClass);
		}
		return number.longValue();
	}

	/**
	 * Adds two {@link Number} values.
	 *
	 * @param x   left operand
	 * @param y   right operand
	 * @param <T> concrete type that extends @{@link Number}
	 * @return the result of addition
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
		DynamicNumber dr = dx.clone();
		if (dx.isDecimal() || dy.isDecimal()) {
			if (dx.isBigDecimal() || dy.isBigDecimal()) {
				dr.set(dx.asBigDecimal().add(dy.asBigDecimal()));
			}
			else if (dx.isDouble() || dy.isDouble()) {
				double res = dx.doubleValue() + dy.doubleValue();
				if (Double.isInfinite(res) || Double.isNaN(res)) {
					// Promote to BigDecimal
					dr.set(dx.promote().asBigDecimal().add(dy.promote().asBigDecimal()));
				}
				else {
					BigDecimal expected = dx.asBigDecimal().add(dy.asBigDecimal());
					dr.set(checkPrecision(expected, res));
				}
			}
			else {
				float res = dx.floatValue() + dy.floatValue();
				if (Float.isInfinite(res) || Float.isNaN(res)) {
					dr.set(dx.promote().doubleValue() + dy.promote().doubleValue());
				}
				else {
					BigDecimal expected = dx.asBigDecimal().add(dy.asBigDecimal());
					dr.set(checkPrecision(expected, res));
				}
			}
		}
		else if (dx.isInteger() || dy.isInteger()) {
			if (dx.isBigInteger() || dy.isBigInteger()) {
				dr.set(dx.asBigInteger().add(dy.asBigInteger()));
			}
			else if (dx.isLong() || dy.isLong()) {
				long a = dx.longValue();
				long b = dy.longValue();
				try {
					dr.set(Math.addExact(a, b));
				}
				catch (ArithmeticException e) {
					dr.set(BigInteger.valueOf(a).add(BigInteger.valueOf(b)));
				}
			}
			else if (dx.isInt() || dy.isInt()) {
				int a = dx.intValue();
				int b = dy.intValue();
				try {
					dr.set(Math.addExact(a, b));
				}
				catch (ArithmeticException e) {
					dr.set((long) a + b);
				}
			}
			else if (dx.isShort() || dy.isShort()) {
				short a = dx.shortValue();
				short b = dy.shortValue();
				dr.set(a + b); // promotes to int
			}
			else {
				byte a = dx.byteValue();
				byte b = dy.byteValue();
				dr.set(a + b); // promotes to int
			}
		}
		else {
			throw newNaftahBugUnsupportedNumbersError(false, dx, dy);
		}
		return dr.normalize();
	}

	/**
	 * Subtracts two {@link Number} values.
	 *
	 * @param x   left operand
	 * @param y   right operand
	 * @param <T> concrete type that extends @{@link Number}
	 * @return the result of subtraction
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
		DynamicNumber dr = dx.clone();
		if (dx.isDecimal() || dy.isDecimal()) {
			if (dx.isBigDecimal() || dy.isBigDecimal()) {
				dr.set(dx.asBigDecimal().subtract(dy.asBigDecimal()));
			}
			else if (dx.isDouble() || dy.isDouble()) {
				double res = dx.doubleValue() - dy.doubleValue();
				if (Double.isInfinite(res) || Double.isNaN(res)) {
					dr.set(dx.promote().asBigDecimal().subtract(dy.promote().asBigDecimal()));
				}
				else {
					dr.set(res);
				}
			}
			else {
				float res = dx.floatValue() - dy.floatValue();
				if (Float.isInfinite(res) || Float.isNaN(res)) {
					dr.set(dx.promote().doubleValue() - dy.promote().doubleValue());
				}
				else {
					dr.set(res);
				}
			}
		}
		else if (dx.isInteger() || dy.isInteger()) {
			if (dx.isBigInteger() || dy.isBigInteger()) {
				dr.set(dx.asBigInteger().subtract(dy.asBigInteger()));
			}
			else if (dx.isLong() || dy.isLong()) {
				long a = dx.longValue();
				long b = dy.longValue();
				try {
					dr.set(Math.subtractExact(a, b));
				}
				catch (ArithmeticException e) {
					dr.set(BigInteger.valueOf(a).subtract(BigInteger.valueOf(b)));
				}
			}
			else if (dx.isInt() || dy.isInt()) {
				int a = dx.intValue();
				int b = dy.intValue();
				try {
					dr.set(Math.subtractExact(a, b));
				}
				catch (ArithmeticException e) {
					dr.set((long) a - b);
				}
			}
			else if (dx.isShort() || dy.isShort()) {
				short a = dx.shortValue();
				short b = dy.shortValue();
				dr.set(a - b);
			}
			else {
				byte a = dx.byteValue();
				byte b = dy.byteValue();
				dr.set(a - b);
			}
		}
		else {
			throw newNaftahBugUnsupportedNumbersError(false, dx, dy);
		}
		return dr.normalize();
	}

	/**
	 * Multiplies two {@link Number} values.
	 *
	 * @param x   left operand
	 * @param y   right operand
	 * @param <T> concrete type that extends @{@link Number}
	 * @return the result of multiplication
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
		DynamicNumber dr = dx.clone();
		if (dx.isDecimal() || dy.isDecimal()) {
			if (dx.isBigDecimal() || dy.isBigDecimal()) {
				dr.set(dx.asBigDecimal().multiply(dy.asBigDecimal()));
			}
			else if (dx.isDouble() || dy.isDouble()) {
				double res = dx.doubleValue() * dy.doubleValue();

				// Check for overflow or infinity
				if (Double.isInfinite(res) || Double.isNaN(res)) {
					dr.set(dx.promote().asBigDecimal().multiply(dy.promote().asBigDecimal()));
				}
				else {
					dr.set(res);
				}
			}
			else {
				float res = dx.floatValue() * dy.floatValue();
				if (Float.isInfinite(res) || Float.isNaN(res)) {
					dr.set(dx.promote().doubleValue() * dy.promote().doubleValue());
				}
				else {
					dr.set(res);
				}
			}
		}
		else if (dx.isInteger() || dy.isInteger()) {
			// If either is BigInteger, use BigInteger multiplication
			if (dx.isBigInteger() || dy.isBigInteger()) {
				dr.set(dx.asBigInteger().multiply(dy.asBigInteger()));
			}

			else if (dx.isLong() || dy.isLong()) {
				// Handle long carefully (to avoid overflow)
				long a = dx.longValue();
				long b = dy.longValue();

				// Check for overflow using Math.multiplyExact
				try {
					dr.set(Math.multiplyExact(a, b));
				}
				catch (ArithmeticException e) {
					// Promote to BigInteger
					dr.set(BigInteger.valueOf(a).multiply(BigInteger.valueOf(b)));
				}
			}

			else if (dx.isInt() || dy.isInt()) {
				// Handle int (with promotion on overflow)
				int a = dx.intValue();
				int b = dy.intValue();
				try {
					dr.set(Math.multiplyExact(a, b));
				}
				catch (ArithmeticException e) {
					dr.set((long) a * b);
				}
			}

			else if (dx.isShort() || dy.isShort()) {
				// Handle short and byte with safe promotion
				short a = dx.shortValue();
				short b = dy.shortValue();
				dr.set(a * b);
			}
			else {
				byte a = dx.byteValue();
				byte b = dy.byteValue();
				dr.set(a * b);
			}
		}
		else {
			throw newNaftahBugUnsupportedNumbersError(false, dx, dy);
		}
		return dr.normalize();
	}

	/**
	 * Divides two {@link Number} values.
	 *
	 * @param x   the dividend
	 * @param y   the divisor
	 * @param <T> concrete type that extends @{@link Number}
	 * @return the result of division
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
		DynamicNumber dr = dx.clone();
		if (dx.isDecimal() || dy.isDecimal()) {
			if (dx.isBigDecimal() || dy.isBigDecimal()) {
				dr.set(dx.asBigDecimal().divide(dy.asBigDecimal(), MathContext.UNLIMITED));
			}
			else if (dx.isDouble() || dy.isDouble()) {
				dr.set(dx.doubleValue() / dy.doubleValue());
			}
			else {
				dr.set(dx.floatValue() / dy.floatValue());
			}
		}
		else if (dx.isInteger() || dy.isInteger()) {
			if (dx.isBigInteger() || dy.isBigInteger()) {
				dr.set(dx.asBigInteger().divide(dy.asBigInteger()));
			}
			else if (dx.isLong() || dy.isLong()) {
				dr.set(dx.longValue() / dy.longValue());
			}
			else if (dx.isInt() || dy.isInt()) {
				dr.set(dx.intValue() / dy.intValue());
			}
			else if (dx.isShort() || dy.isShort()) {
				dr.set(dx.shortValue() / dy.shortValue());
			}
			else {
				dr.set(dx.byteValue() / dy.byteValue());
			}
		}
		else {
			throw newNaftahBugUnsupportedNumbersError(false, dx, dy);
		}
		return dr.normalize();
	}

	/**
	 * Computes the modulo (remainder) of two {@link Number} values.
	 *
	 * @param x   the dividend
	 * @param y   the divisor
	 * @param <T> concrete type that extends @{@link Number}
	 * @return the result of division
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
	 * Computes the modulo (remainder) of two {@link DynamicNumber} instances with
	 * type promotion.
	 *
	 * @param dx the dividend
	 * @param dy the divisor
	 * @return the result of division
	 */
	public static Number modulo(DynamicNumber dx, DynamicNumber dy) {
		DynamicNumber dr = dx.clone();
		if (dx.isDecimal() || dy.isDecimal()) {
			if (dx.isBigDecimal() || dy.isBigDecimal()) {
				dr.set(dx.asBigDecimal().remainder(dy.asBigDecimal()));
			}
			else if (dx.isDouble() || dy.isDouble()) {
				dr.set(dx.doubleValue() % dy.doubleValue());
			}
			else {
				dr.set(dx.floatValue() % dy.floatValue());
			}
		}
		else if (dx.isInteger() || dy.isInteger()) {
			if (dx.isBigInteger() || dy.isBigInteger()) {
				dr.set(dx.asBigInteger().remainder(dy.asBigInteger()));
			}
			else if (dx.isLong() || dy.isLong()) {
				dr.set(dx.longValue() % dy.longValue());
			}
			else if (dx.isInt() || dy.isInt()) {
				dr.set(dx.intValue() % dy.intValue());
			}
			else if (dx.isShort() || dy.isShort()) {
				dr.set(dx.shortValue() % dy.shortValue());
			}
			else {
				dr.set(dx.byteValue() % dy.byteValue());
			}
		}
		else {
			throw newNaftahBugUnsupportedNumbersError(false, dx, dy);
		}
		return dr.normalize();
	}

	/**
	 * Returns the greater of two {@link Number} values.
	 *
	 * @param x   the first number
	 * @param y   the second number
	 * @param <T> concrete type that extends @{@link Number}
	 * @return the greatest of {@code x} and {@code y}, as a {@code Number}
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
	 * Returns the greater of two {@link DynamicNumber} instances with type
	 * promotion.
	 *
	 * @param dx the first number
	 * @param dy the second number
	 * @return the greatest of {@code dx} and {@code dy}, as a {@code Number}
	 */
	public static Number max(DynamicNumber dx, DynamicNumber dy) {
		DynamicNumber dr = dx.clone();
		if (dx.isDecimal() || dy.isDecimal()) {
			if (dx.isBigDecimal() || dy.isBigDecimal()) {
				dr.set(dx.asBigDecimal().max(dy.asBigDecimal()));
			}
			else if (dx.isDouble() || dy.isDouble()) {
				dr.set(Math.max(dx.doubleValue(), dy.doubleValue()));
			}
			else {
				dr.set(Math.max(dx.floatValue(), dy.floatValue()));
			}
		}
		else if (dx.isInteger() || dy.isInteger()) {
			if (dx.isBigInteger() || dy.isBigInteger()) {
				dr.set(dx.asBigInteger().max(dy.asBigInteger()));
			}
			else if (dx.isLong() || dy.isLong()) {
				dr.set(Math.max(dx.longValue(), dy.longValue()));
			}
			else {
				dr.set(Math.max(dx.intValue(), dy.intValue()));
			}
		}
		else {
			throw newNaftahBugUnsupportedNumbersError(false, dx, dy);
		}
		return dr.normalize();
	}

	/**
	 * Returns the lesser of two {@link Number} values.
	 *
	 * @param x   the first number
	 * @param y   the second number
	 * @param <T> concrete type that extends @{@link Number}
	 * @return the least of {@code x} and {@code y}, as a {@code Number}
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
	 * Returns the lesser of two {@link DynamicNumber} instances with type
	 * promotion.
	 *
	 * @param dx the first number
	 * @param dy the second number
	 * @return the least of {@code dx} and {@code dy}, as a {@code Number}
	 */
	public static Number min(DynamicNumber dx, DynamicNumber dy) {
		DynamicNumber dr = dx.clone();
		if (dx.isDecimal() || dy.isDecimal()) {
			if (dx.isBigDecimal() || dy.isBigDecimal()) {
				dr.set(dx.asBigDecimal().min(dy.asBigDecimal()));
			}
			else if (dx.isDouble() || dy.isDouble()) {
				dr.set(Math.min(dx.doubleValue(), dy.doubleValue()));
			}
			else {
				dr.set(Math.min(dx.floatValue(), dy.floatValue()));
			}
		}
		else if (dx.isInteger() || dy.isInteger()) {
			if (dx.isBigInteger() || dy.isBigInteger()) {
				dr.set(dx.asBigInteger().min(dy.asBigInteger()));
			}
			else if (dx.isLong() || dy.isLong()) {
				dr.set(Math.min(dx.longValue(), dy.longValue()));
			}
			else {
				dr.set(Math.min(dx.intValue(), dy.intValue()));
			}
		}
		else {
			throw newNaftahBugUnsupportedNumbersError(false, dx, dy);
		}
		return dr.normalize();
	}

	/**
	 * Raises a number {@link Number} to the power of another.
	 *
	 * @param base     the base number
	 * @param exponent the exponent
	 * @param <T>      concrete type that extends @{@link Number}
	 * @return the result of raising {@code base} to the power of {@code exponent},
	 *         as a {@code
	 * Number}
	 */
	public static <T extends Number> Number pow(T base, T exponent) {
		DynamicNumber dx = DynamicNumber.of(base);
		return pow(dx, exponent.doubleValue());
	}

	/**
	 * Raises a number represented as string to the power of another.
	 *
	 * @param base     the base number
	 * @param exponent the exponent
	 * @return the result of raising {@code base} to the power of {@code exponent},
	 *         as a {@code
	 * Number}
	 */
	public static Number pow(Object base, double exponent) {
		DynamicNumber dx = DynamicNumber.of(base);
		return pow(dx, exponent);
	}

	/**
	 * Raises a number {@link DynamicNumber} to the power of another.
	 *
	 * @param base     the base number
	 * @param exponent the exponent
	 * @return the result of raising {@code base} to the power of {@code exponent},
	 *         as a {@code
	 * Number}
	 */
	public static Number pow(DynamicNumber base, double exponent) {
		DynamicNumber dr = base.clone();
		if (base.isDecimal()) {
			if (base.isBigDecimal()) {
				// BigDecimal.pow only supports non-negative exponents
				if (exponent < 0) {
					BigDecimal pow = DynamicNumber.of(doPow(base.asBigDecimal(), -exponent)).asBigDecimal();
					BigDecimal result = BigDecimal.ONE
							.divide(pow, RoundingMode.HALF_UP);
					dr.set(result);
				}
				else {
					dr.set(doPow(base.asBigDecimal(), exponent));
				}
			}
			else {
				double res = Math.pow(base.doubleValue(), exponent);
				if (Double.isInfinite(res) || Double.isNaN(res)) {
					// Promote to BigDecimal for better precision and large values
					BigDecimal bdBase = base.promote().asBigDecimal();
					if (exponent < 0) {
						BigDecimal pow = DynamicNumber.of(doPow(bdBase, -exponent)).asBigDecimal();
						dr.set(BigDecimal.ONE.divide(pow, RoundingMode.HALF_UP));
					}
					else {
						dr.set(doPow(bdBase, exponent));
					}
				}
				else {
					dr.set(res);
				}
			}
		}
		else if (base.isInteger()) {
			if (base.isBigInteger()) {
				if (exponent < 0) {
					// Promote to BigDecimal for negative exponent
					BigDecimal bdBase = new BigDecimal(base.asBigInteger());
					BigDecimal pow = DynamicNumber.of(doPow(bdBase, -exponent)).asBigDecimal();
					dr.set(BigDecimal.ONE.divide(pow));
				}
				else {
					dr.set(doPow(base.asBigInteger(), exponent));
				}
			}
			else {
				try {
					double res = Math.pow(base.doubleValue(), exponent);
					if (Double.isInfinite(res) || Double.isNaN(res)) {
						if (exponent < 0) {
							// Promote to BigDecimal for negative exponent
							BigDecimal bdBase = new BigDecimal(base.asBigInteger());
							BigDecimal pow = DynamicNumber.of(doPow(bdBase, -exponent)).asBigDecimal();
							dr.set(BigDecimal.ONE.divide(pow));
						}
						else {
							dr.set(doPow(base.asBigInteger(), exponent));
						}
					}
					else {
						dr.set(res);
					}
				}
				catch (ArithmeticException e) {
					// Overflow, promote to BigInteger and recurse
					BigInteger bigBase = BigInteger.valueOf(base.longValue());
					dr.set(doPow(bigBase, exponent));
				}
			}
		}
		else {
			throw newNaftahBugUnsupportedNumbersError(true, base);
		}
		return dr.normalize();
	}

	/**
	 * Performs exponentiation on a given {@link Number} base and a {@code double} exponent.
	 * <p>
	 * This method converts the base to a {@code double} and uses {@link Math#pow(double, double)}.
	 * If the result or base is {@code NaN} or {@code Infinity}, it falls back to
	 * {@link #powInvalidDouble(Number, double, double)}.
	 *
	 * @param base     the numeric base (can be {@link Integer}, {@link Double}, {@link BigInteger}, etc.)
	 * @param exponent the exponent to raise the base to
	 * @return the result of {@code base ^ exponent}, or a fallback value if the operation is invalid
	 */
	private static Number doPow(Number base, double exponent) {
		double baseDouble = base.doubleValue();
		if (Double.isNaN(baseDouble) || Double.isInfinite(baseDouble)) {
			return powInvalidDouble(base, baseDouble, exponent);
		}
		else {
			double res = Math.pow(base.doubleValue(), exponent);
			if (Double.isNaN(res) || Double.isInfinite(res)) {
				return powInvalidDouble(base, baseDouble, exponent);
			}
			else {
				return res;
			}
		}
	}

	/**
	 * Fallback method for exponentiation when {@link Math#pow(double, double)} returns
	 * {@code NaN} or {@code Infinity}.
	 * <p>
	 * Attempts to use type-specific power operations for {@link BigInteger} and {@link BigDecimal}.
	 * If the base is neither of those, or an exception occurs, returns a {@link DynamicNumber}
	 * containing the {@code double} representation of the base.
	 *
	 * @param base       the original numeric base
	 * @param baseDouble the {@code double} representation of the base
	 * @param exponent   the exponent used in the operation
	 * @return a fallback {@link Number} result for the invalid power operation
	 */
	private static Number powInvalidDouble(Number base, double baseDouble, double exponent) {
		try {
			if (base instanceof BigInteger bigInteger) {
				return bigInteger.pow((int) exponent);
			}
			else if (base instanceof BigDecimal bigDecimal) {
				return bigDecimal.pow((int) exponent);
			}
			else {
				return DynamicNumber.of(baseDouble);
			}
		}
		catch (Exception ignored) {
			return DynamicNumber.of(baseDouble);
		}
	}

	/**
	 * Rounds the given number {@link Number} to the nearest integer.
	 *
	 * @param x   the number to round
	 * @param <T> concrete type that extends @{@link Number}
	 * @return the rounded number
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
		DynamicNumber dr = dx.clone();
		if (dx.isDecimal() || dx.isInteger()) {
			BigDecimal bd = dx.asBigDecimal();
			int signum = bd.signum();
			// Determine how many integer digits exist
			int integerDigits = bd.precision() - bd.scale();
			MathContext context = new MathContext(
													integerDigits > 0 ? integerDigits : 1,
													signum >= 0 ?
															RoundingMode.HALF_UP :
															RoundingMode.HALF_DOWN);
			dr.set(bd.round(context));
		}
		else if (dx.isDouble() || dx.isLong()) {
			dr.set(Math.round(dx.doubleValue()));
		}
		else {
			dr.set(Math.round(dx.floatValue()));
		}
		return dr.normalize();
	}

	/**
	 * Returns the largest integer less than or equal the given number
	 * {@link Number} to the nearest integer.
	 *
	 * @param x   the number to round
	 * @param <T> concrete type that extends @{@link Number}
	 * @return the rounded number
	 */
	public static <T extends Number> Number floor(T x) {
		DynamicNumber dx = DynamicNumber.of(x);
		return floor(dx);
	}

	/**
	 * Returns the largest integer less than or equal the given number represented
	 * as string to the nearest integer.
	 *
	 * @param x the number to round
	 * @return the rounded number
	 */
	public static Number floor(Object x) {
		DynamicNumber dx = DynamicNumber.of(x);
		return floor(dx);
	}

	/**
	 * Returns the largest integer less than or equal the given number
	 * {@link DynamicNumber} to the nearest integer.
	 *
	 * @param dx the number to round
	 * @return the rounded number
	 */
	public static Number floor(DynamicNumber dx) {
		DynamicNumber dr = dx.clone();
		if (dx.isBigDecimal()) {
			dr.set(dx.asBigDecimal().setScale(0, RoundingMode.FLOOR));
		}
		else if (dx.isBigInteger()) {
			dr.set(dx.asBigInteger()); // floor is identity for BigInteger
		}
		else if (dx.isDouble() || dx.isFloat()) {
			dr.set(Math.floor(dx.doubleValue()));
		}
		else if (dx.isLong()) {
			dr.set(dx.longValue());
		}
		else if (dx.isInt()) {
			dr.set(dx.intValue());
		}
		else if (dx.isShort()) {
			dr.set(dx.shortValue());
		}
		else if (dx.isByte()) {
			dr.set(dx.byteValue());
		}
		else {
			throw newNaftahBugUnsupportedNumbersError(true, dx);
		}
		return dr.normalize();
	}

	/**
	 * Returns the smallest integer greater than or equal the given number
	 * {@link Number} to the nearest integer.
	 *
	 * @param x   the number to apply ceiling to
	 * @param <T> concrete type that extends @{@link Number}
	 * @return the ceiling value
	 */
	public static <T extends Number> Number ceil(T x) {
		DynamicNumber dx = DynamicNumber.of(x);
		return ceil(dx);
	}

	/**
	 * Returns the smallest integer greater than or equal the given number
	 * represented as string to the nearest integer.
	 *
	 * @param x the number to apply ceiling to
	 * @return the ceiling value
	 */
	public static Number ceil(Object x) {
		DynamicNumber dx = DynamicNumber.of(x);
		return ceil(dx);
	}

	/**
	 * Returns the smallest integer greater than or equal the given number
	 * {@link DynamicNumber} to the nearest integer.
	 *
	 * @param dx the number to apply ceiling to
	 * @return the ceiling value
	 */
	public static Number ceil(DynamicNumber dx) {
		DynamicNumber dr = dx.clone();
		if (dx.isBigDecimal()) {
			dr.set(dx.asBigDecimal().setScale(0, RoundingMode.CEILING));
		}
		else if (dx.isBigInteger()) {
			dr.set(dx.asBigInteger()); // floor is identity for BigInteger
		}
		else if (dx.isDouble() || dx.isFloat()) {
			dr.set(Math.ceil(dx.doubleValue()));
		}
		else if (dx.isLong()) {
			dr.set(dx.longValue());
		}
		else if (dx.isInt()) {
			dr.set(dx.intValue());
		}
		else if (dx.isShort()) {
			dr.set(dx.shortValue());
		}
		else if (dx.isByte()) {
			dr.set(dx.byteValue());
		}
		else {
			throw newNaftahBugUnsupportedNumbersError(true, dx);
		}
		return dr.normalize();
	}

	/**
	 * Returns the negation of the given number {@link Number}.
	 *
	 * @param x   the number to negate
	 * @param <T> concrete type that extends @{@link Number}
	 * @return the negated number
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
		DynamicNumber dr = dx.clone();
		if (dx.isBigDecimal()) {
			dr.set(dx.asBigDecimal().negate());
		}
		else if (dx.isDouble()) {
			dr.set(-dx.doubleValue());
		}
		else if (dx.isFloat()) {
			dr.set(-dx.floatValue());
		}
		else if (dx.isBigInteger()) {
			dr.set(dx.asBigInteger().negate());
		}
		else if (dx.isLong()) {
			dr.set(-dx.longValue());
		}
		else if (dx.isInt()) {
			dr.set(-dx.intValue());
		}
		else if (dx.isShort()) {
			dr.set(-dx.shortValue());
		}
		else if (dx.isByte()) {
			dr.set(-dx.byteValue());
		}
		else {
			throw newNaftahBugUnsupportedNumbersError(true, dx);
		}
		return dr.normalize();
	}

	/**
	 * Returns the square root of the given number {@link Number}.
	 *
	 * @param x   the number to compute the square root of
	 * @param <T> concrete type that extends @{@link Number}
	 * @return the square root of the number
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
		DynamicNumber dr = dx.clone();
		if (compare(dx, 0) < 0) {
			throw newNaftahNegativeNumberError();
		}
		else if (dx.isBigDecimal()) {
			dr.set(dx.asBigDecimal().sqrt(DECIMAL128));
		}
		else if (dx.isBigInteger()) {
			dr.set(dx.asBigInteger().sqrt());
		}
		else {
			dr.set(Math.sqrt(dx.doubleValue()));
		}
		return dr.normalize();
	}

	/**
	 * Returns the absolute value of the given number {@link Number}.
	 *
	 * @param x   the number to compute the absolute value of
	 * @param <T> concrete type that extends @{@link Number}
	 * @return the absolute value of the number
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
		DynamicNumber dr = dx.clone();
		if (dx.isDecimal()) {
			dr.set(dx.asBigDecimal().abs());
		}
		else if (dx.isBigInteger()) {
			dr.set(dx.asBigInteger().abs());
		}
		else {
			dr.set(Math.abs(dx.doubleValue()));
		}
		return dr.normalize();
	}

	/**
	 * Returns the signum of the given number {@link Number}.
	 *
	 * @param x   the number to compute the signum of
	 * @param <T> concrete type that extends @{@link Number}
	 * @return -1 if the number is negative, 0 if zero, and 1 if positive
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
		int signum;
		if (dx.isDecimal()) {
			signum = dx.asBigDecimal().signum();
		}
		else if (dx.isInteger()) {
			signum = dx.asBigInteger().signum();
		}
		else {
			signum = Double.compare(dx.doubleValue(), 0);
		}
		return signum;
	}

	/**
	 * Checks if the given number {@link Number} is zero.
	 *
	 * @param x   the number to check
	 * @param <T> concrete type that extends @{@link Number}
	 * @return {@code true} if the number is zero; {@code false} otherwise
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
	 * @param x   the first number
	 * @param y   the second number
	 * @param <T> concrete type that extends @{@link Number}
	 * @return {@code true} if {@code x} and {@code y} are equal in value;
	 *         {@code false} otherwise
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
	 * @return {@code true} if {@code x} and {@code y} are equal in value;
	 *         {@code false} otherwise
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
	 * @return {@code true} if {@code dx} and {@code dy} are equal in value;
	 *         {@code false} otherwise
	 */
	public static boolean equals(DynamicNumber dx, DynamicNumber dy) {
		if (dx.isNaN() || dy.isNaN()) {
			return false;
		}
		return compare(dx, dy) == 0;
	}

	/**
	 * Compares two numbers {@link Number}.
	 *
	 * @param x   the first number
	 * @param y   the second number
	 * @param <T> concrete type that extends @{@link Number}
	 * @return a negative integer if {@code x < y}; zero if {@code x == y}; a
	 *         positive integer if {@code x > y}
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
	 * @return a negative integer if {@code x < y}; zero if {@code x == y}; a
	 *         positive integer if {@code x > y}
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
	 * @return a negative integer if {@code dx < dy}; zero if {@code dx == dy}; a
	 *         positive integer if {@code dx > dy}
	 */
	public static int compare(DynamicNumber dx, DynamicNumber dy) {
		// Handle NaN or Infinite values first
		if (dx.isNaN()) {
			if (dy.isNaN()) {
				return 0; // Both NaN: consider equal or define your own logic
			}
			return -1; // NaN is considered less than any number, or define as needed
		}
		else if (dy.isNaN()) {
			return 1;  // dx is a number, dy is NaN
		}
		else if (dx.isInfinite()) {
			if (dy.isInfinite()) {
				// Compare sign or treat equal
				return 0; // or define logic if you have positive/negative infinity
			}
			return 1; // Infinite considered greater than any finite number
		}
		else if (dy.isInfinite()) {
			return -1;
		}
		else {
			if (dx.isDecimal() || dy.isDecimal()) {
				return dx.asBigDecimal().compareTo(dy.asBigDecimal());
			}
			else if (dx.isInteger() || dy.isInteger()) {
				return dx.asBigInteger().compareTo(dy.asBigInteger());
			}
			else {
				throw newNaftahBugUnsupportedNumbersError(false, dx, dy);
			}
		}
	}

	/**
	 * Performs a bitwise AND operation between two {@link Number} values of the same type.
	 * <p>
	 * This method wraps both input numbers in {@link DynamicNumber}, then delegates
	 * the operation to {@link #and(DynamicNumber, DynamicNumber)}. It supports all types
	 * that can be safely converted into {@code DynamicNumber} and that allow bitwise operations.
	 * </p>
	 *
	 * @param <T> the type of the input numbers, which must extend {@link Number}.
	 * @param x   the first operand.
	 * @param y   the second operand.
	 * @return the result of the bitwise AND operation as a {@link Number}.
	 * @throws NaftahBugError if the underlying number types do not support bitwise operations.
	 * @see DynamicNumber#of(Number)
	 * @see #and(DynamicNumber, DynamicNumber)
	 */
	public static <T extends Number> Number and(T x, T y) {
		DynamicNumber dx = DynamicNumber.of(x);
		DynamicNumber dy = DynamicNumber.of(y);
		return and(dx, dy);
	}

	/**
	 * Performs a bitwise AND operation between two dynamically-typed numeric values.
	 * <p>
	 * This method accepts arbitrary {@code Object} inputs, attempts to wrap them as {@link DynamicNumber}
	 * instances, and delegates the bitwise operation to {@link #and(DynamicNumber, DynamicNumber)}.
	 * It is useful in dynamic or loosely-typed contexts where inputs may be {@link Number}, {@link String}, or other
	 * types.
	 * </p>
	 *
	 * @param x the first operand; expected to be convertible to a {@link DynamicNumber}.
	 * @param y the second operand; expected to be convertible to a {@link DynamicNumber}.
	 * @return the result of the bitwise AND operation as a {@link Number}.
	 * @throws NaftahBugError if either input is not a valid number or if bitwise operations are not supported on the
	 *                        types.
	 * @see DynamicNumber#of(Object)
	 * @see #and(DynamicNumber, DynamicNumber)
	 */
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
		DynamicNumber dr = dx.clone();
		if (dx.isDecimal() || dy.isDecimal()) {
			throw newNaftahBugUnsupportedBitwiseDecimalError(false, dx, dy);
		}
		else if (dx.isBigInteger()) {
			dr.set(dx.asBigInteger().and(dy.asBigInteger()));
		}
		else if (dx.isLong()) {
			dr.set(dx.longValue() & dy.longValue());
		}
		else if (dx.isInt()) {
			dr.set(dx.intValue() & dy.intValue());
		}
		else if (dx.isShort()) {
			dr.set(dx.shortValue() & dy.shortValue());
		}
		else if (dx.isByte()) {
			dr.set(dx.byteValue() & dy.byteValue());
		}
		else {
			throw newNaftahBugUnsupportedNumbersError(true, dx);
		}
		return dr.normalize();
	}

	/**
	 * Performs a bitwise OR operation between two {@link Number} values of the same type.
	 * <p>
	 * Both operands are converted to {@link DynamicNumber}, and the operation is delegated
	 * to {@link #or(DynamicNumber, DynamicNumber)}. This method is useful when working with strongly typed
	 * numeric values that support bitwise logic (e.g., integers).
	 * </p>
	 *
	 * @param <T> the type of the input numbers, extending {@link Number}.
	 * @param x   the first operand.
	 * @param y   the second operand.
	 * @return the result of the bitwise OR operation as a {@link Number}.
	 * @throws NaftahBugError if bitwise operations are not supported on the operand types.
	 * @see DynamicNumber#of(Number)
	 * @see #or(DynamicNumber, DynamicNumber)
	 */
	public static <T extends Number> Number or(T x, T y) {
		DynamicNumber dx = DynamicNumber.of(x);
		DynamicNumber dy = DynamicNumber.of(y);
		return or(dx, dy);
	}

	/**
	 * Performs a bitwise OR operation between two dynamically-typed numeric values.
	 * <p>
	 * This method accepts arbitrary {@code Object} inputs, converts them to {@link DynamicNumber},
	 * and delegates the operation to {@link #or(DynamicNumber, DynamicNumber)}.
	 * It is designed for dynamic contexts where the input types may vary at runtime.
	 * </p>
	 *
	 * @param x the first operand; must be convertible to a {@link DynamicNumber}.
	 * @param y the second operand; must be convertible to a {@link DynamicNumber}.
	 * @return the result of the bitwise OR operation as a {@link Number}.
	 * @throws NaftahBugError if either operand is not a valid numeric value or bitwise operations are not supported.
	 * @see DynamicNumber#of(Object)
	 * @see #or(DynamicNumber, DynamicNumber)
	 */
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
		DynamicNumber dr = dx.clone();
		if (dx.isDecimal() || dy.isDecimal()) {
			throw newNaftahBugUnsupportedBitwiseDecimalError(false, dx, dy);
		}
		else if (dx.isBigInteger()) {
			dr.set(dx.asBigInteger().or(dy.asBigInteger()));
		}
		else if (dx.isLong()) {
			dr.set(dx.longValue() | dy.longValue());
		}
		else if (dx.isInt()) {
			dr.set(dx.intValue() | dy.intValue());
		}
		else if (dx.isShort()) {
			dr.set(dx.shortValue() | dy.shortValue());
		}
		else if (dx.isByte()) {
			dr.set(dx.byteValue() | dy.byteValue());
		}
		else {
			throw newNaftahBugUnsupportedNumbersError(true, dx);
		}
		return dr.normalize();
	}

	/**
	 * Performs a bitwise XOR (exclusive OR) operation between two {@link Number} values of the same type.
	 * <p>
	 * Both operands are wrapped as {@link DynamicNumber} instances, and the operation is delegated to
	 * {@link #xor(DynamicNumber, DynamicNumber)}. This method supports bitwise XOR on numeric types
	 * that allow such operations (typically integers).
	 * </p>
	 *
	 * @param <T> the type of the input numbers, extending {@link Number}.
	 * @param x   the first operand.
	 * @param y   the second operand.
	 * @return the result of the bitwise XOR operation as a {@link Number}.
	 * @throws NaftahBugError if the operand types do not support bitwise operations.
	 * @see DynamicNumber#of(Number)
	 * @see #xor(DynamicNumber, DynamicNumber)
	 */
	public static <T extends Number> Number xor(T x, T y) {
		DynamicNumber dx = DynamicNumber.of(x);
		DynamicNumber dy = DynamicNumber.of(y);
		return xor(dx, dy);
	}

	/**
	 * Performs a bitwise XOR (exclusive OR) operation between two dynamically-typed numeric values.
	 * <p>
	 * This method accepts any {@code Object} inputs, converts them to {@link DynamicNumber} instances,
	 * and delegates the operation to {@link #xor(DynamicNumber, DynamicNumber)}.
	 * It is suitable for dynamic contexts where inputs may vary in type (e.g., {@link Number}, {@link String}, etc.).
	 * </p>
	 *
	 * @param x the first operand; must be convertible to a {@link DynamicNumber}.
	 * @param y the second operand; must be convertible to a {@link DynamicNumber}.
	 * @return the result of the bitwise XOR operation as a {@link Number}.
	 * @throws NaftahBugError if either input is not a valid numeric type or does not support bitwise operations.
	 * @see DynamicNumber#of(Object)
	 * @see #xor(DynamicNumber, DynamicNumber)
	 */
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
		DynamicNumber dr = dx.clone();
		if (dx.isDecimal() || dy.isDecimal()) {
			throw newNaftahBugUnsupportedBitwiseDecimalError(false, dx, dy);
		}
		else if (dx.isBigInteger()) {
			dr.set(dx.asBigInteger().xor(dy.asBigInteger()));
		}
		else if (dx.isLong()) {
			dr.set(dx.longValue() ^ dy.longValue());
		}
		else if (dx.isInt()) {
			dr.set(dx.intValue() ^ dy.intValue());
		}
		else if (dx.isShort()) {
			dr.set(dx.shortValue() ^ dy.shortValue());
		}
		else if (dx.isByte()) {
			dr.set(dx.byteValue() ^ dy.byteValue());
		}
		else {
			throw newNaftahBugUnsupportedNumbersError(true, dx);
		}
		return dr.normalize();
	}

	/**
	 * Performs a bitwise NOT (inversion) operation on a numeric value.
	 * <p>
	 * The input number is wrapped as a {@link DynamicNumber}, and the operation is delegated to
	 * {@link #not(DynamicNumber)}. This method supports numeric types that allow bitwise operations
	 * (typically integers).
	 * </p>
	 *
	 * @param <T> the type of the input number, extending {@link Number}.
	 * @param x   the number to invert.
	 * @return the result of the bitwise NOT operation as a {@link Number}.
	 * @throws NaftahBugError if the number type does not support bitwise operations.
	 * @see DynamicNumber#of(Number)
	 * @see #not(DynamicNumber)
	 */
	public static <T extends Number> Number not(T x) {
		DynamicNumber dx = DynamicNumber.of(x);
		return not(dx);
	}

	/**
	 * Performs a bitwise NOT (inversion) operation on a dynamically-typed numeric value.
	 * <p>
	 * This method accepts any {@code Object} that can be converted to a {@link DynamicNumber},
	 * and delegates the operation to {@link #not(DynamicNumber)}. It is intended for use in
	 * dynamic contexts where the input type may vary.
	 * </p>
	 *
	 * @param x the value to invert; must be convertible to a {@link DynamicNumber}.
	 * @return the result of the bitwise NOT operation as a {@link Number}.
	 * @throws NaftahBugError if the input is not a valid numeric value or does not support bitwise operations.
	 * @see DynamicNumber#of(Object)
	 * @see #not(DynamicNumber)
	 */
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
		DynamicNumber dr = dx.clone();
		if (dx.isDecimal()) {
			throw newNaftahBugUnsupportedBitwiseDecimalError(true, dx);
		}
		else if (dx.isBigInteger()) {
			dr.set(dx.asBigInteger().not());
		}
		else if (dx.isLong()) {
			dr.set(~dx.longValue());
		}
		else if (dx.isInt()) {
			dr.set(~dx.intValue());
		}
		else if (dx.isShort()) {
			dr.set(~dx.shortValue());
		}
		else if (dx.isByte()) {
			dr.set(~dx.byteValue());
		}
		else {
			throw newNaftahBugUnsupportedNumbersError(true, dx);
		}
		return dr.normalize();
	}

	/**
	 * Performs a left bitwise shift on a numeric value by the specified number of positions.
	 * <p>
	 * The input number is wrapped as a {@link DynamicNumber}, and the shift operation is
	 * delegated to {@link #shiftLeft(DynamicNumber, int)}. This method is suitable for statically
	 * typed {@link Number} values that support bitwise operations (typically integer types).
	 * </p>
	 *
	 * @param <T>       the type of the input number, extending {@link Number}.
	 * @param x         the number to shift.
	 * @param positions the number of bit positions to shift to the left; must be non-negative.
	 * @return the result of the left shift operation as a {@link Number}.
	 * @throws NaftahBugError           if the number type does not support bitwise operations.
	 * @throws IllegalArgumentException if the shift amount is negative or exceeds allowed bounds.
	 * @see DynamicNumber#of(Number)
	 * @see #shiftLeft(DynamicNumber, int)
	 */
	public static <T extends Number> Number shiftLeft(T x, T positions) {
		DynamicNumber dx = DynamicNumber.of(x);
		return shiftLeft(dx, positions.intValue());
	}

	/**
	 * Performs a left bitwise shift on a dynamically-typed numeric value by the specified number of positions.
	 * <p>
	 * This method accepts any {@code Object} that can be converted to a {@link DynamicNumber},
	 * and delegates the shift operation to {@link #shiftLeft(DynamicNumber, int)}.
	 * It is designed for use in dynamic or loosely typed contexts.
	 * </p>
	 *
	 * @param x         the value to shift; must be convertible to a {@link DynamicNumber}.
	 * @param positions the number of bit positions to shift to the left; must be non-negative.
	 * @return the result of the left shift operation as a {@link Number}.
	 * @throws NaftahBugError           if the input is not a valid numeric value or does not support bitwise
	 *                                  operations.
	 * @throws IllegalArgumentException if the shift amount is negative or exceeds allowed bounds.
	 * @see DynamicNumber#of(Object)
	 * @see #shiftLeft(DynamicNumber, int)
	 */
	public static Number shiftLeft(Object x, int positions) {
		DynamicNumber dx = DynamicNumber.of(x);
		return shiftLeft(dx, positions);
	}

	/**
	 * Shifts the given number {@link DynamicNumber} to the left (signed shift).
	 *
	 * <p>
	 * This is equivalent to multiplying the value by {@code 2^n}.
	 *
	 * @param dx        the number to shift
	 * @param positions the number of bits to shift to the left; must be non-negative
	 * @return the result of {@code value << n}
	 */
	public static Number shiftLeft(DynamicNumber dx, int positions) {
		DynamicNumber dr = dx.clone();
		if (dx.isDecimal()) {
			throw newNaftahBugUnsupportedBitwiseDecimalError(true, dx);
		}
		else if (!isZero(dx)) {
			if (dx.isBigInteger()) {
				dr.set(shiftLeft(dx.asBigInteger(), positions));
			}
			else if (dx.isLong()) {
				try {
					checkLeftShiftOverflow(Long.SIZE, Long.MAX_VALUE, Long.MIN_VALUE, dx.asBigInteger(), positions);
					dr.set(dx.longValue() << positions);
				}
				catch (ArithmeticException ignored) {
					dr.set(shiftLeft(dx.asBigInteger(), positions));
				}
			}
			else if (dx.isInt()) {
				try {
					checkLeftShiftOverflow( Integer.SIZE,
											Integer.MAX_VALUE,
											Integer.MIN_VALUE,
											dx.asBigInteger(),
											positions);
					dr.set(dx.intValue() << positions);
				}
				catch (ArithmeticException ignored) {
					dr.set(shiftLeft(dx.asBigInteger(), positions));
				}
			}
			else if (dx.isShort()) {
				try {
					checkLeftShiftOverflow( Short.SIZE,
											Short.MAX_VALUE,
											Short.MIN_VALUE,
											dx.asBigInteger(),
											positions);
					dr.set(dx.shortValue() << positions);
				}
				catch (ArithmeticException ignored) {
					dr.set(shiftLeft(dx.asBigInteger(), positions));
				}
			}
			else if (dx.isByte()) {
				try {
					checkLeftShiftOverflow(Byte.SIZE, Byte.MAX_VALUE, Byte.MIN_VALUE, dx.asBigInteger(), positions);
					dr.set(dx.byteValue() << positions);
				}
				catch (ArithmeticException ignored) {
					dr.set(shiftLeft(dx.asBigInteger(), positions));
				}
			}
			else {
				throw newNaftahBugUnsupportedNumbersError(true, dx);
			}
		}
		return dr.normalize();
	}

	/**
	 * Performs a left bitwise shift on a {@link BigInteger} value by the specified number of positions,
	 * with overflow checking against 64-bit {@code long} bounds.
	 * <p>
	 * This method ensures that the shift will not produce a value that exceeds the range of a 64-bit
	 * signed integer, and throws an exception if overflow is detected.
	 * </p>
	 *
	 * @param bigInteger the {@link BigInteger} value to shift.
	 * @param positions  the number of bit positions to shift to the left; must be non-negative.
	 * @return a new {@link BigInteger} representing the result of the shift.
	 * @throws NaftahBugError      if the shift amount is invalid (e.g., negative or too large).
	 * @throws ArithmeticException if the shift would cause overflow beyond {@code long} range.
	 * @see #checkLeftShiftOverflow(BigInteger, int)
	 * @see java.math.BigInteger#shiftLeft(int)
	 */
	public static BigInteger shiftLeft(BigInteger bigInteger, int positions) {
		checkLeftShiftOverflow(bigInteger, positions);
		return bigInteger.shiftLeft(positions);
	}

	/**
	 * Performs a right bitwise shift on a numeric value by the specified number of positions.
	 * <p>
	 * The input value is converted to a {@link DynamicNumber}, and the operation is delegated
	 * to {@link #shiftRight(DynamicNumber, int)}. This operation is safe and does not cause overflow.
	 * </p>
	 *
	 * @param <T>       the type of the input number, extending {@link Number}.
	 * @param x         the number to shift.
	 * @param positions the number of bit positions to shift to the right; must be non-negative.
	 * @return the result of the right shift operation as a {@link Number}.
	 * @throws NaftahBugError           if the number type does not support bitwise operations.
	 * @throws IllegalArgumentException if the shift amount is negative.
	 * @see DynamicNumber#of(Number)
	 * @see #shiftRight(DynamicNumber, int)
	 */
	public static <T extends Number> Number shiftRight(T x, T positions) {
		DynamicNumber dx = DynamicNumber.of(x);
		return shiftRight(dx, positions.intValue());
	}

	/**
	 * Performs a right bitwise shift on a dynamically-typed numeric value by the specified number of positions.
	 * <p>
	 * This method accepts any {@code Object} that can be converted to a {@link DynamicNumber},
	 * and delegates the operation to {@link #shiftRight(DynamicNumber, int)}.
	 * It is designed for use in dynamic contexts where the input type may vary at runtime.
	 * </p>
	 *
	 * @param x         the value to shift; must be convertible to a {@link DynamicNumber}.
	 * @param positions the number of bit positions to shift to the right; must be non-negative.
	 * @return the result of the right shift operation as a {@link Number}.
	 * @throws NaftahBugError           if the input is not a valid numeric value or does not support bitwise
	 *                                  operations.
	 * @throws IllegalArgumentException if the shift amount is negative.
	 * @see DynamicNumber#of(Object)
	 * @see #shiftRight(DynamicNumber, int)
	 */
	public static Number shiftRight(Object x, int positions) {
		DynamicNumber dx = DynamicNumber.of(x);
		return shiftRight(dx, positions);
	}

	/**
	 * Shifts the given number {@link DynamicNumber} to the right (signed shift).
	 *
	 * <p>
	 * This is equivalent to dividing the value by {@code 2^n}, with sign extension.
	 *
	 * @param dx        the number to shift
	 * @param positions the number of bits to shift to the right; must be non-negative
	 * @return the result of {@code value >> n}
	 */
	public static Number shiftRight(DynamicNumber dx, int positions) {
		DynamicNumber dr = dx.clone();
		if (dx.isDecimal()) {
			throw newNaftahBugUnsupportedBitwiseDecimalError(true, dx);
		}
		else if (!isZero(dx)) {
			if (dx.isBigInteger()) {
				var bigInteger = dx.asBigInteger();
				checkShiftPositions(bigInteger.bitLength(), positions);
				dr.set(bigInteger.shiftRight(positions));
			}
			else if (dx.isLong()) {
				checkShiftPositions(Long.SIZE, positions);
				dr.set(dx.longValue() >> positions);
			}
			else if (dx.isInt()) {
				checkShiftPositions(Integer.SIZE, positions);
				dr.set(dx.intValue() >> positions);
			}
			else if (dx.isShort()) {
				checkShiftPositions(Short.SIZE, positions);
				dr.set(dx.shortValue() >> positions);
			}
			else if (dx.isByte()) {
				checkShiftPositions(Byte.SIZE, positions);
				dr.set(dx.byteValue() >> positions);
			}
			else {
				throw newNaftahBugUnsupportedNumbersError(true, dx);
			}
		}
		return dr.normalize();
	}

	/**
	 * Performs an unsigned (logical) right bitwise shift on a numeric value by the specified number of positions.
	 * <p>
	 * The input number is converted to a {@link DynamicNumber}, and the operation is delegated to
	 * {@link #unsignedShiftRight(DynamicNumber, int)}. This shift fills the left bits with zeros regardless of sign.
	 * </p>
	 *
	 * @param <T>       the type of the input number, extending {@link Number}.
	 * @param x         the number to shift.
	 * @param positions the number of bit positions to shift to the right; must be non-negative.
	 * @return the result of the unsigned right shift operation as a {@link Number}.
	 * @throws NaftahBugError           if the number type does not support bitwise operations.
	 * @throws IllegalArgumentException if the shift amount is negative.
	 * @see DynamicNumber#of(Number)
	 * @see #unsignedShiftRight(DynamicNumber, int)
	 */
	public static <T extends Number> Number unsignedShiftRight(T x, T positions) {
		DynamicNumber dx = DynamicNumber.of(x);
		return unsignedShiftRight(dx, positions.intValue());
	}

	/**
	 * Performs an unsigned (logical) right bitwise shift on a dynamically-typed numeric value by the specified number
	 * of positions.
	 * <p>
	 * This method accepts any {@code Object} convertible to {@link DynamicNumber},
	 * and delegates the operation to {@link #unsignedShiftRight(DynamicNumber, int)}.
	 * </p>
	 *
	 * @param x         the value to shift; must be convertible to a {@link DynamicNumber}.
	 * @param positions the number of bit positions to shift to the right; must be non-negative.
	 * @return the result of the unsigned right shift operation as a {@link Number}.
	 * @throws NaftahBugError           if the input is not a valid numeric value or does not support bitwise
	 *                                  operations.
	 * @throws IllegalArgumentException if the shift amount is negative.
	 * @see DynamicNumber#of(Object)
	 * @see #unsignedShiftRight(DynamicNumber, int)
	 */
	public static Number unsignedShiftRight(Object x, int positions) {
		DynamicNumber dx = DynamicNumber.of(x);
		return unsignedShiftRight(dx, positions);
	}

	/**
	 * Shifts the given number {@link DynamicNumber} to the right (unsigned shift).
	 *
	 * <p>
	 * This is equivalent to dividing the value by {@code 2^n}, with sign extension.
	 *
	 * @param dx        the number to shift
	 * @param positions the number of bits to shift to the right; must be non-negative
	 * @return the result of an unsigned right shift (zero-fill) of {@code value} by
	 *         {@code n} bits
	 */
	public static Number unsignedShiftRight(DynamicNumber dx, int positions) {
		DynamicNumber dr = dx.clone();
		if (dx.isDecimal()) {
			throw newNaftahBugUnsupportedBitwiseDecimalError(true, dx);
		}
		else if (!isZero(dx)) {
			if (dx.isBigInteger()) {
				var bigInteger = dx.asBigInteger();
				checkShiftPositions(bigInteger.bitLength(), positions);
				dr.set(unsignedShiftRight(bigInteger, positions));
			}
			else if (dx.isLong()) {
				checkShiftPositions(Long.SIZE, positions);
				dr.set(dx.longValue() >>> positions);
			}
			else if (dx.isInt()) {
				checkShiftPositions(Integer.SIZE, positions);
				dr.set(dx.intValue() >>> positions);
			}
			else if (dx.isShort()) {
				checkShiftPositions(Short.SIZE, positions);
				dr.set(dx.shortValue() >>> positions);
			}
			else if (dx.isByte()) {
				checkShiftPositions(Byte.SIZE, positions);
				dr.set(dx.byteValue() >>> positions);
			}
			else {
				throw newNaftahBugUnsupportedNumbersError(true, dx);
			}
		}
		return dr.normalize();
	}

	/**
	 * Performs a pre-increment operation on a numeric value.
	 * <p>
	 * The input number is wrapped as a {@link DynamicNumber}, and the increment operation
	 * is delegated to {@link #preIncrement(DynamicNumber)}.
	 * </p>
	 *
	 * @param <T> the type of the input number, extending {@link Number}.
	 * @param x   the number to increment.
	 * @return the result of the pre-increment operation as a {@link Number}.
	 * @throws NaftahBugError if the input type is unsupported for arithmetic operations.
	 * @see DynamicNumber#of(Number)
	 * @see #preIncrement(DynamicNumber)
	 */
	public static <T extends Number> Number preIncrement(T x) {
		DynamicNumber dx = DynamicNumber.of(x);
		return preIncrement(dx);
	}

	/**
	 * Performs a pre-increment operation on a dynamically-typed numeric value.
	 * <p>
	 * This method accepts any {@code Object} convertible to a {@link DynamicNumber}
	 * and delegates the increment operation to {@link #preIncrement(DynamicNumber)}.
	 * </p>
	 *
	 * @param x the value to increment; must be convertible to {@link DynamicNumber}.
	 * @return the result of the pre-increment operation as a {@link Number}.
	 * @throws NaftahBugError if the input is not a valid numeric value or unsupported for arithmetic.
	 * @see DynamicNumber#of(Object)
	 * @see #preIncrement(DynamicNumber)
	 */
	public static Number preIncrement(Object x) {
		DynamicNumber dx = DynamicNumber.of(x);
		return preIncrement(dx);
	}

	/**
	 * Performs a pre-increment operation on the given {@link DynamicNumber} instance.
	 * <p>
	 * This method handles numeric overflow by promoting the underlying number type to
	 * a larger or more precise numeric type when the maximum value is reached:
	 * <ul>
	 * <li>Byte and Short promote to Integer.</li>
	 * <li>Integer promotes to Long.</li>
	 * <li>Long promotes to BigInteger.</li>
	 * <li>Float promotes to Double.</li>
	 * <li>Double promotes to BigDecimal.</li>
	 * </ul>
	 * <p>
	 * For {@link BigInteger} and {@link BigDecimal}, it simply adds one without promotion.
	 * If no overflow occurs, it increments normally.
	 * </p>
	 *
	 * @param dx the {@link DynamicNumber} to increment.
	 * @return the incremented number as a {@link Number}.
	 * @throws NaftahBugError if the underlying numeric type is not supported.
	 */
	public static Number preIncrement(DynamicNumber dx) {
		if (dx.isByte() && dx.byteValue() == Byte.MAX_VALUE) {
			dx.promote().set(dx.promote().shortValue() + 1);
		}
		else if (dx.isShort() && dx.shortValue() == Short.MAX_VALUE) {
			dx.promote().set(dx.promote().intValue() + 1);
		}
		else if (dx.isInt() && dx.intValue() == Integer.MAX_VALUE) {
			dx.promote().set(dx.promote().longValue() + 1L);
		}
		else if (dx.isLong() && dx.longValue() == Long.MAX_VALUE) {
			dx.promote().set(((BigInteger) dx.promote().get()).add(BigInteger.ONE));
		}
		else if (dx.isFloat() && (dx.floatValue() == Float.MAX_VALUE || dx.isNaN() || dx.isInfinite())) {
			dx.promote().set(dx.promote().doubleValue() + 1d);
		}
		else if (dx.isDouble() && (dx.doubleValue() == Double.MAX_VALUE || dx.isNaN() || dx.isInfinite())) {
			dx.promote().set(((BigDecimal) dx.promote().get()).add(BigDecimal.ONE));
		}
		else if (dx.isBigInteger()) {
			dx.set(dx.asBigInteger().add(BigInteger.ONE));
		}
		else if (dx.isBigDecimal()) {
			dx.set(dx.asBigDecimal().add(BigDecimal.ONE));
		}
		else if (dx.isByte()) {
			dx.set(dx.byteValue() + 1);
		}
		else if (dx.isShort()) {
			dx.set(dx.shortValue() + 1);
		}
		else if (dx.isInt()) {
			dx.set(dx.intValue() + 1);
		}
		else if (dx.isLong()) {
			dx.set(dx.longValue() + 1L);
		}
		else if (dx.isFloat()) {
			dx.set(dx.floatValue() + 1f);
		}
		else if (dx.isDouble()) {
			dx.set(dx.doubleValue() + 1d);
		}
		else {
			throw newNaftahBugUnsupportedNumbersError(true, dx);
		}
		return dx.normalize();
	}

	/**
	 * Performs a post-increment operation on a numeric value.
	 * <p>
	 * The input number is wrapped as a {@link DynamicNumber}, and the increment operation
	 * is delegated to {@link #postIncrement(DynamicNumber)}.
	 * The returned value is the original value before the increment.
	 * </p>
	 *
	 * @param <T> the type of the input number, extending {@link Number}.
	 * @param x   the number to increment.
	 * @return the original value before increment as a {@link Number}.
	 * @throws NaftahBugError if the input type is unsupported for arithmetic operations.
	 * @see DynamicNumber#of(Number)
	 * @see #postIncrement(DynamicNumber)
	 */
	public static <T extends Number> Number postIncrement(T x) {
		DynamicNumber dx = DynamicNumber.of(x);
		return postIncrement(dx);
	}

	/**
	 * Performs a post-increment operation on a dynamically-typed numeric value.
	 * <p>
	 * This method accepts any {@code Object} convertible to a {@link DynamicNumber}
	 * and delegates the increment operation to {@link #postIncrement(DynamicNumber)}.
	 * The returned value is the original value before the increment.
	 * </p>
	 *
	 * @param x the value to increment; must be convertible to {@link DynamicNumber}.
	 * @return the original value before increment as a {@link Number}.
	 * @throws NaftahBugError if the input is not a valid numeric value or unsupported for arithmetic.
	 * @see DynamicNumber#of(Object)
	 * @see #postIncrement(DynamicNumber)
	 */
	public static Number postIncrement(Object x) {
		DynamicNumber dx = DynamicNumber.of(x);
		return postIncrement(dx);
	}

	/**
	 * Performs a post-increment operation on the given {@link DynamicNumber}.
	 * <p>
	 * The method returns the original value before incrementing.
	 * It handles overflow by promoting the underlying numeric type to a larger or more precise type when needed:
	 * <ul>
	 * <li>Byte and Short promote to Integer.</li>
	 * <li>Integer promotes to Long.</li>
	 * <li>Long promotes to {@link BigInteger}.</li>
	 * <li>Float promotes to Double.</li>
	 * <li>Double promotes to {@link BigDecimal}.</li>
	 * </ul>
	 * <p>
	 * For {@link BigInteger} and {@link BigDecimal}, it increments without promotion.
	 * If the type is unsupported, it throws a {@link NaftahBugError}.
	 * </p>
	 *
	 * @param dx the {@link DynamicNumber} to post-increment.
	 * @return the original value before the increment as a {@link Number}.
	 * @throws NaftahBugError if the number type is unsupported.
	 */
	public static Number postIncrement(DynamicNumber dx) {
		DynamicNumber current = dx.clone();

		if (dx.isByte()) {
			if (current.byteValue() == Byte.MAX_VALUE) {
				dx.set(dx.promote().shortValue() + 1);
			}
			else {
				dx.set(dx.byteValue() + 1);
			}
		}
		else if (dx.isShort()) {
			if (current.shortValue() == Short.MAX_VALUE) {
				dx.set(dx.promote().intValue() + 1);
			}
			else {
				dx.set(dx.shortValue() + 1);
			}
		}
		else if (dx.isInt()) {
			if (current.intValue() == Integer.MAX_VALUE) {
				dx.set(dx.promote().longValue() + 1L);
			}
			else {
				dx.set(dx.intValue() + 1);
			}
		}
		else if (dx.isLong()) {
			if (current.longValue() == Long.MAX_VALUE) {
				dx.set(((BigInteger) dx.promote().get()).add(BigInteger.ONE));
			}
			else {
				dx.set(dx.longValue() + 1L);
			}
		}
		else if (dx.isFloat()) {
			if (current.floatValue() == Float.MAX_VALUE || current.isNaN() || current.isInfinite()) {
				dx.set(dx.promote().doubleValue() + 1d);
			}
			else {
				dx.set(dx.floatValue() + 1f);
			}
		}
		else if (dx.isDouble()) {
			if (current.doubleValue() == Double.MAX_VALUE || current.isNaN() || current.isInfinite()) {
				dx.set(((BigDecimal) dx.promote().get()).add(BigDecimal.ONE));
			}
			else {
				dx.set(dx.doubleValue() + 1d);
			}
		}
		else if (dx.isBigInteger()) {
			dx.set(dx.asBigInteger().add(BigInteger.ONE));
		}
		else if (dx.isBigDecimal()) {
			dx.set(dx.asBigDecimal().add(BigDecimal.ONE));
		}
		else {
			throw newNaftahBugUnsupportedNumbersError(true, dx);
		}
		return current.normalize();
	}

	/**
	 * Performs a pre-decrement operation on a numeric value.
	 * <p>
	 * The input number is wrapped as a {@link DynamicNumber}, and the decrement operation
	 * is delegated to {@link #preDecrement(DynamicNumber)}.
	 * </p>
	 *
	 * @param <T> the type of the input number, extending {@link Number}.
	 * @param x   the number to decrement.
	 * @return the result of the pre-decrement operation as a {@link Number}.
	 * @throws NaftahBugError if the input type is unsupported for arithmetic operations.
	 * @see DynamicNumber#of(Number)
	 * @see #preDecrement(DynamicNumber)
	 */
	public static <T extends Number> Number preDecrement(T x) {
		DynamicNumber dx = DynamicNumber.of(x);
		return preDecrement(dx);
	}

	/**
	 * Performs a pre-decrement operation on a dynamically-typed numeric value.
	 * <p>
	 * This method accepts any {@code Object} convertible to a {@link DynamicNumber}
	 * and delegates the decrement operation to {@link #preDecrement(DynamicNumber)}.
	 * </p>
	 *
	 * @param x the value to decrement; must be convertible to {@link DynamicNumber}.
	 * @return the result of the pre-decrement operation as a {@link Number}.
	 * @throws NaftahBugError if the input is not a valid numeric value or unsupported for arithmetic.
	 * @see DynamicNumber#of(Object)
	 * @see #preDecrement(DynamicNumber)
	 */
	public static Number preDecrement(Object x) {
		DynamicNumber dx = DynamicNumber.of(x);
		return preDecrement(dx);
	}

	/**
	 * Performs a pre-decrement operation on the given {@link DynamicNumber}.
	 * <p>
	 * This method handles underflow by promoting the underlying number type to a larger or more precise numeric type
	 * when
	 * the minimum value is reached:
	 * <ul>
	 * <li>Byte and Short promote to Integer.</li>
	 * <li>Integer promotes to Long.</li>
	 * <li>Long promotes to {@link BigInteger}.</li>
	 * <li>Float promotes to Double.</li>
	 * <li>Double promotes to {@link BigDecimal}.</li>
	 * </ul>
	 * For {@link BigInteger} and {@link BigDecimal}, it decrements without promotion.
	 * If no underflow occurs, it decrements normally.
	 * </p>
	 *
	 * @param dx the {@link DynamicNumber} to decrement.
	 * @return the decremented number as a {@link Number}.
	 * @throws NaftahBugError if the underlying numeric type is not supported.
	 */
	public static Number preDecrement(DynamicNumber dx) {
		if (dx.isBigDecimal()) {
			dx.set(dx.asBigDecimal().subtract(BigDecimal.ONE));
		}
		else if (dx.isBigInteger()) {
			dx.set(dx.asBigInteger().subtract(BigInteger.ONE));
		}
		else if (dx.isDouble()) {
			dx.set(dx.doubleValue() - 1);
		}
		else if (dx.isFloat()) {
			dx.set(dx.floatValue() - 1);
		}
		else if (dx.isLong()) {
			dx.set(dx.longValue() - 1);
		}
		else if (dx.isInt()) {
			dx.set(dx.intValue() - 1);
		}
		else if (dx.isShort()) {
			dx.set(dx.shortValue() - 1);
		}
		else if (dx.isByte()) {
			dx.set(dx.byteValue() - 1);
		}
		else {
			throw newNaftahBugUnsupportedNumbersError(true, dx);
		}
		return dx.normalize();
	}

	/**
	 * Performs a post-decrement operation on a numeric value.
	 * <p>
	 * The input number is wrapped as a {@link DynamicNumber}, and the decrement operation
	 * is delegated to {@link #postDecrement(DynamicNumber)}.
	 * The returned value is the original value before the decrement.
	 * </p>
	 *
	 * @param <T> the type of the input number, extending {@link Number}.
	 * @param x   the number to decrement.
	 * @return the original value before decrement as a {@link Number}.
	 * @throws NaftahBugError if the input type is unsupported for arithmetic operations.
	 * @see DynamicNumber#of(Number)
	 * @see #postDecrement(DynamicNumber)
	 */
	public static <T extends Number> Number postDecrement(T x) {
		DynamicNumber dx = DynamicNumber.of(x);
		return postDecrement(dx);
	}

	/**
	 * Performs a post-decrement operation on a dynamically-typed numeric value.
	 * <p>
	 * This method accepts any {@code Object} convertible to a {@link DynamicNumber}
	 * and delegates the decrement operation to {@link #postDecrement(DynamicNumber)}.
	 * The returned value is the original value before the decrement.
	 * </p>
	 *
	 * @param x the value to decrement; must be convertible to {@link DynamicNumber}.
	 * @return the original value before decrement as a {@link Number}.
	 * @throws NaftahBugError if the input is not a valid numeric value or unsupported for arithmetic.
	 * @see DynamicNumber#of(Object)
	 * @see #postDecrement(DynamicNumber)
	 */
	public static Number postDecrement(Object x) {
		DynamicNumber dx = DynamicNumber.of(x);
		return postDecrement(dx);
	}

	/**
	 * Performs a post-decrement operation on the given {@link DynamicNumber}.
	 * <p>
	 * The method returns the original value before decrementing.
	 * It handles underflow by promoting the underlying numeric type to a larger or more precise type when needed:
	 * <ul>
	 * <li>Byte and Short promote to Integer.</li>
	 * <li>Integer promotes to Long.</li>
	 * <li>Long promotes to {@link BigInteger}.</li>
	 * <li>Float promotes to Double.</li>
	 * <li>Double promotes to {@link BigDecimal}.</li>
	 * </ul>
	 * For {@link BigInteger} and {@link BigDecimal}, it decrements without promotion.
	 * If the type is unsupported, it throws a {@link NaftahBugError}.
	 * </p>
	 *
	 * @param dx the {@link DynamicNumber} to post-decrement.
	 * @return the original value before the decrement as a {@link Number}.
	 * @throws NaftahBugError if the number type is unsupported.
	 */
	public static Number postDecrement(DynamicNumber dx) {
		DynamicNumber current = dx.clone();

		if (dx.isBigDecimal()) {
			dx.set(dx.asBigDecimal().subtract(BigDecimal.ONE));
		}
		else if (dx.isBigInteger()) {
			dx.set(dx.asBigInteger().subtract(BigInteger.ONE));
		}
		else if (dx.isDouble()) {
			dx.set(dx.doubleValue() - 1);
		}
		else if (dx.isFloat()) {
			dx.set(dx.floatValue() - 1);
		}
		else if (dx.isLong()) {
			dx.set(dx.longValue() - 1);
		}
		else if (dx.isInt()) {
			dx.set(dx.intValue() - 1);
		}
		else if (dx.isShort()) {
			dx.set(dx.shortValue() - 1);
		}
		else if (dx.isByte()) {
			dx.set(dx.byteValue() - 1);
		}
		else {
			throw newNaftahBugUnsupportedNumbersError(true, dx);
		}

		// Normalize the updated value inside dx after decrement
		dx.normalize();

		// Return the old value, no normalization
		return current.normalize();
	}

	/**
	 * Performs an unsigned (logical) right shift on a {@link BigInteger}.
	 *
	 * <p>
	 * Unlike signed right shift, this operation fills high-order bits with zeros
	 * regardless of the sign of the {@code BigInteger}. Since {@code BigInteger}
	 * does not have a built-in unsigned right shift, this method emulates it for
	 * non-negative values. If the value is negative, the behavior may not match
	 * unsigned semantics in lower-level languages like Java's primitive types.
	 *
	 * @param value the {@code BigInteger} to shift; must not be {@code null}
	 * @param n     the number of bits to shift right; must be non-negative
	 * @return the result of shifting {@code value} to the right by {@code n} bits,
	 *         using zero-fill
	 * @throws NullPointerException     if {@code value} is {@code null}
	 * @throws IllegalArgumentException if {@code n} is negative
	 */
	public static BigInteger unsignedShiftRight(BigInteger value, int n) {
		if (value.signum() >= 0) {
			// If positive, normal shiftRight works as unsigned
			return value.shiftRight(n);
		}
		else {
			// For negative values:
			// 1. Add 2^(bitLength) to get the unsigned equivalent positive value
			int bitLength = value.bitLength();
			BigInteger twoPowerBitLength = BigInteger.ONE.shiftLeft(bitLength);
			BigInteger unsignedValue = value.add(twoPowerBitLength);

			// 2. Shift right logically
			return unsignedValue.shiftRight(n);
		}
	}

	/**
	 * Checks whether a double value retains full precision when parsed from the original string.
	 * Throws a NumberFormatException if precision is lost.
	 *
	 * @param text the original string representing the number
	 * @param d    the parsed double value
	 * @return the double value if precision is preserved
	 * @throws NumberFormatException if precision is lost during parsing
	 */
	public static Number checkPrecision(String text, double d) {
		// Precision check
		BigDecimal expected = new BigDecimal(text);
		BigDecimal actual = new BigDecimal(Double.toString(d));

		if (expected.compareTo(actual) != 0) {
			throw new NumberFormatException("فُقدت الدقة عند تحويل '%s' إلى Double. الناتج: %s"
					.formatted(text, actual.toPlainString()));
		}

		return d;
	}

	/**
	 * Checks whether a float value retains full precision when parsed from the original string.
	 * Throws a NumberFormatException if precision is lost.
	 *
	 * @param text the original string representing the number
	 * @param f    the parsed float value
	 * @return the float value if precision is preserved
	 * @throws NumberFormatException if precision is lost during parsing
	 */
	public static Number checkPrecision(String text, float f) {
		// Precision check
		BigDecimal expected = new BigDecimal(text);
		BigDecimal actual = new BigDecimal(Float.toString(f));

		if (expected.compareTo(actual) != 0) {
			throw new NumberFormatException("فُقدت الدقة عند تحويل '%s' إلى Float. الناتج: %s"
					.formatted(text, actual.toPlainString()));
		}
		return f;
	}

	/**
	 * Checks whether a double value matches the expected BigDecimal value.
	 * If not, returns the expected BigDecimal as a fallback.
	 *
	 * @param expected the expected BigDecimal value
	 * @param d        the double value to compare
	 * @return the original double if precise, otherwise the expected BigDecimal
	 */
	public static Number checkPrecision(BigDecimal expected, double d) {
		BigDecimal actual = new BigDecimal(Double.toString(d));

		if (expected.compareTo(actual) != 0) {
			return expected;
		}
		else {
			return d;
		}
	}

	/**
	 * Checks whether a float value matches the expected BigDecimal value.
	 * If not, returns the expected BigDecimal as a fallback.
	 *
	 * @param expected the expected BigDecimal value
	 * @param f        the float value to compare
	 * @return the original float if precise, otherwise the expected BigDecimal
	 */
	public static Number checkPrecision(BigDecimal expected, float f) {
		BigDecimal actual = new BigDecimal(Float.toString(f));

		if (expected.compareTo(actual) != 0) {
			return expected;
		}
		else {
			return f;
		}
	}

	/**
	 * Checks for overflow when left-shifting a BigInteger value by a given number of bits.
	 * <p>
	 * This method verifies that the shift amount is within valid bounds and that the value,
	 * when shifted, will not exceed the range of a 64-bit signed long. If an overflow condition
	 * is detected, an exception is thrown with a descriptive message.
	 * </p>
	 *
	 * @param size      the maximum allowed shift size (in bits). If set to -1, no upper bound check is performed.
	 * @param value     the {@link BigInteger} value to be shifted.
	 * @param positions the number of bits to shift left; must be non-negative and less than {@code size} if {@code
	 *                  size} is not -1.
	 * @throws NaftahBugError      if the shift amount is invalid (negative or out of range).
	 * @throws ArithmeticException if shifting the value would overflow the range of a {@code long}.
	 */
	public static void checkLeftShiftOverflow(int size, long max, long min, BigInteger value, int positions) {
		checkLeftShiftOverflow(size, max, min, value, positions, false);
	}

	/**
	 * Checks if left shift operation on the given {@link BigInteger} value by a number of positions
	 * would cause overflow according to the specified limits.
	 * <p>
	 * This method delegates to the general {@link #checkLeftShiftOverflow(int, long, long, BigInteger, int, boolean)}
	 * method with default size and limits (-1 means no limit).
	 * </p>
	 *
	 * @param value     the {@link BigInteger} value to be shifted left.
	 * @param positions the number of bit positions to shift left.
	 * @throws NaftahBugError if the shift positions are invalid or if the shifted value would overflow.
	 */
	public static void checkLeftShiftOverflow(BigInteger value, int positions) {
		checkLeftShiftOverflow(-1, -1, -1, value, positions, true);
	}

	/**
	 * Checks if left shift operation on the given {@link BigInteger} value by a number of positions
	 * would cause overflow or underflow based on the provided limits and size.
	 * <p>
	 * Throws {@link NaftahBugError} wrapping an {@link ArithmeticException} if overflow or underflow
	 * conditions are detected and wrap is true; otherwise throws the raw {@link ArithmeticException}.
	 * </p>
	 *
	 * @param size      the bit size of the type being shifted, or -1 if unknown/unbounded.
	 * @param max       the maximum allowed long value before shifting, or -1 if no maximum limit.
	 * @param min       the minimum allowed long value before shifting, or -1 if no minimum limit.
	 * @param value     the {@link BigInteger} value to be shifted.
	 * @param positions the number of bits to shift left.
	 * @param wrap      if true, wraps any {@link ArithmeticException} in a {@link NaftahBugError}.
	 * @throws NaftahBugError      if wrap is true and invalid shift or overflow/underflow detected.
	 * @throws ArithmeticException if wrap is false and invalid shift or overflow/underflow detected.
	 */
	public static void checkLeftShiftOverflow(  int size,
												long max,
												long min,
												BigInteger value,
												int positions,
												boolean wrap) {
		ArithmeticException exception = (ArithmeticException) checkShiftPositions(size, positions, false);

		if (Objects.isNull(exception) && max != -1) {
			long maxSafe = max >> positions;

			if (value.compareTo(BigInteger.valueOf(maxSafe)) > 0) {
				exception = new ArithmeticException(
													String
															.format("تجاوز الحد الأعلى بعد الإزاحة: القيمة %d كبيرة جدًا للإزاحة بمقدار %d.",
																	value,
																	positions));
			}
		}

		if (Objects.isNull(exception) && min != -1) {
			long minSafe = min >> positions;

			if (value.compareTo(BigInteger.valueOf(minSafe)) < 0) {
				exception = new ArithmeticException(
													String
															.format("تجاوز الحد الأدنى بعد الإزاحة: القيمة %d صغيرة جدًا للإزاحة بمقدار %d.",
																	value,
																	positions));
			}
		}

		if (Objects.nonNull(exception)) {
			if (wrap) {
				throw new NaftahBugError(exception);
			}
			throw exception;
		}
	}

	/**
	 * Checks if the shift positions parameter is valid given the bit size of the type.
	 * <p>
	 * Throws a {@link NaftahBugError} if the positions are invalid.
	 * </p>
	 *
	 * @param size      the bit size of the type being shifted, or -1 if unbounded.
	 * @param positions the number of bit positions to shift.
	 * @throws NaftahBugError if positions is negative or exceeds size - 1.
	 */
	public static void checkShiftPositions( int size,
											int positions) {
		NaftahBugError naftahBugError = (NaftahBugError) checkShiftPositions(size, positions, true);
		if (Objects.nonNull(naftahBugError)) {
			throw naftahBugError;
		}
	}

	/**
	 * Validates the shift positions against the allowed range for a given bit size.
	 * <p>
	 * Returns a wrapped {@link NaftahBugError} or raw {@link ArithmeticException} if invalid,
	 * or null if valid.
	 * </p>
	 *
	 * @param size      the bit size of the type being shifted, or -1 if unbounded.
	 * @param positions the number of bits to shift.
	 * @param wrap      if true, returns a {@link NaftahBugError} wrapping the exception; otherwise raw.
	 * @return a Throwable representing the error if invalid, or null if valid.
	 */
	public static Throwable checkShiftPositions(int size,
												int positions,
												boolean wrap) {
		ArithmeticException exception = null;

		if (positions < 0 || (size != -1 && positions >= size)) {
			exception = new ArithmeticException(
												String
														.format("مقدار الإزاحة غير صالح: %d. يجب أن يكون بين 0 و %d.",
																positions,
																size - 1));
		}

		if (Objects.nonNull(exception)) {
			if (wrap) {
				return new NaftahBugError(exception);
			}
		}
		return exception;
	}
}
