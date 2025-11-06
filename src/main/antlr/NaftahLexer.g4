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

options {
//    superClass = AbstractLexer;
}

@header {

}

@members {

}

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
OF         : 'من بين' | 'حسب' | 'لـ' | 'وفقا لـ';
TRUE       : 'صحيح' | 'صائب' | 'حقيقي';
FALSE      : 'خطأ' | 'خاطئ' | 'زائف';
TRY        : 'حاول';
OK         : 'نجاح';
ERROR      : 'فشل';
SOME       : 'بعض';
NONE	   : 'معدوم';

// Operators
PLUS       : '+' | 'زائد';
INCREMENT  : '++' | 'زد';
MINUS      : '-' | 'ناقص';
DECREMENT  : '--' | 'نقص';
MUL        : '*' | 'ضارب';
POW        : MUL MUL | 'رفع';
DIV        : '/' | 'قسمة';
MOD        : '%' | 'باقي';
ASSIGN     : '=' | 'تعيين';
LT         : '>' | 'أصغر_من';
GT         : '<' | 'أكبر_من';
LE         : '=>' | 'أصغر_أو_يساوي';
GE         : '>=' | 'أكبر_أو_يساوي';
EQ         : '==' | 'يساوي';
NEQ        : '=!' | 'لا_يساوي';
BITWISE_NOT : '~' | 'بت_ليس';
BITWISE_AND : '&' | 'بت_و' | 'بت_ايضا' | 'بت_أيضا';
BITWISE_OR  : '|' | 'بت_أو' | 'بت_او' | 'بت_وإلا' | 'بت_والا' | 'بت_ولا';
BITWISE_XOR : '^' | 'بت_أو_حصري' | 'بت_او_حصري' | 'بت_وإلا_حصري' | 'بت_والا_حصري' | 'بت_ولا_حصري';
ELEMENTWISE_PLUS : DOT PLUS DOT;
ELEMENTWISE_MINUS : DOT MINUS DOT;
ELEMENTWISE_MUL : DOT MUL DOT;
ELEMENTWISE_DIV : DOT DIV DOT;
ELEMENTWISE_MOD : DOT MOD DOT;


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


// Types
VAR              : 'أي_نوع' | 'أي_نمط';

BOOLEAN : 'بوليان' | 'منطقي';
STRING_TYPE : 'تسلسل_أحرف' | 'تسلسل_حروف' | 'تسلسل_رموز';
CHAR    : 'حرف' | 'رمز';
BYTE    : 'عدد_قصير_جدا' | 'قصير_جدا';
SHORT   : 'عدد_قصير' | 'قصير';
INT     : 'عدد_صحيح' | 'عدد';
LONG    : 'عدد_طويل' | 'طويل';
FLOAT   : 'عدد_عائم' | 'عائم';
DOUBLE  : 'عدد_عائم_طويل' | 'عائم_طويل';

// Literals
BASE_DIGITS	: (Digit | Character)+ QuotationMark QuotationMark;  // Digits for base up to 36 (including a-z for bases > 10)
BASE_RADIX	: ([2-9٢-٩] | ([1-3١-٣] [0-9٠-٩])) QuotationMark; // base 2 to 36 radices
NUMBER      : Digit+ (COMMA Digit+)?;
NAN			: 'ليس_رقم' | 'قيمة_غير_رقمية' | 'رقم_غير_صالح' | 'غير_عددي' | 'ليس_عددي';
CHARACTER   : QuotationMark Character QuotationMark;
RAW		    : 'خام' | 'نص_خام'| 'سلسلة_خام';
BYTE_ARRAY	: 'ثمانية_بت' | 'بايتات' |'سلسلة_ثمانية_بت' |'مصفوفة_ثمانية_بت' | 'سلسلة_بايتات'| 'مصفوفة_بايتات';
STRING      : DoubleQuotationMark String DoubleQuotationMark
			| DoubleQuotationMarkLeft String DoubleQuotationMarkRight;
ID          : [ء-يڠ-ۿﹼپ_٠-٩0-9\u064B-\u065F]* [ء-يڠ-ۿﹼپ_\u064B-\u065F] [ء-يڠ-ۿﹼپ_٠-٩0-9\u064B-\u065F]*;

QuotationMark : '\'' | '’';
DoubleQuotationMark : '"';
DoubleQuotationMarkLeft : '«';
DoubleQuotationMarkRight : '»';

fragment Digit: [٠-٩0-9];
fragment Character: (~["«»\r\n]);
fragment String: Character* ESC* EMOJI* PUNCTUATION* .*?;
fragment ESC: '\\' ["\\n];

fragment EMOJI : '\uD83C' [\uDF00-\uDF5F] | '\uD83D' [\uDE00-\uDE4F] | '\u2600'..'\u26FF' | '\u2700'..'\u27BF';

PUNCTUATION : [،.؟:!-];

// Whitespace and comments
WS         : [ \t\r\n]+ -> skip;
LINE_COMMENT : '---' ~[\r\n]* -> skip;
BLOCK_COMMENT : '---*' .*? '*---' -> skip;
