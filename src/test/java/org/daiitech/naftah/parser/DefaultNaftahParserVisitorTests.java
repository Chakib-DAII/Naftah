package org.daiitech.naftah.parser;

import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.parser.provider.ArithmeticExpressionsProvider;
import org.daiitech.naftah.parser.provider.AssignmentProvider;
import org.daiitech.naftah.parser.provider.BlockProvider;
import org.daiitech.naftah.parser.provider.DeclarationProvider;
import org.daiitech.naftah.parser.provider.FunctionCallProvider;
import org.daiitech.naftah.parser.provider.FunctionDeclarationProvider;
import org.daiitech.naftah.parser.provider.LogicalExpressionsProvider;
import org.daiitech.naftah.parser.provider.ValueExpressionsProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static org.daiitech.naftah.Naftah.SCAN_CLASSPATH_PROPERTY;
import static org.daiitech.naftah.NaftahSystem.TERMINAL_HEIGHT_PROPERTY;
import static org.daiitech.naftah.NaftahSystem.TERMINAL_WIDTH_PROPERTY;
import static org.daiitech.naftah.TestUtils.assertEquals;
import static org.daiitech.naftah.TestUtils.doAssertBugEquals;
import static org.daiitech.naftah.TestUtils.runScript;
import static org.daiitech.naftah.parser.DefaultContext.CONTEXTS;
import static org.daiitech.naftah.parser.DefaultContext.bootstrap;

public class DefaultNaftahParserVisitorTests {

	@BeforeAll
	static void setupAll() {
		System.setProperty(TERMINAL_WIDTH_PROPERTY, Integer.toString(80));
		System.setProperty(TERMINAL_HEIGHT_PROPERTY, Integer.toString(24));
		System.setProperty(SCAN_CLASSPATH_PROPERTY, Boolean.toString(true));

		bootstrap(false);
	}

	@BeforeEach
	void setup() {
		CONTEXTS.clear();
	}

	@ParameterizedTest
	@ArgumentsSource(ValueExpressionsProvider.class)
	void valueExpressionTests(  boolean validScript,
								String script,
								Object expectedValue,
								NaftahBugError expectedNaftahBugError) throws Exception {
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
	void logicalExpressionTests(boolean validScript,
								String script,
								Object expectedValue,
								NaftahBugError expectedNaftahBugError) throws Exception {
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
	void arithmeticExpressionTests( boolean validScript,
									String script,
									Object expectedValue,
									NaftahBugError expectedNaftahBugError) throws Exception {
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
	void blockTests(boolean validScript, String script, Object expectedValue, NaftahBugError expectedNaftahBugError)
			throws Exception {
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
	void assignmentTests(   boolean validScript,
							String script,
							Object expectedValue,
							NaftahBugError expectedNaftahBugError) throws Exception {
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
	void declarationTests(  boolean validScript,
							String script,
							Object expectedValue,
							NaftahBugError expectedNaftahBugError) throws Exception {
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
	void functionDeclarationTests(  boolean validScript,
									String script,
									Object expectedValue,
									NaftahBugError expectedNaftahBugError) throws Exception {
		if (validScript) {
			var result = runScript(script);
			assertEquals(result, expectedValue);
		}
		else {
			doAssertBugEquals(script, expectedNaftahBugError);
		}
	}


	@ParameterizedTest
	@ArgumentsSource(FunctionCallProvider.class)
	void functionCallTests( boolean validScript,
							String script,
							Object expectedValue,
							NaftahBugError expectedNaftahBugError) throws Exception {
		if (validScript) {
			var result = runScript(script);
			assertEquals(result, expectedValue);
		}
		else {
			doAssertBugEquals(script, expectedNaftahBugError);
		}
	}
}
