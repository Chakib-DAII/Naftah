package org.daiitech.naftah.parser.provider.script;

import java.util.stream.Stream;

import org.daiitech.naftah.builtin.lang.None;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class ForStatementProvider implements ArgumentsProvider {
	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
		return Stream
				.of(Arguments
						.of(
							true,
							"""
							كرر_حلقة أ تعيين 1 إلى 3 إفعل {
								إذا أ == 2 إذن {
									اكسر
								}
								أنهي
										}
							غير_ذلك {
								إطبع("لم يتم كسر الحلقة")
								}
							أنهي
							""",
							null,
							null),
					Arguments
							.of(
								true,
								"""
								الخارجي:
								كرر_حلقة أ تعيين 1 إلى 5 إفعل {

									الأوسط:
									كرر_حلقة  ب تعيين 1 إلى 5 إفعل {

										الداخلي:
									كرر_حلقة ت تعيين 1 إلى 5 إفعل {
									إذا ((أ == 3) و (ب == 3) و (ت == 3)) إذن {
											إطبع("كسر الخارجي : أ يساوي ${أ} - ب يساوي ${ب} - ت يساوي ${ت}")
													اكسر الخارجي
											}
										أنهي
								إذا ((أ == 2) و (ب == 2) و (ت == 2)) إذن {
											إطبع("كسر الأوسط : أ يساوي ${أ} - ب يساوي ${ب} - ت يساوي ${ت}")
													اكسر الأوسط
											}
										أنهي
									إذا ت == 2 إذن {
										إطبع("تابع الداخلي : أ يساوي ${أ} - ب يساوي ${ب} - ت يساوي ${ت}")
													تابع
								}
										أنهي
										إطبع("أ يساوي ${أ} - ب يساوي ${ب} - ت يساوي ${ت}")
												}
										أنهي

									}
										أنهي

								}
										أنهي
								""",
								null,
								null
							),
					Arguments
							.of(
								true,
								"""
								الخارجي:
								كرر_حلقة أ تعيين 5 تنازليا 1 إفعل {

									الأوسط:
									كرر_حلقة  ب تعيين 1 إلى 5 إفعل {

										الداخلي:
									كرر_حلقة ت تعيين 5 تنازليا 1 إفعل {
									إذا ((أ == 3) و (ب == 3) و (ت == 3)) إذن {
											إطبع("كسر الخارجي : أ يساوي ${أ} - ب يساوي ${ب} - ت يساوي ${ت}")
													اكسر الخارجي
											}
										أنهي
								إذا ((أ == 2) و (ب == 2) و (ت == 2)) إذن {
											إطبع("كسر الأوسط : أ يساوي ${أ} - ب يساوي ${ب} - ت يساوي ${ت}")
													اكسر الأوسط
											}
										أنهي
									إذا ت == 2 إذن {
										إطبع("تابع الداخلي : أ يساوي ${أ} - ب يساوي ${ب} - ت يساوي ${ت}")
													تابع
								}
										أنهي
										إطبع("أ يساوي ${أ} - ب يساوي ${ب} - ت يساوي ${ت}")
												}
										أنهي

									}
										أنهي

								}
										أنهي
								""",
								null,
								null
							),
					Arguments
							.of(
								true,
								"""
								كرر_حلقة أ تعيين 1 إلى 500 خطوة 10 إفعل {
									إطبع(أ)
								}
								أنهي
								""",
								None.get(),
								null),
					Arguments
							.of(
								true,
								"""
								كرر_حلقة أ تعيين 1 إلى 83647 خطوة 1000 إفعل {
									إطبع(أ)
								}
								أنهي
								""",
								None.get(),
								null)
				);
	}
}
