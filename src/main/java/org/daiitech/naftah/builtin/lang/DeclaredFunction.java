package org.daiitech.naftah.builtin.lang;

import java.util.List;

import org.daiitech.naftah.parser.NaftahParser;

/**
 * definition of function declared in Naftah script
 *
 * @author Chakib Daii
 */
public class DeclaredFunction {
  private final NaftahParser.FunctionDeclarationContext originalContext;
  private final String name;
  private final NaftahParser.ParameterDeclarationListContext parametersContext;
  private List<DeclaredParameter> parameters;
  private final NaftahParser.BlockContext body;
  private final NaftahParser.ReturnTypeContext returnTypeContext;
  private Object returnType;

  private DeclaredFunction(NaftahParser.FunctionDeclarationContext originalContext) {
    this.originalContext = originalContext;
    this.name = originalContext.ID().getText();
    this.parametersContext = originalContext.parameterDeclarationList();
    this.body = originalContext.block();
    this.returnTypeContext = originalContext.returnType();
  }

  public NaftahParser.FunctionDeclarationContext getOriginalContext() {
    return originalContext;
  }

  public String getName() {
    return name;
  }

  public NaftahParser.ParameterDeclarationListContext getParametersContext() {
    return parametersContext;
  }

  public List<DeclaredParameter> getParameters() {
    return parameters;
  }

  public void setParameters(List<DeclaredParameter> parameters) {
    this.parameters = parameters;
  }

  public NaftahParser.BlockContext getBody() {
    return body;
  }

  public NaftahParser.ReturnTypeContext getReturnTypeContext() {
    return returnTypeContext;
  }

  public Object getReturnType() {
    return returnType;
  }

  public void setReturnType(Object returnType) {
    this.returnType = returnType;
  }

  @Override
  public String toString() {
    return "<%s %s>".formatted("دالة", name);
  }

  public static DeclaredFunction of(NaftahParser.FunctionDeclarationContext originalContext) {
    return new DeclaredFunction(originalContext);
  }
}
