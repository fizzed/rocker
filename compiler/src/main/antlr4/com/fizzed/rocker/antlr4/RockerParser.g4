parser grammar RockerParser;

options {
    tokenVocab=RockerLexer;
}

template
    :  (plain | comment | importDeclaration | optionDeclaration)* argumentsDeclaration? templateContent*
    ;

// capture plain plus any escape sequences in sub-lexers

plain
    :   (PLAIN | plainBlock)
    ;

plainBlock
    :   LCURLY templateContent* RCURLY
    ;

comment
    :   COMMENT
    ;

importDeclaration
    :   AT_IMPORT importStatement ML_EOL
    ;

importStatement
    :   ML_PLAIN
    ;

optionDeclaration
    :   AT_OPTION optionStatement ML_EOL
    ;

optionStatement
    :   ML_PLAIN
    ;

argumentsDeclaration
    :   AT_ARGS argumentsStatement
    ;

argumentsStatement
    :   MA_ARGS
    ;

templateContent
    :   (comment | block | plain | contentClosure | valueClosure | value)
    ;

block
    :   (ifBlock | forBlock)
    ;

ifBlock
    :   AT_IF ifExpression MB_LCURLY templateContent* (RCURLY | elseBlock)
    ;

ifExpression
    :   MB_PARENTHESE
    ;

elseBlock
    :   ELSE_CALL templateContent* RCURLY
    ;

forBlock
    :   AT_FOR forStatement MB_LCURLY templateContent* RCURLY
    ;

forStatement
    :   MB_PARENTHESE
    ;

contentClosure
    :   AT_VALUE contentClosureExpression templateContent* RCURLY
    ;

contentClosureExpression
    :   MV_CONTENT_CLOSURE
    ;

valueClosure
    :   AT_VALUE valueClosureExpression templateContent* RCURLY
    ;

valueClosureExpression
    :   MV_VALUE_CLOSURE
    ;

value
    :   AT_VALUE valueExpression
    ;

valueExpression
    :  MV_VALUE
    ;
