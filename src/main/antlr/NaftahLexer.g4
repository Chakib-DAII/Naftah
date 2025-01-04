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
MINUS      : '-' | 'ناقص';
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

// Whitespace and comments
WS         : [ \t\r\n]+ -> skip;
LINE_COMMENT : '--' ~[\r\n]* -> skip;
BLOCK_COMMENT : '--*' .*? '*--' -> skip;

fragment DoubleQuotationMark : '"';
fragment DoubleQuotationMarkLeft : '«';
fragment DoubleQuotationMarkRight : '»';