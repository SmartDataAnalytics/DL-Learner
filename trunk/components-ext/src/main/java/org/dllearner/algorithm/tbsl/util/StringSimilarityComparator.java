package org.dllearner.algorithm.tbsl.util;

import java.util.Comparator;

public class StringSimilarityComparator implements Comparator<String>{
	private String s;
	
	public StringSimilarityComparator(String s) {
		this.s = s;
	}
	
	@Override
	public int compare(String s1, String s2) {
		
		double sim1 = Similarity.getSimilarity(s, s1);
		double sim2 = Similarity.getSimilarity(s, s2);
		
		if(sim1 < sim2){
			return 1;
		} else if(sim1 > sim2){
			return -1;
		} else {
			return s1.compareTo(s2);
		}
	}
	
}
