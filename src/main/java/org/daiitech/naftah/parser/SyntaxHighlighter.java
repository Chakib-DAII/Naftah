package org.daiitech.naftah.parser;

import static org.daiitech.naftah.NaftahSystem.TERMINAL_WIDTH_PROPERTY;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.shape;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.shouldReshape;
import static org.daiitech.naftah.utils.repl.REPLHelper.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Pair;
import org.daiitech.naftah.utils.repl.BaseHighlighter;
import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

/**
 * @author Chakib Daii
 */
public class SyntaxHighlighter extends BaseHighlighter {
  public SyntaxHighlighter(Highlighter originalHighlighter) {
    super(originalHighlighter);
  }

  @Override
  public AttributedString highlight(LineReader reader, String buffer) {
    if (buffer.isBlank()) {
      return new AttributedString(buffer);
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

      if (type == -1 || text == null) continue;

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
        } catch (Exception e) {
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

  private AttributedStyle getStyleForTokenType(int tokenType) {
    return switch (tokenType) {
      case org.daiitech.naftah.parser.NaftahLexer.AND,
          org.daiitech.naftah.parser.NaftahLexer.OR,
          org.daiitech.naftah.parser.NaftahLexer.BREAK,
          org.daiitech.naftah.parser.NaftahLexer.IF,
          org.daiitech.naftah.parser.NaftahLexer.ELSEIF,
          org.daiitech.naftah.parser.NaftahLexer.ELSE,
          org.daiitech.naftah.parser.NaftahLexer.END,
          org.daiitech.naftah.parser.NaftahLexer.FUNCTION,
          org.daiitech.naftah.parser.NaftahLexer.VARIABLE,
          org.daiitech.naftah.parser.NaftahLexer.CONSTANT,
          org.daiitech.naftah.parser.NaftahLexer.VOID,
          org.daiitech.naftah.parser.NaftahLexer.NULL,
          org.daiitech.naftah.parser.NaftahLexer.NOT,
          org.daiitech.naftah.parser.NaftahLexer.REPEAT,
          org.daiitech.naftah.parser.NaftahLexer.RETURN,
          org.daiitech.naftah.parser.NaftahLexer.THEN,
          org.daiitech.naftah.parser.NaftahLexer.UNTIL,
          org.daiitech.naftah.parser.NaftahLexer.WHILE,
          org.daiitech.naftah.parser.NaftahLexer.ID -> AttributedStyle.BOLD.foreground(
          AttributedStyle.BLUE);
      case org.daiitech.naftah.parser.NaftahLexer.VAR,
          org.daiitech.naftah.parser.NaftahLexer.BOOLEAN,
          org.daiitech.naftah.parser.NaftahLexer.STRING_TYPE,
          org.daiitech.naftah.parser.NaftahLexer.CHAR,
          org.daiitech.naftah.parser.NaftahLexer.BYTE,
          org.daiitech.naftah.parser.NaftahLexer.SHORT,
          org.daiitech.naftah.parser.NaftahLexer.INT,
          org.daiitech.naftah.parser.NaftahLexer.LONG,
          org.daiitech.naftah.parser.NaftahLexer.FLOAT,
          org.daiitech.naftah.parser.NaftahLexer.DOUBLE -> AttributedStyle.BOLD.foreground(
          AttributedStyle.MAGENTA);
      case org.daiitech.naftah.parser.NaftahLexer.CHARACTER,
          org.daiitech.naftah.parser.NaftahLexer.STRING,
          org.daiitech.naftah.parser.NaftahLexer.NUMBER,
          org.daiitech.naftah.parser.NaftahLexer.TRUE,
          org.daiitech.naftah.parser.NaftahLexer.FALSE -> AttributedStyle.BOLD.foreground(
          AttributedStyle.GREEN);
      case org.daiitech.naftah.parser.NaftahLexer.LINE_COMMENT,
          org.daiitech.naftah.parser.NaftahLexer.BLOCK_COMMENT -> AttributedStyle.DEFAULT
          .italic()
          .foreground(AttributedStyle.YELLOW);
      case org.daiitech.naftah.parser.NaftahLexer.PLUS,
          org.daiitech.naftah.parser.NaftahLexer.INCREMENT,
          org.daiitech.naftah.parser.NaftahLexer.MINUS,
          org.daiitech.naftah.parser.NaftahLexer.DECREMENT,
          org.daiitech.naftah.parser.NaftahLexer.MUL,
          org.daiitech.naftah.parser.NaftahLexer.DIV,
          org.daiitech.naftah.parser.NaftahLexer.MOD,
          org.daiitech.naftah.parser.NaftahLexer.ASSIGN,
          org.daiitech.naftah.parser.NaftahLexer.LT,
          org.daiitech.naftah.parser.NaftahLexer.GT,
          org.daiitech.naftah.parser.NaftahLexer.LE,
          org.daiitech.naftah.parser.NaftahLexer.GE,
          org.daiitech.naftah.parser.NaftahLexer.EQ,
          org.daiitech.naftah.parser.NaftahLexer.NEQ,
          org.daiitech.naftah.parser.NaftahLexer.BITWISE_NOT,
          org.daiitech.naftah.parser.NaftahLexer.BITWISE_AND,
          org.daiitech.naftah.parser.NaftahLexer.BITWISE_OR,
          org.daiitech.naftah.parser.NaftahLexer.BITWISE_XOR,
          org.daiitech.naftah.parser.NaftahLexer.ELEMENTWISE_PLUS,
          org.daiitech.naftah.parser.NaftahLexer.ELEMENTWISE_MINUS,
          org.daiitech.naftah.parser.NaftahLexer.ELEMENTWISE_MUL,
          org.daiitech.naftah.parser.NaftahLexer.ELEMENTWISE_DIV,
          org.daiitech.naftah.parser.NaftahLexer.ELEMENTWISE_MOD -> AttributedStyle.BOLD.foreground(
          AttributedStyle.RED);
      case org.daiitech.naftah.parser.NaftahLexer.LPAREN,
          org.daiitech.naftah.parser.NaftahLexer.RPAREN,
          org.daiitech.naftah.parser.NaftahLexer.LBRACE,
          org.daiitech.naftah.parser.NaftahLexer.RBRACE,
          org.daiitech.naftah.parser.NaftahLexer.LBRACK,
          org.daiitech.naftah.parser.NaftahLexer.RBRACK,
          org.daiitech.naftah.parser.NaftahLexer.SEMI,
          org.daiitech.naftah.parser.NaftahLexer.COLON,
          org.daiitech.naftah.parser.NaftahLexer.DOT,
          org.daiitech.naftah.parser.NaftahLexer.QuotationMark,
          org.daiitech.naftah.parser.NaftahLexer.DoubleQuotationMark,
          org.daiitech.naftah.parser.NaftahLexer.DoubleQuotationMarkLeft,
          org.daiitech.naftah.parser.NaftahLexer.DoubleQuotationMarkRight,
          org.daiitech.naftah.parser.NaftahLexer.PUNCTUATION -> AttributedStyle.BOLD.foreground(
          AttributedStyle.CYAN);
      default -> AttributedStyle.BOLD.foreground(AttributedStyle.WHITE);
    };
  }

  private AttributedString rightAlign(AttributedString str, int width) {
    int contentWidth = str.columnLength() + (MULTILINE_IS_ACTIVE ? 12 : 8); // text - prompt length
    int padding = Math.max(0, width - contentWidth);
    AttributedString spacePad = new AttributedString(" ".repeat(padding));
    AttributedString prompt =
        MULTILINE_IS_ACTIVE
            ? new AttributedString(RTL_MULTILINE_PROMPT)
            : new AttributedString(RTL_PROMPT);
    return AttributedString.join(new AttributedString(""), spacePad, str, prompt);
  }
}
