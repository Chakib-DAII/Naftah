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
statement: block #blockStatement
         | ifStatement #ifStatementStatement
         | forStatement #forStatementStatement
         | whileStatement #whileStatementStatement
         | repeatStatement #repeatStatementStatement
         | caseStatement #caseStatementStatement
         | functionDeclaration #functionDeclarationStatement
         | functionCall #functionCallStatement
         | qualifiedName #objectAccessStatement
         | declaration #declarationStatement
         | assignment #assignmentStatement
         | returnStatement #returnStatementStatement
         | breakStatement #breakStatementStatement
         | continueStatement #continueStatementStatement
         | expression #expressionStatement
         ;

// Declaration: variable or constant declaration
declaration: (VARIABLE | CONSTANT)? ID (COLON type)?;

// Assignment: variable or constant assignment
assignment: declaration ASSIGN expression;

// Function declaration: Can have parameters and return values
functionDeclaration: FUNCTION ID LPAREN parameterDeclarationList? RPAREN (COLON returnType)? block;

// Function declaration parameter list: parameterDeclarations separated by commas or semicolons
parameterDeclarationList: parameterDeclaration ((COMMA | SEMI) parameterDeclaration)*;

// Parameter declaration : parameter id with optional type and assignment
parameterDeclaration: CONSTANT? ID (COLON type)? (ASSIGN value)?;

// Function call: Can have arguments and return values
functionCall: (ID | qualifiedCall) LPAREN argumentList? RPAREN;

// Argument list: Expressions separated by commas or semicolons
argumentList: (ID ASSIGN)? expression ((COMMA | SEMI) (ID ASSIGN)? expression)*;

// If statement: An 'if' block followed by an optional 'else' block
ifStatement: IF expression THEN block (ELSEIF expression THEN block)* (ELSE block)? END?;

// A 'for' loop: iterates from a starting value to an end value (ascending or descending)
forStatement:
    label?
    FOR ID ASSIGN expression                      // Initialization (e.g., i := 1)
    (TO | DOWNTO) expression                      // Direction of loop (e.g., TO 10 or DOWNTO 1)
    DO block                                      // Loop body
    (ELSE block)?                                 // Optional 'else' block if no break occurred
    END?;                                          // Explicit loop end (if required in your syntax)

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
        (ELSE block)?                         // Optional default case if no labels match
    END?;

// A list of labels for a 'case' option (e.g., 1, 2, 3)
caseLabelList: elements;                // One or more comma or semicolon separated expressions

// Break statement: used in loops to break the loop with optional label
breakStatement: BREAK ID?;

// Continue statement: used in loops to skip the current iteration with optional label
continueStatement: CONTINUE ID?;

// Return statement: 'return' followed by an optional expression
returnStatement: RETURN expression?;

// Block: A block of statements enclosed in curly braces
block: LBRACE statement* RBRACE;

// Expressions: Can be value, binary operations
expression: functionCall #functionCallExpression
          | object #objectExpression
          | LPAREN expression RPAREN #parenthesisExpression
          | collection #collectionExpression
          | expression MUL expression #mulExpression
          | expression ELEMENTWISE_MUL expression #mulExpression
          | expression DIV expression #divExpression
          | expression ELEMENTWISE_DIV expression #divExpression
          | expression MOD expression #modExpression
          | expression ELEMENTWISE_MOD expression #modExpression
          | INCREMENT expression #preIncrementExpression
          | expression INCREMENT #postIncrementExpression
          | expression PLUS expression #plusExpression
          | expression ELEMENTWISE_PLUS expression #plusExpression
          | expression MINUS #negateExpression
          | DECREMENT expression #preDecrementExpression
          | expression DECREMENT #postDecrementExpression
          | expression MINUS expression #minusExpression
          | expression ELEMENTWISE_MINUS expression #minusExpression
          | expression LT expression #lessThanExpression
          | expression GT expression #greaterThanExpression
          | expression LE expression #lessThanEqualsExpression
          | expression GE expression #greaterThanEqualsExpression
          | expression EQ expression #equalsExpression
          | expression NEQ expression #notEqualsExpression
          | expression AND expression #andExpression
          | expression BITWISE_AND expression #bitwiseAndExpression
          | expression OR expression #orExpression
          | expression BITWISE_OR expression #bitwiseOrExpression
          | expression BITWISE_XOR expression #bitwiseXorExpression
          | NOT expression #notExpression
          | BITWISE_NOT expression #bitwiseNotExpression
          | qualifiedName #objectAccessExpression
          | value #valueExpression
          ;

// Object
object: LBRACE objectFields? RBRACE;
objectFields: assignment ((COMMA | SEMI) assignment )*;

// Collections:  can be a list, tuple, set, map
collection: LBRACK elements? RBRACK #listValue
          | LPAREN elements? RPAREN #tupleValue
          | LBRACE elements? RBRACE #setValue
          | LBRACE keyValuePairs? RBRACE #mapValue;

// single value elements
elements: expression ((COMMA | SEMI) expression )*;

// key=value value elements
keyValuePairs: keyValue ((COMMA | SEMI) keyValue )*;
keyValue: expression COLON expression;

// Value: Can be numbers, strings, ID
value: NUMBER #numberValue
     | BASE_DIGITS BASE_RADIX #radixNumberValue
     | TRUE #trueValue
     | FALSE #falseValue
     | NULL #nullValue
     | CHARACTER #characterValue
     | (RAW | BYTE_ARRAY)? STRING #stringValue
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
qualifiedName: ID (COLON ID)+;

qualifiedCall: qualifiedName COLON COLON ID;

// A label is an identifier followed by a colon for loops
label: ID COLON;