package org.daiitech.naftah.core.builtin.lang;

import org.daiitech.naftah.core.parser.NaftahParser;

/**
 * @author Chakib Daii defintion of function declared in Naftah script
 */
public class DeclaredFunction {
  private final NaftahParser.FunctionDeclarationContext originalContext;
  private final String name;
  private final NaftahParser.ArgumentDeclarationListContext arguments;
  private final NaftahParser.BlockContext body;
  private final NaftahParser.ReturnTypeContext returnType;

  private DeclaredFunction(NaftahParser.FunctionDeclarationContext originalContext) {
    this.originalContext = originalContext;
    this.name = originalContext.ID().getText();
    this.arguments = originalContext.argumentDeclarationList();
    this.body = originalContext.block();
    this.returnType = originalContext.returnType();
  }

  public NaftahParser.FunctionDeclarationContext getOriginalContext() {
    return originalContext;
  }

  public String getName() {
    return name;
  }

  public NaftahParser.ArgumentDeclarationListContext getArguments() {
    return arguments;
  }

  public NaftahParser.BlockContext getBody() {
    return body;
  }

  public NaftahParser.ReturnTypeContext getReturnType() {
    return returnType;
  }

  @Override
  public String toString() {
    return "<دالة s%>".formatted(name);
  }

  public static DeclaredFunction of(NaftahParser.FunctionDeclarationContext originalContext) {
    return new DeclaredFunction(originalContext);
  }
}
