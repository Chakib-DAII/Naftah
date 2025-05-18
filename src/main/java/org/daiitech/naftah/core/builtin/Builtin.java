package org.daiitech.naftah.core.builtin;

/**
 * @author Chakib Daii
 **/
public final class Builtin {

    private Builtin() {
        throw new IllegalStateException("Illegal usage.");
    }

    @NaftahFn(name = "جمع",
            description = "إضافة الأعداد معًا للحصول على مجموع. هو العملية الأساسية التي تُستخدم في الرياضيات لتحديد القيمة الإجمالية من خلال جمع عدة أرقام.",
            usage = "جمع(ش ، ي)",
            parameterTypes = {Number.class, Number.class},
            returnType = Number.class)
    public <T extends Number> Number add(T x, T y) {
        if (x instanceof Integer xi && y instanceof Integer yi) return xi + yi;
        return null;
    }
}
