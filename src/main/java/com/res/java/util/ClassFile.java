package com.res.java.util;
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
import java.io.PrintWriter;
import java.util.Stack;

import com.res.java.translation.symbol.SymbolProperties;
import com.res.java.translation.symbol.SymbolTable;

public class ClassFile {
	
	public static ClassFileWriter current=null;
	public static Stack<ClassFileWriter> files=new Stack<ClassFileWriter>();
	
	public PrintWriter out = null;
	public boolean importJavaUtil = false;
	public boolean importJavaIo = false;
	
	public static void doProgramScope(SymbolProperties props, boolean dataLevel) {
		if (files==null)
			files=new Stack<ClassFileWriter>();
		if(dataLevel)
			current=new DataClassFileWriter(props);
		else  {
			if(SymbolTable.getScope().isCurrentProgramTopLevel()) {			
			current=new ProgramClassFileWriter(props);
			} else
				((ProgramClassFileWriter)current).printHeader(props,true);
		}
		files.push(current);
	}

	public static void doProgramScope(ClassFileWriter gen) {
		current=gen;
		files.push(current);
	}

	public static void endProgramScope() {
		if(current==null)
			return;
		current.println("}");
		current.println("}",true);
		if(files.peek().isData()||SymbolTable.getScope().isCurrentProgramTopLevel()) {			
		current.close();
		
		} else 
			current.backTab();
		files.pop();
		if(files.size()>0) {
			current=files.peek();
		} else {
			current=null;
		}
	}

	public static void doMethodScope(String line,boolean bi) {
		if(current==null)
			return;
		current.println(line,bi);
	}

	public static void endMethodScope(boolean bi) {
		if(current==null)
			return;
		current.println("}",bi);
	}

	public static void println(String line,boolean bi) {
		if(current==null)
			return;
		current.println(line,bi);
	}
	
	public static void print(String line,boolean bi) {
		if(current==null)
			return;
		current.print(line,bi);
	}
	
	public static void printPlainln(String line,boolean bi) {
		if(current==null)
			return;
		current.println(line,bi);
	}
	
	public static void printPlain(String line,boolean bi) {
		if(current==null)
			return;
		current.print(line,bi);
	}
	
	public static void printPlain(char c,boolean bi) {
		if(current==null)
			return;
		current.printPlain(c,bi);
	}

	public static void tab(boolean bi) {
		if(current==null)
			return;
		current.tab(bi);
	}
	
	public static void backTab(boolean bi) {
		if(current==null)
			return;
		current.backTab(bi);
	}
	
	////
	
	public static void doMethodScope(String line) {
		if(current==null)
			return;
		current.println(line);
	}

	public static void endMethodScope() {
		if(current==null)
			return;
		current.println("}");
	}

	public static void println(String line) {
		if(current==null)
			return;
		current.println(line);
	}
	
	public static void print(String line) {
		if(current==null)
			return;
		current.print(line);
	}
	
	public static void printPlainln(String line) {
		if(current==null)
			return;
		current.println(line);
	}
	
	public static void printPlain(String line) {
		if(current==null)
			return;
		current.print(line);
	}
	
	public static void printPlain(char c) {
		if(current==null)
			return;
		current.printPlain(c);
	}

	public static void tab() {
		if(current==null)
			return;
		current.tab();
	}
	
	public static void backTab() {
		if(current==null)
			return;
		current.backTab();
	}


	
	
}
