// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

package org.daiitech.naftah.parser.provider.script;

import java.time.Duration;
import java.time.Period;
import java.time.ZoneId;
import java.time.chrono.Chronology;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.Objects;
import java.util.stream.Stream;

import org.daiitech.naftah.builtin.time.NaftahDuration;
import org.daiitech.naftah.builtin.time.NaftahPeriod;
import org.daiitech.naftah.builtin.time.NaftahPeriodWithDuration;
import org.daiitech.naftah.builtin.time.NaftahTemporalAmount;
import org.daiitech.naftah.builtin.utils.op.BinaryOperation;
import org.daiitech.naftah.utils.time.ChronologyUtils;
import org.daiitech.naftah.utils.time.TemporalUtils;
import org.daiitech.naftah.utils.time.ZoneUtils;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import static org.daiitech.naftah.utils.time.Constants.DAY;
import static org.daiitech.naftah.utils.time.Constants.HOUR;
import static org.daiitech.naftah.utils.time.Constants.MINUTE;
import static org.daiitech.naftah.utils.time.Constants.MONTH;
import static org.daiitech.naftah.utils.time.Constants.NANOSECOND;
import static org.daiitech.naftah.utils.time.Constants.SECOND;
import static org.daiitech.naftah.utils.time.Constants.YEAR;
import static org.daiitech.naftah.utils.time.ZoneUtils.parseZoneOffset;

public class PeriodDurationProvider implements ArgumentsProvider {
	private NaftahTemporalAmount createArabicPeriodWithDuration(Temporal start, Temporal end) {
		var durationPeriodTuple = TemporalUtils.between(start, end);

		if (durationPeriodTuple.arity() == 1) {
			TemporalAmount temporalAmount = (TemporalAmount) durationPeriodTuple.get(0);
			if (temporalAmount instanceof Duration duration) {
				return NaftahDuration.of(duration);
			}
			else {
				return NaftahPeriod.of((Period) temporalAmount);
			}
		}
		else {
			return NaftahPeriodWithDuration
					.of(
						NaftahPeriod.of((Period) durationPeriodTuple.get(0)),
						NaftahDuration.of((Duration) durationPeriodTuple.get(1))
					);
		}
	}

	private Temporal createDateTime(int day,
									int monthValue,
									Chronology chronology,
									int year,
									Boolean offset,
									String arabicZoneOrOffset,
									int hour,
									int minute,
									Integer second,
									Integer nano,
									Boolean isPM
	) {

		ZoneId zoneId = null;
		if (Objects.nonNull(arabicZoneOrOffset)) {
			if (Boolean.TRUE.equals(offset)) {
				zoneId = parseZoneOffset(arabicZoneOrOffset);
			}
			else {
				zoneId = ZoneId.of(ZoneUtils.zoneNameToJavaZoneId(arabicZoneOrOffset));
			}
		}

		return TemporalUtils
				.createDateTime(
								day,
								monthValue,
								year,
								chronology,
								TemporalUtils.getHour24(hour, isPM),
								minute,
								second,
								nano,
								zoneId
				);
	}

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
		return Stream
				.of(
					Arguments
							.of(true,
								"""
								مقدار_زمني "مدة 3 ساعات"
								""",
								NaftahDuration
										.of(NaftahDuration.DurationDefinition
												.of(3,
													"ساعات",
													0,
													MINUTE,
													0,
													0,
													SECOND,
													0,
													NANOSECOND
												),
											Duration
													.ofHours(3)),
								null),
					Arguments
							.of(true,
								"""
								مقدار_زمني "مدة 2 ساعات و 30 دقيقة"
								""",
								NaftahDuration
										.of(NaftahDuration.DurationDefinition
												.of(2,
													"ساعات",
													30,
													MINUTE,
													0,
													0,
													SECOND,
													0,
													NANOSECOND
												),
											Duration
													.ofHours(2)
													.plusMinutes(30)),
								null),
					Arguments
							.of(true,
								"""
								قيمة_زمنية "مدة 1 ساعة و15 دقيقة"
								""",
								NaftahDuration
										.of(NaftahDuration.DurationDefinition
												.of(1,
													HOUR,
													15,
													MINUTE,
													0,
													0,
													SECOND,
													0,
													NANOSECOND
												),
											Duration
													.ofHours(1)
													.plusMinutes(15)),
								null),
					Arguments
							.of(true,
								"""
								قيمة_زمنية "مدة 2 ساعات و 10 دقائق و 5 ثوان"
								""",
								NaftahDuration
										.of(NaftahDuration.DurationDefinition
												.of(2,
													"ساعات",
													10,
													"دقائق",
													5,
													0,
													"ثوان",
													0,
													NANOSECOND
												),
											Duration
													.ofHours(2)
													.plusMinutes(10)
													.plusSeconds(5)),
								null),
					Arguments
							.of(true,
								"""
								مقدار_زمني "مدة 2.5 ثانية"
								""",
								NaftahDuration
										.of(NaftahDuration.DurationDefinition
												.of(0,
													HOUR,
													0,
													MINUTE,
													2,
													500,
													SECOND,
													0,
													NANOSECOND
												),
											Duration.ofSeconds(2).plusMillis(500)),
								null),
					Arguments
							.of(true,
								"""
								مقدار_زمني "مدة 10.250 ثانية"
								""",
								NaftahDuration
										.of(NaftahDuration.DurationDefinition
												.of(0,
													HOUR,
													0,
													MINUTE,
													10,
													250,
													SECOND,
													0,
													NANOSECOND
												),
											Duration
													.ofSeconds(10)
													.plusMillis(250)),
								null),
					Arguments
							.of(true,
								"""
								قيمة_زمنية "مدة 1 دقيقة و 10 نانوثوان"
								""",
								NaftahDuration
										.of(NaftahDuration.DurationDefinition
												.of(0,
													HOUR,
													1,
													MINUTE,
													0,
													0,
													SECOND,
													10,
													"نانوثوان"
												),
											Duration
													.ofMinutes(1)
													.plusNanos(10)),
								null),
					Arguments
							.of(true,
								"""
								مقدار_زمني "مدة 1 ثانية و 500 نانوثانية"
								""",
								NaftahDuration
										.of(NaftahDuration.DurationDefinition
												.of(0,
													HOUR,
													0,
													MINUTE,
													1,
													0,
													SECOND,
													500,
													NANOSECOND
												),
											Duration
													.ofSeconds(1)
													.plusNanos(500)),
								null),
					Arguments
							.of(true,
								"""
								قيمة_زمنية "مدة 1 ساعة و 30.75 ثانية"
								""",
								NaftahDuration
										.of(NaftahDuration.DurationDefinition
												.of(1,
													HOUR,
													0,
													MINUTE,
													30,
													750,
													SECOND,
													0,
													NANOSECOND
												),
											Duration
													.ofHours(1)
													.plusSeconds(30)
													.plusMillis(750)),
								null),
					Arguments
							.of(true,
								"""
								مقدار_زمني "فترة 1 سنة"
								""",
								NaftahPeriod
										.of(NaftahPeriod.PeriodDefinition
												.of(1,
													YEAR,
													0,
													MONTH,
													0,
													DAY
												),
											Period.ofYears(1)),
								null),
					Arguments
							.of(true,
								"""
								مقدار_زمني "فترة 5 سنوات"
								""",
								NaftahPeriod
										.of(NaftahPeriod.PeriodDefinition
												.of(5,
													"سنوات",
													0,
													MONTH,
													0,
													DAY
												),
											Period.ofYears(5)),
								null),
					Arguments
							.of(true,
								"""
								مقدار_زمني "فترة 1 سنة و 6 أشهر"
								""",
								NaftahPeriod
										.of(NaftahPeriod.PeriodDefinition
												.of(1,
													YEAR,
													6,
													"أشهر",
													0,
													DAY
												),
											Period.ofYears(1).plusMonths(6)),
								null),
					Arguments
							.of(true,
								"""
								مقدار_زمني "فترة 2 سنة و 3 أشهر"
								""",
								NaftahPeriod
										.of(NaftahPeriod.PeriodDefinition
												.of(2,
													YEAR,
													3,
													"أشهر",
													0,
													DAY
												),
											Period.ofYears(2).plusMonths(3)),
								null),
					Arguments
							.of(true,
								"""
								قيمة_زمنية "فترة 1 سنة و 3 أشهر و 10 أيام"
								""",
								NaftahPeriod
										.of(NaftahPeriod.PeriodDefinition
												.of(1,
													YEAR,
													3,
													"أشهر",
													10,
													"أيام"
												),
											Period.of(1, 3, 10)),
								null),
					Arguments
							.of(true,
								"""
								قيمة_زمنية "فترة 6 أشهر"
								""",
								NaftahPeriod
										.of(NaftahPeriod.PeriodDefinition
												.of(0,
													YEAR,
													6,
													"أشهر",
													0,
													DAY
												),
											Period.ofMonths(6)),
								null),
					Arguments
							.of(true,
								"""
								قيمة_زمنية "فترة 1 شهر و 10 أيام"
								""",
								NaftahPeriod
										.of(NaftahPeriod.PeriodDefinition
												.of(0,
													YEAR,
													1,
													MONTH,
													10,
													"أيام"
												),
											Period.ofMonths(1).plusDays(10)),
								null),
					Arguments
							.of(true,
								"""
								قيمة_زمنية "فترة 14 يوم"
								""",
								NaftahPeriod
										.of(NaftahPeriod.PeriodDefinition
												.of(0,
													YEAR,
													0,
													MONTH,
													14,
													DAY
												),
											Period.ofDays(14)),
								null),
					Arguments
							.of(true,
								"""
								مقدار_زمني "فترة 1 سنة و 1 دقيقة و 10 نانوثوان"
								""",
								NaftahPeriodWithDuration
										.of(
											NaftahPeriod
													.of(NaftahPeriod.PeriodDefinition
															.of(1,
																YEAR,
																0,
																MONTH,
																0,
																DAY
															),
														Period.ofYears(1)),
											NaftahDuration
													.of(NaftahDuration.DurationDefinition
															.of(0,
																HOUR,
																1,
																MINUTE,
																0,
																0,
																SECOND,
																10,
																"نانوثوان"
															),
														Duration
																.ofMinutes(1)
																.plusNanos(10))),
								null),
					Arguments
							.of(true,
								"""
								مقدار_زمني "فترة 5 سنوات و 122 ساعات و 10 دقائق و 5 ثوان"
								""",
								NaftahPeriodWithDuration
										.of(
											NaftahPeriod
													.of(NaftahPeriod.PeriodDefinition
															.of(5,
																"سنوات",
																0,
																MONTH,
																5,
																DAY
															),
														Period.ofYears(5).plusDays(5)),
											NaftahDuration
													.of(NaftahDuration.DurationDefinition
															.of(2,
																"ساعات",
																10,
																"دقائق",
																5,
																0,
																"ثوان",
																0,
																NANOSECOND
															),
														Duration
																.ofHours(2)
																.plusMinutes(10)
																.plusSeconds(5))),
								null),
					Arguments
							.of(true,
								"""
								مقدار_زمني "فترة 1 سنة و 6 أشهر و 2 ساعات و 10 دقائق"
								""",
								NaftahPeriodWithDuration
										.of(
											NaftahPeriod
													.of(NaftahPeriod.PeriodDefinition
															.of(1,
																YEAR,
																6,
																"أشهر",
																0,
																DAY
															),
														Period.ofYears(1).plusMonths(6)),
											NaftahDuration
													.of(NaftahDuration.DurationDefinition
															.of(2,
																"ساعات",
																10,
																"دقائق",
																0,
																0,
																SECOND,
																0,
																NANOSECOND
															),
														Duration
																.ofHours(2)
																.plusMinutes(10))),
								null),
					Arguments
							.of(true,
								"""
								قيمة_زمنية "فترة 1 سنة و 3 أشهر و 10 أيام و 25 ساعات و 10 دقائق"
								""",
								NaftahPeriodWithDuration
										.of(
											NaftahPeriod
													.of(NaftahPeriod.PeriodDefinition
															.of(1,
																YEAR,
																3,
																"أشهر",
																11,
																"أيام"
															),
														Period.of(1, 3, 11)),
											NaftahDuration
													.of(NaftahDuration.DurationDefinition
															.of(1,
																"ساعات",
																10,
																"دقائق",
																0,
																0,
																SECOND,
																0,
																NANOSECOND
															),
														Duration
																.ofHours(1)
																.plusMinutes(10))),
								null),
					Arguments
							.of(true,
								"""
								قيمة_زمنية "فترة 6 أشهر و 1 ساعة و15 دقيقة"
								""",
								NaftahPeriodWithDuration
										.of(
											NaftahPeriod
													.of(NaftahPeriod.PeriodDefinition
															.of(0,
																YEAR,
																6,
																"أشهر",
																0,
																DAY
															),
														Period.ofMonths(6)),
											NaftahDuration
													.of(NaftahDuration.DurationDefinition
															.of(1,
																HOUR,
																15,
																MINUTE,
																0,
																0,
																SECOND,
																0,
																NANOSECOND
															),
														Duration
																.ofHours(1)
																.plusMinutes(15))),
								null),
					Arguments
							.of(true,
								"""
								قيمة_زمنية "فترة 1 شهر و 10 أيام و 2 ساعات و 30 دقيقة"
								""",
								NaftahPeriodWithDuration
										.of(
											NaftahPeriod
													.of(NaftahPeriod.PeriodDefinition
															.of(0,
																YEAR,
																1,
																MONTH,
																10,
																"أيام"
															),
														Period.ofMonths(1).plusDays(10)),
											NaftahDuration
													.of(NaftahDuration.DurationDefinition
															.of(2,
																"ساعات",
																30,
																MINUTE,
																0,
																0,
																SECOND,
																0,
																NANOSECOND
															),
														Duration
																.ofHours(2)
																.plusMinutes(30))),
								null),
					Arguments
							.of(true,
								"""
								قيمة_زمنية "فترة 14 يوم و 3 ساعات"
								""",
								NaftahPeriodWithDuration
										.of(
											NaftahPeriod
													.of(NaftahPeriod.PeriodDefinition
															.of(0,
																YEAR,
																0,
																MONTH,
																14,
																DAY
															),
														Period.ofDays(14)),
											NaftahDuration
													.of(NaftahDuration.DurationDefinition
															.of(3,
																"ساعات",
																0,
																MINUTE,
																0,
																0,
																SECOND,
																0,
																NANOSECOND
															),
														Duration
																.ofHours(3))),
								null),
					Arguments
							.of(true,
								"""
								قيمة_زمنية "فترة 14 يوم و 28 ساعات"
								""",
								NaftahPeriodWithDuration
										.of(

											NaftahPeriod
													.of(NaftahPeriod.PeriodDefinition
															.of(0,
																YEAR,
																0,
																MONTH,
																15,
																DAY
															),
														Period.ofDays(15)),
											NaftahDuration
													.of(NaftahDuration.DurationDefinition
															.of(4,
																"ساعات",
																0,
																MINUTE,
																0,
																0,
																SECOND,
																0,
																NANOSECOND
															),
														Duration
																.ofHours(4))),
								null),
					Arguments
							.of(true,
								"""
								قيمة_زمنية "بين ١ يناير ٢٠٢٤ بالتقويم الميلادي ١٢:٠٠ صباحاً بتوقيت القاهرة و ٥ رجب ١٤٤٥ بالتقويم الهجري ٣:٣٠ مساءً بتوقيت مكة"
								""",
								createArabicPeriodWithDuration(
																createDateTime( 1,
																				1,
																				ChronologyUtils.DEFAULT_CHRONOLOGY,
																				2024,
																				false,
																				"القاهرة",
																				12,
																				0,
																				null,
																				null,
																				false),
																createDateTime( 5,
																				7,
																				ChronologyUtils.HIJRAH_CHRONOLOGY,
																				1445,
																				false,
																				"مكة",
																				3,
																				30,
																				null,
																				null,
																				true)),
								null),
					Arguments
							.of(true,
								"""
								مقدار_زمني "بين ١٥ مارس ٢٠٢٣ بالتقويم الميلادي ٠٩:٤٥:١٥ بتوقيت دبي و ٢٩ رمضان ١٤٤٤ بالتقويم الهجري ١٢:٠٠:٠٠.٥٠٠ صباحاً بتوقيت الرياض"
								""",
								createArabicPeriodWithDuration(
																createDateTime( 15,
																				3,
																				ChronologyUtils.DEFAULT_CHRONOLOGY,
																				2023,
																				false,
																				"دبي",
																				9,
																				45,
																				15,
																				null,
																				null),
																createDateTime( 29,
																				9,
																				ChronologyUtils.HIJRAH_CHRONOLOGY,
																				1444,
																				false,
																				"الرياض",
																				12,
																				0,
																				0,
																				500000000,
																				false)),
								null),
					Arguments
							.of(true,
								"""
								قيمة_زمنية "بين ٣٠ أكتوبر ٢٠٢٢ بالتقويم الميلادي ٢٢:١٥ بتوقيت تونس و ١ شوال ١٤٤٥ بالتقويم الهجري ١٤:٠٠ بتوقيت الدوحة"
								""",
								createArabicPeriodWithDuration(
																createDateTime( 30,
																				10,
																				ChronologyUtils.DEFAULT_CHRONOLOGY,
																				2022,
																				false,
																				"تونس",
																				22,
																				15,
																				null,
																				null,
																				null),
																createDateTime( 1,
																				10,
																				ChronologyUtils.HIJRAH_CHRONOLOGY,
																				1445,
																				false,
																				"الدوحة",
																				14,
																				0,
																				null,
																				null,
																				null)),
								null),
					Arguments
							.of(true,
								"""
								قيمة_زمنية "ما بين ٢٥ يوليو ٢٠٢١ بالتقويم الميلادي ٠٦:٣٠ مساءً بتوقيت الكويت و ١ يناير ٢٠٢٤ بالتقويم الميلادي ١٢:٠٠ صباحاً +02:00"
								""",
								createArabicPeriodWithDuration(
																createDateTime( 25,
																				7,
																				ChronologyUtils.DEFAULT_CHRONOLOGY,
																				2021,
																				false,
																				"الكويت",
																				6,
																				30,
																				null,
																				null,
																				true),
																createDateTime( 1,
																				1,
																				ChronologyUtils.DEFAULT_CHRONOLOGY,
																				2024,
																				true,
																				"+02:00",
																				12,
																				0,
																				null,
																				null,
																				false)),
								null),
					Arguments
							.of(true,
								"""
								قيمة_زمنية "ما بين ١٣ ذو الحجة ١٤٤٢ بالتقويم الهجري ١١:٤٥:٣٠ بتوقيت أبوظبي و ٧ صفر ١٤٤٣ بالتقويم الهجري ٠٨:٢٠:٤٥ +02:00"
								""",
								createArabicPeriodWithDuration(
																createDateTime( 13,
																				12,
																				ChronologyUtils.HIJRAH_CHRONOLOGY,
																				1442,
																				false,
																				"أبوظبي",
																				11,
																				45,
																				30,
																				null,
																				null),
																createDateTime( 7,
																				2,
																				ChronologyUtils.HIJRAH_CHRONOLOGY,
																				1443,
																				true,
																				"+02:00",
																				8,
																				20,
																				45,
																				null,
																				null)),
								null),
					Arguments
							.of(true,
								"""
								قيمة_زمنية "بين ٤ سبتمبر ٢٠٢٠ بالتقويم الميلادي ٠٧:٠٠:٠٠ بتوقيت عمان و ٤ سبتمبر ٢٠٢٠ بالتقويم الميلادي ٠٧:٠٠:٠٠ +04:00"
								""",
								createArabicPeriodWithDuration(
																createDateTime( 4,
																				9,
																				ChronologyUtils.DEFAULT_CHRONOLOGY,
																				2020,
																				false,
																				"عمان",
																				7,
																				0,
																				0,
																				null,
																				null),
																createDateTime( 4,
																				9,
																				ChronologyUtils.DEFAULT_CHRONOLOGY,
																				2020,
																				true,
																				"+04:00",
																				7,
																				0,
																				0,
																				null,
																				null)),
								null),
					Arguments
							.of(true,
								"""
								قيمة_زمنية "بين ٧ صفر ١٤٤٣ بالتقويم الهجري ٠٨:٢٠:٤٥ بتوقيت بيروت و ٢٥ يوليو ٢٠٢١ بالتقويم الميلادي ٠٦:٣٠ مساءً +03:00"
								""",
								createArabicPeriodWithDuration(
																createDateTime( 7,
																				2,
																				ChronologyUtils.HIJRAH_CHRONOLOGY,
																				1443,
																				false,
																				"بيروت",
																				8,
																				20,
																				45,
																				null,
																				null),
																createDateTime( 25,
																				7,
																				ChronologyUtils.DEFAULT_CHRONOLOGY,
																				2021,
																				true,
																				"+03:00",
																				6,
																				30,
																				null,
																				null,
																				true)),
								null),
					Arguments
							.of(true,
								"""
								مقدار_زمني "بين ٥ رجب ١٤٤٥ بالتقويم الهجري ٣:٣٠ مساءً +03:00 و ١٣ ذو الحجة ١٤٤٢ بالتقويم الهجري ١١:٤٥:٣٠ +04:00"
								""",
								createArabicPeriodWithDuration(
																createDateTime( 5,
																				7,
																				ChronologyUtils.HIJRAH_CHRONOLOGY,
																				1445,
																				true,
																				"+03:00",
																				3,
																				30,
																				null,
																				null,
																				true),

																createDateTime( 13,
																				12,
																				ChronologyUtils.HIJRAH_CHRONOLOGY,
																				1442,
																				true,
																				"+04:00",
																				11,
																				45,
																				30,
																				null,
																				null)),
								null
							),
					Arguments
							.of(true,
								"""
								مقدار_زمني "بين ١٥ مارس ٢٠٢٣ بالتقويم الميلادي ٠٩:٤٥:١٥ +04:00 و ٢٩ رمضان ١٤٤٤ بالتقويم الهجري ١٢:٠٠:٠٠.٥٠٠ صباحاً +03:00"
								""",
								createArabicPeriodWithDuration(
																createDateTime( 15,
																				3,
																				ChronologyUtils.DEFAULT_CHRONOLOGY,
																				2023,
																				true,
																				"+04:00",
																				9,
																				45,
																				15,
																				null,
																				null),
																createDateTime( 29,
																				9,
																				ChronologyUtils.HIJRAH_CHRONOLOGY,
																				1444,
																				true,
																				"+03:00",
																				12,
																				0,
																				0,
																				500000000,
																				false)),
								null
							),
					Arguments
							.of(true,
								"""
								مقدار_زمني "ما بين ٣٠ أكتوبر ٢٠٢٢ بالتقويم الميلادي ٢٢:١٥ +01:00 و ١ شوال ١٤٤٥ بالتقويم الهجري ١٤:٠٠ +03:00"
								""",
								createArabicPeriodWithDuration(
																createDateTime( 30,
																				10,
																				ChronologyUtils.DEFAULT_CHRONOLOGY,
																				2022,
																				true,
																				"+01:00",
																				22,
																				15,
																				null,
																				null,
																				null),

																createDateTime( 1,
																				10,
																				ChronologyUtils.HIJRAH_CHRONOLOGY,
																				1445,
																				true,
																				"+03:00",
																				14,
																				0,
																				null,
																				null,
																				null)),
								null
							),
					Arguments
							.of(true,
								"""
								مقدار_زمني "مدة 3 ساعات" + قيمة_زمنية "مدة 2 ساعات و 10 دقائق و 5 ثوان"
								""",
								NaftahDuration
										.of(NaftahDuration.DurationDefinition
												.of(5,
													HOUR,
													10,
													MINUTE,
													5,
													0,
													SECOND,
													0,
													NANOSECOND
												),
											Duration
													.ofHours(5)
													.plusMinutes(10)
													.plusSeconds(5)),
								null),
					Arguments
							.of(true,
								"""
								مقدار_زمني "مدة 3 ساعات" - قيمة_زمنية "مدة 2 ساعات و 10 دقائق و 5 ثوان"
								""",
								NaftahDuration
										.of(NaftahDuration.DurationDefinition
												.of(0,
													HOUR,
													49,
													MINUTE,
													55,
													0,
													SECOND,
													0,
													NANOSECOND
												),
											Duration
													.ofMinutes(49)
													.plusSeconds(55)),
								null),
					Arguments
							.of(true,
								"""
								مقدار_زمني "مدة 3 ساعات" > قيمة_زمنية "مدة 2 ساعات و 10 دقائق و 5 ثوان"
								""",
								false,
								null),
					Arguments
							.of(true,
								"""
								مقدار_زمني "مدة 3 ساعات" أصغر_من قيمة_زمنية "مدة 2 ساعات و 10 دقائق و 5 ثوان"
								""",
								false,
								null),
					Arguments
							.of(true,
								"""
								مقدار_زمني "مدة 3 ساعات" < قيمة_زمنية "مدة 2 ساعات و 10 دقائق و 5 ثوان"
								""",
								true,
								null),
					Arguments
							.of(true,
								"""
								مقدار_زمني "مدة 3 ساعات" أكبر_من قيمة_زمنية "مدة 2 ساعات و 10 دقائق و 5 ثوان"
								""",
								true,
								null),
					Arguments
							.of(true,
								"""
								مقدار_زمني "مدة 3 ساعات" => قيمة_زمنية "مدة 2 ساعات و 10 دقائق و 5 ثوان"
								""",
								false,
								null),
					Arguments
							.of(true,
								"""
								مقدار_زمني "مدة 3 ساعات" أصغر_أو_يساوي قيمة_زمنية "مدة 2 ساعات و 10 دقائق و 5 ثوان"
								""",
								false,
								null),
					Arguments
							.of(true,
								"""
								مقدار_زمني "مدة 3 ساعات" >= قيمة_زمنية "مدة 2 ساعات و 10 دقائق و 5 ثوان"
								""",
								true,
								null),
					Arguments
							.of(true,
								"""
								مقدار_زمني "مدة 3 ساعات" أكبر_أو_يساوي قيمة_زمنية "مدة 2 ساعات و 10 دقائق و 5 ثوان"
								""",
								true,
								null),
					Arguments
							.of(true,
								"""
								مقدار_زمني "مدة 3 ساعات" =! قيمة_زمنية "مدة 2 ساعات و 10 دقائق و 5 ثوان"
								""",
								true,
								null),
					Arguments
							.of(true,
								"""
								مقدار_زمني "مدة 3 ساعات" لا_يساوي قيمة_زمنية "مدة 2 ساعات و 10 دقائق و 5 ثوان"
								""",
								true,
								null),
					Arguments
							.of(true,
								"""
								مقدار_زمني "مدة 3 ساعات" == قيمة_زمنية "مدة 2 ساعات و 10 دقائق و 5 ثوان"
								""",
								false,
								null),
					Arguments
							.of(true,
								"""
								مقدار_زمني "مدة 3 ساعات" يساوي قيمة_زمنية "مدة 2 ساعات و 10 دقائق و 5 ثوان"
								""",
								false,
								null),
					Arguments
							.of(true,
								"""
								مقدار_زمني "فترة 5 سنوات" + قيمة_زمنية "فترة 1 سنة و 3 أشهر و 10 أيام"
								""",
								NaftahPeriod
										.of(NaftahPeriod.PeriodDefinition
												.of(6,
													YEAR,
													3,
													MONTH,
													10,
													DAY
												),
											Period.of(6, 3, 10)),
								null),
					Arguments
							.of(true,
								"""
								مقدار_زمني "فترة 5 سنوات" - قيمة_زمنية "فترة 1 سنة و 3 أشهر و 10 أيام"
								""",
								NaftahPeriod
										.of(NaftahPeriod.PeriodDefinition
												.of(4,
													YEAR,
													-3,
													MONTH,
													-10,
													DAY
												),
											Period.of(4, -3, -10)),
								null),
					Arguments
							.of(true,
								"""
								مقدار_زمني "فترة 5 سنوات" أصغر_من قيمة_زمنية "فترة 1 سنة و 3 أشهر و 10 أيام"
								""",
								false,
								null),
					Arguments
							.of(true,
								"""
								مقدار_زمني "فترة 5 سنوات" أكبر_من قيمة_زمنية "فترة 1 سنة و 3 أشهر و 10 أيام"
								""",
								true,
								null),
					Arguments
							.of(true,
								"""
								مقدار_زمني "فترة 5 سنوات" أصغر_أو_يساوي قيمة_زمنية "فترة 1 سنة و 3 أشهر و 10 أيام"
								""",
								false,
								null),
					Arguments
							.of(true,
								"""
								مقدار_زمني "فترة 5 سنوات" أكبر_أو_يساوي قيمة_زمنية "فترة 1 سنة و 3 أشهر و 10 أيام"
								""",
								true,
								null),
					Arguments
							.of(true,
								"""
								مقدار_زمني "فترة 5 سنوات" لا_يساوي قيمة_زمنية "فترة 1 سنة و 3 أشهر و 10 أيام"
								""",
								true,
								null),
					Arguments
							.of(true,
								"""
								مقدار_زمني "فترة 5 سنوات" يساوي قيمة_زمنية "فترة 1 سنة و 3 أشهر و 10 أيام"
								""",
								false,
								null),
					Arguments
							.of(true,
								"""
								مقدار_زمني "فترة 5 سنوات و 3 ساعات" + قيمة_زمنية "فترة 1 سنة و 3 أشهر و 10 أيام"
								""",
								NaftahPeriodWithDuration
										.of(
											NaftahPeriod
													.of(NaftahPeriod.PeriodDefinition
															.of(6,
																YEAR,
																3,
																MONTH,
																10,
																DAY
															),
														Period.of(6, 3, 10)),
											NaftahDuration
													.of(NaftahDuration.DurationDefinition
															.of(3,
																"ساعات",
																0,
																MINUTE,
																0,
																0,
																SECOND,
																0,
																NANOSECOND
															),
														Duration
																.ofHours(3))),
								null),
					Arguments
							.of(true,
								"""
								مقدار_زمني "فترة 5 سنوات و 3 ساعات" - قيمة_زمنية "فترة 1 سنة و 3 أشهر و 10 أيام"
								""",
								NaftahPeriodWithDuration
										.of(
											NaftahPeriod
													.of(NaftahPeriod.PeriodDefinition
															.of(4,
																YEAR,
																-3,
																MONTH,
																-10,
																DAY
															),
														Period.of(4, -3, -10)),
											NaftahDuration
													.of(NaftahDuration.DurationDefinition
															.of(3,
																"ساعات",
																0,
																MINUTE,
																0,
																0,
																SECOND,
																0,
																NANOSECOND
															),
														Duration
																.ofHours(3))),
								null),
					Arguments
							.of(true,
								"""
								مقدار_زمني "فترة 5 سنوات و 3 ساعات" لا_يساوي قيمة_زمنية "فترة 1 سنة و 3 أشهر و 10 أيام"
								""",
								true,
								null),
					Arguments
							.of(true,
								"""
								مقدار_زمني "فترة 5 سنوات و 3 ساعات" يساوي قيمة_زمنية "فترة 1 سنة و 3 أشهر و 10 أيام"
								""",
								false,
								null),
					Arguments
							.of(false,
								"""
								مقدار_زمني "فترة 5 سنوات و 3 ساعات" أصغر_من قيمة_زمنية "فترة 1 سنة و 3 أشهر و 10 أيام"
								""",
								null,
								BinaryOperation
										.newNaftahBugError( BinaryOperation.LESS_THAN,
															NaftahPeriodWithDuration.ofZero(),
															NaftahPeriod.ofZero())),
					Arguments
							.of(false,
								"""
								مقدار_زمني "مدة 3 ساعات" أكبر_من قيمة_زمنية "فترة 1 سنة و 3 أشهر و 10 أيام"
								""",
								null,
								BinaryOperation
										.newNaftahBugError( BinaryOperation.GREATER_THAN,
															NaftahDuration.ofZero(),
															NaftahPeriod.ofZero())),
					Arguments
							.of(false,
								"""
								مقدار_زمني "فترة 5 سنوات و 3 ساعات" أصغر_أو_يساوي قيمة_زمنية "مدة 3 ساعات"
								""",
								null,
								BinaryOperation
										.newNaftahBugError( BinaryOperation.LESS_THAN_EQUALS,
															NaftahPeriodWithDuration.ofZero(),
															NaftahDuration.ofZero())),
					Arguments
							.of(true,
								"""
								أنشئ_مدة_صفرية()""",
								NaftahDuration.ofZero(),
								null),
					Arguments
							.of(true,
								"""
								أنشئ_مدة_من_أيام(10)""",
								NaftahDuration.ofDays(10),
								null),
					Arguments
							.of(true,
								"""
								أنشئ_مدة_من_ساعات(25)""",
								NaftahDuration.ofHours(25),
								null),
					Arguments
							.of(true,
								"""
								أنشئ_مدة_من_دقائق(30)""",
								NaftahDuration.ofMinutes(30),
								null),
					Arguments
							.of(true,
								"""
								أنشئ_مدة_من_ثواني(45)""",
								NaftahDuration.ofSeconds(45),
								null),
					Arguments
							.of(true,
								"""
								أنشئ_مدة_من_ثواني_مع_نانو(40, 15)""",
								NaftahDuration.ofSeconds(40, 15),
								null),
					Arguments
							.of(true,
								"""
								أنشئ_مدة_من_مللي(455555)""",
								NaftahDuration.ofMillis(455555),
								null),
					Arguments
							.of(true,
								"""
								أنشئ_مدة_من_نانو(100000000000000000)""",
								NaftahDuration.ofNanos(100_000_000_000_000_000L),
								null),
					Arguments
							.of(true,
								"""
								أنشئ_فترة_صفرية()""",
								NaftahPeriod.ofZero(),
								null),
					Arguments
							.of(true,
								"""
								أنشئ_فترة_من_سنوات(10)""",
								NaftahPeriod.ofYears(10),
								null),
					Arguments
							.of(true,
								"""
								أنشئ_فترة_من_شهور(120)""",
								NaftahPeriod.ofMonths(120),
								null),
					Arguments
							.of(true,
								"""
								أنشئ_فترة_من_أسابيع(840)""",
								NaftahPeriod.ofWeeks(840),
								null),
					Arguments
							.of(true,
								"""
								أنشئ_فترة_من_أيام(5880)""",
								NaftahPeriod.ofDays(5880),
								null),
					Arguments
							.of(true,
								"""
								أنشئ_فترة_و_مدة_صفرية()""",
								NaftahPeriodWithDuration.ofZero(),
								null),
					Arguments
							.of(true,
								"""
								أنشئ_فترة_و_مدة_من_سنوات(10)""",
								NaftahPeriodWithDuration.ofYears(10),
								null),
					Arguments
							.of(true,
								"""
								أنشئ_فترة_و_مدة_من_شهور(120)""",
								NaftahPeriodWithDuration.ofMonths(120),
								null),
					Arguments
							.of(true,
								"""
								أنشئ_فترة_و_مدة_من_أسابيع(840)""",
								NaftahPeriodWithDuration.ofWeeks(840),
								null),
					Arguments
							.of(true,
								"""
								أنشئ_فترة_و_مدة_من_أيام(5880)""",
								NaftahPeriodWithDuration.ofDays(5880),
								null),
					Arguments
							.of(true,
								"""
								أنشئ_فترة_و_مدة_من_ساعات(141120)""",
								NaftahPeriodWithDuration.ofHours(141120),
								null),
					Arguments
							.of(true,
								"""
								أنشئ_فترة_و_مدة_من_دقائق(8467200)""",
								NaftahPeriodWithDuration.ofMinutes(8467200),
								null),
					Arguments
							.of(true,
								"""
								أنشئ_فترة_و_مدة_من_ثواني(508032000)""",
								NaftahPeriodWithDuration.ofSeconds(508032000),
								null),
					Arguments
							.of(true,
								"""
								أنشئ_فترة_و_مدة_من_ثواني_مع_نانو(508032000, 1000000000000000)""",
								NaftahPeriodWithDuration.ofSeconds(508032000, 1000_000_000_000_000L),
								null),
					Arguments
							.of(true,
								"""
								أنشئ_فترة_و_مدة_من_مللي(508032000000)""",
								NaftahPeriodWithDuration.ofMillis(508_032_000_000L),
								null),
					Arguments
							.of(true,
								"""
								أنشئ_فترة_و_مدة_من_نانو(508032000000000)""",
								NaftahPeriodWithDuration.ofNanos(508_032_000_000_000L),
								null),
					Arguments
							.of(true,
								"""
								أضف_سنوات_لفترة(أنشئ_فترة_صفرية(), 2)
								""",
								NaftahPeriod.ofYears(2),
								null),
					Arguments
							.of(true,
								"""
								أضف_أشهر_لفترة(أنشئ_فترة_صفرية(), 24)""",
								NaftahPeriod.ofMonths(24),
								null),
					Arguments
							.of(true,
								"""
								أضف_أيام_لفترة(أنشئ_فترة_صفرية(), 10)""",
								NaftahPeriod.ofDays(10),
								null),
					Arguments
							.of(true,
								"""
								اطرح_سنوات_من_الفترة(أنشئ_فترة_صفرية(), 2)
								""",
								NaftahPeriod.ofYears(-2),
								null),
					Arguments
							.of(true,
								"""
								اطرح_أشهر_من_الفترة(أنشئ_فترة_صفرية(), 24)""",
								NaftahPeriod.ofMonths(-24),
								null),
					Arguments
							.of(true,
								"""
								أضف_أيام_للمدة(أنشئ_مدة_صفرية(), 10)""",
								NaftahDuration.ofHours(240),
								null),
					Arguments
							.of(true,
								"""
								أضف_ساعات_للمدة(أنشئ_مدة_صفرية(), 10)""",
								NaftahDuration.ofHours(10),
								null),
					Arguments
							.of(true,
								"""
								أضف_دقائق_للمدة(أنشئ_مدة_صفرية(), 600)""",
								NaftahDuration.ofHours(10),
								null),
					Arguments
							.of(true,
								"""
								أضف_ثواني_للمدة(أنشئ_مدة_صفرية(), 36000)""",
								NaftahDuration.ofHours(10),
								null),
					Arguments
							.of(true,
								"""
								أضف_مللي_للمدة(أنشئ_مدة_صفرية(), 36000000)""",
								NaftahDuration.ofHours(10),
								null),
					Arguments
							.of(true,
								"""
								أضف_نانو_للمدة(أنشئ_مدة_صفرية(), 36000000000000)""",
								NaftahDuration.ofHours(10),
								null),

					Arguments
							.of(true,
								"""
								اطرح_أيام_من_المدة(أنشئ_مدة_صفرية(), 10)""",
								NaftahDuration.ofHours(-240),
								null),
					Arguments
							.of(true,
								"""
								اطرح_ساعات_من_المدة(أنشئ_مدة_صفرية(), 10)""",
								NaftahDuration.ofHours(-10),
								null),
					Arguments
							.of(true,
								"""
								اطرح_دقائق_من_المدة(أنشئ_مدة_صفرية(), 600)""",
								NaftahDuration.ofHours(-10),
								null),
					Arguments
							.of(true,
								"""
								اطرح_ثواني_من_المدة(أنشئ_مدة_صفرية(), 36000)""",
								NaftahDuration.ofHours(-10),
								null),
					Arguments
							.of(true,
								"""
								اطرح_مللي_من_المدة(أنشئ_مدة_صفرية(), 36000000)""",
								NaftahDuration.ofHours(-10),
								null),
					Arguments
							.of(true,
								"""
								اطرح_نانو_من_المدة(أنشئ_مدة_صفرية(), 36000000000000)""",
								NaftahDuration.ofHours(-10),
								null),
					Arguments
							.of(true,
								"""
								أضف_سنوات_لفترة_مع_مدة(أنشئ_فترة_و_مدة_صفرية(), 1)""",
								NaftahPeriodWithDuration.ofYears(1),
								null),
					Arguments
							.of(true,
								"""
								أضف_أشهر_لفترة_مع_مدة(أنشئ_فترة_و_مدة_صفرية(), 12)""",
								NaftahPeriodWithDuration.ofMonths(12),
								null),
					Arguments
							.of(true,
								"""
								أضف_أيام_لفترة_مع_مدة(أنشئ_فترة_و_مدة_صفرية(), 120)""",
								NaftahPeriodWithDuration.ofDays(120),
								null),
					Arguments
							.of(true,
								"""
								أضف_ساعات_لفترة_مع_مدة(أنشئ_فترة_و_مدة_صفرية(), 120)""",
								NaftahPeriodWithDuration.ofDays(5),
								null),
					Arguments
							.of(true,
								"""
								أضف_دقائق_لفترة_مع_مدة(أنشئ_فترة_و_مدة_صفرية(), 7200)""",
								NaftahPeriodWithDuration.ofDays(5),
								null),
					Arguments
							.of(true,
								"""
								أضف_ثواني_لفترة_مع_مدة(أنشئ_فترة_و_مدة_صفرية(), 432000)""",
								NaftahPeriodWithDuration.ofDays(5),
								null),
					Arguments
							.of(true,
								"""
								أضف_مللي_لفترة_مع_مدة(أنشئ_فترة_و_مدة_صفرية(), 432000000)""",
								NaftahPeriodWithDuration.ofDays(5),
								null),
					Arguments
							.of(true,
								"""
								أضف_نانو_لفترة_مع_مدة(أنشئ_فترة_و_مدة_صفرية(), 432000000000000)""",
								NaftahPeriodWithDuration.ofDays(5),
								null),
					Arguments
							.of(true,
								"""
								اطرح_سنوات_من_الفترة_مع_مدة(أنشئ_فترة_و_مدة_صفرية(), 1)""",
								NaftahPeriodWithDuration.ofYears(-1),
								null),
					Arguments
							.of(true,
								"""
								اطرح_أشهر_من_الفترة_مع_مدة(أنشئ_فترة_و_مدة_صفرية(), 12)""",
								NaftahPeriodWithDuration.ofMonths(-12),
								null),
					Arguments
							.of(true,
								"""
								اطرح_أيام_من_الفترة_مع_مدة(أنشئ_فترة_و_مدة_صفرية(), 120)""",
								NaftahPeriodWithDuration.ofDays(-120),
								null),
					Arguments
							.of(true,
								"""
								اطرح_ساعات_من_الفترة_مع_مدة(أنشئ_فترة_و_مدة_صفرية(), 120)""",
								NaftahPeriodWithDuration.ofHours(-120),
								null),
					Arguments
							.of(true,
								"""
								اطرح_دقائق_من_الفترة_مع_مدة(أنشئ_فترة_و_مدة_صفرية(), 7200)""",
								NaftahPeriodWithDuration.ofHours(-120),
								null),
					Arguments
							.of(true,
								"""
								اطرح_ثواني_من_الفترة_مع_مدة(أنشئ_فترة_و_مدة_صفرية(), 432000)""",
								NaftahPeriodWithDuration.ofHours(-120),
								null),
					Arguments
							.of(true,
								"""
								اطرح_مللي_من_الفترة_مع_مدة(أنشئ_فترة_و_مدة_صفرية(), 432000000)""",
								NaftahPeriodWithDuration.ofHours(-120),
								null),
					Arguments
							.of(true,
								"""
								اطرح_نانو_من_الفترة_مع_مدة(أنشئ_فترة_و_مدة_صفرية(), 432000000000000)""",
								NaftahPeriodWithDuration.ofHours(-120),
								null),
					Arguments
							.of(true,
								"""
								احصل_على_سنوات_الفترة(أنشئ_فترة_صفرية())""",
								0,
								null),
					Arguments
							.of(true,
								"""
								احصل_على_أشهر_الفترة(أنشئ_فترة_صفرية())""",
								0,
								null),
					Arguments
							.of(true,
								"""
								احصل_على_أيام_الفترة(أنشئ_فترة_صفرية())""",
								0,
								null),
					Arguments
							.of(true,
								"""
								احصل_على_ساعات_المدة(أنشئ_مدة_صفرية())""",
								0,
								null),
					Arguments
							.of(true,
								"""
								احصل_على_دقائق_المدة(أنشئ_مدة_صفرية())""",
								0,
								null),
					Arguments
							.of(true,
								"""
								احصل_على_ثواني_المدة(أنشئ_مدة_صفرية())""",
								0,
								null),
					Arguments
							.of(true,
								"""
								احصل_على_مللي_المدة(أنشئ_مدة_صفرية())""",
								0,
								null),
					Arguments
							.of(true,
								"""
								احصل_على_نانو_المدة(أنشئ_مدة_صفرية())""",
								0,
								null),
					Arguments
							.of(true,
								"""
								احصل_على_سنوات_الفترة_مع_مدة(أنشئ_فترة_و_مدة_صفرية())""",
								0,
								null),
					Arguments
							.of(true,
								"""
								احصل_على_أشهر_الفترة_مع_مدة(أنشئ_فترة_و_مدة_صفرية())""",
								0,
								null),
					Arguments
							.of(true,
								"""
								احصل_على_أيام_الفترة_مع_مدة(أنشئ_فترة_و_مدة_صفرية())""",
								0,
								null),
					Arguments
							.of(true,
								"""
								احصل_على_ساعات_الفترة_مع_مدة(أنشئ_فترة_و_مدة_صفرية())""",
								0,
								null),
					Arguments
							.of(true,
								"""
								احصل_على_دقائق_الفترة_مع_مدة(أنشئ_فترة_و_مدة_صفرية())""",
								0,
								null),
					Arguments
							.of(true,
								"""
								احصل_على_ثواني_الفترة_مع_مدة(أنشئ_فترة_و_مدة_صفرية())""",
								0,
								null),
					Arguments
							.of(true,
								"""
								احصل_على_مللي_الفترة_مع_مدة(أنشئ_فترة_و_مدة_صفرية())""",
								0,
								null),
					Arguments
							.of(true,
								"""
								احصل_على_نانو_الفترة_مع_مدة(أنشئ_فترة_و_مدة_صفرية())""",
								0,
								null),
					Arguments
							.of(true,
								"""
								أنشئ_مدة_بين_نقطتين(زمن "30 أكتوبر ٢٠٢٢ بالتقويم الميلادي ١٢:٠٠ بتوقيت تونس",زمن "30 أكتوبر ٢٠٢٢ بالتقويم الميلادي ٢٢:٠٠ بتوقيت تونس")
								""",
								NaftahDuration.ofHours(10),
								null),
					Arguments
							.of(true,
								"""
								أنشئ_مدة_بين_نقطتين(زمن "1 شوال ١٤٤٥ بالتقويم الهجري ٠٠:٠٠ بتوقيت الدوحة",زمن "11 شوال ١٤٤٥ بالتقويم الهجري ٠٠:٠٠ بتوقيت الدوحة")""",
								NaftahPeriod.ofDays(10),
								null),
					Arguments
							.of(true,
								"""
								أنشئ_مدة_بين_نقطتين(زمن "25 يوليو ٢٠٢١ بالتقويم الميلادي ٠٨:٣٠ بتوقيت الكويت",زمن "4 أغسطس ٢٠٢١ بالتقويم الميلادي ١٨:٣٠ بتوقيت الكويت")""",
								NaftahPeriodWithDuration.ofDays(10).plusHours(10),
								null)
				);
	}
}
