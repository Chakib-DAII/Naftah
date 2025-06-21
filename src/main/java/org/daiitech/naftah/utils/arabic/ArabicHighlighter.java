package org.daiitech.naftah.utils.arabic;

import static org.daiitech.naftah.utils.arabic.ArabicUtils.containsArabic;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.shape;

import com.ibm.icu.text.ArabicShapingException;
import org.daiitech.naftah.utils.jline.BaseHighlighter;
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
    if (containsArabic(buffer)) {
      try {
        buffer = shape(buffer);
      } catch (ArabicShapingException e) {
        // do nothing
      }
    }
    return super.highlight(reader, buffer);
  }
}
