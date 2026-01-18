// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

/**
 *
 * The NaftahDateParser grammar defines the syntactic structure for parsing
 * Naftah date and time expressions using tokens provided by NaftahDateLexer.
 *
 * It supports:
 * - Date-only expressions (day, month, year)
 * - Time-only expressions
 * - Combined date-time expressions
 * - Optional time zone or numeric offset specifiers
 * - Optional calendar specification (e.g. Hijri, Gregorian)
 * - Duration-only expressions
 * - Period-only expressions
 * - Combined period-duration expressions
 *
 * This parser focuses on composing meaningful date/time structures from
 * previously tokenized Naftah text.
 *
 *
 * قواعد NaftahDateParser تحدد البنية النحوية لتحليل تعبيرات التاريخ والوقت
 * باللغة العربية، اعتماداً على الرموز المعرفة في NaftahDateLexer.
 *
 * تدعم القواعد:
 * - التواريخ فقط (اليوم، الشهر، السنة)
 * - الأوقات فقط
 * - تعبيرات التاريخ والوقت معاً
 * - تحديد المنطقة الزمنية أو فرق التوقيت
 * - تحديد نوع التقويم بشكل اختياري
 *
 * يركز هذا المحلل النحوي على تركيب بنى زمنية ذات معنى انطلاقاً من النص
 * العربي بعد تحليله معجمياً.
 *
 * @author Chakib Daii
 *
 */

parser grammar NaftahDateParser;

options { tokenVocab = NaftahDateLexer; }

root: nowSpecifier 									#now
    | dateTimeSpecifier								#dateTime
    | zonedOrOffsetTimeSpecifier 					#time
    | periodSpecifier (AND timeAmount)?    			#periodWithDuration
	| durationSpecifier                     		#duration
	| betweenSpecifier                     			#betweenTemporalAmount
	;

nowSpecifier: DATE NOW calendarSpecifier? zoneOrOffsetSpecifier?		#nowAsDate
			| TIME NOW zoneOrOffsetSpecifier? 							#nowAsTime
			| DATE_TIME? NOW calendarSpecifier? zoneOrOffsetSpecifier? 	#nowAsDateTime
			;

dateTimeSpecifier: dateSpecifier zonedOrOffsetTimeSpecifier?;

dateSpecifier: NUMBER MONTH_NAME NUMBER calendarSpecifier?;

zonedOrOffsetTimeSpecifier: timeSpecifier zoneOrOffsetSpecifier?;

timeSpecifier: NUMBER COLON NUMBER (COLON NUMBER (DOT NUMBER)?)? AMPM?;

zoneOrOffsetSpecifier: ZONE_PREFIX ARABIC_WORDS #zoneSpecifier
    				 | OFFSET #offsetSpecifier
    				 ;

calendarSpecifier: CALENDAR_PREFIX ARABIC_WORDS;

durationSpecifier: DURATION_PREFIX timeAmount;

timeAmount: (NUMBER HOUR)?
	  		(AND? NUMBER MINUTE)?
	  		(AND? NUMBER (DOT NUMBER)? SECOND)?
	  		(AND? NUMBER NANOSECOND)?
			;

periodSpecifier: PERIOD_PREFIX dateAmount;

dateAmount: (NUMBER YEAR)?
			(AND? NUMBER MONTH)?
  			(AND? NUMBER DAY)?
  			;

betweenSpecifier: BETWEEN betweenTimeSpecifier AND betweenTimeSpecifier;

betweenTimeSpecifier: nowSpecifier
    				| dateTimeSpecifier
    				| zonedOrOffsetTimeSpecifier
    				;