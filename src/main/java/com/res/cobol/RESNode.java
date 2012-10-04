package com.res.cobol;
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

import com.res.cobol.parser.CobolParserTokenManager;
import com.res.java.translation.symbol.SymbolProperties;

public class RESNode {
	
	public boolean deadStatement;
	@SuppressWarnings("unchecked")
	public ArrayList ref;
	@SuppressWarnings("unchecked")
	public ArrayList mod;
	
	public SymbolProperties props;
	
	public int line;
	
	public String sourceFile;
	public RESNode() {
		sourceFile=Main.getContext().getCharStream().getLastSourceFileName();
		if(CobolParserTokenManager.lastToken!=null)
			line=CobolParserTokenManager.lastToken.beginLine;
		else
			line=Main.getContext().getCharStream().getRESBeginLine();
		if(sourceFile!=null)
			sourceFile=sourceFile.substring(Math.max(sourceFile.lastIndexOf('\\'),
				sourceFile.lastIndexOf('/'))+1);
	}
}
