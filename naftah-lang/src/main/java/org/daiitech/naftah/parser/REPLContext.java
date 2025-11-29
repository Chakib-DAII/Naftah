package org.daiitech.naftah.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
	 * Deregisters the current context from the context management system.
	 * <p>
	 * This method retrieves the {@link DefaultContext} associated with the current
	 * thread (via {@code CURRENT_CONTEXT}) and removes it if possible.
	 * If the context has a parent, its variables, functions, and parse tree execution
	 * state are merged into the parent before removal.
	 * </p>
	 * <p>
	 * The actual removal is attempted only if there are no pending tasks in this context
	 * or its descendants. Otherwise, the context may be marked for later removal.
	 * </p>
	 */
	public static void deregisterContext() {
		DefaultContext currentContext = CURRENT_CONTEXT.get();
		deregisterContext(currentContext);
	}

	/**
	 * Deregisters the specified {@link DefaultContext}.
	 * <p>
	 * If the context has a parent and can be safely removed (i.e., it has no pending tasks
	 * and its children are idle), this method will:
	 * <ul>
	 * <li>Remove the context from the static {@code CONTEXTS} map.</li>
	 * <li>Merge the context's variables and functions into its parent.</li>
	 * <li>Copy the parse tree execution state to the parent, if available.</li>
	 * </ul>
	 * </p>
	 * <p>
	 * If the context cannot be removed (because it has pending tasks), this method will
	 * leave it in place and may mark it for removal later.
	 * </p>
	 *
	 * @param context the {@link DefaultContext} to deregister; must not be {@code null}
	 */
	public static void deregisterContext(DefaultContext context) {
		if (Objects.nonNull(context) && Objects.nonNull(context.parent) && tryDeregisterContext(
																								context)) {
			context.parent.variables.get().putAll(context.variables.get());
			context.parent.functions.get().putAll(context.functions.get());
			if (Objects.nonNull(context.parseTreeExecution) && Objects
					.nonNull(context.parent.parseTreeExecution)) {
				context.parent.parseTreeExecution.get().copyFrom(context.parseTreeExecution.get());
			}
		}
	}
}
