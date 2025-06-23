package org.daiitech.naftah.utils.arabic;

import java.util.ArrayList;
import java.util.List;

import static org.daiitech.naftah.utils.arabic.ArabicUtils.shape;

public class ArabicShapingParser {
    private final String input;
    private int pos;

    public ArabicShapingParser(String input) {
        this.input = input;
        this.pos = 0;
    }

    public Node parse() throws Exception {
        return parseGroup('\0'); // Root
    }

    private Node parseGroup(char closingChar) throws Exception {
        GroupNode group = new GroupNode('\0', closingChar);
        StringBuilder buffer = new StringBuilder();

        while (pos < input.length()) {
            char c = input.charAt(pos);

            if (c == closingChar && closingChar != '\0') {
                pos++; // consume closing
                break;
            }

            if (isOpeningParen(c)) {
                flushTextNode(buffer, group, true);
                pos++; // consume opening
                group.children.add(parseGroup(getClosingParen(c)));
            } else if (isQuoteChar(c)) {
                flushTextNode(buffer, group, true);
                group.children.add(parseQuote());
            } else {
                buffer.append(c);
                pos++;
            }
        }

        flushTextNode(buffer, group, true);
        return group.children.size() == 1 ? group.children.get(0) : group;
    }

    private void flushTextNode(StringBuilder buffer, GroupNode group, boolean shapeArabic) {
        if (!buffer.isEmpty()) {
            group.children.add(new TextNode(buffer.toString(), shapeArabic));
            buffer.setLength(0);
        }
    }

    private QuoteNode parseQuote() {
        char start = input.charAt(pos++);
        char end = (start == '«') ? '»' : start;
        StringBuilder quote = new StringBuilder();
        quote.append(start);

        while (pos < input.length()) {
            char c = input.charAt(pos++);
            quote.append(c);
            if (c == end) break;
        }

        return new QuoteNode(quote.toString());
    }

    private boolean isOpeningParen(char c) {
        return c == '(' || c == '{' || c == '[';
    }

    private char getClosingParen(char open) {
        return switch (open) {
            case '(' -> ')';
            case '{' -> '}';
            case '[' -> ']';
            default -> '\0';
        };
    }

    private boolean isQuoteChar(char c) {
        return c == '"' || c == '\'' || c == '«';
    }

    public abstract static class Node {
        abstract String build() throws Exception;
    }

    public static class TextNode extends Node {
        String text;
        boolean shapeArabic;

        TextNode(String text, boolean shapeArabic) {
            this.text = text;
            this.shapeArabic = shapeArabic;
        }

        @Override
        String build() throws Exception {
            return shapeArabic ? shape(text) : text;
        }
    }

    public static class QuoteNode extends Node {
        String quote;

        QuoteNode(String quote) {
            this.quote = quote;
        }

        @Override
        String build() {
            return quote;  // no shaping inside quotes
        }
    }

    public static class GroupNode extends Node {
        List<Node> children = new ArrayList<>();
        char open;
        char close;

        GroupNode(char open, char close) {
            this.open = open;
            this.close = close;
        }

        @Override
        String build() throws Exception {
            StringBuilder sb = new StringBuilder();
            sb.append(open);
            for (Node n : children) {
                sb.append(n.build());
            }
            sb.append(close);
            return sb.toString();
        }
    }

}

