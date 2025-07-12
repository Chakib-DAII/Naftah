package org.daiitech.naftah.builtin.lang;

import java.util.Optional;
import org.antlr.v4.runtime.ParserRuleContext;
import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.parser.NaftahParserHelper;

/**
 * @author Chakib Daii
 */
public class DeclaredVariable {
  private ParserRuleContext originalContext;
  private final String name;
  private final boolean constant;
  private final Object type;
  private final Object defaultValue;
  private Object currentValue;
  private boolean updatedCurrentValue;

  private DeclaredVariable(
      ParserRuleContext originalContext,
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

  public ParserRuleContext getOriginalContext() {
    return originalContext;
  }

  public void setOriginalContext(ParserRuleContext originalContext) {
    this.originalContext = originalContext;
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
    if (constant)
      throw new NaftahBugError(
          "حدث خطأ أثناء إعادة تعيين القيمة الثابتة: '%s'. لا يمكن إعادة تعيين ثابت."
              .formatted(name));
    this.currentValue = currentValue;
    if (!updatedCurrentValue) updatedCurrentValue = true;
  }

  @Override
  public String toString() {
    return "<%s %s = %s>"
        .formatted(
            constant ? "ثابت" : "متغير",
            name,
            Optional.ofNullable(getValue()).orElse(NaftahParserHelper.NULL));
  }

  public static DeclaredVariable of(
      ParserRuleContext originalContext,
      String name,
      boolean constant,
      Object type,
      Object defaultValue) {
    return new DeclaredVariable(originalContext, name, constant, type, defaultValue);
  }
}
