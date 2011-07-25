package org.autosparql.server.search;

import java.util.List;

public interface Search {
	
	List<String> getResources(String query);
	
	List<String> getResources(String query, int limit);
	
	List<String> getResources(String query, int limit, int offset);
	
	List<String> getExamples(String query);
	
	List<String> getExamples(String query, int limit);
	
	List<String> getExamples(String query, int limit, int offset);

}
