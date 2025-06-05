package org.daiitech.naftah.utils;

import org.antlr.v4.runtime.ParserRuleContext;
import org.daiitech.naftah.core.parser.NaftahParser;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Chakib Daii
 **/
public final class NaftahExecutionLogger {
    private static final Logger LOGGER = Logger.getLogger("DefaultNaftahParserVisitor");
    public NaftahExecutionLogger() {
        throw new IllegalStateException("Illegal usage.");
    }

    private static <T extends ParserRuleContext> String join(Collection<T> collection) {
        return collection.stream()
                .map(o -> logExecution(false, o))
                .collect(Collectors.joining("\n"));
    }

    public static <T extends ParserRuleContext> String logExecution( T ctx){
        return logExecution(true, ctx);
    }

    public static <T extends ParserRuleContext> String logExecution(boolean doLog, T ctx){
        String result = null;
        if (ctx instanceof NaftahParser.ProgramContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.BlockStatementContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.IfStatementStatementContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.AssignmentStatementContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.FunctionDeclarationStatementContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.FunctionCallStatementContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.ReturnStatementStatementContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.AssignmentContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.FunctionDeclarationContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.ArgumentDeclarationListContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.ArgumentDeclarationContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.FunctionCallContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.ArgumentListContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.IfStatementContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.ReturnStatementContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.BlockContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.ValueExpressionContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.MinusExpressionContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.ParenthesisExpressionContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.ModExpressionContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.DivExpressionContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.GreaterThanExpressionContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.LessThanEqualsExpressionContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.GreaterThanEqualsExpressionContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.NotEqualsExpressionContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.EqualsExpressionContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.LessThanExpressionContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.PlusExpressionContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.MulExpressionContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.NumberValueContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.StringValueContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.IdValueContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.VoidReturnTypeContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.TypeReturnTypeContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.VarTypeContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.BuiltInTypeContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.QualifiedNameTypeContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.QualifiedNameContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.BitwiseXorExpressionContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.NotExpressionContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.PreDecrementExpressionContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.PostDecrementExpressionContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.BitwiseOrExpressionContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.BitwiseNotExpressionContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.BitwiseAndExpressionContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.PreIncrementExpressionContext context)
            result=logExecution(doLog, context);
        else if (ctx instanceof NaftahParser.PostIncrementExpressionContext context)
            result=logExecution(doLog, context);
        return result;
    }
    public static String logExecution(boolean doLog, NaftahParser.ProgramContext ctx) {
        String result = """
            ProgramContext::statement -> {
                %s
            }
            """.formatted(
                    join(ctx.statement()));
      if (doLog && LOGGER.isLoggable(Level.FINEST)) {
          LOGGER.finest(result);
      }
      return result;
    }

    public static String logExecution(boolean doLog,
                                    NaftahParser.AssignmentStatementContext ctx) {
        String result ="""
            AssignmentStatementContext::assignment -> {
                %s
            }
            """.formatted(
                logExecution(false, ctx.assignment())
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }


    public static String logExecution(boolean doLog,
            NaftahParser.FunctionDeclarationStatementContext ctx) {
        String result = """
            FunctionDeclarationStatementContext::functionDeclaration: -> {
                %s
            }
            """.formatted(
                logExecution(false, ctx.functionDeclaration())
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }


    public static String logExecution(boolean doLog,
            NaftahParser.FunctionCallStatementContext ctx) {
        String result = """
            FunctionCallStatementContext::functionCall -> {
                %s
            }
            """.formatted(
                logExecution(false, ctx.functionCall())
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }


    public static String logExecution(boolean doLog,
            NaftahParser.IfStatementStatementContext ctx) {
        String result = """
            IfStatementStatementContext::ifStatement -> {
                %s
            }
            """.formatted(
                logExecution(false, ctx.ifStatement())
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }


    public static String logExecution(boolean doLog,
            NaftahParser.ReturnStatementStatementContext ctx) {
        String result = """
            ReturnStatementStatementContext::returnStatement -> {
                %s
            }
            """.formatted(
                logExecution(false, ctx.returnStatement())
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }


    public static String logExecution(boolean doLog,
            NaftahParser.BlockStatementContext ctx) {
        String result = """
            BlockStatementContext::block -> {
                %s
            }
            """.formatted(
                logExecution(false, ctx.block())
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }


    public static String logExecution(boolean doLog,
            NaftahParser.AssignmentContext ctx) {
        String result = """
            AssignmentContext::VARIABLE -> %s
            AssignmentContext::CONSTANT -> %s
            AssignmentContext::ID -> %s
            AssignmentContext::COLON -> %s
            AssignmentContext::type -> {
                %s
            }
            AssignmentContext::ASSIGN -> %s
            AssignmentContext::expression -> {
                %s
            }
            """.formatted(
                ctx.VARIABLE(),
                ctx.CONSTANT(),
                ctx.ID(),
                ctx.COLON(),
                logExecution(false, ctx.type()),
                ctx.ASSIGN(),
                logExecution(false, ctx.expression())
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }


    public static String logExecution(boolean doLog,
            NaftahParser.FunctionDeclarationContext ctx) {
        String result = """
            FunctionDeclarationContext::FUNCTION -> %s
            FunctionDeclarationContext::ID -> %s
            FunctionDeclarationContext::LPAREN -> %s
            FunctionDeclarationContext::argumentDeclarationList -> {
                %s
            }
            FunctionDeclarationContext::RPAREN -> %s
            FunctionDeclarationContext::COLON -> %s
            FunctionDeclarationContext::returnType -> {
                %s
            }
            FunctionDeclarationContext::block -> {
                %s
            }
            """.formatted(
                ctx.FUNCTION(),
                ctx.ID(),
                ctx.LPAREN(),
                logExecution(false, ctx.argumentDeclarationList()),
                ctx.RPAREN(),
                ctx.COLON(),
                logExecution(false, ctx.returnType()),
                logExecution(false, ctx.block())
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;

    }


    public static String logExecution(boolean doLog,
            NaftahParser.ArgumentDeclarationListContext ctx) {
        String result = """
            ArgumentDeclarationListContext::COMMA -> %s
            ArgumentDeclarationListContext::argumentDeclaration -> {
                %s
            }
            """.formatted(
                ctx.COMMA(),
                join(ctx.argumentDeclaration())
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }


    public static String logExecution(boolean doLog,
            NaftahParser.ArgumentDeclarationContext ctx) {
        String result = """
            ArgumentDeclarationContext::ID -> %s
            ArgumentDeclarationContext::COLON -> %s
            ArgumentDeclarationContext::type -> {
                %s
            }
            ArgumentDeclarationContext::ASSIGN -> %s
            ArgumentDeclarationContext::value -> {
                %s
            }
            """.formatted(
                ctx.ID(),
                ctx.COLON(),
                logExecution(false, ctx.type()),
                ctx.ASSIGN(),
                logExecution(false, ctx.value())
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }


    public static String logExecution(boolean doLog,
            NaftahParser.FunctionCallContext ctx) {
        String result = """
            FunctionCallContext::ID -> %s
            FunctionCallContext::LPAREN -> %s
            FunctionCallContext::argumentList -> {
                %s
            }
            FunctionCallContext::RPAREN -> %s
            """.formatted(
                ctx.ID(),
                ctx.LPAREN(),
                logExecution(false, ctx.argumentList()),
                ctx.RPAREN()
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }


    public static String logExecution(boolean doLog,
            NaftahParser.ArgumentListContext ctx) {
        String result = """
            ArgumentListContext::COMMA -> %s
            ArgumentListContext::expression -> {
                %s
            }
            """.formatted(
                ctx.COMMA(),
                join(ctx.expression())
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }


    public static String logExecution(boolean doLog,
            NaftahParser.IfStatementContext ctx) {
        String result = """
            IfStatementContext::IF -> %s
            IfStatementContext::THEN -> %s
            IfStatementContext::ELSEIF -> %s
            IfStatementContext::ELSE -> %s
            IfStatementContext::END -> %s
            IfStatementContext::block -> {
                %s
            }
            IfStatementContext::expression -> {
                %s
            }
            """.formatted(
                ctx.IF(),
                ctx.THEN(),
                ctx.ELSEIF(),
                ctx.ELSE(),
                ctx.END(),
                join(ctx.block()),
                join(ctx.expression())
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }


    public static String logExecution(boolean doLog,
            NaftahParser.ReturnStatementContext ctx) {
        String result = """
            ReturnStatementContext::RETURN -> %s
            ReturnStatementContext::expression -> {
                %s
            }
            """.formatted(
                ctx.RETURN(),
                ctx.expression()
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;

    }


    public static String logExecution(boolean doLog, NaftahParser.BlockContext ctx) {
        String result = """
            BlockContext::LBRACE -> %s
            BlockContext::statement -> {
                %s
            }
            BlockContext::RBRACE -> %s
            """.formatted(
                ctx.LBRACE(),
                join(ctx.statement()),
                ctx.RBRACE()
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }


    public static String logExecution(boolean doLog,
            NaftahParser.ValueExpressionContext ctx) {
        String result = """
            ValueExpressionContext::value -> {
                %s
            }
            """.formatted(
                logExecution(false, ctx.value())
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;

    }


    public static String logExecution(boolean doLog,
            NaftahParser.MinusExpressionContext ctx) {
        String result = """
            MinusExpressionContext::MINUS -> %s
            MinusExpressionContext::expression -> {
                %s
            }
            """.formatted(
                    ctx.MINUS(),
                join(ctx.expression())
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }


    public static String logExecution(boolean doLog,
            NaftahParser.ParenthesisExpressionContext ctx) {
        String result = """
                ParenthesisExpressionContext::MINUS -> %s
                ParenthesisExpressionContext::expression -> {
                    %s
                }
                ParenthesisExpressionContext::RPAREN -> %s
                """.formatted(
                ctx.LPAREN(),
                logExecution(false, ctx.expression()),
                ctx.RPAREN()
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }


    public static String logExecution(boolean doLog,
            NaftahParser.ModExpressionContext ctx) {
        String result = """
            ModExpressionContext::MOD -> %s
            ModExpressionContext::expression -> {
                %s
            }
            """.formatted(
                ctx.MOD(),
                join(ctx.expression())
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }


    public static String logExecution(boolean doLog,
            NaftahParser.DivExpressionContext ctx) {
        String result = """
            DivExpressionContext::DIV -> %s
            DivExpressionContext::expression -> {
                %s
            }
            """.formatted(
                ctx.DIV(),
                join(ctx.expression())
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }


    public static String logExecution(boolean doLog,
            NaftahParser.GreaterThanExpressionContext ctx) {
        String result = """
            GreaterThanExpressionContext::GT -> %s
            GreaterThanExpressionContext::expression -> {
                %s
            }
            """.formatted(
                ctx.GT(),
                join(ctx.expression())
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }


    public static String logExecution(boolean doLog,
            NaftahParser.LessThanEqualsExpressionContext ctx) {
        String result = """
            LessThanEqualsExpressionContext::LE -> %s
            LessThanEqualsExpressionContext::expression -> {
                %s
            }
            """.formatted(
                ctx.LE(),
                join(ctx.expression())
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }


    public static String logExecution(boolean doLog,
            NaftahParser.GreaterThanEqualsExpressionContext ctx) {
        String result = """
            GreaterThanEqualsExpressionContext::GE -> %s
            GreaterThanEqualsExpressionContext::expression -> {
                %s
            }
            """.formatted(
                ctx.GE(),
                join(ctx.expression())
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }


    public static String logExecution(boolean doLog,
            NaftahParser.NotEqualsExpressionContext ctx) {
        String result = """
            NotEqualsExpressionContext::NEQ -> %s
            NotEqualsExpressionContext::expression -> {
                %s
            }
            """.formatted(
                ctx.NEQ(),
                join(ctx.expression())
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }


    public static String logExecution(boolean doLog,
            NaftahParser.EqualsExpressionContext ctx) {
        String result = """
            EqualsExpressionContext::EQ -> %s
            EqualsExpressionContext::expression -> {
                %s
            }
            """.formatted(
                ctx.EQ(),
                join(ctx.expression())
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }


    public static String logExecution(boolean doLog,
            NaftahParser.LessThanExpressionContext ctx) {
        String result = """
            LessThanExpressionContext::LT -> %s
            LessThanExpressionContext::expression -> {
                %s
            }
            """.formatted(
                ctx.LT(),
                join(ctx.expression())
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }


    public static String logExecution(boolean doLog,
            NaftahParser.PlusExpressionContext ctx) {
        String result = """
            PlusExpressionContext::PLUS -> %s
            PlusExpressionContext::expression -> {
                %s
            }
            """.formatted(
                ctx.PLUS(),
                join(ctx.expression())
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }


    public static String logExecution(boolean doLog,
            NaftahParser.MulExpressionContext ctx) {
        String result = """
            MulExpressionContext::MUL -> %s
            MulExpressionContext::expression -> {
                %s
            }
            """.formatted(
                ctx.MUL(),
                join(ctx.expression())
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }


    public static String logExecution(boolean doLog,
            NaftahParser.NumberValueContext ctx) {
        String result = """
            NumberValueContext::NUMBER -> %s
            """.formatted(
                ctx.NUMBER()
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }


    public static String logExecution(boolean doLog,
            NaftahParser.StringValueContext ctx) {
        String result = """
            StringValueContext::STRING -> %s
            """.formatted(
                ctx.STRING()
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }


    public static String logExecution(boolean doLog, NaftahParser.IdValueContext ctx) {
        String result = """
            IdValueContext::ID -> %s
            """.formatted(
                ctx.ID()
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }


    public static String logExecution(boolean doLog,
            NaftahParser.VoidReturnTypeContext ctx) {
        String result = """
            VoidReturnTypeContext::VOID -> %s
            """.formatted(
                ctx.VOID()
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }


    public static String logExecution(boolean doLog,
            NaftahParser.TypeReturnTypeContext ctx) {
        String result = """
            VoidReturnTypeContext::type -> %s
            """.formatted(
                logExecution(false, ctx.type())
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }


    public static String logExecution(boolean doLog, NaftahParser.VarTypeContext ctx) {
        String result = """
            VarTypeContext::VAR -> %s
            """.formatted(
                    ctx.VAR()
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }


    public static String logExecution(boolean doLog,
            NaftahParser.BuiltInTypeContext ctx) {
        String result = """
            BuiltInTypeContext::BuiltInType -> %s
            """.formatted(
                ctx.BuiltInType()
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }


    public static String logExecution(boolean doLog,
            NaftahParser.QualifiedNameTypeContext ctx) {
        String result = """
            QualifiedNameTypeContext::qualifiedName -> %s
            """.formatted(
                logExecution(false, ctx.qualifiedName())
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }


    public static String logExecution(boolean doLog,
            NaftahParser.QualifiedNameContext ctx) {
        String result = """
                QualifiedNameContext::ID -> %s
                QualifiedNameContext::COLON -> %s
                """.formatted(
                ctx.ID(),
                ctx.COLON()
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }


    public static String logExecution(boolean doLog, NaftahParser.BitwiseXorExpressionContext ctx) {
        String result = """
            BitwiseXorExpressionContext::BITWISE_XOR -> %s
            BitwiseXorExpressionContext::expression -> {
                %s
            }
            """.formatted(
                ctx.BITWISE_XOR(),
                join(ctx.expression())
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }


    public static String logExecution(boolean doLog, NaftahParser.NotExpressionContext ctx) {
        String result = """
            NotExpressionContext::NOT -> %s
            NotExpressionContext::expression -> {
                %s
            }
            """.formatted(
                ctx.NOT(),
                logExecution(false, ctx.expression())
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }


    public static String logExecution(boolean doLog, NaftahParser.PreDecrementExpressionContext ctx) {
        String result = """
            PreDecrementExpressionContext::DECREMENT -> %s
            PreDecrementExpressionContext::expression -> {
                %s
            }
            """.formatted(
                ctx.DECREMENT(),
                logExecution(false, ctx.expression())
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }


    public static String logExecution(boolean doLog, NaftahParser.PostDecrementExpressionContext ctx) {
        String result = """
            PostDecrementExpressionContext::DECREMENT -> %s
            PostDecrementExpressionContext::expression -> {
                %s
            }
            """.formatted(
                ctx.DECREMENT(),
                logExecution(false, ctx.expression())
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }


    public static String logExecution(boolean doLog, NaftahParser.BitwiseOrExpressionContext ctx) {
        String result = """
            BitwiseOrExpressionContext::BITWISE_OR -> %s
            BitwiseOrExpressionContext::expression -> {
                %s
            }
            """.formatted(
                ctx.BITWISE_OR(),
                join(ctx.expression())
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }


    public static String logExecution(boolean doLog, NaftahParser.BitwiseNotExpressionContext ctx) {
        String result = """
            BitwiseXorExpressionContext::BITWISE_NOT -> %s
            BitwiseXorExpressionContext::expression -> {
                %s
            }
            """.formatted(
                ctx.BITWISE_NOT(),
                logExecution(false, ctx.expression())
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }


    public static String logExecution(boolean doLog, NaftahParser.BitwiseAndExpressionContext ctx) {
        String result = """
            BitwiseAndExpressionContext::BITWISE_AND -> %s
            BitwiseAndExpressionContext::expression -> {
                %s
            }
            """.formatted(
                ctx.BITWISE_AND(),
                join(ctx.expression())
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }


    public static String logExecution(boolean doLog, NaftahParser.PreIncrementExpressionContext ctx) {
        String result = """
            PreIncrementExpressionContext::INCREMENT -> %s
            PreIncrementExpressionContext::expression -> {
                %s
            }
            """.formatted(
                ctx.INCREMENT(),
                logExecution(false, ctx.expression())
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }


    public static String logExecution(boolean doLog, NaftahParser.PostIncrementExpressionContext ctx) {
        String result = """
            PostIncrementExpressionContext::INCREMENT -> %s
            PostIncrementExpressionContext::expression -> {
                %s
            }
            """.formatted(
                ctx.INCREMENT(),
                logExecution(false, ctx.expression())
        );
        if (doLog && LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(result);
        }
        return result;
    }
}
