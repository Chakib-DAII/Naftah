package org.daiitech.naftah.core.builtin.lang;

import org.daiitech.naftah.core.parser.NaftahParser;

/**
 * @author Chakib Daii
 **/
public class DeclaredVariable {
    private final NaftahParser.AssignmentContext originalContext;
    private final String name;
    private final boolean constant;
    private final Object type;
    private final Object defaultValue;

    private DeclaredVariable(NaftahParser.AssignmentContext originalContext, String name, boolean constant,
                            Object type, Object defaultValue) {
        this.originalContext = originalContext;
        this.name = name;
        this.constant = constant;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    public NaftahParser.AssignmentContext getOriginalContext() {
        return originalContext;
    }

    public String getName() {
        return name;
    }

    public boolean isConstant() {
        return constant;
    }

    public Object getType() {
        return type;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String toString() {
        return "<%s %s>"
                .formatted(name, constant ? "ثابت" : "متغير");
    }

    public static DeclaredVariable of(NaftahParser.AssignmentContext originalContext, String name, boolean constant,
                     Object type, Object defaultValue) {
        return new DeclaredVariable(originalContext, name, constant, type, defaultValue);
    }

}
