package org.dllearner.algorithm.tbsl.util;

import java.util.Comparator;

import org.dllearner.algorithm.tbsl.search.SolrQueryResultItem;

public class SolrQueryResultStringSimilarityComparator implements Comparator<SolrQueryResultItem>{
	private String s;
	
	public SolrQueryResultStringSimilarityComparator(String s) {
		this.s = s;
	}
	
	@Override
	public int compare(SolrQueryResultItem item1, SolrQueryResultItem item2) {
		
		double sim1 = Similarity.getSimilarity(s, item1.getLabel());
		double sim2 = Similarity.getSimilarity(s, item2.getLabel());
		
		if(sim1 < sim2){
			return 1;
		} else if(sim1 > sim2){
			return -1;
		} else {
			return item1.getLabel().compareTo(item2.getLabel());
		}
	}
	
}
