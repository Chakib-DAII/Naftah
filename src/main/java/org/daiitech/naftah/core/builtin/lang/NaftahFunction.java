package org.daiitech.naftah.core.builtin.lang;

import java.util.List;

/**
 * @author Chakib Daii
 **/
public record NaftahFunction(
        String name,
        String description,
        String usage,
        Class<?> returnType,
        List<Class<?>> parameterTypes,
        List<Class<?>> exceptionTypes) {

    public static NaftahFunction of(
            String name,
            String description,
            String usage,
            Class<?> returnType,
            Class<?>[] parameterTypes,
            Class<?>[] exceptionTypes) {
        return new NaftahFunction(name, description, usage, returnType, List.of(parameterTypes), List.of(exceptionTypes));
    }
}
