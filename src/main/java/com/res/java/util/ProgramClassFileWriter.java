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

import java.util.ArrayList;

import com.res.cobol.Main;
import com.res.common.RESConfig;
import com.res.java.translation.symbol.SymbolConstants;
import com.res.java.translation.symbol.SymbolProperties;

public class ProgramClassFileWriter extends ClassFileWriter {
	
	public ProgramClassFileWriter(SymbolProperties props) {
		super(
				Files.openClassFile(props,false)
			);
		super.setClassName(NameUtil.getClassName(props.getDataName(),"",false));
		setData(false);
		if(RESConfig.getInstance().isGenerateBeanInfo()) {
			setBeanInfoPrintStream(Files.openBeanInfoFile(props,false));
		}
		printHeader(props,false);
	}
	public void printHeader(SymbolProperties props,boolean isNested) {
		if(!isNested) {
			String s;
			if ((s = NameUtil.getPackageName(props,true))!=null&&s.length()>0) {
					//s+=NameUtil.getJavaName(props,false);
					println("package "+s+";");
					println("package "+s+";",true);
				}
			println("/************************************************************************");
			println(" ** RES generated this class file from Cobol program "+
					props.getDataName().toUpperCase()+" in source file "+Main.getContext().getSourceFileName().toUpperCase());
			println(" ** Generated at time "+RESUtil.getTime()+ 
					" on "+RESUtil.getDayOfWeek()+
					", "+RESUtil.getDate());
			if(Main.getContext().isProcessVerbose()) {
				println(" ** "+Main.getContext().getOptionsVerbose().toString());
			}
			println(" ************************************************************************/");
	
			printImports(props);
			
			println("import com.res.java.lib.*;");
			println("import com.res.java.lib.*;",true);
			println("import com.res.java.lib.exceptions.*;");
			println("import java.math.BigDecimal;");
			if(Main.getContext().isSqlTranslated())
				println("import java.sql.SQLException;");
			println("");
			println("@SuppressWarnings(\"unused\")");
			println("public class "+props.getJavaName2()+" extends Program {");
			println("");
			println("public class "+props.getJavaName2()+"BeanInfo extends RESBeanInfo {",true);
		} else {
			println("/************************************************************************");
			println(" ** RES generated this class from Nested Cobol program "+
					props.getDataName().toUpperCase()+" in source file "+Main.getContext().getSourceFileName().toUpperCase());
			println(" ************************************************************************/");
			println("public class "+props.getJavaName2()+" extends Program {");
		}
		
	}
	private void printImports(SymbolProperties props) {
		ArrayList<SymbolProperties> a = props.getChildren();
		if (a==null) return;
		for(SymbolProperties child : a){
			if(child.getType()==SymbolConstants.FILE||
					child.getType()==SymbolConstants.PROGRAM) {
				printImports(child);
			}
			else
				printSingleImport(child);
		
		}
	}
	private void printSingleImport(SymbolProperties o2) {
		if(!o2.is01Group()||!(o2.getRef()||o2.getMod())||o2.isFromRESLibrary()) 
			return;
		String name2=o2.getJavaName2();
		println("import "+NameUtil.getPackageName(o2, false)+"."+name2+";");
	}
	
}
