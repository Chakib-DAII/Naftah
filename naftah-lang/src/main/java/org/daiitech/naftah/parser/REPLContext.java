package org.daiitech.naftah.parser;

import java.util.HashMap;
import java.util.Map;

import org.daiitech.naftah.builtin.lang.DeclaredParameter;

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
		ETERNAL_CONTEXT.prepareParseTreeExecution();
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
		DefaultContext context = registerContext(ETERNAL_CONTEXT);
		context.prepareParseTreeExecution();
		return context;
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
		DefaultContext context = new DefaultContext(ETERNAL_CONTEXT, null, parameters, arguments);
		context.prepareParseTreeExecution();
		return context;
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
		DefaultContext context = new DefaultContext(parent, null, null, null);
		context.prepareParseTreeExecution();
		return context;
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
		DefaultContext context = new DefaultContext(parent, blockImports, null, null);
		context.prepareParseTreeExecution();
		return context;
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
		DefaultContext context = new DefaultContext(parent, blockImports, parameters, arguments);
		context.prepareParseTreeExecution();
		return context;
	}

	/**
	 * Deregisters a context at the given depth.
	 * <p>
	 * If the depth is greater than zero, removes the context from the static
	 * {@code CONTEXTS} map and merges its variables, functions, and parse tree
	 * execution state into its parent context.
	 * <p>
	 * If the depth is zero or less, returns the context at depth zero.
	 * </p>
	 *
	 * @param depth the depth index of the context to deregister
	 * @return the deregistered context if depth > 0, otherwise the context at depth zero
	 */
	public static DefaultContext deregisterContext(int depth) {
		if (depth > 0) {
			DefaultContext context = CONTEXTS.remove(depth);
			if (context.parent != null) {
				context.parent.variables.putAll(context.variables);
				context.parent.functions.putAll(context.functions);
				if (context.parseTreeExecution != null) {
					context.parent.parseTreeExecution.copyFrom(context.parseTreeExecution);
				}
			}
			return context;
		}
		return CONTEXTS.get(0);
	}
}
