package org.daiitech.naftah.parser.provider.script;

import java.util.stream.Stream;

import org.daiitech.naftah.errors.NaftahBugError;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import static org.daiitech.naftah.builtin.utils.CollectionUtils.newNaftahIndexOutOfBoundsBugError;

public class CollectionAccessProvider implements ArgumentsProvider {
	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
		return Stream
				.of(
					Arguments.of(true, """
										متغير قائمة تعيين [٨٥، ٩٠، ٧٨، ٩٢]
										قائمة[0]
										""", 85, null),
					Arguments.of(true, """
										متغير تركيبة تعيين (٣٢، ٤٥)
										تركيبة[1]
										""", 45, null),
					Arguments.of(true, """
										متغير مجموعة تعيين {١، ٢، ٣، ٤}
										مجموعة[2]
										""", 3, null),
					Arguments
							.of(false,
								"""
								متغير مصفوفة_ترابطية تعيين
								{
									"الاسم": "سارة",
									"العناوين": ["المنزل", "العمل"],
									"الأرقام": (١٠٠١، ٢٠٠٢)
								}
								مصفوفة_ترابطية[1]
								""",
								null,
								new NaftahBugError(
													"""
													لا يمكن استخدام الفهرسة إلا مع الأنواع التالية: تركيبة , قائمة أو مجموعة.
													""",
													7,
													0)),
					Arguments.of(true, """
										متغير قائمة تعيين [١٠ ، [ ٩، [ ٨ ، [ ٧ ، [٦ ، [ ٥ ، [ ٤ ، [٣ ، [٢ ، ١]]]]]]]]]
										قائمة[1][1][1][1][0]
										""", 6, null),
					Arguments.of(true, """
										متغير تركيبة تعيين (١٠، (٩، (٨، (٧، (٦، (٥، (٤، (٣، (٢، ١)))))))))
										تركيبة[1][1][1][1][1][0]
										""", 5, null),
					Arguments.of(true, """
										متغير مجموعة تعيين {١، ٢،  {١، ٢، ٣، ٤}، ٤}
										مجموعة[3][2]
										""", 3, null),
					Arguments
							.of(false,
								"""
								متغير مجموعة تعيين {١،  {١، ٢، ٣، ٤} ، ٣، ٤}
								مجموعة[1][2]
								""",
								null,
								new NaftahBugError(
													"""
													لا يمكن استخدام الفهرسة إلا مع الأنواع التالية: تركيبة , قائمة أو مجموعة.
													""",
													2,
													0)),
					Arguments
							.of(false,
								"""
								متغير قائمة تعيين [٨٥، ٩٠، ٧٨، ٩٢]
								قائمة[4]
								""",
								null,
								newNaftahIndexOutOfBoundsBugError(  4,
																	4,
																	new IndexOutOfBoundsException(
																									"Index 4 out of bounds for length 4"))
							),
					Arguments
							.of(false,
								"""
								متغير مجموعة تعيين {١، ٢، ٣، ٤}
								مجموعة[4]
								""",
								null,
								new NaftahBugError(String
										.format("""
												المؤشر المطلوب (%d) خارج حدود المجموعة. عدد العناصر الحالية هو %d.
												""", 4, 4))));
	}
}
