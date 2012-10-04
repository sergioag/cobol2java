package com.res.java.translation.engine;
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
import java.util.Enumeration;
import java.util.Stack;

import com.res.cobol.syntaxtree.AddStatement;
import com.res.cobol.syntaxtree.AlterStatement;
import com.res.cobol.syntaxtree.ComputeStatement;
import com.res.cobol.syntaxtree.DivideStatement;
import com.res.cobol.syntaxtree.ExecSqlStatement;
import com.res.cobol.syntaxtree.GotoStatement;
import com.res.cobol.syntaxtree.IfStatement;
import com.res.cobol.syntaxtree.MultiplyStatement;
import com.res.cobol.syntaxtree.Node;
import com.res.cobol.syntaxtree.NodeChoice;
import com.res.cobol.syntaxtree.NodeListOptional;
import com.res.cobol.syntaxtree.NodeOptional;
import com.res.cobol.syntaxtree.NodeSequence;
import com.res.cobol.syntaxtree.NodeToken;
import com.res.cobol.syntaxtree.Paragraph;
import com.res.cobol.syntaxtree.ProcedureName;
import com.res.cobol.syntaxtree.ProcedureSection;
import com.res.cobol.syntaxtree.ReadStatement;
import com.res.cobol.syntaxtree.SearchStatement;
import com.res.cobol.syntaxtree.Statement;
import com.res.cobol.syntaxtree.StatementList;
import com.res.cobol.syntaxtree.StopStatement;
import com.res.cobol.syntaxtree.SubtractStatement;
import com.res.cobol.syntaxtree.UnstringStatement;
import com.res.cobol.visitor.DepthFirstVisitor;
import com.res.common.RESContext;
import com.res.java.translation.symbol.SymbolProperties;
import com.res.java.translation.symbol.SymbolTable;

public class CobolRecode extends DepthFirstVisitor {

	private CobolRecode() {}
	private static CobolRecode thiz = null;
        private static RESContext context = null;
	public static CobolRecode getInstance(RESContext ctx) {
		if(thiz==null)  {
                    context = ctx;
                    thiz = new CobolRecode();
            }
		return thiz;
	}
	public static void clear() { thiz = null;context = null; }
	private Stack<Boolean> nextStatementDead = new Stack<Boolean>();
	private boolean lastStatementDead=false;
	private int paraStatementCount=0,sectionStatementCount=0;
	
	@Override
	public void visit(ProcedureSection n) {
		n.sectionHeader.sectionName.accept(this);
		sectionStatementCount=0;
		//String sectionName=lastTokenString;
		super.visit(n);
		//if(sectionStatementCount<=RESConfig.getInstance().getInlineStatement()){
			//props=SymbolTable.getScope().lookup(sectionName,SymbolConstants.SECTION);
			//if(props!=null) props.setConsolidateParagraph(true);
		//}
	}

	@Override
	public void visit(GotoStatement n) {
		NodeOptional nodeopt=(NodeOptional)((NodeSequence)n.nodeChoice.choice).elementAt(1);
		if(!nodeopt.present()) {
			
			if (nextStatementDead.size()>0)
				nextStatementDead.set(nextStatementDead.size()-1, true);
			else
				nextStatementDead.push(true);
			super.visit(n);
			((NodeSequence)n.nodeChoice.choice).elementAt(0).accept(this);
			props=SymbolTable.getScope().lookup(lastTokenString);
			if(props!=null) { 
				props.setGotoTarget(true);
			}
		} else {
			NodeListOptional nodelistopt=(NodeListOptional) ((NodeSequence)nodeopt.node).elementAt(0);
			for(Enumeration<Node> e=nodelistopt.elements();e.hasMoreElements();) {
				e.nextElement().accept(this);
				props=SymbolTable.getScope().lookup(lastTokenString);
				if(props!=null) { 
					props.setGotoTarget(true);
				}
			}

		}
	}
	
	@Override
	public void visit(ProcedureName n) {
		((NodeSequence)n.nodeChoice.choice).elementAt(0).accept(this);
	}

	@Override
	public void visit(Paragraph n) {
		//n.paragraphName.accept(this);
		//String paragraphName=lastTokenString;
		paraStatementCount=0;
		nextStatementDead.push(false);
		super.visit(n);
		/*
		if(paraStatementCount<=RESConfig.getInstance().getInlineStatement()){
			props=SymbolTable.getScope().lookup(paragraphName,SymbolConstants.PARAGRAPH);
			if(props!=null) { 
				props.setConsolidateParagraph(true);
				props.setOtherData2(n.nodeChoice);
			}
		}
		*/
		n.deadStatement=nextStatementDead.pop();
	}

	private String lastTokenString = null;
	private SymbolProperties props = null;
	
	@Override
	public void visit(NodeToken n) {
		lastTokenString=n.tokenImage;
	}


	@Override
	public void visit(UnstringStatement n) {
		boolean onOverflowTailDead=false,notOnOverflowTailDead=false;

		n.nodeOptional3.accept(this);
		
		onOverflowTailDead=lastStatementDead;
		lastStatementDead=false;
		
		n.nodeOptional4.accept(this);
		notOnOverflowTailDead=lastStatementDead;
		lastStatementDead=false;
		
		if(notOnOverflowTailDead&&onOverflowTailDead)
			if (nextStatementDead.size()>0)
				nextStatementDead.set(nextStatementDead.size()-1, true);
			else
				lastStatementDead=true;
	
	}

	@Override
	public void visit(IfStatement n) {
		boolean ifTailDead=false;boolean elseTailDead=false;
		switch(n.nodeChoice.which) {
		case 0:
			NodeSequence seq =(NodeSequence)n.nodeChoice.choice;
			seq.elementAt(0).accept(this);
			break;
		case 1:
		default:
		}
		
		ifTailDead=lastStatementDead;
		lastStatementDead=false;
		
		if(n.nodeOptional1.present()) {
			if (n.nodeOptional1.node instanceof NodeSequence) {
				NodeSequence nodeseq=(NodeSequence)n.nodeOptional1.node;
				NodeChoice choice= (NodeChoice)nodeseq.elementAt(1);
				switch(choice.which) {
				case 0:
					NodeSequence seq =(NodeSequence)choice.choice;
					seq.elementAt(0).accept(this);
					break;
				case 1:
				default:
				}
				elseTailDead=lastStatementDead;
			}
		}
		
		if(ifTailDead&&elseTailDead)
			if (nextStatementDead.size()>0)
				nextStatementDead.set(nextStatementDead.size()-1, true);
			else
				lastStatementDead=true;
	}

	@Override
	public void visit(Statement n) {
		
		if(context.getTraceLevel()>=2) {
			System.out.println("Doing CobolRecode statement "+n.line);
		}
		
		if(n.nodeChoice.choice instanceof ReadStatement||n.nodeChoice.choice instanceof ExecSqlStatement) {
			boolean deadAlready=(nextStatementDead.size()>0)?nextStatementDead.peek().booleanValue():false;
			nextStatementDead.push(deadAlready);
		}
		else
		n.deadStatement=(nextStatementDead.size()>0)?nextStatementDead.peek().booleanValue():false;
		if(!n.deadStatement) {
			paraStatementCount++;sectionStatementCount++;
		}
		super.visit(n);
		lastStatementDead=(nextStatementDead.size()>0)?nextStatementDead.peek().booleanValue():false;
		if(n.nodeChoice.choice instanceof ReadStatement||n.nodeChoice.choice instanceof ExecSqlStatement) 
			nextStatementDead.pop();
	}

	@Override
	public void visit(StatementList n) {
		boolean deadAlready=(nextStatementDead.size()>0)?nextStatementDead.peek().booleanValue():false;
		nextStatementDead.push(deadAlready);
		super.visit(n);
		nextStatementDead.pop();
	}

	@Override
	public void visit(StopStatement n) {
		super.visit(n);
	}

	@Override
	public void visit(SubtractStatement n) {
		//Interpret Overflow as Size Error
		boolean onOverflowTailDead=false,notOnOverflowTailDead=false;

		n.nodeOptional.accept(this);
		
		onOverflowTailDead=lastStatementDead;
		lastStatementDead=false;
		
		n.nodeOptional1.accept(this);
		notOnOverflowTailDead=lastStatementDead;
		lastStatementDead=false;
		
		if(notOnOverflowTailDead&&onOverflowTailDead)
			if (nextStatementDead.size()>0)
				nextStatementDead.set(nextStatementDead.size()-1, true);
			else
				lastStatementDead=true;
	
	}

	@Override
	public void visit(DivideStatement n) {
		//Interpret Overflow as Size Error
		boolean onOverflowTailDead=false,notOnOverflowTailDead=false;

		n.nodeOptional.accept(this);
		
		onOverflowTailDead=lastStatementDead;
		lastStatementDead=false;
		
		n.nodeOptional1.accept(this);
		notOnOverflowTailDead=lastStatementDead;
		lastStatementDead=false;
		
		if(notOnOverflowTailDead&&onOverflowTailDead)
			if (nextStatementDead.size()>0)
				nextStatementDead.set(nextStatementDead.size()-1, true);
			else
				lastStatementDead=true;
	
	
	}

	@Override
	public void visit(MultiplyStatement n) {
		//Interpret Overflow as Size Error
		boolean onOverflowTailDead=false,notOnOverflowTailDead=false;

		n.nodeOptional.accept(this);
		
		onOverflowTailDead=lastStatementDead;
		lastStatementDead=false;
		
		n.nodeOptional1.accept(this);
		notOnOverflowTailDead=lastStatementDead;
		lastStatementDead=false;
		
		if(notOnOverflowTailDead&&onOverflowTailDead)
			if (nextStatementDead.size()>0)
				nextStatementDead.set(nextStatementDead.size()-1, true);
			else
				lastStatementDead=true;
	
		}

	@Override
	public void visit(ComputeStatement n) {
		//Interpret Overflow as Size Error
		boolean onOverflowTailDead=false,notOnOverflowTailDead=false;

		n.nodeOptional.accept(this);
		
		onOverflowTailDead=lastStatementDead;
		lastStatementDead=false;
		
		n.nodeOptional1.accept(this);
		notOnOverflowTailDead=lastStatementDead;
		lastStatementDead=false;
		
		if(notOnOverflowTailDead&&onOverflowTailDead)
			if (nextStatementDead.size()>0)
				nextStatementDead.set(nextStatementDead.size()-1, true);
			else
				lastStatementDead=true;
	
		}

	@Override
	public void visit(AddStatement n) {
		//Interpret Overflow as Size Error
		boolean onOverflowTailDead=false,notOnOverflowTailDead=false;

		n.nodeOptional.accept(this);
		
		onOverflowTailDead=lastStatementDead;
		lastStatementDead=false;
		
		n.nodeOptional1.accept(this);
		notOnOverflowTailDead=lastStatementDead;
		lastStatementDead=false;
		
		if(notOnOverflowTailDead&&onOverflowTailDead)
			if (nextStatementDead.size()>0)
				nextStatementDead.set(nextStatementDead.size()-1, true);
			else
				lastStatementDead=true;
	
	}

	@Override
	public void visit(SearchStatement n) {
		n.nodeOptional2.accept(this);
		boolean searchMakesNextDead = lastStatementDead;
		for(Enumeration<Node> e=n.nodeList.elements();e.hasMoreElements();) {
			NodeSequence nodeseq =  (NodeSequence)e.nextElement();
			nodeseq.elementAt(1).accept(this);
			NodeChoice nodech = (NodeChoice) nodeseq.elementAt(2);
			if(nodech.which==0) {
				nodech.accept(this);
				searchMakesNextDead&=lastStatementDead;
			} else {
				searchMakesNextDead=false;
			}
		}
		
		if(searchMakesNextDead)
			if (nextStatementDead.size()>0)
				nextStatementDead.set(nextStatementDead.size()-1, true);
			else
				lastStatementDead=true;
	}

	@Override
	public void visit(AlterStatement n) {
			for(Enumeration<Node> e=n.nodeList.elements();e.hasMoreElements();) {
				NodeSequence nodeseq=(NodeSequence)e.nextElement();
				nodeseq.elementAt(0).accept(this);
				props=SymbolTable.getScope().lookup(lastTokenString);
				if(props!=null)
					props.setAlteredParagraph(true);
			}
	}


}
