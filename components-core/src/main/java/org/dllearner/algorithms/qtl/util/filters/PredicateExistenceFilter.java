/**
 * 
 */
package org.dllearner.algorithms.qtl.util.filters;

import java.util.HashSet;
import java.util.Set;

import org.dllearner.algorithms.qtl.QueryTreeUtils;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.util.Entailment;

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
		RDFResourceTree newTree;
		if(tree.isLiteralNode() && !tree.isLiteralValueNode()) {
			newTree = new RDFResourceTree(tree.getDatatype());
		} else {
			newTree = new RDFResourceTree(0, tree.getData());
		}
		
		for(Node edge : tree.getEdges()) {
			if(existentialMeaninglessProperties.contains(edge)) {
				for (RDFResourceTree child : tree.getChildren(edge)) {
					if(child.isResourceNode() || child.isLiteralValueNode() || !child.isLeaf()) {
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
		
		// we have to run the subsumption check one more time to prune the tree
		for(Node edge : tree.getEdges()) {
			Set<RDFResourceTree> children2Remove = new HashSet<RDFResourceTree>();
			for (RDFResourceTree child1 : tree.getChildren(edge)) {
				for (RDFResourceTree child2 : tree.getChildren(edge)) {
					if(!child1.equals(child2) && !children2Remove.contains(child2)) {
						if(QueryTreeUtils.isSubsumedBy(child1, child2)) {
							children2Remove.add(child2);
						} else if(QueryTreeUtils.isSubsumedBy(child2, child1)) {
							children2Remove.add(child1);
						}
					}
				}
				
			}
			for (RDFResourceTree child : children2Remove) {
				tree.removeChild(child, edge);
			}
		}
		
		QueryTreeUtils.prune(newTree, null, Entailment.RDFS);
		
		return newTree;
	}

}
