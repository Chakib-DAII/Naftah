package org.daiitech.naftah.core.builtin.lang;

import java.lang.reflect.Method;

/**
 * @author Chakib Daii represention of a builtin function
 */
public record BuiltinFunction(
        Method method,
        NaftahFunctionProvider providerInfo,
        NaftahFunction functionInfo) {
  public static BuiltinFunction of(
          Method method,
          NaftahFunctionProvider providerInfo,
          NaftahFunction functionInfo) {
    return new BuiltinFunction(method, providerInfo, functionInfo);
  }
}
