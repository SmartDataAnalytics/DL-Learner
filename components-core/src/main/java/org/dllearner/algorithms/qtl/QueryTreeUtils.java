/**
 * 
 */
package org.dllearner.algorithms.qtl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.dllearner.algorithms.qtl.datastructures.QueryTree;

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
	 * NOTE: The current implementation just returns (1 + log(|T|)) where 
	 * |T| denotes the number of nodes of query tree T.
	 * @param tree
	 * @return the set of edges in the query tree
	 */
	public static <N> double getComplexity(QueryTree<N> tree) {
		return 1 + Math.log(getNrOfNodes(tree));
	}
	
	

}
