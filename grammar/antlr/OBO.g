grammar OBO;

DBLQUOTE : '\"' ;
ESCAPE : '\\' ; 

WS
	: (' ' | '\t' )+ { $channel = HIDDEN; } 
	;

NL
	: '\r' 
	| '\n' 
	; 

DBLESCAPE : ESCAPE DBLQUOTE ;

CHAR 
	: ~ESCAPE 
	| DBLESCAPE 
	;

quotedstring : DBLQUOTE ( CHAR )* DBLQUOTE ; 



