package org.daiitech.naftah.parser;

import java.util.Collection;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.ParserRuleContext;
import org.daiitech.naftah.errors.NaftahBugError;

import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;

/**
 * Utility class for logging execution details within the Naftah system.
 * <p>
 * This class provides logging functionality specifically tailored to track
 * execution events, errors, and debug information during the runtime of Naftah applications.
 * </p>
 *
 * @author Chakib Daii
 */
public final class NaftahExecutionLogger {
	/**
	 * Logger instance for logging messages related to the default Naftah parser visitor.
	 * This logger is used to record execution details, warnings, and errors during parsing.
	 */
	private static final Logger LOGGER = Logger.getLogger("DefaultNaftahParserVisitor");

	/**
	 * Private constructor to prevent instantiation.
	 * Always throws a {@link NaftahBugError} when called.
	 */
	private NaftahExecutionLogger() {
		throw newNaftahBugInvalidUsageError();
	}

	/**
	 * Joins the string representations of a collection of parser contexts into a single string,
	 * separated by newlines.
	 *
	 * @param <T>        the type of the parser context extending {@link ParserRuleContext}
	 * @param collection the collection of parser contexts to join
	 * @return a single string containing the log execution outputs of each context, separated by newlines
	 */
	private static <T extends ParserRuleContext> String join(Collection<T> collection) {
		return collection.stream().map(o -> logExecution(false, o)).collect(Collectors.joining("\n"));
	}

	/**
	 * Logs the execution details of a given parser context.
	 *
	 * @param <T> the type of the parser context extending {@link ParserRuleContext}
	 * @param ctx the parser context to log
	 * @return a string representation of the execution log for the provided context
	 */
	public static <T extends ParserRuleContext> String logExecution(T ctx) {
		return logExecution(true, ctx);
	}

	/**
	 * Logs the execution details of the given parser context.
	 *
	 * @param <T>   the type of the parser context extending {@link ParserRuleContext}
	 * @param doLog whether to perform logging (true to log, false to just generate the string)
	 * @param ctx   the parser context to log
	 * @return a string representation of the execution log for the provided context
	 */
	public static <T extends ParserRuleContext> String logExecution(boolean doLog, T ctx) {
		String result = null;
		if (ctx instanceof NaftahParser.ProgramContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.BlockStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.IfStatementStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.DeclarationStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.AssignmentStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.FunctionDeclarationStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.FunctionCallStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.ExpressionStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.ReturnStatementStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.DeclarationContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.AssignmentContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.FunctionDeclarationContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.ParameterDeclarationListContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.ParameterDeclarationContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.FunctionCallContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.ArgumentListContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.IfStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.ReturnStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.BlockContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.ValueExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.MinusExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.ParenthesisExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.ModExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.DivExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.GreaterThanExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.LessThanEqualsExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.GreaterThanEqualsExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.NotEqualsExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.EqualsExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.LessThanExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.PlusExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.MulExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.NumberValueContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.CharacterValueContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.StringValueContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.TrueValueContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.FalseValueContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.NullValueContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.IdValueContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.VoidReturnTypeContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.TypeReturnTypeContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.VarTypeContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.BuiltInTypeContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.BuiltInContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.QualifiedNameTypeContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.QualifiedNameContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.QualifiedCallContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.BitwiseXorExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.NotExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.PreDecrementExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.PostDecrementExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.BitwiseOrExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.BitwiseNotExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.BitwiseAndExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.PreIncrementExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof NaftahParser.PostIncrementExpressionContext context) {
			result = logExecution(doLog, context);
		}
		return result;
	}

	public static String logExecution(boolean doLog, NaftahParser.ProgramContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							ProgramContext::statement -> {
								%s
							}
							""".formatted(join(context.statement()));
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.DeclarationStatementContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							DeclarationStatementContext::declaration -> {
								%s
							}
							""".formatted(logExecution(false, context.declaration()));
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.AssignmentStatementContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							AssignmentStatementContext::assignment -> {
								%s
							}
							""".formatted(logExecution(false, context.assignment()));
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.FunctionDeclarationStatementContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							FunctionDeclarationStatementContext::functionDeclaration: -> {
								%s
							}
							""".formatted(logExecution(false, context.functionDeclaration()));
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.FunctionCallStatementContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							FunctionCallStatementContext::functionCall -> {
								%s
							}
							""".formatted(logExecution(false, context.functionCall()));
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.IfStatementStatementContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							IfStatementStatementContext::ifStatement -> {
								%s
							}
							""".formatted(logExecution(false, context.ifStatement()));
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.ExpressionStatementContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							ExpressionStatementContext::expression -> {
								%s
							}
							""".formatted(logExecution(false, context.expression()));
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.ReturnStatementStatementContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							ReturnStatementStatementContext::returnStatement -> {
								%s
							}
							""".formatted(logExecution(false, context.returnStatement()));
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.BlockStatementContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							BlockStatementContext::block -> {
								%s
							}
							""".formatted(logExecution(false, context.block()));
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.DeclarationContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							DeclarationContext::VARIABLE -> %s
							DeclarationContext::CONSTANT -> %s
							DeclarationContext::ID -> %s
							DeclarationContext::COLON -> %s
							DeclarationContext::type -> {
								%s
							}
							"""
					.formatted( context.VARIABLE(),
								context.CONSTANT(),
								context.ID(),
								context.COLON(),
								logExecution(false, context.type()));
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.AssignmentContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							AssignmentContext::declaration -> {
								%s
							}
							AssignmentContext::ASSIGN -> %s
							AssignmentContext::expression -> {
								%s
							}
							"""
					.formatted( logExecution(false, context.declaration()),
								context.ASSIGN(),
								logExecution(false, context.expression()));
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.FunctionDeclarationContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							FunctionDeclarationContext::FUNCTION -> %s
							FunctionDeclarationContext::ID -> %s
							FunctionDeclarationContext::LPAREN -> %s
							FunctionDeclarationContext::parameterDeclarationList -> {
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
							"""
					.formatted( context.FUNCTION(),
								context.ID(),
								context.LPAREN(),
								logExecution(false, context.parameterDeclarationList()),
								context.RPAREN(),
								context.COLON(),
								logExecution(false, context.returnType()),
								logExecution(false, context.block()));
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.ParameterDeclarationListContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							ArgumentDeclarationListContext::COMMA -> %s
							ArgumentDeclarationListContext::SEMI -> %s
							ArgumentDeclarationListContext::parameterDeclaration -> {
								%s
							}
							""".formatted(context.COMMA(), context.SEMI(), join(context.parameterDeclaration()));
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.ParameterDeclarationContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							ParameterDeclarationContext::ID -> %s
							ParameterDeclarationContext::COLON -> %s
							ParameterDeclarationContext::type -> {
								%s
							}
							ParameterDeclarationContext::ASSIGN -> %s
							ParameterDeclarationContext::value -> {
								%s
							}
							"""
					.formatted( context.ID(),
								context.COLON(),
								logExecution(false, context.type()),
								context.ASSIGN(),
								logExecution(false, context.value()));
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.QualifiedCallContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							QualifiedCallContext::qualifiedName -> {
								%s
							}
							QualifiedCallContext::COLON -> %s
							QualifiedCallContext::COLON -> %s
							QualifiedCallContext::ID -> %s
							"""
					.formatted( logExecution(false, context.qualifiedName()),
								context.COLON(),
								context.COLON(),
								context.ID());
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.FunctionCallContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							FunctionCallContext::ID -> %s
							FunctionCallContext::LPAREN -> %s
							FunctionCallContext::argumentList -> {
								%s
							}
							FunctionCallContext::RPAREN -> %s
							"""
					.formatted( context.ID(),
								context.LPAREN(),
								logExecution(false, context.argumentList()),
								context.RPAREN());
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.ArgumentListContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							ArgumentListContext::COMMA -> %s
							ArgumentListContext::SEMI -> %s
							ArgumentListContext::expression -> {
								%s
							}
							""".formatted(context.COMMA(), context.SEMI(), join(context.expression()));
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.IfStatementContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
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
							"""
					.formatted( context.IF(),
								context.THEN(),
								context.ELSEIF(),
								context.ELSE(),
								context.END(),
								join(context.block()),
								join(context.expression()));
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.ReturnStatementContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							ReturnStatementContext::RETURN -> %s
							ReturnStatementContext::expression -> {
								%s
							}
							""".formatted(context.RETURN(), context.expression());
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.BlockContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							BlockContext::LBRACE -> %s
							BlockContext::statement -> {
								%s
							}
							BlockContext::RBRACE -> %s
							""".formatted(context.LBRACE(), join(context.statement()), context.RBRACE());
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.ValueExpressionContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							ValueExpressionContext::value -> {
								%s
							}
							""".formatted(logExecution(false, context.value()));
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.MinusExpressionContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							MinusExpressionContext::MINUS -> %s
							MinusExpressionContext::ELEMENTWISE_MINUS -> %s
							MinusExpressionContext::expression -> {
								%s
							}
							""".formatted(context.MINUS(), context.ELEMENTWISE_MINUS(), join(context.expression()));
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.ParenthesisExpressionContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							ParenthesisExpressionContext::MINUS -> %s
							ParenthesisExpressionContext::expression -> {
								%s
							}
							ParenthesisExpressionContext::RPAREN -> %s
							"""
					.formatted(context.LPAREN(), logExecution(false, context.expression()), context.RPAREN());
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.ModExpressionContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							ModExpressionContext::MOD -> %s
							ModExpressionContext::ELEMENTWISE_MOD -> %s
							ModExpressionContext::expression -> {
								%s
							}
							""".formatted(context.MOD(), context.ELEMENTWISE_MOD(), join(context.expression()));
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.DivExpressionContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							DivExpressionContext::DIV -> %s
							DivExpressionContext::ELEMENTWISE_DIV -> %s
							DivExpressionContext::expression -> {
								%s
							}
							""".formatted(context.DIV(), context.ELEMENTWISE_DIV(), join(context.expression()));
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.GreaterThanExpressionContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							GreaterThanExpressionContext::GT -> %s
							GreaterThanExpressionContext::expression -> {
								%s
							}
							""".formatted(context.GT(), join(context.expression()));
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.LessThanEqualsExpressionContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							LessThanEqualsExpressionContext::LE -> %s
							LessThanEqualsExpressionContext::expression -> {
								%s
							}
							""".formatted(context.LE(), join(context.expression()));
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.GreaterThanEqualsExpressionContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							GreaterThanEqualsExpressionContext::GE -> %s
							GreaterThanEqualsExpressionContext::expression -> {
								%s
							}
							""".formatted(context.GE(), join(context.expression()));
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.NotEqualsExpressionContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							NotEqualsExpressionContext::NEQ -> %s
							NotEqualsExpressionContext::expression -> {
								%s
							}
							""".formatted(context.NEQ(), join(context.expression()));
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.EqualsExpressionContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							EqualsExpressionContext::EQ -> %s
							EqualsExpressionContext::expression -> {
								%s
							}
							""".formatted(context.EQ(), join(context.expression()));
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.LessThanExpressionContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							LessThanExpressionContext::LT -> %s
							LessThanExpressionContext::expression -> {
								%s
							}
							""".formatted(context.LT(), join(context.expression()));
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.PlusExpressionContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							PlusExpressionContext::PLUS -> %s
							PlusExpressionContext::ELEMENTWISE_PLUS -> %s
							PlusExpressionContext::expression -> {
								%s
							}
							""".formatted(context.PLUS(), context.ELEMENTWISE_PLUS(), join(context.expression()));
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.MulExpressionContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							MulExpressionContext::MUL -> %s
							MulExpressionContext::ELEMENTWISE_MUL -> %s
							MulExpressionContext::expression -> {
								%s
							}
							""".formatted(context.MUL(), context.ELEMENTWISE_MUL(), join(context.expression()));
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.NumberValueContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							NumberValueContext::NUMBER -> %s
							""".formatted(context.NUMBER());
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.CharacterValueContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							CharacterValueContext::CHARACTER -> %s
							""".formatted(context.CHARACTER());
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.StringValueContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							StringValueContext::STRING -> %s
							""".formatted(context.STRING());
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.TrueValueContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							TrueValueContext::TRUE -> %s
							""".formatted(context.TRUE());
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.FalseValueContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							FalseValueContext::FALSE -> %s
							""".formatted(context.FALSE());
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.NullValueContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							NullValueContext::NULL -> %s
							""".formatted(context.NULL());
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.IdValueContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							IdValueContext::ID -> %s
							""".formatted(context.ID());
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.VoidReturnTypeContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							VoidReturnTypeContext::VOID -> %s
							""".formatted(context.VOID());
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.TypeReturnTypeContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							VoidReturnTypeContext::type -> %s
							""".formatted(logExecution(false, context.type()));
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.VarTypeContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							VarTypeContext::VAR -> %s
							""".formatted(context.VAR());
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.BuiltInTypeContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							BuiltInTypeContext::builtIn -> {
									%s
							}
							""".formatted(logExecution(false, context.builtIn()));
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.BuiltInContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							BuiltInContext::BOOLEAN -> %s
							BuiltInContext::CHAR -> %s
							BuiltInContext::BYTE -> %s
							BuiltInContext::SHORT -> %s
							BuiltInContext::INT -> %s
							BuiltInContext::LONG -> %s
							BuiltInContext::FLOAT -> %s
							BuiltInContext::DOUBLE -> %s
							BuiltInContext::STRING_TYPE -> %s
							"""
					.formatted( context.BOOLEAN(),
								context.CHAR(),
								context.BYTE(),
								context.SHORT(),
								context.INT(),
								context.LONG(),
								context.FLOAT(),
								context.DOUBLE(),
								context.STRING_TYPE());
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.QualifiedNameTypeContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							QualifiedNameTypeContext::qualifiedName -> %s
							""".formatted(logExecution(false, context.qualifiedName()));
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.QualifiedNameContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							QualifiedNameContext::ID -> %s
							QualifiedNameContext::COLON -> %s
							""".formatted(context.ID(), context.COLON());
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.BitwiseXorExpressionContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							BitwiseXorExpressionContext::BITWISE_XOR -> %s
							BitwiseXorExpressionContext::expression -> {
								%s
							}
							""".formatted(context.BITWISE_XOR(), join(context.expression()));
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.NotExpressionContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							NotExpressionContext::NOT -> %s
							NotExpressionContext::expression -> {
								%s
							}
							""".formatted(context.NOT(), logExecution(false, context.expression()));
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.PreDecrementExpressionContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							PreDecrementExpressionContext::DECREMENT -> %s
							PreDecrementExpressionContext::expression -> {
								%s
							}
							""".formatted(context.DECREMENT(), logExecution(false, context.expression()));
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.PostDecrementExpressionContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							PostDecrementExpressionContext::DECREMENT -> %s
							PostDecrementExpressionContext::expression -> {
								%s
							}
							""".formatted(context.DECREMENT(), logExecution(false, context.expression()));
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.BitwiseOrExpressionContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							BitwiseOrExpressionContext::BITWISE_OR -> %s
							BitwiseOrExpressionContext::expression -> {
								%s
							}
							""".formatted(context.BITWISE_OR(), join(context.expression()));
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.BitwiseNotExpressionContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							BitwiseXorExpressionContext::BITWISE_NOT -> %s
							BitwiseXorExpressionContext::expression -> {
								%s
							}
							""".formatted(context.BITWISE_NOT(), logExecution(false, context.expression()));
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.BitwiseAndExpressionContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							BitwiseAndExpressionContext::BITWISE_AND -> %s
							BitwiseAndExpressionContext::expression -> {
								%s
							}
							""".formatted(context.BITWISE_AND(), join(context.expression()));
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.PreIncrementExpressionContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							PreIncrementExpressionContext::INCREMENT -> %s
							PreIncrementExpressionContext::expression -> {
								%s
							}
							""".formatted(context.INCREMENT(), logExecution(false, context.expression()));
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}

	public static String logExecution(boolean doLog, NaftahParser.PostIncrementExpressionContext ctx) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = """
							PostIncrementExpressionContext::INCREMENT -> %s
							PostIncrementExpressionContext::expression -> {
								%s
							}
							""".formatted(context.INCREMENT(), logExecution(false, context.expression()));
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}
}
