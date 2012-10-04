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
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.regex.Pattern;

import com.res.java.lib.exceptions.InvalidCobolFormatException;

public class FieldFormat {

	private String name=null;
	private String picture=null;
	
	private DecimalFormat numberFormat=null;

	private DecimalFormat getNumberFormat() {
		return numberFormat;
	}

	private void setNumberFormat(DecimalFormat numberFormat) {
		this.numberFormat = numberFormat;
	}
	
	private char[] javaFormat=null;
	
	private char[] getJavaFormat() {
		return javaFormat;
	}

	private void setJavaFormat(char[] javaFormat) {
		this.javaFormat = javaFormat;
	}
	private FieldFormatData sym=new FieldFormatData();
	
	public boolean isSigned() {
		return sym.isSigned;
	}

	public void setSigned(boolean isSigned) {
		this.sym.isSigned = isSigned;
	}

	public boolean isFloating() {
		return false;
	}

	public void setFloating(boolean isFloating) {
		//this.sym.isFloating = isFloating;
	}

	public boolean isPointer() {
		return false;
	}

	public void setPointer(boolean isPointer) {
		//this.isPointer = isPointer;
	}

	//private boolean isPointer=false;

	public boolean isBlankWhenZero() {
		return sym.isBlankWhenZero;
	}

	public void setBlankWhenZero(boolean blankWhenZero) {
		this.sym.isBlankWhenZero = blankWhenZero;
	}

	public boolean isJustifiedRight() {
		return sym.isJustifiedRight;
	}

	public void setJustifiedRight(boolean justifiedRight) {
		this.sym.isJustifiedRight = justifiedRight;
	}

	public boolean isSignSeperate() {
		return sym.isSignSeparate;
	}

	public void setSignSeperate(boolean signSeperate) {
		this.sym.isSignSeparate = signSeperate;
	}

	public boolean isSignTrailing() {
		return !sym.isSignLeading;
	}

	public void setSignTrailing(boolean signTrailing) {
		this.sym.isSignLeading = !signTrailing;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPicture() {
		return picture;
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}
	
	public void setDataCategory(byte dataCategory) {
		if(this.sym!=null)
			this.sym.dataCategory = dataCategory;
	}

	public byte getDataCategory() {
		if(this.sym!=null)
			return this.sym.dataCategory;
		else 
			return -1;
	}
	public void setScaled(boolean isScaled) {
		this.isScaled = isScaled;
	}

	public boolean isScaled() {
		return isScaled;
	}

	private boolean isScaled=false;

	public FieldFormat(String pictureVal) {
		sym=new FieldFormatData();
		sym.type=FieldFormat.verifyCobolPicture(pictureVal);
		setDataCategory(dataCategoryTemp);
		setPicture(pictureVal);
		getJavaFormat(pictureVal);
	}
	
	public FieldFormat(String nameVal, String pictureVal) {
		sym=new FieldFormatData();
		sym.type=FieldFormat.verifyCobolPicture(pictureVal);
		setDataCategory(dataCategoryTemp);
		setName(nameVal);setPicture(pictureVal);
		getJavaFormat(pictureVal);
	}
	public FieldFormat(String nameVal, String pictureVal, boolean bwz) {
		this(nameVal,pictureVal);
		this.setBlankWhenZero(bwz);
	}

	public FieldFormat(String nameVal, String pictureVal, boolean bwz,boolean just, boolean signSep, boolean trail) {
		sym=new FieldFormatData();
		sym.type=FieldFormat.verifyCobolPicture(pictureVal);
		sym.dataCategory=FieldFormat.dataCategoryTemp;
		setName(nameVal);setPicture(pictureVal);
		this.setBlankWhenZero(bwz);this.setJustifiedRight(just);
		this.setSignSeperate(signSep);this.setSignTrailing(trail);
		getJavaFormat(pictureVal);
	}
	
	private String blankWhenZero(String num) {
		//if(!this.sym.isSigned)
			//return num;
		String ret="";
		if(num.matches("[0\\.\\+\\-\\$ ]*")) {
			for(int i=0;i<getJavaFormat().length;++i)
				ret+=' ';
			return ret;
		}
		return num;
	}
	
	private String suppressLeadingZeros(String num) {
		String ret="";String ret2;int i;
		if(num.trim().length()>0) {
			if(num.length()==getJavaFormat().length) {//Is this check always true for correct format?
				boolean isLeading=true;
				char prev=' ';char c=' ';
				//Blank out the leading zeros
				for(i=0;i<num.length();++i) {
					c=getJavaFormat()[i];
					char c2=num.charAt(i);
					if(((c=='Z'&&c2=='0')||(prev=='Z'&&c==','&&c2==','))&&isLeading) {
						ret+=' ';
						prev='Z';
						continue;
					}
					else 
						if(((c=='*'&&c2=='0')||(prev=='*'&&c==','&&c2==','))&&isLeading) {
							ret+='*';
							prev='*';
							continue;
						}
						else if(c=='.') {
							ret+=c2;
							break;
						}
						else 
							if(c=='$'||c=='S'||(c=='B'&&(c2==' '||c2=='B'))||(c=='D'&&(c2==' '||c2=='D'))||
									(c=='C'&&(c2==' '||c2=='C'))||(c=='R'&&c2=='R')) {
								ret+=c2;
								continue;
							}
							else if(c=='.') break;
					
					isLeading=false;
					ret+=c2;
				}
				ret2=ret;//save
				ret="";
				//Blank out trailing zeros
				if(c=='.') {
					boolean isTrailing=true;
					for(i=num.length()-1;i>=0;--i) {
						c=getJavaFormat()[i];
						char c2=num.charAt(i);
						if(((c=='Z'&&c2=='0')||(prev=='Z'&&c==','&&c2==','))&&isTrailing) {
							ret=' '+ret;
							prev='Z';
							continue;
						}
						else 
							if(((c=='*'&&c2=='0')||(prev=='*'&&c==','&&c2==','))&&isTrailing) {
								ret='*'+ret;
								prev='*';
								continue;
							}
							else if(c=='.') {
								break;
							}
							else 
								if(c=='$'||c=='S'||(c=='B'&&(c2==' '||c2=='B'))||(c=='D'&&(c2==' '||c2=='D'))||
										(c=='C'&&(c2==' '||c2=='C'))||(c=='R'&&c2=='R')) {
									ret=c2+ret;
									continue;
								}
						isTrailing=false;
						ret=c2+ret;
					}
					num=ret2+ret;
				} else num=ret2;
			}
		}
		if(isDecimalPointComma()) {
			num=num.replace(',','~').replace('.',',').replace('~','.');
		}
		if(getCurrencySign()!=null) {
			num=num.replace("$",getCurrencySign());
		}
		return  num;
	}
	
	public String doSimpleInsertion(String text) {
	
		if(getNumberFormat()!=null) {
			int j=0;String retVal="";
			if(text.length()>getJavaFormat().length) reportError();

			for(int i=0;i<getJavaFormat().length;++i) {
				char c=getJavaFormat()[i];
				switch(c) {
				case ',':
				case '/':
					retVal+=c; break;
				case '@':
					retVal+='0'; break;
				case 'B':
					retVal+=' ';break;
				default:
					if(j<text.length())
						retVal+=text.charAt(j++);
					else 
						retVal+=' ';

				}
			}

		
			return retVal.replace("DD","DB");
		} else {
			char fill=' ';
			if (text.length()==1) {
				fill=text.charAt(0);
				if(text.getBytes()[0]==-1)
					fill='\uffff';
					else
				if(fill=='\"'||fill=='0'||fill==0x00||fill==' ');
				else fill=' ';
			}
			if(fields==null||fields.length<=0) return text;
			String retVal="";
			for(int i=0,j=0;i<fields.length;i++) {
				TextField field=fields[i];
				if(field==null) break;
				char c=field.picChar;
				if(c=='B') c=' ';
				switch(field.picChar) {
				case 'B':
				case '0':
				case '/':
					for(int l=0;l<field.len;++l)
						retVal+=c;
					break;
				default:
					for(int l=0;l<field.len;++l)
						if(j<text.length())
							retVal+=text.charAt(j++);
						else 
							retVal+=fill;
				}
				
			}
			return retVal;
		}
	}
	
	//TODO Need to test and refine and improve performance. 
	private String doScaling(String num) {
		if(!isScaled())
			return num;
		int i=0;String retVal="";
		boolean leftDecimal=false;
		do {
			if(num.charAt(i)=='S')
				retVal+=num.charAt(i);
			else 
				if(num.charAt(i)=='P'&&!leftDecimal)  {
					retVal+=".0";
					num=num.replace(".", "");
					leftDecimal=true;
				}
				else
					if(num.charAt(i)=='P')
						retVal+='0';
					else break;
			++i;
		} while(true);
		num=retVal+num.substring(i,num.length()-1);
		if(leftDecimal) {
			return num;
		}
		retVal="";
		int j=num.length()-1;boolean isTrailing=!leftDecimal;
		while(j>=0) {
			if(num.charAt(j)=='P'&&isTrailing) {
				retVal+='0';
				num.replace(".", "");
			}
			else 
				if(num.charAt(j)=='P')
					;
				else{
					retVal+=num.charAt(j);
					isTrailing=false;
				}
			--j;
		} 
		num=retVal;
		return num;
	}
	
	//TODO Need to test and refine for performance. 
	private String doFloatingInsertion(String num,boolean isNegative) {
		if(getNumberFormat()==null)
			return num;
		String ret="";int i;
		if(num.trim().length()>0) {
			if(num.length()==getJavaFormat().length) {//Is this check always true for correct format?
				char prev=' ';int signIdx=-1,dollarIdx=-1;char c=0;
				//Blank out the leading zeros
				for(i=0;i<num.length();++i) {
					c=getJavaFormat()[i];
					char c2=num.charAt(i);
					if((c=='+'||c=='-')&&(c2=='0'||c2==','||c2=='+'||c2=='-')) {
						if(c=='+')
							prev=(isNegative)?'-':'+';
						else
							prev=(isNegative)?'-':' ';
						signIdx=i;
						continue;
					}
					else 
						if(c=='$'&&(c2=='0'||c2==','||c2=='$')) {
							dollarIdx=i;
							continue;
						}
						else if(c=='B'&&i==dollarIdx+1) {
							dollarIdx++;
							continue;
						}
						else 
							if(c==','&&c2==',') {
								if(signIdx>=0)
									if(dollarIdx>=0)
										if(signIdx>dollarIdx)
											signIdx=i;
										else
											dollarIdx=i;
									else
										signIdx=i;
								else
									if(dollarIdx>=0)
										dollarIdx=i;
								continue;
							}
					break;
				}
				i=0;boolean haveSign=false;boolean haveDollar=false;
				if(signIdx>=0) {
					if(dollarIdx>=0){
						if(signIdx<dollarIdx){
							for(;i<signIdx;++i)
								ret+=' ';
							ret+=prev;
							for(i++;i<dollarIdx;++i)
								ret+=' ';
							ret+='$';
						} else {
							for(;i<dollarIdx;++i)
								ret+=' ';
							ret+='$';
							for(i++;i<signIdx;++i)
								ret+=' ';
							ret+=prev;
						}
						haveDollar=true;
					}
					else{
						for(;i<signIdx;++i)
							ret+=' ';
						ret+=prev;
					}
					haveSign=true;
					num=ret+num.substring(i+1);
				} else {  
					if(dollarIdx>=0){
						for(;i<dollarIdx;++i)
							ret+=' ';
						ret+='$';
						haveDollar=true;
						num=ret+num.substring(i+1);
					}
				}
					
				//Blank out trailing +/-/$
				prev=' ';signIdx=-1;dollarIdx=-1;c=0;
				for(i=num.length()-1;i>0;--i) {
					c=getJavaFormat()[i];
					char c2=num.charAt(i);
					if((c=='+'||c=='-')&&(c2=='0'||c2==','||c2=='+'||c2=='-')) {
						if(c=='+')
							prev=(isNegative)?'-':'+';
						else
							prev=(isNegative)?'-':' ';
						signIdx=i;
						continue;
					}
					else 
						if(c=='$'&&(c2=='0'||c2==','||c2=='$')) {
							dollarIdx=i;
							continue;
						}
						else 
							if(c==','&&c2==',') {
								if(signIdx>=0)
									if(dollarIdx>=0)
										if(signIdx>dollarIdx)
											signIdx=i;
										else
											dollarIdx=i;
									else
										signIdx=i;
								else
									if(dollarIdx>=0)
										dollarIdx=i;
								continue;
							}
					break;
				}
				i=num.length()-1;ret="";
				if(signIdx>=0) {
					if(dollarIdx>=0){
						if(signIdx<dollarIdx){
							for(;i>dollarIdx;--i)
								ret=' '+ret;
							if(!haveDollar)	ret='$'+ret; else ret=' '+ret;
							for(;i>signIdx;--i)
								ret=' '+ret;
							if(!haveSign) ret=prev+ret; else ret=' '+ret;
						} else {
							for(;i>signIdx;--i)
								ret=' '+ret;
							if(!haveSign) ret=prev+ret; else ret=' '+ret;
							for(;i>dollarIdx;--i)
								ret=' '+ret;
							if(!haveDollar)	ret='$'+ret; else ret=' '+ret;
						}
					}
					else{
						for(;i>signIdx;--i)
							ret=' '+ret;
						if(!haveSign) ret=prev+ret; else ret=' '+ret;
					}
					num=num.substring(0,i)+ret;
				} else 
					if(dollarIdx>=0){
						for(;i>dollarIdx;--i)
							ret=' '+ret;
						if(!haveDollar)	ret='$'+ret; else ret=' '+ret;
						num=num.substring(0,i)+ret;
					}
			}		
		}
		return num;
	}
	private String addSign(String num,boolean isNegative){
		if(picture.indexOf('V')>=0)
			num.replace(".", "");
		num=doSimpleInsertion(num);
		if(this.sym.isSigned)
			if(isNegative){
				num=num.replace('S', '-').replace('+','-');
			}
			else {
				num=num.replace('S', '+').replace('-',' ').replace("DB","  ").replace("CR", "  ");
			}
		else ;
		num=doFloatingInsertion(num,isNegative);
		return doScaling(num);
	}
	public String format(char str) {
		
		
		if(sym==null) return String.valueOf(str);
		if(getNumberFormat()!=null) {
			return format(parseNumber( String.valueOf(str)));
		}
		else
		 {
			return doSimpleInsertion( String.valueOf(str));
			
		}
		
	}
	
	public String format(double d) {

		if(sym==null) 
			return "";
		
		if(this.isBlankWhenZero()&&d==0.00)
			return blankWhenZero("0");
		
		if(numberFormat!=null) 
			return format(new BigDecimal(d));
		else
		if(sym.type==Constants.BIGDECIMAL) {
				return new Double(d).toString().trim();
		} else
			if(sym.type==Constants.STRING) {
				String str = String.valueOf(Math.abs(d));
				for(int i=sym.maxStringLength-str.length()-((d<0)?1:0);i>0;--i)
					str='0'+str;
				str=(d<0)?"-":""+str;
				return format(str);
			}
		
		return String.valueOf(d); 

	}

	public String format(BigDecimal d) {

		if(d==null) d = BigDecimal.ZERO;
		
		if(this.isBlankWhenZero()&&d.compareTo(BigDecimal.ZERO)==0)
			return blankWhenZero("0");

		if(sym!=null) {
			if(getNumberFormat()!=null)
				return suppressLeadingZeros(addSign(getNumberFormat().format(d.abs().
						setScale(getNumberFormat().getMaximumFractionDigits(),RoundingMode.DOWN).multiply(BigDecimal.TEN.pow(scale))),(d.signum()<0)));
			else 
				if(sym.type==Constants.STRING) {
					return format(d.abs().toPlainString());
				}
		}
		
		return d.toString();
		
	}

	
	public String format(int d) {

		if(this.isBlankWhenZero()&&d==0)
			return blankWhenZero("0");

		if(sym!=null) { 
			
			if(getNumberFormat()!=null)
				return suppressLeadingZeros(addSign(getNumberFormat().format((Math.abs(d)*Math.pow(10, scale))),(d<0)));
			else
			if(sym.type==Constants.INTEGER||
					sym.type==Constants.SHORT) {
					return new Integer(d).toString().trim();
			}  else
				if(sym.type==Constants.STRING) {
                                    /*
					String str = String.valueOf(Math.abs(d));
					for(int i=sym.maxStringLength-str.length()-((d<0)?1:0);i>0;--i)
						str='0'+str;
					str=(d<0)?"-":""+str;
                                     *
                                     */
					return format(String.valueOf(Math.abs(d)));
				}
		}
		return new Integer(d).toString().trim();

	}
	

	
	public String format(long val) {

		if(this.isBlankWhenZero()&&val==0)
			return blankWhenZero("0");

		
		if(sym!=null) { 
			if(sym.type==Constants.LONG||
					sym.type==Constants.SHORT||
					sym.type==Constants.INTEGER) {
				if(getNumberFormat()!=null)
					return suppressLeadingZeros(addSign(getNumberFormat().format((Math.abs(val)*Math.pow(10, scale))),(val<0)));
				else
					return new Long(val).toString().trim();
			} 
			else
				if(sym.type==Constants.BIGDECIMAL)
					return format(new BigDecimal(val));
				else 
					if(sym.type==Constants.STRING) {
					return format(String.valueOf(Math.abs(val)));
					}
		}
		return new Long(val).toString().trim();

	}
	
	public String format(String str) {
		
		if(str==null) str="";
		
		if(sym==null) return str;
		if(getNumberFormat()!=null) {
			return format(parseNumber(str));
		}
		else
		 {
			return doSimpleInsertion(str);
			
		}
		
	}
	
	public String format(byte[] str) {
		
		if(str==null) str="".getBytes();
		
		if(sym!=null) {
			if(sym.type==Constants.STRING||sym.type==Constants.GROUP) {
				return doSimpleInsertion(new String(str));
				
			}
		}
		
		return new String(str);
		
	}
	 public static BigDecimal parseNumber(String str) {
		 
		str = normalizeNumberString(str);
		
		return new BigDecimal(str);
		
	}

	private static String normalizeNumberString(String str) {
		str=str.replace(" ","").trim().replace("+", "").replace("$", "").replace("*", "");
		str=str.replace("CR","").replace("DB", "").replace(",","").replaceAll("[a-zA-Z]", "");
		if(str.charAt(str.length()-1)=='-') {
			str='-'+str.substring(0,str.length()-1);
		}
		return str;
	}
	
	public static long parseLong(String str) {

		try {
			return parseNumber(str).longValue();
		}catch(Exception e) {
			return 0;
		}
		
	}

	public static int parseInt(String str) {

		try {
			return parseNumber(str).intValue();
		}catch(Exception e) {
			return 0;
		}
		
	}

	private int scale = 0;
	
	private void getJavaFormat(String pic) {
		
		int picNo=verifyCobolPicture(pic);
		String myFormat=pic.toUpperCase();
		String s="";int i=0,j=0,prev=i;
		if(picNo>=Constants.SHORT&&picNo<=Constants.BIGDECIMAL) {
			do {
				if((i=myFormat.indexOf('(',prev))>0) {
					j=myFormat.substring(i+1).indexOf(')');
					if(j<=0) reportError();
					int k=Integer.parseInt(myFormat.substring(i+1,i+1+j));
					char c=myFormat.charAt(i-1);
					//if (c=='9'||c=='Z'||c=='-'||c=='P'||c=='*'||c=='$') {
						s+=myFormat.substring(prev, i-1);
						for (int l=0;l<k;++l)
							s+=c;
						prev=i+j+2;
					//} 
				} else {
					s+=myFormat.substring(prev);
					break;
				}
				//if(s.length()>20)
					//reportError();
			} while(true);
			this.sym.isSigned=(s.charAt(0)=='S'||s.indexOf('+')>=0||s.indexOf('-')>=0||
					s.indexOf("CR")>=0||s.indexOf("DB")>=0);
			this.setScaled(s.indexOf('P')>=0);
			int maxID=0;
			int maxFD=0;
			s=s.replace("0","@");//For simple insertion editing
			String javaFormatStr=s.replace('9', '0');
			javaFormatStr=javaFormatStr.replace("DB", "DD");
			if((i=javaFormatStr.indexOf('V'))>=0) {
				for(byte c:javaFormatStr.substring(i+1).getBytes())
					if(c=='0') scale++;
				javaFormatStr=javaFormatStr.replace("V", "");
			}
			String ret;
			if(getDataCategory()!=Constants.EXTERNAL_FLOATING_POINT) {
				boolean haveSign=false,haveDollar=false;String ret2="";char c=' '; 
				for(i=0;i<javaFormatStr.length();++i) {
					c=javaFormatStr.charAt(i);
					if(c=='.') break;
					switch(c) {
					case '+':
					case '-':
						if(!haveSign) {
							ret2+=c;
							haveSign=true;
						}
						else 
							ret2+='0';
						break;
					case '$':
						if(!haveDollar) {
							ret2+=c;
							haveDollar=true;
						}
						else 
							ret2+='0';
						break;
					default:
						ret2+=c;
					}
				}
				if(c=='.'){
					String ret3=""; 
					for(i=javaFormatStr.length()-1;i>=0;--i) {
						c=javaFormatStr.charAt(i);
						if(c=='.') break;
						switch(c) {
						case '+':
						case '-':
							if(!haveSign) {
								ret3=c+ret3;
								haveSign=true;
							}
							else 
								ret3='0'+ret3;
							break;
						case '$':
							if(!haveDollar) {
								ret3=c+ret3;
								haveDollar=true;
							}
							else 
								ret3='0'+ret3;
							break;
						default:
							ret3=c+ret3;
						}
					}
					ret=ret2+'.'+ret3;
				} else
					ret=ret2;
			} else ret=javaFormatStr;
			for(int l=0;l<ret.length();++l) {
				char c=ret.charAt(l);
				if(c=='0'||c=='Z'||c=='*'||c=='P')
					maxID++;
				if(c=='.'||c=='E') {
					break;
				}
			}
			int e=-1;
			boolean isExponent=false;
			if(getDataCategory()==Constants.EXTERNAL_FLOATING_POINT)
				isExponent=((e=ret.indexOf('E'))>=0)?true:false;
			int k;
			if((k=ret.indexOf('.'))>=0) {
				for(int l=(isExponent?e:ret.length()-1);l>=k;--l) {
					char c=ret.charAt(l);
					if(c=='0'||c=='Z'||c=='*')
						maxFD++;
					if(c=='.') {
						break;
					}
				}
			}

			if(isExponent){
				ret=ret.substring(0,e+1)+javaFormatStr.substring(e+1).replace('+', '0').replace('-','0');
			}

			ret=ret.replace('Z', '0').replace('*', '0').replace('#', '0').replace(",", "").
				replace("@","").replace("B","").replace("/", "");

			this.setJavaFormat(javaFormatStr.toCharArray());
			DecimalFormat df=new DecimalFormat(ret);
			
			df.setMaximumIntegerDigits(maxID);
			df.setMinimumIntegerDigits(maxID);
			df.setMaximumFractionDigits(maxFD);
			df.setMinimumFractionDigits(maxFD);
			this.setNumberFormat(df);
		} else {
			ArrayList<TextField> a= new ArrayList<TextField>();
			TextField field = new TextField();
			do {
				if(i==0) {
					field.picChar=myFormat.charAt(i);
					if(field.picChar=='G')
						field.len=2;
					else
						field.len=1;
					++i;
				} else 
					if(myFormat.charAt(i)=='(') {
						i++;
						int l=0;
						for (j=i;j<myFormat.length();++j) {
							if(myFormat.charAt(j)>='0'&&myFormat.charAt(j)<='9')
								l=l*10+myFormat.charAt(j)-'0';
							else 
								if(myFormat.charAt(j)==')')
									break;
								else reportError();
						}
						if(l<=0) reportError();
						if(myFormat.charAt(j)==')') {
							if(field.picChar=='G')
								l*=2-1;
							field.len+=l-1;
							i=j+1;
						} else reportError();
					} else
						if(field.picChar==myFormat.charAt(i)) {
							if(field.picChar=='G')
								field.len+=2;
							else
								field.len++;
							i++;
						} else {
							a.add(field);
							field = new TextField();
							field.picChar=myFormat.charAt(i);
							if(field.picChar=='G')
								field.len+=2;
							else
								field.len=1;
							++i;
						}
				sym.maxStringLength+=field.len;
			} while(i<myFormat.length());
			a.add(field);
			fields=a.toArray(fields);
		}
		return;
	}
	
	private String getCurrencySign() {
		return RunConfig.getInstance().getCurrencySign();
	}
	
	private boolean isDecimalPointComma() {
		return RunConfig.getInstance().isDecimalPointAsComma();
	}

	private class TextField {
		char picChar;
		short len;
	}
	
	private TextField fields[]=new TextField[10];
	
	private static byte dataCategoryTemp=-1;
	
    public static boolean isNumericInteger(String pic) {
          char[] c = pic.toCharArray();
          for(int i=0;i<c.length;++i) {
              if (c[i]=='S')
                  if(i!=0)
                    return false;
                  else 
                      continue;
              else
                  if(c[i]=='Z'||c[i]=='9') {
                      continue;
                  }else
                      if(c[i]=='(') {
                          while(++i<c.length) {
                              if(c[i]>='0'&&c[i]<='9') {
                                  continue;
                              }
                              else
                                  if(c[i]==')')
                                      break;
                              return false;
                          }
                          continue;
                      }
              return false;
          }
          return true;
        }

        public static boolean isNumericDecimal(String pic) {
          char[] c = pic.toCharArray();boolean haveSeenDecimal=false;
          for(int i=0;i<c.length;++i) {
              if (c[i]=='S')
                  if(i!=0)
                    return false;
                  else
                      continue;
              else
                  if(c[i]=='V'||c[i]=='.') {
                      if(!haveSeenDecimal) {
                          haveSeenDecimal=true;
                          continue;
                      } else
                          return false;
                  }else
                  if(c[i]=='Z'||c[i]=='9'||c[i]=='P') {
                      continue;
                  }else
                      if(c[i]=='(') {
                          while(++i<c.length) {
                              if(c[i]>='0'&&c[i]<='9') {
                                  continue;
                              }
                              else
                                  if(c[i]==')')
                                      break;
                              return false;
                          }
                          continue;
                      }
              return false;
          }
          return true;
        }
        
         public static boolean isNumericEdited(String pic) {
//myFormat.matches("(((CR)|(DB)){0,1}S{0,1}([$PZ9B0/\\,\\+\\-\\*](\\([0-9]+\\))?)*([V\\.]([$PZ9B0/\\,\\+\\-\\*](\\([0-9]+\\))?)*)?((CR)|(DB)){0,1})")
          char[] c = pic.toCharArray();boolean haveSeenDecimal=false;
          for(int i=0;i<c.length;++i) {
              if (c[i]=='S')
                  if(i!=0)
                    return false;
                  else
                      continue;
              else
                  if(c[i]=='V'||c[i]=='.') {
                      if(!haveSeenDecimal) {
                          haveSeenDecimal=true;
                          continue;
                      } else
                          return false;
                  }else
                  if(c[i]=='Z'||c[i]=='9'||c[i]=='P') {
                      continue;
                  }else
                      if(c[i]=='(') {
                          while(++i<c.length) {
                              if(c[i]>='0'&&c[i]<='9') {
                                  continue;
                              }
                              else
                                  if(c[i]==')')
                                      break;
                              return false;
                          }
                          continue;
                      }
              return false;
          }
          return true;
        }

       private static Pattern numericInteger=
               Pattern.compile("(S{0,1}([9P](\\([0-9]+\\))?)*([9P](\\([0-9]+\\))?)*)");
       private static Pattern numericDecimal=
               Pattern.compile("(S{0,1}([9P](\\([0-9]+\\))?)*[V]([9P](\\([0-9]+\\))?)*)");
       private static Pattern numericEdited=
               Pattern.compile("(((CR)|(DB)){0,1}S{0,1}([$PZ9B0/\\,\\<\\>\\+\\-\\*](\\([0-9]+\\))?)*([V\\.]" +
               		"([$PZ9B0/\\,\\<\\>\\+\\-\\*](\\([0-9]+\\))?)*)?((CR)|(DB)){0,1})");
       private static Pattern alphaNumericEdited=
               Pattern.compile("([AX9B0/\\.\\,](\\([0-9]+\\))?)*");
       private static Pattern national=
               Pattern.compile("([N])*");
       private static Pattern nationalEdited=
               Pattern.compile("([WGNB09/])*");
       private static Pattern externalFloatingPoint=
               Pattern.compile("[+-]([9.V](\\([0-9]+\\))?)*[E][+-]([9])*");
       private static Pattern plainString=
               Pattern.compile("([AX](\\([0-9]+\\))?)*");
   	public static byte getDataCategory(String pic) {
   		verifyCobolPicture(pic);
   		return dataCategoryTemp;
   	}
	public static byte verifyCobolPicture(String pic) {
		if(pic==null) { 
			dataCategoryTemp=Constants.ALPHANUMERIC;
			return Constants.GROUP;
		}
		String myFormat=pic.toUpperCase();
        if(numericInteger.matcher(myFormat).matches()) {
			dataCategoryTemp=Constants.NUMERIC;
    		return Constants.INTEGER;
        }
    	else
        if(numericDecimal.matcher(myFormat).matches()) {
				dataCategoryTemp=Constants.NUMERIC;
	    		return Constants.BIGDECIMAL;
	    	}
		 else 
              if(numericEdited.matcher(myFormat).matches()) {
				dataCategoryTemp=Constants.NUMERIC_EDITED;
				return Constants.BIGDECIMAL;
    		}
        else
        if(alphaNumericEdited.matcher(myFormat).matches()) {
                dataCategoryTemp=Constants.ALPHANUMERIC_EDITED;
                return Constants.STRING;
        }
        else
            if(national.matcher(myFormat).matches()) {
                        dataCategoryTemp=Constants.NATIONAL;
                        return Constants.STRING;
        }
        else
           if(nationalEdited.matcher(myFormat).matches()) {
                            dataCategoryTemp=Constants.NATIONAL_EDITED;
                            return Constants.STRING;
            }
            else
            if(externalFloatingPoint.matcher(myFormat).matches()) {
                    dataCategoryTemp=Constants.EXTERNAL_FLOATING_POINT;
                    return Constants.BIGDECIMAL;
            }
            else {
                return -1;
            }
	}
	public static boolean isPlainString(String  picture) {
		if(picture!=null&&plainString.matcher(picture.toUpperCase()).matches())
				return true;
		return false;
	}
	public static boolean isString(String  picture) {
		if(verifyCobolPicture(picture)==Constants.STRING)
				return true;
		return false;
	}
	private static void reportError() {
		
		try{
			throw new InvalidCobolFormatException("");
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
	}
	
	public class FieldFormatData {

		public String name;
		public String pic;
		public byte type;
		public byte usage;
		public byte dataCategory;
		public short maxIntLength;
		public short maxFractionLength;
		public int maxStringLength;
		public boolean isBlankWhenZero;
		public boolean isCurrency;
		public boolean isSigned;
		public boolean isSignLeading;
		public boolean isSignSeparate;
		public boolean isJustifiedRight;
		public boolean isExternal;
		public boolean isGlobal;
		
	}

}
