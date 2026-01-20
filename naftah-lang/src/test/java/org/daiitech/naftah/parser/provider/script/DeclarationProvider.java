// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

package org.daiitech.naftah.parser.provider.script;

import java.util.Arrays;
import java.util.stream.Stream;

import org.daiitech.naftah.errors.ExceptionUtils;
import org.daiitech.naftah.errors.NaftahBugError;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class DeclarationProvider implements ArgumentsProvider {
	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
		return Stream
				.of(Arguments.of(true, """
										ثابت ت
										ت
										""", null, null),
					Arguments.of(true, """
										متغير ش
										ش
										""", null, null),
					Arguments.of(true, """
										ثابت ت : أي_نمط
										ت
										""", null, null),
					Arguments.of(true, """
										متغير ش : منطقي
										ش
										""", null, null),
					Arguments.of(true, """
										متغير ع : تسلسل_رموز
											ع
										""", null, null),
					Arguments.of(true, """
										ثابت ي : بوليان
										ي
										""", null, null),
					Arguments.of(true, """
										ثابت ي
										ي
										""", null, null),
					Arguments.of(true, """
										ثابت ز : حرف
										ز
										""", null, null),
					Arguments.of(true, """
										ثابت س : عدد_قصير_جدا
										س
										""", null, null),
					Arguments.of(true, """
										ثابت ص : عدد_قصير
										ص
										""", null, null),
					Arguments.of(true, """
										ثابت ه : عدد_صحيح
										ه
										""", null, null),
					Arguments.of(true, """
										ثابت ض : عدد_صحيح
										ض
										""", null, null),
					Arguments.of(true, """
										ثابت ق : عدد_طويل
										ق
										""", null, null),
					Arguments.of(true, """
										ثابت ذ : عدد_طويل
										ذ
										""", null, null),
					Arguments.of(true, """
										ثابت ل : عدد_عائم
										ل
										""", null, null),
					Arguments.of(true, """
										ثابت ك : عدد_عائم_طويل
										ك
										""", null, null),
					Arguments
							.of(false,
								"""
								ثابت ت
								ثابت ت: عدد_عائم_طويل
								""",
								null,
								new NaftahBugError("المتغير 'ت' موجود في السياق الحالي. لا يمكن إعادة إعلانه.", 2, 0)),
					Arguments
							.of(true,
								"""
								ثابت ت٫ش٬ع،ي؛ز,س؛ص٫ه٬ض،ق
								[ت٫ش٬ع،ي؛ز,س؛ص٫ه٬ض،ق]
								""",
								Arrays.asList(null, null, null, null, null, null, null, null, null, null),
								null),
					Arguments
							.of(true,
								"""
								ثابت ت٫ش٬ع،ي؛ز,س؛ص٫ه٬ض،ق: منطقي
								[
								ت٫ش٬ع،ي؛ز,س؛ص٫ه٬ض،ق
								]
								""",
								Arrays.asList(null, null, null, null, null, null, null, null, null, null),
								null),
					Arguments
							.of(true,
								"""
								ثابت ت٫ش٬ع،ي؛ز,س؛ص٫ه٬ض،ق: منطقي؛تسلسل_رموز؛عدد_طويل؛عدد_عائم_طويل؛عدد_عائم؛عدد_صحيح؛رمز؛عدد_قصير_جدا؛عدد_قصير؛منطقي
								[
								ت٫ش٬ع،ي؛ز,س؛ص٫ه٬ض،ق
								]
								""",
								Arrays.asList(null, null, null, null, null, null, null, null, null, null),
								null),
					Arguments
							.of(true,
								"""
								ثابت ت٫ش٬ع،ي؛ز,س؛ص٫ه٬ض،ق: منطقي؛تسلسل_رموز؛عدد_طويل؛عدد_عائم_طويل
								[
								ت٫ش٬ع،ي؛ز,س؛ص٫ه٬ض،ق
								]
								""",
								Arrays.asList(null, null, null, null, null, null, null, null, null, null),
								null),
					Arguments
							.of(false,
								"""
								ثابت ت٫ش٬ع،ي؛ز,س؛ص٫ه٬ت،ق: منطقي؛تسلسل_رموز؛عدد_طويل؛عدد_عائم_طويل
								[
								ت٫ش٬ع،ي؛ز,س؛ص٫ه٬ض،ق
								]
								""",
								null,
								new NaftahBugError("المتغير 'ت' موجود في السياق الحالي. لا يمكن إعادة إعلانه.", 1, 0)),
					Arguments
							.of(false,
								"""
								ثابت ت٫ش٬ع،ي؛ز,س؛ص٫ه٬ض،ق: منطقي٫تسلسل_رموز٫عدد_طويل٫عدد_عائم_طويل٫منطقي٫تسلسل_رموز٫عدد_طويل٫عدد_عائم_طويل٫منطقي٫تسلسل_رموز٫عدد_طويل٫عدد_عائم_طويل٫منطقي٫تسلسل_رموز٫عدد_طويل٫منطقي٫تسلسل_رموز٫عدد_طويل٫عدد_عائم_طويل٫منطقي٫تسلسل_رموز٫عدد_طويل٫عدد_عائم_طويل٫منطقي٫تسلسل_رموز٫عدد_طويل٫عدد_عائم_طويل٫منطقي٫تسلسل_رموز٫عدد_طويل٫عدد_عائم_طويل٫عدد_عائم_طويل
								[
								ت٫ش٬ع،ي؛ز,س؛ص٫ه٬ض،ق
								]
								""",
								null,
								ExceptionUtils.newNaftahSpecifiedTypesExceedVariableNamesError(1, 0)));
	}
}
