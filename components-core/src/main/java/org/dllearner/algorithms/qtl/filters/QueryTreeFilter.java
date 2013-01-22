package org.dllearner.algorithms.qtl.filters;

import org.dllearner.algorithms.qtl.datastructures.QueryTree;

public interface QueryTreeFilter {
	
	QueryTree<String> getFilteredQueryTree(QueryTree<String> tree);

}
