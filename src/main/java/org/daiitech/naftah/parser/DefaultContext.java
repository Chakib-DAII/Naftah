package org.daiitech.naftah.parser;

import static org.daiitech.naftah.Naftah.INSIDE_SHELL_PROPERTY;
import static org.daiitech.naftah.Naftah.SCAN_CLASSPATH_PROPERTY;
import static org.daiitech.naftah.parser.NaftahParserHelper.QUALIFIED_CALL_REGEX;
import static org.daiitech.naftah.utils.reflect.ClassUtils.*;
import static org.daiitech.naftah.utils.reflect.RuntimeClassScanner.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.Tree;
import org.daiitech.naftah.builtin.Builtin;
import org.daiitech.naftah.builtin.lang.*;
import org.daiitech.naftah.utils.Base64SerializationUtils;
import org.daiitech.naftah.utils.reflect.ClassUtils;

/**
 * @author Chakib Daii <br>
 *     TODO: think about a way to vreate child context everytime TODO: and attach it to the function
 *     or variable used at current execution
 */
public class DefaultContext {
  public static final Path CACHE_PATH = Paths.get("bin/.naftah_cache");

  public static final BiFunction<Integer, String, String> FUNCTION_CALL_ID_GENERATOR =
      (depth, functionName) -> "%s-%s-%s".formatted(depth, functionName, UUID.randomUUID());
  public static final BiFunction<String, String, String> PARAMETER_NAME_GENERATOR =
      (functionName, parameterName) -> "%s-%s".formatted(functionName, parameterName);
  public static final BiFunction<String, String, String> ARGUMENT_NAME_GENERATOR =
      (functionCallId, argumentName) -> "%s-%s".formatted(functionCallId, argumentName);

  public static final BiFunction<String, DefaultContext, Object> VARIABLE_GETTER =
      (varName, context) ->
          Optional.ofNullable(context.getFunctionArgument(varName, true))
              .flatMap(functionArgument -> Optional.ofNullable(functionArgument.b))
              .orElseGet(
                  () ->
                      Optional.ofNullable(context.getFunctionParameter(varName, true))
                          .flatMap(
                              functionParameter ->
                                  Optional.ofNullable(functionParameter.b.getValue()))
                          .orElseGet(
                              () ->
                                  Optional.ofNullable(context.getVariable(varName, true))
                                      .flatMap(
                                          declaredVariable ->
                                              Optional.ofNullable(declaredVariable.b.getValue()))
                                      .orElse(null)));

  // CONTEXTS
  private static final Map<Integer, DefaultContext> CONTEXTS = new HashMap<>();

  public static DefaultContext registerContext() {
    return new DefaultContext();
  }

  public static DefaultContext registerContext(
      Map<String, DeclaredParameter> parameters, Map<String, Object> arguments) {
    return new DefaultContext(parameters, arguments);
  }

  public static DefaultContext registerContext(DefaultContext parent) {
    return new DefaultContext(parent, null, null);
  }

  public static DefaultContext registerContext(
      DefaultContext parent,
      Map<String, DeclaredParameter> parameters,
      Map<String, Object> arguments) {
    return new DefaultContext(parent, parameters, arguments);
  }

  public static DefaultContext getContextByDepth(int depth) {
    return CONTEXTS.get(depth);
  }

  public static DefaultContext deregisterContext(int depth) {
    DefaultContext context = CONTEXTS.remove(depth);
    if (context.parent != null && context.parseTreeExecution != null) {
      context.parent.parseTreeExecution.copyFrom(context.parseTreeExecution);
    }
    return context;
  }

  // CALL STACK
  // function - arguments - returned value
  private static final Stack<Pair<Pair<DeclaredFunction, Map<String, Object>>, Object>> CALL_STACK =
      new Stack<>();

  public static void pushCall(DeclaredFunction function, Map<String, Object> arguments) {
    CALL_STACK.push(new Pair<>(new Pair<>(function, arguments), null));
  }

  public static void updateCall(
      DeclaredFunction function, Map<String, Object> arguments, Object returnedValue) {
    CALL_STACK.set(
        CALL_STACK.size() - 1, new Pair<>(new Pair<>(function, arguments), returnedValue));
  }

  public static Pair<Pair<DeclaredFunction, Map<String, Object>>, Object> popCall() {
    return CALL_STACK.pop();
  }

  // LOADED CLASSES
  private static Map<String, Optional<? extends ClassLoader>> CLASS_NAMES;
  private static Set<String> CLASS_QUALIFIERS;
  private static Set<String> ARABIC_CLASS_QUALIFIERS;
  // qualifiedName -> CLass<?>
  private static Map<String, Class<?>> CLASSES;
  private static Map<String, Class<?>> ACCESSIBLE_CLASSES;
  private static Map<String, Class<?>> INSTANTIABLE_CLASSES;
  // qualifiedCall -> Method
  private static Map<String, List<JvmFunction>> JVM_FUNCTIONS;
  private static Map<String, List<BuiltinFunction>> BUILTIN_FUNCTIONS;
  private static volatile boolean SHOULD_BOOT_STRAP;
  private static volatile boolean ASYNC_BOOT_STRAP;
  private static volatile boolean BOOT_STRAP_FAILED;
  private static volatile boolean BOOT_STRAPPED;

  private static final Supplier<ClassScanningResult> LOADER_TASK =
      () -> {
        ExecutorService internalExecutor = Executors.newFixedThreadPool(2);
        ClassScanningResult result = new ClassScanningResult();
        var classNames = scanCLasses();
        result.setClassNames(classNames);

        try {
          Callable<Pair<Set<String>, Set<String>>> qualifiersLoaderTask =
              () -> {
                var classQualifiers = getClassQualifiers(classNames.keySet(), false);
                var arabicClassQualifiers = getArabicClassQualifiers(classQualifiers);
                return new Pair<>(classQualifiers, arabicClassQualifiers);
              };
          var qualifiersFuture = internalExecutor.submit(qualifiersLoaderTask);

          Callable<Map<String, Class<?>>> classLoaderTask = () -> loadClasses(classNames, false);
          var classFuture = internalExecutor.submit(classLoaderTask);

          var qualifiersFutureResult = qualifiersFuture.get();
          result.setClassQualifiers(qualifiersFutureResult.a);
          result.setArabicClassQualifiers(qualifiersFutureResult.b);

          result.setClasses(classFuture.get());

          Callable<Map<String, Class<?>>> accessibleClassLoaderTask =
              () -> filterClasses(result.getClasses(), ClassUtils::isAccessibleClass);
          var accessibleClassFuture = internalExecutor.submit(accessibleClassLoaderTask);

          Callable<Map<String, Class<?>>> instantiableClassLoaderTask =
              () -> filterClasses(result.getClasses(), ClassUtils::isInstantiableClass);
          var instantiableClassFuture = internalExecutor.submit(instantiableClassLoaderTask);

          result.setAccessibleClasses(accessibleClassFuture.get());
          result.setInstantiableClasses(instantiableClassFuture.get());

          var accessibleAndInstantiable =
              new HashMap<>(result.getAccessibleClasses()) {
                {
                  putAll(result.getInstantiableClasses());
                }
              };

          Callable<Map<String, List<JvmFunction>>> jvmFunctionsLoaderTask =
              () -> getClassMethods(accessibleAndInstantiable);
          var jvmFunctionsFuture = internalExecutor.submit(jvmFunctionsLoaderTask);

          Callable<Map<String, List<BuiltinFunction>>> builtinFunctionsLoaderTask =
              () -> getBuiltinMethods(accessibleAndInstantiable);
          var builtinFunctionsFuture = internalExecutor.submit(builtinFunctionsLoaderTask);

          result.setJvmFunctions(jvmFunctionsFuture.get());
          result.setBuiltinFunctions(builtinFunctionsFuture.get());

          return result;
        } catch (InterruptedException | ExecutionException e) {
          throw new RuntimeException(e);
        } finally {
          internalExecutor.shutdown();
        }
      };

  private static void setContextFromClassScanningResult(ClassScanningResult result) {
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

  private static final BiConsumer<? super ClassScanningResult, ? super Throwable> LOADER_CONSUMER =
      (result, throwable) -> {
        if (Objects.nonNull(throwable)) {
          defaultBootstrap();
          BOOT_STRAP_FAILED = true;
        } else {
          setContextFromClassScanningResult(result);
          serializeClassScanningResult(result);
        }
      };

  public static void defaultBootstrap() {
    BUILTIN_FUNCTIONS =
        getBuiltinMethods(Builtin.class).stream()
            .map(
                builtinFunction ->
                    Map.entry(builtinFunction.functionInfo().name(), builtinFunction))
            .collect(
                Collectors.groupingBy(
                    Map.Entry::getKey,
                    Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
  }

  private static void callLoader(boolean async) {
    if (async) CompletableFuture.supplyAsync(LOADER_TASK).whenComplete(LOADER_CONSUMER);
    else {
      ClassScanningResult classScanningResult = null;
      Throwable thr = null;
      try {
        classScanningResult = LOADER_TASK.get();
      } catch (Throwable throwable) {
        thr = throwable;
      } finally {
        LOADER_CONSUMER.accept(classScanningResult, thr);
      }
    }
  }

  private static void serializeClassScanningResult(ClassScanningResult result) {
    try {
      var path = Base64SerializationUtils.serialize(result, CACHE_PATH);
      System.out.println("cache saved to " + path);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static void deserializeClassScanningResult() {
    try {
      var result = (ClassScanningResult) Base64SerializationUtils.deserialize(CACHE_PATH);
      serializeClassScanningResult(result);
    } catch (IOException | ClassNotFoundException e) {
      callLoader(ASYNC_BOOT_STRAP);
    }
  }

  public static void bootstrap(boolean async) {
    SHOULD_BOOT_STRAP = Boolean.getBoolean(SCAN_CLASSPATH_PROPERTY);
    ASYNC_BOOT_STRAP = async;
    if (SHOULD_BOOT_STRAP) {

      if (!Files.exists(CACHE_PATH)) {
        callLoader(ASYNC_BOOT_STRAP);
      } else {
        deserializeClassScanningResult();
      }
    } else {
      defaultBootstrap();
    }
  }

  private static Class<?> doGetJavaType(String qualifiedName) {
    if (INSTANTIABLE_CLASSES.containsKey(qualifiedName))
      return INSTANTIABLE_CLASSES.get(qualifiedName);
    if (ACCESSIBLE_CLASSES.containsKey(qualifiedName)) return ACCESSIBLE_CLASSES.get(qualifiedName);
    if (CLASSES.containsKey(qualifiedName)) return CLASSES.get(qualifiedName);
    return Object.class;
  }

  public static Class<?> getJavaType(String qualifiedName) {
    if (SHOULD_BOOT_STRAP && !BOOT_STRAP_FAILED) {
      while (!BOOT_STRAPPED
          && (Objects.isNull(INSTANTIABLE_CLASSES)
              || Objects.isNull(ACCESSIBLE_CLASSES)
              || Objects.isNull(CLASSES))) {
        //  block the execution until bootstrapped
        if (BOOT_STRAP_FAILED) return Object.class;
      }
      return doGetJavaType(qualifiedName);
    }
    return Object.class;
  }

  // instance
  private final DefaultContext parent;
  private final int depth;
  private String functionCallId; // current function in execution inside a context
  private boolean parsingFunctionCallId; // parsing current function in execution
  private boolean parsingAssignment; // parsing an assignment is in execution
  private NaftahParseTreeProperty<Boolean> parseTreeExecution;
  private final Map<String, DeclaredVariable> variables = new HashMap<>();
  private Map<String, DeclaredParameter> parameters; // only use in function call context
  private Map<String, Object> arguments; // only use in function call context
  private final Map<String, DeclaredFunction> functions = new HashMap<>();

  private DefaultContext() {
    this(null, null, null);
  }

  private DefaultContext(Map<String, DeclaredParameter> parameters, Map<String, Object> arguments) {
    this(null, parameters, arguments);
  }

  private DefaultContext(
      DefaultContext parent,
      Map<String, DeclaredParameter> parameters,
      Map<String, Object> arguments) {
    if (Boolean.FALSE.equals(Boolean.getBoolean(INSIDE_SHELL_PROPERTY))
        && parent == null
        && (CONTEXTS.size() != 0)) throw new IllegalStateException("Illegal usage.");
    this.parent = parent;
    this.depth = parent == null ? 0 : parent.getDepth() + 1;
    this.arguments = arguments;
    this.parameters = parameters;
    CONTEXTS.put(depth, this);
  }

  // variables
  public boolean containsVariable(String name) {
    return variables.containsKey(name) || (parent != null && parent.containsVariable(name));
  }

  public Pair<Integer, DeclaredVariable> getVariable(String name, boolean safe) {
    if (variables.containsKey(name)) {
      return new Pair<>(depth, variables.get(name));
    } else if (parent != null) {
      return parent.getVariable(name, safe);
    }

    if (!safe) {
      throw new RuntimeException("Variable not found: " + name);
    }
    return null;
  }

  public void setVariable(String name, DeclaredVariable value) {
    if (variables.containsKey(name)) {
      variables.put(name, value);
    } else if (parent != null && parent.containsVariable(name)) {
      parent.setVariable(name, value);
    } else {
      variables.put(name, value); // define new in current context
    }
  }

  public void defineVariable(String name, DeclaredVariable value) {
    if (variables.containsKey(name)) {
      throw new IllegalStateException("Variable exists in current context");
    }
    variables.put(name, value); // force local
  }

  public void defineVariables(Map<String, DeclaredVariable> variables) {
    if (variables.keySet().stream().anyMatch(this.variables::containsKey)) {
      throw new IllegalStateException("Variable exists in current context");
    }
    this.variables.putAll(variables); // force local
  }

  // functions

  public boolean containsFunction(String name) {
    return functions.containsKey(name)
        || BUILTIN_FUNCTIONS != null && BUILTIN_FUNCTIONS.containsKey(name)
        || (name.matches(QUALIFIED_CALL_REGEX)
            && SHOULD_BOOT_STRAP
            && (!BOOT_STRAPPED || JVM_FUNCTIONS != null && JVM_FUNCTIONS.containsKey(name)))
        || parent != null && parent.containsFunction(name);
  }

  public Pair<Integer, Object> getFunction(String name, boolean safe) {
    if (functions.containsKey(name)) {
      return new Pair<>(depth, functions.get(name));
    } else if (parent != null) {
      return parent.getFunction(name, safe);
    } else { // root parent
      if (BUILTIN_FUNCTIONS.containsKey(name)) {
        var functions = BUILTIN_FUNCTIONS.get(name);
        return new Pair<>(depth, functions.size() == 1 ? functions.get(0) : functions);
      } else if (SHOULD_BOOT_STRAP && !BOOT_STRAP_FAILED) {
        if (BOOT_STRAPPED && JVM_FUNCTIONS.containsKey(name)) {
          var functions = JVM_FUNCTIONS.get(name);
          return new Pair<>(depth, functions.size() == 1 ? functions.get(0) : functions);
        } else if (name.matches(QUALIFIED_CALL_REGEX)) {
          while (!BOOT_STRAPPED && Objects.isNull(JVM_FUNCTIONS)) {
            //  block the execution until bootstrapped
            if (BOOT_STRAP_FAILED) return null;
          }
          var functions = JVM_FUNCTIONS.get(name);
          return new Pair<>(depth, functions.size() == 1 ? functions.get(0) : functions);
        }
      }
    }

    if (!safe) {
      throw new RuntimeException("Function not found: " + name);
    }
    return null;
  }

  public void setFunction(String name, DeclaredFunction value) {
    if (functions.containsKey(name)) {
      functions.put(name, value);
    } else if (parent != null && parent.containsFunction(name)) {
      parent.setFunction(name, value);
    } else {
      functions.put(name, value); // define new in current context
    }
  }

  public void defineFunction(String name, DeclaredFunction value) {
    if (functions.containsKey(name)) {
      throw new IllegalStateException("Function exists in current context");
    }
    functions.put(name, value); // force local
  }

  // functions parameters

  public String getFunctionParameterName(String name) {
    if (parameters == null) parameters = new HashMap<>();
    if (functionCallId != null) {
      String functionName = functionCallId.split("-")[1];
      name = DefaultContext.PARAMETER_NAME_GENERATOR.apply(functionName, name);
    }
    return name;
  }

  public boolean containsFunctionParameter(String name) {
    String functionParameterName = getFunctionParameterName(name);
    return parameters.containsKey(functionParameterName)
        || (parent != null && parent.containsFunctionParameter(name));
  }

  public Pair<Integer, DeclaredParameter> getFunctionParameter(String name, boolean safe) {
    String functionParameterName = getFunctionParameterName(name);
    if (parameters.containsKey(functionParameterName)) {
      return new Pair<>(depth, parameters.get(functionParameterName));
    } else if (parent != null) {
      return parent.getFunctionParameter(name, safe);
    }

    if (!safe) {
      throw new RuntimeException("Function parameter not found: " + name);
    }
    return null;
  }

  public void setFunctionParameter(String name, DeclaredParameter value) {
    String functionParameterName = getFunctionParameterName(name);
    if (parameters.containsKey(functionParameterName)) {
      parameters.put(functionParameterName, value);
    } else if (parent != null && parent.containsFunctionParameter(name)) {
      parent.setFunctionParameter(name, value);
    } else {
      parameters.put(name, value); // define new in current context
    }
  }

  public void defineFunctionParameter(String name, DeclaredParameter value, boolean lenient) {
    name = getFunctionParameterName(name);
    if (parameters.containsKey(name)) {
      if (lenient) return;

      throw new IllegalStateException("Function parameter exists in current context");
    }
    parameters.put(name, value); // force local
  }

  public void defineFunctionParameters(Map<String, DeclaredParameter> parameters, boolean lenient) {
    parameters =
        parameters.entrySet().stream()
            .map(entry -> Map.entry(getFunctionParameterName(entry.getKey()), entry.getValue()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    if (parameters.keySet().stream().anyMatch(this.parameters::containsKey)) {
      if (lenient) return;

      throw new IllegalStateException("Function parameter exists in current context");
    }
    this.parameters.putAll(parameters); // force local
  }

  // functions arguments

  public String getFunctionArgumentName(String name) {
    if (arguments == null) arguments = new HashMap<>();
    if (functionCallId != null) {
      name = DefaultContext.ARGUMENT_NAME_GENERATOR.apply(functionCallId, name);
    }
    return name;
  }

  public boolean containsFunctionArgument(String name) {
    String functionArgumentName = getFunctionArgumentName(name);
    return arguments.containsKey(functionArgumentName)
        || (parent != null && parent.containsFunctionArgument(name));
  }

  public Pair<Integer, Object> getFunctionArgument(String name, boolean safe) {
    String functionArgumentName = getFunctionArgumentName(name);
    if (arguments.containsKey(functionArgumentName)) {
      return new Pair<>(depth, arguments.get(functionArgumentName));
    } else if (parent != null) {
      return parent.getFunctionArgument(name, safe);
    }

    if (!safe) {
      throw new RuntimeException("Function argument not found: " + name);
    }
    return null;
  }

  public void setFunctionArgument(String name, Object value) {
    String functionArgumentName = getFunctionArgumentName(name);
    if (arguments.containsKey(functionArgumentName)) {
      arguments.put(functionArgumentName, value);
    } else if (parent != null && parent.containsFunctionArgument(name)) {
      parent.setFunctionArgument(name, value);
    } else {
      arguments.put(functionArgumentName, value); // define new in current context
    }
  }

  public void defineFunctionArgument(String name, Object value) {
    name = getFunctionArgumentName(name);
    if (arguments.containsKey(name)) {
      throw new IllegalStateException("Function argument exists in current context");
    }
    arguments.put(name, value); // force local
  }

  public void defineFunctionArguments(Map<String, Object> arguments) {
    arguments =
        arguments.entrySet().stream()
            .map(entry -> Map.entry(getFunctionArgumentName(entry.getKey()), entry.getValue()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    if (arguments.keySet().stream().anyMatch(this.arguments::containsKey)) {
      throw new IllegalStateException("Function argument exists in current context");
    }
    this.arguments.putAll(arguments); // force local
  }

  // execution tree

  public void markExecuted(ParseTree node) {
    prepareParseTreeExecution();
    parseTreeExecution.put(node, true);
  }

  public boolean isExecuted(ParseTree node) {
    return prepareParseTreeExecution()
        && Optional.ofNullable(parseTreeExecution.get(node)).orElse(false);
  }

  public <T extends Tree> boolean hasAnyExecutedChildOrSubChildOfType(
      ParseTree node, Class<T> type) {
    return prepareParseTreeExecution()
        && getChildren(true).stream()
            .anyMatch(
                currentContext ->
                    NaftahParserHelper.hasAnyExecutedChildOrSubChildOfType(
                        node, type, currentContext.parseTreeExecution));
  }

  private boolean prepareParseTreeExecution() {
    if (parseTreeExecution == null) {
      parseTreeExecution = new NaftahParseTreeProperty<>();
      return false;
    }
    return true;
  }

  public int getDepth() {
    return depth;
  }

  public List<DefaultContext> getChildren() {
    return getChildren(false);
  }

  public List<DefaultContext> getChildren(boolean includeParent) {
    return CONTEXTS.entrySet().stream()
        .filter(entry -> includeParent ? entry.getKey() >= depth : entry.getKey() > depth)
        .map(Map.Entry::getValue)
        .toList();
  }

  public String getFunctionCallId() {
    return functionCallId;
  }

  public void setFunctionCallId(String functionCallId) {
    this.functionCallId = functionCallId;
  }

  public boolean isParsingFunctionCallId() {
    return parsingFunctionCallId;
  }

  public void setParsingFunctionCallId(boolean parsingFunctionCallId) {
    this.parsingFunctionCallId = parsingFunctionCallId;
  }

  public boolean isParsingAssignment() {
    return parsingAssignment;
  }

  public void setParsingAssignment(boolean parsingAssignment) {
    this.parsingAssignment = parsingAssignment;
  }
}
