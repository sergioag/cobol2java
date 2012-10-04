package com.res.cobol.parser;
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
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An implementation of interface CharStream, where the stream is assumed to
 * contain only ASCII characters (without unicode processing).
 */

public class RESCharStream implements CharStream
{
/** Whether parser is static. */
  public static final boolean staticFlag = false;
  int bufsize;
   int available;
   int tokenBegin;
/** Position in buffer. */
   public int bufpos = -1;
   protected int bufline[];
   protected int bufcolumn[];
   protected String bufsourcefile[];

   protected int column = 0;
   protected int line = 1;
   protected String fileName="";

   protected boolean prevCharIsCR = false;
   protected boolean prevCharIsLF = false;
  
   protected java.io.Reader inputStream;

   protected char[] buffer;
   protected int maxNextCharInd = 0;
   protected int inBuf = 0;
   protected int tabSize = 8;

   protected void setTabSize(int i) { tabSize = i; }
   protected int getTabSize(int i) { return tabSize; }
 
  protected void ExpandBuff(boolean wrapAround)
  {
    char[] newbuffer = new char[bufsize + 2048];
    int newbufline[] = new int[bufsize + 2048];
    int newbufcolumn[] = new int[bufsize + 2048];
    String newbufsourcefile[] = new String[bufsize + 2048];

    try
    {
    	
      if (wrapAround)
      {
        System.arraycopy(buffer, tokenBegin, newbuffer, 0, bufsize - tokenBegin);
        System.arraycopy(buffer, 0, newbuffer, bufsize - tokenBegin, bufpos);
        buffer = newbuffer;

        System.arraycopy(bufline, tokenBegin, newbufline, 0, bufsize - tokenBegin);
        System.arraycopy(bufline, 0, newbufline, bufsize - tokenBegin, bufpos);
        bufline = newbufline;

        System.arraycopy(bufcolumn, tokenBegin, newbufcolumn, 0, bufsize - tokenBegin);
        System.arraycopy(bufcolumn, 0, newbufcolumn, bufsize - tokenBegin, bufpos);
        bufcolumn = newbufcolumn;

        System.arraycopy(bufsourcefile, tokenBegin, newbufsourcefile, 0, bufsize - tokenBegin);
        System.arraycopy(bufsourcefile, 0, newbufsourcefile, bufsize - tokenBegin, bufpos);
        bufsourcefile = newbufsourcefile;

        maxNextCharInd = (bufpos += (bufsize - tokenBegin));
      }
      else
      {
        System.arraycopy(buffer, tokenBegin, newbuffer, 0, bufsize - tokenBegin);
        buffer = newbuffer;

        System.arraycopy(bufline, tokenBegin, newbufline, 0, bufsize - tokenBegin);
        bufline = newbufline;

        System.arraycopy(bufcolumn, tokenBegin, newbufcolumn, 0, bufsize - tokenBegin);
        bufcolumn = newbufcolumn;

        System.arraycopy(bufsourcefile, tokenBegin, newbufsourcefile, 0, bufsize - tokenBegin);
        bufsourcefile = newbufsourcefile;

        maxNextCharInd = (bufpos -= tokenBegin);
      }
    }
    catch (Throwable t)
    {
      throw new Error(t.getMessage());
    }


    bufsize += 2048;
    available = bufsize;
    tokenBegin = 0;
  }

  protected void FillBuff() throws java.io.IOException
  {
    if (maxNextCharInd == available)
    {
      if (available == bufsize)
      {
        if (tokenBegin > 2048)
        {
          bufpos = maxNextCharInd = 0;
          available = tokenBegin;
        }
        else if (tokenBegin < 0)
          bufpos = maxNextCharInd = 0;
        else
          ExpandBuff(false);
      }
      else if (available > tokenBegin)
        available = bufsize;
      else if ((tokenBegin - available) < 2048)
        ExpandBuff(true);
      else
        available = tokenBegin;
    }

    int i;
    try {
      if ((i = inputStream.read(buffer, maxNextCharInd, available - maxNextCharInd)) == -1)
      {
        inputStream.close();
        throw new java.io.IOException();
      }
      else
        maxNextCharInd += i;
      return;
    }
    catch(java.io.IOException e) {
      --bufpos;
      backup(0);
      if (tokenBegin == -1)
        tokenBegin = bufpos;
      throw e;
    }
  }

/** Start. */
  public char BeginToken() throws java.io.IOException
  {
	  tokenLength=0;
    tokenBegin = -1;
    char c = readChar();
    tokenBegin = bufpos;

    return c;
  }

  private char forceReadChar () throws IOException  {
	   if (++bufpos >= maxNextCharInd) {
		      FillBuff();
	   }
	   return buffer[bufpos];
	  
  }
  private  int tokenLength=0;
  private Pattern fileNamePattern =Pattern.compile("(/[^/:\\*\\?<>\\|]+[\\.\\w{2,6}]?)+|([A-Z]:\\\\[^/:\\*\\?<>\\|]+[\\.\\w{2,6}]?)");

  private  String sourceFileName=null;
  public  String getLastSourceFileName() {
	  return getBeginSourceFile();
  }
  protected void UpdateLineColumn(char c) throws java.io.IOException
  {
	  column++;
	    
	    if (prevCharIsLF)
	    {
	      prevCharIsLF = false;
	      line += (column = 1);
	    }
	    else if (prevCharIsCR)
	    {
	      prevCharIsCR = false;
	      if (c == '\n')
	      {
	        prevCharIsLF = true;
	      }
	      else
	        line += (column = 1);
	    }

	  preprocessorLineStart:	  
	  while(c=='#')  {//Preprocessor line containing line number and file
		  
		  boolean isPreProcessorError=false;
		  int prevBufpos=bufpos;
		  
		  char c2=forceReadChar();
		  
		   while(c2==' ') {
			   c2=forceReadChar();
		   };

		  int lineCalc=0;
		  while(c2>='0'&&c2<='9') {
			  lineCalc*=10;
			  switch(c2) {
			  case '0':break;
			  case '1':lineCalc+=1;break;
			  case '2':lineCalc+=2;break;
			  case '3':lineCalc+=3;break;
			  case '4':lineCalc+=4;break;
			  case '5':lineCalc+=5;break;
			  case '6':lineCalc+=6;break;
			  case '7':lineCalc+=7;break;
			  case '8':lineCalc+=8;break;
			  case '9':lineCalc+=9;break;
			  default: bufpos=prevBufpos;break preprocessorLineStart;
			  }
			  c2=forceReadChar();
		   } ;

		   while(c2==' ') {
			   c2=forceReadChar();
		   };

		   
		   StringBuffer fileName=new StringBuffer("");
		   int numQuote=0;
		   
		   while(c2!='\r'&&c2!='\n'){
			   if(numQuote>2){bufpos=prevBufpos;break preprocessorLineStart;}
			  switch(c2) {
			  case '\"':
				  ++numQuote;
				  break;
			  case ' ':break;
			  default:
				  if(numQuote==1)
						  fileName.append(c2);
				  else
					  isPreProcessorError=true;
			  }
			   c2=forceReadChar();
		   } ;
		   
		   
		   Matcher matcher = fileNamePattern.matcher(fileName.toString());
		   isPreProcessorError=!(matcher.matches());
		   if (isPreProcessorError||numQuote>2) {
			   bufpos=prevBufpos;break preprocessorLineStart;
		   }
		   
		   while(c2=='\r'||c2=='\n')
			   c2=forceReadChar();
		   if (bufpos >= prevBufpos) {
			  	  for(int pos=prevBufpos;bufpos > pos;++pos) {
			  		  deleteFromBuffer(pos);
			  	  }
	    	} else {
		  	  	for(int pos=prevBufpos;bufsize > pos;++pos) {
		  	  		deleteFromBuffer(pos);
		  	  	}
		  	  	for(int pos=0;bufpos > pos;++pos) {
		  	  		deleteFromBuffer(pos);
		  	  	}
		       
		   }
		   
		   c=buffer[bufpos];
		   line=lineCalc-1;
	       prevCharIsCR = false;
	       prevCharIsLF = false;
		   column=1;
		   sourceFileName=fileName.toString();
		   
	  }
	  

	  switch (c)
    {
      case '\r' :
        prevCharIsCR = true;
        break;
      case '\n' :
        prevCharIsLF = true;
        break;
      case '\t' :
        column--;
        column += (tabSize - (column % tabSize));
        break;
      default :
        break;
    }

    bufline[bufpos] = line;
    bufcolumn[bufpos] = column;
    bufsourcefile[bufpos] = sourceFileName;
  }
  public void deleteFromBuffer(int pos) {
	  buffer[pos]=0;
  }
  public void deleteLastReadFromBuffer(int posoffset) {
	  buffer[bufpos-posoffset]=0;
  }

/** Read a character. */
  public char readChar() throws java.io.IOException
  {

	 if(++tokenLength>4194303)
		 throw new Error();
    if (inBuf > 0)
    {
      --inBuf;

      if (++bufpos == bufsize)
        bufpos = 0;
      
      return buffer[bufpos];
    }
    
	
    if (++bufpos >= maxNextCharInd){
    		FillBuff();
    }

    char c = buffer[bufpos];

    UpdateLineColumn(c);
    c = buffer[bufpos];

    return c;
  }

  /**
   * @deprecated
   * @see #getEndColumn
   */

  public int getColumn() {
    return bufcolumn[bufpos];
  }

  /**
   * @deprecated
   * @see #getEndLine
   */

  public int getLine() {
    return bufline[bufpos];
  }

  /** Get token end column number. */
  public int getEndColumn() {
    return bufcolumn[bufpos];
  }

  /** Get token end source file. */
   public String getEndSourceFile() {
    return bufsourcefile[bufpos];
  }

  /** Get token end line number. */
  public int getEndLine() {
     return bufline[bufpos];
  }

  /** Get token beginning column number. */
  public int getBeginColumn() {
    return bufcolumn[tokenBegin];
  }

  /** Get token beginning column number. */
   public String getBeginSourceFile() {
    return bufsourcefile[tokenBegin];
  }
  /** Get token beginning line number. */
  public int getBeginLine() {
    return bufline[tokenBegin];
  }

  public  int getRESBeginLine() {
	    return bufline[tokenBegin];
  }
  
/** Backup a number of characters. */
  public void backup(int amount) {

    inBuf += amount;
    if ((bufpos -= amount) < 0)
      bufpos += bufsize;
  }

  /** Constructor. */
  public RESCharStream(java.io.Reader dstream, int startline,
  int startcolumn, int buffersize)
  {
   // if (inputStream != null)
      //throw new Error("\n   ERROR: Second call to the constructor of a static SimpleCharStream.\n" +
      //"       You must either use ReInit() or set the JavaCC option STATIC to false\n" +
      //"       during the generation of this class.");
    inputStream = dstream;
    line = startline;
    column = startcolumn - 1;

    available = bufsize = buffersize;
    buffer = new char[buffersize];
    bufline = new int[buffersize];
    bufcolumn = new int[buffersize];
    bufsourcefile = new String[buffersize];
  }

  /** Constructor. */
  public RESCharStream(java.io.Reader dstream, int startline,
                          int startcolumn)
  {
    this(dstream, startline, startcolumn, 4096);
  }

  /** Constructor. */
  public RESCharStream(java.io.Reader dstream)
  {
    this(dstream, 1, 1, 4096);
  }

  /** Reinitialise. *
  public void ReInit(java.io.Reader dstream, int startline,
  int startcolumn, int buffersize)
  {
    inputStream = dstream;
    line = startline;
    column = startcolumn - 1;

    if (buffer == null || buffersize != buffer.length)
    {
      available = bufsize = buffersize;
      buffer = new char[buffersize];
      bufline = new int[buffersize];
      bufcolumn = new int[buffersize];
      bufsourcefile = new String[buffersize];
    }
    prevCharIsLF = prevCharIsCR = false;
    tokenBegin = inBuf = maxNextCharInd = 0;
    bufpos = -1;
  }

  /** Reinitialise. *
  public void ReInit(java.io.Reader dstream, int startline,
                     int startcolumn)
  {
    ReInit(dstream, startline, startcolumn, 4096);
  }

  /** Reinitialise. *
  public void ReInit(java.io.Reader dstream)
  {
    ReInit(dstream, 1, 1, 4096);
  }
  /** Constructor. */
  public RESCharStream(java.io.InputStream dstream, String encoding, int startline,
  int startcolumn, int buffersize) throws java.io.UnsupportedEncodingException
  {
    this(encoding == null ? new java.io.InputStreamReader(dstream) : new java.io.InputStreamReader(dstream, encoding), startline, startcolumn, buffersize);
  }

  /** Constructor. */
  public RESCharStream(java.io.InputStream dstream, int startline,
  int startcolumn, int buffersize)
  {
    this(new java.io.InputStreamReader(dstream), startline, startcolumn, buffersize);
  }

  /** Constructor. */
  public RESCharStream(java.io.InputStream dstream, String encoding, int startline,
                          int startcolumn) throws java.io.UnsupportedEncodingException
  {
    this(dstream, encoding, startline, startcolumn, 4096);
  }

  /** Constructor. */
  public RESCharStream(java.io.InputStream dstream, int startline,
                          int startcolumn)
  {
    this(dstream, startline, startcolumn, 4096);
  }

  /** Constructor. */
  public RESCharStream(java.io.InputStream dstream, String encoding) throws java.io.UnsupportedEncodingException
  {
    this(dstream, encoding, 1, 1, 4096);
  }

  /** Constructor. */
  public RESCharStream(java.io.InputStream dstream)
  {
    this(dstream, 1, 1, 4096);
  }

  /** Reinitialise. *
  public void ReInit(java.io.InputStream dstream, String encoding, int startline,
                          int startcolumn, int buffersize) throws java.io.UnsupportedEncodingException
  {
    ReInit(encoding == null ? new java.io.InputStreamReader(dstream) : new java.io.InputStreamReader(dstream, encoding), startline, startcolumn, buffersize);
  }

  /** Reinitialise. *
  public void ReInit(java.io.InputStream dstream, int startline,
                          int startcolumn, int buffersize)
  {
    ReInit(new java.io.InputStreamReader(dstream), startline, startcolumn, buffersize);
  }

  /** Reinitialise. *
  public void ReInit(java.io.InputStream dstream, String encoding) throws java.io.UnsupportedEncodingException
  {
    ReInit(dstream, encoding, 1, 1, 4096);
  }

  /** Reinitialise. *
  public void ReInit(java.io.InputStream dstream)
  {
    ReInit(dstream, 1, 1, 4096);
  }
  /** Reinitialise. *
  public void ReInit(java.io.InputStream dstream, String encoding, int startline,
                     int startcolumn) throws java.io.UnsupportedEncodingException
  {
    ReInit(dstream, encoding, startline, startcolumn, 4096);
  }
  /** Reinitialise. *
  public void ReInit(java.io.InputStream dstream, int startline,
                     int startcolumn)
  {
    ReInit(dstream, startline, startcolumn, 4096);
  }
  /** Get token literal value. */
  public String GetImage()
  {
	  StringBuffer image=new StringBuffer(32);
	 
	    	
	    if (bufpos >= tokenBegin) {
	  	  for(int pos=tokenBegin;bufpos >= pos;++pos) {
	  		  if(buffer[pos]==0) 
	  			  continue;
	  		  image.append(buffer[pos]);
	  	  }
	    	//return new String(buffer, tokenBegin, bufpos - tokenBegin + 1);
	    } else {
		  	  for(int pos=tokenBegin;bufsize > pos;++pos) {
		  		  if(buffer[pos]==0) 
		  			  continue;
		  		  image.append(buffer[pos]);
		  	  }
		  	  for(int pos=0;bufpos >= pos;++pos) {
		  		  if(buffer[pos]==0) 
		  			  continue;
		  		  image.append(buffer[pos]);
		  	  }
		       // return new String(buffer, tokenBegin, bufsize - tokenBegin) +
		        //	new String(buffer, 0, bufpos + 1);
	    }
	    return image.toString();
  }

  /** Get the suffix. */
  public char[] GetSuffix(int len)
  {
    char[] ret = new char[len];

    if ((bufpos + 1) >= len)
      System.arraycopy(buffer, bufpos - len + 1, ret, 0, len);
    else
    {
      System.arraycopy(buffer, bufsize - (len - bufpos - 1), ret, 0,
                                                        len - bufpos - 1);
      System.arraycopy(buffer, 0, ret, len - bufpos - 1, bufpos + 1);
    }

    return ret;
  }

  /** Reset buffer when finished. */
  public void Done()
  {
    buffer = null;
    bufline = null;
    bufcolumn = null;
    bufsourcefile=null;
  }

  /**
   * Method to adjust line and column numbers for the start of a token.
   */
  public void adjustBeginLineColumn(int newLine, int newCol)
  {
    int start = tokenBegin;
    int len;

    if (bufpos >= tokenBegin)
    {
      len = bufpos - tokenBegin + inBuf + 1;
    }
    else
    {
      len = bufsize - tokenBegin + bufpos + 1 + inBuf;
    }

    int i = 0, j = 0, k = 0;
    int nextColDiff = 0, columnDiff = 0;

    while (i < len && bufline[j = start % bufsize] == bufline[k = ++start % bufsize])
    {
      bufline[j] = newLine;
      nextColDiff = columnDiff + bufcolumn[k] - bufcolumn[j];
      bufcolumn[j] = newCol + columnDiff;
      columnDiff = nextColDiff;
      i++;
    }

    if (i < len)
    {
      bufline[j] = newLine++;
      bufcolumn[j] = newCol + columnDiff;

      while (i++ < len)
      {
        if (bufline[j = start % bufsize] != bufline[++start % bufsize])
          bufline[j] = newLine++;
        else
          bufline[j] = newLine;
      }
    }

    line = bufline[j];
    column = bufcolumn[j];
  }

}
/* JavaCC - OriginalChecksum=86ebed92ef21fc5361bd5da0e380c3c5 (do not edit this line) */
