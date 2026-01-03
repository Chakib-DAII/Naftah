package org.daiitech.naftah.parser;

import java.io.IOException;

import org.daiitech.naftah.utils.arabic.ArabicUtils;
import org.jline.reader.EOFError;
import org.jline.reader.LineReader;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Terminal;
import org.jline.terminal.impl.DumbTerminal;
import org.jline.utils.AttributedString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.daiitech.naftah.NaftahSystem.TERMINAL_HEIGHT_PROPERTY;
import static org.daiitech.naftah.NaftahSystem.TERMINAL_WIDTH_PROPERTY;
import static org.daiitech.naftah.utils.arabic.ArabicOutputTransformer.getPrintStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SyntaxHighlighterTests {

	private Terminal terminal;
	private LineReader reader;
	private SyntaxHighlighter highlighter;

	@BeforeEach
	void setup() throws IOException {
		System.setProperty(TERMINAL_WIDTH_PROPERTY, Integer.toString(80));
		System.setProperty(TERMINAL_HEIGHT_PROPERTY, Integer.toString(24));
		terminal = new DumbTerminal(System.in, getPrintStream(System.out));
		reader = new LineReaderImpl(terminal);
		highlighter = new SyntaxHighlighter(reader.getHighlighter());
	}

	@Test
	void testBlankInputReturnsUnstyled() {
		AttributedString result = highlighter.highlight(reader, "   ");
		assertEquals("   ", result.toString());
	}

	@Test
	void testEscapedNewlineThrowsEOFError() {
		String buffer = """
						كرر_حلقة أ تعيين 1 إلى 5 إفعل { N
							إطبع(أ) N
						""";

		assertThrows(EOFError.class, () -> highlighter.highlight(reader, buffer));
	}

	@Test
	void testValidKeywordGetsStyled() {
		String buffer = "ثابت ت تعيين 2";

		AttributedString result = highlighter.highlight(reader, buffer);
		assertNotNull(result);
		if (ArabicUtils.shouldReshape()) {
			assertEquals(124, result.toAnsi().length());
		}
		else {
			assertEquals(66, result.toAnsi().length());
		}
	}

	@Test
	void testUnmatchedTextIsHandled() {
		String buffer = "invalidشسيشسيشسي";

		AttributedString result = highlighter.highlight(reader, buffer);

		assertNotNull(result);
		assertTrue(result.toString().contains("invalidشسيشسيشسي"));
	}

	@Test
	void testLineWrappingWhenExceedingTerminalWidth() {
		StringBuilder longLine = new StringBuilder();
		for (int i = 0; i < 100; i++) {
			longLine.append("ش ");
		}

		AttributedString result = highlighter.highlight(reader, longLine.toString());

		// Should contain multiple lines if wrapping is done
		String[] lines = result.toString().split("\n");
		assertTrue(lines.length > 1);
	}
}
