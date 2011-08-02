package org.autosparql.server.search;

import java.util.List;

import org.autosparql.shared.Example;

public interface Search {
	
	List<String> getResources(String query);
	
	List<String> getResources(String query, int limit);
	
	List<String> getResources(String query, int limit, int offset);
	
	List<Example> getExamples(String query);
	
	List<Example> getExamples(String query, int limit);
	
	List<Example> getExamples(String query, int limit, int offset);

}
