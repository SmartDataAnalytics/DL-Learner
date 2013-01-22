package org.dllearner.algorithms.qtl.operations;

import org.dllearner.algorithms.qtl.datastructures.QueryTree;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl;

import com.hp.hpl.jena.vocabulary.RDF;

public class Generalisation<N> {
	
	public QueryTree<N> generalise(QueryTree<N> queryTree){
		QueryTree<N> copy = new QueryTreeImpl<N>(queryTree);
		
		copy.setUserObject((N)"?");
		
		pruneTree(copy, 0.5);
		retainTypeEdges(copy);
		
		return copy;
	}
	
	private void replaceAllLeafs(QueryTree<N> queryTree){
		for(QueryTree<N> leaf : queryTree.getLeafs()){
			leaf.setUserObject((N)"?");
		}
		
	}
	
	private void pruneTree(QueryTree<N> tree, double limit){
		int childCountBefore = tree.getChildCount();
		for(QueryTree<N> child : tree.getChildren()){
			tree.removeChild((QueryTreeImpl<N>) child);
			if((double)tree.getChildCount()/childCountBefore <= 0.5){
				break;
			}
		}
	}
	
	private void retainTypeEdges(QueryTree<N> tree){
		for(QueryTree<N> child : tree.getChildren()){
			if(!tree.getEdge(child).equals(RDF.type.toString())){
				tree.removeChild((QueryTreeImpl<N>) child);
			}
		}
	}
	

}
