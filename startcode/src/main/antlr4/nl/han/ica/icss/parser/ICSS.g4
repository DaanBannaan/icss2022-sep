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
    : stylerule* EOF // EOF = End Of File
    ;

stylerule
    : selector OPEN_BRACE declaration* CLOSE_BRACE // * betekent 0 of meer
    ;

selector // selector = LOWER_IDENT of ID_IDENT of CLASS_IDENT
    : LOWER_IDENT
    | ID_IDENT
    | CLASS_IDENT
    ;

declaration // dit betekent dat een declaratie bestaat uit een property, een dubbele punt, een concrete waarde en een puntkomma
    : property COLON literal SEMICOLON
    ;

property
    : LOWER_IDENT
    ;

literal
    : PIXELSIZE
    | PERCENTAGE
    | COLOR
    ;

