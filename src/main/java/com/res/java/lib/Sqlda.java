package com.res.java.lib;
/************************************************************************
 **This file automatically generated from Data Level SQLDA
 **Generated at time 10:04:57.84 on Thursday, 12/10/09
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

public class Sqlda extends CobolBean {
	//@CobolSourceFile("EXEC-SQL1.COB",90,12) 
	//05 SQLDAID     PIC X(8).
	public String getSqldaid() {
		return super.toString(0,8);
	}
	public  void setSqldaid(String val) {
		super.valueOf(0,8,val);
	}
	//@CobolSourceFile("EXEC-SQL1.COB",91,12) 
	//05 SQLDABC     PIC S9(9) BINARY.
	public int getSqldabc() {
		return super.getBinaryInt(8,9);
	}
	public  void setSqldabc(int val) {
		super.setBinaryInt(8,9,val,true);
	}
	//@CobolSourceFile("EXEC-SQL1.COB",92,12) 
	//05 SQLN        PIC S9(4) BINARY.
	public int getSqln() {
		return super.getBinaryInt(12,4);
	}
	public  void setSqln(int val) {
		super.setBinaryInt(12,4,val,true);
	}
	//@CobolSourceFile("EXEC-SQL1.COB",93,12) 
	//05 SQLD        PIC S9(4) BINARY.
	public int getSqld() {
		return super.getBinaryInt(14,4);
	}
	public  void setSqld(int val) {
		super.setBinaryInt(14,4,val,true);
	}
	//@CobolSourceFile("EXEC-SQL1.COB",94,12) 
	//05 SQLVAR OCCURS 0 TO 409 TIMES DEPENDING ON SQLD.
	//@CobolSourceFile("EXEC-SQL1.COB",95,15) 
	//10 SQLTYPE   PIC S9(4) BINARY.
	public int getSqltype(int idx1) {
		return super.getBinaryInt(16+48*--idx1+16,4);
	}
	public  void setSqltype(int val,int idx1) {
		super.setBinaryInt(16+48*--idx1+16,4,val,true);
	}
	//@CobolSourceFile("EXEC-SQL1.COB",96,15) 
	//10 SQLLEN    PIC S9(4) BINARY.
	public int getSqllen(int idx1) {
		return super.getBinaryInt(16+48*--idx1+18,4);
	}
	public  void setSqllen(int val,int idx1) {
		super.setBinaryInt(16+48*--idx1+18,4,val,true);
	}
	//@CobolSourceFile("EXEC-SQL1.COB",97,15) 
	//10 FILLER  REDEFINES SQLLEN.
	//@CobolSourceFile("EXEC-SQL1.COB",98,18) 
	//15 SQLPRECISION PIC X.
	public String getSqlprecision(int idx1) {
		return super.toString(16+48*--idx1+18,1);
	}
	public  void setSqlprecision(String val,int idx1) {
		super.valueOf(16+48*--idx1+18,1,val);
	}
	//@CobolSourceFile("EXEC-SQL1.COB",99,18) 
	//15 SQLSCALE     PIC X.
	public String getSqlscale(int idx1) {
		return super.toString(16+48*--idx1+19,1);
	}
	public  void setSqlscale(String val,int idx1) {
		super.valueOf(16+48*--idx1+19,1,val);
	}
	//@CobolSourceFile("EXEC-SQL1.COB",100,15) 
	//10 SQLRES    PIC X(12).
	public String getSqlres(int idx1) {
		return super.toString(16+48*--idx1+20,12);
	}
	public  void setSqlres(String val,int idx1) {
		super.valueOf(16+48*--idx1+20,12,val);
	}
	//@CobolSourceFile("EXEC-SQL1.COB",101,15) 
	//10 SQLDATA   POINTER.
	//@CobolSourceFile("EXEC-SQL1.COB",102,15) 
	//10 SQLIND    POINTER.
	//@CobolSourceFile("EXEC-SQL1.COB",103,15) 
	//10 SQLNAME.
	//@CobolSourceFile("EXEC-SQL1.COB",104,18) 
	//49 SQLNAMEL PIC S9(4) BINARY.
	public int getSqlnamel(int idx1) {
		return super.getBinaryInt(16+48*--idx1+32,4);
	}
	public  void setSqlnamel(int val,int idx1) {
		super.setBinaryInt(16+48*--idx1+32,4,val,true);
	}
	//@CobolSourceFile("EXEC-SQL1.COB",105,18) 
	//49 SQLNAMEC PIC X(30).
	public String getSqlnamec(int idx1) {
		return super.toString(16+48*--idx1+34,30);
	}
	public  void setSqlnamec(String val,int idx1) {
		super.valueOf(16+48*--idx1+34,30,val);
	}
	public void initialize() {
	}
	public Sqlda() {
		super(new CobolBytes(19648));
	}
	public Sqlda(CobolBytes b) {//For redefines
		super(b);
	}
	public String toString() {
		return new String(getBytes());
	}
	public void valueOf(String val) {//Bytes Vs. Chars
		valueOf(val.getBytes());
	}
	public byte[] getBytes() {
		CobolBytes bytes_=new CobolBytes(19648);
		bytes_.valueOf(0,8,super.getBytes(),0);;
		bytes_.valueOf(8,4,super.getBytes(),8);;
		bytes_.valueOf(12,2,super.getBytes(),12);;
		bytes_.valueOf(14,2,super.getBytes(),14);;
		bytes_.valueOf(16,19632,super.getBytes(),16);;
		return bytes_.getBytes();
	}
	public void valueOf(byte[] val) {
		CobolBytes bytes_=new CobolBytes(val,19648);
		super.valueOf(0,8,bytes_.getBytes(),0);
		super.valueOf(8,4,bytes_.getBytes(),8);
		super.valueOf(12,2,bytes_.getBytes(),12);
		super.valueOf(14,2,bytes_.getBytes(),14);
		super.valueOf(16,19632,bytes_.getBytes(),16);
	}
}
