package com.res.java.lib;
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

public class Constants {

	//Java  types
	public static final int BYTE=0;
	public static final int CHAR=1;
	public static final int SHORT=2;
	public static final int INTEGER=3;
	public static final int LONG=4;
	public static final int FLOAT=5;
	public static final int DOUBLE=6;
	public static final int BIGDECIMAL=7;
	public static final int STRING=8;
	public static final int GROUP=9;
	public static final int OBJECT=10;
	public static final int MAX_TYPES=11;
	public static final int UNKNOWN=MAX_TYPES;
	
	//Basic Cobol Usage Types
	public static final int BINARY=0;
	public static final int PACKED_DECIMAL=1;
	public static final int DISPLAY=2;
	public static final int COMPUTATIONAL1=3;
	public static final int COMPUTATIONAL2=4;
	public static final int FLOATING_POINT=5;
	public static final int COMPUTATIONAL5=6;
	public static final int DISPLAY_1=7;
	public static final int INDEX=8;
	public static final int NATIONAL_U=9;
	public static final int POINTER=10;
	public static final int PROCEDURE_POINTER=11;
	public static final int FUNCTION_POINTER=12;
	
	//Basic Cobol Data Categories
	public static final int ALPHABETIC=0;
	public static final int NUMERIC=1;
	public static final int NUMERIC_EDITED=2;
	public static final int ALPHANUMERIC=3;
	public static final int ALPHANUMERIC_EDITED=3;
	public static final int EXTERNAL_FLOATING_POINT=4;
	public static final int NATIONAL=5;
	public static final int NATIONAL_EDITED=6;
	
	//File Organization Codes
	public static final int SEQUENTIAL = 0;
	public static final int INDEXED = 1;
	public static final int RELATIVE = 2;
	public static final int LINE_SEQUENTIAL = 3;
	
	//Access Mode Codes
	public static final int SEQUENTIAL_ACCESS = 0;
	public static final int RANDOM_ACCESS = 1;
	public static final int DYNAMIC_ACCESS = 2;
	
}
