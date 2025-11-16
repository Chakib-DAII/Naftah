package org.daiitech.naftah.builtin;

import java.util.Objects;

import org.daiitech.naftah.builtin.lang.DynamicNumber;
import org.daiitech.naftah.builtin.utils.NumberUtils;
import org.daiitech.naftah.builtin.utils.ObjectUtils;
import org.daiitech.naftah.builtin.utils.op.BinaryOperation;
import org.daiitech.naftah.builtin.utils.op.UnaryOperation;
import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.utils.arabic.ArabicUtils;

import static org.daiitech.naftah.builtin.utils.FunctionUtils.allMatch;
import static org.daiitech.naftah.builtin.utils.FunctionUtils.reduce;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.applyOperation;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.getNaftahValueToString;
import static org.daiitech.naftah.errors.ExceptionUtils.EMPTY_ARGUMENTS_ERROR;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;
import static org.daiitech.naftah.parser.NaftahParserHelper.NULL;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.padText;

/**
 * Provides built-in functions used within the Naftah language for performing various
 * arithmetic and logical operations with precision and efficiency.
 *
 * <p>This class contains static methods that implement fundamental operations such as
 * addition, subtraction, multiplication, division, logical comparisons, bitwise operations,
 * and printing. All functions are annotated to be recognized by the Naftah interpreter.
 *
 * <p><b>Note:</b> This class cannot be instantiated.
 *
 * @author Chakib Daii
 */
@NaftahFnProvider(  name = "مزوّد دوال مدمجة",
					description = """
									يحتوي هذا الموفر على دوال مدمجة تُستخدم ضمن لغة نفطه لأداء عمليات حسابية ومنطقية مختلفة بدقة وكفاءة.""",
					functionNames = {
										"هل_أكبر_من",
										"هل_لا_يساوي",
										"تحليل_رقم_بنظام_العد",
										"إزاحة_إلى_اليمين_غير_موقعة",
										"الأكبر",
										"إنفي",
										"إقسم",
										"إزاحة_إلى_اليسار",
										"هل_يساوي",
										"و_بتي",
										"باقي_القسمة",
										"إجمع",
										"هل_أصغر_من",
										"أو_بتي",
										"نقصان_قبلي",
										"القيمة_المطلقة",
										"إطبع",
										"إلى_نص",
										"أرضي",
										"إرفع",
										"الأصغر",
										"هل_صفر",
										"سقف",
										"هل_أكبر_أو_يساوي",
										"إضرب",
										"إطرح",
										"إشارة",
										"نقصان_بعدي",
										"ليس_بتي",
										"زيادة_بعدية",
										"هل_أصغر_أو_يساوي",
										"حصري_أو_بتي",
										"إزاحة_إلى_اليمين",
										"تقريب",
										"جذر",
										"زيادة_قبلية",
										"و_منطقي",
										"أو_منطقي",
										"أس",
										"جمع_عنصر_ب_عنصر",
										"طرح_عنصر_ب_عنصر",
										"ضرب_عنصر_ب_عنصر",
										"قسمة_عنصر_ب_عنصر",
										"باقي_القسمة_عنصر_ب_عنصر",
										"عكس_الإشارة",
										"نفي_منطقي"
					})
public final class Builtin {

	/**
	 * Private constructor to prevent instantiation.
	 * Throws {@link NaftahBugError} if called.
	 */
	private Builtin() {
		throw newNaftahBugInvalidUsageError();
	}

	/**
	 * Prints the string representation of the given object to the output.
	 * If the object is null, prints a predefined NULL representation.
	 *
	 * @param o the object to print; can be any object
	 */
	@NaftahFn(  name = "إطبع",
				aliases = {"ڨول", "قول", "قل", "ڨلي", "ڨلّي", "قلي", "قلّي", "إكتب"},
				description = """
								تعليمة الطباعة (إطبع) هي التعليمة التي تُستخدم في البرمجة لإظهار نص معين على الشاشة، مثل إظهار رسالة ترحيبية للمستخدم.""",
				usage = "إطبع(ش)",
				parameterTypes = {Object.class})
	public static void print(Object o) {
		if (Objects.nonNull(o)) {
			padText(getNaftahValueToString(o), true);
		}
		else {
			System.out.println(padText(NULL, false));
		}
	}


	/**
	 * Returns the string representation of the given object.
	 * If the object is {@code null}, returns a predefined NULL string.
	 *
	 * @param o the object to convert to string
	 * @return a string representation of the object, or a NULL representation if the object is null
	 */
	@NaftahFn(
				name = "إلى_نص",
				aliases = {"كنص", "نص", "سلسلة"},
				description = """
								تُحوّل أي قيمة إلى تمثيلها النصي (سلسلة من الحروف).
								تُستخدم عندما تحتاج إلى طباعة أو تنسيق القيم داخل سلاسل نصية.
								""",
				usage = "إلى_نص(قيمة)",
				parameterTypes = {Object.class},
				returnType = String.class
	)
	public static String toString(Object o) {
		if (Objects.nonNull(o)) {
			return getNaftahValueToString(o);
		}
		else {
			return NULL;
		}
	}

	/**
	 * Adds two values and returns their sum.
	 *
	 * @param <T>   the type of the operands
	 * @param left  the first value
	 * @param right the second value
	 * @return the sum of left and right
	 */
	@NaftahFn(  name = "إجمع",
				description = """
								إضافة الأعداد معًا للحصول على مجموع. هو العملية الأساسية التي تُستخدم في الرياضيات لتحديد القيمة الإجمالية من خلال جمع عدة أرقام.""",
				usage = "إجمع(ش ، ي)",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object add(T left, T right) {
		return reduce(applyOperation(left, right, BinaryOperation.ADD), BinaryOperation.ADD::apply);
	}

	/**
	 * Logical AND operation with short-circuit evaluation.
	 * <p>
	 * Returns the second operand if the first operand is "truthy"; otherwise returns the first operand.
	 * <p>
	 * Supports numbers, booleans, characters, and strings.
	 * Uses custom "truthy" rules via {@code isTruthy()} to determine truthiness:
	 * <ul>
	 * <li>Numbers: non-zero values are truthy.</li>
	 * <li>Booleans: {@code true} is truthy, {@code false} is falsy.</li>
	 * <li>Characters: non-null, non-zero characters are truthy.</li>
	 * <li>Strings: non-empty strings are truthy.</li>
	 * </ul>
	 *
	 * @param <T>   the type of operands (Number, Boolean, Character, String)
	 * @param left  the first value
	 * @param right the second value
	 * @return the second operand if the first is truthy, otherwise the first operand
	 */
	@NaftahFn(  name = "و_منطقي",
				description = """
								إجراء عملية "و" المنطقية بين قيمتين. تُعيد الدالة القيمة الصحيحة فقط إذا كانت كلتا القيمتين صحيحتين، وتُستخدم في البرمجة والمنطق للتحقق من تحقق شرطين معًا.""",
				usage = "و_منطقي(س ، ص)",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object logicalAnd(T left, T right) {
		return reduce(applyOperation(left, right, BinaryOperation.AND), BinaryOperation.ADD::apply);
	}

	/**
	 * Logical OR operation with short-circuit evaluation.
	 * <p>
	 * Returns the first operand if it is "truthy"; otherwise returns the second operand.
	 * <p>
	 * Supports numbers, booleans, characters, and strings.
	 * Uses custom "truthy" rules via {@code isTruthy()} to determine truthiness:
	 * <ul>
	 * <li>Numbers: non-zero values are truthy.</li>
	 * <li>Booleans: {@code true} is truthy, {@code false} is falsy.</li>
	 * <li>Characters: non-null, non-zero characters are truthy.</li>
	 * <li>Strings: non-empty strings are truthy.</li>
	 * </ul>
	 *
	 * @param <T>   the type of operands (Number, Boolean, Character, String)
	 * @param left  the first value
	 * @param right the second value
	 * @return the first "truthy" operand, or the second operand if the first is falsy
	 */
	@NaftahFn(  name = "أو_منطقي",
				description = """
								إجراء عملية "أو" المنطقية بين قيمتين. تُعيد الدالة القيمة الصحيحة إذا كانت إحدى القيمتين أو كلتاهما صحيحتين، وتُستخدم في البرمجة والمنطق للتحقق من تحقق أحد الشرطين على الأقل.""",
				usage = "أو_منطقي(س ، ص)",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object logicalOr(T left, T right) {
		return reduce(applyOperation(left, right, BinaryOperation.OR), BinaryOperation.ADD::apply);
	}

	/**
	 * Subtracts the second value from the first and returns the difference.
	 *
	 * @param <T>   the type of the operands
	 * @param left  the minuend
	 * @param right the subtrahend
	 * @return the difference between left and right
	 */
	@NaftahFn(  name = "إطرح",
				description = """
								طرح الأعداد للحصول على الفرق. هو العملية الأساسية التي تُستخدم في الرياضيات لتحديد القيمة المتبقية عند إزالة قيمة عدد من عدد آخر.""",
				usage = "إطرح(ش ، ي)",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object subtract(T left, T right) {
		return reduce(applyOperation(left, right, BinaryOperation.SUBTRACT), BinaryOperation.ADD::apply);
	}

	/**
	 * Multiplies two values and returns the product.
	 *
	 * @param <T>   the type of the operands
	 * @param left  the first factor
	 * @param right the second factor
	 * @return the product of left and right
	 */
	@NaftahFn(  name = "إضرب",
				description = """
								ضرب الأعداد للحصول على الناتج هو العملية الأساسية التي تُستخدم في الرياضيات لتحديد القيمة الإجمالية عند تكرار جمع عدد معين عدة مرات.""",
				usage = "إضرب(ش ، ي)",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object multiply(T left, T right) {
		return reduce(applyOperation(left, right, BinaryOperation.MULTIPLY), BinaryOperation.ADD::apply);
	}

	/**
	 * Raises a value to a given power.
	 * <p>
	 * Computes the result of raising the first operand (base) to the second operand (exponent).
	 * Supports numbers and compatible numeric types.
	 *
	 * @param <T>   the type of the operands
	 * @param left  the base value
	 * @param right the exponent value
	 * @return the result of left raised to the power of right
	 */
	@NaftahFn(  name = "أس",
				description = """
								رفع قيمة إلى قوة معينة. تُعيد الدالة النتيجة الناتجة عن رفع القيمة الأولى (الأساس) إلى القيمة الثانية (الأس)، وتُستخدم في الرياضيات والبرمجة لإجراء العمليات الأسية.""",
				usage = "أس(س ، ص)",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object pow(T left, T right) {
		return reduce(applyOperation(left, right, BinaryOperation.POWER), BinaryOperation.ADD::apply);
	}

	/**
	 * Divides the first value by the second and returns the quotient.
	 *
	 * @param <T>   the type of the operands
	 * @param left  the dividend
	 * @param right the divisor
	 * @return the quotient of left divided by right
	 */
	@NaftahFn(  name = "إقسم",
				description = """
								قسمة الأعداد للحصول على خارج القسمة هي العملية الأساسية التي تُستخدم في الرياضيات لتحديد كم مرة يمكن تقسيم عدد إلى أجزاء متساوية.""",
				usage = "إقسم(ش ، ي)",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object divide(T left, T right) {
		return reduce(applyOperation(left, right, BinaryOperation.DIVIDE), BinaryOperation.ADD::apply);
	}

	/**
	 * Calculates the remainder of the division of the first value by the second.
	 *
	 * @param <T>   the type of the operands
	 * @param left  the dividend
	 * @param right the divisor
	 * @return the remainder after dividing left by right
	 */
	@NaftahFn(  name = "باقي_القسمة",
				description = """
								عملية باقي القسمة (المودولو) هي العملية التي تُستخدم في الرياضيات لتحديد الباقي المتبقي بعد قسمة عدد على عدد آخر.""",
				usage = "باقي_القسمة(ش ، ي)",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object modulo(T left, T right) {
		return reduce(applyOperation(left, right, BinaryOperation.MODULO), BinaryOperation.ADD::apply);
	}

	/**
	 * The (max) function is used to compare two numbers and return the larger one.
	 * This operation is useful for determining the higher value when comparing two numeric values.
	 *
	 * @param <T>   The type of number, must be Number or a subclass
	 * @param left  The first number to compare
	 * @param right The second number to compare
	 * @return The larger number between left and right
	 * @usage max(left, right)
	 */
	@NaftahFn(  name = "الأكبر",
				description = """
								دالة (الأكبر) تُستخدم لمقارنة عددين وإرجاع العدد الأكبر بينهما. تُفيد هذه العملية في تحديد القيمة العليا عند المفاضلة بين قيمتين عدديتين.""",
				usage = "الأكبر(ش ، ي)",
				parameterTypes = {Number.class, Number.class},
				returnType = Number.class)
	public static <T extends Number> Number max(T left, T right) {
		return NumberUtils.max(left, right);
	}


	/**
	 * The (min) function is used to compare two numbers and return the smaller one.
	 * This operation is useful for determining the lower value when comparing two numeric values.
	 *
	 * @param <T>   The type of number, must be Number or a subclass
	 * @param left  The first number to compare
	 * @param right The second number to compare
	 * @return The smaller number between left and right
	 * @usage min(left, right)
	 */
	@NaftahFn(  name = "الأصغر",
				description = """
								دالة (الأصغر) تُستخدم لمقارنة عددين وإرجاع العدد الأصغر بينهما. تُفيد هذه العملية في تحديد القيمة الدنيا عند المفاضلة بين قيمتين عدديتين.""",
				usage = "الأصغر(ش ، ي)",
				parameterTypes = {Number.class, Number.class},
				returnType = Number.class)
	public static <T extends Number> Number min(T left, T right) {
		return NumberUtils.min(left, right);
	}


	/**
	 * The (pow) function is used to raise a base number to an integer exponent.
	 * This operation is useful in mathematical calculations requiring exponential repetition such as squares or cubes.
	 *
	 * @param <T>      The type of the base, must be Number or a subclass
	 * @param base     The base number to be raised
	 * @param exponent The power to which the base is raised (integer)
	 * @return The result of raising the base to the specified power
	 * @usage pow(base, exponent)
	 */
	@NaftahFn(  name = "إرفع",
				description = """
								دالة (إرفع) تُستخدم لرفع عدد (الأساس) إلى قوة عدد صحيح (الأس). تُفيد هذه العملية في الحسابات الرياضية التي تتطلب التكرار الأسي مثل حساب المربعات أو المكعبات.""",
				usage = "إرفع(الأساس ، الأس)",
				parameterTypes = {Number.class, Number.class},
				returnType = Number.class)
	public static <T extends Number> Number pow(T base, T exponent) {
		return NumberUtils.pow(base, exponent);
	}


	/**
	 * The (round) function is used to round a decimal number to the nearest integer.
	 * This operation is useful in calculations requiring precise integers.
	 *
	 * @param <T> The type of decimal number, must be Number or a subclass
	 * @param x   The decimal number to round
	 * @return The number rounded to the nearest integer
	 * @usage round(x)
	 */
	@NaftahFn(  name = "تقريب",
				description = """
								دالة (تقريب) تُستخدم لتقريب عدد عشري إلى أقرب عدد صحيح. تُفيد هذه العملية في العمليات الحسابية التي تتطلب أعدادًا صحيحة دقيقة.""",
				usage = "تقريب(ش)",
				parameterTypes = {Number.class},
				returnType = Number.class)
	public static <T extends Number> Number round(T x) {
		return NumberUtils.round(x);
	}


	/**
	 * The (floor) function returns the largest integer less than or equal to the given number.
	 * This operation is useful in calculations that require rounding down numbers.
	 *
	 * @param <T> The type of number, must be Number or a subclass
	 * @param x   The number to floor
	 * @return The largest integer less than or equal to the given number
	 * @usage floor(x)
	 */
	@NaftahFn(  name = "أرضي",
				description = """
								دالة (أرضي) تُستخدم لإرجاع أكبر عدد صحيح أصغر من أو يساوي العدد المعطى. تُفيد هذه العملية في العمليات الحسابية التي تتطلب تقريب الأعداد إلى الأسفل.""",
				usage = "أرضي(ش)",
				parameterTypes = {Number.class},
				returnType = Number.class)
	public static <T extends Number> Number floor(T x) {
		return NumberUtils.floor(x);
	}


	/**
	 * The (ceil) function returns the smallest integer greater than or equal to the given number.
	 * This operation is useful in calculations that require rounding up numbers.
	 *
	 * @param <T> The type of number, must be Number or a subclass
	 * @param x   The number to ceil
	 * @return The smallest integer greater than or equal to the given number
	 * @usage ceil(x)
	 */
	@NaftahFn(  name = "سقف",
				description = """
								دالة (سقف) تُستخدم لإرجاع أصغر عدد صحيح أكبر من أو يساوي العدد المعطى. تُفيد هذه العملية في العمليات الحسابية التي تتطلب تقريب الأعداد إلى الأعلى.""",
				usage = "سقف(ش)",
				parameterTypes = {Number.class},
				returnType = Number.class)
	public static <T extends Number> Number ceil(T x) {
		return NumberUtils.ceil(x);
	}


	/**
	 * The (negate) function returns the given number after changing it to its negative value.
	 * This operation is useful in calculations that require reversing the numeric sign.
	 *
	 * @param <T> The type of number, must be Number or a subclass
	 * @param x   The number to negate
	 * @return The given number after changing it to its negative value
	 * @usage negate(x)
	 */
	@NaftahFn(  name = "إنفي",
				description = """
								دالة (إنفي) تُستخدم لإرجاع العدد المعطى بعد تغييره إلى قيمته السالبة. تُفيد هذه العملية في العمليات الحسابية التي تتطلب عكس الإشارة العددية.""",
				usage = "إنفي(ش)",
				parameterTypes = {Number.class},
				returnType = Number.class)
	public static <T extends Number> Number negate(T x) {
		return NumberUtils.negate(x);
	}

	/**
	 * The (sqrt) function is used to calculate the square root of the given number.
	 * This operation is useful in calculations that require finding a number whose square equals the original number.
	 *
	 * @param <T> The type of number, must be Number or a subclass
	 * @param x   The number to calculate the square root of
	 * @return The square root of the given number
	 * @usage sqrt(x)
	 */
	@NaftahFn(  name = "جذر",
				description = """
								دالة (جذر) تُستخدم لحساب الجذر التربيعي للعدد المعطى. تُفيد هذه العملية في العمليات الحسابية التي تتطلب إيجاد قيمة العدد الذي مربعه يساوي العدد الأصلي.""",
				usage = "جذر(ش)",
				parameterTypes = {Number.class},
				returnType = Number.class)
	public static <T extends Number> Number sqrt(T x) {
		return NumberUtils.sqrt(x);
	}


	/**
	 * The (abs) function is used to calculate the absolute value of the given number,
	 * i.e., removing the negative sign if present.
	 * This operation is useful in calculations that require a positive value always.
	 *
	 * @param <T> The type of number, must be Number or a subclass
	 * @param x   The number to calculate the absolute value for
	 * @return The absolute value of the given number
	 * @usage abs(x)
	 */
	@NaftahFn(  name = "القيمة_المطلقة",
				description = """
								دالة (القيمة_المطلقة) تُستخدم لحساب القيمة المطلقة للعدد المعطى، أي إزالة إشارة السالب إن وجدت. تُفيد هذه العملية في الحسابات التي تتطلب قيمة موجبة دائمًا.""",
				usage = "القيمة_المطلقة(ش)",
				parameterTypes = {Number.class},
				returnType = Number.class)
	public static <T extends Number> Number abs(T x) {
		return NumberUtils.abs(x);
	}


	/**
	 * The (signum) function is used to determine the sign of the given number:
	 * returns -1 if the number is negative, 0 if zero, and 1 if positive.
	 *
	 * @param <T> The type of number, must be Number or a subclass
	 * @param x   The number to determine its sign
	 * @return -1 if negative, 0 if zero, 1 if positive
	 * @usage signum(x)
	 */
	@NaftahFn(  name = "إشارة",
				description = """
								دالة (إشارة) تُستخدم لتحديد إشارة العدد المعطى: ترجع -1 إذا كان العدد سالبًا، 0 إذا كان صفرًا، و1 إذا كان موجبًا.""",
				usage = "إشارة(ش)",
				parameterTypes = {Number.class},
				returnType = int.class)
	public static <T extends Number> int signum(T x) {
		return NumberUtils.signum(x);
	}

	/**
	 * The (isZero) function checks if the given number is zero.
	 * Returns true if the number is zero, false otherwise.
	 *
	 * @param <T> The type of number, must be Number or subclass
	 * @param x   The number to check
	 * @return true if x is zero, false otherwise
	 * @usage isZero(x)
	 */
	@NaftahFn(  name = "هل_صفر",
				description = """
								دالة (هل_صفر) تُستخدم للتحقق مما إذا كان العدد المعطى يساوي صفرًا. تُرجع صحيح إذا كان العدد صفرًا، وخطأ خلاف ذلك.""",
				usage = "هل_صفر(ش)",
				parameterTypes = {Number.class},
				returnType = boolean.class)
	public static <T extends Number> boolean isZero(T x) {
		return NumberUtils.isZero(x);
	}

	/**
	 * The (equals) function compares two values to check if they are equal.
	 * Returns true if both are equal, false otherwise.
	 *
	 * @param <T>   The type of the objects being compared
	 * @param left  The first object
	 * @param right The second object
	 * @return true if left equals right, false otherwise
	 * @usage equals(left, right)
	 */
	@NaftahFn(  name = "هل_يساوي",
				description = """
								دالة (هل_يساوي) تُستخدم لمقارنة عددين والتحقق مما إذا كانا متساويين في القيمة. تُرجع صحيح إذا كان العددان متساويين، وخطأ خلاف ذلك.""",
				usage = "هل_يساوي(ش ، ي)",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> boolean equals(T left, T right) {
		try {
			return allMatch(applyOperation(left, right, BinaryOperation.EQUALS), Boolean.TRUE::equals);
		}
		catch (NaftahBugError bug) {
			if (bug.getBugText().equals(EMPTY_ARGUMENTS_ERROR.formatted(left, right))) {
				throw bug;
			}
			return left.equals(right);
		}
	}

	/**
	 * The (notEquals) function compares two values to check if they are not equal.
	 * Returns true if they are different, false otherwise.
	 *
	 * @param <T>   The type of the objects being compared
	 * @param left  The first object
	 * @param right The second object
	 * @return true if left does not equal right, false otherwise
	 * @usage notEquals(left, right)
	 */
	@NaftahFn(  name = "هل_لا_يساوي",
				description = """
								دالة (هل_لا_يساوي) تُستخدم لمقارنة عددين والتحقق مما إذا كانا غير متساويين في القيمة. تُرجع صحيح إذا كان العددان غير متساويين، وخطأ خلاف ذلك.""",
				usage = "هل_لا_يساوي(ش ، ي)",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> boolean notEquals(T left, T right) {
		try {
			return allMatch(applyOperation(left, right, BinaryOperation.NOT_EQUALS), Boolean.TRUE::equals);
		}
		catch (NaftahBugError bug) {
			if (bug.getBugText().equals(EMPTY_ARGUMENTS_ERROR.formatted(left, right))) {
				throw bug;
			}
			return !left.equals(right);
		}
	}

	/**
	 * The (lessThan) function checks if the first value is less than the second.
	 * Returns true if left is less than right, false otherwise.
	 *
	 * @param <T>   The type of the objects being compared
	 * @param left  The first object
	 * @param right The second object
	 * @return true if left &lt; right, false otherwise
	 * @usage lessThan(left, right)
	 */
	@NaftahFn(  name = "هل_أصغر_من",
				description = """
								دالة (هل_أصغر_من) تُستخدم لمقارنة عددين والتحقق مما إذا كان العدد الأول أصغر من العدد الثاني. تُرجع صحيح إذا كان الأول أصغر، وخطأ خلاف ذلك.""",
				usage = "هل_أصغر_من(ش ، ي)",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> boolean lessThan(T left, T right) {
		return allMatch(applyOperation(left, right, BinaryOperation.LESS_THAN), Boolean.TRUE::equals);
	}

	/**
	 * The (lessThanEquals) function checks if the first value is less than or equal to the second.
	 * Returns true if left ≤ right, false otherwise.
	 *
	 * @param <T>   The type of the objects being compared
	 * @param left  The first object
	 * @param right The second object
	 * @return true if left ≤ right, false otherwise
	 * @usage lessThanEquals(left, right)
	 */
	@NaftahFn(  name = "هل_أصغر_أو_يساوي",
				description = """
								دالة (هل_أصغر_أو_يساوي) تُستخدم لمقارنة عددين والتحقق مما إذا كان العدد الأول أصغر أو يساوي العدد الثاني. تُرجع صحيح إذا كان الأول أصغر أو يساوي، وخطأ خلاف ذلك.""",
				usage = "هل_أصغر_أو_يساوي(ش ، ي)",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> boolean lessThanEquals(T left, T right) {
		return allMatch(applyOperation(left, right, BinaryOperation.LESS_THAN_EQUALS), Boolean.TRUE::equals);
	}

	/**
	 * The (greaterThan) function checks if the first value is greater than the second.
	 * Returns true if left > right, false otherwise.
	 *
	 * @param <T>   The type of the objects being compared
	 * @param left  The first object
	 * @param right The second object
	 * @return true if left > right, false otherwise
	 * @usage greaterThan(left, right)
	 */
	@NaftahFn(  name = "هل_أكبر_من",
				description = """
								دالة (هل_أكبر_من) تُستخدم لمقارنة عددين والتحقق مما إذا كان العدد الأول أكبر من العدد الثاني. تُرجع صحيح إذا كان الأول أكبر، وخطأ خلاف ذلك.""",
				usage = "هل_أكبر_من(ش ، ي)",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> boolean greaterThan(T left, T right) {
		return allMatch(applyOperation(left, right, BinaryOperation.GREATER_THAN), Boolean.TRUE::equals);
	}

	/**
	 * The (greaterThanEquals) function checks if the first value is greater than or equal to the second.
	 * Returns true if left ≥ right, false otherwise.
	 *
	 * @param <T>   The type of the objects being compared
	 * @param left  The first object
	 * @param right The second object
	 * @return true if left ≥ right, false otherwise
	 * @usage greaterThanEquals(left, right)
	 */
	@NaftahFn(  name = "هل_أكبر_أو_يساوي",
				description = """
								دالة (هل_أكبر_أو_يساوي) تُستخدم لمقارنة عددين والتحقق مما إذا كان العدد الأول أكبر أو يساوي العدد الثاني. تُرجع صحيح إذا كان الأول أكبر أو يساوي، وخطأ خلاف ذلك.""",
				usage = "هل_أكبر_أو_يساوي(ش ، ي)",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> boolean greaterThanEquals(T left, T right) {
		return allMatch(applyOperation(left, right, BinaryOperation.GREATER_THAN_EQUALS), Boolean.TRUE::equals);
	}

	/**
	 * The (and) function performs a bitwise AND operation on the given numbers.
	 * Returns the result of the bitwise AND between the two numbers.
	 *
	 * @param <T>   The type of the objects being operated on
	 * @param left  The first number
	 * @param right The second number
	 * @return The result of bitwise AND operation
	 * @usage and(left, right)
	 */
	@NaftahFn(  name = "و_بتي",
				description = """
								دالة (و_بتي) تُنفذ عملية 'AND' على الأعداد الثنائية (bitwise) المعطاة. تُرجع العدد الناتج عن العملية الثنائية بين العددين.""",
				usage = "و_بتي(ش ، ي)",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object and(T left, T right) {
		return reduce(applyOperation(left, right, BinaryOperation.BITWISE_AND), BinaryOperation.ADD::apply);
	}

	/**
	 * The (or) function performs a bitwise OR operation on the given numbers.
	 * Returns the result of the bitwise OR between the two numbers.
	 *
	 * @param <T>   The type of the objects being operated on
	 * @param left  The first number
	 * @param right The second number
	 * @return The result of bitwise OR operation
	 * @usage or(left, right)
	 */
	@NaftahFn(  name = "أو_بتي",
				description = """
								دالة (أو_بتي) تُنفذ عملية 'OR' الثنائية على الأعداد المعطاة. تُرجع العدد الناتج عن تطبيق العملية الثنائية بين العددين.""",
				usage = "أو_بتي(ش ، ي)",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object or(T left, T right) {
		return reduce(applyOperation(left, right, BinaryOperation.BITWISE_OR), BinaryOperation.ADD::apply);
	}

	/**
	 * The (xor) function performs a bitwise XOR operation on the given numbers.
	 * Returns the result of the bitwise exclusive OR between the two numbers.
	 *
	 * @param <T>   The type of the objects being operated on
	 * @param left  The first number
	 * @param right The second number
	 * @return The result of bitwise XOR operation
	 * @usage xor(left, right)
	 */
	@NaftahFn(  name = "حصري_أو_بتي",
				description = """
								دالة (حصري_أو_بتي) تُنفذ عملية 'XOR' الثنائية على الأعداد المعطاة. تُرجع العدد الناتج عن تطبيق العملية الثنائية الحصرية بين العددين.""",
				usage = "حصري_أو_بتي(ش ، ي)",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object xor(T left, T right) {
		return reduce(applyOperation(left, right, BinaryOperation.BITWISE_XOR), BinaryOperation.ADD::apply);
	}

	/**
	 * Performs element-wise addition of two values.
	 * <p>
	 * Applies addition to each element individually for numbers, strings, simple values,
	 * arrays, and compatible collections.
	 *
	 * @param <T>   the type of operands
	 * @param left  the first value
	 * @param right the second value
	 * @return the element-wise sum of left and right
	 */
	@NaftahFn(  name = "جمع_عنصر_ب_عنصر",
				description = """
								يُطبق الجمع عنصرًا عن عنصر على القيم المعطاة، سواء كانت أرقامًا، نصوصًا، أو قيم بسيطة، بما في ذلك المصفوفات والمجموعات المتوافقة.
								تُطبق الدالة عملية الجمع على كل عنصر مقابل حيثما أمكن.""",
				usage = "جمع_عنصر_ب_عنصر(س ، ص)",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object elementWiseAdd(T left, T right) {
		return reduce(applyOperation(left, right, BinaryOperation.ELEMENTWISE_ADD), BinaryOperation.ADD::apply);
	}

	/**
	 * Performs element-wise subtraction of two values.
	 *
	 * @param <T>   the type of operands
	 * @param left  the first value
	 * @param right the second value
	 * @return the element-wise difference of left and right
	 */
	@NaftahFn(  name = "طرح_عنصر_ب_عنصر",
				description = """
								يُطبق الطرح عنصرًا عن عنصر على القيم المعطاة، سواء كانت أرقامًا أو قيمًا بسيطة، بما في ذلك المصفوفات والمجموعات المتوافقة.
								تُطبق الدالة عملية الطرح على كل عنصر مقابل حيثما أمكن.""",
				usage = "طرح_عنصر_ب_عنصر(س ، ص)",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object elementWiseSubtract(T left, T right) {
		return reduce(applyOperation(left, right, BinaryOperation.ELEMENTWISE_SUBTRACT), BinaryOperation.ADD::apply);
	}

	/**
	 * Performs element-wise multiplication of two values.
	 *
	 * @param <T>   the type of operands
	 * @param left  the first value
	 * @param right the second value
	 * @return the element-wise product of left and right
	 */
	@NaftahFn(  name = "ضرب_عنصر_ب_عنصر",
				description = """
								يُطبق الضرب عنصرًا عن عنصر على القيم المعطاة، سواء كانت أرقامًا أو قيمًا بسيطة، بما في ذلك المصفوفات والمجموعات المتوافقة.
								تُطبق الدالة عملية الضرب على كل عنصر مقابل حيثما أمكن.""",
				usage = "ضرب_عنصر_ب_عنصر(س ، ص)",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object elementWiseMultiply(T left, T right) {
		return reduce(applyOperation(left, right, BinaryOperation.ELEMENTWISE_MULTIPLY), BinaryOperation.ADD::apply);
	}

	/**
	 * Performs element-wise division of two values.
	 *
	 * @param <T>   the type of operands
	 * @param left  the first value
	 * @param right the second value
	 * @return the element-wise quotient of left and right
	 */
	@NaftahFn(  name = "قسمة_عنصر_ب_عنصر",
				description = """
								يُطبق القسمة عنصرًا عن عنصر على القيم المعطاة، سواء كانت أرقامًا أو قيمًا بسيطة، بما في ذلك المصفوفات والمجموعات المتوافقة.
								تُطبق الدالة عملية القسمة على كل عنصر مقابل حيثما أمكن.""",
				usage = "قسمة_عنصر_ب_عنصر(س ، ص)",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object elementWiseDivide(T left, T right) {
		return reduce(applyOperation(left, right, BinaryOperation.ELEMENTWISE_DIVIDE), BinaryOperation.ADD::apply);
	}

	/**
	 * Performs element-wise modulo of two values.
	 *
	 * @param <T>   the type of operands
	 * @param left  the first value
	 * @param right the second value
	 * @return the element-wise remainder of left divided by right
	 */
	@NaftahFn(  name = "باقي_القسمة_عنصر_ب_عنصر",
				description = """
								يُطبق عملية باقي القسمة عنصرًا عن عنصر على القيم المعطاة، سواء كانت أرقامًا أو قيمًا بسيطة، بما في ذلك المصفوفات والمجموعات المتوافقة.
								تُطبق الدالة عملية الباقي على كل عنصر مقابل حيثما أمكن.""",
				usage = "باقي_القسمة_عنصر_ب_عنصر(س ، ص)",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object elementWiseModulo(T left, T right) {
		return reduce(applyOperation(left, right, BinaryOperation.ELEMENTWISE_MODULO), BinaryOperation.ADD::apply);
	}

	/**
	 * The (not) function performs a bitwise NOT operation on the given number.
	 * Returns the result of flipping all bits in the number.
	 *
	 * @param <T> The type of the object being operated on
	 * @param x   The number to negate
	 * @return The bitwise NOT of the number
	 * @usage not(x)
	 */
	@NaftahFn(  name = "ليس_بتي",
				description = """
								دالة (ليس_بتي) تُنفذ عملية النفي الثنائي (bitwise NOT) على العدد المعطى. تُرجع العدد الناتج عن عكس كل بت في العدد.""",
				usage = "ليس_بتي(ش)",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object not(T x) {
		return reduce(applyOperation(x, UnaryOperation.BITWISE_NOT), BinaryOperation.ADD::apply);
	}

	/**
	 * The (shiftLeft) function performs a bitwise left shift on the given number by a specified number of positions.
	 * Used in binary calculations to increase the value.
	 *
	 * @param <T>       The type of the number
	 * @param x         The number to shift
	 * @param positions The number of positions to shift left
	 * @return The shifted number
	 * @usage shiftLeft(x, positions)
	 */
	@NaftahFn(  name = "إزاحة_إلى_اليسار",
				description = """
								دالة (إزاحة_إلى_اليسار) تُنفذ عملية إزاحة البتات للعدد المعطى إلى اليسار بعدد المواقع المحدد. تُستخدم هذه العملية في الحسابات الثنائية لتعظيم القيمة.""",
				usage = "إزاحة_إلى_اليسار(ش ، مواقِع)",
				parameterTypes = {Number.class, Number.class},
				returnType = Number.class)
	public static <T extends Number> Number shiftLeft(T x, T positions) {
		return NumberUtils.shiftLeft(x, positions);
	}

	/**
	 * The (shiftRight) function performs a bitwise right shift on the given number by a specified number of positions.
	 * Used in binary calculations to decrease the value.
	 *
	 * @param <T>       The type of the number
	 * @param x         The number to shift
	 * @param positions The number of positions to shift right
	 * @return The shifted number
	 * @usage shiftRight(x, positions)
	 */
	@NaftahFn(  name = "إزاحة_إلى_اليمين",
				description = """
								دالة (إزاحة_إلى_اليمين) تُنفذ عملية إزاحة البتات للعدد المعطى إلى اليمين بعدد المواقع المحدد. تُستخدم هذه العملية في الحسابات الثنائية لتقليل القيمة.""",
				usage = "إزاحة_إلى_اليمين(ش ، مواقِع)",
				parameterTypes = {Number.class, Number.class},
				returnType = Number.class)
	public static <T extends Number> Number shiftRight(T x, T positions) {
		return NumberUtils.shiftRight(x, positions);
	}

	/**
	 * The (unsignedShiftRight) function performs an unsigned bitwise right shift on the given number by a specified
	 * number of positions.
	 * The sign bit is ignored. Used to handle numbers without sign extension.
	 *
	 * @param <T>       The type of the number
	 * @param x         The number to shift
	 * @param positions The number of positions to shift right
	 * @return The shifted number
	 * @usage unsignedShiftRight(x, positions)
	 */
	@NaftahFn(  name = "إزاحة_إلى_اليمين_غير_موقعة",
				description = """
								دالة (إزاحة_إلى_اليمين_غير_موقعة) تُنفذ عملية إزاحة البتات للعدد المعطى إلى اليمين بدون اعتبار الإشارة، بعدد المواقع المحدد. تُستخدم هذه العملية لمعالجة الأعداد بدون تأثير الإشارة السالبة.""",
				usage = "إزاحة_إلى_اليمين_غير_موقعة(ش ، مواقِع)",
				parameterTypes = {Number.class, Number.class},
				returnType = Number.class)
	public static <T extends Number> Number unsignedShiftRight(T x, T positions) {
		return NumberUtils.unsignedShiftRight(x, positions);
	}

	/**
	 * The (preIncrement) function increases the given number by one before using it in the expression.
	 *
	 * @param <T> The type of the object to increment
	 * @param x   The object to increment
	 * @return The incremented value before use
	 * @usage preIncrement(x)
	 */
	@NaftahFn(  name = "زيادة_قبلية",
				description = "دالة (زيادة_قبلية) تُزيد العدد المعطى بمقدار واحد قبل استخدامه في التعبير.",
				usage = "زيادة_قبلية(ش)",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object preIncrement(T x) {
		return reduce(applyOperation(x, UnaryOperation.PRE_INCREMENT), BinaryOperation.ADD::apply);
	}

	/**
	 * The (postIncrement) function increases the given number by one after using it in the expression.
	 *
	 * @param <T> The type of the object to increment
	 * @param x   The object to increment
	 * @return The original value before incrementing
	 * @usage postIncrement(x)
	 */
	@NaftahFn(  name = "زيادة_بعدية",
				description = "دالة (زيادة_بعدية) تُزيد العدد المعطى بمقدار واحد بعد استخدامه في التعبير.",
				usage = "زيادة_بعدية(ش)",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object postIncrement(T x) {
		return reduce(applyOperation(x, UnaryOperation.POST_INCREMENT), BinaryOperation.ADD::apply);
	}

	/**
	 * The (preDecrement) function decreases the given number by one before using it in the expression.
	 *
	 * @param <T> The type of the object to decrement
	 * @param x   The object to decrement
	 * @return The decremented value before use
	 * @usage preDecrement(x)
	 */
	@NaftahFn(  name = "نقصان_قبلي",
				description = "دالة (نقصان_قبلي) تُنقص العدد المعطى بمقدار واحد قبل استخدامه في التعبير.",
				usage = "نقصان_قبلي(ش)",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object preDecrement(T x) {
		return reduce(applyOperation(x, UnaryOperation.PRE_DECREMENT), BinaryOperation.ADD::apply);
	}

	/**
	 * The (postDecrement) function decreases the given number by one after using it in the expression.
	 *
	 * @param <T> The type of the object to decrement
	 * @param x   The object to decrement
	 * @return The original value before decrementing
	 * @usage postDecrement(x)
	 */
	@NaftahFn(  name = "نقصان_بعدي",
				description = "دالة (نقصان_بعدي) تُنقص العدد المعطى بمقدار واحد بعد استخدامه في التعبير.",
				usage = "نقصان_بعدي(ش)",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object postDecrement(T x) {
		return reduce(applyOperation(x, UnaryOperation.POST_DECREMENT), BinaryOperation.ADD::apply);
	}

	/**
	 * The (negate) function returns the arithmetic negation of the given value.
	 * <p>
	 * It inverts the numeric sign of the value. For example:
	 * <pre>{@code
	 * negate(5)  → -5
	 * negate(-2) → 2
	 * }</pre>
	 *
	 * @param <T> The type of the object to negate
	 * @param x   The object whose sign should be inverted
	 * @return The negated value
	 * @usage negate(x)
	 */
	@NaftahFn(  name = "عكس_الإشارة",
				description = "الدالة (عكس_الإشارة) تُعيد معكوس إشارة القيمة المُعطاة.",
				usage = "عكس_الإشارة(س)",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object negate(T x) {
		return reduce(applyOperation(x, UnaryOperation.MINUS), BinaryOperation.ADD::apply);
	}

	/**
	 * The (logicalNot) function returns the logical negation of the given value.
	 * <p>
	 * It inverts a boolean value. For example:
	 * <pre>{@code
	 * logicalNot(true)  → false
	 * logicalNot(false) → true
	 * }</pre>
	 *
	 * @param <T> The type of the object to logically negate
	 * @param x   The object to negate
	 * @return The logical negation of the given value
	 * @usage logicalNot(x)
	 */
	@NaftahFn(  name = "نفي_منطقي",
				description = "الدالة (نفي_منطقي) تُعيد القيمة المنطقية المعاكسة للقيمة المُعطاة.",
				usage = "نفي_منطقي(س)",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object logicalNot(T x) {
		return reduce(applyOperation(x, UnaryOperation.NOT), BinaryOperation.ADD::apply);
	}

	@NaftahFn(
				name = "تحليل_رقم_بنظام_العد",
				description = """
								دالة (تحليل_رقم_بنظام_العد) لتحليل رقم نصي وفقًا للنظام العددي المحدد وإرجاع النوع المناسب من الأعداد.""",
				usage = "تحليل_رقم_بنظام_العد(الرقم، قاعدة)",
				parameterTypes = {String.class, Number.class},
				returnType = Number.class
	)
	public static Number parseDynamicNumber(String text, DynamicNumber radix) {
		String value = ArabicUtils
				.convertArabicToLatinLetterByLetter(text);
		return NumberUtils.parseDynamicNumber(value, radix, text);
	}

	@NaftahFn(
				name = "حجم",
				description = """
								دالة (حجم) لحساب حجم الكائن.
								- للمصفوفات: طول المصفوفة
								- للقوائم والمجموعات: عدد العناصر
								- للخرائط: عدد المدخلات
								- للسلاسل: طول النص
								- للأنواع العددية والمنطقية: 1
								- للكائنات الأخرى: عدد الحقول غير الثابتة
								""",
				usage = "حجم(الكائن)",
				parameterTypes = {Object.class},
				returnType = Number.class
	)
	public static Number size(Object obj) {
		return ObjectUtils.size(obj);
	}

}
