package org.daiitech.naftah.utils.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.daiitech.naftah.builtin.NaftahFn;
import org.daiitech.naftah.builtin.NaftahFnProvider;
import org.daiitech.naftah.builtin.lang.NaftahFunction;
import org.daiitech.naftah.builtin.lang.NaftahFunctionProvider;

/**
 * @author Chakib Daii
 */
public class AnnotationsUtils {

  /**
   * checks if an annotation is present on a class
   *
   * @param method
   * @param annotations
   * @return @{@link Boolean}
   */
  @SafeVarargs
  public static boolean isAnnotationsPresent(
      Method method, Class<? extends Annotation>... annotations) {
    List<Boolean> annotationsPresence = new ArrayList<>();
    for (var annotation : annotations) {
      annotationsPresence.add(method.isAnnotationPresent(annotation));
    }
    return !annotationsPresence.contains(false);
  }

  /**
   * get present annotations on a method
   *
   * @param method
   * @return @{@link List<Annotation>}
   */
  public static List<Annotation> getMethodAnnotations(Method method) {
    return Arrays.stream(method.getDeclaredAnnotations()).toList();
  }

  public static <T extends Annotation> T getMethodAnnotation(
      Method method, Class<T> annotationClass) {
    return method.getAnnotation(annotationClass);
  }

  public static NaftahFunction getNaftahFunctionAnnotation(Method method) {
    NaftahFn naftahFn = getMethodAnnotation(method, NaftahFn.class);
    return NaftahFunction.of(
        naftahFn.name(),
        naftahFn.description(),
        naftahFn.usage(),
        naftahFn.returnType(),
        naftahFn.parameterTypes(),
        naftahFn.exceptionTypes());
  }

  /**
   * checks if an annotation is present on a class
   *
   * @param aClass
   * @param annotations
   * @return @{@link Boolean}
   */
  @SafeVarargs
  public static boolean isAnnotationsPresent(
      Class<?> aClass, Class<? extends Annotation>... annotations) {
    List<Boolean> annotationsPresence = new ArrayList<>();
    for (var annotation : annotations) {
      annotationsPresence.add(aClass.isAnnotationPresent(annotation));
    }
    return !annotationsPresence.contains(false);
  }

  /**
   * get present annotations on a class
   *
   * @param aClass
   * @return @{@link List<Annotation>}
   */
  public static List<Annotation> getClassAnnotations(Class<?> aClass) {
    return List.of(aClass.getAnnotations());
  }

  public static <T extends Annotation> T getClassAnnotation(
      Class<?> aClass, Class<T> annotationClass) {
    return aClass.getAnnotation(annotationClass);
  }

  public static NaftahFunctionProvider getNaftahFunctionProviderAnnotation(Class<?> aClass) {
    NaftahFnProvider naftahFnProvider = getClassAnnotation(aClass, NaftahFnProvider.class);
    return NaftahFunctionProvider.of(
        naftahFnProvider.name(), naftahFnProvider.description(), naftahFnProvider.functionNames());
  }

  /**
   * get present annotations on a method parameter
   *
   * @param parameter
   * @return @{@link List<Annotation>}
   */
  public static List<Annotation> getParametersAnnotations(Parameter parameter) {
    return Arrays.stream(parameter.getAnnotations()).toList();
  }

  public static <T extends Annotation> T getParametersAnnotation(
      Parameter parameter, Class<T> annotationClass) {
    return parameter.getAnnotation(annotationClass);
  }
}
