package org.daiitech.naftah.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.Tree;
import org.daiitech.naftah.builtin.Builtin;
import org.daiitech.naftah.builtin.lang.BuiltinFunction;
import org.daiitech.naftah.builtin.lang.DeclaredFunction;
import org.daiitech.naftah.builtin.lang.DeclaredImplementation;
import org.daiitech.naftah.builtin.lang.DeclaredParameter;
import org.daiitech.naftah.builtin.lang.DeclaredVariable;
import org.daiitech.naftah.builtin.lang.JvmClassInitializer;
import org.daiitech.naftah.builtin.lang.JvmFunction;
import org.daiitech.naftah.builtin.lang.NaftahObject;
import org.daiitech.naftah.builtin.lang.Result;
import org.daiitech.naftah.builtin.utils.concurrent.Actor;
import org.daiitech.naftah.builtin.utils.concurrent.SuppliedInheritableThreadLocal;
import org.daiitech.naftah.builtin.utils.concurrent.Task;
import org.daiitech.naftah.builtin.utils.tuple.ImmutablePair;
import org.daiitech.naftah.builtin.utils.tuple.ImmutableTriple;
import org.daiitech.naftah.builtin.utils.tuple.Pair;
import org.daiitech.naftah.builtin.utils.tuple.Triple;
import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.utils.Base64SerializationUtils;
import org.daiitech.naftah.utils.arabic.ArabicUtils;
import org.daiitech.naftah.utils.reflect.ClassScanningResult;
import org.daiitech.naftah.utils.reflect.ClassUtils;
import org.daiitech.naftah.utils.reflect.RuntimeClassScanner;

import static org.daiitech.naftah.Naftah.BUILTIN_CLASSES_PROPERTY;
import static org.daiitech.naftah.Naftah.BUILTIN_PACKAGES_PROPERTY;
import static org.daiitech.naftah.Naftah.CACHE_SCANNING_RESULTS_PROPERTY;
import static org.daiitech.naftah.Naftah.DEBUG_PROPERTY;
import static org.daiitech.naftah.Naftah.FORCE_CLASSPATH_PROPERTY;
import static org.daiitech.naftah.Naftah.INCLUDE_ALL_IN_COMPLETIONS_PROPERTY;
import static org.daiitech.naftah.Naftah.INSIDE_INIT_PROPERTY;
import static org.daiitech.naftah.Naftah.INSIDE_MAN_PROPERTY;
import static org.daiitech.naftah.Naftah.INSIDE_REPL_PROPERTY;
import static org.daiitech.naftah.Naftah.SCAN_CLASSPATH_PROPERTY;
import static org.daiitech.naftah.builtin.utils.AliasHashMap.toAliasGroupedByName;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahInvocableNotFoundError;
import static org.daiitech.naftah.parser.NaftahParserHelper.QUALIFIED_CALL_REGEX;
import static org.daiitech.naftah.parser.NaftahParserHelper.QUALIFIED_NAME_REGEX;
import static org.daiitech.naftah.parser.NaftahParserHelper.hasAnyParentOfType;
import static org.daiitech.naftah.utils.ConsoleLoader.startLoader;
import static org.daiitech.naftah.utils.ConsoleLoader.stopLoader;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.padText;
import static org.daiitech.naftah.utils.reflect.ClassUtils.QUALIFIED_CALL_SEPARATOR;
import static org.daiitech.naftah.utils.reflect.ClassUtils.QUALIFIED_NAME_SEPARATOR;
import static org.daiitech.naftah.utils.reflect.ClassUtils.filterClasses;
import static org.daiitech.naftah.utils.reflect.ClassUtils.getArabicClassQualifiersMapping;
import static org.daiitech.naftah.utils.reflect.ClassUtils.getBuiltinMethods;
import static org.daiitech.naftah.utils.reflect.ClassUtils.getClassConstructors;
import static org.daiitech.naftah.utils.reflect.ClassUtils.getClassMethods;
import static org.daiitech.naftah.utils.reflect.ClassUtils.getClassQualifiers;
import static org.daiitech.naftah.utils.reflect.ClassUtils.getQualifiedCall;
import static org.daiitech.naftah.utils.reflect.ClassUtils.getQualifiedName;
import static org.daiitech.naftah.utils.reflect.RuntimeClassScanner.loadClasses;
import static org.daiitech.naftah.utils.reflect.RuntimeClassScanner.scanClasses;

/**
 * Represents the default execution context used to manage variables, functions, loops,
 * and class loading information during runtime.
 * <p>
 * This class handles:
 * <ul>
 * <li>Contextual stacks for function calls and loops</li>
 * <li>Unique identifier generation for functions, parameters, arguments, and loops</li>
 * <li>Class scanning and loading tasks executed asynchronously</li>
 * <li>Storage and management of loaded classes, accessible and instantiable classes</li>
 * <li>Management of JVM and builtin functions</li>
 * </ul>
 * <p>
 * Contexts are arranged hierarchically with possible parent-child relationships, and
 * multiple contexts are tracked globally via a static map keyed by depth.
 * <p>
 * This class also provides thread-safe lazy loading of classes and functions, with
 * configurable bootstrap behavior.
 * <p>
 * Note: This class uses custom types such as {@link DeclaredFunction}, {@link DeclaredVariable},
 * {@link DeclaredParameter}, {@link JvmFunction}, {@link BuiltinFunction}, and others related
 * to the execution environment and parser.
 *
 * @author Chakib Daii
 */
public class DefaultContext {

	/**
	 * The path used for caching runtime data.
	 */
	public static final Path CACHE_PATH = Paths.get(".naftah/.naftah_cache");
	/**
	 * The path used for caching minimal runtime data.
	 */
	public static final Path MINIMAL_CACHE_PATH = Paths.get(".naftah/.naftah_minimal_cache");

	/**
	 * Global map holding contexts indexed by their depth.
	 */
	protected static final ConcurrentHashMap<Integer, CopyOnWriteArrayList<DefaultContext>> CONTEXTS = new ConcurrentHashMap<>();
	/**
	 * Stack representing the call stack containing pairs of function and argument maps,
	 * along with the returned value.
	 */
	protected static final ThreadLocal<Deque<Triple<DeclaredFunction, Map<String, Object>, Object>>> CALL_STACK = ThreadLocal
			.withInitial(ArrayDeque::new);
	/**
	 * Stack representing loop labels and their associated parser rule contexts.
	 */
	protected static final InheritableThreadLocal<Deque<Pair<String, ? extends ParserRuleContext>>> LOOP_STACK = SuppliedInheritableThreadLocal
			.withInitial(ArrayDeque::new, ArrayDeque::new);

	/**
	 * A supplier task that performs a full asynchronous scan, loading, filtering,
	 * and extraction of JVM and builtin functions from the classpath.
	 * <p>
	 * The task executes the following steps:
	 * <ol>
	 * <li>Scans all available classes.</li>
	 * <li>Generates class qualifiers and their Arabic transliterations.</li>
	 * <li>Loads the Class objects for all discovered classes.</li>
	 * <li>Filters classes into accessible and instantiable sets.</li>
	 * <li>Retrieves JVM class initializers (constructors) for instantiable classes.</li>
	 * <li>Retrieves all JVM and builtin functions for accessible and instantiable classes.</li>
	 * </ol>
	 * <p>
	 * All operations are executed asynchronously using a fixed thread pool (2 threads),
	 * and results are collected into a {@link ClassScanningResult} instance.
	 * In case of interruption or execution errors, a {@link NaftahBugError} is thrown.
	 */
	protected static final Supplier<ClassScanningResult> LOADER_TASK = () -> {
		ExecutorService internalExecutor = Executors.newFixedThreadPool(2);
		ClassScanningResult result = new ClassScanningResult();
		var classNames = scanClasses();
		result.setClassNames(classNames);

		try {
			Callable<Pair<Set<String>, Map<String, String>>> qualifiersLoaderTask = () -> {
				var classQualifiersMap = getClassQualifiers(classNames.keySet(), false);
				var classQualifiers = new HashSet<>(classQualifiersMap.keySet());
				var arabicClassQualifiers = getArabicClassQualifiersMapping(classQualifiersMap.values());
				return ImmutablePair.of(classQualifiers, arabicClassQualifiers);
			};
			var qualifiersFuture = internalExecutor.submit(qualifiersLoaderTask);

			Callable<Map<String, Class<?>>> classLoaderTask = () -> loadClasses(classNames, false);
			var classFuture = internalExecutor.submit(classLoaderTask);

			var qualifiersFutureResult = qualifiersFuture.get();
			result.setClassQualifiers(qualifiersFutureResult.getLeft());
			result.setArabicClassQualifiers(qualifiersFutureResult.getRight());

			result.setClasses(classFuture.get());

			Callable<Map<String, Class<?>>> accessibleClassLoaderTask = () -> filterClasses(result.getClasses(),
																							ClassUtils::isAccessibleClass);
			var accessibleClassFuture = internalExecutor.submit(accessibleClassLoaderTask);

			Callable<Map<String, Class<?>>> instantiableClassLoaderTask = () -> filterClasses(  result.getClasses(),
																								ClassUtils::isInstantiableClass);
			var instantiableClassFuture = internalExecutor.submit(instantiableClassLoaderTask);

			result.setAccessibleClasses(accessibleClassFuture.get());
			result.setInstantiableClasses(instantiableClassFuture.get());

			Callable<Map<String, List<JvmClassInitializer>>> jvmClassInitializerLoaderTask = () -> getClassConstructors(
																														result
																																.getInstantiableClasses());
			var jvmClassInitializerFuture = internalExecutor.submit(jvmClassInitializerLoaderTask);

			result.setJvmClassInitializers(jvmClassInitializerFuture.get());

			var accessibleAndInstantiable = new HashMap<>(result.getAccessibleClasses()) {
				{
					putAll(result.getInstantiableClasses());
				}
			};

			Callable<Map<String, List<JvmFunction>>> jvmFunctionsLoaderTask = () -> getClassMethods(
																									accessibleAndInstantiable);
			var jvmFunctionsFuture = internalExecutor.submit(jvmFunctionsLoaderTask);

			Callable<Map<String, List<BuiltinFunction>>> builtinFunctionsLoaderTask = () -> getBuiltinMethods(
																												accessibleAndInstantiable);
			var builtinFunctionsFuture = internalExecutor.submit(builtinFunctionsLoaderTask);

			result.setJvmFunctions(jvmFunctionsFuture.get());
			result.setBuiltinFunctions(builtinFunctionsFuture.get());

			return result;
		}
		catch (InterruptedException | ExecutionException e) {
			throw new NaftahBugError(e);
		}
		finally {
			internalExecutor.shutdown();
		}
	};

	/**
	 * A minimal supplier task that performs an asynchronous scan of classes and
	 * computes class qualifiers and their Arabic transliterations.
	 * <p>
	 * Unlike {@link #LOADER_TASK}, this task does not load Class objects, filter
	 * them, or retrieve constructors and functions. It is suitable for lightweight
	 * operations where only class name metadata is needed.
	 * <p>
	 * All operations are executed asynchronously using a single-thread executor.
	 */
	protected static final Supplier<ClassScanningResult> MINIMAL_LOADER_TASK = () -> {
		ExecutorService internalExecutor = Executors.newFixedThreadPool(1);
		ClassScanningResult result = new ClassScanningResult();
		var classNames = scanClasses();
		result.setClassNames(classNames);

		try {
			Callable<Pair<Set<String>, Map<String, String>>> qualifiersLoaderTask = () -> {
				var classQualifiersMap = getClassQualifiers(classNames.keySet(), false);
				var classQualifiers = new HashSet<>(classQualifiersMap.keySet());
				var arabicClassQualifiers = getArabicClassQualifiersMapping(classQualifiersMap.values());
				return ImmutablePair.of(classQualifiers, arabicClassQualifiers);
			};
			var qualifiersFuture = internalExecutor.submit(qualifiersLoaderTask);

			var qualifiersFutureResult = qualifiersFuture.get();
			result.setClassQualifiers(qualifiersFutureResult.getLeft());
			result.setArabicClassQualifiers(qualifiersFutureResult.getRight());

			return result;
		}
		catch (InterruptedException | ExecutionException e) {
			throw new NaftahBugError(e);
		}
		finally {
			internalExecutor.shutdown();
		}
	};
	/**
	 * Holds the current thread's {@link DefaultContext} in a thread-local variable.
	 */
	protected static final ThreadLocal<DefaultContext> CURRENT_CONTEXT = new ThreadLocal<>();
	/**
	 * Thread-local list holding all tasks spawned in the current scope for this thread.
	 */
	protected static ThreadLocal<List<Task<?>>> CURRENT_TASK_SCOPE;
	// LOADED CLASSES
	protected static volatile Set<String> CLASS_NAMES;
	protected static volatile Set<String> CLASS_QUALIFIERS;
	protected static volatile Map<String, String> ARABIC_CLASS_QUALIFIERS;
	// qualifiedName -> CLass<?>
	protected static volatile Map<String, Class<?>> CLASSES;
	protected static volatile Map<String, Class<?>> ACCESSIBLE_CLASSES;
	protected static volatile Map<String, Class<?>> INSTANTIABLE_CLASSES;
	// qualifiedCall -> Method
	protected static volatile Map<String, List<JvmFunction>> JVM_FUNCTIONS;
	protected static volatile ThreadLocal<List<JvmFunction>> CURRENT_LOOKUP_JVM_FUNCTIONS;
	protected static volatile Map<String, List<JvmClassInitializer>> JVM_CLASS_INITIALIZERS;
	protected static volatile ThreadLocal<List<JvmClassInitializer>> CURRENT_LOOKUP_JVM_CLASS_INITIALIZERS;
	protected static volatile Map<String, List<BuiltinFunction>> BUILTIN_FUNCTIONS;
	protected static volatile Map<String, String> IMPORTS = new ConcurrentHashMap<>();
	protected static volatile boolean SHOULD_BOOT_STRAP;
	protected static volatile boolean FORCE_BOOT_STRAP;
	protected static volatile boolean ASYNC_BOOT_STRAP;
	protected static volatile boolean BOOT_STRAP_FAILED;
	protected static volatile boolean BOOT_STRAPPED;

	/**
	 * A consumer to handle the result or error from the full class scanning and loading task.
	 * <p>
	 * If an error occurs (non-null {@code throwable}), it triggers a default bootstrap
	 * and marks {@code BOOT_STRAP_FAILED} as {@code true}.
	 * Otherwise, it sets the scanning context from the {@link ClassScanningResult}
	 * and optionally caches the result if {@code CACHE_SCANNING_RESULTS_PROPERTY} is enabled.
	 */
	protected static final BiConsumer<? super ClassScanningResult, ? super Throwable> LOADER_CONSUMER = (   result,
																											throwable) -> {
		if (Objects.nonNull(throwable)) {
			defaultBootstrap();
			BOOT_STRAP_FAILED = true;
		}
		else {
			setContextFromClassScanningResult(result);
			if (Boolean.getBoolean(CACHE_SCANNING_RESULTS_PROPERTY)) {
				serializeClassScanningResult(result, CACHE_PATH);
			}
		}
	};

	/**
	 * A consumer to handle the result or error from the minimal class scanning task.
	 * <p>
	 * If an error occurs (non-null {@code throwable}), it simply marks
	 * {@code BOOT_STRAP_FAILED} as {@code true}.
	 * Otherwise, it sets the scanning context from the {@link ClassScanningResult}.
	 * <p>
	 * Unlike {@link #LOADER_CONSUMER}, this consumer does not perform caching or default bootstrapping.
	 */
	protected static final BiConsumer<? super ClassScanningResult, ? super Throwable> MINIMAL_LOADER_CONSUMER = (   result,
																													throwable) -> {
		if (Objects.nonNull(throwable)) {
			BOOT_STRAP_FAILED = true;
		}
		else {
			setContextFromClassScanningResult(result);
			if (Boolean.getBoolean(CACHE_SCANNING_RESULTS_PROPERTY)) {
				serializeClassScanningResult(result, MINIMAL_CACHE_PATH);
			}
		}
	};

	// instance
	protected final Thread owner = Thread.currentThread();
	protected final AtomicInteger pendingTasks = new AtomicInteger(0);
	protected final DefaultContext parent;
	protected final int depth;
	protected final InheritableThreadLocal<Map<String, DeclaredVariable>> variables = SuppliedInheritableThreadLocal
			.withInitial(HashMap::new, HashMap::new);
	protected final InheritableThreadLocal<Map<String, DeclaredFunction>> functions = SuppliedInheritableThreadLocal
			.withInitial(HashMap::new, HashMap::new);
	protected final InheritableThreadLocal<Map<String, DeclaredImplementation>> implementations = SuppliedInheritableThreadLocal
			.withInitial(HashMap::new, HashMap::new);
	protected volatile boolean pendingRemoval = false;
	protected ThreadLocal<String> functionCallId; // current function in execution inside a context
	// current implementation of function in execution inside a context
	protected InheritableThreadLocal<String> implementationName;
	protected ThreadLocal<Boolean> parsingFunctionCallId; // parsing current function in execution
	protected ThreadLocal<Boolean> parsingAssignment; // parsing an assignment is in execution
	protected ThreadLocal<Boolean> creatingObject; // object creation is in execution
	protected ThreadLocal<Boolean> awaitingTask; // awaiting a spawned task
	// the declaration of variable being assigned
	protected ThreadLocal<Pair<DeclaredVariable, Boolean>> declarationOfAssignment;
	protected ThreadLocal<String> loopLabel; // current loop label in execution inside a context
	protected InheritableThreadLocal<Map<String, Object>> loopVariables; // only use in loop execution context
	protected InheritableThreadLocal<NaftahParseTreeProperty<Boolean>> parseTreeExecution;
	protected InheritableThreadLocal<Map<String, String>> blockImports;
	protected InheritableThreadLocal<Map<String, DeclaredParameter>> parameters; // only use in function call context
	protected InheritableThreadLocal<Map<String, Object>> arguments; // only use in function call context

	/**
	 * Constructs a default context with no parent, block imports, parameters, or arguments.
	 */
	protected DefaultContext() {
		this(null, null, null, null);
	}

	/**
	 * Constructs a context with specified parameters and arguments, but no parent.
	 *
	 * @param parameters function parameters map
	 * @param arguments  function arguments map
	 */
	protected DefaultContext(Map<String, DeclaredParameter> parameters, Map<String, Object> arguments) {
		this(null, null, parameters, arguments);
	}

	/**
	 * Constructs a new {@code DefaultContext} with an optional parent context and
	 * optional initial state.
	 * <p>
	 * This constructor initializes a context with references to its parent,
	 * lexical depth, and any provided block imports, parameters, or arguments.
	 * Supplied maps are used as the initial thread-local state for the context.
	 * </p>
	 *
	 * <p>
	 * Context creation is restricted: if the environment is not a REPL
	 * (as indicated by {@code INSIDE_REPL_PROPERTY}), a root context may only be
	 * created when no other contexts already exist. Violating this invariant
	 * results in a {@link NaftahBugError}.
	 * </p>
	 *
	 * <p>
	 * When a parent context is provided, selected thread-local metadata
	 * (such as the current function call identifier and implementation name)
	 * is inherited from the parent if present.
	 * </p>
	 *
	 * @param parent       The parent context, or {@code null} if this is a root context.
	 * @param blockImports Initial block-level imports for this context, or {@code null} if none.
	 * @param parameters   Initial function parameters for this context, or {@code null} if none.
	 * @param arguments    Initial function arguments for this context, or {@code null} if none.
	 * @throws NaftahBugError If this constructor is invoked in an invalid context-creation
	 *                        scenario (for example, creating a root context outside the REPL
	 *                        when contexts already exist).
	 */
	protected DefaultContext(   DefaultContext parent,
								Map<String, String> blockImports,
								Map<String, DeclaredParameter> parameters,
								Map<String, Object> arguments) {
		if (!Boolean.getBoolean(INSIDE_REPL_PROPERTY) && parent == null && (!CONTEXTS.isEmpty())) {
			throw newNaftahBugInvalidUsageError();
		}
		this.parent = parent;
		this.depth = parent == null ? 0 : parent.getDepth() + 1;

		if (Objects.nonNull(blockImports)) {
			this.blockImports = SuppliedInheritableThreadLocal.withInitial(() -> blockImports, HashMap::new);
		}
		if (Objects.nonNull(parameters)) {
			this.parameters = SuppliedInheritableThreadLocal.withInitial(() -> parameters, HashMap::new);
		}
		if (Objects.nonNull(arguments)) {
			this.arguments = SuppliedInheritableThreadLocal.withInitial(() -> arguments, HashMap::new);
		}
		if (Objects.nonNull(parent)) {
			if (Objects.nonNull(parent.functionCallId)) {
				String functionCallId;
				if (Objects.nonNull(functionCallId = parent.functionCallId.get())) {
					setFunctionCallId(functionCallId);
				}
			}
			if (Objects.nonNull(parent.implementationName)) {
				String implementationName;
				if (Objects.nonNull(implementationName = parent.implementationName.get())) {
					setImplementationName(implementationName);
				}
			}
		}
		CONTEXTS.computeIfAbsent(depth, d -> new CopyOnWriteArrayList<>()).add(this);
		CURRENT_CONTEXT.set(this);
	}

	/**
	 * Generates a unique loop identifier string based on the loop's depth and a random UUID.
	 *
	 * <p>This is used to create unique labels for unnamed loops,
	 * helping to distinguish nested or recursive loops during parsing or execution.
	 *
	 * @param depth the depth level of the current loop
	 * @return a unique loop ID in the format {@code <depth>-loop-<uuid>}
	 */
	public static String generateLoopId(int depth) {
		return "%s-loop-%s".formatted(depth, UUID.randomUUID());
	}

	/**
	 * Generates a unique name for a function parameter or argument by combining the
	 * function name with the parameter or argument name.
	 *
	 * <p>Useful for creating internally consistent identifiers across contexts
	 * such as code generation, interpretation, or debugging.
	 *
	 * @param functionName the name of the function
	 * @param name         the name of the parameter or argument
	 * @return a unique identifier in the format {@code <functionName>-<name>}
	 */
	public static String generateParameterOrArgumentName(String functionName, String name) {
		return "%s-%s".formatted(functionName, name);
	}

	/**
	 * Resolves a variable's value from the given context using the variable name.
	 *
	 * <p>The lookup order is:
	 * <ol>
	 * <li>Loop variables</li>
	 * <li>Function arguments</li>
	 * <li>Function parameters</li>
	 * <li>Declared variables</li>
	 * </ol>
	 *
	 * <p>If the variable is not found in any of these, a {@code notFound} result is returned.
	 *
	 * @param varName the name of the variable to look up
	 * @param context the current execution context in which to resolve the variable
	 * @return a {@link VariableLookupResult} containing the variable name and its resolved value,
	 *         or a not-found result if the variable does not exist in the context
	 */
	public static VariableLookupResult<Object> getVariable( String varName,
															DefaultContext context) {
		return Optional
				.ofNullable(context.getLoopVariable(varName, true))
				.flatMap(functionArgument -> Optional
						.of(VariableLookupResult.of(varName, functionArgument.getRight())))
				.orElseGet(() -> Optional
						.ofNullable(context.getFunctionArgument(varName, true))
						.flatMap(functionArgument -> Optional
								.of(VariableLookupResult.of(varName, functionArgument.getRight())))
						.orElseGet(() -> Optional
								.ofNullable(context.getFunctionParameter(varName, true))
								.flatMap(functionParameter -> Optional
										.of(VariableLookupResult
												.of(
													varName,
													functionParameter.getRight().getValue())))
								.orElseGet(() -> Optional
										.ofNullable(context.getVariable(varName, true))
										.flatMap(declaredVariable -> {
											var value = declaredVariable.getRight().getValue();

											if (value instanceof Result<?, ?> result) {
												if (result.isOk()) {
													value = result.unwrap();
												}
												else if (result.isError()) {
													value = result.unwrapError();
												}
											}

											return Optional
													.of(VariableLookupResult
															.of(varName, value));
										})
										.orElse(VariableLookupResult.notFound(varName)))));
	}

	/**
	 * Generates a unique function call identifier string based on the call's depth,
	 * the function name, and a random UUID.
	 *
	 * <p>This is typically used to track or label function calls uniquely during
	 * parsing or execution, especially in recursive or nested calls.
	 *
	 * @param depth        the current call depth
	 * @param functionName the name of the function being called
	 * @return a unique call ID in the format {@code <depth>-<functionName>-<uuid>}
	 */
	public static String generateCallId(int depth, String functionName) {
		return "%s-%s-%s".formatted(depth, functionName, UUID.randomUUID());
	}

	/**
	 * Registers a new default context with no parent, parameters, or arguments.
	 *
	 * @return a new {@link DefaultContext} instance
	 */
	public static DefaultContext registerContext() {
		return new DefaultContext();
	}

	/**
	 * Registers a new default context with the specified parameters and arguments.
	 *
	 * @param parameters the function parameters map
	 * @param arguments  the function arguments map
	 * @return a new {@link DefaultContext} instance
	 */
	public static DefaultContext registerContext(   Map<String, DeclaredParameter> parameters,
													Map<String, Object> arguments) {
		return new DefaultContext(parameters, arguments);
	}

	/**
	 * Registers a new {@link DefaultContext} with the specified parent context.
	 * <p>
	 * This method creates a new context instance that inherits settings, variables,
	 * and configurations from the given parent context.
	 * </p>
	 *
	 * @param parent the parent {@link DefaultContext} to inherit from; may be {@code null}
	 * @return a newly created {@link DefaultContext} instance
	 */
	public static DefaultContext registerContext(DefaultContext parent) {
		return new DefaultContext(parent, null, null, null);
	}

	/**
	 * Registers a new {@link DefaultContext} with the specified parent context
	 * and a custom set of block imports.
	 * <p>
	 * This method allows specifying a map of block imports that will be associated
	 * with the new context in addition to inheriting properties from the parent.
	 * </p>
	 *
	 * @param parent       the parent {@link DefaultContext} to inherit from; may be {@code null}
	 * @param blockImports a map of block import definitions for the new context; may be {@code null}
	 * @return a newly created {@link DefaultContext} instance
	 */
	public static DefaultContext registerContext(DefaultContext parent, Map<String, String> blockImports) {
		return new DefaultContext(parent, blockImports, null, null);
	}

	/**
	 * Creates and registers a new {@link DefaultContext} instance with the specified parent context,
	 * parameters, and arguments.
	 * <p>
	 * This overload can be used when no explicit block imports are required.
	 * The created context inherits any imports from its parent.
	 * </p>
	 *
	 * @param parent     the parent {@link DefaultContext} to associate with this new context;
	 *                   may be {@code null} if this is the root context
	 * @param parameters a map of declared function parameters, where keys are parameter names
	 *                   and values are {@link DeclaredParameter} definitions
	 * @param arguments  a map of argument values corresponding to the provided parameters
	 * @return a new {@link DefaultContext} instance linked to the given parent
	 */
	public static DefaultContext registerContext(   DefaultContext parent,
													Map<String, DeclaredParameter> parameters,
													Map<String, Object> arguments) {
		return new DefaultContext(parent, null, parameters, arguments);
	}

	/**
	 * Creates and registers a new {@link DefaultContext} instance with the specified parent context,
	 * block imports, parameters, and arguments.
	 * <p>
	 * This overload allows specifying custom imports that will be available within
	 * the new context, such as additional modules or namespaces.
	 * </p>
	 *
	 * @param parent       the parent {@link DefaultContext} to associate with this new context;
	 *                     may be {@code null} if this is the root context
	 * @param blockImports a map of imported blocks or namespaces, where keys represent import names
	 *                     and values represent their corresponding qualified paths
	 * @param parameters   a map of declared function parameters, where keys are parameter names
	 *                     and values are {@link DeclaredParameter} definitions
	 * @param arguments    a map of argument values corresponding to the provided parameters
	 * @return a new {@link DefaultContext} instance linked to the given parent and imports
	 */
	public static DefaultContext registerContext(   DefaultContext parent,
													Map<String, String> blockImports,
													Map<String, DeclaredParameter> parameters,
													Map<String, Object> arguments) {
		return new DefaultContext(parent, blockImports, parameters, arguments);
	}

	/**
	 * Returns the current thread’s active context.
	 * <p>
	 * This retrieves the value stored in the thread-local {@code CURRENT_CONTEXT}.
	 * Each thread always sees its own context, independent of other threads.
	 * </p>
	 *
	 * @return the {@link DefaultContext} bound to the current thread,
	 *         or {@code null} if none is set
	 */
	public static DefaultContext getCurrentContext() {
		return CURRENT_CONTEXT.get();
	}


	/**
	 * Sets the active context for the current thread.
	 * <p>
	 * This assigns a {@link DefaultContext} to the calling thread’s thread-local
	 * storage. Each thread maintains a separate context pointer.
	 * </p>
	 *
	 * @param currentContext the context to bind to this thread
	 */
	public static void setCurrentContext(DefaultContext currentContext) {
		CURRENT_CONTEXT.set(currentContext);
	}

	/**
	 * Retrieves all contexts registered at a specific depth.
	 * <p>
	 * Multiple contexts may exist at the same depth when asynchronous tasks
	 * or concurrent scopes are active. The returned list is thread-safe but is
	 * not a defensive copy; modifications affect global state.
	 * </p>
	 *
	 * @param depth the depth level whose contexts should be returned
	 * @return a list of {@link DefaultContext} objects at that depth;
	 *         an empty list if none exist
	 */
	public static List<DefaultContext> getContextsByDepth(int depth) {
		return CONTEXTS.getOrDefault(depth, new CopyOnWriteArrayList<>());
	}


	/**
	 * Deregisters and removes the context associated with the specified depth.
	 * If the removed context and its parent both have parse tree execution properties,
	 * copies the parse tree execution state from the removed context to its parent.
	 *
	 */
	public static void deregisterContext() {
		DefaultContext currentContext = CURRENT_CONTEXT.get();
		deregisterContext(currentContext);
	}

	/**
	 * Attempts to detach the given {@link DefaultContext} from its parent context,
	 * performing state propagation when necessary.
	 *
	 * <p>If the context can be safely deregistered (i.e., no pending tasks and
	 * no unresolved execution state), {@link #tryDeregisterContext(DefaultContext)}
	 * is invoked. When deregistration succeeds, any active parse-tree execution
	 * metadata from the child context is copied into the parent to preserve
	 * execution continuity.</p>
	 *
	 * <p>If deregistration fails—typically due to pending asynchronous tasks or
	 * execution markers that must remain valid—the method falls back to
	 * {@link #copyDeclarationsToParent(DefaultContext)} to propagate variable and
	 * function declarations from the child context into the parent. This prevents
	 * redefinition conflicts or loss of symbol information while the child context
	 * remains active.</p>
	 *
	 * <p>Calling this method has no effect if the given context is {@code null} or
	 * has no parent.</p>
	 *
	 * @param context the context to deregister; may be {@code null}
	 */
	public static void deregisterContext(DefaultContext context) {
		if (Objects.nonNull(context) && Objects.nonNull(context.parent)) {
			if (tryDeregisterContext(context)) {
				copyDeclarationsToParent(context);
				if (Objects.nonNull(context.parseTreeExecution) && Objects
						.nonNull(context.parent.parseTreeExecution)) {
					context.parent.parseTreeExecution.get().copyFrom(context.parseTreeExecution.get());
				}
			}
			else {
				// copy declarations of context with pending tasks in case of deregistration failure to parent
				// as prevention of creation of variable with the same name
				copyDeclarationsToParent(context);
			}
		}
	}

	/**
	 * Copies variable, function, and implementation declarations from a child
	 * context into its parent context.
	 * <p>
	 * This method is used as a fallback when a child context cannot be safely
	 * deregistered (for example, due to pending tasks or deferred cleanup).
	 * By propagating declarations upward, the parent context gains visibility
	 * into symbols introduced by the child, avoiding loss of symbol information
	 * if the child remains active longer than expected.
	 * </p>
	 *
	 * <p>
	 * In a REPL environment (indicated by {@code INSIDE_REPL_PROPERTY}), all
	 * declarations declared at depth {@code 1} are copied directly to the parent.
	 * </p>
	 *
	 * <p>
	 * Outside of a REPL environment, only declarations whose declared depth is
	 * less than or equal to {@code context.depth - 1} are copied. This prevents
	 * propagation of symbols that are local to deeper lexical scopes.
	 * </p>
	 *
	 * @param context The child {@link DefaultContext} whose declarations should be copied
	 *                into its parent context.
	 * @throws NullPointerException If {@code context} or its parent context is {@code null}.
	 */
	public static void copyDeclarationsToParent(DefaultContext context) {
		Objects.requireNonNull(Objects.requireNonNull(context).parent);
		if (Boolean.getBoolean(INSIDE_REPL_PROPERTY) && context.depth == 1) {
			context.parent.variables.get().putAll(context.variables.get());
			context.parent.functions.get().putAll(context.functions.get());
			context.parent.implementations.get().putAll(context.implementations.get());
		}
		else {
			context.parent.variables
					.get()
					.putAll(context.variables
							.get()
							.entrySet()
							.stream()
							.filter(declaredVariableEntry -> declaredVariableEntry
									.getValue()
									.getDepth() <= (context.depth - 1))
							.collect(Collectors
									.toMap( Map.Entry::getKey,
											Map.Entry::getValue)));
			context.parent.functions
					.get()
					.putAll(context.functions
							.get()
							.entrySet()
							.stream()
							.filter(declaredFunctionEntry -> declaredFunctionEntry
									.getValue()
									.getDepth() <= (context.depth - 1))
							.collect(Collectors
									.toMap( Map.Entry::getKey,
											Map.Entry::getValue)));
			context.parent.implementations
					.get()
					.putAll(context.implementations
							.get()
							.entrySet()
							.stream()
							.filter(declaredImplementationEntry -> declaredImplementationEntry
									.getValue()
									.getDepth() <= (context.depth - 1))
							.collect(Collectors
									.toMap( Map.Entry::getKey,
											Map.Entry::getValue)));
		}
	}

	/**
	 * Marks the given context as eligible for removal once it becomes safe
	 * to do so.
	 *
	 * <p>This flag does <strong>not</strong> remove the context immediately.
	 * A context may be marked for removal when:
	 * <ul>
	 * <li>its owning scope has ended, or</li>
	 * <li>a thread attempts to deregister the context but it still has
	 * pending asynchronous tasks, or</li>
	 * <li>any of its descendant contexts still have active work.</li>
	 * </ul>
	 *
	 * <p>Contexts marked for removal will be deleted automatically once:
	 * <ul>
	 * <li>the context itself has zero pending tasks, and</li>
	 * <li>all of its descendant contexts have zero pending tasks.</li>
	 * </ul>
	 *
	 * <p>This prevents premature cleanup while ensuring that no contexts leak
	 * after they become fully inactive.</p>
	 *
	 * @param context the {@link DefaultContext} to mark for future removal;
	 *                must not be {@code null}
	 */
	public static void markForRemoval(DefaultContext context) {
		context.pendingRemoval = Thread.currentThread() == context.owner;
	}

	/**
	 * Attempts to remove a context from the global context registry.
	 *
	 * <p>The removal is <strong>conditional</strong>. This method will only
	 * deregister the given context when all the following conditions
	 * are satisfied:
	 *
	 * <ul>
	 * <li>The context has no pending asynchronous tasks.</li>
	 * <li>No descendant context has pending tasks.</li>
	 * <li>The context has been properly registered in the
	 * {@link DefaultContext#CONTEXTS} structure.</li>
	 * </ul>
	 *
	 * <p>If removal is not yet possible, the context is marked for later
	 * cleanup via {@link #markForRemoval(DefaultContext)}.</p>
	 *
	 * <p>If the context is the <em>current</em> context of the calling thread,
	 * the thread-local pointer {@link DefaultContext#CURRENT_CONTEXT}
	 * will be updated to the parent context. Otherwise, the caller's
	 * thread-local context remains unchanged.</p>
	 *
	 * <p>The method is thread-safe and may be called concurrently from
	 * multiple threads. Removal will occur exactly once.</p>
	 *
	 * @param context the context to attempt to deregister
	 * @return {@code true} if the context was removed,
	 *         {@code false} if removal was deferred
	 */
	public static boolean tryDeregisterContext(DefaultContext context) {
		// If this context itself still has tasks → don't remove it.
		if (context.pendingTasks.get() > 0) {
			markForRemoval(context);
			return false; // still used by running tasks
		}

		// Check children & descendants for pending tasks
		for (DefaultContext child : context.getChildren()) {
			if (child.pendingTasks.get() > 0) {
				markForRemoval(context);
				return false;
			}
		}

		// Now it is safe: remove only ctx (not the whole depth)
		CopyOnWriteArrayList<DefaultContext> list = CONTEXTS.get(context.depth);
		if (list != null) {
			list.remove(context);
			if (list.isEmpty()) {
				CONTEXTS.remove(context.depth, list);
			}
		}

		CURRENT_CONTEXT.set(context.parent);

		return true;
	}


	/**
	 * Pushes a function call frame onto the call stack.
	 *
	 * @param function  the {@link DeclaredFunction} being called
	 * @param arguments the map of argument names to values
	 */
	public static void pushCall(DeclaredFunction function, Map<String, Object> arguments) {
		CALL_STACK.get().push(ImmutableTriple.of(function, arguments, null));
	}

	/**
	 * Pops the most recent function call frame from the call stack.
	 *
	 * @return the popped function call frame as a pair containing function, arguments, and return value
	 * @throws NaftahBugError if the call stack is empty
	 */
	public static Triple<DeclaredFunction, Map<String, Object>, Object> popCall() {
		if (CALL_STACK.get().isEmpty()) {
			throw new NaftahBugError("حالة غير قانونية: لا يمكن إزالة عنصر من مكدس استدعاءات الدوال الفارغ.");
		}
		return CALL_STACK.get().pop();
	}

	/**
	 * Returns (without removing) the most recent function call frame from the call stack.
	 *
	 * @return the top function call frame, or {@code null} if the stack is empty
	 */
	public static Triple<DeclaredFunction, Map<String, Object>, Object> peekCall() {
		return CALL_STACK.get().peek();
	}

	/**
	 * Retrieves the current loop label based on the given label context or generates a unique one.
	 *
	 * @param label the parsed label, may be {@code null}
	 * @param depth the depth to use for label generation if none provided
	 * @return the current loop label as a string
	 */
	public static String currentLoopLabel(String label, int depth) {
		return Objects.nonNull(label) ? label : generateLoopId(depth);
	}

	/**
	 * Pushes a loop label and its associated parser context onto the loop stack.
	 *
	 * @param label   the loop label
	 * @param loopCtx the parser context associated with the loop
	 * @param <T>     the type of the parser context
	 */
	public static <T extends ParserRuleContext> void pushLoop(String label, T loopCtx) {
		LOOP_STACK.get().push(ImmutablePair.of(label, loopCtx));
	}

	/**
	 * Initializes a new task scope for the current thread.
	 *
	 * <p>All tasks spawned after this call will be tracked in this scope and can
	 * later be awaited or cleaned up. Must be called before spawning tasks if
	 * no scope is active.</p>
	 */
	public static void startScope() {
		if (Objects.isNull(CURRENT_TASK_SCOPE)) {
			CURRENT_TASK_SCOPE = new ThreadLocal<>();
		}
		CURRENT_TASK_SCOPE.set(new ArrayList<>());
	}

	/**
	 * Retrieves and clears the thread-local list of JVM functions for the current lookup context.
	 * <p>
	 * This method returns the current thread's {@link JvmFunction} list stored in
	 * {@code CURRENT_LOOKUP_JVM_FUNCTIONS} and removes it from the thread-local storage,
	 * preventing accidental reuse across lookups.
	 *
	 * @return the list of {@link JvmFunction} for the current lookup, or {@code null} if none is set
	 */
	public static List<JvmFunction> getCurrentLookupJvmFunctions() {
		if (Objects.isNull(CURRENT_LOOKUP_JVM_FUNCTIONS)) {
			return null;
		}
		var result = CURRENT_LOOKUP_JVM_FUNCTIONS.get();
		CURRENT_LOOKUP_JVM_FUNCTIONS.remove();
		return result;
	}

	/**
	 * Sets the thread-local list of JVM functions for the current lookup context.
	 * <p>
	 * If the thread-local variable {@code CURRENT_LOOKUP_JVM_FUNCTIONS} is not initialized,
	 * it is created. This allows subsequent calls to {@link #getCurrentLookupJvmFunctions()}
	 * to retrieve the list in the same thread.
	 *
	 * @param lookupJvmFunctions the list of {@link JvmFunction} to store for the current thread
	 */
	public static void setCurrentLookupJvmFunctions(List<JvmFunction> lookupJvmFunctions) {
		if (Objects.isNull(CURRENT_LOOKUP_JVM_FUNCTIONS)) {
			CURRENT_LOOKUP_JVM_FUNCTIONS = new ThreadLocal<>();
		}
		CURRENT_LOOKUP_JVM_FUNCTIONS.set(lookupJvmFunctions);
	}

	/**
	 * Retrieves and clears the thread-local list of JVM class initializers for the current lookup context.
	 * <p>
	 * This method returns the current thread's {@link JvmClassInitializer} list stored in
	 * {@code CURRENT_LOOKUP_JVM_CLASS_INITIALIZERS} and removes it from the thread-local storage
	 * to prevent accidental reuse across lookups.
	 *
	 * @return the list of {@link JvmClassInitializer} for the current lookup, or {@code null} if none is set
	 */
	public static List<JvmClassInitializer> getCurrentLookupJvmClassInitializers() {
		if (Objects.isNull(CURRENT_LOOKUP_JVM_CLASS_INITIALIZERS)) {
			return null;
		}
		var result = CURRENT_LOOKUP_JVM_CLASS_INITIALIZERS.get();
		CURRENT_LOOKUP_JVM_CLASS_INITIALIZERS.remove();
		return result;
	}

	/**
	 * Sets the thread-local list of JVM class initializers for the current lookup context.
	 * <p>
	 * If the thread-local variable {@code CURRENT_LOOKUP_JVM_CLASS_INITIALIZERS} is not initialized,
	 * it is created. This allows subsequent calls to {@link #getCurrentLookupJvmClassInitializers()}
	 * to retrieve the list in the same thread.
	 *
	 * @param lookupJvmClassInitializers the list of {@link JvmClassInitializer} to store for the current thread
	 */
	public static void setCurrentLookupJvmClassInitializers(List<JvmClassInitializer> lookupJvmClassInitializers) {
		if (Objects.isNull(CURRENT_LOOKUP_JVM_CLASS_INITIALIZERS)) {
			CURRENT_LOOKUP_JVM_CLASS_INITIALIZERS = new ThreadLocal<>();
		}
		CURRENT_LOOKUP_JVM_CLASS_INITIALIZERS.set(lookupJvmClassInitializers);
	}

	/**
	 * Finalizes the current task scope for the thread.
	 *
	 * <p>Removes all tasks tracked in the current scope and clears the thread-local
	 * reference. After this call, no tasks are associated with the scope until
	 * {@link #startScope()} is called again.</p>
	 */
	public static void endScope() {
		CURRENT_TASK_SCOPE.remove();
	}

	/**
	 * Returns a list of all loop labels currently in the loop stack.
	 *
	 * @return a list of loop labels
	 */
	public static List<String> getLoopLabels() {
		return LOOP_STACK.get().stream().map(Pair::getLeft).toList();
	}

	/**
	 * Checks if the loop stack contains a loop with the given label.
	 *
	 * @param label the label to check
	 * @return {@code true} if the loop stack contains the label, {@code false} otherwise
	 */
	public static boolean loopContainsLabel(String label) {
		return LOOP_STACK.get().stream().anyMatch(stringPair -> stringPair.getLeft().equals(label));
	}

	/**
	 * Pops the most recent loop label and context from the loop stack.
	 *
	 * @return the popped loop label and parser context pair
	 * @throws NaftahBugError if the loop stack is empty
	 */
	public static Pair<String, ? extends ParserRuleContext> popLoop() {
		if (LOOP_STACK.get().isEmpty()) {
			throw new NaftahBugError("حالة غير قانونية: لا يمكن إزالة عنصر من مكدس الحلقات الفارغ.");
		}
		return LOOP_STACK.get().pop();
	}

	/**
	 * Updates the context static fields from the given {@link ClassScanningResult}.
	 * Marks bootstrapping as completed.
	 *
	 * @param result the class scanning result containing loaded class information
	 */
	protected static void setContextFromClassScanningResult(ClassScanningResult result) {
		CLASS_NAMES = Set.copyOf(result.getClassNames().keySet());
		CLASS_QUALIFIERS = Set.copyOf(result.getClassQualifiers());
		ARABIC_CLASS_QUALIFIERS = Map.copyOf(result.getArabicClassQualifiers());
		if (Objects.nonNull(result.getClasses())) {
			CLASSES = Map.copyOf(result.getClasses());
		}
		if (Objects.nonNull(result.getAccessibleClasses())) {
			ACCESSIBLE_CLASSES = Map.copyOf(result.getAccessibleClasses());
		}
		if (Objects.nonNull(result.getInstantiableClasses())) {
			INSTANTIABLE_CLASSES = Map.copyOf(result.getInstantiableClasses());
		}
		if (Objects.nonNull(result.getJvmClassInitializers())) {
			JVM_CLASS_INITIALIZERS = Map.copyOf(result.getJvmClassInitializers());
		}
		if (Objects.nonNull(result.getJvmFunctions())) {
			JVM_FUNCTIONS = Map.copyOf(result.getJvmFunctions());
		}
		if (Objects.nonNull(result.getBuiltinFunctions())) {
			setBuiltinFunctions(Collections.unmodifiableMap(result.getBuiltinFunctions()));
		}
		BOOT_STRAPPED = true;
	}

	/**
	 * Loads the default builtin functions into the context.
	 */
	public static void defaultBootstrap() {
		setBuiltinFunctions(getBuiltinMethods(Builtin.class)
				.stream()
				.collect(toAliasGroupedByName()));

		Set<Class<?>> builtinClasses = new HashSet<>();

		String builtinPropClasses = System.getProperty(BUILTIN_CLASSES_PROPERTY);

		if (Objects.nonNull(builtinPropClasses)) {
			processBuiltin(builtinPropClasses, className -> {
				try {
					Class<?> clazz = Class.forName(className);
					builtinClasses.add(clazz);
				}
				catch (ClassNotFoundException e) {
					padText("تعذر العثور على الفئة(class): " + className, true);
				}
			});
		}

		String builtinPropPackages = System.getProperty(BUILTIN_PACKAGES_PROPERTY);

		if (Objects.nonNull(builtinPropPackages)) {
			processBuiltin(builtinPropPackages, packageName -> {
				try {
					var classNames = RuntimeClassScanner.scanPackageCLasses(packageName);
					var classes = RuntimeClassScanner.loadClasses(classNames, false);
					builtinClasses.addAll(classes.values());
				}
				catch (Exception e) {
					padText("تعذر العثور على الحزمة(package): " + packageName, true);
				}
			});
		}

		if (!builtinClasses.isEmpty()) {
			putAllInBuiltinFunctions(getBuiltinMethods(builtinClasses)
					.stream()
					.collect(toAliasGroupedByName()));
		}
		// Locking the Builtin function, cannot be changed anymore
		setBuiltinFunctions(Collections.unmodifiableMap(getBuiltinFunctions()));
	}

	/**
	 * Processes a comma-separated list of built-in property names by applying the specified
	 * {@link java.util.function.Consumer} to each non-empty, trimmed element.
	 * <p>
	 * This method splits the given string by commas (<code>,</code>), trims whitespace from each part,
	 * filters out any empty entries, and then passes each resulting value to the provided consumer.
	 * </p>
	 *
	 * <h3>Example:</h3>
	 * <pre>{@code
	 * processBuiltin("foo, bar, baz", System.out::println);
	 * // Output:
	 * // foo
	 * // bar
	 * // baz
	 * }</pre>
	 *
	 * @param builtinProp     a comma-separated string of property names; may contain whitespace
	 * @param builtinConsumer a {@link java.util.function.Consumer} that will process each non-empty, trimmed property
	 *                        name
	 * @throws NullPointerException if {@code builtinProp} or {@code builtinConsumer} is {@code null}
	 */
	private static void processBuiltin( String builtinProp,
										Consumer<String> builtinConsumer) {
		Arrays
				.stream(builtinProp.split(","))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.forEach(builtinConsumer);
	}

	/**
	 * Calls the loader task to scan and load classes, optionally asynchronously.
	 *
	 * @param async {@code true} to run asynchronously, {@code false} to run synchronously
	 */
	protected static void callLoader(   boolean async,
										Supplier<ClassScanningResult> loaderTask,
										BiConsumer<? super ClassScanningResult, ? super Throwable> loaderConsumer) {
		if (async) {
			CompletableFuture.supplyAsync(loaderTask).whenComplete(loaderConsumer);
		}
		else {
			ClassScanningResult classScanningResult = null;
			Throwable thr = null;
			try {
				startLoader("""
							تحضير فئات مسار فئات جافا (Java classpath) ومعالجتها لإعادة استخدامها داخل سكربت نفطه. قد يستغرق الأمر عدة دقائق حسب الإعدادات."""
				);
				classScanningResult = loaderTask.get();
				stopLoader();
			}
			catch (Throwable throwable) {
				thr = throwable;
			}
			finally {
				loaderConsumer.accept(classScanningResult, thr);
			}
		}
	}

	/**
	 * Serializes the class scanning result to a cache file.
	 *
	 * @param result    the class scanning result to serialize
	 * @param cachePath the file path where the Base64 string should be saved
	 * @throws NaftahBugError if serialization fails
	 */
	protected static void serializeClassScanningResult(ClassScanningResult result, Path cachePath) {
		try {
			var path = Base64SerializationUtils.serialize(result, cachePath);
			if (Boolean.getBoolean(DEBUG_PROPERTY) || Boolean.getBoolean(INSIDE_INIT_PROPERTY)) {
				padText("تم حفظ البيانات في: " + path, true);
			}
		}
		catch (IOException e) {
			throw new NaftahBugError(e);
		}
	}

	/**
	 * Attempts to deserialize a previously cached class scanning result.
	 * If deserialization fails, triggers class loading asynchronously if configured.
	 *
	 * @param cachePath the file path where the Base64 string should be saved
	 */
	protected static void deserializeClassScanningResult(Path cachePath) {
		try {
			var result = (ClassScanningResult) Base64SerializationUtils.deserialize(cachePath);
			setContextFromClassScanningResult(result);
		}
		catch (Exception e) {
			callLoader(ASYNC_BOOT_STRAP, LOADER_TASK, LOADER_CONSUMER);
		}
	}

	/**
	 * Returns a list of all completion candidates combining builtin functions, JVM functions, and instantiable
	 * classes.
	 *
	 * @return list of completion names
	 */
	public static List<String> getCompletions() {
		if (Objects.isNull(BUILTIN_FUNCTIONS)) {
			defaultBootstrap();
		}
		var runtimeCompletions = new ArrayList<>(BUILTIN_FUNCTIONS.keySet());

		if (Boolean.getBoolean(INCLUDE_ALL_IN_COMPLETIONS_PROPERTY)) {
			Optional
					.ofNullable(JVM_FUNCTIONS)
					.ifPresent(stringListMap -> runtimeCompletions.addAll(stringListMap.keySet()));
			Optional
					.ofNullable(INSTANTIABLE_CLASSES)
					.ifPresent(stringListMap -> runtimeCompletions.addAll(stringListMap.keySet()));
		}

		return runtimeCompletions;
	}

	/**
	 * Performs the bootstrap process to load classes and functions, either synchronously or asynchronously, based on
	 * configuration flags.
	 *
	 * @param async {@code true} for asynchronous bootstrap, {@code false} for synchronous
	 */
	public static void bootstrap(boolean async) {
		if (Boolean.getBoolean(DEBUG_PROPERTY) || Boolean.getBoolean(INSIDE_INIT_PROPERTY) || Boolean
				.getBoolean(INSIDE_MAN_PROPERTY)) {
			padText("تحضير فئات مسار فئات جافا (Java classpath)...", true);
		}
		SHOULD_BOOT_STRAP = Boolean.getBoolean(SCAN_CLASSPATH_PROPERTY);
		ASYNC_BOOT_STRAP = async;
		long start = System.nanoTime();
		FORCE_BOOT_STRAP = Boolean.getBoolean(FORCE_CLASSPATH_PROPERTY);
		if (SHOULD_BOOT_STRAP) {
			try {
				Files.createDirectories(CACHE_PATH.getParent());
			}
			catch (IOException e) {
				throw new NaftahBugError(e);
			}

			if (FORCE_BOOT_STRAP || !Files.exists(CACHE_PATH)) {
				callLoader(ASYNC_BOOT_STRAP, LOADER_TASK, LOADER_CONSUMER);
			}
			else {
				deserializeClassScanningResult(CACHE_PATH);
			}
		}
		else {
			defaultBootstrap();
			try {
				Files.createDirectories(MINIMAL_CACHE_PATH.getParent());
			}
			catch (IOException e) {
				throw new NaftahBugError(e);
			}
			if (FORCE_BOOT_STRAP || !Files.exists(MINIMAL_CACHE_PATH)) {
				callLoader(false, MINIMAL_LOADER_TASK, MINIMAL_LOADER_CONSUMER);
			}
			else {
				deserializeClassScanningResult(MINIMAL_CACHE_PATH);
			}
		}
		if (Boolean.getBoolean(DEBUG_PROPERTY)) {
			long end = System.nanoTime();
			long elapsedMillis = (end - start) / 1_000_000; // convert to milliseconds

			padText("الزمن المستغرق للتمهيد: " + elapsedMillis + " مللي ثانية.", true);
		}
	}

	/**
	 * Gets the Java {@link Class} corresponding to the given qualified name from cached maps.
	 *
	 * @param qualifiedName the fully qualified class name
	 * @return the {@link Class} object if found, or {@link Object} class as fallback
	 */
	protected static Class<?> doGetJavaType(String qualifiedName) {
		if (INSTANTIABLE_CLASSES.containsKey(qualifiedName)) {
			return INSTANTIABLE_CLASSES.get(qualifiedName);
		}
		if (ACCESSIBLE_CLASSES.containsKey(qualifiedName)) {
			return ACCESSIBLE_CLASSES.get(qualifiedName);
		}
		if (CLASSES.containsKey(qualifiedName)) {
			return CLASSES.get(qualifiedName);
		}
		return Object.class;
	}

	/**
	 * Gets the Java {@link Class} corresponding to the given qualified name.
	 * Blocks if the context is bootstrapping.
	 *
	 * @param qualifiedName the fully qualified class name
	 * @return the {@link Class} object if found, or {@link Object} class as fallback
	 */
	public static Class<?> getJavaType(String qualifiedName) {
		if (SHOULD_BOOT_STRAP && !BOOT_STRAP_FAILED) {
			while (!BOOT_STRAPPED && (Objects.isNull(INSTANTIABLE_CLASSES) || Objects
					.isNull(ACCESSIBLE_CLASSES) || Objects
							.isNull(
									CLASSES))) {
				// block the execution until bootstrapped
				if (BOOT_STRAP_FAILED) {
					return Object.class;
				}
			}
			return doGetJavaType(qualifiedName);
		}
		return Object.class;
	}

	/**
	 * Creates a new {@link NaftahBugError} indicating that a variable was not found
	 * in the current context.
	 *
	 * <p>The error message is in Arabic and includes the variable name in the form:
	 * <br>
	 * {@code "المتغير '%s' غير موجود في السياق الحالي."}
	 * </p>
	 *
	 * <p>This overload does not include line or column information and uses
	 * {@code -1} for both.</p>
	 *
	 * @param name the name of the variable that was not found
	 * @return a new {@link NaftahBugError} with the formatted message
	 */
	public static NaftahBugError newNaftahBugVariableNotFoundError(String name) {
		return newNaftahBugVariableNotFoundError(name, -1, -1);
	}

	/**
	 * Creates a new {@link NaftahBugError} indicating that a variable was not found
	 * at a specific source location.
	 *
	 * <p>The error message is in Arabic and includes the variable name in the form:
	 * <br>
	 * {@code "المتغير '%s' غير موجود في السياق الحالي."}
	 * </p>
	 *
	 * @param name   the name of the variable that was not found
	 * @param line   the source line number where the error occurred, or {@code -1} if unknown
	 * @param column the source column number where the error occurred, or {@code -1} if unknown
	 * @return a new {@link NaftahBugError} with the formatted message and position data
	 */
	public static NaftahBugError newNaftahBugVariableNotFoundError( String name,
																	int line,
																	int column) {
		return new NaftahBugError("المتغير '%s' غير موجود في السياق الحالي.".formatted(name), line, column);
	}

	/**
	 * Creates a NaftahBugError for a duplicate (foreach) target with specified line and column.
	 *
	 * @param name   The name of the duplicated element.
	 * @param line   The line number where the duplicate occurs.
	 * @param column The column number where the duplicate occurs.
	 * @return NaftahBugError with a detailed message about the duplicate.
	 */
	public static NaftahBugError newNaftahBugForeachTargetDuplicatesError(  String name,
																			int line,
																			int column) {
		return new NaftahBugError(
									"هدف حلقة (foreach) لا يمكن أن يحتوي على عناصر مكررة. إعلان مكرر ل'%s'."
											.formatted(name),
									line,
									column);
	}

	/**
	 * Creates a NaftahBugError when there is a duplicate in a (foreach) loop target.
	 *
	 * @param name                 The name of the duplicated element.
	 * @param foreachTargetContext The context of the (foreach) target from the Naftah parser.
	 * @return NaftahBugError containing line and column information for the duplicate element.
	 */
	public static NaftahBugError newNaftahBugForeachTargetDuplicatesError(  String name,
																			org.daiitech.naftah.parser.NaftahParser.ForeachTargetContext foreachTargetContext) {
		return newNaftahBugForeachTargetDuplicatesError(
														name,
														foreachTargetContext.getStart().getLine(),
														foreachTargetContext.getStart().getCharPositionInLine());
	}

	/**
	 * Creates a NaftahBugError when a function argument already exists in the current context.
	 *
	 * @param name The name of the duplicated argument.
	 * @return NaftahBugError indicating the argument already exists.
	 */
	public static NaftahBugError newNaftahBugExistentFunctionArgumentError(String name) {
		return new NaftahBugError("الوسيط '%s' موجود في السياق الحالي للدالة. لا يمكن إعادة إعلانه.".formatted(name));
	}

	/**
	 * Creates a NaftahBugError when a function parameter already exists in the current context.
	 *
	 * @param name The name of the duplicated parameter.
	 * @return NaftahBugError indicating the parameter already exists.
	 */
	public static NaftahBugError newNaftahBugExistentFunctionParameterError(String name) {
		return new NaftahBugError("المعامل '%s' موجود في السياق الحالي للدالة. لا يمكن إعادة إعلانه.".formatted(name));
	}

	/**
	 * Creates a NaftahBugError when a function already exists in the current context.
	 *
	 * @param name The name of the duplicated function.
	 * @return NaftahBugError indicating the function already exists.
	 */
	public static NaftahBugError newNaftahBugExistentFunctionError(String name) {
		return new NaftahBugError("الدالة '%s' موجودة في السياق الحالي. لا يمكن إعادة إعلانها.".formatted(name));
	}

	/**
	 * Creates a {@link NaftahBugError} indicating that an implementation with the given name
	 * already exists in the current context.
	 *
	 * @param name the name of the duplicated implementation
	 * @return a {@code NaftahBugError} describing the duplication
	 */
	public static NaftahBugError newNaftahBugExistentImplementationError(String name) {
		return new NaftahBugError("السلوك '%s' موجود في السياق الحالي. لا يمكن إعادة إعلانه.".formatted(name));
	}

	/**
	 * Creates a NaftahBugError when a variable already exists in the current context.
	 *
	 * @param name The name of the duplicated variable.
	 * @return NaftahBugError indicating the variable already exists.
	 */
	public static NaftahBugError newNaftahBugExistentVariableError(String name) {
		return new NaftahBugError("المتغير '%s' موجود في السياق الحالي. لا يمكن إعادة إعلانه.".formatted(name));
	}

	/**
	 * Adds all entries from the given map into the global built-in functions map.
	 * <p>
	 * This method is synchronized to ensure thread-safe updates.
	 * </p>
	 *
	 * @param builtinFunctions a map of function names to lists of {@link BuiltinFunction} to add.
	 */
	public static synchronized void putAllInBuiltinFunctions(Map<String, List<BuiltinFunction>> builtinFunctions) {
		BUILTIN_FUNCTIONS
				.putAll(builtinFunctions);
	}

	/**
	 * Returns the global map of built-in functions.
	 *
	 * @return an unmodifiable view or reference to the map of built-in functions.
	 */
	public static Map<String, List<BuiltinFunction>> getBuiltinFunctions() {
		return BUILTIN_FUNCTIONS;
	}

	/**
	 * Replaces the global built-in functions map with the provided one.
	 * <p>
	 * This method is synchronized to ensure thread-safe updates.
	 * </p>
	 *
	 * @param builtinFunctions the new map of built-in functions to set.
	 */
	public static synchronized void setBuiltinFunctions(Map<String, List<BuiltinFunction>> builtinFunctions) {
		BUILTIN_FUNCTIONS = builtinFunctions;
	}

	/**
	 * Returns the global map of JVM functions.
	 *
	 * @return a map of function names to lists of {@link JvmFunction}.
	 */
	public static Map<String, List<JvmFunction>> getJvmFunctions() {
		return JVM_FUNCTIONS;
	}

	public static Map<String, List<JvmClassInitializer>> getJvmClassInitializers() {
		return JVM_CLASS_INITIALIZERS;
	}

	/**
	 * Returns the global map of all registered classes.
	 *
	 * @return a map of class names to {@link Class} objects.
	 */
	public static Map<String, Class<?>> getClasses() {
		return CLASSES;
	}

	/**
	 * Returns the global map of classes that are marked as accessible.
	 *
	 * @return a map of class names to {@link Class} objects which are accessible.
	 */
	public static Map<String, Class<?>> getAccessibleClasses() {
		return ACCESSIBLE_CLASSES;
	}

	/**
	 * Returns the global map of classes that can be instantiated.
	 *
	 * @return a map of class names to {@link Class} objects that can be instantiated.
	 */
	public static Map<String, Class<?>> getInstantiableClasses() {
		return INSTANTIABLE_CLASSES;
	}

	/**
	 * Defines an import within the given {@link DefaultContext}, handling both
	 * block-level and global imports.
	 * <p>
	 * If the provided {@code ctx} (parse context) is part of a block (i.e., has a
	 * parent of type {@code NaftahParser.BlockContext}), the import is registered
	 * as a block-level import within the current context. Otherwise, the import is
	 * added to the global {@code IMPORTS} map.
	 * </p>
	 *
	 * @param currentContext the active {@link DefaultContext} where the import should be defined
	 * @param ctx            the current {@link ParserRuleContext} representing the parse location
	 * @param alias          the alias to associate with the imported element (e.g., a shorthand name)
	 * @param importElement  the fully qualified name or path of the element being imported
	 */
	public static void defineImport(DefaultContext currentContext,
									ParserRuleContext ctx,
									String alias,
									String importElement) {
		if (hasAnyParentOfType(ctx, org.daiitech.naftah.parser.NaftahParser.BlockContext.class)) {
			currentContext.defineBlockImport(alias, importElement);
		}
		else {
			IMPORTS.put(alias, importElement);
		}
	}

	/**
	 * Clears all static {@link ThreadLocal} fields used by this class,
	 * removing global per-thread interpreter state such as the execution
	 * call stack, loop stack, and the currently active evaluation context.
	 *
	 * <p>This method should be called when global per-thread state is no
	 * longer needed—for example, when shutting down an interpreter instance,
	 * resetting internal state between isolated evaluations, or cleaning up
	 * after thread reuse in pooled environments.</p>
	 *
	 * <p>Only thread-local data for the current thread is removed. This method
	 * does not modify shared global structures or affect the state of other
	 * threads. If the class maintains optional ThreadLocal fields, they are
	 * removed only when non-null to ensure safe cleanup regardless of
	 * initialization order.</p>
	 */
	public static void cleanClassThreadLocals() {
		CALL_STACK.remove();
		LOOP_STACK.remove();
		CURRENT_CONTEXT.remove();
		if (Objects.nonNull(CURRENT_TASK_SCOPE)) {
			CURRENT_TASK_SCOPE.remove();
		}
		if (Objects.nonNull(CURRENT_LOOKUP_JVM_FUNCTIONS)) {
			CURRENT_LOOKUP_JVM_FUNCTIONS.remove();
		}
		if (Objects.nonNull(CURRENT_LOOKUP_JVM_CLASS_INITIALIZERS)) {
			CURRENT_LOOKUP_JVM_CLASS_INITIALIZERS.remove();
		}
	}

	/**
	 * Looks up JVM class initializers by the Arabic-transliterated qualified class name.
	 * <p>
	 * If the class is found, it loads the corresponding {@link Class} object, retrieves
	 * its constructors as {@link JvmClassInitializer} instances, stores them in the
	 * thread-local {@link #CURRENT_LOOKUP_JVM_CLASS_INITIALIZERS}, and returns {@code true}.
	 * Otherwise, it returns {@code false}.
	 *
	 * @param arabicQualifiedName the Arabic-transliterated fully qualified class name
	 * @return {@code true} if the class and its initializers were successfully found; {@code false} otherwise
	 */
	public static boolean lookupJvmClassInitializer(String arabicQualifiedName) {
		String className;
		if (Objects.nonNull(className = ARABIC_CLASS_QUALIFIERS.get(arabicQualifiedName))) {
			try {
				Class<?> clazz = Class.forName(className);
				var jvmClassInitializers = ClassUtils
						.getClassConstructors(  arabicQualifiedName,
												clazz);
				setCurrentLookupJvmClassInitializers(jvmClassInitializers);
				return true;
			}
			catch (ClassNotFoundException e) {
				return false;
			}
		}
		return false;
	}

	/**
	 * Looks up JVM functions by the Arabic-transliterated qualified call.
	 * <p>
	 * The qualified call should contain the class and method name separated by
	 * {@link ClassUtils#QUALIFIED_CALL_SEPARATOR}. If the class is found and contains the
	 * corresponding method, the matching {@link JvmFunction} instances are stored
	 * in the thread-local {@link #CURRENT_LOOKUP_JVM_FUNCTIONS} and {@code true} is returned.
	 *
	 * @param arabicQualifiedCall the Arabic-transliterated fully qualified call (class + method)
	 * @return {@code true} if the function(s) were successfully found; {@code false} otherwise
	 */
	public static boolean lookupJvmFunctions(String arabicQualifiedCall) {
		String[] arabicQualifiedCallParts;
		String className;
		if ((arabicQualifiedCallParts = arabicQualifiedCall.split(QUALIFIED_CALL_SEPARATOR)).length == 2 && Objects
				.nonNull(className = ARABIC_CLASS_QUALIFIERS.get(arabicQualifiedCallParts[0]))) {
			try {
				Class<?> clazz = Class.forName(className);
				var jvmFunctions = ClassUtils
						.getClassMethods(   arabicQualifiedCallParts[0],
											clazz,
											method -> arabicQualifiedCallParts[1]
													.equals(ArabicUtils
															.transliterateToArabicScriptDefault(
																								method.getName())[0]));
				setCurrentLookupJvmFunctions(jvmFunctions);
				return true;
			}
			catch (ClassNotFoundException e) {
				return false;
			}
		}
		return false;
	}

	/**
	 * Checks whether a JVM class initializer exists for the specified qualified name.
	 * <p>
	 * If {@link #SHOULD_BOOT_STRAP} is enabled, this checks both the bootstrap-loaded
	 * initializers and performs a lookup if necessary. Otherwise, it checks the current
	 * thread-local lookup context.
	 *
	 * @param qualifiedName the Arabic-transliterated qualified class name
	 * @return {@code true} if a class initializer exists; {@code false} otherwise
	 */
	public boolean containsJvmClassInitializer(String qualifiedName) {
		return qualifiedName
				.matches(
							QUALIFIED_NAME_REGEX) && (SHOULD_BOOT_STRAP ?
									(!BOOT_STRAP_FAILED && BOOT_STRAPPED && JVM_CLASS_INITIALIZERS != null && JVM_CLASS_INITIALIZERS
											.containsKey(
															qualifiedName)) :
									(!BOOT_STRAP_FAILED && BOOT_STRAPPED && lookupJvmClassInitializer(
																										qualifiedName)));
	}

	/**
	 * Retrieves the JVM class initializer(s) for the specified Arabic-transliterated
	 * qualified name.
	 * <p>
	 * Depending on the bootstrap and lookup state, this method retrieves initializers
	 * from the global {@link #JVM_CLASS_INITIALIZERS} map or from the current
	 * thread-local lookup context. If only one initializer exists, it is returned
	 * directly; otherwise, the list of initializers is returned.
	 *
	 * @param arabicQualifiedName the Arabic-transliterated qualified class name
	 * @param safe                if {@code false}, throws {@link NaftahBugError} if no initializer is found; if
	 *                            * {@code true}, returns {@code null} instead
	 * @return a {@link Pair} containing the current depth and the initializer(s), or {@code null} if not found and
	 *         * {@code safe} is {@code true}
	 * @throws NaftahBugError if the initializer does not exist and {@code safe} is {@code false}
	 */
	public Pair<Integer, Object> getJvmClassInitializer(String arabicQualifiedName, boolean safe) {
		if (SHOULD_BOOT_STRAP && !BOOT_STRAP_FAILED) {
			if (BOOT_STRAPPED && JVM_CLASS_INITIALIZERS.containsKey(arabicQualifiedName)) {
				var jvmClassInitializers = JVM_CLASS_INITIALIZERS.get(arabicQualifiedName);
				return ImmutablePair
						.of(depth,
							jvmClassInitializers.size() == 1 ? jvmClassInitializers.get(0) : jvmClassInitializers);
			}
			else if (arabicQualifiedName.matches(QUALIFIED_NAME_REGEX)) {
				while (!BOOT_STRAPPED && Objects.isNull(JVM_CLASS_INITIALIZERS)) {
					// block the execution until bootstrapped
					if (BOOT_STRAP_FAILED) {
						return null;
					}
				}
				var jvmClassInitializers = JVM_CLASS_INITIALIZERS.get(arabicQualifiedName);
				return ImmutablePair
						.of(depth,
							jvmClassInitializers.size() == 1 ? jvmClassInitializers.get(0) : jvmClassInitializers);
			}
		}
		else if (!BOOT_STRAP_FAILED && BOOT_STRAPPED && Objects.nonNull(CURRENT_LOOKUP_JVM_CLASS_INITIALIZERS)) {
			var jvmClassInitializers = getCurrentLookupJvmClassInitializers();
			if (Objects.nonNull(jvmClassInitializers)) {
				return ImmutablePair
						.of(depth,
							jvmClassInitializers.size() == 1 ? jvmClassInitializers.get(0) : jvmClassInitializers);
			}
		}

		if (!safe) {
			throw new NaftahBugError("المُستدعى '%s' غير موجودة في السياق الحالي.".formatted(arabicQualifiedName));
		}
		return null;
	}

	/**
	 * Registers a newly created asynchronous task in both the current task scope
	 * and the associated execution context.
	 *
	 * <p>This method must be called whenever a task is spawned that logically
	 * belongs to this context. It performs two things:</p>
	 * <ol>
	 * <li>Increments the {@link #pendingTasks} counter to prevent premature
	 * deregistration of the context while the task is running.</li>
	 * <li>Adds the task to the current thread's task scope
	 * ({@link #CURRENT_TASK_SCOPE}) so it can be tracked and awaited
	 * when the scope ends.</li>
	 * </ol>
	 *
	 * <p>If no scope is currently active for this thread, the task is only
	 * registered in the context.</p>
	 *
	 * @param task the asynchronous task to register; must not be {@code null}
	 */
	public void registerTask(Task<?> task) {
		// Increment pending tasks for this context
		pendingTasks.incrementAndGet();

		// Add to the current thread's active scope if it exists
		List<Task<?>> currentTaskScope;
		if (Objects.nonNull(CURRENT_TASK_SCOPE) && Objects.nonNull(currentTaskScope = CURRENT_TASK_SCOPE.get())) {
			currentTaskScope.add(task);
		}
	}

	/**
	 * Marks a previously registered asynchronous task as completed.
	 *
	 * <p>This method performs two actions:</p>
	 * <ol>
	 * <li>Decrements the {@link #pendingTasks} counter, indicating that one
	 * of the tasks associated with this context has finished execution.</li>
	 * <li>If the context was previously marked for removal ({@link #pendingRemoval}),
	 * this triggers a deregistration attempt via {@link #deregisterContext(DefaultContext)}.</li>
	 * </ol>
	 *
	 * <p>The caller must ensure that {@code completeTask()} is invoked exactly once
	 * for every successful {@link #registerTask(Task)}, typically after the task
	 * finishes and its result has been consumed or awaited.</p>
	 *
	 * <p>Even when a task is tracked in a scope (via {@link #CURRENT_TASK_SCOPE}),
	 * this method should still be called to update the context's pending task count
	 * and enable proper lifecycle management.</p>
	 *
	 * @see #registerTask(Task)
	 * @see #deregisterContext(DefaultContext)
	 */
	public void completeTask() {
		pendingTasks.decrementAndGet();
		if (pendingRemoval) {
			deregisterContext(this);
		}
	}

	/**
	 * Clears all {@link ThreadLocal} values associated with this context.
	 * <p>
	 * This removes per-thread state such as variables, functions, implementations,
	 * temporary execution markers, and parsing-related metadata.
	 * </p>
	 *
	 * <p>
	 * This method should be called when a task executing under this context completes
	 * or when the context is being cleaned up. Proper invocation prevents memory
	 * leaks and ensures no stale state is retained across threads, which is
	 * especially important in asynchronous or multi-threaded environments where
	 * contexts may be reused or migrated.
	 * </p>
	 *
	 * <p>
	 * Only non-null {@code ThreadLocal} fields are removed, ensuring safe cleanup
	 * even if the context was only partially initialized.
	 * </p>
	 *
	 * <p>
	 * Calling this method affects only per-thread execution state. Shared state,
	 * global registries, or parent/child context relationships remain unchanged.
	 * </p>
	 */
	public void cleanThreadLocals() {
		this.variables.remove();
		this.functions.remove();
		this.implementations.remove();
		if (Objects.nonNull(this.functionCallId)) {
			this.functionCallId.remove();
		}
		if (Objects.nonNull(this.parsingFunctionCallId)) {
			this.parsingFunctionCallId.remove();
		}
		if (Objects.nonNull(this.parsingAssignment)) {
			this.parsingAssignment.remove();
		}
		if (Objects.nonNull(this.creatingObject)) {
			this.creatingObject.remove();
		}
		if (Objects.nonNull(this.awaitingTask)) {
			this.awaitingTask.remove();
		}
		if (Objects.nonNull(this.declarationOfAssignment)) {
			this.declarationOfAssignment.remove();
		}
		if (Objects.nonNull(this.loopLabel)) {
			this.loopLabel.remove();
		}
		if (Objects.nonNull(this.loopVariables)) {
			this.loopVariables.remove();
		}
		if (Objects.nonNull(this.parseTreeExecution)) {
			this.parseTreeExecution.remove();
		}
		if (Objects.nonNull(this.blockImports)) {
			this.blockImports.remove();
		}
		if (Objects.nonNull(this.parameters)) {
			this.parameters.remove();
		}
		if (Objects.nonNull(this.arguments)) {
			this.arguments.remove();
		}
	}

	/**
	 * Determines whether a variable with the given name exists in this context
	 * or in any of its ancestor contexts.
	 *
	 * <p>This method performs a hierarchical lookup: it first checks the current
	 * context's variable map, and if not found, recursively checks parent
	 * contexts. This allows child contexts to inherit variables from their
	 * enclosing execution scopes.</p>
	 *
	 * @param depth the depth of look up
	 * @param name  the name of the variable to look up
	 * @return {@code true} if the variable exists in this context or any parent
	 *         context; {@code false} otherwise
	 */
	public boolean containsVariable(String name, int depth) {
		return ((depth == -1 || this.depth >= depth) && variables
				.get()
				.containsKey(name)) || (Objects.nonNull(parent) && parent.containsVariable(name, depth));
	}

	/**
	 * Checks whether a variable with the given name exists in the provided local
	 * variable map or in any sibling contexts.
	 *
	 * <p>This method performs a “horizontal” lookup: it does not traverse upward
	 * to parent contexts but instead inspects the current context's variable map
	 * (the provided {@code variableMap}) and then the variable maps of sibling
	 * contexts returned by {@link #getSiblings(boolean)}.</p>
	 *
	 * <p>This is useful in block-level or parallel-scope visibility rules where
	 * sibling contexts may share declarations that should not be inherited from
	 * parent scopes but must still be detected to prevent name conflicts.</p>
	 *
	 * @param variableMap  the variable map belonging to the current context
	 * @param variableName the variable name to check
	 * @return {@code true} if the variable exists locally or in any sibling
	 *         context; {@code false} otherwise
	 */
	public boolean containsLocalVariable(Map<String, DeclaredVariable> variableMap, String variableName) {
		List<DefaultContext> siblings;
		return variableMap.containsKey(variableName) || (Objects.nonNull(siblings = getSiblings(false)) && !siblings
				.isEmpty() && siblings.stream().anyMatch(sibling -> sibling.variables.get().containsKey(variableName)));
	}

	/**
	 * Retrieves a variable by name from the current context or parent contexts.
	 *
	 * @param name the variable name
	 * @param safe if true, returns null instead of throwing an error if not found
	 * @return a pair of the context depth and the DeclaredVariable instance
	 * @throws NaftahBugError if variable not found and safe is false
	 */
	public Pair<Integer, DeclaredVariable> getVariable(String name, boolean safe) {
		var variableMap = variables.get();
		List<DefaultContext> siblings;
		if (variableMap.containsKey(name)) {
			return ImmutablePair.of(depth, variableMap.get(name));
		}
		else if (isAwaitingTask() && Objects.nonNull(siblings = getSiblings(false)) && !siblings.isEmpty()) {
			for (DefaultContext sibling : siblings) {
				variableMap = sibling.variables.get();
				if (variableMap.containsKey(name)) {
					var variable = variableMap.get(name);
					if (variable.getValue() instanceof Task<?>) {
						return ImmutablePair.of(depth, variableMap.get(name));
					}
				}
			}
		}

		if (parent != null) {
			return parent.getVariable(name, safe);
		}

		if (!safe) {
			throw newNaftahBugVariableNotFoundError(name);
		}
		return null;
	}

	/**
	 * Sets the value of a variable in the current context or, if it exists, in a parent context.
	 * If the variable does not exist, it defines it in the current context.
	 *
	 * @param name  the variable name
	 * @param value the new DeclaredVariable value
	 */
	public DeclaredVariable setVariable(String name, DeclaredVariable value) {
		var variableMap = variables.get();
		if (variableMap.containsKey(name)) {
			return variableMap.put(name, value);
		}
		else if (parent != null && parent.containsVariable(name, value.getDepth())) {
			return parent.setVariable(name, value);
		}
		else {
			return variableMap.put(name, value); // define new in current context
		}
	}

	/**
	 * Defines a new variable in the current context.
	 *
	 * @param name  the variable name
	 * @param value the DeclaredVariable to define
	 * @throws NaftahBugError if the variable already exists in the current context
	 */
	public void defineVariable(String name, DeclaredVariable value) {
		var variableMap = variables.get();
		if (containsLocalVariable(variableMap, name) || containsVariable(name, value.getDepth())) {
			throw newNaftahBugExistentVariableError(name);
		}
		variableMap.put(name, value); // force local
	}

	/**
	 * Defines multiple variables in the current context.
	 *
	 * @param variables map of variable names to DeclaredVariable objects
	 * @throws NaftahBugError if any variable already exists in the current context
	 */
	public void defineVariables(Map<String, DeclaredVariable> variables) {
		var variableMap = this.variables.get();
		AtomicReference<String> varName = new AtomicReference<>();
		if (variables.entrySet().stream().anyMatch(declaredVariableEntry -> {
			var name = declaredVariableEntry.getKey();
			if (containsLocalVariable(variableMap, name) || containsVariable(   name,
																				declaredVariableEntry
																						.getValue()
																						.getDepth())) {
				varName.set(name);
				return true;
			}
			return false;
		})) {
			throw newNaftahBugExistentVariableError(varName.get());
		}
		variableMap.putAll(variables); // force local
	}

	/**
	 * Removes a variable from the current context.
	 * <p>
	 * If the variable does not exist and {@code lenient} is {@code false},
	 * an error is thrown. If {@code lenient} is {@code true}, the method
	 * will silently do nothing.
	 * </p>
	 *
	 * @param name    the name of the variable to remove
	 * @param lenient whether to suppress errors when the variable is not found
	 * @throws NaftahBugError if the variable does not exist and lenient is {@code false}
	 */
	public void removeVariable(String name, boolean lenient) {
		var variableMap = variables.get();
		if (!variableMap.containsKey(name)) {
			if (lenient) {
				return;
			}

			throw new NaftahBugError("المتغير '%s' غير موجود في السياق الحالي. لا يمكن إزالته.".formatted(name));
		}
		variableMap.remove(name);
	}

	/**
	 * Checks if the function with the given name exists in the current context, built-in functions,
	 * JVM functions (if bootstrapped), or any parent context.
	 *
	 * @param depth the depth of look up
	 * @param name  the function name
	 * @return true if the function exists, false otherwise
	 */
	public boolean containsFunction(String name, int depth) {
		return containsDeclaredFunction(name, depth) || containsDeclaredImplementation( name,
																						depth) || BUILTIN_FUNCTIONS != null && BUILTIN_FUNCTIONS
																								.containsKey(name) || (name
																										.matches(
																													QUALIFIED_CALL_REGEX) && (SHOULD_BOOT_STRAP ?
																															(!BOOT_STRAP_FAILED && BOOT_STRAPPED && JVM_FUNCTIONS != null && JVM_FUNCTIONS
																																	.containsKey(
																																					name)) :
																															(!BOOT_STRAP_FAILED && BOOT_STRAPPED && lookupJvmFunctions(name))));
	}

	/**
	 * Checks whether a function with the specified name exists in this context
	 * or any parent context up to the given depth.
	 *
	 * @param name  the name of the function to check
	 * @param depth the maximum depth to search; use -1 to ignore depth limits
	 * @return {@code true} if the function exists, {@code false} otherwise
	 */
	public boolean containsDeclaredFunction(String name, int depth) {
		return ((depth == -1 || this.depth >= depth) && functions
				.get()
				.containsKey(name)) || parent != null && parent.containsDeclaredFunction(name, depth);
	}

	/**
	 * Checks whether a declared implementation with the specified qualified call
	 * exists in this context or any parent context up to the given depth.
	 *
	 * @param qualifiedCall the qualified implementation call (format: "implId.functionName")
	 * @param depth         the maximum depth to search; use -1 to ignore depth limits
	 * @return {@code true} if the implementation exists, {@code false} otherwise
	 */
	public boolean containsDeclaredImplementation(String qualifiedCall, int depth) {
		return containsDeclaredImplementation(qualifiedCall, false, depth);
	}

	/**
	 * Checks whether a declared implementation with the specified qualified call
	 * exists in this context or any parent context up to the given depth.
	 *
	 * @param qualifiedCall      the qualified implementation call (format: "implId.functionName")
	 * @param implementationOnly if {@code true}, only checks for the implementation existence,
	 *                           ignoring specific function names
	 * @param depth              the maximum depth to search; use -1 to ignore depth limits
	 * @return {@code true} if the implementation exists according to the search rules,
	 *         {@code false} otherwise
	 */
	public boolean containsDeclaredImplementation(String qualifiedCall, boolean implementationOnly, int depth) {
		Map<String, DeclaredImplementation> implementationMap;
		String[] parts = qualifiedCall.split(QUALIFIED_CALL_SEPARATOR);
		String implementationId = parts.length == 2 ? parts[0] : null;
		return ((depth == -1 || this.depth >= depth) && Objects.nonNull(implementationId) && !implementationId
				.contains(
							QUALIFIED_NAME_SEPARATOR) && (implementationMap = implementations.get())
									.containsKey(implementationId) && (implementationOnly || implementationMap
											.get(implementationId)
											.getImplementationFunctions()
											.containsKey(parts[1]))) || parent != null && parent
													.containsDeclaredImplementation(qualifiedCall,
																					implementationOnly,
																					depth);
	}

	/**
	 * Retrieves the {@link DeclaredFunction} corresponding to a qualified implementation
	 * call from this context or any parent context.
	 *
	 * @param qualifiedCall the qualified implementation call (format: "implId.functionName")
	 * @param safe          if {@code true}, returns {@code null} when the function is not found;
	 *                      if {@code false}, throws an exception
	 * @return the corresponding {@link DeclaredFunction}, or {@code null} if not found and {@code safe} is {@code
	 * * true}
	 * @throws NaftahBugError if the function is not found and {@code safe} is {@code false}
	 */
	public DeclaredFunction getDeclaredImplementationFunction(String qualifiedCall, boolean safe) {
		Map<String, DeclaredImplementation> implementationMap;
		Map<String, DeclaredFunction> functionsMap;
		String[] parts = qualifiedCall.split(QUALIFIED_CALL_SEPARATOR);
		String implementationId = parts.length == 2 ? parts[0] : null;
		if (Objects.nonNull(implementationId) && !implementationId
				.contains(QUALIFIED_NAME_SEPARATOR) && (implementationMap = implementations.get())
						.containsKey(implementationId) && (functionsMap = implementationMap
								.get(implementationId)
								.getImplementationFunctions())
								.containsKey(parts[1])) {
			return functionsMap.get(parts[1]);
		}

		if (!safe) {
			throw newNaftahInvocableNotFoundError(qualifiedCall);
		}
		return null;
	}

	/**
	 * Retrieves a function by name from the current context, built-in functions, JVM functions,
	 * or parent contexts.
	 *
	 * @param name the function name
	 * @param safe if true, returns null instead of throwing an error if not found
	 * @return a pair of the context depth and the function object(s)
	 * @throws NaftahBugError if function not found and safe is false
	 */
	public Pair<Integer, Object> getFunction(String name, boolean safe) {
		var functionMap = functions.get();
		if (functionMap.containsKey(name)) {
			return ImmutablePair.of(depth, functionMap.get(name));
		}
		else if (containsDeclaredImplementation(name, depth)) {
			return ImmutablePair.of(depth, getDeclaredImplementationFunction(name, safe));
		}
		else if (parent != null) {
			return parent.getFunction(name, safe);
		}
		else { // root parent
			if (BUILTIN_FUNCTIONS.containsKey(name)) {
				var functions = BUILTIN_FUNCTIONS.get(name);
				return ImmutablePair.of(depth, functions.size() == 1 ? functions.get(0) : functions);
			}
			else if (SHOULD_BOOT_STRAP && !BOOT_STRAP_FAILED) {
				if (BOOT_STRAPPED && JVM_FUNCTIONS.containsKey(name)) {
					var functions = JVM_FUNCTIONS.get(name);
					return ImmutablePair.of(depth, functions.size() == 1 ? functions.get(0) : functions);
				}
				else if (name.matches(QUALIFIED_CALL_REGEX)) {
					while (!BOOT_STRAPPED && Objects.isNull(JVM_FUNCTIONS)) {
						// block the execution until bootstrapped
						if (BOOT_STRAP_FAILED) {
							return null;
						}
					}
					var functions = JVM_FUNCTIONS.get(name);
					return ImmutablePair.of(depth, functions.size() == 1 ? functions.get(0) : functions);
				}
			}
			else if (!BOOT_STRAP_FAILED && BOOT_STRAPPED && Objects.nonNull(CURRENT_LOOKUP_JVM_FUNCTIONS)) {
				var functions = getCurrentLookupJvmFunctions();
				if (Objects.nonNull(functions)) {
					return ImmutablePair.of(depth, functions.size() == 1 ? functions.get(0) : functions);
				}
			}
		}

		if (!safe) {
			throw newNaftahInvocableNotFoundError(name);
		}
		return null;
	}

	/**
	 * Sets the value of a function in the current context or, if it exists, in a parent context.
	 * If the function does not exist, it defines it in the current context.
	 *
	 * @param name  the function name
	 * @param value the new DeclaredFunction value
	 */
	public void setFunction(String name, DeclaredFunction value) {
		var functionMap = functions.get();
		if (functionMap.containsKey(name)) {
			functionMap.put(name, value);
		}
		else if (parent != null && parent.containsFunction(name, value.getDepth())) {
			parent.setFunction(name, value);
		}
		else {
			functionMap.put(name, value); // define new in current context
		}
	}

	/**
	 * Defines a new function in the current context.
	 * <p>
	 * The function is added to this context's local function registry. If a function
	 * with the same name already exists in the current context at the same depth,
	 * a {@link NaftahBugError} is thrown.
	 * </p>
	 *
	 * @param name  the name of the function to define
	 * @param value the {@link DeclaredFunction} to associate with the name
	 * @throws NaftahBugError if a function with the same name already exists
	 *                        in the current context
	 */
	public void defineFunction(String name, DeclaredFunction value) {
		if (containsFunction(name, value.getDepth())) {
			throw newNaftahBugExistentFunctionError(name);
		}
		functions.get().put(name, value); // force local
	}

	/**
	 * Sets or updates an implementation in the current context or in a parent context.
	 * <p>
	 * If the implementation already exists in the current context, it is updated.
	 * Otherwise, if a function with the same name exists in a parent context, the
	 * implementation is updated there. If neither exists, the implementation is
	 * defined locally in this context.
	 * </p>
	 *
	 * @param name  the name of the implementation
	 * @param value the {@link DeclaredImplementation} to set
	 */
	public void setImplementation(String name, DeclaredImplementation value) {
		var implementationMap = implementations.get();
		if (implementationMap.containsKey(name)) {
			implementationMap.put(name, value);
		}
		else if (parent != null && parent.containsFunction(name, value.getDepth())) {
			parent.setImplementation(name, value);
		}
		else {
			implementationMap.put(name, value); // define new in current context
		}
	}

	/**
	 * Defines a new implementation in the current context.
	 * <p>
	 * If an implementation with the same name already exists in this context (or
	 * matching the search criteria in the depth hierarchy), a
	 * {@link NaftahBugError} is thrown. Otherwise, the implementation is added
	 * to the current context's registry.
	 * </p>
	 *
	 * @param name  the name of the implementation to define
	 * @param value the {@link DeclaredImplementation} to associate with the name
	 * @throws NaftahBugError if the implementation already exists according to
	 *                        the context's depth rules
	 */
	public void defineImplementation(String name, DeclaredImplementation value) {
		if (containsDeclaredImplementation(name, true, value.getDepth())) {
			throw newNaftahBugExistentImplementationError(name);
		}
		implementations.get().put(name, value); // force local
	}

	/**
	 * Generates the canonical name for a function parameter based on the current function call ID.
	 *
	 * @param name the original parameter name
	 * @return the canonical function parameter name
	 */
	public String getFunctionParameterName(String name) {
		if (parameters == null) {
			parameters = SuppliedInheritableThreadLocal.withInitial(HashMap::new, HashMap::new);
		}
		String functionCallIdValue;
		if (functionCallId != null && (functionCallIdValue = functionCallId.get()) != null) {
			String functionName = functionCallIdValue.split("-")[1];
			name = DefaultContext.generateParameterOrArgumentName(functionName, name);
		}
		return name;
	}

	/**
	 * Checks if a function parameter with the given name exists in the current or parent contexts.
	 *
	 * @param depth the depth of look up
	 * @param name  the parameter name
	 * @return true if the function parameter exists, false otherwise
	 */
	public boolean containsFunctionParameter(String name, int depth) {
		String functionParameterName = getFunctionParameterName(name);
		return ((depth == -1 || this.depth >= depth) && parameters
				.get()
				.containsKey(functionParameterName)) || (parent != null && parent
						.containsFunctionParameter(name, depth));
	}

	/**
	 * Retrieves a function parameter by name from the current or parent contexts.
	 *
	 * @param name the parameter name
	 * @param safe if true, returns null instead of throwing an error if not found
	 * @return a pair of the context depth and the DeclaredParameter object
	 * @throws NaftahBugError if parameter not found and safe is false
	 */
	public Pair<Integer, DeclaredParameter> getFunctionParameter(String name, boolean safe) {
		String functionParameterName = getFunctionParameterName(name);
		var parameterMap = parameters.get();
		if (parameterMap.containsKey(functionParameterName)) {
			return ImmutablePair.of(depth, parameterMap.get(functionParameterName));
		}
		else if (parent != null) {
			return parent.getFunctionParameter(name, safe);
		}

		if (!safe) {
			throw new NaftahBugError("المعامل '%s' غير موجود في السياق الحالي للدالة.".formatted(name));
		}
		return null;
	}

	/**
	 * Sets the value of a function parameter in the current or parent context.
	 * Defines the parameter locally if it does not exist.
	 *
	 * @param name  the parameter name
	 * @param value the new DeclaredParameter value
	 */
	public void setFunctionParameter(String name, DeclaredParameter value) {
		String functionParameterName = getFunctionParameterName(name);
		var parameterMap = parameters.get();
		if (parameterMap.containsKey(functionParameterName)) {
			parameterMap.put(functionParameterName, value);
		}
		else if (parent != null && parent.containsFunctionParameter(name, value.getDepth())) {
			parent.setFunctionParameter(name, value);
		}
		else {
			parameterMap.put(name, value); // define new in current context
		}
	}

	/**
	 * Defines a new function parameter in the current context.
	 *
	 * @param name    the parameter name
	 * @param value   the DeclaredParameter to define
	 * @param lenient if true, silently ignores if parameter already exists
	 * @throws NaftahBugError if parameter already exists and lenient is false
	 */
	public void defineFunctionParameter(String name, DeclaredParameter value, boolean lenient) {
		name = getFunctionParameterName(name);
		if (containsFunctionParameter(name, value.getDepth())) {
			if (lenient) {
				return;
			}

			throw newNaftahBugExistentFunctionParameterError(name);
		}
		parameters.get().put(name, value); // force local
	}

	/**
	 * Defines multiple function parameters in the current context.
	 *
	 * @param parameters map of parameter names to DeclaredParameter objects
	 * @param lenient    if true, silently ignores if any parameter already exists
	 * @throws NaftahBugError if any parameter already exists and lenient is false
	 */
	public void defineFunctionParameters(Map<String, DeclaredParameter> parameters, boolean lenient) {
		parameters = parameters
				.entrySet()
				.stream()
				.map(entry -> Map.entry(getFunctionParameterName(entry.getKey()), entry.getValue()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		var parameterMap = this.parameters.get();
		AtomicReference<String> parameterName = new AtomicReference<>();
		if (parameters.entrySet().stream().anyMatch(declaredParameterEntry -> {
			var name = declaredParameterEntry.getKey();
			if (containsFunctionParameter(name, declaredParameterEntry.getValue().getDepth())) {
				parameterName.set(name);
				return true;
			}
			return false;
		})) {
			if (lenient) {
				return;
			}

			throw newNaftahBugExistentFunctionParameterError(parameterName.get());
		}
		parameterMap.putAll(parameters); // force local
	}

	/**
	 * Generates the canonical name for a function argument based on the current function call ID.
	 *
	 * @param name the original argument name
	 * @return the canonical function argument name
	 */
	public String getFunctionArgumentName(String name) {
		if (arguments == null) {
			arguments = SuppliedInheritableThreadLocal.withInitial(HashMap::new, HashMap::new);
		}
		String functionCallIdValue;
		if (functionCallId != null && (functionCallIdValue = functionCallId.get()) != null) {
			name = DefaultContext.generateParameterOrArgumentName(functionCallIdValue, name);
		}
		return name;
	}

	/**
	 * Checks if a function argument with the given name exists in the current or parent contexts.
	 *
	 * @param depth the depth of look up
	 * @param name  the argument name
	 * @return true if the function argument exists, false otherwise
	 */
	public boolean containsFunctionArgument(String name, int depth) {
		String functionArgumentName = getFunctionArgumentName(name);
		return ((depth == -1 || this.depth >= depth) && arguments
				.get()
				.containsKey(functionArgumentName)) || (parent != null && parent
						.containsFunctionArgument(name, depth));
	}

	/**
	 * Retrieves a function argument by name from the current or parent contexts.
	 *
	 * @param name the argument name
	 * @param safe if true, returns null instead of throwing an error if not found
	 * @return a pair of the context depth and the DeclaredArgument object
	 * @throws NaftahBugError if argument not found and safe is false
	 */
	public Pair<Integer, Object> getFunctionArgument(String name, boolean safe) {
		String functionArgumentName = getFunctionArgumentName(name);
		var argumentsMap = arguments.get();
		if (argumentsMap.containsKey(functionArgumentName)) {
			return ImmutablePair.of(depth, argumentsMap.get(functionArgumentName));
		}
		else if (parent != null) {
			return parent.getFunctionArgument(name, safe);
		}

		if (!safe) {
			throw new NaftahBugError("الوسيط '%s' غير موجود في السياق الحالي للدالة.".formatted(name));
		}
		return null;
	}

	/**
	 * Sets the value of a function argument in the current or parent context.
	 * Defines the argument locally if it does not exist.
	 *
	 * @param depth the depth of look up
	 * @param name  the argument name
	 * @param value the new DeclaredArgument value
	 */
	public void setFunctionArgument(String name, Object value, int depth) {
		String functionArgumentName = getFunctionArgumentName(name);
		var argumentsMap = arguments.get();
		if (argumentsMap.containsKey(functionArgumentName)) {
			argumentsMap.put(functionArgumentName, value);
		}
		else if (parent != null && parent.containsFunctionArgument(name, depth)) {
			parent.setFunctionArgument(name, value, depth);
		}
		else {
			argumentsMap.put(functionArgumentName, value); // define new in current context
		}
	}

	/**
	 * Defines a new function argument in the current context.
	 *
	 * @param depth the depth of look up
	 * @param name  the argument name
	 * @param value the DeclaredArgument to define
	 * @throws NaftahBugError if argument already exists and lenient is false
	 */
	public void defineFunctionArgument(String name, Object value, int depth) {
		name = getFunctionArgumentName(name);
		if (containsFunctionArgument(name, depth)) {
			throw newNaftahBugExistentFunctionArgumentError(name);
		}
		arguments.get().put(name, value); // force local
	}

	/**
	 * Defines multiple function arguments in the current context.
	 *
	 * @param depth     the depth of look up
	 * @param arguments map of argument names to DeclaredArgument objects
	 * @throws NaftahBugError if any argument already exists and lenient is false
	 */
	public void defineFunctionArguments(Map<String, Object> arguments, int depth) {
		arguments = arguments
				.entrySet()
				.stream()
				.map(entry -> Map.entry(getFunctionArgumentName(entry.getKey()), entry.getValue()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		var argumentsMap = this.arguments.get();
		AtomicReference<String> argumentName = new AtomicReference<>();
		if (arguments.keySet().stream().anyMatch(name -> {
			if (containsFunctionArgument(name, depth)) {
				argumentName.set(name);
				return true;
			}
			return false;
		})) {
			throw newNaftahBugExistentFunctionArgumentError(argumentName.get());
		}
		argumentsMap.putAll(arguments); // force local
	}

	/**
	 * Ensures that the thread-local map used to store loop-scoped variables
	 * is initialized.
	 *
	 * <p>This method lazily creates an instance of
	 * {@link SuppliedInheritableThreadLocal} that provides a {@link HashMap}
	 * for storing loop variables. The first supplier creates the initial
	 * map for the current thread, while the second supplier provides a copy
	 * for child threads, ensuring that loop-scoped variables are inherited
	 * but remain isolated between threads.</p>
	 *
	 * <p>If the loop variable storage has already been initialized, this method
	 * performs no action.</p>
	 */
	protected void prepareLoopVariable() {
		if (loopVariables == null) {
			loopVariables = SuppliedInheritableThreadLocal.withInitial(HashMap::new, HashMap::new);
		}
	}

	/**
	 * Generates the canonical name for a loop variable based on the current loop ID.
	 *
	 * @param name the original loop variable name
	 * @return the canonical loop variable name
	 */
	public String getLoopVariableName(String name) {
		prepareLoopVariable();
		String loopLabelValue;
		if (Objects.nonNull(loopLabel) && Objects.nonNull(loopLabelValue = loopLabel.get())) {
			name = loopLabelValue + "-" + name;
		}
		return name;
	}

	/**
	 * Generates a list of canonical loop variable names by combining loop labels with the given variable name.
	 * If the loopVariables map is uninitialized, it initializes it first.
	 *
	 * @param name the base name of the loop variable
	 * @return a list of loop variable names prefixed by each loop label followed by a hyphen
	 */
	public List<String> getLoopVariableNames(String name) {
		prepareLoopVariable();
		return getLoopLabels().stream().map(label -> label + "-" + name).toList();
	}

	/**
	 * Checks if a loop variable with the given name exists in the current or parent contexts.
	 *
	 * @param depth the depth of look up
	 * @param name  the loop variable name
	 * @return true if the loop variable exists, false otherwise
	 */
	public boolean containsLoopVariable(String name, int depth) {
		var loopVariableNames = getLoopVariableNames(name);
		return ((depth == -1 || this.depth >= depth) && loopVariableNames
				.stream()
				.anyMatch(loopVariableName -> loopVariables
						.get()
						.containsKey(loopVariableName))) || (parent != null && parent
								.containsLoopVariable(
														name,
														depth));
	}

	/**
	 * Retrieves a loop variable by name from the current or parent contexts.
	 *
	 * @param name the loop variable name
	 * @param safe if true, returns null instead of throwing an error if not found
	 * @return a pair of the context depth and the DeclaredVariable object
	 * @throws NaftahBugError if loop variable not found and safe is false
	 */
	public Pair<Integer, Object> getLoopVariable(String name, boolean safe) {
		var loopVariableNames = getLoopVariableNames(name);
		var loopVariableMap = loopVariables.get();
		var firstMatchedLoopVariableName = loopVariableNames
				.stream()
				.filter(loopVariableMap::containsKey)
				.findFirst()
				.orElse(null);
		if (Objects.nonNull(firstMatchedLoopVariableName)) {
			return ImmutablePair.of(depth, loopVariableMap.get(firstMatchedLoopVariableName));
		}
		else if (parent != null) {
			return parent.getLoopVariable(name, safe);
		}

		if (!safe) {
			throw new NaftahBugError("المتغير '%s' غير موجود في السياق الحالي للحلقة.".formatted(name));
		}
		return null;
	}

	/**
	 * Sets the value of a loop variable in the current or parent context.
	 * Defines the loop variable locally if it does not exist.
	 *
	 * @param name  the loop variable name
	 * @param value the new DeclaredVariable value
	 */
	public Object setLoopVariable(String name, Object value) {
		var loopVariableNames = getLoopVariableNames(name);
		var loopVariableMap = loopVariables.get();
		var firstMatchedLoopVariableName = loopVariableNames
				.stream()
				.filter(loopVariableMap::containsKey)
				.findFirst()
				.orElse(null);
		if (Objects.nonNull(firstMatchedLoopVariableName)) {
			loopVariableMap.put(firstMatchedLoopVariableName, value);
			return value;
		}
		else if (parent != null && parent.containsLoopVariable(name, -1)) {
			parent.setLoopVariable(name, value);
			return value;
		}
		else {
			loopVariableMap.put(getLoopVariableName(name), value); // define new in current context
			return value;
		}
	}

	/**
	 * Defines a new loop variable in the current context.
	 *
	 * @param name  the loop variable name
	 * @param value the DeclaredVariable to define
	 * @throws NaftahBugError if the loop variable already exists in the current context
	 */
	public void defineLoopVariable(String name, Object value, boolean lenient) {
		name = getLoopVariableName(name);
		var loopVariableMap = loopVariables.get();
		if (loopVariableMap.containsKey(name)) {
			if (lenient) {
				return;
			}

			throw new NaftahBugError(
										"المعامل '%s' موجود في السياق الحالي للحلقة. لا يمكن إعادة إعلانه."
												.formatted(name));
		}
		loopVariableMap.put(name, value); // force local
	}

	/**
	 * Removes a loop variable with the specified name from the current context.
	 * Throws an error if the variable exists and lenient is false.
	 *
	 * @param name    the name of the loop variable to remove
	 * @param lenient if true, silently ignores if the variable exists; otherwise throws an error
	 * @throws NaftahBugError if the variable exists in the current context and lenient is false
	 */
	public void removeLoopVariable(String name, boolean lenient) {
		name = getLoopVariableName(name);
		var loopVariableMap = loopVariables.get();
		if (!loopVariableMap.containsKey(name)) {
			if (lenient) {
				return;
			}

			throw new NaftahBugError("المعامل '%s' غير موجود في السياق الحالي للحلقة. لا يمكن إزالته.".formatted(name));
		}
		loopVariableMap.remove(name); // force local
	}

	/**
	 * Marks the given parse tree node as executed in the execution tracking map.
	 *
	 * @param node the parse tree node to mark as executed
	 */
	public void markExecuted(ParseTree node) {
		prepareParseTreeExecution();
		parseTreeExecution.get().put(node, true);
	}

	/**
	 * Checks whether the specified parse tree node has been marked as executed.
	 *
	 * @param node the parse tree node to check
	 * @return true if the node has been executed, false otherwise
	 */
	public boolean isExecuted(ParseTree node) {
		return prepareParseTreeExecution() && Optional.ofNullable(parseTreeExecution.get().get(node)).orElse(false);
	}

	/**
	 * Checks if any child or sub-child of the given parse tree node, within the contexts, has been executed
	 * and is of the specified tree type.
	 *
	 * @param <T>  the type of the tree node to look for
	 * @param node the parse tree node to check children of
	 * @param type the class type of the tree nodes to look for
	 * @return true if any child or sub-child of the specified type has been executed, false otherwise
	 */
	public <T extends Tree> boolean hasAnyExecutedChildOrSubChildOfType(ParseTree node, Class<T> type) {
		return getChildren(true)
				.stream()
				.anyMatch(currentContext -> NaftahParserHelper
						.hasAnyExecutedChildOrSubChildOfType(   node,
																type,
																currentContext.getParseTreeExecution()
						));
	}

	/**
	 * Prepares the parse tree execution map if not already initialized.
	 *
	 * @return true if the execution map was already initialized, false if it was just created
	 */
	protected boolean prepareParseTreeExecution() {
		if (parseTreeExecution == null) {
			parseTreeExecution = SuppliedInheritableThreadLocal.withInitial(NaftahParseTreeProperty::new);
			return false;
		}
		return true;
	}

	protected NaftahParseTreeProperty<Boolean> getParseTreeExecution() {
		prepareParseTreeExecution();
		return parseTreeExecution.get();
	}

	/**
	 * Gets the depth of the current context.
	 *
	 * @return the depth as an integer
	 */
	public int getDepth() {
		return depth;
	}

	/**
	 * Returns the child contexts of the current context, excluding the parent context.
	 *
	 * @return list of child DefaultContext objects
	 */
	public List<DefaultContext> getChildren() {
		return getChildren(false);
	}

	/**
	 * Returns the child contexts of the current context.
	 *
	 * @param includeParent if true, includes contexts at the same depth or greater; otherwise only deeper contexts
	 * @return list of child DefaultContext objects matching the criteria
	 */
	public List<DefaultContext> getChildren(boolean includeParent) {
		return CONTEXTS
				.entrySet()
				.stream()
				.filter(entry -> includeParent ? entry.getKey() >= depth : entry.getKey() > depth)
				.flatMap(e -> e.getValue().stream())
				.toList();
	}

	/**
	 * Returns the sibling contexts of the current context.
	 *
	 * <p>Siblings are contexts that share the same depth as this context. If
	 * {@code includeSelf} is {@code true}, the returned list also includes
	 * this context; otherwise, this context is excluded.</p>
	 *
	 * @param includeSelf if {@code true}, the returned list will include this context itself
	 * @return a list of {@link DefaultContext} objects at the same depth
	 */
	public List<DefaultContext> getSiblings(boolean includeSelf) {
		return CONTEXTS
				.entrySet()
				.stream()
				.filter(entry -> entry.getKey() == depth)
				.flatMap(e -> e.getValue().stream())
				.filter(ctx -> includeSelf || ctx != this)
				.toList();
	}

	/**
	 * Gets the identifier of the current function call being parsed.
	 *
	 * @return the function call ID as a string
	 */
	public String getFunctionCallId() {
		if (Objects.isNull(functionCallId)) {
			return null;
		}
		return functionCallId.get();
	}

	/**
	 * Sets the identifier for the current function call being in execution.
	 * <p>
	 * This value is stored in a {@link ThreadLocal} to allow each thread to
	 * independently track the current function call ID. If the thread-local
	 * is uninitialized, it is created; otherwise, the value is updated.
	 * </p>
	 *
	 * @param functionCallId the function call ID to set
	 */
	public void setFunctionCallId(String functionCallId) {
		if (Objects.isNull(this.functionCallId)) {
			this.functionCallId = ThreadLocal.withInitial(() -> functionCallId);
		}
		else {
			this.functionCallId.set(functionCallId);
		}
	}

	/**
	 * Returns the current implementation name for this context.
	 * <p>
	 * The implementation name is stored in a thread-local variable. If the
	 * thread-local is uninitialized, this method returns {@code null}.
	 * </p>
	 *
	 * @return the current implementation name, or {@code null} if none is set
	 */
	public String getImplementationName() {
		if (Objects.isNull(implementationName)) {
			return null;
		}
		return implementationName.get();
	}

	/**
	 * Sets the implementation name for this context.
	 * <p>
	 * The value is stored in a thread-local variable to allow independent
	 * tracking per thread. If the thread-local is uninitialized, it is created;
	 * otherwise, the value is updated.
	 * </p>
	 *
	 * @param implementationName the implementation name to set
	 */
	public void setImplementationName(String implementationName) {
		if (Objects.isNull(this.implementationName)) {
			this.implementationName = SuppliedInheritableThreadLocal.withInitial(() -> implementationName, true);
		}
		else {
			this.implementationName.set(implementationName);
		}
	}

	/**
	 * Checks whether the parser is currently processing a function call ID.
	 * <p>
	 * This flag is tracked using a thread-local variable. If the thread-local
	 * is uninitialized, this method returns {@code false}.
	 * </p>
	 *
	 * @return {@code true} if a function call ID is being parsed, {@code false} otherwise
	 */
	public boolean isParsingFunctionCallId() {
		if (Objects.isNull(parsingFunctionCallId)) {
			return false;
		}
		return parsingFunctionCallId.get();
	}

	/**
	 * Sets the flag indicating whether the parser is currently parsing a function call ID.
	 *
	 * @param parsingFunctionCallId true to indicate parsing a function call ID, false otherwise
	 */
	public void setParsingFunctionCallId(boolean parsingFunctionCallId) {
		if (Objects.isNull(this.parsingFunctionCallId)) {
			this.parsingFunctionCallId = ThreadLocal.withInitial(() -> parsingFunctionCallId);
		}
		else {
			this.parsingFunctionCallId.set(parsingFunctionCallId);
		}
	}

	/**
	 * Checks if the parser is currently parsing an assignment statement.
	 *
	 * @return true if parsing an assignment, false otherwise
	 */
	public boolean isParsingAssignment() {
		if (Objects.isNull(parsingAssignment)) {
			return false;
		}
		return parsingAssignment.get();
	}

	/**
	 * Sets the flag indicating whether the parser is currently parsing an assignment.
	 * If parsing is set to false, clears the declaration of assignment.
	 *
	 * @param parsingAssignment true to indicate parsing an assignment, false otherwise
	 */
	public void setParsingAssignment(boolean parsingAssignment) {
		if (Objects.isNull(this.parsingAssignment)) {
			this.parsingAssignment = ThreadLocal.withInitial(() -> parsingAssignment);
		}
		else {
			this.parsingAssignment.set(parsingAssignment);
		}
		if (!parsingAssignment) {
			setDeclarationOfAssignment(null);
		}
	}

	/**
	 * Gets the declaration of the current assignment as a pair of DeclaredVariable and a boolean flag.
	 *
	 * @return the declaration of assignment, or null if none
	 */
	public Pair<DeclaredVariable, Boolean> getDeclarationOfAssignment() {
		if (Objects.isNull(declarationOfAssignment)) {
			return null;
		}
		return declarationOfAssignment.get();
	}

	/**
	 * Sets the declaration of the current assignment.
	 *
	 * @param declarationOfAssignment a pair containing the declared variable and a boolean flag
	 */
	public void setDeclarationOfAssignment(Pair<DeclaredVariable, Boolean> declarationOfAssignment) {
		if (Objects.isNull(this.declarationOfAssignment)) {
			this.declarationOfAssignment = ThreadLocal.withInitial(() -> declarationOfAssignment);
		}
		else {
			this.declarationOfAssignment.set(declarationOfAssignment);
		}
	}

	/**
	 * Checks if the parser is currently creating an object.
	 *
	 * @return true if creating an object, false otherwise
	 */
	public boolean isCreatingObject() {
		if (Objects.isNull(creatingObject)) {
			return false;
		}
		return creatingObject.get();
	}

	/**
	 * Sets the flag indicating whether the parser is currently creating an object.
	 *
	 * @param creatingObject true to indicate creating an object, false otherwise
	 */
	public void setCreatingObject(boolean creatingObject) {
		if (Objects.isNull(this.creatingObject)) {
			this.creatingObject = ThreadLocal.withInitial(() -> creatingObject);
		}
		else {
			this.creatingObject.set(creatingObject);
		}
	}

	/**
	 * Checks whether the current thread is awaiting the completion of a task
	 * within this context.
	 *
	 * <p>This uses a {@link ThreadLocal} boolean to track if the current thread
	 * is awaiting a task, allowing multiple threads to track their own state
	 * independently.</p>
	 *
	 * @return {@code true} if the current thread is awaiting a task; {@code false} otherwise
	 */
	public boolean isAwaitingTask() {
		if (Objects.isNull(awaitingTask)) {
			return false;
		}
		return awaitingTask.get();
	}

	/**
	 * Sets the awaiting state for the current thread in this context.
	 *
	 * <p>If the underlying {@link ThreadLocal} has not been initialized yet,
	 * it will be created and initialized with the provided value. Otherwise,
	 * it updates the current thread's value.</p>
	 *
	 * @param awaitingTask {@code true} if the current thread is awaiting a task,
	 *                     {@code false} otherwise
	 */
	public void setAwaitingTask(boolean awaitingTask) {
		if (Objects.isNull(this.awaitingTask)) {
			this.awaitingTask = ThreadLocal.withInitial(() -> awaitingTask);
		}
		else {
			this.awaitingTask.set(awaitingTask);
		}
	}

	/**
	 * Retrieves the label of the current loop context.
	 * If not set locally, retrieves from the parent context if available.
	 *
	 * @return the loop label string, or null if none is set
	 */
	public String getLoopLabel() {
		String loopLabelValue;
		if (Objects.nonNull(loopLabel) && Objects.nonNull(loopLabelValue = loopLabel.get())) {
			return loopLabelValue;
		}
		else if (Objects.nonNull(parent)) {
			return parent.getLoopLabel();
		}
		return null;
	}

	/**
	 * Sets the label for the current loop context.
	 *
	 * @param loopLabel the loop label string to set
	 */
	public void setLoopLabel(String loopLabel) {
		if (Objects.isNull(this.loopLabel)) {
			this.loopLabel = ThreadLocal.withInitial(() -> loopLabel);
		}
		else {
			this.loopLabel.set(loopLabel);
		}
	}

	/**
	 * Defines a block-level import for this context.
	 * <p>
	 * The specified alias is associated with the given import element
	 * and stored in the local {@code blockImports} map. This import will
	 * only be visible within the current block or its child contexts.
	 * </p>
	 *
	 * @param alias         the alias used to reference the imported element
	 * @param importElement the fully qualified name or path of the element being imported
	 */
	private void defineBlockImport(String alias, String importElement) {
		if (blockImports == null) {
			blockImports = SuppliedInheritableThreadLocal.withInitial(HashMap::new, HashMap::new);
		}
		blockImports.get().put(alias, importElement);
	}


	/**
	 * Resolves the fully qualified import corresponding to the specified alias.
	 * <p>
	 * This method attempts to match the alias against local block imports,
	 * parent contexts, and finally the global import registry. It supports
	 * qualified aliases (e.g., {@code my.module.Class}) by recursively matching
	 * the base part of the alias and appending the remainder if a match is found.
	 * </p>
	 *
	 * @param alias the alias or qualified name to resolve
	 * @return the fully qualified import path if found; otherwise {@code null}
	 */
	public String matchImport(String alias) {
		var aliasParts = alias.split(QUALIFIED_NAME_SEPARATOR);
		String base = aliasParts[0];
		if (aliasParts.length > 1) {
			String matchedImport = doMatchImport(base);
			return Objects.nonNull(matchedImport) ? matchedImport + alias.replaceFirst(base, "") : null;
		}
		return doMatchImport(base);
	}

	/**
	 * Recursively searches for an import matching the given alias.
	 * <p>
	 * The search order is:
	 * </p>
	 * <ol>
	 * <li>Local block imports in the current context</li>
	 * <li>Parent context (if available)</li>
	 * <li>Global imports</li>
	 * </ol>
	 *
	 * @param alias the alias to match
	 * @return the fully qualified import path if found; otherwise {@code null}
	 */
	private String doMatchImport(String alias) {
		Map<String, String> blockImportMap;
		if (Objects.nonNull(blockImports) && (Objects.nonNull(blockImportMap = blockImports.get())) && blockImportMap
				.containsKey(
								alias)) {
			return blockImportMap.get(alias);
		}
		else if (parent != null) {
			return parent.doMatchImport(alias);
		}
		else {
			return IMPORTS.get(alias);
		}
	}

	/**
	 * Attempts to resolve a variable reference into a fully qualified call and
	 * retrieve its underlying value.
	 *
	 * <p>This method processes expressions of the form {@code variableId:propertyName}.
	 * If the identifier corresponds to a known variable within this context or any
	 * parent context, the method returns a {@link Triple} containing:</p>
	 *
	 * <ul>
	 * <li><b>Triple.left</b> — the fully qualified call string, in the form
	 * {@code qualifiedVariableClassName:propertyName}</li>
	 * <li><b>Triple.middle</b> — a boolean indicating whether the variable's
	 * runtime class differs from its declared type</li>
	 * <li><b>Triple.right</b> — the resolved variable value, with automatic
	 * unwrapping of {@link NaftahObject} instances</li>
	 * </ul>
	 *
	 * <p>Special handling:</p>
	 * <ul>
	 * <li>If the variable is a {@link NaftahObject}, it is unwrapped to obtain
	 * the underlying value, and a flag is set to indicate whether it originates
	 * from Java.</li>
	 * <li>If the variable is an instance of {@link Actor} or a subclass, the
	 * fully qualified type is reported as {@code Actor.class}.</li>
	 * </ul>
	 *
	 * <p>If the input string does not match the {@code id:property} format, or
	 * if the variable cannot be found in this context or any parent, the method
	 * returns {@code null}.</p>
	 *
	 * @param qualifiedCall a variable reference in the form {@code id:property}
	 * @return a {@link Triple} containing:
	 *         <ul>
	 *         <li>the fully qualified call string</li>
	 *         <li>a boolean indicating if the runtime class differs from the declared type</li>
	 *         <li>the resolved variable value</li>
	 *         </ul>
	 *         Returns {@code null} if resolution fails.
	 */
	public Triple<String, Boolean, Object> matchVariable(String qualifiedCall) {
		String[] parts = qualifiedCall.split(QUALIFIED_CALL_SEPARATOR);
		String id = parts.length == 2 ? parts[0] : null;
		if (Objects.nonNull(id) && !id.contains(QUALIFIED_NAME_SEPARATOR)) {
			var variable = getVariable(id, this);
			if (variable.isFound()) {
				Object variableValue = variable.get();
				boolean fromJava = true;
				if (variableValue instanceof NaftahObject naftahObject) {
					fromJava = naftahObject.fromJava();
					variableValue = naftahObject.get(true);
				}

				if (fromJava) {
					Class<?> variableClass = variableValue.getClass();
					// handling actors implementations created at runtime
					if (Actor.class.isAssignableFrom(variableClass)) {
						variableClass = Actor.class;
					}
					return ImmutableTriple
							.of(getQualifiedCall(getQualifiedName(variableClass.getName()), parts[1]),
								!variableClass.equals(variableValue.getClass()),
								variableValue);
				}
			}
		}
		return null;
	}
}
