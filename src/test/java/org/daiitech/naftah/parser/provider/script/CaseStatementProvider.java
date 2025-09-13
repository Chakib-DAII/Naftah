package org.daiitech.naftah.parser.provider.script;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class CaseStatementProvider implements ArgumentsProvider {
	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
		return Stream
				.of(
					Arguments
							.of(
								true,
								"""
								متغير أ تعيين ١
								متغير ب تعيين 4
								متغير ت تعيين أ زائد ب

								اختر ت من بين
								15؛2؛3؛ت أكبر_من ١٠: {
								ارجع "أ زائد ب أكبر من 10"
								} "15"؛خاطئ؛ت أصغر_من ١٠؛5: {
								ارجع "أ زائد ب أصغر مين 10"
								} غير_ذلك {
								ارجع "أ زائد ب يساوي 10"
								}
								""",
								"أ زائد ب أصغر مين 10",
								null));
	}
}
