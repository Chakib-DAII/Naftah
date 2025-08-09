package org.daiitech.naftah.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Pair;
import org.daiitech.naftah.utils.repl.BaseHighlighter;
import org.jline.reader.EOFError;
import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import static org.daiitech.naftah.NaftahSystem.TERMINAL_WIDTH_PROPERTY;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.shape;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.shouldReshape;
import static org.daiitech.naftah.utils.repl.REPLHelper.ESCAPE_CHARS_REGEX;
import static org.daiitech.naftah.utils.repl.REPLHelper.MULTILINE_IS_ACTIVE;
import static org.daiitech.naftah.utils.repl.REPLHelper.RTL_MULTILINE_PROMPT;
import static org.daiitech.naftah.utils.repl.REPLHelper.RTL_PROMPT;

/**
 * A syntax highlighter class specifically tailored for highlighting
 * Naftah language source code in a terminal environment.
 * <p>
 * It tokenizes the input buffer using a lexer and applies color styling
 * to various language constructs based on token types.
 * <p>
 * Right-to-left (RTL) shaping and terminal wrapping support are also handled.
 *
 * @author Chakib Daii
 */
public class SyntaxHighlighter extends BaseHighlighter {
	/**
	 * Constructs a SyntaxHighlighter that wraps another highlighter.
	 *
	 * @param originalHighlighter the base highlighter to delegate to
	 */
	public SyntaxHighlighter(Highlighter originalHighlighter) {
		super(originalHighlighter);
	}

	/**
	 * Highlights the input line with token-based styles and formatting.
	 * It performs RTL shaping and wraps lines based on terminal width.
	 *
	 * @param reader the line reader
	 * @param buffer the input string buffer
	 * @return an {@link AttributedString} containing styled output
	 * @throws EOFError if the buffer contains escape sequences indicating multiline
	 */
	@Override
	public AttributedString highlight(LineReader reader, String buffer) {
		if (buffer.isBlank()) {
			return new AttributedString(buffer);
		}

		if (buffer.split(ESCAPE_CHARS_REGEX).length > 1) {
			throw new EOFError(-1, -1, "Escaped new line", "newline");
		}

		// Create input stream from buffer
		CharStream input = CharStreams.fromString(buffer);

		// Get all tokens from lexer
		CommonTokenStream tokens = NaftahParserHelper.getCommonTokenStream(input);
		tokens.fill();

		int terminalWidth = Integer.getInteger(TERMINAL_WIDTH_PROPERTY);

		List<AttributedString> lines = new ArrayList<>();
		AttributedStringBuilder currentLine = new AttributedStringBuilder();
		int currentLineWidth = 0;

		int lastIndex = 0;

		List<Pair<CharSequence, AttributedStyle>> styledSegments = new ArrayList<>();

		for (Token token : tokens.getTokens()) {
			int type = token.getType();
			String text = token.getText();

			if (type == -1 || text == null) {
				continue;
			}

			int tokenStartIndex = token.getStartIndex();
			int tokenStopIndex = token.getStopIndex();

			// Add unmatched text before this token
			if (tokenStartIndex > lastIndex && lastIndex >= 0 && tokenStartIndex <= buffer.length()) {
				String gapText = buffer.substring(lastIndex, tokenStartIndex);
				styledSegments.add(new Pair<>(gapText, AttributedStyle.DEFAULT));
			}

			AttributedStyle style = getStyleForTokenType(type);
			String shaped = text;

			if (shouldReshape()) {
				try {
					shaped = shape(text);
				}
				catch (Exception e) {
					// fallback: use original
				}
			}

			styledSegments.add(new Pair<>(shaped, style));
			lastIndex = tokenStopIndex + 1;
		}

		// Add any unmatched trailing text after the last token
		if (lastIndex < buffer.length()) {
			String trailingText = buffer.substring(lastIndex);
			styledSegments.add(new Pair<>(trailingText, AttributedStyle.DEFAULT));
		}

		if (shouldReshape()) {
			// Reverse for RTL visual order (not buffer)
			Collections.reverse(styledSegments);
		}

		// Build lines with wrapping and right-alignment
		for (Pair<CharSequence, AttributedStyle> part : styledSegments) {
			AttributedString fragment = new AttributedString(part.a.toString(), part.b);
			int fragWidth = fragment.columnLength();

			if (currentLineWidth + fragWidth > terminalWidth) {
				// Right-align and add current line
				AttributedString rightAligned = rightAlign(currentLine.toAttributedString(), terminalWidth);
				lines.add(rightAligned);

				currentLine = new AttributedStringBuilder();
				currentLineWidth = 0;
			}

			currentLine.append(fragment);
			currentLineWidth += fragWidth;
		}

		// Add last line (right-aligned)
		if (!currentLine.isEmpty()) {
			lines.add(rightAlign(currentLine.toAttributedString(), terminalWidth));
		}

		// Join lines with newline separator
		Collections.reverse(lines);
		return AttributedString.join(AttributedString.NEWLINE, lines);
	}

	/**
	 * Maps token types to specific styles (e.g., colors, bold, italic).
	 *
	 * @param tokenType the integer type of the token from the lexer
	 * @return the corresponding {@link AttributedStyle} for the token
	 */
	private AttributedStyle getStyleForTokenType(int tokenType) {
		return switch (tokenType) {
			case org.daiitech.naftah.parser.NaftahLexer.AND, org.daiitech.naftah.parser.NaftahLexer.OR,
					org.daiitech.naftah.parser.NaftahLexer.BREAK, org.daiitech.naftah.parser.NaftahLexer.CONTINUE,
					org.daiitech.naftah.parser.NaftahLexer.DO, org.daiitech.naftah.parser.NaftahLexer.TO,
					org.daiitech.naftah.parser.NaftahLexer.DOWNTO, org.daiitech.naftah.parser.NaftahLexer.IF,
					org.daiitech.naftah.parser.NaftahLexer.ELSEIF, org.daiitech.naftah.parser.NaftahLexer.ELSE,
					org.daiitech.naftah.parser.NaftahLexer.END, org.daiitech.naftah.parser.NaftahLexer.FOR,
					org.daiitech.naftah.parser.NaftahLexer.FUNCTION, org.daiitech.naftah.parser.NaftahLexer.VARIABLE,
					org.daiitech.naftah.parser.NaftahLexer.CONSTANT, org.daiitech.naftah.parser.NaftahLexer.VOID,
					org.daiitech.naftah.parser.NaftahLexer.NULL, org.daiitech.naftah.parser.NaftahLexer.NOT,
					org.daiitech.naftah.parser.NaftahLexer.REPEAT, org.daiitech.naftah.parser.NaftahLexer.RETURN,
					org.daiitech.naftah.parser.NaftahLexer.THEN, org.daiitech.naftah.parser.NaftahLexer.UNTIL,
					org.daiitech.naftah.parser.NaftahLexer.WHILE, org.daiitech.naftah.parser.NaftahLexer.CASE,
					org.daiitech.naftah.parser.NaftahLexer.OF, org.daiitech.naftah.parser.NaftahLexer.ID ->
				AttributedStyle.BOLD.foreground(AttributedStyle.BLUE);
			case org.daiitech.naftah.parser.NaftahLexer.VAR, org.daiitech.naftah.parser.NaftahLexer.BOOLEAN,
					org.daiitech.naftah.parser.NaftahLexer.STRING_TYPE, org.daiitech.naftah.parser.NaftahLexer.CHAR,
					org.daiitech.naftah.parser.NaftahLexer.BYTE, org.daiitech.naftah.parser.NaftahLexer.SHORT,
					org.daiitech.naftah.parser.NaftahLexer.INT, org.daiitech.naftah.parser.NaftahLexer.LONG,
					org.daiitech.naftah.parser.NaftahLexer.FLOAT, org.daiitech.naftah.parser.NaftahLexer.DOUBLE ->
				AttributedStyle.BOLD.foreground(AttributedStyle.MAGENTA);
			case org.daiitech.naftah.parser.NaftahLexer.CHARACTER, org.daiitech.naftah.parser.NaftahLexer.STRING,
					org.daiitech.naftah.parser.NaftahLexer.NUMBER, org.daiitech.naftah.parser.NaftahLexer.TRUE,
					org.daiitech.naftah.parser.NaftahLexer.FALSE ->
				AttributedStyle.BOLD.foreground(AttributedStyle.GREEN);
			case org.daiitech.naftah.parser.NaftahLexer.LINE_COMMENT,
					org.daiitech.naftah.parser.NaftahLexer.BLOCK_COMMENT ->
				AttributedStyle.DEFAULT.italic().foreground(AttributedStyle.YELLOW);
			case org.daiitech.naftah.parser.NaftahLexer.PLUS, org.daiitech.naftah.parser.NaftahLexer.INCREMENT,
					org.daiitech.naftah.parser.NaftahLexer.MINUS, org.daiitech.naftah.parser.NaftahLexer.DECREMENT,
					org.daiitech.naftah.parser.NaftahLexer.MUL, org.daiitech.naftah.parser.NaftahLexer.DIV,
					org.daiitech.naftah.parser.NaftahLexer.MOD, org.daiitech.naftah.parser.NaftahLexer.ASSIGN,
					org.daiitech.naftah.parser.NaftahLexer.LT, org.daiitech.naftah.parser.NaftahLexer.GT,
					org.daiitech.naftah.parser.NaftahLexer.LE, org.daiitech.naftah.parser.NaftahLexer.GE,
					org.daiitech.naftah.parser.NaftahLexer.EQ, org.daiitech.naftah.parser.NaftahLexer.NEQ,
					org.daiitech.naftah.parser.NaftahLexer.BITWISE_NOT,
					org.daiitech.naftah.parser.NaftahLexer.BITWISE_AND,
					org.daiitech.naftah.parser.NaftahLexer.BITWISE_OR,
					org.daiitech.naftah.parser.NaftahLexer.BITWISE_XOR,
					org.daiitech.naftah.parser.NaftahLexer.ELEMENTWISE_PLUS,
					org.daiitech.naftah.parser.NaftahLexer.ELEMENTWISE_MINUS,
					org.daiitech.naftah.parser.NaftahLexer.ELEMENTWISE_MUL,
					org.daiitech.naftah.parser.NaftahLexer.ELEMENTWISE_DIV,
					org.daiitech.naftah.parser.NaftahLexer.ELEMENTWISE_MOD ->
				AttributedStyle.BOLD.foreground(AttributedStyle.RED);
			case org.daiitech.naftah.parser.NaftahLexer.LPAREN, org.daiitech.naftah.parser.NaftahLexer.RPAREN,
					org.daiitech.naftah.parser.NaftahLexer.LBRACE, org.daiitech.naftah.parser.NaftahLexer.RBRACE,
					org.daiitech.naftah.parser.NaftahLexer.LBRACK, org.daiitech.naftah.parser.NaftahLexer.RBRACK,
					org.daiitech.naftah.parser.NaftahLexer.SEMI, org.daiitech.naftah.parser.NaftahLexer.COLON,
					org.daiitech.naftah.parser.NaftahLexer.DOT, org.daiitech.naftah.parser.NaftahLexer.COMMA,
					org.daiitech.naftah.parser.NaftahLexer.QuotationMark,
					org.daiitech.naftah.parser.NaftahLexer.DoubleQuotationMark,
					org.daiitech.naftah.parser.NaftahLexer.DoubleQuotationMarkLeft,
					org.daiitech.naftah.parser.NaftahLexer.DoubleQuotationMarkRight,
					org.daiitech.naftah.parser.NaftahLexer.PUNCTUATION ->
				AttributedStyle.BOLD.foreground(AttributedStyle.CYAN);
			default -> AttributedStyle.BOLD.foreground(AttributedStyle.WHITE);
		};
	}

	/**
	 * Aligns the given attributed string to the right side of the terminal,
	 * applying appropriate spacing and appending the prompt.
	 *
	 * @param str   the input string to align
	 * @param width the total width of the terminal line
	 * @return a right-aligned {@link AttributedString}
	 */
	private AttributedString rightAlign(AttributedString str, int width) {
		int contentWidth = str.columnLength() + (MULTILINE_IS_ACTIVE ? 12 : 8); // text - prompt length
		int padding = Math.max(0, width - contentWidth);
		AttributedString spacePad = new AttributedString(" ".repeat(padding));
		AttributedString prompt = MULTILINE_IS_ACTIVE ? new AttributedString(RTL_MULTILINE_PROMPT) : new AttributedString(RTL_PROMPT);
		return AttributedString.join(new AttributedString(""), spacePad, str, prompt);
	}
}
