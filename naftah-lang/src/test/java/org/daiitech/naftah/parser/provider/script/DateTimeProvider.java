package org.daiitech.naftah.parser.provider.script;

import java.time.ZoneId;
import java.time.chrono.Chronology;
import java.util.Objects;
import java.util.stream.Stream;

import org.daiitech.naftah.builtin.time.ArabicDate;
import org.daiitech.naftah.builtin.time.ArabicDateTime;
import org.daiitech.naftah.builtin.time.ArabicTime;
import org.daiitech.naftah.utils.time.ChronologyUtils;
import org.daiitech.naftah.utils.time.TemporalUtils;
import org.daiitech.naftah.utils.time.ZoneUtils;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import static org.daiitech.naftah.utils.time.Constants.DEFAULT_CALENDAR_NAME_1;
import static org.daiitech.naftah.utils.time.Constants.DHU_AL_HIJJAH;
import static org.daiitech.naftah.utils.time.Constants.HIJRI_CALENDAR_NAME_1;
import static org.daiitech.naftah.utils.time.Constants.JANUARY_AR;
import static org.daiitech.naftah.utils.time.Constants.JULY_AR;
import static org.daiitech.naftah.utils.time.Constants.MARCH;
import static org.daiitech.naftah.utils.time.Constants.OCTOBER;
import static org.daiitech.naftah.utils.time.Constants.RAJAB;
import static org.daiitech.naftah.utils.time.Constants.RAMADAN;
import static org.daiitech.naftah.utils.time.Constants.SAFAR;
import static org.daiitech.naftah.utils.time.Constants.SEPTEMBER;
import static org.daiitech.naftah.utils.time.Constants.SHAWAL;
import static org.daiitech.naftah.utils.time.ZoneUtils.parseZoneOffset;

public class DateTimeProvider implements ArgumentsProvider {
	private ArabicDateTime createArabicDateTime(int day,
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
				zoneId = ZoneId.of(ZoneUtils.arabicZoneNameToJava(arabicZoneOrOffset));
			}
		}

		ArabicTime.ZoneOrOffset zoneOrOffset;
		if (Boolean.TRUE.equals(offset)) {
			zoneOrOffset = ArabicTime.ZoneOrOffset.ofOffset(arabicZoneOrOffset);
		}
		else {
			zoneOrOffset = ArabicTime.ZoneOrOffset.ofZone(arabicZoneOrOffset);
		}

		return ArabicDateTime
				.of(
					ArabicDate
							.of(
								ArabicDate.Date.of(day, arabicMonth, chronology, year),
								ArabicDate.Calendar.of(chronologyName, chronology),
								TemporalUtils.createDate(day, monthValue, year, chronology)
							),
					ArabicTime
							.of(
								ArabicTime.Time.of(hour, minute, second, nano, isPM),
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
							)
				);
	}
}
