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

public class Paragraph extends CobolMethod {

	public Paragraph(Program p) {
		super(p);
	}
	public Paragraph(Program p,Section s) {
		super(p);
		if(s!=null)
			if(s.from==null) s.from=this;
			s.to=this;
	}
	
	public Paragraph(Program p,boolean declarative) {
		super(p);isDeclarative=declarative;
	}
	public Paragraph(Program p,Section s,boolean declarative) {
		super(p);isDeclarative=declarative;
		if(s!=null)
			if(s.from==null) s.from=this;
			s.to=this;
	}
	
	public Paragraph(Paragraph p) {
		super(p.getProgram());
	}
	public Paragraph(Section p) {
		super(p.getProgram());
	}	
	public void registerHandler(String n, Paragraph p) {
		getProgram().registerHandler(n, p);
	}
	
	protected int __getReturnCode() {
		return program.returnCode;
	}
	protected void __setReturnCode(int returnCode) {
		program.returnCode = returnCode;
	}
}
