package org.daiitech.naftah.builtin;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Naftah functions provider metadata should be used on top of classes providing functions
 *
 * @author Chakib Daii
 */
@Target(ElementType.TYPE)
@Retention(RUNTIME)
public @interface NaftahFnProvider {
  String name();

  String description();

  String[] functionNames();
}
