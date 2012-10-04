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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Calendar;

public class Console{
	
	  private static BufferedReader in =
		new BufferedReader(new InputStreamReader(System.in), 132);

	  private static Calendar cal=null;
		
	  public static void prompt(String s) {
	    System.out.print(s);
	    System.out.flush();
	  }
	
	  // read a Time line
	  public static String readTime(){
		String s="";
		try{
			cal=Calendar.getInstance();
			int i = cal.get(Calendar.HOUR_OF_DAY);
			String s2=new Integer(i).toString().trim();
			if (i<10&&i>=0)
				s+='0'+s2;
			else
				s+=s2;
			i = cal.get(Calendar.MINUTE);
			s2=new Integer(i).toString().trim();
			if (i<10&&i>=0)
				s+='0'+s2;
			else
				s+=s2;
			i = cal.get(Calendar.SECOND);
			s2=new Integer(i).toString().trim();
			if (i<10&&i>=0)
				s+='0'+s2;
			else
				s+=s2;
			i = cal.get(Calendar.MILLISECOND);
			s2=new Integer(i).toString().trim();
			if(s2.length()>2)
				s2=s2.substring(0,2);
			else
				if(s2.length()==1)
					s2+='0';
				else 
					if(s2.length()==0)
						s2="00";
			s+=s2;
			if(s.length()!=8)
				throw new Exception();
		}catch(Exception e){
			System.out.println("\nInvalid Input Time: " +s+ e);
			return null;
		}
		return s;
	  }
	
	  // read a Date line
	  public static String readDate(){
		String s="";
		try{
			cal=Calendar.getInstance();
			int i = cal.get(Calendar.YEAR);
			String s2=new Integer(i).toString().trim();
			s+=s2.substring(2);
			i = cal.get(Calendar.MONTH);i++;
			s2=new Integer(i).toString().trim();
			if (i<10&&i>=0)
				s+='0'+s2;
			else
				s+=s2;
			i = cal.get(Calendar.DAY_OF_MONTH);
			s2=new Integer(i).toString().trim();
			if (i<10&&i>=0)
				s+='0'+s2;
			else
				s+=s2;
		}catch(Exception e){
			System.out.println("\nInvalid Input Date: " + e);
			return null;
		}
		return s;
	  }
	
	  // read a Date line YYYYMMDD
	  public static String readDate2(){
		String s="";
		try{
			cal=Calendar.getInstance();
			int i = cal.get(Calendar.YEAR);
			String s2=new Integer(i).toString().trim();
			s+=s2.substring(0);
			i = cal.get(Calendar.MONTH);i++;
			s2=new Integer(i).toString().trim();
			if (i<10&&i>=0)
				s+='0'+s2;
			else
				s+=s2;
			i = cal.get(Calendar.DAY_OF_MONTH);
			s2=new Integer(i).toString().trim();
			if (i<10&&i>=0)
				s+='0'+s2;
			else
				s+=s2;
		}catch(Exception e){
			System.out.println("\nInvalid Input Date: " + e);
			return null;
		}
		return s;
	  }
	
	  
	  // read a Day line
	  public static String readDay(){
		String s="";
		try{
			cal=Calendar.getInstance();
			int i = cal.get(Calendar.YEAR);
			String s2=new Integer(i).toString().trim();
			s+=s2.substring(2);
			i = cal.get(Calendar.DAY_OF_YEAR);
			s2=new Integer(i).toString().trim();
			s2=s2.substring(0,3);
			while(s2.length()<3)
				s2='0'+s2;
			s+=s2;
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("\nInvalid Input Day: " + e);
			return null;
		}
		return s;
	  }
	  
	  // read a Day line YYYYDDD
	  public static String readDay2(){
		StringBuffer s=new StringBuffer(7);
		try{
			cal=Calendar.getInstance();
			int i = cal.get(Calendar.YEAR);
			String s2=String.valueOf(i);
			s.append(s2.substring(0));
			i = cal.get(Calendar.DAY_OF_YEAR);
			s2=String.valueOf(i);
			for(i=3-s2.length();i>0;--i)
				s.append('0');
			s.append(s2);
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("\nInvalid Input Day: " + e);
			return null;
		}
		return s.toString();
	  }
	
	  // read a Day line
	  public static String readDayOfWeek(){
		String s="";
		try{
			cal=Calendar.getInstance();
			int i = cal.get(Calendar.DAY_OF_WEEK);
			switch(i) {
			case 1 : s="7"; break;
			case 2 : s="1"; break;
			case 3 : s="2"; break;
			case 4 : s="3"; break;
			case 5 : s="4"; break;
			case 6 : s="5"; break;
			case 7 : s="6"; break;
			default: s="0";
			}
		}catch(Exception e){
			System.out.println("\nInvalid Input Day Of Week: " + e);
			return null;
		}
		return s;
	  }

	  // read a line as bytes
	  public static byte[] readBytes(){//throws InvalidFormatException {
		byte[] s=new byte[]{'\n'};
		try{
			s = in.readLine().getBytes();
		}catch(Exception e){
			//throw new InvalidFormatException("Invalid Input\n");
		}
		return s;
	  }
	  
	  // read a line
	  public static String readLine(){//throws InvalidFormatException {
		String s="";
		try{
			s = in.readLine().trim();
		}catch(Exception e){
			//throw new InvalidFormatException("Invalid Input\n");
		}
		return s;
	  }
	
	  // read a character
	  public static char readChar(){
		try{
			return readLine().charAt(0);
		}catch(Exception e){
		}
		return ' ';
	  }
	
	  //read an integer from console
	  public static int readInt(){
		try {
		  return Integer.parseInt(readLine());
		}catch(Exception e){
		}
		return 0;
	  }
	
	  //read an long from console
	  public static long readLong(){
			try {
			  return Long.parseLong(readLine());
			}catch(Exception e){
			}
			return 0;
		  }
	  
	  //read a double from console
	  public static double readDouble(){
	 	try {
	 		return Double.parseDouble(readLine());
	  	}catch(Exception e){
	  	}
	  	return 0.00;
	  }
	  
	  //Write
	  public static void print(String str) {
		  System.out.print(str);
		  System.out.flush();
	  }
	  
	  public static void print(char str) {
		  System.out.print(str);
		  System.out.flush();
	  }
	  
	  
	  public static void print(byte str) {
		  System.out.print(str);
		  System.out.flush();
	  }
	  
	  
	  public static void print(short str) {
		  System.out.print(str);
		  System.out.flush();
	  }
	  
	  public static void print(int str) {
		  System.out.print(str);
		  System.out.flush();
	  }
	  
	  public static void print(long str) {
		  System.out.print(str);
		  System.out.flush();
	  }
	  
	  public static void print(double str) {
		  System.out.print(str);
		  System.out.flush();
	  }
	  
	  public static void print(BigDecimal str) {
		  System.out.print(str.toPlainString());
		  System.out.flush();
	  }
	  
	  public static void print(byte[] str) {
		  System.out.print(new String(str));
		  System.out.flush();
	  }
	  
	  //Write
	  public static void println(String str) {
		  System.out.println(str);
		  System.out.flush();
	  }
	  
	  public static void println(char str) {
		  System.out.println(str);
		  System.out.flush();
	  }
	  
	  
	  public static void println(byte str) {
		  System.out.println(str);
		  System.out.flush();
	  }
	  
	  
	  public static void println(short str) {
		  System.out.println(str);
		  System.out.flush();
	  }
	  
	  public static void println(int str) {
		  System.out.println(str);
		  System.out.flush();
	  }
	  
	  public static void println(long str) {
		  System.out.println(str);
		  System.out.flush();
	  }
	  
	  public static void println(double str) {
		  System.out.println(str);
		  System.out.flush();
	  }
	  
	  public static void println(BigDecimal str) {
		  System.out.println(str.toPlainString());
		  System.out.flush();
	  }
	  
	  public static void println(byte[] str) {
		  System.out.println(new String(str));
		  System.out.flush();
	  }
}