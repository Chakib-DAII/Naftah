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

parser grammar NaftahParser;

options {
    tokenVocab = NaftahLexer;
//    contextSuperClass = NaftahParserRuleContext;
//    superClass = AbstractParser;
}

@header {
}

@members {

}

// Top-level rule: A Naftah program consists of statements
program: statement+;

// Statement: Can be an assignment, function call, or control flow
statement: block END? #blockStatement
         | ifStatement END? #ifStatementStatement
         | forStatement END? #forStatementStatement
         | whileStatement END? #whileStatementStatement
         | repeatStatement END? #repeatStatementStatement
         | caseStatement END? #caseStatementStatement
         | tryStatement END? #tryStatementStatement
         | functionDeclaration END? #functionDeclarationStatement
         | functionCall END? #functionCallStatement
         | objectAccess END? #objectAccessStatement
         | collectionAccess END? #collectionAccessStatement
         | declaration END? #declarationStatement
         | assignment END? #assignmentStatement
         | returnStatement END? #returnStatementStatement
         | breakStatement END? #breakStatementStatement
         | continueStatement END? #continueStatementStatement
         | expression END? #expressionStatement
         ;

// Declaration: variable or constant declaration
declaration: (VARIABLE | CONSTANT) ID (COLON type)?;

// Assignment: variable or constant assignment, object field or collection element
assignment: (declaration | ID | qualifiedName | qualifiedObjectAccess | collectionAccess) ASSIGN expression;

// Function declaration: Can have parameters and return values
functionDeclaration: FUNCTION ID LPAREN parameterDeclarationList? RPAREN (COLON returnType)? block;

// Function declaration parameter list: parameterDeclarations separated by commas or semicolons
parameterDeclarationList: parameterDeclaration ((COMMA | SEMI) parameterDeclaration)*;

// Parameter declaration : parameter id with optional type and assignment
parameterDeclaration: CONSTANT? ID (COLON type)? (ASSIGN value)?;

// Function call: Can have arguments and return values
functionCall: (ID | qualifiedCall) LPAREN argumentList? RPAREN;

// constructor call: Can have arguments and return the created object
initCall: qualifiedName LPAREN argumentList? RPAREN;

// Argument list: Expressions separated by commas or semicolons
argumentList: (ID ASSIGN)? expression ((COMMA | SEMI) (ID ASSIGN)? expression)*;

// If statement: An 'if' block followed by an optional 'else' block
ifStatement: IF expression THEN block (ELSEIF expression THEN block)* (ELSE block)?;

// A 'for' loop: iterates from a starting value to an end value (ascending or descending)
forStatement:
    label? FOR
    ID ASSIGN expression					// Initialization (e.g., i := 1)
    (TO | DOWNTO) expression				// Direction of loop (e.g., TO 10 or DOWNTO 1)
    (STEP expression)?						// Loop step
    DO block								// Loop body
    (ELSE block)?							// Optional 'else' block if no break occurred
    #indexBasedForLoopStatement
    |
    label? FOR
    foreachTarget IN expression				// Loop elements initialization
    DO block								// Loop body
    (ELSE block)?							// Optional 'else' block if no break occurred
	#forEachLoopStatement
    ;

// 'foreach' loop target definition
foreachTarget
	: ID #valueForeachTarget												// value only
	| ID COLON ID #keyValueForeachTarget									// key : value
    | ID (COMMA | SEMI) ID #indexAndValueForeachTarget						// index, value
    | ID (COMMA | SEMI) ID COLON ID #indexAndKeyValueForeachTarget			// index, key : value
    ;

// A 'while' loop: repeats as long as the condition is true
whileStatement:
    label?
    WHILE expression                               // Loop condition
    DO block;                                      // Loop body

// A 'repeat-until' loop: executes the block at least once, then repeats until the condition is true
repeatStatement:
    label?
    REPEAT block                                   // Loop body (executed at least once)
    UNTIL expression;                              // Exit condition (loop stops when true)

// A 'case'/'match' statement: selects one of many possible blocks to execute based on the expression
caseStatement:
    CASE expression                                // The controlling expression
    OF
        (caseLabelList COLON block)+          // One or more labeled cases (e.g., 1: ..., 2,3: ...)
        (ELSE block)?;                         // Optional default case if no labels match

// A list of labels for a 'case' option (e.g., 1, 2, 3)
caseLabelList: expression ((COMMA | SEMI) expression)*;                // One or more comma or semicolon separated expressions

tryStatement: TRY LPAREN expression RPAREN LBRACE tryCases RBRACE #tryStatementWithTryCases
  | TRY LPAREN expression RPAREN LBRACE optionCases RBRACE #tryStatementWithOptionCases
  ;

tryCases
  : okCase errorCase?
  | errorCase okCase?
  ;

okCase: OK LPAREN ID RPAREN (DO | ARROW) (block | expression);

errorCase: ERROR LPAREN ID RPAREN (DO | ARROW) (block | expression);

optionCases
  : someCase noneCase?
  | noneCase someCase?
  ;

someCase: SOME LPAREN ID RPAREN (DO | ARROW) (block | expression);

noneCase: NONE (DO | ARROW) (block | expression);

// Break statement: used in loops to break the loop with optional label
breakStatement: BREAK ID?;

// Continue statement: used in loops to skip the current iteration with optional label
continueStatement: CONTINUE ID?;

// Return statement: 'return' followed by an optional expression
returnStatement: RETURN expression?;

// Block: A block of statements enclosed in curly braces
block: LBRACE statement* RBRACE;

// Expressions: Can be value, binary operations
expression: ternaryExpression;

ternaryExpression: nullishExpression (QUESTION expression COLON ternaryExpression)?;

nullishExpression: logicalExpression (QUESTION QUESTION logicalExpression)*;

logicalExpression: bitwiseExpression ((AND | OR) bitwiseExpression)*;

bitwiseExpression: equalityExpression ((BITWISE_AND | BITWISE_OR | BITWISE_XOR) equalityExpression)*;

equalityExpression: relationalExpression ((EQ | NEQ) relationalExpression)*;

relationalExpression: additiveExpression ((LT | LE | GT | GE) additiveExpression)*;

additiveExpression: multiplicativeExpression ((PLUS | MINUS | ELEMENTWISE_PLUS | ELEMENTWISE_MINUS) multiplicativeExpression)*;

multiplicativeExpression: powerExpression ((MUL | DIV | MOD | ELEMENTWISE_MUL | ELEMENTWISE_DIV | ELEMENTWISE_MOD) powerExpression)*;

powerExpression: unaryExpression (POW powerExpression)?;

unaryExpression: (PLUS | MINUS | NOT | BITWISE_NOT | INCREMENT | DECREMENT) unaryExpression #prefixUnaryExpression
    		   | postfixExpression #postfixUnaryExpression
     		   ;

postfixExpression: primary (INCREMENT | DECREMENT)?;

primary: functionCall #functionCallExpression
       | object #objectExpression
       | collection #collectionExpression
       | objectAccess #objectAccessExpression
       | collectionAccess #collectionAccessExpression
       | value #valueExpression
       | LPAREN expression RPAREN #parenthesisExpression
       ;

// Object
object: LBRACE objectFields? RBRACE;
objectFields: assignment ((COMMA | SEMI) assignment)*;

objectAccess: qualifiedName
		    | qualifiedObjectAccess;

// Collections:  can be a list, tuple, set, map
collection: LBRACK elements? RBRACK #listValue
          | LPAREN elements? RPAREN #tupleValue
          | ORDERED? LBRACE elements? RBRACE #setValue
          | ORDERED? LBRACE keyValuePairs? RBRACE #mapValue;

// single value elements
elements: expression (COMMA | SEMI) #singleElement
        | expression ((COMMA | SEMI) expression)+ (COMMA | SEMI)? #multipleElements;


// key=value value elements
keyValuePairs: keyValue ((COMMA | SEMI) keyValue)* (COMMA | SEMI)?;
keyValue: expression COLON expression;

// Value: Can be numbers, strings, ID
value: NUMBER #numberValue
     | BASE_DIGITS BASE_RADIX #radixNumberValue
     | TRUE #trueValue
     | FALSE #falseValue
     | NULL #nullValue
     | CHARACTER #characterValue
     | (RAW | BYTE_ARRAY)? STRING #stringValue
     | NAN #nanValue
     | ID #idValue
     ;

// Return type: Can be void, any, builtinType or qualifiedName
returnType: VOID #voidReturnType
          | type #typeReturnType
          ;

// Type: Can be any, builtinType or qualifiedName
type: VAR #varType
    | builtIn #builtInType
    | qualifiedName #qualifiedNameType
    ;

builtIn: BOOLEAN
    |   CHAR
    |   BYTE
    |   SHORT
    |   INT
    |   LONG
    |   FLOAT
    |   DOUBLE
    |   STRING_TYPE
    ;

// QualifiedName: ID separated by COLONs
qualifiedName: ID (QUESTION? COLON ID)+;

qualifiedCall: qualifiedName COLON COLON ID;

qualifiedObjectAccess: ID (QUESTION? propertyAccess)+;

//qualifiedObjectAccess: ID (QUESTION? ((COLON ID) | (LBRACK ID RBRACK) | collectionAccess))+;

collectionAccess: ID (QUESTION? LBRACK collectionAccessIndex RBRACK)+;

propertyAccess
    : COLON ID
    | LBRACK (DoubleQuotationMark | QuotationMark) ID (DoubleQuotationMark | QuotationMark) RBRACK
    ;

collectionAccessIndex
    : NUMBER
    | ID
    ;

// A label is an identifier followed by a colon for loops
label: ID COLON;