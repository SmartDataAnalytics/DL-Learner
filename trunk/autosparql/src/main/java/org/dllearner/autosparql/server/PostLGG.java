package org.dllearner.autosparql.server;

import java.util.List;

import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;

public class PostLGG<N> {
	
	public void simplifyTree(QueryTree<N> tree, List<QueryTree<N>> negTrees){
		for(QueryTree<N> child : tree.getChildren()){
			
		}
	}
	
	private boolean existsPathInEveryTree(List<Object> path, List<QueryTree<N>> trees){
		for(QueryTree<N> tree : trees){
			if(!existsPath(path, tree)){
				return false;
			}
		}
		return true;
	}
	
	private boolean existsPath(List<Object> path, QueryTree<N> tree){
		boolean exists = false;
		Object edge = path.remove(0);
		for(QueryTree<N> child : tree.getChildren(edge)){
			exists = existsPath(path, child);
			if(exists){
				return true;
			}
		}
		return exists;
	}

}
