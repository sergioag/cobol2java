package com.res.cobol;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PipedWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.res.common.RESConfig;
import com.res.common.StringTokenizerExCustom;
import com.res.java.lib.RunTimeUtil;
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
<p>History:<ul>
<li><date>July-2009</date>Writeout preprocessor directives with copy files and source files and related changes to this.
******************************************************************************/
/**
 * This class converts a COBOL 85 fixed-format source into a free format source.
 * It also processes the REPLACE and COPY statements, and converts compiler
 * directives to comments.
 * @author Bernard Pinon
 * <p>Copyright (c) Bernard Pinon 2002
 * <p>This file can be freely redistributed under the terms of the
 * <a href="http://www.gnu.org/licenses/lgpl.html">LGPL licence</a>
 * published by the Free Software Foundation.
 */

public class Preprocessor {
  /**
   * Indicates that the input format is fixed - this is the
   * standard ANSI format, inspired by punched cards. Each line
   * must have exactly 80 characters.
   * <ul>
   * <li>Col 1 to 6 (included) : comments - used to number cards
   * <li>Col 7: indicator field
   * <li>Col 8-12 : Area A
   * <li>Col 13-72 : Area B
   * <li>Col 73-80 : comments
   * </ul>
   */

  public static final int FORMAT_FIXED = 0;

  /**
   * Indicates that the format is variable. This is used by the Fujitsu-Siemens
   * compiler for instance. In this format, lines can have a variable length.
   * <ul>
   * <li>Col 1 to 6 (included) : comments
   * <li>Col 7: indicator field
   * <li>Col 8-12 : Area A
   * <li>Col 13-80 : Area B
   * </ul>
   */

  public static final int FORMAT_VARIABLE = 1;

  /**
   * Indicates that the format is the one used on HP-Compaq Non-stop systems,
   * also known as "Tandem" systems. This is similar to the variable format, but
   * without the first six comment characters.
   * <ul>
   * <li>Col 1: indicator field
   * <li>Col 2-6 : Area A
   * <li>Col 7-80 : Area B
   * </ul>
   */

  public static final int FORMAT_TANDEM = 2;

  /**
   * The format currently in use.
   */

  private int source_format = FORMAT_FIXED;

  /**
   * The path of the source file. We assume that all copy-books will reside in
   * the same directory.
   */

  private File source_path;

  /**
   * A reader for the fixed format COBOL 85 source.
   */

  private BufferedReader input;

  /**
   * A writer for the free format COBOL 85 source.
   */

  private PrintWriter output;

  /**
   * The current source line.
   */

  private String line;

  /**
   * The current line number, for error messages.
   */

  private int line_number;

  /**
   * The indicator field of the current line.
   */

  //private char indicator_field;

  /**
   * The REPLACE substitution map.
   */

  private class CopyClass {
	  
	String copyName=null;
	private String lookAheadLine=null;
	ArrayList<CoupleValue> replace = new ArrayList<CoupleValue>();
	int last_line_number;
	File copy_file=null;
	public void setLookAheadLine(String lookAheadLine) {
		this.lookAheadLine = lookAheadLine;
	}
	public String getLookAheadLine() {
		return lookAheadLine;
}
  }
  
  private ArrayList<CoupleValue> rootReplace = new ArrayList<CoupleValue>();
  
  private Stack<CopyClass> copyStack=new Stack<CopyClass>();

  /**
   * The delimiters used to parse the source line
   * for REPLACE/COPY.
   */

  private static final String TOKEN_PATTERN = "==|((\\d)+\\.(\\d)+)|(\\.(\\d)+)|\\.|(\\s)+|\\;|\\,\\\"(([^\\\"]*)|\\\"\\\"|\\\'\\\')\\\"|\\\'(([^\\\']*|\\\"\\\"|\\\'\\\'))\\\'|\\\'|\\\"";
  private static final Pattern DELIMITERS_SIMPLE = Pattern.compile("==|\\.|\\s+|\\;|\\,|\\\'|\\\"");
  /**
   * If true, we include debugging lines in the resulting source.
   */

  private boolean include_debug_lines=true;

  /**
   * Public empty constructor.
   */

  public Preprocessor() {
  }

  /**
   * Skips all delimiters in the parsed line.
   * @param st a StringTokenizer.
   * @return the next significant token or an empty string.
  */

  private final String skipDelimiters() {
      String tok ;
      for(tok = getNextToken(true); tok!=null&&(tok.length()==0||
      DELIMITERS_SIMPLE.matcher( tok ).matches())  ;tok = getNextToken(true)) 
    	 ;
      return tok;
  }
 
  
  /**
   * Processes the COPY statement.
   * @param st a StringTokenizer.
   */
 	private final boolean alreadyOpened(String copyName) {
		  for(CopyClass c:copyStack) {
			  if(c.copyName.equalsIgnoreCase(copyName))
				  return true;
		  }
		  return false;
  	}
 	private static StringTokenizerExCustom 	aStringTokenizerEx=null;
	public void getTokens(String line) {
		if(aStringTokenizerEx==null) aStringTokenizerEx= new StringTokenizerExCustom(line, TOKEN_PATTERN);
		else aStringTokenizerEx.match(line);
		currentTokenSavest=0;
		savest=aStringTokenizerEx.values();
	}
  	private final void processCopy(  ) throws IOException {
  		turnOffReplacing=true;
  		output.flush();
  		saveLine.clear();
  		String copy_name = skipDelimiters();
		
  		if( copy_name==null) {
  			turnOffReplacing=false;
  			System.err.println("Problem parsing COPY, invalid syntax line " + line_number );
  			return;
  		}
		
		copy_name = copy_name.toUpperCase();
		output.flush();
		CopyClass copyClass = checkCopy(copy_name,true);
		turnOffReplacing=false;
		if(copyClass==null){
			while(!skipSpaces().equals("."))
				;
			printSaved(true);
			return;
		}
	

		copyStack.push(copyClass);
		BufferedReader current_input=input;
		
		output.println( "# " + 1 + " \""+ copyClass.copyName +"\"  " );
		
		try {
		  input = new BufferedReader( new FileReader( copyClass.copy_file ) );
		  preprocessMainline();
		}
		catch( Exception ex ) {
		  System.err.println("IO Exception while processing COPY line " + line_number + " " + line );
		  ex.printStackTrace();
		}
		
		input = current_input;
		copyStack.pop();
		line_number = ++copyStack.peek().last_line_number;
		output.println( "# " + line_number + " \""+ copyStack.peek().copyName  +"\"  " );
	    
  	}

	private ArrayList<String> saveLine=new ArrayList<String>();
	
	private final String skipSpaces( ) {
	      String tok ;
	      while((tok = getNextToken(true))!=null&&tok.trim().length() == 0 ) 
	       ;
	      return tok;
	  }

  	private final String getNextToken(boolean moreLines) {
  		return getNextToken(moreLines,false,false);
  	}
 	

 	private final String getNextToken(boolean moreLines,boolean ignoreComments) {
 		String ret=null;
		ret=getNextToken(moreLines,ignoreComments,false) ;
 		 return ret;
 		 
  	}
 
	private final String getNextToken(boolean moreLines,boolean ignoreComments,boolean markLines) {
  		String ret;	
  		do {
  			if(!hasMoreTokens() ) {
  				if( moreLines){
				
					getNextLine(ignoreComments, markLines);
					if(line==null)	return null;
					getTokens(line);
					if( !hasMoreTokens() ) {
						System.err.println("Problem parsing COPY, invalid syntax line " + line_number );
						return null;
					}
	
				} else return null;
			}
			ret=nextToken();
		} while (ret==null||ret.length()==0||(!markLines&&ret.trim().equals("%%%")));
		savedPartialLine.append(ret);
		return ret ;
  	}

	private void getNextLine(boolean ignoreComments, boolean markLines) {
		saveLine.add(line);
		try {
			savedPartialLine.setLength(0);
			do {
				readLine();
				if(markLines) {
					line+="%%%";
				}
			} while(ignoreComments&&isCommentLine(line));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
 	
  	private boolean hasMoreTokens() {
  		if(savest==null||savest.size()<=currentTokenSavest) return false;
  		return true;
  	}
  	private String nextToken() {
  		if(savest==null||savest.size()<=currentTokenSavest) return null;
  		return savest.get(currentTokenSavest++);
  	}
  	private void backup(int n) {
  		if(savest==null||currentTokenSavest-n<0||currentTokenSavest-n>savest.size()) ;
  		else currentTokenSavest-=n;
  		
  	}
  	private int currentTokenSavest =0;
  	private ArrayList<String> savest;
  	private StringBuffer savedPartialLine=new StringBuffer(132);
  	
 	private final void processExecSqlInclude() throws IOException{
 		
  		saveLine.clear();
		
		String tok=skipSpaces();
		
		if(tok==null||!tok.equalsIgnoreCase("SQL")) {
			printSaved(false);
			return;
		}
		
		tok=skipSpaces();
		
		if(tok==null||!tok.equalsIgnoreCase("INCLUDE")) {
			printSaved(false);
			return;
		}
		
		String copy_name = skipSpaces();
		if(copy_name==null) {printSaved(true);return;}
		
		if(copy_name.equalsIgnoreCase("\"")||
				copy_name.equalsIgnoreCase("\'")) {
			copy_name = skipSpaces();
			if(copy_name==null) {printSaved(true);return;}
			tok=skipSpaces();
			if(tok==null) {printSaved(true);return;}
			if(!tok.equalsIgnoreCase("\"")&&
					tok.equalsIgnoreCase("\'")) {printSaved(true);return;}
		}
			
		
		CopyClass copyClass = checkCopy(copy_name,false);
		
		if(copyClass==null){
			output.flush();
			return;
		}
		
		copyStack.push(copyClass);
		BufferedReader current_input  = input;
		
		output.println( "# " + 1 + " \""+ copyClass.copyName +"\"  " );
		
		try {
		  input = new BufferedReader( new FileReader( copyClass.copy_file ) );
		  preprocessMainline();
		}
		catch( Exception ex ) {
		  System.err.println("IO Exception while processing COPY line " + line_number + " " + line );
		  ex.printStackTrace();
		}
		
		input = current_input;
		copyStack.pop();
		line_number = ++copyStack.peek().last_line_number;
		output.println( "# " + line_number + " \""+ copyStack.peek().copyName  +"\"  " );
		  
  	}

	private void printSaved(boolean asComment) throws IOException {
		turnOffReplacing=false;
		for(String s:saveLine) {
			if(asComment||isCommentLine(s))
				outputAsComment(s);
			else
				output.println(s);
		}
		saveLine.clear();
		if(asComment||isCommentLine(line))
			outputAsComment(line);
		else
			output.println(line);
		//readLine();
		return;
	}

	private boolean isCommentLine(String ln) {
		if(ln==null) return true;
		if(getSourceFormat()==FORMAT_FIXED) {
			return  ln.charAt(6)=='*';
		} else {
			return  ln.charAt(0)=='*';
		}
	}
	
	private CopyClass checkCopy(String copy_name,boolean fromCopy) throws IOException {
		
		copy_name=RunTimeUtil.getInstance().stripQuotes((String)copy_name);
		
		File copy_file = new File( source_path, copy_name );
		
		if(!copy_file.exists()) {
		  String copy_name2=copy_name+".CPY";
          copy_file = new File( source_path, copy_name2 );
          if(!copy_file.exists()) {
        	  copy_name2=copy_name+".CBL";
              copy_file = new File( source_path, copy_name2 );
          if(!copy_file.exists()) {
               copy_name2=copy_name+".COB";
              copy_file = new File( source_path, copy_name2 );
	              if( !copy_file.exists() ) {
	            	  System.err.println("File not found : " + copy_file.getCanonicalPath() );
	            	  if(fromCopy) doCopyTail(null); else doTillExecTail(null);
	            	  return null;
	              }
    	  	}
          }
		}
		
		if(alreadyOpened( copy_file.getCanonicalPath() )) {
			System.err.println("Rucursive COPY not allowed : " + copy_file.getCanonicalPath() );
		  	outputAsComment( line + " (file not found)" );
		  	if(fromCopy) doCopyTail(null); else doTillExecTail(null);
		  	return null;
		}
		output.flush();
		copy_name=copy_file.getCanonicalPath() ;
		
		// saves context
		CopyClass copyClass=copyStack.peek();
		copyClass.last_line_number=line_number;
		copyClass=new CopyClass();
		copyClass.copyName=copy_name;
		copyClass.last_line_number=1;
		copyClass.copy_file = copy_file;
  	  	if(fromCopy) {
  	  		doCopyTail(copyClass);
  	  	}
  	  	else doTillExecTail(copyClass);
  	  	
		return copyClass;
	}
	
	private final void doTillExecTail(CopyClass copyClass) throws IOException {
		
		for(String tok1=getNextToken(true);tok1!=null;tok1=getNextToken(true)) {
			if(tok1.equalsIgnoreCase("END-EXEC")) {break;}
		}
		
		doCopyTail(copyClass);
	}
	
	@SuppressWarnings("unused")
	private void doCopyHead() {
		int i=savedPartialLine.length()-1;
		for(;i>=0;--i)
			if(savedPartialLine.charAt(i)==' '||savedPartialLine.charAt(i)=='\t') ; else break;
		for(;i>=0;--i)
			if(savedPartialLine.charAt(i)==' '||savedPartialLine.charAt(i)=='\t') break;
		if(i>0) {
	 	  	output.println(savedPartialLine.substring(0,i));
			savedPartialLine.delete(0,i);
		}
	}

	private void doCopyTail(CopyClass copyClass) throws IOException {
		String tok=skipSpaces();
		//Only catch is the period if any has to be on the same line as END-EXEC.
		if(line!=null&&line.indexOf('.',savedPartialLine.length()-1)>=0) {
			for(;tok!=null;tok=skipSpaces()) {
				if(tok.equalsIgnoreCase(".")||(copyClass!=null&&tok.equalsIgnoreCase("REPLACING"))) {break;}
			}
		}
		if(copyClass!=null) {
			if(tok.equalsIgnoreCase("REPLACING")) {
				processReplace(copyClass);
			}
		}
  	  	for(String s:saveLine) {
  	  		outputAsComment(RunTimeUtil.getInstance().stripTrailingBlanks(s) +
  	  				((copyClass==null)?"\t\t(file not found)":"") );
  	  	}
  	  	
  	  	outputAsComment( savedPartialLine.toString() +
  	  				((copyClass==null)?"\t\t(file not found)":"") );
  	  	line="";
  	  	for(int i=0;i<savedPartialLine.length();i++)
  	  		line+=' ';
  	  	if(hasMoreTokens())
  	  	for(String s=nextToken();s!=null&&hasMoreTokens();s=nextToken())
  	  		line+=s;
	}
	
	private boolean turnOffReplacing= false;

	/**
	 * Processes the REPLACE statement.
	 * @param st a StringTokenizer.
	 * @throws IOException 
	 */
	class CoupleValue {
		int noOfKeyTokens;
		Pattern key;
		String value;
	}
	private final void processReplace(CopyClass copyclass ) throws IOException {
		turnOffReplacing=true;
		output.flush();
		if(copyclass==null)	saveLine.clear();
		ArrayList<CoupleValue> replace = new ArrayList<CoupleValue>();
		String tok  = skipSpaces(); ; 
		do {
			
			if( tok==null ) {
				printSaved(true);
				return;
			}
			
		  	if(tok.equalsIgnoreCase("OFF")) {
		  		printSaved(true);
		  		if(copyclass==null)
		  			rootReplace.clear();
		  		return;
		  	}
			StringBuffer subst = new StringBuffer();
		
			if(tok.equals("==")) {
				while( (tok = getNextToken(true,true))!=null && !tok.equals("==") ) 
					subst.append( tok );
				tok = skipSpaces();
			} else
					do{
						subst.append( tok );
					} while( (tok=getNextToken(true,true,true))!=null && !tok.equalsIgnoreCase("BY") ) ;
			
			if( tok==null ) {
				  System.err.println("Problem parsing REPLACE, invalid syntax, expecting <COBOL_WORD> line " + line_number );
				  return;
			}
			
			String key = subst.toString().trim();
			
			if(tok==null || !tok.toUpperCase().equals( "BY" ) ) {
			  System.err.println("Problem parsing REPLACE, invalid syntax, expecting BY line " + line_number );
			  return;
			}
			
			tok = skipSpaces().toUpperCase();
			
			if( tok == null ) {
			  System.err.println("Problem parsing REPLACE, invalid syntax, expecting substitution text line " + line_number );
			  return;
			}
			
			subst = new StringBuffer();
			
			
			if(tok.equals("==")) {
				while( (tok = getNextToken(true,true,true))!=null && !tok.equals("==") )  {
					subst.append( tok );
				}
				tok  = skipSpaces(); 
			} else {
				ArrayList<String> tokList=new ArrayList<String>();
				 do{
					tokList.add( tok );
				} while( (tok=getNextToken(true,true,true))!=null && !(tok.equals(".")||tok.equals("==")||tok.equalsIgnoreCase("BY") ));
				 int i=tokList.size()-1;
				if(tok.equalsIgnoreCase("BY")) {
					 backup(1);
					for(;i>0&&(tok=tokList.get(i)).trim().length()==0;--i){
						backup(1);
					}
					backup(1);i--;
					tok  = skipSpaces(); 
				}
				for(;i>=0;--i)
					subst.insert(0,tokList.get(i));
			}
			String value = subst.toString().trim();
			//Prepare Key pattern and Value string.
			value=value.trim().replace("%%%", "").replace("\\", "\\\\").replace("$","\\$");
			String[] sArr=key.trim().split("(\\s|,|;)+");
			StringBuffer regEx=new StringBuffer();
			int sArr_Length=0;
			for(int i=0;i<sArr.length;++i) {
			   if(sArr[i].length()==0) continue;
				sArr_Length++;
				regEx.append(escapeRegEx(sArr[i].trim()));
				  if(i<sArr.length-1)
					  regEx.append(MATCH_DELIMITERS);
			  }
			  
			  Pattern pattern = Pattern.compile(regEx.toString());
			  CoupleValue keyValue=new CoupleValue();
			  keyValue.key=pattern;keyValue.value=value;keyValue.noOfKeyTokens=sArr_Length;
			  replace.add(keyValue);
			
			for(int i=0;i< saveLine.size();++i) {
				saveLine.set(i, saveLine.get(i).replace("%%%", ""));
			}
			line=line.replace("%%%","");
	  	}	while(tok!=null&&!tok.equals("."));
		
		if(copyclass==null) {
			rootReplace.addAll(replace);
			printSaved(true);
		}
		else
			copyclass.replace.addAll( replace );
	  }

  /**
   * Removes the trailing spaces on a line. Used to save some space.
   * @param line a String.
   * @return the string without trailing spaces.
   */

  private final String removeTrailingSpaces( String line ) {
    int i = line.length() - 1;
    int min=0;
    if(source_format == FORMAT_FIXED)
    	min=6;
    while( i >= min && line.charAt(i) == ' ' ) {
      --i;
    }
    return line.substring(0,i+1);
  }

  /**
   * Processes a COBOL line, traping COPY/REPLACE statements, and
   * making the substitutions required by REPLACE.
   * @param line a String
 * @throws IOException 
   */

  
  private final void processCopyReplace( ) throws IOException {
	  	getTokens( line);
    	StringBuffer result = new StringBuffer( line.length() + 10 );
    	boolean in_string = false;
    	
    	String tok;
    	
    	while((tok=getNextToken(false))!=null) {
    			
    		  if(tok.length()==0) continue;
    		
		      // is it a delimiter ?
		      if( DELIMITERS_SIMPLE.matcher(tok).matches() ) {
		        if( tok.equals("\"") ) in_string = !in_string;
		        result.append( tok );
		        continue;
		      }
		
		      // REPLACE/COPY should be the first significant token
		      if( !in_string ) {

			        if( tok.equalsIgnoreCase( "REPLACE" ) ) {
			          //doCopyHead();
			          processReplace(null );
			          return;
			        }
			        else 
		        	if( tok.equalsIgnoreCase( "COPY" ) ) {
			          //doCopyHead();
			          processCopy();
			          return;
			        }
			        else {				      
				    	  if(tok.equalsIgnoreCase("EXEC")||
				    		  tok.equalsIgnoreCase("EXECUTE")) {
					         // printSaved(false);
				    		  processExecSqlInclude();
				    		  return;
				      }
			        }
			      
		      // other token should be checked for substitution, except for
		      // those enclosed in ""
		       // String utok = tok.toUpperCase();
		        //if( copyStack.peek().replace.containsKey( utok ) ) {
		         // tok = (String)copyStack.peek().replace.get( utok );
		        //}
	      	}
	
	      	result.append( tok );
    	}
    	// fix sick strings that are not terminated
   		if( in_string ) result.append('\"');
   		
    	if(!isCommentLine(line)&&!turnOffReplacing) {
    		replacedTail.setLength(0);
			  for(CoupleValue c: rootReplace) {
				  tryReplace(c);
			  }
			  for(CoupleValue c: copyStack.peek().replace) {
				  tryReplace(c);
			  }
			  replacedTail.insert(0,line);line=replacedTail.toString();
    	} 
    	output.println(removeTrailingSpaces( line));
  	}
  
  /**
   * Normalize an input line to the TANDEM format.
   * @param line the input line.
   * @return a COBOL source line in Tandem format.
   */

  private final String normalizeLine( String ln ) {
	
	  if(ln==null||ln.trim().length()<=0||
			  ln.trim().charAt(0)=='$')
		  return ("       ");
  
	ln=ln.
		replace("\t", RESConfig.getInstance().getTabSpaces()).
		replace("\r", "").
		replace("\n", "");
		//replaceAll("[\u0000-\u001f\u0080-\u00ff]"," ");

    switch( source_format ) {
      case FORMAT_FIXED :
    	  if(ln.length()>6)
    		  if(ln.length()>=72)
    			  ln= "      "+ln.substring( 6, 72 );
    		  else
    			  ln= "      "+ln.substring(6);
   		  else
    			  ln= "       ";
    	  break;
      case FORMAT_VARIABLE :
        ln= ln.substring( 0 )+" ";
      default :
    }
    return ln;
  }

  /**
   * Outputs a line (which is not necessary a comment) as a comment.
   * @param line the line.
   */

  private final void outputAsComment( String line ) {
	  if(source_format==Preprocessor.FORMAT_FIXED) {
		    output.print( "      *>" );
	  		output.println( line.substring(7) );
	  }
      else {
    	    output.print( "*> " );
    	    output.println( line.substring(1) );
      }
  }

  /**
   * Outputs a comment line.
   * @param line the line.
   */

  private final void outputComment( String line ) {
	  if(source_format==Preprocessor.FORMAT_FIXED) {
		    output.print( "      *>" );
	  		output.println( line.substring(7) );
	  }
    else {
  	    output.print( "*> " );
  	    output.println( line.substring(1) );
    }
  }

  //Process Continued Line
  //@Author Venkat
  private final String processContinuedLine(String line,String laLine) {
	  if(line==null||laLine==null)
		  return line;
	  if(source_format==Preprocessor.FORMAT_FIXED) {
		  String temp1=line;//.trim();
		  String temp2=(laLine.length()>72)?laLine.substring(6,72):
			  ((laLine.length()>=7)?laLine.substring(7):"")  ;
		  //temp2=temp2.trim();

		  char c;
		  do{
			  int i=temp1.indexOf('\"');
			  int j=temp1.indexOf('\'');
			  int k;
			  if(i<0)
				  if(j<0) {
					  c=' ';k=i;
					  break;//No quotes
				  } else {
					  c='\'';k=j;
				  }
			  else
				  if(j<0) {
					  c='\"';k=i;
				  } else {
					  if(i<j) {
						  c='\"';k=i;
					  } else {
						  c='\'';k=j;
					  }
				  }
			  if(k==temp1.length()) break;
			  temp1=temp1.substring(k+1);
			  int l;
			  if((l=temp1.indexOf(c))>=0)
				  temp1=temp1.substring(l+1);
			  else
				  break;//c has the unmatched quotes
		  } while(true);
		  temp2=removeLeadingSpaces(temp2);
		  if(c!=' '||(c=line.charAt(line.length()-1))=='\"'||c=='\'') {
			  char c2=temp2.charAt(0);
			  if(c2==c)
				  temp2=temp2.substring(1);
			  return line + temp2;
		  } else {
			  return removeTrailingSpaces(line)+temp2;
		  }
		
	  }
	  return line;
  }
  
  private String removeLeadingSpaces(String line) {
	  int i;
	  for(i=0;i<line.length();++i)
		  if(line.charAt(i)!=' '&&line.charAt(i)!='\r'&&line.charAt(i)!='\n'&&line.charAt(i)!='\t')
			  break;

	  return line.substring(i);
  }
  
  private boolean linesMerged=false;
  private StringBuffer replacedTail=new StringBuffer();
  private boolean linesReplaced = false;
  private ArrayList<String> replacedLines=new ArrayList<String>();
  private ArrayList<String> rawUnreplacedLines=new ArrayList<String>();
  private ArrayList<String> commentLines = new ArrayList<String>();
  private StringBuffer replaceWorkspace=new StringBuffer();
  private final void readLine() throws IOException{
	
	  linesReplaced=false;
	 if(replacedLines.size()>0) {
		 line=replacedLines.get(0);
		  replacedLines.remove(0);
		  linesReplaced=true;
		  return;
	  } else 
	  if(rawUnreplacedLines.size()>0){
		  line=rawUnreplacedLines.get(0);
		  rawUnreplacedLines.remove(0);
		  return;
	  }
	  else  
	  if(copyStack.peek().getLookAheadLine()==null) {
		  line =  input.readLine() ;
		  if(line==null) 
			  return;
	  }
	  else {
		  line=copyStack.peek().getLookAheadLine();
		  copyStack.peek().setLookAheadLine(null);
	  }
	  
	  line=normalizeLine(line);
	  savedPartialLine.setLength(0);
	  ++line_number;
     
	  copyStack.peek().setLookAheadLine(input.readLine());
	  
      char indicator_field;
      if(copyStack.peek().getLookAheadLine()!=null) {
    	  copyStack.peek().setLookAheadLine(normalizeLine( copyStack.peek().getLookAheadLine() ));
	      //Free format has no continuation line processing yet or ever.
	      if(source_format==Preprocessor.FORMAT_FIXED)
	    	  try {
	    	  indicator_field = copyStack.peek().getLookAheadLine().charAt(6);
	    	  }catch(Exception e) {
	    		e.printStackTrace();  
	    		return;
	    	  }
	      else
	    	  indicator_field = copyStack.peek().getLookAheadLine().charAt(0);
	      if(copyStack.peek().getLookAheadLine()!=null&&indicator_field=='-') {
		      while(copyStack.peek().getLookAheadLine()!=null&&indicator_field=='-'){
		    	  line=processContinuedLine(line,copyStack.peek().getLookAheadLine());
		    	  ++line_number;
		    	  copyStack.peek().setLookAheadLine(input.readLine());
		    	  if(copyStack.peek().getLookAheadLine()!=null) {
			    	  copyStack.peek().setLookAheadLine(normalizeLine( 
			    			  copyStack.peek().getLookAheadLine() ));
		    	      if(source_format==Preprocessor.FORMAT_FIXED)
		    	    	  indicator_field =  copyStack.peek().getLookAheadLine().charAt(6);
		    	      else
		    	    	  indicator_field =  copyStack.peek().getLookAheadLine().charAt(0);
		    	  }
		      }
		      linesMerged=true;
	      }
      } else
		  if(line==null) 
			  throw new IOException();
  	//Replacing
  }

  	private static final String MATCH_DELIMITERS = "(\\s|,|;|\\.|\\\'|\\\"|%%%|@@@)+";
  	private static final String WORD_DELIMITERS = "( \r\n\t\f,;.\'\"()+";
  	
	private void tryReplace(CoupleValue keyValue) throws IOException {

			if(keyValue==null) return;
		
			replaceWorkspace.setLength(0);

		  Matcher matcher;
		  if(keyValue.noOfKeyTokens<=1){
			  if(replacedTail.length()>0) {
				  replaceWorkspace.append(replacedTail.toString());
			  }
			  else {
				  replaceWorkspace.append(line);line="";
			  }
			 matcher=keyValue.key.matcher(replaceWorkspace.toString());
			 StringBuffer matchedPartOfLine = new StringBuffer();
			 while (matcher.find()) {
				 if((matcher.start()<=0||WORD_DELIMITERS.indexOf(replaceWorkspace.charAt(matcher.start()-1))>=0)&&
						 (matcher.end()>=replaceWorkspace.length()||WORD_DELIMITERS.indexOf(replaceWorkspace.charAt(matcher.end()))>=0)) {
					 matcher.appendReplacement(matchedPartOfLine, keyValue.value);
				 }
			   }
			 if(replacedTail.length()<=0)
				 linesReplaced=true;
			 else {
				 //matchedPartOfLine.append(replacedTail);
				 replacedTail.setLength(0);
			 }
			 matcher.appendTail(replacedTail);
		 	 line+=matchedPartOfLine.toString(); 
		  } else {
			  replaceWorkspace.setLength(0);
			  if(replacedTail.length()>0) {
				  replaceWorkspace.append(replacedTail.toString());
				  replacedTail.setLength(0);
			  }
			  else
				 replaceWorkspace.append(line);
			  replaceWorkspace.append("%%%");
			  replacedTail.setLength(0);
			  int lineWC=countNonEmpty(line.split("(\\s|,|;)+"));
			  int noTokens=keyValue.noOfKeyTokens+lineWC;
			  commentLines=new ArrayList<String>();
			  while (lineWC<noTokens&&!linesReplaced) {
				  readLine();
				  if(line==null)  break;
				  if(include_debug_lines&&source_format==FORMAT_FIXED&&line.charAt(6)=='D')
					  line=line.substring(0,6)+"@@@"+line.substring(7);
				  if(!isCommentLine(line)) {
					  lineWC+=countNonEmpty(line.split("(\\s|,|;)+"));
					  replaceWorkspace.append(line).append("%%%");}
				  else commentLines.add(line);
				  
			  };
			  //line=replaceWorkspace.toString();
			  matcher=keyValue.key.matcher(replaceWorkspace.toString());
			  StringBuffer localLine = new StringBuffer();
			  while (matcher.find()) {
				 if((matcher.start()<=0||WORD_DELIMITERS.indexOf(replaceWorkspace.charAt(matcher.start()-1))>=0)&&
						 (matcher.end()>=replaceWorkspace.length()||WORD_DELIMITERS.indexOf(replaceWorkspace.charAt(matcher.end()))>=0)) {
					 matcher.appendReplacement(localLine, keyValue.value);
					 linesReplaced=true;
				 }
			   }
  			matcher.appendTail(replacedTail);int i;
  			if((i=replacedTail.indexOf("%%%"))>=0) {
	  			postprocessReplace(replacedTail.substring(i+3),false);
	  			replacedTail.delete(i,replacedTail.length());
	  		}
	  		if((i=localLine.indexOf("%%%"))>=0)
	  		{
	  	
	  			replacedTail.insert(0,localLine.substring(i+3));
	  			localLine.delete(i+3,localLine.length());
	  			postprocessReplace(replacedTail.toString(),true);
	  			replacedTail.setLength(0);
	  		} else {
	  			linesReplaced=false;
	  		}
	  		
	  		line=localLine.toString().replace("%%%","");
		  } 
	}

	private int countNonEmpty(String[] arr){
		int ret=0;
		for(String s:arr)
			if(s!=null&&s.length()>0) ret++;
		return ret;
	}
	
	private void postprocessReplace(String str,boolean replaced) {
		
		  String[] lArr=str.replace("@@@", "D").split("%%%");
		  if(replaced){
			  for(int i=0;i<lArr.length;++i){
				  if(lArr[i].length()<=0) continue;
				  replacedLines.add(lArr[i]);
			  }
		  } else {
			  for(int i=lArr.length-1;i>=0;--i){
				  if(lArr[i].length()<=0) continue;
				  rawUnreplacedLines.add(0, lArr[i]);
			  }
		  }
		  
	}
  
  private String escapeRegEx(String regex) {
	  return regex.replace("\\", "\\\\").replace("*", "\\*").replace(".", "\\.").replace("(", "\\(").
	  		replace(")", "\\)").replace("-", "\\-").replace("\"", "\\\"").replace("'", "\\\'").
	  		replace("+", "\\+");
  }
  
  /**
   * The preprocessor mainline, called recursively when processing COPY.
   * @throws IOException
   */
  private final void preprocessMainline()  {
    output.flush();
    try {
		readLine();
	
    
    while( line != null ) {
    
      char indicator_field;
      
      if(source_format==Preprocessor.FORMAT_FIXED)
    	  indicator_field = line.charAt(6);
      else 
    	  if(line.charAt(0)=='*')
    		  indicator_field = '*';
    	  else
    		  indicator_field = ' ';
      switch( indicator_field ) {
        case 'D' :
        case 'd' :
          if( include_debug_lines ) 
        	  ;//processCopyReplace(  );
          else outputAsComment( line );
          break;
        case ' ' :
        case '-' :
          processCopyReplace(  );
          break;
        case '*' :
          outputComment( line );
          break;
        default :
        	if(source_format==FORMAT_FIXED)
        		System.out.println("Warning: Invalid character " +indicator_field +
        				" in column 7 of line "+line_number+". Treated as Comment.");
          //line=removeIndicator(line);
          //processCopyReplace( );
          outputAsComment( line );
          break;
      }
      
      if(linesMerged) {
    	  output.println( "# " + ++line_number + " \""+ copyStack.peek().copyName  +"\"  " );
    	  linesMerged=false;
      }
      output.flush();
      readLine();
    }
    } catch (IOException e) {
		
	}
  }

  /*
  private String removeIndicator(String ln){
	  return ln.substring(0,6)+' '+line.substring(7);
  }
  */
  /**
   * The preprocessor main entry point.
   * @param in_path Path to input file.
   * @param out_pipe Pipe to output file.
   */

public void preprocessToPipe( String in_path, PipedWriter out_pipe, boolean verbose ) {
    try {
      File in_file = new File( in_path );
      source_path = in_file.getParentFile();
      input = new BufferedReader( new FileReader( in_file ) );
      if( out_pipe != null ) {
        output = new PrintWriter(  out_pipe );
      }
      else {
        output = new PrintWriter( System.out );
      }
      if(verbose) 
    	  System.out.println("Processing COBOL file \"" + in_path + "\"." );
      output.println( "# " + 1 + " \""+ in_file.getCanonicalPath()+"\"  " );
      CopyClass copyClass=new CopyClass();
      copyClass.last_line_number=1;
      copyClass.copyName=in_file.getCanonicalPath();
      copyStack.push(copyClass);
      preprocessMainline();
      if(verbose) 
    	  System.out.println("Done, " + line_number + " lines processed." );
      input.close();
      //output.close();
    }
    catch( Exception ex ) {
      if( line_number > 0 ) System.err.println("Exception at line " + line_number + " : " + line );
      ex.printStackTrace();
      return;
    }
  }
  
  /**
   * The preprocessor main entry point.
   * @param in_path Path to input file.
   * @param out_path Path to output file.
   */

public void preprocess( String in_path, String out_path ) {
    try {
      init();
      File in_file = new File( in_path );
      source_path = in_file.getParentFile();
      input = new BufferedReader( new FileReader( in_file ) );
      if( out_path != null ) {
        output = new PrintWriter( new FileWriter( out_path ) );
      }
      else {
        output = new PrintWriter( System.out );
      }
      output.println( "# " + 1 + " \""+ in_file.getCanonicalPath()+"\"  " );
      CopyClass copyClass=new CopyClass();
      copyClass.last_line_number=1;
      copyClass.copyName=in_file.getCanonicalPath();
      copyStack.push(copyClass);
      preprocessMainline();
      input.close();
      if( out_path != null ) 
    	  output.close();
    }
    catch( Exception ex ) {
      if( line_number > 0 ) System.err.println("Exception at line " + line_number + " : " + line );
      ex.printStackTrace();
      return;
    }
  }

  /**
   * @return the last line number.
   */

  public int getLineNumber() {
    return line_number;
  }

  /**
   * @return the source format.
   */

  public int getSourceFormat() {
    return source_format;
  }

  /**
   * Sets the source format.
   * @param source_format the new format.
   */

  public void setSourceFormat(int source_format) {
    this.source_format = source_format;
  }

  /**
   * @return true if we include debug lines
   */

  public boolean includesDebugLines() {
    return include_debug_lines;
  }

  /**
   * @param include_debug_lines if true, includes lines marked with "D".
   */

  public void setIncludeDebugLines(boolean include_debug_lines) {
    this.include_debug_lines = include_debug_lines;
  }

  /**
   * Display program usage and exit. Why do I have this impression of
   * repeating myself?
   */

  private void init() {
    source_path=null;
    input=null;
    output=null;
    line="";
    line_number=0;
    rootReplace.clear();
    copyStack.clear();
  }

  private static final void displayProgramUsageAndExit() {
    System.err.println("Usage : java Preprocessor [<options>] <in_path> [<out_path>]");
    System.err.println("<options> : preprocessor options :");
    System.err.println("Input format option : -F (fixed) -V (variable) -T (tandem)");
    System.err.println("Debug option : -D (include debug lines)");
    System.err.println("<in_path> : COBOL 85 fixed format source path.");
    System.err.println("<out_path> : output path. If omitted, stdout is used.");
    System.exit(0);
  }

  /**
   * A simple mainline.
   * <p>Usage : <code>java Preprocessor [&lt;options&gt;]&lt;in_path&gt; [&lt;out_path&gt;]</code>
   * <br>where :<ul>
   * <li><code>&lt;options&gt;</code> : preprocessor options :<br>
   * Input format option : -F (fixed) -V (variable) -T (tandem)<br>
   * Debug option : -D (include debug lines)
   * <li><code>&lt;in_path&gt;</code> : COBOL 85 source path.
   * <li><code>&lt;out_path&gt</code> : output path. If omitted, stdout is used.
   * </ul>
   * @param args Command-line arguments.
   */

  public static void main(String[] args) {

	  if( args.length == 0 ) displayProgramUsageAndExit();

    Preprocessor preprocessor = new Preprocessor();
    int i;

    // process command line options
    for( i = 0; i < args.length; i++ ) {
      if( !args[i].startsWith("-") ) break;
      switch( args[i].charAt(1) ) {
        case 'F' : preprocessor.setSourceFormat( FORMAT_FIXED ); break;
        case 'V' : preprocessor.setSourceFormat( FORMAT_VARIABLE ); break;
        case 'T' : preprocessor.setSourceFormat( FORMAT_TANDEM ); break;
        case 'D' : preprocessor.setIncludeDebugLines( true ); break;
        default : displayProgramUsageAndExit();
      }
    }

    // process command line arguments
    String source = args[i++];
    String dest = ( args.length > i ? args[i] : null );

    // preprocess
    preprocessor.preprocess( source, dest );

    // bye
    System.exit(0);
  }
}