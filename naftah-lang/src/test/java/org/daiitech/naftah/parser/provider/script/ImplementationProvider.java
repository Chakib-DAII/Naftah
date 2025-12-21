package org.daiitech.naftah.parser.provider.script;

import java.util.stream.Stream;

import org.daiitech.naftah.builtin.lang.None;
import org.daiitech.naftah.builtin.utils.tuple.NTuple;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class ImplementationProvider implements ArgumentsProvider {
	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
		return Stream
				.of(Arguments
						.of(true,
							"""
							--- تعريف متغير 'شخص' ككائن يحتوي حقول الاسم، العمر، حالة الزواج
							متغير شخص تعيين {
							ثابت الاسم تعيين "نورالدين",
							ثابت العمر تعيين 72,
							متغير متزوج تعيين صحيح,
							متغير متوفي تعيين خطأ,
							متغير سعيد تعيين صحيح
							}

							سلوك شخص {

							--- getters (دوال قراءة)

							دالة الاسم() {
								هذا:الاسم
							}

							دالة العمر() {
								ارجع هذا:العمر
							}

							دالة هل_متزوج() {
								هذا:متزوج
							}

							دالة هل_متوفي() {
								ارجع هذا:متوفي
							}

							دالة هل_سعيد() {
								هذا:سعيد
							}

							--- setters (دوال تعديل)

							دالة إجعل_متزوج(قيمة: منطقي) {
								إذا هذا:متوفي إذن
								{
								ارجع خطأ
								}
								غير_ذلك
								{
									هذا:متزوج تعيين قيمة
									ارجع صحيح
								}
							}

							دالة إجعل_سعيد(قيمة: منطقي)  : عدم {
								إذا هذا:متوفي إذن
								{
								هذا:سعيد تعيين خطأ
								}
								غير_ذلك
								{
								هذا:سعيد تعيين قيمة
								}
							}

							دالة توفي() : عدم {
								هذا:متوفي تعيين صحيح
								هذا:سعيد تعيين خطأ
								هذا:متزوج تعيين خطأ
							}

							--- منطق إضافي

							دالة هل_حي() {
								ارجع ليس هذا:متوفي
							}

							دالة وصف() {
								"الإسم: " + هذا:الاسم
								+ "
							العمر: " + هذا:العمر
								+ "
							متزوج: " + هذا:متزوج
								+ "
							متوفي: " + هذا:متوفي
								+ "
							سعيد: " + هذا:سعيد
							}

							--- طباعة

							دالة إطبع() {
								إطبع(هذا::وصف())
							}
							}

							(
							شخص::الاسم(),
							شخص::العمر(),
							شخص::هل_متزوج(),
							شخص::هل_متوفي(),
							شخص::هل_سعيد(),
							شخص::هل_حي(),

							شخص::إجعل_متزوج(خطأ),
							شخص::إطبع(),

							شخص::إجعل_سعيد(خطأ),
							شخص::إطبع(),

							شخص::توفي(),
							شخص::إطبع(),
							)
							""",
							NTuple
									.of(
										"نورالدين",
										72,
										true,
										false,
										true,
										true,
										true,
										None.get(),
										None.get(),
										None.get(),
										None.get(),
										None.get()
									),
							null));
	}
}
