lexer grammar RockerLexer;

// content mode (by default)


ELSE_IF
    :   '}' Ws? 'else' Ws? 'if' Ws? Parentheses Ws? '{'
    ;

ELSE
    :   '}' Ws? 'else' Ws? '{'
    ;

//switch block should not allow WS as this will generate statments within switch block which are not case or default
CASE
    :   Ws? 'case' Ws? Parentheses Ws? '{'
    ;

//switch block should not allow WS as this will generate statments within switch block which are not case or default
CASE_EXPRESSION
    :   Ws? 'case' Ws? Parentheses Ws? '->' Ws? '{'
    ;

//switch block should not allow WS as this will generate statments within switch block which are not case or default
DEFAULT
    :   Ws? 'default' Ws? '{' Ws?;

//switch block should not allow WS as this will generate statments within switch block which are not case or default
DEFAULT_EXPRESSION
    :   Ws? 'default' Ws? '->' Ws? '{' Ws?;


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

//switch block should not allow WS as this will generate statments within switch block which are not case or default
MV_SWITCH
    :   'switch' Ws? Parentheses Ws? '{' Ws?                                -> popMode
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

MV_EVAL
    :   Parentheses                                                 -> popMode
    ;

MV_NULL_TERNARY_LH
    :   VariableExpression '?:'                                     -> pushMode(NULL_TERNARY_EXPR)
    ;

MV_VALUE 
    :   '?'? VariableExpression                                     -> popMode
    ;


mode NULL_TERNARY_EXPR;

MV_NULL_TERNARY_RH
    :   ValueExpression                                             -> popMode, popMode
    ;


//
// fragments used everywhere else
//

fragment ValueExpressions
    :   ValueExpression (Ws? Op Ws? ValueExpression)*
    ;

// variable or literals such as strings and primitives
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
    :   ('if' | 'for' | 'switch' | 'case' | 'default')
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
