package org.daiitech.naftah.utils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.misc.Pair;
import org.daiitech.naftah.core.builtin.lang.BuiltinFunction;
import org.daiitech.naftah.core.builtin.lang.DeclaredParameter;
import org.daiitech.naftah.core.builtin.lang.DeclaredFunction;
import org.daiitech.naftah.core.builtin.lang.DeclaredVariable;

/**
 * @author Chakib Daii
 * TODO: think about a way to vreate child context everytime
 * TODO: and attach it to the function or variable used at current execution
 */
public class DefaultContext {
  public static final BiFunction<Integer, String, String> FUNCTION_CALL_ID_GENERATOR = (depth, functionName) -> "%s-%s-%s".formatted(depth, functionName, UUID.randomUUID());
  public static final BiFunction<String, String, String> PARAMETER_NAME_GENERATOR = (functionName, parameterName) -> "%s-%s".formatted(functionName, parameterName);
  public static final BiFunction<String, String, String> ARGUMENT_NAME_GENERATOR = (functionCallId, argumentName) -> "%s-%s".formatted(functionCallId, argumentName);
  private static final Map<Integer, DefaultContext> CONTEXTS = new HashMap<>();

  public static DefaultContext registerContext(Map<String, BuiltinFunction> builtinFunctions, Map<String, Method> jvmFunctions) {
    return new DefaultContext(builtinFunctions, jvmFunctions);
  }

  public static DefaultContext registerContext(Map<String, BuiltinFunction> builtinFunctions, Map<String, Method> jvmFunctions,
                                               Map<String, DeclaredParameter> parameters , Map<String, Object> arguments) {
    return new DefaultContext(builtinFunctions, jvmFunctions,  parameters, arguments);
  }
  public static DefaultContext registerContext(DefaultContext parent) {
    return new DefaultContext(parent, null, null, null, null);
  }
  public static DefaultContext registerContext(DefaultContext parent, Map<String, DeclaredParameter> parameters , Map<String, Object> arguments) {
    return new DefaultContext(parent, null, null, parameters, arguments);
  }

  public static DefaultContext getContextByDepth(int depth) {
    return CONTEXTS.get(depth);
  }

  public static DefaultContext deregisterContext(int depth) {
    return CONTEXTS.remove(depth);
  }

  private final DefaultContext parent;
  private final int depth;
  private String functionCallId; // current function in execution inside a context
  private final Map<String, DeclaredVariable> variables = new HashMap<>();
  private final Map<String, DeclaredParameter> parameters; // only use in function call context
  private final Map<String, Object> arguments; // only use in function call context
  private final Map<String, DeclaredFunction> functions = new HashMap<>();
  // TODO: those will exist in parent only (think about it)
  private final Map<String, BuiltinFunction> builtinFunctions;
  // TODO: those will exist in parent only (think about it)
  private final Map<String, Method> jvmFunctions;

  private DefaultContext() {
      throw new IllegalStateException("Illegal usage.");
  }

  private DefaultContext(Map<String, BuiltinFunction> builtinFunctions, Map<String, Method> jvmFunctions) {
    this(null, builtinFunctions, jvmFunctions, null, null);
  }

  private DefaultContext(Map<String, BuiltinFunction> builtinFunctions, Map<String, Method> jvmFunctions,
                         Map<String, DeclaredParameter> parameters , Map<String, Object> arguments) {
    this(null, builtinFunctions, jvmFunctions,  parameters, arguments);
  }

  private DefaultContext(DefaultContext parent, Map<String, BuiltinFunction> builtinFunctions, Map<String, Method> jvmFunctions,
                         Map<String, DeclaredParameter> parameters , Map<String, Object> arguments) {
    if (parent == null && (CONTEXTS.size() != 0 || (builtinFunctions == null || jvmFunctions == null))) throw new IllegalStateException("Illegal usage.");
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
    return variables.containsKey(name)
            || (parent != null && parent.containsVariable(name));
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
    if (functionCallId != null) {
      String functionName = functionCallId.split("-")[1];
      name = DefaultContext.PARAMETER_NAME_GENERATOR.apply(functionName, name);
    }
    return name;
  }

  public boolean containsFunctionParameter(String name) {
    if (parameters != null) {
      name = getFunctionParameterName(name);
      return parameters.containsKey(name)
              || (parent != null && parent.containsFunctionParameter(name));
    }
    return false;
  }

  public Pair<Integer, DeclaredParameter> getFunctionParameter(String name, boolean safe) {
    if (parameters != null && parameters.containsKey(name)) {
      name = getFunctionParameterName(name);
      return new Pair<>(depth, parameters.get(name));
    } else if (parent != null) {
      return parent.getFunctionParameter(name, safe);
    }

    if (!safe) {
      throw new RuntimeException("Function parameter not found: " + name);
    }
    return null;
  }

  public void setFunctionParameter(String name, DeclaredParameter value) {
    if (parameters != null && parameters.containsKey(name)) {
      name = getFunctionParameterName(name);
      parameters.put(name, value);
    } else if (parent != null && parent.containsFunctionParameter(name)) {
      parent.setFunctionParameter(name, value);
    } else {
      parameters.put(name, value); // define new in current context
    }
  }

  public void defineFunctionParameter(String name, DeclaredParameter value, boolean lenient) {
    if (parameters != null) {
      name = getFunctionParameterName(name);
      if (parameters.containsKey(name)) {
        if (lenient) return;

        throw new IllegalStateException("Function parameter exists in current context");
      }
      parameters.put(name, value); // force local
    }
  }

  public void defineFunctionParameters(Map<String, DeclaredParameter> parameters, boolean lenient) {
    if (this.parameters != null) {
      if (parameters.keySet().stream().anyMatch(this.parameters::containsKey)) {
        if (lenient) return;

        throw new IllegalStateException("Function parameter exists in current context");
      }
      this.parameters.putAll(parameters.entrySet().stream()
              .map(entry -> Map.entry(getFunctionParameterName(entry.getKey()), entry.getValue()))
              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))); // force local
      
    }
  }

  // functions arguments

  public String getFunctionArgumentName(String name) {
    if (functionCallId != null) {
      name = DefaultContext.ARGUMENT_NAME_GENERATOR.apply(functionCallId, name);
    }
    return name;
  }

  public boolean containsFunctionArgument(String name) {
    if ( arguments != null) {
      name = getFunctionArgumentName(name);
      return arguments.containsKey(name)
              || (parent != null && parent.containsFunctionArgument(name));
    }
    return false;
  }

  public Pair<Integer, Object> getFunctionArgument(String name, boolean safe) {
    if (arguments != null && arguments.containsKey(name)) {
      name = getFunctionArgumentName(name);
      return new Pair<>(depth, arguments.get(name));
    } else if (parent != null) {
      return parent.getFunctionArgument(name, safe);
    }

    if (!safe) {
      throw new RuntimeException("Function argument not found: " + name);
    }
    return null;
  }

  public void setFunctionArgument(String name, Object value) {
    if (arguments != null && arguments.containsKey(name)) {
      name = getFunctionArgumentName(name);
      arguments.put(name, value);
    } else if (parent != null && parent.containsFunctionArgument(name)) {
      parent.setFunctionArgument(name, value);
    } else if (arguments != null){
      arguments.put(name, value); // define new in current context
    }
  }

  public void defineFunctionArgument(String name, Object value) {
    if (arguments != null) {
      name = getFunctionArgumentName(name);
      if (arguments.containsKey(name)) {
        throw new IllegalStateException("Function argument exists in current context");
      }
      arguments.put(name, value); // force local
    }
  }

  public void defineFunctionArguments(Map<String, Object> arguments) {
    if (this.arguments != null) {
      if (arguments.keySet().stream().anyMatch(this.arguments::containsKey)) {
        throw new IllegalStateException("Function argument exists in current context");
      }
      this.arguments.putAll(arguments.entrySet().stream()
              .map(entry -> Map.entry(getFunctionArgumentName(entry.getKey()), entry.getValue()))
              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))); // force local
    }
  }

  public int getDepth() {
    return depth;
  }

  public String getFunctionCallId() {
    return functionCallId;
  }

  public void setFunctionCallId(String functionCallId) {
    this.functionCallId = functionCallId;
  }
}
