package org.daiitech.naftah.utils.jline;

import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.utils.AttributedString;

import java.util.regex.Pattern;

/**
 * @author Chakib Daii
 **/
public abstract class BaseHighlighter implements Highlighter {
    protected Highlighter originalHighlighter;

    public BaseHighlighter(Highlighter originalHighlighter) {
        this.originalHighlighter = originalHighlighter;
    }

    @Override
    public AttributedString highlight(LineReader reader, String buffer) {
        return originalHighlighter.highlight(reader, buffer);
    }

    @Override
    public void setErrorPattern(Pattern errorPattern) {
        originalHighlighter.setErrorPattern(errorPattern);
    }

    @Override
    public void setErrorIndex(int errorIndex) {
        originalHighlighter.setErrorIndex(errorIndex);
    }
}
