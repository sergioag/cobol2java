package com.res.common;
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
import java.util.Properties;

import com.res.java.util.NameUtil;

public class RESConfig implements Cloneable {

	public final String versionString = " alpha1.9(08/05/2010) ";
	private String programPackage;
	private String dataPackage ;
	private String outputDir=null;
	private String configFile="."+File.separatorChar+"Config.properties";
	private boolean longDataPackageName ;
	private boolean doListDir ;
	private boolean printCobolStatementsAsComments;
	private boolean retainCobolComments;
	private int optimizeAlgorithm=0;
	private boolean isEbcdicMachine=false;
	private boolean isFixedFormat=true;
	private boolean isBigEndian=true;
	private byte procedureFormat;//0-Use CobolMethod/Paragraph/Section classes;1-Switch-Case-Loop;2-Java Native Recursive
	private byte dataNameFormat;//0-first char upper for classes and lower for objects and fields, methods
	private boolean toMigrateVSAMISAM=false;
	private boolean toGenVSAMISAMDb=false;
	private boolean isOverwriteJavaFiles=false;
	private boolean isRenameJavaFiles=false;
	private boolean isInError=false;
	private boolean allSymbols=false;
	private boolean generateBeanInfo=false;
	private boolean exception_PrintStackTrace_On=false;
	private int inlineStatement=0;
	private int tabLength = 4;
	private int sqlDatabaseType = 0;
	private String tabSpaces = "    ";

	private static Properties config=null;
	
	public void loadDefaults() {
		try {
			config=new Properties();
			FileInputStream fis = new FileInputStream(new File(configFile));
			config.load(fis);
			programPackage =(String) config.getProperty("programPackage");
			dataPackage =(String) config.getProperty("dataPackage");
			outputDir = (String) config.getProperty("outputDir","");
			String s = (String)config.getProperty("printCobolStatementsAsComments","false");
			printCobolStatementsAsComments=false;
			s = (String)config.getProperty("retainCobolComments","false");
			retainCobolComments=false;
			s=config.getProperty("isEbcdicMachine","false");
			isEbcdicMachine=false;
			s=config.getProperty("isFixedFormat","true");
			isFixedFormat=true;
			s=config.getProperty("isBigEndian","true");
			isBigEndian=true;
			procedureFormat=0;
			s = (String)config.getProperty("dataNameFormat","0");
			dataNameFormat=new Byte(s.trim()).byteValue();
			s = (String)config.getProperty("isOverwriteJavaFiles","false");
			isOverwriteJavaFiles=new Boolean(s.trim()).booleanValue();
			s = config.getProperty("optimizeAlgorithm","0");
			optimizeAlgorithm=Integer.parseInt(s);
			s = config.getProperty("generateBeanInfo","false");
			generateBeanInfo=new Boolean(s.trim()).booleanValue();
			s = config.getProperty("exceptionPrintStackTraceOn","false");
			setExceptionPrintStackTraceOn(new Boolean(s.trim()).booleanValue());
			s = config.getProperty("tabLength","4");
			tabLength=Integer.parseInt(s);
			s = config.getProperty("sqlDatabaseType","1");
			sqlDatabaseType=Integer.parseInt(s);
			programPackage=applyNameValidation(programPackage);
			dataPackage=applyNameValidation(dataPackage);
		} catch(Exception e) {
			programPackage ="cobolprogramclasses";
			dataPackage ="coboldataclasses";
			outputDir="";
		}
		tabSpaces="";
		for(int i=0;i<tabLength;++i)
			tabSpaces+=' ';
	}
	
	private String applyNameValidation(String pkg) {
		StringBuffer pkgBuff=new StringBuffer(pkg);
		int i=0,j=0;
		while((j=pkgBuff.indexOf(".",i))>i) {
			pkgBuff.replace(i, j, NameUtil.convertCobolNameToJava(
					pkgBuff.substring(i, j), false).toLowerCase());
			i=j;
		}
		return pkgBuff.toString();
	}
	
	private static RESConfig resConfig=null;
	
	private RESConfig() {
	}
	
	public static RESConfig getInstance()  {
		if(resConfig==null){
			resConfig=new RESConfig();
			resConfig.loadDefaults();
		} 		
		return resConfig;
	}
	
	public void setDoListDir(boolean doListDir) {
		this.doListDir = doListDir;
	}

	public boolean isDoListDir() {
		return doListDir;
	}

	public boolean isFixedFormat() {
		return isFixedFormat;
	}

	public void setFixedFormat(boolean isFixedFormat) {
		this.isFixedFormat = isFixedFormat;
	}

	public boolean isEbcdicMachine() {
		return isEbcdicMachine;
	}
	public void setEbcdicMachine(boolean isEbcdicMachine) {
		this.isEbcdicMachine = isEbcdicMachine;
	}
	public String getProgramPackage() {
		return programPackage;
	}
	public void setProgramPackage(String programPackage) {
		this.programPackage = programPackage;
	}
	public String getDataPackage() {
		return dataPackage;
	}
	public void setDataPackage(String dataPackage) {
		this.dataPackage = dataPackage;
	}
	public boolean isPrintCobolStatementsAsComments() {
		return printCobolStatementsAsComments;
	}
	public void setPrintCobolStatementsAsComments(
			boolean printCobolStatementsAsComments) {
		this.printCobolStatementsAsComments = printCobolStatementsAsComments;
	}
	public boolean isRetainCobolComments() {
		return retainCobolComments;
	}
	public void setRetainCobolComments(boolean retainCobolComments) {
		this.retainCobolComments = retainCobolComments;
	}
	public byte getDataNameFormat() {
		return dataNameFormat;
	}
	public void setDataNameFormat(byte dataNameFormat) {
		this.dataNameFormat = dataNameFormat;
	}
	
	public void setBigEndian(boolean isBigEndian) {
		this.isBigEndian = isBigEndian;
	}

	public boolean isBigEndian() {
		return isBigEndian;
	}

	public void setProcedureFormat(byte procedureFormat) {
		this.procedureFormat = procedureFormat;
	}

	public byte getProcedureFormat() {
		return this.procedureFormat;
	}

	public void setInError(boolean isInError) {
		this.isInError = isInError;
	}

	public boolean isInError() {
		return isInError;
	}
	
	public boolean isOverwriteJavaFiles() {
		return isOverwriteJavaFiles;
	}

	public void setOverwriteJavaFiles(boolean isOverwriteJavaFiles) {
		this.isOverwriteJavaFiles = isOverwriteJavaFiles;
	}

	public boolean isRenameJavaFiles() {
		return isRenameJavaFiles;
	}

	public void setRenameJavaFiles(boolean isRenameJavaFiles) {
		this.isRenameJavaFiles = isRenameJavaFiles;
	}

	public void setOptimizeAlgorithm(int optimizeAlgorithm) {
		this.optimizeAlgorithm = optimizeAlgorithm;
	}

	public int getOptimizeAlgorithm() {
		return optimizeAlgorithm;
	}

	public void setLongDataPackageName(boolean longDataPackageName) {
		this.longDataPackageName = longDataPackageName;
	}

	public boolean isLongDataPackageName() {
		return longDataPackageName;
	}

	public void setToMigrateVSAMISAM(boolean toMigrateVSAMISAM) {
		this.toMigrateVSAMISAM = toMigrateVSAMISAM;
	}

	public boolean isToMigrateVSAMISAM() {
		return toMigrateVSAMISAM;
	}

	public void setToGenVSAMISAMDb(boolean toGenVSAMISAMDb) {
		this.toGenVSAMISAMDb = toGenVSAMISAMDb;
	}

	public boolean isToGenVSAMISAMDb() {
		return toGenVSAMISAMDb;
	}

	public void setAllSymbols(boolean allSymbols) {
		this.allSymbols = allSymbols;
	}

	public boolean isAllSymbols() {
		return allSymbols;
	}

	public void setInlineStatement(int inlineStatement) {
		this.inlineStatement = inlineStatement;
	}

	public int getInlineStatement() {
		return inlineStatement;
	}

	public void setTabLength(int tabLength) {
		this.tabLength = tabLength;
	}

	public int getTabLength() {
		return tabLength;
	}
	public String getTabSpaces() {
		return tabSpaces;
	}

	public void setTabSpaces(String tabSpaces) {
		this.tabSpaces = tabSpaces;
	}

	public void setGenerateBeanInfo(boolean generateBeanInfo) {
		this.generateBeanInfo = generateBeanInfo;
	}

	public boolean isGenerateBeanInfo() {
		return generateBeanInfo;
	}

	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}

	public String getOutputDir() {
		return outputDir;
	}

	public boolean isUsePointers() {
		return false;
	}

	public void setExceptionPrintStackTraceOn(boolean exception_PrintStackTrace_On) {
		this.exception_PrintStackTrace_On = exception_PrintStackTrace_On;
	}

	public boolean isExceptionPrintStackTraceOn() {
		return exception_PrintStackTrace_On;
	}

	public void setConfigFile(String configFile) {
		this.configFile = configFile;
		loadDefaults();
	}

	public String getConfigFile() {
		return configFile;
	}

	public void setSqlDatabaseType(int sqlDatabaseType) {
		this.sqlDatabaseType = sqlDatabaseType;
	}

	public int getSqlDatabaseType() {
		return sqlDatabaseType;
	}

}
