package org.daiitech.naftah.utils.arabic;

import static org.daiitech.naftah.utils.arabic.ArabicUtils.containsArabic;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.shape;

import com.ibm.icu.text.ArabicShapingException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * @author Chakib Daii
 */
public class ArabicOutputTransformer extends OutputStream {
  private final OutputStream original;

  public ArabicOutputTransformer(OutputStream original) {
    this.original = original;
  }

  @Override
  public void write(int b) throws IOException {
    original.write(b); // fallback for single bytes
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    String raw = new String(b, off, len, StandardCharsets.UTF_8);
    if (containsArabic(raw)) {
      try {
        String display = shape(raw);

        original.write(display.getBytes(StandardCharsets.UTF_8));
      } catch (ArabicShapingException e) {
        original.write(b, off, len); // fallback
      }
    } else original.write(b, off, len); // fallback
  }

  public static PrintStream getPrintStream(OutputStream original) {
    OutputStream out = new ArabicOutputTransformer(original);
    return new PrintStream(out, true, StandardCharsets.UTF_8);
  }
}
