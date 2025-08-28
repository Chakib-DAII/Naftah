package org.daiitech.naftah.builtin.lang;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.daiitech.naftah.errors.NaftahBugError;

import static org.daiitech.naftah.parser.NaftahParserHelper.LEXER_LITERALS;

/**
 * Represents a function definition in the Naftah scripting language.
 * <p>
 * This record holds metadata about a function, including its name, description,
 * usage instructions, return type, parameter types, and declared exception types.
 * </p>
 *
 * @param name           the function name
 * @param aliases        function aliases, list of alternative function name
 * @param description    a brief description of the function
 * @param usage          usage information or signature of the function
 * @param returnType     the return type class of the function
 * @param parameterTypes a list of classes representing the parameter types
 * @param exceptionTypes a list of classes representing the exceptions the function may throw
 * @author Chakib Daii
 */
public record NaftahFunction(
		String name,
		String[] aliases,
		String description,
		String usage,
		Class<?> returnType,
		List<Class<?>> parameterTypes,
		List<Class<?>> exceptionTypes
) implements Serializable {

	public NaftahFunction {
		if (Objects.nonNull(LEXER_LITERALS) && (LEXER_LITERALS.contains(name) || Arrays
				.stream(aliases)
				.anyMatch(alias -> LEXER_LITERALS.contains(alias)))) {
			throw new NaftahBugError(
										String
												.format("اسم الدالة المضمّنة '%s' %s لا يجوز أن يتطابق مع كلمة مفتاحية في اللغة.",
														name,
														aliases.length > 0 ? " : " + Arrays.toString(aliases) : ""));
		}
	}

	/**
	 * Factory method to create a {@code NaftahFunction} instance.
	 *
	 * @param name           the function name
	 * @param description    a brief description of the function
	 * @param usage          usage information or signature of the function
	 * @param returnType     the return type class of the function
	 * @param parameterTypes an array of parameter type classes
	 * @param exceptionTypes an array of exception type classes
	 * @return a new {@code NaftahFunction} instance
	 */
	public static NaftahFunction of(String name,
									String[] aliases,
									String description,
									String usage,
									Class<?> returnType,
									Class<?>[] parameterTypes,
									Class<?>[] exceptionTypes) {
		return new NaftahFunction(  name,
									aliases,
									description,
									usage,
									returnType,
									List.of(parameterTypes),
									List.of(exceptionTypes));
	}
}
