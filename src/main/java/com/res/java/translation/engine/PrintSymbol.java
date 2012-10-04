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
import com.res.cobol.Main;
import com.res.cobol.TreeToCommentFormatter;
import com.res.common.RESConfig;
import com.res.java.lib.Constants;
import com.res.java.lib.RunTimeUtil;
import com.res.java.translation.symbol.SymbolConstants;
import com.res.java.translation.symbol.SymbolProperties;
import com.res.java.translation.symbol.SymbolProperties.CobolSymbol;
import com.res.java.translation.symbol.SymbolTable;
import com.res.java.translation.symbol.SymbolUtil;
import com.res.java.translation.symbol.Visitor;
import com.res.java.util.ClassFile;
import com.res.java.util.NameUtil;

public class PrintSymbol implements Visitor {

    public PrintSymbol() {
    }

    public PrintSymbol(boolean nps) {
        noProgramScope = nps;
    }
    private boolean noProgramScope = false;

    @Override
    public void visit01Element(SymbolProperties props) {
        printElement(props);
    }

    @Override
    public void visit01Group(SymbolProperties props) {
        printGroup(props, true);
    }

    @Override
    public void visit77Element(SymbolProperties props) {
        printElement(props);
    }

    @Override
    public void visit88Element(SymbolProperties props) {
        if (props.getParent().is01Group()) {
            return;
        }
        //This will be done in doClassEnding() for 01 level 88s.
        printAnnotations(props);
        Main.getContext().getCobol2Java().doOne88(props);
    }

    @Override
    public void visitChildPostprocess(SymbolProperties props) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visitInnerGroup(SymbolProperties props) {
        printGroup(props, false);
    }

    @Override
    public void visitParagraph(SymbolProperties props) {
        props.setUsedNativeJavaTypes(true);//faking
    }
    private boolean indexedFileRecord = false;

    @Override
    public void visitFile(SymbolProperties props) {
        SymbolTable.visit(props.getChildren(), this);
        printFile(props);
    }

    @Override
    public void visitPostprocess(SymbolProperties props) {
        if (indexedFileRecord && props.getParent() != null && props.getParent().isIndexedFile()) {
            indexedFileRecord = false;
        }
    }

    @Override
    public void visitPreprocess(SymbolProperties props) {
        if (Main.getContext().getTraceLevel() >= 2) {
            System.out.println("Doing PrintSymbol symbol " + props.getDataName());
        }
        if (noProgramScope) {
            props.setRef(true);
            props.setIsFormat(false);
        }

        if (props.isIndexedFile()) {
            indexedFileRecord = true;
        }

        if (props.getDataUsage() == Constants.INDEX) {
            props.setDataUsage((short) Constants.BINARY);
        }

    }

    @Override
    public void visitChildPreprocess(SymbolProperties props) {
    }

    @Override
    public void visitInnerElement(SymbolProperties props) {
        printElement(props);
    }

    @Override
    public void visitProgram(SymbolProperties props) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visitSection(SymbolProperties props) {
        props.setUsedNativeJavaTypes(true);//faking
    }
    public static TreeToCommentFormatter dumper = new TreeToCommentFormatter(false);

    private void printGroup(SymbolProperties props, boolean isClassScope) {

          if (Main.getContext().getTraceLevel() >= 2) {
            System.out.println("Entering PrintSymbol symbol printGroup " + props.getDataName());
        }

        if (props.getLevelNumber() == 66) {
            printAnnotations(props);
            print66(props);
            return;
        }

        //Picks up from com.res.java.lib
        if (props.is01Group()) {
            if (props.isFromRESLibrary()) {
                props.setRef(false);
                props.setMod(false);
                RunTimeUtil.getInstance().reportError("Warning: 01 level Group "
                        + props.getJavaName1().toUpperCase()
                        + " not translated. Using com.res.java.lib."
                        + props.getJavaName2(), false);
                ClassFile.println("public " + props.getJavaName2() + " " + props.getJavaName1() + " = new "
                        + props.getJavaName2() + "();");
                return;
            }
            if (!(props.getRef() || props.getMod())) {
                return;
            }
        }

        if (!(props.getRef() || props.getMod()) || props.isProgram()) {
            printAnnotations(props);
            SymbolTable.visit(props.getChildren(), this);
            return;
        }

        if (props.getLevelNumber() == 78) {
            printAnnotations(props);
            printA78(props);
            return;
        }

        if (props.getLevelNumber() == 88 && props.getParent().getLevelNumber() == 1
                && props.getParent().getPictureString() == null) {
            return;
        }

        //Case 1 - Do main 01 or inner group data level.
        String name2 = props.getJavaName2();

        if (isClassScope && props.isGroupData()) {
            printAnnotations(props);
            SymbolUtil.getInstance().doClassBegin(props, name2, !noProgramScope);
            SymbolTable.visit(props.getChildren(), this);
        } else {
            printElement(props);
        }

        //props.setJavaType(new CobolSymbol());
        props.getJavaType().setType((byte) Constants.GROUP);

        if (isClassScope && props.isGroupData()) {
            SymbolUtil.getInstance().doClassEnding(props, !noProgramScope);
        }
        return;
    }

    private void print66(SymbolProperties props) {
        SymbolUtil.getInstance().printCobolByteAccessors(props);
    }

    private void printAnnotations(SymbolProperties props) {

        if (noProgramScope) {
            return;
        }

        if ((RESConfig.getInstance().isRetainCobolComments()
                || RESConfig.getInstance().isPrintCobolStatementsAsComments())
                && props.getDataDescriptionEntry() != null) {
            dumper.startAtNextToken();
            dumper.visit(props.getDataDescriptionEntry());
            if (RESConfig.getInstance().isPrintCobolStatementsAsComments()) {
                ClassFile.println("");
            }
        }
    }

    private void printElement(SymbolProperties props) {
        
        if (Main.getContext().getTraceLevel() >= 2) {
            System.out.println("Entering PrintSymbol symbol printElement" + props.getDataName());
        }

        if (props.isIndexRedefines() && props.getRedefines() != null) {
            return;
        }

        if (props.isFromRESLibrary() || props.isVaryingArray() || props.isVaryingLen() || props.getLength() == 0) {
            return;
        }
       if (Main.getContext().getTraceLevel() >= 2) {
            System.out.println("Doing PrintSymbol symbol printElement" + props.getDataName());
        }
        if (!(props.getRef() || props.getMod()) || props.getType() == SymbolConstants.PROGRAM) {
            SymbolTable.visit(props.getChildren(), this);
            return;
        }

        if (props.getLevelNumber() == 88 && props.getParent().getLevelNumber() == 1
                && props.getParent().getPictureString() == null) {
            return;
        }

        printAnnotations(props);

        //Case 2 - Has a picture - create a element data level
        if (props.isUsedNativeJavaTypes() || props.isFloatingPoint()) {
            SymbolUtil.getInstance().printJavaTypesAccessors(props);
        } else {
            SymbolUtil.getInstance().printCobolByteAccessors(props);
        }

        SymbolTable.visit(props.getChildren(), this);

        return;
    }

    private SymbolProperties findIndexedRecord(SymbolProperties file) {
        for (SymbolProperties ch : file.getChildren()) {
            if (ch.isIndexedFileRecord()) {
                return ch;
            }
        }
        return file.getChildren().get(0);
    }

    private void printFile(SymbolProperties props) {
        if (props.isIndexedFile()) {
            ClassFile.println("private CobolIndexedFile " + props.getJavaName1()
                    + " = new CobolIndexedFile(" + Main.getContext().getCobol2Java().formatLiteral('\"' + NameUtil.convertCobolNameToSQL(props.getOtherName())
                    + '\"')
                    + ',' + String.valueOf(props.getOtherData1()) + ',' + findIndexedRecord(props).getJavaName1() + ");");
            TranUtil.get().printIndexedFileDatabaseProperties(props);
        } else if (props.isLineSequentialFile()) {
            ClassFile.println("private CobolFile " + props.getJavaName1()
                    + " = new CobolFile(" + Main.getContext().getCobol2Java().formatLiteral(props.getOtherName())
                    + ",true);");
        } else //if(props.isSequentialFile())
        {
            ClassFile.println("private CobolFile " + props.getJavaName1()
                    + " = new CobolFile(" + Main.getContext().getCobol2Java().formatLiteral(props.getOtherName())
                    + "," + String.valueOf(props.getLength())
                    + ((props.getAdjustedLength() != props.getLength())
                    ? ("," + String.valueOf(props.getAdjustedLength())) : "")
                    + ',' + String.valueOf(props.getOtherData1()) + ");");
        }

    }

    private void printA78(SymbolProperties props) {
        if (props.getValues() == null || props.getValues().size() <= 0) {
            return;
        }
        String constant = Main.getContext().getCobol2Java().formatLiteral(props.getValues().get(0).value1).toString();
        props.setIdentifierType((byte) Main.getContext().getCobol2Java().expressionType);
        ClassFile.println("public final " + SymbolConstants.get(Main.getContext().getCobol2Java().expressionType) + " get"
                + props.getJavaName2() + "() {");
        ClassFile.println("\treturn " + constant + ";");
        ClassFile.println("}");
    }
}
