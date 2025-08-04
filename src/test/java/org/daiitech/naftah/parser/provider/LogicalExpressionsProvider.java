package org.daiitech.naftah.parser.provider;

import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class LogicalExpressionsProvider implements ArgumentsProvider {
  @Override
  public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
    return Stream.of(
        Arguments.of(true, "ليس صحيح", false, null),
        Arguments.of(true, "ليس صائب", false, null),
        Arguments.of(true, "ليس حقيقي", false, null),
        Arguments.of(true, "ليس خطأ", true, null),
        Arguments.of(true, "ليس خاطئ", true, null),
        Arguments.of(true, "ليس زائف", true, null),
        Arguments.of(true, "ليس فارغ", true, null),
        Arguments.of(true, "ليس باطل", true, null),
        Arguments.of(true, "ليس لاشيء", true, null),
        Arguments.of(true, "ليس 'ص'", false, null),
        Arguments.of(true, "ليس «»", true, null),
        Arguments.of(true, "ليس «اسم»", false, null),
        Arguments.of(true, "ليس 2", -2, null),
        Arguments.of(true, "2 و 2", true, null),
        Arguments.of(true, "2 أيضا 2", true, null),
        Arguments.of(true, "2 ايضا ٢", true, null),
        Arguments.of(true, "2 أيضا ٢", true, null),
        Arguments.of(true, "2 او 2", true, null),
        Arguments.of(true, "2 أو 2", true, null),
        Arguments.of(true, "2 وإلا ٢", true, null),
        Arguments.of(true, "2 والا ٢", true, null),
        Arguments.of(true, "2 ولا 2", true, null),
        Arguments.of(true, "2- أو 2", true, null),
        Arguments.of(true, "2 أو 2-", true, null),
        Arguments.of(true, "2- وإلا ٢", true, null),
        Arguments.of(true, "2 والا ٢-", true, null),
        Arguments.of(true, "2- ولا 2", true, null),
        Arguments.of(true, "2- او 2", true, null),
        Arguments.of(true, "2 أو 2-", true, null),
        Arguments.of(true, "2- وإلا 2", true, null),
        Arguments.of(true, "2 والا 2-", true, null),
        Arguments.of(true, "2- ولا 2", true, null),
        Arguments.of(true, "2 او 2-", true, null),
        Arguments.of(true, "2- أو 2", true, null),
        Arguments.of(true, "2- وإلا 2", true, null),
        Arguments.of(true, "2 والا 2-", true, null),
        Arguments.of(true, "2 ولا 2-", true, null),
        Arguments.of(true, "2 > 2", false, null),
        Arguments.of(true, "2 أصغر_من 2", false, null),
        Arguments.of(true, "2 => 2", true, null),
        Arguments.of(true, "2 أصغر_أو_يساوي 2", true, null),
        Arguments.of(true, "2 >= 2", true, null),
        Arguments.of(true, "2 أكبر_أو_يساوي 2", true, null),
        Arguments.of(true, "2 =! 2", false, null),
        Arguments.of(true, "2 لا_يساوي 2", false, null),
        Arguments.of(true, "2 == 2", true, null),
        Arguments.of(true, "2 يساوي 2", true, null),
        Arguments.of(true, "2 > 3", true, null),
        Arguments.of(true, "2 أصغر_من 3", true, null));
  }
}
