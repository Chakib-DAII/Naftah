/**
 *
 * The ArabicDateParser grammar defines the syntactic structure for parsing
 * Arabic date and time expressions using tokens provided by ArabicDateLexer.
 *
 * It supports:
 * - Date-only expressions (day, month, year)
 * - Time-only expressions
 * - Combined date-time expressions
 * - Optional time zone or numeric offset specifiers
 * - Optional calendar specification (e.g. Hijri, Gregorian)
 *
 * This parser focuses on composing meaningful date/time structures from
 * previously tokenized Arabic text.
 *
 *
 * قواعد ArabicDateParser تحدد البنية النحوية لتحليل تعبيرات التاريخ والوقت
 * باللغة العربية، اعتماداً على الرموز المعرفة في ArabicDateLexer.
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

parser grammar ArabicDateParser;

options { tokenVocab = ArabicDateLexer; }

root: dateSpecifier zonedOrOffsetTimeSpecifier? #dateTime
    | dateSpecifier #date
    | zonedOrOffsetTimeSpecifier #time
    ;

dateSpecifier: NUMBER MONTH NUMBER calendarSpecifier?;

zonedOrOffsetTimeSpecifier: timeSpecifier zoneOrOffsetSpecifier?;

timeSpecifier: NUMBER COLON NUMBER (COLON NUMBER (DOT NUMBER)?)? AMPM?;

zoneOrOffsetSpecifier: ZONE_PREFIX ARABIC_WORDS #zoneSpecifier
    				 | OFFSET #offsetSpecifier
    				 ;

calendarSpecifier: CALENDAR_PREFIX ARABIC_WORDS;


