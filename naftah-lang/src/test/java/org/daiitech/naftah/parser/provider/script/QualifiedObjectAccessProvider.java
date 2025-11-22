package org.daiitech.naftah.parser.provider.script;

import java.util.LinkedHashMap;
import java.util.stream.Stream;

import org.daiitech.naftah.builtin.lang.DynamicNumber;
import org.daiitech.naftah.builtin.lang.NaftahObject;
import org.daiitech.naftah.builtin.lang.None;
import org.daiitech.naftah.builtin.utils.Tuple;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import static org.daiitech.naftah.parser.DefaultContext.newNaftahBugVariableNotFoundError;

public class QualifiedObjectAccessProvider implements ArgumentsProvider {

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
		return Stream
				.of(
					Arguments
							.of(true,
								"""
								متغير كائن_فارغ تعيين @{}
								كائن_فارغ
								""",
								NaftahObject.of(new LinkedHashMap<>()),
								null),
					Arguments.of(true, """
										متغير المدينة تعيين {
										متغير الاسم تعيين "قبلي",
										متغير عدد_السكان تعيين 50000,
										متغير البلدة تعيين {
											متغير الاسم تعيين "سوق الأحد",
											متغير عدد_البيوت تعيين 400
										}
										}
										المدينة["البلدة"]["عدد_البيوت"]
										""", 400, null),
					Arguments.of(false, """
										متغير المدينة تعيين @{
										متغير الاسم تعيين "قبلي",
										متغير عدد_السكان تعيين 50000,
										متغير البلدة تعيين {
											متغير الاسم تعيين "سوق الأحد",
											متغير عدد_البيوت تعيين 400
										}
										}
										المدينة؟["ء"]:عدد_البيوت
										""", null, newNaftahBugVariableNotFoundError("المدينة؟:ء", 9, 0)),
					Arguments.of(true, """
										متغير المدينة تعيين {
										متغير الاسم تعيين "قبلي",
										متغير عدد_السكان تعيين 50000,
										متغير البلدة تعيين {
											متغير الاسم تعيين "سوق الأحد",
											متغير عدد_البيوت تعيين 400
										}
										}
										المدينة؟["ء"]؟["عدد_البيوت"]
										""", None.get(), null),
					Arguments.of(false, """
										متغير المدينة تعيين {
										متغير الاسم تعيين "قبلي",
										متغير عدد_السكان تعيين 50000,
										متغير البلدة تعيين {
											متغير الاسم تعيين "سوق الأحد",
											متغير عدد_البيوت تعيين 400
										}
										}
										المدي؟:البلدة؟["عدد_البيوت"]
										""", null, newNaftahBugVariableNotFoundError("المدي")),
					Arguments
							.of(false,
								"""
								متغير المدينة تعيين {
								متغير الاسم تعيين "قبلي",
								متغير عدد_السكان تعيين 50000,
								متغير البلدة تعيين {
									متغير الاسم تعيين "سوق الأحد",
									متغير عدد_البيوت تعيين 400
								}
								}
								المدينة؟["البلدة"]؟:عددت
								""",
								null,
								newNaftahBugVariableNotFoundError("المدينة؟:البلدة؟:عددت", 9, 0)),
					Arguments.of(true, """
										متغير المدينة تعيين {
										متغير الاسم تعيين "قبلي",
										متغير عدد_السكان تعيين 50000,
										متغير البلدة تعيين {
											متغير الاسم تعيين "سوق الأحد",
											متغير عدد_البيوت تعيين 400
										}
										}
										المدينة["البلدة"]["عدد_البيوت"]
										""", 400, null),
					Arguments.of(false, """
										متغير المدينة تعيين {
										متغير الاسم تعيين "قبلي",
										متغير عدد_السكان تعيين 50000,
										متغير البلدة تعيين {
											متغير الاسم تعيين "سوق الأحد",
											متغير عدد_البيوت تعيين 400
										}
										}
										المدينة؟["ء"]["عدد_البيوت"]
										""", null, newNaftahBugVariableNotFoundError("المدينة؟:ء", 9, 0)),
					Arguments.of(true, """
										متغير المدينة تعيين {
										متغير الاسم تعيين "قبلي",
										متغير عدد_السكان تعيين 50000,
										متغير البلدة تعيين {
											متغير الاسم تعيين "سوق الأحد",
											متغير عدد_البيوت تعيين 400
										}
										}
										المدينة؟["ء"]؟["عدد_البيوت"]
										""", None.get(), null),
					Arguments.of(false, """
										متغير المدينة تعيين {
										متغير الاسم تعيين "قبلي",
										متغير عدد_السكان تعيين 50000,
										متغير البلدة تعيين {
											متغير الاسم تعيين "سوق الأحد",
											متغير عدد_البيوت تعيين 400
										}
										}
										المدي؟["البلدة"]؟["عدد_البيوت"]
										""", null, newNaftahBugVariableNotFoundError("المدي")),
					Arguments
							.of(false,
								"""
								متغير المدينة تعيين {
								متغير الاسم تعيين "قبلي",
								متغير عدد_السكان تعيين 50000,
								متغير البلدة تعيين {
									متغير الاسم تعيين "سوق الأحد",
									متغير عدد_البيوت تعيين 400
								}
								}
								المدينة؟["البلدة"]؟["عددت"]
								""",
								null,
								newNaftahBugVariableNotFoundError("المدينة؟:البلدة؟:عددت", 9, 0)),
					Arguments
							.of(true,
								"""
								ثابت معرف_مستخدم تعيين جافا:أدة:معرف_مستخدم(6161415689025233999؛5488940234982179551)

								(
								--- أمثلة على الوصول المشروط والاختياري مع وجود علامات استفهام
								معرف_مستخدم:أكثر_سيغ_بتات,
								معرف_مستخدم:لشرقا_سيغ_بتات,

								--- أمثلة على الوصول إلى الحقول باستخدام الأقواس المربعة []
								معرف_مستخدم["أكثر_سيغ_بتات"],
								معرف_مستخدم["لشرقا_سيغ_بتات"]
								)
								""",
								Tuple
										.of(6161415689025233999L,
											5488940234982179551L,
											6161415689025233999L,
											5488940234982179551L),
								null),
					Arguments.of(true, """
										ثابت ديناميك_رقم تعيين أورغ:داعيتاك:نفطة:مدرجة_مدرجة:لغة:ديناميك_رقم(1)


										(
										--- أمثلة على الوصول المشروط والاختياري مع وجود علامات استفهام
										ديناميك_رقم؟:قيمة,

										--- أمثلة على الوصول إلى الحقول باستخدام الأقواس المربعة []
										ديناميك_رقم؟["قيمة"]
										)
										""", Tuple.of(1, 1), null),
					Arguments
							.of(true,
								"""
								ثابت قائمة تعيين جافا:أدة:صفائف_القائمة([1؛100؛0])


								(
								--- أمثلة على الوصول المشروط والاختياري مع وجود علامات استفهام
								قائمة:الحجم,
								قائمة:عنصر_بيانات,

								--- أمثلة على الوصول إلى الحقول باستخدام الأقواس المربعة []
								قائمة["الحجم"],
								قائمة["عنصر_بيانات"]
								)
								""",
								Tuple
										.of(3,
											new Object[]{   DynamicNumber.of(1),
															DynamicNumber.of(100),
															DynamicNumber.of(0),
															null,
															null,
															null,
															null,
															null,
															null,
															null},
											3,
											new Object[]{   DynamicNumber.of(1),
															DynamicNumber.of(100),
															DynamicNumber.of(0),
															null,
															null,
															null,
															null,
															null,
															null,
															null}),
								null)
				);
	}
}
