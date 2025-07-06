package org.daiitech.naftah.utils.repl;

import static org.daiitech.naftah.parser.DefaultContext.getCompletions;
import static org.daiitech.naftah.utils.ResourceUtils.getJarDirectory;
import static org.daiitech.naftah.utils.ResourceUtils.readFileLines;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.shape;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.shouldReshape;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import org.daiitech.naftah.parser.SyntaxHighlighter;
import org.jline.reader.Completer;
import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;

public final class REPLHelper {
  public static boolean MULTILINE_IS_ACTIVE = false;

  private static final String RTL_PROMPT_VALUE = "< نفطة >";
  private static final String RTL_MULTILINE_PROMPT_VALUE = "    < .... >";

  public static final String RTL_PROMPT  =
          shouldReshape() ? shape(RTL_PROMPT_VALUE) : RTL_PROMPT_VALUE; // Right-to-left mark before prompt
  public static final String RTL_MULTILINE_PROMPT =
          shouldReshape() ? shape(RTL_MULTILINE_PROMPT_VALUE) : RTL_MULTILINE_PROMPT_VALUE; // Right-to-left  multiline mark before prompt

  public REPLHelper() {
    throw new IllegalStateException("Illegal usage.");
  }

  public static Terminal getTerminal() throws IOException {
    return TerminalBuilder.builder()
        .encoding(StandardCharsets.UTF_8)
        .streams(System.in, System.out)
        .jna(true)
        .jansi(true)
        .color(true)
        .nativeSignals(true)
        .system(true)
        .exec(true)
        .build();
  }

  public static LineReader getLineReader(Terminal terminal) {
    LineReader baseReader = LineReaderBuilder.builder().terminal(terminal).build();

    Highlighter originalHighlighter = baseReader.getHighlighter();

    DefaultParser parser =
        new DefaultParser()
            .regexVariable("[\\p{L}_][\\p{L}0-9_-]*")
            .regexCommand("[:]?[\\p{L}]+[\\p{L}0-9_-]*")
            .eofOnEscapedNewLine(true)
            .eofOnUnclosedQuote(true)
            .quoteChars(new char[] {'\'', '"', '«', '»'})
            .escapeChars(new char[] {'/', '\\'});

    var lineReaderBuilder =
        LineReaderBuilder.builder()
            .terminal(terminal)
            .parser(parser)
            .highlighter(new SyntaxHighlighter(originalHighlighter));

    // Complete with fixed lexer strings and loaded builtins and Vm classes and functions
    try {
      var completions = readFileLines(getJarDirectory() + "/lexer-literals");
      var runtimeCompletions = getCompletions();
      completions.addAll(runtimeCompletions);
      Completer stringsCompleter = new ArabicStringsCompleter(completions);
      lineReaderBuilder.completer(stringsCompleter);
    } catch (IOException ignored) {
    }

    return lineReaderBuilder.build();
  }

  public static void setupHistoryConfig(LineReader reader) {

    // Set the history file
    reader.setVariable(LineReader.HISTORY_FILE, Paths.get("bin/.naftah_history"));
    reader.setVariable(LineReader.HISTORY_SIZE, 1000); // Maximum entries in memory
    reader.setVariable(LineReader.HISTORY_FILE_SIZE, 2000); // Maximum entries in file

    // Don't add duplicate entries
    reader.setOpt(LineReader.Option.HISTORY_IGNORE_DUPS);
    // Don't add entries that start with space
    reader.setOpt(LineReader.Option.HISTORY_IGNORE_SPACE);
    // Beep when trying to navigate past the end of history
    reader.setOpt(LineReader.Option.HISTORY_BEEP);
    // Verify history expansion (like !!, !$, etc.)
    reader.setOpt(LineReader.Option.HISTORY_VERIFY);
  }

  public static void print(Terminal terminal, String str) {
    terminal.writer().write(str);
  }

  public static void println(LineReader reader, String s) {
    print(reader.getTerminal(), s);
    println(reader);
  }

  /** Output a platform-dependant newline. */
  public static void println(LineReader reader) {
    reader.getTerminal().puts(InfoCmp.Capability.carriage_return);
    print(reader.getTerminal(), "\n");
    if (reader instanceof LineReaderImpl lineReader) lineReader.redrawLine();
  }
}
