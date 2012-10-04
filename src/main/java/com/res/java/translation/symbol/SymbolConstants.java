package com.res.java.translation.symbol;

import com.res.java.lib.Constants;

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

public class SymbolConstants {

	//Type keys
	public static final short PROGRAM = 0;
	public static final short DATA = 1;
	public static final short PARAGRAPH = 2;
	public static final short SECTION = 3;
	public static final short FILE = 4;
	public static final short DUMMY = 5;
	

	private static final String[] TYPES = {"byte","char","short","int","long","float","double",
		"BigDecimal","String","byte[]","Object","Object"};
	private static final String[] TYPES_O = {"Byte","Char","Short","Integer","Long","Float","Double",
		"BigDecimal","String","byte[]","Object","Object"};
	private static final String[] SQL_TYPES = {"Byte","Char","Short","Int","Long","Float","Double",
		"BigDecimal","String","Bytes","Object","Object"};
	private static final String[] SQL_TYPES_LONG = {"java.sql.Types.INTEGER",
		"java.sql.Types.INTEGER","java.sql.Types.INTEGER",
		"java.sql.Types.INTEGER","java.sql.Types.LONGVARBINARY",
		"java.sql.Types.DECIMAL","java.sql.Types.DECIMAL","java.sql.Types.DECIMAL",
		"java.sql.Types.VARCHAR","java.sql.Types.JAVA_OBJECT",
		"java.sql.Types.JAVA_OBJECT","java.sql.Types.JAVA_OBJECT"};
	
	private static final String[] FUNCTION_NAMES = new String[]{
			"aCos","annuity","aSin","aTan","_char",
			"cos","currentDate","dateOfInteger","dateToYYYYMMDD","dateVal",	
			"dayOfInteger","dayToYYYYDDD","displayOf","factorial","integer",
			"integerOfDate","integerOfDay","integerPart","length","log",
			"log10","lowerCase","max","mean","median",
			"midrange","min","mod",	"nationalOf","numVal",
			"numValC","ord","ordMax","ordMin","presentValue",
			"random","range","rem","reverse","sin",
			"sqrt","standardDeviation","sum","tan","undate",
			"upperCase","variance","whenCompiled","yearToYYYY","yearWindow",
			"MAX_VALUE","MIN_VALUE"};

	private static final int[][] FUNCTION_ARG_TYPES = new int[][]{
		{Constants.DOUBLE,Constants.DOUBLE},{Constants.DOUBLE,Constants.DOUBLE},{Constants.DOUBLE,Constants.DOUBLE},{Constants.DOUBLE,Constants.DOUBLE},{Constants.CHAR,Constants.INTEGER},
		{Constants.DOUBLE,Constants.DOUBLE},{Constants.STRING,Constants.STRING},{Constants.LONG,Constants.LONG},{Constants.STRING,Constants.INTEGER},{Constants.STRING,Constants.OBJECT},	
		{Constants.LONG,Constants.LONG},{Constants.STRING,Constants.INTEGER},{Constants.STRING,Constants.OBJECT},{Constants.INTEGER,Constants.INTEGER},{Constants.INTEGER,Constants.DOUBLE,Constants.BIGDECIMAL},
		{Constants.LONG,Constants.LONG},{Constants.INTEGER,Constants.INTEGER},{Constants.INTEGER,Constants.DOUBLE,Constants.BIGDECIMAL},{Constants.INTEGER,Constants.CHAR,Constants.STRING},{Constants.DOUBLE,Constants.DOUBLE,Constants.BIGDECIMAL},
		{Constants.DOUBLE,Constants.DOUBLE,Constants.BIGDECIMAL},{Constants.STRING,Constants.CHAR,Constants.STRING},{-Constants.DOUBLE,Constants.DOUBLE,Constants.BIGDECIMAL,Constants.STRING},{Constants.DOUBLE,Constants.DOUBLE,Constants.BIGDECIMAL},{Constants.DOUBLE,Constants.DOUBLE,Constants.BIGDECIMAL},
		{Constants.DOUBLE,Constants.DOUBLE,Constants.BIGDECIMAL},{-Constants.DOUBLE,Constants.DOUBLE,Constants.BIGDECIMAL,Constants.STRING},{Constants.LONG,Constants.LONG},{Constants.STRING,Constants.OBJECT},{Constants.DOUBLE,Constants.STRING},
		{Constants.DOUBLE,Constants.STRING},{Constants.INTEGER,Constants.CHAR,Constants.INTEGER,Constants.STRING},{Constants.INTEGER,Constants.CHAR,Constants.INTEGER,Constants.STRING},{Constants.INTEGER,Constants.CHAR,Constants.INTEGER,Constants.STRING},{Constants.DOUBLE,Constants.DOUBLE,Constants.BIGDECIMAL},
		{Constants.DOUBLE,Constants.DOUBLE},{Constants.DOUBLE,Constants.DOUBLE},{Constants.DOUBLE,Constants.DOUBLE,Constants.BIGDECIMAL},{Constants.STRING,Constants.STRING},{Constants.DOUBLE,Constants.DOUBLE,Constants.BIGDECIMAL},
		{Constants.DOUBLE,Constants.DOUBLE,Constants.BIGDECIMAL},{Constants.DOUBLE,Constants.DOUBLE},{Constants.DOUBLE,Constants.DOUBLE,Constants.BIGDECIMAL},{Constants.DOUBLE,Constants.DOUBLE,Constants.BIGDECIMAL},{Constants.INTEGER,Constants.OBJECT},
		{Constants.STRING,Constants.CHAR,Constants.STRING},{Constants.DOUBLE,Constants.DOUBLE},{Constants.STRING,Constants.STRING},{Constants.DOUBLE,Constants.LONG,Constants.DOUBLE},{Constants.DOUBLE,Constants.DOUBLE},
		{Constants.CHAR},{Constants.CHAR}};
	
	
	public static String get(int t) {
		if(t<0||t>=Constants.MAX_TYPES) t=Constants.INTEGER;
		return TYPES[t];
	}
	
	public static String get_O(int t) {
		if(t<0||t>=Constants.MAX_TYPES) t=Constants.INTEGER;
		return TYPES_O[t];
	}
	
	
	public static String getSQL(int t) {
		if(t<0||t>=SQL_TYPES.length)return "Int";
		return SQL_TYPES[t];
	}
	
	public static String getSQL2(int t) {
		if(t<0||t>=SQL_TYPES_LONG.length) t=0;
		return SQL_TYPES_LONG[t];
	}
	
	
	public static String getFunction(int f) {
		if(f<0||f>=FUNCTION_NAMES.length) return "integer";
		return FUNCTION_NAMES[f];
	}

	public static int[] getFunctionArgTypes(int f) {
		if(f<0||f>=FUNCTION_NAMES.length) return FUNCTION_ARG_TYPES[Constants.INTEGER];
		return FUNCTION_ARG_TYPES[f];
	}
	
}
