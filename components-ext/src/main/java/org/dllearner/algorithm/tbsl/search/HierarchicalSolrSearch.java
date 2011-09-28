package org.dllearner.algorithm.tbsl.search;

import java.util.ArrayList;
import java.util.List;

public class HierarchicalSolrSearch extends SolrSearch {
	
	private SolrSearch primarySearch;
	private SolrSearch secondarySearch;
	
	public HierarchicalSolrSearch(SolrSearch primarySearch, SolrSearch secondarySearch) {
		this.primarySearch = primarySearch;
		this.secondarySearch = secondarySearch;
	}
	
	@Override
	public List<String> getResources(String queryString) {
		return getResources(queryString, 10, 0);
	}
	
	@Override
	public List<String> getResources(String queryString, int limit) {
		return getResources(queryString, limit, 0);
	}
	
	@Override
	public List<String> getResources(String queryString, int limit, int offset) {
		List<String> resources = new ArrayList<String>();
		resources = primarySearch.getResources(queryString, limit, offset);
		if(resources.size() < limit){
			resources.addAll(secondarySearch.getResources(queryString, limit-resources.size(), offset));
		}
		return resources;
	}

}
