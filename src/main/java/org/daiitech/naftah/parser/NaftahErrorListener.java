package org.daiitech.naftah.parser;

import static org.daiitech.naftah.parser.NaftahParserHelper.getFormattedTokenSymbols;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.padText;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.IntervalSet;
import org.antlr.v4.runtime.misc.ParseCancellationException;

/**
 * @author Chakib Daii
 */
public class NaftahErrorListener extends BaseErrorListener {
  public static final ANTLRErrorStrategy ERROR_HANDLER_INSTANCE = new BailErrorStrategy();
  public static final NaftahErrorListener INSTANCE = new NaftahErrorListener();

  @Override
  public void syntaxError(
      Recognizer<?, ?> recognizer,
      Object offendingSymbol,
      int line,
      int charPositionInLine,
      String msg,
      RecognitionException e) {

    // Extract offending text
    String offendingText = "";
    if (offendingSymbol instanceof Token token) {
      offendingText = token.getText();
    }

    // Translate message or construct better one if needed
    String translatedMessage = translateMessage(msg);

    // Handle unexpected EOF: show expected tokens
    if (msg.contains("no viable alternative at input") && "<EOF>".equals(offendingText)) {
      if (recognizer instanceof Parser parser) {
        IntervalSet expectedTokens = parser.getExpectedTokens();
        Vocabulary vocabulary = parser.getVocabulary();
        StringBuilder expected = new StringBuilder();
        for (int tokenType : expectedTokens.toArray()) {
          String formattedTokenSymbols = getFormattedTokenSymbols(vocabulary, tokenType, true);
          if (formattedTokenSymbols == null) continue;
          expected.append(formattedTokenSymbols);
        }

        translatedMessage =
            String.format(
                """
                        📄 نهاية غير متوقعة للملف. المتوقع:
                        %s
                        """,
                expected);
      } else {
        translatedMessage = "📄 نهاية غير متوقعة للملف.";
      }
    }

    // Final formatted message (Arabic text block)
    String fullMessage =
        String.format(
            """
        💥 خطأ في بناء الجملة (Syntax Error)!
        📍 السطر: %d، العمود: %d
        %s
        %s
        """,
            line,
            charPositionInLine,
            offendingText.isBlank()
                ? ""
                : String.format("🔴 الرمز غير الصحيح: '%s'\n", offendingText),
            translatedMessage);

    padText(fullMessage, true);

    // Stop execution
    throw new ParseCancellationException("خطأ في بناء الجملة. تم إيقاف التنفيذ.");
  }

  // Arabic translation for common error phrases
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
    return msg; // fallback
  }
}
