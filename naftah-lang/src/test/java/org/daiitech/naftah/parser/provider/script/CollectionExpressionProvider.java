// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

package org.daiitech.naftah.parser.provider.script;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Stream;

import org.daiitech.naftah.builtin.utils.tuple.Pair;
import org.daiitech.naftah.builtin.utils.tuple.Tuple;
import org.daiitech.naftah.errors.NaftahBugError;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;


public class CollectionExpressionProvider implements ArgumentsProvider {
	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
		return Stream
				.of(
					Arguments.of(true, "[]", List.of(), null),
					Arguments.of(true, "[٩٢؛]", List.of(92), null),
					Arguments.of(true, "[٨٥، ٩٠، ٧٨، ٩٢]", List.of(85, 90, 78, 92), null),
					Arguments
							.of(true,
								"[١٠ ، [ ٩، [ ٨ ، [ ٧ ، [٦ ، [ ٥ ، [ ٤ ، [٣ ، [٢ ، ١]]]]]]]]]",
								List
										.of(10,
											List
													.of(9,
														List
																.of(8,
																	List
																			.of(7,
																				List
																						.of(6,
																							List
																									.of(5,
																										List
																												.of(4,
																													List
																															.of(3,
																																List
																																		.of(2,
																																			1))))))))),
								null),
					Arguments.of(true, "()", Tuple.of(), null),
					Arguments.of(true, "(٣٢، ٤٥)", Pair.of(32, 45), null),
					Arguments.of(true, "(٣٢،)", Tuple.of(32), null),
					Arguments.of(true, "(٣٢؛)", Tuple.of(32), null),
					Arguments
							.of(true,
								"(١٠، (٩، (٨، (٧، (٦، (٥، (٤، (٣، (٢، ١)))))))))",
								Pair
										.of(10,
											Pair
													.of(9,
														Pair
																.of(8,
																	Pair
																			.of(7,
																				Pair
																						.of(6,
																							Pair
																									.of(5,
																										Pair
																												.of(4,
																													Pair
																															.of(3,
																																Pair
																																		.of(2,
																																			1))))))
																))),
								null),
					Arguments.of(true, "#{}", new HashSet<>(), null),
					Arguments.of(true, "#{١؛}", new HashSet<>() {
						{
							add(1);
						}
					}, null),
					Arguments.of(true, "{١،}", new HashSet<>() {
						{
							add(1);
						}
					}, null),
					Arguments.of(true, "{١؛}", new HashSet<>() {
						{
							add(1);
						}
					}, null),
					Arguments.of(true, "{١، ٢، ٣، ٤}", new HashSet<>() {
						{
							add(1);
							add(2);
							add(3);
							add(4);
						}
					}, null),
					Arguments
							.of(true,
								"{\"أ\", \"ب\", \"ج\"}",
								new HashSet<>() {
									{
										add("أ");
										add("ب");
										add("ج");
									}
								},
								null),
					Arguments
							.of(true,
								"{\"اسم\": \"أحمد\", \"عمر\": ٢٠, \"معدل\": ٨٨}",
								new HashMap<>() {
									{
										put("اسم", "أحمد");
										put("عمر", 20);
										put("معدل", 88);
									}
								},
								null),
					Arguments.of(true, "مرتب {١، ٢، ٣، ٤}", new LinkedHashSet<>() {
						{
							add(1);
							add(2);
							add(3);
							add(4);
						}
					}, null),
					Arguments
							.of(true,
								"مرتب {'أ', 'ب', 'ج'}",
								new LinkedHashSet<>() {
									{
										add('أ');
										add('ب');
										add('ج');
									}
								},
								null),
					Arguments
							.of(true,
								"${}",
								new HashMap<>(),
								null),
					Arguments
							.of(true,
								"${\"اسم\": \"أحمد\", \"عمر\": ٢٠, \"معدل\": ٨٨}",
								new LinkedHashMap<>() {
									{
										put("اسم", "أحمد");
										put("عمر", 20);
										put("معدل", 88);
									}
								},
								null),
					Arguments
							.of(true,
								"مرتب {\"اسم\": \"أحمد\", \"عمر\": ٢٠, \"معدل\": ٨٨}",
								new LinkedHashMap<>() {
									{
										put("اسم", "أحمد");
										put("عمر", 20);
										put("معدل", 88);
									}
								},
								null),
					Arguments
							.of(true,
								"[{\"اسم\": \"ليلى\", \"عمر\": ١٨}, {\"اسم\": \"كريم\", \"عمر\": ١٩}]",
								List.of(new HashMap<>() {
									{
										put("اسم", "ليلى");
										put("عمر", 18);
									}
								}, new HashMap<>() {
									{
										put("اسم", "كريم");
										put("عمر", 19);
									}
								}),
								null),
					Arguments.of(true, """
										{
											"الاسم": "سارة",
											"العناوين": ["المنزل", "العمل"],
											"الأرقام": (١٠٠١، ٢٠٠٢)
										}
										""", new HashMap<>() {
						{
							put("الاسم", "سارة");
							put("العناوين", List.of("المنزل", "العمل"));
							put("الأرقام", Pair.of(1001, 2002));
						}
					}, null),
					Arguments.of(true, "2 * [٨٥، ٩٠، ٧٨، ٩٢]", List.of(170, 180, 156, 184), null),
					Arguments.of(true, "2 + [٨٥، ٩٠، ٧٨، ٩٢]", List.of(87, 92, 80, 94), null),
					Arguments.of(true, "2 - [٨٥، ٩٠، ٧٨، ٩٢]", List.of(-83, -88, -76, -90), null),
					Arguments.of(true, "2 / [٨٥، ٩٠، ٧٨، ٩٢]", List.of(0, 0, 0, 0), null),
					Arguments.of(true, "2 % [٨٥، ٩٠، ٧٨، ٩٢]", List.of(2, 2, 2, 2), null),
					Arguments.of(true, "[٨٥، ٩٠، ٧٨، ٩٢] * 2", List.of(170, 180, 156, 184), null),
					Arguments.of(true, "[٨٥، ٩٠، ٧٨، ٩٢] + 2", List.of(87, 92, 80, 94), null),
					Arguments.of(true, "[٨٥، ٩٠، ٧٨، ٩٢] - 2", List.of(83, 88, 76, 90), null),
					Arguments.of(true, "[٨٥، ٩٠، ٧٨، ٩٢] / 2", List.of(42, 45, 39, 46), null),
					Arguments.of(true, "[٨٥، ٩٠، ٧٨، ٩٢] % 2", List.of(1, 0, 0, 0), null),
					Arguments.of(true, "[٨٥، ٩٠، ٧٨، ٩٢]++", List.of(85, 90, 78, 92), null),
					Arguments.of(true, "++[٨٥، ٩٠، ٧٨، ٩٢]", List.of(86, 91, 79, 93), null),
					Arguments.of(true, "[٨٥، ٩٠، ٧٨، ٩٢]--", List.of(85, 90, 78, 92), null),
					Arguments.of(true, "--[٨٥، ٩٠، ٧٨، ٩٢]", List.of(84, 89, 77, 91), null),
					Arguments.of(true, "بت_ليس [٨٥، ٩٠، ٧٨، ٩٢]", List.of(-86, -91, -79, -93), null),
					Arguments
							.of(true,
								"[٨٥، ٩٠، ٧٨، ٩٢] * [٨٥، ٩٠، ٧٨، ٩٢]",
								List.of(7225, 8100, 6084, 8464),
								null),
					Arguments.of(true, "[٨٥، ٩٠، ٧٨، ٩٢] + [٨٥، ٩٠، ٧٨، ٩٢]", List.of(170, 180, 156, 184), null),
					Arguments
							.of(false,
								"[٨٥، ٩٠، ٧٨] + [٨٥، ٩٠، ٧٨، ٩٢]",
								null,
								new NaftahBugError("""
													يجب أن تكون أحجام المصفوفات متساوية.
													'[85, 90, 78]'
													'[85, 90, 78, 92]'
													""", 1, 0)),
					Arguments.of(true, "[٨٥، ٩٠، ٧٨، ٩٢] - [٨٥، ٩٠، ٧٨، ٩٢]", List.of(0, 0, 0, 0), null),
					Arguments.of(true, "[٨٥، ٩٠، ٧٨، ٩٢] / [٨٥، ٩٠، ٧٨، ٩٢]", List.of(1, 1, 1, 1), null),
					Arguments.of(true, "[٨٥، ٩٠، ٧٨، ٩٢] % [٨٥، ٩٠، ٧٨، ٩٢]", List.of(0, 0, 0, 0), null),
					Arguments.of(true, "[٨٥، ٩٠، ٧٨، ٩٢] .*. [٨٥، ٩٠، ٧٨، ٩٢]", List.of(85, 90, 78, 92), null),
					Arguments.of(true, "[٨٥، ٩٠، ٧٨، ٩٢] .+. [٨٥، ٩٠، ٧٨، ٩٢]", List.of(0, 0, 0, 0), null),
					Arguments.of(true, "[٨٥، ٩٠، ٧٨، ٩٢] .-. [٨٥، ٩٠، ٧٨، ٩٢]", List.of(-1, -1, -1, -1), null),
					Arguments.of(true, "[٨٥، ٩٠، ٧٨، ٩٢] ./. [2، 2، 2، 2]", List.of(21, 22, 19, 23), null),
					Arguments.of(true, "[٨٥، ٩٠، ٧٨، ٩٢] .%. [٨٥، ٩٠، ٧٨، ٩٢]", List.of(84, 88, 76, 88), null),
					Arguments.of(true, "[٨٥، ٩٠، ٧٨، ٩٢] بت_و [٨٥، ٩٠، ٧٨، ٩٢]", List.of(85, 90, 78, 92), null),
					Arguments.of(true, "[٨٥، ٩٠، ٧٨، ٩٢] بت_أو [٨٥، ٩٠، ٧٨، ٩٢]", List.of(85, 90, 78, 92), null),
					Arguments
							.of(true,
								"[٨٥، ٩٠، ٧٨، ٩٢] بت_أو_حصري [٨٥، ٩٠، ٧٨، ٩٢]",
								List.of(0, 0, 0, 0),
								null),
					Arguments
							.of(true,
								"[٨٥، ٩٠، ٧٨، ٩٢] أصغر_من [٨٥، ٩٠، ٧٨، ٩٢]",
								List.of(false, false, false, false),
								null),
					Arguments
							.of(true,
								"[٨٥، ٩٠، ٧٨، ٩٢] أكبر_من [٨٥، ٩٠، ٧٨، ٩٢]",
								List.of(false, false, false, false),
								null),
					Arguments
							.of(true,
								"[٨٥، ٩٠، ٧٨، ٩٢] أصغر_أو_يساوي [٨٥، ٩٠، ٧٨، ٩٢]",
								List.of(true, true, true, true),
								null),
					Arguments
							.of(true,
								"[٨٥، ٩٠، ٧٨، ٩٢] أكبر_أو_يساوي [٨٥، ٩٠، ٧٨، ٩٢]",
								List.of(true, true, true, true),
								null),
					Arguments
							.of(true,
								"[٨٥، ٩٠، ٧٨، ٩٢] يساوي [٨٥، ٩٠، ٧٨، ٩٢]",
								List.of(true, true, true, true),
								null),
					Arguments
							.of(true,
								"[٨٥، ٩٠، ٧٨، ٩٢] لا_يساوي [٨٥، ٩٠، ٧٨، ٩٢]",
								List.of(false, false, false, false),
								null),
					Arguments.of(true, "2 * (٨٥، ٩٠، ٧٨، ٩٢)", Tuple.of(170, 180, 156, 184), null),
					Arguments.of(true, "2 + (٨٥، ٩٠، ٧٨، ٩٢)", Tuple.of(87, 92, 80, 94), null),
					Arguments.of(true, "2 - (٨٥، ٩٠، ٧٨، ٩٢)", Tuple.of(-83, -88, -76, -90), null),
					Arguments.of(true, "2 / (٨٥، ٩٠، ٧٨، ٩٢)", Tuple.of(0, 0, 0, 0), null),
					Arguments.of(true, "2 % (٨٥، ٩٠، ٧٨، ٩٢)", Tuple.of(2, 2, 2, 2), null),
					Arguments.of(true, "(٨٥، ٩٠، ٧٨، ٩٢) * 2", Tuple.of(170, 180, 156, 184), null),
					Arguments.of(true, "(٨٥، ٩٠، ٧٨، ٩٢) + 2", Tuple.of(87, 92, 80, 94), null),
					Arguments.of(true, "(٨٥، ٩٠، ٧٨، ٩٢) - 2", Tuple.of(83, 88, 76, 90), null),
					Arguments.of(true, "(٨٥، ٩٠، ٧٨، ٩٢) / 2", Tuple.of(42, 45, 39, 46), null),
					Arguments.of(true, "(٨٥، ٩٠، ٧٨، ٩٢) % 2", Tuple.of(1, 0, 0, 0), null),
					Arguments.of(true, "(٨٥، ٩٠، ٧٨، ٩٢)++", Tuple.of(85, 90, 78, 92), null),
					Arguments.of(true, "++(٨٥، ٩٠، ٧٨، ٩٢)", Tuple.of(86, 91, 79, 93), null),
					Arguments.of(true, "(٨٥، ٩٠، ٧٨، ٩٢)--", Tuple.of(85, 90, 78, 92), null),
					Arguments.of(true, "--(٨٥، ٩٠، ٧٨، ٩٢)", Tuple.of(84, 89, 77, 91), null),
					Arguments.of(true, "بت_ليس (٨٥، ٩٠، ٧٨، ٩٢)", Tuple.of(-86, -91, -79, -93), null),
					Arguments
							.of(true,
								"(٨٥، ٩٠، ٧٨، ٩٢) * (٨٥، ٩٠، ٧٨، ٩٢)",
								Tuple.of(7225, 8100, 6084, 8464),
								null),
					Arguments
							.of(false,
								"(٨٥، ٩٠، ٧٨) * (٨٥، ٩٠، ٧٨، ٩٢)",
								null,
								new NaftahBugError("""
													يجب أن تكون أحجام المصفوفات متساوية.
													'[85, 90, 78]'
													'[85, 90, 78, 92]'
													""", 1, 0)),
					Arguments.of(true, "(٨٥، ٩٠، ٧٨، ٩٢) + (٨٥، ٩٠، ٧٨، ٩٢)", Tuple.of(170, 180, 156, 184), null),
					Arguments.of(true, "(٨٥، ٩٠، ٧٨، ٩٢) - (٨٥، ٩٠، ٧٨، ٩٢)", Tuple.of(0, 0, 0, 0), null),
					Arguments.of(true, "(٨٥، ٩٠، ٧٨، ٩٢) / (٨٥، ٩٠، ٧٨، ٩٢)", Tuple.of(1, 1, 1, 1), null),
					Arguments.of(true, "(٨٥، ٩٠، ٧٨، ٩٢) % (٨٥، ٩٠، ٧٨، ٩٢)", Tuple.of(0, 0, 0, 0), null),
					Arguments.of(true, "(٨٥، ٩٠، ٧٨، ٩٢) .*. (٨٥، ٩٠، ٧٨، ٩٢)", Tuple.of(85, 90, 78, 92), null),
					Arguments.of(true, "(٨٥، ٩٠، ٧٨، ٩٢) .+. (٨٥، ٩٠، ٧٨، ٩٢)", Tuple.of(0, 0, 0, 0), null),
					Arguments.of(true, "(٨٥، ٩٠، ٧٨، ٩٢) .-. (٨٥، ٩٠، ٧٨، ٩٢)", Tuple.of(-1, -1, -1, -1), null),
					Arguments.of(true, "(٨٥، ٩٠، ٧٨، ٩٢) ./. [2، 2، 2، 2]", Tuple.of(21, 22, 19, 23), null),
					Arguments.of(true, "(٨٥، ٩٠، ٧٨، ٩٢) .%. (٨٥، ٩٠، ٧٨، ٩٢)", Tuple.of(84, 88, 76, 88), null),
					Arguments.of(true, "(٨٥، ٩٠، ٧٨، ٩٢) بت_و (٨٥، ٩٠، ٧٨، ٩٢)", Tuple.of(85, 90, 78, 92), null),
					Arguments.of(true, "(٨٥، ٩٠، ٧٨، ٩٢) بت_أو (٨٥، ٩٠، ٧٨، ٩٢)", Tuple.of(85, 90, 78, 92), null),
					Arguments
							.of(true,
								"(٨٥، ٩٠، ٧٨، ٩٢) بت_أو_حصري (٨٥، ٩٠، ٧٨، ٩٢)",
								Tuple.of(0, 0, 0, 0),
								null),
					Arguments
							.of(true,
								"(٨٥، ٩٠، ٧٨، ٩٢) أصغر_من (٨٥، ٩٠، ٧٨، ٩٢)",
								Tuple.of(false, false, false, false),
								null),
					Arguments
							.of(true,
								"(٨٥، ٩٠، ٧٨، ٩٢) أكبر_من (٨٥، ٩٠، ٧٨، ٩٢)",
								Tuple.of(false, false, false, false),
								null),
					Arguments
							.of(true,
								"(٨٥، ٩٠، ٧٨، ٩٢) أصغر_أو_يساوي (٨٥، ٩٠، ٧٨، ٩٢)",
								Tuple.of(true, true, true, true),
								null),
					Arguments
							.of(true,
								"(٨٥، ٩٠، ٧٨، ٩٢) أكبر_أو_يساوي (٨٥، ٩٠، ٧٨، ٩٢)",
								Tuple.of(true, true, true, true),
								null),
					Arguments
							.of(true,
								"(٨٥، ٩٠، ٧٨، ٩٢) يساوي (٨٥، ٩٠، ٧٨، ٩٢)",
								Tuple.of(true, true, true, true),
								null),
					Arguments
							.of(true,
								"(٨٥، ٩٠، ٧٨، ٩٢) لا_يساوي (٨٥، ٩٠، ٧٨، ٩٢)",
								Tuple.of(false, false, false, false),
								null),
					Arguments.of(true, "2 * {٨٥، ٩٠، ٧٨، ٩٢}", List.of(170, 180, 184, 156), null),
					Arguments.of(true, "2 + {٨٥، ٩٠، ٧٨، ٩٢}", List.of(87, 92, 94, 80), null),
					Arguments.of(true, "2 - {٨٥، ٩٠، ٧٨، ٩٢}", List.of(-83, -88, -90, -76), null),
					Arguments.of(true, "2 / {٨٥، ٩٠، ٧٨، ٩٢}", List.of(0, 0, 0, 0), null),
					Arguments.of(true, "2 % {٨٥، ٩٠، ٧٨، ٩٢}", List.of(2, 2, 2, 2), null),
					Arguments.of(true, "{٨٥، ٩٠، ٧٨، ٩٢} * 2", List.of(170, 180, 184, 156), null),
					Arguments.of(true, "{٨٥، ٩٠، ٧٨، ٩٢} + 2", List.of(87, 92, 94, 80), null),
					Arguments.of(true, "{٨٥، ٩٠، ٧٨، ٩٢} - 2", List.of(83, 88, 90, 76), null),
					Arguments.of(true, "{٨٥، ٩٠، ٧٨، ٩٢} / 2", List.of(42, 45, 46, 39), null),
					Arguments.of(true, "{٨٥، ٩٠، ٧٨، ٩٢} % 2", List.of(1, 0, 0, 0), null),
					Arguments.of(true, "{٨٥، ٩٠، ٧٨، ٩٢}++", List.of(85, 90, 92, 78), null),
					Arguments.of(true, "++{٨٥، ٩٠، ٧٨، ٩٢}", List.of(86, 91, 93, 79), null),
					Arguments.of(true, "{٨٥، ٩٠، ٧٨، ٩٢}--", List.of(85, 90, 92, 78), null),
					Arguments.of(true, "--{٨٥، ٩٠، ٧٨، ٩٢}", List.of(84, 89, 91, 77), null),
					Arguments.of(true, "بت_ليس {٨٥، ٩٠، ٧٨، ٩٢}", List.of(-86, -91, -93, -79), null),
					Arguments
							.of(true,
								"{٨٥، ٩٠، ٧٨، ٩٢} * {٨٥، ٩٠، ٧٨، ٩٢}",
								List.of(7225, 8100, 8464, 6084),
								null),
					Arguments.of(true, "{٨٥، ٩٠، ٧٨، ٩٢} + {٨٥، ٩٠، ٧٨، ٩٢}", List.of(170, 180, 184, 156), null),
					Arguments.of(true, "{٨٥، ٩٠، ٧٨، ٩٢} - {٨٥، ٩٠، ٧٨، ٩٢}", List.of(0, 0, 0, 0), null),
					Arguments
							.of(false,
								"{٨٥، ٩٠، ٧٨} - {٨٥، ٩٠، ٧٨، ٩٢}",
								null,
								new NaftahBugError("""
													يجب أن تكون أحجام المصفوفات متساوية.
													'[85, 90, 78]'
													'[85, 90, 92, 78]'
													""", 1, 0)),
					Arguments.of(true, "{٨٥، ٩٠، ٧٨، ٩٢} / {٨٥، ٩٠، ٧٨، ٩٢}", List.of(1, 1, 1, 1), null),
					Arguments.of(true, "{٨٥، ٩٠، ٧٨، ٩٢} % {٨٥، ٩٠، ٧٨، ٩٢}", List.of(0, 0, 0, 0), null),
					Arguments.of(true, "{٨٥، ٩٠، ٧٨، ٩٢} .*. {٨٥، ٩٠، ٧٨، ٩٢}", List.of(85, 90, 92, 78), null),
					Arguments.of(true, "{٨٥، ٩٠، ٧٨، ٩٢} .+. {٨٥، ٩٠، ٧٨، ٩٢}", List.of(0, 0, 0, 0), null),
					Arguments.of(true, "{٨٥، ٩٠، ٧٨، ٩٢} .-. {٨٥، ٩٠، ٧٨، ٩٢}", List.of(-1, -1, -1, -1), null),
					Arguments.of(true, "{٨٥، ٩٠، ٧٨، ٩٢} ./. [2، 2، 2، 2]", List.of(21, 22, 23, 19), null),
					Arguments.of(true, "{٨٥، ٩٠، ٧٨، ٩٢} .%. {٨٥، ٩٠، ٧٨، ٩٢}", List.of(84, 88, 88, 76), null),
					Arguments.of(true, "{٨٥، ٩٠، ٧٨، ٩٢} بت_و {٨٥، ٩٠، ٧٨، ٩٢}", List.of(85, 90, 92, 78), null),
					Arguments.of(true, "{٨٥، ٩٠، ٧٨، ٩٢} بت_أو {٨٥، ٩٠، ٧٨، ٩٢}", List.of(85, 90, 92, 78), null),
					Arguments
							.of(true,
								"{٨٥، ٩٠، ٧٨، ٩٢} بت_أو_حصري {٨٥، ٩٠، ٧٨، ٩٢}",
								List.of(0, 0, 0, 0),
								null),
					Arguments
							.of(true,
								"{٨٥، ٩٠، ٧٨، ٩٢} أصغر_من {٨٥، ٩٠، ٧٨، ٩٢}",
								List.of(false, false, false, false),
								null),
					Arguments
							.of(true,
								"{٨٥، ٩٠، ٧٨، ٩٢} أكبر_من {٨٥، ٩٠، ٧٨، ٩٢}",
								List.of(false, false, false, false),
								null),
					Arguments
							.of(true,
								"{٨٥، ٩٠، ٧٨، ٩٢} أصغر_أو_يساوي {٨٥، ٩٠، ٧٨، ٩٢}",
								List.of(true, true, true, true),
								null),
					Arguments
							.of(true,
								"{٨٥، ٩٠، ٧٨، ٩٢} أكبر_أو_يساوي {٨٥، ٩٠، ٧٨، ٩٢}",
								List.of(true, true, true, true),
								null),
					Arguments
							.of(true,
								"{٨٥، ٩٠، ٧٨، ٩٢} يساوي {٨٥، ٩٠، ٧٨، ٩٢}",
								List.of(true, true, true, true),
								null),
					Arguments
							.of(true,
								"{٨٥، ٩٠، ٧٨، ٩٢} لا_يساوي {٨٥، ٩٠، ٧٨، ٩٢}",
								List.of(false, false, false, false),
								null),
					Arguments
							.of(true,
								"""
															2 * {"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨}
								""",
								new HashMap<>() {
									{
										put("اسم", 12680);
										put("عمر", 40);
										put("معدل", 176);
									}
								},
								null),
					Arguments
							.of(true,
								"""
															2 + {"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨}
								""",
								new HashMap<>() {
									{
										put("اسم", 6342);
										put("عمر", 22);
										put("معدل", 90);
									}
								},
								null),
					Arguments
							.of(true,
								"""
									2 - {"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨}
								""",
								new HashMap<>() {
									{
										put("اسم", -6338);
										put("عمر", -18);
										put("معدل", -86);
									}
								},
								null),
					Arguments
							.of(true,
								"""
								2 / {"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨}
								""",
								new HashMap<>() {
									{
										put("اسم", 0);
										put("عمر", 0);
										put("معدل", 0);
									}
								},
								null),
					Arguments
							.of(true,
								"""
								2 % {"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨}
								""",
								new HashMap<>() {
									{
										put("اسم", 2);
										put("عمر", 2);
										put("معدل", 2);
									}
								},
								null),
					Arguments
							.of(true,
								"""
								{"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨} * 2
								""",
								new HashMap<>() {
									{
										put("اسم", "أحمدأحمد");
										put("عمر", 40);
										put("معدل", 176);
									}
								},
								null),
					Arguments
							.of(true,
								"""
								{"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨} + 2
								""",
								new HashMap<>() {
									{
										put("اسم", "أحمد2");
										put("عمر", 22);
										put("معدل", 90);
									}
								},
								null),
					Arguments
							.of(true,
								"""
								{"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨} - 2
								""",
								new HashMap<>() {
									{
										put("اسم", "أحمد");
										put("عمر", 18);
										put("معدل", 86);
									}
								},
								null),
					Arguments
							.of(true,
								"""
								{"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨} / 2
								""",
								new HashMap<>() {
									{
										put("اسم", new String[]{"أح", "مد"});
										put("عمر", 10);
										put("معدل", 44);
									}
								},
								null),
					Arguments
							.of(true,
								"""
								{"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨} % 2
								""",
								new HashMap<>() {
									{
										put("اسم", "\u0015");
										put("عمر", 0);
										put("معدل", 0);
									}
								},
								null),
					Arguments
							.of(true,
								"""
								{"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨}++
								""",
								new HashMap<>() {
									{
										put("اسم", "أحمد");
										put("عمر", 20);
										put("معدل", 88);
									}
								},
								null),
					Arguments
							.of(true,
								"""
								++{"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨}

								""",
								new HashMap<>() {
									{
										put("اسم", "ؤخنذ");
										put("عمر", 21);
										put("معدل", 89);
									}
								},
								null),
					Arguments
							.of(true,
								"""
								{"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨}--

								""",
								new HashMap<>() {
									{
										put("اسم", "أحمد");
										put("عمر", 20);
										put("معدل", 88);
									}
								},
								null),
					Arguments
							.of(true,
								"""
								--{"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨}

								""",
								new HashMap<>() {
									{
										put("اسم", "آجلخ");
										put("عمر", 19);
										put("معدل", 87);
									}
								},
								null),
					Arguments
							.of(true,
								"""
								بت_ليس {"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨}
								""",
								new HashMap<>() {
									{
										put("اسم", "隆戮了類");
										put("عمر", -21);
										put("معدل", -89);
									}
								},
								null),
					Arguments
							.of(true,
								"""
								{"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨} * {"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨}

								""",
								new HashMap<>() {
									{
										put("اسم", "\uA8C9⏩亙㲡");
										put("عمر", 400);
										put("معدل", 7744);
									}
								},
								null),
					Arguments
							.of(true,
								"""
								{"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨} + {"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨}

								""",
								new HashMap<>() {
									{
										put("اسم", "أحمدأحمد");
										put("عمر", 40);
										put("معدل", 176);
									}
								},
								null),
					Arguments
							.of(true,
								"""
								{"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨} - {"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨}

								""",
								new HashMap<>() {
									{
										put("اسم", "");
										put("عمر", 0);
										put("معدل", 0);
									}
								},
								null),
					Arguments
							.of(true,

								"""
								{"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨} / {"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨}

								""",
								new HashMap<>() {
									{
										put("اسم", new String[0]);
										put("عمر", 1);
										put("معدل", 1);
									}
								},
								null),
					Arguments
							.of(false,
								"""
								{"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨} / {"اسم1": "أحمد", "عمر": ٢٠, "معدل": ٨٨}

								""",
								null,
								new NaftahBugError("""
													المفتاح 'اسم' غير موجود في المصفوفة الترابطية الثانية.""", 1, 0)),
					Arguments
							.of(true,

								"""
								{ "عمر": ٢٠, "معدل": ٨٨} % { "عمر": ٢٠, "معدل": ٨٨}

								""",
								new HashMap<>() {
									{
										put("عمر", 0);
										put("معدل", 0);
									}
								},
								null),
					Arguments
							.of(true,

								"""
								{"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨} .*. {"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨}

								""",
								new HashMap<>() {
									{
										put("اسم", "\uA8C9⏩亙㲡");
										put("عمر", 20);
										put("معدل", 88);
									}
								},
								null),
					Arguments
							.of(true,

								"""
								{"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨} .+. {"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨}

								""",
								new HashMap<>() {
									{
										put("اسم", "ెౚಊ\u0C5E");
										put("عمر", 0);
										put("معدل", 0);
									}
								},
								null),
					Arguments
							.of(true,

								"""
								{ "عمر": ٢٠, "معدل": ٨٨} .-. { "عمر": ٢٠, "معدل": ٨٨}

								""",
								new HashMap<>() {
									{
										put("عمر", -1);
										put("معدل", -1);
									}
								},
								null),
					Arguments
							.of(true,

								"""
								{ "عمر": ٢٠, "معدل": ٨٨} ./. {"عمر": 2, "معدل": 7}

								""",
								new HashMap<>() {
									{
										put("عمر", 5);
										put("معدل", 0);
									}
								},
								null),
					Arguments
							.of(true,

								"""
								{"عمر": ٢٠, "معدل": ٨٨} .%. {"عمر": ٢٠, "معدل": ٨٨}

								""",
								new HashMap<>() {
									{
										put("عمر", 16);
										put("معدل", 80);
									}
								},
								null),
					Arguments
							.of(true,

								"""
								{"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨} بت_و {"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨}

								""",
								new HashMap<>() {
									{
										put("اسم", "أحمد");
										put("عمر", 20);
										put("معدل", 88);
									}
								},
								null),
					Arguments
							.of(true,
								"""
								{"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨} بت_أو {"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨}

								""",
								new HashMap<>() {
									{
										put("اسم", "أحمد");
										put("عمر", 20);
										put("معدل", 88);
									}
								},
								null),
					Arguments
							.of(true,

								"""
								{"عمر": ٢٠, "معدل": ٨٨} بت_أو_حصري {"عمر": ٢٠, "معدل": ٨٨}

								""",
								new HashMap<>() {
									{
										put("عمر", 0);
										put("معدل", 0);
									}
								},
								null),
					Arguments
							.of(true,
								"""
								{"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨} أصغر_من {"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨}

								""",
								new HashMap<>() {
									{
										put("اسم", false);
										put("عمر", false);
										put("معدل", false);
									}
								},
								null),
					Arguments
							.of(true,
								"""
								{"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨} أكبر_من {"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨}

								""",
								new HashMap<>() {
									{
										put("اسم", false);
										put("عمر", false);
										put("معدل", false);
									}
								},
								null),
					Arguments
							.of(true,
								"""
								{"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨} أصغر_أو_يساوي {"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨}

								""",
								new HashMap<>() {
									{
										put("اسم", true);
										put("عمر", true);
										put("معدل", true);
									}
								},
								null),
					Arguments
							.of(true,
								"""
								{"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨} أكبر_أو_يساوي {"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨}

								""",
								new HashMap<>() {
									{
										put("اسم", true);
										put("عمر", true);
										put("معدل", true);
									}
								},
								null),
					Arguments
							.of(true,
								"""
								{"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨} يساوي {"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨}

								""",
								new HashMap<>() {
									{
										put("اسم", true);
										put("عمر", true);
										put("معدل", true);
									}
								},
								null),
					Arguments
							.of(true,
								"""
								{"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨} لا_يساوي {"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨}

								""",

								new HashMap<>() {
									{
										put("اسم", false);
										put("عمر", false);
										put("معدل", false);
									}
								},
								null),
					Arguments
							.of(false,
								"""
								{"اسم": "أحمد", "عمر": ٢٠, "معدل": ٨٨} لا_يساوي {"اسم": "أحمد", "معدل": ٨٨}

								""",
								null,
								new NaftahBugError("""
													يجب أن تكون أحجام المصفوفات الترابطية متساوية.
													'{معدل=88, عمر=20, اسم=أحمد}'
													'{معدل=88, اسم=أحمد}'
													""", 1, 0))
				);
	}
}
