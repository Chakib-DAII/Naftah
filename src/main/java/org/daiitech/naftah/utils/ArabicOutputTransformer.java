package org.daiitech.naftah.utils;

import java.io.OutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.ibm.icu.text.ArabicShapingException;

import static org.daiitech.naftah.core.utils.ArabicUtils.shape;

/**
 * @author Chakib Daii
 **/

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
        try {
            String display = shape(raw);

            original.write(display.getBytes(StandardCharsets.UTF_8));
        } catch (ArabicShapingException e) {
            original.write(b, off, len); // fallback
        }
    }
}

