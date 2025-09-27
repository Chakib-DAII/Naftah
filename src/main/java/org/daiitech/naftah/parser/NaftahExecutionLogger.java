package org.daiitech.naftah.parser;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
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
	private static <T extends ParseTree> String join(Collection<T> collection) {
		return collection.stream().filter(Objects::nonNull).map(ParseTree::getText).collect(Collectors.joining("\n"));
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

		if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.ProgramContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.BlockStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.IfStatementStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.ForStatementStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.WhileStatementStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.RepeatStatementStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.CaseStatementStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.FunctionDeclarationStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.FunctionCallStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.ObjectAccessStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.ObjectAccessContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.QualifiedObjectAccessContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.DeclarationStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.AssignmentStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.ReturnStatementStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.BreakStatementStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.ContinueStatementStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.ExpressionStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.DeclarationContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.AssignmentContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.FunctionDeclarationContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.ParameterDeclarationListContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.ParameterDeclarationContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.FunctionCallContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.ArgumentListContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.IfStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.IndexBasedForLoopStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.ForEachLoopStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.ValueForeachTargetContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.IndexAndValueForeachTargetContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.KeyValueForeachTargetContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.IndexAndKeyValueForeachTargetContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.WhileStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.RepeatStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.CaseStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.CaseLabelListContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.BreakStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.ContinueStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.ReturnStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.BlockContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.ExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.LogicalExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.BitwiseExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.EqualityExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.RelationalExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.AdditiveExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.MultiplicativeExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.PrefixUnaryExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.PostfixUnaryExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.PostfixExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.FunctionCallExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.ObjectExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.CollectionExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.CollectionAccessStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.CollectionAccessExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.CollectionAccessContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.ObjectAccessExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.ValueExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.ParenthesisExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.ObjectContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.ObjectFieldsContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.ListValueContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.TupleValueContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.SetValueContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.MapValueContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.SingleElementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.MultipleElementsContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.KeyValuePairsContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.KeyValueContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.NumberValueContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.RadixNumberValueContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.TrueValueContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.FalseValueContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.NullValueContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.CharacterValueContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.StringValueContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.NanValueContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.IdValueContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.VoidReturnTypeContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.TypeReturnTypeContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.VarTypeContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.BuiltInTypeContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.QualifiedNameTypeContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.BuiltInContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.QualifiedNameContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.TernaryExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.NullishExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.QualifiedCallContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.LabelContext context) {
			result = logExecution(doLog, context);
		}
		return result;
	}

	public static String logExecution(boolean doLog, org.daiitech.naftah.parser.NaftahParser.ProgramContext ctx) {
		return doLogExecution(doLog, ctx, context -> """
														ProgramContext::statement -> {
															%s
														}
														""".formatted(join(context.statement())));
	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.ObjectAccessStatementContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											ObjectAccessStatementContext::objectAccess -> {
												%s
											}
											"""
										.formatted(Objects.nonNull(context.objectAccess()) ?
												context.objectAccess().getText() :
												null));

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.ObjectAccessContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											ObjectAccessContext::qualifiedName -> %s
											ObjectAccessContext::qualifiedObjectAccess -> %s
											"""
										.formatted( Objects.nonNull(context.qualifiedName()) ?
															context.qualifiedName().getText() :
															null,
													Objects.nonNull(context.qualifiedObjectAccess()) ?
															context.qualifiedObjectAccess().getText() :
															null));

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.QualifiedObjectAccessContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											QualifiedObjectAccessContext::ID -> %s
											QualifiedObjectAccessContext::QUESTION -> %s
											QualifiedObjectAccessContext::COLON -> %s
											QualifiedObjectAccessContext::LBRACK -> %s
											QualifiedObjectAccessContext::RBRACK -> %s
											"""
										.formatted( join(context.ID()),
													join(context.QUESTION()),
													join(context.COLON()),
													join(context.LBRACK()),
													join(context.RBRACK())));

	}


	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.DeclarationStatementContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											DeclarationStatementContext::declaration -> {
												%s
											}
											"""
										.formatted(Objects.nonNull(context.declaration()) ?
												context.declaration().getText() :
												null));

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.AssignmentStatementContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											AssignmentStatementContext::assignment -> {
												%s
											}
											"""
										.formatted(Objects.nonNull(context.assignment()) ?
												context.assignment().getText() :
												null));

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.FunctionDeclarationStatementContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											FunctionDeclarationStatementContext::functionDeclaration: -> {
												%s
											}
											"""
										.formatted(Objects.nonNull(context.functionDeclaration()) ?
												context.functionDeclaration().getText() :
												null));

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.FunctionCallStatementContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											FunctionCallStatementContext::functionCall -> {
												%s
											}
											"""
										.formatted(Objects.nonNull(context.functionCall()) ?
												context.functionCall().getText() :
												null));

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.IfStatementStatementContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											IfStatementStatementContext::ifStatement -> {
												%s
											}
											"""
										.formatted(Objects.nonNull(context.ifStatement()) ?
												context.ifStatement().getText() :
												null));

	}


	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.ForStatementStatementContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											ForStatementStatementContext::forStatement -> {
												%s
											}
											"""
										.formatted(Objects.nonNull(context.forStatement()) ?
												context.forStatement().getText() :
												null));

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.WhileStatementStatementContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											WhileStatementStatementContext::whileStatement -> {
												%s
											}
											"""
										.formatted(Objects.nonNull(context.whileStatement()) ?
												context.whileStatement().getText() :
												null));

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.RepeatStatementStatementContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											RepeatStatementStatementContext::repeatStatement -> {
												%s
											}
											"""
										.formatted(Objects.nonNull(context.repeatStatement()) ?
												context.repeatStatement().getText() :
												null));

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.CaseStatementStatementContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											CaseStatementStatementContext::caseStatement -> {
												%s
											}
											"""
										.formatted(Objects.nonNull(context.caseStatement()) ?
												context.caseStatement().getText() :
												null));

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.ExpressionStatementContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											ExpressionStatementContext::expression -> {
												%s
											}
											"""
										.formatted(Objects.nonNull(context.expression()) ?
												context.expression().getText() :
												null));

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.BreakStatementStatementContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											BreakStatementStatementContext::breakStatement -> {
												%s
											}
											"""
										.formatted(Objects.nonNull(context.breakStatement()) ?
												context.breakStatement().getText() :
												null));

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.BreakStatementContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											BreakStatementContext::BREAK -> %s
											BreakStatementContext::ID -> %s
											"""
										.formatted( Objects.nonNull(context.BREAK()) ?
															context.BREAK().getText() :
															null,
													context.ID()));

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.ContinueStatementStatementContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											ContinueStatementStatementContext::continueStatement -> {
												%s
											}
											"""
										.formatted(Objects.nonNull(context.continueStatement()) ?
												context.continueStatement().getText() :
												null));

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.ContinueStatementContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											ContinueStatementContext::CONTINUE -> %s
											ContinueStatementContext::ID -> %s
											"""
										.formatted( Objects.nonNull(context.CONTINUE()) ?
															context.CONTINUE().getText() :
															null,
													Objects.nonNull(context.ID()) ?
															context.ID().getText() :
															null));

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.ReturnStatementStatementContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											ReturnStatementStatementContext::returnStatement -> {
												%s
											}
											"""
										.formatted(Objects.nonNull(context.returnStatement()) ?
												context.returnStatement().getText() :
												null));

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.ReturnStatementContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											ReturnStatementContext::RETURN -> %s
											ReturnStatementContext::expression -> {
												%s
											}
											"""
										.formatted( Objects.nonNull(context.RETURN()) ?
															context.RETURN().getText() :
															null,
													Objects.nonNull(context.expression()) ?
															context.expression().getText() :
															null));

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.BlockStatementContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											BlockStatementContext::block -> {
												%s
											}
											"""
										.formatted(Objects.nonNull(context.block()) ?
												context.block() :
												null));

	}

	public static String logExecution(boolean doLog, org.daiitech.naftah.parser.NaftahParser.DeclarationContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											DeclarationContext::VARIABLE -> %s
											DeclarationContext::CONSTANT -> %s
											DeclarationContext::ID -> %s
											DeclarationContext::COLON -> %s
											DeclarationContext::type -> {
												%s
											}
											"""
										.formatted( Objects.nonNull(context.VARIABLE()) ?
															context.VARIABLE().getText() :
															null,
													Objects.nonNull(context.CONSTANT()) ?
															context.CONSTANT().getText() :
															null,
													Objects.nonNull(context.ID()) ? context.ID().getText() : null,
													Objects.nonNull(context.COLON()) ? context.COLON().getText() : null,
													Objects.nonNull(context.type()) ? context.type().getText() : null));

	}

	public static String logExecution(boolean doLog, org.daiitech.naftah.parser.NaftahParser.AssignmentContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											AssignmentContext::declaration -> {
												%s
											}
											AssignmentContext::ASSIGN -> %s
											AssignmentContext::expression -> {
												%s
											}
											"""
										.formatted( Objects.nonNull(context.declaration()) ?
															context.declaration().getText() :
															null,
													Objects.nonNull(context.ASSIGN()) ?
															context.ASSIGN().getText() :
															null,
													Objects.nonNull(context.expression()) ?
															context.expression().getText() :
															null));

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.FunctionDeclarationContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
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
										.formatted( Objects.nonNull(context.FUNCTION()) ?
															context.FUNCTION().getText() :
															null,
													Objects.nonNull(context.ID()) ? context.ID().getText() : null,
													Objects.nonNull(context.LPAREN()) ?
															context.LPAREN().getText() :
															null,
													Objects.nonNull(context.parameterDeclarationList()) ?
															context.parameterDeclarationList().getText() :
															null,
													Objects.nonNull(context.RPAREN()) ?
															context.RPAREN().getText() :
															null,
													Objects.nonNull(context.COLON()) ? context.COLON().getText() : null,
													Objects.nonNull(context.returnType()) ?
															context.returnType().getText() :
															null,
													Objects.nonNull(context.block()) ?
															context.block().getText() :
															null));

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.ParameterDeclarationListContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											ParameterDeclarationListContext::COMMA -> %s
											ParameterDeclarationListContext::SEMI -> %s
											ParameterDeclarationListContext::parameterDeclaration -> {
												%s
											}
											"""
										.formatted( join(context.COMMA()),
													join(context.SEMI()),
													join(context.parameterDeclaration())));

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.ParameterDeclarationContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
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
										.formatted( Objects.nonNull(context.ID()) ? context.ID().getText() : null,
													Objects.nonNull(context.COLON()) ? context.COLON().getText() : null,
													Objects.nonNull(context.type()) ? context.type().getText() : null,
													Objects.nonNull(context.ASSIGN()) ?
															context.ASSIGN().getText() :
															null,
													Objects.nonNull(context.value()) ?
															context.value().getText() :
															null));

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.QualifiedCallContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											QualifiedCallContext::qualifiedName -> {
												%s
											}
											QualifiedCallContext::COLON -> %s
											QualifiedCallContext::COLON -> %s
											QualifiedCallContext::ID -> %s
											"""
										.formatted( Objects.nonNull(context.qualifiedName()) ?
															context.qualifiedName().getText() :
															null,
													join(context.COLON()),
													join(context.COLON()),
													Objects.nonNull(context.ID()) ? context.ID().getText() : null));

	}

	public static String logExecution(boolean doLog, org.daiitech.naftah.parser.NaftahParser.FunctionCallContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											FunctionCallContext::ID -> %s
											FunctionCallContext::LPAREN -> %s
											FunctionCallContext::argumentList -> {
												%s
											}
											FunctionCallContext::RPAREN -> %s
											"""
										.formatted( Objects.nonNull(context.ID()) ? context.ID().getText() : null,
													Objects.nonNull(context.LPAREN()) ?
															context.LPAREN().getText() :
															null,
													Objects.nonNull(context.argumentList()) ?
															context.argumentList().getText() :
															null,
													Objects.nonNull(context.RPAREN()) ?
															context.RPAREN().getText() :
															null));

	}

	public static String logExecution(boolean doLog, org.daiitech.naftah.parser.NaftahParser.ArgumentListContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											ArgumentListContext::COMMA -> %s
											ArgumentListContext::SEMI -> %s
											ArgumentListContext::expression -> {
												%s
											}
											"""
										.formatted( join(context.COMMA()),
													join(context.SEMI()),
													join(context.expression())));

	}

	public static String logExecution(boolean doLog, org.daiitech.naftah.parser.NaftahParser.IfStatementContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											IfStatementContext::IF -> %s
											IfStatementContext::THEN -> %s
											IfStatementContext::ELSEIF -> %s
											IfStatementContext::ELSE -> %s
											IfStatementContext::block -> {
												%s
											}
											IfStatementContext::expression -> {
												%s
											}
											"""
										.formatted( Objects.nonNull(context.IF()) ? context.IF().getText() : null,
													join(context.THEN()),
													join(context.ELSEIF()),
													Objects.nonNull(context.ELSE()) ? context.ELSE().getText() : null,
													join(context.block()),
													join(context.expression())));

	}


	public static String logExecution(boolean doLog, org.daiitech.naftah.parser.NaftahParser.BlockContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											BlockContext::LBRACE -> %s
											BlockContext::statement -> {
												%s
											}
											BlockContext::RBRACE -> %s
											"""
										.formatted( Objects.nonNull(context.LBRACE()) ?
															context.LBRACE().getText() :
															null,
													join(context.statement()),
													context.RBRACE()));

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.ValueExpressionContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											ValueExpressionContext::value -> {
												%s
											}
											"""
										.formatted(Objects.nonNull(context.value()) ?
												context.value().getText() :
												null));

	}


	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.ParenthesisExpressionContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											ParenthesisExpressionContext::LPAREN -> %s
											ParenthesisExpressionContext::expression -> {
												%s
											}
											ParenthesisExpressionContext::RPAREN -> %s
											"""
										.formatted( Objects.nonNull(context.LPAREN()) ?
															context.LPAREN().getText() :
															null,
													Objects.nonNull(context.expression()) ?
															context.expression().getText() :
															null,

													Objects.nonNull(context.RPAREN()) ?
															context.RPAREN().getText() :
															null
										));

	}


	public static String logExecution(boolean doLog, org.daiitech.naftah.parser.NaftahParser.NumberValueContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											NumberValueContext::NUMBER -> %s
											"""
										.formatted(

													Objects.nonNull(context.NUMBER()) ?
															context.NUMBER().getText() :
															null
										));

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.RadixNumberValueContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											RadixNumberValueContext::BASE_DIGITS -> %s
											RadixNumberValueContext::BASE_RADIX -> %s
											"""
										.formatted( Objects.nonNull(context.BASE_DIGITS()) ?
															context.BASE_DIGITS().getText() :
															null,

													Objects.nonNull(context.BASE_RADIX()) ?
															context.BASE_RADIX().getText() :
															null
										));

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.CharacterValueContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											CharacterValueContext::CHARACTER -> %s
											"""
										.formatted(

													Objects.nonNull(context.CHARACTER()) ?
															context.CHARACTER().getText() :
															null
										));

	}

	public static String logExecution(boolean doLog, org.daiitech.naftah.parser.NaftahParser.StringValueContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											StringValueContext::RAW -> %s
											StringValueContext::BYTE_ARRAY -> %s
											StringValueContext::STRING -> %s
											"""
										.formatted( Objects.nonNull(context.RAW()) ? context.RAW().getText() : null,
													Objects.nonNull(context.BYTE_ARRAY()) ?
															context.BYTE_ARRAY().getText() :
															null,

													Objects.nonNull(context.STRING()) ?
															context.STRING().getText() :
															null
										));

	}

	public static String logExecution(boolean doLog, org.daiitech.naftah.parser.NaftahParser.TrueValueContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											TrueValueContext::TRUE -> %s
											"""
										.formatted(
													Objects.nonNull(context.TRUE()) ? context.TRUE().getText() : null));

	}

	public static String logExecution(boolean doLog, org.daiitech.naftah.parser.NaftahParser.FalseValueContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											FalseValueContext::FALSE -> %s
											"""
										.formatted(

													Objects.nonNull(context.FALSE()) ? context.FALSE().getText() : null
										));

	}

	public static String logExecution(boolean doLog, org.daiitech.naftah.parser.NaftahParser.NullValueContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											NullValueContext::NULL -> %s
											"""
										.formatted(

													Objects.nonNull(context.NULL()) ? context.NULL().getText() : null
										));

	}

	public static String logExecution(boolean doLog, org.daiitech.naftah.parser.NaftahParser.IdValueContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											IdValueContext::ID -> %s
											"""
										.formatted(

													Objects.nonNull(context.ID()) ? context.ID().getText() : null
										));

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.VoidReturnTypeContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											VoidReturnTypeContext::VOID -> %s
											"""
										.formatted(

													Objects.nonNull(context.VOID()) ? context.VOID().getText() : null
										));

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.TypeReturnTypeContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											TypeReturnTypeContext::type -> %s
											"""
										.formatted(Objects.nonNull(context.type()) ?
												context.type().getText() :
												null));

	}

	public static String logExecution(boolean doLog, org.daiitech.naftah.parser.NaftahParser.VarTypeContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											VarTypeContext::VAR -> %s
											"""
										.formatted(

													Objects.nonNull(context.VAR()) ? context.VAR().getText() : null
										));

	}

	public static String logExecution(boolean doLog, org.daiitech.naftah.parser.NaftahParser.BuiltInTypeContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											BuiltInTypeContext::builtIn -> {
													%s
											}
											"""
										.formatted(Objects.nonNull(context.builtIn()) ?
												context.builtIn().getText() :
												null));

	}

	public static String logExecution(boolean doLog, org.daiitech.naftah.parser.NaftahParser.BuiltInContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
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
										.formatted( Objects.nonNull(context.BOOLEAN()) ?
															context.BOOLEAN().getText() :
															null,
													Objects.nonNull(context.CHAR()) ? context.CHAR().getText() : null,
													Objects.nonNull(context.BYTE()) ? context.BYTE().getText() : null,
													Objects.nonNull(context.SHORT()) ? context.SHORT().getText() : null,
													Objects.nonNull(context.INT()) ? context.INT().getText() : null,
													Objects.nonNull(context.LONG()) ? context.LONG().getText() : null,
													Objects.nonNull(context.FLOAT()) ? context.FLOAT().getText() : null,
													Objects.nonNull(context.DOUBLE()) ?
															context.DOUBLE().getText() :
															null,


													Objects.nonNull(context.STRING_TYPE()) ?
															context.STRING_TYPE().getText() :
															null
										));

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.QualifiedNameTypeContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											QualifiedNameTypeContext::qualifiedName -> %s
											"""
										.formatted(Objects.nonNull(context.qualifiedName()) ?
												context.qualifiedName().getText() :
												null));

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.QualifiedNameContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											QualifiedNameContext::ID -> %s
											QualifiedNameContext::QUESTION -> %s
											QualifiedNameContext::COLON -> %s
											"""
										.formatted( join(context.ID()),
													join(context.QUESTION()),
													join(context.COLON())));

	}


	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.NullishExpressionContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											NullishExpressionContext::logicalExpression -> %s
											NullishExpressionContext::QUESTION -> %s
											"""
										.formatted( join(context.logicalExpression()),
													join(context.QUESTION()))
		);
	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.TernaryExpressionContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											TernaryExpressionContext::nullishExpression -> %s
											TernaryExpressionContext::QUESTION -> %s
											TernaryExpressionContext::expression -> %s
											TernaryExpressionContext::COLON -> %s
											TernaryExpressionContext::ternaryExpression -> %s
											"""
										.formatted( Objects.nonNull(context.nullishExpression()) ?
															context.nullishExpression().getText() :
															null,
													Objects.nonNull(context.QUESTION()) ?
															context.QUESTION().getText() :
															null,
													Objects.nonNull(context.expression()) ?
															context.expression().getText() :
															null,
													Objects.nonNull(context.COLON()) ?
															context.COLON().getText() :
															null,
													Objects.nonNull(context.ternaryExpression()) ?
															context.ternaryExpression().getText() :
															null)
		);
	}

	public static String logExecution(boolean doLog, org.daiitech.naftah.parser.NaftahParser.ExpressionContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											ExpressionContext::logicalExpression -> %s
											"""
										.formatted(Objects.nonNull(context.ternaryExpression()) ?
												context.ternaryExpression().getText() :
												null));
	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.LogicalExpressionContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											LogicalExpressionContext::bitwiseExpression -> {
											%s
											}
											LogicalExpressionContext::AND -> %s
											LogicalExpressionContext::OR -> %s
											"""
										.formatted( join(context.bitwiseExpression()),
													join(context.AND()),
													join(context.OR())));
	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.BitwiseExpressionContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											BitwiseExpressionContext::equalityExpression -> {
											%s
											}
											BitwiseExpressionContext::BITWISE_AND -> %s
											BitwiseExpressionContext::BITWISE_OR -> %s
											BitwiseExpressionContext::BITWISE_XOR -> %s
											"""
										.formatted( join(context.equalityExpression()),
													join(context.BITWISE_AND()),
													join(context.BITWISE_OR()),
													join(context.BITWISE_XOR())
										));
	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.EqualityExpressionContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											EqualityExpressionContext::relationalExpression -> {
											%s
											}
											EqualityExpressionContext::EQ -> %s
											EqualityExpressionContext::NEQ -> %s
											"""
										.formatted( join(context.relationalExpression()),
													join(context.EQ()),
													join(context.NEQ())));
	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.RelationalExpressionContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											RelationalExpressionContext::additiveExpression -> {
											%s
											}
											RelationalExpressionContext::LT -> %s
											RelationalExpressionContext::LE -> %s
											RelationalExpressionContext::GT -> %s
											RelationalExpressionContext::GE -> %s
											"""
										.formatted( join(context.additiveExpression()),
													join(context.LT()),
													join(context.LE()),
													join(context.GT()),
													join(context.GE())));
	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.AdditiveExpressionContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											AdditiveExpressionContext::multiplicativeExpression -> {
											%s
											}
											AdditiveExpressionContext::PLUS -> %s
											AdditiveExpressionContext::MINUS -> %s
											AdditiveExpressionContext::ELEMENTWISE_PLUS -> %s
											AdditiveExpressionContext::ELEMENTWISE_MINUS -> %s
											"""
										.formatted( join(context.multiplicativeExpression()),
													join(context.PLUS()),
													join(context.MINUS()),
													join(context.ELEMENTWISE_PLUS()),
													join(context.ELEMENTWISE_MINUS())));
	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.MultiplicativeExpressionContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											MultiplicativeExpressionContext::unaryExpression -> {
											%s
											}
											MultiplicativeExpressionContext::MUL -> %s
											MultiplicativeExpressionContext::DIV -> %s
											MultiplicativeExpressionContext::MOD -> %s
											MultiplicativeExpressionContext::ELEMENTWISE_MUL -> %s
											MultiplicativeExpressionContext::ELEMENTWISE_DIV -> %s
											MultiplicativeExpressionContext::ELEMENTWISE_MOD -> %s
											"""
										.formatted( join(context.unaryExpression()),
													join(context.MUL()),
													join(context.DIV()),
													join(context.MOD()),
													join(context.ELEMENTWISE_MUL()),
													join(context.ELEMENTWISE_DIV()),
													join(context.ELEMENTWISE_MOD())
										));
	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.PrefixUnaryExpressionContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											PrefixUnaryExpressionContext::PLUS -> %s
											PrefixUnaryExpressionContext::MINUS -> %s
											PrefixUnaryExpressionContext::NOT -> %s
											PrefixUnaryExpressionContext::BITWISE_NOT -> %s
											PrefixUnaryExpressionContext::INCREMENT -> %s
											PrefixUnaryExpressionContext::DECREMENT -> %s
											PrefixUnaryExpressionContext::unaryExpression -> {
											%s
											}
											"""
										.formatted(
													Objects.nonNull(context.PLUS()) ? context.PLUS().getText() : null,
													Objects.nonNull(context.MINUS()) ? context.MINUS().getText() : null,
													Objects.nonNull(context.NOT()) ? context.NOT().getText() : null,
													Objects.nonNull(context.BITWISE_NOT()) ?
															context.BITWISE_NOT().getText() :
															null,
													Objects.nonNull(context.INCREMENT()) ?
															context.INCREMENT().getText() :
															null,
													Objects.nonNull(context.DECREMENT()) ?
															context.DECREMENT().getText() :
															null,
													Objects.nonNull(context.unaryExpression()) ?
															context.unaryExpression().getText() :
															null
										));
	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.PostfixUnaryExpressionContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											PostfixUnaryExpressionContext::postfixExpression -> %s
											"""
										.formatted(Objects.nonNull(context.postfixExpression()) ?
												context.postfixExpression().getText() :
												null));
	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.PostfixExpressionContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											PostfixExpressionContext::unaryExpression -> {
											%s
											}
											PostfixExpressionContext::INCREMENT -> %s
											PostfixExpressionContext::DECREMENT -> %s
											"""
										.formatted( Objects.nonNull(context.primary()) ?
															context.primary().getText() :
															null,
													Objects.nonNull(context.INCREMENT()) ?
															context.INCREMENT().getText() :
															null,
													Objects.nonNull(context.DECREMENT()) ?
															context.DECREMENT().getText() :
															null));
	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.FunctionCallExpressionContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											FunctionCallExpressionContext::functionCall -> {
											%s
											}
													"""
										.formatted(Objects.nonNull(context.functionCall()) ?
												context.functionCall().getText() :
												null));
	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.ObjectExpressionContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											ObjectExpressionContext::object -> {
											%s
											}
													"""
										.formatted(Objects.nonNull(context.object()) ?
												context.object().getText() :
												null));
	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.CollectionExpressionContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											CollectionExpressionContext::collection -> {
											%s
											}
														"""
										.formatted(Objects.nonNull(context.collection()) ?
												context.collection().getText() :
												null));
	}


	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.CollectionAccessStatementContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											CollectionAccessStatementContext::collectionAccess -> {
											%s
											}
														"""
										.formatted(Objects.nonNull(context.collectionAccess()) ?
												context.collectionAccess().getText() :
												null));
	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.CollectionAccessExpressionContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											CollectionAccessExpressionContext::collectionAccess -> {
											%s
											}
														"""
										.formatted(Objects.nonNull(context.collectionAccess()) ?
												context.collectionAccess().getText() :
												null));
	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.CollectionAccessContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											CollectionAccessContext::ID -> %s
											CollectionAccessContext::QUESTION -> %s
											CollectionAccessContext::LBRACK -> %s
											CollectionAccessContext::NUMBER -> %s
											CollectionAccessContext::RBRACK -> %s"""
										.formatted( Objects.nonNull(context.ID()) ?
															context.ID().getText() :
															null,
													join(context.QUESTION()),
													join(context.LBRACK()),
													join(context.NUMBER()),
													join(context.RBRACK())));
	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.ObjectAccessExpressionContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											ObjectAccessExpressionContext::objectAccess -> {
											%s
											}
														"""
										.formatted(Objects.nonNull(context.objectAccess()) ?
												context.objectAccess().getText() :
												null));
	}


	public static String logExecution(boolean doLog, org.daiitech.naftah.parser.NaftahParser.ObjectContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											ObjectContext::LBRACE -> %s
											ObjectContext::objectFields -> {
											%s
											}
											ObjectContext::RBRACE -> %s
														"""
										.formatted(
													Objects.nonNull(context.LBRACE()) ?
															context.LBRACE().getText() :
															null,
													Objects.nonNull(context.objectFields()) ?
															context.objectFields().getText() :
															null,
													Objects.nonNull(context.RBRACE()) ?
															context.objectFields().getText() :
															null

										));
	}

	public static String logExecution(boolean doLog, org.daiitech.naftah.parser.NaftahParser.ObjectFieldsContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											ObjectFieldsContext::assignment -> {
											%s
											}
											ObjectFieldsContext::COMMA ->{
											%s
											}
											ObjectFieldsContext::SEMI -> {
											%s
											}
														"""
										.formatted(
													join(context.assignment()),
													join(context.COMMA()),
													join(context.SEMI())

										));
	}

	public static String logExecution(boolean doLog, org.daiitech.naftah.parser.NaftahParser.ListValueContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											ListValueContext::LBRACK -> %s
											ListValueContext::elements -> {
											%s
											}
											ListValueContext::RBRACK -> %s
													"""
										.formatted(
													Objects.nonNull(context.LBRACK()) ?
															context.LBRACK().getText() :
															null,
													Objects.nonNull(context.elements()) ?
															context.elements().getText() :
															null,
													Objects.nonNull(context.RBRACK()) ?
															context.RBRACK().getText() :
															null

										));
	}

	public static String logExecution(boolean doLog, org.daiitech.naftah.parser.NaftahParser.TupleValueContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											TupleValueContext::LPAREN -> %s
											TupleValueContext::elements -> {
											%s
											}
											TupleValueContext::RPAREN -> %s
													"""
										.formatted(
													Objects.nonNull(context.LPAREN()) ?
															context.LPAREN().getText() :
															null,
													Objects.nonNull(context.elements()) ?
															context.elements().getText() :
															null,
													Objects.nonNull(context.RPAREN()) ?
															context.RPAREN().getText() :
															null

										));
	}

	public static String logExecution(boolean doLog, org.daiitech.naftah.parser.NaftahParser.SetValueContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											SetValueContext::LBRACE -> %s
											SetValueContext::elements -> {
											%s
											}
											SetValueContext::RBRACE -> %s
													"""
										.formatted(
													Objects.nonNull(context.LBRACE()) ?
															context.LBRACE().getText() :
															null,
													Objects.nonNull(context.elements()) ?
															context.elements().getText() :
															null,
													Objects.nonNull(context.RBRACE()) ?
															context.RBRACE().getText() :
															null

										));
	}

	public static String logExecution(boolean doLog, org.daiitech.naftah.parser.NaftahParser.MapValueContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											MapValueContext::LBRACE -> %s
											MapValueContext::keyValuePairs -> {
											%s
											}
											MapValueContext::RBRACE -> %s
													"""
										.formatted(
													Objects.nonNull(context.LBRACE()) ?
															context.LBRACE().getText() :
															null,
													Objects.nonNull(context.keyValuePairs()) ?
															context.keyValuePairs().getText() :
															null,
													Objects.nonNull(context.RBRACE()) ?
															context.RBRACE().getText() :
															null

										));
	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.SingleElementContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											SingleElementContext::expression -> %s
											SingleElementContext::COMMA -> %s
											SingleElementContext::SEMI -> %s
											"""
										.formatted( Objects.nonNull(context.expression()) ?
															context.expression().getText() :
															null,
													Objects.nonNull(context.COMMA()) ?
															context.expression().getText() :
															null,
													Objects.nonNull(context.SEMI()) ?
															context.expression().getText() :
															null
										));
	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.MultipleElementsContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											MultipleElementsContext::expression -> {
											%s
											}
											MultipleElementsContext::COMMA -> %s
											MultipleElementsContext::SEMI -> %s
											"""
										.formatted(
													join(context.expression()),
													join(context.COMMA()),
													join(context.SEMI())
										));
	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.KeyValuePairsContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											KeyValuePairsContext::keyValue -> {
											%s
											}
											KeyValuePairsContext::COMMA -> %s
											KeyValuePairsContext::SEMI -> %s
											"""
										.formatted( join(context.keyValue()),
													join(context.COMMA()),
													join(context.SEMI())
										));
	}

	public static String logExecution(boolean doLog, org.daiitech.naftah.parser.NaftahParser.KeyValueContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											KeyValueContext::expression -> {
											%s
											}
											KeyValueContext::COLON -> %s
											"""
										.formatted(
													join(context.expression()),
													Objects.nonNull(context.COLON()) ? context.COLON() : null
										));
	}


	public static String logExecution(boolean doLog, org.daiitech.naftah.parser.NaftahParser.NanValueContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											NanValueContext::NAN -> %s
											"""
										.formatted(
													Objects.nonNull(context.NAN()) ? context.NAN() : null
										));
	}

	public static String logExecution(boolean doLog, org.daiitech.naftah.parser.NaftahParser.LabelContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											LabelContext::ID -> %s
											LabelContext::COLON -> %s
											"""
										.formatted(
													Objects.nonNull(context.ID()) ? context.ID() : null,
													Objects.nonNull(context.COLON()) ? context.COLON() : null
										));
	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.IndexBasedForLoopStatementContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											IndexBasedforLoopStatementContext::label -> %s
											IndexBasedforLoopStatementContext::FOR -> %s
											IndexBasedforLoopStatementContext::ID -> %s
											IndexBasedforLoopStatementContext::ASSIGN -> %s
											IndexBasedforLoopStatementContext::expression -> %s
											IndexBasedforLoopStatementContext::TO -> %s
											IndexBasedforLoopStatementContext::DOWNTO -> %s
											IndexBasedforLoopStatementContext::expression -> %s
											IndexBasedforLoopStatementContext::STEP -> %s
											IndexBasedforLoopStatementContext::expression -> %s
											IndexBasedforLoopStatementContext::DO -> %s
											IndexBasedforLoopStatementContext::block -> %s
											IndexBasedforLoopStatementContext::ELSE -> %s
											IndexBasedforLoopStatementContext::block -> %s
											"""
										.formatted(
													Objects.nonNull(context.label()) ? context.label().getText() : null,
													Objects.nonNull(context.FOR()) ? context.FOR().getText() : null,
													Objects.nonNull(context.ID()) ? context.ID().getText() : null,
													Objects.nonNull(context.ASSIGN()) ?
															context.ASSIGN().getText() :
															null,
													Objects.nonNull(context.expression(0)) ?
															context.expression(0).getText() :
															null,
													Objects.nonNull(context.TO()) ? context.TO().getText() : null,
													Objects.nonNull(context.DOWNTO()) ?
															context.DOWNTO().getText() :
															null,
													Objects.nonNull(context.expression(1)) ?
															context.expression(1).getText() :
															null,
													Objects.nonNull(context.STEP()) ?
															context.STEP().getText() :
															null,
													Objects.nonNull(context.expression(2)) ?
															context.expression(2).getText() :
															null,
													Objects.nonNull(context.DO()) ? context.DO().getText() : null,
													Objects.nonNull(context.block(0)) ?
															context.block(0).getText() :
															null,
													Objects.nonNull(context.ELSE()) ? context.ELSE().getText() : null,
													Objects.nonNull(context.block(1)) ?
															context.block(1).getText() :
															null

										));
	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.ForEachLoopStatementContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											ForEachLoopStatementContext::label -> %s
											ForEachLoopStatementContext::FOR -> %s
											ForEachLoopStatementContext::foreachTarget -> %s
											ForEachLoopStatementContext::IN -> %s
											ForEachLoopStatementContext::expression -> %s
											ForEachLoopStatementContext::DO -> %s
											ForEachLoopStatementContext::block -> %s
											ForEachLoopStatementContext::ELSE -> %s
											ForEachLoopStatementContext::block -> %s
											"""
										.formatted(
													Objects.nonNull(context.label()) ? context.label().getText() : null,
													Objects.nonNull(context.FOR()) ? context.FOR().getText() : null,
													Objects.nonNull(context.foreachTarget()) ?
															context.foreachTarget().getText() :
															null,
													Objects.nonNull(context.IN()) ?
															context.IN().getText() :
															null,
													Objects.nonNull(context.expression()) ?
															context.expression().getText() :
															null,
													Objects.nonNull(context.DO()) ? context.DO().getText() : null,
													Objects.nonNull(context.block(0)) ?
															context.block(0).getText() :
															null,
													Objects.nonNull(context.ELSE()) ? context.ELSE().getText() : null,
													Objects.nonNull(context.block(1)) ?
															context.block(1).getText() :
															null
										));
	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.ValueForeachTargetContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											ValueForeachTargetContext::ID -> %s
											"""
										.formatted(
													Objects.nonNull(context.ID()) ? context.ID().getText() : null
										));
	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.KeyValueForeachTargetContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											KeyValueForeachTargetContext::ID -> %s
											KeyValueForeachTargetContext::COLON -> %s
											"""
										.formatted(
													join(context.ID()),
													Objects.nonNull(context.COLON()) ? context.COLON().getText() : null
										));
	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.IndexAndValueForeachTargetContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											IndexAndValueForeachTargetContext::ID -> %s
											IndexAndValueForeachTargetContext::COMMA -> %s
											IndexAndValueForeachTargetContext::SEMI -> %s
											"""
										.formatted(
													join(context.ID()),
													Objects.nonNull(context.COMMA()) ? context.COMMA().getText() : null,
													Objects.nonNull(context.SEMI()) ? context.SEMI().getText() : null
										));
	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.IndexAndKeyValueForeachTargetContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											IndexAndKeyValueForeachTargetContext::ID -> %s
											IndexAndKeyValueForeachTargetContext::COMMA -> %s
											IndexAndKeyValueForeachTargetContext::SEMI -> %s
											IndexAndKeyValueForeachTargetContext::COLON -> %s
											"""
										.formatted(
													join(context.ID()),
													Objects.nonNull(context.COMMA()) ? context.COMMA().getText() : null,
													Objects.nonNull(context.SEMI()) ? context.SEMI().getText() : null,
													Objects.nonNull(context.COLON()) ? context.COLON().getText() : null
										));
	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.WhileStatementContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											WhileStatementContext::label -> %s
											WhileStatementContext::WHILE -> %s
											WhileStatementContext::expression -> %s
											WhileStatementContext::DO -> %s
											WhileStatementContext::block -> %s
											"""
										.formatted(
													Objects.nonNull(context.label()) ? context.label().getText() : null,
													Objects.nonNull(context.WHILE()) ? context.WHILE().getText() : null,
													Objects.nonNull(context.expression()) ?
															context.expression().getText() :
															null,
													Objects.nonNull(context.DO()) ? context.DO().getText() : null,
													Objects.nonNull(context.block()) ?
															context.block().getText() :
															null

										));
	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.RepeatStatementContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											RepeatStatementContext::label -> %s
											RepeatStatementContext::REPEAT -> %s
											RepeatStatementContext::block -> %s
											RepeatStatementContext::UNTIL -> %s
											RepeatStatementContext::expression -> %s
											"""
										.formatted(
													Objects.nonNull(context.label()) ? context.label().getText() : null,
													Objects.nonNull(context.REPEAT()) ?
															context.REPEAT().getText() :
															null,
													Objects.nonNull(context.block()) ?
															context.block().getText() :
															null,
													Objects.nonNull(context.UNTIL()) ? context.UNTIL().getText() : null,
													Objects.nonNull(context.expression()) ?
															context.expression().getText() :
															null

										));
	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.CaseStatementContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											CaseStatementContext::CASE -> %s
											CaseStatementContext::expression -> %s
											CaseStatementContext::OF -> %s
											CaseStatementContext::caseLabelList -> {
											%s
											}
											CaseStatementContext::COLON -> %s
											CaseStatementContext::ELSE -> %s
											CaseStatementContext::block -> {
											%s
											}
											"""
										.formatted(
													Objects.nonNull(context.CASE()) ? context.CASE().getText() : null,
													Objects.nonNull(context.expression()) ?
															context.expression().getText() :
															null,
													Objects.nonNull(context.OF()) ? context.OF().getText() : null,
													join(context.caseLabelList()),
													join(context.COLON()),
													Objects.nonNull(context.ELSE()) ? context.ELSE().getText() : null,
													join(context.block())
										));
	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.CaseLabelListContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											CaseLabelListContext::expression -> {
											%s
											}
											CaseLabelListContext::COMMA -> %s
											CaseLabelListContext::SEMI -> %s
													"""
										.formatted(
													join(context.expression()),
													join(context.COMMA()),
													join(context.SEMI())
										));
	}

	public static <T extends ParserRuleContext> String doLogExecution(  boolean doLog,
																		T ctx,
																		Function<T, String> ctxLogger) {
		return Optional.ofNullable(ctx).map(context -> {
			String result = ctxLogger.apply(ctx);
			if (doLog && LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.finest(result);
			}
			return result;
		}).orElse("");
	}
}
