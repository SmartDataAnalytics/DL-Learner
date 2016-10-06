/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dllearner.algorithms.qtl.operations;

import org.apache.log4j.Logger;
import org.dllearner.algorithms.qtl.datastructures.QueryTree;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl;
import org.dllearner.kb.sparql.SparqlEndpoint;

import java.util.*;

/**
 * This class can be used to simplify a query tree. In general we use it after the LGG is computed, 
 * because the tree of the LGG can be further simplified (only as approximation) by using 
 * the negative trees. In particular there are currently 2 simplifications:
 * 
 * 1: 
 * If for a node n_1 there exists a path from n_1 to a node n_2 over the edge p, which is also contained
 * in every other tree, then we can generalise n_2 if it is a resource or remove n_2 if it is a variable.
 * 
 * 2:
 * If from a node n_1 there exists to edges p_1 and p_2 which always occur together in other trees
 * we can remove one edge.
 * 
 */
public class PostLGG<N> {
	
	private static final Logger logger = Logger.getLogger(PostLGG.class);
	
	private SparqlEndpoint endpoint;
	
	public PostLGG(SparqlEndpoint endpoint){
		this.endpoint = endpoint;
	}
	
	public PostLGG(){
		
	}
	
	public void simplifyTree(QueryTree<N> tree, List<QueryTree<N>> negTrees){
		if(tree.getChildren().isEmpty()){
			return;
		}
		
		if(logger.isDebugEnabled()){
			String s = tree.getStringRepresentation();
			logger.debug("Making post LGG simplification");
			logger.debug("LGG:\n" + s);
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
					if(!pathExists(leaf, new ArrayList<>(path), negTree)){
						pathExists = false;
						break;
					}
				}
//			}
//			if(logger.isInfoEnabled()){
//				logger.info("Exists: " + pathExists);
//			}
			if(pathExists){
				String pathString = "[" + leaf.getParent().getUserObject() + "--" + leaf.getParent().getEdge(leaf) + "--" + leaf.getUserObject() + "]";
				leaf.getParent().removeChild((QueryTreeImpl<N>) leaf);
				if(logger.isDebugEnabled()){
					logger.debug("Removing edge " + pathString + " from LGG because this occurs also in all negative trees.");
				}
			}
		}
//		checkSameEdgeOccurences(tree, negTrees);
//		if(logger.isDebugEnabled()){
//			logger.debug("Pruned tree:\n" + TreeHelper.getAbbreviatedTreeRepresentation(tree, endpoint.getBaseURI(), endpoint.getPrefixes()));
//		}
		
	}
	
	private void checkSameEdgeOccurences(QueryTree<N> tree, List<QueryTree<N>> negTrees){
		List<Object> path1;
		List<Object> path2;
		N label1;
		N label2;
		QueryTree<N> parent;
		Set<Integer> removedNodesIds = new HashSet<>();
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
								int ret = containsEdgeCombination(negTree, new ArrayList<>(path1), new ArrayList<>(path2), label1, label2);
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
		List<Object> path = new ArrayList<>();
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
		List<QueryTree<N>> nodes = new ArrayList<>();
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
				if(existsPath(new ArrayList<>(path), child)){
					return true;
				}
			}
		}
		return false;
	}

}
