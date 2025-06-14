package org.daiitech.naftah.core.builtin.lang;

import org.daiitech.naftah.core.utils.ClassUtils;

import java.util.List;

import static org.daiitech.naftah.core.utils.ClassUtils.getQualifiedName;

/**
 * @author Chakib Daii
 **/
public record ScannedClass(Class<?> clazz,
                           String qualifiedName,
                           boolean isAccessible,
                           boolean isInstantiable,
                           List<JvmFunction> jvmFunctions,
                           List<BuiltinFunction> builtinFunctions
                           ) {

    public static ScannedClass of(Class<?> clazz,
                                  String qualifiedName,
                                  List<JvmFunction> jvmFunctions,
                                  List<BuiltinFunction> builtinFunctions
    ) {
        return new ScannedClass(clazz,
                qualifiedName,
                ClassUtils.isAccessibleClass(clazz),
                ClassUtils.isInstantiableClass(clazz),
                jvmFunctions,
                builtinFunctions);
    }

    public static ScannedClass of(Class<?> clazz,
                                  List<JvmFunction> jvmFunctions,
                                  List<BuiltinFunction> builtinFunctions
    ) {
        return of(clazz,
                getQualifiedName(clazz.getName()),
                jvmFunctions,
                builtinFunctions);
    }
}
