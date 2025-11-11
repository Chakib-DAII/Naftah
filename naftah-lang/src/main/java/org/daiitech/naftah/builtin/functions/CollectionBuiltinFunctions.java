package org.daiitech.naftah.builtin.functions;

import java.util.Collection;

import org.daiitech.naftah.builtin.NaftahFn;
import org.daiitech.naftah.builtin.NaftahFnProvider;
import org.daiitech.naftah.builtin.utils.CollectionUtils;
import org.daiitech.naftah.builtin.utils.op.BinaryOperation;
import org.daiitech.naftah.builtin.utils.op.UnaryOperation;
import org.daiitech.naftah.errors.NaftahBugError;

import static org.daiitech.naftah.builtin.utils.ObjectUtils.applyOperation;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;

/**
 * Provides built-in functions used within the Naftah language for performing various
 * arithmetic and logical operations with precision and efficiency.
 *
 * <p>This class contains static methods that implement fundamental operations such as
 * addition, subtraction, multiplication, division, logical comparisons, bitwise operations,
 * and element-wise collection operations. All functions are annotated to be recognized by the Naftah interpreter.
 *
 * <p><b>Note:</b> This class cannot be instantiated.
 *
 * @author Chakib Daii
 */
@NaftahFnProvider(
					name = "دوال الحزم",
					useQualifiedName = true,
					useQualifiedAliases = true,
					description = """
									يحتوي هذا الموفر على دوال لمعالجة المصفوفات، القوائم، والمجموعات (Collections).
									تشمل العمليات الثنائية والفردية، الوصول للعناصر، واستبدالها.
									""",
					functionNames = {
										"إجمع",
										"و_منطقي",
										"أو_منطقي",
										"إطرح",
										"إضرب",
										"أس",
										"إقسم",
										"باقي_القسمة",
										"هل_يساوي",
										"هل_لا_يساوي",
										"هل_أصغر_من",
										"هل_أصغر_أو_يساوي",
										"هل_أكبر_من",
										"هل_أكبر_أو_يساوي",
										"و_بتي",
										"أو_بتي",
										"حصري_أو_بتي",
										"جمع_عنصر_ب_عنصر",
										"طرح_عنصر_ب_عنصر"
					}
)
public final class CollectionBuiltinFunctions {

	/**
	 * Private constructor to prevent instantiation.
	 * Throws {@link NaftahBugError} if called.
	 */
	private CollectionBuiltinFunctions() {
		throw newNaftahBugInvalidUsageError();
	}

	/**
	 * Adds two values element by element of collection, map with themselves or with simple value.
	 *
	 * @param left  first operand
	 * @param right second operand
	 * @param <T>   type of operands
	 * @return collection element-wise sum
	 */
	@NaftahFn(
				name = "إجمع",
				aliases = {"إجمع"},
				description = "تطبيق عملية جمع عنصر بعنصر على حزمتين بنفس الحجم.",
				usage = """
						دوال:الحزم::إجمع({أ:1 , ب:2} , {أ:1 , ب:2})
						دوال:الحزم::إجمع([1 , 2], [3 , 4])
						دوال:الحزم::إجمع({1 , 2}, {3 , 5})
						دوال:الحزم::إجمع((1 , 2), (1 , 2))
						دوال:الحزم::إجمع([1 ,2 , 3], 10)
						دوال:الحزم::إجمع(10 , [1 ,2 , 3])
						""",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object add(T left, T right) {
		checkParams(left, right);
		return applyOperation(left, right, BinaryOperation.ADD);
	}

	/**
	 * Performs logical AND element by element of collection, map with themselves or with simple value.
	 *
	 * @param left  first operand
	 * @param right second operand
	 * @param <T>   type of operands
	 * @return collection logical AND results
	 */
	@NaftahFn(  name = "و_منطقي",
				description = """
								إجراء عملية "و" المنطقية بين قيمتين. تُعيد الدالة القيمة الصحيحة فقط إذا كانت كلتا القيمتين صحيحتين، وتُستخدم في البرمجة والمنطق للتحقق من تحقق شرطين معًا.""",
				usage = """
						دوال:الحزم::و_منطقي({أ:1 , ب:2} , {أ:1 , ب:2})
						دوال:الحزم::و_منطقي([1 , 2], [3 , 4])
						دوال:الحزم::و_منطقي({1 , 2}, {3 , 5})
						دوال:الحزم::و_منطقي((1 , 2), (1 , 2))
						دوال:الحزم::و_منطقي([1 ,2 , 3], 10)
						دوال:الحزم::و_منطقي(10 , [1 ,2 , 3])
						""",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object logicalAnd(T left, T right) {
		checkParams(left, right);
		return applyOperation(left, right, BinaryOperation.AND);
	}

	/**
	 * Performs logical OR element by element of collection, map with themselves or with simple value.
	 *
	 * @param left  first operand
	 * @param right second operand
	 * @param <T>   type of operands
	 * @return collection of logical OR results
	 */
	@NaftahFn(  name = "أو_منطقي",
				description = """
								إجراء عملية "أو" المنطقية بين قيمتين. تُعيد الدالة القيمة الصحيحة إذا كانت إحدى القيمتين أو كلتاهما صحيحتين، وتُستخدم في البرمجة والمنطق للتحقق من تحقق أحد الشرطين على الأقل.""",
				usage = """
						دوال:الحزم::أو_منطقي({أ:1 , ب:2} , {أ:1 , ب:2})
						دوال:الحزم::أو_منطقي([1 , 2], [3 , 4])
						دوال:الحزم::أو_منطقي({1 , 2}, {3 , 5})
						دوال:الحزم::أو_منطقي((1 , 2), (1 , 2))
						دوال:الحزم::أو_منطقي([1 ,2 , 3], 10)
						دوال:الحزم::أو_منطقي(10 , [1 ,2 , 3])
						""",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object logicalOr(T left, T right) {
		checkParams(left, right);
		return applyOperation(left, right, BinaryOperation.OR);
	}

	/**
	 * Subtracts the second value from the first element by element of collection, map with themselves or with simple
	 * value.
	 *
	 * @param left  first operand
	 * @param right second operand
	 * @param <T>   type of operands
	 * @return subtraction result
	 */
	@NaftahFn(  name = "إطرح",
				description = """
								طرح الأعداد للحصول على الفرق. هو العملية الأساسية التي تُستخدم في الرياضيات لتحديد القيمة المتبقية عند إزالة قيمة عدد من عدد آخر.""",
				usage = """
						دوال:الحزم::إطرح({أ:1 , ب:2} , {أ:1 , ب:2})
						دوال:الحزم::إطرح([1 , 2], [3 , 4])
						دوال:الحزم::إطرح({1 , 2}, {3 , 5})
						دوال:الحزم::إطرح((1 , 2), (1 , 2))
						دوال:الحزم::إطرح([1 ,2 , 3], 10)
						دوال:الحزم::إطرح(10 , [1 ,2 , 3])
						""",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object subtract(T left, T right) {
		checkParams(left, right);
		return applyOperation(left, right, BinaryOperation.SUBTRACT);
	}

	/**
	 * Multiplies two values element by element of collection, map with themselves or with simple value.
	 *
	 * @param left  first operand
	 * @param right second operand
	 * @param <T>   type of operands
	 * @return multiplication result
	 */
	@NaftahFn(  name = "إضرب",
				description = """
								ضرب الأعداد للحصول على الناتج هو العملية الأساسية التي تُستخدم في الرياضيات لتحديد القيمة الإجمالية عند تكرار جمع عدد معين عدة مرات.""",
				usage = """
						دوال:الحزم::إضرب({أ:1 , ب:2} , {أ:1 , ب:2})
						دوال:الحزم::إضرب([1 , 2], [3 , 4])
						دوال:الحزم::إضرب({1 , 2}, {3 , 5})
						دوال:الحزم::إضرب((1 , 2), (1 , 2))
						دوال:الحزم::إضرب([1 ,2 , 3], 10)
						دوال:الحزم::إضرب(10 , [1 ,2 , 3])
						""",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object multiply(T left, T right) {
		checkParams(left, right);
		return applyOperation(left, right, BinaryOperation.MULTIPLY);
	}

	/**
	 * Raises the first value to the power of the second value element by element of collection, map with themselves
	 * or with simple value.
	 *
	 * @param left  base
	 * @param right exponent
	 * @param <T>   type of operands
	 * @return result of exponentiation
	 */
	@NaftahFn(  name = "أس",
				description = """
								رفع قيمة إلى قوة معينة. تُعيد الدالة النتيجة الناتجة عن رفع القيمة الأولى (الأساس) إلى القيمة الثانية (الأس)، وتُستخدم في الرياضيات والبرمجة لإجراء العمليات الأسية.""",
				usage = """
						دوال:الحزم::أس({أ:1 , ب:2} , {أ:1 , ب:2})
						دوال:الحزم::أس([1 , 2], [3 , 4])
						دوال:الحزم::أس({1 , 2}, {3 , 5})
						دوال:الحزم::أس((1 , 2), (1 , 2))
						دوال:الحزم::أس([1 ,2 , 3], 10)
						دوال:الحزم::أس(10 , [1 ,2 , 3])
						""",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object pow(T left, T right) {
		checkParams(left, right);
		return applyOperation(left, right, BinaryOperation.POWER);
	}

	/**
	 * Divides the first value by the second value element by element of collection, map with themselves or with
	 * simple value.
	 *
	 * @param left  numerator
	 * @param right denominator
	 * @param <T>   type of operands
	 * @return division result
	 */
	@NaftahFn(  name = "إقسم",
				description = """
								قسمة الأعداد للحصول على خارج القسمة هي العملية الأساسية التي تُستخدم في الرياضيات لتحديد كم مرة يمكن تقسيم عدد إلى أجزاء متساوية.""",
				usage = """
						دوال:الحزم::إقسم({أ:1 , ب:2} , {أ:1 , ب:2})
						دوال:الحزم::إقسم([1 , 2], [3 , 4])
						دوال:الحزم::إقسم({1 , 2}, {3 , 5})
						دوال:الحزم::إقسم((1 , 2), (1 , 2))
						دوال:الحزم::إقسم([1 ,2 , 3], 10)
						دوال:الحزم::إقسم(10 , [1 ,2 , 3])
						""",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object divide(T left, T right) {
		checkParams(left, right);
		return applyOperation(left, right, BinaryOperation.DIVIDE);
	}

	/**
	 * Computes the modulo (remainder) of the first value divided by the second element by element of collection, map
	 * with themselves or with simple value.
	 *
	 * @param left  dividend
	 * @param right divisor
	 * @param <T>   type of operands
	 * @return remainder after division
	 */
	@NaftahFn(  name = "باقي_القسمة",
				description = """
								عملية باقي القسمة (المودولو) هي العملية التي تُستخدم في الرياضيات لتحديد الباقي المتبقي بعد قسمة عدد على عدد آخر.""",
				usage = """
						دوال:الحزم::باقي_القسمة({أ:1 , ب:2} , {أ:1 , ب:2})
						دوال:الحزم::باقي_القسمة([1 , 2], [3 , 4])
						دوال:الحزم::باقي_القسمة({1 , 2}, {3 , 5})
						دوال:الحزم::باقي_القسمة((1 , 2), (1 , 2))
						دوال:الحزم::باقي_القسمة([1 ,2 , 3], 10)
						دوال:الحزم::باقي_القسمة(10 , [1 ,2 , 3])
						""",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object modulo(T left, T right) {
		checkParams(left, right);
		return applyOperation(left, right, BinaryOperation.MODULO);
	}

	/**
	 * Checks if two values are equal element by element of collection, map with themselves or with simple value.
	 *
	 * @param left  first value
	 * @param right second value
	 * @param <T>   type of operands
	 * @return collection of booleans
	 */
	@NaftahFn(  name = "هل_يساوي",
				description = """
								دالة (هل_يساوي) تُستخدم لمقارنة عددين والتحقق مما إذا كانا متساويين في القيمة. تُرجع صحيح إذا كان العددان متساويين، وخطأ خلاف ذلك.""",
				usage = """
						دوال:الحزم::هل_يساوي({أ:1 , ب:2} , {أ:1 , ب:2})
						دوال:الحزم::هل_يساوي([1 , 2], [3 , 4])
						دوال:الحزم::هل_يساوي({1 , 2}, {3 , 5})
						دوال:الحزم::هل_يساوي((1 , 2), (1 , 2))
						دوال:الحزم::هل_يساوي([1 ,2 , 3], 10)
						دوال:الحزم::هل_يساوي(10 , [1 ,2 , 3])
						""",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object equals(T left, T right) {
		checkParams(left, right);
		return applyOperation(left, right, BinaryOperation.EQUALS);
	}

	/**
	 * Checks if two values are not equal element by element of collection, map with themselves or with simple value.
	 *
	 * @param left  first value
	 * @param right second value
	 * @param <T>   type of operands
	 * @return collection of booleans
	 */
	@NaftahFn(  name = "هل_لا_يساوي",
				description = """
								دالة (هل_لا_يساوي) تُستخدم لمقارنة عددين والتحقق مما إذا كانا غير متساويين في القيمة. تُرجع صحيح إذا كان العددان غير متساويين، وخطأ خلاف ذلك.""",
				usage = """
						دوال:الحزم::هل_لا_يساوي({أ:1 , ب:2} , {أ:1 , ب:2})
						دوال:الحزم::هل_لا_يساوي([1 , 2], [3 , 4])
						دوال:الحزم::هل_لا_يساوي({1 , 2}, {3 , 5})
						دوال:الحزم::هل_لا_يساوي((1 , 2), (1 , 2))
						دوال:الحزم::هل_لا_يساوي([1 ,2 , 3], 10)
						دوال:الحزم::هل_لا_يساوي(10 , [1 ,2 , 3])
						""",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object notEquals(T left, T right) {
		checkParams(left, right);
		return applyOperation(left, right, BinaryOperation.NOT_EQUALS);
	}

	/**
	 * Checks if the first value is less than the second element by element of collection, map with themselves or with
	 * simple value.
	 *
	 * @param left  first value
	 * @param right second value
	 * @param <T>   type of operands
	 * @return collection of booleans
	 */
	@NaftahFn(  name = "هل_أصغر_من",
				description = """
								دالة (هل_أصغر_من) تُستخدم لمقارنة عددين والتحقق مما إذا كان العدد الأول أصغر من العدد الثاني. تُرجع صحيح إذا كان الأول أصغر، وخطأ خلاف ذلك.""",
				usage = """
						دوال:الحزم::هل_أصغر_من({أ:1 , ب:2} , {أ:1 , ب:2})
						دوال:الحزم::هل_أصغر_من([1 , 2], [3 , 4])
						دوال:الحزم::هل_أصغر_من({1 , 2}, {3 , 5})
						دوال:الحزم::هل_أصغر_من((1 , 2), (1 , 2))
						دوال:الحزم::هل_أصغر_من([1 ,2 , 3], 10)
						دوال:الحزم::هل_أصغر_من(10 , [1 ,2 , 3])
						""",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object lessThan(T left, T right) {
		checkParams(left, right);
		return applyOperation(left, right, BinaryOperation.LESS_THAN);
	}

	/**
	 * Checks if the first value is less than or equal to the second element by element of collection, map with
	 * themselves or with simple value.
	 *
	 * @param left  first value
	 * @param right second value
	 * @param <T>   type of operands
	 * @return collection of booleans
	 */
	@NaftahFn(  name = "هل_أصغر_أو_يساوي",
				description = """
								دالة (هل_أصغر_أو_يساوي) تُستخدم لمقارنة عددين والتحقق مما إذا كان العدد الأول أصغر أو يساوي العدد الثاني. تُرجع صحيح إذا كان الأول أصغر أو يساوي، وخطأ خلاف ذلك.""",
				usage = """
						دوال:الحزم::هل_أصغر_أو_يساوي({أ:1 , ب:2} , {أ:1 , ب:2})
						دوال:الحزم::هل_أصغر_أو_يساوي([1 , 2], [3 , 4])
						دوال:الحزم::هل_أصغر_أو_يساوي({1 , 2}, {3 , 5})
						دوال:الحزم::هل_أصغر_أو_يساوي((1 , 2), (1 , 2))
						دوال:الحزم::هل_أصغر_أو_يساوي([1 ,2 , 3], 10)
						دوال:الحزم::هل_أصغر_أو_يساوي(10 , [1 ,2 , 3])
						""",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object lessThanEquals(T left, T right) {
		checkParams(left, right);
		return applyOperation(left, right, BinaryOperation.LESS_THAN_EQUALS);
	}

	/**
	 * Checks if the first value is greater than the second element by element of collection, map with themselves or
	 * with simple value.
	 *
	 * @param left  first value
	 * @param right second value
	 * @param <T>   type of operands
	 * @return collection of booleans
	 */
	@NaftahFn(  name = "هل_أكبر_من",
				description = """
								دالة (هل_أكبر_من) تُستخدم لمقارنة عددين والتحقق مما إذا كان العدد الأول أكبر من العدد الثاني. تُرجع صحيح إذا كان الأول أكبر، وخطأ خلاف ذلك.""",
				usage = """
						دوال:الحزم::هل_أكبر_من({أ:1 , ب:2} , {أ:1 , ب:2})
						دوال:الحزم::هل_أكبر_من([1 , 2], [3 , 4])
						دوال:الحزم::هل_أكبر_من({1 , 2}, {3 , 5})
						دوال:الحزم::هل_أكبر_من((1 , 2), (1 , 2))
						دوال:الحزم::هل_أكبر_من([1 ,2 , 3], 10)
						دوال:الحزم::هل_أكبر_من(10 , [1 ,2 , 3])
						""",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object greaterThan(T left, T right) {
		checkParams(left, right);
		return applyOperation(left, right, BinaryOperation.GREATER_THAN);
	}

	/**
	 * Checks if the first value is greater than or equal to the second element by element of collection, map with
	 * themselves or with simple value.
	 *
	 * @param left  first value
	 * @param right second value
	 * @param <T>   type of operands
	 * @return collection of booleans
	 */
	@NaftahFn(  name = "هل_أكبر_أو_يساوي",
				description = """
								دالة (هل_أكبر_أو_يساوي) تُستخدم لمقارنة عددين والتحقق مما إذا كان العدد الأول أكبر أو يساوي العدد الثاني. تُرجع صحيح إذا كان الأول أكبر أو يساوي، وخطأ خلاف ذلك.""",
				usage = """
						دوال:الحزم::هل_أكبر_أو_يساوي({أ:1 , ب:2} , {أ:1 , ب:2})
						دوال:الحزم::هل_أكبر_أو_يساوي([1 , 2], [3 , 4])
						دوال:الحزم::هل_أكبر_أو_يساوي({1 , 2}, {3 , 5})
						دوال:الحزم::هل_أكبر_أو_يساوي((1 , 2), (1 , 2))
						دوال:الحزم::هل_أكبر_أو_يساوي([1 ,2 , 3], 10)
						دوال:الحزم::هل_أكبر_أو_يساوي(10 , [1 ,2 , 3])
						""",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object greaterThanEquals(T left, T right) {
		checkParams(left, right);
		return applyOperation(left, right, BinaryOperation.GREATER_THAN_EQUALS);
	}

	/**
	 * Performs bitwise AND operation element by element of collection, map with themselves or with simple value.
	 *
	 * @param left  first value
	 * @param right second value
	 * @param <T>   type of operands
	 * @return result of bitwise AND
	 */
	@NaftahFn(  name = "و_بتي",
				description = """
								دالة (و_بتي) تُنفذ عملية 'AND' على الأعداد الثنائية (bitwise) المعطاة. تُرجع العدد الناتج عن العملية الثنائية بين العددين.""",
				usage = """
						دوال:الحزم::و_بتي({أ:1 , ب:2} , {أ:1 , ب:2})
						دوال:الحزم::و_بتي([1 , 2], [3 , 4])
						دوال:الحزم::و_بتي({1 , 2}, {3 , 5})
						دوال:الحزم::و_بتي((1 , 2), (1 , 2))
						دوال:الحزم::و_بتي([1 ,2 , 3], 10)
						دوال:الحزم::و_بتي(10 , [1 ,2 , 3])
						""",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object and(T left, T right) {
		checkParams(left, right);
		return applyOperation(left, right, BinaryOperation.BITWISE_AND);
	}

	/**
	 * Performs bitwise OR operation element by element of collection, map with themselves or with simple value.
	 *
	 * @param left  first value
	 * @param right second value
	 * @param <T>   type of operands
	 * @return result of bitwise OR
	 */
	@NaftahFn(  name = "أو_بتي",
				description = """
								دالة (أو_بتي) تُنفذ عملية 'OR' الثنائية على الأعداد المعطاة. تُرجع العدد الناتج عن تطبيق العملية الثنائية بين العددين.""",
				usage = """
						دوال:الحزم::أو_بتي({أ:1 , ب:2} , {أ:1 , ب:2})
						دوال:الحزم::أو_بتي([1 , 2], [3 , 4])
						دوال:الحزم::أو_بتي({1 , 2}, {3 , 5})
						دوال:الحزم::أو_بتي((1 , 2), (1 , 2))
						دوال:الحزم::أو_بتي([1 ,2 , 3], 10)
						دوال:الحزم::أو_بتي(10 , [1 ,2 , 3])
						""",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object or(T left, T right) {
		checkParams(left, right);
		return applyOperation(left, right, BinaryOperation.BITWISE_OR);
	}

	/**
	 * Performs bitwise XOR operation element by element of collection, map with themselves or with simple value.
	 *
	 * @param left  first value
	 * @param right second value
	 * @param <T>   type of operands
	 * @return result of bitwise XOR
	 */
	@NaftahFn(  name = "حصري_أو_بتي",
				description = """
								دالة (حصري_أو_بتي) تُنفذ عملية 'XOR' الثنائية على الأعداد المعطاة. تُرجع العدد الناتج عن تطبيق العملية الثنائية الحصرية بين العددين.""",
				usage = """
						دوال:الحزم::حصري_أو_بتي({أ:1 , ب:2} , {أ:1 , ب:2})
						دوال:الحزم::حصري_أو_بتي([1 , 2], [3 , 4])
						دوال:الحزم::حصري_أو_بتي({1 , 2}, {3 , 5})
						دوال:الحزم::حصري_أو_بتي((1 , 2), (1 , 2))
						دوال:الحزم::حصري_أو_بتي([1 ,2 , 3], 10)
						دوال:الحزم::حصري_أو_بتي(10 , [1 ,2 , 3])
						""",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object xor(T left, T right) {
		checkParams(left, right);
		return applyOperation(left, right, BinaryOperation.BITWISE_XOR);
	}

	/**
	 * Performs element-wise addition element by element of collection, map with themselves or with simple value.
	 *
	 * @param left  first collection or value
	 * @param right second collection or value
	 * @param <T>   type of operands
	 * @return element-wise addition result
	 */
	@NaftahFn(  name = "جمع_عنصر_ب_عنصر",
				description = """
								يُطبق الجمع عنصرًا عن عنصر على القيم المعطاة، سواء كانت أرقامًا، نصوصًا، أو قيم بسيطة، بما في ذلك المصفوفات والمجموعات المتوافقة.
								تُطبق الدالة عملية الجمع على كل عنصر مقابل حيثما أمكن.""",
				usage = """
						دوال:الحزم::جمع_عنصر_ب_عنصر({أ:1 , ب:2} , {أ:1 , ب:2})
						دوال:الحزم::جمع_عنصر_ب_عنصر([1 , 2], [3 , 4])
						دوال:الحزم::جمع_عنصر_ب_عنصر({1 , 2}, {3 , 5})
						دوال:الحزم::جمع_عنصر_ب_عنصر((1 , 2), (1 , 2))
						دوال:الحزم::جمع_عنصر_ب_عنصر([1 ,2 , 3], 10)
						دوال:الحزم::جمع_عنصر_ب_عنصر(10 , [1 ,2 , 3])
						""",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object elementWiseAdd(T left, T right) {
		checkParams(left, right);
		return applyOperation(left, right, BinaryOperation.ELEMENTWISE_ADD);
	}

	/**
	 * Performs element-wise subtraction element by element of collection, map with themselves or with simple value.
	 *
	 * @param left  first collection or value
	 * @param right second collection or value
	 * @param <T>   type of operands
	 * @return element-wise subtraction result
	 */
	@NaftahFn(  name = "طرح_عنصر_ب_عنصر",
				description = """
								يُطبق الطرح عنصرًا عن عنصر على القيم المعطاة، سواء كانت أرقامًا أو قيمًا بسيطة، بما في ذلك المصفوفات والمجموعات المتوافقة.
								تُطبق الدالة عملية الطرح على كل عنصر مقابل حيثما أمكن.""",
				usage = """
						دوال:الحزم::طرح_عنصر_ب_عنصر({أ:1 , ب:2} , {أ:1 , ب:2})
						دوال:الحزم::طرح_عنصر_ب_عنصر([1 , 2], [3 , 4])
						دوال:الحزم::طرح_عنصر_ب_عنصر({1 , 2}, {3 , 5})
						دوال:الحزم::طرح_عنصر_ب_عنصر((1 , 2), (1 , 2))
						دوال:الحزم::طرح_عنصر_ب_عنصر([1 ,2 , 3], 10)
						دوال:الحزم::طرح_عنصر_ب_عنصر(10 , [1 ,2 , 3])
						""",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object elementWiseSubtract(T left, T right) {
		checkParams(left, right);
		return applyOperation(left, right, BinaryOperation.ELEMENTWISE_SUBTRACT);
	}

	/**
	 * Performs element-wise multiplication element by element of collection, map with themselves or with simple value.
	 *
	 * @param left  first collection or value
	 * @param right second collection or value
	 * @param <T>   type of operands
	 * @return element-wise multiplication result
	 */
	@NaftahFn(  name = "ضرب_عنصر_ب_عنصر",
				description = """
								يُطبق الضرب عنصرًا عن عنصر على القيم المعطاة، سواء كانت أرقامًا أو قيمًا بسيطة، بما في ذلك المصفوفات والمجموعات المتوافقة.
								تُطبق الدالة عملية الضرب على كل عنصر مقابل حيثما أمكن.""",
				usage = """
						دوال:الحزم::ضرب_عنصر_ب_عنصر({أ:1 , ب:2} , {أ:1 , ب:2})
						دوال:الحزم::ضرب_عنصر_ب_عنصر([1 , 2], [3 , 4])
						دوال:الحزم::دوال:الحزم::ضرب_عنصر_ب_عنصر({1 , 2}, {3 , 5})
						دوال:الحزم::ضرب_عنصر_ب_عنصر((1 , 2), (1 , 2))
						دوال:الحزم::ضرب_عنصر_ب_عنصر([1 ,2 , 3], 10)
						دوال:الحزم::ضرب_عنصر_ب_عنصر(10 , [1 ,2 , 3])
						""",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object elementWiseMultiply(T left, T right) {
		checkParams(left, right);
		return applyOperation(left, right, BinaryOperation.ELEMENTWISE_MULTIPLY);
	}

	/**
	 * Performs element-wise division element by element of collection, map with themselves or with simple value.
	 *
	 * @param left  first collection or value
	 * @param right second collection or value
	 * @param <T>   type of operands
	 * @return element-wise division result
	 */
	@NaftahFn(  name = "قسمة_عنصر_ب_عنصر",
				description = """
								يُطبق القسمة عنصرًا عن عنصر على القيم المعطاة، سواء كانت أرقامًا أو قيمًا بسيطة، بما في ذلك المصفوفات والمجموعات المتوافقة.
								تُطبق الدالة عملية القسمة على كل عنصر مقابل حيثما أمكن.""",
				usage = """
						دوال:الحزم::قسمة_عنصر_ب_عنصر({أ:1 , ب:2} , {أ:1 , ب:2})
						دوال:الحزم::قسمة_عنصر_ب_عنصر([1 , 2], [3 , 4])
						دوال:الحزم::قسمة_عنصر_ب_عنصر({1 , 2}, {3 , 5})
						دوال:الحزم::قسمة_عنصر_ب_عنصر((1 , 2), (1 , 2))
						دوال:الحزم::قسمة_عنصر_ب_عنصر([1 ,2 , 3], 10)
						دوال:الحزم::قسمة_عنصر_ب_عنصر(10 , [1 ,2 , 3])
						""",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object elementWiseDivide(T left, T right) {
		checkParams(left, right);
		return applyOperation(left, right, BinaryOperation.ELEMENTWISE_DIVIDE);
	}

	/**
	 * Performs element-wise modulo element by element of collection, map with themselves or with simple value.
	 *
	 * @param left  first collection or value
	 * @param right second collection or value
	 * @param <T>   type of operands
	 * @return element-wise modulo result
	 */
	@NaftahFn(  name = "باقي_القسمة_عنصر_ب_عنصر",
				description = """
								يُطبق عملية باقي القسمة عنصرًا عن عنصر على القيم المعطاة، سواء كانت أرقامًا أو قيمًا بسيطة، بما في ذلك المصفوفات والمجموعات المتوافقة.
								تُطبق الدالة عملية الباقي على كل عنصر مقابل حيثما أمكن.""",
				usage = """
						دوال:الحزم::باقي_القسمة_عنصر_ب_عنصر({أ:1 , ب:2} , {أ:1 , ب:2})
						دوال:الحزم::باقي_القسمة_عنصر_ب_عنصر([1 , 2], [3 , 4])
						دوال:الحزم::باقي_القسمة_عنصر_ب_عنصر({1 , 2}, {3 , 5})
						دوال:الحزم::باقي_القسمة_عنصر_ب_عنصر((1 , 2), (1 , 2))
						دوال:الحزم::باقي_القسمة_عنصر_ب_عنصر([1 ,2 , 3], 10)
						دوال:الحزم::باقي_القسمة_عنصر_ب_عنصر(10 , [1 ,2 , 3])
						""",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object elementWiseModulo(T left, T right) {
		checkParams(left, right);
		return applyOperation(left, right, BinaryOperation.ELEMENTWISE_MODULO);
	}

	/**
	 * Performs bitwise NOT element by element of collection, map with themselves or with simple value.
	 *
	 * @param x   collection or value
	 * @param <T> type of operand
	 * @return result after applying bitwise NOT
	 */
	@NaftahFn(
				name = "ليس_بتي",
				description = "تطبيق عملية ليس_بتي على كل عنصر في حزمة.",
				usage = """
						دوال:الحزم::ليس_بتي({أ:1 , ب:2})
						دوال:الحزم::ليس_بتي([1 , 2 , 3])
						دوال:الحزم::ليس_بتي({1 , 2})
						دوال:الحزم::ليس_بتي((1 , 2))
						""",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object not(T x) {
		checkParam(x);
		return applyOperation(x, UnaryOperation.BITWISE_NOT);
	}

	/**
	 * Performs pre-increment element by element of collection, map with themselves or with simple value.
	 *
	 * @param x   collection or value
	 * @param <T> type of operand
	 * @return result after pre-increment
	 */
	@NaftahFn(  name = "زيادة_قبلية",
				description = "دالة (زيادة_قبلية) تُزيد العدد المعطى بمقدار واحد قبل استخدامه في التعبير.",
				usage = """
						دوال:الحزم::زيادة_قبلية({a:1 , b:2})
						دوال:الحزم::زيادة_قبلية([1 , 2 , 3])
						دوال:الحزم::زيادة_قبلية({1 , 2})
						دوال:الحزم::زيادة_قبلية((1 , 2))
						""",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object preIncrement(T x) {
		checkParam(x);
		return applyOperation(x, UnaryOperation.PRE_INCREMENT);
	}

	/**
	 * Performs post-increment element by element of collection, map with themselves or with simple value.
	 *
	 * @param x   collection or value
	 * @param <T> type of operand
	 * @return result after post-increment
	 */
	@NaftahFn(  name = "زيادة_بعدية",
				description = "دالة (زيادة_بعدية) تُزيد العدد المعطى بمقدار واحد بعد استخدامه في التعبير.",
				usage = """
						دوال:الحزم::زيادة_بعدية({a:1 , b:2})
						دوال:الحزم::زيادة_بعدية([1 , 2 , 3])
						دوال:الحزم::زيادة_بعدية({1 , 2})
						دوال:الحزم::زيادة_بعدية((1 , 2))
						""",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object postIncrement(T x) {
		checkParam(x);
		return applyOperation(x, UnaryOperation.POST_INCREMENT);
	}

	/**
	 * Performs pre-decrement element by element of collection, map with themselves or with simple value.
	 *
	 * @param x   collection or value
	 * @param <T> type of operand
	 * @return result after pre-decrement
	 */
	@NaftahFn(  name = "نقصان_قبلي",
				description = "دالة (نقصان_قبلي) تُنقص العدد المعطى بمقدار واحد قبل استخدامه في التعبير.",
				usage = """
						دوال:الحزم::نقصان_قبلي({a:1 , b:2})
						دوال:الحزم::نقصان_قبلي([1 , 2 , 3])
						دوال:الحزم::نقصان_قبلي({1 , 2})
						دوال:الحزم::نقصان_قبلي((1 , 2))
						""",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object preDecrement(T x) {
		checkParam(x);
		return applyOperation(x, UnaryOperation.PRE_DECREMENT);
	}

	/**
	 * Performs post-decrement element by element of collection, map with themselves or with simple value.
	 *
	 * @param x   collection or value
	 * @param <T> type of operand
	 * @return result after post-decrement
	 */
	@NaftahFn(  name = "نقصان_بعدي",
				description = "دالة (نقصان_بعدي) تُنقص العدد المعطى بمقدار واحد بعد استخدامه في التعبير.",
				usage = """
						دوال:الحزم::نقصان_بعدي({a:1 , b:2})
						دوال:الحزم::نقصان_بعدي([1 , 2 , 3])
						دوال:الحزم::نقصان_بعدي({1 , 2})
						دوال:الحزم::نقصان_بعدي((1 , 2))
						""",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object postDecrement(T x) {
		checkParam(x);
		return applyOperation(x, UnaryOperation.POST_DECREMENT);
	}

	/**
	 * Negates a collection or map.
	 *
	 * @param x   collection or value
	 * @param <T> type of operand
	 * @return result after negation
	 */
	@NaftahFn(  name = "عكس_الإشارة",
				description = "الدالة (عكس_الإشارة) تُعيد معكوس إشارة القيمة المُعطاة.",
				usage = """
						دوال:الحزم::عكس_الإشارة({a:1 , b:2})
						دوال:الحزم::عكس_الإشارة([1 , 2 , 3])
						دوال:الحزم::عكس_الإشارة({1 , 2})
						دوال:الحزم::عكس_الإشارة((1 , 2))
						""",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object negate(T x) {
		checkParam(x);
		return applyOperation(x, UnaryOperation.MINUS);
	}

	/**
	 * Performs logical NOT on a collection or map.
	 *
	 * @param x   collection or value
	 * @param <T> type of operand
	 * @return result after logical NOT
	 */
	@NaftahFn(  name = "نفي_منطقي",
				description = "الدالة (نفي_منطقي) تُعيد القيمة المنطقية المعاكسة للقيمة المُعطاة.",
				usage = """
						دوال:الحزم::نفي_منطقي({a:1 , b:2})
						دوال:الحزم::نفي_منطقي([1 , 2 , 3])
						دوال:الحزم::نفي_منطقي({1 , 2})
						دوال:الحزم::نفي_منطقي((1 , 2))
						""",
				parameterTypes = {Object.class, Object.class},
				returnType = Object.class)
	public static <T> Object logicalNot(T x) {
		checkParam(x);
		return applyOperation(x, UnaryOperation.NOT);
	}

	/**
	 * Gets an element from a collection by index.
	 *
	 * @param collection  the collection to get from
	 * @param targetIndex index of the element
	 * @return the element at the given index
	 */
	@NaftahFn(
				name = "أحصل_على_عنصر",
				aliases = {"الحصول_على_عنصر", "عنصر", "حصول_على_عنصر"},
				description = "الحصول على عنصر من مجموعة حسب الفهرس (index).",
				usage = """
						دوال:الحزم::حصول_على_عنصر([1 , 2 , 3], 1)
						دوال:الحزم::حصول_على_عنصر((1 , 2), 1)
						دوال:الحزم::حصول_على_عنصر({1 , 2}, 1)
						""",
				parameterTypes = {Collection.class, Number.class},
				returnType = Object.class
	)
	public static <I extends Number> Object getElementAt(Collection<?> collection, I targetIndex) {
		return CollectionUtils.getElementAt(collection, targetIndex.intValue());
	}

	/**
	 * Sets an element in a collection at a given index.
	 *
	 * @param collection  the collection to modify
	 * @param targetIndex index to set
	 * @param newValue    new value to assign
	 * @param <T>         type of element
	 * @param <I>         type of index
	 */
	@NaftahFn(
				name = "تعيين_عنصر",
				aliases = {"تعيين", "استبدال_عنصر"},
				description = "استبدال عنصر في مجموعة عند فهرس معين.",
				usage = """
						دوال:الحزم::تعيين_عنصر([1 , 2 , 3], 1 , 99)
						دوال:الحزم::تعيين_عنصر({1 , 2}, 1 , 99)
						""",
				parameterTypes = {Collection.class, Number.class, Object.class},
				returnType = void.class
	)
	public static <I extends Number, T> void setElementAt(Collection<T> collection, I targetIndex, T newValue) {
		CollectionUtils.setElementAt(collection, targetIndex.intValue(), newValue);
	}

	/**
	 * Checks if at least one of the two parameters is a collection, map, or array.
	 * Throws a NaftahBugError with an Arabic message if the check fails.
	 *
	 * @param left  first value to check
	 * @param right second value to check
	 * @param <T>   type of operands
	 * @throws NaftahBugError if neither left nor right is a collection, map, or array
	 */
	private static <T> void checkParams(T left, T right) {
		if (!(CollectionUtils.isCollectionMapOrArray(left) || CollectionUtils.isCollectionMapOrArray(right))) {
			throw newNaftahNotCollectionOrMapArgumentError(false);
		}
	}

	/**
	 * Checks if the parameter is a collection, map, or array.
	 * Throws a NaftahBugError with an Arabic message if the check fails.
	 *
	 * @param x   value to check
	 * @param <T> type of operand
	 * @throws NaftahBugError if x is not a collection, map, or array
	 */
	private static <T> void checkParam(T x) {
		if (!CollectionUtils.isCollectionMapOrArray(x)) {
			throw newNaftahNotCollectionOrMapArgumentError(true);
		}
	}

	/**
	 * Creates a {@link NaftahBugError} indicating that one or more arguments are not valid
	 * collection, array, or map types.
	 *
	 * <p>If {@code singleArgument} is {@code true}, the error message specifies that a single
	 * argument must be a collection, array, or associative array (map). Otherwise, the message
	 * specifies that at least one of the two arguments must be of those types.</p>
	 *
	 * <p><strong>Arabic message:</strong></p>
	 * <ul>
	 * <li>For a single argument: <code>"يجب أن يكون المعامل مصفوفة أو مجموعة أو مصفوفة ترابطية."</code></li>
	 * <li>For multiple arguments: <code>"يجب أن يكون على الأقل أحد المعاملين مصفوفة أو مجموعة أو مصفوفة ترابطية
	 * ."</code></li>
	 * </ul>
	 *
	 * @param singleArgument {@code true} if the error refers to a single argument; {@code false} if it refers to
	 *                       multiple arguments
	 * @return a {@link NaftahBugError} with the appropriate descriptive message
	 */
	public static NaftahBugError newNaftahNotCollectionOrMapArgumentError(boolean singleArgument) {
		return new NaftahBugError(singleArgument ?
				"يجب أن يكون المعامل مصفوفة أو مجموعة أو مصفوفة ترابطية." :
				"يجب أن يكون على الأقل أحد المعاملين مصفوفة أو مجموعة أو مصفوفة ترابطية.");
	}
}
