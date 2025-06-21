package org.daiitech.naftah.utils.arabic;

import com.ibm.icu.text.ArabicShapingException;
import org.daiitech.naftah.utils.jline.BaseHighlighter;
import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.utils.AttributedString;

import static org.daiitech.naftah.utils.arabic.ArabicUtils.*;

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
      } catch (ArabicShapingException e) {
        // do nothing
      }
    }
    return attributedString;
  }
}
