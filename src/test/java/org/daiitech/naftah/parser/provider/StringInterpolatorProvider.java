package org.daiitech.naftah.parser.provider;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.daiitech.naftah.parser.LoopSignal;
import org.daiitech.naftah.parser.NaftahParserHelper;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import static org.daiitech.naftah.parser.LoopSignal.BREAK;
import static org.daiitech.naftah.parser.LoopSignal.CONTINUE;
import static org.daiitech.naftah.parser.LoopSignal.NONE;
import static org.daiitech.naftah.parser.LoopSignal.RETURN;

public class StringInterpolatorProvider implements ArgumentsProvider {
	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
		return Stream
				.of(
					Arguments.of(true, "مرحباً ${الاسم}", Map.of(), "مرحباً <فارغ>", null),
					Arguments.of(true, "${الاسم}", Map.of(), NaftahParserHelper.NULL, null),
					Arguments.of(true, "مرحباً ${الاسم}", Map.of("الاسم", "علي"), "مرحباً علي", null),
					Arguments.of(true, "مرحباً {الاسم}$", Map.of("الاسم", "علي"), "مرحباً علي", null),
					Arguments.of(true, "مرحباً {{الاسم}}", Map.of("الاسم", "علي"), "مرحباً علي", null),
					Arguments.of(true, "مرحباً ${الاسم:علي}", Map.of(), "مرحباً علي", null),
					Arguments.of(true, "مرحباً {الاسم:علي}$", Map.of(), "مرحباً علي", null),
					Arguments.of(true, "مرحباً {{الاسم:علي}}", Map.of(), "مرحباً علي", null),
					Arguments.of(true, "${عدد}", Map.of("عدد", 10000.006), "10000٫006", null),
					Arguments.of(true, "${منطقي}", Map.of("منطقي", true), "صحيح", null),
					Arguments.of(true, "${منطقي}", Map.of("منطقي", false), "خطأ", null),
					Arguments
							.of(true,
								"${إشارة_حلقة}",
								Map.of("إشارة_حلقة", LoopSignal.LoopSignalDetails.of(NONE, null)),
								NaftahParserHelper.NULL,
								null),
					Arguments
							.of(true,
								"مرحباً ${إشارة_حلقة}",
								Map.of("إشارة_حلقة", LoopSignal.LoopSignalDetails.of(CONTINUE, "", "", null)),
								"مرحباً <فارغ>",
								null),
					Arguments
							.of(true,
								"${إشارة_حلقة}",
								Map.of("إشارة_حلقة", LoopSignal.LoopSignalDetails.of(BREAK, 10000.006)),
								"10000٫006",
								null),
					Arguments
							.of(true,
								"${إشارة_حلقة}",
								Map.of("إشارة_حلقة", LoopSignal.LoopSignalDetails.of(RETURN, false)),
								"خطأ",
								null),
					Arguments
							.of(true,
								"${قائمة}",
								Map.of("قائمة", new int[]{1, 2}),
								"قائمة: " + Arrays.toString(new int[]{1, 2}),
								null),
					Arguments
							.of(true,
								"${قائمة}",
								Map.of("قائمة", new long[]{1, 2}),
								"قائمة: " + Arrays.toString(new long[]{1, 2}),
								null),
					Arguments
							.of(true,
								"${قائمة}",
								Map.of("قائمة", new double[]{1, 2.5}),
								"قائمة: " + Arrays.toString(new double[]{1, 2.5}),
								null),
					Arguments
							.of(true,
								"${قائمة}",
								Map.of("قائمة", new float[]{1, 2.1F}),
								"قائمة: " + Arrays.toString(new float[]{1, 2.1F}),
								null),
					Arguments
							.of(true,
								"${قائمة}",
								Map.of("قائمة", new boolean[]{true, false}),
								"قائمة: " + Arrays.toString(new boolean[]{true, false}),
								null),
					Arguments
							.of(true,
								"${قائمة}",
								Map.of("قائمة", new char[]{1, 2}),
								"قائمة: " + Arrays.toString(new char[]{1, 2}),
								null),
					Arguments
							.of(true,
								"${قائمة}",
								Map.of("قائمة", new byte[]{1, 2}),
								"قائمة: " + Arrays.toString(new byte[]{1, 2}),
								null),
					Arguments
							.of(true,
								"${قائمة}",
								Map.of("قائمة", new short[]{1, 2}),
								"قائمة: " + Arrays.toString(new short[]{1, 2}),
								null),
					Arguments
							.of(true,
								"${قائمة}",
								Map.of("قائمة", new Object[]{1, 2}),
								"قائمة: " + Arrays.toString(new Object[]{1, 2}),
								null),
					Arguments
							.of(true,
								"${قائمة}",
								Map.of("قائمة", List.of(1, 2)),
								"قائمة: " + List.of(1, 2),
								null),
					Arguments
							.of(true,
								"${مجموعة}",
								Map.of("مجموعة", Set.of(1, 2)),
								"مجموعة: " + Set.of(1, 2),
								null),
					Arguments
							.of(true,
								"${مصفوفة_ترابطية}",
								Map.of("مصفوفة_ترابطية", Map.of(1, 2)),
								"مصفوفة ترابطية: " + Map.of(1, 2),
								null)
				);
	}
}
