package org.daiitech.naftah.builtin.lang;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a provider of Naftah functions.
 * <p>
 * This record contains metadata about a function provider, including its name,
 * a description, and the list of function names it offers.
 * </p>
 *
 * @param name          the provider's name
 * @param description   a brief description of the provider
 * @param functionNames a list of function names provided
 * @author Chakib Daii
 */
public record NaftahFunctionProvider(
		String name,
		String description,
		List<String> functionNames
) implements Serializable {

	/**
	 * Factory method to create a {@code NaftahFunctionProvider} instance.
	 *
	 * @param name          the provider's name
	 * @param description   a brief description of the provider
	 * @param functionNames an array of function names provided
	 * @return a new {@code NaftahFunctionProvider} instance
	 */
	public static NaftahFunctionProvider of(String name, String description, String[] functionNames) {
		return new NaftahFunctionProvider(name, description, List.of(functionNames));
	}
}
