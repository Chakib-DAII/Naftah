package org.daiitech.naftah.parser;

import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.parser.provider.StringInterpolatorProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static org.daiitech.naftah.TestUtils.assertEquals;
import static org.daiitech.naftah.TestUtils.doAssertBugEquals;

class StringInterpolatorTests {
	@ParameterizedTest
	@ArgumentsSource(StringInterpolatorProvider.class)
	void stringInterpolatorTests(   boolean valid,
									String template,
									Object context,
									String expectedValue,
									NaftahBugError expectedNaftahBugError) throws Exception {
		if (valid) {
			var result = StringInterpolator.process(template, context);
			assertEquals(result, expectedValue);
		}
		else {
			NaftahBugError naftahBugError = Assertions
					.assertThrows(  NaftahBugError.class,
									() -> StringInterpolator
											.process(   template,
														context));
			doAssertBugEquals(naftahBugError, expectedNaftahBugError);
		}
	}
}
