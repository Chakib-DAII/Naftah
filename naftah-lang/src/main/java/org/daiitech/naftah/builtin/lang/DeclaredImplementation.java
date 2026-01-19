// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

package org.daiitech.naftah.builtin.lang;

import java.util.Map;

import org.daiitech.naftah.parser.NaftahParser;

/**
 * Represents a declared implementation (similar to a class or object blueprint) in the Naftah scripting language.
 *
 * <p>A {@code DeclaredImplementation} encapsulates the original parse context,
 * its name, object fields (variables), and the functions defined within the implementation.</p>
 *
 * <p>This class extends {@link Declaration} and is immutable once created.
 * It preserves the depth at which the implementation is declared, supporting
 * scope resolution and context management.</p>
 *
 * <p>Typical usage includes defining object fields and implementation-specific functions,
 * and resolving them during execution or code analysis.</p>
 *
 * @author Chakib Daii
 */
public final class DeclaredImplementation extends Declaration {

	/**
	 * The original ANTLR parse context for the implementation declaration.
	 */
	private final NaftahParser.ImplementationDeclarationContext originalContext;


	/**
	 * The name of the implementation.
	 */
	private final String name;

	/**
	 * Map of object fields declared within this implementation.
	 * Keyed by field name.
	 */
	private final Map<String, DeclaredVariable> objectFields;

	/**
	 * Map of functions declared within this implementation.
	 * Keyed by function name.
	 */
	private final Map<String, DeclaredFunction<?>> implementationFunctions;


	/**
	 * Private constructor for creating a {@code DeclaredImplementation}.
	 *
	 * @param depth                   the depth in the context hierarchy where declared
	 * @param originalContext         the original ANTLR parse context
	 * @param objectFields            map of declared object fields
	 * @param implementationFunctions map of declared functions
	 */
	private DeclaredImplementation( int depth,
									NaftahParser.ImplementationDeclarationContext originalContext,
									Map<String, DeclaredVariable> objectFields,
									Map<String, DeclaredFunction<?>> implementationFunctions) {
		super(depth);
		this.originalContext = originalContext;
		this.name = originalContext.ID().getText();
		this.objectFields = objectFields;
		this.implementationFunctions = implementationFunctions;
	}

	/**
	 * Factory method to create a {@code DeclaredImplementation}.
	 *
	 * @param depth                   the depth in the context hierarchy
	 * @param originalContext         the original ANTLR parse context
	 * @param objectFields            map of object fields
	 * @param implementationFunctions map of functions
	 * @return a new instance of {@code DeclaredImplementation}
	 */
	public static DeclaredImplementation of(int depth,
											NaftahParser.ImplementationDeclarationContext originalContext,
											Map<String, DeclaredVariable> objectFields,
											Map<String, DeclaredFunction<?>> implementationFunctions) {
		return new DeclaredImplementation(depth, originalContext, objectFields, implementationFunctions);
	}

	/**
	 * Returns the original ANTLR parse context for this implementation.
	 *
	 * @return the original implementation declaration context
	 */
	public NaftahParser.ImplementationDeclarationContext getOriginalContext() {
		return originalContext;
	}


	/**
	 * Returns the name of this implementation.
	 *
	 * @return the implementation name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the map of object fields declared in this implementation.
	 *
	 * @return the object fields map
	 */
	public Map<String, DeclaredVariable> getObjectFields() {
		return objectFields;
	}

	/**
	 * Returns the map of functions declared in this implementation.
	 *
	 * @return the implementation functions map
	 */
	public Map<String, DeclaredFunction<?>> getImplementationFunctions() {
		return implementationFunctions;
	}

	/**
	 * Returns a string representation of the declared implementation in Arabic.
	 *
	 * @return a formatted string representing the implementation
	 */
	@Override
	public String toString() {
		return "<%s %s>".formatted("سلوك", name);
	}
}
