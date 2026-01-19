// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

package org.daiitech.naftah.parser.provider.script;

import java.util.List;
import java.util.stream.Stream;

import org.daiitech.naftah.builtin.utils.tuple.Tuple;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class IfStatementProvider implements ArgumentsProvider {
	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
		return Stream
				.of(Arguments
						.of(
							true,
							"""
							متغير أ تعيين ١
							متغير ب تعيين 4

							إذا أ أكبر_من ب إذن {
							أرجع أ
							} غير_ذلك_إذا أ أصغر_من ب إذن {
							أرجع ب
							} غير_ذلك {
							أرجع
							}
							أنهي
							""",
							4,
							null
						),
					Arguments
							.of(
								true,
								"""
								متغير أ تعيين ١
								متغير ب تعيين 4

								أ ؟ أ : ب أنهي
								""",
								1,
								null
							),
					Arguments
							.of(
								true,
								"""
								متغير أ تعيين ١
								متغير ب تعيين 4

								قيمة_غير_رقمية ؟ أ : ب أنهي
								""",
								4,
								null
							),
					Arguments
							.of(
								true,
								"""
								متغير أ تعيين ١
								متغير ب تعيين 4

								فارغ ؟ أ : ب أنهي
								""",
								4,
								null
							),
					Arguments
							.of(
								true,
								"""
								متغير أ تعيين ١
								متغير ب تعيين 4

								(
								(أ ؟ أ : ب) ،
								(قيمة_غير_رقمية ؟ أ : ب ) ،
								(فارغ ؟ أ : ب ) ،
								((أ زائد ب أكبر_من ١٠) ؟ أ : ب ) ،
								((أ زائد ب أصغر_من ١٠) ؟ (أ ؟ أ : ب) : أ )
								)
								""",
								Tuple.of(1, 4, 4, 4, 1),
								null
							),
					Arguments
							.of(
								true,
								"""
								متغير أ تعيين ١
								متغير ب تعيين 4
								متغير ت تعيين 2
								متغير ث تعيين 3
								متغير ج تعيين 5

								[
								فارغ ؟؟ ب ،
								قيمة_غير_رقمية ؟؟ أ ،
								أ ؟؟ ب ؟؟ ت ،
								أ ؟؟ ب ؟؟ ت ؟؟ ث ؟؟ ج ،
								أ ؟؟ ب ؟؟ ت
								]
								""",
								List.of(4, 1, 1, 1, 1),
								null
							));
	}
}
