/**
 * @author Chakib
 *
 * The Naftah grammar is based on multiple programming languages tokens and syntaxes
 * that I learned during my programming journey.
 *
 * قواعد نفطه، تعتمد على رموز وقواعد لغوية متعددة للغات البرمجة تعلمتها خلال رحلتي مع البرمجة
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
         | assignment #assignmentStatement
         | functionDeclaration #functionDeclarationStatement
         | functionCall #functionCallStatement
         | returnStatement #returnStatementStatement
         ;

// Assignment: variable or constant assignment
assignment: (VARIABLE | CONSTANT) ID (COLON type)? ASSIGN expression;

// Function declaration: Can have parameters and return values
functionDeclaration: FUNCTION ID LPAREN parameterDeclarationList? RPAREN (COLON returnType)? block;

// Function declaration parameter list: parameterDeclarations separated by commas
parameterDeclarationList: parameterDeclaration (COMMA parameterDeclaration)*;

// Parameter declaration : parameter id with optional type and assignment
parameterDeclaration: CONSTANT? ID (COLON type)? (ASSIGN value)?;

// Function call: Can have arguments and return values
// TODO: add support for all kind of functions using the qualifiedName
functionCall: ID LPAREN argumentList? RPAREN;

// Argument list: Expressions separated by commas
argumentList: (ID ASSIGN)? expression (COMMA (ID ASSIGN)? expression)*;

// If statement: An 'if' block followed by an optional 'else' block
ifStatement: IF expression THEN block (ELSEIF expression THEN block)* (ELSE block)? END;

// Return statement: 'return' followed by an optional expression
returnStatement: RETURN expression?;

// Block: A block of statements enclosed in curly braces
// TODO: add support to empty blocks
block: LBRACE statement+ RBRACE;

// Expressions: Can be value, binary operations
expression: LPAREN expression RPAREN #parenthesisExpression
          | expression MUL expression #mulExpression
          | expression DIV expression #divExpression
          | expression MOD expression #modExpression
          | expression PLUS expression #plusExpression
          | expression MINUS expression #minusExpression
          | expression LT expression #lessThanExpression
          | expression GT expression #greaterThanExpression
          | expression LE expression #lessThanEqualsExpression
          | expression GE expression #greaterThanEqualsExpression
          | expression EQ expression #equalsExpression
          | expression NEQ expression #notEqualsExpression
          | expression BITWISE_AND expression #bitwiseAndExpression
          | expression BITWISE_OR expression #bitwiseOrExpression
          | expression BITWISE_XOR expression #bitwiseXorExpression
          | NOT expression #notExpression
          | BITWISE_NOT expression #bitwiseNotExpression
          | INCREMENT expression #preIncrementExpression
          | expression INCREMENT #postIncrementExpression
          | DECREMENT expression #preDecrementExpression
          | expression DECREMENT #postDecrementExpression
          | value #valueExpression
          ;

// Value: Can be numbers, strings, ID
value: NUMBER #numberValue
     | STRING #stringValue
     | ID #idValue
     ;

// Return type: Can be void, any, builtinType or qualifiedName
returnType: VOID #voidReturnType
          | type #typeReturnType
          ;

// Type: Can be any, builtinType or qualifiedName
type: VAR #varType
    | BuiltInType #builtInType
    | qualifiedName #qualifiedNameType
    ;

// QualifiedName: ID separated by COLONs
qualifiedName: ID (COLON ID)*;