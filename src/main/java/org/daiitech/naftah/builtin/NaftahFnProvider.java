package org.daiitech.naftah.builtin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to mark a class as a provider of Naftah functions.
 * <p>
 * Classes annotated with this should provide the specified functions.
 * This annotation includes metadata such as the provider's name,
 * description, and the list of function names it offers.
 * </p>
 *
 * <p>مثال على الاستخدام:</p>
 * <pre>
 * {@code
 * @NaftahFnProvider(
 * name = "مزود الدوال الرياضية",
 * description = "يوفر دوال حسابية متنوعة مثل الجمع والطرح",
 * functionNames = {"جمع", "طرح"}
 * )
 * public class MathFunctionProvider {
 * // تعريف الدوال هنا
 * }
 * }
 * </pre>
 *
 * @author Chakib Daii
 */
@Target(ElementType.TYPE)
@Retention(RUNTIME)
public @interface NaftahFnProvider {
	/**
	 * The provider's name.
	 *
	 * @return the name of the function provider
	 */
	String name();

	/**
	 * flags that the function names should be bound with the provider's name
	 *
	 * @return true if we should use qualified name; false if not
	 */
	boolean useQualifiedName() default false;

	/**
	 * flags that the function aliases should be bound with the provider's name
	 *
	 * @return true if we should use qualified name; false if not
	 */
	boolean useQualifiedAliases() default false;

	/**
	 * A brief description of the function provider.
	 *
	 * @return the description of the provider
	 */
	String description();

	/**
	 * The list of function names provided by this provider.
	 *
	 * @return an array of function names
	 */
	String[] functionNames();
}
