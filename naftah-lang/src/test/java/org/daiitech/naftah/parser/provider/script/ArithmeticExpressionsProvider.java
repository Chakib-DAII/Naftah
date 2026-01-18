// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

package org.daiitech.naftah.parser.provider.script;

import java.math.BigInteger;
import java.util.stream.Stream;

import org.daiitech.naftah.builtin.lang.NaN;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class ArithmeticExpressionsProvider implements ArgumentsProvider {
	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
		return Stream
				.of(Arguments.of(true, "2 + 2", 4, null),
					Arguments.of(true, "2 زائد 2", 4, null),
					Arguments.of(true, "2 + ٢", 4, null),
					Arguments.of(true, "2 زائد ٢", 4, null),
					Arguments.of(true, "٢ + 2", 4, null),
					Arguments.of(true, "٢ زائد 2", 4, null),
					Arguments.of(true, "2 - 2", 0, null),
					Arguments.of(true, "2 ناقص 2", 0, null),
					Arguments.of(true, "2 - ٢", 0, null),
					Arguments.of(true, "2 ناقص ٢", 0, null),
					Arguments.of(true, "٢ - 2", 0, null),
					Arguments.of(true, "٢ ناقص 2", 0, null),
					Arguments.of(true, "2 * 2", 4, null),
					Arguments.of(true, "2 ضارب 2", 4, null),
					Arguments.of(true, "2 * ٢", 4, null),
					Arguments.of(true, "2 ضارب ٢", 4, null),
					Arguments.of(true, "٢ * 2", 4, null),
					Arguments.of(true, "٢ ضارب 2", 4, null),
					Arguments.of(true, "2 / 2", 1, null),
					Arguments.of(true, "2 قسمة 2", 1, null),
					Arguments.of(true, "2 / ٢", 1, null),
					Arguments.of(true, "2 قسمة ٢", 1, null),
					Arguments.of(true, "٢ / 2", 1, null),
					Arguments.of(true, "٢ قسمة 2", 1, null),
					Arguments.of(true, "2 % 2", 0, null),
					Arguments.of(true, "2 باقي 2", 0, null),
					Arguments.of(true, "2 % ٢", 0, null),
					Arguments.of(true, "2 باقي ٢", 0, null),
					Arguments.of(true, "٢ % 2", 0, null),
					Arguments.of(true, "٢ باقي 2", 0, null),
					Arguments.of(true, "++2", 3, null),
					Arguments.of(true, "2 زد", 2, null),
					Arguments.of(true, "++٢", 3, null),
					Arguments.of(true, "٢ زد", 2, null),
					Arguments.of(true, "2++", 2, null),
					Arguments.of(true, "زد 2", 3, null),
					Arguments.of(true, "٢++", 2, null),
					Arguments.of(true, "زد ٢", 3, null),
					Arguments.of(true, "--2", 1, null),
					Arguments.of(true, "2 نقص", 2, null),
					Arguments.of(true, "--٢", 1, null),
					Arguments.of(true, "٢ نقص", 2, null),
					Arguments.of(true, "2--", 2, null),
					Arguments.of(true, "نقص 2", 1, null),
					Arguments.of(true, "٢--", 2, null),
					Arguments.of(true, "نقص ٢", 1, null),
					Arguments.of(true, "نقص 127", 126, null),
					Arguments.of(true, "نقص 32767", 32766, null),
					Arguments.of(true, "نقص 2147483647", 2147483646, null),
					Arguments.of(true, "نقص 9223372036854775807", 9223372036854775806L, null),
					Arguments.of(true, "127 نقص", 127, null),
					Arguments.of(true, "32767 نقص", 32767, null),
					Arguments.of(true, "2147483647 نقص", 2147483647, null),
					Arguments.of(true, "9223372036854775807 نقص", 9223372036854775807L, null),
					Arguments.of(true, "زد 127", 128, null),
					Arguments.of(true, "زد 32767", 32768, null),
					Arguments.of(true, "زد 2147483647", 2147483648L, null),
					Arguments.of(true, "زد 9223372036854775807", new BigInteger("9223372036854775808"), null),
					Arguments.of(true, "127 زد", 127, null),
					Arguments.of(true, "32767 زد", 32767, null),
					Arguments.of(true, "2147483647 زد", 2147483647, null),
					Arguments.of(true, "9223372036854775807 زد", 9223372036854775807L, null),
					Arguments.of(true, "2 ** 3 ** 2", 512, null),
					Arguments.of(true, "\"2\" ** 3", 8, null),
					Arguments.of(true, "\"4\" ** \"0.5\"", 2.0, null),
					Arguments.of(true, "\"10\" ** \"-1\"", 0.1, null),
					Arguments.of(true, "\"3.14\" ** 2", 9.859600658798229, null),
					Arguments.of(true, "\"\" ** 2", 0.0, null),
					Arguments.of(true, "\" \" ** 2", 0, null),
					Arguments.of(true, "' ' ** 2", 0, null),
					Arguments.of(true, "\"   \" ** 2", 0.0, null),
					Arguments.of(true, "\"مرحبا\" ** 2", NaN.get(), null),
					Arguments.of(true, "\"مرحبا12312\" ** 2", NaN.get(), null)
				);
	}
}
