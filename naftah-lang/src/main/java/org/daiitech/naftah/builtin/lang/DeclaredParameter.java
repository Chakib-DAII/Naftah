package org.daiitech.naftah.builtin.lang;

import java.util.Objects;

import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.parser.NaftahParser;
import org.daiitech.naftah.parser.NaftahParserHelper;
import org.daiitech.naftah.utils.reflect.type.JavaType;

import static org.daiitech.naftah.builtin.utils.ObjectUtils.validateType;

/**
 * Represents a parameter declared in a Naftah function.
 * <p>
 * A parameter can have a name, type, constant modifier, default value,
 * and optionally a current value if it was updated after declaration.
 *
 * <p>Note: Setting a value on a constant parameter will throw an error.
 *
 * @author Chakib Daii
 */
public final class DeclaredParameter extends Declaration {

	/**
	 * The original ANTLR context of the parameter declaration.
	 */
	private final NaftahParser.ParameterDeclarationContext originalContext;

	/**
	 * The name of the parameter.
	 */
	private final String name;

	/**
	 * Indicates whether the parameter is a constant.
	 */
	private final boolean constant;

	/**
	 * The Java class representing the type of the parameter.
	 */
	private final JavaType type;

	/**
	 * The default value assigned to the parameter.
	 */
	private final Object defaultValue;

	/**
	 * The current value of the parameter, if explicitly set.
	 */
	private Object currentValue;

	/**
	 * Tracks whether the current value has been updated from the default.
	 */
	private boolean updatedCurrentValue;

	/**
	 * Constructs a declared parameter with its definition details.
	 *
	 * @param depth           the depth of context where declared
	 * @param originalContext the original parse context of the parameter
	 * @param name            the name of the parameter
	 * @param constant        whether the parameter is constant
	 * @param type            the type of the parameter
	 * @param defaultValue    the default value of the parameter
	 */
	private DeclaredParameter(  int depth,
								NaftahParser.ParameterDeclarationContext originalContext,
								String name,
								boolean constant,
								JavaType type,
								Object defaultValue) {
		super(depth);
		this.originalContext = originalContext;
		this.name = name;
		this.constant = constant;
		this.type = type;
		this.defaultValue = defaultValue;
	}

	/**
	 * Factory method to create a new {@code DeclaredParameter}.
	 *
	 * @param depth           the depth of context where declared
	 * @param originalContext the original parse context
	 * @param name            the parameter name
	 * @param constant        whether it is a constant
	 * @param type            the parameter type
	 * @param defaultValue    the default value
	 * @return a new instance of {@code DeclaredParameter}
	 */
	public static DeclaredParameter of( int depth,
										NaftahParser.ParameterDeclarationContext originalContext,
										String name,
										boolean constant,
										JavaType type,
										Object defaultValue) {
		validateType(   name,
						defaultValue,
						type,
						Objects.isNull(originalContext) ? -1 : originalContext.getStart().getLine(),
						Objects.isNull(originalContext) ? -1 : originalContext.getStart().getCharPositionInLine());
		return new DeclaredParameter(depth, originalContext, name, constant, type, defaultValue);
	}

	/**
	 * Returns the original parse context for this parameter.
	 *
	 * @return the parameter's parse context
	 */
	public NaftahParser.ParameterDeclarationContext getOriginalContext() {
		return originalContext;
	}

	/**
	 * Returns the name of the parameter.
	 *
	 * @return the parameter name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Indicates whether this parameter is a constant.
	 *
	 * @return true if constant; false otherwise
	 */
	public boolean isConstant() {
		return constant;
	}

	/**
	 * Returns the declared type of the parameter.
	 *
	 * @return the parameter type
	 */
	public JavaType getType() {
		return type;
	}

	/**
	 * Returns the default value of the parameter.
	 *
	 * @return the default value
	 */
	public Object getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Returns the current value of the parameter if set; otherwise returns the default value.
	 *
	 * @return the effective value of the parameter
	 */
	public Object getValue() {
		return updatedCurrentValue ? currentValue : defaultValue;
	}

	/**
	 * Sets the current value of the parameter. Throws an error if the parameter is constant.
	 *
	 * @param currentValue the new value to assign
	 * @throws NaftahBugError if attempting to modify a constant parameter
	 */
	public synchronized void setValue(Object currentValue) {
		validateType(   name,
						currentValue,
						type,
						Objects.isNull(originalContext) ? -1 : originalContext.getStart().getLine(),
						Objects.isNull(originalContext) ? -1 : originalContext.getStart().getCharPositionInLine());
		if (constant) {
			throw new NaftahBugError(("""
										حدث خطأ أثناء إعادة تعيين القيمة الثابتة للمعامل: '%s'. لا يمكن إعادة تعيين ثابت.""")
					.formatted(name));
		}
		this.currentValue = currentValue;
		if (!updatedCurrentValue) {
			updatedCurrentValue = true;
		}
	}

	/**
	 * Returns a string representation of the declared parameter using helper formatting.
	 *
	 * @return the string representation
	 */
	@Override
	public String toString() {
		return NaftahParserHelper.declaredValueToString(constant, name, type, getValue());
	}
}
