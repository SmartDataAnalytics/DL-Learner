package org.dllearner.algorithm.tbsl.exploration.Sparql;


import java.lang.Math;
import java.math.BigDecimal;

public class Levenshtein {

	
	//http://de.wikipedia.org/wiki/Levenshtein-Distanz
	public double nld(String orig, String eing){
		//int result = diff(orig,eing);
		int result = computeLevenshteinDistance(orig,eing);
		int length=Math.max(orig.length(),eing.length());
		
		
		//if distance between both is zero, then the NLD must be one
		if(result==0 ){
			return 1;
		}
		else{
			BigDecimal m = new BigDecimal(result);
			BigDecimal n = new BigDecimal(length);
			
			BigDecimal c = new BigDecimal(0);
			c=m.divide(n, 5, BigDecimal.ROUND_FLOOR);
			
			return c.doubleValue();
		}
	
	}
	
	public int diff(String orig, String eing) {
		  
		int matrix[][] = new int[orig.length() + 1][eing.length() + 1];
		for (int i = 0; i < orig.length() + 1; i++) {
			matrix[i][0] = i;
		 }
		for (int i = 0; i < eing.length() + 1; i++) {
		    matrix[0][i] = i;
		 }
		for (int a = 1; a < orig.length() + 1; a++) {
		   for (int b = 1; b < eing.length() + 1; b++) {
			   int right = 0;
		        if (orig.charAt(a - 1) != eing.charAt(b - 1)) {
		          right = 1;
		        }
		        int mini = matrix[a - 1][b] + 1;
		        if (matrix[a][b - 1] + 1 < mini) {
		          mini = matrix[a][b - 1] + 1;
		        }
		        if (matrix[a - 1][b - 1] + right < mini) {
		          mini = matrix[a - 1][b - 1] + right;
		        }
		        matrix[a][b] = mini;
		      }
		    }
		
		    return matrix[orig.length()][eing.length()];
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
