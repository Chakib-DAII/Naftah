package org.daiitech.naftah.utils.repl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.parser.SyntaxHighlighter;
import org.daiitech.naftah.utils.arabic.ArabicHighlighter;
import org.jline.keymap.KeyMap;
import org.jline.reader.Binding;
import org.jline.reader.Completer;
import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.Reference;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.reader.impl.completer.StringsCompleter;
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

import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;
import static org.daiitech.naftah.parser.DefaultContext.getCompletions;
import static org.daiitech.naftah.parser.NaftahParserHelper.LEXER_LITERALS;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.padText;
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
	public static Parser MARKDOWN_PARSER = Parser.builder().build();

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
		Completer stringsCompleter = new ArabicStringsCompleter(runtimeCompletions);
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
				.highlighter(new ArabicHighlighter(originalHighlighter))
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

	/**
	 * Aligns the given attributed string to the right side of the terminal,
	 * applying appropriate spacing and appending the prompt.
	 *
	 * @param str   the input string to align
	 * @param width the total width of the terminal line
	 * @return a right-aligned {@link AttributedString}
	 */
	public static AttributedString rightAlign(AttributedString str, int width) {
		int contentWidth = str.columnLength() + (MULTILINE_IS_ACTIVE ? 12 : 8); // text - prompt length
		int padding = Math.max(0, width - contentWidth);
		AttributedString spacePad = new AttributedString(" ".repeat(padding));
		AttributedString prompt = MULTILINE_IS_ACTIVE ?
				new AttributedString(RTL_MULTILINE_PROMPT) :
				new AttributedString(RTL_PROMPT);
		return AttributedString.join(new AttributedString(""), spacePad, str, prompt);
	}

	/**
	 * Recursively processes and prints the content of a Markdown AST {@link Node}
	 * from the Flexmark library to the terminal, with appropriate formatting and indentation.
	 * <p>
	 * This method handles a wide range of Markdown node types including:
	 * <ul>
	 * <li>{@link com.vladsch.flexmark.ast.Heading} - rendered with '#' prefix</li>
	 * <li>{@link com.vladsch.flexmark.ast.Paragraph} - rendered as plain text</li>
	 * <li>{@link com.vladsch.flexmark.ast.Text} - rendered as-is</li>
	 * <li>{@link com.vladsch.flexmark.ast.SoftLineBreak} and {@link com.vladsch.flexmark.ast.HardLineBreak}</li>
	 * <li>{@link com.vladsch.flexmark.ast.ThematicBreak} - rendered as a horizontal line</li>
	 * <li>{@link com.vladsch.flexmark.ast.BulletList} and {@link com.vladsch.flexmark.ast.OrderedList}</li>
	 * <li>{@link com.vladsch.flexmark.ast.ListItem} - recursive rendering</li>
	 * <li>{@link com.vladsch.flexmark.ast.Emphasis} and {@link com.vladsch.flexmark.ast.StrongEmphasis}</li>
	 * <li>{@link com.vladsch.flexmark.ast.Code} and {@link com.vladsch.flexmark.ast.FencedCodeBlock}</li>
	 * <li>{@link com.vladsch.flexmark.ast.Link} and {@link com.vladsch.flexmark.ast.Image}</li>
	 * <li>{@link com.vladsch.flexmark.ast.BlockQuote}</li>
	 * </ul>
	 * <p>
	 * For unknown node types, if they have children, this method recursively processes them.
	 * Otherwise, the node is skipped without rendering.
	 *
	 * @param node   the root Markdown {@link Node} to render
	 * @param indent the indentation level for nested elements, used to create visual structure
	 */
	public static void printMarkdownNode(Node node, int indent) {
		String indentStr = "  ".repeat(indent);

		if (node instanceof Heading heading) {
			// Print heading text with its level indicated by hashes #
			int level = heading.getLevel();
			String prefix = "#".repeat(level) + " ";
			String text = heading.getText().toString();
			padText(prefix + text, true);
		}
		else if (node instanceof Paragraph) {
			// Print paragraphs as plain text with indentation
			StringBuilder paragraphText = new StringBuilder();
			for (Node child : node.getChildren()) {
				paragraphText.append(child.getChars());
			}
			padText(indentStr + paragraphText, true);
			System.out.println();
		}
		else if (node instanceof Text textNode) {
			padText(indentStr + textNode.getChars(), true);
		}
		else if (node instanceof SoftLineBreak || node instanceof HardLineBreak) {
			System.out.println();
		}
		else if (node instanceof ThematicBreak) {
			padText("\n---------------------------------------------------\n", true);
		}
		else if (node instanceof BulletList) {
			// For bullet lists, print each item prefixed with a bullet
			for (Node child : node.getChildren()) {
				padText(indentStr + "• ", true);
				printMarkdownNode(child, indent + 1);
				System.out.println();
			}
		}
		else if (node instanceof OrderedList orderedList) {
			int startNumber = orderedList.getStartNumber();
			int index = 0;
			for (Node child : node.getChildren()) {
				padText(indentStr + (startNumber + index) + ". ", true);
				printMarkdownNode(child, indent + 1);
				System.out.println();
				index++;
			}
		}
		else if (node instanceof ListItem) {
			// List items: just print their children
			for (Node child : node.getChildren()) {
				printMarkdownNode(child, indent);
			}
		}
		else if (node instanceof Emphasis) {
			// Emphasis: print children wrapped with * *
			padText(indentStr + "*", false);
			for (Node child : node.getChildren()) {
				printMarkdownNode(child, 0);
			}
			padText("*", true);
		}
		else if (node instanceof StrongEmphasis) {
			// Strong emphasis: print children wrapped with ** **
			padText(indentStr + "**", false);
			for (Node child : node.getChildren()) {
				printMarkdownNode(child, 0);
			}
			padText("**", true);
		}
		else if (node instanceof Code code) {
			// Inline code wrapped with backticks
			padText(indentStr + "`" + code.getText().toString() + "`", true);
		}
		else if (node instanceof FencedCodeBlock codeBlock) {
			// Code block with language info if present
			String info = codeBlock.getInfo().toString();
			padText(indentStr + "```" + info, true);
			padText(codeBlock.getContentChars().toString(), true);
			padText(indentStr + "```", true);
		}
		else if (node instanceof Link link) {
			// Print link text and URL
			String url = link.getUrl().toString();
			padText(indentStr + "[", false);
			for (Node child : link.getChildren()) {
				printMarkdownNode(child, 0);
			}
			padText("](" + url + ")", true);
		}
		else if (node instanceof Image image) {
			// Print image alt text and URL
			String url = image.getUrl().toString();
			padText(indentStr + "![", false);
			for (Node child : image.getChildren()) {
				printMarkdownNode(child, 0);
			}
			padText("](" + url + ")", true);
		}
		else if (node instanceof BlockQuote) {
			padText(indentStr + "> ", false);
			for (Node child : node.getChildren()) {
				printMarkdownNode(child, indent + 1);
			}
			System.out.println();
		}
		else {
			// Default fallback: recursively print children
			if (node.hasChildren()) {
				for (Node child : node.getChildren()) {
					printMarkdownNode(child, indent);
				}
			}
		}
	}

	/**
	 * Parses a raw Markdown string using the configured {@code MARKDOWN_PARSER},
	 * and prints the content to the terminal in a formatted style by delegating
	 * to {@link #printMarkdownNode(Node, int)}.
	 * <p>
	 * This function is typically used to display help documents, manuals, or guides
	 * stored as Markdown content directly to the console or terminal.
	 *
	 * @param topicContent the raw Markdown content as a {@link String}
	 */
	public static void printMarkdown(String topicContent) {
		Node document = MARKDOWN_PARSER.parse(topicContent);
		printMarkdownNode(document, 0);
	}
}
