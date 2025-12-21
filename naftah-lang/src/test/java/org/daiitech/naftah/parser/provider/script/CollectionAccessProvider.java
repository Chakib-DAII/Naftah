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
										متغير قائمة_ تعيين [٨٥، ٩٠، ٧٨، ٩٢]
										قائمة_[0]
										""", 85, null),
					Arguments.of(true, """
										متغير تركيبة_ تعيين (٣٢، ٤٥)
										تركيبة_[1]
										""", 45, null),
					Arguments.of(true, """
										متغير تركيبة_:زوج<:أي_عدد,أي_عدد:> تعيين (٣٢، ٤٥)
										تركيبة_[1]
										""", 45, null),
					Arguments.of(true, """
										متغير تركيبة_:زوج<:عدد_طويل,عدد_طويل:> تعيين (٣٢، ٤٥)
										تركيبة_[1]
										""", 45, null),
					Arguments.of(true, """
										متغير تركيبة_ تعيين (٣٢، ٤٥، ٤٥)
										تركيبة_[1]
										""", 45, null),
					Arguments.of(true, """
										متغير تركيبة_:ثلاثي_القيم<:أي_عدد,أي_عدد,أي_عدد:> تعيين (٣٢، ٤٥، ٤٥)
										تركيبة_[1]
										""", 45, null),
					Arguments.of(true, """
										متغير تركيبة_:ثلاثي_القيم<:عدد_طويل,عدد_طويل,عدد_طويل:> تعيين (٣٢، ٤٥، ٤٥)
										تركيبة_[1]
										""", 45, null),
					Arguments.of(true, """
										متغير مجموعة_ تعيين {١، ٢، ٣، ٤}
										مجموعة_[2]
										""", 3, null),
					Arguments
							.of(false,
								"""
								متغير مصفوفة_ترابطية_ تعيين
								{
									"الاسم": "سارة",
									"العناوين": ["المنزل", "العمل"],
									"الأرقام": (١٠٠١، ٢٠٠٢)
								}
								مصفوفة_ترابطية_[1]
								""",
								null,
								new NaftahBugError(
													"""
													لا يمكن استخدام الفهرسة إلا مع الأنواع التالية: تركيبة , قائمة أو مجموعة.
													""",
													7,
													0)),
					Arguments
							.of(false,
								"""
								متغير مصفوفة_ترابطية_:مصفوفة_ترابطية<:تسلسل_أحرف,أي_نوع:> تعيين
								{
									"الاسم": "سارة",
									"العناوين": ["المنزل", "العمل"],
									"الأرقام": (١٠٠١، ٢٠٠٢)
								}
								مصفوفة_ترابطية_[1]
								""",
								null,
								new NaftahBugError(
													"""
													لا يمكن استخدام الفهرسة إلا مع الأنواع التالية: تركيبة , قائمة أو مجموعة.
													""",
													7,
													0)),
					Arguments.of(true, """
										متغير قائمة_ تعيين [١٠ ، [ ٩، [ ٨ ، [ ٧ ، [٦ ، [ ٥ ، [ ٤ ، [٣ ، [٢ ، ١]]]]]]]]]
										قائمة_[1][1][1][1][0]
										""", 6, null),
					Arguments.of(true, """
										متغير تركيبة_ تعيين (١٠، (٩، (٨، (٧، (٦، (٥، (٤، (٣، (٢، ١)))))))))
										تركيبة_[1][1][1][1][1][0]
										""", 5, null),
					Arguments
							.of(true,
								"""
								متغير تركيبة_: زوج<:عدد_طويل,أي_نوع:> تعيين (١٠، (٩، (٨، (٧، (٦، (٥، (٤، (٣، (٢، ١)))))))))
								تركيبة_[1][1][1][1][1][0]
								""",
								5,
								null),
					Arguments
							.of(true,
								"""
								متغير تركيبة_: تركيبة تعيين (١٠، (٩، (٨، (٧، (٦، (٥، (٤، (٣، (٢، ١)))))))))
								تركيبة_[1][1][1][1][1][0]
								""",
								5,
								null),
					Arguments
							.of(true,
								"""
								متغير تركيبة_: زوج<:عدد_طويل,زوج<:أي_نوع,أي_نوع:>:> تعيين (١٠، (٩، (٨، (٧، (٦، (٥، (٤، (٣، (٢، ١)))))))))
								تركيبة_[1][1][1][1][1][0]
								""",
								5,
								null),
					Arguments.of(true, """
										متغير مجموعة_ تعيين {١، ٢،  {١، ٢، ٣، ٤}، ٤}
										مجموعة_[3][2]
										""", 3, null),
					Arguments.of(true, """
										متغير مجموعة_: مجموعة<:أي_عدد:> تعيين {١، ٢، ٣، ٤}
										مجموعة_[2]
										""", 3, null),
					Arguments.of(true, """
										متغير مجموعة_: مجموعة<:عدد_طويل:> تعيين {١، ٢، ٣، ٤}
										مجموعة_[2]
										""", 3, null),
					Arguments
							.of(false,
								"""
								متغير مجموعة_ تعيين {١،  {١، ٢، ٣، ٤} ، ٣، ٤}
								مجموعة_[1][2]
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
								متغير قائمة_ تعيين [٨٥، ٩٠، ٧٨، ٩٢]
								قائمة_[4]
								""",
								null,
								newNaftahIndexOutOfBoundsBugError(  4,
																	4,
																	new IndexOutOfBoundsException(
																									"Index 4 out of bounds for length 4"),
																	-1,
																	-1)
							),
					Arguments
							.of(false,
								"""
								متغير مجموعة_ تعيين {١، ٢، ٣، ٤}
								مجموعة_[4]
								""",
								null,
								new NaftahBugError(String
										.format("""
												المؤشر المطلوب (%d) خارج حدود المجموعة. عدد العناصر الحالية هو %d.
												""", 4, 4))));
	}
}
