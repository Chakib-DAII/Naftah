package org.daiitech.naftah.parser;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.IntStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.VocabularyImpl;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.misc.IntervalSet;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NaftahErrorListenerTests {

	@Test
	void testMismatchedInputErrorThrowsParseCancellationException() {
		NaftahErrorListener listener = new NaftahErrorListener();
		Token offendingToken = createMockToken("if");

		String msg = "mismatched input 'if' expecting {'else', 'while'}";

		ParseCancellationException exception = assertThrows(ParseCancellationException.class, () -> {
			listener
					.syntaxError(
									createMockRecognizer(),  // can be mock or null
									offendingToken,
									3,
									5,
									msg,
									null
					);
		});

		assertTrue(exception.getMessage().contains("خطأ في بناء الجملة")); // Arabic message
	}

	@Test
	void testUnexpectedEOFShowsExpectedTokens() {
		NaftahErrorListener listener = new NaftahErrorListener();

		Parser parser = new DummyParserWithExpectedTokens(); // see below
		Token eofToken = createMockToken("<EOF>");

		String msg = "no viable alternative at input '<EOF>'";

		ParseCancellationException exception = assertThrows(ParseCancellationException.class, () -> {
			listener
					.syntaxError(
									parser,
									eofToken,
									10,
									0,
									msg,
									null
					);
		});

		assertTrue(exception.getMessage().contains("تم إيقاف التنفيذ"));
	}

	@Test
	void testMissingTokenError() {
		NaftahErrorListener listener = new NaftahErrorListener();
		Token offendingToken = createMockToken("}");

		String msg = "missing ';' at '}'";

		ParseCancellationException exception = assertThrows(ParseCancellationException.class, () -> {
			listener
					.syntaxError(
									createMockRecognizer(),
									offendingToken,
									5,
									12,
									msg,
									null
					);
		});

		// Assert Arabic translation
		assertEquals("خطأ في بناء الجملة. تم إيقاف التنفيذ.", exception.getMessage());
	}

	@Test
	void testTokenRecognitionError() {
		NaftahErrorListener listener = new NaftahErrorListener();
		Token offendingToken = createMockToken("@");

		String msg = "token recognition error at: '@'";

		ParseCancellationException exception = assertThrows(ParseCancellationException.class, () -> {
			listener
					.syntaxError(
									createMockRecognizer(),
									offendingToken,
									1,
									1,
									msg,
									null
					);
		});

		assertEquals("خطأ في بناء الجملة. تم إيقاف التنفيذ.", exception.getMessage());
	}

	private Token createMockToken(String text) {
		return new CommonToken(0, text);
	}

	private Recognizer<?, ?> createMockRecognizer() {
		return new Recognizer<>() {
			@Override
			public String[] getTokenNames() {
				return new String[0];
			}

			@Override
			public String[] getRuleNames() {
				return new String[0];
			}

			@Override
			public Vocabulary getVocabulary() {
				return VocabularyImpl.EMPTY_VOCABULARY;
			}

			@Override
			public String getGrammarFileName() {
				return null;
			}

			@Override
			public ATN getATN() {
				return null;
			}

			@Override
			public IntStream getInputStream() {
				return null;
			}

			@Override
			public void setInputStream(IntStream input) {

			}

			@Override
			public TokenFactory<?> getTokenFactory() {
				return null;
			}

			@Override
			public void setTokenFactory(TokenFactory<?> input) {

			}
		};
	}

	/**
	 * Dummy parser that simulates expected tokens for EOF test
	 */
	private static class DummyParserWithExpectedTokens extends Parser {
		public DummyParserWithExpectedTokens() {
			super(null);
		}

		@Override
		public String[] getTokenNames() {
			return new String[0];
		}

		@Override
		public String[] getRuleNames() {
			return new String[0];
		}

		@Override
		public IntervalSet getExpectedTokens() {
			IntervalSet set = new IntervalSet();
			set.add(TokenConstants.IF);  // Assume this is defined
			set.add(TokenConstants.WHILE);
			return set;
		}

		@Override
		public Vocabulary getVocabulary() {
			return new Vocabulary() {
				@Override
				public int getMaxTokenType() {
					return 100;
				}

				@Override
				public String getLiteralName(int tokenType) {
					return switch (tokenType) {
						case TokenConstants.IF -> "'if'";
						case TokenConstants.WHILE -> "'while'";
						default -> null;
					};
				}

				@Override
				public String getSymbolicName(int tokenType) {
					return switch (tokenType) {
						case TokenConstants.IF -> "IF";
						case TokenConstants.WHILE -> "WHILE";
						default -> null;
					};
				}

				@Override
				public String getDisplayName(int tokenType) {
					return getLiteralName(tokenType);
				}
			};
		}

		@Override
		public String getGrammarFileName() {
			return null;
		}

		@Override
		public ATN getATN() {
			return null;
		}
	}

	/**
	 * Simulated token constants (normally generated by ANTLR)
	 */
	private static class TokenConstants {
		public static final int IF = 1;
		public static final int WHILE = 2;
	}
}
