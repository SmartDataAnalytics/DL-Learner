package org.dllearner.autosparql.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;
import org.dllearner.sparqlquerygenerator.datastructures.impl.QueryTreeImpl;

public class PostLGG<N> {
	
	private static final Logger logger = Logger.getLogger(PostLGG.class);
	
	public void simplifyTree(QueryTree<N> tree, List<QueryTree<N>> negTrees){
		
		if(logger.isInfoEnabled()){
			logger.info("Making post LGG simplification");
			logger.info("LGG:\n" + tree.getStringRepresentation());
			for(QueryTree<N> negTree : negTrees){
				logger.info("Neg tree:\n" + negTree.getStringRepresentation());
			}
		}
		
		List<Object> path;
		boolean pathExists;
		for(QueryTree<N> leaf : tree.getLeafs()){
			pathExists = false;
			path = getPathFromRootToNode(leaf);
//			if(logger.isInfoEnabled()){
//				logger.info("Path: " + path);
//			}
			if(leaf.getParent().getUserObject().equals("?")){
				pathExists = true;
				for(QueryTree<N> negTree : negTrees){
					if(!pathExists(leaf, new ArrayList<Object>(path), negTree)){
						pathExists = false;
						break;
					}
				}
			}
//			if(logger.isInfoEnabled()){
//				logger.info("Exists: " + pathExists);
//			}
			if(pathExists){
				leaf.getParent().removeChild((QueryTreeImpl<N>) leaf);
			}
		}
		if(logger.isInfoEnabled()){
			logger.info("Pruned tree: " + tree.getStringRepresentation());
		}
	}
	
	private boolean pathExists(QueryTree<N> leaf, List<Object> path, QueryTree<N> tree){
		List<QueryTree<N>> negLeaves;
		Object lastEdge = path.remove(path.size()-1);
		for(QueryTree<N> node : getNodesByPath(tree, path)){
			negLeaves = node.getChildren(lastEdge);
			if(negLeaves.isEmpty()){
				break;
			} else {
				if(leaf.getUserObject().equals("?")){
					return true;
				}
				for(QueryTree<N> negLeaf : negLeaves){
					if(negLeaf.getUserObject().equals(leaf.getUserObject())){
						return true;
					}
				}
			}
		}
		return false;
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
	
	private List<QueryTree<N>> getNodesByPath(QueryTree<N> tree, List<Object> path){
		if(path.isEmpty()){
			return Collections.singletonList(tree);
		}
		List<QueryTree<N>> nodes = new ArrayList<QueryTree<N>>();
		Object edge = path.remove(0);
		for(QueryTree<N> child : tree.getChildren(edge)){
			if(path.isEmpty()){
				nodes.add(child);
			} else {
				nodes.addAll(getNodesByPath(child, path));
			}
		}
		
		return nodes;
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
