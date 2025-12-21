package org.daiitech.naftah.builtin.lang;

/**
 * Represents a named declaration in the Naftah scripting language.
 * <p>
 * A {@code Declaration} is the common abstraction for all language constructs
 * that may appear as named declarations within a lexical or semantic scope,
 * including variables, functions, parameters, and implementations.
 * </p>
 *
 * <p>
 * This sealed abstract class restricts extension to its permitted subclasses
 * ({@link DeclaredVariable}, {@link DeclaredFunction},
 * {@link DeclaredParameter}, and {@link DeclaredImplementation}), preserving
 * the integrity of the declaration model.
 * </p>
 *
 * <p>
 * Each declaration records the {@code depth} at which it appears in the
 * language’s scope hierarchy. The depth corresponds to the lexical nesting
 * level of the surrounding context (for example, global scope, function body,
 * or control-flow block).
 * </p>
 *
 * <p>
 * This class is immutable: the depth of a declaration is assigned at
 * construction time and cannot be modified.
 * </p>
 *
 * @author Chakib Daii
 */
public abstract sealed class Declaration permits DeclaredVariable, DeclaredFunction, DeclaredParameter,
		DeclaredImplementation {

	/**
	 * The lexical or semantic depth at which this declaration was introduced.
	 * <p>
	 * Depth {@code 0} typically represents the global or root scope.
	 * Incremental values represent deeper nested scopes.
	 */
	protected final int depth;

	/**
	 * Creates a new declaration at the specified depth.
	 *
	 * @param depth the scope depth where this declaration appears
	 */
	public Declaration(int depth) {
		this.depth = depth;
	}

	/**
	 * Returns the depth of this declaration within the scope hierarchy.
	 *
	 * @return the declaration depth
	 */
	public int getDepth() {
		return depth;
	}
}
