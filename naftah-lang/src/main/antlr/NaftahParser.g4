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
}

// Top-level rule: A Naftah program consists of statements
program: (statement END?)+;

// Statement: Can be an assignment, function call, or control flow
statement: scopeBlock #scopeBlockStatement
		 | block #blockStatement
         | importStatement #importStatementStatement
         | ifStatement #ifStatementStatement
         | forStatement #forStatementStatement
         | whileStatement #whileStatementStatement
         | repeatStatement #repeatStatementStatement
         | caseStatement #caseStatementStatement
         | tryStatement #tryStatementStatement
         | functionDeclaration #functionDeclarationStatement
         | implementationDeclaration #implementationDeclarationStatement
         | declaration #declarationStatement
         | channelDeclaration #channelDeclarationStatement
         | actorDeclaration #actorDeclarationStatement
         | assignment #assignmentStatement
         | returnStatement #returnStatementStatement
         | breakStatement #breakStatementStatement
         | continueStatement #continueStatementStatement
         | expression #expressionStatement
         ;

/**
 * Top-level import statement.
 * Supports:
 *  - Single element import
 *  - Import with specification (colon + list or single import)
 *  - Qualified callable import
 */
importStatement: IMPORT ID importAlias #importStatementAsAlias
			   | IMPORT qualifiedName ((COLON COLON? imports) | importAlias)? #groupedImportStatement
			   | IMPORT qualifiedCall importAlias? #qualifiedCallImportStatement;

imports: LBRACK importElements RBRACK
	   | callableImportElement;

importElements: callableImportElement ((COMMA | SEMI) callableImportElement)+ (COMMA | SEMI)?;

callableImportElement: (ID | qualifiedName | qualifiedCall) importAlias?;

importAlias: AS ID;

// Declaration: variable or constant declaration
declaration: singleDeclaration | multipleDeclarations;

singleDeclaration: (VARIABLE | CONSTANT) ID (COLON type)?;

multipleDeclarations: (VARIABLE | CONSTANT) ID ((COMMA | SEMI) ID)+ (COLON type ((COMMA | SEMI) type)*)?;

// Assignment: variable or constant assignment, object field or collection element
assignment: singleAssignmentExpression | multipleAssignmentsExpression;

singleAssignmentExpression: (singleDeclaration | singleAssignment) ASSIGN expression;

multipleAssignmentsExpression: (multipleDeclarations | multipleAssignments) ASSIGN expression ((COMMA | SEMI) expression)*;

singleAssignment: ID | qualifiedName | qualifiedObjectAccess | collectionAccess;

multipleAssignments: singleAssignment ((COMMA | SEMI) singleAssignment)+;

// Function declaration: Can have parameters and return values
functionDeclaration: ASYNC? FUNCTION ID LPAREN parameterDeclarationList? RPAREN (COLON returnType)? block;

// Function declaration parameter list: parameterDeclarations separated by commas or semicolons
parameterDeclarationList: parameterDeclaration ((COMMA | SEMI) parameterDeclaration)*;

// Parameter declaration : parameter id with optional type and assignment
parameterDeclaration: CONSTANT? ID (COLON type)? (ASSIGN value)?;

/**
* Chained Function calls: Can have arguments and return values
* and the return value is piped to the next in case of chain
*/
functionCall: primaryCall callSegment*;

/**
* constructor call: Can have arguments and return the created object
* the object instance is piped to the next in case of chall chain
*/
initCall: ((AT_SIGN ID) | (AT_SIGN? qualifiedName)) targetExecutableIndex? LPAREN argumentList? RPAREN callSegment*;

/**
* Chained Function calls segment: Can have arguments and return values
* in case of ::: we reuse the previous call qualified name as the same qualified for the current
* and we don't in case of :: (where the qualified name can be provided for itself)
*/
callSegment: COLON COLON COLON? primaryCall;

// Function call: Can have arguments and return values
primaryCall: (selfOrId | qualifiedCall) targetExecutableIndex? LPAREN argumentList? RPAREN;

/**
 * Represents the index of the target executable (method or constructor)
 * in the case where multiple overloads exist for a given qualified call.
 *
 * <p>
 * If the qualified call maps to a single executable, this index is ignored.
 * Otherwise, it specifies which overloaded executable to invoke.
 * </p>
 */
targetExecutableIndex: COLON NUMBER;

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

errorCase: ERROR (LPAREN ID RPAREN)? (DO | ARROW) (block | expression);

optionCases
  : someCase noneCase?
  | noneCase someCase?
  ;

someCase: SOME LPAREN ID RPAREN (DO | ARROW) (block | expression);

noneCase: NONE (DO | ARROW) (block | expression);

// Concurrency Scope Block
scopeBlock: SCOPE ORDERED? block;

// Concurrency Channel / Actor
channelDeclaration: CHANNEL ID (COLON type)?;

actorDeclaration: ACTOR ID
	(LPAREN (
			 (ID (COLON type)? ((COMMA | SEMI) LPAREN objectFields RPAREN)?)
     		 | (LPAREN objectFields RPAREN)
     ) RPAREN)? block;

// Break statement: used in loops to break the loop with optional label
breakStatement: BREAK ID?;

// Continue statement: used in loops to skip the current iteration with optional label
continueStatement: CONTINUE ID?;

// Return statement: 'return' followed by an optional expression
returnStatement: singleReturn | multipleReturns;

singleReturn: RETURN expression?;

multipleReturns: RETURN ((LPAREN tupleElements? RPAREN) | collectionMultipleElements);

// Block: A block of statements enclosed in curly braces
block: LBRACE (statement END?)* RBRACE;

// Expressions: Can be value, binary operations... with optional Concurrency Spawn / Await
expression: ternaryExpression;

ternaryExpression: nullishExpression (QUESTION expression COLON ternaryExpression)?;

nullishExpression: logicalExpression (QUESTION QUESTION logicalExpression)*;

logicalExpression: bitwiseExpression ((AND | OR) bitwiseExpression)*;

bitwiseExpression: equalityExpression ((BITWISE_AND | BITWISE_OR | BITWISE_XOR) equalityExpression)*;

equalityExpression: relationalExpression ((EQ | NEQ) relationalExpression)*;

relationalExpression: shiftExpression ((LT | LE | GT | GE | INSTANCE_OF) shiftExpression)*;

shiftExpression: additiveExpression ((BITWISE_SHL | BITWISE_SHR | BITWISE_USHR) additiveExpression)*;

additiveExpression: multiplicativeExpression ((PLUS | MINUS | ELEMENTWISE_PLUS | ELEMENTWISE_MINUS) multiplicativeExpression)*;

multiplicativeExpression: powerExpression ((MUL | DIV | MOD | ELEMENTWISE_MUL | ELEMENTWISE_DIV | ELEMENTWISE_MOD) powerExpression)*;

powerExpression: unaryExpression (POW powerExpression)?;

unaryExpression: SPAWN (COLON type)? unaryExpression #spawnUnaryExpression
               | AWAIT unaryExpression #awaitUnaryExpression
			   | (PLUS | MINUS | NOT | BITWISE_NOT | INCREMENT | DECREMENT | TYPE_OF | SIZE_OF) unaryExpression #prefixUnaryExpression
    		   | postfixExpression #postfixUnaryExpression
     		   ;

postfixExpression: primary (INCREMENT | DECREMENT)?;

primary: initCall #initCallExpression
	   | functionCall #functionCallExpression
       | object #objectExpression
       | collection #collectionExpression
       | objectAccess #objectAccessExpression
       | collectionAccess #collectionAccessExpression
       | value #valueExpression
       | type #typeExpression
       | LPAREN expression RPAREN #parenthesisExpression
       ;

// Object
object: AT_SIGN LBRACE RBRACE #emptyObject
	  | AT_SIGN? LBRACE objectFields RBRACE #objectValue;

objectFields: assignment ((COMMA | SEMI) assignment)*;

objectAccess: qualifiedName
		    | qualifiedObjectAccess;

// Implementation
implementationDeclaration: IMPLEMENTATION ID LBRACE implementationFunctions RBRACE;

implementationFunctions: functionDeclaration (END? functionDeclaration)*;

// Collections:  can be a list, tuple, set, map
collection: LBRACK elements? RBRACK #listValue
          | LPAREN tupleElements? RPAREN #tupleValue
          | ORDERED? HASH_SIGN LBRACE RBRACE #emptySet
          | ORDERED? HASH_SIGN? LBRACE elements RBRACE #setValue
          | ORDERED? DOLLAR_SIGN LBRACE RBRACE #emptyMap
          | ORDERED? DOLLAR_SIGN? LBRACE keyValuePairs RBRACE #mapValue;

// single value elements
elements: expression (COMMA | SEMI)? #singleElement
        | collectionMultipleElements #multipleElements;

tupleElements: expression (COMMA | SEMI) #tupleSingleElement
        | collectionMultipleElements #tupleMultipleElements;

collectionMultipleElements: expression ((COMMA | SEMI) expression)+ (COMMA | SEMI)?;

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
     | (RAW | TEMPORAL_POINT | TEMPORAL_AMOUNT | BYTE_ARRAY)? STRING #stringValue
     | NAN #nanValue
     | ID #idValue
     ;

// Return type: Can be void, any, builtinType or qualifiedName
returnType: VOID #voidReturnType
          | type #typeReturnType
          ;

// Type: Can be any, builtinType or qualifiedName
type: complexBuiltIn #complexType
    | builtIn #builtInType
    | VAR #varType
    | (ID | qualifiedName) #qualifiedNameType
    ;

complexBuiltIn: STRUCT
	| PAIR GT_TYPE_SIGN type (COMMA | SEMI) type LT_TYPE_SIGN
	| TRIPLE GT_TYPE_SIGN type (COMMA | SEMI) type (COMMA | SEMI) type LT_TYPE_SIGN
	| LIST GT_TYPE_SIGN type LT_TYPE_SIGN
	| TUPLE
	| SET GT_TYPE_SIGN type LT_TYPE_SIGN
	| MAP GT_TYPE_SIGN type (COMMA | SEMI) type LT_TYPE_SIGN
    ;

builtIn: BOOLEAN
    |   CHAR
    |   BYTE
    |   SHORT
    |   INT
    |   LONG
    |   BIG_INT
    |   FLOAT
    |   DOUBLE
    |   BIG_DECIMAL
    |   VAR_NUMBER
    |   STRING_TYPE
    |   DURATION
    |   PERIOD
    |   PERIOD_DURATION
    |   DATE
    |   TIME
    |   DATE_TIME
    ;

// QualifiedName: ID separated by COLONs
qualifiedName: selfOrId (QUESTION? COLON ID)+;

qualifiedCall: selfOrId COLON COLON ID #simpleCall
			| qualifiedName COLON COLON ID #qualifiedNameCall;

qualifiedObjectAccess: selfOrId (QUESTION? propertyAccess)+;

//qualifiedObjectAccess: selfOrId (QUESTION? ((COLON ID) | (LBRACK ID RBRACK) | collectionAccess))+;

collectionAccess: selfOrId (QUESTION? LBRACK collectionAccessIndex RBRACK)+;

propertyAccess
    : COLON ID
    | LBRACK CHARACTER RBRACK
    | LBRACK STRING RBRACK
    ;

collectionAccessIndex
    : NUMBER
    | ID
    ;

// A label is an identifier followed by a colon for loops
label: ID COLON;

selfOrId: SELF | ID;