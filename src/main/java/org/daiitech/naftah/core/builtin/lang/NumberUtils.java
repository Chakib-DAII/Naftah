package org.daiitech.naftah.core.builtin.lang;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Utility class for dynamically parsing and performing arithmetic operations on various numeric types.
 * Supports Byte, Short, Integer, Long, Float, Double, BigInteger, and BigDecimal.
 *
 * @author Chakib Daii
 */
public final class NumberUtils {

    /**
     * Parses a string into the most appropriate {@link Number} type.
     * <ul>
     *     <li>Parses decimal numbers (with dot or exponent) as Float, Double, or BigDecimal (in that order).</li>
     *     <li>Parses whole numbers as Byte, Short, Integer, Long, or BigInteger (in that order).</li>
     * </ul>
     *
     * @param text the input numeric string
     * @return the parsed Number
     * @throws RuntimeException if parsing fails or if value is NaN or infinite
     */
    public static Number parseDynamicNumber(String text) {
        try {
            if (text.contains(".") || text.toLowerCase().contains("e")) {
                try {
                    // Try parsing as a float first
                    float f = Float.parseFloat(text);
                    if (Float.isInfinite(f)) {
                        throw new NumberFormatException("Floating-point overflow: value is infinite for input " + text);
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
                            throw new NumberFormatException("Floating-point overflow: value is infinite/ for input " + text);
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
    // ==============================
    // Arithmetic Operations
    // ==============================

    /**
     * Adds two {@link Number} values.
     *
     * @param x
     * @param y
     * @return
     * @param <T>
     */
    public static  <T extends Number> Number add(T x, T y) {
        DynamicNumber dx = DynamicNumber.of(x);
        DynamicNumber dy = DynamicNumber.of(y);
        return add(dx,dy);
    }
    /**
     * Adds two numeric values represented as strings.
     *
     * @param x
     * @param y
     * @return
     * @param <T>
     */
    public static  <T extends Number> Number add(String x, String y) {
        DynamicNumber dx = DynamicNumber.of(x);
        DynamicNumber dy = DynamicNumber.of(y);
        return add(dx,dy);
    }

    /**
     * Adds two {@link DynamicNumber} instances with type promotion.
     *
     * @param dx
     * @param dy
     * @return
     * @param <T>
     */
    public static  <T extends Number> Number add(DynamicNumber dx, DynamicNumber dy) {
        Number result;
        if (dx.isDecimal() || dy.isDecimal()) {
            if (dx.isBigDecimal() || dy.isBigDecimal()) {
                result =  dx.asBigDecimal().add(dy.asBigDecimal());
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
                    "Unsupported number types: " + dx.get().getClass() + ", " + dy.get().getClass()
            );
        }
        return result;
    }

    public static  <T extends Number> Number subtract(T x, T y) {
        DynamicNumber dx = DynamicNumber.of(x);
        DynamicNumber dy = DynamicNumber.of(y);
        return subtract(dx,dy);
    }

    public static  <T extends Number> Number subtract(String x, String y) {
        DynamicNumber dx = DynamicNumber.of(x);
        DynamicNumber dy = DynamicNumber.of(y);
        return subtract(dx,dy);
    }

    public static  <T extends Number> Number subtract(DynamicNumber dx, DynamicNumber dy) {
        Number result;
        if (dx.isDecimal() || dy.isDecimal()) {
            if (dx.isBigDecimal() || dy.isBigDecimal()) {
                result =  dx.asBigDecimal().subtract(dy.asBigDecimal());
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
                    "Unsupported number types: " + dx.get().getClass() + ", " + dy.get().getClass()
            );
        }
        return result;
    }

    public static  <T extends Number> Number multiply(T x, T y) {
        DynamicNumber dx = DynamicNumber.of(x);
        DynamicNumber dy = DynamicNumber.of(y);
        return multiply(dx,dy);
    }

    public static  <T extends Number> Number multiply(String x, String y) {
        DynamicNumber dx = DynamicNumber.of(x);
        DynamicNumber dy = DynamicNumber.of(y);
        return multiply(dx,dy);
    }

    public static  <T extends Number> Number multiply(DynamicNumber dx, DynamicNumber dy) {
        Number result;
        if (dx.isDecimal() || dy.isDecimal()) {
            if (dx.isBigDecimal() || dy.isBigDecimal()) {
                result =  dx.asBigDecimal().multiply(dy.asBigDecimal());
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
                    "Unsupported number types: " + dx.get().getClass() + ", " + dy.get().getClass()
            );
        }
        return result;}

    public static  <T extends Number> Number divide(T x, T y) {
        DynamicNumber dx = DynamicNumber.of(x);
        DynamicNumber dy = DynamicNumber.of(y);
        return divide(dx,dy);
    }

    public static  <T extends Number> Number divide(String x, String y) {
        DynamicNumber dx = DynamicNumber.of(x);
        DynamicNumber dy = DynamicNumber.of(y);
        return divide(dx,dy);
    }

    public static  <T extends Number> Number divide(DynamicNumber dx, DynamicNumber dy) {
        Number result;
        if (dx.isDecimal() || dy.isDecimal()) {
            if (dx.isBigDecimal() || dy.isBigDecimal()) {
                result =  dx.asBigDecimal().divide(dy.asBigDecimal(), MathContext.UNLIMITED);
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
                    "Unsupported number types: " + dx.get().getClass() + ", " + dy.get().getClass()
            );
        }
        return result;}

    public static  <T extends Number> Number modulo(T x, T y) {
        DynamicNumber dx = DynamicNumber.of(x);
        DynamicNumber dy = DynamicNumber.of(y);
        return modulo(dx,dy);
    }

    public static  <T extends Number> Number modulo(String x, String y) {
        DynamicNumber dx = DynamicNumber.of(x);
        DynamicNumber dy = DynamicNumber.of(y);
        return modulo(dx,dy);
    }

    public static  <T extends Number> Number modulo(DynamicNumber dx, DynamicNumber dy) {
        Number result;
        if (dx.isDecimal() || dy.isDecimal()) {
            if (dx.isBigDecimal() || dy.isBigDecimal()) {
                result =  dx.asBigDecimal().remainder(dy.asBigDecimal());
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
                    "Unsupported number types: " + dx.get().getClass() + ", " + dy.get().getClass()
            );
        }
        return result;
    }

    public static  <T extends Number> Number max(T x, T y) {
        DynamicNumber dx = DynamicNumber.of(x);
        DynamicNumber dy = DynamicNumber.of(y);
        return max(dx,dy);
    }

    public static  <T extends Number> Number max(String x, String y) {
        DynamicNumber dx = DynamicNumber.of(x);
        DynamicNumber dy = DynamicNumber.of(y);
        return max(dx,dy);
    }
    public static  <T extends Number> Number max(DynamicNumber dx, DynamicNumber dy) {
        Number result;
        if (dx.isDecimal() || dy.isDecimal()) {
            if (dx.isBigDecimal() || dy.isBigDecimal()) {
                result =  dx.asBigDecimal().max(dy.asBigDecimal());
            } else if (dx.isDouble() || dy.isDouble()) {
                result = Math.max(dx.asDouble() , dy.asDouble());
            } else {
                result = Math.max(dx.asFloat() , dy.asFloat());
            }
        } else if (dx.isInteger() || dy.isInteger()) {
            if (dx.isBigInteger() || dy.isBigInteger()) {
                result = dx.asBigInteger().max(dy.asBigInteger());
            } else if (dx.isLong() || dy.isLong()) {
                result = Math.max(dx.asLong() , dy.asLong());
            } else {
                result = Math.max(dx.asInt() , dy.asInt());
            }
        } else {
            // Unknown or unsupported number types
            throw new UnsupportedOperationException(
                    "Unsupported number types: " + dx.get().getClass() + ", " + dy.get().getClass()
            );
        }
        return result;
    }

    public static  <T extends Number> Number min(T x, T y) {
        DynamicNumber dx = DynamicNumber.of(x);
        DynamicNumber dy = DynamicNumber.of(y);
        return min(dx,dy);
    }

    public static  <T extends Number> Number min(String x, String y) {
        DynamicNumber dx = DynamicNumber.of(x);
        DynamicNumber dy = DynamicNumber.of(y);
        return min(dx,dy);
    }
    public static  <T extends Number> Number min(DynamicNumber dx, DynamicNumber dy) {
        Number result;
        if (dx.isDecimal() || dy.isDecimal()) {
            if (dx.isBigDecimal() || dy.isBigDecimal()) {
                result =  dx.asBigDecimal().min(dy.asBigDecimal());
            } else if (dx.isDouble() || dy.isDouble()) {
                result = Math.min(dx.asDouble() , dy.asDouble());
            } else {
                result = Math.min(dx.asFloat() , dy.asFloat());
            }
        } else if (dx.isInteger() || dy.isInteger()) {
            if (dx.isBigInteger() || dy.isBigInteger()) {
                result = dx.asBigInteger().min(dy.asBigInteger());
            } else if (dx.isLong() || dy.isLong()) {
                result = Math.min(dx.asLong() , dy.asLong());
            } else {
                result = Math.min(dx.asInt() , dy.asInt());
            }
        } else {
            // Unknown or unsupported number types
            throw new UnsupportedOperationException(
                    "Unsupported number types: " + dx.get().getClass() + ", " + dy.get().getClass()
            );
        }
        return result;
    }
    public static <T extends Number> Number pow(DynamicNumber base, int exponent) {
        if (base.isBigDecimal() || base.isBigInteger()) {
            return base.asBigDecimal().pow(exponent);
        } else if (base.isBigInteger()) {
            return base.asBigInteger().pow(exponent);
        }
        return Math.pow(base.asDouble(), exponent);
    }

    public static  <T extends Number> Number round(T x) {
        DynamicNumber dx = DynamicNumber.of(x);
        return round(dx);
    }

    public static  <T extends Number> Number round(String x) {
        DynamicNumber dx = DynamicNumber.of(x);
        return round(dx);
    }
    public static  <T extends Number> Number round(DynamicNumber dx) {
        if (dx.isDecimal() || dx.isInteger()) return dx.asBigDecimal().round(MathContext.UNLIMITED);
        if (dx.isDouble() || dx.isLong()) return Math.round(dx.asDouble());
        return Math.round(dx.asFloat());
    }
    public static  <T extends Number> Number floor(T x) {
        DynamicNumber dx = DynamicNumber.of(x);
        return floor(dx);
    }

    public static  <T extends Number> Number floor(String x) {
        DynamicNumber dx = DynamicNumber.of(x);
        return floor(dx);
    }
    public static  <T extends Number> Number floor(DynamicNumber dx) {
        if (dx.isBigDecimal()) {
            return dx.asBigDecimal().setScale(0, RoundingMode.FLOOR);
        } else if (dx.isBigInteger()) {
            return dx.asBigInteger();  // floor is identity for BigInteger
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
            throw new UnsupportedOperationException(
                    "Unsupported number type: " + dx.get().getClass()
            );
        }
    }
    public static  <T extends Number> Number ceil(T x) {
        DynamicNumber dx = DynamicNumber.of(x);
        return ceil(dx);
    }

    public static  <T extends Number> Number ceil(String x) {
        DynamicNumber dx = DynamicNumber.of(x);
        return ceil(dx);
    }
    public static  <T extends Number> Number ceil(DynamicNumber dx) {
        if (dx.isBigDecimal()) {
            return dx.asBigDecimal().setScale(0, RoundingMode.CEILING);
        } else if (dx.isBigInteger()) {
            return dx.asBigInteger();  // floor is identity for BigInteger
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
            throw new UnsupportedOperationException(
                    "Unsupported number type: " + dx.get().getClass()
            );
        }
    }
    public static  <T extends Number> Number negate(T x) {
        DynamicNumber dx = DynamicNumber.of(x);
        return negate(dx);
    }

    public static  <T extends Number> Number negate(String x) {
        DynamicNumber dx = DynamicNumber.of(x);
        return negate(dx);
    }
    public static <T extends Number> Number negate(DynamicNumber dx) {
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
            throw new UnsupportedOperationException(
                    "Unsupported number type: " + dx.get().getClass()
            );
        }
        return result;
    }
    public static  <T extends Number> Number sqrt(T x) {
        DynamicNumber dx = DynamicNumber.of(x);
        return sqrt(dx);
    }

    public static  <T extends Number> Number sqrt(String x) {
        DynamicNumber dx = DynamicNumber.of(x);
        return sqrt(dx);
    }
    public static  <T extends Number> Number sqrt(DynamicNumber dx) {
        if (dx.isBigDecimal()) return dx.asBigDecimal().sqrt(MathContext.UNLIMITED);
        if (dx.isBigInteger()) return dx.asBigInteger().sqrt();
        return Math.sqrt(dx.asDouble());
    }
    public static  <T extends Number> Number abs(T x) {
        DynamicNumber dx = DynamicNumber.of(x);
        return abs(dx);
    }

    public static  <T extends Number> Number abs(String x) {
        DynamicNumber dx = DynamicNumber.of(x);
        return abs(dx);
    }
    public static  <T extends Number> Number abs(DynamicNumber dx) {
        if (dx.isDecimal()) return dx.asBigDecimal().abs();
        if (dx.isBigInteger()) return dx.asBigInteger().abs();
        return Math.abs(dx.asDouble());
    }
    public static <T extends Number> int signum(T x) {
        DynamicNumber dx = DynamicNumber.of(x);
        return signum(dx);
    }

    public static <T extends Number> int signum(String x) {
        DynamicNumber dx = DynamicNumber.of(x);
        return signum(dx);
    }
    public static int signum(DynamicNumber dx) {
        if (dx.isDecimal()) return dx.asBigDecimal().signum();
        if (dx.isInteger()) return dx.asBigInteger().signum();
        return Double.compare(dx.asDouble(), 0);
    }
    public static <T extends Number> boolean isZero(T x) {
        DynamicNumber dx = DynamicNumber.of(x);
        return isZero(dx);
    }

    public static <T extends Number> boolean isZero(String x) {
        DynamicNumber dx = DynamicNumber.of(x);
        return isZero(dx);
    }
    public static boolean isZero(DynamicNumber dx) {
        return signum(dx) == 0;
    }

    public static  <T extends Number> boolean equals(T x, T y) {
        DynamicNumber dx = DynamicNumber.of(x);
        DynamicNumber dy = DynamicNumber.of(y);
        return equals(dx,dy);
    }

    public static  <T extends Number> boolean equals(String x, String y) {
        DynamicNumber dx = DynamicNumber.of(x);
        DynamicNumber dy = DynamicNumber.of(y);
        return equals(dx,dy);
    }
    public static boolean equals(DynamicNumber dx, DynamicNumber dy) {
        return compare(dx, dy) == 0;
    }

    public static  <T extends Number> int compare(T x, T y) {
        DynamicNumber dx = DynamicNumber.of(x);
        DynamicNumber dy = DynamicNumber.of(y);
        return compare(dx,dy);
    }

    public static  <T extends Number> int compare(String x, String y) {
        DynamicNumber dx = DynamicNumber.of(x);
        DynamicNumber dy = DynamicNumber.of(y);
        return compare(dx,dy);
    }
    public static int compare(DynamicNumber dx, DynamicNumber dy) {
        if (dx.isDecimal() || dy.isDecimal()) {
            return dx.asBigDecimal().compareTo(dy.asBigDecimal());
        } else if (dx.isInteger() || dy.isInteger()) {
            return dx.asBigInteger().compareTo(dy.asBigInteger());
        } else {
            // Unknown or unsupported number types
            throw new UnsupportedOperationException(
                    "Unsupported number types: " + dx.get().getClass() + ", " + dy.get().getClass()
            );
        }
    }
}