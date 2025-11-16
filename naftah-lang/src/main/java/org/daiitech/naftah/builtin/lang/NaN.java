package org.daiitech.naftah.builtin.lang;

/**
 * Represents a singleton instance of a non-numeric value (NaN).
 * <p>
 * This class is used to indicate an invalid or undefined result
 * in custom expression evaluation, similar to {@code Double.NaN}
 * but at the object level.
 * <p>
 * يستخدم هذا الكائن لتمثيل قيمة غير رقمية (NaN) في لغة مخصصة أو مفسر،
 * وهو بديل مخصص لـ {@code Double.NaN} يمكن استخدامه في القيم الديناميكية.
 *
 * @author Chakib Daii
 */
public final class NaN {

	/**
	 * The single instance of {@code NaN}.
	 * <p>
	 * النسخة الوحيدة المتاحة من {@code NaN}.
	 */
	private static final NaN INSTANCE = new NaN();

	private NaN() {
	}

	/**
	 * Returns the singleton {@code NaN} instance.
	 * <p>
	 * يُرجع النسخة الوحيدة من {@code NaN}.
	 *
	 * @return the singleton instance representing "not a number"
	 */
	public static Object get() {
		return INSTANCE;
	}

	/**
	 * Checks whether the given object is the singleton {@code NaN} instance.
	 * <p>
	 * يتحقق مما إذا كان الكائن المُعطى هو قيمة {@code NaN}.
	 *
	 * @param object the object to check
	 * @return {@code true} if the object is the {@code NaN} instance, otherwise {@code false}
	 */
	public static boolean isNaN(Object object) {
		return object == INSTANCE;
	}

	/**
	 * Returns a string representation of the {@code NaN} value.
	 * <p>
	 * يُرجع تمثيلاً نصياً للقيمة غير الرقمية.
	 *
	 * @return the string {@code "<قيمة_غير_رقمية>"}
	 */
	@Override
	public String toString() {
		return "<قيمة_غير_رقمية>";
	}
}
