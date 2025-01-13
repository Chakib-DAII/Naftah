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
statement: assignment
         | functionDeclaration
         | functionCall
         | ifStatement
         | returnStatement
         | block;

// Assignment: variable or constant assignment
assignment: (VARIABLE | CONSTANT) ID ASSIGN expression;

// Function declaration: Can have arguments and return values
functionDeclaration: FUNCTION functionCall block;

// Function call: Can have arguments and return values
functionCall: ID LPAREN argumentList? RPAREN;

// Argument list: Expressions separated by commas
argumentList: expression (COMMA expression)*;

// If statement: An 'if' block followed by an optional 'else' block
ifStatement: IF expression THEN block (ELSEIF expression THEN block)* (ELSE block)? END;

// Return statement: 'return' followed by an optional expression
returnStatement: RETURN expression?;

// Block: A block of statements enclosed in curly braces
block: LBRACE statement+ RBRACE;

// Expressions: Can be numbers, strings, variables, binary operations
expression: LPAREN expression RPAREN
          | expression MUL expression
          | expression DIV expression
          | expression MOD expression
          | expression PLUS expression
          | expression MINUS expression
          | expression LT expression
          | expression GT expression
          | expression LE expression
          | expression GE expression
          | expression EQ expression
          | expression NEQ expression
          | NUMBER
          | STRING
          | ID;