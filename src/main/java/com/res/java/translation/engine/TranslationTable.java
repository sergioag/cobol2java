package com.res.java.translation.engine;

import com.res.cobol.Main;
import com.res.common.RESContext;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.res.java.lib.Constants;
import com.res.java.translation.engine.Cobol2Java.ExpressionString;
import com.res.java.translation.symbol.SymbolConstants;
import com.res.java.translation.symbol.SymbolProperties;
import com.res.java.translation.symbol.SymbolTable;
import com.res.java.util.NameUtil;

public class TranslationTable {

    private static TranslationTable thiz = null;
    private static RESContext context = null;

    public static TranslationTable getInstance() {
        if (thiz == null) {
            context = Main.getContext();
            thiz = new TranslationTable();
        }
        return thiz;
    }

    public static void clear() {
        thiz = null;
        context = null;
    }

    private TranslationTable() {
    }
    //This is Holier than Holy... do not touch without knowing your prayers.
    private static final String[][] DOIDOP_CONVERSION =
            new String[][]{
        //To->    	BYTE						CHAR
        //To->    	SHORT						INTEGER						LONG
        //To->    	FLOAT						DOUBLE
        //From		BIGDECIMAL					STRING						CLASS
        /*BYTE*/{"%1", "(char) (%1)",
            "(short) (%1)", "(int) (%1)", "(long) (%1)",
            "(float) (%1)", "(double) (%1)",
            "new BigDecimal(%1)", "String.valueOf(%1)", "String.valueOf(%1).getBytes()",
            "%1"},
        /*CHAR*/ {"(byte) (%1)", "%1",
            "(short) (%1)", "(int) (%1)", "(long) (%1)",
            "(float) %1", "(double) %1",
            "new BigDecimal(%1)", "String.valueOf(%1)", "String.valueOf(%1).getBytes()",
            "%1"},
        /*SHORT*/ {"(byte) (%1)", "(char) (%1)",
            "%1", "%1", "%1",
            "(float) (%1)", "(double) (%1)",
            "new BigDecimal(%1)", "String.valueOf(%1)", "String.valueOf(%1).getBytes()",
            "%1"},
        /*INTEGER*/ {"(byte) (%1)", "(char) (%1)",
            "(short) (%1)", "%1", "%1",
            "(float) (%1)", "(double) (%1)",
            "new BigDecimal(%1)", "String.valueOf(%1)", "String.valueOf(%1).getBytes()",
            "%1"},
        /*LONG*/ {"(byte) (%1)", "(char) (%1)",
            "(short) (%1)", "(int) (%1)", "%1",
            "(float) (%1)", "(double) (%1)",
            "new BigDecimal(%1)", "String.valueOf(%1)", "String.valueOf(%1).getBytes()",
            "%1"},
        /*FLOAT*/ {"(byte) (%1)", "(char) (%1)",
            "(short) (%1)", "(int) (%1)", "(long) (%1)",
            "%1", "%1",
            "new BigDecimal(\"%1\")", "String.valueOf(%1)", "String.valueOf(%1).getBytes()",
            "%1"},
        /*DOUBLE*/ {"(byte) (%1)", "(char) (%1)",
            "(short) (%1)", "(int) (%1)", "(long) (%1)",
            "(float) (%1)", "%1",
            "new BigDecimal(\"%1\")", "String.valueOf(%1)", "String.valueOf(%1).getBytes()",
            "%1"},
        /*BIGDECIMAL*/ {"%1.byteValue()", "(char) %1.byteValue()",
            "%1.shortValue()", "%1.intValue()", "%1.longValue()",
            "%1.floatValue()", "%1.doubleValue()",
            "%1", "__formatPlainDecimal(%1)", "__formatPlainDecimal(%1).getBytes()",
            "%1"},
        /*STRING*/ {"Byte.parseByte(%1)", "__getChar(%1)",
            "FieldFormat.parseInt(%1)", "FieldFormat.parseInt(%1)", "FieldFormat.parseLong(%1)",
            "Float.parseFloat(%1)", "Double.parseDouble(%1)",
            "FieldFormat.parseNumber(%1)", "%1", "%1.getBytes()", "%1"},
        /*CLASS*/ {"%1[0]", "__getChar(%1)",
            "FieldFormat.parseInt(new String(%1))", "FieldFormat.parseInt(new String(%1))", "FieldFormat.parseLong(new String(%1))",
            "Float.parseFloat(new String(%1))", "Double.parseDouble(new String(%1))",
            "FieldFormat.parseNumber(new String(%1))", "new String(%1)", "%1",
            "%1"},
        /*OBJECT*/ {"(Byte)%1", "(Char) %1",
            "(Integer)%1", "(Integer)%1", "(Long)%1",
            "(Float)%1", "(Double)%1",
            "new BigDecimal(%1.toString())", "%1.toString()", "%1.toString().getBytes()",
            "%1"}
    };
    private static final String[] DOIDOP_OPERATION =
            new String[]{"_Math.add(%1,%2)", "_Math.subtract(%1,%2)", "_Math.multiply(%1,%2)", "_Math.divide(%1,%2)",
        "_Math.remainder(%1,%2)", "_Math.pow(%1,%2)"};
    private static final String[] DO_NATIVE_OP_OPERATION =
            new String[]{"%1 + %2", "%1 - %2", "%1 * %2", "%1 / %2",
        "%1 % %2", "Math.pow(%1,%2)"};
    public static final int ADD_OP = 0;
    public static final int SUBTRACT_OP = 1;
    public static final int MULTIPLY_OP = 2;
    public static final int DIVIDE_OP = 3;
    public static final int REMAINDER_OP = 4;
    public static final int POW_OP = 5;

    public ExpressionString doOp(int op, ExpressionString arg1, ExpressionString arg2) {
        ExpressionString s1 = arg1,s2 = arg2;
        arg1 = context.getCobol2Java().new ExpressionString(arg1); //Hack... Need to rework...
        arg2 = context.getCobol2Java().new ExpressionString(arg2); //Hack... Need to rework...
        if (op == DIVIDE_OP) {
            context.getCobol2Java().expressionType = Math.max(arg1.type, Constants.DOUBLE);
            arg1 = convertType(arg1, context.getCobol2Java().expressionType, arg1.type);
            arg1.type = context.getCobol2Java().expressionType;
        }
        if (arg1.type < Constants.INTEGER) {
            arg1 = convertType(arg1, Constants.INTEGER, arg1.type);
            arg1.type = Constants.INTEGER;
        }
        if (arg2.type < Constants.INTEGER) {
            arg2 = convertType(arg2, Constants.INTEGER, arg2.type);
            arg2.type = Constants.INTEGER;
        }
        if (op < 0 || op >= DOIDOP_OPERATION.length) {
            op = 0; //reset to add()
        }
        if (Math.max(arg1.type, arg2.type) > Constants.DOUBLE) {
            arg1.literal.replace(0, arg1.literal.length(), DOIDOP_OPERATION[op].replace("%1", arg1.toString()).replace("%2", arg2.toString()));
        } else {
            arg1.literal.replace(0, arg1.literal.length(), DO_NATIVE_OP_OPERATION[op].replace("%1", arg1.toString()).replace("%2", arg2.toString()));
        }
        context.getCobol2Java().expressionType = arg1.type = Math.max(arg1.type, arg2.type);
        return arg1;

    }

    public ExpressionString doOp(String opArg, ExpressionString arg1, ExpressionString arg2) {
        int op = 0;
        switch (opArg.trim().charAt(0)) {
            case '+':
                op = ADD_OP;
                break;
            case '-':
                op = SUBTRACT_OP;
                break;
            case '*':
                op = MULTIPLY_OP;
                break;
            case '/':
                op = DIVIDE_OP;
                break;
            case '%':
                op = REMAINDER_OP;
                break;
            default:
                op = POW_OP;
                break;
        }
        return doOp(op, arg1, arg2);
    }

    public void doIdOp(SymbolProperties id, String op) {
        if (context.getCobol2Java().expression.size() <= 0) {
            context.getCobol2Java().expression.push(context.getCobol2Java().new ExpressionString(id, id.getJavaType().getMaxScalingLength()));
            context.getCobol2Java().expressionType = context.getCobol2Java().expression.peek().type;
        } else {
            context.getCobol2Java().expression.push(
                    doOp(op, context.getCobol2Java().getExpression(), context.getCobol2Java().new ExpressionString(id, id.getJavaType().getMaxScalingLength())));
        }
    }
    public static Pattern NUMBER_PATTERN = Pattern.compile("(([\\-\\+\\*\\/%]?[0-9]*(\\.[0-9]+)?)|\\.[0-9]+)+");

    public void doLiteralOp(String lit, String op) {
        if (context.getCobol2Java().expression.size() <= 0) {
            context.getCobol2Java().expression.push(context.getCobol2Java().formatLiteral(lit));
        } else {
            ExpressionString arg2 = context.getCobol2Java().getExpression();
            ExpressionString arg1 = context.getCobol2Java().formatLiteral(lit);
            context.getCobol2Java().expression.push(doOp(op, arg2, arg1));
        }
    }

    public void doOp(SymbolProperties props, String op) {
        doIdOp(props, op);
    }

    public void doOp(String lit, String op) {
        doLiteralOp(lit, op);
    }

    public void doOp(ExpressionString exprString, String op) {
        if (context.getCobol2Java().expression.size() <= 0) {
            context.getCobol2Java().expression.push(exprString);
            context.getCobol2Java().expressionType = exprString.type;
        } else {
            ExpressionString arg2 = context.getCobol2Java().getExpression();
            ExpressionString arg1 = exprString;
            context.getCobol2Java().expression.push(
                    doOp(op, arg2, arg1));
        }
    }

    public void doOp(String exprString, int expressionType, String op) {
        doOp(context.getCobol2Java().new ExpressionString(exprString, expressionType), op);
    }

    public String getAssignString(SymbolProperties lhs, SymbolProperties rhs, boolean isAll) {
        return getAssignString(lhs, rhs, isAll, true, false);
    }

    public String getAssignString(SymbolProperties lhs, SymbolProperties rhs, boolean isAll, boolean addSemi) {
        return getAssignString(lhs, rhs, isAll, addSemi, false);
    }

    public String getAssignString(SymbolProperties lhs, SymbolProperties rhs, boolean isAll, boolean addSemi, boolean isSpecialMove) {

        int allLength = 0;
        if (isAll && lhs != null) {
            allLength = lhs.getLength();
        }

        ExpressionString exprString, lhsVarString;
        int lhsType, rhsType = 0;

        if (rhs == null) {
            exprString = context.getCobol2Java().new ExpressionString(context.getCobol2Java().expression.pop());
            if (exprString.type < 0) {
                exprString.type = context.getCobol2Java().expressionType;
            }
        } else {
            exprString = context.getCobol2Java().new ExpressionString(rhs);
        }
        rhsType = exprString.type;

        if (lhs == null && rhs == null) {//Create temporary variable assignment from the expression
            String tempVar = "temp" + new Integer(SymbolTable.tempVariablesMark++).toString().trim();
            exprString.literal.insert(0, SymbolConstants.get(context.getCobol2Java().expressionType) + " "
                    + tempVar + " = ").append(";");
            return exprString.toString();
        }
        boolean delayRHSTypeAsString = false, isJustRight = false;
        if (lhs == null) {//Used in file reads and writes
            lhsVarString = context.getCobol2Java().expression.pop();
            lhsVarString.literal.append("(%0)");
            lhsVarString.type = lhsType = context.getCobol2Java().expressionType;
        } else {//General assigns
            lhsType = lhs.getIdentifierType();

            if (isSpecialMove
                    && (isANumber(lhsType) && lhs.getDataCategory() == Constants.NUMERIC_EDITED && lhs.getIsFormat()
                    && isANumber(rhsType) && rhs == null)) {
                if (lhs.getIsFormat()) {
                    lhsVarString = context.getCobol2Java().new ExpressionString(NameUtil.getJavaName(lhs, true).replace("%0", NameUtil.getFormatName2(lhs, false)), lhsType);
                    delayRHSTypeAsString = true;
                } else {
                    lhsVarString = context.getCobol2Java().new ExpressionString(NameUtil.getJavaName(lhs, true), lhsType);
                }
            } else {
                lhsVarString = context.getCobol2Java().new ExpressionString(NameUtil.getJavaName(lhs, true), lhsType);
            }
        }


        if (rhs == null) {

            if (isSpecialMove) {

                if (!isANumber(lhsType) && isANumber(rhsType)) {
                    if (lhs.getDataCategory() == Constants.NUMERIC_EDITED) {
                        exprString = context.getCobol2Java().new ExpressionString(NameUtil.getFormatName2(lhs, false).replace("%0",
                                ((rhsType == Constants.FLOAT || rhsType == Constants.DOUBLE)
                                ? (convertType(exprString, Constants.BIGDECIMAL, rhsType).toString()) : exprString.toString())),
                                exprString.type = rhsType = Constants.STRING);
                        //rhsType=exprString.type;
                    } else {
                        if (exprString.raw != null
                                && context.getCobol2Java().numPattern.matcher(exprString.raw).matches()) {
                            exprString = context.getCobol2Java().new ExpressionString('\"' + exprString.raw + '\"', rhsType = Constants.STRING);
                        }
                        if (lhs.getIsFormat() && !isANumber(lhsType)) {
                            System.out.println(lhs.getDataName() + "," + exprString.raw);
                            exprString = context.getCobol2Java().new ExpressionString(NameUtil.getFormatName2(lhs, false).replace("%0", exprString.toString()), exprString.type);
                            delayRHSTypeAsString = true;
                        }
                    }
                } else if (lhsType == Constants.STRING && lhs.getIsFormat()) {
                    if (rhsType == Constants.FLOAT || rhsType == Constants.DOUBLE) {
                        exprString = context.getCobol2Java().new ExpressionString("new BigDecimal(\"" + exprString + "\")",
                                rhsType = Constants.BIGDECIMAL);
                    }
                    exprString = context.getCobol2Java().new ExpressionString(NameUtil.getFormatName2(lhs, false).replace("%0",
                            exprString.toString()));
                    delayRHSTypeAsString = true;
                } else;//exprString=context.getCobol2Java().expression.pop3();
            } else if (isANumber(rhsType) && isStringOrGroup(lhsType)) {
                if (lhs.getDataCategory() != Constants.NUMERIC_EDITED) {
                    exprString = context.getCobol2Java().new ExpressionString('\"' + exprString.raw + '\"', rhsType = Constants.STRING);
                } else {
                    if (context.getCobol2Java().numPattern.matcher(exprString.toString()).matches()) {
                        exprString = context.getCobol2Java().new ExpressionString(NameUtil.getFormatName2(lhs, false).replace("%0",
                                "new BigDecimal(\"" + exprString + "\")"),
                                rhsType = Constants.STRING);
                    } else {
                        exprString = context.getCobol2Java().new ExpressionString(NameUtil.getFormatName2(lhs, false).replace("%0",
                                convertType(exprString, Constants.BIGDECIMAL, rhsType).toString()),
                                rhsType = Constants.STRING);
                    }

                }
            } else;//exprString=context.getCobol2Java().expression.pop3();
        } else {
            if (isSpecialMove) {
                if (!isANumber(lhsType)) {
                    if (isANumber(rhsType)) {
                        if (rhs.getDataUsage() == Constants.DISPLAY) {
                            exprString = context.getCobol2Java().new ExpressionString(NameUtil.getJavaName(exprString.props, false,
                                    lhs.getDataCategory() != Constants.NUMERIC_EDITED && !lhs.getIsFormat()), rhsType = exprString.type);

                            isJustRight = true;
                            delayRHSTypeAsString = lhs.getDataCategory() != Constants.NUMERIC_EDITED && !lhs.getIsFormat();
                        }

                        if (rhs.getJavaType().getMaxScalingLength() != 0
                                && !delayRHSTypeAsString) {

                            exprString.setString(context.getCobol2Java().doScaleLiteral(rhs, exprString.toString(), true), Constants.BIGDECIMAL);

                        }

                        if (lhsType == Constants.STRING && lhs.getIsFormat()) {
                            exprString = context.getCobol2Java().new ExpressionString(NameUtil.getFormatName2(lhs, false).replace("%0",
                                    exprString.toString()), rhsType = Constants.STRING);
                        }
                        /*
                        else {
                        if(lhs.getDataCategory()!=Constants.NUMERIC_EDITED&&rhs.getDataUsage()==Constants.DISPLAY)
                        exprString=SymbolUtil.getInstance().getToStringOrGroupMove(rhs);
                        else
                        exprString=NameUtil.getJavaName(rhs,false);
                        if(lhs.getIsFormat()) {
                        exprString=NameUtil.getFormatName2(lhs,false).replace("%0",exprString);
                        } else
                        exprString="getProgram().normalizeString("+exprString+","+
                        lhs.getLength()+")";
                        }
                         * */

                    } else if (lhsType == Constants.STRING && rhsType == Constants.STRING) {
                        exprString = context.getCobol2Java().new ExpressionString(NameUtil.getFormatName2(lhs, false).replace("%0",
                                exprString.toString()), rhsType = Constants.STRING);
                    } else;//exprString=NameUtil.getJavaName(rhs,false);
                } else {//isANumber LHS
                    //exprString=NameUtil.getJavaName(rhs,false);
                    rhsType = Math.max(rhs.getIdentifierType(), rhsType);
                    if (!isANumber(rhsType) && lhs.getDataUsage() == Constants.DISPLAY) {
                        lhsVarString.type = lhsType = Constants.STRING;
                    } else if (lhsType == Constants.STRING && lhs.getIsFormat()) {
                        exprString = context.getCobol2Java().new ExpressionString(NameUtil.getFormatName2(lhs, false).replace("%0", exprString.toString()), rhsType);
                        delayRHSTypeAsString = true;
                    }
                }
            }
        }

        if (delayRHSTypeAsString) {
            exprString.type = rhsType = Constants.STRING;
        }

        if (isSpecialMove) {
            if (lhs.getJavaType().getMaxScalingLength() != 0) {

                if (lhs.getJavaType().getMaxScalingLength() < 0) {
                    exprString.literal.insert(0, "__scale(").append(',').append(String.valueOf(lhs.getJavaType().getMaxIntLength() - lhs.getJavaType().getMaxScalingLength())).append(')');
                } else {
                    exprString.literal.insert(0, "__scale(").append(',').append(
                            (-lhs.getJavaType().getMaxScalingLength())).append(')');
                }
                exprString.type = rhsType = Constants.BIGDECIMAL;
            }
        }

        if (lhsType == Constants.UNKNOWN || rhsType == Constants.UNKNOWN) {
            return "";
        }
        /*
        if(isSpecialMove&&!delayRHSTypeAsString&&
        ((isAnEditedNumber(lhs, lhsVarString.type)&&
        exprString.type==Constants.GROUP))) {
        exprString=context.getCobol2Java().new ExpressionString(SymbolUtil.getInstance().getFromStringOrGroupMove(lhs, exprString.toString()),exprString.type);
        lhsVarString.type=lhsType=Constants.STRING;
        context.getCobol2Java().expressionType=Math.max(lhsType,context.getCobol2Java().expressionType);
        } else
         *
         */
        {
            if (lhs.isJustifiedRight() || isJustRight) {
                exprString.literal = exprString.literal.insert(0, "__justifyRight(").append(",").append(String.valueOf(lhs.getLength())).append(")");
                exprString.type = rhsType = Constants.STRING;
            } else if ((isSpecialMove && isANumber(lhsType) && rhsType == Constants.STRING)) {
                exprString.literal = exprString.literal.insert(0, "__trimNumberToSize(").append(",").append(String.valueOf(lhs.getLength())).append(")");
                exprString.type = rhsType = Constants.STRING;
            }

            if (isAll && allLength > 0) {
                exprString.insert(0, "__all(").append(",").append(String.valueOf(allLength)).append(")");
                exprString.type = rhsType = Constants.STRING;
            }


            if (exprString.type != Constants.STRING || !delayRHSTypeAsString) {

                exprString = convertType(exprString, lhsType, rhsType);

                context.getCobol2Java().expressionType = Math.max(lhsType, context.getCobol2Java().expressionType);
            } else {
                if (!isANumber(lhsType) && rhs != null && rhs.getDataUsage() == Constants.DISPLAY) {
                    exprString = convertType(exprString, lhsType, Constants.STRING);
                }

                context.getCobol2Java().expressionType = Math.max(lhsType, context.getCobol2Java().expressionType);
            }

            exprString.replace(0, exprString.literal.length(), lhsVarString.literal.toString().replace("%0", exprString.toString()));
        }
        return exprString.append(addSemi ? ";" : "").toString();
    }

    private boolean isAString(int lhsType) {
        return lhsType == Constants.STRING;
    }

    private boolean isAnEditedNumber(SymbolProperties lhs, int lhsType) {
        return isANumber(lhsType) && (lhs.getDataUsage() == Constants.DISPLAY);
    }

    private boolean isStringOrGroup(int lhsType) {
        return lhsType > Constants.BIGDECIMAL;
    }

    private boolean isANumber(int rhsType) {
        return rhsType <= Constants.BIGDECIMAL && rhsType > Constants.CHAR;
    }

    //Conversion
    public String convertType(String exprString, int lhsType, int rhsType) {
        if ((rhsType == Constants.DOUBLE || rhsType == Constants.FLOAT) && lhsType == Constants.BIGDECIMAL) {
            if (exprString.indexOf("__function") < 0
                    && context.getCobol2Java().numPattern.matcher(exprString).matches()) {
                return "new BigDecimal(\"" + exprString + "\")";
            } else {
                return "new BigDecimal(" + exprString + ")";
            }
        }

        context.getCobol2Java().expressionType = lhsType;
        exprString = DOIDOP_CONVERSION[rhsType][lhsType].replace(
                "%1", exprString);
        return exprString;
    }

    //Conversion
    public ExpressionString convertType(ExpressionString exprString, int lhsType, int rhsType) {
        exprString.literal = new StringBuilder(convertType(exprString.toString(), lhsType, rhsType));
        exprString.type = lhsType;
        return exprString;
    }

    public String convertSymbolToStringType(SymbolProperties symbol) {
        context.getCobol2Java().expressionType = Constants.STRING;
        if (symbol.getIdentifierType() < Constants.STRING && symbol.getIsFormat()) {
            symbol.setIdentifierType(Constants.STRING);
            return NameUtil.getFormatName2(symbol, true).replace("%0",
                    NameUtil.getJavaName(symbol, false));
        } else {
            symbol.setIdentifierType(Constants.STRING);
            return DOIDOP_CONVERSION[symbol.getIdentifierType()][Constants.STRING].replace(
                    "%1", NameUtil.getJavaName(symbol, false));
        }

    }
    public static final int AND_CONDITION = 0;
    public static final int OR_CONDITION = 1;
    public static final int EQ_CONDITION = 2;
    public static final int NE_CONDITION = 3;
    public static final int LT_CONDITION = 4;
    public static final int GE_CONDITION = 5;
    public static final int GT_CONDITION = 6;
    public static final int LE_CONDITION = 7;
    public static final int POSITIVE_CONDITION = 0;
    public static final int NEGATIVE_CONDITION = 1;
    public static final int ZERO_CONDITION = 2;
    public static final int NOT_ZERO_CONDITION = 3;
    private static final int[] REVERSE_OP = new int[]{1, 0, 3, 2, 5, 4, 7, 6};
    private static final String[] DOID_NATIVE_CONDITION_OP =
            new String[]{"%1 && %2", "%1 || %2",
        "%1 == %2", "%1 != %2",
        "%1 < %2", "%1 >= %2",
        "%1 > %2", "%1 <= %2"};
    private static final String[] DOID_METHODS_CONDITION_OP =
            new String[]{"%1 && %2", "%1 || %2",
        "Compare.eq(%1,%2)", "Compare.ne(%1,%2)",
        "Compare.lt(%1,%2)", "Compare.ge(%1,%2)",
        "Compare.gt(%1,%2)", "Compare.le(%1,%2)"};
    private static final String[] DOID_LO_CONDITION_OP =
            new String[]{"Compare.loValues(%0)==0", "Compare.loValues(%0)!=0",
        "Compare.loValues(%0)<0", "Compare.loValues(%0)>=0",
        "Compare.loValues(%0)>0", "Compare.loValues(%0)<=0"};
    private static final String[] DOID_HI_CONDITION_OP =
            new String[]{"Compare.hiValues(%0)==0", "Compare.hiValues(%0)!=0",
        "Compare.hiValues(%0)<0", "Compare.hiValues(%0)>=0",
        "Compare.hiValues(%0)>0", "Compare.hiValues(%0)<=0"};
    private static final String[] DOID_QUOTE_CONDITION_OP =
            new String[]{"Compare.quotes(%0)==0", "Compare.quotes(%0)!=0",
        "Compare.quotes(%0)<0", "Compare.quotes(%0)>=0",
        "Compare.quotes(%0)>0", "Compare.quotes(%0)<=0"};
    private static final String[] DOID_SPACES_CONDITION_OP =
            new String[]{"Compare.spaces(%0)==0", "Compare.spaces(%0)!=0",
        "Compare.spaces(%0)<0", "Compare.spaces(%0)>=0",
        "Compare.spaces(%0)>0", "Compare.spaces(%0)<=0"};
    private static final int[] SINGLE_ARG_REVERSE_OP = new int[]{1, 0, 3, 2};
    private static final String[] DO_SINGLE_ARG_CONDITION_OP =
            new String[]{"Compare.isPositive(%0)", "Compare.isNegative(%0)",
        "Compare.isZero(%0)", "!Compare.isZero(%0)", "!(%0)"};

    public int getReverseOp(int op, boolean reverse) {
        if (op < 0 || op >= REVERSE_OP.length) {
            op = 0;
        }
        return (reverse ? REVERSE_OP[op] : op);
    }

    public String doCondition(int op, ExpressionString expr1, ExpressionString expr2, boolean doReverseCondition) {

        int l1, l2;
        if (expr1.props != null) {
            l1 = expr1.props.getLength();
        } else {
            l1 = expr1.length;
        }
        if (expr2.props != null) {
            l2 = expr2.props.getLength();
        } else {
            l2 = expr2.length;
        }
        int l = Math.max(l1, l2);
        if (expr1.isAll) {
            expr1.literal.insert(0, "__all(").append(',').append(l).append(')');
        }
        if (expr2.isAll) {
            expr2.literal.insert(0, "__all(").append(',').append(l).append(')');
        }

        if (expr1.type == Constants.FLOAT || expr1.type == Constants.DOUBLE) {
            expr1 = convertType(expr1, Constants.BIGDECIMAL, expr1.type);
        }
        if (expr2.type == Constants.FLOAT || expr2.type == Constants.DOUBLE) {
            expr2 = convertType(expr2, Constants.BIGDECIMAL, expr2.type);
        }

        if (op < 0 || op >= DOID_NATIVE_CONDITION_OP.length) {
            op = 2;//reset to ==
        }
        if ((expr1.toString().equals("\"\\\"\"") || expr2.toString().equals("\"\\\"\"")) && op >= 2) {

            op -= 2;
            if (expr1.toString().equals("\"\\\"\"")) {
                return DOID_QUOTE_CONDITION_OP[getReverseOp(op, getFigConstReverseCondition(op, doReverseCondition))].replace("%0", expr2.toString());
            } else {
                return DOID_QUOTE_CONDITION_OP[getReverseOp(op, doReverseCondition)].replace("%0", expr1.toString());
            }
        }
        if ((expr1.toString().equals("__function().MAX_VALUE()") || expr2.toString().equals("__function().MAX_VALUE()")) && op >= 2) {

            op -= 2;
            if (expr1.toString().equals("__function().MAX_VALUE()")) {
                return DOID_HI_CONDITION_OP[getReverseOp(op, getFigConstReverseCondition(op, doReverseCondition))].replace("%0", expr2.toString());
            } else {
                return DOID_HI_CONDITION_OP[getReverseOp(op, doReverseCondition)].replace("%0", expr1.toString());
            }
        }
        if ((expr1.toString().equals("__function().MIN_VALUE") || expr2.toString().equals("__function().MIN_VALUE()")) && op >= 2) {

            op -= 2;
            if (expr1.toString().equals("__function().MIN_VALUE()")) {
                return DOID_LO_CONDITION_OP[getReverseOp(op, getFigConstReverseCondition(op, doReverseCondition))].replace("%0", expr2.toString());
            } else {
                return DOID_LO_CONDITION_OP[getReverseOp(op, doReverseCondition)].replace("%0", expr1.toString());
            }
        }
        if ((expr1.toString().equals("\" \"") || expr2.toString().equals("\" \"")) && op >= 2) {

            op -= 2;
            if (expr1.toString().equals("\" \"")) {
                return DOID_SPACES_CONDITION_OP[getReverseOp(op, getFigConstReverseCondition(op, doReverseCondition))].replace("%0", expr2.toString());
            } else {
                return DOID_SPACES_CONDITION_OP[getReverseOp(op, doReverseCondition)].replace("%0", expr1.toString());
            }
        }
        if (op < 0 || op >= DOID_NATIVE_CONDITION_OP.length) {
            op = 2;//reset to ==
        }
        if (Math.max(expr1.type, expr2.type) < Constants.BIGDECIMAL) {
            if ((expr1.type == Constants.CHAR || expr2.type == Constants.CHAR) && !(expr1.type == Constants.CHAR && expr2.type == Constants.CHAR)) {
                if (expr1.type == Constants.CHAR) {
                    return DOID_NATIVE_CONDITION_OP[(doReverseCondition ? REVERSE_OP[op] : op)].replace("%1", expr1.toString() + " - \'0\'").replace("%2", expr2.toString());
                } else {
                    return DOID_NATIVE_CONDITION_OP[(doReverseCondition ? REVERSE_OP[op] : op)].replace("%1", expr1.toString()).replace("%2", expr2.toString() + " - \'0\'");
                }
            } else {
                return DOID_NATIVE_CONDITION_OP[(doReverseCondition ? REVERSE_OP[op] : op)].replace("%1", expr1.toString()).replace("%2", expr2.toString());
            }
        } else {
            if (expr1.type == Constants.OBJECT) {
                expr1 = convertType(expr1, Constants.STRING, Constants.OBJECT);
            }
            if (expr2.type == Constants.OBJECT) {
                expr2 = convertType(expr2, Constants.STRING, Constants.OBJECT);
            }
            return DOID_METHODS_CONDITION_OP[(doReverseCondition ? REVERSE_OP[op] : op)].replace("%1", expr1.toString()).replace("%2", expr2.toString());
        }
    }

    private boolean getFigConstReverseCondition(int op, boolean doReverseCondition) {
        if (op == 0 || op == 0) {
            return doReverseCondition;
        } else {
            return !doReverseCondition;
        }
    }

    public ExpressionString doCondition2(int op, ExpressionString expr1, ExpressionString expr2, boolean doReverseCondition) {
        expr1.literal = new StringBuilder(doCondition(op, expr1, expr2, doReverseCondition));
        expr1.setLength(Math.max(expr1.length, expr2.length));
        return expr1;
    }

    public String doCondition(int op, ExpressionString expr, boolean doReverseCondition) {
        if (op < 0 || op >= DO_SINGLE_ARG_CONDITION_OP.length) {
            op = 0;//reset to isPositive()
        }
        return DO_SINGLE_ARG_CONDITION_OP[(doReverseCondition ? SINGLE_ARG_REVERSE_OP[op] : op)].replace("%0", expr.toString());
    }

    public ExpressionString doCondition2(int op, ExpressionString expr1, boolean doReverseCondition) {
        expr1.literal = new StringBuilder(doCondition(op, expr1, doReverseCondition));
        return expr1;
    }
}
