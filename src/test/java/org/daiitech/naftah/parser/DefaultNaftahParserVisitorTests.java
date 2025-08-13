package org.daiitech.naftah.parser;

import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.parser.provider.ArithmeticExpressionsProvider;
import org.daiitech.naftah.parser.provider.AssignmentProvider;
import org.daiitech.naftah.parser.provider.BlockProvider;
import org.daiitech.naftah.parser.provider.DeclarationProvider;
import org.daiitech.naftah.parser.provider.FunctionDeclarationProvider;
import org.daiitech.naftah.parser.provider.LogicalExpressionsProvider;
import org.daiitech.naftah.parser.provider.ValueExpressionsProvider;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static org.daiitech.naftah.TestUtils.assertEquals;
import static org.daiitech.naftah.TestUtils.doAssertBugEquals;
import static org.daiitech.naftah.TestUtils.runScript;

public class DefaultNaftahParserVisitorTests {

	@ParameterizedTest
	@ArgumentsSource(ValueExpressionsProvider.class)
	void valueExpressionTests(boolean validScript, String script, Object expectedValue, NaftahBugError expectedNaftahBugError) throws Exception {
		if (validScript) {
			var result = runScript(script);
			assertEquals(result, expectedValue);
		}
		else {
			doAssertBugEquals(script, expectedNaftahBugError);
		}
	}

	@ParameterizedTest
	@ArgumentsSource(LogicalExpressionsProvider.class)
	void logicalExpressionTests(boolean validScript, String script, Object expectedValue, NaftahBugError expectedNaftahBugError) throws Exception {
		if (validScript) {
			var result = runScript(script);
			assertEquals(result, expectedValue);
		}
		else {
			doAssertBugEquals(script, expectedNaftahBugError);
		}
	}

	@ParameterizedTest
	@ArgumentsSource(ArithmeticExpressionsProvider.class)
	void arithmeticExpressionTests(boolean validScript, String script, Object expectedValue, NaftahBugError expectedNaftahBugError) throws Exception {
		if (validScript) {
			var result = runScript(script);
			assertEquals(result, expectedValue);
		}
		else {
			doAssertBugEquals(script, expectedNaftahBugError);
		}
	}

	@ParameterizedTest
	@ArgumentsSource(BlockProvider.class)
	void blockTests(boolean validScript, String script, Object expectedValue, NaftahBugError expectedNaftahBugError) throws Exception {
		if (validScript) {
			var result = runScript(script);
			assertEquals(result, expectedValue);
		}
		else {
			doAssertBugEquals(script, expectedNaftahBugError);
		}
	}

	@ParameterizedTest
	@ArgumentsSource(AssignmentProvider.class)
	void assignmentTests(boolean validScript, String script, Object expectedValue, NaftahBugError expectedNaftahBugError) throws Exception {
		if (validScript) {
			var result = runScript(script);
			assertEquals(result, expectedValue);
		}
		else {
			doAssertBugEquals(script, expectedNaftahBugError);
		}
	}

	@ParameterizedTest
	@ArgumentsSource(DeclarationProvider.class)
	void declarationTests(boolean validScript, String script, Object expectedValue, NaftahBugError expectedNaftahBugError) throws Exception {
		if (validScript) {
			var result = runScript(script);
			assertEquals(result, expectedValue);
		}
		else {
			doAssertBugEquals(script, expectedNaftahBugError);
		}
	}

	@ParameterizedTest
	@ArgumentsSource(FunctionDeclarationProvider.class)
	void functionDeclarationTests(boolean validScript, String script, Object expectedValue, NaftahBugError expectedNaftahBugError) throws Exception {
		if (validScript) {
			var result = runScript(script);
			assertEquals(result, expectedValue);
		}
		else {
			doAssertBugEquals(script, expectedNaftahBugError);
		}
	}
}
