lexer grammar RockerLexer;


// content mode (by default)

ELSE
    :   '}' Ws? 'else' Ws? '{'
    ;

LCURLY
    :   '{'
    ;

RCURLY
    :   '}'
    ;

COMMENT
    :   '@*' .*? '*@'
    ;

PLAIN
    :   ('@@' | '@}' | '@{' | ~('@' | '{' | '}'))+
    ;

AT
    :   '@'                                                         ->  pushMode(MV)
    ;

// magic "for an expression" mode
// @value[0]
// @value[0].getProperty("a")
// @value().getProperty(true)
// @value.getProperty(true)
// @value().getProperty(true).getAnotherProperty("hello")
// @value

mode MV;

MV_IMPORT
    :   'import' LineWs ~[\r\n]+ '\r'? '\n'                         -> popMode
    ;

MV_OPTION
    :   'option' LineWs ~[\r\n]+ '\r'? '\n'                         -> popMode
    ;

MV_ARGS
    :   'args' Ws? '(' ~(')')* ')'                                  -> popMode
    ;

MV_IF
    :   'if' Ws? Parentheses Ws? '{'                                -> popMode
    ;

MV_FOR
    :   'for' Ws? Parentheses Ws? '{'                               -> popMode
    ;

MV_WITH
    :   'with' '?'? Ws? Parentheses Ws? '{'                         -> popMode
    ;

MV_CONTENT_CLOSURE
    :   Identifier Ws? '=>' Ws? '{'                                 -> popMode
    ;

MV_VALUE_CLOSURE
    :   VariableExpression Ws? '->' Ws? '{'                         -> popMode
    ;

MV_EVAL_OPEN
    :   '?'? '('                                                    -> pushMode(EVAL_EXPR)
    ;

MV_VALUE 
    :   '?'? VariableExpression                                     -> popMode
    ;


mode EVAL_EXPR;

EVAL_RH_EXPR
    :   ':' Ws? ValueExpressions Ws? 
    ;

EVAL_LH_EXPR
    :   Ws? ValueExpressions Ws? 
    ;

EVAL_CLOSE
    :   ')'                                                         -> popMode, popMode
    ;

//
// fragments used everywhere else
//

fragment ValueExpressions
    :   ValueExpression (Ws? Op Ws? ValueExpression)*
    ;

// "joe" or variable expression
fragment ValueExpression
    :   JavaString | JavaLiteral | VariableExpression
    ;

fragment VariableExpression
    :   QualifiedName Parentheses? Arrays? ('.' Identifier Parentheses? Arrays?)*
    ;

fragment Op
    :   '+' | '-' | '*' | '/'
    ;

fragment Arrays
    :   '[' (Arrays | ~(']'))* ']'
    ;

fragment Parentheses
    :   '(' (Parentheses | ~(')'))* ')'                                 
    ;

fragment RerservedQualifiedNames
    :   ('if' | 'for')
    ;

fragment LineBreak
    :   ('\r'? '\n')
    ;

fragment Ws
    :   (' ' | '\t' | '\r'? '\n')+
    ;

fragment LineWs
    :   (' ' | '\t')+
    ;

fragment TypeArguments
    :   '<' TypeArgument (',' TypeArgument)* '>'
    ;

fragment TypeArgument
    :   Type
    |   '?' (('extends' | 'super') Type)?
    ;

fragment Type
    :   ClassOrInterfaceType ('[' ']')*
    ;

fragment ClassOrInterfaceType
    :   Identifier TypeArguments? ('.' Identifier TypeArguments? )*
    ;

fragment QualifiedName
    :   Identifier ('.' Identifier)*
    ;

fragment Identifier
    :   JavaLetter JavaLetterOrDigit*
    ;

fragment JavaLetter
    :   [a-zA-Z$_] // these are the "java letters" below 0xFF
    |   // covers all characters above 0xFF which are not a surrogate
        ~[\u0000-\u00FF\uD800-\uDBFF]
        {Character.isJavaIdentifierStart(_input.LA(-1))}?
    |   // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
        [\uD800-\uDBFF] [\uDC00-\uDFFF]
        {Character.isJavaIdentifierStart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
    ;

fragment JavaLetterOrDigit
    :   [a-zA-Z0-9$_] // these are the "java letters or digits" below 0xFF
    |   // covers all characters above 0xFF which are not a surrogate
        ~[\u0000-\u00FF\uD800-\uDBFF]
        {Character.isJavaIdentifierPart(_input.LA(-1))}?
    |   // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
        [\uD800-\uDBFF] [\uDC00-\uDFFF]
        {Character.isJavaIdentifierPart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
    ;

fragment JavaLiteral
    :   [0-9]+ ('.' [0-9]+)? [Ldf]?
    |   'true' | 'false'
    ;

fragment JavaString
    :   '"' ('\\"' | ~('"'))* '"'
    ;