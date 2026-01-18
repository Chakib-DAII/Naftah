// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

package org.daiitech.naftah.utils.repl;

import java.util.Objects;
import java.util.regex.Pattern;

import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

/**
 * An abstract base class for implementing custom {@link Highlighter} logic
 * while preserving behavior from an original highlighter.
 * <p>
 * It delegates highlighting operations to the original highlighter by default,
 * and provides a utility method {@link #merge(AttributedString, AttributedString)}
 * for combining two attributed strings with merged styles.
 * </p>
 *
 * @author Chakib Daii
 */
public abstract class BaseHighlighter implements Highlighter {

	/**
	 * The original highlighter to delegate to.
	 */
	protected final Highlighter originalHighlighter;

	/**
	 * Constructs a new BaseHighlighter with the given original highlighter.
	 *
	 * @param originalHighlighter the highlighter to delegate to
	 */
	public BaseHighlighter(Highlighter originalHighlighter) {
		this.originalHighlighter = originalHighlighter;
	}

	/**
	 * Merges two {@link AttributedString} objects, combining their character styles.
	 * If one string is longer than the other, the remaining portion is appended as-is.
	 *
	 * @param base    the base attributed string (can be {@code null})
	 * @param overlay the string to overlay on top of the base
	 * @return a new merged {@link AttributedString}
	 */
	public static AttributedString merge(AttributedString base, AttributedString overlay) {
		if (Objects.isNull(base)) {
			return overlay;
		}

		int length = Math.min(base.length(), overlay.length());
		AttributedStringBuilder builder = new AttributedStringBuilder();

		for (int i = 0; i < length; i++) {
			char c = base.charAt(i);
			AttributedStyle baseStyle = base.styleAt(i);
			AttributedStyle overlayStyle = overlay.styleAt(i);

			AttributedStyle style = new AttributedStyle(
														baseStyle.getStyle() | overlayStyle.getStyle(),
														baseStyle.getMask() | overlayStyle.getMask()
			);

			builder.append(String.valueOf(c), style);
		}

		// Handle any extra content in base
		if (base.length() > length) {
			builder.append(base.subSequence(length, base.length()));
		}
		if (overlay.length() > length) {
			builder.append(overlay.subSequence(length, overlay.length()));
		}

		return builder.toAttributedString();
	}

	/**
	 * Highlights the given input buffer using the original highlighter.
	 *
	 * @param reader the line reader
	 * @param buffer the input string to highlight
	 * @return an {@link AttributedString} with applied highlighting
	 */
	@Override
	public AttributedString highlight(LineReader reader, String buffer) {
		return originalHighlighter.highlight(reader, buffer);
	}

	/**
	 * Sets the pattern that identifies errors in the input.
	 *
	 * @param errorPattern the regular expression pattern
	 */
	@Override
	public void setErrorPattern(Pattern errorPattern) {
		originalHighlighter.setErrorPattern(errorPattern);
	}

	/**
	 * Sets the index in the buffer that caused a parsing error.
	 *
	 * @param errorIndex the index of the error
	 */
	@Override
	public void setErrorIndex(int errorIndex) {
		originalHighlighter.setErrorIndex(errorIndex);
	}
}
