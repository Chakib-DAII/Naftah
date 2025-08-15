package org.daiitech.naftah.builtin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to mark a method as a Naftah built-in function.
 * <p>
 * This annotation provides metadata about the function, including its name,
 * description, usage, return type, parameter types, and exceptions it may throw.
 * </p>
 *
 * <p>مثال على الاستخدام:</p>
 * <pre>
 * {@code
 * @NaftahFn(
 * name = "جمع",
 * description = "يحسب مجموع رقمين",
 * usage = "جمع(int أ, int ب)",
 * returnType = int.class,
 * parameterTypes = {int.class, int.class},
 * exceptionTypes = {}
 * )
 * public int جمع(int أ, int ب) { ... }
 * }
 * </pre>
 *
 * @author Chakib Daii
 */
@Target(ElementType.METHOD)
@Retention(RUNTIME)
public @interface NaftahFn {
	/**
	 * The function name as exposed to the Naftah environment.
	 *
	 * @return the function name
	 */
	String name();

	/**
	 * A brief description of what the function does.
	 *
	 * @return the function description
	 */
	String description();

	/**
	 * The usage string or signature for the function.
	 *
	 * @return the usage information
	 */
	String usage();

	/**
	 * The return type class of the function.
	 * Defaults to {@code Void.class} if no return value.
	 *
	 * @return the return type class
	 */
	Class<?> returnType() default Void.class;

	/**
	 * The array of parameter type classes accepted by the function.
	 *
	 * @return array of parameter types
	 */
	Class<?>[] parameterTypes() default {};

	/**
	 * The array of exception type classes that the function may throw.
	 *
	 * @return array of exception types
	 */
	Class<?>[] exceptionTypes() default {};
}
