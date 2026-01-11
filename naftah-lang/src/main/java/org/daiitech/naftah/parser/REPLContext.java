package org.daiitech.naftah.parser;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.daiitech.naftah.builtin.lang.DeclaredFunction;
import org.daiitech.naftah.builtin.lang.DeclaredImplementation;
import org.daiitech.naftah.builtin.lang.DeclaredParameter;
import org.daiitech.naftah.builtin.lang.DeclaredVariable;

/**
 * REPLContext extends {@link DefaultContext} to provide
 * a managed context environment for REPL (Read-Eval-Print Loop) execution.
 * <p>
 * It maintains an eternal context shared across all instances and supports
 * registering and deregistering contexts with inheritance of variables,
 * functions, and parse tree execution states.
 * </p>
 *
 * @author Chakib Daii
 */
public class REPLContext extends DefaultContext {

	/**
	 * The eternal, shared base context for all REPL executions.
	 * <p>
	 * Initialized statically with empty parameters and arguments,
	 * and prepared for parse tree execution.
	 * </p>
	 */
	protected static final DefaultContext ETERNAL_CONTEXT;

	static {
		ETERNAL_CONTEXT = DefaultContext.registerContext(new HashMap<>(), new HashMap<>());
	}

	/**
	 * Registers a new context using the eternal context as the parent.
	 * <p>
	 * The new context is prepared for parse tree execution before returning.
	 * </p>
	 *
	 * @return a new {@link DefaultContext} instance with {@link #ETERNAL_CONTEXT} as parent
	 */
	public static DefaultContext registerContext() {
		return registerContext(ETERNAL_CONTEXT);
	}

	/**
	 * Registers a new context with specified parameters and arguments.
	 * <p>
	 * The new context uses the eternal context as its parent.
	 * It is prepared for parse tree execution before returning.
	 * </p>
	 *
	 * @param parameters the declared parameters for the new context
	 * @param arguments  the argument values for the new context
	 * @return a new {@link DefaultContext} initialized with given parameters and arguments
	 */
	public static DefaultContext registerContext(   Map<String, DeclaredParameter> parameters,
													Map<String, Object> arguments) {
		return new DefaultContext(ETERNAL_CONTEXT, null, parameters, arguments);
	}

	/**
	 * Registers a new {@link DefaultContext} with the specified parent context.
	 * <p>
	 * The new context inherits configuration and state from the parent context,
	 * and is automatically prepared for parse tree execution before being returned.
	 * </p>
	 *
	 * @param parent the parent {@link DefaultContext} of the new context; may be {@code null}
	 * @return a new {@link DefaultContext} instance linked to the specified parent
	 */
	public static DefaultContext registerContext(DefaultContext parent) {
		return new DefaultContext(parent, null, null, null);
	}

	/**
	 * Registers a new {@link DefaultContext} with the specified parent context and
	 * an initial set of block imports.
	 * <p>
	 * The new context inherits its parent’s configuration and is initialized with
	 * the provided {@code blockImports} mapping. It is automatically prepared for
	 * parse tree execution before being returned.
	 * </p>
	 *
	 * @param parent       the parent {@link DefaultContext} of the new context; may be {@code null}
	 * @param blockImports a map of block-level imports to associate with the new context; may be {@code null}
	 * @return a new {@link DefaultContext} instance configured with the specified parent and block imports
	 */
	public static DefaultContext registerContext(DefaultContext parent, Map<String, String> blockImports) {
		return new DefaultContext(parent, blockImports, null, null);
	}

	/**
	 * Registers a fully configured {@link DefaultContext} with the specified parent context,
	 * block imports, declared parameters, and argument values.
	 * <p>
	 * This method provides the most flexible initialization for new contexts. The created
	 * context inherits from its parent, applies the given block imports, parameters, and
	 * argument mappings, and is automatically prepared for parse tree execution before
	 * being returned.
	 * </p>
	 *
	 * @param parent       the parent {@link DefaultContext} of the new context; may be {@code null}
	 * @param blockImports a map of block-level imports to associate with the new context; may be {@code null}
	 * @param parameters   a map of declared parameters to register with the new context; may be {@code null}
	 * @param arguments    a map of argument values corresponding to the declared parameters; may be {@code null}
	 * @return a new {@link DefaultContext} instance initialized with the specified configuration
	 */
	public static DefaultContext registerContext(   DefaultContext parent,
													Map<String, String> blockImports,
													Map<String, DeclaredParameter> parameters,
													Map<String, Object> arguments) {
		return new DefaultContext(parent, blockImports, parameters, arguments);
	}

	/**
	 * Clears all REPL-related contexts.
	 * <p>
	 * This method performs two main actions:
	 * <ol>
	 * <li>Calls {@link DefaultContext#clear()} to remove all globally stored contexts.</li>
	 * <li>Invokes {@link #cleanThreadLocals()} to reset any
	 * thread-local data associated with the REPL, preventing leakage across sessions.</li>
	 * </ol>
	 * After calling this method, the REPL state is fully reset and ready for a fresh session.
	 * </p>
	 */
	public static void clear() {
		DefaultContext.clear();
		ETERNAL_CONTEXT.cleanThreadLocals();
	}

	/**
	 * Returns all currently imported elements in the REPL session.
	 *
	 * <p>
	 * Each import is returned as a string in the format:
	 * {@code <imported_element> تحت_إسم <alias>} (meaning "under the name").
	 * </p>
	 *
	 * @return a collection of strings representing all imports and their aliases
	 */
	public static Collection<String> getImports() {
		return IMPORTS
				.entrySet()
				.stream()
				.map(
						importEntry -> importEntry.getValue() + " تحت_إسم " + importEntry.getKey()
				)
				.toList();
	}

	/**
	 * Removes the specified imports from the REPL session.
	 *
	 * @param ids an array of import identifiers to remove; whitespace is trimmed
	 */
	public static void dropImports(String[] ids) {
		for (String id : ids) {
			IMPORTS.remove(id.trim());
		}
	}

	/**
	 * Returns all declared variables in the current REPL session.
	 *
	 * @return a collection of {@link DeclaredVariable} objects
	 */
	public static Collection<DeclaredVariable> getVariables() {
		return ETERNAL_CONTEXT.variables.get().values();
	}

	/**
	 * Removes the specified variables from the current REPL session.
	 *
	 * @param ids an array of variable names to remove; whitespace is trimmed
	 */
	public static void dropVariables(String[] ids) {
		var variableMap = ETERNAL_CONTEXT.variables.get();
		for (String id : ids) {
			variableMap.remove(id.trim());
		}
	}

	/**
	 * Returns all declared functions in the current REPL session.
	 *
	 * @return a collection of {@link DeclaredFunction} objects
	 */
	public static Collection<DeclaredFunction<?>> getFunctions() {
		return ETERNAL_CONTEXT.functions.get().values();
	}

	/**
	 * Removes the specified functions from the current REPL session.
	 *
	 * @param ids an array of function names to remove; whitespace is trimmed
	 */
	public static void dropFunctions(String[] ids) {
		var functionMap = ETERNAL_CONTEXT.functions.get();
		for (String id : ids) {
			functionMap.remove(id.trim());
		}
	}

	/**
	 * Returns all declared implementations (behaviors) in the current REPL session.
	 *
	 * @return a collection of {@link DeclaredImplementation} objects
	 */
	public static Collection<DeclaredImplementation> getImplementations() {
		return ETERNAL_CONTEXT.implementations.get().values();
	}

	/**
	 * Removes the specified implementations (behaviors) from the current REPL session.
	 *
	 * @param ids an array of implementation names to remove; whitespace is trimmed
	 */
	public static void dropImplementations(String[] ids) {
		var implemetationMap = ETERNAL_CONTEXT.implementations.get();
		for (String id : ids) {
			implemetationMap.remove(id.trim());
		}
	}
}
