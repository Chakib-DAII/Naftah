package org.daiitech.naftah.parser;

import java.lang.reflect.InvocationTargetException;
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
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.tree.ParseTree;
import org.daiitech.naftah.builtin.Builtin;
import org.daiitech.naftah.builtin.lang.BuiltinFunction;
import org.daiitech.naftah.builtin.lang.DeclaredFunction;
import org.daiitech.naftah.builtin.lang.DeclaredParameter;
import org.daiitech.naftah.builtin.lang.DeclaredVariable;
import org.daiitech.naftah.builtin.lang.DynamicNumber;
import org.daiitech.naftah.builtin.lang.JvmFunction;
import org.daiitech.naftah.builtin.lang.NaN;
import org.daiitech.naftah.builtin.lang.None;
import org.daiitech.naftah.builtin.lang.Result;
import org.daiitech.naftah.builtin.utils.NumberUtils;
import org.daiitech.naftah.builtin.utils.Tuple;
import org.daiitech.naftah.builtin.utils.op.BinaryOperation;
import org.daiitech.naftah.builtin.utils.op.UnaryOperation;
import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.utils.arabic.ArabicUtils;

import static org.daiitech.naftah.builtin.utils.CollectionUtils.getElementAt;
import static org.daiitech.naftah.builtin.utils.CollectionUtils.newNaftahIndexOutOfBoundsBugError;
import static org.daiitech.naftah.builtin.utils.CollectionUtils.setElementAt;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.applyOperation;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.getJavaType;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.getNaftahType;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.isEmpty;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.isTruthy;
import static org.daiitech.naftah.builtin.utils.Tuple.newNaftahBugNullError;
import static org.daiitech.naftah.builtin.utils.op.BinaryOperation.ADD;
import static org.daiitech.naftah.builtin.utils.op.BinaryOperation.GREATER_THAN_EQUALS;
import static org.daiitech.naftah.builtin.utils.op.BinaryOperation.LESS_THAN;
import static org.daiitech.naftah.builtin.utils.op.BinaryOperation.LESS_THAN_EQUALS;
import static org.daiitech.naftah.builtin.utils.op.BinaryOperation.SUBTRACT;
import static org.daiitech.naftah.builtin.utils.op.UnaryOperation.DECREMENT;
import static org.daiitech.naftah.builtin.utils.op.UnaryOperation.INCREMENT;
import static org.daiitech.naftah.builtin.utils.op.UnaryOperation.POST;
import static org.daiitech.naftah.builtin.utils.op.UnaryOperation.PRE;
import static org.daiitech.naftah.builtin.utils.op.UnaryOperation.PRE_DECREMENT;
import static org.daiitech.naftah.builtin.utils.op.UnaryOperation.PRE_INCREMENT;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidLoopLabelError;
import static org.daiitech.naftah.parser.DefaultContext.LOOP_STACK;
import static org.daiitech.naftah.parser.DefaultContext.currentLoopLabel;
import static org.daiitech.naftah.parser.DefaultContext.generateCallId;
import static org.daiitech.naftah.parser.DefaultContext.getContextByDepth;
import static org.daiitech.naftah.parser.DefaultContext.getVariable;
import static org.daiitech.naftah.parser.DefaultContext.loopContainsLabel;
import static org.daiitech.naftah.parser.DefaultContext.popCall;
import static org.daiitech.naftah.parser.DefaultContext.popLoop;
import static org.daiitech.naftah.parser.DefaultContext.pushCall;
import static org.daiitech.naftah.parser.DefaultContext.pushLoop;
import static org.daiitech.naftah.parser.LoopSignal.BREAK;
import static org.daiitech.naftah.parser.LoopSignal.CONTINUE;
import static org.daiitech.naftah.parser.LoopSignal.RETURN;
import static org.daiitech.naftah.parser.NaftahParserHelper.accessObjectUsingQualifiedName;
import static org.daiitech.naftah.parser.NaftahParserHelper.checkInsideLoop;
import static org.daiitech.naftah.parser.NaftahParserHelper.checkLoopSignal;
import static org.daiitech.naftah.parser.NaftahParserHelper.createDeclaredVariable;
import static org.daiitech.naftah.parser.NaftahParserHelper.deregisterContextByDepth;
import static org.daiitech.naftah.parser.NaftahParserHelper.getBlockContext;
import static org.daiitech.naftah.parser.NaftahParserHelper.getFormattedTokenSymbols;
import static org.daiitech.naftah.parser.NaftahParserHelper.getQualifiedName;
import static org.daiitech.naftah.parser.NaftahParserHelper.getRootContext;
import static org.daiitech.naftah.parser.NaftahParserHelper.hasAnyParentOfType;
import static org.daiitech.naftah.parser.NaftahParserHelper.hasChild;
import static org.daiitech.naftah.parser.NaftahParserHelper.hasChildOrSubChildOfType;
import static org.daiitech.naftah.parser.NaftahParserHelper.hasParentOfType;
import static org.daiitech.naftah.parser.NaftahParserHelper.prepareDeclaredFunction;
import static org.daiitech.naftah.parser.NaftahParserHelper.prepareDeclaredFunctionArguments;
import static org.daiitech.naftah.parser.NaftahParserHelper.setForeachVariables;
import static org.daiitech.naftah.parser.NaftahParserHelper.setObjectUsingQualifiedName;
import static org.daiitech.naftah.parser.NaftahParserHelper.shouldBreakStatementsLoop;
import static org.daiitech.naftah.parser.NaftahParserHelper.typeMismatch;
import static org.daiitech.naftah.parser.NaftahParserHelper.visitContext;

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
	/**
	 * Current depth in the parse tree traversal.
	 */
	private int depth = 0;

	/**
	 * Constructs the visitor with a given parser.
	 *
	 * @param parser the ANTLR-generated Naftah parser instance
	 */
	public DefaultNaftahParserVisitor(org.daiitech.naftah.parser.NaftahParser parser) {
		this.parser = parser;
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
								defaultNaftahParserVisitor.depth = currentContext.getDepth();
								Object result = None.get();
								for (org.daiitech.naftah.parser.NaftahParser.StatementContext statement : programContext
										.statement()) {
									result = defaultNaftahParserVisitor.visit(statement); // Visit each statement in the program
									// break program after executing a return statement
									if (shouldBreakStatementsLoop(currentContext, statement, result)) {
										break;
									}
								}
								NaftahParserHelper.deregisterContextByDepth(defaultNaftahParserVisitor.depth);
								return result;
							}
		);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitObjectAccessStatement(org.daiitech.naftah.parser.NaftahParser.ObjectAccessStatementContext ctx) {
		return visitContext(
							this,
							"visitObjectAccessStatement",
							getContextByDepth(depth),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								objectAccessStatementContext) -> defaultNaftahParserVisitor
										.visit(objectAccessStatementContext.objectAccess())
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
							getContextByDepth(depth),
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
	public Object visitAssignmentStatement(org.daiitech.naftah.parser.NaftahParser.AssignmentStatementContext ctx) {
		return visitContext(
							this,
							"visitAssignmentStatement",
							getContextByDepth(depth),
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
							getContextByDepth(depth),
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
	public Object visitFunctionCallStatement(org.daiitech.naftah.parser.NaftahParser.FunctionCallStatementContext ctx) {
		return visitContext(
							this,
							"visitFunctionCallStatement",
							getContextByDepth(depth),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								functionCallStatementContext) -> defaultNaftahParserVisitor
										.visit(
												functionCallStatementContext.functionCall())
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
							getContextByDepth(depth),
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
							getContextByDepth(depth),
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
							getContextByDepth(depth),
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
							getContextByDepth(depth),
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
							getContextByDepth(depth),
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
							getContextByDepth(depth),
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
							getContextByDepth(depth),
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
							getContextByDepth(depth),
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
							getContextByDepth(depth),
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
	public Object visitBlockStatement(org.daiitech.naftah.parser.NaftahParser.BlockStatementContext ctx) {
		return visitContext(
							this,
							"visitBlockStatement",
							getContextByDepth(depth),
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
							getContextByDepth(depth),
							ctx,
							(defaultNaftahParserVisitor, currentContext, declarationContext) -> {
								String variableName = declarationContext.ID().getText();
								// variable -> new : flags if this is a new variable or not
								Pair<DeclaredVariable, Boolean> declaredVariable;
								boolean isConstant = hasChild(declarationContext.CONSTANT());
								boolean isConstantOrVariable = isConstant || hasChild(declarationContext.VARIABLE());
								boolean hasType = hasChild(declarationContext.type());
								boolean creatingObject = currentContext.isCreatingObject();
								boolean creatingObjectField = hasAnyParentOfType(   declarationContext,
																					org.daiitech.naftah.parser.NaftahParser.ObjectContext.class);
								if (isConstantOrVariable || hasType || creatingObjectField) {
									if (creatingObject && hasType) {
										Class<?> type = (Class<?>) defaultNaftahParserVisitor
												.visit(declarationContext.type());
										if (Objects.nonNull(type) && !Object.class.equals(type)) {
											throw new NaftahBugError(
																		("""
																			لا يمكن أن يكون الكائن '%s' من النوع %s. يجب أن يكون الكائن عامًا لجميع الأنواع (%s).""")
																				.formatted( variableName,
																							getNaftahType(  defaultNaftahParserVisitor.parser,
																											type),
																							getNaftahType(  defaultNaftahParserVisitor.parser,
																											Object.class)),
																		declarationContext.getStart().getLine(),
																		declarationContext
																				.getStart()
																				.getCharPositionInLine());
										}
									}
									declaredVariable = createDeclaredVariable(  defaultNaftahParserVisitor,
																				declarationContext,
																				variableName,
																				isConstant,
																				hasType);
									// TODO: check if inside function to check if it matches any argument /
									// parameter or
									// previously
									// declared and update if possible
									if (!creatingObjectField) {
										currentContext.defineVariable(variableName, declaredVariable.a);
									}
								}
								else {
									declaredVariable = Optional
											.ofNullable(currentContext.getVariable(variableName, true))
											.map(alreadyDeclaredVariable -> new Pair<>(alreadyDeclaredVariable.b, true))
											.orElse(createDeclaredVariable( defaultNaftahParserVisitor,
																			declarationContext,
																			variableName,
																			false,
																			false));
								}
								return currentContext.isParsingAssignment() ? declaredVariable : declaredVariable.a;
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
							getContextByDepth(depth),
							ctx,
							(defaultNaftahParserVisitor, currentContext, assignmentContext) -> {
								Object result;
								if (Objects.nonNull(assignmentContext.ID())) {
									DeclaredVariable variable = currentContext
											.getVariable(   assignmentContext.ID().getText(),
															false).b;
									var newValue = defaultNaftahParserVisitor.visit(assignmentContext.expression());
									variable.setValue(newValue);
									result = variable;
								}
								else if (Objects.nonNull(assignmentContext.declaration())) {
									currentContext.setParsingAssignment(true);
									boolean creatingObjectField = hasAnyParentOfType(   assignmentContext,
																						org.daiitech.naftah.parser.NaftahParser.ObjectContext.class);
									Pair<DeclaredVariable, Boolean> declaredVariable = (Pair<DeclaredVariable, Boolean>) defaultNaftahParserVisitor
											.visit(assignmentContext.declaration());
									currentContext.setDeclarationOfAssignment(declaredVariable);
									// TODO: check if inside function to check if it matches any argument /
									// parameter or previously
									if (declaredVariable.b) {
										declaredVariable = new Pair<>(  DeclaredVariable
																				.of(assignmentContext,
																					declaredVariable.a.getName(),
																					declaredVariable.a.isConstant(),
																					declaredVariable.a.getType(),
																					defaultNaftahParserVisitor
																							.visit(assignmentContext
																									.expression())),
																		declaredVariable.b);
									}
									else {
										declaredVariable.a.setOriginalContext(assignmentContext);
										declaredVariable.a
												.setValue(defaultNaftahParserVisitor
														.visit(assignmentContext.expression()));
									}
									// declared and update if possible
									if (!creatingObjectField) {
										currentContext.setVariable(declaredVariable.a.getName(), declaredVariable.a);
									}
									currentContext.setParsingAssignment(false);
									result = declaredVariable;
								}
								else if (Objects.nonNull(assignmentContext.qualifiedName())) {
									var qualifiedName = getQualifiedName(assignmentContext.qualifiedName());
									var newValue = defaultNaftahParserVisitor.visit(assignmentContext.expression());
									result = setObjectUsingQualifiedName(qualifiedName, currentContext, newValue);
								}
								else if (Objects.nonNull(assignmentContext.qualifiedObjectAccess())) {
									var qualifiedName = getQualifiedName(assignmentContext.qualifiedObjectAccess());
									var newValue = defaultNaftahParserVisitor.visit(assignmentContext.expression());
									result = setObjectUsingQualifiedName(qualifiedName, currentContext, newValue);
								}
								else {
									org.daiitech.naftah.parser.NaftahParser.CollectionAccessContext collectionAccessContext = assignmentContext
											.collectionAccess();
									var newValue = defaultNaftahParserVisitor.visit(assignmentContext.expression());
									Object variable = result = getVariable( collectionAccessContext.ID().getText(),
																			currentContext).get();
									Number number = -1;
									int size = collectionAccessContext.NUMBER().size();
									try {
										for (int i = 0; i < size; i++) {
											Object numberStr = collectionAccessContext.NUMBER(i).getText();
											number = NumberUtils.parseDynamicNumber(numberStr);
											if (variable instanceof List && !(variable instanceof Tuple)) {
												List<Object> list = (List<Object>) variable;
												if (i < size - 1) {
													variable = list.get(number.intValue());
												}
												else {
													list.set(number.intValue(), newValue);
												}
											}
											else if (variable instanceof Set) {
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
								return result;
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
							getContextByDepth(depth),
							ctx,
							(defaultNaftahParserVisitor, currentContext, functionDeclarationContext) -> {
								String functionName = functionDeclarationContext.ID().getText();
								DeclaredFunction declaredFunction = DeclaredFunction.of(functionDeclarationContext);
								currentContext.defineFunction(functionName, declaredFunction);
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
							"visitArgumentDeclarationList",
							getContextByDepth(depth),
							ctx,
							(defaultNaftahParserVisitor, currentContext, parameterDeclarationListContext) -> {
								List<DeclaredParameter> args = new ArrayList<>();
								for (org.daiitech.naftah.parser.NaftahParser.ParameterDeclarationContext argumentDeclaration : parameterDeclarationListContext
										.parameterDeclaration()) {
									args.add((DeclaredParameter) defaultNaftahParserVisitor.visit(argumentDeclaration));
								}
								return args;
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
							getContextByDepth(depth),
							ctx,
							(defaultNaftahParserVisitor, currentContext, parameterDeclarationContext) -> {
								String argumentName = parameterDeclarationContext.ID().getText();
								return DeclaredParameter
										.of(parameterDeclarationContext,
											argumentName,
											hasChild(parameterDeclarationContext.CONSTANT()),
											hasChild(parameterDeclarationContext.type()) ?
													visit(parameterDeclarationContext.type()) :
													Object.class,
											hasChild(parameterDeclarationContext.value()) ?
													visit(parameterDeclarationContext.value()) :
													null);
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
							getContextByDepth(depth),
							ctx,
							(defaultNaftahParserVisitor, currentContext, functionCallContext) -> {
								Object result = None.get();
								// TODO: add extra vars to context to get the function called and so on, it can
								// be a free map
								// TODO: and using an Enum as key of predefined ids to get values
								currentContext.setParsingFunctionCallId(true);
								String functionName = hasChild(functionCallContext.ID()) ?
										functionCallContext.ID().getText() :
										(String) defaultNaftahParserVisitor.visit(functionCallContext.qualifiedCall());
								// TODO: add support to variables as qualified call and match to the jvm
								// function
								String functionCallId = generateCallId(depth, functionName);
								currentContext.setFunctionCallId(functionCallId);
								List<Pair<String, Object>> args = new ArrayList<>();
								// TODO: add support to global variables as argument
								if (hasChild(functionCallContext.argumentList())) {
									args = (List<Pair<String, Object>>) defaultNaftahParserVisitor
											.visit(functionCallContext.argumentList());
								}

								if (currentContext.containsFunction(functionName)) {
									Object function = currentContext.getFunction(functionName, false).b;
									if (function instanceof DeclaredFunction declaredFunction) {
										boolean functionInStack = false;
										try {
											prepareDeclaredFunction(defaultNaftahParserVisitor, declaredFunction);
											Map<String, Object> finalArgs = isEmpty(declaredFunction.getParameters()) ?
													Map.of() :
													prepareDeclaredFunctionArguments(   declaredFunction.getParameters(),
																						args);

											if (!isEmpty(declaredFunction.getParameters())) {
												currentContext
														.defineFunctionParameters(  declaredFunction
																							.getParameters()
																							.stream()
																							.map(parameter -> Map
																									.entry( parameter
																													.getName(),
																											parameter))
																							.collect(Collectors
																									.toMap( Map.Entry::getKey,
																											Map.Entry::getValue)),
																					true);
											}

											if (!isEmpty(declaredFunction.getParameters())) {
												currentContext.defineFunctionArguments(finalArgs);
											}

											pushCall(declaredFunction, finalArgs);
											functionInStack = true;
											result = defaultNaftahParserVisitor.visit(declaredFunction.getBody());
										}
										finally {
											if (functionInStack) {
												popCall();
											}
										}
									}
									else if (function instanceof BuiltinFunction builtinFunction) {
										var methodArgs = args
												.stream()
												.map(stringObjectPair -> stringObjectPair.b)
												.toArray(Object[]::new);
										try {
											var possibleResult = builtinFunction.getMethod().invoke(null, methodArgs);
											if (builtinFunction
													.getFunctionInfo()
													.returnType() != Void.class && possibleResult != null) {
												result = possibleResult;
											}
										}
										catch (IllegalArgumentException e) {
											throw new NaftahBugError(   """
																		عدد الوسائط غير صحيح للدالة '%s' المقدمة من '%s'.
																		العدد المتوقع: %d،
																		العدد الفعلي: %d.

																		%s
																		"""
																				.formatted( functionName,
																							builtinFunction
																									.getProviderInfo()
																									.name(),
																							builtinFunction
																									.getMethod()
																									.getParameterCount(),
																							args.size(),
																							builtinFunction
																									.toDetailedString()
																				),
																		e,
																		functionCallContext.getStart().getLine(),
																		functionCallContext
																				.getStart()
																				.getCharPositionInLine());
										}
										catch (IllegalAccessException | InvocationTargetException e) {
											throw new NaftahBugError(   """
																		.'%s' حدث خطأ أثناء استدعاء الدالة

																		%s
																		"""
																				.formatted( functionName,
																							builtinFunction
																									.toDetailedString()),
																		e,
																		functionCallContext.getStart().getLine(),
																		functionCallContext
																				.getStart()
																				.getCharPositionInLine());
										}
									}
									else if (function instanceof JvmFunction jvmFunction) {
										throw new NaftahBugError(   "الدالة '%s' من النوع: '%s' غير مدعومة حالياً"
																			.formatted( functionName,
																						JvmFunction.class.getName()),
																	functionCallContext.getStart().getLine(),
																	functionCallContext
																			.getStart()
																			.getCharPositionInLine());
									}
									else if (function instanceof Collection<?> functions) {
										throw new NaftahBugError(   "الدالة '%s' : '%s' من النوع: '%s' غير مدعومة حالياً"
																			.formatted( functionName,
																						functions,
																						List.class.getName()),
																	functionCallContext.getStart().getLine(),
																	functionCallContext
																			.getStart()
																			.getCharPositionInLine());
									}
								}
								else {
									throw new NaftahBugError(   "الدالة '%s' غير موجودة في السياق الحالي."
																		.formatted(functionName),
																functionCallContext.getStart().getLine(),
																functionCallContext.getStart().getCharPositionInLine());
								}
								currentContext.setFunctionCallId(null);
								// TODO: add support for all kind of functions using the qualifiedName
								return result;
							});
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitQualifiedCall(org.daiitech.naftah.parser.NaftahParser.QualifiedCallContext ctx) {
		return visitContext(
							this,
							"visitQualifiedCall",
							getContextByDepth(depth),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								qualifiedCallContext) -> defaultNaftahParserVisitor
										.visit(qualifiedCallContext.qualifiedName()) + qualifiedCallContext
												.COLON(0)
												.getText() + qualifiedCallContext
														.COLON(1)
														.getText() + qualifiedCallContext.ID()
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
							getContextByDepth(depth),
							ctx,
							(defaultNaftahParserVisitor, currentContext, qualifiedCallContext) -> {
								List<Pair<String, Object>> args = new ArrayList<>();
								for (int i = 0; i < qualifiedCallContext.expression().size(); i++) {
									String name = hasChild(qualifiedCallContext.ID(i)) ?
											qualifiedCallContext.ID(i).getText() :
											null;
									Object value = defaultNaftahParserVisitor.visit(qualifiedCallContext.expression(i));
									args.add(new Pair<>(name, value)); // Evaluate each expression in the argument list
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
							getContextByDepth(depth),
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
							getContextByDepth(depth),
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

											if (checkLoopSignal(result).equals(LoopSignal.BREAK)) {
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

											if (checkLoopSignal(result).equals(LoopSignal.RETURN)) {
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

											if (checkLoopSignal(result).equals(LoopSignal.BREAK)) {
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

											if (checkLoopSignal(result).equals(LoopSignal.RETURN)) {
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

								return loopSignal && (LOOP_STACK.isEmpty() || !propagateLoopSignal) && !None
										.isNone(result) ?
												Optional
														.ofNullable((LoopSignal.LoopSignalDetails) result)
														.map(LoopSignal.LoopSignalDetails::result)
														.orElse(null) :
												result;
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
							getContextByDepth(depth),
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
								Tuple target;
								Tuple targetValues;

								// Loop expression. should be an iterable or Map
								Object collection = defaultNaftahParserVisitor
										.visit(forEachLoopStatementContext.expression());
								Iterator<?> iterator;
								boolean isMap = false;

								if (collection instanceof Iterable<?> iterable) {
									if (foreachTarget instanceof org.daiitech.naftah.parser.NaftahParser.KeyValueForeachTargetContext || foreachTarget instanceof org.daiitech.naftah.parser.NaftahParser.IndexAndKeyValueForeachTargetContext) {
										throw new NaftahBugError(   "key value not supported for collection.",
																	forEachLoopStatementContext.getStart().getLine(),
																	forEachLoopStatementContext
																			.getStart()
																			.getCharPositionInLine());
									}
									target = (Tuple) defaultNaftahParserVisitor.visit(foreachTarget);
									iterator = iterable.iterator();
								}
								else if (collection instanceof Map<?, ?> map) {
									isMap = true;
									target = (Tuple) defaultNaftahParserVisitor.visit(foreachTarget);
									iterator = map.entrySet().iterator();
								}
								else {
									throw new NaftahBugError(   "not an iterable",
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
											targetValues = Tuple.of(index, entry.getKey(), entry.getValue());
										}
										else {
											Object value = iterator.next();
											targetValues = Tuple.of(index, value);
										}

										setForeachVariables(currentContext, foreachTargetClass, target, targetValues);

										defaultNaftahParserVisitor.visit(loopBlock);

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

										if (checkLoopSignal(result).equals(LoopSignal.BREAK)) {
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

										if (checkLoopSignal(result).equals(LoopSignal.RETURN)) {
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

								return loopSignal && (LOOP_STACK.isEmpty() || !propagateLoopSignal) && !None
										.isNone(result) ?
												Optional
														.ofNullable((LoopSignal.LoopSignalDetails) result)
														.map(LoopSignal.LoopSignalDetails::result)
														.orElse(null) :
												result;
							}
		);
	}

	/**
	 * {@inheritDoc}
	 */

	@Override
	public Tuple visitValueForeachTarget(org.daiitech.naftah.parser.NaftahParser.ValueForeachTargetContext ctx) {
		return visitContext(
							this,
							"visitValueForeachTarget",
							getContextByDepth(depth),
							ctx,
							(defaultNaftahParserVisitor, currentContext, valueForeachTargetContext) -> Tuple
									.of(valueForeachTargetContext.ID().getText()),
							Tuple.class
		);
	}

	@Override
	public Tuple visitIndexAndValueForeachTarget(org.daiitech.naftah.parser.NaftahParser.IndexAndValueForeachTargetContext ctx) {
		return visitContext(
							this,
							"visitIndexAndValueForeachTarget",
							getContextByDepth(depth),
							ctx,
							(defaultNaftahParserVisitor, currentContext, indexAndValueForeachTargetContext) -> {
								String index = indexAndValueForeachTargetContext.ID(0).getText();
								String value = indexAndValueForeachTargetContext.ID(1).getText();
								return Tuple.of(index, value);
							},
							Tuple.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitKeyValueForeachTarget(org.daiitech.naftah.parser.NaftahParser.KeyValueForeachTargetContext ctx) {
		return visitContext(
							this,
							"visitKeyValueForeachTarget",
							getContextByDepth(depth),
							ctx,
							(defaultNaftahParserVisitor, currentContext, indexAndValueForeachTargetContext) -> {
								String key = indexAndValueForeachTargetContext.ID(0).getText();
								String value = indexAndValueForeachTargetContext.ID(1).getText();
								return Tuple.of(key, value);
							},
							Tuple.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitIndexAndKeyValueForeachTarget(org.daiitech.naftah.parser.NaftahParser.IndexAndKeyValueForeachTargetContext ctx) {
		return visitContext(
							this,
							"visitIndexAndKeyValueForeachTarget",
							getContextByDepth(depth),
							ctx,
							(defaultNaftahParserVisitor, currentContext, indexAndValueForeachTargetContext) -> {
								String index = indexAndValueForeachTargetContext.ID(0).getText();
								String key = indexAndValueForeachTargetContext.ID(1).getText();
								String value = indexAndValueForeachTargetContext.ID(2).getText();
								return Tuple.of(index, key, value);
							},
							Tuple.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitWhileStatement(org.daiitech.naftah.parser.NaftahParser.WhileStatementContext ctx) {
		return visitContext(
							this,
							"visitWhileStatement",
							getContextByDepth(depth),
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

									while (isTruthy(visit(whileStatementContext.expression()))) {
										visit(whileStatementContext.block());

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

										if (checkLoopSignal(result).equals(LoopSignal.BREAK)) {
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

										if (checkLoopSignal(result).equals(LoopSignal.RETURN)) {
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

								return loopSignal && (LOOP_STACK.isEmpty() || !propagateLoopSignal) && !None
										.isNone(result) ?
												Optional
														.ofNullable((LoopSignal.LoopSignalDetails) result)
														.map(LoopSignal.LoopSignalDetails::result)
														.orElse(null) :
												result;

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
							getContextByDepth(depth),
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
										visit(repeatStatementContext.block());

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

										if (checkLoopSignal(result).equals(LoopSignal.BREAK)) {
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

										if (checkLoopSignal(result).equals(LoopSignal.RETURN)) {
											loopSignal = true;
											break;
										}

										// force current loop label
										currentContext.setLoopLabel(label);
									}
									while (!isTruthy(visit(repeatStatementContext.expression())));

								}
								finally {
									currentContext.setLoopLabel(null);
									if (loopInStack) {
										popLoop();
									}
								}

								return loopSignal && (LOOP_STACK.isEmpty() || !propagateLoopSignal) && !None
										.isNone(result) ?
												Optional
														.ofNullable((LoopSignal.LoopSignalDetails) result)
														.map(LoopSignal.LoopSignalDetails::result)
														.orElse(null) :
												result;

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
							getContextByDepth(depth),
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
							getContextByDepth(depth),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								tryStatementContext) -> {
								Result<Object, NaftahBugError> result = null;
								String okVariableName = null;
								String errorVariableName = null;
								DeclaredVariable previousOkVariable = null;
								DeclaredVariable previousErrorVariable = null;

								var tryCases = tryStatementContext.tryCases();
								try {
									Object expressionResult = null;
									try {
										expressionResult = defaultNaftahParserVisitor
												.visit(tryStatementContext.expression());
									}
									catch (Throwable th) {
										var errorCase = tryCases.errorCase();

										if (Objects.nonNull(errorCase)) {
											errorVariableName = errorCase.ID().getText();

											result = Result.Error
													.of(th instanceof NaftahBugError naftahBugError ?
															naftahBugError :
															new NaftahBugError(th));

											var declaredVariable = DeclaredVariable
													.of(tryStatementContext,
														errorVariableName,
														true,
														Result.Error.class,
														result);

											boolean errorVarExists = currentContext.containsVariable(errorVariableName);
											if (errorVarExists) {
												previousErrorVariable = currentContext
														.setVariable(   errorVariableName,
																		declaredVariable);
											}
											else {
												currentContext.defineVariable(errorVariableName, declaredVariable);
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
												.of(tryStatementContext,
													okVariableName,
													true,
													Result.Ok.class,
													result);

										boolean okVarExists = currentContext.containsVariable(okVariableName);
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

	@Override
	public Object visitTryStatementWithOptionCases(org.daiitech.naftah.parser.NaftahParser.TryStatementWithOptionCasesContext ctx) {
		return visitContext(
							this,
							"visitTryStatementWithOptionCases",
							getContextByDepth(depth),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								tryStatementContext) -> {
								var result = defaultNaftahParserVisitor
										.visit(tryStatementContext.expression());

								String someVariableName = null;
								DeclaredVariable previousSomeVariable = null;

								var tryCases = tryStatementContext.optionCases();

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
												.of(tryStatementContext,
													someVariableName,
													true,
													result.getClass(),
													result instanceof Optional<?> optional ?
															optional.get() :
															result);

										boolean okVarExists = currentContext.containsVariable(someVariableName);
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
							getContextByDepth(depth),
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
							getContextByDepth(depth),
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
							getContextByDepth(depth),
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
							getContextByDepth(depth),
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
							getContextByDepth(depth),
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
							getContextByDepth(depth),
							ctx,
							(defaultNaftahParserVisitor, currentContext, breakStatementContext) -> {
								if (LOOP_STACK.isEmpty() || !checkInsideLoop(breakStatementContext)) {
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
							getContextByDepth(depth),
							ctx,
							(defaultNaftahParserVisitor, currentContext, continueStatementContext) -> {
								if (LOOP_STACK.isEmpty() || !checkInsideLoop(continueStatementContext)) {
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
							getContextByDepth(depth),
							ctx,
							(defaultNaftahParserVisitor, currentContext, returnStatementContext) -> {
								boolean insideLoop = !LOOP_STACK.isEmpty() || checkInsideLoop(returnStatementContext);
								Object result = None.get();
								if (hasChild(returnStatementContext.expression())) {
									// Evaluate and return the result
									result = defaultNaftahParserVisitor.visit(returnStatementContext.expression());
								}
								return insideLoop ? LoopSignal.LoopSignalDetails.of(RETURN, result) : result;
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
							getBlockContext(ctx, getContextByDepth(depth)),
							ctx,
							(defaultNaftahParserVisitor, nextContext, blockContext) -> {
								defaultNaftahParserVisitor.depth = nextContext.getDepth();
								Object result = None.get();
								for (org.daiitech.naftah.parser.NaftahParser.StatementContext statement : blockContext
										.statement()) {
									// Visit each statement in the block
									result = defaultNaftahParserVisitor.visit(statement);
									// break program after executing a return statement
									if (shouldBreakStatementsLoop(nextContext, statement, result)) {
										break;
									}
								}
								deregisterContextByDepth(defaultNaftahParserVisitor.depth--);
								return result;
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
							getContextByDepth(depth),
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
	public Object visitObject(org.daiitech.naftah.parser.NaftahParser.ObjectContext ctx) {
		return visitContext(
							this,
							"visitObject",
							getContextByDepth(depth),
							ctx,
							(defaultNaftahParserVisitor, currentContext, objectContext) -> defaultNaftahParserVisitor
									.visit(
											objectContext.objectFields())
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitObjectFields(org.daiitech.naftah.parser.NaftahParser.ObjectFieldsContext ctx) {
		return visitContext(
							this,
							"visitObjectFields",
							getContextByDepth(depth),
							ctx,
							(defaultNaftahParserVisitor, currentContext, objectFieldsContext) -> {
								var result = new LinkedHashMap<String, DeclaredVariable>();

								for (int i = 0; i < objectFieldsContext.assignment().size(); i++) {
									var field = (Pair<DeclaredVariable, Boolean>) defaultNaftahParserVisitor
											.visit(
													objectFieldsContext.assignment(i));
									var fieldName = field.a.getName();
									result.put(fieldName, field.a);
								}

								currentContext.setCreatingObject(false);

								return result;
							}
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
							getContextByDepth(depth),
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
							getContextByDepth(depth),
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
							getContextByDepth(depth),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								qualifiedObjectAccessContext) -> {
								var qualifiedName = getQualifiedName(qualifiedObjectAccessContext);
								return accessObjectUsingQualifiedName(qualifiedName, currentContext);
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
							getContextByDepth(depth),
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
	public Object visitCollectionAccessStatement(org.daiitech.naftah.parser.NaftahParser.CollectionAccessStatementContext ctx) {
		return visitContext(
							this,
							"visitCollectionAccessStatement",
							getContextByDepth(depth),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								collectionAccessStatementContext) -> defaultNaftahParserVisitor
										.visit(
												collectionAccessStatementContext.collectionAccess())
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
							getContextByDepth(depth),
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
							getContextByDepth(depth),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								collectionAccessContext) -> {
								Object result = getVariable(collectionAccessContext.ID().getText(), currentContext)
										.get();
								Number number = -1;
								try {
									for (int i = 0; i < collectionAccessContext.NUMBER().size(); i++) {
										Object value = collectionAccessContext.NUMBER(i).getText();
										number = NumberUtils.parseDynamicNumber(value);
										if (result instanceof Tuple tuple) {
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
	public List<?> visitListValue(org.daiitech.naftah.parser.NaftahParser.ListValueContext ctx) {
		return visitContext(
							this,
							"visitListValue",
							getContextByDepth(depth),
							ctx,
							(defaultNaftahParserVisitor, currentContext, listValueContext) -> defaultNaftahParserVisitor
									.visit(
											listValueContext.elements()),
							List.class
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Tuple visitTupleValue(org.daiitech.naftah.parser.NaftahParser.TupleValueContext ctx) {
		return visitContext(
							this,
							"visitTupleValue",
							getContextByDepth(depth),
							ctx,
							(defaultNaftahParserVisitor, currentContext, tupleValueContext) -> {
								if (currentContext.isParsingAssignment()) {
									var currentDeclaration = currentContext.getDeclarationOfAssignment();
									Class<?> currentDeclarationType = currentDeclaration.a.getType();
									String currentDeclarationName = currentDeclaration.a.getName();
									if (Objects.nonNull(currentDeclarationType) && !Object.class
											.equals(currentDeclarationType)) {
										throw new NaftahBugError(
																	("""
																		لا يُسمح بأن تحتوي التركيبة (tuple) '%s' على عناصر من النوع %s. التركيبة يجب أن تكون عامة لجميع الأنواع (%s).""")
																			.formatted( currentDeclarationName,
																						getNaftahType(  defaultNaftahParserVisitor.parser,
																										currentDeclarationType),
																						getNaftahType(  defaultNaftahParserVisitor.parser,
																										Object.class)),
																	tupleValueContext.getStart().getLine(),
																	tupleValueContext
																			.getStart()
																			.getCharPositionInLine());
									}
								}
								return Tuple
										.of((List<Object>) defaultNaftahParserVisitor
												.visit(tupleValueContext.elements()));
							},
							Tuple.class);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<?> visitSetValue(org.daiitech.naftah.parser.NaftahParser.SetValueContext ctx) {
		return visitContext(
							this,
							"visitSetValue",
							getContextByDepth(depth),
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
	public Map<?, ?> visitMapValue(org.daiitech.naftah.parser.NaftahParser.MapValueContext ctx) {
		return visitContext(
							this,
							"visitMapValue",
							getContextByDepth(depth),
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
							getContextByDepth(depth),
							ctx,
							(defaultNaftahParserVisitor, currentContext, elementContext) -> {
								// prepare validations
								boolean creatingList = hasParentOfType( elementContext,
																		org.daiitech.naftah.parser.NaftahParser.ListValueContext.class);
								boolean creatingTuple = hasParentOfType(elementContext,
																		org.daiitech.naftah.parser.NaftahParser.TupleValueContext.class);
								boolean parsingAssignment = currentContext.isParsingAssignment();
								Class<?> currentDeclarationType = null;
								String currentDeclarationName = null;
								if (parsingAssignment) {
									var currentDeclaration = currentContext.getDeclarationOfAssignment();
									currentDeclarationType = currentDeclaration.a.getType();
									currentDeclarationName = currentDeclaration.a.getName();
								}
								// process elements
								var elementValue = defaultNaftahParserVisitor.visit(elementContext.expression());
								var elementType = Objects.nonNull(elementValue) ?
										elementValue.getClass() :
										Object.class;

								if (!creatingTuple) {
									// validating list has all the same type
									if (parsingAssignment && typeMismatch(  elementValue,
																			elementType,
																			currentDeclarationType)) {
										throw new NaftahBugError(
																	("""
																		لا يمكن أن تحتوي %s %s على عناصر من أنواع مختلفة. يجب أن تكون جميع العناصر من نفس النوع %s.""")
																			.formatted( creatingList ?
																								"القائمة (List)" :
																								"المجموعة (Set)",
																						"'%s'"
																								.formatted(currentDeclarationName),
																						"(%s)"
																								.formatted(getNaftahType(   defaultNaftahParserVisitor.parser,
																															currentDeclarationType))),
																	elementContext.getStart().getLine(),
																	elementContext
																			.getStart()
																			.getCharPositionInLine());
									}
								}

								return List.of(elementValue);
							},
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
							getContextByDepth(depth),
							ctx,
							(defaultNaftahParserVisitor, currentContext, elementsContext) -> {
								// prepare validations
								boolean creatingList = hasParentOfType( elementsContext,
																		org.daiitech.naftah.parser.NaftahParser.ListValueContext.class);
								boolean creatingSet = hasParentOfType(  elementsContext,
																		org.daiitech.naftah.parser.NaftahParser.SetValueContext.class);
								boolean creatingTuple = hasParentOfType(elementsContext,
																		org.daiitech.naftah.parser.NaftahParser.TupleValueContext.class);
								boolean parsingAssignment = currentContext.isParsingAssignment();
								Class<?> currentDeclarationType = null;
								String currentDeclarationName = null;
								if (parsingAssignment) {
									var currentDeclaration = currentContext.getDeclarationOfAssignment();
									currentDeclarationType = currentDeclaration.a.getType();
									currentDeclarationName = currentDeclaration.a.getName();
								}
								// process elements
								List<Object> elements = new ArrayList<>();
								Set<Class<?>> elementTypes = new HashSet<>();
								for (int i = 0; i < elementsContext.expression().size(); i++) {
									var elementValue = defaultNaftahParserVisitor.visit(elementsContext.expression(i));
									var elementType = Objects.nonNull(elementValue) ?
											elementValue.getClass() :
											Object.class;
									if (!creatingTuple) {
										// validating list has all the same type
										if (parsingAssignment && typeMismatch(  elementValue,
																				elementType,
																				currentDeclarationType) || elementTypes
																						.stream()
																						.anyMatch(aClass -> typeMismatch(   aClass,
																															elementType))) {
											throw new NaftahBugError(
																		("""
																			لا يمكن أن تحتوي %s %s على عناصر من أنواع مختلفة. يجب أن تكون جميع العناصر من نفس النوع %s.""")
																				.formatted( creatingList ?
																									"القائمة (List)" :
																									"المجموعة (Set)",
																							parsingAssignment ?
																									"'%s'"
																											.formatted(currentDeclarationName) :
																									"",
																							parsingAssignment ?
																									"(%s)"
																											.formatted(getNaftahType(   defaultNaftahParserVisitor.parser,
																																		currentDeclarationType)) :
																									""),
																		elementsContext.getStart().getLine(),
																		elementsContext
																				.getStart()
																				.getCharPositionInLine());
										}

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
																			elementsContext.getStart().getLine(),
																			elementsContext
																					.getStart()
																					.getCharPositionInLine());
											}
										}
									}
									elements.add(elementValue);
									if (Objects.nonNull(elementValue) && !Collection.class
											.isAssignableFrom(elementType) && !Map.class
													.isAssignableFrom(elementType)) {
										elementTypes.add(elementType);
									}
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
	public Map<?, ?> visitKeyValuePairs(org.daiitech.naftah.parser.NaftahParser.KeyValuePairsContext ctx) {
		return visitContext(
							this,
							"visitKeyValuePairs",
							getContextByDepth(depth),
							ctx,
							(defaultNaftahParserVisitor, currentContext, keyValuePairsContext) -> {
								// prepare validations
								boolean creatingMap = hasParentOfType(  keyValuePairsContext,
																		org.daiitech.naftah.parser.NaftahParser.MapValueContext.class);
								boolean parsingAssignment = currentContext.isParsingAssignment();
								Class<?> currentDeclarationType = null;
								String currentDeclarationName = null;
								if (parsingAssignment) {
									var currentDeclaration = currentContext.getDeclarationOfAssignment();
									currentDeclarationType = currentDeclaration.a.getType();
									currentDeclarationName = currentDeclaration.a.getName();
								}
								// process entries
								org.daiitech.naftah.parser.NaftahParser.MapValueContext mapValueContext = (org.daiitech.naftah.parser.NaftahParser.MapValueContext) keyValuePairsContext
										.getParent();
								Map<Object, Object> map = Objects.nonNull(mapValueContext.ORDERED()) ?
										new LinkedHashMap<>() :
										new HashMap<>();
								Set<Class<?>> keyTypes = new HashSet<>();
								Set<Class<?>> valueTypes = new HashSet<>();
								for (int i = 0; i < keyValuePairsContext.keyValue().size(); i++) {
									var entry = (Map.Entry<?, ?>) defaultNaftahParserVisitor
											.visit(keyValuePairsContext.keyValue(i));
									var key = entry.getKey();
									var keyType = Objects.nonNull(key) ? key.getClass() : Object.class;
									var value = entry.getValue();
									var valueType = Objects.nonNull(value) ? value.getClass() : Object.class;
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

										if (parsingAssignment && typeMismatch(  value,
																				valueType,
																				currentDeclarationType) || keyTypes
																						.stream()
																						.anyMatch(aClass -> typeMismatch(   aClass,
																															keyType))) {
											throw new NaftahBugError(
																		("""
																			لا يمكن أن تحتوي المصفوفة الترابطية (Map) %s على عناصر من أنواع مختلفة. يجب أن تكون جميع العناصر من نفس النوع %s.""")
																				.formatted( parsingAssignment ?
																									"'%s'"
																											.formatted(currentDeclarationName) :
																									"",
																							parsingAssignment ?
																									"(%s)"
																											.formatted(getNaftahType(   defaultNaftahParserVisitor.parser,
																																		currentDeclarationType)) :
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

									if (Objects.nonNull(key)) {
										keyTypes.add(keyType);
									}

									if (Objects.nonNull(value)) {
										valueTypes.add(valueType);
									}
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
							getContextByDepth(depth),
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
							getContextByDepth(depth),
							ctx,
							(defaultNaftahParserVisitor, currentContext, valueExpressionContext) -> {
								// prepare validations
								boolean creatingCollection = hasAnyParentOfType(valueExpressionContext,
																				org.daiitech.naftah.parser.NaftahParser.CollectionContext.class);
								boolean parsingAssignment = currentContext.isParsingAssignment();

								// process value
								var result = defaultNaftahParserVisitor.visit(valueExpressionContext.value());

								if (!creatingCollection && parsingAssignment) {
									var currentDeclaration = currentContext.getDeclarationOfAssignment();
									Class<?> currentDeclarationType = currentDeclaration.a.getType();
									Class<?> resultType = Objects.nonNull(result) ? result.getClass() : Object.class;
									String currentDeclarationName = currentDeclaration.a.getName();
									if (typeMismatch(result, resultType, currentDeclarationType)) {
										throw new NaftahBugError(   "القيمة '%s' لا تتوافق مع النوع المتوقع (%s)."
																			.formatted( currentDeclarationName,
																						getNaftahType(  defaultNaftahParserVisitor.parser,
																										currentDeclarationType)),
																	valueExpressionContext.getStart().getLine(),
																	valueExpressionContext
																			.getStart()
																			.getCharPositionInLine());
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
	public Object visitParenthesisExpression(org.daiitech.naftah.parser.NaftahParser.ParenthesisExpressionContext ctx) {
		return visitContext(
							this,
							"visitParenthesisExpression",
							getContextByDepth(depth),
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
	public Object visitFunctionCallExpression(org.daiitech.naftah.parser.NaftahParser.FunctionCallExpressionContext ctx) {
		return visitContext(
							this,
							"visitFunctionCallExpression",
							getContextByDepth(depth),
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
							getContextByDepth(depth),
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
							getContextByDepth(depth),
							ctx,
							(defaultNaftahParserVisitor, currentContext, radixNumberValueContext) -> {
								String originalValue = radixNumberValueContext.BASE_DIGITS().getText();
								String arabicValue = originalValue
										.substring( 0,
													originalValue.length() - 2);
								String value = ArabicUtils
										.convertArabicToLatinLetterByLetter(arabicValue);
								String originalRadix = radixNumberValueContext.BASE_RADIX().getText();
								int radix = Integer
										.parseInt(originalRadix
												.substring( 0,
															originalRadix.length() - 1));

								return NumberUtils.parseDynamicNumber(value, radix, arabicValue);
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
							getContextByDepth(depth),
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
							getContextByDepth(depth),
							ctx,
							(defaultNaftahParserVisitor, currentContext, stringValueContext) -> {
								String value = stringValueContext.STRING().getText();
								Object result;
								if (Objects.isNull(stringValueContext.RAW()) && Objects
										.isNull(stringValueContext.BYTE_ARRAY())) {
									result = StringInterpolator.process(value, currentContext);
								}
								else {
									value = StringInterpolator.cleanInput(value);
									if (Objects.nonNull(stringValueContext.BYTE_ARRAY())) {
										result = value.getBytes(StandardCharsets.UTF_8);
									}
									else {
										result = value;
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
							getContextByDepth(depth),
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
							getContextByDepth(depth),
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
							getContextByDepth(depth),
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
							getContextByDepth(depth),
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
	public Class<?> visitVoidReturnType(org.daiitech.naftah.parser.NaftahParser.VoidReturnTypeContext ctx) {
		return visitContext(
							this,
							"visitVoidReturnType",
							getContextByDepth(depth),
							ctx,
							(defaultNaftahParserVisitor, currentContext, voidReturnTypeContext) -> getJavaType(
																												voidReturnTypeContext),
							Class.class
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> visitTypeReturnType(org.daiitech.naftah.parser.NaftahParser.TypeReturnTypeContext ctx) {
		return visitContext(
							this,
							"visitTypeReturnType",
							getContextByDepth(depth),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								typeReturnTypeContext) -> defaultNaftahParserVisitor
										.visit(typeReturnTypeContext.type()),
							Class.class
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> visitVarType(org.daiitech.naftah.parser.NaftahParser.VarTypeContext ctx) {
		return visitContext(
							this,
							"visitVarType",
							getContextByDepth(depth),
							ctx,
							(defaultNaftahParserVisitor, currentContext, varTypeContext) -> getJavaType(varTypeContext),
							Class.class
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> visitBuiltInType(org.daiitech.naftah.parser.NaftahParser.BuiltInTypeContext ctx) {
		return visitContext(
							this,
							"visitBuiltInType",
							getContextByDepth(depth),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								builtInTypeContext) -> defaultNaftahParserVisitor
										.visit(
												builtInTypeContext.builtIn()),
							Class.class
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> visitBuiltIn(org.daiitech.naftah.parser.NaftahParser.BuiltInContext ctx) {
		return visitContext(
							this,
							"visitBuiltIn",
							getContextByDepth(depth),
							ctx,
							(defaultNaftahParserVisitor, currentContext, builtInContext) -> getJavaType(builtInContext),
							Class.class
		);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitQualifiedNameType(org.daiitech.naftah.parser.NaftahParser.QualifiedNameTypeContext ctx) {
		return visitContext(
							this,
							"visitQualifiedNameType",
							getContextByDepth(depth),
							ctx,
							(defaultNaftahParserVisitor, currentContext, qualifiedNameTypeContext) -> {
								// TODO: think about using id to variable or necessary other elements
								return defaultNaftahParserVisitor.visit(qualifiedNameTypeContext.qualifiedName());
							}
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
							getContextByDepth(depth),
							ctx,
							(defaultNaftahParserVisitor, currentContext, qualifiedNameContext) -> {
								Object result;
								boolean accessingObjectField = hasAnyParentOfType(  qualifiedNameContext,
																					org.daiitech.naftah.parser.NaftahParser.ObjectAccessStatementContext.class) || hasAnyParentOfType(
																																														qualifiedNameContext,
																																														org.daiitech.naftah.parser.NaftahParser.ObjectAccessExpressionContext.class);
								if (accessingObjectField) {
									var qualifiedName = getQualifiedName(qualifiedNameContext);
									result = accessObjectUsingQualifiedName(qualifiedName, currentContext);
								}
								else if (currentContext.isParsingFunctionCallId()) {
									result = getQualifiedName(qualifiedNameContext);
									currentContext.setParsingFunctionCallId(false);
								}
								else {
									result = getJavaType(qualifiedNameContext);
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
							getContextByDepth(depth),
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
							getContextByDepth(depth),
							ctx,
							(   defaultNaftahParserVisitor,
								currentContext,
								expressionContext) -> defaultNaftahParserVisitor
										.visit(expressionContext
												.ternaryExpression()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitTernaryExpression(org.daiitech.naftah.parser.NaftahParser.TernaryExpressionContext ctx) {
		return visitContext(
							this,
							"visitTernaryExpression",
							getContextByDepth(depth),
							ctx,
							(defaultNaftahParserVisitor, currentContext, ternaryExpressionContext) -> {
								if (Objects.nonNull(ternaryExpressionContext.QUESTION())) {
									// ternary expression: condition ? thenExpression : elseExpression
									Object condition = visit(ternaryExpressionContext.nullishExpression());
									return isTruthy(condition) ?
											visit(ternaryExpressionContext.expression()) :
											visit(ternaryExpressionContext.ternaryExpression());
								}
								else {
									return visit(ternaryExpressionContext.nullishExpression());
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
							getContextByDepth(depth),
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
							getContextByDepth(depth),
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
							getContextByDepth(depth),
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
							getContextByDepth(depth),
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
							getContextByDepth(depth),
							ctx,
							(defaultNaftahParserVisitor, currentContext, relationalExpressionContext) -> {
								Object left = defaultNaftahParserVisitor
										.visit(relationalExpressionContext.additiveExpression(0));

								for (int i = 1; i < relationalExpressionContext.additiveExpression().size(); i++) {
									Object right = defaultNaftahParserVisitor
											.visit(relationalExpressionContext
													.additiveExpression(
																		i));

									String op = NaftahParserHelper
											.getDisplayName(relationalExpressionContext.getChild(2 * i - 1),
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
							getContextByDepth(depth),
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
							getContextByDepth(depth),
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
							getContextByDepth(depth),
							ctx,
							(defaultNaftahParserVisitor, currentContext, powerExpressionContext) -> {
								if (Objects.nonNull(powerExpressionContext.POW())) {
									Object left = visit(powerExpressionContext.unaryExpression());
									Object right = visit(powerExpressionContext.powerExpression());

									String op = NaftahParserHelper
											.getDisplayName(powerExpressionContext.POW(),
															defaultNaftahParserVisitor.parser.getVocabulary());
									return applyOperation(left, right, BinaryOperation.of(op));
								}
								else {
									return visit(powerExpressionContext.unaryExpression());
								}
							});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitPrefixUnaryExpression(org.daiitech.naftah.parser.NaftahParser.PrefixUnaryExpressionContext ctx) {
		return visitContext(
							this,
							"visitPrefixUnaryExpression",
							getContextByDepth(depth),
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
								result = applyOperation(value, UnaryOperation.of(op));

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
							getContextByDepth(depth),
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
							getContextByDepth(depth),
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
							getContextByDepth(depth),
							ctx,
							(defaultNaftahParserVisitor, currentContext, nanValueContext) -> NaN.get()
		);
	}
}
