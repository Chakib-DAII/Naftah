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
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.ScopeBlockStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.BlockStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.ImportStatementStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.ImportStatementAsAliasContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.GroupedImportStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.QualifiedCallImportStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.ImportsContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.CallableImportElementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.ImportAliasContext context) {
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
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.TryStatementStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.FunctionDeclarationStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.ObjectAccessContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.QualifiedObjectAccessContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.PropertyAccessContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.DeclarationStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.ChannelDeclarationStatementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.ActorDeclarationStatementContext context) {
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
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.ChannelDeclarationContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.ActorDeclarationContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.SingleDeclarationContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.MultipleDeclarationsContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.AssignmentContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.SingleAssignmentExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.MultipleAssignmentsExpressionContext context) {
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
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.InitCallContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.PrimaryCallContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.CallSegmentContext context) {
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
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.TryStatementWithTryCasesContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.OkCaseContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.ErrorCaseContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.TryStatementWithOptionCasesContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.SomeCaseContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.NoneCaseContext context) {
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
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.SingleReturnContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.MultipleReturnsContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.ScopeBlockContext context) {
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
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.ShiftExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.AdditiveExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.MultiplicativeExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.PowerExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.SpawnUnaryExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.AwaitUnaryExpressionContext context) {
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
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.CollectionAccessExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.CollectionAccessContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.CollectionAccessIndexContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.ObjectAccessExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.EmptyObjectContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.ObjectValueContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.ValueExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.TypeExpressionContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.ParenthesisExpressionContext context) {
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
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.EmptySetContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.SetValueContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.EmptyMapContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.MapValueContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.SingleElementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.TupleSingleElementContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.MultipleElementsContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.TupleMultipleElementsContext context) {
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
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.ComplexTypeContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.BuiltInTypeContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.QualifiedNameTypeContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.SimpleCallContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.QualifiedNameCallContext context) {
			result = logExecution(doLog, context);
		}
		else if (ctx instanceof org.daiitech.naftah.parser.NaftahParser.ComplexBuiltInContext context) {
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
		return doLogExecution(  doLog,
								ctx,
								context -> """
													ProgramContext::statement -> {
														%s
													}
											ProgramContext::END -> {
											%s
											}
											"""
										.formatted( join(context.statement()),
													join(context.END())));
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
											QualifiedObjectAccessContext::propertyAccess -> %s
											"""
										.formatted(
													Objects.nonNull(context.ID()) ?
															context.ID().getText() :
															null,
													join(context.QUESTION()),
													join(context.propertyAccess())));

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.PropertyAccessContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											PropertyAccessContext::ID -> %s
											PropertyAccessContext::COLON -> %s
											PropertyAccessContext::LBRACK -> %s
											PropertyAccessContext::CHARACTER -> %s
											PropertyAccessContext::STRING -> %s
											PropertyAccessContext::RBRACK -> %s
											"""
										.formatted(
													Objects.nonNull(context.ID()) ?
															context.ID().getText() :
															null,
													Objects.nonNull(context.COLON()) ?
															context.COLON().getText() :
															null,
													Objects.nonNull(context.LBRACK()) ?
															context.LBRACK().getText() :
															null,
													Objects.nonNull(context.CHARACTER()) ?
															context.CHARACTER().getText() :
															null,
													Objects.nonNull(context.STRING()) ?
															context.STRING().getText() :
															null,
													Objects.nonNull(context.RBRACK()) ?
															context.RBRACK().getText() :
															null));

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
										org.daiitech.naftah.parser.NaftahParser.ChannelDeclarationStatementContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											ChannelDeclarationStatementContext::channelDeclaration -> {
												%s
											}
											"""
										.formatted(Objects.nonNull(context.channelDeclaration()) ?
												context.channelDeclaration().getText() :
												null));

	}


	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.ActorDeclarationStatementContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											ActorDeclarationStatementContext::actorDeclaration -> {
												%s
											}
											"""
										.formatted(Objects.nonNull(context.actorDeclaration()) ?
												context.actorDeclaration().getText() :
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
											ReturnStatementContext::singleReturn -> %s
											ReturnStatementContext::multipleReturns -> {
												%s
											}
											"""
										.formatted( Objects.nonNull(context.singleReturn()) ?
															context.singleReturn().getText() :
															null,
													Objects.nonNull(context.multipleReturns()) ?
															context.multipleReturns().getText() :
															null));

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.SingleReturnContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											SingleReturnContext::RETURN -> %s
											SingleReturnContext::expression -> {
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
										org.daiitech.naftah.parser.NaftahParser.MultipleReturnsContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											MultipleReturnsContext::RETURN -> %s
											MultipleReturnsContext::LPAREN -> %s
											MultipleReturnsContext::tupleElements -> {
												%s
											}
											MultipleReturnsContext::RPAREN -> %s
											"""
										.formatted( Objects.nonNull(context.RETURN()) ?
															context.RETURN().getText() :
															null,
													Objects.nonNull(context.LPAREN()) ?
															context.LPAREN().getText() :
															null,
													Objects.nonNull(context.tupleElements()) ?
															context.tupleElements().getText() :
															null,
													Objects.nonNull(context.RPAREN()) ?
															context.RPAREN().getText() :
															null));

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.ScopeBlockStatementContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											ScopeBlockStatementContext::scopeBlock -> {
												%s
											}
											"""
										.formatted(Objects.nonNull(context.scopeBlock()) ?
												context.scopeBlock() :
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

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.ImportStatementStatementContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											ImportStatementStatementContext::importStatement -> {
												%s
											}
											"""
										.formatted(Objects.nonNull(context.importStatement()) ?
												context.importStatement() :
												null));

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.ImportStatementAsAliasContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											ImportStatementAsAliasContext::IMPORT -> %s
											ImportStatementAsAliasContext::ID -> %s
											ImportStatementAsAliasContext::importAlias -> %s
											"""
										.formatted( Objects.nonNull(context.IMPORT()) ?
															context.IMPORT() :
															null,
													Objects.nonNull(context.ID()) ?
															context.ID() :
															null,
													Objects.nonNull(context.importAlias()) ?
															context.importAlias() :
															null)
		);

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.GroupedImportStatementContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											GroupedImportStatementContext::IMPORT -> %s
											GroupedImportStatementContext::qualifiedName -> %s
											GroupedImportStatementContext::COLON -> {
												%s
											}
											GroupedImportStatementContext::imports -> %s
											"""
										.formatted( Objects.nonNull(context.IMPORT()) ?
															context.IMPORT() :
															null,
													Objects.nonNull(context.qualifiedName()) ?
															context.qualifiedName() :
															null,
													join(context.COLON()),
													Objects.nonNull(context.imports()) ?
															context.imports() :
															null)
		);

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.QualifiedCallImportStatementContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											QualifiedCallImportStatementContext::IMPORT -> %s
											QualifiedCallImportStatementContext::qualifiedCall -> %s
											QualifiedCallImportStatementContext::importAlias -> %s
											"""
										.formatted( Objects.nonNull(context.IMPORT()) ?
															context.IMPORT() :
															null,
													Objects.nonNull(context.qualifiedCall()) ?
															context.qualifiedCall() :
															null,
													Objects.nonNull(context.importAlias()) ?
															context.importAlias() :
															null)
		);

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.ImportsContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											ImportsContext::LBRACK -> %s
											ImportsContext::importElements -> %s
											ImportsContext::RBRACK -> %s
											ImportsContext::callableImportElement -> %s
											"""
										.formatted( Objects.nonNull(context.LBRACK()) ?
															context.LBRACK() :
															null,
													Objects.nonNull(context.importElements()) ?
															context.importElements() :
															null,
													Objects.nonNull(context.RBRACK()) ?
															context.RBRACK() :
															null,
													Objects.nonNull(context.callableImportElement()) ?
															context.callableImportElement() :
															null)
		);

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.CallableImportElementContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											CallableImportElementContext::ID -> %s
											CallableImportElementContext::qualifiedName -> %s
											CallableImportElementContext::qualifiedCall -> %s
											CallableImportElementContext::importAlias -> %s
											"""
										.formatted( Objects.nonNull(context.ID()) ?
															context.ID() :
															null,
													Objects.nonNull(context.qualifiedName()) ?
															context.qualifiedName() :
															null,
													Objects.nonNull(context.qualifiedCall()) ?
															context.qualifiedCall() :
															null,
													Objects.nonNull(context.importAlias()) ?
															context.importAlias() :
															null)
		);

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.ImportAliasContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											ImportAliasContext::AS -> %s
											ImportAliasContext::ID -> %s
											"""
										.formatted( Objects.nonNull(context.AS()) ?
															context.AS() :
															null,
													Objects.nonNull(context.ID()) ?
															context.ID() :
															null)
		);

	}

	public static String logExecution(boolean doLog, org.daiitech.naftah.parser.NaftahParser.DeclarationContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											DeclarationContext::singleDeclaration -> %s
											DeclarationContext::multipleDeclarations -> %s
											"""
										.formatted(
													Objects.nonNull(context.singleDeclaration()) ?
															context.singleDeclaration().getText() :
															null,
													Objects.nonNull(context.multipleDeclarations()) ?
															context.multipleDeclarations().getText() :
															null));

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.ChannelDeclarationContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											ChannelDeclarationContext::CHANNEL -> %s
											ChannelDeclarationContext::ID -> %s
											ChannelDeclarationContext::COLON -> %s
											ChannelDeclarationContext::type -> %s
											"""
										.formatted(
													Objects.nonNull(context.CHANNEL()) ?
															context.CHANNEL().getText() :
															null,
													Objects.nonNull(context.ID()) ?
															context.ID().getText() :
															null,
													Objects.nonNull(context.COLON()) ?
															context.COLON().getText() :
															null,
													Objects.nonNull(context.type()) ?
															context.type().getText() :
															null));

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.ActorDeclarationContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											ActorDeclarationContext::ACTOR -> %s
											ActorDeclarationContext::LPAREN -> {
											%s
											}
											ActorDeclarationContext::ID -> {
											%s
											}
											ActorDeclarationContext::COLON -> %s
											ActorDeclarationContext::type -> %s
											ActorDeclarationContext::COMMA -> %s
											ActorDeclarationContext::SEMI -> %s
											ActorDeclarationContext::objectFields -> {
											%s
											}
											ActorDeclarationContext::RPAREN -> {
											%s
											}
											"""
										.formatted(
													Objects.nonNull(context.ACTOR()) ?
															context.ACTOR().getText() :
															null,
													join(context.LPAREN()),
													join(context.ID()),
													Objects.nonNull(context.COLON()) ?
															context.COLON().getText() :
															null,
													Objects.nonNull(context.type()) ?
															context.type().getText() :
															null,
													Objects.nonNull(context.COMMA()) ?
															context.COMMA().getText() :
															null,
													Objects.nonNull(context.SEMI()) ?
															context.SEMI().getText() :
															null,
													Objects.nonNull(context.objectFields()) ?
															context.objectFields().getText() :
															null,
													join(context.RPAREN())

										));

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.SingleDeclarationContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											SingleDeclarationContext::VARIABLE -> %s
											SingleDeclarationContext::CONSTANT -> %s
											SingleDeclarationContext::ID -> %s
											SingleDeclarationContext::COLON -> %s
											SingleDeclarationContext::type -> {
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

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.MultipleDeclarationsContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											MultipleDeclarationsContext::VARIABLE -> %s
											MultipleDeclarationsContext::CONSTANT -> %s
											MultipleDeclarationsContext::ID ->  {
												%s
											}
											MultipleDeclarationsContext::COMMA ->  {
												%s
											}
											MultipleDeclarationsContext::SEMI ->  {
												%s
											}
											MultipleDeclarationsContext::COLON -> %s
											MultipleDeclarationsContext::type -> {
												%s
											}
											"""
										.formatted( Objects.nonNull(context.VARIABLE()) ?
															context.VARIABLE().getText() :
															null,
													Objects.nonNull(context.CONSTANT()) ?
															context.CONSTANT().getText() :
															null,
													join(context.ID()),
													join(context.COMMA()),
													join(context.SEMI()),
													Objects.nonNull(context.COLON()) ? context.COLON().getText() : null,
													join(context.type())));

	}

	public static String logExecution(boolean doLog, org.daiitech.naftah.parser.NaftahParser.AssignmentContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											AssignmentContext::singleAssignmentExpression -> %s
											AssignmentContext::multipleAssignmentsExpression -> %s
											"""
										.formatted(
													Objects.nonNull(context.singleAssignmentExpression()) ?
															context.singleAssignmentExpression().getText() :
															null,
													Objects.nonNull(context.multipleAssignmentsExpression()) ?
															context.multipleAssignmentsExpression().getText() :
															null));
	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.SingleAssignmentExpressionContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											SingleAssignmentExpressionContext::singleDeclaration -> {
												%s
											}
											SingleAssignmentExpressionContext::singleAssignment::ID -> {
												%s
											}
											SingleAssignmentExpressionContext::singleAssignment::qualifiedName -> {
												%s
											}
											SingleAssignmentExpressionContext::singleAssignment::qualifiedObjectAccess -> {
												%s
											}
											SingleAssignmentExpressionContext::singleAssignment::collectionAccess -> {
												%s
											}
											SingleAssignmentExpressionContext::ASSIGN -> %s
											SingleAssignmentExpressionContext::expression -> {
												%s
											}
											"""
										.formatted( Objects.nonNull(context.singleDeclaration()) ?
															context.singleDeclaration().getText() :
															null,
													Objects.nonNull(context.singleAssignment()) && Objects
															.nonNull(context
																	.singleAssignment()
																	.ID()) ?
																			context.singleAssignment().ID().getText() :
																			null,
													Objects.nonNull(context.singleAssignment()) && Objects
															.nonNull(context
																	.singleAssignment()
																	.qualifiedName()) ?
																			context
																					.singleAssignment()
																					.qualifiedName()
																					.getText() :
																			null,
													Objects.nonNull(context.singleAssignment()) && Objects
															.nonNull(context
																	.singleAssignment()
																	.qualifiedObjectAccess()) ?
																			context
																					.singleAssignment()
																					.qualifiedObjectAccess()
																					.getText() :
																			null,
													Objects.nonNull(context.singleAssignment()) && Objects
															.nonNull(context
																	.singleAssignment()
																	.collectionAccess()) ?
																			context
																					.singleAssignment()
																					.collectionAccess()
																					.getText() :
																			null,
													Objects.nonNull(context.ASSIGN()) ?
															context.ASSIGN().getText() :
															null,
													Objects.nonNull(context.expression()) ?
															context.expression().getText() :
															null));
	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.MultipleAssignmentsExpressionContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											MultipleAssignmentsExpressionContext::multipleDeclarations -> {
												%s
											}
											MultipleAssignmentsExpressionContext::multipleAssignments -> {
												%s
											}
											MultipleAssignmentsExpressionContext::ASSIGN -> %s
											MultipleAssignmentsExpressionContext::expression -> {
												%s
											}
											"""
										.formatted( Objects.nonNull(context.multipleDeclarations()) ?
															context.multipleDeclarations().getText() :
															null,
													Objects.nonNull(context.multipleAssignments()) && Objects
															.nonNull(
																		context
																				.multipleAssignments()
																				.singleAssignment()) ?
																						join(context
																								.multipleAssignments()
																								.singleAssignment()) :
																						null,
													Objects.nonNull(context.ASSIGN()) ?
															context.ASSIGN().getText() :
															null,
													Objects.nonNull(context.expression()) ?
															join(context.expression()) :
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
										org.daiitech.naftah.parser.NaftahParser.SimpleCallContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											SimpleCallContext::ID -> %s
											SimpleCallContext::COLON -> %s
											SimpleCallContext::COLON -> %s
											SimpleCallContext::ID -> %s
											"""
										.formatted(
													Objects.nonNull(context.ID()) ? context.ID(0).getText() : null,
													Objects.nonNull(context.COLON(0)) ?
															context.COLON(0).getText() :
															null,
													Objects.nonNull(context.COLON(1)) ?
															context.COLON(1).getText() :
															null,
													Objects.nonNull(context.ID()) ? context.ID(1).getText() : null));

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.QualifiedNameCallContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											QualifiedNameCallContext::qualifiedName -> {
												%s
											}
											QualifiedNameCallContext::COLON -> %s
											QualifiedNameCallContext::COLON -> %s
											QualifiedNameCallContext::ID -> %s
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
											FunctionCallContext::primaryCall -> %s
											FunctionCallContext::callSegment -> {
												%s
											}
											"""
										.formatted( Objects.nonNull(context.primaryCall()) ?
															context.primaryCall().getText() :
															null,
													join(context.callSegment()))
		);

	}

	public static String logExecution(boolean doLog, org.daiitech.naftah.parser.NaftahParser.CallSegmentContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											FunctionCallContext::COLON -> {
											%s
											}
											FunctionCallContext::primaryCall -> %s
											"""
										.formatted( join(context.COLON()),
													Objects.nonNull(context.primaryCall()) ?
															context.primaryCall().getText() :
															null)
		);

	}

	public static String logExecution(boolean doLog, org.daiitech.naftah.parser.NaftahParser.InitCallContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
																		InitCallContext::AT_SIGN -> %s
																		InitCallContext::ID -> %s
																		InitCallContext::qualifiedName -> %s
																		InitCallContext::targetExecutableIndex -> %s
																		InitCallContext::LPAREN -> %s
																		InitCallContext::argumentList -> {
																			%s
																		}
																		InitCallContext::RPAREN -> %s
											InitCallContext::callSegment -> {
												%s
											}
											"""
										.formatted( Objects.nonNull(context.AT_SIGN()) ?
															context.AT_SIGN().getText() :
															null,
													Objects.nonNull(context.ID()) ?
															context.ID().getText() :
															null,
													Objects.nonNull(context.qualifiedName()) ?
															context.qualifiedName().getText() :
															null,
													Objects.nonNull(context.targetExecutableIndex()) ?
															context.targetExecutableIndex().getText() :
															null,
													Objects.nonNull(context.LPAREN()) ?
															context.LPAREN().getText() :
															null,
													Objects.nonNull(context.argumentList()) ?
															context.argumentList().getText() :
															null,
													Objects.nonNull(context.RPAREN()) ?
															context.RPAREN().getText() :
															null,
													join(context.callSegment()))
		);
	}

	public static String logExecution(boolean doLog, org.daiitech.naftah.parser.NaftahParser.PrimaryCallContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											PrimaryCallContext::ID -> %s
											PrimaryCallContext::qualifiedCall -> %s
											PrimaryCallContext::targetExecutableIndex -> %s
											PrimaryCallContext::LPAREN -> %s
											PrimaryCallContext::argumentList -> {
											%s
											}
											PrimaryCallContext::RPAREN -> %s
											"""
										.formatted( Objects.nonNull(context.ID()) ? context.ID().getText() : null,
													Objects.nonNull(context.qualifiedCall()) ?
															context.qualifiedCall().getText() :
															null,
													Objects.nonNull(context.targetExecutableIndex()) ?
															context.targetExecutableIndex().getText() :
															null,
													Objects.nonNull(context.LPAREN()) ?
															context.LPAREN().getText() :
															null,
													Objects.nonNull(context.argumentList()) ?
															context.argumentList().getText() :
															null,
													Objects.nonNull(context.RPAREN()) ?
															context.RPAREN().getText() :
															null)
		);
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


	public static String logExecution(boolean doLog, org.daiitech.naftah.parser.NaftahParser.ScopeBlockContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											BlockContext::SCOPE -> %s
											BlockContext::ORDERED -> %s
											BlockContext::block -> {
												%s
											}
											"""
										.formatted( Objects.nonNull(context.SCOPE()) ?
															context.SCOPE().getText() :
															null,
													Objects.nonNull(context.ORDERED()) ?
															context.ORDERED().getText() :
															null,
													Objects.nonNull(context.block()) ?
															context.block().getText() :
															null));

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
											BlockContext::END -> {
														%s
													}
											"""
										.formatted( Objects.nonNull(context.LBRACE()) ?
															context.LBRACE().getText() :
															null,
													join(context.statement()),
													context.RBRACE(),
													join(context.END())));

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
										org.daiitech.naftah.parser.NaftahParser.TypeExpressionContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											TypeExpressionContext::type -> {
												%s
											}
											"""
										.formatted(Objects.nonNull(context.type()) ?
												context.type().getText() :
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

	public static String logExecution(boolean doLog, org.daiitech.naftah.parser.NaftahParser.ComplexTypeContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											ComplexTypeContext::complexBuiltIn -> {
													%s
											}
											"""
										.formatted(Objects.nonNull(context.complexBuiltIn()) ?
												context.complexBuiltIn().getText() :
												null));

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.ComplexBuiltInContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											ComplexBuiltInContext::STRUCT -> %s
											ComplexBuiltInContext::IMPLEMENTATION -> %s
											ComplexBuiltInContext::PAIR -> %s
											ComplexBuiltInContext::TRIPLE -> %s
											ComplexBuiltInContext::LIST -> %s
											ComplexBuiltInContext::TUPLE -> %s
											ComplexBuiltInContext::SET -> %s
											ComplexBuiltInContext::MAP -> %s
											ComplexBuiltInContext::LT_SIGN -> %s
											ComplexBuiltInContext::type -> {
												%s
											}
											ComplexBuiltInContext::GT_SIGN -> %s
											"""
										.formatted( Objects.nonNull(context.STRUCT()) ?
															context.STRUCT().getText() :
															null,
													Objects.nonNull(context.IMPLEMENTATION()) ?
															context.IMPLEMENTATION().getText() :
															null,
													Objects.nonNull(context.PAIR()) ? context.PAIR().getText() : null,
													Objects.nonNull(context.TRIPLE()) ?
															context.TRIPLE().getText() :
															null,
													Objects.nonNull(context.LIST()) ? context.LIST().getText() : null,
													Objects.nonNull(context.TUPLE()) ? context.TUPLE().getText() : null,
													Objects.nonNull(context.SET()) ?
															context.SET().getText() :
															null,
													Objects.nonNull(context.MAP()) ?
															context.MAP().getText() :
															null,
													Objects.nonNull(context.LT_TYPE_SIGN()) ?
															context.LT_TYPE_SIGN().getText() :
															null,
													join(context.type()),
													Objects.nonNull(context.GT_TYPE_SIGN()) ?
															context.GT_TYPE_SIGN().getText() :
															null
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
											QualifiedNameTypeContext::ID -> %s
											QualifiedNameTypeContext::qualifiedName -> %s
											"""
										.formatted( Objects.nonNull(context.ID()) ?
															context.ID().getText() :
															null,
													Objects.nonNull(context.qualifiedName()) ?
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
											RelationalExpressionContext::shiftExpression -> {
											%s
											}
											RelationalExpressionContext::LT -> %s
											RelationalExpressionContext::LE -> %s
											RelationalExpressionContext::GT -> %s
											RelationalExpressionContext::GE -> %s
											"""
										.formatted( join(context.shiftExpression()),
													join(context.LT()),
													join(context.LE()),
													join(context.GT()),
													join(context.GE())));
	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.ShiftExpressionContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											ShiftExpressionContext::additiveExpression -> {
											%s
											}
											ShiftExpressionContext::BITWISE_SHL -> %s
											ShiftExpressionContext::BITWISE_SHR -> %s
											ShiftExpressionContext::BITWISE_USHR -> %s
											"""
										.formatted( join(context.additiveExpression()),
													join(context.BITWISE_SHL()),
													join(context.BITWISE_SHR()),
													join(context.BITWISE_USHR())));
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
										.formatted( join(context.powerExpression()),
													join(context.MUL()),
													join(context.DIV()),
													join(context.MOD()),
													join(context.ELEMENTWISE_MUL()),
													join(context.ELEMENTWISE_DIV()),
													join(context.ELEMENTWISE_MOD())
										));
	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.PowerExpressionContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											MultiplicativeExpressionContext::unaryExpression -> {
											%s
											}
											MultiplicativeExpressionContext::POW -> %s
											MultiplicativeExpressionContext::powerExpression -> {
											%s
											}
											"""
										.formatted( Objects.nonNull(context.unaryExpression()) ?
															context.unaryExpression().getText() :
															null,
													Objects.nonNull(context.POW()) ?
															context.POW().getText() :
															null,
													Objects.nonNull(context.powerExpression()) ?
															context.powerExpression().getText() :
															null)
		);
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
										org.daiitech.naftah.parser.NaftahParser.SpawnUnaryExpressionContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											SpawnUnaryExpressionContext::SPAWN -> %s
											SpawnUnaryExpressionContext::unaryExpression -> {
											%s
											}
											"""
										.formatted( Objects.nonNull(context.SPAWN()) ?
															context.SPAWN().getText() :
															null,
													Objects.nonNull(context.unaryExpression()) ?
															context.unaryExpression().getText() :
															null));
	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.AwaitUnaryExpressionContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											AwaitUnaryExpressionContext::AWAIT -> %s
											AwaitUnaryExpressionContext::unaryExpression -> {
											%s
											}
											"""
										.formatted( Objects.nonNull(context.AWAIT()) ?
															context.AWAIT().getText() :
															null,
													Objects.nonNull(context.unaryExpression()) ?
															context.unaryExpression().getText() :
															null));
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
											CollectionAccessContext::collectionAccessIndex -> %s
											CollectionAccessContext::RBRACK -> %s"""
										.formatted( Objects.nonNull(context.ID()) ?
															context.ID().getText() :
															null,
													join(context.QUESTION()),
													join(context.LBRACK()),
													join(context.collectionAccessIndex()),
													join(context.RBRACK())));
	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.CollectionAccessIndexContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											CollectionAccessIndexContext::NUMBER -> %s
											CollectionAccessIndexContext::ID -> %s"""
										.formatted( Objects.nonNull(context.NUMBER()) ?
															context.NUMBER().getText() :
															null,
													Objects.nonNull(context.ID()) ?
															context.ID().getText() :
															null));
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


	public static String logExecution(boolean doLog, org.daiitech.naftah.parser.NaftahParser.EmptyObjectContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											EmptyObjectContext::AT_SIGN -> %s
											EmptyObjectContext::LBRACE -> %s
											EmptyObjectContext::RBRACE -> %s
											"""
										.formatted(
													Objects.nonNull(context.AT_SIGN()) ?
															context.AT_SIGN().getText() :
															null,
													Objects.nonNull(context.LBRACE()) ?
															context.LBRACE().getText() :
															null,
													Objects.nonNull(context.RBRACE()) ?
															context.RBRACE().getText() :
															null

										));
	}

	public static String logExecution(boolean doLog, org.daiitech.naftah.parser.NaftahParser.ObjectValueContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											ObjectValueContext::AT_SIGN -> %s
											ObjectValueContext::LBRACE -> %s
											ObjectValueContext::objectFields -> {
											%s
											}
											ObjectValueContext::RBRACE -> %s
											"""
										.formatted(
													Objects.nonNull(context.AT_SIGN()) ?
															context.AT_SIGN().getText() :
															null,
													Objects.nonNull(context.LBRACE()) ?
															context.LBRACE().getText() :
															null,
													Objects.nonNull(context.objectFields()) ?
															context.objectFields().getText() :
															null,
													Objects.nonNull(context.RBRACE()) ?
															context.RBRACE().getText() :
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
											TupleValueContext::tupleElements -> {
											%s
											}
											TupleValueContext::RPAREN -> %s
											"""
										.formatted(
													Objects.nonNull(context.LPAREN()) ?
															context.LPAREN().getText() :
															null,
													Objects.nonNull(context.tupleElements()) ?
															context.tupleElements().getText() :
															null,
													Objects.nonNull(context.RPAREN()) ?
															context.RPAREN().getText() :
															null

										));
	}

	public static String logExecution(boolean doLog, org.daiitech.naftah.parser.NaftahParser.EmptySetContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											EmptySetContext::ORDERED -> %s
											EmptySetContext::HASH_SIGN -> %s
											EmptySetContext::LBRACE -> %s
											EmptySetContext::RBRACE -> %s
											"""
										.formatted(
													Objects.nonNull(context.ORDERED()) ?
															context.ORDERED().getText() :
															null,
													Objects.nonNull(context.HASH_SIGN()) ?
															context.HASH_SIGN().getText() :
															null,
													Objects.nonNull(context.LBRACE()) ?
															context.LBRACE().getText() :
															null,
													Objects.nonNull(context.RBRACE()) ?
															context.RBRACE().getText() :
															null

										));
	}

	public static String logExecution(boolean doLog, org.daiitech.naftah.parser.NaftahParser.SetValueContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											SetValueContext::ORDERED -> %s
											SetValueContext::HASH_SIGN -> %s
											SetValueContext::LBRACE -> %s
											SetValueContext::elements -> {
											%s
											}
											SetValueContext::RBRACE -> %s
											"""
										.formatted(
													Objects.nonNull(context.ORDERED()) ?
															context.ORDERED().getText() :
															null,
													Objects.nonNull(context.HASH_SIGN()) ?
															context.HASH_SIGN().getText() :
															null,
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

	public static String logExecution(boolean doLog, org.daiitech.naftah.parser.NaftahParser.EmptyMapContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											EmptyMapContext::ORDERED -> %s
											EmptyMapContext::DOLLAR_SIGN -> %s
											EmptyMapContext::LBRACE -> %s
											EmptyMapContext::RBRACE -> %s
											"""
										.formatted(
													Objects.nonNull(context.ORDERED()) ?
															context.ORDERED().getText() :
															null,
													Objects.nonNull(context.DOLLAR_SIGN()) ?
															context.DOLLAR_SIGN().getText() :
															null,
													Objects.nonNull(context.LBRACE()) ?
															context.LBRACE().getText() :
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
											MapValueContext::ORDERED -> %s
											MapValueContext::DOLLAR_SIGN -> %s
											MapValueContext::LBRACE -> %s
											MapValueContext::keyValuePairs -> {
											%s
											}
											MapValueContext::RBRACE -> %s
											"""
										.formatted(
													Objects.nonNull(context.ORDERED()) ?
															context.ORDERED().getText() :
															null,
													Objects.nonNull(context.DOLLAR_SIGN()) ?
															context.DOLLAR_SIGN().getText() :
															null,
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
										org.daiitech.naftah.parser.NaftahParser.TupleSingleElementContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											TupleSingleElementContext::expression -> %s
											TupleSingleElementContext::COMMA -> %s
											TupleSingleElementContext::SEMI -> %s
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
											MultipleElementsContext::collectionMultipleElements::expression -> {
											%s
											}
											MultipleElementsContext::collectionMultipleElements::COMMA -> %s
											MultipleElementsContext::collectionMultipleElements::SEMI -> %s
											"""
										.formatted(
													join(context.collectionMultipleElements().expression()),
													join(context.collectionMultipleElements().COMMA()),
													join(context.collectionMultipleElements().SEMI())
										));
	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.TupleMultipleElementsContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											TupleMultipleElementsContext::collectionMultipleElements::expression -> {
											%s
											}
											TupleMultipleElementsContext::collectionMultipleElements::COMMA -> %s
											TupleMultipleElementsContext::collectionMultipleElements::SEMI -> %s
											"""
										.formatted(
													join(context.collectionMultipleElements().expression()),
													join(context.collectionMultipleElements().COMMA()),
													join(context.collectionMultipleElements().SEMI())
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


	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.TryStatementStatementContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											TryStatementStatementContext::tryStatement -> {
											%s
											}
											"""
										.formatted(
													Objects.nonNull(context.tryStatement()) ?
															context.tryStatement().getText() :
															null
										));

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.TryStatementWithTryCasesContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											TryStatementWithTryCasesContext::TRY -> %s
											TryStatementWithTryCasesContext::LPAREN -> %s
											TryStatementWithTryCasesContext::expression -> {
											%s
											}
											TryStatementWithTryCasesContext::RPAREN -> %s
											TryStatementWithTryCasesContext::LBRACE -> %s
											TryStatementWithTryCasesContext::tryCases -> {
											%s
											}
											TryStatementWithTryCasesContext::RBRACE -> %s
											"""
										.formatted(
													Objects.nonNull(context.TRY()) ? context.TRY().getText() : null,
													Objects.nonNull(context.LPAREN()) ?
															context.LPAREN().getText() :
															null,
													Objects.nonNull(context.expression()) ?
															context.expression().getText() :
															null,
													Objects.nonNull(context.RPAREN()) ?
															context.RPAREN().getText() :
															null,
													Objects.nonNull(context.LBRACE()) ?
															context.LBRACE().getText() :
															null,
													Objects.nonNull(context.tryCases()) ?
															context.tryCases().getText() :
															null,
													Objects.nonNull(context.RBRACE()) ?
															context.RBRACE().getText() :
															null
										));

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.OkCaseContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											OkCaseContext::OK -> %s
											OkCaseContext::LPAREN -> %s
											OkCaseContext::ID -> %s
											OkCaseContext::RPAREN -> %s
											OkCaseContext::DO -> %s
											OkCaseContext::ARROW -> %s
											OkCaseContext::block -> {
											%s
											}
											OkCaseContext::expression -> {
											%s
											}
											"""
										.formatted(
													Objects.nonNull(context.OK()) ? context.OK().getText() : null,
													Objects.nonNull(context.LPAREN()) ?
															context.LPAREN().getText() :
															null,
													Objects.nonNull(context.ID()) ? context.ID().getText() : null,
													Objects.nonNull(context.RPAREN()) ?
															context.RPAREN().getText() :
															null,
													Objects.nonNull(context.DO()) ? context.DO().getText() : null,
													Objects.nonNull(context.ARROW()) ? context.ARROW().getText() : null,
													Objects.nonNull(context.block()) ? context.block().getText() : null,
													Objects.nonNull(context.expression()) ?
															context.expression().getText() :
															null
										));

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.ErrorCaseContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											ErrorCaseContext::ERROR -> %s
											ErrorCaseContext::LPAREN -> %s
											ErrorCaseContext::ID -> %s
											ErrorCaseContext::RPAREN -> %s
											ErrorCaseContext::DO -> %s
											ErrorCaseContext::ARROW -> %s
											ErrorCaseContext::block -> {
											%s
											}
											ErrorCaseContext::expression -> {
											%s
											}
											"""
										.formatted(
													Objects.nonNull(context.ERROR()) ? context.ERROR().getText() : null,
													Objects.nonNull(context.LPAREN()) ?
															context.LPAREN().getText() :
															null,
													Objects.nonNull(context.ID()) ? context.ID().getText() : null,
													Objects.nonNull(context.RPAREN()) ?
															context.RPAREN().getText() :
															null,
													Objects.nonNull(context.DO()) ? context.DO().getText() : null,
													Objects.nonNull(context.ARROW()) ? context.ARROW().getText() : null,
													Objects.nonNull(context.block()) ? context.block().getText() : null,
													Objects.nonNull(context.expression()) ?
															context.expression().getText() :
															null
										));
	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.TryStatementWithOptionCasesContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											TryStatementWithOptionCasesContext::TRY -> %s
											TryStatementWithOptionCasesContext::LPAREN -> %s
											TryStatementWithOptionCasesContext::expression -> {
											%s
											}
											TryStatementWithOptionCasesContext::RPAREN -> %s
											TryStatementWithOptionCasesContext::LBRACE -> %s
											TryStatementWithOptionCasesContext::optionCases -> {
											%s
											}
											TryStatementWithOptionCasesContext::RBRACE -> %s
											"""
										.formatted(
													Objects.nonNull(context.TRY()) ? context.TRY().getText() : null,
													Objects.nonNull(context.LPAREN()) ?
															context.LPAREN().getText() :
															null,
													Objects.nonNull(context.expression()) ?
															context.expression().getText() :
															null,
													Objects.nonNull(context.RPAREN()) ?
															context.RPAREN().getText() :
															null,
													Objects.nonNull(context.LBRACE()) ?
															context.LBRACE().getText() :
															null,
													Objects.nonNull(context.optionCases()) ?
															context.optionCases().getText() :
															null,
													Objects.nonNull(context.RBRACE()) ?
															context.RBRACE().getText() :
															null
										));

	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.SomeCaseContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											SomeCaseContext::SOME -> %s
											SomeCaseContext::LPAREN -> %s
											SomeCaseContext::ID -> %s
											SomeCaseContext::RPAREN -> %s
											SomeCaseContext::DO -> %s
											SomeCaseContext::ARROW -> %s
											SomeCaseContext::block -> {
											%s
											}
											SomeCaseContext::expression -> {
											%s
											}
											"""
										.formatted(
													Objects.nonNull(context.SOME()) ? context.SOME().getText() : null,
													Objects.nonNull(context.LPAREN()) ?
															context.LPAREN().getText() :
															null,
													Objects.nonNull(context.ID()) ? context.ID().getText() : null,
													Objects.nonNull(context.RPAREN()) ?
															context.RPAREN().getText() :
															null,
													Objects.nonNull(context.DO()) ? context.DO().getText() : null,
													Objects.nonNull(context.ARROW()) ? context.ARROW().getText() : null,
													Objects.nonNull(context.block()) ? context.block().getText() : null,
													Objects.nonNull(context.expression()) ?
															context.expression().getText() :
															null
										));


	}

	public static String logExecution(  boolean doLog,
										org.daiitech.naftah.parser.NaftahParser.NoneCaseContext ctx) {
		return doLogExecution(  doLog,
								ctx,
								context -> """
											OkCaseContext::NONE -> %s
											OkCaseContext::DO -> %s
											OkCaseContext::ARROW -> %s
											OkCaseContext::block -> {
											%s
											}
											OkCaseContext::expression -> {
											%s
											}
											"""
										.formatted(
													Objects.nonNull(context.NONE()) ? context.NONE().getText() : null,
													Objects.nonNull(context.DO()) ? context.DO().getText() : null,
													Objects.nonNull(context.ARROW()) ? context.ARROW().getText() : null,
													Objects.nonNull(context.block()) ? context.block().getText() : null,
													Objects.nonNull(context.expression()) ?
															context.expression().getText() :
															null
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
