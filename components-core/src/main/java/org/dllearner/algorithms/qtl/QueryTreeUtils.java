/**
 * 
 */
package org.dllearner.algorithms.qtl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.dllearner.algorithms.qtl.datastructures.QueryTree;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl.NodeType;

/**
 * @author Lorenz Buehmann
 *
 */
public class QueryTreeUtils {
	
	/**
	 * Returns the path from the given node to the root of the given tree, i.e.
	 * a list of nodes starting from the given node.
	 * @param tree
	 * @param node
	 */
	public static <N> List<QueryTree<N>> getPathToRoot(QueryTree<N> tree, QueryTree<N> node) {
		if(node.isRoot()) {
			return Collections.singletonList(node);
		}
		List<QueryTree<N>> path = new ArrayList<QueryTree<N>>();
		
		// add node itself
		path.add(node);
		
		// add parent node
		QueryTree<N> parent = node.getParent();
		path.add(parent);
		
		// traversal up to root node
		while(!parent.isRoot()) {
			parent = parent.getParent();
			path.add(parent);
		}
		
		return path;
	}
	
	/**
	 * Print the path from the given node to the root of the given tree, i.e.
	 * a list of nodes starting from the given node.
	 * @param tree
	 * @param node
	 */
	public static <N> String printPathToRoot(QueryTree<N> tree, QueryTree<N> node) {
		List<QueryTree<N>> path = getPathToRoot(tree, node);
		
		StringBuilder sb = new StringBuilder();
		Iterator<QueryTree<N>> iterator = path.iterator();
		
		QueryTree<N> child = iterator.next();
		sb.append(child + "(" + child.getId() + ")");
		while (iterator.hasNext()) {
			QueryTree<N> parent = (QueryTree<N>) iterator.next();
			sb.append(" <").append(parent.getEdge(child)).append("> ");
			sb.append(parent + "(" + parent.getId() + ")");
			child = parent;
		}
		return sb.toString();
	}
	
	/**
	 * Returns all nodes in the given query tree, i.e. the closure of 
	 * the children.
	 * @param tree
	 * @return 
	 */
	public static <N> List<QueryTree<N>> getNodes(QueryTree<N> tree) {
		return tree.getChildrenClosure();
	}
	
	/**
	 * Returns all nodes of the given node type in the query tree, i.e. 
	 * the closure of the children.
	 * @param tree
	 * @return 
	 */
	public static <N> List<QueryTree<N>> getNodes(QueryTree<N> tree, NodeType nodeType) {
		// get all nodes
		List<QueryTree<N>> nodes = tree.getChildrenClosure();
		
		// filter by type
		Iterator<QueryTree<N>> iterator = nodes.iterator();
		while (iterator.hasNext()) {
			QueryTree<N> node = (QueryTree<N>) iterator.next();
			if(node.getNodeType() != nodeType) {
				iterator.remove();
			}
			
		}
		return nodes;
	}
	
	/**
	 * Returns the number of nodes in the given query tree, i.e. the number of 
	 * the children closure.
	 * @param tree
	 * @return 
	 */
	public static <N> int getNrOfNodes(QueryTree<N> tree) {
		return tree.getChildrenClosure().size();
	}
	
	/**
	 * Returns the set of edges that occur in the given query tree, i.e. the 
	 * closure of the edges.
	 * @param tree
	 * @return the set of edges in the query tree
	 */
	public static <N> List<QueryTree<N>> getEdges(QueryTree<N> tree) {
		return tree.getChildrenClosure();
	}
	
	/**
	 * Returns the number of edges that occur in the given query tree, which
	 * is obviously n-1 where n is the number of nodes.
	 * @param tree
	 * @return the set of edges in the query tree
	 */
	public static <N> int getNrOfEdges(QueryTree<N> tree) {
		return getNrOfNodes(tree) - 1;
	}
	
	/**
	 * Returns the complexity of the given query tree. 
	 * <div>
	 * Given a query tree T = (V,E) comprising a set V of vertices or nodes 
	 * together with a set E of edges or links. Moreover we have that 
	 * V = U ∪ L ∪ VAR , where U denotes the nodes that are URIs, L denotes
	 * the nodes that are literals and VAR contains the nodes that are variables.
	 * We define the complexity c(T) of query tree T as follows:
	 * </div>
	 * <code>c(T) = 1 + log(|U| * α + |L| * β + |VAR| * γ) </code>
	 * <div>
	 * with <code>α, β, γ</code> being weight of the particular node types.
	 * </div>
	 * @param tree
	 * @return the set of edges in the query tree
	 */
	public static <N> double getComplexity(QueryTree<N> tree) {
		
		double varNodeWeight = 0.8;
		double resourceNodeWeight = 1.0;
		double literalNodeWeight = 1.0;
		
		double complexity = 0;
		
		List<QueryTree<N>> nodes = getNodes(tree);
		for (QueryTree<N> node : nodes) {
			switch (node.getNodeType()) {
			case VARIABLE:
				complexity += varNodeWeight;
				break;
			case RESOURCE:
				complexity += resourceNodeWeight;
				break;
			case LITERAL:
				complexity += literalNodeWeight;
				break;
			default:
				break;
			}
		}
		
		return 1 + Math.log(complexity);
	}
	
	/**
	 * Determines if tree1 is subsumed by tree2, i.e. whether tree2 is more general than
	 * tree1.
	 * @param tree1
	 * @param tree2
	 * @return
	 */
    public static <N> boolean isSubsumedBy(QueryTree<N> tree1, QueryTree<N> tree2) {
    	// 1.compare the root nodes
    	// if both nodes denote the same resource or literal
    	if(tree1.isVarNode() && !tree2.isVarNode() && tree1.getUserObject().equals(tree2.getUserObject())){
    		return true;
    	}
    	
    	// if node2 is more specific than node1
    	if(tree1.isVarNode() && !tree2.isVarNode()) {
    		return false;
    	}
    	
    	// 2. compare the children
    	Object edge;
    	for(QueryTree<N> child2 : tree2.getChildren()){
    		boolean isSubsumed = false;
    		edge = tree2.getEdge(child2);
    		for(QueryTree<N> child1 : tree1.getChildren(edge)){
    			if(child1.isSubsumedBy(child2)){
    				isSubsumed = true;
    				break;
    			}
    		}
    		if(!isSubsumed){
				return false;
			}
    	}
    	return true;
    }
    
    /**
	 * Determines if the trees are equivalent from a subsumptional point of view.
	 * @param trees
	 * @return
	 */
    public static <N> boolean sameTrees(QueryTree<N>... trees) {
    	for(int i = 0; i < trees.length; i++) {
    		QueryTree<N> tree1 = trees[i];
    		for(int j = i; j < trees.length; j++) {
    			QueryTree<N> tree2 = trees[j];
    			if(!sameTrees(tree1, tree2)) {
    				return false;
    			}
        	}
    	}
    	
    	return true;
    }
    
	/**
	 * Determines if both trees are equivalent from a subsumptional point of
	 * view.
	 * 
	 * @param tree1
	 * @param tree2
	 * @return
	 */
	public static <N> boolean sameTrees(QueryTree<N> tree1, QueryTree<N> tree2) {
		return isSubsumedBy(tree1, tree2) && isSubsumedBy(tree2, tree1);
	}
	
	

}
