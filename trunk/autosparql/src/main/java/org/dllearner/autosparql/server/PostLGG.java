package org.dllearner.autosparql.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;

public class PostLGG<N> {
	
	public void simplifyTree(QueryTree<N> tree, List<QueryTree<N>> negTrees){
		List<Object> path;
		System.out.println("Direct children");
		for(QueryTree<N> child : tree.getChildren()){
			path = getPathFromRootToNode(child);
			System.out.println(path);
			System.out.println(existsPathInEveryTree(path, negTrees));
		}
		System.out.println("Leaf nodes");
		for(QueryTree<N> leaf : tree.getLeafs()){
			path = getPathFromRootToNode(leaf);
			System.out.println(path);
			System.out.println(existsPathInEveryTree(path, negTrees));
		}
	}
	
	private List<Object> getPathFromRootToNode(QueryTree<N> node){
		List<Object> path = new ArrayList<Object>();
		QueryTree<N> parent = node.getParent();
		path.add(parent.getEdge(node));
		if(!parent.isRoot()){
			path.addAll(getPathFromRootToNode(parent));
		}
		Collections.reverse(path);
		return path;
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
		Object edge = path.remove(0);
		if(!path.isEmpty()){
			for(QueryTree<N> child : tree.getChildren(edge)){
				if(existsPath(new ArrayList<Object>(path), child)){
					return true;
				}
			}
		}
		return false;
	}

}
