parser grammar WithBlockParser;

options {
    tokenVocab=WithBlockLexer;
}

start
  : withArguments EOF
  ;

withArguments
  : ARGUMENT_COMMA* ARGUMENT
  ;
