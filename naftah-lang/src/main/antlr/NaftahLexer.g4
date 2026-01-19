// SPDX-License-Identifier: Apache-2.0
// Copyright © The Naftah Project Authors

/**
 *
 * The Naftah grammar is based on multiple programming languages tokens and syntaxes
 * that I learned during my programming journey.
 *
 * قواعد نفطه، تعتمد على رموز وقواعد لغوية متعددة للغات البرمجة تعلمتها خلال رحلتي مع البرمجة
 *
 * @author Chakib Daii
 *
 */

lexer grammar NaftahLexer;

// Keywords
AND        : 'و' | 'ايضا' | 'أيضا';
OR         : 'أو' | 'او' | 'وإلا' | 'والا' | 'ولا';
BREAK      : 'كسر' | 'اكسر' | 'إكسر';
CONTINUE   : 'تابع';
DO         : 'إفعل' | 'افعل';
TO         : 'إلى';
DOWNTO     : 'تنازليا';
STEP       : 'خطوة';
IF         : 'إذا' | 'إذا_كان' | 'اذا' | 'اذا_كان';
ELSEIF     : 'غير_ذلك_إذا' | 'غير_ذلك_إذا_كان' | 'غير_ذلك_اذا_كان' | 'غير_ذلك_اذا';
ELSE       : 'غير_ذلك';
END        : 'أنهي' | 'انهي' | 'نهاية';
FOR        : 'كرر_حلقة';
IN         : 'من' | 'ضمن' | 'من_بين' | 'داخل';
FUNCTION   : 'دالة';
VARIABLE   : 'متغير';
CONSTANT   : 'ثابت';
VOID       : 'عدم' | 'فراغ' | 'بطلان' | 'خلاء';
NULL       : 'فارغ' | 'باطل' | 'لاشيء';
NOT        : 'ليس';
REPEAT     : 'كرر';
RETURN     : 'ارجع' | 'أرجع' |'اعد' | 'أعد';
THEN       : 'إذن' | 'اذن';
UNTIL      : 'حتى';
WHILE      : 'بينما';
CASE       : 'اختر';
OF         : 'من بين' | 'حسب' | 'لـ' | 'وفقا_لـ';
TRUE       : 'صحيح' | 'صائب' | 'حقيقي';
FALSE      : 'خطأ' | 'خاطئ' | 'زائف';
TRY        : 'حاول';
OK         : 'نجاح';
ERROR      : 'فشل';
SOME       : 'بعض';
NONE	   : 'معدوم';
IMPORT     : 'استيراد' |  'جلب' | 'إجلب';
AS         : 'تحت_إسم' | 'مثل';

// Implementations
IMPLEMENTATION	: 'سلوك';
SELF       		: 'ذات' | 'هذا';

// Operators
PLUS       			: '+' | 'زائد';
INCREMENT  			: '++' | 'زد';
MINUS      			: '-' | 'ناقص';
DECREMENT  			: '--' | 'نقص';
MUL        			: STAR_SIGN | 'ضارب';
POW        			: STAR_SIGN STAR_SIGN | 'رفع';
DIV        			: '/' | 'قسمة';
MOD        			: '%' | 'باقي';
ASSIGN     			: '=' | 'تعيين';
LT         			: LT_SIGN | 'أصغر_من';
GT         			: GT_SIGN | 'أكبر_من';
LE         			: '=>' | 'أصغر_أو_يساوي';
GE         			: '>=' | 'أكبر_أو_يساوي';
EQ         			: '==' | 'يساوي';
NEQ        			: '=!' | 'لا_يساوي';
BITWISE_NOT 		: '~' | 'بت_ليس';
BITWISE_AND 		: '&' | 'بت_و' | 'بت_ايضا' | 'بت_أيضا';
BITWISE_OR  		: '|' | 'بت_أو' | 'بت_او' | 'بت_وإلا' | 'بت_والا' | 'بت_ولا';
BITWISE_XOR 		: '^' | 'بت_أو_حصري' | 'بت_او_حصري' | 'بت_وإلا_حصري' | 'بت_والا_حصري' | 'بت_ولا_حصري';
BITWISE_USHR 		: '>>>' | 'إزاحة_يمين_غير_موقعة';
BITWISE_SHR  		: '>>' | 'إزاحة_يمين';
BITWISE_SHL  		: '<<' | 'إزاحة_يسار';
ELEMENTWISE_PLUS	: DOT PLUS DOT;
ELEMENTWISE_MINUS 	: DOT MINUS DOT;
ELEMENTWISE_MUL 	: DOT MUL DOT;
ELEMENTWISE_DIV 	: DOT DIV DOT;
ELEMENTWISE_MOD 	: DOT MOD DOT;
INSTANCE_OF			: 'مثيل_من';
TYPE_OF				: 'نوع';
SIZE_OF				: 'حجم';

// Concurrency
ASYNC	:      'غير_متزامن';
SPAWN	:      'تشغيل';
AWAIT	:      'انتظار';
SCOPE	:      'نطاق';
CHANNEL	:    'قناة';
ACTOR	:      'ممثل';

// Special characters
LPAREN      : '(';
RPAREN      : ')';
LBRACE      : '{';
RBRACE      : '}';
LBRACK      : '[';
RBRACK      : ']';
SEMI        : '؛';
COLON       : ':';
DOT         : '.';
COMMA       : ',' | '٫' | '،' | '٬';
QUESTION    : '؟';
ARROW       : '->';
ORDERED     : 'مرتب';
HASH_SIGN   : '#';
AT_SIGN     : '@';
DOLLAR_SIGN : '$';
STAR_SIGN   : '*';
LT_SIGN     : '>';
GT_SIGN     : '<';


// Types
VAR				: 'أي_نوع' | 'أي_نمط';

// simple types
BOOLEAN 		: 'بوليان' | 'منطقي';
STRING_TYPE 	: 'تسلسل_أحرف' | 'تسلسل_حروف' | 'تسلسل_رموز';
CHAR    		: 'حرف' | 'رمز';
BYTE    		: 'عدد_قصير_جدا' | 'قصير_جدا';
SHORT   		: 'عدد_قصير' | 'قصير';
INT     		: 'عدد_صحيح' | 'عدد';
LONG    		: 'عدد_طويل' | 'طويل';
BIG_INT    		: 'عدد_طويل_جدا' | 'طويل_جدا';
FLOAT   		: 'عدد_عائم' | 'عائم';
DOUBLE  		: 'عدد_عائم_طويل' | 'عائم_طويل';
BIG_DECIMAL  	: 'عدد_عائم_طويل_جدا' | 'عائم_طويل_جدا';
VAR_NUMBER  	: 'أي_عدد';

// complex types
STRUCT			: 'كائن' | 'هيكل';
PAIR			: 'زوج' | 'مفتاح_و_قيمة' | 'زوج_مفتاح_و_قيمة';
TRIPLE			: 'ثلاثي' | 'ثلاثي_القيم';
LIST			: 'قائمة';
TUPLE			: 'تركيبة';
SET				: 'مجموعة';
MAP				: 'مصفوفة_ترابطية';
DURATION		: 'مدة';
PERIOD			: 'فترة';
PERIOD_DURATION	: 'فترة_و_مدة';
DATE			: 'تاريخ';
TIME			: 'وقت';
DATE_TIME		: 'تاريخ_و_وقت';
LT_TYPE_SIGN    : COLON LT_SIGN;
GT_TYPE_SIGN    : GT_SIGN COLON;

// Literals
BASE_DIGITS		: QuotationMark (Digit | Character)+ QuotationMark QuotationMark;  // Digits for base up to 36 (including a-z for bases > 10)
BASE_RADIX		: ([2-9٢-٩] | ([1-3١-٣] [0-9٠-٩])) QuotationMark; // base 2 to 36 radices
NUMBER      	: Digit+ (COMMA Digit+)?;
NAN				: 'ليس_رقم' | 'قيمة_غير_رقمية' | 'رقم_غير_صالح' | 'غير_عددي' | 'ليس_عددي';
CHARACTER   	: QuotationMark Character QuotationMark;
RAW		    	: 'خام' | 'نص_خام'| 'سلسلة_خام';
TEMPORAL_POINT	: 'زمن' | 'نقطة_زمنية';
TEMPORAL_AMOUNT	: 'مقدار_زمني' | 'قيمة_زمنية';
BYTE_ARRAY		: 'ثمانية_بت' | 'بايتات' |'سلسلة_ثمانية_بت' |'مصفوفة_ثمانية_بت' | 'سلسلة_بايتات'| 'مصفوفة_بايتات';
STRING      	: DoubleQuotationMark String DoubleQuotationMark
				| DoubleQuotationMarkLeft String DoubleQuotationMarkRight;
ID          	: [ء-يڠ-ۿﹼپ_٠-٩0-9\u064B-\u065F]* [ء-يڠ-ۿﹼپ_\u064B-\u065F] [ء-يڠ-ۿﹼپ_٠-٩0-9\u064B-\u065F]*;

QuotationMark 				: '\'' | '’';
DoubleQuotationMark 		: '"';
DoubleQuotationMarkLeft 	: '«';
DoubleQuotationMarkRight	: '»';

fragment Digit		: [٠-٩0-9];
fragment Character	: (~["«»\r\n]);
fragment String		: Character* ESC* EMOJI* PUNCTUATION* .*?;
fragment ESC		: '\\' ["\\n];
fragment EMOJI 		: '\uD83C' [\uDF00-\uDF5F] | '\uD83D' [\uDE00-\uDE4F] | '\u2600'..'\u26FF' | '\u2700'..'\u27BF';

PUNCTUATION 	: [،.؟:!-];

// Whitespace and comments
WS         		: [ \t\r\n]+ -> skip;
LINE_COMMENT 	: '---' ~[\r\n]* -> skip;
BLOCK_COMMENT	: '---*' .*? '*---' -> skip;
