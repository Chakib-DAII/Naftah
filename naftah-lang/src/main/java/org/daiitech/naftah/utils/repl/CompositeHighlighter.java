// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

package org.daiitech.naftah.utils.repl;

import java.util.ArrayList;
import java.util.List;

import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.utils.AttributedString;

/**
 * A composite implementation of {@link Highlighter} that applies multiple highlighters in sequence
 * and merges their results. If no additional highlighters are provided, it delegates to the original highlighter.
 *
 * <p>This class uses the builder pattern to allow fluent composition of multiple highlighters.</p>
 * <p>
 * Example usage:
 * <pre>
 * Highlighter original = ...;
 * Highlighter custom1 = ...;
 * Highlighter custom2 = ...;
 *
 * CompositeHighlighter highlighter = CompositeHighlighter.builder(original)
 * .add(custom1)
 * .add(custom2)
 * .build();
 * </pre>
 *
 * @author Chakib Daii
 */
public class CompositeHighlighter extends BaseHighlighter {

	/**
	 * List of highlighters to apply in sequence.
	 */
	private final List<Highlighter> highlighters;

	/**
	 * Constructs a CompositeHighlighter with the original highlighter and a list of additional highlighters.
	 *
	 * @param originalHighlighter the original highlighter to delegate to if no others are specified
	 * @param highlighters        a list of highlighters to apply in sequence
	 */
	public CompositeHighlighter(Highlighter originalHighlighter, List<Highlighter> highlighters) {
		super(originalHighlighter);
		this.highlighters = highlighters;
	}

	/**
	 * Creates a new builder for a CompositeHighlighter.
	 *
	 * @param originalHighlighter the original highlighter
	 * @return a new Builder instance
	 */
	public static Builder builder(Highlighter originalHighlighter) {
		return new Builder(originalHighlighter);
	}

	/**
	 * Applies each highlighter in the list to the given input line and merges the results.
	 *
	 * @param reader the LineReader instance
	 * @param buffer the input line to highlight
	 * @return an AttributedString representing the highlighted line
	 */
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

	/**
	 * Builder class for creating a CompositeHighlighter instance using a fluent interface.
	 */
	public static class Builder {

		/**
		 * The original highlighter.
		 */
		private final Highlighter originalHighlighter;

		/**
		 * List of highlighters to add to the composite.
		 */
		private final List<Highlighter> highlighters = new ArrayList<>();

		/**
		 * Creates a new Builder with the specified original highlighter.
		 *
		 * @param originalHighlighter the base highlighter
		 */
		public Builder(Highlighter originalHighlighter) {
			this.originalHighlighter = originalHighlighter;
		}

		/**
		 * Adds a highlighter to the composite.
		 *
		 * @param highlighter the highlighter to add
		 * @return this builder instance
		 */
		public Builder add(Highlighter highlighter) {
			if (highlighter != null) {
				highlighters.add(highlighter);
			}
			return this;
		}

		/**
		 * Builds the CompositeHighlighter with the specified highlighters.
		 *
		 * @return a new CompositeHighlighter instance
		 */
		public CompositeHighlighter build() {
			return new CompositeHighlighter(originalHighlighter, new ArrayList<>(highlighters));
		}
	}
}
