// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

package org.daiitech.naftah.parser.provider.script;

import java.util.stream.Stream;

import org.daiitech.naftah.builtin.lang.None;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import static org.daiitech.naftah.parser.DefaultContext.newNaftahBugVariableNotFoundError;

public class QualifiedNameProvider implements ArgumentsProvider {
	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
		return Stream
				.of(
					Arguments.of(true, """
										ثابت ت : جڤ:لنگ:لنگ تعيين 2
										ت
										""", 2, null),
					Arguments.of(true, """
										متغير أ تعيين {متغير أ تعيين ١ , متغير ب تعيين 4}
										أ:أ
										""", 1, null),
					Arguments.of(true, """
										متغير المدينة تعيين {
										متغير الاسم تعيين "قبلي",
										متغير عدد_السكان تعيين 50000,
										متغير البلدة تعيين {
											متغير الاسم تعيين "سوق الأحد",
											متغير عدد_البيوت تعيين 400
										}
										}
										المدينة:البلدة:عدد_البيوت
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
										المدينة؟:ء:عدد_البيوت
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
										المدينة؟:ء؟:عدد_البيوت
										""", None.get(), null),
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
								المدي؟:البلدة؟:عدد_البيوت
								""",
								null,
								newNaftahBugVariableNotFoundError(  "المدي",
																	9,
																	0)),
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
								المدينة؟:البلدة؟:عددت
								""",
								null,
								newNaftahBugVariableNotFoundError(  "المدينة؟:البلدة؟:عددت",
																	9,
																	0))
				);
	}
}
