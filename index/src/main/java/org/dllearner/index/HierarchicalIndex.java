package org.dllearner.index;

import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class HierarchicalIndex extends Index
{
	private Index primaryIndex;
	private Index secondaryIndex;
	
	@Override
	public IndexResultSet getResourcesWithScores(String queryString, int limit)
	{
		IndexResultSet rs = primaryIndex.getResourcesWithScores(queryString, limit, offset);
		if(rs.size() < limit){
			rs.addAll(secondaryIndex.getResourcesWithScores(queryString, limit-rs.size(), offset));
		}
		return rs;
	}

}