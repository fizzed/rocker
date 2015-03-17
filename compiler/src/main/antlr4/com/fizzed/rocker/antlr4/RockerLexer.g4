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

AT_IMPORT
    :   '@import'                               -> pushMode(MAGIC_LINE)
    ;

AT_OPTION
    :   '@option'                               -> pushMode(MAGIC_LINE)
    ;

AT_ARGS
    :   '@args'                                 -> pushMode(MAGIC_ARGS)
    ;

AT_IF
    :   '@if'                                   -> pushMode(MAGIC_BLOCK)
    ;

AT_FOR
    :   '@for'                                  -> pushMode(MAGIC_BLOCK)
    ;

AT_VALUE
    :   '@'                                     ->  pushMode(MAGIC_VALUE)
    ;


// magic "for a single line" mode
// @import java.util.*

mode MAGIC_LINE;

ML_WSNB
    :   (' ' | '\t')+                           -> skip
    ;

ML_PLAIN
    :   ~[\r\n]+
    ;

ML_EOL
    :   '\r'? '\n'                              -> popMode
    ;




// magic arguments mode
// @args (String s, Date d)

mode MAGIC_ARGS;

MA_ARGS
    :   WhitespaceWithLineBreak* '(' ~(')')* ')' -> popMode
    ;



// magic "for an expression and block" mode
// @if (true) { //stuff }

mode MAGIC_BLOCK;

MB_PARENTHESE
    :   '(' (MB_PARENTHESE | ~(')'))* ')'
    ;

MB_LCURLY
    :   '{'                                     -> popMode
    ;

MB_WSWB
    :   WhitespaceWithLineBreak+                -> skip
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

MV_CONTENT_CLOSURE
    :   Identifier WhitespaceWithLineBreak* '=>' WhitespaceWithLineBreak* '{'    -> popMode
    ;

MV_VALUE_CLOSURE
    :   QualifiedName MV_PARENTHESE? MV_ARRAY? ('.' Identifier MV_PARENTHESE? MV_ARRAY?)* WhitespaceWithLineBreak* '->' WhitespaceWithLineBreak* '{'    -> popMode
    ;

MV_VALUE 
    :   QualifiedName MV_PARENTHESE? MV_ARRAY? ('.' Identifier MV_PARENTHESE? MV_ARRAY?)*     -> popMode
    ;



//
// fragments used everywhere else
//

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
