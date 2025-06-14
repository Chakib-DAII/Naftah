package org.daiitech.naftah.core.builtin.lang;

import org.daiitech.naftah.core.utils.ClassUtils;

import java.lang.reflect.Method;

/**
 * @author Chakib Daii
 **/
public record JvmFunction(String qualifiedCall,
                          Class<?> clazz,
                          Method method,
                          boolean isStatic,
                          boolean isInvocable) {

    public static JvmFunction of(String qualifiedCall, Class<?> clazz, Method method) {
        return new JvmFunction(qualifiedCall, clazz, method,
                ClassUtils.isStatic(method),
                ClassUtils.isInvocable(method));
    }
}
