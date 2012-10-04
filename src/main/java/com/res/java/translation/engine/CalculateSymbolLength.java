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
import java.util.Stack;

import com.res.cobol.Main;
import com.res.common.RESConfig;
import com.res.java.lib.Constants;
import com.res.java.lib.FieldFormat;
import com.res.java.lib.RunTimeUtil;
import com.res.java.translation.symbol.SymbolConstants;
import com.res.java.translation.symbol.SymbolProperties;
import com.res.java.translation.symbol.SymbolTable;
import com.res.java.translation.symbol.SymbolUtil;
import com.res.java.translation.symbol.Visitor;
import com.res.java.util.FieldAttributes;

//Called through SymbolTable.visit() which is in turn 
//Invoked from CobolFillTable.visit(ProcedureDivision n)
public class CalculateSymbolLength implements Visitor {

    @Override
    public void visitPreprocess(SymbolProperties props) {
        if (Main.getContext().getTraceLevel() >= 2) {
            System.out.println("Doing CalculateSymbolLength symbol " + props.getDataName());
        }

        if (props.isOccurs() && props.getType() != SymbolConstants.PROGRAM) {
            if (props.getLevelNumber() == 1 || props.getLevelNumber() == 66
                    || props.getLevelNumber() == 88 || props.getLevelNumber() == 77) {
                props.setOccurs(false);
            } else {
                subscriptParents.push(props);
            }
        }
 
        if (props.getRedefines() != null) {
            if (props.isGroupData()) {
                offset.push(props.getRedefines().getOffset());
                unAdjustedOffset.push(props.getRedefines().getUnAdjustedOffset());
            }
        } else if (props.getType() == SymbolConstants.PROGRAM
                || (props.is01Group())) {
            offset.push(0);
            unAdjustedOffset.push(0);
        }

        if (props.getType() != SymbolConstants.PROGRAM) {
            props.setAParentInOccurs(props.getParent().isOccurs() || props.getParent().isAParentInOccurs());
        }

        if (props.is01Group() && props.hasRenames()) {
            alwaysCobolBytes = true;
        }

        if (props.getDataUsage() == Constants.INDEX) {
            props.setDataUsage((short) Constants.BINARY);
            isIndexSetToBinary = true;
        } else {
            isIndexSetToBinary = false;
        }
    }
    private boolean isIndexSetToBinary = false;

    @Override
    public void visitPostprocess(SymbolProperties props) {

        if (Main.getContext().getTraceLevel() >= 2) {
            System.out.println("Done CalculateSymbolLength symbol " + props.getDataName() + "O=" + props.getOffset()
                    + "L=" + props.getLength() + "UO=" + props.getUnAdjustedOffset() + "AL=" + props.getAdjustedLength());
        }

        if (alwaysNoFormat) {
            props.setIsFormat(false);
        }
        if (alwaysCobolBytes) {
            props.setUsedNativeJavaTypes(false);
        } else if (props.getType() != SymbolConstants.PROGRAM
                && RESConfig.getInstance().getOptimizeAlgorithm() == 1);
        if (props.isOccurs()) {
            subscriptParents.pop();
        }
        if (props.is01Group() && props.getDataName().equalsIgnoreCase("SQLCA")) {
            props.setLength(100);//Hack
        }

        if (props.getRedefines() != null) {
            int maxLen = props.getLength();
            int maxAdjLen = props.getAdjustedLength();
            SymbolProperties props2 = props;
            while (props2.getRedefines() != null) {
                maxLen = Math.max(maxLen, props2.getRedefines().getLength());
                maxAdjLen = Math.max(maxAdjLen, props2.getRedefines().getAdjustedLength());
                if (props2.getLength() > props2.getRedefines().getLength()) {
                    if (props.getParent().isIndexedFile() && props.getRedefines().getParent().isIndexedFile()) {
                        if (props.getParent().getDataName().equalsIgnoreCase(props.getRedefines().getParent().getDataName())) {
                            props.setIndexedFileRecord(true);
                            props.getRedefines().setIndexedFileRecord(false);
                        } else {
                            props.getRedefines().setIndexedFileRecord(true);
                        }
                    }
                } else {
                    if (props2.getParent().isIndexedFile() && props2.getRedefines().getParent().isIndexedFile()) {
                        if (props2.getParent().getDataName().equalsIgnoreCase(props2.getRedefines().getParent().getDataName())) {
                            props2.setIndexedFileRecord(false);
                            props2.getRedefines().setIndexedFileRecord(true);
                        } else {
                            props2.setIndexedFileRecord(true);
                        }
                    }
                }
                props2 = props2.getRedefines();
            }
            props2 = props;
            props2.setLength(maxLen);
            props2.setAdjustedLength(maxAdjLen);
            if (props2.getRedefinedBy() != null) {
                for (SymbolProperties r : props2.getRedefinedBy()) {
                    r.setLength(maxLen);
                    r.setAdjustedLength(maxAdjLen);
                }
            }
            while (props2.getRedefines() != null) {
                props2.getRedefines().setLength(maxLen);
                props2.getRedefines().setAdjustedLength(maxAdjLen);
                props2 = props2.getRedefines();
                if (props2.getRedefinedBy() != null) {
                    for (SymbolProperties r : props2.getRedefinedBy()) {
                        r.setLength(maxLen);
                        r.setAdjustedLength(maxAdjLen);
                    }
                }
            }

        } else {
            if (props.getType() == SymbolConstants.PROGRAM || props.is01Group()) {
                offset.pop();
                unAdjustedOffset.pop();
            }
            if (props.getParent() != null && props.getParent().isIndexedFile()) {
                props.setIndexedFileRecord(true);
            }
        }
        if (props.is01Group() && props.hasRenames()) {
            alwaysCobolBytes = false;
        }
        if (isIndexSetToBinary && props.getDataUsage() == Constants.BINARY) {
            props.setDataUsage((short) Constants.INDEX);
        }
    }
    private boolean alwaysCobolBytes = false;
    private boolean alwaysNoFormat = false;

    @Override
    public void visitChildPreprocess(SymbolProperties props) {
    }

    @Override
    public void visitChildPostprocess(SymbolProperties props) {
    }

    @Override
    public void visit01Element(SymbolProperties props) {
        calculateElementLength(props);
    }

    @Override
    public void visit01Group(SymbolProperties props) {
        if (props.getDataName().equalsIgnoreCase("sqlca")) {
            alwaysNoFormat = true;
        }
        calculateGroupLength(props);
        alwaysNoFormat = false;
    }

    @Override
    public void visit77Element(SymbolProperties props) {
        calculateElementLength(props);
    }

    @Override
    public void visit88Element(SymbolProperties props) {
        //props.setJavaType(new com.res.java.translation.symbol.SymbolProperties.CobolSymbol());
        props.getJavaType().setType(props.getParent().getJavaType().getType());
    }

    @Override
    public void visitInnerElement(SymbolProperties props) {
        calculateElementLength(props);
    }

    @Override
    public void visitInnerGroup(SymbolProperties props) {
        calculateGroupLength(props);
    }

    @Override
    public void visitProgram(SymbolProperties props) {
        calculateGroupLength(props);
    }
    private static Stack<SymbolProperties> subscriptParents = new Stack<SymbolProperties>();
    private static Stack<Integer> offset = new Stack<Integer>();
    private static Stack<Integer> unAdjustedOffset = new Stack<Integer>();
    private static Stack<Integer> noLiveFillers = new Stack<Integer>();

    private void calculateGroupLength(SymbolProperties props) {

        int leng;
        props.setFormat(false);

        if (props.isFromRESLibrary()) {
            return;
        }

        props.getJavaType().setType((byte) Constants.GROUP);

        if (props.getLevelNumber() == 66) {
            processRenames(props);
            return;
        }


        int prevOffset = unAdjustedOffset.peek();
        int prevAdjustedOffset = offset.peek();
        props.setOffset(offset.peek());
        props.setUnAdjustedOffset(unAdjustedOffset.peek());

        switch (RESConfig.getInstance().getOptimizeAlgorithm()) {
            case 0:
                SymbolUtil.setCheckUseNativeJavaTypes(props, alwaysCobolBytes);
                break;
            case 1:
                //SymbolUtil.setCheckUseNativeJavaTypes(props,alwaysCobolBytes);
                props.setUsedNativeJavaTypes(
                        (props.getRef() || props.getMod())
                        && (props.getLevelNumber() == 1 || props.getType() == SymbolConstants.PROGRAM));
                props.setUsedNativeJavaTypes(props.isUsedNativeJavaTypes() && !alwaysCobolBytes
                        && !props.isForceCobolBytes());
                break;
            case 2:                
                SymbolUtil.setCheckUseNativeJavaTypes(props, alwaysCobolBytes);
        }

        if (props.hasChildren()) {
            SymbolTable.visit(props.getChildren(), this);
        }

        if (props.isOccurs() || props.isAParentInOccurs()) {
            ArrayList<SymbolProperties> b = new ArrayList<SymbolProperties>();
            b.addAll(subscriptParents);
            props.setOccursParents(b);
            props.setNoOccursSubscripts(b.size());
        }


        leng = unAdjustedOffset.peek() - prevOffset;
        int adjustedLength = offset.peek() - prevAdjustedOffset;

        props.setLength(leng);
        props.setAdjustedLength(adjustedLength);

        if (props.isOccurs() && props.getMaxOccursInt() > 0) {
            leng *= (props.getMaxOccursInt() - 1);
        } else {
            leng = 0;
        }

        if (props.getRedefines() != null) {
            offset.pop();
            unAdjustedOffset.pop();
        } else {
            unAdjustedOffset.push(unAdjustedOffset.pop() + leng);
            if (!props.isUsedNativeJavaTypes() && adjustedLength > 0) {
                offset.push(offset.pop() + leng);
            }
        }
    }

    private void processRenames(SymbolProperties props) {
        SymbolProperties from = props.getRedefinedBy().get(0);
        if (from == null) {
            RunTimeUtil.getInstance().reportError("Invalid RENAMES clause in symbol: " + props.getDataName(), true);
        }
        SymbolProperties thru = from;
        if (props.getRedefinedBy().size() > 1) {
            thru = props.getRedefinedBy().get(1);
        }
        if (thru == null) {
            RunTimeUtil.getInstance().reportError("Invalid RENAMES clause in symbol: " + props.getDataName(), true);
        }
        if (thru.getUnAdjustedOffset() - from.getUnAdjustedOffset() + thru.getLength() <= 0) {
            RunTimeUtil.getInstance().reportError("Invalid RENAMES clause in symbol: " + props.getDataName(), true);
        }
        props.setOffset(from.getOffset());
        props.setUnAdjustedOffset(from.getUnAdjustedOffset());
        props.setAdjustedLength(thru.getUnAdjustedOffset() - from.getUnAdjustedOffset() + thru.getLength());
        props.setLength(thru.getUnAdjustedOffset() - from.getUnAdjustedOffset() + thru.getLength());
    }

    private void calculateElementLength(SymbolProperties props) {

        int leng;
        boolean isSuppressed = (Boolean) props.getIsSuppressed() || !(props.getRef() || props.getMod());

        if (isSuppressed || props.isFromRESLibrary()) {
            return;
        }


       // com.res.java.translation.symbol.SymbolProperties.CobolSymbol sym = new com.res.java.translation.symbol.SymbolProperties.CobolSymbol();
        props.getJavaType().setPic((String) props.getPictureString());

        String u = props.getJavaType().getPic().toUpperCase();
       // props.getJavaType().setUsage((byte) props.getDataUsage());

        if (FieldFormat.verifyCobolPicture(u) == Constants.BIGDECIMAL) {
            FieldAttributes.processDecimal(u, props.getJavaType(), props.getDataCategory() == Constants.NUMERIC_EDITED);
            props.setCurrency(props.getJavaType().isIsCurrency());
            props.setSigned(props.getJavaType().isIsSigned());
        } else if (FieldFormat.verifyCobolPicture(u) == Constants.INTEGER) {
            FieldAttributes.processDecimal(u, props.getJavaType(), props.getDataCategory() == Constants.NUMERIC_EDITED);
            props.setSigned(props.getJavaType().isIsSigned());
            props.setCurrency(props.getJavaType().isIsCurrency());
        } else {
            if (FieldFormat.verifyCobolPicture(u) == Constants.STRING) {
                FieldAttributes.processAlpha(u, props.getJavaType());
            } else {
                SymbolUtil.getInstance().reportError("Error In Usage or Picture of: " + props.getDataName()
                        + ((props.getParent() != null) ? " IN " + props.getParent().getDataName() : "")
                        + ((props.getPictureString() != null) ? " PICTURE " + props.getPictureString() : ""));
            }
        }

        if (props.getDataUsage() == Constants.COMPUTATIONAL1) {
            props.getJavaType().setType(Constants.FLOAT);
        } else if (props.getDataUsage() == Constants.COMPUTATIONAL2) {
            props.getJavaType().setType(Constants.DOUBLE);
        }

        props.setJavaType(props.getJavaType());
        leng = FieldAttributes.calculateLength(props);
        if (props.getLevelNumber() == 78) {
            return;
        }

        if (props.isOccurs() || props.isAParentInOccurs()) {
            ArrayList<SymbolProperties> b = new ArrayList<SymbolProperties>();
            b.addAll(subscriptParents);
            props.setOccursParents(b);
            props.setNoOccursSubscripts(b.size());
        }
        props.setLength(leng);
        boolean b = SymbolUtil.setCheckUseNativeJavaTypes(props, alwaysCobolBytes);
        if (props.getRedefines() == null) {
            props.setOffset(offset.peek());
            props.setUnAdjustedOffset(unAdjustedOffset.peek());
            if (props.isOccurs() && props.getMaxOccursInt() > 1) {
                leng *= (props.getMaxOccursInt());
            }
            if (b) {
                unAdjustedOffset.push(unAdjustedOffset.pop() + leng);
                leng = 0;
            } else {
                offset.push(offset.pop() + leng);
                unAdjustedOffset.push(unAdjustedOffset.pop() + leng);
            }
        } else {
            props.setOffset(props.getRedefines().getOffset());
            props.setUnAdjustedOffset(props.getRedefines().getUnAdjustedOffset());
            leng = Math.max(leng, props.getRedefines().getLength());
        }


        SymbolTable.visit(props.getChildren(), this);

        return;
    }

    @Override
    public void visitParagraph(SymbolProperties props) {
    }

    @Override
    public void visitSection(SymbolProperties props) {
    }

    @Override
    public void visitFile(SymbolProperties props) {
        alwaysCobolBytes = true;
        SymbolTable.visit(props.getChildren(), this);
        alwaysCobolBytes = false;
        if (props.hasChildren()) {
            for (SymbolProperties child : props.getChildren()) {
                props.setLength(Math.max(props.getLength(), child.getLength()));
            }
            if (props.getAdjustedLength() < props.getLength()) {
                props.setAdjustedLength(props.getLength());
            }
        }
        //props.setAdjustedLength(0);
    }
}
