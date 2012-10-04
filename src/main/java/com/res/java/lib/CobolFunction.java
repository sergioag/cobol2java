package com.res.java.lib;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Random;
import java.util.TimeZone;
import java.util.TreeSet;

import com.res.java.lib.CobolBean.__IndexAll;

public class CobolFunction {

	private static CobolFunction thiz = null;
	
	public static CobolFunction getInstance() {
		if(thiz==null)
			thiz=new CobolFunction();
		return thiz;
	}

	/*
	 * 			<F_ACOS>
	 */
	public double aCos(double arg) {
		return Math.acos(arg);
	}

	/*
			<F_ANNUITY>|
	 */
	public double annuity(double arg0,double arg1) {
		if(arg0 == 0) return 1 / arg1; 
		else return arg0 / (1 - Math.pow((1 + arg0) , (- arg1)));
	}
	/*
		<F_ASIN>|
	 */
	public double aSin(double arg) {
		return Math.asin(arg);
	}

	/*
			<F_ATAN>|
	 */
	public double aTan(double arg) {
		return Math.atan(arg);
	}

	/*
			<F_CHAR>|
	 */
	@Deprecated
	public char _char(long arg) {
			return (char) (arg-1);
	}

	/*			<F_COS>|
	 */
	public double cos(double arg) {
		return Math.cos(arg);
	}

	/*
			<F_CURRENT_DATE>|
	 */
	private String getNumberAsPaddedString(long val, int len) {
		StringBuffer ret=new StringBuffer().append(val);
		if(ret.length()>len) {//leftpad
			return ret.substring(0,len);
		} else {
			for(int i=len-ret.length();i>0;--i)
				ret.insert(0, '0');
		}
		return ret.toString();
	}
	public String currentDate() {
		String ret = getNumberAsPaddedString(currentDate.get(GregorianCalendar.YEAR),4)+
		getNumberAsPaddedString(currentDate.get(GregorianCalendar.MONTH)+1,2)+
		getNumberAsPaddedString(currentDate.get(GregorianCalendar.DAY_OF_MONTH),2)+
		getNumberAsPaddedString(currentDate.get(GregorianCalendar.HOUR),2)+
		getNumberAsPaddedString(currentDate.get(GregorianCalendar.MINUTE),2)+
		getNumberAsPaddedString(currentDate.get(GregorianCalendar.SECOND),2)+
		getNumberAsPaddedString(currentDate.get(GregorianCalendar.MILLISECOND)/10,2);
		long diff=currentDate.getTime().getTime()-GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT")).getTime().getTime();
		if(diff<0)ret+='-';
		else ret+='+';
		long mts=(diff/1000)/60;
		long hrs=mts/60;mts=mts-hrs*60;
		ret+=getNumberAsPaddedString(hrs,2)+getNumberAsPaddedString(mts,2);
		return ret;
	}
	/*
		<F_DATE_OF_INTEGER>|
	 */
	@Deprecated
	public long dateOfInteger(long date) {
		clearDate();
		if (date < 1 || date > 3067671) {
			return 0;
		}
		cal.add(GregorianCalendar.DAY_OF_YEAR, (int)date);
		String ret = getNumberAsPaddedString(cal.get(GregorianCalendar.YEAR),4)+
		getNumberAsPaddedString(cal.get(GregorianCalendar.MONTH)+1,2)+
		getNumberAsPaddedString(cal.get(GregorianCalendar.DAY_OF_MONTH),2);
		return Long.parseLong(ret);	}
	/*		<F_DATE_TO_YYYYMMDD>|
	 */
	@Deprecated
	public long dateToYYYYMMDD(long... arg) {
		Console.println("Function not yet available.");
		return 000000000000000000;	}
	/*
		<F_DATEVAL>|
	 */
	@Deprecated
	public Object dateVal(Object... arg) {
		Console.println("Function not yet available.");
		return 000000000000000000;	}
	/*			<F_DAY_OF_INTEGER>|
	 */
	private static final GregorianCalendar cal = new GregorianCalendar();
	private static final GregorianCalendar currentDate = new GregorianCalendar();
	@Deprecated
	public int dayOfInteger(long days) {
		clearDate();
		if (days < 1 || days > 3067671) {
			return 0;
		}
		cal.add(GregorianCalendar.DAY_OF_YEAR, (int)days);
		return cal.get(GregorianCalendar.YEAR)*1000+cal.get(GregorianCalendar.DAY_OF_YEAR);
	}

	private void clearDate() {
		cal.set(GregorianCalendar.YEAR,1600);
		cal.set(GregorianCalendar.MONTH, 11);
		cal.set(GregorianCalendar.DAY_OF_MONTH,31);
	}
	
	/*		<F_DAY_TO_YYYYDDD>|
			 */
	@Deprecated
	public long dayToYYYYDDD(long... arg) {
		Console.println("Function not yet available.");
		return 000000000000000000;	}
	/*		<F_DISPLAY_OF>|
			 */
	@Deprecated
	public String displayOf(Object... arg) {
		Console.println("Function not yet available.");
		return "000000000000000000";	}
	/*				<F_FACTORIAL>|
			 */
	public long factorial(long arg) {
        if( arg <= 1 )    
            return 1;
        else
            return arg * factorial( arg - 1 );
    }
	/*				<F_INTEGER>|
			 */
	public long integer(double arg) {
		return (long)Math.floor(arg);
    }
	public long integer(BigDecimal arg) {
		return Math.round(arg.doubleValue());
    }
	private static final int NUMBER_OF_MS_IN_A_DAY = (1000 * 60 * 60 * 24);
	/*			<F_INTEGER_OF_DATE>|
			 */
	@Deprecated
	public long integerOfDate(long arg) {
		GregorianCalendar cal2=new GregorianCalendar();
		cal2.set(GregorianCalendar.YEAR, (int)arg/10000);
		cal2.set(GregorianCalendar.MONTH,-1+(int) (arg%10000)/100);
		cal2.set(GregorianCalendar.DAY_OF_MONTH,(int)arg%100);
		clearDate();
		return (int)( (cal2.getTime().getTime() - cal.getTime().getTime()) / NUMBER_OF_MS_IN_A_DAY);
	}
	/*				<F_INTEGER_OF_DAY>|
				 */
	@Deprecated
	public long integerOfDay(long arg) {
		GregorianCalendar cal2=new GregorianCalendar();
		cal2.set(GregorianCalendar.YEAR, (int)arg/1000);
		cal2.set(GregorianCalendar.DAY_OF_YEAR,(int) arg%1000);
		clearDate();
		return (int)( (cal2.getTime().getTime() - cal.getTime().getTime()) / NUMBER_OF_MS_IN_A_DAY);
    }
	/*			<F_INTEGER_PART>|
				 */
	@Deprecated
	public long integerPart(double arg) {
		return (long) arg;	
    }	
	@Deprecated
	public long integerPart(BigDecimal arg) {
		return arg.longValue();	
    }
	/*	<F_LENGTH>|
				 */
	@Deprecated
	public int length(char arg) {
		return 1;	
    }
	@Deprecated
	public int length(String arg) {
		return arg.length();	
    }
	/*			<F_LOG>|
					 */
	@Deprecated
	public double log(double arg) {
		return Math.log(arg);	
    }	

	/*		<F_LOG10>|
						 */
	@Deprecated
	public double log10(double arg) {
		return Math.log10(arg);	
    }	
	@Deprecated
	public double log10(BigDecimal arg) {
		return Math.log10(arg.doubleValue());	
    }
	/*		<F_LOWER_CASE>|
						 */
	@Deprecated
	public String lowerCase(char arg) {
		return new String(new char[]{arg}).toLowerCase();	
    }	
	@Deprecated
	public String lowerCase(String arg) {
		return arg.toLowerCase();	
    }	
	
	/*			<F_MAX>|
	 */

	@Deprecated
	public double max(double... arg) {
		double o1=arg[0];int i=1;
		for(;i<arg.length;++i)
			o1=Math.max(o1,arg[i]);
		return o1;	
    }	
	@Deprecated
	public BigDecimal max(BigDecimal... arg) {
		BigDecimal o1=arg[0];int i=1;
		for(;i<arg.length;++i)
			if(o1.compareTo(arg[i])<0)
				o1=arg[i];
		return o1;	
    }	
	@Deprecated
	public String max(String... arg) {
		int i=0,j=0;
		for(;i<arg.length;++i)
			if(arg[j].compareTo(arg[i])<0)
				j=i;
		return arg[j];	
    }	
	public char maxChar(__IndexAll indexAll) {

		clearIndexes();
		Object prev=null;Object curr=0;
		int i=0;
		do {
			if(!hasMoreElements(indexAll)) break;
			curr=next(indexAll);
			if(prev==null) {
				prev=curr;
			} else {
				++i;
				if(curr.toString().compareTo(prev.toString())>0) {
					prev=curr;
				}
			}
		} while(true);
		return prev.toString().charAt(0);
	
	
	}

	public double maxDouble(__IndexAll indexAll) {
		clearIndexes();
		Object prev=null;Object curr=0;
		int i=0;
		do {
			if(!hasMoreElements(indexAll)) break;
			curr=next(indexAll);
			if(prev==null) {
				prev=curr;
			} else {
				++i;
				if(curr.toString().compareTo(prev.toString())>0) {
					prev=curr;
				}
			}
		} while(true);
		return Double.parseDouble(prev.toString());
	
	}
	
		/*				<F_MEAN>|
 */
	@Deprecated
	public double mean(double... arg) {
		double m=0.0;
		for(double a:arg) m+=a;
		return m/arg.length;
	
    }
	public double mean(BigDecimal... arg) {
		double m=0.0;
		for(Object a:arg)
			m+=((BigDecimal)a).doubleValue();
		return m/arg.length;
	
    }
	public Object mean(__IndexAll indexAll) {
		clearIndexes();
		int count = 0;Integer val=0;
		do {
			if(!hasMoreElements(indexAll)) break;
			val+=nextInteger(indexAll);count++;
		} while(true);
		return (Double)( (double) val/count );
	}
	/*	<F_MEDIAN>|
	*/
	@Deprecated
	public double median(double... arg) {

		TreeSet<Object> list = new TreeSet<Object>();
		clearIndexes();
		for(double d: arg)
			list.add(d);
		Object[] o = new Object[list.size()]; 
		o=list.toArray(o);
		int mid=o.length/2;
		if(list.size()%2==0)
			return  (((Double) o[mid-1])+((Double)o[mid]))/2;
		else return (Double)o[mid];
			
	
    }
	public Object median(__IndexAll indexAll) {
		TreeSet<Object> list = new TreeSet<Object>();
		clearIndexes();
		do {
			if(!hasMoreElements(indexAll)) break;
			list.add(nextInteger(indexAll));
		} while(true);
		Object[] o = new Object[list.size()]; 
		o=list.toArray(o);
		int mid=o.length/2;
		if(list.size()%2==0)
			return  (((Double) o[mid-1])+((Double)o[mid]))/2;
		else return o[mid];
			
	}

	/*		<F_MIDRANGE>|
	*/
	@Deprecated
	public double midrange(double... arg) {
		double min=0,max=0;boolean first=true;
		for(double d: arg) {
			if(first) {
				min=d;max=d;
				first=false;
			} else {
			if(d<min) min=d;
			if(d>max) max=d;
			}
		} ;
		return (double)((min+max)/2);
    }

	public Object midrange(__IndexAll indexAll) {
		clearIndexes();
		double min=0,max=0,curr=0;boolean first=true;
		do {
			if(!hasMoreElements(indexAll)) break;
			curr=nextInteger(indexAll);
			if(first) {
				min=max=curr;first=false;
			} else {
				if(curr<min) min=curr;
				if(curr>max) max=curr;
			}
		} while(true);
		return (Double)(double)((min+max)/2);
	}


	/*				<F_MIN>|
	*/
	@Deprecated
	public double min(double... arg) {
		double min = 0;boolean first=true;
		for(double d:arg) {
			if(!first)
				min=Math.min(min,d);
			else {
				min=d;
				first=false;
			}
		} ;
		return ( min );
    }

	public String min(String... arg) {
		String min = "";boolean first=true;
		for(String d:arg) {
			if(!first){
				if(d.compareTo(min)<0)
					min=d;
			}
			else {
				min=d;
				first=false;
			}
		} 
		return ( min );
	
    }

	public Integer minInt(__IndexAll indexAll) {
		clearIndexes();
		int min = 0;boolean first=true;
		do {
			if(!hasMoreElements(indexAll)) break;
			if(!first)
				min=Math.min(min,nextInteger(indexAll));
			else {
				min=nextInteger(indexAll);
				first=false;
			}
		} while(true);
		return (Integer)( min );
	}


	public char minChar(__IndexAll indexAll) {

		clearIndexes();
		int min = 0;boolean first=true;
		do {
			if(!hasMoreElements(indexAll)) break;
			if(!first)
				min=Math.min(min,nextInteger(indexAll));
			else {
				min=nextInteger(indexAll);
				first=false;
			}
		} while(true);
		return (char)( min );
	
	}




	/*			<F_MOD>|
	*/
	@Deprecated
	public long mod(long arg1,long arg2) {
		return arg1 - (arg2 * integer ((double)arg1 / arg2));
	
    }
	/*		<F_NATIONAL_OF>|
	*/
	@Deprecated
	public String nationalOf(Object... arg) {
		Console.println("Function not available. Use Java Equivalent.");
		return "000000000000000000";
	
    }
	/*		<F_NUMVAL>|
	*/
	@Deprecated
	public double numVal(String arg) {
		return FieldFormat.parseNumber(arg).doubleValue();
    }
	@Deprecated
	public double numVal(char arg) {
		return numVal(String.valueOf(arg));
    }
	/*				<F_NUMVAL_C>|
	*/
	@Deprecated
	public double numValC(String... arg) {
		return FieldFormat.parseNumber(arg[0]).doubleValue();
    }
	/*				<F_ORD>|
	*/
	@Deprecated
	public int ord(String arg) {
		return arg.charAt(0)+1;
    }
	@Deprecated
	public int ord(char arg) {
		return arg+1;
    }
	@Deprecated
	public int ord(int arg) {
		return arg;
    }
	/*
				<F_ORD_MAX>|
	*/
	@Deprecated
	public int ordMax(String... arg) {
		String o1=arg[0];int o2=0,i=1;
		for(;i<arg.length;++i)
				if(o1.compareTo(arg[i])<0) {
						o1=arg[i];
						o2=i;
					}
		return o2+1;	
    }
	@Deprecated
	public int ordMax(char... arg) {
		int o1=arg[0],o2=0,i=1;
		for(;i<arg.length;++i)
			if(o1<arg[i]) {
				o1=arg[i];
				o2=i;
			}
		return o2+1;	
    }
	@Deprecated
	public int ordMax(int... arg) {
		int o1=arg[0],o2=0,i=1;
		for(;i<arg.length;++i)
				if(o1<arg[i]) {
					o1=arg[i];
					o2=i;
				}
		return o2+1;	
    }
	public int ordMaxInt(__IndexAll indexAll) {
		clearIndexes();
		Object prev=null;Object curr=0;ArrayList<Object> list = new ArrayList<Object>();
		int prevIdx=0,i=0;
		do {
			if(!hasMoreElements(indexAll)) break;
			curr=next(indexAll);
			list.add(curr);
			if(prev==null) {
				prev=curr;
			} else {
				++i;
				if(curr.toString().compareTo(prev.toString())>0)
					prevIdx=i;
			}
		} while(true);
		return prevIdx+1;
	}


	/*			<F_ORD_MIN>|
	 * 
*/
	@Deprecated
	public int ordMin(String... arg) {
		String o1=arg[0];int o2=0,i=1;
		for(;i<arg.length;++i)
				if(o1.compareTo(arg[i])>0) {
						o1=arg[i];
						o2=i;
					}
		return o2+1;	
    }
	@Deprecated
	public int ordMin(char... arg) {
		int o1=arg[0],o2=0,i=1;
		for(;i<arg.length;++i)
			if(o1>arg[i]) {
				o1=arg[i];
				o2=i;
			}
		return o2+1;	
    }
	@Deprecated
	public int ordMin(int... arg) {
		int o1=arg[0],o2=0,i=1;
		for(;i<arg.length;++i)
				if(o1>arg[i]) {
					o1=arg[i];
					o2=i;
				}
		return o2+1;	
    }
	public int ordMinInt(__IndexAll indexAll) {
		clearIndexes();
		Object prev=null;Object curr=0;ArrayList<Object> list = new ArrayList<Object>();
		int prevIdx=0,i=0;
		do {
			if(!hasMoreElements(indexAll)) break;
			curr=next(indexAll);
			list.add(curr);
			if(prev==null) {
				prev=curr;
			} else {
				++i;
				if(curr.toString().compareTo(prev.toString())<0)
					prevIdx=i;
			}
		} while(true);
		return prevIdx+1;
	}
	public double minDouble(__IndexAll indexAll) {
		clearIndexes();
		Object prev=null;Object curr=0;
		int i=0;
		do {
			if(!hasMoreElements(indexAll)) break;
			curr=next(indexAll);
			if(prev==null) {
				prev=curr;
			} else {
				++i;
				if(curr.toString().compareTo(prev.toString())<0) {
					prev=curr;
				}
			}
		} while(true);
		return Double.parseDouble(prev.toString());
	}



		/*	<F_PRESENT_VALUE>|
	*/
	
	@Deprecated
	public double presentValue(double arg1,double... arg2) {
		double p=0.0;int i=1;
		for(double a:arg2)
			p+=a / Math.pow((1 + arg1) ,i++);    
		return p;
	}
	
	@Deprecated
	public double presentValue(BigDecimal arg1,BigDecimal... arg2) {
		BigDecimal p=BigDecimal.ZERO;int i=1;
		for(BigDecimal a:arg2)
			p.add(a.divide(arg1.add(BigDecimal.ONE).pow(i++)));    
		return p.doubleValue();
	}
	
	/*		<F_RANDOM>|
	*/
	@Deprecated
	public double random(double... arg) {
		if(arg.length>0)
			return new Random((long) arg[0]).nextDouble();
		else
			return new Random().nextDouble();
	}
	/*				<F_RANGE>|
	 */
	@Deprecated
	public double range(double... arg) {
		return max(arg)-min(arg);
	}
	@Deprecated
	public double rangeDouble(__IndexAll indexAll) {
		return maxDouble(indexAll)-minDouble(indexAll);
	}
	
	/*			<F_REM>|
	 */
	@Deprecated
	public double rem(double arg1, double arg2) {
		return arg1%arg2;
	}
	@Deprecated
	public double rem(BigDecimal arg1, BigDecimal arg2) {
		return arg1.remainder(arg2).doubleValue();
	}
	/*				<F_REVERSE>|
	 */
	@Deprecated
	public String reverse(String arg) {
		return new StringBuffer(arg).reverse().toString();
	}
	/*			<F_SIN>|
						 */
	@Deprecated
	public double sin(double arg) {
		return Math.sin(arg);	
    }	
	@Deprecated
	public double sin(BigDecimal arg) {
		return Math.sin(arg.doubleValue());	
    }
	/*				<F_SQRT>|
						 */
	@Deprecated
	public double sqrt(double arg) {
		return Math.sqrt(arg);	
    }	
	@Deprecated
	public double sqrt(BigDecimal arg) {
		return Math.sqrt(arg.doubleValue());	
    }
	/*			<F_STANDARD_DEVIATION>|
	 */
	@Deprecated
	public double standardDeviation(double... arg) {
		double mean= (Double)mean(arg);
		double sum_sqr=0.0;
		for(double d:arg) {
			sum_sqr+=Math.pow((d-mean),2);
		}
		return Math.abs(Math.sqrt(sum_sqr/arg.length));
	}
	public double standardDeviationDouble(__IndexAll indexAll) {
		
		double mean= (Double)mean(indexAll);
		
		clearIndexes();
		
		double sum_sqr=0.0;int count=0;
		do {
			if(!hasMoreElements(indexAll)) break;
			sum_sqr+=Math.pow((nextDouble(indexAll)-mean),2);
			count++;
		} while(true);
			
		return Math.abs(Math.sqrt(sum_sqr/count));
		
	}
	/*			<F_SUM>|
	 */
	public double sum(double... arg) {
		double sum=0;
		for(double a:arg)
			sum+=a;
		return sum;
	}

	public double sum(BigDecimal... arg) {
		BigDecimal sum=BigDecimal.ZERO;
		for(BigDecimal a:arg)
			sum.add(a);
		return sum.doubleValue();
	}
	
	public double sumDouble(__IndexAll indexAll) {
		
		clearIndexes();
		double sum=0.0;
		do {
			if(!hasMoreElements(indexAll)) break;
			sum+=nextDouble(indexAll);
		} while(true);
			
		return sum;
		
	}
	/*				<F_TAN>|
	 */
	@Deprecated
	public double tan(double arg) {
		return Math.tan(arg);	
    }	
	@Deprecated
	public double tan(BigDecimal arg) {
		return Math.tan(arg.doubleValue());	
    }
	/*		
	 * 		<F_UNDATE>|
		 */
	@Deprecated
	public String undate(Object... arg) {
		Console.println("Function not available. Use Java Equivalent.");
		return "000000000000000000";
    }	
	/*		<F_UPPER_CASE>|
		 */
	@Deprecated
	public String upperCase(char arg) {
		return new String(new char[]{arg}).toUpperCase();	
    }	

	@Deprecated
	public String upperCase(String arg) {
		return arg.toUpperCase();
    }	
	/*			<F_VARIANCE>|
		 */
	@Deprecated
	public double variance(double... arg) {
		double mean= (Double)mean(arg);
		double sum_sqr=0.0;
		for(double d:arg) {
			sum_sqr+=Math.pow((d-mean),2);
		}
		return sum_sqr/arg.length;
	}
	@Deprecated
	public double varianceDouble(__IndexAll indexAll) {
		
		double mean= (Double)mean(indexAll);
		
		clearIndexes();
		
		double sum_sqr=0.0;int count=0;
		do {
			if(!hasMoreElements(indexAll)) break;
			sum_sqr+=Math.pow((nextDouble(indexAll)-mean),2);
			count++;
		} while(true);
			
		return sum_sqr/count;
	}



	/*			<F_WHEN_COMPILED>|
		 */
	@Deprecated
	public String whenCompiled() {
		return "201005011630";
    }	
	/*					<F_YEAR_TO_YYYY>|
		 */
	@Deprecated
	public int yearToYYYY(long... arg) {
		Console.println("Function not yet available.");
		return 0;
	}
	/*			<F_YEARWINDOW>

	 */
	@Deprecated
	public int yearWindow() {
		Console.println("Function not yet available.");
		return 0;
	}

	//MAX and MIN Integer as a String
	public char MAX_VALUE() {
		return CobolBytes.MAX_VALUE();
	}
	public char MIN_VALUE() {
		return CobolBytes.MIN_VALUE();
	}

	public char[] sum(__IndexAll indexAll) {
		// TODO Auto-generated method stub
		return null;
	}

	private Integer nextInteger(__IndexAll indexAll) {
		return (Integer)next(indexAll);
	}
	private double nextDouble(__IndexAll indexAll) {
		return Double.parseDouble(next(indexAll).toString());
	}
	
	private Object next(__IndexAll indexAll) {
		Object o = __get(indexAll);
		if(o==null) throw new Error();
		int j=indexAll.indexes.length-1;
		do {
			if(j<0)break;
			if(++ind[j]<=indexAll.indexes[j]) break;
			if(--j<0) break;
			ind[j]=1;
		} while(true);
		return o;
	}


	private boolean hasMoreElements(__IndexAll indexAll) {
		for(int i=indexAll.indexes.length-1;i>=0;--i) {
			if(ind[i]<=indexAll.indexes[i])return true;
		}
		return false;
	}
	private final int ind[]=new int[7];
	private void clearIndexes() {
		for(int i=0;i<7;++i) ind[i]=1;
	}
	private Hashtable<String,Method> __env = null;
	
	private Object __get(__IndexAll __field) {
		try {
		Object o=null;
		if(__field==null||__field.object==null) return 0;
		Method execute=null;
		if(__env==null)
			__env=new Hashtable<String,Method>();
		else
			execute=__env.get(__field.getter);
		if(execute==null) {
			switch(__field.indexes.length) {
			case 0:
				execute = __field.object.getClass().getMethod(__field.getter);
				if(execute!=null) {
					o = execute.invoke(__field.object);
					return o;
				}
				break;
			case 1:
				execute = __field.object.getClass().getMethod(__field.getter,Integer.TYPE
						);
				if(execute!=null) {
					o = execute.invoke(__field.object,ind[0]);
					return o;
				}
				break;
			case 2:
				execute = __field.object.getClass().getMethod(__field.getter,Integer.TYPE,
						Integer.TYPE
				);
				if(execute!=null) {
					o = execute.invoke(__field.object,ind[0],ind[1]);
					return o;
				}
				break;
			case 3:
				execute = __field.object.getClass().getMethod(__field.getter,Integer.TYPE,
						Integer.TYPE,Integer.TYPE
				);
				if(execute!=null) {
					o = execute.invoke(__field.object,ind[0],ind[1],ind[2]);
					return o;
				}
				break;
			case 4:
				execute = __field.object.getClass().getMethod(__field.getter,Integer.TYPE,
						Integer.TYPE,Integer.TYPE,Integer.TYPE
				);
				if(execute!=null) {
					o = execute.invoke(__field.object,ind[0],ind[1],ind[2],ind[3]);
					return o;
				}
				break;
			case 5:
				execute = __field.object.getClass().getMethod(__field.getter,Integer.TYPE,
						Integer.TYPE,Integer.TYPE,Integer.TYPE,Integer.TYPE
				);
				if(execute!=null) {
					o = execute.invoke(__field.object,ind[0],ind[1],ind[2],ind[3],ind[4]);
					return o;
				}
				break;
			case 6:
				execute = __field.object.getClass().getMethod(__field.getter,Integer.TYPE,
						Integer.TYPE,Integer.TYPE,Integer.TYPE,Integer.TYPE,Integer.TYPE
				);
				if(execute!=null) {
					o = execute.invoke(__field.object,ind[0],ind[1],ind[2],ind[3],ind[4],ind[5]);
					return o;
				}
				break;
			case 7:
				execute = __field.object.getClass().getMethod(__field.getter,Integer.TYPE,
						Integer.TYPE,Integer.TYPE,Integer.TYPE,Integer.TYPE,Integer.TYPE,Integer.TYPE
				);
				if(execute!=null) {
					o = execute.invoke(__field.object,ind[0],ind[1],ind[2],ind[3],ind[4],ind[5],ind[6]);
					return o;
				}
				break;
			}
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

	public int maxInt(__IndexAll indexAll) {
		clearIndexes();
		
		double max=0.0;
		do {
			if(!hasMoreElements(indexAll)) break;
			max=Math.max(nextDouble(indexAll),max);
		} while(true);
			
		return (int)max;	}

	public double meanDouble(__IndexAll indexAll) {
		clearIndexes();
		
		double mean=0.0;int i=0;
		do {
			if(!hasMoreElements(indexAll)) break;
			mean+=nextDouble(indexAll);++i;
		} while(true);
			
		return mean/i;	
	}

	public double medianDouble(__IndexAll indexAll) {
		TreeSet<Double> list = new TreeSet<Double>();
		clearIndexes();
		do {
			if(!hasMoreElements(indexAll)) break;
			list.add(nextDouble(indexAll));
		} while(true);
		Double[] o = new Double[list.size()]; 
		o=list.toArray(o);
		int mid=o.length/2;
		if(list.size()%2==0)
			return  (((Double) o[mid-1])+((Double)o[mid]))/2;
		else return o[mid];	}

	public double midrangeDouble(__IndexAll indexAll) {
		clearIndexes();
		double min=0,max=0,curr=0;boolean first=true;
		do {
			if(!hasMoreElements(indexAll)) break;
			curr=nextDouble(indexAll);
			if(first) {
				min=max=curr;first=false;
			} else {
				if(curr<min) min=curr;
				if(curr>max) max=curr;
			}
		} while(true);
		return (double)((min+max)/2);	}
}
