package org.daiitech.naftah.builtin;

import java.util.Objects;

import org.daiitech.naftah.builtin.utils.NumberUtils;
import org.daiitech.naftah.builtin.utils.op.BinaryOperation;
import org.daiitech.naftah.builtin.utils.op.UnaryOperation;
import org.daiitech.naftah.errors.NaftahBugError;

import static org.daiitech.naftah.builtin.utils.ObjectUtils.applyOperation;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.getNaftahValueToString;
import static org.daiitech.naftah.builtin.utils.op.BinaryOperation.ADD;
import static org.daiitech.naftah.parser.NaftahParserHelper.NULL;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.padText;

/**
 * @author Chakib Daii
 */
@NaftahFnProvider(name = "مزوّد دوال مدمجة", description = "يحتوي هذا الموفر على دوال مدمجة تُستخدم ضمن لغة نفطه لأداء عمليات حسابية ومنطقية مختلفة بدقة وكفاءة.", functionNames = {"جمع", "طرح", "ضرب", "قسمة", "باقي_القسمة", "إطبع"})
public final class Builtin {

	private Builtin() {
		throw new NaftahBugError("استخدام غير مسموح به.");
	}

	@NaftahFn(name = "إطبع", description = "تعليمة الطباعة (إطبع) هي التعليمة التي تُستخدم في البرمجة لإظهار نص معين على الشاشة، مثل إظهار رسالة ترحيبية للمستخدم.", usage = "إطبع(ش)", parameterTypes = {Object.class})
	public static void print(Object o) {
		if (Objects.nonNull(o)) {
			padText(getNaftahValueToString(o), true);
		}
		else
			System.out.println(padText(NULL, false));
	}

	@NaftahFn(name = "جمع", description = "إضافة الأعداد معًا للحصول على مجموع. هو العملية الأساسية التي تُستخدم في الرياضيات لتحديد القيمة الإجمالية من خلال جمع عدة أرقام.", usage = "جمع(ش ، ي)", parameterTypes = {Object.class, Object.class}, returnType = Object.class)
	public static <T> Object add(T x, T y) {
		return applyOperation(x, y, ADD);
	}

	@NaftahFn(name = "طرح", description = "طرح الأعداد للحصول على الفرق. هو العملية الأساسية التي تُستخدم في الرياضيات لتحديد القيمة المتبقية عند إزالة قيمة عدد من عدد آخر.", usage = "طرح(ش ، ي)", parameterTypes = {Object.class, Object.class}, returnType = Object.class)
	public static <T> Object subtract(T x, T y) {
		return applyOperation(x, y, BinaryOperation.SUBTRACT);
	}

	@NaftahFn(name = "ضرب", description = "ضرب الأعداد للحصول على الناتج هو العملية الأساسية التي تُستخدم في الرياضيات لتحديد القيمة الإجمالية عند تكرار جمع عدد معين عدة مرات.", usage = "ضرب(ش ، ي)", parameterTypes = {Object.class, Object.class}, returnType = Object.class)
	public static <T> Object multiply(T x, T y) {
		return applyOperation(x, y, BinaryOperation.MULTIPLY);
	}

	@NaftahFn(name = "قسمة", description = "قسمة الأعداد للحصول على خارج القسمة هي العملية الأساسية التي تُستخدم في الرياضيات لتحديد كم مرة يمكن تقسيم عدد إلى أجزاء متساوية.", usage = "قسمة(ش ، ي)", parameterTypes = {Object.class, Object.class}, returnType = Object.class)
	public static <T> Object divide(T x, T y) {
		return applyOperation(x, y, BinaryOperation.DIVIDE);
	}

	@NaftahFn(name = "باقي_القسمة", description = "عملية باقي القسمة (المودولو) هي العملية التي تُستخدم في الرياضيات لتحديد الباقي المتبقي بعد قسمة عدد على عدد آخر.", usage = "باقي_القسمة(ش ، ي)", parameterTypes = {Object.class, Object.class}, returnType = Object.class)
	public static <T> Object modulo(T x, T y) {
		return applyOperation(x, y, BinaryOperation.MODULO);
	}

	@NaftahFn(name = "الأكبر", description = "دالة (الأكبر) تُستخدم لمقارنة عددين وإرجاع العدد الأكبر بينهما. تُفيد هذه العملية في تحديد القيمة العليا عند المفاضلة بين قيمتين عدديتين.", usage = "الأكبر(ش ، ي)", parameterTypes = {Number.class, Number.class}, returnType = Number.class)
	public static <T extends Number> Number max(T x, T y) {
		return NumberUtils.max(x, y);
	}

	@NaftahFn(name = "الأصغر", description = "دالة (الأصغر) تُستخدم لمقارنة عددين وإرجاع العدد الأصغر بينهما. تُفيد هذه العملية في تحديد القيمة الدنيا عند المفاضلة بين قيمتين عدديتين.", usage = "الأصغر(ش ، ي)", parameterTypes = {Number.class, Number.class}, returnType = Number.class)
	public static <T extends Number> Number min(T x, T y) {
		return NumberUtils.min(x, y);
	}

	@NaftahFn(name = "رفع", description = "دالة (رفع) تُستخدم لرفع عدد (الأساس) إلى قوة عدد صحيح (الأس). تُفيد هذه العملية في الحسابات الرياضية التي تتطلب التكرار الأسي مثل حساب المربعات أو المكعبات.", usage = "رفع(الأساس ، الأس)", parameterTypes = {Number.class, int.class}, returnType = Number.class)
	public static <T extends Number> Number pow(T base, int exponent) {
		return NumberUtils.pow(base, exponent);
	}

	@NaftahFn(name = "تقريب", description = "دالة (تقريب) تُستخدم لتقريب عدد عشري إلى أقرب عدد صحيح. تُفيد هذه العملية في العمليات الحسابية التي تتطلب أعدادًا صحيحة دقيقة.", usage = "تقريب(ش)", parameterTypes = {Number.class}, returnType = Number.class)
	public static <T extends Number> Number round(T x) {
		return NumberUtils.round(x);
	}

	@NaftahFn(name = "أرضي", description = "دالة (أرضي) تُستخدم لإرجاع أكبر عدد صحيح أصغر من أو يساوي العدد المعطى. تُفيد هذه العملية في العمليات الحسابية التي تتطلب تقريب الأعداد إلى الأسفل.", usage = "أرضي(ش)", parameterTypes = {Number.class}, returnType = Number.class)
	public static <T extends Number> Number floor(T x) {
		return NumberUtils.floor(x);
	}

	@NaftahFn(name = "سقف", description = "دالة (سقف) تُستخدم لإرجاع أصغر عدد صحيح أكبر من أو يساوي العدد المعطى. تُفيد هذه العملية في العمليات الحسابية التي تتطلب تقريب الأعداد إلى الأعلى.", usage = "سقف(ش)", parameterTypes = {Number.class}, returnType = Number.class)
	public static <T extends Number> Number ceil(T x) {
		return NumberUtils.ceil(x);
	}

	@NaftahFn(name = "نفي", description = "دالة (نفي) تُستخدم لإرجاع العدد المعطى بعد تغييره إلى قيمته السالبة. تُفيد هذه العملية في العمليات الحسابية التي تتطلب عكس الإشارة العددية.", usage = "نفي(ش)", parameterTypes = {Number.class}, returnType = Number.class)
	public static <T extends Number> Number negate(T x) {
		return NumberUtils.negate(x);
	}

	@NaftahFn(name = "جذر", description = "دالة (جذر) تُستخدم لحساب الجذر التربيعي للعدد المعطى. تُفيد هذه العملية في العمليات الحسابية التي تتطلب إيجاد قيمة العدد الذي مربعه يساوي العدد الأصلي.", usage = "جذر(ش)", parameterTypes = {Number.class}, returnType = Number.class)
	public static <T extends Number> Number sqrt(T x) {
		return NumberUtils.sqrt(x);
	}

	@NaftahFn(name = "القيمة_المطلقة", description = "دالة (القيمة_المطلقة) تُستخدم لحساب القيمة المطلقة للعدد المعطى، أي إزالة إشارة السالب إن وجدت. تُفيد هذه العملية في الحسابات التي تتطلب قيمة موجبة دائمًا.", usage = "القيمة_المطلقة(ش)", parameterTypes = {Number.class}, returnType = Number.class)
	public static <T extends Number> Number abs(T x) {
		return NumberUtils.abs(x);
	}

	@NaftahFn(name = "إشارة", description = "دالة (إشارة) تُستخدم لتحديد إشارة العدد المعطى: ترجع -1 إذا كان العدد سالبًا، 0 إذا كان صفرًا، و1 إذا كان موجبًا.", usage = "إشارة(ش)", parameterTypes = {Number.class}, returnType = int.class)
	public static <T extends Number> int signum(T x) {
		return NumberUtils.signum(x);
	}

	@NaftahFn(name = "هل_صفر", description = "دالة (هل_صفر) تُستخدم للتحقق مما إذا كان العدد المعطى يساوي صفرًا. تُرجع true إذا كان العدد صفرًا، وfalse خلاف ذلك.", usage = "هل_صفر(ش)", parameterTypes = {Number.class}, returnType = boolean.class)
	public static <T extends Number> boolean isZero(T x) {
		return NumberUtils.isZero(x);
	}

	@NaftahFn(name = "يساوي", description = "دالة (يساوي) تُستخدم لمقارنة عددين والتحقق مما إذا كانا متساويين في القيمة. تُرجع true إذا كان العددان متساويين، وfalse خلاف ذلك.", usage = "يساوي(ش ، ي)", parameterTypes = {Object.class, Object.class}, returnType = Object.class)
	public static <T> boolean equals(T x, T y) {
		try {
			return (boolean) applyOperation(x, y, BinaryOperation.EQUALS);
		}
		catch (NaftahBugError bug) {
			if (bug.getBugText().equals("لا يمكن أن تكون الوسائط فارغة."))
				throw bug;
			return x.equals(y);
		}
	}

	@NaftahFn(name = "لا_يساوي", description = "دالة (لا_يساوي) تُستخدم لمقارنة عددين والتحقق مما إذا كانا غير متساويين في القيمة. تُرجع true إذا كان العددان غير متساويين، وfalse خلاف ذلك.", usage = "لا_يساوي(ش ، ي)", parameterTypes = {Object.class, Object.class}, returnType = Object.class)
	public static <T> boolean notEquals(T x, T y) {
		try {
			return (boolean) applyOperation(x, y, BinaryOperation.NOT_EQUALS);
		}
		catch (NaftahBugError bug) {
			if (bug.getBugText().equals("لا يمكن أن تكون الوسائط فارغة."))
				throw bug;
			return !x.equals(y);
		}
	}

	@NaftahFn(name = "أصغر_من", description = "دالة (أصغر_من) تُستخدم لمقارنة عددين والتحقق مما إذا كان العدد الأول أصغر من العدد الثاني. تُرجع true إذا كان الأول أصغر، وfalse خلاف ذلك.", usage = "أصغر_من(ش ، ي)", parameterTypes = {Object.class, Object.class}, returnType = Object.class)
	public static <T> boolean lessThan(T x, T y) {
		return (boolean) applyOperation(x, y, BinaryOperation.LESS_THAN);
	}

	@NaftahFn(name = "أصغر_أو_يساوي", description = "دالة (أصغر_أو_يساوي) تُستخدم لمقارنة عددين والتحقق مما إذا كان العدد الأول أصغر أو يساوي العدد الثاني. تُرجع true إذا كان الأول أصغر أو يساوي، وfalse خلاف ذلك.", usage = "أصغر_أو_يساوي(ش ، ي)", parameterTypes = {Object.class, Object.class}, returnType = Object.class)
	public static <T> boolean lessThanEquals(T x, T y) {
		return (boolean) applyOperation(x, y, BinaryOperation.LESS_THAN_EQUALS);
	}

	@NaftahFn(name = "أكبر_من", description = "دالة (أكبر_من) تُستخدم لمقارنة عددين والتحقق مما إذا كان العدد الأول أكبر من العدد الثاني. تُرجع true إذا كان الأول أكبر، وfalse خلاف ذلك.", usage = "أكبر_من(ش ، ي)", parameterTypes = {Object.class, Object.class}, returnType = Object.class)
	public static <T> boolean greaterThan(T x, T y) {
		return (boolean) applyOperation(x, y, BinaryOperation.GREATER_THAN);
	}

	@NaftahFn(name = "أكبر_أو_يساوي", description = "دالة (أكبر_أو_يساوي) تُستخدم لمقارنة عددين والتحقق مما إذا كان العدد الأول أكبر أو يساوي العدد الثاني. تُرجع true إذا كان الأول أكبر أو يساوي، وfalse خلاف ذلك.", usage = "أكبر_أو_يساوي(ش ، ي)", parameterTypes = {Object.class, Object.class}, returnType = Object.class)
	public static <T> boolean greaterThanEquals(T x, T y) {
		return (boolean) applyOperation(x, y, BinaryOperation.GREATER_THAN_EQUALS);
	}

	@NaftahFn(name = "و", description = "دالة (و) تُنفذ عملية 'AND' على الأعداد الثنائية (bitwise) المعطاة. تُرجع العدد الناتج عن العملية الثنائية بين العددين.", usage = "و(ش ، ي)", parameterTypes = {Object.class, Object.class}, returnType = Object.class)
	public static <T> Object and(T x, T y) {
		return applyOperation(x, y, BinaryOperation.BITWISE_AND);
	}

	@NaftahFn(name = "أو", description = "دالة (أو) تُنفذ عملية 'OR' الثنائية على الأعداد المعطاة. تُرجع العدد الناتج عن تطبيق العملية الثنائية بين العددين.", usage = "أو(ش ، ي)", parameterTypes = {Object.class, Object.class}, returnType = Object.class)
	public static <T> Object or(T x, T y) {
		return applyOperation(x, y, BinaryOperation.BITWISE_OR);
	}

	@NaftahFn(name = "حصري_أو", description = "دالة (حصري_أو) تُنفذ عملية 'XOR' الثنائية على الأعداد المعطاة. تُرجع العدد الناتج عن تطبيق العملية الثنائية الحصرية بين العددين.", usage = "حصري_أو(ش ، ي)", parameterTypes = {Object.class, Object.class}, returnType = Object.class)
	public static <T> Object xor(T x, T y) {
		return applyOperation(x, y, BinaryOperation.BITWISE_XOR);
	}

	@NaftahFn(name = "ليس", description = "دالة (ليس) تُنفذ عملية النفي الثنائي (bitwise NOT) على العدد المعطى. تُرجع العدد الناتج عن عكس كل بت في العدد.", usage = "ليس(ش)", parameterTypes = {Object.class, Object.class}, returnType = Object.class)
	public static <T> Object not(T x) {
		return applyOperation(x, UnaryOperation.BITWISE_NOT);
	}

	@NaftahFn(name = "إزاحة_إلى_اليسار", description = "دالة (إزاحة_إلى_اليسار) تُنفذ عملية إزاحة البتات للعدد المعطى إلى اليسار بعدد المواقع المحدد. تُستخدم هذه العملية في الحسابات الثنائية لتعظيم القيمة.", usage = "إزاحة_إلى_اليسار(ش ، مواقِع)", parameterTypes = {Number.class, int.class}, returnType = Number.class)
	public static <T extends Number> Number shiftLeft(T x, int positions) {
		return NumberUtils.shiftLeft(x, positions);
	}

	@NaftahFn(name = "إزاحة_إلى_اليمين", description = "دالة (إزاحة_إلى_اليمين) تُنفذ عملية إزاحة البتات للعدد المعطى إلى اليمين بعدد المواقع المحدد. تُستخدم هذه العملية في الحسابات الثنائية لتقليل القيمة.", usage = "إزاحة_إلى_اليمين(ش ، مواقِع)", parameterTypes = {Number.class, int.class}, returnType = Number.class)
	public static <T extends Number> Number shiftRight(T x, int positions) {
		return NumberUtils.shiftRight(x, positions);
	}

	@NaftahFn(name = "إزاحة_إلى_اليمين_غير_موقعة", description = "دالة (إزاحة_إلى_اليمين_غير_موقعة) تُنفذ عملية إزاحة البتات للعدد المعطى إلى اليمين بدون اعتبار الإشارة، بعدد المواقع المحدد. تُستخدم هذه العملية لمعالجة الأعداد بدون تأثير الإشارة السالبة.", usage = "إزاحة_إلى_اليمين_غير_موقعة(ش ، مواقِع)", parameterTypes = {Number.class, int.class}, returnType = Number.class)
	public static <T extends Number> Number unsignedShiftRight(T x, int positions) {
		return NumberUtils.unsignedShiftRight(x, positions);
	}

	@NaftahFn(name = "زيادة_قبلية", description = "دالة (زيادة_قبلية) تُزيد العدد المعطى بمقدار واحد قبل استخدامه في التعبير.", usage = "زيادة_قبلية(ش)", parameterTypes = {Object.class, Object.class}, returnType = Object.class)
	public static <T> Object preIncrement(T x) {
		return applyOperation(x, UnaryOperation.PRE_INCREMENT);
	}

	@NaftahFn(name = "زيادة_بعدية", description = "دالة (زيادة_بعدية) تُزيد العدد المعطى بمقدار واحد بعد استخدامه في التعبير.", usage = "زيادة_بعدية(ش)", parameterTypes = {Object.class, Object.class}, returnType = Object.class)
	public static <T> Object postIncrement(T x) {
		return applyOperation(x, UnaryOperation.POST_INCREMENT);
	}

	@NaftahFn(name = "نقصان_قبلي", description = "دالة (نقصان_قبلي) تُنقص العدد المعطى بمقدار واحد قبل استخدامه في التعبير.", usage = "نقصان_قبلي(ش)", parameterTypes = {Object.class, Object.class}, returnType = Object.class)
	public static <T> Object preDecrement(T x) {
		return applyOperation(x, UnaryOperation.PRE_DECREMENT);
	}

	@NaftahFn(name = "نقصان_بعدي", description = "دالة (نقصان_بعدي) تُنقص العدد المعطى بمقدار واحد بعد استخدامه في التعبير.", usage = "نقصان_بعدي(ش)", parameterTypes = {Object.class, Object.class}, returnType = Object.class)
	public static <T> Object postDecrement(T x) {
		return applyOperation(x, UnaryOperation.POST_DECREMENT);
	}
}
