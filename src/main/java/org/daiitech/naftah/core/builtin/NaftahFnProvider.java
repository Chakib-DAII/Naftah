package org.daiitech.naftah.core.builtin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Naftah functions provider metadata
 * should be used on top of classes providing functions
 *
 * @author Chakib Daii
 **/
@Target(ElementType.METHOD)
@Retention(RUNTIME)
public @interface NaftahFnProvider {
    String name();
    String description();
    String[] functionNames();
}
