package org.daiitech.naftah.parser;

import java.util.Objects;

import org.antlr.v4.runtime.ANTLRErrorStrategy;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.misc.IntervalSet;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import static org.daiitech.naftah.parser.NaftahParserHelper.getFormattedTokenSymbols;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.padText;

/**
 * Custom error listener for Naftah language parsing errors.
 * <p>
 * This listener extends ANTLR's {@link BaseErrorListener} and provides custom error messages
 * in Arabic with enhanced formatting for better developer feedback. It translates common
 * syntax errors, provides detailed error locations (line, column), and handles special cases
 * like unexpected end-of-file (EOF).
 * </p>
 *
 * <p>
 * On encountering a syntax error, it formats and prints the error message,
 * then throws a {@link ParseCancellationException} to halt interpretation.
 * </p>
 *
 * <p>
 * Singleton instance: {@link #INSTANCE}
 * Error strategy: {@link #ERROR_HANDLER_INSTANCE}
 * </p>
 *
 * @author Chakib Daii
 */

public class NaftahErrorListener extends BaseErrorListener {
	/**
	 * A reusable ANTLR error handling strategy that immediately
	 * terminates parsing on the first syntax error.
	 */
	public static final ANTLRErrorStrategy ERROR_HANDLER_INSTANCE = new BailErrorStrategy();

	/**
	 * Singleton instance of the NaftahErrorListener to be used
	 * across the application for syntax error handling.
	 */
	public static final NaftahErrorListener INSTANCE = new NaftahErrorListener();

	/**
	 * Called by ANTLR when a syntax error is encountered during parsing.
	 * It formats the error message, translates common error phrases into Arabic,
	 * highlights the offending token, and displays expected tokens for EOF errors.
	 * Then it throws a {@link ParseCancellationException} to stop parsing.
	 *
	 * @param recognizer         the parser instance
	 * @param offendingSymbol    the symbol/token where the error occurred
	 * @param line               the line number of the error (1-based)
	 * @param charPositionInLine the character position within the line (0-based)
	 * @param msg                the error message provided by ANTLR
	 * @param e                  the exception thrown by the parser (can be null)
	 */
	@Override
	public void syntaxError(Recognizer<?, ?> recognizer,
							Object offendingSymbol,
							int line,
							int charPositionInLine,
							String msg,
							RecognitionException e) {

		// Extract offending text
		String offendingText = "";
		if (offendingSymbol instanceof Token token) {
			offendingText = token.getText();
		}

		// Translate message or construct better one if needed
		String translatedMessage = translateMessage(msg);

		// Handle unexpected EOF: show expected tokens
		if (msg.contains("no viable alternative at input") && "<EOF>".equals(offendingText)) {
			if (recognizer instanceof Parser parser) {
				IntervalSet expectedTokens = parser.getExpectedTokens();
				Vocabulary vocabulary = parser.getVocabulary();
				StringBuilder expected = new StringBuilder();
				for (int tokenType : expectedTokens.toArray()) {
					String formattedTokenSymbols = getFormattedTokenSymbols(vocabulary, tokenType, true);
					if (formattedTokenSymbols == null) {
						continue;
					}
					expected.append(formattedTokenSymbols);
				}

				translatedMessage = String.format("""
													📄 نهاية غير متوقعة للملف. المتوقع:
													%s
													""", expected);
			}
			else {
				translatedMessage = "📄 نهاية غير متوقعة للملف.";
			}
		}

		// Final formatted message (Arabic text block)
		String fullMessage = String
				.format("""
						💥 خطأ في بناء الجملة (Syntax Error)!
						📍 السطر: %d، العمود: %d
						%s
						%s
						""",
						line,
						charPositionInLine,
						offendingText.isBlank() ? "" : String.format("🔴 الرمز غير الصحيح: '%s'\n", offendingText),
						translatedMessage);

		try {
			padText(fullMessage, true);
		}
		catch (Throwable throwable) {
			System.out.println(fullMessage);
		}

		// Stop execution
		throw new ParseCancellationException(
												"""
															%s
												تم إيقاف التنفيذ.
												"""
														.formatted(
																	Objects.nonNull(fullMessage) ?
																			fullMessage :
																			"خطأ في بناء الجملة."));
	}

	/**
	 * Translates common ANTLR error message fragments from English into Arabic.
	 * If no known phrase is matched, returns the original message.
	 *
	 * @param msg the original error message from ANTLR
	 * @return the translated message in Arabic or the original if no translation exists
	 */
	private String translateMessage(String msg) {
		if (msg.contains("mismatched input")) {
			return msg.replace("mismatched input", "إدخال غير متطابق");
		}
		else if (msg.contains("missing")) {
			return msg.replace("missing", "مفقود");
		}
		else if (msg.contains("no viable alternative")) {
			return msg.replace("no viable alternative at input", "لا يوجد بديل صالح عند الإدخال");
		}
		else if (msg.contains("token recognition error at:")) {
			return msg.replace("token recognition error at:", "خطأ في التعرف على الرمز:");
		}
		return msg; // fallback
	}
}
