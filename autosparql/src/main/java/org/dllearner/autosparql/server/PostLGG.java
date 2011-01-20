package org.dllearner.autosparql.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dllearner.autosparql.server.util.SPARQLEndpointEx;
import org.dllearner.autosparql.server.util.TreeHelper;
import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;
import org.dllearner.sparqlquerygenerator.datastructures.impl.QueryTreeImpl;

public class PostLGG<N> {
	
	private static final Logger logger = Logger.getLogger(PostLGG.class);
	
	private SPARQLEndpointEx endpoint;
	
	public PostLGG(SPARQLEndpointEx endpoint){
		this.endpoint = endpoint;
	}
	
	public void simplifyTree(QueryTree<N> tree, List<QueryTree<N>> negTrees){
		
		if(logger.isDebugEnabled()){
			logger.debug("Making post LGG simplification");
			logger.debug("LGG:\n" + TreeHelper.getAbbreviatedTreeRepresentation(tree, endpoint.getBaseURI(), endpoint.getPrefixes()));
			int i = 1;
//			for(QueryTree<N> negTree : negTrees){
//				logger.debug("Neg tree (" + i++ + "/" + negTrees.size() +"):\n" + TreeHelper.getAbbreviatedTreeRepresentation(negTree, endpoint.getBaseURI(), endpoint.getPrefixes()));
//			}
		}
		
		List<Object> path;
		boolean pathExists;
		for(QueryTree<N> leaf : tree.getLeafs()){
			pathExists = false;
			path = getPathFromRootToNode(leaf);
//			if(logger.isInfoEnabled()){
//				logger.info("Path: " + path);
//			}
//			if(leaf.getParent().getUserObject().equals("?")){
				pathExists = true;
				for(QueryTree<N> negTree : negTrees){
					if(!pathExists(leaf, new ArrayList<Object>(path), negTree)){
						pathExists = false;
						break;
					}
				}
//			}
//			if(logger.isInfoEnabled()){
//				logger.info("Exists: " + pathExists);
//			}
			if(pathExists){
				leaf.getParent().removeChild((QueryTreeImpl<N>) leaf);
			}
		}
		checkSameEdgeOccurences(tree, negTrees);
		if(logger.isDebugEnabled()){
			logger.debug("Pruned tree:\n" + TreeHelper.getAbbreviatedTreeRepresentation(tree, endpoint.getBaseURI(), endpoint.getPrefixes()));
		}
		
	}
	
	private void checkSameEdgeOccurences(QueryTree<N> tree, List<QueryTree<N>> negTrees){
		List<Object> path1;
		List<Object> path2;
		N label1;
		N label2;
		QueryTree<N> parent;
		Set<Integer> removedNodesIds = new HashSet<Integer>();
		for(QueryTree<N> leaf : tree.getLeafs()){
			if(!removedNodesIds.contains(leaf.getId())){
				parent = leaf.getParent();
				for(QueryTree<N> node1 : parent.getChildren()){
					for(QueryTree<N> node2 : parent.getChildren()){
						if(!node1.equals(node2) && !removedNodesIds.contains(node1.getId()) && !removedNodesIds.contains(node2.getId())) {
							path1 = getPathFromRootToNode(node1);
							path2 = getPathFromRootToNode(node2);
							label1 = node1.getUserObject();
							label2 = node2.getUserObject();
							
							boolean remove = false;
							for(QueryTree<N> negTree : negTrees){
								int ret = containsEdgeCombination(negTree, new ArrayList<Object>(path1), new ArrayList<Object>(path2), label1, label2);
								if(ret == -1){
									remove = false;
									break;
								} else if(ret == 1){
									remove = true;
								}
								
							}
							if(remove){
								if(!removedNodesIds.contains(node2.getId())){
									logger.debug("Removing\n" + node2.getParent().getEdge(node2) + "---" + node2 + "\n because always occurs together with\n"
											+ node1.getParent().getEdge(node1) + "---" + node1);
									node2.getParent().removeChild((QueryTreeImpl<N>) node2);
									removedNodesIds.add(node2.getId());
								}
							}
							
						}
					}
				}
			}
			
			
		}
	}
	
	private int containsEdgeCombination(QueryTree<N> tree, List<Object> path1, List<Object> path2, N label1, N label2){
		Object lastEdge1 = path1.remove(path1.size()-1);
		Object lastEdge2 = path2.remove(path2.size()-1);
		
		List<Object> path = path1;

		List<QueryTree<N>> nodes = getNodesByPath(tree, path);
		List<QueryTree<N>> children1;
		List<QueryTree<N>> children2;
		int ret = 0;
		if(nodes.isEmpty()){
			return 0;
		} else {
			for(QueryTree<N> node : nodes){
				children1 = node.getChildren(lastEdge1);
				children2 = node.getChildren(lastEdge2);
				boolean exists1 = false;
				boolean exists2 = false;
				for(QueryTree<N> child1 : children1){
					if(child1.getUserObject().equals(label1)){
						exists1 = true;
						break;
					}
				}
				for(QueryTree<N> child2 : children2){
					if(child2.getUserObject().equals(label2)){
						exists2 = true;
						break;
					}
				}
				if((exists1 && !exists2) || (!exists1 && exists2)){
					return -1;
				} else if(exists1 && exists2){
					ret = 1;
				}
			}
		}
		
		return ret;
	}
	
	private boolean pathExists(QueryTree<N> leaf, List<Object> path, QueryTree<N> tree){
		List<QueryTree<N>> negLeaves;
		Object lastEdge = path.remove(path.size()-1);
		
		for(QueryTree<N> node : getNodesByPath(tree, path)){
			negLeaves = node.getChildren(lastEdge);
			boolean exists = false;
			if(negLeaves.isEmpty()){
				return false;
			} else {
				if(leaf.getUserObject().equals("?")){
					return true;
				}
				for(QueryTree<N> negLeaf : negLeaves){
					if(negLeaf.getUserObject().equals(leaf.getUserObject())){
						exists = true;
						break;
					}
				}
			}
			if(!exists){
				return false;
			}
		}
		return true;
		
//		List<QueryTree<N>> negLeaves;
//		Object lastEdge = path.remove(path.size()-1);
//		for(QueryTree<N> node : getNodesByPath(tree, path)){
//			negLeaves = node.getChildren(lastEdge);
//			if(negLeaves.isEmpty()){
//				return false;
//			} else {
//				if(leaf.getUserObject().equals("?")){
//					return true;
//				}
//				for(QueryTree<N> negLeaf : negLeaves){
//					if(negLeaf.getUserObject().equals(leaf.getUserObject())){
//						return true;
//					}
//				}
//			}
//		}
//		return false;
		
//		for(QueryTree<N> node : getNodesByPath(tree, path)){
//			if(!node.getUserObject().equals(leaf.getUserObject())){
//				return false;
//			}
//		}
//		return true;
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
