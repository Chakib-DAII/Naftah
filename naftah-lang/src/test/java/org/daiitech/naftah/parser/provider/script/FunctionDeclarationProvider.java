// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

package org.daiitech.naftah.parser.provider.script;

import java.math.BigInteger;
import java.util.stream.Stream;

import org.daiitech.naftah.errors.NaftahBugError;
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
								String.format(factorialWithNestedPrintFunction, "دالة_المضروب(50)"),
								new BigInteger(
												"30414093201713378043612608166064768844377641568960512000000000000"),
								null),
					Arguments
							.of(false,
								"""
								دالة دالة_ترجع_تركيبة() : عدد_طويل {
								ارجع ٣٢ ، ٤٥
								}

								دالة دالة_ترجع_تركيبة() : عدد_طويل {
								ارجع ٣٢ ، ٤٥
								}

								متغير أ؛ب تعيين دالة_ترجع_تركيبة()
								(أ , ب)
								""",
								null,
								new NaftahBugError( "الدالة 'دالة_ترجع_تركيبة' موجودة في السياق الحالي. لا يمكن إعادة إعلانها.",
													5,
													0)),
					Arguments
							.of(false,
								"""
								دالة دالة_المضروب(عدد_مضروب تعيين 10 , عدد_مضروب تعيين 10) : عدد_طويل {
								إذا عدد_مضروب أصغر_أو_يساوي ١ إذن {
								ارجع 1
								}
								أنهي

								ارجع عدد_مضروب ضارب دالة_المضروب(عدد_مضروب ناقص 1)
								}
								دالة_المضروب(20)
								""",
								null,
								new NaftahBugError( "المعامل 'عدد_مضروب' موجود في السياق الحالي للدالة. لا يمكن إعادة إعلانه.",
													1,
													0)),
					Arguments
							.of(false,
								"""
								دالة دالة_المضروب(عدد_مضروب تعيين 10 , عدد_مضروب_1 تعيين 10) : عدد_طويل {
								إذا عدد_مضروب أصغر_أو_يساوي ١ إذن {
								ارجع 1
								}
								أنهي

								ارجع عدد_مضروب ضارب دالة_المضروب(عدد_مضروب ناقص 1)
								}
								دالة_المضروب(عدد_مضروب تعيين 20 , عدد_مضروب تعيين 20)
								""",
								null,
								new NaftahBugError( "الوسيط 'عدد_مضروب' موجود في السياق الحالي للدالة. لا يمكن إعادة إعلانه.",
													9,
													0))
				);
	}
}
