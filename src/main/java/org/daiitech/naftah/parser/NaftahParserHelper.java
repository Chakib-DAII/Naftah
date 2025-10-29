package org.daiitech.naftah.parser;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
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
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.Tree;
import org.daiitech.naftah.builtin.lang.BuiltinFunction;
import org.daiitech.naftah.builtin.lang.DeclaredFunction;
import org.daiitech.naftah.builtin.lang.DeclaredParameter;
import org.daiitech.naftah.builtin.lang.DeclaredVariable;
import org.daiitech.naftah.builtin.lang.JvmFunction;
import org.daiitech.naftah.builtin.lang.NaftahObject;
import org.daiitech.naftah.builtin.lang.None;
import org.daiitech.naftah.builtin.utils.ObjectUtils;
import org.daiitech.naftah.builtin.utils.Tuple;
import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.utils.function.TriFunction;
import org.daiitech.naftah.utils.reflect.ClassUtils;

import com.ibm.icu.text.Normalizer2;

import static org.daiitech.naftah.Naftah.DEBUG_PROPERTY;
import static org.daiitech.naftah.Naftah.INSIDE_REPL_PROPERTY;
import static org.daiitech.naftah.Naftah.STANDARD_EXTENSIONS;
import static org.daiitech.naftah.builtin.utils.CollectionUtils.getElementAt;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.isEmpty;
import static org.daiitech.naftah.errors.ExceptionUtils.INVALID_INSTANCE_METHOD_CALL_MSG;
import static org.daiitech.naftah.errors.ExceptionUtils.NOTE;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugNullInputError;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahIllegalArgumentError;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahInstantiationError;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahInvocationError;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahNoSuchMethodError;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahNonInvocableFunctionError;
import static org.daiitech.naftah.parser.DefaultContext.deregisterContext;
import static org.daiitech.naftah.parser.DefaultContext.getVariable;
import static org.daiitech.naftah.parser.DefaultContext.newNaftahBugVariableNotFoundError;
import static org.daiitech.naftah.parser.DefaultContext.popCall;
import static org.daiitech.naftah.parser.DefaultContext.pushCall;
import static org.daiitech.naftah.parser.DefaultContext.registerContext;
import static org.daiitech.naftah.parser.DefaultNaftahParserVisitor.LOGGER;
import static org.daiitech.naftah.parser.NaftahErrorListener.ERROR_HANDLER_INSTANCE;
import static org.daiitech.naftah.parser.NaftahExecutionLogger.logExecution;
import static org.daiitech.naftah.parser.StringInterpolator.cleanInput;
import static org.daiitech.naftah.utils.ResourceUtils.getJarDirectory;
import static org.daiitech.naftah.utils.ResourceUtils.getProperties;
import static org.daiitech.naftah.utils.ResourceUtils.readFileLines;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.getRawHexBytes;

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
												DeclaredFunction function) {
		if (function.getParameters() == null && hasChild(function.getParametersContext())) {
			function
					.setParameters(
									(List<DeclaredParameter>) visit(naftahParserBaseVisitor,
																	function.getParametersContext()));
		}
		if (function.getReturnType() == null && hasChild(function.getReturnTypeContext())) {
			function.setReturnType(visit(naftahParserBaseVisitor, function.getReturnTypeContext()));
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
			return current.a != null ? Map.entry(i, current) : null;
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
				return Map.entry(param.getName(), argument.b);
			}).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		}
		else {
			Set<String> usedNames = new HashSet<>();
			// arguments that have no names
			Map<Integer, Pair<String, Object>> positionalArguments = IntStream
					.range(0, arguments.size())
					.mapToObj(i -> {
						var current = arguments.get(i);
						return current.a == null ? Map.entry(i, current) : null;
					})
					.filter(Objects::nonNull)
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

			// Assign positional arguments
			for (var entry : positionalArguments.entrySet()) {
				String paramName = parameters.get(entry.getKey()).getName();
				if (namedArguments.containsKey(entry.getKey())) {
					throw new NaftahBugError("تم تحديد الوسيط '%s' موقعياً وبالاسم في آنٍ واحد.".formatted(paramName));
				}
				finalArguments.put(paramName, entry.getValue().b);
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

					finalArguments.put(paramName, entry.getValue().b);
					usedNames.add(paramName);

				}
				else {
					throw new NaftahBugError("الوسيط '%s' لا يتوافق مع أي من المعاملات المحددة." + entry.getValue().a);
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

		for (int i = 0; i < ctx.ID().size(); i++) {
			String id = ctx.ID(i).getText();
			result.get().append(id);

			if (i != ctx.ID().size() - 1) {
				String qualifier = Objects.nonNull(ctx.QUESTION(i)) ?
						ctx.QUESTION(i).getText() + ctx.COLON(i).getText() :
						ctx.COLON(i).getText();
				result.get().append(qualifier);
			}
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

		String id = ctx.ID().getText();
		result.get().append(id);

		for (int i = 0; i < ctx.propertyAccess().size(); i++) {
			String qualifier = Objects.nonNull(ctx.QUESTION(i)) ?
					ctx.QUESTION(i).getText() + ":" :
					":";
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
	 * Prepares a parser instance from the given input character stream with multiple error listeners.
	 *
	 * @param input          The input character stream.
	 * @param errorListeners The list of error listeners to add.
	 * @return The prepared parser instance.
	 */
	public static org.daiitech.naftah.parser.NaftahParser prepareRun(   CharStream input,
																		List<ANTLRErrorListener> errorListeners) {
		// Create a lexer and token stream
		var lexerCommonTokenStreamPair = getCommonTokenStream(input, errorListeners);

		CommonTokenStream tokens = lexerCommonTokenStreamPair.b;

		if (Boolean.getBoolean(DEBUG_PROPERTY)) {
			tokens.fill();
			System.out.println("Tokens:");
			for (Token token : tokens.getTokens()) {
				System.out
						.printf("Token: %-20s Text: %s%n",
								lexerCommonTokenStreamPair.a.getVocabulary().getSymbolicName(token.getType()),
								token.getText());
			}
		}

		// Create a parser
		return getParser(tokens, errorListeners);
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
	 * Creates a parser instance from the given token stream and multiple error listeners.
	 *
	 * @param commonTokenStream The token stream.
	 * @param errorListeners    The list of error listeners.
	 * @return The parser instance.
	 */
	public static org.daiitech.naftah.parser.NaftahParser getParser(CommonTokenStream commonTokenStream,
																	List<ANTLRErrorListener> errorListeners) {
		// Create a parser
		org.daiitech.naftah.parser.NaftahParser parser = new org.daiitech.naftah.parser.NaftahParser(commonTokenStream);
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
		return getCommonTokenStream(charStream, List.of()).b;
	}

	/**
	 * Gets a CommonTokenStream from the given character stream and a single error listener.
	 *
	 * @param charStream    The character stream.
	 * @param errorListener The error listener.
	 * @return The CommonTokenStream.
	 */
	public static CommonTokenStream getCommonTokenStream(CharStream charStream, ANTLRErrorListener errorListener) {
		return getCommonTokenStream(charStream, List.of(errorListener)).b;
	}

	/**
	 * Creates a lexer and CommonTokenStream from the given character stream and error listeners.
	 *
	 * @param charStream     The character stream.
	 * @param errorListeners List of error listeners to add.
	 * @return A pair containing the lexer and token stream.
	 */
	public static Pair<org.daiitech.naftah.parser.NaftahLexer, CommonTokenStream> getCommonTokenStream(
																										CharStream charStream,
																										List<ANTLRErrorListener> errorListeners) {
		// Create a lexer and token stream
		org.daiitech.naftah.parser.NaftahLexer lexer = new org.daiitech.naftah.parser.NaftahLexer(charStream);
		lexer.removeErrorListeners();
		errorListeners.forEach(lexer::addErrorListener);
		return new Pair<>(lexer, new CommonTokenStream(lexer));
	}

	/**
	 * Obtains a CharStream from a script string or script file path.
	 *
	 * @param isScriptFile True if the input string represents a file path, false if it is script content.
	 * @param script       The script content or file path.
	 * @return The CharStream for the script.
	 * @throws Exception If an error occurs reading the file.
	 */
	public static CharStream getCharStream(boolean isScriptFile, String script) throws Exception {
		CharStream charStream;
		if (isScriptFile) {
			// Search for path
			Path filePath = searchForNaftahScriptFile(script).toPath();
			// Read file into a String
			// TODO: this is not needed in windows after rechecking.
			// String content = Files.readString(filePath, StandardCharsets.UTF_8);
			// charStream =
			// CharStreams.fromString(POSSIBLE_SHAPING_FUNCTION.apply(content));
			// TODO: it works like this in windows (maybe Posix systems still need extra
			// fixes, like
			// above)
			charStream = CharStreams.fromPath(filePath, StandardCharsets.UTF_8);
		}
		else {
			// TODO: this is not needed in windows after rechecking.
			// charStream = CharStreams.fromString(POSSIBLE_SHAPING_FUNCTION.apply(script));
			// TODO: it works like this in windows (maybe Posix systems still need extra
			// fixes, like
			// above)
			script = NORMALIZER.normalize(script);
			if (Boolean.getBoolean(DEBUG_PROPERTY)) {
				getRawHexBytes(script);
			}
			charStream = CharStreams.fromString(script);
		}
		return charStream;
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
	 * @param value    The value of the variable.
	 * @return A formatted string representing the declared variable or constant.
	 */
	public static String declaredValueToString(boolean constant, String name, Object value) {
		return "<%s %s = %s>".formatted(constant ? "ثابت" : "متغير", name, Optional.ofNullable(value).map(o -> {
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
	 * Returns a formatted string of token symbols based on the token name.
	 * <p>
	 * This method looks up the token name in a {@code TOKENS_SYMBOLS} map (assumed to be a
	 * {@link java.util.Properties} object). If a match is found, the associated value is used
	 * as the base symbol string. Commas in the value are replaced with " أو" (Arabic "or").
	 * <p>
	 * If {@code ln} is {@code true}, the formatted string will include a line break and bullet point.
	 *
	 * @param tokenName The name of the token.
	 * @param ln        If {@code true}, output is formatted with a line break and bullet.
	 * @return A formatted string representing the token symbols, or {@code null} if not found.
	 */
	public static String getFormattedTokenSymbols(String tokenName, boolean ln) {
		String tokenSymbols = Objects.isNull(TOKENS_SYMBOLS) ? tokenName : TOKENS_SYMBOLS.getProperty(tokenName);
		return tokenSymbols == null ? null : (ln ? """
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
	 * Checks if there is a type mismatch between the given value and the declared type.
	 *
	 * @param value           The value to check.
	 * @param valueType       The class of the value.
	 * @param declarationType The declared type class.
	 * @return True if types mismatch, false otherwise.
	 */
	public static boolean typeMismatch(Object value, Class<?> valueType, Class<?> declarationType) {
		return Objects.nonNull(value) && typeMismatch(valueType, declarationType);
	}

	/**
	 * Checks if there is a type mismatch between two classes.
	 *
	 * @param valueType       The class of the value.
	 * @param declarationType The declared type class.
	 * @return True if types mismatch, false otherwise.
	 */
	public static boolean typeMismatch(Class<?> valueType, Class<?> declarationType) {
		return Objects.nonNull(declarationType) && !Object.class.equals(declarationType) && !Collection.class
				.isAssignableFrom(declarationType) && !Map.class.isAssignableFrom(declarationType) && (((Number.class
						.isAssignableFrom(valueType) && !Number.class
								.isAssignableFrom(declarationType)) || (!Number.class
										.isAssignableFrom(valueType) && Number.class
												.isAssignableFrom(declarationType))) || (!Number.class
														.isAssignableFrom(declarationType) && !Number.class
																.isAssignableFrom(valueType) && !valueType
																		.isAssignableFrom(declarationType)) || Collection.class
																				.isAssignableFrom(valueType) || Map.class
																						.isAssignableFrom(valueType));
	}

	/**
	 * Creates a declared variable instance from the parser context.
	 *
	 * @param naftahParserBaseVisitor The base visitor for the parser.
	 * @param ctx                     The declaration context.
	 * @param variableName            The variable name.
	 * @param isConstant              True if the variable is constant.
	 * @param hasType                 True if the variable has an explicit type.
	 * @return A pair containing the declared variable and a boolean flag.
	 */
	public static Pair<DeclaredVariable, Boolean> createDeclaredVariable(
																			org.daiitech.naftah.parser.NaftahParserBaseVisitor<?> naftahParserBaseVisitor,
																			org.daiitech.naftah.parser.NaftahParser.DeclarationContext ctx,
																			String variableName,
																			boolean isConstant,
																			boolean hasType) {

		return new Pair<>(DeclaredVariable
				.of(ctx,
					variableName,
					isConstant,
					hasType ? (Class<?>) visit(naftahParserBaseVisitor, ctx.type()) : null,
					null), true);
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
	 * Visits a specific parser context by applying a custom {@link TriFunction} and handling
	 * logging and context execution tracking uniformly.
	 *
	 * <p>This method:
	 * <ul>
	 * <li>Logs debugging information about the current rule context</li>
	 * <li>Logs execution entry for the context</li>
	 * <li>Applies the provided visit function using the current visitor and context</li>
	 * <li>Marks the context as executed in the current context object</li>
	 * </ul>
	 *
	 * @param defaultNaftahParserVisitor the visitor instance used to process the parse tree node
	 * @param methodName                 the name of the calling method (for logging/debugging purposes)
	 * @param currentContext             the current {@link DefaultContext} used during traversal
	 * @param ctx                        the current {@link ParserRuleContext} being visited
	 * @param visitFunction              a {@link TriFunction} that processes the visitor, context, and parse rule to
	 *                                   return a result
	 * @param <T>                        a subclass of {@link ParserRuleContext} representing the current rule context
	 * @param <R>                        th return type of the parsed result
	 * @return the result of applying the visit function
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
	}

	/**
	 * Deregisters (removes) a context by its depth level.
	 * <p>
	 * Handles REPL mode or standard context cleanup depending on the system property.
	 *
	 * @param depth the depth level of the context to be deregistered
	 */
	public static void deregisterContextByDepth(int depth) {
		if (Boolean.getBoolean(INSIDE_REPL_PROPERTY)) {
			REPLContext.deregisterContext(depth);
		}
		else {
			deregisterContext(depth);
		}
	}

	public static void setForeachVariables( DefaultContext currentContext,
											Class<? extends org.daiitech.naftah.parser.NaftahParser.ForeachTargetContext> foreachTargetClass,
											Tuple variableNames,
											Tuple targetValues) {
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
	 * @return the resolved {@code DeclaredVariable} if found, or {@code None.get()} if not found with safe chaining
	 * @throws NaftahBugError if a variable in the access chain is not found and safe chaining is not enabled
	 */
	public static Object accessObjectUsingQualifiedName(String qualifiedName, DefaultContext currentContext) {
		Object result = None.get();
		var accessArray = qualifiedName.split(":");
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
			if (!naftahObject.fromJava()) {
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
							.collect(Collectors.joining(":"));
					throw newNaftahBugVariableNotFoundError(traversedQualifiedName);
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
	 * @return the top-level {@link DeclaredVariable} (even if nested values were updated)
	 * @throws NaftahBugError if a non-optional intermediate or final variable is not found
	 */
	public static Object setObjectUsingQualifiedName(   String qualifiedName,
														DefaultContext currentContext,
														Object newValue) {
		var accessArray = qualifiedName.split(":");
		boolean[] optional = new boolean[accessArray.length - 1];

		if (accessArray[0].endsWith("؟")) {
			optional[0] = true;
			accessArray[0] = accessArray[0].substring(0, accessArray[0].length() - 1);
		}

		boolean found = false;
		boolean safeChaining = false;

		DeclaredVariable objectVariable = currentContext.getVariable(accessArray[0], false).b;

		if (accessArray.length > 1 && objectVariable.getValue() instanceof NaftahObject naftahObject) {
			int i = 1;
			if (!naftahObject.fromJava()) {
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
						.collect(Collectors.joining(":"));
				throw newNaftahBugVariableNotFoundError(traversedQualifiedName);
			}
		}
		else {
			objectVariable.setValue(newValue);
		}
		return objectVariable;
	}

	/**
	 * Converts a function object to a detailed string representation.
	 *
	 * @param <T>             the type of the function
	 * @param currentFunction the function object, either {@link BuiltinFunction}, {@link JvmFunction}, or any other
	 *                        object
	 * @return a detailed string representation of the function
	 */
	public static <T> String FunctionToString(T currentFunction) {
		if (currentFunction instanceof BuiltinFunction builtinFunction) {
			return builtinFunction.toDetailedString();
		}
		else if (currentFunction instanceof JvmFunction jvmFunction) {
			return jvmFunction.toDetailedString();
		}
		else {
			return currentFunction.toString();
		}
	}

	/**
	 * Retrieves a function from a collection by index.
	 *
	 * @param functions     the collection of functions
	 * @param functionIndex the index of the desired function
	 * @return the function object at the specified index
	 */
	public static Object getFunction(Collection<?> functions, Number functionIndex) {
		return functions instanceof List<?> list ?
				list.get(functionIndex.intValue()) :
				getElementAt(functions, functionIndex.intValue());
	}

	/**
	 * Invokes a declared function with provided arguments in the given context.
	 *
	 * @param declaredFunction           the declared function to invoke
	 * @param defaultNaftahParserVisitor the visitor used to evaluate the function body
	 * @param args                       the list of argument name/value pairs
	 * @param currentContext             the current execution context
	 * @return the result of invoking the declared function
	 */
	public static Object invokeDeclaredFunction(DeclaredFunction declaredFunction,
												DefaultNaftahParserVisitor defaultNaftahParserVisitor,
												List<Pair<String, Object>> args,
												DefaultContext currentContext
	) {
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
			return defaultNaftahParserVisitor.visit(declaredFunction.getBody());
		}
		finally {
			if (functionInStack) {
				popCall();
			}
		}
	}

	/**
	 * Invokes a built-in function using reflection with argument conversion.
	 *
	 * @param functionName    the name of the function
	 * @param builtinFunction the built-in function to invoke
	 * @param args            the list of argument name/value pairs
	 * @param line            the line number in source code for error reporting
	 * @param column          the column number in source code for error reporting
	 * @return the result of invoking the built-in function
	 */
	public static Object invokeBuiltinFunction( String functionName,
												BuiltinFunction builtinFunction,
												List<Pair<String, Object>> args,
												int line,
												int column) {
		try {
			return ClassUtils
					.invokeJvmMethod(   null,
										builtinFunction.getMethod(),
										args,
										builtinFunction
												.getFunctionInfo()
												.returnType(),
										true);
		}
		catch (IllegalArgumentException e) {
			throw newNaftahIllegalArgumentError(functionName,
												builtinFunction
														.getProviderInfo()
														.name(),
												builtinFunction
														.getMethod()
														.getParameterCount(),
												args.size(),
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
		catch (NoSuchMethodException e) {
			throw newNaftahNoSuchMethodError(   functionName,
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
	 * Invokes a JVM function, either static or instance, with argument conversion.
	 * If the function is an instance method, the first argument must be the instance object.
	 *
	 * @param functionName the name of the function
	 * @param jvmFunction  the JVM function to invoke
	 * @param args         the list of argument name/value pairs
	 * @param line         the line number in source code for error reporting
	 * @param column       the column number in source code for error reporting
	 * @return the result of invoking the JVM function
	 * @throws NaftahBugError if the instance is missing for an instance method or the function is non-invocable
	 */
	public static Object invokeJvmFunction( String functionName,
											JvmFunction jvmFunction,
											List<Pair<String, Object>> args,
											int line,
											int column) {

		if (jvmFunction.isInvocable()) {
			// instance should be passed as first arg if invoking method on instance
			Object possibleInstance = null;
			if (!jvmFunction.isStatic()) {
				if (args.isEmpty()) {
					throw new NaftahBugError(INVALID_INSTANCE_METHOD_CALL_MSG
							.apply( functionName,
									jvmFunction
											.toDetailedString()));
				}
				possibleInstance = args.remove(0).b;
			}

			try {
				return NaftahObject
						.of(ClassUtils
								.invokeJvmMethod(   possibleInstance,
													jvmFunction.getMethod(),
													args,
													jvmFunction.getMethod().getReturnType(),
													false));
			}
			catch (IllegalArgumentException e) {
				throw newNaftahIllegalArgumentError(functionName,
													ClassUtils.getQualifiedName(jvmFunction.getClazz().getName()),
													jvmFunction
															.getMethod()
															.getParameterCount(),
													args.size(),
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
			catch (NoSuchMethodException e) {
				throw newNaftahNoSuchMethodError(   functionName,
													jvmFunction.toDetailedString(),
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
}
