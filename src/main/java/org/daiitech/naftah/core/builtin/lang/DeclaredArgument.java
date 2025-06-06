package org.daiitech.naftah.core.builtin.lang;

import org.daiitech.naftah.core.parser.NaftahParser;

/**
 * @author Chakib Daii
 **/
public class DeclaredArgument {
    private final NaftahParser.ArgumentDeclarationContext originalContext;
    private final String name;
    private final boolean constant;
    private final Object type;
    private final Object defaultValue;

    private DeclaredArgument(NaftahParser.ArgumentDeclarationContext originalContext, String name, boolean constant,
                             Object type, Object defaultValue) {
        this.originalContext = originalContext;
        this.name = name;
        this.constant = constant;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    public NaftahParser.ArgumentDeclarationContext getOriginalContext() {
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

    public static DeclaredArgument of(NaftahParser.ArgumentDeclarationContext originalContext, String name, boolean constant,
                                      Object type, Object defaultValue) {
        return new DeclaredArgument(originalContext, name, constant, type, defaultValue);
    }

}
