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
    :   LCURLY templateContent* (RCURLY | plainElseBlock)
    ;

plainElseBlock
    :   ELSE_CALL templateContent* RCURLY
    ;

comment
    :   COMMENT
    ;

importDeclaration
    :   AT_VALUE importStatement
    ;

importStatement
    :   MV_IMPORT
    ;

optionDeclaration
    :   AT_VALUE optionStatement
    ;

optionStatement
    :   MV_OPTION
    ;

argumentsDeclaration
    :   AT_VALUE argumentsStatement
    ;

argumentsStatement
    :   MV_ARGS
    ;

templateContent
    :   (comment | block | plain | contentClosure | valueClosure | value)
    ;

block
    :   (ifBlock | forBlock | withBlock)
    ;

ifBlock
    :   AT_VALUE MV_IF templateContent* (RCURLY | elseBlock)
    ;

elseBlock
    :   ELSE_CALL templateContent* RCURLY
    ;

forBlock
    :   AT_VALUE MV_FOR templateContent* RCURLY
    ;

withBlock
    :   AT_VALUE MV_WITH templateContent* RCURLY
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
