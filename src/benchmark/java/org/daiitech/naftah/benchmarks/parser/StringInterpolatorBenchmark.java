package org.daiitech.naftah.benchmarks.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.daiitech.naftah.parser.StringInterpolator;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3)
@Measurement(iterations = 5)
@Fork(1)
@State(Scope.Thread)
public class StringInterpolatorBenchmark {

    @Param(
            {
                    // Simple substitution
                    "مرحباً ${الاسم}",

                    // Multiple variables
                    "أهلاً ${الاسم}، لديك ${عدد_الرسائل} رسالة جديدة",

                    // Right-to-left mixed with left-to-right (emails, numbers)
                    "البريد الإلكتروني: ${البريد_الإلكتروني}، الرصيد: ${الرصيد} ريال",

                    // Complex expression (should be pre-resolved by context)
                    "مرحبا ${الاسم_الأول} ${الاسم_الأخير}، رقم المستخدم: ${معرف_المستخدم}",

                    // Text with non-trivial spacing/punctuation
                    "مرحباً بك، يا ${الاسم}! لقد أرسلت ${عدد_الرسائل} رسائل اليوم.",

                    // Placeholder with nested pattern-like value (mimicking a bug scenario)
                    "القيمة: ${map[\"القيمة\"]} يجب أن تكون مساوية لـ ${المتوقع}",

                    // Arabic number formatting (assumes context provides formatted values)
                    "المبلغ الإجمالي: ${المبلغ_الإجمالي} د.إ",

                    // Long sentence with multiple variables
                    "عزيزي ${الاسم}، تم تسجيل الدخول من الجهاز ${الجهاز} في الساعة ${الوقت} بتاريخ ${التاريخ}.",

                    // Repeated variables
                    "مرحبا ${الاسم}، ${الاسم}، كيف حالك؟",

                    // Arabic with brackets and RTL punctuation
                    "«${الاسم}» قام بعملية شراء بمبلغ «${المبلغ}» في {${المكان}}$.",

                    // Simulated fallback / missing variable
                    "البيانات غير مكتملة: ${العمر} سنة، الحالة: ${الحالة}",

                    // Placeholder with a default/fallback (TODO: add support)
                    "اسم العميل: ${الاسم:غير معروف}",
                    // long text with variables (100 vars)
                    """
                    اسم الموظف: ${اسم_الموظف}, المسمى الوظيفي: ${المسمى_الوظيفي}, القسم: ${القسم}, الراتب الأساسي: ${الراتب_الأساسي} ريال،
                    بدل السكن: ${بدل_السكن}، بدل النقل: ${بدل_النقل}، مجموع البدلات: ${مجموع_البدلات}،
                    الخصومات: ${الخصومات}، صافي الراتب: ${صافي_الراتب}، تاريخ الانضمام: ${تاريخ_الانضمام}، رقم الموظف: ${رقم_الموظف}،
                    عدد الأيام: ${عدد_الأيام}، ساعات العمل: ${ساعات_العمل}، حالة الموظف: ${الحالة}،
                    البريد الإلكتروني: ${البريد_الإلكتروني}, رقم الهاتف: ${رقم_الهاتف}،
                    اسم البنك: ${اسم_البنك}, رقم الحساب: ${رقم_الحساب}،
                    مشروع 1: ${مشروع_١}, مشروع 2: ${مشروع_٢}, مشروع 3: ${مشروع_٣}،
                    تقييم الأداء: ${تقييم_الأداء}, ملاحظات: ${ملاحظات}،
                    توقيع المدير: ${توقيع_المدير}, توقيع الموظف: ${توقيع_الموظف}،
                    بند 1: ${بند_١}, بند 2: ${بند_٢}, بند 3: ${بند_٣}, بند 4: ${بند_٤}, بند 5: ${بند_٥}،
                    قسم 1: ${قسم_١}, قسم 2: ${قسم_٢}, قسم 3: ${قسم_٣}, قسم 4: ${قسم_٤}, قسم 5: ${قسم_٥}،
                    متغير إضافي 1: ${متغير_١}, متغير إضافي 2: ${متغير_٢}, متغير إضافي 3: ${متغير_٣}،
                    متغير إضافي 4: ${متغير_٤}, متغير إضافي 5: ${متغير_٥}،
                    رسالة: ${رسالة}, ملاحظة: ${ملاحظة}, نوع العقد: ${نوع_العقد}, فترة التجربة: ${فترة_التجربة}،
                    الرصيد السنوي: ${الرصيد_السنوي}, الرصيد المرضي: ${الرصيد_المرضي}, الرصيد المتبقي: ${الرصيد_المتبقي}،
                    العنوان: ${العنوان}, المدينة: ${المدينة}, الدولة: ${الدولة}،
                    معرف النظام: ${معرف_النظام}, نوع النظام: ${نوع_النظام}, الإصدار: ${الإصدار}،
                    معرف البصمة: ${معرف_البصمة}, الرمز الداخلي: ${الرمز_الداخلي}, رقم الملف: ${رقم_الملف}،
                    آخر تحديث: ${آخر_تحديث}, محدث بواسطة: ${محدث_بواسطة}, الحالة الحالية: ${الحالة_الحالية}،
                    الراتب بعد التعديل: ${الراتب_بعد_التعديل}, تاريخ التعديل: ${تاريخ_التعديل}،
                    تصنيف الأداء: ${تصنيف_الأداء}, ملاحظات الأداء: ${ملاحظات_الأداء}
                    """})
    String input;

    Map<String, Object> context;

    @Setup
    public void setup() {
        context = new HashMap<>();
        context.put("الاسم", "علي");
        context.put("عدد_الرسائل", 5);
        context.put("البريد_الإلكتروني", "ali@example.com");
        context.put("الرصيد", "١٢٠٫٥٠");
        context.put("الاسم_الأول", "علي");
        context.put("الاسم_الأخير", "النعيمي");
        context.put("user_id", 9876);
        context.put("المكان", "دبي");
        context.put("الوقت", "١٢:٣٠");
        context.put("التاريخ", "٢٠٢٥-٠٨-٠٦");
        context.put("الجهاز", "iPhone");
        context.put("map[\"القيمة\"]", "٣٠");
        context.put("المتوقع", "٣٠");
        context.put("المبلغ_الإجمالي", "١٬٥٠٠٫٠٠");
        context.put("العمر", null);
        context.put("الحالة", null);
        for (int i = 1; i <= 100; i++) {
            context.put("متغير_" + i, "قيمة_" + i);
        }
        context.put("اسم_الموظف", "أحمد خالد");
        context.put("المسمى_الوظيفي", "مهندس برمجيات");
        context.put("القسم", "تكنولوجيا المعلومات");
        context.put("الراتب_الأساسي", "15000");
        context.put("بدل_السكن", "5000");
        context.put("بدل_النقل", "1000");
        context.put("مجموع_البدلات", "6000");
        context.put("الخصومات", "750");
        context.put("صافي_الراتب", "20250");
        context.put("تاريخ_الانضمام", "2020-01-15");
        context.put("رقم_الموظف", "EMP12345");
        context.put("عدد_الأيام", "30");
        context.put("ساعات_العمل", "160");
        context.put("الحالة", "نشط");
        context.put("البريد_الإلكتروني", "ahmed@example.com");
        context.put("رقم_الهاتف", "0501234567");
        context.put("اسم_البنك", "بنك الرياض");
        context.put("رقم_الحساب", "SA123456789012345");
        context.put("مشروع_١", "نظام الموارد البشرية");
        context.put("مشروع_٢", "نظام التذاكر");
        context.put("مشروع_٣", "نظام الحضور والانصراف");
        context.put("تقييم_الأداء", "ممتاز");
        context.put("ملاحظات", "لا توجد");
        context.put("توقيع_المدير", "مدير الموارد البشرية");
        context.put("توقيع_الموظف", "أحمد خالد");
        context.put("بند_١", "القانون ١");
        context.put("بند_٢", "القانون ٢");
        context.put("بند_٣", "القانون ٣");
        context.put("بند_٤", "القانون ٤");
        context.put("بند_٥", "القانون ٥");
        context.put("قسم_١", "الدعم الفني");
        context.put("قسم_٢", "البرمجيات");
        context.put("قسم_٣", "الشبكات");
        context.put("قسم_٤", "الأمن السيبراني");
        context.put("قسم_٥", "الصيانة");
        context.put("رسالة", "مرحبًا بعودتك إلى الشركة");
        context.put("ملاحظة", "يرجى تحديث بياناتك");
        context.put("نوع_العقد", "دوام كامل");
        context.put("فترة_التجربة", "3 أشهر");
        context.put("الرصيد_السنوي", "21 يوم");
        context.put("الرصيد_المرضي", "10 أيام");
        context.put("الرصيد_المتبقي", "15 يوم");
        context.put("العنوان", "الرياض، شارع الملك فهد");
        context.put("المدينة", "الرياض");
        context.put("الدولة", "المملكة العربية السعودية");
        context.put("معرف_النظام", "SYS9876");
        context.put("نوع_النظام", "ERP");
        context.put("الإصدار", "v2.3.1");
        context.put("معرف_البصمة", "FP123456");
        context.put("الرمز_الداخلي", "INT987");
        context.put("رقم_الملف", "F123456");
        context.put("آخر_تحديث", "2025-08-06");
        context.put("محدث_بواسطة", "نظام التحديث");
        context.put("الحالة_الحالية", "فعال");
        context.put("الراتب_بعد_التعديل", "21000");
        context.put("تاريخ_التعديل", "2025-07-01");
        context.put("تصنيف_الأداء", "A");
        context.put("ملاحظات_الأداء", "أداء ممتاز، بحاجة إلى تحسين في التواصل");
    }

    @Benchmark
    public String benchmarkInterpolation() {
        return StringInterpolator.process(input, context);
    }
}
