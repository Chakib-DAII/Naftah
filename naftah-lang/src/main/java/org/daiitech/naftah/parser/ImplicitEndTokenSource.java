// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

package org.daiitech.naftah.parser;

import java.util.ArrayDeque;
import java.util.Deque;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.TokenSource;

/**
 * A {@link TokenSource} decorator that inserts virtual {@code END} tokens at
 * unambiguous statement boundaries.
 *
 * <p>This token source sits between the lexer and parser. It delegates lexical
 * analysis to another {@link TokenSource}, but may insert an {@code END} token
 * between two tokens when the transition between those tokens represents an
 * unambiguous statement boundary.
 *
 * <p>The purpose of this class is to allow Naftah source code to use line
 * boundaries as implicit statement terminators without requiring the lexer to
 * emit an {@code END} token for every newline.
 *
 * <p>An implicit {@code END} token is inserted only when all of the following
 * conditions hold:
 *
 * <ul>
 * <li>There is a previously emitted token.</li>
 * <li>The parser is not currently inside parentheses.</li>
 * <li>The parser is not currently inside square brackets.</li>
 * <li>The previous token can legally terminate a statement.</li>
 * <li>The next token is on a later source line, or is {@link Token#EOF}.</li>
 * <li>The next token can legally begin a statement, unless it is EOF.</li>
 * <li>The next token is not already an explicit {@code END} token.</li>
 * </ul>
 *
 * <p>This conservative approach is intentional. A newline alone does not
 * necessarily represent a statement boundary. For example, expressions may
 * legitimately continue across lines while parentheses or square brackets
 * are open. The token source therefore inserts an implicit terminator only
 * when the surrounding token context makes the boundary unambiguous.
 *
 * <p>Explicit {@code END} tokens produced by the underlying token source are
 * never replaced or duplicated.
 *
 * <p>When an implicit terminator is required, the next token is temporarily
 * stored in an internal queue. The virtual {@code END} token is returned
 * first, and the queued token is returned on the following invocation of
 * {@link #nextToken()}.
 *
 * <p>The implementation also tracks parenthesis and square-bracket nesting.
 * This state is maintained from the tokens that have actually been emitted
 * through this source, including tokens previously held in the pending-token
 * queue.
 *
 * <p>This class does not modify the underlying lexer or its input stream.
 * It only changes the token sequence observed by the parser.
 *
 * @author Chakib Daii
 */
public final class ImplicitEndTokenSource implements TokenSource {

	/**
	 * Text assigned to virtual {@code END} tokens.
	 *
	 * <p>The text is primarily diagnostic metadata because the token itself
	 * does not originate from the source file.
	 */
	private static final String IMPLICIT_END_TEXT = "أنهي";

	/**
	 * Underlying token source responsible for producing the real tokens.
	 */
	private final TokenSource delegate;

	/**
	 * Tokens that have already been obtained from {@link #delegate} but must
	 * be returned after a virtual {@code END} token.
	 *
	 * <p>At present this queue normally contains at most one token. A deque is
	 * used because it naturally represents a FIFO token buffer and leaves room
	 * for future extensions that may require more than one pending token.
	 */
	private final Deque<Token> pendingTokens = new ArrayDeque<>();

	/**
	 * Most recently emitted token.
	 *
	 * <p>This is the token against which the next token is evaluated when
	 * determining whether an implicit statement terminator should be inserted.
	 */
	private Token previousToken;

	/**
	 * Current nesting depth of {@code (...)} expressions.
	 *
	 * <p>An implicit statement terminator is never inserted while this value
	 * is non-zero because line boundaries inside parentheses are considered
	 * continuations rather than statement boundaries.
	 */
	private int parenthesisDepth;

	/**
	 * Current nesting depth of {@code [...]} expressions.
	 *
	 * <p>An implicit statement terminator is never inserted while this value
	 * is non-zero because line boundaries inside square brackets are considered
	 * continuations rather than statement boundaries.
	 */
	private int bracketDepth;

	/**
	 * Creates an implicit-terminator token source wrapping the supplied token
	 * source.
	 *
	 * @param delegate the underlying token source from which real tokens are
	 *                 obtained; must not be {@code null}
	 */
	public ImplicitEndTokenSource(TokenSource delegate) {
		this.delegate = delegate;
	}

	/**
	 * Returns the next token visible to the parser.
	 *
	 * <p>If a token is already waiting in {@link #pendingTokens}, that token is
	 * returned before requesting another token from the underlying source.
	 *
	 * <p>Otherwise, the next token is obtained from the delegate. Before
	 * returning it, this method determines whether an implicit {@code END}
	 * token must be inserted between the previously emitted token and the
	 * newly obtained token.
	 *
	 * <p>If an implicit terminator is required, the real next token is placed
	 * into {@link #pendingTokens} and the virtual {@code END} token is returned.
	 * The real token is therefore returned by the next invocation of this
	 * method.
	 *
	 * <p>Every token returned from this method is passed through
	 * {@link #record(Token)} so that the nesting and previous-token state
	 * always describes the token stream already observed by the parser.
	 *
	 * @return the next real or virtual token
	 */
	@Override
	public Token nextToken() {
		if (!pendingTokens.isEmpty()) {
			Token token = pendingTokens.removeFirst();
			record(token);
			return token;
		}

		Token nextToken = delegate.nextToken();

		if (shouldInsertEndBefore(nextToken)) {
			pendingTokens.addLast(nextToken);
			return newImplicitEndToken(previousToken, nextToken);
		}

		record(nextToken);
		return nextToken;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getLine() {
		return delegate.getLine();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getCharPositionInLine() {
		return delegate.getCharPositionInLine();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CharStream getInputStream() {
		return delegate.getInputStream();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSourceName() {
		return delegate.getSourceName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTokenFactory(TokenFactory<?> factory) {
		delegate.setTokenFactory(factory);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TokenFactory<?> getTokenFactory() {
		return delegate.getTokenFactory();
	}

	/**
	 * Determines whether a virtual {@code END} token should be inserted before
	 * the supplied token.
	 *
	 * <p>A terminator is inserted only at an unambiguous statement boundary.
	 * In particular, the previous token must be capable of ending a statement,
	 * the next token must be capable of beginning one, and the tokens must be
	 * separated by a source-line boundary.
	 *
	 * <p>EOF is treated specially. If the final token can terminate a
	 * statement, an implicit {@code END} is emitted before EOF even though EOF
	 * does not itself begin a statement.
	 *
	 * <p>No terminator is inserted while parentheses or square brackets are
	 * open. This prevents constructs spanning multiple lines from being split
	 * into separate statements merely because they cross a line boundary.
	 *
	 * <p>Explicit {@code END} tokens are ignored by this mechanism because the
	 * source program has already supplied the required terminator.
	 *
	 * @param nextToken the next token obtained from the underlying token source
	 * @return {@code true} if a virtual {@code END} token should precede
	 *         {@code nextToken}; {@code false} otherwise
	 */
	private boolean shouldInsertEndBefore(Token nextToken) {
		if (previousToken == null || parenthesisDepth != 0 || bracketDepth != 0 || nextToken
				.getType() == org.daiitech.naftah.parser.NaftahLexer.END || !canEndStatement(previousToken.getType())) {
			return false;
		}

		return nextToken.getType() == Token.EOF || (nextToken.getLine() > previousToken.getLine() && canStartStatement(
																														nextToken
																																.getType()));
	}

	/**
	 * Determines whether a token type can occur at the end of a statement.
	 *
	 * <p>This method defines the conservative set of token types after which a
	 * line boundary may represent the end of a statement. The set intentionally
	 * contains complete values and postfix operators, such as increment and
	 * decrement, but excludes operators and other tokens that normally require
	 * another expression or syntactic component to follow them.
	 *
	 * <p>For example, an identifier or literal can terminate an expression,
	 * whereas an arithmetic operator generally cannot.
	 *
	 * @param tokenType the token type to test
	 * @return {@code true} if the token type can terminate a statement;
	 *         {@code false} otherwise
	 */
	private boolean canEndStatement(int tokenType) {
		return switch (tokenType) {
			case org.daiitech.naftah.parser.NaftahLexer.ID,
					org.daiitech.naftah.parser.NaftahLexer.SELF,
					org.daiitech.naftah.parser.NaftahLexer.NUMBER,
					org.daiitech.naftah.parser.NaftahLexer.BASE_RADIX,
					org.daiitech.naftah.parser.NaftahLexer.TRUE,
					org.daiitech.naftah.parser.NaftahLexer.FALSE,
					org.daiitech.naftah.parser.NaftahLexer.NULL,
					org.daiitech.naftah.parser.NaftahLexer.CHARACTER,
					org.daiitech.naftah.parser.NaftahLexer.STRING,
					org.daiitech.naftah.parser.NaftahLexer.NAN,
					org.daiitech.naftah.parser.NaftahLexer.RPAREN,
					org.daiitech.naftah.parser.NaftahLexer.RBRACK,
					org.daiitech.naftah.parser.NaftahLexer.INCREMENT,
					org.daiitech.naftah.parser.NaftahLexer.DECREMENT -> true;
			default -> false;
		};
	}

	/**
	 * Determines whether a token type can begin a new statement.
	 *
	 * <p>This method defines the token types for which a line transition after
	 * a statement-ending token is considered an unambiguous statement
	 * boundary.
	 *
	 * <p>The method deliberately excludes tokens that normally continue the
	 * preceding expression, such as binary operators, member-access
	 * operators, separators, and other continuation tokens.
	 *
	 * @param tokenType the token type to test
	 * @return {@code true} if the token type can begin a statement;
	 *         {@code false} otherwise
	 */
	private boolean canStartStatement(int tokenType) {
		return switch (tokenType) {
			case org.daiitech.naftah.parser.NaftahLexer.SCOPE,
					org.daiitech.naftah.parser.NaftahLexer.LBRACE,
					org.daiitech.naftah.parser.NaftahLexer.IMPORT,
					org.daiitech.naftah.parser.NaftahLexer.IF,
					org.daiitech.naftah.parser.NaftahLexer.FOR,
					org.daiitech.naftah.parser.NaftahLexer.WHILE,
					org.daiitech.naftah.parser.NaftahLexer.REPEAT,
					org.daiitech.naftah.parser.NaftahLexer.CASE,
					org.daiitech.naftah.parser.NaftahLexer.TRY,
					org.daiitech.naftah.parser.NaftahLexer.ASYNC,
					org.daiitech.naftah.parser.NaftahLexer.FUNCTION,
					org.daiitech.naftah.parser.NaftahLexer.IMPLEMENTATION,
					org.daiitech.naftah.parser.NaftahLexer.VARIABLE,
					org.daiitech.naftah.parser.NaftahLexer.CONSTANT,
					org.daiitech.naftah.parser.NaftahLexer.CHANNEL,
					org.daiitech.naftah.parser.NaftahLexer.ACTOR,
					org.daiitech.naftah.parser.NaftahLexer.RETURN,
					org.daiitech.naftah.parser.NaftahLexer.BREAK,
					org.daiitech.naftah.parser.NaftahLexer.CONTINUE,
					org.daiitech.naftah.parser.NaftahLexer.SPAWN,
					org.daiitech.naftah.parser.NaftahLexer.AWAIT,
					org.daiitech.naftah.parser.NaftahLexer.NOT,
					org.daiitech.naftah.parser.NaftahLexer.BITWISE_NOT,
					org.daiitech.naftah.parser.NaftahLexer.TYPE_OF,
					org.daiitech.naftah.parser.NaftahLexer.SIZE_OF,
					org.daiitech.naftah.parser.NaftahLexer.INCREMENT,
					org.daiitech.naftah.parser.NaftahLexer.DECREMENT,
					org.daiitech.naftah.parser.NaftahLexer.AT_SIGN,
					org.daiitech.naftah.parser.NaftahLexer.LBRACK,
					org.daiitech.naftah.parser.NaftahLexer.LPAREN,
					org.daiitech.naftah.parser.NaftahLexer.ID,
					org.daiitech.naftah.parser.NaftahLexer.SELF,
					org.daiitech.naftah.parser.NaftahLexer.NUMBER,
					org.daiitech.naftah.parser.NaftahLexer.TRUE,
					org.daiitech.naftah.parser.NaftahLexer.FALSE,
					org.daiitech.naftah.parser.NaftahLexer.NULL,
					org.daiitech.naftah.parser.NaftahLexer.CHARACTER,
					org.daiitech.naftah.parser.NaftahLexer.STRING,
					org.daiitech.naftah.parser.NaftahLexer.NAN -> true;
			default -> false;
		};
	}

	/**
	 * Records a token as having been emitted by this token source.
	 *
	 * <p>The token becomes the new {@link #previousToken}, and delimiters update
	 * the nesting state used by {@link #shouldInsertEndBefore(Token)}.
	 *
	 * <p>Parenthesis and bracket depths are clamped at zero when closing tokens
	 * are encountered. This prevents malformed or otherwise unbalanced input
	 * from causing the internal nesting counters to become negative.
	 *
	 * @param token the token that has just been emitted
	 */
	private void record(Token token) {
		previousToken = token;

		switch (token.getType()) {
			case org.daiitech.naftah.parser.NaftahLexer.LPAREN ->
				parenthesisDepth++;

			case org.daiitech.naftah.parser.NaftahLexer.RPAREN ->
				parenthesisDepth = Math.max(0, parenthesisDepth - 1);

			case org.daiitech.naftah.parser.NaftahLexer.LBRACK ->
				bracketDepth++;

			case org.daiitech.naftah.parser.NaftahLexer.RBRACK ->
				bracketDepth = Math.max(0, bracketDepth - 1);

			default -> {
				// No nesting state to update.
			}
		}
	}

	/**
	 * Creates a virtual {@code END} token representing an implicit statement
	 * boundary between two real tokens.
	 *
	 * <p>The virtual token is positioned immediately after the previous token
	 * on the previous token's source line. Its source interval is empty because
	 * it does not correspond to any characters in the input stream.
	 *
	 * <p>The token's text is {@link #IMPLICIT_END_TEXT}, which makes the virtual
	 * token distinguishable in diagnostics and debugging output from an
	 * explicit {@code END} token.
	 *
	 * <p>The start index is set to the next token's start index and the stop
	 * index is set to one character before that position, representing an empty
	 * source interval.
	 *
	 * @param previousToken the token immediately preceding the implicit
	 *                      statement boundary
	 * @param nextToken     the token immediately following the implicit statement
	 *                      boundary
	 * @return a virtual {@code END} token representing the inferred boundary
	 */
	private static Token newImplicitEndToken(Token previousToken, Token nextToken) {
		CommonToken end = new CommonToken(
											org.daiitech.naftah.parser.NaftahLexer.END,
											IMPLICIT_END_TEXT
		);

		end.setLine(previousToken.getLine());
		end
				.setCharPositionInLine(
										previousToken.getCharPositionInLine() + previousToken.getText().length()
				);
		end.setStartIndex(nextToken.getStartIndex());
		end.setStopIndex(nextToken.getStartIndex() - 1);

		return end;
	}
}
