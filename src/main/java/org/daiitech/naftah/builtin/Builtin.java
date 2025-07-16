package org.daiitech.naftah.builtin;

import static org.daiitech.naftah.builtin.utils.ObjectUtils.getNaftahValueToString;
import static org.daiitech.naftah.parser.NaftahParserHelper.NULL;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.padText;

import java.util.Objects;
import org.daiitech.naftah.builtin.utils.NumberUtils;
import org.daiitech.naftah.errors.NaftahBugError;

/**
 * @author Chakib Daii
 */
@NaftahFnProvider(
    name = "مزوّد دوال مدمجة",
    description =
        "يحتوي هذا الموفر على دوال مدمجة تُستخدم ضمن لغة نفطه لأداء عمليات حسابية ومنطقية مختلفة بدقة وكفاءة.",
    functionNames = {"جمع", "طرح", "ضرب", "قسمة", "باقي_القسمة", "إطبع"})
public final class Builtin {

  private Builtin() {
    throw new NaftahBugError("استخدام غير مسموح به.");
  }

  @NaftahFn(
      name = "إطبع",
      description =
          "تعليمة الطباعة (إطبع) هي التعليمة التي تُستخدم في البرمجة لإظهار نص معين على الشاشة، مثل إظهار رسالة ترحيبية للمستخدم.",
      usage = "إطبع(ش)",
      parameterTypes = {Object.class})
  public static void print(Object o) {
    if (Objects.nonNull(o)) {
      padText(getNaftahValueToString(o), true);
    } else System.out.println(padText(NULL, false));
  }

  @NaftahFn(
      name = "جمع",
      description =
          "إضافة الأعداد معًا للحصول على مجموع. هو العملية الأساسية التي تُستخدم في الرياضيات لتحديد القيمة الإجمالية من خلال جمع عدة أرقام.",
      usage = "جمع(ش ، ي)",
      parameterTypes = {Number.class, Number.class},
      returnType = Number.class)
  public static <T extends Number> Number add(T x, T y) {
    return NumberUtils.add(x, y);
  }

  @NaftahFn(
      name = "طرح",
      description =
          "طرح الأعداد للحصول على الفرق. هو العملية الأساسية التي تُستخدم في الرياضيات لتحديد القيمة المتبقية عند إزالة قيمة عدد من عدد آخر.",
      usage = "طرح(ش ، ي)",
      parameterTypes = {Number.class, Number.class},
      returnType = Number.class)
  public static <T extends Number> Number subtract(T x, T y) {
    return NumberUtils.subtract(x, y);
  }

  @NaftahFn(
      name = "ضرب",
      description =
          "ضرب الأعداد للحصول على الناتج هو العملية الأساسية التي تُستخدم في الرياضيات لتحديد القيمة الإجمالية عند تكرار جمع عدد معين عدة مرات.",
      usage = "ضرب(ش ، ي)",
      parameterTypes = {Number.class, Number.class},
      returnType = Number.class)
  public static <T extends Number> Number multiply(T x, T y) {
    return NumberUtils.multiply(x, y);
  }

  @NaftahFn(
      name = "قسمة",
      description =
          "قسمة الأعداد للحصول على خارج القسمة هي العملية الأساسية التي تُستخدم في الرياضيات لتحديد كم مرة يمكن تقسيم عدد إلى أجزاء متساوية.",
      usage = "قسمة(ش ، ي)",
      parameterTypes = {Number.class, Number.class},
      returnType = Number.class)
  public static <T extends Number> Number divide(T x, T y) {
    return NumberUtils.divide(x, y);
  }

  @NaftahFn(
      name = "باقي_القسمة",
      description =
          "عملية باقي القسمة (المودولو) هي العملية التي تُستخدم في الرياضيات لتحديد الباقي المتبقي بعد قسمة عدد على عدد آخر.",
      usage = "باقي_القسمة(ش ، ي)",
      parameterTypes = {Number.class, Number.class},
      returnType = Number.class)
  public static <T extends Number> Number modulo(T x, T y) {
    return NumberUtils.modulo(x, y);
  }
}
