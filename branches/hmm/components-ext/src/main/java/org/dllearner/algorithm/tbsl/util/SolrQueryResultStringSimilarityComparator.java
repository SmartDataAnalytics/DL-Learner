package org.dllearner.algorithm.tbsl.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.dllearner.algorithm.tbsl.search.SolrQueryResultItem;

public class SolrQueryResultStringSimilarityComparator implements Comparator<SolrQueryResultItem>{
	private String s;
	private Map<String, Double> cache;
	
	public SolrQueryResultStringSimilarityComparator(String s) {
		this.s = s;
		cache = new HashMap<String, Double>();
	}
	
	@Override
	public int compare(SolrQueryResultItem item1, SolrQueryResultItem item2) {
		
		double sim1 = 0;
		if(cache.containsKey(item1.getLabel())){
			sim1 = cache.get(item1.getLabel());
		} else {
			sim1 = Similarity.getSimilarity(s, item1.getLabel());
			cache.put(item1.getLabel(), sim1);
		}
		double sim2 = 0;
		if(cache.containsKey(item2.getLabel())){
			sim2 = cache.get(item2.getLabel());
		} else {
			sim2 = Similarity.getSimilarity(s, item2.getLabel());
			cache.put(item2.getLabel(), sim2);
		}
		 
		if(sim1 < sim2){
			return 1;
		} else if(sim1 > sim2){
			return -1;
		} else {
			int val = item1.getLabel().compareTo(item2.getLabel());
			if(val == 0){
				return item1.getUri().compareTo(item2.getUri());
			}
			return val;
		}
	}
	
}
