package org.daiitech.naftah.utils;

import org.daiitech.naftah.core.builtin.lang.BuiltinFunction;
import org.daiitech.naftah.core.builtin.lang.DeclaredFunction;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Chakib Daii
 **/
public class DefaultContext {
    private final DefaultContext parent;
    private final Map<String, Object> variables = new HashMap<>();

    // TODO: those will exist in parent only (think about it)
    private final Map<String, DeclaredFunction> functions = new HashMap<>();
    // TODO: those will exist in parent only (think about it)
    private final Map<String, BuiltinFunction> builtinFunction = new HashMap<>();
    // TODO: add java functions, in parent only (think about it)

    public DefaultContext() {
        this(null);
    }
    public DefaultContext(DefaultContext parent) {
        this.parent = parent;
    }

    public boolean contains(String name) {
        return variables.containsKey(name) || (parent != null && parent.contains(name));
    }

    public Object get(String name, boolean safe) {
        if (variables.containsKey(name)) {
            return variables.get(name);
        } else if (parent != null) {
            return parent.get(name, safe);
        } else if (!safe){
            throw new RuntimeException("Variable not found: " + name);
        }
        return null;
    }

    public void set(String name, Object value) {
        if (variables.containsKey(name)) {
            variables.put(name, value);
        } else if (parent != null && parent.contains(name)) {
            parent.set(name, value);
        } else {
            variables.put(name, value); // define new in current context
        }
    }

    public void define(String name, Object value) {
        variables.put(name, value); // force local
    }
}
