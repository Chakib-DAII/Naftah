package org.daiitech.naftah.core.parser;

import static org.daiitech.naftah.core.builtin.utils.ObjectUtils.*;
import static org.daiitech.naftah.core.builtin.utils.op.BinaryOperation.*;
import static org.daiitech.naftah.core.builtin.utils.op.UnaryOperation.*;
import static org.daiitech.naftah.core.parser.NaftahParserHelper.*;
import static org.daiitech.naftah.utils.DefaultContext.*;
import static org.daiitech.naftah.utils.NaftahExecutionLogger.logExecution;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.misc.Pair;
import org.daiitech.naftah.core.builtin.lang.*;
import org.daiitech.naftah.core.builtin.utils.NumberUtils;
import org.daiitech.naftah.core.builtin.utils.ObjectUtils;
import org.daiitech.naftah.utils.DefaultContext;
import org.daiitech.naftah.utils.StringInterpolator;

/**
 * @author Chakib Daii
 */
public class DefaultNaftahParserVisitor
    extends org.daiitech.naftah.core.parser.NaftahParserBaseVisitor<Object> {

  private static final Logger LOGGER = Logger.getLogger("DefaultNaftahParserVisitor");
  public static final String FORMATTER = "index: %s, text: %s, payload: %s";
  private int depth = 0;

  @Override
  public Object visitProgram(org.daiitech.naftah.core.parser.NaftahParser.ProgramContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitProgram(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    // TODO: add the functions (processed from classpath and provider annotations)
    var rootContext =
        hasChildOrSubChildOfType(
                ctx,
                org.daiitech.naftah.core.parser.NaftahParser.FunctionCallStatementContext.class)
            ? DefaultContext.registerContext(new HashMap<>(), new HashMap<>())
            : DefaultContext.registerContext();
    depth = rootContext.getDepth();
    Object result = null;
    for (org.daiitech.naftah.core.parser.NaftahParser.StatementContext statement :
        ctx.statement()) {
      result = visit(statement); // Visit each statement in the program
      // break program after executing a return statement
      if (rootContext.hasAnyExecutedChildOrSubChildOfType(
          statement,
          org.daiitech.naftah.core.parser.NaftahParser.ReturnStatementStatementContext.class))
        break;
    }
    rootContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitDeclarationStatement(
      org.daiitech.naftah.core.parser.NaftahParser.DeclarationStatementContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitDeclarationStatement(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    var result = visit(ctx.declaration());
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitAssignmentStatement(
      org.daiitech.naftah.core.parser.NaftahParser.AssignmentStatementContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitAssignmentStatement(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    var result = visit(ctx.assignment());
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitFunctionDeclarationStatement(
      org.daiitech.naftah.core.parser.NaftahParser.FunctionDeclarationStatementContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitFunctionDeclarationStatement(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    var result = visit(ctx.functionDeclaration());
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitFunctionCallStatement(
      org.daiitech.naftah.core.parser.NaftahParser.FunctionCallStatementContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitFunctionCallStatement(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    var result = visit(ctx.functionCall());
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitIfStatementStatement(
      org.daiitech.naftah.core.parser.NaftahParser.IfStatementStatementContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitIfStatementStatement(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    var result = visit(ctx.ifStatement());
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitReturnStatementStatement(
      org.daiitech.naftah.core.parser.NaftahParser.ReturnStatementStatementContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitReturnStatementStatement(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    var result = visit(ctx.returnStatement());
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitBlockStatement(
      org.daiitech.naftah.core.parser.NaftahParser.BlockStatementContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitBlockStatement(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    var result = visit(ctx.block());
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitDeclaration(
      org.daiitech.naftah.core.parser.NaftahParser.DeclarationContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitDeclaration(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    String variableName = ctx.ID().getText();
    // variable -> new : flags if this is a new variable or not
    Pair<DeclaredVariable, Boolean> declaredVariable;
    boolean isConstant = hasChild(ctx.CONSTANT());
    boolean isConstantOrVariable = isConstant || hasChild(ctx.VARIABLE());
    boolean hasType = hasChild(ctx.type());
    if (isConstantOrVariable || hasType) {
      declaredVariable =
          new Pair<>(
              DeclaredVariable.of(
                  ctx, variableName, isConstant, hasType ? visit(ctx.type()) : null, null),
              true);
      // TODO: check if inside function to check if it matches any argument / parameter or
      // previously
      // declared and update if possible
      currentContext.defineVariable(variableName, declaredVariable.a);
    } else {
      declaredVariable = new Pair<>(currentContext.getVariable(variableName, false).b, false);
    }
    currentContext.markExecuted(ctx); // Mark as executed
    return currentContext.isParsingAssignment() ? declaredVariable : declaredVariable.a;
  }

  @Override
  public Object visitAssignment(
      org.daiitech.naftah.core.parser.NaftahParser.AssignmentContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitAssignment(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    currentContext.setParsingAssignment(true);
    Pair<DeclaredVariable, Boolean> declaredVariable =
        (Pair<DeclaredVariable, Boolean>) visit(ctx.declaration());
    // TODO: check if inside function to check if it matches any argument / parameter or previously
    if (declaredVariable.b) {
      declaredVariable =
          new Pair<>(
              DeclaredVariable.of(
                  ctx,
                  declaredVariable.a.getName(),
                  declaredVariable.a.isConstant(),
                  declaredVariable.a.getType(),
                  visit(ctx.expression())),
              declaredVariable.b);
    } else {
      declaredVariable.a.setOriginalContext(ctx);
      declaredVariable.a.setValue(visit(ctx.expression()));
    }
    // declared and update if possible
    currentContext.setVariable(declaredVariable.a.getName(), declaredVariable.a);
    currentContext.setParsingAssignment(false);
    currentContext.markExecuted(ctx); // Mark as executed
    return declaredVariable;
  }

  @Override
  public Object visitFunctionDeclaration(
      org.daiitech.naftah.core.parser.NaftahParser.FunctionDeclarationContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitFunctionDeclaration(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    String functionName = ctx.ID().getText();
    DeclaredFunction declaredFunction = DeclaredFunction.of(ctx);
    currentContext.defineFunction(functionName, declaredFunction);
    currentContext.markExecuted(ctx); // Mark as executed
    return declaredFunction;
  }

  @Override
  public Object visitParameterDeclarationList(
      org.daiitech.naftah.core.parser.NaftahParser.ParameterDeclarationListContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitArgumentDeclarationList(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    List<DeclaredParameter> args = new ArrayList<>();
    for (org.daiitech.naftah.core.parser.NaftahParser.ParameterDeclarationContext
        argumentDeclaration : ctx.parameterDeclaration()) {
      args.add((DeclaredParameter) visit(argumentDeclaration));
    }
    currentContext.markExecuted(ctx); // Mark as executed
    return args;
  }

  @Override
  public Object visitParameterDeclaration(
      org.daiitech.naftah.core.parser.NaftahParser.ParameterDeclarationContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitArgumentDeclaration(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    String argumentName = ctx.ID().getText();
    var result =
        DeclaredParameter.of(
            ctx,
            argumentName,
            hasChild(ctx.CONSTANT()),
            hasChild(ctx.type()) ? visit(ctx.type()) : Object.class,
            hasChild(ctx.value()) ? visit(ctx.value()) : null);
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitFunctionCall(
      org.daiitech.naftah.core.parser.NaftahParser.FunctionCallContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitFunctionCall(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    Object result = null;
    // TODO: add extra vars to context to get the function called and so on, it can be a free map
    // TODO:  and using an Enum as key of predefined ids to get values
    currentContext.setParsingFunctionCallId(true);
    String functionName =
        hasChild(ctx.ID()) ? ctx.ID().getText() : (String) visit(ctx.qualifiedCall());
    // TODO: add support to variables as qualified call and match to the jvm function
    String functionCallId = DefaultContext.FUNCTION_CALL_ID_GENERATOR.apply(depth, functionName);
    currentContext.setFunctionCallId(functionCallId);
    List<Pair<String, Object>> args = new ArrayList<>();
    // TODO: add support to global variables as argument
    if (hasChild(ctx.argumentList())) args = (List<Pair<String, Object>>) visit(ctx.argumentList());

    if (currentContext.containsFunction(functionName)) {
      Object function = currentContext.getFunction(functionName, false).b;
      if (function instanceof DeclaredFunction declaredFunction) {
        try {
          prepareDeclaredFunction(this, declaredFunction);
          var finalArgs =
              prepareDeclaredFunctionArguments(this, declaredFunction.getParameters(), args);

          currentContext.defineFunctionParameters(
              declaredFunction.getParameters().stream()
                  .map(parameter -> Map.entry(parameter.getName(), parameter))
                  .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
              true);

          currentContext.defineFunctionArguments(finalArgs);
          pushCall(declaredFunction, finalArgs);
          result = visit(declaredFunction.getBody());
        } finally {
          popCall();
        }
      } else if (function instanceof BuiltinFunction builtinFunction) {
        var methodArgs =
            args.stream().map(stringObjectPair -> stringObjectPair.b).toArray(Object[]::new);
        try {
          var possibleResult = builtinFunction.method().invoke(null, methodArgs);
          if (builtinFunction.functionInfo().returnType() != Void.class && possibleResult != null)
            result = possibleResult;
        } catch (IllegalAccessException | InvocationTargetException e) {
          throw new RuntimeException(e);
        }
      } else if (function instanceof JvmFunction jvmFunction) {
        throw new UnsupportedOperationException(
            "Function %s of type: %s".formatted(functionName, JvmFunction.class.getName()));
      } else if (function instanceof Collection<?> functions) {
        throw new UnsupportedOperationException(
            "Function %s : %s of type: %s"
                .formatted(functionName, functions, List.class.getName()));
      }
    } else throw new RuntimeException("Function not found: " + functionName);
    currentContext.setFunctionCallId(null);
    // TODO: add support for all kind of functions using the qualifiedName
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitQualifiedCall(
      org.daiitech.naftah.core.parser.NaftahParser.QualifiedCallContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitQualifiedCall(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    var result =
        visit(ctx.qualifiedName()) + ctx.COLON(0).getText() + ctx.COLON(1).getText() + ctx.ID();
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitArgumentList(
      org.daiitech.naftah.core.parser.NaftahParser.ArgumentListContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitArgumentList(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    List<Pair<String, Object>> args = new ArrayList<>();
    for (int i = 0; i < ctx.expression().size(); i++) {
      String name = hasChild(ctx.ID(i)) ? ctx.ID(i).getText() : null;
      Object value = visit(ctx.expression(i));
      args.add(new Pair<>(name, value)); // Evaluate each expression in the argument list
    }
    currentContext.markExecuted(ctx); // Mark as executed
    return args;
  }

  @Override
  public Object visitIfStatement(
      org.daiitech.naftah.core.parser.NaftahParser.IfStatementContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitIfStatement(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    Object result = null;
    Object condition = visit(ctx.expression(0)); // Evaluate the condition expression
    if (isTruthy(condition)) {
      result = visit(ctx.block(0)); // If the condition is true, execute the 'then' block
    } else {
      // Iterate through elseif blocks
      for (int i = 0; i < ctx.ELSEIF().size(); i++) {
        Object elseifCondition = visit(ctx.expression(i)); // Evaluate elseif condition
        if (isTruthy(elseifCondition)) {
          result =
              visit(
                  ctx.block(i + 1)); // Execute the corresponding elseif block if condition is true
          break;
        }
      }

      // If no elseif was true, execute the else block (if it exists)
      if (hasChild(ctx.ELSE())) {
        result = visit(ctx.block(ctx.ELSEIF().size() + 1)); // Execute the 'else' block if present
      }
    }
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitReturnStatement(
      org.daiitech.naftah.core.parser.NaftahParser.ReturnStatementContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitReturnStatement(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    Object result = null;
    if (hasChild(ctx.expression())) {
      result = visit(ctx.expression()); // Evaluate and return the result
    }
    currentContext.markExecuted(ctx); // Mark as executed
    return result; // No expression after 'return' means returning null
  }

  @Override
  public Object visitBlock(org.daiitech.naftah.core.parser.NaftahParser.BlockContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitBlock(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    var nextContext =
        hasChildOrSubChildOfType(
                    ctx,
                    org.daiitech.naftah.core.parser.NaftahParser.FunctionCallStatementContext.class)
                || hasChildOrSubChildOfType(
                    ctx,
                    org.daiitech.naftah.core.parser.NaftahParser.FunctionCallExpressionContext
                        .class)
            ? DefaultContext.registerContext(currentContext, new HashMap<>(), new HashMap<>())
            : DefaultContext.registerContext(currentContext);
    depth = nextContext.getDepth();
    Object result = null;
    for (org.daiitech.naftah.core.parser.NaftahParser.StatementContext statement :
        ctx.statement()) {
      result = visit(statement); // Visit each statement in the block
      // break program after executing a return statemnt
      if (nextContext.hasAnyExecutedChildOrSubChildOfType(
          statement,
          org.daiitech.naftah.core.parser.NaftahParser.ReturnStatementStatementContext.class))
        break;
    }
    DefaultContext.deregisterContext(depth);
    depth--;
    nextContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitValueExpression(
      org.daiitech.naftah.core.parser.NaftahParser.ValueExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitValueExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    var result = visit(ctx.value());
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitMinusExpression(
      org.daiitech.naftah.core.parser.NaftahParser.MinusExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitMinusExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    Object left = visit(ctx.expression(0)); // Left operand
    Object right = visit(ctx.expression(1)); // Right operand
    Object result;
    if(left == null || right == null){
      result = null;
    } else {
      result = hasChild(ctx.MINUS()) ? ObjectUtils.applyOperation(left, right, SUBTRACT):
              ObjectUtils.applyOperation(left, right, ELEMENTWISE_SUBTRACT);
    }
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitParenthesisExpression(
      org.daiitech.naftah.core.parser.NaftahParser.ParenthesisExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitParenthesisExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    var result = visit(ctx.expression());
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitModExpression(
      org.daiitech.naftah.core.parser.NaftahParser.ModExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitModExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    Object left = visit(ctx.expression(0)); // Left operand
    Object right = visit(ctx.expression(1)); // Right operand
    Object result;
    if(left == null || right == null){
      result = null;
    } else {
      result = hasChild(ctx.MOD()) ?  ObjectUtils.applyOperation(left, right, MODULO) :
              ObjectUtils.applyOperation(left, right, ELEMENTWISE_MODULO);
    }
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitDivExpression(
      org.daiitech.naftah.core.parser.NaftahParser.DivExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitDivExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    Object left = visit(ctx.expression(0)); // Left operand
    Object right = visit(ctx.expression(1)); // Right operand
    Object result;
    if(left == null || right == null){
      result = null;
    } else {
      result = hasChild(ctx.DIV()) ? ObjectUtils.applyOperation(left, right, DIVIDE)
              : ObjectUtils.applyOperation(left, right, ELEMENTWISE_DIVIDE);
    }
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitGreaterThanExpression(
      org.daiitech.naftah.core.parser.NaftahParser.GreaterThanExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitGreaterThanExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    Object left = visit(ctx.expression(0)); // Left operand
    Object right = visit(ctx.expression(1)); // Right operand
    Object result;
    if(left == null || right == null){
      result = null;
    } else {
      result = ObjectUtils.applyOperation(left, right, GREATER_THAN);
    }
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitLessThanEqualsExpression(
      org.daiitech.naftah.core.parser.NaftahParser.LessThanEqualsExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitLessThanEqualsExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    Object left = visit(ctx.expression(0)); // Left operand
    Object right = visit(ctx.expression(1)); // Right operand
    Object result;
    if(left == null || right == null){
      result = null;
    } else {
      result = ObjectUtils.applyOperation(left, right, LESS_THAN_EQUALS);
    }
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitGreaterThanEqualsExpression(
      org.daiitech.naftah.core.parser.NaftahParser.GreaterThanEqualsExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitGreaterThanEqualsExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    Object left = visit(ctx.expression(0)); // Left operand
    Object right = visit(ctx.expression(1)); // Right operand
    Object result;
    if(left == null || right == null){
      result = null;
    } else {
      result = ObjectUtils.applyOperation(left, right, GREATER_THAN_EQUALS);
    }
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitNotEqualsExpression(
      org.daiitech.naftah.core.parser.NaftahParser.NotEqualsExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitNotEqualsExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    Object left = visit(ctx.expression(0)); // Left operand
    Object right = visit(ctx.expression(1)); // Right operand
    Object result;
    if(left == null || right == null){
      result = null;
    } else {
      result = ObjectUtils.applyOperation(left, right, NOT_EQUALS);
    }
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitEqualsExpression(
      org.daiitech.naftah.core.parser.NaftahParser.EqualsExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitEqualsExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    Object left = visit(ctx.expression(0)); // Left operand
    Object right = visit(ctx.expression(1)); // Right operand
    Object result;
    if(left == null || right == null){
      result = null;
    } else {
      result = ObjectUtils.applyOperation(left, right, EQUALS);
    }
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitLessThanExpression(
      org.daiitech.naftah.core.parser.NaftahParser.LessThanExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitLessThanExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    Object left = visit(ctx.expression(0)); // Left operand
    Object right = visit(ctx.expression(1)); // Right operand
    Object result;
    if(left == null || right == null){
      result = null;
    } else {
      result = ObjectUtils.applyOperation(left, right, LESS_THAN);
    }
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitFunctionCallExpression(
      org.daiitech.naftah.core.parser.NaftahParser.FunctionCallExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitFunctionCallExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    var result = visit(ctx.functionCall());
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitPlusExpression(
      org.daiitech.naftah.core.parser.NaftahParser.PlusExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitPlusExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    Object left = visit(ctx.expression(0)); // Left operand
    Object right = visit(ctx.expression(1)); // Right operand
    Object result;
    if(left == null || right == null){
      result = null;
    } else {
      result = hasChild(ctx.PLUS()) ?
              ObjectUtils.applyOperation(left, right, ADD)
              : ObjectUtils.applyOperation(left, right, ELEMENTWISE_ADD) ;
    }
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitMulExpression(
      org.daiitech.naftah.core.parser.NaftahParser.MulExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitMulExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    Object left = visit(ctx.expression(0)); // Left operand
    Object right = visit(ctx.expression(1)); // Right operand
    Object result;
    if(left == null || right == null){
      result = null;
    } else {
      result = hasChild(ctx.MUL()) ? ObjectUtils.applyOperation(left, right, MULTIPLY)
              : ObjectUtils.applyOperation(left, right, ELEMENTWISE_MULTIPLY) ;
    }
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitNumberValue(
      org.daiitech.naftah.core.parser.NaftahParser.NumberValueContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitNumberValue(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    Object value = ctx.NUMBER().getText();
    var result = NumberUtils.parseDynamicNumber(value);
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitCharacterValue(
      org.daiitech.naftah.core.parser.NaftahParser.CharacterValueContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitStringValue(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    Character result = ctx.CHARACTER().getText().charAt(1);
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitStringValue(
      org.daiitech.naftah.core.parser.NaftahParser.StringValueContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitStringValue(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    String value = ctx.STRING().getText();
    var result = StringInterpolator.process(value, currentContext);
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitTrueValue(org.daiitech.naftah.core.parser.NaftahParser.TrueValueContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitTrueValue(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    currentContext.markExecuted(ctx); // Mark as executed
    return Boolean.TRUE;
  }

  @Override
  public Object visitFalseValue(
      org.daiitech.naftah.core.parser.NaftahParser.FalseValueContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitFalseValue(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    currentContext.markExecuted(ctx); // Mark as executed
    return Boolean.FALSE;
  }

  @Override
  public Object visitNullValue(org.daiitech.naftah.core.parser.NaftahParser.NullValueContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitNullValue(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    currentContext.markExecuted(ctx); // Mark as executed
    return null;
  }

  @Override
  public Object visitIdValue(org.daiitech.naftah.core.parser.NaftahParser.IdValueContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitIdValue(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    String id = ctx.ID().getText();
    var result = VARIABLE_GETTER.apply(id, currentContext);
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitVoidReturnType(
      org.daiitech.naftah.core.parser.NaftahParser.VoidReturnTypeContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitVoidReturnType(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    var result = getJavaType(ctx);
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitTypeReturnType(
      org.daiitech.naftah.core.parser.NaftahParser.TypeReturnTypeContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitTypeReturnType(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    var result = visit(ctx.type());
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitVarType(org.daiitech.naftah.core.parser.NaftahParser.VarTypeContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitVarType(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    var result = getJavaType(ctx);
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitBuiltInType(
      org.daiitech.naftah.core.parser.NaftahParser.BuiltInTypeContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitBuiltInType(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    var result = getJavaType(ctx);
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitBuiltIn(org.daiitech.naftah.core.parser.NaftahParser.BuiltInContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitBuiltIn(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    var result = getJavaType(ctx);
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitQualifiedNameType(
      org.daiitech.naftah.core.parser.NaftahParser.QualifiedNameTypeContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitQualifiedNameType(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    // TODO: think about using id to variable or necessary other elements
    var result = visit(ctx.qualifiedName());
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitQualifiedName(
      org.daiitech.naftah.core.parser.NaftahParser.QualifiedNameContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitQualifiedName(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    Object result;
    if (currentContext.isParsingFunctionCallId()) {
      result = getQualifiedName(ctx);
      currentContext.setParsingFunctionCallId(false);
    } else result = getJavaType(ctx);
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitBitwiseXorExpression(
      org.daiitech.naftah.core.parser.NaftahParser.BitwiseXorExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitBitwiseXorExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    Object left = visit(ctx.expression(0)); // Left operand
    Object right = visit(ctx.expression(1)); // Right operand
    Object result;
    if(left == null || right == null){
      result = null;
    } else {
      result = ObjectUtils.applyOperation(left, right, BITWISE_XOR);
    }
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitNotExpression(
      org.daiitech.naftah.core.parser.NaftahParser.NotExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitNotExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    var result = not(visit(ctx.expression()));
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitPreDecrementExpression(
      org.daiitech.naftah.core.parser.NaftahParser.PreDecrementExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitPreDecrementExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    Object value = visit(ctx.expression());
    Object result;
    if(value == null){
      result = null;
    } else {
      result = ObjectUtils.applyOperation(value, PRE_DECREMENT);
    }
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitPostDecrementExpression(
      org.daiitech.naftah.core.parser.NaftahParser.PostDecrementExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitPostDecrementExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    Object value = visit(ctx.expression());
    Object result;
    if(value == null){
      result = null;
    } else {
      result = ObjectUtils.applyOperation(value, POST_DECREMENT);
    }
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitBitwiseOrExpression(
      org.daiitech.naftah.core.parser.NaftahParser.BitwiseOrExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitBitwiseOrExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    Object left = visit(ctx.expression(0)); // Left operand
    Object right = visit(ctx.expression(1)); // Right operand
    Object result;
    if(left == null || right == null){
      result = null;
    } else {
      result = ObjectUtils.applyOperation(left, right, BITWISE_OR);
    }
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitBitwiseNotExpression(
      org.daiitech.naftah.core.parser.NaftahParser.BitwiseNotExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitBitwiseNotExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    Object value = visit(ctx.expression());
    Object result;
    if(value == null){
      result = null;
    } else {
      result = ObjectUtils.applyOperation(value, BITWISE_NOT);
    }
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitBitwiseAndExpression(
      org.daiitech.naftah.core.parser.NaftahParser.BitwiseAndExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitBitwiseAndExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    Object left = visit(ctx.expression(0)); // Left operand
    Object right = visit(ctx.expression(1)); // Right operand
    Object result;
    if(left == null || right == null){
      result = null;
    } else {
      result = ObjectUtils.applyOperation(left, right, BITWISE_AND);
    }
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitPreIncrementExpression(
      org.daiitech.naftah.core.parser.NaftahParser.PreIncrementExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitPreIncrementExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    Object value = visit(ctx.expression());
    Object result;
    if(value == null){
      result = null;
    } else {
      result = ObjectUtils.applyOperation(value, PRE_INCREMENT);
    }
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }

  @Override
  public Object visitPostIncrementExpression(
      org.daiitech.naftah.core.parser.NaftahParser.PostIncrementExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitPostIncrementExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    var currentContext = DefaultContext.getContextByDepth(depth);
    Object value = visit(ctx.expression());
    Object result;
    if(value == null){
      result = null;
    } else {
      result = ObjectUtils.applyOperation(value, POST_INCREMENT);
    }
    currentContext.markExecuted(ctx); // Mark as executed
    return result;
  }
}
