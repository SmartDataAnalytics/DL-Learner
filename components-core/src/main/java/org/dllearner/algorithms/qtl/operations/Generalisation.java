package org.dllearner.algorithms.qtl.operations;

import java.util.ArrayList;

import org.dllearner.algorithms.qtl.QueryTreeUtils;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.vocabulary.RDF;

public class Generalisation {
	
	public RDFResourceTree generalise(RDFResourceTree queryTree){
		RDFResourceTree copy = new RDFResourceTree(queryTree);
		
		copy.setData(RDFResourceTree.DEFAULT_VAR_NODE);
		
		pruneTree(copy, 0.5);
		retainTypeEdges(copy);
		
		return copy;
	}
	
	private void replaceAllLeafs(RDFResourceTree queryTree){
		for(RDFResourceTree leaf : QueryTreeUtils.getLeafs(queryTree)){
			leaf.setData(RDFResourceTree.DEFAULT_VAR_NODE);
		}
	}
	
	private void pruneTree(RDFResourceTree tree, double limit){
		int childCountBefore = tree.getNumberOfChildren();
		for(RDFResourceTree child : tree.getChildren()){
			tree.removeChild(child);
			if((double)tree.getNumberOfChildren()/childCountBefore <= 0.5){
				break;
			}
		}
	}
	
	private void retainTypeEdges(RDFResourceTree tree){
		for(Node edge : tree.getEdges()) {
			if(!edge.equals(RDF.type.asNode())) {
				for(RDFResourceTree child : new ArrayList<>(tree.getChildren(edge))){
					tree.removeChild(child, edge);
				}
			}
		}
	}
}
