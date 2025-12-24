/**
 *
 * The ArabicDateLexer grammar defines lexical rules for parsing Arabic date
 * and time expressions. It supports multiple calendar systems and regional
 * variations, including:
 *
 * - Gregorian months (standard Arabic and Tunisian variants)
 * - Hijri (Islamic) months
 * - Arabic AM/PM indicators
 * - Time zone and calendar prefixes
 * - Arabic numerals (Eastern and Western)
 *
 * This grammar is designed to be flexible and culturally aware, making it
 * suitable for natural-language date parsing in Arabic contexts.
 *
 *
 * قواعد ArabicDateLexer تهدف إلى تعريف القواعد المعجمية لتحليل تعبيرات
 * التاريخ والوقت باللغة العربية. تدعم القواعد:
 *
 * - أشهر التقويم الميلادي (العربي والتونسي)
 * - أشهر التقويم الهجري
 * - مؤشرات الوقت (صباحاً / مساءً)
 * - بادئات التوقيت والتقويم
 * - الأرقام العربية الشرقية والغربية
 *
 * تم تصميم هذه القواعد لتكون مرنة ومراعية للاختلافات اللغوية والثقافية
 * في استخدام التواريخ باللغة العربية.
 *
 * @author Chakib Daii
 *
 */

lexer grammar ArabicDateLexer;

MONTH_NAME
    // Gregorian + Tunisian
    : 'يناير' | 'جانفي'
    | 'فبراير' | 'فيفري'
    | 'مارس'
    | 'أبريل' | 'أفريل'
    | 'مايو' | 'ماي'
    | 'يونيو' | 'جوان'
    | 'يوليو' | 'جويلية'
    | 'أغسطس' | 'أوت'
    | 'سبتمبر'
    | 'أكتوبر' | 'اكتوبر'
    | 'نوفمبر'
    | 'ديسمبر' | 'دجنبر'

    // Hijri
    | 'محرم'
    | 'صفر'
    | 'ربيع الأول'
    | 'ربيع الآخر' | 'ربيع الثاني'
    | 'جمادى الأولى'
    | 'جمادى الآخرة' | 'جمادى الثانية'
    | 'رجب'
    | 'شعبان'
    | 'رمضان'
    | 'شوال'
    | 'ذو القعدة'
    | 'ذو الحجة'
    ;


AMPM	: 'ص' | 'صباحاً' | 'صباحا'
    	| 'م' | 'مساءً' | 'مساءا'
    	;

ZONE_PREFIX		:  'بتوقيت';
CALENDAR_PREFIX	:  'بالتقويم';

DURATION_PREFIX	: 'مدة';
PERIOD_PREFIX	: 'فترة';

AND				: 'و';

HOUR			: 'ساعة' | 'ساعات';
MINUTE			: 'دقيقة' | 'دقائق';
SECOND			: 'ثانية' | 'ثواني';
NANOSECOND		: 'نانوثانية' | 'نانوثواني';

DAY				: 'يوم' | 'أيام';
MONTH			: 'شهر' | 'أشهر';
YEAR			: 'سنة' | 'سنوات';

NOW				: 'الآن';

DATE			: 'تاريخ';
TIME			: 'وقت';
DATE_TIME		: 'تاريخ و وقت';

COLON      		: ':';
DOT       		: '.';

ARABIC_WORDS	: ARABIC_WORD (ARABIC_WORD)*;
ARABIC_WORD 	: [\u0621-\u064A]+ ;
OFFSET 			: [+\-] DIGIT DIGIT COLON DIGIT DIGIT ;
NUMBER 			: DIGIT+ ;
fragment DIGIT 	: [٠-٩0-9];

WS : [ \t\r\n]+ -> skip ;
