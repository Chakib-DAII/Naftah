package org.daiitech.naftah.core.builtin.lang;

import java.lang.reflect.Method;
import org.daiitech.naftah.core.builtin.NaftahFn;
import org.daiitech.naftah.core.builtin.NaftahFnProvider;

/**
 * @author Chakib Daii represention of a builtin function
 */
public class BuiltinFunction {
  Method method;
  NaftahFnProvider providerInfo;
  NaftahFn functionInfo;
}
