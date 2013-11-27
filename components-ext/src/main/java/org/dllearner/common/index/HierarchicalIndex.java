package org.dllearner.common.index;

import java.util.ArrayList;
import java.util.List;

public class HierarchicalIndex extends Index
{
	
	private Index primaryIndex;
	private Index secondaryIndex;
	
	public HierarchicalIndex(Index primaryIndex, Index secondaryIndex) {
		this.primaryIndex = primaryIndex;
		this.secondaryIndex = secondaryIndex;
	}
	
	public Index getPrimaryIndex() {
		return primaryIndex;
	}
	
	public Index getSecondaryIndex() {
		return secondaryIndex;
	}
	
	@Override
	public List<String> getResources(String queryString, int limit, int offset) {
		List<String> resources = new ArrayList<String>();
		resources = primaryIndex.getResources(queryString, limit, offset);
		if(resources.size() < limit){
			resources.addAll(secondaryIndex.getResources(queryString, limit-resources.size(), offset));
		}
		return resources;
	}

	@Override
	public IndexResultSet getResourcesWithScores(String queryString) {
		return getResourcesWithScores(queryString, DEFAULT_LIMIT);
	}

	@Override
	public IndexResultSet getResourcesWithScores(String queryString, int limit, int offset) {
		IndexResultSet rs = primaryIndex.getResourcesWithScores(queryString, limit, offset);
		if(rs.getItems().size() < limit){
			rs.add(secondaryIndex.getResourcesWithScores(queryString, limit-rs.getItems().size(), offset));
		}
		return rs;
	}

}