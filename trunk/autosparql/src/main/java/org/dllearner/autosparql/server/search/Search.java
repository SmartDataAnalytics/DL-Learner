package org.dllearner.autosparql.server.search;

import java.util.List;

import org.dllearner.autosparql.client.model.Example;

public interface Search {
	List<String> getResources(String queryString);
	List<String> getResources(String queryString, int offset);

	List<Example> getExamples(String queryString);
	List<Example> getExamples(String queryString, int offset);
	
	int getTotalHits(String queryString);
	void setHitsPerPage(int hitsPerPage);
	
	
}
