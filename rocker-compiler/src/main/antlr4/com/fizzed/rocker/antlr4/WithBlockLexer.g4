lexer grammar WithBlockLexer;

ARGUMENT_COMMA
  :  ARGUMENT ','
  ;

ARGUMENT
  :  ( ~( ',' | '(' | '"' | '[' | '<') | JavaString | Parentheses | Array | Generic)+
  ;

fragment Parentheses
  :  '(' (Parentheses | ~(')'))* ')'
  ;

fragment JavaString
  :  '"' ('\\"' | ~('"'))* '"'
  ;

fragment Array
  : '[' (Array | ~(']'))* ']'
  ;

fragment Generic
  : '<' (Generic | ~('>'))* '>'
  ;
