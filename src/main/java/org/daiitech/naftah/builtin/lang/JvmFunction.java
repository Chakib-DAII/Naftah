package org.daiitech.naftah.builtin.lang;

import java.lang.reflect.Method;
import org.daiitech.naftah.utils.reflect.ClassUtils;

/**
 * @author Chakib Daii
 */
public record JvmFunction(
    String qualifiedCall, Class<?> clazz, Method method, boolean isStatic, boolean isInvocable) {

  public static JvmFunction of(String qualifiedCall, Class<?> clazz, Method method) {
    return new JvmFunction(
        qualifiedCall, clazz, method, ClassUtils.isStatic(method), ClassUtils.isInvocable(method));
  }
}
