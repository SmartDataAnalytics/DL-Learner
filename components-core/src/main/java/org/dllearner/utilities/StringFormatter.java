package org.dllearner.utilities;

import java.io.File;
import java.text.DecimalFormat;

import org.dllearner.utilities.statistics.Stat;


public class StringFormatter {

	
	/**
	 * formats a double value between 0 and 100 to a percentage
	 * ex: 0.7854684  will be return 78.5%
	 * @param d the double value
	 */
	public static String doubleToPercent(double d){
		return doubleToPercent( d,  1,  true);
	}
	
	public static String doubleToPercent(double d, int decimals){
		
		return doubleToPercent( d,  decimals,  true);
		
	}
	
	
	/**
	 * used for regex difficult chars like _ or %
	 * @param s
	 * @param search
	 * @param replacement
	 * @return
	 */
	public static String myReplaceAll(String s, char search, String replacement ){
		String ret ="";
		char[] arr = s.toCharArray();
		for (char anArr : arr) {
			if (anArr == search) {
				ret += replacement;
			} else {
				ret += anArr;
			}
		}
		return ret;
		
	}
	
	
	
	public static String doubleToPercent(double d, int decimals, boolean addPercentSign){
		
		String format = (decimals==0)?"00":".";
		for (int i = 0; i < decimals; i++) {
			format += "0";
		}
		format+="%";
		DecimalFormat df = new DecimalFormat( format );
		String ret = df.format(d);
		ret = (addPercentSign)?ret:ret.replaceAll("%", "");
		return ret;
		
	}
	
	public static String doubleRound(double d, int decimals, String before, String after){
		String ret ="";
		if(decimals==0){
			int retInt = (int) Math.floor((d+0.5));
			ret = retInt+"";
		}else{
			String format = ".";
			for (int i = 0; i < decimals; i++) {
				format += "0";
			}
			
			DecimalFormat df = new DecimalFormat( format );
			ret = df.format(d);
			ret = ret.replaceAll("%", "");
		}
		ret = before + ret+ after;
		return ret;
		
	}
	
	public static String convertStatPercentageToLatex(Stat s,
			int decimals,
			boolean addPercentSign,
			boolean includeSTDDeviation){
		String ret ="";
		
		ret = doubleToPercent(s.getMean(), decimals, addPercentSign);
		ret = ret.replaceAll("%", "\\%");
		if(includeSTDDeviation){
			ret += " ($\\pm$"+doubleToPercent(s.getStandardDeviation(), decimals, false)+")";
		}
		return ret;
	}
	
	public static String convertStatDoubleToLatex(Stat s,
			int decimals,
			boolean includeSTDDeviation){
		return convertStatDoubleToLatex(s, decimals,"","",includeSTDDeviation);
	}
	
	
	public static String convertStatDoubleToLatex(Stat s,
			int decimals,
			String before,
			String after,
			boolean includeSTDDeviation){
		String ret ="";
		
		ret = doubleRound(s.getMean(), decimals, before, after);
	
		if(includeSTDDeviation){
			ret += doubleRound(s.getStandardDeviation(), decimals," ($\\pm$", after+")" );
		}
		return ret;
	}
	
	
	 public static boolean isWhitespace(String str) {
	      if (str == null) {
	          return false;
	      }
	      int sz = str.length();
	      for (int i = 0; i < sz; i++) {
	          if ((!Character.isWhitespace(str.charAt(i)))) {
	              return false;
	          }
	      }
	      return true;
	  }
	
	
	public static void main(String[] args) {
		double d = 0.55555;
		System.out.println(doubleToPercent(d, 0));
		System.out.println(doubleToPercent(d, 1));
		System.out.println(doubleToPercent(d, 2));
		System.out.println(doubleToPercent(d, 3));
		System.out.println(doubleToPercent(d, 0, false));
		System.out.println(doubleToPercent(d, 1, false));
		System.out.println(doubleToPercent(d, 2, false));
		System.out.println(doubleToPercent(d, 3, false));
		d= 55.55555;
		System.out.println(doubleRound(d, 0, "|", "|"));
		System.out.println(doubleRound(d, 1, "|", "|"));
		System.out.println(doubleRound(d, 2, "|", "|"));
		System.out.println(doubleRound(d, 3, "|", "|"));
	
	}
	
	public static String checkIfDirEndsOnSlashAndRemove(String dir){
		if(dir.endsWith(File.separator)){
			dir=dir.substring(0,dir.length()-File.separator.length());
		}
		return dir;
	}
	
	
}
