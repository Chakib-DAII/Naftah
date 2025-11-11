package org.daiitech.naftah.utils.arabic;

import org.daiitech.naftah.utils.repl.BaseHighlighter;
import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.utils.AttributedString;

import static org.daiitech.naftah.NaftahSystem.TERMINAL_WIDTH_PROPERTY;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.containsArabic;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.shape;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.shouldReshape;
import static org.daiitech.naftah.utils.repl.REPLHelper.rightAlign;

/**
 * A highlighter implementation that reshapes Arabic text for correct display.
 * <p>
 * Extends {@link BaseHighlighter} to wrap an original highlighter and
 * applies Arabic text shaping on the input buffer if needed.
 * </p>
 * <p>
 * The highlighting process checks if reshaping is required and if the input contains
 * Arabic characters. If so, it reshapes the text for proper visual representation,
 * then merges the reshaped text style with the original highlight styles.
 * </p>
 *
 * @author Chakib Daii
 */
public class ArabicHighlighter extends BaseHighlighter {

	/**
	 * Constructs an ArabicHighlighter wrapping the specified original highlighter.
	 *
	 * @param originalHighlighter the underlying highlighter to delegate to
	 */
	public ArabicHighlighter(Highlighter originalHighlighter) {
		super(originalHighlighter);
	}

	/**
	 * Highlights the input buffer, reshaping Arabic text if applicable.
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

		if (shouldReshape() && containsArabic(buffer)) {
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
