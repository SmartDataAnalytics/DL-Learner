package org.dllearner.algorithm.tbsl.search;

import java.util.ArrayList;
import java.util.List;

public class ThresholdSlidingSolrSearch extends SolrSearch {
	
	private double minThreshold = 0.8;
	private double step = 0.1;
	
	public ThresholdSlidingSolrSearch(String solrServerURL) {
		super(solrServerURL);
	}
	
	public ThresholdSlidingSolrSearch(String solrServerURL, String searchField) {
		super(solrServerURL, searchField);
	}
	
	public ThresholdSlidingSolrSearch(String solrServerURL, String searchField, double minThreshold, double step) {
		super(solrServerURL, searchField);
		this.minThreshold = minThreshold;
		this.step = step;
	}
	
	@Override
	public List<String> getResources(String queryString, int limit, int offset) {
		List<String> resources = new ArrayList<String>();
		
		double threshold = 1;
		
		while(resources.size() < limit){
			resources.addAll(getResources(queryString + "~" + threshold, limit - resources.size()));
			threshold -= step;
			if(threshold < minThreshold){
				break;
			}
		}
		
		
		return resources;
	}
	
	

}
