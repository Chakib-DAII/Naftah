package org.daiitech.naftah.parser.provider;

import java.math.BigInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class FunctionDeclarationProvider implements ArgumentsProvider {
	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
		String factorial = """
							دالة دالة_المضروب(عدد_مضروب تعيين 10) {
							إذا عدد_مضروب أصغر_أو_يساوي ١ إذن {
							ارجع 1
							}
							أنهي

							ارجع عدد_مضروب ضارب دالة_المضروب(عدد_مضروب ناقص 1)
							}
							%s
							""";
		return Stream
				.of(
					Arguments.of(true, String.format(factorial, "دالة_المضروب(1)"), 1, null),
					Arguments.of(true, String.format(factorial, "دالة_المضروب(5)"), 120, null),
					Arguments.of(true, String.format(factorial, "دالة_المضروب()"), 3628800, null),
					Arguments.of(true, String.format(factorial, "دالة_المضروب(15)"), 1307674368000L, null),
					Arguments.of(true, String.format(factorial, "دالة_المضروب(20)"), 2432902008176640000L, null),
					Arguments
							.of(true,
								String.format(factorial, "دالة_المضروب(30)"),
								new BigInteger("265252859812191058636308480000000"),
								null)
				);
	}
}
