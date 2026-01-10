package org.daiitech.naftah.builtin.lang;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.utils.reflect.type.JavaType;

import static org.daiitech.naftah.builtin.utils.ObjectUtils.getNaftahType;
import static org.daiitech.naftah.parser.DefaultNaftahParserVisitor.PARSER_VOCABULARY;
import static org.daiitech.naftah.parser.NaftahParserHelper.LEXER_LITERALS;
import static org.daiitech.naftah.utils.reflect.ClassUtils.getQualifiedName;

/**
 * Represents a function definition in the Naftah scripting language.
 * <p>
 * This record holds metadata about a function, including its name, description,
 * usage instructions, return type, parameter types, and declared exception types.
 * </p>
 *
 * @param name                the function name
 * @param useQualifiedName    flags that the function names should be bound with the provider's name
 * @param useQualifiedAliases flags that the function aliases should be bound with the provider's name
 * @param aliases             function aliases, list of alternative function name
 * @param description         a brief description of the function
 * @param usage               usage information or signature of the function
 * @param returnType          the return type class of the function
 * @param parameterTypes      a list of classes representing the parameter types
 * @param exceptionTypes      a list of classes representing the exceptions the function may throw
 * @author Chakib Daii
 */
public record NaftahFunction(
		String name,
		boolean useQualifiedName,
		boolean useQualifiedAliases,
		String[] aliases,
		String description,
		String usage,
		Class<?> returnType,
		List<Class<?>> parameterTypes,
		List<Class<?>> exceptionTypes
) implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

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
	 * @param name                the function name
	 * @param useQualifiedName    flags that the function names should be bound with the provider's name
	 * @param useQualifiedAliases flags that the function aliases should be bound with the provider's name
	 * @param description         a brief description of the function
	 * @param usage               usage information or signature of the function
	 * @param returnType          the return type class of the function
	 * @param parameterTypes      an array of parameter type classes
	 * @param exceptionTypes      an array of exception type classes
	 * @return a new {@code NaftahFunction} instance
	 */
	public static NaftahFunction of(String name,
									boolean useQualifiedName,
									boolean useQualifiedAliases,
									String[] aliases,
									String description,
									String usage,
									Class<?> returnType,
									Class<?>[] parameterTypes,
									Class<?>[] exceptionTypes) {
		return new NaftahFunction(  name,
									useQualifiedName,
									useQualifiedAliases,
									aliases,
									description,
									usage,
									returnType,
									List.of(parameterTypes),
									List.of(exceptionTypes));
	}

	@Override
	public String toString() {
		return """
				دالـة نفطـه:
					\t\t\t- الاسم: %s
					\t\t\t- الأسماء المستعارة: %s
					\t\t\t- الوصف: %s
					\t\t\t- الاستخدام: %s
					\t\t\t- نوع الإرجاع: %s
					\t\t\t- أنواع المعاملات: %s
					\t\t\t- أنواع الاستثناءات: %s"""
				.formatted(
							name,
							aliases.length == 0 ? "لا شيء" : String.join(", ", aliases),
							description,
							usage,
							Objects.isNull(PARSER_VOCABULARY) ?
									getQualifiedName(returnType.getName()) :
									getNaftahType(  PARSER_VOCABULARY,
													JavaType.of(returnType)),
							parameterTypes.isEmpty() ?
									"لا شيء" :
									parameterTypes
											.stream()
											.map(aClass -> Objects.isNull(PARSER_VOCABULARY) ?
													getQualifiedName(aClass.getName()) :
													getNaftahType(  PARSER_VOCABULARY,
																	JavaType.of(aClass)))
											.collect(Collectors.joining(", ")),
							exceptionTypes.isEmpty() ?
									"لا شيء" :
									exceptionTypes
											.stream()
											.map(aClass -> Objects.isNull(PARSER_VOCABULARY) ?
													getQualifiedName(aClass.getName()) :
													getNaftahType(  PARSER_VOCABULARY,
																	JavaType.of(aClass)))
											.collect(Collectors.joining(", "))
				);
	}
}
