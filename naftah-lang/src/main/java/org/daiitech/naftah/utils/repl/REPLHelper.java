package org.daiitech.naftah.utils.repl;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.daiitech.naftah.builtin.utils.tuple.ImmutablePair;
import org.daiitech.naftah.builtin.utils.tuple.Pair;
import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.parser.NaftahParserHelper;
import org.daiitech.naftah.parser.SyntaxHighlighter;
import org.daiitech.naftah.utils.script.NaftahHighlighter;
import org.jline.keymap.KeyMap;
import org.jline.reader.Binding;
import org.jline.reader.Completer;
import org.jline.reader.Highlighter;
import org.jline.reader.History;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.MaskingCallback;
import org.jline.reader.Reference;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedString;
import org.jline.utils.InfoCmp;

import com.vladsch.flexmark.ast.BlockQuote;
import com.vladsch.flexmark.ast.BulletList;
import com.vladsch.flexmark.ast.Code;
import com.vladsch.flexmark.ast.Emphasis;
import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.ast.HardLineBreak;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.ast.ListItem;
import com.vladsch.flexmark.ast.OrderedList;
import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.ast.SoftLineBreak;
import com.vladsch.flexmark.ast.StrongEmphasis;
import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.ast.ThematicBreak;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;

import static org.daiitech.naftah.Naftah.INSIDE_REPL_PROPERTY;
import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;
import static org.daiitech.naftah.parser.DefaultContext.getCompletions;
import static org.daiitech.naftah.parser.NaftahParserHelper.LEXER_LITERALS;
import static org.daiitech.naftah.utils.script.ScriptUtils.ARABIC_LOCALE;
import static org.daiitech.naftah.utils.script.ScriptUtils.padText;
import static org.daiitech.naftah.utils.script.ScriptUtils.shape;
import static org.daiitech.naftah.utils.script.ScriptUtils.shouldReshape;

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
	 * Holds the last printed output as a thread-safe reference.
	 * <p>
	 * This can be used for operations such as copying the last output to the clipboard
	 * or accessing it programmatically. The use of {@link AtomicReference} ensures
	 * that updates to the last printed value are thread-safe.
	 */
	public static final AtomicReference<String> LAST_PRINTED = new AtomicReference<>();
	/**
	 * A reusable instance of {@link Parser} from the Flexmark library used for parsing
	 * Markdown content into an abstract syntax tree (AST).
	 * <p>
	 * This parser can be used to convert raw Markdown strings into a structured {@link Node}
	 * tree that can be traversed or rendered.
	 * <p>
	 * It is recommended to reuse this instance instead of creating new ones for performance.
	 */
	public static final Parser MARKDOWN_PARSER = Parser.builder().build();
	/**
	 * Regex for matching variable names.
	 */
	public static final String REGEX_VARIABLE = "[\\p{L}_][\\p{L}0-9_-]*";
	/**
	 * Regex for matching command names.
	 */
	public static final String REGEX_COMMAND = "[:]?[\\p{L}]+[\\p{L}0-9_-]*";
	/**
	 * Default character that can be used to escape other characters.
	 */
	public static final char DEFAULT_ESCAPE_CHAR = '\\';
	/**
	 * Characters that can be used to escape other characters.
	 */
	public static final char[] ESCAPE_CHARS = new char[]{'N', DEFAULT_ESCAPE_CHAR};
	/**
	 * Set form of the escape characters for faster lookup.
	 */
	public static final Set<Character> ESCAPE_CHAR_SET = Set.of('N', DEFAULT_ESCAPE_CHAR);
	/**
	 * Regex pattern for matching escape characters or escape + newline.
	 */
	public static final String ESCAPE_CHARS_REGEX = ESCAPE_CHAR_SET
			.stream()
			.flatMap(c -> Stream
					.of(
						Pattern.quote(String.valueOf(c)),
						Pattern.quote(c + "\n")
					))
			.collect(Collectors.joining("|"));

	/**
	 * Quotation characters allowed in the REPL.
	 */
	public static final char[] QUOTE_CHARS = new char[]{'"', '«', '»'};
	/**
	 * Right-to-left prompt value.
	 */
	private static final String RTL_PROMPT_VALUE = "< نفطه >";
	/**
	 * Public RTL prompt with optional reshaping for display.
	 */
	public static final String RTL_PROMPT = shape(RTL_PROMPT_VALUE);
	/**
	 * Right-to-left multiline prompt marker value.
	 */
	private static final String RTL_MULTILINE_PROMPT_VALUE = "    < .... >";
	/**
	 * Public RTL multiline prompt with optional reshaping for display.
	 */
	public static final String RTL_MULTILINE_PROMPT = shape(RTL_MULTILINE_PROMPT_VALUE);
	/**
	 * The raw prompt message (in Arabic) used during right-to-left (RTL) pagination.
	 * <p>
	 * This message is displayed to the user between paginated content chunks,
	 * instructing them to press Enter to continue or enter one of the exit commands
	 * ('q', 'quit', or 'خروج') to terminate navigation and return to the main program.
	 */
	private static final String RTL_PAGINATION_PROMPT_VALUE = """
																[اضغط Enter للمتابعة، أو أدخل 'q' أو 'quit' أو 'خروج' لإنهاء التصفح والعودة إلى البرنامج الرئيسي.]
																""";
	/**
	 * The formatted RTL pagination prompt displayed to the user.
	 * <p>
	 * If Arabic text shaping is enabled (via {@code shouldReshape()}), this version
	 * uses a reshaped (visually adjusted) version of {@link #RTL_PAGINATION_PROMPT_VALUE}.
	 * Otherwise, it falls back to the original raw form.
	 */
	public static final String RTL_PAGINATION_PROMPT = shape(RTL_PAGINATION_PROMPT_VALUE);
	/**
	 * Command name for copying text to the clipboard.
	 */
	private static final String COPY_TO_CLIPBOARD_COMMAND = "copy-to-clipboard";
	/**
	 * Message displayed when text is successfully copied to the clipboard.
	 * Arabic: "[تم النسخ إلى ذاكرة النسخ بنجاح]"
	 */
	private static final String COPIED_TO_CLIPBOARD_MSG = "[تم النسخ إلى ذاكرة النسخ بنجاح]";
	/**
	 * Command name for copying the last printed output to the clipboard.
	 */
	private static final String COPY_LAST_PRINTED_TO_CLIPBOARD_COMMAND = "copy-last-output";
	/**
	 * Command name for pasting text from the clipboard.
	 */
	private static final String PASTE_FROM_CLIPBOARD_COMMAND = "paste-from-clipboard";
	/**
	 * Indicates if multiline mode is active in the REPL.
	 */
	public static boolean MULTILINE_IS_ACTIVE = false;
	/**
	 * Indicates if a text was just pasted to the REPL.
	 */
	public static boolean TEXT_PASTE_DETECTED = false;

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
	 * syntax highlighting, and autocompletion designed for Arabic/Naftah syntax.
	 * <p>
	 * The returned LineReader supports:
	 * <ul>
	 * <li>Parsing with custom regex for variables and commands</li>
	 * <li>Handling of escaped new lines and quoted strings</li>
	 * <li>Syntax highlighting combining the original highlighter and Arabic/Naftah specific rules</li>
	 * <li>Autocompletion with static and runtime completions including lexer literals</li>
	 * </ul>
	 *
	 * @param terminal the {@link Terminal} instance to which the LineReader will be bound
	 * @return a configured {@link LineReader} supporting Arabic/Naftah input
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
		Completer stringsCompleter = new NaftahStringsCompleter(runtimeCompletions);
		lineReaderBuilder.completer(stringsCompleter);

		return lineReaderBuilder.build();
	}

	/**
	 * Creates and returns a {@link LineReader} instance configured with a custom set of completions
	 * and syntax highlighting tailored for Arabic/Naftah input.
	 * <p>
	 * This method allows providing a specific collection of completion strings which will be
	 * used by the autocompleter.
	 *
	 * @param terminal    the {@link Terminal} instance to which the LineReader will be bound
	 * @param completions a {@link Collection} of completion strings for the autocompleter
	 * @return a configured {@link LineReader} with custom completions and Arabic-specific highlighting
	 */
	public static LineReader getLineReader(Terminal terminal, Collection<String> completions) {
		LineReader baseReader = LineReaderBuilder.builder().terminal(terminal).build();

		Highlighter originalHighlighter = baseReader.getHighlighter();

		Completer completer = new StringsCompleter(completions);

		return LineReaderBuilder
				.builder()
				.terminal(terminal)
				.completer(completer)
				.highlighter(new NaftahHighlighter(originalHighlighter))
				.build();
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
	 * @param reader      the LineReader to configure
	 * @param historyPath the path of history to use
	 */
	public static void setupHistoryConfig(LineReader reader, String historyPath) {
		reader.setVariable(LineReader.HISTORY_FILE, Paths.get(historyPath));
		reader.setVariable(LineReader.HISTORY_SIZE, 1000);
		reader.setVariable(LineReader.HISTORY_FILE_SIZE, 2000);

		reader.setOpt(LineReader.Option.HISTORY_IGNORE_DUPS);
		reader.setOpt(LineReader.Option.HISTORY_IGNORE_SPACE);
		reader.setOpt(LineReader.Option.HISTORY_BEEP);
		reader.setOpt(LineReader.Option.HISTORY_VERIFY);
	}

	/**
	 * Configures custom key bindings and clipboard-related widgets for a given {@link LineReader}.
	 * <p>
	 * This method performs the following actions:
	 * <ul>
	 * <li>Swaps the left and right arrow keys for cursor movement.</li>
	 * <li>Adds widgets for clipboard operations:
	 * <ul>
	 * <li>{@code copy-to-clipboard}: Copies the current buffer to the clipboard.</li>
	 * <li>{@code copy-last-output}: Copies the last printed output to the clipboard.</li>
	 * <li>{@code paste-from-clipboard}: Pastes clipboard content into the current buffer at the cursor
	 * position.</li>
	 * </ul>
	 * </li>
	 * <li>Binds keyboard shortcuts for clipboard and text editing operations:
	 * <ul>
	 * <li>Alt+C = copy current buffer</li>
	 * <li>Alt+L = copy last printed output</li>
	 * <li>Alt+V = paste from clipboard</li>
	 * <li>Alt+M = mark start</li>
	 * <li>Alt+X = cut selected region</li>
	 * <li>Alt+K = copy selected region</li>
	 * <li>Alt+Y = paste (yank)</li>
	 * </ul>
	 * </li>
	 * </ul>
	 *
	 * @param reader the {@link LineReader} to configure with custom key bindings and clipboard widgets
	 */
	public static void setupKeyBindingsConfig(LineReader reader) {
		reader.option(LineReader.Option.DISABLE_EVENT_EXPANSION, true);

		KeyMap<Binding> keyMap = reader.getKeyMaps().get(LineReader.MAIN);

		// Flip Left and Right arrow key bindings
		keyMap
				.bind(  new Reference(LineReader.FORWARD_CHAR),
						KeyMap.key(reader.getTerminal(), InfoCmp.Capability.key_left));
		keyMap
				.bind(  new Reference(LineReader.BACKWARD_CHAR),
						KeyMap.key(reader.getTerminal(), InfoCmp.Capability.key_right));

		// Copy current buffer to clipboard widget
		reader.getWidgets().put(COPY_TO_CLIPBOARD_COMMAND, () -> {
			String buffer = reader.getBuffer().toString();
			copyToClipboard(buffer);
			padText(COPIED_TO_CLIPBOARD_MSG, true);
			println(reader);
			return true;
		});

		// Copy last output to clipboard widget
		reader.getWidgets().put(COPY_LAST_PRINTED_TO_CLIPBOARD_COMMAND, () -> {
			String text = LAST_PRINTED.get();
			copyToClipboard(Objects.nonNull(text) ? text : "");
			padText(COPIED_TO_CLIPBOARD_MSG, true);
			println(reader);
			return true;
		});

		// Paste from clipboard into buffer widget
		reader.getWidgets().put(PASTE_FROM_CLIPBOARD_COMMAND, () -> {
			TEXT_PASTE_DETECTED = true;
			String text = pasteFromClipboard();
			if (Boolean.getBoolean(INSIDE_REPL_PROPERTY)) {
				text = processPastedText(text);
			}
			if (!text.isBlank()) {
				reader.getBuffer().write(text);  // inserts at cursor
			}
			return true;
		});

		// key bindings
		// Alt+c = copy current buffer to clipboard
		keyMap.bind(new Reference(COPY_TO_CLIPBOARD_COMMAND), KeyMap.alt('c'));
		// Alt+l = copy last printed line to clipboard
		keyMap.bind(new Reference(COPY_LAST_PRINTED_TO_CLIPBOARD_COMMAND), KeyMap.alt('l'));
		// Alt+v = paste from clipboard into buffer widget
		keyMap.bind(new Reference(PASTE_FROM_CLIPBOARD_COMMAND), KeyMap.alt('v'));
		// Alt+m = mark start
		keyMap.bind(new Reference(LineReader.SET_MARK_COMMAND), KeyMap.alt('m'));
		// Alt+x = cut selected region
		keyMap.bind(new Reference(LineReader.KILL_REGION), KeyMap.alt('x'));
		// Alt+k = copy selected region
		keyMap.bind(new Reference(LineReader.COPY_REGION_AS_KILL), KeyMap.alt('k'));
		// Alt+y = paste (yank)
		keyMap.bind(new Reference(LineReader.YANK), KeyMap.alt('y'));
	}

	/**
	 * Cleans and escapes the input string for proper handling in the REPL or syntax highlighting.
	 * <p>
	 * The method performs the following steps:
	 * <ul>
	 * <li>Removes block comments enclosed in <code>---* ... *---</code>.</li>
	 * <li>Removes single-line comments starting with <code>---</code>.</li>
	 * <li>Removes empty or whitespace-only lines.</li>
	 * <li>Prepends each remaining line with a space followed by the default escape character
	 * and appends a newline character.</li>
	 * </ul>
	 * This is useful to sanitize user input while preserving line separation and escaping for
	 * further processing, such as syntax highlighting or multi-line pasting.
	 *
	 * @param input the raw input string that may contain comments, empty lines, and unescaped characters
	 * @return a cleaned and escaped string where all comments and empty lines are removed,
	 *         and remaining lines are prepended with the escape character and terminated with a newline
	 */
	public static String processPastedText(String input) {
		// Remove block comments (---* ... *---)
		input = input.replaceAll("(?s)---\\*.*?\\*---", "");

		// Remove line comments (--- ...)
		input = input.replaceAll("---.*", "");

		// Split into lines to remove empty or whitespace-only lines
		StringBuilder cleaned = new StringBuilder();
		for (String line : input.split("\\r?\\n")) {
			if (line.trim().isEmpty()) {
				continue;  // skip empty lines
			}
			cleaned.append(" " + DEFAULT_ESCAPE_CHAR).append(line).append("\n");
		}

		// Return final cleaned, escaped string
		return cleaned.toString();
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

	/**
	 * Copies the given text to the system clipboard.
	 *
	 * @param text the text to copy
	 */
	public static void copyToClipboard(String text) {
		StringSelection selection = new StringSelection(text);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
	}

	/**
	 * Retrieves text content from the system clipboard.
	 * <p>
	 * If the clipboard does not contain a string or an error occurs, an empty string is returned.
	 *
	 * @return the text from the clipboard, or an empty string if unavailable
	 */
	public static String pasteFromClipboard() {
		try {
			return (String) Toolkit
					.getDefaultToolkit()
					.getSystemClipboard()
					.getData(DataFlavor.stringFlavor);
		}
		catch (Exception e) {
			return "";
		}
	}

	/**
	 * Aligns the given attributed string to the right side of the terminal,
	 * applying appropriate spacing and appending the prompt.
	 *
	 * @param str   the input string to align
	 * @param width the total width of the terminal line
	 * @return a right-aligned {@link AttributedString}
	 */
	public static AttributedString rightAlign(AttributedString str, int width) {
		AttributedString prompt = MULTILINE_IS_ACTIVE ?
				new AttributedString(RTL_MULTILINE_PROMPT) :
				new AttributedString(RTL_PROMPT);
		AttributedString delimiter = new AttributedString("");
		if (shouldReshape()) {
			int contentWidth = str.columnLength() + (MULTILINE_IS_ACTIVE ? 12 : 8); // text - prompt length
			int padding = Math.max(0, width - contentWidth);
			AttributedString spacePad = new AttributedString(" ".repeat(padding));

			return AttributedString.join(delimiter, spacePad, str, prompt);
		}
		return AttributedString.join(delimiter, prompt, str);
	}

	/**
	 * Prompts the user with a localized RTL pagination message asking whether to continue or quit.
	 * <p>
	 * Reads a line of input and checks if the user wants to quit based on recognized quit commands.
	 *
	 * @param reader the {@link LineReader} instance used to read user input
	 * @return {@code true} if the input matches "q", "quit", or "خروج" (case-insensitive), otherwise {@code false}
	 */
	public static boolean shouldQuit(LineReader reader) {
		String input = reader
				.readLine(  null,
							RTL_PAGINATION_PROMPT,
							(MaskingCallback) null,
							null);
		return List.of("q", "quit", "خروج").contains(input.trim().toLowerCase(ARABIC_LOCALE));
	}

	/**
	 * Clears the console screen using ANSI escape codes.
	 * Moves the cursor to the top-left corner.
	 */
	public static void clearScreen() {
		System.out.print("\033[H\033[2J");
		System.out.flush();
	}

	/**
	 * Recursively processes and converts a Markdown AST {@link Node} from the Flexmark library
	 * into a formatted plain-text representation, preserving structure and indentation.
	 * <p>
	 * This method supports a wide variety of Markdown elements, including:
	 * <ul>
	 * <li>{@link com.vladsch.flexmark.ast.Heading}</li>
	 * <li>{@link com.vladsch.flexmark.ast.Paragraph}</li>
	 * <li>{@link com.vladsch.flexmark.ast.Text}</li>
	 * <li>{@link com.vladsch.flexmark.ast.SoftLineBreak} / {@link com.vladsch.flexmark.ast.HardLineBreak}</li>
	 * <li>{@link com.vladsch.flexmark.ast.ThematicBreak}</li>
	 * <li>{@link com.vladsch.flexmark.ast.BulletList} / {@link com.vladsch.flexmark.ast.OrderedList}</li>
	 * <li>{@link com.vladsch.flexmark.ast.ListItem}</li>
	 * <li>{@link com.vladsch.flexmark.ast.Emphasis} / {@link com.vladsch.flexmark.ast.StrongEmphasis}</li>
	 * <li>{@link com.vladsch.flexmark.ast.Code} / {@link com.vladsch.flexmark.ast.FencedCodeBlock}</li>
	 * <li>{@link com.vladsch.flexmark.ast.Link} / {@link com.vladsch.flexmark.ast.Image}</li>
	 * <li>{@link com.vladsch.flexmark.ast.BlockQuote}</li>
	 * </ul>
	 * <p>
	 * Any unknown or unsupported node types are recursively processed if they contain children.
	 *
	 * @param node   the root Markdown {@link Node} to convert
	 * @param indent the indentation level to apply (used for nested structures)
	 * @return a formatted string representation of the node and its children
	 */
	public static String getMarkdownNodeAsString(Node node, int indent) {
		StringBuilder result = new StringBuilder();
		String indentStr = "  ".repeat(indent);

		if (node instanceof Heading heading) {
			// Append heading text with its level indicated by hashes #
			int level = heading.getLevel();
			String prefix = "#".repeat(level) + " ";
			String text = heading.getText().toString();
			result.append(prefix).append(text);
		}
		else if (node instanceof Paragraph) {
			// Append paragraphs as plain text with indentation
			StringBuilder paragraphText = new StringBuilder();
			for (Node child : node.getChildren()) {
				paragraphText.append(child.getChars());
			}
			result.append(indentStr).append(paragraphText).append("\n");
		}
		else if (node instanceof Text textNode) {
			result.append(indentStr).append(textNode.getChars());
		}
		else if (node instanceof SoftLineBreak || node instanceof HardLineBreak) {
			result.append("\n");
		}
		else if (node instanceof ThematicBreak) {
			result.append("\n---------------------------------------------------\n");
		}
		else if (node instanceof BulletList) {
			// For bullet lists, append each item prefixed with a bullet
			for (Node child : node.getChildren()) {
				result
						.append(indentStr)
						.append("• ")
						.append(getMarkdownNodeAsString(child, indent + 1))
						.append("\n");
			}
		}
		else if (node instanceof OrderedList orderedList) {
			int startNumber = orderedList.getStartNumber();
			int index = 0;
			for (Node child : node.getChildren()) {
				result
						.append(indentStr)
						.append(startNumber + index)
						.append(". ")
						.append(getMarkdownNodeAsString(child, indent + 1))
						.append("\n");
				index++;
			}
		}
		else if (node instanceof ListItem) {
			// List items: just append their children
			for (Node child : node.getChildren()) {
				result.append(getMarkdownNodeAsString(child, indent));
			}
		}
		else if (node instanceof Emphasis) {
			// Emphasis: append children wrapped with * *
			result.append(indentStr).append("*");
			for (Node child : node.getChildren()) {
				result.append(getMarkdownNodeAsString(child, 0));
			}
			result.append("*");
		}
		else if (node instanceof StrongEmphasis) {
			// Strong emphasis: append children wrapped with ** **
			result.append(indentStr).append("**");
			for (Node child : node.getChildren()) {
				result.append(getMarkdownNodeAsString(child, 0));
			}
			result.append("**");
		}
		else if (node instanceof Code code) {
			// Inline code wrapped with backticks
			result.append(indentStr).append("`").append(code.getText().toString()).append("`");
		}
		else if (node instanceof FencedCodeBlock codeBlock) {
			// Code block with language info if present
			String info = codeBlock.getInfo().toString();
			result
					.append(indentStr)
					.append("```")
					.append(info)
					.append(codeBlock.getContentChars())
					.append(indentStr)
					.append("```");
		}
		else if (node instanceof Link link) {
			// Append link text and URL
			String url = link.getUrl().toString();
			result.append(indentStr).append("[");
			for (Node child : link.getChildren()) {
				result.append(getMarkdownNodeAsString(child, 0));
			}
			result.append("](").append(url).append(")");
		}
		else if (node instanceof Image image) {
			// Append image alt text and URL
			String url = image.getUrl().toString();
			result.append(indentStr).append("![");
			for (Node child : image.getChildren()) {
				result.append(getMarkdownNodeAsString(child, 0));
			}
			result.append("](").append(url).append(")");
		}
		else if (node instanceof BlockQuote) {
			result.append(indentStr).append("> ");
			for (Node child : node.getChildren()) {
				result.append(getMarkdownNodeAsString(child, indent + 1));
			}
			result.append("\n");
		}
		else {
			// Default fallback: recursively append children
			if (node.hasChildren()) {
				for (Node child : node.getChildren()) {
					result.append(getMarkdownNodeAsString(child, indent));
				}
			}
		}
		return result.toString();
	}

	/**
	 * Parses a Markdown string using the {@link #MARKDOWN_PARSER} and converts
	 * it into a plain-text formatted representation suitable for terminal output.
	 * <p>
	 * This is typically used to display user guides, help documentation,
	 * or any content written in Markdown in a readable form within a terminal interface.
	 *
	 * @param topicContent the raw Markdown input as a {@link String}
	 * @return the formatted plain-text representation of the Markdown content
	 */
	public static String getMarkdownAsString(String topicContent) {
		Node document = MARKDOWN_PARSER.parse(topicContent);
		return getMarkdownNodeAsString(document, 0);
	}

	/**
	 * Saves a snippet of the REPL history to a timestamped file using the default code validation.
	 *
	 * @param history      the REPL history to save
	 * @param startingFrom only include entries after this timestamp, or all if null
	 * @throws IOException if an error occurs while writing the snippet file
	 */
	public static void saveHistorySnippet(History history, Instant startingFrom)
			throws IOException {
		saveHistorySnippet(history, Collections.emptySet(), startingFrom, NaftahParserHelper::validateCode);
	}

	/**
	 * Saves a snippet of the REPL history to a timestamped file using a custom code validation predicate.
	 *
	 * @param history                 the REPL history to save
	 * @param extraValidText          additional lines that should always be considered valid
	 * @param startingFrom            only include entries after this timestamp, or all if null
	 * @param codeValidationPredicate a predicate to determine if a line of code is valid and should be saved
	 * @throws IOException if an error occurs while writing the snippet file
	 */
	public static void saveHistorySnippet(  History history,
											Set<String> extraValidText,
											Instant startingFrom,
											Predicate<String> codeValidationPredicate)

			throws IOException {
		if (history.isEmpty()) {
			padText("لا يوجد سجل لحفظه.", true);
		}

		// Collect snippet
		String snippet = getHistoryContent(history, extraValidText, startingFrom, codeValidationPredicate);

		// Create a timestamp safe for filenames
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
		String timestamp = LocalDateTime.now().format(formatter);

		// Generate the file path
		Path filePath = Path.of("naftah-snippet-" + timestamp + ".naftah");

		// Write to file
		Files
				.writeString(   filePath,
								snippet,
								StandardOpenOption.CREATE,
								StandardOpenOption.TRUNCATE_EXISTING);

		padText("تم حفظ مقتطف السجل في الملف: " + filePath, true);
	}

	/**
	 * Prints the full REPL history to the console using the default code validation.
	 *
	 * @param history        the REPL history to print
	 * @param extraValidText additional lines that should always be considered valid
	 */
	public static void printFullHistory(History history, Set<String> extraValidText) {
		printHistory(history, extraValidText, null);
	}

	/**
	 * Prints the REPL history to the console, optionally filtering entries starting from a specific timestamp.
	 *
	 * @param history        the REPL history to print
	 * @param extraValidText additional lines that should always be considered valid
	 * @param startingFrom   only include entries after this timestamp, or all if null
	 */
	public static void printHistory(History history, Set<String> extraValidText, Instant startingFrom) {
		String snippet = getHistoryContent(history, extraValidText, startingFrom, NaftahParserHelper::validateCode);
		padText(snippet, true);
	}

	/**
	 * Retrieves the content of the REPL history as a string, filtered by timestamp and a validation predicate.
	 *
	 * @param history                 the REPL history
	 * @param extraValidText          additional lines that should always be considered valid
	 * @param startingFrom            only include entries after this timestamp, or all if null
	 * @param codeValidationPredicate a predicate to determine if a line of code is valid and should be included
	 * @return a string containing all valid history lines separated by line breaks
	 */
	public static String getHistoryContent( History history,
											Set<String> extraValidText,
											Instant startingFrom,
											Predicate<String> codeValidationPredicate) {
		// Collect snippet
		StringBuilder snippet = new StringBuilder();
		for (History.Entry entry : history) {
			if (Objects.nonNull(startingFrom) && entry.time().isBefore(startingFrom)) {
				continue;
			}

			String line = entry.line();
			if (extraValidText.contains(line) || codeValidationPredicate.test(line)) {
				snippet.append(line).append(System.lineSeparator());
			}
		}

		return snippet.toString();
	}

	/**
	 * Sanitizes the REPL history by removing invalid entries using the default code validation.
	 * <p>
	 * Only entries that pass validation or are contained in {@code extraValidText} are preserved.
	 * The sanitized history replaces the original in memory and on disk.
	 * </p>
	 *
	 * @param history        the REPL history to sanitize
	 * @param extraValidText additional lines that should always be considered valid
	 * @throws IOException if an error occurs while saving the sanitized history
	 */
	public static void sanitizeHistory(History history, Set<String> extraValidText) throws IOException {
		sanitizeHistory(history, extraValidText, NaftahParserHelper::validateCode);
	}

	/**
	 * Sanitizes the REPL history by removing invalid entries using a custom code validation predicate.
	 * <p>
	 * Only entries that pass {@code codeValidationPredicate} or are contained in {@code extraValidText} are preserved.
	 * The sanitized history replaces the original in memory and on disk.
	 * </p>
	 *
	 * @param history                 the REPL history to sanitize
	 * @param extraValidText          additional lines that should always be considered valid
	 * @param codeValidationPredicate a predicate to determine if a line of code is valid and should be preserved
	 * @throws IOException if an error occurs while saving the sanitized history
	 */
	public static void sanitizeHistory( History history,
										Set<String> extraValidText,
										Predicate<String> codeValidationPredicate) throws IOException {
		// Iterate safely using a ListIterator so we can remove entries
		List<Pair<Instant, String>> sanitizedEntries = new ArrayList<>();

		for (DefaultHistory.Entry entry : history) {
			String line = entry.line();

			if (extraValidText.contains(line) || codeValidationPredicate.test(line)) {
				sanitizedEntries.add(ImmutablePair.of(entry.time(), line));
			}
		}

		// Clear original history in memory
		history.purge();

		// Add sanitized entries back with original timestamps
		for (var entry : sanitizedEntries) {
			history.add(entry.getLeft(), entry.getRight());
		}

		// Save sanitized history back to the original file
		history.save();
	}

}
