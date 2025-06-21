package org.daiitech.naftah.builtin.lang;

import java.io.*;
import java.lang.reflect.Method;

/**
 * @author Chakib Daii represention of a builtin function
 */
public class BuiltinFunction implements Serializable {
  private transient Method method;
  private final String methodName;
  private final String className;
  private final NaftahFunctionProvider providerInfo;
  private final NaftahFunction functionInfo;

  public BuiltinFunction(
      Method method, NaftahFunctionProvider providerInfo, NaftahFunction functionInfo) {
    this.method = method;
    this.methodName = method.getName();
    this.className = method.getDeclaringClass().getName();
    this.providerInfo = providerInfo;
    this.functionInfo = functionInfo;
  }

  public Method getMethod() {
    return method;
  }

  public NaftahFunctionProvider getProviderInfo() {
    return providerInfo;
  }

  public NaftahFunction getFunctionInfo() {
    return functionInfo;
  }

  @Serial
  private void writeObject(ObjectOutputStream oos) throws IOException {
    oos.defaultWriteObject();
  }

  @Serial
  private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
    ois.defaultReadObject();
    try {
      Class<?> clazz = Class.forName(className);
      for (Method m : clazz.getDeclaredMethods()) {
        if (m.getName().equals(methodName)) {
          this.method = m;
          break;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static BuiltinFunction of(
      Method method, NaftahFunctionProvider providerInfo, NaftahFunction functionInfo) {
    return new BuiltinFunction(method, providerInfo, functionInfo);
  }
}
