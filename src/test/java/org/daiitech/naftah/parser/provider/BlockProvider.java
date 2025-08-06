package org.daiitech.naftah.parser.provider;

import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class BlockProvider implements ArgumentsProvider {
  @Override
  public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
    return Stream.of(
        Arguments.of(true, "{ارجع 2}", 2, null),
        Arguments.of(true, "{أرجع 2}", 2, null),
        Arguments.of(true, "{اعد 2}", 2, null),
        Arguments.of(true, "{أعد 2}", 2, null));
  }
}
