package org.daiitech.naftah.errors;

import java.util.Arrays;

import org.antlr.v4.runtime.Parser;
import org.daiitech.naftah.builtin.lang.DynamicNumber;

import static org.daiitech.naftah.parser.NaftahParserHelper.getFormattedTokenSymbols;

/**
 * Utility class for exception-related helper methods.
 * <p>
 * This class provides static methods to retrieve the root cause or the most specific cause
 * of an exception, as well as a factory method to create a custom {@link NaftahBugError}
 * related to invalid loop labels.
 * </p>
 *
 * <p><b>Note:</b> This class cannot be instantiated. Attempting to instantiate
 * it will always throw a {@link NaftahBugError}.</p>
 *
 * @author Chakib Daii
 */
public final class ExceptionUtils {
	/**
	 * Error message indicating an overflow in a decimal number.
	 * The value provided is infinite and thus invalid.
	 * Example message format: "تجاوز في العدد العشري: القيمة غير نهائية للمدخل 'value'."
	 */
	public static final String INFINITE_DECIMAL_ERROR = "تجاوز في العدد العشري: القيمة غير نهائية للمدخل '%s'.";

	/**
	 * Error message indicating that the value is Not-a-Number (NaN).
	 * Example message format: "القيمة ليست رقمًا (NaN): 'value'"
	 */
	public static final String NAN_DECIMAL_ERROR = "القيمة ليست رقمًا (NaN): '%s'";
	/**
	 * Error message indicating that bitwise operations are not supported on decimal (floating point) numbers.
	 * The message includes the value and the attempted operation.
	 * Example message format: "العمليات الثنائية (bitwise) غير مدعومة على الأعداد ذات الفاصلة العشرية: 'value'
	 * operation،"
	 */
	public static final String UNSUPPORTED_BITWISE_DECIMAL_ERROR = "العمليات الثنائية (bitwise) غير مدعومة على " + "الأعداد" + " ذات الفاصلة العشرية:  '%s' %s،";

	/**
	 * Error message indicating that a single argument is empty.
	 * Arabic: "لا يمكن أن يكون الوسيط فارغًا."
	 */
	public static final String EMPTY_ARGUMENT_ERROR = "لا يمكن أن يكون الوسيط فارغًا (%s).";
	/**
	 * Error message indicating that multiple arguments are empty.
	 * Arabic: "لا يمكن أن تكون الوسائط فارغة."
	 */
	public static final String EMPTY_ARGUMENTS_ERROR = "لا يمكن أن تكون الوسائط فارغة (%s)، (%s).";

	/**
	 * Private constructor to prevent instantiation.
	 * Always throws a {@link NaftahBugError} when called.
	 */
	private ExceptionUtils() {
		throw newNaftahBugInvalidUsageError();
	}

	/**
	 * Returns the root cause of the given {@link Throwable}.
	 * <p>
	 * Traverses the causal chain to find the deepest cause of the throwable.
	 * If the original throwable is {@code null}, returns {@code null}.
	 * </p>
	 *
	 * @param original the throwable to inspect
	 * @return the root cause throwable, or {@code null} if none found or if {@code original} is {@code null}
	 */
	public static Throwable getRootCause(Throwable original) {
		if (original == null) {
			return null;
		}
		else {
			Throwable rootCause = null;

			for (Throwable cause = original.getCause(); cause != null && cause != rootCause; cause = cause.getCause()) {
				rootCause = cause;
			}

			return rootCause;
		}
	}

	/**
	 * Returns the most specific cause of the given {@link Throwable}.
	 * <p>
	 * This method returns the deepest cause (root cause) if available,
	 * otherwise returns the original throwable.
	 * </p>
	 *
	 * @param original the throwable to inspect
	 * @return the most specific cause (root cause if present) or the original throwable
	 */
	public static Throwable getMostSpecificCause(Throwable original) {
		Throwable rootCause = getRootCause(original);
		return rootCause != null ? rootCause : original;
	}

	/**
	 * Creates a new {@link NaftahBugError} indicating an invalid reuse of a loop label.
	 * <p>
	 * Constructs an error message in Arabic describing that the same loop label cannot be
	 * used in the specified statement.
	 * </p>
	 *
	 * @param label  the loop label name that was reused
	 * @param parser the parser instance used to obtain vocabulary information
	 * @return a new {@link NaftahBugError} with a localized error message
	 */
	public static NaftahBugError newNaftahBugInvalidLoopLabelError(String label, Parser parser) {
		return new NaftahBugError(String
				.format("لا يمكن استخدام تسمية الحلقة نفسها '%s' في جملة '%s'.",
						label,
						getFormattedTokenSymbols(   parser.getVocabulary(),
													org.daiitech.naftah.parser.NaftahLexer.BREAK,
													false)));
	}

	/**
	 * Creates a new {@link NaftahBugError} indicating that an invalid usage has occurred.
	 *
	 * @return a {@code NaftahBugError} describing the invalid usage error
	 */
	public static NaftahBugError newNaftahBugInvalidUsageError() {
		return new NaftahBugError("استخدام غير مسموح به.");
	}


	/**
	 * Creates a new {@link NaftahBugError} indicating that a required input argument is null or missing.
	 *
	 * <p>This method selects the appropriate error message depending on whether the error is related to a
	 * single input or multiple inputs.</p>
	 *
	 * @param singleInput {@code true} if the error is due to a single missing input argument;
	 *                    {@code false} if multiple input arguments are missing.
	 * @return a {@link NaftahBugError} instance with the corresponding error message.
	 */
	public static NaftahBugError newNaftahBugNullInputError(boolean singleInput, Object... args) {
		return new NaftahBugError(singleInput ?
				EMPTY_ARGUMENT_ERROR.formatted(args) :
				EMPTY_ARGUMENTS_ERROR.formatted(args));
	}


	/**
	 * Raise an <em>overflow</em> exception for the given number and target class.
	 *
	 * @param number      the number we tried to convert
	 * @param targetClass the target class we tried to convert to
	 * @throws IllegalArgumentException if there is an overflow
	 */
	public static NaftahBugError newNaftahBugInvalidNumberConversionOverflowError(  boolean overflow,
																					Number number,
																					Class<?> targetClass) {
		// TODO: classNames should be in arabic or transliterated or in naftah language types
		return new NaftahBugError(
									"تعذر تحويل الرقم '%s' من النوع '%s' إلى فئة الهدف غير المدعومة '%s' %s."
											.formatted( number,
														number.getClass().getName(),
														targetClass.getName(),
														overflow ? "بسبب: تجاوز السعة" : ""));
	}

	/**
	 * Creates a new {@link NaftahBugError} indicating that the given object
	 * represents an invalid numeric value.
	 *
	 * @param object the invalid numeric value
	 * @return a {@code NaftahBugError} describing the invalid number value error
	 */
	public static NaftahBugError newNaftahBugInvalidNumberValueError(Object object) {
		return new NaftahBugError("قيمة رقمية غير صالحة: '%s'".formatted(object));
	}

	/**
	 * Creates a new {@link NaftahBugError} indicating that one or more
	 * numeric types are unsupported.
	 *
	 * @param singleInput whether the error is for a single input (true) or multiple inputs (false)
	 * @param dn          the dynamic numbers involved in the error
	 * @return a {@code NaftahBugError} describing the unsupported number type(s) error
	 */
	public static NaftahBugError newNaftahBugUnsupportedNumbersError(boolean singleInput, DynamicNumber... dn) {
		Class<?>[] args = Arrays.stream(dn).map(dynamicNumber -> dynamicNumber.get().getClass()).toArray(Class[]::new);
		// Unknown or unsupported number types
		// TODO: classNames should be in arabic or transliterated or in naftah language types
		return new NaftahBugError((singleInput ? "نوع الرقم غير مدعوم: '%s'" : "أنواع الأرقام غير مدعومة: '%s'، '%s'")
				.formatted((Object[]) args));
	}


	/**
	 * Creates a NaftahBugError indicating that bitwise operations on decimal numbers are unsupported.
	 *
	 * @param dn the decimal numbers on which the bitwise operation was attempted
	 * @return a NaftahBugError with an explanatory message
	 */
	public static NaftahBugError newNaftahBugUnsupportedBitwiseDecimalError(boolean singleInput, DynamicNumber... dn) {
		return new NaftahBugError(
									singleInput ?
											UNSUPPORTED_BITWISE_DECIMAL_ERROR.formatted(dn[0], "") :
											UNSUPPORTED_BITWISE_DECIMAL_ERROR
													.formatted(dn[0], "'%s'".formatted(dn[1])));

	}

	/**
	 * Creates a new {@link NaftahBugError} indicating that the specified key
	 * was not found in the associative array.
	 *
	 * @param key the key that was not found
	 * @return a {@code NaftahBugError} describing the missing key error
	 */
	public static NaftahBugError newNaftahKeyNotFoundError(Object key) {
		return new NaftahBugError("المفتاح '%s' غير موجود في المصفوفة الترابطية الثانية.".formatted(key));
	}

	/**
	 * Creates a new {@link NaftahBugError} indicating that negative numbers
	 * are not allowed.
	 *
	 * @return a {@code NaftahBugError} describing the disallowed negative number error
	 */
	public static NaftahBugError newNaftahNegativeNumberError() {
		return new NaftahBugError("لا يُسمح بالأعداد السالبة.");
	}
}
