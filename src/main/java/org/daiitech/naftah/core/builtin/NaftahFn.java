package org.daiitech.naftah.core.builtin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Naftah function metadata as part of built-in
 *
 * @author Chakib Daii
 **/
@Target(ElementType.METHOD)
@Retention(RUNTIME)
public @interface NaftahFn {
    String name();
    String description();
    String usage();
    Class<?> returnType() default Void.class;
    Class<?>[] parameterTypes() default {};
    Class<?>[] exceptionTypes() default {};

}
