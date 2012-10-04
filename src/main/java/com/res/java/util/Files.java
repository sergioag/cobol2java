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

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Calendar;

import com.res.common.RESConfig;
import com.res.java.lib.RunTimeUtil;
import com.res.java.translation.symbol.SymbolProperties;

public class Files {
	
	public static PrintStream openClassFile(SymbolProperties props,boolean isData) {
			String path=NameUtil.getPackageName(props,!isData).replace('.',File.separatorChar);
			path+=File.separatorChar+NameUtil.getFileName(props,isData);	
			return openClassFile(path);
	}
	
	public static PrintStream openBeanInfoFile(SymbolProperties props,boolean isData) {
		String path=NameUtil.getPackageName(props,!isData).replace('.',File.separatorChar);
		path+=File.separatorChar+NameUtil.getBeanInfoFileName(props,isData);	
		return openClassFile(path);
}
	
	public static boolean exists(SymbolProperties props,boolean isData) {
		try {
			
			String path=NameUtil.getPackageName(props,!isData).replace('.',File.separatorChar);
			
			path+=File.separatorChar+NameUtil.getFileName(props,isData);		
			int i = path.indexOf(File.separatorChar);
			String dirStr = "";
			File dir = null;
			do {
				dirStr+=path.substring(0, i)+File.separatorChar;
				path=path.substring(i+1);			
				dir = new File(dirStr);
				if (dir.exists())
					if (dir.isDirectory())
					;
					else {
						return true;
					}
				else 
					return false;
				i = path.indexOf(File.separatorChar);
			} while (i>0);
			if(path!=null&&path.length()>0&&path.indexOf(".java")>0) {
				return new File(dir,path).exists();
			}
			return false;
		} catch(Exception e){
			return true;
		}
	
	}

	public static PrintStream openClassFile(String path) {
		try {
			int i = path.indexOf(File.separatorChar);
			String dirStr =
				RunTimeUtil.getInstance().stripQuotes(
						RESConfig.getInstance().getOutputDir().trim()
				);
			if(dirStr==null) dirStr="";
			else { 
				dirStr.replace('/',File.separatorChar);
				if(dirStr.length()>0&&dirStr.lastIndexOf(File.separatorChar)!=dirStr.length()-1) {
					dirStr+=File.separatorChar;
				}
			}
			File dir = null;
			do {
				dirStr+=path.substring(0, i)+File.separatorChar;
				path=path.substring(i+1);			
				dir = new File(dirStr);
				if (dir.exists())
					if (dir.isDirectory())
					;
					else {
						System.out.println("Cannot create output directory. File of that name exists.");
						System.exit(-1);
					}
				else 
					if(dir.mkdir())
						;
					else {
						System.out.println("Cannot create output directory. Failed.");
						System.exit(-1);
					}
				i = path.indexOf(File.separatorChar);
			} while (i>0);
			if(path!=null&&path.length()>0&&path.indexOf(".java")>0) {
				if(!RESConfig.getInstance().isOverwriteJavaFiles()) 
					if(new File(dir,path).exists()) {
							return null;
						}
					else
						if(new File(path).exists()) {
							return null;
						}
				File file;	
				file=new File(dir,path);						
				if(file.exists())
					file.delete();
				return new PrintStream(new FileOutputStream(file));
			}
		} catch(Exception e){
			e.printStackTrace();
			System.out.println("Could not open file "+path);
			System.out.println("Errors Encountered. Translation terminated.");
			System.exit(0);
		}
		return null;
	}
	
	public static String getCurrentTime() {
		String time = "";
		Calendar cal =Calendar.getInstance();
		time+=new Integer(cal.get(Calendar.HOUR_OF_DAY)).toString().trim()+":"+
			new Integer(cal.get(Calendar.MINUTE)).toString().trim()+":"+
			new Integer(cal.get(Calendar.SECOND)).toString().trim();
		return time;
	}
	
	public static String getCurrentDate() {
		String date = "";
		Calendar cal =Calendar.getInstance();
		date+=new Integer(cal.get(Calendar.MONTH)).toString().trim()+"/"+
			new Integer(cal.get(Calendar.DAY_OF_MONTH)).toString().trim()+"/"+
			new Integer(cal.get(Calendar.YEAR)).toString().trim();
		return date;
	}
}
