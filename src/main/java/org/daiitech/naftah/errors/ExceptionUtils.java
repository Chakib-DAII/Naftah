package org.daiitech.naftah.errors;

import org.antlr.v4.runtime.Parser;

import static org.daiitech.naftah.parser.NaftahParserHelper.getFormattedTokenSymbols;

public class ExceptionUtils {
	public static Throwable getRootCause(Throwable original) {
		if (original == null) {
			return null;
		}
		else {
			Throwable rootCause = null;

			for (Throwable cause = original.getCause(); cause != null && cause != rootCause; cause = cause.getCause()) {
				rootCause = cause;
			}

			return rootCause;
		}
	}

	public static Throwable getMostSpecificCause(Throwable original) {
		Throwable rootCause = getRootCause(original);
		return rootCause != null ? rootCause : original;
	}

	public static NaftahBugError newNaftahBugInvalidLoopLabelError(String label, Parser parser) {
		return new NaftahBugError(String.format("لا يمكن استخدام تسمية الحلقة نفسها '%s' في جملة '%s'.", label, getFormattedTokenSymbols(parser.getVocabulary(), org.daiitech.naftah.parser.NaftahLexer.BREAK, false)));
	}
}
