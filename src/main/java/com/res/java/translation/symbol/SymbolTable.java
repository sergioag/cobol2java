package com.res.java.translation.symbol;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

import com.res.cobol.syntaxtree.Node;
import com.res.java.lib.RunTimeUtil;
import com.res.java.translation.engine.PrintSymbol;
import com.res.java.translation.engine.TranUtil;

/****************************************************************************************************
 *
 * File: SymbolTable.java
 * A General Symbol Table
 * Programmer: Leonidas Fegaras, UTA
 * Date: 4/10/03
 * 
 * @Venkat on 5/6/09 - adapted to RES CobolParser  
 *
 ****************************************************************************************************/
/*****************************************************************************
Copyright 2009 Venkat Krishnamurthy
This file is part of RES.

RES is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

RES is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with RES.  If not, see <http://www.gnu.org/licenses/>.

@author VenkatK mailto: open.cobol.to.java at gmail.com
******************************************************************************/

/* A symbol in the symbol table */
class SymbolCell {
    String     name;
    SymbolProperties  binding; 
    SymbolCell next;
    SymbolCell ( String n, SymbolProperties v, SymbolCell r ) { name=n; binding=v; next=r; }
}

public class SymbolTable {

	final int symbol_table_size = 4096;
    SymbolCell[] symbol_table = new SymbolCell[symbol_table_size];

    //final int scope_stack_length = 1024;

    int scope_stack_top = 0;

    int[] scope_stack = new int[symbol_table_size];
    
	public static int paragraphMark = 0;
	public static int tempVariablesMark = 0;
	public static int duplicateJavaNameMark = 0;
        private static long symbolInternalMark=0;
         private boolean cloneOnLookup = false;
	private static SymbolTable table = null;
	
	private static Stack<SymbolProperties> programs=null;
	
	public SymbolProperties getCurrentProgram() {
		if(programs==null||programs.size()<=0) return null;
		return programs.peek();
	}

	public SymbolProperties getFirstProgram() {
		if(programs==null||programs.size()<=0) return null;
		return programs.get(0);
	}

	public boolean isCurrentProgramTopLevel() {
		if(programs==null||programs.size()!=1) return false;
		return true;
	}
	
	public boolean isTopLevePrograml(SymbolProperties props) {
		if(programs==null||programs.size()!=1) return false;
		return programs.peek().getDataName().equalsIgnoreCase(props.getDataName());
	}
	
	public boolean isCurrentProgram(SymbolProperties props) {
		if(programs==null||programs.size()<=0) return false;
		return programs.peek().getDataName().equalsIgnoreCase(props.getDataName());
	}
	
	//public static Cobol2Java cobol2Java = new Cobol2Java();
	   
    public void setCloneOnLookup(boolean cloneOnLookup) {
		this.cloneOnLookup = cloneOnLookup;
	}

	public boolean isCloneOnLookup() {
		return cloneOnLookup;
	}

	static final String TOSTRING_REF_STRING_="bytes_";
    
    private SymbolTable () {
		scope_stack_top = 0;
		for (int i = 0; i<symbol_table_size; i++)
		    symbol_table[i] = null;
    }

    public void startProgram(SymbolProperties program) {
    	if(programs==null) programs = new Stack<SymbolProperties>();
    	programs.push(program);
    }
    
    public void endProgram() {
    	if(programs==null||programs.size()<=0)
    		RunTimeUtil.getInstance().reportError("FatalError: Program Stack empty",true);
    	programs.pop();
    }
    
    public static SymbolTable getScope() {
    	if(table==null) table=new SymbolTable();
    	return table;
    }
    
    public static void clear() {
    	table=null;
    	programs=null;
        paragraphMark = 0;
	tempVariablesMark = 0;
	duplicateJavaNameMark = 0;
        symbolInternalMark=0;
    }
    
    
    static void fatal_error ( String msg, Node x ) {
    	throw new Error("*** Fatal error: " + msg + x);
    }

    /* a hashing function for strings */
    int hash ( String s ) {
    	if(s==null) {
    		System.out.println("*** Fatal error: ERROR IN NULL STRING BEING HASHED");
    		System.exit(-1);
    		return -1;
    	}
    	else
    		return Math.abs(s.hashCode()) % symbol_table_size;
    }

    /* a hashing function for strings */
    int hash ( String s, int table_size ) {
    	if(s==null) {
    		System.out.println("*** Fatal error: ERROR IN NULL STRING BEING HASHED");
    		System.exit(-1);
    		return -1;
    	}
    	else
    		return Math.abs(s.hashCode()) % table_size;
    }
    
    /* insert a new item in the symbol table */
    public void insert ( String key, SymbolProperties binding ) {
    	
    key=key.toUpperCase();
	int loc = hash(key);
        binding.setInternalMark(symbolInternalMark++);
	SymbolCell o=symbol_table[loc];
	//if(symbol_table[loc]!=null)
		//for (SymbolCell s = symbol_table[loc]; s != null; s=s.next)
		  //  o=s;
	symbol_table[loc] = new SymbolCell(key,binding,o);
	if (scope_stack_top >= symbol_table_size)
	   RunTimeUtil.getInstance().reportError("Fatal Error: Symbol Table Stack Overflow.Exiting",true);
	else scope_stack[scope_stack_top++] = loc;
    }

    /* replace an item with a given name in the symbol table */
    public void replace ( String key, SymbolProperties binding ) {
    key=key.toUpperCase();
	int loc = hash(key);
	for (SymbolCell s = symbol_table[loc]; s != null; s=s.next)
	    if (s.name.equals(key))
		s.binding = binding;
    }

    private SymbolProperties cloneSymbol(SymbolProperties props) {
        if(props.getJavaType()!=null)
            props.setIdentifierType(props.getJavaType().getType());
    	if(isCloneOnLookup()) {
    		props.setIndexesWorkSpace(null);
    		props.setSubstringWorkSpace(null);
			try {
				return (SymbolProperties)props.clone();
			} catch (CloneNotSupportedException e) {
				RunTimeUtil.getInstance().reportError(e.getMessage(),false);
			}
    	}
    	return props;
    }
    
    SymbolProperties ret=null;
    /* lookup for an item in the symbol table */
    public SymbolProperties lookup ( String key ) {
    	if((getCurrentProgram()!=null&&
    			(ret=lookup(key,getCurrentProgram().getDataName()))!=null))
    			return cloneSymbol(ret);
	    key=key.toUpperCase();
		int loc = hash(key);
		for (SymbolCell s = symbol_table[loc]; s != null; s=s.next)
		    if (s.name.equalsIgnoreCase(key)) {
	    		return cloneSymbol(s.binding);
		    }
		return null;      // if not found
    }

    /* lookup for an item and parent in the symbol table */
    public SymbolProperties lookup ( String key, String par ) {
        key=key.toUpperCase();
        par=par.toUpperCase();
	int loc = hash(key);
	boolean passedAProgram=false;
	for (SymbolCell s = symbol_table[loc]; s != null; s=s.next)
	    if (s.name.equalsIgnoreCase(key)) {
	    	SymbolProperties props =s.binding;
	    	boolean first=true;
	    	do {
		    	if(!first) {
			    	if(props==null&&(par==null||par.length()==0))
			    		return cloneSymbol(s.binding);
			    	String name = (String) props.getDataName();	
			    	if (name.equalsIgnoreCase(par)&&(!passedAProgram||s.binding.isExternal()))
			    		return cloneSymbol(s.binding);
			    	else 
			    		if(props.isProgram()) {
			    			break;
			    		}
		    	}
		    	first = false;
		    	props = props.getParent();
	    	} while(props!=null);
	    }
	return null;       // if not found
    }
    
    /* lookup for an item and parent in the symbol table */
    public SymbolProperties lookup ( String key, int type ) {
       	if((getCurrentProgram()!=null&&
    			(ret=lookup(key,getCurrentProgram().getDataName()))!=null))
       		if(ret.getType()==type)
    			return cloneSymbol(ret);
       		else return null;
    key=key.toUpperCase();
	int loc = hash(key);
	for (SymbolCell s = symbol_table[loc]; s != null; s=s.next) {
	    if (s.name.equalsIgnoreCase(key)&&s.binding.getType()==type) {
    		return cloneSymbol(s.binding);
	    }
	}
	return null;       // if not found
    }
    
    /* start a new environment */
    public void begin_scope () {
	if (scope_stack_top >= symbol_table_size)
		RunTimeUtil.getInstance().reportError("Fatal Error: Symbol Table Stack Overflow.Exiting",true);
	else scope_stack[scope_stack_top++] = -1;
    }

    /* pop the last environment */
    public void end_scope () {
	int i = scope_stack_top-1;
	for (; scope_stack[i]>=0 && i>0; i--) {
	    int loc = scope_stack[i];
	    symbol_table[loc] = symbol_table[loc].next;
	};
	scope_stack_top = i;
    }

    /* display the content of the symbol table */
	public void display () {
	SymbolCell[] s = new SymbolCell[symbol_table_size];
	for (int i = 0; i<symbol_table_size; i++)
	    s[i] = symbol_table[i];
	for (int i = scope_stack_top-1; i>=0; i--)
	    if (scope_stack[i] == -1)
	       System.out.println("----------------");
	    else { SymbolCell c = s[scope_stack[i]];
	           s[scope_stack[i]] = c.next;
	       SymbolProperties o1=null,o2=null;
	       o1= c.binding;
	       o2= (SymbolProperties) c.binding.getParent();
	       if (o1==null)
	    	   System.out.println("***ERROR IN SYMBOL TABLE"+c.name);
	       else {
	    	   System.out.print("DATA="+c.name);
	    	   if(o2!=null)
	    		  System.out.print(" Parent=" + 
				   (String)o2.getDataName());
	    	   int i1 ;
	    	   i1=o1.getLevelNumber();
	    	   System.out.print(" LEVEL="+i1);
	    	   i1=o1.getOffset();
    		   System.out.print(" Offset="+i1);
	    	   i1=o1.getLength();
    		   System.out.print(" Length="+i1);
	    	   i1=o1.getDataUsage();
    		   System.out.print(" USAGE="+i1);
	    	   String p1 = null;
	    	   if((p1=(String)o1.getPictureString())!=null)
	    		   System.out.print(" PICTURE="+p1);
	    	   p1 = null;
	    	   if((p1=(String)o1.getValue1())!=null)
	    		   System.out.print(" VALUE="+p1);
	    	   p1 = null;
	    	   if((p1=(String)o1.getValue2())!=null)
	    		   System.out.print(" THROUGH"+p1);
	    	   boolean b1 =  o1.getRef();
    		   System.out.print("Ref="+b1);
	    	   b1 = o1.getMod();
    		   System.out.print("Mod="+b1);
	    	   b1 = o1.getIsSuppressed();
    		   System.out.print("IS_SUPRESSED="+b1);
	    	   b1 = o1.getIsFormat();
    		   System.out.print("IsFormat(="+b1);
	       }
	       System.out.println();
	       if(o1!=null&&o1.getChildren()!=null) {
	    	   for (Iterator<SymbolProperties> ite=o1.getChildren().iterator();
	    	   		ite.hasNext();) {
	    		   o2=ite.next();
	    		   System.out.println("\t\t"+((o2==null)?"NullChild":((o2.getDataName())!=null)?
	    				   ((String)o2.getDataName()):"NullName"));
	    	   }
	       }
	    }
    }
    

    public static void visit(SymbolProperties props,Visitor visitor) {
    	if(props.isProgram()&&
    			!SymbolTable.getScope().isCurrentProgram(props)
    			||props.getType()==SymbolConstants.PARAGRAPH
    			||props.getType()==SymbolConstants.SECTION)
    		return;
    	//Preprocess
    	visitor.visitPreprocess(props);
       	
    	//Body
       	if(props.isFile())
    		visitor.visitFile(props);
    	else
    	if(props.getLevelNumber()==1&&props.getPictureString()==null)
    		visitor.visit01Group(props);
    	else
	    	if(props.getLevelNumber()==1&&props.getPictureString()!=null)
	    		visitor.visit01Element(props);
	    	else
		    	if(props.getLevelNumber()==77&&props.getPictureString()!=null)
		    		visitor.visit77Element(props);
		    	else
	    	    	if(props.getLevelNumber()==88)
	    	    		visitor.visit88Element(props);
	    	    	else
		        	if(props.getPictureString()==null)
		        		visitor.visitInnerGroup(props);
		    	    	else
		    	    	if(props.getPictureString()!=null)
		    	    		visitor.visitInnerElement(props);
			        	
    	
    	//Postprocess
    	visitor.visitPostprocess(props);
    	
    }
    
    public static void visit(ArrayList<SymbolProperties> children,Visitor visitor) {
    	//visit children
    	if(children!=null){
    		for(SymbolProperties child:children){
    			
    			visitor.visitChildPreprocess(child);
    			visit(child,visitor);
    			visitor.visitChildPostprocess(child);
    		}
    	} 
    		
    }
    
    public static boolean convert2Java() {
    	visit(SymbolTable.getScope().getCurrentProgram(), new PrintSymbol());
    	TranUtil.get().saveDatabaseProperties();
    	return true;
    }

}
