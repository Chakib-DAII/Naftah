package org.daiitech.naftah.builtin.lang;

import static org.daiitech.naftah.utils.reflect.ClassUtils.*;

import java.util.List;
import java.util.Optional;

import org.daiitech.naftah.utils.reflect.ClassUtils;

/**
 * @author Chakib Daii
 */
@Deprecated
public record ScannedClass(
    Class<?> clazz,
    String qualifiedName,
    boolean isAccessible,
    boolean isInstantiable,
    List<JvmFunction> jvmFunctions,
    List<BuiltinFunction> builtinFunctions) {

  public static ScannedClass of(Class<?> clazz, String qualifiedName) {
    return new ScannedClass(
        clazz,
        qualifiedName,
        ClassUtils.isAccessibleClass(clazz),
        ClassUtils.isInstantiableClass(clazz),
        getClassMethods(qualifiedName, clazz),
        getBuiltinMethods(clazz));
  }

  public static ScannedClass of(Class<?> clazz) {
    return of(clazz, getQualifiedName(clazz.getName()));
  }

  public static Optional<ScannedClass> safeOf(Class<?> clazz) {
    ScannedClass scannedClass = null;
    try {
      scannedClass = of(clazz, getQualifiedName(clazz.getName()));
    } catch (Throwable ignored) {
      // Silently skip classes that can't be loaded
    }
    return Optional.ofNullable(scannedClass);
  }
}
