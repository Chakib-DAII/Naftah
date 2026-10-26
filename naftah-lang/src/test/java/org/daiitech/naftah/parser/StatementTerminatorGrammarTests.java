// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

package org.daiitech.naftah.parser;

import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;

import static org.daiitech.naftah.parser.NaftahParserHelper.getCharStream;
import static org.daiitech.naftah.parser.NaftahParserHelper.prepareRun;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatementTerminatorGrammarTests {

	@Test
	void insertsEndTokensForOmittedStatementTerminators() {
		var program = parse("""
							متغير أ تعيين 1
							أ تعيين 2 أنهي
							أ
							""");

		assertEquals(3, program.statement().size());
		assertTrue(hasEnd(program.statement(0)));
		assertTrue(hasEnd(program.statement(1)));
		assertTrue(hasEnd(program.statement(2)));
		assertEquals(   4,
						program
								.statement(0)
								.getTokens(org.daiitech.naftah.parser.NaftahParser.END)
								.get(0)
								.getSymbol()
								.getTokenIndex());
		assertTrue(program
				.statement(1)
				.getTokens(org.daiitech.naftah.parser.NaftahParser.END)
				.get(0)
				.getSymbol()
				.getTokenIndex() >= 0);
		assertEquals(   10,
						program
								.statement(2)
								.getTokens(org.daiitech.naftah.parser.NaftahParser.END)
								.get(0)
								.getSymbol()
								.getTokenIndex());
	}

	@Test
	void separatesPostfixAndPrefixExpressionsOnAdjacentLines() {
		var program = parse("""
							أ
							++ب
							""");

		assertEquals(2, program.statement().size());
		assertTrue(hasEnd(program.statement(0)));
	}

	@Test
	void attachesOptionalEndToStatementsInsideBlocks() {
		var program = parse("""
							{
								متغير أ تعيين 1 أنهي
								أ تعيين 2
							}
							""");

		var statements = program
				.statement(0)
				.getChild(org.daiitech.naftah.parser.NaftahParser.BlockContext.class, 0)
				.statement();
		assertEquals(2, statements.size());
		assertTrue(hasEnd(statements.get(0)));
		assertFalse(hasEnd(statements.get(1)));
	}

	@Test
	void separatesImplementationFunctionsByTheirBlocks() {
		var program = parse("""
							سلوك شخص {
								دالة الاسم() { ارجع 1 }
								دالة العمر() { ارجع 2 }
							}
							""");

		var implementation = program
				.statement(0)
				.getChild(org.daiitech.naftah.parser.NaftahParser.ImplementationDeclarationContext.class, 0);
		assertEquals(2, implementation.implementationFunctions().functionDeclaration().size());
	}

	private org.daiitech.naftah.parser.NaftahParser.ProgramContext parse(String script) {
		return prepareRun(getCharStream(script)).program();
	}

	private boolean hasEnd(org.daiitech.naftah.parser.NaftahParser.StatementContext statement) {
		List<? extends ParseTree> endTokens = statement.getTokens(org.daiitech.naftah.parser.NaftahParser.END);
		return !endTokens.isEmpty();
	}
}
