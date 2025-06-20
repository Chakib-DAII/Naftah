package org.daiitech.naftah.utils.arabic;

import static org.daiitech.naftah.utils.arabic.ArabicUtils.containsArabic;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.shape;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * @author Chakib Daii
 */
public class ArabicLogFormatter extends Formatter {
  private static final String LOG_MSG = """
                   [ %s ]: %s
                   """;

  @Override
  public String format(LogRecord record) {
    String msg = record.getMessage();

    if (containsArabic(msg)) {
      try {
        String visual = shape(msg);
        return LOG_MSG.formatted(record.getLevel(), visual);
      } catch (Exception e) {
        return LOG_MSG.formatted(record.getLevel(), msg); // fallback
      }
    } else
      return LOG_MSG.formatted(record.getLevel(), msg); // fallback
  }
}
