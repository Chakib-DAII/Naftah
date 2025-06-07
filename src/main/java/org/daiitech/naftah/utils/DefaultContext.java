package org.daiitech.naftah.utils;

import java.lang.reflect.Method;
import java.util.*;

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
  private static final Map<Integer, DefaultContext> CONTEXTS = new HashMap<>();

  public static DefaultContext newContext(Map<String, BuiltinFunction> builtinFunctions, Map<String, Method> jvmFunctions) {
    return new DefaultContext(builtinFunctions, jvmFunctions);
  }
  public static DefaultContext newContext(DefaultContext parent, Map<String, BuiltinFunction> builtinFunctions, Map<String, Method> jvmFunctions) {
    return new DefaultContext(parent, builtinFunctions, jvmFunctions, null, null);
  }
  public static DefaultContext newContext(DefaultContext parent, Map<String, BuiltinFunction> builtinFunctions, Map<String, Method> jvmFunctions,
                                          Map<String, DeclaredParameter> parameters , Map<String, Object> arguments) {
    return new DefaultContext(parent, builtinFunctions, jvmFunctions, parameters, arguments);
  }

  public static DefaultContext getContextByDepth(int depth) {
    return CONTEXTS.get(depth);
  }


  private final DefaultContext parent;
  private final int depth;
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

  private DefaultContext(DefaultContext parent, Map<String, BuiltinFunction> builtinFunctions, Map<String, Method> jvmFunctions,
                         Map<String, DeclaredParameter> parameters , Map<String, Object> arguments) {
    if (parent == null && (builtinFunctions == null || jvmFunctions == null)) throw new IllegalStateException("Illegal usage.");
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

  public DeclaredVariable getVariable(String name, boolean safe) {
    if (variables.containsKey(name)) {
      return variables.get(name);
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
    return functions.containsKey(name) || builtinFunctions.containsKey(name) || jvmFunctions.containsKey(name)
            || (parent != null && parent.containsFunction(name));
  }

  public Object getFunction(String name, boolean safe) {
    if (functions.containsKey(name)) {
      return functions.get(name);
    } else if (parent != null) {
      return parent.getFunction(name, safe);
    } else { // root parent
      if (builtinFunctions.containsKey(name)) {
        return builtinFunctions.get(name);
      } else if (jvmFunctions.containsKey(name)) {
        return jvmFunctions.get(name);
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

  public boolean containsFunctionParameter(String name) {
    return parameters != null && parameters.containsKey(name)
            || (parent != null && parent.containsFunctionParameter(name));
  }

  public DeclaredParameter getFunctionParameter(String name, boolean safe) {
    if (parameters.containsKey(name)) {
      return parameters.get(name);
    } else if (parent != null) {
      return parent.getFunctionParameter(name, safe);
    }

    if (!safe) {
      throw new RuntimeException("Function parameter not found: " + name);
    }
    return null;
  }

  public void setFunctionParameter(String name, DeclaredParameter value) {
    if (parameters.containsKey(name)) {
      parameters.put(name, value);
    } else if (parent != null && parent.containsFunctionParameter(name)) {
      parent.setFunctionParameter(name, value);
    } else {
      parameters.put(name, value); // define new in current context
    }
  }

  public void defineFunctionParameter(String name, DeclaredParameter value) {
    if (parameters.containsKey(name)) {
      throw new IllegalStateException("Function parameter exists in current context");
    }
    parameters.put(name, value); // force local
  }

  public void defineFunctionParameters(Map<String, DeclaredParameter> variables) {
    if (parameters.keySet().stream().anyMatch(this.parameters::containsKey)) {
      throw new IllegalStateException("Function parameter exists in current context");
    }
    this.parameters.putAll(variables); // force local
  }

  // functions arguments

  public boolean containsFunctionArgument(String name) {
    return arguments != null && arguments.containsKey(name)
            || (parent != null && parent.containsFunctionArgument(name));
  }

  public Object getFunctionArgument(String name, boolean safe) {
    if (arguments.containsKey(name)) {
      return arguments.get(name);
    } else if (parent != null) {
      return parent.getFunctionArgument(name, safe);
    }

    if (!safe) {
      throw new RuntimeException("Function argument not found: " + name);
    }
    return null;
  }

  public void setFunctionArgument(String name, Object value) {
    if (arguments.containsKey(name)) {
      arguments.put(name, value);
    } else if (parent != null && parent.containsFunctionArgument(name)) {
      parent.setFunctionArgument(name, value);
    } else {
      arguments.put(name, value); // define new in current context
    }
  }

  public void defineFunctionArgument(String name, Object value) {
    if (arguments.containsKey(name)) {
      throw new IllegalStateException("Function argument exists in current context");
    }
    arguments.put(name, value); // force local
  }

  public void defineFunctionArguments(Map<String, Object> variables) {
    if (arguments.keySet().stream().anyMatch(this.arguments::containsKey)) {
      throw new IllegalStateException("Function argument exists in current context");
    }
    this.arguments.putAll(variables); // force local
  }

  public int getDepth() {
    return depth;
  }
}
