package com.res.java.translation.symbol;

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
import java.util.Iterator;
import java.util.ListIterator;

import com.res.cobol.Main;
import com.res.common.RESConfig;
import com.res.java.lib.Constants;
import com.res.java.lib.FieldFormat;
import com.res.java.lib.exceptions.InvalidCobolFormatException;
import com.res.java.translation.engine.TranUtil;
import com.res.java.translation.symbol.SymbolProperties.CobolSymbol;
import com.res.java.util.ClassFile;
import com.res.java.util.NameUtil;

public class SymbolUtil {

    private static SymbolUtil symbolUtil = null;

    private SymbolUtil() {
    }

    public static SymbolUtil getInstance() {
        if (symbolUtil == null) {
            symbolUtil = new SymbolUtil();
        }
        return symbolUtil;
    }

    public static void clear() {
        symbolUtil = null;
    }

    public void doClassBegin(SymbolProperties props, String name, boolean programScope) {
        if (programScope) {
            props.setIsFormat(false);
            ClassFile.doProgramScope(props, true);
            if (name == null || name.length() <= 0) {
                name = props.getJavaName2();
            }
            ClassFile.println("");
            ClassFile.println("import com.res.java.lib.*;");
            ClassFile.println("import java.math.BigDecimal;");
            ClassFile.println("import java.beans.*;", true);
            ClassFile.println("import com.res.java.lib.*;", true);
            ClassFile.println("");
            ClassFile.println("@SuppressWarnings(\"unused\")");
            if (props.isIndexedFileRecord()) {
                ClassFile.println("public class " + name + " extends CobolBean implements CobolFileBean {");
            } else {
                ClassFile.println("public class " + name + " extends CobolBean {");
            }
            ClassFile.println("public class " + name + "BeanInfo extends RESBeanInfo {", true);
            ClassFile.tab();
        } else {
            ClassFile.println("public class " + name + " extends CobolBean {");
            ClassFile.tab();
        }
        if (RESConfig.getInstance().isUsePointers()) {
            ClassFile.println("public BytesField " + props.getJavaName1() + "Fld_ = new BytesField(this,"
                    + String.valueOf(props.getUnAdjustedOffset()) + ','
                    + String.valueOf(props.getLength()) + ");");
        }

    }

    public void doClassEnding(SymbolProperties props, boolean programScope) {
        String name1 = props.getJavaName1();
        String name2 = props.getJavaName2();

        int adjLen = props.getAdjustedLength();

        if (props.getType() != SymbolConstants.PROGRAM) {

            printInitialize(props);

            printConstructors(props, adjLen);

            printStringAccessors(props);

            printGroupAccessors(props, true, getGroupOccursIdx(props));
            if (props.isIndexedFileRecord()) {
                TranUtil.get().printSetSQLProperties(props);
                TranUtil.get().printgetSQLResults(props);
                TranUtil.get().printSetStartSQLProperties(props);
            }
        }

        if (props.getType() != SymbolConstants.PROGRAM) {

            ClassFile.backTab();
            if (programScope) {
                ClassFile.endProgramScope();
            } else {
                ClassFile.doMethodScope("}");
            }

            printClassAccessors(props, name1, name2);

            printClass88s(props);

        }

    }
    private static String modifier0 = "";
    private static String modifier1 = "";
    private static String getSubscripts = "";
    private static String modifier2 = "";
    private static String modifier3 = "";
    private static String modifier4 = "";
    private static String setSubscripts = "";

    private void printGroupAccessors(SymbolProperties props,
            boolean isTopLevel, int idx) {

        if (!props.getRef() && !props.getMod()) {
            return;
        }
        int relOff = -1;
        if (isTopLevel) {
            ClassFile.doMethodScope("public byte[] getBytes(" + getGroupOccursSignature(props)
                    + ") {");
            idx = 0;
            if (RESConfig.getInstance().getOptimizeAlgorithm() == 2 && props.isUsedNativeJavaTypes()) {
                relOff = 0;
            }
            //else
            // idx = -2;
        } else {
            ClassFile.doMethodScope("public byte[] get"
                    + props.getJavaName2()
                    + "(" + getGroupOccursSignature(props)
                    + ") {");
            if (RESConfig.getInstance().getOptimizeAlgorithm() == 2 && props.isUsedNativeJavaTypes()) {
                relOff = props.getUnAdjustedOffset();
            }
        }
        ClassFile.tab();

        if (isSingleTopLevel(props) && !props.isUsedNativeJavaTypes()) {
            printGroupGetClassString(props);
        } else if (props.hasChildren()) {
            ClassFile.println("CobolBytes " + SymbolTable.TOSTRING_REF_STRING_
                    + "=new CobolBytes(" + String.valueOf(props.getLength()) + ",true);");
            for (Iterator<SymbolProperties> ite = props.getChildren().iterator(); ite.hasNext();) {
                SymbolProperties child = ite.next();
                if (child.getRedefines() != null || child.getLevelNumber() == 88 || child.getLevelNumber() == 66 || child.getLength()==0) {
                    continue;
                }
                child.setIndexesWorkSpace(props.getIndexesWorkSpace());
                printGroupGetBytesOccursLoop(child, idx, relOff);
            }
            ClassFile.println("return "
                    + SymbolTable.TOSTRING_REF_STRING_
                    + ".getBytes();");
        } else {
            ClassFile.println("return new b[0];");
        }
        ClassFile.backTab();
        ClassFile.endMethodScope();
        if (isTopLevel) {
            ClassFile.doMethodScope("public void valueOf(byte[] val"
                    + (((getSubscripts = getGroupOccursSignature(props)).length() > 0) ? ("," + getSubscripts) : "")
                    + ") {");
            /*
            if(RESConfig.getInstance().getOptimizeAlgorithm()==2&&props.isUsedNativeJavaTypes())
            idx = -1;
            else idx = -2;
             */
        } else {
            ClassFile.doMethodScope("public void set"
                    + props.getJavaName2()
                    + "(byte[] val" + (((getSubscripts = getGroupOccursSignature(props)).length() > 0) ? ("," + getSubscripts) : "")
                    + ") {");
            relOff = props.getUnAdjustedOffset();
        }
        ClassFile.tab();
        if (isSingleTopLevel(props) && !props.isUsedNativeJavaTypes()) {
            printGroupSetClassString(props);
        } else if (props.hasChildren()) {
            ClassFile.println("CobolBytes "
                    + SymbolTable.TOSTRING_REF_STRING_
                    + "=new CobolBytes(val,Math.max(val.length," + String.valueOf(props.getLength())
                    + "));");
            for (Iterator<SymbolProperties> ite = props.getChildren().iterator(); ite.hasNext();) {
                SymbolProperties child = ite.next();
                if (child.getRedefines() != null || child.getLevelNumber() == 88 || child.getLevelNumber() == 66||child.getLength()==0) {
                    continue;
                }
                child.setIndexesWorkSpace(props.getIndexesWorkSpace());
                printGroupSetBytesOccursLoop(child, "val", idx, relOff);
            }
        } else {
            ClassFile.println("return;");
        }
        ClassFile.backTab();
        ClassFile.endMethodScope();
    }

    private boolean isSingleTopLevel(SymbolProperties props) {
        if (!props.isUsedNativeJavaTypes()) {
            return true;
        }
        while (props.hasChildren()
                && !(props = props.getChildren().get(0)).isUsedNativeJavaTypes()
                && props.getIsFiller()) {
            continue;
        }
        return props.getChildren() != null && props.getChildren().size() == 1 && !props.getChildren().get(0).isUsedNativeJavaTypes();
    }

    private void printGroupGetClassString(SymbolProperties props) {
        props.setIndexesWorkSpace(null);
        ClassFile.println("return "
                + getGetterString(props, -2, -1) + ";");
    }

    private void printGroupSetClassString(SymbolProperties props) {
        props.setIndexesWorkSpace(null);
        ClassFile.println(getSetterString(props, -2, -1) + ";");
    }
    private boolean forceDisplayAccessor = false;
    //Of the form...
    //super.setDisplayLong(0,13,bytes_.getDisplayLong(0,13,false,false,false),false,false,false);

    private void printGroupSetCobolBytesString(SymbolProperties props, int modifiedIdx, int relativeUnadjustedOffset) {
        int i = 0;
        String s;
        forceDisplayString = forceDisplayAccessor = true;
        s = getGetterString(props, modifiedIdx, (RESConfig.getInstance().getOptimizeAlgorithm() == 1)
                ? -1 : relativeUnadjustedOffset);
        forceDisplayString = false;
        ClassFile.println(getSetterString(props, modifiedIdx, -1).replace(",val", "," + SymbolTable.TOSTRING_REF_STRING_
                + s.substring(((i = s.indexOf('.')) < 0) ? 0 : i).replace(",val", ",")) + ";");
        forceDisplayAccessor = false;
    }

    //Of the form...
    //bytes_.setDisplayLong(0,13,super.getDisplayLong(0,13,false,false,false),false,false,false);
    private void printGroupGetCobolBytesString(SymbolProperties props, int modifiedIdx, int relativeUnadjustedOffset) {
        int i = 0;
        String s;
        forceDisplayString = forceDisplayAccessor = true;
        s = getSetterString(props, modifiedIdx, relativeUnadjustedOffset);
        forceDisplayString = false;
        ClassFile.println(SymbolTable.TOSTRING_REF_STRING_ + s.substring(((i = s.indexOf('.')) < 0) ? 0 : i).replace(",val", ","
                + getGetterString(props, modifiedIdx, -1)) + ";");
        forceDisplayAccessor = false;
    }

    // Of the form...
    // setName(bytes_.getDisplayLong(0,13,false,false,false));
    String getGroupSetterString(SymbolProperties props, String value, int modifiedIdx, int relativeUnadjustedOffset) {
        int i = 0;
        forceDisplayString = forceDisplayAccessor = true;
        String s = getSetterString(props, modifiedIdx, relativeUnadjustedOffset);
        forceDisplayString = false;
        s = SymbolTable.TOSTRING_REF_STRING_ + s.substring(((i = s.indexOf('.')) < 0) ? 0 : i).replace(",val", "," + value);
        forceDisplayAccessor = false;
        return s;
    }

    //Of the form...
    //bytes_.setDisplayLong(0,13,getName(),false,false,false);
    String getGroupGetterString(SymbolProperties props, int modifiedIdx, int relativeUnadjustedOffset) {
        int i = 0;
        forceDisplayString = forceDisplayAccessor = true;
        String s = getGetterString(props, modifiedIdx, relativeUnadjustedOffset);
        forceDisplayString = false;
        s = SymbolTable.TOSTRING_REF_STRING_ + s.substring(((i = s.indexOf('.')) < 0) ? 0 : i);
        forceDisplayAccessor = false;
        return s;
    }

    //Of the form...
    //for(int idx_=0;idx_<10240;++idx_) {
    //   bytes_.setDisplayLong(0+13*idx_,13,getName(idx_+1),false,false,false);
    //   bytes_.setDisplayLong(13*idx_,13,getName2(idx_+1),false,false,false);
    //   bytes_.setDisplayLong(26*idx_,16,getName3(idx_+1),false,false,false);
    //}
    public void printGroupGetBytesOccursLoop(SymbolProperties props, int modifiedIdx, int relOff) {
        if (props.isOccurs()) {
            //Of form... for(int idx1=0;idx1<10240;++idx_) {
            ClassFile.println("for(int idx" + String.valueOf(props.getNoOccursSubscripts())
                    + "=" + String.valueOf(props.getMaxOccursInt())
                    + ";idx" + String.valueOf(props.getNoOccursSubscripts())
                    + ">=1"
                    + ";--"
                    + "idx" + String.valueOf(props.getNoOccursSubscripts())
                    + ") {");
            ClassFile.tab();
            if (props.getIndexesWorkSpace() == null) {
                props.setIndexesWorkSpace(new ArrayList<String>());
            }
            props.getIndexesWorkSpace().add("idx" + String.valueOf(props.getNoOccursSubscripts()));
        }

        if ((!props.getRef() && !props.getMod()) || (props.getIsFiller() && props.isGroupData())) {
            if (props.hasChildren()) {
                for (SymbolProperties child : props.getChildren()) {
                    child.setIndexesWorkSpace(props.getIndexesWorkSpace());
                    //Of the form... bytes_.setDisplayLong(0+13*--idx1,13,getName(++idx1),false,false,false);
                    printGroupGetBytesOccursLoop(child, modifiedIdx, relOff);
                }
            }

        } else if (!props.isUsedNativeJavaTypes()) {
            printGroupGetCobolBytesString(props, modifiedIdx, relOff);
        } else if (props.isOccurs()) {
            if (props.isGroupData() && props.getIsFiller()) {
                for (SymbolProperties child : props.getChildren()) {
                    child.setIndexesWorkSpace(props.getIndexesWorkSpace());
                    if (child.getIsFiller() && child.isElementData()) {
                        printGroupGetBytesString(child, modifiedIdx, relOff);
                    } else //Recursion
                    {
                        printGroupGetBytesOccursLoop(child, modifiedIdx, relOff);
                    }
                }

            } else {
                //Of the form... bytes_.setDisplayLong(0+13*idx_,13,getName(idx_+1),false,false,false);
                printGroupGetBytesString(props, modifiedIdx, relOff);
            }
        } //Of the form... bytes_.setDisplayLong(0+13*idx_,13,getName(idx_+1),false,false,false);
        else {
            printGroupGetBytesString(props, modifiedIdx, relOff);
        }
        if (props.isOccurs()) {
            ClassFile.backTab();
            ClassFile.println("}");
            if (props.getIndexesWorkSpace() != null) {
                props.getIndexesWorkSpace().remove(props.getIndexesWorkSpace().size() - 1);
            }
        }
    }

    //Of the form...
    //bytes_.setDisplayLong(0+13*--idx1,13,getName(++idx1),false,false,false);
    void printGroupGetBytesString(SymbolProperties props, int modifiedIdx, int relOff) {
        forceDisplayString = forceDisplayAccessor = true;
        ClassFile.println(getGroupSetterString(props, SymbolUtil.getLocalName(NameUtil.getJavaName(props, false, props.isADisplayNumber())), modifiedIdx, relOff) + ";");
        forceDisplayString = forceDisplayAccessor = false;
    }
    //Of the form...
    //for(int idx1=0;idx1<10240;++idx1) {
    // setName1(bytes_.getDisplayLong(0+13*--idx1,13,false,false,false),++idx1);
    // setName2(bytes_.getDisplayLong(0+13*--idx1+13,13,false,false,false),++idx1);
    // setName3(bytes_.toString(0+13*--idx1+26,19),++idx1);
    //}

    void printGroupSetBytesOccursLoop(SymbolProperties props, String value, int modifiedIdx, int relOff) {
        if (props.isOccurs()) {
            ClassFile.println("for(int idx" + String.valueOf(props.getNoOccursSubscripts())
                    + "=" + String.valueOf(props.getMaxOccursInt())
                    + ";idx" + String.valueOf(props.getNoOccursSubscripts())
                    + ">=1"
                    + ";--"
                    + "idx" + String.valueOf(props.getNoOccursSubscripts())
                    + ") {");
            ClassFile.tab();
            if (props.getIndexesWorkSpace() == null) {
                props.setIndexesWorkSpace(new ArrayList<String>());
            }
            props.getIndexesWorkSpace().add("idx" + String.valueOf(props.getNoOccursSubscripts()));
        }
        if (!props.getRef() && !props.getMod() || (props.getIsFiller() && props.isGroupData())) {
            if (props.hasChildren()) {
                for (SymbolProperties child : props.getChildren()) {
                    child.setIndexesWorkSpace(props.getIndexesWorkSpace());
                    printGroupSetBytesOccursLoop(child, value, modifiedIdx, relOff);
                }
            }
        } else if (!props.isUsedNativeJavaTypes()) {
            printGroupSetCobolBytesString(props, modifiedIdx, relOff);
        } else {
            if (props.isOccurs()) {
                if (props.getPictureString() == null && props.getIsFiller()) {
                    for (SymbolProperties child : props.getChildren()) {
                        child.setIndexesWorkSpace(props.getIndexesWorkSpace());
                        printGroupSetBytesOccursLoop(child, value, modifiedIdx, relOff);
                    }
                } else {
                    printGroupSetBytesString(props, value, modifiedIdx, relOff);
                }
            } else {
                printGroupSetBytesString(props, value, modifiedIdx, relOff);
            }
        }
        if (props.isOccurs()) {
            ClassFile.backTab();
            ClassFile.println("}");
            if (props.getIndexesWorkSpace() != null) {
                props.getIndexesWorkSpace().remove((int) (props.getIndexesWorkSpace().size() - 1));
            }
        }
    }

    //Of the form...
    //setName1(bytes_.getDisplayLong(0,13,false,false,false));
    public void printGroupSetBytesString(SymbolProperties props, String value, int modifiedIdx, int relOff) {
        forceDisplayString = forceDisplayAccessor = true;
        ClassFile.println(getLocalName(NameUtil.getJavaName(props, true, props.isADisplayNumber())).replace("%0", getGroupGetterString(props, modifiedIdx, relOff)) + ";");
        forceDisplayString = forceDisplayAccessor = false;
    }

    static String getLocalName(String fullName) {
        int i = ((i = fullName.indexOf('(')) < 0) ? fullName.length() : ++i;
        return fullName.substring(fullName.substring(0, i).lastIndexOf('.') + 1);
    }

    public static boolean setCheckUseNativeJavaTypes(SymbolProperties props, boolean forceCobolBytes) {
        if (props.isFloatingPoint()) {
            props.setUsedNativeJavaTypes(true);
            return true;
        }
        props.setUsedNativeJavaTypes(false);
        if (props.getIsFiller() || props.getIsSuppressed() || props.isConsolidateParagraph() || props.isForceCobolBytes()) {
            return false;
        }
        if (RESConfig.getInstance().getOptimizeAlgorithm() == 0 || forceCobolBytes) {
            return false;
        }
        SymbolProperties props2 = props;
        if (RESConfig.getInstance().getOptimizeAlgorithm() == 1) {
            boolean ret = true;
            while ((props2 = props2.getParent()) != null) {
                ret &= (props2.isUsedNativeJavaTypes() || props2.isProgram());
            }
            if (!ret) {
                return false;
            }
        }

        props2 = props;
        if (!(props2.getRedefines() != null
                && props2.getRedefines().getDataUsage() == Constants.INDEX
                && props2.getRedefines().is01Group())) {
            while (props2 != null) {
                if (props2.getRedefines() != null || (props2.getRedefinedBy() != null && props2.getRedefinedBy().size() > 0)) {
                    return false;
                }
                if ((props2.getLevelNumber() == 1 || props2.getLevelNumber() == 77) && props2.isAChildHasRedefines()) {
                    return false;
                }

                props2 = props2.getParent();
            }
        }
        props.setUsedNativeJavaTypes(true);
        return true;
    }

    public void printJavaTypesAccessors(SymbolProperties props) {

        if (props.getIsSuppressed()) {
            return;
        }
        if (!props.isUsedNativeJavaTypes() && !props.isFloatingPoint()) {
            return;
        }
        if (props.getIsFiller()) {
            return;
        }
        if (!(props.getRef() || props.getMod())) {
            return;
        }

        CobolSymbol sym = (SymbolProperties.CobolSymbol) props.getJavaType();
        String name1 = (String) props.getJavaName1();
        String name2 = (String) props.getJavaName2();

        String s = "";
        modifier0 = "";
        modifier1 = "";
        getSubscripts = "";
        modifier2 = "";
        modifier3 = "";
        modifier4 = "";
        setSubscripts = "";

        if (props.isGroupData() || sym.getType() == Constants.GROUP) {
            props.setIsFormat(false);
            if (RESConfig.getInstance().getOptimizeAlgorithm() == 2) {
                printGroupAccessors(props, false, getGroupOccursIdx(props));
            } else {
                props.setUsedNativeJavaTypes(false);
                printCobolByteAccessors(props);
            }
            return;
        }

        if (props.isOccurs() || props.isAParentInOccurs()) {
            ArrayList<SymbolProperties> a = props.getOccursParents();
            int i = 0;
            //offsetCalc=String.valueOf(a.get(0).getOffset());
            if (a != null && a.size() > 0) {
                for (Iterator<SymbolProperties> ite = a.iterator(); ite.hasNext();) {
                    SymbolProperties curr = ite.next();
                    ++i;
                    modifier0 += "[]";
                    modifier1 += "[" + String.valueOf(curr.getMaxOccursInt()) + "]";
                    getSubscripts += "int idx" + String.valueOf(i) + ((ite.hasNext()) ? "," : "");
                    setSubscripts += ",int idx" + String.valueOf(i);
                    modifier2 += "[--idx" + String.valueOf(i) + "]";
                    modifier3 += "idx" + String.valueOf(i) + ((ite.hasNext()) ? "," : "");
                    modifier4 += ",idx" + String.valueOf(i);
                }

            }
        }

        if (FieldFormat.isPlainString(props.getPictureString())) {
            props.setIsFormat(false);
        }

        if (props.getIsFormat()) {
            printFieldFormat(props);
        }
        boolean isADisplayNumber = false;
        switch (sym.getType()) {
            case Constants.CHAR:
                s = "private " + SymbolConstants.get(sym.getType()) + modifier0 + " " + name1;
                if (modifier1.length() > 0) {
                    s += "= new " + SymbolConstants.get(sym.getType()) + modifier1 + ";";
                }
                s += ";";
                break;
            case Constants.BIGDECIMAL:
                if (props.getDataUsage() == Constants.DISPLAY) {
                    isADisplayNumber = true;
                    if (modifier1.length() <= 0) {
                        modifier1 = "()";
                    }
                    s = "private String" + modifier0 + " " + name1
                            + "= new String" + modifier1 + ";";
                } else {
                    s = "private " + SymbolConstants.get(sym.getType()) + modifier0 + " " + name1 + " = ";
                    if (modifier1.length() <= 0) {
                        s += SymbolConstants.get(sym.getType()) + ".ZERO;";
                    } else {
                        s += "new " + SymbolConstants.get(sym.getType()) + modifier1 + ";";
                    }
                }
                break;
            case Constants.BYTE:
            case Constants.SHORT:
            case Constants.INTEGER:
            case Constants.FLOAT:
            case Constants.DOUBLE:
            case Constants.LONG:
                if (props.getDataUsage() != Constants.DISPLAY) {
                    s = "private " + SymbolConstants.get(sym.getType()) + modifier0 + " " + name1;
                    if (modifier1.length() > 0) {
                        s += "= new " + SymbolConstants.get(sym.getType()) + modifier1 + ";";
                    }
                    s += ";";
                    break;
                }
                isADisplayNumber = true;
            case Constants.STRING:
                if (modifier1.length() <= 0) {
                    modifier1 = "()";
                }
                s = "private String" + modifier0 + " " + name1
                        + "= new String" + modifier1 + ";";
                break;
            case Constants.GROUP:
            //TODO for group moves
            default:
                if (modifier1.length() <= 0) {
                    modifier1 = "()";
                }
                s = "private " + SymbolConstants.get(sym.getType()) + modifier0 + " " + name1
                        + "= new " + SymbolConstants.get(sym.getType()) + modifier1 + ";";
        }

        if (s.length() > 0) {
            ClassFile.println(s);
        }
        if (isADisplayNumber) {
            printDisplayNumberAccessor(props);
        } else {
            ClassFile.doMethodScope("public " + SymbolConstants.get(sym.getType())
                    + " get" + props.getJavaName2() + "(" + getSubscripts + ") {");
            ClassFile.tab();
            ClassFile.println("return " + props.getJavaName1() + modifier2 + ";");
            ClassFile.backTab();
            ClassFile.endMethodScope();
            ClassFile.doMethodScope("public "
                    + " void set" + name2
                    + "("
                    + SymbolConstants.get(sym.getType())
                    + " val" + setSubscripts + ") {");
            ClassFile.tab();
            s = getJavaTypeSetterString(props);
            ClassFile.println(name1 + modifier2 + "=" + s.replace("%0", "val") + ";");
            ClassFile.backTab();
            ClassFile.endMethodScope();
        }
        if (props.isAsBytesAccessor()) {
            ClassFile.doMethodScope("public " + SymbolConstants.get(sym.getType())
                    + " get" + name2 + "AsBytes(" + getSubscripts + ") {");
            ClassFile.tab();
            ClassFile.println("CobolBytes " + SymbolTable.TOSTRING_REF_STRING_
                    + "=new CobolBytes(" + String.valueOf(props.getLength()) + ");");
            printGroupGetBytesOccursLoop(props, -1, props.getUnAdjustedOffset());
            ClassFile.println("return " + SymbolTable.TOSTRING_REF_STRING_ + ".getBytes();");
            ClassFile.backTab();
            ClassFile.endMethodScope();
        }

        printJavaTypeClassConditionTail(props);
        return;
    }

    private void printDisplayNumberAccessor(SymbolProperties props) {
        int len = props.getAdjustedLength();//+Math.abs(props.getJavaType().maxScalingLength);
        String s;
        ClassFile.doMethodScope("public " + SymbolConstants.get(props.getJavaType().getType())
                + " get" + props.getJavaName2() + "(" + getSubscripts + ") {");
        ClassFile.tab();
        switch (props.getJavaType().getType()) {
            case Constants.BIGDECIMAL:
                ClassFile.println("return super.normalizeDisplay(" + props.getJavaName1() + modifier2 + ","
                        + len + ","
                        + +props.getJavaType().getMaxFractionLength() + "," + props.getJavaType().isIsSigned() + ");");
                break;
            case Constants.BYTE:
                ClassFile.println("return (byte) super.normalizeDisplay(" + props.getJavaName1() + modifier2 + ","
                        + len + ","
                        + props.getJavaType().isIsSigned() + ");");
                break;
            case Constants.SHORT:
                ClassFile.println("return (short) super.normalizeDisplay("
                        + props.getJavaName1() + modifier2 + ","
                        + len + ","
                        + props.getJavaType().isIsSigned() + ");");
                break;
            case Constants.INTEGER:
                ClassFile.println("return (int) super.normalizeDisplay("
                        + props.getJavaName1() + modifier2 + ","
                        + len + ","
                        + props.getJavaType().isIsSigned() + ");");
                break;
            case Constants.FLOAT:
            case Constants.DOUBLE:
                reportError("Invalid JavaType for data-name: Float or Double");
            case Constants.LONG:
                ClassFile.println("return super.normalizeDisplay("
                        + props.getJavaName1() + modifier2 + ","
                        + len + ","
                        + props.getJavaType().isIsSigned() + ");");
                break;
        }
        ClassFile.backTab();
        ClassFile.endMethodScope();
        ClassFile.doMethodScope("public String get" + props.getJavaName2() + "AsString(" + getSubscripts + ") {");
        ClassFile.tab();
            switch (props.getJavaType().getType()) {
            case Constants.BYTE:
            case Constants.SHORT:
            case Constants.INTEGER:
            case Constants.LONG:
                s = "super.asDottedSignedString("
                        + props.getJavaName1() + modifier2  +","+ len
                         + ((props.getJavaType().getMaxScalingLength()==0)?
                         (""):
                         (","+String.valueOf(props.getJavaType().getMaxFractionLength()))+","+
                          String.valueOf(props.getJavaType().getMaxScalingLength()))
                        + "," + String.valueOf(props.isSigned()) + "," + props.isSignLeading() + "," + props.isSignSeparate() + ")";
                break;
            case Constants.BIGDECIMAL:
                s = "super.asDottedSignedString("
                        + props.getJavaName1() + modifier2  + ","+ len + ","
                        + ((props.getJavaType().getMaxScalingLength()==0)?
                         (String.valueOf(props.getJavaType().getMaxFractionLength())):
                         (String.valueOf(props.getJavaType().getMaxFractionLength()))+","+
                          String.valueOf(props.getJavaType().getMaxScalingLength()))+ ","
                        + String.valueOf(props.getJavaType().isIsSigned()) + "," + props.isSignLeading() + "," + props.isSignSeparate() + ")";
                break;
            default:
                s = "";
        }
        ClassFile.println("return "+s+';');
        ClassFile.backTab();
        ClassFile.endMethodScope();

        ClassFile.doMethodScope("public " + " void set" + props.getJavaName2() + "("
                + SymbolConstants.get(props.getJavaType().getType()) + " val" + setSubscripts + ") {");
        ClassFile.tab();
        switch (props.getJavaType().getType()) {
            case Constants.BYTE:
            case Constants.SHORT:
            case Constants.INTEGER:
            case Constants.LONG:
                s = "super.toDisplayString(%0," + len
                        + "," + String.valueOf(props.isSigned()) + "," + props.isSignLeading() + "," + props.isSignSeparate() + ")";
                break;
            case Constants.BIGDECIMAL:
                s = "super.toDisplayString(%0," + len + ","
                        + String.valueOf(props.getJavaType().getMaxFractionLength())
                        //               -props.getJavaType().maxScalingLength)
                        + ","+ String.valueOf(props.getJavaType().isIsSigned()) +
                        "," + props.isSignLeading() + "," + props.isSignSeparate() + ")";
                break;
            default:
                s = "";
        }
        ClassFile.println("this." + props.getJavaName1() + modifier2 + "=" + s.replace("%0", "val") + ";");
        ClassFile.backTab();
        ClassFile.endMethodScope();

        ClassFile.doMethodScope("public " + " void set" + props.getJavaName2() + "("
                + "String val" + setSubscripts + ") {");
        ClassFile.tab();
        //s="super.normalizeString(%0,"+String.valueOf(props.getAdjustedLength())+","+
        //String.valueOf(props.getJavaType().getMaxFractionLength())+","+
        //String.valueOf(props.getJavaType().isIsSigned())+")";
        //ClassFile.println("this."+props.getJavaName1()+modifier2+"="+s.replace("%0","val")+";");
        if (props.getJavaType().getType() == Constants.CHAR) {
            ClassFile.println("this." + props.getJavaName1() + modifier2 + "=super.normalizeString(val,"
                    + (len+1) + ",true,true,false).charAt(0);");
        } else {
            ClassFile.println("this." + props.getJavaName1() + modifier2 + "=super.normalizeString(val,"
                    + (len+1) + ",true,true,false);");
        }
        ClassFile.backTab();
        ClassFile.endMethodScope();
    }

    private void printDisplayNumberAccessor2(SymbolProperties props) {
        String s;
        //Now getters
        ClassFile.doMethodScope("public " + SymbolConstants.get(props.getJavaType().getType())
                + " get" + props.getJavaName2() + "(" + getSubscripts + ") {");
        ClassFile.tab();
        forceDisplayAccessor = false;
        s = getGetterString(props, -1, 0);
        //props.setFillerGetter(s);
        s = "return " + s + ";";
        ClassFile.println(s);
        ClassFile.backTab();
        ClassFile.endMethodScope();
        ClassFile.doMethodScope("public String get" + props.getJavaName2() + "AsString(" + getSubscripts + ") {");
        ClassFile.tab();
        forceDisplayAccessor = true;
        s = getGetterString(props, -1, 0);
        String dottedSignedParms =
                "," + props.getJavaType().isIsSigned() + "," + props.getJavaType().isIsSignLeading() + ","
                + props.getJavaType().isIsSignSeparate();
        s = "return " + s + ";";
        ClassFile.println(s);
        ClassFile.backTab();
        ClassFile.endMethodScope();

        //Now setters
        ClassFile.doMethodScope("public " + " void set" + props.getJavaName2()
                + "(" + SymbolConstants.get(props.getJavaType().getType()) + " val"
                + setSubscripts + ") {");
        ClassFile.tab();
        forceDisplayAccessor = false;
        s = getSetterString(props, -1, 0);
        s += ";";
        ClassFile.println(s);
        ClassFile.backTab();
        ClassFile.endMethodScope();
        ClassFile.doMethodScope("public "
                + " void set" + props.getJavaName2() + "(String val" + setSubscripts + ") {");
        ClassFile.tab();
        forceDisplayAccessor = true;
        s = getSetterString(props, -1, 0).replace(props.getJavaName1() + "=", "fromDottedSignedString(val,"
                + props.getAdjustedLength() + "," + props.getJavaType().getMaxFractionLength()
                + dottedSignedParms + ",");
        s += ";";
        ClassFile.println(s);
        ClassFile.backTab();
        ClassFile.endMethodScope();
        forceDisplayAccessor = false;
    }

    private void printFieldFormat(SymbolProperties props) {
        ClassFile.println("public "
                + ((SymbolTable.getScope().isCurrentProgramTopLevel() || ClassFile.current.isData()) ? "static " : "")
                + "final FieldFormat " + props.getJavaName1() + "Fmt_ = new FieldFormat(\""
                + props.getJavaName1() + "\",\"" + props.getJavaType().getPic() + "\""
                + (props.isBlankWhenZero() ? ",true" : "")
                + ");");
    }

    private void printJavaTypeClassConditionTail(SymbolProperties props) {
        if (props.getJavaType().getUsage() == Constants.DISPLAY || props.getJavaType().getUsage() == Constants.PACKED_DECIMAL) {
            String isNumeric = "";
            String isAlphabetic = "";
            String isAlphabeticLower = "";
            String isAlphabeticUpper = "";

            switch (props.getJavaType().getType()) {
                case Constants.STRING:
                    isNumeric = "return " + props.getJavaName1() + ".matches(\"[0-9+-]*\");";
                    isAlphabetic = "return " + props.getJavaName1() + ".matches(\"[a-zA-Z ]*\");";
                    isAlphabeticLower = "return " + props.getJavaName1() + ".matches(\"[a-z ]*\");";
                    isAlphabeticUpper = "return " + props.getJavaName1() + ".matches(\"[A-Z ]*\");";
                    break;
                case Constants.BIGDECIMAL:
                case Constants.SHORT:
                case Constants.INTEGER:
                case Constants.LONG:
                case Constants.FLOAT:
                case Constants.DOUBLE:
                case Constants.CHAR:
                case Constants.BYTE:
                    isNumeric = "return true;";
                    isAlphabetic = "return false";
                    isAlphabeticLower = "return false";
                    isAlphabeticUpper = "return false";
                    break;
                case Constants.GROUP:
                    isNumeric = "return " + "new String(get" + props.getJavaName2() + "())" + ".matches(\"[0-9+-]*\");";
                    isAlphabetic = "return " + "new String(get" + props.getJavaName2() + "())" + ".matches(\"[a-zA-Z ]*\");";
                    isAlphabeticLower = "return " + "new String(get" + props.getJavaName2() + "())" + ".matches(\"[a-z ]*\");";
                    isAlphabeticUpper = "return " + "new String(get" + props.getJavaName2() + "())" + ".matches(\"[A-Z ]*\");";
                    break;
                default:
                    isNumeric = "return false;";
                    isAlphabetic = "return false";
                    isAlphabeticLower = "return false";
                    isAlphabeticUpper = "return false";
                    break;
            }

            if (props.isNumericTested()) {
                ClassFile.doMethodScope("public boolean is" + props.getJavaName2() + "Numeric(" + getSubscripts + ") {");
                ClassFile.tab();
                ClassFile.println(isNumeric);
                ClassFile.backTab();
                ClassFile.endMethodScope();
            }
            if (props.isAlphabeticTested()) {
                ClassFile.doMethodScope("public boolean is" + props.getJavaName2() + "Alphabetic(" + getSubscripts
                        + ") {");
                ClassFile.tab();
                ClassFile.println(isAlphabetic);
                ClassFile.backTab();
                ClassFile.endMethodScope();
            }
            if (props.isAlphabeticLowerTested()) {
                ClassFile.doMethodScope("public boolean is" + props.getJavaName2() + "AlphabeticLower(" + getSubscripts
                        + ") {");
                ClassFile.tab();
                ClassFile.println(isAlphabeticLower);
                ClassFile.backTab();
                ClassFile.endMethodScope();
            }
            if (props.isAlphabeticUpperTested()) {
                ClassFile.doMethodScope("public boolean is" + props.getJavaName2() + "AlphabeticUpper(" + getSubscripts
                        + ") {");
                ClassFile.tab();
                ClassFile.println(isAlphabeticUpper);
                ClassFile.backTab();
                ClassFile.endMethodScope();
            }
        }
    }
    private String refString = null;
    private String offsetCalc = null;

    public void printCobolByteAccessors(SymbolProperties props) {

        if (props.getIsSuppressed()) {
            return;
        }
        if (props.getIsFiller()) {
            return;
        }
        if (props.isUsedNativeJavaTypes() || props.isFloatingPoint()) {
            printJavaTypesAccessors(props);
            return;
        }
        if (!(props.getRef() || props.getMod() || props.getIsFiller())) {
            return;
        }
        if ((props.getLength() == 0 || props.getJavaType() == null)) {
            return;
        }


        String s;
        preparePrintCobolBytesAccessors(props);
        //String name1=(String) props.getJavaName1();
        String name2 = (String) props.getJavaName2();
        boolean isADisplayNumber = props.isANumber() && props.getDataUsage() == Constants.DISPLAY;
        if (props.isGroupData()) {
            props.setIsFormat(false);
            if (!props.getIsFiller()) {
                ClassFile.doMethodScope("public byte[]"
                        + " get" + name2 + "(" + getSubscripts + ") {");
                ClassFile.tab();
                ClassFile.println("return " + getGetterString(props, -1, 0) + ";");
                ClassFile.backTab();
                ClassFile.endMethodScope();

                ClassFile.doMethodScope("public "
                        + " void set" + name2
                        + "(byte[] val"
                        + setSubscripts + ") {");
                ClassFile.tab();
                ClassFile.println(getSetterString(props, -1, 0) + ";");
                ClassFile.backTab();
                ClassFile.endMethodScope();

                props.setIsFormat(Boolean.FALSE);
            }
            return;
        } else {

            if (FieldFormat.isPlainString(props.getPictureString())) {
                props.setIsFormat(false);
            }

            if (props.getIsFormat()) {
                printFieldFormat(props);
            }
            if (isADisplayNumber) {
                printDisplayNumberAccessor2(props);
            } else {
                //Now getters
                ClassFile.doMethodScope("public " + SymbolConstants.get(props.getJavaType().getType())
                        + " get" + name2 + "(" + getSubscripts + ") {");
                ClassFile.tab();
                s = getGetterString(props, -1, 0);
                //props.setFillerGetter(s);
                s = "return " + s + ";";
                ClassFile.println(s);
                ClassFile.backTab();
                ClassFile.endMethodScope();

                //Now setters
                ClassFile.doMethodScope("public "
                        + " void set" + name2
                        + "(" + SymbolConstants.get(props.getJavaType().getType()) + " val"
                        + setSubscripts + ") {");
                ClassFile.tab();
                s = getSetterString(props, -1, 0);
                //props.setFillerSetter(s);
                s += ";";
                ClassFile.println(s);
                ClassFile.backTab();
                ClassFile.endMethodScope();
            }
        }
        if (props.isAsBytesAccessor()) {
            ClassFile.doMethodScope("public " + SymbolConstants.get(props.getJavaType().getType())
                    + " get" + name2 + "AsBytes(" + getSubscripts + ") {");
            ClassFile.tab();
            ClassFile.println("CobolBytes " + SymbolTable.TOSTRING_REF_STRING_
                    + "=new CobolBytes(" + String.valueOf(props.getLength()) + ");");
            printGroupGetBytesOccursLoop(props, -1, props.getUnAdjustedOffset());
            ClassFile.println("return " + SymbolTable.TOSTRING_REF_STRING_ + ".getBytes();");
            ClassFile.backTab();
            ClassFile.endMethodScope();
        }
        printCobolBytesClassConditionTail(props);
        return;
    }

    private void preparePrintCobolBytesAccessors(SymbolProperties props) {
        if ((props.getLevelNumber() == 1 || props.getLevelNumber() == 77)
                && props.getRedefines() != null && props.getPictureString() != null
                && props.getRedefines().getPictureString() == null) {
            refString = props.getRedefines().getJavaName1();
        } else {
            refString = "super";
        }

        getSubscripts = "";
        setSubscripts = "";

        Integer offset = props.getOffset();
        offsetCalc = offset.toString().trim();

        if (props.isOccurs() || props.isAParentInOccurs()) {
            ArrayList<SymbolProperties> a = props.getOccursParents();
            if (a != null && a.size() > 0) {
                //   offsetCalc="";
                int i = 0;
                for (Iterator<SymbolProperties> ite = a.iterator(); ite.hasNext();) {
                    ite.next();
                    ++i;
                    getSubscripts += "int idx" + String.valueOf(i) + ((ite.hasNext()) ? "," : "");
                    setSubscripts += ",int idx" + String.valueOf(i);


                }
            }
            offsetCalc = getOccursString(props, -1, 0, offset);
        }
    }

    private void printCobolBytesClassConditionTail(SymbolProperties props) {

        if (props.getJavaType().getUsage() == Constants.DISPLAY || props.getJavaType().getUsage() == Constants.PACKED_DECIMAL) {
            if (props.isNumericTested()) {
                ClassFile.doMethodScope("public boolean is" + props.getJavaName2() + "Numeric(" + getSubscripts + ") {");
                ClassFile.tab();
                ClassFile.println("return "
                        + refString
                        + ".isNumeric(" + offsetCalc + "," + String.valueOf(props.getLength()) + ");");
                ClassFile.backTab();
                ClassFile.endMethodScope();
            }
            if (props.isAlphabeticTested()) {
                ClassFile.doMethodScope("public boolean is" + props.getJavaName2() + "Alphabetic(" + getSubscripts
                        + ") {");
                ClassFile.tab();
                ClassFile.println("return "
                        + refString
                        + ".isAlphabetic(" + offsetCalc + "," + String.valueOf(props.getLength()) + ");");
                ClassFile.backTab();
                ClassFile.endMethodScope();
            }
            if (props.isAlphabeticLowerTested()) {
                ClassFile.doMethodScope("public boolean is" + props.getJavaName2() + "AlphabeticLower(" + getSubscripts
                        + ") {");
                ClassFile.tab();
                ClassFile.println("return "
                        + refString
                        + ".isAlphabeticLower(" + offsetCalc + "," + String.valueOf(props.getLength()) + ");");
                ClassFile.backTab();
                ClassFile.endMethodScope();
            }
            if (props.isAlphabeticUpperTested()) {
                ClassFile.doMethodScope("public boolean is" + props.getJavaName2() + "AlphabeticUpper(" + getSubscripts
                        + ") {");
                ClassFile.tab();
                ClassFile.println("return "
                        + refString
                        + ".isAlphabeticUpper(" + offsetCalc + "," + String.valueOf(props.getLength()) + ");");
                ClassFile.backTab();
                ClassFile.endMethodScope();
            }
        }
    }

    public String getGetterString(SymbolProperties props, int modifiedIdx, int relativeToUnAdjustedOffset) {

        int offset = getOffset(props, relativeToUnAdjustedOffset);
        int len = props.getLength();
        if ((len == 0 || props.getJavaType() == null)) {
            return "";
        }
        //String refString;

        if ((props.getLevelNumber() == 1 || props.getLevelNumber() == 77)
                && props.getRedefines() != null && !props.isGroupData()
                && props.getRedefines().getPictureString() == null) {
            refString = props.getRedefines().getJavaName1();
        } else {
            refString = "super";
        }

        //String offsetCalc;
        if (modifiedIdx == -2) {
            len = getMaxLen(props);
            offsetCalc = String.valueOf(offset);
        } else {
            offsetCalc = getOccursString(props, modifiedIdx,
                    relativeToUnAdjustedOffset, offset);
        }


        if (props.isGroupData()) {
            return refString + ".getBytes(" + offsetCalc + "," + String.valueOf(len) + ")";
        }

        switch (props.getJavaType().getType()) {
            case Constants.BYTE:
            case Constants.CHAR:
                return getSingleByteGetterString(props, refString, offsetCalc);
            case Constants.SHORT:
            case Constants.INTEGER:
                return getIntegerGetterString(props, refString, offsetCalc);
            case Constants.LONG:
                return getLongGetterString(props, refString, offsetCalc);
            case Constants.FLOAT:
            case Constants.DOUBLE:
            case Constants.BIGDECIMAL:
                return getBigDecimalGetterString(props, refString, offsetCalc);
            case Constants.STRING:
                if (props.getDataUsage() == Constants.DISPLAY) {
                    return refString + ".toString(" + offsetCalc + "," + len + ")";
                } else {
                    return "notAvailable";
                }
            default:
        }
        return "";
    }
    private boolean forceDisplayString = false;

    private int getOffset(SymbolProperties props, int relativeToUnAdjustedOffset) {
        if (forceDisplayString) {
            if (relativeToUnAdjustedOffset > 0) {
                return props.getUnAdjustedOffset() - relativeToUnAdjustedOffset;
            } else {
                return props.getUnAdjustedOffset();
            }
        }
        if (props.isAParentInOccurs()) {
            return props.getUnAdjustedOffset() - props.getOccursParents().get(props.getOccursParents().size() - 1).getUnAdjustedOffset();
        }
        boolean test = (relativeToUnAdjustedOffset <= 0);
        if (RESConfig.getInstance().getOptimizeAlgorithm() == 2
                && (props.isUsedNativeJavaTypes() || props.getIsFiller())) {
            test = (relativeToUnAdjustedOffset < 0);
        }
        return (test) ? props.getOffset() : (props.getUnAdjustedOffset() - relativeToUnAdjustedOffset);
    }

    public String getSetterString(SymbolProperties props, int modifiedIdx, int relativeToUnAdjustedOffset) {

        Integer offset = 0;
        offset = getOffset(props, relativeToUnAdjustedOffset);
        Integer len = props.getLength();
        int usage = props.getDataUsage();
        CobolSymbol sym = (CobolSymbol) props.getJavaType();
        String s = null;
        if ((len == 0 || sym == null)) {
            return "";
        }

        //String refString;
        if ((props.getLevelNumber() == 1 || props.getLevelNumber() == 77)
                && props.getRedefines() != null && props.getPictureString() != null
                && props.getRedefines().getPictureString() == null) {
            refString = props.getRedefines().getJavaName1();
        } else {
            refString = "super";
        }

        // String offsetCalc;
        if (modifiedIdx == -2) {
            len = getMaxLen(props);
            offsetCalc = String.valueOf(offset);
        } else {
            offsetCalc = getOccursString(props, modifiedIdx,
                    relativeToUnAdjustedOffset, offset);
        }

        if (props.isGroupData()) {
            return refString
                    + ".valueOf(" + offsetCalc + ","
                    + len.toString() + ",val"
                    + ",0)";
        }

        switch (sym.getType()) {
            case Constants.BYTE:
            case Constants.CHAR: {
                s = getSingleByteSetterString(props, refString, offsetCalc);
                break;
            }
            case Constants.SHORT:
            case Constants.INTEGER: {
                s = getIntegerSetterString(props, refString, offsetCalc);
                break;
            }
            case Constants.LONG: {
                s = getLongSetterString(props, refString, offsetCalc);
                break;
            }
            case Constants.FLOAT:
            case Constants.DOUBLE:
            case Constants.BIGDECIMAL: {
                {
                    s = getBigDecimalSetterString(props, refString, offsetCalc);
                    break;
                }
            }
            case Constants.STRING: {
                switch (usage) {
                    case Constants.DISPLAY:
                        s = refString
                                + ".valueOf(" + offsetCalc + ","
                                + len.toString().trim() + ","
                                + "val"
                                + ")";
                        break;
                    case Constants.BINARY:
                    case Constants.PACKED_DECIMAL:
                    case Constants.COMPUTATIONAL1:
                    default:
                        s = "notAvailable";
                }
                break;
            }
        }
        return s;
    }

    public String getPointerSetterString(SymbolProperties props) {

        Integer len = props.getLength();
        int usage = props.getDataUsage();
        CobolSymbol sym = (CobolSymbol) props.getJavaType();
        String s = null;
        if ((len == 0 || sym == null)) {
            return "";
        }

        // String refString;
        if ((props.getLevelNumber() == 1 || props.getLevelNumber() == 77)
                && props.getRedefines() != null && props.getPictureString() != null
                && props.getRedefines().getPictureString() == null) {
            refString = props.getRedefines().getJavaName1();
        } else {
            refString = "super";
        }

        if (props.isGroupData()) {
            return refString
                    + ".valueOf(" + offsetCalc + ","
                    + len.toString() + ",val"
                    + ",0);";
        }

        switch (sym.getType()) {
            case Constants.BYTE:
                s = refString + ".setByte(val)";
                break;
            case Constants.CHAR:
                s = refString + ".setChar(val)";
                break;
            case Constants.SHORT:
            case Constants.INTEGER:
                s = refString + ".setInteger(val)";
                break;
            case Constants.LONG:
                s = refString + ".setLong(val)";
                break;
            case Constants.FLOAT:
            case Constants.DOUBLE:
            case Constants.BIGDECIMAL:
                s = refString + ".setBigDecimal(val)";
                break;
            case Constants.STRING:
                switch (usage) {
                    case Constants.DISPLAY:
                        s = refString + ".setString(val)";
                        break;
                    case Constants.BINARY:
                    case Constants.PACKED_DECIMAL:
                    case Constants.COMPUTATIONAL1:
                    default:
                        s = "notAvailable";
                }
                break;
        }
        return s;
    }

    private int getMaxLen(SymbolProperties props) {
        int maxlen = 0;
        if (props.isOccurs() || props.isAParentInOccurs()) {
            ArrayList<SymbolProperties> a = props.getOccursParents();
            if (a != null && a.size() > 0) {
                for (ListIterator<SymbolProperties> ite = a.listIterator(a.size()); ite.hasPrevious();) {
                    props = ite.previous();
                    maxlen += ((props.getMaxOccursInt() > 0) ? props.getLength() * (props.getMaxOccursInt()) : props.getLength());
                }
            }
        } else {
            maxlen = props.getLength();
        }
        return maxlen;
    }

    private int getGroupOccursIdx(SymbolProperties props) {
        return props.getNoOccursSubscripts();
    }

    private String getGroupOccursSignature(SymbolProperties props) {
        String signature = "";
        String subscripts = "";
        int i = 0;
        if (props.isOccurs() || props.isAParentInOccurs()) {
            ArrayList<SymbolProperties> a = props.getOccursParents();
            if (a != null && a.size() > 0) {
                for (Iterator<SymbolProperties> ite = a.iterator(); ite.hasNext();) {
                    ite.next();
                    ++i;
                    signature += "int idx" + String.valueOf(i) + ((ite.hasNext()) ? "," : "");
                    subscripts += "idx" + String.valueOf(i) + ((ite.hasNext()) ? "," : "");
                }

            }
            props.setIndexesWorkSpace(new ArrayList<String>());
            props.getIndexesWorkSpace().add(subscripts);
        }
        return signature;
    }

    public String getOccursString(SymbolProperties props, int modifiedIdx,
            int relativeToUnAdjustedOffset, Integer offset) {
        return getOccursString(props, modifiedIdx, relativeToUnAdjustedOffset, offset,
                RESConfig.getInstance().getOptimizeAlgorithm() == 0);
    }

    public String getOccursString(SymbolProperties props, int modifiedIdx,
            int relativeToUnAdjustedOffset, Integer offset, boolean hideIndexes) {
        getSubscripts = "";
        setSubscripts = "";
        String offsetCalcLocal = null;
        if (props.isOccurs() || props.isAParentInOccurs()) {
            ArrayList<SymbolProperties> a = props.getOccursParents();
            int i = 1;
            if (a != null && a.size() > 0) {
                for (Iterator<SymbolProperties> ite = a.iterator(); ite.hasNext(); i++) {
                    SymbolProperties curr = ite.next();
                    if (i > modifiedIdx) {
                        if (offsetCalcLocal == null) {
                            if (!hideIndexes) {
                                offsetCalcLocal = "super.__offset(";
                            } else {
                                offsetCalcLocal = "";
                            }
                        } else if (!hideIndexes) {
                            offsetCalcLocal += ',';
                        } else {
                            offsetCalcLocal += '+';
                        }
                        getSubscripts += "int idx" + String.valueOf(i) + ((ite.hasNext()) ? "," : "");
                        setSubscripts += ",int idx" + String.valueOf(i);
                        if (hideIndexes) {
                            if ((offset = getOffset(curr, relativeToUnAdjustedOffset)) == 0) {
                                if (curr.getLength() == 1) {
                                    offsetCalcLocal += "--" + "idx" + String.valueOf(i);
                                } else {
                                    offsetCalcLocal += String.valueOf(curr.getLength()) + "*--" + "idx" + String.valueOf(i);
                                }
                            } else {
                                if (curr.getLength() == 1) {
                                    offsetCalcLocal += String.valueOf(getOffset(curr, relativeToUnAdjustedOffset))
                                            + '+' + "--" + "idx" + String.valueOf(i);
                                } else {
                                    offsetCalcLocal += String.valueOf(getOffset(curr, relativeToUnAdjustedOffset))
                                            + '+' + String.valueOf(curr.getLength()) + "*--" + "idx" + String.valueOf(i);
                                }
                            }
                        } else {
                            offsetCalcLocal += String.valueOf(getOffset(curr, relativeToUnAdjustedOffset))
                                    + ',' + String.valueOf(curr.getLength()) + ',' + "idx" + String.valueOf(i);
                        }

                    }
                }
                if (offsetCalcLocal != null) {
                    if (!hideIndexes) {
                        offsetCalcLocal += ')';
                    }
                    if (props.isAParentInOccurs() && (offset = getOffset(props, relativeToUnAdjustedOffset)) > 0) {
                        offsetCalcLocal += "+" + String.valueOf(offset);
                    }
                    return offsetCalcLocal;
                }
            }
        }
        return offset.toString().trim();
    }

    private String getBigDecimalSetterString(SymbolProperties props, String refString, String offsetCalc) {
        switch (props.getDataUsage()) {
            case Constants.DISPLAY:
                if (forceDisplayAccessor) {
                    return refString + ".valueOf(" + offsetCalc + ","
                            + String.valueOf(props.getLength()) + ",val,true)";
                } else {
                    return refString + ".setDisplayBigDecimal(" + offsetCalc + ","
                            + props.getLength() + ",val," + String.valueOf(props.getJavaType().getMaxFractionLength()) + ","
                            + String.valueOf(props.isSigned()) + "," + String.valueOf(props.isSignLeading()) + ","
                            + String.valueOf(props.isSignLeading()) + ")";
                }
            case Constants.BINARY:
                return refString + ".setBinaryBigDecimal(" + offsetCalc + ","
                        + String.valueOf(props.getAdjustedLength()) + ",val,"
                        + String.valueOf(props.getJavaType().getMaxFractionLength()) + ","
                        + String.valueOf(props.isSigned()) + ")";
            case Constants.PACKED_DECIMAL:
                return refString + ".setPackedDecimalBigDecimal(" + offsetCalc + ","
                        + String.valueOf(props.getAdjustedLength()) + ",val,"
                        + String.valueOf(props.getJavaType().getMaxFractionLength()) + ","
                        + String.valueOf(props.isSigned()) + ")";
            case Constants.COMPUTATIONAL1:
            case Constants.COMPUTATIONAL2:
                return refString + ".setBinaryBigDecimal(" + offsetCalc + ","
                        + props.getAdjustedLength() + ",new BigDecimal(val)," + String.valueOf(props.getJavaType().getMaxFractionLength());
            default:
                SymbolUtil.reportError();
        }
        return "";
    }

    private String getLongSetterString(SymbolProperties props,
            String refString, String offsetCalc) {
        switch (props.getDataUsage()) {
            case Constants.DISPLAY:
                if (forceDisplayAccessor) {
                    return refString + ".valueOf(" + offsetCalc + ","
                            + String.valueOf(props.getLength()) + ",val,true)";
                } else {
                    return refString + ".setDisplayLong(" + offsetCalc + ","
                            + props.getLength() + "," + "val" + "," + String.valueOf(props.isSigned()) + ","
                            + String.valueOf(props.isSignLeading()) + "," + String.valueOf(props.isSignLeading()) + ")";
                }
            case Constants.BINARY:
                return refString + ".setBinaryLong(" + offsetCalc + "," + String.valueOf(props.getAdjustedLength()) + ","
                        + "val" + "," + String.valueOf(props.isSigned()) + ")";
            case Constants.PACKED_DECIMAL:
                return refString + ".setPackedDecimalLong(" + offsetCalc + ","
                        + String.valueOf(props.getAdjustedLength()) + "," + "val" + "," + String.valueOf(props.isSigned()) + ")";
            case Constants.COMPUTATIONAL1:
                return refString + ".setBinaryBigDecimal(" + offsetCalc + "," + props.getAdjustedLength() + ","
                        + "super.normalizeDecimal(val," + props.getAdjustedLength() + "," + props.getJavaType().getMaxFractionLength() + ")" + ","
                        + props.getJavaType().getMaxFractionLength() + ")";
            default:
                SymbolUtil.reportError();
        }
        return "";
    }

    private String getIntegerSetterString(SymbolProperties props, String refString, String offsetCalc) {
        switch (props.getDataUsage()) {
            case Constants.DISPLAY:
                if (forceDisplayAccessor) {
                    return refString + ".valueOf(" + offsetCalc + ","
                            + String.valueOf(props.getLength()) + ",val,true)";
                } else {
                    return refString + ".setDisplayInt(" + offsetCalc + "," + String.valueOf(props.getLength()) + ",val"
                            + "," + String.valueOf(props.isSigned()) + "," + String.valueOf(props.isSignLeading()) + ","
                            + String.valueOf(props.isSignLeading()) + ")";
                }
            case Constants.BINARY:
                return refString + ".setBinaryInt(" + offsetCalc + ","
                        + String.valueOf(props.getAdjustedLength()) + ",val" + "," + String.valueOf(props.isSigned()) + ")";
            case Constants.PACKED_DECIMAL:
                return refString + ".setPackedDecimalInt(" + offsetCalc + "," + String.valueOf(props.getAdjustedLength())
                        + ",val" + "," + String.valueOf(props.isSigned()) + ")";
            case Constants.COMPUTATIONAL1:
                return refString + ".setBinaryBigDecimal(" + offsetCalc + "," + String.valueOf(props.getAdjustedLength()) + ","
                        + "super.normalizeDecimal(val," + props.getAdjustedLength() + ","
                        + String.valueOf(props.getJavaType().getMaxFractionLength()) + ")" + ","
                        + props.getJavaType().getMaxFractionLength() + ")";
            default:
                SymbolUtil.reportError();
        }
        return "";
    }

    private String getBigDecimalGetterString(SymbolProperties props, String refString, String offsetCalc) {
        switch (props.getDataUsage()) {
            case Constants.DISPLAY:
                if (forceDisplayAccessor) {
                    return refString + ".toString(" + offsetCalc + ","
                            + String.valueOf(props.getLength())+')';// + "," + String.valueOf(props.getJavaType().getMaxFractionLength()) + "," + props.getJavaType().isIsSigned() + ","
                            //+ props.getJavaType().isIsSignLeading() + "," + props.getJavaType().isIsSignSeparate() + ")";
                } else {
                    return refString + ".getDisplayBigDecimal(" + offsetCalc + ","
                            + String.valueOf(props.getLength()) + "," + String.valueOf(props.getJavaType().getMaxFractionLength()) + ","
                            + String.valueOf(props.isSigned()) + "," + String.valueOf(props.isSignLeading()) + ","
                            + String.valueOf(props.isSignLeading()) + ")";
                }
            case Constants.BINARY:
                return refString + ".getBinaryBigDecimal(" + offsetCalc + "," + String.valueOf(props.getAdjustedLength())
                        + "," + new Integer(props.getJavaType().getMaxFractionLength()).toString().trim() + ")";
            case Constants.PACKED_DECIMAL:
                return refString + ".getPackedDecimalBigDecimal(" + offsetCalc + ","
                        + String.valueOf(props.getAdjustedLength()) + ","
                        + String.valueOf(props.getJavaType().getMaxFractionLength()) + ")";
            case Constants.COMPUTATIONAL1:
            case Constants.COMPUTATIONAL2:
                return refString + ".getBinaryBigDecimal(" + offsetCalc + "," + String.valueOf(props.getLength()) + ","
                        + String.valueOf(props.getJavaType().getMaxFractionLength()) + ").floatValue()";
            default:
                SymbolUtil.reportError();
        }
        return "";
    }

    private String getLongGetterString(SymbolProperties props, String refString, String offsetCalc) {
        switch (props.getDataUsage()) {
            case Constants.DISPLAY:
                if (forceDisplayAccessor) {
                    return refString + ".toString(" + offsetCalc + ","
                            + String.valueOf(props.getLength())+')' ;//+ "," + props.getJavaType().isIsSigned() + ","
                            //+ props.getJavaType().isIsSignLeading() + "," + props.getJavaType().isIsSignSeparate() + ")";
                } else {
                    return refString + ".getDisplayLong(" + offsetCalc + "," + String.valueOf(props.getLength()) + ","
                            + String.valueOf(props.isSigned()) + "," + String.valueOf(props.isSignLeading()) + ","
                            + String.valueOf(props.isSignLeading()) + ")";
                }
            case Constants.BINARY:
                return refString + ".getBinaryLong(" + offsetCalc + "," + String.valueOf(props.getAdjustedLength()) + ")";
            case Constants.PACKED_DECIMAL:
                return refString + ".getPackedDecimalLong(" + offsetCalc + ","
                        + String.valueOf(props.getAdjustedLength()) + ")";
            case Constants.COMPUTATIONAL1:
                return refString + ".getBinaryBigDecimal(" + offsetCalc + "," + String.valueOf(props.getLength()) + ","
                        + new Integer(props.getJavaType().getMaxFractionLength()).toString().trim() + ")";
            default:
                SymbolUtil.reportError();
        }
        return "";
    }

    private String getSingleByteGetterString(SymbolProperties props, String refString, String offsetCalc) {
        if (props.getJavaType().getType() == Constants.CHAR) {
            return refString + ".getChar(" + offsetCalc + ")";
        } else {
            return refString + ".getByte(" + offsetCalc + ")";
        }
    }

    private String getSingleByteSetterString(SymbolProperties props, String refString, String offsetCalc) {
        if (props.getJavaType().getType() == Constants.CHAR) {
            return refString + ".setChar(" + offsetCalc + ",val)";
        } else {
            return refString + ".setByte(" + offsetCalc + ",val)";
        }
    }

    private String getIntegerGetterString(SymbolProperties props, String refString, String offsetCalc) {
        switch (props.getDataUsage()) {
            case Constants.DISPLAY:
                if (forceDisplayAccessor) {
                    return refString + ".toString(" + offsetCalc + ","
                            + String.valueOf(props.getLength())+')';// + "," + props.getJavaType().isIsSigned() + ","
                            //+ props.getJavaType().isIsSignLeading() + "," + props.getJavaType().isIsSignSeparate() + ")";
                } else {
                    return refString + ".getDisplayInt(" + offsetCalc + "," + String.valueOf(props.getLength()) + ","
                            + String.valueOf(props.isSigned()) + "," + String.valueOf(props.isSignLeading()) + ","
                            + String.valueOf(props.isSignLeading()) + ")";
                }
            case Constants.BINARY:
                return refString + ".getBinaryInt(" + offsetCalc + "," + String.valueOf(props.getAdjustedLength()) + ")";
            case Constants.PACKED_DECIMAL:
                return refString + ".getPackedDecimalInt(" + offsetCalc + "," + String.valueOf(props.getAdjustedLength()) + ")";
            case Constants.COMPUTATIONAL1:
                return refString + ".getBinaryBigDecimal(" + offsetCalc + "," + String.valueOf(props.getLength()) + ","
                        + new Integer(props.getJavaType().getMaxFractionLength()).toString().trim() + ")";
            default:
                SymbolUtil.reportError();
                return "";
        }
    }

    private String getJavaTypeSetterString(SymbolProperties props) {
        CobolSymbol sym = (CobolSymbol) props.getJavaType();
        if (sym == null) {
            return "%0";
        }

        switch (sym.getType()) {
            case Constants.SHORT:
            case Constants.INTEGER:
                return "(int) super.normalizeBinary(%0," + String.valueOf(props.getAdjustedLength())
                        + "," + String.valueOf(props.isSigned()) + ")";
            case Constants.LONG:
                return "super.normalizeBinary(%0," + String.valueOf(props.getAdjustedLength())
                        + "," + String.valueOf(props.isSigned()) + ")";
            case Constants.BIGDECIMAL:
                return "super.normalizeDecimal(%0," + String.valueOf(props.getAdjustedLength()) + ","
                        + String.valueOf(props.getJavaType().getMaxFractionLength()) + "," + String.valueOf(props.getJavaType().isIsSigned()) + ")";
            case Constants.STRING:
                return "super.normalizeString(%0," + String.valueOf(props.getLength()) + ","
                        + String.valueOf(props.isJustifiedRight())
                        + ")";
            case Constants.CHAR:
            default:
                return "%0";
        }
    }

    public String getLocalFillerSetter(SymbolProperties props, String fillerVal) {
        return getSetterString(props, -1, -1).replace(",val", "," + fillerVal);
    }

    public String getLocalFillerGetter(SymbolProperties props) {
        return getGetterString(props, -1, -1);
    }

    public String getFillerSetter(SymbolProperties props, String fillerVal) {
        String level01Name = getLevel01Name(props);
        return getLocalFillerSetter(props, fillerVal).replace("super.", ((level01Name == null) ? "" : (level01Name + '.')));
    }
/*
    public String getToStringOrGroupMove(SymbolProperties propsFrom) {
        String level01Name = getLevel01Name(propsFrom);
        return ((level01Name == null) ? ("getProgram().toStringWithoutSign(") : (level01Name + ".toStringWithoutSign("))
                + getOccursString(propsFrom, -1, 0, propsFrom.getOffset(), false)
                + "," + propsFrom.getLength() + "," + propsFrom.isSigned()
                + "," + propsFrom.isSignLeading() + "," + propsFrom.isSignSeparate() + ")";
    }

    public String getFromStringOrGroupMove(SymbolProperties props, String fillerVal) {
        String level01Name = getLevel01Name(props);
        return ((level01Name == null) ? "valueOf(" : (level01Name + ".valueOf(")) + getOccursString(props, -1, 0, props.getOffset(), false) + ","
                + props.getLength() + "," + fillerVal + ")";
    }

 * 
 */
    public String getFillerGetter(SymbolProperties props) {
        String level01Name = getLevel01Name(props);
        return getGetterString(props, -1, -1).
                replace("super.", ((level01Name == null) ? "" : (level01Name + '.')));
    }

    private String getLevel01Name(SymbolProperties props) {
        while ((props = props.getParent()) != null && !props.is01Group()) {
            continue;
        }
        if (props == null) {
            return null;
        } else {
            return props.getJavaName1();
        }
    }

    private void printClass88s(SymbolProperties props) {
        ArrayList<SymbolProperties> a = (ArrayList<SymbolProperties>) props.getChildren();
        if (a != null) {
            //boolean isNativeJavaType=RESConfig.getInstance().isUseNativeJavaTypes();
            for (SymbolProperties child : a) {
                if (child.getLevelNumber() != 88) {
                    break;
                }
                Main.getContext().getCobol2Java().doOne88(child);
            }
        }
    }

    public void printClassAccessors(SymbolProperties props,
            String name1, String name2) {

        if (name1.equals("sqlca")) {
            ClassFile.println("public " + name2 + " " + name1 + " = new " + name2 + "();");
        } else if (props.getRedefines() == null) {
            ClassFile.println("private " + name2 + " " + name1 + " = new " + name2 + "();");
        } else {
            if (props.getRedefines().is01Group()) {
                ClassFile.println("private " + name2 + " " + name1 + " = new " + name2 + "("
                        + props.getRedefines().getJavaName1()
                        + ");");
            } else {
                String exprString = "this," + String.valueOf(props.getRedefines().getOffset()) + ","
                        + String.valueOf(props.getLength());
                ClassFile.println("private " + name2 + " " + name1 + " = new " + name2 + "(" + exprString
                        + ");");
            }
        }
        if (!RESConfig.getInstance().isUsePointers()) {
            ClassFile.println("public byte[] get" + name2 + "() {");
            ClassFile.println("\treturn " + name1 + ".getBytes();");
            ClassFile.println("}");
            ClassFile.println("public void set" + name2 + "(byte[] val) {");
            ClassFile.println("\t" + name1 + ".valueOf(val);");
            ClassFile.println("}");
        } else {
            ClassFile.println("public BytesField get" + name2 + "() {");
            ClassFile.println("\treturn " + name1 + "." + name1 + "Fld_;");
            ClassFile.println("}");
            ClassFile.println("public void set" + name2 + "(byte[] val) {");
            ClassFile.println("\t" + name1 + ".valueOf(val);");
            ClassFile.println("}");
        }

        printClassConditionTail(props);
    }

    private void printClassConditionTail(SymbolProperties props) {
        if (props.isUsedNativeJavaTypes()) {
            printJavaTypeClassConditionTail(props);
        } else {
            preparePrintCobolBytesAccessors(props);
            printCobolBytesClassConditionTail(props);
        }
    }

    private void printStringAccessors(SymbolProperties props) {
        ClassFile.doMethodScope("public String toString() {");
        ClassFile.tab();
        if (props.hasChildren()) {
            ClassFile.println("return new String(getBytes());");
        } else {
            ClassFile.println("return \"\";");
        }
        ClassFile.backTab();
        ClassFile.endMethodScope();
        ClassFile.doMethodScope("public void valueOf(String val) {//Bytes Vs. Chars");
        ClassFile.tab();
        if (props.hasChildren()) {
            ClassFile.println("valueOf(val.getBytes());");
        } else {
            ClassFile.println("return;");
        }
        ClassFile.backTab();
        ClassFile.endMethodScope();
    }

    private void printConstructors(SymbolProperties props, Integer adjLen) {
        ClassFile.doMethodScope("public " + props.getJavaName2() + "() {");
        ClassFile.tab();
        if (//(!props.isUsedNativeJavaTypes()||RESConfig.getInstance().getOptimizeAlgorithm()==2)
                //&&
                adjLen > 0) {
            ClassFile.println("super(new CobolBytes(" + String.valueOf(adjLen) + "));");
        } else {
            ClassFile.println("super();");
        }
        ClassFile.backTab();
        ClassFile.endMethodScope();
        ClassFile.doMethodScope("public " + props.getJavaName2() + "(CobolBytes b) {//For redefines");
        ClassFile.println("\tsuper(b); }");
        ClassFile.doMethodScope("public " + props.getJavaName2() + "(CobolBytes b,int off,int len) {//For redefines");
        ClassFile.println("\tsuper(b,off,len); }");
    }

    private void printInitialize(SymbolProperties props) {
        ClassFile.doMethodScope("public void initialize(Program p) {");
        ClassFile.tab();
        ClassFile.println("__setProgram(p);");
        printInit(props);
        ClassFile.backTab();
        ClassFile.endMethodScope();
    }

    private void printInit(SymbolProperties props) {
        if (props.hasChildren()) {
            for (SymbolProperties child : props.getChildren()) {
                Main.getContext().getCobol2Java().initializeOneSymbol(child);
                if (child != null && child.getChildren() != null) {
                    printInit(child);
                }
            }
        }
    }

    public static void reportError() {
        try {
            throw new InvalidCobolFormatException("");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void reportError(SymbolProperties props, String msg) {
        System.out.println("@CobolSourceFile(\"" + props.getDataDescriptionEntry().sourceFile + "\","
                + props.getDataDescriptionEntry().line
                + "):" + msg);
        System.exit(0);
    }

    public void reportError(String msg) {
        System.out.println("@CobolSourceFile(\"" + Main.getContext().getSourceFileName() + "):" + msg);
        System.exit(0);
    }
}
