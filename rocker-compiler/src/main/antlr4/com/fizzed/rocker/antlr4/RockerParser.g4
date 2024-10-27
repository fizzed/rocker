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
    :   LCURLY templateContent* plainElseIfBlock* (RCURLY | plainElseBlock)
    ;

plainElseIfBlock
    :   ELSE_IF templateContent*
    ;

plainElseBlock
    :   ELSE templateContent* RCURLY
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
    :   (comment | block | plain | contentClosure | valueClosure | value | nullTernary | eval)
    ;

block
    :   (ifBlock | forBlock | withBlock|switchBlock)
    ;

ifBlock
    :   AT MV_IF templateContent* (RCURLY | ifElseIfBlock | ifElseBlock)
    ;

ifElseIfBlock
    :   ELSE_IF templateContent* (RCURLY | ifElseIfBlock | ifElseBlock)
    ;

ifElseBlock
    :   ELSE templateContent* RCURLY
    ;

forBlock
    :   AT MV_FOR templateContent* RCURLY
    ;

withBlock
    :   AT MV_WITH templateContent* (RCURLY | withElseBlock)
    ;

withElseBlock
    :   ELSE templateContent* RCURLY
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

nullTernary
    :   AT nullTernaryExpression
    ;

nullTernaryExpression
    :   MV_NULL_TERNARY_LH MV_NULL_TERNARY_RH
    ;

eval
    :   AT evalExpression
    ;

evalExpression
    :   MV_EVAL
    ;

switchBlock
    :   AT MV_SWITCH  (switchCase |switchDefault | templateContent)*  END_SWITCH_BLOCK
    ;

switchCase
    :   CASE templateContent*  RCURLY
    ;

switchDefault
    :   DEFAULT templateContent*  RCURLY
    ;


