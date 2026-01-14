package org.daiitech.naftah.parser;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.Tree;
import org.daiitech.naftah.builtin.lang.BuiltinFunction;
import org.daiitech.naftah.builtin.lang.DeclaredFunction;
import org.daiitech.naftah.builtin.lang.DeclaredParameter;
import org.daiitech.naftah.builtin.lang.DeclaredVariable;
import org.daiitech.naftah.builtin.lang.JvmClassInitializer;
import org.daiitech.naftah.builtin.lang.JvmExecutable;
import org.daiitech.naftah.builtin.lang.JvmFunction;
import org.daiitech.naftah.builtin.lang.NaftahObject;
import org.daiitech.naftah.builtin.lang.None;
import org.daiitech.naftah.builtin.utils.ObjectUtils;
import org.daiitech.naftah.builtin.utils.concurrent.Task;
import org.daiitech.naftah.builtin.utils.tuple.ImmutablePair;
import org.daiitech.naftah.builtin.utils.tuple.MutablePair;
import org.daiitech.naftah.builtin.utils.tuple.NTuple;
import org.daiitech.naftah.builtin.utils.tuple.Pair;
import org.daiitech.naftah.builtin.utils.tuple.Triple;
import org.daiitech.naftah.errors.ExceptionUtils;
import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.utils.function.TriFunction;
import org.daiitech.naftah.utils.reflect.ClassUtils;
import org.daiitech.naftah.utils.reflect.ObjectAccessUtils;
import org.daiitech.naftah.utils.reflect.type.JavaType;

import com.ibm.icu.text.Normalizer2;

import static org.daiitech.naftah.Naftah.DEBUG_PROPERTY;
import static org.daiitech.naftah.Naftah.INSIDE_REPL_PROPERTY;
import static org.daiitech.naftah.Naftah.STANDARD_EXTENSIONS;
import static org.daiitech.naftah.builtin.utils.CollectionUtils.getElementAt;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.getNaftahType;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.isEmpty;
import static org.daiitech.naftah.errors.ExceptionUtils.INVALID_INSTANCE_METHOD_CALL_MSG;
import static org.daiitech.naftah.errors.ExceptionUtils.NOTE;
import static org.daiitech.naftah.errors.ExceptionUtils.newIllegalFieldAccessException;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugNullInputError;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahIllegalArgumentError;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahInstantiationError;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahInvocableListFoundError;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahInvocableNotFoundError;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahInvocationError;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahNonInvocableFunctionError;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahUnsupportedFunctionError;
import static org.daiitech.naftah.parser.DefaultContext.generateCallId;
import static org.daiitech.naftah.parser.DefaultContext.getCurrentContext;
import static org.daiitech.naftah.parser.DefaultContext.getVariable;
import static org.daiitech.naftah.parser.DefaultContext.newNaftahBugExistentVariableError;
import static org.daiitech.naftah.parser.DefaultContext.newNaftahBugVariableNotFoundError;
import static org.daiitech.naftah.parser.DefaultContext.peekCall;
import static org.daiitech.naftah.parser.DefaultContext.popCall;
import static org.daiitech.naftah.parser.DefaultContext.pushCall;
import static org.daiitech.naftah.parser.DefaultContext.registerContext;
import static org.daiitech.naftah.parser.DefaultNaftahParserVisitor.LOGGER;
import static org.daiitech.naftah.parser.DefaultNaftahParserVisitor.PARSER_VOCABULARY;
import static org.daiitech.naftah.parser.NaftahErrorListener.ERROR_HANDLER_INSTANCE;
import static org.daiitech.naftah.parser.NaftahExecutionLogger.logExecution;
import static org.daiitech.naftah.parser.StringInterpolator.cleanInput;
import static org.daiitech.naftah.utils.ResourceUtils.getJarDirectory;
import static org.daiitech.naftah.utils.ResourceUtils.getProperties;
import static org.daiitech.naftah.utils.ResourceUtils.readFileLines;
import static org.daiitech.naftah.utils.reflect.ClassUtils.QUALIFIED_NAME_SEPARATOR;
import static org.daiitech.naftah.utils.reflect.InvocationUtils.convertArgument;
import static org.daiitech.naftah.utils.reflect.InvocationUtils.findBestExecutable;
import static org.daiitech.naftah.utils.reflect.InvocationUtils.invokeJvmConstructor;
import static org.daiitech.naftah.utils.reflect.InvocationUtils.invokeJvmExecutable;
import static org.daiitech.naftah.utils.script.ScriptUtils.getRawHexBytes;

/**
 * Helper class for various parsing-related utilities used in the Naftah language parser.
 * Includes methods for working with parse trees, preparing function calls, resolving placeholders,
 * and other parsing helper utilities.
 *
 * <p>All methods are static, and instantiation is prevented.
 *
 * @author Chakib Daii
 */
public final class NaftahParserHelper {

	/**
	 * Format string for debug or log messages that include
	 * an index, text, and payload.
	 */
	public static final String FORMATTER = "index: %s, text: %s, payload: %s";
	/**
	 * Regex pattern for detecting placeholders in the form PLACEHOLDER(key).
	 */
	public static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("PLACEHOLDER\\((.*?)\\)");

	/**
	 * String representation for a null value in Naftah.
	 */
	public static final String NULL = "<فارغ>";
	/**
	 * Regex pattern for matching qualified names.
	 */
	public static final String QUALIFIED_NAME_REGEX = "^([^:]+)(:[^:]+)*$";
	/**
	 * Regex pattern for matching qualified calls.
	 */
	public static final String QUALIFIED_CALL_REGEX = "^([^:]+)(:[^:]+)*::[^:]+$";
	/**
	 * Unicode normalizer instance for normalization form NFKC.
	 */
	public static final Normalizer2 NORMALIZER = Normalizer2.getNFKCInstance();
	// Cache to store computed subtrees per node

	/**
	 * Cache to store computed subtrees per parse tree node for optimization.
	 */
	private static final Map<ParseTree, List<ParseTree>> SUB_TREE_CACHE = new IdentityHashMap<>();
	/**
	 * Loaded properties representing token symbols, if available.
	 */
	public static Properties TOKENS_SYMBOLS;
	/**
	 * A list of literal keywords or token strings defined by the lexer.
	 * <p>
	 * These are fixed words or symbols in the language (e.g., operators, control keywords)
	 * that are recognized directly by the lexer and mapped to specific tokens.
	 * </p>
	 */
	public static List<String> LEXER_LITERALS;

	static {
		try {
			TOKENS_SYMBOLS = getProperties(getJarDirectory() + "/tokens-symbols.properties");
			LEXER_LITERALS = readFileLines(getJarDirectory() + "/lexer-literals");
		}
		catch (Throwable ignored) {
			String jarDir = System.getProperty("naftah.jarDir");
			if (Objects.nonNull(jarDir)) {
				try {
					if (Objects.isNull(TOKENS_SYMBOLS)) {
						TOKENS_SYMBOLS = getProperties(jarDir + "/tokens-symbols.properties");
					}
					if (Objects.isNull(LEXER_LITERALS)) {
						LEXER_LITERALS = readFileLines(jarDir + "/lexer-literals");
					}
				}
				catch (Throwable ignoredThrowable) {
				}
			}
		}
	}

	/**
	 * Private constructor to prevent instantiation.
	 * Always throws a {@link NaftahBugError} when called.
	 */
	private NaftahParserHelper() {
		throw newNaftahBugInvalidUsageError();
	}

	/**
	 * Checks if the given parse tree node has a direct parent of the specified type.
	 *
	 * @param <T>  the type of the parent tree node
	 * @param ctx  the parse tree node to check
	 * @param type the parent class type to look for
	 * @return true if the parent is of the specified type; false otherwise
	 */
	public static <T extends Tree> boolean hasParentOfType(ParseTree ctx, Class<T> type) {
		return ctx != null && ctx.getParent() != null && type.isAssignableFrom(ctx.getParent().getClass());
	}


	/**
	 * Checks if the given parse tree node has any parent of any of the specified types.
	 *
	 * @param ctx   the parse tree node to check
	 * @param types list of class types to check for as parent types
	 * @return true if any parent matches one of the types; false otherwise
	 */
	public static boolean hasAnyParentOfType(ParseTree ctx, List<Class<? extends Tree>> types) {
		return !ObjectUtils.isEmpty(types) && types.stream().anyMatch(type -> hasAnyParentOfType(ctx, type));
	}

	/**
	 * Checks recursively if the given parse tree node has any ancestor (parent or further up)
	 * of the specified type.
	 *
	 * @param <T>  the type of the ancestor tree node
	 * @param ctx  the parse tree node to check
	 * @param type the ancestor class type to look for
	 * @return true if any ancestor is of the specified type; false otherwise
	 */
	public static <T extends Tree> boolean hasAnyParentOfType(ParseTree ctx, Class<T> type) {
		boolean hasParent = hasParentOfType(ctx, type);
		while (ctx.getParent() != null && !hasParent) {
			ctx = ctx.getParent();
			hasParent = hasParentOfType(ctx, type);
		}
		return hasParent;
	}

	/**
	 * Checks if the given child node is non-null.
	 *
	 * @param <T>   the type of the child node
	 * @param child the child node to check
	 * @return true if child is not null; false otherwise
	 */
	public static <T extends Tree> boolean hasChild(T child) {
		return child != null;
	}

	/**
	 * Checks if the given child node is of the specified type.
	 *
	 * @param <T>   the child node type
	 * @param <T1>  the type to check against
	 * @param child the child node to check
	 * @param type  the class type to check
	 * @return true if child is non-null and of the specified type; false otherwise
	 */
	public static <T, T1 extends Tree> boolean hasChildOfType(T child, Class<T1> type) {
		return child != null && type.isAssignableFrom(child.getClass());
	}

	/**
	 * Checks if any child in the list is of the specified type.
	 *
	 * @param <T>      the type of elements in the children list
	 * @param <T1>     the type to check against
	 * @param children the list of child nodes
	 * @param type     the class type to check
	 * @return true if any child is of the specified type; false otherwise
	 */
	public static <T, T1 extends Tree> boolean hasChildOfType(List<T> children, Class<T1> type) {
		return !ObjectUtils.isEmpty(children) && children.stream().anyMatch(child -> hasChildOfType(child, type));
	}

	/**
	 * Checks if the given parse tree node has a child or sub-child of the specified type.
	 *
	 * @param <T>  the type to check for in descendants
	 * @param ctx  the parse tree node to check
	 * @param type the class type to check
	 * @return true if any child or descendant is of the specified type; false otherwise
	 */
	public static <T extends Tree> boolean hasChildOrSubChildOfType(ParseTree ctx, Class<T> type) {
		var children = getAllChildren(ctx);
		return !ObjectUtils.isEmpty(children) && children.stream().anyMatch(child -> hasChildOfType(child, type));
	}

	/**
	 * Checks if any executed child or sub-child of the specified type exists under the given node,
	 * based on the provided execution property.
	 *
	 * @param <T>                       the type of child to check
	 * @param ctx                       the root parse tree node
	 * @param type                      the child class type to look for
	 * @param executedParseTreeProperty a property map indicating executed nodes
	 * @return true if any executed child or descendant is of the specified type; false otherwise
	 */
	public static <T extends Tree> boolean hasAnyExecutedChildOrSubChildOfType( ParseTree ctx,
																				Class<T> type,
																				ParseTreeProperty<Boolean> executedParseTreeProperty) {
		return getAllChildrenOfType(ctx, type)
				.stream()
				.anyMatch(child -> Optional
						.ofNullable(executedParseTreeProperty)
						.map(parseTreeProperty -> parseTreeProperty.get(child))
						.orElse(false));
	}

	/**
	 * Retrieves all children of the given parse tree node that are of the specified type.
	 *
	 * @param <T>  the type of children to retrieve
	 * @param ctx  the parse tree node
	 * @param type the class type to filter children by
	 * @return a list of children of the specified type, or empty list if none found
	 */
	public static <T extends Tree> List<ParseTree> getAllChildrenOfType(ParseTree ctx, Class<T> type) {
		var children = getAllChildren(ctx);
		return !ObjectUtils.isEmpty(children) ?
				children.stream().filter(child -> hasChildOfType(child, type)).toList() :
				List.of();
	}

	/**
	 * Recursively searches the given parse tree node and its descendants for the
	 * first occurrence of a child whose type matches the specified class.
	 * <p>
	 * The search is performed in a pre-order, depth-first manner:
	 * <ul>
	 * <li>First, the current {@code node} itself is checked.</li>
	 * <li>If it matches the requested {@code type}, it is returned immediately.</li>
	 * <li>Otherwise, each child is visited in the order they appear in the tree.</li>
	 * </ul>
	 * <p>
	 * This method does <b>not</b> restrict the search to direct children; it will
	 * return the first matching descendant at any depth.
	 *
	 * @param node the root of the subtree to search; may be {@code null}
	 * @param type the class object representing the desired parse tree type
	 * @param <T>  the expected parse tree subtype
	 * @return the first node (including {@code node} itself) that is an instance
	 *         of {@code type}, or {@code null} if no matching node is found
	 */
	public static <T extends ParseTree> T getFirstChildOfType(ParseTree node, Class<T> type) {
		if (node == null) {
			return null;
		}

		if (type.isInstance(node)) {
			return type.cast(node);
		}

		for (int i = 0; i < node.getChildCount(); i++) {
			T child = getFirstChildOfType(node.getChild(i), type);
			if (child != null) {
				return child;
			}
		}

		return null;
	}

	/**
	 * Collects all nodes in the subtree rooted at the given parse tree node, including itself.
	 * Uses an internal cache to optimize repeated calls on the same node.
	 *
	 * @param ctx the root parse tree node
	 * @return list of all descendant nodes including the root node
	 */
	public static List<ParseTree> getAllChildren(ParseTree ctx) {
		// If cached, return from cache
		if (SUB_TREE_CACHE.containsKey(ctx)) {
			return SUB_TREE_CACHE.get(ctx);
		}

		List<ParseTree> nodes = new ArrayList<>();
		collect(ctx, nodes);
		SUB_TREE_CACHE.put(ctx, nodes); // Cache the result
		return nodes;
	}

	/**
	 * Recursively collects all nodes in the subtree rooted at the given node.
	 *
	 * @param node the current parse tree node
	 * @param out  the list to accumulate nodes
	 */
	private static void collect(ParseTree node, List<ParseTree> out) {
		out.add(node); // Include the node itself
		for (int i = 0; i < node.getChildCount(); i++) {
			collect(node.getChild(i), out);
		}
	}

	/**
	 * Visits the given parse tree using the provided Naftah parser visitor.
	 *
	 * @param naftahParserBaseVisitor the visitor instance
	 * @param tree                    the parse tree to visit
	 * @return the result of the visit operation
	 */
	public static Object visit( org.daiitech.naftah.parser.NaftahParserBaseVisitor<?> naftahParserBaseVisitor,
								ParseTree tree) {
		return naftahParserBaseVisitor.visit(tree);
	}

	/**
	 * Prepares a declared function by visiting and setting its parameters and return type if not already set.
	 *
	 * @param naftahParserBaseVisitor the visitor to use for visiting parameter and return type contexts
	 * @param function                the declared function to prepare
	 */
	public static void prepareDeclaredFunction(
												org.daiitech.naftah.parser.NaftahParserBaseVisitor<?> naftahParserBaseVisitor,
												DeclaredFunction<?> function) {
		if (function.getParameters() == null && hasChild(function.getParametersContext())) {
			//noinspection unchecked
			function
					.setParameters(
									(List<DeclaredParameter>) visit(naftahParserBaseVisitor,
																	function.getParametersContext()));
		}

		if (function.getReturnType() == null) {
			if (hasChild(function.getReturnTypeContext())) {
				function.setReturnType((JavaType) visit(naftahParserBaseVisitor, function.getReturnTypeContext()));
			}
			else {
				function.setReturnType(JavaType.ofObject());
			}
		}
	}

	/**
	 * Prepares a map of argument names to their values for a declared function.
	 *
	 * @param parameters List of declared parameters for the function.
	 * @param arguments  List of pairs representing argument name (nullable) and value.
	 * @return A map of parameter names to argument values.
	 * @throws NaftahBugError if more arguments are passed than parameters, or if
	 *                        required parameters are missing or duplicated.
	 */
	public static Map<String, Object> prepareDeclaredFunctionArguments( List<DeclaredParameter> parameters,
																		List<Pair<String, Object>> arguments) {
		if (parameters.size() < arguments.size()) {
			throw new NaftahBugError(
										"عدد الوسائط الممررة '%s' يتجاوز عدد المعاملات '%s' المحددة."
												.formatted(arguments, parameters));
		}

		// how many params don't have defaults
		List<DeclaredParameter> requiredParams = parameters.stream().filter(p -> p.getDefaultValue() == null).toList();

		// arguments that have names
		Map<Integer, Pair<String, Object>> namedArguments = IntStream.range(0, arguments.size()).mapToObj(i -> {
			var current = arguments.get(i);
			return current.getLeft() != null ? Map.entry(i, current) : null;
		}).filter(Objects::nonNull).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		Map<String, Object> finalArguments = new HashMap<>();
		if (namedArguments.isEmpty()) {
			if (arguments.size() < requiredParams.size()) {
				throw new NaftahBugError(
											"عدد الوسائط الممررة '%s' أقل من عدد المعاملات '%s' المحددة."
													.formatted(arguments, parameters));
			}
			// process non named args
			finalArguments = IntStream.range(0, arguments.size()).mapToObj(i -> {
				var argument = arguments.get(i);
				var param = i >= requiredParams.size() ? parameters.get(i) : requiredParams.get(i);
				return Map.entry(param.getName(), argument.getRight());
			})
					.collect(Collectors
							.toMap( Map.Entry::getKey,
									Map.Entry::getValue,
									(o, o2) -> o2));
		}
		else {
			Set<String> usedNames = new HashSet<>();
			// arguments that have no names
			Map<Integer, Pair<String, Object>> positionalArguments = IntStream
					.range(0, arguments.size())
					.mapToObj(i -> {
						var current = arguments.get(i);
						return current.getLeft() == null ? Map.entry(i, current) : null;
					})
					.filter(Objects::nonNull)
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

			// Assign positional arguments
			for (var entry : positionalArguments.entrySet()) {
				String paramName = parameters.get(entry.getKey()).getName();
				if (namedArguments.containsKey(entry.getKey())) {
					throw new NaftahBugError("تم تحديد الوسيط '%s' موقعياً وبالاسم في آنٍ واحد.".formatted(paramName));
				}
				finalArguments.put(paramName, entry.getValue().getRight());
				usedNames.add(paramName);
			}

			// Assign named arguments
			for (var entry : namedArguments.entrySet()) {
				DeclaredParameter param = parameters.get(entry.getKey());

				if (param != null) {
					String paramName = param.getName();

					if (usedNames.contains(paramName)) {
						throw new NaftahBugError("تم تمرير الوسيط '%s' أكثر من مرة.".formatted(paramName));
					}

					finalArguments.put(paramName, entry.getValue().getRight());
					usedNames.add(paramName);

				}
				else {
					throw new NaftahBugError("الوسيط '%s' لا يتوافق مع أي من المعاملات المحددة." + entry
							.getValue()
							.getLeft());
				}
			}

			// Assign default values
			for (DeclaredParameter param : parameters) {
				if (!finalArguments.containsKey(param.getName())) {
					if (param.getDefaultValue() != null) {
						finalArguments.put(param.getName(), param.getDefaultValue());
					}
					else {
						throw new NaftahBugError(
													"الوسيط '%s' لم يتم مطابقته مع أي من المعاملات."
															.formatted(param.getName()));
					}
				}
			}
		}

		return finalArguments;
	}

	/**
	 * Constructs a qualified name string from the given parse context.
	 *
	 * @param ctx The qualified name parse context.
	 * @return The fully qualified name as a string.
	 */
	public static String getQualifiedName(org.daiitech.naftah.parser.NaftahParser.QualifiedNameContext ctx) {
		AtomicReference<StringBuffer> result = new AtomicReference<>(new StringBuffer());

		String id = matchImplementationName(ctx.selfOrId(), getCurrentContext());
		result.get().append(id);

		for (int i = 0; i < ctx.ID().size(); i++) {
			String qualifier = Objects.nonNull(ctx.QUESTION(i)) ?
					ctx.QUESTION(i).getText() + ctx.COLON(i).getText() :
					ctx.COLON(i).getText();
			result.get().append(qualifier);

			id = ctx.ID(i).getText();
			result.get().append(id);
		}
		return result.get().toString();
	}

	/**
	 * Constructs a qualified name string from the given parse context.
	 *
	 * @param ctx The object access parse context.
	 * @return The fully qualified name as a string.
	 */
	public static String getQualifiedName(org.daiitech.naftah.parser.NaftahParser.QualifiedObjectAccessContext ctx) {
		AtomicReference<StringBuffer> result = new AtomicReference<>(new StringBuffer());

		String id = matchImplementationName(ctx.selfOrId(), getCurrentContext());
		result.get().append(id);

		for (int i = 0; i < ctx.propertyAccess().size(); i++) {
			String qualifier = Objects.nonNull(ctx.QUESTION(i)) ?
					ctx.QUESTION(i).getText() + QUALIFIED_NAME_SEPARATOR :
					QUALIFIED_NAME_SEPARATOR;
			result.get().append(qualifier);

			final var currentPropertyAccess = ctx.propertyAccess(i);
			id = Optional
					.of(currentPropertyAccess)
					.map(propertyAccessContext -> Optional
							.ofNullable(propertyAccessContext.ID())
							.map(ParseTree::getText)
							.orElse(Optional
									.ofNullable(propertyAccessContext.CHARACTER())
									.map(ParseTree::getText)
									.orElse(Optional
											.ofNullable(propertyAccessContext.STRING())
											.map(ParseTree::getText)
											.orElse(null))))
					.orElseThrow(() -> newNaftahBugNullInputError(true, currentPropertyAccess));
			result.get().append(cleanInput(id));
		}

		return result.get().toString();
	}

	/**
	 * Prepares a parser instance from the given input character stream with no error listeners.
	 *
	 * @param input The input character stream.
	 * @return The prepared parser instance.
	 */
	public static org.daiitech.naftah.parser.NaftahParser prepareRun(CharStream input) {
		return prepareRun(input, List.of());
	}

	/**
	 * Prepares a parser instance from the given input character stream with a single error listener.
	 *
	 * @param input         The input character stream.
	 * @param errorListener The error listener to add.
	 * @return The prepared parser instance.
	 */
	public static org.daiitech.naftah.parser.NaftahParser prepareRun(   CharStream input,
																		ANTLRErrorListener errorListener) {
		return prepareRun(input, List.of(errorListener));
	}

	/**
	 * Initializes and prepares a {@link org.daiitech.naftah.parser.NaftahParser}
	 * instance from the given {@link CharStream}.
	 * <p>
	 * This method performs the following steps:
	 * <ul>
	 * <li>Creates a lexer and token stream from the input</li>
	 * <li>Registers all provided {@link ANTLRErrorListener} instances</li>
	 * <li>Optionally prints the generated tokens when debugging is enabled</li>
	 * <li>Creates and returns a configured parser instance</li>
	 * </ul>
	 *
	 * @param input          the input character stream to be parsed
	 * @param errorListeners a list of {@link ANTLRErrorListener} instances to be attached to
	 *                       both the lexer and the parser
	 * @return a fully configured {@link org.daiitech.naftah.parser.NaftahParser}
	 *         ready for execution
	 */
	public static org.daiitech.naftah.parser.NaftahParser prepareRun(   CharStream input,
																		List<ANTLRErrorListener> errorListeners) {
		// Create a lexer and token stream
		var lexerCommonTokenStreamPair = getCommonTokenStream(input, errorListeners);

		CommonTokenStream tokens = lexerCommonTokenStreamPair.getRight();

		if (Boolean.getBoolean(DEBUG_PROPERTY)) {
			printTokens(tokens, lexerCommonTokenStreamPair.getLeft().getVocabulary());
		}

		// Create a parser
		return getParser(tokens, errorListeners);
	}

	/**
	 * Prints all tokens produced by the lexer for debugging purposes.
	 * <p>
	 * Each token is printed with its symbolic name and its original text
	 * representation as extracted from the input.
	 * <p>
	 * This method forces the token stream to be fully populated by invoking
	 * {@link CommonTokenStream#fill()}.
	 *
	 * @param tokens     the token stream generated by the lexer
	 * @param vocabulary the lexer vocabulary used to resolve symbolic token names
	 */
	public static void printTokens(CommonTokenStream tokens, Vocabulary vocabulary) {
		tokens.fill();
		System.out.println("Tokens:");
		for (Token token : tokens.getTokens()) {
			System.out
					.printf("Token: %-20s Text: %s%n",
							vocabulary.getSymbolicName(token.getType()),
							token.getText());
		}
	}

	/**
	 * Executes the parser by visiting the parse tree and returning the result.
	 *
	 * @param parser The parser instance.
	 * @return The result of visiting the parse tree.
	 */
	public static Object doRun(org.daiitech.naftah.parser.NaftahParser parser, List<String> args) {
		// Create a visitor and visit the parse tree
		DefaultNaftahParserVisitor visitor = new DefaultNaftahParserVisitor(parser, args);
		// Parse the input and get the parse tree
		return visitor.visit();
	}

	/**
	 * Creates a parser instance from the given token stream and a single error listener.
	 *
	 * @param commonTokenStream The token stream.
	 * @param errorListener     The error listener.
	 * @return The parser instance.
	 */
	public static org.daiitech.naftah.parser.NaftahParser getParser(CommonTokenStream commonTokenStream,
																	ANTLRErrorListener errorListener) {
		return getParser(commonTokenStream, List.of(errorListener));
	}

	/**
	 * Creates and configures a {@link org.daiitech.naftah.parser.NaftahParser}
	 * using the provided {@link CommonTokenStream}.
	 * <p>
	 * All default error listeners are removed and replaced with the supplied
	 * {@link ANTLRErrorListener} instances. The parser is also configured to use
	 * a {@link org.antlr.v4.runtime.BailErrorStrategy} for fast-fail parsing.
	 *
	 * @param commonTokenStream the token stream produced by the lexer
	 * @param errorListeners    the error listeners to be attached to the parser
	 * @return a fully configured {@link org.daiitech.naftah.parser.NaftahParser}
	 */
	public static org.daiitech.naftah.parser.NaftahParser getParser(CommonTokenStream commonTokenStream,
																	List<ANTLRErrorListener> errorListeners) {
		return getParser(   () -> new org.daiitech.naftah.parser.NaftahParser(commonTokenStream),
							errorListeners);
	}

	/**
	 * Creates and configures a generic ANTLR {@link Parser} instance.
	 * <p>
	 * This method:
	 * <ul>
	 * <li>Instantiates the parser using the supplied {@link Supplier}</li>
	 * <li>Removes all default error listeners</li>
	 * <li>Registers the provided {@link ANTLRErrorListener} instances</li>
	 * <li>Configures the parser to use a shared {@code BailErrorStrategy}</li>
	 * </ul>
	 *
	 * @param <T>            the concrete type of the parser
	 * @param parserSupplier supplier used to create the parser instance
	 * @param errorListeners the error listeners to be added to the parser
	 * @return a configured parser instance
	 */
	public static <T extends Parser> T getParser(   Supplier<T> parserSupplier,
													List<ANTLRErrorListener> errorListeners) {
		// Create a parser
		T parser = parserSupplier.get();
		parser.removeErrorListeners();
		errorListeners.forEach(parser::addErrorListener);

		// Use the BailErrorStrategy
		parser.setErrorHandler(ERROR_HANDLER_INSTANCE);

		return parser;
	}

	/**
	 * Gets a CommonTokenStream from the given character stream with no error listeners.
	 *
	 * @param charStream The character stream.
	 * @return The CommonTokenStream.
	 */
	public static CommonTokenStream getCommonTokenStream(CharStream charStream) {
		return getCommonTokenStream(charStream, List.of()).getRight();
	}

	/**
	 * Gets a CommonTokenStream from the given character stream and a single error listener.
	 *
	 * @param charStream    The character stream.
	 * @param errorListener The error listener.
	 * @return The CommonTokenStream.
	 */
	public static CommonTokenStream getCommonTokenStream(CharStream charStream, ANTLRErrorListener errorListener) {
		return getCommonTokenStream(charStream, List.of(errorListener)).getRight();
	}

	/**
	 * Creates and configures a {@link org.daiitech.naftah.parser.NaftahLexer}
	 * and its associated {@link CommonTokenStream} from the given
	 * {@link CharStream}.
	 * <p>
	 * All default lexer error listeners are removed and replaced with the
	 * provided {@link ANTLRErrorListener} instances.
	 *
	 * @param charStream     the input character stream to be tokenized
	 * @param errorListeners the error listeners to be attached to the lexer
	 * @return a {@link Pair} containing the configured lexer and its
	 *         {@link CommonTokenStream}
	 */
	public static Pair<org.daiitech.naftah.parser.NaftahLexer, CommonTokenStream> getCommonTokenStream(
																										CharStream charStream,
																										List<ANTLRErrorListener> errorListeners) {
		return getCommonTokenStream(() -> new org.daiitech.naftah.parser.NaftahLexer(charStream),
									errorListeners);
	}

	/**
	 * Creates and configures a generic ANTLR {@link Lexer} and its
	 * corresponding {@link CommonTokenStream}.
	 * <p>
	 * This method:
	 * <ul>
	 * <li>Instantiates the lexer using the supplied {@link Supplier}</li>
	 * <li>Removes all default lexer error listeners</li>
	 * <li>Registers the provided {@link ANTLRErrorListener} instances</li>
	 * <li>Creates a {@link CommonTokenStream} backed by the lexer</li>
	 * </ul>
	 *
	 * @param <T>            the concrete type of the lexer
	 * @param lexerSupplier  supplier used to create the lexer instance
	 * @param errorListeners the error listeners to be added to the lexer
	 * @return a {@link Pair} containing the configured lexer and its
	 *         {@link CommonTokenStream}
	 */
	public static <T extends Lexer> Pair<T, CommonTokenStream> getCommonTokenStream(
																					Supplier<T> lexerSupplier,
																					List<ANTLRErrorListener> errorListeners) {
		// Create a lexer and token stream
		T lexer = lexerSupplier.get();
		lexer.removeErrorListeners();
		errorListeners.forEach(lexer::addErrorListener);
		return ImmutablePair.of(lexer, new CommonTokenStream(lexer));
	}

	/**
	 * Creates a {@link CharStream} from either script content or a script file.
	 * <p>
	 * When {@code isScriptFile} is {@code true}, the {@code script} parameter is
	 * treated as a file path. The file is located using the internal script
	 * resolution logic and then read using UTF-8 encoding.
	 * <p>
	 * When {@code isScriptFile} is {@code false}, the {@code script} parameter is
	 * treated as raw script content and converted directly into a {@link CharStream}.
	 *
	 * @param isScriptFile {@code true} if {@code script} represents a script file path,
	 *                     {@code false} if it represents script content
	 * @param script       the script content or script file path
	 * @return a {@link CharStream} representing the script input
	 * @throws Exception if an error occurs while locating or reading the script file
	 */
	public static CharStream getCharStream(boolean isScriptFile, String script) throws Exception {
		CharStream charStream;
		if (isScriptFile) {
			// Search for path
			Path filePath = searchForNaftahScriptFile(script).toPath();
			charStream = CharStreams.fromPath(filePath, StandardCharsets.UTF_8);
		}
		else {
			charStream = getCharStream(script);
		}
		return charStream;
	}

	/**
	 * Creates a {@link CharStream} directly from script content.
	 * <p>
	 * The script text is normalized before being converted into a character stream.
	 * Optional debugging output may be produced when the debug property is enabled.
	 * <p>
	 * Platform-specific handling (e.g., Windows vs POSIX) is encapsulated internally.
	 *
	 * @param script the script content
	 * @return a {@link CharStream} representing the normalized script
	 */
	public static CharStream getCharStream(String script) {
		script = NORMALIZER.normalize(script);

		if (Boolean.getBoolean(DEBUG_PROPERTY)) {
			getRawHexBytes(script);
		}

		return CharStreams.fromString(script);
	}

	/**
	 * Searches for a Naftah script file based on a given name, trying multiple extensions.
	 *
	 * <p>
	 * Tries in this order: - actual supplied name - name.naftah - name.nfth -
	 * name.na - name.nsh
	 *
	 * @param input The input file name or path.
	 * @return The File object pointing to the found script file, or the original if none found.
	 * @since 0.0.1
	 */
	public static File searchForNaftahScriptFile(String input) {
		String scriptFileName = input.trim();
		File scriptFile = new File(scriptFileName);
		int i = 0;
		while (i < STANDARD_EXTENSIONS.length && !scriptFile.exists()) {
			scriptFile = new File(scriptFileName + STANDARD_EXTENSIONS[i]);
			i++;
		}
		// if we still haven't found the file, point back to the originally specified
		// filename
		if (!scriptFile.exists()) {
			scriptFile = new File(scriptFileName);
		}
		return scriptFile;
	}

	/**
	 * Resolves placeholders in the properties values by replacing them with their corresponding
	 * property values.
	 *
	 * @param props The Properties object with placeholders to resolve.
	 */
	public static void resolvePlaceholders(Properties props) {
		for (String key : props.stringPropertyNames()) {
			String value = props.getProperty(key);
			String resolved = resolveValue(value, props);
			props.setProperty(key, resolved);
		}
	}

	/**
	 * Resolves placeholders within a string value using the given properties.
	 *
	 * @param value The string potentially containing placeholders.
	 * @param props The properties to use for resolving placeholders.
	 * @return The resolved string, possibly wrapped in single quotes if placeholders were replaced.
	 */
	private static String resolveValue(String value, Properties props) {
		Matcher matcher = PLACEHOLDER_PATTERN.matcher(value);
		StringBuilder result = new StringBuilder();
		boolean found = false;
		while (matcher.find()) {
			found = true;
			String placeholderKey = matcher.group(1);
			String replacement = props.getProperty(placeholderKey, "").split(",")[0].replaceAll("'", "");
			matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
		}

		matcher.appendTail(result);
		var resultValue = result.toString();
		return found ? "'" + resultValue.replaceAll(" ", "") + "'" : resultValue;
	}

	/**
	 * Converts a declared variable or constant value to a string representation.
	 *
	 * @param constant True if the variable is constant, false if mutable.
	 * @param name     The name of the variable.
	 * @param type     the variable type, default {@code JavaType::ofObject}.
	 * @param value    The value of the variable.
	 * @return A formatted string representing the declared variable or constant.
	 */
	public static String declaredValueToString(boolean constant, String name, JavaType type, Object value) {
		return "<%s %s : %s = %s>"
				.formatted( constant ? "ثابت" : "متغير",
							name,
							getNaftahType(PARSER_VOCABULARY, type),
							Optional.ofNullable(value).map(o -> {
								if (o instanceof Boolean aBoolean) {
									return ObjectUtils.booleanToString(aBoolean);
								}
								return o;
							}).orElse(NaftahParserHelper.NULL));
	}

	/**
	 * Returns a formatted string of token symbols based on the token type.
	 *
	 * @param vocabulary The vocabulary containing token definitions.
	 * @param tokenType  The token type.
	 * @param ln         If true, formats output with a line break.
	 * @return A string representing token symbols, or null if none found.
	 */
	public static String getFormattedTokenSymbols(Vocabulary vocabulary, int tokenType, boolean ln) {
		String tokenName = vocabulary.getDisplayName(tokenType);
		return getFormattedTokenSymbols(tokenName, ln);
	}

	/**
	 * Returns a formatted string representing the symbols associated with a token name.
	 * <p>
	 * This method looks up {@code tokenName} in the {@code TOKENS_SYMBOLS} map
	 * (expected to be a {@link java.util.Properties} instance). If a corresponding
	 * entry is found, its value is used as the symbol representation; otherwise,
	 * {@code tokenName} itself is used as a fallback.
	 * </p>
	 *
	 * <p>
	 * Comma-separated symbols in the resolved value are joined using
	 * {@code " أو"} (Arabic for “or”).
	 * </p>
	 *
	 * <p>
	 * If {@code ln} is {@code true}, the result is formatted as a bullet point
	 * prefixed with a line break.
	 * </p>
	 *
	 * @param tokenName the name of the token to look up
	 * @param ln        whether to format the result as a bullet point on its own line
	 * @return a formatted symbol string; never {@code null}
	 */
	public static String getFormattedTokenSymbols(String tokenName, boolean ln) {
		String tokenSymbols = Objects.isNull(TOKENS_SYMBOLS) ? tokenName : TOKENS_SYMBOLS.getProperty(tokenName);
		return tokenSymbols == null ? tokenName : (ln ? """
														- %s
														""" : "%s").formatted(tokenSymbols.replaceAll(",", " أو"));
	}

	/**
	 * Returns the display name of a token using the given ANTLR vocabulary.
	 *
	 * @param node       the terminal node (token) to analyze
	 * @param vocabulary the ANTLR vocabulary (usually from the lexer)
	 * @return the display name of the token (e.g., '+', 'IDENTIFIER', etc.)
	 * @throws NaftahBugError if the node or token is null
	 */
	public static String getDisplayName(ParseTree node, Vocabulary vocabulary) {
		if (node instanceof TerminalNode terminal && terminal.getSymbol() != null) {
			int type = terminal.getSymbol().getType();
			return vocabulary.getSymbolicName(type);
		}
		throw new NaftahBugError("الرمز (token) غير صالح أو مفقود.");

	}

	/**
	 * Creates a declared variable instance from the parser context.
	 *
	 * @param depth        the depth of context where declared
	 * @param ctx          The declaration context.
	 * @param variableName The variable name.
	 * @param isConstant   True if the variable is constant.
	 * @param type         the variable type, default {@code JavaType::ofObject}.
	 * @return A pair containing the declared variable and a boolean flag.
	 */
	public static Pair<DeclaredVariable, Boolean> createDeclaredVariable(   int depth,
																			ParserRuleContext ctx,
																			String variableName,
																			boolean isConstant,
																			JavaType type) {
		return MutablePair
				.of(DeclaredVariable
						.of(depth,
							ctx,
							variableName,
							isConstant,
							type,
							null), true);
	}

	/**
	 * Handles the declaration of a variable or constant in the current execution context.
	 *
	 * <p>This method manages both standard variable/constant declarations and
	 * special cases like object field declarations. It also performs type validation
	 * when creating objects with a specific type.</p>
	 *
	 * <p>Behavior:</p>
	 * <ul>
	 * <li>If the declaration includes a constant, variable, type, or is part of
	 * an object field, a new {@link DeclaredVariable} is created.</li>
	 * <li>If the context is inside an object creation and a type is specified,
	 * it ensures that the object type is compatible with all types (i.e., Object.class).</li>
	 * <li>If the variable is not part of an object field, it is registered in the
	 * {@link DefaultContext}.</li>
	 * <li>If none of the declaration flags are set, it checks for a previously
	 * declared variable with the same name and reuses it if found. Otherwise,
	 * it creates a generic variable of type {@link Object}.</li>
	 * </ul>
	 *
	 * <p>The method returns either the {@link DeclaredVariable} or a pair containing
	 * it and a boolean flag if the context is parsing an assignment.</p>
	 *
	 * @param currentContext the execution context where the variable is declared
	 * @param ctx            the parser context corresponding to the variable declaration
	 * @param variableName   the name of the variable to declare or retrieve
	 * @param hasConstant    true if the declaration includes a constant modifier
	 * @param hasVariable    true if the declaration includes a variable modifier
	 * @param hasType        true if the declaration specifies a type
	 * @param type           the {@link JavaType} representing the type of the variable, if any
	 * @return a {@link DeclaredVariable} object, or a {@link Pair} of the variable and a boolean if parsing an
	 *         * assignment
	 * @throws NaftahBugError if an invalid type is specified for an object creation context
	 */
	public static Object handleDeclaration( DefaultContext currentContext,
											ParserRuleContext ctx,
											String variableName,
											boolean hasConstant,
											boolean hasVariable,
											boolean hasType,
											JavaType type) {
		Pair<DeclaredVariable, Boolean> declaredVariable;
		boolean creatingObjectField = hasAnyParentOfType(   ctx,
															org.daiitech.naftah.parser.NaftahParser.ObjectContext.class);
		if (hasConstant || hasVariable || hasType || creatingObjectField) {
			declaredVariable = createDeclaredVariable(  currentContext.depth,
														ctx,
														variableName,
														hasConstant,
														type);
			if (!creatingObjectField) {
				currentContext.defineVariable(variableName, declaredVariable.getLeft());
			}
		}
		else {
			declaredVariable = Optional
					.ofNullable(currentContext.getVariable(variableName, true))
					.<Pair<DeclaredVariable, Boolean>>map(alreadyDeclaredVariable -> MutablePair
							.of(
								alreadyDeclaredVariable.getRight(),
								true))
					.orElse(createDeclaredVariable( currentContext.depth,
													ctx,
													variableName,
													false,
													JavaType.ofObject()));
		}
		return currentContext.isParsingAssignment() ? declaredVariable : declaredVariable.getLeft();
	}

	/**
	 * Validates whether a variable with the given name already exists in the current context.
	 * <p>
	 * Checks both local variables in the current context and any broader variables in scope.
	 * If a variable with the same name exists, a NaftahBugError is thrown.
	 *
	 * @param currentContext The current scope or context to check for variable existence.
	 * @param name           The name of the variable to validate.
	 * @throws NaftahBugError if a variable with the same name already exists in the context.
	 */
	public static void validateVariableExistence(   DefaultContext currentContext,
													String name) {
		if (!currentContext.isCreatingObject() && (currentContext
				.containsLocalVariable( currentContext.variables.get(),
										name) || currentContext
												.containsVariable(name, currentContext.depth))) {
			throw newNaftahBugExistentVariableError(name);
		}
	}

	/**
	 * Checks if the given result object is a loop signal and returns the corresponding signal.
	 *
	 * @param result The result object to check.
	 * @return The detected loop signal, or LoopSignal.NONE if none.
	 */
	public static LoopSignal checkLoopSignal(Object result) {
		if (result instanceof LoopSignal.LoopSignalDetails loopSignalDetails) {
			return loopSignalDetails.signal();
		}
		else {
			return LoopSignal.NONE;
		}
	}

	/**
	 * Checks if the given parse tree node is inside a loop construct.
	 *
	 * @param ctx The parse tree node to check.
	 * @return True if inside a loop, false otherwise.
	 */
	public static boolean checkInsideLoop(ParseTree ctx) {
		return hasAnyParentOfType(  ctx,
									List
											.of(org.daiitech.naftah.parser.NaftahParser.ForStatementContext.class,
												org.daiitech.naftah.parser.NaftahParser.WhileStatementContext.class,
												org.daiitech.naftah.parser.NaftahParser.RepeatStatementContext.class));
	}

	/**
	 * Determines whether to break out of a loop based on the current context, statement, and result.
	 *
	 * @param currentContext   The current execution context.
	 * @param currentStatement The current parse tree statement.
	 * @param result           The result of the statement execution.
	 * @return True if the loop should break, false otherwise.
	 */
	public static boolean shouldBreakStatementsLoop(DefaultContext currentContext,
													ParseTree currentStatement,
													Object result) {
		return currentContext
				.hasAnyExecutedChildOrSubChildOfType(   currentStatement,
														org.daiitech.naftah.parser.NaftahParser.ReturnStatementStatementContext.class) || (result instanceof LoopSignal.LoopSignalDetails && (currentContext
																.hasAnyExecutedChildOrSubChildOfType(   currentStatement,
																										org.daiitech.naftah.parser.NaftahParser.BreakStatementStatementContext.class) || currentContext
																												.hasAnyExecutedChildOrSubChildOfType(   currentStatement,
																																						org.daiitech.naftah.parser.NaftahParser.ContinueStatementStatementContext.class)));
	}

	/**
	 * Logs detailed debug information about the current {@link ParserRuleContext}
	 * if the logger is configured to log at {@code Level.FINE}.
	 *
	 * <p>This method formats and logs a message containing:
	 * <ul>
	 * <li>The name of the method invoking the debug (as provided in {@code methodName}).</li>
	 * <li>The rule index, text, and payload of the current parse context ({@code ctx}).</li>
	 * </ul>
	 *
	 * <p>Example log message format:
	 * {@code methodName(ruleIndex: contextText : contextPayload)}
	 *
	 * @param methodName the name of the method calling this logger (used for traceability).
	 * @param ctx        the current {@link ParserRuleContext} to extract debugging information from.
	 */
	public static void debugCurrentContextVisit(String methodName, ParserRuleContext ctx) {
		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER
					.fine("%s(%s)"
							.formatted( methodName,
										FORMATTER
												.formatted( ctx.getRuleIndex(),
															ctx.getText(),
															ctx.getPayload())));
		}
	}

	/**
	 * Visits a specific parser rule context by applying a custom {@link TriFunction},
	 * while automatically handling logging, debugging, and execution tracking.
	 *
	 * <p>This method provides a uniform mechanism for visiting ANTLR parser rule contexts
	 * in the Naftah parsing system. It ensures that every context visit is logged and
	 * marked as executed, and that a provided visitor function is safely applied to
	 * the current context.</p>
	 *
	 * <h3>Behavior summary</h3>
	 * <ul>
	 * <li>Logs debugging information for the current rule context (via {@code debugCurrentContextVisit}).</li>
	 * <li>Logs execution entry for the context (via {@code logExecution}).</li>
	 * <li>Applies the provided {@link TriFunction} using the visitor, context, and parser rule.</li>
	 * <li>Marks the context as executed in the {@link DefaultContext} instance.</li>
	 * <li>Casts the visit result to the specified {@code returnType}.</li>
	 * </ul>
	 *
	 * <p>This version provides type safety by allowing the caller to specify
	 * the expected return type.</p>
	 *
	 * @param defaultNaftahParserVisitor the active {@link DefaultNaftahParserVisitor} handling the traversal
	 * @param methodName                 the name of the visitor method being invoked (for logging and debugging)
	 * @param currentContext             the {@link DefaultContext} associated with the current traversal scope
	 * @param ctx                        the current {@link ParserRuleContext} node being visited
	 * @param visitFunction              a {@link TriFunction} that takes the visitor, context, and rule, and returns
	 *                                   a result
	 * @param returnType                 the expected type of the returned value
	 * @param <T>                        the type of the {@link ParserRuleContext} being visited
	 * @param <R>                        the result type returned by the visitor function
	 * @return the result of applying the {@code visitFunction}, cast to the specified return type
	 * @see DefaultNaftahParserVisitor
	 * @see DefaultContext
	 * @see ParserRuleContext
	 * @see TriFunction
	 */
	public static <T extends ParserRuleContext, R> R visitContext(  DefaultNaftahParserVisitor defaultNaftahParserVisitor,
																	String methodName,
																	DefaultContext currentContext,
																	T ctx,
																	TriFunction<DefaultNaftahParserVisitor, DefaultContext, T, Object> visitFunction,
																	Class<R> returnType) {
		debugCurrentContextVisit(methodName, ctx);
		logExecution(ctx);
		var result = visitFunction.apply(defaultNaftahParserVisitor, currentContext, ctx);
		currentContext.markExecuted(ctx); // Mark as executed
		return returnType.cast(result);
	}

	/**
	 * Simplified overload of
	 * {@link #visitContext(DefaultNaftahParserVisitor, String, DefaultContext, ParserRuleContext, TriFunction, Class)}
	 * that defaults to returning an {@link Object}.
	 *
	 * <p>This variant is useful when the expected result type is not known in advance
	 * or when generic type casting is unnecessary.</p>
	 *
	 * @param defaultNaftahParserVisitor the active {@link DefaultNaftahParserVisitor} handling the traversal
	 * @param methodName                 the name of the visitor method being invoked (for logging and debugging)
	 * @param currentContext             the {@link DefaultContext} associated with the current traversal scope
	 * @param ctx                        the current {@link ParserRuleContext} node being visited
	 * @param visitFunction              a {@link TriFunction} that takes the visitor, context, and rule, and returns
	 *                                   a result
	 * @param <T>                        the type of the {@link ParserRuleContext} being visited
	 * @return the result of applying the {@code visitFunction}, as a generic {@link Object}
	 * @see #visitContext(DefaultNaftahParserVisitor, String, DefaultContext, ParserRuleContext, TriFunction, Class)
	 * @see DefaultNaftahParserVisitor
	 * @see DefaultContext
	 */
	public static <T extends ParserRuleContext> Object visitContext(DefaultNaftahParserVisitor defaultNaftahParserVisitor,
																	String methodName,
																	DefaultContext currentContext,
																	T ctx,
																	TriFunction<DefaultNaftahParserVisitor, DefaultContext, T, Object> visitFunction) {
		return visitContext(defaultNaftahParserVisitor, methodName, currentContext, ctx, visitFunction, Object.class);
	}

	/**
	 * Supplies the root {@link DefaultContext} for visiting a {@code Program} parse tree node.
	 * <p>
	 * Chooses between REPL and standard execution modes based on a system property
	 * and the presence of any {@code FunctionCallStatement} within the parse tree.
	 *
	 * @param ctx the root {@code ProgramContext} from the parse tree
	 * @return the initialized root context (REPL or standard)
	 */
	public static DefaultContext getRootContext(org.daiitech.naftah.parser.NaftahParser.ProgramContext ctx) {
		if (Boolean.getBoolean(INSIDE_REPL_PROPERTY)) {
			return hasChildOrSubChildOfType(ctx,
											org.daiitech.naftah.parser.NaftahParser.FunctionCallExpressionContext.class) ?
													REPLContext.registerContext(new HashMap<>(), new HashMap<>()) :
													REPLContext.registerContext();
		}
		else {
			return hasChildOrSubChildOfType(ctx,
											org.daiitech.naftah.parser.NaftahParser.FunctionCallExpressionContext.class) ?
													registerContext(new HashMap<>(), new HashMap<>()) :
													registerContext();
		}
	}

	/**
	 * Supplies or updates the {@link DefaultContext} for a {@code Block} node.
	 * <p>
	 * Decides REPL or standard context registration based on the REPL mode
	 * and whether the block contains function call statements or expressions.
	 *
	 * @param ctx            the block context in the parse tree
	 * @param currentContext the currently active execution context
	 * @return a new or updated execution context
	 */
	public static DefaultContext getBlockContext(   org.daiitech.naftah.parser.NaftahParser.BlockContext ctx,
													DefaultContext currentContext) {
		if (Boolean.getBoolean(INSIDE_REPL_PROPERTY)) {
			return hasChildOrSubChildOfType(ctx,
											org.daiitech.naftah.parser.NaftahParser.FunctionCallExpressionContext.class) ?
													REPLContext
															.registerContext(   currentContext,
																				new HashMap<>(),
																				new HashMap<>(),
																				new HashMap<>()) :
													REPLContext.registerContext(currentContext, new HashMap<>());

		}
		else {
			return hasChildOrSubChildOfType(ctx,
											org.daiitech.naftah.parser.NaftahParser.FunctionCallExpressionContext.class) ?
													registerContext(currentContext,
																	new HashMap<>(),
																	new HashMap<>(),
																	new HashMap<>()) :
													registerContext(currentContext,
																	new HashMap<>());
		}
	}

	/**
	 * Sets loop variables in the {@link DefaultContext} for a Naftah `foreach` iteration.
	 *
	 * <p>This method inspects the type of the {@code foreachTargetClass} to determine
	 * how to assign values from the {@code targetValues} tuple to the corresponding
	 * loop variables in the {@code currentContext}.</p>
	 *
	 * <p>Supported target contexts and their behavior:</p>
	 * <ul>
	 * <li>{@link org.daiitech.naftah.parser.NaftahParser.ValueForeachTargetContext}:
	 * Only a value variable is set.</li>
	 * <li>{@link org.daiitech.naftah.parser.NaftahParser.KeyValueForeachTargetContext}:
	 * Both key and value variables are set.</li>
	 * <li>{@link org.daiitech.naftah.parser.NaftahParser.IndexAndValueForeachTargetContext}:
	 * Index and value variables are set.</li>
	 * <li>{@link org.daiitech.naftah.parser.NaftahParser.IndexAndKeyValueForeachTargetContext}:
	 * Index, key, and value variables are all set.</li>
	 * </ul>
	 *
	 * <p>The mapping from {@code variableNames} to {@code targetValues} is as follows:</p>
	 * <ul>
	 * <li>For value-only iteration: {@code variableNames[0]} → {@code targetValues[1]}</li>
	 * <li>For key-value iteration: {@code variableNames[0]} → key, {@code variableNames[1]} → value</li>
	 * <li>For index-value iteration: {@code variableNames[0]} → index, {@code variableNames[1]} → value</li>
	 * <li>For index-key-value iteration: {@code variableNames[0]} → index, {@code variableNames[1]} → key,
	 * * {@code variableNames[2]} → value</li>
	 * </ul>
	 *
	 * @param currentContext     the execution context in which loop variables will be set
	 * @param foreachTargetClass the class type of the parsed `foreach` target, determines how variables are mapped
	 * @param variableNames      a {@link NTuple} of loop variable names extracted from the `foreach` declaration
	 * @param targetValues       a {@link NTuple} of values to assign to the loop variables
	 */
	public static void setForeachVariables( DefaultContext currentContext,
											Class<? extends org.daiitech.naftah.parser.NaftahParser.ForeachTargetContext> foreachTargetClass,
											NTuple variableNames,
											NTuple targetValues) {
		if (org.daiitech.naftah.parser.NaftahParser.ValueForeachTargetContext.class
				.isAssignableFrom(foreachTargetClass)) {
			String valueVar = (String) variableNames.get(0);
			var value = targetValues.get(1);
			currentContext.setLoopVariable(valueVar, value);
		}
		else if (org.daiitech.naftah.parser.NaftahParser.KeyValueForeachTargetContext.class
				.isAssignableFrom(
									foreachTargetClass)) {
										String keyVar = (String) variableNames.get(0);
										var key = targetValues.get(1);
										currentContext.setLoopVariable(keyVar, key);
										String valueVar = (String) variableNames.get(1);
										var value = targetValues.get(2);
										currentContext.setLoopVariable(valueVar, value);
									}
		else if (org.daiitech.naftah.parser.NaftahParser.IndexAndValueForeachTargetContext.class
				.isAssignableFrom(
									foreachTargetClass)) {
										String indexVar = (String) variableNames.get(0);
										var index = targetValues.get(0);
										currentContext.setLoopVariable(indexVar, index);
										String valueVar = (String) variableNames.get(1);
										var value = targetValues.get(1);
										currentContext.setLoopVariable(valueVar, value);
									}
		else if (org.daiitech.naftah.parser.NaftahParser.IndexAndKeyValueForeachTargetContext.class
				.isAssignableFrom(
									foreachTargetClass)) {
										String indexVar = (String) variableNames.get(0);
										var index = targetValues.get(0);
										currentContext.setLoopVariable(indexVar, index);
										String keyVar = (String) variableNames.get(1);
										var key = targetValues.get(1);
										currentContext.setLoopVariable(keyVar, key);
										String valueVar = (String) variableNames.get(2);
										var value = targetValues.get(2);
										currentContext.setLoopVariable(valueVar, value);
									}
	}

	/**
	 * Resolves a qualified variable name (with optional safe chaining) to its corresponding value
	 * from the current execution context.
	 * <p>
	 * The qualified name uses colon-separated (`:`) segments to represent nested access into maps.
	 * For example:
	 * <pre>{@code
	 * user:profile:name
	 * }</pre>
	 * would attempt to retrieve the value of {@code name} from {@code profile}, which is inside {@code user}.
	 *
	 * <p>Supports optional chaining via the Arabic question mark suffix {@code "؟"} on any segment.
	 * If optional chaining is specified and any part of the chain is missing, the method returns
	 * {@code None.get()} instead of throwing an exception.
	 *
	 * <p>Examples:
	 * <ul>
	 * <li>{@code user:profile:name} – standard access</li>
	 * <li>{@code user:profile؟:name} – optional chaining on {@code profile}</li>
	 * <li>{@code user؟:profile؟:name} – optional chaining on both {@code user} and {@code profile}</li>
	 * </ul>
	 *
	 * @param qualifiedName  the colon-separated variable access path, with optional {@code "؟"} suffixes
	 * @param currentContext the current execution context used to resolve variables
	 * @param line           source line number (used for error reporting)
	 * @param column         source column number (used for error reporting)
	 * @return the resolved {@code DeclaredVariable} if found, or {@code None.get()} if not found with safe chaining
	 * @throws NaftahBugError if a variable in the access chain is not found and safe chaining is not enabled
	 */
	public static Object accessObjectUsingQualifiedName(String qualifiedName,
														DefaultContext currentContext,
														int line,
														int column) {
		Object result = None.get();
		var accessArray = qualifiedName.split(QUALIFIED_NAME_SEPARATOR);
		boolean[] optional = new boolean[accessArray.length - 1];

		if (accessArray[0].endsWith("؟")) {
			optional[0] = true;
			accessArray[0] = accessArray[0].substring(0, accessArray[0].length() - 1);
		}

		boolean found = false;
		boolean safeChaining = false;
		if (accessArray.length > 1 && getVariable(accessArray[0], currentContext)
				.get() instanceof NaftahObject naftahObject) {
			int i = 1;

			if (naftahObject.fromJava()) {
				var javaObject = naftahObject.get(true);
				for (; i < accessArray.length; i++) {
					if (i < accessArray.length - 1) {

						if (accessArray[i].endsWith("؟")) {
							optional[i] = true;
							accessArray[i] = accessArray[i]
									.substring(0, accessArray[i].length() - 1);
						}

						Object objectField = getObjectField(currentContext, javaObject, accessArray[i], line, column);
						if (Objects.isNull(objectField)) {
							safeChaining = IntStream
									.range(0, optional.length)
									.mapToObj(index -> optional[index])
									.allMatch(Boolean.TRUE::equals);
							found = false;
							break;
						}
						else {
							found = true;
							javaObject = objectField;
						}
					}
					else if (Objects.nonNull(javaObject)) {
						Object objectField = getObjectField(currentContext, javaObject, accessArray[i], line, column);
						if (Objects.nonNull(objectField)) {
							found = true;
							result = objectField;
						}
						else {
							found = false;
						}
					}
					else {
						safeChaining = IntStream
								.range(0, optional.length)
								.mapToObj(index -> optional[index])
								.allMatch(Boolean.TRUE::equals);
					}
				}
			}
			else {
				//noinspection unchecked
				var object = (Map<String, DeclaredVariable>) naftahObject.get();
				for (; i < accessArray.length; i++) {
					if (i < accessArray.length - 1) {

						if (accessArray[i].endsWith("؟")) {
							optional[i] = true;
							accessArray[i] = accessArray[i]
									.substring(0, accessArray[i].length() - 1);
						}

						DeclaredVariable declaredVariable = object.get(accessArray[i]);
						if (Objects.isNull(declaredVariable)) {
							safeChaining = IntStream
									.range(0, optional.length)
									.mapToObj(index -> optional[index])
									.allMatch(Boolean.TRUE::equals);
							found = false;
							break;
						}
						else {
							found = true;
							//noinspection unchecked
							object = (Map<String, DeclaredVariable>) ((NaftahObject) declaredVariable
									.getValue()).get();
						}
					}
					else if (Objects.nonNull(object)) {
						DeclaredVariable declaredVariable = object.get(accessArray[i]);
						if (Objects.nonNull(declaredVariable)) {
							found = true;
							result = declaredVariable;
						}
						else {
							found = false;
						}
					}
					else {
						safeChaining = IntStream
								.range(0, optional.length)
								.mapToObj(index -> optional[index])
								.allMatch(Boolean.TRUE::equals);
					}
				}
			}

			if (!found) {
				if (safeChaining) {
					result = None.get();
				}
				else {
					int finalI = i;
					String traversedQualifiedName = IntStream
							.range(0, accessArray.length)
							.filter(index -> index <= finalI)
							.mapToObj(index -> accessArray[index] + (index < optional.length ?
									(optional[index] ?
											"؟" :
											"") :
									""))
							.collect(Collectors.joining(QUALIFIED_NAME_SEPARATOR));
					throw newNaftahBugVariableNotFoundError(traversedQualifiedName, line, column);
				}
			}
		}
		else {
			result = getVariable(accessArray[0], currentContext).get();
		}
		return result instanceof DeclaredVariable declaredVariable ? declaredVariable.getValue() : result;
	}

	/**
	 * Sets the value of a variable or nested property based on a qualified name string.
	 * <p>
	 * The qualified name is a colon-separated path (e.g., {@code user:profile:name}) that
	 * may represent a deeply nested structure, where each intermediate level is a {@code Map<String,
	 * DeclaredVariable>}.
	 * <br>
	 * The method supports optional safe-chaining by appending the Arabic question mark character ({@code ؟})
	 * to any segment (e.g., {@code user؟:profile؟:name}) to avoid exceptions if intermediate keys are missing.
	 * </p>
	 *
	 * <h3>Behavior:</h3>
	 * <ul>
	 * <li>If the qualified name has only one part, the method sets the top-level variable.</li>
	 * <li>If the qualified name represents a nested structure, the method traverses the map hierarchy,
	 * updating the final declared variable's value if found.</li>
	 * <li>If any non-optional segment is missing, it throws a {@code NaftahBugError} using {@code
	 *   newNaftahBugVariableNotFoundError()}.</li>
	 * </ul>
	 *
	 * @param qualifiedName  the colon-separated qualified name (e.g., {@code user:address؟:city})
	 * @param currentContext the current evaluation context that holds top-level variables
	 * @param newValue       the new value to assign to the final declared variable
	 * @param line           source line number (used for error reporting)
	 * @param column         source column number (used for error reporting)
	 * @return the top-level {@link DeclaredVariable} (even if nested values were updated)
	 * @throws NaftahBugError if a non-optional intermediate or final variable is not found
	 */
	public static Object setObjectUsingQualifiedName(   String qualifiedName,
														DefaultContext currentContext,
														Object newValue,
														int line,
														int column) {
		var accessArray = qualifiedName.split(QUALIFIED_NAME_SEPARATOR);
		boolean[] optional = new boolean[accessArray.length - 1];

		if (accessArray[0].endsWith("؟")) {
			optional[0] = true;
			accessArray[0] = accessArray[0].substring(0, accessArray[0].length() - 1);
		}

		boolean found = false;
		boolean safeChaining = false;

		DeclaredVariable objectVariable = currentContext.getVariable(accessArray[0], false).getRight();

		if (accessArray.length > 1 && objectVariable.getValue() instanceof NaftahObject naftahObject) {
			int i = 1;

			if (naftahObject.fromJava()) {
				var javaObject = naftahObject.get(true);
				for (; i < accessArray.length; i++) {
					if (i < accessArray.length - 1) {

						if (accessArray[i].endsWith("؟")) {
							optional[i] = true;
							accessArray[i] = accessArray[i]
									.substring(0, accessArray[i].length() - 1);
						}

						Object objectField = getObjectField(currentContext, javaObject, accessArray[i], line, column);
						if (Objects.isNull(objectField)) {
							safeChaining = IntStream
									.range(0, optional.length)
									.mapToObj(index -> optional[index])
									.allMatch(Boolean.TRUE::equals);
							found = false;
							break;
						}
						else {
							found = true;
							javaObject = objectField;
						}
					}
					else if (Objects.nonNull(javaObject)) {
						var field = ObjectAccessUtils.findField(javaObject.getClass(), accessArray[i], true);
						if (Objects.nonNull(field)) {
							found = true;
							var fieldValue = convertArgument(newValue, field.getType(), field.getGenericType(), false);
							setObjectField(currentContext, javaObject, accessArray[i], fieldValue, line, column);

							var afterUpdateFieldValue = accessObjectUsingQualifiedName( qualifiedName,
																						currentContext,
																						line,
																						column);

							if (!ObjectUtils.equals(fieldValue, afterUpdateFieldValue, true)) {
								throw ExceptionUtils
										.newNaftahSettingConstantError( qualifiedName,
																		newIllegalFieldAccessException(field
																				.getName()));
							}

						}
						else {
							found = false;
						}
					}
					else {
						safeChaining = IntStream
								.range(0, optional.length)
								.mapToObj(index -> optional[index])
								.allMatch(Boolean.TRUE::equals);
					}
				}
			}
			else {
				//noinspection unchecked
				var object = (Map<String, DeclaredVariable>) naftahObject.get();
				for (; i < accessArray.length; i++) {
					if (i < accessArray.length - 1) {

						if (accessArray[i].endsWith("؟")) {
							optional[i] = true;
							accessArray[i] = accessArray[i]
									.substring(0, accessArray[i].length() - 1);
						}

						DeclaredVariable declaredVariable = object.get(accessArray[i]);
						if (Objects.isNull(declaredVariable)) {
							safeChaining = IntStream
									.range(0, optional.length)
									.mapToObj(index -> optional[index])
									.allMatch(Boolean.TRUE::equals);
							found = false;
							break;
						}
						else {
							found = true;
							//noinspection unchecked
							object = (Map<String, DeclaredVariable>) ((NaftahObject) declaredVariable
									.getValue()).get();
						}
					}
					else if (Objects.nonNull(object)) {
						DeclaredVariable declaredVariable = object.get(accessArray[i]);
						if (Objects.nonNull(declaredVariable)) {
							found = true;
							declaredVariable.setValue(newValue);
						}
						else {
							found = false;
						}
					}
					else {
						safeChaining = IntStream
								.range(0, optional.length)
								.mapToObj(index -> optional[index])
								.allMatch(Boolean.TRUE::equals);
					}
				}
			}

			if (!found && !safeChaining) {
				int finalI = i;
				String traversedQualifiedName = IntStream
						.range(0, accessArray.length)
						.filter(index -> index <= finalI)
						.mapToObj(index -> accessArray[index] + (index < optional.length ?
								(optional[index] ?
										"؟" :
										"") :
								""))
						.collect(Collectors.joining(QUALIFIED_NAME_SEPARATOR));
				throw newNaftahBugVariableNotFoundError(traversedQualifiedName, line, column);
			}
		}
		else {
			objectVariable.setValue(newValue);
		}
		return objectVariable;
	}

	/**
	 * Converts a function-like object into a detailed string representation for debugging or logging purposes.
	 *
	 * <p>This utility handles multiple kinds of function representations:
	 * <ul>
	 * <li>{@link BuiltinFunction} — returns the detailed string via {@link BuiltinFunction#toDetailedString()}.</li>
	 * <li>{@link JvmFunction} — returns the detailed string via {@link JvmFunction#toDetailedString()}.</li>
	 * <li>{@link JvmClassInitializer} — returns the detailed string via
	 * {@link JvmClassInitializer#toDetailedString()}.</li>
	 * <li>Other objects — defaults to {@link Object#toString()} if the type is unrecognized.</li>
	 * </ul>
	 *
	 * <p>This method is useful for introspection, debugging, or logging information about
	 * function objects in a consistent and human-readable format.</p>
	 *
	 * @param <T>             the type of the function object
	 * @param currentFunction the function object to convert; may be a built-in, JVM function, JVM class initializer,
	 *                        or any other object
	 * @return a detailed string representation of the function, or the default {@link Object#toString()} for unknown
	 *         types
	 */
	public static <T> String FunctionToString(T currentFunction) {
		if (currentFunction instanceof BuiltinFunction builtinFunction) {
			return builtinFunction.toDetailedString();
		}
		else if (currentFunction instanceof JvmFunction jvmFunction) {
			return jvmFunction.toDetailedString();
		}
		else if (currentFunction instanceof JvmClassInitializer jvmClassInitializer) {
			return jvmClassInitializer.toDetailedString();
		}
		else {
			return currentFunction.toString();
		}
	}

	/**
	 * Retrieves a function (or executable) from a collection by its index.
	 *
	 * <p>This method works with any collection of {@link JvmExecutable} objects
	 * and efficiently handles both {@link List} and generic {@link Collection} implementations:
	 * <ul>
	 * <li>If the collection is a {@link List}, the function is accessed directly by index.</li>
	 * <li>If the collection is a generic {@link Collection}, the method iterates to the
	 * specified index using {@link org.daiitech.naftah.builtin.utils.CollectionUtils#getElementAt(Collection, int)}
	 * .</li>
	 * </ul>
	 *
	 * <p>The function index is provided as a {@link Number} and converted to an integer internally.
	 *
	 * @param <T>           the type of function, extending {@link JvmExecutable}
	 * @param functions     the collection of function objects
	 * @param functionIndex the index of the function to retrieve (as a {@link Number})
	 * @return the function object at the specified index
	 * @throws IndexOutOfBoundsException if the index is out of range
	 */
	public static <T extends JvmExecutable> T getFunction(Collection<T> functions, Number functionIndex) {
		//noinspection unchecked
		return functions instanceof List<T> list ?
				list.get(functionIndex.intValue()) :
				(T) getElementAt(functions, functionIndex.intValue());
	}

	/**
	 * Dynamically finds and invokes a function by name from a collection of {@link JvmExecutable} objects.
	 *
	 * <p>This method performs the following steps:
	 * <ol>
	 * <li>Finds the best matching executable using
	 * {@link org.daiitech.naftah.utils.reflect.InvocationUtils#findBestExecutable} based on the provided
	 * arguments.</li>
	 * <li>If the selected function is a non-static {@link JvmFunction}, the first argument is treated as the
	 * instance.</li>
	 * <li>Delegates the actual invocation to
	 * {@link #invokeFunction(String, boolean, JvmExecutable, Object[], List, Object, int, int)}.</li>
	 * </ol>
	 *
	 * @param functionName    the name of the function to invoke (used for error reporting)
	 * @param forceInvocation true in case of forcing invocation in case the {@link JvmExecutable} is not invocable by
	 *                        default
	 * @param jvmFunctions    the collection of available {@link JvmExecutable} functions
	 * @param naftahArgs      the list of arguments as {@link Pair}, where the first element may be the instance for
	 *                        non-static methods
	 * @param line            the source line number for error reporting
	 * @param column          the source column number for error reporting
	 * @return the result of the function invocation
	 * @throws NaftahBugError if no suitable function is found or invocation fails
	 */
	public static Object invokeFunction(String functionName,
										boolean forceInvocation,
										Collection<JvmExecutable> jvmFunctions,
										List<Pair<String, Object>> naftahArgs,
										int line,
										int column) throws NoSuchMethodException {
		var bestMatch = findBestExecutable(jvmFunctions, new ArrayList<>(naftahArgs), true);

		var selectedFunction = bestMatch.getLeft();

		Object possibleInstance = null;
		if (selectedFunction instanceof JvmFunction jvmFunction && !jvmFunction.isStatic()) {
			possibleInstance = naftahArgs.remove(0).getRight();
		}

		return invokeFunction(  functionName,
								forceInvocation,
								selectedFunction,
								bestMatch.getRight(),
								naftahArgs,
								possibleInstance,
								line,
								column);
	}

	/**
	 * Invokes a specific {@link JvmExecutable}, either a {@link BuiltinFunction} or {@link JvmFunction}.
	 *
	 * <p>This overload delegates to the more general method with {@code executableArgs} set to {@code null}.
	 *
	 * @param functionName     the name of the function for error reporting
	 * @param forceInvocation  true in case of forcing invocation in case the {@link JvmExecutable} is not invocable by
	 *                         default
	 * @param selectedFunction the executable to invoke
	 * @param naftahArgs       the arguments prepared for the executable
	 * @param possibleInstance the instance to invoke on, or {@code null} for static methods
	 * @param line             the source line number for error reporting
	 * @param column           the source column number for error reporting
	 * @return the result of the invocation
	 * @throws NaftahBugError if the function type is unsupported or invocation fails
	 */
	public static Object invokeFunction(
										String functionName,
										boolean forceInvocation,
										JvmExecutable selectedFunction,
										List<Pair<String, Object>> naftahArgs,
										Object possibleInstance,
										int line,
										int column) {
		return invokeFunction(  functionName,
								forceInvocation,
								selectedFunction,
								null,
								naftahArgs,
								possibleInstance,
								line,
								column);
	}

	/**
	 * Invokes a specific {@link JvmExecutable}, either a {@link BuiltinFunction} or {@link JvmFunction}.
	 *
	 * <p>This is the core method handling actual execution:
	 * <ul>
	 * <li>{@link BuiltinFunction} invocation via {@link #invokeBuiltinFunction}</li>
	 * <li>{@link JvmFunction} invocation via {@link #invokeJvmFunction}</li>
	 * <li>Throws {@link NaftahBugError} if the function type is unsupported</li>
	 * </ul>
	 *
	 * @param functionName     the name of the function for error reporting
	 * @param forceInvocation  true in case of forcing invocation in case the {@link JvmExecutable} is not invocable by
	 *                         default
	 * @param selectedFunction the executable to invoke
	 * @param executableArgs   the prepared arguments array for the executable (may be {@code null})
	 * @param naftahArgs       the original list of {@link Pair} arguments
	 * @param possibleInstance the instance to invoke on, or {@code null} for static methods
	 * @param line             the source line number for error reporting
	 * @param column           the source column number for error reporting
	 * @return the result of the invocation
	 * @throws NaftahBugError if the function type is unsupported or invocation fails
	 */
	public static Object invokeFunction(
										String functionName,
										boolean forceInvocation,
										JvmExecutable selectedFunction,
										Object[] executableArgs,
										List<Pair<String, Object>> naftahArgs,
										Object possibleInstance,
										int line,
										int column) {
		Object result;
		if (selectedFunction instanceof BuiltinFunction builtinFunction) {
			result = invokeBuiltinFunction( functionName,
											builtinFunction,
											executableArgs,
											naftahArgs,
											line,
											column);
		}
		else if (selectedFunction instanceof JvmFunction jvmFunction) {
			result = invokeJvmFunction( functionName,
										forceInvocation,
										jvmFunction,
										executableArgs,
										naftahArgs,
										possibleInstance,
										line,
										column);
		}
		else {
			throw newNaftahUnsupportedFunctionError(functionName,
													selectedFunction.getClass(),
													line,
													column);
		}
		return result;
	}

	/**
	 * Invokes a declared function within the current execution context, supporting
	 * both synchronous and asynchronous execution models.
	 *
	 * <p>This method prepares the function’s parameters, binds argument values,
	 * updates the execution context, and then invokes the function body using the
	 * provided {@link DefaultNaftahParserVisitor}. If the function is declared as
	 * asynchronous, its body is executed inside a spawned {@link Task} and the
	 * task object is returned immediately. For synchronous functions, the body is
	 * executed directly and the returned value becomes the result.</p>
	 *
	 * <p>All function calls are recorded in the context’s call stack using
	 * {@link DefaultContext#pushCall}, and removed afterward via
	 * {@link DefaultContext#popCall}, ensuring proper debugging and stack-trace
	 * fidelity. For asynchronous functions, thread-local cleanup is performed
	 * after execution via {@code currentContext.cleanThreadLocals()}.</p>
	 *
	 * @param depth                      the current recursion or call depth, used to generate a unique call ID
	 * @param functionName               the name of the function to invoke
	 * @param declaredFunction           the function to invoke; must not be null
	 * @param defaultNaftahParserVisitor the visitor used to evaluate the function body
	 * @param args                       the list of (name, value) argument pairs to supply to the function
	 * @param currentContext             the active execution context
	 * @return for synchronous functions, the evaluated return value; for asynchronous
	 *         functions, a {@link Task} representing the pending computation
	 * @throws RuntimeException if evaluation of the function body fails
	 */
	public static Object invokeDeclaredFunction(
												int depth,
												String functionName,
												DeclaredFunction<?> declaredFunction,
												DefaultNaftahParserVisitor defaultNaftahParserVisitor,
												List<Pair<String, Object>> args,
												DefaultContext currentContext
	) {
		if (declaredFunction.isAsync()) {
			return spawnTask(   currentContext,
								() -> {
									var ctx = DefaultContext.getCurrentContext();
									String functionCallId = generateCallId(depth, functionName);
									ctx.setFunctionCallId(functionCallId);
									Object result = doInvokeDeclaredFunction(   declaredFunction,
																				defaultNaftahParserVisitor,
																				args,
																				ctx);
									ctx.setFunctionCallId(null);
									return result;
								},
								currentContext::cleanThreadLocals);
		}
		else {
			return doInvokeDeclaredFunction(declaredFunction, defaultNaftahParserVisitor, args, currentContext);
		}
	}

	/**
	 * Executes a declared function within the specified context.
	 * <p>
	 * This method manages the full lifecycle of a function call, including:
	 * </p>
	 * <ul>
	 * <li>Setting the implementation name in the current context.</li>
	 * <li>Preparing the function’s parameter metadata and evaluating argument values.</li>
	 * <li>Binding parameters and arguments into the current context for correct
	 * resolution during body evaluation.</li>
	 * <li>Maintaining the call stack by pushing the function call before evaluation
	 * and popping it afterward.</li>
	 * <li>Invoking the function body via the provided {@link DefaultNaftahParserVisitor}.</li>
	 * <li>Returning {@link None#get()} if the function’s return type is {@link Void},
	 * otherwise returning the evaluated result.</li>
	 * </ul>
	 * <p>
	 * Callers should ensure {@link #prepareDeclaredFunction} has been invoked if any
	 * prerequisite preparation is required for the function.
	 * </p>
	 * <p>
	 * The call stack is updated even if an exception occurs during function execution,
	 * guaranteeing that context state remains consistent. After the function is
	 * popped, the implementation name in the context is restored to the parent call
	 * or cleared if no parent exists.
	 * </p>
	 *
	 * @param declaredFunction           the {@link DeclaredFunction} being executed
	 * @param defaultNaftahParserVisitor the visitor used to evaluate the function body
	 * @param args                       the raw argument name/value pairs supplied to the function
	 * @param currentContext             the {@link DefaultContext} associated with the call
	 * @return the evaluated result of the function body, or {@link None#get()} if
	 *         the function has a {@link Void} return type
	 */
	public static Object doInvokeDeclaredFunction(  DeclaredFunction<?> declaredFunction,
													DefaultNaftahParserVisitor defaultNaftahParserVisitor,
													List<Pair<String, Object>> args,
													DefaultContext currentContext
	) {
		boolean functionInStack = false;
		try {
			currentContext.setImplementationName(declaredFunction.getImplementationName());
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
				currentContext.defineFunctionArguments(finalArgs, declaredFunction.getDepth());
			}

			pushCall(declaredFunction, finalArgs);
			functionInStack = true;
			var result = defaultNaftahParserVisitor.visit(declaredFunction.getBody());
			return declaredFunction.getReturnType().isOfType(Void.class) ? None.get() : result;
		}
		finally {
			if (functionInStack) {
				popCall();
				Triple<DeclaredFunction<?>, Map<String, Object>, Object> parentCall;
				if (Objects.nonNull(parentCall = peekCall())) {
					currentContext.setImplementationName(parentCall.getLeft().getImplementationName());
				}
				else {
					currentContext.setImplementationName(null);
				}
			}
		}
	}

	/**
	 * Invokes a built-in function via reflection with automatic argument conversion.
	 *
	 * <p>This method supports two types of argument inputs:
	 * <ul>
	 * <li>If {@code executableArgs} is provided, it is passed directly to the underlying
	 * {@link java.lang.reflect.Method}.</li>
	 * <li>If {@code naftahArgs} is provided as a {@link List} of {@link Pair} objects (name/value pairs),
	 * each argument is automatically converted to match the parameter type using
	 * {@link org.daiitech.naftah.utils.reflect.InvocationUtils#invokeJvmExecutable}.</li>
	 * </ul>
	 *
	 * <p>Supports conversions for primitives, arrays, collections, and generic types. If conversion fails or the
	 * argument count does not match, detailed runtime errors including line and column are thrown.
	 *
	 * @param functionName    the name of the function for error reporting
	 * @param builtinFunction the {@link BuiltinFunction} to invoke
	 * @param naftahArgs      a list of arguments as {@link Pair} name/value pairs
	 * @param line            the source line number for error reporting
	 * @param column          the source column number for error reporting
	 * @return the result of the function, or {@link None#get()} if the function returns {@code void} or {@code null}
	 * @throws NaftahBugError if argument types/count do not match, invocation fails, or instantiation fails
	 */
	public static Object invokeBuiltinFunction( String functionName,
												BuiltinFunction builtinFunction,
												List<Pair<String, Object>> naftahArgs,
												int line,
												int column) {
		return invokeBuiltinFunction(functionName, builtinFunction, null, naftahArgs, line, column);
	}

	/**
	 * Invokes a built-in function with either pre-prepared argument array or {@link List} of {@link Pair} arguments.
	 *
	 * @param functionName    the name of the function for error reporting
	 * @param builtinFunction the {@link BuiltinFunction} to invoke
	 * @param executableArgs  the pre-prepared argument array (nullable)
	 * @param naftahArgs      a list of arguments as {@link Pair} name/value pairs
	 * @param line            the source line number
	 * @param column          the source column number
	 * @return the result of the function, or {@link None#get()} if {@code void} or {@code null}
	 * @throws NaftahBugError if argument types/count do not match, invocation fails, or instantiation fails
	 */
	public static Object invokeBuiltinFunction( String functionName,
												BuiltinFunction builtinFunction,
												Object[] executableArgs,
												List<Pair<String, Object>> naftahArgs,
												int line,
												int column) {
		try {
			if (Objects.nonNull(executableArgs)) {
				return invokeJvmExecutable( null,
											builtinFunction.getMethod(),
											executableArgs,
											naftahArgs,
											builtinFunction
													.getFunctionInfo()
													.returnType());
			}
			else {
				return invokeJvmExecutable( null,
											builtinFunction.getMethod(),
											naftahArgs,
											builtinFunction
													.getFunctionInfo()
													.returnType(),
											true);
			}
		}
		catch (IllegalArgumentException e) {
			throw newNaftahIllegalArgumentError(functionName,
												builtinFunction
														.getProviderInfo()
														.name(),
												builtinFunction
														.getMethod()
														.getParameterCount(),
												naftahArgs.size(),
												builtinFunction
														.toDetailedString(),
												e,
												line,
												column);
		}
		catch (IllegalAccessException | InvocationTargetException e) {
			throw newNaftahInvocationError( functionName,
											builtinFunction.toDetailedString(),
											e,
											line,
											column);
		}
		catch (InstantiationException e) {
			throw newNaftahInstantiationError(  functionName,
												builtinFunction.toDetailedString(),
												e,
												line,
												column);
		}
	}

	/**
	 * Invokes a JVM function (static or instance) with automatic argument conversion.
	 *
	 * <p>For instance methods, the first argument must be the object instance. Arguments are converted to match
	 * the method parameter types, including primitives, arrays, collections, and generic types.
	 *
	 * <p>The result is wrapped in a {@link NaftahObject}. If the method returns {@code void} or {@code null},
	 * a {@link NaftahObject} representing {@code null} is returned.
	 *
	 * @param functionName     the name of the function for error reporting
	 * @param forceInvocation  true in case of forcing invocation in case the {@link JvmExecutable} is not invocable by
	 *                         default
	 * @param jvmFunction      the {@link JvmFunction} to invoke
	 * @param naftahArgs       a list of arguments as {@link Pair} name/value pairs
	 * @param possibleInstance the instance for instance methods; {@code null} for static methods
	 * @param line             the source line number
	 * @param column           the source column number
	 * @return a {@link NaftahObject} wrapping the result
	 * @throws NaftahBugError if instance is missing, arguments mismatch, invocation fails, instantiation fails, or
	 *                        the function is non-invocable
	 */
	public static Object invokeJvmFunction( String functionName,
											boolean forceInvocation,
											JvmFunction jvmFunction,
											List<Pair<String, Object>> naftahArgs,
											Object possibleInstance,
											int line,
											int column) {
		return invokeJvmFunction(   functionName,
									forceInvocation,
									jvmFunction,
									null,
									naftahArgs,
									possibleInstance,
									line,
									column);
	}

	/**
	 * Invokes a JVM function with either pre-prepared argument array or {@link List} of {@link Pair} arguments.
	 *
	 * @param functionName     the name of the function for error reporting
	 * @param forceInvocation  true in case of forcing invocation in case the {@link JvmExecutable} is not invocable by
	 *                         default
	 * @param jvmFunction      the {@link JvmFunction} to invoke
	 * @param executableArgs   the pre-prepared argument array (nullable)
	 * @param naftahArgs       a list of arguments as {@link Pair} name/value pairs
	 * @param possibleInstance the instance for instance methods; {@code null} for static methods
	 * @param line             the source line number
	 * @param column           the source column number
	 * @return a {@link NaftahObject} wrapping the result
	 * @throws NaftahBugError if instance is missing, arguments mismatch, invocation fails, instantiation fails, or
	 *                        the function is non-invocable
	 */
	public static Object invokeJvmFunction( String functionName,
											boolean forceInvocation,
											JvmFunction jvmFunction,
											Object[] executableArgs,
											List<Pair<String, Object>> naftahArgs,
											Object possibleInstance,
											int line,
											int column) {

		if (forceInvocation || jvmFunction.isInvocable()) {
			if (!jvmFunction.isStatic() && Objects.isNull(naftahArgs)) {
				throw new NaftahBugError(INVALID_INSTANCE_METHOD_CALL_MSG
						.apply( functionName,
								jvmFunction
										.toDetailedString()));
			}

			try {
				Object result;
				if (Objects.nonNull(executableArgs)) {
					result = invokeJvmExecutable(   possibleInstance,
													jvmFunction.getMethod(),
													executableArgs,
													naftahArgs,
													jvmFunction.getMethod().getReturnType());
				}
				else {
					result = invokeJvmExecutable(   possibleInstance,
													jvmFunction.getMethod(),
													naftahArgs,
													jvmFunction.getMethod().getReturnType(),
													false);
				}

				return NaftahObject
						.of(result);
			}
			catch (IllegalArgumentException e) {
				throw newNaftahIllegalArgumentError(functionName,
													ClassUtils.getQualifiedName(jvmFunction.getClazz().getName()),
													jvmFunction
															.getMethod()
															.getParameterCount(),
													naftahArgs.size(),
													jvmFunction.isStatic() ?
															jvmFunction
																	.toDetailedString() :
															jvmFunction
																	.toDetailedString() + NOTE + INVALID_INSTANCE_METHOD_CALL_MSG
																			.apply(functionName, ""),
													e,
													line,
													column);
			}
			catch (IllegalAccessException | InvocationTargetException e) {
				throw newNaftahInvocationError( functionName,
												jvmFunction.isStatic() ?
														jvmFunction
																.toDetailedString() :
														jvmFunction
																.toDetailedString() + NOTE + INVALID_INSTANCE_METHOD_CALL_MSG
																		.apply(functionName, ""),
												e,
												line,
												column);
			}
			catch (InstantiationException e) {
				throw newNaftahInstantiationError(  functionName,
													jvmFunction.toDetailedString(),
													e,
													line,
													column);
			}
		}
		else {
			throw newNaftahNonInvocableFunctionError(   functionName,
														jvmFunction.toDetailedString(),
														line,
														column);
		}
	}

	/**
	 * Invokes a specific JVM class initializer (constructor) with automatic argument conversion.
	 *
	 * <p>Creates a new instance of the class represented by the provided {@link JvmClassInitializer}. Arguments
	 * are automatically converted to match the constructor's parameter types. The resulting instance is wrapped
	 * in a {@link NaftahObject}.
	 *
	 * <p>Reflection exceptions are caught and rethrown as detailed Naftah-specific errors, including the
	 * class name, constructor signature, and source location (line/column).
	 *
	 * @param classInitializerName the name of the class initializer (for error reporting)
	 * @param jvmClassInitializer  the {@link JvmClassInitializer} representing the constructor
	 * @param naftahArgs           a list of arguments as {@link Pair} name/value pairs
	 * @param line                 the source line number
	 * @param column               the source column number
	 * @return a {@link NaftahObject} containing the newly created instance
	 * @throws NaftahBugError if arguments do not match, constructor cannot be accessed, or instantiation fails
	 * @see NaftahObject
	 * @see org.daiitech.naftah.utils.reflect.InvocationUtils#invokeJvmConstructor
	 */
	public static Object invokeJvmClassInitializer( String classInitializerName,
													JvmClassInitializer jvmClassInitializer,
													List<Pair<String, Object>> naftahArgs,
													int line,
													int column) {
		return invokeJvmClassInitializer(   classInitializerName,
											jvmClassInitializer,
											null,
											naftahArgs,
											line,
											column);
	}

	/**
	 * Invokes a JVM class initializer with either pre-prepared argument array or a {@link List} of
	 * {@link Pair} arguments.
	 *
	 * @param classInitializerName the name of the class initializer (for error reporting)
	 * @param jvmClassInitializer  the {@link JvmClassInitializer} representing the constructor
	 * @param executableArgs       the pre-prepared argument array (nullable)
	 * @param naftahArgs           a list of arguments as {@link Pair} name/value pairs
	 * @param line                 the source line number
	 * @param column               the source column number
	 * @return a {@link NaftahObject} containing the newly created instance
	 * @throws NaftahBugError if arguments do not match, constructor cannot be accessed, or instantiation fails
	 */
	public static Object invokeJvmClassInitializer( String classInitializerName,
													JvmClassInitializer jvmClassInitializer,
													Object[] executableArgs,
													List<Pair<String, Object>> naftahArgs,
													int line,
													int column) {
		try {
			Object result;
			var constructor = jvmClassInitializer.getConstructor();
			var returnType = jvmClassInitializer.getClazz();
			if (Objects.nonNull(executableArgs)) {
				result = invokeJvmConstructor(  constructor,
												executableArgs,
												naftahArgs,
												returnType);
			}
			else {
				result = invokeJvmConstructor(
												constructor,
												naftahArgs,
												jvmClassInitializer.getClazz(),
												false);

			}

			return NaftahObject
					.of(result);
		}
		catch (IllegalArgumentException e) {
			throw newNaftahIllegalArgumentError(classInitializerName,
												jvmClassInitializer.getQualifiedName(),
												jvmClassInitializer
														.getConstructor()
														.getParameterCount(),
												naftahArgs.size(),
												jvmClassInitializer
														.toDetailedString(),
												e,
												line,
												column);
		}
		catch (IllegalAccessException | InvocationTargetException e) {
			throw newNaftahInvocationError( classInitializerName,
											jvmClassInitializer
													.toDetailedString(),
											e,
											line,
											column);
		}
		catch (InstantiationException e) {
			throw newNaftahInstantiationError(  classInitializerName,
												jvmClassInitializer.toDetailedString(),
												e,
												line,
												column);
		}
	}

	/**
	 * Selects the best matching JVM class initializer from a collection and invokes it with automatic argument
	 * conversion.
	 *
	 * <p>Evaluates all provided {@link JvmClassInitializer} instances, selects the one whose parameters best
	 * match the provided arguments, and then invokes it using
	 * {@link #invokeJvmClassInitializer(String, JvmClassInitializer, Object[], List, int, int)}.
	 *
	 * @param classInitializerName the name of the class initializer (for error reporting)
	 * @param jvmClassInitializers the collection of candidate {@link JvmClassInitializer} instances
	 * @param naftahArgs           a list of arguments as {@link Pair} name/value pairs
	 * @param line                 the source line number
	 * @param column               the source column number
	 * @return a {@link NaftahObject} containing the newly created instance
	 * @throws NaftahBugError if no suitable constructor is found or invocation fails
	 * @see #invokeJvmClassInitializer(String, JvmClassInitializer, Object[], List, int, int)
	 * @see org.daiitech.naftah.utils.reflect.InvocationUtils#findBestExecutable(Collection, List)
	 */
	public static Object invokeJvmClassInitializer( String classInitializerName,
													List<JvmClassInitializer> jvmClassInitializers,
													List<Pair<String, Object>> naftahArgs,
													int line,
													int column) throws NoSuchMethodException {
		var bestMatch = findBestExecutable(jvmClassInitializers, naftahArgs);
		return invokeJvmClassInitializer(   classInitializerName,
											bestMatch.getLeft(),
											bestMatch.getRight(),
											naftahArgs,
											line,
											column);
	}

	/**
	 * Resolves and executes a function call within a potential chain of calls.
	 *
	 * <p>This method identifies the correct function type from the current context and
	 * invokes it, handling the following cases:</p>
	 *
	 * <ul>
	 * <li>{@link DeclaredFunction} — user-defined functions stored in the current context</li>
	 * <li>{@link BuiltinFunction} — internal built-in language functions</li>
	 * <li>{@link JvmFunction} — Java-reflected methods (static or instance) accessible from Naftah</li>
	 * <li>{@link Collection} — a group of callable functions (overloaded or indexed) where a
	 * numeric index may be required to select the target</li>
	 * </ul>
	 *
	 * <p>The method supports both instance and static JVM functions. For instance methods,
	 * the first argument in {@code args} is treated as the target instance. For collections of
	 * functions, the {@code functionIndex} parameter selects which function to invoke. If omitted,
	 * the method attempts to resolve the best match automatically.</p>
	 *
	 * <p>During execution, a unique function call ID is generated for the current context to
	 * support tracing and debugging.</p>
	 *
	 * @param depth                      the current recursion or call depth, used to generate a unique call ID
	 * @param defaultNaftahParserVisitor the parser visitor driving the execution
	 * @param currentContext             the execution context containing functions, variables, and state
	 * @param functionName               the name of the function to invoke
	 * @param forceInvocation            true in case of forcing invocation in case the {@link JvmExecutable} is not
	 *                                   invocable by default
	 * @param args                       a list of {@code Pair<String, Object>} representing the function arguments
	 * @param functionIndex              an optional numeric index for selecting a function from a collection of
	 *                                   overloaded or indexed functions; may be {@code null}
	 * @param line                       the source line number for error reporting
	 * @param column                     the source column number for error reporting
	 * @return the result of executing the resolved function
	 * @throws NaftahBugError           if the function cannot be invoked or the instance for an instance method is
	 *                                  missing
	 * @throws IllegalArgumentException if argument counts or types do not match the target function
	 * @throws NullPointerException     if required context or arguments are missing
	 * @throws NaftahBugError           if the function object is not invocable
	 * @throws NaftahBugError           if a collection of functions cannot be resolved to a single invocable
	 * @throws NaftahBugError           if no function exists with the given name in the current context
	 *
	 *                                  <p><b>Example usage:</b></p>
	 *                                  <pre>{@code visitFunctionCallInChain(0, visitor, context, "print", List.of(Pair.of("arg", "Hello, world!")), null, 12, 8); }</pre>
	 * @see DeclaredFunction
	 * @see BuiltinFunction
	 * @see JvmFunction
	 * @see Collection
	 */
	public static Object visitFunctionCallInChain(
													int depth,
													DefaultNaftahParserVisitor defaultNaftahParserVisitor,
													DefaultContext currentContext,
													String functionName,
													boolean forceInvocation,
													List<Pair<String, Object>> args,
													Number functionIndex,
													int line,
													int column) {
		Object result;

		String functionCallId = generateCallId(depth, functionName);
		currentContext.setFunctionCallId(functionCallId);

		if (currentContext.containsFunction(functionName, -1)) {
			Object function = currentContext.getFunction(functionName, false).getRight();
			if (function instanceof DeclaredFunction<?> declaredFunction) {
				result = invokeDeclaredFunction(depth,
												functionName,
												declaredFunction,
												defaultNaftahParserVisitor,
												args,
												currentContext);
			}
			else if (function instanceof BuiltinFunction builtinFunction) {
				result = invokeBuiltinFunction( functionName,
												builtinFunction,
												args,
												line,
												column);
			}
			else if (function instanceof JvmFunction jvmFunction) {
				Object possibleInstance = jvmFunction.isStatic() ? null : args.remove(0).getRight();

				result = invokeJvmFunction( functionName,
											forceInvocation,
											jvmFunction,
											args,
											possibleInstance,
											line,
											column);
			}
			else if (function instanceof Collection<?> functions) {
				//noinspection unchecked
				var jvmExecutables = (Collection<JvmExecutable>) functions;
				try {
					if (Objects.nonNull(functionIndex)) {
						var selectedFunction = getFunction(jvmExecutables, functionIndex);

						Object possibleInstance = null;
						if (selectedFunction instanceof JvmFunction jvmFunction && !jvmFunction.isStatic()) {
							possibleInstance = args.remove(0).getRight();
						}

						result = invokeFunction(functionName,
												forceInvocation,
												selectedFunction,
												args,
												possibleInstance,
												line,
												column);
					}
					else {
						result = invokeFunction(functionName,
												forceInvocation,
												jvmExecutables,
												args,
												line,
												column);


					}
				}
				catch (Throwable th) {
					throw newNaftahInvocableListFoundError( functionName,
															jvmExecutables,
															th,
															line,
															column);
				}
			}
			else {
				throw newNaftahUnsupportedFunctionError(functionName,
														function.getClass(),
														line,
														column);
			}
		}
		else {
			throw newNaftahInvocableNotFoundError(  functionName,
													line,
													column);
		}
		currentContext.setFunctionCallId(null);
		return result;
	}

	/**
	 * Retrieves the value of a field from the given target object using context-aware
	 * resolution with default safety behavior.
	 *
	 * <p>This overload is equivalent to calling:
	 * <br>
	 * {@code getObjectField(currentContext, target, fieldName, true, false, line, column)}
	 * </p>
	 *
	 * <p>Behavior defaults:</p>
	 * <ul>
	 * <li><strong>safe = true</strong> — any exceptions encountered during lookup or
	 * reflective access are suppressed and result in a {@code null} value.</li>
	 * <li><strong>failFast = false</strong> — getter resolution errors are ignored and
	 * the method continues searching or falls back to direct reflection.</li>
	 * </ul>
	 *
	 * @param currentContext the evaluation context containing available functions
	 * @param target         the object from which the field value should be retrieved
	 * @param fieldName      the name of the field to access
	 * @param line           source line number (used for error reporting)
	 * @param column         source column number (used for error reporting)
	 * @return the resolved field value, or {@code null} if lookup fails or an error occurs
	 */
	public static Object getObjectField(DefaultContext currentContext,
										Object target,
										String fieldName,
										int line,
										int column) {
		return getObjectField(currentContext, target, fieldName, true, false, line, column);
	}

	/**
	 * Retrieves the value of a field from a target object using context-aware resolution.
	 *
	 * <p>The lookup proceeds in the following order:
	 * <ol>
	 * <li>Generate possible getter names for the field.</li>
	 * <li>For each getter, check whether the {@link DefaultContext} defines
	 * a corresponding function.</li>
	 * <li>If the function is a {@link JvmFunction}, attempt to access the field
	 * using its underlying Java method.</li>
	 * <li>If the function represents multiple overloads, select the best match
	 * via {@code findBestExecutable}. If resolution fails:
	 * <ul>
	 * <li>in <strong>fail-fast</strong> mode a {@link NaftahBugError}
	 * is thrown immediately,</li>
	 * <li>otherwise the error is ignored and resolution continues.</li>
	 * </ul>
	 * </li>
	 * <li>If the function is of an unsupported type:
	 * <ul>
	 * <li>in fail-fast mode a {@link NaftahBugError} is thrown,</li>
	 * <li>otherwise the error is ignored.</li>
	 * </ul>
	 * </li>
	 * <li>If no suitable getter in the context is found or resolution fails,
	 * fall back to direct reflective field access.</li>
	 * </ol>
	 *
	 * <p>Error handling behavior is controlled by two flags:</p>
	 * <ul>
	 * <li><strong>failFast</strong> — if {@code true}, any getter-resolution error or
	 * unsupported function type immediately throws an appropriate {@link NaftahBugError}.</li>
	 * <li><strong>safe</strong> — if {@code true}, <em>any</em> thrown error results in
	 * {@code null} being returned instead of propagating the exception.</li>
	 * </ul>
	 *
	 * @param currentContext the evaluation context containing available functions
	 * @param target         the object from which to retrieve the field value; must not be {@code null}
	 * @param fieldName      the name of the field to retrieve
	 * @param safe           whether to swallow any exceptions and return {@code null}
	 * @param failFast       whether to throw immediately on resolution errors during getter lookup
	 * @param line           source line number (for error reporting)
	 * @param column         source column number (for error reporting)
	 * @return the resolved field value, or {@code null} if not found or if {@code safe} mode suppresses an error
	 * @throws NaftahBugError if fail-fast mode is enabled and:
	 *                        <ul>
	 *                        <li>an unsupported function type is found</li>
	 *                        <li>an ambiguous or invalid overloaded function set is encountered</li>
	 *                        </ul>
	 * @throws Throwable      if an unexpected exception occurs and {@code safe == false}
	 */
	public static Object getObjectField(DefaultContext currentContext,
										Object target,
										String fieldName,
										boolean safe,
										boolean failFast,
										int line,
										int column) {
		Set<String> functionNames = Arrays
				.stream(ObjectAccessUtils.BUILD_GETTERS.apply(fieldName))
				.map(functionName -> ClassUtils
						.getQualifiedCall(  ClassUtils.getQualifiedName(target.getClass().getName()),
											functionName))
				.collect(Collectors.toSet());

		try {
			for (String functionName : functionNames) {

				if (!currentContext.containsFunction(functionName, -1)) {
					continue;
				}

				try {
					Object function = currentContext.getFunction(functionName, false).getRight();
					if (function instanceof JvmFunction jvmFunction) {
						return ObjectAccessUtils.get(target, fieldName, jvmFunction.getMethod(), safe, failFast);
					}
					else if (function instanceof Collection<?> functions) {
						//noinspection unchecked
						var jvmExecutables = (Collection<JvmExecutable>) functions;
						try {
							var bestMatch = findBestExecutable(jvmExecutables, List.of(), true);

							return ObjectAccessUtils
									.get(   target,
											fieldName,
											(Method) bestMatch.getLeft().getExecutable(),
											safe,
											failFast);
						}
						catch (Throwable th) {
							if (failFast) {
								throw newNaftahInvocableListFoundError( functionName,
																		jvmExecutables,
																		th,
																		line,
																		column);
							}
						}
					}
					else {
						if (failFast) {
							throw newNaftahUnsupportedFunctionError(functionName,
																	function.getClass(),
																	line,
																	column);
						}
					}
				}
				catch (Throwable th) {
					if (failFast) {
						throw th;
					}
				}

			}

			return ObjectAccessUtils.get(target, fieldName, null, safe, failFast);
		}
		catch (Throwable th) {
			if (safe) {
				return null;
			}
			else {
				throw th instanceof NaftahBugError naftahBugError ? naftahBugError : new NaftahBugError(th);
			}
		}
	}

	/**
	 * Sets the value of a field on a target object using context-aware resolution with
	 * default safety behavior.
	 *
	 * <p>This is equivalent to calling:
	 * <br>
	 * {@code setObjectField(currentContext, target, fieldName, value, true, false, line, column)}
	 *
	 * @param currentContext the current evaluation context containing available functions
	 * @param target         the object on which to set the field value
	 * @param fieldName      the name of the field to set
	 * @param value          the value to assign to the field
	 * @param line           source line number for error reporting
	 * @param column         source column number for error reporting
	 */
	public static void setObjectField(  DefaultContext currentContext,
										Object target,
										String fieldName,
										Object value,
										int line,
										int column) {
		setObjectField(currentContext, target, fieldName, value, true, false, line, column);
	}

	/**
	 * Sets the value of a field on a target object using context-aware resolution.
	 *
	 * <p>The method proceeds as follows:
	 * <ol>
	 * <li>Constructs the qualified setter name for the target field.</li>
	 * <li>If a matching setter exists in the {@link DefaultContext}:
	 * <ul>
	 * <li>If it is a {@link JvmFunction}, calls it using reflection.</li>
	 * <li>If multiple overloads exist, selects the best match based on the value type.
	 * If resolution fails:
	 * <ul>
	 * <li>in <strong>fail-fast</strong> mode, throws a {@link NaftahBugError} immediately,</li>
	 * <li>otherwise the error is propagated or suppressed depending on {@code safe}.</li>
	 * </ul>
	 * </li>
	 * <li>If the function type is unsupported:
	 * <ul>
	 * <li>in fail-fast mode, throws a {@link NaftahBugError},</li>
	 * <li>otherwise the error is suppressed depending on {@code safe}.</li>
	 * </ul>
	 * </li>
	 * </ul>
	 * </li>
	 * <li>If no setter is found in the context, falls back to direct reflective field access.</li>
	 * </ol>
	 *
	 * <p>Error handling behavior:</p>
	 * <ul>
	 * <li><strong>safe</strong> — if {@code true}, exceptions during resolution or reflective
	 * access are swallowed; otherwise they are thrown.</li>
	 * <li><strong>failFast</strong> — if {@code true}, resolution errors throw immediately without fallback.</li>
	 * </ul>
	 *
	 * @param currentContext the current evaluation context containing available functions
	 * @param target         the object on which to set the field value; must not be {@code null}
	 * @param fieldName      the name of the field to set
	 * @param value          the value to assign to the field
	 * @param safe           whether to swallow exceptions instead of throwing
	 * @param failFast       whether to throw immediately on resolution errors
	 * @param line           source line number for error reporting
	 * @param column         source column number for error reporting
	 * @throws NaftahBugError if an unsupported function type is encountered
	 * @throws NaftahBugError if an ambiguous set of overloaded invocables is found
	 */
	public static void setObjectField(  DefaultContext currentContext,
										Object target,
										String fieldName,
										Object value,
										boolean safe,
										boolean failFast,
										int line,
										int column) {
		String functionName = ClassUtils
				.getQualifiedCall(  ClassUtils.getQualifiedName(target.getClass().getName()),
									ObjectAccessUtils.BUILD_SETTER.apply(fieldName));
		try {
			if (currentContext.containsFunction(functionName, -1)) {
				Object function = currentContext.getFunction(functionName, false).getRight();
				if (function instanceof JvmFunction jvmFunction) {
					ObjectAccessUtils.set(target, fieldName, jvmFunction.getMethod(), value, safe, failFast);
				}
				else if (function instanceof Collection<?> functions) {
					//noinspection unchecked
					var jvmExecutables = (Collection<JvmExecutable>) functions;
					try {
						var bestMatch = findBestExecutable( jvmExecutables,
															List.of(ImmutablePair.of(null, value)),
															true);

						ObjectAccessUtils
								.set(   target,
										fieldName,
										(Method) bestMatch.getLeft().getExecutable(),
										bestMatch.getRight()[0],
										safe,
										failFast);
					}
					catch (Throwable th) {
						throw newNaftahInvocableListFoundError( functionName,
																jvmExecutables,
																th,
																line,
																column);
					}
				}
				else {
					throw newNaftahUnsupportedFunctionError(functionName,
															function.getClass(),
															line,
															column);
				}
			}
			else {
				ObjectAccessUtils.set(target, fieldName, null, value, safe, failFast);
			}
		}
		catch (Throwable th) {
			if (!safe) {
				throw th instanceof NaftahBugError naftahBugError ? naftahBugError : new NaftahBugError(th);
			}
		}
	}

	/**
	 * Visits each expression inside a
	 * * {@link org.daiitech.naftah.parser.NaftahParser.CollectionMultipleElementsContext}
	 * and collects the evaluated results into a list.
	 *
	 * <p>This method iterates over all expression nodes contained in the provided
	 * {@code collectionMultipleElements} context, invokes the given
	 * {@code defaultNaftahParserVisitor} on each one, and stores the returned values
	 * in order inside a newly created {@link java.util.ArrayList}.</p>
	 *
	 * @param defaultNaftahParserVisitor the visitor used to evaluate each expression in the collection
	 * @param collectionMultipleElements the parse-tree context containing multiple expression elements to visit
	 * @return a list of objects representing the evaluated results of each expression
	 */
	public static List<Object> visitCollectionMultipleElements( DefaultNaftahParserVisitor defaultNaftahParserVisitor,
																org.daiitech.naftah.parser.NaftahParser.CollectionMultipleElementsContext collectionMultipleElements) {
		List<Object> elements = new ArrayList<>();

		for (   int i = 0;
				i < collectionMultipleElements
						.expression()
						.size();
				i++) {
			var elementValue = defaultNaftahParserVisitor
					.visit(collectionMultipleElements
							.expression(i));
			elements.add(elementValue);
		}
		return elements;
	}

	/**
	 * Spawns a new asynchronous task within the given execution context.
	 *
	 * <p>This method wraps the provided {@link Supplier} in a {@link Task},
	 * registers it with the current context, executes it asynchronously,
	 * and optionally tracks it in the current scope stack.</p>
	 *
	 * <p>The context's pending task counter is incremented, so that the context
	 * will not be deregistered until all spawned tasks complete.</p>
	 *
	 * @param currentContext the context in which the task should execute
	 * @param supplier       a {@link Supplier} providing the task's computation
	 * @param cleaner        a {@link Runnable} to clean up thread-local state
	 *                       or other resources after the task completes
	 * @return the spawned {@link Task} object representing the asynchronous computation
	 */
	public static Task<Object> spawnTask(
											DefaultContext currentContext,
											Supplier<Object> supplier,
											Runnable cleaner) {
		Task<Object> task = Task.of(currentContext, supplier, cleaner);

		task.spawn();

		return task;
	}

	/**
	 * Resolves the name of an implementation from a {@link org.daiitech.naftah.parser.NaftahParser.SelfOrIdContext}.
	 *
	 * <p>This method determines whether the context explicitly specifies an identifier
	 * or implicitly refers to the current implementation in the execution context.</p>
	 *
	 * <ul>
	 * <li>If {@code selfOrIdContext.ID()} is present, the method returns its text as the implementation name.</li>
	 * <li>If no explicit ID is provided, the method retrieves the implementation name
	 * from the provided {@link DefaultContext}.</li>
	 * <li>If the implementation name cannot be determined (i.e., it is null in the current context),
	 * the method throws a {@link NaftahBugError} with an Arabic message indicating that
	 * {@code self} cannot be used outside an implementation definition.</li>
	 * </ul>
	 *
	 * @param selfOrIdContext the parse context representing either {@code self} or an identifier
	 * @param currentContext  the current {@link DefaultContext} providing execution state
	 * @return the resolved implementation name, either explicitly from the context or from the current execution
	 *         * context
	 * @throws NaftahBugError if {@code self} is used outside a valid implementation context
	 */
	public static String matchImplementationName(   org.daiitech.naftah.parser.NaftahParser.SelfOrIdContext selfOrIdContext,
													DefaultContext currentContext) {
		String result;
		if (hasChild(selfOrIdContext.ID())) {
			result = selfOrIdContext.ID().getText();
		}
		else {
			var implementationName = currentContext.getImplementationName();
			if (Objects.isNull(implementationName)) {
				throw new NaftahBugError(
											"""
											لا يمكن استخدام %s خارج سياق تعريف %s.
											"""
													.formatted(
																getFormattedTokenSymbols(   PARSER_VOCABULARY,
																							org.daiitech.naftah.parser.NaftahLexer.SELF,
																							false),

																getFormattedTokenSymbols(   PARSER_VOCABULARY,
																							org.daiitech.naftah.parser.NaftahLexer.IMPLEMENTATION,
																							false)),
											selfOrIdContext
													.getStart()
													.getLine(),
											selfOrIdContext
													.getStart()
													.getCharPositionInLine()
				);
			}
			result = implementationName;
		}

		return result;
	}

	/**
	 * Validates a snippet of Naftah code without executing it.
	 *
	 * <p>
	 * This method parses the provided script to check for syntax errors or
	 * other issues that would prevent the code from running successfully.
	 * The validation is done in a “dry-run” mode—no code is executed, only parsed.
	 * </p>
	 *
	 * <p>
	 * If the system property defined by {@code DEBUG_PROPERTY} is enabled,
	 * it is temporarily disabled during validation to avoid debug-related side effects,
	 * and restored afterward.
	 * </p>
	 *
	 * @param script the Naftah code to validate
	 * @return {@code true} if the script is syntactically valid; {@code false} if any parsing
	 *         errors or exceptions are encountered
	 */
	public static boolean validateCode(String script) {
		boolean isDebug = Boolean.getBoolean(DEBUG_PROPERTY);

		if (isDebug) {
			System.setProperty(DEBUG_PROPERTY, Boolean.toString(false));
		}

		try {

			var input = getCharStream(false, script);


			var parser = prepareRun(input, new BaseErrorListener());

			parser.program();

			return true;
		}
		catch (Throwable ignored) {
			return false;
		}
		finally {
			if (isDebug) {
				System.setProperty(DEBUG_PROPERTY, String.valueOf(true));
			}
		}
	}

	/**
	 * Checks whether the given object represents a declared variable paired with a boolean flag.
	 * <p>
	 * The method verifies that:
	 * <ul>
	 * <li>The object is an instance of {@link Pair}</li>
	 * <li>The first type parameter of the pair is {@link DeclaredVariable}</li>
	 * <li>The second type parameter of the pair is {@link Boolean}</li>
	 * </ul>
	 *
	 * This is typically used to detect structures of the form:
	 * <pre>
	 * Pair&lt;DeclaredVariable, Boolean&gt;
	 * </pre>
	 * where the boolean value represents an associated flag.
	 *
	 * @param object the object to check
	 * @return {@code true} if the object is a {@code Pair<DeclaredVariable, Boolean>},
	 *         {@code false} otherwise
	 */
	public static boolean isDeclaredVariableWithFlag(Object object) {
		return object instanceof Pair<?, ?> pair && JavaType
				.of(pair)
				.getTypeParameters()
				.get(0)
				.isOfType(DeclaredVariable.class) && JavaType
						.of(pair)
						.getTypeParameters()
						.get(1)
						.isOfType(Boolean.class);
	}
}
