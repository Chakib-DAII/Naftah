package org.daiitech.naftah.builtin;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.daiitech.naftah.builtin.utils.CollectionUtils;
import org.daiitech.naftah.builtin.utils.Tuple;
import org.daiitech.naftah.parser.LoopSignal;
import org.daiitech.naftah.parser.NaftahParserHelper;
import org.daiitech.naftah.utils.arabic.ArabicOutputTransformer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.daiitech.naftah.NaftahSystem.TERMINAL_HEIGHT_PROPERTY;
import static org.daiitech.naftah.NaftahSystem.TERMINAL_WIDTH_PROPERTY;
import static org.daiitech.naftah.TestUtils.assertEquals;
import static org.daiitech.naftah.parser.NaftahParserHelper.NULL;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.containsArabic;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.shape;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.shouldReshape;

class BuiltinTests {

	static PrintStream originalOut = System.out;
	static ByteArrayOutputStream out;

	@BeforeAll
	static void setupAll() {
		System.setProperty(TERMINAL_WIDTH_PROPERTY, Integer.toString(80));
		System.setProperty(TERMINAL_HEIGHT_PROPERTY, Integer.toString(24));
	}

	@AfterAll
	static void tearDownAll() {
		// Restore System.out
		System.setOut(originalOut);
	}

	static Stream<org.junit.jupiter.params.provider.Arguments> printProvider() {
		return Stream
				.of(Arguments.of(null, NULL),
					Arguments.of(true, "صحيح"),
					Arguments.of(false, "خطأ"),
					Arguments.of(new int[]{1, 2}, CollectionUtils.toString(new int[]{1, 2})),
					Arguments.of(new long[]{1, 2}, CollectionUtils.toString(new long[]{1, 2})),
					Arguments.of(new double[]{1, 2}, CollectionUtils.toString(new double[]{1, 2})),
					Arguments.of(new float[]{1, 2}, CollectionUtils.toString(new float[]{1, 2})),
					Arguments
							.of(new boolean[]{true, false},
								CollectionUtils.toString(new boolean[]{true, false})),
					Arguments.of(new char[]{1, 2}, CollectionUtils.toString(new char[]{1, 2})),
					Arguments.of(new byte[]{1, 2}, CollectionUtils.toString(new byte[]{1, 2})),
					Arguments.of(new short[]{1, 2}, CollectionUtils.toString(new short[]{1, 2})),
					Arguments.of(new Object[]{1, 2}, CollectionUtils.toString(new Object[]{1, 2})),
					Arguments.of(List.of(1, 2), CollectionUtils.toString(List.of(1, 2))),
					Arguments.of(Set.of(1, 2), CollectionUtils.toString(Set.of(1, 2))),
					Arguments.of(Tuple.of(1, 2), CollectionUtils.toString(Tuple.of(1, 2))),
					Arguments.of(Map.of(1, 2), CollectionUtils.toString(Map.of(1, 2))),
					Arguments.of(LoopSignal.LoopSignalDetails.of(LoopSignal.CONTINUE, null), NULL),
					Arguments.of(LoopSignal.LoopSignalDetails.of(LoopSignal.RETURN, null), NULL),
					Arguments.of(LoopSignal.LoopSignalDetails.of(LoopSignal.BREAK, null), NULL),
					Arguments.of(LoopSignal.LoopSignalDetails.of(LoopSignal.NONE, null), NULL));
	}

	@BeforeEach
	void setup() {
		// Redirect System.out
		out = new ByteArrayOutputStream();
		PrintStream printStream = ArabicOutputTransformer.getPrintStream(out);
		System.setOut(printStream);
	}

	@ParameterizedTest
	@MethodSource("printProvider")
	void printTest(Object input, Object expectedOutput) {
		Builtin.print(input);
		String output = out.toString(StandardCharsets.UTF_8).trim();
		if (shouldReshape() && containsArabic(output)) {
			output = NaftahParserHelper.NORMALIZER.normalize(shape(output));
		}
		assertEquals(output, expectedOutput);
	}
}
