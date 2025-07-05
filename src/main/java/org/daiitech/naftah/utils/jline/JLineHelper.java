package org.daiitech.naftah.utils.jline;

import org.jline.reader.LineReader;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp;

public final class JLineHelper {

  public JLineHelper() {
    throw new IllegalStateException("Illegal usage.");
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
