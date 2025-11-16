package org.daiitech.naftah.builtin.lang;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Represents a provider of Naftah functions.
 * <p>
 * This record contains metadata about a function provider, including its name,
 * a description, and the list of function names it offers.
 * </p>
 *
 * @param name                the provider's name
 * @param useQualifiedName    flags that the function names should be bound with the provider's name
 * @param useQualifiedAliases flags that the function aliases should be bound with the provider's name
 * @param description         a brief description of the provider
 * @param functionNames       a list of function names provided
 * @author Chakib Daii
 */
public record NaftahFunctionProvider(
		String name,
		boolean useQualifiedName,
		boolean useQualifiedAliases,
		String description,
		List<String> functionNames
) implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * Factory method to create a {@code NaftahFunctionProvider} instance.
	 *
	 * @param name                the provider's name
	 * @param useQualifiedName    flags that the function names should be bound with the provider's name
	 * @param useQualifiedAliases flags that the function aliases should be bound with the provider's name
	 * @param description         a brief description of the provider
	 * @param functionNames       an array of function names provided
	 * @return a new {@code NaftahFunctionProvider} instance
	 */
	public static NaftahFunctionProvider of(String name,
											boolean useQualifiedName,
											boolean useQualifiedAliases,
											String description,
											String[] functionNames) {
		return new NaftahFunctionProvider(  name,
											useQualifiedName,
											useQualifiedAliases,
											description,
											List.of(functionNames));
	}

	@Override
	public String toString() {
		return """
				مزود دوال نفطـه:
				\t\t\t- الاسم: %s
				\t\t\t- الوصف: %s
				\t\t\t- الدوال: %s""".formatted(name, description, String.join(", ", functionNames));
	}
}
