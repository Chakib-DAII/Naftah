package org.daiitech.naftah.parser;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.tree.ParseTree;
import org.daiitech.naftah.builtin.lang.BuiltinFunction;
import org.daiitech.naftah.builtin.lang.DeclaredFunction;
import org.daiitech.naftah.builtin.lang.DeclaredParameter;
import org.daiitech.naftah.builtin.lang.DeclaredVariable;
import org.daiitech.naftah.builtin.lang.JvmFunction;
import org.daiitech.naftah.builtin.utils.NumberUtils;
import org.daiitech.naftah.builtin.utils.Tuple;
import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.utils.arabic.ArabicUtils;

import static org.daiitech.naftah.Naftah.INSIDE_REPL_PROPERTY;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.applyOperation;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.getJavaType;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.getNaftahType;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.isEmpty;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.isTruthy;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.not;
import static org.daiitech.naftah.builtin.utils.Tuple.newNaftahBugNullError;
import static org.daiitech.naftah.builtin.utils.op.BinaryOperation.ADD;
import static org.daiitech.naftah.builtin.utils.op.BinaryOperation.BITWISE_AND;
import static org.daiitech.naftah.builtin.utils.op.BinaryOperation.BITWISE_OR;
import static org.daiitech.naftah.builtin.utils.op.BinaryOperation.BITWISE_XOR;
import static org.daiitech.naftah.builtin.utils.op.BinaryOperation.DIVIDE;
import static org.daiitech.naftah.builtin.utils.op.BinaryOperation.ELEMENTWISE_ADD;
import static org.daiitech.naftah.builtin.utils.op.BinaryOperation.ELEMENTWISE_DIVIDE;
import static org.daiitech.naftah.builtin.utils.op.BinaryOperation.ELEMENTWISE_MODULO;
import static org.daiitech.naftah.builtin.utils.op.BinaryOperation.ELEMENTWISE_MULTIPLY;
import static org.daiitech.naftah.builtin.utils.op.BinaryOperation.ELEMENTWISE_SUBTRACT;
import static org.daiitech.naftah.builtin.utils.op.BinaryOperation.EQUALS;
import static org.daiitech.naftah.builtin.utils.op.BinaryOperation.GREATER_THAN;
import static org.daiitech.naftah.builtin.utils.op.BinaryOperation.GREATER_THAN_EQUALS;
import static org.daiitech.naftah.builtin.utils.op.BinaryOperation.LESS_THAN;
import static org.daiitech.naftah.builtin.utils.op.BinaryOperation.LESS_THAN_EQUALS;
import static org.daiitech.naftah.builtin.utils.op.BinaryOperation.MODULO;
import static org.daiitech.naftah.builtin.utils.op.BinaryOperation.MULTIPLY;
import static org.daiitech.naftah.builtin.utils.op.BinaryOperation.NOT_EQUALS;
import static org.daiitech.naftah.builtin.utils.op.BinaryOperation.SUBTRACT;
import static org.daiitech.naftah.builtin.utils.op.UnaryOperation.BITWISE_NOT;
import static org.daiitech.naftah.builtin.utils.op.UnaryOperation.POST_DECREMENT;
import static org.daiitech.naftah.builtin.utils.op.UnaryOperation.POST_INCREMENT;
import static org.daiitech.naftah.builtin.utils.op.UnaryOperation.PRE_DECREMENT;
import static org.daiitech.naftah.builtin.utils.op.UnaryOperation.PRE_INCREMENT;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidLoopLabelError;
import static org.daiitech.naftah.parser.DefaultContext.FUNCTION_CALL_ID_GENERATOR;
import static org.daiitech.naftah.parser.DefaultContext.LOOP_STACK;
import static org.daiitech.naftah.parser.DefaultContext.VARIABLE_GETTER;
import static org.daiitech.naftah.parser.DefaultContext.currentLoopLabel;
import static org.daiitech.naftah.parser.DefaultContext.deregisterContext;
import static org.daiitech.naftah.parser.DefaultContext.getContextByDepth;
import static org.daiitech.naftah.parser.DefaultContext.loopContainsLabel;
import static org.daiitech.naftah.parser.DefaultContext.popCall;
import static org.daiitech.naftah.parser.DefaultContext.popLoop;
import static org.daiitech.naftah.parser.DefaultContext.pushCall;
import static org.daiitech.naftah.parser.DefaultContext.pushLoop;
import static org.daiitech.naftah.parser.DefaultContext.registerContext;
import static org.daiitech.naftah.parser.LoopSignal.BREAK;
import static org.daiitech.naftah.parser.LoopSignal.CONTINUE;
import static org.daiitech.naftah.parser.LoopSignal.RETURN;
import static org.daiitech.naftah.parser.NaftahExecutionLogger.logExecution;
import static org.daiitech.naftah.parser.NaftahParserHelper.checkInsideLoop;
import static org.daiitech.naftah.parser.NaftahParserHelper.checkLoopSignal;
import static org.daiitech.naftah.parser.NaftahParserHelper.createDeclaredVariable;
import static org.daiitech.naftah.parser.NaftahParserHelper.debugCurrentContextVisit;
import static org.daiitech.naftah.parser.NaftahParserHelper.getFormattedTokenSymbols;
import static org.daiitech.naftah.parser.NaftahParserHelper.getQualifiedName;
import static org.daiitech.naftah.parser.NaftahParserHelper.hasAnyParentOfType;
import static org.daiitech.naftah.parser.NaftahParserHelper.hasChild;
import static org.daiitech.naftah.parser.NaftahParserHelper.hasChildOrSubChildOfType;
import static org.daiitech.naftah.parser.NaftahParserHelper.hasParentOfType;
import static org.daiitech.naftah.parser.NaftahParserHelper.prepareDeclaredFunction;
import static org.daiitech.naftah.parser.NaftahParserHelper.prepareDeclaredFunctionArguments;
import static org.daiitech.naftah.parser.NaftahParserHelper.shouldBreakStatementsLoop;
import static org.daiitech.naftah.parser.NaftahParserHelper.typeMismatch;

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
	private static final Logger LOGGER = Logger.getLogger("DefaultNaftahParserVisitor");

	/**
	 * Supplier function to provide the root context when
	 * visiting a Program parse tree node. It decides whether
	 * to create a REPL or standard context based on a system property
	 * and the presence of function call statements.
	 */
	private static final Function<org.daiitech.naftah.parser.NaftahParser.ProgramContext, DefaultContext> ROOT_CONTEXT_SUPPLIER = (
																																	ctx) -> {
		if (Boolean.getBoolean(INSIDE_REPL_PROPERTY)) {
			return hasChildOrSubChildOfType(ctx,
											org.daiitech.naftah.parser.NaftahParser.FunctionCallStatementContext.class) ?
													REPLContext.registerContext(new HashMap<>(), new HashMap<>()) :
													REPLContext.registerContext();
		}
		else {
			return hasChildOrSubChildOfType(ctx,
											org.daiitech.naftah.parser.NaftahParser.FunctionCallStatementContext.class) ?
													registerContext(new HashMap<>(), new HashMap<>()) :
													registerContext();
		}
	};

	/**
	 * BiFunction to provide or update the context for a block node.
	 * Decides context registration depending on REPL mode and presence
	 * of function call statements or expressions.
	 */
	private static final BiFunction<org.daiitech.naftah.parser.NaftahParser.BlockContext, DefaultContext, DefaultContext> BLOCK_CONTEXT_SUPPLIER = (
																																					ctx,
																																					currentContext) -> {
		if (Boolean.getBoolean(INSIDE_REPL_PROPERTY)) {
			return hasChildOrSubChildOfType(ctx,
											org.daiitech.naftah.parser.NaftahParser.FunctionCallStatementContext.class) ?
													registerContext(currentContext, new HashMap<>(), new HashMap<>()) :
													REPLContext.registerContext(currentContext);
		}
		else {
			return hasChildOrSubChildOfType(ctx,
											org.daiitech.naftah.parser.NaftahParser.FunctionCallStatementContext.class) || hasChildOrSubChildOfType(
																																					ctx,
																																					org.daiitech.naftah.parser.NaftahParser.FunctionCallExpressionContext.class) ?
																																							registerContext(currentContext,
																																											new HashMap<>(),
																																											new HashMap<>()) :
																																							registerContext(currentContext);
		}
	};

	/**
	 * Function to get the context by its depth in the parse tree.
	 * Behavior is the same regardless of REPL mode currently.
	 */
	private static final Function<Integer, DefaultContext> CONTEXT_BY_DEPTH_SUPPLIER = (depth) -> {
		if (Boolean.getBoolean(INSIDE_REPL_PROPERTY)) {
			return getContextByDepth(depth);
		}
		else {
			return getContextByDepth(depth);
		}
	};

	/**
	 * Function to deregister (remove) a context by depth.
	 * Delegates to REPL or normal context deregistration.
	 */
	private static final Function<Integer, DefaultContext> DEREGISTER_CONTEXT_BY_DEPTH_SUPPLIER = (depth) -> {
		if (Boolean.getBoolean(INSIDE_REPL_PROPERTY)) {
			return REPLContext.deregisterContext(depth);
		}
		else {
			return deregisterContext(depth);
		}
	};


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
		debugCurrentContextVisit(LOGGER, "visitProgram", ctx);
		logExecution(ctx);
		// TODO: add the functions (processed from classpath and provider annotations)
		var rootContext = ROOT_CONTEXT_SUPPLIER.apply(ctx);
		depth = rootContext.getDepth();
		Object result = null;
		for (org.daiitech.naftah.parser.NaftahParser.StatementContext statement : ctx.statement()) {
			result = visit(statement); // Visit each statement in the program
			// break program after executing a return statement
			if (shouldBreakStatementsLoop(rootContext, statement, result)) {
				break;
			}
		}
		DEREGISTER_CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		rootContext.markExecuted(ctx); // Mark as executed
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitObjectAccessStatement(org.daiitech.naftah.parser.NaftahParser.ObjectAccessStatementContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitObjectAccessStatement", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		var result = visit(ctx.qualifiedName());
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitDeclarationStatement(org.daiitech.naftah.parser.NaftahParser.DeclarationStatementContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitDeclarationStatement", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		var result = visit(ctx.declaration());
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitAssignmentStatement(org.daiitech.naftah.parser.NaftahParser.AssignmentStatementContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitAssignmentStatement", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		boolean creatingObject = hasChildOrSubChildOfType(  ctx,
															org.daiitech.naftah.parser.NaftahParser.ObjectExpressionContext.class);
		currentContext.setCreatingObject(creatingObject);
		var result = visit(ctx.assignment());
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitFunctionDeclarationStatement(
													org.daiitech.naftah.parser.NaftahParser.FunctionDeclarationStatementContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitFunctionDeclarationStatement", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		var result = visit(ctx.functionDeclaration());
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitFunctionCallStatement(org.daiitech.naftah.parser.NaftahParser.FunctionCallStatementContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitFunctionCallStatement", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		var result = visit(ctx.functionCall());
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitIfStatementStatement(org.daiitech.naftah.parser.NaftahParser.IfStatementStatementContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitIfStatementStatement", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		var result = visit(ctx.ifStatement());
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitForStatementStatement(org.daiitech.naftah.parser.NaftahParser.ForStatementStatementContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitForStatementStatement", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		var result = visit(ctx.forStatement());
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitWhileStatementStatement(
												org.daiitech.naftah.parser.NaftahParser.WhileStatementStatementContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitWhileStatementStatement", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		var result = visit(ctx.whileStatement());
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitRepeatStatementStatement(
												org.daiitech.naftah.parser.NaftahParser.RepeatStatementStatementContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitRepeatStatementStatement", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		var result = visit(ctx.repeatStatement());
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitCaseStatementStatement(org.daiitech.naftah.parser.NaftahParser.CaseStatementStatementContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitCaseStatementStatement", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		var result = visit(ctx.caseStatement());
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitBreakStatementStatement(
												org.daiitech.naftah.parser.NaftahParser.BreakStatementStatementContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitBreakStatementStatement", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		var result = visit(ctx.breakStatement());
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitContinueStatementStatement(
													org.daiitech.naftah.parser.NaftahParser.ContinueStatementStatementContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitContinueStatementStatement", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		var result = visit(ctx.continueStatement());
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitReturnStatementStatement(
												org.daiitech.naftah.parser.NaftahParser.ReturnStatementStatementContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitReturnStatementStatement", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		var result = visit(ctx.returnStatement());
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitBlockStatement(org.daiitech.naftah.parser.NaftahParser.BlockStatementContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitBlockStatement", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		var result = visit(ctx.block());
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitDeclaration(org.daiitech.naftah.parser.NaftahParser.DeclarationContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitDeclaration", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		String variableName = ctx.ID().getText();
		// variable -> new : flags if this is a new variable or not
		Pair<DeclaredVariable, Boolean> declaredVariable;
		boolean isConstant = hasChild(ctx.CONSTANT());
		boolean isConstantOrVariable = isConstant || hasChild(ctx.VARIABLE());
		boolean hasType = hasChild(ctx.type());
		boolean creatingObject = currentContext.isCreatingObject();
		boolean creatingObjectField = hasAnyParentOfType(   ctx,
															org.daiitech.naftah.parser.NaftahParser.ObjectContext.class);
		if (isConstantOrVariable || hasType || creatingObjectField) {
			if (creatingObject && hasType) {
				Class<?> type = (Class<?>) visit(ctx.type());
				if (Objects.nonNull(type) && !Object.class.equals(type)) {
					throw new NaftahBugError(
												"لا يمكن أن يكون الكائن '%s' من النوع %s. يجب أن يكون الكائن عامًا لجميع الأنواع (%s)."
														.formatted( variableName,
																	getNaftahType(parser, type),
																	getNaftahType(parser, Object.class)));
				}
			}
			declaredVariable = createDeclaredVariable(this, ctx, variableName, isConstant, hasType);
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
					.orElse(createDeclaredVariable(this, ctx, variableName, false, false));
		}
		currentContext.markExecuted(ctx); // Mark as executed
		return currentContext.isParsingAssignment() ? declaredVariable : declaredVariable.a;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitAssignment(org.daiitech.naftah.parser.NaftahParser.AssignmentContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitAssignment", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		currentContext.setParsingAssignment(true);
		boolean creatingObjectField = hasAnyParentOfType(   ctx,
															org.daiitech.naftah.parser.NaftahParser.ObjectContext.class);
		Pair<DeclaredVariable, Boolean> declaredVariable = (Pair<DeclaredVariable, Boolean>) visit(ctx.declaration());
		currentContext.setDeclarationOfAssignment(declaredVariable);
		// TODO: check if inside function to check if it matches any argument /
		// parameter or previously
		if (declaredVariable.b) {
			declaredVariable = new Pair<>(DeclaredVariable
					.of(ctx,
						declaredVariable.a.getName(),
						declaredVariable.a.isConstant(),
						declaredVariable.a.getType(),
						visit(ctx.expression())), declaredVariable.b);
		}
		else {
			declaredVariable.a.setOriginalContext(ctx);
			declaredVariable.a.setValue(visit(ctx.expression()));
		}
		// declared and update if possible
		if (!creatingObjectField) {
			currentContext.setVariable(declaredVariable.a.getName(), declaredVariable.a);
		}
		currentContext.setParsingAssignment(false);
		currentContext.markExecuted(ctx); // Mark as executed
		return declaredVariable;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitFunctionDeclaration(org.daiitech.naftah.parser.NaftahParser.FunctionDeclarationContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitFunctionDeclaration", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		String functionName = ctx.ID().getText();
		DeclaredFunction declaredFunction = DeclaredFunction.of(ctx);
		currentContext.defineFunction(functionName, declaredFunction);
		currentContext.markExecuted(ctx); // Mark as executed
		return declaredFunction;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitParameterDeclarationList(
												org.daiitech.naftah.parser.NaftahParser.ParameterDeclarationListContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitArgumentDeclarationList", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		List<DeclaredParameter> args = new ArrayList<>();
		for (org.daiitech.naftah.parser.NaftahParser.ParameterDeclarationContext argumentDeclaration : ctx
				.parameterDeclaration()) {
			args.add((DeclaredParameter) visit(argumentDeclaration));
		}
		currentContext.markExecuted(ctx); // Mark as executed
		return args;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitParameterDeclaration(org.daiitech.naftah.parser.NaftahParser.ParameterDeclarationContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitArgumentDeclaration", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		String argumentName = ctx.ID().getText();
		var result = DeclaredParameter
				.of(ctx,
					argumentName,
					hasChild(ctx.CONSTANT()),
					hasChild(ctx.type()) ? visit(ctx.type()) : Object.class,
					hasChild(ctx.value()) ? visit(ctx.value()) : null);
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitFunctionCall(org.daiitech.naftah.parser.NaftahParser.FunctionCallContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitFunctionCall", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		Object result = null;
		// TODO: add extra vars to context to get the function called and so on, it can
		// be a free map
		// TODO: and using an Enum as key of predefined ids to get values
		currentContext.setParsingFunctionCallId(true);
		String functionName = hasChild(ctx.ID()) ? ctx.ID().getText() : (String) visit(ctx.qualifiedCall());
		// TODO: add support to variables as qualified call and match to the jvm
		// function
		String functionCallId = FUNCTION_CALL_ID_GENERATOR.apply(depth, functionName);
		currentContext.setFunctionCallId(functionCallId);
		List<Pair<String, Object>> args = new ArrayList<>();
		// TODO: add support to global variables as argument
		if (hasChild(ctx.argumentList())) {
			args = (List<Pair<String, Object>>) visit(ctx.argumentList());
		}

		if (currentContext.containsFunction(functionName)) {
			Object function = currentContext.getFunction(functionName, false).b;
			if (function instanceof DeclaredFunction declaredFunction) {
				boolean functionInStack = false;
				try {
					prepareDeclaredFunction(this, declaredFunction);
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
																			.entry( parameter.getName(),
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
					result = visit(declaredFunction.getBody());
				}
				finally {
					if (functionInStack) {
						popCall();
					}
				}
			}
			else if (function instanceof BuiltinFunction builtinFunction) {
				var methodArgs = args.stream().map(stringObjectPair -> stringObjectPair.b).toArray(Object[]::new);
				try {
					var possibleResult = builtinFunction.getMethod().invoke(null, methodArgs);
					if (builtinFunction.getFunctionInfo().returnType() != Void.class && possibleResult != null) {
						result = possibleResult;
					}
				}
				catch (IllegalAccessException | InvocationTargetException e) {
					throw new NaftahBugError(e);
				}
			}
			else if (function instanceof JvmFunction jvmFunction) {
				throw new NaftahBugError("الدالة '%s' من النوع: '%s' غير مدعومة حالياً"
						.formatted(functionName, JvmFunction.class.getName()));
			}
			else if (function instanceof Collection<?> functions) {
				throw new NaftahBugError("الدالة '%s' : '%s' من النوع: '%s' غير مدعومة حالياً"
						.formatted(functionName, functions, List.class.getName()));
			}
		}
		else {
			throw new NaftahBugError("الدالة '%s' غير موجودة في السياق الحالي.".formatted(functionName));
		}
		currentContext.setFunctionCallId(null);
		// TODO: add support for all kind of functions using the qualifiedName
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitQualifiedCall(org.daiitech.naftah.parser.NaftahParser.QualifiedCallContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitQualifiedCall", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		var result = visit(ctx.qualifiedName()) + ctx.COLON(0).getText() + ctx.COLON(1).getText() + ctx.ID();
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitArgumentList(org.daiitech.naftah.parser.NaftahParser.ArgumentListContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitArgumentList", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		List<Pair<String, Object>> args = new ArrayList<>();
		for (int i = 0; i < ctx.expression().size(); i++) {
			String name = hasChild(ctx.ID(i)) ? ctx.ID(i).getText() : null;
			Object value = visit(ctx.expression(i));
			args.add(new Pair<>(name, value)); // Evaluate each expression in the argument list
		}
		currentContext.markExecuted(ctx); // Mark as executed
		return args;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitIfStatement(org.daiitech.naftah.parser.NaftahParser.IfStatementContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitIfStatement", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		Object result = null;
		Object condition = visit(ctx.expression(0)); // Evaluate the condition expression
		if (isTruthy(condition)) {
			result = visit(ctx.block(0)); // If the condition is true, execute the 'then' block
		}
		else {
			boolean elseifConditionMatched = false;
			// Iterate through elseif blocks
			for (int i = 0; i < ctx.ELSEIF().size(); i++) {
				Object elseifCondition = visit(ctx.expression(i + 1)); // Evaluate elseif condition
				if (isTruthy(elseifCondition)) {
					result = visit(ctx.block(i + 1)); // Execute the corresponding elseif block if condition is true
					elseifConditionMatched = true;
					break;
				}
			}

			// If no elseif was true, execute the else block (if it exists)
			if (!elseifConditionMatched && hasChild(ctx.ELSE())) {
				result = visit(ctx.block(ctx.ELSEIF().size() + 1)); // Execute the 'else' block if present
			}
		}
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitForStatement(org.daiitech.naftah.parser.NaftahParser.ForStatementContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitForStatement", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		Object result = null;
		boolean loopInStack = false;
		String label = currentLoopLabel((String) (Objects.isNull(ctx.label()) ? null : visit(ctx.label())), depth);
		currentContext.setLoopLabel(label);
		// Initialization: ID := expression
		String loopVar = ctx.ID().getText();
		Object initValue = visit(ctx.expression(0));
		if (Objects.isNull(initValue)) {
			throw new NaftahBugError(String.format("القيمة الابتدائية للمتغير '%s' لا يمكن أن تكون فارغة.", loopVar));
		}
		// End value
		Object endValue = visit(ctx.expression(1));
		if (Objects.isNull(endValue)) {
			throw new NaftahBugError(String.format("القيمة النهائية للمتغير '%s' لا يمكن أن تكون فارغة.", loopVar));
		}

		if (!Number.class.isAssignableFrom(initValue.getClass()) || !Number.class
				.isAssignableFrom(endValue.getClass())) {
			throw new NaftahBugError(
										String
												.format("يجب أن تكون القيمتين الابتدائية والنهائية للمتغير '%s' من النوع الرقمي.",
														loopVar));
		}

		// Direction (TO or DOWNTO)
		boolean isAscending = ctx.TO() != null;
		// Loop block
		org.daiitech.naftah.parser.NaftahParser.BlockContext loopBlock = ctx.block(0);
		// Optional ELSE block
		org.daiitech.naftah.parser.NaftahParser.BlockContext elseBlock = null;
		if (ctx.block().size() > 1) {
			elseBlock = ctx.block(1);
		}

		boolean brokeEarly = false;
		boolean loopSignal = false;
		boolean propagateLoopSignal = false;

		try {
			pushLoop(label, ctx);
			loopInStack = true;
			currentContext.defineLoopVariable(loopVar, initValue, false);
			if (isAscending) {
				if (Boolean.TRUE.equals(applyOperation(endValue, initValue, LESS_THAN))) {
					throw new NaftahBugError(
												"القيمة النهائية يجب أن تكون أكبر أو تساوي القيمة الابتدائية في الحلقات التصاعدية");
				}

				for (;  Boolean.TRUE.equals(applyOperation(initValue, endValue, LESS_THAN_EQUALS));
						initValue = currentContext.setLoopVariable(loopVar, applyOperation(initValue, PRE_INCREMENT))) {
					result = visit(loopBlock);

					if (checkLoopSignal(result).equals(CONTINUE)) {
						loopSignal = true;
						String targetLabel = ((LoopSignal.LoopSignalDetails) result).targetLabel();
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
						String targetLabel = ((LoopSignal.LoopSignalDetails) result).targetLabel();
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
					throw new NaftahBugError(
												"القيمة الابتدائية يجب أن تكون أكبر أو تساوي القيمة النهائية في الحلقات التنازلية");
				}

				for (;  Boolean.TRUE.equals(applyOperation(initValue, endValue, GREATER_THAN_EQUALS));
						initValue = currentContext.setLoopVariable(loopVar, applyOperation(initValue, PRE_DECREMENT))) {
					result = visit(loopBlock);

					if (checkLoopSignal(result).equals(CONTINUE)) {
						loopSignal = true;
						String targetLabel = ((LoopSignal.LoopSignalDetails) result).targetLabel();
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
						String targetLabel = ((LoopSignal.LoopSignalDetails) result).targetLabel();
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
				result = visit(elseBlock);
			}
		}
		finally {
			currentContext.removeLoopVariable(loopVar, true);
			currentContext.setLoopLabel(null);
			if (loopInStack) {
				popLoop();
			}
		}

		currentContext.markExecuted(ctx); // Mark as executed
		return loopSignal && (LOOP_STACK.isEmpty() || !propagateLoopSignal) ?
				Optional
						.ofNullable((LoopSignal.LoopSignalDetails) result)
						.map(LoopSignal.LoopSignalDetails::result)
						.orElse(null) :
				result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitWhileStatement(org.daiitech.naftah.parser.NaftahParser.WhileStatementContext ctx) {
		return super.visitWhileStatement(ctx);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitRepeatStatement(org.daiitech.naftah.parser.NaftahParser.RepeatStatementContext ctx) {
		return super.visitRepeatStatement(ctx);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitCaseStatement(org.daiitech.naftah.parser.NaftahParser.CaseStatementContext ctx) {
		return super.visitCaseStatement(ctx);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitCaseLabelList(org.daiitech.naftah.parser.NaftahParser.CaseLabelListContext ctx) {
		return super.visitCaseLabelList(ctx);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitExpressionStatement(org.daiitech.naftah.parser.NaftahParser.ExpressionStatementContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitExpressionStatement", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		Object result = visit(ctx.expression()); // Evaluate and return the result
		currentContext.markExecuted(ctx); // Mark as executed
		return result; // No expression after 'return' means returning null
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitBreakStatement(org.daiitech.naftah.parser.NaftahParser.BreakStatementContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitBreakStatement", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		if (LOOP_STACK.isEmpty() || !checkInsideLoop(ctx)) {
			throw new NaftahBugError(String
					.format("لا يمكن استخدام '%s' خارج نطاق الحلقة.",
							getFormattedTokenSymbols(
														parser.getVocabulary(),
														org.daiitech.naftah.parser.NaftahLexer.BREAK,
														false)));
		}
		String currentLoopLabel = currentContext.getLoopLabel();
		String targetLabel = null;
		if (hasChild(ctx.ID())) {
			targetLabel = ctx.ID().getText();
		}
		if (targetLabel != null) {
			if (!loopContainsLabel(targetLabel)) {
				throw new NaftahBugError(String
						.format("لا توجد حلقة تحمل التسمية '%s' لاستخدام '%s' معها.",
								targetLabel,
								getFormattedTokenSymbols(   parser.getVocabulary(),
															org.daiitech.naftah.parser.NaftahLexer.BREAK,
															false)));
			}
			else if (targetLabel.equals(currentLoopLabel)) {
				throw newNaftahBugInvalidLoopLabelError(currentLoopLabel, parser);
			}
		}
		currentContext.markExecuted(ctx);
		return LoopSignal.LoopSignalDetails.of(BREAK, currentLoopLabel, targetLabel);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitContinueStatement(org.daiitech.naftah.parser.NaftahParser.ContinueStatementContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitContinueStatement", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		if (LOOP_STACK.isEmpty() || !checkInsideLoop(ctx)) {
			throw new NaftahBugError(String
					.format("لا يمكن استخدام '%s' خارج نطاق الحلقة.",
							getFormattedTokenSymbols(
														parser.getVocabulary(),
														org.daiitech.naftah.parser.NaftahLexer.CONTINUE,
														false)));
		}
		String currentLoopLabel = currentContext.getLoopLabel();
		String targetLabel = null;
		if (hasChild(ctx.ID())) {
			targetLabel = ctx.ID().getText();
		}

		if (targetLabel != null) {
			if (!loopContainsLabel(targetLabel)) {
				throw new NaftahBugError(String
						.format("لا توجد حلقة تحمل التسمية '%s' لاستخدام '%s' معها.",
								targetLabel,
								getFormattedTokenSymbols(   parser.getVocabulary(),
															org.daiitech.naftah.parser.NaftahLexer.CONTINUE,
															false)));
			}
			else if (targetLabel.equals(currentLoopLabel)) {
				throw newNaftahBugInvalidLoopLabelError(currentLoopLabel, parser);
			}
		}
		currentContext.markExecuted(ctx);
		return LoopSignal.LoopSignalDetails.of(CONTINUE, currentLoopLabel, targetLabel);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitReturnStatement(org.daiitech.naftah.parser.NaftahParser.ReturnStatementContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitReturnStatement", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		boolean insideLoop = !LOOP_STACK.isEmpty() || checkInsideLoop(ctx);
		Object result = null;
		if (hasChild(ctx.expression())) {
			result = visit(ctx.expression()); // Evaluate and return the result
		}
		currentContext.markExecuted(ctx); // Mark as executed
		return insideLoop ? LoopSignal.LoopSignalDetails.of(RETURN, result) : result; // No expression after 'return'
		// means returning null
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitBlock(org.daiitech.naftah.parser.NaftahParser.BlockContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitBlock", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		var nextContext = BLOCK_CONTEXT_SUPPLIER.apply(ctx, currentContext);
		depth = nextContext.getDepth();
		Object result = null;
		for (org.daiitech.naftah.parser.NaftahParser.StatementContext statement : ctx.statement()) {
			result = visit(statement); // Visit each statement in the block
			// break program after executing a return statemnt
			if (shouldBreakStatementsLoop(nextContext, statement, result)) {
				break;
			}
		}
		DEREGISTER_CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		depth--;
		nextContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitObjectExpression(org.daiitech.naftah.parser.NaftahParser.ObjectExpressionContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitObjectExpression", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		var result = visit(ctx.object());
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitObject(org.daiitech.naftah.parser.NaftahParser.ObjectContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitObject", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		var result = visit(ctx.objectFields());
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitObjectFields(org.daiitech.naftah.parser.NaftahParser.ObjectFieldsContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitObjectFields", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		var result = new HashMap<String, DeclaredVariable>();

		for (int i = 0; i < ctx.assignment().size(); i++) {
			var field = (Pair<DeclaredVariable, Boolean>) visit(ctx.assignment(i));
			var fieldName = field.a.getName();
			result.put(fieldName, field.a);
		}
		currentContext.setCreatingObject(false);
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitObjectAccessExpression(org.daiitech.naftah.parser.NaftahParser.ObjectAccessExpressionContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitObjectAccessExpression", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		var result = visit(ctx.qualifiedName());
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitCollectionExpression(org.daiitech.naftah.parser.NaftahParser.CollectionExpressionContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitCollectionExpression", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		var result = visit(ctx.collection());
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Object> visitListValue(org.daiitech.naftah.parser.NaftahParser.ListValueContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitListValue", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		var result = (List<Object>) visit(ctx.elements());
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Tuple visitTupleValue(org.daiitech.naftah.parser.NaftahParser.TupleValueContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitTupleValue", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		if (currentContext.isParsingAssignment()) {
			var currentDeclaration = currentContext.getDeclarationOfAssignment();
			Class<?> currentDeclarationType = currentDeclaration.a.getType();
			String currentDeclarationName = currentDeclaration.a.getName();
			if (Objects.nonNull(currentDeclarationType) && !Object.class.equals(currentDeclarationType)) {
				throw new NaftahBugError(
											("لا يُسمح بأن تحتوي التركيبة (tuple) '%s' على عناصر من النوع %s. التركيبة يجب أن تكون عامة " + "لجميع الأنواع (%s).")
													.formatted( currentDeclarationName,
																getNaftahType(parser, currentDeclarationType),
																getNaftahType(parser, Object.class)));
			}
		}
		var result = Tuple.of((List<Object>) visit(ctx.elements()));
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Object> visitSetValue(org.daiitech.naftah.parser.NaftahParser.SetValueContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitSetValue", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		var result = new HashSet<>((List<Object>) visit(ctx.elements()));
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<Object, Object> visitMapValue(org.daiitech.naftah.parser.NaftahParser.MapValueContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitMapValue", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		var result = (Map<Object, Object>) visit(ctx.keyValuePairs());
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Object> visitElements(org.daiitech.naftah.parser.NaftahParser.ElementsContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitElements", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		// prepare validations
		boolean creatingList = hasParentOfType(ctx, org.daiitech.naftah.parser.NaftahParser.ListValueContext.class);
		boolean creatingSet = hasParentOfType(ctx, org.daiitech.naftah.parser.NaftahParser.SetValueContext.class);
		boolean creatingTuple = hasParentOfType(ctx, org.daiitech.naftah.parser.NaftahParser.TupleValueContext.class);
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
		for (int i = 0; i < ctx.expression().size(); i++) {
			var elementValue = visit(ctx.expression(i));
			var elementType = Objects.nonNull(elementValue) ? elementValue.getClass() : Object.class;
			if (!creatingTuple) {
				// validating list has all the same type
				if (parsingAssignment && typeMismatch(elementValue, elementType, currentDeclarationType) || elementTypes
						.stream()
						.anyMatch(aClass -> typeMismatch(aClass, elementType))) {
					throw new NaftahBugError(
												("لا يمكن أن تحتوي %s %s على عناصر من أنواع مختلفة. يجب أن تكون جميع العناصر من نفس النوع" + " " + "%s.")
														.formatted( creatingList ? "القائمة (List)" : "المجموعة (Set)",
																	parsingAssignment ?
																			"'%s'".formatted(currentDeclarationName) :
																			"",
																	parsingAssignment ?
																			"(%s)"
																					.formatted(getNaftahType(   parser,
																												currentDeclarationType)) :
																			""));
				}

				if (creatingSet) {
					// validating set has no duplicates
					if (elements.stream().filter(Objects::nonNull).anyMatch(o -> o.equals(elementValue))) {
						throw new NaftahBugError(
													("تحتوي المجموعة %s على عناصر مكرّرة، وهذا غير مسموح في المجموعات (Set) التي يجب أن " + "تحتوي على عناصر فريدة فقط.")
															.formatted(parsingAssignment ?
																	"'%s'".formatted(currentDeclarationName) :
																	""));
					}
				}
			}
			elements.add(elementValue);
			if (Objects.nonNull(elementValue) && !Collection.class.isAssignableFrom(elementType) && !Map.class
					.isAssignableFrom(elementType)) {
				elementTypes.add(elementType);
			}
		}
		currentContext.markExecuted(ctx); // Mark as executed
		return elements;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<Object, Object> visitKeyValuePairs(org.daiitech.naftah.parser.NaftahParser.KeyValuePairsContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitKeyValuePairs", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		// prepare validations
		boolean creatingMap = hasParentOfType(ctx, org.daiitech.naftah.parser.NaftahParser.MapValueContext.class);
		boolean parsingAssignment = currentContext.isParsingAssignment();
		Class<?> currentDeclarationType = null;
		String currentDeclarationName = null;
		if (parsingAssignment) {
			var currentDeclaration = currentContext.getDeclarationOfAssignment();
			currentDeclarationType = currentDeclaration.a.getType();
			currentDeclarationName = currentDeclaration.a.getName();
		}
		// process entries
		Map<Object, Object> map = new HashMap<>();
		Set<Class<?>> keyTypes = new HashSet<>();
		Set<Class<?>> valueTypes = new HashSet<>();
		for (int i = 0; i < ctx.keyValue().size(); i++) {
			var entry = (Map.Entry<?, ?>) visit(ctx.keyValue(i));
			var key = entry.getKey();
			var keyType = Objects.nonNull(key) ? key.getClass() : Object.class;
			var value = entry.getValue();
			var valueType = Objects.nonNull(value) ? value.getClass() : Object.class;
			if (creatingMap) {
				// validating keys has all the same type
				// validating null keys
				if (Objects.isNull(key)) {
					throw new NaftahBugError(
												("لا يمكن أن يكون أحد المفاتيح في المصفوفة الترابطية (Map) %s فارغًا (null). يجب أن تكون " + "جميع المفاتيح معرّفة بشكل صحيح.")
														.formatted(parsingAssignment ?
																"'%s'".formatted(currentDeclarationName) :
																""));
				}

				if (parsingAssignment && typeMismatch(value, valueType, currentDeclarationType) || keyTypes
						.stream()
						.anyMatch(aClass -> typeMismatch(aClass, keyType))) {
					throw new NaftahBugError(
												("لا يمكن أن تحتوي المصفوفة الترابطية (Map) %s على عناصر من أنواع مختلفة. يجب أن تكون " + "جميع" + " العناصر من نفس النوع %s.")
														.formatted( parsingAssignment ?
																			"'%s'".formatted(currentDeclarationName) :
																			"",
																	parsingAssignment ?
																			"(%s)"
																					.formatted(getNaftahType(   parser,
																												currentDeclarationType)) :
																			""));
				}

				// validating keySet has no duplicates
				if (map.containsKey(key)) {
					throw new NaftahBugError(
												("تحتوي مجموعة المفاتيح للمصفوفة الترابطية %s على مفاتيح مكرّرة، وهذا غير مسموح في " + "المصفوفة الترابطية (Map) التي يجب أن تحتوي على مفاتيح فريدة فقط.")
														.formatted(parsingAssignment ?
																"'%s'".formatted(currentDeclarationName) :
																""));
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
		currentContext.markExecuted(ctx); // Mark as executed
		return map;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map.Entry<Object, Object> visitKeyValue(org.daiitech.naftah.parser.NaftahParser.KeyValueContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitKeyValue", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		var key = visit(ctx.expression(0));
		var value = visit(ctx.expression(1));
		// prepare validations
		boolean creatingMap = hasAnyParentOfType(ctx, org.daiitech.naftah.parser.NaftahParser.MapValueContext.class);
		if (!creatingMap && Objects.isNull(key)) {
			throw newNaftahBugNullError();
		}
		var result = new AbstractMap.SimpleEntry<>(key, value);
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitValueExpression(org.daiitech.naftah.parser.NaftahParser.ValueExpressionContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitValueExpression", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		// prepare validations
		boolean creatingCollection = hasAnyParentOfType(ctx,
														org.daiitech.naftah.parser.NaftahParser.CollectionContext.class);
		boolean parsingAssignment = currentContext.isParsingAssignment();

		// process value
		var result = visit(ctx.value());

		if (!creatingCollection && parsingAssignment) {
			var currentDeclaration = currentContext.getDeclarationOfAssignment();
			Class<?> currentDeclarationType = currentDeclaration.a.getType();
			Class<?> resultType = Objects.nonNull(result) ? result.getClass() : Object.class;
			String currentDeclarationName = currentDeclaration.a.getName();
			if (typeMismatch(result, resultType, currentDeclarationType)) {
				throw new NaftahBugError("القيمة '%s' لا تتوافق مع النوع المتوقع (%s)."
						.formatted( currentDeclarationName,
									getNaftahType(parser, currentDeclarationType)));
			}
		}

		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitMinusExpression(org.daiitech.naftah.parser.NaftahParser.MinusExpressionContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitMinusExpression", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		Object left = visit(ctx.expression(0)); // Left operand
		Object right = visit(ctx.expression(1)); // Right operand
		Object result;
		if (left == null || right == null) {
			result = null;
		}
		else {
			result = hasChild(ctx.MINUS()) ?
					applyOperation(left, right, SUBTRACT) :
					applyOperation(left, right, ELEMENTWISE_SUBTRACT);
		}
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitParenthesisExpression(org.daiitech.naftah.parser.NaftahParser.ParenthesisExpressionContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitParenthesisExpression", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		var result = visit(ctx.expression());
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitModExpression(org.daiitech.naftah.parser.NaftahParser.ModExpressionContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitModExpression", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		Object left = visit(ctx.expression(0)); // Left operand
		Object right = visit(ctx.expression(1)); // Right operand
		Object result;
		if (left == null || right == null) {
			result = null;
		}
		else {
			result = hasChild(ctx.MOD()) ?
					applyOperation(left, right, MODULO) :
					applyOperation(left, right, ELEMENTWISE_MODULO);
		}
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitDivExpression(org.daiitech.naftah.parser.NaftahParser.DivExpressionContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitDivExpression", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		Object left = visit(ctx.expression(0)); // Left operand
		Object right = visit(ctx.expression(1)); // Right operand
		Object result;
		if (left == null || right == null) {
			result = null;
		}
		else {
			result = hasChild(ctx.DIV()) ?
					applyOperation(left, right, DIVIDE) :
					applyOperation(left, right, ELEMENTWISE_DIVIDE);
		}
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitGreaterThanExpression(org.daiitech.naftah.parser.NaftahParser.GreaterThanExpressionContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitGreaterThanExpression", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		Object left = visit(ctx.expression(0)); // Left operand
		Object right = visit(ctx.expression(1)); // Right operand
		Object result;
		if (left == null || right == null) {
			result = null;
		}
		else {
			result = applyOperation(left, right, GREATER_THAN);
		}
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitLessThanEqualsExpression(
												org.daiitech.naftah.parser.NaftahParser.LessThanEqualsExpressionContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitLessThanEqualsExpression", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		Object left = visit(ctx.expression(0)); // Left operand
		Object right = visit(ctx.expression(1)); // Right operand
		Object result;
		if (left == null || right == null) {
			result = null;
		}
		else {
			result = applyOperation(left, right, LESS_THAN_EQUALS);
		}
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitGreaterThanEqualsExpression(
													org.daiitech.naftah.parser.NaftahParser.GreaterThanEqualsExpressionContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitGreaterThanEqualsExpression", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		Object left = visit(ctx.expression(0)); // Left operand
		Object right = visit(ctx.expression(1)); // Right operand
		Object result;
		if (left == null || right == null) {
			result = null;
		}
		else {
			result = applyOperation(left, right, GREATER_THAN_EQUALS);
		}
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitNotEqualsExpression(org.daiitech.naftah.parser.NaftahParser.NotEqualsExpressionContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitNotEqualsExpression", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		Object left = visit(ctx.expression(0)); // Left operand
		Object right = visit(ctx.expression(1)); // Right operand
		Object result;
		if (left == null || right == null) {
			result = null;
		}
		else {
			result = applyOperation(left, right, NOT_EQUALS);
		}
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitEqualsExpression(org.daiitech.naftah.parser.NaftahParser.EqualsExpressionContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitEqualsExpression", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		Object left = visit(ctx.expression(0)); // Left operand
		Object right = visit(ctx.expression(1)); // Right operand
		Object result;
		if (left == null || right == null) {
			result = null;
		}
		else {
			result = applyOperation(left, right, EQUALS);
		}
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitLessThanExpression(org.daiitech.naftah.parser.NaftahParser.LessThanExpressionContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitLessThanExpression", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		Object left = visit(ctx.expression(0)); // Left operand
		Object right = visit(ctx.expression(1)); // Right operand
		Object result;
		if (left == null || right == null) {
			result = null;
		}
		else {
			result = applyOperation(left, right, LESS_THAN);
		}
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitFunctionCallExpression(org.daiitech.naftah.parser.NaftahParser.FunctionCallExpressionContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitFunctionCallExpression", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		var result = visit(ctx.functionCall());
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitPlusExpression(org.daiitech.naftah.parser.NaftahParser.PlusExpressionContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitPlusExpression", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		Object left = visit(ctx.expression(0)); // Left operand
		Object right = visit(ctx.expression(1)); // Right operand
		Object result;
		if (left == null || right == null) {
			result = null;
		}
		else {
			result = hasChild(ctx.PLUS()) ?
					applyOperation(left, right, ADD) :
					applyOperation(left, right, ELEMENTWISE_ADD);
		}
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitMulExpression(org.daiitech.naftah.parser.NaftahParser.MulExpressionContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitMulExpression", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		Object left = visit(ctx.expression(0)); // Left operand
		Object right = visit(ctx.expression(1)); // Right operand
		Object result;
		if (left == null || right == null) {
			result = null;
		}
		else {
			result = hasChild(ctx.MUL()) ?
					applyOperation(left, right, MULTIPLY) :
					applyOperation(left, right, ELEMENTWISE_MULTIPLY);
		}
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitNumberValue(org.daiitech.naftah.parser.NaftahParser.NumberValueContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitNumberValue", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		Object value = ctx.NUMBER().getText();
		// TODO : minimize the overhead of creating dynamic number from number everytime we perform operation by
		//  creating and using dynamic number
		var result = NumberUtils.parseDynamicNumber(value);
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitRadixNumberValue(org.daiitech.naftah.parser.NaftahParser.RadixNumberValueContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitRadixNumberValue", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		String originalValue = ctx.BASE_DIGITS().getText();
		String arabicValue = originalValue
				.substring( 0,
							originalValue.length() - 2);
		String value = ArabicUtils
				.convertArabicToLatinLetterByLetter(arabicValue);
		String originalRadix = ctx.BASE_RADIX().getText();
		int radix = Integer
				.parseInt(originalRadix
						.substring( 0,
									originalRadix.length() - 1));
		var result = NumberUtils.parseDynamicNumber(value, radix, arabicValue);
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitCharacterValue(org.daiitech.naftah.parser.NaftahParser.CharacterValueContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitStringValue", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		Character result = ctx.CHARACTER().getText().charAt(1);
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitStringValue(org.daiitech.naftah.parser.NaftahParser.StringValueContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitStringValue", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		String value = ctx.STRING().getText();
		Object result;
		if (Objects.isNull(ctx.RAW()) && Objects.isNull(ctx.BYTE_ARRAY())) {
			result = StringInterpolator.process(value, currentContext);
		}
		else {
			value = StringInterpolator.cleanInput(value);
			if (Objects.nonNull(ctx.BYTE_ARRAY())) {
				result = value.getBytes(StandardCharsets.UTF_8);
			}
			else {
				result = value;
			}
		}
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitTrueValue(org.daiitech.naftah.parser.NaftahParser.TrueValueContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitTrueValue", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		currentContext.markExecuted(ctx); // Mark as executed
		return Boolean.TRUE;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitFalseValue(org.daiitech.naftah.parser.NaftahParser.FalseValueContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitFalseValue", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		currentContext.markExecuted(ctx); // Mark as executed
		return Boolean.FALSE;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitNullValue(org.daiitech.naftah.parser.NaftahParser.NullValueContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitNullValue", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		currentContext.markExecuted(ctx); // Mark as executed
		return null;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitIdValue(org.daiitech.naftah.parser.NaftahParser.IdValueContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitIdValue", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		// prepare validations
		boolean creatingMap = hasAnyParentOfType(ctx, org.daiitech.naftah.parser.NaftahParser.MapValueContext.class);
		boolean creatingObject = hasAnyParentOfType(ctx, org.daiitech.naftah.parser.NaftahParser.ObjectContext.class);
		String id = ctx.ID().getText();
		var result = creatingMap || creatingObject ? id : VARIABLE_GETTER.apply(id, currentContext).orElse(null);
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> visitVoidReturnType(org.daiitech.naftah.parser.NaftahParser.VoidReturnTypeContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitVoidReturnType", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		var result = getJavaType(ctx);
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitTypeReturnType(org.daiitech.naftah.parser.NaftahParser.TypeReturnTypeContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitTypeReturnType", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		var result = visit(ctx.type());
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> visitVarType(org.daiitech.naftah.parser.NaftahParser.VarTypeContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitVarType", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		var result = getJavaType(ctx);
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> visitBuiltInType(org.daiitech.naftah.parser.NaftahParser.BuiltInTypeContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitBuiltInType", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		var result = (Class<?>) visit(ctx.builtIn());
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> visitBuiltIn(org.daiitech.naftah.parser.NaftahParser.BuiltInContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitBuiltIn", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		var result = getJavaType(ctx);
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitQualifiedNameType(org.daiitech.naftah.parser.NaftahParser.QualifiedNameTypeContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitQualifiedNameType", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		// TODO: think about using id to variable or necessary other elements
		var result = visit(ctx.qualifiedName());
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitQualifiedName(org.daiitech.naftah.parser.NaftahParser.QualifiedNameContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitQualifiedName", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		Object result;
		boolean accessingObjectField = hasAnyParentOfType(  ctx,
															org.daiitech.naftah.parser.NaftahParser.ObjectAccessStatementContext.class) || hasAnyParentOfType(
																																								ctx,
																																								org.daiitech.naftah.parser.NaftahParser.ObjectAccessExpressionContext.class);
		if (accessingObjectField) {
			var qualifiedName = getQualifiedName(ctx);
			var accessArray = qualifiedName.split(":");
			if (accessArray.length > 1 && VARIABLE_GETTER
					.apply(accessArray[0], currentContext)
					.get() instanceof Map<?, ?> map) {
				var object = (Map<String, DeclaredVariable>) map;
				result = object;
				for (int i = 1; i < accessArray.length; i++) {
					if (i < accessArray.length - 1) {
						object = (Map<String, DeclaredVariable>) object.get(accessArray[i]).getValue();
					}
					else {
						result = object.get(accessArray[i]);
					}
				}
			}
			else {
				result = VARIABLE_GETTER.apply(accessArray[0], currentContext).get();
			}
		}
		else if (currentContext.isParsingFunctionCallId()) {
			result = getQualifiedName(ctx);
			currentContext.setParsingFunctionCallId(false);
		}
		else {
			result = getJavaType(ctx);
		}
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String visitLabel(org.daiitech.naftah.parser.NaftahParser.LabelContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitLabel", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		var result = ctx.ID().getText();
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitBitwiseXorExpression(org.daiitech.naftah.parser.NaftahParser.BitwiseXorExpressionContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitBitwiseXorExpression", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		Object left = visit(ctx.expression(0)); // Left operand
		Object right = visit(ctx.expression(1)); // Right operand
		Object result;
		if (left == null || right == null) {
			result = null;
		}
		else {
			result = applyOperation(left, right, BITWISE_XOR);
		}
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitNotExpression(org.daiitech.naftah.parser.NaftahParser.NotExpressionContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitNotExpression", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		var result = not(visit(ctx.expression()));
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitAndExpression(org.daiitech.naftah.parser.NaftahParser.AndExpressionContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitAndExpression", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		Object left = visit(ctx.expression(0)); // Left operand
		Object right = visit(ctx.expression(1)); // Right operand
		boolean result = isTruthy(left) && isTruthy(right);
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitOrExpression(org.daiitech.naftah.parser.NaftahParser.OrExpressionContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitOrExpression", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		Object left = visit(ctx.expression(0)); // Left operand
		Object right = visit(ctx.expression(1)); // Right operand
		boolean result = isTruthy(left) || isTruthy(right);
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitNegateExpression(org.daiitech.naftah.parser.NaftahParser.NegateExpressionContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitNegateExpression", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		var result = not(visit(ctx.expression()));
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitPreDecrementExpression(org.daiitech.naftah.parser.NaftahParser.PreDecrementExpressionContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitPreDecrementExpression", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		Object value = visit(ctx.expression());
		Object result;
		if (value == null) {
			result = null;
		}
		else {
			result = applyOperation(value, PRE_DECREMENT);
		}
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitPostDecrementExpression(
												org.daiitech.naftah.parser.NaftahParser.PostDecrementExpressionContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitPostDecrementExpression", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		Object value = visit(ctx.expression());
		Object result;
		if (value == null) {
			result = null;
		}
		else {
			result = applyOperation(value, POST_DECREMENT);
		}
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitBitwiseOrExpression(org.daiitech.naftah.parser.NaftahParser.BitwiseOrExpressionContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitBitwiseOrExpression", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		Object left = visit(ctx.expression(0)); // Left operand
		Object right = visit(ctx.expression(1)); // Right operand
		Object result;
		if (left == null || right == null) {
			result = null;
		}
		else {
			result = applyOperation(left, right, BITWISE_OR);
		}
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitBitwiseNotExpression(org.daiitech.naftah.parser.NaftahParser.BitwiseNotExpressionContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitBitwiseNotExpression", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		Object value = visit(ctx.expression());
		Object result;
		if (value == null) {
			result = null;
		}
		else {
			result = applyOperation(value, BITWISE_NOT);
		}
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitBitwiseAndExpression(org.daiitech.naftah.parser.NaftahParser.BitwiseAndExpressionContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitBitwiseAndExpression", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		Object left = visit(ctx.expression(0)); // Left operand
		Object right = visit(ctx.expression(1)); // Right operand
		Object result;
		if (left == null || right == null) {
			result = null;
		}
		else {
			result = applyOperation(left, right, BITWISE_AND);
		}
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitPreIncrementExpression(org.daiitech.naftah.parser.NaftahParser.PreIncrementExpressionContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitPreIncrementExpression", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		Object value = visit(ctx.expression());
		Object result;
		if (value == null) {
			result = null;
		}
		else {
			result = applyOperation(value, PRE_INCREMENT);
		}
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object visitPostIncrementExpression(
												org.daiitech.naftah.parser.NaftahParser.PostIncrementExpressionContext ctx) {
		debugCurrentContextVisit(LOGGER, "visitPostIncrementExpression", ctx);
		logExecution(ctx);
		var currentContext = CONTEXT_BY_DEPTH_SUPPLIER.apply(depth);
		Object value = visit(ctx.expression());
		Object result;
		if (value == null) {
			result = null;
		}
		else {
			result = applyOperation(value, POST_INCREMENT);
		}
		currentContext.markExecuted(ctx); // Mark as executed
		return result;
	}
}
