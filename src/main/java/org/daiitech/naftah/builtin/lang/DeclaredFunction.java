package org.daiitech.naftah.builtin.lang;

import java.util.List;

import org.daiitech.naftah.parser.NaftahParser;

/**
 * Definition of a function declared in the Naftah script.
 * <p>
 * This class wraps the original parse context of a function declaration
 * and provides access to its components such as name, parameters, body,
 * and return type.
 *
 * @author Chakib Daii
 */
public final class DeclaredFunction {

    /**
     * The original ANTLR parse context for the function declaration.
     */
    private final NaftahParser.FunctionDeclarationContext originalContext;

    /**
     * The name of the function.
     */
    private final String name;

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
    private Object returnType;

    /**
     * Private constructor that initializes the function from its parse context.
     *
     * @param originalContext the original function declaration context
     */
    private DeclaredFunction(NaftahParser.FunctionDeclarationContext originalContext) {
        this.originalContext = originalContext;
        this.name = originalContext.ID().getText();
        this.parametersContext = originalContext.parameterDeclarationList();
        this.body = originalContext.block();
        this.returnTypeContext = originalContext.returnType();
    }

    /**
     * Factory method to create a {@code DeclaredFunction} from the provided context.
     *
     * @param originalContext the function declaration context
     * @return a new instance of {@code DeclaredFunction}
     */
    public static DeclaredFunction of(NaftahParser.FunctionDeclarationContext originalContext) {
        return new DeclaredFunction(originalContext);
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
    public void setParameters(List<DeclaredParameter> parameters) {
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
    public Object getReturnType() {
        return returnType;
    }

    /**
     * Sets the resolved return type of the function.
     *
     * @param returnType the return type to set
     */
    public void setReturnType(Object returnType) {
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
