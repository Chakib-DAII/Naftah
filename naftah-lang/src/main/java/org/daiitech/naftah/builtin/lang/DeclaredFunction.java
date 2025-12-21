package org.daiitech.naftah.builtin.lang;

import java.util.List;

import org.daiitech.naftah.parser.NaftahParser;
import org.daiitech.naftah.parser.NaftahParserHelper;
import org.daiitech.naftah.utils.reflect.type.JavaType;

/**
 * Represents a function declaration in the Naftah scripting language.
 * <p>
 * This class encapsulates the ANTLR parse-tree context of a function
 * declaration and exposes its semantic components, including the function
 * name, parameters, body, return type, and asynchronous modifier.
 * </p>
 *
 * <p>
 * A {@code DeclaredFunction} is a concrete, immutable declaration with respect
 * to its identity and scope; however, some derived properties (such as resolved
 * parameter and return types) may be computed lazily during later compilation
 * phases.
 * </p>
 *
 * @author Chakib Daii
 */
public final class DeclaredFunction extends Declaration {

	/**
	 * The original ANTLR parse context for the function declaration.
	 */
	private final NaftahParser.FunctionDeclarationContext originalContext;

	/**
	 * The name of the function.
	 */
	private final String name;

	/**
	 * The name of the implementation.
	 */
	private final String implementationName;

	/**
	 * Indicates whether the function is declared as asynchronous.
	 */
	private final boolean async;

	/**
	 * The parse context for the parameter declaration list.
	 */
	private final NaftahParser.ParameterDeclarationListContext parametersContext;

	/**
	 * The parse context for the function body block.
	 */
	private final NaftahParser.BlockContext body;

	/**
	 * The parse context for the return type.
	 */
	private final NaftahParser.ReturnTypeContext returnTypeContext;

	/**
	 * The list of parameters declared for the function.
	 */
	private List<DeclaredParameter> parameters;

	/**
	 * The resolved return type of the function.
	 */
	private JavaType returnType;

	/**
	 * Creates a {@code DeclaredFunction} from its parsed function declaration.
	 * <p>
	 * This constructor initializes the semantic representation of a function
	 * using information extracted from the ANTLR parse tree. It captures the
	 * function's name, async modifier, parameters, return type, and body, while
	 * retaining the original parse context for later analysis or code generation.
	 * </p>
	 *
	 * <p>
	 * This constructor is private and is intended to be used by factory methods
	 * within this class.
	 * </p>
	 *
	 * @param depth              The lexical scope depth at which the function is declared.
	 * @param originalContext    The ANTLR parse-tree context representing the function declaration.
	 * @param implementationName The internal or generated name used to identify this function during
	 *                           compilation or code generation.
	 */
	private DeclaredFunction(   int depth,
								NaftahParser.FunctionDeclarationContext originalContext,
								String implementationName) {
		super(depth);
		this.originalContext = originalContext;
		this.name = originalContext.ID().getText();
		this.async = NaftahParserHelper.hasChild(originalContext.ASYNC());
		this.parametersContext = originalContext.parameterDeclarationList();
		this.body = originalContext.block();
		this.returnTypeContext = originalContext.returnType();
		this.implementationName = implementationName;
	}

	/**
	 * Creates a {@code DeclaredFunction} from a parsed function declaration.
	 * <p>
	 * This factory method constructs a semantic representation of a function
	 * using the provided ANTLR parse-tree context. It delegates initialization
	 * to the private constructor and associates the function with its lexical
	 * scope depth and internal implementation name.
	 * </p>
	 *
	 * @param depth              The lexical scope depth at which the function is declared.
	 * @param originalContext    The ANTLR parse-tree context representing the function declaration.
	 * @param implementationName The internal or generated name used to identify this function during
	 *                           compilation or code generation.
	 * @return A newly created {@code DeclaredFunction} instance.
	 */
	public static DeclaredFunction of(  int depth,
										NaftahParser.FunctionDeclarationContext originalContext,
										String implementationName) {
		return new DeclaredFunction(depth, originalContext, implementationName);
	}

	/**
	 * Returns the original function declaration context.
	 *
	 * @return the function declaration context
	 */
	public NaftahParser.FunctionDeclarationContext getOriginalContext() {
		return originalContext;
	}

	/**
	 * Returns the name of the function.
	 *
	 * @return the function name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the internal implementation name of this function.
	 * <p>
	 * This name is used internally (for example, during name mangling,
	 * linking, or code generation) and may differ from the function’s
	 * declared source-level name.
	 * </p>
	 *
	 * @return The internal implementation name associated with this function.
	 */
	public String getImplementationName() {
		return implementationName;
	}

	/**
	 * Returns whether this function is asynchronous.
	 * <p>
	 * An asynchronous function executes in its own task and must be awaited
	 * if its result is required before proceeding.
	 *
	 * @return {@code true} if the function is asynchronous; {@code false} otherwise
	 */
	public boolean isAsync() {
		return async;
	}

	/**
	 * Returns the context for the parameter declaration list.
	 *
	 * @return the parameters context
	 */
	public NaftahParser.ParameterDeclarationListContext getParametersContext() {
		return parametersContext;
	}

	/**
	 * Returns the list of declared parameters.
	 *
	 * @return the list of parameters
	 */
	public List<DeclaredParameter> getParameters() {
		return parameters;
	}

	/**
	 * Sets the list of declared parameters.
	 *
	 * @param parameters the parameters to set
	 */
	public synchronized void setParameters(List<DeclaredParameter> parameters) {
		this.parameters = parameters;
	}

	/**
	 * Returns the context for the function body block.
	 *
	 * @return the function body context
	 */
	public NaftahParser.BlockContext getBody() {
		return body;
	}

	/**
	 * Returns the context for the return type.
	 *
	 * @return the return type context
	 */
	public NaftahParser.ReturnTypeContext getReturnTypeContext() {
		return returnTypeContext;
	}

	/**
	 * Returns the resolved return type of the function.
	 *
	 * @return the return type
	 */
	public JavaType getReturnType() {
		return returnType;
	}

	/**
	 * Sets the resolved return type of the function.
	 *
	 * @param returnType the return type to set
	 */
	public synchronized void setReturnType(JavaType returnType) {
		this.returnType = returnType;
	}

	/**
	 * Returns a string representation of the declared function in Arabic.
	 *
	 * @return a formatted string representation of the function
	 */
	@Override
	public String toString() {
		return "<%s %s>".formatted("دالة", name);
	}
}
