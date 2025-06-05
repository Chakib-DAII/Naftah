package org.daiitech.naftah.core.parser;

import static org.daiitech.naftah.utils.NaftahExecutionLogger.logExecution;

import java.util.logging.Level;
import java.util.logging.Logger;
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

  public static final DefaultContext CONTEXT = new DefaultContext();

  @Override
  public Object visitProgram(org.daiitech.naftah.core.parser.NaftahParser.ProgramContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitProgram(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
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
    return super.visitAssignmentStatement(ctx);
  }

  @Override
  public Object visitFunctionDeclarationStatement(
      org.daiitech.naftah.core.parser.NaftahParser.FunctionDeclarationStatementContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitFunctionDeclarationStatement(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    return super.visitFunctionDeclarationStatement(ctx);
  }

  @Override
  public Object visitFunctionCallStatement(
      org.daiitech.naftah.core.parser.NaftahParser.FunctionCallStatementContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitFunctionCallStatement(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    return super.visitFunctionCallStatement(ctx);
  }

  @Override
  public Object visitIfStatementStatement(
      org.daiitech.naftah.core.parser.NaftahParser.IfStatementStatementContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitIfStatementStatement(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    return super.visitIfStatementStatement(ctx);
  }

  @Override
  public Object visitReturnStatementStatement(
      org.daiitech.naftah.core.parser.NaftahParser.ReturnStatementStatementContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitReturnStatementStatement(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    return super.visitReturnStatementStatement(ctx);
  }

  @Override
  public Object visitBlockStatement(
      org.daiitech.naftah.core.parser.NaftahParser.BlockStatementContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitBlockStatement(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    return super.visitBlockStatement(ctx);
  }

  @Override
  public Object visitAssignment(
      org.daiitech.naftah.core.parser.NaftahParser.AssignmentContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitAssignment(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    return super.visitAssignment(ctx);
  }

  @Override
  public Object visitFunctionDeclaration(
      org.daiitech.naftah.core.parser.NaftahParser.FunctionDeclarationContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitFunctionDeclaration(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    return super.visitFunctionDeclaration(ctx);
  }

  @Override
  public Object visitArgumentDeclarationList(
      org.daiitech.naftah.core.parser.NaftahParser.ArgumentDeclarationListContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitArgumentDeclarationList(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    return super.visitArgumentDeclarationList(ctx);
  }

  @Override
  public Object visitArgumentDeclaration(
      org.daiitech.naftah.core.parser.NaftahParser.ArgumentDeclarationContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitArgumentDeclaration(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    return super.visitArgumentDeclaration(ctx);
  }

  @Override
  public Object visitFunctionCall(
      org.daiitech.naftah.core.parser.NaftahParser.FunctionCallContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitFunctionCall(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    return super.visitFunctionCall(ctx);
  }

  @Override
  public Object visitArgumentList(
      org.daiitech.naftah.core.parser.NaftahParser.ArgumentListContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitArgumentList(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    return super.visitArgumentList(ctx);
  }

  @Override
  public Object visitIfStatement(
      org.daiitech.naftah.core.parser.NaftahParser.IfStatementContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitIfStatement(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    return super.visitIfStatement(ctx);
  }

  @Override
  public Object visitReturnStatement(
      org.daiitech.naftah.core.parser.NaftahParser.ReturnStatementContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitReturnStatement(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    return super.visitReturnStatement(ctx);
  }

  @Override
  public Object visitBlock(org.daiitech.naftah.core.parser.NaftahParser.BlockContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitBlock(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    return super.visitBlock(ctx);
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
    String value = ctx.STRING().getText();
    return StringInterpolator.process(value, CONTEXT);
  }

  @Override
  public Object visitIdValue(org.daiitech.naftah.core.parser.NaftahParser.IdValueContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitIdValue(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    return super.visitIdValue(ctx);
  }

  @Override
  public Object visitVoidReturnType(
      org.daiitech.naftah.core.parser.NaftahParser.VoidReturnTypeContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitVoidReturnType(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    return super.visitVoidReturnType(ctx);
  }

  @Override
  public Object visitTypeReturnType(
      org.daiitech.naftah.core.parser.NaftahParser.TypeReturnTypeContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitTypeReturnType(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    return super.visitTypeReturnType(ctx);
  }

  @Override
  public Object visitVarType(org.daiitech.naftah.core.parser.NaftahParser.VarTypeContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitVarType(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    return super.visitVarType(ctx);
  }

  @Override
  public Object visitBuiltInType(
      org.daiitech.naftah.core.parser.NaftahParser.BuiltInTypeContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitBuiltInType(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    return super.visitBuiltInType(ctx);
  }

  @Override
  public Object visitQualifiedNameType(
      org.daiitech.naftah.core.parser.NaftahParser.QualifiedNameTypeContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitQualifiedNameType(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    return super.visitQualifiedNameType(ctx);
  }

  @Override
  public Object visitQualifiedName(
      org.daiitech.naftah.core.parser.NaftahParser.QualifiedNameContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitQualifiedName(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    logExecution(ctx);
    return super.visitQualifiedName(ctx);
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
    /**
     * maybe supporting it in this way can be interesting | Value | `!value` (logical) | `-value`
     * (arithmetic) | | ----------- | ------------------ | --------------------- | | `true` |
     * `false` | `-1` | | `false` | `true` | `0` | | `"123"` | `false` | `-123` | | `""` | `true` |
     * `-0` | | `123` | `false` | `-123` | | `null` | `true` | `0` | | `undefined` | `true` | `NaN`
     * | | `[]` | `false` | `-0` | | `{}` | `false` | `NaN` |
     */
    return super.visitNotExpression(ctx);
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
