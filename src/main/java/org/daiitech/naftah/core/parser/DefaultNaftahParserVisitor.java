package org.daiitech.naftah.core.parser;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Chakib Daii
 */
public class DefaultNaftahParserVisitor
    extends org.daiitech.naftah.core.parser.NaftahParserBaseVisitor<Object> {

  private static final Logger LOGGER = Logger.getLogger("DefaultNaftahParserVisitor");
  public static final String FORMATTER = "index: %s, text: %s, payload: %s";

  @Override
  public Object visitProgram(org.daiitech.naftah.core.parser.NaftahParser.ProgramContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitProgram(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
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
    return super.visitAssignmentStatement(ctx);
  }

  @Override
  public Object visitFunctionDeclarationStatement(
      org.daiitech.naftah.core.parser.NaftahParser.FunctionDeclarationStatementContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitFunctionDeclarationStatement(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitFunctionDeclarationStatement(ctx);
  }

  @Override
  public Object visitFunctionCallStatement(
      org.daiitech.naftah.core.parser.NaftahParser.FunctionCallStatementContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitFunctionCallStatement(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitFunctionCallStatement(ctx);
  }

  @Override
  public Object visitIfStatementStatement(
      org.daiitech.naftah.core.parser.NaftahParser.IfStatementStatementContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitIfStatementStatement(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitIfStatementStatement(ctx);
  }

  @Override
  public Object visitReturnStatementStatement(
      org.daiitech.naftah.core.parser.NaftahParser.ReturnStatementStatementContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitReturnStatementStatement(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitReturnStatementStatement(ctx);
  }

  @Override
  public Object visitBlockStatement(
      org.daiitech.naftah.core.parser.NaftahParser.BlockStatementContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitBlockStatement(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitBlockStatement(ctx);
  }

  @Override
  public Object visitAssignment(
      org.daiitech.naftah.core.parser.NaftahParser.AssignmentContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitAssignment(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitAssignment(ctx);
  }

  @Override
  public Object visitFunctionDeclaration(
      org.daiitech.naftah.core.parser.NaftahParser.FunctionDeclarationContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitFunctionDeclaration(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitFunctionDeclaration(ctx);
  }

  @Override
  public Object visitArgumentDeclarationList(
      org.daiitech.naftah.core.parser.NaftahParser.ArgumentDeclarationListContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitArgumentDeclarationList(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitArgumentDeclarationList(ctx);
  }

  @Override
  public Object visitArgumentDeclaration(
      org.daiitech.naftah.core.parser.NaftahParser.ArgumentDeclarationContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitArgumentDeclaration(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitArgumentDeclaration(ctx);
  }

  @Override
  public Object visitFunctionCall(
      org.daiitech.naftah.core.parser.NaftahParser.FunctionCallContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitFunctionCall(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitFunctionCall(ctx);
  }

  @Override
  public Object visitArgumentList(
      org.daiitech.naftah.core.parser.NaftahParser.ArgumentListContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitArgumentList(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitArgumentList(ctx);
  }

  @Override
  public Object visitIfStatement(
      org.daiitech.naftah.core.parser.NaftahParser.IfStatementContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitIfStatement(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitIfStatement(ctx);
  }

  @Override
  public Object visitReturnStatement(
      org.daiitech.naftah.core.parser.NaftahParser.ReturnStatementContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitReturnStatement(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitReturnStatement(ctx);
  }

  @Override
  public Object visitBlock(org.daiitech.naftah.core.parser.NaftahParser.BlockContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitBlock(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitBlock(ctx);
  }

  @Override
  public Object visitValueExpression(
      org.daiitech.naftah.core.parser.NaftahParser.ValueExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitValueExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitValueExpression(ctx);
  }

  @Override
  public Object visitMinusExpression(
      org.daiitech.naftah.core.parser.NaftahParser.MinusExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitMinusExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitMinusExpression(ctx);
  }

  @Override
  public Object visitParenthesisExpression(
      org.daiitech.naftah.core.parser.NaftahParser.ParenthesisExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitParenthesisExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitParenthesisExpression(ctx);
  }

  @Override
  public Object visitModExpression(
      org.daiitech.naftah.core.parser.NaftahParser.ModExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitModExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitModExpression(ctx);
  }

  @Override
  public Object visitDivExpression(
      org.daiitech.naftah.core.parser.NaftahParser.DivExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitDivExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitDivExpression(ctx);
  }

  @Override
  public Object visitGreaterThanExpression(
      org.daiitech.naftah.core.parser.NaftahParser.GreaterThanExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitGreaterThanExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitGreaterThanExpression(ctx);
  }

  @Override
  public Object visitLessThanEqualsExpression(
      org.daiitech.naftah.core.parser.NaftahParser.LessThanEqualsExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitLessThanEqualsExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitLessThanEqualsExpression(ctx);
  }

  @Override
  public Object visitGreaterThanEqualsExpression(
      org.daiitech.naftah.core.parser.NaftahParser.GreaterThanEqualsExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitGreaterThanEqualsExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitGreaterThanEqualsExpression(ctx);
  }

  @Override
  public Object visitNotEqualsExpression(
      org.daiitech.naftah.core.parser.NaftahParser.NotEqualsExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitNotEqualsExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitNotEqualsExpression(ctx);
  }

  @Override
  public Object visitEqualsExpression(
      org.daiitech.naftah.core.parser.NaftahParser.EqualsExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitEqualsExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitEqualsExpression(ctx);
  }

  @Override
  public Object visitLessThanExpression(
      org.daiitech.naftah.core.parser.NaftahParser.LessThanExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitLessThanExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitLessThanExpression(ctx);
  }

  @Override
  public Object visitPlusExpression(
      org.daiitech.naftah.core.parser.NaftahParser.PlusExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitPlusExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitPlusExpression(ctx);
  }

  @Override
  public Object visitMulExpression(
      org.daiitech.naftah.core.parser.NaftahParser.MulExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitMulExpression(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitMulExpression(ctx);
  }

  @Override
  public Object visitNumberValue(
      org.daiitech.naftah.core.parser.NaftahParser.NumberValueContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitNumberValue(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitNumberValue(ctx);
  }

  @Override
  public Object visitStringValue(
      org.daiitech.naftah.core.parser.NaftahParser.StringValueContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitStringValue(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitStringValue(ctx);
  }

  @Override
  public Object visitIdValue(org.daiitech.naftah.core.parser.NaftahParser.IdValueContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitIdValue(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitIdValue(ctx);
  }

  @Override
  public Object visitVoidReturnType(
      org.daiitech.naftah.core.parser.NaftahParser.VoidReturnTypeContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitVoidReturnType(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitVoidReturnType(ctx);
  }

  @Override
  public Object visitTypeReturnType(
      org.daiitech.naftah.core.parser.NaftahParser.TypeReturnTypeContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitTypeReturnType(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitTypeReturnType(ctx);
  }

  @Override
  public Object visitVarType(org.daiitech.naftah.core.parser.NaftahParser.VarTypeContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitVarType(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitVarType(ctx);
  }

  @Override
  public Object visitBuiltInType(
      org.daiitech.naftah.core.parser.NaftahParser.BuiltInTypeContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitBuiltInType(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitBuiltInType(ctx);
  }

  @Override
  public Object visitQualifiedNameType(
      org.daiitech.naftah.core.parser.NaftahParser.QualifiedNameTypeContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitQualifiedNameType(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitQualifiedNameType(ctx);
  }

  @Override
  public Object visitQualifiedName(
      org.daiitech.naftah.core.parser.NaftahParser.QualifiedNameContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
          "visitQualifiedName(%s)"
              .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitQualifiedName(ctx);
  }

  @Override
  public Object visitBitwiseXorExpression(org.daiitech.naftah.core.parser.NaftahParser.BitwiseXorExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
              "visitBitwiseXorExpression(%s)"
                      .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitBitwiseXorExpression(ctx);
  }

  @Override
  public Object visitNotExpression(org.daiitech.naftah.core.parser.NaftahParser.NotExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
              "visitNotExpression(%s)"
                      .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitNotExpression(ctx);
  }

  @Override
  public Object visitPreDecrementExpression(org.daiitech.naftah.core.parser.NaftahParser.PreDecrementExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
              "visitPreDecrementExpression(%s)"
                      .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitPreDecrementExpression(ctx);
  }

  @Override
  public Object visitPostDecrementExpression(org.daiitech.naftah.core.parser.NaftahParser.PostDecrementExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
              "visitPostDecrementExpression(%s)"
                      .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitPostDecrementExpression(ctx);
  }

  @Override
  public Object visitBitwiseOrExpression(org.daiitech.naftah.core.parser.NaftahParser.BitwiseOrExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
              "visitBitwiseOrExpression(%s)"
                      .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitBitwiseOrExpression(ctx);
  }

  @Override
  public Object visitBitwiseNotExpression(org.daiitech.naftah.core.parser.NaftahParser.BitwiseNotExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
              "visitBitwiseNotExpression(%s)"
                      .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitBitwiseNotExpression(ctx);
  }

  @Override
  public Object visitBitwiseAndExpression(org.daiitech.naftah.core.parser.NaftahParser.BitwiseAndExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
              "visitBitwiseAndExpression(%s)"
                      .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitBitwiseAndExpression(ctx);
  }

  @Override
  public Object visitPreIncrementExpression(org.daiitech.naftah.core.parser.NaftahParser.PreIncrementExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
              "visitPreIncrementExpression(%s)"
                      .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitPreIncrementExpression(ctx);
  }

  @Override
  public Object visitPostIncrementExpression(org.daiitech.naftah.core.parser.NaftahParser.PostIncrementExpressionContext ctx) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine(
              "visitPostIncrementExpression(%s)"
                      .formatted(FORMATTER.formatted(ctx.getRuleIndex(), ctx.getText(), ctx.getPayload())));
    return super.visitPostIncrementExpression(ctx);
  }
}
