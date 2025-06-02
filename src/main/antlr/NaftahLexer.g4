/**
 * @author Chakib
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
DO         : 'إفعل' | 'افعل';
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
COMMA      : '،';

// Literals
ID         : [أ-ي٠-٩_0-9][أ-ي_]*;
NUMBER     : [٠-٩0-9]+ ((',' | '٫') [٠-٩0-9]+)?;
STRING     : (DoubleQuotationMark | DoubleQuotationMarkLeft) (~["\r\n])* (DoubleQuotationMark | DoubleQuotationMarkRight);

// Types
VAR              : 'أي_نوع' | 'أي_نمط';

fragment BOOLEAN : 'بوليان' | 'منطقي';
fragment CHAR    : 'حرف' | 'رمز';
fragment BYTE    : 'عدد_قصير_جدا' | 'قصير_جدا';
fragment SHORT   : 'عدد_قصير' | 'قصير';
fragment INT     : 'عدد_صحيح' | 'عدد';
fragment LONG    : 'عدد_طويل' | 'طويل';
fragment FLOAT   : 'عدد_عائم' | 'عائم';
fragment DOUBLE  : 'عدد_عائم_طويل' | 'عائم_طويل';

BuiltInType
    :   BOOLEAN
    |   CHAR
    |   BYTE
    |   SHORT
    |   INT
    |   LONG
    |   FLOAT
    |   DOUBLE
    ;

// Whitespace and comments
WS         : [ \t\r\n]+ -> skip;
LINE_COMMENT : '--' ~[\r\n]* -> skip;
BLOCK_COMMENT : '--*' .*? '*--' -> skip;

fragment DoubleQuotationMark : '"';
fragment DoubleQuotationMarkLeft : '«';
fragment DoubleQuotationMarkRight : '»';