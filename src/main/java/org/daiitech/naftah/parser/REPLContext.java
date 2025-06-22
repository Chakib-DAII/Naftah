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
    return registerContext(ETERNAL_CONTEXT);
  }

  public static DefaultContext registerContext(
      Map<String, DeclaredParameter> parameters, Map<String, Object> arguments) {
    return new DefaultContext(ETERNAL_CONTEXT, parameters, arguments);
  }

  public static DefaultContext registerContext(DefaultContext parent) {
    return new DefaultContext(parent, null, null);
  }

  public static DefaultContext deregisterContext(int depth) {
    DefaultContext context = CONTEXTS.remove(depth);
    if (context.parent != null) {
      context.parent.variables.putAll(context.variables);
      context.parent.functions.putAll(context.functions);
      if (context.parseTreeExecution != null)
        context.parent.parseTreeExecution.copyFrom(context.parseTreeExecution);
    }
    return context;
  }
}
