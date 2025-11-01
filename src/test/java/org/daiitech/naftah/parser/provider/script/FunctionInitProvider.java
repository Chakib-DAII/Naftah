package org.daiitech.naftah.parser.provider.script;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahInvocableNotFoundError;


public class FunctionInitProvider implements ArgumentsProvider {
	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
		return Stream
				.of(Arguments
						.of(false,
							"""
							جافا:لغة:كائن()
							""",
							null,
							newNaftahInvocableNotFoundError("جافا:لغة:كائن",
															1,
															0)));
	}
}
