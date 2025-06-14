package org.daiitech.naftah.utils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.Tree;
import org.daiitech.naftah.core.builtin.lang.*;
import org.daiitech.naftah.core.parser.NaftahParserHelper;
import org.daiitech.naftah.core.utils.ClassUtils;

import static org.daiitech.naftah.core.utils.ClassUtils.*;
import static org.daiitech.naftah.core.utils.RuntimeClassScanner.*;

/**
 * @author Chakib Daii <br>
 *     TODO: think about a way to vreate child context everytime TODO: and attach it to the function
 *     or variable used at current execution
 */
public class DefaultContext {
  public static final BiFunction<Integer, String, String> FUNCTION_CALL_ID_GENERATOR =
      (depth, functionName) -> "%s-%s-%s".formatted(depth, functionName, UUID.randomUUID());
  public static final BiFunction<String, String, String> PARAMETER_NAME_GENERATOR =
      (functionName, parameterName) -> "%s-%s".formatted(functionName, parameterName);
  public static final BiFunction<String, String, String> ARGUMENT_NAME_GENERATOR =
      (functionCallId, argumentName) -> "%s-%s".formatted(functionCallId, argumentName);

  public static final BiFunction<String, DefaultContext, Object> VARIABLE_GETTER =
      (varName, context) ->
          Optional.ofNullable(context.getFunctionArgument(varName, true))
              .flatMap(
                  functionArgument -> Optional.ofNullable(functionArgument.b).map(Object::toString))
              .orElseGet(
                  () ->
                      Optional.ofNullable(context.getFunctionParameter(varName, true))
                          .flatMap(
                              functionParameter ->
                                  Optional.ofNullable(functionParameter.b.getValue())
                                      .map(Object::toString))
                          .orElseGet(
                              () ->
                                  Optional.ofNullable(context.getVariable(varName, true))
                                      .flatMap(
                                          declaredVariable ->
                                              Optional.ofNullable(declaredVariable.b.getValue())
                                                  .map(Object::toString))
                                      .orElse(null)));

  // CONTEXTS
  private static final Map<Integer, DefaultContext> CONTEXTS = new HashMap<>();

  public static DefaultContext registerContext(
      Map<String, BuiltinFunction> builtinFunctions, Map<String, Method> jvmFunctions) {
    return new DefaultContext(builtinFunctions, jvmFunctions);
  }

  public static DefaultContext registerContext(
      Map<String, BuiltinFunction> builtinFunctions,
      Map<String, Method> jvmFunctions,
      Map<String, DeclaredParameter> parameters,
      Map<String, Object> arguments) {
    return new DefaultContext(builtinFunctions, jvmFunctions, parameters, arguments);
  }

  public static DefaultContext registerContext(DefaultContext parent) {
    return new DefaultContext(parent, null, null, null, null);
  }

  public static DefaultContext registerContext(
      DefaultContext parent,
      Map<String, DeclaredParameter> parameters,
      Map<String, Object> arguments) {
    return new DefaultContext(parent, null, null, parameters, arguments);
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
  private static final Map<String, Optional<? extends ClassLoader>> CLASS_NAMES;
  private static final Set<String> CLASS_QUALIFIERS;
  private static final Set<String> ARABIC_CLASS_QUALIFIERS;
  // qualifiedName -> CLass<?>
  private static final Map<String, Class<?>> CLASSES;
  private static final Map<String, Class<?>> ACCESSIBLE_CLASSES;
  private static final Map<String, Class<?>> INSTANTIABLE_CLASSES;
  // qualifiedCall -> Method
  private static final Map<String, List<JvmFunction>> JVM_FUNCTIONS;
  private static final Map<String, List<BuiltinFunction>> BUILTIN_FUNCTIONS;

  static {
    // TODO: loading should be activated based on a specific flag
    System.out.println("Bootsrapping Runtime...");
    long startTime = System.nanoTime();
    CLASS_NAMES = scanCLasses();
    CLASS_QUALIFIERS = getClassQualifiers(CLASS_NAMES.keySet(), false);
    ARABIC_CLASS_QUALIFIERS = getArabicClassQualifiers(CLASS_QUALIFIERS);
    CLASSES = loadClasses(CLASS_NAMES, false);
    ACCESSIBLE_CLASSES = filterClasses(CLASSES, ClassUtils::isAccessibleClass);
    INSTANTIABLE_CLASSES = filterClasses(CLASSES, ClassUtils::isInstantiableClass);
    var accessibleAndInstantiableClasses = new HashMap<>(ACCESSIBLE_CLASSES) {{
      putAll(INSTANTIABLE_CLASSES);
    }};
    JVM_FUNCTIONS = getClassMethods(accessibleAndInstantiableClasses);
    BUILTIN_FUNCTIONS = getBuiltinMethods(accessibleAndInstantiableClasses);
    long end = System.nanoTime() - startTime;
    System.out.println("took %s ns".formatted(end));
  }

  // instance
  private final DefaultContext parent;
  private final int depth;
  private String functionCallId; // current function in execution inside a context
  private NaftahParseTreeProperty<Boolean> parseTreeExecution;
  private final Map<String, DeclaredVariable> variables = new HashMap<>();
  private Map<String, DeclaredParameter> parameters; // only use in function call context
  private Map<String, Object> arguments; // only use in function call context
  private final Map<String, DeclaredFunction> functions = new HashMap<>();
  // TODO: those will exist in parent only (think about it); it should be part of the class
  private final Map<String, BuiltinFunction> builtinFunctions;
  // TODO: those will exist in parent only (think about it); it should be part of the class
  private final Map<String, Method> jvmFunctions;

  private DefaultContext() {
    throw new IllegalStateException("Illegal usage.");
  }

  private DefaultContext(
      Map<String, BuiltinFunction> builtinFunctions, Map<String, Method> jvmFunctions) {
    this(null, builtinFunctions, jvmFunctions, null, null);
  }

  private DefaultContext(
      Map<String, BuiltinFunction> builtinFunctions,
      Map<String, Method> jvmFunctions,
      Map<String, DeclaredParameter> parameters,
      Map<String, Object> arguments) {
    this(null, builtinFunctions, jvmFunctions, parameters, arguments);
  }

  private DefaultContext(
      DefaultContext parent,
      Map<String, BuiltinFunction> builtinFunctions,
      Map<String, Method> jvmFunctions,
      Map<String, DeclaredParameter> parameters,
      Map<String, Object> arguments) {
    if (parent == null
        && (CONTEXTS.size() != 0 || (builtinFunctions == null || jvmFunctions == null)))
      throw new IllegalStateException("Illegal usage.");
    this.parent = parent;
    this.depth = parent == null ? 0 : parent.getDepth() + 1;
    this.builtinFunctions = builtinFunctions;
    this.jvmFunctions = jvmFunctions;
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
        || (builtinFunctions != null && builtinFunctions.containsKey(name))
        || (jvmFunctions != null && jvmFunctions.containsKey(name))
        || (parent != null && parent.containsFunction(name));
  }

  public Pair<Integer, Object> getFunction(String name, boolean safe) {
    if (functions.containsKey(name)) {
      return new Pair<>(depth, functions.get(name));
    } else if (parent != null) {
      return parent.getFunction(name, safe);
    } else { // root parent
      if (builtinFunctions.containsKey(name)) {
        return new Pair<>(depth, builtinFunctions.get(name));
      } else if (jvmFunctions.containsKey(name)) {
        return new Pair<>(depth, jvmFunctions.get(name));
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
}
