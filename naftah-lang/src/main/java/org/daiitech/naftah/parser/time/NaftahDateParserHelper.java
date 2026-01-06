package org.daiitech.naftah.parser.time;

import java.time.Duration;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.List;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.daiitech.naftah.builtin.time.NaftahDate;
import org.daiitech.naftah.builtin.time.NaftahDuration;
import org.daiitech.naftah.builtin.time.NaftahPeriod;
import org.daiitech.naftah.builtin.time.NaftahPeriodWithDuration;
import org.daiitech.naftah.builtin.time.NaftahTemporal;
import org.daiitech.naftah.builtin.time.NaftahTemporalAmount;
import org.daiitech.naftah.builtin.time.NaftahTemporalPoint;
import org.daiitech.naftah.builtin.time.NaftahTime;
import org.daiitech.naftah.builtin.utils.tuple.Pair;
import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.parser.NaftahDateLexer;
import org.daiitech.naftah.parser.NaftahDateParser;
import org.daiitech.naftah.parser.NaftahDateParserBaseVisitor;
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
 * <li>Prepare and configure an {@link NaftahDateParser}</li>
 * <li>Run the parser on a string input and return an {@link NaftahTemporal} result</li>
 * </ul>
 *
 * <p>It supports multiple error listeners and debug output for token streams.</p>
 *
 * @author Chakib Daii
 */
public final class NaftahDateParserHelper {
	/**
	 * Private constructor to prevent instantiation.
	 * Always throws a {@link NaftahBugError} when called.
	 */
	private NaftahDateParserHelper() {
		throw newNaftahBugInvalidUsageError();
	}

	/**
	 * Creates a {@link CommonTokenStream} and associated {@link NaftahDateLexer} from a given
	 * {@link CharStream} and list of ANTLR error listeners.
	 *
	 * @param charStream     the input character stream
	 * @param errorListeners the list of ANTLR error listeners
	 * @return a pair containing the lexer and token stream
	 */
	public static Pair<NaftahDateLexer, CommonTokenStream> getCommonTokenStream(
																				CharStream charStream,
																				List<ANTLRErrorListener> errorListeners) {
		return NaftahParserHelper
				.getCommonTokenStream(  () -> new NaftahDateLexer(charStream),
										errorListeners);
	}

	/**
	 * Prepares an {@link NaftahDateParser} for parsing using a single error listener.
	 *
	 * @param input         the input character stream
	 * @param errorListener the ANTLR error listener
	 * @return a configured {@link NaftahDateParser} instance
	 */
	public static NaftahDateParser prepareRun(CharStream input, ANTLRErrorListener errorListener) {
		return prepareRun(input, List.of(errorListener));
	}

	/**
	 * Prepares an {@link NaftahDateParser} for parsing using a list of error listeners.
	 *
	 * <p>Also optionally prints token stream information if debug mode is enabled.</p>
	 *
	 * @param input          the input character stream
	 * @param errorListeners the list of ANTLR error listeners
	 * @return a configured {@link NaftahDateParser} instance
	 */
	public static NaftahDateParser prepareRun(  CharStream input,
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
	 * Creates and returns an {@link NaftahDateParser} instance from a given
	 * {@link CommonTokenStream} and list of error listeners.
	 *
	 * @param commonTokenStream the token stream from the lexer
	 * @param errorListeners    the list of ANTLR error listeners
	 * @return a configured {@link NaftahDateParser} instance
	 */
	public static NaftahDateParser getParser(   CommonTokenStream commonTokenStream,
												List<ANTLRErrorListener> errorListeners) {
		return NaftahParserHelper
				.getParser( () -> new NaftahDateParser(commonTokenStream),
							errorListeners);
	}

	/**
	 * Parses an Arabic date/time expression and returns a typed temporal result.
	 *
	 * <p>This method performs the full parsing pipeline:
	 * <ul>
	 * <li>Creates a lexer and parser from the input string</li>
	 * <li>Registers the default error listener</li>
	 * <li>Traverses the parse tree using {@link DefaultNaftahDateParserVisitor}</li>
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
	 * @param <T>        the concrete {@link NaftahTemporal} subtype to return
	 * @return the parsed temporal representation
	 * @throws ClassCastException if the parsed result cannot be cast to {@code tClass}
	 * @throws RuntimeException   if a parsing or semantic error occurs
	 */
	public static <T extends NaftahTemporal> T run(String arabicDate, Class<T> tClass) {
		// Create an input stream from the Naftah code
		CharStream input = getCharStream(arabicDate);

		var parser = prepareRun(input, NaftahErrorListener.INSTANCE);

		// Create a visitor and visit the parse tree
		DefaultNaftahDateParserVisitor visitor = new DefaultNaftahDateParserVisitor(parser);
		// Parse the input and get the parse tree
		return tClass.cast(visitor.visit());
	}

	/**
	 * Visits the given parse tree using the provided Arabic date parser visitor.
	 *
	 * <p>This is a convenience method that delegates directly to
	 * {@link NaftahDateParserBaseVisitor#visit(ParseTree)}.</p>
	 *
	 * @param naftahDateParserBaseVisitor the visitor used to traverse the parse tree
	 * @param tree                        the parse tree to visit
	 * @return the result produced by the visitor
	 */
	public static Object visit( NaftahDateParserBaseVisitor<?> naftahDateParserBaseVisitor,
								ParseTree tree) {
		return naftahDateParserBaseVisitor.visit(tree);
	}

	/**
	 * Resolves and returns the current time as an {@link NaftahTime} instance.
	 *
	 * <p>If a zone or offset specifier is present, it is first visited and resolved
	 * using the provided visitor, then applied when computing the current time.</p>
	 *
	 * <p>If no zone or offset specifier is provided, the system default is used.</p>
	 *
	 * @param naftahDateParserBaseVisitor the visitor used to resolve the zone or offset
	 * @param zoneOrOffsetSpecifier       the optional zone or offset parse context
	 * @return an {@link NaftahTime} representing the current time
	 */
	public static NaftahTime currentTime(   NaftahDateParserBaseVisitor<?> naftahDateParserBaseVisitor,
											NaftahDateParser.ZoneOrOffsetSpecifierContext zoneOrOffsetSpecifier) {
		NaftahTime.ZoneOrOffset zoneOrOffset = NaftahParserHelper.hasChild(zoneOrOffsetSpecifier) ?
				(NaftahTime.ZoneOrOffset) visit(naftahDateParserBaseVisitor,
												zoneOrOffsetSpecifier) :
				null;

		return NaftahTime.now(zoneOrOffset);
	}


	/**
	 * Resolves and returns the current date as an {@link NaftahDate} instance.
	 *
	 * <p>The calendar is resolved from the provided calendar specifier if present;
	 * otherwise, the default chronology is used.</p>
	 *
	 * <p>If a zone or offset specifier is provided, it is applied when determining
	 * the current date.</p>
	 *
	 * @param naftahDateParserBaseVisitor the visitor used to resolve calendar and zone
	 * @param calendarSpecifier           the optional calendar parse context
	 * @param zoneOrOffsetSpecifier       the optional zone or offset parse context
	 * @return an {@link NaftahDate} representing the current date
	 */
	public static NaftahDate currentDate(   NaftahDateParserBaseVisitor<?> naftahDateParserBaseVisitor,
											NaftahDateParser.CalendarSpecifierContext calendarSpecifier,
											NaftahDateParser.ZoneOrOffsetSpecifierContext zoneOrOffsetSpecifier) {
		NaftahDate.Calendar calendar = NaftahParserHelper.hasChild(calendarSpecifier) ?
				(NaftahDate.Calendar) visit(naftahDateParserBaseVisitor,
											calendarSpecifier) :
				NaftahDate.Calendar.of(ChronologyUtils.DEFAULT_CHRONOLOGY);

		NaftahTime.ZoneOrOffset zoneOrOffset = NaftahParserHelper.hasChild(zoneOrOffsetSpecifier) ?
				(NaftahTime.ZoneOrOffset) visit(naftahDateParserBaseVisitor,
												zoneOrOffsetSpecifier) :
				null;

		return NaftahDate.now(calendar, zoneOrOffset);
	}

	/**
	 * Computes the Arabic temporal amount between two {@link NaftahTemporalPoint} instances.
	 *
	 * <p>The result represents the difference between {@code left} and {@code right} and
	 * can be one of the following, depending on the underlying {@link java.time.temporal.Temporal} objects:</p>
	 *
	 * <ul>
	 * <li>{@link NaftahDuration} – if the difference is time-based only (hours, minutes, seconds)</li>
	 * <li>{@link NaftahPeriod} – if the difference is date-based only (years, months, days)</li>
	 * <li>{@link NaftahPeriodWithDuration} – if the difference includes both a period and a duration</li>
	 * </ul>
	 *
	 * <p>This method uses {@link TemporalUtils#between(java.time.temporal.Temporal, java.time.temporal.Temporal)} to
	 * calculate
	 * the raw {@link TemporalAmount} and then wraps it in the appropriate Arabic-aware type.</p>
	 *
	 * @param left  the starting temporal point
	 * @param right the ending temporal point
	 * @return an {@link NaftahTemporalAmount} representing the difference between {@code left} and {@code right}
	 */
	public static NaftahTemporalAmount getArabicTemporalAmountBetween(  NaftahTemporalPoint left,
																		NaftahTemporalPoint right) {
		var durationPeriodTuple = TemporalUtils.between(left.temporal(), right.temporal());
		if (durationPeriodTuple.arity() == 1) {
			TemporalAmount temporalAmount = (TemporalAmount) durationPeriodTuple.get(0);
			if (temporalAmount instanceof Duration duration) {
				return NaftahDuration.of(duration);
			}
			else {
				return NaftahPeriod.of((Period) temporalAmount);
			}
		}
		else {
			return NaftahPeriodWithDuration
					.of(
						NaftahPeriod.of((Period) durationPeriodTuple.get(0)),
						NaftahDuration.of((Duration) durationPeriodTuple.get(1))
					);
		}
	}
}
