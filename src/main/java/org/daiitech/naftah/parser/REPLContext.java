package org.daiitech.naftah.parser;

import java.util.HashMap;
import java.util.Map;

import org.daiitech.naftah.builtin.lang.DeclaredParameter;

public class REPLContext extends DefaultContext {
  protected static final DefaultContext ETERNAL_CONTEXT;

  static {
    ETERNAL_CONTEXT = DefaultContext.registerContext(new HashMap<>(), new HashMap<>());
    ETERNAL_CONTEXT.prepareParseTreeExecution();
  }

  public static DefaultContext registerContext() {
    DefaultContext context = registerContext(ETERNAL_CONTEXT);
    context.prepareParseTreeExecution();
    return context;
  }

  public static DefaultContext registerContext(
      Map<String, DeclaredParameter> parameters, Map<String, Object> arguments) {
    DefaultContext context = new DefaultContext(ETERNAL_CONTEXT, parameters, arguments);
    context.prepareParseTreeExecution();
    return context;
  }

  public static DefaultContext registerContext(DefaultContext parent) {
    DefaultContext context = new DefaultContext(parent, null, null);
    context.prepareParseTreeExecution();
    return context;
  }

  public static DefaultContext deregisterContext(int depth) {
    if (depth > 0) {
      DefaultContext context = CONTEXTS.remove(depth);
      if (context.parent != null) {
        context.parent.variables.putAll(context.variables);
        context.parent.functions.putAll(context.functions);
        if (context.parseTreeExecution != null)
          context.parent.parseTreeExecution.copyFrom(context.parseTreeExecution);
      }
      return context;
    }
    return CONTEXTS.get(0);
  }
}
