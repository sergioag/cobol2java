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

public class GeneralClassFileWriter extends ClassFileWriter {

	public GeneralClassFileWriter(String fileName, String suffix, boolean isData){
		super(Files.openClassFile(NameUtil.getPathName(fileName,suffix,isData)));
		setClassName(NameUtil.getClassName(fileName,suffix,isData));
		setData(isData);
		printHeader();
	}
	public void printHeader() {
		println("/************************************************************************");
		println(" ** RES generated this file from a part of Cobol program.");
		println(" ** Generated at time "+RESUtil.getTime()+ 
				" on "+RESUtil.getDayOfWeek()+
				", "+RESUtil.getDate());
		println(" ************************************************************************/");
		println("import com.res.java.lib.*;");
		println("import java.math.BigDecimal;");
		println("");

		println("public class "+getClassName()+" {");
		println("");
	}

}
