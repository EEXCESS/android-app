package com.aware.utils;

public class Converters {
	
	/**
	 * Converts temperature from Fahrenheit to Celsius
	 * @param fahrenheit
	 * @return
	 */
	public static float Fahrenheit2Celsius( float fahrenheit ) {
	    return (fahrenheit-32)*5/9;
	}
	
	/**
	 * Converts elapsed time (in milliseconds) to human-readable hours:minutes (string)<br/>
	 * If elapsed is 0, N/A is returned.
	 * @param milliseconds
	 * @return
	 */
	public static String readable_elapsed( long milliseconds ) {
	    if( milliseconds == 0 ) return "N/A";
	    
	    long h = (milliseconds/1000)/3600;
	    long m = ((milliseconds/1000)/60) % 60;
	    return h+"h"+((m<10)?"0"+m:m);
	}
	
	/**
	 * Checks if the string is a number
	 * @param String
	 * @return boolean
	 */
	public static boolean isNumeric(String str)
	{
	  return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
	}
}
