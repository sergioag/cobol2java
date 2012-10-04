package com.res.java.lib;

public class RunTimeUtil {

    private static RunTimeUtil runTimeUtil = null;

    private RunTimeUtil() {
    }

    public static RunTimeUtil getInstance() {
        if (runTimeUtil == null) {
            runTimeUtil = new RunTimeUtil();
        }
        return runTimeUtil;
    }

    public String stripQuotes(String in) {
        return stripQuotes(in, true);
    }

    public String stripQuotes(String in, boolean trim) {
        if ((in = in.trim()).length() > 0 && (in.charAt(0) == '\'' || in.charAt(0) == '\"')) {
            if (in.lastIndexOf(in.charAt(0)) > 0) {
                in = in.substring(1, in.lastIndexOf(in.charAt(0)));
            }
            if (trim) {
                return in.trim();
            }
        }
        return in;
    }

    public boolean isWhiteSpaces(char c) {
        return (c == ' ' || c == '\t' || c == '\r' || c == '\n' || c == '\f');
    }

    public String stripTrailingBlanks(String in) {
        if (in == null) {
            return "";
        }
        String s;
        return (in.substring(0, (in.indexOf(s = in.trim()) + s.length())));
    }

    public void reportError(String s, boolean exit) {
        System.out.println(s);
        if (exit) {
            System.out.println("Done.");
            System.exit(1);
        }
    }
    
    private static final java.util.regex.Pattern ZERO_PATTERN = java.util.regex.Pattern.compile("[-+]?[0]*(\\.[0]+)?");

    //Input is a valid number literal
    public String stripLeadingZeros(String lit) {

        lit = lit.replaceAll("[, ]", "");

        if (ZERO_PATTERN.matcher(lit).matches()) {
            return "0";
        }

        int i = 0;
        int sign = 0;
        if (lit.charAt(0) == '+') {
            i = 1;
        } else if (lit.charAt(0) == '-') {
            i = 1;
            sign = -1;
        }

        do {
            if (lit.charAt(i) != '0') {
                break;
            }
            ++i;
        } while (i < lit.length());

        lit = lit.substring(i);

        if (lit.charAt(0) == '.') {
            lit = '0' + lit;
        }

        if (sign < 0) {
            lit = '-' + lit;
        }

        return lit;

    }
}
