package org.daiitech.naftah.parser.provider.script;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class TryStatementProvider implements ArgumentsProvider {
	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
		return Stream
				.of(
					Arguments
							.of(
								true,
								"""
								حاول(1){
								نجاح(أ) -> {
								إطبع(أ)
								ارجع أ
								}
								فشل(ب) إفعل إطبع(ب)
								}
								""",
								1,
								null),
					Arguments
							.of(
								true,
								"""
								حاول(1 قسمة 0){
								نجاح(أ) -> إطبع(أ)
								فشل(ب) إفعل إلى_نص(ب)
								}
								""",
								"""
								💥 خطأ برمجي!
								استثناء غير ملتقط: خطأ رياضي، مثل القسمة على صفر. (/ by zero)""",
								null),
					Arguments
							.of(
								true,
								"""
								حاول(1 قسمة 0){
								نجاح(أ) -> إطبع(أ)
								فشل(ب) إفعل {
								إطبع(ب)
								حاول(1){
								نجاح(أ) -> إلى_نص(أ)
								فشل(ب) إفعل إطبع(ب)
								}
								}
								}
								""",
								"1",
								null)
				);
	}
}
