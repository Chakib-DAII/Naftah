package org.daiitech.naftah.parser.provider.script;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import org.daiitech.naftah.builtin.utils.Tuple;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class CollectionExpressionProvider implements ArgumentsProvider {
	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
		return Stream
				.of(
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
					Arguments.of(true, "(٣٢، ٤٥)", Tuple.of(32, 45), null),
					Arguments
							.of(true,
								"(١٠، (٩، (٨، (٧، (٦، (٥، (٤، (٣، (٢، ١)))))))))",
								Tuple
										.of(10,
											Tuple
													.of(9,
														Tuple
																.of(8,
																	Tuple
																			.of(7,
																				Tuple
																						.of(6,
																							Tuple
																									.of(5,
																										Tuple
																												.of(4,
																													Tuple
																															.of(3,
																																Tuple
																																		.of(2,
																																			1))))))
																))),
								null),
					Arguments.of(true, "{١، ٢، ٣، ٤}", new HashSet() {
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
										add('أ');
										add('ب');
										add('ج');
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
							put("الأرقام", Tuple.of(1001, 2002));
						}
					}, null)
				);
	}
}
