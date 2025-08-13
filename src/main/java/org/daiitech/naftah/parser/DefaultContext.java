package org.daiitech.naftah.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.Tree;
import org.daiitech.naftah.builtin.Builtin;
import org.daiitech.naftah.builtin.lang.BuiltinFunction;
import org.daiitech.naftah.builtin.lang.ClassScanningResult;
import org.daiitech.naftah.builtin.lang.DeclaredFunction;
import org.daiitech.naftah.builtin.lang.DeclaredParameter;
import org.daiitech.naftah.builtin.lang.DeclaredVariable;
import org.daiitech.naftah.builtin.lang.JvmFunction;
import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.utils.Base64SerializationUtils;
import org.daiitech.naftah.utils.reflect.ClassUtils;

import static org.daiitech.naftah.Naftah.DEBUG_PROPERTY;
import static org.daiitech.naftah.Naftah.FORCE_CLASSPATH_PROPERTY;
import static org.daiitech.naftah.Naftah.INSIDE_INIT_PROPERTY;
import static org.daiitech.naftah.Naftah.INSIDE_REPL_PROPERTY;
import static org.daiitech.naftah.Naftah.SCAN_CLASSPATH_PROPERTY;
import static org.daiitech.naftah.parser.NaftahParserHelper.QUALIFIED_CALL_REGEX;
import static org.daiitech.naftah.utils.ConsoleLoader.startLoader;
import static org.daiitech.naftah.utils.ConsoleLoader.stopLoader;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.padText;
import static org.daiitech.naftah.utils.reflect.ClassUtils.filterClasses;
import static org.daiitech.naftah.utils.reflect.ClassUtils.getArabicClassQualifiers;
import static org.daiitech.naftah.utils.reflect.ClassUtils.getBuiltinMethods;
import static org.daiitech.naftah.utils.reflect.ClassUtils.getClassMethods;
import static org.daiitech.naftah.utils.reflect.ClassUtils.getClassQualifiers;
import static org.daiitech.naftah.utils.reflect.RuntimeClassScanner.loadClasses;
import static org.daiitech.naftah.utils.reflect.RuntimeClassScanner.scanCLasses;

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
	public static final Path CACHE_PATH = Paths.get("bin/.naftah_cache");

	/**
	 * Generates unique function call IDs based on the depth, function name, and a random UUID.
	 */
	public static final BiFunction<Integer, String, String> FUNCTION_CALL_ID_GENERATOR = (depth, functionName) -> "%s-%s-%s".formatted(depth, functionName, UUID.randomUUID());

	/**
	 * Generates unique parameter names based on the function name and parameter name.
	 */
	public static final BiFunction<String, String, String> PARAMETER_NAME_GENERATOR = (functionName, parameterName) -> "%s-%s".formatted(functionName, parameterName);

	/**
	 * Generates unique argument names based on the function call ID and argument name.
	 */
	public static final BiFunction<String, String, String> ARGUMENT_NAME_GENERATOR = (functionCallId, argumentName) -> "%s-%s".formatted(functionCallId, argumentName);

	// Helper to generate unique labels for unlabeled loops
	/**
	 * Generates unique loop labels for unlabeled loops based on depth and a UUID.
	 */
	public static final Function<Integer, String> LOOP_ID_GENERATOR = (depth) -> "%s-loop-%s".formatted(depth, UUID.randomUUID());

	// CONTEXTS
	/**
	 * Global map holding contexts indexed by their depth.
	 */
	protected static final Map<Integer, DefaultContext> CONTEXTS = new HashMap<>();

	// CALL STACK
	// function - arguments - returned value
	/**
	 * Stack representing the call stack containing pairs of function and argument maps,
	 * along with the returned value.
	 */
	protected static final Stack<Pair<Pair<DeclaredFunction, Map<String, Object>>, Object>> CALL_STACK = new Stack<>();

	// LOOP STACK
	// loop labels
	/**
	 * Stack representing loop labels and their associated parser rule contexts.
	 */
	protected static final Deque<Pair<String, ? extends ParserRuleContext>> LOOP_STACK = new ArrayDeque<>();

	/**
	 * Function to resolve variable values given a variable name and context.
	 * searching in loop variables, function arguments, function parameters, and declared variables.
	 */
	public static final BiFunction<String, DefaultContext, Object> VARIABLE_GETTER = (varName, context) -> Optional.ofNullable(context.getLoopVariable(varName, true)).flatMap(functionArgument -> Optional.ofNullable(functionArgument.b)).orElseGet(() -> Optional.ofNullable(context.getFunctionArgument(varName, true)).flatMap(functionArgument -> Optional.ofNullable(functionArgument.b)).orElseGet(() -> Optional.ofNullable(context.getFunctionParameter(varName, true)).flatMap(functionParameter -> Optional.ofNullable(functionParameter.b.getValue())).orElseGet(() -> Optional.ofNullable(context.getVariable(varName, true)).flatMap(declaredVariable -> Optional.ofNullable(declaredVariable.b.getValue())).orElse(null))));

	/**
	 * A supplier task to perform class scanning, loading, filtering, and extraction of JVM and builtin functions
	 * asynchronously.
	 */
	protected static final Supplier<ClassScanningResult> LOADER_TASK = () -> {
		ExecutorService internalExecutor = Executors.newFixedThreadPool(2);
		ClassScanningResult result = new ClassScanningResult();
		var classNames = scanCLasses();
		result.setClassNames(classNames);

		try {
			Callable<Pair<Set<String>, Set<String>>> qualifiersLoaderTask = () -> {
				var classQualifiersMap = getClassQualifiers(classNames.keySet(), false);
				var classQualifiers = new HashSet<>(classQualifiersMap.keySet());
				var arabicClassQualifiers = getArabicClassQualifiers(classQualifiersMap.values());
				return new Pair<>(classQualifiers, arabicClassQualifiers);
			};
			var qualifiersFuture = internalExecutor.submit(qualifiersLoaderTask);

			Callable<Map<String, Class<?>>> classLoaderTask = () -> loadClasses(classNames, false);
			var classFuture = internalExecutor.submit(classLoaderTask);

			var qualifiersFutureResult = qualifiersFuture.get();
			result.setClassQualifiers(qualifiersFutureResult.a);
			result.setArabicClassQualifiers(qualifiersFutureResult.b);

			result.setClasses(classFuture.get());

			Callable<Map<String, Class<?>>> accessibleClassLoaderTask = () -> filterClasses(result.getClasses(), ClassUtils::isAccessibleClass);
			var accessibleClassFuture = internalExecutor.submit(accessibleClassLoaderTask);

			Callable<Map<String, Class<?>>> instantiableClassLoaderTask = () -> filterClasses(result.getClasses(), ClassUtils::isInstantiableClass);
			var instantiableClassFuture = internalExecutor.submit(instantiableClassLoaderTask);

			result.setAccessibleClasses(accessibleClassFuture.get());
			result.setInstantiableClasses(instantiableClassFuture.get());

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
	// LOADED CLASSES
	protected static Map<String, ClassLoader> CLASS_NAMES;
	protected static Set<String> CLASS_QUALIFIERS;
	protected static Set<String> ARABIC_CLASS_QUALIFIERS;
	// qualifiedName -> CLass<?>
	protected static Map<String, Class<?>> CLASSES;
	protected static Map<String, Class<?>> ACCESSIBLE_CLASSES;
	protected static Map<String, Class<?>> INSTANTIABLE_CLASSES;
	// qualifiedCall -> Method
	protected static Map<String, List<JvmFunction>> JVM_FUNCTIONS;
	protected static Map<String, List<BuiltinFunction>> BUILTIN_FUNCTIONS;
	protected static volatile boolean SHOULD_BOOT_STRAP;
	protected static volatile boolean FORCE_BOOT_STRAP;
	protected static volatile boolean ASYNC_BOOT_STRAP;
	protected static volatile boolean BOOT_STRAP_FAILED;
	protected static volatile boolean BOOT_STRAPPED;

	/**
	 * Consumer to handle the result or error from class scanning and loading.
	 */
	protected static final BiConsumer<? super ClassScanningResult, ? super Throwable> LOADER_CONSUMER = (result, throwable) -> {
		if (Objects.nonNull(throwable)) {
			defaultBootstrap();
			BOOT_STRAP_FAILED = true;
		}
		else {
			setContextFromClassScanningResult(result);
			serializeClassScanningResult(result);
		}
	};
	// instance
	protected final DefaultContext parent;
	protected final int depth;
	protected final Map<String, DeclaredVariable> variables = new HashMap<>();
	protected final Map<String, DeclaredFunction> functions = new HashMap<>();
	protected String functionCallId; // current function in execution inside a context
	protected boolean parsingFunctionCallId; // parsing current function in execution
	protected boolean parsingAssignment; // parsing an assignment is in execution
	protected boolean creatingObject; // object creation is in execution
	protected Pair<DeclaredVariable, Boolean> declarationOfAssignment; // the declaration of variable being assigned
	protected String loopLabel; // current loop label in execution inside a context
	protected Map<String, Object> loopVariables; // only use in loop execution context
	protected NaftahParseTreeProperty<Boolean> parseTreeExecution;
	protected Map<String, DeclaredParameter> parameters; // only use in function call context
	protected Map<String, Object> arguments; // only use in function call context

	/**
	 * Constructs a default context with no parent, parameters, or arguments.
	 */
	protected DefaultContext() {
		this(null, null, null);
	}

	/**
	 * Constructs a context with specified parameters and arguments, but no parent.
	 *
	 * @param parameters function parameters map
	 * @param arguments  function arguments map
	 */
	protected DefaultContext(Map<String, DeclaredParameter> parameters, Map<String, Object> arguments) {
		this(null, parameters, arguments);
	}

	/**
	 * Constructs a context with a parent context, parameters, and arguments.
	 * <p>
	 * Throws a {@link NaftahBugError} if instantiated outside of allowed conditions
	 * (e.g., outside REPL and without a parent context when contexts already exist).
	 *
	 * @param parent     the parent context, or null if none
	 * @param parameters function parameters map
	 * @param arguments  function arguments map
	 */
	protected DefaultContext(DefaultContext parent, Map<String, DeclaredParameter> parameters, Map<String, Object> arguments) {
		if (Boolean.FALSE.equals(Boolean.getBoolean(INSIDE_REPL_PROPERTY)) && parent == null && (CONTEXTS.size() != 0)) {
			throw new NaftahBugError("استخدام غير مسموح به.");
		}
		this.parent = parent;
		this.depth = parent == null ? 0 : parent.getDepth() + 1;
		this.arguments = arguments;
		this.parameters = parameters;
		CONTEXTS.put(depth, this);
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
	public static DefaultContext registerContext(Map<String, DeclaredParameter> parameters, Map<String, Object> arguments) {
		return new DefaultContext(parameters, arguments);
	}

	/**
	 * Registers a new default context with the specified parent context.
	 *
	 * @param parent the parent {@link DefaultContext}
	 * @return a new {@link DefaultContext} instance
	 */
	public static DefaultContext registerContext(DefaultContext parent) {
		return new DefaultContext(parent, null, null);
	}

	/**
	 * Registers a new default context with the specified parent, parameters, and arguments.
	 *
	 * @param parent     the parent {@link DefaultContext}
	 * @param parameters the function parameters map
	 * @param arguments  the function arguments map
	 * @return a new {@link DefaultContext} instance
	 */
	public static DefaultContext registerContext(DefaultContext parent, Map<String, DeclaredParameter> parameters, Map<String, Object> arguments) {
		return new DefaultContext(parent, parameters, arguments);
	}

	/**
	 * Retrieves the context associated with the specified depth.
	 *
	 * @param depth the depth of the context
	 * @return the {@link DefaultContext} at the given depth, or {@code null} if none
	 */
	public static DefaultContext getContextByDepth(int depth) {
		return CONTEXTS.get(depth);
	}

	/**
	 * Deregisters and removes the context associated with the specified depth.
	 * If the removed context and its parent both have parse tree execution properties,
	 * copies the parse tree execution state from the removed context to its parent.
	 *
	 * @param depth the depth of the context to remove
	 * @return the removed {@link DefaultContext}
	 */
	public static DefaultContext deregisterContext(int depth) {
		DefaultContext context = CONTEXTS.remove(depth);
		if (context.parent != null && context.parseTreeExecution != null && context.parent.parseTreeExecution != null) {
			context.parent.parseTreeExecution.copyFrom(context.parseTreeExecution);
		}
		return context;
	}

	/**
	 * Pushes a function call frame onto the call stack.
	 *
	 * @param function  the {@link DeclaredFunction} being called
	 * @param arguments the map of argument names to values
	 */
	public static void pushCall(DeclaredFunction function, Map<String, Object> arguments) {
		CALL_STACK.push(new Pair<>(new Pair<>(function, arguments), null));
	}

	/**
	 * Updates the top function call frame on the call stack with a returned value.
	 *
	 * @param function      the {@link DeclaredFunction} being updated
	 * @param arguments     the map of argument names to values
	 * @param returnedValue the returned value from the function call
	 */
	public static void updateCall(DeclaredFunction function, Map<String, Object> arguments, Object returnedValue) {
		CALL_STACK.set(CALL_STACK.size() - 1, new Pair<>(new Pair<>(function, arguments), returnedValue));
	}

	/**
	 * Pops the most recent function call frame from the call stack.
	 *
	 * @return the popped function call frame as a pair containing function, arguments, and return value
	 * @throws NaftahBugError if the call stack is empty
	 */
	public static Pair<Pair<DeclaredFunction, Map<String, Object>>, Object> popCall() {
		if (CALL_STACK.empty()) {
			throw new NaftahBugError("حالة غير قانونية: لا يمكن إزالة عنصر من مكدس استدعاءات الدوال الفارغ.");
		}
		return CALL_STACK.pop();
	}

	/**
	 * Retrieves the current loop label based on the given label context or generates a unique one.
	 *
	 * @param labelCtx the label parser context, may be {@code null}
	 * @param depth    the depth to use for label generation if none provided
	 * @return the current loop label as a string
	 */
	public static String currentLoopLabel(org.daiitech.naftah.parser.NaftahParser.LabelContext labelCtx, int depth) {
		if (labelCtx != null) {
			return labelCtx.ID().getText();
		}
		else {
			return LOOP_ID_GENERATOR.apply(depth);
		}
	}

	/**
	 * Pushes a loop label and its associated parser context onto the loop stack.
	 *
	 * @param label   the loop label
	 * @param loopCtx the parser context associated with the loop
	 * @param <T>     the type of the parser context
	 */
	public static <T extends ParserRuleContext> void pushLoop(String label, T loopCtx) {
		LOOP_STACK.push(new Pair<>(label, loopCtx));
	}

	/**
	 * Returns a list of all loop labels currently in the loop stack.
	 *
	 * @return a list of loop labels
	 */
	public static List<String> getLoopLabels() {
		return LOOP_STACK.stream().map(stringPair -> stringPair.a).toList();
	}

	/**
	 * Checks if the loop stack contains a loop with the given label.
	 *
	 * @param label the label to check
	 * @return {@code true} if the loop stack contains the label, {@code false} otherwise
	 */
	public static boolean loopContainsLabel(String label) {
		return LOOP_STACK.stream().anyMatch(stringPair -> stringPair.a.equals(label));
	}

	/**
	 * Pops the most recent loop label and context from the loop stack.
	 *
	 * @return the popped loop label and parser context pair
	 * @throws NaftahBugError if the loop stack is empty
	 */
	public static Pair<String, ? extends ParserRuleContext> popLoop() {
		if (LOOP_STACK.isEmpty()) {
			throw new NaftahBugError("حالة غير قانونية: لا يمكن إزالة عنصر من مكدس الحلقات الفارغ.");
		}
		return LOOP_STACK.pop();
	}

	/**
	 * Updates the context static fields from the given {@link ClassScanningResult}.
	 * Marks bootstrapping as completed.
	 *
	 * @param result the class scanning result containing loaded class information
	 */
	protected static void setContextFromClassScanningResult(ClassScanningResult result) {
		CLASS_NAMES = result.getClassNames();
		CLASS_QUALIFIERS = result.getClassQualifiers();
		ARABIC_CLASS_QUALIFIERS = result.getArabicClassQualifiers();
		CLASSES = result.getClasses();
		ACCESSIBLE_CLASSES = result.getAccessibleClasses();
		INSTANTIABLE_CLASSES = result.getInstantiableClasses();
		JVM_FUNCTIONS = result.getJvmFunctions();
		BUILTIN_FUNCTIONS = result.getBuiltinFunctions();
		BOOT_STRAPPED = true;
	}

	/**
	 * Loads the default builtin functions into the context.
	 */
	public static void defaultBootstrap() {
		BUILTIN_FUNCTIONS = getBuiltinMethods(Builtin.class).stream().map(builtinFunction -> Map.entry(builtinFunction.getFunctionInfo().name(), builtinFunction)).collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
	}

	/**
	 * Calls the loader task to scan and load classes, optionally asynchronously.
	 *
	 * @param async {@code true} to run asynchronously, {@code false} to run synchronously
	 */
	protected static void callLoader(boolean async) {
		if (async) {
			CompletableFuture.supplyAsync(LOADER_TASK).whenComplete(LOADER_CONSUMER);
		}
		else {
			ClassScanningResult classScanningResult = null;
			Throwable thr = null;
			try {
				startLoader(
						"تحضير فئات مسار فئات جافا (Java classpath) ومعالجتها لإعادة استخدامها داخل سكربت نفطة. قد يستغرق الأمر عدة دقائق حسب الإعدادات");
				classScanningResult = LOADER_TASK.get();
				stopLoader();
			}
			catch (Throwable throwable) {
				thr = throwable;
			}
			finally {
				LOADER_CONSUMER.accept(classScanningResult, thr);
			}
		}
	}

	/**
	 * Serializes the class scanning result to a cache file.
	 *
	 * @param result the class scanning result to serialize
	 * @throws NaftahBugError if serialization fails
	 */
	protected static void serializeClassScanningResult(ClassScanningResult result) {
		try {
			var path = Base64SerializationUtils.serialize(result, CACHE_PATH);
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
	 */
	protected static void deserializeClassScanningResult() {
		try {
			var result = (ClassScanningResult) Base64SerializationUtils.deserialize(CACHE_PATH);
			setContextFromClassScanningResult(result);
		}
		catch (Exception e) {
			callLoader(ASYNC_BOOT_STRAP);
		}
	}

	/**
	 * Returns a list of all completion candidates combining builtin functions, JVM functions, and instantiable classes.
	 *
	 * @return list of completion names
	 */
	public static List<String> getCompletions() {
		var runtimeCompletions = new ArrayList<>(BUILTIN_FUNCTIONS.keySet());
		Optional.ofNullable(JVM_FUNCTIONS).ifPresent(stringListMap -> runtimeCompletions.addAll(stringListMap.keySet()));
		Optional.ofNullable(INSTANTIABLE_CLASSES).ifPresent(stringListMap -> runtimeCompletions.addAll(stringListMap.keySet()));
		return runtimeCompletions;
	}

	/**
	 * Performs the bootstrap process to load classes and functions, either synchronously or asynchronously, based on
	 * configuration flags.
	 *
	 * @param async {@code true} for asynchronous bootstrap, {@code false} for synchronous
	 */
	public static void bootstrap(boolean async) {
		if (Boolean.getBoolean(DEBUG_PROPERTY) || Boolean.getBoolean(INSIDE_INIT_PROPERTY)) {
			padText("تحضير فئات مسار فئات جافا (Java classpath)...", true);
		}
		SHOULD_BOOT_STRAP = Boolean.getBoolean(SCAN_CLASSPATH_PROPERTY);
		ASYNC_BOOT_STRAP = async;
		long start = System.nanoTime();
		if (SHOULD_BOOT_STRAP) {
			try {
				Files.createDirectories(CACHE_PATH.getParent());
			}
			catch (IOException e) {
				throw new NaftahBugError(e);
			}

			FORCE_BOOT_STRAP = Boolean.getBoolean(FORCE_CLASSPATH_PROPERTY);

			if (FORCE_BOOT_STRAP || !Files.exists(CACHE_PATH)) {
				callLoader(ASYNC_BOOT_STRAP);
			}
			else {
				deserializeClassScanningResult();
			}
		}
		else {
			defaultBootstrap();
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
			while (!BOOT_STRAPPED && (Objects.isNull(INSTANTIABLE_CLASSES) || Objects.isNull(ACCESSIBLE_CLASSES) || Objects.isNull(CLASSES))) {
				// block the execution until bootstrapped
				if (BOOT_STRAP_FAILED) {
					return Object.class;
				}
			}
			return doGetJavaType(qualifiedName);
		}
		return Object.class;
	}

	// variables

	/**
	 * Checks if the variable with the given name exists in the current context or any parent context.
	 *
	 * @param name the variable name
	 * @return true if the variable exists, false otherwise
	 */
	public boolean containsVariable(String name) {
		return variables.containsKey(name) || (parent != null && parent.containsVariable(name));
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
		if (variables.containsKey(name)) {
			return new Pair<>(depth, variables.get(name));
		}
		else if (parent != null) {
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
	public void setVariable(String name, DeclaredVariable value) {
		if (variables.containsKey(name)) {
			variables.put(name, value);
		}
		else if (parent != null && parent.containsVariable(name)) {
			parent.setVariable(name, value);
		}
		else {
			variables.put(name, value); // define new in current context
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
		if (variables.containsKey(name)) {
			throw new NaftahBugError("المتغير '%s' موجود في السياق الحالي. لا يمكن إعادة إعلانه.".formatted(name));
		}
		variables.put(name, value); // force local
	}

	/**
	 * Defines multiple variables in the current context.
	 *
	 * @param variables map of variable names to DeclaredVariable objects
	 * @throws NaftahBugError if any variable already exists in the current context
	 */
	public void defineVariables(Map<String, DeclaredVariable> variables) {
		if (variables.keySet().stream().anyMatch(this.variables::containsKey)) {
			throw new NaftahBugError("المتغير موجود في السياق الحالي. لا يمكن إعادة إعلانه.");
		}
		this.variables.putAll(variables); // force local
	}

	// functions

	/**
	 * Checks if the function with the given name exists in the current context, built-in functions,
	 * JVM functions (if bootstrapped), or any parent context.
	 *
	 * @param name the function name
	 * @return true if the function exists, false otherwise
	 */
	public boolean containsFunction(String name) {
		return functions.containsKey(name) || BUILTIN_FUNCTIONS != null && BUILTIN_FUNCTIONS.containsKey(name) || (name.matches(QUALIFIED_CALL_REGEX) && SHOULD_BOOT_STRAP && (!BOOT_STRAPPED || JVM_FUNCTIONS != null && JVM_FUNCTIONS.containsKey(name))) || parent != null && parent.containsFunction(name);
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
		if (functions.containsKey(name)) {
			return new Pair<>(depth, functions.get(name));
		}
		else if (parent != null) {
			return parent.getFunction(name, safe);
		}
		else { // root parent
			if (BUILTIN_FUNCTIONS.containsKey(name)) {
				var functions = BUILTIN_FUNCTIONS.get(name);
				return new Pair<>(depth, functions.size() == 1 ? functions.get(0) : functions);
			}
			else if (SHOULD_BOOT_STRAP && !BOOT_STRAP_FAILED) {
				if (BOOT_STRAPPED && JVM_FUNCTIONS.containsKey(name)) {
					var functions = JVM_FUNCTIONS.get(name);
					return new Pair<>(depth, functions.size() == 1 ? functions.get(0) : functions);
				}
				else if (name.matches(QUALIFIED_CALL_REGEX)) {
					while (!BOOT_STRAPPED && Objects.isNull(JVM_FUNCTIONS)) {
						// block the execution until bootstrapped
						if (BOOT_STRAP_FAILED) {
							return null;
						}
					}
					var functions = JVM_FUNCTIONS.get(name);
					return new Pair<>(depth, functions.size() == 1 ? functions.get(0) : functions);
				}
			}
		}

		if (!safe) {
			throw new NaftahBugError("الدالة '%s' غير موجودة في السياق الحالي.".formatted(name));
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
		if (functions.containsKey(name)) {
			functions.put(name, value);
		}
		else if (parent != null && parent.containsFunction(name)) {
			parent.setFunction(name, value);
		}
		else {
			functions.put(name, value); // define new in current context
		}
	}

	/**
	 * Defines a new function in the current context.
	 *
	 * @param name  the function name
	 * @param value the DeclaredFunction to define
	 * @throws NaftahBugError if the function already exists in the current context
	 */
	public void defineFunction(String name, DeclaredFunction value) {
		if (functions.containsKey(name)) {
			throw new NaftahBugError("الدالة '%s' موجودة في السياق الحالي. لا يمكن إعادة إعلانه.".formatted(name));
		}
		functions.put(name, value); // force local
	}

	// functions parameters

	/**
	 * Generates the canonical name for a function parameter based on the current function call ID.
	 *
	 * @param name the original parameter name
	 * @return the canonical function parameter name
	 */
	public String getFunctionParameterName(String name) {
		if (parameters == null) {
			parameters = new HashMap<>();
		}
		if (functionCallId != null) {
			String functionName = functionCallId.split("-")[1];
			name = DefaultContext.PARAMETER_NAME_GENERATOR.apply(functionName, name);
		}
		return name;
	}

	/**
	 * Checks if a function parameter with the given name exists in the current or parent contexts.
	 *
	 * @param name the parameter name
	 * @return true if the function parameter exists, false otherwise
	 */
	public boolean containsFunctionParameter(String name) {
		String functionParameterName = getFunctionParameterName(name);
		return parameters.containsKey(functionParameterName) || (parent != null && parent.containsFunctionParameter(name));
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
		if (parameters.containsKey(functionParameterName)) {
			return new Pair<>(depth, parameters.get(functionParameterName));
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
		if (parameters.containsKey(functionParameterName)) {
			parameters.put(functionParameterName, value);
		}
		else if (parent != null && parent.containsFunctionParameter(name)) {
			parent.setFunctionParameter(name, value);
		}
		else {
			parameters.put(name, value); // define new in current context
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
		if (parameters.containsKey(name)) {
			if (lenient) {
				return;
			}

			throw new NaftahBugError(
					"المعامل '%s' موجود في السياق الحالي للدالة. لا يمكن إعادة إعلانه.".formatted(name));
		}
		parameters.put(name, value); // force local
	}

	/**
	 * Defines multiple function parameters in the current context.
	 *
	 * @param parameters map of parameter names to DeclaredParameter objects
	 * @param lenient    if true, silently ignores if any parameter already exists
	 * @throws NaftahBugError if any parameter already exists and lenient is false
	 */
	public void defineFunctionParameters(Map<String, DeclaredParameter> parameters, boolean lenient) {
		parameters = parameters.entrySet().stream().map(entry -> Map.entry(getFunctionParameterName(entry.getKey()), entry.getValue())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		if (parameters.keySet().stream().anyMatch(this.parameters::containsKey)) {
			if (lenient) {
				return;
			}

			throw new NaftahBugError("المعامل موجود في السياق الحالي للدالة. لا يمكن إعادة إعلانه.");
		}
		this.parameters.putAll(parameters); // force local
	}

	// functions arguments

	/**
	 * Generates the canonical name for a function argument based on the current function call ID.
	 *
	 * @param name the original argument name
	 * @return the canonical function argument name
	 */
	public String getFunctionArgumentName(String name) {
		if (arguments == null) {
			arguments = new HashMap<>();
		}
		if (functionCallId != null) {
			name = DefaultContext.ARGUMENT_NAME_GENERATOR.apply(functionCallId, name);
		}
		return name;
	}

	/**
	 * Checks if a function argument with the given name exists in the current or parent contexts.
	 *
	 * @param name the argument name
	 * @return true if the function argument exists, false otherwise
	 */
	/**
	 * Checks if a function argument with the given name exists in the current or parent contexts.
	 *
	 * @param name the argument name
	 * @return true if the function argument exists, false otherwise
	 */
	public boolean containsFunctionArgument(String name) {
		String functionArgumentName = getFunctionArgumentName(name);
		return arguments.containsKey(functionArgumentName) || (parent != null && parent.containsFunctionArgument(name));
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
		if (arguments.containsKey(functionArgumentName)) {
			return new Pair<>(depth, arguments.get(functionArgumentName));
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
	 * @param name  the argument name
	 * @param value the new DeclaredArgument value
	 */
	public void setFunctionArgument(String name, Object value) {
		String functionArgumentName = getFunctionArgumentName(name);
		if (arguments.containsKey(functionArgumentName)) {
			arguments.put(functionArgumentName, value);
		}
		else if (parent != null && parent.containsFunctionArgument(name)) {
			parent.setFunctionArgument(name, value);
		}
		else {
			arguments.put(functionArgumentName, value); // define new in current context
		}
	}

	/**
	 * Defines a new function argument in the current context.
	 *
	 * @param name  the argument name
	 * @param value the DeclaredArgument to define
	 * @throws NaftahBugError if argument already exists and lenient is false
	 */
	public void defineFunctionArgument(String name, Object value) {
		name = getFunctionArgumentName(name);
		if (arguments.containsKey(name)) {
			throw new NaftahBugError(
					"الوسيط '%s' موجود في السياق الحالي للدالة. لا يمكن إعادة إعلانه.".formatted(name));
		}
		arguments.put(name, value); // force local
	}

	/**
	 * Defines multiple function arguments in the current context.
	 *
	 * @param arguments map of argument names to DeclaredArgument objects
	 * @throws NaftahBugError if any argument already exists and lenient is false
	 */
	public void defineFunctionArguments(Map<String, Object> arguments) {
		arguments = arguments.entrySet().stream().map(entry -> Map.entry(getFunctionArgumentName(entry.getKey()), entry.getValue())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		if (arguments.keySet().stream().anyMatch(this.arguments::containsKey)) {
			throw new NaftahBugError("الوسيط موجود في السياق الحالي للدالة. لا يمكن إعادة إعلانه.");
		}
		this.arguments.putAll(arguments); // force local
	}

	// loop variables

	/**
	 * Generates the canonical name for a loop variable based on the current loop ID.
	 *
	 * @param name the original loop variable name
	 * @return the canonical loop variable name
	 */
	public String getLoopVariableName(String name) {
		if (loopVariables == null) {
			loopVariables = new HashMap<>();
		}
		if (loopLabel != null) {
			name = loopLabel + "-" + name;
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
		if (loopVariables == null) {
			loopVariables = new HashMap<>();
		}
		return getLoopLabels().stream().map(label -> label + "-" + name).toList();
	}

	/**
	 * Checks if a loop variable with the given name exists in the current or parent contexts.
	 *
	 * @param name the loop variable name
	 * @return true if the loop variable exists, false otherwise
	 */
	public boolean containsLoopVariable(String name) {
		var loopVariableNames = getLoopVariableNames(name);
		return loopVariableNames.stream().anyMatch(loopVariableName -> loopVariables.containsKey(loopVariableName)) || (parent != null && parent.containsLoopVariable(name));
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
		var firstMatchedLoopVariableName = loopVariableNames.stream().filter(loopVariableName -> loopVariables.containsKey(loopVariableName)).findFirst().orElse(null);
		if (Objects.nonNull(firstMatchedLoopVariableName)) {
			return new Pair<>(depth, loopVariables.get(firstMatchedLoopVariableName));
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
		var firstMatchedLoopVariableName = loopVariableNames.stream().filter(loopVariableName -> loopVariables.containsKey(loopVariableName)).findFirst().orElse(null);
		if (Objects.nonNull(firstMatchedLoopVariableName)) {
			loopVariables.put(firstMatchedLoopVariableName, value);
			return value;
		}
		else if (parent != null && parent.containsLoopVariable(name)) {
			parent.setLoopVariable(name, value);
			return value;
		}
		else {
			loopVariables.put(getLoopVariableName(name), value); // define new in current context
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
		if (loopVariables.containsKey(name)) {
			if (lenient) {
				return;
			}

			throw new NaftahBugError(
					"المعامل '%s' موجود في السياق الحالي للحلقة. لا يمكن إعادة إعلانه.".formatted(name));
		}
		loopVariables.put(name, value); // force local
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
		if (loopVariables.containsKey(name)) {
			if (lenient) {
				return;
			}

			throw new NaftahBugError("المعامل '%s' موجود في السياق الحالي للحلقة. لا يمكن إزالته.".formatted(name));
		}
		loopVariables.remove(name); // force local
	}

	// execution tree

	/**
	 * Marks the given parse tree node as executed in the execution tracking map.
	 *
	 * @param node the parse tree node to mark as executed
	 */
	public void markExecuted(ParseTree node) {
		prepareParseTreeExecution();
		parseTreeExecution.put(node, true);
	}

	/**
	 * Checks whether the specified parse tree node has been marked as executed.
	 *
	 * @param node the parse tree node to check
	 * @return true if the node has been executed, false otherwise
	 */
	public boolean isExecuted(ParseTree node) {
		return prepareParseTreeExecution() && Optional.ofNullable(parseTreeExecution.get(node)).orElse(false);
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
		return prepareParseTreeExecution() && getChildren(true).stream().anyMatch(currentContext -> NaftahParserHelper.hasAnyExecutedChildOrSubChildOfType(node, type, currentContext.parseTreeExecution));
	}

	/**
	 * Prepares the parse tree execution map if not already initialized.
	 *
	 * @return true if the execution map was already initialized, false if it was just created
	 */
	protected boolean prepareParseTreeExecution() {
		if (parseTreeExecution == null) {
			parseTreeExecution = new NaftahParseTreeProperty<>();
			return false;
		}
		return true;
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
		return CONTEXTS.entrySet().stream().filter(entry -> includeParent ? entry.getKey() >= depth : entry.getKey() > depth).map(Map.Entry::getValue).toList();
	}

	/**
	 * Gets the identifier of the current function call being parsed.
	 *
	 * @return the function call ID as a string
	 */
	public String getFunctionCallId() {
		return functionCallId;
	}

	/**
	 * Sets the identifier for the current function call being parsed.
	 *
	 * @param functionCallId the function call ID to set
	 */
	public void setFunctionCallId(String functionCallId) {
		this.functionCallId = functionCallId;
	}

	/**
	 * Checks if the parser is currently parsing a function call ID.
	 *
	 * @return true if parsing a function call ID, false otherwise
	 */
	public boolean isParsingFunctionCallId() {
		return parsingFunctionCallId;
	}

	/**
	 * Sets the flag indicating whether the parser is currently parsing a function call ID.
	 *
	 * @param parsingFunctionCallId true to indicate parsing a function call ID, false otherwise
	 */
	public void setParsingFunctionCallId(boolean parsingFunctionCallId) {
		this.parsingFunctionCallId = parsingFunctionCallId;
	}

	/**
	 * Checks if the parser is currently parsing an assignment statement.
	 *
	 * @return true if parsing an assignment, false otherwise
	 */
	public boolean isParsingAssignment() {
		return parsingAssignment;
	}

	/**
	 * Sets the flag indicating whether the parser is currently parsing an assignment.
	 * If parsing is set to false, clears the declaration of assignment.
	 *
	 * @param parsingAssignment true to indicate parsing an assignment, false otherwise
	 */
	public void setParsingAssignment(boolean parsingAssignment) {
		this.parsingAssignment = parsingAssignment;
		if (!this.parsingAssignment) {
			setDeclarationOfAssignment(null);
		}
	}

	/**
	 * Gets the declaration of the current assignment as a pair of DeclaredVariable and a boolean flag.
	 *
	 * @return the declaration of assignment, or null if none
	 */
	public Pair<DeclaredVariable, Boolean> getDeclarationOfAssignment() {
		return declarationOfAssignment;
	}

	/**
	 * Sets the declaration of the current assignment.
	 *
	 * @param declarationOfAssignment a pair containing the declared variable and a boolean flag
	 */
	public void setDeclarationOfAssignment(Pair<DeclaredVariable, Boolean> declarationOfAssignment) {
		this.declarationOfAssignment = declarationOfAssignment;
	}

	/**
	 * Checks if the parser is currently creating an object.
	 *
	 * @return true if creating an object, false otherwise
	 */
	public boolean isCreatingObject() {
		return creatingObject;
	}

	/**
	 * Sets the flag indicating whether the parser is currently creating an object.
	 *
	 * @param creatingObject true to indicate creating an object, false otherwise
	 */
	public void setCreatingObject(boolean creatingObject) {
		this.creatingObject = creatingObject;
	}

	/**
	 * Retrieves the label of the current loop context.
	 * If not set locally, retrieves from the parent context if available.
	 *
	 * @return the loop label string, or null if none is set
	 */
	public String getLoopLabel() {
		if (Objects.nonNull(loopLabel)) {
			return loopLabel;
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
		this.loopLabel = loopLabel;
	}

	public static NaftahBugError newNaftahBugVariableNotFoundError(String name) {
		return new NaftahBugError("المتغير '%s' غير موجود في السياق الحالي.".formatted(name));

	}
}
