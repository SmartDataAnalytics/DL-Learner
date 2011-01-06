package org.dllearner.autosparql.server;

import java.util.List;

import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;

public class PostLGG<N> {
	
	public void simplifyTree(QueryTree<N> tree, List<QueryTree<N>> negTrees){
		for(QueryTree<N> child : tree.getChildren()){
			
		}
	}
	
	private boolean existsPathInEveryTree(List<QueryTree<N>> trees){
		boolean exists = true;
		
		for(QueryTree<N> tree : trees){
			
		}
		
		return exists;
	}
	
	private boolean existsPath(List<Object> path, QueryTree<N> tree){
		boolean exists = false;
		Object edge = path.remove(0);
		for(QueryTree<N> child : tree.getChildren(edge)){
			exists = existsPath(path, child);
		}
		return exists;
	}

}
