package org.daiitech.naftah.utils.repl;

import java.util.ArrayList;

import org.jline.reader.Candidate;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.utils.AttributedString;

import static org.daiitech.naftah.utils.script.ScriptUtils.shape;

/**
 * A custom {@link StringsCompleter} implementation that supports shaping text
 * for better display in terminals that require right-to-left formatting.
 * <p>
 * This completer processes a collection of strings and optionally reshapes them
 * using a utility method (e.g., for contextual script display).
 * </p>
 *
 * <p>
 * Each input string is added as a {@link Candidate}, with reshaping applied if necessary.
 * ANSI escape codes are stripped from the completion value but not the display value.
 * </p>
 *
 * @author Chakib Daii
 */
public class NaftahStringsCompleter extends StringsCompleter {

	/**
	 * Constructs a new {@code NaftahStringsCompleter} using the given iterable of strings.
	 * If text shaping is enabled, each string will be reshaped for display.
	 *
	 * @param strings an iterable collection of string candidates for completion (must not be {@code null})
	 * @throws AssertionError if {@code strings} is {@code null}
	 */
	public NaftahStringsCompleter(Iterable<String> strings) {
		assert strings != null;
		this.candidates = new ArrayList<>();
		for (String string : strings) {
			String display = string;
			try {
				display = shape(display);
			}
			catch (Exception e) {
				// do nothing
			}
			candidates
					.add(new Candidate(
										AttributedString.stripAnsi(string),
										display,
										null,
										null,
										null,
										null,
										true
					));
		}
	}
}
