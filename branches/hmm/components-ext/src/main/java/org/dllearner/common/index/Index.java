package org.dllearner.common.index;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface Index {
	List<String> getResources(String queryString);
	List<String> getResources(String queryString, int limit);
	List<String> getResources(String queryString, int limit, int offset);
	IndexResultSet getResourcesWithScores(String queryString);
	IndexResultSet getResourcesWithScores(String queryString, int limit);
	IndexResultSet getResourcesWithScores(String queryString, int limit, int offset);
	IndexResultSet getResourcesWithScores(String queryString, int limit, int offset, Collection<String> additionalFields);
}
