package org.daiitech.naftah.parser.provider.script;

import java.util.stream.Stream;

import org.daiitech.naftah.builtin.lang.None;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import static org.daiitech.naftah.parser.DefaultContext.newNaftahBugVariableNotFoundError;

public class QualifiedObjectAccessProvider implements ArgumentsProvider {

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
		return Stream
				.of(
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
										المدينة؟["ء"]:عدد_البيوت
										""", null, newNaftahBugVariableNotFoundError("المدينة؟:ء")),
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
					Arguments.of(false, """
										متغير المدينة تعيين {
										متغير الاسم تعيين "قبلي",
										متغير عدد_السكان تعيين 50000,
										متغير البلدة تعيين {
											متغير الاسم تعيين "سوق الأحد",
											متغير عدد_البيوت تعيين 400
										}
										}
										المدينة؟["البلدة"]؟:عددت
										""", null, newNaftahBugVariableNotFoundError("المدينة؟:البلدة؟:عددت")),
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
										""", null, newNaftahBugVariableNotFoundError("المدينة؟:ء")),
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
					Arguments.of(false, """
										متغير المدينة تعيين {
										متغير الاسم تعيين "قبلي",
										متغير عدد_السكان تعيين 50000,
										متغير البلدة تعيين {
											متغير الاسم تعيين "سوق الأحد",
											متغير عدد_البيوت تعيين 400
										}
										}
										المدينة؟["البلدة"]؟["عددت"]
										""", null, newNaftahBugVariableNotFoundError("المدينة؟:البلدة؟:عددت"))
				);
	}
}
