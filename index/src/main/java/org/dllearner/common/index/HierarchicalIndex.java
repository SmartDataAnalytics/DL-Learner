package org.dllearner.common.index;

import java.util.Set;
import java.util.SortedSet;
import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class HierarchicalIndex extends Index
{
	private Index primaryIndex;
	private Index secondaryIndex;
	
	@Override
	public SortedSet<IndexItem> getResourcesWithScores(String queryString, int limit, int offset)
	{
		SortedSet<IndexItem> rs = primaryIndex.getResourcesWithScores(queryString, limit, offset);
		if(rs.size() < limit){
			rs.addAll(secondaryIndex.getResourcesWithScores(queryString, limit-rs.size(), offset));
		}
		return rs;
	}

}