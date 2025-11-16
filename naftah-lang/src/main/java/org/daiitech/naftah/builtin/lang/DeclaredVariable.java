package org.daiitech.naftah.builtin.lang;

import java.util.Objects;

import org.antlr.v4.runtime.ParserRuleContext;
import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.parser.NaftahParserHelper;

/**
 * Represents a variable declared in the Naftah scripting language.
 * <p>
 * A declared variable has a name, type, constant modifier, default value,
 * and may hold a current value if updated during execution.
 *
 * <p>Attempting to modify a constant variable will result in a {@link NaftahBugError}.
 *
 * <p>This class also tracks the original parsing context for reference or debugging.
 *
 * @author Chakib Daii
 */
public final class DeclaredVariable {

	/**
	 * The name of the variable.
	 */
	private final String name;

	/**
	 * Whether the variable is declared as constant.
	 */
	private final boolean constant;

	/**
	 * The Java class representing the type of the variable.
	 */
	private final Class<?> type;

	/**
	 * The default value assigned to the variable at declaration.
	 */
	private final Object defaultValue;

	/**
	 * The original parse context where this variable was declared.
	 */
	private ParserRuleContext originalContext;

	/**
	 * The current value assigned to the variable (if updated).
	 */
	private Object currentValue;

	/**
	 * Whether the current value has been explicitly set.
	 */
	private boolean updatedCurrentValue;

	/**
	 * Constructs a new declared variable with the given properties.
	 *
	 * @param originalContext the original parser context of the declaration
	 * @param name            the name of the variable
	 * @param constant        whether the variable is a constant
	 * @param type            the type of the variable
	 * @param defaultValue    the default value of the variable
	 */
	private DeclaredVariable(   ParserRuleContext originalContext,
								String name,
								boolean constant,
								Class<?> type,
								Object defaultValue) {
		this.originalContext = originalContext;
		this.name = name;
		this.constant = constant;
		this.type = type;
		this.defaultValue = defaultValue;
	}

	/**
	 * Factory method to create a {@code DeclaredVariable} instance.
	 *
	 * @param originalContext the original parser context
	 * @param name            the variable name
	 * @param constant        whether the variable is constant
	 * @param type            the variable type
	 * @param defaultValue    the default value
	 * @return a new {@code DeclaredVariable} instance
	 */
	public static DeclaredVariable of(  ParserRuleContext originalContext,
										String name,
										boolean constant,
										Class<?> type,
										Object defaultValue) {
		return new DeclaredVariable(originalContext, name, constant, type, defaultValue);
	}

	/**
	 * Returns the original parser context of this variable declaration.
	 *
	 * @return the original context
	 */
	public ParserRuleContext getOriginalContext() {
		return originalContext;
	}

	/**
	 * Sets the original parser context of this variable declaration.
	 *
	 * @param originalContext the parser context to set
	 */
	public void setOriginalContext(ParserRuleContext originalContext) {
		this.originalContext = originalContext;
	}

	/**
	 * Returns the name of the variable.
	 *
	 * @return the variable name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Indicates whether the variable is a constant.
	 *
	 * @return true if constant; false otherwise
	 */
	public boolean isConstant() {
		return constant;
	}

	/**
	 * Returns the type of the variable.
	 *
	 * @return the variable type
	 */
	public Class<?> getType() {
		return type;
	}

	/**
	 * Returns the default value of the variable.
	 *
	 * @return the default value
	 */
	public Object getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Returns the current value of the variable if updated; otherwise returns the default value.
	 *
	 * @return the effective value of the variable
	 */
	public Object getValue() {
		return updatedCurrentValue ? currentValue : defaultValue;
	}

	/**
	 * Updates the current value of the variable.
	 * <p>
	 * Throws an error if the variable is constant.
	 *
	 * @param currentValue the value to set
	 * @throws NaftahBugError if attempting to modify a constant variable
	 */
	public void setValue(Object currentValue) {
		if (constant) {
			throw new NaftahBugError(
										"حدث خطأ أثناء إعادة تعيين القيمة الثابتة: '%s'. لا يمكن إعادة تعيين ثابت."
												.formatted(name));
		}
		this.currentValue = currentValue;
		if (!updatedCurrentValue) {
			updatedCurrentValue = true;
		}
	}

	/**
	 * Compares this {@code DeclaredVariable} to another object for equality.
	 *
	 * <p>Two {@code DeclaredVariable} instances are considered equal if and only if:
	 * <ul>
	 * <li>They are of the same runtime class, and</li>
	 * <li>They have equal values for all of the following properties:
	 * {@code name}, {@code constant}, {@code type}, {@code defaultValue},
	 * {@code currentValue}, and {@code updatedCurrentValue}.
	 * </li>
	 * </ul>
	 * </p>
	 *
	 * @param o the object to compare with this instance
	 * @return {@code true} if the specified object is equal to this one; otherwise {@code false}
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		DeclaredVariable that = (DeclaredVariable) o;
		return constant == that.constant && updatedCurrentValue == that.updatedCurrentValue && Objects
				.equals(name,
						that.name) && Objects
								.equals(
										type,
										that.type) && Objects.equals(defaultValue, that.defaultValue) && Objects
												.equals(
														currentValue,
														that.currentValue);
	}

	/**
	 * Computes the hash code for this {@code DeclaredVariable}.
	 *
	 * <p>The hash code is based on the same set of fields used in
	 * {@link #equals(Object)} to ensure consistency between equality
	 * and hashing (as required by the Java specification).</p>
	 *
	 * @return a hash code value for this object
	 */
	@Override
	public int hashCode() {
		return Objects.hash(name, constant, type, defaultValue, currentValue, updatedCurrentValue);
	}


	/**
	 * Returns a string representation of the variable using helper formatting.
	 *
	 * @return a formatted string representing the variable
	 */
	@Override
	public String toString() {
		return NaftahParserHelper.declaredValueToString(constant, name, getValue());
	}
}
