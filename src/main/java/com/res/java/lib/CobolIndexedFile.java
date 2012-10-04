package com.res.java.lib;

import java.io.EOFException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import com.res.java.lib.exceptions.InvalidKeyException;

public class CobolIndexedFile extends DataAccessObject implements FileAdapter {
	
	protected String CURSOR_SQL = null,WHERE_SQL=null,ORDERBY_SQL=null, 
					 UPDATE_SQL=null, INSERT_SQL=null,DELETE_SQL=null;

	private int accessMode=Constants.SEQUENTIAL_ACCESS;
	private String fileName=null;
	private CobolFileBean record;
	private int fileStatus=0;

	public CobolIndexedFile(String fileName,int accessMode,CobolBean record) {
		Properties dbProps=RunConfig.getInstance().getDataBaseProperties();
		if(dbProps==null) throw new Error("No Database Properties. Cannot process Indexed File "+fileName);
		getSQLStrings(fileName, dbProps);
		this.setFileName(fileName);
		this.accessMode=accessMode;
		this.record=record;
		initialized=true;
	}

	boolean readAllowed=false,writeAllowed=false,rewriteAllowed=false,startAllowed=false,deleteAllowed=false;
	
	private boolean initialized=false;
	
	public void openInput() throws IOException {
		initOpen(0);
		try {
			isEOF=false;
			switch(accessMode) {
			case Constants.SEQUENTIAL_ACCESS:
				readAllowed=startAllowed=true;
				break;
			case Constants.RANDOM_ACCESS:
				readAllowed=true;break;
			case Constants.DYNAMIC_ACCESS:
				readAllowed=startAllowed=true;
			default:
			}
			prepareRead();
		} catch(SQLException se) {throw new IOException(se);}
	}

	public void openOutput() throws IOException {
		initOpen(1);
		switch(accessMode) {
		case Constants.SEQUENTIAL_ACCESS:
			writeAllowed=true;
		case Constants.RANDOM_ACCESS:
			writeAllowed=true;
		case Constants.DYNAMIC_ACCESS:
			writeAllowed=true;
		default:
		}
		try {
			deleteAllowed=true;
			prepareWrite();prepareDelete();
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}

	public void openIO() throws IOException {
		initOpen(2);
		switch(accessMode) {
		case Constants.SEQUENTIAL_ACCESS:
			readAllowed=rewriteAllowed=startAllowed=deleteAllowed=true;
			break;
		case Constants.RANDOM_ACCESS:
			readAllowed=rewriteAllowed=writeAllowed=deleteAllowed=true;
			break;
		case Constants.DYNAMIC_ACCESS:
			readAllowed=rewriteAllowed=writeAllowed=startAllowed=deleteAllowed=true;
		default:
		}
		try {
			prepareDelete();
			prepareRewrite();
			prepareRead();
			deleteAllowed=true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void openExtend() throws IOException {
		initOpen(3);
		switch(accessMode) {
		case Constants.SEQUENTIAL_ACCESS:
			writeAllowed=true;
		case Constants.RANDOM_ACCESS:
		case Constants.DYNAMIC_ACCESS:
		default:
		}
	}

	private void initOpen(int mode) throws IOException {
		if(CURSOR_SQL == null||WHERE_SQL==null||ORDERBY_SQL==null|| 
				 UPDATE_SQL==null||INSERT_SQL==null||DELETE_SQL==null)
					throwNonExistantFile(mode);
		if(!initialized) throw new IOException("Indexed file adapter SQL Queries not initialized.") ;
		isEOF=false;
		super.setStatement(null);
		super.setResult(null);
		pk=null;
		assertKeyEqOnRead=false;
	}
		
	public void close() throws IOException {
		try {
			super.commit();
			super.close("INPUT");
		} catch (SQLException e) {
			throw new IOException(e);
		}
		readAllowed=writeAllowed=rewriteAllowed=startAllowed=deleteAllowed=false;
		fileStatus=0;
	}
	
	private Object pk = null;

	public boolean read() throws IOException {
		if(!readAllowed) throw new IOException("Indexed file Open mode does not allow Read.");
		if(isEOF) throwEOF();
		try {
			switch(accessMode) {
			case Constants.SEQUENTIAL_ACCESS:
		//		try{
				return readSequential();
				//} catch (EOFException e) {
//					throw new InvalidKeyException();
	//			}
			case Constants.RANDOM_ACCESS:
			case Constants.DYNAMIC_ACCESS:
				assertKeyEqOnRead=true;
				return readRandom(record.primaryKey());
			}
		} catch(SQLException se) {
			throw new IOException(se);}
		return false;
	}
	public boolean read(Object primK, int offset, int len) throws IOException {
		if(!readAllowed) throw new IOException("Indexed file Open mode does not allow Read.");
		if(isEOF) throwEOF();
		try {
			switch(accessMode) {
			case Constants.SEQUENTIAL_ACCESS:
				throw new IOException("Key Clause not allowed during Sequential Access Mode READ.");
			case Constants.RANDOM_ACCESS:
			case Constants.DYNAMIC_ACCESS:
				if(!startAllowed||record==null)return false;
				assertKeyEqOnRead=true;
				startSQL("SELECT DATA_BYTES__ FROM "+fileName+
						getAltKeyWhereClause(offset,len).replace("%%","="),primK);			
				startSQL("SELECT DATA_BYTES__ FROM "+fileName+
						getAltKeyWhereClause(offset,len).replace("%%","<="),primK);			
				//if(super.resultExists()) {
					readSequential();
				//} else 
					//throw new InvalidKeyException();
			}
		} catch(SQLException se) {
			throw new IOException(se);}
		return false;
	}
	public boolean read(Object key,int len) throws IOException {
		if(!readAllowed) throw new IOException("Indexed file Open mode does not allow Read.");
		if(isEOF) throwEOF();
		try {
			switch(accessMode) {
			case Constants.SEQUENTIAL_ACCESS:
				throw new IOException("Key Clause not allowed during Sequential Access Mode READ.");
			case Constants.RANDOM_ACCESS:
				assertKeyEqOnRead=true;
				startSQL("SELECT DATA_BYTES__ FROM "+fileName+
						" WHERE substr(?,1,"+len +") =  substr(PRIMARY_KEY__,1," +
										len+") ORDER BY PRIMARY_KEY__",key);
				startSQL("SELECT DATA_BYTES__ FROM "+fileName+
						" WHERE substr(?,1,"+len +") <=  substr(PRIMARY_KEY__,1," +
										len+") ORDER BY PRIMARY_KEY__",key);//Throws InvalidKeyException if any.
				//if(super.resultExists()) {
					readSequential();
				//} else 
					//throw new InvalidKeyException();
			}
		} catch(SQLException se) {
			throw new IOException(se);}
		return false;
	}
	public boolean read(Object key) throws IOException {
		if(!readAllowed) throw new IOException("Indexed file Open mode does not allow Read.");
		if(isEOF) throwEOF();
		try {
			switch(accessMode) {
			case Constants.SEQUENTIAL_ACCESS:
				throw new IOException("Key Clause not allowed during Sequential Access Mode READ.");
			case Constants.DYNAMIC_ACCESS:
			case Constants.RANDOM_ACCESS:
				startSQL(CURSOR_SQL+WHERE_SQL+ORDERBY_SQL,key);
				startSQL(CURSOR_SQL+WHERE_SQL.replace("=", "<=")+ORDERBY_SQL,key);
				//if(super.resultExists()) {
					readSequential();
				//} else 
					//throw new InvalidKeyException();
			}
		} catch(SQLException se) {
			throw new IOException(se);}
		return false;
	}
	public boolean readNext() throws IOException {
		if(!readAllowed) throw new IOException("Indexed file Open mode does not allow Read.");
		if(isEOF) throwEOF();
		try {
			if(accessMode==Constants.DYNAMIC_ACCESS&&pk==null) {
				super.close("INPUT");
				accessMode=Constants.SEQUENTIAL_ACCESS;
				prepareRead();
				accessMode=Constants.DYNAMIC_ACCESS;
			}
			return readSequential();
		} catch(SQLException se) {
			throw new IOException(se);
		}
	}
	
	class AltKey {
		String name;
		int offset;
		int len;
	}
	
	private AltKey[] altKeys = new AltKey[0];
	
	private int setAlternateKeys() throws SQLException {
		int i=2;
		if(altKeys!=null&&altKeys.length>0) {
			for(AltKey ak:altKeys) {
				this.set(++i,new String(((CobolBean)record).getBytes(ak.offset,ak.len)));
			}
		}
		return i;
	}


	@Override
	public boolean write() throws IOException {
		if(!writeAllowed) throw new IOException("Indexed file Open mode does not allow Write.");
		try {
			if(super.openConnection()) {
				super.loadStatement("INSERT");
				//if(pk!=null&&pk.toString().compareTo(record.primaryKey().toString())>=0)
					//throwNonAscendingKey();
				this.set(1,pk=record.primaryKey());
				this.set(2,new String(((CobolBean)record).get()));
				setAlternateKeys();
				super.executeUpdate();
				return true;
			}
		} catch(SQLException se) {
			if(se.getCause()!=null&&se.getCause() instanceof SQLException &&
					((SQLException)se.getCause()).
					getSQLState().substring(0,2).equals("23"))
				throw new InvalidKeyException();
			se.printStackTrace();
			fileStatus=22;
			throw new IOException(se);}
		return false;
	}

	public boolean rewrite()  throws IOException {
		if(!rewriteAllowed) throw new IOException("Indexed file Open mode does not allow Rewrite.");
		try {
			if(super.openConnection()) {
				super.loadStatement("UPDATE");
				super.set(1, record.primaryKey());
				super.setString(2, record.toString());
				//record.setSQLProperties(this);
				int ret=0;
				this.set(setAlternateKeys()+1,pk=record.primaryKey());
				ret=super.executeUpdate();
				if(ret<=0)
					throw new InvalidKeyException();
				return true;
			}
		} catch(SQLException se) {
			if(se.getCause()!=null&&se.getCause() instanceof SQLException &&
					((SQLException)se.getCause()).
					getSQLState().substring(0,2).equals("23"))
				throw new InvalidKeyException();
			throw new IOException(se);}
		return false;
	}

	public boolean start() throws IOException {
		return startEq(record.primaryKey());
	}
	
	private boolean assertKeyEqOnRead = false;
	
	public boolean startEq(Object o) throws IOException {
		if(!startAllowed||record==null)return false;
		assertKeyEqOnRead=true;
		startSQL(CURSOR_SQL+WHERE_SQL+ORDERBY_SQL,o);
		return startSQL(CURSOR_SQL+WHERE_SQL.replace("=", "<=")+ORDERBY_SQL,o);
	}
	
	private String getAltKeyWhereClause(int offset,int len) throws IOException {
		for(AltKey k:altKeys) {
			if(k.offset==offset) {
				String keyStr = "alt_"+k.offset+'_'+k.len;
				if(k.len==len) {
					return " WHERE ? %%  " +keyStr+" ORDER BY " +keyStr;
				} else {
					return " WHERE substr(?,1," +len +") %%  substr("+keyStr+",1," +
							len+") ORDER BY " +keyStr;
				}
			} 
		}
		throw new IOException("Start statement must specify valid record key or alternate record key.");
	}
	
	public boolean startEq(Object o,int offset,int len) throws IOException {
		if(!startAllowed||record==null)return false;
		assertKeyEqOnRead=true;
		startSQL("SELECT DATA_BYTES__ FROM "+fileName+
				getAltKeyWhereClause(offset,len).replace("%%","="),o);//Throws InvalidKeyException if any.
		return startSQL("SELECT DATA_BYTES__ FROM "+fileName+
				getAltKeyWhereClause(offset,len).replace("%%","<="),o);
	}
	
	public boolean startEq(Object o,int len) throws IOException {
		if(!startAllowed||record==null)return false;
		assertKeyEqOnRead=true;
		startSQL("SELECT DATA_BYTES__ FROM "+fileName+
				" WHERE substr(?,1,"+len +") =  substr(PRIMARY_KEY__,1," +
								len+") ORDER BY PRIMARY_KEY__",o);//Throws InvalidKeyException if any.
		return startSQL("SELECT DATA_BYTES__ FROM "+fileName+
				" WHERE substr(?,1,"+len +") <=  substr(PRIMARY_KEY__,1," +
								len+") ORDER BY PRIMARY_KEY__",o);
	}	
	public boolean startGt(Object o) throws IOException {
		if(!startAllowed||record==null)return false;
		assertKeyEqOnRead=false;
		return startSQL(CURSOR_SQL+WHERE_SQL.replace("=", "<")+ORDERBY_SQL,o);
	}

	public boolean startGt(Object o,int offset,int len) throws IOException {
		if(!startAllowed||record==null)return false;
		return startSQL("SELECT DATA_BYTES__ FROM "+fileName+
				getAltKeyWhereClause(offset,len).replace("%%","<"),o);
	}
	
	public boolean startGt(Object o,int len) throws IOException {
		if(!startAllowed||record==null)return false;
		return startSQL("SELECT DATA_BYTES__ FROM "+fileName+
				" WHERE substr(?,1,"+len +") <  substr(PRIMARY_KEY__,1," +
								len+") ORDER BY PRIMARY_KEY__",o);
	}
	
	public boolean startGe(Object o) throws IOException {
		if(!startAllowed||record==null)return false;
		assertKeyEqOnRead=false;
		return startSQL(CURSOR_SQL+WHERE_SQL.replace("=", "<=")+ORDERBY_SQL,o);
	}
	
	public boolean startGe(Object o,int offset,int len) throws IOException {
		if(!startAllowed||record==null)return false;
		return startSQL("SELECT DATA_BYTES__ FROM "+fileName+
				getAltKeyWhereClause(offset,len).replace("%%","<="),o);
	}

	public boolean startGe(Object o,int len) throws IOException {
		if(!startAllowed||record==null)return false;
		return startSQL("SELECT DATA_BYTES__ FROM "+fileName+
				" WHERE substr(?,1,"+len +") <=  substr(PRIMARY_KEY__,1," +
								len+") ORDER BY PRIMARY_KEY__",o);
	}
	
	public boolean delete()  throws IOException {
		if(!deleteAllowed) throw new IOException("Indexed file Open mode does not allow DELETE.");
		try {
			if(super.openConnection()) {
				super.loadStatement("DELETE");
				this.set(1,record.primaryKey());
				if(super.executeUpdate()!=1)
					throw new InvalidKeyException();
				return true;
			}
		} catch(SQLException se) {throw new IOException(se);}
		return false;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileStatus(int fileStatus) {
		this.fileStatus = fileStatus;
	}

	public String getFileStatus() {
		if(fileStatus==0) return "00";
		String ret = String.valueOf(fileStatus);
		if(ret.length()==1)return '0'+ret;
		else if(ret.length()==2) return ret;
		else if(ret.length()>2)return ret.substring(0,1);
		else return "99";
	}

	private void getSQLStrings(String fileName, Properties dbProps)throws Error {
		String ak = (String) dbProps.get(fileName+"_altKeys");
		String updateStr="",insertStr1="",insertStr2="";
		if(ak!=null&& ak.length()>0) {
			String[] aks=ak.split(",");
			altKeys=new AltKey[aks.length];int i=0;
			for(String s : aks) {
				altKeys[i]=new AltKey();
				altKeys[i].name=s;int j,k;
				altKeys[i].offset=Integer.parseInt(s.substring(((j=s.indexOf("alt_"))+4),k=s.indexOf('_',j+4)));
				altKeys[i].len=Integer.parseInt(s.substring(k+1));
				updateStr+=", "+s+" = ?";
				insertStr1+=", "+s;insertStr2+=",?";
				i++;
			}
			ak=','+ak;
		
		} 
	
		CURSOR_SQL="SELECT DATA_BYTES__ FROM "+fileName;
		WHERE_SQL=" WHERE ? =  PRIMARY_KEY__";
		ORDERBY_SQL=" ORDER BY PRIMARY_KEY__ ";
		
		UPDATE_SQL="UPDATE " +fileName+" set PRIMARY_KEY__ = ?, DATA_BYTES__ = ? "+ updateStr;
		INSERT_SQL="INSERT INTO " +fileName+" (PRIMARY_KEY__, DATA_BYTES__" +insertStr1+
				") VALUES (?,?" +insertStr2+") ";
		DELETE_SQL="DELETE FROM " +fileName+" ";

		if(RunConfig.getInstance().isCreateTables()) {
			Console.print("Confirm Drop and Create of table "+fileName+"(Y/N):");
			char response;
			if((response=Console.readChar())!='Y'&&response!='y') {
				Console.println("Table "+fileName+" not dropped and/or created. Continuing...");
				return;
			}
			String dropSQL="DROP TABLE " +fileName;
			String createSQL=(String)dbProps.get(fileName+"_createSQL");
			if(createSQL!=null) {
				createSQL=RunTimeUtil.getInstance().stripQuotes(createSQL,false);
				try {
					super.prepareStatement(dropSQL);
					super.executeStatement();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				try {
					super.prepareStatement(createSQL);
					super.executeStatement();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

	//File Status Error/Exception
	private void throwNonExistantFile(int openMode) throws IOException {
		fileStatus=35;
		if(openMode==0)
			throw new IOException("Open Input on a non-existant/unreadable file.");
		else
			throw new IOException("Open Output on a non-existant/unreadable file.");
		
	}
	/*
	private void throwNonAscendingKey() throws IOException {
		fileStatus=21;
		throw new IOException("Non Ascending Key during Write.");
	}
	

	private void setOpeningOpened() throws IOException {
		setStatus(41);
		throw new IOException("Attempting to open a file which is already open.");
	}
	

	private void setClosingUnOpened() throws IOException {
		setStatus(42);
		throw new IOException("Attempting to close a file which is not open.");
	}

	
	private void setStatus(int stat) {
		fileStatus=stat;
	}

	private void setReadUnopened() throws IOException {
		fileStatus=47;throw new IOException("Read operation on closed file.");
		
	}
	private void setWriteUnopened() throws IOException {
		fileStatus=48;throw new IOException("Write operation on closed file.");
		
	}
	private void setWriteIO() throws IOException {
		fileStatus=48;throw new IOException("Write operation on a file opened in I-O mode.");
		
	}
	*/
	private boolean isEOF=false;
	public void throwEOF() throws IOException {
		fileStatus=10;
		if(isEOF)
			throw new IOException("Attempting to read beyond EOF");
		else {
			super.setResult(null);
			isEOF=true;
			throw new EOFException();
		}
	}
	
	private void prepareRead() throws SQLException {
		super.close("INPUT");
		try {
			if(super.openConnection()) {
				if(accessMode==Constants.SEQUENTIAL_ACCESS) {
					super.loadStatement("INPUT");
					super.prepareStatement("INPUT",CURSOR_SQL+ORDERBY_SQL);
					super.executeQuery();
					super.saveResult("INPUT");
				}
				else {
				if(accessMode==Constants.DYNAMIC_ACCESS)
					super.prepareStatement(CURSOR_SQL+WHERE_SQL.replace("=","<=")+ORDERBY_SQL);
				else
					super.prepareStatement(CURSOR_SQL+WHERE_SQL);
				}
				super.saveStatement("INPUT");
			}
		}catch(Exception e) {
			e.printStackTrace();throw (SQLException)e;
		}
	}
	
	private void prepareWrite() throws SQLException {
		if(super.openConnection()) {
			super.prepareStatement("INSERT",INSERT_SQL);
		}
	}
	private void prepareRewrite() throws SQLException {
		if(super.openConnection()) {
			super.prepareStatement("UPDATE",UPDATE_SQL+WHERE_SQL);
		}
	}
	
	private void prepareDelete() throws SQLException {
		if(super.openConnection()) {
			super.prepareStatement("DELETE",DELETE_SQL+WHERE_SQL);
		}
	}

	private boolean readRandom(Object key) throws SQLException, InvalidKeyException {
		if(super.openConnection()) {
			super.close("INPUT");
			prepareRead();
			this.set(1,pk=key);
			super.executeQuery();
			if(super.resultExists()) {
				((CobolBean)record).valueOf(0,((CobolBean)record).size(), super.getString(1));
				isEOF=false;
				saveResult("INPUT");
				if(assertKeyEqOnRead&&!pk.equals(record.primaryKey())) {
					fileStatus=23;
					throw new InvalidKeyException();
				}
			} else {
				fileStatus=23;
				throw new InvalidKeyException();
			}
			return true;
		}
		return false;
	}

	private boolean readSequential() throws SQLException, IOException,
			EOFException {
		if(super.openConnection()) {
			super.loadStatement("INPUT");
			super.loadResult("INPUT");
			if(super.resultExists()) {
				((CobolBean)record).valueOf(0,((CobolBean)record).size(), super.getString(1));
				isEOF=false;
				saveResult("INPUT");
				pk=record.primaryKey();
				return true; 
			} else  {
				throwEOF();
				throw new EOFException();
			}
		}
		return false;
	}

	private boolean startSQL(String sql,Object primK) throws IOException {
		try {
			super.close("INPUT");
			super.prepareStatement("INPUT",sql);
			this.set(1,pk=primK);
			super.executeQuery();
			super.saveStatement("INPUT");
			super.saveResult("INPUT");
			if(!super.resultBeforeFirst()) {
				throw new InvalidKeyException();
			}
		} catch (SQLException e) {
			throw new IOException(e);
		}
		return true;
	}	
}
