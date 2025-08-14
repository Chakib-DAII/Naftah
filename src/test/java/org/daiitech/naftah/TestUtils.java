package org.daiitech.naftah;

import java.util.Map;

import org.daiitech.naftah.builtin.lang.DeclaredVariable;
import org.daiitech.naftah.builtin.lang.DynamicNumber;
import org.daiitech.naftah.builtin.utils.NumberUtils;
import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.parser.NaftahErrorListener;
import org.junit.jupiter.api.Assertions;

import static org.daiitech.naftah.parser.NaftahParserHelper.doRun;
import static org.daiitech.naftah.parser.NaftahParserHelper.getCharStream;
import static org.daiitech.naftah.parser.NaftahParserHelper.prepareRun;

public final class TestUtils {
    public static Object runScript(String script) throws Exception {
        var input = getCharStream(false, script);

        var parser = prepareRun(input, NaftahErrorListener.INSTANCE);

        return doRun(parser);
    }

    public static void assertEquals(Object result, Object expectedValue) {
        if (result instanceof Map<?, ?> map) {
            var expectedValueMap = (Map<?, ?>) expectedValue;
            map.forEach((key, value) -> {
                var expectedValueFromMap = expectedValueMap.get(key);
                if (value instanceof DeclaredVariable declaredVariable) {
                    doAssertEquals(expectedValueFromMap, declaredVariable.getValue());
                }
                else {
                    doAssertEquals(expectedValueFromMap, value);
                }
            });
        }
        else if (result instanceof DeclaredVariable declaredVariable) {
            doAssertEquals(expectedValue, declaredVariable.getValue());
        }
        else {
            doAssertEquals(result, expectedValue);
        }
    }

    public static void doAssertEquals(Object result, Object expectedValue) {
        if (result instanceof Number) {
            System.out.println(DynamicNumber.of(result) + "-" + DynamicNumber.of(expectedValue));
            Assertions.assertTrue(NumberUtils.equals(expectedValue, result));
        }
        else {
            Assertions.assertEquals(expectedValue, result);
        }
    }

    public static void doAssertBugEquals(String script, NaftahBugError expectedNaftahBugError) {
        NaftahBugError naftahBugError = Assertions.assertThrows(NaftahBugError.class, () -> runScript(script));
        Assertions.assertEquals(expectedNaftahBugError.getCause(), naftahBugError.getCause());
        Assertions.assertEquals(expectedNaftahBugError.getBugText(), naftahBugError.getBugText());
    }
}
