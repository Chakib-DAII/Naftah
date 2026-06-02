package org.daiitech.naftah.builtin.lang;

/**
 * Lightweight metadata descriptor for a built-in function.
 *
 * <p>This record represents the static, serializable information required to
 * locate and resolve a built-in function at runtime without directly holding
 * reflective {@link java.lang.reflect.Method} references.</p>
 *
 * <p>It is primarily used by the runtime index system to support:
 * <ul>
 * <li>fast lookup of built-in functions</li>
 * <li>deferred reflective resolution</li>
 * <li>alias-based function resolution</li>
 * </ul>
 * </p>
 *
 * @param methodName           the Java method name implementing the function
 * @param className            fully qualified name of the declaring class
 * @param methodParameterTypes fully qualified parameter type names used to resolve the method via reflection
 * @param canonicalKey         the primary lookup key used in function indexing (normalized name)
 * @param qualifiedAliases     alternative lookup keys (possibly qualified or namespaced aliases)
 *
 * @author Chakib Daii
 */
public record BuiltinFunctionInfo(
		String methodName,
		String className,
		String[] methodParameterTypes,
		String canonicalKey,
		String[] qualifiedAliases
) {

	/**
	 * Returns a simplified human-readable representation of this function descriptor.
	 *
	 * <p>Format: {@code className#methodName}</p>
	 *
	 * <p>This is primarily intended for debugging and logging and does not
	 * uniquely identify overloaded methods.</p>
	 *
	 * @return a string in the form {@code className#methodName}
	 */
	@Override
	public String toString() {
		return className + "#" + methodName;
	}
}
