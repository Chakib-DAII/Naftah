package org.daiitech.naftah.builtin.lang;

import java.io.*;
import java.lang.reflect.Method;
import org.daiitech.naftah.utils.reflect.ClassUtils;

/**
 * @author Chakib Daii
 */
public class JvmFunction implements Serializable {
  private final String qualifiedCall;
  private final Class<?> clazz;
  private transient Method method;
  private final String methodName;
  private final boolean isStatic;
  private final boolean isInvocable;

  public JvmFunction(
      String qualifiedCall, Class<?> clazz, Method method, boolean isStatic, boolean isInvocable) {
    this.qualifiedCall = qualifiedCall;
    this.clazz = clazz;
    this.method = method;
    this.methodName = method.getName();
    this.isStatic = isStatic;
    this.isInvocable = isInvocable;
  }

  public String getQualifiedCall() {
    return qualifiedCall;
  }

  public Class<?> getClazz() {
    return clazz;
  }

  public Method getMethod() {
    return method;
  }

  public String getMethodName() {
    return methodName;
  }

  public boolean isStatic() {
    return isStatic;
  }

  public boolean isInvocable() {
    return isInvocable;
  }

  @Serial
  private void writeObject(ObjectOutputStream oos) throws IOException {
    oos.defaultWriteObject();
  }

  @Serial
  private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
    try {
      ois.defaultReadObject();
      for (Method m : clazz.getDeclaredMethods()) {
        if (m.getName().equals(methodName)) {
          this.method = m;
          break;
        }
      }
    } catch (Throwable ignored) {
    }
  }

  @Override
  public String toString() {
    return "<%s %s>".formatted("دالة", this.qualifiedCall);
  }

  public static JvmFunction of(String qualifiedCall, Class<?> clazz, Method method) {
    return new JvmFunction(
        qualifiedCall, clazz, method, ClassUtils.isStatic(method), ClassUtils.isInvocable(method));
  }
}
