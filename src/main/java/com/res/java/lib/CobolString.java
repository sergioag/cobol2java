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

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.res.java.lib.exceptions.OverflowException;

public class CobolString {

    private StringBuilder value;

    public CobolString(String v) {
        value = new StringBuilder(v);
        reset();
    }

    public CobolString(byte[] v) {
        this(new String(v));
        reset();
    }

    public CobolString(int v) {
        value = new StringBuilder().append(v);
        reset();
    }

    public CobolString(char v) {
        value = new StringBuilder().append(v);
        reset();
    }

    public CobolString(long v) {
        value = new StringBuilder().append(v);
        reset();
    }

    public CobolString(BigDecimal v) {
        value = new StringBuilder().append(v.toPlainString());
        reset();
    }

    public CobolString(StringBuilder v) {
        value = v;
        reset();
    }

    @Override
    public String toString() {
        return value.toString();
    }

    public CobolString append(CobolString cobolString) {
        value = value.append(cobolString.toString());
        return this;
    }

    public CobolString append(String cobolString) {
        value = value.append(cobolString);
        return this;
    }

    public CobolString append(char cobolString) {
        value = value.append(cobolString);
        return this;
    }
    private boolean characters, all, leading, first;
    private int beforeIndex = 1, afterIndex = 0;
    private int tallyCount = 0;

    public final void reset() {
        characters = false;
        all = leading = first = false;
        if (value != null) {
            beforeIndex = value.length();
        }
        tallyCount = 0;
    }

    private String applyBeforeAfter() {
        return value.toString().substring(afterIndex, beforeIndex);
    }

    public CobolString tally() {
        tallyCount += applyBeforeAfter().length();
        return this;
    }

    public CobolString tally(String thisStr) {
        String val = applyBeforeAfter();
        int i = 0, j = 0;
        if (characters) {
            return tally();
        } else if (all) {
            do {
                if ((j = val.indexOf(thisStr, j)) < 0) {
                    break;
                }
                j = j + thisStr.length();
                ++i;
            } while (j < val.length());
        } else if (leading) {
            int k = 0;
            do {
                if ((j = val.indexOf(thisStr, k)) != k) {
                    break;
                }
                i++;
                k = j + thisStr.length();
            } while (k < val.length());
        }
        tallyCount += i;
        return this;
    }

    public int tallyCount() {
        return tallyCount;
    }

    public int length() {
        return value.length();
    }

    public CobolString before(String beforeStr) {
        beforeIndex = (((i = value.toString().indexOf(beforeStr)) < 0) ? 0 : i);
        return this;
    }

    public CobolString before(char beforeStr) {
        beforeIndex = (((i = value.toString().indexOf(beforeStr)) < 0) ? 0 : i);
        return this;
    }

    public CobolString after(String afterStr) {
        afterIndex = (((i = value.indexOf(afterStr)) < 0)
                ? value.length() : (i + afterStr.length()));
        return this;
    }

    public CobolString after(char afterStr) {
        afterIndex = (((i = value.toString().indexOf(afterStr)) < 0)
                ? value.length() : (i + 1));
        return this;
    }

    public CobolString replace(String byStr) {
        value.replace(afterIndex, beforeIndex, applyBeforeAfter().replaceAll(".", byStr));
        return this;
    }

    public CobolString replace(char byStr) {
        return replace(String.valueOf(byStr));
    }

    public CobolString replace(String replaceStr, String byStr) {
        String val = applyBeforeAfter();
        if (all) {
            val = val.replace(replaceStr, byStr);
        } else if (leading) {
            StringBuilder val2 = new StringBuilder(val);
            int j = 0, k = 0;
            i = 0;
            do {
                if ((j = val2.indexOf(replaceStr, k)) != k) {
                    break;
                }
                val2 = val2.replace(k, k + replaceStr.length(), byStr);
                k = j + byStr.length();
            } while (k < val2.length());
            val = val2.toString();
        } else if (first) {
            val = val.replaceFirst(replaceStr, byStr);
        }
        value.replace(afterIndex, beforeIndex, val);
        return this;
    }

    public CobolString replace(char replaceStr, char byStr) {
        return replace(String.valueOf(replaceStr), String.valueOf(byStr));
    }

    public CobolString replace(String replaceStr, char byStr) {
        return replace(replaceStr, String.valueOf(byStr));
    }

    public CobolString replace(char replaceStr, String byStr) {
        return replace(String.valueOf(replaceStr), byStr);
    }

    public CobolString first() {
        first = true;
        all = false;
        leading = false;
        characters = false;
        return this;
    }

    public CobolString leading() {
        all = false;
        leading = true;
        characters = false;
        first = false;
        return this;
    }

    public CobolString all() {
        all = true;
        leading = false;
        characters = false;
        first = false;
        return this;
    }

    public CobolString characters() {
        characters = true;
        all = false;
        leading = false;
        first = false;
        return this;
    }

    public CobolString convert(String from, String to) {
        if (value != null) {
            int j;
            for (i = 0; i < value.length(); ++i) {
                if ((j = from.indexOf(value.charAt(i))) >= 0) {
                    value.setCharAt(i, to.charAt(j));
                }
            }
        }
        return this;
    }
    int i = 0;

    public String refMod(String string, int j, int k) {
        return value.replace(j, k, string).toString();
    }

    public String refMod(int string, int j, int k) {
        value.replace(j, k, String.valueOf(string));
        return this.toString();
    }

    public String refMod(long string, int j, int k) {
        value.replace(j, k, String.valueOf(string));
        return this.toString();
    }

    public String refMod(char _char, int j, int k) {
        value.replace(j, k, String.valueOf(_char));
        return this.toString();
    }

    public String refMod(float string, int j, int k) {
        value.replace(j, k, String.valueOf(string));
        return this.toString();
    }

    private String delimBy = null;
    private Matcher matcher = null;
    private int prevMatchStart = 0;

    public CobolString delimitedBy(String dlm) {
        if (dlm != null) {
            dlm = escapeRegEx(dlm);
            if (delimBy == null) {
                delimBy = dlm;
            } else {
                delimBy += "|" + dlm;
            }
        }
        return this;
    }

    public CobolString delimitedBy(String dlm, boolean isAll) {
        if (dlm != null) {
            if (!isAll) {
                return delimitedBy(dlm);
            }
            dlm = '(' + escapeRegEx(dlm) + ")+";
            if (delimBy == null) {
                delimBy = dlm;
            } else {
                delimBy += "|" + dlm;
            }
        }
        return this;
    }

    public CobolString unString() {
        prevMatchStart = unstringTally = 0;
        if (value == null || value.length() <= 0 || delimBy == null) {
            return this;
        }
        matcher = Pattern.compile(delimBy).matcher(value.toString());
        prevMatchStart = 0;
        return this;
    }
    private String lastUnstring = null;
    private String lastUnstringDelim = null;
    private int unstringTally = 0;

    public String getUnString() throws OverflowException {
        lastUnstring = null;
        if (delimBy == null) {
            if (prevMatchStart < 0 || prevMatchStart >= value.length()) {
                throw new OverflowException("Unstring not initialized or has no more string.");
            }
            lastUnstring = value.substring(prevMatchStart++, prevMatchStart);
        } else {
            if (matcher != null && matcher.find()) {
                lastUnstringDelim = value.substring(matcher.start(), matcher.end());
                lastUnstring = value.substring(prevMatchStart, matcher.start());
                prevMatchStart = matcher.end() + 1;
            } else {
                matcher = null;
                lastUnstringDelim = "";
                lastUnstring = value.substring(prevMatchStart);
                prevMatchStart = value.length();
            }
        }
        unstringTally++;
        return lastUnstring;
    }

    public int getUnStringCount() throws OverflowException {
        if (delimBy == null || lastUnstring == null) {
            throw new OverflowException("Unstring not initialized correctly or has no more string.");
        }
        return lastUnstring.length();
    }

    public String getUnStringDelimiter() throws OverflowException {
        if (delimBy == null || lastUnstringDelim == null) {
            throw new OverflowException("Unstring not initialized correctly or has no more string.");
        }
        return lastUnstringDelim;
    }

    public int getUnStringTally() {
        return unstringTally;
    }

    private static String escapeRegEx(String regex) {
        return regex.replace("\\", "\\\\").replace("*", "\\*").replace(".", "\\.").replace("(", "\\(").
                replace(")", "\\)").replace("-", "\\-").replace("\"", "\\\"").replace("'", "\\\'").
                replace("+", "\\+");
    }

    public CobolString delimitedBy(char j) {
        return delimitedBy(String.valueOf(j));
    }

    public CobolString delimitedBy(char j, boolean isAll) {
        return delimitedBy(String.valueOf(j), isAll);
    }

    public CobolString delimitedBy(int j) {
        return delimitedBy(String.valueOf(j));
    }

    public CobolString delimitedBy(int j, boolean isAll) {
        return delimitedBy(String.valueOf(j), isAll);
    }

    public void assertOverflow(int j) throws OverflowException {
        if (getUnStringTally() <= j) {
            throw new OverflowException("Overflow in Unstring of " + value + " occurred.");
        }
    }

    public int getUnStringPointer() {
        return prevMatchStart;
    }

    public String refMod(double string, int j, int k) {
        value.replace(j, k, String.valueOf(string));
        return this.toString();
    }

    public String refMod(BigDecimal string, int j, int k) {
        value.replace(j, k, string.toPlainString());
        return this.toString();
    }

    public String refMod(byte[] bytes, int j, int k) {
        value.replace(j, k, new String(bytes));
        return this.toString();
    }

    public String substring(int j, int k) {
        return value.substring(j, k);
    }
    
    public String substring(int j) {
        return value.substring(j);
    }
}
