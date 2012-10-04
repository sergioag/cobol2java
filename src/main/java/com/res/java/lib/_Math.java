package com.res.java.lib;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class _Math {

	
	public static int add(int arg1,int arg2) {
		return arg1+arg2;
	}
	public static long add(long arg1,long arg2) {
		return arg1+arg2;
	}
	public static double add(double arg1,double arg2) {
		return arg1+arg2;
	}
	public static BigDecimal add(BigDecimal arg1,double arg2) {
		return arg1.add(getBigDecimal(arg2)).setScale(18,RoundingMode.DOWN);
	}
	public static BigDecimal add(double arg1,BigDecimal arg2) {
		return arg2.add(getBigDecimal(arg1)).setScale(18,RoundingMode.DOWN);
	}
	public static BigDecimal add(BigDecimal arg1,BigDecimal arg2) {
		return arg2.add(arg1).setScale(18,RoundingMode.DOWN);
	}
	
	
	public static double subtract(double arg1,double arg2) {
		return arg1-arg2;
	}
	public static BigDecimal subtract(BigDecimal arg1,double arg2) {
		return arg1.subtract(getBigDecimal(arg2)).setScale(18,RoundingMode.DOWN);
	}
	public static BigDecimal subtract(double arg1,BigDecimal arg2) {
		return getBigDecimal(arg1).subtract(arg2).setScale(18,RoundingMode.DOWN);
	}
	public static BigDecimal subtract(BigDecimal arg1,BigDecimal arg2) {
		return arg1.subtract(arg2).setScale(18,RoundingMode.DOWN);
	}
	public static int subtract(int arg1,int arg2) {
		return arg1-arg2;
	}
	public static long subtract(long arg1,long arg2) {
		return arg1-arg2;
	}

	public static int multiply(int arg1,int arg2) {
		return arg1*arg2;
	}
	public static long multiply(long arg1,long arg2) {
		return arg1*arg2;
	}
	public static double multiply(double arg1,double arg2) {
		return multiply(getBigDecimal(arg1),getBigDecimal(arg2)).doubleValue();
	}
	public static BigDecimal multiply(BigDecimal arg1,long arg2) {
		return multiply(arg1,getBigDecimal(arg2));
	}
	public static BigDecimal multiply(BigDecimal arg1,double arg2) {
		return multiply(arg1,getBigDecimal(arg2));
	}
	public static BigDecimal multiply(double arg1,BigDecimal arg2) {
		return multiply(arg2,getBigDecimal(arg1));
	}
	public static BigDecimal multiply(BigDecimal arg1,BigDecimal arg2) {
		return arg2.multiply(arg1).setScale(18,RoundingMode.DOWN);
	}

	public static double divide(double arg1,double arg2) {
		return divide(getBigDecimal(arg1),getBigDecimal(arg2)).doubleValue();
	}
	public static BigDecimal divide(BigDecimal arg1,double arg2) {
		return  divide(arg1,getBigDecimal(arg2));
	}
	public static BigDecimal divide(double arg1,BigDecimal arg2) {
		return  divide(getBigDecimal(arg1),arg2);
	}
	public static BigDecimal divide(BigDecimal arg1,BigDecimal arg2) {
		return arg1.divide(arg2, arg1.scale()+arg2.scale(),RoundingMode.DOWN);
	}
	public static int divide(int arg1,int arg2) {
		return arg1/arg2;
	}
	public static long divide(long arg1,long arg2) {
		return arg1/arg2;
	}

	public static double remainder(double arg1,double arg2) {
		return arg1%arg2;
	}
	public static BigDecimal remainder(BigDecimal arg1,double arg2) {
		return arg1.remainder(getBigDecimal(arg2)).setScale(18,RoundingMode.DOWN);
	}
	public static BigDecimal remainder(double arg1,BigDecimal arg2) {
		return arg2.remainder(getBigDecimal(arg1)).setScale(0,RoundingMode.DOWN);
	}
	public static BigDecimal remainder(BigDecimal arg1,BigDecimal arg2) {
		return arg2.remainder(arg1).setScale(18,RoundingMode.DOWN);
	}
	public static int remainder(int arg1,int arg2) {
		return arg1%arg2;
	}
	public static long remainder(long arg1,long arg2) {
		return arg1%arg2;
	}

	public static int pow(int arg1,int arg2) {
		return (int)java.lang.Math.pow(arg1,arg2);
	}
	public static long pow(long arg1,long arg2) {
		return (long)java.lang.Math.pow(arg1,arg2);
	}
	public static double pow(double arg1,double arg2) {
		return java.lang.Math.pow(arg1,arg2);
	}
        public static BigDecimal pow(BigDecimal arg1,int arg2) {
		return arg1.pow(arg2);
	}
	public static BigDecimal pow(BigDecimal arg1,BigDecimal arg2) {
		return arg1.pow(arg2.intValue()).setScale(18,RoundingMode.DOWN);
	}
	public static BigDecimal getBigDecimal(double arg2) {
		return new BigDecimal(String.valueOf(arg2)).setScale(18,RoundingMode.DOWN);
	}
	
}
