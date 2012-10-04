package com.res.java.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Hashtable;
import java.util.Vector;

import com.res.java.util.NameUtil;

public class Program extends CobolBean {

	int returnCode=0;
	
	protected  int __getReturnCode() {
		return returnCode;
	}
	protected  void __setReturnCode(int returnCode) {
		this.returnCode = returnCode;
	}

	private Vector<CobolMethod> paragraphsAndSections_;
	
	public int add(CobolMethod p) {
		if(paragraphsAndSections_==null)
			paragraphsAndSections_=new Vector<CobolMethod>();
		paragraphsAndSections_.add(p);
		return paragraphsAndSections_.size()-1;
	}
	
	public CobolMethod doCobolPerform(CobolMethod from,CobolMethod thru){
		if(from==null)return null;
		if(thru==null)	thru=from;
		thru.setOpen(true);
		return doCobolGoto(from);
	}
	
	public CobolMethod doCobolPerform(CobolMethod from){
		if(from==null)return null;
		from.setOpen(true);
		return doCobolGoto(from);
	}
	
	private int exceptionCount;
	
	public CobolMethod doCobolGoto(CobolMethod to){
		if(to==null)
			return null;
		do {
			try {
			to=to.run();
			} catch(Exception e) {
				e.printStackTrace();
				if(++exceptionCount>5) {
					System.exit(1000);
				}
			}
		} while(to!=null);
		return null;
	}
	
	protected  CobolMethod getParagraphOrSection(int idx) {
		if(paragraphsAndSections_==null||
				idx<0||
				paragraphsAndSections_.size()<idx+1)
			return null;
		return paragraphsAndSections_.get(idx);
	}
	
	public void doCobolGotoStart() {
		if(paragraphOpen_==null&&paragraphsAndSections_!=null) {
			CobolMethod first=null;int i=0;
			while((first=getParagraphOrSection(i++))!=null&&first.isDeclarative)
				;
			doCobolGoto(first);
		}
	}
	
	public int doCobolExit(int current) {
		if(current<0)
			current=paragraph_;
		if(paragraphOpen_[current]) {
			paragraphOpen_[current]=false;
			return -1;//return
		}
		return (current>=0&&current+1<paragraphOpen_.length)?current+1:-1;
	}
	
	protected  boolean paragraphOpen_[];
	
	protected  int paragraph_;
///////
    /*
     * "Int","Int","Long",
	"BigDecimal","String","Bytes","Object","Object"};
     */
    protected  int __getIntArg(int idx) {
      	if(programEnv!=null)return getInt(programEnv.inValues[idx]);
		return 0;
    }
    
    protected  long __getLongArg(int idx) {
       	if(programEnv!=null)return getLong(programEnv.inValues[idx]);
		return 0;
    }
    
    protected  BigDecimal __getBigDecimalArg(int idx) {
      	if(programEnv!=null)return getBigDecimal(programEnv.inValues[idx]);
		return BigDecimal.ZERO;
    }

    protected  byte __getByteArg(int idx) {
      	if(programEnv!=null)return getByte(programEnv.inValues[idx]);
		return (byte) 0;
    }
    protected  char __getCharArg(int idx) {
     	if(programEnv!=null)return getChar(programEnv.inValues[idx]);
    	return (char)0;
    }
    
    protected  String __getStringArg(int idx) {
    	if(programEnv!=null)return getString(programEnv.inValues[idx]);
    	return null;
    }
    
    protected  String __getStringArg(int idx,int len) {
    	if(programEnv!=null)return getString(programEnv.inValues[idx],len);
    	return null;

    }

    protected  byte[] __getBytesArg(int idx) {
    	if(programEnv!=null)return getBytes(programEnv.inValues[idx]);
    	return null;
    }
    
    protected  byte[] __getBytesArg(int idx, int len) {
    	if(programEnv!=null)return getBytes(programEnv.inValues[idx],len);
    	return null;
    }
    
    protected  int __getIntResult(int idx) {
      	if(programEnv!=null)return getInt(programEnv.retValues[idx]);
		return 0;
    }
    
    protected  long __getLongResult(int idx) {
       	if(programEnv!=null)return getLong(programEnv.retValues[idx]);
		return 0;
    }
    
    protected  BigDecimal __getBigDecimalResult(int idx) {
      	if(programEnv!=null)return getBigDecimal(programEnv.retValues[idx]);
		return BigDecimal.ZERO;
    }

    protected  byte __getByteResult(int idx) {
      	if(programEnv!=null)return getByte(programEnv.retValues[idx]);
		return (byte) 0;
    }
    protected  char __getCharResult(int idx) {
     	if(programEnv!=null)return getChar(programEnv.retValues[idx]);
    	return (char)0;
    }
    
    protected  String __getStringResult(int idx) {
    	if(programEnv!=null)return getString(programEnv.retValues[idx]);
    	return null;
    }
    
    protected  String __getStringResult(int idx,int len) {
    	if(programEnv!=null)return getString(programEnv.retValues[idx],len);
    	return null;

    }

    protected  byte[] __getBytesResult(int idx) {
    	if(programEnv!=null)return getBytes(programEnv.retValues[idx]);
    	return null;
    }
    
    protected  byte[] __getBytesResult(int idx, int len) {
    	if(programEnv!=null)return getBytes(programEnv.retValues[idx],len);
    	return null;
    }
    
	
	public Object get(int idx) {
		if(programEnv.retValues==null||idx<0||idx>=programEnv.retValues.length) return null;
		return programEnv.retValues[idx];
	}
	
    public static class ProgramEnv {
    	Object[] inValues;
    	Object[] retValues=null;
    	Hashtable<String,Program> lastCalls=new Hashtable<String,Program>();
    }
    
	Hashtable<String,Object> __context=null;
    
    private ProgramEnv programEnv = null;
    
	public ProgramEnv __getProgramEnv() {
		return programEnv;
	}

	protected  Object doCobolCall(String programName,Object... args) throws Exception  {
		Program lastCall=null;
		try {
			if(programEnv==null) programEnv=new ProgramEnv();
			if((lastCall=programEnv.lastCalls.get(programName.toUpperCase()))==null) {
				lastCall=findProgram(programName);
				if(lastCall==null)
						throw new ClassNotFoundException("Cobol Program(" +programName +
						") Class not found.");
			
		} 
		Method execute=null;
		
			execute = lastCall.getClass().getMethod("execute", ProgramEnv.class);

		if(execute!=null) {
			programEnv.lastCalls.put(programName.toUpperCase(),lastCall);
			programEnv.inValues=args;
			return execute.invoke(lastCall, programEnv);
		}
		if(++exceptionCount>5) {
			System.exit(1000);
		}
		} catch (SecurityException e) {
			e.printStackTrace();
			throw new Error(e);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			throw new Error(e);
		} catch (ClassNotFoundException e) {
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
		throw new Error(new ClassNotFoundException("Cobol Program Class not found."));
	}
	protected  void doCobolReturn(Object... ret) {
		programEnv.retValues=ret;
	}
	protected void doCobolCancel(String... progs) {
		Program pg;
		if(programEnv==null) return;
		for(String pn:progs) {
			if((pg=programEnv.lastCalls.get(pn.toUpperCase()))!=null)
				pg.doCancel();
		}
	}
    private void doCancel() {
    	__initialized=false;
    }
    private Program findProgram(String name) {
    	Program ret =null;
		if((ret=findAndLoadNestedProgram(name))==null){
			if((ret=findAndLoadClass(NameUtil.convertCobolNameToJava(name, true)))==null) {
				if((ret=findAndLoadClass(name))==null) {
				}
			}
		}
    	return ret;
    }
	private Program findAndLoadClass(String programName) {
    	Class<?> ret=null;
    	try {
			ret=this.getClass().getClassLoader().loadClass(programName);
		} catch (ClassNotFoundException e) {
		}
		try {
			if(ret==null)
				ret=Class.forName(RunConfig.getInstance().getProgramPackage()+'.'+
						NameUtil.convertCobolNameToJava(programName,true));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		try {
			if(ret==null)
	    	ret=this.getClass().getClassLoader().loadClass(RunConfig.getInstance().getProgramPackage()+'.'+programName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
    	if(ret!=null)
    		if(ret.getSuperclass().getName().equalsIgnoreCase("com.res.java.lib.Program"))
				try {
					
					return (Program)ret.newInstance();
				} catch (InstantiationException e) {
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
    	return null;
    }
    
  
    private Program findAndLoadNestedProgram(String programName) {
    	Class<?> ret=null;
    	if(ret==null) {
    		this.getClass().asSubclass(this.getClass()).getSimpleName();
			String javafiedName=NameUtil.convertCobolNameToJava(programName, true);
			for(Class<?> decl:this.getClass().getDeclaredClasses())
				if(decl.getSimpleName().equals(programName)||decl.getSimpleName().equals(javafiedName)) {ret=decl;break;}
    	}
    	if(ret!=null) {
			try {
		    	if(ret!=null) {
			    	this.getClass().asSubclass(this.getClass()).getSimpleName();
			    	final Constructor<?> constructor = ret.getConstructor(new Class[] { this.getClass().asSubclass(this.getClass()) });
		    	    Program p = Program.class.cast(constructor.newInstance(new Object[] {this.getClass().asSubclass(this.getClass()).cast(this)} ));
		    	    return p;
		    	}
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	return null;
    }
    
	public Program() {
		paragraphOpen_=null;
		paragraph_=0;
	}

	public Program(CobolBytes b) {
		super(b);
		paragraphOpen_=null;
		paragraph_=0;
	}

	public int compare(String o1,String o2) {
    	return o1.trim().compareTo(o2.trim());
    }
	
	public int compare(String o1,long o2) {
        	return compare(new String(o1),o2);
        }

	public int compare(long o1,String o2) {
    	return compare(new String(o2),o1);
    }
        public int compare(byte[] o1,long o2) {
            for(byte b:o1)
            	if(b!=o2)
            		if(b<o2)
            			return -1;
            		else
            			return 1;
            return 0;
        }
        
        public int compare(byte[] o1,byte[] o2) {
            for(int i=0;i<Math.min(o1.length,o2.length);++i) {
                if(o1[i]==o2[i])
                    continue;
                if(o1[i]>o2[i])
                    return 1;
                else
                    return -1;
            }
            if((o1.length==1&&o1[0]==' ')||o2.length==1||o2[0]==' ')
                return 0;
            if(o1.length==o2.length)
                return 0;
            if(o1.length>o2.length)
                    return 1;
                else
                    return -1;
        }
        
        public int compare(BigDecimal o1,BigDecimal o2) {
        	return o1.round(MathContext.DECIMAL32).compareTo(o2.round(MathContext.DECIMAL32));
        }
        
        public int compare(String o1,byte[] o2) {
            return compare(new String(o2),o1);
        }

        public int compare(byte[] o1,String o2) {
             return compare(new String(o1),o2);
        }

        public int compare(String o1,BigDecimal o2) {
            return o1.compareTo(o2.toPlainString());
        }

        public int compare(byte[] o1,BigDecimal o2) {
            return compare(new String(o1),o2.toPlainString());
        }

        public int compare(BigDecimal o1,String o2) {
        	return compare(o2,o1.toPlainString());
        }

        public int compare(BigDecimal o1,byte[] o2) {
            return compare(o1.toPlainString(),new String(o2));
        }
  
        public int compare(BigDecimal o1,double o2) {
            return o1.compareTo(new BigDecimal(o2));
        }

        public int compare(double o1,BigDecimal o2) {
            return new BigDecimal(o1).compareTo(o2);
        }
 
        protected  void setNull(Object o,int... idx) {
        	Console.println("Attempting to access a Variable with invalid name.");
        }
        protected  Object getNull(int... idx) {
        	Console.println("Attempting to access a Variable with invalid name.");
        	return 0;
        }
        
        protected  Object notYetImplemented(Object... parms) {
        	Console.println("Not Yet Available. Contact RES Support");
        	return null;
        }

        private static DataAccessObject __dao = null;
        
		protected  DataAccessObject __dao() {
        	if(__dao==null) {
        		__dao=new DataAccessObject();
	        	try {
	        		java.lang.reflect.Field sqlca=null;
	        		__dao.program = this;
					sqlca=this.getClass().getField("sqlca");
					if(sqlca!=null) {
						__dao.sqlca=com.res.java.lib.Sqlca.class.cast(sqlca.get(this));
					}
				} catch (IllegalArgumentException e) {
				} catch (SecurityException e) {
				} catch (NoSuchFieldException e) {
				} catch (IllegalAccessException e) {
				}
        	}
        	return __dao;
        }
        
		public static final String SQLNOTFOUND="0",SQLERROR="1",SQLWARNING="2";
		@SuppressWarnings("unused")
		private boolean handledCondition=false;
		private Paragraph goto_=null;
		public void postProcess() {
			if(__dao!=null&&__dao.sqlca!=null&&handlers!=null) {
				if(__dao.sqlca.notFound()) {
					if((goto_=handlers.get(SQLNOTFOUND))!=null){
						doCobolGoto(goto_);
						handledCondition=true;
						return;
					}
				}
				if(__dao.sqlca.sqlError()) {
					if((goto_=handlers.get(SQLERROR))!=null){
						doCobolGoto(goto_);
						handledCondition=true;
						return;
					}
				}
				if(__dao.sqlca.sqlWarning()) {
					if((goto_=handlers.get(SQLWARNING))!=null){
						doCobolGoto(goto_);
						handledCondition=true;
						return;
					}
				}
			}

		}
		
	       protected  String __delimitedBy(String src,long dlm) {
	        	int i;
	        	if((i=src.indexOf(String.valueOf(dlm)))>=0)
	        		return src.substring(0,i);
	        	else
	        		return src;
	        }
			
		
       protected  String __delimitedBy(String src,char dlm) {
        	int i;
        	if((i=src.indexOf(dlm))>=0)
        		return src.substring(0,i);
        	else
        		return src;
        }
		
       
        protected  String __delimitedBy(String src,String dlm) {
        	int i;
        	if((i=src.indexOf(dlm))>=0)
        		return src.substring(0,i);
        	else
        		return src;
        }
        private Hashtable<String,Object> dataCache = null;
        protected  void  __putEnvironment(String name,Object value) {
        	if(dataCache==null)
        		dataCache=new Hashtable<String,Object>();
        	dataCache.put(name,value);
        }
        
        protected  String  __getEnvironment(String name) {
        	String key=null;
        	if(dataCache==null) return null;
        	if(name.equalsIgnoreCase("ENVIRONMENT-VALUE")){
        		if((key=(String)dataCache.get("ENVIRONMENT-NAME"))==null) return "";
        		return System.getenv(key.toString());
        	}
        	return "";
        }
        
        protected  boolean __initializeCall(ProgramEnv env,int i2) {
        	if(env==null||env.inValues.length!=i2) throw new Error("Invalid number of Arguments.");
        	programEnv=env;
        	return true;
        }
        /*
         * "Int","Int","Long",
		"BigDecimal","String","Bytes","Object","Object"};
         */
        protected  int getInt(Object o) {
        	try {
        		return (Integer) o;
        	} catch(Exception e) {
        		try {
    			if(o instanceof BigDecimal) {
    				return ((BigDecimal)o).intValue();
    			} else
        		if(o instanceof String)
        			return FieldFormat.parseInt(o.toString());
        		else 
        			if(o instanceof byte[]) {
        				return Integer.parseInt(new String((byte[])o));
        			}
        		} catch(Exception e2) {
        			
        		}
       		}
    			return 0;
        }
        
        protected  long getLong(Object o) {
        	try {
        		return (Long) o;
        	} catch(Exception e) {
        		try {
    			if(o instanceof BigDecimal) {
    				return ((BigDecimal)o).longValue();
    			} else
        		if(o instanceof String)
        			return Long.parseLong((String)o);
        		else 
        			if(o instanceof byte[]) {
        				return Long.parseLong(new String((byte[])o));
        			}
        		} catch(Exception e2) {
        			
        		}
       		}
    			return 0;
        }
        
        protected  BigDecimal getBigDecimal(Object o) {
        	try {
        		return (BigDecimal) o;
        	} catch(Exception e) {
        		try {
    			if(o instanceof Long ){
    				return new BigDecimal(getLong(o));
    			} else
    	   			if(o instanceof Integer)  {
        				return new BigDecimal(getInt(o));
        			} else
        		if(o instanceof String)
        			return new BigDecimal((String)o);
        		else 
        			if(o instanceof byte[]) {
        				return new BigDecimal(new String((byte[])o));
        			}
        		} catch(Exception e2) {
        			
        		}
       		}
    			return BigDecimal.ZERO;
        }
 
        protected  byte getByte(Object o) {
        	try {
        		
        		if(o instanceof byte[])
        			return(byte) new String((byte[])o).charAt(0);
        		if(o instanceof String)
        			return (byte)o.toString().charAt(0);
        		else if(o instanceof BigDecimal)
        			return ((BigDecimal) o).byteValue();
        		else
        			return (Byte) o;
        			
        	} catch(Exception e) {
       		}
    			return (byte) 0;
        }
        protected  char getChar(Object o) {
        	try {
        		if(o instanceof byte[])
        			return new String((byte[])o).charAt(0);
        		else if (o instanceof String)
        			return o.toString().charAt(0);
        		else
        			return (Character) o;
        	} catch(Exception e) {
       		}
    			return ' ';
        }
        
        protected  String getString(Object o) {
        	try {
        		if(o instanceof byte[])
        			return new String((byte[])o);
    			return o.toString();
        	} catch(Exception e) {
       		}
    			return "";
        }
        
        protected  String getString(Object o,int len) {
        	try {
        		if(o instanceof byte[]) {
        			return getPaddedString((byte[])o, len,' ').toString();
        		}
        		else if(o instanceof String)
        			return getPaddedString(((String)o).getBytes(), len,' ').toString();
        		else if(o instanceof Long||o instanceof Integer) {
        			return getPaddedString(o.toString().getBytes(), len,'0').toString();
        		} else if(o instanceof BigDecimal) {
        			return getPaddedString(((BigDecimal)o).toPlainString().getBytes(), len,'0').toString();
        		}
        	} catch(Exception e) {
        		e.printStackTrace();
       		}
    			return "";
        }

		private StringBuffer getPaddedString(byte[] b, int len,char pad) {
                    	StringBuffer ret=new StringBuffer();
			int l=Math.min(b.length, len);
			if(pad=='0') {//leftpad
				for(int i=0;i<len-l;++i) ret.append(pad);
				for(int i=0;i<l;++i)	ret.append((char)b[i]);
			} else {
				for(int i=0;i<l;++i) ret.append((char)b[i]);
				for(int i=l;i<len;++i)	ret.append(pad);
			}
                        
			return ret;
		}
        
        protected  byte[] getBytes(Object o) {
        	try {
        		if(o instanceof byte[])
        			return (byte[])o;
        				
        		return getString(o).getBytes();
        	} catch(Exception e) {
       		}
    			return new byte[0];
        }
        
        protected  byte[] getBytes(Object o, int len) {
        	try {
        		if(o instanceof byte[])
        			return (byte[])o;
        				
        		return getString(o,len).getBytes();
        	} catch(Exception e) {
       		}
    			return new byte[0];
        }
        
       private Hashtable<String,Paragraph> handlers = null; 
       public void registerHandler(String name,Paragraph p) {
    	   if(handlers==null)
    		   handlers=new Hashtable<String,Paragraph>();
    	   handlers.put(name,p);
       }
       
       protected  BigDecimal __round(BigDecimal data,int scale) {
    	   return data.setScale(scale,BigDecimal.ROUND_HALF_UP);
       }
       
       protected  BigDecimal __round(double data,int scale) {
    	   return new BigDecimal(data).setScale(scale,BigDecimal.ROUND_HALF_UP);
       }
       
       protected  BigDecimal __round(long data,int scale) {
    	   return new BigDecimal(data).setScale(scale,BigDecimal.ROUND_HALF_UP);
       }
       
       protected  BigDecimal __round(int data,int scale) {
    	   return new BigDecimal(data).setScale(scale,BigDecimal.ROUND_HALF_UP);
       }
 

		protected String getDebugContents__() {
			// TODO Auto-generated method stub
			return null;
		}

		protected String getDebugName__() {
			// TODO Auto-generated method stub
			return null;
		}

		protected String getDebugLine__() {
			// TODO Auto-generated method stub
			return null;
		}
 
		protected String getDebugSub_2__() {
			// TODO Auto-generated method stub
			return null;
		}

		protected String getDebugSub_3__() {
			// TODO Auto-generated method stub
			return null;
		}

		protected String getDebugSub_1__() {
			// TODO Auto-generated method stub
			return null;
		}
		protected  boolean inError=false;
       protected  int lastErrorCode=0;

	protected boolean __initialized;
	protected CobolBytes linkageSection=null;
	
	protected void __printStackTrace(Exception e) {
		if(RunConfig.getInstance().isExceptionPrintStackTraceOn())
			e.printStackTrace();
	}
	
	protected static void __processCmdLineArgs(String[] args) {
		RunConfig.getInstance().setCreateTables(false);
		if(args.length!=0) {
			for(String arg:args) {
				if(arg.equalsIgnoreCase("-createTables")) {
					RunConfig.getInstance().setCreateTables(true);
				} else 
					if(arg.startsWith("-D")) {
						RunConfig.getInstance().getDataBaseProperties().put(
								arg.substring(2, arg.indexOf('=')),
								arg.substring(arg.indexOf('=')));
				}
			}
		}
	}
	protected void __setDecimalPointIsComma() {
		RunConfig.getInstance().setDecimalPointAsComma(true);
	}
	protected boolean __isDecimalPointComma() {
		return RunConfig.getInstance().isDecimalPointAsComma();
	}
	protected void __setCurrencySign(String cs) {
		RunConfig.getInstance().setCurrencySign(cs);
	}
	protected String __getCurrencySign() {
		return RunConfig.getInstance().getCurrencySign();
	}
	
	public boolean isExceptionsEnabled=false;
	
	protected void __enableExceptions() {
		isExceptionsEnabled=true;
	}

	public void __resetExceptions() {
		isExceptionsEnabled=false;
	}
	protected String __formatPlainDecimal(double num) {
		return String.valueOf(num).replace(".", "");
	}
	protected String __formatPlainDecimal(float num) {
		return String.valueOf(num).replace(".", "");
	}
	
	protected String __formatPlainDecimal(BigDecimal num) {
		return num.toPlainString().replace(".", "");
	}
	protected String __justifyRight(BigDecimal num,int len) {
		String ret = num.abs().toPlainString();
		ret=padZeros(ret,len,num.signum()<0);
		return ret.substring(ret.length()-len);
	}
	protected String __justifyRight(long num,int len) {
		String ret = String.valueOf(Math.abs(num));
		ret=padZeros(ret,len,num<0);
		return ret.substring(ret.length()-len);
	}
	protected String __justifyRight(int num,int len) {
		String ret = String.valueOf(Math.abs(num));
		ret=padZeros(ret,len,num<0);
		return ret.substring(ret.length()-len);
	}
	protected String padZeros(String str,int len,boolean isNegative) {
			for(int i=len-str.length()-((isNegative)?1:0);i>0;--i)
				str='0'+str;
			return ((isNegative)?"-":"")+str;
	}
	
}