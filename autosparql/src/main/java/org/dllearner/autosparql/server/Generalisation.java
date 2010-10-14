package org.dllearner.autosparql.server;

import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;
import org.dllearner.sparqlquerygenerator.datastructures.impl.QueryTreeImpl;

import com.hp.hpl.jena.vocabulary.RDF;

public class Generalisation<N> {
	
	public QueryTree<N> generalise(QueryTree<N> queryTree){
		QueryTree<N> copy = new QueryTreeImpl<N>(queryTree);
		
		copy.setUserObject((N)"?");
		
		
//		retainTypeEdges(copy);
		pruneTree(copy, 0.5);
		
		return copy;
	}
	
	private void replaceAllLeafs(QueryTree<N> queryTree){
		for(QueryTree<N> leaf : queryTree.getLeafs()){
			leaf.setUserObject((N)"?");
		}
		
	}
	
	private void pruneTree(QueryTree<N> tree, double limit){
		System.out.println(tree.getStringRepresentation());
		int childCountBefore = tree.getChildCount();
		System.out.println("CHILD COUNT BEFORE: " + childCountBefore);
		for(QueryTree<N> child : tree.getChildren()){
			System.out.println("REMOVING CHILD: " + child);
			tree.removeChild((QueryTreeImpl<N>) child);
			System.out.println("CHILD COUNT: " + tree.getChildCount());
			System.out.println((double)tree.getChildCount()/childCountBefore);
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
