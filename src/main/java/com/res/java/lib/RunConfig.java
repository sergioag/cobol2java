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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

public class RunConfig {

	private String programPackage;
	private String dataPackage ;
	private String connectionUrlJDBC;
	private String driverNameJDBC ;
	private String userNameJDBC ;
	private String passwordJDBC ;
	private boolean isEbcdicMachine=false;
	private boolean isBigEndian=true;
	private boolean createTables=false;
	private boolean exceptionPrintStackTraceOn=false;
	private boolean decimalPointAsComma=false;
	private boolean zwb=false;
	private String currencySign=null;
	private String databasePropertiesFile=null;
	private Properties dataBaseProperties=null;
	
	private Properties config=null;
	
	public void loadDefaults() {
		try {                       
			config=new Properties();
			FileInputStream fis = new FileInputStream(new File("RunConfig.properties"));
			config.load(fis);
			programPackage=RunTimeUtil.getInstance().stripQuotes(config.getProperty("programPackage","cobolprogramclasses"));
			dataPackage=RunTimeUtil.getInstance().stripQuotes(config.getProperty("dataPackage","coboldataclasses"));
			connectionUrlJDBC =RunTimeUtil.getInstance().stripQuotes((String) config.getProperty("connectionUrlJDBC"));
			driverNameJDBC =RunTimeUtil.getInstance().stripQuotes((String) config.getProperty("driverNameJDBC"));
			setUserNameJDBC(RunTimeUtil.getInstance().stripQuotes((String) config.getProperty("userNameJDBC")));
			setPasswordJDBC(RunTimeUtil.getInstance().stripQuotes((String) config.getProperty("passwordJDBC")));
			String s=config.getProperty("isEbcdicMachine");
			if(s!=null&&s.trim().length()>0)
				isEbcdicMachine=new Boolean(s);
			else
				isEbcdicMachine=false;
			setExceptionPrintStackTraceOn(new Boolean(config.getProperty("exceptionPrintStackTraceOn","false")));
			setDecimalPointAsComma(new Boolean(config.getProperty("decimalPointAsComma","false")));
			currencySign=RunTimeUtil.getInstance().stripQuotes((String) config.getProperty("currencySign"));
			databasePropertiesFile=RunTimeUtil.getInstance().stripQuotes((String) config.getProperty("databasePropertiesFile"));
			s=config.getProperty("isBigEndian");
			if(s!=null&&s.trim().length()>0)
				isBigEndian=new Boolean(s);
			else
				isBigEndian=true;
		} catch(Exception e) {
			//connectionUrlJDBC ="";
			//connectionUrlJDBC ="";
		}
	}
	
	public String get(String name) {
		if(config!=null&&name!=null){
			name=RunTimeUtil.getInstance().stripQuotes(name);
			return config.getProperty(name);
		}
		return null;
	}
	
	private static RunConfig runConfig=null;
	
	private RunConfig() {
	}
	
	public static RunConfig getInstance() {
		if(runConfig==null){
			runConfig=new RunConfig();
			runConfig.loadDefaults();
		}
		return runConfig;
	}
	
	public boolean isEbcdicMachine() {
		return isEbcdicMachine;
	}
	public void setEbcdicMachine(boolean isEbcdicMachine) {
		this.isEbcdicMachine = isEbcdicMachine;
	}

	public void setBigEndian(boolean isBigEndian) {
		this.isBigEndian = isBigEndian;
	}

	public boolean isBigEndian() {
		return isBigEndian;
	}

	public void setConnectionUrlJDBC(String connectionUrlJDBC) {
		this.connectionUrlJDBC = connectionUrlJDBC;
	}

	public String getConnectionUrlJDBC() {
		return connectionUrlJDBC;
	}

	public void setDriverNameJDBC(String driverNameJDBC) {
		this.driverNameJDBC = driverNameJDBC;
	}

	public String getDriverNameJDBC() {
		return driverNameJDBC;
	}
	
	public void setUserNameJDBC(String userNameJDBC) {
		this.userNameJDBC = userNameJDBC;
	}

	public String getUserNameJDBC() {
		return userNameJDBC;
	}

	public void setPasswordJDBC(String passwordJDBC) {
		this.passwordJDBC = passwordJDBC;
	}

	public String getPasswordJDBC() {
		return passwordJDBC;
	}

	public void setProgramPackage(String programPackage) {
		this.programPackage = programPackage;
	}

	public String getProgramPackage() {
		return programPackage;
	}

	public void setDataPackage(String dataPackage) {
		this.dataPackage = dataPackage;
	}

	public String getDataPackage() {
		return dataPackage;
	}

	public void loadDatabaseProperties() {
		if(dataBaseProperties==null) {
			File file=null;
			if(databasePropertiesFile!=null){
				file=new File(databasePropertiesFile);
				if(file.exists()&&file.canRead()){
					dataBaseProperties=new Properties();
					try {
						dataBaseProperties.loadFromXML(new FileInputStream(file));
					} catch (InvalidPropertiesFormatException e) {
						throw new Error(e);
					} catch (FileNotFoundException e) {
						throw new Error(e);
					} catch (IOException e) {
						throw new Error(e);
					}
				} 
			}
			if(dataBaseProperties==null) {
				file=new File("Database.properties");;
				if(file.exists()&&file.canRead()){
					dataBaseProperties=new Properties();
					try {
						dataBaseProperties.loadFromXML(new FileInputStream(file));
					} catch (InvalidPropertiesFormatException e) {
						throw new Error(e);
					} catch (FileNotFoundException e) {
						throw new Error(e);
					} catch (IOException e) {
						throw new Error(e);
					}
				} else {
					throw new Error("Could Not Open Database Properties file.");
				}
			}
		}
	}
	
	public Properties getDataBaseProperties() {
		if(dataBaseProperties==null)loadDatabaseProperties();
		return dataBaseProperties;
	}

	public void setDataBaseProperties(Properties dataBaseProperties) {
		this.dataBaseProperties = dataBaseProperties;
	}

	public void setCreateTables(boolean createTables) {
		this.createTables = createTables;
	}

	public boolean isCreateTables() {
		return createTables;
	}

	public void setExceptionPrintStackTraceOn(boolean exceptionPrintStackTraceOn) {
		this.exceptionPrintStackTraceOn = exceptionPrintStackTraceOn;
	}

	public boolean isExceptionPrintStackTraceOn() {
		return exceptionPrintStackTraceOn;
	}

	public void setDecimalPointAsComma(boolean decimalPointAsComma) {
		this.decimalPointAsComma = decimalPointAsComma;
	}

	public boolean isDecimalPointAsComma() {
		return decimalPointAsComma;
	}

	public void setCurrencySign(String currencySign) {
		this.currencySign = currencySign;
	}

	public String getCurrencySign() {
		return currencySign;
	}

	public void setZwb(boolean zwb) {
		this.zwb = zwb;
	}

	public boolean isZwb() {
		return zwb;
	}

}
