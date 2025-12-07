package org.daiitech.naftah.builtin.lang;

/**
 * Represents a declaration element in the Naftah scripting language.
 * <p>
 * A {@code Declaration} is the common abstraction for all language constructs
 * that can appear as named declarations within a given lexical or semantic scope,
 * such as variables, functions, and parameters.
 *
 * <p>This sealed abstract class ensures that only the permitted subclasses
 * ({@link DeclaredVariable}, {@link DeclaredFunction}, and {@link DeclaredParameter})
 * can extend it, preserving the integrity of the declaration model.
 *
 * <p>Each declaration tracks the {@code depth} at which it appears in the
 * language's scope hierarchy. The depth corresponds to the nesting level of the
 * surrounding context (e.g., global scope, function body, control-block scope).
 *
 * <p>This class is immutable: the depth of a declaration is assigned at
 * construction time and cannot be modified.
 *
 * @author Chakib Daii
 */
public abstract sealed class Declaration permits DeclaredVariable, DeclaredFunction, DeclaredParameter {

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
