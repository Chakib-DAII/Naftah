package org.daiitech.naftah.utils.repl;

import java.util.ArrayList;
import java.util.List;

import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.utils.AttributedString;

/**
 * @author Chakib Daii
 */
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

		AttributedString attributedString = null;

		for (Highlighter h : highlighters) {
			attributedString = merge(attributedString, h.highlight(reader, buffer));
		}

		return attributedString;
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
