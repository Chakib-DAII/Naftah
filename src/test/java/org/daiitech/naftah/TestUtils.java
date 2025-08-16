package org.daiitech.naftah;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.daiitech.naftah.builtin.lang.DeclaredVariable;
import org.daiitech.naftah.builtin.utils.NumberUtils;
import org.daiitech.naftah.errors.ExceptionUtils;
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
			doAssertEquals(map, expectedValueMap);
		}
		else if (result instanceof Collection<?> collection) {
			var expectedValueCollection = (Collection<?>) expectedValue;
			doAssertEquals(collection, expectedValueCollection);
		}
		else if (result instanceof DeclaredVariable declaredVariable) {
			assertEquals(expectedValue, declaredVariable.getValue());
		}
		else {
			doAssertEquals(result, expectedValue);
		}
	}

	public static void doAssertEquals(Map<?, ?> map, Map<?, ?> expectedMap) {
		map.forEach((key, value) -> {
			var expectedValueFromMap = expectedMap.get(key);

			if (value instanceof Map<?, ?> internalMap && expectedValueFromMap instanceof Map<?, ?> internalExpectedMap) {
				doAssertEquals(internalMap, internalExpectedMap);
			}
			else if (value instanceof DeclaredVariable declaredVariable) {
				assertEquals(expectedValueFromMap, declaredVariable.getValue());
			}
			else {
				assertEquals(expectedValueFromMap, value);
			}
		});
	}

	public static void doAssertEquals(Collection<?> collection, Collection<?> expectedCollection) {
		Iterator<?> collectionIterator = collection.iterator();
		Iterator<?> expectedValueCollectionIterator = expectedCollection.iterator();

		while (collectionIterator.hasNext() && expectedValueCollectionIterator.hasNext()) {
			var elementResult = collectionIterator.next();
			var elementExpectedValue = expectedValueCollectionIterator.next();

			if (elementResult instanceof Collection<?> internalCollection && elementExpectedValue instanceof Collection<?> internalExpectedCollection) {
				doAssertEquals(internalCollection, internalExpectedCollection);
			}
			else {
				assertEquals(elementResult, elementExpectedValue);
			}
		}
	}

	public static void doAssertEquals(Object result, Object expectedValue) {
		if (result instanceof Number) {
			Assertions.assertTrue(NumberUtils.equals(expectedValue, result));
		}
		else {
			Assertions.assertEquals(expectedValue, result);
		}
	}

	public static void assertBugEquals(String script, NaftahBugError expectedNaftahBugError) {
		NaftahBugError naftahBugError = Assertions.assertThrows(NaftahBugError.class, () -> runScript(script));
		doAssertBugEquals(naftahBugError, expectedNaftahBugError);
	}

	public static void doAssertBugEquals(NaftahBugError thrownNaftahBugError, NaftahBugError expectedNaftahBugError) {
		var expected = ExceptionUtils.getMostSpecificCause(expectedNaftahBugError);
		var actual = ExceptionUtils.getMostSpecificCause(thrownNaftahBugError);
		Assertions.assertEquals(expected.getClass(), actual.getClass());
		Assertions.assertEquals(expected.getMessage(), actual.getMessage());
	}
}
