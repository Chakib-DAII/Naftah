package org.daiitech.naftah.utils.arabic;

import static org.daiitech.naftah.utils.arabic.ArabicUtils.*;

import org.daiitech.naftah.utils.repl.BaseHighlighter;
import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.utils.AttributedString;

/**
 * @author Chakib Daii
 */
public class ArabicHighlighter extends BaseHighlighter {

  public ArabicHighlighter(Highlighter originalHighlighter) {
    super(originalHighlighter);
  }

  @Override
  public AttributedString highlight(LineReader reader, String buffer) {
    AttributedString attributedString = super.highlight(reader, buffer);
    if (shouldReshape() && containsArabic(buffer)) {
      try {
        String reshaped = shape(buffer); // display only
        attributedString = merge(attributedString, new AttributedString(reshaped));
      } catch (Exception e) {
        // do nothing
      }
    }
    return attributedString;
  }
}
