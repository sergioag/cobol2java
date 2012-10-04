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
import java.io.PrintStream;

public class ClassFileWriter {

	private PrintStream out;
	
	private boolean isData;
	
	private PrintStream beanInfoPrintStream = null; 
	
	public ClassFileWriter(PrintStream arg0) {
		out = arg0;
	}
	
	private String className=null;
	private String packageName=null;
	
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setData(boolean isData) {
		this.isData = isData;
	}

	public boolean isData() {
		return isData;
	}

	private short tabs=0;
	
	public ClassFileWriter(String fileName, String suffix, boolean isData) {
		out = Files.openClassFile(NameUtil.getPathName(fileName,suffix,isData));
		className=NameUtil.getClassName(fileName,suffix,isData);
	}
	
	public void tab() {
		tabs++;
	}
	
	public void backTab() {
		if(--tabs<0)
			tabs=0;
	}
	
	public String getTabs() {
		int t;StringBuffer s;
		for (s=new StringBuffer(),t=tabs;t>0;--t) {
			s.append("\t");
		}
		return s.toString();
	}
	
	public void printTabs() {
		for (int t=tabs;t>0;--t) {
			out.print("\t");
		}
	}
	
	public void println(String line) {
		if(out==null) return;
		printTabs();
		out.println(line);
	}
	
	public void println() {
		if(out==null) return;
		out.println();
	}
	
	
	public void print(String line) {
		if(out==null) return;
		printTabs();
		out.print(line);
	}
	
	public void printPlainln(String line) {
		if(out==null) return;
		out.println(line);
	}
	
	public void printPlain(String line) {
		if(out==null) return;
		out.print(line);
	}
	
	public void printPlain(char c) {
		if(out==null) return;
		out.print(c);
	}
	
	public void flush() {
		if(out==null) return;
		out.flush();
	}

	public void setBeanInfoPrintStream(PrintStream beanInfoPrintStream) {
		this.beanInfoPrintStream = beanInfoPrintStream;
	}

	public PrintStream getBeanInfoPrintStream() {
		return beanInfoPrintStream;
	}
	
/////
	private int beanInfoTabs = 0;
	public void tab(boolean bi) {
		if(!bi)	tabs++;
		else beanInfoTabs++;
	}
	
	public void backTab(boolean bi) {
		if(!bi) {if(--tabs<0)tabs=0;}
		else{if(--beanInfoTabs<0)beanInfoTabs=0;}
	}
	
	public String getTabs(boolean bi) {
		int t=tabs;StringBuffer s;
		if(bi) t=beanInfoTabs;
		for (s=new StringBuffer();t>0;--t) {
			s.append("\t");
		}
		return s.toString();
	}
	
	public void printTabs(boolean bi) {
		String tabs = getTabs(bi);
		if(!bi) out.print(tabs);
		else beanInfoPrintStream.print("\t");
			
	}
	
	public void println(String line,boolean bi) {
		if(!bi) {
			if(out==null) return;
			printTabs();
			out.println(line);
		} else {
			if(beanInfoPrintStream==null) return;
			printTabs();
			beanInfoPrintStream.println(line);
		}
	}
	
	public void println(boolean bi) {
		if(!bi) {
			if(out==null) return;
			out.println();
		} else {
			if(beanInfoPrintStream==null) return;
			beanInfoPrintStream.println();
		}
	}
	
	
	public void print(String line,boolean bi) {
		if(!bi) {
			if(out==null) return;
			printTabs();
			out.print(line);
		} else {
			if(beanInfoPrintStream==null) return;
			printTabs();
			beanInfoPrintStream.print(line);
		}
	}
	
	public void printPlainln(String line,boolean bi) {
		if(!bi) {
			if(out==null) return;
			out.println(line);
		} else {
			if(beanInfoPrintStream==null) return;
			beanInfoPrintStream.print(line);

		}
	}
	
	public void printPlain(String line,boolean bi) {
		if(!bi) {
			if(out==null) return;
			out.print(line);
		} else {
			if(beanInfoPrintStream==null) return;
			beanInfoPrintStream.print(line);
		}
	}
	
	public void printPlain(char c,boolean bi) {
		if(!bi) {
			if(out==null) return;
			out.print(c);
		} else {
			if(beanInfoPrintStream==null) return;
			beanInfoPrintStream.print(c);

		}
	}
	
	public void close() {
		if(out==null) return;
		out.close();
		if(beanInfoPrintStream!=null)beanInfoPrintStream.close();
	}
	
	public void flush(boolean bi) {
		if(!bi) {
			if(out==null) return;
			out.flush();
		} else {
			if(beanInfoPrintStream==null) return;
			beanInfoPrintStream.flush();
		}
	}




}
