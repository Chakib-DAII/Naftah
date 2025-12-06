package org.daiitech.naftah.parser.provider.script;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.daiitech.naftah.builtin.lang.NaftahObject;
import org.daiitech.naftah.builtin.lang.None;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class ImportStatementProvider implements ArgumentsProvider {
	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
		return Stream
				.of(Arguments
						.of(
							true,
							"""
							إجلب إطبع تحت_إسم إطبعلي
							إطبعلي("مرحباً أيها العالم!")
							""",
							None.get(),
							null
						),
					Arguments
							.of(
								true,
								"""
								إجلب إجمع تحت_إسم إجمعلي
								إجمعلي(127 ، 1)
								""",
								128,
								null
							),
					Arguments
							.of(
								true,
								"""
								إجلب إضرب تحت_إسم إضربلي
								إضربلي(127 ، 1)
								""",
								127,
								null
							),
					Arguments
							.of(
								true,
								"""
								إجلب دوال:الحزم::و_بتي تحت_إسم و_بتي
								و_بتي([1 , 2], [3 , 4])
								""",
								List.of(1, 0),
								null
							),
					Arguments
							.of(
								true,
								"""
								إجلب دوال:الحزم تحت_إسم حزم
								حزم::و_بتي([1 , 2], [3 , 4])
								""",
								List.of(1, 0),
								null
							),
					Arguments
							.of(
								true,
								"""
								إجلب دوال:الحزم::[و_بتي , عكس_الإشارة  تحت_إسم عكس]
								[
								و_بتي([1 , 2], [3 , 4]) ،
								عكس((1 , 2)) ،
								]
								""",
								List
										.of(
											List.of(1, 0),
											List.of(-1, -2)
										),
								null
							),
					Arguments
							.of(
								true,
								"""
								إجلب جافا:أدة:اختياري::[وف تحت_إسم من_خلال, گت مثل خذ , أو_لس تحت_إسم أو_خذ , وف_فارغابل تحت_إسم من_فارغ]
								[
								من_خلال(10):::خذ() ،
								من_فارغ(فارغ):::أو_خذ("لغة نفطه") ،
								]
								""",

								List
										.of(

											NaftahObject.of(10),
											NaftahObject.of("لغة نفطه")
										),
								null
							),
					Arguments
							.of(
								true,
								"""
								إجلب جافا:أدة:اختياري::وف تحت_إسم من_خلال
								من_خلال(10):::گت()
								""",

								NaftahObject.of(10),
								null
							),
					Arguments
							.of(
								true,
								"""
								إجلب جافا:لغة:سلسلة تحت_إسم سلسلة
								إجلب جافا:لغة:سلسلة::[لنگتهاي تحت_إسم الطول , النفقة مثل سلسلة_فرعية ، إلى_سلسلة]
								[
								@سلسلة("مرحبا"):::الطول() ،
								@سلسلة("مرحبا"):::سلسلة_فرعية(1؛3) ،
								@سلسلة("مرحبا"):::إلى_سلسلة() ،
								]
								""",
								List
										.of(
											NaftahObject.of(5),
											NaftahObject.of("رح"),
											NaftahObject.of("مرحبا")

										),
								null
							),
					Arguments
							.of(
								true,
								"""
								إجلب جافا:أدة:[صفائف_القائمة تحت_إسم قائمة , صفائف_القائمة::إضافة مثل إضافة ، صفائف_القائمة::گت تحت_إسم خذ ، صفائف_القائمة::الحجم تحت_إسم الحجم]
								[
								@قائمة([1؛100؛0])::إضافة(100) ،
								@قائمة([1؛100؛0])::خذ(0) ،
								@قائمة([1؛100؛0])::الحجم() ،
								]
								""",

								List
										.of(
											NaftahObject.of(Boolean.TRUE),
											NaftahObject.of(1),
											NaftahObject.of(3)
										),
								null
							),
					Arguments
							.of(
								true,
								"""
								إجلب جافا:لغة:كائن تحت_إسم كائن
								@كائن() لا_يساوي فارغ
								""",
								true,
								null
							),
					Arguments
							.of(
								true,
								"""
								إجلب جافا:أدة:معرف_مستخدم تحت_إسم معرف_مستخدم

								ثابت معرف_مستخدم : معرف_مستخدم تعيين @معرف_مستخدم(6161415689025233999؛5488940234982179551)
								معرف_مستخدم
								""",
								NaftahObject.of(new UUID(6161415689025233999L, 5488940234982179551L)),
								null
							)
				);
	}
}
