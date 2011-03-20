package org.dllearner.algorithm.tbsl.search;

import java.util.List;

public interface Search {
	List<String> getResources(String queryString);
	List<String> getResources(String queryString, int offset);

	int getTotalHits(String queryString);
	void setHitsPerPage(int hitsPerPage);
	
	
}
