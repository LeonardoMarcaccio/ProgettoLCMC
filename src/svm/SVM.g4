grammar SVM;

@parser::header {
import java.util.*;
}

@lexer::members {
public int lexicalErrors=0;
}
   
@parser::members { 
public int[] code = new int[ExecuteVM.CODESIZE];    
private int i = 0;
private Map<String,Integer> labelDef = new HashMap<>();
private Map<Integer,String> labelRef = new HashMap<>();
}

/*------------------------------------------------------------------
 * PARSER RULES
 *------------------------------------------------------------------*/
   
assembly: instruction* EOF 	{ for (Integer j: labelRef.keySet()) 
								code[j]=labelDef.get(labelRef.get(j)); 
							} ;

instruction : 
        PUSH n=INTEGER   {code[i++] = PUSH; 
			              code[i++] = Integer.parseInt($n.text);}
	  | PUSH l=LABEL    {code[i++] = PUSH;
	    		             labelRef.put(i++,$l.text);} 		     
	  | POP		    {code[i++] = POP;}	
	  | ADD		    {code[i++] = ADD;}
	  | SUB		    {code[i++] = SUB;}
	  | MULT	    {code[i++] = MULT;}
	  | DIV		    {code[i++] = DIV;}
	  | STOREW	  {code[i++] = STOREW;}
	  | LOADW           {code[i++] = LOADW;}
	  | l=LABEL COL     {labelDef.put($l.text,i);}
	  | BRANCH l=LABEL  {code[i++] = BRANCH;
                       labelRef.put(i++,$l.text);}
	  | BRANCHEQ l=LABEL {code[i++] = BRANCHEQ;
                        labelRef.put(i++,$l.text);}
	  | BRANCHLESSEQ l=LABEL {code[i++] = BRANCHLESSEQ;
                          labelRef.put(i++,$l.text);}
	  | JS              {code[i++] = JS;}
	  | LOADRA          {code[i++] = LOADRA;}
	  | STORERA         {code[i++] = STORERA;}
	  | LOADTM          {code[i++] = LOADTM;}   
	  | STORETM         {code[i++] = STORETM;}   
	  | LOADFP          {code[i++] = LOADFP;}
	  | STOREFP         {code[i++] = STOREFP;}
	  | COPYFP          {code[i++] = COPYFP;}
	  | LOADHP          {code[i++] = LOADHP;}
	  | STOREHP         {code[i++] = STOREHP;}
	  | PRINT           {code[i++] = PRINT;}
	  | HALT            {code[i++] = HALT;}
	  ;
	  
/*------------------------------------------------------------------
 * LEXER RULES
 *------------------------------------------------------------------*/

PUSH            : 'push' ;
POP	            : 'pop' ;
ADD	            : 'add' ;
SUB	            : 'sub' ;
MULT	        : 'mult' ;
DIV	            : 'div' ;
STOREW	        : 'sw' ;    // Pop two values, the second one is written at the memory address pointed by the first one
LOADW	        : 'lw' ;    // Read the content of the memory cell pointed by the top of the stack and replace it with such value
BRANCH	        : 'b' ;     // Jump at the instruction pointed by a label
BRANCHEQ        : 'beq' ;   // Conditional Jump 1
BRANCHLESSEQ    : 'bleq' ;  // Conditional Jump 2
JS	            : 'js' ;    // Pop one value, copy the INSTRUCTION POINTER in the RETURN ADDRESS register and jump to the popped value
LOADRA	        : 'lra' ;   // Push the RETURN ADDRESS register content on top of the stack
STORERA         : 'sra' ;   // Pop the top of the stack and copy the value in the RETURN ADDRESS
LOADTM	        : 'ltm' ;   // Push the TEMPORARY register content on top of the stack
STORETM         : 'stm' ;   // Pop the top of the stack and copy the value in the TEMPORARY
LOADFP	        : 'lfp' ;   // Push the FRAME POINTER register content on top of the stack
STOREFP	        : 'sfp' ;   // Pop the top of the stack and copy the value in the FRAME POINTER
COPYFP          : 'cfp' ;   // Copy the current value of the STACK POINTER in the FRAME POINTER
LOADHP	        : 'lhp' ;   // Push the HEAP POINTER register content on top of the stack
STOREHP	        : 'shp' ;   // Pop the top of the stack and copy the value in the HEAP POINTER
PRINT	        : 'print' ; // Show the top of the stack without removing it
HALT	        : 'halt' ;  // Stops the programme
 
COL	            : ':' ;
LABEL	        : ('a'..'z'|'A'..'Z')('a'..'z' | 'A'..'Z' | '0'..'9')* ;
INTEGER	        : '0' | ('-')?(('1'..'9')('0'..'9')*) ;

COMMENT         : '/*' .*? '*/' -> channel(HIDDEN) ;

WHITESP         : (' '|'\t'|'\n'|'\r')+ -> channel(HIDDEN) ;

ERR	            : . { System.out.println("Invalid char: "+getText()+" at line "+getLine()); lexicalErrors++; } -> channel(HIDDEN);

