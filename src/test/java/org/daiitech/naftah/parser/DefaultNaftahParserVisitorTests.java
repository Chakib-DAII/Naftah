package org.daiitech.naftah.parser;

import static org.daiitech.naftah.parser.NaftahParserHelper.*;

import java.util.Map;
import java.util.Objects;
import org.daiitech.naftah.builtin.lang.DeclaredVariable;
import org.daiitech.naftah.builtin.utils.NumberUtils;
import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.parser.provider.ArithmeticExpressionsProvider;
import org.daiitech.naftah.parser.provider.LogicalExpressionsProvider;
import org.daiitech.naftah.parser.provider.ValueExpressionsProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

public class DefaultNaftahParserVisitorTests {

  @ParameterizedTest
  @ArgumentsSource(ValueExpressionsProvider.class)
  void valueExpressionTests(boolean validScript, String script, Object expectedValue, NaftahBugError expectedNaftahBugError) throws Exception {
    if (validScript) {
      var result = runScript(script);
      assertEquals(result, expectedValue);
    } else {
      doAssertBugEquals(script, expectedNaftahBugError);
    }
  }

  @ParameterizedTest
  @ArgumentsSource(LogicalExpressionsProvider.class)
  void logicalExpressionTests(boolean validScript, String script, Object expectedValue, NaftahBugError expectedNaftahBugError) throws Exception {
    if (validScript) {
      var result = runScript(script);
      assertEquals(result, expectedValue);
    } else {
      doAssertBugEquals(script, expectedNaftahBugError);
    }
  }

  @ParameterizedTest
  @ArgumentsSource(ArithmeticExpressionsProvider.class)
  void arithmeticExpressionTests(boolean validScript, String script, Object expectedValue, NaftahBugError expectedNaftahBugError) throws Exception {
    if (validScript) {
      var result = runScript(script);
      assertEquals(result, expectedValue);
    } else {
      doAssertBugEquals(script, expectedNaftahBugError);
    }
  }

  private Object runScript(String script) throws Exception {
    var input = getCharStream(false, script);

    var parser = prepareRun(input, NaftahErrorListener.INSTANCE);

    return doRun(parser);
  }

  private void assertEquals(Object result, Object expectedValue) {
    if (result instanceof Map<?, ?> map) {
      var expectedValueMap = (Map<?, ?>) expectedValue;
      map.forEach(
          (key, value) -> {
            var expectedValueFromMap = expectedValueMap.get(key);
            if (value instanceof DeclaredVariable declaredVariable) {
              doAssertEquals(expectedValueFromMap, declaredVariable.getValue());
            } else doAssertEquals(expectedValueFromMap, value);
          });
    } else if (result instanceof DeclaredVariable declaredVariable) {
      doAssertEquals(expectedValue, declaredVariable.getValue());
    } else doAssertEquals(result, expectedValue);
  }

  private void doAssertEquals(Object result, Object expectedValue) {
    if (result instanceof Number) Assertions.assertTrue(NumberUtils.equals(expectedValue, result));
    else Assertions.assertEquals(expectedValue, result);
  }
  private void doAssertBugEquals(String script, NaftahBugError expectedNaftahBugError) {
    NaftahBugError naftahBugError = Assertions.assertThrows(NaftahBugError.class, () -> runScript(script));
    Assertions.assertEquals(expectedNaftahBugError.getCause(), naftahBugError.getCause());
    Assertions.assertEquals(expectedNaftahBugError.getBugText(), naftahBugError.getBugText());
  }
}
