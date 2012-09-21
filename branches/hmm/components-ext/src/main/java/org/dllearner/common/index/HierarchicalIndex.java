package org.dllearner.common.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class HierarchicalIndex implements Index{
	
	private static final int DEFAULT_LIMIT = 10;
	private static final int DEFAULT_OFFSET = 0;
	
	private Index primaryIndex;
	private Index secondaryIndex;
	
	public HierarchicalIndex(Index primaryIndex, Index secondaryIndex) {
		this.primaryIndex = primaryIndex;
		this.secondaryIndex = secondaryIndex;
	}

	@Override
	public List<String> getResources(String queryString) {
		return getResources(queryString, DEFAULT_LIMIT);
	}
	
	@Override
	public List<String> getResources(String queryString, int limit) {
		return getResources(queryString, limit, DEFAULT_OFFSET);
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
	public IndexResultSet getResourcesWithScores(String queryString, int limit) {
		return getResourcesWithScores(queryString, limit, DEFAULT_OFFSET);
	}

	@Override
	public IndexResultSet getResourcesWithScores(String queryString, int limit, int offset) {
		return getResourcesWithScores(queryString, limit, DEFAULT_OFFSET,Collections.<String>emptyList());
	}

	@Override public IndexResultSet getResourcesWithScores(String queryString, int limit, int offset,
			Collection<String> additionalFields)
	{
		IndexResultSet rs = primaryIndex.getResourcesWithScores(queryString, limit, offset, additionalFields);
		if(rs.getItems().size() < limit){
			rs.add(secondaryIndex.getResourcesWithScores(queryString, limit-rs.getItems().size(), offset,additionalFields));
		}
		return rs;
	}

}
