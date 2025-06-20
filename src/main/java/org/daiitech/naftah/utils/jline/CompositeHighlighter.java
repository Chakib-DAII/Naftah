package org.daiitech.naftah.utils.jline;

import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Chakib Daii
 **/
public class CompositeHighlighter extends BaseHighlighter {

    private final List<Highlighter> highlighters;
    public CompositeHighlighter(Highlighter originalHighlighter, List<Highlighter> highlighters) {
        super(originalHighlighter);
        this.highlighters = highlighters;
    }

    @Override
    public AttributedString highlight(LineReader reader, String buffer) {
        if (highlighters.isEmpty()) {
            return super.highlight(reader, buffer);
        }

        AttributedStringBuilder asb = new AttributedStringBuilder(highlighters.size());

        for (Highlighter h : highlighters) {
            AttributedString highlighted = h.highlight(reader, buffer);
            asb.append(highlighted);
        }

        return asb.toAttributedString();
    }

    // Builder static inner class
    public static class Builder {
        private final Highlighter originalHighlighter;
        private final List<Highlighter> highlighters = new ArrayList<>();

        public Builder(Highlighter originalHighlighter) {
            this.originalHighlighter = originalHighlighter;
        }

        public Builder add(Highlighter highlighter) {
            if (highlighter != null) {
                highlighters.add(highlighter);
            }
            return this;
        }

        public CompositeHighlighter build() {
            return new CompositeHighlighter(originalHighlighter, new ArrayList<>(highlighters));
        }
    }

    public static Builder builder(Highlighter originalHighlighter) {
        return new Builder(originalHighlighter);
    }
}
