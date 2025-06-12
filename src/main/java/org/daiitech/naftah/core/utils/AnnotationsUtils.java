package org.daiitech.naftah.core.utils;

import org.daiitech.naftah.core.builtin.NaftahFn;
import org.daiitech.naftah.core.builtin.NaftahFnProvider;
import org.daiitech.naftah.core.builtin.lang.NaftahFunction;
import org.daiitech.naftah.core.builtin.lang.NaftahFunctionProvider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Chakib Daii
 **/
public class AnnotationsUtils {

    /**
     * checks if an annotation is present on a class
     *
     * @param aClass
     * @param annotations
     * @return @{@link Boolean}
     */
    public static boolean isAnnotationsPresent(
            Class<?> aClass, List<Class<? extends Annotation>> annotations) {
        List<Boolean> annotationsPresence = new ArrayList<>();
        annotations.forEach(
                annotation -> annotationsPresence.add(aClass.isAnnotationPresent(annotation)));
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


    public static <T extends Annotation> T getMethodAnnotation(Method method, Class<T> annotationClass) {
        return method.getAnnotation(annotationClass);
    }

    public static NaftahFunction getNaftahFunctionAnnotation(Class<?> aClass) {
        NaftahFn naftahFnProvider = getClassAnnotation(aClass, NaftahFn.class);
        return NaftahFunction.of(naftahFnProvider.name(), naftahFnProvider.description(),
                naftahFnProvider.usage(), naftahFnProvider.returnType(), naftahFnProvider.parameterTypes(),
                naftahFnProvider.exceptionTypes());
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

    public static <T extends Annotation> T getClassAnnotation(Class<?> aClass, Class<T> annotationClass) {
        return aClass.getAnnotation(annotationClass);
    }

    public static NaftahFunctionProvider getNaftahFunctionProviderAnnotation(Class<?> aClass) {
        NaftahFnProvider naftahFnProvider = getClassAnnotation(aClass, NaftahFnProvider.class);
        return NaftahFunctionProvider.of(naftahFnProvider.name(), naftahFnProvider.description(), naftahFnProvider.functionNames());
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


    public static <T extends Annotation> T getParametersAnnotation(Parameter parameter, Class<T> annotationClass) {
        return parameter.getAnnotation(annotationClass);
    }

}
