package com.res.java.util;
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

import com.res.java.lib.Constants;
import com.res.java.translation.symbol.SymbolProperties.CobolSymbol;
import com.res.java.translation.symbol.SymbolProperties;
import com.res.java.translation.symbol.SymbolUtil;

public class FieldAttributes {

	public static void processDecimal(String pic,CobolSymbol sym,boolean isEdited) {
		
	    if(pic.charAt(0)=='S') {
	    	sym.setIsSigned(true);
	    	pic=pic.substring(1);
	    }
	    else
	    	sym.setIsSigned(false);
	    int i=0;StringBuilder normPic=new StringBuilder();
	    do {
	    	normPic.append(pic.charAt(i));
	    	if(i+1>=pic.length()) break;
	    	if(pic.charAt(i+1)=='(') {
	    		int l;
	    		for(int k=Integer.parseInt(pic.substring(i+2, l=pic.indexOf(')',i+1)));k>1;--k) {
	    			normPic.append(pic.charAt(i));
	    		}
	    		i=l+1;
	    		if(i>=pic.length()) break;
	    	} else
	    		++i;
	    	
	    } while(true);
	    pic=normPic.toString();
  		pic=pic.replace('V','.').replace('*', '9').replace('Z', '9');
  		/*
  		int j=pic.indexOf('+');
  		if(j>=0) {
  			pic=pic.substring(0,j)+'+'+pic.substring(j+1).replace('+','9').replace('-', '9');
  		}
  		j=pic.indexOf('-');
  		if(j>=0) {
  			pic=pic.substring(0,j)+'-'+pic.substring(j+1).replace('-', '9');
  		}
  		j=pic.indexOf('$');
  		if(j>=0) {
  			pic=pic.substring(0,j)+'$'+pic.substring(j+1).replace('$', '9');
  		}
  		*/
  		String s;

            i = pic.indexOf('.');
            //TODO More than one V,. combination
            if (i < 0) {
                i = pic.indexOf('9');
                sym.setType(Constants.INTEGER);
                if (i >= 0) {
                    processPartOfDecimal(pic, sym, false, isEdited);
                    if (pic.indexOf('P') >= 0) 
                        processPScaling(pic, sym);
                } else if (pic.indexOf('P') >= 0) {
                    processPScaling(pic, sym);
                } else {
                    sym.setType(Constants.STRING);
                    processAlpha(pic, sym);
                }
                return;
            }
		sym.setType(Constants.BIGDECIMAL);
		s =pic.substring(0, i);
		processPartOfDecimal(s,sym,false,isEdited);
		s=pic.substring(i+1);
		processPartOfDecimal(s,sym,true,isEdited);
		return;
  }

	private static void processPScaling(String pic, CobolSymbol sym) {
		
		int i=0,l=0;
		
		while (i<pic.length()) {
			if(pic.charAt(i)=='P') {
				if (i+1<pic.length()&&pic.charAt(i+1)=='(') {
					i = i+2;int k= i+1;
					while(pic.charAt(k)!=')'&&k<pic.length()) { 
						if(pic.charAt(k)>='0'&&pic.charAt(k)<='9')
							;
						else {
							SymbolUtil.getInstance().reportError("Data Name:"+sym.getName()+" has invalid picture string=>"+pic);
							System.exit(-1);
						}
						k++;
					}
					if (k>=pic.length()){
						SymbolUtil.getInstance().reportError("Data Name:"+sym.getName()+" has invalid picture string=>"+pic);
						System.exit(-1);
					}
					l+=new Integer(pic.substring(i,k)).intValue();
					i=++k;
				} else {
					++i;l++;
				}
			} else
				i++;
		}

		if(l<=0) return;
		
		if(pic.charAt(0)!='P') {
			sym.setMaxScalingLength((short)l);
		} else {
			sym.setMaxScalingLength((short)(-l));
		}
		return;
	}

	public static void processPartOfDecimal(String pic,CobolSymbol sym, boolean fraction,boolean isEdited){
		byte[] intPart = pic.getBytes();
		int j=0;short l=0;
		while (j<intPart.length) {
			if(intPart[j]=='9'||intPart[j]=='Z'||intPart[j]=='*') {
					++j;l++;
			} else if(intPart[j]=='S') {
				sym.setIsSigned(true);
				l++;j++;
			} else {
				if(intPart[j]=='$') sym.setIsCurrency(true);
				if(isEdited)l++;
				j++;
			}
		}
		
		if(sym.getType()==Constants.BIGDECIMAL)
			if (fraction){ 
				sym.setMaxFractionLength(l);
			}
			else {
				sym.setMaxIntLength(l);
			}
		else {
			sym.setType(Constants.INTEGER);
			sym.setMaxIntLength(l);
		}
	}

	
	public static void processAlpha(String pic,CobolSymbol sym){
		byte[] partsOfPic=pic.getBytes() ;
		int j=0;int len=0;
		while (j<partsOfPic.length) {
			if(partsOfPic[j]=='(') {
					int k = j+1;
					while(partsOfPic[k]!=')'&&k<partsOfPic.length) { 
						if(partsOfPic[k]>='0'&&partsOfPic[k]<='9')
							;
						else {
							SymbolUtil.getInstance().reportError("Data Name:"+sym.getName()+" has invalid picture string=>"+pic);
							System.exit(-1);
						}
						++k;
					}
					if (k>=partsOfPic.length){
						SymbolUtil.getInstance().reportError("Data Name:"+sym.getName()+" has invalid picture string=>"+pic);
						System.exit(-1);
					}
					len+=new Integer(pic.substring(j+1,k)).intValue()-1;
					j=++k;
				}
			else
			if(partsOfPic[j]=='$') {
				sym.setIsCurrency(true);
				len++;j++;
			}
			else {
				len++;
				j++;
			}
		}
		
		sym.setType(Constants.STRING);
		sym.setMaxStringLength(len);
  }
	
	//Update further
	public static int calculateLength(SymbolProperties props) {
			
    	int len=0;
    	boolean isNumber=false;
    	CobolSymbol sym=props.getJavaType();
        switch(sym.getType()) {
		case Constants.FLOAT:
		case Constants.DOUBLE:
		case Constants.BIGDECIMAL: 
			len=sym.getMaxIntLength()+sym.getMaxFractionLength()+Math.abs(sym.getMaxScalingLength());
			isNumber=true;
			break;
		case Constants.LONG:
		case Constants.INTEGER:
		case Constants.SHORT:
			len=sym.getMaxIntLength()+Math.abs(sym.getMaxScalingLength());
			isNumber=true;
			break;
		case Constants.STRING:
			len=sym.getMaxStringLength();
			break;
		}
		
		if(props.getDataCategory()==Constants.NUMERIC_EDITED) {
			sym.setType(Constants.STRING);
			if(sym.getMaxFractionLength()>0)
				len++;
			sym.setMaxIntLength((short)0);sym.setMaxFractionLength((short)0);
			sym.setMaxStringLength(len);
			isNumber=false;
		}

		if(isNumber) {
			switch(sym.getUsage()) {
			case Constants.BINARY:
				props.setAdjustedLength(len);
				if(len>=1&&len<=4) {
					len=2;
					}
					else 
						if(len>=5&&len<=9) {
					len=4;
					}
					else 
						if(len>=10) {
							len=8;	
							if(sym.getType()==Constants.INTEGER)
								sym.setType(Constants.LONG);
						}
						else 
							len=0;
				break;
			case Constants.PACKED_DECIMAL:
				props.setAdjustedLength(len);
				int len2=len;
				len=len2/2;
				if(len2%2>0)
					len++;
				if(len>=1&&len<=4) {
				}
				else 
					if(len>=5&&len<=10) {
						if(sym.getType()==Constants.INTEGER)
							sym.setType(Constants.LONG);
					}
					else 
						len=0;
				break;
			case Constants.DISPLAY:
				props.setAdjustedLength(len);
				if(sym.isIsSigned())len++;
				if(sym.isIsCurrency()) len++;
				if(len>=1&&len<=4) {
				}
				else
					if(len>=5&&len<=9) {
					}
					else 
						if(len>=10) {
							if(sym.getType()==Constants.INTEGER)
								sym.setType(Constants.LONG);
						}
						else 
							len=0;
				break;
			default:
				props.setAdjustedLength(len);
	    	}
		} else 
			if(sym.getMaxStringLength()==1)
				sym.setType(Constants.CHAR);
		return len;
	}
}
