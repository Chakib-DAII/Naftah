// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

package org.daiitech.naftah.parser.provider.script;

import java.time.ZoneId;
import java.time.chrono.Chronology;
import java.util.Objects;
import java.util.stream.Stream;

import org.daiitech.naftah.builtin.time.NaftahDate;
import org.daiitech.naftah.builtin.time.NaftahDateTime;
import org.daiitech.naftah.builtin.time.NaftahDuration;
import org.daiitech.naftah.builtin.time.NaftahPeriod;
import org.daiitech.naftah.builtin.time.NaftahTime;
import org.daiitech.naftah.builtin.utils.op.BinaryOperation;
import org.daiitech.naftah.utils.time.ChronologyUtils;
import org.daiitech.naftah.utils.time.TemporalUtils;
import org.daiitech.naftah.utils.time.ZoneUtils;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import static org.daiitech.naftah.utils.time.Constants.APRIL_LATIN;
import static org.daiitech.naftah.utils.time.Constants.DECEMBER_LATIN;
import static org.daiitech.naftah.utils.time.Constants.DEFAULT_CALENDAR_NAME_1;
import static org.daiitech.naftah.utils.time.Constants.DHU_AL_HIJJAH;
import static org.daiitech.naftah.utils.time.Constants.HIJRI_CALENDAR_NAME_1;
import static org.daiitech.naftah.utils.time.Constants.JANUARY_AR;
import static org.daiitech.naftah.utils.time.Constants.JANUARY_LATIN;
import static org.daiitech.naftah.utils.time.Constants.JULY_AR;
import static org.daiitech.naftah.utils.time.Constants.JUMADA_AL_AWWAL;
import static org.daiitech.naftah.utils.time.Constants.JUNE_LATIN;
import static org.daiitech.naftah.utils.time.Constants.MARCH;
import static org.daiitech.naftah.utils.time.Constants.MAY_LATIN;
import static org.daiitech.naftah.utils.time.Constants.OCTOBER;
import static org.daiitech.naftah.utils.time.Constants.RAJAB;
import static org.daiitech.naftah.utils.time.Constants.RAMADAN;
import static org.daiitech.naftah.utils.time.Constants.SAFAR;
import static org.daiitech.naftah.utils.time.Constants.SEPTEMBER;
import static org.daiitech.naftah.utils.time.Constants.SHAWAL;
import static org.daiitech.naftah.utils.time.ZoneUtils.parseZoneOffset;

public class DateTimeProvider implements ArgumentsProvider {
	private NaftahDateTime createArabicDateTime(int day,
												String arabicMonth,
												int monthValue,
												Chronology chronology,
												String chronologyName,
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

		NaftahTime.ZoneOrOffset zoneOrOffset;
		if (Boolean.TRUE.equals(offset)) {
			zoneOrOffset = NaftahTime.ZoneOrOffset.ofOffset(arabicZoneOrOffset);
		}
		else {
			zoneOrOffset = NaftahTime.ZoneOrOffset.ofZone(arabicZoneOrOffset);
		}

		return NaftahDateTime
				.of(
					NaftahDate
							.of(
								NaftahDate.Date.of(day, arabicMonth, chronology, year),
								NaftahDate.Calendar.of(chronologyName, chronology),
								TemporalUtils.createDate(day, monthValue, year, chronology)
							),
					NaftahTime
							.of(
								NaftahTime.Time.of(hour, minute, second, nano, isPM),
								zoneOrOffset,
								TemporalUtils
										.createTime(
													TemporalUtils.getHour24(hour, isPM),
													minute,
													second,
													nano,
													zoneId)

							),
					TemporalUtils
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
							)
				);
	}

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
		return Stream
				.of(
					Arguments
							.of(true,
								"""
								زمن "١ يناير ٢٠٢٤ بالتقويم الميلادي ١٢:٠٠ صباحاً بتوقيت القاهرة"
								""",
								createArabicDateTime(   1,
														JANUARY_AR,
														1,
														ChronologyUtils.DEFAULT_CHRONOLOGY,
														DEFAULT_CALENDAR_NAME_1,
														2024,
														false,
														"القاهرة",
														12,
														0,
														null,
														null,
														false),
								null),
					Arguments
							.of(true,
								"""
								زمن "٥ رجب ١٤٤٥ بالتقويم الهجري ٣:٣٠ مساءً بتوقيت مكة"
								""",
								createArabicDateTime(   5,
														RAJAB,
														7,
														ChronologyUtils.HIJRAH_CHRONOLOGY,
														HIJRI_CALENDAR_NAME_1,
														1445,
														false,
														"مكة",
														3,
														30,
														null,
														null,
														true),
								null),
					Arguments
							.of(true,
								"""
								زمن "١٥ مارس ٢٠٢٣ بالتقويم الميلادي ٠٩:٤٥:١٥ بتوقيت دبي"
								""",
								createArabicDateTime(   15,
														MARCH,
														3,
														ChronologyUtils.DEFAULT_CHRONOLOGY,
														DEFAULT_CALENDAR_NAME_1,
														2023,
														false,
														"دبي",
														9,
														45,
														15,
														null,
														null),
								null),
					Arguments
							.of(true,
								"""
								زمن "٢٩ رمضان ١٤٤٤ بالتقويم الهجري ١٢:٠٠:٠٠.٥٠٠ صباحاً بتوقيت الرياض"
								""",
								createArabicDateTime(   29,
														RAMADAN,
														9,
														ChronologyUtils.HIJRAH_CHRONOLOGY,
														HIJRI_CALENDAR_NAME_1,
														1444,
														false,
														"الرياض",
														12,
														0,
														0,
														500000000,
														false),
								null),
					Arguments
							.of(true,
								"""
								زمن "٣٠ أكتوبر ٢٠٢٢ بالتقويم الميلادي ٢٢:١٥ بتوقيت تونس"
								""",
								createArabicDateTime(   30,
														OCTOBER,
														10,
														ChronologyUtils.DEFAULT_CHRONOLOGY,
														DEFAULT_CALENDAR_NAME_1,
														2022,
														false,
														"تونس",
														22,
														15,
														null,
														null,
														null),
								null),
					Arguments
							.of(true,
								"""
								نقطة_زمنية "١ شوال ١٤٤٥ بالتقويم الهجري ١٤:٠٠ بتوقيت الدوحة"
								""",
								createArabicDateTime(   1,
														SHAWAL,
														10,
														ChronologyUtils.HIJRAH_CHRONOLOGY,
														HIJRI_CALENDAR_NAME_1,
														1445,
														false,
														"الدوحة",
														14,
														0,
														null,
														null,
														null),
								null),
					Arguments
							.of(true,
								"""
								نقطة_زمنية "٢٥ يوليو ٢٠٢١ بالتقويم الميلادي ٠٦:٣٠ مساءً بتوقيت الكويت"
								""",
								createArabicDateTime(   25,
														JULY_AR,
														7,
														ChronologyUtils.DEFAULT_CHRONOLOGY,
														DEFAULT_CALENDAR_NAME_1,
														2021,
														false,
														"الكويت",
														6,
														30,
														null,
														null,
														true),
								null),
					Arguments
							.of(true,
								"""
								نقطة_زمنية "١٣ ذو الحجة ١٤٤٢ بالتقويم الهجري ١١:٤٥:٣٠ بتوقيت أبوظبي"
								""",
								createArabicDateTime(   13,
														DHU_AL_HIJJAH,
														12,
														ChronologyUtils.HIJRAH_CHRONOLOGY,
														HIJRI_CALENDAR_NAME_1,
														1442,
														false,
														"أبوظبي",
														11,
														45,
														30,
														null,
														null),
								null),
					Arguments
							.of(true,
								"""
								نقطة_زمنية "٤ سبتمبر ٢٠٢٠ بالتقويم الميلادي ٠٧:٠٠:٠٠ بتوقيت عمان"
								""",
								createArabicDateTime(   4,
														SEPTEMBER,
														9,
														ChronologyUtils.DEFAULT_CHRONOLOGY,
														DEFAULT_CALENDAR_NAME_1,
														2020,
														false,
														"عمان",
														7,
														0,
														0,
														null,
														null),
								null),
					Arguments
							.of(true,
								"""
								نقطة_زمنية "٧ صفر ١٤٤٣ بالتقويم الهجري ٠٨:٢٠:٤٥ بتوقيت بيروت"
								""",
								createArabicDateTime(   7,
														SAFAR,
														2,
														ChronologyUtils.HIJRAH_CHRONOLOGY,
														HIJRI_CALENDAR_NAME_1,
														1443,
														false,
														"بيروت",
														8,
														20,
														45,
														null,
														null),
								null),
					Arguments
							.of(true,
								"""
								زمن "١ يناير ٢٠٢٤ بالتقويم الميلادي ١٢:٠٠ صباحاً +02:00"
								""",
								createArabicDateTime(   1,
														JANUARY_AR,
														1,
														ChronologyUtils.DEFAULT_CHRONOLOGY,
														DEFAULT_CALENDAR_NAME_1,
														2024,
														true,
														"+02:00",
														12,
														0,
														null,
														null,
														false),
								null
							),
					Arguments
							.of(true,
								"""
								زمن "٥ رجب ١٤٤٥ بالتقويم الهجري ٣:٣٠ مساءً +03:00"
								""",
								createArabicDateTime(   5,
														RAJAB,
														7,
														ChronologyUtils.HIJRAH_CHRONOLOGY,
														HIJRI_CALENDAR_NAME_1,
														1445,
														true,
														"+03:00",
														3,
														30,
														null,
														null,
														true),
								null
							),
					Arguments
							.of(true,
								"""
								زمن "١٥ مارس ٢٠٢٣ بالتقويم الميلادي ٠٩:٤٥:١٥ +04:00"
								""",
								createArabicDateTime(   15,
														MARCH,
														3,
														ChronologyUtils.DEFAULT_CHRONOLOGY,
														DEFAULT_CALENDAR_NAME_1,
														2023,
														true,
														"+04:00",
														9,
														45,
														15,
														null,
														null),
								null
							),
					Arguments
							.of(true,
								"""
								زمن "٢٩ رمضان ١٤٤٤ بالتقويم الهجري ١٢:٠٠:٠٠.٥٠٠ صباحاً +03:00"
								""",
								createArabicDateTime(   29,
														RAMADAN,
														9,
														ChronologyUtils.HIJRAH_CHRONOLOGY,
														HIJRI_CALENDAR_NAME_1,
														1444,
														true,
														"+03:00",
														12,
														0,
														0,
														500000000,
														false),
								null
							),
					Arguments
							.of(true,
								"""
								زمن "٣٠ أكتوبر ٢٠٢٢ بالتقويم الميلادي ٢٢:١٥ +01:00"
								""",
								createArabicDateTime(   30,
														OCTOBER,
														10,
														ChronologyUtils.DEFAULT_CHRONOLOGY,
														DEFAULT_CALENDAR_NAME_1,
														2022,
														true,
														"+01:00",
														22,
														15,
														null,
														null,
														null),
								null
							),
					Arguments
							.of(true,
								"""
								نقطة_زمنية "١ شوال ١٤٤٥ بالتقويم الهجري ١٤:٠٠ +03:00"
								""",
								createArabicDateTime(   1,
														SHAWAL,
														10,
														ChronologyUtils.HIJRAH_CHRONOLOGY,
														HIJRI_CALENDAR_NAME_1,
														1445,
														true,
														"+03:00",
														14,
														0,
														null,
														null,
														null),
								null
							),
					Arguments
							.of(true,
								"""
								نقطة_زمنية "٢٥ يوليو ٢٠٢١ بالتقويم الميلادي ٠٦:٣٠ مساءً +03:00"
								""",
								createArabicDateTime(   25,
														JULY_AR,
														7,
														ChronologyUtils.DEFAULT_CHRONOLOGY,
														DEFAULT_CALENDAR_NAME_1,
														2021,
														true,
														"+03:00",
														6,
														30,
														null,
														null,
														true),
								null
							),
					Arguments
							.of(true,
								"""
								نقطة_زمنية "١٣ ذو الحجة ١٤٤٢ بالتقويم الهجري ١١:٤٥:٣٠ +04:00"
								""",
								createArabicDateTime(   13,
														DHU_AL_HIJJAH,
														12,
														ChronologyUtils.HIJRAH_CHRONOLOGY,
														HIJRI_CALENDAR_NAME_1,
														1442,
														true,
														"+04:00",
														11,
														45,
														30,
														null,
														null),
								null
							),
					Arguments
							.of(true,
								"""
								نقطة_زمنية "٤ سبتمبر ٢٠٢٠ بالتقويم الميلادي ٠٧:٠٠:٠٠ +04:00"
								""",
								createArabicDateTime(   4,
														SEPTEMBER,
														9,
														ChronologyUtils.DEFAULT_CHRONOLOGY,
														DEFAULT_CALENDAR_NAME_1,
														2020,
														true,
														"+04:00",
														7,
														0,
														0,
														null,
														null),
								null
							),
					Arguments
							.of(true,
								"""
								نقطة_زمنية "٧ صفر ١٤٤٣ بالتقويم الهجري ٠٨:٢٠:٤٥ +02:00"
								""",
								createArabicDateTime(   7,
														SAFAR,
														2,
														ChronologyUtils.HIJRAH_CHRONOLOGY,
														HIJRI_CALENDAR_NAME_1,
														1443,
														true,
														"+02:00",
														8,
														20,
														45,
														null,
														null),
								null
							),
					Arguments
							.of(true,
								"""
								نوع (نقطة_زمنية "الآن بالتقويم الهجري +02:00") يساوي "'تاريخ_و_وقت'"
								""",
								true,
								null
							),
					Arguments
							.of(true,
								"""
								نوع (نقطة_زمنية "تاريخ الآن بالتقويم الهجري +02:00") يساوي "'تاريخ'"
								""",
								true,
								null
							),
					Arguments
							.of(true,
								"""
								نوع (نقطة_زمنية "وقت الآن +02:00") يساوي "'وقت'"
								""",
								true,
								null
							),
					Arguments
							.of(true,
								"""
								نقطة_زمنية "٧ صفر ١٤٤٣ بالتقويم الهجري ٠٨:٢٠:٤٥ +02:00" + قيمة_زمنية "فترة 1 سنة و 3 أشهر و 10 أيام و 2 ساعات و 10 دقائق و 5 ثوان"
								""",
								createArabicDateTime(   30,
														JUMADA_AL_AWWAL,
														5,
														ChronologyUtils.HIJRAH_CHRONOLOGY,
														HIJRI_CALENDAR_NAME_1,
														1444,
														true,
														"+02:00",
														10,
														30,
														50,
														0,
														null),
								null),
					Arguments
							.of(true,
								"""
								نقطة_زمنية "٧ صفر ١٤٤٣ بالتقويم الهجري ٠٨:٢٠:٤٥ +02:00" + قيمة_زمنية "مدة 2 ساعات و 10 دقائق و 5 ثوان"
								""",
								createArabicDateTime(   7,
														SAFAR,
														2,
														ChronologyUtils.HIJRAH_CHRONOLOGY,
														HIJRI_CALENDAR_NAME_1,
														1443,
														true,
														"+02:00",
														10,
														30,
														50,
														0,
														null),
								null),
					Arguments
							.of(true,
								"""
								نقطة_زمنية "٧ صفر ١٤٤٣ بالتقويم الهجري ٠٨:٢٠:٤٥ +02:00" - قيمة_زمنية "مدة 2 ساعات و 10 دقائق و 5 ثوان"
								""",
								createArabicDateTime(   7,
														SAFAR,
														2,
														ChronologyUtils.HIJRAH_CHRONOLOGY,
														HIJRI_CALENDAR_NAME_1,
														1443,
														true,
														"+02:00",
														6,
														10,
														40,
														0,
														null),
								null),
					Arguments
							.of(true,
								"""
								نقطة_زمنية "٧ صفر ١٤٤٣ بالتقويم الهجري ٠٨:٢٠:٤٥ +02:00" > نقطة_زمنية "الآن بالتقويم الهجري +02:00"
								""",
								true,
								null),
					Arguments
							.of(true,
								"""
								نقطة_زمنية "٧ صفر ١٤٤٣ بالتقويم الهجري ٠٨:٢٠:٤٥ +02:00" أصغر_من نقطة_زمنية "الآن بالتقويم الهجري +02:00"
								""",
								true,
								null),
					Arguments
							.of(true,
								"""
								نقطة_زمنية "٧ صفر ١٤٤٣ بالتقويم الهجري ٠٨:٢٠:٤٥ +02:00" < نقطة_زمنية "الآن بالتقويم الهجري +02:00"
								""",
								false,
								null),
					Arguments
							.of(true,
								"""
								نقطة_زمنية "٧ صفر ١٤٤٣ بالتقويم الهجري ٠٨:٢٠:٤٥ +02:00" أكبر_من نقطة_زمنية "الآن بالتقويم الهجري +02:00"
								""",
								false,
								null),
					Arguments
							.of(true,
								"""
								نقطة_زمنية "٧ صفر ١٤٤٣ بالتقويم الهجري ٠٨:٢٠:٤٥ +02:00" => نقطة_زمنية "الآن بالتقويم الهجري +02:00"
								""",
								true,
								null),
					Arguments
							.of(true,
								"""
								نقطة_زمنية "٧ صفر ١٤٤٣ بالتقويم الهجري ٠٨:٢٠:٤٥ +02:00" أصغر_أو_يساوي نقطة_زمنية "الآن بالتقويم الهجري +02:00"
								""",
								true,
								null),
					Arguments
							.of(true,
								"""
								نقطة_زمنية "٧ صفر ١٤٤٣ بالتقويم الهجري ٠٨:٢٠:٤٥ +02:00" >= نقطة_زمنية "الآن بالتقويم الهجري +02:00"
								""",
								false,
								null),
					Arguments
							.of(true,
								"""
								نقطة_زمنية "٧ صفر ١٤٤٣ بالتقويم الهجري ٠٨:٢٠:٤٥ +02:00" أكبر_أو_يساوي نقطة_زمنية "الآن بالتقويم الهجري +02:00"
								""",
								false,
								null),
					Arguments
							.of(true,
								"""
								نقطة_زمنية "٧ صفر ١٤٤٣ بالتقويم الهجري ٠٨:٢٠:٤٥ +02:00" =! نقطة_زمنية "الآن بالتقويم الهجري +02:00"
								""",
								true,
								null),
					Arguments
							.of(true,
								"""
								نقطة_زمنية "٧ صفر ١٤٤٣ بالتقويم الهجري ٠٨:٢٠:٤٥ +02:00" لا_يساوي نقطة_زمنية "الآن بالتقويم الهجري +02:00"
								""",
								true,
								null),
					Arguments
							.of(true,
								"""
								نقطة_زمنية "٧ صفر ١٤٤٣ بالتقويم الهجري ٠٨:٢٠:٤٥ +02:00" == نقطة_زمنية "الآن بالتقويم الهجري +02:00"
								""",
								false,
								null),
					Arguments
							.of(true,
								"""
								نقطة_زمنية "٧ صفر ١٤٤٣ بالتقويم الهجري ٠٨:٢٠:٤٥ +02:00" يساوي نقطة_زمنية "الآن بالتقويم الهجري +02:00"
								""",
								false,
								null),
					Arguments
							.of(true,
								"""
								زمن "١٥ مارس ٢٠٢٣ بالتقويم الميلادي ٠٩:٤٥:١٥ بتوقيت دبي" + قيمة_زمنية "فترة 1 سنة و 3 أشهر و 10 أيام"
								""",
								createArabicDateTime(   25,
														JUNE_LATIN,
														6,
														ChronologyUtils.DEFAULT_CHRONOLOGY,
														DEFAULT_CALENDAR_NAME_1,
														2024,
														false,
														"دبي",
														9,
														45,
														15,
														null,
														null),
								null),
					Arguments
							.of(true,
								"""
								زمن "١٥ مارس ٢٠٢٣ بالتقويم الميلادي ٠٩:٤٥:١٥ بتوقيت دبي" - قيمة_زمنية "فترة 1 سنة و 3 أشهر و 10 أيام"
								""",
								createArabicDateTime(   5,
														DECEMBER_LATIN,
														12,
														ChronologyUtils.DEFAULT_CHRONOLOGY,
														DEFAULT_CALENDAR_NAME_1,
														2021,
														false,
														"دبي",
														9,
														45,
														15,
														null,
														null),
								null),
					Arguments
							.of(true,
								"""
								زمن "١٥ مارس ٢٠٢٣ بالتقويم الميلادي ٠٩:٤٥:١٥ بتوقيت دبي" أصغر_من نقطة_زمنية "الآن بالتقويم الهجري +02:00"
								""",
								true,
								null),
					Arguments
							.of(true,
								"""
								زمن "١٥ مارس ٢٠٢٣ بالتقويم الميلادي ٠٩:٤٥:١٥ بتوقيت دبي" أكبر_من نقطة_زمنية "الآن بالتقويم الهجري +02:00"
								""",
								false,
								null),
					Arguments
							.of(true,
								"""
								زمن "١٥ مارس ٢٠٢٣ بالتقويم الميلادي ٠٩:٤٥:١٥ بتوقيت دبي" أصغر_أو_يساوي نقطة_زمنية "الآن بالتقويم الهجري +02:00"
								""",
								true,
								null),
					Arguments
							.of(true,
								"""
								زمن "١٥ مارس ٢٠٢٣ بالتقويم الميلادي ٠٩:٤٥:١٥ بتوقيت دبي" أكبر_أو_يساوي نقطة_زمنية "الآن بالتقويم الهجري +02:00"
								""",
								false,
								null),
					Arguments
							.of(true,
								"""
								زمن "١٥ مارس ٢٠٢٣ بالتقويم الميلادي ٠٩:٤٥:١٥ بتوقيت دبي" لا_يساوي نقطة_زمنية "الآن بالتقويم الهجري +02:00"
								""",
								true,
								null),
					Arguments
							.of(true,
								"""
								زمن "١٥ مارس ٢٠٢٣ بالتقويم الميلادي ٠٩:٤٥:١٥ بتوقيت دبي" يساوي نقطة_زمنية "الآن بالتقويم الهجري +02:00"
								""",
								false,
								null),
					Arguments
							.of(true,
								"""
								زمن "١ يناير ٢٠٢٤ بالتقويم الميلادي ١٢:٠٠ صباحاً +02:00" + قيمة_زمنية "فترة 1 سنة و 3 أشهر و 10 أيام"
								""",
								createArabicDateTime(   11,
														APRIL_LATIN,
														4,
														ChronologyUtils.DEFAULT_CHRONOLOGY,
														DEFAULT_CALENDAR_NAME_1,
														2025,
														true,
														"+02:00",
														12,
														0,
														null,
														null,
														false),
								null),
					Arguments
							.of(true,
								"""
								زمن "١ يناير ٢٠٢٤ بالتقويم الميلادي ١٢:٠٠ صباحاً +02:00" - قيمة_زمنية "فترة 1 سنة و 3 أشهر و 10 أيام"
								""",
								createArabicDateTime(   21,
														SEPTEMBER,
														9,
														ChronologyUtils.DEFAULT_CHRONOLOGY,
														DEFAULT_CALENDAR_NAME_1,
														2022,
														true,
														"+02:00",
														12,
														0,
														null,
														null,
														false),
								null),
					Arguments
							.of(true,
								"""
								زمن "١ يناير ٢٠٢٤ بالتقويم الميلادي ١٢:٠٠ صباحاً +02:00" لا_يساوي نقطة_زمنية "الآن بالتقويم الهجري +02:00"
								""",
								true,
								null),
					Arguments
							.of(true,
								"""
								زمن "١ يناير ٢٠٢٤ بالتقويم الميلادي ١٢:٠٠ صباحاً +02:00" يساوي نقطة_زمنية "الآن بالتقويم الهجري +02:00"
								""",
								false,
								null),
					Arguments
							.of(false,
								"""
								نقطة_زمنية "٧ صفر ١٤٤٣ بالتقويم الهجري ٠٨:٢٠:٤٥ +02:00" أصغر_من قيمة_زمنية "فترة 1 سنة و 3 أشهر و 10 أيام"
								""",
								null,
								BinaryOperation
										.newNaftahBugError( BinaryOperation.LESS_THAN,
															NaftahDateTime.now(),
															NaftahPeriod.ofZero())),
					Arguments
							.of(false,
								"""
								زمن "١٥ مارس ٢٠٢٣ بالتقويم الميلادي ٠٩:٤٥:١٥ بتوقيت دبي" أكبر_من قيمة_زمنية "فترة 1 سنة و 3 أشهر و 10 أيام"
								""",
								null,
								BinaryOperation
										.newNaftahBugError( BinaryOperation.GREATER_THAN,
															NaftahDateTime.now(),
															NaftahPeriod.ofZero())),
					Arguments
							.of(false,
								"""
								زمن "١ يناير ٢٠٢٤ بالتقويم الميلادي ١٢:٠٠ صباحاً +02:00" أصغر_أو_يساوي قيمة_زمنية "مدة 3 ساعات"
								""",
								null,
								BinaryOperation
										.newNaftahBugError( BinaryOperation.LESS_THAN_EQUALS,
															NaftahDateTime.now(),
															NaftahDuration.ofZero())),
					Arguments
							.of(true,
								"""
								نوع (الوقت_الحالي()) يساوي "'وقت'"
								""",
								true,
								null),
					Arguments
							.of(true,
								"""
								نوع (الوقت_الحالي_بتوقيت("دبي")) يساوي "'وقت'"
								""",
								true,
								null),
					Arguments
							.of(true,
								"""
								نوع (الوقت_الحالي_بإزاحة("+02:00")) يساوي "'وقت'"
								""",
								true,
								null),
					Arguments
							.of(true,
								"""
								انشاء_الوقت(15 , 03)
								""",
								NaftahTime.of(15, 3),
								null),
					Arguments
							.of(true,
								"""
								انشاء_الوقت_بتوقيت(10 , 20 , "تونس")
								""",
								NaftahTime.of(10, 20, NaftahTime.ZoneOrOffset.ofZone("تونس")),
								null),
					Arguments
							.of(true,
								"""
								انشاء_الوقت_بإزاحة(10 , 45 , "+02:00")
								""",
								NaftahTime.of(10, 45, NaftahTime.ZoneOrOffset.ofOffset("+02:00")),
								null),
					Arguments
							.of(true,
								"""
								انشاء_الوقت_بتوقيت_مع_ثانية(15 , 03 ؛ 10 , "تونس")
								""",
								NaftahTime.of(15, 3, 10, NaftahTime.ZoneOrOffset.ofZone("تونس")),
								null),
					Arguments
							.of(true,
								"""
								انشاء_الوقت_بتوقيت_مع_ثانية(10 , 20 ؛ 10 , "تونس")
								""",
								NaftahTime.of(10, 20, 10, NaftahTime.ZoneOrOffset.ofZone("تونس")),
								null),
					Arguments
							.of(true,
								"""
								انشاء_الوقت_بإزاحة_مع_ثانية(10 , 45 ؛ 10 , "+02:00")
								""",
								NaftahTime.of(10, 45, 10, NaftahTime.ZoneOrOffset.ofOffset("+02:00")),
								null),
					Arguments
							.of(true,
								"""
								انشاء_الوقت_مع_نانوثانية(15 , 03 ؛ 10؛ 10)
								""",
								NaftahTime.of(15, 3, 10, 10),
								null),
					Arguments
							.of(true,
								"""
								انشاء_الوقت_مع_نانوثانية_بتوقيت(10 , 20 ؛ 10 ؛ 10 , "تونس")
								""",
								NaftahTime.of(10, 20, 10, 10, NaftahTime.ZoneOrOffset.ofZone("تونس")),
								null),
					Arguments
							.of(true,
								"""
								انشاء_الوقت_مع_الإزاحة_ونانوثانية(10 , 45 ؛ 10؛ 10 , "+02:00")
								""",
								NaftahTime.of(10, 45, 10, 10, NaftahTime.ZoneOrOffset.ofOffset("+02:00")),
								null),
					Arguments
							.of(true,
								"""
								انشاء_الوقت_من_ثواني_اليوم(24739)
								""",
								NaftahTime.ofSecondOfDay(24739),
								null),
					Arguments
							.of(true,
								"""
								انشاء_الوقت_من_ثواني_اليوم_بتوقيت(24739 , "تونس")
								""",
								NaftahTime.ofSecondOfDay(24739, NaftahTime.ZoneOrOffset.ofZone("تونس")),
								null),
					Arguments
							.of(true,
								"""
								انشاء_الوقت_من_ثواني_اليوم_بإزاحة(24739 , "+02:00")
								""",
								NaftahTime.ofSecondOfDay(24739, NaftahTime.ZoneOrOffset.ofOffset("+02:00")),
								null),
					Arguments
							.of(true,
								"""
								انشاء_الوقت_من_نانوثواني_اليوم(24712312312339)
								""",
								NaftahTime.ofNanoOfDay(24712312312339L),
								null),
					Arguments
							.of(true,
								"""
								انشاء_الوقت_من_نانوثواني_اليوم_بتوقيت(24731231231239 , "تونس")
								""",
								NaftahTime.ofNanoOfDay(24731231231239L, NaftahTime.ZoneOrOffset.ofZone("تونس")),
								null),
					Arguments
							.of(true,
								"""
								انشاء_الوقت_من_نانوثواني_اليوم_بإزاحة(2471231339 , "+02:00")
								""",
								NaftahTime.ofNanoOfDay(2471231339L, NaftahTime.ZoneOrOffset.ofOffset("+02:00")),
								null),
					Arguments
							.of(true,
								"""
								نوع (التاريخ_الحالي()) يساوي "'تاريخ'"
								""",
								true,
								null),
					Arguments
							.of(true,
								"""
								نوع (التاريخ_الحالي_بتقويم("التقويم الهجري")) يساوي "'تاريخ'"
								""",
								true,
								null),
					Arguments
							.of(true,
								"""
								نوع (التاريخ_الحالي_بتوقيت("القدس")) يساوي "'تاريخ'"
								""",
								true,
								null),
					Arguments
							.of(true,
								"""
								نوع (التاريخ_الحالي_بإزاحة("+04:00")) يساوي "'تاريخ'"
								""",
								true,
								null),
					Arguments
							.of(true,
								"""
								نوع (التاريخ_الحالي_بتقويم_وتوقيت("الهجري"؛"غزة")) يساوي "'تاريخ'"
								""",
								true,
								null),
					Arguments
							.of(true,
								"""
								نوع (التاريخ_الحالي_بتقويم_وإزاحة("جرجوري"؛"+02:00")) يساوي "'تاريخ'"
								""",
								true,
								null),
					Arguments
							.of(true,
								"""
								انشاء_التاريخ_من_اليوم_و_شهر_و_سنة(14 , "ماي" , 1995)
								""",
								NaftahDate.of(14, 5, 1995),
								null),
					Arguments
							.of(true,
								"""
								انشاء_التاريخ_من_اليوم_و_شهر_و_سنة(14 , 05 , 1995)
								""",
								NaftahDate.of(14, 5, 1995),
								null),
					Arguments
							.of(true,
								"""
								انشاء_التاريخ_من_اليوم_و_شهر_و_سنة_بتقويم("التقويم الميلادي" ، 14 , "ماي" , 1995)
								""",
								NaftahDate.of(14, 5, 1995),
								null),
					Arguments
							.of(true,
								"""
								انشاء_التاريخ_من_اليوم_و_شهر_و_سنة_بتقويم("التقويم الميلادي" ، 14 , 05 , 1995)
								""",
								NaftahDate.of(14, 5, 1995),
								null),
					Arguments
							.of(true,
								"""
								نوع (التاريخ_والوقت_الحالي()) يساوي "'تاريخ_و_وقت'"
								""",
								true,
								null),
					Arguments
							.of(true,
								"""
								نوع (التاريخ_والوقت_الحالي_بتقويم("ميلادي")) يساوي "'تاريخ_و_وقت'"
								""",
								true,
								null),
					Arguments
							.of(true,
								"""
								نوع (التاريخ_والوقت_الحالي_بتوقيت("غزة")) يساوي "'تاريخ_و_وقت'"
								""",
								true,
								null),
					Arguments
							.of(true,
								"""
								نوع (التاريخ_والوقت_الحالي_بإزاحة("+02:00")) يساوي "'تاريخ_و_وقت'"
								""",
								true,
								null),
					Arguments
							.of(true,
								"""
								نوع (التاريخ_والوقت_الحالي_بتقويم_وتوقيت("الهجري", "غزة")) يساوي "'تاريخ_و_وقت'"
								""",
								true,
								null),
					Arguments
							.of(true,
								"""
								نوع (التاريخ_والوقت_الحالي_بتقويم_وإزاحة("الهجري", "+02:00")) يساوي "'تاريخ_و_وقت'"
								""",
								true,
								null),
					Arguments
							.of(true,
								"""
								انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة(1995؛5؛3؛14؛5)
								""",
								NaftahDateTime
										.of(ChronologyUtils.DEFAULT_CHRONOLOGY,
											1995,
											5,
											3,
											14,
											5),
								null),
					Arguments
							.of(true,
								"""
								انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة(1995؛"ماي"؛3؛14؛5)
								""",
								NaftahDateTime
										.of(ChronologyUtils.DEFAULT_CHRONOLOGY,
											1995,
											5,
											3,
											14,
											5),
								null),
					Arguments
							.of(true,
								"""
								انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة_بتوقيت(1995؛5؛3؛14؛5؛"تونس")
								""",
								NaftahDateTime
										.of(ChronologyUtils.DEFAULT_CHRONOLOGY,
											1995,
											5,
											3,
											14,
											5,
											NaftahTime.ZoneOrOffset.ofZone("تونس")),
								null),
					Arguments
							.of(true,
								"""
								انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة_بتوقيت(1995؛"ماي"؛3؛14؛5؛"تونس")
								""",
								NaftahDateTime
										.of(ChronologyUtils.DEFAULT_CHRONOLOGY,
											1995,
											5,
											3,
											14,
											5,
											NaftahTime.ZoneOrOffset.ofZone("تونس")),
								null,
								null),
					Arguments
							.of(true,
								"""
								انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة_بإزاحة(1995؛5؛3؛14؛5؛"+02:00")
								""",
								NaftahDateTime
										.of(1995,
											MAY_LATIN,
											3,
											14,
											5,
											NaftahTime.ZoneOrOffset.ofOffset("+02:00")),
								null),
					Arguments
							.of(true,
								"""
								انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة_بإزاحة(1995؛"ماي"؛3؛14؛5؛"+02:00")
								""",
								NaftahDateTime
										.of(1995,
											MAY_LATIN,
											3,
											14,
											5,
											NaftahTime.ZoneOrOffset.ofOffset("+02:00")),
								null),
					Arguments
							.of(true,
								"""
								انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة_وثانية(1995؛5؛3؛14؛5؛3)
								""",
								NaftahDateTime
										.of(ChronologyUtils.DEFAULT_CHRONOLOGY,
											1995,
											5,
											3,
											14,
											5,
											3),
								null),
					Arguments
							.of(true,
								"""
								انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة_وثانية(1995؛"ماي"؛3؛14؛5؛3)
								""",
								NaftahDateTime
										.of(ChronologyUtils.DEFAULT_CHRONOLOGY,
											1995,
											5,
											3,
											14,
											5,
											3),
								null),
					Arguments
							.of(true,
								"""
								انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة_وثانية_بتوقيت(1995؛5؛3؛14؛5؛3؛"تونس")
								""",
								NaftahDateTime
										.of(ChronologyUtils.DEFAULT_CHRONOLOGY,
											1995,
											5,
											3,
											14,
											5,
											3,
											NaftahTime.ZoneOrOffset.ofZone("تونس")),
								null),
					Arguments
							.of(true,
								"""
								انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة_وثانية_بتوقيت(1995؛"ماي"؛3؛14؛5؛3؛"تونس")
								""",
								NaftahDateTime
										.of(ChronologyUtils.DEFAULT_CHRONOLOGY,
											1995,
											5,
											3,
											14,
											5,
											3,
											NaftahTime.ZoneOrOffset.ofZone("تونس")),
								null),
					Arguments
							.of(true,
								"""
								انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة_وثانية_بإزاحة(1995؛5؛3؛14؛5؛3؛"+02:00")
								""",
								NaftahDateTime
										.of(1995,
											MAY_LATIN,
											3,
											14,
											5,
											3,
											NaftahTime.ZoneOrOffset.ofOffset("+02:00")),
								null),
					Arguments
							.of(true,
								"""
								انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة_وثانية_بإزاحة(1995؛"ماي"؛3؛14؛5؛3؛"+02:00")
								""",
								NaftahDateTime
										.of(1995,
											MAY_LATIN,
											3,
											14,
											5,
											3,
											NaftahTime.ZoneOrOffset.ofOffset("+02:00")),
								null),
					Arguments
							.of(true,
								"""
								انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة_وثانية_ونانوثانية(1995؛5؛3؛14؛5؛3؛505)
								""",
								NaftahDateTime
										.of(ChronologyUtils.DEFAULT_CHRONOLOGY,
											1995,
											5,
											3,
											14,
											5,
											3,
											505),
								null),
					Arguments
							.of(true,
								"""
								انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة_وثانية_ونانوثانية(1995؛"ماي"؛3؛14؛5؛3؛505)
								""",
								NaftahDateTime
										.of(ChronologyUtils.DEFAULT_CHRONOLOGY,
											1995,
											5,
											3,
											14,
											5,
											3,
											505),
								null),
					Arguments
							.of(true,
								"""
								انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة_وثانية_ونانوثانية_بتوقيت(1995؛5؛3؛14؛5؛3؛505؛"تونس")
								""",
								NaftahDateTime
										.of(ChronologyUtils.DEFAULT_CHRONOLOGY,
											1995,
											5,
											3,
											14,
											5,
											3,
											505,
											NaftahTime.ZoneOrOffset.ofZone("تونس")),
								null),
					Arguments
							.of(true,
								"""
								انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة_وثانية_ونانوثانية_بتوقيت(1995؛"ماي"؛3؛14؛5؛3؛505؛"تونس")
								""",
								NaftahDateTime
										.of(ChronologyUtils.DEFAULT_CHRONOLOGY,
											1995,
											5,
											3,
											14,
											5,
											3,
											505,
											NaftahTime.ZoneOrOffset.ofZone("تونس")),
								null),
					Arguments
							.of(true,
								"""
								انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة_وثانية_ونانوثانية_بإزاحة(1995؛5؛3؛14؛5؛3؛505؛"+02:00")
								""",
								NaftahDateTime
										.of(1995,
											MAY_LATIN,
											3,
											14,
											5,
											3,
											505,
											NaftahTime.ZoneOrOffset.ofOffset("+02:00")),
								null),
					Arguments
							.of(true,
								"""
								انشاء_التاريخ_والوقت_من_سنة_وشهر_ويوم_وساعة_ودقيقة_وثانية_ونانوثانية_بإزاحة(1995؛"ماي"؛3؛14؛5؛3؛505؛"+02:00")
								""",
								NaftahDateTime
										.of(1995,
											MAY_LATIN,
											3,
											14,
											5,
											3,
											505,
											NaftahTime.ZoneOrOffset.ofOffset("+02:00")),
								null),
					Arguments
							.of(true,
								"""
								انشاء_التاريخ_والوقت_بتقويم_وساعة_ودقيقة("ميلادي"؛1995؛5؛3؛14؛5)
								""",
								NaftahDateTime
										.of(ChronologyUtils.DEFAULT_CHRONOLOGY,
											1995,
											5,
											3,
											14,
											5),
								null),
					Arguments
							.of(true,
								"""
								انشاء_التاريخ_والوقت_بتقويم_وساعة_ودقيقة("ميلادي"؛1995؛"ماي"؛3؛14؛5)
								""",
								NaftahDateTime
										.of(ChronologyUtils.DEFAULT_CHRONOLOGY,
											1995,
											5,
											3,
											14,
											5),
								null),
					Arguments
							.of(true,
								"""
								انشاء_التاريخ_والوقت_بتقويم_وساعة_ودقيقة_بتوقيت("ميلادي"؛1995؛5؛3؛14؛5؛"تونس")
								""",
								NaftahDateTime
										.of(ChronologyUtils.DEFAULT_CHRONOLOGY,
											1995,
											5,
											3,
											14,
											5,
											NaftahTime.ZoneOrOffset.ofZone("تونس")),
								null),
					Arguments
							.of(true,
								"""
								انشاء_التاريخ_والوقت_بتقويم_وساعة_ودقيقة_بتوقيت("ميلادي"؛1995؛"ماي"؛3؛14؛5؛"تونس")
								""",
								NaftahDateTime
										.of(ChronologyUtils.DEFAULT_CHRONOLOGY,
											1995,
											5,
											3,
											14,
											5,
											NaftahTime.ZoneOrOffset.ofZone("تونس")),
								null),
					Arguments
							.of(true,
								"""
								انشاء_التاريخ_والوقت_بتقويم_وساعة_ودقيقة_بإزاحة("ميلادي"؛1995؛5؛3؛14؛5؛"+02:00")
								""",
								NaftahDateTime
										.of(ChronologyUtils.DEFAULT_CHRONOLOGY,
											1995,
											MAY_LATIN,
											3,
											14,
											5,
											NaftahTime.ZoneOrOffset.ofOffset("+02:00")),
								null),
					Arguments
							.of(true,
								"""
								انشاء_التاريخ_والوقت_بتقويم_وساعة_ودقيقة_بإزاحة("ميلادي"؛1995؛"ماي"؛3؛14؛5؛"+02:00")
								""",
								NaftahDateTime
										.of(ChronologyUtils.DEFAULT_CHRONOLOGY,
											1995,
											MAY_LATIN,
											3,
											14,
											5,
											NaftahTime.ZoneOrOffset.ofOffset("+02:00")),
								null),
					Arguments
							.of(true,
								"""
								انشاء_التاريخ_والوقت_بتقويم_وساعة_ودقيقة_وثانية("ميلادي"؛1995؛5؛3؛14؛5؛3)
								""",
								NaftahDateTime
										.of(ChronologyUtils.DEFAULT_CHRONOLOGY,
											1995,
											5,
											3,
											14,
											5,
											3),
								null),
					Arguments
							.of(true,
								"""
								انشاء_التاريخ_والوقت_بتقويم_وساعة_ودقيقة_وثانية("ميلادي"؛1995؛"ماي"؛3؛14؛5؛3)
								""",
								NaftahDateTime
										.of(ChronologyUtils.DEFAULT_CHRONOLOGY,
											1995,
											5,
											3,
											14,
											5,
											3),
								null),
					Arguments
							.of(true,
								"""
								انشاء_التاريخ_والوقت_بتقويم_وثانية_وبتوقيت("ميلادي"؛1995؛5؛3؛14؛5؛3؛"تونس")
								""",
								NaftahDateTime
										.of(ChronologyUtils.DEFAULT_CHRONOLOGY,
											1995,
											5,
											3,
											14,
											5,
											3,
											NaftahTime.ZoneOrOffset.ofZone("تونس")),
								null),
					Arguments
							.of(true,
								"""
								انشاء_التاريخ_والوقت_بتقويم_وثانية_وبتوقيت("ميلادي"؛1995؛"ماي"؛3؛14؛5؛3؛"تونس")
								""",
								NaftahDateTime
										.of(ChronologyUtils.DEFAULT_CHRONOLOGY,
											1995,
											5,
											3,
											14,
											5,
											3,
											NaftahTime.ZoneOrOffset.ofZone("تونس")),
								null),
					Arguments
							.of(true,
								"""
								انشاء_التاريخ_والوقت_بتقويم_وثانية_بإزاحة("ميلادي"؛1995؛5؛3؛14؛5؛3؛"+02:00")
								""",
								NaftahDateTime
										.of(ChronologyUtils.DEFAULT_CHRONOLOGY,
											1995,
											MAY_LATIN,
											3,
											14,
											5,
											3,
											NaftahTime.ZoneOrOffset.ofOffset("+02:00")),
								null),
					Arguments
							.of(true,
								"""
								انشاء_التاريخ_والوقت_بتقويم_وثانية_بإزاحة("ميلادي"؛1995؛"ماي"؛3؛14؛5؛3؛"+02:00")
								""",
								NaftahDateTime
										.of(ChronologyUtils.DEFAULT_CHRONOLOGY,
											1995,
											MAY_LATIN,
											3,
											14,
											5,
											3,
											NaftahTime.ZoneOrOffset.ofOffset("+02:00")),
								null),
					Arguments
							.of(true,
								"""
								انشاء_التاريخ_والوقت_بتقويم_وثانية_ونانوثانية("ميلادي"؛1995؛5؛3؛14؛5؛3؛505)
								""",
								NaftahDateTime
										.of(ChronologyUtils.DEFAULT_CHRONOLOGY,
											1995,
											MAY_LATIN,
											3,
											14,
											5,
											3,
											505),
								null),
					Arguments
							.of(true,
								"""
								انشاء_التاريخ_والوقت_بتقويم_وثانية_ونانوثانية("ميلادي"؛1995؛"ماي"؛3؛14؛5؛3؛505)
								""",
								NaftahDateTime
										.of(ChronologyUtils.DEFAULT_CHRONOLOGY,
											1995,
											MAY_LATIN,
											3,
											14,
											5,
											3,
											505),
								null),
					Arguments
							.of(true,
								"""
								انشاء_التاريخ_والوقت_بتقويم_وثانية_ونانوثانية_بتوقيت("ميلادي"؛1995؛5؛3؛14؛5؛3؛505؛"تونس")
								""",
								NaftahDateTime
										.of(ChronologyUtils.DEFAULT_CHRONOLOGY,
											1995,
											5,
											3,
											14,
											5,
											3,
											505,
											NaftahTime.ZoneOrOffset.ofZone("تونس")),
								null),
					Arguments
							.of(true,
								"""
								انشاء_التاريخ_والوقت_بتقويم_وثانية_ونانوثانية_بتوقيت("ميلادي"؛1995؛"ماي"؛3؛14؛5؛3؛505؛"تونس")
								""",
								NaftahDateTime
										.of(ChronologyUtils.DEFAULT_CHRONOLOGY,
											1995,
											MAY_LATIN,
											3,
											14,
											5,
											3,
											505,
											NaftahTime.ZoneOrOffset.ofZone("تونس")),
								null),
					Arguments
							.of(true,
								"""
								انشاء_التاريخ_والوقت_بتقويم_وثانية_ونانوثانية_بإزاحة("ميلادي"؛1995؛5؛3؛14؛5؛3؛505؛"+02:00")
								""",
								NaftahDateTime
										.of(ChronologyUtils.DEFAULT_CHRONOLOGY,
											1995,
											MAY_LATIN,
											3,
											14,
											5,
											3,
											505,
											NaftahTime.ZoneOrOffset.ofOffset("+02:00")),
								null),
					Arguments
							.of(true,
								"""
								انشاء_التاريخ_والوقت_بتقويم_وثانية_ونانوثانية_بإزاحة("ميلادي"؛1995؛"ماي"؛3؛14؛5؛3؛505؛"+02:00")
								""",
								NaftahDateTime
										.of(ChronologyUtils.DEFAULT_CHRONOLOGY,
											1995,
											5,
											3,
											14,
											5,
											3,
											505,
											NaftahTime.ZoneOrOffset.ofOffset("+02:00")),
								null),
					Arguments
							.of(true,
								"""
								تحويل_الى_يوم_منذ_الحقبة(انشاء_التاريخ_من_اليوم_و_شهر_و_سنة(1 , 1 , 2026))""",
								20454,
								null),
					Arguments
							.of(true,
								"""
								احصل_على_السنة(انشاء_التاريخ_من_اليوم_و_شهر_و_سنة(1 , 1 , 2026))""",
								2026,
								null),
					Arguments
							.of(true,
								"""
								احصل_على_رقم_الشهر(انشاء_التاريخ_من_اليوم_و_شهر_و_سنة(1 , 1 , 2026))""",
								1,
								null),
					Arguments
							.of(true,
								"""
								احصل_على_الشهر(انشاء_التاريخ_من_اليوم_و_شهر_و_سنة(1 , 1 , 2026))""",
								JANUARY_LATIN,
								null),
					Arguments
							.of(true,
								"""
								احصل_على_اليوم(انشاء_التاريخ_من_اليوم_و_شهر_و_سنة(1 , 1 , 2026))""",
								1,
								null),
					Arguments
							.of(true,
								"""
								احصل_على_يوم_السنة(انشاء_التاريخ_من_اليوم_و_شهر_و_سنة(1 , 1 , 2026))""",
								1,
								null),
					Arguments
							.of(true,
								"""
								احصل_على_يوم_الأسبوع(انشاء_التاريخ_من_اليوم_و_شهر_و_سنة(1 , 1 , 2026))""",
								"الخميس",
								null),
					Arguments
							.of(true,
								"""
								هل_السنة_كبيسة(انشاء_التاريخ_من_اليوم_و_شهر_و_سنة(1 , 1 , 2026))""",
								false,
								null),
					Arguments
							.of(true,
								"""
								عدد_ايام_الشهر(انشاء_التاريخ_من_اليوم_و_شهر_و_سنة(1 , 1 , 2026))""",
								31,
								null),
					Arguments
							.of(true,
								"""
								عدد_ايام_السنة(انشاء_التاريخ_من_اليوم_و_شهر_و_سنة(1 , 1 , 2026))""",
								365,
								null),
					Arguments
							.of(true,
								"""
								اضافة_سنوات(انشاء_التاريخ_من_اليوم_و_شهر_و_سنة(1 , 1 , 2026), 1)""",
								NaftahDate
										.of(1, 1, 2027
										),
								null),
					Arguments
							.of(true,
								"""
								اضافة_اشهر(انشاء_التاريخ_من_اليوم_و_شهر_و_سنة(1 , 1 , 2026), 1)""",
								NaftahDate
										.of(1, 2, 2026
										),
								null),
					Arguments
							.of(true,
								"""
								اضافة_اسابيع(انشاء_التاريخ_من_اليوم_و_شهر_و_سنة(1 , 1 , 2026), 1)""",
								NaftahDate
										.of(8, 1, 2026
										),
								null),
					Arguments
							.of(true,
								"""
								اضافة_ايام(انشاء_التاريخ_من_اليوم_و_شهر_و_سنة(1 , 1 , 2026), 1)""",
								NaftahDate
										.of(2, 1, 2026
										),
								null),
					Arguments
							.of(true,
								"""
								طرح_سنوات(انشاء_التاريخ_من_اليوم_و_شهر_و_سنة(1 , 1 , 2026), 1)""",
								NaftahDate
										.of(1, 1, 2025
										),
								null),
					Arguments
							.of(true,
								"""
								طرح_شهور(انشاء_التاريخ_من_اليوم_و_شهر_و_سنة(1 , 1 , 2026), 1)""",
								NaftahDate
										.of(1, 12, 2025
										),
								null),
					Arguments
							.of(true,
								"""
								طرح_اسابيع(انشاء_التاريخ_من_اليوم_و_شهر_و_سنة(1 , 1 , 2026), 1)""",
								NaftahDate
										.of(25, 12, 2025
										),
								null),
					Arguments
							.of(true,
								"""
								طرح_ايام(انشاء_التاريخ_من_اليوم_و_شهر_و_سنة(1 , 1 , 2026), 1)""",
								NaftahDate
										.of(31, 12, 2025
										),
								null),
					Arguments
							.of(true,
								"""
								احصل_على_الساعة(انشاء_الوقت(15 , 03))""",
								15,
								null),
					Arguments
							.of(true,
								"""
								احصل_على_الدقيقة(انشاء_الوقت(15 , 03))""",
								3,
								null),
					Arguments
							.of(true,
								"""
								احصل_على_الثانية(انشاء_الوقت(15 , 03))""",
								0,
								null),
					Arguments
							.of(true,
								"""
								احصل_على_الملي_ثانية(انشاء_الوقت(15 , 03))""",
								0,
								null),
					Arguments
							.of(true,
								"""
								احصل_على_النانو_ثانية(انشاء_الوقت(15 , 03))""",
								0,
								null),
					Arguments
							.of(true,
								"""
								اضافة_ساعات(انشاء_الوقت(15 , 03),1)""",
								NaftahTime
										.of(
											16,
											3,
											0,
											0
										),
								null),
					Arguments
							.of(true,
								"""
								اضافة_دقائق(انشاء_الوقت(15 , 03),1)""",
								NaftahTime
										.of(
											15,
											4,
											0,
											0
										),
								null),
					Arguments
							.of(true,
								"""
								اضافة_ثواني(انشاء_الوقت(15 , 03),1)""",
								NaftahTime
										.of(
											15,
											3,
											1,
											0
										),
								null),
					Arguments
							.of(true,
								"""
								اضافة_نانوثواني(انشاء_الوقت(15 , 03),1)""",
								NaftahTime
										.of(
											15,
											3,
											0,
											1
										),
								null),
					Arguments
							.of(true,
								"""
								طرح_ساعات(انشاء_الوقت(15 , 03),1)""",
								NaftahTime
										.of(
											14,
											3,
											0,
											0
										),
								null),
					Arguments
							.of(true,
								"""
								طرح_دقائق(انشاء_الوقت(15 , 03),1)""",
								NaftahTime
										.of(
											15,
											2,
											0,
											0
										),
								null),
					Arguments
							.of(true,
								"""
								طرح_ثواني(انشاء_الوقت(15 , 03),1)""",
								NaftahTime
										.of(
											15,
											2,
											59,
											0
										),
								null),
					Arguments
							.of(true,
								"""
								طرح_نانوثواني(انشاء_الوقت(15 , 03),1)""",
								NaftahTime
										.of(
											15,
											2,
											59,
											999999999
										),
								null)
				);
	}
}
