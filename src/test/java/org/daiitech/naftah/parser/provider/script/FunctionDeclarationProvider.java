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
							دالة دالة_المضروب(عدد_مضروب تعيين 10) {
							إذا عدد_مضروب أصغر_أو_يساوي ١ إذن {
							ارجع 1
							}
							أنهي

							ارجع عدد_مضروب ضارب دالة_المضروب(عدد_مضروب ناقص 1)
							}
							%s
							""";
		String factorialWithNestedPrintFunction = """
													دالة دالة_المضروب(عدد_مضروب تعيين 10) {
													دالة دالة_المضروب_إطبع(عدد_مضروب) {
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
								String.format(factorialWithNestedPrintFunction, "دالة_المضروب(260)"),
								new BigInteger(
												"3830195860836169235117497985604491875279556752309096960191300817480651475135399533485285838275429773913773383359294010103333339344249624060099745511339849626153802980398232848965472622820196848860832049579523313702327662760125732592551956622024712475139889122106940319324041688318583612166708334763727216738353107304842707002261430265483385206376839110078156900663427220806900528365808580136352143713956803295894115605151395493267411709188354023557693440000000000000000000000000000000000000000000000000000000000000000"),
								null)
				);
	}
}
