/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dllearner.algorithms.qtl.filters;

/**
 * @author Giorgos Stoilos (gstoil@image.ece.ntua.gr)
 * 
 * This class implements the string matching method proposed in the paper
 * "A String Metric For Ontology Alignment", published in ISWC 2005 
 *
 */
public class I_Sub {
	
	/**
	 * @param s1 input string 1
	 * @param s2 input string 2
	 * @param normaliseStrings a boolean value that specifies whether the two strings are to be normalised by 
	 * a custom normalisation algorithm that basically removes punctuation symbols and converts both input strings 
	 * to lower-case. Note that without normalisation the method is case sensitive.
	 * 
	 * @return degree of similarity between the two strings. 
	 */
	public double score(String s1, String s2, boolean normaliseStrings) {
		
		if( (s1 == null) || (s2 == null) )
				return -1;
		
		String inputStr1 = s1;
		String inputStr2 = s2;
		
		if( normaliseStrings ){
			s1 = s1.toLowerCase();
			s2 = s2.toLowerCase();
			
			s1 = normalizeString( s1 , '.' );
			s2 = normalizeString( s2 , '.' );
			s1 = normalizeString( s1 , '_' );
			s2 = normalizeString( s2 , '_' );
			s1 = normalizeString( s1 , ' ' );
			s2 = normalizeString( s2 , ' ' );
		}
		
		int l1 = s1.length(); // length of s
		int l2 = s2.length(); // length of t

		int L1 = l1;
		int L2 = l2;

		if ((L1 == 0) && (L2 == 0))
			return 1;
		if ((L1 == 0) || (L2 == 0))
			return -1;

		double common = 0;
		int best = 2;

		while( s1.length() > 0 && s2.length() > 0 && best != 0 ) {
			best = 0; // the best subs length so far

			l1 = s1.length(); // length of s
			l2 = s2.length(); // length of t

			int i = 0; // iterates through s1
			int j = 0; // iterates through s2

			int startS2 = 0;
			int endS2 = 0;
			int startS1 = 0;
			int endS1 = 0;
			int p=0;

			for( i = 0; (i < l1) && (l1 - i > best); i++) {
				j = 0;
				while (l2 - j > best) {
					int k = i;
					for(;(j < l2) && (s1.charAt(k) != s2.charAt(j)); j++);
			
					if (j != l2) { // we have found a starting point
						p = j;
						for (j++, k++;
							(j < l2) && (k < l1) && (s1.charAt(k) == s2.charAt(j));
							j++, k++);
						if( k-i > best){
							best = k-i;
							startS1 = i;
							endS1 = k;
							startS2 = p;
							endS2 = j;	
						}
					}
				}
			}
			char[] newString = new char[ s1.length() - (endS1 - startS1) ];
		
			j=0;
			for( i=0 ;i<s1.length() ; i++ ) {
				if( i>=startS1 && i< endS1 )
					continue;
				newString[j++] = s1.charAt( i );			
			}

			s1 = new String( newString );

			newString = new char[ s2.length() - ( endS2 - startS2 ) ];
			j=0;
			for( i=0 ;i<s2.length() ; i++ ) {
				if( i>=startS2 && i< endS2 )
					continue;
				newString[j++] = s2.charAt( i );
			}
			s2 = new String( newString );

			if( best > 2 )
				common += best;
			else
				best = 0;
		}

		double commonality = 0;
		double scaledCommon = 2*common /(L1+L2);
		commonality = scaledCommon;

		double winklerImprovement = winklerImprovement(inputStr1, inputStr2, commonality);
		double dissimilarity = 0;

		double rest1 = L1 - common;
		double rest2 = L2 - common;

		double unmatchedS1 = Math.max( rest1 , 0 );
		double unmatchedS2 = Math.max( rest2 , 0 );
		unmatchedS1 = rest1/L1;
		unmatchedS2 = rest2/L2;
		
		/** Hamacher Product */
		double suma = unmatchedS1 + unmatchedS2;
		double product = unmatchedS1 * unmatchedS2;
		double p = 0.6;   //For 1 it coincides with the algebraic product
		if( (suma-product) == 0 )
			dissimilarity = 0;
		else
			dissimilarity = (product)/(p+(1-p)*(suma-product));
		
		return commonality - dissimilarity + winklerImprovement;
	}

	private double winklerImprovement(String s1, String s2, double commonality) {

		int i;
		int n = Math.min( s1.length() , s2.length() );
		for( i=0 ; i<n ; i++ )
			if( s1.charAt( i ) != s2.charAt( i ) )
				break;

		return Math.min(4, i)*0.1*(1-commonality);
	}

	public String normalizeString(String str, char remo) {
		
		StringBuffer strBuf = new StringBuffer(); 
		for( int i=0 ; i<str.length() ; i++ ){
			if( str.charAt( i ) != remo )
				strBuf.append( str.charAt( i ) );
		}
		return strBuf.toString();		
	}
}