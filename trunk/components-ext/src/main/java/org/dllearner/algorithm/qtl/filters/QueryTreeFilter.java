package org.dllearner.algorithm.qtl.filters;

import org.dllearner.algorithm.qtl.datastructures.QueryTree;

public interface QueryTreeFilter {
	
	QueryTree<String> getFilteredQueryTree(QueryTree<String> tree);

}
