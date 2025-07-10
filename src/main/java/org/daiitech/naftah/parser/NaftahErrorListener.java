package org.daiitech.naftah.parser;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import static org.daiitech.naftah.utils.arabic.ArabicUtils.fillRightWithSpaces;

/**
 * @author Chakib Daii
 */
public class NaftahErrorListener extends BaseErrorListener {
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
                            Object offendingSymbol,
                            int line,
                            int charPositionInLine,
                            String msg,
                            RecognitionException e) {

        String offendingText = "";

        if (offendingSymbol instanceof Token token) {
            offendingText = token.getText();
        }

        String fullMessage = String.format("""
        💥 خطأ في بناء الجملة (Syntax Error)!
        📍 السطر: %d، العمود: %d
        %s📄 الرسالة: %s
        """,
                line,
                charPositionInLine,
                offendingText.isBlank() ? "" : String.format("🔴 الرمز غير الصحيح: '%s'\n", offendingText),
                translateMessage(msg)
        );

        System.err.println( fillRightWithSpaces(fullMessage));

        // Terminate program
        throw new ParseCancellationException("خطأ في بناء الجملة. تم إيقاف التنفيذ.");
    }

    // Optional: Arabic translation for common error phrases
    private String translateMessage(String msg) {
        if (msg.contains("mismatched input")) {
            return msg.replace("mismatched input", "إدخال غير متطابق");
        } else if (msg.contains("missing")) {
            return msg.replace("missing", "مفقود");
        } else if (msg.contains("no viable alternative")) {
            return msg.replace("no viable alternative at input", "لا يوجد بديل صالح عند الإدخال");
        } else if (msg.contains("token recognition error at:")) {
            return msg.replace("token recognition error at:", "خطأ في التعرف على الرمز:");
        }
        return msg; // fallback to raw message
    }
}
