package org.daiitech.naftah.core.builtin.lang;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

/**
 * @author Chakib Daii
 */
public class DynamicNumber {
  private final Number value;

  public DynamicNumber(Number value) {
    Objects.requireNonNull(value, "value must not be null");
    this.value = value;
  }

  public DynamicNumber(String value) {
    Objects.requireNonNull(value, "value must not be null");
    this.value = NumberUtils.parseDynamicNumber(value);
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
    if (isBigInteger()) return (BigInteger) value;
    return new BigInteger(value.toString());
  }

  public BigDecimal asBigDecimal() {
    if (isBigDecimal()) return (BigDecimal) value;
    if (isBigInteger()) return new BigDecimal((BigInteger) value);
    return new BigDecimal(value.toString());
  }

  public Number get() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

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

  public static DynamicNumber of(Number value) {
    return new DynamicNumber(value);
  }

  public static DynamicNumber of(String value) {
    return new DynamicNumber(value);
  }
}
