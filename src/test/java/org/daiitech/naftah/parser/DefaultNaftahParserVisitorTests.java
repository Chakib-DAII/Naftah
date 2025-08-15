package org.daiitech.naftah.parser;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.parser.provider.ArithmeticExpressionsProvider;
import org.daiitech.naftah.parser.provider.AssignmentProvider;
import org.daiitech.naftah.parser.provider.BlockProvider;
import org.daiitech.naftah.parser.provider.DeclarationProvider;
import org.daiitech.naftah.parser.provider.ForStatementProvider;
import org.daiitech.naftah.parser.provider.FunctionCallProvider;
import org.daiitech.naftah.parser.provider.FunctionDeclarationProvider;
import org.daiitech.naftah.parser.provider.IfStatementProvider;
import org.daiitech.naftah.parser.provider.LogicalExpressionsProvider;
import org.daiitech.naftah.parser.provider.QualifiedNameProvider;
import org.daiitech.naftah.parser.provider.ReturnStatementProvider;
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
import static org.daiitech.naftah.utils.reflect.RuntimeClassScanner.CLASS_PATH_PROPERTY;

public class DefaultNaftahParserVisitorTests {

	@BeforeAll
	static void setupAll() {
		String originalClassPath = System.getProperty(CLASS_PATH_PROPERTY);
		String tempPaths = Arrays
				.stream((originalClassPath).split(File.pathSeparator))
				.filter(path -> path.contains("Naftah"))
				.collect(Collectors.joining(File.pathSeparator));
		System.setProperty(CLASS_PATH_PROPERTY, tempPaths);

		System.setProperty(TERMINAL_WIDTH_PROPERTY, Integer.toString(80));
		System.setProperty(TERMINAL_HEIGHT_PROPERTY, Integer.toString(24));
		System.setProperty(SCAN_CLASSPATH_PROPERTY, Boolean.toString(true));

		bootstrap(false);

		System.setProperty(CLASS_PATH_PROPERTY, originalClassPath);
	}

	private static void runTest(boolean validScript,
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
		runTest(validScript, script, expectedValue, expectedNaftahBugError);
	}

	@ParameterizedTest
	@ArgumentsSource(LogicalExpressionsProvider.class)
	void logicalExpressionTests(boolean validScript,
								String script,
								Object expectedValue,
								NaftahBugError expectedNaftahBugError) throws Exception {
		runTest(validScript, script, expectedValue, expectedNaftahBugError);
	}

	@ParameterizedTest
	@ArgumentsSource(ArithmeticExpressionsProvider.class)
	void arithmeticExpressionTests( boolean validScript,
									String script,
									Object expectedValue,
									NaftahBugError expectedNaftahBugError) throws Exception {
		runTest(validScript, script, expectedValue, expectedNaftahBugError);
	}

	@ParameterizedTest
	@ArgumentsSource(BlockProvider.class)
	void blockTests(boolean validScript, String script, Object expectedValue, NaftahBugError expectedNaftahBugError)
			throws Exception {
		runTest(validScript, script, expectedValue, expectedNaftahBugError);
	}

	@ParameterizedTest
	@ArgumentsSource(AssignmentProvider.class)
	void assignmentTests(   boolean validScript,
							String script,
							Object expectedValue,
							NaftahBugError expectedNaftahBugError) throws Exception {
		runTest(validScript, script, expectedValue, expectedNaftahBugError);
	}

	@ParameterizedTest
	@ArgumentsSource(DeclarationProvider.class)
	void declarationTests(  boolean validScript,
							String script,
							Object expectedValue,
							NaftahBugError expectedNaftahBugError) throws Exception {
		runTest(validScript, script, expectedValue, expectedNaftahBugError);
	}

	@ParameterizedTest
	@ArgumentsSource(FunctionDeclarationProvider.class)
	void functionDeclarationTests(  boolean validScript,
									String script,
									Object expectedValue,
									NaftahBugError expectedNaftahBugError) throws Exception {
		runTest(validScript, script, expectedValue, expectedNaftahBugError);
	}

	@ParameterizedTest
	@ArgumentsSource(FunctionCallProvider.class)
	void functionCallTests( boolean validScript,
							String script,
							Object expectedValue,
							NaftahBugError expectedNaftahBugError) throws Exception {
		runTest(validScript, script, expectedValue, expectedNaftahBugError);
	}

	@ParameterizedTest
	@ArgumentsSource(QualifiedNameProvider.class)
	void qualifiedNameTests(boolean validScript,
							String script,
							Object expectedValue,
							NaftahBugError expectedNaftahBugError) throws Exception {
		runTest(validScript, script, expectedValue, expectedNaftahBugError);
	}

	@ParameterizedTest
	@ArgumentsSource(ReturnStatementProvider.class)
	void returnTests(   boolean validScript,
						String script,
						Object expectedValue,
						NaftahBugError expectedNaftahBugError) throws Exception {
		runTest(validScript, script, expectedValue, expectedNaftahBugError);
	}

	@ParameterizedTest
	@ArgumentsSource(IfStatementProvider.class)
	void ifStatementTests(  boolean validScript,
							String script,
							Object expectedValue,
							NaftahBugError expectedNaftahBugError) throws Exception {
		runTest(validScript, script, expectedValue, expectedNaftahBugError);
	}

	@ParameterizedTest
	@ArgumentsSource(ForStatementProvider.class)
	void forStatementTests( boolean validScript,
							String script,
							Object expectedValue,
							NaftahBugError expectedNaftahBugError) throws Exception {
		runTest(validScript, script, expectedValue, expectedNaftahBugError);
	}
}
