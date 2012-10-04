package com.res.java.lib;
/************************************************************************
 **This file automatically generated from Data Level SQLCA 
 **Generated at time 10:04:57.79 on Thursday, 12/10/09
 ************************************************************************/
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

public class Sqlca  {

	public int getSqlcode() {
		return sqlcode;
	}
	public void setSqlcode(int sc) {
		sqlcode=sc;
	}
	public String getSqlwarn0() {
		return sqlstate;
	}
	public void setSqlwarn0(String sw) {
		sqlstate=sw;
	}
	public String getSqlwarn() {
		return sqlstate;
	}
	public void setSqlwarn(String sw) {
		sqlstate=sw;
	}
	
	public String getSqlerrmc() {
		return sqlerrmc;
	}
	public void setSqlerrmc(String se) {
		sqlerrmc=se;
	}
	
	public int getSqlerrd(int idx1) {
		return sqlerrd[--idx1];
	}
	public int incSqlerrd(int idx1) {
		return ++sqlerrd[--idx1];
	}
	public void setSqlerrd(int val,int idx1) {
		sqlerrd[--idx1]=val;
	}
	
	public int sqlcode = 0;

	//private String sqlwarn="";
	
	public String sqlstate="";
	public String sqlerrmc="";

	public int[] sqlerrd=new int[5];

	public boolean notFound() {
		return sqlcode==+100 || sqlstate.equalsIgnoreCase("02000");
	}
	
	public boolean sqlError() {
		char c;
		return sqlstate.trim().length()==5&&(sqlstate.charAt(0)!='0'||((c=sqlstate.charAt(1))!='1'&&c!='2'&&c!='0'));
	}
	
	public boolean sqlWarning() {
		return sqlstate.length()==5&&sqlstate.charAt(0)=='0'&&sqlstate.charAt(1)=='1';
	}
	
	
}
