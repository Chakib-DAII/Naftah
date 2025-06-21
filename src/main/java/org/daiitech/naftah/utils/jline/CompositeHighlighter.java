package org.daiitech.naftah.utils.jline;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

/**
 * @author Chakib Daii
 */
public class CompositeHighlighter extends BaseHighlighter {

  private final List<Highlighter> highlighters;

  public CompositeHighlighter(Highlighter originalHighlighter, List<Highlighter> highlighters) {
    super(originalHighlighter);
    this.highlighters = highlighters;
  }

  @Override
  public AttributedString highlight(LineReader reader, String buffer) {
    if (highlighters.isEmpty()) {
      return super.highlight(reader, buffer);
    }

    AttributedString attributedString = null;

    for (Highlighter h : highlighters) {
      attributedString = merge(attributedString, h.highlight(reader, buffer));
    }

    return attributedString;
  }

  private AttributedString merge(AttributedString base, AttributedString overlay) {
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

  // Builder static inner class
  public static class Builder {
    private final Highlighter originalHighlighter;
    private final List<Highlighter> highlighters = new ArrayList<>();

    public Builder(Highlighter originalHighlighter) {
      this.originalHighlighter = originalHighlighter;
    }

    public Builder add(Highlighter highlighter) {
      if (highlighter != null) {
        highlighters.add(highlighter);
      }
      return this;
    }

    public CompositeHighlighter build() {
      return new CompositeHighlighter(originalHighlighter, new ArrayList<>(highlighters));
    }
  }

  public static Builder builder(Highlighter originalHighlighter) {
    return new Builder(originalHighlighter);
  }
}
