package org.dllearner.utilities;


public class StringFormatter {

	
	/**
	 * formats a double value between 0 and 100 to a percentage
	 * ex: 0.7854684  will be return 78.5%
	 * @param d
	 */
	public static String doubleToPercent(double d){
			if(d>1.0 || d<0.0)return "bad format: "+d;
			else if(d == 1.0){
				return "100.0%";
			}else if(d == 0.0 ){
				return "0.0%";
			}else {
				String acc = (d*100)+"";
				acc = acc.substring(0,"55.5".length());
				return acc+"%";
			}
		
	}
}
