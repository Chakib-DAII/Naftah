package org.daiitech.naftah.errors;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiFunction;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.misc.Pair;
import org.daiitech.naftah.builtin.lang.DynamicNumber;

import static org.daiitech.naftah.builtin.utils.ObjectUtils.getNaftahType;
import static org.daiitech.naftah.parser.DefaultNaftahParserVisitor.PARSER_VOCABULARY;
import static org.daiitech.naftah.parser.NaftahParserHelper.getFormattedTokenSymbols;
import static org.daiitech.naftah.utils.reflect.ClassUtils.getQualifiedName;

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
	public static final String UNSUPPORTED_BITWISE_DECIMAL_ERROR = """
																	العمليات الثنائية (bitwise) غير مدعومة على الأعداد ذات الفاصلة العشرية:  '%s' %s،""";

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
	 * Note prefix used in error messages.
	 * <p>
	 * Arabic: "ملاحظة:"
	 * </p>
	 */
	public static final String NOTE = "\nملاحظة:\n";
	/**
	 * Generates an error message indicating that an instance method was called incorrectly.
	 * <p>
	 * The message is in Arabic and explains that the method is not static and requires the
	 * first argument (or second if the method is from a list of functions) to be the instance
	 * on which the method will be invoked.
	 * </p>
	 *
	 * <p>Usage example:
	 * <pre>
	 * String msg = INVALID_INSTANCE_METHOD_CALL_MSG.apply("methodName", additionalInfo);
	 * </pre>
	 * </p>
	 * <p>
	 * Parameters:
	 * <ul>
	 * <li>First: the method name ({@code %s})</li>
	 * <li>Second: additional context or details ({@code %s})</li>
	 * </ul>
	 */
	public static final BiFunction<String, String, String> INVALID_INSTANCE_METHOD_CALL_MSG = """
																								الدالة '%s' ليست ثابتة (instance method)، يجب تزويد الوسيط الأول (الثاني في حالة الدالة المرتبطة بقائمة من الدوال) كالكائن (instance) الذي سيتم استدعاء الدالة عليه.

																								%s
																								"""::formatted;

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
	public static NaftahBugError newNaftahBugInvalidLoopLabelError(String label, Parser parser, int line, int column) {
		return new NaftahBugError(String
				.format("لا يمكن استخدام تسمية الحلقة نفسها '%s' في جملة '%s'.",
						label,
						getFormattedTokenSymbols(   parser.getVocabulary(),
													org.daiitech.naftah.parser.NaftahLexer.BREAK,
													false)), line, column);
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
		return new NaftahBugError(
									"تعذر تحويل الرقم '%s' من النوع '%s' إلى فئة الهدف غير المدعومة '%s' %s."
											.formatted( number,
														Objects.isNull(PARSER_VOCABULARY) ?
																getQualifiedName(number.getClass().getName()) :
																getNaftahType(PARSER_VOCABULARY, number.getClass()),
														Objects.isNull(PARSER_VOCABULARY) ?
																getQualifiedName(targetClass.getName()) :
																getNaftahType(PARSER_VOCABULARY, targetClass),
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
		return newNaftahBugInvalidNumberValueError(object, null);
	}

	/**
	 * Creates a new {@link NaftahBugError} for an invalid numeric value, with the cause.
	 * <p>
	 * This method provides more detail by attaching the underlying {@link Exception}
	 * that caused the failure (e.g., {@code NumberFormatException}).
	 *
	 * @param object    the invalid numeric value
	 * @param exception the underlying exception (may be {@code null})
	 * @return a {@code NaftahBugError} describing the error with the given cause
	 */
	public static NaftahBugError newNaftahBugInvalidNumberValueError(Object object, Exception exception) {
		return new NaftahBugError("قيمة رقمية غير صالحة: '%s'".formatted(object), exception);
	}

	/**
	 * Creates a new {@link NaftahBugError} for an invalid numeric value in a specific radix.
	 * <p>
	 * This is useful when parsing fails for a given base (e.g., base 16, base 10),
	 * and you want to include the radix in the error message.
	 *
	 * @param object the invalid numeric value
	 * @param radix  the number system base (e.g., 10 for decimal)
	 * @return a {@code NaftahBugError} describing the error for the specified radix
	 */
	public static NaftahBugError newNaftahBugInvalidNumberValueError(Object object, int radix) {
		return newNaftahBugInvalidNumberValueError(object, radix, null);
	}

	/**
	 * Creates a new {@link NaftahBugError} for an invalid numeric value in a specific radix,
	 * including the underlying cause.
	 * <p>
	 * This is the most detailed version, allowing you to specify the erroneous value,
	 * the radix used during parsing, and the original exception that triggered the failure.
	 *
	 * @param object    the invalid numeric value
	 * @param radix     the number system base in which parsing was attempted
	 * @param exception the root exception that caused the error (nullable)
	 * @return a {@code NaftahBugError} describing the full error context
	 */
	public static NaftahBugError newNaftahBugInvalidNumberValueError(Object object, int radix, Exception exception) {
		return new NaftahBugError(  "قيمة رقمية غير صالحة: '%s' في النظام العددي %d"
											.formatted(
														object instanceof Pair<?, ?> pair ?
																(!pair.b.equals(pair.a) ?
																		"%s ← %s"
																				.formatted(pair.b, pair.a) :
																		pair.a) :
																object,
														radix),
									exception);
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
		String[] args = Arrays
				.stream(dn)
				.map(dynamicNumber -> Objects.isNull(PARSER_VOCABULARY) ?
						getQualifiedName(dynamicNumber.get().getClass().getName()) :
						getNaftahType(PARSER_VOCABULARY, dynamicNumber.get().getClass()))
				.toArray(String[]::new);
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


	/**
	 * Creates a new {@link NaftahBugError} indicating that the specified function is not supported.
	 *
	 * @param functionName  the name of the unsupported function.
	 * @param functionClass the class providing the function.
	 * @param line          the line number in the source code where the error occurred.
	 * @param column        the column number in the source code where the error occurred.
	 * @return a {@code NaftahBugError} describing the unsupported function.
	 */
	public static NaftahBugError newNaftahUnsupportedFunctionError( String functionName,
																	Class<?> functionClass,
																	int line,
																	int column) {
		throw new NaftahBugError(   "الدالة '%s' من النوع: '%s' غير مدعومة حالياً"
											.formatted( functionName,
														functionClass.getName()),
									line,
									column);
	}

	/**
	 * Creates a new {@link NaftahBugError} for illegal argument count or mismatched argument types.
	 *
	 * @param functionName           the name of the function being invoked.
	 * @param providerClassName      the class providing the function.
	 * @param paramCount             the expected number of parameters.
	 * @param argCount               the actual number of arguments provided.
	 * @param functionDetailedString detailed string representation of the function.
	 * @param e                      the underlying exception, if any.
	 * @param line                   the line number where the error occurred.
	 * @param column                 the column number where the error occurred.
	 * @return a {@code NaftahBugError} describing the illegal argument error.
	 */
	public static NaftahBugError newNaftahIllegalArgumentError( String functionName,
																String providerClassName,
																int paramCount,
																int argCount,
																String functionDetailedString,
																Exception e,
																int line,
																int column) {

		throw new NaftahBugError(   """
									عدد الوسائط غير صحيح للمُستدعى '%s' المقدمة من '%s'.
									العدد المتوقع: %d،
									العدد الفعلي: %d.

									%s
									"""
											.formatted( functionName,
														providerClassName,
														paramCount,
														argCount,
														functionDetailedString
											),
									e,
									line,
									column);
	}

	/**
	 * Creates a new {@link NaftahBugError} for errors occurring during function invocation.
	 *
	 * @param functionName           the name of the function being invoked.
	 * @param functionDetailedString detailed string representation of the function.
	 * @param e                      the underlying exception.
	 * @param line                   the line number where the error occurred.
	 * @param column                 the column number where the error occurred.
	 * @return a {@code NaftahBugError} describing the invocation error.
	 */
	public static NaftahBugError newNaftahInvocationError(  String functionName,
															String functionDetailedString,
															Exception e,
															int line,
															int column) {
		throw new NaftahBugError(   """
									.'%s' حدث خطأ أثناء استدعاء المُستدعى

									%s
									"""
											.formatted( functionName,
														functionDetailedString),
									e,
									line,
									column);
	}

	/**
	 * Creates a new {@link NaftahBugError} for functions that are not invocable.
	 *
	 * @param functionName           the name of the function.
	 * @param functionDetailedString detailed string representation of the function.
	 * @param line                   the line number where the error occurred.
	 * @param column                 the column number where the error occurred.
	 * @return a {@code NaftahBugError} describing the non-invocable function error.
	 */
	public static NaftahBugError newNaftahNonInvocableFunctionError(String functionName,
																	String functionDetailedString,
																	int line,
																	int column) {
		throw new NaftahBugError(
									"""
									الدالة '%s' غير قابلة للاستدعاء.

									%s
									"""
											.formatted(functionName, functionDetailedString),
									line,
									column
		);
	}

	/**
	 * Creates a new {@link NaftahBugError} when object instantiation fails.
	 *
	 * @param functionName           the name of the function triggering instantiation.
	 * @param functionDetailedString detailed string representation of the function.
	 * @param e                      the underlying exception.
	 * @param line                   the line number where the error occurred.
	 * @param column                 the column number where the error occurred.
	 * @return a {@code NaftahBugError} describing the instantiation failure.
	 */
	public static NaftahBugError newNaftahInstantiationError(   String functionName,
																String functionDetailedString,
																Exception e,
																int line,
																int column) {
		throw new NaftahBugError(
									"""
									تعذر إنشاء كائن من الفئة المطلوبة '%s' أثناء تنفيذ المُستدعى أو أثناء تهيئة أحد الوسائط عند التحويل إلى نوع الطريقة المناسب.

									%s
									"""
											.formatted(functionName, functionDetailedString),
									e,
									line,
									column
		);
	}

	/**
	 * Creates and throws a new {@link NaftahBugError} when the requested invocable
	 * (method or constructor) cannot be found in the current context.
	 *
	 * <p>The generated error message (in Arabic) indicates that the target
	 * callable symbol is undefined or inaccessible.</p>
	 *
	 * @param functionName the name of the missing invocable (method or constructor).
	 * @param line         the source line number where the error occurred.
	 * @param column       the source column number where the error occurred.
	 * @return this method never returns normally; it always throws a {@link NaftahBugError}.
	 * @throws NaftahBugError always thrown to indicate a missing invocable.
	 */
	public static NaftahBugError newNaftahInvocableNotFoundError(   String functionName,
																	int line,
																	int column) {
		throw new NaftahBugError(   "المُستدعى '%s' غير موجودة في السياق الحالي."
											.formatted(functionName),
									line,
									column);
	}

}
