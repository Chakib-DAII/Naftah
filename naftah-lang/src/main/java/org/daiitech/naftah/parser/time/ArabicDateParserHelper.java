package org.daiitech.naftah.parser.time;

import java.util.List;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.daiitech.naftah.builtin.time.ArabicTemporal;
import org.daiitech.naftah.builtin.utils.tuple.Pair;
import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.parser.ArabicDateLexer;
import org.daiitech.naftah.parser.ArabicDateParser;
import org.daiitech.naftah.parser.NaftahErrorListener;
import org.daiitech.naftah.parser.NaftahParserHelper;

import static org.daiitech.naftah.Naftah.DEBUG_PROPERTY;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;
import static org.daiitech.naftah.parser.NaftahParserHelper.getCharStream;
import static org.daiitech.naftah.parser.NaftahParserHelper.printTokens;

/**
 * Helper class for parsing Arabic date expressions using ANTLR-generated
 * lexer and parser classes.
 *
 * <p>This class provides utility methods to:
 * <ul>
 * <li>Create a lexer and token stream from a {@link CharStream}</li>
 * <li>Prepare and configure an {@link ArabicDateParser}</li>
 * <li>Run the parser on a string input and return an {@link ArabicTemporal} result</li>
 * </ul>
 *
 * <p>It supports multiple error listeners and debug output for token streams.</p>
 *
 * @author Chakib Daii
 */
public final class ArabicDateParserHelper {
	/**
	 * Private constructor to prevent instantiation.
	 * Always throws a {@link NaftahBugError} when called.
	 */
	private ArabicDateParserHelper() {
		throw newNaftahBugInvalidUsageError();
	}

	/**
	 * Creates a {@link CommonTokenStream} and associated {@link ArabicDateLexer} from a given
	 * {@link CharStream} and list of ANTLR error listeners.
	 *
	 * @param charStream     the input character stream
	 * @param errorListeners the list of ANTLR error listeners
	 * @return a pair containing the lexer and token stream
	 */
	public static Pair<ArabicDateLexer, CommonTokenStream> getCommonTokenStream(
																				CharStream charStream,
																				List<ANTLRErrorListener> errorListeners) {
		return NaftahParserHelper
				.getCommonTokenStream(  () -> new ArabicDateLexer(charStream),
										errorListeners);
	}

	/**
	 * Prepares an {@link ArabicDateParser} for parsing using a single error listener.
	 *
	 * @param input         the input character stream
	 * @param errorListener the ANTLR error listener
	 * @return a configured {@link ArabicDateParser} instance
	 */
	public static ArabicDateParser prepareRun(CharStream input, ANTLRErrorListener errorListener) {
		return prepareRun(input, List.of(errorListener));
	}

	/**
	 * Prepares an {@link ArabicDateParser} for parsing using a list of error listeners.
	 *
	 * <p>Also optionally prints token stream information if debug mode is enabled.</p>
	 *
	 * @param input          the input character stream
	 * @param errorListeners the list of ANTLR error listeners
	 * @return a configured {@link ArabicDateParser} instance
	 */
	public static ArabicDateParser prepareRun(  CharStream input,
												List<ANTLRErrorListener> errorListeners) {
		// Create a lexer and token stream
		var lexerCommonTokenStreamPair = getCommonTokenStream(input, errorListeners);

		CommonTokenStream tokens = lexerCommonTokenStreamPair.getRight();

		if (Boolean.getBoolean(DEBUG_PROPERTY)) {
			printTokens(tokens, lexerCommonTokenStreamPair.getLeft().getVocabulary());
		}

		// Create a parser
		return getParser(tokens, errorListeners);
	}

	/**
	 * Creates and returns an {@link ArabicDateParser} instance from a given
	 * {@link CommonTokenStream} and list of error listeners.
	 *
	 * @param commonTokenStream the token stream from the lexer
	 * @param errorListeners    the list of ANTLR error listeners
	 * @return a configured {@link ArabicDateParser} instance
	 */
	public static ArabicDateParser getParser(   CommonTokenStream commonTokenStream,
												List<ANTLRErrorListener> errorListeners) {
		return NaftahParserHelper
				.getParser( () -> new ArabicDateParser(commonTokenStream),
							errorListeners);
	}

	/**
	 * Parses a string containing an Arabic date expression and returns
	 * the corresponding {@link ArabicTemporal} object.
	 *
	 * <p>This method handles lexer and parser creation, attaches error listeners,
	 * and uses the {@link DefaultArabicDateParserVisitor} to traverse the parse tree.</p>
	 *
	 * @param arabicDate the Arabic date expression as a string
	 * @return the parsed {@link ArabicTemporal} representation
	 */
	public static ArabicTemporal run(String arabicDate) {
		// Create an input stream from the Naftah code
		CharStream input = getCharStream(arabicDate);

		var parser = prepareRun(input, NaftahErrorListener.INSTANCE);

		// Create a visitor and visit the parse tree
		DefaultArabicDateParserVisitor visitor = new DefaultArabicDateParserVisitor(parser);
		// Parse the input and get the parse tree
		return (ArabicTemporal) visitor.visit();
	}
}
