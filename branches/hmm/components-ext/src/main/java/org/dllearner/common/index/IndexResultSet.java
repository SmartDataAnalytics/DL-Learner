package org.dllearner.common.index;

import java.util.HashSet;
import java.util.Set;

public class IndexResultSet {
	
private Set<IndexResultItem> items;
	
	public IndexResultSet() {
		items = new HashSet<IndexResultItem>();
	}
	
	public IndexResultSet(Set<IndexResultItem> items) {
		this.items = items;
	}
	
	public Set<IndexResultItem> getItems() {
		return items;
	}
	
	public void addItem(IndexResultItem item) {
		this.items.add(item);
	}
	
	public void addItems(Set<IndexResultItem> items) {
		this.items.addAll(items);
	}
	
	public void add(IndexResultSet rs) {
		this.items.addAll(rs.getItems());
	}
	
	@Override
	public String toString() {
		return items.toString();
	}
	
	public boolean isEmpty(){
		return items.isEmpty();
	}

}
