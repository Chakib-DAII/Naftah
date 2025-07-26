/**
 * @author Chakib Daii
 *
 * The Naftah grammar is based on multiple programming languages tokens and syntaxes
 * that I learned during my programming journey.
 *
 * قواعد نفطه، تعتمد على رموز وقواعد لغوية متعددة للغات البرمجة تعلمتها خلال رحلتي مع البرمجة
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
IF         : 'إذا' | 'إذا_كان' | 'اذا' | 'اذا_كان';
ELSEIF     : 'غير_ذلك_إذا' | 'غير_ذلك_إذا_كان' | 'غير_ذلك_اذا_كان' | 'غير_ذلك_اذا';
ELSE       : 'غير_ذلك';
END        : 'أنهي' | 'انهي' | 'نهاية';
FOR        : 'كرر_حلقة';
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

// Operators
PLUS       : '+' | 'زائد';
INCREMENT  : '++' | 'زد';
MINUS      : '-' | 'ناقص';
DECREMENT  : '--' | 'نقص';
MUL        : '*' | 'ضارب';
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
LPAREN     : '(';
RPAREN     : ')';
LBRACE     : '{';
RBRACE     : '}';
LBRACK     : '[';
RBRACK     : ']';
SEMI       : '؛';
COLON      : ':';
DOT        : '.';
COMMA      : ',' | '٫' | '،' | '٬';

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
NUMBER     : [٠-٩0-9]+ (COMMA [٠-٩0-9]+)?;
CHARACTER  : (QuotationMark | DoubleQuotationMark | DoubleQuotationMarkLeft) Character (QuotationMark | DoubleQuotationMark | DoubleQuotationMarkRight);
STRING     : (DoubleQuotationMark | DoubleQuotationMarkLeft) String (DoubleQuotationMark | DoubleQuotationMarkRight);
ID         : [أ-يڠ-ۿ٠-٩_0-9]* [ء-يڠ-ۿ_] [ء-يڠ-ۿ٠-٩_0-9]*;

QuotationMark : '\'';
DoubleQuotationMark : '"';
DoubleQuotationMarkLeft : '«';
DoubleQuotationMarkRight : '»';

fragment Character: (~["«»\r\n]);
fragment String: Character* ESC* EMOJI* PUNCTUATION* .*?;
fragment ESC: '\\' ["\\n];

fragment EMOJI : '\uD83C' [\uDF00-\uDF5F] | '\uD83D' [\uDE00-\uDE4F] | '\u2600'..'\u26FF' | '\u2700'..'\u27BF';

PUNCTUATION : [،.؟:!-];

// Whitespace and comments
WS         : [ \t\r\n]+ -> skip;
LINE_COMMENT : '--' ~[\r\n]* -> skip;
BLOCK_COMMENT : '--*' .*? '*--' -> skip;
