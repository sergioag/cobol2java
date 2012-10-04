package com.res.java.util;

public class RESUtil {
	
	public static String getTime() {
		String s=com.res.java.lib.Console.readTime();
		if(s==null||s.length()<7) return "00:00:00.00";
		return s.substring(0,2)+":"+s.substring(2,4)+":"+s.substring(4,6)+"."+s.substring(6);
	}

	public static String getDayOfWeek() {
		String s=com.res.java.lib.Console.readDayOfWeek();
		switch(s.charAt(0)) {
		case '1':return "Monday";
		case '2':return "Tuesday";
		case '3':return "Wednesday";
		case '4':return "Thursday";
		case '5':return "Friday";
		case '6':return "Saturday";
		case '7':
		default:
				return "Sunday";
		}
	}

	public static String getDate() {
		String s=com.res.java.lib.Console.readDate();
		return s.substring(2,4)+"/"+s.substring(4,6)+"/"+s.substring(0,2);
	}

}
