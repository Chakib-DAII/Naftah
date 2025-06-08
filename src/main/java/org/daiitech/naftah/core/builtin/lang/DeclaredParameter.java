package org.daiitech.naftah.core.builtin.lang;

import org.daiitech.naftah.core.parser.NaftahParser;

/**
 * @author Chakib Daii
 */
public class DeclaredParameter {
  private final NaftahParser.ParameterDeclarationContext originalContext;
  private final String name;
  private final boolean constant;
  private final Object type;
  private final Object defaultValue;
  private Object currentValue;
  private boolean updatedCurrentValue;

  private DeclaredParameter(
      NaftahParser.ParameterDeclarationContext originalContext,
      String name,
      boolean constant,
      Object type,
      Object defaultValue) {
    this.originalContext = originalContext;
    this.name = name;
    this.constant = constant;
    this.type = type;
    this.defaultValue = defaultValue;
  }

  public NaftahParser.ParameterDeclarationContext getOriginalContext() {
    return originalContext;
  }

  public String getName() {
    return name;
  }

  public boolean isConstant() {
    return constant;
  }

  public Object getType() {
    return type;
  }

  public Object getDefaultValue() {
    return defaultValue;
  }

  public Object getValue() {
    return updatedCurrentValue ? currentValue : defaultValue;
  }

  public void setValue(Object currentValue) {
    if (constant) throw new IllegalStateException("setting a constant");
    this.currentValue = currentValue;
    if (!updatedCurrentValue) updatedCurrentValue = true;
  }

  @Override
  public String toString() {
    return "<%s %s>".formatted(name, constant ? "ثابت" : "متغير");
  }

  public static DeclaredParameter of(
      NaftahParser.ParameterDeclarationContext originalContext,
      String name,
      boolean constant,
      Object type,
      Object defaultValue) {
    return new DeclaredParameter(originalContext, name, constant, type, defaultValue);
  }
}
