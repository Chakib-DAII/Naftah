package org.daiitech.naftah.parser.provider.script;

import java.util.stream.Stream;

import org.daiitech.naftah.builtin.lang.NaN;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class TryStatementProvider implements ArgumentsProvider {
	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
		return Stream
				.of(
					Arguments
							.of(
								true,
								"""
								حاول(1){
								نجاح(أ) -> {
								إطبع(أ)
								ارجع أ
								}
								فشل(ب) إفعل إطبع(ب)
								}
								""",
								1,
								null),
					Arguments
							.of(
								true,
								"""
								حاول(1 قسمة 0){
								نجاح(أ) -> إطبع(أ)
								فشل(ب) إفعل إلى_نص(ب)
								}
								""",
								"""
								💥 خطأ برمجي!
								استثناء غير ملتقط: خطأ رياضي، مثل القسمة على صفر. (/ by zero)""",
								null),
					Arguments
							.of(
								true,
								"""
								حاول(1 قسمة 0){
								نجاح(أ) -> إطبع(أ)
								فشل(ب) إفعل {
								إطبع(ب)
								حاول(1){
								نجاح(أ) -> إلى_نص(أ)
								فشل(ب) إفعل إطبع(ب)
								}
								}
								}
								""",
								"1",
								null),
					Arguments
							.of(
								true,
								"""
								حاول(1){
								بعض(أ) -> {
								إطبع(أ)
								أ
								}
								معدوم إفعل إطبع("لاشيء.")
								}
								""",
								1,
								null),
					Arguments
							.of(
								true,
								"""
								حاول(لاشيء){
								بعض(أ) -> {
								إطبع(أ)
								أ
								}
								معدوم إفعل إلى_نص("لاشيء.")
								}
								""",
								"لاشيء.",
								null),
					Arguments
							.of(
								true,
								"""
								حاول(باطل){
								بعض(أ) -> {
								إطبع(أ)
								أ
								}
								معدوم إفعل إلى_نص("لاشيء.")
								}
								""",
								"لاشيء.",
								null),
					Arguments
							.of(
								true,
								"""
								حاول(فارغ){
								بعض(أ) -> {
								إطبع(أ)
								أ
								}
								معدوم إفعل إلى_نص("لاشيء.")
								}
								""",
								"لاشيء.",
								null),
					Arguments
							.of(
								true,
								"""
								حاول(ليس_رقم){
								بعض(أ) -> {
								إطبع(أ)
								أ
								}
								معدوم إفعل إلى_نص("لاشيء.")
								}
								""",
								NaN.get(),
								null),
					Arguments
							.of(
								true,
								"""
								حاول(قيمة_غير_رقمية){
								بعض(أ) -> {
								إطبع(أ)
								أ
								}
								معدوم إفعل إلى_نص("لاشيء.")
								}
								""",
								NaN.get(),
								null),
					Arguments
							.of(
								true,
								"""
								حاول(رقم_غير_صالح){
								بعض(أ) -> {
								إطبع(أ)
								أ
								}
								معدوم إفعل إلى_نص("لاشيء.")
								}
								""",
								NaN.get(),
								null),
					Arguments
							.of(
								true,
								"""
								حاول(غير_عددي){
								بعض(أ) -> {
								إطبع(أ)
								أ
								}
								معدوم إفعل إلى_نص("لاشيء.")
								}
								""",
								NaN.get(),
								null),
					Arguments
							.of(
								true,
								"""
								حاول(ليس_عددي){
								بعض(أ) -> {
								إطبع(أ)
								أ
								}
								معدوم إفعل إلى_نص("لاشيء.")
								}
								""",
								NaN.get(),
								null)
				);
	}
}
