package com.res.java.translation.engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import com.res.common.RESConfig;
import com.res.java.lib.Constants;
import com.res.java.lib.RunTimeUtil;
import com.res.java.lib.StringTokenizerEx;
import com.res.java.translation.symbol.SymbolProperties.CobolSymbol;
import com.res.java.translation.symbol.SymbolConstants;
import com.res.java.translation.symbol.SymbolProperties;
import com.res.java.translation.symbol.SymbolTable;
import com.res.java.translation.symbol.SymbolUtil;
import com.res.java.util.ClassFile;
import com.res.java.util.NameUtil;

public class TranUtil {

    private static TranUtil thiz = null;

    private TranUtil() {
    }

    public static TranUtil get() {
        if (thiz == null) {
            thiz = new TranUtil();
        }
        return thiz;
    }

    public static void clear() {
        thiz = null;
    }

    public ArrayList<String> getParameters(String sqlString, boolean extendedPattern) {
        ArrayList<String> ret = new ArrayList<String>();
        StringTokenizerEx aStringTokenizerEx = null;
        if (!extendedPattern) {
            aStringTokenizerEx = new StringTokenizerEx(" " + sqlString + " ", ":[ ]*[a-zA-Z0-9\\-\\.]+([ \t\r\n]*:[ ]*[a-zA-Z0-9\\-\\.]+)?");
        } else {
            aStringTokenizerEx = new StringTokenizerEx(" " + sqlString + " ", "[:]?[ ]*[a-zA-Z0-9\\-\\.]+([ \t\r\n]*:[ ]*[a-zA-Z0-9\\-\\.]+)?");
        }
        @SuppressWarnings("unused")
        final String firstDelimiter = aStringTokenizerEx.getDelimiter();
        for (@SuppressWarnings("unused") String aString : aStringTokenizerEx) {
            // uses the split String detected and memorized in 'aString'
            ret.add(aStringTokenizerEx.getDelimiter());
        }

        return ret;
    }

    //parmWithOptionalIndiacator is of the format :data-name:indicator or :data-name
    public SymbolProperties getParameter(String parmWithOptionalIndiacator) {
        int j;
        String parm;
        if (parmWithOptionalIndiacator == null
                || (parmWithOptionalIndiacator = parmWithOptionalIndiacator.trim()).length() <= 0) {
            return null;
        }
        if (parmWithOptionalIndiacator.charAt(0) != ':') {
            if (((j = parmWithOptionalIndiacator.indexOf(':', 1)) < 0)) {
                parm = parmWithOptionalIndiacator.substring(0);
            } else {
                parm = parmWithOptionalIndiacator.substring(0, j);
            }
        } else if (((j = parmWithOptionalIndiacator.indexOf(':', 1)) < 0)) {
            parm = parmWithOptionalIndiacator.substring(1);
        } else {
            parm = parmWithOptionalIndiacator.substring(1, j);
        }
        return SymbolTable.getScope().lookup(parm.trim(), SymbolConstants.DATA);
    }

    public SymbolProperties getIndicator(String parmWithOptionalIndiacator) {
        int j;
        String ind;
        if (parmWithOptionalIndiacator == null
                || (parmWithOptionalIndiacator = parmWithOptionalIndiacator.trim()).length() <= 0) {
            return null;
        }
        if (parmWithOptionalIndiacator.charAt(0) != ':') {
            if (((j = parmWithOptionalIndiacator.indexOf(':', 1)) < 0)) {
                return null;
            } else {
                ind = parmWithOptionalIndiacator.substring(j + 1).trim();
            }
        } else if (((j = parmWithOptionalIndiacator.indexOf(':', 1)) < 0)) {
            return null;
        } else {
            ind = parmWithOptionalIndiacator.substring(j + 1).trim();
        }
        return SymbolTable.getScope().lookup(ind, SymbolConstants.DATA);
    }
    private boolean previousError = false;
    private Properties dbProps = null;

    public void printIndexedFileDatabaseProperties(SymbolProperties props) {
        if (previousError || !RESConfig.getInstance().isToGenVSAMISAMDb()) {
            return;
        }
        if (dbProps == null) {
            if (RESConfig.getInstance().getOutputDir().length() <= 0) {
                RESConfig.getInstance().setOutputDir("." + File.separatorChar);
            } else if (RESConfig.getInstance().getOutputDir().charAt(
                    RESConfig.getInstance().getOutputDir().length() - 1) != File.separatorChar) {
                RESConfig.getInstance().setOutputDir(RESConfig.getInstance().getOutputDir() + File.separatorChar);
            }
            File dbPropsFile = new File(RESConfig.getInstance().getOutputDir() + "Database.properties");
            dbProps = new Properties();
            if (dbPropsFile.exists()) {
                if (dbPropsFile.canRead() && dbPropsFile.canWrite()) {
                    if (!RESConfig.getInstance().isOverwriteJavaFiles()) {
                        previousError = true;
                        RunTimeUtil.getInstance().reportError("Database.properties can not be overwritten.Try -overwrite option.", false);
                        return;
                    }
                    try {
                        dbProps.loadFromXML(new FileInputStream(dbPropsFile));
                    } catch (InvalidPropertiesFormatException e) {
                        previousError = true;
                        RunTimeUtil.getInstance().reportError("Database.properties can not be loaded.", false);
                        return;
                    } catch (FileNotFoundException e) {
                        previousError = true;
                        RunTimeUtil.getInstance().reportError("Database.properties can not be loaded.", false);
                        return;
                    } catch (IOException e) {
                        previousError = true;
                        RunTimeUtil.getInstance().reportError("Database.properties can not be loaded.", false);
                        return;
                    }
                } else {
                    previousError = true;
                    RunTimeUtil.getInstance().reportError("Database.properties can not be read/written.", false);
                    return;
                }
            }
        }

        String fileName = NameUtil.convertCobolNameToSQL(props.getOtherName());

//		if(RESConfig.getInstance().isOverwriteJavaFiles()) {
        //SymbolProperties primKey=SymbolTable.getScope().lookup(,props.getChildren().get(0).getDataName());
        ArrayList<String> altKeyList = new ArrayList<String>();
        StringBuffer altKey = new StringBuffer();
        StringBuffer altKeys = new StringBuffer();
        StringBuffer altKeys2 = new StringBuffer();
        StringBuffer altKeys3 = new StringBuffer();
        if (props.getOtherData() != null && props.getOtherData().size() > 0) {
            for (Object o : props.getOtherData()) {
                SymbolProperties os = (SymbolProperties) o;
                next:
                {
                    altKey.setLength(0);
                    altKey.append("alt_").append(os.getOffset()).
                            append('_').append(os.getLength());
                    for (String s : altKeyList) {
                        if (s.equalsIgnoreCase(altKey.toString())) {
                            break next;
                        }
                    }
                    altKeyList.add(altKey.toString());
                    if (altKeys.length() > 0) {
                        altKeys2.append(',');
                    }
                    altKeys.append(',');
                    altKeys2.append(altKey);
                    altKeys.append(altKey).append(" VARCHAR(").append(os.getLength()).append(") ");
                    if (!os.isDuplicateKey()) {
                        altKeys3.append(", UNIQUE (").append(altKey).append(')');
                    }
                }
            }
        }
        if (altKeys3.length() > 0) {
            altKeys.append(altKeys3);
        }
        String idLine = props.getDataName() + "(" + ((String) props.getOtherName()).toUpperCase() + ") PRIMARY(" + ((SymbolProperties) props.getOtherData2()).getJavaName2() + ")";
        dbProps.put(fileName, idLine);
        dbProps.put(fileName + "_createSQL", getSQLCreate(props, altKeys.toString()));
        dbProps.put(fileName + "_altKeys", altKeys2.toString());
        //	} else
        //	RunTimeUtil.getInstance().reportError("Properties exists for file " +fileName +
        //		" . Not overwritten.",false);
    }

    public void saveDatabaseProperties() {
        if (dbProps != null) {
            try {
                dbProps.storeToXML(new FileOutputStream(new File(RESConfig.getInstance().getOutputDir() + File.separatorChar + "Database.properties")), "Generated by RES.");
                dbProps = null;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getSQLCreate(SymbolProperties file, String altKeys) {
        return createSqlTemplate.replace("%0", NameUtil.convertCobolNameToSQL(file.getOtherName())).
                replace("%1", getCreateTableSqlAttributes(file) + altKeys);
    }

    public String getCreateTableSqlAttributes(SymbolProperties file) {
        columnsGen.clear();
        StringBuffer attributeString = new StringBuffer();
        if (file.getChildren() == null || file.getChildren().size() <= 0) {
            reportError("File must have atleast one data record defined.");
        }
        attributeString.append("PRIMARY_KEY__ ");
        switch (((SymbolProperties) file.getOtherData2()).getIdentifierType()) {
            case Constants.SHORT:
            case Constants.INTEGER:
            case Constants.LONG:
                attributeString.append("INTEGER");
                break;
            case Constants.BIGDECIMAL:
                attributeString.append("DECIMAL");
                break;
            case Constants.BYTE:
            case Constants.CHAR:
            case Constants.STRING:
            case Constants.GROUP:
                attributeString.append("varchar(" + String.valueOf(((SymbolProperties) file.getOtherData2()).getLength()) + ')');
            default:
        }
        attributeString.append(" UNIQUE, DATA_BYTES__ ");

        attributeString.append(DATA_BYTES_COLUMN_TYPE[RESConfig.getInstance().getSqlDatabaseType()]);

        //appendCreateAttributes(attributeString,findIndexedRecord(file));
        return attributeString.toString();
    }
    private static final String[] DATA_BYTES_COLUMN_TYPE = new String[]{
        "TEXT", "LONG", "TEXT", "TEXT", "LONG VARCHAR", "TEXT", "LONG CHAR", "LONG VARCHAR", "TEXT"
    };

    /*
    private SymbolProperties findIndexedRecord(SymbolProperties file) {
    for(SymbolProperties ch:file.getChildren())
    if(ch.isIndexedFileRecord())
    return ch;
    return file.getChildren().get(0);
    }
     */
    public void appendCreateAttributes(StringBuffer attributeString, SymbolProperties props) {
        CobolSymbol sym;
        if (props.isGroupData() && props.hasChildren()) {
            appendCreateAttributes(attributeString, props.getChildren());
        } else if (props.isElementData()) {
            sym = props.getJavaType();
            switch (sym.getType()) {
                case Constants.SHORT:
                case Constants.INTEGER:
                    if (attributeString != null) {
                        if (attributeString.length() > 0) {
                            attributeString.append(',');
                        }
                        attributeString.append(NameUtil.convertCobolNameToSQL(props.getDataName())).append(' ');
                        columnsGen.add(props);
                        attributeString.append("INT");
                    } else {
                        columnsGen.add(props);
                    }
                    break;
                case Constants.LONG:
                    if (attributeString != null) {
                        if (attributeString.length() > 0) {
                            attributeString.append(',');
                        }
                        attributeString.append(props.getJavaName1()).append(' ');
                        columnsGen.add(props);
                        attributeString.append("LONG");
                    } else {
                        columnsGen.add(props);
                    }

                    break;
                case Constants.BIGDECIMAL:
                    if (attributeString != null) {
                        if (attributeString.length() > 0) {
                            attributeString.append(',');
                        }
                        attributeString.append(props.getJavaName1()).append(' ');
                        columnsGen.add(props);
                        attributeString.append("DECIMAL");
                    } else {
                        columnsGen.add(props);
                    }

                    break;
                case Constants.BYTE:
                case Constants.CHAR:
                case Constants.STRING:
                case Constants.GROUP:
                default:
                    if (attributeString != null) {
                        if (attributeString.length() > 0) {
                            attributeString.append(',');
                        }
                        attributeString.append(props.getJavaName1()).append(' ');
                        attributeString.append("varchar(" + String.valueOf(sym.getMaxStringLength()) + ')');
                        columnsGen.add(props);
                    } else {
                        columnsGen.add(props);
                    }

            }
        }
    }

    public void appendCreateAttributes(StringBuffer arributeString, ArrayList<SymbolProperties> children) {
        for (SymbolProperties child : children) {
            appendCreateAttributes(arributeString, child);
        }
    }

    public String getInsertIntoSqlAttributes(SymbolProperties file) {
        StringBuffer arributeString1 = new StringBuffer();
        StringBuffer arributeString2 = new StringBuffer();
        if (file.getChildren() == null || file.getChildren().size() <= 0) {
            reportError("File must have atleast one data record defined.");
        }
        appendInsertAttributes1(arributeString1);
        appendInsertAttributes2(arributeString2);
        return arributeString1.insert(0, "(").append(") VALUES (").append(arributeString2).append(')').toString();
    }

    private void appendInsertAttributes1(StringBuffer attributeString) {
        for (SymbolProperties column : columnsGen) {
            CobolSymbol sym = column.getJavaType();
            switch (sym.getType()) {
                case Constants.SHORT:
                case Constants.INTEGER:
                case Constants.LONG:
                case Constants.BIGDECIMAL:
                case Constants.STRING:
                    if (attributeString.length() > 0) {
                        attributeString.append(',');
                    }
                    attributeString.append(NameUtil.convertCobolNameToSQL(column.getDataName()));
                default:
            }
        }

    }

    private void appendInsertAttributes2(StringBuffer attributeString) {
        for (int i = 0; i < columnsGen.size(); ++i) {
            if (i != 0) {
                attributeString.append(',');
            }
            attributeString.append('?');
        }
    }

    public int setSQLParameters(StringBuffer line) {
        int i = 1;
        for (SymbolProperties column : columnsGen) {
            line.append("\t__dao().set").append(SymbolConstants.getSQL(column.getIdentifierType())).append('(').append(i++).append(',').append(NameUtil.getJavaName(column, false)).append(");");
            ClassFile.println(line.toString());
            line.setLength(0);
        }
        return i;
    }

    public void printSetSQLProperties(SymbolProperties props) {
        /*
        columnsGen.clear();
        ClassFile.println("@Override");
        ClassFile.doMethodScope("public void setSQLProperties(CobolIndexedFile file) throws java.io.IOException {");
        ClassFile.tab();
        ClassFile.println("try {");
        ClassFile.tab();
        //appendCreateAttributes(null,props);
        int i=1;StringBuffer line=new StringBuffer();
        line.append("file.set").append(SymbolConstants.getSQL((
        (SymbolProperties)props.getParent().getOtherData2()).getIdentifierType())).append('(')
        .append(i++).append(',').append(
        SymbolUtil.getInstance().getLocalFillerGetter((SymbolProperties)props.getParent().getOtherData2())).append(");");
        line.append("file.set").append(SymbolConstants.getSQL(Constants.STRING)).append('(')
        .append(i++).append(',').append("new String(super.get()));");

        ClassFile.println(line.toString());
        ClassFile.backTab();
        ClassFile.println("} catch(java.sql.SQLException se) {throw new java.io.IOException(se);}");
        ClassFile.backTab();
        ClassFile.println("}");
         */
    }

    public void printSetStartSQLProperties(SymbolProperties props) {
        /*
        ClassFile.println("@Override");
        ClassFile.doMethodScope("public int numberOfColumns() {");
        ClassFile.println("\treturn "+String.valueOf(columnsGen.size()+1)+";");
        ClassFile.println("}");
         */
        if (props.getParent().getOtherData2() != null) {
            SymbolProperties pk = (SymbolProperties) props.getParent().getOtherData2();
            String pkStr = SymbolUtil.getInstance().getGetterString(pk, -1, 0);
            if (pk.getIdentifierType() == Constants.CHAR || pk.getIdentifierType() == Constants.BYTE) {
                pkStr = "String.valueOf(" + pkStr + ")";
            } else if (pk.getIdentifierType() == Constants.GROUP) {
                pkStr = "new String(" + pkStr + ")";
            }
            ClassFile.println("@Override");
            ClassFile.doMethodScope("public Object primaryKey() {");
            ClassFile.println("\treturn " + pkStr + ";");
            ClassFile.println("}");
            /*
            ClassFile.doMethodScope("public void setPrimaryKey(CobolIndexedFile file,int idx) throws java.io.IOException {");
            ClassFile.println("\ttry {");
            ClassFile.println("\t\tfile.set"+ SymbolConstants.getSQL(((SymbolProperties)props.getParent().getOtherData2()).getIdentifierType())+
            "(idx,get"+((SymbolProperties)props.getParent().getOtherData2()).getJavaName2()+"());");
            ClassFile.println("\t} catch(java.sql.SQLException e) {");
            ClassFile.println("\t\tthrow new java.io.IOException(e);}");
            ClassFile.println("}");
             */
        }
    }

    public void printgetSQLResults(SymbolProperties props) {
        /*
        columnsGen.clear();
        ClassFile.println("@Override");
        ClassFile.doMethodScope("public void getSQLResults(CobolIndexedFile file) throws java.io.IOException {");
        ClassFile.tab();
        ClassFile.println("try {");
        ClassFile.tab();
        //appendCreateAttributes(null,props);
        StringBuffer line=new StringBuffer();
        line.append(
        SymbolUtil.getInstance().getLocalFillerSetter(((SymbolProperties)props.getParent().getOtherData2()),
        "file.get"+SymbolConstants.getSQL(
        ((SymbolProperties)props.getParent().getOtherData2()).getIdentifierType())+'('+(i++)+")")).append(';');
        line.append("valueOf(").
        append("file.get"+SymbolConstants.getSQL(Constants.STRING)).	append('(').append(2).append("));");
        ClassFile.println(line.toString());
        ClassFile.backTab();
        ClassFile.println("} catch(java.sql.SQLException se) {throw new java.io.IOException(se);}");
        ClassFile.backTab();
        ClassFile.println("}");
         */
    }

    private void reportError(String msg) {
        RunTimeUtil.getInstance().reportError(msg, true);
    }
    private ArrayList<SymbolProperties> columnsGen = new ArrayList<SymbolProperties>();
    //private static final String dropSqlTemplate = "\"DROP TABLE %0 \"";
    private static final String createSqlTemplate = "\"CREATE TABLE %0 (%1) \"";
    //private static final String insertSqlTemplate = "\"INSERT INTO %0 (%1) VALUES (%2) \"";
    //private static final String updateSqlTemplate = "\"UPDATE %0 %1 \"";
    //private static final String selectSqlTemplate = "\"SELECT %0 FROM %1 \"";
    //private static final String deleteSqlTemplate = "\"DELETE FROM %0 \"";
    //private static final String whereSqlTemplate = "\"WHERE ? %% PRIMARY_KEY__ \"";
    //private static final String orderBySqlTemplate = "\"ORDER BY PRIMARY_KEY__ \" ";
}
