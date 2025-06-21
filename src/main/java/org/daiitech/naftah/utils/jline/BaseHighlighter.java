package org.daiitech.naftah.utils.jline;

import java.util.Objects;
import java.util.regex.Pattern;
import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

/**
 * @author Chakib Daii
 */
public abstract class BaseHighlighter implements Highlighter {
  protected Highlighter originalHighlighter;

  public BaseHighlighter(Highlighter originalHighlighter) {
    this.originalHighlighter = originalHighlighter;
  }

  @Override
  public AttributedString highlight(LineReader reader, String buffer) {
    return originalHighlighter.highlight(reader, buffer);
  }

  @Override
  public void setErrorPattern(Pattern errorPattern) {
    originalHighlighter.setErrorPattern(errorPattern);
  }

  @Override
  public void setErrorIndex(int errorIndex) {
    originalHighlighter.setErrorIndex(errorIndex);
  }

  public static AttributedString merge(AttributedString base, AttributedString overlay) {
    if (Objects.isNull(base)) return overlay;

    int length = Math.min(base.length(), overlay.length());
    AttributedStringBuilder builder = new AttributedStringBuilder();

    for (int i = 0; i < length; i++) {
      char c = base.charAt(i);
      AttributedStyle baseStyle = base.styleAt(i);
      AttributedStyle overlayStyle = overlay.styleAt(i);

      AttributedStyle style =
          new AttributedStyle(
              baseStyle.getStyle() | overlayStyle.getStyle(),
              baseStyle.getMask() | overlayStyle.getMask());

      builder.append(String.valueOf(c), style);
    }

    // Handle any extra content in base
    if (base.length() > length) {
      builder.append(base.subSequence(length, base.length()));
    }
    if (overlay.length() > length) {
      builder.append(overlay.subSequence(length, overlay.length()));
    }

    return builder.toAttributedString();
  }
}
