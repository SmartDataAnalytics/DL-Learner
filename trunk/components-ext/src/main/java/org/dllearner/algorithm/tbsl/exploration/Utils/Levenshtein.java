package org.dllearner.algorithm.tbsl.exploration.Utils;


import java.lang.Math;
import java.math.BigDecimal;

public class Levenshtein {

	
	public static double nld(String orig, String eing){
		double result = computeLevenshteinDistance(orig,eing);
		//System.out
		double length=Math.max(orig.length(),eing.length());
		
		//if distance between both is zero, then the NLD must be one
		//but because they are equal, return a very high value, so that that query will be taken.
		if(result==0.0 ){
			return 10.0;
		}
		else{
			
			double result_nld =result/length;
			return result_nld;
		}
	
	}
	
	
	//http://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#Java
	 private static int minimum(int a, int b, int c) {
         return Math.min(Math.min(a, b), c);
 }

 public static int computeLevenshteinDistance(CharSequence str1,
                 CharSequence str2) {
         int[][] distance = new int[str1.length() + 1][str2.length() + 1];

         for (int i = 0; i <= str1.length(); i++)
                 distance[i][0] = i;
         for (int j = 0; j <= str2.length(); j++)
                 distance[0][j] = j;

         for (int i = 1; i <= str1.length(); i++)
                 for (int j = 1; j <= str2.length(); j++)
                         distance[i][j] = minimum(
                                         distance[i - 1][j] + 1,
                                         distance[i][j - 1] + 1,
                                         distance[i - 1][j - 1]
                                                         + ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0
                                                                         : 1));

         return distance[str1.length()][str2.length()];
 }
}
