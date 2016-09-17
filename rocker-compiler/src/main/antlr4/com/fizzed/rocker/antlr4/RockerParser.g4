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
    :   AT importStatement
    ;

importStatement
    :   MV_IMPORT
    ;

optionDeclaration
    :   AT optionStatement
    ;

optionStatement
    :   MV_OPTION
    ;

argumentsDeclaration
    :   AT argumentsStatement
    ;

argumentsStatement
    :   MV_ARGS
    ;

templateContent
    :   (comment | block | plain | contentClosure | valueClosure | value | elvis)
    ;

block
    :   (ifBlock | forBlock | withBlock)
    ;

ifBlock
    :   AT MV_IF templateContent* (RCURLY | elseBlock)
    ;

elseBlock
    :   ELSE_CALL templateContent* RCURLY
    ;

forBlock
    :   AT MV_FOR templateContent* RCURLY
    ;

withBlock
    :   AT MV_WITH templateContent* RCURLY
    ;

contentClosure
    :   AT contentClosureExpression templateContent* RCURLY
    ;

contentClosureExpression
    :   MV_CONTENT_CLOSURE
    ;

valueClosure
    :   AT valueClosureExpression templateContent* RCURLY
    ;

valueClosureExpression
    :   MV_VALUE_CLOSURE
    ;

value
    :   AT valueExpression
    ;

valueExpression
    :   MV_VALUE
    ;

elvis
    :   AT MV_ELVIS_OPEN elvisExpression ELVIS_CLOSE
    ;

elvisExpression
    :   ELVIS_LH_EXPR ELVIS_RH_EXPR?
    ;
