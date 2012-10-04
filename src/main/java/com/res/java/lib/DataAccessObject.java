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
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Hashtable;
import java.util.Properties;

public class DataAccessObject {

	
	public void set(int idx,Object o) throws SQLException {
		try {
		if(statement==null)	return ;
		statement.setObject(idx, o);
		} catch (Exception e) {
			setStatus(e);
		}	
	}
	
	public Object get(int idx) throws SQLException {
		try {
		if(result==null)	return null ;
		return result.getObject(idx);
		} catch (Exception e) {
			setStatus(e);
			return null;
		}	
	}
	
	public void setBytes(int idx,byte[] b) throws SQLException {
		try {
		if(statement==null)	return ;
		statement.setBytes(idx, b);
		} catch (Exception e) {
			setStatus(e);
		}	
	}
	
	public void setString(int idx,String s) throws SQLException {
		try {
			if(statement==null)	return ;
			//if(statement instanceof CallableStatement)
				//((CallableStatement)statement).setString(idx, s);
			//else
			statement.setString(idx,s);
			return ;
		} catch (Exception e) {
			setStatus(e);
			return ;
		}	
	}

	public void setChar(int i, Character c) throws SQLException {
		setString(i,String.valueOf(c));
	}

	public void setBigDecimal(int idx,BigDecimal b) throws SQLException {
		try {
			if(statement==null)
				return ;
			//if(statement instanceof CallableStatement)
				//((CallableStatement)statement).setBigDecimal(idx, b);
			//else
			statement.setBigDecimal(idx,b);
			return ;
		} catch (Exception e) {
			setStatus(e);
			return ;
		}	
	}


	public void setInt(int idx,Integer i) throws SQLException {
		try {
			if(statement==null)		return ;
			//if(statement instanceof CallableStatement)
				//((CallableStatement)statement).setInt(idx, i);
			//else
			statement.setInt(idx,i);
			return ;
		} catch (Exception e) {
			setStatus(e);
			return ;
		}	
	}

	public void setShort(Short idx,Short i) throws SQLException {
		try {
			if(statement==null)	return ;
			//if(statement instanceof CallableStatement)
				//((CallableStatement)statement).setShort(idx, i);
			//else
			statement.setShort(idx, i);
			return ;
		} catch (Exception e) {
			setStatus(e);
			return ;
		}	
	}

	public void setLong(int idx,Long l) throws SQLException {
		try {
		if(statement==null)	return ;
		//if(statement instanceof CallableStatement)
			//((CallableStatement)statement).setLong(idx, l);
		//else
			statement.setLong(idx, l);
		return ;
		} catch (Exception e) {
			setStatus(e);
			return ;
		}	
	}

	public Program program = null;
	public Sqlca sqlca = null;
	
	public byte[] getBytes(int idx) throws SQLException {
		try {
		if(getResult()==null)
			return null;
		return getResult().getBytes(idx);
		} catch (Exception e) {
			setStatus(e);
			return null;
		}	

	}
	
	public String getString(String name) throws SQLException {
		try {
		if(getResult()==null)
			return "";
		return getResult().getString(name);
		} catch (Exception e) {
			setStatus(e);
			return null;
		}	

	}
	public char getChar(String name) throws SQLException {
		return getString(name).charAt(0);
	}
	
	public String getString(int idx) throws SQLException {
		try {
			if(getResult()==null)
				return "";
			return getResult().getString(idx);
		} catch (Exception e) {
			setStatus(e);
			return null;
		}	
	}
	public char getChar(int idx) throws SQLException {
		return getString(idx).charAt(0);
	}
	public BigDecimal getBigDecimal(String name) throws SQLException {
		try {
			if(getResult()==null)
				return BigDecimal.ZERO;
			return getResult().getBigDecimal(name);
		} catch (Exception e) {
			setStatus(e);
			return null;
		}	
	}

	public BigDecimal getBigDecimal(int idx) throws SQLException {
		try {
			if(getResult()==null)
				return BigDecimal.ZERO;
			return getResult().getBigDecimal(idx);
		} catch (Exception e) {
			setStatus(e);
			return null;
		}	
	}

	public Integer getInt(String name) throws SQLException {
		try {
			if(getResult()==null)
				return 0;
			return getResult().getInt(name);
		} catch (Exception e) {
			setStatus(e);
			return 0;
		}	
	}

	public Integer getInt(int idx) throws SQLException {
		try {
			if(getResult()==null)
				return 0;
			return getResult().getInt(idx);
		} catch (Exception e) {
			setStatus(e);
			return 0;
		}	
	}

	public Short getShort(String name) throws SQLException {
		try {
			if(getResult()==null)
				return 0;
			return getResult().getShort(name);
		} catch (Exception e) {
			setStatus(e);
			return 0;
		}	
	}

	public Short getShort(Short idx) throws SQLException {
		try {
			if(getResult()==null)
				return 0;
			return getResult().getShort(idx);
		} catch (Exception e) {
			setStatus(e);
			return 0;
		}	
	}
	
	public Long getLong(String name) throws SQLException {
		try {
		if(getResult()==null)
			return 0L;
		return getResult().getLong(name);
		} catch (Exception e) {
			setStatus(e);
			return 0L;
		}	
	}

	public Long getLong(int idx) throws SQLException {
		try {
		if(getResult()==null)
			return 0L;
		return getResult().getLong(idx);
		} catch (Exception e) {
			setStatus(e);
			return 0L;
		}	
	}
	
	public byte[] getCallableBytes(int idx) throws SQLException {
		try {
		if(getStatement()==null)
			return null;
		return ((CallableStatement)getStatement()).getBytes(idx);
		} catch (Exception e) {
			setStatus(e);
			return null;
		}	

	}
	
	public String getCallableString(String name) throws SQLException {
		try {
		if(getStatement()==null)
			return "";
		return ((CallableStatement)getStatement()).getString(name);
		} catch (Exception e) {
			setStatus(e);
			return null;
		}	
	}
	
	public char getCallableChar(String name) throws SQLException {
		return getCallableString(name).charAt(0);
	}
	
	public String getCallableString(int idx) throws SQLException {
		try {
			if(getStatement()==null)
				return "";
			return ((CallableStatement)getStatement()).getString(idx);
		} catch (Exception e) {
			setStatus(e);
			return null;
		}	
	}
	public char getCallableChar(int idx) throws SQLException {
		return getCallableString(idx).charAt(0);
	}
	public BigDecimal getCallableBigDecimal(String name) throws SQLException {
		try {
			if(getStatement()==null)
				return BigDecimal.ZERO;
			return ((CallableStatement)getStatement()).getBigDecimal(name);
		} catch (Exception e) {
			setStatus(e);
			return null;
		}	
	}

	public BigDecimal getCallableBigDecimal(int idx) throws SQLException {
		try {
			if(getStatement()==null)
				return BigDecimal.ZERO;
			return ((CallableStatement)getStatement()).getBigDecimal(idx);
		} catch (Exception e) {
			setStatus(e);
			return null;
		}	
	}

	public Integer getCallableInt(String name) throws SQLException {
		try {
			if(getStatement()==null)
				return 0;
			return ((CallableStatement)getStatement()).getInt(name);
		} catch (Exception e) {
			setStatus(e);
			return 0;
		}	
	}

	public Integer getCallableInt(int idx) throws SQLException {
		try {
			if(getStatement()==null)
				return 0;
			return ((CallableStatement)getStatement()).getInt(idx);
		} catch (Exception e) {
			setStatus(e);
			return 0;
		}	
	}

	public Short getCallableShort(String name) throws SQLException {
		try {
			if(getStatement()==null)
				return 0;
			return ((CallableStatement)getStatement()).getShort(name);
		} catch (Exception e) {
			setStatus(e);
			return 0;
		}	
	}

	public Short getCallableShort(Short idx) throws SQLException {
		try {
			if(getStatement()==null)
				return 0;
			return ((CallableStatement)getStatement()).getShort(idx);
		} catch (Exception e) {
			setStatus(e);
			return 0;
		}	
	}
	
	public long getCallableLong(String name) throws SQLException {
		try {
		if(getStatement()==null)
			return 0;
		return ((CallableStatement)getStatement()).getLong(name);
		} catch (Exception e) {
			setStatus(e);
			return 0L;
		}	
	}

	public long getCallableLong(int idx) throws SQLException {
		try {
		if(getStatement()==null)
			return 0;
		return ((CallableStatement)getStatement()).getLong(idx);
		} catch (Exception e) {
			setStatus(e);
			return 0L;
		}	
	}
	
	public boolean setAutoCommit(boolean ac) throws SQLException {
		try {
		if(connection!=null){
			connection.setAutoCommit(ac);
			return true;
		}
		} catch (Exception e) {
			setStatus(e);
			
		}	
		return false;
	}
	
	public boolean openConnection() throws SQLException {
		try {

			if(connection==null) {
				Class.forName(RunConfig.getInstance().getDriverNameJDBC());
				if(RunConfig.getInstance().getUserNameJDBC()!=null&&
						RunConfig.getInstance().getUserNameJDBC().length()>0&&
						RunConfig.getInstance().getPasswordJDBC()!=null&&
						RunConfig.getInstance().getPasswordJDBC().length()>0)
					connection = DriverManager.getConnection(RunConfig.getInstance().getConnectionUrlJDBC(),
							RunConfig.getInstance().getUserNameJDBC(),
							RunConfig.getInstance().getPasswordJDBC());
				else
					connection = DriverManager.getConnection(RunConfig.getInstance().getConnectionUrlJDBC());
				statementTable=new Hashtable<String, PreparedStatement>();
				resultTable=new Hashtable<String, ResultSet>();
			} 
			clearStatus();
			return true;
		} catch (Exception e) {
			setStatus(e);
			return false;
		}	
	}
	
	public boolean openConnection(String url) throws SQLException {
		try {
			Class.forName(RunConfig.getInstance().getDriverNameJDBC());
			if(url==null) return openConnection();
			connection=DriverManager.getConnection(url);
			if(statementTable==null)statementTable=new Hashtable<String, PreparedStatement>();
			if(resultTable==null) resultTable=new Hashtable<String, ResultSet>();
			clearStatus();
			return true;
		} catch (Exception e) {
			setStatus(e);
			return false;
		}	
	}

	public boolean openConnection(String url,String user,String pass) throws SQLException {
		try {
			Class.forName(RunConfig.getInstance().getDriverNameJDBC());
			if(url==null)
				url=RunConfig.getInstance().getConnectionUrlJDBC();
			if(user==null)
				user=RunConfig.getInstance().getUserNameJDBC();
			if(pass==null)
				pass=RunConfig.getInstance().getPasswordJDBC();
			connection=DriverManager.getConnection(url,user,pass);
			if(statementTable==null)statementTable=new Hashtable<String, PreparedStatement>();
			if(resultTable==null) resultTable=new Hashtable<String, ResultSet>();
			clearStatus();
			return true;
		} catch (Exception e) {
			setStatus(e);
			return false;
		}	
	}
	
	public boolean openConnection(String url,String userPass,char userNamePasswordSeperatorChar) throws SQLException {
		if(url==null) url=RunConfig.getInstance().getConnectionUrlJDBC();
		String user=userPass.substring(0,userPass.indexOf(userNamePasswordSeperatorChar));
		String pass=userPass.substring(userPass.indexOf(userNamePasswordSeperatorChar)+1);
		if(user==null||user.trim().length()<=0)	user=RunConfig.getInstance().getUserNameJDBC();
		if(pass==null||pass.trim().length()<=0)	pass=RunConfig.getInstance().getPasswordJDBC();
		return openConnection(url,user,pass); 
	}
	
	public boolean openConnection(String url,String userPass) throws SQLException {
		return openConnection(url,userPass,'/');
	}
	
	
	public boolean openConnection(String url,Properties props) throws SQLException {
		try {
			Class.forName(RunConfig.getInstance().getDriverNameJDBC());
			if(url==null) url=RunConfig.getInstance().getConnectionUrlJDBC();
			connection=DriverManager.getConnection(url,props);
			if(statementTable==null)statementTable=new Hashtable<String, PreparedStatement>();
			if(resultTable==null) resultTable=new Hashtable<String, ResultSet>();
			clearStatus();
			return true;
		} catch (Exception e) {
			setStatus(e);
			return false;
		}	
	}
	
	private void postProcess() throws SQLException {
		setStatus();
		if(program!=null)program.postProcess();
	}
	
	public boolean prepareStatement(String name,String sql) throws SQLException {
 	   try {
			if(openConnection()) {
	    		   	if(sql!=null) {
						statement=connection.prepareStatement(sql);
						saveStatement(name,statement);
						postProcess();
						return true;
	    		   	}
			}} catch (Exception e) {
			setStatus(e);
			return false;
		}	
 		return false;
	}
	
	public boolean registerOutParameter(int parameterIndex, int java_sql_Types_type)  throws SQLException{
	 	   try {
				if(openConnection()) {
					((CallableStatement)statement).registerOutParameter(parameterIndex,java_sql_Types_type);
					postProcess();
					return true;
    		   	}
			} catch (Exception e) {
				setStatus(e);
				return false;
			}	
	 		return false;
	}
	
	public boolean registerOutParameter(int parameterIndex, int java_sql_Types_type,int scale)  throws SQLException{
	 	   try {
	 		  if(openConnection()) {
					((CallableStatement)statement).registerOutParameter(parameterIndex,java_sql_Types_type,scale);
					postProcess();
					return true;
  		   	}
			} catch (Exception e) {
				setStatus(e);
				return false;
			}	
	 		return false;
	}
	
	public boolean prepareCall(String sql) throws SQLException {
	 	   try {
				if(openConnection()) {
	    		   	if(sql!=null) {
						statement=connection.prepareCall(sql);
						postProcess();
						return true;
	    		   	}
				}
			} catch (Exception e) {
				setStatus(e);
				return false;
			}	
	 		return false;
		}
		
	
	public boolean prepareStatement(String sql) throws SQLException {
	 	   try {
	 		   if(openConnection()) {
	    		   	if(sql!=null) {
						statement=connection.prepareStatement(sql);
						postProcess();
						return true;
	    		   	}
			}
		} catch (Exception e) {
			setStatus(e);
			return false;
		}	
		return false;
	}
	
	public boolean declareDynamicCursor(String cursorName,String statementName) throws SQLException {
		try {
		if(openConnection()) {
			loadStatement(statementName);
			saveStatement(cursorName, getStatement());
		}
		return true;
		} catch (Exception e) {
			setStatus(e);
			return false;
		}	
	}
	
	
	//private void setStatement(PreparedStatement statement) {
		//this.statement = statement;
	//}

	public boolean setParameters(Object... parms) throws SQLException {
		if(this.statement==null||parms==null||parms.length<=0) return true;
		int p=1;
		for(Object parm:parms) {
			try {
				this.statement.setObject(p++, parm);
			} catch (Exception e) {
				setStatus(e);
				return false;
			}
		}

		return true;
	}
	
	private PreparedStatement getStatement() {
		return statement;
	}
	
	private ResultSet getResult() throws SQLException {
		return result;
	}
	
	public void saveStatement(String cursorName,PreparedStatement state) {
		if(statementTable!=null)
			statementTable.put(cursorName, state);
	}

	public void saveStatement(String cursorName) {
		if(statementTable!=null)
			statementTable.put(cursorName, getStatement());
	}
	
	public void loadStatement(String cursorName) {
		if(statementTable!=null)
			statement=statementTable.get(cursorName);
	}

	public void saveResult(String cursorName,ResultSet state) {
		if(resultTable!=null)resultTable.put(cursorName, state);
	}
	
	public void saveResult(String cursorName) {
		if(result!=null)resultTable.put(cursorName, result);
	}


	public void loadResult(String cursorName) {
		if(result!=null) result=resultTable.get(cursorName);
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public boolean resultExists() throws SQLException {
		try {
			if(getResult()==null) {
				setStatus(new SQLException("Null Result"));
			} else {
			if(getResult().next()) {
				if(sqlca!=null) {
					sqlca.sqlerrd[2]++;			
				}
				postProcess();
				return true;
			}else {
				setEOF();
			}
			}
		} catch (Exception e) {
			setStatus(e);
		}
		return false;
	}
	public boolean resultBeforeFirst() throws SQLException {
		return getResult().isBeforeFirst();
	}
	public boolean executeStatement() throws SQLException {
		if(openConnection()) {
			if(getStatement()!=null) {
				try {
					if(getStatement().execute()) {
						postProcess();
						if((result=getStatement().getResultSet())==null) {
							return false;
						}
						return true;
					}
				} catch (Exception e) {
					setStatus(e);
					return false;
				}

			}
		}
		return false;
	}
	
	public boolean executeQuery() throws SQLException {
		if(openConnection()) {
			if(getStatement()!=null) {
				try {
					if((result=getStatement().executeQuery())!=null) {
						postProcess();
						return true;
					}
				} catch (Exception e) {
					setStatus(e);
					return false;
				}

			}
		}
		return false;
	}
	
	public int executeUpdate() throws SQLException {
		if(openConnection()) {
			if(getStatement()!=null) {
				try {
					 return getStatement().executeUpdate();
				} catch (Exception e) {
					setStatus(e);
					return -1;
				}

			}
		}
		return -1;
	}
	
	
	public boolean executeStatement(String statementName) throws SQLException {
		if(openConnection()) {
			loadStatement(statementName);
			if(getStatement()!=null) {
				try {
					if(getStatement().executeQuery()!=null) {
						postProcess();
						if((result=getStatement().getResultSet())!=null) {
							saveResult(statementName,result);
						}
						else {
							return (getStatement().getUpdateCount()>=0);
						}
						return true;
					}
				} catch (Exception e) {
					setStatus(e);
					return false;
				}

			}
		}
		return false;
	}

	public boolean commit() throws SQLException {
		try {
			if(openConnection()) 
				getConnection().commit();
			postProcess();
		} catch (Exception e) {
			setStatus(e);
			return false;
		}

		return true;
	}

	public boolean rollback() throws SQLException {
		try {
			if(openConnection()) 
				getConnection().rollback();
			postProcess();
		} catch (Exception e) {
			setStatus(e);
			return false;
		}

		return true;
	}

	public boolean rollback(String savepoint) throws SQLException {
		try {
			if(openConnection()) 
				if(resourceTable!=null)
					getConnection().rollback((Savepoint)resourceTable.get(savepoint));
				else rollback();
			postProcess();
		} catch (Exception e) {
			setStatus(e);
			return false;
		}
		return true;
	}

	public boolean savePoint(String savepoint) throws SQLException {
		try {
			if(openConnection())  {
				if(resourceTable==null)
					resourceTable=new Hashtable<String,Object>();
				resourceTable.put(savepoint,getConnection().setSavepoint(savepoint));
				postProcess();
			}
		} catch (Exception e) {
			setStatus(e);
			return false;
		}

		return true;
	}
	

	public boolean fetchCursor(String cursorName) throws SQLException {
		try {
		loadStatement(cursorName); //ACME
		loadResult(cursorName);
		postProcess();
		} catch (Exception e) {
		setStatus(e);
		return false;
		}
		return true;
	}
	
	public boolean close(String cursorName) throws SQLException {
		try {
			loadStatement(cursorName);
			if (getStatement() != null) getStatement().close();
			statement = null;
			loadResult(cursorName); //Daniele Thanks
			if (getResult() != null) getResult().close();
			result = null;
			postProcess();
			if(statementTable!=null)
				statementTable.remove(cursorName);
			if(resultTable!=null)
				resultTable.remove(cursorName);
		} catch (Exception e) {
			setStatus(e);
			return false;
		}
		return true;
	}
	

	public boolean declareCursor(String cursorName, String sql) throws SQLException {
	
		if(openConnection()) {
			if(prepareStatement(sql)) {
				postProcess();
				saveStatement(cursorName,getStatement());
				return true;
			}
		}

		return false;
		
	}


	private void setStatus(Exception ex) throws SQLException {
		try {
			SQLException status;
			if(sqlca!=null) {
				if(ex instanceof SQLException)
					status=SQLException.class.cast(ex);
				else 
					throw new SQLException(ex);
				if(status!=null) {
					sqlca.sqlcode=   status.getErrorCode();
					sqlca.sqlstate=   status.getSQLState();
					sqlca.sqlerrmc = status.getMessage();
				}
				postProcess();
			} else
				throw new SQLException(ex);
		
		} catch (IllegalArgumentException e) {
			throw new SQLException(e.getMessage());
		}
	}
	
	private void setStatus() throws SQLException {
		try {
			SQLException status;
			if(sqlca!=null&&statement!=null) {
				if(statement.getWarnings()!=null) {
					status=SQLException.class.cast(statement.getWarnings());
					if(status!=null) {
						sqlca.sqlcode=   status.getErrorCode();
						sqlca.sqlstate=   status.getSQLState();
						sqlca.sqlerrmc = status.getMessage();
				}
				}
			} 
		} catch (IllegalArgumentException e) {
			throw new SQLException(e.getMessage());
		}
	}
	
	
	private void clearStatus() throws SQLException {
		try {
			if(sqlca!=null) {
				sqlca.sqlcode=   0;
				sqlca.sqlstate =   "     ";
				sqlca.sqlerrmc = "com.res.java.lib.DataAccessObject: Status - Sccesss";
				sqlca.sqlerrd[2] = 0;
			}
		} catch (IllegalArgumentException e) {
			throw new SQLException(e.getMessage());
		}
	}
	
	private void setEOF() throws SQLException {
		try {
			if(sqlca!=null) {
				sqlca.sqlcode=+100;
				sqlca.sqlstate="02000";
				sqlca.sqlerrmc="com.res.java.lib.DataAccessObject: Status - END OF RESULT";
			}
			postProcess();
		} catch (IllegalArgumentException e) {
			throw new SQLException(e.getMessage());
		}
	}
	
	protected void setStatement(PreparedStatement statement) {
		this.statement = statement;
	}

	protected void setResult(ResultSet result) {
		this.result = result;
	}

	private Connection connection=null;
	private PreparedStatement statement=null;
	private ResultSet result=null;
	
	private Hashtable<String, PreparedStatement> statementTable=null;
	private Hashtable<String, ResultSet> resultTable=null;
	private Hashtable<String,Object> resourceTable = null;

}
