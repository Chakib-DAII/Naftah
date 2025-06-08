package org.daiitech.naftah.core.parser;

import static org.daiitech.naftah.core.builtin.utils.ObjectUtils.isTruthy;
import static org.daiitech.naftah.core.builtin.utils.ObjectUtils.not;
import static org.daiitech.naftah.core.parser.NaftahParserHelper.*;
import static org.daiitech.naftah.utils.DefaultContext.VARIABLE_GETTER;
import static org.daiitech.naftah.utils.NaftahExecutionLogger.logExecution;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.misc.Pair;
import org.daiitech.naftah.core.builtin.lang.BuiltinFunction;
import org.daiitech.naftah.core.builtin.lang.DeclaredParameter;
import org.daiitech.naftah.core.builtin.lang.DeclaredFunction;
import org.daiitech.naftah.core.builtin.lang.DeclaredVariable;
import org.daiitech.naftah.core.builtin.utils.NumberUtils;
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
            hasChildOfType(ctx.statement(), org.daiitech.naftah.core.parser.NaftahParser.FunctionCallStatementContext.class) ?
                    DefaultContext.registerContext(Collections.emptyMap(), Collections.emptyMap(), new HashMap<>(), new HashMap<>()):
                    DefaultContext.registerContext(Collections.emptyMap(), Collections.emptyMap());
    depth = rootContext.getDepth();
    for (org.daiitech.naftah.core.parser.NaftahParser.StatementContext statement :
        ctx.statement()) {
      visit(statement); // Visit each statement in the program
    }
    return null;
  }

  @Override
  public Object visitAssignmentStatement(
      org.daiitech.naftah.core.parser.NaftahParser.AssignmentStatementContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitAssignmentStatement(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    return visit(ctx.assignment());
  }

  @Override
  public Object visitFunctionDeclarationStatement(
      org.daiitech.naftah.core.parser.NaftahParser.FunctionDeclarationStatementContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitFunctionDeclarationStatement(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    return visit(ctx.functionDeclaration());
  }

  @Override
  public Object visitFunctionCallStatement(
      org.daiitech.naftah.core.parser.NaftahParser.FunctionCallStatementContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitFunctionCallStatement(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    return visit(ctx.functionCall());
  }

  @Override
  public Object visitIfStatementStatement(
      org.daiitech.naftah.core.parser.NaftahParser.IfStatementStatementContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitIfStatementStatement(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    return visit(ctx.ifStatement());
  }

  @Override
  public Object visitReturnStatementStatement(
      org.daiitech.naftah.core.parser.NaftahParser.ReturnStatementStatementContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitReturnStatementStatement(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    return visit(ctx.returnStatement());
  }

  @Override
  public Object visitBlockStatement(
      org.daiitech.naftah.core.parser.NaftahParser.BlockStatementContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitBlockStatement(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    return visit(ctx.block());
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
    String variableName = ctx.ID().getText();
    DeclaredVariable declaredVariable =
            DeclaredVariable.of(
                    ctx,
                    variableName,
                    hasChild(ctx.CONSTANT()),
                    hasChild(ctx.type()) ? visit(ctx.type()) : null,
                    visit(ctx.expression())
            );
    // TODO: check if inside function to check if it matches any argument / parameter or previously declared and update if possible
    currentContext.defineVariable(variableName, declaredVariable);
    return null;
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
    DeclaredFunction declaredFunction =
            DeclaredFunction.of(ctx);
    currentContext.defineFunction(functionName, declaredFunction);
    return null;
  }

  @Override
  public Object visitParameterDeclarationList(
      org.daiitech.naftah.core.parser.NaftahParser.ParameterDeclarationListContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitArgumentDeclarationList(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    List<DeclaredParameter> args = new ArrayList<>();
    for (org.daiitech.naftah.core.parser.NaftahParser.ParameterDeclarationContext argumentDeclaration :
            ctx.parameterDeclaration()) {
      args.add((DeclaredParameter) visit(argumentDeclaration));
    }
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
    String argumentName = ctx.ID().getText();
    return DeclaredParameter.of(
            ctx,
            argumentName,
            hasChild(ctx.CONSTANT()),
            hasChild(ctx.type()) ? visit(ctx.type()) : Object.class,
            hasChild(ctx.value()) ? visit(ctx.value()) : null
    );
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
    String functionName = ctx.ID().getText();
    String functionCallId = DefaultContext.FUNCTION_CALL_ID_GENERATOR.apply(depth, functionName);
    currentContext.setFunctionCallId(functionCallId);
    List<Pair<String, Object>> args = new ArrayList<>();
    // TODO: add support to global variables as argument
    if (hasChild(ctx.argumentList()))
       args = (List<Pair<String, Object>>) visit(ctx.argumentList());

    if (currentContext.containsFunction(functionName)) {
      Object function = currentContext.getFunction(functionName, false).b;
      if (function instanceof DeclaredFunction declaredFunction) {
        prepareDeclaredFunction(this, declaredFunction);
        var finalArgs = prepareDeclaredFunctionArguments(this, declaredFunction.getParameters(), args);

        currentContext.defineFunctionParameters(declaredFunction.getParameters().stream()
                .map(parameter -> Map.entry(parameter.getName(), parameter))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)), true);

        currentContext.defineFunctionArguments(finalArgs.entrySet().stream()
                .map(argument -> Map.entry(argument.getKey(), argument.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        result = visit(declaredFunction.getBody());
      } else if (function instanceof BuiltinFunction declaredFunction) {
        throw new UnsupportedOperationException("Function %s of type: %s"
                .formatted(functionName, BuiltinFunction.class.getName()));
      } else if (function instanceof Method methodFunction) {
        throw new UnsupportedOperationException("Function %s of type: %s"
                .formatted(functionName, Method.class.getName()));
      }
    } else
      throw new RuntimeException("Function not found: " + functionName);
    currentContext.setFunctionCallId(null);
    // TODO: add support for all kind of functions using the qualifiedName
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
    List<Pair<String, Object>> args = new ArrayList<>();
    for (int i= 0; i <ctx.expression().size(); i++) {
      String name = hasChild(ctx.ID(i)) ? ctx.ID(i).getText() : null;
      Object value = visit(ctx.expression(i));
      args.add(new Pair<>(name, value));  // Evaluate each expression in the argument list
    }
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
    Object condition = visit(ctx.expression(0));  // Evaluate the condition expression
    if (isTruthy(condition)) {
      visit(ctx.block(0));  // If the condition is true, execute the 'then' block
    } else {
      // Iterate through elseif blocks
      for (int i = 0; i < ctx.ELSEIF().size(); i++) {
        Object elseifCondition = visit(ctx.expression(i));  // Evaluate elseif condition
        if (isTruthy(elseifCondition)) {
          visit(ctx.block(i + 1));  // Execute the corresponding elseif block if condition is true
          return null;
        }
      }

      // If no elseif was true, execute the else block (if it exists)
      if (hasChild(ctx.ELSE())) {
        visit(ctx.block(ctx.ELSEIF().size() + 1));  // Execute the 'else' block if present
      }
    }
    return null;
  }

  @Override
  public Object visitReturnStatement(
      org.daiitech.naftah.core.parser.NaftahParser.ReturnStatementContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitReturnStatement(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    if (hasChild(ctx.expression())) {
      return visit(ctx.expression());  // Evaluate and return the result
    }
    return null;  // No expression after 'return' means returning null
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
            hasChildOfType(ctx.statement(), org.daiitech.naftah.core.parser.NaftahParser.FunctionCallStatementContext.class)
                    || hasChildOfType(ctx.statement(), org.daiitech.naftah.core.parser.NaftahParser.FunctionCallExpressionContext.class) ?
                    DefaultContext.registerContext(currentContext, new HashMap<>(), new HashMap<>()) :
                    DefaultContext.registerContext(currentContext);
    depth = nextContext.getDepth();
    for (org.daiitech.naftah.core.parser.NaftahParser.StatementContext statement : ctx.statement()) {
      visit(statement);  // Visit each statement in the block
    }
    DefaultContext.deregisterContext(depth);
    depth--;
    return null;
  }

  @Override
  public Object visitValueExpression(
      org.daiitech.naftah.core.parser.NaftahParser.ValueExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitValueExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    return visit(ctx.value());
  }

  @Override
  public Object visitMinusExpression(
      org.daiitech.naftah.core.parser.NaftahParser.MinusExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitMinusExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    Object left = visit(ctx.expression(0)); // Left operand
    Object right = visit(ctx.expression(1)); // Right operand
    return NumberUtils.subtract(left, right);
  }

  @Override
  public Object visitParenthesisExpression(
      org.daiitech.naftah.core.parser.NaftahParser.ParenthesisExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitParenthesisExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    return visit(ctx.expression());
  }

  @Override
  public Object visitModExpression(
      org.daiitech.naftah.core.parser.NaftahParser.ModExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitModExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    Object left = visit(ctx.expression(0)); // Left operand
    Object right = visit(ctx.expression(1)); // Right operand
    return NumberUtils.modulo(left, right);
  }

  @Override
  public Object visitDivExpression(
      org.daiitech.naftah.core.parser.NaftahParser.DivExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitDivExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    Object left = visit(ctx.expression(0)); // Left operand
    Object right = visit(ctx.expression(1)); // Right operand
    return NumberUtils.divide(left, right);
  }

  @Override
  public Object visitGreaterThanExpression(
      org.daiitech.naftah.core.parser.NaftahParser.GreaterThanExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitGreaterThanExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    Object left = visit(ctx.expression(0)); // Left operand
    Object right = visit(ctx.expression(1)); // Right operand
    return NumberUtils.compare(left, right) > 0;
  }

  @Override
  public Object visitLessThanEqualsExpression(
      org.daiitech.naftah.core.parser.NaftahParser.LessThanEqualsExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitLessThanEqualsExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    Object left = visit(ctx.expression(0)); // Left operand
    Object right = visit(ctx.expression(1)); // Right operand
    return NumberUtils.compare(left, right) <= 0;
  }

  @Override
  public Object visitGreaterThanEqualsExpression(
      org.daiitech.naftah.core.parser.NaftahParser.GreaterThanEqualsExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitGreaterThanEqualsExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    Object left = visit(ctx.expression(0)); // Left operand
    Object right = visit(ctx.expression(1)); // Right operand
    // a negative integer if {@code x < y}; zero if {@code x == y}; a positive integer if {@code x >
    // y}
    return NumberUtils.compare(left, right) >= 0;
  }

  @Override
  public Object visitNotEqualsExpression(
      org.daiitech.naftah.core.parser.NaftahParser.NotEqualsExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitNotEqualsExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    Object left = visit(ctx.expression(0)); // Left operand
    Object right = visit(ctx.expression(1)); // Right operand
    // a negative integer if {@code x < y}; zero if {@code x == y}; a positive integer if {@code x >
    // y}
    return NumberUtils.compare(left, right) != 0;
  }

  @Override
  public Object visitEqualsExpression(
      org.daiitech.naftah.core.parser.NaftahParser.EqualsExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitEqualsExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    Object left = visit(ctx.expression(0)); // Left operand
    Object right = visit(ctx.expression(1)); // Right operand
    // a negative integer if {@code x < y}; zero if {@code x == y}; a positive integer if {@code x >
    // y}
    return NumberUtils.equals(left, right);
  }

  @Override
  public Object visitLessThanExpression(
      org.daiitech.naftah.core.parser.NaftahParser.LessThanExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitLessThanExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    Object left = visit(ctx.expression(0)); // Left operand
    Object right = visit(ctx.expression(1)); // Right operand
    // a negative integer if {@code x < y}; zero if {@code x == y}; a positive integer if {@code x >
    // y}
    return NumberUtils.compare(left, right) < 0;
  }

  @Override
  public Object visitFunctionCallExpression(org.daiitech.naftah.core.parser.NaftahParser.FunctionCallExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
              "visitFunctionCallExpression(%s)"
                      .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    return visit(ctx.functionCall());
  }

  @Override
  public Object visitPlusExpression(
      org.daiitech.naftah.core.parser.NaftahParser.PlusExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitPlusExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    Object left = visit(ctx.expression(0)); // Left operand
    Object right = visit(ctx.expression(1)); // Right operand
    return NumberUtils.add(left, right);
  }

  @Override
  public Object visitMulExpression(
      org.daiitech.naftah.core.parser.NaftahParser.MulExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitMulExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    Object left = visit(ctx.expression(0)); // Left operand
    Object right = visit(ctx.expression(1)); // Right operand
    return NumberUtils.multiply(left, right);
  }

  @Override
  public Object visitNumberValue(
      org.daiitech.naftah.core.parser.NaftahParser.NumberValueContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitNumberValue(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    Object value = ctx.NUMBER().getText();
    return NumberUtils.parseDynamicNumber(value);
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
    return StringInterpolator.process(value, currentContext);
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
    return VARIABLE_GETTER.apply(id, currentContext);
  }

  @Override
  public Object visitVoidReturnType(
      org.daiitech.naftah.core.parser.NaftahParser.VoidReturnTypeContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitVoidReturnType(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    return ctx.VOID().getText();
  }

  @Override
  public Object visitTypeReturnType(
      org.daiitech.naftah.core.parser.NaftahParser.TypeReturnTypeContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitTypeReturnType(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    return visit(ctx.type());
  }

  @Override
  public Object visitVarType(org.daiitech.naftah.core.parser.NaftahParser.VarTypeContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitVarType(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    return ctx.VAR().getText();
  }

  @Override
  public Object visitBuiltInType(
      org.daiitech.naftah.core.parser.NaftahParser.BuiltInTypeContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitBuiltInType(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    // TODO: map types to java types
    return ctx.BuiltInType().getText();
  }

  @Override
  public Object visitQualifiedNameType(
      org.daiitech.naftah.core.parser.NaftahParser.QualifiedNameTypeContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitQualifiedNameType(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    // TODO: think about using id to variable or necessary other elements
    return visit(ctx.qualifiedName());
  }

  @Override
  public Object visitQualifiedName(
      org.daiitech.naftah.core.parser.NaftahParser.QualifiedNameContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitQualifiedName(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    AtomicReference<StringBuffer> result = new AtomicReference<>(new StringBuffer());

    for (int i= 0; i <ctx.ID().size(); i++) {
      result.get().append(ctx.ID(i));
      if (i != ctx.ID().size() -1) // if not the last
        result.get().append(ctx.ID());
    }
    return result.get().toString();
  }

  @Override
  public Object visitBitwiseXorExpression(
      org.daiitech.naftah.core.parser.NaftahParser.BitwiseXorExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitBitwiseXorExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    Object left = visit(ctx.expression(0)); // Left operand
    Object right = visit(ctx.expression(1)); // Right operand
    return NumberUtils.xor(left, right);
  }

  @Override
  public Object visitNotExpression(
      org.daiitech.naftah.core.parser.NaftahParser.NotExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitNotExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    return not(visit(ctx.expression()));
  }

  @Override
  public Object visitPreDecrementExpression(
      org.daiitech.naftah.core.parser.NaftahParser.PreDecrementExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitPreDecrementExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    Object value = visit(ctx.expression());
    return NumberUtils.PreDecrement(value);
  }

  @Override
  public Object visitPostDecrementExpression(
      org.daiitech.naftah.core.parser.NaftahParser.PostDecrementExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitPostDecrementExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    Object value = visit(ctx.expression());
    return NumberUtils.PreDecrement(value);
  }

  @Override
  public Object visitBitwiseOrExpression(
      org.daiitech.naftah.core.parser.NaftahParser.BitwiseOrExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitBitwiseOrExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    Object left = visit(ctx.expression(0)); // Left operand
    Object right = visit(ctx.expression(1)); // Right operand
    return NumberUtils.or(left, right);
  }

  @Override
  public Object visitBitwiseNotExpression(
      org.daiitech.naftah.core.parser.NaftahParser.BitwiseNotExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitBitwiseNotExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    Object value = visit(ctx.expression());
    return NumberUtils.not(value);
  }

  @Override
  public Object visitBitwiseAndExpression(
      org.daiitech.naftah.core.parser.NaftahParser.BitwiseAndExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitBitwiseAndExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    Object left = visit(ctx.expression(0)); // Left operand
    Object right = visit(ctx.expression(1)); // Right operand
    return NumberUtils.and(left, right);
  }

  @Override
  public Object visitPreIncrementExpression(
      org.daiitech.naftah.core.parser.NaftahParser.PreIncrementExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitPreIncrementExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    Object value = visit(ctx.expression());
    return NumberUtils.PreIncrement(value);
  }

  @Override
  public Object visitPostIncrementExpression(
      org.daiitech.naftah.core.parser.NaftahParser.PostIncrementExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitPostIncrementExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    Object value = visit(ctx.expression());
    return NumberUtils.PostIncrement(value);
  }
}
