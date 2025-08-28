package org.daiitech.naftah.parser;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.parser.provider.script.ArithmeticExpressionsProvider;
import org.daiitech.naftah.parser.provider.script.AssignmentProvider;
import org.daiitech.naftah.parser.provider.script.BlockProvider;
import org.daiitech.naftah.parser.provider.script.CollectionExpressionProvider;
import org.daiitech.naftah.parser.provider.script.DeclarationProvider;
import org.daiitech.naftah.parser.provider.script.ForStatementProvider;
import org.daiitech.naftah.parser.provider.script.FunctionCallProvider;
import org.daiitech.naftah.parser.provider.script.FunctionDeclarationProvider;
import org.daiitech.naftah.parser.provider.script.IfStatementProvider;
import org.daiitech.naftah.parser.provider.script.LogicalExpressionsProvider;
import org.daiitech.naftah.parser.provider.script.QualifiedNameProvider;
import org.daiitech.naftah.parser.provider.script.ReturnStatementProvider;
import org.daiitech.naftah.parser.provider.script.ValueExpressionsProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static org.daiitech.naftah.Naftah.SCAN_CLASSPATH_PROPERTY;
import static org.daiitech.naftah.NaftahSystem.TERMINAL_HEIGHT_PROPERTY;
import static org.daiitech.naftah.NaftahSystem.TERMINAL_WIDTH_PROPERTY;
import static org.daiitech.naftah.TestUtils.assertBugEquals;
import static org.daiitech.naftah.TestUtils.assertEquals;
import static org.daiitech.naftah.TestUtils.runScript;
import static org.daiitech.naftah.parser.DefaultContext.CONTEXTS;
import static org.daiitech.naftah.parser.DefaultContext.bootstrap;
import static org.daiitech.naftah.utils.JulLoggerConfig.LOGGING_FILE;
import static org.daiitech.naftah.utils.JulLoggerConfig.initializeFromResources;
import static org.daiitech.naftah.utils.reflect.RuntimeClassScanner.CLASS_PATH_PROPERTY;

public class DefaultNaftahParserVisitorTests {

	@BeforeAll
	static void setupAll() throws IOException {
		initializeFromResources(LOGGING_FILE);

		String originalClassPath = System.getProperty(CLASS_PATH_PROPERTY);
		String tempPaths = Arrays
				.stream((originalClassPath).split(File.pathSeparator))
				.filter(path -> path.contains("Naftah"))
				.collect(Collectors.joining(File.pathSeparator));
		System.setProperty(CLASS_PATH_PROPERTY, tempPaths);

		System.setProperty(TERMINAL_WIDTH_PROPERTY, Integer.toString(80));
		System.setProperty(TERMINAL_HEIGHT_PROPERTY, Integer.toString(24));
		System.setProperty(SCAN_CLASSPATH_PROPERTY, Boolean.toString(false));

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
			assertBugEquals(script, expectedNaftahBugError);
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
	@ArgumentsSource(CollectionExpressionProvider.class)
	void collectionTests(   boolean validScript,
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
