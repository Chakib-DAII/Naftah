package org.daiitech.naftah.parser.provider.script;

import java.util.stream.Stream;

import org.daiitech.naftah.builtin.lang.None;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class RepeatStatementProvider implements ArgumentsProvider {
	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
		return Stream
				.of(Arguments
						.of(
							true,
							"""
							متغير أ تعيين ١

							كرر {
								إطبع(أ) أنهي
								زد أ
							}
							حتى أ أكبر_من 9
							أنهي
							""",
							None.get(),
							null));
	}
}
