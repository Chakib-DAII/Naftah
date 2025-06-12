package org.daiitech.naftah.core.utils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * @author Chakib Daii
 */
public final class ClassUtils {
    /**
     * returns a set of class parts of the provided paths or classpath and jdk classnames if not
     *
     * @return set of qualified class names parts
     */
    public static Set<String> getClassQualifiers(Set<String> classNames) {
        return classNames.stream()
                .flatMap(s -> Arrays.stream(s.split("\\.")))
                // TODO: replace all $ with the right code
                .collect(Collectors.toSet());
    }

    /**
     * returns a set of class parts of the provided paths or classpath and jdk classnames if not
     *
     * @return set of qualified class names parts transliterated to arabic
     */
    public static Set<String> getArabicClassQualifiers(Set<String> classQualifiers) {
        return classQualifiers.stream()
                .map(ArabicUtils::transliterateToArabicScript)
                .collect(Collectors.toSet());
    }

    /**
     * returns methods mapped to the input classes filtered by method filter
     * Usage:
     * <pre>
     *      Set<Class<?>> classes = ClassUtils.getClasses();
     *      Predicate<Method> instancePublicMethodPredicate = method -> Modifier.isPublic(method.getModifiers());
     *      Predicate<Method> classPublicMethodPredicate = instancePublicMethodPredicate.and(method -> Modifier.isStatic(method.getModifiers()));
     *      Map<Class<?>, Method[]> instancePublicMethods = ClassUtils.getClassMethods(classes, instancePublicMethodPredicate);
     *      Map<Class<?>, Method[]> classPublicMethods = ClassUtils.getClassMethods(classes, classPublicMethodPredicate);
     * </pre>
     * @param classes set of classes having methods as members
     * @param methodPredicate methods filter
     * @return hashtable (Map) of classes and methods
     */
    public static Map<Class<?>, Method[]> getClassMethods(Set<Class<?>> classes, Predicate<Method> methodPredicate) {
        return classes.stream()
                .filter(Objects::nonNull)
                .map(aClass -> {
                    try {
                        return Map.entry(aClass, Arrays.stream(aClass.getMethods()).filter(methodPredicate).toArray(Method[]::new));
                    } catch (Throwable e) {
                        //skip
//                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * returns methods mapped to the input classes
     * Usage:
     * <pre>
     *      Set<Class<?>> classes = ClassUtils.getClasses();
     *      Map<Class<?>, Method[]> methods = ClassUtils.getClassMethods(classes);
     * </pre>
     * @param classes set of classes having methods as members
     * @return hashtable (Map) of classes and methods
     */
    public static Map<Class<?>, Method[]> getClassMethods(Set<Class<?>> classes) {
        return getClassMethods(classes, (method) -> true);
    }

    /**
     * returns method names mapped to all classes in the classpath and jdk
     * Usage:
     * <pre>
     *      Map<Class<?>, String[]> methods = ClassUtils.getClassMethodNames();
     * </pre>
     * @return hashtable (Map) of classes and method names
     */
    public static Map<Class<?>, String[]> getClassMethodNames(Set<Class<?>> classes) {
        return getClassMethods(classes).entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), Arrays.stream(entry.getValue())
                        .map(Method::getName)
                        .collect(Collectors.toSet())
                        .toArray(String[]::new)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * returns method names mapped to all classes in the classpath and jdk
     * Usage:
     * <pre>
     *      Map<Class<?>, String[]> methods = ClassUtils.getClassArabicMethodNames();
     * </pre>
     * @return hashtable (Map) of classes and method names transliterated to arabic
     */
    public static Map<Class<?>, String[]> getClassArabicMethodNames(Set<Class<?>> classes) {
        return getClassMethodNames(classes).entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), Arrays.stream(entry.getValue())
                        .map(ArabicUtils::transliterateToArabicScript)
                        .collect(Collectors.toSet())
                        .toArray(String[]::new)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
