package org.daiitech.naftah.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.daiitech.naftah.builtin.utils.tuple.ImmutablePair;
import org.daiitech.naftah.builtin.utils.tuple.Pair;
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
import static org.daiitech.naftah.utils.repl.REPLHelper.TEXT_PASTE_DETECTED;
import static org.daiitech.naftah.utils.repl.REPLHelper.println;
import static org.daiitech.naftah.utils.repl.REPLHelper.rightAlign;

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

		long linesCount = Arrays.stream(buffer.split(ESCAPE_CHARS_REGEX)).filter(s -> !s.isBlank()).count();
		if (linesCount > 1) {
			if (!TEXT_PASTE_DETECTED) {
				throw new EOFError(-1, -1, "Escaped new line", "newline");
			}
			MULTILINE_IS_ACTIVE = true;
			println(reader);
		}


		// Remove escaped newlines
		buffer = buffer.replaceAll(ESCAPE_CHARS_REGEX, "");

		String[] bufferLines = buffer.split("\\r?\\n");

		List<Pair<CharSequence, AttributedStyle>> styledSegments = new ArrayList<>();

		for (String line : bufferLines) {
			if (line.isBlank()) {
				continue;
			}

			// Remove escaped characters before tokenization
			String cleanedLine = line.replaceAll(ESCAPE_CHARS_REGEX, "");

			// Create input stream from buffer
			CharStream input = CharStreams.fromString(cleanedLine);

			// Tokenize this line
			CommonTokenStream tokens = NaftahParserHelper.getCommonTokenStream(input);
			tokens.fill();

			int lastIndex = 0;

			for (Token token : tokens.getTokens()) {
				if (token.getType() == -1 || token.getText() == null) {
					continue;
				}

				int tokenStartIndex = token.getStartIndex();
				int tokenStopIndex = token.getStopIndex();

				// Add unmatched text before this token
				if (tokenStartIndex > lastIndex && lastIndex >= 0 && tokenStartIndex <= cleanedLine.length()) {
					String gapText = cleanedLine.substring(lastIndex, tokenStartIndex);
					styledSegments.add(ImmutablePair.of(gapText, AttributedStyle.DEFAULT));
				}

				// Get style for token type
				AttributedStyle style = getStyleForTokenType(token.getType());
				String shapedText = token.getText();
				try {
					shapedText = shape(shapedText);
				}
				catch (Exception e) {
					// fallback to original
				}

				styledSegments.add(ImmutablePair.of(shapedText, style));
				lastIndex = tokenStopIndex + 1;
			}

			// Add unmatched trailing text
			if (lastIndex < cleanedLine.length()) {
				String trailingText = cleanedLine.substring(lastIndex);
				styledSegments.add(ImmutablePair.of(trailingText, AttributedStyle.DEFAULT));
			}

			// Explicitly add newline after each logical line
			styledSegments.add(ImmutablePair.of("\n", AttributedStyle.DEFAULT));
		}

		int terminalWidth = Integer.getInteger(TERMINAL_WIDTH_PROPERTY, 80); // fallback to 80
		List<AttributedString> lines = new ArrayList<>();
		AttributedStringBuilder currentLine = new AttributedStringBuilder();
		int currentLineWidth = 0;

		if (shouldReshape()) {
			// Reverse for RTL visual order (not buffer)
			Collections.reverse(styledSegments);
		}

		// Build lines with wrapping and right-alignment
		for (Pair<CharSequence, AttributedStyle> part : styledSegments) {
			String fragText = part.getLeft().toString();
			AttributedString fragment = new AttributedString(fragText, part.getRight());
			int fragWidth = fragment.columnLength();

			if (fragText.equals("\n") || currentLineWidth + fragWidth > terminalWidth) {
				if (!currentLine.isEmpty()) {
					// Right-align and add current line
					lines.add(rightAlign(currentLine.toAttributedString(), terminalWidth));
					currentLine = new AttributedStringBuilder();
					currentLineWidth = 0;
				}
				if (fragText.equals("\n")) {
					continue; // skip newline fragment
				}
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
					org.daiitech.naftah.parser.NaftahLexer.DOWNTO, org.daiitech.naftah.parser.NaftahLexer.STEP,
					org.daiitech.naftah.parser.NaftahLexer.IN, org.daiitech.naftah.parser.NaftahLexer.IF,
					org.daiitech.naftah.parser.NaftahLexer.ELSEIF, org.daiitech.naftah.parser.NaftahLexer.ELSE,
					org.daiitech.naftah.parser.NaftahLexer.END, org.daiitech.naftah.parser.NaftahLexer.FOR,
					org.daiitech.naftah.parser.NaftahLexer.FUNCTION, org.daiitech.naftah.parser.NaftahLexer.VARIABLE,
					org.daiitech.naftah.parser.NaftahLexer.CONSTANT, org.daiitech.naftah.parser.NaftahLexer.VOID,
					org.daiitech.naftah.parser.NaftahLexer.NULL, org.daiitech.naftah.parser.NaftahLexer.NAN,
					org.daiitech.naftah.parser.NaftahLexer.NOT, org.daiitech.naftah.parser.NaftahLexer.REPEAT,
					org.daiitech.naftah.parser.NaftahLexer.RETURN, org.daiitech.naftah.parser.NaftahLexer.THEN,
					org.daiitech.naftah.parser.NaftahLexer.UNTIL, org.daiitech.naftah.parser.NaftahLexer.WHILE,
					org.daiitech.naftah.parser.NaftahLexer.CASE, org.daiitech.naftah.parser.NaftahLexer.OF,
					org.daiitech.naftah.parser.NaftahLexer.TRY, org.daiitech.naftah.parser.NaftahLexer.ARROW,
					org.daiitech.naftah.parser.NaftahLexer.IMPORT, org.daiitech.naftah.parser.NaftahLexer.AS,
					org.daiitech.naftah.parser.NaftahLexer.ASYNC, org.daiitech.naftah.parser.NaftahLexer.SPAWN,
					org.daiitech.naftah.parser.NaftahLexer.AWAIT, org.daiitech.naftah.parser.NaftahLexer.SCOPE,
					org.daiitech.naftah.parser.NaftahLexer.CHANNEL, org.daiitech.naftah.parser.NaftahLexer.ACTOR,
					org.daiitech.naftah.parser.NaftahLexer.IMPLEMENTATION,
					org.daiitech.naftah.parser.NaftahLexer.SELF -> AttributedStyle.BOLD
							.foreground(AttributedStyle.BLUE);
			case org.daiitech.naftah.parser.NaftahLexer.VAR, org.daiitech.naftah.parser.NaftahLexer.BOOLEAN,
					org.daiitech.naftah.parser.NaftahLexer.STRING_TYPE, org.daiitech.naftah.parser.NaftahLexer.CHAR,
					org.daiitech.naftah.parser.NaftahLexer.BYTE, org.daiitech.naftah.parser.NaftahLexer.SHORT,
					org.daiitech.naftah.parser.NaftahLexer.INT, org.daiitech.naftah.parser.NaftahLexer.BIG_INT,
					org.daiitech.naftah.parser.NaftahLexer.LONG, org.daiitech.naftah.parser.NaftahLexer.FLOAT,
					org.daiitech.naftah.parser.NaftahLexer.DOUBLE, org.daiitech.naftah.parser.NaftahLexer.BIG_DECIMAL,
					org.daiitech.naftah.parser.NaftahLexer.VAR_NUMBER, org.daiitech.naftah.parser.NaftahLexer.STRUCT,
					org.daiitech.naftah.parser.NaftahLexer.DURATION, org.daiitech.naftah.parser.NaftahLexer.PERIOD,
					org.daiitech.naftah.parser.NaftahLexer.PERIOD_DURATION, org.daiitech.naftah.parser.NaftahLexer.DATE,
					org.daiitech.naftah.parser.NaftahLexer.TIME, org.daiitech.naftah.parser.NaftahLexer.DATE_TIME,
					org.daiitech.naftah.parser.NaftahLexer.PAIR, org.daiitech.naftah.parser.NaftahLexer.LIST,
					org.daiitech.naftah.parser.NaftahLexer.TUPLE, org.daiitech.naftah.parser.NaftahLexer.SET,
					org.daiitech.naftah.parser.NaftahLexer.MAP -> AttributedStyle.BOLD
							.foreground(AttributedStyle.MAGENTA);
			case org.daiitech.naftah.parser.NaftahLexer.CHARACTER, org.daiitech.naftah.parser.NaftahLexer.STRING,
					org.daiitech.naftah.parser.NaftahLexer.NUMBER, org.daiitech.naftah.parser.NaftahLexer.BASE_DIGITS,
					org.daiitech.naftah.parser.NaftahLexer.TRUE, org.daiitech.naftah.parser.NaftahLexer.FALSE ->
				AttributedStyle.BOLD.foreground(AttributedStyle.GREEN);
			case org.daiitech.naftah.parser.NaftahLexer.LINE_COMMENT,
					org.daiitech.naftah.parser.NaftahLexer.BLOCK_COMMENT ->
				AttributedStyle.DEFAULT.italic().foreground(AttributedStyle.BRIGHT);
			case org.daiitech.naftah.parser.NaftahLexer.PLUS, org.daiitech.naftah.parser.NaftahLexer.INCREMENT,
					org.daiitech.naftah.parser.NaftahLexer.MINUS, org.daiitech.naftah.parser.NaftahLexer.DECREMENT,
					org.daiitech.naftah.parser.NaftahLexer.MUL, org.daiitech.naftah.parser.NaftahLexer.DIV,
					org.daiitech.naftah.parser.NaftahLexer.MOD, org.daiitech.naftah.parser.NaftahLexer.ASSIGN,
					org.daiitech.naftah.parser.NaftahLexer.LT, org.daiitech.naftah.parser.NaftahLexer.GT,
					org.daiitech.naftah.parser.NaftahLexer.LE, org.daiitech.naftah.parser.NaftahLexer.GE,
					org.daiitech.naftah.parser.NaftahLexer.EQ, org.daiitech.naftah.parser.NaftahLexer.NEQ,
					org.daiitech.naftah.parser.NaftahLexer.POW,
					org.daiitech.naftah.parser.NaftahLexer.BITWISE_NOT,
					org.daiitech.naftah.parser.NaftahLexer.BITWISE_AND,
					org.daiitech.naftah.parser.NaftahLexer.BITWISE_OR,
					org.daiitech.naftah.parser.NaftahLexer.BITWISE_XOR,
					org.daiitech.naftah.parser.NaftahLexer.ELEMENTWISE_PLUS,
					org.daiitech.naftah.parser.NaftahLexer.ELEMENTWISE_MINUS,
					org.daiitech.naftah.parser.NaftahLexer.ELEMENTWISE_MUL,
					org.daiitech.naftah.parser.NaftahLexer.ELEMENTWISE_DIV,
					org.daiitech.naftah.parser.NaftahLexer.ELEMENTWISE_MOD,
					org.daiitech.naftah.parser.NaftahLexer.BASE_RADIX,
					org.daiitech.naftah.parser.NaftahLexer.RAW,
					org.daiitech.naftah.parser.NaftahLexer.BYTE_ARRAY,
					org.daiitech.naftah.parser.NaftahLexer.TEMPORAL_POINT,
					org.daiitech.naftah.parser.NaftahLexer.TEMPORAL_AMOUNT,
					org.daiitech.naftah.parser.NaftahLexer.OK, org.daiitech.naftah.parser.NaftahLexer.ERROR,
					org.daiitech.naftah.parser.NaftahLexer.SOME, org.daiitech.naftah.parser.NaftahLexer.NONE,
					org.daiitech.naftah.parser.NaftahLexer.ORDERED -> AttributedStyle.BOLD
							.foreground(AttributedStyle.RED);
			case org.daiitech.naftah.parser.NaftahLexer.LPAREN, org.daiitech.naftah.parser.NaftahLexer.RPAREN,
					org.daiitech.naftah.parser.NaftahLexer.LBRACE, org.daiitech.naftah.parser.NaftahLexer.RBRACE,
					org.daiitech.naftah.parser.NaftahLexer.LBRACK, org.daiitech.naftah.parser.NaftahLexer.RBRACK,
					org.daiitech.naftah.parser.NaftahLexer.SEMI, org.daiitech.naftah.parser.NaftahLexer.COLON,
					org.daiitech.naftah.parser.NaftahParser.QUESTION, org.daiitech.naftah.parser.NaftahLexer.DOT,
					org.daiitech.naftah.parser.NaftahLexer.COMMA, org.daiitech.naftah.parser.NaftahLexer.QuotationMark,
					org.daiitech.naftah.parser.NaftahLexer.DoubleQuotationMark,
					org.daiitech.naftah.parser.NaftahLexer.DoubleQuotationMarkLeft,
					org.daiitech.naftah.parser.NaftahLexer.DoubleQuotationMarkRight,
					org.daiitech.naftah.parser.NaftahLexer.PUNCTUATION,
					org.daiitech.naftah.parser.NaftahLexer.HASH_SIGN,
					org.daiitech.naftah.parser.NaftahLexer.AT_SIGN,
					org.daiitech.naftah.parser.NaftahLexer.DOLLAR_SIGN,
					org.daiitech.naftah.parser.NaftahLexer.STAR_SIGN,
					org.daiitech.naftah.parser.NaftahLexer.LT_SIGN,
					org.daiitech.naftah.parser.NaftahLexer.GT_SIGN ->
				AttributedStyle.BOLD.foreground(AttributedStyle.CYAN);
			case org.daiitech.naftah.parser.NaftahLexer.ID -> AttributedStyle.BOLD.foreground(AttributedStyle.YELLOW);
			default -> AttributedStyle.BOLD.foreground(AttributedStyle.WHITE);
		};
	}

}
