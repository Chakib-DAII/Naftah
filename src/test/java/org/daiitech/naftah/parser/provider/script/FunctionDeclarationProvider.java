package org.daiitech.naftah.parser.provider.script;

import java.math.BigInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class FunctionDeclarationProvider implements ArgumentsProvider {
	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
		String factorial = """
							دالة دالة_المضروب(عدد_مضروب تعيين 10) : عدد_طويل {
							إذا عدد_مضروب أصغر_أو_يساوي ١ إذن {
							ارجع 1
							}
							أنهي

							ارجع عدد_مضروب ضارب دالة_المضروب(عدد_مضروب ناقص 1)
							}
							%s
							""";
		String factorialWithNestedPrintFunction = """
													دالة دالة_المضروب(عدد_مضروب تعيين 10) : عدد_طويل {
													دالة دالة_المضروب_إطبع(عدد_مضروب) : عدم {
														إطبع(عدد_مضروب)
													}
													دالة_المضروب_إطبع(عدد_مضروب)
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
								null),
					Arguments
							.of(true,
								String.format(factorialWithNestedPrintFunction, "دالة_المضروب(100)"),
								new BigInteger(
												"93326215443944152681699238856266700490715968264381621468592963895217599993229915608941463976156518286253697920827223758251185210916864000000000000000000000000"),
								null)
				);
	}
}
