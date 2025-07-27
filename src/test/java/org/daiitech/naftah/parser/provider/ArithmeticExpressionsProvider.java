package org.daiitech.naftah.parser.provider;

import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class ArithmeticExpressionsProvider implements ArgumentsProvider {
  @Override
  public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
    return Stream.of(
        Arguments.of(true,"2 + 2", 4, null),
        Arguments.of(true,"2 زائد 2", 4, null),
        Arguments.of(true,"2 + ٢", 4, null),
        Arguments.of(true,"2 زائد ٢", 4, null),
        Arguments.of(true,"٢ + 2", 4, null),
        Arguments.of(true,"٢ زائد 2", 4, null),
        Arguments.of(true,"2 - 2", 0, null),
        Arguments.of(true,"2 ناقص 2", 0, null),
        Arguments.of(true,"2 - ٢", 0, null),
        Arguments.of(true,"2 ناقص ٢", 0, null),
        Arguments.of(true,"٢ - 2", 0, null),
        Arguments.of(true,"٢ ناقص 2", 0, null),
        Arguments.of(true,"2 * 2", 4, null),
        Arguments.of(true,"2 ضارب 2", 4, null),
        Arguments.of(true,"2 * ٢", 4, null),
        Arguments.of(true,"2 ضارب ٢", 4, null),
        Arguments.of(true,"٢ * 2", 4, null),
        Arguments.of(true,"٢ ضارب 2", 4, null),
        Arguments.of(true,"2 / 2", 1, null),
        Arguments.of(true,"2 قسمة 2", 1, null),
        Arguments.of(true,"2 / ٢", 1, null),
        Arguments.of(true,"2 قسمة ٢", 1, null),
        Arguments.of(true,"٢ / 2", 1, null),
        Arguments.of(true,"٢ قسمة 2", 1, null),
        Arguments.of(true,"2 % 2", 0, null),
        Arguments.of(true,"2 باقي 2", 0, null),
        Arguments.of(true,"2 % ٢", 0, null),
        Arguments.of(true,"2 باقي ٢", 0, null),
        Arguments.of(true,"٢ % 2", 0, null),
        Arguments.of(true,"٢ باقي 2", 0, null),
        //        Arguments.of(true,"++2", 2, null),
        Arguments.of(true,"2 زد", 2, null),
        //        Arguments.of(true,"++٢", 2, null),
        Arguments.of(true,"٢ زد", 2, null),
        Arguments.of(true,"2++", 2, null), // TODO: check
        Arguments.of(true,"زد 2", 3, null),
        Arguments.of(true,"٢++", 2, null), // TODO: check
        Arguments.of(true,"زد ٢", 3, null),
        //        Arguments.of(true,"--2", 2, null), // TODO: check
        Arguments.of(true,"2 نقص", 2, null),
        //        Arguments.of(true,"--٢", 2, null), // TODO: check
        Arguments.of(true,"٢ نقص", 2, null),
        Arguments.of(true,"2--", 2, null),
        Arguments.of(true,"نقص 2", 1, null), // TODO: check
        Arguments.of(true,"٢--", 2, null), // TODO: check
        Arguments.of(true,"نقص ٢", 1, null));
  }
}
