package org.daiitech.naftah.parser;

import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.tree.ParseTree;
import org.daiitech.naftah.builtin.Builtin;
import org.daiitech.naftah.builtin.lang.DeclaredFunction;
import org.daiitech.naftah.builtin.lang.DeclaredImplementation;
import org.daiitech.naftah.builtin.lang.DeclaredParameter;
import org.daiitech.naftah.builtin.lang.DeclaredVariable;
import org.daiitech.naftah.builtin.lang.DynamicNumber;
import org.daiitech.naftah.builtin.lang.JvmClassInitializer;
import org.daiitech.naftah.builtin.lang.NaN;
import org.daiitech.naftah.builtin.lang.NaftahObject;
import org.daiitech.naftah.builtin.lang.None;
import org.daiitech.naftah.builtin.lang.Result;
import org.daiitech.naftah.builtin.time.NaftahTemporalAmount;
import org.daiitech.naftah.builtin.time.NaftahTemporalPoint;
import org.daiitech.naftah.builtin.utils.NumberUtils;
import org.daiitech.naftah.builtin.utils.ObjectUtils;
import org.daiitech.naftah.builtin.utils.concurrent.Actor;
import org.daiitech.naftah.builtin.utils.concurrent.Channel;
import org.daiitech.naftah.builtin.utils.concurrent.Task;
import org.daiitech.naftah.builtin.utils.op.BinaryOperation;
import org.daiitech.naftah.builtin.utils.op.UnaryOperation;
import org.daiitech.naftah.builtin.utils.tuple.ImmutablePair;
import org.daiitech.naftah.builtin.utils.tuple.MutablePair;
import org.daiitech.naftah.builtin.utils.tuple.NTuple;
import org.daiitech.naftah.builtin.utils.tuple.Pair;
import org.daiitech.naftah.builtin.utils.tuple.Triple;
import org.daiitech.naftah.builtin.utils.tuple.Tuple;
import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.parser.time.NaftahDateParserHelper;
import org.daiitech.naftah.utils.reflect.type.JavaType;
import org.daiitech.naftah.utils.reflect.type.TypeReference;
import org.daiitech.naftah.utils.script.ScriptUtils;

import static org.daiitech.naftah.Naftah.INSIDE_REPL_PROPERTY;
import static org.daiitech.naftah.builtin.utils.CollectionUtils.getElementAt;
import static org.daiitech.naftah.builtin.utils.CollectionUtils.newNaftahIndexOutOfBoundsBugError;
import static org.daiitech.naftah.builtin.utils.CollectionUtils.setElementAt;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.applyOperation;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.getJavaType;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.isTruthy;
import static org.daiitech.naftah.builtin.utils.op.BinaryOperation.ADD;
import static org.daiitech.naftah.builtin.utils.op.BinaryOperation.GREATER_THAN_EQUALS;
import static org.daiitech.naftah.builtin.utils.op.BinaryOperation.INSTANCE_OF;
import static org.daiitech.naftah.builtin.utils.op.BinaryOperation.LESS_THAN;
import static org.daiitech.naftah.builtin.utils.op.BinaryOperation.LESS_THAN_EQUALS;
import static org.daiitech.naftah.builtin.utils.op.BinaryOperation.SUBTRACT;
import static org.daiitech.naftah.builtin.utils.op.UnaryOperation.DECREMENT;
import static org.daiitech.naftah.builtin.utils.op.UnaryOperation.INCREMENT;
import static org.daiitech.naftah.builtin.utils.op.UnaryOperation.POST;
import static org.daiitech.naftah.builtin.utils.op.UnaryOperation.PRE;
import static org.daiitech.naftah.builtin.utils.op.UnaryOperation.PRE_DECREMENT;
import static org.daiitech.naftah.builtin.utils.op.UnaryOperation.PRE_INCREMENT;
import static org.daiitech.naftah.builtin.utils.op.UnaryOperation.SIZE_OF;
import static org.daiitech.naftah.builtin.utils.op.UnaryOperation.TYPE_OF;
import static org.daiitech.naftah.builtin.utils.tuple.NTuple.newNaftahBugNullError;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidLoopLabelError;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahExpressionsDeclarationsSizeMismatchErrorError;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahInvocableListFoundError;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahInvocableNotFoundError;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahSingleExpressionAssignmentError;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahSpecifiedTypesExceedVariableNamesError;
import static org.daiitech.naftah.parser.DefaultContext.CURRENT_TASK_SCOPE;
import static org.daiitech.naftah.parser.DefaultContext.LOOP_STACK;
import static org.daiitech.naftah.parser.DefaultContext.cleanClassThreadLocals;
import static org.daiitech.naftah.parser.DefaultContext.currentLoopLabel;
import static org.daiitech.naftah.parser.DefaultContext.defineImport;
import static org.daiitech.naftah.parser.DefaultContext.deregisterContext;
import static org.daiitech.naftah.parser.DefaultContext.endScope;
import static org.daiitech.naftah.parser.DefaultContext.generateCallId;
import static org.daiitech.naftah.parser.DefaultContext.getCurrentContext;
import static org.daiitech.naftah.parser.DefaultContext.getVariable;
import static org.daiitech.naftah.parser.DefaultContext.loopContainsLabel;
import static org.daiitech.naftah.parser.DefaultContext.newNaftahBugExistentFunctionArgumentError;
import static org.daiitech.naftah.parser.DefaultContext.newNaftahBugExistentFunctionError;
import static org.daiitech.naftah.parser.DefaultContext.newNaftahBugExistentFunctionParameterError;
import static org.daiitech.naftah.parser.DefaultContext.newNaftahBugForeachTargetDuplicatesError;
import static org.daiitech.naftah.parser.DefaultContext.popLoop;
import static org.daiitech.naftah.parser.DefaultContext.pushLoop;
import static org.daiitech.naftah.parser.DefaultContext.startScope;
import static org.daiitech.naftah.parser.LoopSignal.BREAK;
import static org.daiitech.naftah.parser.LoopSignal.CONTINUE;
import static org.daiitech.naftah.parser.LoopSignal.RETURN;
import static org.daiitech.naftah.parser.NaftahParserHelper.accessObjectUsingQualifiedName;
import static org.daiitech.naftah.parser.NaftahParserHelper.checkInsideLoop;
import static org.daiitech.naftah.parser.NaftahParserHelper.checkLoopSignal;
import static org.daiitech.naftah.parser.NaftahParserHelper.getBlockContext;
import static org.daiitech.naftah.parser.NaftahParserHelper.getFirstChildOfType;
import static org.daiitech.naftah.parser.NaftahParserHelper.getFormattedTokenSymbols;
import static org.daiitech.naftah.parser.NaftahParserHelper.getQualifiedName;
import static org.daiitech.naftah.parser.NaftahParserHelper.getRootContext;
import static org.daiitech.naftah.parser.NaftahParserHelper.handleDeclaration;
import static org.daiitech.naftah.parser.NaftahParserHelper.hasAnyParentOfType;
import static org.daiitech.naftah.parser.NaftahParserHelper.hasChild;
import static org.daiitech.naftah.parser.NaftahParserHelper.hasChildOrSubChildOfType;
import static org.daiitech.naftah.parser.NaftahParserHelper.hasParentOfType;
import static org.daiitech.naftah.parser.NaftahParserHelper.invokeJvmClassInitializer;
import static org.daiitech.naftah.parser.NaftahParserHelper.matchImplementationName;
import static org.daiitech.naftah.parser.NaftahParserHelper.prepareDeclaredFunction;
import static org.daiitech.naftah.parser.NaftahParserHelper.setForeachVariables;
import static org.daiitech.naftah.parser.NaftahParserHelper.setObjectUsingQualifiedName;
import static org.daiitech.naftah.parser.NaftahParserHelper.shouldBreakStatementsLoop;
import static org.daiitech.naftah.parser.NaftahParserHelper.spawnTask;
import static org.daiitech.naftah.parser.NaftahParserHelper.validateVariableExistence;
import static org.daiitech.naftah.parser.NaftahParserHelper.visitContext;
import static org.daiitech.naftah.parser.NaftahParserHelper.visitFunctionCallInChain;
import static org.daiitech.naftah.utils.reflect.ClassUtils.QUALIFIED_CALL_SEPARATOR;
import static org.daiitech.naftah.utils.reflect.ClassUtils.QUALIFIED_NAME_SEPARATOR;

/**
 * The default implementation of the Naftah language visitor.
 * <p>
 * This class visits and evaluates parse tree nodes generated by the Naftah parser.
 * It provides the interpretation logic for various language constructs such as expressions,
 * statements, operations, and control flows.
 * </p>
 *
 * @author Chakib Daii
 */
public class DefaultNaftahParserVisitor extends org.daiitech.naftah.parser.NaftahParserBaseVisitor<Object> {

	/**
	 * Logger instance for logging parser visitor activities.
	 */
	static final Logger LOGGER = Logger.getLogger("DefaultNaftahParserVisitor");

	/**
	 * The vocabulary used by the parser, typically containing mappings from token names to their
	 * symbolic and literal representations. This is often generated by ANTLR and used for error
	 * reporting, syntax highlighting, and introspection.
	 * <p>
	 * This field is usually initialized by the parser class generated by ANTLR and provides access
	 * to the full vocabulary of the language grammar, including terminals and their associated
	 * names.
	 */
	public static Vocabulary PARSER_VOCABULARY;
	/**
	 * The ANTLR parser instance that produces the parse tree.
	 */
	private final org.daiitech.naftah.parser.NaftahParser parser;
	private final NTuple args;
	private final String ARGS_VAR_NAME = "وسائط";
	private final String ARGS_SIZE = "عدد_الوسائط";
	private final String ACTOR_MESSAGE = "رسالة_الممثل";
	/**
	 * Current depth in the parse tree traversal.
	 */
	private int depth = 0;

	/**
	 * Constructs the visitor with a given parser.
	 *
	 * @param parser the ANTLR-generated Naftah parser instance
	 */
	public DefaultNaftahParserVisitor(org.daiitech.naftah.parser.NaftahParser parser, List<String> args) {
		this.parser = parser;
		this.args = NTuple.of(args);
		PARSER_VOCABULARY = parser.getVocabulary();
	}

	/**
	 * Starts the visiting process by parsing the program and
	 * visiting the resulting parse tree.
	 *
	 * @return the result of visiting the parse tree
	 */
	public Object visit() {
		// Parse the input and get the parse tree
		ParseTree tree = parser.program();
		return visit(tree);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitProgram(org.daiitech.naftah.parser.NaftahParser.ProgramContext ctx) {
		return visitContext(
							this,
							"visitProgram",
							getRootContext(ctx),
							ctx,
							(defaultNaftahParserVisitor, currentContext, programContext) -> {
								currentContext
										.setVariable(   ARGS_VAR_NAME,
														DeclaredVariable
																.of(currentContext.depth,
																	programContext,
																	ARGS_VAR_NAME,
																	true,
																	JavaType.of(NTuple.class),
																	args));
								currentContext
										.setVariable(   ARGS_SIZE,
														DeclaredVariable
																.of(currentContext.depth,
																	programContext,
																	ARGS_SIZE,
																	true,
																	JavaType.of(int.class),
																	args.arity()));

								defaultNaftahParserVisitor.depth = currentContext.depth;
								Object result = None.get();
								try {
									for (org.daiitech.naftah.parser.NaftahParser.StatementContext statement : programContext
											.statement()) {
										result = defaultNaftahParserVisitor.visit(statement); // Visit each statement in the
										// program
										// break program after executing a return statement
										if (shouldBreakStatementsLoop(currentContext, statement, result)) {
											break;
										}
									}
									return result;
								}
								finally {
									deregisterContext();
									if (!Boolean.getBoolean(INSIDE_REPL_PROPERTY)) {
										currentContext.cleanThreadLocals();
										cleanClassThreadLocals();
									}
								}
							}
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitImportStatementStatement(org.daiitech.naftah.parser.NaftahParser.ImportStatementStatementContext ctx) {
		return visitContext(
							this,
							"visitImportStatementStatement",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								importStatementStatementContext) -> defaultNaftahParserVisitor
										.visit(
												importStatementStatementContext.importStatement())
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitImportStatementAsAlias(org.daiitech.naftah.parser.NaftahParser.ImportStatementAsAliasContext ctx) {
		return visitContext(
							this,
							"visitImportStatementAsAlias",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								importStatementAsAliasContext) -> {
								String id = importStatementAsAliasContext.ID().getText();
								String alias = importStatementAsAliasContext.importAlias().ID().getText();
								defineImport(currentContext, importStatementAsAliasContext, alias, id);
								return None.get();
							}
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitGroupedImportStatement(org.daiitech.naftah.parser.NaftahParser.GroupedImportStatementContext ctx) {
		return visitContext(
							this,
							"visitGroupedImportStatement",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								groupedImportStatementContext) -> {
								String qualifiedName = (String) defaultNaftahParserVisitor
										.visit(groupedImportStatementContext.qualifiedName());
								if (Objects.nonNull(groupedImportStatementContext.imports())) {
									// pair of alias -> id
									//noinspection unchecked
									var imports = (Set<Pair<String, String>>) defaultNaftahParserVisitor
											.visit(groupedImportStatementContext.imports());


									imports
											.forEach(currentImport -> defineImport( currentContext,
																					groupedImportStatementContext,
																					Objects
																							.nonNull(currentImport
																									.getLeft()) ?
																											currentImport
																													.getLeft() :
																											currentImport
																													.getRight(),
																					String
																							.join(
																									groupedImportStatementContext
																											.COLON()
																											.size() == 1 ?
																													QUALIFIED_NAME_SEPARATOR :
																													QUALIFIED_CALL_SEPARATOR,
																									qualifiedName,
																									currentImport
																											.getRight())));
								}
								else {
									if (Objects.nonNull(groupedImportStatementContext.importAlias())) {
										String alias = groupedImportStatementContext.importAlias().ID().getText();
										defineImport(   currentContext,
														groupedImportStatementContext,
														alias,
														qualifiedName);
									}
									else {
										var qualifiedNameParts = qualifiedName.split(QUALIFIED_NAME_SEPARATOR);
										String alias = qualifiedNameParts[qualifiedNameParts.length - 1];
										defineImport(   currentContext,
														groupedImportStatementContext,
														alias,
														qualifiedName);
									}
								}
								return None.get();
							}
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitQualifiedCallImportStatement(org.daiitech.naftah.parser.NaftahParser.QualifiedCallImportStatementContext ctx) {
		return visitContext(
							this,
							"visitQualifiedCallImportStatement",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								qualifiedCallImportStatementContext) -> {
								String qualifiedCall = (String) defaultNaftahParserVisitor
										.visit(qualifiedCallImportStatementContext.qualifiedCall());
								if (Objects.nonNull(qualifiedCallImportStatementContext.importAlias())) {
									String alias = qualifiedCallImportStatementContext.importAlias().ID().getText();
									defineImport(   currentContext,
													qualifiedCallImportStatementContext,
													alias,
													qualifiedCall);
								}
								else {
									var qualifiedCallParts = qualifiedCall.split(QUALIFIED_CALL_SEPARATOR);
									String alias = qualifiedCallParts[qualifiedCallParts.length - 1];
									defineImport(   currentContext,
													qualifiedCallImportStatementContext,
													alias,
													qualifiedCall);
								}
								return None.get();
							}
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitImports(org.daiitech.naftah.parser.NaftahParser.ImportsContext ctx) {
		return visitContext(
							this,
							"visitImports",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								importsContext) -> {
								var imports = new HashSet<Pair<String, String>>();
								if (Objects.nonNull(importsContext.importElements())) {
									for (org.daiitech.naftah.parser.NaftahParser.CallableImportElementContext callableImportElementContext : importsContext
											.importElements()
											.callableImportElement()) {
										//noinspection unchecked
										imports
												.add((Pair<String, String>) defaultNaftahParserVisitor
														.visit(callableImportElementContext));
									}
								}
								else {
									//noinspection unchecked
									imports
											.add((Pair<String, String>) defaultNaftahParserVisitor
													.visit(
															importsContext.callableImportElement()));
								}
								return imports;
							},
							Set.class
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitCallableImportElement(org.daiitech.naftah.parser.NaftahParser.CallableImportElementContext ctx) {
		return visitContext(
							this,
							"visitCallableImportElement",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								callableImportElementContext) -> {
								String alias = null;
								String importElement;
								if (Objects
										.nonNull(callableImportElementContext.importAlias())) {
									alias = callableImportElementContext
											.importAlias()
											.ID()
											.getText();
								}

								if (Objects.nonNull(callableImportElementContext.ID())) {
									importElement = callableImportElementContext.ID().getText();
									if (Objects.isNull(alias)) {
										alias = importElement;
									}
								}
								else if (Objects
										.nonNull(callableImportElementContext
												.qualifiedName())) {
													importElement = (String) defaultNaftahParserVisitor
															.visit(callableImportElementContext
																	.qualifiedName());

													if (Objects.isNull(alias)) {
														var qualifiedNameParts = importElement
																.split(QUALIFIED_NAME_SEPARATOR);
														alias = qualifiedNameParts[qualifiedNameParts.length - 1];
													}
												}
								else {
									importElement = (String) defaultNaftahParserVisitor
											.visit(callableImportElementContext
													.qualifiedCall());

									if (Objects.isNull(alias)) {
										var qualifiedCallParts = importElement.split(QUALIFIED_CALL_SEPARATOR);
										alias = qualifiedCallParts[qualifiedCallParts.length - 1];
									}
								}

								return ImmutablePair.of(alias, importElement);
							},
							Pair.class
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String visitImportAlias(org.daiitech.naftah.parser.NaftahParser.ImportAliasContext ctx) {
		return visitContext(
							this,
							"visitImportAlias",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								importAliasContext) -> importAliasContext.ID().getText(),
							String.class
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitDeclarationStatement(org.daiitech.naftah.parser.NaftahParser.DeclarationStatementContext ctx) {
		return visitContext(
							this,
							"visitDeclarationStatement",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								declarationStatementContext) -> defaultNaftahParserVisitor
										.visit(
												declarationStatementContext.declaration())
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitChannelDeclarationStatement(org.daiitech.naftah.parser.NaftahParser.ChannelDeclarationStatementContext ctx) {
		return visitContext(
							this,
							"visitChannelDeclarationStatement",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								channelDeclarationStatementContext) -> defaultNaftahParserVisitor
										.visit(
												channelDeclarationStatementContext.channelDeclaration())
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitActorDeclarationStatement(org.daiitech.naftah.parser.NaftahParser.ActorDeclarationStatementContext ctx) {
		return visitContext(
							this,
							"visitDeclarationStatement",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								actorDeclarationStatementContext) -> defaultNaftahParserVisitor
										.visit(
												actorDeclarationStatementContext.actorDeclaration())
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitAssignmentStatement(org.daiitech.naftah.parser.NaftahParser.AssignmentStatementContext ctx) {
		return visitContext(
							this,
							"visitAssignmentStatement",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, assignmentStatementContext) -> {
								boolean creatingObject = hasChildOrSubChildOfType(  assignmentStatementContext,
																					org.daiitech.naftah.parser.NaftahParser.ObjectExpressionContext.class);
								currentContext.setCreatingObject(creatingObject);
								return defaultNaftahParserVisitor.visit(assignmentStatementContext.assignment());
							}
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitFunctionDeclarationStatement(
													org.daiitech.naftah.parser.NaftahParser.FunctionDeclarationStatementContext ctx) {
		return visitContext(
							this,
							"visitFunctionDeclarationStatement",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								functionDeclarationStatementContext) -> defaultNaftahParserVisitor
										.visit(
												functionDeclarationStatementContext.functionDeclaration())
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitIfStatementStatement(org.daiitech.naftah.parser.NaftahParser.IfStatementStatementContext ctx) {
		return visitContext(
							this,
							"visitIfStatementStatement",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								ifStatementStatementContext) -> defaultNaftahParserVisitor
										.visit(
												ifStatementStatementContext.ifStatement())
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitForStatementStatement(org.daiitech.naftah.parser.NaftahParser.ForStatementStatementContext ctx) {
		return visitContext(
							this,
							"visitForStatementStatement",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								forStatementStatementContext) -> defaultNaftahParserVisitor
										.visit(
												forStatementStatementContext.forStatement())
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitWhileStatementStatement(
												org.daiitech.naftah.parser.NaftahParser.WhileStatementStatementContext ctx) {
		return visitContext(
							this,
							"visitWhileStatementStatement",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								whileStatementStatementContext) -> defaultNaftahParserVisitor
										.visit(
												whileStatementStatementContext.whileStatement())
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitRepeatStatementStatement(
												org.daiitech.naftah.parser.NaftahParser.RepeatStatementStatementContext ctx) {
		return visitContext(
							this,
							"visitRepeatStatementStatement",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								repeatStatementStatementContext) -> defaultNaftahParserVisitor
										.visit(
												repeatStatementStatementContext.repeatStatement())
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitCaseStatementStatement(org.daiitech.naftah.parser.NaftahParser.CaseStatementStatementContext ctx) {
		return visitContext(
							this,
							"visitCaseStatementStatement",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								caseStatementStatementContext) -> defaultNaftahParserVisitor
										.visit(
												caseStatementStatementContext.caseStatement())
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitTryStatementStatement(org.daiitech.naftah.parser.NaftahParser.TryStatementStatementContext ctx) {
		return visitContext(
							this,
							"visitTryStatementStatement",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								tryStatementStatementContext) -> defaultNaftahParserVisitor
										.visit(
												tryStatementStatementContext.tryStatement())
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitBreakStatementStatement(
												org.daiitech.naftah.parser.NaftahParser.BreakStatementStatementContext ctx) {
		return visitContext(
							this,
							"visitBreakStatementStatement",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								breakStatementStatementContext) -> defaultNaftahParserVisitor
										.visit(
												breakStatementStatementContext.breakStatement())
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitContinueStatementStatement(
													org.daiitech.naftah.parser.NaftahParser.ContinueStatementStatementContext ctx) {
		return visitContext(
							this,
							"visitContinueStatementStatement",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								continueStatementStatementContext) -> defaultNaftahParserVisitor
										.visit(
												continueStatementStatementContext.continueStatement())
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitReturnStatementStatement(
												org.daiitech.naftah.parser.NaftahParser.ReturnStatementStatementContext ctx) {
		return visitContext(
							this,
							"visitReturnStatementStatement",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								returnStatementStatementContext) -> defaultNaftahParserVisitor
										.visit(
												returnStatementStatementContext.returnStatement())
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitScopeBlockStatement(org.daiitech.naftah.parser.NaftahParser.ScopeBlockStatementContext ctx) {
		return visitContext(
							this,
							"visitScopeBlockStatement",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								scopeBlockStatementContext) -> {
								if (Objects.nonNull(CURRENT_TASK_SCOPE) && Objects.nonNull(CURRENT_TASK_SCOPE.get())) {
									return spawnTask(   currentContext,
														() -> defaultNaftahParserVisitor
																.visit(
																		scopeBlockStatementContext.scopeBlock()),
														currentContext::cleanThreadLocals);
								}
								else {
									return defaultNaftahParserVisitor
											.visit(
													scopeBlockStatementContext.scopeBlock());
								}
							});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitBlockStatement(org.daiitech.naftah.parser.NaftahParser.BlockStatementContext ctx) {
		return visitContext(
							this,
							"visitBlockStatement",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								blockStatementContext) -> defaultNaftahParserVisitor
										.visit(
												blockStatementContext.block())
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitDeclaration(org.daiitech.naftah.parser.NaftahParser.DeclarationContext ctx) {
		return visitContext(
							this,
							"visitDeclaration",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, declarationContext) -> {
								Object result;
								if (Objects.nonNull(declarationContext.singleDeclaration())) {
									result = defaultNaftahParserVisitor.visit(declarationContext.singleDeclaration());
								}
								else {
									result = defaultNaftahParserVisitor
											.visit(declarationContext.multipleDeclarations());
								}
								return result;
							}
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitSingleDeclaration(org.daiitech.naftah.parser.NaftahParser.SingleDeclarationContext ctx) {
		return visitContext(
							this,
							"visitSingleDeclaration",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, singleDeclarationContext) -> {
								validateVariableExistence(currentContext, singleDeclarationContext.ID().getText());
								// variable -> new : flags if this is a new variable or not
								boolean hasType = hasChild(singleDeclarationContext.type());
								return handleDeclaration(   currentContext,
															singleDeclarationContext,
															singleDeclarationContext.ID().getText(),
															hasChild(singleDeclarationContext.CONSTANT()),
															hasChild(singleDeclarationContext
																	.VARIABLE()),
															hasType,
															hasType ?
																	(JavaType) NaftahParserHelper
																			.visit( defaultNaftahParserVisitor,
																					singleDeclarationContext.type()) :
																	JavaType.ofObject()
								);
							}
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitMultipleDeclarations(org.daiitech.naftah.parser.NaftahParser.MultipleDeclarationsContext ctx) {
		return visitContext(
							this,
							"visitMultipleDeclarations",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, multipleDeclarationsContext) -> {
								var variableNames = multipleDeclarationsContext.ID();
								var possibleSpecifiedTypes = multipleDeclarationsContext.type();

								if (possibleSpecifiedTypes.size() > variableNames.size()) {
									throw newNaftahSpecifiedTypesExceedVariableNamesError(  multipleDeclarationsContext
																									.getStart()
																									.getLine(),
																							multipleDeclarationsContext
																									.getStart()
																									.getCharPositionInLine());
								}

								boolean hasConstant = hasChild(multipleDeclarationsContext.CONSTANT());
								boolean hasVariable = hasChild(multipleDeclarationsContext.VARIABLE());
								boolean hasType = !possibleSpecifiedTypes.isEmpty();
								List<Object> declarations = new ArrayList<>();
								for (int i = 0; i < variableNames.size(); i++) {
									var variableName = variableNames.get(i).getText();
									validateVariableExistence(currentContext, variableName);
									declarations
											.add(handleDeclaration( currentContext,
																	multipleDeclarationsContext,
																	variableName,
																	hasConstant,
																	hasVariable,
																	hasType,
																	hasType ?
																			(JavaType) (i < possibleSpecifiedTypes
																					.size() ?
																							NaftahParserHelper
																									.visit( defaultNaftahParserVisitor,
																											possibleSpecifiedTypes
																													.get(i)) :
																							NaftahParserHelper
																									.visit( defaultNaftahParserVisitor,
																											possibleSpecifiedTypes
																													.get(
																															possibleSpecifiedTypes
																																	.size() - 1))) :
																			JavaType.ofObject()
											));
								}
								return NTuple.of(declarations);
							}
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitChannelDeclaration(org.daiitech.naftah.parser.NaftahParser.ChannelDeclarationContext ctx) {
		return visitContext(
							this,
							"visitChannelDeclaration",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, channelDeclarationContext) -> {
								String name = channelDeclarationContext.ID().getText();
								Channel<Object> channel = Channel.of(name);

								boolean hasType = hasChild(channelDeclarationContext.type());

								var declaredVariable = DeclaredVariable
										.of(currentContext.depth,
											channelDeclarationContext,
											name,
											true,
											JavaType
													.of(TypeReference
															.dynamicParameterizedType(  Channel.class,
																						hasType ?
																								(JavaType) NaftahParserHelper
																										.visit( defaultNaftahParserVisitor,
																												channelDeclarationContext
																														.type()) :
																								JavaType.ofObject())),
											channel);

								currentContext.defineVariable(name, declaredVariable);

								return declaredVariable;
							}
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitActorDeclaration(org.daiitech.naftah.parser.NaftahParser.ActorDeclarationContext ctx) {
		return visitContext(
							this,
							"visitActorDeclaration",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, actorDeclarationContext) -> {
								String name = actorDeclarationContext.ID(0).getText();

								String msgVariableName = Optional
										.ofNullable(actorDeclarationContext.ID(1))
										.map(ParseTree::getText)
										.orElse(ACTOR_MESSAGE);

								boolean hasType = hasChild(actorDeclarationContext.type());

								JavaType messageType = hasType ?
										(JavaType) NaftahParserHelper
												.visit( defaultNaftahParserVisitor,
														actorDeclarationContext.type()) :
										JavaType.ofObject();
								JavaType actorType = JavaType
										.of(TypeReference
												.dynamicParameterizedType(Actor.class, messageType));

								Actor<Object> actor = Actor.of(name, currentContext, () -> {
									var cctx = DefaultContext.getCurrentContext();

									var declaredVariable = DeclaredVariable
											.of(cctx.depth,
												actorDeclarationContext,
												msgVariableName,
												false,
												messageType,
												null);

									cctx.defineVariable(msgVariableName, declaredVariable);

									if (Objects.nonNull(actorDeclarationContext.objectFields())) {
										defaultNaftahParserVisitor.visit(actorDeclarationContext.objectFields());
									}

								}, (message) -> {
									var cctx = DefaultContext.getCurrentContext();

									var declaredVariable = DeclaredVariable
											.of(cctx.depth,
												actorDeclarationContext,
												msgVariableName,
												false,
												messageType,
												message);

									cctx.setVariable(msgVariableName, declaredVariable);

									defaultNaftahParserVisitor.visit(actorDeclarationContext.block());
								}, currentContext::cleanThreadLocals);

								var declaredVariable = DeclaredVariable
										.of(currentContext.depth,
											actorDeclarationContext,
											name,
											true,
											actorType,
											actor);

								currentContext.defineVariable(name, declaredVariable);

								return declaredVariable;
							}
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitAssignment(org.daiitech.naftah.parser.NaftahParser.AssignmentContext ctx) {
		return visitContext(
							this,
							"visitAssignment",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, assignmentContext) -> {
								Object result;
								if (Objects.nonNull(assignmentContext.singleAssignmentExpression())) {
									result = defaultNaftahParserVisitor
											.visit(assignmentContext.singleAssignmentExpression());
								}
								else {
									result = defaultNaftahParserVisitor
											.visit(assignmentContext.multipleAssignmentsExpression());
								}
								return result;
							});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitSingleAssignmentExpression(org.daiitech.naftah.parser.NaftahParser.SingleAssignmentExpressionContext ctx) {
		return visitContext(
							this,
							"visitSingleAssignmentExpression",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, singleAssignmentExpressionContext) -> {
								Object result;
								if (Objects.nonNull(singleAssignmentExpressionContext.singleAssignment())) {
									var singleAssignment = singleAssignmentExpressionContext.singleAssignment();

									var newValue = defaultNaftahParserVisitor
											.visit(singleAssignmentExpressionContext.expression());

									if (Objects.nonNull(singleAssignment.ID())) {
										DeclaredVariable variable = currentContext
												.getVariable(singleAssignment.ID().getText(), false)
												.getRight();
										variable.setValue(newValue);
										result = variable;
									}
									else if (Objects.nonNull(singleAssignment.qualifiedName())) {
										var qualifiedName = getQualifiedName(singleAssignment.qualifiedName());
										result = setObjectUsingQualifiedName(   qualifiedName,
																				currentContext,
																				newValue,
																				singleAssignmentExpressionContext
																						.getStart()
																						.getLine(),
																				singleAssignmentExpressionContext
																						.getStart()
																						.getCharPositionInLine());
									}
									else if (Objects.nonNull(singleAssignment.qualifiedObjectAccess())) {
										var qualifiedName = getQualifiedName(singleAssignment.qualifiedObjectAccess());
										result = setObjectUsingQualifiedName(   qualifiedName,
																				currentContext,
																				newValue,
																				singleAssignmentExpressionContext
																						.getStart()
																						.getLine(),
																				singleAssignmentExpressionContext
																						.getStart()
																						.getCharPositionInLine());
									}
									else {
										org.daiitech.naftah.parser.NaftahParser.CollectionAccessContext collectionAccessContext = singleAssignment
												.collectionAccess();
										Object variable = result = getVariable( matchImplementationName(collectionAccessContext
																						.selfOrId(),
																										currentContext),
																				currentContext).get();
										Number number = -1;
										int size = collectionAccessContext.collectionAccessIndex().size();
										try {
											for (int i = 0; i < size; i++) {
												number = (Number) defaultNaftahParserVisitor
														.visit(collectionAccessContext
																.collectionAccessIndex(
																						i));
												if (variable instanceof List && !(variable instanceof Tuple)) {
													// noinspection unchecked
													List<Object> list = (List<Object>) variable;
													if (i < size - 1) {
														variable = list.get(number.intValue());
													}
													else {
														list.set(number.intValue(), newValue);
													}
												}
												else if (variable instanceof Set) {
													// noinspection unchecked
													Set<Object> set = (Set<Object>) variable;
													if (i < size - 1) {
														variable = getElementAt(set, number.intValue());
													}
													else {
														setElementAt(set, number.intValue(), newValue);
													}
												}
												else {
													throw new NaftahBugError(
																				"""
																				لا يمكن استخدام الفهرسة إلا مع الأنواع التالية: قائمة أو مجموعة.
																				""",
																				collectionAccessContext
																						.getStart()
																						.getLine(),
																				collectionAccessContext
																						.getStart()
																						.getCharPositionInLine());
												}
											}
										}
										catch (IndexOutOfBoundsException indexOutOfBoundsException) {
											throw newNaftahIndexOutOfBoundsBugError(number.intValue(),
																					((Collection<?>) variable).size(),
																					indexOutOfBoundsException,
																					collectionAccessContext
																							.getStart()
																							.getLine(),
																					collectionAccessContext
																							.getStart()
																							.getCharPositionInLine());
										}
									}
								}
								else {
									var singleDeclaration = singleAssignmentExpressionContext.singleDeclaration();
									currentContext.setParsingAssignment(true);
									boolean creatingObjectField = hasAnyParentOfType(   singleDeclaration,
																						org.daiitech.naftah.parser.NaftahParser.ObjectContext.class);
									//noinspection unchecked
									MutablePair<DeclaredVariable, Boolean> declaredVariable = (MutablePair<DeclaredVariable, Boolean>) defaultNaftahParserVisitor
											.visit(singleDeclaration);
									currentContext.setDeclarationOfAssignment(declaredVariable);
									var newValue = defaultNaftahParserVisitor
											.visit(singleAssignmentExpressionContext.expression());

									if (declaredVariable.getRight()) {
										declaredVariable
												.setLeft(DeclaredVariable
														.of(currentContext.depth,
															singleAssignmentExpressionContext,
															declaredVariable.getLeft().getName(),
															declaredVariable.getLeft().isConstant(),
															declaredVariable.getLeft().getType(),
															newValue));
									}
									else {
										declaredVariable
												.getLeft()
												.setOriginalContext(singleAssignmentExpressionContext);
										declaredVariable.getLeft().setValue(newValue);
									}
									// declared and update if possible
									if (!creatingObjectField) {
										currentContext
												.setVariable(   declaredVariable.getLeft().getName(),
																declaredVariable.getLeft());
									}
									currentContext.setParsingAssignment(false);
									result = declaredVariable;
								}

								return result;
							}
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitMultipleAssignmentsExpression(org.daiitech.naftah.parser.NaftahParser.MultipleAssignmentsExpressionContext ctx) {
		return visitContext(
							this,
							"visitMultipleAssignmentsExpression",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, multipleAssignmentsExpressionContext) -> {
								if (Objects.nonNull(multipleAssignmentsExpressionContext.multipleAssignments())) {
									var multipleAssignments = multipleAssignmentsExpressionContext
											.multipleAssignments();
									boolean singleTupleValue = false;
									NTuple tupleValue = null;
									if (multipleAssignmentsExpressionContext
											.expression()
											.size() == 1) {
										var newValue = defaultNaftahParserVisitor
												.visit(multipleAssignmentsExpressionContext.expression(0));
										if (newValue instanceof NTuple objects) {
											tupleValue = objects;
											singleTupleValue = true;
										}
										else {
											throw newNaftahSingleExpressionAssignmentError( multipleAssignmentsExpressionContext
																									.getStart()
																									.getLine(),
																							multipleAssignmentsExpressionContext
																									.getStart()
																									.getCharPositionInLine());
										}
									}
									else if (multipleAssignments
											.singleAssignment()
											.size() != multipleAssignmentsExpressionContext
													.expression()
													.size()) {
														throw newNaftahExpressionsDeclarationsSizeMismatchErrorError(
																														multipleAssignmentsExpressionContext
																																.getStart()
																																.getLine(),
																														multipleAssignmentsExpressionContext
																																.getStart()
																																.getCharPositionInLine());
													}
									List<Object> assignments = new ArrayList<>();
									for (int i = 0; i < multipleAssignments.singleAssignment().size(); i++) {
										var singleAssignment = multipleAssignments.singleAssignment(i);

										var newValue = singleTupleValue ?
												tupleValue.get(i) :
												defaultNaftahParserVisitor
														.visit(multipleAssignmentsExpressionContext.expression(i));

										if (Objects.nonNull(singleAssignment.ID())) {
											DeclaredVariable variable = currentContext
													.getVariable(singleAssignment.ID().getText(), false)
													.getRight();
											variable.setValue(newValue);
											assignments.add(variable);
										}
										else if (Objects.nonNull(singleAssignment.qualifiedName())) {
											var qualifiedName = getQualifiedName(singleAssignment.qualifiedName());
											assignments
													.add(setObjectUsingQualifiedName(   qualifiedName,
																						currentContext,
																						newValue,
																						singleAssignment
																								.getStart()
																								.getLine(),
																						singleAssignment
																								.getStart()
																								.getCharPositionInLine()));
										}
										else if (Objects.nonNull(singleAssignment.qualifiedObjectAccess())) {
											var qualifiedName = getQualifiedName(singleAssignment
													.qualifiedObjectAccess());
											assignments
													.add(setObjectUsingQualifiedName(   qualifiedName,
																						currentContext,
																						newValue,
																						singleAssignment
																								.getStart()
																								.getLine(),
																						singleAssignment
																								.getStart()
																								.getCharPositionInLine()));
										}
										else {
											org.daiitech.naftah.parser.NaftahParser.CollectionAccessContext collectionAccessContext = singleAssignment
													.collectionAccess();
											Object variable = getVariable(  matchImplementationName(collectionAccessContext
																					.selfOrId(),
																									currentContext),
																			currentContext).get();
											Number number = -1;
											int size = collectionAccessContext.collectionAccessIndex().size();
											try {
												for (int j = 0; j < size; j++) {
													number = (Number) defaultNaftahParserVisitor
															.visit(collectionAccessContext
																	.collectionAccessIndex(
																							j));
													if (variable instanceof List && !(variable instanceof Tuple)) {
														// noinspection unchecked
														List<Object> list = (List<Object>) variable;
														if (j < size - 1) {
															variable = list.get(number.intValue());
														}
														else {
															list.set(number.intValue(), newValue);
														}
													}
													else if (variable instanceof Set) {
														// noinspection unchecked
														Set<Object> set = (Set<Object>) variable;
														if (j < size - 1) {
															variable = getElementAt(set, number.intValue());
														}
														else {
															setElementAt(set, number.intValue(), newValue);
														}
													}
													else {
														throw new NaftahBugError(
																					"""
																					لا يمكن استخدام الفهرسة إلا مع الأنواع التالية: قائمة أو مجموعة.
																					""",
																					collectionAccessContext
																							.getStart()
																							.getLine(),
																					collectionAccessContext
																							.getStart()
																							.getCharPositionInLine());
													}
												}
											}
											catch (IndexOutOfBoundsException indexOutOfBoundsException) {
												throw newNaftahIndexOutOfBoundsBugError(number.intValue(),
																						((Collection<?>) variable)
																								.size(),
																						indexOutOfBoundsException,
																						collectionAccessContext
																								.getStart()
																								.getLine(),
																						collectionAccessContext
																								.getStart()
																								.getCharPositionInLine());
											}
											assignments.add(variable);
										}
									}

									return NTuple.of(assignments);
								}
								else {
									var multipleDeclarations = multipleAssignmentsExpressionContext
											.multipleDeclarations();
									boolean singleTupleValue = false;
									NTuple tupleValue = null;
									if (multipleAssignmentsExpressionContext
											.expression()
											.size() == 1) {
										var newValue = defaultNaftahParserVisitor
												.visit(multipleAssignmentsExpressionContext.expression(0));
										if (newValue instanceof NTuple objects) {
											tupleValue = objects;
											singleTupleValue = true;
										}
										else {
											throw newNaftahSingleExpressionAssignmentError( multipleAssignmentsExpressionContext
																									.getStart()
																									.getLine(),
																							multipleAssignmentsExpressionContext
																									.getStart()
																									.getCharPositionInLine());
										}
									}
									else if (multipleDeclarations.ID().size() != multipleAssignmentsExpressionContext
											.expression()
											.size()) {
												throw newNaftahExpressionsDeclarationsSizeMismatchErrorError(
																												multipleAssignmentsExpressionContext
																														.getStart()
																														.getLine(),
																												multipleAssignmentsExpressionContext
																														.getStart()
																														.getCharPositionInLine());
											}

									currentContext.setParsingAssignment(true);
									boolean creatingObjectField = hasAnyParentOfType(   multipleDeclarations,
																						org.daiitech.naftah.parser.NaftahParser.ObjectContext.class);

									NTuple declaredVariables = (NTuple) defaultNaftahParserVisitor
											.visit(multipleDeclarations);

									for (int i = 0; i < declaredVariables.arity(); i++) {
										//noinspection unchecked
										MutablePair<DeclaredVariable, Boolean> declaredVariable = (MutablePair<DeclaredVariable, Boolean>) declaredVariables
												.get(i);
										currentContext.setDeclarationOfAssignment(declaredVariable);

										var newValue = singleTupleValue ?
												tupleValue.get(i) :
												defaultNaftahParserVisitor
														.visit(multipleAssignmentsExpressionContext.expression(i));

										if (declaredVariable.getRight()) {
											declaredVariable
													.setLeft(DeclaredVariable
															.of(currentContext.depth,
																multipleAssignmentsExpressionContext,
																declaredVariable.getLeft().getName(),
																declaredVariable.getLeft().isConstant(),
																declaredVariable.getLeft().getType(),
																newValue));
										}
										else {
											declaredVariable
													.getLeft()
													.setOriginalContext(multipleAssignmentsExpressionContext);
											declaredVariable.getLeft().setValue(newValue);
										}
										// declared and update if possible
										if (!creatingObjectField) {
											currentContext
													.setVariable(   declaredVariable.getLeft().getName(),
																	declaredVariable.getLeft());
										}
									}

									currentContext.setParsingAssignment(false);

									return declaredVariables;
								}

							}
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitFunctionDeclaration(org.daiitech.naftah.parser.NaftahParser.FunctionDeclarationContext ctx) {
		return visitContext(
							this,
							"visitFunctionDeclaration",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, functionDeclarationContext) -> {
								boolean creatingImplementationFunction = hasAnyParentOfType(functionDeclarationContext,
																							org.daiitech.naftah.parser.NaftahParser.ImplementationDeclarationContext.class);

								String implementationName = creatingImplementationFunction ?
										((org.daiitech.naftah.parser.NaftahParser.ImplementationDeclarationContext) functionDeclarationContext
												.getParent()
												.getParent())
												.ID()
												.getText() :
										null;
								String functionName = functionDeclarationContext.ID().getText();
								if (!creatingImplementationFunction && currentContext
										.containsFunction(  functionName,
															currentContext.depth)) {
									throw newNaftahBugExistentFunctionError(functionName);
								}
								DeclaredFunction<?> declaredFunction = DeclaredFunction
										.of(currentContext.depth,
											functionDeclarationContext,
											implementationName);

								prepareDeclaredFunction(defaultNaftahParserVisitor, declaredFunction);

								if (!creatingImplementationFunction) {
									currentContext.defineFunction(functionName, declaredFunction);
								}
								return declaredFunction;
							}
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitParameterDeclarationList(
												org.daiitech.naftah.parser.NaftahParser.ParameterDeclarationListContext ctx) {
		return visitContext(
							this,
							"visitParameterDeclarationList",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, parameterDeclarationListContext) -> {
								var parameterDeclarations = parameterDeclarationListContext.parameterDeclaration();

								Set<String> uniqueParameterNames = new HashSet<>();
								Optional<String> firstDuplicateParameterName = parameterDeclarations
										.stream()
										.map(parameterDeclaration -> parameterDeclaration
												.ID()
												.getText())
										.filter(e -> !uniqueParameterNames.add(e))
										.findFirst();
								if (firstDuplicateParameterName.isPresent()) {
									throw newNaftahBugExistentFunctionParameterError(firstDuplicateParameterName.get());
								}

								List<DeclaredParameter> params = new ArrayList<>();
								for (org.daiitech.naftah.parser.NaftahParser.ParameterDeclarationContext parameterDeclaration : parameterDeclarations) {
									params
											.add((DeclaredParameter) defaultNaftahParserVisitor
													.visit(parameterDeclaration));
								}
								return params;
							}
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitParameterDeclaration(org.daiitech.naftah.parser.NaftahParser.ParameterDeclarationContext ctx) {
		return visitContext(
							this,
							"visitParameterDeclaration",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, parameterDeclarationContext) -> {
								String parameterName = parameterDeclarationContext.ID().getText();
								return DeclaredParameter
										.of(currentContext.depth,
											parameterDeclarationContext,
											parameterName,
											hasChild(parameterDeclarationContext.CONSTANT()),
											hasChild(parameterDeclarationContext.type()) ?
													(JavaType) defaultNaftahParserVisitor
															.visit(parameterDeclarationContext.type()) :
													JavaType.ofObject(),
											hasChild(parameterDeclarationContext.value()) ?
													defaultNaftahParserVisitor
															.visit(parameterDeclarationContext.value()) :
													null);
							});
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitInitCall(org.daiitech.naftah.parser.NaftahParser.InitCallContext ctx) {
		return visitContext(
							this,
							"visitInitCall",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, initCallContext) -> {
								boolean hasQualifiedName = hasChild(initCallContext
										.qualifiedName());

								currentContext.setParsingFunctionCallId(true);

								String functionName = hasQualifiedName ?
										(String) defaultNaftahParserVisitor
												.visit(initCallContext
														.qualifiedName()) :
										initCallContext.ID().getText();

								var matchedImport = currentContext.matchImport(functionName);
								if (Objects.nonNull(matchedImport)) {
									functionName = matchedImport;
								}

								List<Pair<String, Object>> args = new ArrayList<>();

								if (hasChild(initCallContext.argumentList())) {
									//noinspection unchecked
									args = (List<Pair<String, Object>>) defaultNaftahParserVisitor
											.visit(initCallContext.argumentList());
								}

								Object result;

								String functionCallId = generateCallId(depth, functionName);
								currentContext.setFunctionCallId(functionCallId);


								if (currentContext.containsJvmClassInitializer(functionName)) {
									Object jvmClassInitializerOrList = currentContext
											.getJvmClassInitializer(functionName, false)
											.getRight();
									if (jvmClassInitializerOrList instanceof JvmClassInitializer jvmClassInitializer) {
										result = invokeJvmClassInitializer( functionName,
																			jvmClassInitializer,
																			args,
																			initCallContext.getStart().getLine(),
																			initCallContext
																					.getStart()
																					.getCharPositionInLine());
									}
									else {
										//noinspection unchecked
										List<JvmClassInitializer> jvmClassInitializersList = (List<JvmClassInitializer>) jvmClassInitializerOrList;
										try {
											if (Objects.nonNull(initCallContext.targetExecutableIndex())) {
												Number jvmClassInitializerIndex = NumberUtils
														.parseDynamicNumber(initCallContext
																.targetExecutableIndex()
																.NUMBER()
																.getText());

												JvmClassInitializer jvmClassInitializer = jvmClassInitializersList
														.get(jvmClassInitializerIndex.intValue());
												result = invokeJvmClassInitializer( functionName,
																					jvmClassInitializer,
																					args,
																					initCallContext
																							.getStart()
																							.getLine(),
																					initCallContext
																							.getStart()
																							.getCharPositionInLine());
											}
											else {
												result = invokeJvmClassInitializer( functionName,
																					jvmClassInitializersList,
																					args,
																					initCallContext
																							.getStart()
																							.getLine(),
																					initCallContext
																							.getStart()
																							.getCharPositionInLine());
											}
										}
										catch (Throwable th) {
											throw newNaftahInvocableListFoundError( functionName,
																					jvmClassInitializersList,
																					th,
																					initCallContext
																							.getStart()
																							.getLine(),
																					initCallContext
																							.getStart()
																							.getCharPositionInLine());
										}
									}
								}
								else {
									throw newNaftahInvocableNotFoundError(  functionName,
																			initCallContext.getStart().getLine(),
																			initCallContext
																					.getStart()
																					.getCharPositionInLine());
								}

								currentContext.setFunctionCallId(null);

								if (Objects.nonNull(initCallContext.callSegment()) && !initCallContext
										.callSegment()
										.isEmpty()) {
									boolean firstSegment = true;
									String[] parts;
									String qualifiedName;
									boolean hasQualifiedCall;

									for (org.daiitech.naftah.parser.NaftahParser.CallSegmentContext callSegmentContext : initCallContext
											.callSegment()) {
										if (firstSegment) {
											qualifiedName = callSegmentContext.COLON().size() == 3 ?
													functionName :
													null;
											firstSegment = false;
										}
										else {
											parts = functionName.split(QUALIFIED_CALL_SEPARATOR);
											qualifiedName = parts.length == 2 && callSegmentContext
													.COLON()
													.size() == 3 ?
															parts[0] :
															null;
										}

										hasQualifiedCall = hasChild(callSegmentContext.primaryCall().qualifiedCall());

										currentContext.setParsingFunctionCallId(hasQualifiedCall);

										functionName = hasQualifiedCall ?
												(String) defaultNaftahParserVisitor
														.visit(callSegmentContext
																.primaryCall()
																.qualifiedCall()) :
												matchImplementationName(callSegmentContext.primaryCall().selfOrId(),
																		currentContext);

										matchedImport = currentContext.matchImport(functionName);
										if (Objects.nonNull(matchedImport)) {
											functionName = matchedImport;
										}
										else if (Objects.nonNull(qualifiedName) && !qualifiedName.isBlank()) {
											functionName = qualifiedName + QUALIFIED_CALL_SEPARATOR + functionName;
										}

										if (hasChild(callSegmentContext.primaryCall().argumentList())) {
											//noinspection unchecked
											args = (List<Pair<String, Object>>) defaultNaftahParserVisitor
													.visit(callSegmentContext.primaryCall().argumentList());
										}
										else {
											args.clear();
										}

										// result of previous call in chain to perform the current function on it
										// (it behaves like a pipe for builtin, declared and static java methods (first argument))
										// and as the instance (this) for java instance methods
										args.add(0, ImmutablePair.of(null, result));

										Number jvmFunctionIndex = Objects
												.nonNull(callSegmentContext
														.primaryCall()
														.targetExecutableIndex()) ?
																NumberUtils
																		.parseDynamicNumber(callSegmentContext
																				.primaryCall()
																				.targetExecutableIndex()
																				.NUMBER()
																				.getText()) :
																null;

										result = visitFunctionCallInChain(  depth,
																			defaultNaftahParserVisitor,
																			currentContext,
																			functionName,
																			false,
																			args,
																			jvmFunctionIndex,
																			callSegmentContext.getStart().getLine(),
																			callSegmentContext
																					.getStart()
																					.getCharPositionInLine());
									}
								}

								return result;
							});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitFunctionCall(org.daiitech.naftah.parser.NaftahParser.FunctionCallContext ctx) {
		return visitContext(
							this,
							"visitFunctionCall",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, functionCallContext) -> {
								boolean hasQualifiedCall = hasChild(functionCallContext.primaryCall().qualifiedCall());

								currentContext.setParsingFunctionCallId(hasQualifiedCall);

								String functionName = hasQualifiedCall ?
										(String) defaultNaftahParserVisitor
												.visit(functionCallContext
														.primaryCall()
														.qualifiedCall()) :
										matchImplementationName(functionCallContext.primaryCall().selfOrId(),
																currentContext);

								Triple<String, Boolean, Object> matchedVariableQualifiedCallAndForceInvocationWithValue = null;
								if (hasQualifiedCall) {
									matchedVariableQualifiedCallAndForceInvocationWithValue = currentContext
											.matchVariable(
															functionName);
									if (Objects.nonNull(matchedVariableQualifiedCallAndForceInvocationWithValue)) {
										functionName = matchedVariableQualifiedCallAndForceInvocationWithValue
												.getLeft();
									}
								}

								String matchedImport;
								List<Pair<String, Object>> args = new ArrayList<>();

								if (Objects.isNull(matchedVariableQualifiedCallAndForceInvocationWithValue)) {
									matchedImport = currentContext.matchImport(functionName);
									if (Objects.nonNull(matchedImport)) {
										functionName = matchedImport;
									}
								}
								else {
									// the variable value to perform the current function on it
									args
											.add(ImmutablePair
													.of(null,
														matchedVariableQualifiedCallAndForceInvocationWithValue
																.getRight()));
								}

								if (hasChild(functionCallContext.primaryCall().argumentList())) {
									//noinspection unchecked
									args
											.addAll((List<Pair<String, Object>>) defaultNaftahParserVisitor
													.visit(functionCallContext.primaryCall().argumentList()));
								}

								Number jvmFunctionIndex = Objects
										.nonNull(functionCallContext
												.primaryCall()
												.targetExecutableIndex()) ?
														NumberUtils
																.parseDynamicNumber(functionCallContext
																		.primaryCall()
																		.targetExecutableIndex()
																		.NUMBER()
																		.getText()) :
														null;

								Object result = visitFunctionCallInChain(   depth,
																			defaultNaftahParserVisitor,
																			currentContext,
																			functionName,
																			Optional
																					.ofNullable(
																								matchedVariableQualifiedCallAndForceInvocationWithValue)
																					.map(Triple::getMiddle)
																					.orElse(false),
																			args,
																			jvmFunctionIndex,
																			functionCallContext.getStart().getLine(),
																			functionCallContext
																					.getStart()
																					.getCharPositionInLine());

								if (Objects.nonNull(functionCallContext.callSegment()) && !functionCallContext
										.callSegment()
										.isEmpty()) {
									String[] parts;
									String qualifiedName;

									for (org.daiitech.naftah.parser.NaftahParser.CallSegmentContext callSegmentContext : functionCallContext
											.callSegment()) {
										parts = functionName.split(QUALIFIED_CALL_SEPARATOR);
										qualifiedName = parts.length == 2 && callSegmentContext.COLON().size() == 3 ?
												parts[0] :
												null;

										hasQualifiedCall = hasChild(callSegmentContext.primaryCall().qualifiedCall());

										currentContext.setParsingFunctionCallId(hasQualifiedCall);

										functionName = hasQualifiedCall ?
												(String) defaultNaftahParserVisitor
														.visit(callSegmentContext
																.primaryCall()
																.qualifiedCall()) :
												matchImplementationName(callSegmentContext.primaryCall().selfOrId(),
																		currentContext);

										matchedImport = currentContext.matchImport(functionName);
										if (Objects.nonNull(matchedImport)) {
											functionName = matchedImport;
										}
										else if (Objects.nonNull(qualifiedName) && !qualifiedName.isBlank()) {
											functionName = qualifiedName + QUALIFIED_CALL_SEPARATOR + functionName;
										}

										if (hasChild(callSegmentContext.primaryCall().argumentList())) {
											//noinspection unchecked
											args = (List<Pair<String, Object>>) defaultNaftahParserVisitor
													.visit(callSegmentContext.primaryCall().argumentList());
										}
										else {
											args.clear();
										}

										// result of previous call in chain to perform the current function on it
										// (it behaves like a pipe for builtin, declared and static java methods (first argument))
										// and as the instance (this) for java instance methods
										args.add(0, ImmutablePair.of(null, result));

										jvmFunctionIndex = Objects
												.nonNull(callSegmentContext
														.primaryCall()
														.targetExecutableIndex()) ?
																NumberUtils
																		.parseDynamicNumber(callSegmentContext
																				.primaryCall()
																				.targetExecutableIndex()
																				.NUMBER()
																				.getText()) :
																null;

										result = visitFunctionCallInChain(  depth,
																			defaultNaftahParserVisitor,
																			currentContext,
																			functionName,
																			Optional
																					.ofNullable(
																								matchedVariableQualifiedCallAndForceInvocationWithValue)
																					.map(Triple::getMiddle)
																					.orElse(false),
																			args,
																			jvmFunctionIndex,
																			callSegmentContext.getStart().getLine(),
																			callSegmentContext
																					.getStart()
																					.getCharPositionInLine());
									}
								}

								return result;
							});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitSimpleCall(org.daiitech.naftah.parser.NaftahParser.SimpleCallContext ctx) {
		return visitContext(
							this,
							"visitSimpleCall",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								simpleCallContext) -> matchImplementationName(  simpleCallContext.selfOrId(),
																				currentContext) + simpleCallContext
																						.COLON(0)
																						.getText() + simpleCallContext
																								.COLON(1)
																								.getText() + simpleCallContext
																										.ID()
																										.getText()
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitQualifiedNameCall(org.daiitech.naftah.parser.NaftahParser.QualifiedNameCallContext ctx) {
		return visitContext(
							this,
							"visitQualifiedNameCall",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								qualifiedNameCallContext) -> defaultNaftahParserVisitor
										.visit(qualifiedNameCallContext.qualifiedName()) + qualifiedNameCallContext
												.COLON(0)
												.getText() + qualifiedNameCallContext
														.COLON(1)
														.getText() + qualifiedNameCallContext.ID().getText()
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitArgumentList(org.daiitech.naftah.parser.NaftahParser.ArgumentListContext ctx) {
		return visitContext(
							this,
							"visitArgumentList",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, argumentListContext) -> {
								List<Pair<String, Object>> args = new ArrayList<>();
								Set<String> argumentNames = new HashSet<>();
								for (int i = 0; i < argumentListContext.expression().size(); i++) {
									String name = null;
									if (hasChild(argumentListContext.ID(i))) {
										name = argumentListContext.ID(i).getText();
										if (!argumentNames.add(name)) {
											throw newNaftahBugExistentFunctionArgumentError(name);
										}
									}
									Object value = defaultNaftahParserVisitor.visit(argumentListContext.expression(i));
									args.add(ImmutablePair.of(name, value)); // Evaluate each expression in the argument list
								}
								return args;
							}
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitIfStatement(org.daiitech.naftah.parser.NaftahParser.IfStatementContext ctx) {
		return visitContext(
							this,
							"visitIfStatement",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, ifStatementContext) -> {
								Object result = None.get();
								// Evaluate the condition expression
								Object condition = defaultNaftahParserVisitor.visit(ifStatementContext.expression(0));
								if (isTruthy(condition)) {
									result = defaultNaftahParserVisitor.visit(ifStatementContext.block(0)); // If the condition is
									// true, execute the 'then' block
								}
								else {
									boolean elseifConditionMatched = false;
									// Iterate through elseif blocks
									for (int i = 0; i < ifStatementContext.ELSEIF().size(); i++) {
										Object elseifCondition = defaultNaftahParserVisitor
												.visit(ifStatementContext.expression(i + 1)); //
										// Evaluate elseif condition
										if (isTruthy(elseifCondition)) {
											result = defaultNaftahParserVisitor.visit(ifStatementContext.block(i + 1)); // Execute
											// the corresponding elseif block if
											// condition is true
											elseifConditionMatched = true;
											break;
										}
									}

									// If no elseif was true, execute the else block (if it exists)
									if (!elseifConditionMatched && hasChild(ifStatementContext.ELSE())) {
										result = defaultNaftahParserVisitor
												.visit(ifStatementContext
														.block(ifStatementContext
																.ELSEIF()
																.size() + 1));
										// Execute the 'else' block if present
									}
								}
								return result;
							}
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitIndexBasedForLoopStatement(org.daiitech.naftah.parser.NaftahParser.IndexBasedForLoopStatementContext ctx) {
		return visitContext(
							this,
							"visitIndexBasedForLoopStatement",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, indexBasedForLoopStatementContext) -> {
								Object result = None.get();
								boolean loopInStack = false;
								String label = currentLoopLabel((String) (Objects
										.isNull(indexBasedForLoopStatementContext.label()) ?
												null :
												defaultNaftahParserVisitor
														.visit(
																indexBasedForLoopStatementContext.label())),
																depth);
								currentContext.setLoopLabel(label);
								// Initialization: ID := expression
								String loopVar = indexBasedForLoopStatementContext.ID().getText();
								Object initValue = defaultNaftahParserVisitor
										.visit(indexBasedForLoopStatementContext.expression(0));
								if (Objects.isNull(initValue)) {
									throw new NaftahBugError(   String
																		.format("""
																				القيمة الابتدائية للمتغير '%s' لا يمكن أن تكون فارغة.""",
																				loopVar),
																indexBasedForLoopStatementContext.getStart().getLine(),
																indexBasedForLoopStatementContext
																		.getStart()
																		.getCharPositionInLine());
								}
								// End value
								Object endValue = defaultNaftahParserVisitor
										.visit(indexBasedForLoopStatementContext.expression(1));
								if (Objects.isNull(endValue)) {
									throw new NaftahBugError(   String
																		.format("القيمة النهائية للمتغير '%s' لا يمكن أن تكون فارغة.",
																				loopVar),
																indexBasedForLoopStatementContext.getStart().getLine(),
																indexBasedForLoopStatementContext
																		.getStart()
																		.getCharPositionInLine());
								}
								// step value
								Object stepValue = indexBasedForLoopStatementContext.STEP() != null ?
										defaultNaftahParserVisitor
												.visit(indexBasedForLoopStatementContext.expression(2)) :
										DynamicNumber.of(1);
								if (Boolean.TRUE
										.equals(applyOperation(stepValue, 0, LESS_THAN_EQUALS))) {
									throw new NaftahBugError(   String
																		.format("قيمة الخطوة للمتغير '%s' لا يمكن أن تكون أقل من أو " + "يساوي 0.",
																				loopVar),
																indexBasedForLoopStatementContext.getStart().getLine(),
																indexBasedForLoopStatementContext
																		.getStart()
																		.getCharPositionInLine());
								}

								if (!Number.class.isAssignableFrom(initValue.getClass()) || !Number.class
										.isAssignableFrom(endValue.getClass()) || !Number.class
												.isAssignableFrom(stepValue.getClass())) {
									throw new NaftahBugError(
																String
																		.format("""
																				يجب أن تكون القيمتين الابتدائية والنهائية و الخطوة للمتغير '%s' من النوع الرقمي.""",
																				loopVar),
																indexBasedForLoopStatementContext.getStart().getLine(),
																indexBasedForLoopStatementContext
																		.getStart()
																		.getCharPositionInLine());
								}

								// Direction (TO or DOWNTO)
								boolean isAscending = indexBasedForLoopStatementContext.TO() != null;
								// Loop block
								org.daiitech.naftah.parser.NaftahParser.BlockContext loopBlock = indexBasedForLoopStatementContext
										.block(0);
								// Optional ELSE block
								org.daiitech.naftah.parser.NaftahParser.BlockContext elseBlock = null;
								if (indexBasedForLoopStatementContext.block().size() > 1) {
									elseBlock = indexBasedForLoopStatementContext.block(1);
								}

								boolean brokeEarly = false;
								boolean loopSignal = false;
								boolean propagateLoopSignal = false;

								try {
									pushLoop(label, indexBasedForLoopStatementContext);
									loopInStack = true;
									currentContext.defineLoopVariable(loopVar, initValue, false);
									if (isAscending) {
										if (Boolean.TRUE.equals(applyOperation(endValue, initValue, LESS_THAN))) {
											throw new NaftahBugError(   """
																		القيمة النهائية يجب أن تكون أكبر أو تساوي القيمة الابتدائية في الحلقات التصاعدية.""",
																		indexBasedForLoopStatementContext
																				.getStart()
																				.getLine(),
																		indexBasedForLoopStatementContext
																				.getStart()
																				.getCharPositionInLine());
										}

										for (;  Boolean.TRUE
														.equals(applyOperation(initValue, endValue, LESS_THAN_EQUALS));
												initValue = currentContext
														.setLoopVariable(   loopVar,
																			stepValue.equals(1) ?
																					applyOperation( initValue,
																									PRE_INCREMENT) :
																					applyOperation( initValue,
																									stepValue,
																									ADD))) {
											result = defaultNaftahParserVisitor.visit(loopBlock);

											if (checkLoopSignal(result).equals(CONTINUE)) {
												loopSignal = true;
												String targetLabel = ((LoopSignal.LoopSignalDetails) result)
														.targetLabel();
												if (Objects.isNull(targetLabel) || targetLabel.equals(label)) {
													continue;
												}
												else {
													propagateLoopSignal = true;
													break;
												}
											}

											if (checkLoopSignal(result).equals(BREAK)) {
												loopSignal = true;
												String targetLabel = ((LoopSignal.LoopSignalDetails) result)
														.targetLabel();
												brokeEarly = true;
												if (Objects.isNull(targetLabel) || targetLabel.equals(label)) {
													break;
												}
												else {
													propagateLoopSignal = true;
													break;
												}
											}

											if (checkLoopSignal(result).equals(RETURN)) {
												loopSignal = true;
												brokeEarly = true;
												break;
											}

											// force current loop label
											currentContext.setLoopLabel(label);
										}
									}
									else {
										if (Boolean.TRUE.equals(applyOperation(initValue, endValue, LESS_THAN))) {
											throw new NaftahBugError(   """
																		القيمة الابتدائية يجب أن تكون أكبر أو تساوي القيمة النهائية في الحلقات التنازلية.""",
																		indexBasedForLoopStatementContext
																				.getStart()
																				.getLine(),
																		indexBasedForLoopStatementContext
																				.getStart()
																				.getCharPositionInLine());
										}

										for (;  Boolean.TRUE
														.equals(applyOperation( initValue,
																				endValue,
																				GREATER_THAN_EQUALS));
												initValue = currentContext
														.setLoopVariable(   loopVar,
																			stepValue.equals(1) ?
																					applyOperation( initValue,
																									PRE_DECREMENT) :
																					applyOperation( initValue,
																									stepValue,
																									SUBTRACT))) {
											result = defaultNaftahParserVisitor.visit(loopBlock);

											if (checkLoopSignal(result).equals(CONTINUE)) {
												loopSignal = true;
												String targetLabel = ((LoopSignal.LoopSignalDetails) result)
														.targetLabel();
												if (Objects.isNull(targetLabel) || targetLabel.equals(label)) {
													continue;
												}
												else {
													propagateLoopSignal = true;
													break;
												}
											}

											if (checkLoopSignal(result).equals(BREAK)) {
												loopSignal = true;
												String targetLabel = ((LoopSignal.LoopSignalDetails) result)
														.targetLabel();
												brokeEarly = true;
												if (Objects.isNull(targetLabel) || targetLabel.equals(label)) {
													break;
												}
												else {
													propagateLoopSignal = true;
													break;
												}
											}

											if (checkLoopSignal(result).equals(RETURN)) {
												loopSignal = true;
												brokeEarly = true;
												break;
											}

											// force current loop label
											currentContext.setLoopLabel(label);
										}
									}

									// Run ELSE block only if loop did not break early
									if (!brokeEarly && elseBlock != null) {
										result = defaultNaftahParserVisitor.visit(elseBlock);
									}
								}
								finally {
									currentContext.removeLoopVariable(loopVar, true);
									currentContext.setLoopLabel(null);
									if (loopInStack) {
										popLoop();
									}
								}

								if (loopSignal) {
									if ((LOOP_STACK.get().isEmpty() || !propagateLoopSignal) && !None.isNone(result)) {
										return Optional
												.ofNullable((LoopSignal.LoopSignalDetails) result)
												.map(LoopSignal.LoopSignalDetails::result)
												.orElse(None.get());
									}
									return result;
								}
								else {
									return None.get();
								}
							}
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitForEachLoopStatement(org.daiitech.naftah.parser.NaftahParser.ForEachLoopStatementContext ctx) {
		return visitContext(
							this,
							"visitForEachLoopStatement",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, forEachLoopStatementContext) -> {
								Object result = None.get();
								boolean loopInStack = false;
								String label = currentLoopLabel((String) (Objects
										.isNull(forEachLoopStatementContext.label()) ?
												null :
												defaultNaftahParserVisitor
														.visit(
																forEachLoopStatementContext.label())),
																depth);
								currentContext.setLoopLabel(label);

								// Loop target
								org.daiitech.naftah.parser.NaftahParser.ForeachTargetContext foreachTarget = forEachLoopStatementContext
										.foreachTarget();
								Class<? extends org.daiitech.naftah.parser.NaftahParser.ForeachTargetContext> foreachTargetClass = foreachTarget
										.getClass();
								NTuple target;
								NTuple targetValues;

								// Loop expression. should be an iterable or Map or Naftah Object
								Object object = defaultNaftahParserVisitor
										.visit(forEachLoopStatementContext.expression());

								if (object instanceof NaftahObject naftahObject) {
									object = naftahObject.get();
								}

								Iterator<?> iterator;
								boolean isMap = false;

								if (object instanceof Iterable<?> iterable) {
									if (foreachTarget instanceof org.daiitech.naftah.parser.NaftahParser.KeyValueForeachTargetContext || foreachTarget instanceof org.daiitech.naftah.parser.NaftahParser.IndexAndKeyValueForeachTargetContext) {
										throw new NaftahBugError(   "زوج المفتاح والقيمة غير مدعوم للمجموعة.",
																	forEachLoopStatementContext.getStart().getLine(),
																	forEachLoopStatementContext
																			.getStart()
																			.getCharPositionInLine());
									}
									target = (NTuple) defaultNaftahParserVisitor.visit(foreachTarget);
									iterator = iterable.iterator();
								}
								else if (object instanceof Map<?, ?> map) {
									isMap = true;
									target = (NTuple) defaultNaftahParserVisitor.visit(foreachTarget);
									iterator = map.entrySet().iterator();
								}
								else {
									throw new NaftahBugError(   "غير قابل للتكرار",
																forEachLoopStatementContext.getStart().getLine(),
																forEachLoopStatementContext
																		.getStart()
																		.getCharPositionInLine());
								}


								// Loop block
								org.daiitech.naftah.parser.NaftahParser.BlockContext loopBlock = forEachLoopStatementContext
										.block(0);
								// Optional ELSE block
								org.daiitech.naftah.parser.NaftahParser.BlockContext elseBlock = null;
								if (forEachLoopStatementContext.block().size() > 1) {
									elseBlock = forEachLoopStatementContext.block(1);
								}

								DynamicNumber index = DynamicNumber.of(0);
								boolean brokeEarly = false;
								boolean loopSignal = false;
								boolean propagateLoopSignal = false;

								try {
									pushLoop(label, forEachLoopStatementContext);
									loopInStack = true;

									while (iterator.hasNext()) {
										if (isMap) {
											Map.Entry<?, ?> entry = (Map.Entry<?, ?>) iterator.next();
											targetValues = NTuple.of(index, entry.getKey(), entry.getValue());
										}
										else {
											Object value = iterator.next();
											targetValues = NTuple.of(index, value);
										}

										setForeachVariables(currentContext, foreachTargetClass, target, targetValues);

										result = defaultNaftahParserVisitor.visit(loopBlock);

										if (checkLoopSignal(result).equals(CONTINUE)) {
											loopSignal = true;
											String targetLabel = ((LoopSignal.LoopSignalDetails) result)
													.targetLabel();
											if (Objects.isNull(targetLabel) || targetLabel.equals(label)) {
												continue;
											}
											else {
												propagateLoopSignal = true;
												break;
											}
										}

										if (checkLoopSignal(result).equals(BREAK)) {
											loopSignal = true;
											String targetLabel = ((LoopSignal.LoopSignalDetails) result)
													.targetLabel();
											if (Objects.isNull(targetLabel) || targetLabel.equals(label)) {
												break;
											}
											else {
												propagateLoopSignal = true;
												break;
											}
										}

										if (checkLoopSignal(result).equals(RETURN)) {
											loopSignal = true;
											break;
										}
										// increment index
										NumberUtils.preIncrement(index);

										// force current loop label
										currentContext.setLoopLabel(label);
									}

									// Run ELSE block only if loop did not break early
									if (!brokeEarly && elseBlock != null) {
										result = defaultNaftahParserVisitor.visit(elseBlock);
									}
								}
								finally {
									currentContext.setLoopLabel(null);
									if (loopInStack) {
										popLoop();
									}
								}

								if (loopSignal) {
									if ((LOOP_STACK.get().isEmpty() || !propagateLoopSignal) && !None.isNone(result)) {
										return Optional
												.ofNullable((LoopSignal.LoopSignalDetails) result)
												.map(LoopSignal.LoopSignalDetails::result)
												.orElse(None.get());
									}
									return result;
								}
								else {
									return None.get();
								}
							}
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NTuple visitValueForeachTarget(org.daiitech.naftah.parser.NaftahParser.ValueForeachTargetContext ctx) {
		return visitContext(
							this,
							"visitValueForeachTarget",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, valueForeachTargetContext) -> NTuple
									.of(valueForeachTargetContext.ID().getText()),
							NTuple.class
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NTuple visitIndexAndValueForeachTarget(org.daiitech.naftah.parser.NaftahParser.IndexAndValueForeachTargetContext ctx) {
		return visitContext(
							this,
							"visitIndexAndValueForeachTarget",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, indexAndValueForeachTargetContext) -> {
								String index = indexAndValueForeachTargetContext.ID(0).getText();
								String value = indexAndValueForeachTargetContext.ID(1).getText();

								if (index.equals(value)) {
									throw newNaftahBugForeachTargetDuplicatesError( index,
																					indexAndValueForeachTargetContext);
								}

								return NTuple.of(index, value);
							},
							NTuple.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NTuple visitKeyValueForeachTarget(org.daiitech.naftah.parser.NaftahParser.KeyValueForeachTargetContext ctx) {
		return visitContext(
							this,
							"visitKeyValueForeachTarget",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, keyValueForeachTargetContext) -> {
								String key = keyValueForeachTargetContext.ID(0).getText();
								String value = keyValueForeachTargetContext.ID(1).getText();

								if (key.equals(value)) {
									throw newNaftahBugForeachTargetDuplicatesError(
																					key,
																					keyValueForeachTargetContext);
								}

								return NTuple.of(key, value);
							},
							NTuple.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NTuple visitIndexAndKeyValueForeachTarget(org.daiitech.naftah.parser.NaftahParser.IndexAndKeyValueForeachTargetContext ctx) {
		return visitContext(
							this,
							"visitIndexAndKeyValueForeachTarget",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, indexAndKeyValueForeachTargetContext) -> {
								Set<String> uniqueLoopVariableNames = new HashSet<>();

								String index = indexAndKeyValueForeachTargetContext.ID(0).getText();
								uniqueLoopVariableNames.add(index);
								String key = indexAndKeyValueForeachTargetContext.ID(1).getText();
								if (!uniqueLoopVariableNames.add(key)) {
									throw newNaftahBugForeachTargetDuplicatesError( key,
																					indexAndKeyValueForeachTargetContext);
								}
								String value = indexAndKeyValueForeachTargetContext.ID(2).getText();
								if (!uniqueLoopVariableNames.add(value)) {
									throw newNaftahBugForeachTargetDuplicatesError( value,
																					indexAndKeyValueForeachTargetContext);
								}

								return NTuple.of(index, key, value);
							},
							NTuple.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitWhileStatement(org.daiitech.naftah.parser.NaftahParser.WhileStatementContext ctx) {
		return visitContext(
							this,
							"visitWhileStatement",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								whileStatementContext) -> {
								Object result = None.get();
								boolean loopInStack = false;
								String label = currentLoopLabel((String) (Objects
										.isNull(whileStatementContext.label()) ?
												null :
												defaultNaftahParserVisitor.visit(whileStatementContext.label())),
																defaultNaftahParserVisitor.depth);
								currentContext.setLoopLabel(label);

								boolean loopSignal = false;
								boolean propagateLoopSignal = false;

								try {
									pushLoop(label, whileStatementContext);
									loopInStack = true;

									while (isTruthy(defaultNaftahParserVisitor
											.visit(whileStatementContext.expression()))) {
										result = defaultNaftahParserVisitor.visit(whileStatementContext.block());

										if (checkLoopSignal(result).equals(CONTINUE)) {
											loopSignal = true;
											String targetLabel = ((LoopSignal.LoopSignalDetails) result)
													.targetLabel();
											if (Objects.isNull(targetLabel) || targetLabel.equals(label)) {
												continue;
											}
											else {
												propagateLoopSignal = true;
												break;
											}
										}

										if (checkLoopSignal(result).equals(BREAK)) {
											loopSignal = true;
											String targetLabel = ((LoopSignal.LoopSignalDetails) result)
													.targetLabel();
											if (Objects.isNull(targetLabel) || targetLabel.equals(label)) {
												break;
											}
											else {
												propagateLoopSignal = true;
												break;
											}
										}

										if (checkLoopSignal(result).equals(RETURN)) {
											loopSignal = true;
											break;
										}

										// force current loop label
										currentContext.setLoopLabel(label);
									}

								}
								finally {
									currentContext.setLoopLabel(null);
									if (loopInStack) {
										popLoop();
									}
								}

								if (loopSignal) {
									if ((LOOP_STACK.get().isEmpty() || !propagateLoopSignal) && !None.isNone(result)) {
										return Optional
												.ofNullable((LoopSignal.LoopSignalDetails) result)
												.map(LoopSignal.LoopSignalDetails::result)
												.orElse(None.get());
									}
									return result;
								}
								else {
									return None.get();
								}
							}
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitRepeatStatement(org.daiitech.naftah.parser.NaftahParser.RepeatStatementContext ctx) {
		return visitContext(
							this,
							"visitRepeatStatement",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								repeatStatementContext) -> {
								Object result = None.get();
								boolean loopInStack = false;
								String label = currentLoopLabel((String) (Objects
										.isNull(repeatStatementContext.label()) ?
												null :
												defaultNaftahParserVisitor.visit(repeatStatementContext.label())),
																defaultNaftahParserVisitor.depth);
								currentContext.setLoopLabel(label);

								boolean loopSignal = false;
								boolean propagateLoopSignal = false;

								try {
									pushLoop(label, repeatStatementContext);
									loopInStack = true;

									do {
										result = defaultNaftahParserVisitor.visit(repeatStatementContext.block());

										if (checkLoopSignal(result).equals(CONTINUE)) {
											loopSignal = true;
											String targetLabel = ((LoopSignal.LoopSignalDetails) result)
													.targetLabel();
											if (Objects.isNull(targetLabel) || targetLabel.equals(label)) {
												continue;
											}
											else {
												propagateLoopSignal = true;
												break;
											}
										}

										if (checkLoopSignal(result).equals(BREAK)) {
											loopSignal = true;
											String targetLabel = ((LoopSignal.LoopSignalDetails) result)
													.targetLabel();
											if (Objects.isNull(targetLabel) || targetLabel.equals(label)) {
												break;
											}
											else {
												propagateLoopSignal = true;
												break;
											}
										}

										if (checkLoopSignal(result).equals(RETURN)) {
											loopSignal = true;
											break;
										}

										// force current loop label
										currentContext.setLoopLabel(label);
									}
									while (!isTruthy(defaultNaftahParserVisitor
											.visit(repeatStatementContext.expression())));

								}
								finally {
									currentContext.setLoopLabel(null);
									if (loopInStack) {
										popLoop();
									}
								}

								if (loopSignal) {
									if ((LOOP_STACK.get().isEmpty() || !propagateLoopSignal) && !None.isNone(result)) {
										return Optional
												.ofNullable((LoopSignal.LoopSignalDetails) result)
												.map(LoopSignal.LoopSignalDetails::result)
												.orElse(None.get());
									}
									return result;
								}
								else {
									return None.get();
								}
							}
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitCaseStatement(org.daiitech.naftah.parser.NaftahParser.CaseStatementContext ctx) {
		return visitContext(
							this,
							"visitCaseStatement",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								caseStatementContext) -> {
								Object controlValue = defaultNaftahParserVisitor
										.visit(caseStatementContext.expression());

								boolean matched = false;
								Object result = null;

								for (int i = 0; i < caseStatementContext.caseLabelList().size(); i++) {
									org.daiitech.naftah.parser.NaftahParser.CaseLabelListContext labels = caseStatementContext
											.caseLabelList(
															i);

									for (org.daiitech.naftah.parser.NaftahParser.ExpressionContext expression : labels
											.expression()) {
										Object labelValue = defaultNaftahParserVisitor.visit(expression);

										if (Builtin.equals(labelValue, controlValue)) {
											org.daiitech.naftah.parser.NaftahParser.BlockContext block = caseStatementContext
													.block(i);
											result = defaultNaftahParserVisitor.visit(block);
											matched = true;
											break;
										}
									}

									if (matched) {
										break;
									}
								}

								if (!matched && caseStatementContext.ELSE() != null) {
									org.daiitech.naftah.parser.NaftahParser.BlockContext elseBlock = caseStatementContext
											.block(
													caseStatementContext
															.block()
															.size() - 1);
									result = defaultNaftahParserVisitor.visit(elseBlock);
								}

								return result;
							}
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitTryStatementWithTryCases(org.daiitech.naftah.parser.NaftahParser.TryStatementWithTryCasesContext ctx) {
		return visitContext(
							this,
							"visitTryStatementWithTryCases",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								tryStatementWithTryCasesContext) -> {
								Result<Object, NaftahBugError> result = null;
								String okVariableName = null;
								String errorVariableName = null;
								DeclaredVariable previousOkVariable = null;
								DeclaredVariable previousErrorVariable = null;

								var tryCases = tryStatementWithTryCasesContext.tryCases();
								try {
									Object expressionResult;
									try {
										expressionResult = defaultNaftahParserVisitor
												.visit(tryStatementWithTryCasesContext.expression());
									}
									catch (Throwable th) {
										var errorCase = tryCases.errorCase();

										if (Objects.nonNull(errorCase)) {
											if (Objects.nonNull(errorCase.ID())) {
												errorVariableName = errorCase.ID().getText();

												result = Result.Error
														.of(th instanceof NaftahBugError naftahBugError ?
																naftahBugError :
																new NaftahBugError(th));

												var declaredVariable = DeclaredVariable
														.of(currentContext.depth,
															tryStatementWithTryCasesContext,
															errorVariableName,
															true,
															JavaType
																	.of(new TypeReference<Result.Error<Object, NaftahBugError>>() {
																	}),
															result);

												boolean errorVarExists = currentContext
														.containsVariable(errorVariableName, currentContext.depth);
												if (errorVarExists) {
													previousErrorVariable = currentContext
															.setVariable(   errorVariableName,
																			declaredVariable);
												}
												else {
													currentContext.defineVariable(errorVariableName, declaredVariable);
												}
											}

											return defaultNaftahParserVisitor.visit(errorCase);
										}
										else {
											throw th;
										}
									}

									var okCase = tryCases.okCase();

									if (Objects.nonNull(okCase) && Objects.nonNull(expressionResult)) {
										okVariableName = okCase.ID().getText();

										result = Result.Ok.of(expressionResult);

										var declaredVariable = DeclaredVariable
												.of(currentContext.depth,
													tryStatementWithTryCasesContext,
													okVariableName,
													true,
													JavaType.of(new TypeReference<Result.Ok<Object, NaftahBugError>>() {
													}),
													result);

										boolean okVarExists = currentContext
												.containsVariable(  okVariableName,
																	currentContext.depth);
										if (okVarExists) {
											previousOkVariable = currentContext
													.setVariable(okVariableName, declaredVariable);
										}
										else {
											currentContext.defineVariable(okVariableName, declaredVariable);
										}

										return defaultNaftahParserVisitor.visit(okCase);
									}
								}
								finally {
									if (Objects.nonNull(okVariableName)) {
										if (Objects.nonNull(previousOkVariable)) {
											currentContext
													.setVariable(   okVariableName,
																	previousOkVariable);
										}
										else {
											currentContext.removeVariable(okVariableName, true);
										}
									}
									if (Objects.nonNull(errorVariableName)) {
										if (Objects.nonNull(previousErrorVariable)) {
											currentContext
													.setVariable(   errorVariableName,
																	previousErrorVariable);
										}
										else {
											currentContext.removeVariable(errorVariableName, true);
										}
									}
								}
								return Objects.nonNull(result) ? result : None.get();
							}
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitTryStatementWithOptionCases(org.daiitech.naftah.parser.NaftahParser.TryStatementWithOptionCasesContext ctx) {
		return visitContext(
							this,
							"visitTryStatementWithOptionCases",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								tryStatementWithOptionCasesContext) -> {
								var result = defaultNaftahParserVisitor
										.visit(tryStatementWithOptionCasesContext.expression());

								String someVariableName = null;
								DeclaredVariable previousSomeVariable = null;

								var tryCases = tryStatementWithOptionCasesContext.optionCases();

								try {
									if (Objects.isNull(result) || None
											.isNone(result) || (result instanceof Optional<?> optional && (optional
													.isEmpty() || None.isNone(optional.get())))) {
										var noneCase = tryCases.noneCase();

										result = defaultNaftahParserVisitor.visit(noneCase);
									}
									else {
										var someCase = tryCases.someCase();
										someVariableName = someCase.ID().getText();

										var declaredVariable = DeclaredVariable
												.of(currentContext.depth,
													tryStatementWithOptionCasesContext,
													someVariableName,
													true,
													JavaType.of(result.getClass()),
													result instanceof Optional<?> optional ?
															optional.get() :
															result);

										boolean okVarExists = currentContext
												.containsVariable(  someVariableName,
																	currentContext.depth);
										if (okVarExists) {
											previousSomeVariable = currentContext
													.setVariable(someVariableName, declaredVariable);
										}
										else {
											currentContext.defineVariable(someVariableName, declaredVariable);
										}

										return defaultNaftahParserVisitor.visit(someCase);
									}

								}
								finally {
									if (Objects.nonNull(someVariableName)) {
										if (Objects.nonNull(previousSomeVariable)) {
											currentContext
													.setVariable(   someVariableName,
																	previousSomeVariable);
										}
										else {
											currentContext.removeVariable(someVariableName, true);
										}
									}
								}
								return result;
							}
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitOkCase(org.daiitech.naftah.parser.NaftahParser.OkCaseContext ctx) {
		return visitContext(
							this,
							"visitOkCase",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								okCaseContext) -> Objects.nonNull(okCaseContext.expression()) ?
										defaultNaftahParserVisitor
												.visit(
														okCaseContext.expression()) :
										defaultNaftahParserVisitor
												.visit(
														okCaseContext.block())
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitErrorCase(org.daiitech.naftah.parser.NaftahParser.ErrorCaseContext ctx) {
		return visitContext(
							this,
							"visitErrorCase",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								errorCaseContext) -> Objects.nonNull(errorCaseContext.expression()) ?
										defaultNaftahParserVisitor
												.visit(
														errorCaseContext.expression()) :
										defaultNaftahParserVisitor
												.visit(
														errorCaseContext.block())
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitSomeCase(org.daiitech.naftah.parser.NaftahParser.SomeCaseContext ctx) {
		return visitContext(
							this,
							"visitSomeCase",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								someCaseContext) -> Objects.nonNull(someCaseContext.expression()) ?
										defaultNaftahParserVisitor
												.visit(
														someCaseContext.expression()) :
										defaultNaftahParserVisitor
												.visit(
														someCaseContext.block())
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitNoneCase(org.daiitech.naftah.parser.NaftahParser.NoneCaseContext ctx) {
		return visitContext(
							this,
							"visitNoneCase",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								noneCaseContext) -> Objects.nonNull(noneCaseContext.expression()) ?
										defaultNaftahParserVisitor
												.visit(
														noneCaseContext.expression()) :
										defaultNaftahParserVisitor
												.visit(
														noneCaseContext.block())
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitExpressionStatement(org.daiitech.naftah.parser.NaftahParser.ExpressionStatementContext ctx) {
		return visitContext(
							this,
							"visitExpressionStatement",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								expressionStatementContext) -> defaultNaftahParserVisitor
										.visit(
												expressionStatementContext.expression())
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitBreakStatement(org.daiitech.naftah.parser.NaftahParser.BreakStatementContext ctx) {
		return visitContext(
							this,
							"visitBreakStatement",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, breakStatementContext) -> {
								if (LOOP_STACK.get().isEmpty() || !checkInsideLoop(breakStatementContext)) {
									throw new NaftahBugError(   String
																		.format("لا يمكن استخدام '%s' خارج نطاق الحلقة.",
																				getFormattedTokenSymbols(
																											defaultNaftahParserVisitor.parser
																													.getVocabulary(),
																											org.daiitech.naftah.parser.NaftahLexer.BREAK,
																											false)),
																breakStatementContext.getStart().getLine(),
																breakStatementContext
																		.getStart()
																		.getCharPositionInLine());
								}
								String currentLoopLabel = currentContext.getLoopLabel();
								String targetLabel = null;
								if (hasChild(breakStatementContext.ID())) {
									targetLabel = breakStatementContext.ID().getText();
								}
								if (targetLabel != null) {
									if (!loopContainsLabel(targetLabel)) {
										throw new NaftahBugError(   String
																			.format("""
																					لا توجد حلقة تحمل التسمية '%s' لاستخدام '%s' معها.""",
																					targetLabel,
																					getFormattedTokenSymbols(   defaultNaftahParserVisitor.parser
																														.getVocabulary(),
																												org.daiitech.naftah.parser.NaftahLexer.BREAK,
																												false)),
																	breakStatementContext.getStart().getLine(),
																	breakStatementContext
																			.getStart()
																			.getCharPositionInLine());
									}
									else if (targetLabel.equals(currentLoopLabel)) {
										throw newNaftahBugInvalidLoopLabelError(currentLoopLabel,
																				defaultNaftahParserVisitor.parser,
																				breakStatementContext
																						.getStart()
																						.getLine(),
																				breakStatementContext
																						.getStart()
																						.getCharPositionInLine());
									}
								}

								return LoopSignal.LoopSignalDetails.of(BREAK, currentLoopLabel, targetLabel);
							}
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitContinueStatement(org.daiitech.naftah.parser.NaftahParser.ContinueStatementContext ctx) {
		return visitContext(
							this,
							"visitContinueStatement",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, continueStatementContext) -> {
								if (LOOP_STACK.get().isEmpty() || !checkInsideLoop(continueStatementContext)) {
									throw new NaftahBugError(   String
																		.format("لا يمكن استخدام '%s' خارج نطاق الحلقة.",
																				getFormattedTokenSymbols(
																											defaultNaftahParserVisitor.parser
																													.getVocabulary(),
																											org.daiitech.naftah.parser.NaftahLexer.CONTINUE,
																											false)),
																continueStatementContext.getStart().getLine(),
																continueStatementContext
																		.getStart()
																		.getCharPositionInLine());
								}
								String currentLoopLabel = currentContext.getLoopLabel();
								String targetLabel = null;
								if (hasChild(continueStatementContext.ID())) {
									targetLabel = continueStatementContext.ID().getText();
								}

								if (targetLabel != null) {
									if (!loopContainsLabel(targetLabel)) {
										throw new NaftahBugError(   String
																			.format("""
																					لا توجد حلقة تحمل التسمية '%s' لاستخدام '%s' معها.""",
																					targetLabel,
																					getFormattedTokenSymbols(   defaultNaftahParserVisitor.parser
																														.getVocabulary(),
																												org.daiitech.naftah.parser.NaftahLexer.CONTINUE,
																												false)),
																	continueStatementContext.getStart().getLine(),
																	continueStatementContext
																			.getStart()
																			.getCharPositionInLine());
									}
									else if (targetLabel.equals(currentLoopLabel)) {
										throw newNaftahBugInvalidLoopLabelError(currentLoopLabel,
																				defaultNaftahParserVisitor.parser,
																				continueStatementContext
																						.getStart()
																						.getLine(),
																				continueStatementContext
																						.getStart()
																						.getCharPositionInLine());
									}
								}

								return LoopSignal.LoopSignalDetails.of(CONTINUE, currentLoopLabel, targetLabel);
							}
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitReturnStatement(org.daiitech.naftah.parser.NaftahParser.ReturnStatementContext ctx) {
		return visitContext(
							this,
							"visitReturnStatement",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, returnStatementContext) -> {
								boolean insideLoop = !LOOP_STACK.get().isEmpty() || checkInsideLoop(
																									returnStatementContext);
								Object result;
								if (Objects.nonNull(returnStatementContext.singleReturn())) {
									result = defaultNaftahParserVisitor.visit(returnStatementContext.singleReturn());
								}
								else {
									result = defaultNaftahParserVisitor.visit(returnStatementContext.multipleReturns());
								}
								return insideLoop ? LoopSignal.LoopSignalDetails.of(RETURN, result) : result;
							}
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitSingleReturn(org.daiitech.naftah.parser.NaftahParser.SingleReturnContext ctx) {
		return visitContext(
							this,
							"visitSingleReturn",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, returnStatementContext) -> {
								Object result = None.get();
								if (hasChild(returnStatementContext.expression())) {
									// Evaluate and return the result
									result = defaultNaftahParserVisitor.visit(returnStatementContext.expression());
								}
								return result;
							}
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitMultipleReturns(org.daiitech.naftah.parser.NaftahParser.MultipleReturnsContext ctx) {
		return visitContext(
							this,
							"visitMultipleReturns",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, returnStatementContext) -> {
								if (hasChild(returnStatementContext.tupleElements())) {
									return defaultNaftahParserVisitor.visit(returnStatementContext.tupleElements());
								}
								else if (hasChild(returnStatementContext.collectionMultipleElements())) {
									return NTuple
											.of(NaftahParserHelper
													.visitCollectionMultipleElements(   defaultNaftahParserVisitor,
																						returnStatementContext
																								.collectionMultipleElements()));
								}
								else {
									return NTuple.of();
								}
							}
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitScopeBlock(org.daiitech.naftah.parser.NaftahParser.ScopeBlockContext ctx) {
		return visitContext(
							this,
							"visitScopeBlock",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, scopeBlockContext) -> {
								startScope();

								defaultNaftahParserVisitor.visit(scopeBlockContext.block());

								List<Task<?>> tasks = CURRENT_TASK_SCOPE.get();
								List<Object> results;

								// Await all tasks spawned inside scope
								if (hasChild(scopeBlockContext.ORDERED())) {
									// Wait in the order tasks were spawned (default)
									results = new ArrayList<>();
									for (Task<?> t : tasks) {
										results.add(t.await());
									}
								}
								else {
									// Wait in completion order
									ExecutorService executor = Executors.newFixedThreadPool(tasks.size());
									try {
										results = new CopyOnWriteArrayList<>();
										// Wrap all tasks in CompletableFutures that just call await() when they finish
										List<CompletableFuture<Void>> futures = tasks
												.stream()
												.map(task -> CompletableFuture
														.supplyAsync(
																		task::await,
																		executor)
														.thenAccept(results::add))
												.toList();

										// wait for all tasks to finish if needed
										CompletableFuture<Void> allDone = CompletableFuture
												.allOf(futures.toArray(new CompletableFuture[0]));
										allDone.join();
									}
									finally {
										executor.shutdown();
									}
								}

								endScope();

								return NTuple.of(results);
							}
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitBlock(org.daiitech.naftah.parser.NaftahParser.BlockContext ctx) {
		return visitContext(
							this,
							"visitBlock",
							getBlockContext(ctx, getCurrentContext()),
							ctx,
							(defaultNaftahParserVisitor, nextContext, blockContext) -> {
								defaultNaftahParserVisitor.depth = nextContext.getDepth();
								Object result = None.get();
								try {
									for (org.daiitech.naftah.parser.NaftahParser.StatementContext statement : blockContext
											.statement()) {
										// Visit each statement in the block
										result = defaultNaftahParserVisitor.visit(statement);
										// break program after executing a return statement
										if (shouldBreakStatementsLoop(nextContext, statement, result)) {
											break;
										}
									}

									return result;
								}
								finally {
									deregisterContext();
									defaultNaftahParserVisitor.depth--;
								}
							}
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitObjectExpression(org.daiitech.naftah.parser.NaftahParser.ObjectExpressionContext ctx) {
		return visitContext(
							this,
							"visitObjectExpression",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								objectExpressionContext) -> defaultNaftahParserVisitor
										.visit(
												objectExpressionContext.object())
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitEmptyObject(org.daiitech.naftah.parser.NaftahParser.EmptyObjectContext ctx) {
		return visitContext(
							this,
							"visitEmptyObject",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, emptyObjectContext) -> NaftahObject
									.of(new LinkedHashMap<>())
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitObjectValue(org.daiitech.naftah.parser.NaftahParser.ObjectValueContext ctx) {
		//noinspection unchecked
		return visitContext(
							this,
							"visitObjectValue",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								objectValueContext) -> NaftahObject
										.of((Map<String, DeclaredVariable>) defaultNaftahParserVisitor
												.visit(objectValueContext.objectFields())),
							NaftahObject.class
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, DeclaredVariable> visitObjectFields(org.daiitech.naftah.parser.NaftahParser.ObjectFieldsContext ctx) {
		//noinspection unchecked
		return (Map<String, DeclaredVariable>) visitContext(
															this,
															"visitObjectFields",
															getCurrentContext(),
															ctx,
															(   defaultNaftahParserVisitor,
																currentContext,
																objectFieldsContext) -> {
																var result = new LinkedHashMap<String, DeclaredVariable>();

																for (   int i = 0;
																		i < objectFieldsContext.assignment().size();
																		i++) {
																	//noinspection unchecked
																	var field = (Pair<DeclaredVariable, Boolean>) defaultNaftahParserVisitor
																			.visit(
																					objectFieldsContext.assignment(i));
																	var fieldName = field.getLeft().getName();
																	result.put(fieldName, field.getLeft());
																}

																currentContext.setCreatingObject(false);

																return result;
															},
															Map.class
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitImplementationDeclarationStatement(org.daiitech.naftah.parser.NaftahParser.ImplementationDeclarationStatementContext ctx) {
		return visitContext(
							this,
							"visitImplementationDeclarationStatement",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								implementationDeclarationStatementContext) -> defaultNaftahParserVisitor
										.visit(
												implementationDeclarationStatementContext.implementationDeclaration())
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitImplementationDeclaration(org.daiitech.naftah.parser.NaftahParser.ImplementationDeclarationContext ctx) {
		return visitContext(
							this,
							"visitImplementationDeclaration",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								implementationDeclarationContext) -> {
								var implementationName = implementationDeclarationContext.ID().getText();
								var objectVariable = getVariable(implementationName, currentContext);
								Map<String, DeclaredVariable> objectFields;
								if (objectVariable.isFound()) {
									if (objectVariable.get() instanceof NaftahObject naftahObject && !naftahObject
											.fromJava()) {
										objectFields = naftahObject.objectFields();
									}
									else {
										throw new NaftahBugError(
																	"لا يمكن تعريف سلوك للاسم '%s' لأنه ليس كائنًا أو هيكلًا صالحًا."
																			.formatted(implementationName),
																	implementationDeclarationContext
																			.getStart()
																			.getLine(),
																	implementationDeclarationContext
																			.getStart()
																			.getCharPositionInLine()
										);
									}
								}
								else {
									throw new NaftahBugError(   """
																لا يمكن تعريف سلوك للكائن '%s'، هذا الكائن غير موجود أو غير مُعرّف في السياق الحالي.
																"""
																		.formatted(implementationName),
																implementationDeclarationContext.getStart().getLine(),
																implementationDeclarationContext
																		.getStart()
																		.getCharPositionInLine());
								}
								//noinspection unchecked
								DeclaredImplementation declaredImplementation = DeclaredImplementation
										.of(depth,
											implementationDeclarationContext,
											objectFields,
											(Map<String, DeclaredFunction<?>>) defaultNaftahParserVisitor
													.visit(implementationDeclarationContext.implementationFunctions())
										);

								currentContext.defineImplementation(implementationName, declaredImplementation);

								return declaredImplementation;
							},
							DeclaredImplementation.class
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitImplementationFunctions(org.daiitech.naftah.parser.NaftahParser.ImplementationFunctionsContext ctx) {
		//noinspection unchecked
		return (Map<String, DeclaredVariable>) visitContext(
															this,
															"visitImplementationFunctions",
															getCurrentContext(),
															ctx,
															(   defaultNaftahParserVisitor,
																currentContext,
																implementationFunctionsContext) -> {
																var implementationName = ((org.daiitech.naftah.parser.NaftahParser.ImplementationDeclarationContext) implementationFunctionsContext
																		.getParent())
																		.ID()
																		.getText();

																var result = new LinkedHashMap<String, DeclaredFunction<?>>();

																for (   int i = 0;
																		i < implementationFunctionsContext
																				.functionDeclaration()
																				.size();
																		i++) {
																	var functionDeclarationContext = implementationFunctionsContext
																			.functionDeclaration(i);
																	var functionName = functionDeclarationContext
																			.ID()
																			.getText();
																	if (result.containsKey(functionName)) {
																		throw newNaftahBugExistentFunctionError(implementationName + QUALIFIED_CALL_SEPARATOR + functionName);
																	}
																	var function = (DeclaredFunction<?>) defaultNaftahParserVisitor
																			.visit(functionDeclarationContext);
																	result.put(functionName, function);
																}

																return result;
															},
															Map.class
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitObjectAccessExpression(org.daiitech.naftah.parser.NaftahParser.ObjectAccessExpressionContext ctx) {
		return visitContext(
							this,
							"visitObjectAccessExpression",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								objectAccessExpressionContext) -> defaultNaftahParserVisitor
										.visit(
												objectAccessExpressionContext.objectAccess())
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitObjectAccess(org.daiitech.naftah.parser.NaftahParser.ObjectAccessContext ctx) {
		return visitContext(
							this,
							"visitObjectAccess",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								objectAccessContext) -> {
								if (Objects.nonNull(objectAccessContext.qualifiedName())) {
									return defaultNaftahParserVisitor
											.visit(
													objectAccessContext.qualifiedName());
								}
								else {
									return defaultNaftahParserVisitor
											.visit(
													objectAccessContext.qualifiedObjectAccess());
								}
							}
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitQualifiedObjectAccess(org.daiitech.naftah.parser.NaftahParser.QualifiedObjectAccessContext ctx) {
		return visitContext(
							this,
							"visitQualifiedObjectAccess",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								qualifiedObjectAccessContext) -> {
								var qualifiedName = getQualifiedName(qualifiedObjectAccessContext);
								return accessObjectUsingQualifiedName(  qualifiedName,
																		currentContext,
																		qualifiedObjectAccessContext
																				.getStart()
																				.getLine(),
																		qualifiedObjectAccessContext
																				.getStart()
																				.getCharPositionInLine());
							}
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitCollectionExpression(org.daiitech.naftah.parser.NaftahParser.CollectionExpressionContext ctx) {
		return visitContext(
							this,
							"visitCollectionExpression",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								collectionExpressionContext) -> defaultNaftahParserVisitor
										.visit(
												collectionExpressionContext.collection())
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitCollectionAccessExpression(org.daiitech.naftah.parser.NaftahParser.CollectionAccessExpressionContext ctx) {
		return visitContext(
							this,
							"visitCollectionAccessExpression",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								collectionAccessExpressionContext) -> defaultNaftahParserVisitor
										.visit(
												collectionAccessExpressionContext.collectionAccess())
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitCollectionAccess(org.daiitech.naftah.parser.NaftahParser.CollectionAccessContext ctx) {
		return visitContext(
							this,
							"visitCollectionAccess",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								collectionAccessContext) -> {
								Object result = getVariable(matchImplementationName(collectionAccessContext.selfOrId(),
																					currentContext), currentContext)
										.get();
								Number number = -1;
								try {
									for (int i = 0; i < collectionAccessContext.collectionAccessIndex().size(); i++) {
										number = (Number) defaultNaftahParserVisitor
												.visit(collectionAccessContext
														.collectionAccessIndex(
																				i));
										if (result instanceof NTuple tuple) {
											result = tuple.get(number.intValue());
										}
										else if (result instanceof List<?> list) {
											result = list.get(number.intValue());
										}
										else if (result instanceof Set<?> set) {
											result = getElementAt(set, number.intValue());
										}
										else {
											throw new NaftahBugError(
																		"""
																		لا يمكن استخدام الفهرسة إلا مع الأنواع التالية: تركيبة , قائمة أو مجموعة.
																		""",
																		collectionAccessContext.getStart().getLine(),
																		collectionAccessContext
																				.getStart()
																				.getCharPositionInLine());
										}
									}
								}
								catch (IndexOutOfBoundsException indexOutOfBoundsException) {
									throw newNaftahIndexOutOfBoundsBugError(number.intValue(),
																			((Collection<?>) result).size(),
																			indexOutOfBoundsException,
																			collectionAccessContext
																					.getStart()
																					.getLine(),
																			collectionAccessContext
																					.getStart()
																					.getCharPositionInLine());
								}
								return result;
							}
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Number visitCollectionAccessIndex(org.daiitech.naftah.parser.NaftahParser.CollectionAccessIndexContext ctx) {
		return visitContext(
							this,
							"visitCollectionAccessIndex",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								collectionAccessIndexContext) -> {
								if (Objects.nonNull(collectionAccessIndexContext.NUMBER())) {
									Object value = collectionAccessIndexContext.NUMBER().getText();
									return NumberUtils.parseDynamicNumber(value);
								}
								else {
									return getVariable(collectionAccessIndexContext.ID().getText(), currentContext)
											.get();
								}
							},
							Number.class
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<?> visitListValue(org.daiitech.naftah.parser.NaftahParser.ListValueContext ctx) {
		return visitContext(
							this,
							"visitListValue",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, listValueContext) -> {
								var value = (List<?>) (Objects.nonNull(listValueContext.elements()) ?
										defaultNaftahParserVisitor
												.visit(
														listValueContext.elements()) :
										List.of());
								return new ArrayList<>(value);
							},
							List.class
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public NTuple visitTupleValue(org.daiitech.naftah.parser.NaftahParser.TupleValueContext ctx) {
		return visitContext(
							this,
							"visitTupleValue",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, tupleValueContext) -> {
								//noinspection unchecked
								return NTuple
										.of((List<Object>) (Objects.nonNull(tupleValueContext.tupleElements()) ?
												defaultNaftahParserVisitor
														.visit(tupleValueContext.tupleElements()) :
												List.of()));
							},
							NTuple.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitEmptySet(org.daiitech.naftah.parser.NaftahParser.EmptySetContext ctx) {
		return visitContext(
							this,
							"visitEmptySet",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, emptySetContext) -> Objects
									.nonNull(emptySetContext.ORDERED()) ?
											new LinkedHashSet<>() :
											new HashSet<>(),
							Set.class
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<?> visitSetValue(org.daiitech.naftah.parser.NaftahParser.SetValueContext ctx) {
		return visitContext(
							this,
							"visitSetValue",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, setValueContext) -> {
								var value = (List<?>) defaultNaftahParserVisitor
										.visit(setValueContext
												.elements());
								return Objects.nonNull(setValueContext.ORDERED()) ?
										new LinkedHashSet<>(value) :
										new HashSet<>(value);
							},
							Set.class
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitEmptyMap(org.daiitech.naftah.parser.NaftahParser.EmptyMapContext ctx) {
		return visitContext(
							this,
							"visitEmptyMap",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, emptyMapContext) -> Objects
									.nonNull(emptyMapContext.ORDERED()) ?
											new LinkedHashMap<>() :
											new HashMap<>(),
							Map.class
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<?, ?> visitMapValue(org.daiitech.naftah.parser.NaftahParser.MapValueContext ctx) {
		return visitContext(
							this,
							"visitMapValue",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, mapValueContext) -> defaultNaftahParserVisitor
									.visit(mapValueContext.keyValuePairs()),
							Map.class
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitSingleElement(org.daiitech.naftah.parser.NaftahParser.SingleElementContext ctx) {
		return visitContext(
							this,
							"visitSingleElement",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, singleElementContext) -> {
								var elementValue = defaultNaftahParserVisitor.visit(singleElementContext.expression());

								return List.of(elementValue);
							},
							List.class
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitTupleSingleElement(org.daiitech.naftah.parser.NaftahParser.TupleSingleElementContext ctx) {
		return visitContext(
							this,
							"visitTupleSingleElement",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, tupleSingleElementContext) -> List
									.of(defaultNaftahParserVisitor.visit(tupleSingleElementContext.expression())),
							List.class
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitMultipleElements(org.daiitech.naftah.parser.NaftahParser.MultipleElementsContext ctx) {
		return visitContext(
							this,
							"visitMultipleElements",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, multipleElementsContext) -> {
								// prepare validations
								boolean creatingSet = hasParentOfType(  multipleElementsContext,
																		org.daiitech.naftah.parser.NaftahParser.SetValueContext.class);

								boolean parsingAssignment = currentContext.isParsingAssignment();
								String currentDeclarationName = null;
								if (parsingAssignment) {
									var currentDeclaration = currentContext.getDeclarationOfAssignment();
									currentDeclarationName = currentDeclaration.getLeft().getName();
								}
								// process elements
								List<Object> elements = new ArrayList<>();
								for (   int i = 0;
										i < multipleElementsContext.collectionMultipleElements().expression().size();
										i++) {
									var elementValue = defaultNaftahParserVisitor
											.visit(multipleElementsContext
													.collectionMultipleElements()
													.expression(i));
									if (creatingSet) {
										// validating set has no duplicates
										if (elements
												.stream()
												.filter(Objects::nonNull)
												.anyMatch(o -> o.equals(elementValue))) {
											throw new NaftahBugError(
																		("""
																			تحتوي المجموعة %s على عناصر مكرّرة، وهذا غير مسموح في المجموعات (Set) التي يجب أن تحتوي على عناصر فريدة فقط.""")
																				.formatted(parsingAssignment ?
																						"'%s'"
																								.formatted(currentDeclarationName) :
																						""),
																		multipleElementsContext.getStart().getLine(),
																		multipleElementsContext
																				.getStart()
																				.getCharPositionInLine());
										}
									}
									elements.add(elementValue);
								}
								return elements;
							},
							List.class
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitTupleMultipleElements(org.daiitech.naftah.parser.NaftahParser.TupleMultipleElementsContext ctx) {
		return visitContext(
							this,
							"visitTupleMultipleElements",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								tupleMultipleElementsContext) -> NaftahParserHelper
										.visitCollectionMultipleElements(   defaultNaftahParserVisitor,
																			tupleMultipleElementsContext
																					.collectionMultipleElements()),
							List.class
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<?, ?> visitKeyValuePairs(org.daiitech.naftah.parser.NaftahParser.KeyValuePairsContext ctx) {
		return visitContext(
							this,
							"visitKeyValuePairs",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, keyValuePairsContext) -> {
								// prepare validations
								boolean creatingMap = hasParentOfType(  keyValuePairsContext,
																		org.daiitech.naftah.parser.NaftahParser.MapValueContext.class);
								boolean parsingAssignment = currentContext.isParsingAssignment();
								String currentDeclarationName = null;
								if (parsingAssignment) {
									var currentDeclaration = currentContext.getDeclarationOfAssignment();
									currentDeclarationName = currentDeclaration.getLeft().getName();
								}
								// process entries
								org.daiitech.naftah.parser.NaftahParser.MapValueContext mapValueContext = (org.daiitech.naftah.parser.NaftahParser.MapValueContext) keyValuePairsContext
										.getParent();
								Map<Object, Object> map = Objects.nonNull(mapValueContext.ORDERED()) ?
										new LinkedHashMap<>() :
										new HashMap<>();
								for (int i = 0; i < keyValuePairsContext.keyValue().size(); i++) {
									var entry = (Map.Entry<?, ?>) defaultNaftahParserVisitor
											.visit(keyValuePairsContext.keyValue(i));
									var key = entry.getKey();
									var value = entry.getValue();

									if (creatingMap) {
										// validating keys has all the same type
										// validating null keys
										if (Objects.isNull(key)) {
											throw new NaftahBugError(
																		("""
																			لا يمكن أن يكون أحد المفاتيح في المصفوفة الترابطية (Map) %s فارغًا (null). يجب أن تكون جميع المفاتيح معرّفة بشكل صحيح.""")
																				.formatted(parsingAssignment ?
																						"'%s'"
																								.formatted(currentDeclarationName) :
																						""),
																		keyValuePairsContext.getStart().getLine(),
																		keyValuePairsContext
																				.getStart()
																				.getCharPositionInLine());
										}

										// validating keySet has no duplicates
										if (map.containsKey(key)) {
											throw new NaftahBugError(
																		("""
																			تحتوي مجموعة المفاتيح للمصفوفة الترابطية %s على مفاتيح مكرّرة، وهذا غير مسموح في المصفوفة الترابطية (Map) التي يجب أن تحتوي على مفاتيح فريدة فقط.""")
																				.formatted(parsingAssignment ?
																						"'%s'"
																								.formatted(currentDeclarationName) :
																						""),
																		keyValuePairsContext.getStart().getLine(),
																		keyValuePairsContext
																				.getStart()
																				.getCharPositionInLine());
										}
									}
									map.put(key, value);
								}
								return map;
							},
							Map.class
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map.Entry<?, ?> visitKeyValue(org.daiitech.naftah.parser.NaftahParser.KeyValueContext ctx) {
		return visitContext(
							this,
							"visitKeyValue",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, keyValueContext) -> {
								var key = defaultNaftahParserVisitor.visit(keyValueContext.expression(0));
								var value = defaultNaftahParserVisitor.visit(keyValueContext.expression(1));
								// prepare validations
								boolean creatingMap = hasAnyParentOfType(   keyValueContext,
																			org.daiitech.naftah.parser.NaftahParser.MapValueContext.class);
								if (!creatingMap && Objects.isNull(key)) {
									throw newNaftahBugNullError(
																keyValueContext.getStart().getLine(),
																keyValueContext.getStart().getCharPositionInLine());
								}
								return new AbstractMap.SimpleEntry<>(key, value);
							},
							Map.Entry.class
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitValueExpression(org.daiitech.naftah.parser.NaftahParser.ValueExpressionContext ctx) {
		return visitContext(
							this,
							"visitValueExpression",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								valueExpressionContext) -> defaultNaftahParserVisitor
										.visit(valueExpressionContext.value())
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JavaType visitTypeExpression(org.daiitech.naftah.parser.NaftahParser.TypeExpressionContext ctx) {
		return visitContext(
							this,
							"visitTypeExpression",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								typeExpressionContext) -> defaultNaftahParserVisitor
										.visit(typeExpressionContext.type()),
							JavaType.class
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitParenthesisExpression(org.daiitech.naftah.parser.NaftahParser.ParenthesisExpressionContext ctx) {
		return visitContext(
							this,
							"visitParenthesisExpression",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								parenthesisExpressionContext) -> defaultNaftahParserVisitor
										.visit(
												parenthesisExpressionContext.expression())
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitInitCallExpression(org.daiitech.naftah.parser.NaftahParser.InitCallExpressionContext ctx) {
		return visitContext(
							this,
							"visitInitCallExpression",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								initCallExpressionContext) -> defaultNaftahParserVisitor
										.visit(
												initCallExpressionContext.initCall())
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitFunctionCallExpression(org.daiitech.naftah.parser.NaftahParser.FunctionCallExpressionContext ctx) {
		return visitContext(
							this,
							"visitFunctionCallExpression",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								functionCallExpressionContext) -> defaultNaftahParserVisitor
										.visit(
												functionCallExpressionContext.functionCall())
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitNumberValue(org.daiitech.naftah.parser.NaftahParser.NumberValueContext ctx) {
		return visitContext(
							this,
							"visitNumberValue",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, numberValueContext) -> {
								Object value = numberValueContext.NUMBER().getText();
								return NumberUtils.parseDynamicNumber(value);
							}
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitRadixNumberValue(org.daiitech.naftah.parser.NaftahParser.RadixNumberValueContext ctx) {
		return visitContext(
							this,
							"visitRadixNumberValue",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, radixNumberValueContext) -> {
								String originalValue = radixNumberValueContext.BASE_DIGITS().getText();
								String arabicIndicValue = originalValue
										.substring( 1,
													originalValue.length() - 2);
								String value = ScriptUtils
										.convertArabicToLatinLetterByLetter(arabicIndicValue);
								String originalRadix = radixNumberValueContext.BASE_RADIX().getText();
								int radix = Integer
										.parseInt(originalRadix
												.substring( 0,
															originalRadix.length() - 1));

								return NumberUtils.parseDynamicNumber(value, radix, arabicIndicValue);
							}
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitCharacterValue(org.daiitech.naftah.parser.NaftahParser.CharacterValueContext ctx) {
		return visitContext(
							this,
							"visitCharacterValue",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, characterValueContext) -> characterValueContext
									.CHARACTER()
									.getText()
									.charAt(1)
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitStringValue(org.daiitech.naftah.parser.NaftahParser.StringValueContext ctx) {
		return visitContext(
							this,
							"visitStringValue",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, stringValueContext) -> {
								String value = stringValueContext.STRING().getText();
								Object result;
								if (Objects.nonNull(stringValueContext.RAW())) {
									result = StringInterpolator.cleanInput(value);
								}
								else {
									result = value = StringInterpolator.process(value, currentContext);
									if (Objects.nonNull(stringValueContext.TEMPORAL_POINT())) {
										try {
											result = NaftahDateParserHelper.run(value, NaftahTemporalPoint.class);
										}
										catch (Throwable throwable) {
											throw new NaftahBugError(   """
																		فشل تحليل النقطة الزمنية: '%s'
																		الرجاء استخدام صيغة مشابهة للأمثلة التالية:
																		٣٠ أكتوبر ٢٠٢٢ بالتقويم الميلادي ٢٢:١٥ بتوقيت تونس
																		صفر ١٤٤٣ بالتقويم الهجري ٠٨:٢٠:٤٥ بتوقيت بيروت
																		"""
																				.formatted(value),
																		throwable,
																		stringValueContext.getStart().getLine(),
																		stringValueContext
																				.getStart()
																				.getCharPositionInLine());
										}
									}
									else if (Objects.nonNull(stringValueContext.TEMPORAL_AMOUNT())) {
										try {
											result = NaftahDateParserHelper.run(value, NaftahTemporalAmount.class);
										}
										catch (Throwable throwable) {
											throw new NaftahBugError(   """
																		فشل تحليل القيمة الزمنية: '%s'
																		الرجاء استخدام صيغة مشابهة للأمثلة التالية:
																		مقدار_زمني "مدة 3 ساعات"
																		قيمة_زمنية "مدة 1 ساعة و15 دقيقة"
																		قيمة_زمنية "مدة 2 ساعات و10 دقائق و5 ثوانٍ"
																		مقدار_زمني "مدة 1 ثانية و 500 نانوثانية"
																		قيمة_زمنية "مدة 1 ساعة و 30.75 ثانية"
																		مقدار_زمني "فترة 1 سنة"
																		مقدار_زمني "فترة 5 سنوات"
																		قيمة_زمنية "فترة 1 شهر و 10 أيام"
																		قيمة_زمنية "فترة 14 يوم"
																		"""
																				.formatted(value),
																		throwable,
																		stringValueContext.getStart().getLine(),
																		stringValueContext
																				.getStart()
																				.getCharPositionInLine());
										}
									}
									else if (Objects.nonNull(stringValueContext.BYTE_ARRAY())) {
										result = value.getBytes(StandardCharsets.UTF_8);
									}
								}

								return result;
							}
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitTrueValue(org.daiitech.naftah.parser.NaftahParser.TrueValueContext ctx) {
		return visitContext(
							this,
							"visitTrueValue",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, trueValueContext) -> Boolean.TRUE
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitFalseValue(org.daiitech.naftah.parser.NaftahParser.FalseValueContext ctx) {
		return visitContext(
							this,
							"visitFalseValue",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, falseValueContext) -> Boolean.FALSE
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitNullValue(org.daiitech.naftah.parser.NaftahParser.NullValueContext ctx) {
		return visitContext(
							this,
							"visitNullValue",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, nullValueContext) -> None.get()
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitIdValue(org.daiitech.naftah.parser.NaftahParser.IdValueContext ctx) {
		return visitContext(
							this,
							"visitIdValue",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, idValueContext) -> {
								// prepare validations
								boolean creatingMap = hasAnyParentOfType(   idValueContext,
																			org.daiitech.naftah.parser.NaftahParser.MapValueContext.class);
								boolean creatingObject = hasAnyParentOfType(idValueContext,
																			org.daiitech.naftah.parser.NaftahParser.ObjectContext.class);
								String id = idValueContext.ID().getText();

								return creatingMap || creatingObject ?
										id :
										getVariable(id, currentContext).orElse(None.get());
							}
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public JavaType visitVoidReturnType(org.daiitech.naftah.parser.NaftahParser.VoidReturnTypeContext ctx) {
		return visitContext(
							this,
							"visitVoidReturnType",
							getCurrentContext(),
							ctx,
							ObjectUtils::getJavaType,
							JavaType.class
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public JavaType visitTypeReturnType(org.daiitech.naftah.parser.NaftahParser.TypeReturnTypeContext ctx) {
		return visitContext(
							this,
							"visitTypeReturnType",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								typeReturnTypeContext) -> defaultNaftahParserVisitor
										.visit(typeReturnTypeContext.type()),
							JavaType.class
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public JavaType visitVarType(org.daiitech.naftah.parser.NaftahParser.VarTypeContext ctx) {
		return visitContext(
							this,
							"visitVarType",
							getCurrentContext(),
							ctx,
							ObjectUtils::getJavaType,
							JavaType.class
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JavaType visitComplexType(org.daiitech.naftah.parser.NaftahParser.ComplexTypeContext ctx) {
		return visitContext(
							this,
							"visitComplexType",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								complexTypeContext) -> defaultNaftahParserVisitor
										.visit(
												complexTypeContext.complexBuiltIn()),
							JavaType.class
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JavaType visitBuiltInType(org.daiitech.naftah.parser.NaftahParser.BuiltInTypeContext ctx) {
		return visitContext(
							this,
							"visitBuiltInType",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								builtInTypeContext) -> defaultNaftahParserVisitor
										.visit(
												builtInTypeContext.builtIn()),
							JavaType.class
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JavaType visitComplexBuiltIn(org.daiitech.naftah.parser.NaftahParser.ComplexBuiltInContext ctx) {
		return visitContext(
							this,
							"visitComplexBuiltIn",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, complexBuiltInContext) -> getJavaType(
																												defaultNaftahParserVisitor,
																												complexBuiltInContext),
							JavaType.class
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JavaType visitBuiltIn(org.daiitech.naftah.parser.NaftahParser.BuiltInContext ctx) {
		return visitContext(
							this,
							"visitBuiltIn",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, builtInContext) -> getJavaType(builtInContext),
							JavaType.class
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public JavaType visitQualifiedNameType(org.daiitech.naftah.parser.NaftahParser.QualifiedNameTypeContext ctx) {
		return visitContext(
							this,
							"visitQualifiedNameType",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								qualifiedNameTypeContext) -> getJavaType(currentContext, qualifiedNameTypeContext),
							JavaType.class
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitQualifiedName(org.daiitech.naftah.parser.NaftahParser.QualifiedNameContext ctx) {
		return visitContext(
							this,
							"visitQualifiedName",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, qualifiedNameContext) -> {
								Object result;
								boolean accessingObjectField = hasAnyParentOfType(
																					qualifiedNameContext,
																					org.daiitech.naftah.parser.NaftahParser.ObjectAccessExpressionContext.class);
								boolean definingImport = hasAnyParentOfType(
																			qualifiedNameContext,
																			org.daiitech.naftah.parser.NaftahParser.ImportStatementContext.class);

								if (accessingObjectField) {
									var qualifiedName = getQualifiedName(qualifiedNameContext);
									result = accessObjectUsingQualifiedName(qualifiedName,
																			currentContext,
																			qualifiedNameContext.getStart().getLine(),
																			qualifiedNameContext
																					.getStart()
																					.getCharPositionInLine());
								}
								else if (definingImport || currentContext.isParsingFunctionCallId()) {
									result = getQualifiedName(qualifiedNameContext);
									if (currentContext.isParsingFunctionCallId()) {
										currentContext.setParsingFunctionCallId(false);
									}
								}
								else {
									result = getJavaType(   defaultNaftahParserVisitor,
															currentContext,
															qualifiedNameContext);
								}

								return result;
							}
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String visitLabel(org.daiitech.naftah.parser.NaftahParser.LabelContext ctx) {
		return visitContext(
							this,
							"visitLabel",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, labelContext) -> labelContext.ID().getText(),
							String.class
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitExpression(org.daiitech.naftah.parser.NaftahParser.ExpressionContext ctx) {
		return visitContext(
							this,
							"visitExpression",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								expressionContext) -> defaultNaftahParserVisitor
										.visit(expressionContext.ternaryExpression()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitTernaryExpression(org.daiitech.naftah.parser.NaftahParser.TernaryExpressionContext ctx) {
		return visitContext(
							this,
							"visitTernaryExpression",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, ternaryExpressionContext) -> {
								if (Objects.nonNull(ternaryExpressionContext.QUESTION())) {
									// ternary expression: condition ? thenExpression : elseExpression
									Object condition = defaultNaftahParserVisitor
											.visit(ternaryExpressionContext.nullishExpression());
									return isTruthy(condition) ?
											defaultNaftahParserVisitor.visit(ternaryExpressionContext.expression()) :
											defaultNaftahParserVisitor
													.visit(ternaryExpressionContext.ternaryExpression());
								}
								else {
									return defaultNaftahParserVisitor
											.visit(ternaryExpressionContext.nullishExpression());
								}
							});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitNullishExpression(org.daiitech.naftah.parser.NaftahParser.NullishExpressionContext ctx) {
		return visitContext(
							this,
							"visitLogicalExpression",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, nullishExpressionContext) -> {
								Object left = defaultNaftahParserVisitor
										.visit(nullishExpressionContext.logicalExpression(0));

								for (int i = 1; i < nullishExpressionContext.logicalExpression().size(); i++) {
									left = isTruthy(left) ?
											left :
											defaultNaftahParserVisitor
													.visit(nullishExpressionContext
															.logicalExpression(
																				i));
								}

								return left;
							});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitLogicalExpression(org.daiitech.naftah.parser.NaftahParser.LogicalExpressionContext ctx) {
		return visitContext(
							this,
							"visitLogicalExpression",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, logicalExpressionContext) -> {
								Object left = defaultNaftahParserVisitor
										.visit(logicalExpressionContext.bitwiseExpression(0));

								for (int i = 1; i < logicalExpressionContext.bitwiseExpression().size(); i++) {
									Object right = defaultNaftahParserVisitor
											.visit(logicalExpressionContext
													.bitwiseExpression(
																		i));

									String op = NaftahParserHelper
											.getDisplayName(logicalExpressionContext.getChild(2 * i - 1),
															defaultNaftahParserVisitor.parser.getVocabulary());
									left = applyOperation(left, right, BinaryOperation.of(op));
								}

								return left;
							});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitBitwiseExpression(org.daiitech.naftah.parser.NaftahParser.BitwiseExpressionContext ctx) {
		return visitContext(
							this,
							"visitBitwiseExpression",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, bitwiseExpressionContext) -> {
								Object left = defaultNaftahParserVisitor
										.visit(bitwiseExpressionContext.equalityExpression(0));

								for (int i = 1; i < bitwiseExpressionContext.equalityExpression().size(); i++) {
									Object right = defaultNaftahParserVisitor
											.visit(bitwiseExpressionContext
													.equalityExpression(
																		i));

									String op = NaftahParserHelper
											.getDisplayName(bitwiseExpressionContext.getChild(2 * i - 1),
															defaultNaftahParserVisitor.parser.getVocabulary());
									left = applyOperation(left, right, BinaryOperation.of(op));
								}

								return left;
							});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitEqualityExpression(org.daiitech.naftah.parser.NaftahParser.EqualityExpressionContext ctx) {
		return visitContext(
							this,
							"visitEqualityExpression",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, equalityExpressionContext) -> {
								Object left = defaultNaftahParserVisitor
										.visit(equalityExpressionContext.relationalExpression(0));

								for (int i = 1; i < equalityExpressionContext.relationalExpression().size(); i++) {
									Object right = defaultNaftahParserVisitor
											.visit(equalityExpressionContext
													.relationalExpression(
																			i));

									String op = NaftahParserHelper
											.getDisplayName(equalityExpressionContext.getChild(2 * i - 1),
															defaultNaftahParserVisitor.parser.getVocabulary());
									left = applyOperation(left, right, BinaryOperation.of(op));
								}

								return left;
							});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitRelationalExpression(org.daiitech.naftah.parser.NaftahParser.RelationalExpressionContext ctx) {
		return visitContext(
							this,
							"visitRelationalExpression",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, relationalExpressionContext) -> {
								Object left = defaultNaftahParserVisitor
										.visit(relationalExpressionContext.shiftExpression(0));

								for (int i = 1; i < relationalExpressionContext.shiftExpression().size(); i++) {
									Object right = defaultNaftahParserVisitor
											.visit(relationalExpressionContext
													.shiftExpression(i));

									String op = NaftahParserHelper
											.getDisplayName(relationalExpressionContext.getChild(2 * i - 1),
															defaultNaftahParserVisitor.parser.getVocabulary());
									BinaryOperation binaryOperation = BinaryOperation.of(op);
									if (INSTANCE_OF.equals(binaryOperation)) {
										left = binaryOperation.apply(left, right);
									}
									else {
										left = applyOperation(left, right, binaryOperation);
									}

								}

								return left;
							});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitShiftExpression(org.daiitech.naftah.parser.NaftahParser.ShiftExpressionContext ctx) {
		return visitContext(
							this,
							"visitShiftExpression",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, shiftExpressionContext) -> {
								Object left = defaultNaftahParserVisitor
										.visit(shiftExpressionContext.additiveExpression(0));

								for (int i = 1; i < shiftExpressionContext.additiveExpression().size(); i++) {
									Object right = defaultNaftahParserVisitor
											.visit(shiftExpressionContext
													.additiveExpression(
																		i));

									String op = NaftahParserHelper
											.getDisplayName(shiftExpressionContext.getChild(2 * i - 1),
															defaultNaftahParserVisitor.parser.getVocabulary());
									left = applyOperation(left, right, BinaryOperation.of(op));
								}

								return left;
							});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitAdditiveExpression(org.daiitech.naftah.parser.NaftahParser.AdditiveExpressionContext ctx) {
		return visitContext(
							this,
							"visitAdditiveExpression",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, additiveExpressionContext) -> {
								Object left = defaultNaftahParserVisitor
										.visit(additiveExpressionContext.multiplicativeExpression(0));

								for (int i = 1; i < additiveExpressionContext.multiplicativeExpression().size(); i++) {
									Object right = defaultNaftahParserVisitor
											.visit(additiveExpressionContext
													.multiplicativeExpression(
																				i));

									String op = NaftahParserHelper
											.getDisplayName(additiveExpressionContext.getChild(2 * i - 1),
															defaultNaftahParserVisitor.parser.getVocabulary());
									left = applyOperation(left, right, BinaryOperation.of(op));
								}

								return left;
							});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitMultiplicativeExpression(org.daiitech.naftah.parser.NaftahParser.MultiplicativeExpressionContext ctx) {
		return visitContext(
							this,
							"visitMultiplicativeExpression",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, multiplicativeExpressionContext) -> {
								Object left = defaultNaftahParserVisitor
										.visit(multiplicativeExpressionContext.powerExpression(0));

								for (int i = 1; i < multiplicativeExpressionContext.powerExpression().size(); i++) {
									Object right = defaultNaftahParserVisitor
											.visit(multiplicativeExpressionContext
													.powerExpression(
																		i));

									String op = NaftahParserHelper
											.getDisplayName(multiplicativeExpressionContext.getChild(2 * i - 1),
															defaultNaftahParserVisitor.parser.getVocabulary());
									left = applyOperation(left, right, BinaryOperation.of(op));
								}

								return left;
							});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitPowerExpression(org.daiitech.naftah.parser.NaftahParser.PowerExpressionContext ctx) {
		return visitContext(
							this,
							"visitPowerExpression",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, powerExpressionContext) -> {
								if (Objects.nonNull(powerExpressionContext.POW())) {
									Object left = defaultNaftahParserVisitor
											.visit(powerExpressionContext.unaryExpression());
									Object right = defaultNaftahParserVisitor
											.visit(powerExpressionContext.powerExpression());

									String op = NaftahParserHelper
											.getDisplayName(powerExpressionContext.POW(),
															defaultNaftahParserVisitor.parser.getVocabulary());
									return applyOperation(left, right, BinaryOperation.of(op));
								}
								else {
									return defaultNaftahParserVisitor.visit(powerExpressionContext.unaryExpression());
								}
							});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitSpawnUnaryExpression(org.daiitech.naftah.parser.NaftahParser.SpawnUnaryExpressionContext ctx) {
		return visitContext(
							this,
							"visitSpawnUnaryExpression",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, spawnUnaryExpressionContext) -> {
								var possibleFunctionCallContext = getFirstChildOfType(  spawnUnaryExpressionContext,
																						org.daiitech.naftah.parser.NaftahParser.FunctionCallContext.class);
								if (Objects.nonNull(possibleFunctionCallContext)) {
									boolean hasQualifiedCall = hasChild(possibleFunctionCallContext
											.primaryCall()
											.qualifiedCall());
									String functionName = hasQualifiedCall ?
											(String) defaultNaftahParserVisitor
													.visit(possibleFunctionCallContext
															.primaryCall()
															.qualifiedCall()) :
											matchImplementationName(possibleFunctionCallContext
													.primaryCall()
													.selfOrId(), currentContext);

									if (currentContext.containsFunction(functionName, -1)) {
										Object function = currentContext.getFunction(functionName, false).getRight();
										if (function instanceof DeclaredFunction<?> declaredFunction && declaredFunction
												.isAsync()) {
											throw new NaftahBugError(
																		"""
																		الدالة غير المتزامنة (async) '%s' لا يمكن تشغيلها باستخدام أمر '%s'.
																		"""
																				.formatted(
																							functionName,
																							getFormattedTokenSymbols(
																														defaultNaftahParserVisitor.parser
																																.getVocabulary(),
																														org.daiitech.naftah.parser.NaftahLexer.AWAIT,
																														false)),
																		possibleFunctionCallContext
																				.getStart()
																				.getLine(),
																		possibleFunctionCallContext
																				.getStart()
																				.getCharPositionInLine()
											);
										}
									}
								}
								return spawnTask(   currentContext,
													() -> defaultNaftahParserVisitor
															.visit(spawnUnaryExpressionContext.unaryExpression()),
													currentContext::cleanThreadLocals);
							});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitAwaitUnaryExpression(org.daiitech.naftah.parser.NaftahParser.AwaitUnaryExpressionContext ctx) {
		return visitContext(
							this,
							"visitAwaitUnaryExpression",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, awaitUnaryExpressionContext) -> {
								currentContext.setAwaitingTask(true);
								Object obj = visit(awaitUnaryExpressionContext.unaryExpression());
								currentContext.setAwaitingTask(false);
								if (obj instanceof Task<?> task) {
									return task.await();
								}
								throw new NaftahBugError("""
															لا يمكن استخدام '%s' على غير '%s'. يجب أن يكون القيمة المراد انتظارها من نوع شغل تم إنشاؤه باستخدام '%s' أو دالة غير متزامنة.
															"""
										.formatted(
													getFormattedTokenSymbols(
																				defaultNaftahParserVisitor.parser
																						.getVocabulary(),
																				org.daiitech.naftah.parser.NaftahLexer.AWAIT,
																				false),
													getFormattedTokenSymbols(
																				defaultNaftahParserVisitor.parser
																						.getVocabulary(),
																				org.daiitech.naftah.parser.NaftahLexer.ASYNC,
																				false),
													getFormattedTokenSymbols(
																				defaultNaftahParserVisitor.parser
																						.getVocabulary(),
																				org.daiitech.naftah.parser.NaftahLexer.SPAWN,
																				false)));
							}
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitPrefixUnaryExpression(org.daiitech.naftah.parser.NaftahParser.PrefixUnaryExpressionContext ctx) {
		return visitContext(
							this,
							"visitPrefixUnaryExpression",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, prefixUnaryExpressionContext) -> {
								Object value = defaultNaftahParserVisitor
										.visit(prefixUnaryExpressionContext.unaryExpression());
								Object result;

								String op = NaftahParserHelper
										.getDisplayName(prefixUnaryExpressionContext.getChild(0),
														defaultNaftahParserVisitor.parser.getVocabulary());
								if (INCREMENT.equals(op) || DECREMENT.equals(op)) {
									op = PRE + op;
								}

								UnaryOperation unaryOperation = UnaryOperation.of(op);
								if (TYPE_OF.equals(unaryOperation) || SIZE_OF.equals(unaryOperation)) {
									result = unaryOperation.apply(value);
								}
								else {
									result = applyOperation(value, unaryOperation);
								}

								return result;
							});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitPostfixUnaryExpression(org.daiitech.naftah.parser.NaftahParser.PostfixUnaryExpressionContext ctx) {
		return visitContext(
							this,
							"visitPostfixUnaryExpression",
							getCurrentContext(),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								postfixUnaryExpressionContext) -> defaultNaftahParserVisitor
										.visit(postfixUnaryExpressionContext.postfixExpression()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitPostfixExpression(org.daiitech.naftah.parser.NaftahParser.PostfixExpressionContext ctx) {
		return visitContext(
							this,
							"visitPostfixExpression",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, postfixExpressionContext) -> {
								Object value = defaultNaftahParserVisitor
										.visit(postfixExpressionContext.primary());
								Object result;
								if (postfixExpressionContext.getChildCount() == 2) {
									String op = NaftahParserHelper
											.getDisplayName(postfixExpressionContext.getChild(1),
															defaultNaftahParserVisitor.parser.getVocabulary());
									if (INCREMENT.equals(op) || DECREMENT.equals(op)) {
										op = POST + op;
									}
									result = applyOperation(value, UnaryOperation.of(op));
								}
								else {
									result = value;
								}

								return result;
							});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitNanValue(org.daiitech.naftah.parser.NaftahParser.NanValueContext ctx) {
		return visitContext(
							this,
							"visitNanValue",
							getCurrentContext(),
							ctx,
							(defaultNaftahParserVisitor, currentContext, nanValueContext) -> NaN.get()
		);
	}
}
