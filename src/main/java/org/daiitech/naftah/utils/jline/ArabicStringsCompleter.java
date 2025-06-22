package org.daiitech.naftah.utils.jline;

import static org.daiitech.naftah.utils.arabic.ArabicUtils.shape;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.shouldReshape;

import java.util.ArrayList;
import org.jline.reader.Candidate;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.utils.AttributedString;

/**
 * @author Chakib Daii
 */
public class ArabicStringsCompleter extends StringsCompleter {

  public ArabicStringsCompleter(Iterable<String> strings) {
    assert strings != null;
    this.candidates = new ArrayList<>();
    for (String string : strings) {
      String display = string;
      if (shouldReshape()) {
        try {
          display = shape(display);
        } catch (Exception e) {
          // do nothing
        }
      }
      candidates.add(
          new Candidate(AttributedString.stripAnsi(string), display, null, null, null, null, true));
    }
  }
}
