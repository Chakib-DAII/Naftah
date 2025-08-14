package org.daiitech.naftah.utils.arabic;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.daiitech.naftah.utils.arabic.ArabicUtils.containsArabic;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.shape;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.shouldReshape;

/**
 * An {@link OutputStream} wrapper that transforms Arabic text for proper visual display
 * when printed to an output stream (e.g., console or file).
 * <p>
 * If Arabic text is detected and reshaping is enabled, the text will be reshaped before
 * being written. Otherwise, the original content is passed through.
 *
 * <p>Note: The methods {@code shouldReshape()}, {@code containsArabic(String)},
 * and {@code shape(String)} are assumed to be implemented elsewhere.
 *
 * @author Chakib Daii
 */
public class ArabicOutputTransformer extends OutputStream {

    /**
     * The original output stream to which the transformed or raw output will be written.
     */
    private final OutputStream original;

    /**
     * Constructs a new {@code ArabicOutputTransformer} wrapping the specified output stream.
     *
     * @param original the original output stream
     */
    public ArabicOutputTransformer(OutputStream original) {
        this.original = original;
    }

    /**
     * Returns a {@link PrintStream} that wraps the provided output stream and applies Arabic text transformation.
     *
     * @param original the original output stream to wrap
     * @return a {@code PrintStream} using {@code ArabicOutputTransformer}
     */
    public static PrintStream getPrintStream(OutputStream original) {
        OutputStream out = new ArabicOutputTransformer(original);
        return new PrintStream(out, true, StandardCharsets.UTF_8);
    }

    /**
     * Writes a single byte to the original output stream.
     * This method serves as a fallback when writing raw byte data.
     *
     * @param b the byte to write
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void write(int b) throws IOException {
        original.write(b); // fallback for single bytes
    }

    /**
     * Writes a portion of a byte array to the output stream.
     * If the content contains Arabic text and reshaping is enabled, it reshapes the text before writing.
     * Otherwise, writes the original byte sequence.
     *
     * @param b   the byte array containing the data
     * @param off the start offset in the array
     * @param len the number of bytes to write
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        String raw = new String(b, off, len, StandardCharsets.UTF_8);
        if (shouldReshape() && containsArabic(raw)) {
            try {
                String display = shape(raw);

                original.write(display.getBytes(StandardCharsets.UTF_8));
            }
            catch (Exception e) {
                original.write(b, off, len); // fallback
            }
        }
        else {
            original.write(b, off, len); // fallback
        }
    }
}
