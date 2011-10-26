package org.dllearner.algorithm.tbsl.search;

import java.util.HashSet;
import java.util.Set;

public class SolrQueryResultSet {
	
	private Set<SolrQueryResultItem> items;
	
	public SolrQueryResultSet() {
		items = new HashSet<SolrQueryResultItem>();
	}
	
	public SolrQueryResultSet(Set<SolrQueryResultItem> items) {
		this.items = items;
	}
	
	public Set<SolrQueryResultItem> getItems() {
		return items;
	}
	
	public void addItems(Set<SolrQueryResultItem> items) {
		this.items.addAll(items);
	}
	
	public void add(SolrQueryResultSet rs) {
		this.items.addAll(rs.getItems());
	}
	
	

}
