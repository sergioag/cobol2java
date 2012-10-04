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

import com.res.cobol.Main;
import com.res.common.RESConfig;
import com.res.java.translation.symbol.SymbolConstants;
import com.res.java.translation.symbol.SymbolProperties;

public class DataClassFileWriter extends ClassFileWriter {
	
	public DataClassFileWriter(SymbolProperties props){
		super(Files.openClassFile(props,true));
		setData(true);
		if(RESConfig.getInstance().isGenerateBeanInfo()) {
			setBeanInfoPrintStream(Files.openBeanInfoFile(props,true));
		}
		printHeader(props);
	}
	public void printHeader(SymbolProperties props) {
		String s;
		if ((s = NameUtil.getPackageName(props,false))!=null&&s.length()>0) {
			println("package "+s+";");
			println("package "+s+";",true);
		}
		if(props.getType()==SymbolConstants.PROGRAM) {
			println("/************************************************************************");
			println(" ** RES generated this class file for Data Encapsulation "+
					" of program "+props.getDataName().toUpperCase()+" in source file "
					+Main.getContext().getSourceFileName().toUpperCase());
		} else {
			println("/************************************************************************");
			println(" ** RES generated this class file from Data Level "+
					props.getDataName().toUpperCase()+" in source file "
						+Main.getContext().getSourceFileName().toUpperCase());
		}
		println(" ** Generated at time "+RESUtil.getTime()+ 
				" on "+RESUtil.getDayOfWeek()+
				", "+RESUtil.getDate());
		if(Main.getContext().isProcessVerbose()) {
			println(" ** "+Main.getContext().getOptionsVerbose().toString());
		}
		println(" ************************************************************************/");
	}
	
}
