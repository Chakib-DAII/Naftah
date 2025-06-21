package org.daiitech.naftah.utils.jline;

import com.ibm.icu.text.ArabicShapingException;
import org.jline.reader.Candidate;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.utils.AttributedString;

import java.util.ArrayList;

import static org.daiitech.naftah.utils.arabic.ArabicUtils.shape;

/**
 * @author Chakib Daii
 **/
public class ArabicStringsCompleter extends StringsCompleter {

    public ArabicStringsCompleter(Iterable<String> strings) {
        assert strings != null;
        this.candidates = new ArrayList<>();
        for (String string : strings) {
            String display = string;
            try {
                display = shape(display);
            } catch (ArabicShapingException e) {
                // do nothing
            }
            candidates.add(new Candidate(AttributedString.stripAnsi(string), display, null, null, null, null, true));
        }
    }
}
