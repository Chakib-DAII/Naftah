package org.daiitech.naftah.parser.provider.script;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class IfStatementProvider implements ArgumentsProvider {
	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
		return Stream
				.of(Arguments
						.of(
							true,
							"""
							متغير أ تعيين ١
							متغير ب تعيين 4

							إذا أ أكبر_من ب إذن {
							أرجع أ
							} غير_ذلك_إذا أ أصغر_من ب إذن {
							أرجع ب
							} غير_ذلك {
							أرجع
							}
							أنهي
							""",
							4,
							null
						));
	}
}
