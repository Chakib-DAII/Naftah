package org.daiitech.naftah.parser;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.daiitech.naftah.utils.jline.BaseHighlighter;
import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import static org.jline.utils.AttributedString.EMPTY;

/**
 * @author Chakib Daii
 **/
public class SyntaxHighlighter extends BaseHighlighter {
    public SyntaxHighlighter(Highlighter originalHighlighter) {
        super(originalHighlighter);
    }

    @Override
    public AttributedString highlight(LineReader reader, String buffer) {
        if (!buffer.isBlank()) {
            // Create input stream from buffer
            CharStream input = CharStreams.fromString(buffer);

            // Get all tokens from lexer
            CommonTokenStream tokens = NaftahParserHelper.getCommonTokenStream(input);
            tokens.fill();

            AttributedStringBuilder asb = new AttributedStringBuilder(tokens.size());

            for (Token token : tokens.getTokens()) {
                String text = token.getText();
                if (text == null) continue;

                int type = token.getType();

                AttributedStyle style = getStyleForTokenType(type);

                asb.append(text, style);
            }

            return asb.toAttributedString();
        }
        return EMPTY;
        }

    private AttributedStyle getStyleForTokenType(int tokenType) {
//            switch (tokenType) {
//                case org.daiitech.naftah.parser.NaftahLexer.KEYWORD:
//                    return AttributedStyle.BOLD.foreground(AttributedStyle.BLUE);
//                case org.daiitech.naftah.parser.NaftahLexer.STRING_LITERAL:
//                    return AttributedStyle.BOLD.foreground(AttributedStyle.GREEN);
//                case org.daiitech.naftah.parser.NaftahLexer.COMMENT:
//                    return AttributedStyle.italic().foreground(AttributedStyle.YELLOW);
//                case org.daiitech.naftah.parser.NaftahLexer.ERROR:
//                    return AttributedStyle.BOLD.foreground(AttributedStyle.RED);
//                default:
//                    return AttributedStyle.DEFAULT;
//            }
        return AttributedStyle.DEFAULT;
    }
}
