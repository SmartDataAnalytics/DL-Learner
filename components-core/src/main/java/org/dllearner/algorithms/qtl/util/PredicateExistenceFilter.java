/**
 * 
 */
package org.dllearner.algorithms.qtl.util;

import java.util.HashSet;
import java.util.Set;

import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;

import com.hp.hpl.jena.graph.Node;

/**
 * A query tree filter that removes edges whose existence is supposed to be
 * semantically meaningless from user perspective.
 * @author Lorenz Buehmann
 *
 */
public class PredicateExistenceFilter {
	

	private Set<Node> existentialMeaninglessProperties = new HashSet<Node>();
	
	public PredicateExistenceFilter() {
	}

	public PredicateExistenceFilter(Set<Node> existentialMeaninglessProperties) {
		this.existentialMeaninglessProperties = existentialMeaninglessProperties;
	}
	
	/**
	 * @param existentialMeaninglessProperties the existential meaningless properties
	 */
	public void setExistentialMeaninglessProperties(Set<Node> existentialMeaninglessProperties) {
		this.existentialMeaninglessProperties = existentialMeaninglessProperties;
	}
	
	/**
	 * Returns a new tree based on the input tree.
	 * @param tree
	 * @return
	 */
	public RDFResourceTree filter(RDFResourceTree tree) {
		RDFResourceTree newTree = new RDFResourceTree(0, tree.getData());
		
		for(Node edge : tree.getEdges()) {
			if(existentialMeaninglessProperties.contains(edge)) {
				for (RDFResourceTree child : tree.getChildren(edge)) {
					if(!child.isVarNode() || !child.isLeaf()) {
						RDFResourceTree newChild = filter(child);
						newTree.addChild(newChild, edge);
					}
				}
			} else {
				for (RDFResourceTree child : tree.getChildren(edge)) {
					RDFResourceTree newChild = filter(child);
					newTree.addChild(newChild, edge);
				}
			}
		}
		
		return newTree;
	}

}
