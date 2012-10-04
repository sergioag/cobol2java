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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;


/**
 * Base class for all 01 Level Data.
 * Must be inherited by classes representing 01 Levels or equivalent.
 * Extends CobolBytes which is a bytes stream in Cobol format.
 * Has support for non-traditional  <pre><code>compare</code><pre>, <pre><code>equals</code><pre>
 * Check continuation line for quote-quote.
 */
public class CobolBean extends CobolBytes implements CobolFileBean {

		protected CobolBean() {
		}
		
		private Program __program=null;

		public CobolBean(int byteCount) {
			super(byteCount);
		}
		    
		public CobolBean(CobolBytes bytes) {
			super(bytes);
		}
		
		public CobolBean(CobolBytes bytes,int off,int len) {
			super(bytes,off,len);
		}
		
		public CobolBean(byte[] bytes) {
			super(bytes);
		}
		
	    public boolean equals(long o1,byte[] o2) {
	        for(byte rhs:o2)
	        	if(o1!=rhs)return false;
	        return true;
	    }
	
	    public boolean equals(byte[] o1,long o2) {
	        for(byte rhs:o1)
	        	if(o2!=rhs)return false;
	        return true;
	    }
	
	    public boolean equals(String o1,byte[] o2) {
	    	   return equals(o2,o1);
	    }
	
	    public boolean equals(String o1,char o2) {
	    	   return Compare.eq(o1,o2);
	    }
	    
	    public boolean equals(char o1,String o2) {
	    	   return Compare.eq(o1,o2);
	    }
	    
	    public boolean equals(byte[] o1,char o2) {
	    	   return Compare.eq(o1,o2);
	    }
	    
	    public boolean equals(char o1,byte[] o2) {
	    	   return Compare.eq(o1,o2);
	    }
	    
	    
	    private boolean couldBeFigurativeConstant(String str) {
	    	if(str.length()==1&&str.charAt(0)==' ') return true;
	    	return false;
	    }
	    
	    public boolean equals(byte[] o1,String o2) {
	    	if(couldBeFigurativeConstant(o2)) {
	    		for(int i=0;i<o1.length;++i)
	    			if(o1[i]!=o2.charAt(0))
	    				return false;
	    		return true;
	    	}
	        return new String(o1).equals(o2);
	    }
	    
	    public boolean equals(String o1,ArrayList<String> o2) {
	        for(String rhs:o2)
	        	if(!o1.equalsIgnoreCase(rhs))return false;
	        return true;
	    }
	
       public boolean equals(String o1,int o2) {
        	return equals(o1.getBytes(),o2);
        }

        public boolean equals(byte[] o1,int o2) {
            for(byte b:o1)
            	if(b!=o2)
            		return false;
            return true;
        }
        public boolean equals(char o1,String... o2) {
	        for(String rhs:o2)
                if(Compare.eq(o1,rhs))return true;
	        return false;
	    } 
	    public boolean equals(String o1,String... o2) {
	        for(String rhs:o2)
                if(o1.trim().equalsIgnoreCase(rhs.trim()))return true;
	        return false;
	    }
	
	    public boolean equals(BigDecimal o1,BigDecimal... o2) {
	        for(BigDecimal rhs:o2)
                if(o1.equals(rhs))return true;
	        return false;
	    }
	
	    public boolean equals(BigDecimal o1,double... o2) {
	        double o1d=o1.doubleValue();
	        for(double rhs:o2)
                if(o1d==rhs)return true;
	        return false;
	    }
	    
	    public boolean equals(double o1,double... o2) {
	        for(double rhs:o2)
	                if(o1==rhs)return true;
	        return false;
	    }

	    public boolean equalsRange(String o1,String range1,String range2) {
	                return o1.compareTo(range1)>=0&&o1.compareTo(range2)<=0;
	    }
	
	    public boolean equalsRange(char o1,String range1,String range2) {
            return Compare.ge(o1,range1)&&Compare.le(o1,range2);
	    }

	    public boolean equalsRange(BigDecimal o1,BigDecimal range1,BigDecimal range2) {
	                return o1.compareTo(range1)>=0&&o1.compareTo(range2)<=0;
	    }
	
	    public boolean equalsRange(BigDecimal o1,double range1,double range2) {
	                return o1.compareTo(new BigDecimal(range1))>=0&&o1.compareTo(new BigDecimal(range2))<=0;
	    }
	
	    public boolean equalsRange(double o1,double range1,double range2) {
	                return o1>=range1&&o1<=range2;
	    }

	    protected String __all(long aInt,int cnt) {
	    	String aStr=String.valueOf(aInt);
	    	StringBuffer ret=new StringBuffer(aStr);
	    	int partial=cnt%aStr.length();cnt=cnt/aStr.length();
	    	for(;cnt-1>0;--cnt) ret.append(aStr);
	    	ret.append(aStr.substring(0,partial));
	    	return ret.toString();
	    }
	    
	    protected String __all(String aStr,int cnt) {
	    	StringBuffer ret=new StringBuffer(aStr);
	    	int partial=cnt%aStr.length();cnt=cnt/aStr.length();
	    	for(;cnt-1>0;--cnt) ret.append(aStr);
	    	ret.append(aStr.substring(0,partial));
	    	return ret.toString();
	    }
	    
	    protected String __all(byte[] bs,int cnt) {
	    	byte[] ret=new byte[cnt];
	    	for(int i=0;i<cnt;++i)
	    		ret[i]=bs[i%bs.length];
	    	return new String(ret);
	    }
	    
	    protected String __padZeros(long aStr,int cnt) {
	    	StringBuffer ret=new StringBuffer().append(aStr);
	    	if(ret.length()>=cnt)return ret.toString();
	    	for(int i=cnt-ret.length();i>0;--i) ret.append('0');
	    	return ret.toString();
	    }
	    
	    protected String __justifyRight(String aStr,int cnt) {
	    	return super.normalizeString(aStr, cnt,true);
	    }
	    private String getZeros(int cnt) {
	    	StringBuffer ret=new StringBuffer();
	    	for(int i=0;i<cnt;++i) {
	    		ret.append('0');
	    	}
	    	return ret.toString();
	    }
	    protected String __justifyRight(String aStr,int cnt,int scale) {
	    	
	    	return super.normalizeString(aStr+getZeros(scale), cnt,true);
	    }
	    
	    protected String __justifyRight(byte[] aStr,int cnt,int scale) {
	    	
	    	return super.normalizeString(new String(aStr)+getZeros(scale), cnt,true);
	    }
	    
	    protected String __justifyRight(byte[] bs,int cnt) {
	    	return super.normalizeString(new String(bs),cnt,true);
	    }
	    
	    protected String __justifyRight(int aStr,int cnt,int scale) {
	    	return super.normalizeString(String.valueOf(__padZeros(aStr,cnt))+getZeros(scale), cnt,true);
	    }

	    protected String __justifyRight(int aStr,int cnt) {
	    	return super.normalizeString(String.valueOf(__padZeros(aStr,cnt)), cnt,true);
	    }
	    
	    protected String __justifyRight(long aStr,int cnt) {
	    	return super.normalizeString(String.valueOf(__padZeros(aStr,cnt)), cnt,true);
	    }
	    
	    protected String __justifyRight(long aStr,int cnt,int scale) {
	    	return super.normalizeString(String.valueOf(__padZeros(aStr,cnt))+getZeros(scale), cnt,true);
	    }


	    protected String __trimNumberToSize(String aStr,int cnt) {
	    	if(aStr.length()<cnt) return aStr;
	    	return aStr.substring(aStr.length()-cnt);
	    }
	    
	    protected String __trimNumberToSize(byte[] bs,int cnt) {
	    	return __trimNumberToSize(new String(bs),cnt);
	    }

	    protected String __trimNumberToSize(int aStr,int cnt) {
	    	return __trimNumberToSize(String.valueOf(aStr), cnt);
	    }
	    
	    protected String __trimNumberToSize(long aStr,int cnt) {
	    	return __trimNumberToSize(String.valueOf(aStr), cnt);
	    }
	    
	    
		public void getSQLResults(CobolIndexedFile file) throws java.io.IOException {
	    	Console.println("getSQLResults(CobolIndexedFile) Not implemented. Must be implemented in the Data record.");
		}
		
	    public void setSQLProperties(CobolIndexedFile file) throws java.io.IOException {
	    	Console.println("setSQLProperties(CobolIndexedFile) Not implemented. Must be implemented in the Data record.");
	    }
	    
		public String __normalizeLiteral(String lit) {
			if(RunConfig.getInstance().isEbcdicMachine())
				;//lit=super.toAsciiFromEbcdic(lit,true);
			return lit;
		}

		@Override
		public int numberOfColumns() {
			return 0;
		}

		@Override
		public Object primaryKey() {
			return null;
		}

		@Override
		public boolean initialize(CobolIndexedFile file) throws IOException {
			return false;
		}


		protected int calculateOffset(int... args){
			int off=0;
			for(int i=0;i<args.length;) {
				if(i+3<=args.length)
					off+=args[i++]+args[i++]*--args[i++];
				else
					off+=args[i++];
			}
			return off;
		}

		public int __offset(int... args) {
			int off=0;
			for(int i=0;i<args.length;) {
				off+=args[i++]+(args[i++]*--args[i++]);
			}
			return off;
		}


		public void __setProgram(Program __program) {
			this.__program = super.__program = __program;
		}

		public Program __getProgram() {
			return __program;
		}

		public int __getDependingOnInt(String __field) {
			try {
			Object o=null;
			if(this.__program==null||__field==null) return 0;
			Method execute=null;
			
			execute = __program.getClass().asSubclass(__program.getClass()).cast(__program).getClass().getMethod(__field);

			if(execute!=null) {
				o = execute.invoke(__program);
				return (Integer)o;
			}
			return 0;
		} catch (SecurityException e) {
			e.printStackTrace();
			throw new Error(e);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			throw new Error(e);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new Error(e);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new Error(e);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			throw new Error(e);
		}			
		}

		protected class __IndexAll {
			public Object object;
			public String getter;
			public int[] indexes;
			public __IndexAll(Object o,String g,int... in) {
				object = o;getter=g;indexes=in;
			}
		}

		protected void __postprocess() {
			__program.isArithmeticExceptionOccurred|=super.isArithmeticExceptionOccurred;
			__program.isArithmeticExceptionOccurred|=super.isOverflowExceptionOccurred;
		}

	protected BigDecimal __scale(int n,int scl) {
		return new BigDecimal(n).scaleByPowerOfTen(scl);
	}
	protected BigDecimal __scale(long n,int scl) {
		return new BigDecimal(n).scaleByPowerOfTen(scl);
	}
	protected BigDecimal __scale(double n,int scl) {
		return new BigDecimal(String.valueOf(n)).scaleByPowerOfTen(scl);
	}
	protected BigDecimal __scale(BigDecimal n,int scl) {
		return n.scaleByPowerOfTen(scl);
	}
	protected BigDecimal __scale(String n,int scl) {
		return new BigDecimal(n).scaleByPowerOfTen(scl);
	}

        private static CobolFunction __function = null;
        protected  CobolFunction __function() {
        	if(__function==null)
        		__function=new CobolFunction();
        	return __function;
        }
        
}
