grammar ICSS;

//--- LEXER: ---

// IF support:
IF: 'if';
ELSE: 'else';
BOX_BRACKET_OPEN: '[';
BOX_BRACKET_CLOSE: ']';


//Literals
TRUE: 'TRUE';
FALSE: 'FALSE';
PIXELSIZE: [0-9]+ 'px';
PERCENTAGE: [0-9]+ '%';
SCALAR: [0-9]+;


//Color value takes precedence over id idents
COLOR: '#' [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f];

//Specific identifiers for id's and css classes
ID_IDENT: '#' [a-z0-9\-]+;
CLASS_IDENT: '.' [a-z0-9\-]+;

//General identifiers
LOWER_IDENT: [a-z] [a-z0-9\-]*;
CAPITAL_IDENT: [A-Z] [A-Za-z0-9_]*;

//All whitespace is skipped
WS: [ \t\r\n]+ -> skip;

//
OPEN_BRACE: '{';
CLOSE_BRACE: '}';
SEMICOLON: ';';
COLON: ':';
PLUS: '+';
MIN: '-';
MUL: '*';
ASSIGNMENT_OPERATOR: ':=';




//--- PARSER: ---
stylesheet
    : (stylerule | variableAssignment)* EOF // EOF = End Of File
    ;

stylerule
    : selector OPEN_BRACE body CLOSE_BRACE
    ;

body
    : (declaration | variableAssignment | ifClause)* // * betekent 0 of meer
    ;

selector // selector = LOWER_IDENT of ID_IDENT of CLASS_IDENT
    : LOWER_IDENT
    | ID_IDENT
    | CLASS_IDENT
    ;

declaration // dit betekent dat een declaratie bestaat uit een property, een dubbele punt, een expression en een puntkomma
    : property COLON expression SEMICOLON
    ;

property
    : LOWER_IDENT
    ;

variableAssignment
    : variableReference ASSIGNMENT_OPERATOR expression SEMICOLON
    ;

variableReference
    : CAPITAL_IDENT
    ;

expression
    : expression MUL expression                 # multiplyExpression // eerst vermenigvuldigen
    | expression op=(PLUS | MIN) expression     # addSubtractExpression // dan optellen of aftrekken
    | literal                                   # literalExpression
    | variableReference                         # variableExpression
    ;

literal
    : PIXELSIZE
    | PERCENTAGE
    | SCALAR // getal zonder eenheid
    | COLOR
    | TRUE
    | FALSE
    ;

ifClause
    : IF BOX_BRACKET_OPEN expression BOX_BRACKET_CLOSE
      OPEN_BRACE body CLOSE_BRACE
      elseClause? // optioneel
    ;

elseClause
    : ELSE OPEN_BRACE body CLOSE_BRACE
    ;
