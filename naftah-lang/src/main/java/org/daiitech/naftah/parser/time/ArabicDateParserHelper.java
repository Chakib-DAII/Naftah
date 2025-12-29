package org.daiitech.naftah.parser.time;

import java.time.Duration;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.List;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.daiitech.naftah.builtin.time.ArabicDate;
import org.daiitech.naftah.builtin.time.ArabicDuration;
import org.daiitech.naftah.builtin.time.ArabicPeriod;
import org.daiitech.naftah.builtin.time.ArabicPeriodWithDuration;
import org.daiitech.naftah.builtin.time.ArabicTemporal;
import org.daiitech.naftah.builtin.time.ArabicTemporalAmount;
import org.daiitech.naftah.builtin.time.ArabicTemporalPoint;
import org.daiitech.naftah.builtin.time.ArabicTime;
import org.daiitech.naftah.builtin.utils.tuple.Pair;
import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.parser.ArabicDateLexer;
import org.daiitech.naftah.parser.ArabicDateParser;
import org.daiitech.naftah.parser.ArabicDateParserBaseVisitor;
import org.daiitech.naftah.parser.NaftahErrorListener;
import org.daiitech.naftah.parser.NaftahParserHelper;
import org.daiitech.naftah.utils.time.ChronologyUtils;
import org.daiitech.naftah.utils.time.TemporalUtils;

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
	 * Parses an Arabic date/time expression and returns a typed temporal result.
	 *
	 * <p>This method performs the full parsing pipeline:
	 * <ul>
	 * <li>Creates a lexer and parser from the input string</li>
	 * <li>Registers the default error listener</li>
	 * <li>Traverses the parse tree using {@link DefaultArabicDateParserVisitor}</li>
	 * <li>Returns the parsed result cast to the requested temporal type</li>
	 * </ul>
	 *
	 * <p>The returned object may represent:
	 * <ul>
	 * <li>A temporal point (date, time, or date-time)</li>
	 * <li>A temporal amount (duration, period, or a combination)</li>
	 * </ul>
	 *
	 * @param arabicDate the Arabic date/time expression to parse
	 * @param tClass     the expected result type
	 * @param <T>        the concrete {@link ArabicTemporal} subtype to return
	 * @return the parsed temporal representation
	 * @throws ClassCastException if the parsed result cannot be cast to {@code tClass}
	 * @throws RuntimeException   if a parsing or semantic error occurs
	 */
	public static <T extends ArabicTemporal> T run(String arabicDate, Class<T> tClass) {
		// Create an input stream from the Naftah code
		CharStream input = getCharStream(arabicDate);

		var parser = prepareRun(input, NaftahErrorListener.INSTANCE);

		// Create a visitor and visit the parse tree
		DefaultArabicDateParserVisitor visitor = new DefaultArabicDateParserVisitor(parser);
		// Parse the input and get the parse tree
		return tClass.cast(visitor.visit());
	}

	/**
	 * Visits the given parse tree using the provided Arabic date parser visitor.
	 *
	 * <p>This is a convenience method that delegates directly to
	 * {@link ArabicDateParserBaseVisitor#visit(ParseTree)}.</p>
	 *
	 * @param arabicDateParserBaseVisitor the visitor used to traverse the parse tree
	 * @param tree                        the parse tree to visit
	 * @return the result produced by the visitor
	 */
	public static Object visit( ArabicDateParserBaseVisitor<?> arabicDateParserBaseVisitor,
								ParseTree tree) {
		return arabicDateParserBaseVisitor.visit(tree);
	}

	/**
	 * Resolves and returns the current time as an {@link ArabicTime} instance.
	 *
	 * <p>If a zone or offset specifier is present, it is first visited and resolved
	 * using the provided visitor, then applied when computing the current time.</p>
	 *
	 * <p>If no zone or offset specifier is provided, the system default is used.</p>
	 *
	 * @param arabicDateParserBaseVisitor the visitor used to resolve the zone or offset
	 * @param zoneOrOffsetSpecifier       the optional zone or offset parse context
	 * @return an {@link ArabicTime} representing the current time
	 */
	public static ArabicTime currentTime(   ArabicDateParserBaseVisitor<?> arabicDateParserBaseVisitor,
											ArabicDateParser.ZoneOrOffsetSpecifierContext zoneOrOffsetSpecifier) {
		ArabicTime.ZoneOrOffset zoneOrOffset = NaftahParserHelper.hasChild(zoneOrOffsetSpecifier) ?
				(ArabicTime.ZoneOrOffset) visit(arabicDateParserBaseVisitor,
												zoneOrOffsetSpecifier) :
				null;

		return ArabicTime.now(zoneOrOffset);
	}


	/**
	 * Resolves and returns the current date as an {@link ArabicDate} instance.
	 *
	 * <p>The calendar is resolved from the provided calendar specifier if present;
	 * otherwise, the default chronology is used.</p>
	 *
	 * <p>If a zone or offset specifier is provided, it is applied when determining
	 * the current date.</p>
	 *
	 * @param arabicDateParserBaseVisitor the visitor used to resolve calendar and zone
	 * @param calendarSpecifier           the optional calendar parse context
	 * @param zoneOrOffsetSpecifier       the optional zone or offset parse context
	 * @return an {@link ArabicDate} representing the current date
	 */
	public static ArabicDate currentDate(   ArabicDateParserBaseVisitor<?> arabicDateParserBaseVisitor,
											ArabicDateParser.CalendarSpecifierContext calendarSpecifier,
											ArabicDateParser.ZoneOrOffsetSpecifierContext zoneOrOffsetSpecifier) {
		ArabicDate.Calendar calendar = NaftahParserHelper.hasChild(calendarSpecifier) ?
				(ArabicDate.Calendar) visit(arabicDateParserBaseVisitor,
											calendarSpecifier) :
				ArabicDate.Calendar.of(ChronologyUtils.DEFAULT_CHRONOLOGY);

		ArabicTime.ZoneOrOffset zoneOrOffset = NaftahParserHelper.hasChild(zoneOrOffsetSpecifier) ?
				(ArabicTime.ZoneOrOffset) visit(arabicDateParserBaseVisitor,
												zoneOrOffsetSpecifier) :
				null;

		return ArabicDate.now(calendar, zoneOrOffset);
	}

	/**
	 * Computes the Arabic temporal amount between two {@link ArabicTemporalPoint} instances.
	 *
	 * <p>The result represents the difference between {@code left} and {@code right} and
	 * can be one of the following, depending on the underlying {@link java.time.temporal.Temporal} objects:</p>
	 *
	 * <ul>
	 * <li>{@link ArabicDuration} – if the difference is time-based only (hours, minutes, seconds)</li>
	 * <li>{@link ArabicPeriod} – if the difference is date-based only (years, months, days)</li>
	 * <li>{@link ArabicPeriodWithDuration} – if the difference includes both a period and a duration</li>
	 * </ul>
	 *
	 * <p>This method uses {@link TemporalUtils#between(java.time.temporal.Temporal, java.time.temporal.Temporal)} to
	 * calculate
	 * the raw {@link TemporalAmount} and then wraps it in the appropriate Arabic-aware type.</p>
	 *
	 * @param left  the starting temporal point
	 * @param right the ending temporal point
	 * @return an {@link ArabicTemporalAmount} representing the difference between {@code left} and {@code right}
	 */
	public static ArabicTemporalAmount getArabicTemporalAmountBetween(  ArabicTemporalPoint left,
																		ArabicTemporalPoint right) {
		var durationPeriodTuple = TemporalUtils.between(left.temporal(), right.temporal());
		if (durationPeriodTuple.arity() == 1) {
			TemporalAmount temporalAmount = (TemporalAmount) durationPeriodTuple.get(0);
			if (temporalAmount instanceof Duration duration) {
				return ArabicDuration.of(duration);
			}
			else {
				return ArabicPeriod.of((Period) temporalAmount);
			}
		}
		else {
			return ArabicPeriodWithDuration
					.of(
						ArabicPeriod.of((Period) durationPeriodTuple.get(0)),
						ArabicDuration.of((Duration) durationPeriodTuple.get(1))
					);
		}
	}
}
