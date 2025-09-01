package org.daiitech.naftah.utils.repl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Stream;

import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.parser.SyntaxHighlighter;
import org.jline.keymap.KeyMap;
import org.jline.reader.Binding;
import org.jline.reader.Completer;
import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.Reference;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;

import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;
import static org.daiitech.naftah.parser.DefaultContext.getCompletions;
import static org.daiitech.naftah.parser.NaftahParserHelper.LEXER_LITERALS;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.shape;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.shouldReshape;

/**
 * A utility class providing helper methods and constants used by the REPL (Read-Eval-Print Loop)
 * in the Naftah programming environment. This includes prompt formatting, line reader configuration,
 * terminal setup, history handling, and string/character constants for parsing and highlighting.
 * <p>
 * This class is non-instantiable and all members are static.
 * </p>
 *
 * @author Chakib Daii
 */
public final class REPLHelper {

	/**
	 * Right-to-left prompt value.
	 */
	private static final String RTL_PROMPT_VALUE = "< نفطة >";

	/**
	 * Public RTL prompt with optional reshaping for display.
	 */
	public static final String RTL_PROMPT = shouldReshape() ? shape(RTL_PROMPT_VALUE) : RTL_PROMPT_VALUE;

	/**
	 * Right-to-left multiline prompt marker value.
	 */
	private static final String RTL_MULTILINE_PROMPT_VALUE = "    < .... >";

	/**
	 * Public RTL multiline prompt with optional reshaping for display.
	 */
	public static final String RTL_MULTILINE_PROMPT = shouldReshape() ?
			shape(RTL_MULTILINE_PROMPT_VALUE) :
			RTL_MULTILINE_PROMPT_VALUE;

	/**
	 * Indicates if multiline mode is active in the REPL.
	 */
	public static boolean MULTILINE_IS_ACTIVE = false;

	/**
	 * Regex for matching variable names.
	 */
	public static String REGEX_VARIABLE = "[\\p{L}_][\\p{L}0-9_-]*";

	/**
	 * Regex for matching command names.
	 */
	public static String REGEX_COMMAND = "[:]?[\\p{L}]+[\\p{L}0-9_-]*";

	/**
	 * Characters that can be used to escape other characters.
	 */
	public static char[] ESCAPE_CHARS = new char[]{'#', '\\'};

	/**
	 * Set form of the escape characters for faster lookup.
	 */
	public static Set<Character> ESCAPE_CHAR_SET = Set.of('#', '\\');

	/**
	 * Regex pattern for matching escape characters or escape + newline.
	 */
	public static String ESCAPE_CHARS_REGEX = String
			.join(  "|",
					ESCAPE_CHAR_SET
							.stream()
							.flatMap(character -> Stream.of(String.valueOf(character), character + "\n"))
							.toArray(String[]::new));

	/**
	 * Quotation characters allowed in the REPL.
	 */
	public static char[] QUOTE_CHARS = new char[]{'"', '«', '»'};

	/**
	 * Private constructor to prevent instantiation.
	 * Always throws a {@link NaftahBugError} when called.
	 */
	private REPLHelper() {
		throw newNaftahBugInvalidUsageError();
	}

	/**
	 * Builds and returns a configured {@link Terminal} for use in the REPL.
	 *
	 * @return a Terminal instance with UTF-8 encoding and color capabilities
	 * @throws IOException if terminal initialization fails
	 */
	public static Terminal getTerminal() throws IOException {
		return TerminalBuilder
				.builder()
				.encoding(StandardCharsets.UTF_8)
				.streams(System.in, System.out)
				.jna(true)
				.jansi(true)
				.color(true)
				.nativeSignals(true)
				.system(true)
				.exec(true)
				.build();
	}

	/**
	 * Creates and returns a {@link LineReader} instance configured with custom parsing,
	 * highlighting, and autocompletion for Arabic/Naftah syntax.
	 *
	 * @param terminal the terminal instance to bind the reader to
	 * @return a configured LineReader
	 */
	public static LineReader getLineReader(Terminal terminal) {
		LineReader baseReader = LineReaderBuilder.builder().terminal(terminal).build();

		Highlighter originalHighlighter = baseReader.getHighlighter();

		DefaultParser parser = new DefaultParser()
				.regexVariable(REGEX_VARIABLE)
				.regexCommand(REGEX_COMMAND)
				.eofOnEscapedNewLine(true)
				.eofOnUnclosedQuote(true)
				.quoteChars(QUOTE_CHARS)
				.escapeChars(ESCAPE_CHARS);

		var lineReaderBuilder = LineReaderBuilder
				.builder()
				.terminal(terminal)
				.parser(parser)
				.highlighter(new SyntaxHighlighter(originalHighlighter));

		// Load static and runtime completions for autocompletion
		var runtimeCompletions = getCompletions();
		runtimeCompletions.addAll(LEXER_LITERALS);
		Completer stringsCompleter = new ArabicStringsCompleter(runtimeCompletions);
		lineReaderBuilder.completer(stringsCompleter);

		return lineReaderBuilder.build();
	}

	/**
	 * Configures terminal capabilities, e.g., making the cursor invisible.
	 *
	 * @param terminal the terminal to configure
	 */
	public static void setupTerminalCapabilities(Terminal terminal) {
		terminal.puts(InfoCmp.Capability.cursor_invisible);
		terminal.flush();
	}

	/**
	 * Configures history settings for the REPL, including history file path,
	 * size limits, and duplicate entry handling.
	 *
	 * @param reader the LineReader to configure
	 */
	public static void setupHistoryConfig(LineReader reader) {
		reader.setVariable(LineReader.HISTORY_FILE, Paths.get("bin/.naftah_history"));
		reader.setVariable(LineReader.HISTORY_SIZE, 1000);
		reader.setVariable(LineReader.HISTORY_FILE_SIZE, 2000);

		reader.setOpt(LineReader.Option.HISTORY_IGNORE_DUPS);
		reader.setOpt(LineReader.Option.HISTORY_IGNORE_SPACE);
		reader.setOpt(LineReader.Option.HISTORY_BEEP);
		reader.setOpt(LineReader.Option.HISTORY_VERIFY);
	}

	public static void setupKeyBindingsConfig(LineReader reader) {
		KeyMap<Binding> keyMap = reader.getKeyMaps().get(LineReader.MAIN);

		// Flip Left and Right arrow key bindings
		keyMap
				.bind(  new Reference(LineReader.FORWARD_CHAR),
						KeyMap.key(reader.getTerminal(), InfoCmp.Capability.key_left));
		keyMap
				.bind(  new Reference(LineReader.BACKWARD_CHAR),
						KeyMap.key(reader.getTerminal(), InfoCmp.Capability.key_right));
	}

	/**
	 * Writes the given string to the terminal output without a newline.
	 *
	 * @param terminal the terminal to write to
	 * @param str      the string to output
	 */
	public static void print(Terminal terminal, String str) {
		terminal.writer().write(str);
	}

	/**
	 * Prints the given string followed by a newline to the terminal via the line reader.
	 *
	 * @param reader the line reader bound to the terminal
	 * @param s      the string to print
	 */
	public static void println(LineReader reader, String s) {
		print(reader.getTerminal(), s);
		println(reader);
	}

	/**
	 * Prints a platform-dependent newline to the terminal. Also triggers a line redraw
	 * if using {@link LineReaderImpl}.
	 *
	 * @param reader the line reader
	 */
	public static void println(LineReader reader) {
		reader.getTerminal().puts(InfoCmp.Capability.carriage_return);
		print(reader.getTerminal(), "\n");
		if (reader instanceof LineReaderImpl lineReader) {
			lineReader.redrawLine();
		}
	}
}
