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
import java.math.MathContext;

public class Decimal {
	
	public static BigDecimal add(double a, double b) {
	    return add (new BigDecimal (a).round(MathContext.DECIMAL32), new BigDecimal (b).round(MathContext.DECIMAL32));
	}

	public static BigDecimal add(BigDecimal a, double b) {
	    return add (a, new BigDecimal (b).round(MathContext.DECIMAL32));
	}

	public static BigDecimal add(double a, BigDecimal b) {
	    return add (new BigDecimal (a).round(MathContext.DECIMAL32), b);
	}

	public static BigDecimal add(BigDecimal a, BigDecimal b) {
	    return (((a==null)?BigDecimal.ZERO:a).add((b==null)?BigDecimal.ZERO:b)).round(MathContext.DECIMAL32);
	}
	
	public static BigDecimal subtract(double a, double b) {
	    return subtract (new BigDecimal (a).round(MathContext.DECIMAL32), new BigDecimal (b).round(MathContext.DECIMAL32));
	}

	public static BigDecimal subtract(BigDecimal a, double b) {
	    return subtract (a, new BigDecimal (b).round(MathContext.DECIMAL32));
	}

	public static BigDecimal subtract(double a, BigDecimal b) {
	    return subtract (new BigDecimal (a).round(MathContext.DECIMAL32), b);
	}

	public static BigDecimal subtract(BigDecimal a, BigDecimal b) {
	    return (((a==null)?BigDecimal.ZERO:a).subtract((b==null)?BigDecimal.ZERO:b)).round(MathContext.DECIMAL32);
	}

	public static BigDecimal multiply(double a, double b) {
	    BigDecimal bd = new BigDecimal (a).round(MathContext.DECIMAL32);
	    return multiply (bd, b);
	}

	public static BigDecimal multiply(BigDecimal a, double b) {
	    return multiply (a, new BigDecimal (b).round(MathContext.DECIMAL32));
	}

	public static BigDecimal multiply(double a, BigDecimal b) {
	    return multiply (new BigDecimal (a).round(MathContext.DECIMAL32), b);
	}

	public static BigDecimal multiply(BigDecimal a, BigDecimal b) {
	    return (((a==null)?BigDecimal.ZERO:a).multiply((b==null)?BigDecimal.ZERO:b)).round(MathContext.DECIMAL32);
	}


	public static BigDecimal divide(double a, double b) {
	    BigDecimal bd = new BigDecimal (a).round(MathContext.DECIMAL32);
	    return divide (bd, b);
	}

	public static BigDecimal divide(BigDecimal a, double b) {
	    BigDecimal bd = new BigDecimal (b).round(MathContext.DECIMAL32);;
	    return divide (a, bd);
	}

	public static BigDecimal divide(double a, BigDecimal b) {
	    BigDecimal bd = new BigDecimal (a).round(MathContext.DECIMAL32);;
	    return divide (bd, b);
	}

	public static BigDecimal divide(BigDecimal a, BigDecimal b) {
	    return new BigDecimal((((a==null)?0:a.doubleValue())/((b==null)?0:b.doubleValue())));
	}

	public static BigDecimal remainder(double a, double b) {
	    return remainder (new BigDecimal (a).round(MathContext.DECIMAL32), new BigDecimal (b).round(MathContext.DECIMAL32));
	}

	public static BigDecimal remainder(BigDecimal a, double b) {
	    return remainder (a, new BigDecimal (b).round(MathContext.DECIMAL32));
	}

	public static BigDecimal remainder(double a, BigDecimal b) {
	    return remainder ( new BigDecimal (a).round(MathContext.DECIMAL32),b);
	}

	public static BigDecimal remainder(BigDecimal a, BigDecimal b) {
	    return (((a==null)?BigDecimal.ZERO:a).remainder((b==null)?BigDecimal.ZERO:b)).round(MathContext.DECIMAL32);
	}
	
	public static BigDecimal pow(double a, double b) {
	    return new BigDecimal (Math.pow(a,b)).round(MathContext.DECIMAL32);
	}

	public static BigDecimal pow(BigDecimal a, double b) {
	    return new BigDecimal (Math.pow((a==null)?0:a.doubleValue(),b)).round(MathContext.DECIMAL32);
	}

	public static BigDecimal pow(double a, BigDecimal b) {
	    return new BigDecimal (Math.pow(a,(b==null)?0:b.doubleValue())).round(MathContext.DECIMAL32);
	}

	public static BigDecimal pow(BigDecimal a, BigDecimal b) {
	    return new BigDecimal (Math.pow((a==null)?0:a.doubleValue(),(b==null)?0:b.doubleValue())).round(MathContext.DECIMAL32);
	}
	
	public static BigDecimal minus(double a) {
	    return new BigDecimal (-(a)).round(MathContext.DECIMAL32);
	}

	public static BigDecimal minus(BigDecimal a) {
	    return BigDecimal.ZERO.subtract(a);
	}
	
}
