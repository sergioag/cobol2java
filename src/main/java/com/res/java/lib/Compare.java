package com.res.java.lib;

import java.math.BigDecimal;
import java.math.MathContext;

public class Compare {


	public static final boolean eq(int o1,int o2) {
		return o1==o2;
	}
	public static final boolean eq(long o1,long o2) {
		return o1==o2;
	}
	public static final boolean eq(double o1,double o2) {
		return eq(_Math.getBigDecimal(o1),_Math.getBigDecimal(o2));
	}
	public static final boolean eq(double o1,BigDecimal o2) {
		return eq(_Math.getBigDecimal(o1),o2);
	}
	public static final boolean eq(BigDecimal o1,double o2) {
		return eq(o1,_Math.getBigDecimal(o2));
	}
	public static final boolean eq(BigDecimal o1,BigDecimal o2) {
		return o1.compareTo(o2)==0;
	}
	public static final boolean eq(BigDecimal o1,String o2) {
		return eq(FieldFormat.parseNumber(o2),o1);
	}
	public static final boolean eq(String o1,BigDecimal o2) {
		return eq(FieldFormat.parseNumber(o1),o2);
	}
   public static final boolean eq(byte[] o1,BigDecimal o2) {
        return eq(FieldFormat.parseNumber(new String(o1)),o2);
    }

    public static final boolean eq(BigDecimal o1,byte[] o2) {
        return eq(o1,FieldFormat.parseNumber(new String(o2)));
    }

    public static final boolean eq(char o1,String o2) {
		return o2.charAt(0)==o1&&o2.trim().length()<=1;
	}
	
    public static final boolean eq(String o1,char o2) {
		return o1.charAt(0)==o2&&o1.trim().length()<=1;
	}
	  
    public static final boolean eq(String o1,String o2) {
		return o1.trim().compareTo(o2.trim())==0;
	}
	
	public static final boolean eq(String o1,byte[] o2) {
        return eq(new String(o2),o1);
    }

    public static final boolean eq(byte[] o1,String o2) {
         return eq(new String(o1),o2);
    }
 
    public static final boolean eq(String o1,long o2) {
    	return eq(o2,o1);
    }
    
    public static final boolean eq(long o1,String o2) {
    	return new BigDecimal(zwb(o1)).compareTo(FieldFormat.parseNumber(o2))==0;
    }
	private static long zwb(long o1) {
		o1=(true/*RunConfig.getInstance().isZwb()*/)?Math.abs(o1):o1;
		return o1;
	}
 
    public static final boolean eq(long o1,byte[] o2) {
    	return new BigDecimal(String.valueOf(zwb(o1))).compareTo(FieldFormat.parseNumber(new String(o2)))==0;
    }
    
    public static final boolean eq(byte[] o1,long o2) {
    	return FieldFormat.parseNumber(new String(o1)).compareTo(new BigDecimal(String.valueOf(zwb(o2))))==0;
    }
    
    public static final boolean eq(byte[] o1,byte[] o2) {
    	return eq(new String(o1),new String(o2));
    }

		public static final boolean ne(int o1,int o2) {
			return o1!=o2;
		}
		public static final boolean ne(long o1,long o2) {
			return o1!=o2;
		}
		public static final boolean ne(double o1,double o2) {
			return ne(_Math.getBigDecimal(o1),_Math.getBigDecimal(o2));
		}
		public static final boolean ne(double o1,BigDecimal o2) {
			return ne(_Math.getBigDecimal(o1),o2);
		}
		public static final boolean ne(BigDecimal o1,double o2) {
			return ne(o1,_Math.getBigDecimal(o2));
		}
		public static final boolean ne(BigDecimal o1,BigDecimal o2) {
			return o1.compareTo(o2)!=0;
		}
		public static final boolean ne(BigDecimal o1,String o2) {
			return ne(FieldFormat.parseNumber(o2),o1);
		}
		public static final boolean ne(String o1,BigDecimal o2) {
			return ne(FieldFormat.parseNumber(o1),o2);
		}
		
		public static final boolean ne(byte[] o1,BigDecimal o2) {
	        return ne(FieldFormat.parseNumber(new String(o1)),o2);
	    }
		
		public static final boolean ne(byte[] o1,long o2) {
	        return ne(new Long(new String(o1)),o2);
	    }

	    public static final boolean ne(BigDecimal o1,byte[] o2) {
	        return ne(o1,FieldFormat.parseNumber(new String(o2)));
	    }
	    public static final boolean ne(char o1,String o2) {
			return o2.charAt(0)!=o1||o2.trim().length()!=1;
		}
		
	    public static final boolean ne(String o1,char o2) {
			return o1.charAt(0)!=o2||o1.length()!=1;
		}
		  
	   public static final boolean ne(String o1,String o2) {
			return o1.trim().compareTo(o2.trim())!=0;
		}
		
		public static final boolean ne(String o1,byte[] o2) {
	        return new String(o2).compareTo(o1) != 0;
	    }

	    public static final boolean ne(byte[] o1,String o2) {
	         return ne(new String(o1),o2);
	    }
	    
	    public static final boolean ne(String o1,long o2) {
	    	return ne(String.valueOf(o2),o1.trim());
	    }
	    
	    public static final boolean ne(long o1,String o2) {
	    	return ne(String.valueOf(o1),o2.trim());    
	    }
	 
	    public static final boolean ne(long o1,byte[] o2) {
	    	return ne(String.valueOf(o1),new String(o2).trim());
	    	    }
	    public static final boolean ne(byte[] o1,byte[] o2) {
	    	return new String(o1).trim().compareTo(new String(o2).trim()) != 0;
	    }

		public static final boolean lt(int o1,int o2) {
			return o1<o2;
		}
		public static final boolean lt(long o1,long o2) {
			return o1<o2;
		}
		public static final boolean lt(double o1,double o2) {
			return lt(_Math.getBigDecimal(o1),_Math.getBigDecimal(o2));
		}
		public static final boolean lt(double o1,BigDecimal o2) {
			return lt(_Math.getBigDecimal(o1),o2);
		}
		public static final boolean lt(BigDecimal o1,double o2) {
			return lt(o1,_Math.getBigDecimal(o2));
		}
		public static final boolean lt(BigDecimal o1,BigDecimal o2) {
			return o1.compareTo(o2)<0;
		}
		public static final boolean lt(BigDecimal o1,String o2) {
			return lt(FieldFormat.parseNumber(o2),o1);
		}
		public static final boolean lt(String o1,BigDecimal o2) {
			return lt(FieldFormat.parseNumber(o1),o2);
		}
		
	   public static final boolean lt(byte[] o1,BigDecimal o2) {
	        return lt(FieldFormat.parseNumber(new String(o1)),o2);
	    }

	    public static final boolean lt(BigDecimal o1,byte[] o2) {
	        return lt(o1,FieldFormat.parseNumber(new String(o2)));
	    }
	    public static final boolean lt(char o1,String o2) {
			return o1<o2.charAt(0);
		}
		
	    public static final boolean lt(String o1,char o2) {
			return o1.charAt(0)<o2||o1.length()<=1;
		}		
	    public static final boolean lt(byte[] o1,long o2) {
	        return lt(new Long(new String(o1)),o2);
	    }
		  
	   public static final boolean lt(String o1,String o2) {
			return o1.trim().compareTo(o2.trim())<0;
		}
		
		public static final boolean lt(String o1,byte[] o2) {
	        return new String(o2).compareTo(o1) > 0;
	    }

	    public static final boolean lt(byte[] o1,String o2) {
	         return lt(new String(o1),o2);
	    }
	    
	    public static final boolean lt(String o1,long o2) {
	    	return lt(o1.trim(),String.valueOf(o2));
	    }
	    
	    public static final boolean lt(long o1,String o2) {
	    	return lt(String.valueOf(o1),o2.trim());
	    }
	 
	    public static final boolean lt(long o1,byte[] o2) {
	    	return lt(String.valueOf(o1),new String(o2).trim());
	    }
	    public static final boolean lt(byte[] o1,byte[] o2) {
	    	return new String(o1).compareTo(new String(o2))<0;
	    }
	    
		public static final boolean le(int o1,int o2) {
			return o1<=o2;
		}
		public static final boolean le(long o1,long o2) {
			return o1<=o2;
		}
		public static final boolean le(double o1,double o2) {
			return le(_Math.getBigDecimal(o1),_Math.getBigDecimal(o2));
		}
		public static final boolean le(double o1,BigDecimal o2) {
			return le(_Math.getBigDecimal(o1),o2);
		}
		public static final boolean le(BigDecimal o1,double o2) {
			return le(o1,_Math.getBigDecimal(o2));
		}
		public static final boolean le(BigDecimal o1,BigDecimal o2) {
			return o1.compareTo(o2)<=0;
		}
		public static final boolean le(BigDecimal o1,String o2) {
			return le(FieldFormat.parseNumber(o2),o1);
		}
		public static final boolean le(String o1,BigDecimal o2) {
			return le(FieldFormat.parseNumber(o1),o2);
		}
		
	   public static final boolean le(byte[] o1,BigDecimal o2) {
	        return le(FieldFormat.parseNumber(new String(o1)),o2);
	    }

	    public static final boolean le(BigDecimal o1,byte[] o2) {
	        return le(o1,FieldFormat.parseNumber(new String(o2)));
	    }
	    public static final boolean le(char o1,String o2) {
			return o1<=o2.charAt(0);
		}
		
	    public static final boolean le(String o1,char o2) {
			return o1.charAt(0)<=o2||o1.length()<=1;
		}		
		public static final boolean le(byte[] o1,long o2) {
	        return le(new Long(new String(o1)),o2);
	    }

	   public static final boolean le(String o1,String o2) {
			return o1.trim().compareTo(o2.trim())<=0;
		}
		
		public static final boolean le(String o1,byte[] o2) {
	        return new String(o2).compareTo(o1) >= 0;
	    }

	    public static final boolean le(byte[] o1,String o2) {
	         return le(new String(o1),o2);
	    }
	    
	    public static final boolean le(String o1,long o2) {
	    	return le(o1.trim(),String.valueOf(o2));
	    }
	    
	    public static final boolean le(long o1,String o2) {
	    	return le(String.valueOf(o1),o2.trim());
	    }
	 
	    public static final boolean le(long o1,byte[] o2) {
	    	return le(String.valueOf(o1),new String(o2).trim());
	    }
	    public static final boolean le(byte[] o1,byte[] o2) {
	    	return new String(o1).trim().compareTo(new String(o2).trim()) <= 0;
	    }

		public static final boolean gt(char o1,long o2) {
			return o1-'0'==o2;
		}
		public static final boolean gt(long o1,char o2) {
			return o1==o2-'0';
		}
		public static final boolean gt(char o1,double o2) {
			return o1-'0'==o2;
		}
		public static final boolean gt(double o1,char o2) {
			return o1>o2-'0';
		}
		
	    
		public static final boolean gt(int o1,int o2) {
			return o1>o2;
		}
		public static final boolean gt(long o1,long o2) {
			return o1>o2;
		}
		public static final boolean gt(double o1,double o2) {
			return gt(_Math.getBigDecimal(o1),_Math.getBigDecimal(o2));
		}
		public static final boolean gt(double o1,BigDecimal o2) {
			return gt(_Math.getBigDecimal(o1),o2);
		}
		public static final boolean gt(BigDecimal o1,double o2) {
			return gt(o1,_Math.getBigDecimal(o2));
		}
		public static final boolean gt(BigDecimal o1,BigDecimal o2) {
			return o1.compareTo(o2)>0;
		}
		public static final boolean gt(BigDecimal o1,String o2) {
			return gt(FieldFormat.parseNumber(o2),o1);
		}
		public static final boolean gt(String o1,BigDecimal o2) {
			return gt(FieldFormat.parseNumber(o1),o2);
		}
		
	   public static final boolean gt(byte[] o1,BigDecimal o2) {
	        return gt(FieldFormat.parseNumber(new String(o1)),o2);
	    }

	    public static final boolean gt(BigDecimal o1,byte[] o2) {
	        return gt(o1,FieldFormat.parseNumber(new String(o2)));
	    }
	    public static final boolean gt(char o1,String o2) {
			return o1>o2.charAt(0);
		}
		
	    public static final boolean gt(String o1,char o2) {
			return o1.charAt(0)>o2;
		}		
		public static final boolean gt(byte[] o1,long o2) {
	        return gt(new Long(new String(o1)),o2);
	    }

	   public static final boolean gt(String o1,String o2) {
			return o1.trim().compareTo(o2.trim())>0;
		}
		
		public static final boolean gt(String o1,byte[] o2) {
	        return new String(o2).compareTo(o1) < 0;
	    }

	    public static final boolean gt(byte[] o1,String o2) {
	         return gt(new String(o1),o2);
	    }
	    
	    public static final boolean gt(String o1,long o2) {
	    	return gt(o1.trim(),String.valueOf(o2));

	    }
	    
	    public static final boolean gt(long o1,String o2) {
	    	return gt(String.valueOf(o1),o2.trim());
	    }
	 
	    public static final boolean gt(long o1,byte[] o2) {
	    	return gt(String.valueOf(o1),new String(o2).trim());
	    }
	    public static final boolean gt(byte[] o1,byte[] o2) {
	    	return new String(o1).trim().compareTo(new String(o2).trim()) > 0;
	    }
	    
		public static final boolean ge(char o1,char o2) {
			return o1==o2;
		}
		public static final boolean ge(char o1,long o2) {
			return o1-'0'==o2;
		}
		public static final boolean ge(long o1,char o2) {
			return o1==o2-'0';
		}
		public static final boolean ge(char o1,double o2) {
			return o1-'0'==o2;
		}
		public static final boolean ge(double o1,char o2) {
			return o1>=o2-'0';
		}
		
	    
		public static final boolean ge(int o1,int o2) {
			return o1>=o2;
		}
		public static final boolean ge(long o1,long o2) {
			return o1>=o2;
		}
		public static final boolean ge(double o1,double o2) {
			return ge(_Math.getBigDecimal(o1),_Math.getBigDecimal(o2));
		}
		public static final boolean ge(double o1,BigDecimal o2) {
			return ge(_Math.getBigDecimal(o1),o2);
		}
		public static final boolean ge(BigDecimal o1,double o2) {
			return ge(o1,_Math.getBigDecimal(o2));
		}
		public static final boolean ge(BigDecimal o1,BigDecimal o2) {
			return o1.compareTo(o2)>=0;
		}
		public static final boolean ge(BigDecimal o1,String o2) {
			return ge(FieldFormat.parseNumber(o2),o1);
		}
		public static final boolean ge(String o1,BigDecimal o2) {
			return ge(FieldFormat.parseNumber(o1),o2);
		}
		public static final boolean ge(byte[] o1,BigDecimal o2) {
	        return ge(FieldFormat.parseNumber(new String(o1)),o2);
	    }
	    public static final boolean ge(BigDecimal o1,byte[] o2) {
	        return ge(o1,FieldFormat.parseNumber(new String(o2)));
	    }
	    public static final boolean ge(char o1,String o2) {
			return o1>=o2.charAt(0);
		}
		
	    public static final boolean ge(String o1,char o2) {
			return o1.charAt(0)>=o2&&o1.length()>=1;
		}		
	    
		public static final boolean ge(byte[] o1,long o2) {
	        return ge(new Long(new String(o1)),o2);
	    }

	    public static final boolean ge(String o1,String o2) {
			return o1.trim().compareTo(o2.trim())>=0;
		}
		public static final boolean ge(String o1,byte[] o2) {
	        return new String(o2).compareTo(o1) <= 0;
	    }

	    public static final boolean ge(byte[] o1,String o2) {
	         return ge(new String(o1),o2);
	    }
	    public static final boolean ge(String o1,long o2) {
	    	return ge(o1.trim(),String.valueOf(o2));

	    }
	    public static final boolean ge(long o1,String o2) {
	    	return ge(String.valueOf(o1),o2.trim());
	    }
	    public static final boolean ge(long o1,byte[] o2) {
	    	return ge(String.valueOf(o1),new String(o2).trim());
	    }
	    public static final boolean ge(byte[] o1,byte[] o2) {
	    	return new String(o1).compareTo(new String(o2)) >= 0;
	    }

	    public static final boolean isPositive(int arg) {
	    	return arg>0;
	    }
	    public static final boolean isPositive(long arg) {
	    	return arg>0;
	    }
	    public static final boolean isPositive(double arg) {
	    	return arg>0;
	    }
	    public static final boolean isPositive(BigDecimal arg) {
	    	return arg.compareTo(BigDecimal.ZERO)>0;
	    }
	    
	    public static final boolean isNegative(int arg) {
	    	return arg<0;
	    }
	    public static final boolean isNegative(long arg) {
	    	return arg<0;
	    }
	    public static final boolean isNegative(double arg) {
	    	return arg<0;
	    }
	    public static final boolean isNegative(BigDecimal arg) {
	    	return arg.compareTo(BigDecimal.ZERO)<0;
	    }

	    public static final boolean isZero(int arg) {
	    	return arg==0;
	    }
	    public static final boolean isZero(long arg) {
	    	return arg==0;
	    }
	    public static final boolean isZero(double arg) {
	    	return arg==0;
	    }
	    public static final boolean isZero(BigDecimal arg) {
	    	return arg.compareTo(BigDecimal.ZERO)==0;
	    }
	    
	    public static final int spaces(char arg) {
    		if(arg<' ')
    			return -1;
    		else 
    			if(arg>' ')
    				return 1;
	    	return 0;
	    }
	    

	    public static final int spaces(double arg) {
    		if(arg<0.00)
    			return -1;
    		else 
    			if(arg>0.00)
    				return 1;
    		return 0;
	    }
	    
	    public static final int spaces(BigDecimal arg) {
			return arg.compareTo(BigDecimal.ZERO);
	    }
    
	    public static final int spaces(byte[] arg) {
	    	for(int i=0;i<arg.length;++i)
	    		if(arg[i]<' ')
	    			return -1;
	    		else 
	    			if(arg[i]>' ')
	    				return 1;
	    	return 0;
	    }
	    


	    public static final int spaces(String arg) {
	    	for(int i=0;i<arg.length();++i)
	    		if(arg.charAt(i)<' ')
	    			return -1;
	    		else 
	    			if(arg.charAt(i)>' ')
	    				return 1;
	    	return 0;
	    }

	    
	    public static final int loValues(char arg) {
    		if(arg<0)
    			return -1;
    		else 
    			if(arg>0)
    				return 1;
	    	return 0;
	    }
	    

	    public static final int loValues(double arg) {
    		if(arg<0)
    			return -1;
    		else 
    			if(arg>0)
    				return 1;
	    	return 0;
	    }
	    
	    public static final int loValues(BigDecimal arg) {
    		if(arg.compareTo(BigDecimal.ZERO)<0)
    			return -1;
    		else 
    			if(arg.compareTo(BigDecimal.ZERO)>0)
    				return 1;
	    	return 0;

	    }
    
	    public static final int loValues(byte[] arg) {
	    	for(int i=0;i<arg.length;++i)
	    		if(arg[i]<0)
	    			return -1;
	    		else 
	    			if(arg[i]>0)
	    				return 1;
	    	return 0;
	    }
	    
	    public static final int loValues(String arg) {
	    	for(int i=0;i<arg.length();++i)
	    		if(arg.charAt(i)<0)
	    			return -1;
	    		else 
	    			if(arg.charAt(i)>0)
	    				return 1;
	    	return 0;
	    } 
	    
	    ////
	    public static final int hiValues(char arg) {
    		if(arg<0x7f)
    			return -1;
    		else 
    			if(arg>0x7f)
    				return 1;
	    	return 0;
	    }
	    

	    public static final int hiValues(double arg) {
    			return -1;
	    }
	    
	    public static final int hiValues(BigDecimal arg) {
			return -1;
	    }
    
	    public static final int hiValues(byte[] arg) {
	    	for(int i=0;i<arg.length;++i)
	    		if(arg[i]<0x7f)
	    			return -1;
	    		else 
	    			if(arg[i]>0x7f)
	    				return 1;
	    	return 0;
	    }
	    


	    public static final int hiValues(String arg) {
	    	return hiValues(arg.getBytes());
	    } 
	    
	    ///
	    public static final int quotes(char arg) {
    		if(arg<'\"')
    			return -1;
    		else 
    			if(arg>'\"')
    				return 1;
	    	return 0;
	    }
	    

	    public static final int quotes(double arg) {
    			return -1;
	    }
	    
	    public static final int quotes(BigDecimal arg) {
			return -1;
	    }
    
	    public static final int quotes(byte[] arg) {
	    	for(int i=0;i<arg.length;++i)
	    		if(arg[i]<(byte)'\"')
	    			return -1;
	    		else 
	    			if(arg[i]>(byte)'\"')
	    				return 1;
	    	return 0;
	    }
	    


	    public static final int quotes(String arg) {
	    	return quotes(arg.getBytes());
	    } 
}
