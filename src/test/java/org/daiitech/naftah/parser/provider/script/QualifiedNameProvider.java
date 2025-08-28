package org.daiitech.naftah.parser.provider.script;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class QualifiedNameProvider implements ArgumentsProvider {
	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
		return Stream
				.of(
					Arguments.of(true, """
										ثابت ت : جڤ:لنگ:لنگ تعيين 2
										ت
										""", 2, null),
//				Arguments.of(true, "جڤ:لنگ:سيستم::گك()", null, null), TODO: fix
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
										""", 400, null)
				);
	}
}
