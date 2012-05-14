package org.dllearner.common.index;

import java.util.List;

public interface Index {
	List<String> getResources(String queryString);
	List<String> getResources(String queryString, int limit);
	List<String> getResources(String queryString, int limit, int offset);
}
