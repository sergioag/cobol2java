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

public class Section extends CobolMethod {

	Paragraph from=null,to=null;
	
	public Section(Program p) {
		super(p);from=null;to=null;
	}
	public Section(Program p,boolean declarative) {
		super(p);from=null;to=null;isDeclarative=declarative;
	}
	public void registerHandler(String n, Paragraph p) {
		getProgram().registerHandler(n, p);
	}	
	public CobolMethod doCobolPerform(CobolMethod from,CobolMethod thru){
		if(from==null) return null;
		return program.doCobolPerform(from,to);
	}
	public CobolMethod doCobolPerform(CobolMethod from){
		if(from==null) return null;
		return program.doCobolPerform(from,null);
	}
	public void setOpen(boolean b) {
		if(to!=null) to.setOpen(b);
		else from.setOpen(b);
	}
	public CobolMethod run(){
		if(from!=null)
			return program.doCobolGoto(from);
		else
			return doCobolExit();
	}
	
	protected int __getReturnCode() {
		return program.returnCode;
	}
	protected void __setReturnCode(int returnCode) {
		program.returnCode = returnCode;
	}
}
