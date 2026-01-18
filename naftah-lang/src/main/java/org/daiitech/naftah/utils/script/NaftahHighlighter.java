// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

package org.daiitech.naftah.utils.script;

import org.daiitech.naftah.utils.repl.BaseHighlighter;
import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.utils.AttributedString;

import static org.daiitech.naftah.NaftahSystem.TERMINAL_WIDTH_PROPERTY;
import static org.daiitech.naftah.utils.repl.REPLHelper.rightAlign;
import static org.daiitech.naftah.utils.script.ScriptUtils.containsArabicLetters;
import static org.daiitech.naftah.utils.script.ScriptUtils.shape;

/**
 * A highlighter implementation that reshapes text for correct display.
 * <p>
 * Extends {@link BaseHighlighter} to wrap an original highlighter and
 * applies text shaping on the input buffer if needed.
 * </p>
 * <p>
 * The highlighting process checks if reshaping is required and if the input contains
 * characters. If so, it reshapes the text for proper visual representation,
 * then merges the reshaped text style with the original highlight styles.
 * </p>
 *
 * @author Chakib Daii
 */
public class NaftahHighlighter extends BaseHighlighter {

	/**
	 * Constructs an NaftahHighlighter wrapping the specified original highlighter.
	 *
	 * @param originalHighlighter the underlying highlighter to delegate to
	 */
	public NaftahHighlighter(Highlighter originalHighlighter) {
		super(originalHighlighter);
	}

	/**
	 * Highlights the input buffer, reshaping text if applicable.
	 *
	 * @param reader the {@link LineReader} used for reading input
	 * @param buffer the input text to be highlighted
	 * @return an {@link AttributedString} representing the highlighted and reshaped text
	 */
	@Override
	public AttributedString highlight(LineReader reader, String buffer) {
		if (buffer.isBlank()) {
			return new AttributedString(buffer);
		}

		int terminalWidth = Integer.getInteger(TERMINAL_WIDTH_PROPERTY);

		AttributedString attributedString = super.highlight(reader, buffer);

		if (containsArabicLetters(buffer)) {
			try {
				String reshaped = shape(buffer); // display only
				attributedString = rightAlign(new AttributedString(reshaped), terminalWidth);
			}
			catch (Exception e) {
				// do nothing
			}
		}
		return attributedString;
	}
}
