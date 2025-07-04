package org.daiitech.naftah.utils.jline;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jline.reader.EOFError;
import org.jline.reader.ParsedLine;
import org.jline.reader.impl.DefaultParser;

public class NaftahJLineParser extends DefaultParser {
    private char[] quoteChars = {'\'', '"'};
    private char[] escapeChars = {'\\'};
    private boolean eofOnUnclosedQuote;
    private boolean eofOnEscapedNewLine;
    private char[] openingBrackets = null;
    private char[] closingBrackets = null;
    private String[] lineCommentDelims = null;
    private DefaultParser.BlockCommentDelims blockCommentDelims = null;
    private String regexVariable = "[a-zA-Z_]+[a-zA-Z0-9_-]*((\\.|\\['|\\[\"|\\[)[a-zA-Z0-9_-]*(|']|\"]|]))?";
    private String regexCommand = "[:]?[a-zA-Z]+[a-zA-Z0-9_-]*";
    private int commandGroup = 4;

    //
    // Chainable setters
    //
    public NaftahJLineParser lineCommentDelims(final String[] lineCommentDelims) {
        this.lineCommentDelims = lineCommentDelims;
        return this;
    }

    public NaftahJLineParser blockCommentDelims(final DefaultParser.BlockCommentDelims blockCommentDelims) {
        this.blockCommentDelims = blockCommentDelims;
        return this;
    }

    public NaftahJLineParser quoteChars(final char[] chars) {
        this.quoteChars = chars;
        return this;
    }

    public NaftahJLineParser escapeChars(final char[] chars) {
        this.escapeChars = chars;
        return this;
    }

    public NaftahJLineParser eofOnUnclosedQuote(boolean eofOnUnclosedQuote) {
        this.eofOnUnclosedQuote = eofOnUnclosedQuote;
        return this;
    }

    public NaftahJLineParser eofOnUnclosedBracket(DefaultParser.Bracket... brackets) {
        setEofOnUnclosedBracket(brackets);
        return this;
    }

    public NaftahJLineParser eofOnEscapedNewLine(boolean eofOnEscapedNewLine) {
        this.eofOnEscapedNewLine = eofOnEscapedNewLine;
        return this;
    }

    public NaftahJLineParser regexVariable(String regexVariable) {
        this.regexVariable = regexVariable;
        return this;
    }

    public NaftahJLineParser regexCommand(String regexCommand) {
        this.regexCommand = regexCommand;
        return this;
    }

    public NaftahJLineParser commandGroup(int commandGroup) {
        this.commandGroup = commandGroup;
        return this;
    }

    //
    // Java bean getters and setters
    //

    public void setQuoteChars(final char[] chars) {
        this.quoteChars = chars;
    }

    public char[] getQuoteChars() {
        return this.quoteChars;
    }

    public void setEscapeChars(final char[] chars) {
        this.escapeChars = chars;
    }

    public char[] getEscapeChars() {
        return this.escapeChars;
    }

    public void setLineCommentDelims(String[] lineCommentDelims) {
        this.lineCommentDelims = lineCommentDelims;
    }

    public String[] getLineCommentDelims() {
        return this.lineCommentDelims;
    }

    public void setBlockCommentDelims(DefaultParser.BlockCommentDelims blockCommentDelims) {
        this.blockCommentDelims = blockCommentDelims;
    }

    public DefaultParser.BlockCommentDelims getBlockCommentDelims() {
        return blockCommentDelims;
    }

    public void setEofOnUnclosedQuote(boolean eofOnUnclosedQuote) {
        this.eofOnUnclosedQuote = eofOnUnclosedQuote;
    }

    public boolean isEofOnUnclosedQuote() {
        return eofOnUnclosedQuote;
    }

    public void setEofOnEscapedNewLine(boolean eofOnEscapedNewLine) {
        this.eofOnEscapedNewLine = eofOnEscapedNewLine;
    }

    public boolean isEofOnEscapedNewLine() {
        return eofOnEscapedNewLine;
    }

    public void setEofOnUnclosedBracket(DefaultParser.Bracket... brackets) {
        if (brackets == null) {
            openingBrackets = null;
            closingBrackets = null;
        } else {
            Set<DefaultParser.Bracket> bs = new HashSet<>(Arrays.asList(brackets));
            openingBrackets = new char[bs.size()];
            closingBrackets = new char[bs.size()];
            int i = 0;
            for (DefaultParser.Bracket b : bs) {
                switch (b) {
                    case ROUND:
                        openingBrackets[i] = '(';
                        closingBrackets[i] = ')';
                        break;
                    case CURLY:
                        openingBrackets[i] = '{';
                        closingBrackets[i] = '}';
                        break;
                    case SQUARE:
                        openingBrackets[i] = '[';
                        closingBrackets[i] = ']';
                        break;
                    case ANGLE:
                        openingBrackets[i] = '<';
                        closingBrackets[i] = '>';
                        break;
                }
                i++;
            }
        }
    }

    public void setRegexVariable(String regexVariable) {
        this.regexVariable = regexVariable;
    }

    public void setRegexCommand(String regexCommand) {
        this.regexCommand = regexCommand;
    }

    public void setCommandGroup(int commandGroup) {
        this.commandGroup = commandGroup;
    }

    @Override
    public boolean validCommandName(String name) {
        return name != null && name.matches(regexCommand);
    }

    @Override
    public boolean validVariableName(String name) {
        return name != null && regexVariable != null && name.matches(regexVariable);
    }

    @Override
    public String getCommand(final String line) {
        String out = "";
        boolean checkCommandOnly = regexVariable == null;
        if (!checkCommandOnly) {
            Pattern patternCommand = Pattern.compile("^\\s*" + regexVariable + "=(" + regexCommand + ")(\\s+|$)");
            Matcher matcher = patternCommand.matcher(line);
            if (matcher.find()) {
                out = matcher.group(commandGroup);
            } else {
                checkCommandOnly = true;
            }
        }
        if (checkCommandOnly) {
            out = line.trim().split("\\s+")[0];
            if (!out.matches(regexCommand)) {
                out = "";
            }
        }
        return out;
    }

    @Override
    public String getVariable(final String line) {
        String out = null;
        if (regexVariable != null) {
            Pattern patternCommand = Pattern.compile("^\\s*(" + regexVariable + ")\\s*=[^=~].*");
            Matcher matcher = patternCommand.matcher(line);
            if (matcher.find()) {
                out = matcher.group(1);
            }
        }
        return out;
    }

    public ParsedLine parse(final String line, final int cursor, ParseContext context) {
        List<String> words = new LinkedList<>();
        StringBuilder current = new StringBuilder();
        int wordCursor = -1;
        int wordIndex = -1;
        int quoteStart = -1;
        int rawWordCursor = -1;
        int rawWordLength = -1;
        int rawWordStart = 0;
        BracketChecker bracketChecker = new BracketChecker(cursor);
        boolean quotedWord = false;
        boolean lineCommented = false;
        boolean blockCommented = false;
        boolean blockCommentInRightOrder = true;
        final String blockCommentEnd = blockCommentDelims == null ? null : blockCommentDelims.getEnd();
        final String blockCommentStart = blockCommentDelims == null ? null : blockCommentDelims.getStart();

        for (int i = 0; (line != null) && (i < line.length()); i++) {
            // once we reach the cursor, set the
            // position of the selected index
            if (i == cursor) {
                wordIndex = words.size();
                // the position in the current argument is just the
                // length of the current argument
                wordCursor = current.length();
                rawWordCursor = i - rawWordStart;
            }

            if (quoteStart < 0 && isQuoteChar(line, i) && !lineCommented && !blockCommented) {
                // Start a quote block
                quoteStart = i;
                if (current.length() == 0) {
                    quotedWord = true;
                    if (context == ParseContext.SPLIT_LINE) {
                        current.append(line.charAt(i));
                    }
                } else {
                    current.append(line.charAt(i));
                }
            } else if (quoteStart >= 0 && line.charAt(quoteStart) == line.charAt(i) && !isEscaped(line, i)) {
                // End quote block
                if (!quotedWord || context == ParseContext.SPLIT_LINE) {
                    current.append(line.charAt(i));
                } else if (rawWordCursor >= 0 && rawWordLength < 0) {
                    rawWordLength = i - rawWordStart + 1;
                }
                quoteStart = -1;
                quotedWord = false;
            } else if (quoteStart < 0 && isDelimiter(line, i)) {
                if (lineCommented) {
                    if (isCommentDelim(line, i, System.lineSeparator())) {
                        lineCommented = false;
                    }
                } else if (blockCommented) {
                    if (isCommentDelim(line, i, blockCommentEnd)) {
                        blockCommented = false;
                    }
                } else {
                    // Delimiter
                    rawWordLength = handleDelimiterAndGetRawWordLength(
                            current, words, rawWordStart, rawWordCursor, rawWordLength, i);
                    rawWordStart = i + 1;
                }
            } else {
                if (quoteStart < 0 && !blockCommented && (lineCommented || isLineCommentStarted(line, i))) {
                    lineCommented = true;
                } else if (quoteStart < 0
                        && !lineCommented
                        && (blockCommented || isCommentDelim(line, i, blockCommentStart))) {
                    if (blockCommented) {
                        if (blockCommentEnd != null && isCommentDelim(line, i, blockCommentEnd)) {
                            blockCommented = false;
                            i += blockCommentEnd.length() - 1;
                        }
                    } else {
                        blockCommented = true;
                        rawWordLength = handleDelimiterAndGetRawWordLength(
                                current, words, rawWordStart, rawWordCursor, rawWordLength, i);
                        i += blockCommentStart == null ? 0 : blockCommentStart.length() - 1;
                        rawWordStart = i + 1;
                    }
                } else if (quoteStart < 0 && !lineCommented && isCommentDelim(line, i, blockCommentEnd)) {
                    current.append(line.charAt(i));
                    blockCommentInRightOrder = false;
                } else if (!isEscapeChar(line, i)) {
                    current.append(line.charAt(i));
                    if (quoteStart < 0) {
                        bracketChecker.check(line, i);
                    }
                } else if (context == ParseContext.SPLIT_LINE) {
                    current.append(line.charAt(i));
                }
            }
        }

        if (current.length() > 0 || cursor == line.length()) {
            words.add(current.toString());
            if (rawWordCursor >= 0 && rawWordLength < 0) {
                rawWordLength = line.length() - rawWordStart;
            }
        }

        if (cursor == line.length()) {
            wordIndex = words.size() - 1;
            wordCursor = words.get(words.size() - 1).length();
            rawWordCursor = cursor - rawWordStart;
            rawWordLength = rawWordCursor;
        }

        if (context != ParseContext.COMPLETE && context != ParseContext.SPLIT_LINE) {
            if (eofOnEscapedNewLine && isEscapeChar(line, line.length() - 1)) {
                throw new EOFError(-1, -1, "Escaped new line", "newline");
            }
            if (eofOnUnclosedQuote && quoteStart >= 0) {
                throw new EOFError(
                        -1, -1, "Missing closing quote", line.charAt(quoteStart) == '\'' ? "quote" : "dquote");
            }
            if (blockCommented) {
                throw new EOFError(-1, -1, "Missing closing block comment delimiter", "add: " + blockCommentEnd);
            }
            if (!blockCommentInRightOrder) {
                throw new EOFError(-1, -1, "Missing opening block comment delimiter", "missing: " + blockCommentStart);
            }
            if (bracketChecker.isClosingBracketMissing() || bracketChecker.isOpeningBracketMissing()) {
                String message = null;
                String missing = null;
                if (bracketChecker.isClosingBracketMissing()) {
                    message = "Missing closing brackets";
                    missing = "add: " + bracketChecker.getMissingClosingBrackets();
                } else {
                    message = "Missing opening bracket";
                    missing = "missing: " + bracketChecker.getMissingOpeningBracket();
                }
                throw new EOFError(
                        -1,
                        -1,
                        message,
                        missing,
                        bracketChecker.getOpenBrackets(),
                        bracketChecker.getNextClosingBracket());
            }
        }

        String openingQuote = quotedWord ? line.substring(quoteStart, quoteStart + 1) : null;
        return new DefaultParser.ArgumentList(line, words, wordIndex, wordCursor, cursor, openingQuote, rawWordCursor, rawWordLength);
    }

    /**
     * Returns true if the specified character is a whitespace parameter. Check to ensure that the character is not
     * escaped by any of {@link #getQuoteChars}, and is not escaped by any of the {@link #getEscapeChars}, and
     * returns true from {@link #isDelimiterChar}.
     *
     * @param buffer    The complete command buffer
     * @param pos       The index of the character in the buffer
     * @return          True if the character should be a delimiter
     */
    public boolean isDelimiter(final CharSequence buffer, final int pos) {
        return !isQuoted(buffer, pos) && !isEscaped(buffer, pos) && isDelimiterChar(buffer, pos);
    }

    private int handleDelimiterAndGetRawWordLength(
            StringBuilder current,
            List<String> words,
            int rawWordStart,
            int rawWordCursor,
            int rawWordLength,
            int pos) {
        if (current.length() > 0) {
            words.add(current.toString());
            current.setLength(0); // reset the arg
            if (rawWordCursor >= 0 && rawWordLength < 0) {
                return pos - rawWordStart;
            }
        }
        return rawWordLength;
    }

    public boolean isQuoted(final CharSequence buffer, final int pos) {
        return false;
    }

    public boolean isQuoteChar(final CharSequence buffer, final int pos) {
        if (pos < 0) {
            return false;
        }
        if (quoteChars != null) {
            for (char e : quoteChars) {
                if (e == buffer.charAt(pos)) {
                    return !isEscaped(buffer, pos);
                }
            }
        }
        return false;
    }

    private boolean isCommentDelim(final CharSequence buffer, final int pos, final String pattern) {
        if (pos < 0) {
            return false;
        }

        if (pattern != null) {
            final int length = pattern.length();
            if (length <= buffer.length() - pos) {
                for (int i = 0; i < length; i++) {
                    if (pattern.charAt(i) != buffer.charAt(pos + i)) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean isLineCommentStarted(final CharSequence buffer, final int pos) {
        if (lineCommentDelims != null) {
            for (String comment : lineCommentDelims) {
                if (isCommentDelim(buffer, pos, comment)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isEscapeChar(char ch) {
        if (escapeChars != null) {
            for (char e : escapeChars) {
                if (e == ch) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if this character is a valid escape char (i.e. one that has not been escaped)
     *
     * @param buffer
     *          the buffer to check in
     * @param pos
     *          the position of the character to check
     * @return true if the character at the specified position in the given buffer is an escape
     *         character and the character immediately preceding it is not an escape character.
     */
    public boolean isEscapeChar(final CharSequence buffer, final int pos) {
        if (pos < 0) {
            return false;
        }
        char ch = buffer.charAt(pos);
        return isEscapeChar(ch) && !isEscaped(buffer, pos);
    }

    /**
     * Check if a character is escaped (i.e. if the previous character is an escape)
     *
     * @param buffer
     *          the buffer to check in
     * @param pos
     *          the position of the character to check
     * @return true if the character at the specified position in the given buffer is an escape
     *         character and the character immediately preceding it is an escape character.
     */
    public boolean isEscaped(final CharSequence buffer, final int pos) {
        if (pos <= 0) {
            return false;
        }
        return isEscapeChar(buffer, pos - 1);
    }

    /**
     * Returns true if the character at the specified position if a delimiter. This method will only be called if
     * the character is not enclosed in any of the {@link #getQuoteChars}, and is not escaped by any of the
     * {@link #getEscapeChars}. To perform escaping manually, override {@link #isDelimiter} instead.
     *
     * @param buffer
     *          the buffer to check in
     * @param pos
     *          the position of the character to check
     * @return true if the character at the specified position in the given buffer is a delimiter.
     */
    public boolean isDelimiterChar(CharSequence buffer, int pos) {
        return Character.isWhitespace(buffer.charAt(pos));
    }

    private boolean isRawEscapeChar(char key) {
        if (escapeChars != null) {
            for (char e : escapeChars) {
                if (e == key) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isRawQuoteChar(char key) {
        if (quoteChars != null) {
            for (char e : quoteChars) {
                if (e == key) {
                    return true;
                }
            }
        }
        return false;
    }

    private class BracketChecker {
        private int missingOpeningBracket = -1;
        private List<Integer> nested = new ArrayList<>();
        private int openBrackets = 0;
        private int cursor;
        private String nextClosingBracket;

        public BracketChecker(int cursor) {
            this.cursor = cursor;
        }

        public void check(final CharSequence buffer, final int pos) {
            if (openingBrackets == null || pos < 0) {
                return;
            }
            int bid = bracketId(openingBrackets, buffer, pos);
            if (bid >= 0) {
                nested.add(bid);
            } else {
                bid = bracketId(closingBrackets, buffer, pos);
                if (bid >= 0) {
                    if (!nested.isEmpty() && bid == nested.get(nested.size() - 1)) {
                        nested.remove(nested.size() - 1);
                    } else {
                        missingOpeningBracket = bid;
                    }
                }
            }
            if (cursor > pos) {
                openBrackets = nested.size();
                if (nested.size() > 0) {
                    nextClosingBracket = String.valueOf(closingBrackets[nested.get(nested.size() - 1)]);
                }
            }
        }

        public boolean isOpeningBracketMissing() {
            return missingOpeningBracket != -1;
        }

        public String getMissingOpeningBracket() {
            if (!isOpeningBracketMissing()) {
                return null;
            }
            return Character.toString(openingBrackets[missingOpeningBracket]);
        }

        public boolean isClosingBracketMissing() {
            return !nested.isEmpty();
        }

        public String getMissingClosingBrackets() {
            if (!isClosingBracketMissing()) {
                return null;
            }
            StringBuilder out = new StringBuilder();
            for (int i = nested.size() - 1; i > -1; i--) {
                out.append(closingBrackets[nested.get(i)]);
            }
            return out.toString();
        }

        public int getOpenBrackets() {
            return openBrackets;
        }

        public String getNextClosingBracket() {
            return nested.size() == 2 ? nextClosingBracket : null;
        }

        private int bracketId(final char[] brackets, final CharSequence buffer, final int pos) {
            for (int i = 0; i < brackets.length; i++) {
                if (buffer.charAt(pos) == brackets[i]) {
                    return i;
                }
            }
            return -1;
        }
    }
}
