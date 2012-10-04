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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.res.cobol.RESNode;
import com.res.cobol.TreeToCommentFormatter;
import com.res.cobol.parser.CobolParserConstants;
import com.res.cobol.syntaxtree.*;
import com.res.cobol.visitor.DepthFirstVisitor;
import com.res.common.RESConfig;
import com.res.common.RESContext;
import com.res.java.lib.Constants;
import com.res.java.lib.RunTimeUtil;
import com.res.java.translation.symbol.SymbolProperties.CobolSymbol;
import com.res.java.translation.symbol.SymbolConstants;
import com.res.java.translation.symbol.SymbolProperties;
import com.res.java.translation.symbol.SymbolTable;
import com.res.java.translation.symbol.SymbolUtil;
import com.res.java.util.ClassFile;
import com.res.java.util.NameUtil;
import java.math.BigDecimal;

@SuppressWarnings("unused")
public class Cobol2Java extends DepthFirstVisitor {

    private static Cobol2Java thiz = null;

    private Cobol2Java() {
    }
    private boolean doQualified = false;
    private boolean doLiteral = false;
    private boolean doingAcceptOption = false;
    private boolean doingSingleParagraph = false;
    private boolean doingNestedProgram = false;
    private Stack<String> condition = new Stack<String>();
    public int expressionType = 0;
    public String expressionString;
    public boolean isAll = false;
    public ExpressionString literal = new ExpressionString();
    public String lastTokenString = null;
    private Stack<String> qualified = new Stack<String>();
    private Stack<SymbolProperties> idProps = null;
    private Stack<String> optionStack = null;
    private SymbolProperties props = null;
    private SymbolProperties paragraphProps = null;
    private SymbolProperties sectionProps = null;
    private SymbolProperties procedureProps = null;
    private SymbolProperties procedureThruProps = null;
    private ArrayList<ExpressionString> idOrLiteralList;
    private Stack<StringBuilder> aLine = new Stack<StringBuilder>();
    private Stack<StringBuilder> bLine = new Stack<StringBuilder>();
    private Stack<StringBuilder> cLine = new Stack<StringBuilder>();
    private int callParmIdx = 0;
    private String paragraphName = null;
    private String sectionName = null;

    @SuppressWarnings("serial")
    public class ExpressionStack extends Stack<ExpressionString> {

        @Override
        public boolean empty() {
            return super.empty();
        }

        @Override
        public synchronized ExpressionString peek() {
            return super.peek();
        }

        public synchronized String peek3() {
            return super.peek().toString();
        }

        @Override
        public synchronized ExpressionString pop() {
            return super.pop();
        }

        public synchronized String pop3() {
            return super.pop().toString();
        }

        public ExpressionString push(String item) {
            return super.push(new ExpressionString(item, expressionType));
        }

        @Override
        public ExpressionString push(ExpressionString item) {
            return super.push(item);
        }

        public ExpressionString push(SymbolProperties item) {
            return super.push(new ExpressionString(item));
        }

        public boolean isAll() {
            return super.peek().isAll;
        }

        public void setAll(boolean b) {
            super.peek().isAll = b;
        }

        public int getType() {
            return super.peek().type;
        }

        public void setType(int b) {
            super.peek().type = b;
        }
    }
    public ExpressionStack expression = new ExpressionStack();

    @Override
    public void visit(CompilationUnit n) {
        super.visit(n);
    }

    @Override
    public void visit(NestedProgramUnit n) {
        doingNestedProgram = true;
        n.nestedIdentificationDivision.accept(this);
        n.nodeOptional1.accept(this);
        n.nodeOptional.accept(this);
        n.nodeOptional2.accept(this);
        n.nodeListOptional.accept(this);
        ClassFile.endProgramScope();
        dumper.printCobolComments(n.endProgramStatement.line);
        SymbolTable.getScope().endProgram();
        doingNestedProgram = false;
    }

    @Override
    public void visit(ProgramUnit n) {
        SymbolTable.getScope().setCloneOnLookup(true);
        n.identificationDivision.accept(this);
        n.nodeOptional1.accept(this);
        n.nodeOptional.accept(this);
        n.nodeOptional2.accept(this);
        dumper.printCobolComments(9999999);
        if (!SymbolTable.getScope().isCurrentProgramTopLevel()) {
            ClassFile.endProgramScope();
            SymbolTable.getScope().endProgram();
        }
        SymbolTable.getScope().setCloneOnLookup(false);
    }
    private boolean isDeclarative = false;

    @Override
    public void visit(Declaratives n) {
        isDeclarative = true;
        for (Enumeration<Node> e = n.nodeList.elements(); e.hasMoreElements();) {
            NodeSequence nodeseq = (NodeSequence) e.nextElement();
            ((SectionHeader) nodeseq.elementAt(0)).sectionName.accept(this);
            nodeseq.elementAt(2).accept(this);
            ClassFile.println("Section " + sectionProps.getJavaName1() + "=new Section(this,true) {");
            ClassFile.println("\tpublic CobolMethod run() {");
            ClassFile.tab();
            ClassFile.println("return super.run();");
            ClassFile.backTab();
            ClassFile.println("}};");

            nodeseq.elementAt(4).accept(this);
        }
        isDeclarative = false;
    }

    class UseCondition {

        boolean isError = false;
        String openCondition = null;

        public UseCondition(boolean e, String o, SymbolProperties sec) {
            isError = e;
            openCondition = o;
            section = sec;
        }
        SymbolProperties section = null;
    }
    private UseCondition useCondition = null;
    private ArrayList<UseCondition> useConditions = null;

    @Override
    public void visit(UseStatement n) {
        if (useConditions == null) {
            useConditions = new ArrayList<UseCondition>();
        }
        if (sectionProps == null) {
            return;
        }
        if (n.nodeChoice.which == 1) {
            NodeSequence nodeseq = (NodeSequence) n.nodeChoice.choice;
            NodeChoice nodech = (NodeChoice) nodeseq.elementAt(6);
            switch (nodech.which) {
                case 0:
                    NodeList filelist = (NodeList) nodech.choice;
                    for (Enumeration<Node> e = filelist.elements(); e.hasMoreElements();) {
                        e.nextElement().accept(this);
                        if (props == null) {
                            continue;
                        }
                        useConditions.add(new UseCondition(true, props.getDataName(), sectionProps));
                    }
                    break;
                case 1:
                case 2:
                case 3:
                case 4:
                    useConditions.add(new UseCondition(true, String.valueOf(nodech.which), sectionProps));
            }
        }
    }

    @Override
    public void visit(CurrencySignClause n) {
        n.literal.accept(this);
        context.setCurrencySign(formatLiteral(literal).toString());
    }

    @Override
    public void visit(DecimalPointClause n) {
        context.setDecimalPointIsComma(true);
    }
    private ArrayList<Node> delayedStatements = null;

    @Override
    public void visit(DataDescriptionEntry n) {

        if (n.nodeChoice.which == 5) {
            NodeChoice nodech = (NodeChoice) ((NodeSequence) n.nodeChoice.choice).elementAt(2);
            if (nodech.which == 3) {
                //DECLARE CURSOR
                if (delayedStatements == null) {
                    delayedStatements = new ArrayList<Node>();
                }
                delayedStatements.add(n);
            }
        }
        //super.visit(n);
    }
    private String returnLine = null;

    @Override
    public void visit(ProcedureDivision n) {

        if (SymbolTable.getScope().getCurrentProgram() == null) {
            return;
        }
        aLine.push(new StringBuilder());
        returnLine = null;
        isLastGotoStatement = false;
        ClassFile.doProgramScope(SymbolTable.getScope().getCurrentProgram(), false);
        ClassFile.tab();

        //Create Symbols and Accessors.
        SymbolTable.convert2Java();

        String className = (String) SymbolTable.getScope().getCurrentProgram().getJavaName2();
        if (SymbolTable.getScope().isCurrentProgramTopLevel()) {
            ClassFile.println("public static void main(String[] args) {");
            ClassFile.tab();
            ClassFile.println("__processCmdLineArgs(args);");
            ClassFile.println(className + " instance_ = new " + className + "();");
            ClassFile.println("instance_.execute(null); ");
            ClassFile.println("System.exit(instance_.__getReturnCode());");
            ClassFile.backTab();
            ClassFile.endMethodScope();
        }

        ClassFile.println("public void execute(ProgramEnv __env) {");
        ClassFile.tab();
        ClassFile.println("initialize(this); ");
        if (n.nodeOptional.present()) {
            NodeList nodelist = ((UsingArgs) ((NodeSequence) n.nodeOptional.node).elementAt(1)).nodeList;
            processUsingArgs1(nodelist);
        }
        if (delayedStatements != null) {
            for (Node nd : delayedStatements) {
                nd.accept(execSql2Java);
            }
        }
        if (n.procedureBody.paragraphs.nodeListOptional.present()) {
            n.procedureBody.paragraphs.nodeListOptional.accept(this);
        }
        if (!isLastGotoStatement) {
            ClassFile.println("doCobolGotoStart();");
        }
        if (n.nodeOptional.present()) {
            NodeList nodelist = ((UsingArgs) ((NodeSequence) n.nodeOptional.node).elementAt(1)).nodeList;
            processUsingArgs2(nodelist);
        }

        ClassFile.backTab();
        ClassFile.endMethodScope();

        aLine.pop();
        if (n.nodeOptional1.present()) {
            dumper.startAtNextToken();
            dumper.visit(n.nodeOptional1);
            n.nodeOptional1.accept(this);
        }
        if (n.procedureBody.paragraphs.nodeListOptional1.present()) {
            n.procedureBody.paragraphs.nodeListOptional1.accept(this);
        }
        if (n.procedureBody.nodeListOptional.present()) {
            n.procedureBody.nodeListOptional.accept(this);
        }
        doTail();

    }

    private void processUsingArgs2(NodeList nodelist) {
        aLine.peek().setLength(0);
        bLine.push(new StringBuilder());
        for (Enumeration<Node> e = nodelist.elements(); e.hasMoreElements();) {
            e.nextElement().accept(this);
            if (aLine.peek().length() > 0) {
                aLine.peek().append(',');
            }
            if (props != null) {
                // if (props.getIsFormat()) {
                //   line = NameUtil.getFormatName2(props, false).replace("%0", NameUtil.getJavaName(props, false));
                //} else {
                line = new ExpressionString(props).toString();
                //}
                aLine.peek().append(line);
            }
        }
        aLine.peek().append(bLine.peek());
        ClassFile.println(aLine.peek().insert(0, "doCobolReturn(").append(");").toString());
        returnLine = aLine.peek().toString();
        bLine.pop();
    }

    private void processUsingArgs1(NodeList nodelist) {
        ClassFile.println("__initializeCall(__env," + String.valueOf(nodelist.size())
                + ");");
        int i = 0;
        for (Enumeration<Node> e = nodelist.elements(); e.hasMoreElements();) {
            e.nextElement().accept(this);
            if (props != null) {
                if (props.isAChildHasDependingOn() || (props.isOccurs() && props.getDependingOnOccurs() != null)) {
                    aLine.peek().append(ClassFile.current.getTabs()).append(NameUtil.getJavaName(props, true).replace("%0",
                            ("__get" + SymbolConstants.getSQL(props.getIdentifierType()) + "Arg("
                            + String.valueOf(i++) + ((props.getIdentifierType() >= Constants.STRING)
                            ? ("," + String.valueOf(props.getLength())) : "") + ")")) + ';');
                } else {
                    ClassFile.println(NameUtil.getJavaName(props, true).replace("%0",
                            ("__get" + SymbolConstants.getSQL(props.getIdentifierType()) + "Arg("
                            + String.valueOf(i++) + ((props.getIdentifierType() >= Constants.STRING)
                            ? ("," + String.valueOf(props.getLength())) : "") + ")")) + ';');
                }
            }
        }
        ClassFile.println(aLine.peek().toString());
    }

    @Override
    public void visit(ExitProgramStatement n) {
        isLastExitStatement = true;
        ClassFile.println("return null;");
        super.visit(n);
    }

    @Override
    public void visit(StopStatement n) {
        //super.visit(n);
        ClassFile.println("System.exit(super.__getReturnCode());");
    }

    @Override
    public void visit(MoveStatement n) {

        expressionType = 0;
        switch (n.nodeChoice.which) {
            case 0:
                NodeSequence nodeseq = (NodeSequence) n.nodeChoice.choice;
                if (nodeseq.size() != 3) {
                    return;
                }
                NodeChoice choice2 = (NodeChoice) nodeseq.elementAt(0);
                ;
                choice2.accept(this);
                ExpressionString fromLit = null;
                SymbolProperties fromProps = null;
                if (choice2.which == 0) {
                    fromProps = props;
                    expressionType = props.getIdentifierType();
                } else {
                    fromLit = formatLiteral(literal);
                }

                int saveType = expressionType;
                if (nodeseq.elementAt(2) instanceof NodeList) {
                    for (Enumeration<Node> e = ((NodeList) nodeseq.elementAt(2)).elements();
                            e.hasMoreElements();) {
                        Node node = e.nextElement();
                        node.accept(this);
                        expressionType = saveType;
                        if (fromProps != null) {
                            doMove(fromProps, props);
                        } else {
                            doMove(fromLit, props);
                        }
                    }
                }
                break;
            case 1:
                nodeseq = (NodeSequence) n.nodeChoice.choice;
                nodeseq.elementAt(1).accept(this);
                fromProps = props;
                if (nodeseq.elementAt(3) instanceof NodeList) {
                    for (Enumeration<Node> e = ((NodeList) nodeseq.elementAt(3)).elements();
                            e.hasMoreElements();) {
                        Node node = e.nextElement();
                        node.accept(this);
                        doCorrespondingMove(fromProps, props, true);
                    }
                }
        }
    }

    private void doMove(ExpressionString from, SymbolProperties to) {

        boolean isAllLiteral = from.isAll;
        expression.push(from);
        ClassFile.println(TranslationTable.getInstance().getAssignString(to, null, isAllLiteral, true, true));
        expression.clear();
        initExpressionType();
        return;

    }

    private void doMove(SymbolProperties propsFrom, SymbolProperties to) {

        if (propsFrom.getType() == SymbolConstants.DUMMY) {
            propsFrom.setIsFormat(false);
        }
        ClassFile.println(TranslationTable.getInstance().getAssignString(to, propsFrom, false, true, true));
        expression.clear();
        initExpressionType();
        return;

    }

    private boolean doCorrespondingMove(SymbolProperties from, SymbolProperties to, boolean first) {
        if (first) {
            if (!isCorrespondingValid2(from, to)) {
                return false;
            }
        } else if (!isCorrespondingValid(from, to)) {
            return false;
        }
        if (from.getPictureString() != null || to.getPictureString() != null) {
            doMove(from, to);
            return true;
        } else {
            if (from.getChildren() != null && to.getChildren() != null) {
                for (int i = 0; i < from.getChildren().size(); ++i) {
                    for (int j = 0; j < to.getChildren().size(); ++j) {
                        from.getChildren().get(i).setIndexesWorkSpace(from.getIndexesWorkSpace());
                        to.getChildren().get(j).setIndexesWorkSpace(to.getIndexesWorkSpace());
                        if (doCorrespondingMove(from.getChildren().get(i), to.getChildren().get(j), false)) {
                            break;
                        }
                    }
                }
            }
            return false;
        }
    }

    public boolean isCorrespondingValid(SymbolProperties corrF, SymbolProperties corrT) {
        if (!corrF.getDataName().equalsIgnoreCase(corrT.getDataName())) {
            return false;
        }
        if (corrF == null || corrF.isOccurs() || corrF.getIsFiller() || corrF.getJavaType() == null
                || corrF.getIdentifierType() > Constants.GROUP
                || corrF.getRedefines() != null || corrF.hasRenames()) {
            return false;
        }
        if (corrT == null || corrT.isOccurs() || corrT.getIsFiller() || corrT.getJavaType() == null
                || corrT.getIdentifierType() > Constants.GROUP
                || corrT.getRedefines() != null || corrT.hasRenames()) {
            return false;
        }
        return true;
    }

    public boolean isCorrespondingValid2(SymbolProperties corrF, SymbolProperties corrT) {
        if (corrF == null || corrF.getLevelNumber() == 66
                || corrF.getLevelNumber() == 77 || corrF.getLevelNumber() == 88
                || corrF.getJavaType() == null || corrF.getIdentifierType() > Constants.GROUP) {
            return false;
        }
        if (corrT == null || corrT.getLevelNumber() == 66
                || corrT.getLevelNumber() == 77 || corrT.getLevelNumber() == 88
                || corrT.getJavaType() == null || corrT.getIdentifierType() > Constants.GROUP) {
            return false;
        }
        return true;
    }
    private final Pattern HEX_COBOL_PATTERN = Pattern.compile("[xX]\\\'[0-9a-fA-F]+\\\'|[xX]\\\"[0-9a-fA-F]+\\\"");
    private final Pattern HEX_PATTERN = Pattern.compile("0[xX][0-9a-fA-F]+|0[xX][0-9a-fA-F]+");
    private final Pattern INT_PATTERN = Pattern.compile("(\\+|\\-)?(\\d)+(\\,(\\d)+)*");
    private final Pattern DEC_PATTERN = Pattern.compile("(\\+|\\-)?(\\d)*(\\.[\\d]+)");
    private String line = "";

    public ExpressionString formatLiteral(String lit) {
        literal = new ExpressionString(lit);
        return formatLiteral(literal);
    }

    public ExpressionString formatLiteral(ExpressionString litP) {

        ExpressionString litC = new ExpressionString(litP);
        int j = 0;
        boolean isFunction = false;
        String lit = litC.raw = litC.toString();

        if (lit.equals("\"\\\"\""))//tell me why this?
        {
            expressionType = Constants.STRING;
        } else if ((lit.indexOf('\"') == 0 && (j = lit.lastIndexOf('\"')) >= 0)
                || lit.indexOf('\'') == 0
                && (j = lit.lastIndexOf('\'')) >= 0) {
            lit = '\"' + lit.substring(1, j).replace("\"\"", "\"").
                    replace("\\", "\\\\").
                    replace("\"", "\\\"").replace("\'", "\\\"") + '\"';
            expressionType = Constants.STRING;
        } else if (HEX_COBOL_PATTERN.matcher(lit).matches()) {
            lit = "0" + lit.replace("\'", "").replace("\"", "").replace('X', 'x');
            expressionType = Constants.INTEGER;
        } else if (HEX_PATTERN.matcher(lit).matches()) {
            expressionType = Constants.INTEGER;

        } else if (INT_PATTERN.matcher(lit).matches()) {
            lit = stripLeadingZeros(lit);
            expressionType = Constants.INTEGER;
            if (Long.parseLong(lit) > Integer.MAX_VALUE || Long.parseLong(lit) < Integer.MIN_VALUE) {
                lit += "L";//tell me why this?
            }
        } else if (DEC_PATTERN.matcher(lit).matches()) {
            lit = stripLeadingZeros(lit);
            expressionType = Constants.DOUBLE;
        } else if (lit.equalsIgnoreCase("ZERO") || lit.equalsIgnoreCase("ZEROS") || lit.equalsIgnoreCase("ZEROES")) {
            lit = litC.raw = "0";
            expressionType = Constants.INTEGER;
        } else if (lit.equalsIgnoreCase("SPACE") || lit.equalsIgnoreCase("SPACES")) {
            lit = "\" \"";
            litC.raw = null;
            expressionType = Constants.STRING;
        } else if (lit.equalsIgnoreCase("HIGH-VALUE") || lit.equalsIgnoreCase("HIGH-VALUES")) {
             litC.setString("__function__" + String.valueOf(50) + "__()",expressionType = Constants.CHAR);
            litC.raw = null;
            return formatFunction(litC);
        } else if (lit.equalsIgnoreCase("LOW-VALUE") || lit.equalsIgnoreCase("LOW-VALUES")) {
             litC.setString("__function__" + String.valueOf(51) + "__()",expressionType = Constants.CHAR);
            litC.raw = null;
            return formatFunction(litC);
        } else if (lit.equalsIgnoreCase("QUOTE") || lit.equalsIgnoreCase("QUOTES")) {
            lit = "\"\\\"\"";
            litC.raw = null;
            expressionType = Constants.STRING;
        } else if (litC.literal.indexOf("__function__") == 0) {
            return formatFunction(litC);
        } else {
            expressionType = litC.type;
            return litC;
        }
        litC.type = expressionType;
        litC.literal = new StringBuilder(lit);
        if (RESConfig.getInstance().isEbcdicMachine() && expressionType == Constants.STRING && !isFunction) {
            litC.literal.insert(0, "__normalizeLiteral(").append(')');
        }
        return litC;
    }

    private ExpressionString formatFunction(ExpressionString litC) throws NumberFormatException {
        int idx = Integer.parseInt(litC.literal.substring(12, litC.literal.indexOf("__", 12)));
        litC.literal = litC.literal.replace(0, litC.literal.indexOf("__", 12) + 2, SymbolConstants.getFunction(idx)).insert(0, "__function().");
        litC.raw = null;
        expressionType = Math.max(expressionType, Math.abs(SymbolConstants.getFunctionArgTypes(idx)[0]));
        litC.type = expressionType;
        return litC;
    }

    //Input is a valid number literal
    private String stripLeadingZeros(String lit) {

        return RunTimeUtil.getInstance().stripLeadingZeros(lit);

    }

    @Override
    public void visit(DisplayStatement n) {
        line = "";
        for (Enumeration<Node> e = n.nodeList.elements(); e.hasMoreElements();) {
            Node node = e.nextElement();
            NodeSequence nodeseq = (NodeSequence) node;
            NodeChoice nodechoice = (NodeChoice) nodeseq.elementAt(0);
            nodechoice.accept(this);
            switch (nodechoice.which) {
                case 0:
                    if (props.getIdentifierType() == Constants.STRING) {
                        line += "getString(" + new ExpressionString(props) + ',' + String.valueOf(props.getLength()) + ")";
                    } else if (props.getIdentifierType() == Constants.GROUP) {
                        line += "getString(" + new ExpressionString(props) + ',' + String.valueOf(props.getLength()) + ")";
                    } else if (props.getIsFormat()) {
                        line += NameUtil.getFormatName2(props, false).replace("%0",
                                new ExpressionString(props).toString());
                    } else if (props.getPictureString() == null && props.getLevelNumber() == 1) {
                        String temp = new ExpressionString(props).toString();
                        line += temp.substring(0, temp.lastIndexOf('.') + 1) + props.getJavaName1() + ".toString()";
                    } else {
                        line += new ExpressionString(props).toString();
                    }
                    break;
                case 1:
                    line += formatLiteral(literal);
                    break;
                default:
            }
            if (e.hasMoreElements()) {
                line += "+";
            }
        }
        if (n.nodeOptional.present()) {
            n.nodeOptional.node.accept(this);
            if (!lastTokenString.equalsIgnoreCase("CONSOLE")) {
                ClassFile.println("__putEnvironment(" + formatLiteral(
                        new ExpressionString('\"' + RunTimeUtil.getInstance().stripQuotes(lastTokenString) + '\"')) + "," + line + ");");
                return;
            }
        }
        if (!n.nodeOptional1.present()) {
            ClassFile.println("Console.println(" + line + ");");
        } else {
            ClassFile.println("Console.print(" + line + ");");
        }
    }

    private void initExpressionType() {
        //TODO to isolate the inits may be removed later
        expressionType = Constants.BYTE;
    }

    private void initFunctionExpressionType() {
        //TODO to isolate the inits may be removed later
        expressionType = Constants.DOUBLE;
    }

    @Override
    public void visit(AddStatement n) {
        if (n.nodeOptional1.present() || n.nodeOptional.present()) {
            doArithTry();
        }
        n.addBody.accept(this);
        if (n.nodeOptional1.present() || n.nodeOptional.present()) {
            doCatchArith(n.nodeOptional, n.nodeOptional1);
        }
    }

    @Override
    public void visit(AddBody n) {
        initExpressionType();
        expression.clear();
        switch (n.nodeChoice.which) {
            case 1://Format 2
                NodeSequence nodeseq = (NodeSequence) n.nodeChoice.choice;
                nodeseq.elementAt(0).accept(this);
                for (Iterator<ExpressionString> e = idOrLiteralList.iterator(); e.hasNext();) {
                    ExpressionString o = e.next();
                    if (o.isIdSymbol) {

                        doIdOp(o.props, "+");
                    } else {
                        doLiteralOp(o.toString(), "+");
                    }
                }
                if (idOrLiteralList.size() < 1) {
                    break;
                }
                if (idOrLiteralList.size() > 1) {
                    //Create a temporary variable
                    expressionType = expression.peek().type;

                    String assign = TranslationTable.getInstance().getAssignString(null, null, false, true, true);
                    ClassFile.println(assign);
                    expression.push("temp" + String.valueOf(SymbolTable.tempVariablesMark - 1));
                } else {
                    //Here expression is on the stack
                }
                nodeseq.elementAt(2).accept(this);
                ExpressionString top = expression.pop();
                //int topType=expressionType;
                for (Iterator<ExpressionString> e = idOrLiteralList.iterator(); e.hasNext();) {
                    ExpressionString o = e.next();
                    expression.push(top);
                    expressionType = top.type;
                    doIdOp(o.Id(), "+");
                    if (o.isRounded) {
                        expression.peek().literal.insert(0, "__round(").append(",").append(
                                String.valueOf(o.Id().getJavaType().getMaxFractionLength())).append(")");
                        expression.peek().type=Constants.BIGDECIMAL;
                    }
                    if (o.Id().getJavaType().getMaxScalingLength() != 0) {
                        expression.peek().setString(
                                doScaleLiteral(o.Id(), expression.peek().toString(), true),
                                Constants.BIGDECIMAL);
                    }
                    String assign = TranslationTable.getInstance().getAssignString(o.Id(), null, false, true, true);
                    ClassFile.println(assign);
                }
                break;
            case 0://Format 1
                nodeseq = (NodeSequence) n.nodeChoice.choice;
                nodeseq.elementAt(0).accept(this);
                nodeseq.elementAt(1).accept(this);
                for (Iterator<ExpressionString> e = idOrLiteralList.iterator(); e.hasNext();) {
                    ExpressionString o = e.next();
                    if (o.Id() != null) {
                        doIdOp(o.Id(), "+");
                    } else {
                        doLiteralOp(o.Lit(), "+");
                    }
                }
                if (idOrLiteralList.size() > 1) {
                    //Create a temporary variable
                    expressionType = expression.peek().type;
                    String assign = TranslationTable.getInstance().getAssignString(null, null, false, true, true);
                    ClassFile.println(assign);
                    expression.push("temp" + new Integer(SymbolTable.tempVariablesMark - 1).toString().trim());
                } else {
                    //Here expression is on the stack
                }
                nodeseq.elementAt(3).accept(this);
                top = expression.pop();
                //topType=expressionType;
                for (Iterator<ExpressionString> e = idOrLiteralList.iterator(); e.hasNext();) {
                    ExpressionString o = e.next();
                    if (o.IsRounded()) {
                        expression.push(
                                new ExpressionString("__round(" + top + "," + String.valueOf(o.Id().getJavaType().getMaxFractionLength()) + ")", top.type));
                        expression.peek().type=Constants.BIGDECIMAL;
                    } else {
                        expression.push(new ExpressionString(top));
                    }
                    expressionType = top.type;
                    if (o.Id().getJavaType().getMaxScalingLength() != 0) {
                        expression.push(new ExpressionString(doScaleLiteral(o.Id(), expression.pop().toString(), true), Constants.BIGDECIMAL));
                    }/*
                    if(o.Id().getJavaType().getMaxScalingLength()!=0)
                    expression.push(doScaleLiteral(o.Id(),expression.pop().toString(),true));
                     * *
                     */
                    String assign = TranslationTable.getInstance().getAssignString(o.Id(), null, false, true, true);
                    ClassFile.println(assign);
                }
                break;
            case 2://Format 3
                nodeseq = (NodeSequence) n.nodeChoice.choice;
                nodeseq.elementAt(1).accept(this);
                SymbolProperties from = props;
                nodeseq.elementAt(3).accept(this);
                doCorrespondingOp(from, props, "+", false, true);
        }
        //super.visit(n);
    }

    @Override
    public void visit(MultiplyBody n) {
        initExpressionType();
        expression.clear();
        idOrLiteralList = new ArrayList<ExpressionString>();
        n.idOrLiteral.accept(this);
        switch (n.nodeChoice.which) {
            case 0://Format 1
                NodeSequence nodeseq = (NodeSequence) n.nodeChoice.choice;
                nodeseq.elementAt(0).accept(this);
                for (Iterator<ExpressionString> e = idOrLiteralList.iterator(); e.hasNext();) {
                    ExpressionString o = e.next();
                    if (o.Id() != null) {
                        doIdOp(o.Id(), "*");
                    } else {
                        doLiteralOp(o.Lit(), "*");
                    }
                }
                idOrLiteralList = new ArrayList<ExpressionString>();
                nodeseq.elementAt(2).accept(this);
                if (idOrLiteralList.size() > 1) {
                    expressionType = expression.peek().type;
                    String assign = TranslationTable.getInstance().getAssignString(null, null, false,true,true);
                    ClassFile.println(assign);
                    expression.push("temp" + new Integer(SymbolTable.tempVariablesMark - 1).toString().trim());
                } else {
                    //Here expression is on the stack
                }
                ExpressionString top = expression.pop();
                //int topType=expressionType;
                for (Iterator<ExpressionString> e = idOrLiteralList.iterator(); e.hasNext();) {
                    ExpressionString o = e.next();
                    if (o.IsRounded()) {
                        expression.push(new ExpressionString("__round(" + top + "," + String.valueOf(o.Id().getJavaType().getMaxFractionLength()) + ")", top.type));
                        expression.peek().type=Constants.BIGDECIMAL;
                    } else {
                        expression.push(top);
                    }
                    expressionType = top.type;
                    if (o.Id().getJavaType().getMaxScalingLength() != 0) {
                        expression.push(new ExpressionString(doScaleLiteral(o.Id(), expression.pop().toString(), true), Constants.BIGDECIMAL));
                    }
                    String assign = TranslationTable.getInstance().getAssignString(o.Id(), null, false,true,true);
                    ClassFile.println(assign);
                }
                break;
            case 1:
                if (idOrLiteralList.size() != 1) {
                    break;
                }
                ExpressionString o = idOrLiteralList.get(0);
                if (o.Id() != null) {
                    doIdOp(o.Id(), "*");
                } else {
                    doLiteralOp(o.Lit(), "*");
                }
                idOrLiteralList = new ArrayList<ExpressionString>();
                n.nodeChoice.choice.accept(this);
                top = expression.pop();
                //topType=expressionType;
                for (Iterator<ExpressionString> e = idOrLiteralList.iterator(); e.hasNext();) {
                    o = e.next();
                    expression.push(top);
                    expressionType = top.type;
                    doIdOp(o.Id(), "*");
                    if (o.IsRounded()) {
                        expression.push(new ExpressionString("__round(" + expression.peek() + "," + String.valueOf(o.Id().getJavaType().getMaxFractionLength()) + ")", expression.pop().type));
                        expression.peek().type=Constants.BIGDECIMAL;
                    }
                    if (o.Id().getJavaType().getMaxScalingLength() != 0) {
                        expression.push(new ExpressionString(doScaleLiteral(o.Id(), expression.pop().toString(), true), Constants.BIGDECIMAL));
                    }
                    String assign = TranslationTable.getInstance().getAssignString(o.Id(), null, false,true,true);
                    ClassFile.println(assign);
                }
                break;
        }
    }

    @Override
    public void visit(SubtractStatement n) {

        initExpressionType();
        if (n.nodeOptional1.present() || n.nodeOptional.present()) {
            doArithTry();
        }

        expression.clear();
        idOrLiteralList = new ArrayList<ExpressionString>();
        switch (n.nodeChoice.which) {
            case 0:
                NodeSequence nodeseq = (NodeSequence) n.nodeChoice.choice;
                nodeseq.elementAt(0).accept(this);
                for (Iterator<ExpressionString> e = idOrLiteralList.iterator(); e.hasNext();) {
                    ExpressionString o = e.next();
                    if (o.Id() != null) {
                        doIdOp(o.Id(), "+");
                    } else {
                        doLiteralOp(o.Lit(), "+");
                    }
                }
                if (idOrLiteralList.size() > 1) {
                    //Create a temporary variable
                    expressionType = expression.peek().type;
                    String assign = TranslationTable.getInstance().getAssignString(null, null, false,true,true);
                    ClassFile.println(assign);
                    expression.push("temp" + new Integer(SymbolTable.tempVariablesMark - 1).toString().trim());
                } else {
                    //Here expression is on the stack
                }
                NodeChoice nodech = (NodeChoice) nodeseq.elementAt(2);
                switch (nodech.which) {
                    case 0:
                        idOrLiteralList = new ArrayList<ExpressionString>();
                        NodeSequence nodeseq2 = (NodeSequence) nodech.choice;
                        nodeseq2.elementAt(0).accept(this);
                        ExpressionString top = expression.pop();
                        //int topType=expressionType;
                        if (idOrLiteralList.get(0).Id() != null) {
                            TranslationTable.getInstance().doOp(idOrLiteralList.get(0).Id(), "-");
                            TranslationTable.getInstance().doOp(top, "-");
                        } else if (idOrLiteralList.get(0).Lit() != null) {
                            TranslationTable.getInstance().doOp(idOrLiteralList.get(0).Lit(), "-");
                            TranslationTable.getInstance().doOp(top, "-");
                        }
                        idOrLiteralList = new ArrayList<ExpressionString>();
                        nodeseq2.elementAt(2).accept(this);
                        top = expression.pop();
                        //topType=expressionType;
                        for (Iterator<ExpressionString> e = idOrLiteralList.iterator(); e.hasNext();) {
                            ExpressionString o = e.next();
                            if (o.IsRounded()) {
                                expression.push(new ExpressionString("__round(" + top + "," + String.valueOf(o.Id().getJavaType().getMaxFractionLength()) + ")", top.type));
                                expression.peek().type=Constants.BIGDECIMAL;
                            } else {
                                expression.push(top);
                            }
                            //expressionType=topType;
                            if (o.Id().getJavaType().getMaxScalingLength() != 0) {
                                expression.push(new ExpressionString(doScaleLiteral(o.Id(), expression.pop().toString(), true), Constants.BIGDECIMAL));
                            }
                            String assign = TranslationTable.getInstance().getAssignString(o.Id(), null, false,true,true);
                            ClassFile.println(assign);
                        }
                        break;
                    case 1:
                        idOrLiteralList = new ArrayList<ExpressionString>();
                        nodech.choice.accept(this);
                        top = expression.pop();
                        //topType=expressionType;
                        for (Iterator<ExpressionString> e = idOrLiteralList.iterator(); e.hasNext();) {
                            ExpressionString o = e.next();
                            /*
                            if(o.isRounded()) {
                            expression.push("__round("+top+"," +String.valueOf(o.Id().getJavaType().getMaxFractionLength())+")");
                            }
                            else expression.push(top);
                             */
                            expression.push(NameUtil.getJavaName(o.Id(), false));
                            expressionType = o.Id().getIdentifierType();
                            //doIdOp(o.Id(),"-");
                            TranslationTable.getInstance().doOp(top, "-");
                            if (o.Id().getJavaType().getMaxScalingLength() != 0) {
                                expression.push(new ExpressionString(doScaleLiteral(o.Id(), expression.pop().toString(), true),Constants.BIGDECIMAL));
                            }
                            String assign = TranslationTable.getInstance().getAssignString(o.Id(), null, false,true,true);
                            ClassFile.println(assign);
                        }
                        break;
                }
                break;
            case 1:
                nodeseq = (NodeSequence) n.nodeChoice.choice;
                nodeseq.elementAt(1).accept(this);
                SymbolProperties from = props;
                nodeseq.elementAt(3).accept(this);
                doCorrespondingOp(from, props, "-", true, true);
        }
        if (n.nodeOptional1.present() || n.nodeOptional.present()) {
            doCatchArith(n.nodeOptional, n.nodeOptional1);
        }
    }

    private void doArithTry() {
        ClassFile.println("try {");
        ClassFile.tab();
        ClassFile.println("__enableExceptions();");
    }

    private void doTry() {
        ClassFile.println("try {");
        ClassFile.tab();
    }

    private void doCatchArith(Node n, NodeOptional n2) {
        ClassFile.println("__resetExceptions();");
        n2.accept(this);
        ClassFile.backTab();
        ClassFile.println("} catch(ArithmeticException ae) {");
        ClassFile.tab();
        ClassFile.println("__resetExceptions();");
        exceptionPrintStackTrace("ae");
        n.accept(this);
        ClassFile.backTab();
        ClassFile.println("}");
    }

    private void exceptionPrintStackTrace(String e) {
        if (RESConfig.getInstance().isExceptionPrintStackTraceOn()) {
            ClassFile.println("__printStackTrace(" + e + ");");
        }
    }

    @Override
    public void visit(MultiplyStatement n) {
        if (n.nodeOptional1.present() || n.nodeOptional.present()) {
            doArithTry();
        }
        n.multiplyBody.accept(this);
        if (n.nodeOptional1.present() || n.nodeOptional.present()) {
            doCatchArith(n.nodeOptional, n.nodeOptional1);
        }
    }

    @Override
    public void visit(DivideBody n) {

        initExpressionType();
        expression.clear();
        idOrLiteralList = new ArrayList<ExpressionString>();
        switch (n.nodeChoice.which) {
            case 0:
                NodeSequence nodeseq = (NodeSequence) n.nodeChoice.choice;
                if (!((NodeOptional) nodeseq.elementAt(3)).present()) {
                    nodeseq.elementAt(0).accept(this);
                    if (idOrLiteralList.get(0).Id() != null) {
                        doIdOp(idOrLiteralList.get(0).Id(), "/");
                    } else {
                        doLiteralOp(idOrLiteralList.get(0).Lit(), "/");
                    }
                    idOrLiteralList = new ArrayList<ExpressionString>();
                    nodeseq.elementAt(2).accept(this);
                    ExpressionString top = expression.pop();//from elementAt(0)
                    //int topType=expressionType;
                    expression.clear();
                    for (int i = 0; i < idOrLiteralList.size(); ++i) {
                        ExpressionString o = idOrLiteralList.get(i);
                        if (o.Id() != null) {
                            expression.push(top);
                            expressionType = top.type;
                            doIdOp(o.Id(), "/");
                            if (o.IsRounded()) {
                                expression.push(new ExpressionString("__round(" + expression.peek() + "," + String.valueOf(o.Id().getJavaType().getMaxFractionLength()) + ")", expression.pop().type));
                                expression.peek().type=Constants.BIGDECIMAL;
                            }
                            TranslationTable.getInstance().doOp(top, "/");
                            if (o.Id().getJavaType().getMaxScalingLength() != 0) {
                                expression.push(new ExpressionString(doScaleLiteral(o.Id(), expression.pop().toString(), true), Constants.BIGDECIMAL));
                            }
                            String assign = TranslationTable.getInstance().getAssignString(o.Id(), null, false,true,true);

                            ClassFile.println(assign);
                        }
                    }
                } else {//Giving present
                    NodeSequence nodeseq2 = (NodeSequence) ((NodeOptional) nodeseq.elementAt(3)).node;
                    nodeseq.elementAt(0).accept(this);
                    if (idOrLiteralList.size() == 1) {
                        if (idOrLiteralList.get(0).Id() != null) {
                            doIdOp(idOrLiteralList.get(0).Id(), "/");
                        } else {
                            doLiteralOp(idOrLiteralList.get(0).Lit(), "/");
                        }
                    }
                    ExpressionString top2 = expression.peek();//These two for remainder calculation
                    //int type2=expressionType;
                    idOrLiteralList.clear();
                    nodeseq.elementAt(2).accept(this);
                    expression.clear();
                    if (idOrLiteralList.get(0).Id() != null) {
                        doIdOp(idOrLiteralList.get(0).Id(), "/");
                    } else {
                        doLiteralOp(idOrLiteralList.get(0).Lit(), "/");
                    }
                    ExpressionString top = expression.peek();//from elementAt(2)
                    //int topType=expressionType;
                    TranslationTable.getInstance().doOp(top2, "/");
                    idOrLiteralList.clear();
                    nodeseq2.elementAt(1).accept(this);
                    ExpressionString top3 = expression.pop();
                    //int type3=expressionType;
                    if (((NodeOptional) nodeseq2.elementAt(2)).present()) {//Also Remainder
                        ExpressionString o = idOrLiteralList.get(0);
                        if (o.Id() != null) {
                            if (o.IsRounded()) {
                                expression.push(new ExpressionString("__round(" + top3 + "," + String.valueOf(o.Id().getJavaType().getMaxFractionLength()) + ")", top3.type));
                                expression.peek().type=Constants.BIGDECIMAL;
                            } else {
                                expression.push(top3);
                            }
                            expressionType = top3.type;
                            if (o.Id().getJavaType().getMaxScalingLength() != 0) {
                                expression.push(new ExpressionString(doScaleLiteral(o.Id(), expression.pop().toString(), true), Constants.BIGDECIMAL));
                            }
                            String assign = TranslationTable.getInstance().getAssignString(o.Id(), null, false,true,true);

                            ClassFile.println(assign);

                        }
                        if (idOrLiteralList.size() > 1) {
                            //TODO Syntax Error to be caught
                            return;
                        }
                        expression.clear();
                        expression.push(top);
                        expressionType = top.type;
                        TranslationTable.getInstance().doOp(top2, "%");
                        idOrLiteralList.clear();
                        nodeseq2.elementAt(2).accept(this);
                        if (idOrLiteralList.size() == 1) {
                            if (o.Id().getJavaType().getMaxScalingLength() != 0) {
                                expression.push(new ExpressionString(doScaleLiteral(o.Id(), expression.pop().toString(), true),Constants.BIGDECIMAL));
                            }
                            String assign = TranslationTable.getInstance().getAssignString(idOrLiteralList.get(0).Id(),
                                    null, false,true,true);
                            ClassFile.println(assign);
                        }
                    } else {//Only Giving
                        for (int i = 0; i < idOrLiteralList.size(); ++i) {
                            ExpressionString o = idOrLiteralList.get(i);
                            if (o.Id() != null) {
                                if (o.IsRounded()) {
                                    expression.push(new ExpressionString("__round(" + top3 + "," + String.valueOf(o.Id().getJavaType().getMaxFractionLength()) + ")", top3.type));
                                    expression.peek().type=Constants.BIGDECIMAL;
                                } else {
                                    expression.push(top3);
                                }
                                expressionType = top3.type;
                                if (o.Id().getJavaType().getMaxScalingLength() != 0) {
                                    expression.push(new ExpressionString(doScaleLiteral(o.Id(), expression.pop().toString(), true), Constants.BIGDECIMAL));
                                }
                                String assign = TranslationTable.getInstance().getAssignString(o.Id(),
                                        null, false,true,true);
                                ClassFile.println(assign);

                            }
                        }
                    }
                }
                break;
            case 1:
                nodeseq = (NodeSequence) n.nodeChoice.choice;
                nodeseq.elementAt(2).accept(this);
                if (idOrLiteralList.size() == 1) {
                    if (idOrLiteralList.get(0).Id() != null) {
                        doIdOp(idOrLiteralList.get(0).Id(), "/");
                    } else {
                        doLiteralOp(idOrLiteralList.get(0).Lit(), "/");
                    }
                }
                ExpressionString top2 = expression.peek();//These two for remainder calculation
                //int type2=expressionType;
                idOrLiteralList.clear();
                nodeseq.elementAt(0).accept(this);
                expression.clear();
                if (idOrLiteralList.size() == 1) {
                    if (idOrLiteralList.get(0).Id() != null) {
                        doIdOp(idOrLiteralList.get(0).Id(), "/");
                    } else {
                        doLiteralOp(idOrLiteralList.get(0).Lit(), "/");
                    }
                }
                ExpressionString top = expression.peek();//from elementAt(0)
                //int type1=expressionType;
                TranslationTable.getInstance().doOp(top2, "/");
                idOrLiteralList.clear();
                nodeseq.elementAt(4).accept(this);
                ExpressionString top3 = expression.pop();
                //int type3=expressionType;
                if (((NodeOptional) nodeseq.elementAt(5)).present()) {//Also Remainder
                    ExpressionString o = idOrLiteralList.get(0);
                    if (o.Id() != null) {
                        if (o.IsRounded()) {
                            expression.push(new ExpressionString("__round(" + top3 + "," + String.valueOf(o.Id().getJavaType().getMaxFractionLength()) + ")", top3.type));
                            expression.peek().type=Constants.BIGDECIMAL;
                        } else {
                            expression.push(top3);
                        }
                        expressionType = top3.type;
                        if (o.Id().getJavaType().getMaxScalingLength() != 0) {
                            expression.push(new ExpressionString(doScaleLiteral(o.Id(), expression.pop().toString(), true), Constants.BIGDECIMAL));
                        }
                        String assign = TranslationTable.getInstance().getAssignString(o.Id(), null, false,true,true);
                        ClassFile.println(assign);

                    }
                    if (idOrLiteralList.size() > 1) {
                        return;
                    }
                    expression.clear();
                    expression.push(top);
                    TranslationTable.getInstance().doOp(top2, "%");
                    idOrLiteralList.clear();
                    nodeseq.elementAt(5).accept(this);
                    if (idOrLiteralList.size() == 1) {
                        if (idOrLiteralList.get(0).Id().getJavaType().getMaxScalingLength() != 0) {
                            expression.push(new ExpressionString(doScaleLiteral(idOrLiteralList.get(0).Id(), expression.pop().toString(), true), Constants.BIGDECIMAL));
                        }
                        String assign = TranslationTable.getInstance().getAssignString(idOrLiteralList.get(0).Id(), null, false,true,true);

                        ClassFile.println(assign);
                    }
                } else {//Only Giving
                    for (int i = 0; i < idOrLiteralList.size(); ++i) {
                        ExpressionString o = idOrLiteralList.get(i);
                        if (o.Id() != null) {
                            if (o.IsRounded()) {
                                expression.push("__round(" + top3 + "," + String.valueOf(o.Id().getJavaType().getMaxFractionLength()) + ")");
                                expression.peek().type=Constants.BIGDECIMAL;
                            } else {
                                expression.push(top3);
                            }
                            expressionType = top3.type;
                            if (o.Id().getJavaType().getMaxScalingLength() != 0) {
                                expression.push(new ExpressionString(doScaleLiteral(o.Id(), expression.pop().toString(), true), Constants.BIGDECIMAL));
                            }
                            String assign = TranslationTable.getInstance().getAssignString(o.Id(), null, false,true,true);
                            ClassFile.println(assign);

                        }
                    }
                }
                break;
        }
    }

    @Override
    public void visit(DivideStatement n) {
        if (n.nodeOptional1.present() || n.nodeOptional.present()) {
            doArithTry();
        }
        n.divideBody.accept(this);
        if (n.nodeOptional1.present() || n.nodeOptional.present()) {
            doCatchArith(n.nodeOptional, n.nodeOptional1);
        }
    }

    private boolean doCorrespondingOp(SymbolProperties from, SymbolProperties to, String op, boolean doReverseOp, boolean first) {
        if (first) {
            if (!isCorrespondingValid2(from, to)) {
                return false;
            }
        } else if (!isCorrespondingValid(from, to)) {
            return false;
        }
        if (from.getPictureString() != null && to.getPictureString() != null) {
            expression.clear();
            if (!doReverseOp) {
                doIdOp(from, op);
                doIdOp(to, op);
            } else {
                doIdOp(from, op);
                //int topType=expressionType;
                ExpressionString top = expression.pop();
                doIdOp(to, op);
                TranslationTable.getInstance().doOp(top, op);
            }
            if (to.getJavaType().getMaxScalingLength() != 0) {
                expression.push(new ExpressionString(doScaleLiteral(to, expression.pop().toString(), true), Constants.BIGDECIMAL));
            }
            String assign = TranslationTable.getInstance().getAssignString(to, null, false,true,true);
            ClassFile.println(assign);
            return true;
        }
        if (from.getChildren() != null && to.getChildren() != null) {
            for (int i = 0; i < from.getChildren().size(); ++i) {
                for (int j = 0; j < to.getChildren().size(); ++j) {
                    if (doCorrespondingOp(from.getChildren().get(i), to.getChildren().get(j), op, doReverseOp, false)) {
                        break;
                    }
                }
            }
        }
        return false;
    }
    /*
    private void doLiteralOp(ExpressionString lit,String op) {
    TranslationTable.getInstance().doLiteralOp(lit.toString(), op);
    }
     * *
     */

    private void doLiteralOp(String lit, String op) {
        TranslationTable.getInstance().doLiteralOp(lit, op);
    }

    private void doIdOp(SymbolProperties id, String op) {
        TranslationTable.getInstance().doIdOp(id, op);
    }

    @Override
    public void visit(ArithIdentifier n) {
        super.visit(n);
        if (idOrLiteralList != null) {
            idOrLiteralList.add(new ExpressionString(props, n.nodeOptional.present()));
        }

    }

    @Override
    public void visit(ArithIdentifierList n) {
        idOrLiteralList = new ArrayList<ExpressionString>();
        super.visit(n);
    }

    @Override
    public void visit(IdOrLiteral n) {
        super.visit(n);
        if (idOrLiteralList != null) {
            switch (n.nodeChoice.which) {
                case 0:
                    idOrLiteralList.add(new ExpressionString(props));
                    break;
                case 1:
                    idOrLiteralList.add(new ExpressionString(literal));
                    break;
            }
        }
    }

    @Override
    public void visit(IdOrLiteralList n) {
        idOrLiteralList = new ArrayList<ExpressionString>();
        super.visit(n);
    }

    @Override
    public void visit(ComputeStatement n) {
        if (n.nodeOptional1.present() || n.nodeOptional.present()) {
            doArithTry();
        }
        initExpressionType();
        Stack<ExpressionString> exprLHS = new Stack<ExpressionString>();
        for (Enumeration<Node> e = n.nodeList.elements(); e.hasMoreElements();) {
            NodeSequence nodeseq = (NodeSequence) e.nextElement();
            nodeseq.elementAt(0).accept(this);
            ExpressionString idr = new ExpressionString(props, ((NodeOptional) nodeseq.elementAt(1)).present());
            exprLHS.push(idr);
        }
        
        n.arithmeticExpression.accept(this);
        
        SymbolProperties props2 = null;
        for (Iterator<ExpressionString> e = exprLHS.iterator(); e.hasNext();) {
            ExpressionString idr = e.next();
            String assign = TranslationTable.getInstance().getAssignString(idr.Id(), props2, false, true, true);
            if (assign.length() > 0) {
                ClassFile.println(assign);
            }
            props2 = idr.Id();
        }
        if (n.nodeOptional1.present() || n.nodeOptional.present()) {
            doCatchArith(n.nodeOptional, n.nodeOptional1);
        }
    }
    public java.util.regex.Pattern numPattern = java.util.regex.Pattern.compile("[-+]?[0-9]*(\\.[0-9]+)?");
    private java.util.regex.Pattern ZERO_PATTERN = java.util.regex.Pattern.compile("[-+]?[0]*(\\.[0]+)?");

    @Override
    public void visit(ExitStatement n) {
        if (consolidationInProgress) {
            return;
        }
        isLastExitStatement = true;
        ClassFile.println("return doCobolExit();");
        //super.visit(n);
    }

    @Override
    public void visit(GotoStatement n) {
        super.visit(n);
        if (RESConfig.getInstance().getProcedureFormat() == (byte) 2)//TODO NATIVE JAVA  METHOD
        {
            return;
        }
        NodeOptional nodeopt = (NodeOptional) ((NodeSequence) n.nodeChoice.choice).elementAt(1);
        ((NodeSequence) n.nodeChoice.choice).elementAt(0).accept(this);
        if (nodeopt.present()) {
            NodeSequence nodeseq = (NodeSequence) nodeopt.node;
            nodeseq.elementAt(3).accept(this);
            if (props == null) {
                return;
            }
            ClassFile.println("switch(" + new ExpressionString(props).toString() + ") {");
            ClassFile.tab();
            ClassFile.print("case 1:");
            NodeListOptional nlo = (NodeListOptional) nodeseq.elementAt(0);
            printCobolGoto(procedureProps);
            int pc = 2;
            if (nlo.present()) {
                for (Enumeration<Node> e = nlo.elements(); e.hasMoreElements();) {
                    e.nextElement().accept(this);
                    ClassFile.print("case "
                            + String.valueOf(pc++)
                            + ":");
                    printCobolGoto(procedureProps);
                }
            }
            ClassFile.backTab();
            ClassFile.println("}");
        } else {
            printCobolGoto(procedureProps);
        }
    }

    private void printCobolGoto(SymbolProperties props) {
        //Integer paragraphMarkInteger=(Integer)props.getParagraphMark();
        //if(paragraphMarkInteger==null||paragraphMarkInteger.intValue()<=0) return;
        ClassFile.println("return " + props.getJavaName1() + ";");
    }

    @Override
    public void visit(ParagraphName n) {
        super.visit(n);
        paragraphProps = SymbolTable.getScope().lookup(lastTokenString, SymbolConstants.PARAGRAPH);
        if (paragraphProps == null) {
            sectionProps = SymbolTable.getScope().lookup(lastTokenString, SymbolConstants.SECTION);
            if (sectionProps == null) {
                reportError(n, "Undefined Procedure Name " + lastTokenString + "\n");
                return;
            }
            sectionName = lastTokenString;
            return;
        }
        paragraphName = lastTokenString;
    }

    @Override
    public void visit(SectionName n) {
        super.visit(n);
        sectionName = lastTokenString;
        sectionProps = SymbolTable.getScope().lookup(sectionName, SymbolConstants.SECTION);
    }

    @Override
    public void visit(ProcedureName n) {
        sectionName = paragraphName = null;
        procedureProps = paragraphProps = sectionProps = null;
        n.nodeChoice.choice.accept(this);
        if (paragraphName != null && sectionName != null) {
            paragraphProps = SymbolTable.getScope().lookup(paragraphName, sectionName);
            procedureProps = paragraphProps;
        } else if (sectionName != null) {
            procedureProps = sectionProps;
        } else if (paragraphName != null) {
            procedureProps = paragraphProps;
        }
        if (procedureProps == null) {
            reportError(n, "Unknown Paragraph " + paragraphName + ((sectionName != null) ? " IN " + sectionName : ""));
        }
    }

    @Override
    public void visit(ProcedureSection n) {

        n.sectionHeader.sectionName.accept(this);

        String declarative = "";
        if (isDeclarative) {
            declarative = ",true";
        }

        ClassFile.println("Section " + sectionProps.getJavaName1() + "=new Section(this" + declarative + ") {");
        ClassFile.println("public CobolMethod run() {");
        ClassFile.tab();
        n.paragraphs.nodeListOptional.accept(this);
        ClassFile.println("return super.run();");

        ClassFile.backTab();
        ClassFile.println("}};");
        n.paragraphs.nodeListOptional1.accept(this);

    }
    private boolean consolidationInProgress = false;
    private boolean isLastExitStatement = false;

    @Override
    public void visit(EntryStatement n) {

        n.literal.accept(this);
        paragraphProps = SymbolTable.getScope().lookup(
                paragraphName = RunTimeUtil.getInstance().stripQuotes(literal.toString(), true), SymbolConstants.PROGRAM);
        if (paragraphProps == null) {
            reportError(n, "Unknown Symbol :" + literal.toString());
            return;
        }

        ClassFile.println("public void " + paragraphProps.getJavaName1() + "Entry__(ProgramEnv __env) {");
        ClassFile.tab();
        ClassFile.println("initialize(this); ");
        if (n.nodeOptional.present()) {
            NodeList nodelist = ((UsingArgs) n.nodeOptional.node).nodeList;
            processUsingArgs1(nodelist);
        }
        ClassFile.println("doCobolGoto(" + paragraphProps.getJavaName1() + ");");
        if (n.nodeOptional.present()) {
            NodeList nodelist = ((UsingArgs) n.nodeOptional.node).nodeList;
            processUsingArgs2(nodelist);
        }
        ClassFile.backTab();
        ClassFile.endMethodScope();

        ClassFile.doProgramScope(paragraphProps, false);
        ClassFile.tab();
        ClassFile.println("public static void main(String[] args) {");
        ClassFile.tab();
        ClassFile.println("__processCmdLineArgs(args);");
        ClassFile.println("new " + SymbolTable.getScope().getFirstProgram().getJavaName2() + "()."
                + paragraphProps.getJavaName1() + "Entry__(null);");
        ClassFile.backTab();
        ClassFile.endMethodScope();

        ClassFile.println("public void execute(ProgramEnv __env) {");
        ClassFile.tab();
        ClassFile.println("new " + SymbolTable.getScope().getFirstProgram().getJavaName2() + "()."
                + paragraphProps.getJavaName1() + "Entry__(__env);");
        ClassFile.backTab();
        ClassFile.endMethodScope();
        ClassFile.backTab();
        ClassFile.println("");
        ClassFile.endProgramScope();

    }

    @Override
    @SuppressWarnings("CallToThreadDumpStack")
    public void visit(Paragraph n) {

        if ((paragraphProps = ((RESNode) n).props) == null) {
            n.nodeChoice.choice.accept(this);
        }
        //paragraphProps=SymbolTable.getScope().lookup(lastTokenString, SymbolTypes.PARAGRAPH);
        if (paragraphProps == null) {
            //paragraphProps=SymbolTable.getScope().lookup(lastTokenString, SymbolTypes.SECTION);
            if (paragraphProps == null) {
                try {
                    throw new Exception();
                } catch (Exception e) {
                    reportError(n, "Undefined Paragraph Name " + lastTokenString + "\n");
                    e.printStackTrace();
                    return;
                }
            }
        }

        if (paragraphProps.isAlteredParagraph()) {//Altered Go To
            ClassFile.println("CobolMethod " + paragraphProps.getJavaName1() + "Altered__=null;");
        }

        if (paragraphProps.isConsolidateParagraph() && !paragraphProps.isGotoTarget()) {
            return;
        }
        isLastExitStatement = false;
        String declarative = "";
        if (isDeclarative) {
            declarative = ",true";
        }
        if (paragraphProps.getParent() == null/*Entry*/ || paragraphProps.getParent().getType() == SymbolConstants.SECTION) {
            ClassFile.println("Paragraph " + paragraphProps.getJavaName1() + "=new Paragraph(this," + paragraphProps.getParent().getJavaName1()
                    + declarative + ") {");
        } else {
            ClassFile.println("Paragraph " + paragraphProps.getJavaName1() + "=new Paragraph(this" + declarative
                    + ") {");
        }
        ClassFile.println("public CobolMethod run() {");
        ClassFile.tab();
        if (paragraphProps.isAlteredParagraph()) {//Not Altered
            ClassFile.println("if(" + paragraphProps.getJavaName1() + "Altered__!=null)");
            ClassFile.println("\treturn " + paragraphProps.getJavaName1() + "Altered__;");
        }
        n.nodeChoice1.accept(this);
        if (!n.deadStatement && !isLastExitStatement) {
            ClassFile.println("return doCobolExit();");
        }
        ClassFile.backTab();
        ClassFile.println("}};");

    }

    @Override
    public void visit(AlterStatement n) {
        for (Enumeration<Node> e = n.nodeList.elements(); e.hasMoreElements();) {
            NodeSequence nodeseq = (NodeSequence) e.nextElement();
            nodeseq.elementAt(0).accept(this);
            SymbolProperties alt = procedureProps;
            nodeseq.elementAt(3).accept(this);
            ClassFile.println(alt.getJavaName1() + "Altered__=" + procedureProps.getJavaName1() + ";");
        }
    }

    @Override
    public void visit(Basis n) {
        boolean saveUMinus = doUMinus;
        int saveType = expressionType;
        n.nodeChoice.choice.accept(this);
        switch (n.nodeChoice.which) {
            case 0:
                if (doingRelationCondition) {
                    literal = new ExpressionString(props, props.getJavaType().getMaxScalingLength());
                } else {
                    literal = new ExpressionString(props);
                }
                if (doUMinus) {
                    if (props.getIdentifierType() < Constants.BIGDECIMAL) {
                        literal.literal.insert(0, '-');
                    } else {
                        literal.literal.append(".negate()");
                    }
                }
                //if(expressionType!=literal.type)
                //   expression.push(TranslationTable.getInstance().convertType(
                //		literal, expressionType=Math.max(literal.type, expressionType),
                //		literal.type));
                //else
                expressionType = Math.max(literal.type, expressionType);
                expression.push(literal);
                break;
            case 1:
                literal = formatLiteral(literal);
                if (doUMinus) {
                    literal.literal.insert(0, '-');
                }
                if (expressionType == Constants.FLOAT || expressionType == Constants.DOUBLE) {
                    expression.push(TranslationTable.getInstance().convertType(literal, Constants.BIGDECIMAL, expressionType));
                    expressionType = Constants.BIGDECIMAL;
                } else {
                    expressionType = Math.max(saveType, expressionType);
                }
                break;
            case 2:
                literal = expression.pop();
                literal.literal.insert(0, "(").append(")");
                if (saveUMinus) {
                    if (props.getIdentifierType() < Constants.BIGDECIMAL) {
                        literal.literal.insert(0, '-');
                    } else {
                        literal.literal.append(".negate()");
                    }
                }
                expression.push(literal);
                expressionType = Math.max(saveType, literal.type);
        }
        expression.push(literal);
        doUMinus = false;

    }

    @Override
    public void visit(Power n) {
        String exprString = "";
        doUMinus = false;
        if (n.nodeOptional.present()) {
            n.nodeOptional.node.accept(this);
            doUMinus = lastTokenString.charAt(0) == '-';
        }
        n.basis.accept(this);

        if (n.nodeListOptional.present()) {
            for (Enumeration<Node> e = n.nodeListOptional.elements(); e.hasMoreElements();) {
                Node node = e.nextElement();
                ExpressionString arg1 = getExpression();
                node.accept(this);
                ExpressionString arg2 = getExpression();
                expression.push(TranslationTable.getInstance().doOp(
                        TranslationTable.POW_OP, arg1, arg2));
            }
        }
    }

    @Override
    public void visit(ReturnStatement n) {
    }
    private boolean doingArithmeticExpression = false;

    @Override
    public void visit(ArithmeticExpression n) {
        doingArithmeticExpression = true;
        String exprString = "";
        n.timesDiv.accept(this);
        if (n.nodeListOptional.present()) {
            for (Enumeration<Node> e = n.nodeListOptional.elements(); e.hasMoreElements();) {
                Node node = e.nextElement();
                ((NodeSequence) node).elementAt(0).accept(this);
                String op = lastTokenString;
                ExpressionString arg1 = getExpression();
                node.accept(this);
                ExpressionString arg2 = getExpression();
                expression.push(TranslationTable.getInstance().doOp(op, arg1, arg2));
            }

        }
        doingArithmeticExpression = false;
    }

    @Override
    public void visit(TimesDiv n) {
        String exprString = "";
        n.power.accept(this);
        if (n.nodeListOptional.present()) {
            for (Enumeration<Node> e = n.nodeListOptional.elements(); e.hasMoreElements();) {
                Node node = e.nextElement();
                ((NodeSequence) node).elementAt(0).accept(this);
                String op = lastTokenString;
                ExpressionString arg1 = getExpression();
                ((NodeSequence) node).elementAt(1).accept(this);
                ExpressionString arg2 = getExpression();
                expression.push(TranslationTable.getInstance().doOp(op, arg1, arg2));
            }
        }
    }

    @Override
    public void visit(AcceptStatement n) {
        n.identifier.accept(this);
        String exprString = null;
        if (n.nodeOptional.present()) {
            NodeChoice nodech = (NodeChoice) n.nodeOptional.node;
            NodeSequence nodeseq = (NodeSequence) nodech.choice;
            if (nodech.which == 1) {
                return;
            }
            NodeChoice nodechoice = (NodeChoice) nodeseq.elementAt(1);
            switch (nodechoice.which) {
                case 0://Mnemonic
                case 1://Environment
                    nodechoice.accept(this);
                    if (!lastTokenString.equalsIgnoreCase("CONSOLE")) {
                        exprString = "__getEnvironment(" + formatLiteral(
                                new ExpressionString('\"' + RunTimeUtil.getInstance().stripQuotes(lastTokenString) + '\"')) + ")";
                    } else {
                        exprString = doOneAccept();
                    }
                    break;
                case 2://Date
                    if (((NodeOptional) ((NodeSequence) nodechoice.choice).elementAt(1)).present()) {
                        exprString = "Console.readDate2()";
                    } else {
                        exprString = "Console.readDate()";
                    }
                    break;
                case 3://Day
                    if (((NodeOptional) ((NodeSequence) nodechoice.choice).elementAt(1)).present()) {
                        exprString = "Console.readDay2()";
                    } else {
                        exprString = "Console.readDay()";
                    }
                    break;
                case 4://Day of Week
                    exprString = "Console.readDayOfWeek()";
                    break;
                case 5://time
                    exprString = "Console.readTime()";
                    break;
            }
            expression.push(exprString);
            expressionType = expression.peek().type = Constants.STRING;
            ClassFile.println(TranslationTable.getInstance().getAssignString(props, null, false,true,true));
            return;
        } else {
            exprString = doOneAccept();
        }

        if (exprString != null) {
            exprString = NameUtil.getJavaName(props, true).replace("%0", exprString);
            ClassFile.println(exprString + ";");
        }
    }

    private String doOneAccept() {
        String exprString = null;
        CobolSymbol sym = (CobolSymbol) props.getJavaType();
        if (sym != null) {
            switch (sym.getType()) {
                case Constants.BYTE:
                    exprString = "(byte) Console.readChar()";
                    break;
                case Constants.CHAR:
                    exprString = "Console.readChar()";
                    break;
                case Constants.SHORT:
                    exprString = "(short)Console.readInt()";
                    break;
                case Constants.INTEGER:
                    exprString = "Console.readInt()";
                    break;
                case Constants.LONG:
                    exprString = "Console.readLong()";
                    break;
                case Constants.FLOAT:
                    exprString = "(float) Console.readDouble()";
                    break;
                case Constants.DOUBLE:
                    exprString = "Console.readDouble()";
                    break;
                case Constants.BIGDECIMAL:
                    exprString = "new BigDecimal(Console.readDouble())";
                    break;
                case Constants.STRING:
                    exprString = "Console.readLine()";
                    break;
                case Constants.GROUP:
                    exprString = "Console.readBytes()";
                    break;
            }
        }
        return exprString;

    }

    @Override
    public void visit(Identifier n) {
        if (n.nodeChoice.which == 1) {
            props = SymbolTable.getScope().lookup("RETURN-CODE");
        } else {
            super.visit(n);
        }
        if (props.getSubstringWorkSpace() != null
                && props.getSubstringWorkSpace().size() > 0) {
            props.getSubstringWorkSpace().set(0, props.getSubstringWorkSpace().get(0).replace("%%", "-1"));
        }
    }

    @Override
    public void visit(IntrinsicFunction n) {
        int[] functionArgType = SymbolConstants.getFunctionArgTypes(((NodeChoice) n.nodeSequence.elementAt(1)).which);
        String exprString = "__function__" + String.valueOf(((NodeChoice) n.nodeSequence.elementAt(1)).which) + "__";
        doit:
        if (((NodeOptional) n.nodeSequence.elementAt(2)).present()) {

            NodeOptional nodeopt = (NodeOptional) ((NodeSequence) ((NodeOptional) n.nodeSequence.elementAt(2)).node).elementAt(1);
            if (nodeopt.present()) {
                NodeChoice nodechoice = (NodeChoice) nodeopt.node;
                NodeSequence nodeseq = (NodeSequence) nodechoice.choice;
                int saveType = expressionType = 0;
                nodeseq.elementAt(0).accept(this);
                saveType = Math.max(saveType, expressionType);
                if (nodechoice.which == 0) {
                    saveType = props.getIdentifierType();
                    if (functionArgType[0] < 0) {
                        exprString += SymbolConstants.getSQL(props.getIdentifierType());
                    } else if (functionArgType.length > 1) {
                        for (int i = 1; i < functionArgType.length; i++) {
                            if (functionArgType[i] >= saveType) {
                                saveType = functionArgType[i];
                                break;
                            }
                        }
                        if (saveType == Constants.BIGDECIMAL) {
                            saveType = Constants.DOUBLE;
                        }
                        exprString += SymbolConstants.getSQL(saveType);
                    }
                    expressionType = saveType;
                    exprString += "(";
                    if (props != null) {
                        StringBuilder indexes = new StringBuilder();
                        indexes.append("new __IndexAll(").
                                append(props.findNearestCobolBean().getCobolBeanName()).append(",\"").
                                append("get").append(props.getJavaName2()).append("\"");
                        for (SymbolProperties oc : props.getOccursParents()) {
                            indexes.append(',').append(oc.getMaxOccurs());
                        }
                        indexes.append(')');
                        exprString += indexes.toString();
                        exprString += ")";
                        break doit;
                    }
                } else {
                    exprString += "(";
                    ArrayList<ExpressionString> exprStrings = new ArrayList<ExpressionString>();             
                    exprStrings.add(getExpression());
                    saveType = expressionType;
                    if (((NodeListOptional) nodeseq.elementAt(1)).present()) {
                        for (Enumeration<Node> e = ((NodeListOptional) nodeseq.elementAt(1)).elements(); e.hasMoreElements();) {
                            expressionType = 0;
                            e.nextElement().accept(this);
                            saveType = Math.max(saveType, expressionType);
                            exprStrings.add(getExpression());
                        }
                    }
                    if (functionArgType.length > 1) {
                        for (int i = 1; i < functionArgType.length; i++) {
                            if (functionArgType[i] >= saveType) {
                                saveType = functionArgType[i];
                                break;
                            }
                        }
                        if (saveType == Constants.BIGDECIMAL) {
                            saveType = Constants.DOUBLE;
                        }
                        expressionType = saveType;
                        for (int i = 0; i < exprStrings.size(); ++i) {
                            exprString += TranslationTable.getInstance().convertType(
                                    exprStrings.get(i).toString(), saveType, exprStrings.get(i).type);
                            if (i < exprStrings.size() - 1) {
                                exprString += ",";
                            }
                        }
                    }
                }
                exprString += ")";
                if (functionArgType[0] > 0) {
                    expressionType = functionArgType[0];
                }
            } else {
                exprString += "()";
                expressionType = functionArgType[0];
            }
        } else {
            exprString += "()";
            expressionType = functionArgType[0];
        }
        expression.push(exprString);
        literal = getExpression();
    }

    @Override
    public void visit(FunctionArgument n) {
        super.visit(n);
        switch (n.nodeChoice.which) {
            case 0:
                expression.push(NameUtil.getJavaName(props, false));
                expressionType = props.getIdentifierType();
                break;
            case 1:
                expression.push(formatLiteral(literal));
                break;
        }
    }

    private String declareTemp(SymbolProperties pr) {
        String ret;
        ClassFile.println(SymbolConstants.get(props.getIdentifierType()) + " "
                + (ret = "temp" + String.valueOf(SymbolTable.tempVariablesMark++))
                + ";");
        return ret;
    }

    private String declareTempArr(SymbolProperties pr) {
        String ret;
        String modif = "";
        for (int i = props.getNoOccursSubscripts(); i > 0; --i) {
            modif += "[]";
        }
        ClassFile.println(SymbolConstants.get(props.getIdentifierType()) + modif + " "
                + (ret = "temp" + String.valueOf(SymbolTable.tempVariablesMark++))
                + ";");
        return ret;
    }

    @Override
    public void visit(SpecialRegister n) {
        switch (n.nodeChoice.which) {
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
                n.nodeChoice.choice.accept(this);
                props = SymbolTable.getScope().lookup(lastTokenString, SymbolConstants.DATA);
                if (props != null) {
                    literal = new ExpressionString(new ExpressionString(props).toString());
                    literal.type = Constants.STRING;
                    break;
                }
            default:
                literal = new ExpressionString(0);
                break;
            case 1:
                n.nodeChoice.choice.accept(this);
                literal = new ExpressionString(props.getLength());
                break;
        }
    }

    @Override
    public void visit(NodeToken n) {
        //super.visit(n);
        lastTokenString = n.tokenImage;
        if (doNumericConstant) {
            numericConstant += n.tokenImage;
        }
        if (doQualified) {
            if (qualified == null) {
                qualified = new Stack<String>();
            }
            if (n.tokenImage != null) {
                if (n.tokenImage.equalsIgnoreCase("IN")
                        || n.tokenImage.equalsIgnoreCase("OF")); else {
                    qualified.push(n.tokenImage);
                }
            }
        } else if (doLiteral) {
            literal.literal.append(n.tokenImage);
        } else if (doingAcceptOption) {
            if (optionStack == null) {
                optionStack = new Stack<String>();
            }
            optionStack.push(n.tokenImage);
        } /*else
        if (doingRelationalOp){
        switch(n.kind) {
        case CobolParserConstants.NOT: break;
        case CobolParserConstants.GREATER: break;
        case CobolParserConstants.LESS: break;
        case CobolParserConstants.EQUAL: break;
        case CobolParserConstants.MORETHANCHAR: break;
        case CobolParserConstants.MORETHANOREQUAL: break;
        case CobolParserConstants.LESSTHANCHAR: case CobolParserConstants.LESSTHANOREQUAL: break;
        case CobolParserConstants.EQUALCHAR: default:break;
        }
        }*/
    }

    @Override
    public void visit(QualifiedDataName n) {
        qualified.clear();
        doQualified = true;
        super.visit(n);
        processQualifiedStack(n);
        doQualified = false;
    }

    private void processQualifiedStack(Node n) {

        SymbolProperties props2 = null;
        props = null;
        if (qualified == null || qualified.size() <= 0) {
            return;
        }
        String curr = null;
        String prev = null;
        curr = (String) qualified.pop();
        do {
            if (qualified.size() > 0) {
                prev = curr;
                curr = (String) qualified.pop();
                if (props2 != null) {
                    props2 = findChild(props2, curr);
                } else {
                    props2 = SymbolTable.getScope().lookup(curr, prev);
                }
                if (props2 == null) {
                    reportError(n, "Unknown Symbol : " + curr + " IN " + prev);
                    break;
                }
            } else {
                props2 = SymbolTable.getScope().lookup(curr);
                if (props2 == null) {
                    reportError(n, "Unknown Symbol : " + curr);
                    break;
                }
            }

            if (props2.isVaryingArray() && props2.getParent().isVarying()) {
                props2 = props2.getParent();
            }

        } while (qualified != null && qualified.size() > 0);


        try {
            props = (SymbolProperties) props2.clone();
        } catch (CloneNotSupportedException e) {
            reportError(n, e.getMessage());
        }
        props.setIndexesWorkSpace(null);//subscripts will fill this
        props.setSubstringWorkSpace(null);//LeftMost... and Length will fill this
        props.setIdentifierType(props.getJavaType().getType());
        expressionType = props.getIdentifierType();

    }

    private SymbolProperties findChild(SymbolProperties props, String name) {
        if (props.getChildren() == null || props.getChildren().size() <= 0) {
            return null;
        }
        for (SymbolProperties ch : props.getChildren()) {
            if (ch.getDataName().equalsIgnoreCase(name)) {
                return ch;
            }
        }
        SymbolProperties ret = null;
        for (SymbolProperties ch : props.getChildren()) {
            ret = findChild(ch, name);
            if (ret != null) {
                return ret;
            }
        }
        return ret;
    }

    private SymbolProperties findProgram(SymbolProperties props) {
        if (props == null) {
            return null;
        }
        do {
            props = props.getParent();
        } while (props != null && !props.isProgram());
        return props;
    }

    @Override
    public void visit(Subscript n) {
        SymbolProperties propsDataName = props;

        if (props.getIndexesWorkSpace() == null) {
            props.setIndexesWorkSpace(new ArrayList<String>());
        }

        switch (n.nodeChoice.which) {
            case 0:
                n.nodeChoice.choice.accept(this);
                int i = 0,
                 j = 0;
                do {
                    if ((j = literal.literal.indexOf(",", i)) < 0) {
                        j = literal.literal.length();
                    }
                    propsDataName.getIndexesWorkSpace().add(stripLeadingZeros(literal.literal.substring(i, j)));
                    i = j + 1;
                } while (i < literal.toString().length());
                break;
            case 1:
                NodeSequence seq = (NodeSequence) n.nodeChoice.choice;
                seq.elementAt(0).accept(this);
                String sub = new ExpressionString(props).toString();
                CobolSymbol sym = props.getJavaType();
                if (sym != null) {
                    if (sym.getType() == Constants.LONG) {
                        sub = "(int)" + sub;
                    } else if (sym.getType() == Constants.BIGDECIMAL) {
                        sub += ".intValue()";
                    }
                }
                if (((NodeOptional) seq.elementAt(1)).present()) {
                    NodeSequence seq2 = (NodeSequence) ((NodeOptional) seq.elementAt(1)).node;
                    NodeChoice ch2 = (NodeChoice) seq2.elementAt(0);
                    switch (ch2.which) {
                        case 0:
                            sub += "+";
                            break;
                        case 1:
                            sub += "-";
                            break;
                    }
                    seq2.elementAt(1).accept(this);
                    i = j = 0;
                    do {
                        if ((j = literal.literal.indexOf(",", i)) < 0) {
                            j = literal.literal.length();
                            sub += stripLeadingZeros(literal.literal.substring(i, j));
                            break;
                        }
                        propsDataName.getIndexesWorkSpace().
                                add(stripLeadingZeros(literal.literal.substring(i, j)));
                        //literal.literal.replace(i, j, stripLeadingZeros(literal.literal.substring(i,j)).toString());
                        i = j + 1;
                    } while (i < literal.toString().length());
                    //sub+=literal.toString();
                }
                propsDataName.getIndexesWorkSpace().add(sub);
                break;
            case 2:
        }
        //}
        //propsDataName.setIndexesWorkSpace(a);
        props = propsDataName;
        //super.visit(n);
    }

    @Override
    public void visit(LeftmostCharacterPosition n) {
        SymbolProperties propsDataName = props;
        initExpressionType();
        super.visit(n);
        if (expression.size() > 0 && expressionType < Constants.BIGDECIMAL) {//TODO REPORT ERROR ON NOT INTEGER SUBSCRIPT
            propsDataName.setSubstringWorkSpace(new ArrayList<String>());
            if (numPattern.matcher(expression.peek3()).matches()) {
                propsDataName.getSubstringWorkSpace().add(
                        new BigDecimal(stripLeadingZeros(expression.pop3())).subtract(BigDecimal.ONE).toPlainString());
            } else {
                propsDataName.getSubstringWorkSpace().add(expression.pop3() + "%%");
            }
        }
        props = propsDataName;
        expressionType = Constants.STRING;
    }

    @Override
    public void visit(Length n) {
        SymbolProperties propsDataName = props;
        initExpressionType();
        super.visit(n);
        if (propsDataName.getSubstringWorkSpace() == null) {
            propsDataName.setSubstringWorkSpace(new ArrayList<String>());
            propsDataName.getSubstringWorkSpace().add("0");
        }
        if (expression.size() > 0 && expressionType < Constants.BIGDECIMAL) {//TODO REPORT ERROR ON NOT INTEGER SUBSCRIPT
            if (numPattern.matcher(propsDataName.getSubstringWorkSpace().get(0)).matches()) {
                if (numPattern.matcher(expression.peek3()).matches()) {
                    propsDataName.getSubstringWorkSpace().add(
                            new BigDecimal(propsDataName.getSubstringWorkSpace().get(0)).add(new BigDecimal(stripLeadingZeros(expression.pop3()))).toPlainString());
                } else {
                    propsDataName.getSubstringWorkSpace().add(
                            propsDataName.getSubstringWorkSpace().get(0).replace("%%", "-1")
                            + "+" + stripLeadingZeros(expression.pop3()));
                }
            } else if (numPattern.matcher(expression.peek3()).matches()) {
                propsDataName.getSubstringWorkSpace().add(
                        propsDataName.getSubstringWorkSpace().get(0).replace("%%", "")
                        + "+" + new BigDecimal(stripLeadingZeros(expression.pop3())).subtract(BigDecimal.ONE).toPlainString());
            } else {
                propsDataName.getSubstringWorkSpace().add(
                        propsDataName.getSubstringWorkSpace().get(0).replace("%%", "-1")
                        + "+" + stripLeadingZeros(expression.pop3()));
            }
        }
        props = propsDataName;
    }

    @Override
    public void visit(ProgramIdParagraph n) {
        SymbolProperties pgm = SymbolTable.getScope().lookup(n.programName.cobolWord.nodeToken.tokenImage, SymbolConstants.PROGRAM);
        if (pgm == null) {
            reportError(n, "Fatal Error: Cobol2Java.visit(ProgramIdParagraph):"
                    + n.programName.cobolWord.nodeToken.tokenImage);
        }
        SymbolTable.getScope().startProgram(pgm);
        super.visit(n);
    }

    @Override
    public void visit(NestedProgramIdParagraph n) {
        SymbolProperties pgm = SymbolTable.getScope().lookup(n.programName.cobolWord.nodeToken.tokenImage, SymbolConstants.PROGRAM);
        if (pgm == null) {
            reportError(n, "Fatal Error: Cobol2Java.visit(ProgramIdParagraph):"
                    + n.programName.cobolWord.nodeToken.tokenImage);
        }
        SymbolTable.getScope().startProgram(pgm);
        super.visit(n);
    }

    @Override
    public void visit(Literal n) {
        literal = new ExpressionString();
        if (n.nodeChoice.which == 3) {
            n.nodeChoice.choice.accept(this);
        } else if (n.nodeChoice.which == 4) {
            n.nodeChoice.choice.accept(this);
        } else if (n.nodeChoice.which == 5) {
            NodeSequence nodeseq = (NodeSequence) n.nodeChoice.choice;
            if (((NodeOptional) nodeseq.elementAt(1)).present()) {
                ((NodeOptional) nodeseq.elementAt(1)).accept(this);
            } else {
                props = null;
                for (SymbolProperties f : SymbolTable.getScope().getCurrentProgram().getChildren()) {
                    if (f.isFile()) {
                        props = f;
                        break;
                    }

                }
            }
            if (props == null || !props.isFile()) {
                reportError(n, "Unknown LINAGE-COUNTER either file name missing or invalid.");
                return;
            }
            literal = new ExpressionString(props.getJavaName1() + ".linageCounter()");
            literal.setType(Constants.INTEGER);
        } else {
            doLiteral = true;
            n.nodeChoice.choice.accept(this);
            doLiteral = false;
        }

        if (n.nodeOptional.present() && (n.nodeChoice.which == 0 || n.nodeChoice.which == 1)) {
            literal.isAll = true;
        }
    }
    private String numericConstant = "";
    private boolean doNumericConstant = false;

    @Override
    public void visit(NumericConstant n) {
        numericConstant = "";
        doNumericConstant = true;
        super.visit(n);
        literal = new ExpressionString(numericConstant);
        doNumericConstant = false;
    }
    private boolean isPreviousRelationCondition = false;
    private ExpressionString previousRelationSubject = null;
    private int previousRelationOp = 0;
    private int relop = 0;

    @Override
    public void visit(Condition n) {
        isPreviousRelationCondition = false;
        n.combinableCondition.accept(this);
        if (n.nodeListOptional.present()) {
            for (int i = 0; i < n.nodeListOptional.size(); ++i) {
                NodeSequence nodeseq = (NodeSequence) n.nodeListOptional.elementAt(i);
                NodeChoice nodechoice = (NodeChoice) nodeseq.elementAt(0);
                nodeseq.elementAt(1).accept(this);
                int op = 0;
                switch (nodechoice.which) {
                    case 0:
                        op = TranslationTable.AND_CONDITION;
                        break;
                    case 1:
                    default:
                        op = TranslationTable.OR_CONDITION;
                        break;
                }
                ExpressionString temp = new ExpressionString(condition.pop()); //For reversing the order
                condition.push(TranslationTable.getInstance().doCondition(
                        op,
                        new ExpressionString(condition.pop()),
                        temp,
                        doReverseCondition));
            }
        }
    }

    @Override
    public void visit(RelationalOperator n) {
        super.visit(n);
        boolean isNot = n.nodeOptional1.present() ? !doReverseCondition : doReverseCondition;
        mapRelOp(n.nodeChoice.which, isNot);
    }

    private int mapRelOp(int which, boolean isNot) {
        relop = TranslationTable.EQ_CONDITION;
        switch (which) {
            case 0:
            case 1:
                relop = TranslationTable.GE_CONDITION;
                break;
            case 2:
            case 3:
                relop = TranslationTable.LE_CONDITION;
                break;
            case 4:
            case 5:
                relop = TranslationTable.GT_CONDITION;
                break;
            case 6:
            case 7:
                relop = TranslationTable.LT_CONDITION;
                break;
            case 8:
            case 9:
                relop = TranslationTable.EQ_CONDITION;
                break;
            case 10:
                relop = TranslationTable.NE_CONDITION;
                break;
            default:
        }
        return relop = TranslationTable.getInstance().getReverseOp(relop, isNot);
    }

    //private  int conditionType=0;
    @Override
    public void visit(AbbreviationLeaf n) {
        super.visit(n);
    }

    @Override
    public void visit(AbbreviationRest n) {
        if (!isPreviousRelationCondition) {
            return;//TODO Syntax error reporting
        }
        for (int i = 0; i < n.nodeList.size(); ++i) {
            NodeSequence seq = (NodeSequence) n.nodeList.elementAt(i);

            if (((NodeOptional) seq.elementAt(1)).present()) {
                if (((NodeOptional) seq.elementAt(0)).present()) {
                    doReverseCondition = !doReverseCondition;//for formatComparison
                }
                seq.elementAt(1).accept(this);
                if (((NodeOptional) seq.elementAt(0)).present()) {
                    doReverseCondition = !doReverseCondition;//Restore
                }
            } else {
                relop = mapRelOp(relop, ((NodeOptional) seq.elementAt(0)).present());
            }
            seq.elementAt(2).accept(this);
            String top = TranslationTable.getInstance().doCondition(relop, previousRelationSubject, getExpression(), false);
            //while(condition.size()>0)
            //top=condition.pop()+top;
            condition.push(top);
        }
    }
    private boolean doingRelationCondition = false;

    @Override
    public void visit(RelationCondition n) {
        isPreviousRelationCondition = true;
        doingRelationCondition = true;
        initExpressionType();
        n.arithmeticExpression.accept(this);
        previousRelationSubject = getExpression();
        initExpressionType();
        n.nodeChoice.accept(this);
        if (n.nodeChoice.which == 0) {
            String top = TranslationTable.getInstance().doCondition(relop, previousRelationSubject,
                    getExpression(), false);
            //while(condition.size()>0)
            //top=condition.pop()+top;
            condition.push(top);
        }
        doingRelationCondition = false;
    }

    private String formatCompareToRelop(int relopInt) {
        switch (relopInt) {
            case 0:
                return " < 0";
            case 1:
                return " <= 0";
            case 2:
                return " > 0";
            case 3:
                return " >= 0";
            case 4:
                return " == 0";
            case 5:
                return " !=0";
        }
        return "";
    }

    @Override
    public void visit(CombinableCondition n) {
        if (n.nodeOptional.present()) {
            doReverseCondition = !doReverseCondition;
        }
        super.visit(n);
        if (n.nodeOptional.present()) {
            doReverseCondition = !doReverseCondition;
        }
    }

    @Override
    public void visit(ClassCondition n) {
        isPreviousRelationCondition = false;
        n.identifier.accept(this);
        StringBuilder name = new StringBuilder(new ExpressionString(props).toString());
        int begin = name.lastIndexOf(".") + 1;
        int end = name.indexOf("(");
        switch (n.nodeChoice.which) {
            case 0:
                if (props.isNumericTested()) {
                    condition.push(name.replace(begin, end, "is" + props.getJavaName2() + "Numeric").toString());
                } else {
                    condition.push(name.toString());
                }
                break;
            case 1:
                if (props.isAlphabeticTested()) {
                    condition.push(name.replace(begin, end, "is" + props.getJavaName2() + "Alphabetic").toString());
                } else {
                    condition.push(name.toString());
                }
                break;
            case 2:
                if (props.isAlphabeticTested()) {
                    condition.push(name.replace(begin, end, "is" + props.getJavaName2() + "AlphabeticLower").toString());
                } else {
                    condition.push(name.toString());
                }
                break;
            case 3:
                if (props.isAlphabeticTested()) {
                    condition.push(name.replace(begin, end, "is" + props.getJavaName2() + "AlphabeticUpper").toString());
                } else {
                    condition.push(name.toString());
                }
                break;
            case 4:
            case 5:
            case 6:
                condition.push(name.toString());
        }
        if (n.nodeOptional1.present()) {
            condition.push("!" + condition.pop());
        }
    }

    @Override
    public void visit(SignCondition n) {
        isPreviousRelationCondition = false;
        super.visit(n);
        boolean isNot = n.nodeOptional1.present() ? !doReverseCondition : doReverseCondition;
        int op = 0;
        switch (n.nodeChoice.which) {
            case 0:
                op = TranslationTable.POSITIVE_CONDITION;
                break;
            case 1:
                op = TranslationTable.NEGATIVE_CONDITION;
                break;
            case 2:
                op = TranslationTable.ZERO_CONDITION;
        }
        if (isNot) {
            condition.push('!' + TranslationTable.getInstance().doCondition(op,
                    previousRelationSubject, false));
        } else {
            condition.push(TranslationTable.getInstance().doCondition(op,
                    previousRelationSubject, false));
        }
        expressionType = Constants.INTEGER;
    }

    @Override
    public void visit(ConditionNameCondition n) {
        props = null;
        isPreviousRelationCondition = false;
        super.visit(n);
        String top = ((doReverseCondition) ? "!(%0)" : "%0");
        if (props == null) {
            return;
        }
        if (props.getLevelNumber() == 88 || previousRelationSubject == null) {
            top = top.replace("%0", new ExpressionString(props).toString());
        } else {
            expression.push(new ExpressionString(props).toString());
            expressionType = props.getIdentifierType();
            top = top.replace("%0",
                    TranslationTable.getInstance().
                    doCondition(relop, previousRelationSubject, getExpression(), false));
        }
        condition.push(top);
    }

    @Override
    public void visit(ConditionNameReference n) {
        qualified.clear();
        doQualified = true;
        n.conditionName.accept(this);
        switch (n.nodeChoice.which) {
            case 0:
                NodeSequence nodeseq = (NodeSequence) n.nodeChoice.choice;
                nodeseq.elementAt(0).accept(this);
                nodeseq.elementAt(1).accept(this);
                doQualified = false;
                processQualifiedStack(n);
                nodeseq.elementAt(2).accept(this);
                break;
            case 1:
                n.nodeChoice.choice.accept(this);
                doQualified = false;
                processQualifiedStack(n);
        }
    }

    @Override
    public void visit(SimpleCondition n) {
        switch (n.nodeChoice.which) {
            case 3:
                if (n.nodeChoice.choice instanceof NodeSequence) {
                    //String top="";
                    //if (condition.size()>0)
                    //top=condition.pop();
                    NodeSequence nodeseq = (NodeSequence) n.nodeChoice.choice;
                    nodeseq.elementAt(1).accept(this);
                    condition.push("(" + condition.pop() + ")");
                }
                break;
            case 1:
            case 2:
            case 0:
                n.nodeChoice.choice.accept(this);
        }
    }
    private boolean doReverseCondition = false;
    private boolean doUMinus = false;

    @Override
    public void visit(IfStatement n) {

        initExpressionType();
        doReverseCondition = false;
        condition = new Stack<String>();
        n.condition.accept(this);
        doReverseCondition = false;
        ClassFile.println("if(" + condition.pop() + ") {");
        ClassFile.tab();
        switch (n.nodeChoice.which) {
            case 0:
                NodeSequence seq = (NodeSequence) n.nodeChoice.choice;
                seq.elementAt(0).accept(this);
                break;
            case 1:
            default:
        }
        //n.statementList.accept(this);

        ClassFile.backTab();
        if (n.nodeOptional1.present()) {
            ClassFile.println("} else {");
            ClassFile.tab();
            if (n.nodeOptional1.node instanceof NodeSequence) {
                NodeSequence seq2 = (NodeSequence) n.nodeOptional1.node;
                NodeChoice choice = (NodeChoice) seq2.elementAt(1);
                switch (choice.which) {
                    case 0:
                        NodeSequence seq = (NodeSequence) choice.choice;
                        seq.elementAt(0).accept(this);
                        break;
                    case 1:
                    default:
                }
            }
            ClassFile.backTab();
            ClassFile.println("}");
        } else {
            ClassFile.println("}");
        }
    }

    @Override
    public void visit(PerformBody arg0) {
        //super.visit(arg0);
        switch (arg0.nodeChoice.which) {
            case 1:
                if (arg0.nodeChoice.choice instanceof NodeSequence) {
                    NodeSequence nodeseq = (NodeSequence) arg0.nodeChoice.choice;
                    if (nodeseq.nodes.size() == 2) {
                        nodeseq.accept(this);//do the descent for both
                        if (procedureProps == null) {
                            return;
                        }
                        if (!someOrAllConsolidated(procedureProps, procedureThruProps)) {
                            ClassFile.println("doCobolPerform(" + procedureProps.getJavaName1() + ((procedureThruProps == null) ? "" : ("," + procedureThruProps.getJavaName1())) + ");");
                        }
                        if (((NodeOptional) nodeseq.elementAt(1)).present()
                                && endBlockScopeCriterion != null && endBlockScopeCriterion.size() > 0) {
                            Stack<String> st = endBlockScopeCriterion.pop();
                            while (st.size() > 0) {
                                String endScope = st.pop();
                                if (endScope.charAt(0) == '}') {
                                    ClassFile.backTab();
                                }
                                ClassFile.println(endScope);
                            }
                        }
                    }
                }
                break;
            case 0:
                if (arg0.nodeChoice.choice instanceof NodeSequence) {
                    NodeSequence nodeseq = (NodeSequence) arg0.nodeChoice.choice;
                    nodeseq.elementAt(0).accept(this);//do the descent for both
                    nodeseq.elementAt(1).accept(this);
                    if (endBlockScopeCriterion != null && endBlockScopeCriterion.size() > 0) {
                        Stack<String> st = endBlockScopeCriterion.pop();
                        while (st.size() > 0) {
                            String endScope = st.pop();
                            if (endScope.charAt(0) == '}') {
                                ClassFile.backTab();
                            }
                            ClassFile.println(endScope);
                        }
                    }
                }
                break;
            default:
        }
    }

    private boolean someOrAllConsolidated(SymbolProperties from, SymbolProperties thru) {
        /*
        if(thru==null) thru=from;
        if(thru.getParagraphMark()>=from.getParagraphMark()) {
        SymbolProperties sectionF=null,paraF=null,sectionT=null,paraT=null;
        if(from.getType()==SymbolConstants.PARAGRAPH) {
        sectionF=from.getParent();
        paraF=from;
        } else {
        sectionF=from;
        paraF=from.getChildren().get(0);
        }
        if(thru.getType()==SymbolConstants.PARAGRAPH) {
        sectionT=thru.getParent();
        paraT=thru;
        } else {
        sectionT=thru;
        paraT=thru.getChildren().get(0);
        }
        allUnConsolidated: {
        someConsolidated: {
        if(!sectionF.getDataName().equalsIgnoreCase(sectionT.getDataName())) {
        props=sectionF.getChildren().get(sectionF.getChildren().size()-1);
        } else {
        props=paraT;
        }
        for(paragraphProps=paraF;paragraphProps!=null&&paragraphProps.getParagraphMark()<=props.getParagraphMark();paragraphProps=paragraphProps.getSibling()) {
        if(paragraphProps.isConsolidateParagraph()&&!paragraphProps.isGotoTarget()) break someConsolidated;
        }
        if(!sectionF.getDataName().equalsIgnoreCase(sectionT.getDataName())) {
        paragraphProps=sectionT.getChildren().get(0);
        } else {
        break allUnConsolidated;
        }
        for(;paragraphProps!=null&&paragraphProps.getParagraphMark()<=paraT.getParagraphMark();paragraphProps=paragraphProps.getSibling()) {
        if(paragraphProps.isConsolidateParagraph()&&!paragraphProps.isGotoTarget()) break someConsolidated;
        }
        }
        //Some Consolidated
        if(!sectionF.getDataName().equalsIgnoreCase(sectionT.getDataName())) {
        unConsolidate(paraF,sectionF.getChildren().get(sectionF.getChildren().size()-1));
        } else {
        unConsolidate(paraF,paraT);
        }
        if(!sectionF.getDataName().equalsIgnoreCase(sectionT.getDataName())) {
        if(!sectionF.getDataName().equalsIgnoreCase(sectionT.getDataName())) {
        paragraphProps=sectionT.getChildren().get(0);
        unConsolidate(paragraphProps,paraT);
        } else {
        }
        } else {
        }
        return true;
        }
        //allUnConsolidation
        }*/
        return false;
    }
    /*
    private void unConsolidate(SymbolProperties paraF,SymbolProperties thru) {
    consolidationInProgress=true;
    if(thru==null) thru=paraF;
    SymbolProperties temp=null,temp2=null;boolean lastOneToBePrnted=false;
    for(props=paraF;props!=null&&props.getParagraphMark()<=thru.getParagraphMark();props=props.getSibling()) {
    if(props.isConsolidateParagraph()&&!props.isGotoTarget()) {
    if(lastOneToBePrnted) {

    ClassFile.println("doCobolPerform("+temp.getJavaName1()+","+((temp2==null)?"null":temp2.getJavaName1())+");");
    temp=temp2=null;lastOneToBePrnted=false;
    }
    ((Node)props.getOtherData2()).accept(this);
    } else {
    if(temp==null)
    temp=props;
    else
    temp2=props;
    lastOneToBePrnted=true;
    }
    }
    if(lastOneToBePrnted) {
    ClassFile.println("doCobolPerform("+temp.getJavaName1()+","+((temp2==null)?"null":temp2.getJavaName1())+");");
    temp=temp2=null;lastOneToBePrnted=false;
    }
    consolidationInProgress=false;
    }
     */

    @Override
    public void visit(PerformOption arg0) {
        //super.visit(arg0);
        Stack<String> st = new Stack<String>();
        switch (arg0.nodeChoice.which) {
            case 0:
                String timesVar = null;
                if (arg0.nodeChoice.choice instanceof NodeSequence) {
                    NodeSequence nodeseq = (NodeSequence) arg0.nodeChoice.choice;
                    nodeseq.accept(this);
                    if (nodeseq.elementAt(0) instanceof NodeChoice) {
                        switch (((NodeChoice) nodeseq.elementAt(0)).which) {
                            case 0:
                                timesVar = new ExpressionString(props).toString();
                                expressionType = props.getIdentifierType();
                                break;//out of ((NodeChoice)nodeseq.elementAt(0)).which
                            case 1:
                                timesVar = formatLiteral(literal).toString();
                                break;//out of ((NodeChoice)nodeseq.elementAt(0)).which
                            default:
                        }
                        timesVar = TranslationTable.getInstance().convertType(timesVar, Constants.INTEGER, expressionType);
                        String tempVar = "i" + new Integer(SymbolTable.tempVariablesMark++).toString().trim();
                        ClassFile.println("for(int " + tempVar + "=" + timesVar
                                + ";" + tempVar + ">0;" + "--" + tempVar + ") {");
                        ClassFile.tab();
                        st.push("}");
                    }
                }
                break;
            case 1:
                if (arg0.nodeChoice.choice instanceof NodeSequence) {
                    NodeSequence nodeseq = (NodeSequence) arg0.nodeChoice.choice;
                    NodeOptional nodeOpt = (NodeOptional) nodeseq.elementAt(0);
                    beforeOrAfter = 0;
                    if (nodeOpt.present()) {
                        nodeOpt.accept(this);
                    }
                    doReverseCondition = true;
                    condition = new Stack<String>();
                    nodeseq.elementAt(2).accept(this);
                    if (condition != null && condition.size() > 0) {
                        if (beforeOrAfter == 0) {	//Before
                            ClassFile.println("while(" + condition.pop() + ") {");
                            ClassFile.tab();
                            st.push("}");
                        } else {	//After
                            ClassFile.println("do {");
                            ClassFile.tab();
                            st.push("} while(" + condition.pop() + ");");
                        }
                    }
                }
                break;
            case 2:
                if (arg0.nodeChoice.choice instanceof NodeSequence) {
                    endBlockScopeCriterion.push(st);//pushed ahead and returns instead of break
                    NodeSequence nodeseq = (NodeSequence) arg0.nodeChoice.choice;
                    NodeOptional nodeOpt = (NodeOptional) nodeseq.elementAt(0);
                    beforeOrAfter = 0;
                    if (nodeOpt.present()) {
                        nodeOpt.accept(this);
                    }
                    nodeseq.elementAt(2).accept(this);
                    return;
                }
            default:
        }
        endBlockScopeCriterion.push(st);
    }
    private Stack<Stack<String>> endBlockScopeCriterion = new Stack<Stack<String>>();

    @Override
    public void visit(PerformProcedure arg0) {
        if (arg0.nodeOptional.present()) {
            arg0.nodeOptional.accept(this);
            procedureThruProps = procedureProps;
        } else {
            procedureThruProps = null;
        }
        arg0.procedureName.accept(this);
    }

    @Override
    public void visit(PerformStatement n) {
        super.visit(n);
    }

    @Override
    public void visit(PerformTest arg0) {
        super.visit(arg0);
    }
    private int beforeOrAfter = 0;

    @Override
    public void visit(BeforeOrAfter n) {
        beforeOrAfter = n.nodeChoice.which;
    }

    @Override
    public void visit(PerformVarying arg0) {

        arg0.identifier.accept(this);
        saveProps = props;
        idOrLiteralList = new ArrayList<ExpressionString>();
        String fromVar, byVar;
        arg0.idOrLiteral.accept(this);
        ExpressionString o = idOrLiteralList.get(0);
        if (o.Id() != null) {
            fromVar = new ExpressionString(o.Id()).toString();
            expressionType = o.Id().getIdentifierType();
        } else {
            fromVar = formatLiteral(new ExpressionString(o.Lit())).toString();
        }
        expression.push(fromVar);
        String init = TranslationTable.getInstance().getAssignString(saveProps, null, false, false);
        idOrLiteralList = new ArrayList<ExpressionString>();
        arg0.idOrLiteral1.accept(this);
        o = idOrLiteralList.get(0);

        if (o.Id() != null) {
            byVar = new ExpressionString(o.Id()).toString();
            expressionType = o.Id().getIdentifierType();
        } else {
            byVar = formatLiteral(new ExpressionString(o.Lit())).toString();
        }

        expression.push(byVar);
        TranslationTable.getInstance().doIdOp(saveProps, "+");
        String incr = TranslationTable.getInstance().getAssignString(saveProps, null, false, false);

        if (beforeOrAfter == 0) {
            doReverseCondition = true;
            condition = new Stack<String>();
            arg0.condition.accept(this);
            ClassFile.println("for(" + init + ";" + condition.pop() + ";" + incr + ") {");
            ClassFile.tab();
            endBlockScopeCriterion.peek().add("}");
        } else {
            doReverseCondition = false;
            condition = new Stack<String>();
            arg0.condition.accept(this);
            ClassFile.println(init + ";");
            ClassFile.println("do {");
            ClassFile.tab();
            endBlockScopeCriterion.peek().add("} while(true);");
            endBlockScopeCriterion.peek().add(incr + ";");
            endBlockScopeCriterion.peek().add("if(" + condition.pop() + ") break;");
        }
    }

    @Override
    public void visit(IntegerConstant n) {
        if (doLiteral || doNumericConstant) {
            super.visit(n);
        } else {
            literal = new ExpressionString(((NodeToken) n.nodeChoice.choice).tokenImage);
        }
    }

    @Override
    public void visit(NonNumericConstant n) {
        switch (n.nodeChoice.which) {
            case 0:
                literal = new ExpressionString(((NodeToken) n.nodeChoice.choice).tokenImage);
                break;
            case 1:
                literal = new ExpressionString(((NodeToken) n.nodeChoice.choice).tokenImage);
                literal = new ExpressionString("0" + literal.toString().replace("\'", "").replace("\"", ""));
        }
    }

    @Override
    public void visit(FigurativeConstant n) {
        String lit = null;
        switch (n.nodeChoice.which) {
            case 0:
            case 1:
            case 2:
                lit = "0";
                break;
            case 3:
            case 4:
                lit = "SPACES";
                break;
            case 5:
            case 6:
                lit = "__function__" + String.valueOf(50) + "__()";
                expressionType = Constants.CHAR;
                break;
            case 7:
            case 8:
                lit = "__function__" + String.valueOf(51) + "__()";
                expressionType = Constants.CHAR;
                break;
            case 9:
            case 10:
                lit = "\"\\\"\"";
                break;
            case 11:
            case 12:
            default:
                lit = "0";
                break;
        }
        if (doLiteral) {
            literal.literal.append(lit);
        } else {
            literal = new ExpressionString(lit);
        }
    }

    @Override
    public void visit(PerformVaryingList arg0) {
        //super.visit(arg0);
        arg0.performVarying.accept(this);
        for (int i = 0; ((i < 4) && arg0.nodeListOptional.present()
                && i < arg0.nodeListOptional.size()); ++i) {
            NodeSequence nodeseq = (NodeSequence) arg0.nodeListOptional.elementAt(i);
            nodeseq.elementAt(1).accept(this);
        }
    }

    @Override
    public void visit(StringStatement n) {

        StringBuilder cLine = new StringBuilder("");
        StringBuilder single = new StringBuilder("");
        n.identifier.accept(this);
        if (props == null) {
            return;
        }
        saveProps = props;
        for (Enumeration<Node> e = n.nodeList.elements(); e.hasMoreElements();) {
            ExpressionString delimitedBy = null;
            NodeSequence nodeseq = (NodeSequence) e.nextElement();
            if (((NodeOptional) nodeseq.elementAt(1)).present()) {
                NodeSequence opt = (NodeSequence) ((NodeOptional) nodeseq.elementAt(1)).node;
                NodeChoice nodech = (NodeChoice) opt.elementAt(2);
                nodech.accept(this);
                switch (nodech.which) {
                    case 0:
                        delimitedBy = new ExpressionString(props);
                        break;
                    case 1:
                        delimitedBy = formatLiteral(literal);
                        break;
                    default:
                        delimitedBy = null;
                }
            }
            for (Enumeration<Node> e2 = ((NodeList) nodeseq.elementAt(0)).elements(); e2.hasMoreElements();) {
                props = null;
                literal = null;
                NodeChoice nodechoice = (NodeChoice) e2.nextElement();
                nodechoice.accept(this);
                switch (nodechoice.which) {
                    case 0:
                        single.append(TranslationTable.getInstance().
                                convertType(new ExpressionString(props),Constants.STRING,props.getIdentifierType()));
                        break;
                    case 1:
                        single.append(TranslationTable.getInstance().convertType(
                                formatLiteral(literal),
                                Constants.STRING, expressionType));
                }

                if (delimitedBy != null) {
                    cLine.append("__delimitedBy(").append(single).append(',').append(delimitedBy.toString()).append(')');
                } else {
                    cLine.append(single);
                }
                if (e2.hasMoreElements()) {
                    cLine.append('+');
                }
                single.setLength(0);
            }
            if (e.hasMoreElements()) {
                cLine.append('+');
            }
        }
        if (cLine.length() > 0) {
            expression.push(cLine.toString());
            expressionType = Constants.STRING;
            ClassFile.println(TranslationTable.getInstance().getAssignString(saveProps, null, false));
        }
    }
    private SymbolProperties inspectSourceSym = null;
    private SymbolProperties inspectCounterSym = null;
    //These two lists should match

    @Override
    public void visit(InspectStatement n) {
        n.identifier.accept(this);
        inspectSourceSym = props;
        super.visit(n);
    }

    /*
     *
    <REPLACING>
    (
    <CHARACTERS> <BY>
    ( Identifier() | Literal() ) ( BeforeAfterPhrase() )*
    | ( <ALL> | <LEADING> | <FIRST> )
    ( ( Identifier() | Literal() )
    <BY> ( Identifier() | Literal() )
    ( BeforeAfterPhrase() )*
    )+
    )+
     */

    /*
    <TALLYING>
    ( Identifier() <FOR> ( <CHARACTERS> ( BeforeAfterPhrase() )*  |
    ( <ALL> | <LEADING> )
    (
    ( Identifier() | Literal() ) ( BeforeAfterPhrase() )*
    )+
    )+
    )+
     */
    @Override
    public void visit(TallyingPhrase n) {
        aLine.push(new StringBuilder());
        for (Enumeration<?> e = n.nodeList.elements(); e.hasMoreElements();) {
            aLine.peek().append("new CobolString(").append(new ExpressionString(inspectSourceSym).toString() + ")");
            NodeSequence seq = (NodeSequence) e.nextElement();
            seq.elementAt(0).accept(this);
            inspectCounterSym = props;
            NodeList nodelist = (NodeList) seq.elementAt(2);
            //String tempLine=aLine;
            for (Enumeration<?> e2 = nodelist.elements(); e2.hasMoreElements();) {
                NodeChoice nodechoice = (NodeChoice) e2.nextElement();
                if (nodechoice.which == 0) {
                    nodechoice.choice.accept(this);
                    aLine.peek().append(".tally()");
                } else {
                    String thisChar = "";
                    NodeSequence seq2 = (NodeSequence) nodechoice.choice;
                    NodeChoice allLeading = (NodeChoice) seq2.elementAt(0);
                    switch (allLeading.which) {
                        case 0:
                            aLine.peek().append(".all()");
                            break;
                        case 1:
                            aLine.peek().append(".leading()");
                            break;
                        default:
                            continue;
                    }
                    NodeList list = (NodeList) seq2.elementAt(1);
                    for (Enumeration<?> e3 = list.elements(); e3.hasMoreElements();) {
                        NodeSequence seq3 = (NodeSequence) e3.nextElement();
                        seq3.elementAt(1).accept(this);
                        thisChar = doInspectPhrase1(seq3.elementAt(0));
                        aLine.peek().append(".tally(").append(thisChar).append(")");
                    }
                }
            }
            aLine.peek().append(".tallyCount()");
            aLine.peek().append("+").append(new ExpressionString(inspectCounterSym).toString());
            line = NameUtil.getJavaName(inspectCounterSym, true).replace("%0", aLine.peek().toString()) + ";";
            ClassFile.println(line);
            aLine.peek().setLength(1);
            aLine.peek().setCharAt(0, '\t');
        }
        aLine.pop();
        n.nodeOptional.accept(this);
    }

    @Override
    public void visit(ReplacingPhrase n) {
        String byChar, thisChar;
        aLine.push(new StringBuilder());
        aLine.peek().append("new CobolString(").append(new ExpressionString(inspectSourceSym).toString()).append(")");
        for (Enumeration<Node> e = n.nodeList.elements(); e.hasMoreElements();) {
            NodeChoice nodechoice = (NodeChoice) e.nextElement();
            NodeSequence seq = (NodeSequence) nodechoice.choice;
            if (nodechoice.which == 0) {
                byChar = doInspectPhrase1(seq.elementAt(2));
                nodechoice.choice.accept(this);
                aLine.peek().append(".replace(").append(byChar).append(")");
            } else {
                NodeSequence seq2 = (NodeSequence) nodechoice.choice;
                NodeChoice allLeading = (NodeChoice) seq2.elementAt(0);
                switch (allLeading.which) {
                    case 0:
                        aLine.peek().append(".all()");
                        break;
                    case 1:
                        aLine.peek().append(".leading()");
                        break;
                    case 2:
                        aLine.peek().append(".first()");
                        break;
                    default:
                        continue;
                }
                NodeList list = (NodeList) seq2.elementAt(1);
                for (Enumeration<Node> e2 = list.elements(); e2.hasMoreElements();) {
                    NodeSequence seq3 = (NodeSequence) e2.nextElement();
                    seq3.elementAt(3).accept(this);
                    thisChar = doInspectPhrase1(seq3.elementAt(0));
                    byChar = doInspectPhrase1(seq3.elementAt(2));
                    aLine.peek().append(".replace(").append(thisChar).append(',').append(byChar).append(")");
                }
            }
        }
        expression.push(aLine.peek().append(".toString()").toString());
        expressionType = Constants.STRING;
        line = TranslationTable.getInstance().getAssignString(inspectSourceSym, null, false, true);
        ClassFile.println(line);
        aLine.pop();
    }

    @Override
    public void visit(ConvertingPhrase n) {
        String from = doInspectPhrase1(n.nodeChoice);
        String to = doInspectPhrase1(n.nodeChoice1);
        aLine.push(new StringBuilder());
        aLine.peek().append("new CobolString(").append(new ExpressionString(inspectSourceSym)).append(")");
        n.nodeListOptional.accept(this);
        aLine.peek().append(".convert(").append(from).append(',').append(to).append(')').append(".toString()");
        line = NameUtil.getJavaName(inspectSourceSym, true).replace("%0", aLine.peek().toString()) + ";";
        ClassFile.println(line);
        aLine.pop();
    }

    //private String inspectLine=null;
    private String doInspectPhrase1(Node n) {
        props = null;
        literal = null;
        n.accept(this);
        if (props != null) {
            return TranslationTable.getInstance().convertType(
                    new ExpressionString(props).toString(),
                    Constants.STRING,
                    props.getIdentifierType());
        } else if (literal != null) {
            literal = formatLiteral(literal);
            return TranslationTable.getInstance().convertType(
                    literal.toString(),
                    Constants.STRING,
                    expressionType);
        }
        return "";
    }

    @Override
    public void visit(BeforeAfterPhrase n) {
        if (aLine == null) {
            return;
        }
        props = null;
        literal = null;
        n.nodeChoice1.accept(this);
        if (n.nodeChoice.which == 0) {
            aLine.peek().append(".before(");
        } else {
            aLine.peek().append(".after(");
        }
        if (props != null) {
            aLine.peek().append(new ExpressionString(props).toString());
        } else {
            if (literal != null) {
                aLine.peek().append(formatLiteral(literal).toString());
            }
        }
        aLine.peek().append(")");
    }

    @Override
    public void visit(SetStatement n) {
        saveProps = null;
        props = null;
        literal = null;
        for (Enumeration<Node> e0 = n.nodeList.elements(); e0.hasMoreElements();) {
            NodeSequence nodeSequence  = (NodeSequence) e0.nextElement();
            nodeSequence.elementAt(1).accept(this);
            saveProps = props;
            for (Enumeration<Node> e = ((NodeList)nodeSequence.elementAt(0))
                    .elements(); e.hasMoreElements();) {
                e.nextElement().accept(this);
                if (props == null) {
                    continue;
                }
               
                switch (((NodeChoice)nodeSequence.elementAt(1)).which) {
                    case 0:
                        NodeChoice nodech = (NodeChoice) ((NodeSequence) ((NodeChoice)nodeSequence.elementAt(1)).choice).elementAt(1);
                        switch (nodech.which) {
                            case 0://Identifier
                                if (saveProps == null) {
                                    continue;
                                }
                                context.getCobol2Java().expression.push(new ExpressionString(saveProps));
                                expressionType = saveProps.getIdentifierType();
                                ClassFile.println(TranslationTable.getInstance().getAssignString(props, null, false,true,true));
                                continue;
                            case 1://True
                                if (props.getLevelNumber() == 88) {
                                    if (props.getValues().get(0).value1 != null) {
                                        expression.push(context.getCobol2Java().formatLiteral(props.getValues().get(0).value1));
                                        ClassFile.println(TranslationTable.getInstance().getAssignString(props.getParent(), null,
                                                props.getParent().getIdentifierType() == Constants.STRING || props.getIdentifierType() == Constants.GROUP,true,true));
                                    }
                                }
                                continue;
                            case 2://False - Not given Enterprise Cobol
                            case 3://On
                            case 4://Of
                            case 5://Literal
                                if (literal == null) {
                                    continue;
                                }
                                context.getCobol2Java().expression.push(context.getCobol2Java().
                                        formatLiteral(literal));
                                ClassFile.println(TranslationTable.getInstance().getAssignString(props, null, false));
                                continue;
                        }
                    case 1:
                        NodeSequence nodeseq = (NodeSequence) ((NodeChoice)nodeSequence.elementAt(1)).choice;
                        String op = null;
                        switch (((NodeChoice) nodeseq.elementAt(0)).which) {
                            case 0:
                                op = "+";
                                break;
                            case 1:
                                op = "-";
                        }
                        if (op == null) {
                            return;
                        }
                        expression.push(new ExpressionString(props));
                        switch (((NodeChoice) nodeseq.elementAt(2)).which) {
                            case 0:
                                if (saveProps == null) {
                                    continue;
                                }
                                doIdOp(saveProps,op);
                                //context.getCobol2Java().expressionType = saveProps.getIdentifierType();
                                ClassFile.println(TranslationTable.getInstance().getAssignString(props, null, false));
                                continue;
                            case 1:
                                if (literal == null) {
                                    continue;
                                }
                                doLiteralOp(formatLiteral(literal).toString(),op);
                               // context.getCobol2Java().expression.push(context.getCobol2Java().
                                        //.toString() + op + new ExpressionString(props).toString());
                                ClassFile.println(TranslationTable.getInstance().getAssignString(props, null, false));
                                continue;
                        }
                }
            }
        }
        //super.visit(n);
    }
    private TreeToCommentFormatter dumper = new TreeToCommentFormatter(false);
    public boolean isLastGotoStatement = false;

    @Override
    public void visit(StatementList n) {
        isLastGotoStatement = false;
        super.visit(n);
    }

    @Override
    public void visit(GobackStatement n) {
        if (returnLine != null) {
            ClassFile.println("return " + returnLine);
        }
    }

    @Override
    public void visit(Statement n) {
        initExpressionType();
        if (n.deadStatement) {
            return;
        }
        isLastGotoStatement = n.nodeChoice.choice instanceof GotoStatement
                || n.nodeChoice.choice instanceof GobackStatement;
        isLastExitStatement = false;
        if (RESConfig.getInstance().isPrintCobolStatementsAsComments()
                || RESConfig.getInstance().isRetainCobolComments()) {
            dumper.startAtNextToken();
            dumper.visit(n);
            if (RESConfig.getInstance().isPrintCobolStatementsAsComments()) {
                ClassFile.println("");
            }
            if (execSql2Java != null) {
                execSql2Java.setStatementNode(n);
            }
        }
        super.visit(n);
    }
    private ExecSql2Java execSql2Java = new ExecSql2Java(this);

    @Override
    public void visit(ExecSqlStatement n) {
        execSql2Java.visit(n);
        //super.visit(n);
    }

    @Override
    public void visit(OpenStatement n) {
        doTry();
        boolean inp = false, op = false, iop = false, ext = false;
        for (Enumeration<Node> e = n.nodeList.elements(); e.hasMoreElements();) {
            NodeChoice nodech = (NodeChoice) e.nextElement();
            NodeSequence nodeseq = (NodeSequence) nodech.choice;
            switch (((NodeToken) nodeseq.elementAt(0)).kind) {
                case CobolParserConstants.INPUT:
                    inp = true;
                    break;
                case CobolParserConstants.OUTPUT:
                    op = true;
                    break;
                case CobolParserConstants.INPUT_OUTPUT:
                case CobolParserConstants.I_O:
                    iop = true;
                    break;
                case CobolParserConstants.EXTEND:
                    ext = true;
                    break;
                default:
            }
            for (Enumeration<Node> e2 = ((NodeList) nodeseq.elementAt(1)).elements();
                    e2.hasMoreElements();) {
                e2.nextElement().accept(this);
                if (props == null) {
                    continue;
                }
                if (inp) {
                    ClassFile.println(props.getJavaName1() + ".openInput();");
                }
                if (op) {
                    ClassFile.println(props.getJavaName1() + ".openOutput();");
                }
                if (iop) {
                    ClassFile.println(props.getJavaName1() + ".openIO();");
                }
                if (ext) {
                    ClassFile.println(props.getJavaName1() + ".openExtend();");
                }

            }
            inp = op = iop = ext = false;
        }
        printFileStatus(props);
        ClassFile.backTab();
        ClassFile.println("} catch(java.io.IOException ioe) {");
        printFileStatus(props);
        exceptionPrintStackTrace("ioe");
        String declConditions = props.getDataName();
        if (props.isFileOpenedInput()) {
            declConditions += ",1";
        }
        if (props.isFileOpenedOutput()) {
            declConditions += ",2";
        }
        if (props.isFileOpenedIO()) {
            declConditions += ",3";
        }
        if (props.isFileOpenedExtend()) {
            declConditions += ",4";
        }
        doDeclaratives(declConditions.split(","));
        ClassFile.println("}");
    }

    private void doDeclaratives(String... declConditions) {
        if (!isDeclarative && useConditions != null) {
            for (UseCondition u : useConditions) {
                for (String s : declConditions) {
                    if ((u.openCondition != null && u.openCondition.equalsIgnoreCase(s) && u.isError)) {
                        ClassFile.println("doCobolPerform(" + u.section.getJavaName1() + ",null);");
                        return;
                    }
                }
            }
        }
    }

    @Override
    public void visit(ReadStatement n) {
        n.fileName.accept(this);
        if (props == null) {
            return;
        }
        SymbolProperties fileProps = props;

        if (fileProps.getChildren() == null || fileProps.getChildren().size() <= 0) {
            return;
        }
        SymbolProperties record = null;
        if (fileProps.isIndexedFile()) {
            record = findIndexedRecord(fileProps);
        } else {
            record = fileProps.getChildren().get(0);
        }
        if (record == null || record.getLevelNumber() != 1) {
            return;
        }
        doTry();
        String assign = fileProps.getJavaName1() + ".read";
        if (fileProps.isIndexedFile()) {
            indFile:
            {
                if (n.nodeOptional.present() && fileProps.getOtherData1() == Constants.DYNAMIC_ACCESS) {
                    assign += "Next";
                } else {
                    if (n.nodeOptional3.present()) {
                        ((NodeSequence) n.nodeOptional3.node).elementAt(2).accept(this);
                        if (props != null && record != null) {
                            SymbolProperties pk = (SymbolProperties) fileProps.getOtherData2();
                            String keyStr = record.getJavaName1() + ".primaryKey()";
                            if (pk == null || !(pk.getOffset() == props.getOffset())) {
                                keyStr = TranslationTable.getInstance().convertType(
                                        new ExpressionString(props), Constants.STRING, props.getIdentifierType()).toString();
                                keyStr += "," + String.valueOf(props.getOffset()) + "," + String.valueOf(props.getLength());
                            } else if (pk.getLength() != props.getLength()) {
                                keyStr += "," + String.valueOf(props.getLength());
                            }
                            assign += "(" + keyStr + ")";
                            break indFile;
                        }
                    }
                }
                assign += "()";
            }
        } else {
            if (record.is01Group()) {
                assign = assign + "(" + record.getJavaName1();
            } else {
                assign = assign + "(super.getProgram().get()," + record.getOffset() + ',' + record.getLength();
            }
            if (fileProps.getOtherData2() != null) {
                assign += ","
                        + NameUtil.getJavaName((SymbolProperties) fileProps.getOtherData2(), false);
            }
            assign += ")";
        }
        ClassFile.println(assign + ";");
        if (n.nodeOptional2.present()) {
            props = null;
            n.nodeOptional2.accept(this);
            if (props != null) {
                ClassFile.println(TranslationTable.getInstance().getAssignString(props, record, false));
            }
        }
        printFileStatus(fileProps);
        n.nodeOptional5.accept(this);
        n.nodeOptional7.accept(this);
        ClassFile.backTab();
        String declConditions = fileProps.getDataName();
        if (fileProps.isFileOpenedInput()) {
            declConditions += ",1";
        }
        if (fileProps.isFileOpenedIO()) {
            declConditions += ",3";
        }
        ClassFile.println("} catch(java.io.EOFException ioe) {");
        ClassFile.tab();
        exceptionPrintStackTrace("ioe");
        printFileStatus(fileProps);
        if (!n.nodeOptional6.present()) {
            doDeclaratives(declConditions.split(","));
        } else {
            n.nodeOptional6.accept(this);
        }
        ClassFile.backTab();
        ClassFile.println("} catch(InvalidKeyException ike) {");
        ClassFile.tab();
        exceptionPrintStackTrace("ike");
        printFileStatus(fileProps);
        if (n.nodeOptional4.present()) {
            n.nodeOptional4.accept(this);
        } else {
            doDeclaratives(declConditions.split(","));
        }
        ClassFile.backTab();
        ClassFile.println("} catch(java.io.IOException ioe) {");
        ClassFile.tab();
        exceptionPrintStackTrace("ioe");
        doDeclaratives(declConditions.split(","));
        ClassFile.backTab();
        ClassFile.println("}");
    }

    private SymbolProperties findIndexedRecord(SymbolProperties fileProps) {
        SymbolProperties record = null;
        for (SymbolProperties k : fileProps.getChildren()) {
            if (!k.isIndexedFileRecord()) {
                continue;
            }
            record = k;
            break;
        }
        return record;
    }
    private SymbolProperties saveProps = null;

    @Override
    public void visit(WriteStatement n) {
        n.recordName.accept(this);
        if (props == null) {
            return;
        }
        SymbolProperties fileProps = props.getParent();
        saveProps = props;
        if (!fileProps.isFile()) {
            return;
        }
        doTry();
        if (n.nodeOptional.present()) {
            saveProps = props;
            props = null;
            literal = null;
            n.nodeOptional.accept(this);
            if (props != null) {
                ClassFile.println(TranslationTable.getInstance().getAssignString(saveProps, props, false));
            } else {
                literal = formatLiteral(literal);
                expression.push(literal.toString());
                ClassFile.println(TranslationTable.getInstance().getAssignString(saveProps, null, false));
            }
        }
        props = saveProps;
        if (fileProps.isIndexedFile()) {
            ClassFile.println(fileProps.getJavaName1() + ".write();");
        } else {
            if (n.nodeOptional1.present() && ((AdvancingPhrase) n.nodeOptional1.node).nodeChoice.which == 1) {
                n.nodeOptional1.accept(this);
            }
            if (n.nodeOptional.present()) {
                props = null;
                literal = null;
                n.nodeOptional.accept(this);
            }
            if (props != null || literal != null) {
                String assign;
                if (props == null) {
                    assign = formatLiteral(literal).toString();
                    assign = TranslationTable.getInstance().convertType(assign, Constants.GROUP, expressionType);
                } else {
                    assign = new ExpressionString(props).toString();
                    assign = TranslationTable.getInstance().convertType(assign, Constants.GROUP, props.getIdentifierType());
                }
                ClassFile.println(fileProps.getJavaName1() + ".write(" + assign + ");");
            }
            if (n.nodeOptional1.present() && ((AdvancingPhrase) n.nodeOptional1.node).nodeChoice.which == 0) {
                n.nodeOptional1.accept(this);
            }
        }
        printFileStatus(fileProps);
        n.nodeOptional5.accept(this);
        String declConditions = fileProps.getDataName();
        if (fileProps.isFileOpenedOutput()) {
            declConditions += ",2";
        }
        if (fileProps.isFileOpenedIO()) {
            declConditions += ",3";
        }
        if (fileProps.isFileOpenedExtend()) {
            declConditions += ",4";
        }

        ClassFile.backTab();
        ClassFile.println("} catch(InvalidKeyException ike) {");
        ClassFile.tab();
        exceptionPrintStackTrace("ike");
        printFileStatus(fileProps);
        if (!n.nodeOptional4.present()) {
            doDeclaratives(declConditions.split(","));
        } else {
            n.nodeOptional4.accept(this);
        }
        ClassFile.backTab();
        ClassFile.println("} catch(java.io.IOException ioe) {");
        ClassFile.tab();
        exceptionPrintStackTrace("ioe");
        printFileStatus(fileProps);
        doDeclaratives(declConditions.split(","));
        ClassFile.backTab();
        ClassFile.println("}");
    }

    @Override
    public void visit(AdvancingPhrase n) {
        if (saveProps == null) {
            return;
        }
        switch (n.nodeChoice1.which) {
            case 0:
                ClassFile.println(saveProps.getParent().getJavaName1() + ".advancePage();");
                break;
            case 1:
                props = null;
                n.nodeChoice1.choice.accept(this);
                if (props != null) {
                    expression.push(new ExpressionString(props).toString());
                    expressionType = props.getIdentifierType();
                } else {
                    expression.push(formatLiteral(literal).toString());
                }
                ClassFile.println(saveProps.getParent().getJavaName1() + ".advanceLines("
                        + TranslationTable.getInstance().convertType(expression.pop3(), Constants.INTEGER, expressionType) + ");");
                props = saveProps;
                break;
            case 2:
                n.nodeChoice1.accept(this);
                literal = formatLiteral(literal);
                ClassFile.println(saveProps.getParent().getJavaName1() + ".advanceLines(" + literal.toString()
                        + ");");
                break;
        }
    }

    @Override
    public void visit(RewriteStatement n) {

        n.recordName.accept(this);
        if (props == null) {
            return;
        }
        SymbolProperties fileProps = props.getParent();
        props = fileProps.getChildren().get(0);
        if (!fileProps.isFile()) {
            return;
        }
        if (n.nodeOptional.present()) {
            props = null;
            literal = null;
            n.nodeOptional.accept(this);
            if (props == null && literal == null) {
                return;
            }
        }
        doTry();
        String assign;
        assign = "";
        if (fileProps.isIndexedFile()) {
            assign = "";
            if (!fileProps.getChildren().get(0).getDataName().equalsIgnoreCase(props.getDataName())) {
                expression.push(new ExpressionString(props));
                ClassFile.println(TranslationTable.getInstance().getAssignString(fileProps.getChildren().get(0), null, false));
            }
        } else if (props == null) {
            assign = formatLiteral(literal).toString();
            assign = TranslationTable.getInstance().convertType(assign, Constants.GROUP, expressionType);
        } else {
            assign = new ExpressionString(props).toString();
            assign = TranslationTable.getInstance().convertType(assign, Constants.GROUP, props.getIdentifierType());
        }
        ClassFile.println(fileProps.getJavaName1() + ".rewrite(" + assign + ");");
        printFileStatus(fileProps);
        n.nodeOptional2.accept(this);
        ClassFile.backTab();
        String declConditions = fileProps.getDataName();
        if (fileProps.isFileOpenedOutput()) {
            declConditions += ",2";
        }
        if (fileProps.isFileOpenedIO()) {
            declConditions += ",3";
        }
        if (props.isFileOpenedExtend()) {
            declConditions += ",4";
        }
        ClassFile.println("} catch(InvalidKeyException ike) {");
        ClassFile.tab();
        exceptionPrintStackTrace("ike");
        printFileStatus(fileProps);
        if (!n.nodeOptional1.present()) {
            doDeclaratives(declConditions.split(","));
        } else {
            n.nodeOptional1.accept(this);
        }
        ClassFile.backTab();
        ClassFile.println("} catch(java.io.IOException ioe) {");
        ClassFile.tab();
        exceptionPrintStackTrace("ioe");
        printFileStatus(fileProps);
        doDeclaratives(declConditions.split(","));
        ClassFile.backTab();
        ClassFile.println("}");
    }

    @Override
    public void visit(StartStatement n) {
        doTry();
        n.fileName.accept(this);
        if (props == null) {
            return;
        }
        SymbolProperties fileProps = props;
        props = null;
        if (n.nodeOptional.present()) {
            SymbolProperties record = findIndexedRecord(fileProps);
            ((NodeSequence) n.nodeOptional.node).elementAt(3).accept(this);
            if (props != null && record != null) {
                SymbolProperties pk = (SymbolProperties) fileProps.getOtherData2();
                String keyStr = record.getJavaName1() + ".primaryKey()";
                if (pk == null || !(pk.getOffset() == props.getOffset())) {
                    keyStr = TranslationTable.getInstance().convertType(
                            new ExpressionString(props), Constants.STRING, props.getIdentifierType()).toString();
                    keyStr += "," + String.valueOf(props.getOffset()) + "," + String.valueOf(props.getLength());
                } else if (pk.getLength() != props.getLength()) {
                    keyStr += "," + String.valueOf(props.getLength());
                }
                NodeChoice nodechoice = (NodeChoice) ((NodeSequence) n.nodeOptional.node).elementAt(2);
                switch (nodechoice.which) {
                    case 0:
                    case 1:
                        ClassFile.println(fileProps.getJavaName1() + ".startEq(" + keyStr + ");");
                        break;
                    case 2://ge
                    case 7://>=
                    case 5://Not LESS
                    case 6://Not <
                        ClassFile.println(fileProps.getJavaName1() + ".startGe(" + keyStr + ");");
                        break;
                    case 3://gt
                    case 4://>
                        ClassFile.println(fileProps.getJavaName1() + ".startGt(" + keyStr + ");");
                        break;
                    default:
                        ClassFile.println(fileProps.getJavaName1() + ".start();");
                }
            } else {
                ClassFile.println(fileProps.getJavaName1() + ".start();");
            }
        } else {
            ClassFile.println(fileProps.getJavaName1() + ".start();");
        }

        printFileStatus(fileProps);
        String declConditions = fileProps.getDataName();
        if (fileProps.isFileOpenedOutput()) {
            declConditions += ",2";
        }
        if (fileProps.isFileOpenedIO()) {
            declConditions += ",3";
        }
        if (fileProps.isFileOpenedExtend()) {
            declConditions += ",4";
        }
        ClassFile.backTab();
        ClassFile.println("} catch(InvalidKeyException e) {");
        ClassFile.tab();
        exceptionPrintStackTrace("e");
        printFileStatus(fileProps);
        if (n.nodeOptional1.present()) {
            n.nodeOptional1.accept(this);
        } else {
            doDeclaratives(declConditions.split(","));
        }
        ClassFile.backTab();
        ClassFile.println("} catch(java.io.IOException e) {");
        ClassFile.tab();
        exceptionPrintStackTrace("e");
        printFileStatus(fileProps);
        ClassFile.backTab();
        ClassFile.println("}");

    }

    @Override
    public void visit(DeleteStatement n) {
        doTry();
        n.fileName.accept(this);
        if (props == null) {
            return;
        }
        String arg = "";
        if (!props.isIndexedFile()) {
            arg = NameUtil.getJavaName((SymbolProperties) props.getOtherData2(), false);
        }
        ClassFile.println(props.getJavaName1() + ".delete(" + arg + ");");
        n.nodeOptional2.accept(this);
        printFileStatus(props);
        String declConditions = props.getDataName();
        if (props.isFileOpenedOutput()) {
            declConditions += ",2";
        }
        if (props.isFileOpenedIO()) {
            declConditions += ",3";
        }
        if (props.isFileOpenedExtend()) {
            declConditions += ",4";
        }
        ClassFile.backTab();
        ClassFile.println("} catch(InvalidKeyException e) {");
        ClassFile.tab();
        exceptionPrintStackTrace("e");
        printFileStatus(props);
        if (n.nodeOptional1.present()) {
            n.nodeOptional1.accept(this);
        } else {
            doDeclaratives(declConditions.split(","));
        }
        ClassFile.backTab();
        ClassFile.println("} catch(java.io.IOException e) {");
        ClassFile.tab();
        exceptionPrintStackTrace("e");
        printFileStatus(props);
        ClassFile.backTab();
        ClassFile.println("}");
    }

    private void printFileStatus(SymbolProperties fileProps) {

        if (fileProps.getFileStatus() == null || fileProps.getFileStatus().size() <= 0) {
            return;
        }
        SymbolProperties fs = null;
        doit:
        {
            for (SymbolProperties s : fileProps.getFileStatus()) {
                if (findProgram(s).equals(SymbolTable.getScope().getCurrentProgram())) {
                    fs = s;
                    break doit;
                }
            }
            return;
        }
        fs.setIdentifierType(fs.getJavaType().getType());
        expression.push(new ExpressionString(fileProps.getJavaName1() + ".getFileStatus()", expressionType = Constants.STRING));
        doMove(getExpression(), fs);
    }

    @Override
    public void visit(CloseStatement n) {
        boolean doExceptions = false;

        for (Enumeration<Node> e = n.nodeList.elements(); e.hasMoreElements();) {
            ((NodeSequence) e.nextElement()).elementAt(0).accept(this);
            if (props != null) {
                if (!doExceptions) {
                    doTry();
                }
                doExceptions = true;
                ClassFile.println(props.getJavaName1() + ".close();");
                printFileStatus(props);
            }
        }
        if (doExceptions) {
            ClassFile.backTab();
            ClassFile.println("} catch(java.io.IOException ioe) {");
            ClassFile.tab();
            exceptionPrintStackTrace("ioe");
            printFileStatus(props);
            String declConditions = props.getDataName();
            if (props.isFileOpenedInput()) {
                declConditions += ",1";
            }
            if (props.isFileOpenedOutput()) {
                declConditions += ",2";
            }
            if (props.isFileOpenedIO()) {
                declConditions += ",3";
            }
            if (props.isFileOpenedExtend()) {
                declConditions += ",4";
            }
            doDeclaratives(declConditions.split(","));
            ClassFile.backTab();
            ClassFile.println("}");
        }
    }

    @Override
    public void visit(FileName n) {
        props = SymbolTable.getScope().lookup(n.cobolWord.nodeToken.tokenImage, SymbolConstants.FILE);
    }

    public void reportError(Node n, String msg) {
        System.out.println("@CobolSourceFile(\"" + ((RESNode) n).sourceFile + "\"," + ((RESNode) n).line + "):" + msg);
        System.exit(0);
    }

    public void reportError(Node n, String msg, boolean exit) {
        System.out.println("@CobolSourceFile(\"" + ((RESNode) n).sourceFile + "\"," + ((RESNode) n).line + "):" + msg);
        if (exit) {
            System.exit(0);
        }
    }

    public SymbolProperties getCurrentProgram() {
        return SymbolTable.getScope().getCurrentProgram();
    }

    public String getAssignString(TranslationTable translationTable,
            SymbolProperties lhs, SymbolProperties rhs) {
        return translationTable.getAssignString(lhs, rhs, false);
    }

    public void doTail() {
        initializeSymbols();
        ClassFile.backTab();
    }

    @Override
    public void visit(CallStatement n) {
        doTry();
        if (idOrLiteralList != null) {
            idOrLiteralList.clear();
        } else {
            idOrLiteralList = new ArrayList<ExpressionString>();
        }
        StringBuilder line = new StringBuilder("doCobolCall(");
        bLine.push(new StringBuilder());
        cLine.push(new StringBuilder());
        callParmIdx = 0;
        n.nodeChoice.choice.accept(this);
        switch (n.nodeChoice.which) {
            case 0:
                expression.push(new ExpressionString(props).toString());
                expressionType = props.getIdentifierType();
                break;
            case 1:
                expression.push(formatLiteral(literal).toString());
                break;
        }
        line.append(TranslationTable.getInstance().
                convertType(expression.pop3(), Constants.STRING, expressionType));
        n.nodeOptional.accept(this);
        for (Iterator<ExpressionString> e = idOrLiteralList.iterator(); e.hasNext();) {
            line.append(',');
            line.append(e.next().toString());
        }
        line.append(");");
        ClassFile.println(line.toString());
        bLine.peek().append(cLine.peek().toString());
        ClassFile.println(bLine.peek().toString());
        n.nodeOptional3.accept(this);
        ClassFile.backTab();
        bLine.pop();
        cLine.pop();
        ClassFile.println("} catch(Exception e) {");
        ClassFile.tab();
        exceptionPrintStackTrace("e");
        n.nodeOptional1.accept(this);
        n.nodeOptional2.accept(this);
        ClassFile.backTab();
        ClassFile.println("}");
    }

    @Override
    public void visit(CancelStatement n) {
        aLine.push(new StringBuilder());
        for (Enumeration<Node> e = n.nodeList.elements(); e.hasMoreElements();) {
            String exprString = null;
            props = null;
            literal = null;
            e.nextElement().accept(this);
            if (props != null) {
                exprString = TranslationTable.getInstance().convertType(
                        NameUtil.getJavaName(props, false), Constants.STRING, props.getIdentifierType());
            } else {
                literal = formatLiteral(literal);
                exprString = TranslationTable.getInstance().convertType(
                        literal.toString(), Constants.STRING, expressionType);
            }
            if (aLine.peek().length() > 0) {
                aLine.peek().append(',');
            }
            aLine.peek().append(exprString);
        }
        if (aLine.peek().length() > 0) {
            ClassFile.println("doCobolCancel(" + aLine.peek().toString() + ");");
        }
        aLine.pop();
    }

    @Override
    public void visit(CallByContentArgs n) {
        switch (n.nodeChoice.which) {
            case 0:
                n.nodeChoice.choice.accept(this);
                if (props == null) {
                    idOrLiteralList.add(new ExpressionString("null"));
                } else if (((NodeOptional) ((NodeSequence) n.nodeChoice.choice).elementAt(0)).present()) {
                    idOrLiteralList.add(new ExpressionString(String.valueOf(props.getLength())));
                } else {
                    idOrLiteralList.add(new ExpressionString(new ExpressionString(props).toString()));
                }
                break;
            case 1:
                idOrLiteralList.add(new ExpressionString("null"));
                break;
            case 2:
                n.nodeChoice.choice.accept(this);
                idOrLiteralList.add(new ExpressionString(formatLiteral(literal).toString()));
        }
        callParmIdx++;
    }

    @Override
    public void visit(CallByReferenceArgs n) {

        switch (n.nodeChoice.which) {
            case 0:
            case 2:
                n.nodeChoice.choice.accept(this);
                if (props == null) {
                    idOrLiteralList.add(new ExpressionString("null"));
                } else {
                    idOrLiteralList.add(new ExpressionString(props));
                }
                expression.push("__get" + SymbolConstants.getSQL(props.getIdentifierType()) + "Result("
                        + String.valueOf(callParmIdx) + ")");
                expressionType = props.getIdentifierType();
                if (props.isAChildHasDependingOn() || (props.isOccurs() && props.getDependingOnOccurs() != null)) {
                    cLine.peek().append(TranslationTable.getInstance().getAssignString(props, null, false, true, true));
                } else {
                    bLine.peek().append(TranslationTable.getInstance().getAssignString(props, null, false, true, true));
                }
                break;
            case 1:
                idOrLiteralList.add(new ExpressionString("null"));
                break;
        }
        callParmIdx++;
    }
    private HashMap<Integer, ExpressionString> replacingValues;

    @Override
    public void visit(InitializeStatement n) {

        replacingValues = new HashMap<Integer, ExpressionString>();
        if (n.nodeOptional.present()) {
            NodeSequence nodeseq = (NodeSequence) n.nodeOptional.node;
            for (Enumeration<Node> e = ((NodeList) nodeseq.elementAt(1)).elements(); e.hasMoreElements();) {
                props = null;
                literal = null;
                ExpressionString replacingVal = null;
                NodeSequence nodeseq2 = (NodeSequence) e.nextElement();
                nodeseq2.elementAt(3).accept(this);
                if (props != null) {
                    replacingVal = new ExpressionString(props);
                } else if (literal != null) {
                    replacingVal = formatLiteral(literal);
                }
                if (replacingVal == null) {
                    continue;
                }
                replacingValues.put(((NodeChoice) nodeseq2.elementAt(0)).which, replacingVal);
            }
        }
        aLine.push(new StringBuilder());
        for (Enumeration<Node> e = n.nodeList.elements(); e.hasMoreElements();) {
            e.nextElement().accept(this);
            if (props == null) {
                continue;
            }
            if (props.isOccurs() && props.getIndexesWorkSpace() == null) {
                reportError(n, "Warning: A table may not be directly specified with INITIALIZE verb without subscript.", false);
            }
            int i = 0;
            if (props.isOccurs() || props.isAParentInOccurs() && props.getIndexesWorkSpace() == null) {
                for (SymbolProperties occursPar : props.getOccursParents()) {
                    i++;
                    if (aLine.peek().length() > 0) {
                        aLine.peek().append(',');
                    }
                    String idx = "idx" + String.valueOf(i + 1);
                    aLine.peek().append(idx);
                    ClassFile.println("for(int " + idx + "=1;" + idx + "<=" + String.valueOf(occursPar.getMaxOccurs()) + ";++" + idx + ") {");
                    ClassFile.tab();
                }
            }
            doInitializeVerb(props, i);
            if (props.isOccurs() || props.isAParentInOccurs()) {
                for (i = 0; i < props.getOccursParents().size(); ++i) {
                    ClassFile.backTab();
                    ClassFile.println("}");
                }
            }
        }
        aLine.pop();

    }

    private void doInitializeVerb(ArrayList<SymbolProperties> a, int idx) {
        if (a == null || a.size() <= 0) {
            return;
        }
        int saveIdx = idx, i = idx;
        for (SymbolProperties ch : a) {
            if (ch.isOccurs()) {
                for (; i < ch.getOccursParents().size(); ++i) {
                    SymbolProperties occursPar = ch.getOccursParents().get(i);
                    if (aLine.peek().length() > 0) {
                        aLine.peek().append(',');
                    }
                    String ix = "idx" + String.valueOf(i + 1);
                    aLine.peek().append(ix);
                    ClassFile.println("for(int " + ix + "=1;" + ix + "<="
                            + String.valueOf(occursPar.getMaxOccurs()) + ";++" + ix + ") {");
                    ClassFile.tab();

                }
            }
            if (!ch.isElementData()) {
                doInitializeVerb(ch.getChildren(), i);
            } else {
                doInitializeVerb(ch, i);
            }
            if (ch.isOccurs()) {
                for (; i > saveIdx; --i) {
                    if (aLine.peek().length() > 0) {
                        int temp;
                        aLine.peek().delete((((temp = aLine.peek().lastIndexOf(",")) < 0) ? 0 : temp), aLine.peek().length());
                    }
                    ClassFile.backTab();
                    ClassFile.println("}");
                }
            }
        }
    }

    private void doInitializeVerb(SymbolProperties p, int idx) {
        int saveIdx = idx, i = idx;
        if (p.isOccurs()) {
            for (; i < p.getOccursParents().size(); ++i) {
                SymbolProperties occursPar = p.getOccursParents().get(i);
                if (aLine.peek().length() > 0) {
                    aLine.peek().append(',');
                }
                String ix = "idx" + String.valueOf(i + 1);
                aLine.peek().append(ix);
                ClassFile.println("for(int " + ix + "=1;" + ix + "<="
                        + String.valueOf(occursPar.getMaxOccurs()) + ";++" + ix + ") {");
                ClassFile.tab();
            }
        }
        if (!p.isElementData()) {
            doInitializeVerb(p.getChildren(), i);
        } else {
            p.setIndexesWorkSpace(new ArrayList<String>());
            p.getIndexesWorkSpace().add(aLine.peek().toString());
            switch (p.getIdentifierType()) {
                case Constants.CHAR:
                    expression.push(new ExpressionString("\' \'", expressionType = Constants.CHAR));
                    ClassFile.println(TranslationTable.getInstance().getAssignString(p, null, false, true));
                    break;
                case Constants.BYTE:
                case Constants.SHORT:
                case Constants.INTEGER:
                case Constants.LONG:
                    expression.push(new ExpressionString("0", expressionType = Constants.INTEGER));
                    ClassFile.println(TranslationTable.getInstance().getAssignString(p, null, false, true));
                    break;
                case Constants.FLOAT:
                case Constants.DOUBLE:
                    expression.push(new ExpressionString("0.00", expressionType = Constants.FLOAT));
                    ClassFile.println(TranslationTable.getInstance().getAssignString(p, null, false, true));
                    break;
                case Constants.BIGDECIMAL:
                    expression.push(new ExpressionString("BigDecimal.ZERO", expressionType = Constants.BIGDECIMAL));
                    ClassFile.println(TranslationTable.getInstance().getAssignString(p, null, false, true));
                    break;
                case Constants.STRING:
                    expression.push(new ExpressionString("\" \"", expressionType = Constants.STRING));
                    ClassFile.println(TranslationTable.getInstance().getAssignString(p, null, false, true));
                    break;
                case Constants.GROUP:
                    if (p.hasChildren()) {
                        doInitializeVerb(p.getChildren(), idx);
                    }
                    break;
                default:
                    ClassFile.println(NameUtil.getJavaName(p, true).replace("%0", "null") + ';');
                    break;
            }
        }
        if (p.isOccurs()) {
            for (; i > saveIdx; --i) {
                if (aLine.peek().length() > 0) {
                    int temp;
                    aLine.peek().delete((((temp = aLine.lastIndexOf(",")) < 0) ? 0 : temp), aLine.peek().length());
                }
                ClassFile.backTab();
                ClassFile.println("}");
            }
        }
    }

    @Override
    public void visit(EvaluateStatement n) {

        if (evaluateValue != null) {
            evaluateValue.clear();
        }
        n.evaluateValue.accept(this);
        n.nodeListOptional.accept(this);
        for (Enumeration<Node> e = n.nodeList.elements(); e.hasMoreElements();) {//WHEN...ALSO..Statement()+
            NodeSequence node = (NodeSequence) e.nextElement();
            evaluateValueIndex = 0;
            aLine.push(new StringBuilder());
            aLine.peek().append("if(");
            for (Enumeration<Node> e2 = ((NodeList) node.elementAt(0)).elements(); e2.hasMoreElements();) {//WHEN
                NodeSequence nodeseq = (NodeSequence) e2.nextElement();
                nodeseq.elementAt(2).accept(this);
                if (((NodeListOptional) nodeseq.elementAt(3)).present()) {//ALSO LIST
                    for (Enumeration<Node> e3 = ((NodeListOptional) nodeseq.elementAt(3)).elements(); e3.hasMoreElements();) {
                        aLine.peek().append("&&");
                        evaluateValueIndex++;
                        e3.nextElement().accept(this);
                    }
                }
                if (e2.hasMoreElements()) {
                    aLine.peek().append("||");
                }
            }
            aLine.peek().append(") {");
            ClassFile.println(aLine.peek().toString());
            ClassFile.tab();
            node.elementAt(1).accept(this);
            ClassFile.backTab();
            if (e.hasMoreElements()) {
                ClassFile.println("} else ");
            } else {
                ClassFile.println("}");
            }
            aLine.pop();
        }
        if (n.nodeOptional.present()) {
            ClassFile.println("else {");
            ClassFile.tab();
            n.nodeOptional.accept(this);
            ClassFile.backTab();
            ClassFile.println("}");
        }

    }
    private ArrayList<ExpressionString> evaluateValue = null;
    private int evaluateValueIndex = 0;

    @Override
    public void visit(EvaluatePhrase n) {

        switch (n.nodeChoice.which) {
            case 0:
                aLine.peek().append("true");
                return;
            case 3:
                aLine.peek().append("");
                break;
            case 4:
                aLine.peek().append("!");
                break;
            case 2:
                doReverseCondition = false;
                n.nodeChoice.choice.accept(this);
                aLine.peek().append(condition.pop()).append("==");
                break;
            case 1:
                StringBuilder subphrase = new StringBuilder();
                NodeSequence nodeSeq = (NodeSequence) n.nodeChoice.choice;
                ((NodeOptional) (nodeSeq.elementAt(0))).present();//Not
                NodeChoice nodech1 = ((NodeChoice) (nodeSeq.elementAt(1)));
                nodech1.accept(this);
                switch (nodech1.which) {
                    case 0:
                        expressionType = props.getIdentifierType();
                        expression.push(new ExpressionString(props));
                        break;
                    case 1:
                        expression.push(formatLiteral(literal).toString());
                        break;
                    case 2:
                }
                String top = "";
                ExpressionString expr1 = getExpression();
                if (((NodeOptional) nodeSeq.elementAt(2)).present()) {
                    expression.push(appendEvaluateValue());
                    top = TranslationTable.getInstance().doCondition(
                            TranslationTable.LE_CONDITION,
                            expr1, getExpression(), false);
                    ;
                    subphrase.append(top).append("&&");
                    NodeSequence nodeseq2 = (NodeSequence) ((NodeOptional) nodeSeq.elementAt(2)).node;
                    NodeChoice nodech2 = ((NodeChoice) (nodeseq2.elementAt(1)));
                    nodech2.accept(this);
                    switch (nodech2.which) {
                        case 0:
                            expressionType = props.getIdentifierType();
                            expression.push(new ExpressionString(props));
                            break;
                        case 1:
                            expression.push(formatLiteral(literal).toString());
                            break;
                        case 2:
                    }
                    expr1 = getExpression();
                    expression.push(appendEvaluateValue());
                    top = TranslationTable.getInstance().doCondition(
                            TranslationTable.GE_CONDITION,
                            expr1, getExpression(), false);
                    subphrase.append(top);
                } else {
                    if (appendEvaluateValue().equals("true")) {
                        subphrase.append(expr1.toString());
                    } else if (appendEvaluateValue().equals("false")) {
                        subphrase.append('!').append(expr1.toString());
                    } else {
                        expression.push(appendEvaluateValue());
                        top = TranslationTable.getInstance().doCondition(TranslationTable.EQ_CONDITION,
                                expr1, getExpression(), false);
                        subphrase.append(top);
                    }
                }
                if (((NodeOptional) (nodeSeq.elementAt(0))).present()) {
                    aLine.peek().append("!(").append(subphrase).append(')');
                } else {
                    aLine.peek().append(subphrase);
                }
                return;
        }
        aLine.peek().append(appendEvaluateValue());
    }

    private String appendEvaluateValue() {
        if (evaluateValueIndex >= 0 && evaluateValueIndex < evaluateValue.size()) {


            // if (evaluateValue.get(evaluateValueIndex) instanceof ExpressionString) {
            expressionType = evaluateValue.get(evaluateValueIndex).type;
            return evaluateValue.get(evaluateValueIndex).toString();
            // }
            // return (String) evaluateValue.get(evaluateValueIndex);
        }
        return "0";
    }

    @Override
    public void visit(EvaluateValue n) {
        if (evaluateValue == null) {
            evaluateValue = new ArrayList<ExpressionString>();
        }
        n.nodeChoice.accept(this);

        switch (n.nodeChoice.which) {
            case 2:
                literal = new ExpressionString(expression.pop());
                literal.type = expressionType;
                evaluateValue.add(literal);
                break;
            case 0:
                evaluateValue.add(new ExpressionString(props));
                break;
            case 3:
                evaluateValue.add(formatLiteral(literal));
                break;
            case 1:
                evaluateValue.add(new ExpressionString(condition.pop()));
                ;
                break;
            case 4:
                evaluateValue.add(new ExpressionString("true"));
                break;
            case 5:
                evaluateValue.add(new ExpressionString("false"));
                break;
        }
        //super.visit(n);
    }

    public SymbolProperties getLastVisitedIdentifer() {
        return props;
    }

    public void setLastVisitedIdentifer(SymbolProperties props) {
        this.props = props;
    }
    private static RESContext context = null;

    public static Cobol2Java getInstance(RESContext ctx) {
        if (thiz == null) {
            context = ctx;
            thiz = new Cobol2Java();
        }
        return thiz;
    }

    public static void clear() {
        thiz = null;
        context = null;
    }

    @Override
    public void visit(UnstringStatement n) {
        if (n.nodeOptional3.present() || n.nodeOptional4.present()) {
            doTry();
        }
        n.identifier.accept(this);
        if (props == null) {
            return;
        }
        aLine.push(new StringBuilder());
        bLine.push(new StringBuilder());
        aLine.peek().append("new CobolString(").append(new ExpressionString(props).toString()).append(')');
        if (n.nodeOptional.present()) {
            props = null;
            literal = null;
            NodeSequence nodeseq = (NodeSequence) n.nodeOptional.node;
            nodeseq.elementAt(3).accept(this);
            bLine.peek().append('.').append("delimitedBy(");
            if (props != null) {
                bLine.peek().append(new ExpressionString(props).toString());
            } else {
                bLine.peek().append(formatLiteral(literal).toString());
            }
            if (((NodeOptional) nodeseq.elementAt(2)).present()) {
                bLine.peek().append(",true");
            }
            bLine.peek().append(')');
            NodeListOptional orList = (NodeListOptional) nodeseq.elementAt(4);
            if (orList.present()) {
                for (Enumeration<Node> e = orList.elements(); e.hasMoreElements();) {
                    NodeSequence nodeseq2 = (NodeSequence) e.nextElement();
                    nodeseq2.elementAt(2).accept(this);
                    bLine.peek().append('.').append("delimitedBy(");
                    if (props != null) {
                        bLine.peek().append(new ExpressionString(props).toString());
                    } else {
                        bLine.peek().append(formatLiteral(literal).toString());
                    }
                    if (((NodeOptional) nodeseq2.elementAt(1)).present()) {
                        bLine.peek().append(",true");
                    }
                    bLine.peek().append(')');
                }
            }
        }
        aLine.peek().append(bLine.peek()).append(".unString();");
        String tempVar = "temp" + String.valueOf(SymbolTable.tempVariablesMark++);
        aLine.peek().insert(0, "CobolString " + tempVar + " = ");
        ClassFile.println(aLine.peek().toString());
        int i = 0;
        for (Enumeration<Node> e2 = n.nodeList.elements(); e2.hasMoreElements();) {
            NodeSequence nodeseq3 = (NodeSequence) e2.nextElement();
            nodeseq3.elementAt(0).accept(this);
            bLine.push(new StringBuilder());
            expressionType = Constants.STRING;
            expression.push(bLine.peek().append(tempVar).append(".getUnString()").toString());
            ClassFile.println(TranslationTable.getInstance().getAssignString(props, null, false));
            NodeOptional opt = (NodeOptional) nodeseq3.elementAt(1);
            if (opt.present()) {
                bLine.push(new StringBuilder());
                opt.accept(this);
                expression.push(bLine.peek().append(tempVar).append(".getUnStringDelimiter()").toString());
                ClassFile.println(TranslationTable.getInstance().getAssignString(props, null, false));
            }
            opt = (NodeOptional) nodeseq3.elementAt(2);
            if (opt.present()) {
                expressionType = Constants.INTEGER;
                bLine.push(new StringBuilder());
                opt.accept(this);
                expression.push(bLine.peek().append(tempVar).append(".getUnStringCount()").toString());
                ClassFile.println(TranslationTable.getInstance().getAssignString(props, null, false));
            }
            i++;
        }
        if (n.nodeOptional1.present()) {
            bLine.push(new StringBuilder());
            bLine.peek().append(tempVar).append(".getUnStringPointer()");
            n.nodeOptional1.accept(this);
            expression.push(bLine.peek().toString());
            expressionType = Constants.INTEGER;
            ClassFile.println(TranslationTable.getInstance().getAssignString(props, null, false));
        }
        bLine.push(new StringBuilder());
        bLine.peek().append(tempVar).append(".getUnStringTally()");
        if (n.nodeOptional2.present()) {
            n.nodeOptional2.accept(this);
            expression.push(bLine.peek().toString());
            expressionType = Constants.INTEGER;
            ClassFile.println(TranslationTable.getInstance().getAssignString(props, null, false));
        }

        aLine.pop();
        bLine.pop();

        if (n.nodeOptional3.present() || n.nodeOptional4.present()) {
            //ClassFile.println("__assertException();");
            if (n.nodeOptional4.present()) {//Not On OverFlow
                n.nodeOptional4.accept(this);
            }
            ClassFile.backTab();
            ClassFile.println("} catch(OverflowException ofe) {");
            ClassFile.tab();
            exceptionPrintStackTrace("ofe");
            if (n.nodeOptional3.present()) {//On OverFlow
                n.nodeOptional3.accept(this);
            }
            ClassFile.backTab();
            ClassFile.println("}");
        }
    }

    @Override
    public void visit(SearchStatement n) {
        n.qualifiedDataName.accept(this);
        if (!props.isOccurs()) {
            reportError(n, "Search Statement is valid only on tables with Occurs clause.");
        }
        if (props.getMajorIndex() == null) {
            reportError(n, "Search Statement is valid only on tables with INDEXED BY.");
        }
        SymbolProperties table = props;
        props = null;
        n.nodeOptional1.accept(this);
        SymbolProperties varying = props;
        ClassFile.println("do {");
        ClassFile.tab();
        ClassFile.println("if(" + NameUtil.getJavaName(table.getMajorIndex(), false) + ">"
                + table.getMaxOccurs() + ") {");
        ClassFile.tab();
        n.nodeOptional2.accept(this);
        if (!isLastGotoStatement) {
            ClassFile.println(" break;");
        }
        ClassFile.backTab();
        ClassFile.println("}");
        for (Enumeration<Node> e = n.nodeList.elements(); e.hasMoreElements();) {
            NodeSequence nodeseq = (NodeSequence) e.nextElement();
            nodeseq.elementAt(1).accept(this);
            ClassFile.println("if(" + condition.pop() + ") {");
            ClassFile.tab();
            NodeChoice nodech = (NodeChoice) nodeseq.elementAt(2);
            if (nodech.which == 0) {
                nodech.accept(this);
                if (!isLastGotoStatement) {
                    ClassFile.println(" break;");
                }
            } else {
                ClassFile.println(" break;");
            }
            ClassFile.backTab();
            ClassFile.println("}");
        }
        ClassFile.println(getIncrement(table.getMajorIndex()) + ";");
        if (varying != null && !varying.equals(table.getMajorIndex())) {
            ClassFile.println(getIncrement(varying) + ";");
        }
        ClassFile.backTab();
        ClassFile.println("} while(true);");
    }

    public String getIncrement(SymbolProperties props2) {
        return NameUtil.getJavaName(props2, true).replace("%0", NameUtil.getJavaName(props2, false) + "+1");
    }

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
    public class ExpressionString implements Cloneable {

        public StringBuilder literal;
        public String raw;
        public SymbolProperties props = null;
        public int type = -1;
        public boolean isAll = false;
        public boolean isJustRight = false;
        public int length = -1;
        public boolean isIdSymbol = false;
        public boolean isRounded = false;

        public String toString() {
            return literal.toString();
        }
       public String toString(boolean isO) {
           if(isO)
               return super.toString();
            return literal.toString();
        }

        public ExpressionString(SymbolProperties sym) {
            this(sym, 0);
        }

        public ExpressionString(SymbolProperties sym, int scale) {
            this(sym, scale, false);
        }

        public ExpressionString(SymbolProperties sym, int scale, boolean isPlainGet) {
            if (sym == null) {
                literal = new StringBuilder("");
            } else {
                isIdSymbol = true;
                literal = new StringBuilder(NameUtil.getJavaName(props = sym, false));
                type = sym.getIdentifierType();
                if (scale != 0) {
                    if (scale < 0) {

                        literal.insert(0, "__scale(").append(',').append(scale - sym.getJavaType().getMaxIntLength()).append(')');
                    } else if (scale > 0) {

                        literal.insert(0, "__scale(").append(',').append(scale).append(')');
                    }
                    type = Constants.BIGDECIMAL;
                }

                length = sym.getLength();
            }
        }

        public ExpressionString(SymbolProperties sym, boolean isRnd) {
            this(sym, 0);
            isRounded = isRnd;
        }

        public ExpressionString(String lit) {
            if (lit == null) {
                lit = "";
            }
            literal = new StringBuilder(lit);
        }

        public ExpressionString(String lit, int type) {
            this(lit);
            this.type = type;
        }

        public ExpressionString(ExpressionString lit) {            
            isJustRight = lit.isJustRight;
            literal = lit.literal;

            raw = lit.raw;
            props = lit.props;
            literal = new StringBuilder(lit.literal);

            length = lit.length;
            isAll = lit.isAll;
            type = lit.type;
            props = lit.props;
            isIdSymbol = lit.isIdSymbol;
            isRounded = lit.isRounded;
        }

        public ExpressionString() {
            literal = new StringBuilder();
        }

        public ExpressionString(int lit) {
            this(String.valueOf(lit));
        }

        public ExpressionString set(String lit) {
            literal = new StringBuilder(lit);
            return this;
        }

        public ExpressionString set(int lit) {
            literal = new StringBuilder(String.valueOf(lit));
            return this;
        }

        public ExpressionString setAll(boolean b) {
            isAll = b;
            return this;
        }

        public ExpressionString setType(int t) {
            type = t;
            return this;
        }

        public ExpressionString setLength(int l) {
            length = l;
            return this;
        }

        public void setString(String str) {
            literal = new StringBuilder(str);
        }

        public void setString(String str, int type) {
            literal = new StringBuilder(str);
            this.type = type;
        }

        public void setString(StringBuilder str) {
            literal = str;
        }

        //The Three Methods below for upward compatibility with old IdOrLit class.
        public SymbolProperties Id() {
            return props;
        }

        public String Lit() {
            return literal.toString();
        }

        public boolean IsRounded() {
            return isRounded;
        }

        public ExpressionString append(String s) {
            if (literal != null) {
                literal.append(s);
            }
            return this;
        }

        public ExpressionString insert(int i, String s) {
            if (literal != null) {
                literal.insert(i, s);
            }
            return this;
        }

        public ExpressionString replace(int i, int j, String s) {
            if (literal != null) {
                literal.replace(i, j, s);
            }
            return this;
        }
    }

    public ExpressionString getExpression() {
        ExpressionString thz = expression.pop();
        if (thz.type < 0) {
            thz.type = expressionType;
        }
        return thz;
    }

    public void initializeSymbols() {

        ClassFile.doMethodScope("public " + SymbolTable.getScope().getCurrentProgram().getJavaName2()
                + "() {");
        if (SymbolTable.getScope().getCurrentProgram().getAdjustedLength() > 0) /*
        if(SymbolTable.getScope().getCurrentProgram().getNoLivingFillers()>0)
        ClassFile.println("\tsuper(" +
        String.valueOf(SymbolTable.getScope().getCurrentProgram().getNoLivingFillers())+","+
        "new CobolBytes("+
        String.valueOf(SymbolTable.getScope().getCurrentProgram().getAdjustedLength())+"));");
        else*/ {
            ClassFile.println("\tsuper(" + "new CobolBytes("
                    + String.valueOf(SymbolTable.getScope().getCurrentProgram().getAdjustedLength()) + "));");
        } else;
        ClassFile.println("}");
        ClassFile.println("public void initialize(Program p) {");
        ClassFile.tab();
        ClassFile.println("if(__initialized) return; else __initialized=true;");
        ClassFile.println("__setProgram(p);");
        if (context.isDecimalPointIsComma()) {
            ClassFile.println("__setDecimalPointIsComma();");
        }
        if (context.getCurrencySign() != null) {
            ClassFile.println("__setCurrencySign(" + context.getCurrencySign() + ");");
        }
        ArrayList<?> a = (ArrayList<?>) SymbolTable.getScope().getCurrentProgram().getChildren();
        if (a != null) {
            for (Iterator<?> e = a.iterator(); e.hasNext();) {
                SymbolProperties o2 = (SymbolProperties) e.next();
                if (!(o2.getRef() || o2.getMod())) {
                    continue;
                }
                initializeOneSymbol(o2);
            }
        }
        ClassFile.backTab();
        ClassFile.endMethodScope();
    }

    public void initializeOneSymbol(SymbolProperties initProps) {
        //try{
        if (initProps == null
                || //!(initProps.getRef()||initProps.getMod())||
                initProps.getType() == SymbolConstants.PARAGRAPH
                || initProps.getType() == SymbolConstants.SECTION) {
            return;
        }
        if (initProps.getIsSuppressed()) {
            return;
        }

        if (initProps.getJavaType() != null) {
            initProps.setIdentifierType(initProps.getJavaType().getType());
        }

        int lvl = initProps.getLevelNumber();

        if (lvl == 88 || lvl == 66 || lvl == 78) {
            return;
        }

        if (initProps.is01Group()) {
            if (initProps.getValues() != null && initProps.getValues().size() > 0) {
                SymbolProperties.CoupleValue values = initProps.getValues().get(0);
                expression.push(formatLiteral(values.value1).toString());
                String exprString = TranslationTable.getInstance().getAssignString(initProps, null, false, true, true);
                exprString = exprString.substring(exprString.substring(0, exprString.indexOf('(') + 1).indexOf('.') + 1);
                ClassFile.println(exprString);
            } else {
                ClassFile.println(initProps.getJavaName1() + ".initialize(this);");
            }
            return;
        }
        if (initProps.getValues() != null && initProps.getValues().size() > 0) {
            SymbolProperties.CoupleValue values = initProps.getValues().get(0);
            String temp = formatLiteral(values.value1).toString();
            if (values.value1.isAll) {
                temp = "__all(" + temp + ',' + String.valueOf(initProps.getLength()) + ")";
            }
            if (initProps.getJavaType().getMaxScalingLength() != 0) {
                temp = doScaleLiteral(initProps, temp, false);
            }
            expression.push(new ExpressionString(temp, expressionType));
            if (!initProps.getIsFiller() && initProps.getNoOccursSubscripts() > 0) {
                int idx = 1;
                if (initProps.getIndexesWorkSpace() != null) {
                    initProps.getIndexesWorkSpace().clear();
                } else {
                    initProps.setIndexesWorkSpace(new ArrayList<String>());
                }
                for (SymbolProperties par : initProps.getOccursParents()) {
                    String idxStr = "idx" + String.valueOf(idx++);
                    ClassFile.println("for(int " + idxStr + "=1;"
                            + idxStr + "<=" + String.valueOf(par.getMaxOccurs())
                            + ";++" + idxStr + ") {");
                    ClassFile.tab();
                    initProps.getIndexesWorkSpace().add(idxStr);
                }
            }

            if (!initProps.getIsFiller()) {
                temp = TranslationTable.getInstance().getAssignString(initProps, null, false);
                temp = temp.substring(temp.substring(0, temp.indexOf('(') + 1).indexOf('.') + 1);
            } else {
                String exprString = TranslationTable.getInstance().convertType(expression.pop3(), initProps.getIdentifierType(), expressionType);
                temp = SymbolUtil.getInstance().getLocalFillerSetter(initProps, exprString) + ';';
            }


            if (temp != null && temp.trim().length() > 0) {
                ClassFile.println(temp);
            }
            if (!initProps.getIsFiller() && initProps.getNoOccursSubscripts() > 0) {
                initProps.getIndexesWorkSpace().clear();
                for (int idx = 0; idx < initProps.getNoOccursSubscripts(); ++idx) {
                    ClassFile.backTab();
                    ClassFile.println("}");
                }
            }
        }
        //} catch(Exception e) {
        //e.printStackTrace();
        //}
        return;

    }

    public String doScaleLiteral(SymbolProperties initProps, String temp, boolean isArith) {

        temp = "__scale(" + temp + ',' + ((!isArith ? ((initProps.getJavaType().getMaxScalingLength() < 0)
                ? (-initProps.getJavaType().getMaxScalingLength() + initProps.getJavaType().getMaxIntLength())
                : -initProps.getJavaType().getMaxScalingLength())
                : initProps.getJavaType().getMaxScalingLength())) + ')';
        expressionType = Constants.BIGDECIMAL;

        return temp;
    }

    private String doOneAll(ExpressionString lit, int len) {
        if (lit.isAll && len > 0) {
            return lit.literal.insert(0, "__all(").append(',').append(len).append(')').toString();
        } else {
            return lit.toString();
        }
    }

    public void doOne88(SymbolProperties o1) {
        try {
            if (o1 == null) {
                return;
            }
            if (o1.getIsSuppressed()) {
                return;
            }

            int lvl = o1.getLevelNumber();

            if (lvl != 88 || !(o1.getParent().getRef() || o1.getParent().getMod())) {
                return;
            }

            if (o1.getValues() == null || o1.getValues().size() <= 0) {
                return;
            }
            String getSubscripts = "";
            String accessSubscripts = "";
            if (o1.getParent().isOccurs() || o1.getParent().isAParentInOccurs()) {
                int size;
                if (o1.getParent().getOccursParents() != null && (size = o1.getParent().getOccursParents().size()) > 0) {
                    for (int i = 1; i <= size; ++i) {
                        getSubscripts += "int idx" + String.valueOf(i) + ((i != size) ? "," : "");
                        accessSubscripts += "idx" + String.valueOf(i) + ((i != size) ? "," : "");

                    }
                }
            }

            ClassFile.doMethodScope("public boolean get" + o1.getJavaName2() + "(" + getSubscripts
                    + ") {");
            ClassFile.tab();
            String retValue;
            if (o1.getParent().getIsFiller()) {
                retValue = SymbolUtil.getInstance().getLocalFillerGetter(o1.getParent());
            } else {
                retValue = "get" + o1.getParent().getJavaName2() + "(" + accessSubscripts
                        + ")";
            }
            retValue = "equals(" + retValue;
            int numThrough = 0;

            boolean foundPlain = false;
            boolean first = true;
            methodScope:
            {
                for (SymbolProperties.CoupleValue values : o1.getValues()) {
                    if (values.value2 != null) {
                        ++numThrough;
                        continue;
                    }
                    foundPlain = true;
                    retValue += "," + doOneAll(formatLiteral(values.value1), o1.getParent().getLength());
                }

                if (numThrough > 0) {
                    if (foundPlain) {
                        ClassFile.println("boolean b_=" + retValue + ");");
                    } else {
                    }

                } else {
                    if (foundPlain) {
                        ClassFile.println("return " + retValue + ");");
                    } else {
                        ClassFile.println("return false;");
                    }
                    break methodScope;
                }
                first = true;
                for (SymbolProperties.CoupleValue values : o1.getValues()) {
                    if (values.value2 == null) {
                        continue;
                    }
                    if (first) {
                        if (numThrough == 1) {
                            ClassFile.println("return equalsRange(get" + o1.getParent().getJavaName2() + "(" + accessSubscripts
                                    + "),"
                                    + doOneAll(formatLiteral(values.value1), o1.getParent().getLength()) + ","
                                    + doOneAll(formatLiteral(values.value2), o1.getParent().getLength()) + ");");
                            //ClassFile.println("return b_;");
                            break methodScope;
                        }
                        if (!foundPlain) {
                            ClassFile.println("boolean b_=equalsRange(get" + o1.getParent().getJavaName2() + "(" + accessSubscripts
                                    + "),"
                                    + doOneAll(formatLiteral(values.value1), o1.getParent().getLength()) + "," + doOneAll(formatLiteral(values.value2), o1.getParent().getLength()) + ");");
                            foundPlain = true;
                            continue;
                        }
                    }
                    ClassFile.println("b_|=equalsRange(get" + o1.getParent().getJavaName2() + "(" + accessSubscripts
                            + "),"
                            + doOneAll(formatLiteral(values.value1), o1.getParent().getLength()) + "," + doOneAll(formatLiteral(values.value2), o1.getParent().getLength()) + ");");
                }
                ClassFile.println("return b_;");
            }
            ClassFile.backTab();
            ClassFile.endMethodScope();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;

    }
}
