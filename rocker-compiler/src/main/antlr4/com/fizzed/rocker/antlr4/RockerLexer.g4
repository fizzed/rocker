lexer grammar RockerLexer;


// content mode (by default)

ELSE_CALL
    :   '}' WhitespaceWithLineBreak* 'else' WhitespaceWithLineBreak* '{'
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

AT_VALUE
    :   '@'                                     ->  pushMode(MAGIC_VALUE)
    ;

// magic "for an expression" mode
// @value[0]
// @value[0].getProperty("a")
// @value().getProperty(true)
// @value.getProperty(true)
// @value().getProperty(true).getAnotherProperty("hello")
// @value

mode MAGIC_VALUE;

MV_PARENTHESE
    :   '(' (MV_PARENTHESE | ~(')'))* ')'                                 
    ;

MV_ARRAY
    :   '[' (MV_ARRAY | ~(']'))* ']'
    ;

MV_IMPORT
    :   'import' WhitespaceWithLineBreak+ ~[\r\n]+ '\r'? '\n'                                                -> popMode
    ;

MV_OPTION
    :   'option' WhitespaceWithLineBreak+ ~[\r\n]+ '\r'? '\n'                                                -> popMode
    ;

MV_ARGS
    :   'args' WhitespaceWithLineBreak* '(' ~(')')* ')'                             -> popMode
    ;

MV_IF
    :   'if' WhitespaceWithLineBreak* MV_PARENTHESE WhitespaceWithLineBreak* '{'    -> popMode
    ;

MV_FOR
    :   'for' WhitespaceWithLineBreak* MV_PARENTHESE WhitespaceWithLineBreak* '{'    -> popMode
    ;

MV_WITH
    :   'with' '?'? WhitespaceWithLineBreak* MV_PARENTHESE WhitespaceWithLineBreak* '{'    -> popMode
    ;

MV_CONTENT_CLOSURE
    :   Identifier WhitespaceWithLineBreak* '=>' WhitespaceWithLineBreak* '{'    -> popMode
    ;

MV_VALUE_CLOSURE
    :   QualifiedName MV_PARENTHESE? MV_ARRAY? ('.' Identifier MV_PARENTHESE? MV_ARRAY?)* WhitespaceWithLineBreak* '->' WhitespaceWithLineBreak* '{'    -> popMode
    ;

MV_ELVIS
    :   '?' MV_PARENTHESE                                                                                 -> popMode
    ;

MV_VALUE 
    :   '?'? QualifiedName MV_PARENTHESE? MV_ARRAY? ('.' Identifier MV_PARENTHESE? MV_ARRAY?)*            -> popMode
    ;


//
// fragments used everywhere else
//

fragment RerservedQualifiedNames
    :   ('if' | 'for')
    ;

fragment LineBreak
    :   ('\r'? '\n')
    ;

fragment WhitespaceWithNoLineBreak
    :   (' ' | '\t')
    ;

fragment WhitespaceWithLineBreak
    :   (' ' | '\t' | '\r'? '\n')
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
